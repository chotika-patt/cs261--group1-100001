const imageInput = document.getElementById('imageFile');
const previewImage = document.getElementById('previewImage');
const removeBtn = document.getElementById('removeImage');
const uploadLabel = document.getElementById('uploadLabel');

// เมื่อเลือกไฟล์
imageInput.addEventListener('change', function() {
  const file = this.files[0];
  if (file) {
    previewImage.src = URL.createObjectURL(file);
    previewImage.style.display = 'block';
    removeBtn.style.display = 'inline-block';
    uploadLabel.style.display = 'none'; // ซ่อน icon + กับข้อความ
  }
});

// เมื่อกดปุ่มลบ
removeBtn.addEventListener('click', function() {
  previewImage.src = '';
  previewImage.style.display = 'none';
  imageInput.value = ''; // รีเซ็ต input
  removeBtn.style.display = 'none';
  uploadLabel.style.display = 'flex';
});



     document.getElementById("submitBtn").addEventListener("click", async (event) => {
       event.preventDefault();


        const formData = new FormData();
        formData.append("name", document.getElementById("name").value);
        formData.append("category", document.getElementById("category").value);
        formData.append("price", parseFloat(document.getElementById("price").value));
        formData.append("stock", parseInt(document.getElementById("stock").value));
        formData.append("description", document.getElementById("description").value);
        
         const mainImageInput = document.getElementById("imageFile");
        if (mainImageInput && mainImageInput.files.length > 0) {
            formData.append("main_image", mainImageInput.files[0]);
        }
        /*
       const productData = {
         name: document.getElementById("name").value,
         category: document.getElementById("category").value,
         price: parseFloat(document.getElementById("price").value),
         stock: parseInt(document.getElementById("stock").value),
         status: "AVAILABLE",
         description: document.getElementById("description").value
       };
       */

       try {
         const response = await fetch("/api/add", {
           method: "POST",
           //headers: { "Content-Type": "application/json" },
           credentials: "include",
           body: formData
         });

         if (!response.ok) {
           const msg = await response.text();
           alert("❌ เพิ่มสินค้าไม่สำเร็จ: " + msg);
           return;
         }

         alert("✅ เพิ่มสินค้าเรียบร้อย!");
         window.location.href = "/sellerTemp";
       } catch (error) {
         alert("เกิดข้อผิดพลาด: " + error.message);
       }


     });