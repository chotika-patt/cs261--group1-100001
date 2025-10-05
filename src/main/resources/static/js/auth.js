document.addEventListener("DOMContentLoaded", () => {
    const subBtn = document.getElementById("subBtn"); // ปุ่ม Login
    subBtn.addEventListener("click", async (event) => {
        event.preventDefault(); // ป้องกัน form reload หน้า

        const username = document.getElementById("email").value;
        const password = document.getElementById("password").value;

        // ส่ง request ไป backend
        const response = await fetch("/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });

        if (response.ok) {
            alert("Login successful");
            window.location.href = "home.html"; // redirect หลัง login
        } else {
            alert("Invalid username or password");
        }
    });
});
