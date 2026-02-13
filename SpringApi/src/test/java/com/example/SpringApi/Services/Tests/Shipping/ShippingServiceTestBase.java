package com.example.SpringApi.Services.Tests.Shipping;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Authentication.JwtTokenProvider;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.UnauthorizedException;
import com.example.SpringApi.FilterQueryBuilder.ShipmentFilterQueryBuilder;
import com.example.SpringApi.Helpers.PackagingHelper;
import com.example.SpringApi.Helpers.ShipRocketHelper;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.DatabaseModels.Package;
import com.example.SpringApi.Models.RequestModels.*;
import com.example.SpringApi.Models.ResponseModels.*;
import com.example.SpringApi.Models.ShippingResponseModel.*;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.ClientService;
import com.example.SpringApi.Services.ShippingService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Services.Interface.IPaymentSubTranslator;
import com.example.SpringApi.Services.Interface.IShippingSubTranslator;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Base test class for ShippingService tests.
 * Centralizes mocks, default objects, and stub helpers.
 */
@ExtendWith(MockitoExtension.class)
public abstract class ShippingServiceTestBase {

    // ==================== COMMON TEST CONSTANTS ====================

    protected static final Long DEFAULT_CLIENT_ID = 1L;
    protected static final Long DEFAULT_SHIPMENT_ID = 1L;
    protected static final String DEFAULT_CREATED_USER = "admin";

    @Mock
    protected ClientService clientService;

    @Mock
    protected ProductRepository productRepository;

    @Mock
    protected ProductPickupLocationMappingRepository productPickupLocationMappingRepository;

    @Mock
    protected PackagePickupLocationMappingRepository packagePickupLocationMappingRepository;

    @Mock
    protected PackagingHelper packagingHelper;

    @Mock
    protected ShipmentRepository shipmentRepository;

    @Mock
    protected ReturnShipmentRepository returnShipmentRepository;

    @Mock
    protected ReturnShipmentProductRepository returnShipmentProductRepository;

    @Mock
    protected ShipmentProductRepository shipmentProductRepository;

    @Mock
    protected ShipmentPackageRepository shipmentPackageRepository;

    @Mock
    protected ShipmentPackageProductRepository shipmentPackageProductRepository;

    @Mock
    protected PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    protected OrderSummaryRepository orderSummaryRepository;

    @Mock
    protected IPaymentSubTranslator paymentService;

    @Mock
    protected PickupLocationRepository pickupLocationRepository;

    @Mock
    protected PackageRepository packageRepository;

    @Mock
    protected ClientRepository clientRepository;

    @Mock
    protected UserLogService userLogService;

    @Mock
    protected ShipmentFilterQueryBuilder shipmentFilterQueryBuilder;

    @Mock
    protected ShipRocketHelper shipRocketHelper;

    @Mock
    protected HttpServletRequest request;

    @Mock
    protected JwtTokenProvider jwtTokenProvider;

    @Mock
    protected IShippingSubTranslator shippingServiceMock;

    @Mock
    protected ShippingService shippingServiceControllerMock;

    protected ShippingService shippingService;

    protected ShippingCalculationRequestModel shippingRequest;
    protected OrderOptimizationRequestModel optimizationRequest;
    protected CreateReturnRequestModel createReturnRequest;
    protected CashPaymentRequestModel cashPaymentRequest;
    protected RazorpayVerifyRequestModel razorpayRequest;

    protected PurchaseOrder testPurchaseOrder;
    protected OrderSummary testOrderSummary;
    protected Shipment testShipment;
    protected ShipmentProduct testShipmentProduct;
    protected ShipmentPackage testShipmentPackage;
    protected Product testProduct;
    protected com.example.SpringApi.Models.DatabaseModels.Package testPackage;
    protected Client testClient;
    protected ClientResponseModel testClientResponse;
    protected PickupLocation testPickupLocation;
    protected Address testDeliveryAddress;
    protected Address testPickupAddress;
    protected ReturnShipment testReturnShipment;

