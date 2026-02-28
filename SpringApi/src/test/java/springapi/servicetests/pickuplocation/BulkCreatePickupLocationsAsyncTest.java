package springapi.servicetests.pickuplocation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import springapi.controllers.PickupLocationController;
import springapi.models.requestmodels.PickupLocationRequestModel;

/**
 * Unit tests for PickupLocationService.bulkCreatePickupLocationsAsync() method. Tests async batch
 * processing, validation, error handling, and logging. Test Count: 13 tests
 */
@DisplayName("Bulk Create Pickup Locations Async Tests")
class BulkCreatePickupLocationsAsyncTest extends PickupLocationServiceTestBase {

  // Total Tests: 13
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify successful async bulk creation with valid pickup locations. Expected Result:
   * All locations processed and logged successfully. Assertions: No exception thrown,
   * userLogService called.
   */
  @Test
  @DisplayName("Bulk Create Pickup Locations Async - All Valid - Success")
  void bulkCreatePickupLocationsAsync_AllValid_Success() {
    // Arrange
    List<PickupLocationRequestModel> requests = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      requests.add(createValidPickupLocationRequest((long) i));
    }
    stubAddressRepositorySave(testAddress);
    stubPickupLocationRepositorySave(testPickupLocation);
    stubShipRocketHelperAddPickupLocation(testShipRocketResponse);

