package tu_store.demo;

import tu_store.demo.models.User;
import tu_store.demo.models.UserRole;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Test Data / Mock Stub สำหรับ Unit Tests
 * ใช้สำหรับสร้าง test data ที่ใช้ซ้ำๆ
 */
public class UserTestData {

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ====== Valid Users ======

    /**
     * สร้าง valid buyer user
     */
    public static User createValidBuyerUser() {
        return new User(
                "testbuyer",
                "buyer@email.com",
                passwordEncoder.encode("password123"),
                "0812345678",
                null,
                null,
                UserRole.CLIENT,
                null
        );
    }

    /**
     * สร้าง valid seller user
     */
    public static User createValidSellerUser() {
        return new User(
                "testseller",
                "seller@email.com",
                passwordEncoder.encode("password123"),
                "0812345678",
                "5912345678",
                null,
                UserRole.SELLER,
                false
        );
    }

    // ====== Invalid Users ======

    /**
     * สร้าง user ที่มี username ว่าง
     */
    public static User createUserWithEmptyUsername() {
        User user = new User();
        user.setUsername("");
        user.setPassword("password123");
        user.setEmail("test@email.com");
        user.setPhone("0812345678");
        user.setRole(UserRole.CLIENT);
        return user;
    }

    /**
     * สร้าง user ที่มี password ว่าง
     */
    public static User createUserWithEmptyPassword() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("");
        user.setEmail("test@email.com");
        user.setPhone("0812345678");
        user.setRole(UserRole.CLIENT);
        return user;
    }

    /**
     * สร้าง user ที่มี password สั้นกว่า 6 ตัว
     */
    public static User createUserWithShortPassword() {
        return new User(
                "testuser",
                "test@email.com",
                "123",  // สั้นกว่า 6 ตัว
                "0812345678",
                null,
                null,
                UserRole.CLIENT,
                null
        );
    }

    /**
     * สร้าง user ที่มี email ไม่ถูกต้อง (ไม่มี @)
     */
    public static User createUserWithInvalidEmail() {
        return new User(
                "testuser",
                "invalidemail",  // ไม่มี @
                "password123",
                "0812345678",
                null,
                null,
                UserRole.CLIENT,
                null
        );
    }

    /**
     * สร้าง user ที่มี phone number ไม่ถูกต้อง (ไม่ 10 ตัว)
     */
    public static User createUserWithInvalidPhone() {
        return new User(
                "testuser",
                "test@email.com",
                "password123",
                "123",  // ไม่ 10 ตัว
                null,
                null,
                UserRole.CLIENT,
                null
        );
    }

    /**
     * สร้าง seller ที่ไม่มี student ID
     */
    public static User createSellerWithoutStudentID() {
        return new User(
                "testseller",
                "seller@email.com",
                "password123",
                "0812345678",
                null,  // ไม่มี student ID
                null,
                UserRole.SELLER,
                false
        );
    }

    /**
     * สร้าง seller ที่มี student ID ไม่ถูกต้อง (ไม่ 10 ตัว)
     */
    public static User createSellerWithInvalidStudentID() {
        return new User(
                "testseller",
                "seller@email.com",
                "password123",
                "0812345678",
                "123",  // ไม่ 10 ตัว
                null,
                UserRole.SELLER,
                false
        );
    }

    // ====== Custom Users ======

    /**
     * สร้าง user ที่มี username และ hashed password ตามที่ต้องการ
     */
    public static User createUserWithHashedPassword(String username, String plainPassword) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(plainPassword));
        user.setEmail(username + "@email.com");
        user.setPhone("0812345678");
        user.setRole(UserRole.CLIENT);
        return user;
    }

    /**
     * สร้าง user ที่มี username และ role ที่กำหนด
     */
    public static User createUserWithRole(String username, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setEmail(username + "@email.com");
        user.setPhone("0812345678");
        user.setRole(role);
        return user;
    }

    /**
     * สร้าง user ข้อมูลว่าง
     */
    public static User createEmptyUser() {
        return new User();
    }

    // ====== Helper Methods ======

    /**
     * ตรวจสอบว่า user ใช้ได้สำหรับ registration ไหม
     */
    public static boolean isValidForRegistration(User user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) return false;
        if (user.getPassword() == null || user.getPassword().length() < 6) return false;
        if (user.getEmail() == null || !user.getEmail().contains("@")) return false;
        if (user.getPhone() == null || user.getPhone().length() != 10) return false;
        if (user.getRole() == null) return false;

        return true;
    }
}