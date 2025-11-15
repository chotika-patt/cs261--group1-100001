package tu_store.demo.services.payment;

import tu_store.demo.models.Payment;
import tu_store.demo.services.payment.ProviderResponse;
import tu_store.demo.services.payment.ProviderWebhookEvent;

public interface ProviderClient {
    ProviderResponse createPayment(Payment payment) throws Exception;
    boolean verifySignature(String signatureHeader, String body);
    ProviderWebhookEvent parseWebhook(String body);
}
