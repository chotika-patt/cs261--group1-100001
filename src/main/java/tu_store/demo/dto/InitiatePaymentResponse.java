package tu_store.demo.dto;

public class InitiatePaymentResponse {
    private Long paymentId;
    private String status; // PENDING
    private String paymentUrl; // for redirect or QR link
    private String paymentRef; // optional1

    //Getter Setter
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentUrl() { return paymentUrl; }
    public void setPaymentUrl(String paymentUrl) { this.paymentUrl = paymentUrl; }

    public String getPaymentRef() { return paymentRef; }
    public void setPaymentRef(String paymentRef) { this.paymentRef = paymentRef; }
}
