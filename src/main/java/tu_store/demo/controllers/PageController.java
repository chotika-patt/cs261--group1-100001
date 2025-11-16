package tu_store.demo.controllers;
// import java.util.HashMap;
// import java.util.Map;

import java.util.List;
import java.util.Map;
import org.apache.el.stream.Optional;
//import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import tu_store.demo.models.Category;
import tu_store.demo.models.Product;
import tu_store.demo.models.ProductStatus;
import tu_store.demo.models.User;
import tu_store.demo.models.UserRole;

import org.springframework.web.bind.annotation.RequestParam;
import tu_store.demo.repositories.ProductRepository;
import tu_store.demo.repositories.UserRepository;
import tu_store.demo.services.UserService;
import org.springframework.web.bind.annotation.RequestBody;



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

    @GetMapping("/register")
    public String gotoRegister(){
        return "register";
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
    public String cartPage(HttpSession session, Model model) {
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("email", session.getAttribute("email"));
        model.addAttribute("phone", session.getAttribute("phone"));
        return "cart"; // หมายถึงไฟล์ templates/cart.html
    }

    @GetMapping({"/", "/index"})
    public String indexPage(Model model, HttpSession session) {
        Object role = session.getAttribute("role");

        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);

        if (role == null) {
            return "index"; // Not logged in, show homepage
        }

        // Redirect based on role
        if ("SELLER".equals(role.toString())) {
            return "redirect:/sellerTemp";
        } else if ("CLIENT".equals(role.toString())) {
            return "redirect:/buyerTemp";
        }
        return "index"; // ชี้ไปที่ templates/index.html
    }

    @GetMapping("/buyerTemp")
    public String buyerTempPage(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        User buyer = userService.findByUsername(username);
        if (username == null) return "redirect:/login";
        else if(buyer == null || buyer.getRole() != UserRole.CLIENT) return "redirect:/";

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
        if (username == null) return "redirect:/login";
        else if(seller == null || seller.getRole() != UserRole.SELLER) return "redirect:/";
        
        String verifiedStatus;
        if (Boolean.TRUE.equals(seller.getVerified())) {
            verifiedStatus = "อนุมัติการขายแล้ว";
        } else {
            verifiedStatus = "ยังไม่อนุมัติการขาย";
        }
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("email", session.getAttribute("email"));
        model.addAttribute("phone", session.getAttribute("phone"));
        model.addAttribute("verifiedStatus", verifiedStatus);
        List<Product> products = productRepository.findBySeller(seller);
        model.addAttribute("products", products);
        return "sellerTemp"; // ✅ ชี้ไปที่ templates/sellerTemp.html
    }

    @GetMapping("/product")
    public String productPage(HttpSession session,Model model) {
        String username = (String) session.getAttribute("username");
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        if (username == null) {
            model.addAllAttributes(Map.of(
                "username", "Guest",
                "email", "-",
                "phone", "-"
            ));
            return "product_no_login";
        }
        String email = (String) session.getAttribute("email");
        String phone = (String) session.getAttribute("phone");
        model.addAllAttributes(Map.of(
            "username", username,
            "email", email,
            "phone", phone
        ));
        return "product"; // ✅ ชี้ไปที่ templates/product.html
    }

    @GetMapping("/product_detail")
    public String productDetail(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        // mock product object
        Map<String, Object> product = Map.of(
            "name", "เสื้อยืดธรรมศาสตร์",
            "price", 259,
            "description", "เสื้อยืดผ้าคุณภาพดี พิมพ์ลายตรามหาวิทยาลัยธรรมศาสตร์"
        );
        model.addAttribute("product", product);

        if (username == null) {
            model.addAllAttributes(Map.of(
                "username", "Guest",
                "email", "-",
                "phone", "-"
            ));
            return "product_detail_no_login";
        }
        // login แล้ว
        String email = (String) session.getAttribute("email");
        String phone = (String) session.getAttribute("phone");
        model.addAllAttributes(Map.of(
            "username", username,
            "email", email,
            "phone", phone
        ));
        return "product_detail";
    }


    @GetMapping("/product_detail/{productId}")
    public String productDetailPage (Model model, @PathVariable Long productId,HttpSession session) {
        String username = (String) session.getAttribute("username");
        Product product = productRepository.findById(productId).orElse(null);
        // Validate productId
        if (productId == null || productId <= 0) {
            model.addAttribute("errorMessage", "รหัสสินค้าผิดพลาด");
            return "error_page"; // create a generic error template
        }
        if (product == null) {
            model.addAttribute("errorMessage", "สินค้านี้ไม่มีอยู่");
            return "error_page"; // or redirect to a 404 page
        }
        if (username == null) {
            model.addAllAttributes(Map.of(
                "username", "Guest",
                "email", "-",
                "phone", "-"
            ));
            model.addAttribute("stock", product.getStock());
            model.addAttribute("product", product);
            return "product_detail_no_login";
        }
        String email = (String) session.getAttribute("email");
        String phone = (String) session.getAttribute("phone");
        model.addAllAttributes(Map.of(
            "username", username,
            "email", email,
            "phone", phone
        ));
        model.addAttribute("product", product);
        model.addAttribute("stock", product.getStock());
        model.addAttribute("productId", product.getProductId());
        return "product_detail";
    }
    @GetMapping("/addProduct")
    public String addProductPage(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        User seller = userService.findByUsername(username);
        if (username == null) return "redirect:/login";
        else if(seller == null || seller.getRole() != UserRole.SELLER) return "redirect:/";

        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("email", session.getAttribute("email"));
        model.addAttribute("phone", session.getAttribute("phone"));
        return "addProduct"; // <-- ต้องมีไฟล์ addProduct.html ใน templates
    }
    @GetMapping("/product_edit/{id}")
    public String editProductPage(@PathVariable Long id, HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        User seller = userService.findByUsername(username);
        if (username == null) return "redirect:/login";
        else if(seller == null || seller.getRole() != UserRole.SELLER) return "redirect:/";


        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("email", session.getAttribute("email"));
        model.addAttribute("phone", session.getAttribute("phone"));
        model.addAttribute("productId", id);

        return "editProduct";
    }

    @GetMapping("/category/{category}")
    public String getProductsByCategory(
            @PathVariable("category") Category category,
            Model model,
            HttpSession session) {

        List<Product> products = productRepository.findByCategory(category);
        model.addAttribute("products", products);
        model.addAttribute("category", category);

        String username = (String) session.getAttribute("username");
        if (username == null) {
            model.addAllAttributes(Map.of(
                "username", "Guest",
                "email", "-",
                "phone", "-"
            ));
            return "product_no_login";
        }

        model.addAllAttributes(Map.of(
            "username", username,
            "email", session.getAttribute("email"),
            "phone", session.getAttribute("phone")
        ));

        return "product";
    }

    @GetMapping("/product_no_login")
    public String productNoLoginPage(Model model) {
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        model.addAllAttributes(Map.of(
                "username", "Guest",
                "email", "-",
                "phone", "-"
        ));
        return "product_no_login"; // ✅ ชี้ไปที่ templates/product_no_login.html
    }
    //---------ลิงก์ไปหน้า ลืมรหัส--------------
    @GetMapping("/forget_password")
    public String showForgetPasswordPage() {
        return "forget_password";
    }
    @GetMapping("/order-status-check")
    public String orderStatusPage(HttpSession session, Model model) { // เปลี่ยนชื่อฟังก์ชันให้ตรงกับหน้าที่
        String username = (String) session.getAttribute("username");
        User buyer = userService.findByUsername(username);
        if (username == null) {
            return "redirect:/login";
        }else if(buyer == null || buyer.getRole() != UserRole.CLIENT){
            return "redirect:/";
        }

        model.addAttribute("username", "Guest");
        model.addAttribute("email", "-");
        model.addAttribute("phone", "-");
        return "order-status-check"; // ✅ ชี้ไปที่ templates/order_status_check.html
    }

     @GetMapping("/order-detail-waiting")
    public String orderDetailWaitingPage(HttpSession session, Model model) { // เปลี่ยนชื่อฟังก์ชันให้ตรงกับหน้าที่
        String username = (String) session.getAttribute("username");
        User buyer = userService.findByUsername(username);
        if (username == null) {
            return "redirect:/login";
        }else if(buyer == null || buyer.getRole() != UserRole.CLIENT){
            return "redirect:/";
        }

        model.addAttribute("username", "Guest");
        model.addAttribute("email", "-");
        model.addAttribute("phone", "-");
        return "order-detail-waiting"; // ✅ ชี้ไปที่ templates/order_status_check.html
    }

    @GetMapping("/sellerOrder")
    public String sellerOrderPage(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        User seller = userService.findByUsername(username);
        if (username == null) {
            return "redirect:/login";
        }else if(seller == null || seller.getRole() != UserRole.SELLER){
            return "redirect:/";
        }
        
        String verifiedStatus;
        if (Boolean.TRUE.equals(seller.getVerified())) {
            verifiedStatus = "อนุมัติการขายแล้ว";
        } else {
            verifiedStatus = "ยังไม่อนุมัติการขาย";
        }
        model.addAttribute("username", username);
        model.addAttribute("email", session.getAttribute("email"));
        model.addAttribute("phone", session.getAttribute("phone"));
        model.addAttribute("verifiedStatus", verifiedStatus);
        List<Product> products = productRepository.findBySeller(seller);
        model.addAttribute("products", products);
        return "sellerOrder";
    };
    @GetMapping("/payment")
    public String showPaymentPage(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        User buyer = userService.findByUsername(username);
        if (username == null) {
            return "redirect:/login";
        }else if(buyer == null || buyer.getRole() != UserRole.CLIENT){
            return "redirect:/";
        }
        model.addAttribute("username", username);
        model.addAttribute("email", session.getAttribute("email"));

        return "payment"; // ✅ ต้องมีไฟล์ชื่อ 'payment.html' อยู่ใน 'src/main/resources/templates/'
    }


}

    

    




