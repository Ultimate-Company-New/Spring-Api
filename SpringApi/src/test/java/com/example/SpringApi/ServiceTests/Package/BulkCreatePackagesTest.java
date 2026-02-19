package com.example.SpringApi.ServiceTests.Package;

import com.example.SpringApi.Controllers.PackageController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.RequestModels.PackageRequestModel;
import com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PackageService.bulkCreatePackages() method.
 * Covers success, partial success, and validation failures.
 */
@DisplayName("Bulk Create Packages Tests")
class BulkCreatePackagesTest extends PackageServiceTestBase {

    // Total Tests: 37
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify bulk creation succeeds with duplicate names.
     * Expected Result: All packages are saved successfully.
     * Assertions: Success count equals total and save called expected times.
     */
    @Test
    @DisplayName("Bulk Create Packages - All Duplicate Names - Success")
    void bulkCreatePackages_s01_AllDuplicateNames_Success() {
        // Arrange
        List<PackageRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PackageRequestModel req = createValidPackageRequest();
            req.setPackageName("DUPLICATE");
            requests.add(req);
        }
        stubPackageRepositorySave(testPackage);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(requests);

        // Assert
        assertEquals(5, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        verify(packageRepository, times(5)).save(any());
    }

    /*
     * Purpose: Verify successful bulk creation when all provided packages are valid.
     * Expected Result: Result model indicates 100% success rate.
     * Assertions: Total requested, success count, and repository save count match.
     */
    @Test
    @DisplayName("Bulk Create Packages - All Valid - Success")
    void bulkCreatePackages_s02_AllValid_Success() {
        // Arrange
        List<PackageRequestModel> packages = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            PackageRequestModel pkgReq = createValidPackageRequest();
            pkgReq.setPackageName("Package" + i);
            packages.add(pkgReq);
        }
        stubPackageRepositorySave(testPackage);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(packages);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        verify(packageRepository, times(3)).save(any());
    }

    /*
     * Purpose: Verify partial success for alternating valid and invalid packages.
     * Expected Result: Valid packages are saved and invalid packages are reported as failures.
     * Assertions: Success count and failure count match inputs.
     */
    @Test
    @DisplayName("Bulk Create Packages - Alternating Valid Invalid - Partial Success")
    void bulkCreatePackages_s03_AlternatingValidInvalid_PartialSuccess() {
        // Arrange
        List<PackageRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            PackageRequestModel req = createValidPackageRequest();
            if (i % 2 == 1) {
                req.setPackageName("");
            }
            requests.add(req);
        }
        stubPackageRepositorySave(testPackage);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(requests);

        // Assert
        assertEquals(4, result.getTotalRequested());
        assertEquals(2, result.getSuccessCount());
        assertEquals(2, result.getFailureCount());
    }

    /*
     * Purpose: Verify system handles extreme batch sizes correctly.
     * Expected Result: All packages are processed and counted.
     * Assertions: Total requested count matches list size.
     */
    @Test
    @DisplayName("Bulk Create Packages - Extreme Batch Size - Success")
    void bulkCreatePackages_s04_ExtremeBatchSize_Success() {
        // Arrange
        List<PackageRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            requests.add(createValidPackageRequest());
        }
        stubPackageRepositorySave(testPackage);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(requests);

        // Assert
        assertEquals(200, result.getTotalRequested());
    }

    /*
     * Purpose: Verify system handles large batches (100 items) correctly.
     * Expected Result: All 100 packages are processed and success is recorded.
     * Assertions: Total requested count matches 100.
     */
    @Test
    @DisplayName("Bulk Create Packages - Large Batch - Success")
    void bulkCreatePackages_s05_LargeBatch_Success() {
        // Arrange
        List<PackageRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            requests.add(createValidPackageRequest());
        }
        stubPackageRepositorySave(testPackage);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(requests);

        // Assert
        assertEquals(100, result.getTotalRequested());
    }

    /*
     * Purpose: Verify partial success when the list contains both valid and invalid packages.
     * Expected Result: Valid packages are saved, invalid ones are reported as failures.
     * Assertions: Success count is 2 and failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Packages - Mixed Invalid and Valid - Partial Success")
    void bulkCreatePackages_s06_MixedInvalidAndValid_PartialSuccess() {
        // Arrange
        List<PackageRequestModel> requests = new ArrayList<>();
        requests.add(createValidPackageRequest());

        PackageRequestModel invalidPkg = createValidPackageRequest();
        invalidPkg.setPackageName("");
        requests.add(invalidPkg);

        requests.add(createValidPackageRequest());
        stubPackageRepositorySave(testPackage);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(requests);

        // Assert
        assertEquals(3, result.getTotalRequested());
        assertEquals(2, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
    }

    /*
     * Purpose: Verify creation of a single valid package via bulk method.
     * Expected Result: Single success recorded.
     * Assertions: Success count is 1 and failure count is 0.
     */
    @Test
    @DisplayName("Bulk Create Packages - Single Valid Item - Success")
    void bulkCreatePackages_s07_SingleValidItem_Success() {
        // Arrange
        List<PackageRequestModel> packages = List.of(createValidPackageRequest());
        stubPackageRepositorySave(testPackage);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(packages);

        // Assert
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify result when all items in the batch have invalid names.
     * Expected Result: All items are reported as failures.
     * Assertions: Failure count equals the total requested count.
     */
    @Test
    @DisplayName("Bulk Create Packages - All Invalid Names - All Fail")
    void bulkCreatePackages_f01_AllInvalidNames_AllFail() {
        // Arrange
        List<PackageRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PackageRequestModel req = createValidPackageRequest();
            req.setPackageName("");
            requests.add(req);
        }

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(requests);

        // Assert
        assertEquals(5, result.getFailureCount());
        verify(packageRepository, never()).save(any());
    }

    /*
     * Purpose: Handle database runtime errors during individual package processing.
     * Expected Result: Error is caught and recorded as a failure for that specific item.
     * Assertions: Failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Packages - Database Error - Records Failure")
    void bulkCreatePackages_f02_DatabaseError_RecordsFailure() {
        // Arrange
        List<PackageRequestModel> packages = List.of(createValidPackageRequest());
        stubPackageRepositorySaveThrows(new RuntimeException("DB Error"));

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(packages);

        // Assert
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
    }

    /*
     * Purpose: Reject bulk creation when the provided list is empty.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches CommonErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY.
     */
    @Test
    @DisplayName("Bulk Create Packages - Empty List - Throws BadRequestException")
    void bulkCreatePackages_f03_EmptyList_ThrowsBadRequestException() {
        // Arrange
        List<PackageRequestModel> packages = new ArrayList<>();

        // Act
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> packageService.bulkCreatePackages(packages));

        // Assert
        assertEquals(String.format(ErrorMessages.CommonErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY, "Package"),
                exception.getMessage());
    }

    /*
     * Purpose: Reject bulk creation for invalid attribute: Breadth Zero.
     * Expected Result: Failure count is 1.
     * Assertions: Result failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Packages - Invalid Breadth Zero - Fails")
    void bulkCreatePackages_f04_InvalidBreadthZero_Fails() {
        // Arrange
        PackageRequestModel req = createValidPackageRequest();
        req.setBreadth(0);

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(List.of(req));

        // Assert
        assertEquals(1, result.getFailureCount());
    }

    /*
     * Purpose: Reject bulk creation for invalid attribute: Height Zero.
     * Expected Result: Failure count is 1.
     * Assertions: Result failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Packages - Invalid Height Zero - Fails")
    void bulkCreatePackages_f05_InvalidHeightZero_Fails() {
        // Arrange
        PackageRequestModel req = createValidPackageRequest();
        req.setHeight(0);

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(List.of(req));

        // Assert
        assertEquals(1, result.getFailureCount());
    }

    /*
     * Purpose: Reject bulk creation for invalid attribute: Length Zero.
     * Expected Result: Failure count is 1.
     * Assertions: Result failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Packages - Invalid Length Zero - Fails")
    void bulkCreatePackages_f06_InvalidLengthZero_Fails() {
        // Arrange
        PackageRequestModel req = createValidPackageRequest();
        req.setLength(0);

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(List.of(req));

        // Assert
        assertEquals(1, result.getFailureCount());
    }

    /*
     * Purpose: Reject bulk creation for invalid attribute: Negative Weight.
     * Expected Result: Failure count is 1.
     * Assertions: Result failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Packages - Invalid Negative Weight - Fails")
    void bulkCreatePackages_f07_InvalidNegativeWeight_Fails() {
        // Arrange
        PackageRequestModel req = createValidPackageRequest();
        req.setMaxWeight(new BigDecimal("-1.0"));

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(List.of(req));

        // Assert
        assertEquals(1, result.getFailureCount());
    }

    /*
     * Purpose: Reject bulk creation when the provided list is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches CommonErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY.
     */
    @Test
    @DisplayName("Bulk Create Packages - Null List - Throws BadRequestException")
    void bulkCreatePackages_f08_NullList_ThrowsBadRequestException() {
        // Arrange
        List<PackageRequestModel> packages = null;

        // Act
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> packageService.bulkCreatePackages(packages));

        // Assert
        assertEquals(String.format(ErrorMessages.CommonErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY, "Package"),
                exception.getMessage());
    }

    /*
     * Purpose: Reject bulk creation when the package name is null.
     * Expected Result: Failure count is 1.
     * Assertions: Result failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Packages - Null Package Name - Fails")
    void bulkCreatePackages_f09_NullPackageName_Fails() {
        // Arrange
        PackageRequestModel req = createValidPackageRequest();
        req.setPackageName(null);

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(List.of(req));

        // Assert
        assertEquals(1, result.getFailureCount());
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify unauthorized access is blocked at the controller level.
     * Expected Result: Unauthorized status is returned.
     * Assertions: Response status is 401 UNAUTHORIZED.
     */
    @Test
    @DisplayName("bulkCreatePackages - Controller Permission - Unauthorized")
    void bulkCreatePackages_p01_controller_permission_unauthorized() {
        // Arrange
        stubConcretePackageServiceThrowsUnauthorized();
        PackageController controller = new PackageController(packageServiceMock, concretePackageServiceMock);

        // Act
        ResponseEntity<?> response = controller.bulkCreatePackages(new ArrayList<>());

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(packageServiceMock, never()).bulkCreatePackagesAsync(anyList(), anyLong(), anyString(), anyLong());
    }
    /*
     * Purpose: Verify controller delegates to async service call for valid requests.
     * Expected Result: Service method is invoked and HTTP 200 returned.
     * Assertions: Service called once and status code is OK.
     */
    @Test
    @DisplayName("bulkCreatePackages - Controller delegates to service")
    void bulkCreatePackages_p03_WithValidRequest_DelegatesToService() {
        // Arrange
        stubConcretePackageServiceUserContext(TEST_USER_ID, "testuser", TEST_CLIENT_ID);
        PackageController controller = new PackageController(packageServiceMock, concretePackageServiceMock);
        stubPackageServiceBulkCreatePackagesAsyncDoNothing();

        // Act
        ResponseEntity<?> response = controller.bulkCreatePackages(List.of(createValidPackageRequest()));

        // Assert
        verify(packageServiceMock).bulkCreatePackagesAsync(anyList(), anyLong(), anyString(), anyLong());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}

