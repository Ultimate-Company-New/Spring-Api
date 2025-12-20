package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    List<Shipment> findByOrderSummaryId(Long orderSummaryId);
    List<Shipment> findByOrderSummaryIdAndClientId(Long orderSummaryId, Long clientId);
}
