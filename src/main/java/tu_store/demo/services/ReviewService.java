package tu_store.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.transaction.Transactional;
import tu_store.demo.dto.ReviewImageDto;
import tu_store.demo.dto.ReviewRequest;
import tu_store.demo.dto.ReviewResponse;
import tu_store.demo.models.Review;
import tu_store.demo.models.ReviewImage;
import tu_store.demo.models.Product;
import tu_store.demo.models.Order;
import tu_store.demo.repositories.ProductRepository;
import tu_store.demo.repositories.OrderRepository;
import tu_store.demo.repositories.ReviewRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
}
