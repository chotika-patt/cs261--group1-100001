package tu_store.demo.controllers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import tu_store.demo.dto.ProductResponse;
import tu_store.demo.dto.ProductSearchRequest;
import tu_store.demo.dto.ReviewResponse;
import tu_store.demo.exception.ApiException;
import tu_store.demo.models.Category;
import tu_store.demo.models.Product;
import tu_store.demo.models.User;
import tu_store.demo.repositories.CartRepository;
import tu_store.demo.repositories.ProductRepository;
import tu_store.demo.repositories.UserRepository;
import tu_store.demo.services.ProductService;
import tu_store.demo.services.ReviewService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import tu_store.demo.services.UserService;


@RestController
@RequestMapping("/api")
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewService reviewService;

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
                searchRequest.getMinPrice(),
                searchRequest.getMaxPrice(),
                searchRequest.getRating(),     // ⭐ new
                searchRequest.getInStock(),    // ⭐ new
                searchRequest.getSort()        // ⭐ new
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
        if(name == null){
            return ResponseEntity.status(500).body("เกิดข้อผิดพลาด: การุณาใส่ชื่อสินค้า");
        }
        product.setName(name);
        if(category == null){
            return ResponseEntity.status(500).body("เกิดข้อผิดพลาด: การุณาใส่ประเภทสินค้า");
        }
        product.setCategory(category);
        if(price <= 0 ){
            return ResponseEntity.status(500).body("เกิดข้อผิดพลาด: การุณากรอกราคาเป็นจำนวนบวก");
        }
        product.setPrice(price);
        if(stock <= 0){
            return ResponseEntity.status(500).body("เกิดข้อผิดพลาด: การุณากรอกจำนวนสินค้าเป็นจำนวนเต็มบวก");
        }
        product.setStock(stock);
        product.setDescription(description);

        // บันทึก product ก่อน เพื่อให้ได้ ID
        ProductResponse saved = productService.addProductDTO(product, username);

        if (main_image != null && !main_image.isEmpty()) {
            String ext = "";
            String originalName = main_image.getOriginalFilename();
            int i = originalName.lastIndexOf('.');
            if (i > 0) ext = originalName.substring(i + 1);

            String fileName = "product_seller_" + saved.getProduct_id()  + (ext.isEmpty() ? "" : "." + ext);

            Path uploadPath = Paths.get(uploadDirProduct).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(fileName);
            main_image.transferTo(filePath.toFile());

            // อัปเดตชื่อไฟล์ใน product
            product.setMain_image(fileName);
            productService.addProductDTO(product, username); // update
        }

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

    //DELETE Product
    @DeleteMapping("seller/product/{id}")
    public ResponseEntity<?> deleteProductByIdFromSeller(
            @PathVariable Long id,
            HttpSession session) {

        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("❌ Please login as seller first.");
        }

        User seller = userRepository.findByUsername(username);
        if (seller == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("❌ Seller not found.");
        }


        Optional<Product> optionalProduct = productRepository.findByProductIdAndSellerUserId(id, seller.getUser_id());
        if (optionalProduct.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("❌ Product not found or you don’t have permission to delete it.");
        }
        productRepository.deleteById(id);

        return ResponseEntity.ok("✅ Product ID " + id + " deleted successfully.");
    }

    @PutMapping("seller/product/{id}")
    public ResponseEntity<?> updateProductBySeller(
            @PathVariable Long id,
            HttpSession session,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) Long price,
            @RequestParam(required = false) Integer stock,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) MultipartFile main_image) {

        // 1️⃣ ตรวจสอบว่า seller login
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("❌ Please login as seller first.");
        }

        // 2️⃣ ดึง seller
        User seller = userRepository.findByUsername(username);
        if (seller == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("❌ Seller not found.");
        }

        // 3️⃣ ตรวจสอบว่าเป็นสินค้าของ seller จริง
        Optional<Product> optionalProduct = productRepository.findByProductIdAndSellerUserId(id, seller.getUser_id());
        if (optionalProduct.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("❌ Product not found or you don’t have permission to update it.");
        }

        Product product = optionalProduct.get();

        // 4️⃣ อัปเดตเฉพาะ field ที่ส่งมา
        if (name != null && !name.isEmpty()) product.setName(name);
        if (category != null) product.setCategory(category);
        if (price != null && price >= 0){
            product.setPrice(price);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("❌ Pirce is no less equal than 0");
        }
        if (stock != null && stock >= 0){
            product.setStock(stock);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("❌ Stock is no less equal than 0");
        }
        if (description != null && !description.isEmpty()) product.setDescription(description);

        // 5️⃣ อัปโหลดรูปใหม่ (ถ้ามี)
        try {
            if (main_image != null && !main_image.isEmpty()) {
                String ext = "";
                String originalName = main_image.getOriginalFilename();
                int i = originalName.lastIndexOf('.');
                if (i > 0) ext = originalName.substring(i + 1);

                String fileName = "product_seller_" + product.getProductId() + (ext.isEmpty() ? "" : "." + ext);
                Path uploadPath = Paths.get(uploadDirProduct).toAbsolutePath().normalize();
                Files.createDirectories(uploadPath); // สร้างโฟลเดอร์ถ้ายังไม่มี
                Path filePath = uploadPath.resolve(fileName);
                main_image.transferTo(filePath.toFile());

                product.setMain_image(fileName);
            }

            // 6️⃣ บันทึกข้อมูล
            productRepository.save(product);

            return ResponseEntity.ok("✅ Product ID " + id + " updated successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error while updating product: " + e.getMessage());
        }
    }


    

    @GetMapping("/{productId}/reviews")
    public ResponseEntity<?> getReviews(
            @PathVariable Long productId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) Integer maxRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt:desc") String sort,
            @RequestParam(required = false) String dateRange,
            HttpSession session
    ){
        Product product = productService.getProductEntityById(productId);
        if (product == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "Product not found");
        }

        // Convert Date
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        if (dateRange != null) {
            String[] parts = dateRange.split(",");
            if (parts.length != 2) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                        "Invalid dateRange format. Use: yyyy-MM-dd,yyyy-MM-dd");
            }

            try {
                startDate = LocalDate.parse(parts[0]).atStartOfDay();
                endDate = LocalDate.parse(parts[1]).atTime(23, 59, 59);
            } catch (DateTimeParseException e) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                        "Invalid date format. Use: yyyy-MM-dd,yyyy-MM-dd");
            }
        }

        // Convert sort
        String sortField = "createdAt";
        Sort.Direction direction = Sort.Direction.DESC;

        if (sort.contains(":")) {
            String[] parts = sort.split(":");
            sortField = parts[0];
            direction = parts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        }

        Sort sortObj;
        try {
            sortObj = Sort.by(direction, sortField);
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Invalid sort field");
        }

        Pageable pageable = PageRequest.of(page, pageSize, sortObj);
        Page<ReviewResponse> reviews = reviewService.getReviewPageResponseWithFilters(productId, search, minRating, maxRating, startDate, endDate, pageable);
        if(reviews.getContent().isEmpty()) return ResponseEntity.status(404).body("Review not found");
        
        return ResponseEntity.ok(reviews);
    }
}