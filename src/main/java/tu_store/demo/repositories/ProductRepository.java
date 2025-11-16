package tu_store.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tu_store.demo.models.*;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 🔍 Search Product with optional filters:
     * - name LIKE %name%
     * - category
     * - price range
     * - rating >= rating
     * - inStock (stock > 0)
     *
     *  (Sorting จะทำใน Service เพื่อความยืดหยุ่น)
     */
    @Query("""
    SELECT p FROM Product p
    WHERE (:name IS NULL OR :name = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
      AND (:category IS NULL OR p.category = :category)
      AND (:minPrice IS NULL OR p.price >= :minPrice)
      AND (:maxPrice IS NULL OR p.price <= :maxPrice)
      AND (:rating IS NULL OR p.ratingAvg >= :rating)
      AND (
            :inStock IS NULL OR 
            (:inStock = TRUE AND p.stock > 0) OR
            (:inStock = FALSE AND p.stock = 0)
          )
""")
List<Product> searchProducts(
        @Param("name") String name,
        @Param("category") Category category,
        @Param("minPrice") Long minPrice,
        @Param("maxPrice") Long maxPrice,
        @Param("rating") Double rating,
        @Param("inStock") Boolean inStock
);

    // Query สำหรับข้อมูลเฉพาะทาง

    Product findFirstByName(String name);

    Product findFirstByProductId(long id);

    List<Product> findAllBySellerUserId(Long id);

    List<Product> findBySeller(User seller);

    List<Product> findByCategory(Category category);

    // ใช้ index rating (สำหรับสินค้าที่ rating ยังไม่ถูกคำนวณ)
    List<Product> findAllByRatingAvgIsNullOrRatingCountIsNull();

    // ใช้ index stock → inStock filter
    List<Product> findByStockGreaterThan(int stock);

    // ใช้ index sold_count → best-selling
    List<Product> findAllByOrderBySoldCountDesc();

    // ใช้ index updated_at → latest update
    List<Product> findAllByOrderByUpdatedAtDesc();

    // ใช้ index created_at → newest items
    List<Product> findAllByOrderByCreatedAtDesc();

    Optional<Product> findByProductIdAndSellerUserId(Long productId, Long sellerId);
}
