package com.example.SpringApi.ServiceTests.PickupLocation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.SpringApi.Controllers.PickupLocationController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.ResponseModels.PickupLocationResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for PickupLocationService.getPickupLocationById() method. Tests successful retrieval,
 * not found scenarios, and authorization. Test Count: 12 tests
 */
@DisplayName("Get Pickup Location By ID Tests")
class GetPickupLocationByIdTest extends PickupLocationServiceTestBase {

  // Total Tests: 12
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify successful retrieval of pickup location by ID. Expected Result:
   * PickupLocationResponseModel is returned with correct data. Assertions: Result is not null, ID
   * matches expected value.
   */
  @Test
  @DisplayName("Get Pickup Location By ID - Success")
  void getPickupLocationById_Success_Success() {
    // Arrange
    stubPickupLocationRepositoryFindByIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, testPickupLocation);

    // Act
    PickupLocationResponseModel result =
        pickupLocationService.getPickupLocationById(TEST_PICKUP_LOCATION_ID);

    // Assert
    assertNotNull(result);
    assertEquals(TEST_PICKUP_LOCATION_ID, result.getPickupLocationId());
    assertEquals(TEST_ADDRESS_NICKNAME, result.getAddressNickName());
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify successful retrieval with deleted location marked as active. Expected Result:
   * Active location is returned successfully. Assertions: Result is not null, isDeleted is false.
   */
  @Test
  @DisplayName("Get Pickup Location By ID - Active Location - Success")
  void getPickupLocationById_ActiveLocation_Success() {
    // Arrange
    testPickupLocation.setIsDeleted(false);
    stubPickupLocationRepositoryFindByIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, testPickupLocation);

    // Act
    PickupLocationResponseModel result =
        pickupLocationService.getPickupLocationById(TEST_PICKUP_LOCATION_ID);

