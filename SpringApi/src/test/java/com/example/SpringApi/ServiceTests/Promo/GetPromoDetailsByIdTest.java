package com.example.SpringApi.ServiceTests.Promo;

import static org.junit.jupiter.api.Assertions.*;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.ResponseModels.PromoResponseModel;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Test class for PromoService.getPromoDetailsById method. */
@DisplayName("PromoService - GetPromoDetailsById Tests")
class GetPromoDetailsByIdTest extends PromoServiceTestBase {

  // Total Tests: 9
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Verify valid id returns promo.
   * Expected Result: PromoResponseModel returned.
   * Assertions: response not null.
   */
  @Test
  @DisplayName("getPromoDetailsById - Valid Id - Success")
  void getPromoDetailsById_ValidId_Success() {
    // Arrange
    stubPromoRepositoryFindByPromoIdAndClientId(
        TEST_PROMO_ID, TEST_CLIENT_ID, Optional.of(testPromo));

    // Act
    PromoResponseModel result = promoService.getPromoDetailsById(TEST_PROMO_ID);

    // Assert
    assertNotNull(result);
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Verify invalid id negative throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: exception thrown.
   */
  @Test
  @DisplayName("getPromoDetailsById - Invalid Id Negative - NotFound")
  void getPromoDetailsById_InvalidId_Negative() {
    // Arrange
    stubPromoRepositoryFindByPromoIdAndClientId(-1L, TEST_CLIENT_ID, Optional.empty());

    // Act & Assert
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> promoService.getPromoDetailsById(-1L));
    assertEquals(ErrorMessages.PromoErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   * Purpose: Verify invalid id 0 throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: exception thrown.
   */
  @Test
  @DisplayName("getPromoDetailsById - Invalid Id Zero - NotFound")
  void getPromoDetailsById_InvalidId_Zero() {
    // Arrange
    stubPromoRepositoryFindByPromoIdAndClientId(0L, TEST_CLIENT_ID, Optional.empty());

    // Act & Assert
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> promoService.getPromoDetailsById(0L));
    assertEquals(ErrorMessages.PromoErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   * Purpose: Verify not found throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: error message matches.
   */
  @Test
  @DisplayName("getPromoDetailsById - Not Found - Throws NotFoundException")
  void getPromoDetailsById_NotFound_ThrowsNotFoundException() {
    // Arrange
    stubPromoRepositoryFindByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> promoService.getPromoDetailsById(TEST_PROMO_ID));

    // Assert
    assertEquals(ErrorMessages.PromoErrorMessages.INVALID_ID, ex.getMessage());
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
  @DisplayName("getPromoDetailsById - Controller Permission - Unauthorized")
  void getPromoDetailsById_controller_permission_unauthorized() {
    // Arrange
    stubServiceThrowsUnauthorizedException();

    // Act
    ResponseEntity<?> response = promoController.getPromoDetailsById(TEST_PROMO_ID);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  /*
   * Purpose: Verify controller handles bad request.
   * Expected Result: BAD_REQUEST status.
   * Assertions: status is 400.
   */
  @Test
  @DisplayName("getPromoDetailsById - Controller Handles BadRequest - Success")
  void getPromoDetailsById_ControllerHandlesBadRequest_Success() {
    // Arrange
    stubServiceGetPromoDetailsByIdThrowsBadRequest("Bad ID");

    // Act
    ResponseEntity<?> response = promoController.getPromoDetailsById(TEST_PROMO_ID);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  /*
   * Purpose: Verify controller handles exception.
   * Expected Result: INTERNAL_SERVER_ERROR status.
   * Assertions: status is 500.
   */
  @Test
  @DisplayName("getPromoDetailsById - Controller Handles Exception - Failure")
  void getPromoDetailsById_ControllerHandlesException_Failure() {
    // Arrange
    stubServiceGetPromoDetailsByIdThrowsRuntime("Crash");

    // Act
    ResponseEntity<?> response = promoController.getPromoDetailsById(TEST_PROMO_ID);

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  /*
   * Purpose: Verify controller handles not found.
   * Expected Result: NOT_FOUND status.
   * Assertions: status is NOT_FOUND.
   */
  @Test
  @DisplayName("getPromoDetailsById - Controller Handles NotFound - Failure")
  void getPromoDetailsById_ControllerHandlesNotFound_Failure() {
    // Arrange
    stubServiceGetPromoDetailsByIdThrowsNotFound("Not Found");

    // Act
    ResponseEntity<?> response = promoController.getPromoDetailsById(TEST_PROMO_ID);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  /*
   * Purpose: Verify controller delegation.
   * Expected Result: OK status.
   * Assertions: status is OK.
   */
  @Test
  @DisplayName("getPromoDetailsById - Controller Delegation - Success")
  void getPromoDetailsById_WithValidId_DelegatesToService() {
    // Arrange
    stubServiceGetPromoDetailsByIdReturns(new PromoResponseModel(testPromo));

    // Act
    ResponseEntity<?> response = promoController.getPromoDetailsById(TEST_PROMO_ID);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}

