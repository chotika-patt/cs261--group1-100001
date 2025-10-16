package tu_store.demo.dto;

public class CartItemDto {
    private Long productId;
    private int quantity;
    private long price;

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(long price){
        this.price = price;
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public long getPrice(){
        return this.price;
    }
}
