package tu_store.demo.models;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(
    name = "Products",
    indexes = {
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_price", columnList = "price"),
        @Index(name = "idx_stock", columnList = "stock"),
        @Index(name = "idx_rating_average", columnList = "rating_average"),
        @Index(name = "idx_sold_count", columnList = "sold_count"),
        @Index(name = "idx_updated_at", columnList = "updated_at"),
        @Index(name = "idx_created_at", columnList = "created_at")
    }
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = true)
    @JsonIgnoreProperties({
            "password", "verify_document", "studentID", "phone", "role",
            "createdAt", "user_id", "hibernateLazyInitializer", "handler"
    })
    private User seller;

    @Column(nullable = true, length = 255, columnDefinition = "NVARCHAR(255)")
    private String name;

    @Lob
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(nullable = true)
    private long price;

    @Column(nullable = true)
    private int stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ProductStatus status;

    @Column(length = 255)
    private String main_image;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ⭐ NEW: updated_at สำหรับ sorting
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ⭐ NEW: sold_count สำหรับ sorting/filter
    @Column(name = "sold_count", nullable = true)
    private Integer soldCount = 0;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private ProductGroup productGroup;

    @Column(name = "rating_average")
    private Double ratingAvg = 0.0;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @Column(name = "organization_type", columnDefinition = "NVARCHAR(50)", nullable = true)
    private String organizationType;


    // ================= Constructors =================

    public Product() {}

    public Product(User seller, String name, long price, String description, int stock) {
        setSeller(seller);
        setStock(stock);
        this.name = name;
        this.price = price;
        this.description = description;

        if (stock <= 0) this.status = ProductStatus.OUT_OF_STOCK;
        else this.status = ProductStatus.AVAILABLE;
    }

    public Product(User seller, String name, long price, String description, int stock, Category category) {
        setSeller(seller);
        setStock(stock);
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;

        if (stock <= 0) this.status = ProductStatus.OUT_OF_STOCK;
        else this.status = ProductStatus.AVAILABLE;
    }

    // ⭐ Auto-update timestamp
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ================= Helpers =================

    public boolean isAvailable() {
        return stock > 0;
    }

    // ================= Setters =================

    public void setStock(int stock) {
        this.stock = stock;
        if (stock > 0) this.status = ProductStatus.AVAILABLE;
        else this.status = ProductStatus.OUT_OF_STOCK;
    }

    public void setSeller(User seller) {
        if (seller != null && seller.getRole() == UserRole.SELLER) {
            this.seller = seller;
        }
    }

    public void setProductGroup(ProductGroup productGroup) {
        this.productGroup = productGroup;
    }

    public void setMain_image(String main_image) {
        this.main_image = main_image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRatingAvg(Double ratingAvg) {
        this.ratingAvg = ratingAvg;
    }

    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }

    public void setSoldCount(Integer soldCount) {
        this.soldCount = soldCount;
    }

    public void setOrganizationType(String organizationType) { 
        this.organizationType = organizationType; 
    }

    // ================= Getters =================

    public Long getProductId() {
        return this.productId;
    }

    public String getName() {
        return this.name;
    }

    public long getPrice() {
        return this.price;
    }

    public int getStock() {
        return this.stock;
    }

    public Category getCategory() {
        return this.category;
    }

    public ProductStatus getStatus() {
        return this.status;
    }

    public User getSeller() {
        return this.seller;
    }

    public String getDescription() {
        return this.description;
    }

    public String getMain_image() {
        return this.main_image;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public Integer getSoldCount() {
        return this.soldCount;
    }

    public ProductGroup getProductGroup() {
        return this.productGroup;
    }

    public Double getRatingAvg() {
        return this.ratingAvg;
    }

    public Integer getRatingCount() {
        return this.ratingCount;
    }

    public String getOrganizationType() { 
        return organizationType; 
    }
}
