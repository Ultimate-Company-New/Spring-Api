package com.example.SpringApi.Services.Tests.Package;

import com.example.SpringApi.Controllers.PackageController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.SpringApi.Models.ResponseModels.PackageResponseModel;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.NotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
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

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("getPackagesByPickupLocationId - Verify @PreAuthorize Annotation")
    void getPackagesByPickupLocationId_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PackageController.class.getMethod("getPackagesByPickupLocationId", Long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.VIEW_PACKAGES_PERMISSION),
            "@PreAuthorize should reference VIEW_PACKAGES_PERMISSION");
    }

    @Test
    @DisplayName("getPackagesByPickupLocationId - Controller delegates to service")
    void getPackagesByPickupLocationId_WithValidRequest_DelegatesToService() {
        PackageController controller = new PackageController(packageService, null);
        when(packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID)).thenReturn(Arrays.asList(new PackageResponseModel(testPackage)));

        ResponseEntity<?> response = controller.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

        verify(packageService).getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}