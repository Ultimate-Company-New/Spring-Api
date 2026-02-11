package com.example.SpringApi.Services.Tests.Promo;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.Promo;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.PromoResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for PromoService.getPromosInBatches method.
 */
@DisplayName("PromoService - GetPromosInBatches Tests")
class GetPromosInBatchesTest extends PromoServiceTestBase {

    // Total Tests: 10

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify valid request returns results.
     * Expected Result: Response contains promo data.
     * Assertions: data not empty.
     */
    @Test
    @DisplayName("getPromosInBatches - Valid Request - Success")
    void getPromosInBatches_ValidRequest_Success() {
        // Arrange
        Page<Promo> promoPage = new PageImpl<>(Arrays.asList(testPromo), PageRequest.of(0, 10), 1);
        stubPromoFilterQueryBuilderFindPaginatedEntities(promoPage);

        // Act
        PaginationBaseResponseModel<Promo> result = promoService.getPromosInBatches(testPaginationRequest);

        // Assert
        assertEquals(1, result.getData().size());
    }

    /*
     * Purpose: Verify include deleted request returns results.
     * Expected Result: Response contains promo data.
     * Assertions: data not empty.
     */
    @Test
    @DisplayName("getPromosInBatches - Include Deleted - Success")
    void getPromosInBatches_IncludeDeleted_Success() {
        // Arrange
        testPaginationRequest.setIncludeDeleted(true);
        Page<Promo> promoPage = new PageImpl<>(Arrays.asList(testPromo), PageRequest.of(0, 10), 1);
        stubPromoFilterQueryBuilderFindPaginatedEntities(promoPage);

        // Act
        PaginationBaseResponseModel<Promo> result = promoService.getPromosInBatches(testPaginationRequest);

        // Assert
        assertEquals(1, result.getData().size());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify invalid column throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message matches.
     */
    @Test
    @DisplayName("getPromosInBatches - Invalid Column - Throws BadRequestException")
    void getPromosInBatches_InvalidColumn_ThrowsBadRequestException() {
        // Arrange
        testPaginationRequest.getFilters().get(0).setColumn("invalidColumn");

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.getPromosInBatches(testPaginationRequest));

        // Assert
        assertEquals(String.format(ErrorMessages.CommonErrorMessages.InvalidColumnName, "invalidColumn"),
                ex.getMessage());
    }

    /*
     * Purpose: Verify negative start throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message matches.
     */
    @Test
    @DisplayName("getPromosInBatches - Negative Start - Throws BadRequestException")
    void getPromosInBatches_NegativeStart_ThrowsBadRequestException() {
        // Arrange
        testPaginationRequest.setStart(-1);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.getPromosInBatches(testPaginationRequest));

