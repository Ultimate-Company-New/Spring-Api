package com.example.springapi.ServiceTests.PurchaseOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;

import com.example.springapi.authentication.JwtTokenProvider;
import com.example.springapi.exceptions.BadRequestException;
import com.example.springapi.exceptions.NotFoundException;
import com.example.springapi.filterquerybuilder.PurchaseOrderFilterQueryBuilder;
import com.example.springapi.helpers.ImgbbHelper;
import com.example.springapi.models.databasemodels.*;
import com.example.springapi.models.dtos.AddressDuplicateCriteria;
import com.example.springapi.models.requestmodels.AddressRequestModel;
import com.example.springapi.models.requestmodels.PurchaseOrderProductItem;
import com.example.springapi.models.requestmodels.PurchaseOrderRequestModel;
import com.example.springapi.repositories.*;
import com.example.springapi.services.MessageService;
import com.example.springapi.services.PurchaseOrderService;
import com.example.springapi.services.UserLogService;
import com.itextpdf.text.DocumentException;
import freemarker.template.TemplateException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Base test class for PurchaseOrderService tests. Provides common setup, test data, and helper
 * methods for all PurchaseOrder test files.
 */
@ExtendWith(MockitoExtension.class)
abstract class PurchaseOrderServiceTestBase {

  @Mock protected PurchaseOrderRepository purchaseOrderRepository;

  @Mock protected AddressRepository addressRepository;

  @Mock protected UserRepository userRepository;

  @Mock protected LeadRepository leadRepository;

  @Mock protected ClientRepository clientRepository;

  @Mock protected ResourcesRepository resourcesRepository;

  @Mock protected OrderSummaryRepository orderSummaryRepository;

  @Mock protected PaymentRepository paymentRepository;

  @Mock protected ShipmentRepository shipmentRepository;

  @Mock protected ShipmentProductRepository shipmentProductRepository;

  @Mock protected ShipmentPackageRepository shipmentPackageRepository;

  @Mock protected ShipmentPackageProductRepository shipmentPackageProductRepository;

  @Mock protected UserLogService userLogService;

  @Mock protected MessageService messageService;

  @Mock protected PurchaseOrderFilterQueryBuilder purchaseOrderFilterQueryBuilder;

  @Mock protected Environment environment;

  @Mock protected JwtTokenProvider jwtTokenProvider;

  @Mock protected HttpServletRequest request;

  @Mock protected PurchaseOrderService purchaseOrderServiceMock;

  protected PurchaseOrderService purchaseOrderService;

  protected PurchaseOrder testPurchaseOrder;
  protected PurchaseOrderRequestModel testPurchaseOrderRequest;
  protected Address testAddress;
  protected OrderSummary testOrderSummary;
  protected Lead testLead;
  protected User testUser;
  protected Client testClient;
  protected AddressRequestModel testAddressRequest;

  protected static final Long TEST_PO_ID = 1L;
  protected static final Long TEST_CLIENT_ID = 1L;
  protected static final Long TEST_USER_ID = 1L;
  protected static final Long TEST_ADDRESS_ID = 1L;
  protected static final Long TEST_LEAD_ID = 1L;
  protected static final Long TEST_PRODUCT_ID = 1L;
  protected static final String CREATED_USER = "admin";
  protected static final String TEST_VENDOR_NUMBER = "VEN-001";
  protected static final Long TEST_PICKUP_LOCATION_ID = 1L;
  protected static final Long TEST_PACKAGE_ID = 1L;

