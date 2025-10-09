package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

import java.util.Optional;

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
           "WHERE p.productId = :id")
    Optional<Product> findByIdWithRelatedEntities(@Param("id") Long id);

    @Query("SELECT p FROM Product p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.createdByUser " +
           "WHERE " +
           "(:columnName IS NULL OR :columnName = '' OR " +
           "CASE :columnName " +
           "WHEN 'productId' THEN CAST(p.productId AS string) " +
           "WHEN 'title' THEN p.title " +
           "WHEN 'description' THEN p.descriptionHtml " +
           "WHEN 'price' THEN CAST(p.price AS string) " +
           "WHEN 'categoryId' THEN CAST(p.categoryId AS string) " +
           "ELSE '' END LIKE " +
           "(CASE :condition " +
           "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
           "WHEN 'equals' THEN :filterExpr " +
           "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
           "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
           "WHEN 'isEmpty' THEN '' " +
           "WHEN 'isNotEmpty' THEN '%' " +
           "ELSE '' END)) " +
           "AND (:selectedIds IS NULL OR p.productId IN (:selectedIds)) " +
           "AND (:includeDeleted = true OR p.isDeleted = false)")
    Page<Product> findPaginatedProducts(@Param("columnName") String columnName,
                                       @Param("condition") String condition,
                                       @Param("filterExpr") String filterExpr,
                                       @Param("includeDeleted") boolean includeDeleted,
                                       @Param("selectedIds") Set<Long> selectedIds,
                                       Pageable pageable);
}
