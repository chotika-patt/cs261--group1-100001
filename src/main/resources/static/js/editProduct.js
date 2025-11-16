// edit-product.js - ไฟล์สำหรับหน้าแก้ไขสินค้า

const imageInput = document.getElementById('imageFile');
const previewImage = document.getElementById('previewImage');
const removeBtn = document.getElementById('removeImage');
const uploadLabel = document.getElementById('uploadLabel');
let currentImageUrl = null; // เก็บ URL รูปเดิม
let imageChanged = false; // ตรวจสอบว่ามีการเปลี่ยนรูปหรือไม่

// ดึง Product ID จาก URL
function getProductIdFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('id') || window.location.pathname.split('/').pop();
}

// โหลดข้อมูลสินค้าเดิม
async function loadProductData() {
    const productId = getProductIdFromUrl();
    
    if (!productId) {
        alert("❌ ไม่พบรหัสสินค้า");
        window.location.href = "/sellerTemp";
        return;
    }

    document.getElementById('productId').value = productId;

    try {
        // แสดง Loading (ถ้ามี)
        showLoading();

        const response = await fetch(`/api/products/${productId}`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("ไม่สามารถโหลดข้อมูลสินค้าได้");
        }

        const product = await response.json();

        // เติมข้อมูลในฟอร์ม
        document.getElementById("name").value = product.name || '';
        document.getElementById("category").value = product.category || '';
        document.getElementById("stock").value = product.stock || 0;
        document.getElementById("price").value = product.price || 0;
        document.getElementById("description").value = product.description || '';

        // แสดงรูปภาพเดิม (ถ้ามี)
        if (product.main_image || product.imageUrl) {
            currentImageUrl = product.main_image || product.imageUrl;
            previewImage.src = currentImageUrl;
            previewImage.style.display = 'block';
            removeBtn.style.display = 'inline-block';
            uploadLabel.style.display = 'none';
        }

        hideLoading();

    } catch (error) {
        hideLoading();
        alert("เกิดข้อผิดพลาดในการโหลดข้อมูล: " + error.message);
        console.error(error);
    }
}

// จัดการการอัพโหลดรูปภาพใหม่
imageInput.addEventListener('change', function () {
    const file = this.files[0];
    if (file) {
        // ตรวจสอบขนาดไฟล์ (ไม่เกิน 5MB)
        if (file.size > 5 * 1024 * 1024) {
            alert("❌ ขนาดไฟล์ใหญ่เกินไป (สูงสุด 5MB)");
            this.value = '';
            return;
        }

        // ตรวจสอบประเภทไฟล์
        if (!file.type.startsWith('image/')) {
            alert("❌ กรุณาเลือกไฟล์รูปภาพเท่านั้น");
            this.value = '';
            return;
        }

        previewImage.src = URL.createObjectURL(file);
        previewImage.style.display = 'block';
        removeBtn.style.display = 'inline-block';
        uploadLabel.style.display = 'none';
        imageChanged = true;
    }
});

// ลบรูปภาพ
removeBtn.addEventListener('click', function () {
    if (confirm("คุณต้องการลบรูปภาพหรือไม่?")) {
        previewImage.src = '';
        previewImage.style.display = 'none';
        imageInput.value = '';
        removeBtn.style.display = 'none';
        uploadLabel.style.display = 'flex';
        currentImageUrl = null;
        imageChanged = true;
    }
});

// Validation ฟอร์ม
function validateForm() {
    const name = document.getElementById("name").value.trim();
    const category = document.getElementById("category").value;
    const stock = parseInt(document.getElementById("stock").value);
    const price = parseFloat(document.getElementById("price").value);
    const description = document.getElementById("description").value.trim();

    if (!name) {
        alert("❌ กรุณาใส่ชื่อสินค้า");
        return false;
    }

    if (!category) {
        alert("❌ กรุณาเลือกหมวดหมู่สินค้า");
        return false;
    }

    if (isNaN(stock) || stock < 0) {
        alert("❌ กรุณาใส่จำนวนสินค้าที่ถูกต้อง");
        return false;
    }

    if (isNaN(price) || price <= 0) {
        alert("❌ กรุณาใส่ราคาที่ถูกต้อง");
        return false;
    }

    if (!description) {
        alert("❌ กรุณาใส่รายละเอียดสินค้า");
        return false;
    }

    return true;
}

// บันทึกการแก้ไข
document.getElementById("submitBtn").addEventListener("click", async (event) => {
    event.preventDefault();

    // Validate Form
    if (!validateForm()) {
        return;
    }

    const productId = document.getElementById('productId').value;

    if (!productId) {
        alert("❌ ไม่พบรหัสสินค้า");
        return;
    }

    // ยืนยันการบันทึก
    if (!confirm("คุณต้องการบันทึกการแก้ไขหรือไม่?")) {
        return;
    }

    const formData = new FormData();
    formData.append("name", document.getElementById("name").value.trim());
    formData.append("category", document.getElementById("category").value);
    formData.append("price", parseFloat(document.getElementById("price").value));
    formData.append("stock", parseInt(document.getElementById("stock").value));
    formData.append("description", document.getElementById("description").value.trim());

    // ส่งรูปใหม่เฉพาะเมื่อมีการเปลี่ยน
    if (imageChanged && imageInput.files.length > 0) {
        formData.append("main_image", imageInput.files[0]);
    }

    try {
        showLoading();

        const response = await fetch(`/api/products/${productId}`, {
            method: "PUT", // หรือใช้ PATCH ตาม API ของคุณ
            credentials: "include",
            body: formData
        });

        hideLoading();

        if (!response.ok) {
            const msg = await response.text();
            showModal("error", "แก้ไขสินค้าไม่สำเร็จ", msg);
            return;
        }

        // แสดง Modal สำเร็จ
        showModal("success", "แก้ไขสินค้าสำเร็จ", "ข้อมูลสินค้าของคุณได้รับการอัปเดตเรียบร้อยแล้ว");
    } catch (error) {
        hideLoading();
        alert("เกิดข้อผิดพลาด: " + error.message);
        console.error(error);
    }
});

// ฟังก์ชัน Loading (ถ้าต้องการ)
function showLoading() {
    const submitBtn = document.getElementById("submitBtn");
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> กำลังบันทึก...';
    }
}

function hideLoading() {
    const submitBtn = document.getElementById("submitBtn");
    if (submitBtn) {
        submitBtn.disabled = false;
        submitBtn.textContent = 'บันทึกข้อมูลสินค้า';
    }
}

// เรียกใช้เมื่อโหลดหน้า
window.addEventListener('DOMContentLoaded', loadProductData);