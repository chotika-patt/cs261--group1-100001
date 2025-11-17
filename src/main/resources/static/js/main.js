// ======================== MAIN SCRIPT ========================
// รวมทุกอย่างให้สะอาด ไม่มีซ้ำ ไม่มี conflict

document.addEventListener("DOMContentLoaded", () => {

  // ============================================================
  // 1) USER DROPDOWN
  // ============================================================
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


  // ============================================================
  // 2) LOGOUT MODAL
  // ============================================================
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

        fetch('/api/logout', { method: 'POST' })
          .then((res) => {
            if (res.ok) {
              logoutSuccess.classList.add("active");
              setTimeout(() => {
                logoutSuccess.classList.remove("active");
                window.location.href = "/";
              }, 2000);
            }
          });
      });
    }
  })();


  // ============================================================
  // 3) SEARCH BAR (เปิด–ปิด)
  // ============================================================
  (() => {
    const searchIcon = document.getElementById("search-icon");
    const searchClose = document.getElementById("search-close");
    const navBottom = document.querySelector(".nav-bottom");

    if (searchIcon && searchClose && navBottom) {
      searchIcon.addEventListener("click", (e) => {
        e.preventDefault();
        navBottom.classList.add("search-active");
      });

      searchClose.addEventListener("click", () => {
        navBottom.classList.remove("search-active");
      });
    }
  })();


  // ============================================================
  // 4) DROPDOWN หมวดหมู่ (เหลือง)
  // ============================================================
  (() => {
    const categoryDropdown = document.querySelector('.dropdown-category');
    if (!categoryDropdown) return;

    const toggle = categoryDropdown.querySelector('.dropdown-toggle');
    const overlay = categoryDropdown.querySelector('.overlay');

    if (toggle && overlay) {
      toggle.addEventListener('click', (e) => {
        e.preventDefault();
        categoryDropdown.classList.toggle('active');
      });

      overlay.addEventListener('click', () => {
        categoryDropdown.classList.remove('active');
      });
    }
  })();


  // ============================================================
  // 5) FILTER OVERLAY (เปิด–ปิด)
  // ============================================================
  (() => {
    const filterIcon = document.getElementById("filter-icon");
    const overlay = document.getElementById("filter-overlay");

    if (filterIcon && overlay) {

      filterIcon.addEventListener("click", (e) => {
        e.preventDefault();
        overlay.classList.add("show");
      });

      overlay.addEventListener("click", (e) => {
        if (e.target === overlay) {
          overlay.classList.remove("show");
        }
      });
    }
  })();


  // ============================================================
  // 6) SINGLE SELECT BUTTON (category / status / rating)
  // ============================================================
  document.addEventListener("click", (e) => {
    const container = e.target.closest(".category-buttons, .status-buttons, .rating-buttons");
    if (!container) return;

    const btn = e.target.closest("button");
    if (!btn) return;

    const wasActive = btn.classList.contains("active");
    container.querySelectorAll("button").forEach(b => b.classList.remove("active"));
    if (!wasActive) btn.classList.add("active");
  });


  // ============================================================
  // 7) FILTER → redirect ไป /product?... 
  // ============================================================
  (() => {
  const confirmBtn = document.getElementById("filter-confirm-button");
  if (!confirmBtn) return;

  confirmBtn.addEventListener("click", (e) => {
    e.preventDefault();

    const getActiveData = (selector) => {
      const el = document.querySelector(selector + " .active");
      return el ? el.dataset.category || el.dataset.status || el.dataset.rating : null;
    };

    const category = getActiveData(".category-buttons");
    const status = getActiveData(".status-buttons");
    const rating = getActiveData(".rating-buttons");

    const minPriceVal = document.getElementById("min-price")?.value.trim();
    const maxPriceVal = document.getElementById("max-price")?.value.trim();

    const minPrice = minPriceVal !== "" ? Number(minPriceVal) : null;
    const maxPrice = maxPriceVal !== "" ? Number(maxPriceVal) : null;

    // ⭐ TC6: ถ้า min > max → แจ้งเตือน + ไม่ค้นหา
    if (minPrice !== null && maxPrice !== null && minPrice > maxPrice) {
      alert("ราคาต่ำสุดต้องไม่มากกว่าราคาสูงสุด");
      return;
    }

    const params = new URLSearchParams();

    if (category) params.set("category", category);
    if (status) params.set("status", status);
    if (rating) params.set("rating", rating);
    if (minPrice !== null) params.set("minPrice", minPrice);
    if (maxPrice !== null) params.set("maxPrice", maxPrice);

    document.getElementById("filter-overlay").classList.remove("show");

    window.location.href = `/product?${params.toString()}`;
  });
})();

  // ====================== CLEAR FILTER ======================
document.addEventListener("DOMContentLoaded", () => {
    const clearBtn = document.getElementById("filter-clear-button");
    if (!clearBtn) return;

    clearBtn.addEventListener("click", () => {
        // ปิด overlay
        const overlay = document.getElementById("filter-overlay");
        overlay?.classList.remove("show");

        // redirect กลับหน้าเริ่มต้น
        window.location.href = "/buyerTemp";
    });
});


});
