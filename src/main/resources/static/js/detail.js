// ===== แถบแดงหมวดหมู่สินค้าหน้า product เป็นแถบ dropdown =====
const toggle = document.querySelector('.dropdown-toggle');
const dropdown = document.querySelector('.dropdown-content');
const overlay = document.querySelector('.overlay');

toggle.addEventListener('click', function(e) {
e.preventDefault();
    const isOpen = dropdown.style.display === 'block';
    dropdown.style.display = isOpen ? 'none' : 'block';
    overlay.style.display = isOpen ? 'none' : 'block';
});

overlay.addEventListener('click', function() {
dropdown.style.display = 'none';
overlay.style.display = 'none';
});
const minusBtn = document.querySelector('.minus');
const plusBtn = document.querySelector('.plus');
const quantityInput = document.getElementById('quantity');
plusBtn.addEventListener('click', () => {
    let currentValue = parseInt(quantityInput.value);
    quantityInput.value = currentValue + 1;
});
minusBtn.addEventListener('click', () => {
    let currentValue = parseInt(quantityInput.value);
    if (currentValue > 1) { // ไม่ให้ลดต่ำกว่า 1
        quantityInput.value = currentValue - 1;
    }
});