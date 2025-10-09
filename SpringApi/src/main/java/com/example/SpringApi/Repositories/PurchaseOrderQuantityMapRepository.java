package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.PurchaseOrderQuantityMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for PurchaseOrderQuantityMap entity operations.
 */
@Repository
public interface PurchaseOrderQuantityMapRepository extends JpaRepository<PurchaseOrderQuantityMap, Long> {
    
    List<PurchaseOrderQuantityMap> findByPurchaseOrderId(Long purchaseOrderId);
    
    List<PurchaseOrderQuantityMap> findByProductId(Long productId);
    
    List<PurchaseOrderQuantityMap> findByPackageId(Long packageId);
}

