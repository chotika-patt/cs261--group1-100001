package tu_store.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import tu_store.demo.dto.CartItemDto;
import tu_store.demo.dto.ProductResponse;
import tu_store.demo.dto.ReviewImageDto;
import tu_store.demo.dto.ReviewRequest;
import tu_store.demo.dto.ReviewResponse;
import tu_store.demo.models.*;
import tu_store.demo.repositories.*;

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
        newReview.setBuyer(order.getBuyer());  // ←❗ เปลี่ยนจาก getUser()
        newReview.setProduct(product);
        newReview.setComment(reviewRequest.getComment());
        newReview.setRating(reviewRequest.getRating());

        List<ReviewImage> newImages = new ArrayList<>();
        if (reviewRequest.getImages() != null) {
            for(ReviewImageDto images : reviewRequest.getImages()){
                newImages.add(reviewImageService.creatReviewImage(newReview, images));
            }
        }

        newReview.setImages(newImages);
        reviewRepository.save(newReview);
        
        productService.updateProductRatingById(product.getProductId());

        return newReview;
    }

    public ReviewResponse createReviewResponse(Review review){
        if(review == null) return null;

        ReviewResponse response = new ReviewResponse();
        response.setComment(review.getComment());
        response.setProductId(review.getProduct().getProductId());
        response.setCreatedAt(review.getCreatedAt());
        response.setHasImages(review.isHasImages());
        response.setRating(review.getRating());

        return response;
    }

    public List<ReviewResponse> getLatest100AndCreateResponses(){
        List<Review> reviews = reviewRepository.findTop100ByOrderByCreatedAtDesc();

        if(reviews == null || reviews.isEmpty()) return null;

        List<ReviewResponse> responses = new ArrayList<>();

        for(Review review : reviews){
            responses.add(createReviewResponse(review));
        }

        return responses;
    }
}