    protected static final Long TEST_CLIENT_ID = 1L;
    protected static final Long TEST_USER_ID = 1L;
    protected static final Long TEST_PURCHASE_ORDER_ID = 10L;
    protected static final Long TEST_ORDER_SUMMARY_ID = 20L;
    protected static final Long TEST_SHIPMENT_ID = 30L;
    protected static final Long TEST_PICKUP_LOCATION_ID = 40L;
    protected static final Long TEST_PRODUCT_ID = 50L;
    protected static final Long TEST_PACKAGE_ID = 60L;
    protected static final Long TEST_RETURN_SHIPMENT_ID = 70L;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("Authorization", "Bearer test-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

        shippingService = new TestableShippingService();

        testClient = createTestClient(TEST_CLIENT_ID);
        testClient.setShipRocketEmail("ship@example.com");
        testClient.setShipRocketPassword("ship-pass");

        testClientResponse = new ClientResponseModel();
        testClientResponse.setClientId(TEST_CLIENT_ID);
        testClientResponse.setShipRocketEmail("ship@example.com");
        testClientResponse.setShipRocketPassword("ship-pass");
        testClientResponse.setName("Test Client");

        testPurchaseOrder = new PurchaseOrder();
        testPurchaseOrder.setPurchaseOrderId(TEST_PURCHASE_ORDER_ID);
        testPurchaseOrder.setClientId(TEST_CLIENT_ID);
        testPurchaseOrder.setPurchaseOrderStatus(PurchaseOrder.Status.PENDING_APPROVAL.getValue());
        testPurchaseOrder.setVendorNumber("VN-001");

        testDeliveryAddress = new Address();
        testDeliveryAddress.setAddressId(100L);
        testDeliveryAddress.setStreetAddress("123 Main");
        testDeliveryAddress.setCity("City");
        testDeliveryAddress.setState("State");
        testDeliveryAddress.setPostalCode("400001");
        testDeliveryAddress.setCountry("India");
        testDeliveryAddress.setNameOnAddress("Customer");
        testDeliveryAddress.setEmailOnAddress("cust@example.com");
        testDeliveryAddress.setPhoneOnAddress("9999999999");

        testOrderSummary = new OrderSummary();
        testOrderSummary.setOrderSummaryId(TEST_ORDER_SUMMARY_ID);
        testOrderSummary.setEntityType(OrderSummary.EntityType.PURCHASE_ORDER.getValue());
        testOrderSummary.setEntityId(TEST_PURCHASE_ORDER_ID);
        testOrderSummary.setEntityAddress(testDeliveryAddress);
        testOrderSummary.setSubtotal(new BigDecimal("1000.00"));

        testPickupAddress = new Address();
        testPickupAddress.setAddressId(200L);
        testPickupAddress.setStreetAddress("Warehouse St");
        testPickupAddress.setCity("City");
        testPickupAddress.setState("State");
        testPickupAddress.setPostalCode("400002");
        testPickupAddress.setCountry("India");
        testPickupAddress.setNameOnAddress("Warehouse");
        testPickupAddress.setEmailOnAddress("warehouse@example.com");
        testPickupAddress.setPhoneOnAddress("8888888888");

        testPickupLocation = new PickupLocation();
        testPickupLocation.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        testPickupLocation.setAddressNickName("WH1");
        testPickupLocation.setAddress(testPickupAddress);

        testShipment = createTestShipment(TEST_SHIPMENT_ID);
        testShipment.setOrderSummaryId(TEST_ORDER_SUMMARY_ID);
        testShipment.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        testShipment.setSelectedCourierCompanyId(123L);
        testShipment.setShipRocketStatus("NEW");
        testShipment.setShipRocketOrderId("1001");
        testShipment.setOrderSummary(testOrderSummary);
        testShipment.setPickupLocation(testPickupLocation);
        testShipment.setShipmentProducts(new ArrayList<>());
        testShipment.setShipmentPackages(new ArrayList<>());

        testShipmentProduct = new ShipmentProduct();
        testShipmentProduct.setShipmentId(TEST_SHIPMENT_ID);
        testShipmentProduct.setProductId(TEST_PRODUCT_ID);
        testShipmentProduct.setAllocatedQuantity(2);

        testShipmentPackage = new ShipmentPackage();
        testShipmentPackage.setShipmentId(TEST_SHIPMENT_ID);
        testShipmentPackage.setPackageId(TEST_PACKAGE_ID);
        testShipmentPackage.setQuantityUsed(1);

        testPackage = new com.example.SpringApi.Models.DatabaseModels.Package();
        testPackage.setPackageId(TEST_PACKAGE_ID);
        testPackage.setLength(10);
        testPackage.setBreadth(10);
        testPackage.setHeight(10);
        testPackage.setClientId(TEST_CLIENT_ID);
        testPackage.setPackageName("Test Package");
        testPackage.setPackageType("BOX");
        testPackage.setMaxWeight(new BigDecimal("5.00"));
        testPackage.setStandardCapacity(100);
        testPackage.setPricePerUnit(new BigDecimal("10.00"));
        testPackage.setIsDeleted(false);

        testProduct = new Product();
        testProduct.setProductId(TEST_PRODUCT_ID);
        testProduct.setTitle("Test Product");
        testProduct.setPrice(new BigDecimal("100.00"));
        testProduct.setDiscount(BigDecimal.ZERO);
        testProduct.setWeightKgs(new BigDecimal("1.00"));
        testProduct.setLength(new BigDecimal("10"));
        testProduct.setBreadth(new BigDecimal("10"));
        testProduct.setHeight(new BigDecimal("10"));
        testProduct.setReturnWindowDays(7);

        shippingRequest = new ShippingCalculationRequestModel();
        shippingRequest.setDeliveryPostcode("400001");
        shippingRequest.setIsCod(false);
        ShippingCalculationRequestModel.PickupLocationShipment pls = new ShippingCalculationRequestModel.PickupLocationShipment();
        pls.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        pls.setPickupPostcode("400002");
        pls.setTotalWeightKgs(new BigDecimal("1.00"));
        pls.setTotalQuantity(2);
        pls.setLocationName("WH1");
        pls.setProductIds(List.of(TEST_PRODUCT_ID));
        shippingRequest.setPickupLocations(List.of(pls));

        optimizationRequest = new OrderOptimizationRequestModel();
        optimizationRequest.setDeliveryPostcode("400001");
        optimizationRequest.setIsCod(false);
        optimizationRequest.setProductQuantities(new HashMap<>(Map.of(TEST_PRODUCT_ID, 2)));

        createReturnRequest = new CreateReturnRequestModel();
        createReturnRequest.setShipmentId(TEST_SHIPMENT_ID);
        CreateReturnRequestModel.ReturnProductItem returnItem = new CreateReturnRequestModel.ReturnProductItem();
        returnItem.setProductId(TEST_PRODUCT_ID);
        returnItem.setQuantity(1);
        returnItem.setReason("Damaged");
        createReturnRequest.setProducts(List.of(returnItem));
        createReturnRequest.setLength(new BigDecimal("10"));
        createReturnRequest.setBreadth(new BigDecimal("10"));
        createReturnRequest.setHeight(new BigDecimal("10"));
        createReturnRequest.setWeight(new BigDecimal("1.00"));

        cashPaymentRequest = new CashPaymentRequestModel();
        cashPaymentRequest.setPurchaseOrderId(TEST_PURCHASE_ORDER_ID);
        cashPaymentRequest.setAmount(new BigDecimal("100.00"));
        cashPaymentRequest.setPaymentDate(LocalDate.now());

        razorpayRequest = new RazorpayVerifyRequestModel();
        razorpayRequest.setRazorpayOrderId("order_1");
        razorpayRequest.setRazorpayPaymentId("pay_1");
        razorpayRequest.setRazorpaySignature("sig_1");

        testReturnShipment = new ReturnShipment();
        testReturnShipment.setReturnShipmentId(TEST_RETURN_SHIPMENT_ID);
        testReturnShipment.setClientId(TEST_CLIENT_ID);
        testReturnShipment.setShipRocketReturnOrderId("2001");
        testReturnShipment.setShipRocketReturnStatus(ReturnShipment.ReturnStatus.RETURN_PENDING.getValue());

        stubUserLogServiceLogData(true);
        stubPackageRepositoryFindById(testPackage);
    }

