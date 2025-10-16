package tu_store.demo.dto;

public class AddToCartRequest {
    private Long productId;
    private int quantity;
    
    public void setProductId(long productId){
        this.productId = productId;
    }
    public void setQuantity(int quantity){
        this.quantity = quantity;
    }
    public Long getProductId(){
        return productId;
    }
    public int getQuantity(){
        return quantity;
    }
}
