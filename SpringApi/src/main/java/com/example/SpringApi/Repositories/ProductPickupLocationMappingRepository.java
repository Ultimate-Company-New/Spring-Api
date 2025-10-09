package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.ProductPickupLocationMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ProductPickupLocationMapping entity operations.
 */
@Repository
public interface ProductPickupLocationMappingRepository extends JpaRepository<ProductPickupLocationMapping, Long> {
    
    List<ProductPickupLocationMapping> findByProductId(Long productId);
    
    List<ProductPickupLocationMapping> findByPickupLocationId(Long pickupLocationId);
    
    List<ProductPickupLocationMapping> findByIsDeletedFalse();
}

