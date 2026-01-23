package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.FilterQueryBuilder.ShipmentFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.ShipmentResponseModel;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Services.ShipmentService;
import com.example.SpringApi.Services.UserLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ShipmentService}.
 * 
 * Tests shipment retrieval, pagination, and filtering operations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ShipmentService Tests")
class ShipmentServiceTest {

        @Mock
        private ShipmentRepository shipmentRepository;

        @Mock
        private ShipmentProductRepository shipmentProductRepository;

        @Mock
        private ShipmentPackageRepository shipmentPackageRepository;

        @Mock
        private ShipmentPackageProductRepository shipmentPackageProductRepository;

        @Mock
        private OrderSummaryRepository orderSummaryRepository;

        @Mock
        private PurchaseOrderRepository purchaseOrderRepository;

        @Mock
        private AddressRepository addressRepository;

        @Mock
        private ShipmentFilterQueryBuilder shipmentFilterQueryBuilder;

        @Mock
        private UserLogService userLogService;

        @Spy
        @InjectMocks
        private ShipmentService shipmentService;

        private Shipment testShipment;
        private PaginationBaseRequestModel testPaginationRequest;

        private static final Long TEST_SHIPMENT_ID = 1L;
        private static final Long TEST_CLIENT_ID = 1L;
        private static final Long TEST_ORDER_SUMMARY_ID = 1L;

        @BeforeEach
        void setUp() {
                // Mock BaseService methods
                lenient().doReturn(TEST_CLIENT_ID).when(shipmentService).getClientId();
                lenient().doReturn("testuser").when(shipmentService).getUser();

                // Initialize test shipment
                testShipment = new Shipment();
                testShipment.setShipmentId(TEST_SHIPMENT_ID);
                testShipment.setOrderSummaryId(TEST_ORDER_SUMMARY_ID);
                testShipment.setClientId(TEST_CLIENT_ID);
                testShipment.setTotalWeightKgs(new BigDecimal("5.00"));
                testShipment.setTotalQuantity(10);
                testShipment.setPackagingCost(new BigDecimal("50.00"));
                testShipment.setShippingCost(new BigDecimal("100.00"));
                testShipment.setShipRocketOrderId("12345"); // Set order ID for getShipmentById

                // Initialize test pagination request
                testPaginationRequest = new PaginationBaseRequestModel();
                testPaginationRequest.setStart(0);
                testPaginationRequest.setEnd(10);
                testPaginationRequest.setIncludeDeleted(false);
        }

        @Nested
        @DisplayName("Get Shipments In Batches Tests")
        class GetShipmentsInBatchesTests {

                @Test
                @DisplayName("Get Shipments - Invalid Pagination End Less Than Start - Throws BadRequestException")
                void getShipmentsInBatches_InvalidPagination_ThrowsBadRequestException() {
                        testPaginationRequest.setStart(10);
                        testPaginationRequest.setEnd(5);

                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shipmentService.getShipmentsInBatches(testPaginationRequest));

