package com.example.SpringApi.Services.Tests.Product;

import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Models.DatabaseModels.Product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.mockito.MockedConstruction;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductService image processing logic.
 * Tests URL to base64 conversion failures, null/empty image handling,
 * and optional image variations.
 * 
 * Test count: 6 tests
 * - SUCCESS: 2 tests
 * - FAILURE / EXCEPTION: 4 tests (1 test + 3 dynamic tests)
 */
@DisplayName("ProductService - Image Processing Tests")
public class ProductImageProcessingTest extends ProductServiceTestBase {

    // ========================================
    // SUCCESS Tests
    // ========================================

    @Test
    @DisplayName("Image Processing - Handle empty image data")
    void imageProcessing_EmptyImage_Success() {
        // Test through the service - empty images should be handled gracefully for optional images
        testProductRequest.setAdditionalImage1("");

        try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                (mock, context) -> {
                    ImgbbHelper.ImgbbUploadResponse mockResponse = new ImgbbHelper.ImgbbUploadResponse(
                            "https://i.ibb.co/test/image.png",
                            "test-delete-hash");
                    when(mock.uploadFileToImgbb(anyString(), anyString())).thenReturn(mockResponse);
                    when(mock.deleteImage(anyString())).thenReturn(true);
                })) {

            assertDoesNotThrow(() -> productService.addProduct(testProductRequest));
        }
    }

    @Test
    @DisplayName("Image Processing - Handle null image data")
    void imageProcessing_NullImage_Success() {
        // Test through the service - null images should be handled gracefully for optional images
        testProductRequest.setAdditionalImage1(null);

        try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                (mock, context) -> {
                    ImgbbHelper.ImgbbUploadResponse mockResponse = new ImgbbHelper.ImgbbUploadResponse(
                            "https://i.ibb.co/test/image.png",
                            "test-delete-hash");
                    when(mock.uploadFileToImgbb(anyString(), anyString())).thenReturn(mockResponse);
                    when(mock.deleteImage(anyString())).thenReturn(true);
                })) {

            assertDoesNotThrow(() -> productService.addProduct(testProductRequest));
        }
    }

    // ========================================
    // FAILURE / EXCEPTION Tests
    // ========================================

    @Test
    @DisplayName("Image Processing - Convert URL to base64 fails in unit test (network access)")
    void imageProcessing_UrlToBase64Fails_ThrowsBadRequestException() {
        // This test verifies that URL image processing fails appropriately in unit test environment
        // where network access is not available

        testProductRequest.setMainImage(TEST_URL_IMAGE);

        try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                (mock, context) -> {
                    ImgbbHelper.ImgbbUploadResponse mockResponse = new ImgbbHelper.ImgbbUploadResponse(
                            "https://i.ibb.co/test/image.png",
                            "test-delete-hash");
                    when(mock.uploadFileToImgbb(anyString(), anyString())).thenReturn(mockResponse);
                    when(mock.deleteImage(anyString())).thenReturn(true);
                })) {

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> productService.addProduct(testProductRequest));

            assertTrue(exception.getMessage().contains("Failed to process image from URL"));
            verify(productRepository, times(1)).save(any(Product.class));
        }
    }

    /**
     * Purpose: Additional image processing cases for optional images.
     * Expected Result: Optional images can be null or empty without failure.
     * Assertions: No exceptions thrown.
     */
    @TestFactory
    @DisplayName("Image Processing - Optional image variations")
    Stream<DynamicTest> imageProcessing_OptionalImageVariations() {
        return Stream.of("additionalImage2 null", "additionalImage2 empty", "additionalImage3 null")
                .map(label -> DynamicTest.dynamicTest(label, () -> {
                    if ("additionalImage2 null".equals(label)) {
                        testProductRequest.setAdditionalImage2(null);
                    } else if ("additionalImage2 empty".equals(label)) {
                        testProductRequest.setAdditionalImage2("");
                    } else {
                        testProductRequest.setAdditionalImage3(null);
                    }

                    try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                            (mock, context) -> {
                                ImgbbHelper.ImgbbUploadResponse mockResponse = new ImgbbHelper.ImgbbUploadResponse(
                                        "https://i.ibb.co/test/image.png",
                                        "test-delete-hash");
                                when(mock.uploadFileToImgbb(anyString(), anyString())).thenReturn(mockResponse);
                                when(mock.deleteImage(anyString())).thenReturn(true);
                            })) {

                        assertDoesNotThrow(() -> productService.addProduct(testProductRequest));
                    }
                }));
    }
}
