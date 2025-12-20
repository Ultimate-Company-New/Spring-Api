package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.ProductPickupLocationMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
    
    /**
     * Delete all ProductPickupLocationMappings by product ID.
     * 
     * @param productId The product ID
     */
    @Modifying
    @Query("DELETE FROM ProductPickupLocationMapping pplm WHERE pplm.productId = :productId")
    void deleteByProductId(@Param("productId") Long productId);
    
    /**
     * Find all ProductPickupLocationMappings by pickup location ID.
     * 
     * @param pickupLocationId The pickup location ID
     * @return List of ProductPickupLocationMappings
     */
    List<ProductPickupLocationMapping> findByPickupLocationId(@Param("pickupLocationId") Long pickupLocationId);
    
    /**
     * Delete all ProductPickupLocationMappings by pickup location ID.
     * 
     * @param pickupLocationId The pickup location ID
     */
    @Modifying
    @Query("DELETE FROM ProductPickupLocationMapping pplm WHERE pplm.pickupLocationId = :pickupLocationId")
    void deleteByPickupLocationId(@Param("pickupLocationId") Long pickupLocationId);
    
    /**
     * Count all ProductPickupLocationMappings by pickup location ID.
     * 
     * @param pickupLocationId The pickup location ID
     * @return The count of mappings
     */
    @Query("SELECT COUNT(pplm) FROM ProductPickupLocationMapping pplm WHERE pplm.pickupLocationId = :pickupLocationId")
    Integer countByPickupLocationId(@Param("pickupLocationId") Long pickupLocationId);
    
    /**
     * Get counts of ProductPickupLocationMappings grouped by pickup location ID for multiple locations.
     * Returns a list of Object arrays where [0] is pickupLocationId and [1] is the count.
     * 
     * @param pickupLocationIds List of pickup location IDs
     * @return List of Object arrays with [pickupLocationId, count]
     */
    @Query("SELECT pplm.pickupLocationId, COUNT(pplm) FROM ProductPickupLocationMapping pplm " +
           "WHERE pplm.pickupLocationId IN :pickupLocationIds GROUP BY pplm.pickupLocationId")
    List<Object[]> countByPickupLocationIds(@Param("pickupLocationIds") List<Long> pickupLocationIds);
    
    /**
     * Find all ProductPickupLocationMappings for a specific product across all pickup locations.
     * Fetches pickup location with address for distance calculation.
     * 
     * @param productId The product ID
     * @return List of ProductPickupLocationMappings with pickup location and address
     */
    @Query("SELECT pplm FROM ProductPickupLocationMapping pplm " +
           "JOIN FETCH pplm.pickupLocation pl " +
           "LEFT JOIN FETCH pl.address a " +
           "WHERE pplm.productId = :productId AND pplm.isActive = true AND pl.isDeleted = false")
    List<ProductPickupLocationMapping> findByProductIdWithPickupLocationAndAddress(@Param("productId") Long productId);
}

