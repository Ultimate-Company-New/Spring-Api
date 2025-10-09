package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.PurchaseOrderPaymentMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for PurchaseOrderPaymentMapping entity operations.
 */
@Repository
public interface PurchaseOrderPaymentMappingRepository extends JpaRepository<PurchaseOrderPaymentMapping, Long> {
    
    List<PurchaseOrderPaymentMapping> findByPurchaseOrderId(Long purchaseOrderId);
    
    List<PurchaseOrderPaymentMapping> findByPaymentId(Long paymentId);
    
    List<PurchaseOrderPaymentMapping> findByPurchaseOrderIdOrderByPaymentSequenceAsc(Long purchaseOrderId);
}

