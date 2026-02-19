package com.example.SpringApi.ServiceTests.Product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.SpringApi.Authentication.JwtTokenProvider;
import com.example.SpringApi.Constants.ProductConditionConstants;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.FilterQueryBuilder.ProductFilterQueryBuilder;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.ProductRequestModel;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.ClientService;
import com.example.SpringApi.Services.MessageService;
import com.example.SpringApi.Services.ProductService;
import com.example.SpringApi.Services.UserLogService;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Base test class for ProductService tests. Contains common mocks, dependencies, and setup logic
 * shared across all ProductService test classes.
 */
@ExtendWith(MockitoExtension.class)
abstract class ProductServiceTestBase {

  // ==================== COMMON TEST CONSTANTS ====================

  protected static final Long DEFAULT_PRODUCT_ID = 1L;
  protected static final Long DEFAULT_USER_ID = 1L;
  protected static final Long DEFAULT_PICKUP_LOCATION_ID = 1L;
  protected static final Long DEFAULT_GOOGLE_CRED_ID = 100L;
  protected static final String DEFAULT_CREATED_USER = "admin";

  @Mock protected ProductRepository productRepository;

  @Mock protected ProductPickupLocationMappingRepository productPickupLocationMappingRepository;

  @Mock
  protected com.example.SpringApi.Repositories.PackagePickupLocationMappingRepository
      packagePickupLocationMappingRepository;

  @Mock protected UserLogService userLogService;

  @Mock protected ProductCategoryRepository productCategoryRepository;

  @Mock protected GoogleCredRepository googleCredRepository;

  @Mock protected ClientRepository clientRepository;

  @Mock protected ClientService clientService;

  @Mock protected ProductFilterQueryBuilder productFilterQueryBuilder;

  @Mock protected MessageService messageService;

  @Mock protected Environment environment;

  @Mock protected HttpServletRequest request;

  @Mock protected JwtTokenProvider jwtTokenProvider;

  protected ProductService productService;
  protected ProductService productServiceMock;

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
  protected static final String TEST_BASE64_IMAGE =
      "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
  protected static final String TEST_URL_IMAGE = "https://example.com/image.png";

  @BeforeEach
  void setUp() {
    // Initialize mocks
    productRepository = mock(ProductRepository.class);
    productPickupLocationMappingRepository = mock(ProductPickupLocationMappingRepository.class);
    packagePickupLocationMappingRepository =
        mock(com.example.SpringApi.Repositories.PackagePickupLocationMappingRepository.class);
    userLogService = mock(UserLogService.class);
    productCategoryRepository = mock(ProductCategoryRepository.class);
    googleCredRepository = mock(GoogleCredRepository.class);
    clientRepository = mock(ClientRepository.class);
    clientService = mock(ClientService.class);
    messageService = mock(MessageService.class);
    productFilterQueryBuilder = mock(ProductFilterQueryBuilder.class);
    environment = mock(Environment.class);
    request = mock(HttpServletRequest.class);
    jwtTokenProvider = mock(JwtTokenProvider.class);
    productServiceMock = mock(ProductService.class);

    // Stub environment
    stubEnvironment(new String[] {"test"});

    // Initialize ProductService
    productService =
        new ProductService(
            productRepository,
            productPickupLocationMappingRepository,
            packagePickupLocationMappingRepository,
            productCategoryRepository,
            clientRepository,
            userLogService,
            clientService,
            productFilterQueryBuilder,
            messageService,
            environment,
            jwtTokenProvider,
            request);
    ReflectionTestUtils.setField(productService, "imageLocation", "imgbb");
    ReflectionTestUtils.setField(productService, "jwtTokenProvider", jwtTokenProvider);
    ReflectionTestUtils.setField(productService, "request", request);

    // Initialize test data
    initializeTestData();

    // Standard stubs (lenient)
    stubClientRepositoryFindById(TEST_CLIENT_ID, testClient);
    stubClientServiceGetClientById(TEST_CLIENT_ID, testClientResponse);
    stubProductCategoryRepositoryFindById(TEST_CATEGORY_ID, testCategory);
    stubProductRepositorySaveAssignId();
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
    lenient()
        .when(productCategoryRepository.findById(categoryId))
        .thenReturn(Optional.ofNullable(category));
  }

  protected void stubProductCategoryRepositoryFindAll(
      java.util.List<com.example.SpringApi.Models.DatabaseModels.ProductCategory> categories) {
    lenient().when(productCategoryRepository.findAll()).thenReturn(categories);
  }

  protected void stubGoogleCredRepositoryFindById(Long clientId, GoogleCred googleCred) {
    lenient()
        .when(googleCredRepository.findById(clientId))
        .thenReturn(Optional.ofNullable(googleCred));
  }

