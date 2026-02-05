package com.example.SpringApi.Services.Tests.Package;

import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.ErrorMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PackageService.togglePackage() method.
 * * Test Count: 9 tests
 */
@DisplayName("Toggle Package Tests")
class TogglePackageTest extends PackageServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify multiple toggles correctly persist state alternating between true and false.
     * Expected Result: State transition: Active -> Deleted -> Active.
     * Assertions: isDeleted is false after the second toggle.
     */
    @Test
    @DisplayName("Toggle Package - Multiple Toggles - Success")
    void togglePackage_MultipleToggles_Success() {
        testPackage.setIsDeleted(false);
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        when(packageRepository.save(any())).thenReturn(testPackage);

        packageService.togglePackage(TEST_PACKAGE_ID);
        assertTrue(testPackage.getIsDeleted());

        packageService.togglePackage(TEST_PACKAGE_ID);
        assertFalse(testPackage.getIsDeleted());
        verify(packageRepository, times(2)).save(testPackage);
    }

    /**
     * Purpose: Verify permission check is performed for DELETE_PACKAGE permission.
     * Expected Result: Authorization service is called to check permissions.
     * Assertions: authorization.hasAuthority() is called with correct permission.
     */
    @Test
    @DisplayName("Toggle Package - Permission Check - Success Verifies Authorization")
    void togglePackage_PermissionCheck_SuccessVerifiesAuthorization() {
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        when(packageRepository.save(any())).thenReturn(testPackage);
        lenient().when(authorization.hasAuthority(Authorizations.TOGGLE_PACKAGES_PERMISSION)).thenReturn(true);

        packageService.togglePackage(TEST_PACKAGE_ID);

        verify(authorization, atLeastOnce()).hasAuthority(Authorizations.TOGGLE_PACKAGES_PERMISSION);
    }

    /**
     * Purpose: Verify toggle from deleted status to active status works correctly.
     * Expected Result: isDeleted changes from true to false.
     * Assertions: result is active (false).
     */
    @Test
    @DisplayName("Toggle Package - Restore from Deleted - Success")
    void togglePackage_RestoreFromDeleted_Success() {
        testPackage.setIsDeleted(true);
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        when(packageRepository.save(any())).thenReturn(testPackage);

        packageService.togglePackage(TEST_PACKAGE_ID);

        assertFalse(testPackage.getIsDeleted());
    }

    /**
     * Purpose: Verify successful toggle of package status and logging.
     * Expected Result: Package isDeleted flag is toggled and logged.
     * Assertions: Repository save and userLogService.logData are called.
     */
    @Test
    @DisplayName("Toggle Package - Success")
    void togglePackage_Success() {
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        when(packageRepository.save(any())).thenReturn(testPackage);

        assertDoesNotThrow(() -> packageService.togglePackage(TEST_PACKAGE_ID));

        verify(packageRepository, times(1)).save(testPackage);
        verify(userLogService, times(1)).logData(eq(TEST_USER_ID), contains("Successfully toggled"), any());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject toggle attempts for the maximum possible long ID if not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidId error.
     */
    @Test
    @DisplayName("Toggle Package - Max Long ID - Throws NotFoundException")
    void togglePackage_MaxLongId_ThrowsNotFoundException() {
        when(packageRepository.findByPackageIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> packageService.togglePackage(Long.MAX_VALUE));
    }

    /**
     * Purpose: Reject toggle attempts for negative IDs.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidId error.
     */
    @Test
    @DisplayName("Toggle Package - Negative ID - Throws NotFoundException")
    void togglePackage_NegativeId_ThrowsNotFoundException() {
        when(packageRepository.findByPackageIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> packageService.togglePackage(-1L));
    }

    /**
     * Purpose: Reject toggle attempts when the package ID is not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidId error.
     */
    @Test
    @DisplayName("Toggle Package - Not Found - Throws NotFoundException")
    void togglePackage_NotFound_ThrowsNotFoundException() {
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException exception = assertThrows(NotFoundException.class, () -> packageService.togglePackage(TEST_PACKAGE_ID));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, exception.getMessage());
    }

    /**
     * Purpose: Reject toggle attempts for a zero ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidId error.
     */
    @Test
    @DisplayName("Toggle Package - Zero ID - Throws NotFoundException")
    void togglePackage_ZeroId_ThrowsNotFoundException() {
        when(packageRepository.findByPackageIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> packageService.togglePackage(0L));
    }
}