package com.example.SpringApi.Helpers;

import com.example.SpringApi.Adapters.DateAdapter;
import com.example.SpringApi.Adapters.LocalDateTimeAdapter;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.PickupLocation;
import com.example.SpringApi.Models.ShippingResponseModel.AddPickupLocationResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.ShipRocketAwbResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.ShipRocketInvoiceResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.ShipRocketLabelResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.ShipRocketManifestResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.ShipRocketOrderResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.ShipRocketPickupResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.ShipRocketReturnOrderResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.ShipRocketTrackingResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.ShippingOptionsResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.TokenResponseModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import com.nimbusds.jose.shaded.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class ShipRocketHelper {
  private static final String API_URL = "https://apiv2.shiprocket.in/v1/external";
  private static final String SHIPMENT_ID_KEY = "shipment_id";
  private final String email;
  private final String password;

  /**
   * Timeout for HTTP requests to Shiprocket API (5 seconds). Prevents hanging if the API is slow or
   * unresponsive.
   */
  private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(5);

  /**
   * Cached token and its expiration time. Shiprocket tokens typically expire after some time, so we
   * cache and reuse.
   */
  private String cachedToken = null;

  private long tokenExpiresAt = 0;
  private static final long TOKEN_CACHE_DURATION_MS =
      55L * 60 * 1000; // 55 minutes (tokens usually valid for 1 hour)

  /** Create an HttpClient with timeout configuration. */
  protected HttpClient createHttpClient() {
    return HttpClient.newBuilder().connectTimeout(HTTP_TIMEOUT).build();
  }

  public ShipRocketHelper(String email, String password) {
    this.email = email;
    this.password = password;
  }

  /** Creates a configured Gson instance for JSON serialization/deserialization. */
  protected Gson createGson() {
    return new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .registerTypeAdapter(Date.class, new DateAdapter())
        .create();
  }

  /**
   * Base HTTP method that returns raw JSON response string. All other HTTP methods should use this
   * to avoid duplicating HTTP logic.
   *
   * @param token Authorization token (can be null for unauthenticated requests)
   * @param url Full URL to call
   * @param methodType HTTP method (GET, POST, PUT, DELETE)
   * @param content Request body object (will be serialized to JSON)
   * @return Raw JSON response string
   * @throws BadRequestException if HTTP status is not 200 or on any error
   */
  protected String httpResponseRaw(String token, String url, String methodType, Object content) {
    try {
      HttpClient client = createHttpClient();
      Gson gson = createGson();

      String requestBody = content != null ? gson.toJson(content) : "";

      var requestBuilder =
          HttpRequest.newBuilder()
              .uri(new URI(url))
              .header("Content-Type", "application/json")
              .timeout(HTTP_TIMEOUT)
              .method(methodType, HttpRequest.BodyPublishers.ofString(requestBody));

      if (org.springframework.util.StringUtils.hasText(token)) {
        requestBuilder.header("Authorization", "Bearer " + token);
      }

      HttpResponse<String> response =
          client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 200) {
        return response.body();
      } else {
        throw new BadRequestException(
            "Shiprocket API error (status " + response.statusCode() + "): " + response.body());
      }
    } catch (BadRequestException e) {
      throw e;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new BadRequestException("Shiprocket API request was interrupted: " + e.getMessage());
    } catch (Exception e) {
      throw new BadRequestException("Exception occurred: " + e.getMessage());
    }
  }

  /**
   * HTTP method that deserializes the response into a typed object. Uses httpResponseRaw internally
   * for the actual HTTP call.
   *
   * @param token Authorization token
   * @param url Full URL to call
   * @param methodType HTTP method (GET, POST, PUT, DELETE)
   * @param type Type to deserialize response into
   * @param content Request body object
   * @param successMessage Unused (kept for backward compatibility)
   * @return Deserialized response object
   */
  public <T> T httpResponse(
      String token,
      String url,
      String methodType,
      Type type,
      Object content,
      String successMessage) {
    String rawResponse = httpResponseRaw(token, url, methodType, content);
    return createGson().fromJson(rawResponse, type);
  }

  /**
   * Get authentication token from Shiprocket API. Uses cached token if available and not expired to
   * avoid excessive API calls.
   *
   * @return Authentication token
   */
  public synchronized String getToken() {
    // Check if cached token is still valid
    long currentTime = System.currentTimeMillis();
    if (cachedToken != null && currentTime < tokenExpiresAt) {
      return cachedToken;
    }

    // Token expired or not cached - fetch new one
    try {
      HttpClient client = createHttpClient();
      URI uri = URI.create(API_URL + "/auth/login");

      HashMap<String, Object> jsonBody = new HashMap<>();
      jsonBody.put("email", email);
      jsonBody.put("password", password);

      ObjectMapper mapper = new ObjectMapper();
      String data = mapper.writeValueAsString(jsonBody);

      HttpRequest request =
          HttpRequest.newBuilder()
              .POST(HttpRequest.BodyPublishers.ofString(data, StandardCharsets.UTF_8))
              .uri(uri)
              .header("Content-Type", "application/json")
              .timeout(HTTP_TIMEOUT)
              .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != 200) {
        throw new BadRequestException(
            "Error in getting the auth token from Shiprocket API. Status: "
                + response.statusCode());
      }

      TokenResponseModel tokenResponse =
          mapper.readValue(response.body(), TokenResponseModel.class);
      String newToken = tokenResponse.getToken();

      // Cache the token with expiration time
      cachedToken = newToken;
      tokenExpiresAt = currentTime + TOKEN_CACHE_DURATION_MS;

      return newToken;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      cachedToken = null;
      tokenExpiresAt = 0;
      throw new BadRequestException(
          "Authentication token request was interrupted: " + e.getMessage());
    } catch (Exception e) {
      // Clear cached token on error so we retry next time
      cachedToken = null;
      tokenExpiresAt = 0;
      // Check if it's a connection/timeout related error
      String errorMsg = e.getMessage();
      String className = e.getClass().getSimpleName();
      if (errorMsg != null
          && (errorMsg.contains("timeout")
              || errorMsg.contains("timed out")
              || errorMsg.contains("connect")
              || className.contains("Timeout")
              || className.contains("Connect"))) {
        throw new BadRequestException(
            "Authentication token request failed due to network timeout. Please check network connectivity or Shiprocket API status.");
      }
      throw new BadRequestException(
          "Exception occurred while getting auth token: " + e.getMessage());
    }
  }

  public AddPickupLocationResponseModel addPickupLocation(PickupLocation pickupLocation) {
    String token = getToken();
    HashMap<String, Object> jsonBody = new HashMap<>();

    String pickupLocationName = pickupLocation.getAddressNickName();
    jsonBody.put("pickup_location", pickupLocationName);
    jsonBody.put(
        "name",
        pickupLocation.getAddress().getNameOnAddress() != null
            ? pickupLocation.getAddress().getNameOnAddress()
            : "");
    jsonBody.put(
        "email",
        pickupLocation.getAddress().getEmailOnAddress() != null
            ? pickupLocation.getAddress().getEmailOnAddress()
            : "");
    jsonBody.put(
        "phone",
        pickupLocation.getAddress().getPhoneOnAddress() != null
            ? pickupLocation.getAddress().getPhoneOnAddress()
            : "");
    jsonBody.put("address", pickupLocation.getAddress().getStreetAddress());
    jsonBody.put("address_2", pickupLocation.getAddress().getStreetAddress2());
    jsonBody.put("city", pickupLocation.getAddress().getCity());
    jsonBody.put("state", pickupLocation.getAddress().getState());
    jsonBody.put("country", pickupLocation.getAddress().getCountry());
    jsonBody.put("pin_code", pickupLocation.getAddress().getPostalCode());

    return httpResponse(
        token,
        API_URL + "/settings/company/addpickup",
        "POST",
        new TypeToken<AddPickupLocationResponseModel>() {}.getType(),
        jsonBody,
        "Successfully added pickup location.");
  }

  /**
   * Get available shipping options/courier companies for a shipment
   *
   * @param pickupPostcode Pickup location postal code
   * @param deliveryPostcode Delivery location postal code
   * @param isCod Whether the order is Cash on Delivery (false = prepaid)
   * @param weightInKgs Weight of the order in kilograms
   * @return ShippingOptionsResponseModel with available courier companies
   */
  public ShippingOptionsResponseModel getAvailableShippingOptions(
      String pickupPostcode, String deliveryPostcode, boolean isCod, String weightInKgs) {
    String token = getToken();

    HashMap<String, Object> jsonBody = new HashMap<>();
    jsonBody.put("pickup_postcode", pickupPostcode);
    jsonBody.put("delivery_postcode", deliveryPostcode);
    jsonBody.put("cod", isCod ? 1 : 0);
    jsonBody.put("weight", weightInKgs);

    return httpResponse(
        token,
        API_URL + "/courier/serviceability/",
        "GET",
        new TypeToken<ShippingOptionsResponseModel>() {}.getType(),
        jsonBody,
        "Successfully got the available shipping options.");
  }

  /**
   * Creates a custom order in ShipRocket.
   *
   * <p>Based on ShipRocket API: POST /orders/create/adhoc Documentation:
   * https://www.postman.com/shiprocketdev/shiprocket-dev-s-public-workspace/request/mydll5u/create-custom-order
   *
   * <p>Note: ShipRocket can return HTTP 200 with error data in the response body. This method
   * validates that the response contains required fields (order_id, shipment_id).
   *
   * @param orderRequest ShipRocketOrderRequestModel containing order details
   * @return ShipRocketOrderResponseModel containing order details
   * @throws BadRequestException if the response doesn't contain valid order data
   */
  public ShipRocketOrderResponseModel createCustomOrder(Object orderRequest) {
    String token = getToken();
    ShipRocketOrderResponseModel response =
        httpResponse(
            token,
            API_URL + "/orders/create/adhoc",
            "POST",
            new TypeToken<ShipRocketOrderResponseModel>() {}.getType(),
            orderRequest,
            "Successfully created ShipRocket order.");

    // Validate response content - HTTP 200 doesn't guarantee success
    if (response == null) {
      throw new BadRequestException("ShipRocket create order returned null response");
    }

    // Check for error message in response
    if (response.getMessage() != null && !response.getMessage().isEmpty()) {
      // ShipRocket sometimes returns success with a message, check if it's an error
      String message = response.getMessage().toLowerCase();
      if (message.contains("error") || message.contains("invalid") || message.contains("failed")) {
        throw new BadRequestException("ShipRocket create order failed: " + response.getMessage());
      }
    }

    // Validate required fields exist
    String orderId = response.getOrderIdAsString();
    if (orderId == null || orderId.isEmpty()) {
      throw new BadRequestException(
          "ShipRocket create order response missing order_id. Response message: "
              + (response.getMessage() != null ? response.getMessage() : "none"));
    }

    if (response.getShipmentId() == null) {
      throw new BadRequestException(
          "ShipRocket create order response missing shipment_id for order: " + orderId);
    }

    return response;
  }

  /**
   * Assigns AWB and returns the raw JSON response for storing as metadata.
   *
   * <p>Note: ShipRocket can return HTTP 200 with error data in the response body. This method
   * validates that awb_assign_status is 1 (success) before returning.
   *
   * @param shipmentId The ShipRocket shipment ID
   * @param courierId The courier company ID to assign
   * @return Raw JSON string of the AWB assignment response
   * @throws BadRequestException if the AWB assignment fails or response is invalid
   */
  public String assignAwbAsJson(Long shipmentId, Long courierId) {
    if (shipmentId == null) {
      throw new BadRequestException("Shipment ID is required for AWB assignment");
    }
    if (courierId == null) {
      throw new BadRequestException("Courier ID is required for AWB assignment");
    }

    String token = getToken();

    HashMap<String, Object> jsonBody = new HashMap<>();
    jsonBody.put(SHIPMENT_ID_KEY, String.valueOf(shipmentId));
    jsonBody.put("courier_id", String.valueOf(courierId));

    // Use base HTTP method
    String responseBody = httpResponseRaw(token, API_URL + "/courier/assign/awb", "POST", jsonBody);

    // Validate response content - HTTP 200 doesn't guarantee success
    ShipRocketAwbResponseModel awbResponse =
        createGson().fromJson(responseBody, ShipRocketAwbResponseModel.class);

    if (awbResponse == null) {
      throw new BadRequestException(
          "ShipRocket AWB assignment returned null response for shipment: " + shipmentId);
    }

    if (!awbResponse.isSuccess()) {
      throw new BadRequestException(
          "ShipRocket AWB assignment failed for shipment: "
              + shipmentId
              + ". awb_assign_status: "
              + awbResponse.getAwbAssignStatus()
              + ". Response: "
              + responseBody);
    }

    String awbCode = awbResponse.getAwbCode();
    if (awbCode == null || awbCode.isEmpty()) {
      throw new BadRequestException(
          "ShipRocket AWB assignment returned empty AWB code for shipment: " + shipmentId);
    }

    return responseBody;
  }

  /**
   * Generates pickup for a ShipRocket shipment.
   *
   * <p>Based on ShipRocket API: POST /courier/generate/pickup
   *
   * <p>This API schedules a pickup for the shipment after AWB has been assigned. Must be called
   * after AWB assignment.
   *
   * @param shipmentId The ShipRocket shipment ID (from order creation response)
   * @return Raw JSON string of the pickup generation response
   * @throws BadRequestException if the pickup generation fails
   */
  public String generatePickupAsJson(Long shipmentId) {
    if (shipmentId == null) {
      throw new BadRequestException("Shipment ID is required for pickup generation");
    }

    String token = getToken();

    // ShipRocket expects shipment_id as an array
    HashMap<String, Object> jsonBody = new HashMap<>();
    jsonBody.put(SHIPMENT_ID_KEY, java.util.List.of(shipmentId));

    // Use base HTTP method
    String responseBody =
        httpResponseRaw(token, API_URL + "/courier/generate/pickup", "POST", jsonBody);

    // Deserialize into typed response model
    ShipRocketPickupResponseModel response =
        createGson().fromJson(responseBody, ShipRocketPickupResponseModel.class);

    if (response == null) {
      throw new BadRequestException(
          "ShipRocket pickup generation returned null response for shipment: " + shipmentId);
    }

    if (!response.isSuccess()) {
      throw new BadRequestException(
          "ShipRocket pickup generation failed for shipment: "
              + shipmentId
              + ". pickup_status: "
              + response.getPickupStatus()
              + ". Response: "
              + responseBody);
    }

    return responseBody;
  }

  /**
   * Generates manifest for a ShipRocket shipment.
   *
   * <p>Based on ShipRocket API: POST /manifests/generate
   *
   * <p>This API generates a manifest PDF for the shipment. Must be called after pickup has been
   * generated.
   *
   * @param shipmentId The ShipRocket shipment ID
   * @return Manifest URL from the response, or null if generation failed
   * @throws BadRequestException if the manifest generation fails
   */
  public String generateManifest(Long shipmentId) {
    if (shipmentId == null) {
      throw new BadRequestException("Shipment ID is required for manifest generation");
    }

    String token = getToken();

    // ShipRocket expects shipment_id as an array
    HashMap<String, Object> jsonBody = new HashMap<>();
    jsonBody.put("shipment_id", java.util.List.of(shipmentId));

    // Use base HTTP method
    String responseBody = httpResponseRaw(token, API_URL + "/manifests/generate", "POST", jsonBody);

    // Deserialize into typed response model
    ShipRocketManifestResponseModel response =
        createGson().fromJson(responseBody, ShipRocketManifestResponseModel.class);

    if (response == null) {
      throw new BadRequestException(
          "ShipRocket manifest generation returned null response for shipment: " + shipmentId);
    }

    if (!response.isSuccess()) {
      throw new BadRequestException(
          "ShipRocket manifest generation failed for shipment: "
              + shipmentId
              + ". status: "
              + response.getStatus()
              + ". Response: "
              + responseBody);
    }

    return response.getManifestUrl();
  }

  /**
   * Generates shipping label for a ShipRocket shipment.
   *
   * <p>Based on ShipRocket API: POST /courier/generate/label
   *
   * <p>This API generates a shipping label PDF for the shipment. Must be called after manifest has
   * been generated.
   *
   * @param shipmentId The ShipRocket shipment ID
   * @return Label URL from the response, or null if generation failed
   * @throws BadRequestException if the label generation fails
   */
  public String generateLabel(Long shipmentId) {
    if (shipmentId == null) {
      throw new BadRequestException("Shipment ID is required for label generation");
    }

    String token = getToken();

    // ShipRocket expects shipment_id as an array of strings
    HashMap<String, Object> jsonBody = new HashMap<>();
    jsonBody.put(SHIPMENT_ID_KEY, java.util.List.of(String.valueOf(shipmentId)));

    // Use base HTTP method
    String responseBody =
        httpResponseRaw(token, API_URL + "/courier/generate/label", "POST", jsonBody);

    // Deserialize into typed response model
    ShipRocketLabelResponseModel response =
        createGson().fromJson(responseBody, ShipRocketLabelResponseModel.class);

    if (response == null) {
      throw new BadRequestException(
          "ShipRocket label generation returned null response for shipment: " + shipmentId);
    }

    if (!response.isSuccess()) {
      throw new BadRequestException(
          "ShipRocket label generation failed for shipment: "
              + shipmentId
              + ". label_created: "
              + response.getLabelCreated()
              + ". Response: "
              + responseBody);
    }

    return response.getLabelUrl();
  }

  /**
   * Generates invoice for a ShipRocket order.
   *
   * <p>Based on ShipRocket API: POST /orders/print/invoice
   *
   * <p>This API generates an invoice PDF for the order. Must be called after label has been
   * generated.
   *
   * @param shipmentId The ShipRocket shipment ID
   * @return Invoice URL from the response, or null if generation failed
   * @throws BadRequestException if the invoice generation fails
   */
  public String generateInvoice(Long shipmentId) {
    if (shipmentId == null) {
      throw new BadRequestException("Shipment ID is required for invoice generation");
    }

    String token = getToken();

    // ShipRocket expects ids as an array of strings
    HashMap<String, Object> jsonBody = new HashMap<>();
    jsonBody.put("ids", java.util.List.of(String.valueOf(shipmentId)));

    // Use base HTTP method
    String responseBody =
        httpResponseRaw(token, API_URL + "/orders/print/invoice", "POST", jsonBody);

    // Deserialize into typed response model
    ShipRocketInvoiceResponseModel response =
        createGson().fromJson(responseBody, ShipRocketInvoiceResponseModel.class);

    if (response == null) {
      throw new BadRequestException(
          "ShipRocket invoice generation returned null response for shipment: " + shipmentId);
    }

    if (!response.isSuccess()) {
      throw new BadRequestException(
          "ShipRocket invoice generation failed for shipment: "
              + shipmentId
              + ". is_invoice_created: "
              + response.getIsInvoiceCreated()
              + ". Response: "
              + responseBody);
    }

    return response.getInvoiceUrl();
  }

  /**
   * Gets tracking information for a shipment by AWB code.
   *
   * <p>Based on ShipRocket API: GET /courier/track/awb/{awb_code}
   *
   * <p>This API returns tracking information including status, activities, and delivery details.
   * Must be called after AWB has been assigned.
   *
   * @param awbCode The AWB code for the shipment
   * @return Raw JSON string of the tracking response
   * @throws BadRequestException if the tracking request fails
   */
  public String getTrackingAsJson(String awbCode) {
    if (awbCode == null || awbCode.trim().isEmpty()) {
      throw new BadRequestException("AWB code is required for tracking");
    }

    String token = getToken();

    // Use base HTTP method - GET request with AWB in URL
    String responseBody =
        httpResponseRaw(token, API_URL + "/courier/track/awb/" + awbCode.trim(), "GET", null);

    // Deserialize into typed response model
    ShipRocketTrackingResponseModel response =
        createGson().fromJson(responseBody, ShipRocketTrackingResponseModel.class);

    if (response == null) {
      throw new BadRequestException(
          "ShipRocket tracking returned null response for AWB: " + awbCode);
    }

    if (response.getTrackingData() == null) {
      throw new BadRequestException(
          "ShipRocket tracking returned no tracking_data for AWB: "
              + awbCode
              + ". Response: "
              + responseBody);
    }

    return responseBody;
  }

  /**
   * This method returns the raw JSON response for storing as metadata.
   *
   * <p>Note: ShipRocket can return HTTP 200 with error data in the response body. This method
   * validates that the response contains valid order data.
   *
   * @param shipRocketOrderId The ShipRocket order ID to fetch details for
   * @return Raw JSON string of the order details response
   * @throws BadRequestException if the response doesn't contain valid order data
   */
  public String getOrderDetailsAsJson(String shipRocketOrderId) {
    if (shipRocketOrderId == null || shipRocketOrderId.trim().isEmpty()) {
      throw new BadRequestException("ShipRocket order ID is required to fetch order details");
    }

    String token = getToken();

    // Use base HTTP method
    String responseBody =
        httpResponseRaw(token, API_URL + "/orders/show/" + shipRocketOrderId.trim(), "GET", null);

    // Validate response content - HTTP 200 doesn't guarantee success
    com.example.SpringApi.Models.ShippingResponseModel.ShipRocketOrderDetailsResponseModel
        orderDetails =
            createGson()
                .fromJson(
                    responseBody,
                    com.example.SpringApi.Models.ShippingResponseModel
                        .ShipRocketOrderDetailsResponseModel.class);

    if (orderDetails == null) {
      throw new BadRequestException(
          "ShipRocket get order details returned null response for order: " + shipRocketOrderId);
    }

    if (orderDetails.getData() == null) {
      throw new BadRequestException(
          "ShipRocket get order details returned empty data for order: "
              + shipRocketOrderId
              + ". Response: "
              + responseBody);
    }

    if (orderDetails.getData().getId() == null) {
      throw new BadRequestException(
          "ShipRocket get order details returned response without order ID for: "
              + shipRocketOrderId);
    }

    return responseBody;
  }

  /**
   * Creates a return order in ShipRocket.
   *
   * <p>Based on ShipRocket API: POST /orders/create/return
   *
   * <p>This API creates a return order for products being returned by the customer.
   *
   * @param returnOrderRequest The return order request containing pickup (customer) and shipping
   *     (warehouse) details
   * @return ShipRocketReturnOrderResponseModel with the return order details
   * @throws BadRequestException if the return order creation fails
   */
  public ShipRocketReturnOrderResponseModel createReturnOrder(Object returnOrderRequest) {
    String token = getToken();

    String responseBody =
        httpResponseRaw(token, API_URL + "/orders/create/return", "POST", returnOrderRequest);

    // Deserialize into typed response model
    ShipRocketReturnOrderResponseModel response =
        createGson().fromJson(responseBody, ShipRocketReturnOrderResponseModel.class);

    if (response == null) {
      throw new BadRequestException("ShipRocket create return order returned null response");
    }

    // Check for error message
    if (response.getMessage() != null && !response.getMessage().isEmpty()) {
      String message = response.getMessage().toLowerCase();
      if (message.contains("error") || message.contains("invalid") || message.contains("failed")) {
        throw new BadRequestException(
            "ShipRocket create return order failed: " + response.getMessage());
      }
    }

    // Validate required fields
    if (!response.isSuccess()) {
      throw new BadRequestException(
          "ShipRocket create return order failed. Response: " + responseBody);
    }

    return response;
  }

  /**
   * Creates a return order and returns raw JSON response for storing as metadata.
   *
   * @param returnOrderRequest The return order request
   * @return Raw JSON string of the return order response
   * @throws BadRequestException if the return order creation fails
   */
  public String createReturnOrderAsJson(Object returnOrderRequest) {
    String token = getToken();

    String responseBody =
        httpResponseRaw(token, API_URL + "/orders/create/return", "POST", returnOrderRequest);

    // Validate response
    ShipRocketReturnOrderResponseModel response =
        createGson().fromJson(responseBody, ShipRocketReturnOrderResponseModel.class);

    if (response == null || !response.isSuccess()) {
      throw new BadRequestException(
          "ShipRocket create return order failed. Response: " + responseBody);
    }

    return responseBody;
  }

  /**
   * Assigns AWB for a return shipment.
   *
   * <p>Based on ShipRocket API: POST /courier/assign/awb For returns, the is_return parameter
   * should be 1.
   *
   * @param shipmentId The ShipRocket return shipment ID
   * @return Raw JSON string of the AWB assignment response
   * @throws BadRequestException if the AWB assignment fails
   */
  public String assignReturnAwbAsJson(Long shipmentId) {
    if (shipmentId == null) {
      throw new BadRequestException("Shipment ID is required for return AWB assignment");
    }

    String token = getToken();

    HashMap<String, Object> jsonBody = new HashMap<>();
    jsonBody.put(SHIPMENT_ID_KEY, String.valueOf(shipmentId));
    jsonBody.put("is_return", 1); // Indicates this is a return shipment

    // Use base HTTP method
    String responseBody = httpResponseRaw(token, API_URL + "/courier/assign/awb", "POST", jsonBody);

    // Validate response content
    ShipRocketAwbResponseModel awbResponse =
        createGson().fromJson(responseBody, ShipRocketAwbResponseModel.class);

    if (awbResponse == null) {
      throw new BadRequestException(
          "ShipRocket return AWB assignment returned null response for shipment: " + shipmentId);
    }

    if (!awbResponse.isSuccess()) {
      throw new BadRequestException(
          "ShipRocket return AWB assignment failed for shipment: "
              + shipmentId
              + ". awb_assign_status: "
              + awbResponse.getAwbAssignStatus()
              + ". Response: "
              + responseBody);
    }

    String awbCode = awbResponse.getAwbCode();
    if (awbCode == null || awbCode.isEmpty()) {
      throw new BadRequestException(
          "ShipRocket return AWB assignment returned empty AWB code for shipment: " + shipmentId);
    }

    return responseBody;
  }

  /**
   * Cancels orders in ShipRocket.
   *
   * <p>Based on ShipRocket API: POST /orders/cancel
   *
   * <p>This API cancels orders in ShipRocket. Note: This endpoint returns no response body.
   *
   * @param orderIds List of ShipRocket order IDs to cancel
   * @throws BadRequestException if the cancellation fails
   */
  public void cancelOrders(java.util.List<Long> orderIds) {
    if (orderIds == null || orderIds.isEmpty()) {
      throw new BadRequestException("Order IDs are required for cancellation");
    }

    String token = getToken();

    // ShipRocket expects ids as an array
    HashMap<String, Object> jsonBody = new HashMap<>();
    jsonBody.put("ids", orderIds);

    // Use base HTTP method - this endpoint returns no response body on success
    httpResponseRaw(token, API_URL + "/orders/cancel", "POST", jsonBody);

    // If we get here without exception, the cancellation was successful
  }

  /**
   * Gets the wallet balance from ShipRocket.
   *
   * <p>Based on ShipRocket API: GET /account/details/wallet-balance
   *
   * @return The wallet balance as a Double
   * @throws BadRequestException if the request fails
   */
  public Double getWalletBalance() {
    String token = getToken();

    String responseBody =
        httpResponseRaw(token, API_URL + "/account/details/wallet-balance", "GET", null);

    try {
      com.google.gson.JsonObject jsonResponse =
          com.google.gson.JsonParser.parseString(responseBody).getAsJsonObject();

      if (jsonResponse.has("data")) {
        com.google.gson.JsonObject data = jsonResponse.getAsJsonObject("data");
        if (data.has("balance_amount")) {
          String balanceStr = data.get("balance_amount").getAsString();
          return Double.parseDouble(balanceStr);
        }
      }

      throw new BadRequestException(
          "Invalid wallet balance response from ShipRocket: missing balance_amount");
    } catch (NumberFormatException e) {
      throw new BadRequestException(
          "Invalid wallet balance format from ShipRocket: " + e.getMessage());
    } catch (Exception e) {
      if (e instanceof BadRequestException badRequestException) {
        throw badRequestException;
      }
      throw new BadRequestException("Failed to parse wallet balance response: " + e.getMessage());
    }
  }
}
