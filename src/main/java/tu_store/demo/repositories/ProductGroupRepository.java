package tu_store.demo.repositories;
import tu_store.demo.models.Cart;
import tu_store.demo.models.ProductGroup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductGroupRepository extends JpaRepository<ProductGroup, Long>  {
}
