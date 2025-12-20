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
import com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.PurchaseOrderResponseModel;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.PurchaseOrderService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.ErrorMessages;
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
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PurchaseOrderService.
 *
 * This test class provides comprehensive coverage of PurchaseOrderService methods including:
 * - CRUD operations (create, read, update, toggle)
 * - Batch retrieval with pagination and filtering
 * - Approval/rejection workflow
 * - PDF generation
 * - Attachment handling with ImgBB
 * - Validation and error handling
 *
 * @author SpringApi Team
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseOrderService Unit Tests")
class PurchaseOrderServiceTest {

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
    private static final Long TEST_CLIENT_ID = 1L;
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_ADDRESS_ID = 1L;
    private static final Long TEST_PAYMENT_ID = 1L;
    private static final Long TEST_LEAD_ID = 1L;
    private static final Long TEST_PRODUCT_ID = 1L;
    private static final String CREATED_USER = "admin";
    private static final String TEST_VENDOR_NUMBER = "VEN-001";

    @BeforeEach
    void setUp() {
        // Note: BaseService methods are now handled by the actual service implementation

        // Initialize test data
        initializeTestData();

        // Setup common mocks
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
        lenient().when(environment.getProperty("imageLocation")).thenReturn("imgbb");
        lenient().when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});
    }

    // ==================== getPurchaseOrdersInBatches Tests ====================

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
            anyLong(), any(), any(), any(), any(), anyBoolean(), any(Pageable.class)
        )).thenReturn(page);

        when(resourcesRepository.findByEntityIdAndEntityType(TEST_PO_ID, EntityType.PURCHASE_ORDER))
            .thenReturn(new ArrayList<>());
        
        // Mock OrderSummary and Shipments loading
        when(orderSummaryRepository.findByEntityTypeAndEntityId(
            eq(OrderSummary.EntityType.PURCHASE_ORDER.getValue()), eq(TEST_PO_ID)))
            .thenReturn(Optional.empty());

        // Act
        PaginationBaseResponseModel<PurchaseOrderResponseModel> result = 
            purchaseOrderService.getPurchaseOrdersInBatches(paginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1L, result.getTotalDataCount());
        verify(purchaseOrderFilterQueryBuilder).findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), any(), any(), any(), anyBoolean(), any(Pageable.class)
        );
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
    @DisplayName("Get POs In Batches - With Single Filter")
    void getPurchaseOrdersInBatches_WithSingleFilter() {
        // Arrange
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);

        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn("vendorNumber");
        filter.setOperator("contains");
        filter.setValue(TEST_VENDOR_NUMBER);
        paginationRequest.setFilters(Arrays.asList(filter));
        paginationRequest.setLogicOperator("AND");

        List<PurchaseOrder> purchaseOrders = Arrays.asList(testPurchaseOrder);
        Page<PurchaseOrder> page = new PageImpl<>(purchaseOrders);

        lenient().when(purchaseOrderFilterQueryBuilder.getColumnType("vendorNumber")).thenReturn("string");
        lenient().when(purchaseOrderFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), any(), any(), any(), anyBoolean(), any(Pageable.class)
        )).thenReturn(page);

        when(resourcesRepository.findByEntityIdAndEntityType(TEST_PO_ID, EntityType.PURCHASE_ORDER))
            .thenReturn(new ArrayList<>());
        
        when(orderSummaryRepository.findByEntityTypeAndEntityId(
            eq(OrderSummary.EntityType.PURCHASE_ORDER.getValue()), eq(TEST_PO_ID)))
            .thenReturn(Optional.empty());

        // Act
        PaginationBaseResponseModel<PurchaseOrderResponseModel> result = 
            purchaseOrderService.getPurchaseOrdersInBatches(paginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(purchaseOrderFilterQueryBuilder, times(1)).getColumnType("vendorNumber");
    }

    @Test
    @DisplayName("Get POs In Batches - With Multiple Filters AND")
    void getPurchaseOrdersInBatches_WithMultipleFiltersAND() {
        // Arrange
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);

        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
        filter1.setColumn("vendorNumber");
        filter1.setOperator("contains");
        filter1.setValue(TEST_VENDOR_NUMBER);

        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
        filter2.setColumn("purchaseOrderStatus");
        filter2.setOperator("contains");
        filter2.setValue("Pending");

        paginationRequest.setFilters(Arrays.asList(filter1, filter2));
        paginationRequest.setLogicOperator("AND");

        List<PurchaseOrder> purchaseOrders = Arrays.asList(testPurchaseOrder);
        Page<PurchaseOrder> page = new PageImpl<>(purchaseOrders);

        lenient().when(purchaseOrderFilterQueryBuilder.getColumnType("vendorNumber")).thenReturn("string");
        when(purchaseOrderFilterQueryBuilder.getColumnType("purchaseOrderStatus")).thenReturn("string");
        lenient().when(purchaseOrderFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), any(), any(), any(), anyBoolean(), any(Pageable.class)
        )).thenReturn(page);

        when(resourcesRepository.findByEntityIdAndEntityType(TEST_PO_ID, EntityType.PURCHASE_ORDER))
            .thenReturn(new ArrayList<>());
        
        when(orderSummaryRepository.findByEntityTypeAndEntityId(
            eq(OrderSummary.EntityType.PURCHASE_ORDER.getValue()), eq(TEST_PO_ID)))
            .thenReturn(Optional.empty());

        // Act
        PaginationBaseResponseModel<PurchaseOrderResponseModel> result = 
            purchaseOrderService.getPurchaseOrdersInBatches(paginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(purchaseOrderFilterQueryBuilder, times(1)).getColumnType("vendorNumber");
        verify(purchaseOrderFilterQueryBuilder, times(1)).getColumnType("purchaseOrderStatus");
    }

    @Test
    @DisplayName("Get POs In Batches - With Multiple Filters OR")
    void getPurchaseOrdersInBatches_WithMultipleFiltersOR() {
        // Arrange
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);

        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
        filter1.setColumn("purchaseOrderStatus");
        filter1.setOperator("contains");
        filter1.setValue("Pending");

        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
        filter2.setColumn("purchaseOrderStatus");
        filter2.setOperator("contains");
        filter2.setValue("Approved");

        paginationRequest.setFilters(Arrays.asList(filter1, filter2));
        paginationRequest.setLogicOperator("OR");

        List<PurchaseOrder> purchaseOrders = Arrays.asList(testPurchaseOrder);
        Page<PurchaseOrder> page = new PageImpl<>(purchaseOrders);

        when(purchaseOrderFilterQueryBuilder.getColumnType("purchaseOrderStatus")).thenReturn("string");
        lenient().when(purchaseOrderFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), any(), any(), any(), anyBoolean(), any(Pageable.class)
        )).thenReturn(page);

        when(resourcesRepository.findByEntityIdAndEntityType(TEST_PO_ID, EntityType.PURCHASE_ORDER))
            .thenReturn(new ArrayList<>());
        
        when(orderSummaryRepository.findByEntityTypeAndEntityId(
            eq(OrderSummary.EntityType.PURCHASE_ORDER.getValue()), eq(TEST_PO_ID)))
            .thenReturn(Optional.empty());

        // Act
        PaginationBaseResponseModel<PurchaseOrderResponseModel> result = 
            purchaseOrderService.getPurchaseOrdersInBatches(paginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(purchaseOrderFilterQueryBuilder, times(2)).getColumnType("purchaseOrderStatus");
    }

    @Test
    @DisplayName("Get POs In Batches - With Complex Filters")
    void getPurchaseOrdersInBatches_WithComplexFilters() {
        // Arrange
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);

        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
        filter1.setColumn("vendorNumber");
        filter1.setOperator("contains");
        filter1.setValue(TEST_VENDOR_NUMBER);

        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
        filter2.setColumn("purchaseOrderId");
        filter2.setOperator("greaterThan");
        filter2.setValue("0");

        paginationRequest.setFilters(Arrays.asList(filter1, filter2));
        paginationRequest.setLogicOperator("AND");

        List<PurchaseOrder> purchaseOrders = Arrays.asList(testPurchaseOrder);
        Page<PurchaseOrder> page = new PageImpl<>(purchaseOrders);

        lenient().when(purchaseOrderFilterQueryBuilder.getColumnType("vendorNumber")).thenReturn("string");
        when(purchaseOrderFilterQueryBuilder.getColumnType("purchaseOrderId")).thenReturn("number");
        lenient().when(purchaseOrderFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), any(), any(), any(), anyBoolean(), any(Pageable.class)
        )).thenReturn(page);

        when(resourcesRepository.findByEntityIdAndEntityType(TEST_PO_ID, EntityType.PURCHASE_ORDER))
            .thenReturn(new ArrayList<>());
        
        when(orderSummaryRepository.findByEntityTypeAndEntityId(
            eq(OrderSummary.EntityType.PURCHASE_ORDER.getValue()), eq(TEST_PO_ID)))
            .thenReturn(Optional.empty());

        // Act
        PaginationBaseResponseModel<PurchaseOrderResponseModel> result = 
            purchaseOrderService.getPurchaseOrdersInBatches(paginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(purchaseOrderFilterQueryBuilder, times(1)).getColumnType("vendorNumber");
        verify(purchaseOrderFilterQueryBuilder, times(1)).getColumnType("purchaseOrderId");
    }

    @Test
    @DisplayName("Get POs In Batches - Invalid Operator")
    void getPurchaseOrdersInBatches_InvalidOperator() {
        // Arrange
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);

        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn("vendorNumber");
        filter.setOperator("invalidOperator");
        filter.setValue("test");
        paginationRequest.setFilters(Arrays.asList(filter));
        paginationRequest.setLogicOperator("AND");

        lenient().when(purchaseOrderFilterQueryBuilder.getColumnType("vendorNumber")).thenReturn("string");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> purchaseOrderService.getPurchaseOrdersInBatches(paginationRequest));

        assertEquals("Invalid operator: invalidOperator", exception.getMessage());
    }

    @Test
    @DisplayName("Get POs In Batches - Invalid Pagination")
    void getPurchaseOrdersInBatches_InvalidPagination() {
        // Arrange
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(10);
        paginationRequest.setEnd(5); // end < start

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> purchaseOrderService.getPurchaseOrdersInBatches(paginationRequest));

        assertEquals("Invalid pagination: end must be greater than start", exception.getMessage());
    }

    // ==================== createPurchaseOrder Tests ====================

    @Test
    @DisplayName("Create PO - Success Without Attachments")
    void createPurchaseOrder_Success_WithoutAttachments() {
        // Arrange
        testPurchaseOrderRequest.setAttachments(null);

        when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Optional.empty());
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
        when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shipmentProductRepository.save(any(ShipmentProduct.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shipmentPackageRepository.save(any(ShipmentPackage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shipmentPackageProductRepository.save(any(ShipmentPackageProduct.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        assertDoesNotThrow(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));

        // Assert
        verify(addressRepository, atLeastOnce()).save(any(Address.class));
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
        verify(orderSummaryRepository, times(1)).save(any(OrderSummary.class));
        verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("Create PO - Success With Attachments")
    void createPurchaseOrder_Success_WithAttachments() {
        // Arrange
        Map<String, String> attachments = new HashMap<>();
        attachments.put("receipt.pdf", "base64-data-here");
        testPurchaseOrderRequest.setAttachments(attachments);

        lenient().when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Optional.empty());
        lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        lenient().when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
        lenient().when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);
        lenient().when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(shipmentProductRepository.save(any(ShipmentProduct.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(shipmentPackageRepository.save(any(ShipmentPackage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(shipmentPackageProductRepository.save(any(ShipmentPackageProduct.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(resourcesRepository.save(any(Resources.class))).thenReturn(new Resources());

        try (MockedConstruction<ImgbbHelper> imgbbHelperMock = mockConstruction(ImgbbHelper.class,
                (mock, context) -> {
                    List<ImgbbHelper.AttachmentUploadResult> results = Arrays.asList(
                        new ImgbbHelper.AttachmentUploadResult(
                            "https://i.ibb.co/test/receipt.pdf",
                            "delete-hash-123",
                            null
                        )
                    );
                    when(mock.uploadPurchaseOrderAttachments(anyList(), anyString(), anyString(), anyLong()))
                        .thenReturn(results);
                })) {

            // Act
            assertDoesNotThrow(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));

            // Assert
            verify(addressRepository, atLeastOnce()).save(any(Address.class));
            verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
            verify(orderSummaryRepository, times(1)).save(any(OrderSummary.class));
            // Note: Attachment upload is conditional based on environment configuration
        }
    }

    // ==================== updatePurchaseOrder Tests ====================

    @Test
    @DisplayName("Update PO - Success")
    void updatePurchaseOrder_Success() {
        // Arrange
        testPurchaseOrderRequest.setPurchaseOrderId(TEST_PO_ID);
        testPurchaseOrderRequest.setAttachments(null);

        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.of(testPurchaseOrder));
        when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Optional.empty());
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
        when(orderSummaryRepository.findByEntityTypeAndEntityId(
            eq(OrderSummary.EntityType.PURCHASE_ORDER.getValue()), eq(TEST_PO_ID)))
            .thenReturn(Optional.of(testOrderSummary));
        when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);
        when(shipmentRepository.findByOrderSummaryId(anyLong())).thenReturn(new ArrayList<>());
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shipmentProductRepository.save(any(ShipmentProduct.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shipmentPackageRepository.save(any(ShipmentPackage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(shipmentPackageProductRepository.save(any(ShipmentPackageProduct.class))).thenAnswer(invocation -> invocation.getArgument(0));
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
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> purchaseOrderService.updatePurchaseOrder(testPurchaseOrderRequest));

        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
        verify(purchaseOrderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update PO - Address Not Found")
    void updatePurchaseOrder_AddressNotFound() {
        // Arrange
        testPurchaseOrderRequest.setPurchaseOrderId(TEST_PO_ID);

        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.of(testPurchaseOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
        when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Optional.empty());
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> purchaseOrderService.updatePurchaseOrder(testPurchaseOrderRequest));

        assertTrue(exception.getMessage().contains("Client") || exception.getMessage().contains("not found"));
    }

    // ==================== getPurchaseOrderDetailsById Tests ====================

    @Test
    @DisplayName("Get PO Details By ID - Success")
    void getPurchaseOrderDetailsById_Success() {
        // Arrange
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientIdWithAllRelations(TEST_PO_ID, TEST_CLIENT_ID))
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
            .findByPurchaseOrderIdAndClientIdWithAllRelations(TEST_PO_ID, TEST_CLIENT_ID);
    }

    @Test
    @DisplayName("Get PO Details By ID - Not Found")
    void getPurchaseOrderDetailsById_NotFound() {
        // Arrange
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientIdWithAllRelations(TEST_PO_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> purchaseOrderService.getPurchaseOrderDetailsById(TEST_PO_ID));

        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
    }

    // ==================== togglePurchaseOrder Tests ====================

    @Test
    @DisplayName("Toggle PO - Success - Mark As Deleted")
    void togglePurchaseOrder_Success_MarkAsDeleted() {
        // Arrange
        testPurchaseOrder.setIsDeleted(false);
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
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
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
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
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> purchaseOrderService.togglePurchaseOrder(TEST_PO_ID));

        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
        verify(purchaseOrderRepository, never()).save(any());
    }

    // ==================== approvedByPurchaseOrder Tests ====================

    @Test
    @DisplayName("Approve PO - Success")
    void approvedByPurchaseOrder_Success() {
        // Arrange
        testPurchaseOrder.setApprovedByUserId(null);
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
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
        testPurchaseOrder.setApprovedByUserId(TEST_USER_ID);
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
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
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> purchaseOrderService.approvedByPurchaseOrder(TEST_PO_ID));

        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
    }

    // ==================== rejectedByPurchaseOrder Tests ====================

    @Test
    @DisplayName("Reject PO - Success")
    void rejectedByPurchaseOrder_Success() {
        // Arrange
        testPurchaseOrder.setRejectedByUserId(null);
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
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
        testPurchaseOrder.setRejectedByUserId(TEST_USER_ID);
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
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
        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> purchaseOrderService.rejectedByPurchaseOrder(TEST_PO_ID));

        assertEquals(ErrorMessages.PurchaseOrderErrorMessages.InvalidId, exception.getMessage());
    }

    // ==================== Helper Methods ====================

    private void initializeTestData() {
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
        
        // Set shipments (empty for basic tests)
        testPurchaseOrderRequest.setShipments(new ArrayList<>());

        // Initialize test purchase order
        testPurchaseOrder = new PurchaseOrder();
        testPurchaseOrder.setPurchaseOrderId(TEST_PO_ID);
        testPurchaseOrder.setVendorNumber(TEST_VENDOR_NUMBER);
        testPurchaseOrder.setPurchaseOrderStatus("DRAFT");
        testPurchaseOrder.setAssignedLeadId(TEST_LEAD_ID);
        testPurchaseOrder.setPaymentId(TEST_PAYMENT_ID);
        testPurchaseOrder.setClientId(TEST_CLIENT_ID);
        testPurchaseOrder.setIsDeleted(false);
        testPurchaseOrder.setCreatedAt(LocalDateTime.now());
        testPurchaseOrder.setUpdatedAt(LocalDateTime.now());
        testPurchaseOrder.setCreatedUser(CREATED_USER);
        testPurchaseOrder.setModifiedUser(CREATED_USER);
    }

    // ==================== Bulk Create Purchase Orders Tests ====================

    @Test
    @DisplayName("Bulk Create Purchase Orders - Success - All valid POs")
    void bulkCreatePurchaseOrders_AllValid_Success() {
        // Arrange
        List<PurchaseOrderRequestModel> purchaseOrders = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            PurchaseOrderRequestModel poReq = new PurchaseOrderRequestModel();
            poReq.setVendorNumber("VENDOR" + i);
            poReq.setPurchaseOrderStatus("DRAFT");
            poReq.setAssignedLeadId(TEST_LEAD_ID);
            
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
            poReq.setOrderSummary(orderSummaryData);
            poReq.setShipments(new ArrayList<>());
            
            purchaseOrders.add(poReq);
        }

        lenient().when(leadRepository.findById(TEST_LEAD_ID)).thenReturn(java.util.Optional.of(testLead));
        lenient().when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Optional.empty());
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder po = invocation.getArgument(0);
            po.setPurchaseOrderId((long) (Math.random() * 1000));
            return po;
        });
        when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(any(), any()))
            .thenReturn(Optional.of(testPurchaseOrder));
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act
        BulkInsertResponseModel<Long> result = purchaseOrderService.bulkCreatePurchaseOrders(purchaseOrders);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalRequested());
        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
    }

    @Test
    @DisplayName("Bulk Create Purchase Orders - Partial Success")
    void bulkCreatePurchaseOrders_PartialSuccess() {
        // Arrange
        List<PurchaseOrderRequestModel> purchaseOrders = new ArrayList<>();
        
        // Valid PO
        PurchaseOrderRequestModel validPO = new PurchaseOrderRequestModel();
        validPO.setVendorNumber("VALID_VENDOR");
        validPO.setPurchaseOrderStatus("DRAFT");
        validPO.setAssignedLeadId(TEST_LEAD_ID);
        
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
        validPO.setOrderSummary(orderSummaryData);
        validPO.setShipments(new ArrayList<>());
        purchaseOrders.add(validPO);
        
        // Invalid PO (missing vendor number)
        PurchaseOrderRequestModel invalidPO = new PurchaseOrderRequestModel();
        invalidPO.setVendorNumber(null);
        purchaseOrders.add(invalidPO);

        lenient().when(leadRepository.findById(TEST_LEAD_ID)).thenReturn(java.util.Optional.of(testLead));
        lenient().when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Optional.empty());
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder po = invocation.getArgument(0);
            po.setPurchaseOrderId((long) (Math.random() * 1000));
            return po;
        });
        when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(any(), any()))
            .thenReturn(Optional.of(testPurchaseOrder));
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act
        BulkInsertResponseModel<Long> result = purchaseOrderService.bulkCreatePurchaseOrders(purchaseOrders);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalRequested());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
    }

    @Test
    @DisplayName("Bulk Create Purchase Orders - Database Error")
    void bulkCreatePurchaseOrders_DatabaseError() {
        // Arrange
        List<PurchaseOrderRequestModel> purchaseOrders = new ArrayList<>();
        PurchaseOrderRequestModel poReq = new PurchaseOrderRequestModel();
        poReq.setVendorNumber("TEST_VENDOR");
        poReq.setPurchaseOrderStatus("DRAFT");
        poReq.setAssignedLeadId(TEST_LEAD_ID);
        
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
        poReq.setOrderSummary(orderSummaryData);
        poReq.setShipments(new ArrayList<>());
        purchaseOrders.add(poReq);

        lenient().when(leadRepository.findById(TEST_LEAD_ID)).thenReturn(java.util.Optional.of(testLead));
        lenient().when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Optional.empty());
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenThrow(new RuntimeException("Database error"));

        // Act
        BulkInsertResponseModel<Long> result = purchaseOrderService.bulkCreatePurchaseOrders(purchaseOrders);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalRequested());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
    }

    @Test
    @DisplayName("Bulk Create Purchase Orders - Empty List")
    void bulkCreatePurchaseOrders_EmptyList() {
        // Arrange
        List<PurchaseOrderRequestModel> purchaseOrders = new ArrayList<>();

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> purchaseOrderService.bulkCreatePurchaseOrders(purchaseOrders));
        assertTrue(exception.getMessage().contains("Purchase order list cannot be null or empty"));
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }
}

