package tu_store.demo.repositories;
import tu_store.demo.models.OrderStatusLog;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OrderStatusLogRepository extends JpaRepository<OrderStatusLog, Long>  {
    List<OrderStatusLog> findTop100ByOrderByUpdatedAtDesc();
    List<OrderStatusLog> findAllByOrderOrderId(Long id);
    List<OrderStatusLog> findAllByOrderUserUserId(Long id);
}