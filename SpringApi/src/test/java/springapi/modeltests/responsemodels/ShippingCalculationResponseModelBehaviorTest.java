package springapi.modeltests.responsemodels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import springapi.models.responsemodels.ShippingCalculationResponseModel;
import springapi.models.shippingresponsemodel.ShippingOptionsResponseModel;

@DisplayName("Shipping Calculation Response Model Behavior Tests")
class ShippingCalculationResponseModelBehaviorTest {

  // Total Tests: 3

  /**
   * Purpose: Verify default construction initializes collection and total cost defaults. Expected
   * Result: Location options list is initialized and cost defaults to zero. Assertions: Defaults
   * are non-null and match expected values.
   */
  @Test
  @DisplayName(
      "shippingCalculationResponseModel - DefaultConstructor Initializes Defaults - Success")
  void shippingCalculationResponseModel_s01_defaultConstructorInitializesDefaults_success() {
    // Arrange
    ShippingCalculationResponseModel model = new ShippingCalculationResponseModel();

    // Act
    List<ShippingCalculationResponseModel.LocationShippingOptions> locations =
        model.getLocationOptions();

    // Assert
    assertNotNull(locations);
    assertTrue(locations.isEmpty());
    assertEquals(BigDecimal.ZERO, model.getTotalShippingCost());
  }

  /**
   * Purpose: Verify parameterized location constructor sets core fields correctly. Expected Result:
   * Constructor arguments are assigned to matching properties. Assertions: All constructor-backed
   * fields match expected values.
   */
  @Test
  @DisplayName("shippingCalculationResponseModel - LocationConstructor Maps Fields - Success")
  void shippingCalculationResponseModel_s02_locationConstructorMapsFields_success() {
    // Arrange
    List<Long> productIds = List.of(5L, 9L);

    // Act
    ShippingCalculationResponseModel.LocationShippingOptions options =
        new ShippingCalculationResponseModel.LocationShippingOptions(
            12L, "Warehouse A", "10001", new BigDecimal("15.50"), 7, productIds);

    // Assert
    assertEquals(12L, options.getPickupLocationId());
    assertEquals("Warehouse A", options.getLocationName());
    assertEquals("10001", options.getPickupPostcode());
    assertEquals(new BigDecimal("15.50"), options.getTotalWeightKgs());
    assertEquals(7, options.getTotalQuantity());
    assertEquals(productIds, options.getProductIds());
    assertNotNull(options.getAvailableCouriers());
  }

  /**
   * Purpose: Verify Shiprocket courier mapping populates response courier option fields. Expected
   * Result: All mapped properties are transferred from source courier object. Assertions:
   * Representative mapped fields match expected values.
   */
  @Test
  @DisplayName("shippingCalculationResponseModel - FromShiprocketCourier MapsFields - Success")
  void shippingCalculationResponseModel_s03_fromShiprocketCourierMapsFields_success() {
    // Arrange
    ShippingOptionsResponseModel.AvailableCourierCompany courier =
        new ShippingOptionsResponseModel.AvailableCourierCompany();
    courier.setCourierCompanyId(101);
    courier.setId(501);
    courier.setCourierName("FastShip");
    courier.setCourierType("Air");
    courier.setDescription("Priority");
    courier.setRate(99.25);
    courier.setCodCharges(5.10);
    courier.setFreightCharge(20.0);
    courier.setRtoCharges(7.0);
    courier.setCoverageCharges(2);
    courier.setOtherCharges(3);
    courier.setCost("104.35");
    courier.setEstimatedDeliveryDays("2");
    courier.setEtd("2026-02-28");
    courier.setEtdHours(48);
    courier.setEdd("2026-03-01");
    courier.setRating(4.7);
    courier.setDeliveryPerformance(91.3);
    courier.setPickupPerformance(90.0);
    courier.setRtoPerformance(88.2);
    courier.setTrackingPerformance(92.8);
    courier.setRank("1");
    courier.setCity("New York");
    courier.setState("NY");
    courier.setPostcode("10001");
    courier.setZone("North");
    courier.setChargeWeight(4.5);
    courier.setMinWeight(0.5);
    courier.setBaseWeight("0.5");
    courier.setAirMaxWeight("30");
    courier.setSurfaceMaxWeight("100");
    courier.setSurface(true);
    courier.setHyperlocal(false);
    courier.setRealtimeTracking("yes");
    courier.setCallBeforeDelivery("yes");
    courier.setPodAvailable("yes");
    courier.setRtoAddressAvailable(true);
    courier.setPickupAvailability("available");
    courier.setCutoffTime("18:00");
    courier.setBlocked(0);
    courier.setCod(1);

    // Act
    ShippingCalculationResponseModel.CourierOption option =
        ShippingCalculationResponseModel.CourierOption.fromShiprocketCourier(courier);

    // Assert
    assertEquals(101, option.getCourierCompanyId());
    assertEquals(501, option.getId());
    assertEquals("FastShip", option.getCourierName());
    assertEquals(new BigDecimal("99.25"), option.getRate());
    assertEquals(new BigDecimal("5.1"), option.getCodCharges());
    assertEquals("10001", option.getPostcode());
    assertEquals(true, option.getIsSurface());
    assertEquals(true, option.getIsRtoAddressAvailable());
    assertEquals("18:00", option.getCutoffTime());
    assertEquals(1, option.getCod());
  }
}
