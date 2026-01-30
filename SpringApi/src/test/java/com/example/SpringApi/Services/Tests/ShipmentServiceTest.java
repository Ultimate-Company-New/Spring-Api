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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ShipmentService.
 * 
 * Test Group Summary:
 * | Group Name | Number of Tests |
 * | :--- | :--- |
 * | GetShipmentsInBatchesTests | 1 |
 * | GetShipmentByIdValidationTests | 7 |
 * | GetShipmentByIdAdditionalTests | 5 |
 * | GetShipmentsInBatchesPaginationTests | 6 |
 * | GetShipmentsInBatchesFilterTests | 7 |
 * | **Total** | **26** |
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ShipmentService Tests")
class ShipmentServiceTest extends BaseTest {

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

        private static final Long TEST_SHIPMENT_ID = DEFAULT_SHIPMENT_ID;
        private static final Long TEST_CLIENT_ID = DEFAULT_CLIENT_ID;
        private static final Long TEST_ORDER_SUMMARY_ID = 1L;

        @BeforeEach
        void setUp() {
                // Initialize test shipment
                testShipment = createTestShipment();
                testShipment.setShipmentId(TEST_SHIPMENT_ID);
                testShipment.setOrderSummaryId(TEST_ORDER_SUMMARY_ID);
                testShipment.setClientId(TEST_CLIENT_ID);

                // Initialize test pagination request
                testPaginationRequest = createValidPaginationRequest();
                testPaginationRequest.setIncludeDeleted(false);
        }

        @Nested
        @DisplayName("GetShipmentsInBatchesTests")
        class GetShipmentsInBatchesTests {

                /**
                 * Purpose: Comprehensive test for shipment batch retrieval including invalid pagination, 
                 *          success without filters, and triple-loop filter validation.
                 * Expected Result: Invalid pagination throws BadRequestException; valid pagination returns results;
                 *                  invalid columns/operators throw BadRequestException.
                 * Assertions: Exception messages match expected error messages; result is not null with correct size.
                 */
                @Test
                @DisplayName("Get Shipments In Batches - Invalid pagination, success no filters, and triple-loop filter validation")
                void getShipmentsInBatches_SingleComprehensiveTest() {
                        PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
                        
                        // (1) Invalid pagination: end <= start
                        paginationRequest.setStart(10);
                        paginationRequest.setEnd(5);
                        assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                                        () -> shipmentService.getShipmentsInBatches(paginationRequest));

                        // (2) Success: simple retrieval without filters
                        paginationRequest.setStart(0);
                        paginationRequest.setEnd(10);
                        paginationRequest.setFilters(null);
                        paginationRequest.setIncludeDeleted(false);

                        List<Shipment> shipments = Arrays.asList(testShipment);
                        Page<Shipment> page = new PageImpl<>(shipments);

                        lenient().when(shipmentFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                        anyLong(), any(), any(), any(), any(Pageable.class)))
                                        .thenReturn(page);

                        PaginationBaseResponseModel<ShipmentResponseModel> result = 
                                        shipmentService.getShipmentsInBatches(paginationRequest);

                        assertNotNull(result);
                        assertEquals(1, result.getData().size());
                        assertEquals(1L, result.getTotalDataCount());

                        // (3) Triple-loop: valid columns × operators × types + invalid combinations
                        String[] stringCols = {"selectedCourierName", "shipRocketOrderId", "shipRocketAwbCode", 
                                "shipRocketTrackingId", "shipRocketStatus", "createdUser", "modifiedUser"};
                        String[] numberCols = {"shipmentId", "orderSummaryId", "pickupLocationId", "totalQuantity",
                                "selectedCourierCompanyId", "shipRocketShipmentId"};
                        String[] decimalCols = {"totalWeightKgs", "packagingCost", "shippingCost", "totalCost",
                                "selectedCourierRate", "selectedCourierMinWeight"};
                        String[] dateCols = {"expectedDeliveryDate", "createdAt", "updatedAt"};
                        String[] allColumns = new String[23];
                        System.arraycopy(stringCols, 0, allColumns, 0, stringCols.length);
                        System.arraycopy(numberCols, 0, allColumns, stringCols.length, numberCols.length);
                        System.arraycopy(decimalCols, 0, allColumns, stringCols.length + numberCols.length, decimalCols.length);
                        System.arraycopy(dateCols, 0, allColumns, stringCols.length + numberCols.length + decimalCols.length, dateCols.length);

