package com.example.SpringApi.Helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.ShippingResponseModel.ShipRocketOrderResponseModel;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ShipRocketHelper Tests")
class ShipRocketHelperTest {

  // Total Tests: 39
  private static final String BASE_URL = "https://apiv2.shiprocket.in/v1/external";

  /**
   * Purpose: Verify cached token path returns without making API requests. Expected Result: Cached
   * token value. Assertions: Returned token equality.
   */
  @Test
  @DisplayName("getToken - Cached Token Returned Without Network - Success")
  void getToken_s01_cachedTokenReturnedWithoutNetwork_success() throws Exception {
    // Arrange
    ShipRocketHelper helper = new ShipRocketHelper("user@example.com", "password");
    setField(helper, "cachedToken", "cached-token");
    setField(helper, "tokenExpiresAt", System.currentTimeMillis() + 60_000L);

    // Act
    String token = helper.getToken();

    // Assert
    assertEquals("cached-token", token);
  }

  /**
   * Purpose: Verify createCustomOrder fails when response body deserializes to null. Expected
   * Result: BadRequestException. Assertions: Exception message equality.
   */
  @Test
  @DisplayName("createCustomOrder - Null Response Throws - Success")
  void createCustomOrder_s02_nullResponseThrows_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    helper.stubResponse("POST", BASE_URL + "/orders/create/adhoc", "null");

