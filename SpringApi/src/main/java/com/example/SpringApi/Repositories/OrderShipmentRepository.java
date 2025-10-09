package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.OrderShipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for OrderShipment entity operations.
 */
@Repository
public interface OrderShipmentRepository extends JpaRepository<OrderShipment, Long> {
}

