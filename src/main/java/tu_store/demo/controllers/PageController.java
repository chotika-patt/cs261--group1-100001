package tu_store.demo.controllers;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class PageController {
    @GetMapping("/loginTemp")
    public String loginPage(HttpSession session, Model model) {
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("email", session.getAttribute("email"));
        model.addAttribute("phone", session.getAttribute("phone"));
        return "loginTemp";
    }
    @GetMapping("/logout")
    public String logoutPage(@RequestParam String param) {
        return "redirect:/index";
    }
}
