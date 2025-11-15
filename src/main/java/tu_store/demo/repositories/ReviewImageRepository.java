package tu_store.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tu_store.demo.models.ReviewImage;


@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    
}
