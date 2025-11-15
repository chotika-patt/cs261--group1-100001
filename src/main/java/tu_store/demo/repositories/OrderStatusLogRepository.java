package tu_store.demo.repositories;

import tu_store.demo.models.OrderStatusLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderStatusLogRepository extends JpaRepository<OrderStatusLog, Long>  {

    List<OrderStatusLog> findTop100ByOrderByUpdatedAtDesc();

    // ดึง log ทั้งหมดตาม Order
    List<OrderStatusLog> findAllByOrderOrderId(Long orderId);

    // ดึง log ตาม Buyer (user ที่ซื้อ)
    List<OrderStatusLog> findAllByOrderBuyerUserId(Long buyerId);

    // ดึง log ตาม Seller (user ที่ขาย)
    List<OrderStatusLog> findAllByOrderSellerUserId(Long sellerId);
}
