package tu_store.demo.services.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tu_store.demo.models.Payment;
import tu_store.demo.models.enums.PaymentStatus;
import tu_store.demo.repositories.PaymentRepository;
import tu_store.demo.services.PaymentService;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PaymentExpiryJob {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentService paymentService;

    // run every 60s
    @Scheduled(fixedDelayString = "${payments.expiry.check_interval_ms:60000}")
    public void expirePayments() {
        LocalDateTime now = LocalDateTime.now();
        List<Payment> expired = paymentRepository.findByStatusAndExpiresAtBefore(PaymentStatus.PENDING, now);

        if (expired == null || expired.isEmpty()) return;

        for (Payment p : expired) {
            try {
                paymentService.markPaymentExpired(p.getPaymentId());
            } catch (Exception e) {
                System.err.println("[PAYMENT_EXPIRY] failed on payment " + p.getPaymentId() + " : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
