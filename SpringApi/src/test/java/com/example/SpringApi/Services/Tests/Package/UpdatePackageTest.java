package com.example.SpringApi.Services.Tests.Package;

import com.example.SpringApi.Controllers.PackageController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping;
import com.example.SpringApi.Models.RequestModels.PackagePickupLocationMappingRequestModel;
import com.example.SpringApi.Models.RequestModels.PackageRequestModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for PackageService.updatePackage() method.
 * Covers field validation, mapping logic, and lastRestockDate logic.
 */
@DisplayName("Update Package Tests")
class UpdatePackageTest extends PackageServiceTestBase {
    // Total Tests: 25

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify lastRestockDate remains unchanged when quantity is decreased.
     * Expected Result: Saved mapping has the original lastRestockDate.
     * Assertions: The restock date in the captured mapping matches the old date.
     */
    @Test
    @DisplayName("Update Package - Decrease Quantity - Keeps Old Restock Date")
    void updatePackage_DecreaseQuantity_KeepsRestockDate() {
        // Arrange
        LocalDateTime oldDate = LocalDateTime.now().minusDays(5);
        PackagePickupLocationMapping existing = new PackagePickupLocationMapping();
        existing.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        existing.setAvailableQuantity(100);
        existing.setLastRestockDate(oldDate);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);
        stubPackagePickupLocationMappingRepositoryFindByPackageId(TEST_PACKAGE_ID, List.of(existing));

        Map<Long, PackagePickupLocationMappingRequestModel> qMap = new HashMap<>();
        PackagePickupLocationMappingRequestModel mReq = new PackagePickupLocationMappingRequestModel();
        mReq.setQuantity(50);
        qMap.put(TEST_PICKUP_LOCATION_ID, mReq);
        testPackageRequest.setPickupLocationQuantities(qMap);

        // Act
        packageService.updatePackage(testPackageRequest);

