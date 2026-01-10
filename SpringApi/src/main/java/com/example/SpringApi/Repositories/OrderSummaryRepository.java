package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.OrderSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderSummaryRepository extends JpaRepository<OrderSummary, Long> {
    Optional<OrderSummary> findByEntityTypeAndEntityId(String entityType, Long entityId);
    Optional<OrderSummary> findByOrderSummaryIdAndClientId(Long orderSummaryId, Long clientId);
    
    /**
     * Find order summary by purchase order ID.
     * Convenience method that uses entityType='PURCHASE_ORDER' and entityId=purchaseOrderId.
     */
    default Optional<OrderSummary> findByPurchaseOrderId(Long purchaseOrderId) {
        return findByEntityTypeAndEntityId("PURCHASE_ORDER", purchaseOrderId);
    }
}
