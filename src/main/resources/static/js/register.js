function switchForm(type) {
    const buyerForm = document.getElementById('buyerForm');
    const sellerForm = document.getElementById('sellerForm');
    const buyerBtn = document.getElementById('buyerBtn');
    const sellerBtn = document.getElementById('sellerBtn');
    const toggleSlider = document.getElementById('toggleSlider');

    if (type === 'buyer') {
        buyerForm.classList.add('active');
        sellerForm.classList.remove('active');
        buyerBtn.classList.add('active');
        sellerBtn.classList.remove('active');
        toggleSlider.classList.remove('seller');
    } else {
        sellerForm.classList.add('active');
        buyerForm.classList.remove('active');
        sellerBtn.classList.add('active');
        buyerBtn.classList.remove('active');
        toggleSlider.classList.add('seller');
    }
}

// ----------------------------
// ฟอร์มผู้ขาย
// ----------------------------
document.getElementById("sellerForm").addEventListener("submit", async (event) => {
  event.preventDefault();

  // 1️⃣ ส่ง JSON ลงทะเบียน
  const userData = {
    username: document.getElementById("seller-username").value,
    email: document.getElementById("seller-email").value,
    password: document.getElementById("seller-password").value,
    confirm_password: document.getElementById("seller-confirm-password").value,
    phone: document.getElementById("seller-phone").value,
    studentID: document.getElementById("student-ID").value,
    role: "SELLER"
  };

  try {
    // ส่ง JSON ไปสร้าง User
    const response = await fetch("/api/register/seller", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(userData)
    });

    if (!response.ok) {
      const text = await response.text();
      return alert("ลงทะเบียนล้มเหลว: " + text);
    }

    // รับ user_id จาก backend (ต้องแก้ service ให้ return user_id)
    const userId = await response.text(); 

    // 2️⃣ ส่งไฟล์ไป UploadController
    const fileInput = document.getElementById("application-document");
    if (fileInput.files.length > 0) {
      const formData = new FormData();
      formData.append("application_document", fileInput.files[0]);

      const uploadResponse = await fetch(`/api/upload/seller/${userId}`, {
        method: "POST",
        body: formData
      });

      const uploadResult = await uploadResponse.text();
      alert(uploadResult);
    } else {
      alert("ลงทะเบียนเรียบร้อย แต่ไม่ได้แนบไฟล์");
    }

    // ไปหน้า login
    window.location.href = "login.html";

  } catch (error) {
    alert("เกิดข้อผิดพลาด: " + error.message);
  }
});

