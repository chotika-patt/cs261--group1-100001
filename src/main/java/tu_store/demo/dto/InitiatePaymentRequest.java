package tu_store.demo.dto;

import java.util.Map;

public class InitiatePaymentRequest {
    private Long orderId;
    private Double amount;
    private String currency; // optional
    private String provider; // "MOCK", "BANK_X", "CARD"
    private String method;   // "QR", "IBANK", "CARD"
    private String idempotencyKey; // client-provided or server-generated
    private Map<String,String> metadata;

    //Getters & Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
}
