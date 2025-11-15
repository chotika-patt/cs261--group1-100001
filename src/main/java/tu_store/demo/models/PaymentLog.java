package tu_store.demo.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_logs")
public class PaymentLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    private Long paymentId;
    private String eventType;

    @Lob
    private String payload;

    private LocalDateTime createdAt = LocalDateTime.now();

    // ---------------- Getters & Setters ----------------
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
}
