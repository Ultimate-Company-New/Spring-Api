package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.ShipmentPackageMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for ShipmentPackageMapping entity operations.
 */
@Repository
public interface ShipmentPackageMappingRepository extends JpaRepository<ShipmentPackageMapping, Long> {
}

