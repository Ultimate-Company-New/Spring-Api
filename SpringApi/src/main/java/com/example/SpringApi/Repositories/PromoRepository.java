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

    @Query("SELECT p FROM Promo p WHERE " +
           "(:columnName IS NULL OR :columnName = '' OR " +
           "CASE :columnName " +
           "WHEN 'promoId' THEN CAST(p.promoId AS string) " +
           "WHEN 'description' THEN p.description " +
           "WHEN 'promoCode' THEN p.promoCode " +
           "WHEN 'discountValue' THEN CAST(p.discountValue AS string) " +
           "ELSE '' END LIKE CONCAT('%', :filterExpr, '%')) " +
           "AND (:includeDeleted = true OR p.isDeleted = false)")
    Page<Promo> findPaginatedPromos(@Param("columnName") String columnName,
                                    @Param("condition") String condition,
                                    @Param("filterExpr") String filterExpr,
                                    @Param("includeDeleted") boolean includeDeleted,
                                    Pageable pageable);

    Optional<Promo> findByPromoCode(String promoCode);
}