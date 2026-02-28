package springapi.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springapi.models.databasemodels.ReturnShipment;

/** Defines the return shipment repository contract. */
@Repository
public interface ReturnShipmentRepository extends JpaRepository<ReturnShipment, Long> {
  List<ReturnShipment> findByShipmentId(Long shipmentId);

  List<ReturnShipment> findByShipmentIdAndClientId(Long shipmentId, Long clientId);

  List<ReturnShipment> findByShipmentIdAndClientIdAndIsDeletedFalse(Long shipmentId, Long clientId);

  ReturnShipment findByReturnShipmentIdAndClientId(Long returnShipmentId, Long clientId);
}
