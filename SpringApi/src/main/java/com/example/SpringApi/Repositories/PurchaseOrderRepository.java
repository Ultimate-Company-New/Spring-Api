package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for PurchaseOrder entity operations.
 */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
}

