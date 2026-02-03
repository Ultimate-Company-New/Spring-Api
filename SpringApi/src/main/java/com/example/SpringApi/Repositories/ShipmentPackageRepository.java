package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.ShipmentPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentPackageRepository extends JpaRepository<ShipmentPackage, Long> {
    List<ShipmentPackage> findByShipmentId(Long shipmentId);
    void deleteByShipmentId(Long shipmentId);

    /**
     * Batch fetch shipment packages for multiple shipments with package info eagerly loaded.
     */
    @Query("SELECT sp FROM ShipmentPackage sp JOIN FETCH sp.packageInfo WHERE sp.shipmentId IN :shipmentIds")
    List<ShipmentPackage> findByShipmentIdInWithPackage(@Param("shipmentIds") List<Long> shipmentIds);
}
