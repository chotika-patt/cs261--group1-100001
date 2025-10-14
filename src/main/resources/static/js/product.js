// imports ด้านบนสุดต้องมี
import jakarta.persistence.*;
import tu_store.demo.models.Category;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double price;

    // ✅ ต้องใส่ annotation นี้
    @ManyToOne
    @JoinColumn(name = "category_id") // ชื่อคอลัมน์ใน DB ที่โยงกับ Category
    private Category category;

    // ตัวอย่าง field อื่น ๆ
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    private String description;

    // ✅ getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
