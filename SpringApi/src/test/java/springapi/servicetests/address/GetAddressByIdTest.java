package com.example.springapi.ServiceTests.Address;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.springapi.ErrorMessages;
import com.example.springapi.controllers.AddressController;
import com.example.springapi.models.Authorizations;
import com.example.springapi.models.responsemodels.AddressResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for AddressService.getAddressById() method. Tests retrieval of address by ID with
 * various scenarios.
 */
@DisplayName("Get Address By ID Tests")
class GetAddressByIdTest extends AddressServiceTestBase {

  // Total Tests: 11
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify address details are returned for a valid ID. Expected Result: Response contains
   * address fields. Assertions: Response fields match the test address.
   */
  @Test
  @DisplayName("Get Address By ID - Address found - Success returns address details")
  void getAddressById_AddressFound_Success() {
    // Arrange
    stubDefaultRepositoryResponses();

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
   * Purpose: Verify null optional fields are handled. Expected Result: Response contains nulls for
   * optional fields. Assertions: Optional fields are null in response.
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
    stubDefaultRepositoryResponses();

    // Act
    AddressResponseModel result = addressService.getAddressById(DEFAULT_ADDRESS_ID);

    // Assert
    assertNotNull(result);
    assertNull(result.getStreetAddress2());
    assertNull(result.getStreetAddress3());
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Validate missing address ID returns not found. Expected Result: NotFoundException is
   * thrown. Assertions: Error message matches NotFound.
   */
  @Test
  @DisplayName("Get Address By ID - Address not found - ThrowsNotFoundException")
  void getAddressById_AddressNotFound_ThrowsNotFoundException() {
    // Arrange
    stubDefaultRepositoryResponses();

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.AddressErrorMessages.NOT_FOUND,
        () -> addressService.getAddressById(DEFAULT_ADDRESS_ID + 1));
  }

  /**
   * Purpose: Verify deleted address returns not found. Expected Result: NotFoundException is thrown
   * when address is deleted. Assertions: Error message matches NotFound.
   */
  @Test
  @DisplayName("Get Address By ID - Deleted address - ThrowsNotFoundException")
  void getAddressById_DeletedAddress_ThrowsNotFoundException() {
    // Arrange
    testAddress.setIsDeleted(true);
    stubDefaultRepositoryResponses();

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.AddressErrorMessages.NOT_FOUND,
        () -> addressService.getAddressById(DEFAULT_ADDRESS_ID));
    verify(addressRepository, times(1)).findById(DEFAULT_ADDRESS_ID);
  }

  /**
   * Purpose: Validate max long ID is rejected when not found. Expected Result: NotFoundException is
   * thrown. Assertions: Error message matches NotFound.
   */
  @Test
  @DisplayName("Get Address By ID - Max Long value - ThrowsNotFoundException")
  void getAddressById_MaxLongValue_ThrowsNotFoundException() {
    // Arrange
    stubDefaultRepositoryResponses();

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.AddressErrorMessages.NOT_FOUND,
        () -> addressService.getAddressById(Long.MAX_VALUE));
  }

  /**
   * Purpose: Validate min long ID is rejected when not found. Expected Result: NotFoundException is
   * thrown. Assertions: Error message matches NotFound.
   */
  @Test
  @DisplayName("Get Address By ID - Min Long value - ThrowsNotFoundException")
  void getAddressById_MinLongValue_ThrowsNotFoundException() {
    // Arrange
    stubDefaultRepositoryResponses();

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.AddressErrorMessages.NOT_FOUND,
        () -> addressService.getAddressById(Long.MIN_VALUE));
  }

  /**
   * Purpose: Validate negative ID is rejected. Expected Result: NotFoundException is thrown.
   * Assertions: Error message matches NotFound.
   */
  @Test
  @DisplayName("Get Address By ID - Negative ID - ThrowsNotFoundException")
  void getAddressById_NegativeId_ThrowsNotFoundException() {
    // Arrange
    long negativeId = -1L;
    stubDefaultRepositoryResponses();

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.AddressErrorMessages.NOT_FOUND,
        () -> addressService.getAddressById(negativeId));
  }

  /**
   * Purpose: Validate zero ID is rejected. Expected Result: NotFoundException is thrown.
   * Assertions: Error message matches NotFound.
   */
  @Test
  @DisplayName("Get Address By ID - Zero ID - ThrowsNotFoundException")
  void getAddressById_ZeroId_ThrowsNotFoundException() {
    // Arrange
    long zeroId = 0L;
    stubDefaultRepositoryResponses();

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.AddressErrorMessages.NOT_FOUND, () -> addressService.getAddressById(zeroId));
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
  @DisplayName("Get Address By ID - Controller permission forbidden - Success")
  void getAddressById_p03_controller_permission_forbidden() throws NoSuchMethodException {
    // Arrange
    var method = AddressController.class.getMethod("getAddressById", Long.class);
    AddressResponseModel mockResponse = new AddressResponseModel();
    mockResponse.setAddressId(DEFAULT_ADDRESS_ID);
    stubServiceGetAddressById(DEFAULT_ADDRESS_ID, mockResponse);

    // Act
    var preAuthorizeAnnotation =
        method.getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class);
    ResponseEntity<?> response = addressController.getAddressById(DEFAULT_ADDRESS_ID);

    // Assert
    assertNotNull(
        preAuthorizeAnnotation, "getAddressById method should have @PreAuthorize annotation");

    String expectedPermission =
        "@customAuthorization.hasAuthority('" + Authorizations.VIEW_ADDRESS_PERMISSION + "')";

    assertEquals(
        expectedPermission,
        preAuthorizeAnnotation.value(),
        "PreAuthorize annotation should reference VIEW_ADDRESS_PERMISSION");
    assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return HTTP 200 OK");
  }
}
