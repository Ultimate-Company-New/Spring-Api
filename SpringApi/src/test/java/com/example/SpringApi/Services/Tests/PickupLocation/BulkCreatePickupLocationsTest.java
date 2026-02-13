package com.example.SpringApi.Services.Tests.PickupLocation;

import com.example.SpringApi.Controllers.PickupLocationController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.RequestModels.PickupLocationRequestModel;
import com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PickupLocationService.bulkCreatePickupLocations() method.
 * Covers bulk insertion success, partial success, edge cases, and validation failures.
 */
@DisplayName("Bulk Create Pickup Locations Tests")
class BulkCreatePickupLocationsTest extends PickupLocationServiceTestBase {

    // Total Tests: 22
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
        // Arrange
        List<PickupLocationRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            requests.add(createValidPickupLocationRequest((long) i));
        }
        stubAddressRepositorySave(testAddress);
        stubPickupLocationRepositorySave(testPickupLocation);
        stubShipRocketHelperAddPickupLocation(testShipRocketResponse);

        // Act
        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);

        // Assert
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
        // Arrange
        List<PickupLocationRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            requests.add(createValidPickupLocationRequest((long) i));
        }
        stubAddressRepositorySave(testAddress);
        stubPickupLocationRepositorySave(testPickupLocation);
        stubShipRocketHelperAddPickupLocation(testShipRocketResponse);

        // Act
        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);

        // Assert
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
        // Arrange
        List<PickupLocationRequestModel> requests = new ArrayList<>();
        requests.add(createValidPickupLocationRequest(1L));

        PickupLocationRequestModel invalidReq = createValidPickupLocationRequest(2L);
        invalidReq.setAddressNickName("");
        requests.add(invalidReq);

        requests.add(createValidPickupLocationRequest(3L));
        stubAddressRepositorySave(testAddress);
        stubPickupLocationRepositorySave(testPickupLocation);
        stubShipRocketHelperAddPickupLocation(testShipRocketResponse);

        // Act
        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);

        // Assert
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
        // Arrange
        List<PickupLocationRequestModel> requests = List.of(createValidPickupLocationRequest(1L));
        stubAddressRepositorySave(testAddress);
        stubPickupLocationRepositorySave(testPickupLocation);
        stubShipRocketHelperAddPickupLocation(testShipRocketResponse);

        // Act
        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);

        // Assert
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
        // Arrange
        List<PickupLocationRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PickupLocationRequestModel req = createValidPickupLocationRequest((long) i);
            req.setAddressNickName("");
            requests.add(req);
        }

        // Act
        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);

        // Assert
        assertEquals(5, result.getFailureCount());
        verify(pickupLocationRepository, never()).save(any());
    }

    /**
     * Purpose: Test batch size of exactly 100 (potential limit).
     * Expected Result: All items processed successfully.
     * Assertions: Success count is 100.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - Batch 100 Items - Success")
    void bulkCreatePickupLocations_Batch100Items_Success() {
        // Arrange
        List<PickupLocationRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            requests.add(createValidPickupLocationRequest((long) i));
        }
        stubAddressRepositorySave(testAddress);
        stubPickupLocationRepositorySave(testPickupLocation);

        // Act
        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);

        // Assert
        assertEquals(100, result.getSuccessCount());
    }

    /**
     * Purpose: Handle database runtime error during processing.
     * Expected Result: Exception is caught and recorded as failure.
     * Assertions: Failure count increments.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - Database Error - Records Failure")
    void bulkCreatePickupLocations_DatabaseError_RecordsFailure() {
        // Arrange
        List<PickupLocationRequestModel> requests = List.of(createValidPickupLocationRequest(1L));
        stubAddressRepositorySaveThrows(new RuntimeException("DB Error"));

        // Act
        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);

        // Assert
        assertEquals(1, result.getFailureCount());
        assertEquals(0, result.getSuccessCount());
    }

    /**
     * Purpose: Handle batch with duplicate IDs.
     * Expected Result: System processes all items, duplicates may result in errors.
     * Assertions: Result reflects processing.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - Duplicate IDs - Success")
    void bulkCreatePickupLocations_DuplicateIds_Success() {
        // Arrange
        List<PickupLocationRequestModel> requests = new ArrayList<>();
        requests.add(createValidPickupLocationRequest(1L));
        requests.add(createValidPickupLocationRequest(1L));
        stubAddressRepositorySave(testAddress);
        stubPickupLocationRepositorySave(testPickupLocation);

        // Act
        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);

        // Assert
        assertNotNull(result);
    }

    /**
     * Purpose: Reject empty list input.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message indicates list cannot be empty.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - Empty List - Throws BadRequestException")
    void bulkCreatePickupLocations_EmptyList_ThrowsBadRequestException() {
        // Arrange
        List<PickupLocationRequestModel> requests = new ArrayList<>();

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.bulkCreatePickupLocations(requests));

        // Assert
        assertEquals(String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "Pickup location"),
                ex.getMessage());
    }

    /**
     * Purpose: Test batch with international addresses.
     * Expected Result: International addresses are accepted.
     * Assertions: Success count matches valid items.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - International Addresses - Success")
    void bulkCreatePickupLocations_InternationalAddresses_Success() {
        // Arrange
        List<PickupLocationRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            PickupLocationRequestModel req = createValidPickupLocationRequest((long) i);
            req.getAddress().setCity("São Paulo");
            req.getAddress().setCountry("Brasil");
            requests.add(req);
        }
        stubAddressRepositorySave(testAddress);
        stubPickupLocationRepositorySave(testPickupLocation);

        // Act
        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);

        // Assert
        assertEquals(2, result.getSuccessCount());
    }

    /**
     * Purpose: Test with very long address nicknames.
     * Expected Result: All items processed successfully.
     * Assertions: Success count matches total.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - Long Nicknames - Partial Success")
    void bulkCreatePickupLocations_LongNicknames_PartialSuccess() {
        // Arrange
        List<PickupLocationRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            PickupLocationRequestModel req = createValidPickupLocationRequest((long) i);
            StringBuilder longName = new StringBuilder();
            for (int j = 0; j < 100; j++) {
                longName.append("A");
            }
            req.setAddressNickName(longName.toString());
            requests.add(req);
        }
        stubAddressRepositorySave(testAddress);
        stubPickupLocationRepositorySave(testPickupLocation);

        // Act
        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);

        // Assert
        assertNotNull(result);
    }

    /**
     * Purpose: Reject items with missing address object.
     * Expected Result: Item fails validation.
     * Assertions: Failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - Missing Address Object - Fails")
    void bulkCreatePickupLocations_MissingAddressObject_Fails() {
        // Arrange
        PickupLocationRequestModel req = createValidPickupLocationRequest(1L);
        req.setAddress(null);

        // Act
        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(List.of(req));

        // Assert
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
        // Arrange
        PickupLocationRequestModel req = createValidPickupLocationRequest(1L);
        req.getAddress().setCity(null);

        // Act
        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(List.of(req));

        // Assert
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
        // Arrange
        PickupLocationRequestModel req = createValidPickupLocationRequest(1L);
        req.getAddress().setPhoneOnAddress(null);

        // Act
        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(List.of(req));

        // Assert
        assertEquals(1, result.getFailureCount());
    }

    /**
     * Purpose: Handle mixed valid and invalid ShipRocket IDs.
     * Expected Result: Valid items succeed, invalid items fail.
     * Assertions: Partial success recorded.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - Mixed ShipRocket IDs - Partial Success")
    void bulkCreatePickupLocations_MixedShipRocketIds_PartialSuccess() {
        // Arrange
        List<PickupLocationRequestModel> requests = new ArrayList<>();
        requests.add(createValidPickupLocationRequest(1L));

        PickupLocationRequestModel invalidReq = createValidPickupLocationRequest(2L);
        invalidReq.setShipRocketPickupLocationId(-100L);
        requests.add(invalidReq);
        stubAddressRepositorySave(testAddress);
        stubPickupLocationRepositorySave(testPickupLocation);

        // Act
        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);

        // Assert
        assertEquals(2, result.getTotalRequested());
    }

    /**
     * Purpose: Reject items with negative ShipRocket ID.
     * Expected Result: Item fails validation.
     * Assertions: Failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - Negative ShipRocket ID - Fails")
    void bulkCreatePickupLocations_NegativeShipRocketId_Fails() {
        // Arrange
        PickupLocationRequestModel req = createValidPickupLocationRequest(1L);
        req.setShipRocketPickupLocationId(-1L);

        // Act
        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(List.of(req));

        // Assert
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
        // Arrange

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.bulkCreatePickupLocations(null));

        // Assert
        assertEquals(String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "Pickup location"),
                ex.getMessage());
    }

    /**
     * Purpose: Reject items with null nickname.
     * Expected Result: Item fails validation.
     * Assertions: Failure count is 1.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - Null Nickname - Fails")
    void bulkCreatePickupLocations_NullNickname_Fails() {
        // Arrange
        PickupLocationRequestModel req = createValidPickupLocationRequest(1L);
        req.setAddressNickName(null);

        // Act
        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(List.of(req));

        // Assert
        assertEquals(1, result.getFailureCount());
    }

    /**
     * Purpose: Test with request returning null result.
     * Expected Result: Handled gracefully without null pointer.
     * Assertions: No exception thrown.
     */
    @Test
    @DisplayName("Bulk Create Pickup Locations - Repository Returns Null - Graceful Handling")
    void bulkCreatePickupLocations_RepositoryReturnsNull_GracefulHandling() {
        // Arrange
        List<PickupLocationRequestModel> requests = List.of(createValidPickupLocationRequest(1L));
        stubAddressRepositorySaveReturnsNull();

        // Act
        BulkInsertResponseModel<Long> result = pickupLocationService.bulkCreatePickupLocations(requests);

        // Assert
        assertNotNull(result);
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
    @DisplayName("bulkCreatePickupLocations - Controller Permission - Unauthorized")
    void bulkCreatePickupLocations_controller_permission_unauthorized() {
        // Arrange
        PickupLocationController controller = new PickupLocationController(pickupLocationServiceMock);
        // Setup user context to return normally (so getUserId() doesn't throw)
        stubPickupLocationServiceUserContext(TEST_USER_ID, "testuser", TEST_CLIENT_ID);
        // Then make the actual service method throw
        stubPickupLocationServiceThrowsUnauthorized();

        // Act
        ResponseEntity<?> response = controller.bulkCreatePickupLocations(new ArrayList<>());

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify @PreAuthorize annotation on bulkCreatePickupLocations endpoint.
     * Expected Result: Annotation exists and references INSERT_PICKUP_LOCATIONS_PERMISSION.
     * Assertions: Annotation is present and contains permission.
     */
    @Test
    @DisplayName("bulkCreatePickupLocations - Verify @PreAuthorize Annotation")
    void bulkCreatePickupLocations_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        Method method = PickupLocationController.class.getMethod("bulkCreatePickupLocations", List.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present on bulkCreatePickupLocations");
        assertTrue(annotation.value().contains(Authorizations.INSERT_PICKUP_LOCATIONS_PERMISSION),
                "@PreAuthorize should reference INSERT_PICKUP_LOCATIONS_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates to async service call for valid requests.
     * Expected Result: Service method is invoked and HTTP 200 returned.
     * Assertions: Service called once and status code is OK.
     */
    @Test
    @DisplayName("bulkCreatePickupLocations - Controller delegates to service")
    void bulkCreatePickupLocations_WithValidRequests_DelegatesToService() {
        // Arrange
        PickupLocationController controller = new PickupLocationController(pickupLocationServiceMock);
        List<PickupLocationRequestModel> requests = List.of(createValidPickupLocationRequest(1L));
        stubPickupLocationServiceUserContext(1L, "testuser", TEST_CLIENT_ID);
        stubPickupLocationServiceBulkCreatePickupLocationsAsyncDoNothing();

        // Act
        ResponseEntity<?> response = controller.bulkCreatePickupLocations(requests);

        // Assert
        verify(pickupLocationServiceMock).bulkCreatePickupLocationsAsync(eq(requests), eq(1L), eq("testuser"),
                eq(TEST_CLIENT_ID));
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