                        assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, exception.getMessage());
                }

                @Test
                @DisplayName("Get Shipments - End Before Start - Throws BadRequestException")
                void getShipmentsInBatches_EndBeforeStart_ThrowsBadRequestException() {
                        PaginationBaseRequestModel req = new PaginationBaseRequestModel();
                        req.setStart(10);
                        req.setEnd(5);

                        BadRequestException ex = assertThrows(BadRequestException.class,
                                () -> shipmentService.getShipmentsInBatches(req));
                        assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
                }

                @Test
                @DisplayName("Get Shipments - Success - Returns Paginated Results")
                void getShipmentsInBatches_Success_ReturnsPaginatedResults() {
                        List<Shipment> shipments = Arrays.asList(testShipment);
                        Page<Shipment> page = new PageImpl<>(shipments);

                        when(shipmentFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                        anyLong(), any(), any(), any(), any(Pageable.class)))
                                        .thenReturn(page);

                        PaginationBaseResponseModel<ShipmentResponseModel> result = shipmentService
                                        .getShipmentsInBatches(testPaginationRequest);

                        assertNotNull(result);
                        assertEquals(1, result.getData().size());
                        assertEquals(1L, result.getTotalDataCount());
                }

                @Test
                @DisplayName("Get Shipments - Large Page Size (1000) - Success")
                void getShipmentsInBatches_LargePageSize_Success() {
                        PaginationBaseRequestModel req = new PaginationBaseRequestModel();
                        req.setStart(0);
                        req.setEnd(1000);

                        List<Shipment> shipments = Arrays.asList(testShipment);
                        Page<Shipment> page = new PageImpl<>(shipments, org.springframework.data.domain.PageRequest.of(0, 1000), 1);
                        lenient().when(shipmentFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), isNull(), anyString(), isNull(), any(org.springframework.data.domain.Pageable.class)))
                                .thenReturn(page);

                        PaginationBaseResponseModel<ShipmentResponseModel> result = shipmentService.getShipmentsInBatches(req);
                        assertNotNull(result);
                }

                @Test
                @DisplayName("Get Shipments - Empty Results - Returns Empty")
                void getShipmentsInBatches_EmptyResults_ReturnsEmpty() {
                        PaginationBaseRequestModel req = new PaginationBaseRequestModel();
                        req.setStart(0);
                        req.setEnd(10);

                        List<Shipment> emptyList = new ArrayList<>();
                        Page<Shipment> emptyPage = new PageImpl<>(emptyList, org.springframework.data.domain.PageRequest.of(0, 10), 0);
                        lenient().when(shipmentFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), isNull(), anyString(), isNull(), any(org.springframework.data.domain.Pageable.class)))
                                .thenReturn(emptyPage);

                        PaginationBaseResponseModel<ShipmentResponseModel> result = shipmentService.getShipmentsInBatches(req);
                        assertNotNull(result);
                        assertEquals(0, result.getData().size());
                }

                @Test
                @DisplayName("Triple Loop Validation - Comprehensive Filter Test")
                void getShipmentsInBatches_TripleLoopValidation() {
                        List<String> validColumns = Arrays.asList(
                                "shipmentId", "orderSummaryId", "pickupLocationId", "totalWeightKgs", "totalQuantity",
                                "expectedDeliveryDate", "packagingCost", "shippingCost", "totalCost",
                                "selectedCourierCompanyId", "selectedCourierName", "selectedCourierRate", "selectedCourierMinWeight",
                                "shipRocketOrderId", "shipRocketShipmentId", "shipRocketAwbCode", "shipRocketTrackingId", "shipRocketStatus",
                                "createdUser", "modifiedUser", "createdAt", "updatedAt"
                        );

                        List<String> invalidColumns = Arrays.asList("invalidCol", "dropTable", "select");

                        List<String> validOperators = Arrays.asList(
                                "equals", "contains", "startsWith", "endsWith",
                                "greaterThan", "lessThan", "greaterThanOrEqual", "lessThanOrEqual"
                        );

                        List<String> invalidOperators = Arrays.asList("invalidOp", "like");

                        List<String> values = Arrays.asList("test", "123", "2023-01-01");

                        Page<Shipment> emptyPage = new PageImpl<>(Collections.emptyList());
                        lenient().when(shipmentFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), any(), any(), any(), any(Pageable.class)))
                                .thenReturn(emptyPage);
                        lenient().when(shipmentFilterQueryBuilder.getColumnType(anyString())).thenReturn("string");

                        for (String column : joinLists(validColumns, invalidColumns)) {
                                for (String operator : joinLists(validOperators, invalidOperators)) {
                                        for (String value : values) {
                                                PaginationBaseRequestModel req = new PaginationBaseRequestModel();
                                                req.setStart(0);
                                                req.setEnd(10);
                                                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                                                filter.setColumn(column);
                                                filter.setOperator(operator);
                                                filter.setValue(value);
                                                req.setFilters(Collections.singletonList(filter));

                                                boolean isValidColumn = validColumns.contains(column);
                                                boolean isValidOperator = validOperators.contains(operator);

                                                if (isValidColumn && isValidOperator) {
                                                        assertDoesNotThrow(() -> shipmentService.getShipmentsInBatches(req));
                                                } else {
                                                        BadRequestException ex = assertThrows(BadRequestException.class,
                                                                () -> shipmentService.getShipmentsInBatches(req));
                                                        if (!isValidColumn) {
                                                                assertTrue(ex.getMessage().contains("Invalid column name"));
                                                        } else {
                                                                assertTrue(ex.getMessage().contains("Invalid operator"));
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }

        // ==================== Get Shipment By ID Tests ====================

        @Nested
        @DisplayName("Get Shipment By ID Tests")
        class GetShipmentByIdTests {

                @Test
                @DisplayName("Get Shipment By ID - Null ID - Throws BadRequestException")
                void getShipmentById_NullId_ThrowsBadRequestException() {
                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shipmentService.getShipmentById(null));

                        assertEquals(ErrorMessages.ShipmentErrorMessages.InvalidId, exception.getMessage());
                }

                @Test
                @DisplayName("Get Shipment By ID - Zero ID - Throws BadRequestException")
                void getShipmentById_ZeroId_ThrowsBadRequestException() {
                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shipmentService.getShipmentById(0L));

                        assertEquals(ErrorMessages.ShipmentErrorMessages.InvalidId, exception.getMessage());
                }

                @Test
                @DisplayName("Get Shipment By ID - Negative ID - Throws BadRequestException")
                void getShipmentById_NegativeId_ThrowsBadRequestException() {
                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shipmentService.getShipmentById(-1L));

                        assertEquals(ErrorMessages.ShipmentErrorMessages.InvalidId, exception.getMessage());
                }

                @Test
                @DisplayName("Get Shipment By ID - Shipment Not Found - Throws NotFoundException")
                void getShipmentById_ShipmentNotFound_ThrowsNotFoundException() {
                        when(shipmentRepository.findById(TEST_SHIPMENT_ID))
                                        .thenReturn(Optional.empty());

                        NotFoundException exception = assertThrows(NotFoundException.class,
                                        () -> shipmentService.getShipmentById(TEST_SHIPMENT_ID));

                        assertEquals(String.format(ErrorMessages.ShipmentErrorMessages.NotFound, TEST_SHIPMENT_ID),
                                        exception.getMessage());
                }

                @Test
                @DisplayName("Get Shipment By ID - Success - Returns Shipment Details")
                void getShipmentById_Success_ReturnsShipmentDetails() {
                        testShipment.setShipRocketOrderId("12345"); // Ensure it has order ID
                        when(shipmentRepository.findById(TEST_SHIPMENT_ID))
                                        .thenReturn(Optional.of(testShipment));

                        testShipment.setShipmentProducts(new ArrayList<>());
                        testShipment.setShipmentPackages(new ArrayList<>());
                        testShipment.setReturnShipments(new ArrayList<>());

                        ShipmentResponseModel result = shipmentService.getShipmentById(TEST_SHIPMENT_ID);

                        assertNotNull(result);
                        assertEquals(TEST_SHIPMENT_ID, result.getShipmentId());
                }

                @Test
                @DisplayName("Get Shipment By ID - Max Long ID - Not Found")
                void getShipmentById_MaxLongId_ThrowsNotFoundException() {
                        when(shipmentRepository.findById(Long.MAX_VALUE))
                                .thenReturn(Optional.empty());
                        NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> shipmentService.getShipmentById(Long.MAX_VALUE));
                        assertTrue(ex.getMessage().contains("not found"));
                }
        }

        @SafeVarargs
        private final <T> List<T> joinLists(List<T>... lists) {
                List<T> result = new ArrayList<>();
                for (List<T> list : lists) {
                        result.addAll(list);
                }
                return result;
        }
}
