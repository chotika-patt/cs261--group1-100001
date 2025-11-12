package tu_store.demo.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class ReviewResponse {
    private Long reviewId;
    private Long orderId;
    private Long productId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private boolean hasImages;


    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setHasImages(boolean hasImages) {
        this.hasImages = hasImages;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }

    public String getComment() {
        return comment;
    }
    public Long getReviewId() {
        return reviewId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getProductId() {
        return productId;
    }
    
    public Long getOrderId() {
        return orderId;
    }

    public int getRating() {
        return rating;
    }

    public boolean isHasImages() {
        return hasImages;
    }
}