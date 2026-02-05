package com.example.SpringApi.Services.Tests.Address;

import com.example.SpringApi.Controllers.AddressController;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.ResponseModels.AddressResponseModel;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.ErrorMessages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AddressService getAddressByUserId functionality.
 * Tests for: GetAddressByUserIdTests (11 tests)
 */
@DisplayName("Get Address By User ID Tests")
class GetAddressByUserIdTest extends AddressServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify multiple addresses returned for a user.
     * Expected Result: List contains all addresses.
     * Assertions: Response size and fields match expected.
     */
    @Test
    @DisplayName("Get Address By User ID - Multiple addresses found - Success returns list")
    void getAddressByUserId_MultipleAddressesFound_Success() {
        when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.of(testUser));
        Address secondAddress = createTestAddress(2L, "WORK");
        List<Address> addresses = Arrays.asList(testAddress, secondAddress);
        when(addressRepository.findByUserIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_USER_ID, false))
                .thenReturn(addresses);

        // Act
        List<AddressResponseModel> result = addressService.getAddressByUserId(DEFAULT_USER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(DEFAULT_ADDRESS_ID, result.get(0).getAddressId());
        assertEquals(DEFAULT_ADDRESS_TYPE, result.get(0).getAddressType());
        assertEquals(2L, result.get(1).getAddressId());
        assertEquals("WORK", result.get(1).getAddressType());

        verify(userRepository, times(1)).findById(DEFAULT_USER_ID);
        verify(addressRepository, times(1)).findByUserIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_USER_ID, false);
    }

    /**
     * Purpose: Handle user with no addresses.
     * Expected Result: Empty list is returned.
     * Assertions: Result is empty and repository is called.
     */
    @Test
    @DisplayName("Get Address By User ID - No addresses - Success returns empty list")
    void getAddressByUserId_NoAddresses_ReturnsEmptyList() {
        // Arrange
        when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.of(testUser));
        when(addressRepository.findByUserIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_USER_ID, false))
                .thenReturn(new ArrayList<>());

        // Act
        List<AddressResponseModel> result = addressService.getAddressByUserId(DEFAULT_USER_ID);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(addressRepository, times(1)).findByUserIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_USER_ID, false);
    }

    /**
     * Purpose: Verify single address response for user.
     * Expected Result: List contains one address.
     * Assertions: Result size is one.
     */
    @Test
    @DisplayName("Get Address By User ID - Single address - Success returns list with one item")
    void getAddressByUserId_SingleAddress_Success() {
        // Arrange
        when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.of(testUser));
        when(addressRepository.findByUserIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_USER_ID, false))
                .thenReturn(Collections.singletonList(testAddress));

        // Act
        List<AddressResponseModel> result = addressService.getAddressByUserId(DEFAULT_USER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject very large user ID when not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound.
     */
    @Test
    @DisplayName("Get Address By User ID - Max Long value - ThrowsNotFoundException")
    void getAddressByUserId_MaxLongValue_ThrowsNotFoundException() {
        // Arrange
        when(userRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.getAddressByUserId(Long.MAX_VALUE));

        assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
    }

    /**
     * Purpose: Reject min long user ID when not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound.
     */
    @Test
    @DisplayName("Get Address By User ID - Min Long value - ThrowsNotFoundException")
    void getAddressByUserId_MinLongValue_ThrowsNotFoundException() {
        // Arrange
        when(userRepository.findById(Long.MIN_VALUE)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.getAddressByUserId(Long.MIN_VALUE));

        assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
    }

    /**
     * Purpose: Reject negative user ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound.
     */
    @Test
    @DisplayName("Get Address By User ID - Negative ID - ThrowsNotFoundException")
    void getAddressByUserId_NegativeId_ThrowsNotFoundException() {
        // Arrange
        long negativeId = -1L;
        when(userRepository.findById(negativeId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.getAddressByUserId(negativeId));

        assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
    }

    /**
     * Purpose: Reject deleted user.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound.
     */
    @Test
    @DisplayName("Get Address By User ID - User deleted - ThrowsNotFoundException")
    void getAddressByUserId_UserDeleted_ThrowsNotFoundException() {
        // Arrange
        testUser.setIsDeleted(true);
        when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.of(testUser));

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.getAddressByUserId(DEFAULT_USER_ID));

        assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        verify(addressRepository, never()).findByUserIdAndIsDeletedOrderByAddressIdDesc(anyLong(), anyBoolean());
    }

    /**
     * Purpose: Reject user not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound.
     */
    @Test
    @DisplayName("Get Address By User ID - User not found - ThrowsNotFoundException")
    void getAddressByUserId_UserNotFound_ThrowsNotFoundException() {
        // Arrange
        when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.getAddressByUserId(DEFAULT_USER_ID));

        assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        verify(addressRepository, never()).findByUserIdAndIsDeletedOrderByAddressIdDesc(anyLong(), anyBoolean());
    }

    /**
     * Purpose: Reject zero user ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound.
     */
    @Test
    @DisplayName("Get Address By User ID - Zero ID - ThrowsNotFoundException")
    void getAddressByUserId_ZeroId_ThrowsNotFoundException() {
        // Arrange
        long zeroId = 0L;
        when(userRepository.findById(zeroId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.getAddressByUserId(zeroId));

        assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
    }

    /*
     **********************************************************************************************
     * CONTROLLER AUTHORIZATION TESTS
     **********************************************************************************************
     * The following tests verify that authorization is properly configured at the
     * controller level.
     * These tests check that @PreAuthorize annotations are present and correctly
     * configured.
     */

    /**
     * Purpose: Verify @PreAuthorize annotation is declared on getAddressesByUserId
     * method.
     * Expected Result: Method has @PreAuthorize annotation with correct permission.
     * Assertions: Annotation exists and references VIEW_ADDRESS_PERMISSION.
     */
    @Test
    @DisplayName("Get Address By User ID - Verify @PreAuthorize annotation is configured correctly")
    void getAddressesByUserId_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        // Use reflection to verify the @PreAuthorize annotation is present
        var method = AddressController.class.getMethod("getAddressesByUserId",
                Long.class);

        var preAuthorizeAnnotation = method.getAnnotation(
                org.springframework.security.access.prepost.PreAuthorize.class);

        assertNotNull(preAuthorizeAnnotation,
                "getAddressesByUserId method should have @PreAuthorize annotation");

        String expectedPermission = "@customAuthorization.hasAuthority('" +
                Authorizations.VIEW_ADDRESS_PERMISSION + "')";

        assertEquals(expectedPermission, preAuthorizeAnnotation.value(),
                "PreAuthorize annotation should reference VIEW_ADDRESS_PERMISSION");
    }

    /**
     * Purpose: Verify controller calls service when authorization passes
     * (simulated).
     * Expected Result: Service method is called and correct HTTP status is
     * returned.
     * Assertions: Service called once, HTTP status is correct.
     * 
     * Note: This test simulates the happy path assuming authorization has already
     * passed.
     * Actual @PreAuthorize enforcement is handled by Spring Security AOP and tested
     * in end-to-end tests.
     */
    @Test
    @DisplayName("Get Address By User ID - Controller delegates to service correctly")
    void getAddressesByUserId_WithValidRequest_DelegatesToService() {
        // Arrange
        doReturn(new ArrayList<>()).when(addressService).getAddressByUserId(DEFAULT_USER_ID);
        AddressController controller = new AddressController(addressService);

        // Act - Call controller directly (simulating authorization has already passed)
        ResponseEntity<?> response = controller.getAddressesByUserId(DEFAULT_USER_ID);

        // Assert - Verify service was called and correct response returned
        verify(addressService, times(1)).getAddressByUserId(DEFAULT_USER_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Should return HTTP 200 OK");
    }
}