        // Assert
        assertEquals(ErrorMessages.CommonErrorMessages.StartIndexCannotBeNegative, ex.getMessage());
    }

    /*
     * Purpose: Verify null request throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message matches.
     */
    @Test
    @DisplayName("getPromosInBatches - Null Request - Throws BadRequestException")
    void getPromosInBatches_NullRequest_ThrowsException() {
        // Arrange
        testPaginationRequest = null;

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> promoService.getPromosInBatches(testPaginationRequest));

        // Assert
        assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
    }

    /*
     **********************************************************************************************
     * PERMISSION TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify controller delegation.
     * Expected Result: OK status.
     * Assertions: status is OK.
     */
    @Test
    @DisplayName("getPromosInBatches - Controller Delegation - Success")
    void getPromosInBatches_ControllerDelegation_Success() {
        // Arrange
        PaginationBaseResponseModel<PromoResponseModel> responseModel = new PaginationBaseResponseModel<>();
        responseModel.setData(List.of(new PromoResponseModel(testPromo)));
        stubServiceGetPromosInBatchesReturns(responseModel);

        // Act
        ResponseEntity<?> response = promoController.getPromosInBatches(testPaginationRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /*
     * Purpose: Verify unauthorized access is blocked at controller level.
     * Expected Result: Unauthorized status.
     * Assertions: status is 401.
     */
    @Test
    @DisplayName("getPromosInBatches - Controller Permission - Unauthorized")
    void getPromosInBatches_controller_permission_unauthorized() {
        // Arrange
        stubServiceThrowsUnauthorizedException();

        // Act
        ResponseEntity<?> response = promoController.getPromosInBatches(testPaginationRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /*
     * Purpose: Verify controller handles bad request.
     * Expected Result: BAD_REQUEST status.
     * Assertions: status is 400.
     */
    @Test
    @DisplayName("getPromosInBatches - Controller Handles BadRequest - Success")
    void getPromosInBatches_ControllerHandlesBadRequest_Success() {
        // Arrange
        stubServiceGetPromosInBatchesThrowsBadRequest("Bad Filter");

        // Act
        ResponseEntity<?> response = promoController.getPromosInBatches(testPaginationRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /*
     * Purpose: Verify controller handles exception.
     * Expected Result: INTERNAL_SERVER_ERROR status.
     * Assertions: status is 500.
     */
    @Test
    @DisplayName("getPromosInBatches - Controller Handles Exception - Failure")
    void getPromosInBatches_ControllerHandlesException_Failure() {
        // Arrange
        stubServiceGetPromosInBatchesThrowsRuntime("Crash");

        // Act
        ResponseEntity<?> response = promoController.getPromosInBatches(testPaginationRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}

/**
 * Test class for PromoService.getPromosInBatches method.
 */
@DisplayName("PromoService - GetPromosInBatches Tests - Duplicate Block")
class GetPromosInBatchesTestDuplicate extends PromoServiceTestBase {

    // Total Tests: 13

    /*
     **********************************************************************************************
     * SECTION 1: SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify successful retrieval of a batch of promos.
     */
    @Test
    @DisplayName("Get Promos In Batches - Success - Valid request")
    void getPromosInBatches_ValidRequest_Success() {
        // Arrange
        Page<Promo> page = new PageImpl<>(List.of(testPromo));
        when(promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), anyList(), anyBoolean(), any()))
                .thenReturn(page);

        // Act
        PaginationBaseResponseModel<Promo> result = promoService.getPromosInBatches(testPaginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1, result.getTotalDataCount());
        verify(promoFilterQueryBuilder).findPaginatedEntitiesWithMultipleFilters(
                eq(TEST_CLIENT_ID), any(), eq("AND"), anyList(), eq(false), any());
    }

    /**
     * Purpose: Verify retrieval with includeDeleted = true.
     */
    @Test
    @DisplayName("Get Promos In Batches - Success - Include deleted")
    void getPromosInBatches_IncludeDeleted_Success() {
        // Arrange
        testPaginationRequest.setIncludeDeleted(true);
        Page<Promo> page = new PageImpl<>(List.of(testPromo));
        when(promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), anyList(), anyBoolean(), any()))
                .thenReturn(page);

        // Act
        PaginationBaseResponseModel<Promo> result = promoService.getPromosInBatches(testPaginationRequest);

        // Assert
        verify(promoFilterQueryBuilder).findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), anyList(), eq(true), any());
        assertNotNull(result);
    }

    /**
     * Purpose: Verify retrieval with OR logic operator.
     */
    @Test
    @DisplayName("Get Promos In Batches - Success - OR Logic")
    void getPromosInBatches_OrLogic_Success() {
        // Arrange
        testPaginationRequest.setLogicOperator("OR");
        Page<Promo> page = new PageImpl<>(List.of(testPromo));
        when(promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), anyList(), anyBoolean(), any()))
                .thenReturn(page);

        // Act
        promoService.getPromosInBatches(testPaginationRequest);

        // Assert
        verify(promoFilterQueryBuilder).findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), eq("OR"), anyList(), anyBoolean(), any());
    }

    /**
     * Purpose: Verify retrieval with large page size.
     */
    @Test
    @DisplayName("Get Promos In Batches - Success - Large page size")
    void getPromosInBatches_LargePage_Success() {
        // Arrange
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(1000);
        Page<Promo> page = new PageImpl<>(List.of(testPromo));
        when(promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), anyList(), anyBoolean(), any()))
                .thenReturn(page);

        // Act
        promoService.getPromosInBatches(testPaginationRequest);

        // Assert
        verify(promoFilterQueryBuilder).findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), anyList(), anyBoolean(), argThat(pb -> pb.getPageSize() == 1000));
    }

    /**
     * Purpose: Verify retrieval with multiple filters.
     */
    @Test
    @DisplayName("Get Promos In Batches - Success - Multiple Filters")
    void getPromosInBatches_MultipleFilters_Success() {
        // Arrange
        PaginationBaseRequestModel.FilterCondition f1 = new PaginationBaseRequestModel.FilterCondition();
        f1.setColumn("promoCode");
        f1.setOperator("equals");
        f1.setValue("CODE1");
        PaginationBaseRequestModel.FilterCondition f2 = new PaginationBaseRequestModel.FilterCondition();
        f2.setColumn("isDeleted");
        f2.setOperator("is");
        f2.setValue("true");
        testPaginationRequest.setFilters(List.of(f1, f2));

        Page<Promo> page = new PageImpl<>(List.of(testPromo));
        when(promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), anyList(), anyBoolean(), any()))
                .thenReturn(page);

        // Act
        promoService.getPromosInBatches(testPaginationRequest);

        // Assert
        verify(promoFilterQueryBuilder).findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), argThat(list -> list.size() == 2), anyBoolean(), any());
    }

    /*
     **********************************************************************************************
     * SECTION 2: FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject retrieval if an invalid column name is provided in filters.
     */
    @Test
    @DisplayName("Get Promos In Batches - Failure - Invalid Column")
    void getPromosInBatches_InvalidColumn_ThrowsBadRequestException() {
        // Arrange
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn("invalidColumn");
        testPaginationRequest.setFilters(List.of(filter));

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.getPromosInBatches(testPaginationRequest));
        assertEquals(String.format(ErrorMessages.CommonErrorMessages.InvalidColumnName, "invalidColumn"),
            ex.getMessage());
    }

    /**
     * Purpose: Reject retrieval if page size (end - start) is zero or negative.
     */
    @Test
    @DisplayName("Get Promos In Batches - Failure - Invalid Pagination (Zero Page Size)")
    void getPromosInBatches_ZeroPageSize_ThrowsBadRequestException() {
        // Arrange
        testPaginationRequest.setStart(10);
        testPaginationRequest.setEnd(10);

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.getPromosInBatches(testPaginationRequest));
        assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
    }

    /**
     * Purpose: Reject retrieval if start index is negative.
     */
    @Test
    @DisplayName("Get Promos In Batches - Failure - Negative Start Index")
    void getPromosInBatches_NegativeStart_ThrowsBadRequestException() {
        // Arrange
        testPaginationRequest.setStart(-1);
        testPaginationRequest.setEnd(10);

        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.getPromosInBatches(testPaginationRequest));
        assertEquals(ErrorMessages.CommonErrorMessages.StartIndexCannotBeNegative, ex.getMessage());
    }

    /**
     * Purpose: Reject retrieval if pagination model is null.
     */
    @Test
    @DisplayName("Get Promos In Batches - Failure - Null Request Model")
    void getPromosInBatches_NullRequest_ThrowsException() {
        // Act & Assert
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
                com.example.SpringApi.Exceptions.BadRequestException.class,
                () -> promoService.getPromosInBatches(null));
        assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
    }

    /**
     * Purpose: Verify validation of filter operator against column type.
     */
    @Test
    @DisplayName("Get Promos In Batches - Failure - Invalid operator for type")
    void getPromosInBatches_InvalidOperatorForType_ThrowsException() {
        // Arrange
        PaginationBaseRequestModel request = new PaginationBaseRequestModel();
        request.setStart(0);
        request.setEnd(10);
        request.setIncludeDeleted(false);

        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn("startDate"); // Date type
        filter.setOperator("contains"); // String operator, invalid for Date
        filter.setValue("2024");
        request.setFilters(List.of(filter));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> promoService.getPromosInBatches(request));
    }

    /*
     **********************************************************************************************
     * SECTION 3: CONTROLLER PERMISSION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify that the controller correctly delegates getPromosInBatches
     * calls to the service layer.
     */
    @Test
    @DisplayName("getPromosInBatches - Controller delegates to service")
    void getPromosInBatches_ControllerDelegation_Success() {
        // Arrange
        PaginationBaseResponseModel<Promo> mockResponse = new PaginationBaseResponseModel<>();
        doReturn(mockResponse).when(promoService).getPromosInBatches(any(PaginationBaseRequestModel.class));

        // Act
        ResponseEntity<?> response = promoController.getPromosInBatches(testPaginationRequest);

        // Assert
        verify(promoService).getPromosInBatches(testPaginationRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    /**
     * Purpose: Verify unauthorized access is blocked at the controller level.
     */
    @Test
    @DisplayName("getPromosInBatches - Controller Permission - Unauthorized")
    void getPromosInBatches_controller_permission_unauthorized() {
        // Arrange
        stubServiceThrowsUnauthorizedException();

        // Act
        ResponseEntity<?> response = promoController.getPromosInBatches(testPaginationRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify controller handles BadRequestException from service.
     */
    @Test
    @DisplayName("getPromosInBatches - Controller handles BadRequestException")
    void getPromosInBatches_ControllerHandlesBadRequest() {
        // Arrange
        doThrow(new com.example.SpringApi.Exceptions.BadRequestException("Bad Filter"))
                .when(promoService).getPromosInBatches(any());

        // Act
        ResponseEntity<?> response = promoController.getPromosInBatches(testPaginationRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    /**
     * Purpose: Verify controller handles generic Exception from service.
     */
    @Test
    @DisplayName("getPromosInBatches - Controller handles Exception")
    void getPromosInBatches_ControllerHandlesException() {
        // Arrange
        doThrow(new RuntimeException("Crash"))
                .when(promoService).getPromosInBatches(any());

        // Act
        ResponseEntity<?> response = promoController.getPromosInBatches(testPaginationRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
