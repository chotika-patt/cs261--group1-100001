package tu_store.demo.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tu_store.demo.models.Review;


@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Review findFirstByReviewId(Long reviewId);

    List<Review> findAllByProductProductId(Long id);
    List<Review> findTop100ByOrderByCreatedAtDesc();
    boolean existsByBuyerUserIdAndProductProductId(Long userId, Long productId);
    boolean existsByBuyerUserIdAndProductProductIdAndOrderOrderId(Long userId, Long productId, Long OrderId);

    @Query("""
        SELECT r FROM Review r
        WHERE r.product.productId = :productId
        AND (:minRating IS NULL OR r.rating >= :minRating)
        AND (:maxRating IS NULL OR r.rating <= :maxRating)
        AND (:search IS NULL OR r.comment LIKE CONCAT('%', :search, '%'))
        AND (:startDate IS NULL OR r.createdAt >= :startDate)
        AND (:endDate IS NULL OR r.createdAt <= :endDate)
    """)
    Page<Review> findAllByProductIdWithFilters(
            @Param("productId") Long productId,
            @Param("search") String search,
            @Param("minRating") Integer minRating,
            @Param("maxRating") Integer maxRating,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
