package tu_store.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.transaction.Transactional;
import tu_store.demo.dto.ReviewImageDto;
import tu_store.demo.dto.ReviewRequest;
import tu_store.demo.dto.ReviewResponse;
import tu_store.demo.dto.SellerOrderResponse;
import tu_store.demo.exception.ApiException;
import tu_store.demo.models.Review;
import tu_store.demo.models.ReviewImage;
import tu_store.demo.models.enums.OrderStatus;
import tu_store.demo.models.Product;
import tu_store.demo.models.Order;
import tu_store.demo.repositories.ProductRepository;
import tu_store.demo.repositories.OrderRepository;
import tu_store.demo.repositories.ReviewRepository;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewImageService reviewImageService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Transactional
    public Review createReview(ReviewRequest reviewRequest) {
        if(reviewRequest == null) return null;

        Order order = orderRepository.findFirstByOrderId(reviewRequest.getOrderId());
        Product product = productRepository.findFirstByProductId(reviewRequest.getProductId());

        if(order == null || product == null) return null;

        Review newReview = new Review();
        newReview.setOrder(order);
        newReview.setBuyer(order.getBuyer());
        newReview.setProduct(product);
        newReview.setComment(reviewRequest.getComment());
        newReview.setRating(reviewRequest.getRating());

        List<ReviewImage> newImages = new ArrayList<>();
        if(reviewRequest.getImages() != null){
            for(ReviewImageDto dto : reviewRequest.getImages()){
                newImages.add(reviewImageService.creatReviewImage(newReview, dto));
            }
        }

        newReview.setImages(newImages);
        reviewRepository.save(newReview);

        productService.updateProductRatingById(product.getProductId());

        return newReview;
    }

    public void saveReviewImage(Review review, MultipartFile image, String uploadDir){
        try {
            String originalName = image.getOriginalFilename();
            String ext = "";
            int dot = originalName != null ? originalName.lastIndexOf('.') : -1;
            if(dot > 0) ext = originalName.substring(dot + 1);

            String fileName = "review_" + review.getReviewId() + (ext.isEmpty() ? "" : "." + ext);

            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(fileName);
            image.transferTo(filePath.toFile());

            ReviewImage reviewImage = new ReviewImage();
            reviewImage.setReview(review);
            reviewImage.setFilePath(fileName);
            review.getImages().add(reviewImage);

            reviewRepository.save(review);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Transactional
    public Review updateReview(Long reviewId, String newComment, Integer newRating, List<MultipartFile> images, String uploadDirRev) {
        Review review = reviewRepository.findFirstByReviewId(reviewId);
        if(review == null) return null;

        if (newComment != null) review.setComment(newComment);
        if (newRating != null) review.setRating(newRating);

        // Update images
        if (images != null && !images.isEmpty()) {
            review.getImages().clear(); // remove old images
            for (MultipartFile image : images) {
                saveReviewImage(review, image, uploadDirRev);
            }
        }

        Review saved = reviewRepository.save(review);

        productService.updateProductRatingById(review.getProduct().getProductId());
        return saved;
    }

    public Review findReviewById(Long id){
        return reviewRepository.findFirstByReviewId(id);
    }

    private void deleteReviewImageFile(String fileName, String uploadDir) {
        if (fileName == null || fileName.isBlank()) return;

        Path path = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(fileName);
        File file = path.toFile();

        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                System.err.println("Failed to delete file: " + path.toString());
            }
        }
    }

    public boolean deleteReview(Review review, String uploadDirRev){
        if(review == null) return false;

        if (review.getImages() != null && !review.getImages().isEmpty()) {
            for (ReviewImage img : review.getImages()) {
                deleteReviewImageFile(img.getFilePath(), uploadDirRev);
            }
        }

        reviewRepository.delete(review);
        productService.updateProductRatingById(review.getProduct().getProductId());

        return true;
    }

    public List<ReviewResponse> getLatest100AndCreateResponses(){
        List<Review> reviews = reviewRepository.findTop100ByOrderByCreatedAtDesc();
        List<ReviewResponse> responses = new ArrayList<>();
        if(reviews != null){
            for(Review r : reviews){
                ReviewResponse res = new ReviewResponse();
                res.setProductId(r.getProduct().getProductId());
                res.setRating(r.getRating());
                res.setComment(r.getComment());
                res.setCreatedAt(r.getCreatedAt());
                res.setHasImages(r.isHasImages());
                responses.add(res);
            }
        }
        return responses;
    }

    public ReviewResponse createReviewResponse(Review review){
        if(review == null) return null;
        ReviewResponse response = new ReviewResponse();
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        response.setOrderId(review.getOrder().getOrderId());
        response.setProductId(review.getOrder().getOrderId());
        response.setRating(review.getRating());
        response.setReviewId(review.getReviewId());

        boolean hasImage = false;
        if(review.getImages() != null && !review.getImages().isEmpty()) hasImage = true;

        response.setHasImages(hasImage);

        return response;
    }
    public Page<ReviewResponse> getReviewPageResponseWithFilters(
        Long productId,
        String search,
        Integer minRating,
        Integer maxRating,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    ){
        Page<Review> reviews;
        
        reviews = reviewRepository.findAllByProductIdWithFilters(productId, search, minRating, maxRating, startDate, endDate, pageable);

        return reviews.map(this::createReviewResponse);
    }

    public boolean isReviewExist(Long buyerId, Long productId, Long orderId){  
        return reviewRepository.existsByBuyerUserIdAndProductProductIdAndOrderOrderId(buyerId, productId, orderId);
    }
}
