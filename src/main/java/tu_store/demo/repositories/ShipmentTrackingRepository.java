package tu_store.demo.repositories;

import tu_store.demo.models.ShipmentTracking;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ShipmentTrackingRepository extends JpaRepository<ShipmentTracking, Long>  {

}