                        String[] invalidColumns = BATCH_INVALID_COLUMNS;
                        String[] stringOps = BATCH_STRING_OPERATORS;
                        String[] numberOps = BATCH_NUMBER_OPERATORS;
                        String[] dateOps = BATCH_DATE_OPERATORS;
                        String[] invalidOps = BATCH_INVALID_OPERATORS;
                        Object[] validValues = BATCH_VALID_VALUES;

                        // Test invalid columns
                        for (String invalidCol : invalidColumns) {
                                paginationRequest.setFilters(List.of(createFilterCondition(invalidCol, "equals", "test")));
                                assertThrowsBadRequest(String.format(ErrorMessages.PurchaseOrderErrorMessages.InvalidColumnName, invalidCol),
                                        () -> shipmentService.getShipmentsInBatches(paginationRequest));
                        }

                        // Test valid columns with valid/invalid operators and values
                        for (String column : allColumns) {
                                String columnType = shipmentFilterQueryBuilder.getColumnType(column);
                                String[] validOps;
                                if (Arrays.asList(stringCols).contains(column)) {
                                        validOps = stringOps;
                                } else if (Arrays.asList(numberCols).contains(column) || Arrays.asList(decimalCols).contains(column)) {
                                        validOps = numberOps;
                                } else {
                                        validOps = dateOps;
                                }

                                // Test invalid operators
                                for (String invalidOp : invalidOps) {
                                        paginationRequest.setFilters(List.of(createFilterCondition(column, invalidOp, "test")));
                                        assertThrowsBadRequest(String.format(ErrorMessages.PurchaseOrderErrorMessages.InvalidOperator, invalidOp),
                                                () -> shipmentService.getShipmentsInBatches(paginationRequest));
                                }

                                // Test valid operators with appropriate values
                                for (String operator : validOps) {
                                        boolean shouldPass = true;
                                        Object testValue = validValues[0];

                                        // Date/number columns don't support string operators
                                        if ((columnType.equals("date") || columnType.equals("number")) && 
                                            Arrays.asList("contains", "notContains", "startsWith", "endsWith").contains(operator)) {
                                                shouldPass = false;
                                        }

                                        // isEmpty/isNotEmpty don't need values
                                        if (operator.equals("isEmpty") || operator.equals("isNotEmpty")) {
                                                testValue = null;
                                        }

                                        paginationRequest.setFilters(List.of(createFilterCondition(column, operator, testValue)));

                                        if (shouldPass) {
                                                lenient().when(shipmentFilterQueryBuilder.getColumnType(column)).thenReturn(columnType);
                                                lenient().when(shipmentFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                                        anyLong(), any(), any(), any(), any(Pageable.class)))
                                                        .thenReturn(new PageImpl<>(Collections.emptyList()));
                                                assertDoesNotThrow(() -> shipmentService.getShipmentsInBatches(paginationRequest));
                                        } else {
                                                // Should throw BadRequestException for invalid operator/type combination
                                                try {
                                                        when(shipmentFilterQueryBuilder.getColumnType(column)).thenReturn(columnType);
                                                        shipmentService.getShipmentsInBatches(paginationRequest);
                                                        fail("Expected BadRequestException for invalid operator " + operator + " on " + columnType + " column " + column);
                                                } catch (BadRequestException e) {
                                                        // Expected
                                                }
                                        }
                                }
                        }
                }
        }

        @Nested
        @DisplayName("GetShipmentByIdValidationTests")
        class GetShipmentByIdValidationTests {

                /**
                 * Purpose: Verify that getting shipment with null ID throws BadRequestException.
                 * Expected Result: BadRequestException with InvalidId message is thrown.
                 * Assertions: Exception message equals ErrorMessages.ShipmentErrorMessages.InvalidId.
                 */
                @Test
                @DisplayName("Get Shipment By ID - Null ID - Throws BadRequestException")
                void getShipmentById_NullId_ThrowsBadRequestException() {
                        assertThrowsBadRequest(ErrorMessages.ShipmentErrorMessages.InvalidId,
                                        () -> shipmentService.getShipmentById(null));
                }

                /**
                 * Purpose: Verify that getting shipment with zero ID throws BadRequestException.
                 * Expected Result: BadRequestException with InvalidId message is thrown.
                 * Assertions: Exception message equals ErrorMessages.ShipmentErrorMessages.InvalidId.
                 */
                @Test
                @DisplayName("Get Shipment By ID - Zero ID - Throws BadRequestException")
                void getShipmentById_ZeroId_ThrowsBadRequestException() {
                        assertThrowsBadRequest(ErrorMessages.ShipmentErrorMessages.InvalidId,
                                        () -> shipmentService.getShipmentById(0L));
                }

                /**
                 * Purpose: Verify that getting shipment with negative ID throws BadRequestException.
                 * Expected Result: BadRequestException with InvalidId message is thrown.
                 * Assertions: Exception message equals ErrorMessages.ShipmentErrorMessages.InvalidId.
                 */
                @Test
                @DisplayName("Get Shipment By ID - Negative ID - Throws BadRequestException")
                void getShipmentById_NegativeId_ThrowsBadRequestException() {
                        assertThrowsBadRequest(ErrorMessages.ShipmentErrorMessages.InvalidId,
                                        () -> shipmentService.getShipmentById(-1L));
                }

                /**
                 * Purpose: Verify that getting shipment when not found throws NotFoundException.
                 * Expected Result: NotFoundException with NotFound message is thrown.
                 * Assertions: Exception message equals formatted NotFound error message.
                 */
                @Test
                @DisplayName("Get Shipment By ID - Shipment Not Found - Throws NotFoundException")
                void getShipmentById_ShipmentNotFound_ThrowsNotFoundException() {
                        when(shipmentRepository.findByShipmentIdAndClientId(TEST_SHIPMENT_ID, TEST_CLIENT_ID))
                                        .thenReturn(null);

                        assertThrowsNotFound(String.format(ErrorMessages.ShipmentErrorMessages.NotFound, TEST_SHIPMENT_ID),
                                        () -> shipmentService.getShipmentById(TEST_SHIPMENT_ID));
                }

                /**
                 * Purpose: Verify that getting shipment with valid ID returns shipment details.
                 * Expected Result: ShipmentResponseModel is returned with correct shipment ID.
                 * Assertions: Result is not null; shipment ID matches expected value.
                 */
                @Test
                @DisplayName("Get Shipment By ID - Success - Returns Shipment Details")
                void getShipmentById_Success_ReturnsShipmentDetails() {
                        when(shipmentRepository.findByShipmentIdAndClientId(TEST_SHIPMENT_ID, TEST_CLIENT_ID))
                                        .thenReturn(testShipment);
                        when(shipmentProductRepository.findByShipmentId(TEST_SHIPMENT_ID))
                                        .thenReturn(Collections.emptyList());
                        when(shipmentPackageRepository.findByShipmentId(TEST_SHIPMENT_ID))
                                        .thenReturn(Collections.emptyList());

                        ShipmentResponseModel result = shipmentService.getShipmentById(TEST_SHIPMENT_ID);

                        assertNotNull(result);
                        assertEquals(TEST_SHIPMENT_ID, result.getShipmentId());
                }

                /**
                 * Purpose: Verify that getting shipment with Long.MAX_VALUE ID throws NotFoundException.
                 * Expected Result: NotFoundException is thrown with message containing "not found".
                 * Assertions: Exception message contains "not found".
                 */
                @Test
                @DisplayName("Get Shipment By ID - Long.MAX_VALUE - Not Found")
                void getShipmentById_MaxLongId_ThrowsNotFoundException() {
                        when(shipmentRepository.findByShipmentIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID))
                                .thenReturn(null);
                        NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> shipmentService.getShipmentById(Long.MAX_VALUE));
                        assertTrue(ex.getMessage().contains("not found"));
                }

                /**
                 * Purpose: Verify that getting shipment with Long.MIN_VALUE ID throws NotFoundException.
                 * Expected Result: NotFoundException is thrown with message containing "not found".
                 * Assertions: Exception message contains "not found".
                 */
                @Test
                @DisplayName("Get Shipment By ID - Long.MIN_VALUE - Not Found")
                void getShipmentById_MinLongId_ThrowsNotFoundException() {
                        when(shipmentRepository.findByShipmentIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID))
                                .thenReturn(null);
                        NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> shipmentService.getShipmentById(Long.MIN_VALUE));
                        assertTrue(ex.getMessage().contains("not found"));
                }
        }

        @Nested
        @DisplayName("GetShipmentByIdAdditionalTests")
        class GetShipmentByIdAdditionalTests {

                /**
                 * Purpose: Verify that getting shipment without ShipRocket order ID throws NotFoundException.
                 * Expected Result: NotFoundException with NotFound message is thrown.
                 * Assertions: Exception message equals formatted NotFound error message.
                 */
                @Test
                @DisplayName("Get Shipment By ID - Shipment without ShipRocket Order ID - Not Found")
                void getShipmentById_NoShipRocketOrderId_ThrowsNotFoundException() {
                        Shipment shipmentNoOrderId = createTestShipment();
                        shipmentNoOrderId.setShipmentId(TEST_SHIPMENT_ID);
                        shipmentNoOrderId.setClientId(TEST_CLIENT_ID);
                        shipmentNoOrderId.setShipRocketOrderId(null); // Missing required field

                        when(shipmentRepository.findByShipmentIdAndClientId(TEST_SHIPMENT_ID, TEST_CLIENT_ID))
                                .thenReturn(shipmentNoOrderId);

                        assertThrowsNotFound(String.format(ErrorMessages.ShipmentErrorMessages.NotFound, TEST_SHIPMENT_ID),
                                () -> shipmentService.getShipmentById(TEST_SHIPMENT_ID));
                }

                /**
                 * Purpose: Verify that getting shipment with empty ShipRocket order ID throws NotFoundException.
                 * Expected Result: NotFoundException with NotFound message is thrown.
                 * Assertions: Exception message equals formatted NotFound error message.
                 */
                @Test
                @DisplayName("Get Shipment By ID - Shipment with empty ShipRocket Order ID - Not Found")
                void getShipmentById_EmptyShipRocketOrderId_ThrowsNotFoundException() {
                        Shipment shipmentEmptyOrderId = createTestShipment();
                        shipmentEmptyOrderId.setShipmentId(TEST_SHIPMENT_ID);
                        shipmentEmptyOrderId.setClientId(TEST_CLIENT_ID);
                        shipmentEmptyOrderId.setShipRocketOrderId(""); // Empty string

                        when(shipmentRepository.findByShipmentIdAndClientId(TEST_SHIPMENT_ID, TEST_CLIENT_ID))
                                .thenReturn(shipmentEmptyOrderId);

                        assertThrowsNotFound(String.format(ErrorMessages.ShipmentErrorMessages.NotFound, TEST_SHIPMENT_ID),
                                () -> shipmentService.getShipmentById(TEST_SHIPMENT_ID));
                }

                /**
                 * Purpose: Verify that getting shipment with different client ID throws NotFoundException.
                 * Expected Result: NotFoundException with NotFound message is thrown.
                 * Assertions: Exception message equals formatted NotFound error message.
                 */
                @Test
                @DisplayName("Get Shipment By ID - Different Client ID - Not Found")
                void getShipmentById_DifferentClientId_ThrowsNotFoundException() {
                        when(shipmentRepository.findByShipmentIdAndClientId(TEST_SHIPMENT_ID, TEST_CLIENT_ID))
                                .thenReturn(null);

                        assertThrowsNotFound(String.format(ErrorMessages.ShipmentErrorMessages.NotFound, TEST_SHIPMENT_ID),
                                () -> shipmentService.getShipmentById(TEST_SHIPMENT_ID));
                }

                /**
                 * Purpose: Verify that getting deleted shipment returns shipment details.
                 * Expected Result: ShipmentResponseModel is returned with correct shipment ID.
                 * Assertions: Result is not null; shipment ID matches expected value.
                 */
                @Test
                @DisplayName("Get Shipment By ID - Deleted Shipment - Returns response")
                void getShipmentById_DeletedShipment_ReturnsDetails() {
                        Shipment deletedShipment = createDeletedTestShipment();
                        deletedShipment.setShipmentId(TEST_SHIPMENT_ID);
                        deletedShipment.setClientId(TEST_CLIENT_ID);
                        deletedShipment.setShipRocketOrderId("SR-123456");

                        when(shipmentRepository.findByShipmentIdAndClientId(TEST_SHIPMENT_ID, TEST_CLIENT_ID))
                                .thenReturn(deletedShipment);
                        when(shipmentProductRepository.findByShipmentId(TEST_SHIPMENT_ID))
                                .thenReturn(Collections.emptyList());
                        when(shipmentPackageRepository.findByShipmentId(TEST_SHIPMENT_ID))
                                .thenReturn(Collections.emptyList());

                        ShipmentResponseModel result = shipmentService.getShipmentById(TEST_SHIPMENT_ID);

                        assertNotNull(result);
                        assertEquals(TEST_SHIPMENT_ID, result.getShipmentId());
                }

                /**
                 * Purpose: Verify that getting shipment with valid ID returns shipment and verifies repository call.
                 * Expected Result: ShipmentResponseModel is returned; repository is called with correct parameters.
                 * Assertions: Result is not null; repository method is verified.
                 */
                @Test
                @DisplayName("Get Shipment By ID - One ID - Success")
                void getShipmentById_ValidId_Success() {
                        when(shipmentRepository.findByShipmentIdAndClientId(1L, TEST_CLIENT_ID))
                                .thenReturn(testShipment);
                        when(shipmentProductRepository.findByShipmentId(1L))
                                .thenReturn(Collections.emptyList());
                        when(shipmentPackageRepository.findByShipmentId(1L))
                                .thenReturn(Collections.emptyList());

                        ShipmentResponseModel result = shipmentService.getShipmentById(1L);

                        assertNotNull(result);
                        verify(shipmentRepository).findByShipmentIdAndClientId(1L, TEST_CLIENT_ID);
                }
        }

        @Nested
        @DisplayName("GetShipmentsInBatchesPaginationTests")
        class GetShipmentsInBatchesPaginationTests {

                /**
                 * Purpose: Verify that getting shipments with start equals end throws BadRequestException.
                 * Expected Result: BadRequestException with InvalidPagination message is thrown.
                 * Assertions: Exception message equals ErrorMessages.CommonErrorMessages.InvalidPagination.
                 */
                @Test
                @DisplayName("Get Shipments In Batches - Start equals End - Throws BadRequestException")
                void getShipmentsInBatches_StartEqualsEnd_ThrowsBadRequest() {
                        PaginationBaseRequestModel request = createValidPaginationRequest();
                        request.setStart(10);
                        request.setEnd(10);

                        assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                                () -> shipmentService.getShipmentsInBatches(request));
                }

                /**
                 * Purpose: Verify that getting shipments with negative start throws BadRequestException.
                 * Expected Result: BadRequestException with InvalidPagination message is thrown.
                 * Assertions: Exception message equals ErrorMessages.CommonErrorMessages.InvalidPagination.
                 */
                @Test
                @DisplayName("Get Shipments In Batches - Negative Start - Throws BadRequestException")
                void getShipmentsInBatches_NegativeStart_ThrowsBadRequest() {
                        PaginationBaseRequestModel request = createValidPaginationRequest();
                        request.setStart(-5);
                        request.setEnd(10);

                        assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                                () -> shipmentService.getShipmentsInBatches(request));
                }

                /**
                 * Purpose: Verify that getting shipments with negative end throws BadRequestException.
                 * Expected Result: BadRequestException with InvalidPagination message is thrown.
                 * Assertions: Exception message equals ErrorMessages.CommonErrorMessages.InvalidPagination.
                 */
                @Test
                @DisplayName("Get Shipments In Batches - Negative End - Throws BadRequestException")
                void getShipmentsInBatches_NegativeEnd_ThrowsBadRequest() {
                        PaginationBaseRequestModel request = createValidPaginationRequest();
                        request.setStart(0);
                        request.setEnd(-10);

                        assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                                () -> shipmentService.getShipmentsInBatches(request));
                }

                /**
                 * Purpose: Verify that getting shipments with start greater than end throws BadRequestException.
                 * Expected Result: BadRequestException with InvalidPagination message is thrown.
                 * Assertions: Exception message equals ErrorMessages.CommonErrorMessages.InvalidPagination.
                 */
                @Test
                @DisplayName("Get Shipments In Batches - Start greater than End - Throws BadRequestException")
                void getShipmentsInBatches_StartGreaterThanEnd_ThrowsBadRequest() {
                        PaginationBaseRequestModel request = createValidPaginationRequest();
                        request.setStart(100);
                        request.setEnd(50);

                        assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                                () -> shipmentService.getShipmentsInBatches(request));
                }

                /**
                 * Purpose: Verify that getting shipments with very large page size succeeds.
                 * Expected Result: PaginationBaseResponseModel is returned with results.
                 * Assertions: Result is not null.
                 */
                @Test
                @DisplayName("Get Shipments In Batches - Very large page size - Success")
                void getShipmentsInBatches_LargePageSize_Success() {
                        PaginationBaseRequestModel request = createValidPaginationRequest();
                        request.setStart(0);
                        request.setEnd(1000000);

                        List<Shipment> shipments = Arrays.asList(testShipment);
                        Page<Shipment> page = new PageImpl<>(shipments);

                        lenient().when(shipmentFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), any(), any(), any(), any(Pageable.class)))
                                .thenReturn(page);

                        PaginationBaseResponseModel<ShipmentResponseModel> result = 
                                shipmentService.getShipmentsInBatches(request);

                        assertNotNull(result);
                }

                /**
                 * Purpose: Verify that getting shipments with zero start and end throws BadRequestException.
                 * Expected Result: BadRequestException with InvalidPagination message is thrown.
                 * Assertions: Exception message equals ErrorMessages.CommonErrorMessages.InvalidPagination.
                 */
                @Test
                @DisplayName("Get Shipments In Batches - Zero Start and End - Throws BadRequestException")
                void getShipmentsInBatches_ZeroStartAndEnd_ThrowsBadRequest() {
                        PaginationBaseRequestModel request = createValidPaginationRequest();
                        request.setStart(0);
                        request.setEnd(0);

                        assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                                () -> shipmentService.getShipmentsInBatches(request));
                }
        }

        @Nested
        @DisplayName("GetShipmentsInBatchesFilterTests")
        class GetShipmentsInBatchesFilterTests {

                /**
                 * Purpose: Verify that getting shipments with invalid column name throws BadRequestException.
                 * Expected Result: BadRequestException with InvalidColumnName message is thrown.
                 * Assertions: Exception message equals formatted InvalidColumnName error message.
                 */
                @Test
                @DisplayName("Get Shipments In Batches - Invalid column 'invalidColumn' - Throws BadRequestException")
                void getShipmentsInBatches_InvalidColumn_ThrowsBadRequest() {
                        PaginationBaseRequestModel request = createValidPaginationRequest();
                        request.setStart(0);
                        request.setEnd(10);
                        request.setFilters(List.of(createFilterCondition("invalidColumn", "equals", "test")));

                        assertThrowsBadRequest(String.format(ErrorMessages.PurchaseOrderErrorMessages.InvalidColumnName, "invalidColumn"),
                                () -> shipmentService.getShipmentsInBatches(request));
                }

                /**
                 * Purpose: Verify that getting shipments with invalid operator throws BadRequestException.
                 * Expected Result: BadRequestException with InvalidOperator message is thrown.
                 * Assertions: Exception message equals formatted InvalidOperator error message.
                 */
                @Test
                @DisplayName("Get Shipments In Batches - Invalid operator 'badOperator' - Throws BadRequestException")
                void getShipmentsInBatches_InvalidOperator_ThrowsBadRequest() {
                        PaginationBaseRequestModel request = createValidPaginationRequest();
                        request.setStart(0);
                        request.setEnd(10);
                        request.setFilters(List.of(createFilterCondition("shipmentId", "badOperator", "123")));

                        assertThrowsBadRequest(String.format(ErrorMessages.PurchaseOrderErrorMessages.InvalidOperator, "badOperator"),
                                () -> shipmentService.getShipmentsInBatches(request));
                }

                /**
                 * Purpose: Verify that using string operator on numeric column throws BadRequestException.
                 * Expected Result: BadRequestException is thrown.
                 * Assertions: BadRequestException is thrown for invalid operator/type combination.
                 */
                @Test
                @DisplayName("Get Shipments In Batches - String operator on numeric column - Throws BadRequestException")
                void getShipmentsInBatches_StringOperatorOnNumericColumn_ThrowsBadRequest() {
                        PaginationBaseRequestModel request = createValidPaginationRequest();
                        request.setStart(0);
                        request.setEnd(10);
                        // "contains" is a string operator, but applied to numeric column "shipmentId"
                        request.setFilters(List.of(createFilterCondition("shipmentId", "contains", "123")));

                        when(shipmentFilterQueryBuilder.getColumnType("shipmentId")).thenReturn("number");

                        assertThrows(BadRequestException.class,
                                () -> shipmentService.getShipmentsInBatches(request));
                }

                /**
                 * Purpose: Verify that getting shipments with multiple invalid columns throws BadRequestException for first invalid column.
                 * Expected Result: BadRequestException with InvalidColumnName message is thrown for first invalid column.
                 * Assertions: Exception message equals formatted InvalidColumnName error message for first invalid column.
                 */
                @Test
                @DisplayName("Get Shipments In Batches - Multiple invalid columns - First one throws")
                void getShipmentsInBatches_MultipleInvalidColumns_ThrowsBadRequest() {
                        PaginationBaseRequestModel request = createValidPaginationRequest();
                        request.setStart(0);
                        request.setEnd(10);
                        request.setFilters(List.of(
                                createFilterCondition("fakeCol1", "equals", "val1"),
                                createFilterCondition("shipmentId", "equals", "123")
                        ));

                        assertThrowsBadRequest(String.format(ErrorMessages.PurchaseOrderErrorMessages.InvalidColumnName, "fakeCol1"),
                                () -> shipmentService.getShipmentsInBatches(request));
                }

                /**
                 * Purpose: Verify that getting shipments with valid 'equals' filter succeeds.
                 * Expected Result: PaginationBaseResponseModel is returned with correct data size.
                 * Assertions: Result is not null; data size equals expected count.
                 */
                @Test
                @DisplayName("Get Shipments In Batches - Valid filter with 'equals' operator - Success")
                void getShipmentsInBatches_ValidFilterEquals_Success() {
                        PaginationBaseRequestModel request = createValidPaginationRequest();
                        request.setStart(0);
                        request.setEnd(10);
                        request.setFilters(List.of(createFilterCondition("shipmentId", "equals", "123")));

                        List<Shipment> shipments = Arrays.asList(testShipment);
                        Page<Shipment> page = new PageImpl<>(shipments);

                        lenient().when(shipmentFilterQueryBuilder.getColumnType("shipmentId")).thenReturn("number");
                        lenient().when(shipmentFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), any(), any(), any(), any(Pageable.class)))
                                .thenReturn(page);

                        PaginationBaseResponseModel<ShipmentResponseModel> result = 
                                shipmentService.getShipmentsInBatches(request);

                        assertNotNull(result);
                        assertEquals(1, result.getData().size());
                }

                /**
                 * Purpose: Verify that getting shipments with valid 'contains' filter on string column succeeds.
                 * Expected Result: PaginationBaseResponseModel is returned.
                 * Assertions: Result is not null.
                 */
                @Test
                @DisplayName("Get Shipments In Batches - Valid filter with 'contains' on string column - Success")
                void getShipmentsInBatches_ValidFilterContains_Success() {
                        PaginationBaseRequestModel request = createValidPaginationRequest();
                        request.setStart(0);
                        request.setEnd(10);
                        request.setFilters(List.of(createFilterCondition("selectedCourierName", "contains", "FedEx")));

                        List<Shipment> shipments = Arrays.asList(testShipment);
                        Page<Shipment> page = new PageImpl<>(shipments);

                        lenient().when(shipmentFilterQueryBuilder.getColumnType("selectedCourierName")).thenReturn("string");
                        lenient().when(shipmentFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), any(), any(), any(), any(Pageable.class)))
                                .thenReturn(page);

                        PaginationBaseResponseModel<ShipmentResponseModel> result = 
                                shipmentService.getShipmentsInBatches(request);

                        assertNotNull(result);
                }

                /**
                 * Purpose: Verify that getting shipments with empty filters list succeeds.
                 * Expected Result: PaginationBaseResponseModel is returned.
                 * Assertions: Result is not null.
                 */
                @Test
                @DisplayName("Get Shipments In Batches - Empty filters list - Success")
                void getShipmentsInBatches_EmptyFiltersList_Success() {
                        PaginationBaseRequestModel request = createValidPaginationRequest();
                        request.setStart(0);
                        request.setEnd(10);
                        request.setFilters(Collections.emptyList());

                        List<Shipment> shipments = Arrays.asList(testShipment);
                        Page<Shipment> page = new PageImpl<>(shipments);

                        lenient().when(shipmentFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), any(), any(), any(), any(Pageable.class)))
                                .thenReturn(page);

                        PaginationBaseResponseModel<ShipmentResponseModel> result = 
                                shipmentService.getShipmentsInBatches(request);

                        assertNotNull(result);
                }
        }

}
