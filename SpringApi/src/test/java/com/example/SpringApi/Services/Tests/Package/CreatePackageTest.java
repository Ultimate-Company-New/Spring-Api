package com.example.SpringApi.Services.Tests.Package;

import com.example.SpringApi.Controllers.PackageController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.RequestModels.PackageRequestModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.math.BigDecimal;

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
 * Unit tests for PackageService.createPackage() method.
 * Exhaustive validation for every field and path in the creation logic.
 */
@DisplayName("Create Package Tests")
class CreatePackageTest extends PackageServiceTestBase {

    // Total Tests: 34
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify creation succeeds with the minimum valid integer values (1).
     * Expected Result: Package is saved successfully.
     * Assertions: assertDoesNotThrow verifies success.
     */
    @Test
    @DisplayName("Create Package - Boundary Values (1) - Success")
    void createPackage_BoundaryValues_Success() {
        // Arrange
        testPackageRequest.setLength(1);
        testPackageRequest.setBreadth(1);
        testPackageRequest.setHeight(1);
        testPackageRequest.setStandardCapacity(1);
        stubPackageRepositorySave(testPackage);

        // Act & Assert
        assertDoesNotThrow(() -> packageService.createPackage(testPackageRequest));
    }

    /**
     * Purpose: Verify creation succeeds with large standard capacity values.
     * Expected Result: Package with large capacity is saved successfully.
     * Assertions: assertDoesNotThrow verifies success, repository save is called once.
     */
    @Test
    @DisplayName("Create Package - Large Standard Capacity - Success")
    void createPackage_LargeStandardCapacity_Success() {
        // Arrange
        testPackageRequest.setStandardCapacity(1000000);
        stubPackageRepositorySave(testPackage);

        // Act
        assertDoesNotThrow(() -> packageService.createPackage(testPackageRequest));

        // Assert
        verify(packageRepository, times(1)).save(any());
    }

    /**
     * Purpose: Verify creation succeeds with Integer.MAX_VALUE for all dimension fields.
     * Expected Result: Package is created successfully with extreme dimensions.
     * Assertions: assertDoesNotThrow verifies no exception is raised.
     */
    @Test
    @DisplayName("Create Package - Max Integer Dimensions - Success")
    void createPackage_MaxIntegerDimensions_Success() {
        // Arrange
        testPackageRequest.setLength(Integer.MAX_VALUE);
        testPackageRequest.setBreadth(Integer.MAX_VALUE);
        testPackageRequest.setHeight(Integer.MAX_VALUE);
        testPackageRequest.setStandardCapacity(Integer.MAX_VALUE);
        stubPackageRepositorySave(testPackage);

        // Act
        assertDoesNotThrow(() -> packageService.createPackage(testPackageRequest));

        // Assert
        verify(packageRepository, times(1)).save(any());
    }

    /**
     * Purpose: Verify creation succeeds with all BigDecimal fields at maximum precision.
     * Expected Result: Package is created with full precision monetary values.
     * Assertions: assertDoesNotThrow verifies success and repository save is called.
     */
    @Test
    @DisplayName("Create Package - Max Precision BigDecimal Values - Success")
    void createPackage_MaxPrecisionBigDecimals_Success() {
        // Arrange
        testPackageRequest.setPricePerUnit(new BigDecimal("99999.99999999"));
        testPackageRequest.setMaxWeight(new BigDecimal("99999.99999999"));
        stubPackageRepositorySave(testPackage);

        // Act
        assertDoesNotThrow(() -> packageService.createPackage(testPackageRequest));

        // Assert
        verify(packageRepository, times(1)).save(any());
    }

    /**
     * Purpose: Verify successful package creation with a valid request.
     * Expected Result: Package is created and success is logged.
     * Assertions: Repository save and userLogService.logData are called.
     */
    @Test
    @DisplayName("Create Package - Success - Valid request")
    void createPackage_Success_Success() {
        // Arrange
        stubPackageRepositorySave(testPackage);

        // Act
        assertDoesNotThrow(() -> packageService.createPackage(testPackageRequest));

        // Assert
        verify(packageRepository, times(1)).save(any());
        verify(userLogService, times(1)).logData(eq(TEST_USER_ID),
                org.mockito.ArgumentMatchers.contains("Successfully inserted package"), anyString());
    }

    /**
     * Purpose: Verify creation succeeds with package name containing special Unicode characters.
     * Expected Result: Package is saved successfully with Unicode in name.
     * Assertions: assertDoesNotThrow verifies success, repository save is called.
     */
    @Test
    @DisplayName("Create Package - Unicode Characters in Name - Success")
    void createPackage_UnicodeCharactersInName_Success() {
        // Arrange
        testPackageRequest.setPackageName("Package™ © ® 日本語 العربية");
        stubPackageRepositorySave(testPackage);

        // Act
        assertDoesNotThrow(() -> packageService.createPackage(testPackageRequest));

        // Assert
        verify(packageRepository, times(1)).save(any());
    }

