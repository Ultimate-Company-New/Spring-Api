package com.example.SpringApi.Services.Tests;

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
 * This test class provides comprehensive coverage of PickupLocationService methods including:
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
class PickupLocationServiceTest {

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
    private HttpServletRequest request;

    @InjectMocks
    @Spy
    private PickupLocationService pickupLocationService;

    private PickupLocation testPickupLocation;
    private Address testAddress;
    private PickupLocationRequestModel testPickupLocationRequest;
    private AddPickupLocationResponseModel testShipRocketResponse;
    private static final Long TEST_PICKUP_LOCATION_ID = 1L;
    private static final Long TEST_ADDRESS_ID = 100L;
    private static final Long TEST_CLIENT_ID = 200L;
    private static final Long TEST_SHIPROCKET_ID = 300L;
    private static final String TEST_ADDRESS_NICKNAME = "Home Warehouse";
    private static final String TEST_STREET_ADDRESS = "123 Main St";
    private static final String TEST_CITY = "New York";
    private static final String TEST_STATE = "NY";
    private static final String TEST_POSTAL_CODE = "10001";
    private static final String TEST_COUNTRY = "USA";
    private static final String CREATED_USER = "admin";

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
        
        // Mock getClientId() to return TEST_CLIENT_ID
        lenient().when(pickupLocationService.getClientId()).thenReturn(TEST_CLIENT_ID);
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
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID)).thenReturn(testPickupLocation);

        // Act
        PickupLocationResponseModel result = pickupLocationService.getPickupLocationById(TEST_PICKUP_LOCATION_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_PICKUP_LOCATION_ID, result.getPickupLocationId());
        assertEquals(TEST_ADDRESS_NICKNAME, result.getAddressNickName());
        verify(pickupLocationRepository, times(1)).findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
    }

    /**
     * Test get pickup location by ID when not found.
     * Verifies that NotFoundException is thrown.
     */
    @Test
    @DisplayName("Get Pickup Location By ID - Not Found")
    void getPickupLocationById_NotFound_ThrowsNotFoundException() {
        // Arrange
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> pickupLocationService.getPickupLocationById(TEST_PICKUP_LOCATION_ID));
        assertTrue(exception.getMessage().contains(String.valueOf(TEST_PICKUP_LOCATION_ID)));
        verify(pickupLocationRepository, times(1)).findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
    }

    // ==================== Get Pickup Locations In Batches Tests ====================

    /**
     * Test successful retrieval of pickup locations in batches.
     * Verifies pagination and data conversion.
     */
    @Test
    @DisplayName("Get Pickup Locations In Batches - Success")
    void getPickupLocationsInBatches_Success() {
        // Arrange
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setStart(0);
        paginationRequest.setEnd(10);
        paginationRequest.setColumnName("pickupLocationId");
        paginationRequest.setCondition("equals");
        paginationRequest.setFilterExpr("1");
        paginationRequest.setIncludeDeleted(false);

        List<PickupLocation> dataList = Collections.singletonList(testPickupLocation);
        Page<PickupLocation> pageResult = new PageImpl<>(dataList, PageRequest.of(0, 10), 1);

        when(pickupLocationRepository.findPaginatedPickupLocations(
            eq(TEST_CLIENT_ID), eq("pickupLocationId"), eq("equals"), eq("1"), eq(false), any(Pageable.class)))
            .thenReturn(pageResult);

        // Act
        PaginationBaseResponseModel<PickupLocationResponseModel> result = pickupLocationService.getPickupLocationsInBatches(paginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1, result.getTotalDataCount());
        verify(pickupLocationRepository, times(1)).findPaginatedPickupLocations(
            eq(TEST_CLIENT_ID), eq("pickupLocationId"), eq("equals"), eq("1"), eq(false), any(Pageable.class));
    }

    /**
     * Test get pickup locations with invalid column name.
     * Verifies that BadRequestException is thrown.
     */
    @Test
    @DisplayName("Get Pickup Locations In Batches - Invalid Column")
    void getPickupLocationsInBatches_InvalidColumn_ThrowsBadRequestException() {
        // Arrange
        PaginationBaseRequestModel paginationRequest = new PaginationBaseRequestModel();
        paginationRequest.setColumnName("invalidColumn");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> pickupLocationService.getPickupLocationsInBatches(paginationRequest));
        assertTrue(exception.getMessage().contains("Invalid column name"));
    }

    // ==================== Create Pickup Location Tests ====================

    /**
     * Test successful creation of pickup location.
     * Verifies address creation, pickup location creation, ShipRocket integration, and logging.
     */
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
        verify(pickupLocationRepository, times(2)).save(any(PickupLocation.class)); // Once for creation, once for ShipRocket ID update
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
     * Verifies address update, pickup location update, ShipRocket integration, and logging.
     */
    @Test
    @DisplayName("Update Pickup Location - Success")
    void updatePickupLocation_Success() throws Exception {
        // Arrange
        PickupLocation existingPickupLocation = new PickupLocation(testPickupLocationRequest, CREATED_USER, TEST_CLIENT_ID);
        existingPickupLocation.setPickupLocationId(TEST_PICKUP_LOCATION_ID);

        when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID)).thenReturn(existingPickupLocation);
        when(addressRepository.findById(TEST_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);
        when(shippingHelper.addPickupLocation(any(PickupLocation.class))).thenReturn(testShipRocketResponse);

        // Act
        pickupLocationService.updatePickupLocation(testPickupLocationRequest);

        // Assert
        verify(pickupLocationRepository, times(1)).findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
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
        lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> pickupLocationService.updatePickupLocation(testPickupLocationRequest));
        assertTrue(exception.getMessage().contains(String.valueOf(TEST_PICKUP_LOCATION_ID)));
        verify(pickupLocationRepository, times(1)).findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
    }

    /**
     * Test update pickup location when address not found.
     * Verifies that NotFoundException is thrown.
     */
    @Test
    @DisplayName("Update Pickup Location - Address Not Found")
    void updatePickupLocation_AddressNotFound_ThrowsNotFoundException() {
        // Arrange
        PickupLocation existingPickupLocation = new PickupLocation(testPickupLocationRequest, CREATED_USER, TEST_CLIENT_ID);
        existingPickupLocation.setPickupLocationId(TEST_PICKUP_LOCATION_ID);

        when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID)).thenReturn(existingPickupLocation);
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
        PickupLocation existingPickupLocation = new PickupLocation(testPickupLocationRequest, CREATED_USER, TEST_CLIENT_ID);
        existingPickupLocation.setPickupLocationId(TEST_PICKUP_LOCATION_ID);

        when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID)).thenReturn(existingPickupLocation);
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

    // ==================== Toggle Pickup Location Tests ====================

    /**
     * Test successful toggle of pickup location.
     * Verifies that isDeleted flag is toggled and logging occurs.
     */
    @Test
    @DisplayName("Toggle Pickup Location - Success")
    void togglePickupLocation_Success() {
        // Arrange
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID)).thenReturn(testPickupLocation);
        when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(testPickupLocation);

        // Act
        pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);

        // Assert
        assertTrue(testPickupLocation.getIsDeleted());
        verify(pickupLocationRepository, times(1)).findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
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
        lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID));
        assertTrue(exception.getMessage().contains(String.valueOf(TEST_PICKUP_LOCATION_ID)));
        verify(pickupLocationRepository, times(1)).findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
    }

}