package com.example.SpringApi.Services.Tests.Product;

import com.example.SpringApi.Controllers.ProductController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductService.addProduct method.
 * 
 * Test count: 14 tests
 * - SUCCESS: 9 tests
 * - FAILURE / EXCEPTION: 5 tests
 */
@DisplayName("ProductService - AddProduct Tests")
public class AddProductTest extends ProductServiceTestBase {

    // ===========================
    // SUCCESS TESTS
    // ===========================

    @TestFactory
    @DisplayName("Add Product - Additional success variations")
    Stream<DynamicTest> addProduct_AdditionalSuccessVariations() {
        return Stream.of(
                "additionalImage1 null",
                "additionalImage1 empty",
                "additionalImage2 empty",
                "additionalImage3 null",
                "additionalImage3 empty",
                "detailsImage present",
                "defectImage present"
        ).map(label -> DynamicTest.dynamicTest(label, () -> {
            ProductRequestModel req = new ProductRequestModel();
            req.setProductId(TEST_PRODUCT_ID);
            req.setTitle(TEST_TITLE);
            req.setDescriptionHtml(TEST_DESCRIPTION);
            req.setBrand(TEST_BRAND);
            req.setColorLabel(TEST_COLOR_LABEL);
            req.setCondition(TEST_CONDITION);
            req.setCountryOfManufacture(TEST_COUNTRY);
            req.setUpc(TEST_UPC);
            req.setPrice(TEST_PRICE);
            req.setCategoryId(TEST_CATEGORY_ID);
            req.setClientId(TEST_CLIENT_ID);
            req.setIsDeleted(false);
            req.setReturnWindowDays(30);

            Map<Long, Integer> quantities = new HashMap<>();
            quantities.put(TEST_PICKUP_LOCATION_ID, 10);
            req.setPickupLocationQuantities(quantities);

            req.setMainImage(TEST_BASE64_IMAGE);
            req.setTopImage(TEST_BASE64_IMAGE);
            req.setBottomImage(TEST_BASE64_IMAGE);
            req.setFrontImage(TEST_BASE64_IMAGE);
            req.setBackImage(TEST_BASE64_IMAGE);
            req.setRightImage(TEST_BASE64_IMAGE);
            req.setLeftImage(TEST_BASE64_IMAGE);
            req.setDetailsImage(TEST_BASE64_IMAGE);
            req.setDefectImage(TEST_BASE64_IMAGE);

            req.setAdditionalImage1(TEST_BASE64_IMAGE);
            req.setAdditionalImage2(TEST_BASE64_IMAGE);
            req.setAdditionalImage3(TEST_BASE64_IMAGE);

            switch (label) {
                case "additionalImage1 null" -> req.setAdditionalImage1(null);
                case "additionalImage1 empty" -> req.setAdditionalImage1("");
                case "additionalImage2 empty" -> req.setAdditionalImage2("");
                case "additionalImage3 null" -> req.setAdditionalImage3(null);
                case "additionalImage3 empty" -> req.setAdditionalImage3("");
                default -> { }
            }

            try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                    (mock, context) -> {
                        ImgbbHelper.ImgbbUploadResponse mockResponse = new ImgbbHelper.ImgbbUploadResponse(
                                "https://i.ibb.co/test/image.png",
                                "test-delete-hash");
                        when(mock.uploadFileToImgbb(anyString(), anyString())).thenReturn(mockResponse);
                        when(mock.deleteImage(anyString())).thenReturn(true);
                    })) {
                assertDoesNotThrow(() -> productService.addProduct(req));
            }
        }));
    }

    @Test
    @DisplayName("Add Product - Success: Valid product with all required images")
    void addProduct_Success() {
        try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                (mock, context) -> {
                    ImgbbHelper.ImgbbUploadResponse mockResponse = new ImgbbHelper.ImgbbUploadResponse(
                            "https://i.ibb.co/test/image.png",
                            "test-delete-hash");
                    when(mock.uploadFileToImgbb(anyString(), anyString())).thenReturn(mockResponse);
                    when(mock.deleteImage(anyString())).thenReturn(true);
                })) {

            assertDoesNotThrow(() -> productService.addProduct(testProductRequest));

            verify(productCategoryRepository, times(1)).findById(TEST_CATEGORY_ID);
            verify(clientService, times(1)).getClientById(TEST_CLIENT_ID);
            verify(productRepository, atLeastOnce()).save(any(Product.class));
            verify(userLogService, times(1)).logDataWithContext(eq(1L), anyString(), eq(1L), anyString(), eq("addProduct"));
        }
    }

    @Test
    @DisplayName("Add Product - Success: With valid request and ImgBB upload")
    void addProduct_ValidRequestWithImgbbUpload_Success() {
        try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                (mock, context) -> {
                    ImgbbHelper.ImgbbUploadResponse mockResponse = new ImgbbHelper.ImgbbUploadResponse(
                            "https://i.ibb.co/test/image.png",
                            "test-delete-hash");
                    when(mock.uploadFileToImgbb(anyString(), anyString())).thenReturn(mockResponse);
                    when(mock.deleteImage(anyString())).thenReturn(true);
                })) {

            assertDoesNotThrow(() -> productService.addProduct(testProductRequest));
            verify(productRepository, atLeastOnce()).save(any());
        }
    }

    // ===========================
    // FAILURE / EXCEPTION TESTS
    // ===========================

    @Test
    @DisplayName("Add Product - Failure: Category not found")
    void addProduct_CategoryNotFound_ThrowsNotFoundException() {
        // Arrange
        when(productCategoryRepository.findById(TEST_CATEGORY_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> productService.addProduct(testProductRequest));
        assertTrue(exception.getMessage().contains(String.valueOf(TEST_CATEGORY_ID)));
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Add Product - Failure: ImgBB upload fails")
    void addProduct_ImgbbUploadFails_ThrowsBadRequestException() {
        try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                (mock, context) -> {
                    when(mock.uploadFileToImgbb(anyString(), anyString())).thenReturn(null);
                    when(mock.deleteImage(anyString())).thenReturn(true);
                })) {

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> productService.addProduct(testProductRequest));

            assertTrue(exception.getMessage().contains("Failed to upload") && exception.getMessage().contains("image"));
            verify(productRepository, times(1)).save(any());
        }
    }

    @Test
    @DisplayName("Add Product - Failure: Missing required image")
    void addProduct_MissingRequiredImage_ThrowsBadRequestException() {
        // Arrange
        testProductRequest.setMainImage(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> productService.addProduct(testProductRequest));
        assertTrue(exception.getMessage().contains("MAIN"));
        verify(productRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Add Product - Failure: Null category ID")
    void addProduct_NullCategoryId_ThrowsBadRequestException() {
        // Arrange
        testProductRequest.setCategoryId(null);

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidCategoryId,
                () -> productService.addProduct(testProductRequest));
        verify(productCategoryRepository, never()).findById(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Add Product - Failure: URL image conversion fails (network error)")
    void addProduct_UrlImageConversionFails_ThrowsBadRequestException() {
        // Arrange
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

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("addProduct - Verify @PreAuthorize Annotation")
    void addProduct_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = ProductController.class.getMethod("addProduct", ProductRequestModel.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on addProduct");
        assertTrue(annotation.value().contains(Authorizations.INSERT_PRODUCTS_PERMISSION),
                "@PreAuthorize should reference INSERT_PRODUCTS_PERMISSION");
    }

    @Test
    @DisplayName("addProduct - Controller delegates to service")
    void addProduct_WithValidRequest_DelegatesToService() throws Exception {
        ProductController controller = new ProductController(productService);
        doNothing().when(productService).addProduct(testProductRequest);

        ResponseEntity<?> response = controller.addProduct(testProductRequest);

        verify(productService).addProduct(testProductRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
