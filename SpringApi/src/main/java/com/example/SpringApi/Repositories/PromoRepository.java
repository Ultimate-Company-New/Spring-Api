package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.Promo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromoRepository extends JpaRepository<Promo, Long> {

    @Query("SELECT p FROM Promo p " +
           "WHERE (:includeDeleted = true OR p.isDeleted = false) " +
           "AND (COALESCE(:filterExpr, '') = '' OR " +
           "(CASE :columnName " +
           "WHEN 'promoId' THEN CONCAT(p.promoId, '') " +
           "WHEN 'promoCode' THEN CONCAT(p.promoCode, '') " +
           "WHEN 'description' THEN CONCAT(p.description, '') " +
           "WHEN 'discountValue' THEN CONCAT(p.discountValue, '') " +
           "ELSE '' END) LIKE " +
           "(CASE :condition " +
           "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
           "WHEN 'equals' THEN :filterExpr " +
           "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
           "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
           "WHEN 'isEmpty' THEN '' " +
           "WHEN 'isNotEmpty' THEN '%' " +
           "ELSE '' END))")
    Page<Promo> findPaginatedPromos(@Param("columnName") String columnName,
                                    @Param("condition") String condition,
                                    @Param("filterExpr") String filterExpr,
                                    @Param("includeDeleted") boolean includeDeleted,
                                    Pageable pageable);

    Optional<Promo> findByPromoCode(String promoCode);
}