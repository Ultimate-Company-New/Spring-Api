package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.PickupLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for PickupLocation entity.
 *
 * This interface provides CRUD operations for PickupLocation entities
 * and custom queries for pickup location management.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Repository
public interface PickupLocationRepository extends JpaRepository<PickupLocation, Long> {

    PickupLocation findByAddressNickName(String addressNickName);

    @Query("SELECT p FROM PickupLocation p JOIN FETCH p.address WHERE p.pickupLocationId = :pickupLocationId")
    PickupLocation findPickupLocationById(@Param("pickupLocationId") Long pickupLocationId);

    @Query("SELECT p FROM PickupLocation p JOIN FETCH p.address WHERE (:includeDeleted = true OR p.isDeleted = false)")
    List<PickupLocation> findAllWithAddresses(@Param("includeDeleted") boolean includeDeleted);

    @Query("select p,a from PickupLocation p join Address a on a.addressId = p.pickupLocationAddressId " +
            "where (:includeDeleted = true OR p.isDeleted = false) " +
            "and (COALESCE(:filterExpr, '') = '' OR " +
            "(CASE :columnName " +
            "WHEN 'pickupLocationId' THEN CONCAT(p.pickupLocationId, '') " +
            "WHEN 'locationName' THEN CONCAT(p.addressNickName, '') " +
            "WHEN 'address' THEN CONCAT(a.streetAddress, ' ', a.streetAddress2, ' ', a.city, ' ', a.state, ' ', a.postalCode) " +
            "ELSE '' END) LIKE " +
            "(CASE :condition " +
            "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
            "WHEN 'equals' THEN :filterExpr " +
            "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
            "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
            "WHEN 'isEmpty' THEN '' " +
            "WHEN 'isNotEmpty' THEN '%' " +
            "ELSE '' END))")
    Page<Object[]> findPaginatedPickupLocations(@Param("columnName") String columnName,
                                               @Param("condition") String condition,
                                               @Param("filterExpr") String filterExpr,
                                               @Param("includeDeleted") boolean includeDeleted,
                                               Pageable pageable);
}