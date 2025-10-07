package tu_store.demo.controllers;

import org.springframework.web.bind.annotation.*;
import tu_store.demo.services.UserService;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register/buyer")
    public String registerBuyer(@RequestParam String username, @RequestParam String email, @RequestParam String password, @RequestParam String passwordConfirm) {
        return userService.register(username, email, password, passwordConfirm, "Client");
    }

    @PostMapping("/register/seller")
    public String registerSeller(@RequestParam String username, @RequestParam String email, @RequestParam String password, @RequestParam String passwordConfirm) {
        return userService.register(username, email, password, passwordConfirm, "Seller");
    }
}
