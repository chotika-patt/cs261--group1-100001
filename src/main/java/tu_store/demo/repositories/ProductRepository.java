package tu_store.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tu_store.demo.models.Product;
import tu_store.demo.models.Category;
import tu_store.demo.models.ProductStatus;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
   @Query("""
    SELECT p FROM Product p
    WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
      AND (:category IS NULL OR p.category = :category)
      AND (:status IS NULL OR p.status = :status)
      AND (:minPrice IS NULL OR p.price >= :minPrice)
      AND (:maxPrice IS NULL OR p.price <= :maxPrice)
""")
List<Product> searchProducts(
    @Param("name") String name,
    @Param("category") Category category,
    @Param("status") ProductStatus status,
    @Param("minPrice") Long minPrice,
    @Param("maxPrice") Long maxPrice
);

  Product findFirstByName(String name);
  Product findFirstByProductId(long id);
  List<Product> findAllBySellerUserId(Long id);
}