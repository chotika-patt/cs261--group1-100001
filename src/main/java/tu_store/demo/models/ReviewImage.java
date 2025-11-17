package tu_store.demo.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "review_images")
public class ReviewImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(nullable = false, length = 255)
    private String filePath;

    @Column(length = 100)
    private String mimeType;

    private Long size;
    private LocalDateTime createdAt = LocalDateTime.now();

    public ReviewImage() {} // default constructor สำหรับ JPA

    // getters
    public Long getImageId() { return imageId; }
    public Review getReview() { return review; }
    public String getFilePath() { return filePath; }
    public String getMimeType() { return mimeType; }
    public Long getSize() { return size; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // setters
    public void setReview(Review review) { this.review = review; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public void setSize(Long size) { this.size = size; }
}
