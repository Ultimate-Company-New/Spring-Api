package springapi.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springapi.models.databasemodels.ReturnShipmentProduct;

/** Defines the return shipment product repository contract. */
@Repository
public interface ReturnShipmentProductRepository
    extends JpaRepository<ReturnShipmentProduct, Long> {
  List<ReturnShipmentProduct> findByReturnShipmentId(Long returnShipmentId);

  List<ReturnShipmentProduct> findByReturnShipmentIdAndClientId(
      Long returnShipmentId, Long clientId);

  List<ReturnShipmentProduct> findByReturnShipmentIdAndIsDeletedFalse(Long returnShipmentId);
}
