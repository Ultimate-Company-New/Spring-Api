package com.example.SpringApi.Services.Tests.Product;

import com.example.SpringApi.Constants.ProductConditionConstants;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.Product;
import com.example.SpringApi.Models.RequestModels.ProductRequestModel;
import com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.mockito.MockedConstruction;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductService.bulkAddProducts method.
 * Tests bulk addition of products, handling valid/invalid inputs,
 * partial success scenarios, and database errors.
 * 
 * Test count: 10 tests
 * - SUCCESS: 3 tests (1 test + 2 dynamic tests)
 * - FAILURE / EXCEPTION: 7 tests (3 tests + 4 dynamic tests)
 */
@DisplayName("ProductService - BulkAddProducts Tests")
public class BulkAddProductsTest extends ProductServiceTestBase {

    // ========================================
    // SUCCESS Tests
    // ========================================

    @Test
    @DisplayName("Bulk Add Products - Success - All valid products")
    void bulkAddProducts_AllValid_Success() {
        // Arrange
        List<ProductRequestModel> products = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            ProductRequestModel prodReq = new ProductRequestModel();
            prodReq.setTitle("Product" + i);
            prodReq.setDescriptionHtml("Description" + i);
            prodReq.setBrand("Brand" + i);
            prodReq.setPrice(BigDecimal.valueOf(100 + i));
            prodReq.setCategoryId(TEST_CATEGORY_ID);
            prodReq.setMainImage(TEST_BASE64_IMAGE);
            prodReq.setTopImage(TEST_BASE64_IMAGE);
            prodReq.setBottomImage(TEST_BASE64_IMAGE);
            prodReq.setFrontImage(TEST_BASE64_IMAGE);
            prodReq.setBackImage(TEST_BASE64_IMAGE);
            prodReq.setRightImage(TEST_BASE64_IMAGE);
            prodReq.setLeftImage(TEST_BASE64_IMAGE);
            prodReq.setDetailsImage(TEST_BASE64_IMAGE);
            prodReq.setDefectImage(TEST_BASE64_IMAGE);
            prodReq.setColorLabel("Color" + i);
            prodReq.setCondition(ProductConditionConstants.NEW_WITH_TAGS);
            prodReq.setCountryOfManufacture("USA");
            prodReq.setClientId(TEST_CLIENT_ID);
            Map<Long, Integer> quantities = new HashMap<>();
            quantities.put(TEST_PICKUP_LOCATION_ID, 10);
            prodReq.setPickupLocationQuantities(quantities);
            products.add(prodReq);
        }

