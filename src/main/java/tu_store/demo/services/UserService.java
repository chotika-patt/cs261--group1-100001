package tu_store.demo.services;

import org.springframework.stereotype.Service;

import tu_store.demo.models.User;
import tu_store.demo.models.UserRole;
import tu_store.demo.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

}
