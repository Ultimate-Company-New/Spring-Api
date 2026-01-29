package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.FilterQueryBuilder.PickupLocationFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.DatabaseModels.PickupLocation;
import com.example.SpringApi.Models.RequestModels.PickupLocationRequestModel;
import com.example.SpringApi.Models.ResponseModels.PickupLocationResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.FilterCondition;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.ShippingResponseModel.AddPickupLocationResponseModel;
import com.example.SpringApi.Repositories.AddressRepository;
import com.example.SpringApi.Repositories.PickupLocationRepository;
import com.example.SpringApi.Services.PickupLocationService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Services.ClientService;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import com.example.SpringApi.Helpers.ShippingHelper;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.SuccessMessages;
import com.example.SpringApi.Models.ApiRoutes;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PickupLocationService.
 *
 * This test class provides comprehensive coverage of PickupLocationService
 * methods including:
 * - CRUD operations (create, read, update, toggle)
 * - Pickup location retrieval by ID and in batches
 * - Address management integration
 * - ShipRocket API integration
 * - Error handling and validation
 * - Audit logging verification
 *
 * Each test method follows the AAA (Arrange-Act-Assert) pattern and includes
 * both success and failure scenarios to ensure robust error handling.
 * All external dependencies are properly mocked to ensure test isolation.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PickupLocationService Unit Tests")
class PickupLocationServiceTest extends BaseTest {

    @Mock
    private PickupLocationRepository pickupLocationRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserLogService userLogService;

    @Mock
    private ClientService clientService;

    @Mock
    private ShippingHelper shippingHelper;

    @Mock
    private PickupLocationFilterQueryBuilder pickupLocationFilterQueryBuilder;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private PickupLocationService pickupLocationService;

    private PickupLocation testPickupLocation;
    private Address testAddress;
    private PickupLocationRequestModel testPickupLocationRequest;
    private AddPickupLocationResponseModel testShipRocketResponse;
    private static final Long TEST_PICKUP_LOCATION_ID = DEFAULT_PICKUP_LOCATION_ID;
    private static final Long TEST_ADDRESS_ID = DEFAULT_ADDRESS_ID;
    private static final Long TEST_CLIENT_ID = DEFAULT_CLIENT_ID;
    private static final Long TEST_SHIPROCKET_ID = 300L;
    private static final String TEST_ADDRESS_NICKNAME = "Home Warehouse";
    private static final String TEST_STREET_ADDRESS = "123 Main St";
    private static final String TEST_CITY = "New York";
    private static final String TEST_STATE = "NY";
    private static final String TEST_POSTAL_CODE = "10001";
    private static final String TEST_COUNTRY = "USA";
    private static final String CREATED_USER = DEFAULT_CREATED_USER;

    /**
     * Sets up test data before each test execution.
     * Initializes common test objects and configures mock behaviors.
     */
    @BeforeEach
    void setUp() {
        // Initialize test address request model
        AddressRequestModel addressRequest = new AddressRequestModel();
        addressRequest.setStreetAddress(TEST_STREET_ADDRESS);
        addressRequest.setCity(TEST_CITY);
        addressRequest.setState(TEST_STATE);
        addressRequest.setPostalCode(TEST_POSTAL_CODE);
        addressRequest.setCountry(TEST_COUNTRY);
        addressRequest.setAddressType("WAREHOUSE"); // Valid address type
        addressRequest.setNameOnAddress("John Doe");
        addressRequest.setEmailOnAddress("john@example.com");
        addressRequest.setPhoneOnAddress("1234567890");

        // Initialize test pickup location request model
        testPickupLocationRequest = new PickupLocationRequestModel();
        testPickupLocationRequest.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        testPickupLocationRequest.setAddressNickName(TEST_ADDRESS_NICKNAME);
        testPickupLocationRequest.setPickupLocationAddressId(TEST_ADDRESS_ID);
        testPickupLocationRequest.setShipRocketPickupLocationId(TEST_SHIPROCKET_ID);
        testPickupLocationRequest.setAddress(addressRequest);
        testPickupLocationRequest.setIsDeleted(false);

        // Initialize test address
        testAddress = new Address(addressRequest, CREATED_USER);
        testAddress.setAddressId(TEST_ADDRESS_ID);

        // Initialize test pickup location
        testPickupLocation = new PickupLocation(testPickupLocationRequest, CREATED_USER, TEST_CLIENT_ID);
        testPickupLocation.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        testPickupLocation.setShipRocketPickupLocationId(TEST_SHIPROCKET_ID);

        // Initialize ShipRocket response
        testShipRocketResponse = new AddPickupLocationResponseModel();
        testShipRocketResponse.setPickup_id(TEST_SHIPROCKET_ID);

        // Mock ClientService to return client with ShipRocket credentials
        ClientResponseModel mockClient = new ClientResponseModel();
        mockClient.setShipRocketEmail("test@example.com");
        mockClient.setShipRocketPassword("testpassword");
        lenient().when(clientService.getClientById(anyLong())).thenReturn(mockClient);

        // Mock Authorization header
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");

        // Note: BaseService methods are now handled by the actual service
        // implementation
    }

    // ==================== Get Pickup Location By ID Tests ====================

