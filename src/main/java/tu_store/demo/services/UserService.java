package tu_store.demo.services;

import org.springframework.stereotype.Service;

import tu_store.demo.models.User;
import tu_store.demo.models.UserRole;
import tu_store.demo.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.servlet.http.HttpSession;

@Service
public class UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TuApiService tuApiService;

    // ----------------------------------------------------
    // LOGIN
    // ----------------------------------------------------
    public boolean login(User user) {
        User dbUser = userRepository.findFirstByUsername(user.getUsername());
        if (dbUser == null) return false;

        return passwordEncoder.matches(user.getPassword(), dbUser.getPassword());
    }


    // ----------------------------------------------------
    // REGISTER (แบบเดิม, return String)
    // ----------------------------------------------------
    public String register(User httpUser) {

        String username = httpUser.getUsername().trim();
        String email = httpUser.getEmail().trim();
        String password = httpUser.getPassword();
        String phone = httpUser.getPhone();
        String studentID = httpUser.getStudentID();
        String organizationType = httpUser.getOrganizationType();   // ⭐ ดึงค่ามาใช้
        UserRole role = httpUser.getRole();

        User existingUser = userRepository.findFirstByUsernameIgnoreCase(username);
        if (existingUser != null) {
            return "Username has already been used.";
        }

        if (password.length() < 6) {
            return "Password must be at least 6 characters.";
        }

        if (!email.contains("@")) {
            return "Invalid email address.";
        }

        if (phone.length() != 10) {
            return "Invalid phone number.";
        }

        String hashedPassword = passwordEncoder.encode(password);

        if (role == UserRole.CLIENT) {

            User newUser = new User(
                username, email, hashedPassword, phone,
                null, null,
                role, null,
                organizationType   // ⭐ เพิ่มฟิลด์ใหม่
            );

            userRepository.save(newUser);
        }

        else if (role == UserRole.SELLER) {

            if (studentID.length() != 10) {
                return "Invalid student ID.";
            }

            User newUser = new User(
                username, email, hashedPassword, phone,
                studentID, httpUser.getVerify_document(),
                role, false,
                organizationType   // ⭐ เพิ่มฟิลด์ใหม่
            );

            userRepository.save(newUser);
        }

        else {
            return "Invalid role.";
        }

        return role + " registered successfully.";
    }



    // ----------------------------------------------------
    // FIND USER BY USERNAME
    // ----------------------------------------------------
    public User findByUsername(String username) {
        return userRepository.findFirstByUsername(username);
    }


    // ----------------------------------------------------
    // GET USER FROM SESSION
    // ----------------------------------------------------
    public User getUserBySession(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) return null;

        return findByUsername(username);
    }

    public Long getUserIdBySession(HttpSession session) {
        User user = getUserBySession(session);
        return (user == null) ? null : user.getUser_id();
    }



    // ----------------------------------------------------
    // REGISTER (เวอร์ชันคืนค่า User พร้อม user_id)
    // ----------------------------------------------------
    public User registerReturnUser(User httpUser) {

        String username = httpUser.getUsername().trim();
        String email = httpUser.getEmail().trim();
        String password = httpUser.getPassword();
        String phone = httpUser.getPhone();
        String studentID = httpUser.getStudentID();
        String organizationType = httpUser.getOrganizationType();   // ⭐ ฟิลด์ใหม่
        UserRole role = httpUser.getRole();

        User existingUser = userRepository.findFirstByUsernameIgnoreCase(username);
        if (existingUser != null) {
            throw new RuntimeException("Username has already been used.");
        }

        if (password.length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters.");
        }

        if (!email.contains("@")) {
            throw new RuntimeException("Invalid email address.");
        }

        if (phone.length() != 10) {
            throw new RuntimeException("Invalid phone number.");
        }

        String hashedPassword = new BCryptPasswordEncoder().encode(password);

        User newUser;

        if (role == UserRole.CLIENT) {

            newUser = new User(
                username, email, hashedPassword, phone,
                null, null,
                role, null,
                organizationType    // ⭐ เพิ่มฟิลด์ใหม่
            );
        }

        else if (role == UserRole.SELLER) {

            if (studentID == null || studentID.length() != 10) {
                throw new RuntimeException("Invalid student ID.");
            }

            boolean isValidStudent = tuApiService.checkStudentExists(studentID);
            if (!isValidStudent) {
                throw new RuntimeException("Student ID not valid according to TU records.");
            }

            newUser = new User(
                username, email, hashedPassword, phone,
                studentID, null,
                role, false,
                organizationType     // ⭐ เพิ่มฟิลด์ใหม่
            );
        }

        else {
            throw new RuntimeException("Invalid role.");
        }

        return userRepository.save(newUser);
    }



    // ----------------------------------------------------
    // ROLE CHECK HELPERS
    // ----------------------------------------------------
    public boolean isSellerById(Long id) {
        User user = userRepository.findFirstByUserId(id);
        return (user != null && user.getRole() == UserRole.SELLER);
    }

    public boolean isBuyerById(Long id) {
        User user = userRepository.findFirstByUserId(id);
        return (user != null && user.getRole() == UserRole.CLIENT);
    }

    public boolean isVerifiedSellerById(Long id) {
        User user = userRepository.findFirstByUserId(id);
        return (user != null && user.getRole() == UserRole.SELLER && user.getVerified() == true);
    }
}
