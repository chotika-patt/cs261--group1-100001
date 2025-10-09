const userBtn = document.getElementById("user-btn");
    const dropdown = document.getElementById("user-dropdown");
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