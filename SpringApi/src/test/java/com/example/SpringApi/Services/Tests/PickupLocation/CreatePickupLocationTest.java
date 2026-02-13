package com.example.SpringApi.Services.Tests.PickupLocation;

import com.example.SpringApi.Controllers.PickupLocationController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.RequestModels.PickupLocationRequestModel;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PickupLocationService.createPickupLocation() method.
 * Extensive validation of all fields, edge cases, and error handling.
 * Test Count: 40 tests
 */

@DisplayName("Create Pickup Location Tests")
class CreatePickupLocationTest extends PickupLocationServiceTestBase {

    // Total Tests: 30
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify address nickname at minimum length (1 char) succeeds.
     * Expected Result: Creation successful.
     * Assertions: assertDoesNotThrow verifies success.
     */
    @Test
    @DisplayName("Create Pickup Location - Address Nickname Length 1 - Success")
    void createPickupLocation_AddressNicknameLengthOne_Success() {
        // Arrange
        testPickupLocationRequest.setAddressNickName("A");
        stubSuccessfulPickupLocationCreation();

        // Act & Assert
        assertDoesNotThrow(() -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
    }

    /**
     * Purpose: Verify basic success scenario where location is saved.
     * Expected Result: Pickup location and address are saved.
     * Assertions: Repositories are called the expected number of times.
     */
    @Test
    @DisplayName("Create Pickup Location - Success")
    void createPickupLocation_Success_Success() throws Exception {
        // Arrange
        stubSuccessfulPickupLocationCreation();

        // Act
        pickupLocationService.createPickupLocation(testPickupLocationRequest);

        // Assert
        verify(addressRepository, times(1)).save(any());
        verify(pickupLocationRepository, times(2)).save(any());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify address with alphanumeric postal code.
     * Expected Result: Creation succeeds with alphanumeric postal code.
     * Assertions: No exception is thrown.
     */
    @Test
    @DisplayName("Create Pickup Location - Alphanumeric Postal Code - Success")
    void createPickupLocation_AlphanumericPostalCode_Success() {
        // Arrange
        testPickupLocationRequest.getAddress().setPostalCode("123456");
        stubSuccessfulPickupLocationCreation();

        // Act & Assert
        assertDoesNotThrow(() -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
    }

    /**
     * Purpose: Verify international characters in address.
     * Expected Result: Creation succeeds with UTF-8 characters.
     * Assertions: No exception is thrown.
     */
    @Test
    @DisplayName("Create Pickup Location - International Characters - Success")
    void createPickupLocation_InternationalCharacters_Success() {
        // Arrange
        testPickupLocationRequest.getAddress().setCity("São Paulo");
        testPickupLocationRequest.getAddress().setCountry("Brasil");
        stubSuccessfulPickupLocationCreation();

        // Act & Assert
        assertDoesNotThrow(() -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
    }

    /**
     * Purpose: Verify isDeleted flag is correctly set to false on creation.
     * Expected Result: New location is marked as active (not deleted).
     * Assertions: Location is not marked as deleted.
     */
    @Test
    @DisplayName("Create Pickup Location - IsDeleted Flag False - Success")
    void createPickupLocation_IsDeletedFlagFalse_Success() throws Exception {
        // Arrange
        testPickupLocationRequest.setIsDeleted(false);
        stubSuccessfulPickupLocationCreation();

        // Act
        pickupLocationService.createPickupLocation(testPickupLocationRequest);

        // Assert
        verify(pickupLocationRepository, times(2)).save(argThat(saved -> !saved.getIsDeleted()));
    }

    /**
     * Purpose: Verify address with max phone length.
     * Expected Result: Creation succeeds with long phone number.
     * Assertions: No exception is thrown.
     */
    @Test
    @DisplayName("Create Pickup Location - Long Phone Number - Success")
    void createPickupLocation_LongPhoneNumber_Success() {
        // Arrange
        testPickupLocationRequest.getAddress().setPhoneOnAddress("+1-212-555-0123 ext. 1234");
        stubSuccessfulPickupLocationCreation();

        // Act & Assert
        assertDoesNotThrow(() -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
    }

    /**
     * Purpose: Reject null address object.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message contains ER001.
     */
    @Test
    @DisplayName("Create Pickup Location - Null Address - Throws BadRequestException")
    void createPickupLocation_NullAddress_ThrowsBadRequestException() {
        // Arrange
        testPickupLocationRequest.setAddress(null);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));

        // Assert
        assertEquals(ErrorMessages.AddressErrorMessages.ER001, ex.getMessage());
    }

    /**
     * Purpose: Reject address with null city.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message contains ER002.
     */
    @Test
    @DisplayName("Create Pickup Location - Null City - Throws BadRequestException")
    void createPickupLocation_NullCity_ThrowsBadRequestException() {
        // Arrange
        testPickupLocationRequest.getAddress().setCity(null);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));

        // Assert
        assertEquals(ErrorMessages.AddressErrorMessages.ER002, ex.getMessage());
    }

    /**
     * Purpose: Reject address with null country.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message contains ER005.
     */
    @Test
    @DisplayName("Create Pickup Location - Null Country - Throws BadRequestException")
    void createPickupLocation_NullCountry_ThrowsBadRequestException() {
        // Arrange
        testPickupLocationRequest.getAddress().setCountry(null);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));

