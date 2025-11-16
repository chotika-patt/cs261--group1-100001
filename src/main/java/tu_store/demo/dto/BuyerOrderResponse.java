package tu_store.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import tu_store.demo.models.enums.OrderStatus;
import tu_store.demo.models.enums.PaymentStatus;
import tu_store.demo.models.enums.ShipmentTrackingStatus;

public class BuyerOrderResponse {
    private Long orderId;
    private String productName;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private ShipmentTrackingStatus sTrackingStatus;
    private String shopName;
    private Integer quantity;
    private String trackingCode;
    private String imagePath;
    private Double totalPrice;

    private List<BuyerOrderItemResponse> items = new ArrayList<>();

    private LocalDateTime createdAt;

    public BuyerOrderResponse(){}

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    public String getImagePath() {
        return imagePath;
    }

    public void setItems(List<BuyerOrderItemResponse> items) {
        this.items = items;
    }
    
    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setsTrackingStatus(ShipmentTrackingStatus sTrackingStatus) {
        this.sTrackingStatus = sTrackingStatus;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setTrackingCode(String trackingCode) {
        this.trackingCode = trackingCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<BuyerOrderItemResponse> getItems() {
        return items;
    }

    public Long getOrderId() {
        return orderId;
    }
    
    public String getProductName() {
        return productName;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public String getTrackingCode() {
        return trackingCode;
    }

    public ShipmentTrackingStatus getsTrackingStatus() {
        return sTrackingStatus;
    }

    public String getShopName() {
        return shopName;
    }
}
