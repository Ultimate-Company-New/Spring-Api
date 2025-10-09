package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.PurchaseOrderPaymentMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for PurchaseOrderPaymentMapping entity operations.
 */
@Repository
public interface PurchaseOrderPaymentMappingRepository extends JpaRepository<PurchaseOrderPaymentMapping, Long> {
}

