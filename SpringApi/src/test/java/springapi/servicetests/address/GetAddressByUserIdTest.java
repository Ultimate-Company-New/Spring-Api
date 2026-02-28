package springapi.ServiceTests.Address;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import springapi.ErrorMessages;
import springapi.controllers.AddressController;
import springapi.models.Authorizations;
import springapi.models.databasemodels.Address;
import springapi.models.responsemodels.AddressResponseModel;

/** Unit tests for AddressService getAddressByUserId functionality. */
@DisplayName("Get Address By User ID Tests")
class GetAddressByUserIdTest extends AddressServiceTestBase {

  // Total Tests: 12
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify multiple addresses returned for a user. Expected Result: List contains all
   * addresses. Assertions: Response size and fields match expected.
   */
  @Test
  @DisplayName("Get Address By User ID - Multiple addresses found - Success returns list")
  void getAddressByUserId_MultipleAddressesFound_Success() {
    // Arrange
    Address secondAddress = createTestAddress(2L, "WORK");
    List<Address> addresses = Arrays.asList(testAddress, secondAddress);
    stubDefaultRepositoryResponses();
    stubFindAddressesByUserId(DEFAULT_USER_ID, addresses);

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
    verify(addressRepository, times(1))
        .findByUserIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_USER_ID, false);
  }

  /**
   * Purpose: Handle user with no addresses. Expected Result: Empty list is returned. Assertions:
   * Result is empty and repository is called.
   */
  @Test
  @DisplayName("Get Address By User ID - No addresses - Success returns empty list")
  void getAddressByUserId_NoAddresses_ReturnsEmptyList() {
    // Arrange
    stubDefaultRepositoryResponses();
    stubFindAddressesByUserId(DEFAULT_USER_ID, new ArrayList<>());

    // Act
    List<AddressResponseModel> result = addressService.getAddressByUserId(DEFAULT_USER_ID);

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(addressRepository, times(1))
        .findByUserIdAndIsDeletedOrderByAddressIdDesc(DEFAULT_USER_ID, false);
  }

  /**
   * Purpose: Verify single address response for user. Expected Result: List contains one address.
   * Assertions: Result size is one.
   */
  @Test
  @DisplayName("Get Address By User ID - Single address - Success returns list with one item")
  void getAddressByUserId_SingleAddress_Success() {
    // Arrange
    stubDefaultRepositoryResponses();
    stubFindAddressesByUserId(DEFAULT_USER_ID, List.of(testAddress));

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
   * Purpose: Reject very large user ID when not found. Expected Result: NotFoundException is
   * thrown. Assertions: Error message matches NotFound.
   */
  @Test
  @DisplayName("Get Address By User ID - Max Long value - ThrowsNotFoundException")
  void getAddressByUserId_MaxLongValue_ThrowsNotFoundException() {
    // Arrange
    stubDefaultRepositoryResponses();

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.AddressErrorMessages.NOT_FOUND,
        () -> addressService.getAddressByUserId(Long.MAX_VALUE));
  }

  /**
   * Purpose: Reject min long user ID when not found. Expected Result: NotFoundException is thrown.
   * Assertions: Error message matches NotFound.
   */
  @Test
  @DisplayName("Get Address By User ID - Min Long value - ThrowsNotFoundException")
  void getAddressByUserId_MinLongValue_ThrowsNotFoundException() {
    // Arrange
    stubDefaultRepositoryResponses();

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.AddressErrorMessages.NOT_FOUND,
        () -> addressService.getAddressByUserId(Long.MIN_VALUE));
  }

  /**
   * Purpose: Reject negative user ID. Expected Result: NotFoundException is thrown. Assertions:
   * Error message matches NotFound.
   */
  @Test
  @DisplayName("Get Address By User ID - Negative ID - ThrowsNotFoundException")
  void getAddressByUserId_NegativeId_ThrowsNotFoundException() {
    // Arrange
    long negativeId = -1L;
    stubDefaultRepositoryResponses();

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.AddressErrorMessages.NOT_FOUND,
        () -> addressService.getAddressByUserId(negativeId));
  }

  /**
   * Purpose: Reject deleted user. Expected Result: NotFoundException is thrown. Assertions: Error
   * message matches NotFound.
   */
  @Test
  @DisplayName("Get Address By User ID - User deleted - ThrowsNotFoundException")
  void getAddressByUserId_UserDeleted_ThrowsNotFoundException() {
    // Arrange
    testUser.setIsDeleted(true);
    stubDefaultRepositoryResponses();

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.AddressErrorMessages.NOT_FOUND,
        () -> addressService.getAddressByUserId(DEFAULT_USER_ID));
    verify(addressRepository, never())
        .findByUserIdAndIsDeletedOrderByAddressIdDesc(anyLong(), anyBoolean());
  }

  /**
   * Purpose: Reject user not found. Expected Result: NotFoundException is thrown. Assertions: Error
   * message matches NotFound.
   */
  @Test
  @DisplayName("Get Address By User ID - User not found - ThrowsNotFoundException")
  void getAddressByUserId_UserNotFound_ThrowsNotFoundException() {
    // Arrange
    stubDefaultRepositoryResponses();

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.AddressErrorMessages.NOT_FOUND,
        () -> addressService.getAddressByUserId(DEFAULT_USER_ID + 1));
    verify(addressRepository, never())
        .findByUserIdAndIsDeletedOrderByAddressIdDesc(anyLong(), anyBoolean());
  }

  /**
   * Purpose: Reject zero user ID. Expected Result: NotFoundException is thrown. Assertions: Error
   * message matches NotFound.
   */
  @Test
  @DisplayName("Get Address By User ID - Zero ID - ThrowsNotFoundException")
  void getAddressByUserId_ZeroId_ThrowsNotFoundException() {
    // Arrange
    long zeroId = 0L;
    stubDefaultRepositoryResponses();

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.AddressErrorMessages.NOT_FOUND,
        () -> addressService.getAddressByUserId(zeroId));
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
  @DisplayName("Get Address By User ID - Controller permission forbidden - Success")
  void getAddressByUserId_p03_controller_permission_forbidden() throws NoSuchMethodException {
    // Arrange
    var method = AddressController.class.getMethod("getAddressesByUserId", Long.class);
    stubServiceGetAddressByUserId(DEFAULT_USER_ID, new ArrayList<>());

    // Act
    var preAuthorizeAnnotation =
        method.getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class);
    ResponseEntity<?> response = addressController.getAddressesByUserId(DEFAULT_USER_ID);

    // Assert
    assertNotNull(
        preAuthorizeAnnotation, "getAddressesByUserId method should have @PreAuthorize annotation");

    String expectedPermission =
        "@customAuthorization.hasAuthority('" + Authorizations.VIEW_ADDRESS_PERMISSION + "')";

    assertEquals(
        expectedPermission,
        preAuthorizeAnnotation.value(),
        "PreAuthorize annotation should reference VIEW_ADDRESS_PERMISSION");
    assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return HTTP 200 OK");
  }
}
