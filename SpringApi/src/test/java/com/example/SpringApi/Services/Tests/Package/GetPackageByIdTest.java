package com.example.SpringApi.Services.Tests.Package;

import com.example.SpringApi.Controllers.PackageController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.SpringApi.Models.ResponseModels.PackageResponseModel;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.ErrorMessages;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PackageService.getPackageById() method.
 * Verifies retrieval of package and associated inventory mappings.
 * * Test Count: 10 tests
 */
@DisplayName("Get Package By ID Tests")
class GetPackageByIdTest extends PackageServiceTestBase {

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
        testPackage.setPackageName("Premium Box");
        testPackage.setPackageType("Premium");
        testPackage.setLength(50);
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);

        PackageResponseModel result = packageService.getPackageById(TEST_PACKAGE_ID);

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
        testPackage.setIsDeleted(true);
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);

        PackageResponseModel result = packageService.getPackageById(TEST_PACKAGE_ID);

        assertNotNull(result);
        assertTrue(result.getIsDeleted());
    }



    /**
     * Purpose: Verify successful retrieval of package by valid ID.
     * Expected Result: PackageResponseModel is returned with correct data.
     * Assertions: Result is not null, ID and name match expected values.
     */
    @Test
    @DisplayName("Get Package By ID - Valid ID - Success")
    void getPackageById_Success() {
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);

        PackageResponseModel result = packageService.getPackageById(TEST_PACKAGE_ID);

        assertNotNull(result);
        assertEquals(TEST_PACKAGE_ID, result.getPackageId());
        assertEquals(DEFAULT_PACKAGE_NAME, result.getPackageName());
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
        when(packageRepository.findByPackageIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class, () -> packageService.getPackageById(Long.MAX_VALUE));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Reject retrieval attempts using the minimum possible long ID if not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidId error.
     */
    @Test
    @DisplayName("Get Package By ID - Min Long ID - Throws NotFoundException")
    void getPackageById_MinLongValue_ThrowsNotFoundException() {
        when(packageRepository.findByPackageIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class, () -> packageService.getPackageById(Long.MIN_VALUE));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Reject retrieval attempts for negative package IDs.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidId error.
     */
    @Test
    @DisplayName("Get Package By ID - Negative ID - Throws NotFoundException")
    void getPackageById_NegativeId_ThrowsNotFoundException() {
        when(packageRepository.findByPackageIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class, () -> packageService.getPackageById(-1L));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
    }

    /**
     * Purpose: Verify NotFoundException is thrown when a package is not found for the client.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidId error.
     */
    @Test
    @DisplayName("Get Package By ID - Not Found - Throws NotFoundException")
    void getPackageById_NotFound_ThrowsNotFoundException() {
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException exception = assertThrows(NotFoundException.class, () -> packageService.getPackageById(TEST_PACKAGE_ID));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, exception.getMessage());
    }

    /**
     * Purpose: Reject retrieval attempts for a zero ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Exception message matches InvalidId error.
     */
    @Test
    @DisplayName("Get Package By ID - Zero ID - Throws NotFoundException")
    void getPackageById_ZeroId_ThrowsNotFoundException() {
        when(packageRepository.findByPackageIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class, () -> packageService.getPackageById(0L));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("getPackageById - Verify @PreAuthorize Annotation")
    void getPackageById_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PackageController.class.getMethod("getPackageById", Long.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.VIEW_PACKAGES_PERMISSION),
            "@PreAuthorize should reference VIEW_PACKAGES_PERMISSION");
    }

    @Test
    @DisplayName("getPackageById - Controller delegates to service")
    void getPackageById_WithValidRequest_DelegatesToService() {
        PackageController controller = new PackageController(packageService, null);
        when(packageService.getPackageById(TEST_PACKAGE_ID)).thenReturn(new PackageResponseModel(testPackage));

        ResponseEntity<?> response = controller.getPackageById(TEST_PACKAGE_ID);

        verify(packageService).getPackageById(TEST_PACKAGE_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}