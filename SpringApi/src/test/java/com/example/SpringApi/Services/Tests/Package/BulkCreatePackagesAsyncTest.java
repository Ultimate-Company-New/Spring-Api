package com.example.SpringApi.Services.Tests.Package;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.RequestModels.PackageRequestModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for PackageService.bulkCreatePackagesAsync() method.
 */
@DisplayName("Bulk Create Packages Async Tests")
class BulkCreatePackagesAsyncTest extends PackageServiceTestBase {
    // Total Tests: 4

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify async bulk creation handles valid packages without throwing.
     * Expected Result: Method completes without exception and logs once.
     * Assertions: No exception and logDataWithContext called.
     */
    @Test
    @DisplayName("Bulk Create Packages Async - All Valid - Success")
    void bulkCreatePackagesAsync_AllValid_Success() {
        // Arrange
        List<PackageRequestModel> packages = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            PackageRequestModel req = createValidPackageRequest();
            req.setPackageName("PKG" + i);
            packages.add(req);
        }
        stubPackageRepositorySave(testPackage);
        stubUserLogServiceLogDataWithContextReturnsTrue();

        // Act & Assert
        assertDoesNotThrow(() -> packageService.bulkCreatePackagesAsync(
                packages, TEST_USER_ID, "testuser", TEST_CLIENT_ID));

        verify(userLogService).logDataWithContext(anyLong(), any(), any(), any(), any());
    }

    /*
     * Purpose: Reject async bulk creation when list is empty.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches CommonErrorMessages.ListCannotBeNullOrEmpty.
     */
    @Test
    @DisplayName("Bulk Create Packages Async - Empty List - Throws BadRequestException")
    void bulkCreatePackagesAsync_EmptyList_ThrowsBadRequestException() {
        // Arrange
        List<PackageRequestModel> packages = new ArrayList<>();

        // Act
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> packageService.bulkCreatePackagesAsync(packages, TEST_USER_ID, "testuser", TEST_CLIENT_ID));

        // Assert
        assertEquals(String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "Package"),
                exception.getMessage());
    }

    /*
     * Purpose: Reject async bulk creation when list is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches CommonErrorMessages.ListCannotBeNullOrEmpty.
     */
    @Test
    @DisplayName("Bulk Create Packages Async - Null List - Throws BadRequestException")
    void bulkCreatePackagesAsync_NullList_ThrowsBadRequestException() {
        // Arrange
        List<PackageRequestModel> packages = null;

        // Act
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> packageService.bulkCreatePackagesAsync(packages, TEST_USER_ID, "testuser", TEST_CLIENT_ID));

        // Assert
        assertEquals(String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "Package"),
                exception.getMessage());
    }

    /*
     **********************************************************************************************
     * PERMISSION TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify unauthorized access is blocked at the controller level.
     * Expected Result: Unauthorized status is returned.
     * Assertions: Response status is 401 UNAUTHORIZED.
     */
    @Test
    @DisplayName("bulkCreatePackagesAsync - Controller Permission - Unauthorized")
    void bulkCreatePackagesAsync_controller_permission_unauthorized() {
        // Arrange
        com.example.SpringApi.Controllers.PackageController controller =
                new com.example.SpringApi.Controllers.PackageController(packageServiceMock, concretePackageServiceMock);
        stubConcretePackageServiceThrowsUnauthorized();

        // Act
        ResponseEntity<?> response = controller.bulkCreatePackages(new ArrayList<>());

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}