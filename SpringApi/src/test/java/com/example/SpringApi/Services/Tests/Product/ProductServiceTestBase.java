package com.example.SpringApi.Services.Tests.Product;

import com.example.SpringApi.FilterQueryBuilder.ProductFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.ProductRequestModel;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.ClientService;
import com.example.SpringApi.Services.MessageService;
import com.example.SpringApi.Services.ProductService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Constants.ProductConditionConstants;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;
import com.example.SpringApi.Repositories.UserRepository;
import com.example.SpringApi.Authentication.JwtTokenProvider;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Base test class for ProductService tests.
 * Contains common mocks, dependencies, and setup logic shared across all
 * ProductService test classes.
 */
@ExtendWith(MockitoExtension.class)
public abstract class ProductServiceTestBase {

    // ==================== COMMON TEST CONSTANTS ====================

    protected static final Long DEFAULT_PRODUCT_ID = 1L;
    protected static final Long DEFAULT_USER_ID = 1L;
    protected static final Long DEFAULT_PICKUP_LOCATION_ID = 1L;
    protected static final Long DEFAULT_GOOGLE_CRED_ID = 100L;
    protected static final String DEFAULT_CREATED_USER = "admin";

    @Mock
    protected ProductRepository productRepository;

    @Mock
    protected ProductPickupLocationMappingRepository productPickupLocationMappingRepository;

    @Mock
    protected com.example.SpringApi.Repositories.PackagePickupLocationMappingRepository packagePickupLocationMappingRepository;

    @Mock
    protected UserLogService userLogService;

    @Mock
    protected ProductCategoryRepository productCategoryRepository;

    @Mock
    protected GoogleCredRepository googleCredRepository;

    @Mock
    protected ClientRepository clientRepository;

    @Mock
    protected ClientService clientService;

    @Mock
    protected ProductFilterQueryBuilder productFilterQueryBuilder;

    @Mock
    protected MessageService messageService;

    @Mock
    protected Environment environment;

    @Mock
    protected HttpServletRequest request;

    @Mock
    protected UserRepository userRepository;

    @Mock
    protected JwtTokenProvider jwtTokenProvider;

    protected ProductService productService;

    protected Product testProduct;
    protected ProductRequestModel testProductRequest;
    protected ProductCategory testCategory;
    protected GoogleCred testGoogleCred;
    protected ClientResponseModel testClientResponse;
    protected Client testClient;
    protected PickupLocation testPickupLocation;

    protected static final Long TEST_PRODUCT_ID = DEFAULT_PRODUCT_ID;
    protected static final Long TEST_CATEGORY_ID = 2L;
    protected static final Long TEST_CLIENT_ID = 1L;
    protected static final Long TEST_PICKUP_LOCATION_ID = DEFAULT_PICKUP_LOCATION_ID;
    protected static final Long TEST_GOOGLE_CRED_ID = DEFAULT_GOOGLE_CRED_ID;
    protected static final String TEST_TITLE = "Test Product";
    protected static final String TEST_DESCRIPTION = "Test Description";
    protected static final String TEST_BRAND = "Test Brand";
    protected static final String TEST_COLOR_LABEL = "Red";
    protected static final String TEST_CONDITION = ProductConditionConstants.NEW_WITH_TAGS;
    protected static final String TEST_COUNTRY = "USA";
    protected static final String TEST_UPC = "123456789012";
    protected static final BigDecimal TEST_PRICE = new BigDecimal("99.99");
    protected static final String CREATED_USER = DEFAULT_CREATED_USER;
    protected static final String TEST_CLIENT_NAME = "Test Client";
    protected static final String TEST_BASE64_IMAGE = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
    protected static final String TEST_URL_IMAGE = "https://example.com/image.png";

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
        messageService = mock(MessageService.class);
        productFilterQueryBuilder = mock(ProductFilterQueryBuilder.class);
        environment = mock(Environment.class);
        request = mock(HttpServletRequest.class);
        userRepository = mock(UserRepository.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);

