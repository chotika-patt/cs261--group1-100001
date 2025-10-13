// Code นี้ ใช้กับ template
    const userBtn = document.getElementById("user-btn");
    const closeBtn = document.getElementById("close-dropdown");
    const logoutBtn = document.getElementById("logout-btn");
    const logoutModal = document.getElementById("logout-modal");
    const cancelLogout = document.getElementById("cancel-logout");
    const confirmLogout = document.getElementById("confirm-logout");
    const closeModal = document.getElementById("close-modal");

    // เปิด/ปิด dropdown
    userBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      dropdown.classList.toggle("active");
    });

    closeBtn.addEventListener("click", () => {
      dropdown.classList.remove("active");
    });

    window.addEventListener("click", (e) => {
      if (!dropdown.contains(e.target) && !userBtn.contains(e.target)) {
        dropdown.classList.remove("active");
      }
    });

    // Popup Logout
    logoutBtn.addEventListener("click", (e) => {
      e.preventDefault();
      dropdown.classList.remove("active");
      logoutModal.classList.add("active");
    });

    cancelLogout.addEventListener("click", () => {
      logoutModal.classList.remove("active");
    });

    closeModal.addEventListener("click", () => {
      logoutModal.classList.remove("active");
    });

    confirmLogout.addEventListener("click", () => {
      window.location.href = "index.html";
    });

    const searchIcon = document.getElementById('search-icon');
    const searchClose = document.getElementById('search-close');
    const navBottom = document.querySelector('.nav-bottom');

    searchIcon.addEventListener('click', (event) => {
      event.preventDefault(); 
      navBottom.classList.add('search-active');
    });

    searchClose.addEventListener('click', () => {
      navBottom.classList.remove('search-active');
    });
    // ===== แถบค้นหาแบบใหม่ =====
    

    // ===== แถบแดงหมวดหมู่สินค้าหน้า product เป็นแถบ dropdown =====
    const toggle = document.querySelector('.dropdown-toggle');
    const dropdown = document.querySelector('.dropdown-content');
    const overlay = document.querySelector('.overlay');

    toggle.addEventListener('click', function(e) {
      e.preventDefault();
      const isOpen = dropdown.style.display === 'block';
      dropdown.style.display = isOpen ? 'none' : 'block';
      overlay.style.display = isOpen ? 'none' : 'block';
    });

    overlay.addEventListener('click', function() {
      dropdown.style.display = 'none';
      overlay.style.display = 'none';
    });

