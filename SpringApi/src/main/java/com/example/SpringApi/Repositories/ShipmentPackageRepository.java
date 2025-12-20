package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.ShipmentPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentPackageRepository extends JpaRepository<ShipmentPackage, Long> {
    List<ShipmentPackage> findByShipmentId(Long shipmentId);
    void deleteByShipmentId(Long shipmentId);
}
