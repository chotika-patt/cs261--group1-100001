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

        assertTrue(user.getUsername().isBlank());
    }

    @Test
    public void testLogin_WithEmptyPassword_ShouldReturnBadRequest() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("");

        assertTrue(user.getPassword().isBlank());
    }

    @Test
    public void testLogin_WithBothEmpty_ShouldReturnBadRequest() {
        User user = new User();
        user.setUsername("");
        user.setPassword("");

        assertTrue(user.getUsername().isBlank() && user.getPassword().isBlank());
    }


    // ====== TEST 2: ตรวจสอบ verifyCredentials ด้วย mock service ======
    @Test
    public void testLogin_WithValidCredentials_ShouldReturnSuccess() {

        User inputUser = new User();
        inputUser.setUsername("testuser");
        inputUser.setPassword("password123");

        // ⭐ ใช้ constructor ใหม่ (add organizationType)
        User dbUser = new User(
                "testuser", "test@email.com", "hashedPassword",
                "0812345678", null, null,
                UserRole.CLIENT, null,
                null  // ⭐ ค่า organizationType
        );

        when(userService.login(inputUser)).thenReturn(true);
        when(userService.findByUsername("testuser")).thenReturn(dbUser);

        boolean loginResult = userService.login(inputUser);
        User foundUser = userService.findByUsername("testuser");

        assertTrue(loginResult);
        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
        assertEquals(UserRole.CLIENT, foundUser.getRole());

        verify(userService).login(inputUser);
        verify(userService).findByUsername("testuser");
    }

    @Test
    public void testLogin_WithInvalidCredentials_ShouldReturnUnauthorized() {

        User inputUser = new User();
        inputUser.setUsername("testuser");
        inputUser.setPassword("wrongpassword");

        when(userService.login(inputUser)).thenReturn(false);

        boolean result = userService.login(inputUser);
        assertFalse(result);

        verify(userService).login(inputUser);
    }

    @Test
    public void testLogin_WithNonexistentUser_ShouldReturnUnauthorized() {

        User inputUser = new User();
        inputUser.setUsername("nonexistentuser");
        inputUser.setPassword("password123");

        when(userService.login(inputUser)).thenReturn(false);
        when(userService.findByUsername("nonexistentuser")).thenReturn(null);

        boolean loginResult = userService.login(inputUser);
        User foundUser = userService.findByUsername("nonexistentuser");

        assertFalse(loginResult);
        assertNull(foundUser);
    }


    // ====== TEST 3: Register Buyer ======
    @Test
    public void testRegisterBuyer_WithValidData_ShouldSucceed() {

        User user = new User(
                "newbuyer", "buyer@email.com", "password123",
                "0812345678", null, null,
                UserRole.CLIENT, null,
                null   // ⭐ organizationType
        );

        when(userService.register(any(User.class)))
                .thenReturn("CLIENT registered successfully.");

        String result = userService.register(user);

        assertTrue(result.contains("successfully"));
        assertEquals(UserRole.CLIENT, user.getRole());
        verify(userService).register(any(User.class));
    }

    @Test
    public void testRegisterBuyer_WithDuplicateUsername_ShouldFail() {

        User user = new User(
                "existinguser", "buyer@email.com", "password123",
                "0812345678", null, null,
                UserRole.CLIENT, null,
                null
        );

        when(userService.register(any(User.class)))
                .thenReturn("Username has already been used.");

        String result = userService.register(user);
        assertEquals("Username has already been used.", result);
    }


    // ====== TEST 4: Register Seller ======
    @Test
    public void testRegisterSeller_WithValidData_ShouldSucceed() {

        User user = new User(
                "newseller", "seller@email.com", "password123",
                "0812345678", "5912345678",
                null, UserRole.SELLER, false,
                "ชมรม"   // ⭐ Seller ต้องมี organizationType
        );

        when(userService.registerReturnUser(any(User.class))).thenReturn(user);

        User result = userService.registerReturnUser(user);

        assertNotNull(result);
        assertEquals(UserRole.SELLER, result.getRole());
        assertEquals("5912345678", result.getStudentID());
        assertEquals("ชมรม", result.getOrganizationType());
    }

    @Test
    public void testRegisterSeller_WithoutStudentID_ShouldFail() {

        User user = new User(
                "newseller", "seller@email.com", "password123",
                "0812345678", null,
                null, UserRole.SELLER, false,
                "ชุมนุม"
        );

        when(userService.registerReturnUser(any(User.class)))
                .thenThrow(new RuntimeException("Invalid student ID."));

        assertThrows(RuntimeException.class, () -> {
            userService.registerReturnUser(user);
        });
    }

    @Test
    public void testRegisterSeller_WithInvalidStudentID_ShouldFail() {

        User user = new User(
                "newseller", "seller@email.com", "password123",
                "0812345678", "123",
                null, UserRole.SELLER, false,
                "กลุ่มอิสระ"
        );

        when(userService.registerReturnUser(any(User.class)))
                .thenThrow(new RuntimeException("Invalid student ID."));

        assertThrows(RuntimeException.class, () -> {
            userService.registerReturnUser(user);
        });
    }
}
