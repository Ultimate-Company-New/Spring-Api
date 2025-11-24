package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.Promo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromoRepository extends JpaRepository<Promo, Long> {

    Optional<Promo> findByPromoCode(String promoCode);

    @Query("SELECT p FROM Promo p WHERE p.promoCode = :promoCode AND p.clientId = :clientId")
    Optional<Promo> findByPromoCodeAndClientId(@Param("promoCode") String promoCode, @Param("clientId") Long clientId);

    @Query("SELECT p FROM Promo p WHERE p.promoId = :promoId AND p.clientId = :clientId")
    Optional<Promo> findByPromoIdAndClientId(@Param("promoId") Long promoId, @Param("clientId") Long clientId);
}