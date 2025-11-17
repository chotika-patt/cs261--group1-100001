package tu_store.demo.repositories;

import tu_store.demo.models.Cart;
import tu_store.demo.models.CartItem;
import tu_store.demo.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // ⭐ ใช้ size ในการ match ด้วย
    Optional<CartItem> findByCartAndProductAndSize(Cart cart, Product product, String size);
}
