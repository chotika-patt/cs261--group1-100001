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

        UserRole role = (UserRole) session.getAttribute("role");

        // ✅ ป้องกัน NullPointerException
        if (role == null) {
            // ถ้าไม่มี role ใน session ให้กลับไปหน้า login
            return "redirect:/login";
        }

        if (role.equals(UserRole.SELLER)) {
            return "sellerTemp";
        } else if (role.equals(UserRole.CLIENT)) {
            return "buyerTemp";
        }

        // เผื่อ role ไม่ตรงกับที่กำหนดไว้
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logoutPage(@RequestParam String param) {
        return "redirect:/index";
    }
   @GetMapping("redirect:/loginTemp")
    public String productDetail(HttpSession session, Model model) {
        model.addAllAttributes(Map.of(
            "username", session.getAttribute("username"),
            "email", session.getAttribute("email"),
            "phone", session.getAttribute("phone"),
            "role", session.getAttribute("role")
        ));
        return "product_detail_temp";
    }

    @GetMapping("/cart")
    public String cartPage() {
        return "cart"; // หมายถึงไฟล์ templates/cart.html
    }

    @GetMapping({"/", "/index"})
    public String indexPage() {
        return "index"; // ชี้ไปที่ templates/index.html
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
    public String productDetailTempPage() {
        return "product_detail_temp"; // ✅ อยู่ใน templates/product_detail_temp.html
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // หมายถึง templates/login.html
    }

    @GetMapping("/product_detail")
    public String productDetailPage(HttpSession session) {
        // ดึงจาก session แล้วส่งให้ Thymeleaf ใช้
        session.getAttribute("username");
        return "product_detail"; // ชื่อไฟล์ใน templates
    }




}

