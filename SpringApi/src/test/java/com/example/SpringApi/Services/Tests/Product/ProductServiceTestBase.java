package com.example.SpringApi.Services.Tests.Product;

import com.example.SpringApi.FilterQueryBuilder.ProductFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.ProductRequestModel;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.ClientService;
import com.example.SpringApi.Services.MessageService;
import com.example.SpringApi.Services.ProductService;
import com.example.SpringApi.Services.Tests.BaseTest;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Constants.ProductConditionConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Base test class for ProductService tests.
 * Contains common mocks, dependencies, and setup logic shared across all ProductService test classes.
 */
@ExtendWith(MockitoExtension.class)
public abstract class ProductServiceTestBase extends BaseTest {

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

    protected MessageService messageService;

    @Mock
    protected Environment environment;

    @Mock
    protected HttpServletRequest request;

    protected ProductService productService;

    protected Product testProduct;
    protected ProductRequestModel testProductRequest;
    protected ProductCategory testCategory;
    protected GoogleCred testGoogleCred;
    protected ClientResponseModel testClientResponse;
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
        testClient.setImgbbApiKey("test-imgbb-api-key");
        lenient().when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
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


}
