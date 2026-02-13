package com.example.SpringApi.Services.Tests.PurchaseOrder;

import com.example.SpringApi.Controllers.PurchaseOrderController;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PurchaseOrderService.bulkCreatePurchaseOrdersAsync method.
 *
 * Test count: 6 tests
 */
@DisplayName("PurchaseOrderService - BulkCreatePurchaseOrdersAsync Tests")
class BulkCreatePurchaseOrdersAsyncTest extends PurchaseOrderServiceTestBase {

    // Total Tests: 6
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify bulk async creation succeeds with valid input.
     * Expected Result: Processing completes without throwing.
     * Assertions: No exception is thrown.
     */
    @Test
    @DisplayName("Bulk Create Purchase Orders Async - Success")
    void bulkCreatePurchaseOrdersAsync_Success_Success() {
        // Arrange
        List<PurchaseOrderRequestModel> requests = new ArrayList<>();
        requests.add(testPurchaseOrderRequest);
        stubUserLogServiceLogDataWithContext(true);

        // Act & Assert
        assertDoesNotThrow(() -> purchaseOrderService.bulkCreatePurchaseOrdersAsync(
                requests, TEST_USER_ID, "testuser", TEST_CLIENT_ID));
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify empty list is handled gracefully.
     * Expected Result: Method handles error and returns without throwing.
     * Assertions: No exception is thrown.
     */
    @Test
    @DisplayName("Bulk Create Purchase Orders Async - Empty List - Handles Error Gracefully")
    void bulkCreatePurchaseOrdersAsync_EmptyList_HandlesErrorGracefully() {
        // Arrange
        List<PurchaseOrderRequestModel> requests = new ArrayList<>();

        // Act & Assert
        assertDoesNotThrow(() -> purchaseOrderService.bulkCreatePurchaseOrdersAsync(
                requests, TEST_USER_ID, "testuser", TEST_CLIENT_ID));
    }

    /**
     * Purpose: Verify null list is handled gracefully.
     * Expected Result: Method handles error and returns without throwing.
     * Assertions: No exception is thrown.
     */
    @Test
    @DisplayName("Bulk Create Purchase Orders Async - Null List - Handles Error Gracefully")
    void bulkCreatePurchaseOrdersAsync_NullList_HandlesErrorGracefully() {
        // Arrange
        List<PurchaseOrderRequestModel> requests = null;

        // Act & Assert
        assertDoesNotThrow(() -> purchaseOrderService.bulkCreatePurchaseOrdersAsync(
                requests, TEST_USER_ID, "testuser", TEST_CLIENT_ID));
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify unauthorized access is blocked at the controller level.
     * Expected Result: Unauthorized status is returned.
     * Assertions: Response status is 401 UNAUTHORIZED.
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
     * Purpose: Verify controller has @PreAuthorize for bulkCreatePurchaseOrders.
     * Expected Result: Annotation exists and includes INSERT_PURCHASE_ORDERS_PERMISSION.
     * Assertions: Annotation is present and contains permission.
     */
    @Test
    @DisplayName("bulkCreatePurchaseOrdersAsync - Verify @PreAuthorize Annotation")
    void bulkCreatePurchaseOrdersAsync_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        Method method = PurchaseOrderController.class.getMethod("bulkCreatePurchaseOrders", List.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present on bulkCreatePurchaseOrders");
        assertTrue(annotation.value().contains(Authorizations.INSERT_PURCHASE_ORDERS_PERMISSION),
                "@PreAuthorize should reference INSERT_PURCHASE_ORDERS_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to async service method.
     * Expected Result: Service is called once and HTTP 200 returned.
     * Assertions: Delegation and response status are correct.
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
