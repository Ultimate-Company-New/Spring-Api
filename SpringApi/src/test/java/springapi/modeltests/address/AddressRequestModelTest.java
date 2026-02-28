package springapi.ModelTests.Address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import springapi.models.requestmodels.AddressRequestModel;

class AddressRequestModelTest {

  // Total Tests: 4
  @Test
  void addressRequestModel_DefaultValues_AreInitializedCorrectly() {
    AddressRequestModel model = new AddressRequestModel();

    assertFalse(model.isIncludeDeleted());
    assertNull(model.getId());
    assertNull(model.getUserId());
    assertNull(model.getClientId());
  }

  @Test
  void addressRequestModel_SettersAndGetters_RoundTripSuccessfully() {
    AddressRequestModel model = new AddressRequestModel();
    model.setId(11L);
    model.setUserId(22L);
    model.setClientId(33L);
    model.setIncludeDeleted(true);
    model.setAddressType("HOME");
    model.setStreetAddress("123 Main St");
    model.setStreetAddress2("Apt 4");
    model.setStreetAddress3("Landmark");
    model.setCity("New York");
    model.setState("NY");
    model.setPostalCode("10001");
    model.setNameOnAddress("John Doe");
    model.setEmailOnAddress("john@example.com");
    model.setPhoneOnAddress("1234567890");
    model.setCountry("USA");
    model.setIsPrimary(Boolean.TRUE);
    model.setIsDeleted(Boolean.FALSE);

    assertEquals(11L, model.getId());
    assertEquals(22L, model.getUserId());
    assertEquals(33L, model.getClientId());
    assertTrue(model.isIncludeDeleted());
    assertEquals("HOME", model.getAddressType());
    assertEquals("123 Main St", model.getStreetAddress());
    assertEquals("Apt 4", model.getStreetAddress2());
    assertEquals("Landmark", model.getStreetAddress3());
    assertEquals("New York", model.getCity());
    assertEquals("NY", model.getState());
    assertEquals("10001", model.getPostalCode());
    assertEquals("John Doe", model.getNameOnAddress());
    assertEquals("john@example.com", model.getEmailOnAddress());
    assertEquals("1234567890", model.getPhoneOnAddress());
    assertEquals("USA", model.getCountry());
    assertEquals(Boolean.TRUE, model.getIsPrimary());
    assertEquals(Boolean.FALSE, model.getIsDeleted());
  }

  @Test
  void addressRequestModel_JacksonSerialization_UsesModelFieldNames() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    AddressRequestModel model = new AddressRequestModel();
    model.setStreetAddress("123 Main St");
    model.setPostalCode("10001");
    model.setIncludeDeleted(true);

    String json = mapper.writeValueAsString(model);

    assertTrue(json.contains("\"streetAddress\":\"123 Main St\""));
    assertTrue(json.contains("\"postalCode\":\"10001\""));
    assertTrue(json.contains("\"includeDeleted\":true"));
  }

  @Test
  void addressRequestModel_JacksonDeserialization_MapsFieldsCorrectly() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    String json =
        """
                {
                  "id": 5,
                  "userId": 10,
                  "addressType": "WORK",
                  "streetAddress": "221B Baker Street",
                  "postalCode": "12345",
                  "includeDeleted": true
                }
                """;

    AddressRequestModel model = mapper.readValue(json, AddressRequestModel.class);

    assertEquals(5L, model.getId());
    assertEquals(10L, model.getUserId());
    assertEquals("WORK", model.getAddressType());
    assertEquals("221B Baker Street", model.getStreetAddress());
    assertEquals("12345", model.getPostalCode());
    assertTrue(model.isIncludeDeleted());
  }
}
