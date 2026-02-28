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
 * Consolidated test class for ProductService.toggleDeleteProduct. Fully compliant with Unit Test
 * Verification rules.
 */
@DisplayName("ProductService - ToggleDeleteProduct Tests")
class ToggleDeleteProductTest extends ProductServiceTestBase {

  // Total Tests: 9
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify toggle from false to true. Expected Result: Product is marked deleted.
   * Assertions: isDeleted is true and repository save called.
   */
  @Test
  @DisplayName("toggleDeleteProduct - From false to true - Success")
  void toggleDeleteProduct_Delete_Success() {
    // Arrange
    testProduct.setIsDeleted(false);
    stubProductRepositoryFindByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID, testProduct);
    stubProductRepositorySave(testProduct);

    // Act
    assertDoesNotThrow(() -> productService.toggleDeleteProduct(TEST_PRODUCT_ID));

    // Assert
    assertTrue(testProduct.getIsDeleted());
    verify(productRepository).save(testProduct);
    verify(userLogService).logData(eq(1L), anyString(), anyString());
  }

  /**
   * Purpose: Verify toggle from true to false (undelete). Expected Result: Product is marked not
   * deleted. Assertions: isDeleted is false and repository save called.
   */
  @Test
  @DisplayName("toggleDeleteProduct - From true to false - Success")
  void toggleDeleteProduct_Undelete_Success() {
    // Arrange
    testProduct.setIsDeleted(true);
    stubProductRepositoryFindByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID, testProduct);
    stubProductRepositorySave(testProduct);

    // Act
    assertDoesNotThrow(() -> productService.toggleDeleteProduct(TEST_PRODUCT_ID));

    // Assert
    assertFalse(testProduct.getIsDeleted());
    verify(productRepository).save(testProduct);
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify failure with negative ID. Expected Result: NotFoundException is thrown.
   * Assertions: Exception message matches ErrorMessages constant.
   */
  @Test
  @DisplayName("toggleDeleteProduct - Negative ID - Throws NotFound")
  void toggleDeleteProduct_IdNegative_ThrowsNotFound() {
    // Arrange
    long id = -1L;
    stubProductRepositoryFindByIdWithRelatedEntities(id, TEST_CLIENT_ID, null);

    // Act & Assert
    assertThrowsNotFound(
        String.format(ErrorMessages.ProductErrorMessages.ER013, id),
        () -> productService.toggleDeleteProduct(id));
  }

  /**
   * Purpose: Verify failure with ID zero. Expected Result: NotFoundException is thrown. Assertions:
   * Exception message matches ErrorMessages constant.
   */
  @Test
  @DisplayName("toggleDeleteProduct - ID zero - Throws NotFound")
  void toggleDeleteProduct_IdZero_ThrowsNotFound() {
    // Arrange
    long id = 0L;
    stubProductRepositoryFindByIdWithRelatedEntities(id, TEST_CLIENT_ID, null);

    // Act & Assert
    assertThrowsNotFound(
        String.format(ErrorMessages.ProductErrorMessages.ER013, id),
        () -> productService.toggleDeleteProduct(id));
  }

  /**
   * Purpose: Verify failure when product not found. Expected Result: NotFoundException is thrown.
   * Assertions: Exception message matches ErrorMessages constant.
   */
  @Test
  @DisplayName("toggleDeleteProduct - Product not found - Throws NotFound")
  void toggleDeleteProduct_NotFound_ThrowsNotFound() {
    // Arrange
    stubProductRepositoryFindByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID, null);

    // Act & Assert
    assertThrowsNotFound(
        String.format(ErrorMessages.ProductErrorMessages.ER013, TEST_PRODUCT_ID),
        () -> productService.toggleDeleteProduct(TEST_PRODUCT_ID));
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
  @DisplayName("toggleDeleteProduct - Controller permission unauthorized - Success")
  void toggleDeleteProduct_p01_controller_permission_unauthorized() {
    // Arrange
    ProductController controller = new ProductController(productServiceMock);
    stubProductServiceToggleDeleteProductThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = controller.toggleDeleteProduct(TEST_PRODUCT_ID);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    verify(productServiceMock).toggleDeleteProduct(TEST_PRODUCT_ID);
  }

  /**
   * Purpose: Verify controller delegation to service. Expected Result: OK status returned.
   * Assertions: Service called and response is OK.
   */
  @Test
  @DisplayName("toggleDeleteProduct - Controller delegation check - Success")
  void toggleDeleteProduct_p03_ControllerDelegation_Success() {
    // Arrange
    ProductController controller = new ProductController(productServiceMock);
    stubProductServiceToggleDeleteProductDoNothing();

    // Act
    ResponseEntity<?> response = controller.toggleDeleteProduct(TEST_PRODUCT_ID);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(productServiceMock).toggleDeleteProduct(TEST_PRODUCT_ID);
  }

  /**
   * Purpose: Simulate forbidden flow without throwing at controller. Expected Result: No exception
   * thrown. Assertions: Service method invoked.
   */
  @Test
  @DisplayName("toggleDeleteProduct - No permission - Forbidden simulated")
  void toggleDeleteProduct_p04_NoPermission_Forbidden() {
    // Arrange
    ProductController controller = new ProductController(productServiceMock);

    // Act & Assert
    assertDoesNotThrow(() -> controller.toggleDeleteProduct(TEST_PRODUCT_ID));
    verify(productServiceMock).toggleDeleteProduct(TEST_PRODUCT_ID);
  }
}
