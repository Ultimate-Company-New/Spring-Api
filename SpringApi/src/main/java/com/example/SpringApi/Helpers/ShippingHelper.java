package com.example.SpringApi.Helpers;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
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
import com.example.SpringApi.Adapters.DateAdapter;
import com.example.SpringApi.Adapters.LocalDateTimeAdapter;

public class ShippingHelper {
    private final String _apiUrl = "https://apiv2.shiprocket.in/v1/external";
    private final String _email;
    private final String _password;

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
            HttpClient client = HttpClient.newHttpClient();
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .registerTypeAdapter(Date.class, new DateAdapter())
                    .create();

            String requestBody = content != null ? gson.toJson(content) : "";

            var request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Content-Type", "application/json")
                    .method(methodType, HttpRequest.BodyPublishers.ofString(requestBody));

            if(org.springframework.util.StringUtils.hasText(token)){
                request.header("Authorization", "Bearer " + token);
            }

            HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
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

    public String getToken() {
        try {
            HttpClient client = HttpClient.newHttpClient();
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
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new BadRequestException("Error in getting the auth token from Shiprocket API. Status: " + response.statusCode());
            }

            TokenResponseModel tokenResponse = mapper.readValue(response.body(), TokenResponseModel.class);
            return tokenResponse.getToken();
        }
        catch (Exception e) {
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
                    
                    System.out.println("Route " + pickupPostcode + " -> " + deliveryPostcode + 
                        ": Found couriers at " + weight + " kg");
                    return weight;
                }
            } catch (Exception e) {
                System.err.println("Error checking weight " + weight + " for route: " + e.getMessage());
            }
        }
        
        // No couriers found even at 100kg
        System.err.println("No couriers available for route " + pickupPostcode + " -> " + deliveryPostcode + 
            " even at 100kg");
        return 0; // Indicates no couriers available
    }
}