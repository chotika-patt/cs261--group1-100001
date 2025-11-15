package tu_store.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tu_store.demo.models.Payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findFirstByIdempotencyKey(String idempotencyKey);

    List<Payment> findByStatusAndExpiresAtBefore(tu_store.demo.models.enums.PaymentStatus status, LocalDateTime cutoff);
}