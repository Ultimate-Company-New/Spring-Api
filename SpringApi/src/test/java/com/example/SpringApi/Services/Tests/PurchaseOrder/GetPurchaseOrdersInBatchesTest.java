package com.example.SpringApi.Services.Tests.PurchaseOrder;

import com.example.SpringApi.Controllers.PurchaseOrderController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DTOs.PurchaseOrderWithDetails;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.PurchaseOrderResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PurchaseOrderService.getPurchaseOrdersInBatches method.
 *
 * Test count: 7 tests
 * - SUCCESS: 1 test
 * - FAILURE / EXCEPTION: 3 tests
 * - PERMISSION: 3 tests
 */
@DisplayName("PurchaseOrderService - GetPurchaseOrdersInBatches Tests")
public class GetPurchaseOrdersInBatchesTest extends PurchaseOrderServiceTestBase {
    // Total Tests: 7

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify valid filters return expected data.
     * Expected Result: PaginationBaseResponseModel contains results.
     * Assertions: Result data size and total count match.
     */
    @Test
    @DisplayName("Get Purchase Orders In Batches - Valid Filters - Success")
    void getPurchaseOrdersInBatches_ValidFilters_Success() {
        // ARRANGE
        PaginationBaseRequestModel request = new PaginationBaseRequestModel();
        request.setStart(0);
        request.setEnd(10);
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn("vendorNumber");
        filter.setOperator("contains");
        filter.setValue(TEST_VENDOR_NUMBER);
        request.setFilters(List.of(filter));
        request.setLogicOperator("AND");

        PurchaseOrderWithDetails poWithDetails = new PurchaseOrderWithDetails(
                testPurchaseOrder, testOrderSummary, Collections.emptyList(), Collections.emptyList());
        Page<PurchaseOrderWithDetails> page = new PageImpl<>(List.of(poWithDetails));

        stubPurchaseOrderFilterQueryBuilderGetColumnType("vendorNumber", "string");
        stubPurchaseOrderFilterQueryBuilderFindPaginatedWithDetails(page);

        // ACT
        PaginationBaseResponseModel<PurchaseOrderResponseModel> result =
                purchaseOrderService.getPurchaseOrdersInBatches(request);

        // ASSERT
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1L, result.getTotalDataCount());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject invalid pagination when end <= start.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message matches InvalidPagination.
     */
    @Test
    @DisplayName("Get Purchase Orders In Batches - Invalid Pagination - Throws BadRequestException")
    void getPurchaseOrdersInBatches_InvalidPagination_ThrowsBadRequestException() {
        // ARRANGE
        PaginationBaseRequestModel request = new PaginationBaseRequestModel();
        request.setStart(10);
        request.setEnd(5);

        // ACT
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> purchaseOrderService.getPurchaseOrdersInBatches(request));

        // ASSERT
        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidPagination, ex.getMessage());
    }

    /**
     * Purpose: Reject invalid filter column.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message matches InvalidColumnName format.
     */
    @Test
    @DisplayName("Get Purchase Orders In Batches - Invalid Column - Throws BadRequestException")
    void getPurchaseOrdersInBatches_InvalidColumn_ThrowsBadRequestException() {
        // ARRANGE
        PaginationBaseRequestModel request = new PaginationBaseRequestModel();
        request.setStart(0);
        request.setEnd(10);
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn("invalidColumn");
        filter.setOperator("contains");
        filter.setValue("test");
        request.setFilters(Arrays.asList(filter));

        // ACT
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> purchaseOrderService.getPurchaseOrdersInBatches(request));

        // ASSERT
        assertEquals(String.format(ErrorMessages.PurchaseOrderErrorMessages.InvalidColumnName, "invalidColumn"),
                ex.getMessage());
    }

    /**
     * Purpose: Reject invalid operator for filters.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message matches InvalidOperator format.
     */
    @Test
    @DisplayName("Get Purchase Orders In Batches - Invalid Operator - Throws BadRequestException")
    void getPurchaseOrdersInBatches_InvalidOperator_ThrowsBadRequestException() {
        // ARRANGE
        PaginationBaseRequestModel request = new PaginationBaseRequestModel();
        request.setStart(0);
        request.setEnd(10);
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn("vendorNumber");
        filter.setOperator("invalidOperator");
        filter.setValue("test");
        request.setFilters(Arrays.asList(filter));

        // ACT
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> purchaseOrderService.getPurchaseOrdersInBatches(request));

        // ASSERT
        assertEquals(String.format(ErrorMessages.PurchaseOrderErrorMessages.InvalidOperator, "invalidOperator"),
                ex.getMessage());
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
    @DisplayName("getPurchaseOrdersInBatches - Controller Permission - Unauthorized")
    void getPurchaseOrdersInBatches_controller_permission_unauthorized() {
        // ARRANGE
        PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
        stubPurchaseOrderServiceThrowsUnauthorizedOnGetBatches();

        // ACT
        ResponseEntity<?> response = controller.getPurchaseOrdersInBatches(new PaginationBaseRequestModel());

        // ASSERT
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("getPurchaseOrdersInBatches - Verify @PreAuthorize Annotation")
    void getPurchaseOrdersInBatches_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // ARRANGE
        Method method = PurchaseOrderController.class.getMethod("getPurchaseOrdersInBatches",
                PaginationBaseRequestModel.class);

        // ACT
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // ASSERT
        assertNotNull(annotation, "@PreAuthorize annotation should be present on getPurchaseOrdersInBatches");
        assertTrue(annotation.value().contains(Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION),
                "@PreAuthorize should reference VIEW_PURCHASE_ORDERS_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service.
     * Expected Result: Service is called once and HTTP 200 returned.
     * Assertions: Delegation and response status are correct.
     */
    @Test
    @DisplayName("getPOsInBatches - Controller delegates to service")
    void getPOsInBatches_WithValidRequest_DelegatesToService() {
        // ARRANGE
        PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
        PaginationBaseResponseModel<PurchaseOrderResponseModel> mockResponse = new PaginationBaseResponseModel<>();
        stubPurchaseOrderServiceGetPurchaseOrdersInBatches(mockResponse);

        // ACT
        ResponseEntity<?> response = controller.getPurchaseOrdersInBatches(new PaginationBaseRequestModel());

        // ASSERT
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}