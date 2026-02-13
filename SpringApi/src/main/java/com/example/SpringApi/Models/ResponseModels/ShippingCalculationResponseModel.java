package com.example.SpringApi.Models.ResponseModels;

import com.example.SpringApi.Models.ShippingResponseModel.ShippingOptionsResponseModel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Response model for shipping calculation at the order level.
 * Returns shipping options grouped by pickup location.
 */
@Getter
@Setter
public class ShippingCalculationResponseModel {
    
    /** List of pickup locations with their available shipping options */
    private List<LocationShippingOptions> locationOptions = new ArrayList<>();
    
    /** Total shipping cost for all locations (sum of selected couriers) */
    private BigDecimal totalShippingCost = BigDecimal.ZERO;
    
    @Getter
    @Setter
    public static class LocationShippingOptions {
        /** Pickup location ID */
        private Long pickupLocationId;

        /** Pickup location name */
        private String locationName;

        /** Pickup location postal code */
        private String pickupPostcode;

        /** Total weight being shipped from this location */
        private BigDecimal totalWeightKgs;

        /** Total quantity of items from this location */
        private Integer totalQuantity;

        /** List of product IDs being shipped from this location */
        private List<Long> productIds;

        /** Available courier options for this location, sorted by rate (lowest first) */
        private List<CourierOption> availableCouriers = new ArrayList<>();

        /** The selected/recommended courier (cheapest by default) */
        private CourierOption selectedCourier;

        public LocationShippingOptions() {
            // Required for JSON serialization/deserialization.
        }

        public LocationShippingOptions(Long pickupLocationId, String locationName, String pickupPostcode,
                                        BigDecimal totalWeightKgs, Integer totalQuantity, List<Long> productIds) {
            this.pickupLocationId = pickupLocationId;
            this.locationName = locationName;
            this.pickupPostcode = pickupPostcode;
            this.totalWeightKgs = totalWeightKgs;
            this.totalQuantity = totalQuantity;
            this.productIds = productIds;
        }
    }
    
    @Getter
    @Setter
    public static class CourierOption {
        // Basic identification
        private Integer courierCompanyId;
        private Integer id;
        private String courierName;
        private String courierType;
        private String description;
        
        // Pricing
        private BigDecimal rate;
        private BigDecimal codCharges;
        private BigDecimal freightCharge;
        private BigDecimal rtoCharges;
        private Integer coverageCharges;
        private Integer otherCharges;
        private String cost;
        
        // Delivery information
        private String estimatedDeliveryDays;
        private String etd;
        private Integer etdHours;
        private String edd;
        
        // Performance metrics
        private Double rating;
        private Double deliveryPerformance;
        private Double pickupPerformance;
        private Double rtoPerformance;
        private Double trackingPerformance;
        private String rank;
        
        // Location info
        private String city;
        private String state;
        private String postcode;
        private String zone;
        
        // Weight and dimensions
        private Double chargeWeight;
        private Double minWeight;
        private String baseWeight;
        private String airMaxWeight;
        private String surfaceMaxWeight;
        
        // Service features
        private Boolean isSurface;
        private Boolean isHyperlocal;
        private String realtimeTracking;
        private String callBeforeDelivery;
        private String podAvailable;
        private Boolean isRtoAddressAvailable;
        
        // Pickup information
        private String pickupAvailability;
        private String cutoffTime;
        
        // Status flags
        private Integer blocked;
        private Integer cod;

        public CourierOption() {
            // Required for JSON serialization/deserialization.
        }

        /**
         * Creates a CourierOption from Shiprocket API courier company data.
         */
        public static CourierOption fromShiprocketCourier(ShippingOptionsResponseModel.AvailableCourierCompany courier) {
            CourierOption option = new CourierOption();
            option.setCourierCompanyId(courier.getCourierCompanyId());
            option.setId(courier.getId());
            option.setCourierName(courier.getCourierName());
            option.setCourierType(courier.getCourierType());
            option.setDescription(courier.getDescription());
            option.setRate(BigDecimal.valueOf(courier.getRate()));
            option.setCodCharges(BigDecimal.valueOf(courier.getCodCharges()));
            option.setFreightCharge(BigDecimal.valueOf(courier.getFreightCharge()));
            option.setRtoCharges(BigDecimal.valueOf(courier.getRtoCharges()));
            option.setCoverageCharges(courier.getCoverageCharges());
            option.setOtherCharges(courier.getOtherCharges());
            option.setCost(courier.getCost());
            option.setEstimatedDeliveryDays(courier.getEstimatedDeliveryDays());
            option.setEtd(courier.getEtd());
            option.setEtdHours(courier.getEtdHours());
            option.setEdd(courier.getEdd());
            option.setRating(courier.getRating());
            option.setDeliveryPerformance(courier.getDeliveryPerformance());
            option.setPickupPerformance(courier.getPickupPerformance());
            option.setRtoPerformance(courier.getRtoPerformance());
            option.setTrackingPerformance(courier.getTrackingPerformance());
            option.setRank(courier.getRank());
            option.setCity(courier.getCity());
            option.setState(courier.getState());
            option.setPostcode(courier.getPostcode());
            option.setZone(courier.getZone());
            option.setChargeWeight(courier.getChargeWeight());
            option.setMinWeight(courier.getMinWeight());
            option.setBaseWeight(courier.getBaseWeight());
            option.setAirMaxWeight(courier.getAirMaxWeight());
            option.setSurfaceMaxWeight(courier.getSurfaceMaxWeight());
            option.setIsSurface(courier.isSurface());
            option.setIsHyperlocal(courier.isHyperlocal());
            option.setRealtimeTracking(courier.getRealtimeTracking());
            option.setCallBeforeDelivery(courier.getCallBeforeDelivery());
            option.setPodAvailable(courier.getPodAvailable());
            option.setIsRtoAddressAvailable(courier.isRtoAddressAvailable());
            option.setPickupAvailability(courier.getPickupAvailability());
            option.setCutoffTime(courier.getCutoffTime());
            option.setBlocked(courier.getBlocked());
            option.setCod(courier.getCod());
            return option;
        }
    }
}
