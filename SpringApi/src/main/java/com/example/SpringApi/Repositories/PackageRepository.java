package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.Package;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {
        
    @Query("SELECT p FROM Package p WHERE p.clientId = :clientId AND p.isDeleted = false")
    List<Package> findByClientIdAndIsDeletedFalse(@Param("clientId") Long clientId);

    @Query("SELECT p FROM Package p " +
            "WHERE p.clientId = :clientId " +
            "AND (:includeDeleted = true OR p.isDeleted = false) " +
            "AND (COALESCE(:filterExpr, '') = '' OR " +
            "(CASE :columnName " +
            "WHEN 'packageId' THEN CONCAT(p.packageId, '') " +
            "WHEN 'packageName' THEN CONCAT(p.packageName, '') " +
            "WHEN 'dimensions' THEN CONCAT(p.length, ' x ', p.breadth, ' x ', p.height) " +
            "WHEN 'standardCapacity' THEN CONCAT(p.standardCapacity, '') " +
            "WHEN 'packageType' THEN CONCAT(p.packageType, '') " +
            "WHEN 'maxWeight' THEN CONCAT(p.maxWeight, '') " +
            "WHEN 'pricePerUnit' THEN CONCAT(p.pricePerUnit, '') " +
            "WHEN 'createdUser' THEN CONCAT(p.createdUser, '') " +
            "WHEN 'modifiedUser' THEN CONCAT(p.modifiedUser, '') " +
            "WHEN 'createdAt' THEN CONCAT(p.createdAt, '') " +
            "WHEN 'updatedAt' THEN CONCAT(p.updatedAt, '') " +
            "WHEN 'notes' THEN CONCAT(p.notes, '') " +
            "WHEN 'isDeleted' THEN CONCAT(p.isDeleted, '') " +
            "ELSE '' END) LIKE " +
            "(CASE :condition " +
            "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
            "WHEN 'equals' THEN :filterExpr " +
            "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
            "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
            "WHEN 'isEmpty' THEN '' " +
            "WHEN 'isNotEmpty' THEN '%' " +
            "ELSE '' END))")
    Page<Package> findPaginatedPackages(@Param("clientId") Long clientId,
                                       @Param("columnName") String columnName,
                                       @Param("condition") String condition,
                                       @Param("filterExpr") String filterExpr,
                                       @Param("includeDeleted") boolean includeDeleted,
                                       Pageable pageable);

    @Query("select p from Package p where p.packageId = :packageId and p.clientId = :clientId")
    Package findByPackageIdAndClientId(@Param("packageId") Long packageId, @Param("clientId") Long clientId);
}