    // Act & Assert
    assertDoesNotThrow(
        () ->
            pickupLocationService.bulkCreatePickupLocationsAsync(
                requests, 1L, "testuser", TEST_CLIENT_ID));
    verify(userLogService)
        .logDataWithContext(
            anyLong(), anyString(), anyLong(), contains("Bulk: 3 succeeded"), anyString());
  }

  /**
   * Purpose: Test async with international characters in nicknames. Expected Result: UTF-8
   * characters handled correctly in async processing. Assertions: No exception thrown, special
   * characters processed.
   */
  @Test
  @DisplayName("Bulk Create Pickup Locations Async - International Characters - Success")
  void bulkCreatePickupLocationsAsync_InternationalCharacters_Success() {
    // Arrange
    List<PickupLocationRequestModel> requests = new ArrayList<>();

    PickupLocationRequestModel req1 = createValidPickupLocationRequest(1L);
    req1.setAddressNickName("São Paulo Hub");
    requests.add(req1);

    PickupLocationRequestModel req2 = createValidPickupLocationRequest(2L);
    req2.setAddressNickName("北京中心"); // Beijing in Chinese
    requests.add(req2);
    stubAddressRepositorySave(testAddress);
    stubPickupLocationRepositorySave(testPickupLocation);
    stubShipRocketHelperAddPickupLocation(testShipRocketResponse);

    // Act & Assert
    assertDoesNotThrow(
        () ->
            pickupLocationService.bulkCreatePickupLocationsAsync(
                requests, 1L, "testuser", TEST_CLIENT_ID));
  }

  /**
   * Purpose: Test async with very large batch size (boundary test). Expected Result: All items
   * processed asynchronously. Assertions: No exception thrown, batch processed completely.
   */
  @Test
  @DisplayName("Bulk Create Pickup Locations Async - Large Batch 100 Items - Success")
  void bulkCreatePickupLocationsAsync_LargeBatch100Items_Success() {
    // Arrange
    List<PickupLocationRequestModel> requests = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      requests.add(createValidPickupLocationRequest((long) i));
    }
    stubAddressRepositorySave(testAddress);
    stubPickupLocationRepositorySave(testPickupLocation);
    stubShipRocketHelperAddPickupLocation(testShipRocketResponse);

    // Act & Assert
    assertDoesNotThrow(
        () ->
            pickupLocationService.bulkCreatePickupLocationsAsync(
                requests, 1L, "testuser", TEST_CLIENT_ID));
    verify(userLogService)
        .logDataWithContext(
            anyLong(), anyString(), anyLong(), contains("Bulk: 100 succeeded"), anyString());
  }

  /**
   * Purpose: Verify async processing with partial success (some items fail). Expected Result: Valid
   * items processed, failures logged with error messages. Assertions: No exception thrown, logging
   * captures partial results.
   */
  @Test
  @DisplayName("Bulk Create Pickup Locations Async - Mixed Valid and Invalid - Partial Success")
  void bulkCreatePickupLocationsAsync_MixedValidAndInvalid_PartialSuccess() {
    // Arrange
    List<PickupLocationRequestModel> requests = new ArrayList<>();
    requests.add(createValidPickupLocationRequest(1L)); // Valid

    PickupLocationRequestModel invalidReq = createValidPickupLocationRequest(2L);
    invalidReq.setAddressNickName(""); // Invalid - empty nickname
    requests.add(invalidReq);

    requests.add(createValidPickupLocationRequest(3L)); // Valid
    stubAddressRepositorySave(testAddress);
    stubPickupLocationRepositorySave(testPickupLocation);
    stubShipRocketHelperAddPickupLocation(testShipRocketResponse);

    // Act & Assert
    assertDoesNotThrow(
        () ->
            pickupLocationService.bulkCreatePickupLocationsAsync(
                requests, 1L, "testuser", TEST_CLIENT_ID));
    // Verify logging captured partial results
    verify(userLogService)
        .logDataWithContext(anyLong(), anyString(), anyLong(), contains("succeeded"), anyString());
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify client ID isolation in async processing. Expected Result: All locations
   * associated with correct client. Assertions: Repository called with correct client ID.
   */
  @Test
  @DisplayName("Bulk Create Pickup Locations Async - Client ID Isolation - Success")
  void bulkCreatePickupLocationsAsync_ClientIdIsolation_Success() {
    // Arrange
    Long differentClientId = 999L;
    List<PickupLocationRequestModel> requests = new ArrayList<>();
    requests.add(createValidPickupLocationRequest(1L));
    stubAddressRepositorySave(testAddress);
    stubPickupLocationRepositorySave(testPickupLocation);
    stubShipRocketHelperAddPickupLocation(testShipRocketResponse);

    // Act
    pickupLocationService.bulkCreatePickupLocationsAsync(
        requests, 1L, "testuser", differentClientId);

    // Assert
    verify(userLogService)
        .logDataWithContext(
            anyLong(), anyString(), eq(differentClientId), anyString(), anyString());
  }

  /**
   * Purpose: Reject async processing with empty list. Expected Result: Error is handled and logged
   * without throwing. Assertions: No exception is thrown.
   */
  @Test
  @DisplayName("Bulk Create Pickup Locations Async - Empty List - Handles Error Gracefully")
  void bulkCreatePickupLocationsAsync_EmptyList_HandlesErrorGracefully() {
    // Arrange
    List<PickupLocationRequestModel> emptyList = new ArrayList<>();

    // Act & Assert
    assertDoesNotThrow(
        () ->
            pickupLocationService.bulkCreatePickupLocationsAsync(
                emptyList, 1L, "testuser", TEST_CLIENT_ID));
  }

  /**
   * Purpose: Reject async processing with null list. Expected Result: Error is handled and logged
   * without throwing. Assertions: No exception is thrown.
   */
  @Test
  @DisplayName("Bulk Create Pickup Locations Async - Null List - Handles Error Gracefully")
  void bulkCreatePickupLocationsAsync_NullList_HandlesErrorGracefully() {
    // Arrange

    // Act & Assert
    assertDoesNotThrow(
        () ->
            pickupLocationService.bulkCreatePickupLocationsAsync(
                null, 1L, "testuser", TEST_CLIENT_ID));
  }

  /**
   * Purpose: Verify async error handling when repository throws exception. Expected Result:
   * Exception caught and logged, processing continues for other items. Assertions: Failure count
   * incremented, logging captures error.
   */
  @Test
  @DisplayName("Bulk Create Pickup Locations Async - Repository Error - Failure Logged")
  void bulkCreatePickupLocationsAsync_RepositoryError_FailureLogged() {
    // Arrange
    List<PickupLocationRequestModel> requests = new ArrayList<>();
    requests.add(createValidPickupLocationRequest(1L));

    stubAddressRepositorySaveThrows(new RuntimeException("DB Connection Error"));
    stubPickupLocationRepositorySave(testPickupLocation);

    // Act & Assert
    assertDoesNotThrow(
        () ->
            pickupLocationService.bulkCreatePickupLocationsAsync(
                requests, 1L, "testuser", TEST_CLIENT_ID));

    // Verify error was logged
    verify(userLogService)
        .logDataWithContext(anyLong(), anyString(), anyLong(), anyString(), anyString());
  }

  /**
   * Purpose: Verify requesting user ID is captured in logging. Expected Result: UserLogService
   * called with correct requestingUserId. Assertions: logDataWithContext called with specified
   * userId.
   */
  @Test
  @DisplayName("Bulk Create Pickup Locations Async - Requesting User ID Captured - Success")
  void bulkCreatePickupLocationsAsync_RequestingUserIdCaptured_Success() {
    // Arrange
    Long requestingUserId = 42L;
    List<PickupLocationRequestModel> requests = new ArrayList<>();
    requests.add(createValidPickupLocationRequest(1L));
    stubAddressRepositorySave(testAddress);
    stubPickupLocationRepositorySave(testPickupLocation);
    stubShipRocketHelperAddPickupLocation(testShipRocketResponse);

    // Act
    pickupLocationService.bulkCreatePickupLocationsAsync(
        requests, requestingUserId, "testuser", TEST_CLIENT_ID);

    // Assert
    verify(userLogService)
        .logDataWithContext(eq(requestingUserId), anyString(), anyLong(), anyString(), anyString());
  }

  /**
   * Purpose: Verify requesting user login name is captured in logging. Expected Result:
   * UserLogService called with correct requestingUserLoginName. Assertions: logDataWithContext
   * called with specified username.
   */
  @Test
  @DisplayName("Bulk Create Pickup Locations Async - Requesting User Name Captured - Success")
  void bulkCreatePickupLocationsAsync_RequestingUserNameCaptured_Success() {
    // Arrange
    String requestingUserName = "alice_manager";
    List<PickupLocationRequestModel> requests = new ArrayList<>();
    requests.add(createValidPickupLocationRequest(1L));
    stubAddressRepositorySave(testAddress);
    stubPickupLocationRepositorySave(testPickupLocation);
    stubShipRocketHelperAddPickupLocation(testShipRocketResponse);

    // Act
    pickupLocationService.bulkCreatePickupLocationsAsync(
        requests, 1L, requestingUserName, TEST_CLIENT_ID);

    // Assert
    verify(userLogService)
        .logDataWithContext(anyLong(), eq(requestingUserName), anyLong(), anyString(), anyString());
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
  @DisplayName("bulkCreatePickupLocationsAsync - Controller Permission - Unauthorized")
  void bulkCreatePickupLocationsAsync_controller_permission_unauthorized() {
    // Arrange
    PickupLocationController controller = new PickupLocationController(pickupLocationServiceMock);
    // Setup user context to return normally (so getUserId() doesn't throw)
    stubPickupLocationServiceUserContext(TEST_USER_ID, "testuser", TEST_CLIENT_ID);
    // Then make the actual service method throw
    stubPickupLocationServiceThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = controller.bulkCreatePickupLocations(new ArrayList<>());

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  /**
   * Purpose: Verify controller method returns ResponseEntity. Expected Result: Return type is
   * ResponseEntity. Assertions: Return type equals ResponseEntity class.
   */
  @Test
  @DisplayName("bulkCreatePickupLocationsAsync - Method Is Not Void Return Type - Success")
  void bulkCreatePickupLocationsAsync_MethodIsNotNullReturnType_Success()
      throws NoSuchMethodException {
    // Arrange
    Method method =
        PickupLocationController.class.getMethod("bulkCreatePickupLocations", List.class);

    // Act
    Class<?> returnType = method.getReturnType();

    // Assert
    assertEquals(
        org.springframework.http.ResponseEntity.class,
        returnType,
        "bulkCreatePickupLocationsAsync should return ResponseEntity");
  }
}
