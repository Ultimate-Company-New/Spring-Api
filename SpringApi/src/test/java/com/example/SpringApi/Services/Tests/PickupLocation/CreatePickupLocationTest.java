package com.example.SpringApi.Services.Tests.PickupLocation;

import com.example.SpringApi.Controllers.PickupLocationController;
import com.example.SpringApi.Services.PickupLocationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.RequestModels.PickupLocationRequestModel;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PickupLocationService.createPickupLocation() method.
 * Extensive validation of all fields and creation logic.
 * * Test Count: 28 tests
 */
@DisplayName("Create Pickup Location Tests")
class CreatePickupLocationTest extends PickupLocationServiceTestBase {

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
        testPickupLocationRequest.setAddressNickName("A");
        when(addressRepository.save(any())).thenReturn(testAddress);
        when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);
        lenient().when(shippingHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);

        assertDoesNotThrow(() -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
    }

    /**
     * Purpose: Verify basic success scenario where location is saved.
     * Expected Result: Pickup location and address are saved.
     * Assertions: Repositories are called the expected number of times.
     */
    @Test
    @DisplayName("Create Pickup Location - Success")
    void createPickupLocation_Success() throws Exception {
        when(addressRepository.save(any())).thenReturn(testAddress);
        when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);
        lenient().when(shippingHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);

        pickupLocationService.createPickupLocation(testPickupLocationRequest);

        verify(addressRepository, times(1)).save(any());
        verify(pickupLocationRepository, times(2)).save(any());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject null address object.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message contains ER001.
     */
    @Test
    @DisplayName("Create Pickup Location - Null Address - Throws BadRequestException")
    void createPickupLocation_NullAddress_ThrowsBadRequestException() {
        testPickupLocationRequest.setAddress(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
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
        testPickupLocationRequest.getAddress().setCity(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
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
        testPickupLocationRequest.getAddress().setCountry(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
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
        testPickupLocationRequest.setAddressNickName(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
        assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidAddressNickName, ex.getMessage());
    }

    /**
     * Purpose: Verify null phone is accepted as optional field.
     * Expected Result: Creation succeeds.
     * Assertions: No exception is thrown.
     */
    @Test
    @DisplayName("Create Pickup Location - Null Phone - Success")
    void createPickupLocation_NullPhone_Success() {
        testPickupLocationRequest.getAddress().setPhoneOnAddress(null);
        when(addressRepository.save(any())).thenReturn(testAddress);
        when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);
        lenient().when(shippingHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);

        assertDoesNotThrow(() -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
    }

    /**
     * Purpose: Reject address with null postal code.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message contains ER004.
     */
    @Test
    @DisplayName("Create Pickup Location - Null Postal Code - Throws BadRequestException")
    void createPickupLocation_NullPostalCode_ThrowsBadRequestException() {
        testPickupLocationRequest.getAddress().setPostalCode(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
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
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(null));
        assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidRequest, ex.getMessage());
    }

    /**
     * Purpose: Reject address with null state.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message contains ER003.
     */
    @Test
    @DisplayName("Create Pickup Location - Null State - Throws BadRequestException")
    void createPickupLocation_NullState_ThrowsBadRequestException() {
        testPickupLocationRequest.getAddress().setState(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
        assertEquals(ErrorMessages.AddressErrorMessages.ER003, ex.getMessage());
    }

    /**
     * Purpose: Reject address with null street address.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message contains ER001.
     */
    @Test
    @DisplayName("Create Pickup Location - Null Street Address - Throws BadRequestException")
    void createPickupLocation_NullStreetAddress_ThrowsBadRequestException() {
        testPickupLocationRequest.getAddress().setStreetAddress(null);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
        assertEquals(ErrorMessages.AddressErrorMessages.ER001, ex.getMessage());
    }

    /**
     * Purpose: Reject whitespace city.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Message contains ER002.
     */
    @Test
    @DisplayName("Create Pickup Location - Whitespace City - Throws BadRequestException")
    void createPickupLocation_WhitespaceCity_ThrowsBadRequestException() {
        testPickupLocationRequest.getAddress().setCity("   ");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
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
        testPickupLocationRequest.getAddress().setCountry("   ");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
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
        testPickupLocationRequest.setAddressNickName("   ");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
        assertEquals(ErrorMessages.PickupLocationErrorMessages.InvalidAddressNickName, ex.getMessage());
    }

    /**
     * Purpose: Verify whitespace phone is trimmed and accepted.
     * Expected Result: Creation succeeds.
     * Assertions: No exception is thrown.
     */
    @Test
    @DisplayName("Create Pickup Location - Whitespace Phone - Success")
    void createPickupLocation_WhitespacePhone_Success() {
        testPickupLocationRequest.getAddress().setPhoneOnAddress("   ");
        when(addressRepository.save(any())).thenReturn(testAddress);
        when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);
        lenient().when(shippingHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);

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
        testPickupLocationRequest.getAddress().setPostalCode("   ");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
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
        testPickupLocationRequest.getAddress().setState("   ");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
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
        testPickupLocationRequest.getAddress().setStreetAddress("   ");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pickupLocationService.createPickupLocation(testPickupLocationRequest));
        assertEquals(ErrorMessages.AddressErrorMessages.ER001, ex.getMessage());
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("createPickupLocation - Verify @PreAuthorize Annotation")
    void createPickupLocation_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        Method method = PickupLocationController.class.getMethod("createPickupLocation",
                PickupLocationRequestModel.class);
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
        assertNotNull(annotation, "@PreAuthorize annotation should be present");
        assertTrue(annotation.value().contains(Authorizations.INSERT_PICKUP_LOCATIONS_PERMISSION),
                "@PreAuthorize should reference INSERT_PICKUP_LOCATIONS_PERMISSION");
    }

    @Test
    @DisplayName("createPickupLocation - Controller delegates to service")
    void createPickupLocation_WithValidRequest_DelegatesToService() throws Exception {
        PickupLocationService mockService = mock(PickupLocationService.class);
        PickupLocationController controller = new PickupLocationController(mockService);
        doNothing().when(mockService).createPickupLocation(testPickupLocationRequest);

        ResponseEntity<?> response = controller.createPickupLocation(testPickupLocationRequest);

        verify(mockService).createPickupLocation(testPickupLocationRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}