package com.example.springapi.ServiceTests.Address;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.springapi.ErrorMessages;
import com.example.springapi.controllers.AddressController;
import com.example.springapi.models.Authorizations;
import com.example.springapi.models.databasemodels.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Unit tests for AddressService.toggleAddress() method. */
@DisplayName("Toggle Address Tests")
class ToggleAddressTest extends AddressServiceTestBase {

  // Total Tests: 13
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify toggle from active to deleted state. Expected Result: Address is saved with
   * isDeleted set to true. Assertions: State change and repository interactions verified.
   */
  @Test
  @DisplayName("Toggle Address - Address found and active - Success toggles to deleted")
  void toggleAddress_AddressFoundActive_Success() {
    // Arrange
    testAddress.setIsDeleted(false);
    stubDefaultRepositoryResponses();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act
    assertDoesNotThrow(() -> addressService.toggleAddress(DEFAULT_ADDRESS_ID));

    // Assert
    assertTrue(testAddress.getIsDeleted());
    verify(addressRepository, times(1)).findById(DEFAULT_ADDRESS_ID);
    verify(addressRepository, times(1)).save(any(Address.class));
    verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
  }

  /**
   * Purpose: Verify toggle from deleted to active state. Expected Result: Address is saved with
   * isDeleted set to false. Assertions: State change and repository interactions verified.
   */
  @Test
  @DisplayName("Toggle Address - Address found and deleted - Success toggles to active")
  void toggleAddress_AddressFoundDeleted_Success() {
    // Arrange
    testAddress.setIsDeleted(true);
    stubDefaultRepositoryResponses();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act
    assertDoesNotThrow(() -> addressService.toggleAddress(DEFAULT_ADDRESS_ID));

    // Assert
    assertFalse(testAddress.getIsDeleted());
    verify(addressRepository, times(1)).findById(DEFAULT_ADDRESS_ID);
    verify(addressRepository, times(1)).save(any(Address.class));
  }

  /**
   * Purpose: Verify explicit state transitions through multiple toggles. Expected Result: State
   * toggles correctly on each call. Assertions: Final state and interaction counts match
   * expectations.
   */
  @Test
  @DisplayName("Toggle Address - Multiple Toggles - State Transitions")
  void toggleAddress_MultipleToggles_StateTransitions() {
    // Arrange
    testAddress.setIsDeleted(false);
    stubDefaultRepositoryResponses();
    stubAddressRepositorySave(testAddress);

    // Act & Assert
    // First toggle: false -> true
    addressService.toggleAddress(DEFAULT_ADDRESS_ID);
    assertTrue(testAddress.getIsDeleted());

    // Second toggle: true -> false
    testAddress.setIsDeleted(true);
    addressService.toggleAddress(DEFAULT_ADDRESS_ID);
    assertFalse(testAddress.getIsDeleted());

    verify(addressRepository, times(2)).save(any(Address.class));
  }

  /**
   * Purpose: Verify multiple toggles in sequence successfully update the repository. Expected
   * Result: repository.save is called for each toggle. Assertions: Repository interactions verified
   * for both calls.
   */
  @Test
  @DisplayName("Toggle Address - Multiple toggles in sequence - Success")
  void toggleAddress_MultipleToggles_Success() {
    // Arrange
    testAddress.setIsDeleted(false);
    stubDefaultRepositoryResponses();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act - First toggle
    assertDoesNotThrow(() -> addressService.toggleAddress(DEFAULT_ADDRESS_ID));
    assertTrue(testAddress.getIsDeleted());

    // Act - Second toggle
    assertDoesNotThrow(() -> addressService.toggleAddress(DEFAULT_ADDRESS_ID));
    assertFalse(testAddress.getIsDeleted());

    // Assert
    verify(addressRepository, times(2)).findById(DEFAULT_ADDRESS_ID);
    verify(addressRepository, times(2)).save(any(Address.class));
  }

  /**
   * Purpose: Specifically verify active to deleted state change in logs. Expected Result: Success
   * log entry is created. Assertions: logData is invoked.
   */
  @Test
  @DisplayName("Toggle Address - Verify toggle state changes - Active to Deleted")
  void toggleAddress_StateChange_ActiveToDeleted() {
    // Arrange
    testAddress.setIsDeleted(false);
    stubDefaultRepositoryResponses();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act
    assertDoesNotThrow(() -> addressService.toggleAddress(DEFAULT_ADDRESS_ID));

    // Assert
    assertTrue(testAddress.getIsDeleted());
    verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
  }

