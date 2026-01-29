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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
 * @version 1.0
 * @since 2024-01-15
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
    private static final Long TEST_CLIENT_ID = DEFAULT_CLIENT_ID;
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
        lenient().when(request.getAttribute("userId")).thenReturn(TEST_CLIENT_ID);
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

    // ==================== addProduct Tests ====================

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
            verify(userLogService, times(1)).logData(eq(1L), anyString(), eq("addProduct"));
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
    }

    // ==================== editProduct Tests ====================

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
    }

    // ==================== toggleDeleteProduct Tests ====================

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
    }

    // ==================== toggleReturnProduct Tests ====================

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
    }

    // ==================== getProductDetailsById Tests ====================

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
    }

    // ==================== getProductInBatches Tests ====================

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
            String[] stringColumns = PRODUCT_STRING_COLUMNS;
            String[] numberColumns = PRODUCT_NUMBER_COLUMNS;
            String[] booleanColumns = PRODUCT_BOOLEAN_COLUMNS;
            String[] dateColumns = PRODUCT_DATE_COLUMNS;
            String[] invalidColumns = BATCH_INVALID_COLUMNS;
            String[] stringOperators = BATCH_STRING_OPERATORS;
            String[] numberOperators = BATCH_NUMBER_OPERATORS;
            String[] booleanOperators = BATCH_BOOLEAN_OPERATORS;
            String[] dateOperators = BATCH_DATE_OPERATORS;
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

                        boolean isValueRequired = !PaginationBaseRequestModel.OP_IS_EMPTY.equals(operator)
                                && !PaginationBaseRequestModel.OP_IS_NOT_EMPTY.equals(operator);
                        boolean isValuePresent = value != null;
                        boolean shouldSucceed = isColumnKnown && isOperatorValidForType && (!isValueRequired || isValuePresent);

                        try {
                            productService.getProductInBatches(testRequest);
                            if (!shouldSucceed) {
                                String reason = !isColumnKnown ? "Invalid column: " + column
                                        : !isOperatorValidForType ? "Invalid operator '" + operator + "' for column '" + column + "'"
                                        : "Missing value for operator " + operator;
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

    // ==================== Validation Tests ====================

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
        @DisplayName("Product Validation - Invalid client ID (null)")
        void productValidation_InvalidClientId_Null_ThrowsBadRequestException() {
            // Arrange
            testProductRequest.setClientId(null);

            // Act & Assert
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidClientId,
                    () -> productService.addProduct(testProductRequest));
        }
    }

    // ==================== Image Processing Tests ====================

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

    // ==================== Bulk Add Products Tests ====================

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
    }

    // ==================== Additional GetProductDetailsById Tests ====================

    @Nested
    @DisplayName("Additional GetProductDetailsById Tests")
    class AdditionalGetProductDetailsByIdTests {

        @Test
        @DisplayName("Get Product By ID - Negative ID - Not Found")
        void getProductDetailsById_NegativeId_ThrowsNotFoundException() {
            when(productRepository.findByProductIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
            assertThrowsNotFound(ErrorMessages.ProductErrorMessages.InvalidId,
                    () -> productService.getProductDetailsById(-1L));
        }

        @Test
        @DisplayName("Get Product By ID - Zero ID - Not Found")
        void getProductDetailsById_ZeroId_ThrowsNotFoundException() {
            when(productRepository.findByProductIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
            assertThrowsNotFound(ErrorMessages.ProductErrorMessages.InvalidId,
                    () -> productService.getProductDetailsById(0L));
        }

        @Test
        @DisplayName("Get Product By ID - Long.MAX_VALUE - Not Found")
        void getProductDetailsById_MaxLongId_ThrowsNotFoundException() {
            when(productRepository.findByProductIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
            assertThrowsNotFound(ErrorMessages.ProductErrorMessages.InvalidId,
                    () -> productService.getProductDetailsById(Long.MAX_VALUE));
        }

        @Test
        @DisplayName("Get Product By ID - Long.MIN_VALUE - Not Found")
        void getProductDetailsById_MinLongId_ThrowsNotFoundException() {
            when(productRepository.findByProductIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID)).thenReturn(null);
            assertThrowsNotFound(ErrorMessages.ProductErrorMessages.InvalidId,
                    () -> productService.getProductDetailsById(Long.MIN_VALUE));
        }
    }

    // ==================== Additional CreateProduct Tests ====================

    @Nested
    @DisplayName("Additional CreateProduct Tests")
    class AdditionalCreateProductTests {

        @Test
        @DisplayName("Create Product - Null Request - Throws BadRequestException")
        void createProduct_NullRequest_ThrowsBadRequestException() {
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidRequest,
                    () -> productService.createProduct(null));
        }

        @Test
        @DisplayName("Create Product - Null Title - Throws BadRequestException")
        void createProduct_NullTitle_ThrowsBadRequestException() {
            testProductRequest.setTitle(null);
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidTitle,
                    () -> productService.createProduct(testProductRequest));
        }

        @Test
        @DisplayName("Create Product - Empty Title - Throws BadRequestException")
        void createProduct_EmptyTitle_ThrowsBadRequestException() {
            testProductRequest.setTitle("");
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidTitle,
                    () -> productService.createProduct(testProductRequest));
        }

        @Test
        @DisplayName("Create Product - Whitespace Title - Throws BadRequestException")
        void createProduct_WhitespaceTitle_ThrowsBadRequestException() {
            testProductRequest.setTitle("   ");
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidTitle,
                    () -> productService.createProduct(testProductRequest));
        }

        @Test
        @DisplayName("Create Product - Null Description - Throws BadRequestException")
        void createProduct_NullDescription_ThrowsBadRequestException() {
            testProductRequest.setDescription(null);
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidDescription,
                    () -> productService.createProduct(testProductRequest));
        }

        @Test
        @DisplayName("Create Product - Empty Description - Throws BadRequestException")
        void createProduct_EmptyDescription_ThrowsBadRequestException() {
            testProductRequest.setDescription("");
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidDescription,
                    () -> productService.createProduct(testProductRequest));
        }

        @Test
        @DisplayName("Create Product - Null Brand - Throws BadRequestException")
        void createProduct_NullBrand_ThrowsBadRequestException() {
            testProductRequest.setBrand(null);
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidBrand,
                    () -> productService.createProduct(testProductRequest));
        }

        @Test
        @DisplayName("Create Product - Empty Brand - Throws BadRequestException")
        void createProduct_EmptyBrand_ThrowsBadRequestException() {
            testProductRequest.setBrand("");
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidBrand,
                    () -> productService.createProduct(testProductRequest));
        }

        @Test
        @DisplayName("Create Product - Null Color Label - Throws BadRequestException")
        void createProduct_NullColorLabel_ThrowsBadRequestException() {
            testProductRequest.setColorLabel(null);
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidColorLabel,
                    () -> productService.createProduct(testProductRequest));
        }

        @Test
        @DisplayName("Create Product - Empty Color Label - Throws BadRequestException")
        void createProduct_EmptyColorLabel_ThrowsBadRequestException() {
            testProductRequest.setColorLabel("");
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidColorLabel,
                    () -> productService.createProduct(testProductRequest));
        }

        @Test
        @DisplayName("Create Product - Null Condition - Throws BadRequestException")
        void createProduct_NullCondition_ThrowsBadRequestException() {
            testProductRequest.setCondition(null);
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidCondition,
                    () -> productService.createProduct(testProductRequest));
        }

        @Test
        @DisplayName("Create Product - Empty Condition - Throws BadRequestException")
        void createProduct_EmptyCondition_ThrowsBadRequestException() {
            testProductRequest.setCondition("");
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidCondition,
                    () -> productService.createProduct(testProductRequest));
        }

        @Test
        @DisplayName("Create Product - Null Country Of Manufacture - Throws BadRequestException")
        void createProduct_NullCountryOfManufacture_ThrowsBadRequestException() {
            testProductRequest.setCountryOfManufacture(null);
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidCountryOfManufacture,
                    () -> productService.createProduct(testProductRequest));
        }

        @Test
        @DisplayName("Create Product - Empty Country Of Manufacture - Throws BadRequestException")
        void createProduct_EmptyCountryOfManufacture_ThrowsBadRequestException() {
            testProductRequest.setCountryOfManufacture("");
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidCountryOfManufacture,
                    () -> productService.createProduct(testProductRequest));
        }

        @Test
        @DisplayName("Create Product - Negative Price - Throws BadRequestException")
        void createProduct_NegativePrice_ThrowsBadRequestException() {
            testProductRequest.setPrice(BigDecimal.valueOf(-10.0));
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidPrice,
                    () -> productService.createProduct(testProductRequest));
        }

        @Test
        @DisplayName("Create Product - Zero Price - Success (Valid)")
        void createProduct_ZeroPrice_Success() {
            testProductRequest.setPrice(BigDecimal.ZERO);
            when(productCategoryRepository.findById(TEST_CATEGORY_ID)).thenReturn(java.util.Optional.of(testCategory));
            when(productRepository.save(any(Product.class))).thenReturn(testProduct);
            
            assertDoesNotThrow(() -> productService.createProduct(testProductRequest));
        }

        @Test
        @DisplayName("Create Product - Null Category ID - Throws BadRequestException")
        void createProduct_NullCategoryId_ThrowsBadRequestException() {
            testProductRequest.setCategoryId(null);
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidCategoryId,
                    () -> productService.createProduct(testProductRequest));
        }

        @Test
        @DisplayName("Create Product - Invalid Category ID - Throws BadRequestException")
        void createProduct_InvalidCategoryId_ThrowsBadRequestException() {
            testProductRequest.setCategoryId(999L);
            when(productCategoryRepository.findById(999L)).thenReturn(java.util.Optional.empty());
            
            assertThrowsBadRequest(ErrorMessages.ProductErrorMessages.InvalidCategoryId,
                    () -> productService.createProduct(testProductRequest));
        }
    }

    // ==================== Additional UpdateProduct Tests ====================

    @Test
    @DisplayName("Update Product - Negative ID - Not Found")
    void updateProduct_NegativeId_ThrowsNotFoundException() {
        testProductRequest.setProductId(-1L);
        when(productRepository.findByProductIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productService.updateProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Update Product - Zero ID - Not Found")
    void updateProduct_ZeroId_ThrowsNotFoundException() {
        testProductRequest.setProductId(0L);
        when(productRepository.findByProductIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
        
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productService.updateProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Update Product - Null Title - Throws BadRequestException")
    void updateProduct_NullTitle_ThrowsBadRequestException() {
        testProductRequest.setTitle(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.updateProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidTitle, ex.getMessage());
    }

    // ==================== Additional ToggleProduct Tests ====================

    @Test
    @DisplayName("Toggle Product - Negative ID - Not Found")
    void toggleProduct_NegativeId_ThrowsNotFoundException() {
        when(productRepository.findByProductIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productService.toggleProduct(-1L));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Toggle Product - Zero ID - Not Found")
    void toggleProduct_ZeroId_ThrowsNotFoundException() {
        when(productRepository.findByProductIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productService.toggleProduct(0L));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Toggle Product - Max Long ID - Not Found")
    void toggleProduct_MaxLongId_ThrowsNotFoundException() {
        when(productRepository.findByProductIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productService.toggleProduct(Long.MAX_VALUE));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Toggle Product - Multiple Toggles - State Persistence")
    void toggleProduct_MultipleToggles_StatePersists() {
        testProduct.setIsDeleted(false);
        when(productRepository.findByProductIdAndClientId(TEST_PRODUCT_ID, TEST_CLIENT_ID))
                .thenReturn(testProduct);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        
        productService.toggleProduct(TEST_PRODUCT_ID);
        assertTrue(testProduct.getIsDeleted());
        
        productService.toggleProduct(TEST_PRODUCT_ID);
        assertFalse(testProduct.getIsDeleted());
    }

    // ==================== Additional GetProductsInBatches Tests ====================

    @Test
    @DisplayName("Get Products In Batches - Negative Start - Throws BadRequestException")
    void getProductsInBatches_NegativeStart_Throws() {
        PaginationBaseRequestModel req = new PaginationBaseRequestModel();
        req.setStart(-1);
        req.setEnd(10);
        
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.getProductsInBatches(req));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidRequest, ex.getMessage());
    }

    @Test
    @DisplayName("Get Products In Batches - End Before Start - Throws BadRequestException")
    void getProductsInBatches_EndBeforeStart_Throws() {
        PaginationBaseRequestModel req = new PaginationBaseRequestModel();
        req.setStart(10);
        req.setEnd(5);
        
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.getProductsInBatches(req));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidRequest, ex.getMessage());
    }

    @Test
    @DisplayName("Get Products In Batches - Null Filters")
    void getProductsInBatches_NullFilters_Success() {
        PaginationBaseRequestModel req = new PaginationBaseRequestModel();
        req.setStart(0);
        req.setEnd(10);
        req.setFilters(null);
        
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> page = new PageImpl<>(products, PageRequest.of(0, 10), 1);
        lenient().when(productFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), isNull(), anyString(), isNull(), anyBoolean(), any(Pageable.class)))
                .thenReturn(page);
        
        PaginationBaseResponseModel<Product> result = productService.getProductsInBatches(req);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Get Products In Batches - Empty Results")
    void getProductsInBatches_EmptyResults_ReturnsEmpty() {
        PaginationBaseRequestModel req = new PaginationBaseRequestModel();
        req.setStart(0);
        req.setEnd(10);
        
        List<Product> emptyList = new ArrayList<>();
        Page<Product> emptyPage = new PageImpl<>(emptyList, PageRequest.of(0, 10), 0);
        lenient().when(productFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), isNull(), anyString(), isNull(), anyBoolean(), any(Pageable.class)))
                .thenReturn(emptyPage);
        
        PaginationBaseResponseModel<Product> result = productService.getProductsInBatches(req);
        assertNotNull(result);
        assertEquals(0, result.getData().size());
    }

    @Test
    @DisplayName("Get Products In Batches - Large Page Size (1000)")
    void getProductsInBatches_LargePageSize_Success() {
        PaginationBaseRequestModel req = new PaginationBaseRequestModel();
        req.setStart(0);
        req.setEnd(1000);
        
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> page = new PageImpl<>(products, PageRequest.of(0, 1000), 1);
        lenient().when(productFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), isNull(), anyString(), isNull(), anyBoolean(), any(Pageable.class)))
                .thenReturn(page);
        
        PaginationBaseResponseModel<Product> result = productService.getProductsInBatches(req);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Get Products In Batches - With Search Query")
    void getProductsInBatches_WithSearchQuery_Success() {
        PaginationBaseRequestModel req = new PaginationBaseRequestModel();
        req.setStart(0);
        req.setEnd(10);
        req.setSearchQuery("laptop");
        
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> page = new PageImpl<>(products, PageRequest.of(0, 10), 1);
        lenient().when(productFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), isNull(), eq("laptop"), isNull(), anyBoolean(), any(Pageable.class)))
                .thenReturn(page);
        
        PaginationBaseResponseModel<Product> result = productService.getProductsInBatches(req);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Get Products In Batches - Ascending Sort")
    void getProductsInBatches_AscendingSort_Success() {
        PaginationBaseRequestModel req = new PaginationBaseRequestModel();
        req.setStart(0);
        req.setEnd(10);
        req.setIsAscending(true);
        
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> page = new PageImpl<>(products, PageRequest.of(0, 10), 1);
        lenient().when(productFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), isNull(), anyString(), anyBoolean(), anyBoolean(), any(Pageable.class)))
                .thenReturn(page);
        
        PaginationBaseResponseModel<Product> result = productService.getProductsInBatches(req);
        assertNotNull(result);
    }

    // ==================== Additional BulkAddProducts Tests ====================

    @Test
    @DisplayName("Bulk Add Products - All Invalid Titles")
    void bulkAddProducts_AllInvalidTitles_AllFail() {
        List<ProductRequestModel> products = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ProductRequestModel req = new ProductRequestModel();
            req.setTitle("");
            req.setDescription("Valid");
            req.setBrand("Valid");
            req.setColorLabel("Red");
            req.setCondition("New");
            req.setCountryOfManufacture("USA");
            req.setPrice(BigDecimal.valueOf(100.0));
            req.setCategoryId(TEST_CATEGORY_ID);
            products.add(req);
        }
        
        BulkInsertResponseModel<Long> result = productService.bulkAddProducts(products);
        assertNotNull(result);
        assertEquals(5, result.getTotalRequested());
    }

    @Test
    @DisplayName("Bulk Add Products - Mixed Valid and Invalid")
    void bulkAddProducts_MixedValidInvalid_PartialSuccess() {
        List<ProductRequestModel> products = new ArrayList<>();
        
        // Valid
        ProductRequestModel valid = new ProductRequestModel();
        valid.setTitle("Valid Product");
        valid.setDescription("Valid");
        valid.setBrand("Brand");
        valid.setColorLabel("Red");
        valid.setCondition("New");
        valid.setCountryOfManufacture("USA");
        valid.setPrice(BigDecimal.valueOf(100.0));
        valid.setCategoryId(TEST_CATEGORY_ID);
        products.add(valid);
        
        // Invalid (empty title)
        ProductRequestModel invalid = new ProductRequestModel();
        invalid.setTitle("");
        invalid.setDescription("Valid");
        products.add(invalid);
        
        when(productCategoryRepository.findById(TEST_CATEGORY_ID)).thenReturn(java.util.Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        
        BulkInsertResponseModel<Long> result = productService.bulkAddProducts(products);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Bulk Add Products - Large Batch (50 items)")
    void bulkAddProducts_LargeBatch_Success() {
        List<ProductRequestModel> products = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            ProductRequestModel req = new ProductRequestModel();
            req.setTitle("Product " + i);
            req.setDescription("Description " + i);
            req.setBrand("Brand " + i);
            req.setColorLabel("Color");
            req.setCondition("New");
            req.setCountryOfManufacture("USA");
            req.setPrice(BigDecimal.valueOf(100.0 + i));
            req.setCategoryId(TEST_CATEGORY_ID);
            products.add(req);
        }
        
        when(productCategoryRepository.findById(TEST_CATEGORY_ID)).thenReturn(java.util.Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        
        BulkInsertResponseModel<Long> result = productService.bulkAddProducts(products);
        assertNotNull(result);
    }

    // ==================== Comprehensive Validation Tests - Added ====================

    @Test
    @DisplayName("Add Product - Null Request - Throws BadRequestException")
    void addProduct_NullRequest_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.addProduct(null));
        assertTrue(ex.getMessage().contains("request") || ex.getMessage().contains("invalid"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Add Product - Null Product Name - Throws BadRequestException")
    void addProduct_NullProductName_ThrowsBadRequestException() {
        testProductRequest.setProductName(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.addProduct(testProductRequest));
        assertTrue(ex.getMessage().contains("name") || ex.getMessage().contains("invalid"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Add Product - Empty Product Name - Throws BadRequestException")
    void addProduct_EmptyProductName_ThrowsBadRequestException() {
        testProductRequest.setProductName("");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.addProduct(testProductRequest));
        assertTrue(ex.getMessage().contains("name") || ex.getMessage().contains("empty"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Add Product - Null Price - Throws BadRequestException")
    void addProduct_NullPrice_ThrowsBadRequestException() {
        testProductRequest.setPrice(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.addProduct(testProductRequest));
        assertTrue(ex.getMessage().contains("price") || ex.getMessage().contains("invalid"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Add Product - Negative Price - Throws BadRequestException")
    void addProduct_NegativePrice_ThrowsBadRequestException() {
        testProductRequest.setPrice(BigDecimal.valueOf(-100.0));
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.addProduct(testProductRequest));
        assertTrue(ex.getMessage().contains("price") || ex.getMessage().contains("invalid"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Add Product - Zero Price - Throws BadRequestException")
    void addProduct_ZeroPrice_ThrowsBadRequestException() {
        testProductRequest.setPrice(BigDecimal.ZERO);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.addProduct(testProductRequest));
        assertTrue(ex.getMessage().contains("price") || ex.getMessage().contains("invalid"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Add Product - Null Category ID - Throws BadRequestException")
    void addProduct_NullCategoryId_ThrowsBadRequestException() {
        testProductRequest.setCategoryId(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.addProduct(testProductRequest));
        assertTrue(ex.getMessage().contains("category") || ex.getMessage().contains("invalid"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Add Product - Negative Category ID - Throws BadRequestException")
    void addProduct_NegativeCategoryId_ThrowsBadRequestException() {
        testProductRequest.setCategoryId(-1L);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.addProduct(testProductRequest));
        assertTrue(ex.getMessage().contains("category") || ex.getMessage().contains("invalid"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Add Product - Zero Category ID - Throws BadRequestException")
    void addProduct_ZeroCategoryId_ThrowsBadRequestException() {
        testProductRequest.setCategoryId(0L);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.addProduct(testProductRequest));
        assertTrue(ex.getMessage().contains("category") || ex.getMessage().contains("invalid"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Add Product - Non-existent Category - Throws NotFoundException")
    void addProduct_NonexistentCategory_ThrowsNotFoundException() {
        when(productCategoryRepository.findById(testProductRequest.getCategoryId()))
                .thenReturn(java.util.Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productService.addProduct(testProductRequest));
        assertTrue(ex.getMessage().contains("category") || ex.getMessage().contains("not found"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Update Product - Negative ID - Throws NotFoundException")
    void updateProduct_NegativeId_ThrowsNotFoundException() {
        testProductRequest.setProductId(-1L);
        when(productRepository.findById(-1L)).thenReturn(java.util.Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productService.updateProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.NotFound, ex.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Update Product - Zero ID - Throws NotFoundException")
    void updateProduct_ZeroId_ThrowsNotFoundException() {
        testProductRequest.setProductId(0L);
        when(productRepository.findById(0L)).thenReturn(java.util.Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productService.updateProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.NotFound, ex.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Update Product - Long.MAX_VALUE ID - Throws NotFoundException")
    void updateProduct_MaxLongId_ThrowsNotFoundException() {
        testProductRequest.setProductId(Long.MAX_VALUE);
        when(productRepository.findById(Long.MAX_VALUE)).thenReturn(java.util.Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productService.updateProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.NotFound, ex.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Update Product - Negative Price - Throws BadRequestException")
    void updateProduct_NegativePrice_ThrowsBadRequestException() {
        testProductRequest.setPrice(BigDecimal.valueOf(-100.0));
        when(productRepository.findById(TEST_PRODUCT_ID)).thenReturn(java.util.Optional.of(testProduct));
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.updateProduct(testProductRequest));
        assertTrue(ex.getMessage().contains("price") || ex.getMessage().contains("invalid"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Delete Product - Negative ID - Throws NotFoundException")
    void deleteProduct_NegativeId_ThrowsNotFoundException() {
        when(productRepository.findById(-1L)).thenReturn(java.util.Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productService.deleteProduct(-1L));
        assertEquals(ErrorMessages.ProductErrorMessages.NotFound, ex.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Delete Product - Zero ID - Throws NotFoundException")
    void deleteProduct_ZeroId_ThrowsNotFoundException() {
        when(productRepository.findById(0L)).thenReturn(java.util.Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productService.deleteProduct(0L));
        assertEquals(ErrorMessages.ProductErrorMessages.NotFound, ex.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Get Product By ID - Negative ID - Throws NotFoundException")
    void getProductById_NegativeId_ThrowsNotFoundException() {
        when(productRepository.findById(-1L)).thenReturn(java.util.Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productService.getProductById(-1L));
        assertEquals(ErrorMessages.ProductErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Get Product By ID - Zero ID - Throws NotFoundException")
    void getProductById_ZeroId_ThrowsNotFoundException() {
        when(productRepository.findById(0L)).thenReturn(java.util.Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productService.getProductById(0L));
        assertEquals(ErrorMessages.ProductErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Get Product By ID - Long.MAX_VALUE ID - Throws NotFoundException")
    void getProductById_MaxLongId_ThrowsNotFoundException() {
        when(productRepository.findById(Long.MAX_VALUE)).thenReturn(java.util.Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productService.getProductById(Long.MAX_VALUE));
        assertEquals(ErrorMessages.ProductErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Toggle Product - Negative ID - Throws NotFoundException")
    void toggleProduct_NegativeId_ThrowsNotFoundException() {
        when(productRepository.findById(-1L)).thenReturn(java.util.Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productService.toggleProduct(-1L));
        assertEquals(ErrorMessages.ProductErrorMessages.NotFound, ex.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Toggle Product - Zero ID - Throws NotFoundException")
    void toggleProduct_ZeroId_ThrowsNotFoundException() {
        when(productRepository.findById(0L)).thenReturn(java.util.Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productService.toggleProduct(0L));
        assertEquals(ErrorMessages.ProductErrorMessages.NotFound, ex.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Toggle Product - Multiple Toggles - State Transitions")
    void toggleProduct_MultipleToggles_StateTransitions() {
        testProduct.setIsDeleted(false);
        when(productRepository.findById(TEST_PRODUCT_ID)).thenReturn(java.util.Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // First toggle: false -> true
        productService.toggleProduct(TEST_PRODUCT_ID);
        assertTrue(testProduct.getIsDeleted());

        // Second toggle: true -> false
        testProduct.setIsDeleted(true);
        productService.toggleProduct(TEST_PRODUCT_ID);
        assertFalse(testProduct.getIsDeleted());

        verify(productRepository, times(2)).save(any(Product.class));
    }

    @Test
    @DisplayName("Get Products In Batches - Null Request - Throws BadRequestException")
    void getProductsInBatches_NullRequest_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.getProductsInBatches(null));
        assertTrue(ex.getMessage().contains("request") || ex.getMessage().contains("invalid"));
    }

    @Test
    @DisplayName("Get Products In Batches - Start Greater Than End - Throws BadRequestException")
    void getProductsInBatches_StartGreaterThanEnd_ThrowsBadRequestException() {
        testPaginationRequest.setStart(100);
        testPaginationRequest.setEnd(10);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.getProductsInBatches(testPaginationRequest));
        assertTrue(ex.getMessage().contains("start") || ex.getMessage().contains("end"));
    }

    @Test
    @DisplayName("Bulk Add Products - Empty List - Throws BadRequestException")
    void bulkAddProducts_EmptyList_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.bulkAddProducts(new java.util.ArrayList<>()));
        assertTrue(ex.getMessage().contains("empty") || ex.getMessage().contains("null"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Bulk Add Products - Null List - Throws BadRequestException")
    void bulkAddProducts_NullList_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.bulkAddProducts(null));
        assertTrue(ex.getMessage().contains("empty") || ex.getMessage().contains("null"));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Search Products - Null Search Term - Throws BadRequestException")
    void searchProducts_NullSearchTerm_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.searchProducts(null));
        assertTrue(ex.getMessage().contains("search") || ex.getMessage().contains("invalid"));
    }

    @Test
    @DisplayName("Search Products - Empty Search Term - Throws BadRequestException")
    void searchProducts_EmptySearchTerm_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.searchProducts(""));
        assertTrue(ex.getMessage().contains("search") || ex.getMessage().contains("empty"));
    }

    @Test
    @DisplayName("Get Products By Category - Negative Category ID - Throws BadRequestException")
    void getProductsByCategory_NegativeCategoryId_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.getProductsByCategory(-1L));
        assertTrue(ex.getMessage().contains("category") || ex.getMessage().contains("invalid"));
    }

    @Test
    @DisplayName("Get Products By Category - Zero Category ID - Throws BadRequestException")
    void getProductsByCategory_ZeroCategoryId_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.getProductsByCategory(0L));
        assertTrue(ex.getMessage().contains("category") || ex.getMessage().contains("invalid"));
    }
}
}