package com.example.springapi.ServiceTests.Address;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.springapi.ErrorMessages;
import com.example.springapi.controllers.AddressController;
import com.example.springapi.models.Authorizations;
import com.example.springapi.models.databasemodels.Address;
import com.example.springapi.models.responsemodels.AddressResponseModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Unit tests for AddressService getAddressByClientId functionality. */
@DisplayName("Get Address By Client ID Tests")
class GetAddressByClientIdTest extends AddressServiceTestBase {

  // Total Tests: 16
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify all address types are returned for a client. Expected Result: List includes
   * each address type. Assertions: Result size and type set include all expected values.
   */
  @Test
  @DisplayName("Get Address By Client ID - All address types - Success returns all types")
  void getAddressByClientId_AllAddressTypes_Success() {
    // Arrange
    Address homeAddress = createTestAddress(1L, "HOME");
    Address workAddress = createTestAddress(2L, "WORK");
    Address billingAddress = createTestAddress(3L, "BILLING");
    Address shippingAddress = createTestAddress(4L, "SHIPPING");
    Address officeAddress = createTestAddress(5L, "OFFICE");
    Address warehouseAddress = createTestAddress(6L, "WAREHOUSE");
    List<Address> addresses =
        Arrays.asList(
            homeAddress,
            workAddress,
            billingAddress,
            shippingAddress,
            officeAddress,
            warehouseAddress);
    stubDefaultRepositoryResponses();
    stubFindAddressesByClientId(DEFAULT_CLIENT_ID, addresses);

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
   * Purpose: Verify large result sets are handled for client addresses. Expected Result: All
   * addresses are returned. Assertions: Result size matches expected count.
   */
  @Test
  @DisplayName("Get Address By Client ID - Large result set - Success")
  void getAddressByClientId_LargeResultSet_Success() {
    // Arrange
    List<Address> addresses = new ArrayList<>();
    for (int i = 1; i <= 100; i++) {
      addresses.add(createTestAddress((long) i, i % 2 == 0 ? "HOME" : "WORK"));
    }
    stubDefaultRepositoryResponses();
    stubFindAddressesByClientId(DEFAULT_CLIENT_ID, addresses);

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
   * Purpose: Verify multiple addresses returned for a client. Expected Result: List contains all
   * addresses. Assertions: Response size and fields match expected.
   */
  @Test
  @DisplayName("Get Address By Client ID - Multiple addresses found - Success returns list")
  void getAddressByClientId_MultipleAddressesFound_Success() {
    // Arrange
    Address secondAddress = createTestAddress(2L, "BILLING");
    List<Address> addresses = Arrays.asList(testAddress, secondAddress);
    stubDefaultRepositoryResponses();
    stubFindAddressesByClientId(DEFAULT_CLIENT_ID, addresses);

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
    verify(addressRepository, times(1))
        .findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID, false);
  }

  /**
   * Purpose: Handle client with no addresses. Expected Result: Empty list is returned. Assertions:
   * Result is empty and repository is called.
   */
  @Test
  @DisplayName("Get Address By Client ID - No addresses - Success returns empty list")
  void getAddressByClientId_NoAddresses_ReturnsEmptyList() {
    // Arrange
    stubDefaultRepositoryResponses();
    stubFindAddressesByClientId(DEFAULT_CLIENT_ID, new ArrayList<>());

    // Act
    List<AddressResponseModel> result = addressService.getAddressByClientId(DEFAULT_CLIENT_ID);

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(addressRepository, times(1))
        .findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID, false);
  }

  /**
   * Purpose: Verify null optional fields are handled for client addresses. Expected Result:
   * Response contains null optional fields. Assertions: Optional fields are null in response.
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
    stubDefaultRepositoryResponses();
    stubFindAddressesByClientId(DEFAULT_CLIENT_ID, List.of(testAddress));

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
   * Purpose: Verify single address response for client. Expected Result: List contains one address.
   * Assertions: Result size is one.
   */
  @Test
  @DisplayName("Get Address By Client ID - Single address - Success returns list with one item")
  void getAddressByClientId_SingleAddress_Success() {
    // Arrange
    stubDefaultRepositoryResponses();
    stubFindAddressesByClientId(DEFAULT_CLIENT_ID, List.of(testAddress));

    // Act
    List<AddressResponseModel> result = addressService.getAddressByClientId(DEFAULT_CLIENT_ID);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
  }

