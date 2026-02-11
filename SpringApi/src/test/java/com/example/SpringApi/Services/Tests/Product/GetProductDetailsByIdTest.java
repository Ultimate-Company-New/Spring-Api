package com.example.SpringApi.Services.Tests.Product;

import com.example.SpringApi.Controllers.ProductController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.ResponseModels.ProductResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import com.example.SpringApi.Services.ProductService;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Consolidated test class for ProductService.getProductDetailsById.
 * Fully compliant with Unit Test Verification rules.
 */
// Total Tests: 7
@DisplayName("ProductService - GetProductDetailsById Tests")
class GetProductDetailsByIdTest extends ProductServiceTestBase {

    // ==========================================
    // SECTION 1: SUCCESS TESTS
    // ==========================================

    /*
     * Purpose: Verify success when product is found
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

    // ==========================================
    // SECTION 2: FAILURE TESTS
    // ==========================================

    /*
     * Purpose: Verify failure when product is not found
     */
    @Test
    @DisplayName("getProductDetailsById - Product not found - Throws NotFound")
    void getProductDetailsById_NotFound_ThrowsNotFound() {
        // Arrange
        stubProductRepositoryFindByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID, null);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> productService.getProductDetailsById(TEST_PRODUCT_ID));
    }

    /*
     * Purpose: Verify failure with ID zero
     */
    @Test
    @DisplayName("getProductDetailsById - ID zero - Throws NotFound")
    void getProductDetailsById_IdZero_ThrowsNotFound() {
        // Arrange
        stubProductRepositoryFindByIdWithRelatedEntities(0L, TEST_CLIENT_ID, null);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> productService.getProductDetailsById(0L));
    }

    /*
     * Purpose: Verify failure with negative ID
     */
    @Test
    @DisplayName("getProductDetailsById - ID negative - Throws NotFound")
    void getProductDetailsById_IdNegative_ThrowsNotFound() {
        // Arrange
        stubProductRepositoryFindByIdWithRelatedEntities(-1L, TEST_CLIENT_ID, null);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> productService.getProductDetailsById(-1L));
    }

    // ==========================================
    // SECTION 3: PERMISSION / DELEGATION
    // ==========================================

    @Test
    @DisplayName("getProductDetailsById - Verify @PreAuthorize annotation")
    void getProductDetailsById_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = ProductController.class.getMethod("getProductDetailsById", long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation);
        assertTrue(annotation.value().contains(Authorizations.VIEW_PRODUCTS_PERMISSION));
    }

    @Test
    @DisplayName("getProductDetailsById - Controller delegation check")
    void getProductDetailsById_ControllerDelegation_Success() {
        // Arrange
        ProductService mockService = mock(ProductService.class);
        ProductController controller = new ProductController(mockService);
        when(mockService.getProductDetailsById(TEST_PRODUCT_ID)).thenReturn(new ProductResponseModel(testProduct));

        // Act
        ResponseEntity<?> response = controller.getProductDetailsById(TEST_PRODUCT_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(mockService).getProductDetailsById(TEST_PRODUCT_ID);
    }

    @Test
    @DisplayName("getProductDetailsById - No permission - Unauthorized")
    void getProductDetailsById_NoPermission_Unauthorized() {
        // Arrange
        ProductService mockService = mock(ProductService.class);
        ProductController controller = new ProductController(mockService);
        doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                .when(mockService).getProductDetailsById(TEST_PRODUCT_ID);

        // Act
        ResponseEntity<?> response = controller.getProductDetailsById(TEST_PRODUCT_ID);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(mockService).getProductDetailsById(TEST_PRODUCT_ID);
    }
}
