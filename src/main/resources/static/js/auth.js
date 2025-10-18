document.addEventListener("DOMContentLoaded", () => {
    const btn = document.getElementById("subBtn");
    if (btn) {
        btn.addEventListener("click", async function(event) {
            event.preventDefault();

            const username = document.getElementById("username").value.trim();
            const password = document.getElementById("password").value.trim();

            if (!username || !password) {
                alert("กรุณากรอกชื่อผู้ใช้และรหัสผ่านให้ครบ");
                return;
            }

            btn.disabled = true;
            btn.textContent = "กำลังเข้าสู่ระบบ...";

            try {
                const response = await fetch("/api/login", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    credentials: "include",
                    body: JSON.stringify({ username, password })
                });

                if (response.ok) {
                    const data = await response.json();
                    alert("เข้าสู่ระบบสำเร็จ!");
                    if (data.role === "SELLER") window.location.href = "sellerTemp";
                    else if (data.role === "CLIENT") window.location.href = "buyerTemp";
                    else window.location.href = "index";
                } else if (response.status === 401) {
                    alert("ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง");
                } else {
                    alert("เกิดข้อผิดพลาดในระบบ กรุณาลองใหม่อีกครั้ง");
                }
            } catch (error) {
                console.error("Error:", error);
                alert("ไม่สามารถเชื่อมต่อกับเซิร์ฟเวอร์ได้");
            } finally {
                btn.disabled = false;
                btn.textContent = "เข้าสู่ระบบ";
            }
        });
    }
});