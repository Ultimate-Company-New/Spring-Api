package com.example.SpringApi.Services.Tests.Promo;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.DatabaseModels.Promo;
import com.example.SpringApi.Models.RequestModels.PromoRequestModel;
import com.example.SpringApi.SuccessMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for PromoService.createPromo method.
 */
@DisplayName("PromoService - CreatePromo Tests")
class CreatePromoTest extends PromoServiceTestBase {

    // Total Tests: 41

    /*
     **********************************************************************************************
     * SECTION 1: SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify successful creation of a promo with valid data.
     */
    @Test
    @DisplayName("createPromo - Success - All valid data")
    void createPromo_AllValid_Success() {
        // Arrange
        when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);

        // Act
        assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));

        // Assert
        verify(promoRepository).save(any(Promo.class));
        verify(userLogService).logData(
                eq(TEST_USER_ID),
                contains(SuccessMessages.PromoSuccessMessages.CreatePromo),
                eq(ApiRoutes.PromosSubRoute.CREATE_PROMO));
    }

    /**
     * Purpose: Verify successful creation of a promo with a fixed value discount.
     */
    @Test
    @DisplayName("createPromo - Success - Fixed value discount")
    void createPromo_FixedValueDiscount_Success() {
        // Arrange
        testPromoRequest.setIsPercent(false);
        testPromoRequest.setDiscountValue(BigDecimal.valueOf(50.0));
        when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));

        // Assert
        verify(promoRepository).save(any(Promo.class));
    }

    /**
     * Purpose: Verify successful creation of a promo with a future start date.
     */
    @Test
    @DisplayName("createPromo - Success - Future start date")
    void createPromo_FutureStartDate_Success() {
        // Arrange
        testPromoRequest.setStartDate(LocalDate.now().plusDays(10));
        testPromoRequest.setExpiryDate(LocalDate.now().plusDays(20));
        when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));

        // Assert
        verify(promoRepository).save(any(Promo.class));
    }

    /**
     * Purpose: Verify successful creation of a promo with a long description.
     */
    @Test
    @DisplayName("createPromo - Success - Long description")
    void createPromo_LongDescription_Success() {
        // Arrange
        testPromoRequest.setDescription("A".repeat(500));
        when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));

        // Assert
        verify(promoRepository).save(any(Promo.class));
    }

    /**
     * Purpose: Verify successful creation of a promo with no expiry date.
     */
    @Test
    @DisplayName("createPromo - Success - No expiry date")
    void createPromo_NoExpiryDate_Success() {
        // Arrange
        testPromoRequest.setExpiryDate(null);
        when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));

        // Assert
        verify(promoRepository).save(any(Promo.class));
    }

    /**
     * Purpose: Verify that the promo code is saved in consistent casing
     * (UPPERCASE).
     */
    @Test
    @DisplayName("createPromo - Success - Promo code casing normalization")
    void createPromo_PromoCodeNormalization_Success() {
        // Arrange
        testPromoRequest.setPromoCode("lowercode123");
        when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));

        // Assert
        verify(promoRepository).save(argThat(promo -> promo.getPromoCode().equals("LOWERCODE123")));
    }

    /**
     * Purpose: Verify that lowercase promo codes are handled correctly.
     */
    @Test
    @DisplayName("createPromo - Success - Small Case Promo Code")
    void createPromo_SmallCaseCode_Success() {
        // Arrange
        testPromoRequest.setPromoCode("promo10");
        when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));

        // Assert
        verify(promoRepository).save(any(Promo.class));
    }

    /**
     * Purpose: Verify successful creation of a promo with a valid promo code
     * string.
     */
    @Test
    @DisplayName("createPromo - Success - Valid Code String")
    void createPromo_ValidCodeString_Success() {
        // Arrange
        testPromoRequest.setPromoCode("SUMMER2024");
        when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));

        // Assert
        verify(promoRepository).save(any(Promo.class));
    }

    /**
     * Purpose: Verify successful creation of a promo with numeric promo code.
     */
    @Test
    @DisplayName("createPromo - Success - Valid Numeric Code")
    void createPromo_ValidNumericCode_Success() {
        // Arrange
        testPromoRequest.setPromoCode("123456");
        when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));

        // Assert
        verify(promoRepository).save(any(Promo.class));
    }

    /*
     **********************************************************************************************
     * SECTION 2: FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify rejection of promo when a duplicate code overlaps within the
     * same client and dates.
     */
    @Test
    @DisplayName("createPromo - Failure - Duplicate overlapping promo code")
    void createPromo_DuplicateOverlappingPromo_ThrowsBadRequestException() {
        // Arrange
        when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                .thenReturn(List.of(testPromo));

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.OverlappingPromoCode, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection of promo when the description is empty.
     */
    @Test
    @DisplayName("createPromo - Failure - Empty description")
    void createPromo_EmptyDescription_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setDescription("");

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.DescriptionRequired, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection of promo when the description is null.
     */
    @Test
    @DisplayName("createPromo - Failure - Null description")
    void createPromo_NullDescription_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setDescription(null);

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.DescriptionRequired, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection of promo when the discount value is null.
     */
    @Test
    @DisplayName("createPromo - Failure - Null discount value")
    void createPromo_NullDiscountValue_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setDiscountValue(null);

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.DiscountValueGreaterThanZero, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection of promo when the promo request is null.
     */
    @Test
    @DisplayName("createPromo - Failure - Null request model")
    void createPromo_NullRequest_ThrowsBadRequestException() {
        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(null));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidRequest, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection of promo when the promo code is empty.
     */
    @Test
    @DisplayName("createPromo - Failure - Empty promo code")
    void createPromo_EmptyPromoCode_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setPromoCode("");

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidPromoCode, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection of promo when the promo code is null.
     */
    @Test
    @DisplayName("createPromo - Failure - Null promo code")
    void createPromo_NullPromoCode_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setPromoCode(null);

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidPromoCode, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection of promo when the promo code contains only
     * whitespace.
     */
    @Test
    @DisplayName("createPromo - Failure - Whitespace promo code")
    void createPromo_WhitespacePromoCode_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setPromoCode("   ");

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidPromoCode, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection of promo when the description contains only
     * whitespace.
     */
    @Test
    @DisplayName("createPromo - Failure - Whitespace description")
    void createPromo_WhitespaceDescription_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setDescription("   ");

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.DescriptionRequired, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection of promo when discount value is negative.
     */
    @Test
    @DisplayName("createPromo - Failure - Negative discount value")
    void createPromo_DiscountValueNegative_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setDiscountValue(BigDecimal.valueOf(-10));

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.DiscountValueGreaterThanZero, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection of promo when percentage discount is over 100.
     */
    @Test
    @DisplayName("createPromo - Failure - Percentage over 100")
    void createPromo_PercentageOver100_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setIsPercent(true);
        testPromoRequest.setDiscountValue(BigDecimal.valueOf(101));

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidPercentageValue, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection of promo when expiry date is before start date.
     */
    @Test
    @DisplayName("createPromo - Failure - Expiry date before start date")
    void createPromo_ExpiryBeforeStart_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setStartDate(LocalDate.now());
        testPromoRequest.setExpiryDate(LocalDate.now().minusDays(1));

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.ExpiryDateMustBeAfterStartDate, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection of promo when promo code has special characters.
     */
    @Test
    @DisplayName("createPromo - Failure - Special characters in promo code")
    void createPromo_PromoCodeSpecialCharacters_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setPromoCode("PROMO@123");

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.PromoCodeAlphaNumeric, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection of promo when promo code is too long.
     */
    @Test
    @DisplayName("createPromo - Failure - Promo code too long")
    void createPromo_PromoCodeTooLong_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setPromoCode("A".repeat(51));

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.PromoCodeLength, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection of promo when description is too long.
     */
    @Test
    @DisplayName("createPromo - Failure - Description too long")
    void createPromo_DescriptionTooLong_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setDescription("A".repeat(1001));

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.LongDescriptionTooLong, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection when start date is null.
     */
    @Test
    @DisplayName("createPromo - Failure - Null start date")
    void createPromo_NullStartDate_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setStartDate(null);

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidStartDate, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection when promo code has tab.
     */
    @Test
    @DisplayName("createPromo - Failure - Code with tab")
    void createPromo_InvalidCode_Tab() {
        // Arrange
        testPromoRequest.setPromoCode("\t\t\t");

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidPromoCode, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection when promo code has newline.
     */
    @Test
    @DisplayName("createPromo - Failure - Code with newline")
    void createPromo_InvalidCode_Newline() {
        // Arrange
        testPromoRequest.setPromoCode("\n\n\n");

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidPromoCode, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection when description has tab.
     */
    @Test
    @DisplayName("createPromo - Failure - Description with tab")
    void createPromo_InvalidDescription_Tab() {
        // Arrange
        testPromoRequest.setDescription("\t\t\t");

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.DescriptionRequired, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection when description has newline.
     */
    @Test
    @DisplayName("createPromo - Failure - Description with newline")
    void createPromo_InvalidDescription_Newline() {
        // Arrange
        testPromoRequest.setDescription("\n\n\n");

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.DescriptionRequired, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection when discount value is zero.
     */
    @Test
    @DisplayName("createPromo - Failure - Zero discount value")
    void createPromo_DiscountValueZero_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setDiscountValue(BigDecimal.ZERO);

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.DiscountValueGreaterThanZero, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection when client ID mismatch.
     */
    @Test
    @DisplayName("createPromo - Failure - Client ID mismatch")
    void createPromo_ClientIdMismatch_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setClientId(999L); // Different from TEST_CLIENT_ID

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.ClientIdMismatch, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection when promo code too short.
     */
    @Test
    @DisplayName("createPromo - Failure - Promo code too short")
    void createPromo_PromoCodeTooShort_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setPromoCode("AB"); // Too short ( assuming min 3)

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.PromoCodeLength, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection when start date far in past.
     */
    @Test
    @DisplayName("createPromo - Success - Start date far in past now allowed")
    void createPromo_StartDateFarPast_Success() {
        // Arrange
        testPromoRequest.setStartDate(LocalDate.now().minusYears(100));
        when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));
    }

    /**
     * Purpose: Verify rejection when discount value is null for fixed.
     */
    @Test
    @DisplayName("createPromo - Failure - Null discount for fixed")
    void createPromo_NullDiscountFixed_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setIsPercent(false);
        testPromoRequest.setDiscountValue(null);

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.DiscountValueGreaterThanZero, ex.getMessage());
    }

    /**
     * Purpose: Verify rejection when discount value is negative for fixed.
     */
    @Test
    @DisplayName("createPromo - Failure - Negative discount for fixed")
    void createPromo_NegativeDiscountFixed_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setIsPercent(false);
        testPromoRequest.setDiscountValue(BigDecimal.valueOf(-1));

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.DiscountValueGreaterThanZero, ex.getMessage());
    }

    /*
     **********************************************************************************************
     * SECTION 3: CONTROLLER PERMISSION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify that the controller correctly delegates createPromo calls to
     * the service layer.
     */
    @Test
    @DisplayName("addPromo - Controller delegates to service")
    void createPromo_ControllerDelegation_Success() {
        // Arrange
        doNothing().when(promoService).createPromo(any(PromoRequestModel.class));

        // Act
        ResponseEntity<?> response = promoController.addPromo(testPromoRequest);

        // Assert
        verify(promoService).createPromo(testPromoRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Purpose: Verify unauthorized access is blocked at the controller level.
     */
    @Test
    @DisplayName("addPromo - Controller Permission - Unauthorized")
    void createPromo_controller_permission_unauthorized() {
        // Arrange
        stubServiceThrowsUnauthorizedException();

        // Act
        ResponseEntity<?> response = promoController.addPromo(testPromoRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify that BadRequestException at service is handled by controller.
     */
    @Test
    @DisplayName("addPromo - Controller handles BadRequestException")
    void createPromo_ControllerHandlesBadRequest() {
        // Arrange
        doThrow(new com.example.SpringApi.Exceptions.BadRequestException("Bad Request"))
                .when(promoService).createPromo(any());

        // Act
        ResponseEntity<?> response = promoController.addPromo(testPromoRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * Purpose: Verify that generic clinical Exception at service is handled by
     * controller.
     */
    @Test
    @DisplayName("addPromo - Controller handles generic Exception")
    void createPromo_ControllerHandlesGenericException() {
        // Arrange
        doThrow(new RuntimeException("Server Error"))
                .when(promoService).createPromo(any());

        // Act
        ResponseEntity<?> response = promoController.addPromo(testPromoRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
