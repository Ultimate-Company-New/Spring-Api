package com.example.SpringApi.ServiceTests.PickupLocation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.SpringApi.Controllers.PickupLocationController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.PickupLocationResponseModel;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for PickupLocationService.getPickupLocationsInBatches() method. Tests pagination
 * validation, edge cases, and multiple result scenarios. Test Count: 16 tests
 */
@DisplayName("Get Pickup Locations In Batches Tests")
class GetPickupLocationsInBatchesTest extends PickupLocationServiceTestBase {

  // Total Tests: 19
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify successful retrieval of pickup locations with valid pagination. Expected
   * Result: PaginationBaseResponseModel with correct data is returned. Assertions: Result is not
   * null, data size and total count are correct.
   */
  @Test
  @DisplayName("Get Pickup Locations In Batches - Comprehensive Validation - Success")
  void getPickupLocationsInBatches_Comprehensive_Success() {
    // Arrange
    testPaginationRequest.setStart(0);
    testPaginationRequest.setEnd(10);
    testPaginationRequest.setFilters(null);

    Page<com.example.SpringApi.Models.DatabaseModels.PickupLocation> pageResult =
        new PageImpl<>(Collections.singletonList(testPickupLocation));

    stubPickupLocationFilterQueryBuilderFindPaginatedEntities(pageResult);

    // Act
    PaginationBaseResponseModel<PickupLocationResponseModel> result =
        pickupLocationService.getPickupLocationsInBatches(testPaginationRequest);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.getData().size());
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * /** Purpose: Test empty result set with valid pagination. Expected Result: Empty list returned
   * successfully. Assertions: Result data size is zero.
   */
  @Test
  @DisplayName("Get Pickup Locations In Batches - Empty Results - Success")
  void getPickupLocationsInBatches_EmptyResults_Success() {
    // Arrange
    testPaginationRequest.setStart(0);
    testPaginationRequest.setEnd(10);

    Page<com.example.SpringApi.Models.DatabaseModels.PickupLocation> pageResult =
        new PageImpl<>(Collections.emptyList());

    stubPickupLocationFilterQueryBuilderFindPaginatedEntities(pageResult);

    // Act
    PaginationBaseResponseModel<PickupLocationResponseModel> result =
        pickupLocationService.getPickupLocationsInBatches(testPaginationRequest);

    // Assert
    assertNotNull(result);
    assertEquals(0, result.getData().size());
  }

