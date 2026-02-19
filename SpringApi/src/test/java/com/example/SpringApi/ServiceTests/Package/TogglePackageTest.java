package com.example.SpringApi.ServiceTests.Package;

import com.example.SpringApi.Controllers.PackageController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.Authorizations;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for PackageService.togglePackage() method.
 */
@DisplayName("Toggle Package Tests")
class TogglePackageTest extends PackageServiceTestBase {

    // Total Tests: 15
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify toggle succeeds when package is already in deleted state.
     * Expected Result: Package is restored from deleted state (isDeleted = false).
     * Assertions: isDeleted changes from true to false after toggle.
     */
    @Test
    @DisplayName("Toggle Package - Already Deleted - Toggle Success")
    void togglePackage_AlreadyDeleted_Toggle_Success() {
        // Arrange
        testPackage.setIsDeleted(true);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);
        stubPackageRepositorySave(testPackage);

        // Act
        packageService.togglePackage(TEST_PACKAGE_ID);

        // Assert
        assertFalse(testPackage.getIsDeleted());
        verify(packageRepository, times(1)).save(testPackage);
    }

    /**
     * Purpose: Verify toggle operation logs correctly every time for audit trail.
     * Expected Result: Each toggle operation is logged independently.
     * Assertions: userLogService.logData is called exactly once per toggle.
     */
    @Test
    @DisplayName("Toggle Package - Logging for Each Toggle - Success")
    void togglePackage_LoggingForEachToggle_Success() {
        // Arrange
        testPackage.setIsDeleted(false);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);
        stubPackageRepositorySave(testPackage);

        // Act
        packageService.togglePackage(TEST_PACKAGE_ID);
        packageService.togglePackage(TEST_PACKAGE_ID);
        packageService.togglePackage(TEST_PACKAGE_ID);

        // Assert
        verify(userLogService, times(3)).logData(eq(TEST_USER_ID), contains("Successfully toggled"), anyString());
    }

    /**
     * Purpose: Verify state persistence after multiple consecutive toggles.
     * Expected Result: State alternates correctly: Active → Deleted → Active → Deleted.
     * Assertions: After 3 toggles, final state is deleted (true).
     */
    @Test
    @DisplayName("Toggle Package - Multiple Consecutive Toggles - State Correct")
    void togglePackage_MultipleConsecutiveToggles_Success() {
        // Arrange
        testPackage.setIsDeleted(false);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);
        stubPackageRepositorySave(testPackage);

        // Act
        packageService.togglePackage(TEST_PACKAGE_ID);
        packageService.togglePackage(TEST_PACKAGE_ID);
        packageService.togglePackage(TEST_PACKAGE_ID);

        // Assert
        assertTrue(testPackage.getIsDeleted());
        verify(packageRepository, times(3)).save(testPackage);
    }

    /**
     * Purpose: Verify multiple toggles correctly persist state alternating between true and false.
     * Expected Result: State transition: Active -> Deleted -> Active.
     * Assertions: isDeleted is false after the second toggle.
     */
    @Test
    @DisplayName("Toggle Package - Multiple Toggles - Success")
    void togglePackage_MultipleToggles_Success() {
        // Arrange
        testPackage.setIsDeleted(false);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);
        stubPackageRepositorySave(testPackage);

        // Act
        packageService.togglePackage(TEST_PACKAGE_ID);
        packageService.togglePackage(TEST_PACKAGE_ID);

        // Assert
        assertFalse(testPackage.getIsDeleted());
        verify(packageRepository, times(2)).save(testPackage);
    }

    /**
     * Purpose: Verify rapid successive toggles maintain correct state integrity.
     * Expected Result: After 10 toggles, state matches expected toggle count parity.
     * Assertions: isDeleted state is correct based on toggle count.
     */
    @Test
    @DisplayName("Toggle Package - Rapid Successive Toggles - State Integrity")
    void togglePackage_RapidSuccessiveToggles_StateIntegrity() {
        // Arrange
        testPackage.setIsDeleted(false);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);
        stubPackageRepositorySaveReturnsInput();

        // Act
        for (int i = 0; i < 10; i++) {
            packageService.togglePackage(TEST_PACKAGE_ID);
        }

        // Assert
        assertFalse(testPackage.getIsDeleted());
        verify(packageRepository, times(10)).save(testPackage);
    }

    /**
     * Purpose: Verify toggle from deleted status to active status works correctly.
     * Expected Result: isDeleted changes from true to false.
     * Assertions: result is active (false).
     */
    @Test
    @DisplayName("Toggle Package - Restore from Deleted - Success")
    void togglePackage_RestoreFromDeleted_Success() {
        // Arrange
        testPackage.setIsDeleted(true);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);
        stubPackageRepositorySave(testPackage);

        // Act
        packageService.togglePackage(TEST_PACKAGE_ID);

        // Assert
        assertFalse(testPackage.getIsDeleted());
    }

    /**
     * Purpose: Verify successful toggle of package status and logging.
     * Expected Result: Package isDeleted flag is toggled and logged.
     * Assertions: Repository save and userLogService.logData are called.
     */
    @Test
    @DisplayName("Toggle Package - Success")
    void togglePackage_Success_Success() {
        // Arrange
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);
        stubPackageRepositorySave(testPackage);

        // Act
        assertDoesNotThrow(() -> packageService.togglePackage(TEST_PACKAGE_ID));

        // Assert
        verify(packageRepository, times(1)).save(testPackage);
        verify(userLogService, times(1)).logData(eq(TEST_USER_ID), contains("Successfully toggled"), anyString());
    }

    /**
     * Purpose: Verify toggle with very large package ID value works correctly.
     * Expected Result: Package with large ID is toggled successfully.
     * Assertions: isDeleted status is toggled and saved.
     */
    @Test
    @DisplayName("Toggle Package - Very High ID Value - Success")
    void togglePackage_VeryHighIdValue_Success() {
        // Arrange
        long highId = 9999999999L;
        testPackage.setIsDeleted(false);
        stubPackageRepositoryFindByPackageIdAndClientId(highId, TEST_CLIENT_ID, testPackage);
        stubPackageRepositorySave(testPackage);

        // Act
        packageService.togglePackage(highId);

        // Assert
        assertTrue(testPackage.getIsDeleted());
        verify(packageRepository, times(1)).save(testPackage);
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
        // Arrange
        stubPackageRepositoryFindByPackageIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID, null);

        // Act
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> packageService.togglePackage(Long.MAX_VALUE));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.INVALID_ID, exception.getMessage());
    }

    /**
     * Purpose: Reject toggle attempts for negative IDs.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidId error.
     */
    @Test
    @DisplayName("Toggle Package - Negative ID - Throws NotFoundException")
    void togglePackage_NegativeId_ThrowsNotFoundException() {
        // Arrange
        stubPackageRepositoryFindByPackageIdAndClientId(-1L, TEST_CLIENT_ID, null);

        // Act
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> packageService.togglePackage(-1L));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.INVALID_ID, exception.getMessage());
    }

    /**
     * Purpose: Reject toggle attempts when the package ID is not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidId error.
     */
    @Test
    @DisplayName("Toggle Package - Not Found - Throws NotFoundException")
    void togglePackage_NotFound_ThrowsNotFoundException() {
        // Arrange
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, null);

        // Act
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> packageService.togglePackage(TEST_PACKAGE_ID));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.INVALID_ID, exception.getMessage());
    }

    /**
     * Purpose: Reject toggle attempts for a zero ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidId error.
     */
    @Test
    @DisplayName("Toggle Package - Zero ID - Throws NotFoundException")
    void togglePackage_ZeroId_ThrowsNotFoundException() {
        // Arrange
        stubPackageRepositoryFindByPackageIdAndClientId(0L, TEST_CLIENT_ID, null);

        // Act
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> packageService.togglePackage(0L));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.INVALID_ID, exception.getMessage());
    }

    /*
     **********************************************************************************************
     * PERMISSION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify unauthorized access is blocked at controller level.
     * Expected Result: Unauthorized status.
     * Assertions: status is 401.
     */
    @Test
    @DisplayName("togglePackage - Controller Permission - Unauthorized")
    void togglePackage_controller_permission_unauthorized() {
        // Arrange
        PackageController controller = new PackageController(packageServiceMock, null);
        stubPackageServiceThrowsUnauthorizedException();

        // Act
        ResponseEntity<?> response = controller.togglePackage(TEST_PACKAGE_ID);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify @PreAuthorize annotation exists and includes permission.
     * Expected Result: Annotation present and contains correct permission.
     * Assertions: annotation not null and contains permission.
     */
    @Test
    @DisplayName("togglePackage - Verify @PreAuthorize Annotation")
    void togglePackage_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        Method method = PackageController.class.getMethod("togglePackage", long.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.TOGGLE_PACKAGES_PERMISSION),
                "@PreAuthorize should reference TOGGLE_PACKAGES_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates toggle request to service.
     * Expected Result: Service called and OK returned.
     * Assertions: status is OK and service invoked.
     */
    @Test
    @DisplayName("togglePackage - Controller delegates to service")
    void togglePackage_WithValidRequest_DelegatesToService() {
        // Arrange
        PackageController controller = new PackageController(packageServiceMock, null);
        stubPackageServiceTogglePackageDoNothing();

        // Act
        ResponseEntity<?> response = controller.togglePackage(TEST_PACKAGE_ID);

        // Assert
        verify(packageServiceMock).togglePackage(TEST_PACKAGE_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}