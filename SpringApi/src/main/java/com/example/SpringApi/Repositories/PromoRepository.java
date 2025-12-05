package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.Promo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromoRepository extends JpaRepository<Promo, Long> {

    Optional<Promo> findByPromoCode(String promoCode);

    @Query("SELECT p FROM Promo p WHERE p.promoCode = :promoCode AND p.clientId = :clientId")
    Optional<Promo> findByPromoCodeAndClientId(@Param("promoCode") String promoCode, @Param("clientId") Long clientId);

    @Query("SELECT p FROM Promo p WHERE p.promoId = :promoId AND p.clientId = :clientId")
    Optional<Promo> findByPromoIdAndClientId(@Param("promoId") Long promoId, @Param("clientId") Long clientId);
    
    /**
     * Find overlapping promo codes within a date range.
     * Checks for active (non-deleted) promos with the same code in the same client
     * that have overlapping date ranges.
     * 
     * Overlap logic:
     * Case 1: New promo has no expiry - overlaps if existing promo's end is after new start (or existing has no end)
     * Case 2: Existing promo has no expiry - overlaps if new promo's end is after existing start (or new has no end)
     * Case 3: Both have expiry dates - standard overlap: new.start <= existing.end AND new.end >= existing.start
     * 
     * @param promoCode The promo code to check
     * @param clientId The client ID
     * @param startDate The start date of the new promo
     * @param expiryDate The expiry date of the new promo (can be null for no expiry)
     * @return List of overlapping promos
     */
    @Query("SELECT p FROM Promo p WHERE " +
           "p.promoCode = :promoCode AND " +
           "p.clientId = :clientId AND " +
           "p.isDeleted = false AND " +
           "(" +
           "    (" +  // Case 1: New promo has no expiry
           "        :expiryDate IS NULL AND " +
           "        (p.expiryDate IS NULL OR p.expiryDate >= :startDate)" +
           "    ) OR " +
           "    (" +  // Case 2: Existing promo has no expiry, new has expiry
           "        p.expiryDate IS NULL AND :expiryDate IS NOT NULL AND " +
           "        :expiryDate >= p.startDate" +
           "    ) OR " +
           "    (" +  // Case 3: Both have expiry dates (standard overlap)
           "        :expiryDate IS NOT NULL AND p.expiryDate IS NOT NULL AND " +
           "        :startDate <= p.expiryDate AND :expiryDate >= p.startDate" +
           "    )" +
           ")")
    List<Promo> findOverlappingPromos(
        @Param("promoCode") String promoCode,
        @Param("clientId") Long clientId,
        @Param("startDate") LocalDate startDate,
        @Param("expiryDate") LocalDate expiryDate
    );
}