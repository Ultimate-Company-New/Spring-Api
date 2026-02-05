package com.example.SpringApi.Services.Tests.Address;

import com.example.SpringApi.Controllers.AddressController;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.ResponseModels.AddressResponseModel;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.ErrorMessages;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AddressService getAddressByClientId functionality.
 * Tests for: GetAddressByClientIdTests (15 tests)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Get Address By Client ID Tests")
class GetAddressByClientIdTest extends AddressServiceTestBase {

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify all address types are returned for a client.
     * Expected Result: List includes each address type.
     * Assertions: Result size and type set include all expected values.
     */
    @Test
    @DisplayName("Get Address By Client ID - All address types - Success returns all types")
    void getAddressByClientId_AllAddressTypes_Success() {
        when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
        Address homeAddress = createTestAddress(1L, "HOME");
        Address workAddress = createTestAddress(2L, "WORK");
        Address billingAddress = createTestAddress(3L, "BILLING");
        Address shippingAddress = createTestAddress(4L, "SHIPPING");
        Address officeAddress = createTestAddress(5L, "OFFICE");
        Address warehouseAddress = createTestAddress(6L, "WAREHOUSE");
        List<Address> addresses = Arrays.asList(
                homeAddress, workAddress, billingAddress,
                shippingAddress, officeAddress, warehouseAddress);
        when(addressRepository.findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID, false))
                .thenReturn(addresses);

        // Act
        List<AddressResponseModel> result = addressService.getAddressByClientId(DEFAULT_CLIENT_ID);

        // Assert
        assertNotNull(result);
        assertEquals(6, result.size());

        Set<String> addressTypes = new HashSet<>();
        for (AddressResponseModel address : result) {
            addressTypes.add(address.getAddressType());
            assertEquals(DEFAULT_CLIENT_ID, address.getClientId());
        }

