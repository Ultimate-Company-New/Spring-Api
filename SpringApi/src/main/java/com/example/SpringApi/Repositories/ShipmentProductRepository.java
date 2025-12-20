package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.ShipmentProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentProductRepository extends JpaRepository<ShipmentProduct, Long> {
    List<ShipmentProduct> findByShipmentId(Long shipmentId);
    void deleteByShipmentId(Long shipmentId);
}
