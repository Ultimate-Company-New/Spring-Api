package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.FilterQueryBuilder.PickupLocationFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.DatabaseModels.PickupLocation;
import com.example.SpringApi.Models.RequestModels.PickupLocationRequestModel;
import com.example.SpringApi.Models.ResponseModels.PickupLocationResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.ShippingResponseModel.AddPickupLocationResponseModel;
import com.example.SpringApi.Repositories.AddressRepository;
import com.example.SpringApi.Repositories.PackagePickupLocationMappingRepository;
import com.example.SpringApi.Repositories.PickupLocationRepository;
import com.example.SpringApi.Repositories.ProductPickupLocationMappingRepository;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PickupLocationService.
 *
 * Test Group Summary:
 * | Group Name                              | Number of Tests |
 * | :-------------------------------------- | :-------------- |
 * | GetPickupLocationByIdTests              | 12              |
 * | GetPickupLocationsInBatchesTests        | 1               |
 * | CreatePickupLocationTests               | 24              |
 * | UpdatePickupLocationTests               | 14              |
 * | TogglePickupLocationTests               | 12              |
 * | BulkCreatePickupLocationsTests          | 18              |
 * | **Total**                               | **81**          |
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PickupLocationService Unit Tests")
class PickupLocationServiceTest extends BaseTest {

    @Mock
    private PickupLocationRepository pickupLocationRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ProductPickupLocationMappingRepository productMappingRepository;

    @Mock
    private PackagePickupLocationMappingRepository packageMappingRepository;

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
    private static final Long TEST_CLIENT_ID = 1L;
    private static final Long TEST_SHIPROCKET_ID = 300L;
    private static final String TEST_ADDRESS_NICKNAME = "Home Warehouse";
    private static final String TEST_STREET_ADDRESS = "123 Main St";
    private static final String TEST_CITY = "New York";
    private static final String TEST_STATE = "NY";
    private static final String TEST_POSTAL_CODE = "10001";
    private static final String TEST_COUNTRY = "USA";
    private static final String CREATED_USER = DEFAULT_CREATED_USER;

    @BeforeEach
    void setUp() {
        AddressRequestModel addressRequest = new AddressRequestModel();
        addressRequest.setStreetAddress(TEST_STREET_ADDRESS);
        addressRequest.setCity(TEST_CITY);
        addressRequest.setState(TEST_STATE);
        addressRequest.setPostalCode(TEST_POSTAL_CODE);
        addressRequest.setCountry(TEST_COUNTRY);
        addressRequest.setAddressType("WAREHOUSE");
        addressRequest.setNameOnAddress("John Doe");
        addressRequest.setEmailOnAddress("john@example.com");
        addressRequest.setPhoneOnAddress("1234567890");

        testPickupLocationRequest = new PickupLocationRequestModel();
        testPickupLocationRequest.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        testPickupLocationRequest.setAddressNickName(TEST_ADDRESS_NICKNAME);
        testPickupLocationRequest.setPickupLocationAddressId(TEST_ADDRESS_ID);
        testPickupLocationRequest.setShipRocketPickupLocationId(TEST_SHIPROCKET_ID);
        testPickupLocationRequest.setAddress(addressRequest);
        testPickupLocationRequest.setIsDeleted(false);

        testAddress = new Address(addressRequest, CREATED_USER);
        testAddress.setAddressId(TEST_ADDRESS_ID);

        testPickupLocation = new PickupLocation(testPickupLocationRequest, CREATED_USER, TEST_CLIENT_ID);
        testPickupLocation.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        testPickupLocation.setShipRocketPickupLocationId(TEST_SHIPROCKET_ID);

        testShipRocketResponse = new AddPickupLocationResponseModel();
        testShipRocketResponse.setPickup_id(TEST_SHIPROCKET_ID);

        ClientResponseModel mockClient = new ClientResponseModel();
        mockClient.setShipRocketEmail("test@example.com");
        mockClient.setShipRocketPassword("testpassword");
        lenient().when(clientService.getClientById(anyLong())).thenReturn(mockClient);

        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
    }

    @Nested
    @DisplayName("GetPickupLocationByIdTests")
    class GetPickupLocationByIdTests {

        /**
         * Purpose: Verify successful retrieval of pickup location by ID.
         * Expected Result: PickupLocationResponseModel is returned with correct data.
         * Assertions: Result is not null, ID and nickname match expected values.
         */
        @Test
        @DisplayName("Get Pickup Location By ID - Success")
        void getPickupLocationById_Success() {
            when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                    .thenReturn(testPickupLocation);

            PickupLocationResponseModel result = pickupLocationService.getPickupLocationById(TEST_PICKUP_LOCATION_ID);

            assertNotNull(result);
            assertEquals(TEST_PICKUP_LOCATION_ID, result.getPickupLocationId());
            assertEquals(TEST_ADDRESS_NICKNAME, result.getAddressNickName());
            verify(pickupLocationRepository, times(1)).findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
        }

        /**
         * Purpose: Verify that NotFoundException is thrown when pickup location is not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message contains the pickup location ID.
         */
        @Test
        @DisplayName("Get Pickup Location By ID - Not Found")
        void getPickupLocationById_NotFound_ThrowsNotFoundException() {
            when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                    .thenReturn(null);

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> pickupLocationService.getPickupLocationById(TEST_PICKUP_LOCATION_ID));
            assertTrue(exception.getMessage().contains(String.valueOf(TEST_PICKUP_LOCATION_ID)));
        }

