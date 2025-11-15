package tu_store.demo.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import tu_store.demo.models.enums.OrderStatus;

@Entity
@Table(name = "order_status_log")
public class OrderStatusLog  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private OrderStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status")
    private OrderStatus newStatus;

    private LocalDateTime updatedAt = LocalDateTime.now();

    public OrderStatusLog(){}
    public OrderStatusLog(Order order){
        this.order = order;
    }   

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setOldStatus(OrderStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public void setNewStatus(OrderStatus newStatus) {
        this.newStatus = newStatus;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getLogId() {
        return logId;
    }

    public OrderStatus getNewStatus() {
        return newStatus;
    }

    public OrderStatus getOldStatus() {
        return oldStatus;
    }

    public Long getOrderId() {
        return order.getOrderId();
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
