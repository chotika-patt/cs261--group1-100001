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

  // Validation
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
  if(!priceVal || isNaN(priceVal)){
    alert("กรุณากรอกราคาสินค้าเป็นตัวเลข");
    return;
  }

  const stockVal = formData.get('stock');
  if(!stockVal || isNaN(stockVal)){
    alert("กรุณากรอกจำนวนสินค้าเป็นตัวเลข");
    return;
  }

  // Add image file if exists
  if(imageInput.files.length > 0){
    formData.set('main_image', imageInput.files[0]);
  }

  // Get productId from URL
  const productId = window.location.pathname.split('/').pop();

  try {
    const response = await fetch(`/api/seller/product/${productId}`, {
      method: 'PUT',
      body: formData,
      credentials: 'include'
    });

    if(response.ok){
      alert("✅ อัปเดตสินค้าเรียบร้อย!");
      window.location.href = "/sellerTemp";
    } else {
      const msg = await response.text();
      alert("❌ อัปเดตสินค้าไม่สำเร็จ: " + msg);
    }
  } catch(err){
    alert("❌ เกิดข้อผิดพลาด: " + err.message);
  }
});