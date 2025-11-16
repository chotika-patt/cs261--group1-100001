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

        // inject mock repository
        java.lang.reflect.Field field = UserService.class.getDeclaredField("userRepository");
        field.setAccessible(true);
        field.set(userService, userRepository);

        passwordEncoder = new BCryptPasswordEncoder();
    }

    // ====== TEST 1: LOGIN input validation ======

    @Test
    public void testLogin_WithEmptyUsername_ShouldReturnFalse() {
        User user = new User();
        user.setUsername("");
        user.setPassword("password123");

        when(userRepository.findFirstByUsername("")).thenReturn(null);

        boolean result = userService.login(user);
        assertFalse(result);
    }

    @Test
    public void testLogin_WithEmptyPassword_ShouldReturnFalse() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("");

        User dbUser = new User(
            "testuser", "test@email.com", "hashedPassword",
            "0812345678", null, null, UserRole.CLIENT, null,
            null   // organizationType
        );

        when(userRepository.findFirstByUsername("testuser")).thenReturn(dbUser);

        boolean result = userService.login(user);
        assertFalse(result);
    }

    // ====== TEST 2: verify credentials via DB ======

    @Test
    public void testLogin_WithCorrectCredentials_ShouldReturnTrue() {
        String username = "testuser";
        String password = "password123";

        String hashedPassword = passwordEncoder.encode(password);

        User dbUser = new User(
            username, "test@email.com", hashedPassword,
            "0812345678", null, null, UserRole.CLIENT, null,
            null
        );

        User inputUser = new User();
        inputUser.setUsername(username);
        inputUser.setPassword(password);

        when(userRepository.findFirstByUsername(username)).thenReturn(dbUser);

        boolean result = userService.login(inputUser);

        assertTrue(result);
        verify(userRepository).findFirstByUsername(username);
    }

    @Test
    public void testLogin_WithIncorrectPassword_ShouldReturnFalse() {
        String username = "testuser";
        String correctPassword = "password123";
        String wrongPassword = "wrongpassword";

        String hashedPassword = passwordEncoder.encode(correctPassword);

        User dbUser = new User(
            username, "email@mail.com", hashedPassword,
            "0812345678", null, null, UserRole.CLIENT, null,
            null
        );

        User inputUser = new User();
        inputUser.setUsername(username);
        inputUser.setPassword(wrongPassword);

        when(userRepository.findFirstByUsername(username)).thenReturn(dbUser);

        boolean result = userService.login(inputUser);
        assertFalse(result);
    }

    @Test
    public void testLogin_WithNonexistentUser_ShouldReturnFalse() {
        User inputUser = new User();
        inputUser.setUsername("nonexistent");
        inputUser.setPassword("password123");

        when(userRepository.findFirstByUsername("nonexistent")).thenReturn(null);

        boolean result = userService.login(inputUser);
        assertFalse(result);
    }


    // ====== TEST 3: REGISTER validation ======

    @Test
    public void testRegister_WithValidBuyerCredentials_ShouldReturnSuccess() {
        User user = new User(
            "newuser", "newuser@email.com", "password123",
            "0812345678", null, null, UserRole.CLIENT, null,
            null   // organizationType
        );

        when(userRepository.findFirstByUsernameIgnoreCase("newuser")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(user);

        String result = userService.register(user);

        assertTrue(result.contains("successfully"));
        verify(userRepository).findFirstByUsernameIgnoreCase("newuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testRegister_WithDuplicateUsername_ShouldReturnError() {
        User existingUser = new User(
            "existinguser", "existing@mail.com", "hashedPassword",
            "0812345678", null, null, UserRole.CLIENT, null,
            null
        );

        User newUser = new User(
            "existinguser", "new@mail.com", "password123",
            "0812345678", null, null, UserRole.CLIENT, null,
            null
        );

        when(userRepository.findFirstByUsernameIgnoreCase("existinguser"))
                .thenReturn(existingUser);

        String result = userService.register(newUser);

        assertEquals("Username has already been used.", result);
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testRegister_WithShortPassword_ShouldReturnError() {
        User user = new User(
            "newuser", "new@mail.com", "123",
            "0812345678", null, null, UserRole.CLIENT, null,
            null
        );

        when(userRepository.findFirstByUsernameIgnoreCase("newuser"))
                .thenReturn(null);

        String result = userService.register(user);

        assertEquals("Password must be at least 6 characters.", result);
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testRegister_WithInvalidEmail_ShouldReturnError() {
        User user = new User(
            "newuser", "invalidemail", "password123",
            "0812345678", null, null, UserRole.CLIENT, null,
            null
        );

        when(userRepository.findFirstByUsernameIgnoreCase("newuser"))
                .thenReturn(null);

        String result = userService.register(user);

        assertEquals("Invalid email address.", result);
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testRegister_WithInvalidPhone_ShouldReturnError() {
        User user = new User(
            "newuser", "new@mail.com", "password123",
            "123", null, null, UserRole.CLIENT, null,
            null
        );

        when(userRepository.findFirstByUsernameIgnoreCase("newuser"))
                .thenReturn(null);

        String result = userService.register(user);

        assertEquals("Invalid phone number.", result);
        verify(userRepository, never()).save(any());
    }
}
