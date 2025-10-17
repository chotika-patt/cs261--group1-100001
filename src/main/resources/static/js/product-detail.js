// =================== PRODUCT QUANTITY ===================
(() => {
  const minusBtn = document.querySelector('.minus');
  const plusBtn = document.querySelector('.plus');
  const quantityInput = document.getElementById('quantity');

  if (minusBtn && plusBtn && quantityInput) {
    plusBtn.addEventListener('click', () => {
      let currentValue = parseInt(quantityInput.value);
      quantityInput.value = currentValue + 1;
    });

    minusBtn.addEventListener('click', () => {
      let currentValue = parseInt(quantityInput.value);
      if (currentValue > 1) {
        quantityInput.value = currentValue - 1;
      }
    });
  }
})();