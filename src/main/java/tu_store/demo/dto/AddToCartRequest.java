package tu_store.demo.dto;

public class AddToCartRequest {

    private Long productId;
    private int quantity;

    // ⭐ NEW: size (nullable)
    private String size;

    // ------------------------------
    // Setters
    // ------------------------------
    public void setProductId(Long productId){
        this.productId = productId;
    }

    public void setQuantity(int quantity){
        this.quantity = quantity;
    }

    public void setSize(String size) {
        this.size = size;
    }

    // ------------------------------
    // Getters
    // ------------------------------
    public Long getProductId(){
        return productId;
    }

    public int getQuantity(){
        return quantity;
    }

    public String getSize() {
        return size;
    }
}
