package springapi.models.requestmodels;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Request model for calculating shipping options at the order level. Groups products by pickup.
 * location and calculates combined shipping.
 */
@Getter
@Setter
public class ShippingCalculationRequestModel {

  /** Delivery address postal code. */
  private String deliveryPostcode;

  /** Whether the order is Cash on Delivery. */
  private Boolean isCod;

  /** List of pickup locations with their shipment details. */
  private List<PickupLocationShipment> pickupLocations;

  /** Represents the pickup location shipment component. */
  @Getter
  @Setter
  public static class PickupLocationShipment {
    /** Pickup location ID. */
    private Long pickupLocationId;

    /** Pickup location name. */
    private String locationName;

    /** Pickup location postal code. */
    private String pickupPostcode;

    /** Total weight in kg for all products from this location. */
    private BigDecimal totalWeightKgs;

    /** Total quantity of items from this location. */
    private Integer totalQuantity;

    /** List of product IDs being shipped from this location. */
    private List<Long> productIds;
  }
}