        /**
         * Purpose: Verify that NotFoundException is thrown for negative ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches expected error message.
         */
        @Test
        @DisplayName("Get Pickup Location By ID - Negative ID - Not Found")
        void getPickupLocationById_NegativeId_ThrowsNotFoundException() {
            when(pickupLocationRepository.findPickupLocationByIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> pickupLocationService.getPickupLocationById(-1L));
                assertEquals(String.format(ErrorMessages.PickupLocationErrorMessages.NotFound, -1L), ex.getMessage());
        }

        /**
         * Purpose: Verify that NotFoundException is thrown for zero ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches expected error message.
         */
        @Test
        @DisplayName("Get Pickup Location By ID - Zero ID - Not Found")
        void getPickupLocationById_ZeroId_ThrowsNotFoundException() {
            when(pickupLocationRepository.findPickupLocationByIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> pickupLocationService.getPickupLocationById(0L));
                assertEquals(String.format(ErrorMessages.PickupLocationErrorMessages.NotFound, 0L), ex.getMessage());
        }

        /**
         * Purpose: Verify that NotFoundException is thrown for Long.MAX_VALUE ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches expected error message.
         */
        @Test
        @DisplayName("Get Pickup Location By ID - Long.MAX_VALUE - Not Found")
        void getPickupLocationById_MaxLongId_ThrowsNotFoundException() {
            when(pickupLocationRepository.findPickupLocationByIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> pickupLocationService.getPickupLocationById(Long.MAX_VALUE));
                assertEquals(String.format(ErrorMessages.PickupLocationErrorMessages.NotFound, Long.MAX_VALUE), ex.getMessage());
        }

        /**
         * Purpose: Verify that NotFoundException is thrown for Long.MIN_VALUE ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches expected error message.
         */
        @Test
        @DisplayName("Get Pickup Location By ID - Long.MIN_VALUE - Not Found")
        void getPickupLocationById_MinLongId_ThrowsNotFoundException() {
            when(pickupLocationRepository.findPickupLocationByIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> pickupLocationService.getPickupLocationById(Long.MIN_VALUE));
                assertEquals(String.format(ErrorMessages.PickupLocationErrorMessages.NotFound, Long.MIN_VALUE), ex.getMessage());
        }

