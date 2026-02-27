package com.example.springapi.ServiceTests.ProductReview;

import static org.junit.jupiter.api.Assertions.*;

import com.example.springapi.ErrorMessages;
import com.example.springapi.controllers.ProductReviewController;
import com.example.springapi.exceptions.BadRequestException;
import com.example.springapi.models.databasemodels.ProductReview;
import com.example.springapi.models.responsemodels.PaginationBaseResponseModel;
import com.example.springapi.models.responsemodels.ProductReviewResponseModel;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Test class for ProductReviewService.getProductReviewsInBatchesGivenProductId method. */
@DisplayName("ProductReviewService - GetProductReviewsInBatchesGivenProductId Tests")
class GetProductReviewsInBatchesGivenProductIdTest extends ProductReviewServiceTestBase {

  // Total Tests: 14
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Verify empty results are returned properly.
   * Expected Result: Empty list and total count 0.
   * Assertions: size equals 0.
   */
  @Test
  @DisplayName("Get Product Reviews In Batches - Empty Results - Success")
  void getProductReviewsInBatchesGivenProductId_EmptyResults_Success() {
    // Arrange
    Page<ProductReview> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);
    stubProductReviewFilterQueryBuilderReturn(emptyPage);

    // Act
    PaginationBaseResponseModel<ProductReviewResponseModel> result =
        productReviewService.getProductReviewsInBatchesGivenProductId(
            testPaginationRequest, TEST_PRODUCT_ID);

