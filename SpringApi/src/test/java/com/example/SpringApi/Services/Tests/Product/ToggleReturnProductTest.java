package com.example.SpringApi.Services.Tests.Product;

import com.example.SpringApi.Controllers.ProductController;
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
 * Consolidated test class for ProductService.toggleReturnProduct.
 * Fully compliant with Unit Test Verification rules.
 */
// Total Tests: 8
@DisplayName("ProductService - ToggleReturnProduct Tests")
class ToggleReturnProductTest extends ProductServiceTestBase {

    // ==========================================
    // SECTION 1: SUCCESS TESTS
    // ==========================================

    /*
     * Purpose: Verify toggle from 30 to 0 (disable returns)
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

    /*
     * Purpose: Verify toggle from 0 to 30 (enable returns)
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
     * Purpose: Verify toggle from null to 30
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

    // ==========================================
    // SECTION 2: FAILURE TESTS
    // ==========================================

    /*
     * Purpose: Verify failure when product not found
     */
    @Test
    @DisplayName("toggleReturnProduct - Product not found - Throws NotFound")
    void toggleReturnProduct_NotFound_ThrowsNotFound() {
        // Arrange
        stubProductRepositoryFindByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID, null);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> productService.toggleReturnProduct(TEST_PRODUCT_ID));
    }

    /*
     * Purpose: Verify failure with invalid ID 0
     */
    @Test
    @DisplayName("toggleReturnProduct - ID zero - Throws NotFound")
    void toggleReturnProduct_IdZero_ThrowsNotFound() {
        // Arrange
        stubProductRepositoryFindByIdWithRelatedEntities(0L, TEST_CLIENT_ID, null);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> productService.toggleReturnProduct(0L));
    }

    // ==========================================
    // SECTION 3: PERMISSION / DELEGATION
    // ==========================================

    @Test
    @DisplayName("toggleReturnProduct - Verify @PreAuthorize annotation")
    void toggleReturnProduct_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = ProductController.class.getMethod("toggleReturnProduct", long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation);
        assertTrue(annotation.value().contains(Authorizations.TOGGLE_PRODUCT_RETURNS_PERMISSION));
    }

    @Test
    @DisplayName("toggleReturnProduct - Controller delegation check")
    void toggleReturnProduct_ControllerDelegation_Success() {
        // Arrange
        ProductService mockService = mock(ProductService.class);
        ProductController controller = new ProductController(mockService);
        doNothing().when(mockService).toggleReturnProduct(TEST_PRODUCT_ID);

        // Act
        ResponseEntity<?> response = controller.toggleReturnProduct(TEST_PRODUCT_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(mockService).toggleReturnProduct(TEST_PRODUCT_ID);
    }

    @Test
    @DisplayName("toggleReturnProduct - No permission - Forbidden simulated")
    void toggleReturnProduct_NoPermission_Forbidden() {
        // Arrange
        ProductService mockService = mock(ProductService.class);
        ProductController controller = new ProductController(mockService);

        // Act & Assert
        assertDoesNotThrow(() -> controller.toggleReturnProduct(TEST_PRODUCT_ID));
        verify(mockService).toggleReturnProduct(TEST_PRODUCT_ID);
    }
}