  @BeforeEach
  void setUp() {
    initializeTestData();

    // Setup common stubs
    stubUserLogServiceLogData(true);
    stubEnvironmentGetProperty("imageLocation", "imgbb");
    stubEnvironmentGetActiveProfiles(new String[] {"test"});
    stubClientRepositoryFindById(Optional.of(testClient));
    stubAddressRepositoryFindExactDuplicate(Optional.empty());
    stubAddressRepositorySave(testAddress);
    stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(Optional.empty());
    stubOrderSummaryRepositorySave(testOrderSummary);
    stubShipmentRepositoryFindByOrderSummaryId(Collections.emptyList());
    stubShipmentRepositorySaveAssigningId(1L);
    stubShipmentPackageRepositoryFindByShipmentId(Collections.emptyList());
    stubShipmentPackageRepositorySaveAssigningId(1L);
    stubShipmentProductRepositorySaveAll();
    stubShipmentPackageProductRepositorySaveAll();

    // Set up RequestContextHolder so BaseService.getClientId() works
    MockHttpServletRequest mockRequest = new MockHttpServletRequest();
    mockRequest.addHeader("Authorization", "Bearer test-token");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

    // Create service with manual injection (not @InjectMocks due to complex dependencies)
    purchaseOrderService =
        new PurchaseOrderService(
            purchaseOrderRepository,
            addressRepository,
            userRepository,
            leadRepository,
            clientRepository,
            resourcesRepository,
            orderSummaryRepository,
            shipmentRepository,
            shipmentProductRepository,
            shipmentPackageRepository,
            shipmentPackageProductRepository,
            paymentRepository,
            userLogService,
            purchaseOrderFilterQueryBuilder,
            messageService,
            environment,
            jwtTokenProvider,
            request);
  }

