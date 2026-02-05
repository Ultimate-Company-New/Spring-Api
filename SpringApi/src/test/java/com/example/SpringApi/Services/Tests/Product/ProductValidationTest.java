package com.example.SpringApi.Services.Tests.Product;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Helpers.ImgbbHelper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.mockito.MockedConstruction;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductService validation logic.
 * Tests all field validations including title, description, brand, color, condition,
 * country, price, pickup locations, and more.
 * 
 * Test count: 24 tests
 * - SUCCESS: 4 tests
 * - FAILURE / EXCEPTION: 20 tests
 */
@DisplayName("ProductService - Product Validation Tests")
public class ProductValidationTest extends ProductServiceTestBase {

    // ========================================
    // SUCCESS Tests
    // ========================================

    @Test
    @DisplayName("Product Validation - Client ID negative is allowed")
    void productValidation_InvalidClientId_Negative_Success() {
        // Arrange
        testProductRequest.setClientId(-1L);

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
    @DisplayName("Product Validation - Client ID null in request is ignored")
    void productValidation_InvalidClientId_Null_Success() {
        // Arrange
        testProductRequest.setClientId(null);

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
    @DisplayName("Product Validation - Price zero is allowed")
    void productValidation_PriceZero_Success() {
        // Arrange
        testProductRequest.setPrice(BigDecimal.ZERO);

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
    @DisplayName("Product Validation - UPC empty is allowed")
    void productValidation_UpcEmpty_Success() {
        // Arrange
        testProductRequest.setUpc("");

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
    @DisplayName("Product Validation - Brand null throws BadRequestException")
    void productValidation_InvalidBrand_Null_ThrowsBadRequestException() {
        // Arrange
        testProductRequest.setBrand(null);

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidBrand,
                () -> productService.addProduct(testProductRequest));
    }

    @Test
    @DisplayName("Product Validation - Category ID null throws BadRequestException")
    void productValidation_InvalidCategoryId_Null_ThrowsBadRequestException() {
        // Arrange
        testProductRequest.setCategoryId(null);

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidCategoryId,
                () -> productService.addProduct(testProductRequest));
    }

    @Test
    @DisplayName("Product Validation - Color label null throws BadRequestException")
    void productValidation_InvalidColorLabel_Null_ThrowsBadRequestException() {
        // Arrange
        testProductRequest.setColorLabel(null);

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidColorLabel,
                () -> productService.addProduct(testProductRequest));
    }

    @Test
    @DisplayName("Product Validation - Condition null throws BadRequestException")
    void productValidation_InvalidCondition_Null_ThrowsBadRequestException() {
        // Arrange
        testProductRequest.setCondition(null);

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidCondition,
                () -> productService.addProduct(testProductRequest));
    }

    @Test
    @DisplayName("Product Validation - Country of manufacture null throws BadRequestException")
    void productValidation_InvalidCountry_Null_ThrowsBadRequestException() {
        // Arrange
        testProductRequest.setCountryOfManufacture(null);

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidCountryOfManufacture,
                () -> productService.addProduct(testProductRequest));
    }

    @Test
    @DisplayName("Product Validation - Description HTML null throws BadRequestException")
    void productValidation_InvalidDescription_Null_ThrowsBadRequestException() {
        // Arrange
        testProductRequest.setDescriptionHtml(null);

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidDescription,
                () -> productService.addProduct(testProductRequest));
    }

    @Test
    @DisplayName("Product Validation - Pickup location quantities empty throws BadRequestException")
    void productValidation_InvalidPickupLocations_Empty_ThrowsBadRequestException() {
        // Arrange
        testProductRequest.setPickupLocationQuantities(new HashMap<>());

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.AtLeastOnePickupLocationRequired,
                () -> productService.addProduct(testProductRequest));
    }

    @Test
    @DisplayName("Product Validation - Pickup location quantities null throws BadRequestException")
    void productValidation_InvalidPickupLocations_Null_ThrowsBadRequestException() {
        // Arrange
        testProductRequest.setPickupLocationQuantities(null);

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.AtLeastOnePickupLocationRequired,
                () -> productService.addProduct(testProductRequest));
    }

    @Test
    @DisplayName("Product Validation - Price negative throws BadRequestException")
    void productValidation_InvalidPrice_Negative_ThrowsBadRequestException() {
        // Arrange
        testProductRequest.setPrice(BigDecimal.valueOf(-10));

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidPrice,
                () -> productService.addProduct(testProductRequest));
    }

    @Test
    @DisplayName("Product Validation - Price null throws BadRequestException")
    void productValidation_InvalidPrice_Null_ThrowsBadRequestException() {
        // Arrange
        testProductRequest.setPrice(null);

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidPrice,
                () -> productService.addProduct(testProductRequest));
    }

    @Test
    @DisplayName("Product Validation - Title null throws BadRequestException")
    void productValidation_InvalidTitle_Null_ThrowsBadRequestException() {
        // Arrange
        testProductRequest.setTitle(null);

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidTitle,
                () -> productService.addProduct(testProductRequest));
    }

    /**
     * Purpose: Additional validation coverage for addProduct.
     * Expected Result: Invalid inputs throw BadRequestException.
     * Assertions: Exception messages match expected errors.
     */
    @TestFactory
    @DisplayName("Product Validation - Additional invalid inputs")
    Stream<DynamicTest> productValidation_AdditionalInvalidInputs() {
        return Stream.of(
                DynamicTest.dynamicTest("Brand empty", () -> {
                    initializeTestData();
                    testProductRequest.setBrand(" ");
                    assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidBrand,
                            () -> productService.addProduct(testProductRequest));
                }),
                DynamicTest.dynamicTest("Color label empty", () -> {
                    initializeTestData();
                    testProductRequest.setColorLabel(" ");
                    assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidColorLabel,
                            () -> productService.addProduct(testProductRequest));
                }),
                DynamicTest.dynamicTest("Condition empty", () -> {
                    initializeTestData();
                    testProductRequest.setCondition(" ");
                    assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidCondition,
                            () -> productService.addProduct(testProductRequest));
                }),
                DynamicTest.dynamicTest("Country empty", () -> {
                    initializeTestData();
                    testProductRequest.setCountryOfManufacture(" ");
                    assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidCountryOfManufacture,
                            () -> productService.addProduct(testProductRequest));
                }),
                DynamicTest.dynamicTest("Description empty", () -> {
                    initializeTestData();
                    testProductRequest.setDescriptionHtml(" ");
                    assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidDescription,
                            () -> productService.addProduct(testProductRequest));
                }),
                DynamicTest.dynamicTest("Title whitespace", () -> {
                    initializeTestData();
                    testProductRequest.setTitle("   ");
                    assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidTitle,
                            () -> productService.addProduct(testProductRequest));
                })
        );
    }
}
