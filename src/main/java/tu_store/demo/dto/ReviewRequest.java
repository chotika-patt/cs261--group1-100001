package tu_store.demo.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReviewRequest {
    private Long orderId;
    private Long productId;
    private Integer rating;
    private String comment;
    private List<ReviewImageDto> images;

    // ===== Getter =====
    public String getComment() {
        return comment;
    }

    public List<ReviewImageDto> getImages() {
        return images;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public int getRating() {
        return rating;
    }

    // ===== Setter =====
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setImages(List<ReviewImageDto> images) {
        this.images = images;
    }
}
