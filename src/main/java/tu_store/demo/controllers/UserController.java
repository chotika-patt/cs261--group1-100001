package tu_store.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import tu_store.demo.models.User;
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
        user.setRole("Client");
        return userService.register(user);
    }
    @PostMapping("/register/seller")
    public String registerSeller(@RequestBody User user) {
        user.setRole("Seller");
        return userService.register(user);
    }
    @PostMapping("/login/buyer")
    public ResponseEntity<String> loginBuyer(HttpSession sessions,@RequestBody User user) {
        sessions.setAttribute("username", user.getUsername());
        sessions.setAttribute("role", user.getRole());
        boolean valid = userService.login(user);
        if (!valid) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
        return ResponseEntity.ok("Login successful");
    }
    @PostMapping("/login/seller")
    public ResponseEntity<String> loginSeller(HttpSession sessions,@RequestBody() User user) {
        sessions.setAttribute("username", user.getUsername());
        sessions.setAttribute("role", user.getRole());
        boolean valid = userService.login(user);
        if (!valid) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
        return ResponseEntity.ok("Login successful");
    }
}
