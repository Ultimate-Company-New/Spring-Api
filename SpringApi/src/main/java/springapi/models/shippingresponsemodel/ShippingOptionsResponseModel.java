package springapi.models.shippingresponsemodel;

import com.nimbusds.jose.shaded.gson.annotations.SerializedName;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Response model for Shiprocket courier serviceability API. Variable names use underscores to
 * match. Shiprocket API response format.
 */
@Getter
@Setter
public class ShippingOptionsResponseModel {
  @SerializedName("company_auto_shipment_insurance_setting")
  private boolean companyAutoShipmentInsuranceSetting;

  @SerializedName("covid_zones")
  private CovidZones covidZones;

  @SerializedName("currency")
  private String currency;

  @SerializedName("data")
  private Data data;

  @SerializedName("dg_courier")
  private int dgCourier;

  @SerializedName("eligible_for_insurance")
  private int eligibleForInsurance;

  @SerializedName("insurace_opted_at_order_creation")
  private boolean insuraceOptedAtOrderCreation;

  @SerializedName("is_allow_templatized_pricing")
  private boolean allowTemplatizedPricing;

  @SerializedName("is_latlong")
  private int latlong;

  @SerializedName("is_old_zone_opted")
  private boolean oldZoneOpted;

  @SerializedName("is_zone_from_mongo")
  private boolean zoneFromMongo;

  @SerializedName("label_generate_type")
  private int labelGenerateType;

  @SerializedName("on_new_zone")
  private int onNewZone;

  @SerializedName("seller_address")
  private List<Object> sellerAddress;

  @SerializedName("status")
  private int status;

  @SerializedName("user_insurance_manadatory")
  private boolean userInsuranceManadatory;

  /** Represents the available courier company component. */
  @Getter
  @Setter
  public static class AvailableCourierCompany {
    @SerializedName("air_max_weight")
    private String airMaxWeight;

    @SerializedName("assured_amount")
    private int assuredAmount;

    @SerializedName("base_courier_id")
    private Object baseCourierId;

    @SerializedName("base_weight")
    private String baseWeight;

    @SerializedName("blocked")
    private int blocked;

    @SerializedName("call_before_delivery")
    private String callBeforeDelivery;

    @SerializedName("charge_weight")
    private double chargeWeight;

    @SerializedName("city")
    private String city;

    @SerializedName("cod")
    private int cod;

    @SerializedName("cod_charges")
    private double codCharges;

    @SerializedName("cod_multiplier")
    private double codMultiplier;

    @SerializedName("cost")
    private String cost;

    @SerializedName("courier_company_id")
    private int courierCompanyId;

    @SerializedName("courier_name")
    private String courierName;

    @SerializedName("courier_type")
    private String courierType;

    @SerializedName("coverage_charges")
    private int coverageCharges;

    @SerializedName("cutoff_time")
    private String cutoffTime;

    @SerializedName("delivery_boy_contact")
    private String deliveryBoyContact;

    @SerializedName("delivery_performance")
    private double deliveryPerformance;

    @SerializedName("description")
    private String description;

    @SerializedName("edd")
    private String edd;

    @SerializedName("entry_tax")
    private int entryTax;

    @SerializedName("estimated_delivery_days")
    private String estimatedDeliveryDays;

    @SerializedName("etd")
    private String etd;

    @SerializedName("etd_hours")
    private int etdHours;

    @SerializedName("freight_charge")
    private double freightCharge;

    @SerializedName("id")
    private int id;

    @SerializedName("is_custom_rate")
    private int customRate;

    @SerializedName("is_hyperlocal")
    private boolean hyperlocal;

    @SerializedName("is_international")
    private int international;

    @SerializedName("is_rto_address_available")
    private boolean rtoAddressAvailable;

    @SerializedName("is_surface")
    private boolean surface;

    @SerializedName("local_region")
    private int localRegion;

    @SerializedName("metro")
    private int metro;

