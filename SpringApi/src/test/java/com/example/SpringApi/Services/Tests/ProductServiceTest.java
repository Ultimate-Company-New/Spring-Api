package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.ProductRequestModel;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.ProductResponseModel;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.ClientService;
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
 * This test class provides comprehensive coverage of ProductService methods including:
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
        
        // Create ProductService instance manually with all dependencies
        productService = new ProductService(
            productRepository,
            userLogService,
            productCategoryRepository,
            clientRepository,
            clientService,
            environment,
            request
        );
        
        // Initialize test data
        initializeTestData();
        
        // Setup common mock behaviors
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        lenient().when(request.getAttribute("userId")).thenReturn(TEST_CLIENT_ID);
        lenient().when(request.getAttribute("user")).thenReturn(CREATED_USER);
        lenient().when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});

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
                        "test-delete-hash"
                    );
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
                        "test-delete-hash"
                    );
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
                        "test-delete-hash"
                    );
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
                        "test-delete-hash"
                    );
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
        // Arrange
        testProduct.setReturnsAllowed(true);
        when(productRepository.findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID)).thenReturn(testProduct);

        // Act
        assertDoesNotThrow(() -> productService.toggleReturnProduct(TEST_PRODUCT_ID));

        // Assert
        verify(productRepository, times(1)).findByIdWithRelatedEntities(TEST_PRODUCT_ID, TEST_CLIENT_ID);
        verify(productRepository, times(1)).save(testProduct);
        verify(userLogService, times(1)).logData(eq(1L), anyString(), anyString());
        assertFalse(testProduct.getReturnsAllowed());
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
        assertEquals(testCategory, result.getCategory());
        assertEquals(testPickupLocation, result.getPickupLocation());
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
        paginationRequest.setColumnName("title");
        paginationRequest.setCondition("contains");
        paginationRequest.setFilterExpr("Test");
        paginationRequest.setIncludeDeleted(false);

        List<Product> productList = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(productList, PageRequest.of(0, 10), 1);

        when(productRepository.findPaginatedProducts(
            eq(TEST_CLIENT_ID), eq("title"), eq("contains"), eq("Test"), eq(false), isNull(), any(Pageable.class)
        )).thenReturn(productPage);

        // Act
        PaginationBaseResponseModel<ProductResponseModel> result = productService.getProductInBatches(paginationRequest);

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
        paginationRequest.setColumnName("invalidColumn");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> productService.getProductInBatches(paginationRequest));

        assertTrue(exception.getMessage().contains("Invalid column"));
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

        when(productRepository.findPaginatedProducts(
            eq(TEST_CLIENT_ID), isNull(), isNull(), isNull(), eq(false), anySet(), any(Pageable.class)
        )).thenReturn(productPage);

        // Act
        PaginationBaseResponseModel<ProductResponseModel> result = productService.getProductInBatches(paginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
    }

    @Test
    @DisplayName("getProductInBatches - Success: Include deleted products")
    void testGetProductInBatches_IncludeDeleted() {
        // Arrange
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);
        paginationRequest.setIncludeDeleted(true);

        List<Product> productList = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(productList, PageRequest.of(0, 10), 1);

        when(productRepository.findPaginatedProducts(
            eq(TEST_CLIENT_ID), isNull(), isNull(), isNull(), eq(true), isNull(), any(Pageable.class)
        )).thenReturn(productPage);

        // Act
        PaginationBaseResponseModel<ProductResponseModel> result = productService.getProductInBatches(paginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
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

    @Test
    @DisplayName("Product Validation - Invalid pickup location ID (null)")
    void testProductValidation_InvalidPickupLocationId_Null() {
        // Arrange
        testProductRequest.setPickupLocationId(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> productService.addProduct(testProductRequest));

        assertEquals(ErrorMessages.ProductErrorMessages.InvalidPickupLocationId, exception.getMessage());
    }

    @Test
    @DisplayName("Product Validation - Invalid pickup location ID (zero)")
    void testProductValidation_InvalidPickupLocationId_Zero() {
        // Arrange
        testProductRequest.setPickupLocationId(0L);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> productService.addProduct(testProductRequest));

        assertEquals(ErrorMessages.ProductErrorMessages.InvalidPickupLocationId, exception.getMessage());
    }

    // ==================== Image Processing Tests ====================

    @Test
    @DisplayName("Image Processing - Convert URL to base64 fails in unit test (network access)")
    void testConvertToBase64_UrlToBase64() {
        // This test verifies that URL image processing fails appropriately in unit test environment
        // where network access is not available

        testProductRequest.setMainImage(TEST_URL_IMAGE);

        try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                (mock, context) -> {
                    ImgbbHelper.ImgbbUploadResponse mockResponse = new ImgbbHelper.ImgbbUploadResponse(
                        "https://i.ibb.co/test/image.png",
                        "test-delete-hash"
                    );
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
        // Test through the service - null images should be handled gracefully for optional images
        testProductRequest.setAdditionalImage1(null);

        try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                (mock, context) -> {
                    ImgbbHelper.ImgbbUploadResponse mockResponse = new ImgbbHelper.ImgbbUploadResponse(
                        "https://i.ibb.co/test/image.png",
                        "test-delete-hash"
                    );
                    when(mock.uploadFileToImgbb(anyString(), anyString())).thenReturn(mockResponse);
                    when(mock.deleteImage(anyString())).thenReturn(true);
                })) {

            assertDoesNotThrow(() -> productService.addProduct(testProductRequest));
        }
    }

    @Test
    @DisplayName("Image Processing - Handle empty image data")
    void testConvertToBase64_EmptyImage() {
        // Test through the service - empty images should be handled gracefully for optional images
        testProductRequest.setAdditionalImage1("");

        try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                (mock, context) -> {
                    ImgbbHelper.ImgbbUploadResponse mockResponse = new ImgbbHelper.ImgbbUploadResponse(
                        "https://i.ibb.co/test/image.png",
                        "test-delete-hash"
                    );
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
        testProductRequest.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        testProductRequest.setIsDeleted(false);
        testProductRequest.setReturnsAllowed(true);

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
        testProduct = new Product(testProductRequest, CREATED_USER);
        testProduct.setProductId(TEST_PRODUCT_ID);
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setUpdatedAt(LocalDateTime.now());
        testProduct.setCategory(testCategory);
        testProduct.setPickupLocation(testPickupLocation);
    }
}