package springapi.modeltests.responsemodels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import springapi.models.databasemodels.Shipment;
import springapi.models.responsemodels.PurchaseOrderResponseModel;

@DisplayName("Purchase Order Response Model Behavior Tests")
class PurchaseOrderResponseModelBehaviorTest {

  // Total Tests: 2

  /**
   * Purpose: Verify courier selection data constructor maps shipment courier fields. Expected
   * Result: Response data mirrors selected courier fields from shipment. Assertions: All courier
   * selection fields match shipment source values.
   */
  @Test
  @DisplayName("purchaseOrderResponseModel - CourierSelectionData MapsShipmentFields - Success")
  void purchaseOrderResponseModel_s01_courierSelectionDataMapsShipmentFields_success() {
    // Arrange
    Shipment shipment = new Shipment();
    shipment.setSelectedCourierCompanyId(1001L);
    shipment.setSelectedCourierName("RapidShip");
    shipment.setSelectedCourierRate(new BigDecimal("149.75"));
    shipment.setSelectedCourierMinWeight(new BigDecimal("0.500"));
    shipment.setSelectedCourierMetadata("{\"service\":\"express\"}");

    // Act
    PurchaseOrderResponseModel.CourierSelectionResponseData data =
        new PurchaseOrderResponseModel.CourierSelectionResponseData(shipment);

    // Assert
    assertEquals(1001L, data.getCourierCompanyId());
    assertEquals("RapidShip", data.getCourierName());
    assertEquals(new BigDecimal("149.75"), data.getCourierRate());
    assertEquals(new BigDecimal("0.500"), data.getCourierMinWeight());
    assertEquals("{\"service\":\"express\"}", data.getCourierMetadata());
  }

  /**
   * Purpose: Verify courier selection constructor handles null shipment safely. Expected Result: No
   * exception and all fields remain null. Assertions: Courier selection fields are null.
   */
  @Test
  @DisplayName("purchaseOrderResponseModel - CourierSelectionData NullShipment - Success")
  void purchaseOrderResponseModel_s02_courierSelectionDataNullShipment_success() {
    // Arrange
    Shipment shipment = null;

    // Act
    PurchaseOrderResponseModel.CourierSelectionResponseData data =
        new PurchaseOrderResponseModel.CourierSelectionResponseData(shipment);

    // Assert
    assertNull(data.getCourierCompanyId());
    assertNull(data.getCourierName());
    assertNull(data.getCourierRate());
    assertNull(data.getCourierMinWeight());
    assertNull(data.getCourierMetadata());
  }
}
