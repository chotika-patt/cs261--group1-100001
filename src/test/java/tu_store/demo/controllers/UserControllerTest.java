package tu_store.demo.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tu_store.demo.models.User;
import tu_store.demo.models.UserRole;
import tu_store.demo.services.UserService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    @Mock
    private UserService userService;

    private UserController userController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        userController = new UserController(userService);
    }

    // ====== TEST 1: ตรวจสอบ input ว่างไหม ======
    @Test
    public void testLogin_WithEmptyUsername_ShouldReturnBadRequest() {
        User user = new User();
        user.setUsername("");
        user.setPassword("password123");

        // ตรวจสอบว่า username ว่าง
        assertTrue(user.getUsername().isBlank(), "Username should be blank");
    }

    @Test
    public void testLogin_WithEmptyPassword_ShouldReturnBadRequest() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("");

        // ตรวจสอบว่า password ว่าง
        assertTrue(user.getPassword().isBlank(), "Password should be blank");
    }

    @Test
    public void testLogin_WithBothEmpty_ShouldReturnBadRequest() {
        User user = new User();
        user.setUsername("");
        user.setPassword("");

        assertTrue(user.getUsername().isBlank() && user.getPassword().isBlank(),
                "Both should be blank");
    }

    // ====== TEST 2: ตรวจสอบ verifyCredentials ด้วย mock service ======
    @Test
    public void testLogin_WithValidCredentials_ShouldReturnSuccess() {
        // Arrange
        User inputUser = new User();
        inputUser.setUsername("testuser");
        inputUser.setPassword("password123");

        User dbUser = new User("testuser", "test@email.com", "hashedPassword",
                "0812345678", null, null, UserRole.CLIENT, null);

        // Mock service ให้ return true (login สำเร็จ)
        when(userService.login(inputUser)).thenReturn(true);
        when(userService.findByUsername("testuser")).thenReturn(dbUser);

        // Act
        boolean loginResult = userService.login(inputUser);
        User foundUser = userService.findByUsername("testuser");

        // Assert
        assertTrue(loginResult, "Login should return true");
        assertNotNull(foundUser, "User should be found");
        assertEquals("testuser", foundUser.getUsername());
        assertEquals(UserRole.CLIENT, foundUser.getRole());

        verify(userService).login(inputUser);
        verify(userService).findByUsername("testuser");
    }

    @Test
    public void testLogin_WithInvalidCredentials_ShouldReturnUnauthorized() {
        // Arrange
        User inputUser = new User();
        inputUser.setUsername("testuser");
        inputUser.setPassword("wrongpassword");

        // Mock service ให้ return false (login ไม่สำเร็จ)
        when(userService.login(inputUser)).thenReturn(false);

        // Act
        boolean result = userService.login(inputUser);

        // Assert
        assertFalse(result, "Login should return false with invalid credentials");
        verify(userService).login(inputUser);
    }

    @Test
    public void testLogin_WithNonexistentUser_ShouldReturnUnauthorized() {
        // Arrange
        User inputUser = new User();
        inputUser.setUsername("nonexistentuser");
        inputUser.setPassword("password123");

        when(userService.login(inputUser)).thenReturn(false);
        when(userService.findByUsername("nonexistentuser")).thenReturn(null);

        // Act
        boolean loginResult = userService.login(inputUser);
        User foundUser = userService.findByUsername("nonexistentuser");

        // Assert
        assertFalse(loginResult, "Login should fail for nonexistent user");
        assertNull(foundUser, "User should not be found");
    }

    // ====== TEST 3: Register Buyer ======
    @Test
    public void testRegisterBuyer_WithValidData_ShouldSucceed() {
        // Arrange
        User user = new User("newbuyer", "buyer@email.com", "password123",
                "0812345678", null, null, UserRole.CLIENT, null);

        when(userService.register(any(User.class))).thenReturn("CLIENT registered successfully.");

        // Act
        String result = userService.register(user);

        // Assert
        assertTrue(result.contains("successfully"), "Should return success message");
        assertEquals(UserRole.CLIENT, user.getRole(), "Role should be CLIENT");
        verify(userService).register(any(User.class));
    }

    @Test
    public void testRegisterBuyer_WithDuplicateUsername_ShouldFail() {
        // Arrange
        User user = new User("existinguser", "buyer@email.com", "password123",
                "0812345678", null, null, UserRole.CLIENT, null);

        when(userService.register(any(User.class))).thenReturn("Username has already been used.");

        // Act
        String result = userService.register(user);

        // Assert
        assertEquals("Username has already been used.", result);
    }

    // ====== TEST 4: Register Seller ======
    @Test
    public void testRegisterSeller_WithValidData_ShouldSucceed() {
        // Arrange
        User user = new User("newseller", "seller@email.com", "password123",
                "0812345678", "5912345678", null, UserRole.SELLER, false);

        when(userService.registerReturnUser(any(User.class))).thenReturn(user);

        // Act
        User result = userService.registerReturnUser(user);

        // Assert
        assertNotNull(result, "User should not be null");
        assertEquals(UserRole.SELLER, result.getRole(), "Role should be SELLER");
        assertEquals("5912345678", result.getStudentID());
    }

    @Test
    public void testRegisterSeller_WithoutStudentID_ShouldFail() {
        // Arrange
        User user = new User("newseller", "seller@email.com", "password123",
                "0812345678", null, null, UserRole.SELLER, false);

        when(userService.registerReturnUser(any(User.class)))
                .thenThrow(new RuntimeException("Invalid student ID."));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.registerReturnUser(user);
        });
    }

    @Test
    public void testRegisterSeller_WithInvalidStudentID_ShouldFail() {
        // Arrange
        User user = new User("newseller", "seller@email.com", "password123",
                "0812345678", "123", null, UserRole.SELLER, false);

        when(userService.registerReturnUser(any(User.class)))
                .thenThrow(new RuntimeException("Invalid student ID."));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            userService.registerReturnUser(user);
        });
    }
}
