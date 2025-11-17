package tu_store.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ReviewResponse {

    private Long reviewId;
    private Long orderId;
    private Long productId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private boolean hasImages;

    // ✅ เพิ่มฟิลด์ใหม่
    private String buyerName;
    private String size;
    private String color;
    private Integer quantity;
    private List<String> imageUrls; // สำหรับรูปรีวิวถ้ามี

    // --- Getter / Setter ---
    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isHasImages() { return hasImages; }
    public void setHasImages(boolean hasImages) { this.hasImages = hasImages; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}
