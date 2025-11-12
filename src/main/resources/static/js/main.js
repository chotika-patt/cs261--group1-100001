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

  const redirectToSearch = () => {
    const keyword = searchInput.value.trim();
    const searchUrl = `/product_no_login?q=${encodeURIComponent(keyword)}`;
    window.location.href = searchUrl;
  };-
  searchInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      e.preventDefault(); 
      redirectToSearch(); 
    }
  });

}); // 👈 ปิด DOMContentLoaded ตัวแม่