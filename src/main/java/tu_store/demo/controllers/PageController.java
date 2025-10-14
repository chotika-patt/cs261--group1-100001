package tu_store.demo.controllers;
// import java.util.HashMap;
// import java.util.Map;

// import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import tu_store.demo.models.UserRole;

import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class PageController {
    @GetMapping("/loginTemp")
    public String loginPage(HttpSession session, Model model) {
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("email", session.getAttribute("email"));
        model.addAttribute("phone", session.getAttribute("phone"));
        UserRole role =(UserRole) session.getAttribute("role");
        
        if(role.equals(UserRole.SELLER)){
            return "sellerTemp";
        }
        else if(role.equals(UserRole.CLIENT)){
            return "buyerTemp";
        }
        // ADMIN ค่อยทำ
        return "Failed";
    }
    @GetMapping("/logout")
    public String logoutPage(@RequestParam String param) {
        return "redirect:/index";
    }
}

