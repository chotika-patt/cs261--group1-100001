package tu_store.demo.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import tu_store.demo.models.enums.OrderStatus;
import tu_store.demo.models.enums.ShipmentTrackingStatus;

@Entity
@Table(name = "shipment_trackings")
public class ShipmentTracking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    private String trackingNumber;

    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentTrackingStatus status = ShipmentTrackingStatus.PREPARING;

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public ShipmentTracking(){}

    public ShipmentTracking(Order order){
        setOrder(order);
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public void setStatus(ShipmentTrackingStatus status) {
        this.status = status;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }

    public Long getId() {
        return id;
    }

    public ShipmentTrackingStatus getStatus() {
        return status;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public Long getOrderId() {
        return order.getOrderId();
    }
}
