package tu_store.demo.dto;

import jakarta.persistence.Column;
import tu_store.demo.models.Product;

public class BuyerOrderItemResponse {
    private Long productId;

    private Integer quantity;

    private Double totalPrice;

    public BuyerOrderItemResponse(){}

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }
}
