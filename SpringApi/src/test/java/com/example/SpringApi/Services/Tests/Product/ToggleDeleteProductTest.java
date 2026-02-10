package com.example.SpringApi.Services.Tests.Product;

import com.example.SpringApi.Controllers.ProductController;
import com.example.SpringApi.Services.ProductService;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductService.toggleDeleteProduct method.
 * 
 * Test count: 6 tests
 * - SUCCESS: 2 tests
 * - FAILURE / EXCEPTION: 4 tests
 */
@DisplayName("ProductService - ToggleDeleteProduct Tests")
public class ToggleDeleteProductTest extends ProductServiceTestBase {

    // ===========================
    // SUCCESS TESTS
    // ===========================

    @Test
    @DisplayName("Toggle Delete Product - Success: Toggle from false to true")
    void toggleDeleteProduct_Success() {
        testProduct.setIsDeleted(false);
        when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID)).thenReturn(testProduct);

        assertDoesNotThrow(() -> productService.toggleDeleteProduct(TEST_PRODUCT_ID));

        verify(productRepository, times(1)).findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID);
        verify(productRepository, times(1)).save(testProduct);
        verify(userLogService, times(1)).logData(eq(1L), anyString(), anyString());
        assertTrue(testProduct.getIsDeleted());
    }

    @Test
    @DisplayName("Toggle Delete Product - Success: Toggle from true to false")
    void toggleDeleteProduct_Undelete_Success() {
        // Arrange
        testProduct.setIsDeleted(true);
        when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID)).thenReturn(testProduct);

        // Act
        assertDoesNotThrow(() -> productService.toggleDeleteProduct(TEST_PRODUCT_ID));

        // Assert
        assertFalse(testProduct.getIsDeleted());
    }

    // ===========================
    // FAILURE / EXCEPTION TESTS
    // ===========================

    @TestFactory
    @DisplayName("Toggle Delete Product - Additional invalid IDs")
    Stream<DynamicTest> toggleDeleteProduct_AdditionalInvalidIds() {
        return Stream.of(2L, 3L, 4L)
                .map(id -> DynamicTest.dynamicTest("Invalid ID: " + id, () -> {
                    lenient().when(productRepository.findByIdWithRelatedEntities(id, TEST_CLIENT_ID)).thenReturn(null);
                    NotFoundException exception = assertThrows(NotFoundException.class,
                            () -> productService.toggleDeleteProduct(id));
                    assertTrue(exception.getMessage().contains(String.valueOf(id)));
                }));
    }

    @Test
    @DisplayName("Toggle Delete Product - Failure: Product not found")
    void toggleDeleteProduct_ProductNotFound_ThrowsNotFoundException() {
        // Arrange
        lenient().when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> productService.toggleDeleteProduct(TEST_PRODUCT_ID));
        assertTrue(exception.getMessage().contains(String.valueOf(TEST_PRODUCT_ID)));
        verify(productRepository, never()).save(any());
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("toggleDeleteProduct - Verify @PreAuthorize Annotation")
    void toggleDeleteProduct_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = ProductController.class.getMethod("toggleDeleteProduct", long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on toggleDeleteProduct");
        assertTrue(annotation.value().contains(Authorizations.DELETE_PRODUCTS_PERMISSION),
                "@PreAuthorize should reference DELETE_PRODUCTS_PERMISSION");
    }

    @Test
    @DisplayName("toggleDeleteProduct - Controller delegates to service")
    void toggleDeleteProduct_WithValidId_DelegatesToService() {
        ProductService mockProductService = mock(ProductService.class);
        ProductController controller = new ProductController(mockProductService);
        doNothing().when(mockProductService).toggleDeleteProduct(TEST_PRODUCT_ID);

        ResponseEntity<?> response = controller.toggleDeleteProduct(TEST_PRODUCT_ID);

        verify(mockProductService).toggleDeleteProduct(TEST_PRODUCT_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
