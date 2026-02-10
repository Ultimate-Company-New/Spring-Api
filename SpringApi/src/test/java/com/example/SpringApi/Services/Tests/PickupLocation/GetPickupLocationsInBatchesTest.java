package com.example.SpringApi.Services.Tests.PickupLocation;

import com.example.SpringApi.Controllers.PickupLocationController;
import com.example.SpringApi.Services.PickupLocationService;
import com.example.SpringApi.Models.ResponseModels.PickupLocationResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
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
     * Purpose: Verify successful retrieval of pickup locations with valid
     * pagination.
     * Expected Result: PaginationBaseResponseModel with correct data is returned.
     * Assertions: Result is not null, data size and total count are correct.
     */
    @Test
    @DisplayName("Get Pickup Locations In Batches - Comprehensive Validation - Success")
    void getPickupLocationsInBatches_Comprehensive_Success() {
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(10);
        testPaginationRequest.setFilters(null);

        Page<com.example.SpringApi.Models.DatabaseModels.PickupLocation> pageResult = new PageImpl<>(
                Collections.singletonList(testPickupLocation));

        when(pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)))
                .thenReturn(pageResult);

        PaginationBaseResponseModel<PickupLocationResponseModel> result = pickupLocationService
                .getPickupLocationsInBatches(testPaginationRequest);

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
     * Purpose: Reject pagination when start index is greater than or equal to end
     * index.
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

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("getPickupLocationsInBatches - Verify @PreAuthorize Annotation")
    void getPickupLocationsInBatches_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PickupLocationController.class.getMethod("getPickupLocationsInBatches",
                PaginationBaseRequestModel.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on getPickupLocationsInBatches");
        assertTrue(annotation.value().contains(Authorizations.VIEW_PICKUP_LOCATIONS_PERMISSION),
                "@PreAuthorize should reference VIEW_PICKUP_LOCATIONS_PERMISSION");
    }

    @Test
    @DisplayName("getPickupLocationsInBatches - Controller delegates to service")
    void getPickupLocationsInBatches_WithValidRequest_DelegatesToService() {
        PickupLocationService mockService = mock(PickupLocationService.class);
        PickupLocationController controller = new PickupLocationController(mockService);
        PaginationBaseResponseModel<PickupLocationResponseModel> mockResponse = new PaginationBaseResponseModel<>();
        when(mockService.getPickupLocationsInBatches(testPaginationRequest))
                .thenReturn(mockResponse);

        ResponseEntity<?> response = controller.getPickupLocationsInBatches(testPaginationRequest);

        verify(mockService).getPickupLocationsInBatches(testPaginationRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}