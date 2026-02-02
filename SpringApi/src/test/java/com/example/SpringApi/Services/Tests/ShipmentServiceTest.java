package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.FilterQueryBuilder.ShipmentFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ShipmentService.
 * 
 * Test Group Summary:
 * | Group Name                              | Number of Tests |
 * | :-------------------------------------- | :-------------- |
 * | GetShipmentByIdTests                    | 13              |
 * | GetShipmentsInBatchesPaginationTests    | 11              |
 * | GetShipmentsInBatchesFilterTests        | 9               |
 * | **Total**                               | **33**          |
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
    private static final Long TEST_CLIENT_ID = 100L;
    private static final Long TEST_ORDER_SUMMARY_ID = 1L;

    @BeforeEach
    void setUp() {
        // Initialize test shipment
        testShipment = new Shipment();
        testShipment.setShipmentId(TEST_SHIPMENT_ID);
        testShipment.setOrderSummaryId(TEST_ORDER_SUMMARY_ID);
        testShipment.setClientId(TEST_CLIENT_ID);
        testShipment.setShipRocketOrderId("SR123456");
        testShipment.setShipRocketStatus("NEW");

        // Initialize test pagination request
        testPaginationRequest = new PaginationBaseRequestModel();
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(10);
        testPaginationRequest.setFilters(new ArrayList<>());
        testPaginationRequest.setIncludeDeleted(false);
    }

    @Nested
    @DisplayName("GetShipmentById Tests")
    class GetShipmentByIdTests {

        /**
         * Purpose: Verify that null ID throws BadRequestException.
         * Expected Result: BadRequestException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.ShipmentErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Get Shipment By ID - Null ID - Throws BadRequestException")
        void getShipmentById_NullId_ThrowsBadRequestException() {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentById(null));
            assertEquals(ErrorMessages.ShipmentErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that zero ID throws BadRequestException.
         * Expected Result: BadRequestException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.ShipmentErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Get Shipment By ID - Zero ID - Throws BadRequestException")
        void getShipmentById_ZeroId_ThrowsBadRequestException() {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentById(0L));
            assertEquals(ErrorMessages.ShipmentErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that negative ID throws BadRequestException.
         * Expected Result: BadRequestException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.ShipmentErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Get Shipment By ID - Negative ID - Throws BadRequestException")
        void getShipmentById_NegativeId_ThrowsBadRequestException() {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentById(-1L));
            assertEquals(ErrorMessages.ShipmentErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that -100 ID throws BadRequestException.
         * Expected Result: BadRequestException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.ShipmentErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Get Shipment By ID - Negative 100 ID - Throws BadRequestException")
        void getShipmentById_Negative100Id_ThrowsBadRequestException() {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentById(-100L));
            assertEquals(ErrorMessages.ShipmentErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that Long.MIN_VALUE ID throws BadRequestException.
         * Expected Result: BadRequestException with InvalidId message.
         * Assertions: assertEquals(ErrorMessages.ShipmentErrorMessages.InvalidId, ex.getMessage());
         */
        @Test
        @DisplayName("Get Shipment By ID - Min Long ID - Throws BadRequestException")
        void getShipmentById_MinLongId_ThrowsBadRequestException() {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentById(Long.MIN_VALUE));
            assertEquals(ErrorMessages.ShipmentErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify that non-existent shipment throws NotFoundException.
         * Expected Result: NotFoundException with NotFound message.
         * Assertions: assertTrue(ex.getMessage().contains("not found"));
         */
        @Test
        @DisplayName("Get Shipment By ID - Shipment Not Found - Throws NotFoundException")
        void getShipmentById_ShipmentNotFound_ThrowsNotFoundException() {
            when(shipmentRepository.findById(TEST_SHIPMENT_ID)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentService.getShipmentById(TEST_SHIPMENT_ID));
            assertTrue(ex.getMessage().contains("not found"));
        }

        /**
         * Purpose: Verify that max long ID throws NotFoundException when not found.
         * Expected Result: NotFoundException with NotFound message.
         * Assertions: assertTrue(ex.getMessage().contains("not found"));
         */
        @Test
        @DisplayName("Get Shipment By ID - Max Long ID - Throws NotFoundException")
        void getShipmentById_MaxLongId_ThrowsNotFoundException() {
            when(shipmentRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> shipmentService.getShipmentById(Long.MAX_VALUE));
            assertTrue(ex.getMessage().contains("not found"));
        }

        /**
         * Purpose: Verify repository findById is called with correct ID.
         * Expected Result: Repository is called with correct parameter.
         * Assertions: verify(shipmentRepository).findById(TEST_SHIPMENT_ID);
         */
        @Test
        @DisplayName("Get Shipment By ID - Verify Repository Called")
        void getShipmentById_VerifyRepositoryCalled() {
            when(shipmentRepository.findById(TEST_SHIPMENT_ID)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> shipmentService.getShipmentById(TEST_SHIPMENT_ID));

            verify(shipmentRepository).findById(TEST_SHIPMENT_ID);
        }

        /**
         * Purpose: Verify that multiple calls work independently.
         * Expected Result: Each call checks the correct ID.
         * Assertions: verify repository called multiple times.
         */
        @Test
        @DisplayName("Get Shipment By ID - Multiple Calls Work Independently")
        void getShipmentById_MultipleCallsWorkIndependently() {
            when(shipmentRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> shipmentService.getShipmentById(1L));
            assertThrows(NotFoundException.class,
                    () -> shipmentService.getShipmentById(2L));

            verify(shipmentRepository).findById(1L);
            verify(shipmentRepository).findById(2L);
        }

        /**
         * Purpose: Verify different IDs are handled independently.
         * Expected Result: Each call checks the correct ID.
         * Assertions: verify repository called with each ID.
         */
        @Test
        @DisplayName("Get Shipment By ID - Different IDs")
        void getShipmentById_DifferentIds() {
            when(shipmentRepository.findById(100L)).thenReturn(Optional.empty());
            when(shipmentRepository.findById(200L)).thenReturn(Optional.empty());
            when(shipmentRepository.findById(300L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> shipmentService.getShipmentById(100L));
            assertThrows(NotFoundException.class, () -> shipmentService.getShipmentById(200L));
            assertThrows(NotFoundException.class, () -> shipmentService.getShipmentById(300L));

            verify(shipmentRepository).findById(100L);
            verify(shipmentRepository).findById(200L);
            verify(shipmentRepository).findById(300L);
        }

        /**
         * Purpose: Verify that ID 1 works correctly.
         * Expected Result: NotFoundException when not found.
         * Assertions: Exception is thrown.
         */
        @Test
        @DisplayName("Get Shipment By ID - ID 1 - Not Found")
        void getShipmentById_Id1_NotFound() {
            when(shipmentRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> shipmentService.getShipmentById(1L));
        }

        /**
         * Purpose: Verify that large ID works correctly.
         * Expected Result: NotFoundException when not found.
         * Assertions: Exception is thrown.
         */
        @Test
        @DisplayName("Get Shipment By ID - Large ID - Not Found")
        void getShipmentById_LargeId_NotFound() {
            when(shipmentRepository.findById(999999999L)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> shipmentService.getShipmentById(999999999L));
        }

        /**
         * Purpose: Verify repository is called exactly once per call.
         * Expected Result: Repository called exactly once.
         * Assertions: verify(shipmentRepository, times(1)).findById(TEST_SHIPMENT_ID);
         */
        @Test
        @DisplayName("Get Shipment By ID - Repository Called Once")
        void getShipmentById_RepositoryCalledOnce() {
            when(shipmentRepository.findById(TEST_SHIPMENT_ID)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> shipmentService.getShipmentById(TEST_SHIPMENT_ID));

            verify(shipmentRepository, times(1)).findById(TEST_SHIPMENT_ID);
        }
    }

    @Nested
    @DisplayName("GetShipmentsInBatches Pagination Tests")
    class GetShipmentsInBatchesPaginationTests {

        /**
         * Purpose: Verify that start equals end throws BadRequestException.
         * Expected Result: BadRequestException with InvalidPagination message.
         * Assertions: assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
         */
        @Test
        @DisplayName("Get Shipments - Start Equals End - Throws BadRequestException")
        void getShipmentsInBatches_StartEqualsEnd_ThrowsBadRequest() {
            testPaginationRequest.setStart(10);
            testPaginationRequest.setEnd(10);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentsInBatches(testPaginationRequest));
            assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
        }

        /**
         * Purpose: Verify that start greater than end throws BadRequestException.
         * Expected Result: BadRequestException with InvalidPagination message.
         * Assertions: assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
         */
        @Test
        @DisplayName("Get Shipments - Start Greater Than End - Throws BadRequestException")
        void getShipmentsInBatches_StartGreaterThanEnd_ThrowsBadRequest() {
            testPaginationRequest.setStart(100);
            testPaginationRequest.setEnd(50);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentsInBatches(testPaginationRequest));
            assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
        }

        /**
         * Purpose: Verify that zero start and end throws BadRequestException.
         * Expected Result: BadRequestException with InvalidPagination message.
         * Assertions: assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
         */
        @Test
        @DisplayName("Get Shipments - Zero Start And End - Throws BadRequestException")
        void getShipmentsInBatches_ZeroStartAndEnd_ThrowsBadRequest() {
            testPaginationRequest.setStart(0);
            testPaginationRequest.setEnd(0);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentsInBatches(testPaginationRequest));
            assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
        }

        /**
         * Purpose: Verify that negative page size throws BadRequestException.
         * Expected Result: BadRequestException with InvalidPagination message.
         * Assertions: assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
         */
        @Test
        @DisplayName("Get Shipments - Negative Page Size - Throws BadRequestException")
        void getShipmentsInBatches_NegativePageSize_ThrowsBadRequest() {
            testPaginationRequest.setStart(10);
            testPaginationRequest.setEnd(5);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentsInBatches(testPaginationRequest));
            assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
        }

        /**
         * Purpose: Verify that start 50, end 25 throws BadRequestException.
         * Expected Result: BadRequestException with InvalidPagination message.
         * Assertions: assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
         */
        @Test
        @DisplayName("Get Shipments - Start 50 End 25 - Throws BadRequestException")
        void getShipmentsInBatches_Start50End25_ThrowsBadRequest() {
            testPaginationRequest.setStart(50);
            testPaginationRequest.setEnd(25);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentsInBatches(testPaginationRequest));
            assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
        }

        /**
         * Purpose: Verify that start 1000, end 500 throws BadRequestException.
         * Expected Result: BadRequestException with InvalidPagination message.
         * Assertions: assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
         */
        @Test
        @DisplayName("Get Shipments - Start 1000 End 500 - Throws BadRequestException")
        void getShipmentsInBatches_Start1000End500_ThrowsBadRequest() {
            testPaginationRequest.setStart(1000);
            testPaginationRequest.setEnd(500);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentsInBatches(testPaginationRequest));
            assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
        }

        /**
         * Purpose: Verify that Integer.MAX_VALUE start, end 0 throws BadRequestException.
         * Expected Result: BadRequestException with InvalidPagination message.
         * Assertions: assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
         */
        @Test
        @DisplayName("Get Shipments - Max Int Start Zero End - Throws BadRequestException")
        void getShipmentsInBatches_MaxIntStartZeroEnd_ThrowsBadRequest() {
            testPaginationRequest.setStart(Integer.MAX_VALUE);
            testPaginationRequest.setEnd(0);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentsInBatches(testPaginationRequest));
            assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
        }

        /**
         * Purpose: Verify that same large start and end throws BadRequestException.
         * Expected Result: BadRequestException with InvalidPagination message.
         * Assertions: assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
         */
        @Test
        @DisplayName("Get Shipments - Same Large Start And End - Throws BadRequestException")
        void getShipmentsInBatches_SameLargeStartAndEnd_ThrowsBadRequest() {
            testPaginationRequest.setStart(999999);
            testPaginationRequest.setEnd(999999);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentsInBatches(testPaginationRequest));
            assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
        }

        /**
         * Purpose: Verify that start 5, end 5 throws BadRequestException.
         * Expected Result: BadRequestException with InvalidPagination message.
         * Assertions: assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
         */
        @Test
        @DisplayName("Get Shipments - Start 5 End 5 - Throws BadRequestException")
        void getShipmentsInBatches_Start5End5_ThrowsBadRequest() {
            testPaginationRequest.setStart(5);
            testPaginationRequest.setEnd(5);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentsInBatches(testPaginationRequest));
            assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
        }

        /**
         * Purpose: Verify that start 100, end 99 throws BadRequestException.
         * Expected Result: BadRequestException with InvalidPagination message.
         * Assertions: assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
         */
        @Test
        @DisplayName("Get Shipments - Start 100 End 99 - Throws BadRequestException")
        void getShipmentsInBatches_Start100End99_ThrowsBadRequest() {
            testPaginationRequest.setStart(100);
            testPaginationRequest.setEnd(99);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentsInBatches(testPaginationRequest));
            assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
        }

        /**
         * Purpose: Verify that start 1, end 0 throws BadRequestException.
         * Expected Result: BadRequestException with InvalidPagination message.
         * Assertions: assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
         */
        @Test
        @DisplayName("Get Shipments - Start 1 End 0 - Throws BadRequestException")
        void getShipmentsInBatches_Start1End0_ThrowsBadRequest() {
            testPaginationRequest.setStart(1);
            testPaginationRequest.setEnd(0);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentsInBatches(testPaginationRequest));
            assertEquals(ErrorMessages.CommonErrorMessages.InvalidPagination, ex.getMessage());
        }
    }

    @Nested
    @DisplayName("GetShipmentsInBatches Filter Tests")
    class GetShipmentsInBatchesFilterTests {

        /**
         * Purpose: Verify that invalid column name throws BadRequestException.
         * Expected Result: BadRequestException with InvalidColumnName message.
         * Assertions: assertTrue(ex.getMessage().contains("invalidColumn"));
         */
        @Test
        @DisplayName("Get Shipments - Invalid Column - Throws BadRequestException")
        void getShipmentsInBatches_InvalidColumn_ThrowsBadRequest() {
            testPaginationRequest.setStart(0);
            testPaginationRequest.setEnd(10);
            PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
            filter.setColumn("invalidColumn");
            filter.setOperator("equals");
            filter.setValue("test");
            testPaginationRequest.setFilters(List.of(filter));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentsInBatches(testPaginationRequest));
            assertTrue(ex.getMessage().contains("invalidColumn"));
        }

        /**
         * Purpose: Verify that xyz column throws BadRequestException.
         * Expected Result: BadRequestException with InvalidColumnName message.
         * Assertions: assertTrue(ex.getMessage().contains("xyz"));
         */
        @Test
        @DisplayName("Get Shipments - xyz Column - Throws BadRequestException")
        void getShipmentsInBatches_XyzColumn_ThrowsBadRequest() {
            testPaginationRequest.setStart(0);
            testPaginationRequest.setEnd(10);
            PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
            filter.setColumn("xyz");
            filter.setOperator("equals");
            filter.setValue("test");
            testPaginationRequest.setFilters(List.of(filter));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentsInBatches(testPaginationRequest));
            assertTrue(ex.getMessage().contains("xyz"));
        }

        /**
         * Purpose: Verify that special char column throws BadRequestException.
         * Expected Result: BadRequestException with InvalidColumnName message.
         * Assertions: assertTrue(ex.getMessage().contains("@#$"));
         */
        @Test
        @DisplayName("Get Shipments - Special Char Column - Throws BadRequestException")
        void getShipmentsInBatches_SpecialCharColumn_ThrowsBadRequest() {
            testPaginationRequest.setStart(0);
            testPaginationRequest.setEnd(10);
            PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
            filter.setColumn("@#$");
            filter.setOperator("equals");
            filter.setValue("test");
            testPaginationRequest.setFilters(List.of(filter));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentsInBatches(testPaginationRequest));
            assertTrue(ex.getMessage().contains("@#$"));
        }

        /**
         * Purpose: Verify that fakeColumn throws BadRequestException.
         * Expected Result: BadRequestException with InvalidColumnName message.
         * Assertions: assertTrue(ex.getMessage().contains("fakeColumn"));
         */
        @Test
        @DisplayName("Get Shipments - fakeColumn - Throws BadRequestException")
        void getShipmentsInBatches_FakeColumn_ThrowsBadRequest() {
            testPaginationRequest.setStart(0);
            testPaginationRequest.setEnd(10);
            PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
            filter.setColumn("fakeColumn");
            filter.setOperator("equals");
            filter.setValue("test");
            testPaginationRequest.setFilters(List.of(filter));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentsInBatches(testPaginationRequest));
            assertTrue(ex.getMessage().contains("fakeColumn"));
        }

        /**
         * Purpose: Verify that nonExistentField throws BadRequestException.
         * Expected Result: BadRequestException with InvalidColumnName message.
         * Assertions: assertTrue(ex.getMessage().contains("nonExistentField"));
         */
        @Test
        @DisplayName("Get Shipments - nonExistentField - Throws BadRequestException")
        void getShipmentsInBatches_NonExistentField_ThrowsBadRequest() {
            testPaginationRequest.setStart(0);
            testPaginationRequest.setEnd(10);
            PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
            filter.setColumn("nonExistentField");
            filter.setOperator("equals");
            filter.setValue("test");
            testPaginationRequest.setFilters(List.of(filter));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentsInBatches(testPaginationRequest));
            assertTrue(ex.getMessage().contains("nonExistentField"));
        }

        /**
         * Purpose: Verify that invalid operator throws BadRequestException.
         * Expected Result: BadRequestException with InvalidOperator message.
         * Assertions: assertTrue(ex.getMessage().contains("badOperator"));
         */
        @Test
        @DisplayName("Get Shipments - Invalid Operator - Throws BadRequestException")
        void getShipmentsInBatches_InvalidOperator_ThrowsBadRequest() {
            testPaginationRequest.setStart(0);
            testPaginationRequest.setEnd(10);
            PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
            filter.setColumn("shipmentId");
            filter.setOperator("badOperator");
            filter.setValue("123");
            testPaginationRequest.setFilters(List.of(filter));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentsInBatches(testPaginationRequest));
            assertTrue(ex.getMessage().contains("badOperator"));
        }

        /**
         * Purpose: Verify that invalid operator xyz throws BadRequestException.
         * Expected Result: BadRequestException with InvalidOperator message.
         * Assertions: assertTrue(ex.getMessage().contains("xyz"));
         */
        @Test
        @DisplayName("Get Shipments - Invalid Operator xyz - Throws BadRequestException")
        void getShipmentsInBatches_InvalidOperatorXyz_ThrowsBadRequest() {
            testPaginationRequest.setStart(0);
            testPaginationRequest.setEnd(10);
            PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
            filter.setColumn("shipmentId");
            filter.setOperator("xyz");
            filter.setValue("123");
            testPaginationRequest.setFilters(List.of(filter));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentsInBatches(testPaginationRequest));
            assertTrue(ex.getMessage().contains("xyz"));
        }

        /**
         * Purpose: Verify that invalid operator special char throws BadRequestException.
         * Expected Result: BadRequestException with InvalidOperator message.
         * Assertions: assertTrue(ex.getMessage().contains("!@#"));
         */
        @Test
        @DisplayName("Get Shipments - Invalid Operator Special Char - Throws BadRequestException")
        void getShipmentsInBatches_InvalidOperatorSpecialChar_ThrowsBadRequest() {
            testPaginationRequest.setStart(0);
            testPaginationRequest.setEnd(10);
            PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
            filter.setColumn("shipmentId");
            filter.setOperator("!@#");
            filter.setValue("123");
            testPaginationRequest.setFilters(List.of(filter));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentsInBatches(testPaginationRequest));
            assertTrue(ex.getMessage().contains("!@#"));
        }

        /**
         * Purpose: Verify that multiple invalid columns first one throws.
         * Expected Result: BadRequestException with first invalid column.
         * Assertions: assertTrue(ex.getMessage().contains("fakeCol1"));
         */
        @Test
        @DisplayName("Get Shipments - Multiple Invalid Columns First One Throws")
        void getShipmentsInBatches_MultipleInvalidColumnsFirstOneThrows() {
            testPaginationRequest.setStart(0);
            testPaginationRequest.setEnd(10);
            PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
            filter1.setColumn("fakeCol1");
            filter1.setOperator("equals");
            filter1.setValue("val1");
            PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
            filter2.setColumn("shipmentId");
            filter2.setOperator("equals");
            filter2.setValue("123");
            testPaginationRequest.setFilters(List.of(filter1, filter2));

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> shipmentService.getShipmentsInBatches(testPaginationRequest));
            assertTrue(ex.getMessage().contains("fakeCol1"));
        }
    }
}
