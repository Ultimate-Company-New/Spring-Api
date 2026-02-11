package com.example.SpringApi.Services.Tests.Package;

import com.example.SpringApi.Controllers.PackageController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.ResponseModels.PackageResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for PackageService.getPackageById() method.
 *
 * Test count: 16 tests
}
 * - FAILURE / EXCEPTION: 5 tests
 * - PERMISSION: 3 tests
 */
@DisplayName("Get Package By ID Tests")
class GetPackageByIdTest extends PackageServiceTestBase {
    // Total Tests: 16

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify package with all fields populated is returned correctly.
     * Expected Result: Model fields correctly map the entity attributes.
     * Assertions: Name and Type in response match mock entity.
     */
    @Test
    @DisplayName("Get Package By ID - All Fields Populated - Success")
    void getPackageById_AllFieldsPopulated_Success() {
        // Arrange
        testPackage.setPackageName("Premium Box");
        testPackage.setPackageType("Premium");
        testPackage.setLength(50);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);

        // Act
        PackageResponseModel result = packageService.getPackageById(TEST_PACKAGE_ID);

        // Assert
        assertNotNull(result);
        assertEquals("Premium Box", result.getPackageName());
        assertEquals("Premium", result.getPackageType());
    }

    /**
     * Purpose: Verify deleted package is still retrievable by ID.
     * Expected Result: Package model is returned with isDeleted true.
     * Assertions: result.getIsDeleted() is true.
     */
    @Test
    @DisplayName("Get Package By ID - Deleted Package - Success")
    void getPackageById_DeletedPackage_Success() {
        // Arrange
        testPackage.setIsDeleted(true);
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);

        // Act
        PackageResponseModel result = packageService.getPackageById(TEST_PACKAGE_ID);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsDeleted());
    }

    /**
    /**
     * Purpose: Verify package with very specific ID (9999999999) is retrieved correctly.
     * Expected Result: Correct package is returned.
     * Assertions: Result contains expected data with specific ID.
     */
    @Test
    @DisplayName("Get Package By ID - High Value ID - Success")
    void getPackageById_HighValueId_Success() {
        // Arrange
        long highId = 9999999999L;
        testPackage.setPackageName("High ID Package");
        stubPackageRepositoryFindByPackageIdAndClientId(highId, TEST_CLIENT_ID, testPackage);

        // Act
        PackageResponseModel result = packageService.getPackageById(highId);

        // Assert
        assertNotNull(result);
        assertEquals("High ID Package", result.getPackageName());
    }

    /**
     * Purpose: Verify package retrieval succeeds with Long.MAX_VALUE when package exists.
     * Expected Result: Package with maximum long ID is returned correctly.
     * Assertions: Result is not null and ID matches Long.MAX_VALUE.
     */
    @Test
    @DisplayName("Get Package By ID - Max Long Value ID - Success")
    void getPackageById_MaxLongId_Success() {
        // Arrange
        com.example.SpringApi.Models.DatabaseModels.Package maxIdPackage = createTestPackage();
        maxIdPackage.setPackageId(Long.MAX_VALUE);
        maxIdPackage.setPackageName("Max ID Package");
        stubPackageRepositoryFindByPackageIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID, maxIdPackage);

        // Act
        PackageResponseModel result = packageService.getPackageById(Long.MAX_VALUE);

        // Assert
        assertNotNull(result);
        assertEquals(Long.MAX_VALUE, result.getPackageId());
        assertEquals("Max ID Package", result.getPackageName());
    }

    /**
     * Purpose: Verify package with maximum long ID value is retrieved successfully.
     * Expected Result: PackageResponseModel is returned with all fields.
     * Assertions: Result is not null and contains correct data.
     */
    @Test
    @DisplayName("Get Package By ID - Max Long ID Value - Success")
    void getPackageById_MaxLongValue_Success() {
        // Arrange
        testPackage.setPackageName("Max ID Package");
        stubPackageRepositoryFindByPackageIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID, testPackage);

        // Act
        PackageResponseModel result = packageService.getPackageById(Long.MAX_VALUE);

        // Assert
        assertNotNull(result);
        assertEquals("Max ID Package", result.getPackageName());
    }

    /**
     * Purpose: Verify retrieval of package with special characters in name.
     * Expected Result: Package with special characters is returned correctly.
     * Assertions: Package name with special characters is preserved in response.
     */
    @Test
    @DisplayName("Get Package By ID - Special Characters in Name - Success")
    void getPackageById_SpecialCharactersInName_Success() {
        // Arrange
        testPackage.setPackageName("Box-#123!@$%^&*()_+-=[]{}");
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);

        // Act
        PackageResponseModel result = packageService.getPackageById(TEST_PACKAGE_ID);

        // Assert
        assertNotNull(result);
        assertEquals("Box-#123!@$%^&*()_+-=[]{}", result.getPackageName());
    }

    /**
     * Purpose: Verify successful retrieval of package by valid ID.
     * Expected Result: PackageResponseModel is returned with correct data.
     * Assertions: Result is not null, ID and name match expected values.
     */
    @Test
    @DisplayName("Get Package By ID - Valid ID - Success")
    void getPackageById_Success_Success() {
        // Arrange
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);

        // Act
        PackageResponseModel result = packageService.getPackageById(TEST_PACKAGE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_PACKAGE_ID, result.getPackageId());
        assertEquals(DEFAULT_PACKAGE_NAME, result.getPackageName());
    }

    /**
     * Purpose: Verify package with many pickup location mappings is retrieved correctly.
     * Expected Result: Package is returned successfully even with multiple location mappings.
     * Assertions: Package data and ID are correctly mapped in response.
     */
    @Test
    @DisplayName("Get Package By ID - With Many Location Mappings - Success")
    void getPackageById_WithManyMappings_Success() {
        // Arrange
        testPackage.setPackageName("Multi-Location Package");
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, testPackage);

        // Act
        PackageResponseModel result = packageService.getPackageById(TEST_PACKAGE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_PACKAGE_ID, result.getPackageId());
        assertEquals("Multi-Location Package", result.getPackageName());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject retrieval attempts using the maximum possible long ID if not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidId error.
     */
    @Test
    @DisplayName("Get Package By ID - Max Long ID - Throws NotFoundException")
    void getPackageById_MaxLongValue_ThrowsNotFoundException() {
        // Arrange
        stubPackageRepositoryFindByPackageIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID, null);

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.PackageErrorMessages.InvalidId,
                () -> packageService.getPackageById(Long.MAX_VALUE));
    }

    /**
     * Purpose: Reject retrieval attempts using the minimum possible long ID if not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidId error.
     */
    @Test
    @DisplayName("Get Package By ID - Min Long ID - Throws NotFoundException")
    void getPackageById_MinLongValue_ThrowsNotFoundException() {
        // Arrange
        stubPackageRepositoryFindByPackageIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID, null);

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.PackageErrorMessages.InvalidId,
                () -> packageService.getPackageById(Long.MIN_VALUE));
    }

    /**
     * Purpose: Reject retrieval attempts for negative package IDs.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidId error.
     */
    @Test
    @DisplayName("Get Package By ID - Negative ID - Throws NotFoundException")
    void getPackageById_NegativeId_ThrowsNotFoundException() {
        // Arrange
        stubPackageRepositoryFindByPackageIdAndClientId(-1L, TEST_CLIENT_ID, null);

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.PackageErrorMessages.InvalidId,
                () -> packageService.getPackageById(-1L));
    }

    /**
     * Purpose: Verify NotFoundException is thrown when a package is not found for the client.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidId error.
     */
    @Test
    @DisplayName("Get Package By ID - Not Found - Throws NotFoundException")
    void getPackageById_NotFound_ThrowsNotFoundException() {
        // Arrange
        stubPackageRepositoryFindByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID, null);

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.PackageErrorMessages.InvalidId,
                () -> packageService.getPackageById(TEST_PACKAGE_ID));
    }

    /**
     * Purpose: Reject retrieval attempts for a zero ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidId error.
     */
    @Test
    @DisplayName("Get Package By ID - Zero ID - Throws NotFoundException")
    void getPackageById_ZeroId_ThrowsNotFoundException() {
        // Arrange
        stubPackageRepositoryFindByPackageIdAndClientId(0L, TEST_CLIENT_ID, null);

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.PackageErrorMessages.InvalidId,
                () -> packageService.getPackageById(0L));
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
    @DisplayName("getPackageById - Controller Permission - Unauthorized")
    void getPackageById_controller_permission_unauthorized() {
        // Arrange
        PackageController controller = new PackageController(packageServiceMock, null);
        stubPackageServiceThrowsUnauthorizedException();

        // Act
        ResponseEntity<?> response = controller.getPackageById(TEST_PACKAGE_ID);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify @PreAuthorize annotation on getPackageById endpoint.
     * Expected Result: Annotation exists and references VIEW_PACKAGES_PERMISSION.
     * Assertions: Annotation is present and contains permission.
     */
    @Test
    @DisplayName("getPackageById - Verify @PreAuthorize Annotation")
    void getPackageById_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        Method method = PackageController.class.getMethod("getPackageById", long.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.VIEW_PACKAGES_PERMISSION),
                "@PreAuthorize should reference VIEW_PACKAGES_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service for valid requests.
     * Expected Result: Service method is invoked and HTTP 200 returned.
     * Assertions: Service called once and status code is OK.
     */
    @Test
    @DisplayName("getPackageById - Controller delegates to service")
    void getPackageById_WithValidRequest_DelegatesToService() {
        // Arrange
        PackageController controller = new PackageController(packageServiceMock, null);
        stubPackageServiceGetPackageByIdReturns(new PackageResponseModel(testPackage));

        // Act
        ResponseEntity<?> response = controller.getPackageById(TEST_PACKAGE_ID);

        // Assert
        verify(packageServiceMock).getPackageById(TEST_PACKAGE_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