  /** Initialize common test data for all PurchaseOrder tests. */
  protected void initializeTestData() {
    // Initialize test client
    testClient = new Client();
    testClient.setClientId(TEST_CLIENT_ID);
    testClient.setName("Test Client");
    testClient.setImgbbApiKey("test-api-key");
    testClient.setSupportEmail("support@test.com");

    // Initialize test user
    testUser = new User();
    testUser.setUserId(TEST_USER_ID);
    testUser.setFirstName("Test");
    testUser.setLastName("User");
    testUser.setLoginName("testuser");

    // Initialize test lead
    testLead = new Lead();
    testLead.setLeadId(TEST_LEAD_ID);
    testLead.setFirstName("Test");
    testLead.setLastName("Lead");
    testLead.setEmail("lead@test.com");

    // Initialize test address request
    testAddressRequest = new AddressRequestModel();
    testAddressRequest.setAddressType("SHIPPING");
    testAddressRequest.setStreetAddress("123 Test St");
    testAddressRequest.setCity("Test City");
    testAddressRequest.setState("TS");
    testAddressRequest.setPostalCode("12345");
    testAddressRequest.setCountry("Test Country");

    // Initialize test address
    testAddress = new Address();
    testAddress.setAddressId(TEST_ADDRESS_ID);
    testAddress.setStreetAddress("123 Test St");
    testAddress.setCity("Test City");
    testAddress.setState("TS");
    testAddress.setPostalCode("12345");
    testAddress.setCountry("Test Country");

    // Initialize test order summary
    testOrderSummary = new OrderSummary();
    testOrderSummary.setOrderSummaryId(1L);
    testOrderSummary.setEntityType(OrderSummary.EntityType.PURCHASE_ORDER.getValue());
    testOrderSummary.setEntityId(TEST_PO_ID);
    testOrderSummary.setProductsSubtotal(new BigDecimal("100.00"));
    testOrderSummary.setTotalDiscount(new BigDecimal("0.00"));
    testOrderSummary.setPackagingFee(new BigDecimal("5.00"));
    testOrderSummary.setTotalShipping(new BigDecimal("10.00"));
    testOrderSummary.setSubtotal(new BigDecimal("115.00"));
    testOrderSummary.setGstPercentage(new BigDecimal("18.00"));
    testOrderSummary.setGstAmount(new BigDecimal("20.70"));
    testOrderSummary.setGrandTotal(new BigDecimal("135.70"));
    testOrderSummary.setPendingAmount(new BigDecimal("135.70"));
    testOrderSummary.setEntityAddressId(TEST_ADDRESS_ID);
    testOrderSummary.setPriority("HIGH");
    testOrderSummary.setClientId(TEST_CLIENT_ID);
    testOrderSummary.setCreatedUser(CREATED_USER);
    testOrderSummary.setModifiedUser(CREATED_USER);

    // Initialize test purchase order request
    testPurchaseOrderRequest = new PurchaseOrderRequestModel();
    testPurchaseOrderRequest.setPurchaseOrderId(TEST_PO_ID);
    testPurchaseOrderRequest.setVendorNumber(TEST_VENDOR_NUMBER);
    testPurchaseOrderRequest.setPurchaseOrderStatus("DRAFT");
    testPurchaseOrderRequest.setAssignedLeadId(TEST_LEAD_ID);

    // Set OrderSummary data
    PurchaseOrderRequestModel.OrderSummaryData orderSummaryData =
        new PurchaseOrderRequestModel.OrderSummaryData();
    orderSummaryData.setProductsSubtotal(new BigDecimal("100.00"));
    orderSummaryData.setTotalDiscount(new BigDecimal("0.00"));
    orderSummaryData.setPackagingFee(new BigDecimal("5.00"));
    orderSummaryData.setTotalShipping(new BigDecimal("10.00"));
    orderSummaryData.setGstPercentage(new BigDecimal("18.00"));
    orderSummaryData.setGstAmount(new BigDecimal("20.70"));
    orderSummaryData.setGrandTotal(new BigDecimal("135.70"));
    orderSummaryData.setPendingAmount(new BigDecimal("135.70"));
    orderSummaryData.setPriority("HIGH");
    orderSummaryData.setAddress(testAddressRequest);
    testPurchaseOrderRequest.setOrderSummary(orderSummaryData);

    // Set products (required)
    List<PurchaseOrderProductItem> products = new ArrayList<>();
    products.add(new PurchaseOrderProductItem(TEST_PRODUCT_ID, new BigDecimal("100.00"), 1));
    testPurchaseOrderRequest.setProducts(products);

    // Set shipments (required)
    PurchaseOrderRequestModel.ShipmentProductData shipmentProduct =
        new PurchaseOrderRequestModel.ShipmentProductData();
    shipmentProduct.setProductId(TEST_PRODUCT_ID);
    shipmentProduct.setAllocatedQuantity(1);
    shipmentProduct.setAllocatedPrice(new BigDecimal("50.00"));

    PurchaseOrderRequestModel.PackageProductData packageProduct =
        new PurchaseOrderRequestModel.PackageProductData();
    packageProduct.setProductId(TEST_PRODUCT_ID);
    packageProduct.setQuantity(1);

    PurchaseOrderRequestModel.ShipmentPackageData shipmentPackage =
        new PurchaseOrderRequestModel.ShipmentPackageData();
    shipmentPackage.setPackageId(TEST_PACKAGE_ID);
    shipmentPackage.setQuantityUsed(1);
    shipmentPackage.setTotalCost(new BigDecimal("0.00"));
    shipmentPackage.setProducts(Collections.singletonList(packageProduct));

    PurchaseOrderRequestModel.CourierSelectionData courierSelection =
        new PurchaseOrderRequestModel.CourierSelectionData();
    courierSelection.setCourierCompanyId(1L);
    courierSelection.setCourierName("Test Courier");
    courierSelection.setCourierRate(new BigDecimal("0.00"));
    courierSelection.setCourierMinWeight(new BigDecimal("0.00"));
    courierSelection.setCourierMetadata("{}");

    PurchaseOrderRequestModel.ShipmentData shipmentData =
        new PurchaseOrderRequestModel.ShipmentData();
    shipmentData.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
    shipmentData.setTotalWeightKgs(new BigDecimal("1.000"));
    shipmentData.setTotalQuantity(1);
    shipmentData.setExpectedDeliveryDate(LocalDateTime.now().plusDays(1));
    shipmentData.setPackagingCost(new BigDecimal("0.00"));
    shipmentData.setShippingCost(new BigDecimal("0.00"));
    shipmentData.setTotalCost(new BigDecimal("0.00"));
    shipmentData.setSelectedCourier(courierSelection);
    shipmentData.setProducts(Collections.singletonList(shipmentProduct));
    shipmentData.setPackages(Collections.singletonList(shipmentPackage));

    testPurchaseOrderRequest.setShipments(Collections.singletonList(shipmentData));

    // Initialize test purchase order
    testPurchaseOrder = new PurchaseOrder();
    testPurchaseOrder.setPurchaseOrderId(TEST_PO_ID);
    testPurchaseOrder.setVendorNumber(TEST_VENDOR_NUMBER);
    testPurchaseOrder.setPurchaseOrderStatus("DRAFT");
    testPurchaseOrder.setAssignedLeadId(TEST_LEAD_ID);
    testPurchaseOrder.setClientId(TEST_CLIENT_ID);
    testPurchaseOrder.setIsDeleted(false);
    testPurchaseOrder.setCreatedAt(LocalDateTime.now());
    testPurchaseOrder.setUpdatedAt(LocalDateTime.now());
    testPurchaseOrder.setCreatedUser(CREATED_USER);
    testPurchaseOrder.setModifiedUser(CREATED_USER);
  }

