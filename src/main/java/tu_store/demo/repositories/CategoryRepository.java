package tu_store.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tu_store.demo.models.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByName(String name);
}
