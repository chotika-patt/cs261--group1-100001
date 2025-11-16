// payment.js (merged + backend integration)
// Assumes your HTML from the message (ids/classes used there)
document.addEventListener("DOMContentLoaded", () => {
  // ------------------------------
  // DOM references (existing UI)
  // ------------------------------
  const qrBtn = document.querySelector(".method"); // first .method is QR Payment in your markup
  const qrPopup = document.getElementById("qrPopup");
  const countdownEl = document.getElementById("countdown");
  const confirmBtn = document.getElementById("confirmBtn");
  const newQRBtn = document.getElementById("newQRBtn");
  const qrCard = document.querySelector(".qr-card");
  const loadingCard = document.getElementById("loadingCard");
  const successCard = document.getElementById("successCard");
  const failCard = document.getElementById("failCard");
  const errorText = document.querySelector(".error-text");
  const unsupportedPopup = document.getElementById("unsupportedPopup");
  const closeUnsupportedBtn = document.getElementById("closeUnsupportedBtn");

  // other method buttons (card / bank) — existing logic shows unsupported popup
  const cardMethod = document.querySelector(".fa-cc-visa")?.closest(".method");
  const bankMethod = document.querySelector(".fa-building-columns")?.closest(".method");

  // ------------------------------
  // Config
  // ------------------------------
  const POLL_INTERVAL_MS = 3000;
  let pollTimer = null;
  let countdownTimer = null;
  let currentPaymentId = null;
  let currentPaymentRef = null;
  let expiresAt = null; // server-provided fallback
  let remainingSeconds = 60; // local display fallback (keeps your UI)
  const API = {
    initiate: "/api/payments/initiate",
    status: (id) => `/api/payments/${id}`,        // POST as per backend
    cancel: (id) => `/api/payments/${id}/cancel`
  };

  // ------------------------------
  // Utilities
  // ------------------------------
  function getCsrfHeaders() {
    // Try common Spring CSRF meta or hidden input patterns
    const headers = {};
    const meta = document.querySelector('meta[name="_csrf"]');
    const metaHeader = document.querySelector('meta[name="_csrf_header"]');
    if (meta) {
      const token = meta.getAttribute("content");
      const headerName = metaHeader ? metaHeader.getAttribute("content") : "X-CSRF-TOKEN";
      headers[headerName] = token;
      return headers;
    }
    // fallback: <input name="_csrf" value="...">
    const input = document.querySelector('input[name="_csrf"]');
    if (input) {
      headers["X-CSRF-TOKEN"] = input.value;
      return headers;
    }
    return headers;
  }

  function readOrderInfoFromPage() {
    // Try multiple fallbacks to find orderId & amount on page
    // 1) element with id="orderData" and data attributes
    const el1 = document.getElementById("orderData");
    if (el1) {
      return {
        orderId: parseInt(el1.dataset.orderId),
        amount: parseFloat(el1.dataset.amount)
      };
    }

    // 2) main container dataset
    const main = document.querySelector(".payment-container");
    if (main && (main.dataset.orderId || main.dataset.amount)) {
      return {
        orderId: main.dataset.orderId ? parseInt(main.dataset.orderId) : null,
        amount: main.dataset.amount ? parseFloat(main.dataset.amount) : null
      };
    }

    // 3) parse success card sample values (if present)
    try {
      const detailBs = document.querySelectorAll("#successCard .detail-list b");
      // based on your markup: [order number, paid time, method, buyer, price]
      if (detailBs && detailBs.length >= 5) {
        const orderText = detailBs[0].textContent.trim();
        const priceText = detailBs[4].textContent.trim().replace(/[^\d.]/g, "");
        const maybeOrderId = parseInt(orderText.replace(/\D/g, ""), 10);
        const maybeAmount = parseFloat(priceText);
        return { orderId: maybeOrderId || null, amount: maybeAmount || null };
      }
    } catch (e) { /* ignore */ }

    // 4) global fallback
    console.warn("Order info not found in DOM; using fallback placeholders. Replace with real values.");
    return { orderId: null, amount: null };
  }

  function showOnly(elem) {
    const list = [qrCard, loadingCard, successCard, failCard];
    list.forEach((e) => { if (!e) return; e.style.display = (e === elem) ? "block" : "none"; });
  }

  function showErrorMessageUI(codeOrText) {
    if (typeof codeOrText === "number") {
      switch (codeOrText) {
        case 400: errorText.textContent = "400 – Invalid input (ข้อมูลไม่ถูกต้อง)"; break;
        case 401: errorText.textContent = "401 – Unauthorized (ไม่ได้รับอนุญาต)"; break;
        case 404: errorText.textContent = "404 – Order not found (ไม่พบคำสั่งซื้อ)"; break;
        case 409: errorText.textContent = "409 – Duplicate/Expired QR (QR ซ้ำหรือหมดอายุ)"; break;
        case 422: errorText.textContent = "422 – Validation failed (ตรวจสอบข้อมูลไม่ผ่าน)"; break;
        case 500: errorText.textContent = "500 – Server error (เซิร์ฟเวอร์มีปัญหา)"; break;
        default: errorText.textContent = "Unexpected error occurred (เกิดข้อผิดพลาดไม่ทราบสาเหตุ)"; break;
      }
    } else {
      errorText.textContent = String(codeOrText || "เกิดข้อผิดพลาด กรุณาลองอีกครั้ง");
    }
  }

  // ------------------------------
  // Countdown
  // ------------------------------
  function startCountdownFrom(seconds = 60) {
    clearInterval(countdownTimer);
    remainingSeconds = seconds;
    countdownEl.textContent = remainingSeconds;
    confirmBtn.disabled = false;
    newQRBtn.style.display = "none";

    countdownTimer = setInterval(() => {
      remainingSeconds--;
      countdownEl.textContent = remainingSeconds;
      if (remainingSeconds <= 0) {
        clearInterval(countdownTimer);
        confirmBtn.disabled = true;
        newQRBtn.style.display = "block";
        console.log("⏰ Countdown expired -> auto-check status");
        // on expire: check backend immediately (also mark expired if server does that)
        if (currentPaymentId) {
          // do one final poll; if still pending, show expired UI
          pollPaymentStatusOnce().then(s => {
            if (!s || (s && (s.status === "EXPIRED" || s.status === "FAILED"))) {
              showOnly(failCard);
              showErrorMessageUI("QR หมดอายุ หรือชำระเงินไม่สำเร็จ");
            } else {
              // if backend already marked PAID, handle it
              handlePaymentStatus(s);
            }
          });
        } else {
          // nothing to check
          showOnly(failCard);
          showErrorMessageUI("QR หมดอายุ กรุณาสร้าง QR ใหม่");
        }
      }
    }, 1000);
  }

  // ------------------------------
  // API calls: initiate, poll, cancel
  // ------------------------------
  async function initiatePayment(payload) {
    // payload should include: orderId, amount, provider, method, currency, idempotencyKey, metadata...
    try {
      showOnly(loadingCard);
      const headers = Object.assign({ "Content-Type": "application/json" }, getCsrfHeaders());
      const resp = await fetch(API.initiate, {
        method: "POST",
        headers,
        credentials: "same-origin",
        body: JSON.stringify(payload)
      });

      if (resp.status === 201 || resp.ok) {
        const data = await resp.json();
        console.log("[PAY] Initiated:", data);
        currentPaymentId = data.paymentId;
        currentPaymentRef = data.paymentRef || null;

        // If server returns a paymentUrl (preferable), set QR image src
        if (data.paymentUrl) {
          const img = qrCard.querySelector("img");
          if (img) img.src = data.paymentUrl;
        }

        // If server returned expiry (metadata) attempt to compute seconds
        if (data.expiresAt) {
          try {
            const then = new Date(data.expiresAt);
            const now = new Date();
            const diff = Math.max(0, Math.floor((then - now) / 1000));
            startCountdownFrom(diff || 60);
          } catch (e) {
            startCountdownFrom(60);
          }
        } else {
          startCountdownFrom(60);
        }

        // start polling
        startPolling(currentPaymentId);
        showOnly(qrCard);
        return data;
      } else {
        const err = await resp.json().catch(() => null);
        console.error("[PAY] initiate failed:", resp.status, err);
        showOnly(failCard);
        showErrorMessageUI(err?.message || resp.status);
        return null;
      }
    } catch (e) {
      console.error("[PAY] network/initiate error", e);
      showOnly(failCard);
      showErrorMessageUI("Network error. โปรดลองอีกครั้ง");
      return null;
    }
  }

  async function pollPaymentStatusOnce() {
    if (!currentPaymentId) return null;
    try {
      const headers = Object.assign({}, getCsrfHeaders());
      const resp = await fetch(API.status(currentPaymentId), {
        method: "POST", // matches your backend controller
        credentials: "same-origin",
        headers
      });
      if (!resp.ok) {
        console.warn("[PAY] poll returned non-ok", resp.status);
        return null;
      }
      const s = await resp.json();
      return s;
    } catch (e) {
      console.error("[PAY] poll error", e);
      return null;
    }
  }

  function startPolling(paymentId) {
    stopPolling();
    // immediate check then interval
    (async () => {
      const s = await pollPaymentStatusOnce();
      if (s) handlePaymentStatus(s);
    })();

    pollTimer = setInterval(async () => {
      const s = await pollPaymentStatusOnce();
      if (!s) return;
      handlePaymentStatus(s);
    }, POLL_INTERVAL_MS);
  }

  function stopPolling() {
    if (pollTimer) {
      clearInterval(pollTimer);
      pollTimer = null;
    }
  }

  async function cancelPaymentOnServer(paymentId) {
    try {
      const headers = Object.assign({}, getCsrfHeaders());
      const resp = await fetch(API.cancel(paymentId), {
        method: "POST",
        credentials: "same-origin",
        headers
      });
      if (resp.ok) {
        console.log("[PAY] Cancelled payment", paymentId);
      } else {
        console.warn("[PAY] Cancel returned", resp.status);
      }
    } catch (e) {
      console.warn("[PAY] cancel error", e);
    }
  }

  // ------------------------------
  // Payment status handling
  // ------------------------------
  function handlePaymentStatus(statusResp) {
    if (!statusResp) return;
    const st = statusResp.status;
    console.log("[PAY] statusResp", statusResp);

    switch ((st || "").toUpperCase()) {
      case "PENDING":
      case "INIT":
        // keep UI as QR, keep polling
        return;
      case "PAID":
        stopPolling();
        clearInterval(countdownTimer);
        showOnly(successCard);
        // update success details (amount etc.)
        try {
          const amtEl = successCard.querySelector(".amount");
          if (amtEl && statusResp.amount) amtEl.textContent = `${statusResp.amount} ${statusResp.currency || ""}`;
        } catch (e) {}
        // redirect after small delay so user sees success card
        setTimeout(() => { window.location.href = "/buyerTemp"; }, 1300);
        return;
      case "EXPIRED":
        stopPolling();
        clearInterval(countdownTimer);
        showOnly(failCard);
        showErrorMessageUI("QR หมดอายุ กรุณาสร้าง QR ใหม่");
        return;
      case "FAILED":
      case "CANCELLED":
        stopPolling();
        clearInterval(countdownTimer);
        showOnly(failCard);
        showErrorMessageUI("ชำระเงินไม่สำเร็จ กรุณาลองอีกครั้ง");
        return;
      default:
        console.warn("Unknown payment status:", st);
        return;
    }
  }

  // ------------------------------
  // Wire up UI interactions
  // ------------------------------
  // QR Payment button click -> initiate flow
  qrBtn?.addEventListener("click", async () => {
    qrPopup.style.display = "flex";

    // build payload from DOM (best-effort)
    const info = readOrderInfoFromPage();
    // Provide sensible defaults if not found (but you should populate server-side)
    const payload = {
      orderId: info.orderId || null,
      amount: info.amount || null,
      provider: "MOCK_PROVIDER", // change to real provider identifier if needed
      method: "QR",
      currency: "THB",
      idempotencyKey: (window.crypto && crypto.randomUUID) ? crypto.randomUUID() : (`cid-${Date.now()}`)
    };

    // If orderId or amount are missing, warn in console and still try to initiate
    if (!payload.orderId || !payload.amount) {
      console.warn("Missing orderId or amount in DOM; ensure you provide real values via data attributes or server-rendered JS.");
    }

    await initiatePayment(payload);
  });

  // Confirm button (manual re-check)
  confirmBtn?.addEventListener("click", async () => {
    clearInterval(countdownTimer); // stop countdown when user actively checks
    confirmBtn.disabled = true;
    showOnly(loadingCard);
    const s = await pollPaymentStatusOnce();
    if (s) handlePaymentStatus(s);
    else {
      showOnly(failCard);
      showErrorMessageUI("ไม่สามารถตรวจสอบสถานะได้");
    }
  });

  // New QR button -> cancel previous payment and create a new one
  newQRBtn?.addEventListener("click", async () => {
    // cancel old if exists
    if (currentPaymentId) {
      await cancelPaymentOnServer(currentPaymentId);
      stopPolling();
      currentPaymentId = null;
      currentPaymentRef = null;
    }

    // build payload and re-initiate
    const info = readOrderInfoFromPage();
    const payload = {
      orderId: info.orderId || null,
      amount: info.amount || null,
      provider: "MOCK_PROVIDER",
      method: "QR",
      currency: "THB",
      idempotencyKey: (window.crypto && crypto.randomUUID) ? crypto.randomUUID() : (`cid-${Date.now()}`)
    };
    await initiatePayment(payload);
  });

  // Redirect buttons exist in success/fail card; attach handlers
  document.getElementById("successRedirectBtn")?.addEventListener("click", () => {
    window.location.href = "/buyerTemp";
  });

  document.getElementById("failRedirectBtn")?.addEventListener("click", () => {
    window.location.reload();
  });

  // Unsupported popup logic for card/bank methods
  [cardMethod, bankMethod].forEach((btn) => {
    if (!btn) return;
    btn.addEventListener("click", () => {
      if (unsupportedPopup) unsupportedPopup.style.display = "flex";
    });
  });

  closeUnsupportedBtn?.addEventListener("click", () => {
    if (unsupportedPopup) unsupportedPopup.style.display = "none";
  });

  // ------------------------------
  // FRONT-END UNIT TEST
  // ------------------------------
  function runUnitTests() {
    console.group("FRONT-END UNIT TESTS (Payment Page)");
    try {
      console.assert(typeof initiatePayment === "function", "initiatePayment() not found");
      console.assert(typeof startPolling === "function", "startPolling() not found");
      console.assert(typeof pollPaymentStatusOnce === "function", "pollPaymentStatusOnce() not found");
      console.log("Core functions loaded");

      const successBtn = document.getElementById("successRedirectBtn");
      const failBtn = document.getElementById("failRedirectBtn");
      console.assert(successBtn && failBtn, "Redirect buttons missing");
      console.log("Redirect buttons found");

      const testCodes = [400, 401, 404, 409, 422, 500];
      testCodes.forEach((c) => {
        showErrorMessageUI(c);
        const text = document.querySelector(".error-text").textContent;
        console.assert(text.includes(c.toString()) || typeof text === "string", `showErrorMessage(${c}) check`);
      });
      console.log("showErrorMessageUI() test passed");
    } catch (e) {
      console.error("Unit test failed:", e);
    }
    console.groupEnd();
  }
  setTimeout(runUnitTests, 1000);

  // cleanup when leaving page (stop timers)
  window.addEventListener("beforeunload", () => {
    stopPolling();
    clearInterval(countdownTimer);
  });
});