    /**
     * Purpose: Verify creation succeeds with very large BigDecimal price per unit.
     * Expected Result: Package is saved with maximum precision BigDecimal price.
     * Assertions: assertDoesNotThrow and verify save was called.
     */
    @Test
    @DisplayName("Create Package - Very Large Price Per Unit - Success")
    void createPackage_VeryLargePricePerUnit_Success() {
        // Arrange
        testPackageRequest.setPricePerUnit(new BigDecimal("999999999999.99"));
        testPackageRequest.setMaxWeight(new BigDecimal("999999999999.99"));
        stubPackageRepositorySave(testPackage);

        // Act
        assertDoesNotThrow(() -> packageService.createPackage(testPackageRequest));

        // Assert
        verify(packageRepository, times(1)).save(any());
    }

    /**
     * Purpose: Verify zero max weight is considered a valid entry.
     * Expected Result: Package is created without exception.
     * Assertions: assertDoesNotThrow verifies success.
     */
    @Test
    @DisplayName("Create Package - Zero Max Weight - Success")
    void createPackage_ZeroMaxWeight_Success() {
        // Arrange
        testPackageRequest.setMaxWeight(BigDecimal.ZERO);
        stubPackageRepositorySave(testPackage);

        // Act & Assert
        assertDoesNotThrow(() -> packageService.createPackage(testPackageRequest));
    }

