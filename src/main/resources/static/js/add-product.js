const imageInput = document.getElementById('imageFile');
const previewImage = document.getElementById('previewImage');
const removeBtn = document.getElementById('removeImage');
const uploadLabel = document.getElementById('uploadLabel');
const submitBtn = document.getElementById("submitBtn");

// ----- Image Preview & Remove -----
imageInput.addEventListener('change', function() {
  const file = this.files[0];
  if(file){
    // ตรวจสอบชนิดไฟล์
    const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    if(!validTypes.includes(file.type)){
      alert("กรุณาอัปโหลดรูปภาพที่ถูกต้อง (jpg/png/gif/webp)");
      this.value = '';
      return;
    }

    // ตรวจสอบขนาดไฟล์ไม่เกิน 4MB
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

// ----- Submit Form via POST -----
submitBtn.addEventListener("click", async (event) => {
  event.preventDefault();

  // ----- Validation -----
  const name = document.getElementById("name").value.trim();
  if(!name){
    alert("กรุณากรอกชื่อสินค้า");
    return;
  }

  const category = document.getElementById("category").value.trim();
  if(!category){
    alert("กรุณาเลือกหมวดหมู่สินค้า");
    return;
  }

  const priceVal = parseFloat(document.getElementById("price").value);
  if(isNaN(priceVal) || priceVal < 0){
    alert("กรุณากรอกราคาสินค้าเป็นตัวเลขบวก");
    return;
  }

  const stockVal = parseInt(document.getElementById("stock").value);
  if(isNaN(stockVal) || stockVal < 0){
    alert("กรุณากรอกจำนวนสินค้าเป็นจำนวนเต็มบวก");
    return;
  }

  const description = document.getElementById("description").value.trim();

  // ----- FormData -----
  const formData = new FormData();
  formData.append("name", name);
  formData.append("category", category.toUpperCase());
  formData.append("price", priceVal);
  formData.append("stock", stockVal);
  formData.append("description", description);

  if(imageInput.files.length > 0){
    formData.append("main_image", imageInput.files[0]);
  }

  try {
    const response = await fetch("/api/add", {
      method: "POST",
      body: formData,
      credentials: "include"
    });

    const msg = await response.text();

    if(!response.ok){
      if(msg.includes("SKU_DUPLICATE")){
        alert("รหัสสินค้าซ้ำ กรุณาเปลี่ยน");
      } else {
        alert("❌ เพิ่มสินค้าไม่สำเร็จ: " + msg);
      }
      return;
    }

    alert("✅ เพิ่มสินค้าเรียบร้อย!");
    window.location.href = "/sellerTemp";

  } catch(error){
    alert("❌ เกิดข้อผิดพลาด: " + error.message);
  }
});
