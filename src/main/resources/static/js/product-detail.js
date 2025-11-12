document.addEventListener("DOMContentLoaded", () => {
    //  ตั้งค่า stock mock
    let stock = 0; // <-- mock ก่อน (รอเชื่อม backend)
    const stockCountEl = document.getElementById("stock-count");
    const stockStatusText = document.getElementById("stock-status-text");
    const addCartBtn = document.querySelector(".add-cart-btn");
    const buyBtn = document.querySelector(".buy-btn");

    stockCountEl.textContent = stock;

    //  สินค้าหมด
    if (stock <= 0) {
        stockStatusText.textContent = "สินค้าหมด";
        stockStatusText.classList.remove("in-stock");
        stockStatusText.classList.add("out-of-stock");
        addCartBtn.disabled = true;
        addCartBtn.classList.add("out-of-stock");
        buyBtn.disabled = true;
        buyBtn.classList.add("out-of-stock");
        buyBtn.innerHTML = `<i class="fa-solid fa-circle-exclamation"></i> ขออภัย สินค้าหมด`;
    }

    //  ปุ่มเลือกไซซ์
    document.querySelectorAll('.size-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.size-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
        });
    });

    // เพิ่ม/ลดจำนวนสินค้า
    const qtyInput = document.getElementById('quantity');
    const plusBtn = document.querySelector('.plus');
    const minusBtn = document.querySelector('.minus');

    // สร้าง inline error message
    const warningMsg = document.createElement('p');
    warningMsg.style.color = 'red';
    warningMsg.style.fontSize = '14px';
    warningMsg.style.marginTop = '4px';
    warningMsg.style.display = 'none';
    qtyInput.parentElement.appendChild(warningMsg);

    // ฟังก์ชันตรวจสอบ input
    function validateInput() {
        const value = parseInt(qtyInput.value) || 0;
        let valid = true;

        if (isNaN(value) || value <= 0) {
            showError("⚠️ จำนวนสินค้าต้องมากกว่า 0");
            valid = false;
        } else if (value > stock) {
            showError(`⚠️ เกินจำนวนสินค้าในคลัง (${stock} ชิ้น)`);
            valid = false;
        } else {
            hideError();
        }

        addCartBtn.disabled = !valid;
        buyBtn.disabled = !valid;
        return valid;
    }

    function showError(msg) {
        warningMsg.textContent = msg;
        warningMsg.style.display = "block";
    }
    function hideError() {
        warningMsg.style.display = "none";
    }

    //  เพิ่มการตรวจซ้ำหลัง API
    async function simulateAddToCart() {
        if (!validateInput()) return; // ไม่ต้องเรียก API ถ้า invalid ก่อนหน้า

        try {
            // mock API เรียก
            const response = await fakeApiCall();
            if (!response.ok) throw { status: response.status };
            alert("เพิ่มสินค้าสำเร็จ ✅");
        } catch (err) {
            handleApiError(err.status);
        }
    }

    // จำลอง API response เพื่อทดสอบ
    async function fakeApiCall() {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                const randomStatus = [200, 400, 401, 404, 409, 500][Math.floor(Math.random() * 6)];
                if (randomStatus === 200) resolve({ ok: true });
                else reject({ status: randomStatus });
            }, 500);
        });
    }

    // ===== แม็ปข้อความ error ที่อ่านง่าย =====
    function handleApiError(status) {
        let message = "เกิดข้อผิดพลาดที่ไม่ทราบสาเหตุ";
        switch (status) {
            case 400:
                message = "คำขอไม่ถูกต้อง กรุณาลองใหม่อีกครั้ง";
                break;
            case 401:
                message = "กรุณาเข้าสู่ระบบก่อนทำรายการ";
                break;
            case 404:
                message = "ไม่พบข้อมูลสินค้าในระบบ";
                break;
            case 409:
                message = "จำนวนที่สั่งเกินสต๊อกที่มีอยู่";
                break;
            case 500:
                message = "เกิดข้อผิดพลาดจากเซิร์ฟเวอร์ กรุณาลองใหม่ภายหลัง";
                break;
        }
        showError(`❌ ${message}`);
    }

    //  event ปุ่ม
    plusBtn.addEventListener('click', () => {
        let current = parseInt(qtyInput.value) || 0;
        qtyInput.value = current + 1;
        validateInput();
    });
    minusBtn.addEventListener('click', () => {
        let current = parseInt(qtyInput.value) || 1;
        if (current > 1) qtyInput.value = current - 1;
        validateInput();
    });
    qtyInput.addEventListener('input', validateInput);
    addCartBtn.addEventListener('click', simulateAddToCart);

    // Dropdowns
    const toggleBtn = document.getElementById("toggle-detail");
    const detailBox = document.getElementById("detail-content");
    const icon = toggleBtn.querySelector("i");
    toggleBtn.addEventListener("click", () => {
        detailBox.classList.toggle("active");
        icon.classList.toggle("fa-chevron-down");
        icon.classList.toggle("fa-chevron-up");
    });

    const toggleImgBtn = document.getElementById("toggle-images");
    const imageGallery = document.getElementById("image-gallery");
    const iconImg = toggleImgBtn.querySelector("i");
    toggleImgBtn.addEventListener("click", () => {
        imageGallery.classList.toggle("active");
        iconImg.classList.toggle("fa-chevron-down");
        iconImg.classList.toggle("fa-chevron-up");
    });

    const toggleReview = document.getElementById("toggle-review");
    const reviewBox = document.getElementById("review-box");
    const reviewIcon = toggleReview.querySelector("i");
    toggleReview.addEventListener("click", () => {
        reviewBox.classList.toggle("active");
        reviewIcon.classList.toggle("fa-chevron-down");
        reviewIcon.classList.toggle("fa-chevron-up");
    });
});