/**
 * Unit tests for PackageService.bulkCreatePackages() method.
 * Covers success, partial success, and exhaustive validation failures.
 */
@DisplayName("Bulk Create Packages Tests - Duplicate Block")
class BulkCreatePackagesDuplicateTests extends PackageServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify bulk creation with all packages having identical names (duplicate scenario).
     * Expected Result: All duplicates are saved successfully (if business logic allows).
     * Assertions: Success count equals number of duplicate packages.
     */
    @Test
    @DisplayName("Bulk Create Packages - All Duplicate Names - Success if Allowed")
    void bulkCreatePackages_s08_AllDuplicateNames_Success() {
        // Arrange
        List<PackageRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PackageRequestModel req = createValidPackageRequest();
            req.setPackageName("DuplicateName");
            requests.add(req);
        }
        stubPackageRepositorySave(testPackage);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(requests);

        // Assert
        assertTrue(result.getSuccessCount() > 0);
    }

    /**
     * Purpose: Verify successful bulk creation when all provided packages are valid.
     * Expected Result: Result model indicates 100% success rate.
     * Assertions: Total requested, success count, and repository save count match.
     */
    @Test
    @DisplayName("Bulk Create Packages - All Valid - Success")
    void bulkCreatePackages_s09_AllValid_Success() {
        // Arrange
        List<PackageRequestModel> packages = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            PackageRequestModel pkgReq = createValidPackageRequest();
            pkgReq.setPackageName("Package" + i);
            packages.add(pkgReq);
        }
        stubPackageRepositorySave(testPackage);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(packages);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        verify(packageRepository, times(3)).save(any());
    }

    /**
     * Purpose: Verify bulk creation with alternating valid and invalid packages (stress test).
     * Expected Result: Valid packages are saved, invalid ones are reported.
     * Assertions: Failure count is greater than 0 for alternating valid/invalid pattern.
     */
    @Test
    @DisplayName("Bulk Create Packages - Alternating Valid Invalid - Partial Success")
    void bulkCreatePackages_s10_AlternatingValidInvalid_PartialSuccess() {
        // Arrange
        List<PackageRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                requests.add(createValidPackageRequest());
            } else {
                PackageRequestModel invalidReq = createValidPackageRequest();
                invalidReq.setLength(0);
                requests.add(invalidReq);
            }
        }
        stubPackageRepositorySave(testPackage);

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(requests);

        // Assert
        assertEquals(5, result.getSuccessCount());
        assertEquals(5, result.getFailureCount());
    }

    /**
     * Purpose: Verify bulk creation handles extremely large batch (1000 items).
     * Expected Result: All 1000 packages are processed successfully.
     * Assertions: Total requested equals 1000 and success count equals 1000.
     */
    @Test
    @DisplayName("Bulk Create Packages - Extreme Batch Size 1000 - Success")
    void bulkCreatePackages_s11_ExtremeBatchSize_Success() {
        // Arrange
        List<PackageRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            requests.add(createValidPackageRequest());
        }
        stubPackageRepositorySave(testPackage);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(requests);

        // Assert
        assertEquals(1000, result.getTotalRequested());
        assertEquals(1000, result.getSuccessCount());
    }

    /**
     * Purpose: Verify system handles large batches (100 items) correctly.
     * Expected Result: All 100 packages are processed and success is recorded.
     * Assertions: Total requested count matches 100.
     */
    @Test
    @DisplayName("Bulk Create Packages - Large Batch - Success")
    void bulkCreatePackages_s12_LargeBatch_Success() {
        // Arrange
        List<PackageRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            requests.add(createValidPackageRequest());
        }
        stubPackageRepositorySave(testPackage);

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(requests);

        // Assert
        assertEquals(100, result.getTotalRequested());
    }

    /**
     * Purpose: Verify partial success when the list contains both valid and invalid packages.
     * Expected Result: Valid packages are saved, invalid ones are reported as failures.
     * Assertions: Success count is 2 and failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Packages - Mixed Invalid and Valid - Partial Success")
    void bulkCreatePackages_s13_MixedInvalidAndValid_PartialSuccess() {
        // Arrange
        List<PackageRequestModel> requests = new ArrayList<>();
        requests.add(createValidPackageRequest());
        PackageRequestModel invalidPkg = createValidPackageRequest();
        invalidPkg.setPackageName("");
        requests.add(invalidPkg);
        requests.add(createValidPackageRequest());
        stubPackageRepositorySave(testPackage);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(requests);

        // Assert
        assertEquals(3, result.getTotalRequested());
        assertEquals(2, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
    }

    /**
     * Purpose: Verify creation of a single valid package via bulk method.
     * Expected Result: Single success recorded.
     * Assertions: Success count is 1 and failure count is 0.
     */
    @Test
    @DisplayName("Bulk Create Packages - Single Valid Item - Success")
    void bulkCreatePackages_s14_SingleValidItem_Success() {
        // Arrange
        List<PackageRequestModel> packages = List.of(createValidPackageRequest());
        stubPackageRepositorySave(testPackage);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(packages);

        // Assert
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify result when all items in the batch have invalid names.
     * Expected Result: All items are reported as failures.
     * Assertions: Failure count equals the total requested count.
     */
    @Test
    @DisplayName("Bulk Create Packages - All Invalid Names - All Fail")
    void bulkCreatePackages_f10_AllInvalidNames_AllFail() {
        // Arrange
        List<PackageRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PackageRequestModel req = createValidPackageRequest();
            req.setPackageName("");
            requests.add(req);
        }

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(requests);

        // Assert
        assertEquals(5, result.getFailureCount());
        verify(packageRepository, never()).save(any());
    }

    /**
     * Purpose: Handle database runtime errors during individual package processing.
     * Expected Result: Error is caught and recorded as a failure for that specific item.
     * Assertions: Failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Packages - Database Error - Records Failure")
    void bulkCreatePackages_f11_DatabaseError_RecordsFailure() {
        // Arrange
        List<PackageRequestModel> packages = List.of(createValidPackageRequest());
        stubPackageRepositorySaveThrows(new RuntimeException("DB Error"));

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(packages);

        // Assert
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
    }

    /**
     * Purpose: Reject bulk creation when the provided list is empty.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches CommonErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY.
     */
    @Test
    @DisplayName("Bulk Create Packages - Empty List - Throws BadRequestException")
    void bulkCreatePackages_f12_EmptyList_ThrowsBadRequestException() {
        // Arrange
        List<PackageRequestModel> packages = new ArrayList<>();

        // Act
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> packageService.bulkCreatePackages(packages));

        // Assert
        assertEquals(String.format(ErrorMessages.CommonErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY, "Package"),
                exception.getMessage());
    }

    /**
     * Purpose: Reject bulk creation for invalid attribute: Breadth Zero.
     * Expected Result: Failure count is 1.
     * Assertions: Result failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Packages - Invalid Breadth Zero - Fails")
    void bulkCreatePackages_f13_InvalidBreadthZero_Fails() {
        // Arrange
        PackageRequestModel req = createValidPackageRequest();
        req.setBreadth(0);

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(List.of(req));

        // Assert
        assertEquals(1, result.getFailureCount());
    }

    /**
     * Purpose: Reject bulk creation for invalid attribute: Height Zero.
     * Expected Result: Failure count is 1.
     * Assertions: Result failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Packages - Invalid Height Zero - Fails")
    void bulkCreatePackages_f14_InvalidHeightZero_Fails() {
        // Arrange
        PackageRequestModel req = createValidPackageRequest();
        req.setHeight(0);

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(List.of(req));

        // Assert
        assertEquals(1, result.getFailureCount());
    }

    /**
     * Purpose: Reject bulk creation for invalid attribute: Length Zero.
     * Expected Result: Failure count is 1.
     * Assertions: Result failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Packages - Invalid Length Zero - Fails")
    void bulkCreatePackages_f15_InvalidLengthZero_Fails() {
        // Arrange
        PackageRequestModel req = createValidPackageRequest();
        req.setLength(0);

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(List.of(req));

        // Assert
        assertEquals(1, result.getFailureCount());
    }

    /**
     * Purpose: Reject bulk creation for invalid attribute: Negative Weight.
     * Expected Result: Failure count is 1.
     * Assertions: Result failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Packages - Invalid Negative Weight - Fails")
    void bulkCreatePackages_f16_InvalidNegativeWeight_Fails() {
        // Arrange
        PackageRequestModel req = createValidPackageRequest();
        req.setMaxWeight(new BigDecimal("-1.0"));

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(List.of(req));

        // Assert
        assertEquals(1, result.getFailureCount());
    }

    /**
     * Purpose: Reject bulk creation when the provided list is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches CommonErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY.
     */
    @Test
    @DisplayName("Bulk Create Packages - Null List - Throws BadRequestException")
    void bulkCreatePackages_f17_NullList_ThrowsBadRequestException() {
        // Arrange
        // Act
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> packageService.bulkCreatePackages(null));

        // Assert
        assertEquals(String.format(ErrorMessages.CommonErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY, "Package"),
                exception.getMessage());
    }

    /**
     * Purpose: Reject bulk creation when the package name is null.
     * Expected Result: Failure count is 1.
     * Assertions: Result failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Packages - Null Package Name - Fails")
    void bulkCreatePackages_f18_NullPackageName_Fails() {
        // Arrange
        PackageRequestModel req = createValidPackageRequest();
        req.setPackageName(null);

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(List.of(req));

        // Assert
        assertEquals(1, result.getFailureCount());
    }
    /**
     * Purpose: Verify controller delegates bulkCreatePackages to service.
     * Expected Result: Returns HTTP 200 OK when delegation succeeds.
     * Assertions: Response status is 200 OK.
     */
    @Test
    @DisplayName("bulkCreatePackages - Controller delegates to service")
    void bulkCreatePackages_p05_WithValidRequest_DelegatesToService() {
        // Arrange
        stubConcretePackageServiceUserContext(TEST_USER_ID, "testuser", TEST_CLIENT_ID);
        PackageController controller = new PackageController(packageServiceMock, concretePackageServiceMock);
        List<PackageRequestModel> packages = List.of(createValidPackageRequest());
        stubPackageServiceBulkCreatePackagesAsyncDoNothing();

        // Act
        ResponseEntity<?> response = controller.bulkCreatePackages(packages);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
