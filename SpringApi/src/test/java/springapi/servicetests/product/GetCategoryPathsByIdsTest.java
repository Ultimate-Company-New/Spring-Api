package springapi.ServiceTests.Product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import springapi.controllers.ProductController;
import springapi.models.databasemodels.ProductCategory;

/**
 * Consolidated test class for ProductService.getCategoryPathsByIds. Fully compliant with Unit Test
 * Verification rules.
 */
@DisplayName("ProductService - GetCategoryPathsByIds Tests")
class GetCategoryPathsByIdsTest extends ProductServiceTestBase {

  // Total Tests: 8
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify empty input behavior. Expected Result: Empty result map is returned.
   * Assertions: Result map is empty.
   */
  @Test
  @DisplayName("getCategoryPathsByIds - Empty input - Success")
  void getCategoryPathsByIds_EmptyInput_Success() {
    // Arrange

    // Act
    Map<Long, String> result = productService.getCategoryPathsByIds(Collections.emptyList());

    // Assert
    assertTrue(result.isEmpty());
  }

  /**
   * Purpose: Verify path building for multiple levels. Expected Result: Full parent-child path is
   * returned. Assertions: Result map contains expected full path.
   */
  @Test
  @DisplayName("getCategoryPathsByIds - Multiple levels - Success")
  void getCategoryPathsByIds_MultipleLevels_Success() {
    // Arrange
    ProductCategory parent = new ProductCategory();
    parent.setName("Electronics");
    testCategory.setParent(parent);
    testCategory.setParentId(1L); // Assume parent has ID 1
    stubProductCategoryRepositoryFindById(TEST_CATEGORY_ID, testCategory);
    stubProductCategoryRepositoryFindById(1L, parent);

    // Act
    Map<Long, String> results =
        productService.getCategoryPathsByIds(Collections.singletonList(TEST_CATEGORY_ID));

    // Assert
    assertEquals(1, results.size());
    assertEquals("Electronics > Test Category", results.get(TEST_CATEGORY_ID));
  }

  /**
   * Purpose: Verify null list input behavior. Expected Result: Empty result map is returned.
   * Assertions: Result map is empty.
   */
  @Test
  @DisplayName("getCategoryPathsByIds - Null list - Success")
  void getCategoryPathsByIds_NullList_Success() {
    // Arrange

    // Act
    Map<Long, String> result = productService.getCategoryPathsByIds(null);

    // Assert
    assertTrue(result.isEmpty());
  }

  /**
   * Purpose: Verify path building for root level. Expected Result: Category name is returned
   * without parent path. Assertions: Result map contains category name.
   */
  @Test
  @DisplayName("getCategoryPathsByIds - Root level - Success")
  void getCategoryPathsByIds_RootLevel_Success() {
    // Arrange
    testCategory.setParent(null);
    testCategory.setParentId(null);
    stubProductCategoryRepositoryFindById(TEST_CATEGORY_ID, testCategory);

    // Act
    Map<Long, String> results =
        productService.getCategoryPathsByIds(Collections.singletonList(TEST_CATEGORY_ID));

    // Assert
    assertEquals("Test Category", results.get(TEST_CATEGORY_ID));
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify invalid ID is skipped. Expected Result: Empty result map returned. Assertions:
   * Result map is empty.
   */
  @Test
  @DisplayName("getCategoryPathsByIds - Invalid ID in list - Skips")
  void getCategoryPathsByIds_InvalidId_Skips() {
    // Arrange
    stubProductCategoryRepositoryFindById(999L, null);

    // Act
    Map<Long, String> result =
        productService.getCategoryPathsByIds(Collections.singletonList(999L));

    // Assert
    assertTrue(result.isEmpty());
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
  @DisplayName("getCategoryPathsByIds - Controller permission unauthorized - Success")
  void getCategoryPathsByIds_p01_controller_permission_unauthorized() {
    // Arrange
    ProductController controller = new ProductController(productServiceMock);
    stubProductServiceGetCategoryPathsByIdsThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = controller.getCategoryPathsByIds(Collections.singletonList(1L));

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    verify(productServiceMock).getCategoryPathsByIds(any());
  }

  /**
   * Purpose: Verify controller delegates to service. Expected Result: Response status is OK.
   * Assertions: Service method invoked and HTTP 200 returned.
   */
  @Test
  @DisplayName("getCategoryPathsByIds - Controller delegation check - Success")
  void getCategoryPathsByIds_p03_ControllerDelegation_Success() {
    // Arrange
    ProductController controller = new ProductController(productServiceMock);
    stubProductServiceGetCategoryPathsByIdsReturns(Collections.emptyMap());

    // Act
    ResponseEntity<?> response = controller.getCategoryPathsByIds(Collections.singletonList(1L));

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(productServiceMock).getCategoryPathsByIds(any());
  }
}
