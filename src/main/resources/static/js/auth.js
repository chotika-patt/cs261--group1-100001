document.addEventListener("DOMContentLoaded", () => {
  const btn = document.getElementById("subBtn");

  if (btn) {
    btn.addEventListener("click", async function (event) {
      event.preventDefault();

      const username = document.getElementById("username").value.trim();
      const password = document.getElementById("password").value.trim();

      if (!username || !password) {
        showErrorBanner("กรุณากรอกชื่อผู้ใช้และรหัสผ่านให้ครบ");
        return;
      }

      btn.disabled = true;
      btn.textContent = "กำลังเข้าสู่ระบบ...";

      try {
        const response = await fetch("/api/login", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          credentials: "include",
          body: JSON.stringify({ username, password }),
        });

        if (response.ok) {
          const data = await response.json();

          setTimeout(() => {
            if (data.role === "SELLER") window.location.href = "sellerTemp";
            else if (data.role === "CLIENT") window.location.href = "buyerTemp";
            else window.location.href = "index";
          }, 2000);
        } else if (response.status === 400) {
          showErrorBanner("กรุณากรอกชื่อผู้ใช้และรหัสผ่านให้ครบ");
        } else if (response.status === 401) {
          showErrorBanner("ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง");
        } else {
          showErrorBanner("เกิดข้อผิดพลาดในระบบ กรุณาลองใหม่อีกครั้ง");
        }
      } catch (error) {
        console.error("Error:", error);
        showErrorBanner("ไม่สามารถเชื่อมต่อกับเซิร์ฟเวอร์ได้");
      } finally {
        btn.disabled = false;
        btn.textContent = "เข้าสู่ระบบ";
      }
    });
  }
});

// ฟังก์ชันสร้าง & แสดง overlay แจ้งเตือน (อัตโนมัติ)
function createBanner(type, message) {

  const existing = document.querySelector(".banner");
  if (existing) existing.remove();

  const banner = document.createElement("div");
  banner.className = `banner ${type}`;
  banner.innerHTML = `
    <i class="fa-solid ${type === "error" ? "fa-circle-exclamation" : "fa-circle-check"}"></i>
    <span>${message}</span>
  `;
  document.body.appendChild(banner);

  setTimeout(() => banner.classList.add("show"), 10);
  setTimeout(() => banner.classList.remove("show"), 3000);
}

function showErrorBanner(message) {
  createBanner("error", message);
}

function showSuccessBanner(message) {
  createBanner("success", message);
}
