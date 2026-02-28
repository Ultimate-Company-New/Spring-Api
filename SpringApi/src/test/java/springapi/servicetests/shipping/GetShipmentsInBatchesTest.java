package springapi.servicetests.shipping;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import springapi.ErrorMessages;
import springapi.controllers.ShippingController;
import springapi.models.databasemodels.Shipment;
import springapi.models.requestmodels.PaginationBaseRequestModel;
import springapi.models.responsemodels.PaginationBaseResponseModel;

/** Tests for ShippingService.getShipmentsInBatches(). */
@DisplayName("GetShipmentsInBatches Tests")
class GetShipmentsInBatchesTest extends ShippingServiceTestBase {

  // Total Tests: 16
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify success with an empty result set. Expected Result: Response contains zero
   * shipments. Assertions: Response data size is 0.
   */
  @Test
  @DisplayName("getShipmentsInBatches - Empty Result - Success")
  void getShipmentsInBatches_EmptyResult_Success() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    stubShipmentFilterQueryBuilderFindPaginatedEntitiesWithMultipleFilters(
        createShipmentPage(List.of()));

    // Act
    PaginationBaseResponseModel<?> result = shippingService.getShipmentsInBatches(request);

    // Assert
    assertNotNull(result);
    assertEquals(0, result.getData().size());
  }

  /**
   * Purpose: Verify success when logic operator is provided. Expected Result: Response returns
   * without error. Assertions: Response is not null.
   */
  @Test
  @DisplayName("getShipmentsInBatches - Logic Operator - Success")
  void getShipmentsInBatches_LogicOperator_Success() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    request.setLogicOperator(PaginationBaseRequestModel.LOGIC_AND);
    stubShipmentFilterQueryBuilderFindPaginatedEntitiesWithMultipleFilters(
        createShipmentPage(List.of(testShipment)));

    // Act
    PaginationBaseResponseModel<?> result = shippingService.getShipmentsInBatches(request);

    // Assert
    assertNotNull(result);
  }

  /**
   * Purpose: Verify success with multiple shipments returned. Expected Result: Response contains
   * multiple shipments. Assertions: Data size matches expected.
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
   * Purpose: Verify success when selectedIds is provided. Expected Result: Response returns without
   * error. Assertions: Response is not null.
   */
  @Test
  @DisplayName("getShipmentsInBatches - Selected Ids - Success")
  void getShipmentsInBatches_SelectedIds_Success() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    request.setSelectedIds(List.of(1L, 2L));
    stubShipmentFilterQueryBuilderFindPaginatedEntitiesWithMultipleFilters(
        createShipmentPage(List.of(testShipment)));

    // Act
    PaginationBaseResponseModel<?> result = shippingService.getShipmentsInBatches(request);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getData().size());
  }

  /**
   * Purpose: Verify success with a valid filter condition. Expected Result: Response is returned
   * without exception. Assertions: Response data is not null.
   */
  @Test
  @DisplayName("getShipmentsInBatches - Valid Filters - Success")
  void getShipmentsInBatches_ValidFilters_Success() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    PaginationBaseRequestModel.FilterCondition filter =
        createFilterCondition("shipmentId", "equals", "1");
    request.setFilters(List.of(filter));
    stubShipmentFilterQueryBuilderGetColumnType("shipmentId", "number");
    stubShipmentFilterQueryBuilderFindPaginatedEntitiesWithMultipleFilters(
        createShipmentPage(List.of(testShipment)));

    // Act
    PaginationBaseResponseModel<?> result = shippingService.getShipmentsInBatches(request);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getData().size());
  }

  /**
   * Purpose: Verify basic pagination success with a single shipment. Expected Result: Response
   * contains one shipment and total count. Assertions: Response data size and total count.
   */
  @Test
  @DisplayName("getShipmentsInBatches - Valid Request - Success")
  void getShipmentsInBatches_ValidRequest_Success() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    stubShipmentFilterQueryBuilderFindPaginatedEntitiesWithMultipleFilters(
        createShipmentPage(List.of(testShipment)));

    // Act
    PaginationBaseResponseModel<?> result = shippingService.getShipmentsInBatches(request);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertEquals(1, result.getTotalDataCount());
  }

  /**
   * Purpose: Verify pageable offset is propagated from start index. Expected Result: Query builder
   * receives expected offset. Assertions: Response is returned without error.
   */
  @Test
  @DisplayName("getShipmentsInBatches - Offset Propagated - Success")
  void getShipmentsInBatches_OffsetPropagated_Success() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    request.setStart(5);
    request.setEnd(10);
    stubShipmentFilterQueryBuilderFindPaginatedEntitiesWithMultipleFiltersAndOffset(
        createShipmentPage(List.of(testShipment)), 5L);

    // Act
    PaginationBaseResponseModel<?> result = shippingService.getShipmentsInBatches(request);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getData().size());
  }

  /*
   **********************************************************************************************
   * FAILURE TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify end less than start throws BadRequestException. Expected Result:
   * BadRequestException with InvalidPagination message. Assertions: Exception type and message.
   */
  @Test
  @DisplayName("getShipmentsInBatches - End Less Than Start - Throws BadRequestException")
  void getShipmentsInBatches_EndLessThanStart_ThrowsBadRequestException() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    request.setStart(50);
    request.setEnd(25);

    // Act
    springapi.exceptions.BadRequestException ex =
        assertThrows(
            springapi.exceptions.BadRequestException.class,
            () -> shippingService.getShipmentsInBatches(request));

    // Assert
    assertEquals(ErrorMessages.CommonErrorMessages.INVALID_PAGINATION, ex.getMessage());
  }

  /**
   * Purpose: Verify invalid column throws BadRequestException. Expected Result: BadRequestException
   * with invalid column format message. Assertions: Exception type and message.
   */
  @Test
  @DisplayName("getShipmentsInBatches - Invalid Column - Throws BadRequestException")
  void getShipmentsInBatches_InvalidColumn_ThrowsBadRequestException() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    PaginationBaseRequestModel.FilterCondition filter =
        createFilterCondition("invalidColumn", "equals", "1");
    request.setFilters(List.of(filter));

    // Act
    springapi.exceptions.BadRequestException ex =
        assertThrows(
            springapi.exceptions.BadRequestException.class,
            () -> shippingService.getShipmentsInBatches(request));

    // Assert
    assertEquals(
        String.format(
            ErrorMessages.ShipmentErrorMessages.INVALID_COLUMN_NAME_FORMAT, "invalidColumn"),
        ex.getMessage());
  }

  /**
   * Purpose: Verify invalid operator throws BadRequestException. Expected Result:
   * BadRequestException with invalid operator format message. Assertions: Exception type and
   * message.
   */
  @Test
  @DisplayName("getShipmentsInBatches - Invalid Operator - Throws BadRequestException")
  void getShipmentsInBatches_InvalidOperator_ThrowsBadRequestException() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    PaginationBaseRequestModel.FilterCondition filter =
        createFilterCondition("shipmentId", "badOperator", "1");
    request.setFilters(List.of(filter));

    // Act
    springapi.exceptions.BadRequestException ex =
        assertThrows(
            springapi.exceptions.BadRequestException.class,
            () -> shippingService.getShipmentsInBatches(request));

    // Assert
    assertEquals(
        String.format(ErrorMessages.ShipmentErrorMessages.INVALID_OPERATOR_FORMAT, "badOperator"),
        ex.getMessage());
  }

  /**
   * Purpose: Verify negative page size throws BadRequestException. Expected Result:
   * BadRequestException with InvalidPagination message. Assertions: Exception type and message.
   */
  @Test
  @DisplayName("getShipmentsInBatches - Negative Page Size - Throws BadRequestException")
  void getShipmentsInBatches_NegativePageSize_ThrowsBadRequestException() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    request.setStart(100);
    request.setEnd(99);

    // Act
    springapi.exceptions.BadRequestException ex =
        assertThrows(
            springapi.exceptions.BadRequestException.class,
            () -> shippingService.getShipmentsInBatches(request));

    // Assert
    assertEquals(ErrorMessages.CommonErrorMessages.INVALID_PAGINATION, ex.getMessage());
  }

  /**
   * Purpose: Verify start equals end throws BadRequestException. Expected Result:
   * BadRequestException with InvalidPagination message. Assertions: Exception type and message.
   */
  @Test
  @DisplayName("getShipmentsInBatches - Start Equals End - Throws BadRequestException")
  void getShipmentsInBatches_StartEqualsEnd_ThrowsBadRequestException() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    request.setStart(10);
    request.setEnd(10);

    // Act
    springapi.exceptions.BadRequestException ex =
        assertThrows(
            springapi.exceptions.BadRequestException.class,
            () -> shippingService.getShipmentsInBatches(request));

    // Assert
    assertEquals(ErrorMessages.CommonErrorMessages.INVALID_PAGINATION, ex.getMessage());
  }

  /**
   * Purpose: Verify start greater than end throws BadRequestException. Expected Result:
   * BadRequestException with InvalidPagination message. Assertions: Exception type and message.
   */
  @Test
  @DisplayName("getShipmentsInBatches - Start Greater Than End - Throws BadRequestException")
  void getShipmentsInBatches_StartGreaterThanEnd_ThrowsBadRequestException() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    request.setStart(10);
    request.setEnd(5);

    // Act
    springapi.exceptions.BadRequestException ex =
        assertThrows(
            springapi.exceptions.BadRequestException.class,
            () -> shippingService.getShipmentsInBatches(request));

    // Assert
    assertEquals(ErrorMessages.CommonErrorMessages.INVALID_PAGINATION, ex.getMessage());
  }

  /**
   * Purpose: Verify zero page size throws BadRequestException. Expected Result: BadRequestException
   * with InvalidPagination message. Assertions: Exception type and message.
   */
  @Test
  @DisplayName("getShipmentsInBatches - Zero Page Size - Throws BadRequestException")
  void getShipmentsInBatches_ZeroPageSize_ThrowsBadRequestException() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    request.setStart(0);
    request.setEnd(0);

    // Act
    springapi.exceptions.BadRequestException ex =
        assertThrows(
            springapi.exceptions.BadRequestException.class,
            () -> shippingService.getShipmentsInBatches(request));

    // Assert
    assertEquals(ErrorMessages.CommonErrorMessages.INVALID_PAGINATION, ex.getMessage());
  }

  /*
   **********************************************************************************************
   * PERMISSION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify unauthorized access is blocked at the controller level. Expected Result:
   * Unauthorized status is returned. Assertions: Response status is 401 UNAUTHORIZED.
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
}
