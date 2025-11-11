package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.ProductPickupLocationMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ProductPickupLocationMapping entity operations.
 */
@Repository
public interface ProductPickupLocationMappingRepository extends JpaRepository<ProductPickupLocationMapping, Long> {
    
    /**
     * Find all ProductPickupLocationMappings by product ID and client ID.
     * 
     * @param productId The product ID
     * @param clientId The client ID
     * @return List of ProductPickupLocationMappings
     */
    @Query("SELECT pplm FROM ProductPickupLocationMapping pplm " +
           "JOIN FETCH pplm.product p " +
           "JOIN FETCH pplm.pickupLocation pl " +
           "WHERE p.productId = :productId AND p.clientId = :clientId AND p.isDeleted = false")
    List<ProductPickupLocationMapping> findByProductIdAndClientId(@Param("productId") Long productId, @Param("clientId") Long clientId);
}

