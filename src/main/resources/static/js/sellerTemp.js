// =================== Popup เพิ่มสินค้า ===================
const addProductBtn = document.querySelector(".add-product-btn");
const addProductPopup = document.getElementById("add-product-popup");
const closePopupBtn = document.getElementById("close-popup");
const addProductForm = document.getElementById("add-product-form");

// เปิด popup
if (addProductBtn) {
  addProductBtn.addEventListener("click", () => {
    addProductPopup.classList.add("active");
  });
}

// ปิด popup
if (closePopupBtn) {
  closePopupBtn.addEventListener("click", () => {
    addProductPopup.classList.remove("active");
  });
}

// ส่งข้อมูลฟอร์ม (ตัวอย่างยังไม่เชื่อม API)
if (addProductForm) {
  addProductForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const productData = {
      name: document.getElementById("product-name").value,
      category: document.getElementById("product-category").value,
      price: parseFloat(document.getElementById("product-price").value),
      stock: parseInt(document.getElementById("product-stock").value),
      description: document.getElementById("product-description").value,
      image: document.getElementById("product-image").files[0]?.name || null,
    };

    console.log("📦 เพิ่มสินค้าใหม่:", productData);

    // ตัวอย่างแจ้งเตือน
    alert("เพิ่มสินค้าเรียบร้อย!");

    // ปิด popup และเคลียร์ข้อมูล
    addProductPopup.classList.remove("active");
    addProductForm.reset();
  });
}
