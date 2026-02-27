package com.example.springapi.ServiceTests.Address;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.springapi.ErrorMessages;
import com.example.springapi.controllers.AddressController;
import com.example.springapi.exceptions.BadRequestException;
import com.example.springapi.models.Authorizations;
import com.example.springapi.models.databasemodels.Address;
import com.example.springapi.models.requestmodels.AddressRequestModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Unit tests for AddressService insert functionality. Tests for: InsertAddressTests (68 tests) */
@DisplayName("Insert Address Tests")
class InsertAddressTest extends AddressServiceTestBase {

  // Total Tests: 68
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Accept valid address types (case-insensitive). Expected Result: Insert succeeds for
   * each valid type. Assertions: No exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - Address type BILLING - Success")
  void insertAddress_addressType_BILLING_success() {
    // Arrange
    testAddressRequest.setAddressType("BILLING");
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Accept lowercase home address type. Expected Result: Insert succeeds. Assertions: No
   * exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - Address type home (lowercase) - Success")
  void insertAddress_addressType_home_lowercase_success() {
    // Arrange
    testAddressRequest.setAddressType("home");
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Accept mixed-case Home address type. Expected Result: Insert succeeds. Assertions: No
   * exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - Address type Home (mixed) - Success")
  void insertAddress_addressType_Home_mixedcase_success() {
    // Arrange
    testAddressRequest.setAddressType("Home");
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Accept uppercase HOME address type. Expected Result: Insert succeeds. Assertions: No
   * exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - Address type HOME - Success")
  void insertAddress_addressType_HOME_success() {
    // Arrange
    testAddressRequest.setAddressType("HOME");
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Accept OFFICE address type. Expected Result: Insert succeeds. Assertions: No exception
   * is thrown.
   */
  @Test
  @DisplayName("Insert Address - Address type OFFICE - Success")
  void insertAddress_addressType_OFFICE_success() {
    // Arrange
    testAddressRequest.setAddressType("OFFICE");
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Accept SHIPPING address type. Expected Result: Insert succeeds. Assertions: No
   * exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - Address type SHIPPING - Success")
  void insertAddress_addressType_SHIPPING_success() {
    // Arrange
    testAddressRequest.setAddressType("SHIPPING");
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Accept WAREHOUSE address type. Expected Result: Insert succeeds. Assertions: No
   * exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - Address type WAREHOUSE - Success")
  void insertAddress_addressType_WAREHOUSE_success() {
    // Arrange
    testAddressRequest.setAddressType("WAREHOUSE");
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Accept lowercase work address type. Expected Result: Insert succeeds. Assertions: No
   * exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - Address type work (lowercase) - Success")
  void insertAddress_addressType_work_lowercase_success() {
    // Arrange
    testAddressRequest.setAddressType("work");
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Accept mixed-case Work address type. Expected Result: Insert succeeds. Assertions: No
   * exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - Address type Work (mixed) - Success")
  void insertAddress_addressType_Work_mixedcase_success() {
    // Arrange
    testAddressRequest.setAddressType("Work");
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Accept uppercase WORK address type. Expected Result: Insert succeeds. Assertions: No
   * exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - Address type WORK - Success")
  void insertAddress_addressType_WORK_success() {
    // Arrange
    testAddressRequest.setAddressType("WORK");
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Normalize address type to uppercase on insert. Expected Result: Address type is saved
   * as uppercase. Assertions: Saved address has normalized address type.
   */
  @Test
  @DisplayName("Insert Address - Address type normalized - Success")
  void insertAddress_AddressTypeNormalized_Success() {
    // Arrange
    testAddressRequest.setAddressType("home");
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));

    // Assert
    ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
    verify(addressRepository).save(captor.capture());
    assertEquals("HOME", captor.getValue().getAddressType());
  }

  /**
   * Purpose: Accept all optional fields as null. Expected Result: Insert succeeds. Assertions: No
   * exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - All optional fields null - Success")
  void insertAddress_AllOptionalFieldsNull_Success() {
    // Arrange
    testAddressRequest.setStreetAddress2(null);
    testAddressRequest.setStreetAddress3(null);
    testAddressRequest.setNameOnAddress(null);
    testAddressRequest.setEmailOnAddress(null);
    testAddressRequest.setPhoneOnAddress(null);
    testAddressRequest.setIsPrimary(null);
    testAddressRequest.setIsDeleted(null);
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Accept all optional fields provided. Expected Result: Insert succeeds. Assertions: No
   * exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - All optional fields provided - Success")
  void insertAddress_AllOptionalFieldsProvided_Success() {
    // Arrange
    testAddressRequest.setStreetAddress2("Apt 101");
    testAddressRequest.setStreetAddress3("Building A");
    testAddressRequest.setNameOnAddress("John Doe");
    testAddressRequest.setEmailOnAddress("john@example.com");
    testAddressRequest.setPhoneOnAddress("1234567890");
    testAddressRequest.setIsPrimary(true);
    testAddressRequest.setIsDeleted(false);
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Accept boundary postal code of 5 digits. Expected Result: Insert succeeds. Assertions:
   * No exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - Boundary postal code 5 digits - Success")
  void insertAddress_BoundaryPostalCode5Digits_Success() {
    // Arrange
    testAddressRequest.setPostalCode("00000");
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Accept boundary postal code of 6 digits. Expected Result: Insert succeeds. Assertions:
   * No exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - Boundary postal code 6 digits - Success")
  void insertAddress_BoundaryPostalCode6Digits_Success() {
    // Arrange
    testAddressRequest.setPostalCode("999999");
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Accept case-insensitive HOME address type. Expected Result: Insert succeeds.
   * Assertions: No exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - Case insensitive address type HOME - Success")
  void insertAddress_CaseInsensitiveHome_Success() {
    // Arrange
    testAddressRequest.setAddressType("home");
    testAddressRequest.setCity("CaseHomeCity");
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
    ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
    verify(addressRepository).save(captor.capture());
    assertEquals("HOME", captor.getValue().getAddressType());
    assertEquals("CaseHomeCity", captor.getValue().getCity());
    verify(userLogService, atLeastOnce()).logData(anyLong(), anyString(), anyString());
  }

  /**
   * Purpose: Accept case-insensitive WORK address type. Expected Result: Insert succeeds.
   * Assertions: No exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - Case insensitive address type WORK - Success")
  void insertAddress_CaseInsensitiveWork_Success() {
    // Arrange
    testAddressRequest.setAddressType("work");
    testAddressRequest.setCity("CaseWorkCity");
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
    ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
    verify(addressRepository).save(captor.capture());
    assertEquals("WORK", captor.getValue().getAddressType());
    assertEquals("CaseWorkCity", captor.getValue().getCity());
    verify(userLogService, atLeastOnce()).logData(anyLong(), anyString(), anyString());
  }

  /**
   * Purpose: Accept isDeleted true. Expected Result: Insert succeeds. Assertions: No exception is
   * thrown.
   */
  @Test
  @DisplayName("Insert Address - Boolean isDeleted true - Success")
  void insertAddress_IsDeletedTrue_Success() {
    // Arrange
    testAddressRequest.setIsDeleted(true);
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Accept isPrimary false. Expected Result: Insert succeeds. Assertions: No exception is
   * thrown.
   */
  @Test
  @DisplayName("Insert Address - Boolean isPrimary false - Success")
  void insertAddress_IsPrimaryFalse_Success() {
    // Arrange
    testAddressRequest.setIsPrimary(false);
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Accept isPrimary true. Expected Result: Insert succeeds. Assertions: No exception is
   * thrown.
   */
  @Test
  @DisplayName("Insert Address - Boolean isPrimary true - Success")
  void insertAddress_IsPrimaryTrue_Success() {
    // Arrange
    testAddressRequest.setIsPrimary(true);
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Verify insert logs success route with created ID. Expected Result: Log entry includes
   * success message and route. Assertions: logData is called with INSERT_ADDRESS route and success
   * text.
   */
  @Test
  @DisplayName("Insert Address - Logs success message and route")
  void insertAddress_LogsSuccessMessageAndRoute_Success() {
    // Arrange
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act
    addressService.insertAddress(testAddressRequest);

    // Assert
    verify(userLogService)
        .logData(
            eq(DEFAULT_USER_ID),
            contains(com.example.springapi.SuccessMessages.AddressSuccessMessages.INSERT_ADDRESS),
            eq(com.example.springapi.models.ApiRoutes.AddressSubRoute.INSERT_ADDRESS));
  }

  /**
   * Purpose: Accept max long client ID. Expected Result: Insert succeeds. Assertions: No exception
   * is thrown.
   */
  @Test
  @DisplayName("Insert Address - Max Long client ID - Success")
  void insertAddress_MaxLongClientId_Success() {
    // Arrange
    testAddressRequest.setClientId(Long.MAX_VALUE);
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Accept max long user ID. Expected Result: Insert succeeds. Assertions: No exception is
   * thrown.
   */
  @Test
  @DisplayName("Insert Address - Max Long user ID - Success")
  void insertAddress_MaxLongUserId_Success() {
    // Arrange
    testAddressRequest.setUserId(Long.MAX_VALUE);
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Accept mixed-case address type. Expected Result: Insert succeeds. Assertions: No
   * exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - Mixed case address type - Success")
  void insertAddress_MixedCaseAddressType_Success() {
    // Arrange
    testAddressRequest.setAddressType("BiLLiNg");
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Verify multiple valid fields succeed together. Expected Result: Insert succeeds.
   * Assertions: Save is called.
   */
  @Test
  @DisplayName("Insert Address - Multiple validations pass together - Success")
  void insertAddress_MultipleValidationsPass_Success() {
    // Arrange
    testAddressRequest.setStreetAddress("123 Valid Street");
    testAddressRequest.setCity("ValidCity");
    testAddressRequest.setState("VA");
    testAddressRequest.setPostalCode("12345");
    testAddressRequest.setCountry("USA");
    testAddressRequest.setAddressType("HOME");
    testAddressRequest.setUserId(123L);
    testAddressRequest.setClientId(456L);
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
    verify(addressRepository, times(1)).save(any(Address.class));
  }

  /**
   * Purpose: Default boolean fields when null on insert. Expected Result: isPrimary/isDeleted
   * default false. Assertions: Saved address has default boolean values.
   */
  @Test
  @DisplayName("Insert Address - Null boolean fields - Defaults applied")
  void insertAddress_NullBooleanFields_DefaultsApplied() {
    // Arrange
    testAddressRequest.setCountry("India");
    testAddressRequest.setIsPrimary(null);
    testAddressRequest.setIsDeleted(null);
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));

    // Assert
    ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
    verify(addressRepository).save(captor.capture());
    assertFalse(captor.getValue().getIsPrimary());
    assertFalse(captor.getValue().getIsDeleted());
  }

  /**
   * Purpose: Allow null client ID (optional). Expected Result: Insert succeeds. Assertions: No
   * exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - Null client ID (optional) - Success")
  void insertAddress_NullClientId_Success() {
    // Arrange
    testAddressRequest.setClientId(null);
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Allow null user ID (optional). Expected Result: Insert succeeds. Assertions: No
   * exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - Null user ID (optional) - Success")
  void insertAddress_NullUserId_Success() {
    // Arrange
    testAddressRequest.setUserId(null);
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Accept valid postal code length of 6 digits. Expected Result: Insert succeeds.
   * Assertions: No exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - Postal code 123456 - Success")
  void insertAddress_postalCode_123456_success() {
    // Arrange
    testAddressRequest.setPostalCode("123456");
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Allow valid postal code lengths. Expected Result: Insert succeeds for each postal
   * code. Assertions: No exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - Postal code 12345 - Success")
  void insertAddress_postalCode_12345_success() {
    // Arrange
    testAddressRequest.setPostalCode("12345");
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Accept postal codes with leading zeros. Expected Result: Insert succeeds. Assertions:
   * No exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - Postal code with leading zeros - Success")
  void insertAddress_PostalCodeWithLeadingZeros_Success() {
    // Arrange
    testAddressRequest.setPostalCode("00123");
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Accept special characters in street address. Expected Result: Insert succeeds.
   * Assertions: No exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - Special characters in street address - Success")
  void insertAddress_SpecialCharactersInStreetAddress_Success() {
    // Arrange
    testAddressRequest.setStreetAddress("123 Main St, Apt #101 & Suite B-2");
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Allow street address with leading/trailing spaces. Expected Result: Insert succeeds
   * and trimming occurs. Assertions: No exception is thrown.
   */
  @Test
  @DisplayName(
      "Insert Address - Street address with leading/trailing spaces - Success trims spaces")
  void insertAddress_StreetAddressWithSpaces_SuccessTrimsSpaces() {
    // Arrange
    testAddressRequest.setStreetAddress("  123 Main St  ");
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Accept unicode characters in city. Expected Result: Insert succeeds. Assertions: No
   * exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - Unicode characters in city - Success")
  void insertAddress_UnicodeCharactersInCity_Success() {
    // Arrange
    testAddressRequest.setCity("München");
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /**
   * Purpose: Verify inserting a valid address succeeds. Expected Result: Address is saved and log
   * is written. Assertions: Save and log calls are invoked.
   */
  @Test
  @DisplayName("Insert Address - Valid request - Success")
  void insertAddress_ValidRequest_Success() {
    // Arrange
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));

    // Verify
    verify(addressRepository, times(1)).save(any(Address.class));
    verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
  }

  /**
   * Purpose: Accept very long street addresses. Expected Result: Insert succeeds. Assertions: No
   * exception is thrown.
   */
  @Test
  @DisplayName("Insert Address - Very long street address - Success")
  void insertAddress_VeryLongStreetAddress_Success() {
    // Arrange
    String longAddress = "A".repeat(500);
    testAddressRequest.setStreetAddress(longAddress);
    stubBaseServiceBehaviors();
    stubAddressRepositorySave(testAddress);
    stubUserLogSuccess();

    // Act & Assert
    assertDoesNotThrow(() -> addressService.insertAddress(testAddressRequest));
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Reject empty country. Expected Result: BadRequestException is thrown. Assertions:
   * Error message matches ER005.
   */
  @Test
  @DisplayName("Insert Address - Empty country - ThrowsBadRequestException")
  void insertAddress_EmptyCountry_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setCountry("");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER005, exception.getMessage());
  }

  /**
   * Purpose: Reject empty postal code. Expected Result: BadRequestException is thrown. Assertions:
   * Error message matches ER004.
   */
  @Test
  @DisplayName("Insert Address - Empty postal code - ThrowsBadRequestException")
  void insertAddress_EmptyPostalCode_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setPostalCode("");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER004, exception.getMessage());
  }

  /**
   * Purpose: Reject empty state. Expected Result: BadRequestException is thrown. Assertions: Error
   * message matches ER003.
   */
  @Test
  @DisplayName("Insert Address - Empty state - ThrowsBadRequestException")
  void insertAddress_EmptyState_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setState("");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER003, exception.getMessage());
  }

  /**
   * Purpose: Reject empty street address. Expected Result: BadRequestException is thrown.
   * Assertions: Error message matches ER001.
   */
  @Test
  @DisplayName("Insert Address - Empty street address - ThrowsBadRequestException")
  void insertAddress_EmptyStreetAddress_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setStreetAddress("");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER001, exception.getMessage());
  }

  /**
   * Purpose: Reject invalid address type (COMMERCIAL). Expected Result: BadRequestException is
   * thrown. Assertions: Error message matches ER006.
   */
  @Test
  @DisplayName("Insert Address - Invalid address type (COMMERCIAL) - ThrowsBadRequestException")
  void insertAddress_InvalidAddressTypeCommercial_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setAddressType("COMMERCIAL");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER006, exception.getMessage());
  }

  /**
   * Purpose: Reject invalid address type (empty). Expected Result: BadRequestException is thrown.
   * Assertions: Error message matches ER006.
   */
  @Test
  @DisplayName("Insert Address - Invalid address type (empty) - ThrowsBadRequestException")
  void insertAddress_InvalidAddressTypeEmpty_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setAddressType("");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER006, exception.getMessage());
  }

  /*
   * Purpose: Verify that insertAddress throws BadRequestException when address
   * type contains invalid characters
   * Expected Result: BadRequestException with message ER006
   * Assertions: Exception type and error message
   */
  @Test
  @DisplayName("Insert Address - Invalid address type (home123) - ThrowsBadRequestException")
  void insertAddress_InvalidAddressTypeHome123_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setAddressType("home123");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER006, exception.getMessage());
  }

  /**
   * Purpose: Reject invalid address types. Expected Result: BadRequestException is thrown.
   * Assertions: Error message matches ER006.
   */
  @Test
  @DisplayName("Insert Address - Invalid address type (INVALID) - ThrowsBadRequestException")
  void insertAddress_InvalidAddressTypeInvalid_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setAddressType("INVALID");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER006, exception.getMessage());
  }

  /**
   * Purpose: Reject residential address type (not supported). Expected Result: BadRequestException
   * is thrown. Assertions: Error message matches ER006.
   */
  @Test
  @DisplayName("Insert Address - Invalid address type (RESIDENTIAL) - ThrowsBadRequestException")
  void insertAddress_InvalidAddressTypeResidential_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setAddressType("RESIDENTIAL");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER006, exception.getMessage());
  }

  /**
   * Purpose: Reject invalid created user. Expected Result: BadRequestException is thrown.
   * Assertions: Error message matches InvalidUser.
   */
  @Test
  @DisplayName("Insert Address - Invalid created user - ThrowsBadRequestException")
  void insertAddress_InvalidCreatedUser_ThrowsBadRequestException() {
    // Arrange
    stubServiceGetUser(" ");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.UserErrorMessages.INVALID_USER, exception.getMessage());
  }

  /**
   * Purpose: Reject invalid postal code with hyphens. Expected Result: BadRequestException is
   * thrown. Assertions: Error message matches ER007.
   */
  @Test
  @DisplayName("Insert Address - Invalid postal code (hyphens) - ThrowsBadRequestException")
  void insertAddress_InvalidPostalCodeHyphens_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setPostalCode("12-345");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER007, exception.getMessage());
  }

  /**
   * Purpose: Reject invalid postal code with letters. Expected Result: BadRequestException is
   * thrown. Assertions: Error message matches ER007.
   */
  @Test
  @DisplayName("Insert Address - Invalid postal code (letters) - ThrowsBadRequestException")
  void insertAddress_InvalidPostalCodeLetters_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setPostalCode("abcde");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER007, exception.getMessage());
  }

  /**
   * Purpose: Reject invalid postal code with periods. Expected Result: BadRequestException is
   * thrown. Assertions: Error message matches ER007.
   */
  @Test
  @DisplayName("Insert Address - Invalid postal code (periods) - ThrowsBadRequestException")
  void insertAddress_InvalidPostalCodePeriods_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setPostalCode("12.345");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER007, exception.getMessage());
  }

  /**
   * Purpose: Reject invalid postal code with spaces. Expected Result: BadRequestException is
   * thrown. Assertions: Error message matches ER007.
   */
  @Test
  @DisplayName("Insert Address - Invalid postal code (spaces) - ThrowsBadRequestException")
  void insertAddress_InvalidPostalCodeSpaces_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setPostalCode("12 345");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER007, exception.getMessage());
  }

  /**
   * Purpose: Reject negative client ID. Expected Result: BadRequestException is thrown. Assertions:
   * Error message matches InvalidId.
   */
  @Test
  @DisplayName("Insert Address - Negative client ID - ThrowsBadRequestException")
  void insertAddress_NegativeClientId_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setClientId(-1L);

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, exception.getMessage());
  }

  /**
   * Purpose: Reject negative user ID. Expected Result: BadRequestException is thrown. Assertions:
   * Error message matches InvalidId.
   */
  @Test
  @DisplayName("Insert Address - Negative user ID - ThrowsBadRequestException")
  void insertAddress_NegativeUserId_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setUserId(-1L);

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.UserErrorMessages.INVALID_ID, exception.getMessage());
  }

  /**
   * Purpose: Reject null address type. Expected Result: BadRequestException is thrown. Assertions:
   * Error message matches ER006.
   */
  @Test
  @DisplayName("Insert Address - Null address type - ThrowsBadRequestException")
  void insertAddress_NullAddressType_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setAddressType(null);

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER006, exception.getMessage());
  }

  /**
   * Purpose: Reject null country. Expected Result: BadRequestException is thrown. Assertions: Error
   * message matches ER005.
   */
  @Test
  @DisplayName("Insert Address - Null country - ThrowsBadRequestException")
  void insertAddress_NullCountry_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setCountry(null);

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER005, exception.getMessage());
  }

  /**
   * Purpose: Reject null postal code. Expected Result: BadRequestException is thrown. Assertions:
   * Error message matches ER004.
   */
  @Test
  @DisplayName("Insert Address - Null postal code - ThrowsBadRequestException")
  void insertAddress_NullPostalCode_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setPostalCode(null);

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER004, exception.getMessage());
  }

  /**
   * Purpose: Reject null insert request. Expected Result: BadRequestException is thrown.
   * Assertions: Error message matches ER001 and save is not called.
   */
  @Test
  @DisplayName("Insert Address - Null request - ThrowsBadRequestException")
  void insertAddress_NullRequest_ThrowsBadRequestException() {
    // Arrange
    AddressRequestModel nullRequest = null;

    // Act & Assert
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> addressService.insertAddress(nullRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER001, exception.getMessage());
    verify(addressRepository, never()).save(any(Address.class));
  }

  /**
   * Purpose: Reject null state. Expected Result: BadRequestException is thrown. Assertions: Error
   * message matches ER003.
   */
  @Test
  @DisplayName("Insert Address - Null state - ThrowsBadRequestException")
  void insertAddress_NullState_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setState(null);

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER003, exception.getMessage());
  }

  /**
   * Purpose: Reject postal code of 4 digits. Expected Result: BadRequestException is thrown.
   * Assertions: Error message matches ER007.
   */
  @Test
  @DisplayName("Insert Address - Postal code 4 digits - ThrowsBadRequestException")
  void insertAddress_PostalCode4Digits_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setPostalCode("1234");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER007, exception.getMessage());
  }

  /**
   * Purpose: Reject postal code of 7 digits. Expected Result: BadRequestException is thrown.
   * Assertions: Error message matches ER007.
   */
  @Test
  @DisplayName("Insert Address - Postal code 7 digits - ThrowsBadRequestException")
  void insertAddress_PostalCode7Digits_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setPostalCode("1234567");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER007, exception.getMessage());
  }

  /**
   * Purpose: Reject whitespace-only country. Expected Result: BadRequestException is thrown.
   * Assertions: Error message matches ER005.
   */
  @Test
  @DisplayName("Insert Address - Whitespace only country - ThrowsBadRequestException")
  void insertAddress_WhitespaceCountry_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setCountry("   ");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER005, exception.getMessage());
  }

  /**
   * Purpose: Reject whitespace-only postal code. Expected Result: BadRequestException is thrown.
   * Assertions: Error message matches ER004.
   */
  @Test
  @DisplayName("Insert Address - Whitespace only postal code - ThrowsBadRequestException")
  void insertAddress_WhitespacePostalCode_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setPostalCode("   ");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER004, exception.getMessage());
  }

  /**
   * Purpose: Reject whitespace-only state. Expected Result: BadRequestException is thrown.
   * Assertions: Error message matches ER003.
   */
  @Test
  @DisplayName("Insert Address - Whitespace only state - ThrowsBadRequestException")
  void insertAddress_WhitespaceState_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setState("   ");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER003, exception.getMessage());
  }

  /**
   * Purpose: Reject whitespace-only street address. Expected Result: BadRequestException is thrown.
   * Assertions: Error message matches ER001.
   */
  @Test
  @DisplayName("Insert Address - Whitespace only street address - ThrowsBadRequestException")
  void insertAddress_WhitespaceStreetAddress_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setStreetAddress("   ");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.AddressErrorMessages.ER001, exception.getMessage());
  }

  /**
   * Purpose: Reject zero client ID. Expected Result: BadRequestException is thrown. Assertions:
   * Error message matches InvalidId.
   */
  @Test
  @DisplayName("Insert Address - Zero client ID - ThrowsBadRequestException")
  void insertAddress_ZeroClientId_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setClientId(0L);

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, exception.getMessage());
  }

  /**
   * Purpose: Reject zero user ID. Expected Result: BadRequestException is thrown. Assertions: Error
   * message matches InvalidId.
   */
  @Test
  @DisplayName("Insert Address - Zero user ID - ThrowsBadRequestException")
  void insertAddress_ZeroUserId_ThrowsBadRequestException() {
    // Arrange
    testAddressRequest.setUserId(0L);

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> addressService.insertAddress(testAddressRequest));

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
   */
  /**
   * Purpose: Verify that the controller has the correct @PreAuthorize annotation for security.
   * Expected Result: The method should be annotated with @PreAuthorize checking for
   * INSERT_ADDRESS_PERMISSION. Assertions: Annotation is present and contains expected permission
   * string.
   */
  @Test
  @DisplayName("Insert Address - Controller permission forbidden - Success")
  void insertAddress_p03_controller_permission_forbidden() throws NoSuchMethodException {
    // Arrange
    var method =
        AddressController.class.getMethod(
            "createAddress", com.example.springapi.models.requestmodels.AddressRequestModel.class);
    stubServiceInsertAddressDoNothing();

    // Act
    var preAuthorizeAnnotation =
        method.getAnnotation(org.springframework.security.access.prepost.PreAuthorize.class);
    ResponseEntity<?> response = addressController.createAddress(testAddressRequest);

    // Assert
    assertNotNull(
        preAuthorizeAnnotation, "createAddress method should have @PreAuthorize annotation");

    String expectedPermission =
        "@customAuthorization.hasAuthority('" + Authorizations.INSERT_ADDRESS_PERMISSION + "')";

    assertEquals(
        expectedPermission,
        preAuthorizeAnnotation.value(),
        "PreAuthorize annotation should reference INSERT_ADDRESS_PERMISSION");
    assertEquals(
        HttpStatus.CREATED,
        response.getStatusCode(),
        "Should return HTTP 201 Created on successful insertion");
  }
}
