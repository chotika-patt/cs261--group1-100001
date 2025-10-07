package tu_store.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public String registerBuyer(@RequestParam String username, @RequestParam String password, @RequestParam String passwordConfirm) {
        return userService.register(username, password, passwordConfirm, "Client");
    }

    @PostMapping("/register/seller")
    public String registerSeller(@RequestParam String username, @RequestParam String password, @RequestParam String passwordConfirm) {
        return userService.register(username, password, passwordConfirm, "Seller");
    }
    
    @PostMapping("/login/buyer")
    public ResponseEntity<String> loginBuyer(@RequestBody User user) {
        boolean valid = userService.login(user);
        if (!valid) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
        return ResponseEntity.ok("Login successful");
    }
    @PostMapping("/login/seller")
    public ResponseEntity<String> loginSeller(@RequestBody() User user) {
        boolean valid = userService.login(user);
        if (!valid) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
        return ResponseEntity.ok("Login successful");
    }
}
