package com.example.SpringApi.Services.Tests.Promo;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.ResponseModels.PromoResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Test class for PromoService.getPromoDetailsById method.
 */
@DisplayName("PromoService - GetPromoDetailsById Tests")
class GetPromoDetailsByIdTest extends PromoServiceTestBase {

    // Total Tests: 17

    /*
     **********************************************************************************************
     * SECTION 1: SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify that promo details can be successfully retrieved by a valid
     * ID.
     */
    @Test
    @DisplayName("Get Promo Details By Id - Valid ID - Success")
    void getPromoDetailsById_ValidId_Success() {
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

    /**
     * Purpose: Verify that details of a deleted promo can still be retrieved by ID
     * (for administrative visibility).
     */
    @Test
    @DisplayName("Get Promo Details By Id - Deleted promo - Success")
    void getPromoDetailsById_DeletedPromo_Success() {
        // Arrange
        testPromo.setIsDeleted(true);
        when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testPromo));

        // Act
        PromoResponseModel result = promoService.getPromoDetailsById(TEST_PROMO_ID);

        // Assert
        assertNotNull(result);
        assertEquals(true, result.getIsDeleted());
    }

    /**
     * Purpose: Verify that details can be retrieved for a promo with maximum
     * possible ID.
     */
    @Test
    @DisplayName("Get Promo Details By Id - Max Long ID - Success")
    void getPromoDetailsById_MaxLongId_Success() {
        // Arrange
        long maxId = Long.MAX_VALUE;
        testPromo.setPromoId(maxId);
        when(promoRepository.findByPromoIdAndClientId(maxId, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testPromo));

        // Act
        PromoResponseModel result = promoService.getPromoDetailsById(maxId);

        // Assert
        assertEquals(maxId, result.getPromoId());
    }

    /**
     * Purpose: Verify retrieval of an inactive promo.
     */
    @Test
    @DisplayName("Get Promo Details By Id - Inactive Promo - Success")
    void getPromoDetailsById_InactivePromo_Success() {
        // Arrange
        testPromo.setExpiryDate(java.time.LocalDate.now().minusDays(1));
        when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testPromo));

        // Act
        PromoResponseModel result = promoService.getPromoDetailsById(TEST_PROMO_ID);

        // Assert
        assertNotNull(result);
    }

    /**
     * Purpose: Verify retrieval with large numeric ID.
     */
    @Test
    @DisplayName("Get Promo Details By Id - Large numeric ID")
    void getPromoDetailsById_LargeId_Success() {
        // Arrange
        long largeId = 999999999L;
        testPromo.setPromoId(largeId);
        when(promoRepository.findByPromoIdAndClientId(largeId, TEST_CLIENT_ID))
                .thenReturn(Optional.of(testPromo));

        // Act
        PromoResponseModel result = promoService.getPromoDetailsById(largeId);

        // Assert
        assertEquals(largeId, result.getPromoId());
    }

    /*
     **********************************************************************************************
     * SECTION 2: FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify that accessing a promo belonging to a different client throws
     * NotFoundException.
     */
    @Test
    @DisplayName("Get Promo Details By Id - Different client - Throws NotFoundException")
    void getPromoDetailsById_DifferentClient_ThrowsNotFoundException() {
        // Arrange
        // The service uses getClientId() from base class, which returns TEST_CLIENT_ID
        when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.getPromoDetailsById(TEST_PROMO_ID));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that providing an ID of 999L (not in DB) throws
     * NotFoundException.
     */
    @Test
    @DisplayName("Get Promo Details By Id - Invalid ID 999L - Throws NotFoundException")
    void getPromoDetailsById_InvalidId_999L() {
        // Arrange
        long id = 999L;
        when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.getPromoDetailsById(id));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that providing an ID of 0 throws NotFoundException.
     */
    @Test
    @DisplayName("Get Promo Details By Id - Invalid ID 0 - Throws NotFoundException")
    void getPromoDetailsById_InvalidId_Zero() {
        // Arrange
        long id = 0L;
        when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.getPromoDetailsById(id));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that providing a negative ID of -100L throws
     * NotFoundException.
     */
    @Test
    @DisplayName("Get Promo Details By Id - Invalid ID -100L - Throws NotFoundException")
    void getPromoDetailsById_InvalidId_Negative100L() {
        // Arrange
        long id = -100L;
        when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.getPromoDetailsById(id));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that providing Min Value for ID throws NotFoundException.
     */
    @Test
    @DisplayName("Get Promo Details By Id - Invalid ID MinLong - Throws NotFoundException")
    void getPromoDetailsById_InvalidId_MinLong() {
        // Arrange
        long id = Long.MIN_VALUE;
        when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.getPromoDetailsById(id));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that providing Max Value for ID throws NotFoundException if
     * not in DB.
     */
    @Test
    @DisplayName("Get Promo Details By Id - Invalid ID MaxLong - Throws NotFoundException")
    void getPromoDetailsById_InvalidId_MaxLong_NotFound() {
        // Arrange
        long id = Long.MAX_VALUE;
        when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.getPromoDetailsById(id));
        assertEquals(ErrorMessages.PromoErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify that providing an ID of -1 throws NotFoundException.
     */
    @Test
    @DisplayName("Get Promo Details By Id - Negative 1 ID - Throws NotFoundException")
    void getPromoDetailsById_Negative1Id_ThrowsNotFoundException() {
        // Arrange
        long id = -1L;
        when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> promoService.getPromoDetailsById(id));
    }

    /*
     **********************************************************************************************
     * SECTION 3: CONTROLLER PERMISSION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify that the controller correctly delegates getPromoDetailsById
     * calls to the service layer.
     */
    @Test
    @DisplayName("getPromoDetailsById - Controller delegates to service")
    void getPromoDetailsById_WithValidId_DelegatesToService() {
        // Arrange
        PromoResponseModel mockResponse = new PromoResponseModel(testPromo);
        // Using doReturn because service is a spy
        doReturn(mockResponse).when(promoService).getPromoDetailsById(TEST_PROMO_ID);

        // Act
        ResponseEntity<?> response = promoController.getPromoDetailsById(TEST_PROMO_ID);

        // Assert
        verify(promoService).getPromoDetailsById(TEST_PROMO_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    /**
     * Purpose: Verify unauthorized access is blocked at the controller level.
     */
    @Test
    @DisplayName("getPromoDetailsById - Controller Permission - Unauthorized")
    void getPromoDetailsById_controller_permission_unauthorized() {
        // Arrange
        stubServiceThrowsUnauthorizedException();

        // Act
        ResponseEntity<?> response = promoController.getPromoDetailsById(TEST_PROMO_ID);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify controller handles NotFoundException from service.
     */
    @Test
    @DisplayName("getPromoDetailsById - Controller handles NotFoundException")
    void getPromoDetailsById_ControllerHandlesNotFound() {
        // Arrange
        doThrow(new com.example.SpringApi.Exceptions.NotFoundException("Not Found"))
                .when(promoService).getPromoDetailsById(anyLong());

        // Act
        ResponseEntity<?> response = promoController.getPromoDetailsById(TEST_PROMO_ID);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Purpose: Verify controller handles BadRequestException from service.
     */
    @Test
    @DisplayName("getPromoDetailsById - Controller handles BadRequestException")
    void getPromoDetailsById_ControllerHandlesBadRequest() {
        // Arrange
        doThrow(new com.example.SpringApi.Exceptions.BadRequestException("Bad ID"))
                .when(promoService).getPromoDetailsById(anyLong());

        // Act
        ResponseEntity<?> response = promoController.getPromoDetailsById(TEST_PROMO_ID);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * Purpose: Verify controller handles generic server error.
     */
    @Test
    @DisplayName("getPromoDetailsById - Controller handles Exception")
    void getPromoDetailsById_ControllerHandlesException() {
        // Arrange
        doThrow(new RuntimeException("Crash"))
                .when(promoService).getPromoDetailsById(anyLong());

        // Act
        ResponseEntity<?> response = promoController.getPromoDetailsById(TEST_PROMO_ID);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
