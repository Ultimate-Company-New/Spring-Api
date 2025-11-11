package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * Repository interface for Product entity.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.createdByUser " +
           "WHERE p.productId = :id AND p.clientId = :clientId")
    Product findByIdWithRelatedEntities(@Param("id") Long id, @Param("clientId") Long clientId);

    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.createdByUser " +
           "WHERE p.clientId = :clientId " +
           "AND (:columnName IS NULL OR :columnName = '' OR " +
           // Handle boolean fields - only support 'equals' condition
           "(:columnName = 'isDiscountPercent' AND :condition = 'equals' AND ((:filterExpr = 'true' AND p.isDiscountPercent = true) OR (:filterExpr = 'false' AND p.isDiscountPercent = false))) OR " +
           "(:columnName = 'returnsAllowed' AND :condition = 'equals' AND ((:filterExpr = 'true' AND p.returnsAllowed = true) OR (:filterExpr = 'false' AND p.returnsAllowed = false))) OR " +
           "(:columnName = 'itemModified' AND :condition = 'equals' AND ((:filterExpr = 'true' AND p.itemModified = true) OR (:filterExpr = 'false' AND p.itemModified = false))) OR " +
           "(:columnName = 'isDeleted' AND :condition = 'equals' AND ((:filterExpr = 'true' AND p.isDeleted = true) OR (:filterExpr = 'false' AND p.isDeleted = false))) OR " +
           // For non-boolean fields, use LIKE
           "(:columnName NOT IN ('isDiscountPercent', 'returnsAllowed', 'itemModified', 'isDeleted') AND " +
           "CASE :columnName " +
           "WHEN 'productId' THEN CAST(p.productId AS string) " +
           "WHEN 'title' THEN CONCAT(p.title, '') " +
           "WHEN 'descriptionHtml' THEN CONCAT(p.descriptionHtml, '') " +
           "WHEN 'brand' THEN CONCAT(p.brand, '') " +
           "WHEN 'color' THEN CONCAT(p.color, '') " +
           "WHEN 'colorLabel' THEN CONCAT(p.colorLabel, '') " +
           "WHEN 'condition' THEN CONCAT(p.condition, '') " +
           "WHEN 'countryOfManufacture' THEN CONCAT(p.countryOfManufacture, '') " +
           "WHEN 'model' THEN CONCAT(p.model, '') " +
           "WHEN 'upc' THEN CONCAT(p.upc, '') " +
           "WHEN 'modificationHtml' THEN CONCAT(p.modificationHtml, '') " +
           "WHEN 'price' THEN CAST(p.price AS string) " +
           "WHEN 'discount' THEN CAST(p.discount AS string) " +
           "WHEN 'length' THEN CAST(p.length AS string) " +
           "WHEN 'breadth' THEN CAST(p.breadth AS string) " +
           "WHEN 'height' THEN CAST(p.height AS string) " +
           "WHEN 'weightKgs' THEN CAST(p.weightKgs AS string) " +
           "WHEN 'categoryId' THEN CAST(p.categoryId AS string) " +
           "WHEN 'createdUser' THEN CONCAT(p.createdUser, '') " +
           "WHEN 'modifiedUser' THEN CONCAT(p.modifiedUser, '') " +
           "WHEN 'createdAt' THEN CAST(p.createdAt AS string) " +
           "WHEN 'updatedAt' THEN CAST(p.updatedAt AS string) " +
           "WHEN 'notes' THEN CONCAT(p.notes, '') " +
           "ELSE '' END LIKE " +
           "(CASE :condition " +
           "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
           "WHEN 'equals' THEN :filterExpr " +
           "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
           "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
           "WHEN 'isEmpty' THEN '' " +
           "WHEN 'isNotEmpty' THEN '%' " +
           "ELSE '' END))) " +
           "AND (:selectedIds IS NULL OR p.productId IN (:selectedIds)) " +
           "AND (:includeDeleted = true OR p.isDeleted = false)")
    Page<Product> findPaginatedProducts(@Param("clientId") Long clientId,
                                       @Param("columnName") String columnName,
                                       @Param("condition") String condition,
                                       @Param("filterExpr") String filterExpr,
                                       @Param("includeDeleted") boolean includeDeleted,
                                       @Param("selectedIds") Set<Long> selectedIds,
                                       Pageable pageable);
}