    @SerializedName("min_weight")
    private double minWeight;

    @SerializedName("mode")
    private int mode;

    @SerializedName("odablock")
    private boolean odablock;

    @SerializedName("other_charges")
    private int otherCharges;

    @SerializedName("others")
    private String others;

    @SerializedName("pickup_availability")
    private String pickupAvailability;

    @SerializedName("pickup_performance")
    private double pickupPerformance;

    @SerializedName("pickup_priority")
    private String pickupPriority;

    @SerializedName("pickup_supress_hours")
    private int pickupSupressHours;

    @SerializedName("pod_available")
    private String podAvailable;

    @SerializedName("postcode")
    private String postcode;

    @SerializedName("qc_courier")
    private int qcCourier;

    @SerializedName("rank")
    private String rank;

    @SerializedName("rate")
    private double rate;

    @SerializedName("rating")
    private double rating;

    @SerializedName("realtime_tracking")
    private String realtimeTracking;

    @SerializedName("region")
    private int region;

    @SerializedName("rto_charges")
    private double rtoCharges;

    @SerializedName("rto_performance")
    private double rtoPerformance;

    @SerializedName("seconds_left_for_pickup")
    private int secondsLeftForPickup;

    @SerializedName("secure_shipment_disabled")
    private boolean secureShipmentDisabled;

    @SerializedName("ship_type")
    private int shipType;

    @SerializedName("state")
    private String state;

    @SerializedName("suppress_date")
    private String suppressDate;

    @SerializedName("suppress_text")
    private String suppressText;

    @SerializedName("suppression_dates")
    private SuppressionDates suppressionDates;

    @SerializedName("surface_max_weight")
    private String surfaceMaxWeight;

    @SerializedName("tracking_performance")
    private double trackingPerformance;

    @SerializedName("volumetric_max_weight")
    private int volumetricMaxWeight;

    @SerializedName("weight_cases")
    private double weightCases;

    @SerializedName("zone")
    private String zone;
  }

  /** Represents the suppression dates component. */
  @Getter
  @Setter
  public static class SuppressionDates {
    @SerializedName("action_on")
    private String actionOn;

    @SerializedName("delay_remark")
    private String delayRemark;

    @SerializedName("delivery_delay_by")
    private int deliveryDelayBy;

    @SerializedName("delivery_delay_days")
    private String deliveryDelayDays;

    @SerializedName("delivery_delay_from")
    private String deliveryDelayFrom;

    @SerializedName("delivery_delay_to")
    private String deliveryDelayTo;

    @SerializedName("pickup_delay_by")
    private int pickupDelayBy;

    @SerializedName("pickup_delay_days")
    private String pickupDelayDays;

    @SerializedName("pickup_delay_from")
    private String pickupDelayFrom;

    @SerializedName("pickup_delay_to")
    private String pickupDelayTo;
  }

  /** Represents the covid zones component. */
  @Getter
  @Setter
  public static class CovidZones {
    @SerializedName("delivery_zone")
    private Object deliveryZone;

    @SerializedName("pickup_zone")
    private Object pickupZone;
  }

  /** Represents the data component. */
  @Getter
  @Setter
  public static class Data {
    @SerializedName("available_courier_companies")
    private List<AvailableCourierCompany> availableCourierCompanies;

    @SerializedName("child_courier_id")
    private Object childCourierId;

    @SerializedName("is_recommendation_enabled")
    private int recommendationEnabled;

    @SerializedName("recommendation_advance_rule")
    private int recommendationAdvanceRule;

    @SerializedName("recommended_by")
    private RecommendedBy recommendedBy;

    @SerializedName("recommended_courier_company_id")
    private int recommendedCourierCompanyId;

    @SerializedName("shiprocket_recommended_courier_id")
    private int shiprocketRecommendedCourierId;
  }

  /** Represents the recommended by component. */
  @Getter
  @Setter
  public static class RecommendedBy {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;
  }
}
