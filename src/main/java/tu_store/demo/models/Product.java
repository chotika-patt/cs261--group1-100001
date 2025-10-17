package tu_store.demo.models;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "Products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    @JsonIgnoreProperties({
            "password",
            "verify_document",
            "studentID",
            "phone",
            "role",
            "createdAt",
            "user_id",
            "hibernateLazyInitializer",
            "handler"})
    private User seller;

    @Column(nullable = false, length = 255)
    private String  name;

    @Lob
    private String description;
    
    @Column(nullable = false)
    private long price;

    @Column(nullable = false)
    private int stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Column(length = 255, nullable = true)
    private String main_image;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private ProductGroup productGroup;

    public Product() {}

    public Product(User seller, String name, long price, String description, int stock) {
        setSeller(seller);
        setStock(stock);

        this.name = name;
        this.price = price;
        this.description = description;
        
        if(stock <= 0){ 
            this.status = ProductStatus.OUT_OF_STOCK; 
        }else this.status = ProductStatus.AVAILABLE; 
    }

    public Product(User seller, String name, long price, String description, int stock, Category category) 
    { 
        setSeller(seller); 
        setStock(stock); 
        this.name = name; 
        this.price = price; 
        this.description = description; 
        this.category = category; 

        if(stock <= 0){ 
            this.status = ProductStatus.OUT_OF_STOCK; 
        }else this.status = ProductStatus.AVAILABLE; 
    }

    public boolean isAvailable() {
        return stock > 0;
    }

    public void setStock(int stock){
        this.stock = stock;
        if(stock > 0) this.status = ProductStatus.AVAILABLE;
        else this.status = ProductStatus.OUT_OF_STOCK;
    }

    public int getStock(){
        return this.stock;
    }

    public void setSeller(User seller) {
        if(seller != null && seller.getRole() == UserRole.SELLER){
            this.seller = seller;
        }
    }

    public void setProductGroup(ProductGroup productGroup) {
        this.productGroup = productGroup;
    }

    // ===== แก้ getter =====

    public ProductGroup getProductGroup() {
        return productGroup;
    }

    public Long getProductId() {
        return this.productId;
    }

    public String getName() {
        return this.name;
    }

    public long getPrice() {
        return this.price;
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
}