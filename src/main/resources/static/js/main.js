// 1. รอให้ HTML โหลดเสร็จก่อน (สำคัญมาก)
// เราจะใช้ DOMContentLoaded แค่ "ครั้งเดียว" หุ้มทุกอย่างไว้ครับ
document.addEventListener('DOMContentLoaded', () => {

  // =================== USER DROPDOWN ===================
  // (อันนี้ของคุณดีอยู่แล้ว)
  (() => {
    const userBtn = document.getElementById("user-btn");
    const userDropdown = document.getElementById("user-dropdown");
    const closeBtn = document.getElementById("close-dropdown");

    if (userBtn && userDropdown && closeBtn) {
      userBtn.addEventListener("click", (e) => {
        e.stopPropagation();
        userDropdown.classList.toggle("active");
      });

      closeBtn.addEventListener("click", () => {
        userDropdown.classList.remove("active");
      });

      window.addEventListener("click", (e) => {
        if (!userDropdown.contains(e.target) && !userBtn.contains(e.target)) {
          userDropdown.classList.remove("active");
        }
      });
    }
  })();

  // =================== LOGOUT MODAL ===================
  // (อันนี้ของคุณดีอยู่แล้ว)
  (() => {
    const logoutBtn = document.getElementById("logout-btn");
    const logoutModal = document.getElementById("logout-modal");
    const cancelLogout = document.getElementById("cancel-logout");
    const confirmLogout = document.getElementById("confirm-logout");
    const closeModal = document.getElementById("close-modal");
    const logoutSuccess = document.getElementById("logout-success");

    if (logoutBtn && logoutModal && cancelLogout && confirmLogout && closeModal) {
      logoutBtn.addEventListener("click", (e) => {
        e.preventDefault();
        logoutModal.classList.add("active");
      });

      cancelLogout.addEventListener("click", () => {
        logoutModal.classList.remove("active");
      });

      closeModal.addEventListener("click", () => {
        logoutModal.classList.remove("active");
      });

      confirmLogout.addEventListener("click", () => {
        logoutModal.classList.remove("active");

        // ส่ง POST request ไป /logout
        fetch('/api/logout', { method: 'POST' })
          .then(response => {
            if (response.ok) {
              logoutSuccess.classList.add("active");
              setTimeout(() => {
                logoutSuccess.classList.remove("active");
                window.location.href = "/"; // redirect หลัง logout สำเร็จ
              }, 2500);
            } else {
              alert("เกิดข้อผิดพลาดในการออกจากระบบ");
            }
          })
          .catch(err => {
            console.error(err);
            alert("เกิดข้อผิดพลาดในการออกจากระบบ");
          });
      });
    }
  })();

  // =================== SEARCH BAR ===================
  // (อันนี้ของคุณดีอยู่แล้ว)
  (() => {
    const searchIcon = document.getElementById('search-icon');
    const searchClose = document.getElementById('search-close');
    const navBottom = document.querySelector('.nav-bottom');

    if (searchIcon && searchClose && navBottom) {
      searchIcon.addEventListener('click', (event) => {
        event.preventDefault();
        navBottom.classList.add('search-active');
      });

      searchClose.addEventListener('click', () => {
        navBottom.classList.remove('search-active');
      });
    }
  })();

  // =================== DROPDOWN หมวดหมู่สินค้า ===================
  (() => {
    const categoryDropdown = document.querySelector('.dropdown-category');
    if (!categoryDropdown) return;

    const toggle = categoryDropdown.querySelector('.dropdown-toggle');
    const overlay = categoryDropdown.querySelector('.overlay');

    if (toggle && overlay) {
        toggle.addEventListener('click', function(e) {
            e.preventDefault();
            categoryDropdown.classList.toggle('active');
        });
        overlay.addEventListener('click', function() {
            categoryDropdown.classList.remove('active');
        });
    }
  })();

  // =================== DROPDOWN แถบแดง (Category Bar) ===================
  // (อันนี้ของคุณดีอยู่แล้ว แต่ผมย้ายมาไว้ใน DOMContentLoaded)
  (() => {
    const dropdownToggle = document.querySelector('.category-toggle');
    const dropdownContent = document.querySelector('.category-dropdown');

    if (dropdownToggle && dropdownContent) {
      dropdownToggle.addEventListener('click', function(e) {
        e.preventDefault();
        dropdownContent.classList.toggle('show');
      });
    }
  })();


  // =================== FILTER OVERLAY ===================
  // (ผมเลือกรุ่นล่างสุดของคุณมา 1 อัน และลบอันที่อยู่ใน <script> ทิ้ง)
  (() => {
    const filterIcons = document.querySelectorAll('#filter-icon');
    const filterOverlay = document.getElementById('filter-overlay');

    if (filterIcons.length > 0 && filterOverlay) {
        const openFilter = () => {
          filterOverlay.classList.add('show');
        };
        const closeFilter = () => {
          filterOverlay.classList.remove('show');
        };

        filterIcons.forEach(icon => {
          icon.addEventListener('click', (ev) => {
            ev.preventDefault();
            openFilter();
          });
        });

        filterOverlay.addEventListener('click', (ev) => {
          if (ev.target === filterOverlay) {
            closeFilter();
          }
        });
    }
  })();

  const searchInput = document.querySelector('.search-input-container input');

const redirectToSearch = () => {
  const keyword = searchInput?.value?.trim();
  if (keyword) {
    const searchUrl = `/product_no_login?q=${encodeURIComponent(keyword)}`;
    window.location.href = searchUrl;
  }
};

searchInput?.addEventListener("keydown", (e) => {
  if (e.key === "Enter") redirectToSearch();
});

}); 


  /* ======= JS ตัวช่วยปุ่มฟิลเตอร์ ======= */
