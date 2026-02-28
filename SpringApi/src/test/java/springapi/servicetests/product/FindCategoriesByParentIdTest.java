package springapi.ServiceTests.Product;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import springapi.controllers.ProductController;
import springapi.models.databasemodels.ProductCategory;
import springapi.models.responsemodels.ProductCategoryWithPathResponseModel;

/**
 * Consolidated test class for ProductService.findCategoriesByParentId. Fully compliant with Unit
 * Test Verification rules.
 */
@DisplayName("ProductService - FindCategoriesByParentId Tests")
class FindCategoriesByParentIdTest extends ProductServiceTestBase {

  // Total Tests: 7
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify root level categories retrieval. Expected Result: Root categories returned.
   * Assertions: Results contain expected category.
   */
  @Test
  @DisplayName("findCategoriesByParentId - Parent null - Success")
  void findCategoriesByParentId_s01_Root_Success() {
    // Arrange
    testCategory.setParentId(null);
    stubProductCategoryRepositoryFindAll(Collections.singletonList(testCategory));

    // Act
    List<ProductCategoryWithPathResponseModel> results =
        productService.findCategoriesByParentId(null);

    // Assert
    assertEquals(1, results.size());
    assertEquals("Test Category", results.get(0).getName());
  }

  /**
   * Purpose: Verify child categories retrieval. Expected Result: Child categories returned for
   * parent. Assertions: Results contain child category.
   */
  @Test
  @DisplayName("findCategoriesByParentId - Valid parent ID - Success")
  void findCategoriesByParentId_s02_Child_Success() {
    // Arrange
    ProductCategory child = new ProductCategory();
    child.setCategoryId(11L);
    child.setName("Child Category");
    child.setParentId(10L);
    child.setIsEnd(true);
    stubProductCategoryRepositoryFindAll(Collections.singletonList(child));
    stubProductCategoryRepositoryFindById(10L, null); // Stop path traversal

    // Act
    List<ProductCategoryWithPathResponseModel> results =
        productService.findCategoriesByParentId(10L);

    // Assert
    assertEquals(1, results.size());
    assertEquals("Child Category", results.get(0).getName());
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify empty results when no categories exist. Expected Result: Empty list returned.
   * Assertions: Result list is empty.
   */
  @Test
  @DisplayName("findCategoriesByParentId - Empty results - Success")
  void findCategoriesByParentId_EmptyResults_Success() {
    // Arrange
    stubProductCategoryRepositoryFindAll(Collections.emptyList());

    // Act
    List<ProductCategoryWithPathResponseModel> results =
        productService.findCategoriesByParentId(null);

    // Assert
    assertTrue(results.isEmpty());
  }

  /**
   * Purpose: Verify invalid parent ID returns empty results. Expected Result: Empty list returned.
   * Assertions: Result list is empty.
   */
  @Test
  @DisplayName("findCategoriesByParentId - Invalid parent ID (not in DB) - Success (Empty)")
  void findCategoriesByParentId_InvalidParentId_Success() {
    // Arrange
    stubProductCategoryRepositoryFindAll(Collections.emptyList());

    // Act
    List<ProductCategoryWithPathResponseModel> results =
        productService.findCategoriesByParentId(999L);

    // Assert
    assertTrue(results.isEmpty());
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
  @DisplayName("findCategoriesByParentId - Controller permission unauthorized - Success")
  void findCategoriesByParentId_p01_controller_permission_unauthorized() {
    // Arrange
    ProductController controller = new ProductController(productServiceMock);
    stubProductServiceFindCategoriesByParentIdThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = controller.findCategoriesByParentId(null);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    verify(productServiceMock).findCategoriesByParentId(any());
  }

  /**
   * Purpose: Verify controller delegation to service. Expected Result: OK status returned.
   * Assertions: Service method invoked and response is OK.
   */
  @Test
  @DisplayName("findCategoriesByParentId - Controller delegation check - Success")
  void findCategoriesByParentId_p03_ControllerDelegation_Success() {
    // Arrange
    ProductController controller = new ProductController(productServiceMock);
    stubProductServiceFindCategoriesByParentIdReturns(Collections.emptyList());

    // Act
    ResponseEntity<?> response = controller.findCategoriesByParentId(null);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(productServiceMock).findCategoriesByParentId(any());
  }
}
