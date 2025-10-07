package tu_store.demo.services;

import org.springframework.stereotype.Service;

import tu_store.demo.models.User;
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
        
        // return users.containsKey(user.getUsername()) &&
        //         users.get(user.getUsername()).equals(user.getPassword());
    }

    public String register(User httpUser) {
        String username = httpUser.getUsername().trim().toLowerCase();
        String email = httpUser.getEmail().trim();
        String password = httpUser.getPassword();
        String role = httpUser.getRole();

        User user = userRepository.findFirstByUsername(username);

        if (user != null){
            return "Username has already been used.";
        }

        if (password.length() < 6){
            return "Password must be at least 6 characters.";
        }

        if (!email.contains("@")) {
            return "Invalid email address.";
        }
        
        String hashedPassword = passwordEncoder.encode(password);

        if(role == "Client"){
            User newUser = new User(username, email, hashedPassword, httpUser.getPhone(), null, null, role);
            userRepository.save(newUser);
        }
        else if(role == "Seller"){
            User newUser = new User(username, email, hashedPassword, httpUser.getPhone(), httpUser.getStudent_code(), httpUser.getVerify_document(), role);
            userRepository.save(newUser);
        }
        else{
            return "Invalid role.";
        }

        return role + " registered successfully.";
    }
    // public String register(String username, String password, String email, String role) {
    //     username = username.trim().toLowerCase();
    //     email = email.trim();

    //     if (users.containsKey(username)){
    //         return "Username has already been used.";
    //     }

    //     if (password.length() < 6){
    //         return "Password must be at least 6 characters.";
    //     }

    //     if (!email.contains("@")) {
    //         return "Invalid email address.";
    //     }

    //     String hashedPassword = passwordEncoder.encode(password);

    //     User newUser = new User(username, email, hashedPassword, role);
    //     users.put(username, newUser);

    //     return role + " registered successfully.";
    // }
}
