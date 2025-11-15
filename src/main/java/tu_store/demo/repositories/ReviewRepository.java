package tu_store.demo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tu_store.demo.models.Review;


@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByProductProductId(Long id);
    List<Review> findTop100ByOrderByCreatedAtDesc();
    boolean existsByBuyerUserIdAndProductProductId(Long userId, Long productId);
}