  // ==========================================
  // COMMON STUB SETUP HELPERS
  // ==========================================

  protected void stubUserLogServiceLogData(boolean result) {
    lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(result);
  }

  protected void stubUserLogServiceLogDataWithContext(boolean result) {
    lenient()
        .when(
            userLogService.logDataWithContext(
                anyLong(), anyString(), anyLong(), anyString(), anyString()))
        .thenReturn(result);
  }

  protected void stubEnvironmentGetProperty(String key, String value) {
    lenient().when(environment.getProperty(key)).thenReturn(value);
  }

  protected void stubEnvironmentGetActiveProfiles(String[] profiles) {
    lenient().when(environment.getActiveProfiles()).thenReturn(profiles);
  }

  protected void stubClientRepositoryFindById(Optional<Client> client) {
    lenient().when(clientRepository.findById(anyLong())).thenReturn(client);
  }

  protected void stubAddressRepositoryFindExactDuplicate(Optional<Address> address) {
    lenient()
        .when(addressRepository.findExactDuplicate(any(AddressDuplicateCriteria.class)))
        .thenReturn(address);
  }

  protected void stubAddressRepositorySave(Address address) {
    lenient().when(addressRepository.save(any(Address.class))).thenReturn(address);
  }

  protected void stubAddressRepositoryFindById(Optional<Address> address) {
    lenient().when(addressRepository.findById(anyLong())).thenReturn(address);
  }

  protected void stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(
      Optional<OrderSummary> orderSummary) {
    lenient()
        .when(orderSummaryRepository.findByEntityTypeAndEntityId(anyString(), anyLong()))
        .thenReturn(orderSummary);
  }

  protected void stubOrderSummaryRepositorySave(OrderSummary orderSummary) {
    lenient().when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(orderSummary);
  }

  protected void stubShipmentRepositoryFindByOrderSummaryId(List<Shipment> shipments) {
    lenient().when(shipmentRepository.findByOrderSummaryId(anyLong())).thenReturn(shipments);
  }

  protected void stubShipmentProductRepositoryFindByShipmentId(
      List<ShipmentProduct> shipmentProducts) {
    lenient()
        .when(shipmentProductRepository.findByShipmentId(anyLong()))
        .thenReturn(shipmentProducts);
  }

  protected void stubShipmentRepositorySaveAssigningId(Long shipmentId) {
    lenient()
        .when(shipmentRepository.save(any(Shipment.class)))
        .thenAnswer(
            invocation -> {
              Shipment shipment = invocation.getArgument(0);
              if (shipment.getShipmentId() == null) {
                shipment.setShipmentId(shipmentId);
              }
              return shipment;
            });
  }

  protected void stubShipmentPackageRepositoryFindByShipmentId(
      List<ShipmentPackage> shipmentPackages) {
    lenient()
        .when(shipmentPackageRepository.findByShipmentId(anyLong()))
        .thenReturn(shipmentPackages);
  }

