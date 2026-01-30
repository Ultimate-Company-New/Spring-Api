package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.FilterQueryBuilder.PickupLocationFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.DatabaseModels.PickupLocation;
import com.example.SpringApi.Models.RequestModels.PickupLocationRequestModel;
import com.example.SpringApi.Models.ResponseModels.PickupLocationResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
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
 * Test Group Summary:
 * | Group Name                              | Number of Tests |
 * | :-------------------------------------- | :-------------- |
 * | GetPickupLocationByIdTests              | 6               |
 * | GetPickupLocationsInBatchesTests        | 6               |
 * | CreatePickupLocationTests               | 12              |
 * | UpdatePickupLocationTests               | 7               |
 * | TogglePickupLocationTests               | 6               |
 * | BulkCreatePickupLocationsTests          | 9               |
 * | **Total**                               | **46**          |
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
            assertEquals(ErrorMessages.PickupLocationErrorMessages.NotFound, ex.getMessage());
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
            assertEquals(ErrorMessages.PickupLocationErrorMessages.NotFound, ex.getMessage());
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
            assertEquals(ErrorMessages.PickupLocationErrorMessages.NotFound, ex.getMessage());
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
            assertEquals(ErrorMessages.PickupLocationErrorMessages.NotFound, ex.getMessage());
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
        @DisplayName("Get Pickup Locations In Batches - Success")
        void getPickupLocationsInBatches_Success() {
            PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
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
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for invalid pagination (end <= start).
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidPagination error.
         */
        @Test
        @DisplayName("Get Pickup Locations In Batches - Invalid Pagination (end <= start)")
        void getPickupLocationsInBatches_InvalidPagination_ThrowsBadRequestException() {
            PaginationBaseRequestModel paginationRequest = createValidPaginationRequest();
            paginationRequest.setStart(10);
            paginationRequest.setEnd(5);

            assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                    () -> pickupLocationService.getPickupLocationsInBatches(paginationRequest));
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for negative start value.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidPagination error.
         */
        @Test
        @DisplayName("Get Pickup Locations In Batches - Negative Start")
        void getPickupLocationsInBatches_NegativeStart_ThrowsBadRequestException() {
            PaginationBaseRequestModel request = createValidPaginationRequest();
            request.setStart(-1);
            request.setEnd(10);

            assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                    () -> pickupLocationService.getPickupLocationsInBatches(request));
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for negative end value.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidPagination error.
         */
        @Test
        @DisplayName("Get Pickup Locations In Batches - Negative End")
        void getPickupLocationsInBatches_NegativeEnd_ThrowsBadRequestException() {
            PaginationBaseRequestModel request = createValidPaginationRequest();
            request.setStart(0);
            request.setEnd(-10);

            assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                    () -> pickupLocationService.getPickupLocationsInBatches(request));
        }

        /**
         * Purpose: Verify that BadRequestException is thrown when start equals end.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidPagination error.
         */
        @Test
        @DisplayName("Get Pickup Locations In Batches - Start Equals End")
        void getPickupLocationsInBatches_StartEqualsEnd_ThrowsBadRequestException() {
            PaginationBaseRequestModel request = createValidPaginationRequest();
            request.setStart(25);
            request.setEnd(25);

            assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.InvalidPagination,
                    () -> pickupLocationService.getPickupLocationsInBatches(request));
        }

        /**
         * Purpose: Verify successful retrieval with valid pagination.
         * Expected Result: PaginationBaseResponseModel with data is returned.
         * Assertions: Result is not null, data has expected size.
         */
        @Test
        @DisplayName("Get Pickup Locations In Batches - Valid Pagination - Success")
        void getPickupLocationsInBatches_ValidPagination_Success() {
            PaginationBaseRequestModel request = createValidPaginationRequest();
            request.setStart(0);
            request.setEnd(20);
            request.setFilters(null);
            request.setIncludeDeleted(false);

            List<PickupLocation> dataList = Collections.singletonList(testPickupLocation);
            Page<PickupLocation> page = new PageImpl<>(dataList, PageRequest.of(0, 20), 1);
            lenient().when(pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), isNull(), anyString(), isNull(), anyBoolean(), any(Pageable.class)))
                    .thenReturn(page);

            PaginationBaseResponseModel<PickupLocationResponseModel> result =
                    pickupLocationService.getPickupLocationsInBatches(request);

            assertNotNull(result);
            assertEquals(1, result.getData().size());
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
        @DisplayName("Create Pickup Location - Null Request - Throws BadRequestException")
        void createPickupLocation_NullRequest_ThrowsBadRequestException() {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> pickupLocationService.createPickupLocation(null));
            assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidRequest, ex.getMessage());
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
            String longName = "a".repeat(256);
            testPickupLocationRequest.setAddressNickName(longName);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
            assertEquals(ErrorMessages.PickupLocationErrorMessages.AddressNickNameTooLong, ex.getMessage());
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
        @DisplayName("Create Pickup Location - Null ShipRocket ID - Throws BadRequestException")
        void createPickupLocation_NullShipRocketId_ThrowsBadRequestException() {
            testPickupLocationRequest.setShipRocketPickupLocationId(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
            assertNotNull(ex.getMessage());
        }

        /**
         * Purpose: Verify that NotFoundException is thrown when address is not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches AddressErrorMessages.NotFound.
         */
        @Test
        @DisplayName("Create Pickup Location - Address Not Found - Throws NotFoundException")
        void createPickupLocation_AddressNotFound_ThrowsNotFoundException() {
            when(addressRepository.findById(any())).thenReturn(Optional.empty());
            assertThrowsNotFound(ErrorMessages.AddressErrorMessages.NotFound,
                    () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
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
            when(addressRepository.findById(TEST_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
            when(shippingHelper.addPickupLocation(any(PickupLocation.class))).thenReturn(testShipRocketResponse);

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
            testPickupLocationRequest.setAddressNickName("");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
            assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidAddressNickName, ex.getMessage());
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

            PickupLocationRequestModel valid = new PickupLocationRequestModel();
            valid.setAddressNickName("Valid Location");
            valid.setShipRocketPickupLocationId(1L);
            requests.add(valid);

            PickupLocationRequestModel invalid = new PickupLocationRequestModel();
            invalid.setAddressNickName("");
            invalid.setShipRocketPickupLocationId(2L);
            requests.add(invalid);

            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);

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
            assertDoesNotThrow(() -> pickupLocationService.bulkCreatePickupLocations(new ArrayList<>()));
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
                PickupLocationRequestModel req = new PickupLocationRequestModel();
                req.setAddressNickName("Location " + i);
                req.setShipRocketPickupLocationId((long) i);
                requests.add(req);
            }

            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);

            assertDoesNotThrow(() -> pickupLocationService.bulkCreatePickupLocations(requests));
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for null list.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidRequest error.
         */
        @Test
        @DisplayName("Bulk Create Pickup Locations - Null List - Throws BadRequestException")
        void bulkCreatePickupLocations_NullList_ThrowsBadRequestException() {
            assertThrowsBadRequest(ErrorMessages.PickupLocationErrorMessages.InvalidRequest,
                    () -> pickupLocationService.bulkCreatePickupLocations(null));
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

            when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
            when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);

            assertDoesNotThrow(() -> pickupLocationService.bulkCreatePickupLocations(requests));
            verify(pickupLocationRepository, times(100)).save(any(PickupLocation.class));
        }

        /**
         * Purpose: Verify that BadRequestException is thrown when item has null nickname.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidAddressNickName error.
         */
        @Test
        @DisplayName("Bulk Create - Item with Null Nickname - Throws BadRequestException")
        void bulkCreatePickupLocations_NullNicknameInItem_ThrowsBadRequestException() {
            PickupLocationRequestModel request = createValidPickupLocationRequest(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
            request.setAddressNickName(null);

            assertThrowsBadRequest(ErrorMessages.PickupLocationErrorMessages.InvalidAddressNickName,
                    () -> pickupLocationService.bulkCreatePickupLocations(List.of(request)));
        }

        /**
         * Purpose: Verify that BadRequestException is thrown when item has null ShipRocket ID.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message is not null.
         */
        @Test
        @DisplayName("Bulk Create - Item with Null ShipRocket ID - Throws BadRequestException")
        void bulkCreatePickupLocations_NullShipRocketIdInItem_ThrowsBadRequestException() {
            PickupLocationRequestModel request = createValidPickupLocationRequest(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
            request.setShipRocketPickupLocationId(null);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> pickupLocationService.bulkCreatePickupLocations(List.of(request)));
            assertNotNull(ex.getMessage());
        }
    }

    // Helper method to create a valid PickupLocationRequestModel
    private PickupLocationRequestModel createValidPickupLocationRequest(Long pickupLocationId, Long clientId) {
        AddressRequestModel addressRequest = new AddressRequestModel();
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
