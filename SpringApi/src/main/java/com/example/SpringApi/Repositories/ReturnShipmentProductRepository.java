package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.ReturnShipmentProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnShipmentProductRepository extends JpaRepository<ReturnShipmentProduct, Long> {
    List<ReturnShipmentProduct> findByReturnShipmentId(Long returnShipmentId);
    List<ReturnShipmentProduct> findByReturnShipmentIdAndClientId(Long returnShipmentId, Long clientId);
    List<ReturnShipmentProduct> findByReturnShipmentIdAndIsDeletedFalse(Long returnShipmentId);
}
