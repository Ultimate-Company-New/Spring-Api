package com.example.SpringApi.ServiceTests.Product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.SpringApi.Controllers.ProductController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.Product;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.ProductResponseModel;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Consolidated test class for ProductService.getProductInBatches. Fully compliant with Unit Test
 * Verification rules.
 */
@DisplayName("ProductService - GetProductInBatches Tests")
class GetProductInBatchesTest extends ProductServiceTestBase {

  // Total Tests: 15
  @BeforeEach
  void setUpFilters() {
    // Stub ProductFilterQueryBuilder.getColumnType for various categories
    stubProductFilterQueryBuilderGetColumnType();

    // Default empty page stub
    stubProductFilterQueryBuilderFindPaginatedEntitiesEmptyPage();
  }

  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify includeDeleted flag propagation. Expected Result: Query executed with
   * includeDeleted = true. Assertions: Filter query builder invoked with includeDeleted true.
   */
  @Test
  @DisplayName("getProductInBatches - IncludeDeleted flag - Success")
  void getProductInBatches_IncludeDeleted_Success() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    request.setIncludeDeleted(true);

    // Act
    assertDoesNotThrow(() -> productService.getProductInBatches(request));

    // Assert
    verify(productFilterQueryBuilder)
        .findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), anyString(), any(), eq(true), any(Pageable.class));
  }

  /**
   * Purpose: Verify multiple filters with AND logic. Expected Result: Query executed with AND
   * operator. Assertions: Filter query builder invoked with AND logic.
   */
  @Test
  @DisplayName("getProductInBatches - Multiple filters AND logic - Success")
  void getProductInBatches_MultipleFiltersAnd_Success() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    request.setLogicOperator("AND");
    request.setFilters(
        Arrays.asList(
            createFilter("brand", "equals", "Test"), createFilter("price", "lessThan", "500")));

    // Act
    assertDoesNotThrow(() -> productService.getProductInBatches(request));

    // Assert
    verify(productFilterQueryBuilder)
        .findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), eq("AND"), anyList(), anyBoolean(), any(Pageable.class));
  }

  /**
   * Purpose: Verify multiple filters with OR logic. Expected Result: Query executed with OR
   * operator. Assertions: Filter query builder invoked with OR logic.
   */
  @Test
  @DisplayName("getProductInBatches - Multiple filters OR logic - Success")
  void getProductInBatches_MultipleFiltersOr_Success() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    request.setLogicOperator("OR");
    request.setFilters(
        Arrays.asList(
            createFilter("brand", "equals", "Test"), createFilter("price", "lessThan", "500")));

    // Act
    assertDoesNotThrow(() -> productService.getProductInBatches(request));

    // Assert
    verify(productFilterQueryBuilder)
        .findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), eq("OR"), anyList(), anyBoolean(), any(Pageable.class));
  }

  /**
   * Purpose: Verify retrieval without filters. Expected Result: Results returned for default
   * request. Assertions: Response contains product data.
   */
  @Test
  @DisplayName("getProductInBatches - No filters - Success")
  void getProductInBatches_NoFilters_Success() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    List<Product> list = Collections.singletonList(testProduct);
    Page<Product> page = new PageImpl<>(list, PageRequest.of(0, 10), 1);
    stubProductFilterQueryBuilderFindPaginatedEntities(page);

    // Act
    PaginationBaseResponseModel<ProductResponseModel> result =
        productService.getProductInBatches(request);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getData().size());
    verify(productFilterQueryBuilder)
        .findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class));
  }

  /**
   * Purpose: Verify filter by pickupLocationId (special join logic). Expected Result: Query
   * executes without error. Assertions: No exception thrown.
   */
  @Test
  @DisplayName("getProductInBatches - Filter by pickupLocationId - Success")
  void getProductInBatches_PickupLocationFilter_Success() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    request.setFilters(Collections.singletonList(createFilter("pickupLocationId", "equals", "1")));

    // Act & Assert
    assertDoesNotThrow(() -> productService.getProductInBatches(request));
  }

  /**
   * Purpose: Verify price 'greaterThan' filter. Expected Result: Query executes without error.
   * Assertions: No exception thrown.
   */
  @Test
  @DisplayName("getProductInBatches - Price greaterThan filter - Success")
  void getProductInBatches_PriceGreaterThan_Success() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    request.setFilters(Collections.singletonList(createFilter("price", "greaterThan", "100")));

    // Act & Assert
    assertDoesNotThrow(() -> productService.getProductInBatches(request));
  }

  /**
   * Purpose: Verify title 'contains' filter. Expected Result: Query executes without error.
   * Assertions: No exception thrown.
   */
  @Test
  @DisplayName("getProductInBatches - Title contains filter - Success")
  void getProductInBatches_TitleContains_Success() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    request.setFilters(Collections.singletonList(createFilter("title", "contains", "test")));

    // Act & Assert
    assertDoesNotThrow(() -> productService.getProductInBatches(request));
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify failure on invalid operator for type. Expected Result: BadRequestException is
   * thrown. Assertions: Exception message matches ErrorMessages constant.
   */
  @Test
  @DisplayName("getProductInBatches - Invalid operator for type - Throws BadRequest")
  void getProductInBatches_InvalidOperatorForType_ThrowsBadRequest() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    request.setFilters(Collections.singletonList(createFilter("title", "greaterThan", "val")));

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> productService.getProductInBatches(request));
    assertEquals(
        String.format(
            ErrorMessages.ProductErrorMessages.INVALID_OPERATOR_FOR_COLUMN_FORMAT,
            "greaterThan",
            "string",
            "title"),
        exception.getMessage());
  }

  /**
   * Purpose: Verify failure on invalid pagination (end <= start). Expected Result:
   * BadRequestException is thrown. Assertions: Exception message matches ErrorMessages constant.
   */
  @Test
  @DisplayName("getProductInBatches - End before Start - Throws BadRequest")
  void getProductInBatches_InvalidRange_ThrowsBadRequest() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    request.setStart(10);
    request.setEnd(5);

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.CommonErrorMessages.INVALID_PAGINATION,
        () -> productService.getProductInBatches(request));
  }

  /**
   * Purpose: Verify failure on malformed operator string. Expected Result: BadRequestException is
   * thrown. Assertions: Exception message matches ErrorMessages constant.
   */
  @Test
  @DisplayName("getProductInBatches - Malformed operator - Throws BadRequest")
  void getProductInBatches_MalformedOperator_ThrowsBadRequest() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    request.setFilters(Collections.singletonList(createFilter("title", "unknown_op", "val")));

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> productService.getProductInBatches(request));
    assertEquals(
        String.format(ErrorMessages.ProductErrorMessages.INVALID_OPERATOR_FORMAT, "unknown_op"),
        exception.getMessage());
  }

  /**
   * Purpose: Verify failure on invalid column name. Expected Result: BadRequestException is thrown.
   * Assertions: Exception message matches ErrorMessages constant.
   */
  @Test
  @DisplayName("getProductInBatches - Unknown column - Throws BadRequest")
  void getProductInBatches_UnknownColumn_ThrowsBadRequest() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    request.setFilters(Collections.singletonList(createFilter("nonExistent", "equals", "val")));

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> productService.getProductInBatches(request));
    assertEquals(
        String.format(ErrorMessages.ProductErrorMessages.INVALID_COLUMN_NAME_FORMAT, "nonExistent"),
        exception.getMessage());
  }

  /**
   * Purpose: Verify failure on zero page size. Expected Result: BadRequestException is thrown.
   * Assertions: Exception message matches ErrorMessages constant.
   */
  @Test
  @DisplayName("getProductInBatches - Zero page size - Throws BadRequest")
  void getProductInBatches_ZeroPageSize_ThrowsBadRequest() {
    // Arrange
    PaginationBaseRequestModel request = createValidPaginationRequest();
    request.setStart(5);
    request.setEnd(5);

    // Act & Assert
    assertThrowsBadRequest(
        ErrorMessages.CommonErrorMessages.INVALID_PAGINATION,
        () -> productService.getProductInBatches(request));
  }

  /*
   **********************************************************************************************
   * CONTROLLER AUTHORIZATION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify unauthorized access is blocked at controller level. Expected Result:
   * Unauthorized status is returned. Assertions: Response status is 401 UNAUTHORIZED.
   */
  @Test
  @DisplayName("getProductInBatches - Controller permission unauthorized - Success")
  void getProductInBatches_p01_controller_permission_unauthorized() {
    // Arrange
    ProductController controller = new ProductController(productServiceMock);
    PaginationBaseRequestModel request = createValidPaginationRequest();
    stubProductServiceGetProductInBatchesThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = controller.getProductInBatches(request);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    verify(productServiceMock).getProductInBatches(any(PaginationBaseRequestModel.class));
  }

  /**
   * Purpose: Verify controller delegation to service. Expected Result: OK status returned.
   * Assertions: Service method invoked and response is OK.
   */
  @Test
  @DisplayName("getProductInBatches - Controller delegation check - Success")
  void getProductInBatches_p03_ControllerDelegation_Success() {
    // Arrange
    ProductController controller = new ProductController(productServiceMock);
    PaginationBaseRequestModel request = createValidPaginationRequest();
    stubProductServiceGetProductInBatchesReturns(new PaginationBaseResponseModel<>());

    // Act
    ResponseEntity<?> response = controller.getProductInBatches(request);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(productServiceMock).getProductInBatches(any(PaginationBaseRequestModel.class));
  }

  private PaginationBaseRequestModel.FilterCondition createFilter(
      String col, String op, String val) {
    PaginationBaseRequestModel.FilterCondition f = new PaginationBaseRequestModel.FilterCondition();
    f.setColumn(col);
    f.setOperator(op);
    f.setValue(val);
    return f;
  }
}

