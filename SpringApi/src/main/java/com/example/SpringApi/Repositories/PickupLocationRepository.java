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

    @Query("SELECT p FROM PickupLocation p JOIN FETCH p.address WHERE p.addressNickName = :addressNickName AND p.clientId = :clientId")
    PickupLocation findByAddressNickNameAndClientId(@Param("addressNickName") String addressNickName, @Param("clientId") Long clientId);

    @Query("SELECT p FROM PickupLocation p JOIN FETCH p.address WHERE p.pickupLocationId = :pickupLocationId AND p.clientId = :clientId")
    PickupLocation findPickupLocationByIdAndClientId(@Param("pickupLocationId") Long pickupLocationId, @Param("clientId") Long clientId);

    @Query("SELECT COUNT(p.pickupLocationId) FROM PickupLocation p WHERE p.pickupLocationId = :pickupLocationId AND p.clientId = :clientId")
    long countByPickupLocationIdAndClientId(@Param("pickupLocationId") Long pickupLocationId, @Param("clientId") Long clientId);

    @Query("SELECT p FROM PickupLocation p JOIN FETCH p.address WHERE p.clientId = :clientId AND (:includeDeleted = true OR p.isDeleted = false)")
    List<PickupLocation> findAllWithAddressesByClientId(@Param("clientId") Long clientId, @Param("includeDeleted") boolean includeDeleted);

    @Query("SELECT p FROM PickupLocation p JOIN FETCH p.address a " +
            "WHERE p.clientId = :clientId " +
            "AND (:includeDeleted = true OR p.isDeleted = false) " +
            "AND (COALESCE(:filterExpr, '') = '' OR " +
            "(CASE :columnName " +
            "WHEN 'pickupLocationId' THEN CONCAT(p.pickupLocationId, '') " +
            "WHEN 'locationName' THEN CONCAT(p.addressNickName, '') " +
            "WHEN 'address' THEN CONCAT(a.streetAddress, ' ', a.streetAddress2, ' ', a.city, ' ', a.state, ' ', a.postalCode) " +
            "WHEN 'isDeleted' THEN CONCAT(p.isDeleted, '') " +
            "WHEN 'shipRocketPickupLocationId' THEN CONCAT(p.shipRocketPickupLocationId, '') " +
            "WHEN 'createdBy' THEN CONCAT(p.createdBy, '') " +
            "WHEN 'modifiedBy' THEN CONCAT(p.modifiedBy, '') " +
            "WHEN 'createdAt' THEN CONCAT(p.createdAt, '') " +
            "WHEN 'updatedAt' THEN CONCAT(p.updatedAt, '') " +
            "WHEN 'notes' THEN CONCAT(p.notes, '') " +
            "ELSE '' END) LIKE " +
            "(CASE :condition " +
            "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
            "WHEN 'equals' THEN :filterExpr " +
            "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
            "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
            "WHEN 'isEmpty' THEN '' " +
            "WHEN 'isNotEmpty' THEN '%' " +
            "ELSE '' END))")
    Page<PickupLocation> findPaginatedPickupLocations(@Param("clientId") Long clientId,
                                                      @Param("columnName") String columnName,
                                                      @Param("condition") String condition,
                                                      @Param("filterExpr") String filterExpr,
                                                      @Param("includeDeleted") boolean includeDeleted,
                                                      Pageable pageable);
}