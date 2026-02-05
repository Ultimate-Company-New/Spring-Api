package com.example.SpringApi.Services.Tests.Address;

import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.ErrorMessages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AddressService update functionality.
 * Tests for: UpdateAddressTests (29 tests)
 */
@DisplayName("Update Address Tests")
class UpdateAddressTest extends AddressServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify updating an existing address succeeds.
     * Expected Result: Address is saved and log is written.
     * Assertions: Save/log calls are invoked.
     */
    @Test
    @DisplayName("Update Address - Address found - Success updates all fields")
    void updateAddress_AddressFound_Success() {
        // Arrange
        testAddressRequest.setStreetAddress("456 Oak Ave");
        testAddressRequest.setCity("Los Angeles");
        testAddressRequest.setState("CA");

        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> addressService.updateAddress(testAddressRequest));

        // Verify
        verify(addressRepository, times(1)).findById(DEFAULT_ADDRESS_ID);
        verify(addressRepository, times(1)).save(any(Address.class));
        verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
    }

    /**
     * Purpose: Verify changing address type succeeds.
     * Expected Result: Update succeeds.
     * Assertions: No exception is thrown.
     */
    @Test
    @DisplayName("Update Address - Change address type - Success")
    void updateAddress_ChangeAddressType_Success() {
        // Arrange
        testAddressRequest.setAddressType("WORK");
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> addressService.updateAddress(testAddressRequest));
    }

    /**
     * Purpose: Verify update logs success route with updated ID.
     * Expected Result: Log entry includes success message and route.
     * Assertions: logData is called with UPDATE_ADDRESS route and success text.
     */
    @Test
    @DisplayName("Update Address - Logs success message and route")
    void updateAddress_LogsSuccessMessageAndRoute() {
        // Arrange
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act
        addressService.updateAddress(testAddressRequest);

        // Assert
        verify(userLogService).logData(
                eq(DEFAULT_USER_ID),
                contains(com.example.SpringApi.SuccessMessages.AddressSuccessMessages.UpdateAddress),
                eq(com.example.SpringApi.Models.ApiRoutes.AddressSubRoute.UPDATE_ADDRESS));
    }

    /**
     * Purpose: Default boolean fields when null on update.
     * Expected Result: isPrimary/isDeleted default false.
     * Assertions: Saved address has default boolean values.
     */
    @Test
    @DisplayName("Update Address - Null boolean fields - Defaults applied")
    void updateAddress_NullBooleanFields_DefaultsApplied() {
        // Arrange
        testAddressRequest.setCountry("India");
        testAddressRequest.setIsPrimary(null);
        testAddressRequest.setIsDeleted(null);
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> addressService.updateAddress(testAddressRequest));

        // Assert
        ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
        verify(addressRepository).save(captor.capture());
        assertFalse(captor.getValue().getIsPrimary());
        assertFalse(captor.getValue().getIsDeleted());
    }

    /**
     * Purpose: Verify update succeeds with null optional fields.
     * Expected Result: Update succeeds.
     * Assertions: No exception is thrown.
     */
    @Test
    @DisplayName("Update Address - Update with null optional fields - Success")
    void updateAddress_NullOptionalFieldsUpdated_Success() {
        // Arrange
        testAddressRequest.setStreetAddress2(null);
        testAddressRequest.setNameOnAddress(null);
        testAddressRequest.setPhoneOnAddress(null);
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> addressService.updateAddress(testAddressRequest));
    }

    /**
     * Purpose: Verify permission check is performed for UPDATE_ADDRESS permission.
     * Expected Result: Authorization service is called to check permissions.
     * Assertions: authorization.hasAuthority() is called with correct permission.
     */
    @Test
    @DisplayName("Update Address - Permission check - Success Verifies Authorization")
    void updateAddress_PermissionCheck_SuccessVerifiesAuthorization() {
        // Arrange
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
        lenient().when(authorization.hasAuthority(Authorizations.UPDATE_ADDRESS_PERMISSION)).thenReturn(true);

        // Act
        addressService.updateAddress(testAddressRequest);

        // Assert
        verify(authorization, times(1)).hasAuthority(Authorizations.UPDATE_ADDRESS_PERMISSION);
    }

    /**
     * Purpose: Preserve created fields on update.
     * Expected Result: createdUser and createdAt remain unchanged.
     * Assertions: Saved address retains original created fields.
     */
    @Test
    @DisplayName("Update Address - Preserve created fields - Success")
    void updateAddress_PreservesCreatedFields_Success() {
        // Arrange
        testAddress.setCreatedUser("original-user");
        testAddress.setCreatedAt(java.time.LocalDateTime.now().minusDays(1));

        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> addressService.updateAddress(testAddressRequest));

        // Assert
        ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
        verify(addressRepository).save(captor.capture());
        assertEquals("original-user", captor.getValue().getCreatedUser());
        assertEquals(testAddress.getCreatedAt(), captor.getValue().getCreatedAt());
    }

    /**
     * Purpose: Verify update succeeds with valid fields.
     * Expected Result: Address is saved and log is written.
     * Assertions: Save and log calls are invoked.
     */
    @Test
    @DisplayName("Update Address - Valid fields updated - Success")
    void updateAddress_ValidFieldsUpdated_Success() {
        // Arrange
        testAddressRequest.setStreetAddress("999 New Road");
        testAddressRequest.setCity("Chicago");
        testAddressRequest.setState("IL");
        testAddressRequest.setPostalCode("60601");
        testAddressRequest.setCountry("USA");
        testAddressRequest.setAddressType("BILLING");

        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        when(addressRepository.save(any(Address.class))).thenReturn(testAddress);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> addressService.updateAddress(testAddressRequest));
        verify(addressRepository, times(1)).save(any(Address.class));
        verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject update when address does not exist.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound and save is not called.
     */
    @Test
    @DisplayName("Update Address - Address not found - ThrowsNotFoundException")
    void updateAddress_AddressNotFound_ThrowsNotFoundException() {
        // Arrange
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.updateAddress(testAddressRequest));

        assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        verify(addressRepository, times(1)).findById(DEFAULT_ADDRESS_ID);
        verify(addressRepository, never()).save(any(Address.class));
    }

    /**
     * Purpose: Reject empty city.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER002.
     */
    @Test
    @DisplayName("Update Address - Empty city - ThrowsBadRequestException")
    void updateAddress_EmptyCity_ThrowsBadRequestException() {
        testAddressRequest.setCity("");
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest));
        assertEquals(ErrorMessages.AddressErrorMessages.ER002, exception.getMessage());
    }

    /**
     * Purpose: Reject empty country.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER005.
     */
    @Test
    @DisplayName("Update Address - Empty country - ThrowsBadRequestException")
    void updateAddress_EmptyCountry_ThrowsBadRequestException() {
        testAddressRequest.setCountry("");
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest));
        assertEquals(ErrorMessages.AddressErrorMessages.ER005, exception.getMessage());
    }

    /**
     * Purpose: Reject empty state.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER003.
     */
    @Test
    @DisplayName("Update Address - Empty state - ThrowsBadRequestException")
    void updateAddress_EmptyState_ThrowsBadRequestException() {
        testAddressRequest.setState("");
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest));
        assertEquals(ErrorMessages.AddressErrorMessages.ER003, exception.getMessage());
    }

    /**
     * Purpose: Reject empty street address.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER001.
     */
    @Test
    @DisplayName("Update Address - Empty street address - ThrowsBadRequestException")
    void updateAddress_EmptyStreetAddress_ThrowsBadRequestException() {
        // Arrange
        testAddressRequest.setStreetAddress("");
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest));

        assertEquals(ErrorMessages.AddressErrorMessages.ER001, exception.getMessage());
    }

    /**
     * Purpose: Reject invalid address type.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER006.
     */
    @Test
    @DisplayName("Update Address - Invalid address type - ThrowsBadRequestException")
    void updateAddress_InvalidAddressType_ThrowsBadRequestException() {
        // Arrange
        testAddressRequest.setAddressType("INVALID_TYPE");
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest));

        assertEquals(ErrorMessages.AddressErrorMessages.ER006, exception.getMessage());
    }

    /**
     * Purpose: Reject invalid modified user on update.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidUser.
     */
    @Test
    @DisplayName("Update Address - Invalid modified user - ThrowsBadRequestException")
    void updateAddress_InvalidModifiedUser_ThrowsBadRequestException() {
        // Arrange
        doReturn("").when(addressService).getUser();
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest));

        assertEquals(ErrorMessages.UserErrorMessages.InvalidUser, exception.getMessage());
    }

    /**
     * Purpose: Reject invalid postal code.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER007.
     */
    @Test
    @DisplayName("Update Address - Invalid postal code - ThrowsBadRequestException")
    void updateAddress_InvalidPostalCode_ThrowsBadRequestException() {
        // Arrange
        testAddressRequest.setPostalCode("invalid");
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest));

        assertEquals(ErrorMessages.AddressErrorMessages.ER007, exception.getMessage());
    }

    /**
     * Purpose: Reject update for max long ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound and save is not called.
     */
    @Test
    @DisplayName("Update Address - Long.MAX_VALUE ID - Throws NotFoundException")
    void updateAddress_MaxLongId_ThrowsNotFoundException() {
        testAddressRequest.setId(Long.MAX_VALUE);
        when(addressRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> addressService.updateAddress(testAddressRequest));
        assertEquals(ErrorMessages.AddressErrorMessages.NotFound, ex.getMessage());
        verify(addressRepository, never()).save(any(Address.class));
    }

    /**
     * Purpose: Reject negative client ID.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidId.
     */
    @Test
    @DisplayName("Update Address - Negative client ID - ThrowsBadRequestException")
    void updateAddress_NegativeClientId_ThrowsBadRequestException() {
        // Arrange
        testAddressRequest.setClientId(-1L);
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest));

        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, exception.getMessage());
    }

    /**
     * Purpose: Reject update for negative ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound and save is not called.
     */
    @Test
    @DisplayName("Update Address - Negative ID - Throws NotFoundException")
    void updateAddress_NegativeId_ThrowsNotFoundException() {
        testAddressRequest.setId(-1L);
        when(addressRepository.findById(-1L)).thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> addressService.updateAddress(testAddressRequest));
        assertEquals(ErrorMessages.AddressErrorMessages.NotFound, ex.getMessage());
        verify(addressRepository, never()).save(any(Address.class));
    }

    /**
     * Purpose: Reject negative user ID.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidId.
     */
    @Test
    @DisplayName("Update Address - Negative user ID - ThrowsBadRequestException")
    void updateAddress_NegativeUserId_ThrowsBadRequestException() {
        // Arrange
        testAddressRequest.setUserId(-1L);
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest));

        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, exception.getMessage());
    }

    /**
     * Purpose: Reject null city.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER002.
     */
    @Test
    @DisplayName("Update Address - Null city - ThrowsBadRequestException")
    void updateAddress_NullCity_ThrowsBadRequestException() {
        testAddressRequest.setCity(null);
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest));
        assertEquals(ErrorMessages.AddressErrorMessages.ER002, exception.getMessage());
    }

    /**
     * Purpose: Reject null country.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER005.
     */
    @Test
    @DisplayName("Update Address - Null country - ThrowsBadRequestException")
    void updateAddress_NullCountry_ThrowsBadRequestException() {
        testAddressRequest.setCountry(null);
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest));
        assertEquals(ErrorMessages.AddressErrorMessages.ER005, exception.getMessage());
    }

    /**
     * Purpose: Reject null postal code.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER004.
     */
    @Test
    @DisplayName("Update Address - Null postal code - ThrowsBadRequestException")
    void updateAddress_NullPostalCode_ThrowsBadRequestException() {
        // Arrange
        testAddressRequest.setPostalCode(null);
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest));

        assertEquals(ErrorMessages.AddressErrorMessages.ER004, exception.getMessage());
    }

    /**
     * Purpose: Reject null update request.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER001.
     */
    @Test
    @DisplayName("Update Address - Null request - ThrowsBadRequestException")
    void updateAddress_NullRequest_ThrowsBadRequestException() {
        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(null));

        assertEquals(ErrorMessages.AddressErrorMessages.ER001, exception.getMessage());
    }

    /**
     * Purpose: Reject null state.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER003.
     */
    @Test
    @DisplayName("Update Address - Null state - ThrowsBadRequestException")
    void updateAddress_NullState_ThrowsBadRequestException() {
        testAddressRequest.setState(null);
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest));
        assertEquals(ErrorMessages.AddressErrorMessages.ER003, exception.getMessage());
    }

    /**
     * Purpose: Reject null street address.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER001.
     */
    @Test
    @DisplayName("Update Address - Null street address - ThrowsBadRequestException")
    void updateAddress_NullStreetAddress_ThrowsBadRequestException() {
        // Arrange
        testAddressRequest.setStreetAddress(null);
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest));

        assertEquals(ErrorMessages.AddressErrorMessages.ER001, exception.getMessage());
    }

    /**
     * Purpose: Reject zero client ID.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidId.
     */
    @Test
    @DisplayName("Update Address - Zero client ID - ThrowsBadRequestException")
    void updateAddress_ZeroClientId_ThrowsBadRequestException() {
        // Arrange
        testAddressRequest.setClientId(0L);
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest));

        assertEquals(ErrorMessages.ClientErrorMessages.InvalidId, exception.getMessage());
    }

    /**
     * Purpose: Reject update for zero ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound and save is not called.
     */
    @Test
    @DisplayName("Update Address - Zero ID - Throws NotFoundException")
    void updateAddress_ZeroId_ThrowsNotFoundException() {
        testAddressRequest.setId(0L);
        when(addressRepository.findById(0L)).thenReturn(Optional.empty());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> addressService.updateAddress(testAddressRequest));
        assertEquals(ErrorMessages.AddressErrorMessages.NotFound, ex.getMessage());
        verify(addressRepository, never()).save(any(Address.class));
    }

    /**
     * Purpose: Reject zero user ID.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches InvalidId.
     */
    @Test
    @DisplayName("Update Address - Zero user ID - ThrowsBadRequestException")
    void updateAddress_ZeroUserId_ThrowsBadRequestException() {
        // Arrange
        testAddressRequest.setUserId(0L);
        when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest));

        assertEquals(ErrorMessages.UserErrorMessages.InvalidId, exception.getMessage());
    }
}