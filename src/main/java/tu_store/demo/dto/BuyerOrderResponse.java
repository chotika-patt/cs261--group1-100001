package tu_store.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import tu_store.demo.models.enums.OrderStatus;
import tu_store.demo.models.enums.PaymentStatus;

public class BuyerOrderResponse {
    private Long orderId;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private Integer quantity;
    private String trackingCode;
    private Double totalPrice;

    private List<BuyerOrderItemResponse> items = new ArrayList<>();

    private LocalDateTime createdAt;

    public BuyerOrderResponse(){}

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setItems(List<BuyerOrderItemResponse> items) {
        this.items = items;
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
}
