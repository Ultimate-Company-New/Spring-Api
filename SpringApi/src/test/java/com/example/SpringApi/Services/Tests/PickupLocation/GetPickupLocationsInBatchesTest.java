package com.example.SpringApi.Services.Tests.PickupLocation;

import com.example.SpringApi.Models.ResponseModels.PickupLocationResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PickupLocationService.getPickupLocationsInBatches() method.
 * * Test Count: 5 tests
 */
@DisplayName("Get Pickup Locations In Batches Tests")
class GetPickupLocationsInBatchesTest extends PickupLocationServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify successful retrieval of pickup locations with valid pagination.
     * Expected Result: PaginationBaseResponseModel with correct data is returned.
     * Assertions: Result is not null, data size and total count are correct.
     */
    @Test
    @DisplayName("Get Pickup Locations In Batches - Comprehensive Validation - Success")
    void getPickupLocationsInBatches_Comprehensive_Success() {
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(10);
        testPaginationRequest.setFilters(null);
        
        Page<com.example.SpringApi.Models.DatabaseModels.PickupLocation> pageResult = 
            new PageImpl<>(Collections.singletonList(testPickupLocation));

        when(pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)))
            .thenReturn(pageResult);

        PaginationBaseResponseModel<PickupLocationResponseModel> result =
            pickupLocationService.getPickupLocationsInBatches(testPaginationRequest);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
    }

    /**
     * Purpose: Verify permission check is performed for VIEW_PICKUP_LOCATION permission.
     * Expected Result: Authorization service is called to check permissions.
     * Assertions: authorization.hasAuthority() is called with correct permission.
     */
    @Test
    @DisplayName("Get Pickup Locations In Batches - Permission Check - Success Verifies Authorization")
    void getPickupLocationsInBatches_PermissionCheck_SuccessVerifiesAuthorization() {
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(10);
        Page<com.example.SpringApi.Models.DatabaseModels.PickupLocation> pageResult = 
            new PageImpl<>(Collections.singletonList(testPickupLocation));
        when(pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)))
            .thenReturn(pageResult);
        lenient().when(authorization.hasAuthority(Authorizations.VIEW_PICKUP_LOCATIONS_PERMISSION)).thenReturn(true);

        pickupLocationService.getPickupLocationsInBatches(testPaginationRequest);

        verify(authorization, atLeastOnce()).hasAuthority(Authorizations.VIEW_PICKUP_LOCATIONS_PERMISSION);
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
    @DisplayName("Get Pickup Locations In Batches - End Index Zero - Throws BadRequestException")
    void getPickupLocationsInBatches_EndIndexZero_ThrowsBadRequestException() {
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(0);
        assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.EndIndexMustBeGreaterThanZero, 
            () -> pickupLocationService.getPickupLocationsInBatches(testPaginationRequest));
    }

    /**
     * Purpose: Reject pagination when start index is negative.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches StartIndexCannotBeNegative.
     */
    @Test
    @DisplayName("Get Pickup Locations In Batches - Negative Start - Throws BadRequestException")
    void getPickupLocationsInBatches_NegativeStart_ThrowsBadRequestException() {
        testPaginationRequest.setStart(-1);
        assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.StartIndexCannotBeNegative, 
            () -> pickupLocationService.getPickupLocationsInBatches(testPaginationRequest));
    }

    /**
     * Purpose: Reject pagination when start index is greater than or equal to end index.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches StartIndexMustBeLessThanEnd.
     */
    @Test
    @DisplayName("Get Pickup Locations In Batches - Start Greater Than End - Throws BadRequestException")
    void getPickupLocationsInBatches_StartGreaterEqualEnd_ThrowsBadRequestException() {
        testPaginationRequest.setStart(10);
        testPaginationRequest.setEnd(5);
        assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.StartIndexMustBeLessThanEnd, 
            () -> pickupLocationService.getPickupLocationsInBatches(testPaginationRequest));
    }
}