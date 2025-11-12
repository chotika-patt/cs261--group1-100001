/* =============================================== */
/* search.js (ฉบับแก้บั๊กตัวแปรหาย + หน่วงเวลา)     */
/* =============================================== */

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
    // ไม่ return return; ปล่อยให้มันทำงานส่วนอื่นต่อได้ (เช่น handlePageLoadSearch)
  }

  // ===============================================
  // ส่วนที่ 2.5: จัดการ URL ตอนโหลดหน้า
  // ===============================================
  function handlePageLoadSearch() {
    const urlParams = new URLSearchParams(window.location.search);
    const searchQuery = urlParams.get('search');
    const searchStatus = urlParams.get('status');
    
    // (!!!) เพิ่มตัวแปรที่เคยหายไป กลับมาตรงนี้ครับ (!!!)
    const category = urlParams.get('category');
    const minPrice = urlParams.get('minPrice');
    const maxPrice = urlParams.get('maxPrice');
    const status = urlParams.get('status');

    // (เช็คว่าเราอยู่หน้า product.html โดยหา #search-loading)
    const loadingDiv = document.getElementById('search-loading');

    // --- ส่วนสร้าง Tags (โค้ดเดิม แต่ตอนนี้มีตัวแปรให้ใช้แล้ว) ---
    const tagsContainer = document.getElementById('filter-tags-container');
    if (tagsContainer) {
        tagsContainer.innerHTML = ''; // ล้างของเก่าทิ้งก่อน

        // Mapping (แปลกลับเป็นภาษาไทย)
        const categoryReverseMap = {
          'SHIRT': 'เสื้อ',
          'BAG': 'กระเป๋า',
          'STUDENT_UNIFORM': 'เครื่องแบบนักศึกษา',
          'GENERAL_PURPOSE': 'ของใช้ทั่วไป',
          'MEMORABILIA': 'ของที่ระลึก'
        };
        const statusReverseMap = {
          'READY': 'พร้อมจัดส่ง',
          'PRE_ORDER': 'PRE-ORDER'
        };

        // ฟังก์ชันช่วยสร้าง Tag
        const createTag = (text) => {
            const tag = document.createElement('div');
            tag.className = 'filter-tag';
            tag.textContent = text;
            tagsContainer.appendChild(tag);
        };

        // เช็ค Params แล้วสร้าง Tag
        if (searchQuery) {
            createTag(`ค้นหา: "${searchQuery}"`);
        }
        if (category && categoryReverseMap[category]) {
            createTag(`หมวดหมู่: ${categoryReverseMap[category]}`);
        }
        if (status && statusReverseMap[status] && status !== 'notfound') {
            createTag(`สถานะ: ${statusReverseMap[status]}`);
        }
        
        if (minPrice && maxPrice) {
            createTag(`ราคา: ฿${minPrice} - ฿${maxPrice}`);
        } else if (minPrice) {
            createTag(`ราคา: ฿${minPrice} ขึ้นไป`);
        } else if (maxPrice) {
            createTag(`ราคา: ไม่เกิน ฿${maxPrice}`);
        }
    }

    // --- Logic การแสดง Loading ---
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
      }, 1200); // หน่วงเวลา 1.2 วิ

    } else if (loadingDiv) {
      // ถ้าเข้าหน้าปกติ ให้ซ่อน Loading
      loadingDiv.style.display = 'none';
      const notFoundDiv = document.getElementById('search-not-found');
      if (notFoundDiv) notFoundDiv.style.display = 'none';
    }
  }
  
  // เรียกใช้งานทันที
  handlePageLoadSearch(); 

  // ===============================================
  // ส่วนที่ 3: โค้ด Search Suggestions 
  // ===============================================
  
  if (searchInput) {
      // 3. เมื่อ "พิมพ์" ในช่องค้นหา
      searchInput.addEventListener('input', () => {
        const query = searchInput.value.toLowerCase();
        if (suggestionsList) suggestionsList.innerHTML = ''; 

        if (!query || !suggestionsList) {
          if (suggestionsList) suggestionsList.style.display = 'none';
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

      // 7. โค้ด "กด Enter" 
      searchInput.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
          e.preventDefault();
          const keyword = searchInput.value.trim();
          if (suggestionsList) suggestionsList.style.display = 'none'; 

          if (document.getElementById('search-loading')) {
            // หน้า Product: ค้นหาเลย
            if (keyword) searchProducts(keyword); 
          } else {
            // หน้าอื่น: เด้งไปหน้า Product
            if (keyword) {
              const url = `/product?search=${encodeURIComponent(keyword)}&status=notfound`;
              window.location.href = url;
            }
          }
        }
      });
  }

  // 4. ซ่อนกล่อง เมื่อคลิกที่อื่น
  document.addEventListener('click', (ev) => {
    if (searchInput && suggestionsList && !searchInput.contains(ev.target) && !suggestionsList.contains(ev.target)) {
      suggestionsList.style.display = 'none';
    }
  });
  
  // 5. เมื่อคลิกที่ "แถว" แนะนำ
  if (suggestionsList) {
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
  }

  // 6. โค้ด "ปุ่มกากบาท"
  if (searchClose && searchInput) {
      searchClose.addEventListener("click", () => {
        searchInput.value = "";
        if (suggestionsList) suggestionsList.style.display = 'none';
      });
  }

  // ===============================================
  // ส่วนที่ 4: ฟังก์ชัน Fetch API (ค้นหาจริง)
  // ===============================================
  async function searchProducts(keyword) {
    
    const loadingDiv = document.getElementById('search-loading');
    const notFoundDiv = document.getElementById('search-not-found');
    const productGrid = document.querySelector('.product-grid');
    const productHeader = document.querySelector('.product-header');
    const queryText = document.getElementById('search-query-text');
    
    if (!loadingDiv || !notFoundDiv || !productGrid || !productHeader) {
        console.error("Cannot find product display elements.");
        return;
    }

    // 1. โชว์ Loading
    loadingDiv.style.display = 'flex';
    notFoundDiv.style.display = 'none';
    productGrid.style.display = 'none';
    productHeader.style.display = 'none'; 

    try {
      // จำลองความหน่วง (เพื่อให้เห็น loading นิดนึง)
      const minDelay = new Promise(resolve => setTimeout(resolve, 800)); 
      
      const responsePromise = fetch("/api/search", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          name: keyword, category: null, status: null, minPrice: null, maxPrice: null
        })
      });

      // รอทั้ง API และ Delay ให้เสร็จ
      const [response, _] = await Promise.all([responsePromise, minDelay]);
      const data = await response.json();

      loadingDiv.style.display = 'none'; // ซ่อน Loading

      if (data.message) {
        // --- ไม่พบสินค้า ---
        if (queryText) queryText.textContent = keyword;
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
      if (queryText) queryText.textContent = 'Error';
      if (notFoundDiv) {
          notFoundDiv.querySelector('h2').textContent = 'เกิดข้อผิดพลาด';
          notFoundDiv.style.display = 'flex';
      }
    }
  }

  function renderResults(products) {
    const productGrid = document.querySelector('.product-grid');
    if (!productGrid) return;

    productGrid.innerHTML = ""; 

    products.forEach(product => {
      const cardLink = document.createElement('a');
      // เช็ค field id ดีๆ (ใช้ productId ตามหน้าเว็บ หรือ product_id ตาม API)
      const pId = product.productId || product.product_id; 
      cardLink.href = `/product_detail/${pId}`; 
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