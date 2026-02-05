package com.example.SpringApi.Services.Tests.Package;

import com.example.SpringApi.Models.ResponseModels.PackageResponseModel;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PackageService.getPackagesByPickupLocationId() method.
 * * Test Count: 9 tests
 */
@DisplayName("Get Packages By Pickup Location ID Tests")
class GetPackagesByPickupLocationIdTest extends PackageServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify empty result when no packages are mapped to a valid pickup location.
     * Expected Result: Empty list is returned.
     * Assertions: result.isEmpty() is true.
     */
    @Test
    @DisplayName("Get Packages By Pickup Location ID - Empty Result - Success")
    void getPackagesByPickupLocationId_EmptyResult_Success() {
        when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID)).thenReturn(1L);
        when(packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID)).thenReturn(Arrays.asList());

        List<PackageResponseModel> result = packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Purpose: Verify multiple packages are returned for a pickup location.
     * Expected Result: List contains multiple package entries.
     * Assertions: result.size() matches the mapping count.
     */
    @Test
    @DisplayName("Get Packages By Pickup Location ID - Multiple Packages - Success")
    void getPackagesByPickupLocationId_MultiplePackages_Success() {
        when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID)).thenReturn(1L);
        when(packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(Arrays.asList(testMapping, testMapping));

        List<PackageResponseModel> result = packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

        assertEquals(2, result.size());
    }

    /**
     * Purpose: Verify permission check is performed for VIEW_PACKAGE permission.
     * Expected Result: Authorization service is called to check permissions.
     * Assertions: authorization.hasAuthority() is called with correct permission.
     */
    @Test
    @DisplayName("Get Packages By Pickup Location ID - Permission Check - Success Verifies Authorization")
    void getPackagesByPickupLocationId_PermissionCheck_SuccessVerifiesAuthorization() {
        when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID)).thenReturn(1L);
        lenient().when(authorization.hasAuthority(Authorizations.VIEW_PACKAGES_PERMISSION)).thenReturn(true);

        packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

        verify(authorization, atLeastOnce()).hasAuthority(Authorizations.VIEW_PACKAGES_PERMISSION);
    }

    /**
     * Purpose: Verify successful retrieval of packages for a valid pickup location.
     * Expected Result: List of packages is returned.
     * Assertions: ID matches the mapped package entity.
     */
    @Test
    @DisplayName("Get Packages By Pickup Location ID - Success")
    void getPackagesByPickupLocationId_Success() {
        when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID)).thenReturn(1L);
        when(packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID)).thenReturn(Arrays.asList(testMapping));

        List<PackageResponseModel> result = packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

        assertNotNull(result);
        assertEquals(TEST_PACKAGE_ID, result.get(0).getPackageId());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject retrieval attempts for Long.MAX_VALUE if location not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message indicates not found.
     */
    @Test
    @DisplayName("Get Packages By Pickup Location ID - Max Long ID - Throws NotFoundException")
    void getPackagesByPickupLocationId_MaxLongId_ThrowsNotFoundException() {
        when(pickupLocationRepository.countByPickupLocationIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(0L);
        assertThrows(NotFoundException.class, () -> packageService.getPackagesByPickupLocationId(Long.MAX_VALUE));
    }

    /**
     * Purpose: Reject retrieval attempts for negative IDs.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message indicates not found.
     */
    @Test
    @DisplayName("Get Packages By Pickup Location ID - Negative ID - Throws NotFoundException")
    void getPackagesByPickupLocationId_NegativeId_ThrowsNotFoundException() {
        when(pickupLocationRepository.countByPickupLocationIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(0L);
        assertThrows(NotFoundException.class, () -> packageService.getPackagesByPickupLocationId(-1L));
    }

    /**
     * Purpose: Reject retrieval attempts when the pickup location is not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception is thrown.
     */
    @Test
    @DisplayName("Get Packages By Pickup Location ID - Not Found - Throws NotFoundException")
    void getPackagesByPickupLocationId_NotFound_ThrowsNotFoundException() {
        when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID)).thenReturn(0L);
        assertThrows(NotFoundException.class, () -> packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID));
    }

    /**
     * Purpose: Reject retrieval attempts for a zero ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message indicates not found.
     */
    @Test
    @DisplayName("Get Packages By Pickup Location ID - Zero ID - Throws NotFoundException")
    void getPackagesByPickupLocationId_ZeroId_ThrowsNotFoundException() {
        when(pickupLocationRepository.countByPickupLocationIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(0L);
        assertThrows(NotFoundException.class, () -> packageService.getPackagesByPickupLocationId(0L));
    }
}