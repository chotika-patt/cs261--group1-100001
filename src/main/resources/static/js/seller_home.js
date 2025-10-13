// ฟังก์ชันนี้จะทำงานทันทีเมื่อหน้าเว็บโหลดเสร็จ
document.addEventListener('DOMContentLoaded', function() {
    
    // ยิง request ไปที่ API เพื่อขอข้อมูล session ของผู้ใช้
    fetch('/api/user/session')
        .then(response => {
            if (response.status === 401) { // ถ้ายังไม่ login
                window.location.href = '/login.html'; // ให้เด้งไปหน้า login
                return;
            }
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json(); // แปลงข้อมูลที่ได้เป็น JSON
        })
        .then(user => {
            // ถ้าได้ข้อมูล user มา (และ user ไม่ใช่ null)
            if (user && user.username) {
                // ตรวจสอบว่าเป็น SELLER หรือไม่
                if (user.role !== 'SELLER') {
                    alert('คุณไม่มีสิทธิ์เข้าถึงหน้านี้');
                    window.location.href = '/index.html'; // หรือหน้าหลักของผู้ซื้อ
                    return;
                }
                
                // นำข้อมูลที่ได้ไปใส่ใน HTML ตาม id ที่เรากำหนดไว้
                document.getElementById('username-text').textContent = user.username;
                document.getElementById('dropdown-username').textContent = user.username;
                document.getElementById('dropdown-email').textContent = user.email;
                document.getElementById('dropdown-phone').textContent = user.phone;
                document.getElementById('welcome-header').textContent = `ยินดีต้อนรับ, ${user.username}!`;
            }
        })
        .catch(error => {
            console.error('Error fetching user data:', error);
            // ถ้ามีปัญหาในการดึงข้อมูล ก็ให้กลับไปหน้า login
            window.location.href = '/login.html';
        });

    // จัดการปุ่ม Logout
    const confirmLogoutBtn = document.getElementById('confirm-logout');
    if(confirmLogoutBtn) {
        confirmLogoutBtn.addEventListener('click', () => {
            fetch('/api/logout', { method: 'POST' }) // ส่งคำขอ logout
                .then(() => {
                    alert('ออกจากระบบสำเร็จแล้ว');
                    window.location.href = '/index.html'; // กลับไปหน้าแรก
                });
        });
    }
});

