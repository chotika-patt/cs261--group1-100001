package tu_store.demo.repositories;
import tu_store.demo.models.Cart;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CartRepository extends JpaRepository<Cart, Long>  {
    Cart findFirstByCartId(Long id);
    Cart findFirstByUserUserId(Long id);
    Cart findFirstBySessionId(String id);

    List<Cart> findAllByUserUserId(Long id);
    Cart findFirstByUserUserIdAndIsActiveTrue(Long userId);
    Cart findFirstBySessionIdAndIsActiveTrue(String id);
}
