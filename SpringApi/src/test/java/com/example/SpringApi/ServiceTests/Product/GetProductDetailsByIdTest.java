package com.example.SpringApi.ServiceTests.Product;

import com.example.SpringApi.Controllers.ProductController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.ResponseModels.ProductResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

/**
 * Consolidated test class for ProductService.getProductDetailsById.
 * Fully compliant with Unit Test Verification rules.
 */
@DisplayName("ProductService - GetProductDetailsById Tests")
class GetProductDetailsByIdTest extends ProductServiceTestBase {

    // Total Tests: 7
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify success when product is found.
     * Expected Result: Product response is returned.
     * Assertions: Response contains product ID.
     */
    @Test
    @DisplayName("getProductDetailsById - Product found - Success")
    void getProductDetailsById_Found_Success() {
        // Arrange
        stubProductRepositoryFindByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID, testProduct);

        // Act
        ProductResponseModel result = productService.getProductDetailsById(TEST_PRODUCT_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_PRODUCT_ID, result.getProductId());
        verify(productRepository).findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID);
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify failure when product is not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches ErrorMessages constant.
     */
    @Test
    @DisplayName("getProductDetailsById - Product not found - Throws NotFound")
    void getProductDetailsById_f01_NotFound_ThrowsNotFound() {
        // Arrange
        stubProductRepositoryFindByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID, null);

        // Act & Assert
        assertThrowsNotFound(String.format(ErrorMessages.ProductErrorMessages.ER013, TEST_PRODUCT_ID),
                () -> productService.getProductDetailsById(TEST_PRODUCT_ID));
    }

    /**
     * Purpose: Verify failure with ID zero.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches ErrorMessages constant.
     */
    @Test
    @DisplayName("getProductDetailsById - ID zero - Throws NotFound")
    void getProductDetailsById_f02_IdZero_ThrowsNotFound() {
        // Arrange
        long id = 0L;
        stubProductRepositoryFindByIdWithRelatedEntities(id, TEST_CLIENT_ID, null);

        // Act & Assert
        assertThrowsNotFound(String.format(ErrorMessages.ProductErrorMessages.ER013, id),
                () -> productService.getProductDetailsById(id));
    }

    /**
     * Purpose: Verify failure with negative ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches ErrorMessages constant.
     */
    @Test
    @DisplayName("getProductDetailsById - ID negative - Throws NotFound")
    void getProductDetailsById_f03_IdNegative_ThrowsNotFound() {
        // Arrange
        long id = -1L;
        stubProductRepositoryFindByIdWithRelatedEntities(id, TEST_CLIENT_ID, null);

        // Act & Assert
        assertThrowsNotFound(String.format(ErrorMessages.ProductErrorMessages.ER013, id),
                () -> productService.getProductDetailsById(id));
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify unauthorized access is blocked at controller level.
     * Expected Result: Unauthorized status is returned.
     * Assertions: Response status is 401 UNAUTHORIZED.
     */
    @Test
    @DisplayName("getProductDetailsById - Controller permission unauthorized - Success")
    void getProductDetailsById_p01_controller_permission_unauthorized() {
        // Arrange
        ProductController controller = new ProductController(productServiceMock);
        stubProductServiceGetProductDetailsByIdThrowsUnauthorized();

        // Act
        ResponseEntity<?> response = controller.getProductDetailsById(TEST_PRODUCT_ID);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(productServiceMock).getProductDetailsById(anyLong());
    }
    /**
     * Purpose: Verify controller delegation to service.
     * Expected Result: OK status returned.
     * Assertions: Service method invoked and response is OK.
     */
    @Test
    @DisplayName("getProductDetailsById - Controller delegation check - Success")
    void getProductDetailsById_p03_ControllerDelegation_Success() {
        // Arrange
        ProductController controller = new ProductController(productServiceMock);
        stubProductServiceGetProductDetailsByIdReturns(new ProductResponseModel(testProduct));

        // Act
        ResponseEntity<?> response = controller.getProductDetailsById(TEST_PRODUCT_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(productServiceMock).getProductDetailsById(anyLong());
    }
}
