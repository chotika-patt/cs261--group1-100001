package tu_store.demo.dto;

public class CartItemDto {

    private Long productId;
    private int quantity;
    private long price;

    // ⭐ NEW
    private String size;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    // ⭐ NEW
    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
