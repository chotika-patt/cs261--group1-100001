package tu_store.demo.dto;

import jakarta.persistence.Column;
import tu_store.demo.models.Product;

public class SellerOrderItemResponse {
    private Long productId;

    private Integer quantity;

    private Double totalPrice;

    private String productName;

    public SellerOrderItemResponse(){}

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

    public String getProductName() {return productName;}
    public void setProductName(String productName) {this.productName = productName;}
}
