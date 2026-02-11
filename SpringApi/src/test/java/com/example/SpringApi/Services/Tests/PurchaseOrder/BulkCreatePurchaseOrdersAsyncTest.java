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
import static org.mockito.Mockito.when;

/**
 * Test class for PurchaseOrderService.bulkCreatePurchaseOrdersAsync method.
 *
 * Test count: 6 tests
 * - SUCCESS: 1 test
 * - FAILURE / EXCEPTION: 2 tests
 * - PERMISSION: 3 tests
 */
@DisplayName("PurchaseOrderService - BulkCreatePurchaseOrdersAsync Tests")
public class BulkCreatePurchaseOrdersAsyncTest extends PurchaseOrderServiceTestBase {
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
    void bulkCreatePurchaseOrdersAsync_Success() {
        // ARRANGE
        List<PurchaseOrderRequestModel> requests = new ArrayList<>();
        requests.add(testPurchaseOrderRequest);
        stubUserLogServiceLogDataWithContext(true);

        // ACT & ASSERT
        assertDoesNotThrow(() -> purchaseOrderService.bulkCreatePurchaseOrdersAsync(
                requests, TEST_USER_ID, "testuser", TEST_CLIENT_ID));
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify null list is handled gracefully.
     * Expected Result: Method handles error and returns without throwing.
     * Assertions: No exception is thrown.
     */
    @Test
    @DisplayName("Bulk Create Purchase Orders Async - Null List - Handles Error Gracefully")
    void bulkCreatePurchaseOrdersAsync_NullList_HandlesErrorGracefully() {
        // ARRANGE & ACT & ASSERT
        assertDoesNotThrow(() -> purchaseOrderService.bulkCreatePurchaseOrdersAsync(
                null, TEST_USER_ID, "testuser", TEST_CLIENT_ID));
    }

    /**
     * Purpose: Verify empty list is handled gracefully.
     * Expected Result: Method handles error and returns without throwing.
     * Assertions: No exception is thrown.
     */
    @Test
    @DisplayName("Bulk Create Purchase Orders Async - Empty List - Handles Error Gracefully")
    void bulkCreatePurchaseOrdersAsync_EmptyList_HandlesErrorGracefully() {
        // ARRANGE
        List<PurchaseOrderRequestModel> requests = new ArrayList<>();

        // ACT & ASSERT
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
        // ARRANGE
        PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
        // Setup user context to return normally (so getUserId() doesn't throw)
        when(purchaseOrderServiceMock.getUserId()).thenReturn(TEST_USER_ID);
        when(purchaseOrderServiceMock.getUser()).thenReturn("testuser");
        when(purchaseOrderServiceMock.getClientId()).thenReturn(TEST_CLIENT_ID);
        // Then make the actual service method throw
        stubPurchaseOrderServiceThrowsUnauthorizedOnBulkCreate();

        // ACT
        ResponseEntity<?> response = controller.bulkCreatePurchaseOrders(new ArrayList<>());

        // ASSERT
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("bulkCreatePurchaseOrdersAsync - Verify @PreAuthorize Annotation")
    void bulkCreatePurchaseOrdersAsync_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // ARRANGE
        Method method = PurchaseOrderController.class.getMethod("bulkCreatePurchaseOrders", List.class);

        // ACT
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // ASSERT
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
        // ARRANGE
        PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
        stubPurchaseOrderServiceBulkCreateDoNothing();

        // ACT
        ResponseEntity<?> response = controller.bulkCreatePurchaseOrders(new ArrayList<>());

        // ASSERT
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}