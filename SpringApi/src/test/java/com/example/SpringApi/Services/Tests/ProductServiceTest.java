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
 * @version 1.0
 * @since 2024-01-15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

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
    private static final Long TEST_CLIENT_ID = 1L;
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
    private static final String CREATED_USER = "admin";
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
            verify(clientService, times(1)).getClientById(TEST_CLIENT_ID);
            verify(productRepository, atLeastOnce()).save(any(Product.class));
            verify(userLogService, times(1)).logData(eq(1L), anyString(), eq("addProduct"));
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
    void testAddProduct_FirebaseUploadFails() {
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
    @DisplayName("addProduct - Success: With valid request and ImgBB upload")
    void testAddProduct_GoogleCredNotFound() {
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

    // ==================== editProduct Tests ====================

    @Test
    @DisplayName("editProduct - Success: Valid product update")
    void testEditProduct_Success() {
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
        lenient().when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> productService.editProduct(testProductRequest));

        assertEquals(String.format(ErrorMessages.ProductErrorMessages.ER013, TEST_PRODUCT_ID), exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    // ==================== toggleDeleteProduct Tests ====================

    @Test
    @DisplayName("toggleDeleteProduct - Success: Toggle from false to true")
    void testToggleDeleteProduct_Success() {
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
    @DisplayName("toggleDeleteProduct - Success: Toggle from true to false")
    void testToggleDeleteProduct_Success_Undelete() {
        // Arrange
        testProduct.setIsDeleted(true);
        when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID)).thenReturn(testProduct);

        // Act
        assertDoesNotThrow(() -> productService.toggleDeleteProduct(TEST_PRODUCT_ID));

        // Assert
        assertFalse(testProduct.getIsDeleted());
    }

    @Test
    @DisplayName("toggleDeleteProduct - Failure: Product not found")
    void testToggleDeleteProduct_ProductNotFound() {
        // Arrange
        lenient().when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> productService.toggleDeleteProduct(TEST_PRODUCT_ID));

        assertEquals(String.format(ErrorMessages.ProductErrorMessages.ER013, TEST_PRODUCT_ID), exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    // ==================== toggleReturnProduct Tests ====================

    @Test
    @DisplayName("toggleReturnProduct - Success: Toggle returns allowed")
    void testToggleReturnProduct_Success() {
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
    @DisplayName("toggleReturnProduct - Failure: Product not found")
    void testToggleReturnProduct_ProductNotFound() {
        // Arrange
        lenient().when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> productService.toggleReturnProduct(TEST_PRODUCT_ID));

        assertEquals(String.format(ErrorMessages.ProductErrorMessages.ER013, TEST_PRODUCT_ID), exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    // ==================== getProductDetailsById Tests ====================

    @Test
    @DisplayName("getProductDetailsById - Success: Product found with related entities")
    void testGetProductDetailsById_Success() {
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
    @DisplayName("getProductDetailsById - Failure: Product not found")
    void testGetProductDetailsById_ProductNotFound() {
        // Arrange
        when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> productService.getProductDetailsById(TEST_PRODUCT_ID));

        assertEquals(String.format(ErrorMessages.ProductErrorMessages.ER013, TEST_PRODUCT_ID), exception.getMessage());
    }

    // ==================== getProductInBatches Tests ====================

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

    @Test
    @DisplayName("getProductInBatches - Success: With selected IDs")
    void testGetProductInBatches_WithSelectedIds() {
        // Arrange
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);
        paginationRequest.setSelectedIds(Arrays.asList(1L, 2L, 3L));

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
    }

    @Test
    @DisplayName("getProductInBatches - Success: With single filter")
    void testGetProductInBatches_WithSingleFilter() {
        // Arrange
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);

        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn("title");
        filter.setOperator("contains");
        filter.setValue("Test");
        paginationRequest.setFilters(Arrays.asList(filter));
        paginationRequest.setLogicOperator("AND");

        List<Product> productList = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(productList, PageRequest.of(0, 10), 1);

        when(productFilterQueryBuilder.getColumnType("title")).thenReturn("string");
        when(productFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(productPage);

        // Act
        PaginationBaseResponseModel<ProductResponseModel> result = productService
                .getProductInBatches(paginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(productFilterQueryBuilder, times(1)).getColumnType("title");
    }

    @Test
    @DisplayName("getProductInBatches - Success: With multiple filters AND")
    void testGetProductInBatches_WithMultipleFiltersAND() {
        // Arrange
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);

        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
        filter1.setColumn("title");
        filter1.setOperator("contains");
        filter1.setValue("Test");

        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
        filter2.setColumn("brand");
        filter2.setOperator("contains");
        filter2.setValue("Brand");

        paginationRequest.setFilters(Arrays.asList(filter1, filter2));
        paginationRequest.setLogicOperator("AND");

        List<Product> productList = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(productList, PageRequest.of(0, 10), 1);

        when(productFilterQueryBuilder.getColumnType("title")).thenReturn("string");
        when(productFilterQueryBuilder.getColumnType("brand")).thenReturn("string");
        when(productFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(productPage);

        // Act
        PaginationBaseResponseModel<ProductResponseModel> result = productService
                .getProductInBatches(paginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(productFilterQueryBuilder, times(1)).getColumnType("title");
        verify(productFilterQueryBuilder, times(1)).getColumnType("brand");
    }

    @Test
    @DisplayName("getProductInBatches - Success: With multiple filters OR")
    void testGetProductInBatches_WithMultipleFiltersOR() {
        // Arrange
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);

        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
        filter1.setColumn("title");
        filter1.setOperator("contains");
        filter1.setValue("Test");

        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
        filter2.setColumn("title");
        filter2.setOperator("contains");
        filter2.setValue("Product");

        paginationRequest.setFilters(Arrays.asList(filter1, filter2));
        paginationRequest.setLogicOperator("OR");

        List<Product> productList = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(productList, PageRequest.of(0, 10), 1);

        when(productFilterQueryBuilder.getColumnType("title")).thenReturn("string");
        when(productFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(productPage);

        // Act
        PaginationBaseResponseModel<ProductResponseModel> result = productService
                .getProductInBatches(paginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(productFilterQueryBuilder, times(2)).getColumnType("title");
    }

    @Test
    @DisplayName("getProductInBatches - Success: With complex filters")
    void testGetProductInBatches_WithComplexFilters() {
        // Arrange
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);

        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
        filter1.setColumn("title");
        filter1.setOperator("contains");
        filter1.setValue("Test");

        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
        filter2.setColumn("productId");
        filter2.setOperator("greaterThan");
        filter2.setValue("0");

        paginationRequest.setFilters(Arrays.asList(filter1, filter2));
        paginationRequest.setLogicOperator("AND");

        List<Product> productList = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(productList, PageRequest.of(0, 10), 1);

        when(productFilterQueryBuilder.getColumnType("title")).thenReturn("string");
        when(productFilterQueryBuilder.getColumnType("productId")).thenReturn("number");
        when(productFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(productPage);

        // Act
        PaginationBaseResponseModel<ProductResponseModel> result = productService
                .getProductInBatches(paginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(productFilterQueryBuilder, times(1)).getColumnType("title");
        verify(productFilterQueryBuilder, times(1)).getColumnType("productId");
    }

    // ==================== Validation Tests ====================

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
    @DisplayName("Product Validation - Null Pickup Location Quantities - Throws BadRequestException")
    void testProductValidation_NullPickupLocationQuantities_Throws() {
        testProductRequest.setPickupLocationQuantities(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.addProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.AtLeastOnePickupLocationRequired, ex.getMessage());
    }

    @Test
    @DisplayName("Product Validation - Empty Pickup Location Quantities - Throws BadRequestException")
    void testProductValidation_EmptyPickupLocationQuantities_Throws() {
        testProductRequest.setPickupLocationQuantities(new HashMap<>());
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.addProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.AtLeastOnePickupLocationRequired, ex.getMessage());
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

    // ==================== Image Processing Tests ====================

    @Test
    @DisplayName("Image Processing - Convert URL to base64 fails in unit test (network access)")
    void testConvertToBase64_UrlToBase64() {
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
    void testConvertToBase64_NullImage() {
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
    void testConvertToBase64_EmptyImage() {
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
    void bulkAddProducts_EmptyList() {
        // Arrange
        List<ProductRequestModel> products = new ArrayList<>();

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            productService.bulkAddProducts(products);
        });
        assertTrue(exception.getMessage().contains("Product list cannot be null or empty"));
        verify(productRepository, never()).save(any(Product.class));
    }

    // ==================== Additional GetProductDetailsById Tests ====================

    @Test
    @DisplayName("Get Product By ID - Negative ID - Not Found")
    void getProductDetailsById_NegativeId_ThrowsNotFoundException() {
        when(productRepository.findByProductIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productService.getProductDetailsById(-1L));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Get Product By ID - Zero ID - Not Found")
    void getProductDetailsById_ZeroId_ThrowsNotFoundException() {
        when(productRepository.findByProductIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productService.getProductDetailsById(0L));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Get Product By ID - Long.MAX_VALUE - Not Found")
    void getProductDetailsById_MaxLongId_ThrowsNotFoundException() {
        when(productRepository.findByProductIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productService.getProductDetailsById(Long.MAX_VALUE));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Get Product By ID - Long.MIN_VALUE - Not Found")
    void getProductDetailsById_MinLongId_ThrowsNotFoundException() {
        when(productRepository.findByProductIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productService.getProductDetailsById(Long.MIN_VALUE));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidId, ex.getMessage());
    }

    // ==================== Additional CreateProduct Tests ====================

    @Test
    @DisplayName("Create Product - Null Request - Throws BadRequestException")
    void createProduct_NullRequest_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.createProduct(null));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidRequest, ex.getMessage());
    }

    @Test
    @DisplayName("Create Product - Null Title - Throws BadRequestException")
    void createProduct_NullTitle_ThrowsBadRequestException() {
        testProductRequest.setTitle(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.createProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidTitle, ex.getMessage());
    }

    @Test
    @DisplayName("Create Product - Empty Title - Throws BadRequestException")
    void createProduct_EmptyTitle_ThrowsBadRequestException() {
        testProductRequest.setTitle("");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.createProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidTitle, ex.getMessage());
    }

    @Test
    @DisplayName("Create Product - Whitespace Title - Throws BadRequestException")
    void createProduct_WhitespaceTitle_ThrowsBadRequestException() {
        testProductRequest.setTitle("   ");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.createProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidTitle, ex.getMessage());
    }

    @Test
    @DisplayName("Create Product - Null Description - Throws BadRequestException")
    void createProduct_NullDescription_ThrowsBadRequestException() {
        testProductRequest.setDescription(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.createProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidDescription, ex.getMessage());
    }

    @Test
    @DisplayName("Create Product - Empty Description - Throws BadRequestException")
    void createProduct_EmptyDescription_ThrowsBadRequestException() {
        testProductRequest.setDescription("");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.createProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidDescription, ex.getMessage());
    }

    @Test
    @DisplayName("Create Product - Null Brand - Throws BadRequestException")
    void createProduct_NullBrand_ThrowsBadRequestException() {
        testProductRequest.setBrand(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.createProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidBrand, ex.getMessage());
    }

    @Test
    @DisplayName("Create Product - Empty Brand - Throws BadRequestException")
    void createProduct_EmptyBrand_ThrowsBadRequestException() {
        testProductRequest.setBrand("");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.createProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidBrand, ex.getMessage());
    }

    @Test
    @DisplayName("Create Product - Null Color Label - Throws BadRequestException")
    void createProduct_NullColorLabel_ThrowsBadRequestException() {
        testProductRequest.setColorLabel(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.createProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidColorLabel, ex.getMessage());
    }

    @Test
    @DisplayName("Create Product - Empty Color Label - Throws BadRequestException")
    void createProduct_EmptyColorLabel_ThrowsBadRequestException() {
        testProductRequest.setColorLabel("");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.createProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidColorLabel, ex.getMessage());
    }

    @Test
    @DisplayName("Create Product - Null Condition - Throws BadRequestException")
    void createProduct_NullCondition_ThrowsBadRequestException() {
        testProductRequest.setCondition(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.createProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidCondition, ex.getMessage());
    }

    @Test
    @DisplayName("Create Product - Empty Condition - Throws BadRequestException")
    void createProduct_EmptyCondition_ThrowsBadRequestException() {
        testProductRequest.setCondition("");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.createProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidCondition, ex.getMessage());
    }

    @Test
    @DisplayName("Create Product - Null Country Of Manufacture - Throws BadRequestException")
    void createProduct_NullCountryOfManufacture_ThrowsBadRequestException() {
        testProductRequest.setCountryOfManufacture(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.createProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidCountryOfManufacture, ex.getMessage());
    }

    @Test
    @DisplayName("Create Product - Empty Country Of Manufacture - Throws BadRequestException")
    void createProduct_EmptyCountryOfManufacture_ThrowsBadRequestException() {
        testProductRequest.setCountryOfManufacture("");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.createProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidCountryOfManufacture, ex.getMessage());
    }

    @Test
    @DisplayName("Create Product - Negative Price - Throws BadRequestException")
    void createProduct_NegativePrice_ThrowsBadRequestException() {
        testProductRequest.setPrice(BigDecimal.valueOf(-10.0));
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.createProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidPrice, ex.getMessage());
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
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.createProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidCategoryId, ex.getMessage());
    }

    @Test
    @DisplayName("Create Product - Invalid Category ID - Throws BadRequestException")
    void createProduct_InvalidCategoryId_ThrowsBadRequestException() {
        testProductRequest.setCategoryId(999L);
        when(productCategoryRepository.findById(999L)).thenReturn(java.util.Optional.empty());
        
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productService.createProduct(testProductRequest));
        assertEquals(ErrorMessages.ProductErrorMessages.InvalidCategoryId, ex.getMessage());
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
}