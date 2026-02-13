package com.example.SpringApi.Services.Tests.Shipping;

import com.example.SpringApi.Controllers.ShippingController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.Shipment;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ShippingService.getShipmentsInBatches().
 */
@DisplayName("GetShipmentsInBatches Tests")
class GetShipmentsInBatchesTest extends ShippingServiceTestBase {


    // Total Tests: 15
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify success with an empty result set.
     * Expected Result: Response contains zero shipments.
     * Assertions: Response data size is 0.
     */
    @Test
    @DisplayName("getShipmentsInBatches - Empty Result - Success")
    void getShipmentsInBatches_EmptyResult_Success() {
        // Arrange
        PaginationBaseRequestModel request = createValidPaginationRequest();
        stubShipmentFilterQueryBuilderFindPaginatedEntitiesWithMultipleFilters(createShipmentPage(List.of()));

        // Act
        PaginationBaseResponseModel<?> result = shippingService.getShipmentsInBatches(request);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getData().size());
    }

    /**
     * Purpose: Verify success when logic operator is provided.
     * Expected Result: Response returns without error.
     * Assertions: Response is not null.
     */
    @Test
    @DisplayName("getShipmentsInBatches - Logic Operator - Success")
    void getShipmentsInBatches_LogicOperator_Success() {
        // Arrange
        PaginationBaseRequestModel request = createValidPaginationRequest();
        request.setLogicOperator(PaginationBaseRequestModel.LOGIC_AND);
        stubShipmentFilterQueryBuilderFindPaginatedEntitiesWithMultipleFilters(createShipmentPage(List.of(testShipment)));

        // Act
        PaginationBaseResponseModel<?> result = shippingService.getShipmentsInBatches(request);

        // Assert
        assertNotNull(result);
    }

    /**
     * Purpose: Verify success with multiple shipments returned.
     * Expected Result: Response contains multiple shipments.
     * Assertions: Data size matches expected.
     */
    @Test
    @DisplayName("getShipmentsInBatches - Multiple Shipments - Success")
    void getShipmentsInBatches_MultipleShipments_Success() {
        // Arrange
        PaginationBaseRequestModel request = createValidPaginationRequest();
        Shipment shipment2 = createTestShipment(999L);
        stubShipmentFilterQueryBuilderFindPaginatedEntitiesWithMultipleFilters(
                createShipmentPage(List.of(testShipment, shipment2)));

        // Act
        PaginationBaseResponseModel<?> result = shippingService.getShipmentsInBatches(request);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getData().size());
    }

    /**
     * Purpose: Verify success when selectedIds is provided.
     * Expected Result: Response returns without error.
     * Assertions: Response is not null.
     */
    @Test
    @DisplayName("getShipmentsInBatches - Selected Ids - Success")
    void getShipmentsInBatches_SelectedIds_Success() {
        // Arrange
        PaginationBaseRequestModel request = createValidPaginationRequest();
        request.setSelectedIds(List.of(1L, 2L));
        stubShipmentFilterQueryBuilderFindPaginatedEntitiesWithMultipleFilters(createShipmentPage(List.of(testShipment)));

        // Act
        PaginationBaseResponseModel<?> result = shippingService.getShipmentsInBatches(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
    }

    /**
     * Purpose: Verify success with a valid filter condition.
     * Expected Result: Response is returned without exception.
     * Assertions: Response data is not null.
     */
    @Test
    @DisplayName("getShipmentsInBatches - Valid Filters - Success")
    void getShipmentsInBatches_ValidFilters_Success() {
        // Arrange
        PaginationBaseRequestModel request = createValidPaginationRequest();
        PaginationBaseRequestModel.FilterCondition filter = createFilterCondition("shipmentId", "equals", "1");
        request.setFilters(List.of(filter));
        stubShipmentFilterQueryBuilderGetColumnType("shipmentId", "number");
        stubShipmentFilterQueryBuilderFindPaginatedEntitiesWithMultipleFilters(createShipmentPage(List.of(testShipment)));

        // Act
        PaginationBaseResponseModel<?> result = shippingService.getShipmentsInBatches(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
    }

    /**
     * Purpose: Verify basic pagination success with a single shipment.
     * Expected Result: Response contains one shipment and total count.
     * Assertions: Response data size and total count.
     */
    @Test
    @DisplayName("getShipmentsInBatches - Valid Request - Success")
    void getShipmentsInBatches_ValidRequest_Success() {
        // Arrange
        PaginationBaseRequestModel request = createValidPaginationRequest();
        stubShipmentFilterQueryBuilderFindPaginatedEntitiesWithMultipleFilters(createShipmentPage(List.of(testShipment)));

        // Act
        PaginationBaseResponseModel<?> result = shippingService.getShipmentsInBatches(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1, result.getTotalDataCount());
    }

    /*
     **********************************************************************************************
     * FAILURE TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify end less than start throws BadRequestException.
     * Expected Result: BadRequestException with InvalidPagination message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("getShipmentsInBatches - End Less Than Start - Throws BadRequestException")
    void getShipmentsInBatches_EndLessThanStart_ThrowsBadRequestException() {
        // Arrange
        PaginationBaseRequestModel request = createValidPaginationRequest();
        request.setStart(50);
        request.setEnd(25);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
            com.example.SpringApi.Exceptions.BadRequestException.class,
            () -> shippingService.getShipmentsInBatches(request));

        // Assert
        assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
    }

    /**
     * Purpose: Verify invalid column throws BadRequestException.
     * Expected Result: BadRequestException with invalid column format message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("getShipmentsInBatches - Invalid Column - Throws BadRequestException")
    void getShipmentsInBatches_InvalidColumn_ThrowsBadRequestException() {
        // Arrange
        PaginationBaseRequestModel request = createValidPaginationRequest();
        PaginationBaseRequestModel.FilterCondition filter = createFilterCondition("invalidColumn", "equals", "1");
        request.setFilters(List.of(filter));

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
            com.example.SpringApi.Exceptions.BadRequestException.class,
            () -> shippingService.getShipmentsInBatches(request));

        // Assert
        assertEquals(String.format(ErrorMessages.ShipmentErrorMessages.InvalidColumnNameFormat, "invalidColumn"),
            ex.getMessage());
    }

    /**
     * Purpose: Verify invalid operator throws BadRequestException.
     * Expected Result: BadRequestException with invalid operator format message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("getShipmentsInBatches - Invalid Operator - Throws BadRequestException")
    void getShipmentsInBatches_InvalidOperator_ThrowsBadRequestException() {
        // Arrange
        PaginationBaseRequestModel request = createValidPaginationRequest();
        PaginationBaseRequestModel.FilterCondition filter = createFilterCondition("shipmentId", "badOperator", "1");
        request.setFilters(List.of(filter));

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
            com.example.SpringApi.Exceptions.BadRequestException.class,
            () -> shippingService.getShipmentsInBatches(request));

        // Assert
        assertEquals(String.format(ErrorMessages.ShipmentErrorMessages.InvalidOperatorFormat, "badOperator"),
            ex.getMessage());
    }

    /**
     * Purpose: Verify negative page size throws BadRequestException.
     * Expected Result: BadRequestException with InvalidPagination message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("getShipmentsInBatches - Negative Page Size - Throws BadRequestException")
    void getShipmentsInBatches_NegativePageSize_ThrowsBadRequestException() {
        // Arrange
        PaginationBaseRequestModel request = createValidPaginationRequest();
        request.setStart(100);
        request.setEnd(99);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
            com.example.SpringApi.Exceptions.BadRequestException.class,
            () -> shippingService.getShipmentsInBatches(request));

        // Assert
        assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
    }

    /**
     * Purpose: Verify start equals end throws BadRequestException.
     * Expected Result: BadRequestException with InvalidPagination message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("getShipmentsInBatches - Start Equals End - Throws BadRequestException")
    void getShipmentsInBatches_StartEqualsEnd_ThrowsBadRequestException() {
        // Arrange
        PaginationBaseRequestModel request = createValidPaginationRequest();
        request.setStart(10);
        request.setEnd(10);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
            com.example.SpringApi.Exceptions.BadRequestException.class,
            () -> shippingService.getShipmentsInBatches(request));

        // Assert
        assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
    }

    /**
     * Purpose: Verify start greater than end throws BadRequestException.
     * Expected Result: BadRequestException with InvalidPagination message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("getShipmentsInBatches - Start Greater Than End - Throws BadRequestException")
    void getShipmentsInBatches_StartGreaterThanEnd_ThrowsBadRequestException() {
        // Arrange
        PaginationBaseRequestModel request = createValidPaginationRequest();
        request.setStart(10);
        request.setEnd(5);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
            com.example.SpringApi.Exceptions.BadRequestException.class,
            () -> shippingService.getShipmentsInBatches(request));

        // Assert
        assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
    }

    /**
     * Purpose: Verify zero page size throws BadRequestException.
     * Expected Result: BadRequestException with InvalidPagination message.
     * Assertions: Exception type and message.
     */
    @Test
    @DisplayName("getShipmentsInBatches - Zero Page Size - Throws BadRequestException")
    void getShipmentsInBatches_ZeroPageSize_ThrowsBadRequestException() {
        // Arrange
        PaginationBaseRequestModel request = createValidPaginationRequest();
        request.setStart(0);
        request.setEnd(0);

        // Act
        com.example.SpringApi.Exceptions.BadRequestException ex = assertThrows(
            com.example.SpringApi.Exceptions.BadRequestException.class,
            () -> shippingService.getShipmentsInBatches(request));

        // Assert
        assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
    }

    /*
     **********************************************************************************************
     * PERMISSION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify unauthorized access is blocked at the controller level.
     * Expected Result: Unauthorized status is returned.
     * Assertions: Response status is 401 UNAUTHORIZED.
     */
    @Test
    @DisplayName("getShipmentsInBatches - Controller Permission - Unauthorized")
    void getShipmentsInBatches_controller_permission_unauthorized() {
        // Arrange
        PaginationBaseRequestModel request = createValidPaginationRequest();
        ShippingController controller = new ShippingController(shippingServiceMock);
        stubShippingServiceMockGetShipmentsInBatchesUnauthorized();

        // Act
        ResponseEntity<?> response = controller.getShipmentsInBatches(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify controller has @PreAuthorize for getShipmentsInBatches.
     * Expected Result: Annotation exists and includes VIEW_SHIPMENTS_PERMISSION.
     * Assertions: Annotation is present and contains permission.
     */
    @Test
    @DisplayName("getShipmentsInBatches - Verify @PreAuthorize Annotation")
    void getShipmentsInBatches_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        Method method = ShippingController.class.getMethod("getShipmentsInBatches", PaginationBaseRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation);
        assertTrue(annotation.value().contains(Authorizations.VIEW_SHIPMENTS_PERMISSION));
    }
}
