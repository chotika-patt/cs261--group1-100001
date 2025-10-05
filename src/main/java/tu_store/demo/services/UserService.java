package tu_store.demo.services;

import org.springframework.stereotype.Service;
import tu_store.demo.models.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    // DB แบบ ยังไม่ใส่ sql เอาจริงต้องเชื่อมและใส่ใน repositories
    private static final Map<String,String> users = new HashMap<>();
    private static BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public boolean register(User user){
        if(users.containsKey(user.getUsername())){
            return false;
        }
        users.put(user.getUsername(),user.getPassword());
        return true;
    }
    public boolean login(User user) {
        return users.containsKey(user.getUsername()) &&
                users.get(user.getUsername()).equals(user.getPassword());
    }

    public static String register(String username, String password) {
        if (users.containsKey(username)){
            return "Username has already been used.";
        }

        if (password.length() < 6){
            return "Password must be at least 6 characters.";
        }

        String hashedPassword = passwordEncoder.encode(password);

        User newUser = new User(username, hashedPassword);
        users.put(username, String.valueOf(newUser));

        return "User registered successfully.";
    }
}