    // Act
    BadRequestException exception =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.createCustomOrder(Map.of("order", "value")));

    // Assert
    assertEquals("ShipRocket create order returned null response", exception.getMessage());
  }

  /**
   * Purpose: Verify createCustomOrder fails when response message indicates error. Expected Result:
   * BadRequestException with message content. Assertions: Exception message equality.
   */
  @Test
  @DisplayName("createCustomOrder - Error Message Throws - Success")
  void createCustomOrder_s03_errorMessageThrows_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    helper.stubResponse(
        "POST",
        BASE_URL + "/orders/create/adhoc",
        "{\"order_id\":123,\"shipment_id\":456,\"message\":\"invalid order payload\"}");

    // Act
    BadRequestException exception =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.createCustomOrder(Map.of("order", "value")));

    // Assert
    assertEquals("ShipRocket create order failed: invalid order payload", exception.getMessage());
  }

  /**
   * Purpose: Verify createCustomOrder fails when order_id is missing. Expected Result:
   * BadRequestException. Assertions: Exception message contains missing order_id info.
   */
  @Test
  @DisplayName("createCustomOrder - Missing OrderId Throws - Success")
  void createCustomOrder_s04_missingOrderIdThrows_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    helper.stubResponse("POST", BASE_URL + "/orders/create/adhoc", "{\"shipment_id\":456}");

    // Act
    BadRequestException exception =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.createCustomOrder(Map.of("order", "value")));

    // Assert
    assertEquals(
        "ShipRocket create order response missing order_id. Response message: none",
        exception.getMessage());
  }

  /**
   * Purpose: Verify createCustomOrder fails when shipment_id is missing. Expected Result:
   * BadRequestException. Assertions: Exception message equality.
   */
  @Test
  @DisplayName("createCustomOrder - Missing ShipmentId Throws - Success")
  void createCustomOrder_s05_missingShipmentIdThrows_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    helper.stubResponse("POST", BASE_URL + "/orders/create/adhoc", "{\"order_id\":123}");

    // Act
    BadRequestException exception =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.createCustomOrder(Map.of("order", "value")));

    // Assert
    assertEquals(
        "ShipRocket create order response missing shipment_id for order: 123",
        exception.getMessage());
  }

  /**
   * Purpose: Verify createCustomOrder returns parsed response for valid payload. Expected Result:
   * Parsed order response model. Assertions: Order/shipment IDs.
   */
  @Test
  @DisplayName("createCustomOrder - Success Returns Response - Success")
  void createCustomOrder_s06_successReturnsResponse_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    helper.stubResponse(
        "POST",
        BASE_URL + "/orders/create/adhoc",
        "{\"order_id\":123,\"shipment_id\":456,\"message\":\"ok\"}");

    // Act
    ShipRocketOrderResponseModel response = helper.createCustomOrder(Map.of("order", "value"));

    // Assert
    assertEquals(123L, response.getOrderId());
    assertEquals(456L, response.getShipmentId());
  }

  /**
   * Purpose: Verify assignAwbAsJson validates shipment ID input. Expected Result:
   * BadRequestException on null shipment ID. Assertions: Exception message equality.
   */
  @Test
  @DisplayName("assignAwbAsJson - Null Shipment Throws - Success")
  void assignAwbAsJson_s07_nullShipmentThrows_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();

    // Act
    BadRequestException exception =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.assignAwbAsJson(null, 12L));

    // Assert
    assertEquals("Shipment ID is required for AWB assignment", exception.getMessage());
  }

  /**
   * Purpose: Verify assignAwbAsJson validates courier ID input. Expected Result:
   * BadRequestException on null courier ID. Assertions: Exception message equality.
   */
  @Test
  @DisplayName("assignAwbAsJson - Null Courier Throws - Success")
  void assignAwbAsJson_s08_nullCourierThrows_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();

    // Act
    BadRequestException exception =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.assignAwbAsJson(10L, null));

    // Assert
    assertEquals("Courier ID is required for AWB assignment", exception.getMessage());
  }

  /**
   * Purpose: Verify assignAwbAsJson response validation for null, failed status, and empty AWB
   * code. Expected Result: BadRequestException for each invalid branch. Assertions: Exception
   * message contents.
   */
  @Test
  @DisplayName("assignAwbAsJson - Invalid Response Branches Throw - Success")
  void assignAwbAsJson_s09_invalidResponseBranchesThrow_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    String endpoint = BASE_URL + "/courier/assign/awb";

    helper.stubResponse("POST", endpoint, "null");

    // Act
    BadRequestException nullResponse =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.assignAwbAsJson(10L, 20L));

    helper.stubResponse("POST", endpoint, "{\"awb_assign_status\":0}");
    BadRequestException failedStatus =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.assignAwbAsJson(10L, 20L));

    helper.stubResponse(
        "POST", endpoint, "{\"awb_assign_status\":1,\"response\":{\"data\":{\"awb_code\":\"\"}}}");
    BadRequestException emptyAwb =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.assignAwbAsJson(10L, 20L));

    // Assert
    assertEquals(
        "ShipRocket AWB assignment returned null response for shipment: 10",
        nullResponse.getMessage());
    assertTrue(failedStatus.getMessage().contains("awb_assign_status: 0"));
    assertEquals(
        "ShipRocket AWB assignment returned empty AWB code for shipment: 10",
        emptyAwb.getMessage());
  }

  /**
   * Purpose: Verify assignAwbAsJson returns raw JSON on success. Expected Result: Original response
   * body. Assertions: Raw JSON equality.
   */
  @Test
  @DisplayName("assignAwbAsJson - Success Returns Raw Json - Success")
  void assignAwbAsJson_s10_successReturnsRawJson_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    String json =
        "{\"awb_assign_status\":1,\"response\":{\"data\":{\"awb_code\":\"AWB123\",\"shipment_id\":100}}}";
    helper.stubResponse("POST", BASE_URL + "/courier/assign/awb", json);

    // Act
    String result = helper.assignAwbAsJson(100L, 200L);

    // Assert
    assertEquals(json, result);
  }

  /**
   * Purpose: Verify generatePickupAsJson validates null shipment ID. Expected Result:
   * BadRequestException. Assertions: Exception message equality.
   */
  @Test
  @DisplayName("generatePickupAsJson - Null Shipment Throws - Success")
  void generatePickupAsJson_s11_nullShipmentThrows_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();

    // Act
    BadRequestException exception =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.generatePickupAsJson(null));

    // Assert
    assertEquals("Shipment ID is required for pickup generation", exception.getMessage());
  }

  /**
   * Purpose: Verify generatePickupAsJson fails for null and unsuccessful responses. Expected
   * Result: BadRequestException for both branches. Assertions: Exception messages.
   */
  @Test
  @DisplayName("generatePickupAsJson - Null Or Failed Response Throws - Success")
  void generatePickupAsJson_s12_nullOrFailedResponseThrows_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    String endpoint = BASE_URL + "/courier/generate/pickup";

    helper.stubResponse("POST", endpoint, "null");
    BadRequestException nullResponse =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.generatePickupAsJson(10L));

    helper.stubResponse("POST", endpoint, "{\"pickup_status\":0}");
    BadRequestException failed =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.generatePickupAsJson(10L));

    // Assert
    assertEquals(
        "ShipRocket pickup generation returned null response for shipment: 10",
        nullResponse.getMessage());
    assertTrue(failed.getMessage().contains("pickup_status: 0"));
  }

  /**
   * Purpose: Verify generatePickupAsJson returns raw JSON for successful status. Expected Result:
   * Response body string. Assertions: JSON equality.
   */
  @Test
  @DisplayName("generatePickupAsJson - Success Returns Raw Json - Success")
  void generatePickupAsJson_s13_successReturnsRawJson_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    String json = "{\"pickup_status\":1}";
    helper.stubResponse("POST", BASE_URL + "/courier/generate/pickup", json);

    // Act
    String result = helper.generatePickupAsJson(10L);

    // Assert
    assertEquals(json, result);
  }

  /**
   * Purpose: Verify generateManifest validates null shipment ID. Expected Result:
   * BadRequestException. Assertions: Exception message equality.
   */
  @Test
  @DisplayName("generateManifest - Null Shipment Throws - Success")
  void generateManifest_s14_nullShipmentThrows_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();

    // Act
    BadRequestException exception =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.generateManifest(null));

    // Assert
    assertEquals("Shipment ID is required for manifest generation", exception.getMessage());
  }

  /**
   * Purpose: Verify generateManifest fails for null and unsuccessful responses. Expected Result:
   * BadRequestException for both branches. Assertions: Exception messages.
   */
  @Test
  @DisplayName("generateManifest - Null Or Failed Response Throws - Success")
  void generateManifest_s15_nullOrFailedResponseThrows_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    String endpoint = BASE_URL + "/manifests/generate";

    helper.stubResponse("POST", endpoint, "null");
    BadRequestException nullResponse =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.generateManifest(10L));

    helper.stubResponse("POST", endpoint, "{\"status\":0}");
    BadRequestException failed =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.generateManifest(10L));

    // Assert
    assertEquals(
        "ShipRocket manifest generation returned null response for shipment: 10",
        nullResponse.getMessage());
    assertTrue(failed.getMessage().contains("status: 0"));
  }

  /**
   * Purpose: Verify generateManifest returns manifest URL for successful response. Expected Result:
   * Manifest URL string. Assertions: URL equality.
   */
  @Test
  @DisplayName("generateManifest - Success Returns ManifestUrl - Success")
  void generateManifest_s16_successReturnsManifestUrl_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    helper.stubResponse(
        "POST",
        BASE_URL + "/manifests/generate",
        "{\"status\":1,\"manifest_url\":\"https://manifest\"}");

    // Act
    String manifestUrl = helper.generateManifest(10L);

    // Assert
    assertEquals("https://manifest", manifestUrl);
  }

  /**
   * Purpose: Verify generateLabel validates null shipment ID. Expected Result: BadRequestException.
   * Assertions: Exception message equality.
   */
  @Test
  @DisplayName("generateLabel - Null Shipment Throws - Success")
  void generateLabel_s17_nullShipmentThrows_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();

    // Act
    BadRequestException exception =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.generateLabel(null));

    // Assert
    assertEquals("Shipment ID is required for label generation", exception.getMessage());
  }

  /**
   * Purpose: Verify generateLabel fails for null and unsuccessful responses. Expected Result:
   * BadRequestException for both branches. Assertions: Exception messages.
   */
  @Test
  @DisplayName("generateLabel - Null Or Failed Response Throws - Success")
  void generateLabel_s18_nullOrFailedResponseThrows_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    String endpoint = BASE_URL + "/courier/generate/label";

    helper.stubResponse("POST", endpoint, "null");
    BadRequestException nullResponse =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.generateLabel(10L));

    helper.stubResponse("POST", endpoint, "{\"label_created\":0}");
    BadRequestException failed =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.generateLabel(10L));

    // Assert
    assertEquals(
        "ShipRocket label generation returned null response for shipment: 10",
        nullResponse.getMessage());
    assertTrue(failed.getMessage().contains("label_created: 0"));
  }

  /**
   * Purpose: Verify generateLabel returns label URL for successful response. Expected Result: Label
   * URL string. Assertions: URL equality.
   */
  @Test
  @DisplayName("generateLabel - Success Returns LabelUrl - Success")
  void generateLabel_s19_successReturnsLabelUrl_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    helper.stubResponse(
        "POST",
        BASE_URL + "/courier/generate/label",
        "{\"label_created\":1,\"label_url\":\"https://label\"}");

    // Act
    String labelUrl = helper.generateLabel(10L);

    // Assert
    assertEquals("https://label", labelUrl);
  }

  /**
   * Purpose: Verify generateInvoice validates null shipment ID. Expected Result:
   * BadRequestException. Assertions: Exception message equality.
   */
  @Test
  @DisplayName("generateInvoice - Null Shipment Throws - Success")
  void generateInvoice_s20_nullShipmentThrows_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();

    // Act
    BadRequestException exception =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.generateInvoice(null));

    // Assert
    assertEquals("Shipment ID is required for invoice generation", exception.getMessage());
  }

  /**
   * Purpose: Verify generateInvoice fails for null and unsuccessful responses. Expected Result:
   * BadRequestException for both branches. Assertions: Exception messages.
   */
  @Test
  @DisplayName("generateInvoice - Null Or Failed Response Throws - Success")
  void generateInvoice_s21_nullOrFailedResponseThrows_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    String endpoint = BASE_URL + "/orders/print/invoice";

    helper.stubResponse("POST", endpoint, "null");
    BadRequestException nullResponse =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.generateInvoice(10L));

    helper.stubResponse("POST", endpoint, "{\"is_invoice_created\":false}");
    BadRequestException failed =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.generateInvoice(10L));

    // Assert
    assertEquals(
        "ShipRocket invoice generation returned null response for shipment: 10",
        nullResponse.getMessage());
    assertTrue(failed.getMessage().contains("is_invoice_created: false"));
  }

  /**
   * Purpose: Verify generateInvoice returns invoice URL for successful response. Expected Result:
   * Invoice URL string. Assertions: URL equality.
   */
  @Test
  @DisplayName("generateInvoice - Success Returns InvoiceUrl - Success")
  void generateInvoice_s22_successReturnsInvoiceUrl_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    helper.stubResponse(
        "POST",
        BASE_URL + "/orders/print/invoice",
        "{\"is_invoice_created\":true,\"invoice_url\":\"https://invoice\"}");

    // Act
    String invoiceUrl = helper.generateInvoice(10L);

    // Assert
    assertEquals("https://invoice", invoiceUrl);
  }

  /**
   * Purpose: Verify getTrackingAsJson validates blank AWB input. Expected Result:
   * BadRequestException. Assertions: Exception message equality.
   */
  @Test
  @DisplayName("getTrackingAsJson - Blank Awb Throws - Success")
  void getTrackingAsJson_s23_blankAwbThrows_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();

    // Act
    BadRequestException exception =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.getTrackingAsJson("   "));

    // Assert
    assertEquals("AWB code is required for tracking", exception.getMessage());
  }

  /**
   * Purpose: Verify getTrackingAsJson fails for null response and missing tracking_data. Expected
   * Result: BadRequestException for both branches. Assertions: Exception messages.
   */
  @Test
  @DisplayName("getTrackingAsJson - Null Or Missing TrackingData Throws - Success")
  void getTrackingAsJson_s24_nullOrMissingTrackingDataThrows_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    String endpoint = BASE_URL + "/courier/track/awb/AWB1";

    helper.stubResponse("GET", endpoint, "null");
    BadRequestException nullResponse =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.getTrackingAsJson("AWB1"));

    helper.stubResponse("GET", endpoint, "{}");
    BadRequestException missingData =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.getTrackingAsJson("AWB1"));

    // Assert
    assertEquals(
        "ShipRocket tracking returned null response for AWB: AWB1", nullResponse.getMessage());
    assertTrue(missingData.getMessage().contains("returned no tracking_data"));
  }

  /**
   * Purpose: Verify getTrackingAsJson returns raw JSON when tracking_data exists. Expected Result:
   * Raw JSON string. Assertions: JSON equality.
   */
  @Test
  @DisplayName("getTrackingAsJson - Success Returns Raw Json - Success")
  void getTrackingAsJson_s25_successReturnsRawJson_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    String json = "{\"tracking_data\":{\"track_status\":1}}";
    helper.stubResponse("GET", BASE_URL + "/courier/track/awb/AWB1", json);

    // Act
    String result = helper.getTrackingAsJson("AWB1");

    // Assert
    assertEquals(json, result);
  }

  /**
   * Purpose: Verify getOrderDetailsAsJson validates blank order ID input. Expected Result:
   * BadRequestException. Assertions: Exception message equality.
   */
  @Test
  @DisplayName("getOrderDetailsAsJson - Blank OrderId Throws - Success")
  void getOrderDetailsAsJson_s26_blankOrderIdThrows_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();

    // Act
    BadRequestException exception =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.getOrderDetailsAsJson(" "));

    // Assert
    assertEquals("ShipRocket order ID is required to fetch order details", exception.getMessage());
  }

  /**
   * Purpose: Verify getOrderDetailsAsJson fails for null response, empty data, and missing data.id.
   * Expected Result: BadRequestException for each invalid branch. Assertions: Exception messages.
   */
  @Test
  @DisplayName("getOrderDetailsAsJson - Null Empty MissingId Branches Throw - Success")
  void getOrderDetailsAsJson_s27_nullEmptyMissingIdBranchesThrow_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    String endpoint = BASE_URL + "/orders/show/ORD1";

    helper.stubResponse("GET", endpoint, "null");
    BadRequestException nullResponse =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.getOrderDetailsAsJson("ORD1"));

    helper.stubResponse("GET", endpoint, "{}");
    BadRequestException emptyData =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.getOrderDetailsAsJson("ORD1"));

    helper.stubResponse("GET", endpoint, "{\"data\":{}}");
    BadRequestException missingId =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.getOrderDetailsAsJson("ORD1"));

    // Assert
    assertTrue(nullResponse.getMessage().contains("returned null response"));
    assertTrue(emptyData.getMessage().contains("returned empty data"));
    assertTrue(missingId.getMessage().contains("without order ID"));
  }

  /**
   * Purpose: Verify getOrderDetailsAsJson returns raw JSON on valid response. Expected Result: Raw
   * JSON string. Assertions: JSON equality.
   */
  @Test
  @DisplayName("getOrderDetailsAsJson - Success Returns Raw Json - Success")
  void getOrderDetailsAsJson_s28_successReturnsRawJson_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    String json = "{\"data\":{\"id\":123}}";
    helper.stubResponse("GET", BASE_URL + "/orders/show/ORD1", json);

    // Act
    String result = helper.getOrderDetailsAsJson("ORD1");

    // Assert
    assertEquals(json, result);
  }

  /**
   * Purpose: Verify createReturnOrder fails for null response, error message, and isSuccess false.
   * Expected Result: BadRequestException for each branch. Assertions: Exception messages.
   */
  @Test
  @DisplayName("createReturnOrder - Null Error And False Branches Throw - Success")
  void createReturnOrder_s29_nullErrorAndFalseBranchesThrow_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    String endpoint = BASE_URL + "/orders/create/return";

    helper.stubResponse("POST", endpoint, "null");
    BadRequestException nullResponse =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.createReturnOrder(Map.of("key", "value")));

    helper.stubResponse("POST", endpoint, "{\"order_id\":10,\"message\":\"failed request\"}");
    BadRequestException errorMessage =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.createReturnOrder(Map.of("key", "value")));

    helper.stubResponse("POST", endpoint, "{\"order_id\":0}");
    BadRequestException falseSuccess =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.createReturnOrder(Map.of("key", "value")));

    // Assert
    assertEquals(
        "ShipRocket create return order returned null response", nullResponse.getMessage());
    assertEquals(
        "ShipRocket create return order failed: failed request", errorMessage.getMessage());
    assertTrue(falseSuccess.getMessage().contains("create return order failed"));
  }

  /**
   * Purpose: Verify createReturnOrder returns parsed model on valid response. Expected Result:
   * Success model with order and shipment IDs. Assertions: Response field values.
   */
  @Test
  @DisplayName("createReturnOrder - Success Returns Model - Success")
  void createReturnOrder_s30_successReturnsModel_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    helper.stubResponse(
        "POST", BASE_URL + "/orders/create/return", "{\"order_id\":99,\"shipment_id\":77}");

    // Act
    com.example.SpringApi.Models.ShippingResponseModel.ShipRocketReturnOrderResponseModel response =
        helper.createReturnOrder(Map.of("key", "value"));

    // Assert
    assertEquals(99L, response.getOrderId());
    assertEquals(77L, response.getShipmentId());
  }

  /**
   * Purpose: Verify createReturnOrderAsJson covers invalid and success branches. Expected Result:
   * Exception for invalid response and raw JSON for success. Assertions: Exception message and JSON
   * equality.
   */
  @Test
  @DisplayName("createReturnOrderAsJson - Invalid And Success Branches - Success")
  void createReturnOrderAsJson_s31_invalidAndSuccessBranches_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    String endpoint = BASE_URL + "/orders/create/return";

    helper.stubResponse("POST", endpoint, "{\"order_id\":0}");
    BadRequestException invalid =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class,
            () -> helper.createReturnOrderAsJson(Map.of("key", "value")));

    String successJson = "{\"order_id\":101,\"shipment_id\":88}";
    helper.stubResponse("POST", endpoint, successJson);

    // Act
    String result = helper.createReturnOrderAsJson(Map.of("key", "value"));

    // Assert
    assertTrue(invalid.getMessage().contains("create return order failed"));
    assertEquals(successJson, result);
  }

  /**
   * Purpose: Verify assignReturnAwbAsJson validates null shipment ID. Expected Result:
   * BadRequestException. Assertions: Exception message equality.
   */
  @Test
  @DisplayName("assignReturnAwbAsJson - Null Shipment Throws - Success")
  void assignReturnAwbAsJson_s32_nullShipmentThrows_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();

    // Act
    BadRequestException exception =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.assignReturnAwbAsJson(null));

    // Assert
    assertEquals("Shipment ID is required for return AWB assignment", exception.getMessage());
  }

  /**
   * Purpose: Verify assignReturnAwbAsJson covers invalid response branches and success path.
   * Expected Result: Exceptions for invalid branches and raw JSON for success. Assertions:
   * Exception messages and JSON equality.
   */
  @Test
  @DisplayName("assignReturnAwbAsJson - Invalid Branches And Success - Success")
  void assignReturnAwbAsJson_s33_invalidBranchesAndSuccess_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    String endpoint = BASE_URL + "/courier/assign/awb";

    helper.stubResponse("POST", endpoint, "null");
    BadRequestException nullResponse =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.assignReturnAwbAsJson(10L));

    helper.stubResponse("POST", endpoint, "{\"awb_assign_status\":0}");
    BadRequestException failed =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.assignReturnAwbAsJson(10L));

    helper.stubResponse(
        "POST", endpoint, "{\"awb_assign_status\":1,\"response\":{\"data\":{\"awb_code\":\"\"}}}");
    BadRequestException emptyAwb =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.assignReturnAwbAsJson(10L));

    String successJson =
        "{\"awb_assign_status\":1,\"response\":{\"data\":{\"awb_code\":\"RAWB1\",\"shipment_id\":10}}}";
    helper.stubResponse("POST", endpoint, successJson);

    // Act
    String result = helper.assignReturnAwbAsJson(10L);

    // Assert
    assertTrue(nullResponse.getMessage().contains("returned null response"));
    assertTrue(failed.getMessage().contains("awb_assign_status: 0"));
    assertTrue(emptyAwb.getMessage().contains("returned empty AWB code"));
    assertEquals(successJson, result);
  }

  /**
   * Purpose: Verify cancelOrders validates null or empty order ID lists. Expected Result:
   * BadRequestException for invalid inputs. Assertions: Exception message equality.
   */
  @Test
  @DisplayName("cancelOrders - Null Or Empty Throws - Success")
  void cancelOrders_s34_nullOrEmptyThrows_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();

    // Act
    BadRequestException nullList =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.cancelOrders(null));

    BadRequestException emptyList =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, () -> helper.cancelOrders(List.of()));

    // Assert
    assertEquals("Order IDs are required for cancellation", nullList.getMessage());
    assertEquals("Order IDs are required for cancellation", emptyList.getMessage());
  }

  /**
   * Purpose: Verify cancelOrders completes without exception on successful API response. Expected
   * Result: No exception thrown. Assertions: Method completes and request metadata recorded.
   */
  @Test
  @DisplayName("cancelOrders - Success Completes - Success")
  void cancelOrders_s35_successCompletes_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    helper.stubResponse("POST", BASE_URL + "/orders/cancel", "{}");

    // Act
    helper.cancelOrders(List.of(1L, 2L));

    // Assert
    assertEquals("POST", helper.lastMethodType);
    assertEquals(BASE_URL + "/orders/cancel", helper.lastUrl);
  }

  /**
   * Purpose: Verify getWalletBalance parses numeric balance from valid response. Expected Result:
   * Parsed double balance. Assertions: Parsed value equality.
   */
  @Test
  @DisplayName("getWalletBalance - Success Parses Double - Success")
  void getWalletBalance_s36_successParsesDouble_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    helper.stubResponse(
        "GET",
        BASE_URL + "/account/details/wallet-balance",
        "{\"data\":{\"balance_amount\":\"123.45\"}}");

    // Act
    Double balance = helper.getWalletBalance();

    // Assert
    assertEquals(123.45, balance);
  }

  /**
   * Purpose: Verify getWalletBalance fails when response is missing balance_amount. Expected
   * Result: BadRequestException. Assertions: Exception message prefix.
   */
  @Test
  @DisplayName("getWalletBalance - Missing Balance Throws - Success")
  void getWalletBalance_s37_missingBalanceThrows_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    helper.stubResponse("GET", BASE_URL + "/account/details/wallet-balance", "{\"data\":{}}");

    // Act
    BadRequestException exception =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, helper::getWalletBalance);

    // Assert
    assertEquals(
        "Invalid wallet balance response from ShipRocket: missing balance_amount",
        exception.getMessage());
  }

  /**
   * Purpose: Verify getWalletBalance fails when balance_amount is not numeric. Expected Result:
   * BadRequestException. Assertions: Exception message prefix.
   */
  @Test
  @DisplayName("getWalletBalance - Invalid Number Throws - Success")
  void getWalletBalance_s38_invalidNumberThrows_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    helper.stubResponse(
        "GET",
        BASE_URL + "/account/details/wallet-balance",
        "{\"data\":{\"balance_amount\":\"NaN-Value\"}}");

    // Act
    BadRequestException exception =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, helper::getWalletBalance);

    // Assert
    assertTrue(exception.getMessage().startsWith("Invalid wallet balance format from ShipRocket:"));
  }

  /**
   * Purpose: Verify getWalletBalance wraps malformed JSON parsing failures. Expected Result:
   * BadRequestException. Assertions: Exception message prefix.
   */
  @Test
  @DisplayName("getWalletBalance - Malformed Json Throws - Success")
  void getWalletBalance_s39_malformedJsonThrows_success() {
    // Arrange
    StubShipRocketHelper helper = new StubShipRocketHelper();
    helper.stubResponse("GET", BASE_URL + "/account/details/wallet-balance", "{");

    // Act
    BadRequestException exception =
        org.junit.jupiter.api.Assertions.assertThrows(
            BadRequestException.class, helper::getWalletBalance);

    // Assert
    assertTrue(exception.getMessage().startsWith("Failed to parse wallet balance response:"));
  }

  private static void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  private static final class StubShipRocketHelper extends ShipRocketHelper {
    private final Map<String, String> stubbedResponses = new HashMap<>();
    private String lastMethodType;
    private String lastUrl;

    StubShipRocketHelper() {
      super("user@example.com", "password");
    }

    void stubResponse(String methodType, String url, String responseBody) {
      stubbedResponses.put(methodType + " " + url, responseBody);
    }

    @Override
    public synchronized String getToken() {
      return "stub-token";
    }

    @Override
    protected String httpResponseRaw(String token, String url, String methodType, Object content) {
      lastMethodType = methodType;
      lastUrl = url;
      String key = methodType + " " + url;
      if (!stubbedResponses.containsKey(key)) {
        throw new BadRequestException("No stubbed response configured for " + key);
      }
      return stubbedResponses.get(key);
    }
  }
}
