package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for PurchaseOrder entity operations.
 */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    
    List<PurchaseOrder> findByUserId(Long userId);
    
    List<PurchaseOrder> findByStatus(String status);
    
    List<PurchaseOrder> findByIsDeletedFalse();
    
    List<PurchaseOrder> findByUserIdAndIsDeletedFalse(Long userId, Boolean isDeleted);
}

