package tu_store.demo.services;

import org.springframework.stereotype.Service;

import tu_store.demo.models.User;
import tu_store.demo.models.UserRole;
import tu_store.demo.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.servlet.http.HttpSession;

// import java.util.HashMap;
// import java.util.Map;

@Service
public class UserService {
    // DB แบบ ยังไม่ใส่ sql เอาจริงต้องเชื่อมและใส่ใน repositories
    // private final Map<String, User> users = new HashMap<>();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    

    @Autowired
    private UserRepository userRepository;

    public boolean login(User user) {
        User dbUser = userRepository.findFirstByUsername(user.getUsername());

        if(dbUser == null) return false;

        return passwordEncoder.matches(user.getPassword(), dbUser.getPassword());
    }

    public String register(User httpUser) {
        String username = httpUser.getUsername().trim();
        String email = httpUser.getEmail().trim();
        String password = httpUser.getPassword();
        String phone = httpUser.getPhone();
        String studentID = httpUser.getStudentID();
        UserRole role = httpUser.getRole();

        User user = userRepository.findFirstByUsernameIgnoreCase(username);

        if (user != null){
            return "Username has already been used.";
        }

        if (password.length() < 6){
            return "Password must be at least 6 characters.";
        }

        if (!email.contains("@")) {
            return "Invalid email address.";
        }

        if (phone.length() != 10){
            return "Invalid phone number.";
        }
        
        String hashedPassword = passwordEncoder.encode(password);

        if(role == UserRole.CLIENT){
            User newUser = new User(username, email, hashedPassword, phone, null, null, role);
            userRepository.save(newUser);
        }
        else if(role == UserRole.SELLER){
            if (studentID.length() != 10){
                return "Invalid student ID.";
            }
            User newUser = new User(username, email, hashedPassword, phone, studentID, httpUser.getVerify_document(), role);
            userRepository.save(newUser);
        }
        else{
            return "Invalid role.";
        }

        return role + " registered successfully.";
    }
    public User findByUsername(String username) {
        return userRepository.findFirstByUsername(username);
    }

    public User getUserBySession(HttpSession session){
        String username = (String) session.getAttribute("username");
        if(username == null) return null;

        User user = findByUsername(username);
        if(user == null) return null;

        return user;
    }

    public Long getUserIdBySession(HttpSession session){
        User user = getUserBySession(session);
        
        if(user == null) return null;

        return user.getUser_id();
    }

    public User registerReturnUser(User httpUser) {
        String username = httpUser.getUsername().trim();
        String email = httpUser.getEmail().trim();
        String password = httpUser.getPassword();
        String phone = httpUser.getPhone();
        String studentID = httpUser.getStudentID();
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
            newUser = new User(username, email, hashedPassword, phone, null, null, role);
        } else if (role == UserRole.SELLER) {
            if (studentID == null || studentID.length() != 10) {
                throw new RuntimeException("Invalid student ID.");
            }
            newUser = new User(username, email, hashedPassword, phone, studentID, null, role);
        } else {
            throw new RuntimeException("Invalid role.");
        }
        return userRepository.save(newUser); // คืนค่า User ที่บันทึกแล้ว (มี user_id)
    }




}
