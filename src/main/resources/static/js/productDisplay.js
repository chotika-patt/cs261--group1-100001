// 🧱 mock data เผื่อ backend ยังไม่พร้อม
const mockProducts = [
  { id: 1, name: "เสื้อเชียร์ธรรมศาสตร์", price: 350, image: "img/shirt1.jpg" },
  { id: 2, name: "เสื้อเชียร์ธรรมศาสตร์ รุ่น 75", price: 300, image: "img/shirt2.jpg" },
  { id: 3, name: "กระเป๋าผ้า TU", price: 199, image: "img/bag1.jpg" },
  { id: 4, name: "เสื้อโปโล TU", price: 450, image: "img/shirt3.jpg" }
];

// 🧭 ฟังก์ชันโหลดสินค้า
async function loadProducts() {
  const grid = document.getElementById("productGrid");
  grid.innerHTML = `<p>กำลังโหลดสินค้า...</p>`;
  
  try {
    const res = await fetch("/api/products");
    if (!res.ok) throw new Error("backend not ready");
    const data = await res.json();
    renderProducts(data);
  } catch (err) {
    console.warn("ใช้ mock data แทนเพราะ backend ยังไม่ตอบ:", err.message);
    renderProducts(mockProducts);
  }
}

// 🎨 แสดงสินค้า
function renderProducts(list) {
  const grid = document.getElementById("productGrid");
  grid.innerHTML = "";

  if (!list || list.length === 0) {
    grid.innerHTML = "<p>ไม่พบสินค้า</p>";
    return;
  }

  list.forEach(p => {
    const card = document.createElement("div");
    card.classList.add("product-card");
    card.innerHTML = `
      <div class="product-image">
        <img src="${p.image}" alt="${p.name}" style="width:100%; border-radius:8px;">
      </div>
      <div class="product-info">
        <span class="product-name">${p.name}</span>
        <span class="product-price">฿ ${p.price}</span>
      </div>
    `;
    grid.appendChild(card);
  });
}

// 🔍 ระบบค้นหา
document.getElementById("searchInput").addEventListener("input", e => {
  const keyword = e.target.value.trim().toLowerCase();
  const allCards = document.querySelectorAll(".product-card");

  allCards.forEach(card => {
    const name = card.querySelector(".product-name").textContent.toLowerCase();
    card.style.display = name.includes(keyword) ? "block" : "none";
  });
});

// 🚀 โหลดสินค้าเมื่อหน้าเปิด
window.addEventListener("DOMContentLoaded", loadProducts);
