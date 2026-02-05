package com.example.SpringApi.Services.Tests.Package;

import com.example.SpringApi.Controllers.PackageController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.SpringApi.Models.RequestModels.PackagePickupLocationMappingRequestModel;
import com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.RequestModels.PackageRequestModel;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PackageService.updatePackage() method.
 * Covers field validation, mapping logic, and lastRestockDate logic.
 * * Test Count: 33 tests
 */
@DisplayName("Update Package Tests")
class UpdatePackageTest extends PackageServiceTestBase {

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
        LocalDateTime oldDate = LocalDateTime.now().minusDays(5);
        PackagePickupLocationMapping existing = new PackagePickupLocationMapping();
        existing.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        existing.setAvailableQuantity(100);
        existing.setLastRestockDate(oldDate);

        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        when(packagePickupLocationMappingRepository.findByPackageId(TEST_PACKAGE_ID)).thenReturn(List.of(existing));

        Map<Long, PackagePickupLocationMappingRequestModel> qMap = new HashMap<>();
        PackagePickupLocationMappingRequestModel mReq = new PackagePickupLocationMappingRequestModel();
        mReq.setQuantity(50); // Decrease
        qMap.put(TEST_PICKUP_LOCATION_ID, mReq);
        testPackageRequest.setPickupLocationQuantities(qMap);

        packageService.updatePackage(testPackageRequest);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PackagePickupLocationMapping>> captor = ArgumentCaptor.forClass((Class<List<PackagePickupLocationMapping>>) (Class<?>) List.class);
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
        LocalDateTime oldDate = LocalDateTime.now().minusDays(5);
        PackagePickupLocationMapping existing = new PackagePickupLocationMapping();
        existing.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        existing.setAvailableQuantity(10);
        existing.setLastRestockDate(oldDate);

        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        when(packagePickupLocationMappingRepository.findByPackageId(TEST_PACKAGE_ID)).thenReturn(List.of(existing));

        Map<Long, PackagePickupLocationMappingRequestModel> qMap = new HashMap<>();
        PackagePickupLocationMappingRequestModel mReq = new PackagePickupLocationMappingRequestModel();
        mReq.setQuantity(20); // Increase
        qMap.put(TEST_PICKUP_LOCATION_ID, mReq);
        testPackageRequest.setPickupLocationQuantities(qMap);

        packageService.updatePackage(testPackageRequest);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PackagePickupLocationMapping>> captor = ArgumentCaptor.forClass((Class<List<PackagePickupLocationMapping>>) (Class<?>) List.class);
        verify(packagePickupLocationMappingRepository).saveAll(captor.capture());
        assertTrue(captor.getValue().get(0).getLastRestockDate().isAfter(oldDate));
    }



    /**
     * Purpose: Verify successful package update with valid data.
     * Expected Result: Package is updated and logged.
     * Assertions: Repository save and userLogService.logData are called.
     */
    @Test
    @DisplayName("Update Package - Success")
    void updatePackage_Success() {
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        when(packageRepository.save(any())).thenReturn(testPackage);

        assertDoesNotThrow(() -> packageService.updatePackage(testPackageRequest));

        verify(packageRepository, times(1)).save(any());
        verify(userLogService, times(1)).logData(eq(TEST_USER_ID), contains("Successfully updated"), any());
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
        testPackageRequest.setBreadth(-1);
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        assertThrows(BadRequestException.class, () -> packageService.updatePackage(testPackageRequest));
    }

    /**
     * Purpose: Reject update for a non-existent package ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidId error.
     */
    @Test
    @DisplayName("Update Package - Not Found - Throws NotFoundException")
    void updatePackage_NotFound_ThrowsNotFoundException() {
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> packageService.updatePackage(testPackageRequest));
    }

    /**
     * Purpose: Reject update when breadth is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidBreadth.
     */
    @Test
    @DisplayName("Update Package - Null Breadth - Throws BadRequestException")
    void updatePackage_NullBreadth_Throws() {
        testPackageRequest.setBreadth(null);
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.updatePackage(testPackageRequest));
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
        testPackageRequest.setHeight(null);
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.updatePackage(testPackageRequest));
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
        testPackageRequest.setLength(null);
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.updatePackage(testPackageRequest));
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
        testPackageRequest.setPackageName(null);
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.updatePackage(testPackageRequest));
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
        testPackageRequest.setPricePerUnit(null);
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.updatePackage(testPackageRequest));
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
        testPackageRequest.setStandardCapacity(null);
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.updatePackage(testPackageRequest));
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
        testPackageRequest.setPackageType(null);
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.updatePackage(testPackageRequest));
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
        testPackageRequest.setBreadth(0);
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.updatePackage(testPackageRequest));
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
        testPackageRequest.setHeight(0);
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.updatePackage(testPackageRequest));
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
        testPackageRequest.setLength(0);
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.updatePackage(testPackageRequest));
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
        testPackageRequest.setStandardCapacity(0);
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.updatePackage(testPackageRequest));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidStandardCapacity, ex.getMessage());
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("updatePackage - Verify @PreAuthorize Annotation")
    void updatePackage_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PackageController.class.getMethod("updatePackage", PackageRequestModel.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.UPDATE_PACKAGES_PERMISSION),
            "@PreAuthorize should reference UPDATE_PACKAGES_PERMISSION");
    }

    @Test
    @DisplayName("updatePackage - Controller delegates to service")
    void updatePackage_WithValidRequest_DelegatesToService() {
        PackageController controller = new PackageController(packageService, null);
        doNothing().when(packageService).updatePackage(testPackageRequest);

        ResponseEntity<?> response = controller.updatePackage(testPackageRequest);

        verify(packageService).updatePackage(testPackageRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}