(function ensureButtonType(){
  document.querySelectorAll('.cat-btn, .status-btn, .rating-btn').forEach(b=>{
    if (b.tagName === 'BUTTON' && !b.getAttribute('type')) b.setAttribute('type','button');
  });
})();

document.addEventListener('click', (e) => {
  const overlay = document.getElementById('filter-overlay');
  if (!overlay || !overlay.contains(e.target)) return;
  const group = e.target.closest('.category-buttons, .status-buttons, .rating-buttons');
  if (!group) return;

  const btn = e.target.closest('.cat-btn, .status-btn, .rating-btn');
  if (!btn || !group.contains(btn)) return;
  e.preventDefault();
  group.querySelectorAll('.cat-btn, .status-btn, .rating-btn').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
});


  /* ======= JS ตัวช่วยปุ่มฟิลเตอร์ ======= */
(function setupCategorySingleSelectWithToggle() {
  const container = document.querySelector('.category-buttons');
  if (!container) return;
  container.querySelectorAll('.cat-btn').forEach(btn => {
    if (btn.tagName === 'BUTTON' && !btn.getAttribute('type')) {
      btn.setAttribute('type', 'button');
    }
  });

  container.addEventListener('click', (e) => {
    const btn = e.target.closest('.cat-btn');
    if (!btn || !container.contains(btn)) return;
    e.preventDefault();

    const isActive = btn.classList.contains('active');
    container.querySelectorAll('.cat-btn').forEach(b => b.classList.remove('active'));
    if (!isActive) {
      btn.classList.add('active');
    }
  });
})();


(() => {
  const PRODUCT_PATH = '/product'; 

  /* ======= JS ตัวช่วยปุ่มฟิลเตอร์ ======= */
  const $ = (sel, root = document) => root.querySelector(sel);
  const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));
  const getText = (el) => (el?.textContent || '').trim();

  function getNumberOrNull(el) {
    if (!el) return null;
    const v = (el.value || '').trim();
    if (v === '') return null;
    const n = Number(v);
    return Number.isFinite(n) ? n : null;
  }

  function buildQuery(params) {
    const sp = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => {
      if (v === null || v === undefined) return;
      if (typeof v === 'string' && v.trim() === '') return;
      sp.set(k, String(v));
    });
    return sp.toString();
  }

  /** ================== ดึงค่าจากฟิลเตอร์ ================== **/
  function collectFilterValues() {
    const overlay = document.getElementById('filter-overlay');
    // หมวดหมู่ (เลือกได้หรือยกเลิกได้)
    const selectedCatBtn = $('.category-buttons .cat-btn.active', overlay);
    const category = getText(selectedCatBtn);
    // ราคา
    const [minInput, maxInput] = $$('.price-range .price-input', overlay);
    let priceMin = getNumberOrNull(minInput);
    let priceMax = getNumberOrNull(maxInput);
    if (priceMin != null && priceMax != null && priceMin > priceMax) {
      // สลับถ้าใส่กลับด้าน
      const t = priceMin; priceMin = priceMax; priceMax = t;
    }

    // สถานะ 
    const statusBtn = $('.status-buttons .status-btn.active', overlay);
    const status = getText(statusBtn);
    // คะแนน 
    const ratingBtn = $('.rating-buttons .rating-btn.active', overlay);
    const ratingText = getText(ratingBtn);
    const rating = ratingText ? (ratingText.match(/\d+/)?.[0] || '') : '';

    return { category, priceMin, priceMax, status, rating };
  }

  /** ================== ทำงานตอนกด "ตกลง" ================== **/
  const confirmBtn = document.querySelector('.filter-confirm-btn');
  if (confirmBtn) {
    confirmBtn.addEventListener('click', (e) => {
      e.preventDefault();

      const { category, priceMin, priceMax, status, rating } = collectFilterValues();

      // ให้ผล "เหมือน searchbar": ใช้ q เป็น keyword หลัก
      // - ถ้าเลือกหมวดหมู่ ให้ q = ชื่อหมวดหมู่
      // - ถ้าอยากรวม status/rating ใน q ด้วยก็ทำได้ แต่ตอนนี้เราแนบเป็นพารามิเตอร์แยกเพื่อกรองในหน้าปลายทาง
      const q = category || '';

      const query = buildQuery({
        q,                         
        priceMin,               
        priceMax,
        status,                    
        rating                    
      });

      const url = query ? `${PRODUCT_PATH}?${query}` : PRODUCT_PATH;
      const overlay = document.getElementById('filter-overlay');
      overlay?.classList.remove('show');
      window.location.href = url;
    });
  }
  /** ================== กันปุ่ม submit ฟอร์มโดยไม่ตั้งใจ ================== **/
  document.querySelectorAll('.cat-btn, .status-btn, .rating-btn, .filter-confirm-btn').forEach(b => {
    if (b.tagName === 'BUTTON' && !b.getAttribute('type')) b.setAttribute('type','button');
  });
})();


document.addEventListener("DOMContentLoaded", function() {
    const searchIcon = document.getElementById('search-icon');
    const filterIcon = document.getElementById('filter-icon'); 
    const searchCloseBtn = document.getElementById('search-close'); 
    if (searchIcon) {
        searchIcon.addEventListener('click', function(e) {
            if (filterIcon) {
                filterIcon.style.display = 'inline-block'; 
            }
        });
    }
    if (searchCloseBtn) {
        searchCloseBtn.addEventListener('click', function() {
            if (filterIcon) {
                filterIcon.style.display = 'none';
            }
        });
    }
});




