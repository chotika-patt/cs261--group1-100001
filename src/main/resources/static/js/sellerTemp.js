// ====== ส่วนจัดการลบสินค้า ======
const deleteModal = document.getElementById('delete-product-modal');
const deleteSuccessModal = document.getElementById('delete-product-success');
const cancelDeleteBtn = document.getElementById('cancel-delete-product');
const confirmDeleteBtn = document.getElementById('confirm-delete-product');
const closeDeleteModal = document.getElementById('close-delete-modal');

let productIdToDelete = null;

// ----- Async Delete Function -----
async function confirmDelete(productId) {
    if (!productId) return;

    try {
        const response = await fetch(`/api/seller/product/${productId}`, {
            method: 'DELETE',
            credentials: 'include' // ใช้ session
        });

        const msg = await response.text();
        if (response.ok) {
            alert(msg);
            // ซ่อน modal success ถ้ามี
            if(deleteSuccessModal){
                deleteSuccessModal.style.display = 'flex';
                setTimeout(() => {
                    deleteSuccessModal.style.display = 'none';
                    location.reload();
                }, 1500);
            } else {
                location.reload();
            }
        } else {
            alert("❌ ลบสินค้าไม่สำเร็จ: " + msg);
        }
    } catch (err) {
        alert("❌ เกิดข้อผิดพลาด: " + err.message);
    }
}

// ----- เมื่อคลิกปุ่มลบสินค้า -----
const deleteButtons = document.querySelectorAll('.delete-btn');
deleteButtons.forEach(btn => {
    btn.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();

        productIdToDelete = this.getAttribute('data-product-id');

        if (deleteModal) {
            deleteModal.style.display = 'flex';
        } else {
            console.error('Delete modal not found!');
        }
    });
});

// ----- ปิด modal - ปุ่มยกเลิก -----
if (cancelDeleteBtn) {
    cancelDeleteBtn.addEventListener('click', () => {
        deleteModal.style.display = 'none';
        productIdToDelete = null;
    });
}

// ----- ปิด modal - ปุ่ม X -----
if (closeDeleteModal) {
    closeDeleteModal.addEventListener('click', () => {
        deleteModal.style.display = 'none';
        productIdToDelete = null;
    });
}

// ----- ยืนยันการลบจาก modal -----
if (confirmDeleteBtn) {
    confirmDeleteBtn.addEventListener('click', () => {
        if (productIdToDelete) {
            deleteModal.style.display = 'none';
            confirmDelete(productIdToDelete);
            productIdToDelete = null;
        }
    });
}

// ----- ปิด modal เมื่อคลิกนอก modal -----
window.addEventListener('click', (e) => {
    if (e.target === deleteModal) {
        deleteModal.style.display = 'none';
        productIdToDelete = null;
    }
});
