package com.example.SpringApi.Services.Tests.Promo;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.Promo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Test class for PromoService validation logic.
 * 
 * Test count: 20 tests
 * - SUCCESS: 4 tests
 * - FAILURE / EXCEPTION: 16 tests
 */
@DisplayName("PromoService - Validation Tests")
public class PromoValidationTest extends PromoServiceTestBase {

    // ===========================
    // SUCCESS TESTS
    // ===========================

    @Test
    @DisplayName("Promo Validation - Description max length (500) - Success")
    void testPromoValidation_DescriptionMaxLength_Success() {
        // Arrange
        testPromoRequest.setDescription("a".repeat(500));
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);

        // Act & Assert
        assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));
    }

    @Test
    @DisplayName("Promo Validation - Expiry date equals start date - Success")
    void testPromoValidation_ExpiryDateEqualsStartDate() {
        // Arrange
        testPromoRequest.setStartDate(java.time.LocalDate.now().plusDays(1));
        testPromoRequest.setExpiryDate(testPromoRequest.getStartDate());
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);

        // Act & Assert
        assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));
    }

    @Test
    @DisplayName("Promo Validation - Expiry date null is allowed")
    void testPromoValidation_ExpiryDateNull_Allows() {
        // Arrange
        testPromoRequest.setExpiryDate(null);
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);

        // Act & Assert
        assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));
    }

    @Test
    @DisplayName("Promo Validation - Promo code max length (100) - Success")
    void testPromoValidation_PromoCodeMaxLength_Success() {
        // Arrange
        testPromoRequest.setPromoCode("a".repeat(100));
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);

        // Act & Assert
        assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));
    }

    // ===========================
    // FAILURE / EXCEPTION TESTS
    // ===========================

    @Test
    @DisplayName("Promo Validation - Description too long - Throws BadRequestException")
    void testPromoValidation_DescriptionTooLong() {
        // Arrange
        testPromoRequest.setDescription("a".repeat(501));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.DescriptionTooLong, exception.getMessage());
    }

    @Test
    @DisplayName("Promo Validation - Expiry date before start date - Throws BadRequestException")
    void testPromoValidation_ExpiryDateBeforeStartDate() {
        // Arrange
        testPromoRequest.setStartDate(java.time.LocalDate.now().plusDays(2));
        testPromoRequest.setExpiryDate(java.time.LocalDate.now().plusDays(1));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.ExpiryDateMustBeAfterStartDate, exception.getMessage());
    }

    @Test
    @DisplayName("Promo Validation - Invalid description (empty) - Throws BadRequestException")
    void testPromoValidation_InvalidDescription_Empty() {
        // Arrange
        testPromoRequest.setDescription("   ");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidDescription, exception.getMessage());
    }

    @Test
    @DisplayName("Promo Validation - Invalid description (null) - Throws BadRequestException")
    void testPromoValidation_InvalidDescription_Null() {
        // Arrange
        testPromoRequest.setDescription(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidDescription, exception.getMessage());
    }

    @Test
    @DisplayName("Promo Validation - Invalid discount for percent (>100) - Throws BadRequestException")
    void testPromoValidation_InvalidDiscountPercentOver100() {
        // Arrange
        testPromoRequest.setIsPercent(true);
        testPromoRequest.setDiscountValue(new BigDecimal("150"));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidPercentageValue, exception.getMessage());
    }

    @Test
    @DisplayName("Promo Validation - Invalid discount value (negative) - Throws BadRequestException")
    void testPromoValidation_InvalidDiscountValue_Negative() {
        // Arrange
        testPromoRequest.setDiscountValue(new BigDecimal("-1"));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidDiscountValue, exception.getMessage());
    }

    @Test
    @DisplayName("Promo Validation - Invalid discount value (null) - Throws BadRequestException")
    void testPromoValidation_InvalidDiscountValue_Null() {
        // Arrange
        testPromoRequest.setDiscountValue(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidDiscountValue, exception.getMessage());
    }

    @Test
    @DisplayName("Promo Validation - Invalid expiry date (past) - Throws BadRequestException")
    void testPromoValidation_InvalidExpiryDate_Past() {
        // Arrange
        testPromoRequest.setStartDate(java.time.LocalDate.now());
        testPromoRequest.setExpiryDate(java.time.LocalDate.now().minusDays(1));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidExpiryDate, exception.getMessage());
    }

    @Test
    @DisplayName("Promo Validation - Invalid percentage value (> 100) - Throws BadRequestException")
    void testPromoValidation_InvalidPercentageValue() {
        // Arrange
        testPromoRequest.setIsPercent(true);
        testPromoRequest.setDiscountValue(new BigDecimal("101"));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidPercentageValue, exception.getMessage());
    }

    @Test
    @DisplayName("Promo Validation - Invalid promo code (empty) - Throws BadRequestException")
    void testPromoValidation_InvalidPromoCode_Empty() {
        // Arrange
        testPromoRequest.setPromoCode(" ");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidPromoCode, exception.getMessage());
    }

    @Test
    @DisplayName("Promo Validation - Invalid promo code (null) - Throws NullPointerException")
    void testPromoValidation_InvalidPromoCode_Null() {
        // Arrange
        testPromoRequest.setPromoCode(null);

        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> promoService.createPromo(testPromoRequest));
    }

    @Test
    @DisplayName("Promo Validation - Invalid start date (null) - Throws BadRequestException")
    void testPromoValidation_InvalidStartDate_Null() {
        // Arrange
        testPromoRequest.setStartDate(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidStartDate, exception.getMessage());
    }

    @Test
    @DisplayName("Promo Validation - Overlapping Promo Code - Throws BadRequestException")
    void testPromoValidation_OverlappingPromoCode() {
        // Arrange
        testPromoRequest.setStartDate(java.time.LocalDate.now());
        testPromoRequest.setExpiryDate(java.time.LocalDate.now().plusDays(10));
        testPromoRequest.setPromoCode("OVERLAP");

        when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                .thenReturn(List.of(new Promo()));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.OverlappingPromoCode, exception.getMessage());
    }

    @Test
    @DisplayName("Promo Validation - Promo code too long - Throws BadRequestException")
    void testPromoValidation_PromoCodeTooLong() {
        // Arrange
        testPromoRequest.setPromoCode("a".repeat(101));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.PromoCodeTooLong, exception.getMessage());
    }

    @Test
    @DisplayName("Promo Validation - Start date in past - Throws BadRequestException")
    void testPromoValidation_StartDatePast() {
        // Arrange
        testPromoRequest.setStartDate(java.time.LocalDate.now().minusDays(1));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.StartDateMustBeTodayOrFuture, exception.getMessage());
    }

    @Test
    @DisplayName("Promo Validation - Start date today - Success")
    void testPromoValidation_StartDateToday_Success() {
        // Arrange
        testPromoRequest.setStartDate(java.time.LocalDate.now());
        testPromoRequest.setExpiryDate(java.time.LocalDate.now().plusDays(1));
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);

        // Act & Assert
        assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));
    }
}
