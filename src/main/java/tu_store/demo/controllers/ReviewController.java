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
    private ReviewService reviewService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Value("${file.upload-dir-review}")
    private String uploadDirRev;

    // Helper: ตรวจสอบ session → return User หรือ null
    private User getSessionUser(HttpSession session){
        String username = (String) session.getAttribute("username");
        if(username == null) return null;
        return userRepository.findByUsername(username);
    }

    // ✅ Test add review ผ่าน DTO
    @PostMapping("/addTest")
    public ResponseEntity<?> addTest(HttpSession session, @RequestBody ReviewRequest reviewRequest){
        User user = getSessionUser(session);
        if(user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("❌ Please login first.");

        Review review = reviewService.createReview(reviewRequest);

        return ResponseEntity.ok(reviewService.getLatest100AndCreateResponses());
    }

    // ✅ ดึง review ล่าสุด 100
    @GetMapping("/getReviewsTest")
    public ResponseEntity<?> getReviewTest() {
        return ResponseEntity.ok(reviewService.getLatest100AndCreateResponses());
    }

    // ✅ Create review แบบ form-data (มีรูปได้)
    @PostMapping("{productId}")
    public ResponseEntity<?> addReview(
            @PathVariable Long productId,
            HttpSession session,
            @RequestParam int rating,
            @RequestParam String comment,
            @RequestParam(required = false) MultipartFile image) {

        User user = getSessionUser(session);
        if(user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("❌ Please login first.");

        Product product = productRepository.findFirstByProductId(productId);
        if(product == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("❌ Product not found.");

        if(comment == null || comment.trim().isEmpty() || comment.length() > 1000)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("❌ Invalid comment");

        if(rating < 0 || rating > 5)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("❌ Rating must be between 0 and 5");

        if(reviewRepository.existsByBuyerUserIdAndProductProductId(user.getUser_id(), productId))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("❌ You already reviewed this product");

        Order order = orderRepository.findPurchasedOrder(productId, user.getUser_id());
        if(order == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("❌ You haven't purchased this product");

        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setOrderId(order.getOrderId());
        reviewRequest.setProductId(productId);
        reviewRequest.setRating(rating);
        reviewRequest.setComment(comment);

        Review review = reviewService.createReview(reviewRequest);

        if(image != null && !image.isEmpty()){
            reviewService.saveReviewImage(review, image, uploadDirRev);
        }

        return ResponseEntity.ok("✅ Uploaded review successfully");
    }
}
