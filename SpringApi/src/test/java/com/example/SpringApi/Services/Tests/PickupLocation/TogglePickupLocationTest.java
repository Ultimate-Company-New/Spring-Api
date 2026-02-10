package com.example.SpringApi.Services.Tests.PickupLocation;

import com.example.SpringApi.Controllers.PickupLocationController;
import com.example.SpringApi.Services.PickupLocationService;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PickupLocationService.togglePickupLocation() method.
 * Tests state transitions, edge cases, and client isolation.
 * Test Count: 14 tests
 */
@DisplayName("Toggle Pickup Location Tests")
class TogglePickupLocationTest extends PickupLocationServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify multiple toggles correctly persist state alternating between
     * true and false.
     * Expected Result: State transition: Active -> Deleted -> Active.
     * Assertions: isDeleted matches expected state after each toggle.
     */
    @Test
    @DisplayName("Toggle Pickup Location - Multiple Toggles - Success")
    void togglePickupLocation_MultipleToggles_Success() {
        testPickupLocation.setIsDeleted(false);
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(testPickupLocation);
        when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);

        pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);
        assertTrue(testPickupLocation.getIsDeleted());

        pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);
        assertFalse(testPickupLocation.getIsDeleted());
        verify(pickupLocationRepository, times(2)).save(testPickupLocation);
    }

    /**
     * Purpose: Verify toggle from deleted status to active status works correctly.
     * Expected Result: isDeleted changes from true to false.
     * Assertions: result is active (false).
     */
    @Test
    @DisplayName("Toggle Pickup Location - Restore from Deleted - Success")
    void togglePickupLocation_RestoreFromDeleted_Success() {
        testPickupLocation.setIsDeleted(true);
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(testPickupLocation);
        when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);

        pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);

        assertFalse(testPickupLocation.getIsDeleted());
    }

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
        when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);

        pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);

        assertTrue(testPickupLocation.getIsDeleted());
        verify(pickupLocationRepository, times(1)).save(testPickupLocation);
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject toggle attempts for the maximum possible long ID if not
     * found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message is not null.
     */
    @Test
    @DisplayName("Toggle Pickup Location - Max Long ID - Throws NotFoundException")
    void togglePickupLocation_MaxLongId_ThrowsNotFoundException() {
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID))
                .thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> pickupLocationService.togglePickupLocation(Long.MAX_VALUE));
        assertNotNull(ex.getMessage());
    }

    /**
     * Purpose: Reject toggle attempts for the minimum possible long ID if not
     * found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message is not null.
     */
    @Test
    @DisplayName("Toggle Pickup Location - Min Long ID - Throws NotFoundException")
    void togglePickupLocation_MinLongId_ThrowsNotFoundException() {
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID))
                .thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> pickupLocationService.togglePickupLocation(Long.MIN_VALUE));
        assertNotNull(ex.getMessage());
    }

    /**
     * Purpose: Reject toggle attempts for negative IDs.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message is not null.
     */
    @Test
    @DisplayName("Toggle Pickup Location - Negative ID - Throws NotFoundException")
    void togglePickupLocation_NegativeId_ThrowsNotFoundException() {
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(-100L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> pickupLocationService.togglePickupLocation(-100L));
        assertNotNull(ex.getMessage());
    }

    /**
     * Purpose: Reject toggle attempts when the pickup location is not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception is thrown.
     */
    @Test
    @DisplayName("Toggle Pickup Location - Not Found - Throws NotFoundException")
    void togglePickupLocation_NotFound_ThrowsNotFoundException() {
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(null);
        assertThrows(NotFoundException.class,
                () -> pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID));
    }

    /**
     * Purpose: Reject toggle attempts for a zero ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message is not null.
     */
    @Test
    @DisplayName("Toggle Pickup Location - Zero ID - Throws NotFoundException")
    void togglePickupLocation_ZeroId_ThrowsNotFoundException() {
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> pickupLocationService.togglePickupLocation(0L));
        assertNotNull(ex.getMessage());
    }

    /**
     * Purpose: Verify toggle preserves all other location properties except isDeleted.
     * Expected Result: Only isDeleted is toggled, other properties unchanged.
     * Assertions: ShipRocket ID and other properties remain the same.
     */
    @Test
    @DisplayName("Toggle Pickup Location - Preserves Other Properties - Success")
    void togglePickupLocation_PreservesOtherProperties_Success() {
        // ARRANGE
        testPickupLocation.setIsDeleted(false);
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(testPickupLocation);
        when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);

        // ACT
        pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);

        // ASSERT
        verify(pickupLocationRepository).save(argThat(saved -> 
            saved.getShipRocketPickupLocationId().equals(TEST_SHIPROCKET_ID)));
    }

    /**
     * Purpose: Test toggle with very large ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message is not null.
     */
    @Test
    @DisplayName("Toggle Pickup Location - Very Large ID - Throws NotFoundException")
    void togglePickupLocation_VeryLargeId_ThrowsNotFoundException() {
        // ARRANGE
        Long largeId = 999999999999L;
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(largeId, TEST_CLIENT_ID))
                .thenReturn(null);

        // ACT & ASSERT
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> pickupLocationService.togglePickupLocation(largeId));
        assertNotNull(ex.getMessage());
    }

    /**
     * Purpose: Test toggle respects client isolation.
     * Expected Result: Only searches for location in client's data.
     * Assertions: Repository called with correct client ID.
     */
    @Test
    @DisplayName("Toggle Pickup Location - Client Isolation - Success")
    void togglePickupLocation_ClientIsolation_Success() {
        // ARRANGE
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(testPickupLocation);
        when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);

        // ACT
        pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);

        // ASSERT
        verify(pickupLocationRepository).findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
    }

    /**
     * Purpose: Test consecutive toggles produce correct state sequence.
     * Expected Result: Toggling 3 times results in active state.
     * Assertions: State alternates correctly.
     */
    @Test
    @DisplayName("Toggle Pickup Location - Three Consecutive Toggles - Success")
    void togglePickupLocation_ThreeConsecutiveToggles_Success() {
        // ARRANGE
        testPickupLocation.setIsDeleted(false);
        when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(testPickupLocation);
        when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);

        // ACT - Toggle 3 times: false -> true -> false -> true
        pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);
        pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);
        pickupLocationService.togglePickupLocation(TEST_PICKUP_LOCATION_ID);

        // ASSERT
        verify(pickupLocationRepository, times(3)).save(testPickupLocation);
        assertTrue(testPickupLocation.getIsDeleted());
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("togglePickupLocation - Verify @PreAuthorize Annotation")
    void togglePickupLocation_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PickupLocationController.class.getMethod("togglePickupLocation", Long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on togglePickupLocation");
        assertTrue(annotation.value().contains(Authorizations.DELETE_PICKUP_LOCATIONS_PERMISSION),
                "@PreAuthorize should reference DELETE_PICKUP_LOCATIONS_PERMISSION");
    }

    @Test
    @DisplayName("togglePickupLocation - Controller delegates to service")
    void togglePickupLocation_WithValidId_DelegatesToService() {
        PickupLocationService mockService = mock(PickupLocationService.class);
        PickupLocationController controller = new PickupLocationController(mockService);
        doNothing().when(mockService).togglePickupLocation(TEST_PICKUP_LOCATION_ID);

        ResponseEntity<?> response = controller.togglePickupLocation(TEST_PICKUP_LOCATION_ID);

        verify(mockService).togglePickupLocation(TEST_PICKUP_LOCATION_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}