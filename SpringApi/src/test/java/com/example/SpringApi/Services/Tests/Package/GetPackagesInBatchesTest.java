package com.example.SpringApi.Services.Tests.Package;

import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.PackageResponseModel;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PackageService.getPackagesInBatches() method.
 * Comprehensive testing for pagination and filtering logic.
 * * Test Count: 5 tests
 */
@DisplayName("Get Packages In Batches Tests")
class GetPackagesInBatchesTest extends PackageServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify permission check is performed for VIEW_PACKAGE permission.
     * Expected Result: Authorization service is called to check permissions.
     * Assertions: authorization.hasAuthority() is called with correct permission.
     */
    @Test
    @DisplayName("Get Packages In Batches - Permission Check - Success Verifies Authorization")
    void getPackagesInBatches_PermissionCheck_SuccessVerifiesAuthorization() {
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(10);
        testPaginationRequest.setFilters(null);
        Page<com.example.SpringApi.Models.DatabaseModels.Package> page = new PageImpl<>(Arrays.asList(testPackage));
        when(packageFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(page);
        lenient().when(authorization.hasAuthority(Authorizations.VIEW_PACKAGES_PERMISSION)).thenReturn(true);

        packageService.getPackagesInBatches(testPaginationRequest);

        verify(authorization, atLeastOnce()).hasAuthority(Authorizations.VIEW_PACKAGES_PERMISSION);
    }

    /**
     * Purpose: Verify successful retrieval without filters.
     * Expected Result: Valid pagination request returns a single data entry.
     * Assertions: result.getData().size() equals 1.
     */
    @Test
    @DisplayName("Get Packages In Batches - Success")
    void getPackagesInBatches_Success() {
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(10);
        testPaginationRequest.setFilters(null);
        Page<com.example.SpringApi.Models.DatabaseModels.Package> page = new PageImpl<>(Arrays.asList(testPackage));
        when(packageFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(page);

        PaginationBaseResponseModel<PackageResponseModel> result = packageService.getPackagesInBatches(testPaginationRequest);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject pagination when end index is zero.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches EndIndexMustBeGreaterThanZero.
     */
    @Test
    @DisplayName("Get Packages In Batches - End Index Zero - Throws BadRequestException")
    void getPackagesInBatches_EndIndexZero_ThrowsBadRequestException() {
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(0);
        assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.EndIndexMustBeGreaterThanZero, () -> packageService.getPackagesInBatches(testPaginationRequest));
    }

    /**
     * Purpose: Reject pagination when start index is negative.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches StartIndexCannotBeNegative.
     */
    @Test
    @DisplayName("Get Packages In Batches - Negative Start - Throws BadRequestException")
    void getPackagesInBatches_NegativeStart_ThrowsBadRequestException() {
        testPaginationRequest.setStart(-1);
        assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.StartIndexCannotBeNegative, () -> packageService.getPackagesInBatches(testPaginationRequest));
    }

    /**
     * Purpose: Reject pagination when start index is greater than or equal to end index.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches StartIndexMustBeLessThanEnd.
     */
    @Test
    @DisplayName("Get Packages In Batches - Start Greater Than End - Throws BadRequestException")
    void getPackagesInBatches_StartGreaterEqualEnd_ThrowsBadRequestException() {
        testPaginationRequest.setStart(10);
        testPaginationRequest.setEnd(5);
        assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.StartIndexMustBeLessThanEnd, () -> packageService.getPackagesInBatches(testPaginationRequest));
    }
}