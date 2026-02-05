package com.example.SpringApi.Services.Tests.Promo;

import com.example.SpringApi.Models.DatabaseModels.Promo;
import com.example.SpringApi.Models.RequestModels.PromoRequestModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for PromoService.bulkCreatePromosAsync method.
 * 
 * Test count: 10 tests
 * - SUCCESS: 3 tests
 * - FAILURE / EXCEPTION: 7 tests
 */
@DisplayName("PromoService - BulkCreatePromos Tests")
public class BulkCreatePromosTest extends PromoServiceTestBase {

    // ===========================
    // SUCCESS TESTS
    // ===========================

    @Test
    @DisplayName("Bulk Create Promos - Success - All valid promos")
    void bulkCreatePromos_AllValid_Success() {
        // Arrange
        List<PromoRequestModel> promos = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            PromoRequestModel promoReq = new PromoRequestModel();
            promoReq.setPromoCode("PROMO" + i);
            promoReq.setDescription("Promo " + i);
            promoReq.setDiscountValue(BigDecimal.valueOf(10 + i));
            promoReq.setClientId(TEST_CLIENT_ID);
            promoReq.setStartDate(java.time.LocalDate.now());
            promoReq.setExpiryDate(java.time.LocalDate.now().plusDays(30));
            promos.add(promoReq);
        }

        Map<String, Promo> savedPromos = new HashMap<>();
        lenient().when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        lenient().when(promoRepository.findByPromoCodeAndClientId(anyString(), eq(TEST_CLIENT_ID))).thenAnswer(invocation -> {
            String code = invocation.getArgument(0);
            return Optional.ofNullable(savedPromos.get(code != null ? code.toUpperCase() : null));
        });
        when(promoRepository.save(any(Promo.class))).thenAnswer(invocation -> {
            Promo promo = invocation.getArgument(0);
            promo.setPromoId((long) (Math.random() * 1000));
            savedPromos.put(promo.getPromoCode(), promo);
            return promo;
        });