  protected void stubClientServiceGetClientById(Long clientId, ClientResponseModel clientResponse) {
    lenient().when(clientService.getClientById(clientId)).thenReturn(clientResponse);
  }

  protected void stubProductRepositorySave(Product product) {
    lenient().when(productRepository.save(any(Product.class))).thenReturn(product);
  }

  protected void stubProductRepositorySaveThrows(RuntimeException exception) {
    lenient().when(productRepository.save(any(Product.class))).thenThrow(exception);
  }

  protected void stubProductRepositorySaveAssignId() {
    lenient()
        .when(productRepository.save(any(Product.class)))
        .thenAnswer(
            invocation -> {
              Product p = invocation.getArgument(0);
              if (p.getProductId() == null) {
                p.setProductId(TEST_PRODUCT_ID);
              }
              return p;
            });
  }

  protected void stubClientRepositoryFindById(Long clientId, Client client) {
    lenient().when(clientRepository.findById(clientId)).thenReturn(Optional.ofNullable(client));
  }

  protected com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel
      createValidPaginationRequest() {
    com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel paginationRequest =
        new com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel();
    paginationRequest.setStart(0);
    paginationRequest.setEnd(10);
    paginationRequest.setFilters(new java.util.ArrayList<>());
    return paginationRequest;
  }

  protected void assertThrowsBadRequest(
      String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
    BadRequestException ex = assertThrows(BadRequestException.class, executable);
    assertEquals(expectedMessage, ex.getMessage());
  }

  protected void assertThrowsNotFound(
      String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
    NotFoundException ex = assertThrows(NotFoundException.class, executable);
    assertEquals(expectedMessage, ex.getMessage());
  }

  protected void stubProductRepositoryFindByIdWithRelatedEntities(
      Long productId, Long clientId, Product product) {
    lenient()
        .when(productRepository.findByIdWithRelatedEntities(productId, clientId))
        .thenReturn(product);
  }

  protected void stubProductRepositoryFindById(Long productId, Product product) {
    lenient().when(productRepository.findById(productId)).thenReturn(Optional.ofNullable(product));
  }

  protected void
      stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(
          Long productId, java.util.List<ProductPickupLocationMapping> mappings) {
    lenient()
        .when(
            productPickupLocationMappingRepository.findByProductIdWithPickupLocationAndAddress(
                productId))
        .thenReturn(mappings);
  }

  protected void
      stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddressThrows(
          RuntimeException exception) {
    lenient()
        .when(
            productPickupLocationMappingRepository.findByProductIdWithPickupLocationAndAddress(
                anyLong()))
        .thenThrow(exception);
  }

  protected void stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdsWithPackages(
      java.util.List<Long> pickupLocationIds,
      java.util.List<com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping>
          mappings) {
    lenient()
        .when(
            packagePickupLocationMappingRepository.findByPickupLocationIdsWithPackages(
                pickupLocationIds))
        .thenReturn(mappings);
  }

