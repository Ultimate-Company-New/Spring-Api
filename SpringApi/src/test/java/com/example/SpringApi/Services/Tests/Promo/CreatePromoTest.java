package com.example.SpringApi.Services.Tests.Promo;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.Promo;
import com.example.SpringApi.Models.RequestModels.PromoRequestModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for PromoService.createPromo method.
 */
@DisplayName("PromoService - CreatePromo Tests")
class CreatePromoTest extends PromoServiceTestBase {


    // Total Tests: 13
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify basic valid promo creation.
     * Expected Result: Promo saved.
     * Assertions: save called.
     */
    @Test
    @DisplayName("createPromo - All Valid - Success")
    void createPromo_AllValid_Success() {
        // Arrange
        stubPromoRepositoryFindOverlappingPromos(Collections.emptyList());
        stubPromoRepositorySave(testPromo);

        // Act
        promoService.createPromo(testPromoRequest);

        // Assert
        verify(promoRepository, times(1)).save(any(Promo.class));
    }




    /*
     * Purpose: Verify fixed value discount promo creation.
     * Expected Result: Promo saved.
     * Assertions: save called.
     */
    @Test
    @DisplayName("createPromo - Fixed Value Discount - Success")
    void createPromo_FixedValueDiscount_Success() {
        // Arrange
        PromoRequestModel request = testPromoRequest;
        request.setIsPercent(false);
        stubPromoRepositoryFindOverlappingPromos(Collections.emptyList());
        stubPromoRepositorySave(testPromo);

        // Act
        promoService.createPromo(request);

        // Assert
        verify(promoRepository, times(1)).save(any(Promo.class));
    }




    /*
     * Purpose: Verify future start date accepted.
     * Expected Result: Promo saved.
     * Assertions: save called.
     */
    @Test
    @DisplayName("createPromo - Future Start Date - Success")
    void createPromo_FutureStartDate_Success() {
        // Arrange
        stubPromoRepositoryFindOverlappingPromos(Collections.emptyList());
        stubPromoRepositorySave(testPromo);

        // Act
        promoService.createPromo(testPromoRequest);

        // Assert
        verify(promoRepository, times(1)).save(any(Promo.class));
    }




    /*
     * Purpose: Verify long description accepted.
     * Expected Result: Promo saved.
     * Assertions: save called.
     */
    @Test
    @DisplayName("createPromo - Long Description - Success")
    void createPromo_LongDescription_Success() {
        // Arrange
        testPromoRequest.setDescription("A".repeat(250));
        stubPromoRepositoryFindOverlappingPromos(Collections.emptyList());
        stubPromoRepositorySave(testPromo);

        // Act
        promoService.createPromo(testPromoRequest);

        // Assert
        verify(promoRepository, times(1)).save(any(Promo.class));
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify discount value zero throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: exception thrown.
     */
    @Test
    @DisplayName("createPromo - Discount Value Zero - Throws BadRequestException")
    void createPromo_DiscountValueZero_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setDiscountValue(java.math.BigDecimal.ZERO);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.DISCOUNT_VALUE_GREATER_THAN_ZERO, ex.getMessage());
    }




    /*
     * Purpose: Verify duplicate overlapping promo throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: exception thrown.
     */
    @Test
    @DisplayName("createPromo - Duplicate Overlapping Promo - Throws BadRequestException")
    void createPromo_DuplicateOverlappingPromo_ThrowsBadRequestException() {
        // Arrange
        stubPromoRepositoryFindOverlappingPromos(Collections.singletonList(testPromo));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.OVERLAPPING_PROMO_CODE, ex.getMessage());
    }




    /*
     * Purpose: Verify empty description throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: exception thrown.
     */
    @Test
    @DisplayName("createPromo - Empty Description - Throws BadRequestException")
    void createPromo_EmptyDescription_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setDescription("");

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.DESCRIPTION_REQUIRED, ex.getMessage());
    }




    /*
     * Purpose: Verify empty promo code throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: exception thrown.
     */
    @Test
    @DisplayName("createPromo - Empty Promo Code - Throws BadRequestException")
    void createPromo_EmptyPromoCode_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setPromoCode("");

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.createPromo(testPromoRequest));
        assertEquals(ErrorMessages.PromoErrorMessages.INVALID_PROMO_CODE, ex.getMessage());
    }




    /*
     * Purpose: Verify null request throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: exception thrown.
     */
    @Test
    @DisplayName("createPromo - Null Request - Throws BadRequestException")
    void createPromo_NullRequest_ThrowsBadRequestException() {
        // Arrange
        PromoRequestModel request = null;

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class, () -> promoService.createPromo(request));
        assertEquals(ErrorMessages.PromoErrorMessages.INVALID_REQUEST, ex.getMessage());
    }

    /*
     **********************************************************************************************
     * PERMISSION TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify unauthorized access is blocked at controller level.
     * Expected Result: Unauthorized status.
     * Assertions: status is 401.
     */
    @Test
    @DisplayName("createPromo - Controller Permission - Unauthorized")
    void createPromo_controller_permission_unauthorized() {
        // Arrange
        stubServiceThrowsUnauthorizedException();

        // Act
        ResponseEntity<?> response = promoController.addPromo(testPromoRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }


    /*
     * Purpose: Verify controller delegation.
     * Expected Result: OK status.
     * Assertions: status is OK.
     */
    @Test
    @DisplayName("createPromo - Controller Delegation - Success")
    void createPromo_ControllerDelegation_Success() {
        // Arrange
        stubServiceCreatePromoDoNothing();

        // Act
        ResponseEntity<?> response = promoController.addPromo(testPromoRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }




    /*
     * Purpose: Verify controller handles bad request.
     * Expected Result: BAD_REQUEST status.
     * Assertions: status is 400.
     */
    @Test
    @DisplayName("createPromo - Controller Handles BadRequest - Success")
    void createPromo_ControllerHandlesBadRequest_Success() {
        // Arrange
        stubServiceCreatePromoThrowsBadRequest("Bad Request");

        // Act
        ResponseEntity<?> response = promoController.addPromo(testPromoRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }




    /*
     * Purpose: Verify controller handles generic exception.
     * Expected Result: INTERNAL_SERVER_ERROR status.
     * Assertions: status is 500.
     */
    @Test
    @DisplayName("createPromo - Controller Handles Generic Exception - Failure")
    void createPromo_ControllerHandlesGenericException_Failure() {
        // Arrange
        stubServiceCreatePromoThrowsRuntime("Crash");

        // Act
        ResponseEntity<?> response = promoController.addPromo(testPromoRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

}