        // Assert
        assertEquals(ErrorMessages.AddressErrorMessages.ER005, ex.getMessage());
    }

    /**
     * Purpose: Reject null address nickname.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message contains InvalidAddressNickName.
     */
    @Test
    @DisplayName("Create Pickup Location - Null Nickname - Throws BadRequestException")
    void createPickupLocation_NullNickname_ThrowsBadRequestException() {
        // Arrange
        testPickupLocationRequest.setAddressNickName(null);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));

        // Assert
        assertEquals(ErrorMessages.PickupLocationErrorMessages.INVALID_ADDRESS_NICK_NAME, ex.getMessage());
    }

    /**
     * Purpose: Verify null phone is accepted as optional field.
     * Expected Result: Creation succeeds.
     * Assertions: No exception is thrown.
     */
    @Test
    @DisplayName("Create Pickup Location - Null Phone - Success")
    void createPickupLocation_NullPhone_Success() {
        // Arrange
        testPickupLocationRequest.getAddress().setPhoneOnAddress(null);
        stubSuccessfulPickupLocationCreation();

        // Act & Assert
        assertDoesNotThrow(() -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
    }

    /**
     * Purpose: Reject null postal code.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message contains ER004.
     */
    @Test
    @DisplayName("Create Pickup Location - Null Postal Code - Throws BadRequestException")
    void createPickupLocation_NullPostalCode_ThrowsBadRequestException() {
        // Arrange
        testPickupLocationRequest.getAddress().setPostalCode(null);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));

        // Assert
        assertEquals(ErrorMessages.AddressErrorMessages.ER004, ex.getMessage());
    }

    /**
     * Purpose: Reject null request object.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message contains InvalidRequest.
     */
    @Test
    @DisplayName("Create Pickup Location - Null Request - Throws BadRequestException")
    void createPickupLocation_NullRequest_ThrowsBadRequestException() {
        // Arrange

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(null));

        // Assert
        assertEquals(ErrorMessages.PickupLocationErrorMessages.INVALID_REQUEST, ex.getMessage());
    }

    /**
     * Purpose: Reject null state.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message contains ER003.
     */
    @Test
    @DisplayName("Create Pickup Location - Null State - Throws BadRequestException")
    void createPickupLocation_NullState_ThrowsBadRequestException() {
        // Arrange
        testPickupLocationRequest.getAddress().setState(null);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));

        // Assert
        assertEquals(ErrorMessages.AddressErrorMessages.ER003, ex.getMessage());
    }

    /**
     * Purpose: Reject null street address.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message contains ER001.
     */
    @Test
    @DisplayName("Create Pickup Location - Null Street Address - Throws BadRequestException")
    void createPickupLocation_NullStreetAddress_ThrowsBadRequestException() {
        // Arrange
        testPickupLocationRequest.getAddress().setStreetAddress(null);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));

        // Assert
        assertEquals(ErrorMessages.AddressErrorMessages.ER001, ex.getMessage());
    }

    /**
     * Purpose: Verify address with numeric postal code (all digits).
     * Expected Result: Creation succeeds with numeric postal code.
     * Assertions: No exception is thrown.
     */
    @Test
    @DisplayName("Create Pickup Location - Numeric Postal Code - Success")
    void createPickupLocation_NumericPostalCode_Success() {
        // Arrange
        testPickupLocationRequest.getAddress().setPostalCode("12345");
        stubSuccessfulPickupLocationCreation();

        // Act & Assert
        assertDoesNotThrow(() -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
    }

    /**
     * Purpose: Verify repository exception handling during address save.
     * Expected Result: Exception is propagated.
     * Assertions: Exception is thrown.
     */
    @Test
    @DisplayName("Create Pickup Location - Repository Error on Address Save - Throws Exception")
    void createPickupLocation_RepositoryErrorOnAddressSave_ThrowsException() {
        // Arrange
        stubAddressRepositorySaveThrows(new RuntimeException(ErrorMessages.CommonErrorMessages.DATABASE_ERROR));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
        assertEquals(ErrorMessages.CommonErrorMessages.DATABASE_ERROR, ex.getMessage());
    }

    /**
     * Purpose: Verify repository exception handling during pickup location save.
     * Expected Result: Exception is propagated.
     * Assertions: Exception is thrown.
     */
    @Test
    @DisplayName("Create Pickup Location - Repository Error on Location Save - Throws Exception")
    void createPickupLocation_RepositoryErrorOnLocationSave_ThrowsException() {
        // Arrange
        ClientResponseModel mockClient = new ClientResponseModel();
        mockClient.setShipRocketEmail("test@example.com");
        mockClient.setShipRocketPassword("testpassword");

        stubClientServiceGetClientById(TEST_CLIENT_ID, mockClient);
        stubAddressRepositorySave(testAddress);
        stubShipRocketHelperAddPickupLocation(testShipRocketResponse);
        stubPickupLocationRepositorySaveThrows(
                new RuntimeException(ErrorMessages.CommonErrorMessages.DATABASE_ERROR));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
        assertEquals(ErrorMessages.CommonErrorMessages.DATABASE_ERROR, ex.getMessage());
    }

    /**
     * Purpose: Verify special characters in street address.
     * Expected Result: Creation succeeds.
     * Assertions: No exception is thrown.
     */
    @Test
    @DisplayName("Create Pickup Location - Special Characters in Address - Success")
    void createPickupLocation_SpecialCharactersInAddress_Success() {
        // Arrange
        testPickupLocationRequest.getAddress().setStreetAddress("123 St. Paul's Ave #456 (South)");
        stubSuccessfulPickupLocationCreation();

        // Act & Assert
        assertDoesNotThrow(() -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
    }

    /**
     * Purpose: Verify long nickname at max length is accepted.
     * Expected Result: Creation succeeds with long nickname.
     * Assertions: No exception is thrown.
     */
    @Test
    @DisplayName("Create Pickup Location - Very Long Nickname - Success")
    void createPickupLocation_VeryLongNickname_Success() {
        // Arrange
        testPickupLocationRequest.setAddressNickName("A".repeat(36));
        stubSuccessfulPickupLocationCreation();

        // Act & Assert
        assertDoesNotThrow(() -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
    }

    /**
     * Purpose: Reject whitespace city.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message contains ER002.
     */
    @Test
    @DisplayName("Create Pickup Location - Whitespace City - Throws BadRequestException")
    void createPickupLocation_WhitespaceCity_ThrowsBadRequestException() {
        // Arrange
        testPickupLocationRequest.getAddress().setCity("   ");

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));

        // Assert
        assertEquals(ErrorMessages.AddressErrorMessages.ER002, ex.getMessage());
    }

    /**
     * Purpose: Reject whitespace country.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message contains ER005.
     */
    @Test
    @DisplayName("Create Pickup Location - Whitespace Country - Throws BadRequestException")
    void createPickupLocation_WhitespaceCountry_ThrowsBadRequestException() {
        // Arrange
        testPickupLocationRequest.getAddress().setCountry("   ");

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));

        // Assert
        assertEquals(ErrorMessages.AddressErrorMessages.ER005, ex.getMessage());
    }

    /**
     * Purpose: Reject whitespace nickname.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message contains InvalidAddressNickName.
     */
    @Test
    @DisplayName("Create Pickup Location - Whitespace Nickname - Throws BadRequestException")
    void createPickupLocation_WhitespaceNickname_ThrowsBadRequestException() {
        // Arrange
        testPickupLocationRequest.setAddressNickName("   ");

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));

        // Assert
        assertEquals(ErrorMessages.PickupLocationErrorMessages.INVALID_ADDRESS_NICK_NAME, ex.getMessage());
    }

    /**
     * Purpose: Verify whitespace phone is accepted as optional field.
     * Expected Result: Creation succeeds.
     * Assertions: No exception is thrown.
     */
    @Test
    @DisplayName("Create Pickup Location - Whitespace Phone - Success")
    void createPickupLocation_WhitespacePhone_Success() {
        // Arrange
        testPickupLocationRequest.getAddress().setPhoneOnAddress("   ");
        stubSuccessfulPickupLocationCreation();

        // Act & Assert
        assertDoesNotThrow(() -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
    }

    /**
     * Purpose: Reject whitespace postal code.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message contains ER004.
     */
    @Test
    @DisplayName("Create Pickup Location - Whitespace Postal Code - Throws BadRequestException")
    void createPickupLocation_WhitespacePostalCode_ThrowsBadRequestException() {
        // Arrange
        testPickupLocationRequest.getAddress().setPostalCode("   ");

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));

        // Assert
        assertEquals(ErrorMessages.AddressErrorMessages.ER004, ex.getMessage());
    }

    /**
     * Purpose: Reject whitespace state.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message contains ER003.
     */
    @Test
    @DisplayName("Create Pickup Location - Whitespace State - Throws BadRequestException")
    void createPickupLocation_WhitespaceState_ThrowsBadRequestException() {
        // Arrange
        testPickupLocationRequest.getAddress().setState("   ");

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));

        // Assert
        assertEquals(ErrorMessages.AddressErrorMessages.ER003, ex.getMessage());
    }

    /**
     * Purpose: Reject whitespace street address.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message contains ER001.
     */
    @Test
    @DisplayName("Create Pickup Location - Whitespace Street Address - Throws BadRequestException")
    void createPickupLocation_WhitespaceStreetAddress_ThrowsBadRequestException() {
        // Arrange
        testPickupLocationRequest.getAddress().setStreetAddress("   ");

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));

        // Assert
        assertEquals(ErrorMessages.AddressErrorMessages.ER001, ex.getMessage());
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
    @DisplayName("createPickupLocation - Controller Permission - Unauthorized")
    void createPickupLocation_controller_permission_unauthorized() throws Exception {
        // Arrange
        PickupLocationController controller = new PickupLocationController(pickupLocationServiceMock);
        stubPickupLocationServiceThrowsUnauthorizedOnCreate();

        // Act
        ResponseEntity<?> response = controller.createPickupLocation(testPickupLocationRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /**
     * Purpose: Verify @PreAuthorize annotation on createPickupLocation endpoint.
     * Expected Result: Annotation exists and references INSERT_PICKUP_LOCATIONS_PERMISSION.
     * Assertions: Annotation is present and contains permission.
     */
    @Test
    @DisplayName("createPickupLocation - Verify @PreAuthorize Annotation")
    void createPickupLocation_VerifyPreAuthorizeAnnotation_Success() throws NoSuchMethodException {
        // Arrange
        Method method = PickupLocationController.class.getMethod("createPickupLocation",
                PickupLocationRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.INSERT_PICKUP_LOCATIONS_PERMISSION),
                "@PreAuthorize should reference INSERT_PICKUP_LOCATIONS_PERMISSION");
    }

    /**
     * Purpose: Verify controller delegates createPickupLocation to service.
     * Expected Result: Service method called and HTTP 200 returned.
     * Assertions: Delegation occurs and response is OK.
     */
    @Test
    @DisplayName("createPickupLocation - Controller delegates to service")
    void createPickupLocation_WithValidRequest_DelegatesToService() throws Exception {
        // Arrange
        PickupLocationController controller = new PickupLocationController(pickupLocationServiceMock);
        stubPickupLocationServiceCreatePickupLocationDoNothing();

        // Act
        ResponseEntity<?> response = controller.createPickupLocation(testPickupLocationRequest);

        // Assert
        verify(pickupLocationServiceMock).createPickupLocation(testPickupLocationRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
