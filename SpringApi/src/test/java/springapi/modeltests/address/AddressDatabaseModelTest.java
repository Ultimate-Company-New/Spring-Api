package springapi.modeltests.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import springapi.ErrorMessages;
import springapi.exceptions.BadRequestException;
import springapi.models.databasemodels.Address;
import springapi.models.requestmodels.AddressRequestModel;

class AddressDatabaseModelTest {

  // Total Tests: 9
  @Test
  void address_CreateConstructor_ValidRequestNormalizesAndSetsAuditFields() {
    AddressRequestModel request = createValidRequest();
    request.setAddressType("shipping");
    request.setStreetAddress("  221B Baker Street ");
    request.setCity("  London ");
    request.setState("  LN ");
    request.setPostalCode("12345");
    request.setNameOnAddress("  Sherlock ");
    request.setIsPrimary(null);
    request.setIsDeleted(null);

    Address address = new Address(request, "admin");

    assertEquals("SHIPPING", address.getAddressType());
    assertEquals("221B Baker Street", address.getStreetAddress());
    assertEquals("London", address.getCity());
    assertEquals("LN", address.getState());
    assertEquals("12345", address.getPostalCode());
    assertEquals("Sherlock", address.getNameOnAddress());
    assertFalse(address.getIsPrimary());
    assertFalse(address.getIsDeleted());
    assertEquals("admin", address.getCreatedUser());
    assertEquals("admin", address.getModifiedUser());
  }

  @Test
  void address_UpdateConstructor_PreservesImmutableFieldsAndUpdatesModifier() {
    Address existing = new Address();
    LocalDateTime createdAt = LocalDateTime.of(2025, 1, 10, 8, 15);
    existing.setAddressId(999L);
    existing.setCreatedUser("creator");
    existing.setCreatedAt(createdAt);

    AddressRequestModel request = createValidRequest();
    request.setStreetAddress("New Street");

    Address updated = new Address(request, "editor", existing);

    assertEquals(999L, updated.getAddressId());
    assertEquals("creator", updated.getCreatedUser());
    assertEquals(createdAt, updated.getCreatedAt());
    assertEquals("editor", updated.getModifiedUser());
    assertEquals("New Street", updated.getStreetAddress());
  }

  @Test
  void address_CreateConstructor_NullRequestThrowsBadRequest() {
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> new Address(null, "admin"));
    assertEquals(ErrorMessages.AddressErrorMessages.ER001, exception.getMessage());
  }

  @Test
  void address_CreateConstructor_InvalidPostalCodeThrowsBadRequest() {
    AddressRequestModel request = createValidRequest();
    request.setPostalCode("12AB");

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> new Address(request, "admin"));
    assertEquals(ErrorMessages.AddressErrorMessages.ER007, exception.getMessage());
  }

  @Test
  void address_CreateConstructor_InvalidAddressTypeThrowsBadRequest() {
    AddressRequestModel request = createValidRequest();
    request.setAddressType("INVALID_TYPE");

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> new Address(request, "admin"));
    assertEquals(ErrorMessages.AddressErrorMessages.ER006, exception.getMessage());
  }

  @Test
  void address_CreateConstructor_InvalidUserIdThrowsBadRequest() {
    AddressRequestModel request = createValidRequest();
    request.setUserId(0L);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> new Address(request, "admin"));
    assertEquals(ErrorMessages.UserErrorMessages.INVALID_ID, exception.getMessage());
  }

  @Test
  void address_CreateConstructor_InvalidClientIdThrowsBadRequest() {
    AddressRequestModel request = createValidRequest();
    request.setClientId(-10L);

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> new Address(request, "admin"));
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, exception.getMessage());
  }

  @Test
  void address_CreateConstructor_BlankCreatedUserThrowsBadRequest() {
    AddressRequestModel request = createValidRequest();

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> new Address(request, " "));
    assertEquals(ErrorMessages.UserErrorMessages.INVALID_USER, exception.getMessage());
  }

  @Test
  void address_ValidateRequest_PublicMethodRejectsMissingCity() {
    AddressRequestModel request = createValidRequest();
    request.setCity(" ");

    Address model = new Address();
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> model.validateRequest(request));
    assertEquals(ErrorMessages.AddressErrorMessages.ER002, exception.getMessage());
  }

  private AddressRequestModel createValidRequest() {
    AddressRequestModel request = new AddressRequestModel();
    request.setAddressType("HOME");
    request.setStreetAddress("123 Main St");
    request.setCity("New York");
    request.setState("NY");
    request.setPostalCode("10001");
    request.setCountry("USA");
    request.setIsPrimary(Boolean.TRUE);
    request.setIsDeleted(Boolean.FALSE);
    return request;
  }
}
