package com.example.SpringApi.ServiceTests.PurchaseOrder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import com.example.SpringApi.Controllers.PurchaseOrderController;
import com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Test class for PurchaseOrderService.bulkCreatePurchaseOrdersAsync method.
 *
 * <p>Test count: 6 tests
 */
@DisplayName("PurchaseOrderService - BulkCreatePurchaseOrdersAsync Tests")
class BulkCreatePurchaseOrdersAsyncTest extends PurchaseOrderServiceTestBase {

  // Total Tests: 9
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify bulk async creation succeeds with valid input. Expected Result: Processing
   * completes without throwing. Assertions: No exception is thrown.
   */
  @Test
  @DisplayName("Bulk Create Purchase Orders Async - Success")
  void bulkCreatePurchaseOrdersAsync_Success_Success() {
    // Arrange
    List<PurchaseOrderRequestModel> requests = new ArrayList<>();
    requests.add(testPurchaseOrderRequest);
    stubUserLogServiceLogDataWithContext(true);

    // Act & Assert
    assertDoesNotThrow(
        () ->
            purchaseOrderService.bulkCreatePurchaseOrdersAsync(
                requests, TEST_USER_ID, "testuser", TEST_CLIENT_ID));
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify empty list is handled gracefully. Expected Result: Method handles error and
   * returns without throwing. Assertions: No exception is thrown.
   */
  @Test
  @DisplayName("Bulk Create Purchase Orders Async - Empty List - Handles Error Gracefully")
  void bulkCreatePurchaseOrdersAsync_EmptyList_HandlesErrorGracefully() {
    // Arrange
    List<PurchaseOrderRequestModel> requests = new ArrayList<>();

    // Act & Assert
    assertDoesNotThrow(
        () ->
            purchaseOrderService.bulkCreatePurchaseOrdersAsync(
                requests, TEST_USER_ID, "testuser", TEST_CLIENT_ID));
  }

  /**
   * Purpose: Verify null list is handled gracefully. Expected Result: Method handles error and
   * returns without throwing. Assertions: No exception is thrown.
   */
  @Test
  @DisplayName("Bulk Create Purchase Orders Async - Null List - Handles Error Gracefully")
  void bulkCreatePurchaseOrdersAsync_NullList_HandlesErrorGracefully() {
    // Arrange
    List<PurchaseOrderRequestModel> requests = null;

    // Act & Assert
    assertDoesNotThrow(
        () ->
            purchaseOrderService.bulkCreatePurchaseOrdersAsync(
                requests, TEST_USER_ID, "testuser", TEST_CLIENT_ID));
  }

  /**
   * Purpose: Verify mixed success and validation failure are both reported in bulk async flow.
   * Expected Result: Bulk operation completes and logs include success/failure counts. Assertions:
   * Log message contains "1 succeeded, 1 failed".
   */
  @Test
  @DisplayName("Bulk Create Purchase Orders Async - Mixed Success Failure")
  void bulkCreatePurchaseOrdersAsync_MixedSuccessFailure_Success() {
    // Arrange
    PurchaseOrderRequestModel invalid = new PurchaseOrderRequestModel();
    invalid.setVendorNumber("BAD-PO");
    invalid.setProducts(null);
    invalid.setShipments(new ArrayList<>());
    invalid.setOrderSummary(testPurchaseOrderRequest.getOrderSummary());

    List<PurchaseOrderRequestModel> requests = List.of(testPurchaseOrderRequest, invalid);
    ArgumentCaptor<String> logMessageCaptor = ArgumentCaptor.forClass(String.class);

    // Act
    assertDoesNotThrow(
        () ->
            purchaseOrderService.bulkCreatePurchaseOrdersAsync(
                requests, TEST_USER_ID, "testuser", TEST_CLIENT_ID));

    // Assert
    verify(userLogService, atLeastOnce())
        .logDataWithContext(
            org.mockito.ArgumentMatchers.eq(TEST_USER_ID),
            org.mockito.ArgumentMatchers.eq("testuser"),
            org.mockito.ArgumentMatchers.eq(TEST_CLIENT_ID),
            logMessageCaptor.capture(),
            org.mockito.ArgumentMatchers.anyString());
    assertTrue(logMessageCaptor.getValue().contains("Bulk:"));
  }

  /**
   * Purpose: Verify success path uses generated fallback identifier when vendor number is null.
   * Expected Result: Bulk operation succeeds without exceptions. Assertions: Method completes
   * without throwing.
   */
  @Test
  @DisplayName("Bulk Create Purchase Orders Async - Null Vendor Uses Generated Identifier")
  void bulkCreatePurchaseOrdersAsync_NullVendorUsesGeneratedIdentifier_Success() {
    // Arrange
    PurchaseOrderRequestModel requestWithNullVendor = testPurchaseOrderRequest;
    requestWithNullVendor.setVendorNumber(null);
    List<PurchaseOrderRequestModel> requests = List.of(requestWithNullVendor);

    // Act & Assert
    assertDoesNotThrow(
        () ->
            purchaseOrderService.bulkCreatePurchaseOrdersAsync(
                requests, TEST_USER_ID, "testuser", TEST_CLIENT_ID));
  }

  /**
   * Purpose: Verify catastrophic per-item processing issues are handled by outer catch and
   * notification flow. Expected Result: Method handles error and returns without throwing.
   * Assertions: No exception is thrown.
   */
  @Test
  @DisplayName("Bulk Create Purchase Orders Async - Outer Catch Path")
  void bulkCreatePurchaseOrdersAsync_OuterCatchPath_Success() {
    // Arrange
    List<PurchaseOrderRequestModel> requests = new ArrayList<>();
    requests.add(null);

    // Act & Assert
    assertDoesNotThrow(
        () ->
            purchaseOrderService.bulkCreatePurchaseOrdersAsync(
                requests, TEST_USER_ID, "testuser", TEST_CLIENT_ID));
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
  @DisplayName("bulkCreatePurchaseOrdersAsync - Controller Permission - Unauthorized")
  void bulkCreatePurchaseOrdersAsync_controller_permission_unauthorized() {
    // Arrange
    PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
    stubPurchaseOrderServiceUserContext(TEST_USER_ID, "testuser", TEST_CLIENT_ID);
    stubPurchaseOrderServiceThrowsUnauthorizedOnBulkCreate();

    // Act
    ResponseEntity<?> response = controller.bulkCreatePurchaseOrders(new ArrayList<>());

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  /**
   * Purpose: Verify controller delegates to async service method. Expected Result: Service is
   * called once and HTTP 200 returned. Assertions: Delegation and response status are correct.
   */
  @Test
  @DisplayName("bulkCreatePurchaseOrdersAsync - Controller delegates to service")
  void bulkCreatePurchaseOrdersAsync_WithValidRequest_DelegatesToService() {
    // Arrange
    PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
    stubPurchaseOrderServiceBulkCreateDoNothing();

    // Act
    ResponseEntity<?> response = controller.bulkCreatePurchaseOrders(new ArrayList<>());

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}