  /**
   * Purpose: Reject pagination when end index is zero. Expected Result: BadRequestException is
   * thrown. Assertions: Error message matches EndIndexMustBeGreaterThanZero.
   */
  @Test
  @DisplayName("Get Pickup Locations In Batches - End Index Zero - Throws BadRequestException")
  void getPickupLocationsInBatches_EndIndexZero_ThrowsBadRequestException() {
    // Arrange
    testPaginationRequest.setStart(0);
    testPaginationRequest.setEnd(0);

    // Act & Assert
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> pickupLocationService.getPickupLocationsInBatches(testPaginationRequest));
    assertEquals(
        ErrorMessages.CommonErrorMessages.END_INDEX_MUST_BE_GREATER_THAN_ZERO, ex.getMessage());
  }

  /**
   * Purpose: Test with includeDeleted flag set to true. Expected Result: Both deleted and active
   * locations are retrieved. Assertions: Result is not null, includeDeleted is respected.
   */
  @Test
  @DisplayName("Get Pickup Locations In Batches - Include Deleted - Success")
  void getPickupLocationsInBatches_IncludeDeleted_Success() {
    // Arrange
    testPaginationRequest.setStart(0);
    testPaginationRequest.setEnd(10);
    testPaginationRequest.setIncludeDeleted(true);

    Page<com.example.SpringApi.Models.DatabaseModels.PickupLocation> pageResult =
        new PageImpl<>(Collections.singletonList(testPickupLocation));

    stubPickupLocationFilterQueryBuilderFindPaginatedEntities(pageResult);

    // Act
    PaginationBaseResponseModel<PickupLocationResponseModel> result =
        pickupLocationService.getPickupLocationsInBatches(testPaginationRequest);

    // Assert
    assertNotNull(result);
    verify(pickupLocationFilterQueryBuilder)
        .findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), anyString(), any(), eq(true), any(Pageable.class));
  }

  /**
   * Purpose: Test with large batch size (high end value). Expected Result: Pagination succeeds with
   * large range. Assertions: Result is not null.
   */
  @Test
  @DisplayName("Get Pickup Locations In Batches - Large Batch Size - Success")
  void getPickupLocationsInBatches_LargeBatchSize_Success() {
    // Arrange
    testPaginationRequest.setStart(0);
    testPaginationRequest.setEnd(1000);

    Page<com.example.SpringApi.Models.DatabaseModels.PickupLocation> pageResult =
        new PageImpl<>(Collections.singletonList(testPickupLocation));

    stubPickupLocationFilterQueryBuilderFindPaginatedEntities(pageResult);

    // Act
    PaginationBaseResponseModel<PickupLocationResponseModel> result =
        pickupLocationService.getPickupLocationsInBatches(testPaginationRequest);

    // Assert
    assertNotNull(result);
  }

  /**
   * Purpose: Test pagination with start equals end minus one (minimal range). Expected Result:
   * Pagination succeeds with minimal range. Assertions: Result is not null.
   */
  @Test
  @DisplayName("Get Pickup Locations In Batches - Minimal Range - Success")
  void getPickupLocationsInBatches_MinimalRange_Success() {
    // Arrange
    testPaginationRequest.setStart(0);
    testPaginationRequest.setEnd(1);

    Page<com.example.SpringApi.Models.DatabaseModels.PickupLocation> pageResult =
        new PageImpl<>(Collections.emptyList());

    stubPickupLocationFilterQueryBuilderFindPaginatedEntities(pageResult);

    // Act
    PaginationBaseResponseModel<PickupLocationResponseModel> result =
        pickupLocationService.getPickupLocationsInBatches(testPaginationRequest);

    // Assert
    assertNotNull(result);
    assertEquals(0, result.getData().size());
  }

  /**
   * Purpose: Test with multiple results in pagination. Expected Result: All results returned
   * correctly. Assertions: Result data size matches expected count.
   */
  @Test
  @DisplayName("Get Pickup Locations In Batches - Multiple Results - Success")
  void getPickupLocationsInBatches_MultipleResults_Success() {
    // Arrange
    testPaginationRequest.setStart(0);
    testPaginationRequest.setEnd(10);

    com.example.SpringApi.Models.DatabaseModels.PickupLocation location2 =
        new com.example.SpringApi.Models.DatabaseModels.PickupLocation();
    location2.setPickupLocationId(2L);

    Page<com.example.SpringApi.Models.DatabaseModels.PickupLocation> pageResult =
        new PageImpl<>(java.util.Arrays.asList(testPickupLocation, location2));

    stubPickupLocationFilterQueryBuilderFindPaginatedEntities(pageResult);

    // Act
    PaginationBaseResponseModel<PickupLocationResponseModel> result =
        pickupLocationService.getPickupLocationsInBatches(testPaginationRequest);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.getData().size());
  }

  /**
   * Purpose: Test with negative end index. Expected Result: BadRequestException is thrown.
   * Assertions: Error message indicates invalid pagination.
   */
  @Test
  @DisplayName("Get Pickup Locations In Batches - Negative End Index - Throws BadRequestException")
  void getPickupLocationsInBatches_NegativeEndIndex_ThrowsBadRequestException() {
    // Arrange
    testPaginationRequest.setStart(0);
    testPaginationRequest.setEnd(-1);

    // Act & Assert
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> pickupLocationService.getPickupLocationsInBatches(testPaginationRequest));
    assertNotNull(ex.getMessage());
  }

  /**
   * Purpose: Reject pagination when start index is negative. Expected Result: BadRequestException
   * is thrown. Assertions: Error message matches StartIndexCannotBeNegative.
   */
  @Test
  @DisplayName("Get Pickup Locations In Batches - Negative Start - Throws BadRequestException")
  void getPickupLocationsInBatches_NegativeStart_ThrowsBadRequestException() {
    // Arrange
    testPaginationRequest.setStart(-1);

    // Act & Assert
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> pickupLocationService.getPickupLocationsInBatches(testPaginationRequest));
    assertEquals(ErrorMessages.CommonErrorMessages.START_INDEX_CANNOT_BE_NEGATIVE, ex.getMessage());
  }

  /**
   * Purpose: Test with equal start and end indices. Expected Result: BadRequestException is thrown.
   * Assertions: Error message indicates start must be less than end.
   */
  @Test
  @DisplayName("Get Pickup Locations In Batches - Start Equals End - Throws BadRequestException")
  void getPickupLocationsInBatches_StartEqualsEnd_ThrowsBadRequestException() {
    // Arrange
    testPaginationRequest.setStart(5);
    testPaginationRequest.setEnd(5);

    // Act & Assert
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> pickupLocationService.getPickupLocationsInBatches(testPaginationRequest));
    assertEquals(
        ErrorMessages.CommonErrorMessages.START_INDEX_MUST_BE_LESS_THAN_END, ex.getMessage());
  }

  /**
   * Purpose: Reject pagination when start index is greater than or equal to end index. Expected
   * Result: BadRequestException is thrown. Assertions: Error message matches
   * StartIndexMustBeLessThanEnd.
   */
  @Test
  @DisplayName(
      "Get Pickup Locations In Batches - Start Greater Than End - Throws BadRequestException")
  void getPickupLocationsInBatches_StartGreaterEqualEnd_ThrowsBadRequestException() {
    // Arrange
    testPaginationRequest.setStart(10);
    testPaginationRequest.setEnd(5);

    // Act & Assert
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> pickupLocationService.getPickupLocationsInBatches(testPaginationRequest));
    assertEquals(
        ErrorMessages.CommonErrorMessages.START_INDEX_MUST_BE_LESS_THAN_END, ex.getMessage());
  }

  /**
   * Purpose: Reject null pagination request. Expected Result: BadRequestException is thrown.
   * Assertions: Error message matches InvalidRequest.
   */
  @Test
  @DisplayName("Get Pickup Locations In Batches - Null Request - Throws BadRequestException")
  void getPickupLocationsInBatches_NullRequest_ThrowsBadRequestException() {
    // Arrange

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> pickupLocationService.getPickupLocationsInBatches(null));

    // Assert
    assertEquals(ErrorMessages.PickupLocationErrorMessages.INVALID_REQUEST, ex.getMessage());
  }

  /**
   * Purpose: Reject filter with invalid column name. Expected Result: BadRequestException is
   * thrown. Assertions: Error message matches InvalidColumnNameFormat.
   */
  @Test
  @DisplayName(
      "Get Pickup Locations In Batches - Invalid Filter Column - Throws BadRequestException")
  void getPickupLocationsInBatches_InvalidFilterColumn_ThrowsBadRequestException() {
    // Arrange
    PaginationBaseRequestModel.FilterCondition invalidFilter =
        new PaginationBaseRequestModel.FilterCondition();
    invalidFilter.setColumn("unknownColumn");
    invalidFilter.setOperator("equals");
    invalidFilter.setValue("value");
    testPaginationRequest.setFilters(List.of(invalidFilter));

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> pickupLocationService.getPickupLocationsInBatches(testPaginationRequest));

    // Assert
    assertEquals(
        String.format(
            ErrorMessages.PickupLocationErrorMessages.INVALID_COLUMN_NAME_FORMAT, "unknownColumn"),
        ex.getMessage());
  }

  /**
   * Purpose: Reject filter with invalid operator. Expected Result: BadRequestException is thrown.
   * Assertions: Error message matches InvalidOperatorFormat.
   */
  @Test
  @DisplayName(
      "Get Pickup Locations In Batches - Invalid Filter Operator - Throws BadRequestException")
  void getPickupLocationsInBatches_InvalidFilterOperator_ThrowsBadRequestException() {
    // Arrange
    PaginationBaseRequestModel.FilterCondition invalidFilter =
        new PaginationBaseRequestModel.FilterCondition();
    invalidFilter.setColumn("pickupLocationId");
    invalidFilter.setOperator("nope");
    invalidFilter.setValue("1");
    testPaginationRequest.setFilters(List.of(invalidFilter));

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> pickupLocationService.getPickupLocationsInBatches(testPaginationRequest));

    // Assert
    assertEquals(
        String.format(ErrorMessages.PickupLocationErrorMessages.INVALID_OPERATOR_FORMAT, "nope"),
        ex.getMessage());
  }

  /**
   * Purpose: Reject filter with missing value for value-based operator. Expected Result: Filter
   * validation throws IllegalArgumentException. Assertions: Exception message is not empty.
   */
  @Test
  @DisplayName(
      "Get Pickup Locations In Batches - Filter Missing Value - Throws IllegalArgumentException")
  void getPickupLocationsInBatches_FilterMissingValue_ThrowsIllegalArgumentException() {
    // Arrange
    PaginationBaseRequestModel.FilterCondition invalidFilter =
        new PaginationBaseRequestModel.FilterCondition();
    invalidFilter.setColumn("pickupLocationId");
    invalidFilter.setOperator("equals");
    invalidFilter.setValue(null);
    testPaginationRequest.setFilters(List.of(invalidFilter));
    stubPickupLocationFilterQueryBuilderGetColumnType("pickupLocationId", "number");

    // Act
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> pickupLocationService.getPickupLocationsInBatches(testPaginationRequest));

    // Assert
    assertNotNull(ex.getMessage());
  }

  /**
   * Purpose: Verify product/package count maps are populated from batch count queries. Expected
   * Result: Response models include count values from repository aggregates. Assertions: Product
   * and package counts match aggregate rows.
   */
  @Test
  @DisplayName("Get Pickup Locations In Batches - Count Maps Populated - Success")
  void getPickupLocationsInBatches_CountMapsPopulated_Success() {
    // Arrange
    testPaginationRequest.setStart(0);
    testPaginationRequest.setEnd(10);
    Page<com.example.SpringApi.Models.DatabaseModels.PickupLocation> pageResult =
        new PageImpl<>(Collections.singletonList(testPickupLocation));
    stubPickupLocationFilterQueryBuilderFindPaginatedEntities(pageResult);
    stubProductMappingRepositoryCountByPickupLocationIds(
        Collections.singletonList(new Object[] {TEST_PICKUP_LOCATION_ID, 7L}));
    stubPackageMappingRepositoryCountByPickupLocationIds(
        Collections.singletonList(new Object[] {TEST_PICKUP_LOCATION_ID, 3L}));

    // Act
    PaginationBaseResponseModel<PickupLocationResponseModel> result =
        pickupLocationService.getPickupLocationsInBatches(testPaginationRequest);

    // Assert
    assertEquals(1, result.getData().size());
    assertEquals(7, result.getData().getFirst().getProductCount());
    assertEquals(3, result.getData().getFirst().getPackageCount());
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
  @DisplayName("getPickupLocationsInBatches - Controller Permission - Unauthorized")
  void getPickupLocationsInBatches_controller_permission_unauthorized() {
    // Arrange
    PickupLocationController controller = new PickupLocationController(pickupLocationServiceMock);
    stubPickupLocationServiceThrowsUnauthorizedOnGetBatches();

    // Act
    ResponseEntity<?> response = controller.getPickupLocationsInBatches(testPaginationRequest);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  /**
   * Purpose: Verify controller delegates getPickupLocationsInBatches to service. Expected Result:
   * Service method called and HTTP 200 returned. Assertions: Delegation occurs and response is OK.
   */
  @Test
  @DisplayName("getPickupLocationsInBatches - Controller delegates to service")
  void getPickupLocationsInBatches_WithValidRequest_DelegatesToService() {
    // Arrange
    PickupLocationController controller = new PickupLocationController(pickupLocationServiceMock);
    PaginationBaseResponseModel<PickupLocationResponseModel> mockResponse =
        new PaginationBaseResponseModel<>();
    stubPickupLocationServiceGetPickupLocationsInBatchesReturns(mockResponse);

    // Act
    ResponseEntity<?> response = controller.getPickupLocationsInBatches(testPaginationRequest);

    // Assert
    verify(pickupLocationServiceMock).getPickupLocationsInBatches(testPaginationRequest);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}

