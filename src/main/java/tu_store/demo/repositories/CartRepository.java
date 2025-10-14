package tu_store.demo.repositories;
import tu_store.demo.models.Cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CartRepository extends JpaRepository<Cart, Long>  {
    Cart findFirstByCartId(long id);
    Cart findFirstByUserUserId(long id);
    Cart findFirstBySessionId(String id);
}
