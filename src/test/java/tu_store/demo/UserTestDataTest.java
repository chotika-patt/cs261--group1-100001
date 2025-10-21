package tu_store.demo;

import org.junit.jupiter.api.Test;
import tu_store.demo.models.User;
import tu_store.demo.models.UserRole;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test สำหรับ UserTestData
 * ตรวจสอบว่า mock data ถูกต้องหรือไม่
 */
public class UserTestDataTest {

    // ====== Test Valid Users ======

    @Test
    public void testCreateValidBuyerUser_ShouldReturnValidUser() {
        // Act
        User user = UserTestData.createValidBuyerUser();

        // Assert
        assertNotNull(user, "User should not be null");
        assertEquals("testbuyer", user.getUsername());
        assertEquals("buyer@email.com", user.getEmail());
        assertEquals("0812345678", user.getPhone());
        assertEquals(UserRole.CLIENT, user.getRole());
        assertNotNull(user.getPassword(), "Password should be hashed");
        assertTrue(user.getPassword().length() > 0, "Hashed password should not be empty");
    }

    @Test
    public void testCreateValidSellerUser_ShouldReturnValidUser() {
        // Act
        User user = UserTestData.createValidSellerUser();

        // Assert
        assertNotNull(user, "User should not be null");
        assertEquals("testseller", user.getUsername());
        assertEquals("seller@email.com", user.getEmail());
        assertEquals("0812345678", user.getPhone());
        assertEquals("5912345678", user.getStudentID());
        assertEquals(UserRole.SELLER, user.getRole());
        assertFalse(user.getVerified(), "Seller should not be verified initially");
    }

    // ====== Test Invalid Users ======

    @Test
    public void testCreateUserWithEmptyUsername_ShouldHaveEmptyUsername() {
        // Act
        User user = UserTestData.createUserWithEmptyUsername();

        // Assert
        assertTrue(user.getUsername().isEmpty(), "Username should be empty");
        assertEquals("test@email.com", user.getEmail());
    }

    @Test
    public void testCreateUserWithEmptyPassword_ShouldHaveEmptyPassword() {
        // Act
        User user = UserTestData.createUserWithEmptyPassword();

        // Assert
        assertEquals("testuser", user.getUsername());
        assertTrue(user.getPassword().isEmpty(), "Password should be empty");
    }

    @Test
    public void testCreateUserWithShortPassword_ShouldHavePasswordLessThan6() {
        // Act
        User user = UserTestData.createUserWithShortPassword();

        // Assert
        assertEquals("testuser", user.getUsername());
        assertEquals("123", user.getPassword());
        assertTrue(user.getPassword().length() < 6, "Password should be less than 6 characters");
    }

    @Test
    public void testCreateUserWithInvalidEmail_ShouldNotContainAt() {
        // Act
        User user = UserTestData.createUserWithInvalidEmail();

        // Assert
        assertFalse(user.getEmail().contains("@"), "Email should not contain @");
        assertEquals("invalidemail", user.getEmail());
    }

    @Test
    public void testCreateUserWithInvalidPhone_ShouldNotBe10Digits() {
        // Act
        User user = UserTestData.createUserWithInvalidPhone();

        // Assert
        assertNotEquals(10, user.getPhone().length(), "Phone should not be 10 digits");
        assertEquals("123", user.getPhone());
    }

    @Test
    public void testCreateSellerWithoutStudentID_ShouldHaveNullStudentID() {
        // Act
        User user = UserTestData.createSellerWithoutStudentID();

        // Assert
        assertNull(user.getStudentID(), "Student ID should be null");
        assertEquals(UserRole.SELLER, user.getRole());
    }

    @Test
    public void testCreateSellerWithInvalidStudentID_ShouldNotBe10Digits() {
        // Act
        User user = UserTestData.createSellerWithInvalidStudentID();

        // Assert
        assertNotEquals(10, user.getStudentID().length(), "Student ID should not be 10 digits");
        assertEquals("123", user.getStudentID());
    }

    // ====== Test Custom Users ======

    @Test
    public void testCreateUserWithHashedPassword_ShouldHaveHashedPassword() {
        // Act
        User user = UserTestData.createUserWithHashedPassword("customuser", "mypassword");

        // Assert
        assertEquals("customuser", user.getUsername());
        assertNotEquals("mypassword", user.getPassword(), "Password should be hashed, not plain text");
        assertNotNull(user.getPassword(), "Hashed password should not be null");
        assertTrue(user.getPassword().length() > 10, "Hashed password should be long");
    }

    @Test
    public void testCreateUserWithRole_ShouldHaveCorrectRole() {
        // Act
        User adminUser = UserTestData.createUserWithRole("admin", UserRole.CLIENT);
        User sellerUser = UserTestData.createUserWithRole("seller", UserRole.SELLER);

        // Assert
        assertEquals(UserRole.CLIENT, adminUser.getRole());
        assertEquals(UserRole.SELLER, sellerUser.getRole());
    }

    @Test
    public void testCreateEmptyUser_ShouldHaveNullFields() {
        // Act
        User user = UserTestData.createEmptyUser();

        // Assert
        assertNull(user.getUsername());
        assertNull(user.getPassword());
        assertNull(user.getEmail());
    }

    // ====== Test Helper Methods ======

    @Test
    public void testIsValidForRegistration_WithValidUser_ShouldReturnTrue() {
        // Arrange
        User user = UserTestData.createValidBuyerUser();

        // Act
        boolean result = UserTestData.isValidForRegistration(user);

        // Assert
        assertTrue(result, "Valid buyer user should pass validation");
    }

    @Test
    public void testIsValidForRegistration_WithEmptyUsername_ShouldReturnFalse() {
        // Arrange
        User user = UserTestData.createUserWithEmptyUsername();

        // Act
        boolean result = UserTestData.isValidForRegistration(user);

        // Assert
        assertFalse(result, "User with empty username should fail validation");
    }

    @Test
    public void testIsValidForRegistration_WithShortPassword_ShouldReturnFalse() {
        // Arrange
        User user = UserTestData.createUserWithShortPassword();

        // Act
        boolean result = UserTestData.isValidForRegistration(user);

        // Assert
        assertFalse(result, "User with short password should fail validation");
    }

    @Test
    public void testIsValidForRegistration_WithInvalidEmail_ShouldReturnFalse() {
        // Arrange
        User user = UserTestData.createUserWithInvalidEmail();

        // Act
        boolean result = UserTestData.isValidForRegistration(user);

        // Assert
        assertFalse(result, "User with invalid email should fail validation");
    }

    @Test
    public void testIsValidForRegistration_WithInvalidPhone_ShouldReturnFalse() {
        // Arrange
        User user = UserTestData.createUserWithInvalidPhone();

        // Act
        boolean result = UserTestData.isValidForRegistration(user);

        // Assert
        assertFalse(result, "User with invalid phone should fail validation");
    }
}
