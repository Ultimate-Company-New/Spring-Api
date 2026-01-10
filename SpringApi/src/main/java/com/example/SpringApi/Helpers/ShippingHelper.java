package com.example.SpringApi.Helpers;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import com.nimbusds.jose.shaded.gson.reflect.TypeToken;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.PickupLocation;
import com.example.SpringApi.Models.ShippingResponseModel.TokenResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.AddPickupLocationResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.GetAllPickupLocationsResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.ShippingOptionsResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.ShipRocketOrderResponseModel;
import com.example.SpringApi.Adapters.DateAdapter;
import com.example.SpringApi.Adapters.LocalDateTimeAdapter;

public class ShippingHelper {
    private final String _apiUrl = "https://apiv2.shiprocket.in/v1/external";
    private final String _email;
    private final String _password;
    
    /**
     * Timeout for HTTP requests to Shiprocket API (5 seconds).
     * Prevents hanging if the API is slow or unresponsive.
     */
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(5);
    
    /**
     * Cached token and its expiration time.
     * Shiprocket tokens typically expire after some time, so we cache and reuse.
     */
    private String cachedToken = null;
    private long tokenExpiresAt = 0;
    private static final long TOKEN_CACHE_DURATION_MS = 55 * 60 * 1000; // 55 minutes (tokens usually valid for 1 hour)
    
