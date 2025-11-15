package tu_store.demo.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tu_store.demo.dto.InitiatePaymentRequest;
import tu_store.demo.dto.InitiatePaymentResponse;
import tu_store.demo.dto.PaymentStatusResponse;
import tu_store.demo.models.Payment;
import tu_store.demo.models.PaymentLog;
import tu_store.demo.models.enums.PaymentStatus;
import tu_store.demo.repositories.PaymentLogRepository;
import tu_store.demo.repositories.PaymentRepository;
import tu_store.demo.services.payment.ProviderClient;
import tu_store.demo.services.payment.ProviderClientFactory;
import tu_store.demo.services.payment.ProviderResponse;
import tu_store.demo.services.payment.ProviderWebhookEvent;
import tu_store.demo.services.payment.ProviderClientFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentLogRepository paymentLogRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private tu_store.demo.services.payment.ProviderClientFactory providerClientFactory;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public InitiatePaymentResponse initiatePayment(InitiatePaymentRequest req, Long userId) {
        // validation
        if (req.getOrderId() == null) throw new IllegalArgumentException("orderId required");
        if (req.getAmount() == null || req.getAmount() <= 0) throw new IllegalArgumentException("invalid amount");

        String idKey = req.getIdempotencyKey() != null ? req.getIdempotencyKey() : UUID.randomUUID().toString();

        // idempotency check
        Optional<Payment> existing = paymentRepository.findFirstByIdempotencyKey(idKey);
        if (existing.isPresent()) {
            return mapToInitiateResponse(existing.get());
        }

        // create payment
        Payment payment = new Payment();
        payment.setOrderId(req.getOrderId());
        payment.setUserId(userId);
        payment.setProvider(req.getProvider());
        payment.setMethod(req.getMethod());
        payment.setAmount(req.getAmount());
        payment.setCurrency(req.getCurrency() == null ? "THB" : req.getCurrency());
        payment.setStatus(PaymentStatus.INIT);
        payment.setIdempotencyKey(idKey);
        payment.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        if (req.getMetadata() != null) {
            try {
                payment.setMetadata(objectMapper.writeValueAsString(req.getMetadata()));
            } catch (JsonProcessingException e) {
                payment.setMetadata("{}");
            }
        }

        payment = paymentRepository.save(payment);

        log(payment.getPaymentId(), "PAYMENT_CREATED", payment.getMetadata());

        // call provider (mock or real)
        ProviderClient client = providerClientFactory.getClient(payment.getProvider());
        ProviderResponse pr;
        try {
            pr = client.createPayment(payment);
        } catch (Exception e) {
            // provider call failed: mark failed and return
            payment.setStatus(PaymentStatus.FAILED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            log(payment.getPaymentId(), "PROVIDER_CREATE_ERROR", e.getMessage());
            throw new RuntimeException("provider error: " + e.getMessage(), e);
        }

        // update based on provider response
        if (pr.getProviderRef() != null) payment.setPaymentRef(pr.getProviderRef());
        if (pr.isPending()) payment.setStatus(PaymentStatus.PENDING);
        else if (pr.isSuccess()) payment.setStatus(PaymentStatus.PAID);
        else payment.setStatus(PaymentStatus.FAILED);

        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        try {
            log(payment.getPaymentId(), "PROVIDER_RESPONSE", objectMapper.writeValueAsString(pr));
        } catch (JsonProcessingException e) {
            log(payment.getPaymentId(), "PROVIDER_RESPONSE", pr.toString());
        }

        // If provider says PAID instantly, update order
        if (PaymentStatus.PAID.equals(payment.getStatus())) {
            try {
                orderService.updateStatusToPaid(payment.getOrderId(), payment.getPaymentRef());
                log(payment.getPaymentId(), "ORDER_PAID", "orderId=" + payment.getOrderId());
            } catch (Exception e) {
                log(payment.getPaymentId(), "ORDER_UPDATE_FAILED", e.getMessage());
            }
        }

        return mapToInitiateResponse(payment);
    }

    public PaymentStatusResponse getStatus(Long paymentId, Long userId) {
        Payment p = paymentRepository.findById(paymentId).orElseThrow(() -> new EntityNotFoundException("payment not found"));
        if (!p.getUserId().equals(userId)) throw new AccessDeniedException("not your payment");
        return mapToStatus(p);
    }

    @Transactional
    public void cancelPayment(Long paymentId, Long userId) {
        Payment p = paymentRepository.findById(paymentId).orElseThrow(() -> new EntityNotFoundException("payment not found"));
        if (!p.getUserId().equals(userId)) throw new AccessDeniedException("not your payment");
        if (PaymentStatus.PAID.equals(p.getStatus()) || PaymentStatus.CANCELLED.equals(p.getStatus()))
            throw new IllegalStateException("cannot cancel");
        p.setStatus(PaymentStatus.CANCELLED);
        p.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(p);
        log(paymentId, "PAYMENT_CANCELLED", null);
    }

    @Transactional
    public ResponseEntity<?> handleWebhook(String provider, String signatureHeader, String body) {
        ProviderClient client = providerClientFactory.getClient(provider);

        boolean okSig;
        try {
            okSig = client.verifySignature(signatureHeader, body);
        } catch (Exception e) {
            log(null, "WEBHOOK_VERIFY_ERROR", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("errorCode", "INVALID_SIGNATURE", "message", "signature check failed"));
        }

        if (!okSig) {
            log(null, "WEBHOOK_SIGNATURE_INVALID", body);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("errorCode", "INVALID_SIGNATURE", "message", "signature mismatch"));
        }

        ProviderWebhookEvent event;
        try {
            event = client.parseWebhook(body);
        } catch (Exception e) {
            log(null, "WEBHOOK_PARSE_ERROR", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errorCode", "INVALID_PAYLOAD", "message", "could not parse webhook"));
        }

        // idempotency
        if (isDuplicateWebhook(event)) return ResponseEntity.ok(Map.of("message", "duplicate"));

        // find payment by ref or id
        Payment p = findByPaymentRefOrId(event);
        if (p == null) {
            log(null, "WEBHOOK_PAYMENT_NOT_FOUND", body);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("errorCode", "PAYMENT_NOT_FOUND", "message", "no payment"));
        }

        // map and update status
        PaymentStatus newStatus = PaymentStatus.valueOf(mapProviderStatusToInternal(event.getStatus()));
        p.setStatus(newStatus);
        if (event.getProviderRef() != null) p.setPaymentRef(event.getProviderRef());
        p.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(p);

        log(p.getPaymentId(), "WEBHOOK_PROCESSED", body);

        if (PaymentStatus.PAID.equals(newStatus)) {
            try {
                orderService.updateStatusToPaid(p.getOrderId(), p.getPaymentRef());
                log(p.getPaymentId(), "ORDER_PAID", "orderId=" + p.getOrderId());
            } catch (Exception e) {
                log(p.getPaymentId(), "ORDER_UPDATE_FAILED", e.getMessage());
            }
        }

        return ResponseEntity.ok(Map.of("message", "ok"));
    }

    private void log(Long paymentId, String eventType, String payload) {
        PaymentLog l = new PaymentLog();
        l.setPaymentId(paymentId);
        l.setEventType(eventType);
        l.setPayload(payload);
        paymentLogRepository.save(l);
    }

    public boolean verifyHmac(String secret, String signatureHeader, String body) {
        if (!StringUtils.hasText(signatureHeader)) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            String signatureHex = bytesToHex(digest);
            return MessageDigest.isEqual(signatureHex.getBytes(StandardCharsets.UTF_8), signatureHeader.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    private InitiatePaymentResponse mapToInitiateResponse(Payment p) {
        InitiatePaymentResponse resp = new InitiatePaymentResponse();
        resp.setPaymentId(p.getPaymentId());
        resp.setPaymentRef(p.getPaymentRef());
        resp.setStatus(p.getStatus() != null ? p.getStatus().name() : null);

        try {
            if (p.getMetadata() != null) {
                JsonNode meta = objectMapper.readTree(p.getMetadata());
                if (meta.has("paymentUrl")) {
                    resp.setPaymentUrl(meta.get("paymentUrl").asText());
                }
            }
        } catch (Exception ignored) {
        }

        return resp;
    }

    private PaymentStatusResponse mapToStatus(Payment p) {
        PaymentStatusResponse resp = new PaymentStatusResponse();
        resp.setPaymentId(p.getPaymentId());
        resp.setStatus(p.getStatus() != null ? p.getStatus().name() : null);
        resp.setPaymentRef(p.getPaymentRef());
        resp.setAmount(p.getAmount());
        resp.setCurrency(p.getCurrency());
        return resp;
    }

    private boolean isDuplicateWebhook(ProviderWebhookEvent event) {
        if (event.getEventId() == null) return false;

        return paymentLogRepository
                .findAll()
                .stream()
                .anyMatch(log ->
                        log.getPayload() != null &&
                                log.getPayload().contains(event.getEventId())
                );
    }

    private Payment findByPaymentRefOrId(ProviderWebhookEvent event) {
        if (event.getProviderRef() != null) {
            return paymentRepository.findAll()
                    .stream()
                    .filter(p -> event.getProviderRef().equals(p.getPaymentRef()))
                    .findFirst()
                    .orElse(null);
        }

        if (event.getPaymentId() != null) {
            return paymentRepository.findById(event.getPaymentId()).orElse(null);
        }
        return null;
    }

    private String mapProviderStatusToInternal(String providerStatus) {
        if (providerStatus == null) return "FAILED";

        switch (providerStatus.toUpperCase()) {
            case "PAID":
            case "SUCCESS":
                return "PAID";
            case "PENDING":
            case "WAITING":
                return "PENDING";
            case "EXPIRED":
            case "TIMEOUT":
                return "EXPIRED";
            case "FAILED":
            case "ERROR":
            default:
                return "FAILED";
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Transactional
    public void markPaymentExpired(Long paymentId) {
        Payment p = paymentRepository.findById(paymentId).orElse(null);
        if (p == null) return;

        // Only expire if still in progress
        if (p.getStatus() == null) return;
        if (!(p.getStatus() == PaymentStatus.PENDING || p.getStatus() == PaymentStatus.INIT)) return;

        p.setStatus(PaymentStatus.EXPIRED);
        p.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(p);

        log(p.getPaymentId(), "PAYMENT_EXPIRED", "expiredByScheduler");
    }
}
