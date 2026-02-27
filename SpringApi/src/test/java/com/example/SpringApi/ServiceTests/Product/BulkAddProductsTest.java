package com.example.SpringApi.ServiceTests.Product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.SpringApi.Controllers.ProductController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Models.RequestModels.ProductRequestModel;
import com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Consolidated test class for ProductService.bulkAddProducts. Fully compliant with Unit Test
 * Verification rules.
 */
@DisplayName("ProductService - BulkAddProducts Tests")
class BulkAddProductsTest extends ProductServiceTestBase {

  // Total Tests: 7
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify success when all products are valid. Expected Result: All products inserted.
   * Assertions: Success count is 1 and failure count is 0.
   */
  @Test
  @DisplayName("bulkAddProducts - All products valid - Success")
  void bulkAddProducts_AllValid_Success() {
    // Arrange
    List<ProductRequestModel> requests = Collections.singletonList(testProductRequest);
    stubProductCategoryRepositoryFindById(TEST_CATEGORY_ID, testCategory);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
    stubClientRepositoryFindById(TEST_CLIENT_ID, testClient);
    stubProductRepositorySave(testProduct);

    // Act
    try (MockedConstruction<ImgbbHelper> imgbbMock =
        mockConstruction(
            ImgbbHelper.class, (mock, context) -> stubImgbbHelperUploadSuccess(mock))) {
      BulkInsertResponseModel<Long> result = productService.bulkAddProducts(requests);

      // Assert
      assertEquals(1, result.getSuccessCount());
      assertEquals(0, result.getFailureCount());
    }
  }

  /**
   * Purpose: Verify partial success when one product is invalid. Expected Result: One success and
   * one failure recorded. Assertions: Total requested equals 2 with 1 success and 1 failure.
   */
  @Test
  @DisplayName("bulkAddProducts - Partial success - One failure")
  void bulkAddProducts_PartialSuccess_Success() {
    // Arrange
    ProductRequestModel invalid = new ProductRequestModel(); // Missing title
    invalid.setClientId(TEST_CLIENT_ID);
    List<ProductRequestModel> requests = new ArrayList<>();
    requests.add(testProductRequest);
    requests.add(invalid);

    stubProductCategoryRepositoryFindById(TEST_CATEGORY_ID, testCategory);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
    stubClientRepositoryFindById(TEST_CLIENT_ID, testClient);
    stubProductRepositorySave(testProduct);

    // Act
    try (MockedConstruction<ImgbbHelper> imgbbMock =
        mockConstruction(
            ImgbbHelper.class, (mock, context) -> stubImgbbHelperUploadSuccess(mock))) {
      BulkInsertResponseModel<Long> result = productService.bulkAddProducts(requests);

      // Assert
      assertEquals(2, result.getTotalRequested());
      assertEquals(1, result.getSuccessCount());
      assertEquals(1, result.getFailureCount());
    }
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify failure on database error. Expected Result: Failure recorded. Assertions:
   * Success count is 0 and failure count is 1.
   */
  @Test
  @DisplayName("bulkAddProducts - Database error - Failure recorded")
  void bulkAddProducts_DatabaseError_Success() {
    // Arrange
    List<ProductRequestModel> requests = Collections.singletonList(testProductRequest);
    stubProductCategoryRepositoryFindById(TEST_CATEGORY_ID, testCategory);
    stubProductRepositorySaveThrows(
        new RuntimeException(ErrorMessages.CommonErrorMessages.DATABASE_ERROR));

    // Act
    BulkInsertResponseModel<Long> result = productService.bulkAddProducts(requests);

    // Assert
    assertEquals(0, result.getSuccessCount());
    assertEquals(1, result.getFailureCount());
  }

  /**
   * Purpose: Verify failure when list is empty. Expected Result: BadRequestException is thrown.
   * Assertions: Exception message matches ErrorMessages constant.
   */
  @Test
  @DisplayName("bulkAddProducts - Empty list - Throws BadRequest")
  void bulkAddProducts_EmptyList_ThrowsBadRequest() {
    // Arrange

    // Act & Assert
    assertThrowsBadRequest(
        String.format(ErrorMessages.CommonErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY, "Product"),
        () -> productService.bulkAddProducts(new ArrayList<>()));
  }

  /**
   * Purpose: Verify failure when list is null. Expected Result: BadRequestException is thrown.
   * Assertions: Exception message matches ErrorMessages constant.
   */
  @Test
  @DisplayName("bulkAddProducts - Null list - Throws BadRequest")
  void bulkAddProducts_NullList_ThrowsBadRequest() {
    // Arrange

    // Act & Assert
    assertThrowsBadRequest(
        String.format(ErrorMessages.CommonErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY, "Product"),
        () -> productService.bulkAddProducts(null));
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
  @DisplayName("bulkAddProducts - Controller permission unauthorized - Success")
  void bulkAddProducts_controller_permission_unauthorized() {
    // Arrange
    ProductController controller = new ProductController(productServiceMock);
    List<ProductRequestModel> requests = Collections.singletonList(testProductRequest);
    stubProductServiceBulkAddProductsThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = controller.bulkAddProducts(requests);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    verify(productServiceMock).bulkAddProducts(anyList());
  }
}

