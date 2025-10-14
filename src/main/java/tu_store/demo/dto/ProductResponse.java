package tu_store.demo.dto;

import tu_store.demo.models.Category;
import tu_store.demo.models.ProductStatus;

public class ProductResponse {
    private Long product_id;
    private String name;
    private long price;
    private int stock;
    private Category category;
    private ProductStatus status;
    private String sellerName;

    public ProductResponse(Long product_id, String name, long price, int stock,
                           Category category, ProductStatus status, String sellerName) {
        this.product_id = product_id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.status = status;
        this.sellerName = sellerName;
    }

    // Getters
    public Long getProduct_id() { return product_id; }
    public String getName() { return name; }
    public long getPrice() { return price; }
    public int getStock() { return stock; }
    public Category getCategory() { return category; }
    public ProductStatus getStatus() { return status; }
    public String getSellerName() { return sellerName; }
}
