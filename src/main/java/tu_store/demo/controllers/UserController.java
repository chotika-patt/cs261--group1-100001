package tu_store.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import tu_store.demo.models.User;
import tu_store.demo.models.UserRole;
import tu_store.demo.services.UserService;

import java.util.Map;


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
    public ResponseEntity<Map<String, Object>> login(HttpSession sessions, @RequestBody User user) {
        // ✅ ตรวจสอบว่าข้อมูลครบไหม
        if (user.getUsername() == null || user.getUsername().isBlank() ||
                user.getPassword() == null || user.getPassword().isBlank()) {
            return ResponseEntity
                    .badRequest() // <-- นี่คือ status 400
                    .body(Map.of("status", "400","error", "ข้อมูลไม่ครบ กรุณากรอกชื่อผู้ใช้และรหัสผ่าน"));
        }

        boolean valid = userService.login(user);
        if (!valid) {
            return ResponseEntity
                    .status(401)
                    .body(Map.of("status", "401","error", "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง"));
        }

        User dbUser = userService.findByUsername(user.getUsername());
        sessions.setAttribute("username", dbUser.getUsername());
        sessions.setAttribute("email", dbUser.getEmail());
        sessions.setAttribute("phone", dbUser.getPhone());
        sessions.setAttribute("role", dbUser.getRole());

        Map<String, Object> resp = Map.of(
                "username", dbUser.getUsername(),
                "role", dbUser.getRole().toString()
        );

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logOut(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logout succuess");
    }

    @PostMapping("/upload")
    public String uploadData(@RequestBody User user) {
        return "Success" ;
    }    
}
