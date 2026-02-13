package com.example.SpringApi.Services.Tests.Address;

import com.example.SpringApi.Controllers.AddressController;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AddressService update functionality.
 */
@DisplayName("Update Address Tests")
class UpdateAddressTest extends AddressServiceTestBase {

    // Total Tests: 31
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

        stubDefaultRepositoryResponses();
        stubAddressRepositorySave(testAddress);
        stubUserLogSuccess();

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
        stubDefaultRepositoryResponses();
        stubAddressRepositorySave(testAddress);
        stubUserLogSuccess();

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
    void updateAddress_LogsSuccessMessageAndRoute_Success() {
        // Arrange
        stubDefaultRepositoryResponses();
        stubAddressRepositorySave(testAddress);
        stubUserLogSuccess();

        // Act
        addressService.updateAddress(testAddressRequest);

        // Assert
        verify(userLogService).logData(
                eq(DEFAULT_USER_ID),
                contains(com.example.SpringApi.SuccessMessages.AddressSuccessMessages.UPDATE_ADDRESS),
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
        stubDefaultRepositoryResponses();
        stubAddressRepositorySave(testAddress);
        stubUserLogSuccess();

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
        stubDefaultRepositoryResponses();
        stubAddressRepositorySave(testAddress);
        stubUserLogSuccess();

        // Act & Assert
        assertDoesNotThrow(() -> addressService.updateAddress(testAddressRequest));
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

        stubDefaultRepositoryResponses();
        stubAddressRepositorySave(testAddress);
        stubUserLogSuccess();

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

        stubDefaultRepositoryResponses();
        stubAddressRepositorySave(testAddress);
        stubUserLogSuccess();

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
        testAddressRequest.setId(DEFAULT_ADDRESS_ID + 1);
        stubDefaultRepositoryResponses();
        stubFindAddressById(DEFAULT_ADDRESS_ID + 1, Optional.empty());

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.AddressErrorMessages.NOT_FOUND,
                () -> addressService.updateAddress(testAddressRequest));
        verify(addressRepository, times(1)).findById(DEFAULT_ADDRESS_ID + 1);
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
        // Arrange
        testAddressRequest.setCity("");
        stubDefaultRepositoryResponses();

        // Act & Assert
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
        // Arrange
        testAddressRequest.setCountry("");
        stubDefaultRepositoryResponses();

        // Act & Assert
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
        // Arrange
        testAddressRequest.setState("");
        stubDefaultRepositoryResponses();

        // Act & Assert
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
        stubDefaultRepositoryResponses();

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
        stubDefaultRepositoryResponses();

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
        stubServiceGetUser("");
        stubDefaultRepositoryResponses();

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest));

        assertEquals(ErrorMessages.UserErrorMessages.INVALID_USER, exception.getMessage());
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
        stubDefaultRepositoryResponses();

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
        // Arrange
        testAddressRequest.setId(Long.MAX_VALUE);
        stubDefaultRepositoryResponses();

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.AddressErrorMessages.NOT_FOUND,
                () -> addressService.updateAddress(testAddressRequest));
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
        stubDefaultRepositoryResponses();

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest));

        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, exception.getMessage());
    }

    /**
     * Purpose: Reject update for negative ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound and save is not called.
     */
    @Test
    @DisplayName("Update Address - Negative ID - Throws NotFoundException")
    void updateAddress_NegativeId_ThrowsNotFoundException() {
        // Arrange
        testAddressRequest.setId(-1L);
        stubDefaultRepositoryResponses();

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.AddressErrorMessages.NOT_FOUND,
                () -> addressService.updateAddress(testAddressRequest));
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
        stubDefaultRepositoryResponses();

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest));

        assertEquals(ErrorMessages.UserErrorMessages.INVALID_ID, exception.getMessage());
    }

    /**
     * Purpose: Reject null city.
     * Expected Result: BadRequestException is thrown.
     * Assertions: Error message matches ER002.
     */
    @Test
    @DisplayName("Update Address - Null city - ThrowsBadRequestException")
    void updateAddress_NullCity_ThrowsBadRequestException() {
        // Arrange
        testAddressRequest.setCity(null);
        stubDefaultRepositoryResponses();

        // Act & Assert
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
        // Arrange
        testAddressRequest.setCountry(null);
        stubDefaultRepositoryResponses();

        // Act & Assert
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
        stubDefaultRepositoryResponses();

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
        // Arrange
        com.example.SpringApi.Models.RequestModels.AddressRequestModel nullRequest = null;

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(nullRequest));

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
        // Arrange
        testAddressRequest.setState(null);
        stubDefaultRepositoryResponses();

        // Act & Assert
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
        stubDefaultRepositoryResponses();

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
        stubDefaultRepositoryResponses();

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest));

        assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, exception.getMessage());
    }

    /**
     * Purpose: Reject update for zero ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound and save is not called.
     */
    @Test
    @DisplayName("Update Address - Zero ID - Throws NotFoundException")
    void updateAddress_ZeroId_ThrowsNotFoundException() {
        // Arrange
        testAddressRequest.setId(0L);
        stubDefaultRepositoryResponses();

        // Act & Assert
        assertThrowsNotFound(ErrorMessages.AddressErrorMessages.NOT_FOUND,
                () -> addressService.updateAddress(testAddressRequest));
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
        stubDefaultRepositoryResponses();

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(testAddressRequest));

        assertEquals(ErrorMessages.UserErrorMessages.INVALID_ID, exception.getMessage());
    }

    /*
     **********************************************************************************************
     * PERMISSION TESTS
     **********************************************************************************************
     * The following tests verify that authorization is properly configured at the
     * controller level.
     * These tests check that @PreAuthorize annotations are present and correctly
     * configured.
     */    /**
     * Purpose: Verify that the controller has the correct @PreAuthorize annotation
     * for security.
     * Expected Result: The method should be annotated with @PreAuthorize checking
     * for UPDATE_ADDRESS_PERMISSION.
     * Assertions: Annotation is present and contains expected permission string.
     */
    @Test
    @DisplayName("Update Address - Controller permission forbidden - Success")
    void updateAddress_p03_controller_permission_forbidden() throws NoSuchMethodException {
        // Arrange
        var method = AddressController.class.getMethod("updateAddress",
                Long.class, com.example.SpringApi.Models.RequestModels.AddressRequestModel.class);
        testAddressRequest.setId(DEFAULT_ADDRESS_ID);
        stubServiceUpdateAddressDoNothing();

        // Act
        var preAuthorizeAnnotation = method.getAnnotation(
                org.springframework.security.access.prepost.PreAuthorize.class);
        ResponseEntity<?> response = addressController.updateAddress(DEFAULT_ADDRESS_ID, testAddressRequest);

        // Assert
        assertNotNull(preAuthorizeAnnotation, "updateAddress method should have @PreAuthorize annotation");

        String expectedPermission = "@customAuthorization.hasAuthority('" +
                Authorizations.UPDATE_ADDRESS_PERMISSION + "')";

        assertEquals(expectedPermission, preAuthorizeAnnotation.value(),
                "PreAuthorize annotation should reference UPDATE_ADDRESS_PERMISSION");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return HTTP 200 OK");
    }
}