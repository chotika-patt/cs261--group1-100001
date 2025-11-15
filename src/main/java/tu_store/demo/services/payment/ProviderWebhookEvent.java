package tu_store.demo.services.payment;

public class ProviderWebhookEvent {
    private String eventId;
    private String providerRef;
    private String status;
    private Long paymentId;
    private String raw;

    public String getEventId() {return eventId;}
    public void setEventId(String eventId) {this.eventId = eventId;}

    public String getProviderRef() {return providerRef;}
    public void setProviderRef(String providerRef) {this.providerRef = providerRef;}

    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}

    public Long getPaymentId() {return paymentId;}
    public void setPaymentId(Long paymentId) {this.paymentId = paymentId;}

    public String getRaw() {return raw;}
    public void setRaw(String raw) {this.raw = raw;}
}
