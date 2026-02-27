package com.example.SpringApi.ModelTests.Shipping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import com.nimbusds.jose.shaded.gson.Gson;
import org.junit.jupiter.api.Test;

class ShippingResponseModelsTest {

  private final Gson gson = new Gson();

  // Total Tests: 11
  @Test
  void shipRocketOrderResponseModel_HelperMethods_ParseIdsSafely() {
    ShipRocketOrderResponseModel model = new ShipRocketOrderResponseModel();
    model.setOrderId(12345L);
    model.setCourierCompanyId("  99 ");

    assertEquals("12345", model.getOrderIdAsString());
    assertEquals(99L, model.getCourierId());

    model.setCourierCompanyId("abc");
    assertNull(model.getCourierId());
  }

  @Test
  void shipRocketReturnOrderResponseModel_HelperMethods_EvaluateSuccessAndId() {
    ShipRocketReturnOrderResponseModel model = new ShipRocketReturnOrderResponseModel();
    model.setOrderId(777L);

    assertTrue(model.isSuccess());
    assertEquals("777", model.getOrderIdAsString());

    model.setOrderId(0L);
    assertFalse(model.isSuccess());
  }

  @Test
  void shipRocketAwbResponseModel_HelperMethods_ExtractNestedDataSafely() {
    ShipRocketAwbResponseModel model = new ShipRocketAwbResponseModel();
    model.setAwbAssignStatus(1);

    ShipRocketAwbResponseModel.AwbData data = new ShipRocketAwbResponseModel.AwbData();
    data.setAwbCode("AWB-123");
    data.setShipmentId(456L);
    ShipRocketAwbResponseModel.AwbResponse response = new ShipRocketAwbResponseModel.AwbResponse();
    response.setData(data);
    model.setResponse(response);

    assertTrue(model.isSuccess());
    assertEquals("AWB-123", model.getAwbCode());
    assertEquals(456L, model.getShipmentId());
  }

  @Test
  void shipRocketAwbResponseModel_HelperMethods_ReturnNullWhenResponseMissing() {
    ShipRocketAwbResponseModel model = new ShipRocketAwbResponseModel();

    assertNull(model.getAwbCode());
    assertNull(model.getShipmentId());
  }

  @Test
  void shipRocketGenericSuccessModels_EvaluateSuccessFlagsCorrectly() {
    ShipRocketPickupResponseModel pickup = new ShipRocketPickupResponseModel();
    pickup.setPickupStatus(1);
    assertTrue(pickup.isSuccess());

    ShipRocketManifestResponseModel manifest = new ShipRocketManifestResponseModel();
    manifest.setStatus(1);
    assertTrue(manifest.isSuccess());

    ShipRocketLabelResponseModel label = new ShipRocketLabelResponseModel();
    label.setLabelCreated(1);
    assertTrue(label.isSuccess());

    ShipRocketInvoiceResponseModel invoice = new ShipRocketInvoiceResponseModel();
    invoice.setIsInvoiceCreated(Boolean.TRUE);
    assertTrue(invoice.isSuccess());
  }

  @Test
  void shipRocketTrackingResponseModel_IsSuccessDependsOnTrackStatusOne() {
    ShipRocketTrackingResponseModel model = new ShipRocketTrackingResponseModel();
    ShipRocketTrackingResponseModel.TrackingData trackingData =
        new ShipRocketTrackingResponseModel.TrackingData();
    trackingData.setTrackStatus(1);
    model.setTrackingData(trackingData);

    assertTrue(model.isSuccess());

    trackingData.setTrackStatus(0);
    assertFalse(model.isSuccess());
  }

