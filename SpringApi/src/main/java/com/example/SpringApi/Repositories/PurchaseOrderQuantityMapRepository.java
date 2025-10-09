package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.PurchaseOrderQuantityMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for PurchaseOrderQuantityMap entity operations.
 */
@Repository
public interface PurchaseOrderQuantityMapRepository extends JpaRepository<PurchaseOrderQuantityMap, Long> {
}

