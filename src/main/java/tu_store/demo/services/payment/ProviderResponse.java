package tu_store.demo.services.payment;

public class ProviderResponse {
    private String providerRef;
    private boolean success;
    private String raw;
    private boolean pending;
    private String paymentUrl;

    public String getProviderRef() {return providerRef;}
    public void setProviderRef(String providerRef) {this.providerRef = providerRef;}

    public boolean isSuccess() {return success;}
    public void setSuccess(boolean success) {this.success = success;}

    public String getRaw() {return raw;}
    public void setRaw(String raw) {this.raw = raw;}

    public boolean isPending() {return pending;}
    public void setPending(boolean pending) {this.pending = pending;}

    public String getPaymentUrl() {return paymentUrl;}
    public void setPaymentUrl(String paymentUrl) {this.paymentUrl = paymentUrl;}
}