  /**
   * Purpose: Verify that service layer returns addresses when called directly. Note: Permission
   * checking is a controller-level concern and is tested in API tests. This unit test verifies the
   * service method works correctly with valid inputs. Expected Result: List of addresses is
   * returned successfully. Assertions: Repository is called and result is not null.
   */
  @Test
  @DisplayName("Get Address By Client ID - Valid call - Success returns list")
  void getAddressByClientId_ValidCall_SuccessReturnsAddresses() {
    // Arrange
    stubDefaultRepositoryResponses();
    stubFindAddressesByClientId(DEFAULT_CLIENT_ID, List.of(testAddress));

    // Act
    List<AddressResponseModel> result = addressService.getAddressByClientId(DEFAULT_CLIENT_ID);

    // Assert
    assertNotNull(result);
    assertFalse(result.isEmpty());
    verify(clientRepository, times(1)).findById(DEFAULT_CLIENT_ID);
    verify(addressRepository, times(1))
        .findByClientIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_CLIENT_ID, false);
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Reject deleted client. Expected Result: NotFoundException is thrown. Assertions: Error
   * message matches NotFound.
   */
  @Test
  @DisplayName("Get Address By Client ID - Client deleted - ThrowsNotFoundException")
  void getAddressByClientId_ClientDeleted_ThrowsNotFoundException() {
    // Arrange
    testClient.setIsDeleted(true);
    stubDefaultRepositoryResponses();

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.AddressErrorMessages.NOT_FOUND,
        () -> addressService.getAddressByClientId(DEFAULT_CLIENT_ID));
    verify(addressRepository, never())
        .findByClientIdAndIsDeletedOrderByAddressIdDesc(anyLong(), anyBoolean());
  }

  /**
   * Purpose: Reject client not found. Expected Result: NotFoundException is thrown. Assertions:
   * Error message matches NotFound.
   */
  @Test
  @DisplayName("Get Address By Client ID - Client not found - ThrowsNotFoundException")
  void getAddressByClientId_ClientNotFound_ThrowsNotFoundException() {
    // Arrange
    stubDefaultRepositoryResponses();

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.AddressErrorMessages.NOT_FOUND,
        () -> addressService.getAddressByClientId(DEFAULT_CLIENT_ID + 1));
    verify(addressRepository, never())
        .findByClientIdAndIsDeletedOrderByAddressIdDesc(anyLong(), anyBoolean());
  }

  /**
   * Purpose: Reject min long client ID when not found. Expected Result: NotFoundException is
   * thrown. Assertions: Error message matches NotFound.
   */
  @Test
  @DisplayName("Get Address By Client ID - Min Long value - ThrowsNotFoundException")
  void getAddressByClientId_MinLongValue_ThrowsNotFoundException() {
    // Arrange
    stubDefaultRepositoryResponses();

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.AddressErrorMessages.NOT_FOUND,
        () -> addressService.getAddressByClientId(Long.MIN_VALUE));
  }

  /**
   * Purpose: Reject negative client ID. Expected Result: NotFoundException is thrown. Assertions:
   * Error message matches NotFound.
   */
  @Test
  @DisplayName("Get Address By Client ID - Negative ID - ThrowsNotFoundException")
  void getAddressByClientId_NegativeId_ThrowsNotFoundException() {
    // Arrange
    stubDefaultRepositoryResponses();

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.AddressErrorMessages.NOT_FOUND,
        () -> addressService.getAddressByClientId(-1L));
  }

  /**
   * Purpose: Reject very large client ID when not found. Expected Result: NotFoundException is
   * thrown. Assertions: Error message matches NotFound.
   */
  @Test
  @DisplayName("Get Address By Client ID - Very large ID - ThrowsNotFoundException")
  void getAddressByClientId_VeryLargeId_ThrowsNotFoundException() {
    // Arrange
    stubDefaultRepositoryResponses();

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.AddressErrorMessages.NOT_FOUND,
        () -> addressService.getAddressByClientId(Long.MAX_VALUE));
  }

  /**
   * Purpose: Reject zero client ID. Expected Result: NotFoundException is thrown. Assertions: Error
   * message matches NotFound.
   */
  @Test
  @DisplayName("Get Address By Client ID - Zero ID - ThrowsNotFoundException")
  void getAddressByClientId_ZeroId_ThrowsNotFoundException() {
    // Arrange
    stubDefaultRepositoryResponses();

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.AddressErrorMessages.NOT_FOUND,
        () -> addressService.getAddressByClientId(0L));
  }

  /*
   **********************************************************************************************
   * PERMISSION TESTS
   **********************************************************************************************
   * The following tests verify that authorization is properly configured at the
   * controller level.
   * These tests check that @PreAuthorize annotations are present and correctly
   * configured.
   */
  /**
   * Purpose: Verify that the controller has the correct @PreAuthorize annotation for security.
   * Expected Result: The method should be annotated with @PreAuthorize checking for
   * VIEW_ADDRESS_PERMISSION. Assertions: Annotation is present and contains expected permission
   * string.
   */
  @Test
  @DisplayName("Get Address By Client ID - Controller permission forbidden - Success")
  void getAddressByClientId_p03_controller_permission_forbidden() throws NoSuchMethodException {
    // Arrange
    var method = AddressController.class.getMethod("getAddressByClientId", Long.class);
    stubServiceGetAddressByClientId(DEFAULT_CLIENT_ID, new ArrayList<>());

    // Act
    var preAuthorizeAnnotation =
        method.getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class);
    ResponseEntity<?> response = addressController.getAddressByClientId(DEFAULT_CLIENT_ID);

    // Assert
    assertNotNull(
        preAuthorizeAnnotation, "getAddressByClientId method should have @PreAuthorize annotation");

    String expectedPermission =
        "@customAuthorization.hasAuthority('" + Authorizations.VIEW_ADDRESS_PERMISSION + "')";

    assertEquals(
        expectedPermission,
        preAuthorizeAnnotation.value(),
        "PreAuthorize annotation should reference VIEW_ADDRESS_PERMISSION");
    assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return HTTP 200 OK");
  }
}
