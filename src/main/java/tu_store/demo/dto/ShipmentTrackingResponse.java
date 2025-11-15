package tu_store.demo.dto;

import java.time.LocalDateTime;

import tu_store.demo.models.enums.ShipmentTrackingStatus;

public class ShipmentTrackingResponse {
    private Long OrderId;

    private String trackingNumber;

    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    private ShipmentTrackingStatus status;

    public ShipmentTrackingResponse(){}

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public void setOrderId(Long orderId) {
        OrderId = orderId;
    }

    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }

    public void setStatus(ShipmentTrackingStatus status) {
        this.status = status;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public Long getOrderId() {
        return OrderId;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public ShipmentTrackingStatus getStatus() {
        return status;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }
}
