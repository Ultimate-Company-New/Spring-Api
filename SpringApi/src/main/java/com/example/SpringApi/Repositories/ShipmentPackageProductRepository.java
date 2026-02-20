package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.ShipmentPackageProduct;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipmentPackageProductRepository
    extends JpaRepository<ShipmentPackageProduct, Long> {
  List<ShipmentPackageProduct> findByShipmentPackageId(Long shipmentPackageId);

  void deleteByShipmentPackageId(Long shipmentPackageId);

  /**
   * Batch fetch shipment package products for multiple shipment packages with product eagerly
   * loaded.
   */
  @Query(
      "SELECT spp FROM ShipmentPackageProduct spp JOIN FETCH spp.product WHERE spp.shipmentPackageId IN :shipmentPackageIds")
  List<ShipmentPackageProduct> findByShipmentPackageIdInWithProduct(
      @Param("shipmentPackageIds") List<Long> shipmentPackageIds);
}