    // Assert
    assertNotNull(result);
    assertEquals(0, result.getData().size());
    assertEquals(0L, result.getTotalDataCount());
  }

  /*
   * Purpose: Verify large page size.
   * Expected Result: Data returned without errors.
   * Assertions: data not null.
   */
  @Test
  @DisplayName("Get Product Reviews In Batches - Start 0 End 100 Large Page - Success")
  void getProductReviewsInBatchesGivenProductId_LargePage_Success() {
    // Arrange
    testPaginationRequest.setStart(0);
    testPaginationRequest.setEnd(100);
    Page<ProductReview> reviewPage =
        new PageImpl<>(Arrays.asList(testProductReview), PageRequest.of(0, 100), 1);
    stubProductReviewFilterQueryBuilderReturn(reviewPage);

    // Act
    PaginationBaseResponseModel<ProductReviewResponseModel> result =
        productReviewService.getProductReviewsInBatchesGivenProductId(
            testPaginationRequest, TEST_PRODUCT_ID);

    // Assert
    assertNotNull(result.getData());
  }

  /*
   * Purpose: Verify middle page pagination.
   * Expected Result: Total count preserved.
   * Assertions: total count equals 100.
   */
  @Test
  @DisplayName("Get Product Reviews In Batches - Start 50 End 60 Middle Page - Success")
  void getProductReviewsInBatchesGivenProductId_MiddlePage_Success() {
    // Arrange
    testPaginationRequest.setStart(50);
    testPaginationRequest.setEnd(60);
    Page<ProductReview> reviewPage =
        new PageImpl<>(Arrays.asList(testProductReview), PageRequest.of(0, 10), 100);
    stubProductReviewFilterQueryBuilderReturn(reviewPage);

    // Act
    PaginationBaseResponseModel<ProductReviewResponseModel> result =
        productReviewService.getProductReviewsInBatchesGivenProductId(
            testPaginationRequest, TEST_PRODUCT_ID);

    // Assert
    assertEquals(100L, result.getTotalDataCount());
  }

  /*
   * Purpose: Verify multiple results.
   * Expected Result: Multiple reviews returned.
   * Assertions: size equals 3.
   */
  @Test
  @DisplayName("Get Product Reviews In Batches - Multiple Results - Success")
  void getProductReviewsInBatchesGivenProductId_MultipleResults_Success() {
    // Arrange
    ProductReview review2 = new ProductReview();
    review2.setReviewId(2L);
    ProductReview review3 = new ProductReview();
    review3.setReviewId(3L);
    Page<ProductReview> reviewPage =
        new PageImpl<>(
            Arrays.asList(testProductReview, review2, review3), PageRequest.of(0, 10), 3);
    stubProductReviewFilterQueryBuilderReturn(reviewPage);

    // Act
    PaginationBaseResponseModel<ProductReviewResponseModel> result =
        productReviewService.getProductReviewsInBatchesGivenProductId(
            testPaginationRequest, TEST_PRODUCT_ID);

    // Assert
    assertEquals(3, result.getData().size());
  }

  /*
   * Purpose: Verify large product ID returns results.
   * Expected Result: Data returned.
   * Assertions: data not null.
   */
  @Test
  @DisplayName("Get Product Reviews In Batches - Product ID Large Value - Success")
  void getProductReviewsInBatchesGivenProductId_ProductIdLarge_Success() {
    // Arrange
    Page<ProductReview> reviewPage =
        new PageImpl<>(Arrays.asList(testProductReview), PageRequest.of(0, 10), 1);
    stubProductReviewFilterQueryBuilderReturn(reviewPage);

    // Act
    PaginationBaseResponseModel<ProductReviewResponseModel> result =
        productReviewService.getProductReviewsInBatchesGivenProductId(
            testPaginationRequest, 999999L);

    // Assert
    assertNotNull(result.getData());
  }

  /*
   * Purpose: Verify product ID = 1 returns results.
   * Expected Result: One review.
   * Assertions: size equals 1.
   */
  @Test
  @DisplayName("Get Product Reviews In Batches - Product ID 1 - Success")
  void getProductReviewsInBatchesGivenProductId_ProductIdOne_Success() {
    // Arrange
    Page<ProductReview> reviewPage =
        new PageImpl<>(Arrays.asList(testProductReview), PageRequest.of(0, 10), 1);
    stubProductReviewFilterQueryBuilderReturn(reviewPage);

    // Act
    PaginationBaseResponseModel<ProductReviewResponseModel> result =
        productReviewService.getProductReviewsInBatchesGivenProductId(testPaginationRequest, 1L);

    // Assert
    assertEquals(1, result.getData().size());
  }

  /*
   * Purpose: Verify single item page.
   * Expected Result: One review.
   * Assertions: size equals 1.
   */
  @Test
  @DisplayName("Get Product Reviews In Batches - Start 0 End 1 Single Item - Success")
  void getProductReviewsInBatchesGivenProductId_SingleItem_Success() {
    // Arrange
    testPaginationRequest.setStart(0);
    testPaginationRequest.setEnd(1);
    Page<ProductReview> reviewPage =
        new PageImpl<>(Arrays.asList(testProductReview), PageRequest.of(0, 1), 1);
    stubProductReviewFilterQueryBuilderReturn(reviewPage);

    // Act
    PaginationBaseResponseModel<ProductReviewResponseModel> result =
        productReviewService.getProductReviewsInBatchesGivenProductId(
            testPaginationRequest, TEST_PRODUCT_ID);

    // Assert
    assertEquals(1, result.getData().size());
  }

  /*
   * Purpose: Verify success response with single review.
   * Expected Result: One review returned.
   * Assertions: data size equals 1.
   */
  @Test
  @DisplayName("Get Product Reviews In Batches - Success")
  void getProductReviewsInBatchesGivenProductId_Success_Success() {
    // Arrange
    Page<ProductReview> reviewPage =
        new PageImpl<>(Arrays.asList(testProductReview), PageRequest.of(0, 10), 1);
    stubProductReviewFilterQueryBuilderReturn(reviewPage);

    // Act
    PaginationBaseResponseModel<ProductReviewResponseModel> result =
        productReviewService.getProductReviewsInBatchesGivenProductId(
            testPaginationRequest, TEST_PRODUCT_ID);

    // Assert
    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().size());
    assertEquals(1L, result.getTotalDataCount());
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Verify equal start and end throws BadRequestException.
   * Expected Result: Exception thrown.
   * Assertions: message matches invalid pagination.
   */
  @Test
  @DisplayName(
      "Get Product Reviews In Batches - Start 5 End 5 Equal Start End - Throws BadRequestException")
  void getProductReviewsInBatchesGivenProductId_EqualStartEnd_ThrowsBadRequestException() {
    // Arrange
    testPaginationRequest.setStart(5);
    testPaginationRequest.setEnd(5);

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () ->
                productReviewService.getProductReviewsInBatchesGivenProductId(
                    testPaginationRequest, TEST_PRODUCT_ID));
    assertEquals(ErrorMessages.CommonErrorMessages.INVALID_PAGINATION, exception.getMessage());
  }

  /*
   * Purpose: Verify invalid pagination throws BadRequestException.
   * Expected Result: Exception thrown.
   * Assertions: message matches invalid pagination.
   */
  @Test
  @DisplayName("Get Product Reviews In Batches - Invalid Pagination - Throws BadRequestException")
  void getProductReviewsInBatchesGivenProductId_InvalidPagination_ThrowsBadRequestException() {
    // Arrange
    testPaginationRequest.setStart(10);
    testPaginationRequest.setEnd(5);

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () ->
                productReviewService.getProductReviewsInBatchesGivenProductId(
                    testPaginationRequest, TEST_PRODUCT_ID));
    assertEquals(ErrorMessages.CommonErrorMessages.INVALID_PAGINATION, exception.getMessage());
  }

  /*
   * Purpose: Verify reversed pagination throws BadRequestException.
   * Expected Result: Exception thrown.
   * Assertions: message matches invalid pagination.
   */
  @Test
  @DisplayName(
      "Get Product Reviews In Batches - Start 100 End 50 Reversed - Throws BadRequestException")
  void getProductReviewsInBatchesGivenProductId_ReversedPagination_ThrowsBadRequestException() {
    // Arrange
    testPaginationRequest.setStart(100);
    testPaginationRequest.setEnd(50);

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () ->
                productReviewService.getProductReviewsInBatchesGivenProductId(
                    testPaginationRequest, TEST_PRODUCT_ID));
    assertEquals(ErrorMessages.CommonErrorMessages.INVALID_PAGINATION, exception.getMessage());
  }

  /*
   * Purpose: Verify zero page size throws BadRequestException.
   * Expected Result: Exception thrown.
   * Assertions: message matches invalid pagination.
   */
  @Test
  @DisplayName(
      "Get Product Reviews In Batches - Start 0 End 0 Zero Page Size - Throws BadRequestException")
  void getProductReviewsInBatchesGivenProductId_ZeroPageSize_ThrowsBadRequestException() {
    // Arrange
    testPaginationRequest.setStart(0);
    testPaginationRequest.setEnd(0);

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () ->
                productReviewService.getProductReviewsInBatchesGivenProductId(
                    testPaginationRequest, TEST_PRODUCT_ID));
    assertEquals(ErrorMessages.CommonErrorMessages.INVALID_PAGINATION, exception.getMessage());
  }

  /*
   **********************************************************************************************
   * PERMISSION TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Verify unauthorized access is handled at controller level.
   * Expected Result: Unauthorized status is returned.
   * Assertions: Response status is 401.
   */
  @Test
  @DisplayName("getProductReviewsInBatchesGivenProductId - Controller Permission - Unauthorized")
  void getProductReviewsInBatchesGivenProductId_controller_permission_unauthorized() {
    // Arrange
    ProductReviewController controller = new ProductReviewController(productReviewServiceMock);
    stubProductReviewServiceGetProductReviewsInBatchesGivenProductIdThrowsUnauthorized();

    // Act
    ResponseEntity<?> response =
        controller.getProductReviewsGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }
}
