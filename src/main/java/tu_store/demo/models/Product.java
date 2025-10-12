package tu_store.demo.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;


@Entity
@Table(name = "Products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long product_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
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
    private ProductStatus status;

    @Column(length = 255, nullable = true)
    private String main_image;  // path หรือ URL ของไฟล์ภาพ (.jpg / .pdf)

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Product() {}

    public Product(User seller, String name, long price, String description, int stock) {
        setSeller(seller);

        this.name = name;
        this.price = price;
        this.description = description;
        this.stock = stock;

        this.status = ProductStatus.AVAILABLE;
    }

    public boolean isAvailable() {
        return stock > 0;
    }

    public void setStock(int stock){
        this.stock = stock;

        if(stock > 0) this.status = ProductStatus.AVAILABLE;
        else this.status = ProductStatus.OUT_OF_STOCK;
    }

    public void setSeller(User seller) {
        if(seller != null && seller.getRole() == UserRole.SELLER){
            this.seller = seller;
        }
    }
}
