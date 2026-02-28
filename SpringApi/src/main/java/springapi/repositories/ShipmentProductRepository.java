package springapi.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import springapi.models.databasemodels.ShipmentProduct;

/** Defines the shipment product repository contract. */
@Repository
public interface ShipmentProductRepository extends JpaRepository<ShipmentProduct, Long> {
  List<ShipmentProduct> findByShipmentId(Long shipmentId);

  void deleteByShipmentId(Long shipmentId);

  /** Batch fetch shipment products for multiple shipments with product eagerly loaded. */
  @Query(
      "SELECT sp FROM ShipmentProduct sp JOIN FETCH sp.product WHERE sp.shipmentId IN :shipmentIds")
  List<ShipmentProduct> findByShipmentIdInWithProduct(@Param("shipmentIds") List<Long> shipmentIds);
}
