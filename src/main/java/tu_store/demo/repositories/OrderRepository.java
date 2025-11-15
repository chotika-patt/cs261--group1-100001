package tu_store.demo.repositories;

import tu_store.demo.models.Order;
import tu_store.demo.models.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>  {
    
    Order findFirstByCartCartId(Long id);
    Order findFirstByOrderId(Long id);

    // ==== Buyer ====
    List<Order> findAllByBuyerUserId(Long id);
    Order findFirstByOrderIdAndBuyerUserId(Long orderId, Long userId);
    Page<Order> findAllByBuyerUserId(Long customerId, Pageable pageable);
    Page<Order> findAllByBuyerUserIdAndStatus(Long customerId, String status, Pageable pageable);

    @Query("""
        SELECT o FROM Order o
        WHERE o.buyer.userId = :buyerId
        AND (:status IS NULL OR o.status = :status)
        AND (:search IS NULL OR CAST(o.orderId AS string) LIKE CONCAT('%', :search, '%'))
        AND (:startDate IS NULL OR o.createdAt >= :startDate)
        AND (:endDate IS NULL OR o.createdAt <= :endDate)
    """)
    Page<Order> findAllByBuyerIdWithFilters(
            @Param("buyerId") Long buyerId,
            @Param("search") String search,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // ==== Seller ====
    List<Order> findAllBySellerUserId(Long id);
    Order findFirstByOrderIdAndSellerUserId(Long orderId, Long userId);
    Page<Order> findAllBySellerUserId(Long sellerId, Pageable pageable);
    Page<Order> findAllBySellerUserIdAndStatus(Long sellerId, OrderStatus status, Pageable pageable);

    @Query("""
        SELECT o FROM Order o
        WHERE o.seller.userId = :sellerId
        AND (:status IS NULL OR o.status = :status)
        AND (:search IS NULL OR CAST(o.orderId AS string) LIKE CONCAT('%', :search, '%'))
        AND (:startDate IS NULL OR o.createdAt >= :startDate)
        AND (:endDate IS NULL OR o.createdAt <= :endDate)
    """)
    Page<Order> findAllBySellerIdWithFilters(
            @Param("sellerId") Long sellerId,
            @Param("search") String search,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT COUNT(o) FROM Order o WHERE o.seller.userId = :sellerId")
    Long totalOrders(@Param("sellerId") Long sellerId);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.seller.userId = :sellerId AND o.status = :status")
    Double totalPriceByStatus(@Param("sellerId") Long sellerId,
                              @Param("status") OrderStatus status);

    @Query("""
        SELECT COUNT(o) FROM Order o
        WHERE o.seller.userId = :sellerId
        AND (:status IS NULL OR o.status = :status)
        AND (:search IS NULL OR CAST(o.orderId AS string) LIKE CONCAT('%', :search, '%'))
        AND (:startDate IS NULL OR o.createdAt >= :startDate)
        AND (:endDate IS NULL OR o.createdAt <= :endDate)
    """)
    Long totalOrdersFiltered(
            @Param("sellerId") Long sellerId,
            @Param("search") String search,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o
        WHERE o.seller.userId = :sellerId
        AND (:status IS NULL OR o.status = :status)
        AND (:search IS NULL OR CAST(o.orderId AS string) LIKE CONCAT('%', :search, '%'))
        AND (:startDate IS NULL OR o.createdAt >= :startDate)
        AND (:endDate IS NULL OR o.createdAt <= :endDate)
    """)
    Double totalPriceFiltered(
            @Param("sellerId") Long sellerId,
            @Param("search") String search,
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT o FROM Order o 
        JOIN o.items i
        WHERE i.product.productId = :productId
        AND o.buyer.userId = :buyerId
        AND o.status IN (tu_store.demo.models.enums.OrderStatus.PAID, 
                        tu_store.demo.models.enums.OrderStatus.COMPLETED)
    """)
    Order findPurchasedOrder(@Param("productId") Long productId, 
                            @Param("buyerId") Long buyerId);

}
