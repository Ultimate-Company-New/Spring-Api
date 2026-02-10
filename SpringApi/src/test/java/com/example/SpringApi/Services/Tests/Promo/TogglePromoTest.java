package com.example.SpringApi.Services.Tests.Promo;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.DatabaseModels.Promo;
import com.example.SpringApi.SuccessMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for PromoService.togglePromo method.
 */
@DisplayName("PromoService - TogglePromo Tests")
class TogglePromoTest extends PromoServiceTestBase {

    // Total Tests: 16

    /*
     **********************************************************************************************
     * SECTION 1: SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify that a deleted promo can be restored using togglePromo.
     */
    @Test
    @DisplayName("Toggle Promo - Restore from deleted - Success")
    void togglePromo_RestoreFromDeleted_Success() {
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

    /**
     * Purpose: Verify successful toggling of the isDeleted flag and corresponding
     * logging.
     */
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
                TEST_USER_ID,
                SuccessMessages.PromoSuccessMessages.ToggledPromo + TEST_PROMO_ID,
                ApiRoutes.PromosSubRoute.TOGGLE_PROMO);
    }

    /**
     * Purpose: Verify that toggling twice returns the promo to its original state.
     */
    @Test
    @DisplayName("Toggle Promo - Success - Double Toggle")
    void togglePromo_DoubleToggle_Success() {
        // Arrange
        when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testPromo));
        when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);

        // Act
        promoService.togglePromo(TEST_PROMO_ID); // To true
        promoService.togglePromo(TEST_PROMO_ID); // Back to false

        // Assert
        assertFalse(testPromo.getIsDeleted());
        verify(promoRepository, times(2)).save(testPromo);
    }

    /**
     * Purpose: Verify isolation when toggling (one promo toggle doesn't affect
     * another).
     */
    @Test
    @DisplayName("Toggle Promo - Success - Isolation")
    void togglePromo_Isolation_Success() {
        // Arrange
        Promo p2 = new Promo();
        p2.setPromoId(999L);
        p2.setIsDeleted(false);
        lenient().when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testPromo));
        lenient().when(promoRepository.findByPromoIdAndClientId(999L, TEST_CLIENT_ID))
                .thenReturn(Optional.of(p2));

        // Act
        promoService.togglePromo(TEST_PROMO_ID);

        // Assert
        assertTrue(testPromo.getIsDeleted());
        assertFalse(p2.getIsDeleted());
    }

    /*
     **********************************************************************************************
     * SECTION 2: FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify that toggling with invalid ID 999L throws NotFoundException.
     */
    @Test
    @DisplayName("Toggle Promo - Invalid ID 999L - Throws NotFoundException")
    void togglePromo_InvalidId_999L() {
        // Arrange
        long id = 999L;
        when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.togglePromo(id));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that toggling with invalid ID Max Value throws
     * NotFoundException.
     */
    @Test
    @DisplayName("Toggle Promo - Invalid ID Max Value - Throws NotFoundException")
    void togglePromo_InvalidId_MaxLong() {
        // Arrange
        long id = Long.MAX_VALUE;
        when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.togglePromo(id));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that toggling with invalid ID Min Value throws
     * NotFoundException.
     */
    @Test
    @DisplayName("Toggle Promo - Invalid ID Min Value - Throws NotFoundException")
    void togglePromo_InvalidId_MinLong() {
        // Arrange
        long id = Long.MIN_VALUE;
        when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.togglePromo(id));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that toggling with invalid ID -100L throws NotFoundException.
     */
    @Test
    @DisplayName("Toggle Promo - Invalid ID -100L - Throws NotFoundException")
    void togglePromo_InvalidId_Negative100L() {
        // Arrange
        long id = -100L;
        when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.togglePromo(id));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Reject toggling if ID is negative.
     */
    @Test
    @DisplayName("Toggle Promo - Edge Case - Negative ID - Throws NotFoundException")
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

    /**
     * Purpose: Reject toggling if promo is not found in the repository.
     */
    @Test
    @DisplayName("Toggle Promo - Failure - Promo not found - Throws NotFoundException")
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
    }

    /**
     * Purpose: Reject toggling if ID is zero.
     */
    @Test
    @DisplayName("Toggle Promo - Edge Case - Zero ID - Throws NotFoundException")
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

    /**
     * Purpose: Verify that accessing a promo belonging to a different client throws
     * NotFoundException.
     */
    @Test
    @DisplayName("Toggle Promo - Different client - Throws NotFoundException")
    void togglePromo_DifferentClient_ThrowsNotFoundException() {
        // Arrange
        when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> promoService.togglePromo(TEST_PROMO_ID));
    }

    /*
     **********************************************************************************************
     * SECTION 3: CONTROLLER PERMISSION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify that the controller correctly delegates togglePromo calls to
     * the service layer.
     */
    @Test
    @DisplayName("togglePromo - Controller delegates to service")
    void togglePromo_WithValidId_DelegatesToService() {
        // Arrange
        doNothing().when(promoService).togglePromo(TEST_PROMO_ID);

        // Act
        ResponseEntity<?> response = promoController.togglePromo(TEST_PROMO_ID);

        // Assert
        verify(promoService).togglePromo(TEST_PROMO_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Purpose: Verify unauthorized access is blocked at the controller level.
     */
    @Test
    @DisplayName("togglePromo - Controller Permission - Unauthorized")
    void togglePromo_controller_permission_unauthorized() {
        // Arrange
        stubServiceThrowsUnauthorizedException();

        // Act
        ResponseEntity<?> response = promoController.togglePromo(TEST_PROMO_ID);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify controller handles NotFoundException from service.
     */
    @Test
    @DisplayName("togglePromo - Controller handles NotFoundException")
    void togglePromo_ControllerHandlesNotFound() {
        // Arrange
        doThrow(new com.example.SpringApi.Exceptions.NotFoundException("Not Found"))
                .when(promoService).togglePromo(anyLong());

        // Act
        ResponseEntity<?> response = promoController.togglePromo(TEST_PROMO_ID);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Purpose: Verify controller handles generic server error.
     */
    @Test
    @DisplayName("togglePromo - Controller handles Exception")
    void togglePromo_ControllerHandlesException() {
        // Arrange
        doThrow(new RuntimeException("Crash"))
                .when(promoService).togglePromo(anyLong());

        // Act
        ResponseEntity<?> response = promoController.togglePromo(TEST_PROMO_ID);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
