package springapi.servicetests.purchaseorder;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import springapi.ErrorMessages;
import springapi.controllers.PurchaseOrderController;
import springapi.exceptions.BadRequestException;
import springapi.models.dtos.PurchaseOrderWithDetails;
import springapi.models.requestmodels.PaginationBaseRequestModel;
import springapi.models.responsemodels.PaginationBaseResponseModel;
import springapi.models.responsemodels.PurchaseOrderResponseModel;

/**
 * Test class for PurchaseOrderService.getPurchaseOrdersInBatches method.
 *
 * <p>Test count: 7 tests
 */
@DisplayName("PurchaseOrderService - GetPurchaseOrdersInBatches Tests")
class GetPurchaseOrdersInBatchesTest extends PurchaseOrderServiceTestBase {

  // Total Tests: 7
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify valid filters return expected data. Expected Result:
   * PaginationBaseResponseModel contains results. Assertions: Result data size and total count
   * match.
   */
  @Test
  @DisplayName("Get Purchase Orders In Batches - Valid Filters - Success")
  void getPurchaseOrdersInBatches_ValidFilters_Success() {
    // Arrange
    PaginationBaseRequestModel request = new PaginationBaseRequestModel();
    request.setStart(0);
    request.setEnd(10);
    PaginationBaseRequestModel.FilterCondition filter =
        new PaginationBaseRequestModel.FilterCondition();
    filter.setColumn("vendorNumber");
    filter.setOperator("contains");
    filter.setValue(TEST_VENDOR_NUMBER);
    request.setFilters(List.of(filter));
    request.setLogicOperator("AND");

    PurchaseOrderWithDetails poWithDetails =
        new PurchaseOrderWithDetails(
            testPurchaseOrder, testOrderSummary, Collections.emptyList(), Collections.emptyList());
    Page<PurchaseOrderWithDetails> page = new PageImpl<>(List.of(poWithDetails));

    stubPurchaseOrderFilterQueryBuilderGetColumnType("vendorNumber", "string");
    stubPurchaseOrderFilterQueryBuilderFindPaginatedWithDetails(page);

    // Act
    PaginationBaseResponseModel<PurchaseOrderResponseModel> result =
        purchaseOrderService.getPurchaseOrdersInBatches(request);

    // Assert
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
   * Purpose: Reject invalid filter column. Expected Result: BadRequestException is thrown.
   * Assertions: Message matches InvalidColumnName format.
   */
  @Test
  @DisplayName("Get Purchase Orders In Batches - Invalid Column - Throws BadRequestException")
  void getPurchaseOrdersInBatches_InvalidColumn_ThrowsBadRequestException() {
    // Arrange
    PaginationBaseRequestModel request = new PaginationBaseRequestModel();
    request.setStart(0);
    request.setEnd(10);
    PaginationBaseRequestModel.FilterCondition filter =
        new PaginationBaseRequestModel.FilterCondition();
    filter.setColumn("invalidColumn");
    filter.setOperator("contains");
    filter.setValue("test");
    request.setFilters(Arrays.asList(filter));

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> purchaseOrderService.getPurchaseOrdersInBatches(request));

    // Assert
    assertEquals(
        String.format(
            ErrorMessages.PurchaseOrderErrorMessages.INVALID_COLUMN_NAME, "invalidColumn"),
        ex.getMessage());
  }

  /**
   * Purpose: Reject invalid operator for filters. Expected Result: BadRequestException is thrown.
   * Assertions: Message matches InvalidOperator format.
   */
  @Test
  @DisplayName("Get Purchase Orders In Batches - Invalid Operator - Throws BadRequestException")
  void getPurchaseOrdersInBatches_InvalidOperator_ThrowsBadRequestException() {
    // Arrange
    PaginationBaseRequestModel request = new PaginationBaseRequestModel();
    request.setStart(0);
    request.setEnd(10);
    PaginationBaseRequestModel.FilterCondition filter =
        new PaginationBaseRequestModel.FilterCondition();
    filter.setColumn("vendorNumber");
    filter.setOperator("invalidOperator");
    filter.setValue("test");
    request.setFilters(Arrays.asList(filter));

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> purchaseOrderService.getPurchaseOrdersInBatches(request));

    // Assert
    assertEquals(
        String.format(ErrorMessages.PurchaseOrderErrorMessages.INVALID_OPERATOR, "invalidOperator"),
        ex.getMessage());
  }

  /**
   * Purpose: Reject invalid pagination when end <= start. Expected Result: BadRequestException is
   * thrown. Assertions: Message matches InvalidPagination.
   */
  @Test
  @DisplayName("Get Purchase Orders In Batches - Invalid Pagination - Throws BadRequestException")
  void getPurchaseOrdersInBatches_InvalidPagination_ThrowsBadRequestException() {
    // Arrange
    PaginationBaseRequestModel request = new PaginationBaseRequestModel();
    request.setStart(10);
    request.setEnd(5);

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> purchaseOrderService.getPurchaseOrdersInBatches(request));

    // Assert
    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_PAGINATION, ex.getMessage());
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
  @DisplayName("getPurchaseOrdersInBatches - Controller Permission - Unauthorized")
  void getPurchaseOrdersInBatches_controller_permission_unauthorized() {
    // Arrange
    PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
    stubPurchaseOrderServiceThrowsUnauthorizedOnGetBatches();

    // Act
    ResponseEntity<?> response =
        controller.getPurchaseOrdersInBatches(new PaginationBaseRequestModel());

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  /**
   * Purpose: Verify controller delegates to service. Expected Result: Service is called once and
   * HTTP 200 returned. Assertions: Delegation and response status are correct.
   */
  @Test
  @DisplayName("getPurchaseOrdersInBatches - Controller delegates to service")
  void getPurchaseOrdersInBatches_WithValidRequest_DelegatesToService() {
    // Arrange
    PurchaseOrderController controller = new PurchaseOrderController(purchaseOrderServiceMock);
    PaginationBaseResponseModel<PurchaseOrderResponseModel> mockResponse =
        new PaginationBaseResponseModel<>();
    stubPurchaseOrderServiceGetPurchaseOrdersInBatches(mockResponse);

    // Act
    ResponseEntity<?> response =
        controller.getPurchaseOrdersInBatches(new PaginationBaseRequestModel());

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}