    /**
     * Create an HttpClient with timeout configuration.
     */
    private HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(HTTP_TIMEOUT)
                .build();
    }

    public ShippingHelper(String email, String password) {
        this._email = email;
        this._password = password;
    }

    public <T> T httpResponse(
            String token,
            String url,
            String methodType,
            Type type,
            Object content,
            String successMessage) {
        try{
            HttpClient client = createHttpClient();
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .registerTypeAdapter(Date.class, new DateAdapter())
                    .create();

            String requestBody = content != null ? gson.toJson(content) : "";

            var requestBuilder = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Content-Type", "application/json")
                    .timeout(HTTP_TIMEOUT)
                    .method(methodType, HttpRequest.BodyPublishers.ofString(requestBody));

            if(org.springframework.util.StringUtils.hasText(token)){
                requestBuilder.header("Authorization", "Bearer " + token);
            }

            HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                return gson.fromJson(response.body(), type);
            }
            else{
                throw new BadRequestException("Shiprocket API error (status " + response.statusCode() + "): " + response.body());
            }
        }
        catch (BadRequestException e){
            throw e;
        }
        catch (Exception e){
            throw new BadRequestException("Exception occurred: " + e.getMessage());
        }
    }

    /**
     * Get authentication token from Shiprocket API.
     * Uses cached token if available and not expired to avoid excessive API calls.
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
            URI uri = URI.create(_apiUrl + "/auth/login");

            HashMap<String, Object> jsonBody = new HashMap<>();
            jsonBody.put("email", _email);
            jsonBody.put("password", _password);

            ObjectMapper mapper = new ObjectMapper();
            String data = mapper.writeValueAsString(jsonBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(data, StandardCharsets.UTF_8))
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .timeout(HTTP_TIMEOUT)
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new BadRequestException("Error in getting the auth token from Shiprocket API. Status: " + response.statusCode());
            }

            TokenResponseModel tokenResponse = mapper.readValue(response.body(), TokenResponseModel.class);
            String newToken = tokenResponse.getToken();
            
            // Cache the token with expiration time
            cachedToken = newToken;
            tokenExpiresAt = currentTime + TOKEN_CACHE_DURATION_MS;
            
            return newToken;
        }
        catch (Exception e) {
            // Clear cached token on error so we retry next time
            cachedToken = null;
            tokenExpiresAt = 0;
            // Check if it's a connection/timeout related error
            String errorMsg = e.getMessage();
            String className = e.getClass().getSimpleName();
            if (errorMsg != null && (errorMsg.contains("timeout") || errorMsg.contains("timed out") || errorMsg.contains("connect") || 
                className.contains("Timeout") || className.contains("Connect"))) {
                throw new BadRequestException("Authentication token request failed due to network timeout. Please check network connectivity or Shiprocket API status.");
            }
            throw new BadRequestException("Exception occurred while getting auth token: " + e.getMessage());
        }
    }

    public AddPickupLocationResponseModel addPickupLocation(
            PickupLocation pickupLocation
    ) {
        String token = getToken();
        HashMap<String, Object> jsonBody = new HashMap<>();

        String pickupLocationName = pickupLocation.getAddressNickName();
        jsonBody.put("pickup_location", pickupLocationName);
        jsonBody.put("name", pickupLocation.getAddress().getNameOnAddress() != null ? pickupLocation.getAddress().getNameOnAddress() : "");
        jsonBody.put("email", pickupLocation.getAddress().getEmailOnAddress() != null ? pickupLocation.getAddress().getEmailOnAddress() : "");
        jsonBody.put("phone", pickupLocation.getAddress().getPhoneOnAddress() != null ? pickupLocation.getAddress().getPhoneOnAddress() : "");
        jsonBody.put("address", pickupLocation.getAddress().getStreetAddress());
        jsonBody.put("address_2", pickupLocation.getAddress().getStreetAddress2());
        jsonBody.put("city", pickupLocation.getAddress().getCity());
        jsonBody.put("state", pickupLocation.getAddress().getState());
        jsonBody.put("country", pickupLocation.getAddress().getCountry());
        jsonBody.put("pin_code", pickupLocation.getAddress().getPostalCode());

        return httpResponse(
                token,
                _apiUrl + "/settings/company/addpickup",
                "POST",
                new TypeToken<AddPickupLocationResponseModel>(){}.getType(),
                jsonBody,
                "Successfully added pickup location.");
    }

    public GetAllPickupLocationsResponseModel getAllPickupLocations(
    ) {
        String token = getToken();
        return httpResponse(
                token,
                _apiUrl + "/settings/company/pickup",
                "GET",
                new TypeToken<GetAllPickupLocationsResponseModel>(){}.getType(),
                null,
                "Successfully got all pickup locations.");
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
            String pickupPostcode,
            String deliveryPostcode,
            boolean isCod,
            String weightInKgs
    ) {
        String token = getToken();
        
        HashMap<String, Object> jsonBody = new HashMap<>();
        jsonBody.put("pickup_postcode", pickupPostcode);
        jsonBody.put("delivery_postcode", deliveryPostcode);
        jsonBody.put("cod", isCod ? 1 : 0);
        jsonBody.put("weight", weightInKgs);

        return httpResponse(
                token,
                _apiUrl + "/courier/serviceability/",
                "GET",
                new TypeToken<ShippingOptionsResponseModel>(){}.getType(),
                jsonBody,
                "Successfully got the available shipping options.");
    }

    /**
     * Find the maximum weight that couriers can handle for a given route.
     * Starts at 500kg and reduces by 100kg until couriers are found or reaches 100kg.
     * 
     * @param pickupPostcode Pickup location postal code
     * @param deliveryPostcode Delivery location postal code
     * @param isCod Whether the order is Cash on Delivery
     * @return Maximum weight in kg that can be shipped on this route
     * @throws RuntimeException if no couriers available even at 100kg
     */
    public double findMaxWeightForRoute(
            String pickupPostcode,
            String deliveryPostcode,
            boolean isCod
    ) {
        // Start at 500kg and reduce by 100kg until we find couriers
        double[] weightsToTry = {500, 400, 300, 200, 100};
        
        for (double weight : weightsToTry) {
            try {
                ShippingOptionsResponseModel response = getAvailableShippingOptions(
                    pickupPostcode, deliveryPostcode, isCod, String.valueOf(weight));
                
                if (response != null && response.getData() != null && 
                    response.getData().available_courier_companies != null &&
                    !response.getData().available_courier_companies.isEmpty()) {
                    return weight;
                }
            } catch (Exception e) {
                // Error checking weight - continue to next weight
            }
        }
        
        // No couriers found even at 100kg
        return 0; // Indicates no couriers available
    }
    
    /**
     * Creates a custom order in ShipRocket.
     * 
     * Based on ShipRocket API: POST /orders/create/adhoc
     * Documentation: https://www.postman.com/shiprocketdev/shiprocket-dev-s-public-workspace/request/mydll5u/create-custom-order
     * 
     * @param orderRequest ShipRocketOrderRequestModel containing order details
     * 
     * @return ShipRocketOrderResponseModel containing order details
     */
    public ShipRocketOrderResponseModel createCustomOrder(Object orderRequest) {
        String token = getToken();
        return httpResponse(
                token,
                _apiUrl + "/orders/create/adhoc",
                "POST",
                new TypeToken<ShipRocketOrderResponseModel>(){}.getType(),
                orderRequest,
                "Successfully created ShipRocket order.");
    }
    
    /**
     * Gets order details from ShipRocket by order ID.
     * 
     * Based on ShipRocket API: GET /orders/show/{order_id}
     * 
     * This API returns comprehensive order information including:
     * - Order details (customer info, addresses, payment info)
     * - Products in the order
     * - Shipment details (AWB, courier, status)
     * - AWB data with charges breakdown
     * - Insurance and return pickup data
     * 
     * @param shipRocketOrderId The ShipRocket order ID to fetch details for
     * @return ShipRocketOrderDetailsResponseModel containing full order details
     */
    public com.example.SpringApi.Models.ShippingResponseModel.ShipRocketOrderDetailsResponseModel getOrderDetails(String shipRocketOrderId) {
        if (shipRocketOrderId == null || shipRocketOrderId.trim().isEmpty()) {
            throw new BadRequestException("ShipRocket order ID is required to fetch order details");
        }
        
        String token = getToken();
        return httpResponse(
                token,
                _apiUrl + "/orders/show/" + shipRocketOrderId.trim(),
                "GET",
                new TypeToken<com.example.SpringApi.Models.ShippingResponseModel.ShipRocketOrderDetailsResponseModel>(){}.getType(),
                null,
                "Successfully fetched ShipRocket order details.");
    }
    
    /**
     * Gets order details as raw JSON string from ShipRocket by order ID.
     * 
     * This method returns the raw JSON response for storing as metadata.
     * 
     * @param shipRocketOrderId The ShipRocket order ID to fetch details for
     * @return Raw JSON string of the order details response
     */
    public String getOrderDetailsAsJson(String shipRocketOrderId) {
        if (shipRocketOrderId == null || shipRocketOrderId.trim().isEmpty()) {
            throw new BadRequestException("ShipRocket order ID is required to fetch order details");
        }
        
        try {
            String token = getToken();
            HttpClient client = createHttpClient();
            
            var requestBuilder = HttpRequest.newBuilder()
                    .uri(new URI(_apiUrl + "/orders/show/" + shipRocketOrderId.trim()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .timeout(HTTP_TIMEOUT)
                    .GET();
            
            HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                throw new BadRequestException("Shiprocket API error (status " + response.statusCode() + "): " + response.body());
            }
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Exception occurred while fetching order details: " + e.getMessage());
        }
    }
}