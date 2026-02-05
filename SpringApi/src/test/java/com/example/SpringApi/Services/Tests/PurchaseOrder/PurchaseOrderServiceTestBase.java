package com.example.SpringApi.Services.Tests.PurchaseOrder;

import com.example.SpringApi.FilterQueryBuilder.PurchaseOrderFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel;
import com.example.SpringApi.Models.RequestModels.PurchaseOrderProductItem;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.PurchaseOrderService;
import com.example.SpringApi.Services.MessageService;
import com.example.SpringApi.Services.Tests.BaseTest;
import com.example.SpringApi.Services.UserLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;

/**
 * Base test class for PurchaseOrderService tests.
 * Provides common setup, test data, and helper methods for all PurchaseOrder test files.
 */
@ExtendWith(MockitoExtension.class)
public abstract class PurchaseOrderServiceTestBase extends BaseTest {

    @Mock
    protected PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    protected AddressRepository addressRepository;

    @Mock
    protected UserRepository userRepository;

    @Mock
    protected LeadRepository leadRepository;

    @Mock
    protected ClientRepository clientRepository;

    @Mock
    protected ResourcesRepository resourcesRepository;

    @Mock
    protected OrderSummaryRepository orderSummaryRepository;

    @Mock
    protected PaymentRepository paymentRepository;

    @Mock
    protected ShipmentRepository shipmentRepository;

    @Mock
    protected ShipmentProductRepository shipmentProductRepository;

    @Mock
    protected ShipmentPackageRepository shipmentPackageRepository;

    @Mock
    protected ShipmentPackageProductRepository shipmentPackageProductRepository;

    @Mock
    protected UserLogService userLogService;

    @Mock
    protected MessageService messageService;

    @Mock
    protected PurchaseOrderFilterQueryBuilder purchaseOrderFilterQueryBuilder;

    @Mock
    protected Environment environment;

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

        // Setup common mocks
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
        lenient().when(environment.getProperty("imageLocation")).thenReturn("imgbb");
        lenient().when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });
        lenient().when(clientRepository.findById(anyLong())).thenReturn(Optional.of(testClient));

        // Set up RequestContextHolder so BaseService.getClientId() works
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("Authorization", "Bearer test-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

        // Create service with manual injection (not @InjectMocks due to complex dependencies)
        purchaseOrderService = new PurchaseOrderService(
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
                environment
        );
    }

    /**
     * Initialize common test data for all PurchaseOrder tests.
     */
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
        PurchaseOrderRequestModel.OrderSummaryData orderSummaryData = new PurchaseOrderRequestModel.OrderSummaryData();
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
        products.add(new PurchaseOrderProductItem(
                TEST_PRODUCT_ID,
                new BigDecimal("100.00"),
                1));
        testPurchaseOrderRequest.setProducts(products);

        // Set shipments (required)
        PurchaseOrderRequestModel.ShipmentProductData shipmentProduct = new PurchaseOrderRequestModel.ShipmentProductData();
        shipmentProduct.setProductId(TEST_PRODUCT_ID);
        shipmentProduct.setAllocatedQuantity(1);
        shipmentProduct.setAllocatedPrice(new BigDecimal("50.00"));

        PurchaseOrderRequestModel.PackageProductData packageProduct = new PurchaseOrderRequestModel.PackageProductData();
        packageProduct.setProductId(TEST_PRODUCT_ID);
        packageProduct.setQuantity(1);

        PurchaseOrderRequestModel.ShipmentPackageData shipmentPackage = new PurchaseOrderRequestModel.ShipmentPackageData();
        shipmentPackage.setPackageId(TEST_PACKAGE_ID);
        shipmentPackage.setQuantityUsed(1);
        shipmentPackage.setTotalCost(new BigDecimal("0.00"));
        shipmentPackage.setProducts(Collections.singletonList(packageProduct));

        PurchaseOrderRequestModel.CourierSelectionData courierSelection = new PurchaseOrderRequestModel.CourierSelectionData();
        courierSelection.setCourierCompanyId(1L);
        courierSelection.setCourierName("Test Courier");
        courierSelection.setCourierRate(new BigDecimal("0.00"));
        courierSelection.setCourierMinWeight(new BigDecimal("0.00"));
        courierSelection.setCourierMetadata("{}");

        PurchaseOrderRequestModel.ShipmentData shipmentData = new PurchaseOrderRequestModel.ShipmentData();
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
}
