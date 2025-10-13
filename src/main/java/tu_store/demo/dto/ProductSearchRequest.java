package tu_store.demo.dto;

import tu_store.demo.models.Category;
import tu_store.demo.models.ProductStatus;

public class ProductSearchRequest {
    private String name;
    private Category category;
    private ProductStatus status;
    private long minPrice;
    private long maxPrice;

    //Getters & Setters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }
    public void setCategory(Category category) {
        this.category = category;
    }

    public ProductStatus getStatus() {
        return status;
    }
    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public long getMinPrice() {
        return minPrice;
    }
    public void setMinPrice(long minPrice) {
        this.minPrice = minPrice;
    }

    public long getMaxPrice() {
        return maxPrice;
    }
    public void setMaxPrice(long maxPrice) {
        this.maxPrice = maxPrice;
    }
}
