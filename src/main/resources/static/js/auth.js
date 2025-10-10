

// Code ใช้ตอน login
document.getElementById("subBtn").addEventListener("click", function(event) {
    event.preventDefault(); // ป้องกัน reload หน้า

    // ดึงค่า username และ password
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();

    if (!username || !password) {
        alert("กรุณากรอกชื่อผู้ใช้และรหัสผ่าน");
        return;
    }

    // ส่ง POST request เป็น JSON
    fetch("/api/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            username: username,
            password: password
        })
    })
    .then(response => {
        if (response.ok) {
            // login สำเร็จ
            alert("Login สำเร็จ!");
            window.location.href = "/loginTemp"; // redirect ไปหน้าแรก
        } else if (response.status === 401) {
            alert("ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง");
        } else {
            alert("เกิดข้อผิดพลาด กรุณาลองใหม่");
        }
    })
    .catch(error => {
        console.error("Error:", error);
        alert("เกิดข้อผิดพลาดในการเชื่อมต่อเซิร์ฟเวอร์");
    });
});
