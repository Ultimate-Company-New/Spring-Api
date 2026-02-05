package com.example.SpringApi.Services.Tests.PickupLocation;

import com.example.SpringApi.Controllers.PickupLocationController;
import com.example.SpringApi.Models.RequestModels.PickupLocationRequestModel;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.BadRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PickupLocationService.bulkCreatePickupLocations() method.
 * Covers bulk insertion success, partial success, and validation failures.
 * * Test Count: 14 tests
 */
@DisplayName("Bulk Create Pickup Locations Tests")
class BulkCreatePickupLocationsTest extends PickupLocationServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify successful bulk creation when all items are valid.
     * Expected Result: Result indicates 100% success.
     * Assertions: Success count equals input size.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - All Valid - Success")
    void bulkCreatePickupLocations_AllValid_Success() {
        List<PickupLocationRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            requests.add(createValidPickupLocationRequest((long) i, TEST_CLIENT_ID));
        }

        when(addressRepository.save(any())).thenReturn(testAddress);
        when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);
        lenient().when(shippingHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);

        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);

        assertNotNull(result);
        assertEquals(3, result.getTotalRequested());
        assertEquals(3, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
    }

    /**
     * Purpose: Verify system handles a large batch of 50 locations.
     * Expected Result: All locations processed successfully.
     * Assertions: Success count is 50.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - Large Batch - Success")
    void bulkCreatePickupLocations_LargeBatch_Success() {
        List<PickupLocationRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            requests.add(createValidPickupLocationRequest((long) i, TEST_CLIENT_ID));
        }

        when(addressRepository.save(any())).thenReturn(testAddress);
        when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);
        lenient().when(shippingHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);

        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);

        assertEquals(50, result.getSuccessCount());
    }

    /**
     * Purpose: Verify partial success when some items fail validation.
     * Expected Result: Valid items saved, invalid reported as failures.
     * Assertions: Counts reflect mixed results.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - Mixed Invalid and Valid - Partial Success")
    void bulkCreatePickupLocations_MixedInvalidAndValid_PartialSuccess() {
        List<PickupLocationRequestModel> requests = new ArrayList<>();
        requests.add(createValidPickupLocationRequest(1L, TEST_CLIENT_ID)); // Valid
        
        PickupLocationRequestModel invalidReq = createValidPickupLocationRequest(2L, TEST_CLIENT_ID);
        invalidReq.setAddressNickName(""); // Invalid
        requests.add(invalidReq);
        
        requests.add(createValidPickupLocationRequest(3L, TEST_CLIENT_ID)); // Valid

        when(addressRepository.save(any())).thenReturn(testAddress);
        when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);
        lenient().when(shippingHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);

        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);

        assertEquals(3, result.getTotalRequested());
        assertEquals(2, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
    }



    /**
     * Purpose: Verify creation of a single valid location via bulk method.
     * Expected Result: Single success recorded.
     * Assertions: Success count is 1.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - Single Valid Item - Success")
    void bulkCreatePickupLocations_SingleValidItem_Success() {
        List<PickupLocationRequestModel> requests = List.of(createValidPickupLocationRequest(1L, TEST_CLIENT_ID));
        
        when(addressRepository.save(any())).thenReturn(testAddress);
        when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);
        lenient().when(shippingHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);

        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);

        assertEquals(1, result.getSuccessCount());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify failure when all items have invalid addresses.
     * Expected Result: All items fail.
     * Assertions: Failure count equals input size.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - All Invalid Addresses - All Fail")
    void bulkCreatePickupLocations_AllInvalidAddresses_AllFail() {
        List<PickupLocationRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PickupLocationRequestModel req = createValidPickupLocationRequest((long) i, TEST_CLIENT_ID);
            req.setAddressNickName(""); // Invalid
            requests.add(req);
        }

        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);

        assertEquals(5, result.getFailureCount());
        verify(pickupLocationRepository, never()).save(any());
    }

    /**
     * Purpose: Handle database runtime error during processing.
     * Expected Result: Exception is caught and recorded as failure.
     * Assertions: Failure count increments.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - Database Error - Records Failure")
    void bulkCreatePickupLocations_DatabaseError_RecordsFailure() {
        List<PickupLocationRequestModel> requests = List.of(createValidPickupLocationRequest(1L, TEST_CLIENT_ID));
        when(addressRepository.save(any())).thenThrow(new RuntimeException("DB Error"));

        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);

        assertEquals(1, result.getFailureCount());
        assertEquals(0, result.getSuccessCount());
    }

    /**
     * Purpose: Reject empty list input.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message indicates list cannot be empty.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - Empty List - Throws BadRequestException")
    void bulkCreatePickupLocations_EmptyList_ThrowsBadRequestException() {
        List<PickupLocationRequestModel> requests = new ArrayList<>();
        BadRequestException ex = assertThrows(BadRequestException.class, 
            () -> pickupLocationService.bulkCreatePickupLocations(requests));
        assertTrue(ex.getMessage().contains("list cannot be null or empty"));
    }

    /**
     * Purpose: Reject items with missing address object.
     * Expected Result: Item fails validation.
     * Assertions: Failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - Missing Address Object - Fails")
    void bulkCreatePickupLocations_MissingAddressObject_Fails() {
        PickupLocationRequestModel req = createValidPickupLocationRequest(1L, TEST_CLIENT_ID);
        req.setAddress(null);
        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(List.of(req));
        assertEquals(1, result.getFailureCount());
    }

    /**
     * Purpose: Reject items with missing city in address.
     * Expected Result: Item fails validation.
     * Assertions: Failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - Missing City - Fails")
    void bulkCreatePickupLocations_MissingCity_Fails() {
        PickupLocationRequestModel req = createValidPickupLocationRequest(1L, TEST_CLIENT_ID);
        req.getAddress().setCity(null);
        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(List.of(req));
        assertEquals(1, result.getFailureCount());
    }

    /**
     * Purpose: Reject items with missing phone in address.
     * Expected Result: Item fails validation.
     * Assertions: Failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - Missing Phone - Fails")
    void bulkCreatePickupLocations_MissingPhone_Fails() {
        PickupLocationRequestModel req = createValidPickupLocationRequest(1L, TEST_CLIENT_ID);
        req.getAddress().setPhoneOnAddress(null);
        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(List.of(req));
        assertEquals(1, result.getFailureCount());
    }

    /**
     * Purpose: Reject items with null nickname.
     * Expected Result: Item fails validation.
     * Assertions: Failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - Null Nickname - Fails")
    void bulkCreatePickupLocations_NullNickname_Fails() {
        PickupLocationRequestModel req = createValidPickupLocationRequest(1L, TEST_CLIENT_ID);
        req.setAddressNickName(null);
        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(List.of(req));
        assertEquals(1, result.getFailureCount());
    }

    /**
     * Purpose: Reject null list input.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message indicates list cannot be null.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - Null List - Throws BadRequestException")
    void bulkCreatePickupLocations_NullList_ThrowsBadRequestException() {
        BadRequestException ex = assertThrows(BadRequestException.class, 
            () -> pickupLocationService.bulkCreatePickupLocations(null));
        assertTrue(ex.getMessage().contains("list cannot be null or empty"));
    }

    /**
     * Purpose: Reject items with negative ShipRocket ID.
     * Expected Result: Item fails validation.
     * Assertions: Failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - Negative ShipRocket ID - Fails")
    void bulkCreatePickupLocations_NegativeShipRocketId_Fails() {
        PickupLocationRequestModel req = createValidPickupLocationRequest(1L, TEST_CLIENT_ID);
        req.setShipRocketPickupLocationId(-1L);
        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(List.of(req));
        assertEquals(1, result.getFailureCount());
    }

    private PickupLocationRequestModel createValidPickupLocationRequest(Long id, Long clientId) {
        AddressRequestModel address = new AddressRequestModel();
        address.setAddressType("WAREHOUSE");
        address.setStreetAddress("123 Street");
        address.setCity("City");
        address.setState("State");
        address.setPostalCode("12345");
        address.setCountry("Country");
        address.setPhoneOnAddress("1234567890");

        PickupLocationRequestModel req = new PickupLocationRequestModel();
        req.setPickupLocationId(id);
        req.setAddressNickName("Loc" + id);
        req.setAddress(address);
        req.setIsDeleted(false);
        return req;
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("bulkCreatePickupLocations - Verify @PreAuthorize Annotation")
    void bulkCreatePickupLocations_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PickupLocationController.class.getMethod("bulkCreatePickupLocations", List.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present on bulkCreatePickupLocations");
        assertTrue(annotation.value().contains(Authorizations.INSERT_PICKUP_LOCATIONS_PERMISSION),
                "@PreAuthorize should reference INSERT_PICKUP_LOCATIONS_PERMISSION");
    }

    @Test
    @DisplayName("bulkCreatePickupLocations - Controller delegates to service")
    void bulkCreatePickupLocations_WithValidRequests_DelegatesToService() {
        PickupLocationController controller = new PickupLocationController(pickupLocationService);
        List<PickupLocationRequestModel> requests = List.of(createValidPickupLocationRequest(1L, TEST_CLIENT_ID));
        BulkInsertResponseModel<Long> mockResponse = new BulkInsertResponseModel<>();
        when(pickupLocationService.bulkCreatePickupLocations(requests)).thenReturn(mockResponse);

        ResponseEntity<?> response = controller.bulkCreatePickupLocations(requests);

        verify(pickupLocationService).bulkCreatePickupLocations(requests);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
}