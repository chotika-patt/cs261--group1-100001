package tu_store.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import tu_store.demo.models.User;
import tu_store.demo.models.UserRole;
import tu_store.demo.services.UserService;




@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/register/buyer")
    public String registerBuyer(@RequestBody User user) {
        user.setRole(UserRole.CLIENT);
        return userService.register(user);
    }
    @PostMapping("/register/seller")
    public Long registerSeller(@RequestBody User user) {
        user.setRole(UserRole.SELLER);
        User savedUser = userService.registerReturnUser(user); 
        return savedUser.getUser_id();
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(HttpSession sessions,@RequestBody User user) {
        boolean valid = userService.login(user);
        if (!valid) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
        User dbUser = userService.findByUsername(user.getUsername());
        sessions.setAttribute("username", dbUser.getUsername());
        sessions.setAttribute("email", dbUser.getEmail());
        sessions.setAttribute("phone", dbUser.getPhone());
        sessions.setAttribute("role", dbUser.getRole());
        return ResponseEntity.ok("Login successful");
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logOut(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logout successful");
    }
    @PostMapping("/uplaod")
    public String uploadData(@RequestBody String entity) {
        return "Success" ;
    }    
}
