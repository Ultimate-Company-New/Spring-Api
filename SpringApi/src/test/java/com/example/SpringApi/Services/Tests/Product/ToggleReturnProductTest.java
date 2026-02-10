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
 * Test class for ProductService.toggleReturnProduct method.
 * 
 * Test count: 4 tests
 * - SUCCESS: 1 test
 * - FAILURE / EXCEPTION: 3 tests
 */
@DisplayName("ProductService - ToggleReturnProduct Tests")
public class ToggleReturnProductTest extends ProductServiceTestBase {

    // ===========================
    // SUCCESS TESTS
    // ===========================

    @Test
    @DisplayName("Toggle Return Product - Success: Toggle returns allowed")
    void toggleReturnProduct_Success() {
        testProduct.setReturnWindowDays(30);
        when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID)).thenReturn(testProduct);

        assertDoesNotThrow(() -> productService.toggleReturnProduct(TEST_PRODUCT_ID));

        verify(productRepository, times(1)).findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID);
        verify(productRepository, times(1)).save(testProduct);
        verify(userLogService, times(1)).logData(eq(1L), anyString(), anyString());
        assertEquals(0, testProduct.getReturnWindowDays());
    }

    // ===========================
    // FAILURE / EXCEPTION TESTS
    // ===========================

    @Test
    @DisplayName("Toggle Return Product - Failure: Product not found")
    void toggleReturnProduct_ProductNotFound_ThrowsNotFoundException() {
        // Arrange
        lenient().when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> productService.toggleReturnProduct(TEST_PRODUCT_ID));
        assertTrue(exception.getMessage().contains(String.valueOf(TEST_PRODUCT_ID)));
        verify(productRepository, never()).save(any());
    }

    @TestFactory
    @DisplayName("Toggle Return Product - Null/Zero returnWindowDays")
    Stream<DynamicTest> toggleReturnProduct_NullOrZeroReturnWindow() {
        return Stream.of("null", "zero")
                .map(label -> DynamicTest.dynamicTest("Return window: " + label, () -> {
                    if ("null".equals(label)) {
                        testProduct.setReturnWindowDays(null);
                    } else {
                        testProduct.setReturnWindowDays(0);
                    }
                    when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID))
                            .thenReturn(testProduct);

                    productService.toggleReturnProduct(TEST_PRODUCT_ID);

                    assertEquals(30, testProduct.getReturnWindowDays());
                }));
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("toggleReturnProduct - Verify @PreAuthorize Annotation")
    void toggleReturnProduct_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = ProductController.class.getMethod("toggleReturnProduct", long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on toggleReturnProduct");
        assertTrue(annotation.value().contains(Authorizations.TOGGLE_PRODUCT_RETURNS_PERMISSION),
                "@PreAuthorize should reference TOGGLE_PRODUCT_RETURNS_PERMISSION");
    }

    @Test
    @DisplayName("toggleReturnProduct - Controller delegates to service")
    void toggleReturnProduct_WithValidId_DelegatesToService() {
        ProductService mockProductService = mock(ProductService.class);
        ProductController controller = new ProductController(mockProductService);
        doNothing().when(mockProductService).toggleReturnProduct(TEST_PRODUCT_ID);

        ResponseEntity<?> response = controller.toggleReturnProduct(TEST_PRODUCT_ID);

        verify(mockProductService).toggleReturnProduct(TEST_PRODUCT_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
