document.addEventListener("DOMContentLoaded", () => {
    // ตัวแปร DOM หลัก
    const qrBtn = document.querySelector(".method"); // ปุ่ม QR Payment ตัวแรก
    const qrPopup = document.getElementById("qrPopup");
    const countdownEl = document.getElementById("countdown");
    const confirmBtn = document.getElementById("confirmBtn");
    const newQRBtn = document.getElementById("newQRBtn");
    const qrCard = document.querySelector(".qr-card");
    const loadingCard = document.getElementById("loadingCard");
    const successCard = document.getElementById("successCard");
    const failCard = document.getElementById("failCard");
    const errorText = document.querySelector(".error-text");

    //  เปิด popup เมื่อกด QR Payment
    qrBtn.addEventListener("click", () => {
        qrPopup.style.display = "flex";
        startCountdown();
    });

    // ⏳ ฟังก์ชัน Countdown
    let timer;
    function startCountdown() {
        let time = 60;
        countdownEl.textContent = time;
        confirmBtn.disabled = false;
        newQRBtn.style.display = "none";

        timer = setInterval(() => {
            time--;
            countdownEl.textContent = time;

            if (time <= 0) {
                clearInterval(timer);
                confirmBtn.disabled = true;
                newQRBtn.style.display = "block";

                console.log("⏰ Countdown หมดเวลา! เรียกตรวจสอบการชำระเงินอัตโนมัติ...");
                autoValidatePayment();
            }
        }, 1000);
    }

    //  ฟังก์ชันจำลองตรวจสอบ API หลังหมดเวลา
    function autoValidatePayment() {
        qrCard.style.display = "none";
        loadingCard.style.display = "block";

        setTimeout(() => {
            loadingCard.style.display = "none";

            const apiCodes = [200, 400, 401, 404, 409, 422, 500];
            const randomCode = apiCodes[Math.floor(Math.random() * apiCodes.length)];
            console.log("🔄 Auto Validate API Code:", randomCode);

            if (randomCode === 200) {
                successCard.style.display = "block";
            } else {
                failCard.style.display = "block";
                showErrorMessage(randomCode);
            }
        }, 2500);
    }

    //  ฟังก์ชันแสดงข้อความ Error ตาม API Code
    function showErrorMessage(code) {
        switch (code) {
            case 400:
                errorText.textContent = "400 – Invalid input (ข้อมูลไม่ถูกต้อง)";
                break;
            case 401:
                errorText.textContent = "401 – Unauthorized (ไม่ได้รับอนุญาต)";
                break;
            case 404:
                errorText.textContent = "404 – Order not found (ไม่พบคำสั่งซื้อ)";
                break;
            case 409:
                errorText.textContent = "409 – Duplicate/Expired QR (QR ซ้ำหรือหมดอายุ)";
                break;
            case 422:
                errorText.textContent = "422 – Validation failed (ตรวจสอบข้อมูลไม่ผ่าน)";
                break;
            case 500:
                errorText.textContent = "500 – Server error (เซิร์ฟเวอร์มีปัญหา)";
                break;
            default:
                errorText.textContent = "Unexpected error occurred (เกิดข้อผิดพลาดไม่ทราบสาเหตุ)";
        }
    }

    // ปุ่ม “ยืนยันการชำระเงิน”
    confirmBtn.addEventListener("click", () => {
        clearInterval(timer);
        qrCard.style.display = "none";
        loadingCard.style.display = "block";

        setTimeout(() => {
            loadingCard.style.display = "none";
            const isSuccess = Math.random() > 0.5;
            if (isSuccess) {
                successCard.style.display = "block";
            } else {
                failCard.style.display = "block";
                showErrorMessage(400 + Math.floor(Math.random() * 5));
            }
        }, 2500);
    });
    //  ปุ่ม “สร้าง QR ใหม่”
    newQRBtn.addEventListener("click", () => {
        qrCard.style.display = "block";
        successCard.style.display = "none";
        failCard.style.display = "none";
        startCountdown();
    });


    //  ปุ่ม Redirect (ต้องรอ DOM โหลดก่อนถึงจะเจอ)

    document.getElementById("successRedirectBtn").addEventListener("click", () => {
        window.location.href = "/buyerTemp";
    });

    document.getElementById("failRedirectBtn").addEventListener("click", () => {
        window.location.reload();
    });


    // FRONT-END UNIT TEST

    function runUnitTests() {
        console.group("FRONT-END UNIT TESTS (Payment Page)");

        try {
            console.assert(typeof startCountdown === "function", "startCountdown() not found");
            console.assert(typeof autoValidatePayment === "function", "autoValidatePayment() not found");
            console.assert(typeof showErrorMessage === "function", "showErrorMessage() not found");
            console.log("Core functions loaded");

            const successBtn = document.getElementById("successRedirectBtn");
            const failBtn = document.getElementById("failRedirectBtn");
            console.assert(successBtn && failBtn, "Redirect buttons missing");
            console.log("Redirect buttons found");

            const testCodes = [400, 401, 404, 409, 422, 500];
            testCodes.forEach((c) => {
                showErrorMessage(c);
                const text = document.querySelector(".error-text").textContent;
                console.assert(text.includes(c.toString()), `showErrorMessage(${c}) failed`);
            });
            console.log("showErrorMessage() test passed");

        } catch (e) {
            console.error("Unit test failed:", e);
        }

        console.groupEnd();
    }

    //  รันทดสอบหลังโหลดเสร็จ
    setTimeout(runUnitTests, 1000);
    // Popup แจ้งเตือนวิธีที่ยังไม่รองรับ
    const unsupportedPopup = document.getElementById("unsupportedPopup");
    const closeUnsupportedBtn = document.getElementById("closeUnsupportedBtn");

// ดึงปุ่ม Pay by Card และ Internet Banking
    const cardMethod = document.querySelector(".fa-cc-visa").closest(".method");
    const bankMethod = document.querySelector(".fa-building-columns").closest(".method");

// แสดง popup เมื่อกดปุ่มใดปุ่มหนึ่ง
    [cardMethod, bankMethod].forEach((btn) => {
        btn.addEventListener("click", () => {
            unsupportedPopup.style.display = "flex";
        });
    });

// ปุ่ม “ตกลง” ปิด popup
    closeUnsupportedBtn.addEventListener("click", () => {
        unsupportedPopup.style.display = "none";
    });

});
