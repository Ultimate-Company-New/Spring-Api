package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.PurchaseOrderQuantityPriceMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository interface for PurchaseOrderQuantityPriceMap entity operations.
 */
@Repository
public interface PurchaseOrderQuantityPriceMapRepository extends JpaRepository<PurchaseOrderQuantityPriceMap, Long> {
    
    /**
     * Finds all PurchaseOrderQuantityPriceMap entries for a given purchase order.
     * 
     * @param purchaseOrderId The purchase order ID
     * @return List of PurchaseOrderQuantityPriceMap entries
     */
    List<PurchaseOrderQuantityPriceMap> findByPurchaseOrderId(Long purchaseOrderId);
    
    /**
     * Deletes all PurchaseOrderQuantityPriceMap entries for a given purchase order.
     * 
     * @param purchaseOrderId The purchase order ID
     */
    @Transactional
    @Modifying
    void deleteByPurchaseOrderId(Long purchaseOrderId);
}