  /**
   * Purpose: Specifically verify deleted to active state change in logs. Expected Result: Success
   * log entry is created. Assertions: logData is invoked.
   */
  @Test
  @DisplayName("Toggle Address - Verify toggle state changes - Deleted to Active")
  void toggleAddress_StateChange_DeletedToActive() {
    // Arrange
    testAddress.setIsDeleted(true);
    stubDefaultRepositoryResponses();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act
    assertDoesNotThrow(() -> addressService.toggleAddress(DEFAULT_ADDRESS_ID));

    // Assert
    assertFalse(testAddress.getIsDeleted());
    verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Validate missing address ID throws NotFound. Expected Result: NotFoundException is
   * thrown. Assertions: Error message matches expectations and save is never called.
   */
  @Test
  @DisplayName("Toggle Address - Address not found - ThrowsNotFoundException")
  void toggleAddress_AddressNotFound_ThrowsNotFoundException() {
    // Arrange
    stubDefaultRepositoryResponses();

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.AddressErrorMessages.NOT_FOUND,
        () -> addressService.toggleAddress(DEFAULT_ADDRESS_ID + 1));
    verify(addressRepository, never()).save(any(Address.class));
    verify(userLogService, never()).logData(anyLong(), anyString(), anyString());
  }

  /**
   * Purpose: Validate min long ID throws NotFound. Expected Result: NotFoundException is thrown.
   * Assertions: Error message matches expectations.
   */
  @Test
  @DisplayName("Toggle Address - Min Long value - ThrowsNotFoundException")
  void toggleAddress_MinLongValue_ThrowsNotFoundException() {
    // Arrange
    stubDefaultRepositoryResponses();

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.AddressErrorMessages.NOT_FOUND,
        () -> addressService.toggleAddress(Long.MIN_VALUE));
  }

  /**
   * Purpose: Validate negative ID throws NotFound. Expected Result: NotFoundException is thrown.
   * Assertions: Error message matches expectations.
   */
  @Test
  @DisplayName("Toggle Address - Negative ID - ThrowsNotFoundException")
  void toggleAddress_NegativeId_ThrowsNotFoundException() {
    // Arrange
    long negativeId = -1L;
    stubDefaultRepositoryResponses();

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.AddressErrorMessages.NOT_FOUND,
        () -> addressService.toggleAddress(negativeId));
  }

  /**
   * Purpose: Validate zero ID throws NotFound. Expected Result: NotFoundException is thrown.
   * Assertions: Error message matches expectations.
   */
  @Test
  @DisplayName("Toggle Address - Zero ID - ThrowsNotFoundException")
  void toggleAddress_ZeroId_ThrowsNotFoundException() {
    // Arrange
    long zeroId = 0L;
    stubDefaultRepositoryResponses();

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.AddressErrorMessages.NOT_FOUND, () -> addressService.toggleAddress(zeroId));
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
   * DELETE_ADDRESS_PERMISSION. Assertions: Annotation is present and contains expected permission
   * string.
   */
  @Test
  @DisplayName("Toggle Address - Controller permission forbidden - Success")
  void toggleAddress_p03_controller_permission_forbidden() throws NoSuchMethodException {
    // Arrange
    var method = AddressController.class.getMethod("toggleAddress", Long.class);
    stubServiceToggleAddressDoNothing(DEFAULT_ADDRESS_ID);

    // Act
    var preAuthorizeAnnotation =
        method.getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class);
    ResponseEntity<?> response = addressController.toggleAddress(DEFAULT_ADDRESS_ID);

    // Assert
    assertNotNull(
        preAuthorizeAnnotation, "toggleAddress method should have @PreAuthorize annotation");

    String expectedPermission =
        "@customAuthorization.hasAuthority('" + Authorizations.DELETE_ADDRESS_PERMISSION + "')";

    assertEquals(
        expectedPermission,
        preAuthorizeAnnotation.value(),
        "PreAuthorize annotation should reference DELETE_ADDRESS_PERMISSION");
    assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return HTTP 200 OK");
  }
}
