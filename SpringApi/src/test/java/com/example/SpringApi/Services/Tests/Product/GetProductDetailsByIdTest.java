package com.example.SpringApi.Services.Tests.Product;

import com.example.SpringApi.Controllers.ProductController;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.ResponseModels.ProductResponseModel;
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
import static org.mockito.Mockito.*;

/**
 * Test class for ProductService.getProductDetailsById method.
 * 
 * Test count: 4 tests
 * - SUCCESS: 1 test
 * - FAILURE / EXCEPTION: 3 tests
 */
@DisplayName("ProductService - GetProductDetailsById Tests")
public class GetProductDetailsByIdTest extends ProductServiceTestBase {

    // ===========================
    // SUCCESS TESTS
    // ===========================

    @Test
    @DisplayName("Get Product Details By ID - Success: Product found with related entities")
    void getProductDetailsById_Success() {
        when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID)).thenReturn(testProduct);

        ProductResponseModel result = productService.getProductDetailsById(TEST_PRODUCT_ID);

        assertNotNull(result);
        assertEquals(TEST_PRODUCT_ID, result.getProductId());
        assertEquals(TEST_TITLE, result.getTitle());
        assertNotNull(result.getCategory());
        assertEquals(TEST_CATEGORY_ID, result.getCategory().getCategoryId());
        verify(productRepository, times(1)).findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID);
    }

    // ===========================
    // FAILURE / EXCEPTION TESTS
    // ===========================

    @TestFactory
    @DisplayName("Get Product Details By ID - Additional invalid IDs")
    Stream<DynamicTest> getProductDetailsById_AdditionalInvalidIds() {
        return Stream.of(2L, 3L)
                .map(id -> DynamicTest.dynamicTest("Invalid ID: " + id, () -> {
                    when(productRepository.findByIdWithRelatedEntities(id, TEST_CLIENT_ID)).thenReturn(null);
                    NotFoundException exception = assertThrows(NotFoundException.class,
                            () -> productService.getProductDetailsById(id));
                    assertTrue(exception.getMessage().contains(String.valueOf(id)));
                }));
    }

    @Test
    @DisplayName("Get Product Details By ID - Failure: Product not found")
    void getProductDetailsById_ProductNotFound_ThrowsNotFoundException() {
        // Arrange
        when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> productService.getProductDetailsById(TEST_PRODUCT_ID));
        assertTrue(exception.getMessage().contains(String.valueOf(TEST_PRODUCT_ID)));
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("getProductDetailsById - Verify @PreAuthorize Annotation")
    void getProductDetailsById_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = ProductController.class.getMethod("getProductDetailsById", long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on getProductDetailsById");
        assertTrue(annotation.value().contains(Authorizations.VIEW_PRODUCTS_PERMISSION),
                "@PreAuthorize should reference VIEW_PRODUCTS_PERMISSION");
    }

    @Test
    @DisplayName("getProductDetailsById - Controller delegates to service")
    void getProductDetailsById_WithValidId_DelegatesToService() {
        ProductController controller = new ProductController(productService);
        when(productService.getProductDetailsById(TEST_PRODUCT_ID))
                .thenReturn(mock(ProductResponseModel.class));

        ResponseEntity<?> response = controller.getProductDetailsById(TEST_PRODUCT_ID);

        verify(productService).getProductDetailsById(TEST_PRODUCT_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
