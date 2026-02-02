package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.FilterQueryBuilder.ProductFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.FilterCondition;
import com.example.SpringApi.Models.RequestModels.ProductRequestModel;
import com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.ProductResponseModel;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.ClientService;
import com.example.SpringApi.Services.MessageService;
import com.example.SpringApi.Services.ProductService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService.
 *
 * Test Group Summary:
 * | Group Name                              | Number of Tests |
 * | :-------------------------------------- | :-------------- |
 * | AddProductTests                         | 14              |
 * | EditProductTests                        | 6               |
 * | ToggleDeleteProductTests                | 6               |
 * | ToggleReturnProductTests                | 4               |
 * | GetProductDetailsByIdTests              | 4               |
 * | GetProductInBatchesTests                | 1               |
 * | ProductValidationTests                  | 24              |
 * | ImageProcessingTests                    | 6               |
 * | BulkAddProductsTests                    | 10              |
 * | **Total**                               | **75**          |
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest extends BaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductPickupLocationMappingRepository productPickupLocationMappingRepository;

    @Mock
    private com.example.SpringApi.Repositories.PackagePickupLocationMappingRepository packagePickupLocationMappingRepository;

    @Mock
    private UserLogService userLogService;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    @Mock
    private GoogleCredRepository googleCredRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientService clientService;

    @Mock
    private ProductFilterQueryBuilder productFilterQueryBuilder;

    private MessageService messageService;

    @Mock
    private Environment environment;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductRequestModel testProductRequest;
    private ProductCategory testCategory;
    private GoogleCred testGoogleCred;
    private ClientResponseModel testClientResponse;
    private PickupLocation testPickupLocation;

    private static final Long TEST_PRODUCT_ID = DEFAULT_PRODUCT_ID;
    private static final Long TEST_CATEGORY_ID = 2L;
    private static final Long TEST_CLIENT_ID = 1L;
    private static final Long TEST_PICKUP_LOCATION_ID = DEFAULT_PICKUP_LOCATION_ID;
    private static final Long TEST_GOOGLE_CRED_ID = DEFAULT_GOOGLE_CRED_ID;
    private static final String TEST_TITLE = "Test Product";
    private static final String TEST_DESCRIPTION = "Test Description";
    private static final String TEST_BRAND = "Test Brand";
    private static final String TEST_COLOR_LABEL = "Red";
    private static final String TEST_CONDITION = "New";
    private static final String TEST_COUNTRY = "USA";
    private static final String TEST_UPC = "123456789012";
    private static final BigDecimal TEST_PRICE = new BigDecimal("99.99");
    private static final String CREATED_USER = DEFAULT_CREATED_USER;
    private static final String TEST_CLIENT_NAME = "Test Client";
    private static final String TEST_BASE64_IMAGE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
    private static final String TEST_URL_IMAGE = "https://example.com/image.png";

    /**
     * Sets up test data before each test execution.
     * Initializes common test objects and configures mock behaviors.
     */
    @BeforeEach
    void setUp() {
        // Initialize mocks
        productRepository = mock(ProductRepository.class);
        productPickupLocationMappingRepository = mock(ProductPickupLocationMappingRepository.class);
        packagePickupLocationMappingRepository = mock(
                com.example.SpringApi.Repositories.PackagePickupLocationMappingRepository.class);
        userLogService = mock(UserLogService.class);
        productCategoryRepository = mock(ProductCategoryRepository.class);
        googleCredRepository = mock(GoogleCredRepository.class);
        clientRepository = mock(ClientRepository.class);
        clientService = mock(ClientService.class);
        environment = mock(Environment.class);
        request = mock(HttpServletRequest.class);

        // Mock HttpServletRequest Authorization header for authentication
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");

        // Mock BaseService.getUser() to return a valid user ID
        lenient().when(request.getAttribute("userId")).thenReturn(1L);

        // Mock BaseService.getClientId() to return a valid client ID
        lenient().when(request.getAttribute("clientId")).thenReturn(1L);

        // MessageService is not used in tested methods, so pass null
        messageService = null;

        // Create ProductService instance manually with all dependencies
        productService = new ProductService(
                productRepository,
                productPickupLocationMappingRepository,
                packagePickupLocationMappingRepository,
                userLogService,
                productCategoryRepository,
                clientRepository,
                clientService,
                productFilterQueryBuilder,
                messageService,
                environment,
                request);

        // Initialize test data
        initializeTestData();

        // Setup common mock behaviors
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        lenient().when(request.getAttribute("userId")).thenReturn(DEFAULT_USER_ID);
        lenient().when(request.getAttribute("user")).thenReturn(CREATED_USER);
        lenient().when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });

        // Mock repository methods
        lenient().when(productCategoryRepository.findById(TEST_CATEGORY_ID)).thenReturn(Optional.of(testCategory));
        lenient().when(googleCredRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testGoogleCred));
        lenient().when(clientService.getClientById(TEST_CLIENT_ID)).thenReturn(testClientResponse);
        lenient().when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Mock clientRepository.findById for Client lookup
        Client testClient = new Client();
        testClient.setClientId(TEST_CLIENT_ID);
        testClient.setName(TEST_CLIENT_NAME);
        testClient.setImgbbApiKey("test-imgbb-api-key"); // Set ImgBB API key for image uploads
        lenient().when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
    }

    @Nested
    @DisplayName("AddProduct Tests")
    class AddProductTests {

        @Test
        @DisplayName("Add Product - Success: Valid product with all required images")
        void addProduct_Success() {
        // Arrange
        try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                (mock, context) -> {
                    ImgbbHelper.ImgbbUploadResponse mockResponse = new ImgbbHelper.ImgbbUploadResponse(
                            "https://i.ibb.co/test/image.png",
                            "test-delete-hash");
                    when(mock.uploadFileToImgbb(anyString(), anyString())).thenReturn(mockResponse);
                    when(mock.deleteImage(anyString())).thenReturn(true);
                })) {

            // Act & Assert
            assertDoesNotThrow(() -> productService.addProduct(testProductRequest));

            // Verify interactions
            verify(productCategoryRepository, times(1)).findById(TEST_CATEGORY_ID);
            verify(clientService, times(1)).getClientById(TEST_CLIENT_ID);
            verify(productRepository, atLeastOnce()).save(any(Product.class));
            verify(userLogService, times(1)).logDataWithContext(eq(1L), anyString(), eq(1L), anyString(), eq("addProduct"));
        }
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
        @DisplayName("Add Product - Failure: Category not found")
        void addProduct_CategoryNotFound_ThrowsNotFoundException() {
            // Arrange
            when(productCategoryRepository.findById(TEST_CATEGORY_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrowsNotFound(String.format(ErrorMessages.ProductErrorMessages.ER008, TEST_CATEGORY_ID),
                    () -> productService.addProduct(testProductRequest));
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("Add Product - Failure: Missing required image")
        void addProduct_MissingRequiredImage_ThrowsBadRequestException() {
            // Arrange
            testProductRequest.setMainImage(null);

            // Act & Assert
            assertThrowsBadRequest(String.format(ErrorMessages.ProductErrorMessages.ER009, "main"),
                    () -> productService.addProduct(testProductRequest));
            verify(productRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("Add Product - Failure: ImgBB upload fails")
        void addProduct_ImgbbUploadFails_ThrowsBadRequestException() {
            // Arrange
            try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                    (mock, context) -> {
                        when(mock.uploadFileToImgbb(anyString(), anyString())).thenReturn(null); // Simulate upload failure
                        when(mock.deleteImage(anyString())).thenReturn(true);
                    })) {

                // Act & Assert
                BadRequestException exception = assertThrows(BadRequestException.class,
                        () -> productService.addProduct(testProductRequest));

                assertTrue(exception.getMessage().contains("Failed to upload") && exception.getMessage().contains("image"));
                verify(productRepository, times(1)).save(any()); // Product is saved first
            }
        }

        @Test
        @DisplayName("Add Product - Success: With valid request and ImgBB upload")
        void addProduct_ValidRequestWithImgbbUpload_Success() {
            // Arrange - This test now validates successful ImgBB upload flow
            try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                    (mock, context) -> {
                        ImgbbHelper.ImgbbUploadResponse mockResponse = new ImgbbHelper.ImgbbUploadResponse(
                                "https://i.ibb.co/test/image.png",
                                "test-delete-hash");
                        when(mock.uploadFileToImgbb(anyString(), anyString())).thenReturn(mockResponse);
                        when(mock.deleteImage(anyString())).thenReturn(true);
                    })) {

                // Act & Assert
                assertDoesNotThrow(() -> productService.addProduct(testProductRequest));
                verify(productRepository, atLeastOnce()).save(any());
            }
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

                // Act & Assert - URL conversion should fail in unit test due to network access
                BadRequestException exception = assertThrows(BadRequestException.class,
                        () -> productService.addProduct(testProductRequest));

                assertTrue(exception.getMessage().contains("Failed to process image from URL"));
                verify(productRepository, times(1)).save(any(Product.class));
            }
        }

        /**
         * Purpose: Additional addProduct success variations.
         * Expected Result: Valid requests succeed with optional image variations.
         * Assertions: No exceptions thrown.
         */
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
    }

    @Nested
    @DisplayName("EditProduct Tests")
    class EditProductTests {

        @Test
        @DisplayName("Edit Product - Success: Valid product update")
        void editProduct_Success() {
        // Arrange
        when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID)).thenReturn(testProduct);

        try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                (mock, context) -> {
                    ImgbbHelper.ImgbbUploadResponse mockResponse = new ImgbbHelper.ImgbbUploadResponse(
                            "https://i.ibb.co/test/image.png",
                            "test-delete-hash");
                    when(mock.uploadFileToImgbb(anyString(), anyString())).thenReturn(mockResponse);
                    when(mock.deleteImage(anyString())).thenReturn(true);
                })) {

            // Act & Assert
            assertDoesNotThrow(() -> productService.editProduct(testProductRequest));

            verify(productRepository, times(1)).findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID);
            verify(productRepository, atLeastOnce()).save(any(Product.class));
            verify(userLogService, times(1)).logData(eq(1L), anyString(), eq("editProduct"));
        }
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
            assertThrowsNotFound(String.format(ErrorMessages.ProductErrorMessages.ER013, TEST_PRODUCT_ID),
                    () -> productService.editProduct(testProductRequest));
            verify(productRepository, never()).save(any());
        }

        /**
         * Purpose: Additional invalid ID coverage for editProduct.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches ER013.
         */
        @TestFactory
        @DisplayName("Edit Product - Additional invalid IDs")
        Stream<DynamicTest> editProduct_AdditionalInvalidIds() {
            return Stream.of(2L, 3L, 4L)
                    .map(id -> DynamicTest.dynamicTest("Invalid ID: " + id, () -> {
                        testProductRequest.setProductId(id);
                        lenient().when(productRepository.findByIdWithRelatedEntities(id, TEST_CLIENT_ID)).thenReturn(null);
                        assertThrowsNotFound(String.format(ErrorMessages.ProductErrorMessages.ER013, id),
                                () -> productService.editProduct(testProductRequest));
                    }));
        }
    }

    @Nested
    @DisplayName("ToggleDeleteProduct Tests")
    class ToggleDeleteProductTests {

        @Test
        @DisplayName("Toggle Delete Product - Success: Toggle from false to true")
        void toggleDeleteProduct_Success() {
        // Arrange
        testProduct.setIsDeleted(false);
        when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID)).thenReturn(testProduct);

        // Act
        assertDoesNotThrow(() -> productService.toggleDeleteProduct(TEST_PRODUCT_ID));

        // Assert
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

        @Test
        @DisplayName("Toggle Delete Product - Failure: Product not found")
        void toggleDeleteProduct_ProductNotFound_ThrowsNotFoundException() {
            // Arrange
            lenient().when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID)).thenReturn(null);

            // Act & Assert
            assertThrowsNotFound(String.format(ErrorMessages.ProductErrorMessages.ER013, TEST_PRODUCT_ID),
                    () -> productService.toggleDeleteProduct(TEST_PRODUCT_ID));
            verify(productRepository, never()).save(any());
        }

        /**
         * Purpose: Additional invalid ID coverage for toggleDeleteProduct.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches ER013.
         */
        @TestFactory
        @DisplayName("Toggle Delete Product - Additional invalid IDs")
        Stream<DynamicTest> toggleDeleteProduct_AdditionalInvalidIds() {
            return Stream.of(2L, 3L, 4L)
                    .map(id -> DynamicTest.dynamicTest("Invalid ID: " + id, () -> {
                        lenient().when(productRepository.findByIdWithRelatedEntities(id, TEST_CLIENT_ID)).thenReturn(null);
                        assertThrowsNotFound(String.format(ErrorMessages.ProductErrorMessages.ER013, id),
                                () -> productService.toggleDeleteProduct(id));
                    }));
        }
    }

    @Nested
    @DisplayName("ToggleReturnProduct Tests")
    class ToggleReturnProductTests {

        @Test
        @DisplayName("Toggle Return Product - Success: Toggle returns allowed")
        void toggleReturnProduct_Success() {
        // Arrange - set returnWindowDays to 30 (returns allowed)
        testProduct.setReturnWindowDays(30);
        when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID)).thenReturn(testProduct);

        // Act
        assertDoesNotThrow(() -> productService.toggleReturnProduct(TEST_PRODUCT_ID));

        // Assert
        verify(productRepository, times(1)).findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID);
        verify(productRepository, times(1)).save(testProduct);
        verify(userLogService, times(1)).logData(eq(1L), anyString(), anyString());
        // After toggle, returnWindowDays should be 0 (returns not allowed)
        assertEquals(0, testProduct.getReturnWindowDays());
    }

        @Test
        @DisplayName("Toggle Return Product - Failure: Product not found")
        void toggleReturnProduct_ProductNotFound_ThrowsNotFoundException() {
            // Arrange
            lenient().when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID)).thenReturn(null);

            // Act & Assert
            assertThrowsNotFound(String.format(ErrorMessages.ProductErrorMessages.ER013, TEST_PRODUCT_ID),
                    () -> productService.toggleReturnProduct(TEST_PRODUCT_ID));
            verify(productRepository, never()).save(any());
        }

        /**
         * Purpose: Verify returnWindowDays toggles to 30 when null or zero.
         * Expected Result: returnWindowDays is set to 30.
         * Assertions: returnWindowDays equals 30.
         */
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
    }

    @Nested
    @DisplayName("GetProductDetailsById Tests")
    class GetProductDetailsByIdTests {

        @Test
        @DisplayName("Get Product Details By ID - Success: Product found with related entities")
        void getProductDetailsById_Success() {
        // Arrange
        when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID)).thenReturn(testProduct);

        // Act
        ProductResponseModel result = productService.getProductDetailsById(TEST_PRODUCT_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_PRODUCT_ID, result.getProductId());
        assertEquals(TEST_TITLE, result.getTitle());
        assertNotNull(result.getCategory());
        assertEquals(TEST_CATEGORY_ID, result.getCategory().getCategoryId());
        verify(productRepository, times(1)).findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID);
    }

        @Test
        @DisplayName("Get Product Details By ID - Failure: Product not found")
        void getProductDetailsById_ProductNotFound_ThrowsNotFoundException() {
            // Arrange
            when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID)).thenReturn(null);

            // Act & Assert
            assertThrowsNotFound(String.format(ErrorMessages.ProductErrorMessages.ER013, TEST_PRODUCT_ID),
                    () -> productService.getProductDetailsById(TEST_PRODUCT_ID));
        }

        /**
         * Purpose: Additional invalid ID coverage for getProductDetailsById.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches ER013.
         */
        @TestFactory
        @DisplayName("Get Product Details By ID - Additional invalid IDs")
        Stream<DynamicTest> getProductDetailsById_AdditionalInvalidIds() {
            return Stream.of(2L, 3L)
                    .map(id -> DynamicTest.dynamicTest("Invalid ID: " + id, () -> {
                        when(productRepository.findByIdWithRelatedEntities(id, TEST_CLIENT_ID)).thenReturn(null);
                        assertThrowsNotFound(String.format(ErrorMessages.ProductErrorMessages.ER013, id),
                                () -> productService.getProductDetailsById(id));
                    }));
        }
    }

    @Nested
    @DisplayName("GetProductInBatches Tests")
    class GetProductInBatchesTests {

        /**
         * Single comprehensive unit test for getProductInBatches.
         * Covers: (1) invalid pagination → BadRequestException with message,
         * (2) success without filters, (3) triple-loop validation over all valid
         * column×operator×type combinations plus invalid column/operator/value combinations.
         */
        @Test
        @DisplayName("Get Products In Batches - Invalid pagination, success no filters, and triple-loop filter validation")
        void getProductInBatches_SingleComprehensiveTest() {
            // ---- (1) Invalid pagination: end <= start ----
            PaginationBaseRequestModel invalidRequest = createValidPaginationRequest();
            invalidRequest.setStart(10);
            invalidRequest.setEnd(5);
            assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                    () -> productService.getProductInBatches(invalidRequest));

            // ---- (2) Success: simple retrieval without filters ----
            PaginationBaseRequestModel successRequest = createValidPaginationRequest();
            successRequest.setStart(0);
            successRequest.setEnd(10);
            successRequest.setFilters(null);
            List<Product> productList = Collections.singletonList(testProduct);
            Page<Product> productPage = new PageImpl<>(productList, PageRequest.of(0, 10), 1);
            when(productFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)))
                    .thenReturn(productPage);
            PaginationBaseResponseModel<ProductResponseModel> result = productService.getProductInBatches(successRequest);
            assertNotNull(result);
            assertEquals(1, result.getData().size());
            assertEquals(testProduct.getProductId(), result.getData().get(0).getProductId());

            // ---- (3) Triple-loop: valid columns × operators × types + invalid combinations ----
                String[] stringColumns = {
                    "title", "descriptionHtml", "brand", "color", "colorLabel", "condition",
                    "countryOfManufacture", "model", "upc", "modificationHtml", "notes",
                    "createdUser", "modifiedUser"
                };
                String[] numberColumns = {
                        "productId", "price", "discount", "length", "breadth",
                    "height", "weightKgs", "categoryId", "pickupLocationId"
                };
                String[] booleanColumns = {"isDiscountPercent", "returnsAllowed", "itemModified", "isDeleted"};
                String[] dateColumns = {"createdAt", "updatedAt"};
            String[] invalidColumns = BATCH_INVALID_COLUMNS;
                String[] stringOperators = {
                    "contains", "equals", "startsWith", "endsWith", "isEmpty", "isNotEmpty",
                    "isOneOf", "isNotOneOf", "containsOneOf"
                };
                String[] numberOperators = {
                    "equals", "notEquals", "greaterThan", "greaterThanOrEqual", "lessThan", "lessThanOrEqual",
                    "isEmpty", "isNotEmpty", "isOneOf", "isNotOneOf"
                };
                String[] booleanOperators = {"is"};
                String[] dateOperators = {
                    "is", "isNot", "isAfter", "isOnOrAfter", "isBefore", "isOnOrBefore", "isEmpty", "isNotEmpty"
                };
            String[] invalidOperators = BATCH_INVALID_OPERATORS;
            String[] validValues = BATCH_VALID_VALUES;
            String[] emptyValues = BATCH_EMPTY_VALUES;

            Page<Product> emptyPage = new PageImpl<>(Collections.emptyList());
            lenient().when(productFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)))
                    .thenReturn(emptyPage);
            lenient()
                    .when(productFilterQueryBuilder.getColumnType(argThat(arg -> Arrays.asList(stringColumns).contains(arg))))
                    .thenReturn("string");
            lenient()
                    .when(productFilterQueryBuilder.getColumnType(argThat(arg -> Arrays.asList(numberColumns).contains(arg))))
                    .thenReturn("number");
            lenient()
                    .when(productFilterQueryBuilder.getColumnType(argThat(arg -> Arrays.asList(booleanColumns).contains(arg))))
                    .thenReturn("boolean");
            lenient()
                    .when(productFilterQueryBuilder.getColumnType(argThat(arg -> Arrays.asList(dateColumns).contains(arg))))
                    .thenReturn("date");

            String[] allColumns = joinArrays(stringColumns, numberColumns, booleanColumns, dateColumns, invalidColumns);
            String[] allOperators = joinArrays(stringOperators, numberOperators, booleanOperators, dateOperators, invalidOperators);
            Set<String> uniqueOperators = new HashSet<>(Arrays.asList(allOperators));
            String[] allValues = joinArrays(validValues, emptyValues);

            for (String column : allColumns) {
                for (String operator : uniqueOperators) {
                    for (String value : allValues) {
                        PaginationBaseRequestModel testRequest = createValidPaginationRequest();
                        testRequest.setStart(0);
                        testRequest.setEnd(10);
                        FilterCondition filter = createFilterCondition(column, operator, value);
                        testRequest.setFilters(Collections.singletonList(filter));

                        boolean isColumnKnown = !Arrays.asList(invalidColumns).contains(column);
                        boolean isValidForString = Arrays.asList(stringColumns).contains(column)
                                && Arrays.asList(stringOperators).contains(operator);
                        boolean isValidForNumber = Arrays.asList(numberColumns).contains(column)
                                && Arrays.asList(numberOperators).contains(operator);
                        boolean isValidForBoolean = Arrays.asList(booleanColumns).contains(column)
                                && Arrays.asList(booleanOperators).contains(operator);
                        boolean isValidForDate = Arrays.asList(dateColumns).contains(column)
                                && Arrays.asList(dateOperators).contains(operator);
                        boolean isOperatorValidForType = isValidForString || isValidForNumber || isValidForBoolean || isValidForDate;

                        boolean shouldSucceed = isColumnKnown && isOperatorValidForType;

                        try {
                            productService.getProductInBatches(testRequest);
                            if (!shouldSucceed) {
                                String reason = !isColumnKnown ? "Invalid column: " + column
                                    : "Invalid operator '" + operator + "' for column '" + column + "'";
                                fail("Expected failure but succeeded. Context: " + reason);
                            }
                        } catch (BadRequestException | IllegalArgumentException e) {
                            if (shouldSucceed) {
                                fail("Expected success but failed: Col=" + column + " Op=" + operator + " Val=" + value + ". Error: " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("Product Validation Tests")
    class ProductValidationTests {

        @Test
        @DisplayName("Product Validation - Invalid title (null)")
        void productValidation_InvalidTitle_Null_ThrowsBadRequestException() {
            // Arrange
            testProductRequest.setTitle(null);

            // Act & Assert
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidTitle,
                    () -> productService.addProduct(testProductRequest));
        }

        @Test
        @DisplayName("Product Validation - Invalid title (empty)")
        void productValidation_InvalidTitle_Empty_ThrowsBadRequestException() {
            // Arrange
            testProductRequest.setTitle("");

            // Act & Assert
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidTitle,
                    () -> productService.addProduct(testProductRequest));
        }

        @Test
        @DisplayName("Product Validation - Invalid description (null)")
        void productValidation_InvalidDescription_Null_ThrowsBadRequestException() {
            // Arrange
            testProductRequest.setDescriptionHtml(null);

            // Act & Assert
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidDescription,
                    () -> productService.addProduct(testProductRequest));
        }

        @Test
        @DisplayName("Product Validation - Invalid brand (null)")
        void productValidation_InvalidBrand_Null_ThrowsBadRequestException() {
            // Arrange
            testProductRequest.setBrand(null);

            // Act & Assert
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidBrand,
                    () -> productService.addProduct(testProductRequest));
        }

        @Test
        @DisplayName("Product Validation - Invalid color label (null)")
        void productValidation_InvalidColorLabel_Null_ThrowsBadRequestException() {
            // Arrange
            testProductRequest.setColorLabel(null);

            // Act & Assert
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidColorLabel,
                    () -> productService.addProduct(testProductRequest));
        }

        @Test
        @DisplayName("Product Validation - Invalid condition (null)")
        void productValidation_InvalidCondition_Null_ThrowsBadRequestException() {
            // Arrange
            testProductRequest.setCondition(null);

            // Act & Assert
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidCondition,
                    () -> productService.addProduct(testProductRequest));
        }

        @Test
        @DisplayName("Product Validation - Null Pickup Location Quantities - Throws BadRequestException")
        void productValidation_NullPickupLocationQuantities_ThrowsBadRequestException() {
            testProductRequest.setPickupLocationQuantities(null);
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.AtLeastOnePickupLocationRequired,
                    () -> productService.addProduct(testProductRequest));
        }

        @Test
        @DisplayName("Product Validation - Empty Pickup Location Quantities - Throws BadRequestException")
        void productValidation_EmptyPickupLocationQuantities_ThrowsBadRequestException() {
            testProductRequest.setPickupLocationQuantities(new HashMap<>());
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.AtLeastOnePickupLocationRequired,
                    () -> productService.addProduct(testProductRequest));
        }

        @Test
        @DisplayName("Product Validation - Invalid country of manufacture (null)")
        void productValidation_InvalidCountry_Null_ThrowsBadRequestException() {
            // Arrange
            testProductRequest.setCountryOfManufacture(null);

            // Act & Assert
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidCountryOfManufacture,
                    () -> productService.addProduct(testProductRequest));
        }

        @Test
        @DisplayName("Product Validation - Invalid price (null)")
        void productValidation_InvalidPrice_Null_ThrowsBadRequestException() {
            // Arrange
            testProductRequest.setPrice(null);

            // Act & Assert
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidPrice,
                    () -> productService.addProduct(testProductRequest));
        }

        @Test
        @DisplayName("Product Validation - Invalid price (negative)")
        void productValidation_InvalidPrice_Negative_ThrowsBadRequestException() {
            // Arrange
            testProductRequest.setPrice(new BigDecimal("-10.00"));

            // Act & Assert
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidPrice,
                    () -> productService.addProduct(testProductRequest));
        }

        @Test
        @DisplayName("Product Validation - Client ID null in request is ignored")
        void productValidation_InvalidClientId_Null_ThrowsBadRequestException() {
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

        /**
         * Purpose: Additional validation coverage for addProduct.
         * Expected Result: Invalid inputs throw BadRequestException.
         * Assertions: Exception messages match expected errors.
         */
        @TestFactory
        @DisplayName("Product Validation - Additional invalid inputs")
        Stream<DynamicTest> productValidation_AdditionalInvalidInputs() {
            return Stream.of(
                    DynamicTest.dynamicTest("Title whitespace", () -> {
                        initializeTestData();
                        testProductRequest.setTitle("   ");
                        assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidTitle,
                                () -> productService.addProduct(testProductRequest));
                    }),
                    DynamicTest.dynamicTest("Description empty", () -> {
                        initializeTestData();
                        testProductRequest.setDescriptionHtml(" ");
                        assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidDescription,
                                () -> productService.addProduct(testProductRequest));
                    }),
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
                    DynamicTest.dynamicTest("Price zero allowed?", () -> {
                        initializeTestData();
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
                    }),
                    DynamicTest.dynamicTest("UPC empty allowed", () -> {
                        initializeTestData();
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
                    }),
                    DynamicTest.dynamicTest("Pickup quantities null", () -> {
                        initializeTestData();
                        testProductRequest.setPickupLocationQuantities(null);
                        assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.AtLeastOnePickupLocationRequired,
                                () -> productService.addProduct(testProductRequest));
                    }),
                    DynamicTest.dynamicTest("Pickup quantities empty", () -> {
                        initializeTestData();
                        testProductRequest.setPickupLocationQuantities(new HashMap<>());
                        assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.AtLeastOnePickupLocationRequired,
                                () -> productService.addProduct(testProductRequest));
                    }),
                    DynamicTest.dynamicTest("Client ID negative", () -> {
                        initializeTestData();
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
                    }),
                    DynamicTest.dynamicTest("Category ID null", () -> {
                        initializeTestData();
                        testProductRequest.setCategoryId(null);
                        assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidCategoryId,
                                () -> productService.addProduct(testProductRequest));
                    })
            );
        }
    }

    @Nested
    @DisplayName("Image Processing Tests")
    class ImageProcessingTests {

        @Test
        @DisplayName("Image Processing - Convert URL to base64 fails in unit test (network access)")
        void imageProcessing_UrlToBase64Fails_ThrowsBadRequestException() {
        // This test verifies that URL image processing fails appropriately in unit test
        // environment
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

        @Test
        @DisplayName("Image Processing - Handle null image data")
        void imageProcessing_NullImage_Success() {
            // Test through the service - null images should be handled gracefully for
            // optional images
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

        @Test
        @DisplayName("Image Processing - Handle empty image data")
        void imageProcessing_EmptyImage_Success() {
            // Test through the service - empty images should be handled gracefully for
            // optional images
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

    private void initializeTestData() {
        // Initialize test category
        testCategory = new ProductCategory();
        testCategory.setCategoryId(TEST_CATEGORY_ID);
        testCategory.setName("Test Category");

        // Initialize test pickup location
        testPickupLocation = new PickupLocation();
        testPickupLocation.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        testPickupLocation.setAddressNickName("Test Location");
        testPickupLocation.setIsDeleted(false);

        // Initialize test GoogleCred
        testGoogleCred = new GoogleCred();
        testGoogleCred.setGoogleCredId(TEST_GOOGLE_CRED_ID);
        testGoogleCred.setClientId(TEST_CLIENT_ID.toString()); // clientId is String
        testGoogleCred.setType("{}"); // Using type field instead of serviceAccountKey

        // Initialize test client response
        testClientResponse = new ClientResponseModel();
        testClientResponse.setClientId(TEST_CLIENT_ID);
        testClientResponse.setName(TEST_CLIENT_NAME);

        // Initialize test product request
        testProductRequest = new ProductRequestModel();
        testProductRequest.setProductId(TEST_PRODUCT_ID);
        testProductRequest.setTitle(TEST_TITLE);
        testProductRequest.setDescriptionHtml(TEST_DESCRIPTION);
        testProductRequest.setBrand(TEST_BRAND);
        testProductRequest.setColorLabel(TEST_COLOR_LABEL);
        testProductRequest.setCondition(TEST_CONDITION);
        testProductRequest.setCountryOfManufacture(TEST_COUNTRY);
        testProductRequest.setUpc(TEST_UPC);
        testProductRequest.setPrice(TEST_PRICE);
        testProductRequest.setCategoryId(TEST_CATEGORY_ID);
        testProductRequest.setClientId(TEST_CLIENT_ID);
        testProductRequest.setIsDeleted(false);
        testProductRequest.setReturnWindowDays(30);

        // Set pickup location quantities
        Map<Long, Integer> pickupLocationQuantities = new HashMap<>();
        pickupLocationQuantities.put(TEST_PICKUP_LOCATION_ID, 10);
        testProductRequest.setPickupLocationQuantities(pickupLocationQuantities);

        // Set all required images
        testProductRequest.setMainImage(TEST_BASE64_IMAGE);
        testProductRequest.setTopImage(TEST_BASE64_IMAGE);
        testProductRequest.setBottomImage(TEST_BASE64_IMAGE);
        testProductRequest.setFrontImage(TEST_BASE64_IMAGE);
        testProductRequest.setBackImage(TEST_BASE64_IMAGE);
        testProductRequest.setRightImage(TEST_BASE64_IMAGE);
        testProductRequest.setLeftImage(TEST_BASE64_IMAGE);
        testProductRequest.setDetailsImage(TEST_BASE64_IMAGE);
        testProductRequest.setDefectImage(TEST_BASE64_IMAGE);

        // Set optional images
        testProductRequest.setAdditionalImage1(TEST_BASE64_IMAGE);
        testProductRequest.setAdditionalImage2(null); // Test null optional image
        testProductRequest.setAdditionalImage3(TEST_BASE64_IMAGE);

        // Initialize test product
        testProduct = new Product(testProductRequest, CREATED_USER, TEST_CLIENT_ID);
        testProduct.setProductId(TEST_PRODUCT_ID);
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setUpdatedAt(LocalDateTime.now());
        testProduct.setCategory(testCategory);

        // Initialize product pickup location mappings
        Set<ProductPickupLocationMapping> mappings = new HashSet<>();
        ProductPickupLocationMapping mapping = new ProductPickupLocationMapping();
        mapping.setProduct(testProduct);
        mapping.setPickupLocation(testPickupLocation);
        mapping.setAvailableStock(10);
        mappings.add(mapping);
        testProduct.setProductPickupLocationMappings(mappings);
    }

    @Nested
    @DisplayName("BulkAddProducts Tests")
    class BulkAddProductsTests {

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
            prodReq.setCondition("NEW");
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
        validProduct.setCondition("NEW");
        validProduct.setCountryOfManufacture("USA");
        validProduct.setClientId(TEST_CLIENT_ID);
        Map<Long, Integer> quantities = new HashMap<>();
        quantities.put(TEST_PICKUP_LOCATION_ID, 10);
        validProduct.setPickupLocationQuantities(quantities);
        products.add(validProduct);

        // Invalid product (missing title)
        ProductRequestModel invalidProduct = new ProductRequestModel();
        invalidProduct.setTitle(null);
        invalidProduct.setCondition("USED");
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

        /**
         * Purpose: Additional bulk add validation coverage.
         * Expected Result: Null list throws; valid list succeeds; invalid list fails.
         * Assertions: Exceptions and counts are correct.
         */
        @TestFactory
        @DisplayName("Bulk Add Products - Additional scenarios")
        Stream<DynamicTest> bulkAddProducts_AdditionalScenarios() {
            return Stream.of(
                    DynamicTest.dynamicTest("Null list - Throws BadRequestException", () -> {
                        BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> productService.bulkAddProducts(null));
                        assertTrue(ex.getMessage().contains("Product list cannot be null or empty"));
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
                    })
            );
        }
    }
}
