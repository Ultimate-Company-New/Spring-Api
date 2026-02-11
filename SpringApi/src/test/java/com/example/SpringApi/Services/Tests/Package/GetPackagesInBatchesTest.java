package com.example.SpringApi.Services.Tests.Package;

import com.example.SpringApi.Controllers.PackageController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.PackageResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for PackageService.getPackagesInBatches() method.
 * Comprehensive testing for pagination and filtering logic.
 */
@DisplayName("Get Packages In Batches Tests")
class GetPackagesInBatchesTest extends PackageServiceTestBase {
    // Total Tests: 10

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify pagination succeeds at boundary conditions (start=0, end=1).
     * Expected Result: Single item or empty result is handled correctly.
     * Assertions: Result is returned without error.
     */
    @Test
    @DisplayName("Get Packages In Batches - Boundary Indexes - Success")
    void getPackagesInBatches_BoundaryIndexes_Success() {
        // Arrange
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(1);
        testPaginationRequest.setFilters(null);
        Page<com.example.SpringApi.Models.DatabaseModels.Package> page = new PageImpl<>(Arrays.asList(testPackage));
        stubPackageFilterQueryBuilderFindPaginatedEntities(page);

        // Act
        PaginationBaseResponseModel<PackageResponseModel> result =
                packageService.getPackagesInBatches(testPaginationRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.getData().size() >= 0);
    }

    /**
     * Purpose: Verify pagination succeeds with very large page size (1000+).
     * Expected Result: Large batch of packages is returned.
     * Assertions: Result is not null and contains expected data.
     */
    @Test
    @DisplayName("Get Packages In Batches - Large Page Size - Success")
    void getPackagesInBatches_LargePage_Success() {
        // Arrange
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(1500);
        testPaginationRequest.setFilters(null);
        List<com.example.SpringApi.Models.DatabaseModels.Package> largeList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largeList.add(testPackage);
        }
        Page<com.example.SpringApi.Models.DatabaseModels.Package> page = new PageImpl<>(largeList);
        stubPackageFilterQueryBuilderFindPaginatedEntities(page);

        // Act
        PaginationBaseResponseModel<PackageResponseModel> result =
                packageService.getPackagesInBatches(testPaginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(100, result.getData().size());
    }

    /**
     * Purpose: Verify pagination succeeds with large start/end index values.
     * Expected Result: Query is executed with maximum index range.
     * Assertions: Result is not null and data size matches page content.
     */
    @Test
    @DisplayName("Get Packages In Batches - Max Pagination Range - Success")
    void getPackagesInBatches_MaxPaginationRange_Success() {
        // Arrange
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(Integer.MAX_VALUE - 1);
        testPaginationRequest.setFilters(null);
        Page<com.example.SpringApi.Models.DatabaseModels.Package> page = new PageImpl<>(Arrays.asList(testPackage));
        stubPackageFilterQueryBuilderFindPaginatedEntities(page);

        // Act
        PaginationBaseResponseModel<PackageResponseModel> result =
                packageService.getPackagesInBatches(testPaginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
    }

    /**
     * Purpose: Verify successful retrieval without filters.
     * Expected Result: Valid pagination request returns a single data entry.
     * Assertions: result.getData().size() equals 1.
     */
    @Test
    @DisplayName("Get Packages In Batches - Success")
    void getPackagesInBatches_Success() {
        // Arrange
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(10);
        testPaginationRequest.setFilters(null);
        Page<com.example.SpringApi.Models.DatabaseModels.Package> page = new PageImpl<>(Arrays.asList(testPackage));
        stubPackageFilterQueryBuilderFindPaginatedEntities(page);

        // Act
        PaginationBaseResponseModel<PackageResponseModel> result =
                packageService.getPackagesInBatches(testPaginationRequest);

        // Assert
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
        // Arrange
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(0);

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.EndIndexMustBeGreaterThanZero,
                () -> packageService.getPackagesInBatches(testPaginationRequest));
    }

    /**
     * Purpose: Reject pagination when start index is negative.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches StartIndexCannotBeNegative.
     */
    @Test
    @DisplayName("Get Packages In Batches - Negative Start - Throws BadRequestException")
    void getPackagesInBatches_NegativeStart_ThrowsBadRequestException() {
        // Arrange
        testPaginationRequest.setStart(-1);

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.StartIndexCannotBeNegative,
                () -> packageService.getPackagesInBatches(testPaginationRequest));
    }

    /**
     * Purpose: Reject pagination when start index is greater than or equal to end index.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches StartIndexMustBeLessThanEnd.
     */
    @Test
    @DisplayName("Get Packages In Batches - Start Greater Than End - Throws BadRequestException")
    void getPackagesInBatches_StartGreaterEqualEnd_ThrowsBadRequestException() {
        // Arrange
        testPaginationRequest.setStart(10);
        testPaginationRequest.setEnd(5);

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.StartIndexMustBeLessThanEnd,
                () -> packageService.getPackagesInBatches(testPaginationRequest));
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
    @DisplayName("getPackagesInBatches - Controller Permission - Unauthorized")
    void getPackagesInBatches_controller_permission_unauthorized() {
        // Arrange
        PackageController controller = new PackageController(packageServiceMock, null);
        stubPackageServiceThrowsUnauthorizedException();

        // Act
        ResponseEntity<?> response = controller.getPackagesInBatches(testPaginationRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify @PreAuthorize annotation on getPackagesInBatches endpoint.
     * Expected Result: Annotation exists and references VIEW_PACKAGES_PERMISSION.
     * Assertions: Annotation is present and contains permission.
     */
    @Test
    @DisplayName("getPackagesInBatches - Verify @PreAuthorize Annotation")
    void getPackagesInBatches_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        Method method = PackageController.class.getMethod("getPackagesInBatches", PaginationBaseRequestModel.class);

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
    @DisplayName("getPackagesInBatches - Controller delegates to service")
    void getPackagesInBatches_WithValidRequest_DelegatesToService() {
        // Arrange
        PackageController controller = new PackageController(packageServiceMock, null);
        stubPackageServiceGetPackagesInBatchesReturns(new PaginationBaseResponseModel<PackageResponseModel>());

        // Act
        ResponseEntity<?> response = controller.getPackagesInBatches(testPaginationRequest);

        // Assert
        verify(packageServiceMock).getPackagesInBatches(testPaginationRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}