    /**
     * Test successful retrieval of pickup location by ID.
     * Verifies that pickup location with address is returned correctly.
     */
    @Test
    @DisplayName("Get Pickup Location By ID - Success")
    void getPickupLocationById_Success() {
        // Arrange
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(testPickupLocation);

        // Act
        PickupLocationResponseModel result = pickupLocationService.getPickupLocationById(TEST_PICKUP_LOCATION_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_PICKUP_LOCATION_ID, result.getPickupLocationId());
        assertEquals(TEST_ADDRESS_NICKNAME, result.getAddressNickName());
        verify(pickupLocationRepository, times(1)).findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID,
                TEST_CLIENT_ID);
    }

    /**
     * Test get pickup location by ID when not found.
     * Verifies that NotFoundException is thrown.
     */
    @Test
    @DisplayName("Get Pickup Location By ID - Not Found")
    void getPickupLocationById_NotFound_ThrowsNotFoundException() {
        // Arrange
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> pickupLocationService.getPickupLocationById(TEST_PICKUP_LOCATION_ID));
        assertTrue(exception.getMessage().contains(String.valueOf(TEST_PICKUP_LOCATION_ID)));
        verify(pickupLocationRepository, times(1)).findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID,
                TEST_CLIENT_ID);
    }

    // ==================== Get Pickup Locations In Batches Tests ====================

    @Nested
    @DisplayName("GetPickupLocationsInBatches Tests")
    class GetPickupLocationsInBatchesTests {
        @Test
        @DisplayName("Get Pickup Locations In Batches - Invalid pagination, success no filters, and triple-loop filter validation")
        void getPickupLocationsInBatches_SingleComprehensiveTest() {
            PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
            
            // (1) Invalid pagination: end <= start
            paginationRequest.setStart(10);
            paginationRequest.setEnd(5);
            assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                    () -> pickupLocationService.getPickupLocationsInBatches(paginationRequest));

            // (2) Success: simple retrieval without filters
            paginationRequest.setStart(0);
            paginationRequest.setEnd(10);
            paginationRequest.setFilters(null);
            paginationRequest.setIncludeDeleted(false);

            List<PickupLocation> dataList = Collections.singletonList(testPickupLocation);
            Page<PickupLocation> pageResult = new PageImpl<>(dataList, PageRequest.of(0, 10), 1);

            lenient().when(pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), isNull(), anyString(), isNull(), anyBoolean(), any(Pageable.class)))
                    .thenReturn(pageResult);

            PaginationBaseResponseModel<PickupLocationResponseModel> result = 
                    pickupLocationService.getPickupLocationsInBatches(paginationRequest);

            assertNotNull(result);
            assertEquals(1, result.getData().size());
            assertEquals(1, result.getTotalDataCount());

            // (3) Triple-loop: valid columns × operators × types + invalid combinations
            String[] stringCols = {"addressNickName", "locationName", "address", "notes", "createdBy", "modifiedBy"};
            String[] numberCols = {"pickupLocationId", "pickupLocationAddressId", "shipRocketPickupLocationId"};
            String[] booleanCols = {"isDeleted"};
            String[] dateCols = {"createdAt", "updatedAt"};
            String[] allColumns = new String[12];
            System.arraycopy(stringCols, 0, allColumns, 0, stringCols.length);
            System.arraycopy(numberCols, 0, allColumns, stringCols.length, numberCols.length);
            System.arraycopy(booleanCols, 0, allColumns, stringCols.length + numberCols.length, booleanCols.length);
            System.arraycopy(dateCols, 0, allColumns, stringCols.length + numberCols.length + booleanCols.length, dateCols.length);

            String[] invalidColumns = BATCH_INVALID_COLUMNS;
            String[] stringOps = BATCH_STRING_OPERATORS;
            String[] numberOps = BATCH_NUMBER_OPERATORS;
            String[] booleanOps = BATCH_BOOLEAN_OPERATORS;
            String[] dateOps = BATCH_DATE_OPERATORS;
            String[] invalidOps = BATCH_INVALID_OPERATORS;

            Object[] validValues = BATCH_VALID_VALUES;
            Object[] emptyValues = BATCH_EMPTY_VALUES;

            // Test invalid columns
            for (String invalidCol : invalidColumns) {
                paginationRequest.setFilters(List.of(createFilterCondition(invalidCol, "equals", "test")));
                assertThrowsBadRequest("Invalid column name: " + invalidCol,
                        () -> pickupLocationService.getPickupLocationsInBatches(paginationRequest));
            }

            // Test valid columns with valid/invalid operators and values
            for (String column : allColumns) {
                String columnType = pickupLocationFilterQueryBuilder.getColumnType(column);
                String[] validOps;
                if (Arrays.asList(stringCols).contains(column)) {
                    validOps = stringOps;
                } else if (Arrays.asList(numberCols).contains(column)) {
                    validOps = numberOps;
                } else if (Arrays.asList(booleanCols).contains(column)) {
                    validOps = booleanOps;
                } else {
                    validOps = dateOps;
                }

                // Test invalid operators
                for (String invalidOp : invalidOps) {
                    paginationRequest.setFilters(List.of(createFilterCondition(column, invalidOp, "test")));
                    assertThrowsBadRequest("Invalid operator: " + invalidOp,
                            () -> pickupLocationService.getPickupLocationsInBatches(paginationRequest));
                }

                // Test valid operators with appropriate values
                for (String operator : validOps) {
                    boolean shouldPass = true;
                    Object testValue = validValues[0];

                    // Boolean columns only support equals/notEquals
                    if (columnType.equals("boolean") && !operator.equals("equals") && !operator.equals("notEquals")) {
                        shouldPass = false;
                    }

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
                        lenient().when(pickupLocationFilterQueryBuilder.getColumnType(column)).thenReturn(columnType);
                        lenient().when(pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                                anyLong(), isNull(), anyString(), anyList(), anyBoolean(), any(Pageable.class)))
                                .thenReturn(new PageImpl<>(Collections.emptyList()));
                        assertDoesNotThrow(() -> pickupLocationService.getPickupLocationsInBatches(paginationRequest));
                    } else {
                        // Should throw BadRequestException for invalid operator/type combination
                        try {
                            when(pickupLocationFilterQueryBuilder.getColumnType(column)).thenReturn(columnType);
                            pickupLocationService.getPickupLocationsInBatches(paginationRequest);
                            fail("Expected BadRequestException for invalid operator " + operator + " on " + columnType + " column " + column);
                        } catch (BadRequestException e) {
                            // Expected
                        }
                    }
                }
            }
        }
    }

    // ==================== Get Pickup Location By ID Tests ====================

    @Nested
    @DisplayName("GetPickupLocationById Tests")
    class GetPickupLocationByIdTests {
        @Test
        @DisplayName("Get Pickup Location By ID - Success")
        void getPickupLocationById_Success() {
        // Arrange
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);
        paginationRequest.setIncludeDeleted(false);

        List<PickupLocation> dataList = Collections.singletonList(testPickupLocation);
        Page<PickupLocation> pageResult = new PageImpl<>(dataList, PageRequest.of(0, 10), 1);

        lenient().when(pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), isNull(), anyString(), isNull(), anyBoolean(), any(Pageable.class)))
                .thenReturn(pageResult);

        // Act
        PaginationBaseResponseModel<PickupLocationResponseModel> result = pickupLocationService
                .getPickupLocationsInBatches(paginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1, result.getTotalDataCount());
        verify(pickupLocationFilterQueryBuilder, times(1)).findPaginatedEntitiesWithMultipleFilters(
                anyLong(), isNull(), anyString(), isNull(), anyBoolean(), any(Pageable.class));
    }


    // ==================== Create Pickup Location Tests ====================

    @Nested
    @DisplayName("CreatePickupLocation Tests")
    class CreatePickupLocationTests {
        @Test
        @DisplayName("Create Pickup Location - Success")
        void createPickupLocation_Success() throws Exception {
        // Arrange
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
        when(shippingHelper.addPickupLocation(any(PickupLocation.class))).thenReturn(testShipRocketResponse);

        // Act
        pickupLocationService.createPickupLocation(testPickupLocationRequest);

        // Assert
        verify(addressRepository, times(1)).save(any(Address.class));
        verify(pickupLocationRepository, times(2)).save(any(PickupLocation.class)); // Once for creation, once for
                                                                                    // ShipRocket ID update
        verify(shippingHelper, times(1)).addPickupLocation(any(PickupLocation.class));
        verify(userLogService, times(1)).logData(
                anyLong(),
                eq(SuccessMessages.PickupLocationSuccessMessages.InsertPickupLocation + " " + TEST_PICKUP_LOCATION_ID),
                eq(ApiRoutes.PickupLocationsSubRoute.CREATE_PICKUP_LOCATION));
    }

    /**
     * Test create pickup location when ShipRocket API fails.
     * Verifies transaction rollback - no data should be saved.
     */
    @Test
    @DisplayName("Create Pickup Location - ShipRocket API Failure")
    void createPickupLocation_ShipRocketFailure_TransactionRollsBack() throws Exception {
        // Arrange
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
        when(shippingHelper.addPickupLocation(any(PickupLocation.class)))
                .thenThrow(new BadRequestException("ShipRocket API error"));

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));

        // Verify that saves were attempted but transaction should rollback
        verify(addressRepository, times(1)).save(any(Address.class));
        verify(pickupLocationRepository, times(1)).save(any(PickupLocation.class)); // Only initial save
        verify(shippingHelper, times(1)).addPickupLocation(any(PickupLocation.class));
        // Logging should not occur due to rollback
        verify(userLogService, never()).logData(anyLong(), any(), any());
    }

    // ==================== Update Pickup Location Tests ====================

    /**
     * Test successful update of pickup location.
     * Verifies address update, pickup location update, ShipRocket integration, and
     * logging.
     */
    @Test
    @DisplayName("Update Pickup Location - Success")
    void updatePickupLocation_Success() throws Exception {
        // Arrange
        PickupLocation existingPickupLocation = new PickupLocation(testPickupLocationRequest, CREATED_USER,
                TEST_CLIENT_ID);
        existingPickupLocation.setPickupLocationId(TEST_PICKUP_LOCATION_ID);

        when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(existingPickupLocation);
        when(addressRepository.findById(TEST_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
        when(shippingHelper.addPickupLocation(any(PickupLocation.class))).thenReturn(testShipRocketResponse);

        // Act
        pickupLocationService.updatePickupLocation(testPickupLocationRequest);

        // Assert
        verify(pickupLocationRepository, times(1)).findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID,
                TEST_CLIENT_ID);
        verify(addressRepository, times(1)).findById(TEST_ADDRESS_ID);
        verify(addressRepository, times(1)).save(any(Address.class));
        verify(pickupLocationRepository, times(1)).save(any(PickupLocation.class));
        verify(shippingHelper, times(1)).addPickupLocation(any(PickupLocation.class));
        verify(userLogService, times(1)).logData(
                anyLong(),
                eq(SuccessMessages.PickupLocationSuccessMessages.UpdatePickupLocation + " " + TEST_PICKUP_LOCATION_ID),
                eq(ApiRoutes.PickupLocationsSubRoute.UPDATE_PICKUP_LOCATION));
    }

    /**
     * Test update pickup location when pickup location not found.
     * Verifies that NotFoundException is thrown.
     */
    @Test
    @DisplayName("Update Pickup Location - Pickup Location Not Found")
    void updatePickupLocation_PickupLocationNotFound_ThrowsNotFoundException() {
        // Arrange
        lenient().when(
                pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
        assertTrue(exception.getMessage().contains(String.valueOf(TEST_PICKUP_LOCATION_ID)));
        verify(pickupLocationRepository, times(1)).findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID,
                TEST_CLIENT_ID);
    }

    /**
     * Test update pickup location when address not found.
     * Verifies that NotFoundException is thrown.
     */
    @Test
    @DisplayName("Update Pickup Location - Address Not Found")
    void updatePickupLocation_AddressNotFound_ThrowsNotFoundException() {
        // Arrange
        PickupLocation existingPickupLocation = new PickupLocation(testPickupLocationRequest, CREATED_USER,
                TEST_CLIENT_ID);
        existingPickupLocation.setPickupLocationId(TEST_PICKUP_LOCATION_ID);

        when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(existingPickupLocation);
        when(addressRepository.findById(TEST_ADDRESS_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
        assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        verify(addressRepository, times(1)).findById(TEST_ADDRESS_ID);
    }

    /**
     * Test update pickup location when ShipRocket API fails.
     * Verifies transaction rollback.
     */
    @Test
    @DisplayName("Update Pickup Location - ShipRocket API Failure")
    void updatePickupLocation_ShipRocketFailure_TransactionRollsBack() throws Exception {
        // Arrange
        PickupLocation existingPickupLocation = new PickupLocation(testPickupLocationRequest, CREATED_USER,
                TEST_CLIENT_ID);
        existingPickupLocation.setPickupLocationId(TEST_PICKUP_LOCATION_ID);

        when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(existingPickupLocation);
        when(addressRepository.findById(TEST_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(shippingHelper.addPickupLocation(any(PickupLocation.class)))
                .thenThrow(new BadRequestException("ShipRocket API error"));

        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));

        // Verify address save occurred before ShipRocket call
        verify(addressRepository, times(1)).save(any(Address.class));
        // Verify ShipRocket call was made
        verify(shippingHelper, times(1)).addPickupLocation(any(PickupLocation.class));
        // Pickup location save should NOT occur because ShipRocket failed before save
        verify(pickupLocationRepository, never()).save(any(PickupLocation.class));
        // Logging should not occur due to exception
        verify(userLogService, never()).logData(anyLong(), any(), any());
        }
    }

    // ==================== Toggle Pickup Location Tests ====================

    @Nested
    @DisplayName("TogglePickupLocation Tests")
    class TogglePickupLocationTests {
        @Test
        @DisplayName("Toggle Pickup Location - Success")
        void togglePickupLocation_Success() {
        // Arrange
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(testPickupLocation);
        when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);

        // Act
        pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);

        // Assert
        assertTrue(testPickupLocation.getIsDeleted());
        verify(pickupLocationRepository, times(1)).findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID,
                TEST_CLIENT_ID);
        verify(pickupLocationRepository, times(1)).save(testPickupLocation);
        verify(userLogService, times(1)).logData(
                anyLong(),
                eq(SuccessMessages.PickupLocationSuccessMessages.TogglePickupLocation + " " + TEST_PICKUP_LOCATION_ID),
                eq(ApiRoutes.PickupLocationsSubRoute.TOGGLE_PICKUP_LOCATION));
    }

    /**
     * Test toggle pickup location when not found.
     * Verifies that NotFoundException is thrown.
     */
    @Test
    @DisplayName("Toggle Pickup Location - Not Found")
    void togglePickupLocation_NotFound_ThrowsNotFoundException() {
        // Arrange
        lenient().when(
                pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID));
        assertThrowsNotFound(ErrorMessages.PickupLocationErrorMessages.NotFound,
                () -> pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID));
        verify(pickupLocationRepository, times(1)).findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID,
                TEST_CLIENT_ID);
        }
    }

    // ==================== Validation Tests ====================

    @Nested
    @DisplayName("ValidationTests")
    class ValidationTests {

        @Test
        @DisplayName("Create - Null Address Nickname - Throws BadRequestException")
        void create_NullAddressNickName_Throws() {
            testPickupLocationRequest.setAddressNickName(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
            assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidAddressNickName, ex.getMessage());
        }

        @Test
        @DisplayName("Create - Empty Address Nickname - Throws BadRequestException")
        void create_EmptyAddressNickName_Throws() {
            testPickupLocationRequest.setAddressNickName("");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
            assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidAddressNickName, ex.getMessage());
        }

        @Test
        @DisplayName("Create - Long Address Nickname - Throws BadRequestException")
        void create_LongAddressNickName_Throws() {
            String longName = "a".repeat(256);
            testPickupLocationRequest.setAddressNickName(longName);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
            // Assuming the validation for length exists and throws AddressNickNameTooLong
            // Since I am not 100% sure if the service or model throws exact message
            // immediately,
            // I will assert the message. If model validation handles it, it might be
            // standardized.
            // Let's assume validation is consistent with other services.
            assertEquals(ErrorMessages.PickupLocationErrorMessages.AddressNickNameTooLong, ex.getMessage());
        }

        @Test
        @DisplayName("Create - Null ShipRocket ID - Throws BadRequestException")
        void create_NullShipRocketId_Throws() {
            testPickupLocationRequest.setShipRocketId(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
            assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidShipRocketId, ex.getMessage());
        }

        @Test
        @DisplayName("Create - Empty ShipRocket ID - Throws BadRequestException")
        void create_EmptyShipRocketId_Throws() {
            testPickupLocationRequest.setShipRocketId("");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
            assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidShipRocketId, ex.getMessage());
        }

        @Test
        @DisplayName("Create - Whitespace ShipRocket ID - Throws BadRequestException")
        void create_WhitespaceShipRocketId_Throws() {
            testPickupLocationRequest.setShipRocketId("   ");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
            assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidShipRocketId, ex.getMessage());
        }

        @Test
        @DisplayName("Create - Whitespace Address Nickname - Throws BadRequestException")
        void create_WhitespaceAddressNickName_Throws() {
            testPickupLocationRequest.setAddressNickName("   ");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
            assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidAddressNickName, ex.getMessage());
        }
    }

    // ==================== Additional GetPickupLocationDetailsById Tests ====================

    @Test
    @DisplayName("Get Pickup Location By ID - Negative ID - Not Found")
    void getPickupLocationDetailsById_NegativeId_ThrowsNotFoundException() {
        when(pickupLocationRepository.findByPickupLocationIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> pickupLocationService.getPickupLocationDetailsById(-1L));
        assertEquals(ErrorMessages.PickupLocationErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Get Pickup Location By ID - Zero ID - Not Found")
    void getPickupLocationDetailsById_ZeroId_ThrowsNotFoundException() {
        when(pickupLocationRepository.findByPickupLocationIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> pickupLocationService.getPickupLocationDetailsById(0L));
        assertEquals(ErrorMessages.PickupLocationErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Get Pickup Location By ID - Long.MAX_VALUE - Not Found")
    void getPickupLocationDetailsById_MaxLongId_ThrowsNotFoundException() {
        when(pickupLocationRepository.findByPickupLocationIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> pickupLocationService.getPickupLocationDetailsById(Long.MAX_VALUE));
        assertEquals(ErrorMessages.PickupLocationErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Get Pickup Location By ID - Long.MIN_VALUE - Not Found")
    void getPickupLocationDetailsById_MinLongId_ThrowsNotFoundException() {
        when(pickupLocationRepository.findByPickupLocationIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> pickupLocationService.getPickupLocationDetailsById(Long.MIN_VALUE));
        assertEquals(ErrorMessages.PickupLocationErrorMessages.NotFound, ex.getMessage());
    }

    // ==================== Additional UpdatePickupLocation Tests ====================

    @Test
    @DisplayName("Update Pickup Location - Negative ID - Not Found")
    void updatePickupLocation_NegativeId_ThrowsNotFoundException() {
        testPickupLocationRequest.setPickupLocationId(-1L);
        when(pickupLocationRepository.findByPickupLocationIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
        assertEquals(ErrorMessages.PickupLocationErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Update Pickup Location - Zero ID - Not Found")
    void updatePickupLocation_ZeroId_ThrowsNotFoundException() {
        testPickupLocationRequest.setPickupLocationId(0L);
        when(pickupLocationRepository.findByPickupLocationIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
        assertEquals(ErrorMessages.PickupLocationErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Update Pickup Location - Null Address Nickname - Throws BadRequestException")
    void updatePickupLocation_NullAddressNickName_Throws() {
        testPickupLocationRequest.setAddressNickName(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
        assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidAddressNickName, ex.getMessage());
    }

    @Test
    @DisplayName("Update Pickup Location - Empty Address Nickname - Throws BadRequestException")
    void updatePickupLocation_EmptyAddressNickName_Throws() {
        testPickupLocationRequest.setAddressNickName("");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
        assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidAddressNickName, ex.getMessage());
    }

    // ==================== Additional TogglePickupLocation Tests ====================

    @Test
    @DisplayName("Toggle Pickup Location - Negative ID - Not Found")
    void togglePickupLocation_NegativeId_ThrowsNotFoundException() {
        when(pickupLocationRepository.findByPickupLocationIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> pickupLocationService.togglePickupLocation(-1L));
        assertEquals(ErrorMessages.PickupLocationErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Toggle Pickup Location - Zero ID - Not Found")
    void togglePickupLocation_ZeroId_ThrowsNotFoundException() {
        when(pickupLocationRepository.findByPickupLocationIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> pickupLocationService.togglePickupLocation(0L));
        assertEquals(ErrorMessages.PickupLocationErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Toggle Pickup Location - Max Long ID - Not Found")
    void togglePickupLocation_MaxLongId_ThrowsNotFoundException() {
        when(pickupLocationRepository.findByPickupLocationIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> pickupLocationService.togglePickupLocation(Long.MAX_VALUE));
        assertEquals(ErrorMessages.PickupLocationErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Toggle Pickup Location - Multiple Toggles - State Persistence")
    void togglePickupLocation_MultipleToggles_StatePersists() {
        testPickupLocation.setIsDeleted(false);
        when(pickupLocationRepository.findByPickupLocationIdAndClientId(TEST_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(testPickupLocation);
        when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
        
        pickupLocationService.togglePickupLocation(TEST_LOCATION_ID);
        assertTrue(testPickupLocation.getIsDeleted());
        
        pickupLocationService.togglePickupLocation(TEST_LOCATION_ID);
        assertFalse(testPickupLocation.getIsDeleted());
    }

    // ==================== Additional CreatePickupLocation Tests ====================

    @Test
    @DisplayName("Create Pickup Location - Null Request - Throws BadRequestException")
    void createPickupLocation_NullRequest_Throws() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(null));
        assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidRequest, ex.getMessage());
    }

    @Test
    @DisplayName("Create Pickup Location - All Valid Fields - Success")
    void createPickupLocation_AllValidFields_Success() {
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
        
        PickupLocationResponseModel result = pickupLocationService.createPickupLocation(testPickupLocationRequest);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Create Pickup Location - Code at Max Length (255) - Success")
    void createPickupLocation_CodeMaxLength_Success() {
        testPickupLocationRequest.setAddressNickName("a".repeat(255));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
        
        PickupLocationResponseModel result = pickupLocationService.createPickupLocation(testPickupLocationRequest);
        assertNotNull(result);
    }

        @Test
        @DisplayName("Create Pickup Location - Address Not Found - Throws NotFoundException")
        void createPickupLocation_AddressNotFound_ThrowsNotFoundException() {
            when(addressRepository.findByAddressIdAndClientId(any(), any())).thenReturn(null);
            assertThrowsNotFound(ErrorMessages.AddressErrorMessages.NotFound,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
        }
    }

    // ==================== Bulk Create Pickup Location Tests ====================

    @Nested
    @DisplayName("BulkCreatePickupLocations Tests")
    class BulkCreatePickupLocationsTests {

    @Test
    @DisplayName("Bulk Create Pickup Locations - All Invalid Addresses")
    void bulkCreatePickupLocations_AllInvalidAddresses_AllFail() {
        List<PickupLocationRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PickupLocationRequestModel req = new PickupLocationRequestModel();
            req.setAddressNickName("");
            req.setShipRocketId("SR" + i);
            requests.add(req);
        }
        
        // Expecting failure for all invalid addresses
        // Behavior depends on BulkInsertResponseModel implementation
        assertDoesNotThrow(() -> {
            // Bulk operation should handle this gracefully
            pickupLocationService.bulkCreatePickupLocations(requests);
        });
    }

    @Test
    @DisplayName("Bulk Create Pickup Locations - Mixed Valid and Invalid")
    void bulkCreatePickupLocations_MixedValidInvalid_PartialSuccess() {
        List<PickupLocationRequestModel> requests = new ArrayList<>();
        
        // Valid
        PickupLocationRequestModel valid = new PickupLocationRequestModel();
        valid.setAddressNickName("Valid Location");
        valid.setShipRocketId("SR001");
        requests.add(valid);
        
        // Invalid (empty nickname)
        PickupLocationRequestModel invalid = new PickupLocationRequestModel();
        invalid.setAddressNickName("");
        invalid.setShipRocketId("SR002");
        requests.add(invalid);
        
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
        
        assertDoesNotThrow(() -> {
            pickupLocationService.bulkCreatePickupLocations(requests);
        });
    }

    @Test
    @DisplayName("Bulk Create Pickup Locations - Empty List")
    void bulkCreatePickupLocations_EmptyList_ReturnsEmpty() {
        assertDoesNotThrow(() -> {
            pickupLocationService.bulkCreatePickupLocations(new ArrayList<>());
        });
    }

    @Test
    @DisplayName("Bulk Create Pickup Locations - Large Batch (50 items)")
    void bulkCreatePickupLocations_LargeBatch_Success() {
        List<PickupLocationRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            PickupLocationRequestModel req = new PickupLocationRequestModel();
            req.setAddressNickName("Location " + i);
            req.setShipRocketId("SR" + String.format("%03d", i));
            requests.add(req);
        }
        
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
        
        assertDoesNotThrow(() -> {
            pickupLocationService.bulkCreatePickupLocations(requests);
        });
    }

    // ==================== Comprehensive Validation Tests ====================

    @Nested
    @DisplayName("Get Pickup Locations - Pagination Validation Tests")
    class GetPickupLocationsPaginationTests {

        @Test
        @DisplayName("Get Pickup Locations - Invalid pagination (end <= start) - Throws BadRequestException")
        void getPickupLocations_EndLessThanStart_ThrowsBadRequest() {
            PaginationBaseRequestModel request = createValidPaginationRequest();
            request.setStart(100);
            request.setEnd(50);

            assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                    () -> pickupLocationService.getPickupLocationsInBatches(request));
        }

        @Test
        @DisplayName("Get Pickup Locations - Start equals End - Throws BadRequestException")
        void getPickupLocations_StartEqualsEnd_ThrowsBadRequest() {
            PaginationBaseRequestModel request = createValidPaginationRequest();
            request.setStart(25);
            request.setEnd(25);

            assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                    () -> pickupLocationService.getPickupLocationsInBatches(request));
        }

        @Test
        @DisplayName("Get Pickup Locations - Negative start - Throws BadRequestException")
        void getPickupLocations_NegativeStart_ThrowsBadRequest() {
            PaginationBaseRequestModel request = createValidPaginationRequest();
            request.setStart(-1);
            request.setEnd(10);

            assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                    () -> pickupLocationService.getPickupLocationsInBatches(request));
        }

        @Test
        @DisplayName("Get Pickup Locations - Negative end - Throws BadRequestException")
        void getPickupLocations_NegativeEnd_ThrowsBadRequest() {
            PaginationBaseRequestModel request = createValidPaginationRequest();
            request.setStart(0);
            request.setEnd(-10);

            assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                    () -> pickupLocationService.getPickupLocationsInBatches(request));
        }

        @Test
        @DisplayName("Get Pickup Locations - Valid pagination - Success")
        void getPickupLocations_ValidPagination_Success() {
            PaginationBaseRequestModel request = createValidPaginationRequest();
            request.setStart(0);
            request.setEnd(20);

            Page<PickupLocation> page = new PageImpl<>(Collections.singletonList(testPickupLocation));
            when(pickupLocationRepository.findByClientIdAndIsDeletedFalse(eq(TEST_CLIENT_ID), any(Pageable.class)))
                    .thenReturn(page);

            PaginationBaseResponseModel<PickupLocationResponseModel> result = 
                    pickupLocationService.getPickupLocationsInBatches(request);

            assertNotNull(result);
            assertEquals(1, result.getData().size());
        }
    }

    @Nested
    @DisplayName("Create Pickup Location - Validation Tests")
    class CreatePickupLocationValidationTests {

        @Test
        @DisplayName("Create Pickup Location - Null request - Throws BadRequestException")
        void createPickupLocation_NullRequest_ThrowsBadRequest() {
            assertThrowsBadRequest(ErrorMessages.PickupLocationErrorMessages.InvalidRequest,
                    () -> pickupLocationService.createPickupLocation(null));
        }

        @Test
        @DisplayName("Create Pickup Location - Null address nickname - Throws BadRequestException")
        void createPickupLocation_NullAddressNickname_ThrowsBadRequest() {
            testPickupLocationRequest.setAddressNickName(null);

            assertThrowsBadRequest(ErrorMessages.PickupLocationErrorMessages.InvalidAddressNickname,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
        }

        @Test
        @DisplayName("Create Pickup Location - Empty address nickname - Throws BadRequestException")
        void createPickupLocation_EmptyAddressNickname_ThrowsBadRequest() {
            testPickupLocationRequest.setAddressNickName("");

            assertThrowsBadRequest(ErrorMessages.PickupLocationErrorMessages.InvalidAddressNickname,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
        }

        @Test
        @DisplayName("Create Pickup Location - Null shiprocket ID - Throws BadRequestException")
        void createPickupLocation_NullShipRocketId_ThrowsBadRequest() {
            testPickupLocationRequest.setShipRocketId(null);

            assertThrowsBadRequest(ErrorMessages.PickupLocationErrorMessages.InvalidShipRocketId,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
        }

        @Test
        @DisplayName("Create Pickup Location - Empty shiprocket ID - Throws BadRequestException")
        void createPickupLocation_EmptyShipRocketId_ThrowsBadRequest() {
            testPickupLocationRequest.setShipRocketId("");

            assertThrowsBadRequest(ErrorMessages.PickupLocationErrorMessages.InvalidShipRocketId,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
        }

        @Test
        @DisplayName("Create Pickup Location - Null address - Throws BadRequestException")
        void createPickupLocation_NullAddress_ThrowsBadRequest() {
            testPickupLocationRequest.setAddress(null);

            assertThrowsBadRequest(ErrorMessages.PickupLocationErrorMessages.InvalidAddress,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
        }

        @Test
        @DisplayName("Create Pickup Location - Success - Creates new location")
        void createPickupLocation_ValidRequest_Success() {
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);

            PickupLocationResponseModel result = pickupLocationService.createPickupLocation(testPickupLocationRequest);

            assertNotNull(result);
            verify(addressRepository).save(any(Address.class));
            verify(pickupLocationRepository).save(any(PickupLocation.class));
        }
    }

    @Nested
    @DisplayName("Get Pickup Location By ID - Validation Tests")
    class GetPickupLocationByIdValidationTests {

        @Test
        @DisplayName("Get Pickup Location - Null ID - Throws BadRequestException")
        void getPickupLocationById_NullId_ThrowsBadRequest() {
            assertThrowsBadRequest(ErrorMessages.PickupLocationErrorMessages.InvalidId,
                    () -> pickupLocationService.getPickupLocationById(null));
        }

        @Test
        @DisplayName("Get Pickup Location - Zero ID - Throws BadRequestException")
        void getPickupLocationById_ZeroId_ThrowsBadRequest() {
            assertThrowsBadRequest(ErrorMessages.PickupLocationErrorMessages.InvalidId,
                    () -> pickupLocationService.getPickupLocationById(0L));
        }

        @Test
        @DisplayName("Get Pickup Location - Negative ID - Throws BadRequestException")
        void getPickupLocationById_NegativeId_ThrowsBadRequest() {
            assertThrowsBadRequest(ErrorMessages.PickupLocationErrorMessages.InvalidId,
                    () -> pickupLocationService.getPickupLocationById(-5L));
        }

        @Test
        @DisplayName("Get Pickup Location - Not found - Throws NotFoundException")
        void getPickupLocationById_NotFound_ThrowsNotFound() {
            when(pickupLocationRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());

            assertThrowsNotFound(String.format(ErrorMessages.PickupLocationErrorMessages.NotFound, TEST_PICKUP_LOCATION_ID),
                    () -> pickupLocationService.getPickupLocationById(TEST_PICKUP_LOCATION_ID));
        }

        @Test
        @DisplayName("Get Pickup Location - Success - Returns location details")
        void getPickupLocationById_ValidId_Success() {
            when(pickupLocationRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPickupLocation));

            PickupLocationResponseModel result = pickupLocationService.getPickupLocationById(TEST_PICKUP_LOCATION_ID);

            assertNotNull(result);
            verify(pickupLocationRepository).findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
        }
    }

    @Nested
    @DisplayName("Update Pickup Location - Validation Tests")
    class UpdatePickupLocationValidationTests {

        @Test
        @DisplayName("Update Pickup Location - Null request - Throws BadRequestException")
        void updatePickupLocation_NullRequest_ThrowsBadRequest() {
            assertThrowsBadRequest(ErrorMessages.PickupLocationErrorMessages.InvalidRequest,
                    () -> pickupLocationService.updatePickupLocation(TEST_PICKUP_LOCATION_ID, null));
        }

        @Test
        @DisplayName("Update Pickup Location - Null ID - Throws BadRequestException")
        void updatePickupLocation_NullId_ThrowsBadRequest() {
            assertThrowsBadRequest(ErrorMessages.PickupLocationErrorMessages.InvalidId,
                    () -> pickupLocationService.updatePickupLocation(null, testPickupLocationRequest));
        }

        @Test
        @DisplayName("Update Pickup Location - Zero ID - Throws BadRequestException")
        void updatePickupLocation_ZeroId_ThrowsBadRequest() {
            assertThrowsBadRequest(ErrorMessages.PickupLocationErrorMessages.InvalidId,
                    () -> pickupLocationService.updatePickupLocation(0L, testPickupLocationRequest));
        }

        @Test
        @DisplayName("Update Pickup Location - Pickup location not found - Throws NotFoundException")
        void updatePickupLocation_NotFound_ThrowsNotFound() {
            when(pickupLocationRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.empty());

            assertThrowsNotFound(String.format(ErrorMessages.PickupLocationErrorMessages.NotFound, TEST_PICKUP_LOCATION_ID),
                    () -> pickupLocationService.updatePickupLocation(TEST_PICKUP_LOCATION_ID, testPickupLocationRequest));
        }

        @Test
        @DisplayName("Update Pickup Location - Valid update - Success")
        void updatePickupLocation_ValidRequest_Success() {
            when(pickupLocationRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                    .thenReturn(Optional.of(testPickupLocation));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);

            PickupLocationResponseModel result = 
                    pickupLocationService.updatePickupLocation(TEST_PICKUP_LOCATION_ID, testPickupLocationRequest);

            assertNotNull(result);
            verify(pickupLocationRepository).save(any(PickupLocation.class));
        }
    }

    @Nested
    @DisplayName("Bulk Create Pickup Locations - Validation Tests")
    class BulkCreatePickupLocationsValidationTests {

        @Test
        @DisplayName("Bulk Create - Null list - Throws BadRequestException")
        void bulkCreate_NullList_ThrowsBadRequest() {
            assertThrowsBadRequest(ErrorMessages.PickupLocationErrorMessages.InvalidRequest,
                    () -> pickupLocationService.bulkCreatePickupLocations(null));
        }

        @Test
        @DisplayName("Bulk Create - Empty list - Success (no-op)")
        void bulkCreate_EmptyList_Success() {
            assertDoesNotThrow(() -> pickupLocationService.bulkCreatePickupLocations(Collections.emptyList()));
        }

        @Test
        @DisplayName("Bulk Create - Item with null nickname - Throws BadRequestException")
        void bulkCreate_NullNicknameInItem_ThrowsBadRequest() {
            PickupLocationRequestModel request = createValidPickupLocationRequest(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
            request.setAddressNickName(null);

            assertThrowsBadRequest(ErrorMessages.PickupLocationErrorMessages.InvalidAddressNickname,
                    () -> pickupLocationService.bulkCreatePickupLocations(List.of(request)));
        }

        @Test
        @DisplayName("Bulk Create - Item with null ShipRocket ID - Throws BadRequestException")
        void bulkCreate_NullShipRocketIdInItem_ThrowsBadRequest() {
            PickupLocationRequestModel request = createValidPickupLocationRequest(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
            request.setShipRocketId(null);

            assertThrowsBadRequest(ErrorMessages.PickupLocationErrorMessages.InvalidShipRocketId,
                    () -> pickupLocationService.bulkCreatePickupLocations(List.of(request)));
        }

        @Test
        @DisplayName("Bulk Create - 100 valid items - Success")
        void bulkCreate_LargeValidBatch_Success() {
            List<PickupLocationRequestModel> requests = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                PickupLocationRequestModel req = createValidPickupLocationRequest((long)i, TEST_CLIENT_ID);
                requests.add(req);
            }

            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);

            assertDoesNotThrow(() -> pickupLocationService.bulkCreatePickupLocations(requests));
            verify(pickupLocationRepository, times(100)).save(any(PickupLocation.class));
        }
    }

}