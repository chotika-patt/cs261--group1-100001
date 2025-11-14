package tu_store.demo.dto;

import tu_store.demo.models.Category;

public class ProductSearchRequest {

    private String name;
    private Category category;

    private Long minPrice;
    private Long maxPrice;

    private Double rating;      // ⭐ new
    private Boolean inStock;    // ⭐ new
    private String sort;        // ⭐ new

    // ======== Getters & Setters ========

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

    public Long getMinPrice() {
        return minPrice;
    }
    public void setMinPrice(Long minPrice) {
        this.minPrice = minPrice;
    }

    public Long getMaxPrice() {
        return maxPrice;
    }
    public void setMaxPrice(Long maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Double getRating() {
        return rating;
    }
    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Boolean getInStock() {
        return inStock;
    }
    public void setInStock(Boolean inStock) {
        this.inStock = inStock;
    }

    public String getSort() {
        return sort;
    }
    public void setSort(String sort) {
        this.sort = sort;
    }
}
