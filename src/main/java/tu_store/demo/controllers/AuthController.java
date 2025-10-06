package tu_store.demo.controllers;

import tu_store.demo.models.User;
import tu_store.demo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // อนุญาตให้ frontend เรียกได้
public class AuthController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        boolean valid = userService.login(user);
        if (!valid) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
        return ResponseEntity.ok("Login successful");
    }
}
