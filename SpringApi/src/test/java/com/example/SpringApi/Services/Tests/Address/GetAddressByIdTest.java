package com.example.SpringApi.Services.Tests.Address;

import com.example.SpringApi.Models.ResponseModels.AddressResponseModel;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.ErrorMessages;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AddressService.getAddressById() method.
 * Tests retrieval of address by ID with various scenarios.
 * * Test Count: 9 tests
 */
@DisplayName("Get Address By ID Tests")
class GetAddressByIdTest extends AddressServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify address details are returned for a valid ID.
     * Expected Result: Response contains address fields.
     * Assertions: Response fields match the test address.
     */
    @Test
    @DisplayName("Get Address By ID - Address found - Success returns address details")
    void getAddressById_AddressFound_Success() {
        // Arrange
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

        // Act
        AddressResponseModel result = addressService.getAddressById(DEFAULT_ADDRESS_ID);

        // Assert
        assertNotNull(result);
        assertEquals(DEFAULT_ADDRESS_ID, result.getAddressId());
        assertEquals(DEFAULT_USER_ID, result.getUserId());
        assertEquals(DEFAULT_CLIENT_ID, result.getClientId());
        assertEquals(DEFAULT_ADDRESS_TYPE, result.getAddressType());
        assertEquals(DEFAULT_STREET_ADDRESS, result.getStreetAddress());
        assertEquals(DEFAULT_CITY, result.getCity());
        assertEquals(DEFAULT_STATE, result.getState());
        assertEquals(DEFAULT_POSTAL_CODE, result.getPostalCode());
        assertEquals(DEFAULT_COUNTRY, result.getCountry());
        assertTrue(result.getIsPrimary());
        assertFalse(result.getIsDeleted());

        verify(addressRepository, times(1)).findById(DEFAULT_ADDRESS_ID);
    }

    /**
     * Purpose: Verify null optional fields are handled.
     * Expected Result: Response contains nulls for optional fields.
     * Assertions: Optional fields are null in response.
     */
    @Test
    @DisplayName("Get Address By ID - Address with null optional fields - Success")
    void getAddressById_NullOptionalFields_Success() {
        // Arrange
        testAddress.setStreetAddress2(null);
        testAddress.setStreetAddress3(null);
        testAddress.setNameOnAddress(null);
        testAddress.setEmailOnAddress(null);
        testAddress.setPhoneOnAddress(null);
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

        // Act
        AddressResponseModel result = addressService.getAddressById(DEFAULT_ADDRESS_ID);

        // Assert
        assertNotNull(result);
        assertNull(result.getStreetAddress2());
        assertNull(result.getStreetAddress3());
    }

    /**
     * Purpose: Verify permission check is performed for VIEW_ADDRESS permission.
     * Expected Result: Authorization service is called to check permissions.
     * Assertions: authorization.hasAuthority() is called with correct permission.
     */
    @Test
    @DisplayName("Get Address By ID - Permission check - Success Verifies Authorization")
    void getAddressById_PermissionCheck_SuccessVerifiesAuthorization() {
        // Arrange
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        lenient().when(authorization.hasAuthority(Authorizations.VIEW_ADDRESS_PERMISSION)).thenReturn(true);

        // Act
        addressService.getAddressById(DEFAULT_ADDRESS_ID);

        // Assert
        verify(authorization, times(1)).hasAuthority(Authorizations.VIEW_ADDRESS_PERMISSION);
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Validate missing address ID returns not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound.
     */
    @Test
    @DisplayName("Get Address By ID - Address not found - ThrowsNotFoundException")
    void getAddressById_AddressNotFound_ThrowsNotFoundException() {
        // Arrange
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.getAddressById(DEFAULT_ADDRESS_ID));

        assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        verify(addressRepository, times(1)).findById(DEFAULT_ADDRESS_ID);
    }

    /**
     * Purpose: Verify deleted address returns not found.
     * Expected Result: NotFoundException is thrown when address is deleted.
     * Assertions: Error message matches NotFound.
     */
    @Test
    @DisplayName("Get Address By ID - Deleted address - ThrowsNotFoundException")
    void getAddressById_DeletedAddress_ThrowsNotFoundException() {
        // Arrange
        testAddress.setIsDeleted(true);
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.AddressErrorMessages.NotFound, () -> addressService.getAddressById(DEFAULT_ADDRESS_ID));
        verify(addressRepository, times(1)).findById(DEFAULT_ADDRESS_ID);
    }

    /**
     * Purpose: Validate max long ID is rejected when not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound.
     */
    @Test
    @DisplayName("Get Address By ID - Max Long value - ThrowsNotFoundException")
    void getAddressById_MaxLongValue_ThrowsNotFoundException() {
        when(addressRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());
        assertThrowsNotFound(ErrorMessages.AddressErrorMessages.NotFound, () -> addressService.getAddressById(Long.MAX_VALUE));
    }

    /**
     * Purpose: Validate min long ID is rejected when not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound.
     */
    @Test
    @DisplayName("Get Address By ID - Min Long value - ThrowsNotFoundException")
    void getAddressById_MinLongValue_ThrowsNotFoundException() {
        when(addressRepository.findById(Long.MIN_VALUE)).thenReturn(Optional.empty());
        assertThrowsNotFound(ErrorMessages.AddressErrorMessages.NotFound, () -> addressService.getAddressById(Long.MIN_VALUE));
    }

    /**
     * Purpose: Validate negative ID is rejected.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound.
     */
    @Test
    @DisplayName("Get Address By ID - Negative ID - ThrowsNotFoundException")
    void getAddressById_NegativeId_ThrowsNotFoundException() {
        long negativeId = -1L;
        when(addressRepository.findById(negativeId)).thenReturn(Optional.empty());
        assertThrowsNotFound(ErrorMessages.AddressErrorMessages.NotFound, () -> addressService.getAddressById(negativeId));
    }

    /**
     * Purpose: Validate zero ID is rejected.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound.
     */
    @Test
    @DisplayName("Get Address By ID - Zero ID - ThrowsNotFoundException")
    void getAddressById_ZeroId_ThrowsNotFoundException() {
        long zeroId = 0L;
        when(addressRepository.findById(zeroId)).thenReturn(Optional.empty());
        assertThrowsNotFound(ErrorMessages.AddressErrorMessages.NotFound, () -> addressService.getAddressById(zeroId));
    }
}