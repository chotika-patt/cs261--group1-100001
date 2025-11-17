const imageInput = document.getElementById('editImageFile');
const previewImage = document.getElementById('editPreviewImage');
const removeBtn = document.getElementById('editRemoveImage');
const uploadLabel = document.getElementById('editUploadLabel');
const submitBtn = document.getElementById('submitBtn');
const form = document.getElementById('editForm');

// ----- Image Preview & Remove -----
imageInput.addEventListener('change', function() {
  const file = this.files[0];
  if(file){
    // TC4 – ตรวจสอบชนิดไฟล์
    const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    if(!validTypes.includes(file.type)){
      alert("กรุณาอัปโหลดรูปภาพที่ถูกต้อง (jpg/png/gif/webp)");
      this.value = '';
      return;
    }
    // TC4 – ตรวจสอบขนาดไฟล์ไม่เกิน 4MB
    const maxSizeMB = 4;
    if(file.size > maxSizeMB * 1024 * 1024){
      alert(`กรุณาอัปโหลดรูปภาพไม่เกิน ${maxSizeMB} MB`);
      this.value = '';
      return;
    }

    previewImage.src = URL.createObjectURL(file);
    previewImage.style.display = 'block';
    removeBtn.style.display = 'inline-block';
    uploadLabel.style.display = 'none';
  }
});

removeBtn.addEventListener('click', function() {
  previewImage.src = '';
  previewImage.style.display = 'none';
  imageInput.value = '';
  removeBtn.style.display = 'none';
  uploadLabel.style.display = 'flex';
});

// ----- Submit Form via PUT -----
submitBtn.addEventListener('click', async (e) => {
  e.preventDefault();
  const formData = new FormData(form);

  // ----- Validation -----
  const name = formData.get('name');
  if(!name || name.trim() === ''){
    alert("กรุณากรอกชื่อสินค้า");
    return;
  }

  const category = formData.get('category');
  if(!category || category.trim() === ''){
    alert("กรุณาเลือกหมวดหมู่สินค้า");
    return;
  }
  formData.set('category', category.toUpperCase());

  const priceVal = formData.get('price');
  if(!priceVal || isNaN(priceVal) || parseFloat(priceVal) < 0){
    alert("กรุณากรอกราคาสินค้าเป็นตัวเลขบวก");
    return;
  }

  const stockVal = formData.get('stock');
  if(!stockVal || isNaN(stockVal) || parseInt(stockVal) < 0){
    alert("กรุณากรอกจำนวนสินค้าเป็นจำนวนเต็มบวก");
    return;
  }

  // ----- Add image file if exists -----
  if(imageInput.files.length > 0){
    formData.set('main_image', imageInput.files[0]);
  }

  // ----- Get productId from URL -----
  const productId = window.location.pathname.split('/').pop();

  try {
    const response = await fetch(`/api/seller/product/${productId}`, {
      method: 'PUT',
      body: formData,
      credentials: 'include'
    });

    const msg = await response.text();

    // TC7 – ตรวจสอบรหัสซ้ำจาก response
    if(!response.ok){
      if(msg.includes("SKU_DUPLICATE")){
        alert("รหัสสินค้าซ้ำ กรุณาเปลี่ยน");
      } else {
        alert("❌ อัปเดตสินค้าไม่สำเร็จ: " + msg);
      }
      return;
    }

    alert("✅ อัปเดตสินค้าเรียบร้อย!");
    window.location.href = "/sellerTemp";

  } catch(err){
    alert("❌ เกิดข้อผิดพลาด: " + err.message);
  }
});
