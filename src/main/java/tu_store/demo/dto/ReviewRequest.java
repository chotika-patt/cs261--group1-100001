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
}