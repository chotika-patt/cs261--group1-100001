document.addEventListener("DOMContentLoaded", () => {

    const metaStockEl = document.getElementById("meta-stock");
    const metaProductEl = document.getElementById("meta-product-id");

    const stock = Number(metaStockEl?.content) || 0;
    const productId = Number(metaProductEl?.content) || 0;

    const stockCountEl = document.getElementById("stock-count");
    const stockStatusText = document.getElementById("stock-status-text");
    const addCartBtn = document.querySelector(".add-cart-btn");
    const buyBtn = document.querySelector(".buy-btn");
    const qtyInput = document.getElementById("quantity");
    const plusBtn = document.querySelector(".plus");
    const minusBtn = document.querySelector(".minus");

    const sizeInput = document.getElementById("selected-size");

    // ====== STOCK UI ======
    stockCountEl.textContent = stock;

    function updateStockUI() {
        if (stock > 0) {
            stockStatusText.textContent = `พร้อมจัดส่ง (${stock} ชิ้นในคลัง)`;
            addCartBtn.disabled = false;
        } else {
            stockStatusText.textContent = "สินค้าหมด";
            addCartBtn.disabled = true;
        }
    }
    updateStockUI();


    // ====== QUANTITY VALIDATION ======
    const warningMsg = document.createElement("p");
    warningMsg.style.color = "red";
    warningMsg.style.display = "none";
    qtyInput?.parentElement.appendChild(warningMsg);

    function showError(msg) {
        warningMsg.textContent = msg;
        warningMsg.style.display = "block";
    }
    function hideError() {
        warningMsg.style.display = "none";
    }

    function validateQty() {
        const qty = Number(qtyInput.value);
        if (qty <= 0) {
            showError("⚠️ จำนวนต้องมากกว่า 0");
            return false;
        }
        if (qty > stock) {
            showError(`⚠️ จำนวนคงเหลือไม่พอ (${stock} ชิ้น)`);
            return false;
        }
        hideError();
        return true;
    }

    plusBtn?.addEventListener("click", () => {
        qtyInput.value = Number(qtyInput.value) + 1;
        validateQty();
    });

    minusBtn?.addEventListener("click", () => {
        const current = Number(qtyInput.value);
        if (current > 1) qtyInput.value = current - 1;
        validateQty();
    });


    // ====== SIZE SELECT ======
    document.querySelectorAll(".size-btn").forEach(btn => {
        btn.addEventListener("click", () => {

            // remove active
            document.querySelectorAll(".size-btn").forEach(b => b.classList.remove("active"));

            // add active
            btn.classList.add("active");

            // update hidden input
            sizeInput.value = btn.dataset.size;
        });
    });


    // ====== ADD TO CART API ======
    async function callAddToCart() {
        if (!validateQty()) return;

        const size = sizeInput?.value || null;

        try {
            const response = await fetch("/api/cart/add", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    productId: productId,
                    quantity: Number(qtyInput.value),
                    size: size
                })
            });

            if (!response.ok) {
                alert("เกิดข้อผิดพลาด ไม่สามารถเพิ่มสินค้าได้");
                return;
            }

            alert("เพิ่มสินค้าเข้าตะกร้าสำเร็จ!");
        } catch (err) {
            console.error(err);
            alert("❌ ไม่สามารถเชื่อมต่อเซิร์ฟเวอร์");
        }
    }

    addCartBtn.addEventListener("click", callAddToCart);
});
