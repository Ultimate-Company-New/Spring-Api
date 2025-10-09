package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.ShipmentPackageMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ShipmentPackageMapping entity operations.
 */
@Repository
public interface ShipmentPackageMappingRepository extends JpaRepository<ShipmentPackageMapping, Long> {
    
    List<ShipmentPackageMapping> findByShipmentId(Long shipmentId);
    
    List<ShipmentPackageMapping> findByPackageId(Long packageId);
}

