package com.example.SpringApi.Services.Tests.Package;

import com.example.SpringApi.Controllers.PackageController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.SpringApi.Models.RequestModels.PackageRequestModel;
import com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.BadRequestException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PackageService.bulkCreatePackages() method.
 * Covers success, partial success, and exhaustive validation failures.
 */
@DisplayName("Bulk Create Packages Tests")
class BulkCreatePackagesTest extends PackageServiceTestBase {
    // Total Tests: 15

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify successful bulk creation when all provided packages are valid.
     * Expected Result: Result model indicates 100% success rate.
     * Assertions: Total requested, success count, and repository save count match.
     */
    @Test
    @DisplayName("Bulk Create Packages - All Valid - Success")
    void bulkCreatePackages_AllValid_Success() {
        List<PackageRequestModel> packages = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            PackageRequestModel pkgReq = createValidPackageRequest();
            pkgReq.setPackageName("Package" + i);
            packages.add(pkgReq);
        }

        when(packageRepository.save(any(com.example.SpringApi.Models.DatabaseModels.Package.class))).thenReturn(testPackage);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(packages);

        assertNotNull(result);
        assertEquals(3, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        verify(packageRepository, times(3)).save(any());
    }

    /**
     * Purpose: Verify system handles large batches (100 items) correctly.
     * Expected Result: All 100 packages are processed and success is recorded.
     * Assertions: Total requested count matches 100.
     */
    @Test
    @DisplayName("Bulk Create Packages - Large Batch - Success")
    void bulkCreatePackages_LargeBatch_Success() {
        List<PackageRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            requests.add(createValidPackageRequest());
        }

