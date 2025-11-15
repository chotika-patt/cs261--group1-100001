package tu_store.demo.services.payment;

import org.springframework.stereotype.Component;
import tu_store.demo.models.Payment;
import com.fasterxml.jackson.databind.ObjectMapper;
import tu_store.demo.services.payment.ProviderClient;
import tu_store.demo.services.payment.ProviderResponse;
import tu_store.demo.services.payment.ProviderWebhookEvent;

@Component("MOCK")
public class MockProviderClient implements ProviderClient {

    @Override
    public ProviderResponse createPayment(Payment payment) {
        ProviderResponse r = new ProviderResponse();
        r.setProviderRef("MOCK-" + System.currentTimeMillis());
        r.setPending(true);
        r.setSuccess(false);
        r.setPaymentUrl("https://mock.pay/" + r.getProviderRef());
        try {
            r.setRaw(new ObjectMapper().writeValueAsString(payment));
        } catch (Exception ignored){}
        return r;
    }

    @Override
    public boolean verifySignature(String signatureHeader, String body) {
        // Accept MOCK-SIGN as valid signature in development
        return "MOCK-SIGN".equals(signatureHeader) || signatureHeader == null;
    }

    @Override
    public ProviderWebhookEvent parseWebhook(String body){
        try {
            // Expect simple JSON: { "eventId":"e1", "providerRef":"MOCK-xxx", "status":"PAID", "paymentId": 12 }
            ObjectMapper m = new ObjectMapper();
            var node = m.readTree(body);

            ProviderWebhookEvent ev = new ProviderWebhookEvent();
            if (node.has("eventId")) ev.setEventId(node.get("eventId").asText());
            if (node.has("providerRef")) ev.setProviderRef(node.get("providerRef").asText());
            if (node.has("status")) ev.setStatus(node.get("status").asText());
            if (node.has("paymentId")) ev.setPaymentId(node.get("paymentId").asLong());
            ev.setRaw(body);
            return ev;
        } catch (Exception e){
            throw new RuntimeException("Failed to parse webhook: " + e.getMessage(), e);
        }
    }
}