        // Stub environment
        lenient().when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });

        // Initialize ProductService
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
        ReflectionTestUtils.setField(productService, "imageLocation", "imgbb");
        ReflectionTestUtils.setField(productService, "userRepository", userRepository);
        ReflectionTestUtils.setField(productService, "jwtTokenProvider", jwtTokenProvider);
        ReflectionTestUtils.setField(productService, "request", request);

        // Initialize test data
        initializeTestData();

        // Standard stubs (lenient)
        lenient().when(clientRepository.findById(anyLong())).thenReturn(Optional.of(testClient));
        lenient().when(clientService.getClientById(anyLong())).thenReturn(testClientResponse);
        lenient().when(productCategoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));
        lenient().when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            if (p.getProductId() == null) {
                p.setProductId(TEST_PRODUCT_ID);
            }
            return p;
        });
    }

    protected void stubRequestAuthorization() {
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
    }

    protected void stubRequestAttributes(Long userId, Long clientId, String user) {
        lenient().when(request.getAttribute("userId")).thenReturn(userId);
        lenient().when(request.getAttribute("clientId")).thenReturn(clientId);
        lenient().when(request.getAttribute("user")).thenReturn(user);
    }

    protected void stubEnvironment(String[] profiles) {
        lenient().when(environment.getActiveProfiles()).thenReturn(profiles);
    }

    protected void stubProductCategoryRepositoryFindById(Long categoryId, ProductCategory category) {
        lenient().when(productCategoryRepository.findById(categoryId)).thenReturn(Optional.ofNullable(category));
    }

    protected void stubGoogleCredRepositoryFindById(Long clientId, GoogleCred googleCred) {
        lenient().when(googleCredRepository.findById(clientId)).thenReturn(Optional.ofNullable(googleCred));
    }

    protected void stubClientServiceGetClientById(Long clientId, ClientResponseModel clientResponse) {
        lenient().when(clientService.getClientById(clientId)).thenReturn(clientResponse);
    }

    protected void stubProductRepositorySave(Product product) {
        lenient().when(productRepository.save(any(Product.class))).thenReturn(product);
    }

    protected void stubClientRepositoryFindById(Long clientId, Client client) {
        lenient().when(clientRepository.findById(clientId)).thenReturn(Optional.ofNullable(client));
    }

    protected com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel createValidPaginationRequest() {
        com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel request = new com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel();
        request.setStart(0);
        request.setEnd(10);
        request.setFilters(new java.util.ArrayList<>());
        return request;
    }

    protected void assertThrowsBadRequest(String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
        BadRequestException ex = assertThrows(BadRequestException.class, executable);
        assertEquals(expectedMessage, ex.getMessage());
    }

    protected void assertThrowsNotFound(String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
        NotFoundException ex = assertThrows(NotFoundException.class, executable);
        assertEquals(expectedMessage, ex.getMessage());
    }

    protected void stubProductRepositoryFindByIdWithRelatedEntities(Long productId, Long clientId, Product product) {
        lenient().when(productRepository.findByIdWithRelatedEntities(productId, clientId)).thenReturn(product);
    }

    protected void stubProductPickupLocationMappingRepositoryDeleteByProductId(Long productId) {
        lenient().doNothing().when(productPickupLocationMappingRepository).deleteByProductId(productId);
    }

    protected void stubUserLogServiceLogDataWithContext() {
        lenient().when(userLogService.logDataWithContext(anyLong(), anyString(), anyLong(), anyString(),
                anyString())).thenReturn(true);
    }

    protected void stubImgbbHelperUploadSuccess(ImgbbHelper mock) {
        ImgbbHelper.ImgbbUploadResponse mockResponse = new ImgbbHelper.ImgbbUploadResponse(
                "https://i.ibb.co/test/image.png",
                "test-delete-hash");
        lenient().when(mock.uploadFileToImgbb(anyString(), anyString())).thenReturn(mockResponse);
        lenient().when(mock.deleteImage(anyString())).thenReturn(true);
    }

    protected void stubImgbbHelperUploadFailure(ImgbbHelper mock) {
        lenient().when(mock.uploadFileToImgbb(anyString(), anyString())).thenReturn(null);
    }

    protected void initializeTestData() {
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
        testGoogleCred.setClientId(TEST_CLIENT_ID.toString());
        testGoogleCred.setType("{}");

        // Initialize test client response
        testClientResponse = new ClientResponseModel();
        testClientResponse.setClientId(TEST_CLIENT_ID);
        testClientResponse.setName(TEST_CLIENT_NAME);

        // Initialize test client database model
        testClient = new Client();
        testClient.setClientId(TEST_CLIENT_ID);
        testClient.setName(TEST_CLIENT_NAME);
        testClient.setImgbbApiKey("test-imgbb-key");

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
        testProductRequest.setAdditionalImage2(null);
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

    protected void stubProductFilterQueryBuilderGetColumnType() {
        lenient().when(productFilterQueryBuilder.getColumnType(anyString())).thenAnswer(invocation -> {
            String col = invocation.getArgument(0);
            if (java.util.Arrays.asList("title", "brand", "condition").contains(col))
                return "string";
            if (java.util.Arrays.asList("productId", "price", "categoryId", "pickupLocationId").contains(col))
                return "number";
            if (java.util.Arrays.asList("isDeleted").contains(col))
                return "boolean";
            if (java.util.Arrays.asList("createdAt").contains(col))
                return "date";
            return null;
        });
    }

    protected void stubProductFilterQueryBuilderFindPaginatedEntitiesEmptyPage() {
        org.springframework.data.domain.Page<Product> emptyPage = new org.springframework.data.domain.PageImpl<>(java.util.Collections.emptyList());
        lenient().when(productFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(emptyPage);
    }

}
