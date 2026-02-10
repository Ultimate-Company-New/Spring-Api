package com.example.SpringApi.Services.Tests.Promo;

import com.example.SpringApi.Services.PromoService;

import com.example.SpringApi.Controllers.PromoController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.ResponseModels.PromoResponseModel;
import com.example.SpringApi.Models.Authorizations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

/**
 * Test class for PromoService.getPromoDetailsById method.
 * 
 * Test count: 12 tests
 * - SUCCESS: 3 tests
 * - FAILURE / EXCEPTION: 9 tests
 */
@DisplayName("PromoService - GetPromoDetailsById Tests")
public class GetPromoDetailsByIdTest extends PromoServiceTestBase {

    // ===========================
    // SUCCESS TESTS
    // ===========================

    @Test
    @DisplayName("Get Promo Details By ID - Edge Case - Negative ID")
    void getPromoDetailsById_NegativeId_Success() {
        // Arrange
        Long negativeId = -1L;
        when(promoRepository.findByPromoIdAndClientId(negativeId, TEST_CLIENT_ID)).thenReturn(Optional.of(testPromo));

        // Act
        PromoResponseModel result = promoService.getPromoDetailsById(negativeId);

        // Assert
        assertNotNull(result);
        verify(promoRepository).findByPromoIdAndClientId(negativeId, TEST_CLIENT_ID);
    }

    @Test
    @DisplayName("Get Promo Details By ID - Success - Should return promo details")
    void getPromoDetailsById_Success() {
        // Arrange
        when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testPromo));

        // Act
        PromoResponseModel result = promoService.getPromoDetailsById(TEST_PROMO_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_PROMO_ID, result.getPromoId());
        assertEquals(TEST_PROMO_CODE, result.getPromoCode());
        verify(promoRepository).findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID);
    }

    @Test
    @DisplayName("Get Promo Details By ID - Edge Case - Zero ID")
    void getPromoDetailsById_ZeroId_Success() {
        // Arrange
        Long zeroId = 0L;
        when(promoRepository.findByPromoIdAndClientId(zeroId, TEST_CLIENT_ID)).thenReturn(Optional.of(testPromo));

        // Act
        PromoResponseModel result = promoService.getPromoDetailsById(zeroId);

        // Assert
        assertNotNull(result);
        verify(promoRepository).findByPromoIdAndClientId(zeroId, TEST_CLIENT_ID);
    }

    // ===========================
    // FAILURE / EXCEPTION TESTS
    // ===========================

    @TestFactory
    @DisplayName("Get Promo Details By ID - Additional Invalid IDs")
    Stream<DynamicTest> getPromoDetailsById_AdditionalInvalidIds() {
        return Stream.of(-100L, 999L, Long.MAX_VALUE, Long.MIN_VALUE)
                .map(id -> DynamicTest.dynamicTest("Additional Invalid ID: " + id, () -> {
                    when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID)).thenReturn(Optional.empty());
                    NotFoundException ex = assertThrows(NotFoundException.class,
                            () -> promoService.getPromoDetailsById(id));
                    assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, ex.getMessage());
                }));
    }

    @TestFactory
    @DisplayName("Get Promo Details By ID - Invalid IDs")
    Stream<DynamicTest> getPromoDetailsById_InvalidIds() {
        return Stream.of(-10L, 999L, Long.MAX_VALUE, Long.MIN_VALUE)
                .map(id -> DynamicTest.dynamicTest("Invalid ID: " + id, () -> {
                    when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID)).thenReturn(Optional.empty());
                    NotFoundException ex = assertThrows(NotFoundException.class,
                            () -> promoService.getPromoDetailsById(id));
                    assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, ex.getMessage());
                }));
    }

    @Test
    @DisplayName("Get Promo Details By ID - Failure - Promo not found")
    void getPromoDetailsById_PromoNotFound_ThrowsNotFoundException() {
        // Arrange
        when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            promoService.getPromoDetailsById(TEST_PROMO_ID);
        });
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, exception.getMessage());
        verify(promoRepository).findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID);
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("getPromoDetailsById - Verify @PreAuthorize Annotation")
    void getPromoDetailsById_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PromoController.class.getMethod("getPromoDetailsById", long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on getPromoDetailsById");
        assertTrue(annotation.value().contains(Authorizations.VIEW_PROMOS_PERMISSION),
                "@PreAuthorize should reference VIEW_PROMOS_PERMISSION");
    }

    @Test
    @DisplayName("getPromoDetailsById - Controller delegates to service")
    void getPromoDetailsById_WithValidId_DelegatesToService() {
        PromoService mockPromoService = mock(PromoService.class);
        PromoController controller = new PromoController(mockPromoService);
        when(mockPromoService.getPromoDetailsById(TEST_PROMO_ID))
                .thenReturn(mock(PromoResponseModel.class));

        ResponseEntity<?> response = controller.getPromoDetailsById(TEST_PROMO_ID);

        verify(mockPromoService).getPromoDetailsById(TEST_PROMO_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
