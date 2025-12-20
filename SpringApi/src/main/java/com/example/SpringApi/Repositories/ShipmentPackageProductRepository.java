package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.ShipmentPackageProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentPackageProductRepository extends JpaRepository<ShipmentPackageProduct, Long> {
    List<ShipmentPackageProduct> findByShipmentPackageId(Long shipmentPackageId);
    void deleteByShipmentPackageId(Long shipmentPackageId);
}
