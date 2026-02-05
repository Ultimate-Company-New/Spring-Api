package com.example.SpringApi.Services.Tests.Promo;

import com.example.SpringApi.Controllers.PromoController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.DatabaseModels.Promo;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.SuccessMessages;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test class for PromoService.togglePromo method.
 * 
 * Test count: 20 tests
 * - SUCCESS: 2 tests
 * - FAILURE / EXCEPTION: 18 tests
 */
@DisplayName("PromoService - TogglePromo Tests")
public class TogglePromoTest extends PromoServiceTestBase {

    // ===========================
    // SUCCESS TESTS
    // ===========================

    @Test
    @DisplayName("Toggle Promo - Restore from deleted")
    void togglePromo_RestoreFromDeleted() {
        // Arrange
        testPromo.setIsDeleted(true);
        when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testPromo));
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);

        // Act
        promoService.togglePromo(TEST_PROMO_ID);

        // Assert
        assertFalse(testPromo.getIsDeleted());
        verify(promoRepository).save(testPromo);
    }

    @Test
    @DisplayName("Toggle Promo - Success - Should toggle isDeleted flag and log")
    void togglePromo_Success() {
        // Arrange
        when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testPromo));
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);

        // Act
        promoService.togglePromo(TEST_PROMO_ID);

        // Assert
        assertTrue(testPromo.getIsDeleted()); // Should be toggled from false to true
        verify(promoRepository).findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID);
        verify(promoRepository).save(testPromo);
        verify(userLogService).logData(
                eq(TEST_USER_ID),
                eq(SuccessMessages.PromoSuccessMessages.ToggledPromo + TEST_PROMO_ID),
                eq(ApiRoutes.PromosSubRoute.TOGGLE_PROMO));
    }

    // ===========================
    // FAILURE / EXCEPTION TESTS
    // ===========================

    @TestFactory
    @DisplayName("Toggle Promo - Additional invalid IDs (0, -100, 2, 999, MAX, MIN)")
    Stream<DynamicTest> togglePromo_AdditionalInvalidIds() {
        return Stream.of(2L, 999L, Long.MAX_VALUE, Long.MIN_VALUE, -100L, 0L)
                .map(id -> DynamicTest.dynamicTest("Additional Invalid ID: " + id, () -> {
                    when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID))
                            .thenReturn(Optional.empty());
                    NotFoundException ex = assertThrows(NotFoundException.class,
                            () -> promoService.togglePromo(id));
                    assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, ex.getMessage());
                }));
    }

    @TestFactory
    @DisplayName("Toggle Promo - Boundary IDs (MAX_VALUE - 1, MIN_VALUE + 1)")
    Stream<DynamicTest> togglePromo_BoundaryIds() {
        return Stream.of(Long.MAX_VALUE - 1, Long.MIN_VALUE + 1, Long.MAX_VALUE / 2, Long.MIN_VALUE / 2)
                .map(id -> DynamicTest.dynamicTest("Boundary ID: " + id, () -> {
                    when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID))
                            .thenReturn(Optional.empty());
                    NotFoundException ex = assertThrows(NotFoundException.class,
                            () -> promoService.togglePromo(id));
                    assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, ex.getMessage());
                }));
    }

    @TestFactory
    @DisplayName("Toggle Promo - Common invalid IDs (100, 200, 500, 1000)")
    Stream<DynamicTest> togglePromo_CommonInvalidIds() {
        return Stream.of(100L, 200L, 500L, 1000L, 5000L)
                .map(id -> DynamicTest.dynamicTest("Common Invalid ID: " + id, () -> {
                    when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID))
                            .thenReturn(Optional.empty());
                    NotFoundException ex = assertThrows(NotFoundException.class,
                            () -> promoService.togglePromo(id));
                    assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, ex.getMessage());
                }));
    }

    @Test
    @DisplayName("Toggle Promo - Edge Case - Negative ID")
    void togglePromo_NegativeId_ThrowsNotFoundException() {
        // Arrange
        Long negativeId = -1L;
        when(promoRepository.findByPromoIdAndClientId(negativeId, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            promoService.togglePromo(negativeId);
        });
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, exception.getMessage());
    }

    @Test
    @DisplayName("Toggle Promo - Failure - Promo not found")
    void togglePromo_PromoNotFound_ThrowsNotFoundException() {
        // Arrange
        when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            promoService.togglePromo(TEST_PROMO_ID);
        });
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, exception.getMessage());
        verify(promoRepository).findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID);
        verify(promoRepository, never()).save(any(Promo.class));
        verify(userLogService, never()).logData(anyLong(), any(), any());
    }

    @Test
    @DisplayName("Toggle Promo - Edge Case - Zero ID")
    void togglePromo_ZeroId_ThrowsNotFoundException() {
        // Arrange
        Long zeroId = 0L;
        when(promoRepository.findByPromoIdAndClientId(zeroId, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            promoService.togglePromo(zeroId);
        });
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, exception.getMessage());
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("togglePromo - Verify @PreAuthorize Annotation")
    void togglePromo_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PromoController.class.getMethod("togglePromo", long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on togglePromo");
        assertTrue(annotation.value().contains(Authorizations.DELETE_PROMOS_PERMISSION),
                "@PreAuthorize should reference DELETE_PROMOS_PERMISSION");
    }

    @Test
    @DisplayName("togglePromo - Controller delegates to service")
    void togglePromo_WithValidId_DelegatesToService() {
        PromoController controller = new PromoController(promoService);
        doNothing().when(promoService).togglePromo(TEST_PROMO_ID);

        ResponseEntity<?> response = controller.togglePromo(TEST_PROMO_ID);

        verify(promoService).togglePromo(TEST_PROMO_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
