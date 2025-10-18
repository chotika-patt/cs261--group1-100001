package tu_store.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import tu_store.demo.dto.AddToCartRequest;
import tu_store.demo.dto.ProductResponse;
import tu_store.demo.dto.ProductSearchRequest;
import tu_store.demo.models.Cart;
import tu_store.demo.models.Category;
import tu_store.demo.models.Product;
import tu_store.demo.models.User;
import tu_store.demo.repositories.CartRepository;
import tu_store.demo.repositories.UserRepository;
import tu_store.demo.services.ProductService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestParam;
import tu_store.demo.services.UserService;


@RestController
@RequestMapping("/api")
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @Value("${file.upload-dir-product}")
    private String uploadDirProduct;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @PostMapping("/search")
    public ResponseEntity<?> searchProducts(@RequestBody ProductSearchRequest searchRequest){
        var results = productService.search(
                searchRequest.getName(),
                searchRequest.getCategory(),
                searchRequest.getStatus(),
                searchRequest.getMinPrice(),
                searchRequest.getMaxPrice()
        );
    if (results.isEmpty()){
        return ResponseEntity.ok(Map.of("message", "Nothing match your search terms, please try again."));
    }

    return ResponseEntity.ok(results);

    }
    @PostMapping("/add")
    public ResponseEntity<?> addProduct(
            HttpSession session,
            @RequestParam String name,
            @RequestParam Category category,
            @RequestParam long price,
            @RequestParam int stock,
            @RequestParam String description,
            @RequestParam(required = false) MultipartFile main_image) {

        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(401).body("Please login as seller.");
        }

        User user = userRepository.findByUsername(username);
        if (user.getVerified() == null || !user.getVerified()) {
            return ResponseEntity.status(403).body("Your account has not been verified yet.");
        }

        try {
            Product product = new Product();
            product.setName(name);
            product.setCategory(category);
            product.setPrice(price);
            product.setStock(stock);
            product.setDescription(description);

            if (main_image != null && !main_image.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + main_image.getOriginalFilename();

                // ✅ สร้าง directory ถ้ายังไม่มี
                Path uploadPath = Paths.get(uploadDirProduct).toAbsolutePath().normalize();
                Files.createDirectories(uploadPath);

                Path filePath = uploadPath.resolve(fileName);
                main_image.transferTo(filePath.toFile());

                product.setMain_image(filePath.toString());
            }

            ProductResponse saved = productService.addProductDTO(product, username);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("เกิดข้อผิดพลาด: " + e.getMessage());
        }
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProdctById(@PathVariable Long id) {
        ProductResponse response = productService.getProductResponseById(id);
        if (response == null) return ResponseEntity.status(404).body("Product not found");
        return ResponseEntity.ok(response);
    }

    @GetMapping("users/{userId}/products")
    public ResponseEntity<?> getProdctsByUserId(@PathVariable Long userId) {
        List <ProductResponse> responseList = productService.getAllProductsResponseByUserId(userId);
        return ResponseEntity.ok(responseList);
    }
}