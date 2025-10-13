  // =================== USER DROPDOWN ===================
const userBtn = document.getElementById("user-btn");
const dropdown = document.getElementById("user-dropdown");
const closeBtn = document.getElementById("close-dropdown");

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

// =================== LOGOUT MODAL ===================
const logoutBtn = document.getElementById("logout-btn");
const logoutModal = document.getElementById("logout-modal");
const cancelLogout = document.getElementById("cancel-logout");
const confirmLogout = document.getElementById("confirm-logout");
const closeModal = document.getElementById("close-modal");
const logoutSuccess = document.getElementById("logout-success");

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
  logoutModal.classList.remove("active");

  // ส่ง POST request ไป /logout
  fetch('/api/logout', { method: 'POST' })
    .then(response => {
      if (response.ok) {
        logoutSuccess.classList.add("active");
        setTimeout(() => {
          logoutSuccess.classList.remove("active");
          window.location.href = "/index.html"; // redirect หลัง logout สำเร็จ
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


// =================== SEARCH BAR ===================
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

// =================== DROPDOWN หมวดหมู่สินค้า ===================
const toggle = document.querySelector('.dropdown-toggle');
const categoryDropdown = document.querySelector('.dropdown-content');
const overlay = document.querySelector('.overlay');

toggle.addEventListener('click', function(e) {
  e.preventDefault();
  const isOpen = categoryDropdown.style.display === 'block';
  categoryDropdown.style.display = isOpen ? 'none' : 'block';
  overlay.style.display = isOpen ? 'none' : 'block';
});

overlay.addEventListener('click', function() {
  categoryDropdown.style.display = 'none';
  overlay.style.display = 'none';
});

