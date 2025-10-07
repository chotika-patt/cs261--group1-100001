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
        if(!users.containsKey(user.getUsername())){
            return false;
        }
        User storedUser = users.get(user.getUsername());
        return passwordEncoder.matches(user.getPassword(), storedUser.getPassword());
    }
    public String register(User user) {
        String username = user.getUsername().trim().toLowerCase();
        String email = user.getEmail().trim();
        if (users.containsKey(username)){
            return "Username has already been used.";
        }

        if (user.getPassword().length() < 6){
            return "Password must be at least 6 characters.";
        }

        if (!user.getEmail().contains("@")) {
            return "Invalid email address.";
        }
        String hashedPassword = passwordEncoder.encode(user.getPassword());

        // ใส่ค่า null ให้ฟิลด์เฉพาะ seller/client ที่ยังไม่มีข้อมูล
        User newUser = new User(username, email, hashedPassword, null, null, null, user.getRole());
        users.put(username, newUser);

        return user.getRole() + " registered successfully.";
    }

}