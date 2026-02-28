package springapi.ServiceTests.PickupLocation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import springapi.ErrorMessages;
import springapi.controllers.PickupLocationController;
import springapi.exceptions.NotFoundException;

/**
 * Unit tests for PickupLocationService.togglePickupLocation() method. Tests state transitions, edge
 * cases, and client isolation. Test Count: 15 tests
 */
@DisplayName("Toggle Pickup Location Tests")
class TogglePickupLocationTest extends PickupLocationServiceTestBase {

  // Total Tests: 15
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify multiple toggles correctly persist state alternating between true and false.
   * Expected Result: State transition: Active -> Deleted -> Active. Assertions: isDeleted matches
   * expected state after each toggle.
   */
  @Test
  @DisplayName("Toggle Pickup Location - Multiple Toggles - Success")
  void togglePickupLocation_MultipleToggles_Success() {
    // Arrange
    testPickupLocation.setIsDeleted(false);
    stubPickupLocationRepositoryFindByIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, testPickupLocation);
    stubPickupLocationRepositorySave(testPickupLocation);

    // Act
    pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);
    assertTrue(testPickupLocation.getIsDeleted());

    pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);
    assertFalse(testPickupLocation.getIsDeleted());

    // Assert
    verify(pickupLocationRepository, times(2)).save(testPickupLocation);
  }

  /**
   * Purpose: Verify toggle from deleted status to active status works correctly. Expected Result:
   * isDeleted changes from true to false. Assertions: result is active (false).
   */
  @Test
  @DisplayName("Toggle Pickup Location - Restore from Deleted - Success")
  void togglePickupLocation_RestoreFromDeleted_Success() {
    // Arrange
    testPickupLocation.setIsDeleted(true);
    stubPickupLocationRepositoryFindByIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, testPickupLocation);
    stubPickupLocationRepositorySave(testPickupLocation);

    // Act
    pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);

    // Assert
    assertFalse(testPickupLocation.getIsDeleted());
  }

  /**
   * Purpose: Verify successful toggle of pickup location deleted status. Expected Result: Pickup
   * location isDeleted flag is toggled, save is called. Assertions: isDeleted is true after toggle,
   * repository save is called.
   */
  @Test
  @DisplayName("Toggle Pickup Location - Success")
  void togglePickupLocation_Success_Success() {
    // Arrange
    stubPickupLocationRepositoryFindByIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, testPickupLocation);
    stubPickupLocationRepositorySave(testPickupLocation);

    // Act
    pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);

    // Assert
    assertTrue(testPickupLocation.getIsDeleted());
    verify(pickupLocationRepository, times(1)).save(testPickupLocation);
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Test toggle respects client isolation. Expected Result: Only searches for location in
   * client's data. Assertions: Repository called with correct client ID.
   */
  @Test
  @DisplayName("Toggle Pickup Location - Client Isolation - Success")
  void togglePickupLocation_ClientIsolation_Success() {
    // Arrange
    stubPickupLocationRepositoryFindByIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, testPickupLocation);
    stubPickupLocationRepositorySave(testPickupLocation);

    // Act
    pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);

    // Assert
    verify(pickupLocationRepository)
        .findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
  }

  /**
   * Purpose: Reject toggle attempts for the maximum possible long ID if not found. Expected Result:
   * NotFoundException is thrown. Assertions: Exception message is not null.
   */
  @Test
  @DisplayName("Toggle Pickup Location - Max Long ID - Throws NotFoundException")
  void togglePickupLocation_MaxLongId_ThrowsNotFoundException() {
    // Arrange
    stubPickupLocationRepositoryFindByIdAndClientIdNotFound(Long.MAX_VALUE, TEST_CLIENT_ID);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> pickupLocationService.togglePickupLocation(Long.MAX_VALUE));
    assertEquals(
        String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, Long.MAX_VALUE),
        ex.getMessage());
  }

  /**
   * Purpose: Reject toggle attempts for the minimum possible long ID if not found. Expected Result:
   * NotFoundException is thrown. Assertions: Exception message is not null.
   */
  @Test
  @DisplayName("Toggle Pickup Location - Min Long ID - Throws NotFoundException")
  void togglePickupLocation_MinLongId_ThrowsNotFoundException() {
    // Arrange
    stubPickupLocationRepositoryFindByIdAndClientIdNotFound(Long.MIN_VALUE, TEST_CLIENT_ID);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> pickupLocationService.togglePickupLocation(Long.MIN_VALUE));
    assertEquals(
        String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, Long.MIN_VALUE),
        ex.getMessage());
  }

  /**
   * Purpose: Reject toggle attempts for negative IDs. Expected Result: NotFoundException is thrown.
   * Assertions: Exception message is not null.
   */
  @Test
  @DisplayName("Toggle Pickup Location - Negative ID - Throws NotFoundException")
  void togglePickupLocation_NegativeId_ThrowsNotFoundException() {
    // Arrange
    stubPickupLocationRepositoryFindByIdAndClientIdNotFound(-100L, TEST_CLIENT_ID);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> pickupLocationService.togglePickupLocation(-100L));
    assertEquals(
        String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, -100L), ex.getMessage());
  }

  /**
   * Purpose: Reject toggle attempts when the pickup location is not found. Expected Result:
   * NotFoundException is thrown. Assertions: Exception is thrown.
   */
  @Test
  @DisplayName("Toggle Pickup Location - Not Found - Throws NotFoundException")
  void togglePickupLocation_NotFound_ThrowsNotFoundException() {
    // Arrange
    stubPickupLocationRepositoryFindByIdAndClientIdNotFound(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID));
    assertEquals(
        String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, TEST_PICKUP_LOCATION_ID),
        ex.getMessage());
  }

  /**
   * Purpose: Verify toggle preserves all other location properties except isDeleted. Expected
   * Result: Only isDeleted is toggled, other properties unchanged. Assertions: ShipRocket ID and
   * other properties remain the same.
   */
  @Test
  @DisplayName("Toggle Pickup Location - Preserves Other Properties - Success")
  void togglePickupLocation_PreservesOtherProperties_Success() {
    // Arrange
    testPickupLocation.setIsDeleted(false);
    stubPickupLocationRepositoryFindByIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, testPickupLocation);
    stubPickupLocationRepositorySave(testPickupLocation);

    // Act
    pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);

    // Assert
    verify(pickupLocationRepository)
        .save(argThat(saved -> saved.getShipRocketPickupLocationId().equals(TEST_SHIPROCKET_ID)));
  }

  /**
   * Purpose: Test consecutive toggles produce correct state sequence. Expected Result: Toggling 3
   * times results in active state. Assertions: State alternates correctly.
   */
  @Test
  @DisplayName("Toggle Pickup Location - Three Consecutive Toggles - Success")
  void togglePickupLocation_ThreeConsecutiveToggles_Success() {
    // Arrange
    testPickupLocation.setIsDeleted(false);
    stubPickupLocationRepositoryFindByIdAndClientId(
        TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID, testPickupLocation);
    stubPickupLocationRepositorySave(testPickupLocation);

    // Act - Toggle 3 times: false -> true -> false -> true
    pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);
    pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);
    pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);

    // Assert
    verify(pickupLocationRepository, times(3)).save(testPickupLocation);
    assertTrue(testPickupLocation.getIsDeleted());
  }

  /**
   * Purpose: Test toggle with very large ID. Expected Result: NotFoundException is thrown.
   * Assertions: Exception message is not null.
   */
  @Test
  @DisplayName("Toggle Pickup Location - Very Large ID - Throws NotFoundException")
  void togglePickupLocation_VeryLargeId_ThrowsNotFoundException() {
    // Arrange
    Long largeId = 999999999999L;
    stubPickupLocationRepositoryFindByIdAndClientIdNotFound(largeId, TEST_CLIENT_ID);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> pickupLocationService.togglePickupLocation(largeId));
    assertEquals(
        String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, largeId),
        ex.getMessage());
  }

  /**
   * Purpose: Reject toggle attempts for a zero ID. Expected Result: NotFoundException is thrown.
   * Assertions: Exception message is not null.
   */
  @Test
  @DisplayName("Toggle Pickup Location - Zero ID - Throws NotFoundException")
  void togglePickupLocation_ZeroId_ThrowsNotFoundException() {
    // Arrange
    stubPickupLocationRepositoryFindByIdAndClientIdNotFound(0L, TEST_CLIENT_ID);

    // Act & Assert
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> pickupLocationService.togglePickupLocation(0L));
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
  @DisplayName("togglePickupLocation - Controller Permission - Unauthorized")
  void togglePickupLocation_controller_permission_unauthorized() {
    // Arrange
    PickupLocationController controller = new PickupLocationController(pickupLocationServiceMock);
    stubPickupLocationServiceThrowsUnauthorizedOnToggle();

    // Act
    ResponseEntity<?> response = controller.togglePickupLocation(TEST_PICKUP_LOCATION_ID);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  /**
   * Purpose: Verify controller delegates togglePickupLocation to service. Expected Result: Service
   * method called and HTTP 200 returned. Assertions: Delegation occurs and response is OK.
   */
  @Test
  @DisplayName("togglePickupLocation - Controller delegates to service")
  void togglePickupLocation_WithValidId_DelegatesToService() {
    // Arrange
    PickupLocationController controller = new PickupLocationController(pickupLocationServiceMock);
    stubPickupLocationServiceTogglePickupLocationDoNothing();

    // Act
    ResponseEntity<?> response = controller.togglePickupLocation(TEST_PICKUP_LOCATION_ID);

    // Assert
    verify(pickupLocationServiceMock).togglePickupLocation(TEST_PICKUP_LOCATION_ID);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}