        when(productCategoryRepository.findById(TEST_CATEGORY_ID)).thenReturn(java.util.Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setProductId((long) (Math.random() * 1000));
            return product;
        });
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
        lenient().when(productPickupLocationMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        lenient().when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });
        Client productClient = new Client();
        productClient.setClientId(TEST_CLIENT_ID);
        productClient.setImgbbApiKey("test-imgbb-api-key");
        lenient().when(clientRepository.findById(anyLong())).thenReturn(Optional.of(productClient));

        ClientResponseModel clientResponse = new ClientResponseModel();
        clientResponse.setName("Test Client");
        lenient().when(clientService.getClientById(anyLong())).thenReturn(clientResponse);

        try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                (mock, context) -> {
                    when(mock.uploadFileToImgbb(anyString(), anyString()))
                            .thenReturn(
                                    new ImgbbHelper.ImgbbUploadResponse("https://imgbb.com/test.png", "deleteHash"));
                    when(mock.deleteImage(anyString())).thenReturn(true);
                })) {
            // Act
            BulkInsertResponseModel<Long> result = productService.bulkAddProducts(products);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getTotalRequested());
            assertEquals(2, result.getSuccessCount());
            assertEquals(0, result.getFailureCount());
        }
    }

    // ========================================
    // FAILURE / EXCEPTION Tests
    // ========================================

    @Test
    @DisplayName("Bulk Add Products - Database Error")
    void bulkAddProducts_DatabaseError() {
        // Arrange
        List<ProductRequestModel> products = new ArrayList<>();
        ProductRequestModel prodReq = new ProductRequestModel();
        prodReq.setTitle("Test Product");
        prodReq.setDescriptionHtml("Description");
        prodReq.setBrand("Brand");
        prodReq.setPrice(BigDecimal.valueOf(100));
        prodReq.setCategoryId(TEST_CATEGORY_ID);
        prodReq.setMainImage(TEST_BASE64_IMAGE);
        prodReq.setTopImage(TEST_BASE64_IMAGE);
        prodReq.setBottomImage(TEST_BASE64_IMAGE);
        prodReq.setFrontImage(TEST_BASE64_IMAGE);
        prodReq.setBackImage(TEST_BASE64_IMAGE);
        prodReq.setRightImage(TEST_BASE64_IMAGE);
        prodReq.setLeftImage(TEST_BASE64_IMAGE);
        prodReq.setDetailsImage(TEST_BASE64_IMAGE);
        prodReq.setDefectImage(TEST_BASE64_IMAGE);
        Map<Long, Integer> quantities = new HashMap<>();
        quantities.put(TEST_PICKUP_LOCATION_ID, 10);
        prodReq.setPickupLocationQuantities(quantities);
        products.add(prodReq);

        lenient().when(productCategoryRepository.findById(TEST_CATEGORY_ID))
                .thenReturn(java.util.Optional.of(testCategory));
        lenient().when(productRepository.save(any(Product.class))).thenThrow(new RuntimeException("Database error"));

        // Act
        BulkInsertResponseModel<Long> result = productService.bulkAddProducts(products);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalRequested());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
    }

    @Test
    @DisplayName("Bulk Add Products - Empty List")
    void bulkAddProducts_EmptyList_ThrowsBadRequestException() {
        // Arrange
        List<ProductRequestModel> products = new ArrayList<>();

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            productService.bulkAddProducts(products);
        });
        assertTrue(exception.getMessage().contains("Product list cannot be null or empty"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Bulk Add Products - Partial Success")
    void bulkAddProducts_PartialSuccess() {
        // Arrange
        List<ProductRequestModel> products = new ArrayList<>();

        // Valid product
        ProductRequestModel validProduct = new ProductRequestModel();
        validProduct.setTitle("Valid Product");
        validProduct.setDescriptionHtml("Description");
        validProduct.setBrand("Brand");
        validProduct.setPrice(BigDecimal.valueOf(100));
        validProduct.setCategoryId(TEST_CATEGORY_ID);
        validProduct.setMainImage(TEST_BASE64_IMAGE);
        validProduct.setTopImage(TEST_BASE64_IMAGE);
        validProduct.setBottomImage(TEST_BASE64_IMAGE);
        validProduct.setFrontImage(TEST_BASE64_IMAGE);
        validProduct.setBackImage(TEST_BASE64_IMAGE);
        validProduct.setRightImage(TEST_BASE64_IMAGE);
        validProduct.setLeftImage(TEST_BASE64_IMAGE);
        validProduct.setDetailsImage(TEST_BASE64_IMAGE);
        validProduct.setDefectImage(TEST_BASE64_IMAGE);
        validProduct.setColorLabel("ValidColor");
        validProduct.setCondition(ProductConditionConstants.NEW_WITH_TAGS);
        validProduct.setCountryOfManufacture("USA");
        validProduct.setClientId(TEST_CLIENT_ID);
        Map<Long, Integer> quantities = new HashMap<>();
        quantities.put(TEST_PICKUP_LOCATION_ID, 10);
        validProduct.setPickupLocationQuantities(quantities);
        products.add(validProduct);

        // Invalid product (missing title)
        ProductRequestModel invalidProduct = new ProductRequestModel();
        invalidProduct.setTitle(null);
        invalidProduct.setCondition(ProductConditionConstants.PRE_OWNED);
        invalidProduct.setCountryOfManufacture("USA");
        invalidProduct.setClientId(TEST_CLIENT_ID);
        invalidProduct.setColorLabel("InvalidColor");
        products.add(invalidProduct);

        when(productCategoryRepository.findById(TEST_CATEGORY_ID)).thenReturn(java.util.Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setProductId((long) (Math.random() * 1000));
            return product;
        });
        lenient().when(productPickupLocationMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        lenient().when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });
        Client partialClient = new Client();
        partialClient.setClientId(TEST_CLIENT_ID);
        partialClient.setImgbbApiKey("test-imgbb-api-key");
        lenient().when(clientRepository.findById(anyLong())).thenReturn(Optional.of(partialClient));

        ClientResponseModel clientResponse = new ClientResponseModel();
        clientResponse.setName("Test Client");
        lenient().when(clientService.getClientById(anyLong())).thenReturn(clientResponse);

        try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                (mock, context) -> {
                    when(mock.uploadFileToImgbb(anyString(), anyString()))
                            .thenReturn(
                                    new ImgbbHelper.ImgbbUploadResponse("https://imgbb.com/test.png", "deleteHash"));
                    when(mock.deleteImage(anyString())).thenReturn(true);
                })) {
            // Act
            BulkInsertResponseModel<Long> result = productService.bulkAddProducts(products);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getTotalRequested());
            assertEquals(1, result.getSuccessCount());
            assertEquals(1, result.getFailureCount());
        }
    }

    /**
     * Purpose: Additional bulk add validation coverage.
     * Expected Result: Null list throws; valid list succeeds; invalid list fails.
     * Assertions: Exceptions and counts are correct.
     */
    @TestFactory
    @DisplayName("Bulk Add Products - Additional scenarios")
    Stream<DynamicTest> bulkAddProducts_AdditionalScenarios() {
        return Stream.of(
                DynamicTest.dynamicTest("Multiple valid products - Success", () -> {
                    List<ProductRequestModel> products = new ArrayList<>();
                    for (int i = 0; i < 2; i++) {
                        ProductRequestModel prodReq = new ProductRequestModel();
                        prodReq.setTitle("Product" + i);
                        prodReq.setDescriptionHtml("Description" + i);
                        prodReq.setBrand("Brand" + i);
                        prodReq.setPrice(BigDecimal.valueOf(100 + i));
                        prodReq.setCategoryId(TEST_CATEGORY_ID);
                        prodReq.setMainImage(TEST_BASE64_IMAGE);
                        prodReq.setTopImage(TEST_BASE64_IMAGE);
                        prodReq.setBottomImage(TEST_BASE64_IMAGE);
                        prodReq.setFrontImage(TEST_BASE64_IMAGE);
                        prodReq.setBackImage(TEST_BASE64_IMAGE);
                        prodReq.setRightImage(TEST_BASE64_IMAGE);
                        prodReq.setLeftImage(TEST_BASE64_IMAGE);
                        prodReq.setDetailsImage(TEST_BASE64_IMAGE);
                        prodReq.setDefectImage(TEST_BASE64_IMAGE);
                        prodReq.setColorLabel(TEST_COLOR_LABEL);
                        prodReq.setCondition(TEST_CONDITION);
                        prodReq.setCountryOfManufacture(TEST_COUNTRY);
                        prodReq.setClientId(TEST_CLIENT_ID);
                        Map<Long, Integer> quantities = new HashMap<>();
                        quantities.put(TEST_PICKUP_LOCATION_ID, 10);
                        prodReq.setPickupLocationQuantities(quantities);
                        products.add(prodReq);
                    }

                    when(productCategoryRepository.findById(TEST_CATEGORY_ID)).thenReturn(Optional.of(testCategory));
                    when(productRepository.save(any(Product.class))).thenReturn(testProduct);
                    lenient().when(productPickupLocationMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
                    lenient().when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });
                    Client productClient = new Client();
                    productClient.setClientId(TEST_CLIENT_ID);
                    productClient.setImgbbApiKey("test-imgbb-api-key");
                    lenient().when(clientRepository.findById(anyLong())).thenReturn(Optional.of(productClient));
                    lenient().when(clientService.getClientById(anyLong())).thenReturn(testClientResponse);

                    try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                            (mock, context) -> {
                                when(mock.uploadFileToImgbb(anyString(), anyString()))
                                        .thenReturn(new ImgbbHelper.ImgbbUploadResponse("https://imgbb.com/test.png", "deleteHash"));
                                when(mock.deleteImage(anyString())).thenReturn(true);
                            })) {
                        BulkInsertResponseModel<Long> result = productService.bulkAddProducts(products);
                        assertNotNull(result);
                        assertEquals(2, result.getTotalRequested());
                        assertEquals(2, result.getSuccessCount());
                        assertEquals(0, result.getFailureCount());
                    }
                }),
                DynamicTest.dynamicTest("Null list - Throws BadRequestException", () -> {
                    BadRequestException ex = assertThrows(BadRequestException.class,
                            () -> productService.bulkAddProducts(null));
                    assertTrue(ex.getMessage().contains("Product list cannot be null or empty"));
                }),
                DynamicTest.dynamicTest("Single invalid product - Failure", () -> {
                    List<ProductRequestModel> products = new ArrayList<>();
                    ProductRequestModel invalid = new ProductRequestModel();
                    invalid.setTitle(null);
                    invalid.setCondition(TEST_CONDITION);
                    invalid.setCountryOfManufacture(TEST_COUNTRY);
                    invalid.setClientId(TEST_CLIENT_ID);
                    invalid.setColorLabel(TEST_COLOR_LABEL);
                    products.add(invalid);

                    BulkInsertResponseModel<Long> result = productService.bulkAddProducts(products);
                    assertNotNull(result);
                    assertEquals(1, result.getTotalRequested());
                    assertEquals(0, result.getSuccessCount());
                    assertEquals(1, result.getFailureCount());
                }),
                DynamicTest.dynamicTest("Single valid product - Success", () -> {
                    List<ProductRequestModel> products = new ArrayList<>();
                    ProductRequestModel prodReq = new ProductRequestModel();
                    prodReq.setTitle("Product");
                    prodReq.setDescriptionHtml("Description");
                    prodReq.setBrand("Brand");
                    prodReq.setPrice(BigDecimal.valueOf(100));
                    prodReq.setCategoryId(TEST_CATEGORY_ID);
                    prodReq.setMainImage(TEST_BASE64_IMAGE);
                    prodReq.setTopImage(TEST_BASE64_IMAGE);
                    prodReq.setBottomImage(TEST_BASE64_IMAGE);
                    prodReq.setFrontImage(TEST_BASE64_IMAGE);
                    prodReq.setBackImage(TEST_BASE64_IMAGE);
                    prodReq.setRightImage(TEST_BASE64_IMAGE);
                    prodReq.setLeftImage(TEST_BASE64_IMAGE);
                    prodReq.setDetailsImage(TEST_BASE64_IMAGE);
                    prodReq.setDefectImage(TEST_BASE64_IMAGE);
                    prodReq.setColorLabel(TEST_COLOR_LABEL);
                    prodReq.setCondition(TEST_CONDITION);
                    prodReq.setCountryOfManufacture(TEST_COUNTRY);
                    prodReq.setClientId(TEST_CLIENT_ID);
                    Map<Long, Integer> quantities = new HashMap<>();
                    quantities.put(TEST_PICKUP_LOCATION_ID, 10);
                    prodReq.setPickupLocationQuantities(quantities);
                    products.add(prodReq);

                    when(productCategoryRepository.findById(TEST_CATEGORY_ID)).thenReturn(Optional.of(testCategory));
                    when(productRepository.save(any(Product.class))).thenReturn(testProduct);
                    lenient().when(productPickupLocationMappingRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
                    lenient().when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });
                    Client productClient = new Client();
                    productClient.setClientId(TEST_CLIENT_ID);
                    productClient.setImgbbApiKey("test-imgbb-api-key");
                    lenient().when(clientRepository.findById(anyLong())).thenReturn(Optional.of(productClient));
                    lenient().when(clientService.getClientById(anyLong())).thenReturn(testClientResponse);

                    try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                            (mock, context) -> {
                                when(mock.uploadFileToImgbb(anyString(), anyString()))
                                        .thenReturn(new ImgbbHelper.ImgbbUploadResponse("https://imgbb.com/test.png", "deleteHash"));
                                when(mock.deleteImage(anyString())).thenReturn(true);
                            })) {
                        BulkInsertResponseModel<Long> result = productService.bulkAddProducts(products);
                        assertNotNull(result);
                        assertEquals(1, result.getTotalRequested());
                        assertEquals(1, result.getSuccessCount());
                        assertEquals(0, result.getFailureCount());
                    }
                })
        );
    }
}
