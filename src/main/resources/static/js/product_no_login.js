document.addEventListener("DOMContentLoaded", function () {
    const buyBtn = document.getElementById("buy-btn");
    const popup = document.getElementById("login-popup");
    const popupOkBtn = document.getElementById("popup-ok-btn");

    buyBtn.addEventListener("click", function () {
        popup.style.display = "flex";
    });

    popupOkBtn.addEventListener("click", function () {
        popup.style.display = "none";
        window.location.href = "login.html"; // ✅ redirect ไปหน้า login
    });
});
