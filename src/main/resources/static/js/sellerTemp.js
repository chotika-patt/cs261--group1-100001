async function confirmDelete(productId) {
    if (confirm("⚠️ คุณแน่ใจหรือไม่ว่าต้องการลบสินค้านี้?")) {
        try {
            const response = await fetch(`api/seller/product/${productId}`, {
                method: 'DELETE',
                credentials: 'include'  // ใช้ session
            });

            const msg = await response.text();
            if (response.ok) {
                alert(msg);
                // ลบ row ของ product ในตาราง หรือ reload หน้า
                location.reload();
            } else {
                alert("❌ ลบสินค้าไม่สำเร็จ: " + msg);
            }
        } catch (err) {
            alert("❌ เกิดข้อผิดพลาด: " + err.message);
        }
    }
}
// ================= Add Product Popup =================

const addPopup = document.getElementById("add-product-popup");
const addOpenBtn = document.querySelector(".add-product-btn");
const addCloseBtn = document.getElementById("close-popup");

addOpenBtn.addEventListener("click", () => addPopup.classList.add("active"));
addCloseBtn.addEventListener("click", () => addPopup.classList.remove("active"));

// ================= Image Preview + Remove =================
const imageInput = document.getElementById("product-image");
const previewImage = document.getElementById("previewImage");
const removeBtn = document.getElementById("removeImage");
const uploadLabel = document.getElementById("uploadLabel");

imageInput.addEventListener("change", () => {
  const file = imageInput.files[0];
  if(file){
    previewImage.src = URL.createObjectURL(file);
    previewImage.style.display = "block";
    removeBtn.style.display = "inline-block";
    uploadLabel.style.display = "none";
  }
});

removeBtn.addEventListener("click", () => {
  previewImage.src = "";
  previewImage.style.display = "none";
  imageInput.value = "";
  removeBtn.style.display = "none";
  uploadLabel.style.display = "flex";
});

// ================= Edit Product Popup (ถ้ามี) =================
const editPopup = document.getElementById("edit-product-popup");
const editOpenBtn = document.querySelector(".edit-product-btn");
const editCloseBtn = document.getElementById("close-edit-popup");

if(editPopup && editOpenBtn && editCloseBtn){
  editOpenBtn.addEventListener("click", () => editPopup.classList.add("active"));
  editCloseBtn.addEventListener("click", () => editPopup.classList.remove("active"));
}

// ================= Image Preview + Remove สำหรับ Edit =================
const editImageInput = document.getElementById("edit-product-image");
const editPreviewImage = document.getElementById("edit-previewImage");
const editRemoveBtn = document.getElementById("edit-removeImage");
const editUploadLabel = document.getElementById("edit-uploadLabel");

if(editImageInput){
  editImageInput.addEventListener("change", () => {
    const file = editImageInput.files[0];
    if(file){
      editPreviewImage.src = URL.createObjectURL(file);
      editPreviewImage.style.display = "block";
      editRemoveBtn.style.display = "inline-block";
      editUploadLabel.style.display = "none";
    }
  });

  editRemoveBtn.addEventListener("click", () => {
    editPreviewImage.src = "";
    editPreviewImage.style.display = "none";
    editImageInput.value = "";
    editRemoveBtn.style.display = "none";
    editUploadLabel.style.display = "flex";
  });
}