    protected class TestableShippingService extends ShippingService {
        TestableShippingService() {
            super(clientService, productRepository, productPickupLocationMappingRepository,
                    packagePickupLocationMappingRepository, packagingHelper, shipmentRepository,
                    returnShipmentRepository, returnShipmentProductRepository, shipmentProductRepository,
                    shipmentPackageRepository, shipmentPackageProductRepository, purchaseOrderRepository,
                    orderSummaryRepository, paymentService, pickupLocationRepository, packageRepository,
                    clientRepository, userLogService, shipmentFilterQueryBuilder,
                    org.mockito.Mockito.mock(JwtTokenProvider.class),
                    org.mockito.Mockito.mock(HttpServletRequest.class));
        }

        protected ShipRocketHelper createShipRocketHelper(String email, String password) {
            return shipRocketHelper;
        }
    }

    // =============================
    // Stub helpers
    // =============================

    protected void stubPurchaseOrderRepositoryFindById(PurchaseOrder po) {
        lenient().when(purchaseOrderRepository.findById(anyLong()))
                .thenReturn(po != null ? Optional.of(po) : Optional.empty());
    }

    protected void stubOrderSummaryRepositoryFindByEntityTypeAndEntityId(OrderSummary summary) {
        lenient().when(orderSummaryRepository.findByEntityTypeAndEntityId(anyString(), anyLong()))
                .thenReturn(summary != null ? Optional.of(summary) : Optional.empty());
    }

