// จัดการ popup ลบสินค้า
document.addEventListener('DOMContentLoaded', function() {
  
  // ====== ส่วนจัดการลบสินค้า ======
  const deleteModal = document.getElementById('delete-product-modal');
  const deleteSuccessModal = document.getElementById('delete-product-success');
  const cancelDeleteBtn = document.getElementById('cancel-delete-product');
  const confirmDeleteBtn = document.getElementById('confirm-delete-product');
  const closeDeleteModal = document.getElementById('close-delete-modal');
  
  let productIdToDelete = null;

  // เช็คว่า element มีอยู่จริงหรือไม่
  console.log('Delete modal:', deleteModal);
  console.log('Delete buttons:', document.querySelectorAll('.delete-btn'));

  // เมื่อคลิกปุ่มลบสินค้า
  const deleteButtons = document.querySelectorAll('.delete-btn');
  console.log('Found delete buttons:', deleteButtons.length);
  
  deleteButtons.forEach(btn => {
    btn.addEventListener('click', function(e) {
      e.preventDefault();
      e.stopPropagation();
      
      productIdToDelete = this.getAttribute('data-product-id');
      console.log('Product ID to delete:', productIdToDelete);
      
      if (deleteModal) {
        deleteModal.style.display = 'flex';
        console.log('Modal should be visible now');
      } else {
        console.error('Delete modal not found!');
      }
    });
  });

  // ปิด modal - ปุ่มยกเลิก
  if (cancelDeleteBtn) {
    cancelDeleteBtn.addEventListener('click', () => {
      deleteModal.style.display = 'none';
      productIdToDelete = null;
    });
  }

  // ปิด modal - ปุ่ม X
  if (closeDeleteModal) {
    closeDeleteModal.addEventListener('click', () => {
      deleteModal.style.display = 'none';
      productIdToDelete = null;
    });
  }

  // ยืนยันการลบ
  if (confirmDeleteBtn) {
    confirmDeleteBtn.addEventListener('click', () => {
      if (productIdToDelete) {
        deleteModal.style.display = 'none';
        
        // แสดง popup สำเร็จ
        if (deleteSuccessModal) {
          deleteSuccessModal.style.display = 'flex';
        }
        
        // รีไดเร็กไปหน้าลบสินค้าหลัง 1.5 วินาที
        setTimeout(() => {
          window.location.href = '/product_delete/' + productIdToDelete;
        }, 1500);
      }
    });
  }

  // ปิด modal เมื่อคลิกนอก modal
  window.addEventListener('click', (e) => {
    if (e.target === deleteModal) {
      deleteModal.style.display = 'none';
      productIdToDelete = null;
    }
  });

  // ====== ส่วนจัดการเพิ่มสินค้า (ถ้ามี) ======
  const popup = document.getElementById("add-product-popup");
  const openBtn = document.querySelector(".add-product-btn");
  const closeBtn = document.getElementById("close-popup");
  const form = document.getElementById("add-product-form");

  if (openBtn && popup) {
    openBtn.addEventListener("click", (e) => {
      e.preventDefault();
      popup.classList.add("active");
    });
  }

  if (closeBtn && popup) {
    closeBtn.addEventListener("click", () => popup.classList.remove("active"));
  }

  if (form) {
    form.addEventListener("submit", async (e) => {
      e.preventDefault();

      const formData = new FormData();
      formData.append("name", document.getElementById("product-name").value);
      formData.append("category", document.getElementById("product-category").value);
      formData.append("price", document.getElementById("product-price").value);
      formData.append("stock", document.getElementById("product-stock").value);
      formData.append("description", document.getElementById("product-description").value);

      const fileInput = document.getElementById("product-image");
      if(fileInput.files.length > 0){
        formData.append("main_image", fileInput.files[0]);
      }

      try {
        const res = await fetch("/api/add", {
          method: "POST",
          body: formData
        });

        if (res.ok) {
          alert("เพิ่มสินค้าสำเร็จ!");
          form.reset();
          location.reload();
        } else {
          const msg = await res.text();
          alert("เกิดข้อผิดพลาด: " + msg);
        }
      } catch (err) {
        console.error(err);
        alert("ไม่สามารถเชื่อมต่อกับเซิร์ฟเวอร์ได้");
      }
    });
  }
});