    // Assert
    assertNotNull(result);
    assertFalse(testPickupLocation.getIsDeleted());
  }

  /**
   * Purpose: Reject retrieval attempts using the maximum possible long ID if not found. Expected
   * Result: NotFoundException is thrown. Assertions: Error message indicates not found.
   */
  @Test
  @DisplayName("Get Pickup Location By ID - Max Long ID - Throws NotFoundException")
  void getPickupLocationById_MaxLongId_ThrowsNotFoundException() {
    // Arrange
    stubPickupLocationRepositoryFindByIdAndClientIdNotFound(Long.MAX_VALUE, TEST_CLIENT_ID);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> pickupLocationService.getPickupLocationById(Long.MAX_VALUE));
    assertEquals(
        String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, Long.MAX_VALUE),
        ex.getMessage());
  }

  /**
   * Purpose: Reject retrieval attempts using the minimum possible long ID if not found. Expected
   * Result: NotFoundException is thrown. Assertions: Error message indicates not found.
   */
  @Test
  @DisplayName("Get Pickup Location By ID - Min Long ID - Throws NotFoundException")
  void getPickupLocationById_MinLongId_ThrowsNotFoundException() {
    // Arrange
    stubPickupLocationRepositoryFindByIdAndClientIdNotFound(Long.MIN_VALUE, TEST_CLIENT_ID);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> pickupLocationService.getPickupLocationById(Long.MIN_VALUE));
    assertEquals(
        String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, Long.MIN_VALUE),
        ex.getMessage());
  }

  /**
   * Purpose: Reject retrieval attempts for negative IDs. Expected Result: NotFoundException is
   * thrown. Assertions: Error message indicates not found.
   */
  @Test
  @DisplayName("Get Pickup Location By ID - Negative ID - Throws NotFoundException")
  void getPickupLocationById_NegativeId_ThrowsNotFoundException() {
    // Arrange
    stubPickupLocationRepositoryFindByIdAndClientIdNotFound(-1L, TEST_CLIENT_ID);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> pickupLocationService.getPickupLocationById(-1L));
    assertEquals(
        String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, -1L), ex.getMessage());
  }

  /**
   * Purpose: Verify NotFoundException is thrown when a location is not found for the client.
   * Expected Result: NotFoundException is thrown. Assertions: Exception is thrown.
   */
  @Test
  @DisplayName("Get Pickup Location By ID - Not Found - Throws NotFoundException")
  void getPickupLocationById_NotFound_ThrowsNotFoundException() {
    // Arrange
    stubPickupLocationRepositoryFindByIdAndClientIdNotFound(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> pickupLocationService.getPickupLocationById(TEST_PICKUP_LOCATION_ID));
    assertEquals(
        String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, TEST_PICKUP_LOCATION_ID),
        ex.getMessage());
  }

  /**
   * Purpose: Verify repository is called with correct client ID. Expected Result: Repository method
   * called with correct parameters. Assertions: Verify is called with expected ID and client ID.
   */
  @Test
  @DisplayName("Get Pickup Location By ID - Verify Repository Call - Success")
  void getPickupLocationById_VerifyRepositoryCall_Success() {
    // Arrange
    stubPickupLocationRepositoryFindByIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, testPickupLocation);

    // Act
    pickupLocationService.getPickupLocationById(TEST_PICKUP_LOCATION_ID);

    // Assert
    verify(pickupLocationRepository)
        .findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
  }

  /**
   * Purpose: Verify retrieval with large ID value. Expected Result: NotFoundException is thrown for
   * non-existent large ID. Assertions: Error indicates not found.
   */
  @Test
  @DisplayName("Get Pickup Location By ID - Very Large ID - Throws NotFoundException")
  void getPickupLocationById_VeryLargeId_ThrowsNotFoundException() {
    // Arrange
    Long largeId = 999999999999L;
    stubPickupLocationRepositoryFindByIdAndClientIdNotFound(largeId, TEST_CLIENT_ID);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> pickupLocationService.getPickupLocationById(largeId));
    assertEquals(
        String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, largeId),
        ex.getMessage());
  }

  /**
   * Purpose: Reject retrieval attempts for a zero ID. Expected Result: NotFoundException is thrown.
   * Assertions: Error message indicates not found.
   */
  @Test
  @DisplayName("Get Pickup Location By ID - Zero ID - Throws NotFoundException")
  void getPickupLocationById_ZeroId_ThrowsNotFoundException() {
    // Arrange
    stubPickupLocationRepositoryFindByIdAndClientIdNotFound(0L, TEST_CLIENT_ID);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> pickupLocationService.getPickupLocationById(0L));
    assertEquals(
        String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, 0L), ex.getMessage());
  }

  /*
   **********************************************************************************************
   * CONTROLLER AUTHORIZATION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify unauthorized access is blocked at the controller level. Expected Result:
   * Unauthorized status is returned. Assertions: Response status is 401 UNAUTHORIZED.
   */
  @Test
  @DisplayName("getPickupLocationById - Controller Permission - Unauthorized")
  void getPickupLocationById_controller_permission_unauthorized() {
    // Arrange
    PickupLocationController controller = new PickupLocationController(pickupLocationServiceMock);
    stubPickupLocationServiceThrowsUnauthorizedOnGetById();

    // Act
    ResponseEntity<?> response = controller.getPickupLocationById(TEST_PICKUP_LOCATION_ID);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  /**
   * Purpose: Verify controller delegates getPickupLocationById to service. Expected Result: Service
   * method called and HTTP 200 returned. Assertions: Delegation occurs and response is OK.
   */
  @Test
  @DisplayName("getPickupLocationById - Controller delegates to service")
  void getPickupLocationById_WithValidId_DelegatesToService() {
    // Arrange
    PickupLocationController controller = new PickupLocationController(pickupLocationServiceMock);
    PickupLocationResponseModel mockResponse = new PickupLocationResponseModel(testPickupLocation);
    stubPickupLocationServiceGetPickupLocationByIdReturns(mockResponse);

    // Act
    ResponseEntity<?> response = controller.getPickupLocationById(TEST_PICKUP_LOCATION_ID);

    // Assert
    verify(pickupLocationServiceMock).getPickupLocationById(TEST_PICKUP_LOCATION_ID);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}
