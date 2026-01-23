package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Constants.EntityType;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.FilterQueryBuilder.PurchaseOrderFilterQueryBuilder;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel;
import com.example.SpringApi.Models.RequestModels.PurchaseOrderProductItem;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.PurchaseOrderResponseModel;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.PurchaseOrderService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Services.MessageService;
import com.example.SpringApi.ErrorMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PurchaseOrderService.
 *
 * This test class provides comprehensive coverage of PurchaseOrderService
 * methods including:
 * - CRUD operations (create, read, update, toggle)
 * - Batch retrieval with pagination and filtering
 * - Approval/rejection workflow
 * - PDF generation
 * - Attachment handling with ImgBB
 * - Validation and error handling
 *
 * @author SpringApi Team
 * @version 2.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseOrderService Unit Tests")
class PurchaseOrderServiceTest extends BaseTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LeadRepository leadRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ResourcesRepository resourcesRepository;

    @Mock
    private OrderSummaryRepository orderSummaryRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentProductRepository shipmentProductRepository;

    @Mock
    private ShipmentPackageRepository shipmentPackageRepository;

    @Mock
    private ShipmentPackageProductRepository shipmentPackageProductRepository;

    @Mock
    private UserLogService userLogService;

    @Mock
    private PurchaseOrderFilterQueryBuilder purchaseOrderFilterQueryBuilder;

    @Mock
    private MessageService messageService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private Environment environment;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    private PurchaseOrder testPurchaseOrder;
    private PurchaseOrderRequestModel testPurchaseOrderRequest;
    private Address testAddress;
    private OrderSummary testOrderSummary;
    private Lead testLead;
    private User testUser;
    private Client testClient;
    private AddressRequestModel testAddressRequest;

    private static final Long TEST_PO_ID = 1L;
    private static final Long TEST_ADDRESS_ID = 1L;
    private static final Long TEST_LEAD_ID = 1L;
    private static final String TEST_VENDOR_NUMBER = "VEN-001";
    private static final Long TEST_PICKUP_LOCATION_ID = 1L;
    private static final Long TEST_PACKAGE_ID = 1L;
    private static final Long TEST_PRODUCT_ID = 1L;

    @BeforeEach
    void setUp() {
        // Initialize test data using BaseTest helpers where applicable
        initializeTestData();

        // Setup common mocks
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
        lenient().when(environment.getProperty("imageLocation")).thenReturn("imgbb");
        lenient().when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });

        // Mock BaseService methods
        lenient().doReturn(DEFAULT_CLIENT_ID).when(purchaseOrderService).getClientId();
        lenient().doReturn(DEFAULT_USER_ID).when(purchaseOrderService).getUserId();
        lenient().doReturn(DEFAULT_LOGIN_NAME).when(purchaseOrderService).getUser();
    }

    private void initializeTestData() {
        // Initialize test client
        testClient = createTestClient(DEFAULT_CLIENT_ID);
        testClient.setImgbbApiKey("test-api-key");
        testClient.setSupportEmail("support@test.com");

        // Initialize test user
        testUser = createTestUser(DEFAULT_USER_ID);

        // Initialize test lead
        testLead = new Lead();
        testLead.setLeadId(TEST_LEAD_ID);
        testLead.setFirstName("Test");
        testLead.setLastName("Lead");
        testLead.setEmail("lead@test.com");

        // Initialize test address request
        testAddressRequest = createValidAddressRequest(TEST_ADDRESS_ID, DEFAULT_USER_ID, DEFAULT_CLIENT_ID);
        testAddressRequest.setAddressType("SHIPPING");

        // Initialize test address
        testAddress = createTestAddress(testAddressRequest, DEFAULT_CREATED_USER);

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
        testOrderSummary.setClientId(DEFAULT_CLIENT_ID);
        testOrderSummary.setCreatedUser(DEFAULT_CREATED_USER);
        testOrderSummary.setModifiedUser(DEFAULT_CREATED_USER);

        // Initialize test purchase order request
        testPurchaseOrderRequest = new PurchaseOrderRequestModel();
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
        testPurchaseOrder.setClientId(DEFAULT_CLIENT_ID);
        testPurchaseOrder.setIsDeleted(false);
        testPurchaseOrder.setCreatedAt(LocalDateTime.now());
        testPurchaseOrder.setUpdatedAt(LocalDateTime.now());
        testPurchaseOrder.setCreatedUser(DEFAULT_CREATED_USER);
        testPurchaseOrder.setModifiedUser(DEFAULT_CREATED_USER);
    }

    // ==================== getPurchaseOrdersInBatches Tests ====================

    @Nested
    @DisplayName("getPurchaseOrdersInBatches Tests")
    class GetPurchaseOrdersInBatchesTests {

        @Test
        @DisplayName("Get POs In Batches - Success")
        void getPurchaseOrdersInBatches_Success() {
            // Arrange
            PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
            paginationRequest.setStart(0);
            paginationRequest.setEnd(10);
            paginationRequest.setIncludeDeleted(false);

            List<PurchaseOrder> purchaseOrders = Arrays.asList(testPurchaseOrder);
            Page<PurchaseOrder> page = new PageImpl<>(purchaseOrders);

            lenient().when(purchaseOrderFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), any(), any(), any(), any(), anyBoolean(), any(Pageable.class))).thenReturn(page);

            when(resourcesRepository.findByEntityIdAndEntityType(TEST_PO_ID, EntityType.PURCHASE_ORDER))
                    .thenReturn(new ArrayList<>());

            when(orderSummaryRepository.findByEntityTypeAndEntityId(
                    eq(OrderSummary.EntityType.PURCHASE_ORDER.getValue()), eq(TEST_PO_ID)))
                    .thenReturn(Optional.empty());

            // Act
            PaginationBaseResponseModel<PurchaseOrderResponseModel> result = purchaseOrderService
                    .getPurchaseOrdersInBatches(paginationRequest);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getData().size());
            assertEquals(1L, result.getTotalDataCount());
            verify(purchaseOrderFilterQueryBuilder).findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), any(), any(), any(), any(), anyBoolean(), any(Pageable.class));
        }

        @Test
        @DisplayName("Get POs In Batches - Invalid Column Name")
        void getPurchaseOrdersInBatches_InvalidColumnName() {
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
                    () -> purchaseOrderService.getPurchaseOrdersInBatches(paginationRequest));

            assertEquals("Invalid column name: invalidColumn", exception.getMessage());
        }

        @Test
        @DisplayName("Get POs In Batches - Triple Loop Validation")
        void getPurchaseOrdersInBatches_TripleLoopValidation() {
            List<String> validColumns = Arrays.asList(
                "purchaseOrderId", "vendorNumber", "priority", "orderStatus", "paymentStatus",
                "totalAmount", "amountPaid", "expectedShipmentDate", "termsConditionsHtml",
                "createdUser", "modifiedUser", "createdAt", "updatedAt",
                "leadId", "assignedLeadId", "addressId", "clientId"
            );

            List<String> invalidColumns = Arrays.asList("invalidCol", "dropTable", "select");

            List<String> validOperators = Arrays.asList(
                "equals", "contains", "startsWith", "endsWith",
                "greaterThan", "lessThan", "greaterThanOrEqual", "lessThanOrEqual"
            );

            List<String> invalidOperators = Arrays.asList("invalidOp", "like");

            List<String> values = Arrays.asList("test", "123", "2023-01-01");

            Page<PurchaseOrder> emptyPage = new PageImpl<>(Collections.emptyList());
            lenient().when(purchaseOrderFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), any(), any(), any(), any(), anyBoolean(), any(Pageable.class)))
                    .thenReturn(emptyPage);
            lenient().when(purchaseOrderFilterQueryBuilder.getColumnType(anyString())).thenReturn("string");

            for (String column : validColumns) {
                for (String operator : validOperators) {
                    for (String value : values) {
                        PaginationBaseRequestModel req = new PaginationBaseRequestModel();
                        req.setStart(0);
                        req.setEnd(10);
                        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                        filter.setColumn(column);
                        filter.setOperator(operator);
                        filter.setValue(value);
                        req.setFilters(Collections.singletonList(filter));

                        // Mock specific column types if needed, or assume default string handling for validation pass
                        // The service relies on FilterQueryBuilder for validation mainly.

                        assertDoesNotThrow(() -> purchaseOrderService.getPurchaseOrdersInBatches(req));
                    }
                }
            }

            for (String column : invalidColumns) {
                PaginationBaseRequestModel req = new PaginationBaseRequestModel();
                req.setStart(0);
                req.setEnd(10);
                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                filter.setColumn(column);
                filter.setOperator("equals");
                filter.setValue("val");
                req.setFilters(Collections.singletonList(filter));

                BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.getPurchaseOrdersInBatches(req));
                assertTrue(ex.getMessage().contains("Invalid column name"));
            }

            for (String operator : invalidOperators) {
                PaginationBaseRequestModel req = new PaginationBaseRequestModel();
                req.setStart(0);
                req.setEnd(10);
                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                filter.setColumn("vendorNumber");
                filter.setOperator(operator);
                filter.setValue("val");
                req.setFilters(Collections.singletonList(filter));

                BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.getPurchaseOrdersInBatches(req));
                assertTrue(ex.getMessage().contains("Invalid operator"));
            }
        }
    }

    // ==================== createPurchaseOrder Tests ====================

    @Nested
    @DisplayName("createPurchaseOrder Tests")
    class CreatePurchaseOrderTests {

        @Test
        @DisplayName("Create PO - Success Without Attachments")
        void createPurchaseOrder_Success_WithoutAttachments() {
            // Arrange
            testPurchaseOrderRequest.setAttachments(null);

            when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                    any(), any(), any(), any(), any()))
                    .thenReturn(Optional.empty());
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
            when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);
            AtomicLong shipmentIdSeq = new AtomicLong(1L);
            when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
                Shipment s = invocation.getArgument(0);
                s.setShipmentId(shipmentIdSeq.getAndIncrement());
                return s;
            });
            when(shipmentProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            AtomicLong shipmentPackageIdSeq = new AtomicLong(1L);
            when(shipmentPackageRepository.save(any(ShipmentPackage.class))).thenAnswer(invocation -> {
                ShipmentPackage p = invocation.getArgument(0);
                p.setShipmentPackageId(shipmentPackageIdSeq.getAndIncrement());
                return p;
            });
            when(shipmentPackageProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            assertDoesNotThrow(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));

            // Assert
            verify(addressRepository, atLeastOnce()).save(any(Address.class));
            verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
            verify(orderSummaryRepository, times(1)).save(any(OrderSummary.class));
            verify(userLogService, times(1)).logDataWithContext(anyLong(), anyString(), anyLong(), anyString(),
                    anyString());
        }

        @Test
        @DisplayName("Create PO - Success With Attachments")
        void createPurchaseOrder_Success_WithAttachments() {
            // Arrange
            Map<String, String> attachments = new HashMap<>();
            attachments.put("receipt.pdf", "base64-data-here");
            testPurchaseOrderRequest.setAttachments(attachments);

            lenient()
                    .when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(),
                            any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(Optional.empty());
            lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            lenient().when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
            lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
            lenient().when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);
            AtomicLong shipmentIdSeq = new AtomicLong(1L);
            lenient().when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
                Shipment s = invocation.getArgument(0);
                s.setShipmentId(shipmentIdSeq.getAndIncrement());
                return s;
            });
            lenient().when(shipmentProductRepository.saveAll(anyList()))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            AtomicLong shipmentPackageIdSeq = new AtomicLong(1L);
            lenient().when(shipmentPackageRepository.save(any(ShipmentPackage.class))).thenAnswer(invocation -> {
                ShipmentPackage p = invocation.getArgument(0);
                p.setShipmentPackageId(shipmentPackageIdSeq.getAndIncrement());
                return p;
            });
            lenient().when(shipmentPackageProductRepository.saveAll(anyList()))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            lenient().when(resourcesRepository.save(any(Resources.class))).thenReturn(new Resources());

            try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                    (mock, context) -> {
                        List<ImgbbHelper.AttachmentUploadResult> results = Arrays.asList(
                                new ImgbbHelper.AttachmentUploadResult(
                                        "https://i.ibb.co/test/receipt.pdf",
                                        "delete-hash-123",
                                        null));
                        when(mock.uploadPurchaseOrderAttachments(anyList(), anyString(), anyString(), anyLong()))
                                .thenReturn(results);
                    })) {

                // Act
                assertDoesNotThrow(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));

                // Assert
                verify(addressRepository, atLeastOnce()).save(any(Address.class));
                verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
                verify(orderSummaryRepository, times(1)).save(any(OrderSummary.class));
            }
        }
    }

    // ==================== updatePurchaseOrder Tests ====================

    @Nested
    @DisplayName("updatePurchaseOrder Tests")
    class UpdatePurchaseOrderTests {

        @Test
        @DisplayName("Update PO - Success")
        void updatePurchaseOrder_Success() {
            // Arrange
            testPurchaseOrderRequest.setPurchaseOrderId(TEST_PO_ID);
            testPurchaseOrderRequest.setAttachments(null);

            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.of(testPurchaseOrder));
            when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                    any(), any(), any(), any(), any()))
                    .thenReturn(Optional.empty());
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
            when(orderSummaryRepository.findByEntityTypeAndEntityId(
                    eq(OrderSummary.EntityType.PURCHASE_ORDER.getValue()), eq(TEST_PO_ID)))
                    .thenReturn(Optional.of(testOrderSummary));
            when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);
            when(shipmentRepository.findByOrderSummaryId(anyLong())).thenReturn(new ArrayList<>());
            AtomicLong shipmentIdSeq = new AtomicLong(1L);
            when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
                Shipment s = invocation.getArgument(0);
                s.setShipmentId(shipmentIdSeq.getAndIncrement());
                return s;
            });
            when(shipmentProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            AtomicLong shipmentPackageIdSeq = new AtomicLong(1L);
            when(shipmentPackageRepository.save(any(ShipmentPackage.class))).thenAnswer(invocation -> {
                ShipmentPackage p = invocation.getArgument(0);
                p.setShipmentPackageId(shipmentPackageIdSeq.getAndIncrement());
                return p;
            });
            when(shipmentPackageProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
            when(resourcesRepository.findByEntityIdAndEntityType(TEST_PO_ID, EntityType.PURCHASE_ORDER))
                    .thenReturn(new ArrayList<>());

            // Act
            assertDoesNotThrow(() -> purchaseOrderService.updatePurchaseOrder(testPurchaseOrderRequest));

            // Assert
            verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
            verify(addressRepository, atLeastOnce()).save(any(Address.class));
            verify(orderSummaryRepository, times(1)).save(any(OrderSummary.class));
            verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("Update PO - PO Not Found")
        void updatePurchaseOrder_NotFound() {
            // Arrange
            testPurchaseOrderRequest.setPurchaseOrderId(TEST_PO_ID);
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> purchaseOrderService.updatePurchaseOrder(testPurchaseOrderRequest));

            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
            verify(purchaseOrderRepository, never()).save(any());
        }
    }

    // ==================== getPurchaseOrderDetailsById Tests ====================

    @Nested
    @DisplayName("getPurchaseOrderDetailsById Tests")
    class GetPurchaseOrderDetailsByIdTests {

        @Test
        @DisplayName("Get PO Details By ID - Success")
        void getPurchaseOrderDetailsById_Success() {
            // Arrange
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientIdWithAllRelations(TEST_PO_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.of(testPurchaseOrder));
            when(resourcesRepository.findByEntityIdAndEntityType(TEST_PO_ID, EntityType.PURCHASE_ORDER))
                    .thenReturn(new ArrayList<>());
            when(orderSummaryRepository.findByEntityTypeAndEntityId(
                    eq(OrderSummary.EntityType.PURCHASE_ORDER.getValue()), eq(TEST_PO_ID)))
                    .thenReturn(Optional.of(testOrderSummary));
            when(shipmentRepository.findByOrderSummaryId(anyLong())).thenReturn(new ArrayList<>());

            // Act
            PurchaseOrderResponseModel result = purchaseOrderService.getPurchaseOrderDetailsById(TEST_PO_ID);

            // Assert
            assertNotNull(result);
            assertEquals(TEST_PO_ID, result.getPurchaseOrderId());
            verify(purchaseOrderRepository, times(1))
                    .findByPurchaseOrderIdAndClientIdWithAllRelations(TEST_PO_ID, DEFAULT_CLIENT_ID);
        }

        @Test
        @DisplayName("Get PO Details By ID - Not Found")
        void getPurchaseOrderDetailsById_NotFound() {
            // Arrange
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientIdWithAllRelations(TEST_PO_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> purchaseOrderService.getPurchaseOrderDetailsById(TEST_PO_ID));

            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
        }
    }

    // ==================== togglePurchaseOrder Tests ====================

    @Nested
    @DisplayName("togglePurchaseOrder Tests")
    class TogglePurchaseOrderTests {

        @Test
        @DisplayName("Toggle PO - Success - Mark As Deleted")
        void togglePurchaseOrder_Success_MarkAsDeleted() {
            // Arrange
            testPurchaseOrder.setIsDeleted(false);
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.of(testPurchaseOrder));
            when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);

            // Act
            assertDoesNotThrow(() -> purchaseOrderService.togglePurchaseOrder(TEST_PO_ID));

            // Assert
            assertTrue(testPurchaseOrder.getIsDeleted());
            verify(purchaseOrderRepository, times(1)).save(testPurchaseOrder);
            verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("Toggle PO - Success - Restore")
        void togglePurchaseOrder_Success_Restore() {
            // Arrange
            testPurchaseOrder.setIsDeleted(true);
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.of(testPurchaseOrder));
            when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);

            // Act
            assertDoesNotThrow(() -> purchaseOrderService.togglePurchaseOrder(TEST_PO_ID));

            // Assert
            assertFalse(testPurchaseOrder.getIsDeleted());
            verify(purchaseOrderRepository, times(1)).save(testPurchaseOrder);
        }

        @Test
        @DisplayName("Toggle PO - Not Found")
        void togglePurchaseOrder_NotFound() {
            // Arrange
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> purchaseOrderService.togglePurchaseOrder(TEST_PO_ID));

            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
            verify(purchaseOrderRepository, never()).save(any());
        }
    }

    // ==================== approvedByPurchaseOrder Tests ====================

    @Nested
    @DisplayName("approvedByPurchaseOrder Tests")
    class ApprovedByPurchaseOrderTests {

        @Test
        @DisplayName("Approve PO - Success")
        void approvedByPurchaseOrder_Success() {
            // Arrange
            testPurchaseOrder.setApprovedByUserId(null);
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.of(testPurchaseOrder));
            when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);

            // Act
            assertDoesNotThrow(() -> purchaseOrderService.approvedByPurchaseOrder(TEST_PO_ID));

            // Assert
            assertNotNull(testPurchaseOrder.getApprovedByUserId());
            verify(purchaseOrderRepository, times(1)).save(testPurchaseOrder);
            verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("Approve PO - Already Approved")
        void approvedByPurchaseOrder_AlreadyApproved() {
            // Arrange
            testPurchaseOrder.setApprovedByUserId(DEFAULT_USER_ID);
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.of(testPurchaseOrder));

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.approvedByPurchaseOrder(TEST_PO_ID));

            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.AlreadyApproved, exception.getMessage());
            verify(purchaseOrderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Approve PO - Not Found")
        void approvedByPurchaseOrder_NotFound() {
            // Arrange
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> purchaseOrderService.approvedByPurchaseOrder(TEST_PO_ID));

            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
        }
    }

    // ==================== rejectedByPurchaseOrder Tests ====================

    @Nested
    @DisplayName("rejectedByPurchaseOrder Tests")
    class RejectedByPurchaseOrderTests {

        @Test
        @DisplayName("Reject PO - Success")
        void rejectedByPurchaseOrder_Success() {
            // Arrange
            testPurchaseOrder.setRejectedByUserId(null);
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.of(testPurchaseOrder));
            when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);

            // Act
            assertDoesNotThrow(() -> purchaseOrderService.rejectedByPurchaseOrder(TEST_PO_ID));

            // Assert
            assertNotNull(testPurchaseOrder.getRejectedByUserId());
            verify(purchaseOrderRepository, times(1)).save(testPurchaseOrder);
            verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("Reject PO - Already Rejected")
        void rejectedByPurchaseOrder_AlreadyRejected() {
            // Arrange
            testPurchaseOrder.setRejectedByUserId(DEFAULT_USER_ID);
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.of(testPurchaseOrder));

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.rejectedByPurchaseOrder(TEST_PO_ID));

            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.AlreadyRejected, exception.getMessage());
            verify(purchaseOrderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Reject PO - Not Found")
        void rejectedByPurchaseOrder_NotFound() {
            // Arrange
            when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> purchaseOrderService.rejectedByPurchaseOrder(TEST_PO_ID));

            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
        }
    }

    // ==================== Validation Tests ====================

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Create PO - Null Request - Throws BadRequestException")
        void createPurchaseOrder_NullRequest_ThrowsBadRequestException() {
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.createPurchaseOrder(null));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidRequest, exception.getMessage());
        }

        @Test
        @DisplayName("Create PO - Null Order Summary - Throws BadRequestException")
        void createPurchaseOrder_NullOrderSummary_ThrowsBadRequestException() {
            testPurchaseOrderRequest.setOrderSummary(null);
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
            assertEquals(ErrorMessages.OrderSummaryErrorMessages.InvalidRequest, exception.getMessage());
        }

        @Test
        @DisplayName("Create PO - Max Attachments Exceeded - Throws BadRequestException")
        void createPurchaseOrder_MaxAttachmentsExceeded_ThrowsBadRequestException() {
            Map<String, String> attachments = new HashMap<>();
            for (int i = 0; i < 31; i++) {
                attachments.put("file" + i + ".txt", "data");
            }
            testPurchaseOrderRequest.setAttachments(attachments);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.MaxAttachmentsExceeded, exception.getMessage());
        }

        @Test
        @DisplayName("Create PO - Null Products List - Throws BadRequestException")
        void createPurchaseOrder_NullProducts_ThrowsBadRequestException() {
            testPurchaseOrderRequest.setProducts(null);
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.ER004, exception.getMessage());
        }

        @Test
        @DisplayName("Create PO - Empty Products List - Throws BadRequestException")
        void createPurchaseOrder_EmptyProducts_ThrowsBadRequestException() {
            testPurchaseOrderRequest.setProducts(new ArrayList<>());
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.ER004, exception.getMessage());
        }

        @Test
        @DisplayName("Create PO - Invalid Product ID - Throws BadRequestException")
        void createPurchaseOrder_InvalidProductId_ThrowsBadRequestException() {
            testPurchaseOrderRequest.getProducts().get(0).setProductId(0L);
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
            assertEquals(ErrorMessages.ProductErrorMessages.InvalidId, exception.getMessage());
        }

        @Test
        @DisplayName("Create PO - Invalid Quantity - Throws BadRequestException")
        void createPurchaseOrder_InvalidQuantity_ThrowsBadRequestException() {
            testPurchaseOrderRequest.getProducts().get(0).setQuantity(0);
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
            assertTrue(exception.getMessage().contains("Quantity must be greater than 0"));
        }

        @Test
        @DisplayName("Create PO - Null Price Per Unit - Throws BadRequestException")
        void createPurchaseOrder_NullPricePerUnit_ThrowsBadRequestException() {
            testPurchaseOrderRequest.getProducts().get(0).setPricePerUnit(null);
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
            assertTrue(exception.getMessage().contains("pricePerUnit is required"));
        }

        @Test
        @DisplayName("Create PO - Negative Price Per Unit - Throws BadRequestException")
        void createPurchaseOrder_NegativePricePerUnit_ThrowsBadRequestException() {
            testPurchaseOrderRequest.getProducts().get(0).setPricePerUnit(new BigDecimal("-1.00"));
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
            assertTrue(exception.getMessage().contains("pricePerUnit must be greater than or equal to 0"));
        }

        @Test
        @DisplayName("Create PO - Null Order Status - Throws BadRequestException")
        void createPurchaseOrder_NullOrderStatus_ThrowsBadRequestException() {
            testPurchaseOrderRequest.setPurchaseOrderStatus(null);
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidOrderStatus, exception.getMessage());
        }

        @Test
        @DisplayName("Create PO - Empty Order Status - Throws BadRequestException")
        void createPurchaseOrder_EmptyOrderStatus_ThrowsBadRequestException() {
            testPurchaseOrderRequest.setPurchaseOrderStatus("");
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidOrderStatus, exception.getMessage());
        }

        @Test
        @DisplayName("Create PO - Invalid Order Status Value - Throws BadRequestException")
        void createPurchaseOrder_InvalidOrderStatusValue_ThrowsBadRequestException() {
            testPurchaseOrderRequest.setPurchaseOrderStatus("INVALID_STATUS");
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidOrderStatusValue, exception.getMessage());
        }

        @Test
        @DisplayName("Create PO - Null Assigned Lead ID - Throws BadRequestException")
        void createPurchaseOrder_NullAssignedLeadId_ThrowsBadRequestException() {
            testPurchaseOrderRequest.setAssignedLeadId(null);
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidAssignedLeadId, exception.getMessage());
        }

        @Test
        @DisplayName("Create PO - No Shipments - Throws BadRequestException")
        void createPurchaseOrder_NoShipments_ThrowsBadRequestException() {
            lenient()
                    .when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(),
                            any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(Optional.empty());
            lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
            lenient().when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);

            testPurchaseOrderRequest.setShipments(null);
            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
            assertEquals(ErrorMessages.PurchaseOrderErrorMessages.AtLeastOneShipmentRequired, exception.getMessage());
        }

        @Test
        @DisplayName("Create PO - Shipment Missing Courier - Throws BadRequestException")
        void createPurchaseOrder_ShipmentMissingCourier_ThrowsBadRequestException() {
            lenient()
                    .when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(),
                            any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(Optional.empty());
            lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
            lenient().when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);

            testPurchaseOrderRequest.getShipments().get(0).setSelectedCourier(null);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
            assertEquals(ErrorMessages.ShipmentErrorMessages.CourierSelectionRequired, exception.getMessage());
        }

        @Test
        @DisplayName("Create PO - Shipment Missing Packages - Throws BadRequestException")
        void createPurchaseOrder_ShipmentMissingPackages_ThrowsBadRequestException() {
            lenient()
                    .when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(),
                            any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(Optional.empty());
            lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
            lenient().when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);
            lenient().when(shipmentRepository.save(any(Shipment.class))).thenReturn(new Shipment());

            testPurchaseOrderRequest.getShipments().get(0).setPackages(null);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
            assertEquals(ErrorMessages.ShipmentPackageErrorMessages.AtLeastOnePackageRequired, exception.getMessage());
        }

        @Test
        @DisplayName("Create PO - Package Missing Products - Throws BadRequestException")
        void createPurchaseOrder_PackageMissingProducts_ThrowsBadRequestException() {
            lenient()
                    .when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(),
                            any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(Optional.empty());
            lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
            lenient().when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);
            lenient().when(shipmentRepository.save(any(Shipment.class))).thenReturn(new Shipment());
            lenient().when(shipmentPackageRepository.save(any(ShipmentPackage.class)))
                    .thenReturn(new ShipmentPackage());

            testPurchaseOrderRequest.getShipments().get(0).getPackages().get(0).setProducts(null);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));
            assertEquals(ErrorMessages.ShipmentPackageProductErrorMessages.AtLeastOneProductRequired,
                    exception.getMessage());
        }
    }
}