    protected void stubPackageRepositoryFindById(com.example.SpringApi.Models.DatabaseModels.Package pkg) {
        lenient().when(packageRepository.findById(anyLong()))
                .thenReturn(pkg != null ? Optional.of(pkg) : Optional.empty());
    }

    protected void stubShipmentRepositoryFindByOrderSummaryId(List<Shipment> shipments) {
        lenient().when(shipmentRepository.findByOrderSummaryId(anyLong())).thenReturn(shipments);
    }

    protected void stubShipmentRepositoryFindByShipmentIdAndClientId(Shipment shipment) {
        lenient().when(shipmentRepository.findByShipmentIdAndClientId(anyLong(), anyLong()))
                .thenReturn(shipment);
    }

    protected void stubShipmentRepositoryFindById(Shipment shipment) {
        lenient().when(shipmentRepository.findById(anyLong()))
                .thenReturn(shipment != null ? Optional.of(shipment) : Optional.empty());
    }

    protected void stubShipmentRepositorySave(Shipment shipment) {
        lenient().when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);
    }

    protected void stubShipmentProductRepositoryFindByShipmentId(List<ShipmentProduct> products) {
        lenient().when(shipmentProductRepository.findByShipmentId(anyLong())).thenReturn(products);
    }

    protected void stubShipmentPackageRepositoryFindByShipmentId(List<ShipmentPackage> packages) {
        lenient().when(shipmentPackageRepository.findByShipmentId(anyLong())).thenReturn(packages);
    }

    protected void stubProductPickupLocationMappingRepositoryFindByProductIdAndPickupLocationId(
            ProductPickupLocationMapping mapping) {
        lenient().when(productPickupLocationMappingRepository.findByProductIdAndPickupLocationId(anyLong(), anyLong()))
                .thenReturn(mapping != null ? Optional.of(mapping) : Optional.empty());
    }

    protected void stubPackagePickupLocationMappingRepositoryFindByPackageIdAndPickupLocationId(
            PackagePickupLocationMapping mapping) {
        lenient().when(packagePickupLocationMappingRepository.findByPackageIdAndPickupLocationId(anyLong(), anyLong()))
                .thenReturn(mapping != null ? Optional.of(mapping) : Optional.empty());
    }

    protected void stubPaymentServiceRecordCashPayment(PaymentVerificationResponseModel response) {
        lenient().when(paymentService.recordCashPayment(any(CashPaymentRequestModel.class))).thenReturn(response);
    }

    protected void stubPaymentServiceVerifyPayment(PaymentVerificationResponseModel response) {
        lenient().when(paymentService.verifyPayment(any(RazorpayVerifyRequestModel.class))).thenReturn(response);
    }

    protected void stubClientRepositoryFindById(Client client) {
        lenient().when(clientRepository.findById(anyLong()))
                .thenReturn(client != null ? Optional.of(client) : Optional.empty());
    }

    protected void stubPickupLocationRepositoryFindById(PickupLocation location) {
        lenient().when(pickupLocationRepository.findById(anyLong()))
                .thenReturn(location != null ? Optional.of(location) : Optional.empty());
    }

    protected void stubUserLogServiceLogData(boolean result) {
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(result);
    }

    protected void stubClientServiceGetClientById(ClientResponseModel client) {
        lenient().when(clientService.getClientById(anyLong())).thenReturn(client);
    }

    protected void stubShipRocketHelperCreateCustomOrder(ShipRocketOrderResponseModel response) {
        lenient().when(shipRocketHelper.createCustomOrder(any(ShipRocketOrderRequestModel.class))).thenReturn(response);
    }

    protected void stubShipRocketHelperAssignAwbAsJson(String json) {
        lenient().when(shipRocketHelper.assignAwbAsJson(anyLong(), anyLong())).thenReturn(json);
    }

    protected void stubShipRocketHelperGeneratePickupAsJson(String json) {
        lenient().when(shipRocketHelper.generatePickupAsJson(anyLong())).thenReturn(json);
    }

    protected void stubShipRocketHelperGenerateManifest(String url) {
        lenient().when(shipRocketHelper.generateManifest(anyLong())).thenReturn(url);
    }

    protected void stubShipRocketHelperGenerateLabel(String url) {
        lenient().when(shipRocketHelper.generateLabel(anyLong())).thenReturn(url);
    }

    protected void stubShipRocketHelperGenerateInvoice(String url) {
        lenient().when(shipRocketHelper.generateInvoice(anyLong())).thenReturn(url);
    }

    protected void stubShipRocketHelperGetTrackingAsJson(String json) {
        lenient().when(shipRocketHelper.getTrackingAsJson(anyString())).thenReturn(json);
    }

    protected void stubShipRocketHelperGetOrderDetailsAsJson(String json) {
        lenient().when(shipRocketHelper.getOrderDetailsAsJson(anyString())).thenReturn(json);
    }

    protected void stubShipRocketHelperGetAvailableShippingOptions(ShippingOptionsResponseModel response) {
        lenient().when(shipRocketHelper.getAvailableShippingOptions(anyString(), anyString(), anyBoolean(), anyString()))
                .thenReturn(response);
    }

    protected void stubShipRocketHelperGetAvailableShippingOptionsThrows(RuntimeException ex) {
        lenient().when(shipRocketHelper.getAvailableShippingOptions(anyString(), anyString(), anyBoolean(), anyString()))
                .thenThrow(ex);
    }

    protected void stubShipRocketHelperCancelOrders() {
        lenient().doNothing().when(shipRocketHelper).cancelOrders(anyList());
    }

    protected void stubShipRocketHelperCancelOrdersThrows(RuntimeException ex) {
        lenient().doThrow(ex).when(shipRocketHelper).cancelOrders(anyList());
    }

    protected void stubShipRocketHelperCreateReturnOrderAsJson(String json) {
        lenient().when(shipRocketHelper.createReturnOrderAsJson(any(ShipRocketReturnOrderRequestModel.class)))
                .thenReturn(json);
    }

    protected void stubShipRocketHelperCreateReturnOrderAsJsonThrows(RuntimeException ex) {
        lenient().when(shipRocketHelper.createReturnOrderAsJson(any(ShipRocketReturnOrderRequestModel.class)))
                .thenThrow(ex);
    }

    protected void stubShipRocketHelperAssignReturnAwbAsJson(String json) {
        lenient().when(shipRocketHelper.assignReturnAwbAsJson(anyLong())).thenReturn(json);
    }

    protected void stubShipRocketHelperAssignReturnAwbAsJsonThrows(RuntimeException ex) {
        lenient().when(shipRocketHelper.assignReturnAwbAsJson(anyLong())).thenThrow(ex);
    }

    protected void stubShipRocketHelperGetWalletBalance(Double balance) {
        lenient().when(shipRocketHelper.getWalletBalance()).thenReturn(balance);
    }

    protected void stubReturnShipmentRepositorySave(ReturnShipment returnShipment) {
        lenient().when(returnShipmentRepository.save(any(ReturnShipment.class))).thenReturn(returnShipment);
    }

    protected void stubReturnShipmentRepositoryFindByReturnShipmentIdAndClientId(ReturnShipment returnShipment) {
        lenient().when(returnShipmentRepository.findByReturnShipmentIdAndClientId(anyLong(), anyLong()))
                .thenReturn(returnShipment);
    }

    protected void stubReturnShipmentProductRepositorySave(ReturnShipmentProduct product) {
        lenient().when(returnShipmentProductRepository.save(any(ReturnShipmentProduct.class))).thenReturn(product);
    }

    protected void stubProductRepositoryFindById(Product product) {
        lenient().when(productRepository.findById(anyLong()))
                .thenReturn(product != null ? Optional.of(product) : Optional.empty());
    }

    protected void stubProductRepositoryFindAllById(List<Product> products) {
        lenient().when(productRepository.findAllById(anySet())).thenReturn(products);
    }

    protected void stubProductRepositoryFindAllByIdThrows(RuntimeException ex) {
        lenient().when(productRepository.findAllById(anySet())).thenThrow(ex);
    }

    protected void stubShippingServiceMockCalculateShippingUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
            .when(shippingServiceMock).calculateShipping(any(ShippingCalculationRequestModel.class));
    }

    protected void stubShippingServiceMockOptimizeOrderUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
            .when(shippingServiceMock).optimizeOrder(any(OrderOptimizationRequestModel.class));
    }

    protected void stubShippingServiceMockCreateReturnUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
            .when(shippingServiceMock).createReturn(any(CreateReturnRequestModel.class));
    }

    protected void stubShippingServiceMockCancelShipmentUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
            .when(shippingServiceMock).cancelShipment(anyLong());
    }

    protected void stubShippingServiceMockCancelReturnShipmentUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
            .when(shippingServiceMock).cancelReturnShipment(anyLong());
    }

    protected void stubShippingServiceMockGetShipmentByIdUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
            .when(shippingServiceMock).getShipmentById(anyLong());
    }

    protected void stubShippingServiceMockGetShipmentsInBatchesUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
            .when(shippingServiceMock).getShipmentsInBatches(any(PaginationBaseRequestModel.class));
    }

    protected void stubShippingServiceMockGetWalletBalanceUnauthorized() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
            .when(shippingServiceMock).getWalletBalance();
    }

    protected void stubShippingServiceProcessShipmentsAfterPaymentApprovalUnauthorizedCash() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
            .when(shippingServiceControllerMock)
            .processShipmentsAfterPaymentApproval(anyLong(), any(CashPaymentRequestModel.class));
    }

    protected void stubShippingServiceProcessShipmentsAfterPaymentApprovalUnauthorizedOnline() {
        lenient().doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
            .when(shippingServiceControllerMock)
            .processShipmentsAfterPaymentApproval(anyLong(), any(RazorpayVerifyRequestModel.class));
    }

    protected void stubProductPickupLocationMappingRepositoryFindByProductIdWithPickupLocationAndAddress(
            List<ProductPickupLocationMapping> mappings) {
        lenient().when(productPickupLocationMappingRepository.findByProductIdWithPickupLocationAndAddress(anyLong()))
                .thenReturn(mappings);
    }

    protected void stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdsWithPackages(
            List<PackagePickupLocationMapping> mappings) {
        lenient().when(packagePickupLocationMappingRepository.findByPickupLocationIdsWithPackages(anyList()))
                .thenReturn(mappings);
    }

    protected void stubPackagingHelperCalculatePackaging(PackagingHelper.PackagingEstimateResult estimate) {
        lenient().when(packagingHelper.calculatePackaging(any(PackagingHelper.ProductDimension.class), anyList()))
                .thenReturn(estimate);
    }

    protected void stubPackagingHelperCalculatePackagingForMultipleProducts(
            PackagingHelper.MultiProductPackagingResult result) {
        lenient().when(packagingHelper.calculatePackagingForMultipleProducts(anyMap(), anyList()))
                .thenReturn(result);
    }

    protected void stubShipmentFilterQueryBuilderFindPaginatedEntitiesWithMultipleFilters(Page<Shipment> page) {
        lenient().when(shipmentFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(),
            org.mockito.ArgumentMatchers.<List<Long>>any(),
                anyString(),
            org.mockito.ArgumentMatchers.<List<com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.FilterCondition>>any(),
                any(Pageable.class)))
                .thenReturn(page);
    }

    protected void stubShipmentFilterQueryBuilderGetColumnType(String column, String type) {
        lenient().when(shipmentFilterQueryBuilder.getColumnType(eq(column))).thenReturn(type);
    }

    protected Page<Shipment> createShipmentPage(List<Shipment> shipments) {
        return new PageImpl<>(shipments);
    }

    protected ProductPickupLocationMapping createProductPickupLocationMapping(Long productId, Long locationId, int stock) {
        ProductPickupLocationMapping mapping = new ProductPickupLocationMapping();
        mapping.setProductId(productId);
        mapping.setPickupLocationId(locationId);
        mapping.setAvailableStock(stock);
        mapping.setPickupLocation(testPickupLocation);
        return mapping;
    }

    protected PackagePickupLocationMapping createPackagePickupLocationMapping(Long packageId, Long locationId, int qty) {
        PackagePickupLocationMapping mapping = new PackagePickupLocationMapping();
        mapping.setPackageId(packageId);
        mapping.setPickupLocationId(locationId);
        mapping.setAvailableQuantity(qty);
        com.example.SpringApi.Models.DatabaseModels.Package pkg = new com.example.SpringApi.Models.DatabaseModels.Package();
        pkg.setPackageId(packageId);
        pkg.setPackageName("PKG");
        pkg.setPackageType("BOX");
        pkg.setLength(10);
        pkg.setBreadth(10);
        pkg.setHeight(10);
        pkg.setMaxWeight(new BigDecimal("10"));
        pkg.setPricePerUnit(new BigDecimal("5"));
        mapping.setPackageEntity(pkg);
        return mapping;
    }

    protected ShippingOptionsResponseModel createShippingOptions(double... rates) {
        ShippingOptionsResponseModel response = new ShippingOptionsResponseModel();
        ShippingOptionsResponseModel.Data data = new ShippingOptionsResponseModel.Data();
        data.available_courier_companies = new ArrayList<>();
        for (double rate : rates) {
            ShippingOptionsResponseModel.AvailableCourierCompany courier = new ShippingOptionsResponseModel.AvailableCourierCompany();
            courier.rate = rate;
            courier.courier_company_id = 1;
            courier.courier_name = "Courier";
            data.available_courier_companies.add(courier);
        }
        response.data = data;
        return response;
    }

    protected ShipRocketOrderResponseModel createValidShipRocketOrderResponse() {
        ShipRocketOrderResponseModel response = new ShipRocketOrderResponseModel();
        response.order_id = 1001L;
        response.shipment_id = 2001L;
        response.status = Shipment.ShipRocketStatus.NEW.getValue();
        response.awb_code = "AWB-1";
        response.courier_company_id = "123";
        response.courier_name = "Courier";
        return response;
    }

    protected String createValidAwbJson() {
        return "{\"awb_assign_status\":1,\"response\":{\"data\":{\"awb_code\":\"AWB-1\"}}}";
    }

    protected PackagingHelper.PackagingEstimateResult createPackagingEstimateResult(int requested, int packed) {
        return new PackagingHelper.PackagingEstimateResult(Collections.emptyList(), requested, packed);
    }

        protected PackagingHelper.MultiProductPackagingResult createMultiProductPackagingResult() {
        PackagingHelper.MultiProductPackageUsageResult usage = new PackagingHelper.MultiProductPackageUsageResult(
            TEST_PACKAGE_ID, "PKG", "BOX", 1, new BigDecimal("5"), Map.of(TEST_PRODUCT_ID, 1));
        return new PackagingHelper.MultiProductPackagingResult(
            List.of(usage),
            Map.of(TEST_PRODUCT_ID, 1),
            Map.of(TEST_PRODUCT_ID, 1));
        }

        // ==================== FACTORY METHODS ====================

        protected Client createTestClient(Long clientId) {
            Client client = new Client();
            client.setClientId(clientId);
            client.setName("Test Client");
            client.setCreatedUser(DEFAULT_CREATED_USER);
            client.setModifiedUser(DEFAULT_CREATED_USER);
            client.setCreatedAt(LocalDateTime.now());
            client.setUpdatedAt(LocalDateTime.now());
            return client;
        }

        protected Shipment createTestShipment(Long shipmentId) {
            Shipment shipment = new Shipment();
            shipment.setShipmentId(shipmentId);
            shipment.setClientId(DEFAULT_CLIENT_ID);
            shipment.setShipRocketOrderId("SR" + shipmentId);
            shipment.setShipRocketStatus("NEW");
            shipment.setTotalWeightKgs(new BigDecimal("1.00"));
            shipment.setCreatedUser(DEFAULT_CREATED_USER);
            shipment.setModifiedUser(DEFAULT_CREATED_USER);
            shipment.setCreatedAt(LocalDateTime.now());
            shipment.setUpdatedAt(LocalDateTime.now());
            return shipment;
        }

        protected PaginationBaseRequestModel createValidPaginationRequest() {
            PaginationBaseRequestModel request = new PaginationBaseRequestModel();
            request.setStart(0);
            request.setEnd(10);
            request.setFilters(new ArrayList<>());
            return request;
        }

        protected PaginationBaseRequestModel.FilterCondition createFilterCondition(String column, String operator, String value) {
            PaginationBaseRequestModel.FilterCondition condition = new PaginationBaseRequestModel.FilterCondition();
            condition.setColumn(column);
            condition.setOperator(operator);
            condition.setValue(value);
            return condition;
        }

        protected void assertThrowsBadRequest(String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
            BadRequestException ex = assertThrows(BadRequestException.class, executable);
            assertEquals(expectedMessage, ex.getMessage());
        }

        protected void assertThrowsNotFound(String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
            NotFoundException ex = assertThrows(NotFoundException.class, executable);
            assertEquals(expectedMessage, ex.getMessage());
        }
}
