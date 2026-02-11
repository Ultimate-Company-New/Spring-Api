package com.example.SpringApi.Services.Tests.PurchaseOrder;

import com.example.SpringApi.Controllers.PurchaseOrderController;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DTOs.PurchaseOrderWithDetails;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.PurchaseOrderResponseModel;
import com.example.SpringApi.Models.Authorizations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;

/**
 * Test class for PurchaseOrderService.getPurchaseOrdersInBatches method.
 *
 * Test count: 1 comprehensive test
 * - Covers invalid pagination, invalid column, invalid operator, and success scenarios
 */
@DisplayName("PurchaseOrderService - GetPOsInBatches Tests")
public class GetPOsInBatchesTest extends PurchaseOrderServiceTestBase {

    @Test
    @DisplayName("Get POs In Batches - Comprehensive validation and success")
    void getPurchaseOrdersInBatches_Comprehensive() {
        // Invalid pagination
        PaginationBaseRequestModel invalidPagination = new PaginationBaseRequestModel();
        invalidPagination.setStart(10);
        invalidPagination.setEnd(5);
        BadRequestException paginationEx = assertThrows(BadRequestException.class,
                () -> purchaseOrderService.getPurchaseOrdersInBatches(invalidPagination));
        assertEquals("Invalid pagination: end must be greater than start", paginationEx.getMessage());

        // Invalid column
        PaginationBaseRequestModel invalidColumn = new PaginationBaseRequestModel();
        invalidColumn.setStart(0);
        invalidColumn.setEnd(10);
        PaginationBaseRequestModel.FilterCondition invalidFilter = new PaginationBaseRequestModel.FilterCondition();
        invalidFilter.setColumn("invalidColumn");
        invalidFilter.setOperator("contains");
        invalidFilter.setValue("test");
        invalidColumn.setFilters(Arrays.asList(invalidFilter));
        invalidColumn.setLogicOperator("AND");
        BadRequestException invalidColumnEx = assertThrows(BadRequestException.class,
                () -> purchaseOrderService.getPurchaseOrdersInBatches(invalidColumn));
        assertEquals("Invalid column name: invalidColumn", invalidColumnEx.getMessage());

        // Invalid operator
        PaginationBaseRequestModel invalidOperator = new PaginationBaseRequestModel();
        invalidOperator.setStart(0);
        invalidOperator.setEnd(10);
        PaginationBaseRequestModel.FilterCondition badOp = new PaginationBaseRequestModel.FilterCondition();
        badOp.setColumn("vendorNumber");
        badOp.setOperator("invalidOperator");
        badOp.setValue("test");
        invalidOperator.setFilters(Arrays.asList(badOp));
        invalidOperator.setLogicOperator("AND");
        lenient().when(purchaseOrderFilterQueryBuilder.getColumnType("vendorNumber")).thenReturn("string");
        BadRequestException invalidOpEx = assertThrows(BadRequestException.class,
                () -> purchaseOrderService.getPurchaseOrdersInBatches(invalidOperator));
        assertEquals("Invalid operator: invalidOperator", invalidOpEx.getMessage());

        // Success with filters
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn("vendorNumber");
        filter.setOperator("contains");
        filter.setValue(TEST_VENDOR_NUMBER);
        paginationRequest.setFilters(Arrays.asList(filter));
        paginationRequest.setLogicOperator("AND");

        PurchaseOrderWithDetails poWithDetails = new PurchaseOrderWithDetails(
                testPurchaseOrder, testOrderSummary, new ArrayList<>(), new ArrayList<>());
        Page<PurchaseOrderWithDetails> page = new PageImpl<>(Arrays.asList(poWithDetails));

        lenient().when(purchaseOrderFilterQueryBuilder.getColumnType("vendorNumber")).thenReturn("string");
        when(purchaseOrderFilterQueryBuilder.findPaginatedWithDetails(
                anyLong(), any(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(page);

        PaginationBaseResponseModel<PurchaseOrderResponseModel> result = purchaseOrderService
                .getPurchaseOrdersInBatches(paginationRequest);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1L, result.getTotalDataCount());
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("getPurchaseOrdersInBatches - Verify @PreAuthorize Annotation")
    void getPurchaseOrdersInBatches_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PurchaseOrderController.class.getMethod("getPurchaseOrdersInBatches", PaginationBaseRequestModel.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on getPurchaseOrdersInBatches");
        assertTrue(annotation.value().contains(Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION),
                "@PreAuthorize should reference VIEW_PURCHASE_ORDERS_PERMISSION");
    }

    @Test
    @DisplayName("getPurchaseOrdersInBatches - Controller delegates to service")
    void getPurchaseOrdersInBatches_WithValidRequest_DelegatesToService() {
                com.example.SpringApi.Services.PurchaseOrderService mockService = mock(com.example.SpringApi.Services.PurchaseOrderService.class);
                PurchaseOrderController controller = new PurchaseOrderController(mockService);
        PaginationBaseRequestModel request = new PaginationBaseRequestModel();
        PaginationBaseResponseModel<PurchaseOrderResponseModel> mockResponse = new PaginationBaseResponseModel<>();
                when(mockService.getPurchaseOrdersInBatches(request)).thenReturn(mockResponse);

        ResponseEntity<?> response = controller.getPurchaseOrdersInBatches(request);

                verify(mockService).getPurchaseOrdersInBatches(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
