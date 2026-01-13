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

        @InjectMocks
        private ShipmentService shipmentService;

        private Shipment testShipment;
        private PaginationBaseRequestModel testPaginationRequest;

        private static final Long TEST_SHIPMENT_ID = 1L;
        private static final Long TEST_CLIENT_ID = 1L;
        private static final Long TEST_ORDER_SUMMARY_ID = 1L;

        @BeforeEach
        void setUp() {
                // Initialize test shipment
                testShipment = new Shipment();
                testShipment.setShipmentId(TEST_SHIPMENT_ID);
                testShipment.setOrderSummaryId(TEST_ORDER_SUMMARY_ID);
                testShipment.setClientId(TEST_CLIENT_ID);
                testShipment.setTotalWeightKgs(new BigDecimal("5.00"));
                testShipment.setTotalQuantity(10);
                testShipment.setPackagingCost(new BigDecimal("50.00"));
                testShipment.setShippingCost(new BigDecimal("100.00"));

                // Initialize test pagination request
                testPaginationRequest = new PaginationBaseRequestModel();
                testPaginationRequest.setStart(0);
                testPaginationRequest.setEnd(10);
                testPaginationRequest.setIncludeDeleted(false);
        }

        // ==================== Get Shipments In Batches Tests ====================

        @Nested
        @DisplayName("Get Shipments In Batches - Validation Tests")
        class GetShipmentsInBatchesValidationTests {

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
                @DisplayName("Get Shipments - Invalid Column Name Filter - Throws BadRequestException")
                void getShipmentsInBatches_InvalidColumnName_ThrowsBadRequestException() {
                        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                        filter.setColumn("invalidColumn");
                        filter.setOperator("equals");
                        filter.setValue("test");
                        testPaginationRequest.setFilters(Arrays.asList(filter));

                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shipmentService.getShipmentsInBatches(testPaginationRequest));

                        assertTrue(exception.getMessage().contains("Invalid column name"));
                }

                @Test
                @DisplayName("Get Shipments - Invalid Operator - Throws BadRequestException")
                void getShipmentsInBatches_InvalidOperator_ThrowsBadRequestException() {
                        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                        filter.setColumn("shipmentId");
                        filter.setOperator("invalidOperator");
                        filter.setValue("test");
                        testPaginationRequest.setFilters(Arrays.asList(filter));

                        when(shipmentFilterQueryBuilder.getColumnType("shipmentId")).thenReturn("number");

                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shipmentService.getShipmentsInBatches(testPaginationRequest));

                        assertTrue(exception.getMessage().contains("Invalid operator"));
                }

                @Test
                @DisplayName("Get Shipments - Boolean Column With Invalid Operator - Throws BadRequestException")
                void getShipmentsInBatches_BooleanColumnInvalidOperator_ThrowsBadRequestException() {
                        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                        filter.setColumn("isCancelled");
                        filter.setOperator("contains");
                        filter.setValue("true");
                        testPaginationRequest.setFilters(Arrays.asList(filter));

                        when(shipmentFilterQueryBuilder.getColumnType("isCancelled")).thenReturn("boolean");

                        BadRequestException exception = assertThrows(BadRequestException.class,
                                        () -> shipmentService.getShipmentsInBatches(testPaginationRequest));

                        assertEquals(ErrorMessages.CommonErrorMessages.BooleanColumnsOnlySupportEquals,
                                        exception.getMessage());
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
        }

        // ==================== Get Shipment By ID Tests ====================

        @Nested
        @DisplayName("Get Shipment By ID - Validation Tests")
        class GetShipmentByIdValidationTests {

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
                        when(shipmentRepository.findByShipmentIdAndClientId(TEST_SHIPMENT_ID, TEST_CLIENT_ID))
                                        .thenReturn(Optional.empty());

                        NotFoundException exception = assertThrows(NotFoundException.class,
                                        () -> shipmentService.getShipmentById(TEST_SHIPMENT_ID));

                        assertEquals(String.format(ErrorMessages.ShipmentErrorMessages.NotFound, TEST_SHIPMENT_ID),
                                        exception.getMessage());
                }

                @Test
                @DisplayName("Get Shipment By ID - Success - Returns Shipment Details")
                void getShipmentById_Success_ReturnsShipmentDetails() {
                        when(shipmentRepository.findByShipmentIdAndClientId(TEST_SHIPMENT_ID, TEST_CLIENT_ID))
                                        .thenReturn(Optional.of(testShipment));
                        when(shipmentProductRepository.findByShipmentId(TEST_SHIPMENT_ID))
                                        .thenReturn(Collections.emptyList());
                        when(shipmentPackageRepository.findByShipmentId(TEST_SHIPMENT_ID))
                                        .thenReturn(Collections.emptyList());

                        ShipmentResponseModel result = shipmentService.getShipmentById(TEST_SHIPMENT_ID);

                        assertNotNull(result);
                        assertEquals(TEST_SHIPMENT_ID, result.getShipmentId());
                }
        }

        // ==================== Additional GetShipmentById Tests ====================

        @Test
        @DisplayName("Get Shipment By ID - Negative ID - Not Found")
        void getShipmentById_NegativeId_ThrowsNotFoundException() {
                when(shipmentRepository.findByShipmentIdAndClientId(-1L, TEST_CLIENT_ID))
                        .thenReturn(Optional.empty());
                NotFoundException ex = assertThrows(NotFoundException.class,
                        () -> shipmentService.getShipmentById(-1L));
                assertTrue(ex.getMessage().contains("not found"));
        }

        @Test
        @DisplayName("Get Shipment By ID - Zero ID - Not Found")
        void getShipmentById_ZeroId_ThrowsNotFoundException() {
                when(shipmentRepository.findByShipmentIdAndClientId(0L, TEST_CLIENT_ID))
                        .thenReturn(Optional.empty());
                NotFoundException ex = assertThrows(NotFoundException.class,
                        () -> shipmentService.getShipmentById(0L));
                assertTrue(ex.getMessage().contains("not found"));
        }

        @Test
        @DisplayName("Get Shipment By ID - Long.MAX_VALUE - Not Found")
        void getShipmentById_MaxLongId_ThrowsNotFoundException() {
                when(shipmentRepository.findByShipmentIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID))
                        .thenReturn(Optional.empty());
                NotFoundException ex = assertThrows(NotFoundException.class,
                        () -> shipmentService.getShipmentById(Long.MAX_VALUE));
                assertTrue(ex.getMessage().contains("not found"));
        }

        @Test
        @DisplayName("Get Shipment By ID - Long.MIN_VALUE - Not Found")
        void getShipmentById_MinLongId_ThrowsNotFoundException() {
                when(shipmentRepository.findByShipmentIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID))
                        .thenReturn(Optional.empty());
                NotFoundException ex = assertThrows(NotFoundException.class,
                        () -> shipmentService.getShipmentById(Long.MIN_VALUE));
                assertTrue(ex.getMessage().contains("not found"));
        }

        // ==================== Additional GetShipmentsInBatches Tests ====================

        @Test
        @DisplayName("Get Shipments In Batches - Negative Start - Throws BadRequestException")
        void getShipmentsInBatches_NegativeStart_ThrowsBadRequestException() {
                PaginationBaseRequestModel req = new PaginationBaseRequestModel();
                req.setStart(-1);
                req.setEnd(10);
                
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> shipmentService.getShipmentsInBatches(req));
                assertEquals(ErrorMessages.ShipmentErrorMessages.InvalidRequest, ex.getMessage());
        }

        @Test
        @DisplayName("Get Shipments In Batches - End Before Start - Throws BadRequestException")
        void getShipmentsInBatches_EndBeforeStart_ThrowsBadRequestException() {
                PaginationBaseRequestModel req = new PaginationBaseRequestModel();
                req.setStart(10);
                req.setEnd(5);
                
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> shipmentService.getShipmentsInBatches(req));
                assertEquals(ErrorMessages.ShipmentErrorMessages.InvalidRequest, ex.getMessage());
        }

        @Test
        @DisplayName("Get Shipments In Batches - Large Page Size (1000)")
        void getShipmentsInBatches_LargePageSize_Success() {
                PaginationBaseRequestModel req = new PaginationBaseRequestModel();
                req.setStart(0);
                req.setEnd(1000);
                
                List<Shipment> shipments = Arrays.asList(testShipment);
                Page<Shipment> page = new PageImpl<>(shipments, org.springframework.data.domain.PageRequest.of(0, 1000), 1);
                lenient().when(shipmentFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                        anyLong(), isNull(), anyString(), isNull(), anyBoolean(), any(org.springframework.data.domain.Pageable.class)))
                        .thenReturn(page);
                
                PaginationBaseResponseModel<Shipment> result = shipmentService.getShipmentsInBatches(req);
                assertNotNull(result);
        }

        @Test
        @DisplayName("Get Shipments In Batches - Empty Results")
        void getShipmentsInBatches_EmptyResults_ReturnsEmpty() {
                PaginationBaseRequestModel req = new PaginationBaseRequestModel();
                req.setStart(0);
                req.setEnd(10);
                
                List<Shipment> emptyList = new ArrayList<>();
                Page<Shipment> emptyPage = new PageImpl<>(emptyList, org.springframework.data.domain.PageRequest.of(0, 10), 0);
                lenient().when(shipmentFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                        anyLong(), isNull(), anyString(), isNull(), anyBoolean(), any(org.springframework.data.domain.Pageable.class)))
                        .thenReturn(emptyPage);
                
                PaginationBaseResponseModel<Shipment> result = shipmentService.getShipmentsInBatches(req);
                assertNotNull(result);
                assertEquals(0, result.getData().size());
        }

        @Test
        @DisplayName("Get Shipments In Batches - With Search Query")
        void getShipmentsInBatches_WithSearchQuery_Success() {
                PaginationBaseRequestModel req = new PaginationBaseRequestModel();
                req.setStart(0);
                req.setEnd(10);
                req.setSearchQuery("shipment");
                
                List<Shipment> shipments = Arrays.asList(testShipment);
                Page<Shipment> page = new PageImpl<>(shipments, org.springframework.data.domain.PageRequest.of(0, 10), 1);
                lenient().when(shipmentFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                        anyLong(), isNull(), eq("shipment"), isNull(), anyBoolean(), any(org.springframework.data.domain.Pageable.class)))
                        .thenReturn(page);
                
                PaginationBaseResponseModel<Shipment> result = shipmentService.getShipmentsInBatches(req);
                assertNotNull(result);
        }
}
