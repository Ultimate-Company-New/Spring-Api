package com.example.SpringApi.Services.Tests.Package;

import com.example.SpringApi.Controllers.PackageController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.PackageResponseModel;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
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

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("getPackagesInBatches - Verify @PreAuthorize Annotation")
    void getPackagesInBatches_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PackageController.class.getMethod("getPackagesInBatches", PaginationBaseRequestModel.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.VIEW_PACKAGES_PERMISSION),
            "@PreAuthorize should reference VIEW_PACKAGES_PERMISSION");
    }

    @Test
    @DisplayName("getPackagesInBatches - Controller delegates to service")
    void getPackagesInBatches_WithValidRequest_DelegatesToService() {
        PackageController controller = new PackageController(packageService, null);
        when(packageService.getPackagesInBatches(testPaginationRequest)).thenReturn(new PaginationBaseResponseModel<PackageResponseModel>());

        ResponseEntity<?> response = controller.getPackagesInBatches(testPaginationRequest);

        verify(packageService).getPackagesInBatches(testPaginationRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}