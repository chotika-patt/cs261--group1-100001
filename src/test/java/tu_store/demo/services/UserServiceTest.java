package tu_store.demo.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import tu_store.demo.models.User;
import tu_store.demo.models.UserRole;
import tu_store.demo.repositories.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        userService = new UserService();

        // ใช้ Reflection เพื่อ inject mock repository
        java.lang.reflect.Field field = UserService.class.getDeclaredField("userRepository");
        field.setAccessible(true);
        field.set(userService, userRepository);

        passwordEncoder = new BCryptPasswordEncoder();
    }

    // ====== TEST 1: validateInput() - ตรวจสอบ username/password ว่างไหม ======

    @Test
    public void testLogin_WithEmptyUsername_ShouldReturnFalse() {
        // Arrange (จัดเตรียมข้อมูล)
        User user = new User();
        user.setUsername("");
        user.setPassword("password123");

        when(userRepository.findFirstByUsername("")).thenReturn(null);

        // Act (ทำการทดสอบ)
        boolean result = userService.login(user);

        // Assert (ตรวจสอบผลลัพธ์)
        assertFalse(result, "Login should return false when username is empty");
    }

    @Test
    public void testLogin_WithEmptyPassword_ShouldReturnFalse() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("");

        User dbUser = new User("testuser", "test@email.com", "hashedPassword", "0812345678",
                null, null, UserRole.CLIENT, null);
        when(userRepository.findFirstByUsername("testuser")).thenReturn(dbUser);

        // Act
        boolean result = userService.login(user);

        // Assert
        assertFalse(result, "Login should return false when password is empty");
    }

    // ====== TEST 2: verifyCredentials() - ตรวจสอบ username/password จาก DB ======

    @Test
    public void testLogin_WithCorrectCredentials_ShouldReturnTrue() {
        // Arrange
        String username = "testuser";
        String password = "password123";

        // สร้าง password ที่ hash แล้ว (เหมือนใน database)
        String hashedPassword = passwordEncoder.encode(password);

        User dbUser = new User();
        dbUser.setUsername(username);
        dbUser.setPassword(hashedPassword);
        dbUser.setEmail("test@email.com");

        User inputUser = new User();
        inputUser.setUsername(username);
        inputUser.setPassword(password);

        // Mock repository ให้ return user จาก database
        when(userRepository.findFirstByUsername(username)).thenReturn(dbUser);

        // Act
        boolean result = userService.login(inputUser);

        // Assert
        assertTrue(result, "Login should return true with correct credentials");
        verify(userRepository).findFirstByUsername(username); // ตรวจสอบว่า repository ถูก call
    }

    @Test
    public void testLogin_WithIncorrectPassword_ShouldReturnFalse() {
        // Arrange
        String username = "testuser";
        String correctPassword = "password123";
        String wrongPassword = "wrongpassword";

        String hashedPassword = passwordEncoder.encode(correctPassword);
        User dbUser = new User();
        dbUser.setUsername(username);
        dbUser.setPassword(hashedPassword);

        User inputUser = new User();
        inputUser.setUsername(username);
        inputUser.setPassword(wrongPassword);

        when(userRepository.findFirstByUsername(username)).thenReturn(dbUser);

        // Act
        boolean result = userService.login(inputUser);

        // Assert
        assertFalse(result, "Login should return false with incorrect password");
    }

    @Test
    public void testLogin_WithNonexistentUser_ShouldReturnFalse() {
        // Arrange
        String username = "nonexistentuser";
        String password = "password123";

        User inputUser = new User();
        inputUser.setUsername(username);
        inputUser.setPassword(password);

        // Mock repository ให้ return null (user ไม่มี)
        when(userRepository.findFirstByUsername(username)).thenReturn(null);

        // Act
        boolean result = userService.login(inputUser);

        // Assert
        assertFalse(result, "Login should return false when user does not exist");
    }

    // ====== TEST 3: hashPassword() - ตรวจสอบ password ถูก hash ======

    @Test
    public void testRegister_WithValidBuyerCredentials_ShouldReturnSuccess() {
        // Arrange
        User user = new User("newuser", "newuser@email.com", "password123", "0812345678",
                null, null, UserRole.CLIENT, null);

        when(userRepository.findFirstByUsernameIgnoreCase("newuser")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        String result = userService.register(user);

        // Assert
        assertTrue(result.contains("successfully"), "Register should return success message");
        verify(userRepository).findFirstByUsernameIgnoreCase("newuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testRegister_WithDuplicateUsername_ShouldReturnError() {
        // Arrange
        User existingUser = new User("existinguser", "existing@email.com", "hashedPassword",
                "0812345678", null, null, UserRole.CLIENT, null);
        User newUser = new User("existinguser", "newuser@email.com", "password123",
                "0812345678", null, null, UserRole.CLIENT, null);

        // Mock repository ให้ return user ที่มีอยู่แล้ว
        when(userRepository.findFirstByUsernameIgnoreCase("existinguser")).thenReturn(existingUser);

        // Act
        String result = userService.register(newUser);

        // Assert
        assertEquals("Username has already been used.", result,
                "Should return error message for duplicate username");
        verify(userRepository, never()).save(any(User.class)); // ไม่ควร save
    }

    @Test
    public void testRegister_WithShortPassword_ShouldReturnError() {
        // Arrange
        User user = new User("newuser", "newuser@email.com", "123", "0812345678",
                null, null, UserRole.CLIENT, null);

        when(userRepository.findFirstByUsernameIgnoreCase("newuser")).thenReturn(null);

        // Act
        String result = userService.register(user);

        // Assert
        assertEquals("Password must be at least 6 characters.", result,
                "Should return error for password < 6 characters");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testRegister_WithInvalidEmail_ShouldReturnError() {
        // Arrange
        User user = new User("newuser", "invalidemail", "password123", "0812345678",
                null, null, UserRole.CLIENT, null);

        when(userRepository.findFirstByUsernameIgnoreCase("newuser")).thenReturn(null);

        // Act
        String result = userService.register(user);

        // Assert
        assertEquals("Invalid email address.", result,
                "Should return error for email without @");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testRegister_WithInvalidPhone_ShouldReturnError() {
        // Arrange
        User user = new User("newuser", "newuser@email.com", "password123", "123",
                null, null, UserRole.CLIENT, null);

        when(userRepository.findFirstByUsernameIgnoreCase("newuser")).thenReturn(null);

        // Act
        String result = userService.register(user);

        // Assert
        assertEquals("Invalid phone number.", result,
                "Should return error for phone != 10 digits");
        verify(userRepository, never()).save(any(User.class));
    }
}