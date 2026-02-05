package com.example.SpringApi.Services.Tests.Promo;

import com.example.SpringApi.Controllers.PromoController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.DatabaseModels.Promo;
import com.example.SpringApi.Models.RequestModels.PromoRequestModel;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.SuccessMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

/**
 * Test class for PromoService.createPromo method.
 * 
 * Test count: 8 tests
 * - SUCCESS: 4 tests
 * - FAILURE / EXCEPTION: 4 tests
 */
@DisplayName("PromoService - CreatePromo Tests")
public class CreatePromoTest extends PromoServiceTestBase {

    // ===========================
    // SUCCESS TESTS
    // ===========================

    @Test
    @DisplayName("Create Promo - Non-percent discount over 100 allowed")
    void createPromo_NonPercentDiscountOver100_Allows() {
        // Arrange
        testPromoRequest.setIsPercent(false);
        testPromoRequest.setDiscountValue(new BigDecimal("150"));
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);

        // Act & Assert
        assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));
    }

    @Test
    @DisplayName("Create Promo - Normalizes promo code and description")
    void createPromo_NormalizesFields() {
        // Arrange
        testPromoRequest.setPromoCode("  test10 ");
        testPromoRequest.setDescription("  Promo Desc  ");

        ArgumentCaptor<Promo> captor = ArgumentCaptor.forClass(Promo.class);
        when(promoRepository.save(captor.capture())).thenReturn(testPromo);

        // Act
        promoService.createPromo(testPromoRequest);

        // Assert
        Promo saved = captor.getValue();
        assertEquals("TEST10", saved.getPromoCode());
        assertEquals("Promo Desc", saved.getDescription());
    }

    @Test
    @DisplayName("Create Promo - Success - Should create and save promo with logging")
    void createPromo_Success() {
        // Arrange
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);

        // Act
        promoService.createPromo(testPromoRequest);

        // Assert
        verify(promoRepository).save(any(Promo.class));
        verify(userLogService).logData(
                eq(TEST_USER_ID),
                eq(SuccessMessages.PromoSuccessMessages.CreatePromo + TEST_PROMO_CODE),
                eq(ApiRoutes.PromosSubRoute.CREATE_PROMO));
    }

    @Test
    @DisplayName("Create Promo - Zero discount value allowed")
    void createPromo_ZeroDiscountValue_Allows() {
        // Arrange
        testPromoRequest.setDiscountValue(BigDecimal.ZERO);
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);

        // Act & Assert
        assertDoesNotThrow(() -> promoService.createPromo(testPromoRequest));
    }

    // ===========================
    // FAILURE / EXCEPTION TESTS
    // ===========================

    @Test
    @DisplayName("Create Promo - Failure - Empty promo code throws BadRequestException")
    void createPromo_EmptyPromoCode_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setPromoCode("");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            promoService.createPromo(testPromoRequest);
        });
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidPromoCode, exception.getMessage());
    }

    @Test
    @DisplayName("Create Promo - Failure - Null promo code throws NullPointerException")
    void createPromo_NullPromoCode_ThrowsNullPointerException() {
        // Arrange
        testPromoRequest.setPromoCode(null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            promoService.createPromo(testPromoRequest);
        });
    }

    @Test
    @DisplayName("Create Promo - Failure - Null request model throws BadRequestException")
    void createPromo_NullRequestModel_ThrowsBadRequestException() {
        // Arrange
        PromoRequestModel nullRequest = null;

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            promoService.createPromo(nullRequest);
        });
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidRequest, exception.getMessage());
    }

    @Test
    @DisplayName("Create Promo - Failure - Whitespace promo code throws BadRequestException")
    void createPromo_WhitespacePromoCode_ThrowsBadRequestException() {
        // Arrange
        testPromoRequest.setPromoCode("   ");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            promoService.createPromo(testPromoRequest);
        });
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidPromoCode, exception.getMessage());
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("addPromo - Verify @PreAuthorize Annotation")
    void addPromo_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PromoController.class.getMethod("addPromo", PromoRequestModel.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on addPromo");
        assertTrue(annotation.value().contains(Authorizations.INSERT_PROMOS_PERMISSION),
                "@PreAuthorize should reference INSERT_PROMOS_PERMISSION");
    }

    @Test
    @DisplayName("addPromo - Controller delegates to service")
    void addPromo_WithValidRequest_DelegatesToService() {
        PromoController controller = new PromoController(promoService);
        doNothing().when(promoService).createPromo(testPromoRequest);

        ResponseEntity<?> response = controller.addPromo(testPromoRequest);

        verify(promoService).createPromo(testPromoRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