        when(packageRepository.save(any())).thenReturn(testPackage);
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(requests);
        assertEquals(100, result.getTotalRequested());
    }

    /**
     * Purpose: Verify partial success when the list contains both valid and invalid packages.
     * Expected Result: Valid packages are saved, invalid ones are reported as failures.
     * Assertions: Success count is 2 and failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Packages - Mixed Invalid and Valid - Partial Success")
    void bulkCreatePackages_MixedInvalidAndValid_PartialSuccess() {
        List<PackageRequestModel> requests = new ArrayList<>();
        requests.add(createValidPackageRequest());
        
        PackageRequestModel invalidPkg = createValidPackageRequest();
        invalidPkg.setPackageName(""); // Invalid
        requests.add(invalidPkg);
        
        requests.add(createValidPackageRequest());

        when(packageRepository.save(any())).thenReturn(testPackage);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
        
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(requests);

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
    void bulkCreatePackages_SingleValidItem_Success() {
        List<PackageRequestModel> packages = List.of(createValidPackageRequest());
        when(packageRepository.save(any())).thenReturn(testPackage);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(packages);

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
    void bulkCreatePackages_AllInvalidNames_AllFail() {
        List<PackageRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PackageRequestModel req = createValidPackageRequest();
            req.setPackageName(""); 
            requests.add(req);
        }

        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(requests);
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
    void bulkCreatePackages_DatabaseError_RecordsFailure() {
        List<PackageRequestModel> packages = List.of(createValidPackageRequest());
        when(packageRepository.save(any())).thenThrow(new RuntimeException("DB Error"));

        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(packages);

        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
    }

    /**
     * Purpose: Reject bulk creation when the provided list is empty.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches CommonErrorMessages.ListCannotBeNullOrEmpty.
     */
    @Test
    @DisplayName("Bulk Create Packages - Empty List - Throws BadRequestException")
    void bulkCreatePackages_EmptyList_ThrowsBadRequestException() {
        List<PackageRequestModel> packages = new ArrayList<>();
        BadRequestException exception = assertThrows(BadRequestException.class, 
            () -> packageService.bulkCreatePackages(packages));
        assertTrue(exception.getMessage().contains("Package list cannot be null or empty"));
    }

    /**
     * Purpose: Reject bulk creation for invalid attribute: Breadth Zero.
     * Expected Result: Failure count is 1.
     * Assertions: Result failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Packages - Invalid Breadth Zero - Fails")
    void bulkCreatePackages_InvalidBreadthZero_Fails() {
        PackageRequestModel req = createValidPackageRequest();
        req.setBreadth(0);
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(List.of(req));
        assertEquals(1, result.getFailureCount());
    }

    /**
     * Purpose: Reject bulk creation for invalid attribute: Height Zero.
     * Expected Result: Failure count is 1.
     * Assertions: Result failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Packages - Invalid Height Zero - Fails")
    void bulkCreatePackages_InvalidHeightZero_Fails() {
        PackageRequestModel req = createValidPackageRequest();
        req.setHeight(0);
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(List.of(req));
        assertEquals(1, result.getFailureCount());
    }

    /**
     * Purpose: Reject bulk creation for invalid attribute: Length Zero.
     * Expected Result: Failure count is 1.
     * Assertions: Result failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Packages - Invalid Length Zero - Fails")
    void bulkCreatePackages_InvalidLengthZero_Fails() {
        PackageRequestModel req = createValidPackageRequest();
        req.setLength(0);
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(List.of(req));
        assertEquals(1, result.getFailureCount());
    }

    /**
     * Purpose: Reject bulk creation for invalid attribute: Negative Weight.
     * Expected Result: Failure count is 1.
     * Assertions: Result failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Packages - Invalid Negative Weight - Fails")
    void bulkCreatePackages_InvalidNegativeWeight_Fails() {
        PackageRequestModel req = createValidPackageRequest();
        req.setMaxWeight(new BigDecimal("-1.0"));
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(List.of(req));
        assertEquals(1, result.getFailureCount());
    }

    /**
     * Purpose: Reject bulk creation when the package name is null.
     * Expected Result: Failure count is 1.
     * Assertions: Result failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Packages - Null Package Name - Fails")
    void bulkCreatePackages_NullPackageName_Fails() {
        PackageRequestModel req = createValidPackageRequest();
        req.setPackageName(null);
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(List.of(req));
        assertEquals(1, result.getFailureCount());
    }

    /**
     * Purpose: Reject bulk creation when the provided list is null.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches CommonErrorMessages.ListCannotBeNullOrEmpty.
     */
    @Test
    @DisplayName("Bulk Create Packages - Null List - Throws BadRequestException")
    void bulkCreatePackages_NullList_ThrowsBadRequestException() {
        BadRequestException exception = assertThrows(BadRequestException.class, 
            () -> packageService.bulkCreatePackages(null));
        assertTrue(exception.getMessage().contains("Package list cannot be null or empty"));
    }

    /**
     * Purpose: Verify bulk creation handles extremely large batch (1000 items).
     * Expected Result: All 1000 packages are processed successfully.
     * Assertions: Total requested equals 1000 and success count equals 1000.
     */
    @Test
    @DisplayName("Bulk Create Packages - Extreme Batch Size 1000 - Success")
    void bulkCreatePackages_ExtremeBatchSize_Success() {
        List<PackageRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            requests.add(createValidPackageRequest());
        }
        when(packageRepository.save(any())).thenReturn(testPackage);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
        
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(requests);
        
        assertEquals(1000, result.getTotalRequested());
        assertEquals(1000, result.getSuccessCount());
    }

    /**
     * Purpose: Verify bulk creation with all packages having identical names (duplicate scenario).
     * Expected Result: All duplicates are saved successfully (if business logic allows).
     * Assertions: Success count equals number of duplicate packages.
     */
    @Test
    @DisplayName("Bulk Create Packages - All Duplicate Names - Success if Allowed")
    void bulkCreatePackages_AllDuplicateNames_Success() {
        List<PackageRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PackageRequestModel req = createValidPackageRequest();
            req.setPackageName("DuplicateName");
            requests.add(req);
        }
        when(packageRepository.save(any())).thenReturn(testPackage);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
        
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(requests);
        
        assertTrue(result.getSuccessCount() > 0);
    }

    /**
     * Purpose: Verify bulk creation with alternating valid and invalid packages (stress test).
     * Expected Result: Valid packages are saved, invalid ones are reported.
     * Assertions: Failure count is greater than 0 for alternating valid/invalid pattern.
     */
    @Test
    @DisplayName("Bulk Create Packages - Alternating Valid Invalid - Partial Success")
    void bulkCreatePackages_AlternatingValidInvalid_PartialSuccess() {
        List<PackageRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                requests.add(createValidPackageRequest());
            } else {
                PackageRequestModel invalidReq = createValidPackageRequest();
                invalidReq.setLength(0); // Invalid
                requests.add(invalidReq);
            }
        }
        when(packageRepository.save(any())).thenReturn(testPackage);
        
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(requests);
        
        assertEquals(5, result.getSuccessCount());
        assertEquals(5, result.getFailureCount());
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("bulkCreatePackages - Verify @PreAuthorize Annotation")
    void bulkCreatePackages_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PackageController.class.getMethod("bulkCreatePackages", List.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.INSERT_PACKAGES_PERMISSION),
            "@PreAuthorize should reference INSERT_PACKAGES_PERMISSION");
    }

    @Test
    @DisplayName("bulkCreatePackages - Controller delegates to service")
    void bulkCreatePackages_WithValidRequest_DelegatesToService() {
        PackageController controller = new PackageController(packageService, packageService);
        List<PackageRequestModel> packages = List.of(createValidPackageRequest());

        ResponseEntity<?> response = controller.bulkCreatePackages(packages);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}