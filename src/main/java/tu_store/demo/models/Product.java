package tu_store.demo.models;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long product_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false, length = 255)
    private String name;

    @Lob
    private String description;

    @Column(nullable = false)
    private long price;

    @Column(nullable = false)
    private int stock;

    // ✅ Category แบบ Entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Column(length = 255)
    private String main_image;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Product() {}

    public Product(User seller, String name, long price, String description, int stock, Category category) {
        this.seller = seller;
        this.name = name;
        this.price = price;
        this.description = description;
        this.stock = stock;
        this.category = category;
        this.status = ProductStatus.AVAILABLE;
    }

    public boolean isAvailable() { return stock > 0; }

    public void setStock(int stock) {
        this.stock = stock;
        this.status = (stock > 0) ? ProductStatus.AVAILABLE : ProductStatus.OUT_OF_STOCK;
    }

    public Long getProduct_id() { return product_id; }
    public User getSeller() { return seller; }
    public void setSeller(User seller) { this.seller = seller; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }
    public int getStock() { return stock; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
    public String getMain_image() { return main_image; }
    public void setMain_image(String main_image) { this.main_image = main_image; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
