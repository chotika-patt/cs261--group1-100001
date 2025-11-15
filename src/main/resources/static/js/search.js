document.addEventListener('DOMContentLoaded', () => {

  // 1. (ตัวอย่าง) รายการคำค้นหาทั้งหมด
  const allSuggestions = [
    'กระเป๋าผ้า Thammasat University',
    'กระเป๋าผ้าแคนวาส',
    'กระเป๋าผ้า TU LAW',
    'เสื้อยืด',
    'เสื้อ POLO',
    'เสื้อช็อป วิศวะ',
    'เครื่องแบบนักศึกษา'
  ];

  // 2. เลือก Element ที่เราจะใช้
  const searchInput = document.getElementById('search-input');
  const suggestionsList = document.getElementById('search-suggestions');
  const searchClose = document.getElementById('search-close');
  if (!searchInput || !suggestionsList || !searchClose) {
    console.warn('Search elements (input/suggestions/close) not found.');
    return; 
  }
  // ===============================================
  function handlePageLoadSearch() {
    const urlParams = new URLSearchParams(window.location.search);
    const searchQuery = urlParams.get('search');
    const searchStatus = urlParams.get('status');

    // (เช็คว่าเราอยู่หน้า product.html โดยหา #search-loading)
    const loadingDiv = document.getElementById('search-loading');

    const tagsContainer = document.getElementById('filter-tags-container');
        if (tagsContainer) {
            tagsContainer.innerHTML = ''; // ล้างของเก่าทิ้งก่อน

            // --- 2. สร้าง Mapping (แปลกลับเป็นภาษาไทย) ---
            const categoryReverseMap = {
              'SHIRT': 'เสื้อ',
              'BAG': 'กระเป๋า',
              'STUDENT_UNIFORM': 'เครื่องแบบนักศึกษา',
              'GENERAL_PURPOSE': 'ของใช้ทั่วไป',
              'MEMORABILIA': 'ของที่ระลึก'
            };
            const statusReverseMap = {
              'READY': 'พร้อมจัดส่ง', // (เราต้องเดา Enum/String ที่ Backend ใช้)
              'PRE_ORDER': 'PRE-ORDER'
            };

            // --- 3. ฟังก์ชันช่วยสร้าง Tag ---
            const createTag = (text) => {
                const tag = document.createElement('div');
                tag.className = 'filter-tag';
                tag.textContent = text;
                tagsContainer.appendChild(tag);
            };

            // --- 4. เช็ค Params แล้วสร้าง Tag ---
            if (searchQuery) {
                createTag(`ค้นหา: "${searchQuery}"`);
            }
            if (category && categoryReverseMap[category]) {
                createTag(`หมวดหมู่: ${categoryReverseMap[category]}`);
            }
            // (เช็คว่า status ไม่ใช่ 'notfound' ที่มาจาก search bar)
            if (status && statusReverseMap[status] && status !== 'notfound') {
                createTag(`สถานะ: ${statusReverseMap[status]}`);
            }
            
            // (จัดการเรื่องราคา)
            if (minPrice && maxPrice) {
                createTag(`ราคา: ฿${minPrice} - ฿${maxPrice}`);
            } else if (minPrice) {
                createTag(`ราคา: ฿${minPrice} ขึ้นไป`);
            } else if (maxPrice) {
                createTag(`ราคา: ไม่เกิน ฿${maxPrice}`);
            }
        }
    if (loadingDiv && searchStatus === 'notfound' && searchQuery) {
      const notFoundDiv = document.getElementById('search-not-found');
      const productGrid = document.querySelector('.product-grid');
      const productHeader = document.querySelector('.product-header');
      const queryText = document.getElementById('search-query-text');

      if (productGrid) productGrid.style.display = 'none';
      if (productHeader) productHeader.style.display = 'none';
      if (notFoundDiv) notFoundDiv.style.display = 'none'; 
      loadingDiv.style.display = 'flex'; 

      setTimeout(() => {
      
        loadingDiv.style.display = 'none';
        if (queryText) queryText.textContent = searchQuery; 
        if (notFoundDiv) notFoundDiv.style.display = 'flex';
      }, 1500);

    } else if (loadingDiv) {
      loadingDiv.style.display = 'none';
      const notFoundDiv = document.getElementById('search-not-found');
      if (notFoundDiv) notFoundDiv.style.display = 'none';
    }
  }
  
  handlePageLoadSearch(); 

  // ===============================================
  // ส่วนที่ 3: โค้ด Search Suggestions 
  // ===============================================
  
  // 3. เมื่อ "พิมพ์" ในช่องค้นหา
  searchInput.addEventListener('input', () => {
    const query = searchInput.value.toLowerCase();
    suggestionsList.innerHTML = ''; 

    if (!query) {
      suggestionsList.style.display = 'none';
      return;
    }

    const filteredSuggestions = allSuggestions.filter(item => 
      item.toLowerCase().includes(query)
    );

    if (filteredSuggestions.length > 0) {
      filteredSuggestions.forEach(item => {
        const itemHtml = `
          <div class="suggestion-item">
            <i class="fa-solid fa-magnifying-glass"></i>
            <span>${item}</span>
            <i class="fa-solid fa-arrow-up-right-from-square history-icon"></i>
          </div>
        `;
        suggestionsList.insertAdjacentHTML('beforeend', itemHtml);
      });
      suggestionsList.style.display = 'block';
    } else {
      suggestionsList.style.display = 'none';
    }
  });

  // 4. ซ่อนกล่อง เมื่อคลิกที่อื่น
  document.addEventListener('click', (ev) => {
    if (!searchInput.contains(ev.target) && !suggestionsList.contains(ev.target)) {
      suggestionsList.style.display = 'none';
    }
  });
  
 // 5. เมื่อคลิกที่ "แถว" แนะนำ (ให้มันค้นหาเลย *ไม่ต้องเติมคำ*)
suggestionsList.addEventListener('click', (ev) => {
  const suggestionItem = ev.target.closest('.suggestion-item');
  if (!suggestionItem) return; 
  const keyword = suggestionItem.querySelector('span').textContent;
  suggestionsList.style.display = 'none'; 
  if (document.getElementById('search-loading')) {
    searchProducts(keyword); 
  } else {
    const url = `/product?search=${encodeURIComponent(keyword)}&status=notfound`;
    window.location.href = url;
  }
});

  // ===============================================
  // ส่วนที่ 4: โค้ด Search Logic 
  // ===============================================
  
  // 6. โค้ด "ปุ่มกากบาทาง Input
  searchClose.addEventListener("click", () => {
    searchInput.value = "";
    suggestionsList.style.display = 'none';
  });

  // 7. โค้ด "กด Enter" 
  searchInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      e.preventDefault();
      const keyword = searchInput.value.trim();
      suggestionsList.style.display = 'none'; 

      if (document.getElementById('search-loading')) {
        if (keyword) {
            searchProducts(keyword); 
        }
      } else {
        if (keyword) {
          const url = `/product?search=${encodeURIComponent(keyword)}&status=notfound`;
          window.location.href = url;
        }
      }
    }
  });

  // 8. ฟังก์ชัน Fetch API (ค้นหาจริงในหน้า product)
  async function searchProducts(keyword) {
    
    const loadingDiv = document.getElementById('search-loading');
    const notFoundDiv = document.getElementById('search-not-found');
    const productGrid = document.querySelector('.product-grid');
    const productHeader = document.querySelector('.product-header');
    const queryText = document.getElementById('search-query-text');
    
    if (!loadingDiv || !notFoundDiv || !productGrid || !productHeader) {
        console.error("Cannot find product display elements. Search aborted.");
        return;
    }

    loadingDiv.style.display = 'flex';
    notFoundDiv.style.display = 'none';
    productGrid.style.display = 'none';
    productHeader.style.display = 'none'; 

    try {
      const response = await fetch("/api/search", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          name: keyword, category: null, status: null, minPrice: null, maxPrice: null
        })
      });

      const data = await response.json();
      loadingDiv.style.display = 'none';

      if (data.message) {
        // --- ไม่พบสินค้า
        queryText.textContent = keyword;
        notFoundDiv.style.display = 'flex';
      } else {
        // --- พบสินค้า ---
        productHeader.style.display = 'flex'; 
        productGrid.style.display = 'grid'; 
        renderResults(data); 
      }
    } catch (error) {
      console.error("Error:", error);
      loadingDiv.style.display = 'none';
      queryText.textContent = 'Error';
      notFoundDiv.querySelector('h2').textContent = 'เกิดข้อผิดพลาดในการค้นหา';
      notFoundDiv.style.display = 'flex';
    }
  }

  function renderResults(products) {
    const productGrid = document.querySelector('.product-grid');
    if (!productGrid) return;

    productGrid.innerHTML = ""; 

    products.forEach(product => {
      const cardLink = document.createElement('a');
      cardLink.href = `/product_detail/${product.product_id}`; 
      cardLink.className = 'product-card-link';

      const mainImage = product.main_image 
         ? `<img src="/product_img/${product.main_image}" alt="Product Image" style="width:200px;height:auto;">`
         : ''; 

      cardLink.innerHTML = `
        <div class="product-card">
          <div class="product-image">
            ${mainImage}
          </div>
          <div class="product-info">
            <span class="product-name">${product.name}</span>
            <span class="product-price">฿ ${product.price}</span>
          </div> 
        </div>
      `;
      productGrid.appendChild(cardLink);
    });
  }

}); 