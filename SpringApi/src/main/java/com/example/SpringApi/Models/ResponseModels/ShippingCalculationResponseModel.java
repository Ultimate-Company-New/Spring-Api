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
        }

        /**
         * Creates a CourierOption from Shiprocket API courier company data.
         */
        public static CourierOption fromShiprocketCourier(ShippingOptionsResponseModel.AvailableCourierCompany courier) {
            CourierOption option = new CourierOption();
            option.setCourierCompanyId(courier.courier_company_id);
            option.setId(courier.id);
            option.setCourierName(courier.courier_name);
            option.setCourierType(courier.courier_type);
            option.setDescription(courier.description);
            option.setRate(BigDecimal.valueOf(courier.rate));
            option.setCodCharges(BigDecimal.valueOf(courier.cod_charges));
            option.setFreightCharge(BigDecimal.valueOf(courier.freight_charge));
            option.setRtoCharges(BigDecimal.valueOf(courier.rto_charges));
            option.setCoverageCharges(courier.coverage_charges);
            option.setOtherCharges(courier.other_charges);
            option.setCost(courier.cost);
            option.setEstimatedDeliveryDays(courier.estimated_delivery_days);
            option.setEtd(courier.etd);
            option.setEtdHours(courier.etd_hours);
            option.setEdd(courier.edd);
            option.setRating(courier.rating);
            option.setDeliveryPerformance(courier.delivery_performance);
            option.setPickupPerformance(courier.pickup_performance);
            option.setRtoPerformance(courier.rto_performance);
            option.setTrackingPerformance(courier.tracking_performance);
            option.setRank(courier.rank);
            option.setCity(courier.city);
            option.setState(courier.state);
            option.setPostcode(courier.postcode);
            option.setZone(courier.zone);
            option.setChargeWeight(courier.charge_weight);
            option.setMinWeight(courier.min_weight);
            option.setBaseWeight(courier.base_weight);
            option.setAirMaxWeight(courier.air_max_weight);
            option.setSurfaceMaxWeight(courier.surface_max_weight);
            option.setIsSurface(courier.is_surface);
            option.setIsHyperlocal(courier.is_hyperlocal);
            option.setRealtimeTracking(courier.realtime_tracking);
            option.setCallBeforeDelivery(courier.call_before_delivery);
            option.setPodAvailable(courier.pod_available);
            option.setIsRtoAddressAvailable(courier.is_rto_address_available);
            option.setPickupAvailability(courier.pickup_availability);
            option.setCutoffTime(courier.cutoff_time);
            option.setBlocked(courier.blocked);
            option.setCod(courier.cod);
            return option;
        }
    }
}
