// ============ STATUS MAPPING ============
function mapStatus(order) {
  if (order.status === "PENDING" || order.paymentStatus === "PENDING") {
    return "pending";
  }

  switch (order.sTrackingStatus) {
    case "PREPARING": return "preparing";
    case "SHIPPED":   return "delivery";
    case "DELIVERED": return "finish";
    case "CANCELLED": return "cancelled";
    default:          return null;
  }
}

function translateStatus(order) {
  const status = mapStatus(order);
  switch (status) {
    case "pending":   return "รอการชำระเงิน";
    case "preparing": return "ร้านค้ากำลังจัดเตรียมสินค้า";
    case "delivery":  return "พัสดุกำลังถูกขนส่ง";
    case "finish":    return "จัดส่งสำเร็จ";
    case "cancelled": return "คำสั่งซื้อถูกยกเลิก";
    default:          return "-";
  }
}

// ============ ปุ่มรีวิวสินค้า ============
function buildReviewButton(order) {
  if (mapStatus(order) !== "finish") return "";

  return `
    <div class="review-btn-box">
      <a href="/review?orderId=${order.orderId}&productId=${order.productId}"
         class="review-btn">
        รีวิวสินค้า
      </a>
    </div>
  `;
}

// ============ BUCKET TARGET ============
const buckets = {
  pending:   document.getElementById("bucket-payment"),
  preparing: document.getElementById("bucket-preparing"),
  delivery:  document.getElementById("bucket-delivery"),
  finish:    document.getElementById("bucket-finish"),
};

// ============ LOAD ORDERS ============
document.addEventListener("DOMContentLoaded", async () => {
  console.log("DOM loaded!");
  const API_URL = "/api/orders?sort=createdAt:desc";

  Object.values(buckets).forEach(bucket => bucket.innerHTML = "");

  try {
    const response = await fetch(API_URL);
    if (!response.ok) throw new Error("Fetch failed");
    const page = await response.json();

    const orders = page.content || [];

    // ============ ใส่ลง bucket ============
    orders.forEach(order => {
      const mapped = mapStatus(order);
      const bucket = buckets[mapped];
      if (!bucket) return;

      const shopName = order.shopName ?? "ไม่ทราบชื่อร้าน";
      let sizeText  = order.size  ? `ขนาด (${order.size})` : "";
      let colorText = order.color ? `สี (${order.color})`   : "";
      let spaceText = (sizeText && colorText) ? ", " : "";

      const card = `
        <article class="order-card order-item">
            <div class="order-card__meta">
                <span class="shop-icon material-symbols-outlined">storefront</span>
                <div class="shop-name">${shopName}</div>
            </div>
            <div class="order-card__body">
                <div class="product-thumb-box"><div class="icon"><span class="material-symbols-outlined">image</span><a href="order-detail-waiting?orderId=${order.orderId}"><img src="/product_img/${order.imagePath}" alt="รูปสินค้า" style="object-fit: cover;"/></a></div></div>
                <div class="product-info">
                <div class="product-title"><a href="order-detail-waiting?orderId=${order.orderId}">${order.productName}</a></div>
                <div class="product-meta">${sizeText}${spaceText}${colorText} x${order.quantity} <div class="price">฿${order.totalPrice}</div></div>
                <div class="order-status-line">สถานะคำสั่งซื้อ : <span class="status status--${mapStatus(order)}">${translateStatus(order)}</span></div>
                </div>
            </div>
        </article>
      `;

      bucket.insertAdjacentHTML("beforeend", card);
      bucket.hidden = true;
    });

    renderFromBucket("payment")
    // ใช้ page.number, page.totalPages ทำ pagination ได้


    const ordersTopList = document.querySelector(".ordered-items");
    const emptyState = document.querySelector('.order-status .empty-state');
    ordersTopList.innerHTML = '';
    if (orders[0] == null) {
        emptyState.classList.remove('is-hidden');
    }else{
        const order = orders[0];
    const shopName = order.shopName ?? "ไม่ทราบชื่อร้าน";
    let sizeText = order.size ?? "";
    let colorText = order.color ?? "";
    let spaceText = "";
    if(sizeText != "") sizeText = "ขนาด (" + sizeText + ")"
    if(colorText != "") colorText = "สี (" + colorText + ")"
    if(sizeText != "" && colorText != "") spaceText = ", "
      
    const card = `
        <article class="order-card order-item" data-demo="true" data-status="payment">
          <div class="order-card__meta">
            <span class="shop-icon material-symbols-outlined" aria-hidden="true">storefront</span>
            <div class="shop-name">${shopName}</div>
          </div>

          <div class="order-card__body">
            <div class="product-thumb-box" role="img" aria-label="รูปสินค้า (รอการอัปโหลด)">
              <div class="icon" aria-hidden="true">
                <span class="material-symbols-outlined">image</span>
                <a href="order-detail-waiting?orderId=${order.orderId}"><img src="/product_img/${order.imagePath}" alt="รูปสินค้า" style="object-fit: cover;" /></a>
              </div>
            </div>

            <div class="product-info">
              <div class="product-title">
                <a href="order-detail-waiting?orderId=${order.orderId}">
                  ${order.productName}
                </a>
              </div>

              <div class="product-meta">
                ${sizeText}${spaceText}${colorText} × ${order.quantity}
                <div class="price">฿${order.totalPrice}</div>
              </div>

              <div class="order-status-line">
                สถานะคำสั่งซื้อ :
                <span class="status status--${mapped}">
                  ${translateStatus(order)}
                </span>
              </div>

              ${buildReviewButton(order)}
            </div>
          </div>
        </article>
      `;

      bucket.insertAdjacentHTML("beforeend", card);
      bucket.hidden = true;
    });

    // ============ แสดง bucket แรกของหน้า ============
    renderFromBucket("payment");

  } catch (err) {
    console.error("Error:", err);
  }
});

// ============ RENDER SECTION ============
function renderFromBucket(key) {
  const ordersList = document.querySelector('.order-status .orders-list');
  const emptyState = document.querySelector('.order-status .empty-state');
  const statusSwitch = document.querySelector('.status-switch');
  if (!ordersList || !emptyState || !statusSwitch) return;

  const BUCKET_MAP = {
    payment:   document.getElementById('bucket-payment'),
    preparing: document.getElementById('bucket-preparing'),
    delivery:  document.getElementById('bucket-delivery'),
    finish:    document.getElementById('bucket-finish'),
  };

  ordersList.innerHTML = '';
  const bucket = BUCKET_MAP[key];
  if (!bucket) { emptyState.classList.remove('is-hidden'); return; }

  const items = bucket.querySelectorAll('.order-card, .order-item, article');
  if (!items.length) { emptyState.classList.remove('is-hidden'); return; }

  emptyState.classList.add('is-hidden');
  ordersList.innerHTML = bucket.innerHTML;

  ordersList.addEventListener('click', (e) => {
    const a = e.target.closest('.product-title a');
    if (a) return;
    const title = e.target.closest('.product-title');
    if (!title) return;
    const link = title.querySelector('a');
    if (link) window.location.href = link.href;
  }, { once: true });
}
