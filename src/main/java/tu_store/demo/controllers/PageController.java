package tu_store.demo.controllers;
// import java.util.HashMap;
// import java.util.Map;

import java.util.Map;

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
    @GetMapping("/cart")
    public String cartPage() {
        return "cart"; // หมายถึงไฟล์ templates/cart.html
    }
    
    @GetMapping("/buyerTemp")
    public String buyerTempPage(HttpSession session, Model model) {
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("email", session.getAttribute("email"));
        model.addAttribute("phone", session.getAttribute("phone"));
        return "buyerTemp"; // ✅ ชี้ไปที่ templates/buyerTemp.html
    }

    @GetMapping("/sellerTemp")
    public String sellerTempPage(HttpSession session, Model model) {
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("email", session.getAttribute("email"));
        model.addAttribute("phone", session.getAttribute("phone"));
        return "sellerTemp"; // ✅ ชี้ไปที่ templates/sellerTemp.html
    }

    @GetMapping("/product")
    public String productPage() {
        return "product"; // ✅ ชี้ไปที่ templates/product.html
    }
    @GetMapping("/product_detail_temp")
    public String productDetail(HttpSession session, Model model) {
        model.addAllAttributes(Map.of(
            "username", session.getAttribute("username"),
            "email", session.getAttribute("email"),
            "phone", session.getAttribute("phone"),
            "role", session.getAttribute("role")
        ));
        return "product_detail";
    }
}

