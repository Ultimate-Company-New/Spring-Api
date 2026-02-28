package springapi.modeltests.shipping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import springapi.ErrorMessages;
import springapi.exceptions.BadRequestException;
import springapi.models.databasemodels.Shipment;
import springapi.models.requestmodels.PurchaseOrderRequestModel;
import springapi.models.shippingresponsemodel.ShipRocketOrderResponseModel;

class ShipmentDatabaseModelTest {

  // Total Tests: 11
  @Test
  void shipmentShipRocketStatus_StaticHelpers_HandleNormalizationAndInvalidValues() {
    assertTrue(Shipment.ShipRocketStatus.isValid("ready_to_ship"));
    assertTrue(Shipment.ShipRocketStatus.isValid("Ready To Ship"));
    assertEquals(
        Shipment.ShipRocketStatus.READY_TO_SHIP,
        Shipment.ShipRocketStatus.fromString(" ready to ship "));
    assertNull(Shipment.ShipRocketStatus.fromString("unknown"));
  }

  @Test
  void shipment_SetShipRocketStatus_NormalizesValue() {
    Shipment shipment = new Shipment();

    shipment.setShipRocketStatus("out for delivery");

    assertEquals("OUT_FOR_DELIVERY", shipment.getShipRocketStatus());
  }

  @Test
  void shipment_SetShipRocketStatus_InvalidValueThrowsBadRequest() {
    Shipment shipment = new Shipment();

    BadRequestException exception =
        assertThrows(
            BadRequestException.class, () -> shipment.setShipRocketStatus("NOT_A_REAL_STATUS"));
    assertTrue(exception.getMessage().contains("Invalid ShipRocket status"));
  }

  @Test
  void shipment_SetShipRocketStatus_BlankValueClearsStatus() {
    Shipment shipment = new Shipment();
    shipment.setShipRocketStatus("DELIVERED");

    shipment.setShipRocketStatus("  ");

    assertNull(shipment.getShipRocketStatus());
  }

  @Test
  void shipment_CreateConstructor_ValidInputSetsComputedTotalsAndAuditFields() {
    PurchaseOrderRequestModel.ShipmentData data = createValidShipmentData();

    Shipment shipment = new Shipment(10L, data, 20L, "creator");

    assertEquals(10L, shipment.getOrderSummaryId());
    assertEquals(40L, shipment.getPickupLocationId());
    assertEquals(new BigDecimal("1.5"), shipment.getTotalWeightKgs());
    assertEquals(4, shipment.getTotalQuantity());
    assertEquals(new BigDecimal("25.00"), shipment.getPackagingCost());
    assertEquals(new BigDecimal("15.00"), shipment.getShippingCost());
    assertEquals(new BigDecimal("40.00"), shipment.getTotalCost());
    assertEquals(20L, shipment.getClientId());
    assertEquals("creator", shipment.getCreatedUser());
    assertEquals("creator", shipment.getModifiedUser());
  }

