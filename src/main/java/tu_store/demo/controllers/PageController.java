package tu_store.demo.controllers;
// import java.util.HashMap;
// import java.util.Map;

import java.util.List;
import java.util.Map;

// import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import tu_store.demo.models.Product;
import tu_store.demo.models.User;
import tu_store.demo.models.UserRole;

import org.springframework.web.bind.annotation.RequestParam;
import tu_store.demo.repositories.ProductRepository;
import tu_store.demo.repositories.UserRepository;
import tu_store.demo.services.UserService;


@Controller
public class PageController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    public PageController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // just show the login.html template
    }

    @PostMapping("/login")
    public String handleLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session) {

        User user = userRepository.findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            return "redirect:/login?error"; // failed login
        }

        // Save user in session
        session.setAttribute("user", user);

        // redirect based on role
        if (user.getRole() == UserRole.SELLER) {
            return "redirect:/sellerTemp";
        } else if (user.getRole() == UserRole.CLIENT) {
            return "redirect:/buyerTemp";
        } else {
            return "redirect:/login?error";
        }
    }

    @GetMapping("/logout")
    public String logoutPage(@RequestParam String param) {
        return "redirect:/index";
    }

    @GetMapping("/cart")
    public String cartPage() {
        return "cart"; // หมายถึงไฟล์ templates/cart.html
    }

    @GetMapping({"/", "/index"})
    public String indexPage(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "index"; // ชี้ไปที่ templates/index.html
    }

    @GetMapping("/buyerTemp")
    public String buyerTempPage(HttpSession session, Model model) {
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("email", session.getAttribute("email"));
        model.addAttribute("phone", session.getAttribute("phone"));
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "buyerTemp"; // ✅ ชี้ไปที่ templates/buyerTemp.html
    }

    @GetMapping("/sellerTemp")
    public String sellerTempPage(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        User seller = userService.findByUsername(username);

        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("email", session.getAttribute("email"));
        model.addAttribute("phone", session.getAttribute("phone"));
        List<Product> products = productRepository.findBySeller(seller);
        model.addAttribute("products", products);
        return "sellerTemp"; // ✅ ชี้ไปที่ templates/sellerTemp.html
    }

    @GetMapping("/product")
    public String productPage(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "product"; // ✅ ชี้ไปที่ templates/product.html
    }

    @GetMapping("/product_detail")
    public String productDetailTempPage() {
        return "product_detail"; // ✅ อยู่ใน templates/product_detail_temp.html
    }

    @GetMapping("/product_detail/{productId}")
    public String productDetailPage (Model model, @PathVariable Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return "redirect:/error"; // or handle product not found
        }
        model.addAttribute("product", product);
        return "product_detail";
    }
    @GetMapping("/addProduct")
    public String addProductPage() {
        return "addProduct"; // <-- ต้องมีไฟล์ addProduct.html ใน templates
    }
}

