package com.example.SpringApi.ServiceTests.Product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.SpringApi.Controllers.ProductController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Models.RequestModels.ProductRequestModel;
import com.example.SpringApi.SuccessMessages;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Consolidated test class for ProductService.bulkAddProductsAsync. Fully compliant with Unit Test
 * Verification rules.
 */
@DisplayName("ProductService - BulkAddProductsAsync Tests")
class BulkAddProductsAsyncTest extends ProductServiceTestBase {

  // Total Tests: 10
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify async success. Expected Result: Products inserted and log entry created.
   * Assertions: UserLogService logDataWithContext invoked.
   */
  @Test
  @DisplayName("bulkAddProductsAsync - Valid input - Logs success")
  void bulkAddProductsAsync_s01_ValidInput_Success() {
    // Arrange
    List<ProductRequestModel> products = Collections.singletonList(testProductRequest);
    stubProductRepositorySave(testProduct);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
    stubUserLogServiceLogDataWithContext();

    // Act
    try (MockedConstruction<ImgbbHelper> imgbbMock =
        mockConstruction(
            ImgbbHelper.class, (mock, context) -> stubImgbbHelperUploadSuccess(mock))) {
      productService.bulkAddProductsAsync(products, DEFAULT_USER_ID, CREATED_USER, TEST_CLIENT_ID);

      // Assert
      verify(userLogService, atLeastOnce())
          .logDataWithContext(
              eq(DEFAULT_USER_ID),
              eq(CREATED_USER),
              eq(TEST_CLIENT_ID),
              contains(SuccessMessages.ProductsSuccessMessages.INSERT_PRODUCT),
              anyString());
    }
  }

  /**
   * Purpose: Verify multiple products handled in async. Expected Result: All products attempted and
   * saved. Assertions: Repository save invoked.
   */
  @Test
  @DisplayName("bulkAddProductsAsync - Multiple products - Success")
  void bulkAddProductsAsync_s02_MultipleProducts_Success() {
    // Arrange
    List<ProductRequestModel> products = Arrays.asList(testProductRequest, testProductRequest);
    stubProductRepositorySave(testProduct);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
    stubUserLogServiceLogDataWithContext();

    // Act
    try (MockedConstruction<ImgbbHelper> imgbbMock =
        mockConstruction(
            ImgbbHelper.class, (mock, context) -> stubImgbbHelperUploadSuccess(mock))) {
      productService.bulkAddProductsAsync(products, DEFAULT_USER_ID, CREATED_USER, TEST_CLIENT_ID);

      // Assert
      verify(productRepository, atLeastOnce()).save(any());
    }
  }

  /**
   * Purpose: Verify error log when image upload fails but product still inserted. Expected Result:
   * Processing continues with success log. Assertions: UserLogService called with success message.
   */
  @Test
  @DisplayName("bulkAddProductsAsync - Image upload fails - Logs success (Partial)")
  void bulkAddProductsAsync_s03_ImageFails_Success() {
    // Arrange
    List<ProductRequestModel> products = Collections.singletonList(testProductRequest);
    stubProductRepositorySave(testProduct);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
    stubUserLogServiceLogDataWithContext();

    // Act
    try (MockedConstruction<ImgbbHelper> imgbbMock =
        mockConstruction(
            ImgbbHelper.class, (mock, context) -> stubImgbbHelperUploadFailure(mock))) {
      productService.bulkAddProductsAsync(products, DEFAULT_USER_ID, CREATED_USER, TEST_CLIENT_ID);

      // Assert
      verify(userLogService, atLeastOnce())
          .logDataWithContext(
              eq(DEFAULT_USER_ID),
              eq(CREATED_USER),
              eq(TEST_CLIENT_ID),
              contains("Successfully inserted"),
              anyString());
    }
  }

  /**
   * Purpose: Verify behavior when userId is null. Expected Result: Processing completes without
   * throwing. Assertions: No exception thrown.
   */
  @Test
  @DisplayName("bulkAddProductsAsync - Null userId - Success")
  void bulkAddProductsAsync_s04_NullUserId_Success() {
    // Arrange
    List<ProductRequestModel> products = Collections.singletonList(testProductRequest);
    stubProductRepositorySave(testProduct);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);

