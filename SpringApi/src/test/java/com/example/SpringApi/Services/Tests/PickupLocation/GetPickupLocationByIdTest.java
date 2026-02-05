package com.example.SpringApi.Services.Tests.PickupLocation;

import com.example.SpringApi.Models.ResponseModels.PickupLocationResponseModel;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PickupLocationService.getPickupLocationById() method.
 * * Test Count: 9 tests
 */
@DisplayName("Get Pickup Location By ID Tests")
class GetPickupLocationByIdTest extends PickupLocationServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify permission check is performed for VIEW_PICKUP_LOCATION permission.
     * Expected Result: Authorization service is called to check permissions.
     * Assertions: authorization.hasAuthority() is called with correct permission.
     */
    @Test
    @DisplayName("Get Pickup Location By ID - Permission Check - Success Verifies Authorization")
    void getPickupLocationById_PermissionCheck_SuccessVerifiesAuthorization() {
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(testPickupLocation);
        lenient().when(authorization.hasAuthority(Authorizations.VIEW_PICKUP_LOCATIONS_PERMISSION)).thenReturn(true);

        pickupLocationService.getPickupLocationById(TEST_PICKUP_LOCATION_ID);

        verify(authorization, atLeastOnce()).hasAuthority(Authorizations.VIEW_PICKUP_LOCATIONS_PERMISSION);
    }

    /**
     * Purpose: Verify successful retrieval of pickup location by ID.
     * Expected Result: PickupLocationResponseModel is returned with correct data.
     * Assertions: Result is not null, ID matches expected value.
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
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject retrieval attempts using the maximum possible long ID if not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message indicates not found.
     */
    @Test
    @DisplayName("Get Pickup Location By ID - Max Long ID - Throws NotFoundException")
    void getPickupLocationById_MaxLongId_ThrowsNotFoundException() {
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class, 
            () -> pickupLocationService.getPickupLocationById(Long.MAX_VALUE));
        assertTrue(ex.getMessage().contains("Pickup location not found"));
    }

    /**
     * Purpose: Reject retrieval attempts using the minimum possible long ID if not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message indicates not found.
     */
    @Test
    @DisplayName("Get Pickup Location By ID - Min Long ID - Throws NotFoundException")
    void getPickupLocationById_MinLongId_ThrowsNotFoundException() {
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class, 
            () -> pickupLocationService.getPickupLocationById(Long.MIN_VALUE));
        assertTrue(ex.getMessage().contains("Pickup location not found"));
    }

    /**
     * Purpose: Reject retrieval attempts for negative IDs.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message indicates not found.
     */
    @Test
    @DisplayName("Get Pickup Location By ID - Negative ID - Throws NotFoundException")
    void getPickupLocationById_NegativeId_ThrowsNotFoundException() {
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class, 
            () -> pickupLocationService.getPickupLocationById(-1L));
        assertTrue(ex.getMessage().contains("Pickup location not found"));
    }

    /**
     * Purpose: Verify NotFoundException is thrown when a location is not found for the client.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception is thrown.
     */
    @Test
    @DisplayName("Get Pickup Location By ID - Not Found - Throws NotFoundException")
    void getPickupLocationById_NotFound_ThrowsNotFoundException() {
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
            .thenReturn(null);
        assertThrows(NotFoundException.class, 
            () -> pickupLocationService.getPickupLocationById(TEST_PICKUP_LOCATION_ID));
    }

    /**
     * Purpose: Reject retrieval attempts for a zero ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message indicates not found.
     */
    @Test
    @DisplayName("Get Pickup Location By ID - Zero ID - Throws NotFoundException")
    void getPickupLocationById_ZeroId_ThrowsNotFoundException() {
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class, 
            () -> pickupLocationService.getPickupLocationById(0L));
        assertTrue(ex.getMessage().contains("Pickup location not found"));
    }
}