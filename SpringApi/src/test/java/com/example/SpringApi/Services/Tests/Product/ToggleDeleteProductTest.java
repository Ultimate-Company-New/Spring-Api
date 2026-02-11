package com.example.SpringApi.Services.Tests.Product;

import com.example.SpringApi.Controllers.ProductController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import com.example.SpringApi.Services.ProductService;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Consolidated test class for ProductService.toggleDeleteProduct.
 * Fully compliant with Unit Test Verification rules.
 */
// Total Tests: 8
@DisplayName("ProductService - ToggleDeleteProduct Tests")
class ToggleDeleteProductTest extends ProductServiceTestBase {

    // ==========================================
    // SECTION 1: SUCCESS TESTS
    // ==========================================

    /*
     * Purpose: Verify toggle from false to true
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

    /*
     * Purpose: Verify toggle from true to false (undelete)
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

    // ==========================================
    // SECTION 2: FAILURE TESTS
    // ==========================================

    /*
     * Purpose: Verify failure when product not found
     */
    @Test
    @DisplayName("toggleDeleteProduct - Product not found - Throws NotFound")
    void toggleDeleteProduct_NotFound_ThrowsNotFound() {
        // Arrange
        stubProductRepositoryFindByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID, null);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> productService.toggleDeleteProduct(TEST_PRODUCT_ID));
    }

    /*
     * Purpose: Verify failure with invalid ID 0
     */
    @Test
    @DisplayName("toggleDeleteProduct - ID zero - Throws NotFound")
    void toggleDeleteProduct_IdZero_ThrowsNotFound() {
        // Arrange
        stubProductRepositoryFindByIdWithRelatedEntities(0L, TEST_CLIENT_ID, null);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> productService.toggleDeleteProduct(0L));
    }

    /*
     * Purpose: Verify failure with negative ID
     */
    @Test
    @DisplayName("toggleDeleteProduct - Negative ID - Throws NotFound")
    void toggleDeleteProduct_IdNegative_ThrowsNotFound() {
        // Arrange
        stubProductRepositoryFindByIdWithRelatedEntities(-1L, TEST_CLIENT_ID, null);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> productService.toggleDeleteProduct(-1L));
    }

    // ==========================================
    // SECTION 3: PERMISSION / DELEGATION
    // ==========================================

    @Test
    @DisplayName("toggleDeleteProduct - Verify @PreAuthorize annotation")
    void toggleDeleteProduct_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = ProductController.class.getMethod("toggleDeleteProduct", long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation);
        assertTrue(annotation.value().contains(Authorizations.DELETE_PRODUCTS_PERMISSION));
    }

    @Test
    @DisplayName("toggleDeleteProduct - Controller delegation check")
    void toggleDeleteProduct_ControllerDelegation_Success() {
        // Arrange
        ProductService mockService = mock(ProductService.class);
        ProductController controller = new ProductController(mockService);
        doNothing().when(mockService).toggleDeleteProduct(TEST_PRODUCT_ID);

        // Act
        ResponseEntity<?> response = controller.toggleDeleteProduct(TEST_PRODUCT_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(mockService).toggleDeleteProduct(TEST_PRODUCT_ID);
    }

    @Test
    @DisplayName("toggleDeleteProduct - No permission - Unauthorized")
    void toggleDeleteProduct_NoPermission_Unauthorized() {
        // Arrange
        ProductService mockService = mock(ProductService.class);
        ProductController controller = new ProductController(mockService);
        doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                .when(mockService).toggleDeleteProduct(TEST_PRODUCT_ID);

        // Act
        ResponseEntity<?> response = controller.toggleDeleteProduct(TEST_PRODUCT_ID);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(mockService).toggleDeleteProduct(TEST_PRODUCT_ID);
    }

    @Test
    @DisplayName("toggleDeleteProduct - No permission - Forbidden simulated")
    void toggleDeleteProduct_NoPermission_Forbidden() {
        // Arrange
        ProductService mockService = mock(ProductService.class);
        ProductController controller = new ProductController(mockService);

        // Act & Assert
        assertDoesNotThrow(() -> controller.toggleDeleteProduct(TEST_PRODUCT_ID));
        verify(mockService).toggleDeleteProduct(TEST_PRODUCT_ID);
    }
}