    // Act & Assert
    assertDoesNotThrow(
        () -> productService.bulkAddProductsAsync(products, null, CREATED_USER, TEST_CLIENT_ID));
  }

  /**
   * Purpose: Verify behavior when clientId is null. Expected Result: Processing completes without
   * throwing. Assertions: No exception thrown.
   */
  @Test
  @DisplayName("bulkAddProductsAsync - Null clientId - Success")
  void bulkAddProductsAsync_s05_NullClientId_Success() {
    // Arrange
    List<ProductRequestModel> products = Collections.singletonList(testProductRequest);
    stubProductRepositorySave(testProduct);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);

    // Act & Assert
    assertDoesNotThrow(
        () -> productService.bulkAddProductsAsync(products, DEFAULT_USER_ID, CREATED_USER, null));
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify database error is handled and reported. Expected Result: Exception is handled
   * without throwing. Assertions: No exception thrown.
   */
  @Test
  @DisplayName("bulkAddProductsAsync - Database error - Exception handled")
  void bulkAddProductsAsync_DatabaseError_ExceptionHandled() {
    // Arrange
    List<ProductRequestModel> products = Collections.singletonList(testProductRequest);
    stubProductRepositorySaveThrows(
        new RuntimeException(ErrorMessages.CommonErrorMessages.DATABASE_ERROR));
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
    stubUserLogServiceLogDataWithContext();

    // Act
    try (MockedConstruction<ImgbbHelper> imgbbMock =
        mockConstruction(
            ImgbbHelper.class, (mock, context) -> stubImgbbHelperUploadSuccess(mock))) {
      // Act & Assert
      assertDoesNotThrow(
          () ->
              productService.bulkAddProductsAsync(
                  products, DEFAULT_USER_ID, CREATED_USER, TEST_CLIENT_ID));
    }
  }

  /**
   * Purpose: Verify nothing happens on empty list. Expected Result: No saves or logs executed.
   * Assertions: Repository save and logging not called.
   */
  @Test
  @DisplayName("bulkAddProductsAsync - Empty list - Handles error")
  void bulkAddProductsAsync_EmptyList_HandlesError() {
    // Arrange

    // Act
    productService.bulkAddProductsAsync(
        Collections.emptyList(), DEFAULT_USER_ID, CREATED_USER, TEST_CLIENT_ID);

    // Assert
    verify(productRepository, never()).save(any());
    verify(userLogService, never())
        .logDataWithContext(anyLong(), anyString(), anyLong(), anyString(), anyString());
  }

  /**
   * Purpose: Verify nothing happens on null list. Expected Result: No saves executed. Assertions:
   * Repository save not called.
   */
  @Test
  @DisplayName("bulkAddProductsAsync - Null list - Handles error")
  void bulkAddProductsAsync_NullList_HandlesError() {
    // Arrange

    // Act
    productService.bulkAddProductsAsync(null, DEFAULT_USER_ID, CREATED_USER, TEST_CLIENT_ID);

    // Assert
    verify(productRepository, never()).save(any());
  }

  /**
   * Purpose: Verify partial invalid product input is handled. Expected Result: Processing continues
   * without throwing. Assertions: No exception thrown.
   */
  @Test
  @DisplayName("bulkAddProductsAsync - Single product invalid - Continues")
  void bulkAddProductsAsync_PartialInvalid_Success() {
    // Arrange
    List<ProductRequestModel> products = Collections.singletonList(new ProductRequestModel());

    // Act & Assert
    assertDoesNotThrow(
        () ->
            productService.bulkAddProductsAsync(
                products, DEFAULT_USER_ID, CREATED_USER, TEST_CLIENT_ID));
  }

  /*
   **********************************************************************************************
   * PERMISSION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify unauthorized access is blocked at controller level. Expected Result:
   * Unauthorized status is returned. Assertions: Response status is 401 UNAUTHORIZED.
   */
  @Test
  @DisplayName("bulkAddProductsAsync - Controller permission unauthorized - Success")
  void bulkAddProductsAsync_controller_permission_unauthorized() {
    // Arrange
    ProductController controller = new ProductController(productServiceMock);
    stubProductServiceUserContext(DEFAULT_USER_ID, CREATED_USER, TEST_CLIENT_ID);
    stubProductServiceBulkAddProductsAsyncThrowsUnauthorized();
    List<ProductRequestModel> products =
        Arrays.asList(
            testProductRequest,
            testProductRequest,
            testProductRequest,
            testProductRequest,
            testProductRequest,
            testProductRequest);

    // Act
    ResponseEntity<?> response = controller.bulkAddProducts(products);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    verify(productServiceMock).bulkAddProductsAsync(anyList(), anyLong(), anyString(), anyLong());
  }
}

