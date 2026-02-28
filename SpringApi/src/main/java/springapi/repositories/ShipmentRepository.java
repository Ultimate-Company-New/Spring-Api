package springapi.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import springapi.models.databasemodels.Shipment;

/** Defines the shipment repository contract. */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
  List<Shipment> findByOrderSummaryId(Long orderSummaryId);

  List<Shipment> findByOrderSummaryIdAndClientId(Long orderSummaryId, Long clientId);

  Shipment findByShipmentIdAndClientId(Long shipmentId, Long clientId);

  /**
   * Batch fetch shipments for multiple order summaries with pickup location and address eagerly.
   * loaded.
   */
  @Query(
      "SELECT DISTINCT s FROM Shipment s LEFT JOIN FETCH s.pickupLocation "
          + "pl LEFT JOIN FETCH pl.address "
          + "WHERE s.orderSummaryId IN :orderSummaryIds")
  List<Shipment> findByOrderSummaryIdInWithPickupLocation(
      @Param("orderSummaryIds") List<Long> orderSummaryIds);
}
