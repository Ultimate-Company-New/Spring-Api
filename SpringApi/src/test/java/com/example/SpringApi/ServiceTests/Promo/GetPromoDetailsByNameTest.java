package com.example.SpringApi.ServiceTests.Promo;

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
import static org.mockito.Mockito.*;

/**
 * Test class for PromoService.getPromoDetailsByName method.
 */
@DisplayName("PromoService - GetPromoDetailsByName Tests")
class GetPromoDetailsByNameTest extends PromoServiceTestBase {


    // Total Tests: 19
    /*
     **********************************************************************************************
     * SECTION 1: SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify that a deleted promo can still be found by its name.
     */
    @Test
    @DisplayName("Get Promo Details By Name - Deleted promo - Success")
    void getPromoDetailsByName_DeletedPromo_Success() {
        // Arrange
        testPromo.setIsDeleted(true);
        stubPromoRepositoryFindByPromoCodeAndClientId(TEST_PROMO_CODE, TEST_CLIENT_ID, Optional.of(testPromo));

        // Act
        PromoResponseModel result = promoService.getPromoDetailsByName(TEST_PROMO_CODE);

        // Assert
        assertNotNull(result);
        assertEquals(true, result.getIsDeleted());
    }




    /**
     * Purpose: Verify that the retrieval is case-insensitive (automatically
     * converts to uppercase).
     */
    @Test
    @DisplayName("Get Promo Details By Name - Lowercase code - Success (Normalization)")
    void getPromoDetailsByName_LowercaseCode_Success() {
        // Arrange
        String lowercaseCode = "test10";
        stubPromoRepositoryFindByPromoCodeAndClientId(TEST_PROMO_CODE, TEST_CLIENT_ID, Optional.of(testPromo));

        // Act
        PromoResponseModel result = promoService.getPromoDetailsByName(lowercaseCode);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_PROMO_CODE, result.getPromoCode());
    }




    /**
     * Purpose: Verify retrieval with mixed characters.
     */
    @Test
    @DisplayName("Get Promo Details By Name - Mixed Char Code - Success")
    void getPromoDetailsByName_MixedChars_Success() {
        // Arrange
        String code = "MIX-123_CODE";
        testPromo.setPromoCode(code);
        stubPromoRepositoryFindByPromoCodeAndClientId(code, TEST_CLIENT_ID, Optional.of(testPromo));

        // Act
        PromoResponseModel result = promoService.getPromoDetailsByName(code);

        // Assert
        assertEquals(code, result.getPromoCode());
    }




    /**
     * Purpose: Verify retrieval with numeric name.
     */
    @Test
    @DisplayName("Get Promo Details By Name - Numeric Code - Success")
    void getPromoDetailsByName_NumericCode_Success() {
        // Arrange
        String code = "12345";
        testPromo.setPromoCode(code);
        stubPromoRepositoryFindByPromoCodeAndClientId(code, TEST_CLIENT_ID, Optional.of(testPromo));

        // Act
        PromoResponseModel result = promoService.getPromoDetailsByName(code);

        // Assert
        assertEquals(code, result.getPromoCode());
    }




    /**
     * Purpose: Verify that promo details can be successfully retrieved using a
     * valid promo code.
     */
    @Test
    @DisplayName("Get Promo Details By Name - Valid Code - Success")
    void getPromoDetailsByName_ValidCode_Success() {
        // Arrange
        stubPromoRepositoryFindByPromoCodeAndClientId(TEST_PROMO_CODE, TEST_CLIENT_ID, Optional.of(testPromo));

        // Act
        PromoResponseModel result = promoService.getPromoDetailsByName(TEST_PROMO_CODE);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_PROMO_CODE, result.getPromoCode());
        verify(promoRepository).findByPromoCodeAndClientId(TEST_PROMO_CODE, TEST_CLIENT_ID);
    }

    /*
     **********************************************************************************************
     * SECTION 2: FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify rejection of search if name belongs to different client.
     */
    @Test
    @DisplayName("Get Promo Details By Name - Different client - Throws NotFoundException")
    void getPromoDetailsByName_DifferentClient_ThrowsNotFoundException() {
        // Arrange
        stubPromoRepositoryFindByPromoCodeAndClientId(TEST_PROMO_CODE, TEST_CLIENT_ID, Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.getPromoDetailsByName(TEST_PROMO_CODE));
        assertEquals(ErrorMessages.PromoErrorMessages.INVALID_NAME, ex.getMessage());
    }




    /**
     * Purpose: Reject search if code is empty.
     */
    @Test
    @DisplayName("Get Promo Details By Name - Empty promo code - Throws BadRequestException")
    void getPromoDetailsByName_EmptyCode_ThrowsBadRequestException() {
        // Arrange

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.getPromoDetailsByName(""));
        assertEquals(ErrorMessages.PromoErrorMessages.INVALID_PROMO_CODE, ex.getMessage());
    }




    /**
     * Purpose: Reject promo lookup if code does not exist in the DB.
     */
    @Test
    @DisplayName("Get Promo Details By Name - Non-existent code - Throws NotFoundException")
    void getPromoDetailsByName_NonExistentCode_ThrowsNotFoundException() {
        // Arrange
        String code = "GHOSTCODE";
        stubPromoRepositoryFindByPromoCodeAndClientId(code, TEST_CLIENT_ID, Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.getPromoDetailsByName(code));
        assertEquals(ErrorMessages.PromoErrorMessages.INVALID_NAME, ex.getMessage());
    }




    /**
     * Purpose: Reject search if code is null.
     */
    @Test
    @DisplayName("Get Promo Details By Name - Null promo code - Throws BadRequestException")
    void getPromoDetailsByName_NullCode_ThrowsBadRequestException() {
        // Arrange

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.getPromoDetailsByName(null));
        assertEquals(ErrorMessages.PromoErrorMessages.INVALID_PROMO_CODE, ex.getMessage());
    }




    /**
     * Purpose: Reject promo lookup if code is not found (INVALID).
     */
    @Test
    @DisplayName("Get Promo Details By Name - Promo code not found (INVALID)- Throws NotFoundException")
    void getPromoDetailsByName_PromoCodeNotFound_INVALID() {
        // Arrange
        String code = "INVALID";
        stubPromoRepositoryFindByPromoCodeAndClientId(code, TEST_CLIENT_ID, Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.getPromoDetailsByName(code));
        assertEquals(ErrorMessages.PromoErrorMessages.INVALID_NAME, ex.getMessage());
    }




    /**
     * Purpose: Reject promo lookup if code is not found (NOTFOUND).
     */
    @Test
    @DisplayName("Get Promo Details By Name - Promo code not found (NOTFOUND) - Throws NotFoundException")
    void getPromoDetailsByName_PromoCodeNotFound_NOTFOUND() {
        // Arrange
        String code = "NOTFOUND";
        stubPromoRepositoryFindByPromoCodeAndClientId(code, TEST_CLIENT_ID, Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> promoService.getPromoDetailsByName(code));
        assertEquals(ErrorMessages.PromoErrorMessages.INVALID_NAME, ex.getMessage());
    }




    /**
     * Purpose: Verify rejection of search with tab character.
     */
    @Test
    @DisplayName("Get Promo Details By Name - Tab character - Throws BadRequestException")
    void getPromoDetailsByName_TabCode_ThrowsBadRequestException() {
        // Arrange

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.getPromoDetailsByName("\t"));
        assertEquals(ErrorMessages.PromoErrorMessages.INVALID_PROMO_CODE, ex.getMessage());
    }




    /**
     * Purpose: Reject search if code is only whitespace.
     */
    @Test
    @DisplayName("Get Promo Details By Name - Whitespace promo code - Throws BadRequestException")
    void getPromoDetailsByName_WhitespaceCode_ThrowsBadRequestException() {
        // Arrange

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.getPromoDetailsByName("   "));
        assertEquals(ErrorMessages.PromoErrorMessages.INVALID_PROMO_CODE, ex.getMessage());
    }

    /*
     **********************************************************************************************
     * SECTION 3: CONTROLLER PERMISSION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify controller handles context retrieval failure.
     */
    @Test
    @DisplayName("getPromoDetailsByName - Failure - Context retrieval error")
    void getPromoDetailsByName_ContextRetrievalError_Failure() {
        // Arrange
        stubServiceGetPromoDetailsByNameThrowsUnauthorized("Context missing");

        // Act
        ResponseEntity<?> response = promoController.getPromoDetailsByName(TEST_PROMO_CODE);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }




    /**
     * Purpose: Verify unauthorized access is blocked at the controller level.
     */
    @Test
    @DisplayName("getPromoDetailsByName - Controller Permission - Unauthorized")
    void getPromoDetailsByName_controller_permission_unauthorized() {
        // Arrange
        stubServiceThrowsUnauthorizedException();

        // Act
        ResponseEntity<?> response = promoController.getPromoDetailsByName(TEST_PROMO_CODE);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }



    /**
     * Purpose: Verify controller handles BadRequestException from service.
     */
    @Test
    @DisplayName("getPromoDetailsByName - Controller handles BadRequestException")
    void getPromoDetailsByName_ControllerHandlesBadRequest_Success() {
        // Arrange
        stubServiceGetPromoDetailsByNameThrowsBadRequest("Empty name");

        // Act
        ResponseEntity<?> response = promoController.getPromoDetailsByName(" ");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }




    /**
     * Purpose: Verify controller handles generic server error.
     */
    @Test
    @DisplayName("getPromoDetailsByName - Controller handles internal exception")
    void getPromoDetailsByName_ControllerHandlesException_Failure() {
        // Arrange
        stubServiceGetPromoDetailsByNameThrowsRuntime("Crash");

        // Act
        ResponseEntity<?> response = promoController.getPromoDetailsByName(TEST_PROMO_CODE);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }


    /**
     * Purpose: Verify controller handles NotFoundException from service.
     */
    @Test
    @DisplayName("getPromoDetailsByName - Controller handles NotFoundException")
    void getPromoDetailsByName_ControllerHandlesNotFound_Failure() {
        // Arrange
        stubServiceGetPromoDetailsByNameThrowsNotFound("Not Found");

        // Act
        ResponseEntity<?> response = promoController.getPromoDetailsByName("GHOST");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }




    /**
     * Purpose: Verify that the controller correctly delegates getPromoDetailsByName
     * calls to the service layer.
     */
    @Test
    @DisplayName("getPromoDetailsByName - Controller delegates to service")
    void getPromoDetailsByName_WithValidCode_DelegatesToService() {
        // Arrange
        PromoResponseModel mockResponse = new PromoResponseModel(testPromo);
        stubServiceGetPromoDetailsByNameReturns(mockResponse);

        // Act
        ResponseEntity<?> response = promoController.getPromoDetailsByName(TEST_PROMO_CODE);

        // Assert
        verify(promoService).getPromoDetailsByName(TEST_PROMO_CODE);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }


}