  @Test
  void shipment_CreateConstructor_MissingOrderSummaryIdThrowsBadRequest() {
    PurchaseOrderRequestModel.ShipmentData data = createValidShipmentData();

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> new Shipment(null, data, 20L, "creator"));
    assertEquals(
        ErrorMessages.ShipmentErrorMessages.ORDER_SUMMARY_ID_REQUIRED, exception.getMessage());
  }

  @Test
  void shipment_UpdateConstructor_PreservesCreatedMetadataAndCourierSelection() {
    PurchaseOrderRequestModel.ShipmentData updatedData = createValidShipmentData();
    updatedData.setPackagingCost(new BigDecimal("30.00"));
    updatedData.setShippingCost(new BigDecimal("20.00"));

    Shipment existing = new Shipment();
    LocalDateTime createdAt = LocalDateTime.of(2025, 2, 1, 11, 0);
    existing.setShipmentId(501L);
    existing.setCreatedUser("originalCreator");
    existing.setCreatedAt(createdAt);
    existing.setClientId(77L);
    existing.setSelectedCourierCompanyId(333L);
    existing.setSelectedCourierName("FastShip");
    existing.setSelectedCourierRate(new BigDecimal("99.99"));
    existing.setSelectedCourierMetadata("{\"id\":333}");

    Shipment updated = new Shipment(15L, updatedData, "editor", existing);

    assertEquals(501L, updated.getShipmentId());
    assertEquals("originalCreator", updated.getCreatedUser());
    assertEquals(createdAt, updated.getCreatedAt());
    assertEquals(77L, updated.getClientId());
    assertEquals("editor", updated.getModifiedUser());
    assertEquals(333L, updated.getSelectedCourierCompanyId());
    assertEquals("FastShip", updated.getSelectedCourierName());
    assertEquals(new BigDecimal("99.99"), updated.getSelectedCourierRate());
    assertEquals("{\"id\":333}", updated.getSelectedCourierMetadata());
    assertEquals(new BigDecimal("50.00"), updated.getTotalCost());
  }

  @Test
  void shipment_SetCourierSelection_NullInputThrowsBadRequest() {
    Shipment shipment = new Shipment(10L, createValidShipmentData(), 20L, "creator");

    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> shipment.setCourierSelection(null));
    assertEquals(
        ErrorMessages.ShipmentErrorMessages.COURIER_SELECTION_REQUIRED, exception.getMessage());
  }

  @Test
  void shipment_SetCourierSelection_ValidInputUpdatesCourierAndRecalculatesTotal() {
    Shipment shipment = new Shipment(10L, createValidShipmentData(), 20L, "creator");
    PurchaseOrderRequestModel.CourierSelectionData courier = createValidCourierSelection();
    courier.setCourierRate(new BigDecimal("11.00"));
    courier.setCourierMinWeight(null);

    shipment.setCourierSelection(courier);

    assertEquals(1001L, shipment.getSelectedCourierCompanyId());
    assertEquals("Courier One", shipment.getSelectedCourierName());
    assertEquals(new BigDecimal("11.00"), shipment.getSelectedCourierRate());
    assertEquals(BigDecimal.ZERO, shipment.getSelectedCourierMinWeight());
    assertEquals("{\"company\":\"Courier One\"}", shipment.getSelectedCourierMetadata());
    assertEquals(new BigDecimal("11.00"), shipment.getShippingCost());
    assertEquals(new BigDecimal("36.00"), shipment.getTotalCost());
  }

  @Test
  void shipment_SetCourierSelection_NegativeRateKeepsExistingShippingCost() {
    Shipment shipment = new Shipment(10L, createValidShipmentData(), 20L, "creator");
    PurchaseOrderRequestModel.CourierSelectionData courier = createValidCourierSelection();
    courier.setCourierRate(new BigDecimal("-1.00"));

    shipment.setCourierSelection(courier);

    assertEquals(new BigDecimal("15.00"), shipment.getShippingCost());
    assertEquals(new BigDecimal("40.00"), shipment.getTotalCost());
  }

  @Test
  void shipment_PopulateFromShipRocketOrderResponse_MapsResponseFields() {
    Shipment shipment = new Shipment(10L, createValidShipmentData(), 20L, "creator");
    ShipRocketOrderResponseModel response = new ShipRocketOrderResponseModel();
    response.setOrderId(9001L);
    response.setShipmentId(8001L);
    response.setTrackingId("TRK-1");
    response.setStatus("READY_TO_SHIP");
    response.setManifestUrl("https://manifest");
    response.setInvoiceUrl("https://invoice");
    response.setLabelUrl("https://label");

    shipment.populateFromShipRocketOrderResponse(response);

    assertEquals("9001", shipment.getShipRocketOrderId());
    assertEquals(8001L, shipment.getShipRocketShipmentId());
    assertEquals("TRK-1", shipment.getShipRocketTrackingId());
    assertEquals("READY_TO_SHIP", shipment.getShipRocketStatus());
    assertEquals("https://manifest", shipment.getShipRocketManifestUrl());
    assertEquals("https://invoice", shipment.getShipRocketInvoiceUrl());
    assertEquals("https://label", shipment.getShipRocketLabelUrl());
  }

  private PurchaseOrderRequestModel.ShipmentData createValidShipmentData() {
    PurchaseOrderRequestModel.ShipmentData data = new PurchaseOrderRequestModel.ShipmentData();
    data.setPickupLocationId(40L);
    data.setTotalWeightKgs(new BigDecimal("1.5"));
    data.setTotalQuantity(4);
    data.setPackagingCost(new BigDecimal("25.00"));
    data.setShippingCost(new BigDecimal("15.00"));
    data.setExpectedDeliveryDate(LocalDateTime.of(2025, 3, 1, 9, 0));
    return data;
  }

  private PurchaseOrderRequestModel.CourierSelectionData createValidCourierSelection() {
    PurchaseOrderRequestModel.CourierSelectionData courier =
        new PurchaseOrderRequestModel.CourierSelectionData();
    courier.setCourierCompanyId(1001L);
    courier.setCourierName("Courier One");
    courier.setCourierRate(new BigDecimal("10.00"));
    courier.setCourierMinWeight(new BigDecimal("0.5"));
    courier.setCourierMetadata("{\"company\":\"Courier One\"}");
    return courier;
  }
}