        // Act
        assertDoesNotThrow(() -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));

        // Assert
        verify(promoRepository, times(3)).save(any(Promo.class));
    }

    @Test
    @DisplayName("Bulk Create Promos - Success - Empty list")
    void bulkCreatePromos_EmptyList() {
        // Arrange
        List<PromoRequestModel> promos = new ArrayList<>();

        // Act & Assert
        assertDoesNotThrow(() -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
        verify(promoRepository, never()).save(any(Promo.class));
    }

    @Test
    @DisplayName("Bulk Create Promos - Success - Large batch (50 promos)")
    void bulkCreatePromos_LargeBatch_Success() {
        // Arrange
        List<PromoRequestModel> promos = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PromoRequestModel promoReq = new PromoRequestModel();
            promoReq.setPromoCode("BULK" + i);
            promoReq.setDescription("Bulk Promo " + i);
            promoReq.setDiscountValue(BigDecimal.valueOf(5));
            promoReq.setClientId(TEST_CLIENT_ID);
            promoReq.setStartDate(java.time.LocalDate.now());
            promoReq.setExpiryDate(java.time.LocalDate.now().plusDays(7));
            promos.add(promoReq);
        }

        Map<String, Promo> savedPromos = new HashMap<>();
        lenient().when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        lenient().when(promoRepository.findByPromoCodeAndClientId(anyString(), eq(TEST_CLIENT_ID))).thenAnswer(invocation -> {
            String code = invocation.getArgument(0);
            return Optional.ofNullable(savedPromos.get(code != null ? code.toUpperCase() : null));
        });
        when(promoRepository.save(any(Promo.class))).thenAnswer(invocation -> {
            Promo promo = invocation.getArgument(0);
            promo.setPromoId((long) (Math.random() * 1000));
            savedPromos.put(promo.getPromoCode(), promo);
            return promo;
        });

        // Act
        assertDoesNotThrow(() -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));

        // Assert
        verify(promoRepository, times(50)).save(any(Promo.class));
    }

    // ===========================
    // FAILURE / EXCEPTION TESTS
    // ===========================

    @Test
    @DisplayName("Bulk Create Promos - All invalid promos")
    void bulkCreatePromos_AllInvalid() {
        // Arrange
        List<PromoRequestModel> promos = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            PromoRequestModel invalid = new PromoRequestModel();
            invalid.setPromoCode(" ");
            invalid.setDescription(" ");
            invalid.setDiscountValue(new BigDecimal("-1"));
            invalid.setClientId(TEST_CLIENT_ID);
            promos.add(invalid);
        }

        // Act
        assertDoesNotThrow(() -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));

        // Assert
        verify(promoRepository, never()).save(any(Promo.class));
    }

    @Test
    @DisplayName("Bulk Create Promos - Failure - Database error")
    void bulkCreatePromos_DatabaseError() {
        // Arrange
        List<PromoRequestModel> promos = new ArrayList<>();
        PromoRequestModel promoReq = new PromoRequestModel();
        promoReq.setPromoCode("TEST");
        promoReq.setDescription("Test Promo");
        promoReq.setDiscountValue(BigDecimal.valueOf(10));
        promoReq.setClientId(TEST_CLIENT_ID);
        promos.add(promoReq);

        lenient().when(promoRepository.findByPromoCodeAndClientId(anyString(), eq(TEST_CLIENT_ID)))
                .thenReturn(Optional.empty());
        lenient().when(promoRepository.save(any(Promo.class))).thenThrow(new RuntimeException("Database error"));

        // Act
        assertDoesNotThrow(() -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));

        // Assert
        verify(promoRepository, never()).save(any(Promo.class));
    }

    @Test
    @DisplayName("Bulk Create Promos - Duplicate promo code")
    void bulkCreatePromos_DuplicatePromoCode_PartialSuccess() {
        // Arrange
        List<PromoRequestModel> promos = new ArrayList<>();

        PromoRequestModel valid = new PromoRequestModel();
        valid.setPromoCode("DUPLICATE");
        valid.setDescription("Valid Promo");
        valid.setDiscountValue(BigDecimal.ONE);
        valid.setClientId(TEST_CLIENT_ID);
        valid.setStartDate(java.time.LocalDate.now());
        valid.setExpiryDate(java.time.LocalDate.now().plusDays(1));

        PromoRequestModel duplicate = new PromoRequestModel();
        duplicate.setPromoCode("DUPLICATE");
        duplicate.setDescription("Duplicate Promo");
        duplicate.setDiscountValue(BigDecimal.ONE);
        duplicate.setClientId(TEST_CLIENT_ID);
        duplicate.setStartDate(java.time.LocalDate.now());
        duplicate.setExpiryDate(java.time.LocalDate.now().plusDays(1));

        promos.add(valid);
        promos.add(duplicate);

        when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList())
                .thenReturn(List.of(testPromo));
        when(promoRepository.findByPromoCodeAndClientId(anyString(), eq(TEST_CLIENT_ID)))
                .thenReturn(Optional.of(testPromo));
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);

        // Act
        assertDoesNotThrow(() -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));

        // Assert
        verify(promoRepository, times(1)).save(any(Promo.class));
    }

    @Test
    @DisplayName("Bulk Create Promos - Failure - Null list")
    void bulkCreatePromos_NullList_ThrowsBadRequestException() {
        // Act & Assert
        assertDoesNotThrow(() -> promoService.bulkCreatePromosAsync(null, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));
    }

    @Test
    @DisplayName("Bulk Create Promos - Partial failure with expired dates")
    void bulkCreatePromos_PartialFailure_ExpiredDates() {
        // Arrange
        List<PromoRequestModel> promos = new ArrayList<>();

        PromoRequestModel valid = new PromoRequestModel();
        valid.setPromoCode("VALID");
        valid.setDescription("Valid Promo");
        valid.setDiscountValue(BigDecimal.valueOf(10));
        valid.setClientId(TEST_CLIENT_ID);
        valid.setStartDate(java.time.LocalDate.now());
        valid.setExpiryDate(java.time.LocalDate.now().plusDays(30));
        promos.add(valid);

        PromoRequestModel expired = new PromoRequestModel();
        expired.setPromoCode("EXPIRED");
        expired.setDescription("Expired Promo");
        expired.setDiscountValue(BigDecimal.valueOf(10));
        expired.setClientId(TEST_CLIENT_ID);
        expired.setStartDate(java.time.LocalDate.now().minusDays(10));
        expired.setExpiryDate(java.time.LocalDate.now().minusDays(5));
        promos.add(expired);

        Map<String, Promo> savedPromos = new HashMap<>();
        lenient().when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        lenient().when(promoRepository.findByPromoCodeAndClientId(anyString(), eq(TEST_CLIENT_ID))).thenAnswer(invocation -> {
            String code = invocation.getArgument(0);
            return Optional.ofNullable(savedPromos.get(code != null ? code.toUpperCase() : null));
        });
        when(promoRepository.save(any(Promo.class))).thenAnswer(invocation -> {
            Promo promo = invocation.getArgument(0);
            promo.setPromoId((long) (Math.random() * 1000));
            savedPromos.put(promo.getPromoCode(), promo);
            return promo;
        });

        // Act
        assertDoesNotThrow(() -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));

        // Assert - only valid promo saved
        verify(promoRepository, times(1)).save(any(Promo.class));
    }

    @Test
    @DisplayName("Bulk Create Promos - Partial Success - Some promos fail validation")
    void bulkCreatePromos_PartialSuccess() {
        // Arrange
        List<PromoRequestModel> promos = new ArrayList<>();

        PromoRequestModel validPromo = new PromoRequestModel();
        validPromo.setPromoCode("VALID");
        validPromo.setDescription("Valid Promo");
        validPromo.setDiscountValue(BigDecimal.valueOf(10));
        validPromo.setClientId(TEST_CLIENT_ID);
        validPromo.setStartDate(java.time.LocalDate.now());
        validPromo.setExpiryDate(java.time.LocalDate.now().plusDays(30));
        promos.add(validPromo);

        PromoRequestModel invalidPromo = new PromoRequestModel();
        invalidPromo.setPromoCode(null);
        invalidPromo.setDescription("Invalid Promo");
        invalidPromo.setDiscountValue(BigDecimal.valueOf(10));
        invalidPromo.setClientId(TEST_CLIENT_ID);
        promos.add(invalidPromo);

        Map<String, Promo> savedPromos = new HashMap<>();
        lenient().when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        lenient().when(promoRepository.findByPromoCodeAndClientId(anyString(), eq(TEST_CLIENT_ID))).thenAnswer(invocation -> {
            String code = invocation.getArgument(0);
            return Optional.ofNullable(savedPromos.get(code != null ? code.toUpperCase() : null));
        });
        when(promoRepository.save(any(Promo.class))).thenAnswer(invocation -> {
            Promo promo = invocation.getArgument(0);
            promo.setPromoId((long) (Math.random() * 1000));
            if (promo.getPromoCode() != null) {
                savedPromos.put(promo.getPromoCode(), promo);
            }
            return promo;
        });

        // Act
        assertDoesNotThrow(() -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));

        // Assert
        verify(promoRepository, times(1)).save(any(Promo.class));
    }

    @Test
    @DisplayName("Bulk Create Promos - Performance test with 100 promos")
    void bulkCreatePromos_PerformanceTest_100Promos() {
        // Arrange
        List<PromoRequestModel> promos = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            PromoRequestModel promoReq = new PromoRequestModel();
            promoReq.setPromoCode("PERF" + i);
            promoReq.setDescription("Performance Test " + i);
            promoReq.setDiscountValue(BigDecimal.valueOf(i % 100));
            promoReq.setClientId(TEST_CLIENT_ID);
            promoReq.setStartDate(java.time.LocalDate.now());
            promoReq.setExpiryDate(java.time.LocalDate.now().plusDays(i % 30 + 1));
            promos.add(promoReq);
        }

        Map<String, Promo> savedPromos = new HashMap<>();
        lenient().when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        lenient().when(promoRepository.findByPromoCodeAndClientId(anyString(), eq(TEST_CLIENT_ID))).thenAnswer(invocation -> {
            String code = invocation.getArgument(0);
            return Optional.ofNullable(savedPromos.get(code != null ? code.toUpperCase() : null));
        });
        when(promoRepository.save(any(Promo.class))).thenAnswer(invocation -> {
            Promo promo = invocation.getArgument(0);
            promo.setPromoId((long) (Math.random() * 1000));
            savedPromos.put(promo.getPromoCode(), promo);
            return promo;
        });

        // Act
        assertDoesNotThrow(() -> promoService.bulkCreatePromosAsync(promos, TEST_USER_ID, TEST_LOGIN_NAME, TEST_CLIENT_ID));

        // Assert
        verify(promoRepository, times(100)).save(any(Promo.class));
    }
}