        /**
         * Purpose: Additional invalid ID coverage.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Message matches NotFound error.
         */
        @TestFactory
        @DisplayName("Get Pickup Location By ID - Additional invalid IDs")
        Stream<DynamicTest> getPickupLocationById_AdditionalInvalidIds() {
            return Stream.of(2L, 999L, -10L, 1000L, Long.MAX_VALUE - 1, Long.MIN_VALUE + 1)
                .map(id -> DynamicTest.dynamicTest("Invalid ID: " + id, () -> {
                when(pickupLocationRepository.findPickupLocationByIdAndClientId(id, TEST_CLIENT_ID))
                    .thenReturn(null);
                NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> pickupLocationService.getPickupLocationById(id));
                assertEquals(String.format(ErrorMessages.PickupLocationErrorMessages.NotFound, id), ex.getMessage());
                }));
        }
    }

    @Nested
    @DisplayName("GetPickupLocationsInBatchesTests")
    class GetPickupLocationsInBatchesTests {

        /**
         * Purpose: Verify successful retrieval of pickup locations with valid pagination.
         * Expected Result: PaginationBaseResponseModel with correct data is returned.
         * Assertions: Result is not null, data size and total count are correct.
         */
        @Test
        @DisplayName("Get Pickup Locations In Batches - Comprehensive validation and success")
        void getPickupLocationsInBatches_Comprehensive() {
            // Invalid pagination
            PaginationBaseRequestModel invalidPagination = createValidPaginationRequest();
            invalidPagination.setStart(10);
            invalidPagination.setEnd(5);
            assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                () -> pickupLocationService.getPickupLocationsInBatches(invalidPagination));

            // Invalid column
            PaginationBaseRequestModel invalidColumn = createValidPaginationRequest();
            invalidColumn.setStart(0);
            invalidColumn.setEnd(10);
            PaginationBaseRequestModel.FilterCondition badColumn = new PaginationBaseRequestModel.FilterCondition();
            badColumn.setColumn("invalidColumn");
            badColumn.setOperator("contains");
            badColumn.setValue("test");
            invalidColumn.setFilters(List.of(badColumn));
            invalidColumn.setLogicOperator("AND");
            BadRequestException invalidColumnEx = assertThrows(BadRequestException.class,
                () -> pickupLocationService.getPickupLocationsInBatches(invalidColumn));
            assertTrue(invalidColumnEx.getMessage().contains("Invalid column name"));

            // Invalid operator
            PaginationBaseRequestModel invalidOperator = createValidPaginationRequest();
            invalidOperator.setStart(0);
            invalidOperator.setEnd(10);
            PaginationBaseRequestModel.FilterCondition badOp = new PaginationBaseRequestModel.FilterCondition();
            badOp.setColumn("addressNickName");
            badOp.setOperator("invalidOperator");
            badOp.setValue("test");
            invalidOperator.setFilters(List.of(badOp));
            invalidOperator.setLogicOperator("AND");
            BadRequestException invalidOpEx = assertThrows(BadRequestException.class,
                () -> pickupLocationService.getPickupLocationsInBatches(invalidOperator));
            assertTrue(invalidOpEx.getMessage().contains("Invalid operator"));

            // Success without filters
            PaginationBaseRequestModel successRequest = createValidPaginationRequest();
            successRequest.setStart(0);
            successRequest.setEnd(10);
            successRequest.setFilters(null);
            successRequest.setIncludeDeleted(false);

            List<PickupLocation> dataList = Collections.singletonList(testPickupLocation);
            Page<PickupLocation> pageResult = new PageImpl<>(dataList, PageRequest.of(0, 10), 1);

            lenient().when(pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), isNull(), anyString(), isNull(), anyBoolean(), any(Pageable.class)))
                .thenReturn(pageResult);
            lenient().when(productMappingRepository.countByPickupLocationIds(anyList()))
                .thenReturn(Collections.emptyList());
            lenient().when(packageMappingRepository.countByPickupLocationIds(anyList()))
                .thenReturn(Collections.emptyList());

            PaginationBaseResponseModel<PickupLocationResponseModel> result =
                pickupLocationService.getPickupLocationsInBatches(successRequest);

            assertNotNull(result);
            assertEquals(1, result.getData().size());
            assertEquals(1, result.getTotalDataCount());
        }
    }

    @Nested
    @DisplayName("CreatePickupLocationTests")
    class CreatePickupLocationTests {

        /**
         * Purpose: Verify successful creation of pickup location.
         * Expected Result: Pickup location is created and saved without exception.
         * Assertions: Repository save methods are called, logging is performed.
         */
        @Test
        @DisplayName("Create Pickup Location - Success")
        void createPickupLocation_Success() throws Exception {
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
            when(shippingHelper.addPickupLocation(any(PickupLocation.class))).thenReturn(testShipRocketResponse);

            pickupLocationService.createPickupLocation(testPickupLocationRequest);

            verify(addressRepository, times(1)).save(any(Address.class));
            verify(pickupLocationRepository, times(2)).save(any(PickupLocation.class));
            verify(shippingHelper, times(1)).addPickupLocation(any(PickupLocation.class));
            verify(userLogService, times(1)).logData(
                    anyLong(),
                    eq(SuccessMessages.PickupLocationSuccessMessages.InsertPickupLocation + " " + TEST_PICKUP_LOCATION_ID),
                    eq(ApiRoutes.PickupLocationsSubRoute.CREATE_PICKUP_LOCATION));
        }

        /**
         * Purpose: Verify that ShipRocket API failure causes transaction rollback.
         * Expected Result: BadRequestException is thrown, logging is not performed.
         * Assertions: Exception is thrown, userLogService.logData is never called.
         */
        @Test
        @DisplayName("Create Pickup Location - ShipRocket API Failure")
        void createPickupLocation_ShipRocketFailure_TransactionRollsBack() throws Exception {
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
            when(shippingHelper.addPickupLocation(any(PickupLocation.class)))
                    .thenThrow(new BadRequestException("ShipRocket API error"));

            assertThrows(BadRequestException.class,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));

            verify(addressRepository, times(1)).save(any(Address.class));
            verify(pickupLocationRepository, times(1)).save(any(PickupLocation.class));
            verify(shippingHelper, times(1)).addPickupLocation(any(PickupLocation.class));
            verify(userLogService, never()).logData(anyLong(), any(), any());
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for null request.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidRequest error.
         */
        @Test
        @DisplayName("Create Pickup Location - Null Request - Throws NullPointerException")
        void createPickupLocation_NullRequest_ThrowsBadRequestException() {
            assertThrows(NullPointerException.class,
                () -> pickupLocationService.createPickupLocation(null));
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for null address nickname.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidAddressNickName error.
         */
        @Test
        @DisplayName("Create Pickup Location - Null Address Nickname - Throws BadRequestException")
        void createPickupLocation_NullAddressNickname_ThrowsBadRequestException() {
            testPickupLocationRequest.setAddressNickName(null);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
            assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidAddressNickName, ex.getMessage());
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for empty address nickname.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidAddressNickName error.
         */
        @Test
        @DisplayName("Create Pickup Location - Empty Address Nickname - Throws BadRequestException")
        void createPickupLocation_EmptyAddressNickname_ThrowsBadRequestException() {
            testPickupLocationRequest.setAddressNickName("");
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
            assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidAddressNickName, ex.getMessage());
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for whitespace address nickname.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidAddressNickName error.
         */
        @Test
        @DisplayName("Create Pickup Location - Whitespace Address Nickname - Throws BadRequestException")
        void createPickupLocation_WhitespaceAddressNickname_ThrowsBadRequestException() {
            testPickupLocationRequest.setAddressNickName("   ");
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
            assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidAddressNickName, ex.getMessage());
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for address nickname exceeding max length.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches AddressNickNameTooLong error.
         */
        @Test
        @DisplayName("Create Pickup Location - Address Nickname Too Long - Throws BadRequestException")
        void createPickupLocation_AddressNicknameTooLong_ThrowsBadRequestException() {
            String longName = "a".repeat(37);
            testPickupLocationRequest.setAddressNickName(longName);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
            assertEquals(ErrorMessages.PickupLocationErrorMessages.LocationNameTooLong, ex.getMessage());
        }

        /**
         * Purpose: Verify successful creation when address nickname is at max length (36 chars).
         * Expected Result: Pickup location is created without exception.
         * Assertions: Result is not null.
         */
        @Test
        @DisplayName("Create Pickup Location - Address Nickname Max Length (36) - Success")
        void createPickupLocation_AddressNicknameMaxLength_Success() {
            testPickupLocationRequest.setAddressNickName("a".repeat(36));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
            when(shippingHelper.addPickupLocation(any(PickupLocation.class))).thenReturn(testShipRocketResponse);

            assertDoesNotThrow(() -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for null ShipRocket Pickup Location ID.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception is thrown with correct message.
         */
        @Test
        @DisplayName("Create Pickup Location - Null ShipRocket ID - Success")
        void createPickupLocation_NullShipRocketId_ThrowsBadRequestException() {
            testPickupLocationRequest.setShipRocketPickupLocationId(null);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
            when(shippingHelper.addPickupLocation(any(PickupLocation.class))).thenReturn(testShipRocketResponse);

            assertDoesNotThrow(() -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
        }

        /**
         * Purpose: Verify that invalid address data throws BadRequestException.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches Address ER001.
         */
        @Test
        @DisplayName("Create Pickup Location - Invalid address data - Throws BadRequestException")
        void createPickupLocation_InvalidAddress_ThrowsBadRequestException() {
            testPickupLocationRequest.getAddress().setStreetAddress(null);

            BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.ER001, ex.getMessage());
        }

        /**
         * Purpose: Verify invalid ShipRocket response ID triggers BadRequestException.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message contains pickup_id validation text.
         */
        @Test
        @DisplayName("Create Pickup Location - ShipRocket pickup_id invalid")
        void createPickupLocation_ShipRocketInvalidId_ThrowsBadRequestException() throws Exception {
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
            AddPickupLocationResponseModel invalidResponse = new AddPickupLocationResponseModel();
            invalidResponse.setPickup_id(0L);
            when(shippingHelper.addPickupLocation(any(PickupLocation.class))).thenReturn(invalidResponse);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
            assertTrue(ex.getMessage().contains("pickup_id"));
        }

        /**
         * Purpose: Verify null address request throws BadRequestException.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches Address ER001.
         */
        @Test
        @DisplayName("Create Pickup Location - Null address - Throws BadRequestException")
        void createPickupLocation_NullAddress_ThrowsBadRequestException() {
            testPickupLocationRequest.setAddress(null);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.ER001, ex.getMessage());
        }

        /**
         * Purpose: Verify invalid ShipRocket ID in request throws BadRequestException.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidShipRocketId.
         */
        @Test
        @DisplayName("Create Pickup Location - Invalid ShipRocket ID in request")
        void createPickupLocation_InvalidShipRocketIdInRequest_ThrowsBadRequestException() {
            testPickupLocationRequest.setShipRocketPickupLocationId(0L);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
            assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidShipRocketId, ex.getMessage());
        }

        /**
         * Purpose: Verify address nickname at min length succeeds.
         * Expected Result: No exception is thrown.
         * Assertions: assertDoesNotThrow verifies success.
         */
        @Test
        @DisplayName("Create Pickup Location - Address Nickname length 1 - Success")
        void createPickupLocation_AddressNicknameLengthOne_Success() throws Exception {
            testPickupLocationRequest.setAddressNickName("A");
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
            when(shippingHelper.addPickupLocation(any(PickupLocation.class))).thenReturn(testShipRocketResponse);

            assertDoesNotThrow(() -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
        }

        /**
         * Purpose: Verify create uses address saved ID.
         * Expected Result: pickupLocationRepository.save is called.
         * Assertions: Repository save is called at least once.
         */
        @Test
        @DisplayName("Create Pickup Location - Saves pickup location")
        void createPickupLocation_SavesPickupLocation() throws Exception {
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
            when(shippingHelper.addPickupLocation(any(PickupLocation.class))).thenReturn(testShipRocketResponse);

            pickupLocationService.createPickupLocation(testPickupLocationRequest);

            verify(pickupLocationRepository, atLeastOnce()).save(any(PickupLocation.class));
        }
    }

    @Nested
    @DisplayName("UpdatePickupLocationTests")
    class UpdatePickupLocationTests {

        /**
         * Purpose: Verify successful update of pickup location.
         * Expected Result: Pickup location is updated and saved without exception.
         * Assertions: Repository methods are called, logging is performed.
         */
        @Test
        @DisplayName("Update Pickup Location - Success")
        void updatePickupLocation_Success() throws Exception {
            PickupLocation existingPickupLocation = new PickupLocation(testPickupLocationRequest, CREATED_USER, TEST_CLIENT_ID);
            existingPickupLocation.setPickupLocationId(TEST_PICKUP_LOCATION_ID);

            when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                    .thenReturn(existingPickupLocation);
            when(addressRepository.findById(anyLong())).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);

            pickupLocationService.updatePickupLocation(testPickupLocationRequest);

            verify(pickupLocationRepository, times(1)).findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
            verify(addressRepository, times(1)).findById(TEST_ADDRESS_ID);
            verify(addressRepository, times(1)).save(any(Address.class));
            verify(pickupLocationRepository, times(1)).save(any(PickupLocation.class));
        }

        /**
         * Purpose: Verify that NotFoundException is thrown when pickup location is not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message contains the pickup location ID.
         */
        @Test
        @DisplayName("Update Pickup Location - Pickup Location Not Found")
        void updatePickupLocation_PickupLocationNotFound_ThrowsNotFoundException() {
            lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                    .thenReturn(null);

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
            assertTrue(exception.getMessage().contains(String.valueOf(TEST_PICKUP_LOCATION_ID)));
        }

        /**
         * Purpose: Verify that NotFoundException is thrown when address is not found during update.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches AddressErrorMessages.NotFound.
         */
        @Test
        @DisplayName("Update Pickup Location - Address Not Found")
        void updatePickupLocation_AddressNotFound_ThrowsNotFoundException() {
            PickupLocation existingPickupLocation = new PickupLocation(testPickupLocationRequest, CREATED_USER, TEST_CLIENT_ID);
            existingPickupLocation.setPickupLocationId(TEST_PICKUP_LOCATION_ID);

            when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                    .thenReturn(existingPickupLocation);
            when(addressRepository.findById(TEST_ADDRESS_ID)).thenReturn(Optional.empty());

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
            assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        }

        /**
         * Purpose: Verify that NotFoundException is thrown for negative ID during update.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches NotFound error.
         */
        @Test
        @DisplayName("Update Pickup Location - Negative ID - Not Found")
        void updatePickupLocation_NegativeId_ThrowsNotFoundException() {
            testPickupLocationRequest.setPickupLocationId(-1L);
            when(pickupLocationRepository.findPickupLocationByIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
            assertTrue(ex.getMessage().contains("-1"));
        }

        /**
         * Purpose: Verify that NotFoundException is thrown for zero ID during update.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches NotFound error.
         */
        @Test
        @DisplayName("Update Pickup Location - Zero ID - Not Found")
        void updatePickupLocation_ZeroId_ThrowsNotFoundException() {
            testPickupLocationRequest.setPickupLocationId(0L);
            when(pickupLocationRepository.findPickupLocationByIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
            assertTrue(ex.getMessage().contains("0"));
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for null address nickname during update.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidAddressNickName error.
         */
        @Test
        @DisplayName("Update Pickup Location - Null Address Nickname - Throws BadRequestException")
        void updatePickupLocation_NullAddressNickname_ThrowsBadRequestException() {
            PickupLocation existingPickupLocation = new PickupLocation(testPickupLocationRequest, CREATED_USER, TEST_CLIENT_ID);
            existingPickupLocation.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
            when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(existingPickupLocation);
            when(addressRepository.findById(anyLong())).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            testPickupLocationRequest.setAddressNickName(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
            assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidAddressNickName, ex.getMessage());
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for empty address nickname during update.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidAddressNickName error.
         */
        @Test
        @DisplayName("Update Pickup Location - Empty Address Nickname - Throws BadRequestException")
        void updatePickupLocation_EmptyAddressNickname_ThrowsBadRequestException() {
            PickupLocation existingPickupLocation = new PickupLocation(testPickupLocationRequest, CREATED_USER, TEST_CLIENT_ID);
            existingPickupLocation.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
            when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(existingPickupLocation);
            when(addressRepository.findById(anyLong())).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            testPickupLocationRequest.setAddressNickName("");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
            assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidAddressNickName, ex.getMessage());
        }

        /**
         * Purpose: Verify invalid ShipRocket ID in update request throws BadRequestException.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidShipRocketId.
         */
        @Test
        @DisplayName("Update Pickup Location - Invalid ShipRocket ID - Throws BadRequestException")
        void updatePickupLocation_InvalidShipRocketId_ThrowsBadRequestException() {
            PickupLocation existingPickupLocation = new PickupLocation(testPickupLocationRequest, CREATED_USER, TEST_CLIENT_ID);
            existingPickupLocation.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
            when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(existingPickupLocation);
            when(addressRepository.findById(anyLong())).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            testPickupLocationRequest.setShipRocketPickupLocationId(0L);
            BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
            assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidShipRocketId, ex.getMessage());
        }

        /**
         * Purpose: Verify update skips ShipRocket call when address not provided.
         * Expected Result: shippingHelper is not invoked.
         * Assertions: addPickupLocation is never called.
         */
        @Test
        @DisplayName("Update Pickup Location - No address update - No ShipRocket call")
        void updatePickupLocation_NoAddressUpdate_NoShipRocketCall() throws Exception {
            PickupLocation existingPickupLocation = new PickupLocation(testPickupLocationRequest, CREATED_USER, TEST_CLIENT_ID);
            existingPickupLocation.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
            existingPickupLocation.setShipRocketPickupLocationId(TEST_SHIPROCKET_ID);

            testPickupLocationRequest.setAddress(null);
            when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(existingPickupLocation);
            when(addressRepository.findById(anyLong())).thenReturn(Optional.of(testAddress));
            when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);

            pickupLocationService.updatePickupLocation(testPickupLocationRequest);

            verify(shippingHelper, never()).addPickupLocation(any(PickupLocation.class));
        }

        /**
         * Purpose: Verify update preserves ShipRocket ID when address unchanged.
         * Expected Result: Updated pickup location keeps existing ShipRocket ID.
         * Assertions: Saved entity uses existing ShipRocket ID.
         */
        @Test
        @DisplayName("Update Pickup Location - Address unchanged keeps ShipRocket ID")
        void updatePickupLocation_AddressUnchanged_KeepsShipRocketId() throws Exception {
            PickupLocation existingPickupLocation = new PickupLocation(testPickupLocationRequest, CREATED_USER, TEST_CLIENT_ID);
            existingPickupLocation.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
            existingPickupLocation.setShipRocketPickupLocationId(TEST_SHIPROCKET_ID);

            when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(existingPickupLocation);
            when(addressRepository.findById(anyLong())).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(pickupLocationRepository.save(any(PickupLocation.class))).thenAnswer(invocation -> invocation.getArgument(0));

            pickupLocationService.updatePickupLocation(testPickupLocationRequest);

            verify(pickupLocationRepository, atLeastOnce()).save(argThat(saved ->
                TEST_SHIPROCKET_ID.equals(saved.getShipRocketPickupLocationId())));
        }

        /**
         * Purpose: Verify update throws for null request.
         * Expected Result: NullPointerException is thrown.
         * Assertions: Exception is thrown.
         */
        @Test
        @DisplayName("Update Pickup Location - Null request - Throws NullPointerException")
        void updatePickupLocation_NullRequest_ThrowsNullPointerException() {
            assertThrows(NullPointerException.class, () -> pickupLocationService.updatePickupLocation(null));
        }

        /**
         * Purpose: Additional invalid ID coverage for update.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message contains ID.
         */
        @TestFactory
        @DisplayName("Update Pickup Location - Additional invalid IDs")
        Stream<DynamicTest> updatePickupLocation_AdditionalInvalidIds() {
            return Stream.of(2L, 999L, Long.MAX_VALUE, Long.MIN_VALUE, -100L, 0L)
                .map(id -> DynamicTest.dynamicTest("Invalid ID: " + id, () -> {
                testPickupLocationRequest.setPickupLocationId(id);
                when(pickupLocationRepository.findPickupLocationByIdAndClientId(id, TEST_CLIENT_ID))
                    .thenReturn(null);
                NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
                assertTrue(ex.getMessage().contains(String.valueOf(id)));
                }));
        }
    }

    @Nested
    @DisplayName("TogglePickupLocationTests")
    class TogglePickupLocationTests {

        /**
         * Purpose: Verify successful toggle of pickup location deleted status.
         * Expected Result: Pickup location isDeleted flag is toggled, save is called.
         * Assertions: isDeleted is true after toggle, repository save is called.
         */
        @Test
        @DisplayName("Toggle Pickup Location - Success")
        void togglePickupLocation_Success() {
            when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                    .thenReturn(testPickupLocation);
            when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);

            pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);

            assertTrue(testPickupLocation.getIsDeleted());
            verify(pickupLocationRepository, times(1)).findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
            verify(pickupLocationRepository, times(1)).save(testPickupLocation);
        }

        /**
         * Purpose: Verify that NotFoundException is thrown when pickup location is not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception is thrown, repository save is never called.
         */
        @Test
        @DisplayName("Toggle Pickup Location - Not Found")
        void togglePickupLocation_NotFound_ThrowsNotFoundException() {
            lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                    .thenReturn(null);

            assertThrows(NotFoundException.class,
                    () -> pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID));
            verify(pickupLocationRepository, times(1)).findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
        }

        /**
         * Purpose: Verify that NotFoundException is thrown for negative ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches NotFound error.
         */
        @Test
        @DisplayName("Toggle Pickup Location - Negative ID - Not Found")
        void togglePickupLocation_NegativeId_ThrowsNotFoundException() {
            when(pickupLocationRepository.findPickupLocationByIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> pickupLocationService.togglePickupLocation(-1L));
            assertTrue(ex.getMessage().contains("-1"));
        }

        /**
         * Purpose: Verify that NotFoundException is thrown for zero ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches NotFound error.
         */
        @Test
        @DisplayName("Toggle Pickup Location - Zero ID - Not Found")
        void togglePickupLocation_ZeroId_ThrowsNotFoundException() {
            when(pickupLocationRepository.findPickupLocationByIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> pickupLocationService.togglePickupLocation(0L));
            assertTrue(ex.getMessage().contains("0"));
        }

        /**
         * Purpose: Verify that NotFoundException is thrown for Long.MAX_VALUE ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches NotFound error.
         */
        @Test
        @DisplayName("Toggle Pickup Location - Max Long ID - Not Found")
        void togglePickupLocation_MaxLongId_ThrowsNotFoundException() {
            when(pickupLocationRepository.findPickupLocationByIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> pickupLocationService.togglePickupLocation(Long.MAX_VALUE));
            assertNotNull(ex.getMessage());
        }

        /**
         * Purpose: Verify that multiple toggles correctly persist state.
         * Expected Result: isDeleted toggles between true and false.
         * Assertions: First toggle sets true, second toggle sets false.
         */
        @Test
        @DisplayName("Toggle Pickup Location - Multiple Toggles - State Persistence")
        void togglePickupLocation_MultipleToggles_StatePersists() {
            testPickupLocation.setIsDeleted(false);
            when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                    .thenReturn(testPickupLocation);
            when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);

            pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);
            assertTrue(testPickupLocation.getIsDeleted());

            pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);
            assertFalse(testPickupLocation.getIsDeleted());
        }

        /**
         * Purpose: Additional invalid ID coverage for toggle.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message is present.
         */
        @TestFactory
        @DisplayName("Toggle Pickup Location - Additional invalid IDs")
        Stream<DynamicTest> togglePickupLocation_AdditionalInvalidIds() {
            return Stream.of(2L, 999L, Long.MAX_VALUE, Long.MIN_VALUE, -100L, 0L)
                    .map(id -> DynamicTest.dynamicTest("Invalid ID: " + id, () -> {
                        when(pickupLocationRepository.findPickupLocationByIdAndClientId(id, TEST_CLIENT_ID))
                                .thenReturn(null);
                        NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> pickupLocationService.togglePickupLocation(id));
                        assertNotNull(ex.getMessage());
                    }));
        }
    }

    @Nested
    @DisplayName("BulkCreatePickupLocationsTests")
    class BulkCreatePickupLocationsTests {

        /**
         * Purpose: Verify bulk creation with all invalid addresses fails gracefully.
         * Expected Result: Operation completes without throwing exception.
         * Assertions: assertDoesNotThrow verifies graceful handling.
         */
        @Test
        @DisplayName("Bulk Create Pickup Locations - All Invalid Addresses")
        void bulkCreatePickupLocations_AllInvalidAddresses_AllFail() {
            List<PickupLocationRequestModel> requests = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                PickupLocationRequestModel req = new PickupLocationRequestModel();
                req.setAddressNickName("");
                req.setShipRocketPickupLocationId((long) i);
                requests.add(req);
            }

            assertDoesNotThrow(() -> pickupLocationService.bulkCreatePickupLocations(requests));
        }

        /**
         * Purpose: Verify bulk creation with mixed valid and invalid entries.
         * Expected Result: Operation completes with partial success.
         * Assertions: assertDoesNotThrow verifies operation completes.
         */
        @Test
        @DisplayName("Bulk Create Pickup Locations - Mixed Valid and Invalid")
        void bulkCreatePickupLocations_MixedValidInvalid_PartialSuccess() {
            List<PickupLocationRequestModel> requests = new ArrayList<>();

            PickupLocationRequestModel valid = createValidPickupLocationRequest(1L, TEST_CLIENT_ID);
            requests.add(valid);

            PickupLocationRequestModel invalid = new PickupLocationRequestModel();
            invalid.setAddressNickName("");
            invalid.setShipRocketPickupLocationId(2L);
            requests.add(invalid);

            lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            lenient().when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
            lenient().when(shippingHelper.addPickupLocation(any(PickupLocation.class))).thenReturn(testShipRocketResponse);

            assertDoesNotThrow(() -> pickupLocationService.bulkCreatePickupLocations(requests));
        }

        /**
         * Purpose: Verify bulk creation with empty list completes successfully.
         * Expected Result: Operation completes without throwing exception.
         * Assertions: assertDoesNotThrow verifies successful completion.
         */
        @Test
        @DisplayName("Bulk Create Pickup Locations - Empty List")
        void bulkCreatePickupLocations_EmptyList_ReturnsEmpty() {
            BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.bulkCreatePickupLocations(new ArrayList<>()));
            assertEquals(String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "Pickup location"),
                ex.getMessage());
        }

        /**
         * Purpose: Verify bulk creation with large batch (50 items) succeeds.
         * Expected Result: Operation completes without throwing exception.
         * Assertions: assertDoesNotThrow verifies successful completion.
         */
        @Test
        @DisplayName("Bulk Create Pickup Locations - Large Batch (50 items)")
        void bulkCreatePickupLocations_LargeBatch_Success() {
            List<PickupLocationRequestModel> requests = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                PickupLocationRequestModel req = createValidPickupLocationRequest((long) i, TEST_CLIENT_ID);
                requests.add(req);
            }

            lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            lenient().when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
            lenient().when(shippingHelper.addPickupLocation(any(PickupLocation.class))).thenReturn(testShipRocketResponse);

            BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);
            assertNotNull(result);
            assertEquals(50, result.getTotalRequested());
            assertEquals(50, result.getSuccessCount());
            assertEquals(0, result.getFailureCount());
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for null list.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidRequest error.
         */
        @Test
        @DisplayName("Bulk Create Pickup Locations - Null List - Throws BadRequestException")
        void bulkCreatePickupLocations_NullList_ThrowsBadRequestException() {
            BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.bulkCreatePickupLocations(null));
            assertEquals(String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "Pickup location"),
                ex.getMessage());
        }

        /**
         * Purpose: Verify bulk creation with 100 valid items succeeds.
         * Expected Result: Operation completes, repository save is called 100 times.
         * Assertions: assertDoesNotThrow verifies success, verify save call count.
         */
        @Test
        @DisplayName("Bulk Create Pickup Locations - 100 Valid Items - Success")
        void bulkCreatePickupLocations_LargeValidBatch_Success() {
            List<PickupLocationRequestModel> requests = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                PickupLocationRequestModel req = createValidPickupLocationRequest((long) i, TEST_CLIENT_ID);
                requests.add(req);
            }

            lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            lenient().when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
            lenient().when(shippingHelper.addPickupLocation(any(PickupLocation.class))).thenReturn(testShipRocketResponse);

            BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);
            assertNotNull(result);
            assertEquals(100, result.getTotalRequested());
            assertEquals(100, result.getSuccessCount());
            assertEquals(0, result.getFailureCount());
        }

        /**
         * Purpose: Verify that BadRequestException is thrown when item has null nickname.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidAddressNickName error.
         */
        @Test
        @DisplayName("Bulk Create - Item with Null Nickname - Failure recorded")
        void bulkCreatePickupLocations_NullNicknameInItem_ThrowsBadRequestException() {
            PickupLocationRequestModel request = createValidPickupLocationRequest(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
            request.setAddressNickName(null);
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

            BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(List.of(request));
            assertNotNull(result);
            assertEquals(1, result.getTotalRequested());
            assertEquals(0, result.getSuccessCount());
            assertEquals(1, result.getFailureCount());
        }

        /**
         * Purpose: Verify that BadRequestException is thrown when item has null ShipRocket ID.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message is not null.
         */
        @Test
        @DisplayName("Bulk Create - Item with Null ShipRocket ID - Success")
        void bulkCreatePickupLocations_NullShipRocketIdInItem_ThrowsBadRequestException() {
            PickupLocationRequestModel request = createValidPickupLocationRequest(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
            request.setShipRocketPickupLocationId(null);
            lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            lenient().when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
            lenient().when(shippingHelper.addPickupLocation(any(PickupLocation.class))).thenReturn(testShipRocketResponse);

            BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(List.of(request));
            assertNotNull(result);
            assertEquals(1, result.getTotalRequested());
            assertEquals(1, result.getSuccessCount());
            assertEquals(0, result.getFailureCount());
        }

        /**
         * Purpose: Verify bulk creation handles invalid ShipRocket IDs.
         * Expected Result: Failure is recorded for invalid entries.
         * Assertions: Failure count equals total requested.
         */
        @Test
        @DisplayName("Bulk Create - Invalid ShipRocket IDs")
        void bulkCreatePickupLocations_InvalidShipRocketIds_Failures() {
            List<PickupLocationRequestModel> requests = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                PickupLocationRequestModel req = createValidPickupLocationRequest((long) i, TEST_CLIENT_ID);
                req.setShipRocketPickupLocationId(0L);
                requests.add(req);
            }

            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);

            BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);

            assertNotNull(result);
            assertEquals(3, result.getTotalRequested());
            assertEquals(0, result.getSuccessCount());
            assertEquals(3, result.getFailureCount());
        }

        /**
         * Purpose: Verify bulk creation with mixed valid and invalid nicknames.
         * Expected Result: Partial success is reported.
         * Assertions: Success and failure counts match.
         */
        @Test
        @DisplayName("Bulk Create - Mixed nicknames - Partial success")
        void bulkCreatePickupLocations_MixedNicknames_PartialSuccess() {
            List<PickupLocationRequestModel> requests = new ArrayList<>();
            PickupLocationRequestModel valid = createValidPickupLocationRequest(10L, TEST_CLIENT_ID);
            PickupLocationRequestModel invalid = createValidPickupLocationRequest(11L, TEST_CLIENT_ID);
            invalid.setAddressNickName(" ");
            requests.add(valid);
            requests.add(invalid);

            lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            lenient().when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
            lenient().when(shippingHelper.addPickupLocation(any(PickupLocation.class))).thenReturn(testShipRocketResponse);

            BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);

            assertNotNull(result);
            assertEquals(2, result.getTotalRequested());
            assertEquals(1, result.getSuccessCount());
            assertEquals(1, result.getFailureCount());
        }

        /**
         * Purpose: Verify bulk creation respects address validation.
         * Expected Result: Invalid address causes failure.
         * Assertions: Failure count equals total.
         */
        @Test
        @DisplayName("Bulk Create - Invalid address - All fail")
        void bulkCreatePickupLocations_InvalidAddress_AllFail() {
            List<PickupLocationRequestModel> requests = new ArrayList<>();
            PickupLocationRequestModel req = createValidPickupLocationRequest(12L, TEST_CLIENT_ID);
            req.setAddress(null);
            requests.add(req);

            BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);

            assertNotNull(result);
            assertEquals(1, result.getTotalRequested());
            assertEquals(0, result.getSuccessCount());
            assertEquals(1, result.getFailureCount());
        }

        /**
         * Purpose: Verify bulk creation success with valid entries only.
         * Expected Result: All entries succeed.
         * Assertions: Success count equals total.
         */
        @Test
        @DisplayName("Bulk Create - All valid entries - Success")
        void bulkCreatePickupLocations_AllValid_Success() {
            List<PickupLocationRequestModel> requests = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                requests.add(createValidPickupLocationRequest((long) i, TEST_CLIENT_ID));
            }

            lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            lenient().when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
            lenient().when(shippingHelper.addPickupLocation(any(PickupLocation.class))).thenReturn(testShipRocketResponse);

            BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);

            assertNotNull(result);
            assertEquals(3, result.getTotalRequested());
            assertEquals(3, result.getSuccessCount());
            assertEquals(0, result.getFailureCount());
        }
    }

    // Helper method to create a valid PickupLocationRequestModel
    private PickupLocationRequestModel createValidPickupLocationRequest(Long pickupLocationId, Long clientId) {
        AddressRequestModel addressRequest = new AddressRequestModel();
        addressRequest.setAddressType("WAREHOUSE");
        addressRequest.setStreetAddress("123 Test St");
        addressRequest.setCity("Test City");
        addressRequest.setState("TS");
        addressRequest.setPostalCode("12345");
        addressRequest.setCountry("Test Country");
        addressRequest.setPhoneOnAddress("1234567890");

        PickupLocationRequestModel request = new PickupLocationRequestModel();
        request.setPickupLocationId(pickupLocationId);
        request.setAddressNickName("Location " + pickupLocationId);
        request.setPickupLocationAddressId(DEFAULT_ADDRESS_ID);
        request.setShipRocketPickupLocationId(pickupLocationId + 100);
        request.setAddress(addressRequest);
        request.setIsDeleted(false);
        return request;
    }
}
