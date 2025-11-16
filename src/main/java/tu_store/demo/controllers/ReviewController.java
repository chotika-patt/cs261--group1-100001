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
import tu_store.demo.dto.BuyerOrderResponse;
import tu_store.demo.dto.CartDto;
import tu_store.demo.dto.OrderDraftResponse;
import tu_store.demo.dto.ReviewImageDto;
import tu_store.demo.dto.ReviewRequest;
import tu_store.demo.dto.ReviewResponse;
import tu_store.demo.exception.ApiException;
import tu_store.demo.models.*;
import tu_store.demo.services.CartService;
import tu_store.demo.services.OrderService;
import tu_store.demo.services.ProductService;
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

    @Autowired private UserService userService;
    @Autowired private ReviewService reviewService;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderService orderService;
    @Autowired private ProductService productService;

    
    @Value("${file.upload-dir-review}")
    private String uploadDirRev;

    private void validateReviewRequest(ReviewRequest req){
        if (req.getOrderId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "orderId is required", "ORDER_ID_REQUIRED");
        }

        if (req.getProductId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "productId is required", "PRODUCT_ID_REQUIRED");
        }

        if (req.getComment() == null || req.getComment().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "comment is required", "COMMENT_REQUIRED");
        }

        if (req.getRating() < 1 || req.getRating() > 5) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Rating must be 1-5", "INVALID_RATING");
        }

        if (req.getComment() != null && req.getComment().length() > 1000) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Comment must be ≤ 1000 characters", "COMMENT_TOO_LONG");
        }
    }
    private void validateReviewImages(List<MultipartFile> images) {
        if(images.size() > 5){
            throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "Maximum 5 images allowed", "TOO_MANY_IMAGES");
        }

        for (MultipartFile image : images) {
            if (image.isEmpty()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Image file is required", "INVALID_IMAGE");
            }

            String contentType = image.getContentType();
            if (contentType == null ||
            !(contentType.equalsIgnoreCase("image/jpeg") || contentType.equalsIgnoreCase("image/png"))) {
                throw new ApiException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only JPG or PNG is allowed", "UNSUPPORTED_MEDIA_TYPE");
            }

            if (image.getSize() > 5 * 1024 * 1024) {
                throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "Maximum allowed image size is 5MB", "IMAGE_TOO_LARGE");
            }
        }
    }

    @PostMapping("")
    public ResponseEntity<?> createReview(
            HttpSession session,
            @RequestParam Long orderId,
            @RequestParam Long productId,
            @RequestParam int rating,
            @RequestParam String comment,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) 
        {

        Long userId = userService.getUserIdBySession(session);
        if(userId == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please login first.");
        
        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setOrderId(orderId);
        reviewRequest.setProductId(productId);
        reviewRequest.setRating(rating);
        reviewRequest.setComment(comment);

        validateReviewRequest(reviewRequest);

        if(images != null && !images.isEmpty()){
            validateReviewImages(images);
        }
        
        BuyerOrderResponse orderResponse = orderService.createClientOrderResponseByIdAndUserId(reviewRequest.getOrderId(), userId);
        if (orderResponse == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT FOUND", "Order Not Found");
        }

        if(reviewService.isReviewExist(userId, productId, orderId)){
            throw new ApiException(
                HttpStatus.BAD_REQUEST,
                "You have already reviewed this product for this order",
                "REVIEW_ALREADY_EXISTS"
            );
        }

        Review review = reviewService.createReview(reviewRequest);

        if(review == null){
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create review", "REVIEW_CREATION_FAILED");
        }

        if (images != null && !images.isEmpty()) {
            int i = 1;
            for(MultipartFile image : images){
                reviewService.saveReviewImage(review, image, uploadDirRev);
                i++;
            }
            
        }
        
        return ResponseEntity.ok(reviewService.createReviewResponse(review));
    }
    
    @PatchMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
        HttpSession session,
        @PathVariable Long reviewId,
        @RequestParam(required = false) Integer rating,
        @RequestParam(required = false) String comment,
        @RequestPart(value = "images", required = false) List<MultipartFile> images
    ){
        Long userId = userService.getUserIdBySession(session);
        if(userId == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please login first.");


        Review review = reviewService.findReviewById(reviewId);
        if(review == null) throw new ApiException(HttpStatus.NOT_FOUND, "REVIEW_NOT_FOUND", "Review not found");

        if (!review.getBuyer().getUser_id().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You cannot update this review");
        }

        
        if (rating != null) {
            if (rating < 1 || rating > 5) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_RATING", "Rating must be between 1 and 5");
            }
        }

        if (comment != null) {
            if (comment.length() > 1000) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "COMMENT_TOO_LONG", "Comment cannot exceed 1000 characters");
            }
        }

        if (images != null && !images.isEmpty()) {
            validateReviewImages(images);
        }

        Review updated = reviewService.updateReview(reviewId, comment, rating, images, uploadDirRev);

        return ResponseEntity.ok(reviewService.createReviewResponse(updated));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(HttpSession session, @PathVariable Long reviewId) {
        Long userId = userService.getUserIdBySession(session);
        if(userId == null) throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Please login first.");

        Review review = reviewService.findReviewById(reviewId);
        if(review == null) throw new ApiException(HttpStatus.NOT_FOUND, "REVIEW_NOT_FOUND", "Review not found");

        if (!review.getBuyer().getUser_id().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You cannot delete this review");
        }

        reviewService.deleteReview(review, uploadDirRev);
        
        return ResponseEntity.ok("Deleted!");
    }

    

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
    @PostMapping("/{productId}")
    public ResponseEntity<?> addReview(
            @PathVariable Long productId,
            HttpSession session,
            @RequestParam int rating,
            @RequestParam String comment,
            @RequestParam(required = false) MultipartFile image) {

        // เช็ค login
        String username = (String) session.getAttribute("username");
        if (username == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("❌ Please login first.");

        User user = userRepository.findByUsername(username);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("❌ User not found.");

        // ดึงสินค้า
        Product product = productRepository.findFirstByProductId(productId);
        if (product == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("❌ Product not found.");

        // เช็คว่า user ซื้อสินค้านี้แล้วหรือยัง
        Order order = orderRepository.findPurchasedOrder(productId, user.getUser_id());
        if (order == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("❌ You haven't purchased this product");

        // เช็คว่าเคยรีวิวสินค้านี้หรือยัง
        if (reviewRepository.existsByBuyerUserIdAndProductProductId(user.getUser_id(), productId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("❌ You already reviewed this product");
        }

        // สร้าง ReviewRequest DTO
        ReviewRequest reviewRequest = new ReviewRequest();
        reviewRequest.setOrderId(order.getOrderId());
        reviewRequest.setProductId(productId);
        reviewRequest.setRating(rating);
        reviewRequest.setComment(comment);

        // สร้างรีวิว
        var review = reviewService.createReview(reviewRequest);

        // ถ้ามีรูป
        if (image != null && !image.isEmpty()) {
            reviewService.saveReviewImage(review, image, uploadDirRev);
        }

        return ResponseEntity.ok("✅ Uploaded review successfully");
    }
}
