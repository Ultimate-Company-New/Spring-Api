package springapi.ServiceTests.Product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import springapi.ErrorMessages;
import springapi.controllers.ProductController;

/**
 * Consolidated test class for ProductService.toggleReturnProduct. Fully compliant with Unit Test
 * Verification rules.
 */
@DisplayName("ProductService - ToggleReturnProduct Tests")
class ToggleReturnProductTest extends ProductServiceTestBase {

  // Total Tests: 8
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify toggle from 30 to 0 (disable returns). Expected Result: Return window set to 0.
   * Assertions: returnWindowDays is 0 and repository save called.
   */
  @Test
  @DisplayName("toggleReturnProduct - From 30 to 0 - Success")
  void toggleReturnProduct_Disable_Success() {
    // Arrange
    testProduct.setReturnWindowDays(30);
    stubProductRepositoryFindByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID, testProduct);
    stubProductRepositorySave(testProduct);

    // Act
    assertDoesNotThrow(() -> productService.toggleReturnProduct(TEST_PRODUCT_ID));

    // Assert
    assertEquals(0, testProduct.getReturnWindowDays());
    verify(productRepository).save(testProduct);
    verify(userLogService).logData(eq(1L), anyString(), anyString());
  }

  /**
   * Purpose: Verify toggle from null to 30. Expected Result: Return window set to 30. Assertions:
   * returnWindowDays is 30.
   */
  @Test
  @DisplayName("toggleReturnProduct - From null to 30 - Success")
  void toggleReturnProduct_EnableFromNull_Success() {
    // Arrange
    testProduct.setReturnWindowDays(null);
    stubProductRepositoryFindByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID, testProduct);
    stubProductRepositorySave(testProduct);

    // Act
    assertDoesNotThrow(() -> productService.toggleReturnProduct(TEST_PRODUCT_ID));

    // Assert
    assertEquals(30, testProduct.getReturnWindowDays());
  }

  /**
   * Purpose: Verify toggle from 0 to 30 (enable returns). Expected Result: Return window set to 30.
   * Assertions: returnWindowDays is 30 and repository save called.
   */
  @Test
  @DisplayName("toggleReturnProduct - From 0 to 30 - Success")
  void toggleReturnProduct_EnableFromZero_Success() {
    // Arrange
    testProduct.setReturnWindowDays(0);
    stubProductRepositoryFindByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID, testProduct);
    stubProductRepositorySave(testProduct);

    // Act
    assertDoesNotThrow(() -> productService.toggleReturnProduct(TEST_PRODUCT_ID));

    // Assert
    assertEquals(30, testProduct.getReturnWindowDays());
    verify(productRepository).save(testProduct);
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify failure with ID zero. Expected Result: NotFoundException is thrown. Assertions:
   * Exception message matches ErrorMessages constant.
   */
  @Test
  @DisplayName("toggleReturnProduct - ID zero - Throws NotFound")
  void toggleReturnProduct_IdZero_ThrowsNotFound() {
    // Arrange
    long id = 0L;
    stubProductRepositoryFindByIdWithRelatedEntities(id, TEST_CLIENT_ID, null);

    // Act & Assert
    assertThrowsNotFound(
        String.format(ErrorMessages.ProductErrorMessages.ER013, id),
        () -> productService.toggleReturnProduct(id));
  }

  /**
   * Purpose: Verify failure when product not found. Expected Result: NotFoundException is thrown.
   * Assertions: Exception message matches ErrorMessages constant.
   */
  @Test
  @DisplayName("toggleReturnProduct - Product not found - Throws NotFound")
  void toggleReturnProduct_NotFound_ThrowsNotFound() {
    // Arrange
    stubProductRepositoryFindByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID, null);

    // Act & Assert
    assertThrowsNotFound(
        String.format(ErrorMessages.ProductErrorMessages.ER013, TEST_PRODUCT_ID),
        () -> productService.toggleReturnProduct(TEST_PRODUCT_ID));
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
  @DisplayName("toggleReturnProduct - Controller permission unauthorized - Success")
  void toggleReturnProduct_p01_controller_permission_unauthorized() {
    // Arrange
    ProductController controller = new ProductController(productServiceMock);
    stubProductServiceToggleReturnProductThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = controller.toggleReturnProduct(TEST_PRODUCT_ID);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    verify(productServiceMock).toggleReturnProduct(anyLong());
  }

  /**
   * Purpose: Verify controller delegation to service. Expected Result: OK status returned.
   * Assertions: Service method invoked and response is OK.
   */
  @Test
  @DisplayName("toggleReturnProduct - Controller delegation check - Success")
  void toggleReturnProduct_p03_ControllerDelegation_Success() {
    // Arrange
    ProductController controller = new ProductController(productServiceMock);
    stubProductServiceToggleReturnProductDoNothing();

    // Act
    ResponseEntity<?> response = controller.toggleReturnProduct(TEST_PRODUCT_ID);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(productServiceMock).toggleReturnProduct(TEST_PRODUCT_ID);
  }
}
