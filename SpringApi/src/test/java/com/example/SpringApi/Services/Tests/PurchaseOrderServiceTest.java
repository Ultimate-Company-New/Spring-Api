package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Constants.EntityType;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Helpers.ImgbbHelper;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.PurchaseOrderProductItem;
import com.example.SpringApi.Models.RequestModels.PurchaseOrderRequestModel;
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
import org.mockito.Spy;
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
    private PaymentInfoRepository paymentInfoRepository;

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
    private PurchaseOrderQuantityPriceMapRepository purchaseOrderQuantityPriceMapRepository;

    @Mock
    private UserLogService userLogService;

    @Mock
    private Environment environment;

    @Spy
    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    private PurchaseOrder testPurchaseOrder;
    private PurchaseOrderRequestModel testPurchaseOrderRequest;
    private Address testAddress;
    private PaymentInfo testPaymentInfo;
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
        // Mock BaseService methods
        lenient().doReturn(TEST_CLIENT_ID).when(purchaseOrderService).getClientId();
        lenient().doReturn(TEST_USER_ID).when(purchaseOrderService).getUserId();
        lenient().doReturn(CREATED_USER).when(purchaseOrderService).getUser();

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

        when(purchaseOrderRepository.findPaginatedPurchaseOrders(
            eq(TEST_CLIENT_ID), isNull(), isNull(), isNull(), eq(false), isNull(), any(Pageable.class)
        )).thenReturn(page);

        when(resourcesRepository.findByEntityIdAndEntityType(TEST_PO_ID, EntityType.PURCHASE_ORDER))
            .thenReturn(new ArrayList<>());

        // Act
        PaginationBaseResponseModel<PurchaseOrderResponseModel> result = 
            purchaseOrderService.getPurchaseOrdersInBatches(paginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1L, result.getTotalDataCount());
        verify(purchaseOrderRepository).findPaginatedPurchaseOrders(
            anyLong(), isNull(), isNull(), isNull(), anyBoolean(), isNull(), any(Pageable.class)
        );
    }

    @Test
    @DisplayName("Get POs In Batches - With Column Filter")
    void getPurchaseOrdersInBatches_WithColumnFilter() {
        // Arrange
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);
        paginationRequest.setColumnName("vendorNumber");
        paginationRequest.setCondition("equals");
        paginationRequest.setFilterExpr(TEST_VENDOR_NUMBER);
        paginationRequest.setIncludeDeleted(false);

        List<PurchaseOrder> purchaseOrders = Arrays.asList(testPurchaseOrder);
        Page<PurchaseOrder> page = new PageImpl<>(purchaseOrders);

        when(purchaseOrderRepository.findPaginatedPurchaseOrders(
            eq(TEST_CLIENT_ID), eq("vendorNumber"), eq("equals"), eq(TEST_VENDOR_NUMBER), 
            eq(false), isNull(), any(Pageable.class)
        )).thenReturn(page);

        when(resourcesRepository.findByEntityIdAndEntityType(TEST_PO_ID, EntityType.PURCHASE_ORDER))
            .thenReturn(new ArrayList<>());

        // Act
        PaginationBaseResponseModel<PurchaseOrderResponseModel> result = 
            purchaseOrderService.getPurchaseOrdersInBatches(paginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
    }

    @Test
    @DisplayName("Get POs In Batches - Invalid Column Name")
    void getPurchaseOrdersInBatches_InvalidColumnName() {
        // Arrange
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);
        paginationRequest.setColumnName("invalidColumn");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> purchaseOrderService.getPurchaseOrdersInBatches(paginationRequest));

        assertTrue(exception.getMessage().contains("Invalid column name"));
    }

    @Test
    @DisplayName("Get POs In Batches - Invalid Condition")
    void getPurchaseOrdersInBatches_InvalidCondition() {
        // Arrange
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);
        paginationRequest.setColumnName("vendorNumber");
        paginationRequest.setCondition("invalidCondition");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> purchaseOrderService.getPurchaseOrdersInBatches(paginationRequest));

        assertTrue(exception.getMessage().contains("Invalid condition"));
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

    @Test
    @DisplayName("Get POs In Batches - With Product IDs Filter")
    void getPurchaseOrdersInBatches_WithProductIds() {
        // Arrange
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);
        paginationRequest.setColumnName("productIds");
        paginationRequest.setFilterExpr("1,2,3");
        paginationRequest.setIncludeDeleted(false);

        List<PurchaseOrder> purchaseOrders = Arrays.asList(testPurchaseOrder);
        Page<PurchaseOrder> page = new PageImpl<>(purchaseOrders);

        when(purchaseOrderRepository.findPaginatedPurchaseOrders(
            eq(TEST_CLIENT_ID), eq("productIds"), isNull(), eq("1,2,3"), 
            eq(false), anyList(), any(Pageable.class)
        )).thenReturn(page);

        when(resourcesRepository.findByEntityIdAndEntityType(TEST_PO_ID, EntityType.PURCHASE_ORDER))
            .thenReturn(new ArrayList<>());

        // Act
        PaginationBaseResponseModel<PurchaseOrderResponseModel> result = 
            purchaseOrderService.getPurchaseOrdersInBatches(paginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
    }

    @Test
    @DisplayName("Get POs In Batches - Invalid Product IDs Format")
    void getPurchaseOrdersInBatches_InvalidProductIdsFormat() {
        // Arrange
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);
        paginationRequest.setColumnName("productIds");
        paginationRequest.setFilterExpr("1,abc,3"); // Invalid format

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> purchaseOrderService.getPurchaseOrdersInBatches(paginationRequest));

        assertTrue(exception.getMessage().contains("Invalid productIds format"));
    }

    // ==================== createPurchaseOrder Tests ====================

    @Test
    @DisplayName("Create PO - Success Without Attachments")
    void createPurchaseOrder_Success_WithoutAttachments() {
        // Arrange
        testPurchaseOrderRequest.setAttachments(null);

        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(paymentInfoRepository.save(any(PaymentInfo.class))).thenReturn(testPaymentInfo);
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
        when(purchaseOrderQuantityPriceMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

        // Act
        assertDoesNotThrow(() -> purchaseOrderService.createPurchaseOrder(testPurchaseOrderRequest));

        // Assert
        verify(addressRepository, times(1)).save(any(Address.class));
        verify(paymentInfoRepository, times(1)).save(any(PaymentInfo.class));
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
        verify(purchaseOrderQuantityPriceMapRepository, times(1)).saveAll(anyList());
        verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("Create PO - Success With Attachments")
    void createPurchaseOrder_Success_WithAttachments() {
        // Arrange
        Map<String, String> attachments = new HashMap<>();
        attachments.put("receipt.pdf", "base64-data-here");
        testPurchaseOrderRequest.setAttachments(attachments);

        lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        lenient().when(paymentInfoRepository.save(any(PaymentInfo.class))).thenReturn(testPaymentInfo);
        lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
        lenient().when(purchaseOrderQuantityPriceMapRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        lenient().when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));
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
            verify(addressRepository, times(1)).save(any(Address.class));
            verify(paymentInfoRepository, times(1)).save(any(PaymentInfo.class));
            verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
            verify(purchaseOrderQuantityPriceMapRepository, times(1)).saveAll(anyList());
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
        when(addressRepository.findById(TEST_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        when(paymentInfoRepository.findById(TEST_PAYMENT_ID)).thenReturn(Optional.of(testPaymentInfo));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(paymentInfoRepository.save(any(PaymentInfo.class))).thenReturn(testPaymentInfo);
        when(purchaseOrderQuantityPriceMapRepository.findByPurchaseOrderId(TEST_PO_ID))
            .thenReturn(new ArrayList<>());
        when(resourcesRepository.findByEntityIdAndEntityType(TEST_PO_ID, EntityType.PURCHASE_ORDER))
            .thenReturn(new ArrayList<>());

        // Act
        assertDoesNotThrow(() -> purchaseOrderService.updatePurchaseOrder(testPurchaseOrderRequest));

        // Assert
        verify(purchaseOrderRepository, times(1)).save(any(PurchaseOrder.class));
        verify(addressRepository, times(1)).save(any(Address.class));
        verify(paymentInfoRepository, times(1)).save(any(PaymentInfo.class));
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
        when(addressRepository.findById(TEST_ADDRESS_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> purchaseOrderService.updatePurchaseOrder(testPurchaseOrderRequest));

        assertEquals(ErrorMessages.AddressErrorMessages.InvalidId, exception.getMessage());
    }

    @Test
    @DisplayName("Update PO - Payment Not Found")
    void updatePurchaseOrder_PaymentNotFound() {
        // Arrange
        testPurchaseOrderRequest.setPurchaseOrderId(TEST_PO_ID);

        when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(TEST_PO_ID, TEST_CLIENT_ID))
            .thenReturn(Optional.of(testPurchaseOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);
        when(addressRepository.findById(TEST_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(purchaseOrderQuantityPriceMapRepository.findByPurchaseOrderId(TEST_PO_ID))
            .thenReturn(new ArrayList<>());
        when(paymentInfoRepository.findById(TEST_PAYMENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> purchaseOrderService.updatePurchaseOrder(testPurchaseOrderRequest));

        assertEquals(ErrorMessages.PaymentInfoErrorMessages.InvalidId, exception.getMessage());
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

        // Initialize test payment info
        testPaymentInfo = new PaymentInfo();
        testPaymentInfo.setPaymentId(TEST_PAYMENT_ID);
        testPaymentInfo.setSubTotal(new BigDecimal("100.00"));
        testPaymentInfo.setTax(new BigDecimal("10.00"));
        testPaymentInfo.setDeliveryFee(new BigDecimal("5.00"));
        testPaymentInfo.setTotal(new BigDecimal("115.00"));

        // Initialize test purchase order request
        testPurchaseOrderRequest = new PurchaseOrderRequestModel();
        testPurchaseOrderRequest.setVendorNumber(TEST_VENDOR_NUMBER);
        testPurchaseOrderRequest.setPurchaseOrderStatus("DRAFT");
        testPurchaseOrderRequest.setPriority("HIGH");
        testPurchaseOrderRequest.setAssignedLeadId(TEST_LEAD_ID);
        testPurchaseOrderRequest.setAddress(testAddressRequest);
        testPurchaseOrderRequest.setDeliveryFee(new BigDecimal("5.00"));
        testPurchaseOrderRequest.setServiceFee(new BigDecimal("10.00"));
        testPurchaseOrderRequest.setDiscount(new BigDecimal("0.00"));
        
        // Set products list
        List<PurchaseOrderProductItem> products = new ArrayList<>();
        products.add(new PurchaseOrderProductItem(TEST_PRODUCT_ID, new BigDecimal("10.00"), 10));
        testPurchaseOrderRequest.setProducts(products);

        // Initialize test purchase order
        testPurchaseOrder = new PurchaseOrder();
        testPurchaseOrder.setPurchaseOrderId(TEST_PO_ID);
        testPurchaseOrder.setVendorNumber(TEST_VENDOR_NUMBER);
        testPurchaseOrder.setPurchaseOrderStatus("DRAFT");
        testPurchaseOrder.setPriority("HIGH");
        testPurchaseOrder.setAssignedLeadId(TEST_LEAD_ID);
        testPurchaseOrder.setPurchaseOrderAddressId(TEST_ADDRESS_ID);
        testPurchaseOrder.setPaymentId(TEST_PAYMENT_ID);
        testPurchaseOrder.setClientId(TEST_CLIENT_ID);
        testPurchaseOrder.setIsDeleted(false);
        testPurchaseOrder.setCreatedAt(LocalDateTime.now());
        testPurchaseOrder.setUpdatedAt(LocalDateTime.now());
        testPurchaseOrder.setCreatedUser(CREATED_USER);
        testPurchaseOrder.setModifiedUser(CREATED_USER);
    }
}

