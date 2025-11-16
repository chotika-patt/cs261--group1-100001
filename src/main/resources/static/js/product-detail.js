document.addEventListener("DOMContentLoaded", () => {

    const metaStockEl = document.getElementById("meta-stock");
    const metaProductEl = document.getElementById("meta-product-id");

    const stock = Number(metaStockEl?.content) || 0;
    const productId = Number(metaProductEl?.content) || 0;

    console.log("Product ID:", productId);
    console.log("Stock:", stock);

    const stockCountEl = document.getElementById("stock-count");
    const stockStatusText = document.getElementById("stock-status-text");
    const addCartBtn = document.querySelector(".add-cart-btn");
    const buyBtn = document.querySelector(".buy-btn");
    const qtyInput = document.getElementById("quantity");
    const plusBtn = document.querySelector(".plus");
    const minusBtn = document.querySelector(".minus");

    if (!addCartBtn) {
        console.error("Add to cart button not found!");
        return;
    }

    stockCountEl.textContent = stock;

    // Update stock UI
    function updateStockUI() {
        if (stock > 0) {
            stockStatusText.textContent = `พร้อมจัดส่ง (${stock} ชิ้นในคลัง)`;
            stockStatusText.classList.add("in-stock");
            stockStatusText.classList.remove("out-of-stock");
            enableButtons();
        } else {
            stockStatusText.textContent = "สินค้าหมด";
            stockStatusText.classList.add("out-of-stock");
            stockStatusText.classList.remove("in-stock");
            disableButtons();
        }
    }

    function disableButtons() {
        addCartBtn.disabled = true;
        if (buyBtn) buyBtn.disabled = true;
        addCartBtn.classList.add("out-of-stock");
        if (buyBtn) buyBtn.classList.add("out-of-stock");
    }

    function enableButtons() {
        addCartBtn.disabled = false;
        if (buyBtn) buyBtn.disabled = false;
        addCartBtn.classList.remove("out-of-stock");
        if (buyBtn) buyBtn.classList.remove("out-of-stock");
    }

    updateStockUI();

    // Error message
    const warningMsg = document.createElement("p");
    warningMsg.style.color = "red";
    warningMsg.style.fontSize = "14px";
    warningMsg.style.marginTop = "4px";
    warningMsg.style.display = "none";
    qtyInput?.parentElement.appendChild(warningMsg);

    function validateQty() {
        const qty = Number(qtyInput?.value) || 0;
        let valid = true;

        if (qty <= 0) {
            showError("⚠️ จำนวนต้องมากกว่า 0");
            valid = false;
        } else if (qty > stock) {
            showError(`⚠️ จำนวนคงเหลือไม่พอ (${stock} ชิ้น)`);
            valid = false;
        } else {
            hideError();
        }

        addCartBtn.disabled = !valid;
        if (buyBtn) buyBtn.disabled = !valid;
        return valid;
    }

    function showError(msg) {
        warningMsg.textContent = msg;
        warningMsg.style.display = "block";
    }

    function hideError() {
        warningMsg.style.display = "none";
    }

    // Add to cart API
    async function callAddToCart() {
        if (!validateQty()) return;

        try {
            console.log("Calling /api/cart/add", { productId, quantity: qtyInput.value });
            const response = await fetch("/api/cart/add", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    productId: productId,
                    quantity: Number(qtyInput.value)
                })
            });

            if (!response.ok) {
                let errStatus = 500;
                try {
                    const errData = await response.json();
                    errStatus = errData.status || 500;
                } catch {}
                handleApiError(errStatus);
                return;
            }

            alert("เพิ่มสินค้าเข้าตะกร้าสำเร็จ! 🛒");

        } catch (error) {
            console.error(error);
            showError("❌ ไม่สามารถติดต่อเซิร์ฟเวอร์ได้");
        }
    }

    // Buy now
    async function callBuyNow() {
        if (!validateQty()) return;

        try {
            console.log("Calling /api/cart/add", { productId, quantity: qtyInput.value });
            const response = await fetch("/api/orders/add", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    productId: productId,
                    quantity: Number(qtyInput.value)
                })
            });

            if (!response.ok) {
                let errStatus = 500;
                try {
                    const errData = await response.json();
                    errStatus = errData.status || 500;
                } catch {}
                handleApiError(errStatus);
                return;
            }

            alert("เพิ่มสินค้าเข้าตะกร้าสำเร็จ! 🛒");

        } catch (error) {
            console.error(error);
            showError("❌ ไม่สามารถติดต่อเซิร์ฟเวอร์ได้");
        }
    }

    function handleApiError(status) {
        const msg = {
            400: "คำขอไม่ถูกต้อง",
            401: "กรุณาเข้าสู่ระบบก่อน",
            404: "ไม่พบสินค้า",
            409: "จำนวนไม่พอในสต็อก",
            500: "เซิร์ฟเวอร์ผิดพลาด"
        }[status] || "เกิดข้อผิดพลาด";

        showError(`❌ ${msg}`);
    }

    plusBtn?.addEventListener("click", () => {
        qtyInput.value = (Number(qtyInput.value) || 0) + 1;
        validateQty();
    });

    minusBtn?.addEventListener("click", () => {
        const current = Number(qtyInput.value) || 1;
        if (current > 1) qtyInput.value = current - 1;
        validateQty();
    });

    qtyInput?.addEventListener("input", validateQty);

    addCartBtn.addEventListener("click", callAddToCart);
    if (buyBtn) buyBtn.addEventListener("click", callBuyNow);

    // Size buttons
    document.querySelectorAll('.size-btn')?.forEach(btn => {
        btn.addEventListener('click', () => {
            document.querySelectorAll('.size-btn').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
        });
    });
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
