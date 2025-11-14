package tu_store.demo.repositories;
import tu_store.demo.models.Cart;
import tu_store.demo.models.Order;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long>  {
    Order findFirstByCartCartId(Long id);
    Order findFirstByOrderId(Long id);

    List<Order> findAllByBuyerUserId(Long id);

    Order findFirstByOrderIdAndBuyerUserId(Long orderId, Long userId);

    Page<Order> findAllByBuyerUserId(Long customerId, Pageable pageable);
    Page<Order> findAllByBuyerUserIdAndStatus(Long customerId, String status, Pageable pageable);
}