    /**
     * Purpose: Verify zero price per unit is considered a valid entry.
     * Expected Result: Package is created without exception.
     * Assertions: assertDoesNotThrow verifies success.
     */
    @Test
    @DisplayName("Create Package - Zero Price - Success")
    void createPackage_ZeroPrice_Success() {
        // Arrange
        testPackageRequest.setPricePerUnit(BigDecimal.ZERO);
        stubPackageRepositorySave(testPackage);

        // Act & Assert
        assertDoesNotThrow(() -> packageService.createPackage(testPackageRequest));
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject creation when breadth is negative.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidBreadth error.
     */
    @Test
    @DisplayName("Create Package - Negative Breadth - Throws BadRequestException")
    void createPackage_NegativeBreadth_Throws() {
        // Arrange
        testPackageRequest.setBreadth(-1);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidBreadth, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when height is negative.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidHeight error.
     */
    @Test
    @DisplayName("Create Package - Negative Height - Throws BadRequestException")
    void createPackage_NegativeHeight_Throws() {
        // Arrange
        testPackageRequest.setHeight(-1);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidHeight, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when length is negative.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidLength error.
     */
    @Test
    @DisplayName("Create Package - Negative Length - Throws BadRequestException")
    void createPackage_NegativeLength_Throws() {
        // Arrange
        testPackageRequest.setLength(-5);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidLength, ex.getMessage());
    }

    /**
     * Purpose: Verify creation fails with negative max weight (duplicate validation).
     * Expected Result: BadRequestException is thrown for negative weight.
     * Assertions: Exception message matches InvalidMaxWeight error.
     */
    @Test
    @DisplayName("Create Package - Negative Max Weight (Duplicate) - Throws BadRequestException")
    void createPackage_NegativeMaxWeight_Duplicate_ThrowsBadRequestException() {
        // Arrange
        testPackageRequest.setMaxWeight(new BigDecimal("-50.00"));

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidMaxWeight, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when max weight is negative.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidMaxWeight error.
     */
    @Test
    @DisplayName("Create Package - Negative Max Weight - Throws BadRequestException")
    void createPackage_NegativeMaxWeight_Throws() {
        // Arrange
        testPackageRequest.setMaxWeight(BigDecimal.valueOf(-1.0));

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidMaxWeight, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when price per unit is negative.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidPricePerUnit error.
     */
    @Test
    @DisplayName("Create Package - Negative Price Per Unit - Throws BadRequestException")
    void createPackage_NegativePricePerUnit_Throws() {
        // Arrange
        testPackageRequest.setPricePerUnit(BigDecimal.valueOf(-1.0));

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidPricePerUnit, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when standard capacity is negative.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidStandardCapacity error.
     */
    @Test
    @DisplayName("Create Package - Negative Standard Capacity - Throws BadRequestException")
    void createPackage_NegativeStandardCapacity_Throws() {
        // Arrange
        testPackageRequest.setStandardCapacity(-5);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidStandardCapacity, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when breadth is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidBreadth error.
     */
    @Test
    @DisplayName("Create Package - Null Breadth - Throws BadRequestException")
    void createPackage_NullBreadth_Throws() {
        // Arrange
        testPackageRequest.setBreadth(null);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidBreadth, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when height is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidHeight error.
     */
    @Test
    @DisplayName("Create Package - Null Height - Throws BadRequestException")
    void createPackage_NullHeight_Throws() {
        // Arrange
        testPackageRequest.setHeight(null);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidHeight, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when length is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidLength error.
     */
    @Test
    @DisplayName("Create Package - Null Length - Throws BadRequestException")
    void createPackage_NullLength_Throws() {
        // Arrange
        testPackageRequest.setLength(null);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidLength, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when max weight is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidMaxWeight error.
     */
    @Test
    @DisplayName("Create Package - Null Max Weight - Throws BadRequestException")
    void createPackage_NullMaxWeight_Throws() {
        // Arrange
        testPackageRequest.setMaxWeight(null);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidMaxWeight, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when package name is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidPackageName error.
     */
    @Test
    @DisplayName("Create Package - Null Name - Throws BadRequestException")
    void createPackage_NullName_Throws() {
        // Arrange
        testPackageRequest.setPackageName(null);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageName, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when price per unit is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidPricePerUnit error.
     */
    @Test
    @DisplayName("Create Package - Null Price Per Unit - Throws BadRequestException")
    void createPackage_NullPricePerUnit_Throws() {
        // Arrange
        testPackageRequest.setPricePerUnit(null);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidPricePerUnit, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when the entire request is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidRequest error.
     */
    @Test
    @DisplayName("Create Package - Null Request - Throws BadRequestException")
    void createPackage_NullRequest_Throws() {
        // Arrange

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(null));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidRequest, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when standard capacity is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidStandardCapacity error.
     */
    @Test
    @DisplayName("Create Package - Null Standard Capacity - Throws BadRequestException")
    void createPackage_NullStandardCapacity_Throws() {
        // Arrange
        testPackageRequest.setStandardCapacity(null);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidStandardCapacity, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when package type is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidPackageType error.
     */
    @Test
    @DisplayName("Create Package - Null Type - Throws BadRequestException")
    void createPackage_NullType_Throws() {
        // Arrange
        testPackageRequest.setPackageType(null);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageType, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when package name exceeds maximum length validation.
     * Expected Result: BadRequestException is thrown for name length validation.
     * Assertions: Exception message matches InvalidPackageName error.
     */
    @Test
    @DisplayName("Create Package - Name Too Long - Throws BadRequestException")
    void createPackage_PackageName_TooLong_Throws() {
        // Arrange
        String tooLongName = "A".repeat(300);
        testPackageRequest.setPackageName(tooLongName);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageName, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when package type consists only of whitespace.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidPackageType error.
     */
    @Test
    @DisplayName("Create Package - Whitespace Type - Throws BadRequestException")
    void createPackage_WhitespaceType_Throws() {
        // Arrange
        testPackageRequest.setPackageType("   ");

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageType, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when breadth is zero.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidBreadth error.
     */
    @Test
    @DisplayName("Create Package - Zero Breadth - Throws BadRequestException")
    void createPackage_ZeroBreadth_Throws() {
        // Arrange
        testPackageRequest.setBreadth(0);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidBreadth, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when height is zero.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidHeight error.
     */
    @Test
    @DisplayName("Create Package - Zero Height - Throws BadRequestException")
    void createPackage_ZeroHeight_Throws() {
        // Arrange
        testPackageRequest.setHeight(0);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidHeight, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when length is zero.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidLength error.
     */
    @Test
    @DisplayName("Create Package - Zero Length - Throws BadRequestException")
    void createPackage_ZeroLength_Throws() {
        // Arrange
        testPackageRequest.setLength(0);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));

        // Assert
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidLength, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when standard capacity is zero.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidStandardCapacity error.
     */
    @Test
    @DisplayName("Create Package - Zero Standard Capacity - Throws BadRequestException")
    void createPackage_ZeroStandardCapacity_Throws() {
        // Arrange
        testPackageRequest.setStandardCapacity(0);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));

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
    @DisplayName("createPackage - Controller Permission - Unauthorized")
    void createPackage_controller_permission_unauthorized() {
        // Arrange
        PackageController controller = new PackageController(packageServiceMock, null);
        stubPackageServiceThrowsUnauthorizedException();

        // Act
        ResponseEntity<?> response = controller.createPackage(testPackageRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify @PreAuthorize annotation on createPackage endpoint.
     * Expected Result: Annotation exists and references INSERT_PACKAGES_PERMISSION.
     * Assertions: Annotation is present and contains permission.
     */
    @Test
    @DisplayName("createPackage - Verify @PreAuthorize Annotation")
    void createPackage_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        Method method = PackageController.class.getMethod("createPackage", PackageRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.INSERT_PACKAGES_PERMISSION),
                "@PreAuthorize should reference INSERT_PACKAGES_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to service for valid requests.
     * Expected Result: Service method is invoked and HTTP 200 returned.
     * Assertions: Service called once and status code is OK.
     */
    @Test
    @DisplayName("createPackage - Controller delegates to service")
    void createPackage_WithValidRequest_DelegatesToService() {
        // Arrange
        PackageController controller = new PackageController(packageServiceMock, null);
        stubPackageServiceCreatePackageDoNothing();

        // Act
        ResponseEntity<?> response = controller.createPackage(testPackageRequest);

        // Assert
        verify(packageServiceMock).createPackage(testPackageRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
