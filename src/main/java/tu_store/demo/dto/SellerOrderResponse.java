package tu_store.demo.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import tu_store.demo.models.enums.OrderStatus;
import tu_store.demo.models.enums.PaymentStatus;
import tu_store.demo.models.enums.ShipmentTrackingStatus;

public class SellerOrderResponse {
    private Long orderId;
    private OrderStatus status;             // order-level status (PAID / COMPLETED ...)
    private PaymentStatus paymentStatus;    // payment status if known (PAID/PENDING/etc)
    private Integer quantity;
    private String trackingCode;
    private Double totalPrice;
    private Long buyerId;
    private List<SellerOrderItemResponse> items = new ArrayList<>();
    private LocalDateTime createdAt;

    // ----- NEW FIELDS -----
    private String shopName;

    private String buyerName;
    private String buyerPhone;
    private String buyerAddress;

    private String paymentMethod;
    private String paymentRef;

    private ShipmentTrackingStatus shipmentStatus;

    // Product summary (first product)
    private String productName;
    private String productImageUrl;
    private Double productPrice;

    public SellerOrderResponse(){}

    // ===== setters =====
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setItems(List<SellerOrderItemResponse> items) { this.items = items; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
    public void setTrackingCode(String trackingCode) { this.trackingCode = trackingCode; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }

    public void setShopName(String shopName) { this.shopName = shopName; }

    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public void setBuyerPhone(String buyerPhone) { this.buyerPhone = buyerPhone; }
    public void setBuyerAddress(String buyerAddress) { this.buyerAddress = buyerAddress; }

    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setPaymentRef(String paymentRef) { this.paymentRef = paymentRef; }

    public void setShipmentStatus(ShipmentTrackingStatus shipmentStatus) { this.shipmentStatus = shipmentStatus; }

    public void setProductName(String productName) { this.productName = productName; }
    public void setProductImageUrl(String productImageUrl) { this.productImageUrl = productImageUrl; }
    public void setProductPrice(Double productPrice) { this.productPrice = productPrice; }

    // ===== getters =====
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<SellerOrderItemResponse> getItems() { return items; }
    public Long getOrderId() { return orderId; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public Integer getQuantity() { return quantity; }
    public OrderStatus getStatus() { return status; }
    public Double getTotalPrice() { return totalPrice; }
    public String getTrackingCode() { return trackingCode; }
    public Long getBuyerId() { return buyerId; }

    public String getShopName() { return shopName; }

    public String getBuyerName() { return buyerName; }
    public String getBuyerPhone() { return buyerPhone; }
    public String getBuyerAddress() { return buyerAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentRef() { return paymentRef; }

    public ShipmentTrackingStatus getShipmentStatus() { return shipmentStatus; }

    public String getProductName() { return productName; }
    public String getProductImageUrl() { return productImageUrl; }
    public Double getProductPrice() { return productPrice; }
}