  @Test
  void shipRocketTrackingResponseModel_DeserializationMapsHyphenatedFields() {
    String json =
        """
                {
                  "tracking_data":{
                    "track_status":1,
                    "shipment_track_activities":[
                      {
                        "date":"2025-01-01",
                        "status":"IN_TRANSIT",
                        "activity":"Moved",
                        "location":"Mumbai",
                        "sr-status":"17",
                        "sr-status-label":"In Transit"
                      }
                    ]
                  }
                }
                """;

    ShipRocketTrackingResponseModel parsed =
        gson.fromJson(json, ShipRocketTrackingResponseModel.class);

    assertTrue(parsed.isSuccess());
    assertEquals(
        "17", parsed.getTrackingData().getShipmentTrackActivities().getFirst().getSrStatus());
    assertEquals(
        "In Transit",
        parsed.getTrackingData().getShipmentTrackActivities().getFirst().getSrStatusLabel());
  }

  @Test
  void addPickupLocationResponseModel_BackwardCompatibleAddressAccessors_WorkCorrectly() {
    AddPickupLocationResponseModel model = new AddPickupLocationResponseModel();
    AddPickupLocationResponseModel.Address address = new AddPickupLocationResponseModel.Address();
    address.setAddressLine1("Warehouse Street");

    model.setAddress(address);

    assertNotNull(model.getPickupAddress());
    assertEquals("Warehouse Street", model.getAddress().getAddressLine1());
  }

  @Test
  void addPickupLocationResponseModel_DeserializationMapsUnderscoreFields() {
    String json =
        """
                {
                  "pickup_id": 101,
                  "company_name": "Test Company",
                  "full_name": "Main Warehouse",
                  "address": {
                    "pickup_code": "PK-101",
                    "address": "Street One",
                    "address_2": "Building B",
                    "pin_code": "400001"
                  }
                }
                """;

    AddPickupLocationResponseModel parsed =
        gson.fromJson(json, AddPickupLocationResponseModel.class);

    assertEquals(101L, parsed.getPickupId());
    assertEquals("Test Company", parsed.getCompanyName());
    assertEquals("PK-101", parsed.getAddress().getPickupCode());
    assertEquals("Street One", parsed.getAddress().getAddressLine1());
    assertEquals("Building B", parsed.getAddress().getAddress2());
    assertEquals("400001", parsed.getAddress().getPinCode());
  }

  @Test
  void shippingOptionsResponseModel_DeserializationMapsNestedUnderscoreFields() {
    String json =
        """
                {
                  "company_auto_shipment_insurance_setting": true,
                  "currency": "INR",
                  "data": {
                    "available_courier_companies": [
                      {
                        "courier_company_id": 99,
                        "courier_name": "FastShip",
                        "rate": 54.5
                      }
                    ],
                    "recommended_courier_company_id": 99
                  }
                }
                """;

    ShippingOptionsResponseModel parsed = gson.fromJson(json, ShippingOptionsResponseModel.class);

    assertTrue(parsed.isCompanyAutoShipmentInsuranceSetting());
    assertEquals("INR", parsed.getCurrency());
    assertEquals(1, parsed.getData().getAvailableCourierCompanies().size());
    assertEquals(
        99, parsed.getData().getAvailableCourierCompanies().getFirst().getCourierCompanyId());
    assertEquals(
        "FastShip", parsed.getData().getAvailableCourierCompanies().getFirst().getCourierName());
    assertEquals(99, parsed.getData().getRecommendedCourierCompanyId());
  }

  @Test
  void shipRocketOrderResponseModel_DeserializationMapsUnderscoreFields() {
    String json =
        """
                {
                  "order_id": 5001,
                  "shipment_id": 7001,
                  "tracking_id": "TRK-111",
                  "courier_company_id": "45",
                  "label_url": "https://label.url"
                }
                """;

    ShipRocketOrderResponseModel parsed = gson.fromJson(json, ShipRocketOrderResponseModel.class);

    assertEquals(5001L, parsed.getOrderId());
    assertEquals(7001L, parsed.getShipmentId());
    assertEquals("TRK-111", parsed.getTrackingId());
    assertEquals(45L, parsed.getCourierId());
    assertEquals("https://label.url", parsed.getLabelUrl());
  }
}