        assertTrue(addressTypes.contains("HOME"));
        assertTrue(addressTypes.contains("WORK"));
        assertTrue(addressTypes.contains("BILLING"));
        assertTrue(addressTypes.contains("SHIPPING"));
        assertTrue(addressTypes.contains("OFFICE"));
        assertTrue(addressTypes.contains("WAREHOUSE"));
    }

    /**
     * Purpose: Verify large result sets are handled for client addresses.
     * Expected Result: All addresses are returned.
     * Assertions: Result size matches expected count.
     */
    @Test
    @DisplayName("Get Address By Client ID - Large result set - Success")
    void getAddressByClientId_LargeResultSet_Success() {
        // Arrange
        when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));

        List<Address> addresses = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            addresses.add(createTestAddress((long) i, i % 2 == 0 ? "HOME" : "WORK"));
        }
        when(addressRepository.findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID, false))
                .thenReturn(addresses);

        // Act
        List<AddressResponseModel> result = addressService.getAddressByClientId(DEFAULT_CLIENT_ID);

        // Assert
        assertNotNull(result);
        assertEquals(100, result.size());
        for (AddressResponseModel address : result) {
            assertEquals(DEFAULT_CLIENT_ID, address.getClientId());
        }
    }

    /**
     * Purpose: Verify multiple addresses returned for a client.
     * Expected Result: List contains all addresses.
     * Assertions: Response size and fields match expected.
     */
    @Test
    @DisplayName("Get Address By Client ID - Multiple addresses found - Success returns list")
    void getAddressByClientId_MultipleAddressesFound_Success() {
        when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
        Address secondAddress = createTestAddress(2L, "BILLING");
        List<Address> addresses = Arrays.asList(testAddress, secondAddress);
        when(addressRepository.findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID, false))
                .thenReturn(addresses);

        // Act
        List<AddressResponseModel> result = addressService.getAddressByClientId(DEFAULT_CLIENT_ID);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(DEFAULT_ADDRESS_ID, result.get(0).getAddressId());
        assertEquals(DEFAULT_CLIENT_ID, result.get(0).getClientId());
        assertEquals(DEFAULT_ADDRESS_TYPE, result.get(0).getAddressType());
        assertEquals(2L, result.get(1).getAddressId());
        assertEquals(DEFAULT_CLIENT_ID, result.get(1).getClientId());
        assertEquals("BILLING", result.get(1).getAddressType());

        verify(clientRepository, times(1)).findById(DEFAULT_CLIENT_ID);
        verify(addressRepository, times(1)).findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID,
                false);
    }

    /**
     * Purpose: Handle client with no addresses.
     * Expected Result: Empty list is returned.
     * Assertions: Result is empty and repository is called.
     */
    @Test
    @DisplayName("Get Address By Client ID - No addresses - Success returns empty list")
    void getAddressByClientId_NoAddresses_ReturnsEmptyList() {
        // Arrange
        when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(addressRepository.findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID, false))
                .thenReturn(new ArrayList<>());

        // Act
        List<AddressResponseModel> result = addressService.getAddressByClientId(DEFAULT_CLIENT_ID);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(addressRepository, times(1)).findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID,
                false);
    }

    /**
     * Purpose: Verify null optional fields are handled for client addresses.
     * Expected Result: Response contains null optional fields.
     * Assertions: Optional fields are null in response.
     */
    @Test
    @DisplayName("Get Address By Client ID - Addresses with null optional fields - Success")
    void getAddressByClientId_NullOptionalFields_Success() {
        // Arrange
        testAddress.setStreetAddress2(null);
        testAddress.setStreetAddress3(null);
        testAddress.setNameOnAddress(null);
        testAddress.setEmailOnAddress(null);
        testAddress.setPhoneOnAddress(null);
        when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(addressRepository.findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID, false))
                .thenReturn(Collections.singletonList(testAddress));

        // Act
        List<AddressResponseModel> result = addressService.getAddressByClientId(DEFAULT_CLIENT_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getStreetAddress2());
        assertNull(result.get(0).getStreetAddress3());
        assertNull(result.get(0).getNameOnAddress());
    }

    /**
     * Purpose: Verify single address response for client.
     * Expected Result: List contains one address.
     * Assertions: Result size is one.
     */
    @Test
    @DisplayName("Get Address By Client ID - Single address - Success returns list with one item")
    void getAddressByClientId_SingleAddress_Success() {
        // Arrange
        when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(addressRepository.findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID, false))
                .thenReturn(Collections.singletonList(testAddress));

        // Act
        List<AddressResponseModel> result = addressService.getAddressByClientId(DEFAULT_CLIENT_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    /**
     * Purpose: Verify that service layer returns addresses when called directly.
     * Note: Permission checking is a controller-level concern and is tested in API
     * tests.
     * This unit test verifies the service method works correctly with valid inputs.
     * Expected Result: List of addresses is returned successfully.
     * Assertions: Repository is called and result is not null.
     */
    @Test
    @DisplayName("Get Address By Client ID - Valid call - Success returns list")
    void getAddressByClientId_ValidCall_SuccessReturnsAddresses() {
        // Arrange
        when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
        when(addressRepository.findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID, false))
                .thenReturn(Collections.singletonList(testAddress));

        // Act
        List<AddressResponseModel> result = addressService.getAddressByClientId(DEFAULT_CLIENT_ID);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(clientRepository, times(1)).findById(DEFAULT_CLIENT_ID);
        verify(addressRepository, times(1)).findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID, false);
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Reject deleted client.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound.
     */
    @Test
    @DisplayName("Get Address By Client ID - Client deleted - ThrowsNotFoundException")
    void getAddressByClientId_ClientDeleted_ThrowsNotFoundException() {
        // Arrange
        testClient.setIsDeleted(true);
        when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.getAddressByClientId(DEFAULT_CLIENT_ID));

        assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        verify(addressRepository, never()).findByClientIdAndIsDeletedOrderByAddressIdDesc(anyLong(), anyBoolean());
    }

    /**
     * Purpose: Reject client not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound.
     */
    @Test
    @DisplayName("Get Address By Client ID - Client not found - ThrowsNotFoundException")
    void getAddressByClientId_ClientNotFound_ThrowsNotFoundException() {
        // Arrange
        when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.getAddressByClientId(DEFAULT_CLIENT_ID));

        assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
        verify(addressRepository, never()).findByClientIdAndIsDeletedOrderByAddressIdDesc(anyLong(), anyBoolean());
    }

    /**
     * Purpose: Reject min long client ID when not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound.
     */
    @Test
    @DisplayName("Get Address By Client ID - Min Long value - ThrowsNotFoundException")
    void getAddressByClientId_MinLongValue_ThrowsNotFoundException() {
        // Arrange
        when(clientRepository.findById(Long.MIN_VALUE)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.getAddressByClientId(Long.MIN_VALUE));

        assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
    }

    /**
     * Purpose: Reject negative client ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound.
     */
    @Test
    @DisplayName("Get Address By Client ID - Negative ID - ThrowsNotFoundException")
    void getAddressByClientId_NegativeId_ThrowsNotFoundException() {
        // Arrange
        long negativeId = -1L;
        when(clientRepository.findById(negativeId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.getAddressByClientId(negativeId));

        assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
    }

    /**
     * Purpose: Reject very large client ID when not found.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound.
     */
    @Test
    @DisplayName("Get Address By Client ID - Very large ID - ThrowsNotFoundException")
    void getAddressByClientId_VeryLargeId_ThrowsNotFoundException() {
        // Arrange
        long largeId = Long.MAX_VALUE;
        when(clientRepository.findById(largeId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.getAddressByClientId(largeId));

        assertEquals(ErrorMessages.AddressErrorMessages.NotFound, exception.getMessage());
    }

    /**
     * Purpose: Reject zero client ID.
     * Expected Result: NotFoundException is thrown.
     * Assertions: Error message matches NotFound.
     */
    @Test
    @DisplayName("Get Address By Client ID - Zero ID - ThrowsNotFoundException")
    void getAddressByClientId_ZeroId_ThrowsNotFoundException() {
        // Arrange
        long zeroId = 0L;
        when(clientRepository.findById(zeroId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.getAddressByClientId(zeroId));

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
     * Purpose: Verify @PreAuthorize annotation is declared on getAddressByClientId
     * method.
     * Expected Result: Method has @PreAuthorize annotation with correct permission.
     * Assertions: Annotation exists and references VIEW_ADDRESS_PERMISSION.
     */
    @Test
    @DisplayName("Get Address By Client ID - Verify @PreAuthorize annotation is configured correctly")
    void getAddressByClientId_VerifyPreAuthorizeAnnotation() throws NoSuchMethodException {
        // Use reflection to verify the @PreAuthorize annotation is present
        var method = AddressController.class.getMethod("getAddressByClientId",
                Long.class);

        var preAuthorizeAnnotation = method.getAnnotation(
                org.springframework.security.access.prepost.PreAuthorize.class);

        assertNotNull(preAuthorizeAnnotation,
                "getAddressByClientId method should have @PreAuthorize annotation");

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
    @DisplayName("Get Address By Client ID - Controller delegates to service correctly")
    void getAddressByClientId_WithValidRequest_DelegatesToService() {
        // Arrange
        doReturn(new ArrayList<>()).when(addressService).getAddressByClientId(DEFAULT_CLIENT_ID);
        AddressController controller = new AddressController(addressService);

        // Act - Call controller directly (simulating authorization has already passed)
        ResponseEntity<?> response = controller.getAddressByClientId(DEFAULT_CLIENT_ID);

        // Assert - Verify service was called and correct response returned
        verify(addressService, times(1)).getAddressByClientId(DEFAULT_CLIENT_ID);
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Should return HTTP 200 OK");
    }
}