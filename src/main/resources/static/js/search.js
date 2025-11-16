document.addEventListener('DOMContentLoaded', () => {

  const searchInput = document.getElementById('search-input');
  const suggestionsList = document.getElementById('search-suggestions');
  const searchClose = document.getElementById('search-close');

  const allSuggestions = [
    "กระเป๋า", "เสื้อยืด", "ของที่ระลึก", "เสื้อโปโล",
    "เครื่องแบบนักศึกษา", "กระเป๋าผ้า", "พวงกุญแจ"
  ];

  /* ========= Suggestion List ========= */
  if (searchInput) {
    searchInput.addEventListener("input", () => {
      const query = searchInput.value.toLowerCase().trim();
      suggestionsList.innerHTML = "";

      if (!query) {
        suggestionsList.style.display = "none";
        return;
      }

      const matched = allSuggestions.filter(s => s.toLowerCase().includes(query));

      matched.forEach(item => {
        const html = `
          <div class="suggestion-item">
            <i class="fa-solid fa-magnifying-glass"></i>
            <span>${item}</span>
          </div>`;
        suggestionsList.insertAdjacentHTML("beforeend", html);
      });

      suggestionsList.style.display = matched.length ? "block" : "none";
    });
  }

  /* ========= Click suggestion ========= */
  if (suggestionsList) {
    suggestionsList.addEventListener("click", (ev) => {
      const item = ev.target.closest(".suggestion-item");
      if (!item) return;

      const keyword = item.querySelector("span").textContent;

      // ❗ เปลี่ยนแบบใหม่ — ไม่ใช้ status=notfound แล้ว
      window.location.href = `/product?search=${encodeURIComponent(keyword)}`;
    });
  }

  /* ========= ENTER key ========= */
  if (searchInput) {
    searchInput.addEventListener("keydown", (e) => {
      if (e.key === "Enter") {
        e.preventDefault();
        const keyword = searchInput.value.trim();
        if (keyword !== "") {
          window.location.href = `/product?search=${encodeURIComponent(keyword)}`;
        }
      }
    });
  }

  /* ========= Close search bar ========= */
  if (searchClose) {
    searchClose.addEventListener("click", () => {
      searchInput.value = "";
      suggestionsList.style.display = "none";
    });
  }

  /* ========= Search Page Load ========= */
  handleSearchPage();
});



/* ============================
   Handle Search Page
============================ */
async function handleSearchPage() {
  const loadingDiv = document.getElementById("search-loading");
  const notFoundDiv = document.getElementById("search-not-found");
  const productGrid = document.getElementById("product-grid");
  const queryText = document.getElementById("search-query-text");
  const tagsContainer = document.getElementById("filter-tags-container");

  if (!productGrid || !loadingDiv) return;

  const params = new URLSearchParams(window.location.search);

  const keyword = params.get("search") || null;
  const category = params.get("category") || null;
  const minPrice = params.get("minPrice") || null;
  const maxPrice = params.get("maxPrice") || null;
  const rating = params.get("rating") || null;
  let status = params.get("status") || null;

  // ⭐ แปลง PRE_ORDER → OUT_OF_STOCK
  if (status === "PRE_ORDER") {
    status = "OUT_OF_STOCK";
  }

  // ถ้าไม่มี filter และไม่มี keyword ก็หยุด
  if (!keyword && !category && !minPrice && !maxPrice && !rating && !status) return;

  /* Show loading */
  loadingDiv.style.display = "flex";
  productGrid.style.display = "none";
  notFoundDiv.style.display = "none";
  tagsContainer.innerHTML = "";

  /* Tag Display */
  if (keyword) tagsContainer.innerHTML += `<div class="filter-tag">ค้นหา: "${keyword}"</div>`;
  if (category) tagsContainer.innerHTML += `<div class="filter-tag">หมวดหมู่: ${category}</div>`;
  if (rating) tagsContainer.innerHTML += `<div class="filter-tag">คะแนน: ${rating}⭐</div>`;
  if (minPrice && maxPrice) tagsContainer.innerHTML += `<div class="filter-tag">ราคา: ฿${minPrice} - ฿${maxPrice}</div>`;


  /* Transform status → inStock */
  const inStock =
      status === "AVAILABLE"
        ? true
        : status === "OUT_OF_STOCK"
          ? false
          : null;

  /* API body (ถูกต้อง 100%) */
  const body = {
    name: keyword,                            // ช่อง search ส่งเฉพาะ name ได้เลย
    category: category || null,
    minPrice: minPrice ? Number(minPrice) : null,
    maxPrice: maxPrice ? Number(maxPrice) : null,
    rating: rating ? Number(rating) : null,
    inStock: inStock,
    sort: null
  };

  try {
    const res = await fetch("/api/search", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body)
    });

    const products = await res.json();

    loadingDiv.style.display = "none";

    if (!products || products.length === 0 || products.message) {
      if (queryText) queryText.textContent = keyword || "";
      notFoundDiv.style.display = "flex";
      return;
    }

    renderSearchResults(products);

  } catch (err) {
    console.error("Search error:", err);
    loadingDiv.style.display = "none";
    notFoundDiv.style.display = "flex";
  }
}



/* ============================
    Render Search Results
============================ */
function renderSearchResults(products) {
  const grid = document.getElementById("product-grid");
  if (!grid) return;

  grid.innerHTML = "";
  grid.style.display = "grid";

  products.forEach(p => {
    const id = p.productId || p.product_id;

    const html = `
      <a href="/product_detail/${id}" class="product-card-link">
        <div class="product-card">
            <div class="product-image">
                <img src="/product_img/${p.main_image}">
            </div>
            <div class="product-info">
                <span class="product-name">${p.name}</span>
                <span class="product-price">฿ ${p.price}</span>
            </div>
        </div>
      </a>
    `;
    grid.insertAdjacentHTML("beforeend", html);
  });
}
