package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.FilterQueryBuilder.ProductFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
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
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.ErrorMessages;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService.
 *
 * This test class provides comprehensive coverage of ProductService methods
 * including:
 * - CRUD operations (create, read, update, toggle)
 * - Product retrieval by ID and batch operations
 * - Image processing and Firebase integration
 * - Validation and error handling
 * - Pagination and filtering
 *
 * Each test method follows the AAA (Arrange-Act-Assert) pattern and includes
 * both success and failure scenarios to ensure robust error handling.
 * All external dependencies are properly mocked to ensure test isolation.
 *
 * @author SpringApi Team
 * @version 2.0
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

    private static final Long TEST_PRODUCT_ID = 1L;
    private static final Long TEST_CATEGORY_ID = 2L;
    private static final Long TEST_PICKUP_LOCATION_ID = 3L;
    private static final Long TEST_GOOGLE_CRED_ID = 1L;
    private static final String TEST_TITLE = "Test Product";
    private static final String TEST_DESCRIPTION = "Test Description";
    private static final String TEST_BRAND = "Test Brand";
    private static final String TEST_COLOR_LABEL = "Red";
    private static final String TEST_CONDITION = "New";
    private static final String TEST_COUNTRY = "USA";
    private static final String TEST_UPC = "123456789012";
    private static final BigDecimal TEST_PRICE = new BigDecimal("99.99");
    private static final String TEST_BASE64_IMAGE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
    private static final String TEST_URL_IMAGE = "https://example.com/image.png";

    /**
     * Sets up test data before each test execution.
     * Initializes common test objects and configures mock behaviors.
     */
    @BeforeEach
    void setUp() {
        // Create ProductService instance manually with all dependencies if needed, or use @InjectMocks
        // Re-injecting mocks manually to ensure all dependencies are set if @InjectMocks misses any (like messageService which is null)
        messageService = null;
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

        // Mock BaseService behavior
        lenient().doReturn(DEFAULT_CLIENT_ID).when(productService).getClientId();
        lenient().doReturn(DEFAULT_USER_ID).when(productService).getUserId();
        lenient().doReturn(DEFAULT_LOGIN_NAME).when(productService).getUser();

        lenient().when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });

        // Mock repository methods
        lenient().when(productCategoryRepository.findById(TEST_CATEGORY_ID)).thenReturn(Optional.of(testCategory));
        lenient().when(googleCredRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testGoogleCred));
        lenient().when(clientService.getClientById(DEFAULT_CLIENT_ID)).thenReturn(testClientResponse);
        lenient().when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Mock clientRepository.findById for Client lookup
        Client testClient = createTestClient(DEFAULT_CLIENT_ID);
        testClient.setImgbbApiKey("test-imgbb-api-key"); // Set ImgBB API key for image uploads
        lenient().when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
    }

    // ==================== addProduct Tests ====================

    @Nested
    @DisplayName("addProduct Tests")
    class AddProductTests {

        @Test
        @DisplayName("addProduct - Success: Valid product with all required images")
        void testAddProduct_Success() {
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
                verify(clientService, times(1)).getClientById(DEFAULT_CLIENT_ID);
                verify(productRepository, atLeastOnce()).save(any(Product.class));
                verify(userLogService, times(1)).logDataWithContext(anyLong(), anyString(), anyLong(), anyString(), anyString());
            }
        }

        @Test
        @DisplayName("addProduct - Failure: Null category ID")
        void testAddProduct_NullCategoryId() {
            // Arrange
            testProductRequest.setCategoryId(null);

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> productService.addProduct(testProductRequest));

            assertEquals(ErrorMessages.ProductErrorMessages.InvalidCategoryId, exception.getMessage());
            verify(productCategoryRepository, never()).findById(any());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("addProduct - Failure: Category not found")
        void testAddProduct_CategoryNotFound() {
            // Arrange
            when(productCategoryRepository.findById(TEST_CATEGORY_ID)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> productService.addProduct(testProductRequest));

            assertEquals(String.format(ErrorMessages.ProductErrorMessages.ER008, TEST_CATEGORY_ID), exception.getMessage());
            verify(productRepository, never()).save(any());
        }

        @Test
        @DisplayName("addProduct - Failure: Missing required image")
        void testAddProduct_MissingRequiredImage() {
            // Arrange
            testProductRequest.setMainImage(null);

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> productService.addProduct(testProductRequest));

            assertEquals(String.format(ErrorMessages.ProductErrorMessages.ER009, "main"), exception.getMessage());
            verify(productRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("addProduct - Failure: ImgBB upload fails")
        void testAddProduct_ImgBBUploadFails() {
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
        @DisplayName("addProduct - Failure: URL image conversion fails (network error)")
        void testAddProduct_UrlImageConversion() {
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
    }

    // ==================== editProduct Tests ====================

    @Nested
    @DisplayName("editProduct Tests")
    class EditProductTests {

        @Test
        @DisplayName("editProduct - Success: Valid product update")
        void testEditProduct_Success() {
            // Arrange
            when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, DEFAULT_CLIENT_ID)).thenReturn(testProduct);

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

                verify(productRepository, times(1)).findByIdWithRelatedEntities(TEST_PRODUCT_ID, DEFAULT_CLIENT_ID);
                verify(productRepository, atLeastOnce()).save(any(Product.class));
                verify(userLogService, times(1)).logData(eq(DEFAULT_USER_ID), anyString(), eq("editProduct"));
            }
        }

        @Test
        @DisplayName("editProduct - Failure: Null product ID")
        void testEditProduct_NullProductId() {
            // Arrange
            testProductRequest.setProductId(null);

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> productService.editProduct(testProductRequest));

            assertEquals(ErrorMessages.ProductErrorMessages.InvalidId, exception.getMessage());
            verify(productRepository, never()).findByIdWithRelatedEntities(anyLong(), anyLong());
        }

        @Test
        @DisplayName("editProduct - Failure: Product not found")
        void testEditProduct_ProductNotFound() {
            // Arrange
            lenient().when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, DEFAULT_CLIENT_ID)).thenReturn(null);

            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> productService.editProduct(testProductRequest));

            assertEquals(String.format(ErrorMessages.ProductErrorMessages.ER013, TEST_PRODUCT_ID), exception.getMessage());
            verify(productRepository, never()).save(any());
        }
    }

    // ==================== toggleDeleteProduct Tests ====================

    @Nested
    @DisplayName("toggleDeleteProduct Tests")
    class ToggleDeleteProductTests {

        @Test
        @DisplayName("toggleDeleteProduct - Success: Toggle from false to true")
        void testToggleDeleteProduct_Success() {
            // Arrange
            testProduct.setIsDeleted(false);
            when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, DEFAULT_CLIENT_ID)).thenReturn(testProduct);

            // Act
            assertDoesNotThrow(() -> productService.toggleDeleteProduct(TEST_PRODUCT_ID));

            // Assert
            verify(productRepository, times(1)).findByIdWithRelatedEntities(TEST_PRODUCT_ID, DEFAULT_CLIENT_ID);
            verify(productRepository, times(1)).save(testProduct);
            verify(userLogService, times(1)).logData(eq(DEFAULT_USER_ID), anyString(), anyString());
            assertTrue(testProduct.getIsDeleted());
        }

        @Test
        @DisplayName("toggleDeleteProduct - Success: Toggle from true to false")
        void testToggleDeleteProduct_Success_Undelete() {
            // Arrange
            testProduct.setIsDeleted(true);
            when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, DEFAULT_CLIENT_ID)).thenReturn(testProduct);

            // Act
            assertDoesNotThrow(() -> productService.toggleDeleteProduct(TEST_PRODUCT_ID));

            // Assert
            assertFalse(testProduct.getIsDeleted());
        }

        @Test
        @DisplayName("toggleDeleteProduct - Failure: Product not found")
        void testToggleDeleteProduct_ProductNotFound() {
            // Arrange
            lenient().when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, DEFAULT_CLIENT_ID)).thenReturn(null);

            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> productService.toggleDeleteProduct(TEST_PRODUCT_ID));

            assertEquals(String.format(ErrorMessages.ProductErrorMessages.ER013, TEST_PRODUCT_ID), exception.getMessage());
            verify(productRepository, never()).save(any());
        }
    }

    // ==================== toggleReturnProduct Tests ====================

    @Nested
    @DisplayName("toggleReturnProduct Tests")
    class ToggleReturnProductTests {

        @Test
        @DisplayName("toggleReturnProduct - Success: Toggle returns allowed")
        void testToggleReturnProduct_Success() {
            // Arrange - set returnWindowDays to 30 (returns allowed)
            testProduct.setReturnWindowDays(30);
            when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, DEFAULT_CLIENT_ID)).thenReturn(testProduct);

            // Act
            assertDoesNotThrow(() -> productService.toggleReturnProduct(TEST_PRODUCT_ID));

            // Assert
            verify(productRepository, times(1)).findByIdWithRelatedEntities(TEST_PRODUCT_ID, DEFAULT_CLIENT_ID);
            verify(productRepository, times(1)).save(testProduct);
            verify(userLogService, times(1)).logData(eq(DEFAULT_USER_ID), anyString(), anyString());
            // After toggle, returnWindowDays should be 0 (returns not allowed)
            assertEquals(0, testProduct.getReturnWindowDays());
        }

        @Test
        @DisplayName("toggleReturnProduct - Failure: Product not found")
        void testToggleReturnProduct_ProductNotFound() {
            // Arrange
            lenient().when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, DEFAULT_CLIENT_ID)).thenReturn(null);

            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> productService.toggleReturnProduct(TEST_PRODUCT_ID));

            assertEquals(String.format(ErrorMessages.ProductErrorMessages.ER013, TEST_PRODUCT_ID), exception.getMessage());
            verify(productRepository, never()).save(any());
        }
    }

    // ==================== getProductDetailsById Tests ====================

    @Nested
    @DisplayName("getProductDetailsById Tests")
    class GetProductDetailsByIdTests {

        @Test
        @DisplayName("getProductDetailsById - Success: Product found with related entities")
        void testGetProductDetailsById_Success() {
            // Arrange
            when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, DEFAULT_CLIENT_ID)).thenReturn(testProduct);

            // Act
            ProductResponseModel result = productService.getProductDetailsById(TEST_PRODUCT_ID);

            // Assert
            assertNotNull(result);
            assertEquals(TEST_PRODUCT_ID, result.getProductId());
            assertEquals(TEST_TITLE, result.getTitle());
            assertNotNull(result.getCategory());
            assertEquals(TEST_CATEGORY_ID, result.getCategory().getCategoryId());
            verify(productRepository, times(1)).findByIdWithRelatedEntities(TEST_PRODUCT_ID, DEFAULT_CLIENT_ID);
        }

        @Test
        @DisplayName("getProductDetailsById - Failure: Product not found")
        void testGetProductDetailsById_ProductNotFound() {
            // Arrange
            when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, DEFAULT_CLIENT_ID)).thenReturn(null);

            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> productService.getProductDetailsById(TEST_PRODUCT_ID));

            assertEquals(String.format(ErrorMessages.ProductErrorMessages.ER013, TEST_PRODUCT_ID), exception.getMessage());
        }
    }

    // ==================== getProductInBatches Tests ====================

    @Nested
    @DisplayName("getProductInBatches Tests")
    class GetProductInBatchesTests {

        @Test
        @DisplayName("getProductInBatches - Success: Valid pagination request")
        void testGetProductInBatches_Success() {
            // Arrange
            PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
            paginationRequest.setStart(0);
            paginationRequest.setEnd(10);
            paginationRequest.setIncludeDeleted(false);

            List<Product> productList = Arrays.asList(testProduct);
            Page<Product> productPage = new PageImpl<>(productList, PageRequest.of(0, 10), 1);

            when(productFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(productPage);

            // Act
            PaginationBaseResponseModel<ProductResponseModel> result = productService
                    .getProductInBatches(paginationRequest);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getData().size());
            assertEquals(1L, result.getTotalDataCount());
            assertEquals(TEST_PRODUCT_ID, result.getData().get(0).getProductId());
        }

        @Test
        @DisplayName("getProductInBatches - Failure: Invalid column name")
        void testGetProductInBatches_InvalidColumn() {
            // Arrange
            PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
            paginationRequest.setStart(0);
            paginationRequest.setEnd(10);

            PaginationBaseRequestModel.FilterCondition invalidFilter = new PaginationBaseRequestModel.FilterCondition();
            invalidFilter.setColumn("invalidColumn");
            invalidFilter.setOperator("contains");
            invalidFilter.setValue("test");
            paginationRequest.setFilters(Arrays.asList(invalidFilter));
            paginationRequest.setLogicOperator("AND");

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> productService.getProductInBatches(paginationRequest));

            assertTrue(exception.getMessage().contains("Invalid column name"));
            verify(productFilterQueryBuilder, never()).getColumnType("invalidColumn");
        }

        /**
         * Triple Loop Test for Filter Validation.
         * ProductService uses ProductFilterQueryBuilder and validates columns/operators.
         */
        @Test
        @DisplayName("Get Products In Batches - Filter Logic Triple Loop Validation")
        void getProductInBatches_TripleLoopValidation() {
            // 1. Columns
            String[] validColumns = {
                    "productId", "title", "descriptionHtml", "brand", "color", "colorLabel",
                    "condition", "countryOfManufacture", "model", "upc", "modificationHtml",
                    "price", "discount", "isDiscountPercent", "returnsAllowed", "length",
                    "breadth", "height", "weightKgs", "categoryId",
                    "isDeleted", "itemModified", "createdUser", "modifiedUser", "createdAt",
                    "updatedAt", "notes", "pickupLocationId"
            };
            String[] invalidColumns = { "invalidCol", "DROP TABLE", "unknown" };

            // 2. Operators
            String[] validOperators = {
                    "equals", "notEquals", "contains", "notContains", "startsWith", "endsWith",
                    "greaterThan", "lessThan", "greaterThanOrEqual", "lessThanOrEqual",
                    "isEmpty", "isNotEmpty"
            };
            String[] invalidOperators = { "invalidOp", "like" };

            // 3. Values
            String[] values = { "val", "" };

            // Mock response
            Page<Product> emptyPage = new PageImpl<>(Collections.emptyList());
            lenient().when(productFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), any(), any(), any(), anyBoolean(), any(Pageable.class)))
                    .thenReturn(emptyPage);

            // Mock column types (simplified)
            lenient().when(productFilterQueryBuilder.getColumnType(anyString())).thenReturn("string");

            for (String column : joinArrays(validColumns, invalidColumns)) {
                for (String operator : joinArrays(validOperators, invalidOperators)) {
                    for (String value : values) {
                        PaginationBaseRequestModel req = new PaginationBaseRequestModel();
                        req.setStart(0);
                        req.setEnd(10);
                        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                        filter.setColumn(column);
                        filter.setOperator(operator);
                        filter.setValue(value);
                        req.setFilters(List.of(filter));

                        boolean isValidColumn = Arrays.asList(validColumns).contains(column);
                        boolean isValidOperator = Arrays.asList(validOperators).contains(operator);

                        if (isValidColumn && isValidOperator) {
                            assertDoesNotThrow(() -> productService.getProductInBatches(req),
                                    "Failed for valid column/operator: " + column + "/" + operator);
                        } else {
                            BadRequestException ex = assertThrows(BadRequestException.class,
                                    () -> productService.getProductInBatches(req),
                                    "Expected BadRequest for invalid input: " + column + "/" + operator);
                            assertTrue(ex.getMessage().contains("Invalid"));
                        }
                    }
                }
            }
        }
    }

    private String[] joinArrays(String[]... arrays) {
        int length = 0;
        for (String[] array : arrays)
            length += array.length;
        String[] result = new String[length];
        int offset = 0;
        for (String[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    // ==================== Validation Tests ====================

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Product Validation - Invalid title (null)")
        void testProductValidation_InvalidTitle_Null() {
            // Arrange
            testProductRequest.setTitle(null);

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> productService.addProduct(testProductRequest));

            assertEquals(ErrorMessages.ProductErrorMessages.InvalidTitle, exception.getMessage());
        }

        @Test
        @DisplayName("Product Validation - Invalid title (empty)")
        void testProductValidation_InvalidTitle_Empty() {
            // Arrange
            testProductRequest.setTitle("");

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> productService.addProduct(testProductRequest));

            assertEquals(ErrorMessages.ProductErrorMessages.InvalidTitle, exception.getMessage());
        }

        @Test
        @DisplayName("Product Validation - Invalid description (null)")
        void testProductValidation_InvalidDescription_Null() {
            // Arrange
            testProductRequest.setDescriptionHtml(null);

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> productService.addProduct(testProductRequest));

            assertEquals(ErrorMessages.ProductErrorMessages.InvalidDescription, exception.getMessage());
        }

        @Test
        @DisplayName("Product Validation - Invalid brand (null)")
        void testProductValidation_InvalidBrand_Null() {
            // Arrange
            testProductRequest.setBrand(null);

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> productService.addProduct(testProductRequest));

            assertEquals(ErrorMessages.ProductErrorMessages.InvalidBrand, exception.getMessage());
        }

        @Test
        @DisplayName("Product Validation - Invalid color label (null)")
        void testProductValidation_InvalidColorLabel_Null() {
            // Arrange
            testProductRequest.setColorLabel(null);

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> productService.addProduct(testProductRequest));

            assertEquals(ErrorMessages.ProductErrorMessages.InvalidColorLabel, exception.getMessage());
        }

        @Test
        @DisplayName("Product Validation - Invalid condition (null)")
        void testProductValidation_InvalidCondition_Null() {
            // Arrange
            testProductRequest.setCondition(null);

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> productService.addProduct(testProductRequest));

            assertEquals(ErrorMessages.ProductErrorMessages.InvalidCondition, exception.getMessage());
        }

        @Test
        @DisplayName("Product Validation - Invalid country of manufacture (null)")
        void testProductValidation_InvalidCountry_Null() {
            // Arrange
            testProductRequest.setCountryOfManufacture(null);

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> productService.addProduct(testProductRequest));

            assertEquals(ErrorMessages.ProductErrorMessages.InvalidCountryOfManufacture, exception.getMessage());
        }

        @Test
        @DisplayName("Product Validation - Invalid price (null)")
        void testProductValidation_InvalidPrice_Null() {
            // Arrange
            testProductRequest.setPrice(null);

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> productService.addProduct(testProductRequest));

            assertEquals(ErrorMessages.ProductErrorMessages.InvalidPrice, exception.getMessage());
        }

        @Test
        @DisplayName("Product Validation - Invalid price (negative)")
        void testProductValidation_InvalidPrice_Negative() {
            // Arrange
            testProductRequest.setPrice(new BigDecimal("-10.00"));

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> productService.addProduct(testProductRequest));

            assertEquals(ErrorMessages.ProductErrorMessages.InvalidPrice, exception.getMessage());
        }

        @Test
        @DisplayName("Product Validation - Invalid client ID (null)")
        void testProductValidation_InvalidClientId_Null() {
            // Arrange
            testProductRequest.setClientId(null);

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> productService.addProduct(testProductRequest));

            assertEquals(ErrorMessages.ProductErrorMessages.InvalidClientId, exception.getMessage());
        }
    }

    // ==================== Helper Methods ====================

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
        testGoogleCred.setClientId(DEFAULT_CLIENT_ID.toString()); // clientId is String
        testGoogleCred.setType("{}"); // Using type field instead of serviceAccountKey

        // Initialize test client response
        testClientResponse = new ClientResponseModel();
        testClientResponse.setClientId(DEFAULT_CLIENT_ID);
        testClientResponse.setName(DEFAULT_CLIENT_NAME);

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
        testProductRequest.setClientId(DEFAULT_CLIENT_ID);
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
        testProduct = new Product(testProductRequest, DEFAULT_CREATED_USER, DEFAULT_CLIENT_ID);
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
}
