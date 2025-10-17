document.getElementById("subBtn").addEventListener("click", async function(event) {
    event.preventDefault(); // ❌ ป้องกันการ reload หน้า

    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();

    if (!username || !password) {
        alert("⚠️ กรุณากรอกชื่อผู้ใช้และรหัสผ่านให้ครบ");
        return;
    }

    // ปิดปุ่มชั่วคราวกันกดซ้ำ
    const btn = document.getElementById("subBtn");
    btn.disabled = true;
    btn.textContent = "กำลังเข้าสู่ระบบ...";

    try {
        const response = await fetch("/api/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "include", // ✅ สำคัญมาก — เก็บ session cookie
            body: JSON.stringify({
                username: username,
                password: password
            })
        });

        if (response.ok) {
            alert("✅ เข้าสู่ระบบสำเร็จ!");
            // ✅ redirect ไป controller ที่อ่าน session แล้วแสดงชื่อผู้ใช้
            window.location.href = "/loginTemp";
        } else if (response.status === 401) {
            alert("❌ ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง");
        } else {
            alert("⚠️ เกิดข้อผิดพลาดในระบบ กรุณาลองใหม่อีกครั้ง");
        }
    } catch (error) {
        console.error("Error:", error);
        alert("❌ ไม่สามารถเชื่อมต่อกับเซิร์ฟเวอร์ได้");
    } finally {
        btn.disabled = false;
        btn.textContent = "เข้าสู่ระบบ";
    }
});
