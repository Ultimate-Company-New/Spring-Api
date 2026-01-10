package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.ReturnShipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnShipmentRepository extends JpaRepository<ReturnShipment, Long> {
    List<ReturnShipment> findByShipmentId(Long shipmentId);
    List<ReturnShipment> findByShipmentIdAndClientId(Long shipmentId, Long clientId);
    List<ReturnShipment> findByShipmentIdAndClientIdAndIsDeletedFalse(Long shipmentId, Long clientId);
    ReturnShipment findByReturnShipmentIdAndClientId(Long returnShipmentId, Long clientId);
}
