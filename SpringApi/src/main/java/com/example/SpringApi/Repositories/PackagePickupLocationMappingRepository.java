package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}