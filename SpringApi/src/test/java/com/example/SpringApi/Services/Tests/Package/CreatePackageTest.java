package com.example.SpringApi.Services.Tests.Package;

import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PackageService.createPackage() method.
 * Exhaustive validation for every field and path in the creation logic.
 * * Test Count: 31 tests
 */
@DisplayName("Create Package Tests")
class CreatePackageTest extends PackageServiceTestBase {

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
        testPackageRequest.setLength(1);
        testPackageRequest.setBreadth(1);
        testPackageRequest.setHeight(1);
        testPackageRequest.setStandardCapacity(1);
        when(packageRepository.save(any())).thenReturn(testPackage);
        assertDoesNotThrow(() -> packageService.createPackage(testPackageRequest));
    }

    /**
     * Purpose: Verify permission check is performed for INSERT_PACKAGE permission.
     * Expected Result: Authorization service is called to check permissions.
     * Assertions: authorization.hasAuthority() is called with correct permission.
     */
    @Test
    @DisplayName("Create Package - Permission Check - Success Verifies Authorization")
    void createPackage_PermissionCheck_SuccessVerifiesAuthorization() {
        when(packageRepository.save(any())).thenReturn(testPackage);
        lenient().when(authorization.hasAuthority(Authorizations.INSERT_PACKAGES_PERMISSION)).thenReturn(true);

        packageService.createPackage(testPackageRequest);

        verify(authorization, atLeastOnce()).hasAuthority(Authorizations.INSERT_PACKAGES_PERMISSION);
    }

    /**
     * Purpose: Verify successful package creation with a valid request.
     * Expected Result: Package is created and success is logged.
     * Assertions: Repository save and userLogService.logData are called.
     */
    @Test
    @DisplayName("Create Package - Success - Valid request")
    void createPackage_Success() {
        when(packageRepository.save(any())).thenReturn(testPackage);

        assertDoesNotThrow(() -> packageService.createPackage(testPackageRequest));

        verify(packageRepository, times(1)).save(any());
        verify(userLogService, times(1)).logData(eq(TEST_USER_ID), contains("Successfully inserted package"), any());
    }

    /**
     * Purpose: Verify zero max weight is considered a valid entry.
     * Expected Result: Package is created without exception.
     * Assertions: assertDoesNotThrow verifies success.
     */
    @Test
    @DisplayName("Create Package - Zero Max Weight - Success")
    void createPackage_ZeroMaxWeight_Success() {
        testPackageRequest.setMaxWeight(BigDecimal.ZERO);
        when(packageRepository.save(any())).thenReturn(testPackage);
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
        testPackageRequest.setPricePerUnit(BigDecimal.ZERO);
        when(packageRepository.save(any())).thenReturn(testPackage);
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
        testPackageRequest.setBreadth(-1);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.createPackage(testPackageRequest));
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
        testPackageRequest.setHeight(-1);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.createPackage(testPackageRequest));
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
        testPackageRequest.setLength(-5);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.createPackage(testPackageRequest));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidLength, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when max weight is negative.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidMaxWeight error.
     */
    @Test
    @DisplayName("Create Package - Negative Max Weight - Throws BadRequestException")
    void createPackage_NegativeMaxWeight_Throws() {
        testPackageRequest.setMaxWeight(BigDecimal.valueOf(-1.0));
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.createPackage(testPackageRequest));
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
        testPackageRequest.setPricePerUnit(BigDecimal.valueOf(-1.0));
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.createPackage(testPackageRequest));
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
        testPackageRequest.setStandardCapacity(-5);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.createPackage(testPackageRequest));
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
        testPackageRequest.setBreadth(null);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.createPackage(testPackageRequest));
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
        testPackageRequest.setHeight(null);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.createPackage(testPackageRequest));
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
        testPackageRequest.setLength(null);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.createPackage(testPackageRequest));
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
        testPackageRequest.setMaxWeight(null);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.createPackage(testPackageRequest));
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
        testPackageRequest.setPackageName(null);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.createPackage(testPackageRequest));
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
        testPackageRequest.setPricePerUnit(null);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.createPackage(testPackageRequest));
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
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.createPackage(null));
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
        testPackageRequest.setStandardCapacity(null);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.createPackage(testPackageRequest));
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
        testPackageRequest.setPackageType(null);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.createPackage(testPackageRequest));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageType, ex.getMessage());
    }

    /**
     * Purpose: Reject creation when package type consists only of whitespace.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Exception message matches InvalidPackageType error.
     */
    @Test
    @DisplayName("Create Package - Whitespace Type - Throws BadRequestException")
    void createPackage_WhitespaceType_Throws() {
        testPackageRequest.setPackageType("   ");
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.createPackage(testPackageRequest));
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
        testPackageRequest.setBreadth(0);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.createPackage(testPackageRequest));
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
        testPackageRequest.setHeight(0);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.createPackage(testPackageRequest));
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
        testPackageRequest.setLength(0);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.createPackage(testPackageRequest));
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
        testPackageRequest.setStandardCapacity(0);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> packageService.createPackage(testPackageRequest));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidStandardCapacity, ex.getMessage());
    }
}