



document.addEventListener("DOMContentLoaded", async () => {

    console.log("DOM loaded!");
    const params = new URLSearchParams(window.location.search);
    const orderId = params.get("orderId");

    const API_URL = "/api/orders/" + orderId;
    console.log(orderId)
    try {
        const response = await fetch(API_URL);
        if (!response.ok) throw new Error("Fetch failed");
        const old = document.querySelector('.order-detail-card');
        if (old) old.innerHTML = '';
        else return;

        const order = await response.json();;

        
        const shopName = order.shopName ?? "ไม่ทราบชื่อร้าน";
        let sizeText = order.size ?? "";
        let colorText = order.color ?? "";
        let spaceText = "";
        if(sizeText != "") sizeText = "ขนาด (" + sizeText + ")"
        if(colorText != "") colorText = "สี (" + colorText + ")"
        if(sizeText != "" && colorText != "") spaceText = ", "

        const username = document.getElementById("usernameForJS")?.textContent.trim();
        const phone = document.getElementById("phoneForJS")?.textContent.trim();
        const card = `
            <div class="order-date">วันที่ทำรายการสั่งซื้อ : <strong>5 - 11 - 2025</strong></div>

            <!-- ร้านค้า -->
            <div class="shop-row">
                <span class="shop-icon material-symbols-outlined">storefront</span>
                <span class="shop-name">${shopName}</span>
            </div>

            <!-- สรุปรายการสินค้า + ผู้รับ -->
            <div class="summary-grid">
            <div class="thumb">
            <img src="/product_img/${order.imagePath}"/>
            </div>

            <div class="summary-content">

            <div class="detail-item">
                <span class="detail-label">ชื่อสินค้า</span>
                <span class="detail-value">${order.productName}</span>
            </div>

            <div class="detail-item">
                <span class="detail-label">รายละเอียด</span>
                <span class="detail-value">${sizeText}${spaceText}${colorText} จำนวน x${order.quantity} </span>
            </div>

            <div class="detail-item">
                <span class="detail-label">ราคา</span>
                <span class="detail-value">฿${order.totalPrice}</span>
            </div>

            <hr class="detail-divider">

            <div class="detail-item">
                <span class="detail-label">ผู้รับ</span>
                <span class="detail-value">${username}</span>
            </div>

            <div class="detail-item">
                <span class="detail-label">โทร</span>
                <span class="detail-value">${phone}</span>
            </div>

            <div class="detail-item">
                <span class="detail-label">ที่อยู่</span>
                <span class="detail-value">99/99 ซอย 11 ถนนแฟนคลับ ตำบลทดสอบ อำเภอหลงทาง จังหวัดปทุมธานี 12121</span>
            </div>

            </div>
            </div>
            <!-- ช่องทางชำระเงิน -->
            <div class="payment-box">
        
        <div class="detail-item">
            <span class="detail-label">ช่องทางชำระเงิน</span>
            <span class="detail-value">พร้อมเพย์</span>
        </div>

        <div class="detail-item">
            <span class="detail-label">สถานะการชำระเงิน</span>
            <span class="detail-value">ยังไม่ชำระเงิน (เมื่อวันที่ 5 - 11 - 2025)</span>
        </div>

        </div>

            <!-- สถานะคำสั่งซื้อ -->
            <div class="status-steps">
                <div class="step active">
                <span class="icon material-symbols-outlined">account_balance_wallet</span>
                <span class="text">รอการชำระ</span>
                </div>
                <div class="step">
                <span class="icon material-symbols-outlined">redeem</span>
                <span class="text">เตรียมการจัดส่ง</span>
                </div>
                <div class="step">
                <span class="icon material-symbols-outlined">local_shipping</span>
                <span class="text">ที่ต้องได้รับ</span>
                </div>
                <div class="step">
                <span class="icon material-symbols-outlined">task_alt</span>
                <span class="text">สำเร็จ</span>
                </div>
            </div>

            <div class="cta">
                <a href="#" class="btn btn-primary">ไปชำระเงิน <span class="material-symbols-outlined">chevron_right</span></a>
            </div>
            `;
        old.insertAdjacentHTML("beforeend", card);
    } catch (err) {
        console.error("Error:", err);
    }
});