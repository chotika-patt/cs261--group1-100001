document.addEventListener("DOMContentLoaded", () => {
  // ------------------------------
  // DOM references
  // ------------------------------
  const qrBtn = document.getElementById("qrMethodBtn");
  const qrPopup = document.getElementById("qrPopup");
  const countdownEl = document.getElementById("countdown");
  const confirmBtn = document.getElementById("confirmBtn");
  const newQRBtn = document.getElementById("newQRBtn");
  const qrCard = document.querySelector(".qr-card");
  const loadingCard = document.getElementById("loadingCard");
  const successCard = document.getElementById("successCard");
  const failCard = document.getElementById("failCard");
  const unsupportedPopup = document.getElementById("unsupportedPopup");
  const closeUnsupportedBtn = document.getElementById("closeUnsupportedBtn");

  const cardMethod = document.querySelector(".fa-cc-visa")?.closest(".method");
  const bankMethod = document.querySelector(".fa-building-columns")?.closest(".method");

  // ------------------------------
  // Config / State
  // ------------------------------
  const POLL_INTERVAL_MS = 3000;
  const MAX_POLL_ATTEMPTS = 40;
  const CONFIRM_SIMULATED_DELAY_MS = 1500; // <-- change this to increase/decrease loading delay
  let pollTimer = null;
  let pollAttempts = 0;
  let countdownTimer = null;
  let currentPaymentId = null;
  let currentPaymentRef = null;
  let remainingSeconds = 60;
  let lastKnownAmount = null;
  let lastKnownCurrency = "THB";

  const API = {
    initiate: "/api/payments/initiate",
    status: (id) => `/api/payments/${id}`,
    cancel: (id) => `/api/payments/${id}/cancel`,
    createDraft: "/api/orders/draft",
    markPaid: (id) => `/api/payments/${id}/markPaid`
  };

  // ------------------------------
  // Utilities
  // ------------------------------
  function getCsrfHeaders() {
    const headers = {};
    const meta = document.querySelector('meta[name="_csrf"]');
    const metaHeader = document.querySelector('meta[name="_csrf_header"]');
    if (meta) {
      const token = meta.getAttribute("content");
      const headerName = metaHeader ? metaHeader.getAttribute("content") : "X-CSRF-TOKEN";
      headers[headerName] = token;
      return headers;
    }
    const input = document.querySelector('input[name="_csrf"]');
    if (input) {
      headers["X-CSRF-TOKEN"] = input.value;
      return headers;
    }
    return headers;
  }

  function readOrderInfoFromPage() {
    const el1 = document.getElementById("orderData");
    if (el1) {
      return {
        orderId: el1.dataset.orderId ? parseInt(el1.dataset.orderId) : null,
        amount: el1.dataset.amount ? parseFloat(el1.dataset.amount) : null
      };
    }
    const main = document.querySelector(".payment-container");
    if (main) {
      return {
        orderId: main.dataset.orderId ? parseInt(main.dataset.orderId) : null,
        amount: main.dataset.amount ? parseFloat(main.dataset.amount) : null
      };
    }
    return { orderId: null, amount: null };
  }

  function showOnly(elem) {
    const list = [qrCard, loadingCard, successCard, failCard];
    list.forEach((e) => { if (!e) return; e.style.display = (e === elem) ? "block" : "none"; });
  }

  function setInlineError(text) {
    const el = qrCard ? qrCard.querySelector(".error-text") : document.querySelector(".error-text");
    if (el) el.textContent = text || "";
  }

  function showErrorMessageUI(codeOrText) {
    const errEl = document.querySelector(".qr-card .error-text") || document.querySelector(".error-text");
    if (!errEl) return;
    if (typeof codeOrText === "number") {
      switch (codeOrText) {
        case 400: errEl.textContent = "400 – Invalid input (ข้อมูลไม่ถูกต้อง)"; break;
        case 401: errEl.textContent = "401 – Unauthorized (ไม่ได้รับอนุญาต)"; break;
        case 404: errEl.textContent = "404 – Not found (ไม่พบ)"; break;
        case 409: errEl.textContent = "409 – Duplicate/Expired QR (QR ซ้ำหรือหมดอายุ)"; break;
        case 422: errEl.textContent = "422 – Validation failed (ตรวจสอบข้อมูลไม่ผ่าน)"; break;
        case 500: errEl.textContent = "500 – Server error (เซิร์ฟเวอร์มีปัญหา)"; break;
        default: errEl.textContent = "เกิดข้อผิดพลาด กรุณาลองอีกครั้ง"; break;
      }
    } else {
      errEl.textContent = String(codeOrText || "เกิดข้อผิดพลาด กรุณาลองอีกครั้ง");
    }
  }

  // ------------------------------
  // Countdown
  // ------------------------------
  function startCountdownFrom(seconds = 60) {
    clearInterval(countdownTimer);
    remainingSeconds = seconds || 60;
    if (countdownEl) countdownEl.textContent = remainingSeconds;
    confirmBtn.disabled = false;
    newQRBtn.style.display = "none";

    countdownTimer = setInterval(() => {
      remainingSeconds--;
      if (countdownEl) countdownEl.textContent = remainingSeconds;
      if (remainingSeconds <= 0) {
        clearInterval(countdownTimer);
        confirmBtn.disabled = true;
        newQRBtn.style.display = "block";
        if (currentPaymentId) {
          pollPaymentStatusOnce().then(s => {
            if (!s || ["EXPIRED","FAILED"].includes((s.status||"").toUpperCase())) {
              showOnly(failCard);
              showErrorMessageUI("QR หมดอายุ หรือชำระเงินไม่สำเร็จ");
            } else {
              handlePaymentStatus(s);
            }
          });
        } else {
          showOnly(failCard);
          showErrorMessageUI("QR หมดอายุ กรุณาสร้าง QR ใหม่");
        }
      }
    }, 1000);
  }

  // ------------------------------
  // API calls
  // ------------------------------
  async function initiatePayment(payload) {
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
        currentPaymentId = data.paymentId;
        currentPaymentRef = data.paymentRef || null;
        lastKnownAmount = data.amount || payload.amount || lastKnownAmount;
        lastKnownCurrency = data.currency || lastKnownCurrency;

        // show QR if provider returned paymentUrl
        if (data.paymentUrl) {
          const img = qrCard.querySelector("img");
          if (img) img.src = data.paymentUrl;
        }

        // show the QR UI now
        showOnly(qrCard);

        // start countdown
        if (data.expiresAt) {
          try {
            const then = new Date(data.expiresAt);
            const diff = Math.max(0, Math.floor((then - new Date()) / 1000));
            startCountdownFrom(diff || 60);
          } catch (e) { startCountdownFrom(60); }
        } else {
          startCountdownFrom(60);
        }

        // start polling
        startPolling(currentPaymentId);
        setInlineError("");
        return data;
      } else {
        const err = await resp.json().catch(() => null);
        showOnly(failCard);
        showErrorMessageUI(err?.message || resp.status);
        return null;
      }
    } catch (e) {
      console.error("initiate error", e);
      showOnly(failCard);
      showErrorMessageUI("Network error. โปรดลองอีกครั้ง");
      return null;
    }
  }

  // Poll once, return JSON or wrapper {__errorStatus:status} or null
  async function pollPaymentStatusOnce() {
    if (!currentPaymentId) return null;
    try {
      const headers = Object.assign({}, getCsrfHeaders());
      const resp = await fetch(API.status(currentPaymentId), {
        method: "POST",
        credentials: "same-origin",
        headers
      });
      if (!resp.ok) {
        console.warn("[PAY] poll non-ok", resp.status);
        return { __errorStatus: resp.status };
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
    pollAttempts = 0;

    // Ensure QR UI visible
    if (qrCard) showOnly(qrCard);

    // immediate check
    (async () => {
      const s = await pollPaymentStatusOnce();
      if (s) {
        if (s.__errorStatus) {
          showOnly(failCard);
          showErrorMessageUI(`Server returned ${s.__errorStatus}`);
          stopPolling();
          return;
        }
        handlePaymentStatus(s);
      }
    })();

    pollTimer = setInterval(async () => {
      pollAttempts++;
      if (pollAttempts >= MAX_POLL_ATTEMPTS) {
        console.warn("[PAY] max poll attempts reached, stopping client poll");
        stopPolling();
        clearInterval(countdownTimer);
        showOnly(failCard);
        showErrorMessageUI("การตรวจสอบสถานะนานเกินไป กรุณาลองอีกครั้งหรือติดต่อผู้ดูแลระบบ");
        return;
      }

      const s = await pollPaymentStatusOnce();
      if (!s) return;

      if (s.__errorStatus) {
        showOnly(failCard);
        showErrorMessageUI(`Server returned ${s.__errorStatus}`);
        stopPolling();
        return;
      }

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
    if (!paymentId) return;
    try {
      const headers = Object.assign({}, getCsrfHeaders());
      await fetch(API.cancel(paymentId), {
        method: "POST",
        credentials: "same-origin",
        headers
      });
    } catch (e) { /* ignore */ }
  }

  // ------------------------------
  // Payment status handling
  // ------------------------------
  function handlePaymentStatus(statusResp) {
    if (!statusResp) return;
    const st = (statusResp.status || "").toUpperCase();
    switch (st) {
      case "PENDING":
      case "INIT":
        return;
      case "PAID":
        stopPolling();
        clearInterval(countdownTimer);
        try {
          const amtEl = successCard.querySelector(".amount");
          const amountToShow = statusResp.amount || lastKnownAmount || "";
          const currencyToShow = statusResp.currency || lastKnownCurrency || "";
          if (amtEl && amountToShow !== null) amtEl.textContent = `${amountToShow} ${currencyToShow}`;

          const detailBs = successCard.querySelectorAll(".detail-list b");
          if (detailBs && detailBs.length >= 5) {
            detailBs[0].textContent = (statusResp.paymentId || currentPaymentId) + "";
            detailBs[1].textContent = new Date().toLocaleString();
            detailBs[2].textContent = "QR payment";
            detailBs[3].textContent = document.querySelector('span.user-text')?.textContent || "";
            detailBs[4].textContent = `${amountToShow || ''} ${currencyToShow || ''}`;
          }
        } catch (e) {}
        showOnly(successCard);
        setTimeout(()=> window.location.href = "/buyerTemp", 1300);
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
  // create order draft if missing
  // ------------------------------
  async function ensureOrderDraftExists() {
    try {
      const headers = Object.assign({ "Content-Type": "application/json" }, getCsrfHeaders());
      const resp = await fetch(API.createDraft, {
        method: "POST",
        credentials: "same-origin",
        headers,
        body: JSON.stringify({})
      });

      if (!resp.ok) {
        console.warn("Draft creation returned", resp.status);
        return null;
      }

      const data = await resp.json();
      const orderId = data.orderId || data.order_id || data.id || null;
      const amount = data.totalAmount || data.total_amount || data.totalAmount || null;
      return { orderId, amount };
    } catch (e) {
      console.error("Draft creation error", e);
      return null;
    }
  }

  // ------------------------------
  // UI interactions
  // ------------------------------
  qrBtn?.addEventListener("click", async () => {
    qrPopup.style.display = "flex";
    setInlineError("");
    const infoInitial = readOrderInfoFromPage();
    let info = infoInitial;

    if (!info.orderId || !info.amount) {
      const draft = await ensureOrderDraftExists();
      if (draft && draft.orderId) {
        const orderDataEl = document.getElementById('orderData');
        if (orderDataEl) {
          orderDataEl.dataset.orderId = draft.orderId;
          orderDataEl.dataset.amount = draft.amount;
        } else {
          const main = document.querySelector('.payment-container');
          if (main) {
            main.dataset.orderId = draft.orderId;
            main.dataset.amount = draft.amount;
          }
        }
        info = { orderId: draft.orderId, amount: draft.amount };
      } else {
        showOnly(qrCard);
        showErrorMessageUI("ยังไม่มีคำสั่งซื้อ — กรุณากลับไปตะกร้าสินค้าแล้วชำระ หรือเข้าสู่ระบบ");
        return;
      }
    }

    if (!info.orderId || !info.amount) {
      showOnly(qrCard);
      showErrorMessageUI("orderId หรือ amount หายไป กรุณาลองอีกครั้ง");
      return;
    }

    // save last known amount so confirm can use it
    lastKnownAmount = info.amount;
    lastKnownCurrency = "THB";

    const payload = {
      orderId: info.orderId,
      amount: info.amount,
      provider: "MOCK_PROVIDER",
      method: "QR",
      currency: "THB",
      idempotencyKey: (window.crypto && crypto.randomUUID) ? crypto.randomUUID() : (`cid-${Date.now()}`)
    };

    await initiatePayment(payload);
  });

  // ------------------------------
  // Confirm button: simulate PAID after loading delay, then call server to finalize
  // ------------------------------
  confirmBtn?.addEventListener("click", async () => {
    // disable the confirm button to avoid double-clicks
    confirmBtn.disabled = true;

    // stop countdown & polling so client won't overwrite simulated result
    clearInterval(countdownTimer);
    stopPolling();

    // show loading card to simulate processing
    showOnly(loadingCard);

    // wait for configured delay, then simulate PAID and call backend
    setTimeout(async () => {
      const simulated = {
        paymentId: currentPaymentId,
        status: "PAID",
        paymentRef: currentPaymentRef || ("SIM-" + (currentPaymentId || Date.now())),
        amount: lastKnownAmount || 0,
        currency: lastKnownCurrency || "THB"
      };

      // Call server to mark payment as paid -> server will update payment, order and clear cart
      try {
        const headers = Object.assign({ "Content-Type": "application/json" }, getCsrfHeaders());
        const resp = await fetch(API.markPaid(currentPaymentId), {
          method: "POST",
          credentials: "same-origin",
          headers
        });

        if (!resp.ok) {
          let body = null;
          try { body = await resp.json(); } catch (e) { body = await resp.text().catch(()=>null); }
          console.error("markPaid failed", resp.status, body);
          showOnly(failCard);
          showErrorMessageUI(body?.message || "ไม่สามารถยืนยันการชำระเงินได้");
          return;
        }

        // success — update UI to PAID
        handlePaymentStatus(simulated);
      } catch (e) {
        console.error("markPaid error", e);
        showOnly(failCard);
        showErrorMessageUI("Network error while finalizing payment");
      }
    }, CONFIRM_SIMULATED_DELAY_MS);
  });

  // New QR logic (re-initiate)
  newQRBtn?.addEventListener("click", async () => {
    if (currentPaymentId) {
      await cancelPaymentOnServer(currentPaymentId);
      stopPolling();
      currentPaymentId = null;
      currentPaymentRef = null;
    }

    const info = readOrderInfoFromPage();
    if (!info.orderId || !info.amount) {
      showOnly(qrCard);
      showErrorMessageUI("ไม่พบคำสั่งซื้อ กรุณาลองอีกครั้ง");
      return;
    }

    lastKnownAmount = info.amount;
    lastKnownCurrency = "THB";

    const payload = {
      orderId: info.orderId,
      amount: info.amount,
      provider: "MOCK_PROVIDER",
      method: "QR",
      currency: "THB",
      idempotencyKey: (window.crypto && crypto.randomUUID) ? crypto.randomUUID() : (`cid-${Date.now()}`)
    };
    await initiatePayment(payload);
  });

  document.getElementById("successRedirectBtn")?.addEventListener("click", () => {
    window.location.href = "/buyerTemp";
  });

  document.getElementById("failRedirectBtn")?.addEventListener("click", () => {
    window.location.reload();
  });

  [cardMethod, bankMethod].forEach((btn) => {
    if (!btn) return;
    btn.addEventListener("click", () => {
      if (unsupportedPopup) unsupportedPopup.style.display = "flex";
    });
  });

  closeUnsupportedBtn?.addEventListener("click", () => {
    if (unsupportedPopup) unsupportedPopup.style.display = "none";
  });

  window.addEventListener("beforeunload", () => {
    stopPolling();
    clearInterval(countdownTimer);
  });

});