package springapi.modeltests.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import springapi.models.databasemodels.Address;
import springapi.models.responsemodels.AddressResponseModel;

class AddressResponseModelTest {

  // Total Tests: 4
  @Test
  void addressResponseModel_DefaultConstructor_LeavesFieldsNull() {
    AddressResponseModel response = new AddressResponseModel();

    assertNull(response.getAddressId());
    assertNull(response.getCity());
    assertNull(response.getCreatedAt());
  }

  @Test
  void addressResponseModel_EntityConstructor_MapsAllFields() {
    LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 10, 30);
    LocalDateTime updatedAt = LocalDateTime.of(2025, 1, 2, 11, 45);
    Address entity = new Address();
    entity.setAddressId(101L);
    entity.setUserId(11L);
    entity.setClientId(22L);
    entity.setAddressType("HOME");
    entity.setStreetAddress("123 Main St");
    entity.setStreetAddress2("Apt 4");
    entity.setStreetAddress3("Near Park");
    entity.setCity("Mumbai");
    entity.setState("MH");
    entity.setPostalCode("400001");
    entity.setNameOnAddress("John");
    entity.setEmailOnAddress("john@example.com");
    entity.setPhoneOnAddress("9999999999");
    entity.setCountry("India");
    entity.setIsPrimary(Boolean.TRUE);
    entity.setIsDeleted(Boolean.FALSE);
    entity.setCreatedAt(createdAt);
    entity.setCreatedUser("creator");
    entity.setUpdatedAt(updatedAt);
    entity.setModifiedUser("modifier");
    entity.setNotes("Address notes");

    AddressResponseModel response = new AddressResponseModel(entity);

    assertEquals(101L, response.getAddressId());
    assertEquals(11L, response.getUserId());
    assertEquals(22L, response.getClientId());
    assertEquals("HOME", response.getAddressType());
    assertEquals("123 Main St", response.getStreetAddress());
    assertEquals("Apt 4", response.getStreetAddress2());
    assertEquals("Near Park", response.getStreetAddress3());
    assertEquals("Mumbai", response.getCity());
    assertEquals("MH", response.getState());
    assertEquals("400001", response.getPostalCode());
    assertEquals("John", response.getNameOnAddress());
    assertEquals("john@example.com", response.getEmailOnAddress());
    assertEquals("9999999999", response.getPhoneOnAddress());
    assertEquals("India", response.getCountry());
    assertEquals(Boolean.TRUE, response.getIsPrimary());
    assertEquals(Boolean.FALSE, response.getIsDeleted());
    assertEquals(createdAt, response.getCreatedAt());
    assertEquals("creator", response.getCreatedUser());
    assertEquals(updatedAt, response.getUpdatedAt());
    assertEquals("modifier", response.getModifiedUser());
    assertEquals("Address notes", response.getNotes());
  }

  @Test
  void addressResponseModel_NullEntityConstructor_DoesNotThrowAndKeepsDefaults() {
    AddressResponseModel response = new AddressResponseModel(null);

    assertNull(response.getAddressId());
    assertNull(response.getStreetAddress());
    assertNull(response.getUpdatedAt());
  }

  @Test
  void addressResponseModel_JacksonSerializationAndDeserialization_RoundTripWorks()
      throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    AddressResponseModel response = new AddressResponseModel();
    response.setAddressId(501L);
    response.setStreetAddress("742 Evergreen Terrace");
    response.setCity("Springfield");
    response.setIsPrimary(Boolean.TRUE);

    String json = mapper.writeValueAsString(response);
    AddressResponseModel parsed = mapper.readValue(json, AddressResponseModel.class);

    assertTrue(json.contains("\"streetAddress\":\"742 Evergreen Terrace\""));
    assertEquals(501L, parsed.getAddressId());
    assertEquals("Springfield", parsed.getCity());
    assertEquals(Boolean.TRUE, parsed.getIsPrimary());
  }
}
