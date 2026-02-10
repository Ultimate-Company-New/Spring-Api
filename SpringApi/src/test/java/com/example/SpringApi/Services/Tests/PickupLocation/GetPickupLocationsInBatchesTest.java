//package com.example.SpringApi.Services.Tests.PickupLocation;
//
//import com.example.SpringApi.Controllers.PickupLocationController;
//import com.example.SpringApi.Services.PickupLocationService;
//import com.example.SpringApi.Models.ResponseModels.PickupLocationResponseModel;
//import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
//import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
//import com.example.SpringApi.Models.Authorizations;
//import com.example.SpringApi.ErrorMessages;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//
//import java.lang.reflect.Method;
//import java.util.Collections;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
///**
// * Unit tests for PickupLocationService.getPickupLocationsInBatches() method.
// * Tests pagination validation, edge cases, and multiple result scenarios.
// * Test Count: 15 tests
// */
//@DisplayName("Get Pickup Locations In Batches Tests")
//class GetPickupLocationsInBatchesTest extends PickupLocationServiceTestBase {
//
//    /*
//     **********************************************************************************************
//     * SUCCESS TESTS
//     **********************************************************************************************
//     */
//
//    /**
//     * Purpose: Verify successful retrieval of pickup locations with valid
//     * pagination.
//     * Expected Result: PaginationBaseResponseModel with correct data is returned.
//     * Assertions: Result is not null, data size and total count are correct.
//     */
//    @Test
//    @DisplayName("Get Pickup Locations In Batches - Comprehensive Validation - Success")
//    void getPickupLocationsInBatches_Comprehensive_Success() {
//        testPaginationRequest.setStart(0);
//        testPaginationRequest.setEnd(10);
//        testPaginationRequest.setFilters(null);
//
//        Page<com.example.SpringApi.Models.DatabaseModels.PickupLocation> pageResult = new PageImpl<>(
//                Collections.singletonList(testPickupLocation));
//
//        when(pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
//                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)))
//                .thenReturn(pageResult);
//
//        PaginationBaseResponseModel<PickupLocationResponseModel> result = pickupLocationService
//                .getPickupLocationsInBatches(testPaginationRequest);
//
//        assertNotNull(result);
//        assertEquals(1, result.getData().size());
//    }
//
//    /*
//     **********************************************************************************************
//     * FAILURE / EXCEPTION TESTS
//     **********************************************************************************************
//     */
//
//    /**
//     * Purpose: Reject pagination when end index is zero.
//     * Expected Result: BadRequestException is thrown.
//     * Assertions: Error message matches EndIndexMustBeGreaterThanZero.
//     */
//    @Test
//    @DisplayName("Get Pickup Locations In Batches - End Index Zero - Throws BadRequestException")
//    void getPickupLocationsInBatches_EndIndexZero_ThrowsBadRequestException() {
//        // ARRANGE
//        testPaginationRequest.setStart(0);
//        testPaginationRequest.setEnd(0);
//
//        // ACT & ASSERT
//        BadRequestException ex = assertThrows(BadRequestException.class,
//                () -> pickupLocationService.getPickupLocationsInBatches(testPaginationRequest));
//        assertEquals(ErrorMessages.CommonErrorMessages.EndIndexMustBeGreaterThanZero, ex.getMessage());
//    }
//
//    /**
//     * Purpose: Reject pagination when start index is negative.
//     * Expected Result: BadRequestException is thrown.
//     * Assertions: Error message matches StartIndexCannotBeNegative.
//     */
//    @Test
//    @DisplayName("Get Pickup Locations In Batches - Negative Start - Throws BadRequestException")
//    void getPickupLocationsInBatches_NegativeStart_ThrowsBadRequestException() {
//        // ARRANGE
//        testPaginationRequest.setStart(-1);
//
//        // ACT & ASSERT
//        BadRequestException ex = assertThrows(BadRequestException.class,
//                () -> pickupLocationService.getPickupLocationsInBatches(testPaginationRequest));
//        assertEquals(ErrorMessages.CommonErrorMessages.StartIndexCannotBeNegative, ex.getMessage());
//    }
//
//    /**
//     * Purpose: Reject pagination when start index is greater than or equal to end
//     * index.
//     * Expected Result: BadRequestException is thrown.
//     * Assertions: Error message matches StartIndexMustBeLessThanEnd.
//     */
//    @Test
//    @DisplayName("Get Pickup Locations In Batches - Start Greater Than End - Throws BadRequestException")
//    void getPickupLocationsInBatches_StartGreaterEqualEnd_ThrowsBadRequestException() {
//        // ARRANGE
//        testPaginationRequest.setStart(10);
//        testPaginationRequest.setEnd(5);
//
//        // ACT & ASSERT
//        BadRequestException ex = assertThrows(BadRequestException.class,
//                () -> pickupLocationService.getPickupLocationsInBatches(testPaginationRequest));
//        assertEquals(ErrorMessages.CommonErrorMessages.StartIndexMustBeLessThanEnd, ex.getMessage());
//    }
//
//    /**
//     * Purpose: Test with large batch size (high end value).
//     * Expected Result: Pagination succeeds with large range.
//     * Assertions: Result is not null.
//     */
//    @Test
//    @DisplayName("Get Pickup Locations In Batches - Large Batch Size - Success")
//    void getPickupLocationsInBatches_LargeBatchSize_Success() {
//        // ARRANGE
//        testPaginationRequest.setStart(0);
//        testPaginationRequest.setEnd(1000);
//
//        Page<com.example.SpringApi.Models.DatabaseModels.PickupLocation> pageResult = new PageImpl<>(
//                Collections.singletonList(testPickupLocation));
//
//        when(pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
//                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)))
//                .thenReturn(pageResult);
//
//        // ACT
//        PaginationBaseResponseModel<PickupLocationResponseModel> result = pickupLocationService
//                .getPickupLocationsInBatches(testPaginationRequest);
//
//        // ASSERT
//        assertNotNull(result);
//    }
//
//    /**
//     * Purpose: Test pagination with start equals end minus one (minimal range).
//     * Expected Result: Pagination succeeds with minimal range.
//     * Assertions: Result is not null.
//     */
//    @Test
//    @DisplayName("Get Pickup Locations In Batches - Minimal Range - Success")
//    void getPickupLocationsInBatches_MinimalRange_Success() {
//        // ARRANGE
//        testPaginationRequest.setStart(0);
//        testPaginationRequest.setEnd(1);
//
//        Page<com.example.SpringApi.Models.DatabaseModels.PickupLocation> pageResult = new PageImpl<>(
//                Collections.emptyList());
//
//        when(pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
//                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)))
//                .thenReturn(pageResult);
//
//        // ACT
//        PaginationBaseResponseModel<PickupLocationResponseModel> result = pickupLocationService
//                .getPickupLocationsInBatches(testPaginationRequest);
//
//        // ASSERT
//        assertNotNull(result);
//        assertEquals(0, result.getData().size());
//    }
//
//    /**
//     * Purpose: Test with includeDeleted flag set to true.
//     * Expected Result: Both deleted and active locations are retrieved.
//     * Assertions: Result is not null, includeDeleted is respected.
//     */
//    @Test
//    @DisplayName("Get Pickup Locations In Batches - Include Deleted - Success")
//    void getPickupLocationsInBatches_IncludeDeleted_Success() {
//        // ARRANGE
//        testPaginationRequest.setStart(0);
//        testPaginationRequest.setEnd(10);
//        testPaginationRequest.setIncludeDeleted(true);
//
//        Page<com.example.SpringApi.Models.DatabaseModels.PickupLocation> pageResult = new PageImpl<>(
//                Collections.singletonList(testPickupLocation));
//
//        when(pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
//                anyLong(), any(), anyString(), any(), eq(true), any(Pageable.class)))
//                .thenReturn(pageResult);
//
//        // ACT
//        PaginationBaseResponseModel<PickupLocationResponseModel> result = pickupLocationService
//                .getPickupLocationsInBatches(testPaginationRequest);
//
//        // ASSERT
//        assertNotNull(result);
//        verify(pickupLocationFilterQueryBuilder).findPaginatedEntitiesWithMultipleFilters(
//                anyLong(), any(), anyString(), any(), eq(true), any(Pageable.class));
//    }
//
//    /**
//     * Purpose: Test with negative end index.
//     * Expected Result: BadRequestException is thrown.
//     * Assertions: Error message indicates invalid pagination.
//     */
//    @Test
//    @DisplayName("Get Pickup Locations In Batches - Negative End Index - Throws BadRequestException")
//    void getPickupLocationsInBatches_NegativeEndIndex_ThrowsBadRequestException() {
//        // ARRANGE
//        testPaginationRequest.setStart(0);
//        testPaginationRequest.setEnd(-1);
//
//        // ACT & ASSERT
//        BadRequestException ex = assertThrows(BadRequestException.class,
//                () -> pickupLocationService.getPickupLocationsInBatches(testPaginationRequest));
//        assertNotNull(ex.getMessage());
//    }
//
//    /**
//     * Purpose: Test with equal start and end indices.
//     * Expected Result: BadRequestException is thrown.
//     * Assertions: Error message indicates start must be less than end.
//     */
//    @Test
//    @DisplayName("Get Pickup Locations In Batches - Start Equals End - Throws BadRequestException")
//    void getPickupLocationsInBatches_StartEqualsEnd_ThrowsBadRequestException() {
//        // ARRANGE
//        testPaginationRequest.setStart(5);
//        testPaginationRequest.setEnd(5);
//
//        // ACT & ASSERT
//        BadRequestException ex = assertThrows(BadRequestException.class,
//                () -> pickupLocationService.getPickupLocationsInBatches(testPaginationRequest));
//        assertEquals(ErrorMessages.CommonErrorMessages.StartIndexMustBeLessThanEnd, ex.getMessage());
//    }
//
//    /**
//     * Purpose: Test empty result set with valid pagination.
//     * Expected Result: Empty list returned successfully.
//     * Assertions: Result data size is zero.
//     */
//    @Test
//    @DisplayName("Get Pickup Locations In Batches - Empty Results - Success")
//    void getPickupLocationsInBatches_EmptyResults_Success() {
//        // ARRANGE
//        testPaginationRequest.setStart(0);
//        testPaginationRequest.setEnd(10);
//
//        Page<com.example.SpringApi.Models.DatabaseModels.PickupLocation> pageResult = new PageImpl<>(
//                Collections.emptyList());
//
//        when(pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
//                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)))
//                .thenReturn(pageResult);
//
//        // ACT
//        PaginationBaseResponseModel<PickupLocationResponseModel> result = pickupLocationService
//                .getPickupLocationsInBatches(testPaginationRequest);
//
//        // ASSERT
//        assertNotNull(result);
//        assertEquals(0, result.getData().size());
//    }
//
//    /**
//     * Purpose: Test with multiple results in pagination.
//     * Expected Result: All results returned correctly.
//     * Assertions: Result data size matches expected count.
//     */
//    @Test
//    @DisplayName("Get Pickup Locations In Batches - Multiple Results - Success")
//    void getPickupLocationsInBatches_MultipleResults_Success() {
//        // ARRANGE
//        testPaginationRequest.setStart(0);
//        testPaginationRequest.setEnd(10);
//
//        com.example.SpringApi.Models.DatabaseModels.PickupLocation location2 =
//            new com.example.SpringApi.Models.DatabaseModels.PickupLocation();
//        location2.setPickupLocationId(2L);
//
//        Page<com.example.SpringApi.Models.DatabaseModels.PickupLocation> pageResult = new PageImpl<>(
//                java.util.Arrays.asList(testPickupLocation, location2));
//
//        when(pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
//                anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class)))
//                .thenReturn(pageResult);
//
//        // ACT
//        PaginationBaseResponseModel<PickupLocationResponseModel> result = pickupLocationService
//                .getPickupLocationsInBatches(testPaginationRequest);
//
//        // ASSERT
//        assertNotNull(result);
//        assertEquals(2, result.getData().size());
//    }
//
//    /*
//     **********************************************************************************************
//     * CONTROLLER AUTHORIZATION TESTS
//     **********************************************************************************************
//     */
//
//    @Test
//    @DisplayName("getPickupLocationsInBatches - Verify @PreAuthorize Annotation")
//    void getPickupLocationsInBatches_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
//        Method method = PickupLocationController.class.getMethod("getPickupLocationsInBatches",
//                PaginationBaseRequestModel.class);
//        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
//        assertNotNull(annotation, "@PreAuthorize annotation should be present on getPickupLocationsInBatches");
//        assertTrue(annotation.value().contains(Authorizations.VIEW_PICKUP_LOCATIONS_PERMISSION),
//                "@PreAuthorize should reference VIEW_PICKUP_LOCATIONS_PERMISSION");
//    }
//
//    @Test
//    @DisplayName("getPickupLocationsInBatches - Controller delegates to service")
//    void getPickupLocationsInBatches_WithValidRequest_DelegatesToService() {
//        PickupLocationService mockService = mock(PickupLocationService.class);
//        PickupLocationController controller = new PickupLocationController(mockService);
//        PaginationBaseResponseModel<PickupLocationResponseModel> mockResponse = new PaginationBaseResponseModel<>();
//        when(mockService.getPickupLocationsInBatches(testPaginationRequest))
//                .thenReturn(mockResponse);
//
//        ResponseEntity<?> response = controller.getPickupLocationsInBatches(testPaginationRequest);
//
//        verify(mockService).getPickupLocationsInBatches(testPaginationRequest);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//    }
//}