  protected void stubShipmentPackageRepositorySaveAssigningId(Long shipmentPackageId) {
    lenient()
        .when(shipmentPackageRepository.save(any(ShipmentPackage.class)))
        .thenAnswer(
            invocation -> {
              ShipmentPackage shipmentPackage = invocation.getArgument(0);
              if (shipmentPackage.getShipmentPackageId() == null) {
                shipmentPackage.setShipmentPackageId(shipmentPackageId);
              }
              return shipmentPackage;
            });
  }

  protected void stubShipmentProductRepositorySaveAll() {
    lenient()
        .when(shipmentProductRepository.saveAll(anyList()))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  protected void stubShipmentProductRepositorySaveAllCapture(
      org.mockito.ArgumentCaptor<
              java.util.List<com.example.springapi.models.databasemodels.ShipmentProduct>>
          captor) {
    lenient()
        .when(shipmentProductRepository.saveAll(captor.capture()))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  protected void stubShipmentPackageProductRepositorySaveAll() {
    lenient()
        .when(shipmentPackageProductRepository.saveAll(anyList()))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  protected void stubPurchaseOrderRepositorySave(PurchaseOrder purchaseOrder) {
    lenient()
        .when(purchaseOrderRepository.save(any(PurchaseOrder.class)))
        .thenReturn(purchaseOrder);
  }

  protected void stubPurchaseOrderRepositoryFindById(Optional<PurchaseOrder> purchaseOrder) {
    lenient()
        .when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(anyLong(), anyLong()))
        .thenReturn(purchaseOrder);
  }

  protected void stubPurchaseOrderRepositoryFindByIdWithRelations(
      Optional<PurchaseOrder> purchaseOrder) {
    lenient()
        .when(
            purchaseOrderRepository.findByPurchaseOrderIdAndClientIdWithAllRelations(
                anyLong(), anyLong()))
        .thenReturn(purchaseOrder);
  }

  protected void stubPurchaseOrderFilterQueryBuilderGetColumnType(String column, String type) {
    lenient().when(purchaseOrderFilterQueryBuilder.getColumnType(column)).thenReturn(type);
  }

  protected void stubPurchaseOrderFilterQueryBuilderFindPaginatedWithDetails(
      org.springframework.data.domain.Page<
              com.example.springapi.models.dtos.PurchaseOrderWithDetails>
          page) {
    lenient()
        .when(
            purchaseOrderFilterQueryBuilder.findPaginatedWithDetails(
                anyLong(), any(), any(), anyString(), any(), anyBoolean(), any()))
        .thenReturn(page);
  }

  protected void stubResourcesRepositorySave(Resources resources) {
    lenient().when(resourcesRepository.save(any(Resources.class))).thenReturn(resources);
  }

  protected void stubResourcesRepositoryFindByEntityIdAndEntityType(List<Resources> resources) {
    lenient()
        .when(resourcesRepository.findByEntityIdAndEntityType(anyLong(), anyString()))
        .thenReturn(resources);
  }

  protected void stubLeadRepositoryFindLeadWithDetails(Lead lead) {
    lenient()
        .when(leadRepository.findLeadWithDetailsByIdIncludingDeleted(anyLong(), anyLong()))
        .thenReturn(lead);
  }

  protected void stubUserRepositoryFindByUserIdAndClientId(Optional<User> user) {
    lenient().when(userRepository.findByUserIdAndClientId(anyLong(), anyLong())).thenReturn(user);
  }

  protected void stubPurchaseOrderServiceGetPurchaseOrdersInBatches(
      com.example.springapi.models.responsemodels.PaginationBaseResponseModel<
              com.example.springapi.models.responsemodels.PurchaseOrderResponseModel>
          response) {
    lenient().when(purchaseOrderServiceMock.getPurchaseOrdersInBatches(any())).thenReturn(response);
  }

  protected void stubPurchaseOrderServiceGetPurchaseOrderDetailsById(
      com.example.springapi.models.responsemodels.PurchaseOrderResponseModel response) {
    lenient()
        .when(purchaseOrderServiceMock.getPurchaseOrderDetailsById(anyLong()))
        .thenReturn(response);
  }

  protected void stubPurchaseOrderServiceCreateDoNothing() {
    lenient().doNothing().when(purchaseOrderServiceMock).createPurchaseOrder(any());
  }

  protected void stubPurchaseOrderServiceUpdateDoNothing() {
    lenient().doNothing().when(purchaseOrderServiceMock).updatePurchaseOrder(any());
  }

  protected void stubPurchaseOrderServiceToggleDoNothing() {
    lenient().doNothing().when(purchaseOrderServiceMock).togglePurchaseOrder(anyLong());
  }

  protected void stubPurchaseOrderServiceApproveDoNothing() {
    lenient().doNothing().when(purchaseOrderServiceMock).approvedByPurchaseOrder(anyLong());
  }

  protected void stubPurchaseOrderServiceRejectDoNothing() {
    lenient().doNothing().when(purchaseOrderServiceMock).rejectedByPurchaseOrder(anyLong());
  }

  protected void stubPurchaseOrderServiceBulkCreateDoNothing() {
    lenient()
        .doNothing()
        .when(purchaseOrderServiceMock)
        .bulkCreatePurchaseOrdersAsync(anyList(), anyLong(), anyString(), anyLong());
  }

  protected void stubPurchaseOrderServiceUserContext(Long userId, String userName, Long clientId) {
    lenient().when(purchaseOrderServiceMock.getUserId()).thenReturn(userId);
    lenient().when(purchaseOrderServiceMock.getUser()).thenReturn(userName);
    lenient().when(purchaseOrderServiceMock.getClientId()).thenReturn(clientId);
  }

  protected void stubPurchaseOrderServiceGetPurchaseOrderPdf(byte[] pdfBytes)
      throws TemplateException, IOException, DocumentException {
    lenient().when(purchaseOrderServiceMock.getPurchaseOrderPdf(anyLong())).thenReturn(pdfBytes);
  }

  protected void stubPurchaseOrderServiceThrowsUnauthorizedOnCreate() {
    lenient()
        .doThrow(
            new com.example.springapi.exceptions.UnauthorizedException(
                com.example.springapi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(purchaseOrderServiceMock)
        .createPurchaseOrder(any());
  }

  protected void stubSuccessfulPurchaseOrderCreate() {
    stubPurchaseOrderRepositorySave(testPurchaseOrder);
    stubOrderSummaryRepositorySave(testOrderSummary);
    stubAddressRepositoryFindExactDuplicate(Optional.empty());
    stubAddressRepositorySave(testAddress);
    stubShipmentRepositorySaveAssigningId(1L);
    stubShipmentProductRepositorySaveAll();
    stubShipmentPackageRepositorySaveAssigningId(1L);
    stubShipmentPackageProductRepositorySaveAll();
  }

  protected void stubPurchaseOrderServiceThrowsUnauthorizedOnUpdate() {
    lenient()
        .doThrow(
            new com.example.springapi.exceptions.UnauthorizedException(
                com.example.springapi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(purchaseOrderServiceMock)
        .updatePurchaseOrder(any());
  }

  protected void stubPurchaseOrderServiceThrowsUnauthorizedOnGetBatches() {
    lenient()
        .doThrow(
            new com.example.springapi.exceptions.UnauthorizedException(
                com.example.springapi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(purchaseOrderServiceMock)
        .getPurchaseOrdersInBatches(any());
  }

  protected void stubPurchaseOrderServiceThrowsUnauthorizedOnGetById() {
    lenient()
        .doThrow(
            new com.example.springapi.exceptions.UnauthorizedException(
                com.example.springapi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(purchaseOrderServiceMock)
        .getPurchaseOrderDetailsById(anyLong());
  }

  protected void stubPurchaseOrderServiceThrowsUnauthorizedOnToggle() {
    lenient()
        .doThrow(
            new com.example.springapi.exceptions.UnauthorizedException(
                com.example.springapi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(purchaseOrderServiceMock)
        .togglePurchaseOrder(anyLong());
  }

  protected void stubPurchaseOrderServiceThrowsUnauthorizedOnApprove() {
    lenient()
        .doThrow(
            new com.example.springapi.exceptions.UnauthorizedException(
                com.example.springapi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(purchaseOrderServiceMock)
        .approvedByPurchaseOrder(anyLong());
  }

  protected void stubPurchaseOrderServiceThrowsUnauthorizedOnReject() {
    lenient()
        .doThrow(
            new com.example.springapi.exceptions.UnauthorizedException(
                com.example.springapi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(purchaseOrderServiceMock)
        .rejectedByPurchaseOrder(anyLong());
  }

  protected void stubPurchaseOrderServiceThrowsUnauthorizedOnGetPdf()
      throws TemplateException, IOException, DocumentException {
    lenient()
        .doThrow(
            new com.example.springapi.exceptions.UnauthorizedException(
                com.example.springapi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(purchaseOrderServiceMock)
        .getPurchaseOrderPdf(anyLong());
  }

  protected void stubPurchaseOrderServiceThrowsUnauthorizedOnBulkCreate() {
    lenient()
        .doThrow(
            new com.example.springapi.exceptions.UnauthorizedException(
                com.example.springapi.ErrorMessages.ERROR_UNAUTHORIZED))
        .when(purchaseOrderServiceMock)
        .bulkCreatePurchaseOrdersAsync(anyList(), anyLong(), anyString(), anyLong());
  }

  protected org.mockito.MockedConstruction<com.example.springapi.helpers.ImgbbHelper>
      stubImgbbHelperUploadResults(List<ImgbbHelper.AttachmentUploadResult> results) {
    return org.mockito.Mockito.mockConstruction(
        com.example.springapi.helpers.ImgbbHelper.class,
        (mock, context) ->
            lenient()
                .when(
                    mock.uploadPurchaseOrderAttachments(
                        anyList(), anyString(), anyString(), anyLong()))
                .thenReturn(results));
  }

  protected org.mockito.MockedConstruction<com.example.springapi.helpers.ImgbbHelper>
      stubImgbbHelperUploadResultsWithDelete(
          List<ImgbbHelper.AttachmentUploadResult> results, int deleteResult) {
    return org.mockito.Mockito.mockConstruction(
        com.example.springapi.helpers.ImgbbHelper.class,
        (mock, context) -> {
          lenient()
              .when(
                  mock.uploadPurchaseOrderAttachments(
                      anyList(), anyString(), anyString(), anyLong()))
              .thenReturn(results);
          lenient().when(mock.deleteMultipleImages(anyList())).thenReturn(deleteResult);
        });
  }

  protected org.mockito.MockedStatic<com.example.springapi.helpers.PdfHelper>
      stubPdfHelperConvertPurchaseOrderHtmlToPdf(byte[] pdfBytes) {
    org.mockito.MockedStatic<com.example.springapi.helpers.PdfHelper> mocked =
        org.mockito.Mockito.mockStatic(com.example.springapi.helpers.PdfHelper.class);
    mocked
        .when(
            () ->
                com.example.springapi.helpers.PdfHelper.convertPurchaseOrderHtmlToPdf(anyString()))
        .thenReturn(pdfBytes);
    return mocked;
  }

  protected org.mockito.MockedStatic<com.example.springapi.helpers.HtmlHelper>
      stubHtmlHelperReplaceBrTags(String htmlResult) {
    org.mockito.MockedStatic<com.example.springapi.helpers.HtmlHelper> mocked =
        org.mockito.Mockito.mockStatic(com.example.springapi.helpers.HtmlHelper.class);
    mocked
        .when(() -> com.example.springapi.helpers.HtmlHelper.replaceBrTags(anyString()))
        .thenReturn(htmlResult);
    return mocked;
  }

  // ==================== ASSERTION HELPERS ====================

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
}