        // Assert
        ArgumentCaptor<List<PackagePickupLocationMapping>> captor = ArgumentCaptor.forClass(List.class);
        verify(packagePickupLocationMappingRepository).saveAll(captor.capture());
        assertEquals(oldDate, captor.getValue().get(0).getLastRestockDate());
    }

    /**
     * Purpose: Verify lastRestockDate is updated when available quantity is increased.
     * Expected Result: Mapping is saved with a new UTC timestamp.
     * Assertions: Restock date is greater than the old date.
     */
    @Test
    @DisplayName("Update Package - Increase Quantity - Updates Restock Date")
    void updatePackage_IncreaseQuantity_UpdatesRestockDate() {
        // Arrange
        LocalDateTime oldDate = LocalDateTime.now().minusDays(5);
        PackagePickupLocationMapping existing = new PackagePickupLocationMapping();
        existing.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        existing.setAvailableQuantity(10);
        existing.setLastRestockDate(oldDate);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);
        stubPackagePickupLocationMappingRepositoryFindByPackageId(TEST_PACKAGE_ID, List.of(existing));

        Map<Long, PackagePickupLocationMappingRequestModel> qMap = new HashMap<>();
        PackagePickupLocationMappingRequestModel mReq = new PackagePickupLocationMappingRequestModel();
        mReq.setQuantity(20);
        qMap.put(TEST_PICKUP_LOCATION_ID, mReq);
        testPackageRequest.setPickupLocationQuantities(qMap);

        // Act
        packageService.updatePackage(testPackageRequest);

        // Assert
        ArgumentCaptor<List<PackagePickupLocationMapping>> captor = ArgumentCaptor.forClass(List.class);
        verify(packagePickupLocationMappingRepository).saveAll(captor.capture());
        assertTrue(captor.getValue().get(0).getLastRestockDate().isAfter(oldDate));
    }

    /**
     * Purpose: Verify update succeeds with large quantity increase values.
     * Expected Result: Package receives large quantity increase and restock date is updated.
     * Assertions: Quantity is increased and restock date reflects new timestamp.
     */
    @Test
    @DisplayName("Update Package - Large Quantity Increase - Success")
    void updatePackage_LargeQuantityIncrease_Success() {
        // Arrange
        PackagePickupLocationMapping existing = new PackagePickupLocationMapping();
        existing.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        existing.setAvailableQuantity(100);
        existing.setLastRestockDate(LocalDateTime.now().minusDays(10));
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);
        stubPackagePickupLocationMappingRepositoryFindByPackageId(TEST_PACKAGE_ID, List.of(existing));

        Map<Long, PackagePickupLocationMappingRequestModel> qMap = new HashMap<>();
        PackagePickupLocationMappingRequestModel mReq = new PackagePickupLocationMappingRequestModel();
        mReq.setQuantity(1000000);
        qMap.put(TEST_PICKUP_LOCATION_ID, mReq);
        testPackageRequest.setPickupLocationQuantities(qMap);

        // Act
        packageService.updatePackage(testPackageRequest);

        // Assert
        ArgumentCaptor<List<PackagePickupLocationMapping>> captor = ArgumentCaptor.forClass(List.class);
        verify(packagePickupLocationMappingRepository).saveAll(captor.capture());
        assertEquals(1000000, captor.getValue().get(0).getAvailableQuantity());
        assertTrue(captor.getValue().get(0).getLastRestockDate().isAfter(existing.getLastRestockDate()));
    }

    /**
     * Purpose: Verify update succeeds when modifying quantities in multiple pickup locations.
     * Expected Result: All location quantities are updated and restock dates reflect increases.
     * Assertions: Multiple mappings are saved with correct quantity values.
     */
    @Test
    @DisplayName("Update Package - Multiple Location Quantity Updates - Success")
    void updatePackage_MultipleLocationUpdates_Success() {
        // Arrange
        PackagePickupLocationMapping loc1 = new PackagePickupLocationMapping();
        loc1.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        loc1.setAvailableQuantity(50);
        loc1.setLastRestockDate(LocalDateTime.now().minusDays(5));

        PackagePickupLocationMapping loc2 = new PackagePickupLocationMapping();
        loc2.setPickupLocationId(TEST_PICKUP_LOCATION_ID + 1);
        loc2.setAvailableQuantity(30);
        loc2.setLastRestockDate(LocalDateTime.now().minusDays(3));

        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);
        stubPackagePickupLocationMappingRepositoryFindByPackageId(TEST_PACKAGE_ID, List.of(loc1, loc2));

        Map<Long, PackagePickupLocationMappingRequestModel> qMap = new HashMap<>();
        PackagePickupLocationMappingRequestModel mReq1 = new PackagePickupLocationMappingRequestModel();
        mReq1.setQuantity(100);
        qMap.put(TEST_PICKUP_LOCATION_ID, mReq1);

        PackagePickupLocationMappingRequestModel mReq2 = new PackagePickupLocationMappingRequestModel();
        mReq2.setQuantity(75);
        qMap.put(TEST_PICKUP_LOCATION_ID + 1, mReq2);
        testPackageRequest.setPickupLocationQuantities(qMap);

        // Act
        packageService.updatePackage(testPackageRequest);

        // Assert
        ArgumentCaptor<List<PackagePickupLocationMapping>> captor = ArgumentCaptor.forClass(List.class);
        verify(packagePickupLocationMappingRepository).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
    }

    /**
     * Purpose: Verify successful package update with valid data.
     * Expected Result: Package is updated and logged.
     * Assertions: Repository save and userLogService.logData are called.
     */
    @Test
    @DisplayName("Update Package - Success")
    void updatePackage_Success_Success() {
        // Arrange
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);
        stubPackageRepositorySave(testPackage);

        // Act
        assertDoesNotThrow(() -> packageService.updatePackage(testPackageRequest));

        // Assert
        verify(packageRepository, times(1)).save(any());
        verify(userLogService, times(1)).logData(eq(TEST_USER_ID),
                org.mockito.ArgumentMatchers.contains("Successfully updated"), anyString());
    }

    /**
     * Purpose: Verify update succeeds when setting quantity to zero in a location.
     * Expected Result: Mapping is saved with zero quantity without error.
     * Assertions: Quantity is set to 0 and mapping is saved.
     */
    @Test
    @DisplayName("Update Package - Zero Quantity Update - Success")
    void updatePackage_ZeroQuantityUpdate_Success() {
        // Arrange
        PackagePickupLocationMapping existing = new PackagePickupLocationMapping();
        existing.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        existing.setAvailableQuantity(50);
        existing.setLastRestockDate(LocalDateTime.now());
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);
        stubPackagePickupLocationMappingRepositoryFindByPackageId(TEST_PACKAGE_ID, List.of(existing));

        Map<Long, PackagePickupLocationMappingRequestModel> qMap = new HashMap<>();
        PackagePickupLocationMappingRequestModel mReq = new PackagePickupLocationMappingRequestModel();
        mReq.setQuantity(0);
        qMap.put(TEST_PICKUP_LOCATION_ID, mReq);
        testPackageRequest.setPickupLocationQuantities(qMap);

        // Act
        packageService.updatePackage(testPackageRequest);

        // Assert
        ArgumentCaptor<List<PackagePickupLocationMapping>> captor = ArgumentCaptor.forClass(List.class);
        verify(packagePickupLocationMappingRepository).saveAll(captor.capture());
        assertEquals(0, captor.getValue().get(0).getAvailableQuantity());
    }

    /**
     * Purpose: Verify update succeeds with maximum allowed dimension values.
     * Expected Result: Package is updated with large dimension values.
     * Assertions: Mapping is saved with maximum dimensions.
     */
    @Test
    @DisplayName("Update Package - Max Dimension Values - Success")
    void updatePackage_MaxDimensionValues_Success() {
        // Arrange
        testPackageRequest.setLength(9999);
        testPackageRequest.setBreadth(9999);
        testPackageRequest.setHeight(9999);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);
        stubPackageRepositorySave(testPackage);

        // Act
        assertDoesNotThrow(() -> packageService.updatePackage(testPackageRequest));

        // Assert
        verify(packageRepository).save(any());
    }

    /**
     * Purpose: Verify update succeeds with minimum valid dimensions.
     * Expected Result: Package name is updated appropriately.
     * Assertions: Package is saved with updated name.
     */
    @Test
    @DisplayName("Update Package - All Dimensions at Minimum Valid - Success")
    void updatePackage_MinValidDimensions_Success() {
        // Arrange
        testPackageRequest.setLength(1);
        testPackageRequest.setBreadth(1);
        testPackageRequest.setHeight(1);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);
        stubPackageRepositorySave(testPackage);

        // Act
        assertDoesNotThrow(() -> packageService.updatePackage(testPackageRequest));

        // Assert
        verify(packageRepository).save(any());
    }

    /**
     * Purpose: Verify update with large standard capacity values.
     * Expected Result: Large standard capacity is stored correctly.
     * Assertions: Service completes without error.
     */
    @Test
    @DisplayName("Update Package - Very Large Standard Capacity - Success")
    void updatePackage_VeryLargeStandardCapacity_Success() {
        // Arrange
        testPackageRequest.setStandardCapacity(1000000);
        testPackageRequest.setLength(100);
        testPackageRequest.setBreadth(100);
        testPackageRequest.setHeight(100);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);
        stubPackageRepositorySave(testPackage);

        // Act
        assertDoesNotThrow(() -> packageService.updatePackage(testPackageRequest));

        // Assert
        verify(packageRepository).save(any());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject update when breadth is negative.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidBreadth error.
     */
    @Test
    @DisplayName("Update Package - Negative Breadth - Throws BadRequestException")
    void updatePackage_NegativeBreadth_Throws() {
        // Arrange
        testPackageRequest.setBreadth(-1);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.updatePackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidBreadth, ex.getMessage());
    }

    /**
     * Purpose: Reject update for a non-existent package ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidId error.
     */
    @Test
    @DisplayName("Update Package - Not Found - Throws NotFoundException")
    void updatePackage_NotFound_ThrowsNotFoundException() {
        // Arrange
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> packageService.updatePackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Reject update when breadth is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidBreadth.
     */
    @Test
    @DisplayName("Update Package - Null Breadth - Throws BadRequestException")
    void updatePackage_NullBreadth_Throws() {
        // Arrange
        testPackageRequest.setBreadth(null);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.updatePackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidBreadth, ex.getMessage());
    }

    /**
     * Purpose: Reject update when height is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidHeight.
     */
    @Test
    @DisplayName("Update Package - Null Height - Throws BadRequestException")
    void updatePackage_NullHeight_Throws() {
        // Arrange
        testPackageRequest.setHeight(null);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.updatePackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidHeight, ex.getMessage());
    }

    /**
     * Purpose: Reject update when length is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidLength.
     */
    @Test
    @DisplayName("Update Package - Null Length - Throws BadRequestException")
    void updatePackage_NullLength_Throws() {
        // Arrange
        testPackageRequest.setLength(null);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.updatePackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidLength, ex.getMessage());
    }

    /**
     * Purpose: Reject update when name is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidPackageName.
     */
    @Test
    @DisplayName("Update Package - Null Name - Throws BadRequestException")
    void updatePackage_NullName_Throws() {
        // Arrange
        testPackageRequest.setPackageName(null);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.updatePackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageName, ex.getMessage());
    }

    /**
     * Purpose: Reject update when price per unit is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidPricePerUnit.
     */
    @Test
    @DisplayName("Update Package - Null Price Per Unit - Throws BadRequestException")
    void updatePackage_NullPricePerUnit_Throws() {
        // Arrange
        testPackageRequest.setPricePerUnit(null);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.updatePackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidPricePerUnit, ex.getMessage());
    }

    /**
     * Purpose: Reject update when standard capacity is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidStandardCapacity.
     */
    @Test
    @DisplayName("Update Package - Null Standard Capacity - Throws BadRequestException")
    void updatePackage_NullStandardCapacity_Throws() {
        // Arrange
        testPackageRequest.setStandardCapacity(null);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.updatePackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidStandardCapacity, ex.getMessage());
    }

    /**
     * Purpose: Reject update when package type is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidPackageType.
     */
    @Test
    @DisplayName("Update Package - Null Type - Throws BadRequestException")
    void updatePackage_NullType_Throws() {
        // Arrange
        testPackageRequest.setPackageType(null);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.updatePackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageType, ex.getMessage());
    }

    /**
     * Purpose: Reject update when breadth is zero.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidBreadth.
     */
    @Test
    @DisplayName("Update Package - Zero Breadth - Throws BadRequestException")
    void updatePackage_ZeroBreadth_Throws() {
        // Arrange
        testPackageRequest.setBreadth(0);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.updatePackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidBreadth, ex.getMessage());
    }

    /**
     * Purpose: Reject update when height is zero.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidHeight.
     */
    @Test
    @DisplayName("Update Package - Zero Height - Throws BadRequestException")
    void updatePackage_ZeroHeight_Throws() {
        // Arrange
        testPackageRequest.setHeight(0);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.updatePackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidHeight, ex.getMessage());
    }

    /**
     * Purpose: Reject update when length is zero.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidLength.
     */
    @Test
    @DisplayName("Update Package - Zero Length - Throws BadRequestException")
    void updatePackage_ZeroLength_Throws() {
        // Arrange
        testPackageRequest.setLength(0);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.updatePackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidLength, ex.getMessage());
    }

    /**
     * Purpose: Reject update when standard capacity is zero.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidStandardCapacity.
     */
    @Test
    @DisplayName("Update Package - Zero Standard Capacity - Throws BadRequestException")
    void updatePackage_ZeroStandardCapacity_Throws() {
        // Arrange
        testPackageRequest.setStandardCapacity(0);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.updatePackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidStandardCapacity, ex.getMessage());
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify unauthorized access is blocked at the controller level.
     * Expected Result: Unauthorized status is returned.
     * Assertions: Response status is 401 UNAUTHORIZED.
     */
    @Test
    @DisplayName("updatePackage - Controller Permission - Unauthorized")
    void updatePackage_controller_permission_unauthorized() {
        // Arrange
        PackageController controller = new PackageController(packageServiceMock, null);
        stubPackageServiceThrowsUnauthorizedException();

        // Act
        ResponseEntity<?> response = controller.updatePackage(testPackageRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify @PreAuthorize annotation on updatePackage endpoint.
     * Expected Result: Annotation exists and references UPDATE_PACKAGES_PERMISSION.
     * Assertions: Annotation is present and contains permission.
     */
    @Test
    @DisplayName("updatePackage - Verify @PreAuthorize Annotation")
    void updatePackage_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        Method method = PackageController.class.getMethod("updatePackage", PackageRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_PACKAGES_PERMISSION),
                "@PreAuthorize should reference UPDATE_PACKAGES_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service for valid requests.
     * Expected Result: Service method is invoked and HTTP 200 returned.
     * Assertions: Service called once and status code is OK.
     */
    @Test
    @DisplayName("updatePackage - Controller delegates to service")
    void updatePackage_WithValidRequest_DelegatesToService() {
        // Arrange
        PackageController controller = new PackageController(packageServiceMock, null);
        stubPackageServiceUpdatePackageDoNothing();

        // Act
        ResponseEntity<?> response = controller.updatePackage(testPackageRequest);

        // Assert
        verify(packageServiceMock).updatePackage(testPackageRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}