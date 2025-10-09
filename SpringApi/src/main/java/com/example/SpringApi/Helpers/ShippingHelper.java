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
import com.example.SpringApi.Adapters.DateAdapter;
import com.example.SpringApi.Adapters.LocalDateTimeAdapter;

public class ShippingHelper {
    private final String _apiUrl = "https://apiv2.shiprocket.in/v1/external";
    private final String _email;
    private final String _password;
    private final String _token;

    public ShippingHelper(String email, String password) {
        this._email = email;
        this._password = password;
        this._token = getToken();
    }

    public <T> T httpResponse(
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

            if(org.springframework.util.StringUtils.hasText(this._token)){
                request.header("Authorization", "Bearer " + this._token);
            }

            HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200) {
                return gson.fromJson(response.body(), type);
            }
            else{
                throw new BadRequestException(response.body());
            }
        }
        catch (Exception e){
            throw new BadRequestException("Exception occurred: " + e.getMessage());
        }
    }

    private String getToken() {
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
                _apiUrl + "/settings/company/addpickup",
                "POST",
                new TypeToken<AddPickupLocationResponseModel>(){}.getType(),
                jsonBody,
                "Successfully added pickup location.");
    }

    public GetAllPickupLocationsResponseModel getAllPickupLocations(
    ) {
        return httpResponse(
                _apiUrl + "/settings/company/pickup",
                "GET",
                new TypeToken<GetAllPickupLocationsResponseModel>(){}.getType(),
                null,
                "Successfully got all pickup locations.");
    }
}