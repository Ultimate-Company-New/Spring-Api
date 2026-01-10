package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackagePickupLocationMappingRepository extends JpaRepository<PackagePickupLocationMapping, Long> {
    List<PackagePickupLocationMapping> findByPickupLocationId(Long pickupLocationId);

    @Query("select m from PackagePickupLocationMapping m join m.packageEntity p where m.pickupLocationId = :pickupLocationId and p.clientId = :clientId order by p.packageId desc")
    List<PackagePickupLocationMapping> findByPickupLocationIdAndClientId(@Param("pickupLocationId") Long pickupLocationId, @Param("clientId") Long clientId);

    /**
     * Find all mappings for a specific package
     */
    List<PackagePickupLocationMapping> findByPackageId(Long packageId);

    /**
     * Delete all mappings for a specific package (used before recreating mappings on update)
     */
    @Modifying
    @Query("DELETE FROM PackagePickupLocationMapping m WHERE m.packageId = :packageId")
    void deleteByPackageId(@Param("packageId") Long packageId);
    
    /**
     * Delete all mappings for a specific pickup location
     */
    @Modifying
    @Query("DELETE FROM PackagePickupLocationMapping m WHERE m.pickupLocationId = :pickupLocationId")
    void deleteByPickupLocationId(@Param("pickupLocationId") Long pickupLocationId);
    
    /**
     * Count all PackagePickupLocationMappings by pickup location ID.
     * 
     * @param pickupLocationId The pickup location ID
     * @return The count of mappings
     */
    @Query("SELECT COUNT(m) FROM PackagePickupLocationMapping m WHERE m.pickupLocationId = :pickupLocationId")
    Integer countByPickupLocationId(@Param("pickupLocationId") Long pickupLocationId);
    
    /**
     * Get counts of PackagePickupLocationMappings grouped by pickup location ID for multiple locations.
     * Returns a list of Object arrays where [0] is pickupLocationId and [1] is the count.
     * 
     * @param pickupLocationIds List of pickup location IDs
     * @return List of Object arrays with [pickupLocationId, count]
     */
    @Query("SELECT m.pickupLocationId, COUNT(m) FROM PackagePickupLocationMapping m " +
           "WHERE m.pickupLocationId IN :pickupLocationIds GROUP BY m.pickupLocationId")
    List<Object[]> countByPickupLocationIds(@Param("pickupLocationIds") List<Long> pickupLocationIds);
    
    /**
     * Find all package mappings for multiple pickup locations with package details.
     * 
     * @param pickupLocationIds List of pickup location IDs
     * @return List of PackagePickupLocationMappings with package entity loaded
     */
    @Query("SELECT m FROM PackagePickupLocationMapping m " +
           "JOIN FETCH m.packageEntity p " +
           "WHERE m.pickupLocationId IN :pickupLocationIds AND m.availableQuantity > 0 " +
           "ORDER BY p.pricePerUnit ASC")
    List<PackagePickupLocationMapping> findByPickupLocationIdsWithPackages(@Param("pickupLocationIds") List<Long> pickupLocationIds);
    
    /**
     * Find PackagePickupLocationMapping by package ID and pickup location ID.
     * 
     * @param packageId The package ID
     * @param pickupLocationId The pickup location ID
     * @return Optional PackagePickupLocationMapping
     */
    @Query("SELECT m FROM PackagePickupLocationMapping m " +
           "WHERE m.packageId = :packageId AND m.pickupLocationId = :pickupLocationId")
    Optional<PackagePickupLocationMapping> findByPackageIdAndPickupLocationId(
            @Param("packageId") Long packageId,
            @Param("pickupLocationId") Long pickupLocationId);
}