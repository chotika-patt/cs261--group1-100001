package tu_store.demo.services;

import org.springframework.stereotype.Service;
import tu_store.demo.models.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    // DB แบบ ยังไม่ใส่ sql เอาจริงต้องเชื่อมและใส่ใน repositories
    private final Map<String, User> users = new HashMap<>();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public boolean login(User user) {
        return users.containsKey(user.getUsername()) &&
                users.get(user.getUsername()).equals(user.getPassword());
    }

    public String register(String username, String email, String password, String passwordConfirm, String role) {
        username = username.trim().toLowerCase();
        email = email.trim();

        if (users.containsKey(username)){
            return "Username has already been used.";
        }

        if (!password.equals(passwordConfirm)) {
            return "Passwords do not match.";
        }

        if (password.length() < 6){
            return "Password must be at least 6 characters.";
        }

        if (!email.contains("@")) {
            return "Invalid email address.";
        }

        String hashedPassword = passwordEncoder.encode(password);

        User newUser = new User(username, email, hashedPassword, role);
        users.put(username, newUser);

        return role + " registered successfully.";
    }
}
