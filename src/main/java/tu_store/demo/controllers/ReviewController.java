package tu_store.demo.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties.Request;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import tu_store.demo.dto.CartItemDto;
import tu_store.demo.dto.CartDto;
import tu_store.demo.dto.OrderDraftResponse;
import tu_store.demo.dto.ReviewRequest;
import tu_store.demo.dto.ReviewResponse;
import tu_store.demo.models.*;
import tu_store.demo.services.CartService;
import tu_store.demo.services.OrderService;
import tu_store.demo.services.ReviewService;
import tu_store.demo.services.UserService;
import tu_store.demo.services.CartItemService;
import tu_store.demo.repositories.OrderRepository;
import tu_store.demo.repositories.ProductRepository;
import tu_store.demo.repositories.ReviewRepository;
import tu_store.demo.repositories.UserRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



@RestController
@RequestMapping("/api/review")
public class ReviewController {
    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    
    private UserRepository userRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired 
    private OrderRepository orderRepository;
    
    @Value("${file.upload-dir-review}")
    private String uploadDirRev;

    @PostMapping("/addTest")
    public ResponseEntity<?> addTest(HttpSession session, @RequestBody ReviewRequest reviewRequest){

        reviewService.createReview(reviewRequest);
        

        return ResponseEntity.ok(getReviewTest(session));
    }

    @GetMapping("/getReviewsTest")
    public ResponseEntity<?> getReviewTest(HttpSession session) {

        return ResponseEntity.ok(reviewService.getLatest100AndCreateResponses());
    }

    //Create Review
    @PostMapping("{id}")
    public ResponseEntity<?> addReview(
            @PathVariable Long id,
            HttpSession session,
            @RequestParam int rating,
            @RequestParam String comment,
            @RequestParam(required = false) MultipartFile image) {

        try {
            String username = (String) session.getAttribute("username");
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("❌ Please login as CLIENT first.");
            }

            UserRole role = (UserRole) session.getAttribute("role");
        
            if (role !=UserRole.CLIENT) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("❌ Only CLIENT can add review.");
            }

            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("❌ User not found");
            }

            Product product = productRepository.findFirstByProductId(id);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("❌ Product not found");
            }

            if (comment == null || comment.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("❌ No text in comment");
            }

            if (comment.length() > 1000) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("❌ Comment too long");
            }

            if (rating < 0 || rating > 5) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("❌ Rating must be between 0 and 5");
            }
            
            if (reviewRepository.existsByBuyerUserIdAndProductProductId(user.getUser_id(), id)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("❌ You already reviewed this product");
            }

            Review review = new Review();
            review.setBuyer(user);
            review.setProduct(product);
            review.setRating(rating);
            review.setComment(comment);


            Order order = orderRepository.findPurchasedOrder(id, user.getUser_id());
            if(order == null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You've never bought this product");
            }

            // เตรียม list images ให้ไม่ null
            List<ReviewImage> imagesList = new ArrayList<>();
            review.setImages(imagesList);

            // save review ก่อนเพื่อให้ได้ reviewId
            Review savedReview = reviewRepository.save(review);
            
            // ถ้ามี image
            if (image != null && !image.isEmpty()) {
                try {
                    String originalName = image.getOriginalFilename();
                    String ext = "";
                    int dot = originalName != null ? originalName.lastIndexOf('.') : -1;
                    if (dot > 0) ext = originalName.substring(dot + 1);

                    String fileName = "review_" + savedReview.getReviewId() + (ext.isEmpty() ? "" : "." + ext);

                    Path uploadPath = Paths.get(uploadDirRev).toAbsolutePath().normalize();
                    Files.createDirectories(uploadPath);

                    Path filePath = uploadPath.resolve(fileName);
                    image.transferTo(filePath.toFile());

                    // สร้าง ReviewImage แล้ว add เข้า review
                    ReviewImage reviewImage = new ReviewImage();
                    reviewImage.setReview(savedReview);
                    reviewImage.setFilePath(fileName);
                    savedReview.getImages().add(reviewImage);

                    reviewRepository.save(savedReview);
                } catch (Exception e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("❌ Error while uploading review image: " + e.getMessage());
                }
            }

            return ResponseEntity.ok("✅ Uploaded review successfully");

        } catch (Exception e) {
            e.printStackTrace(); // ดู stack trace ที่ console
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "errorCode", "INTERNAL_ERROR",
                            "message", "Internal server error",
                            "details", e.getMessage()
                    ));
        }
    }


    
    
}