  protected void stubProductFilterQueryBuilderFindPaginatedEntities(
      org.springframework.data.domain.Page<Product> page) {
    lenient()
        .when(
            productFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(),
                any(),
                anyString(),
                any(),
                anyBoolean(),
                any(org.springframework.data.domain.Pageable.class)))
        .thenReturn(page);
  }

  protected void stubProductServiceAddProductDoNothing() {
    lenient().doNothing().when(productServiceMock).addProduct(any(ProductRequestModel.class));
  }

  protected void stubProductServiceAddProductThrowsUnauthorized() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(productServiceMock)
        .addProduct(any(ProductRequestModel.class));
  }

  protected void stubProductServiceEditProductDoNothing() {
    lenient().doNothing().when(productServiceMock).editProduct(any(ProductRequestModel.class));
  }

  protected void stubProductServiceEditProductThrowsUnauthorized() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(productServiceMock)
        .editProduct(any(ProductRequestModel.class));
  }

  protected void stubProductServiceToggleDeleteProductDoNothing() {
    lenient().doNothing().when(productServiceMock).toggleDeleteProduct(anyLong());
  }

  protected void stubProductServiceToggleDeleteProductThrowsUnauthorized() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(productServiceMock)
        .toggleDeleteProduct(anyLong());
  }

  protected void stubProductServiceToggleReturnProductDoNothing() {
    lenient().doNothing().when(productServiceMock).toggleReturnProduct(anyLong());
  }

  protected void stubProductServiceToggleReturnProductThrowsUnauthorized() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(productServiceMock)
        .toggleReturnProduct(anyLong());
  }

  protected void stubProductServiceBulkAddProductsDoNothing() {
    lenient().doNothing().when(productServiceMock).bulkAddProducts(anyList());
  }

  protected void stubProductServiceBulkAddProductsThrowsUnauthorized() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(productServiceMock)
        .bulkAddProducts(anyList());
  }

  protected void stubProductServiceBulkAddProductsAsyncDoNothing() {
    lenient()
        .doNothing()
        .when(productServiceMock)
        .bulkAddProductsAsync(anyList(), anyLong(), anyString(), anyLong());
  }

  protected void stubProductServiceBulkAddProductsAsyncThrowsUnauthorized() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(productServiceMock)
        .bulkAddProductsAsync(anyList(), anyLong(), anyString(), anyLong());
  }

  protected void stubProductServiceGetProductDetailsByIdReturns(
      com.example.SpringApi.Models.ResponseModels.ProductResponseModel response) {
    lenient().when(productServiceMock.getProductDetailsById(anyLong())).thenReturn(response);
  }

  protected void stubProductServiceGetProductDetailsByIdThrowsUnauthorized() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(productServiceMock)
        .getProductDetailsById(anyLong());
  }

  protected void stubProductServiceGetProductInBatchesReturns(
      com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel<
              com.example.SpringApi.Models.ResponseModels.ProductResponseModel>
          response) {
    lenient()
        .when(
            productServiceMock.getProductInBatches(
                any(com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.class)))
        .thenReturn(response);
  }

  protected void stubProductServiceGetProductInBatchesThrowsUnauthorized() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(productServiceMock)
        .getProductInBatches(
            any(com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.class));
  }

  protected void stubProductServiceGetProductStockAtLocationsByProductIdReturns(
      java.util.List<
              com.example.SpringApi.Models.ResponseModels.ProductStockByLocationResponseModel>
          response) {
    lenient()
        .when(
            productServiceMock.getProductStockAtLocationsByProductId(
                anyLong(), any(), any(), any()))
        .thenReturn(response);
  }

  protected void stubProductServiceGetProductStockAtLocationsByProductIdThrowsUnauthorized() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(productServiceMock)
        .getProductStockAtLocationsByProductId(anyLong(), any(), any(), any());
  }

  protected void stubProductServiceGetCategoryPathsByIdsReturns(java.util.Map<Long, String> paths) {
    lenient().when(productServiceMock.getCategoryPathsByIds(any())).thenReturn(paths);
  }

  protected void stubProductServiceGetCategoryPathsByIdsThrowsUnauthorized() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(productServiceMock)
        .getCategoryPathsByIds(any());
  }

  protected void stubProductServiceFindCategoriesByParentIdReturns(
      java.util.List<
              com.example.SpringApi.Models.ResponseModels.ProductCategoryWithPathResponseModel>
          categories) {
    lenient().when(productServiceMock.findCategoriesByParentId(any())).thenReturn(categories);
  }

  protected void stubProductServiceFindCategoriesByParentIdThrowsUnauthorized() {
    lenient()
        .doThrow(
            new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(productServiceMock)
        .findCategoriesByParentId(any());
  }

  protected void stubProductServiceUserContext(Long userId, String userName, Long clientId) {
    lenient().when(productServiceMock.getUserId()).thenReturn(userId);
    lenient().when(productServiceMock.getUser()).thenReturn(userName);
    lenient().when(productServiceMock.getClientId()).thenReturn(clientId);
  }

  protected void stubProductPickupLocationMappingRepositoryDeleteByProductId(Long productId) {
    lenient().doNothing().when(productPickupLocationMappingRepository).deleteByProductId(productId);
  }

  protected void stubUserLogServiceLogDataWithContext() {
    lenient()
        .when(
            userLogService.logDataWithContext(
                anyLong(), anyString(), anyLong(), anyString(), anyString()))
        .thenReturn(true);
  }

  protected void stubImgbbHelperUploadSuccess(ImgbbHelper mock) {
    ImgbbHelper.ImgbbUploadResponse mockResponse =
        new ImgbbHelper.ImgbbUploadResponse("https://i.ibb.co/test/image.png", "test-delete-hash");
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
    lenient()
        .when(productFilterQueryBuilder.getColumnType(anyString()))
        .thenAnswer(
            invocation -> {
              String col = invocation.getArgument(0);
              if (java.util.Arrays.asList("title", "brand", "condition").contains(col))
                return "string";
              if (java.util.Arrays.asList("productId", "price", "categoryId", "pickupLocationId")
                  .contains(col)) return "number";
              if (java.util.Arrays.asList("isDeleted").contains(col)) return "boolean";
              if (java.util.Arrays.asList("createdAt").contains(col)) return "date";
              return null;
            });
  }

  protected void stubProductFilterQueryBuilderFindPaginatedEntitiesEmptyPage() {
    org.springframework.data.domain.Page<Product> emptyPage =
        new org.springframework.data.domain.PageImpl<>(java.util.Collections.emptyList());
    lenient()
        .when(
            productFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(),
                any(),
                anyString(),
                any(),
                anyBoolean(),
                any(org.springframework.data.domain.Pageable.class)))
        .thenReturn(emptyPage);
  }
}
