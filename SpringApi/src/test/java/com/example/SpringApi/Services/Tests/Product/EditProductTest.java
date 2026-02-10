package com.example.SpringApi.Services.Tests.Product;

import com.example.SpringApi.Controllers.ProductController;
import com.example.SpringApi.Services.ProductService;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.Product;
import com.example.SpringApi.Models.RequestModels.ProductRequestModel;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Helpers.ImgbbHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.mockito.MockedConstruction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductService.editProduct method.
 * 
 * Test count: 6 tests
 * - SUCCESS: 1 test
 * - FAILURE / EXCEPTION: 5 tests
 */
@DisplayName("ProductService - EditProduct Tests")
class EditProductTest extends ProductServiceTestBase {

    // ===========================
    // SUCCESS TESTS
    // ===========================

    @Test
    @DisplayName("Edit Product - Success: Valid product update")
    void editProduct_Success() {
        when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID)).thenReturn(testProduct);

        try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                (mock, context) -> {
                    ImgbbHelper.ImgbbUploadResponse mockResponse = new ImgbbHelper.ImgbbUploadResponse(
                            "https://i.ibb.co/test/image.png",
                            "test-delete-hash");
                    when(mock.uploadFileToImgbb(anyString(), anyString())).thenReturn(mockResponse);
                    when(mock.deleteImage(anyString())).thenReturn(true);
                })) {

            assertDoesNotThrow(() -> productService.editProduct(testProductRequest));

            verify(productRepository, times(1)).findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID);
            verify(productRepository, atLeastOnce()).save(any(Product.class));
            verify(userLogService, times(1)).logData(eq(1L), anyString(), eq("editProduct"));
        }
    }

    // ===========================
    // FAILURE / EXCEPTION TESTS
    // ===========================

    @TestFactory
    @DisplayName("Edit Product - Additional invalid IDs")
    Stream<DynamicTest> editProduct_AdditionalInvalidIds() {
        return Stream.of(2L, 3L, 4L)
                .map(id -> DynamicTest.dynamicTest("Invalid ID: " + id, () -> {
                    testProductRequest.setProductId(id);
                    lenient().when(productRepository.findByIdWithRelatedEntities(id, TEST_CLIENT_ID)).thenReturn(null);
                    NotFoundException exception = assertThrows(NotFoundException.class,
                            () -> productService.editProduct(testProductRequest));
                    assertTrue(exception.getMessage().contains(String.valueOf(id)));
                }));
    }

    @Test
    @DisplayName("Edit Product - Failure: Null product ID")
    void editProduct_NullProductId_ThrowsBadRequestException() {
        // Arrange
        testProductRequest.setProductId(null);

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidId,
                () -> productService.editProduct(testProductRequest));
        verify(productRepository, never()).findByIdWithRelatedEntities(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Edit Product - Failure: Product not found")
    void editProduct_ProductNotFound_ThrowsNotFoundException() {
        // Arrange
        lenient().when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> productService.editProduct(testProductRequest));
        assertTrue(exception.getMessage().contains(String.valueOf(TEST_PRODUCT_ID)));
        verify(productRepository, never()).save(any());
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("editProduct - Verify @PreAuthorize Annotation")
    void editProduct_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = ProductController.class.getMethod("editProduct", ProductRequestModel.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on editProduct");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_PRODUCTS_PERMISSION),
                "@PreAuthorize should reference UPDATE_PRODUCTS_PERMISSION");
    }

    @Test
    @DisplayName("Edit Product - Valid Request - Delegates to Service")
    void editProduct_WithValidRequest_DelegatesToService() {
        ProductService mockProductService = mock(ProductService.class);
        ProductController controller = new ProductController(mockProductService);
        doNothing().when(mockProductService).editProduct(testProductRequest);

        ResponseEntity<?> response = controller.editProduct(testProductRequest);

        verify(mockProductService).editProduct(testProductRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
