package com.example.SpringApi.Models.ResponseModels;

import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.DatabaseModels.PickupLocation;
import com.example.SpringApi.Models.DatabaseModels.ProductPickupLocationMapping;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Response model for product stock at a specific pickup location.
 * Includes pickup location details and address for distance calculation.
 */
@Getter
@Setter
public class ProductStockByLocationResponseModel {
    private Long pickupLocationId;
    private String locationName;
    private Integer availableStock;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private Integer reorderLevel;
    
    // Address fields for distance calculation and display
    private String addressType;
    private String streetAddress;
    private String streetAddress2;
    private String streetAddress3;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String nameOnAddress;
    private String emailOnAddress;
    private String phoneOnAddress;
    
    // Package information at this location
    private List<PackageInfoModel> availablePackages = new ArrayList<>();
    
    // Packaging estimate (calculated based on product dimensions)
    private List<PackageUsageModel> packagingEstimate = new ArrayList<>();
    private BigDecimal totalPackagingCost = BigDecimal.ZERO;
    private Integer maxItemsPackable = 0; // Max items that can be packed based on dimensions
    
    // Shipping options from this pickup location (sorted by price low to high)
    private List<CourierOptionModel> availableCouriers = new ArrayList<>();
    private CourierOptionModel selectedCourier; // Default: cheapest option
    
    public ProductStockByLocationResponseModel() {
        // Required for JSON serialization/deserialization.
    }
    
    public ProductStockByLocationResponseModel(ProductPickupLocationMapping mapping) {
        this.pickupLocationId = mapping.getPickupLocationId();
        this.availableStock = mapping.getAvailableStock();
        this.minStockLevel = mapping.getMinStockLevel();
        this.maxStockLevel = mapping.getMaxStockLevel();
        this.reorderLevel = mapping.getReorderLevel();
        
        PickupLocation pickupLocation = mapping.getPickupLocation();
        if (pickupLocation != null) {
            this.locationName = pickupLocation.getAddressNickName();
            
            Address address = pickupLocation.getAddress();
            if (address != null) {
                this.addressType = address.getAddressType();
                this.streetAddress = address.getStreetAddress();
                this.streetAddress2 = address.getStreetAddress2();
                this.streetAddress3 = address.getStreetAddress3();
                this.city = address.getCity();
                this.state = address.getState();
                this.postalCode = address.getPostalCode();
                this.country = address.getCountry();
                this.nameOnAddress = address.getNameOnAddress();
                this.emailOnAddress = address.getEmailOnAddress();
                this.phoneOnAddress = address.getPhoneOnAddress();
            }
        }
    }
    
    // Product dimensions for package capacity calculation
    private BigDecimal productLength;
    private BigDecimal productBreadth;
    private BigDecimal productHeight;
    private BigDecimal productWeightKgs;
    
    /**
     * Package information model for available packages at a location
     */
    @Getter
    @Setter
    public static class PackageInfoModel {
        private Long packageId;
        private String packageName;
        private String packageType;
        private BigDecimal pricePerUnit;  // Price per package
        private Integer availableQuantity; // Number of this package type available
        
        // Package dimensions for capacity calculation
        private Integer packageLength;
        private Integer packageBreadth;
        private Integer packageHeight;
        private BigDecimal maxWeight;
        
        public PackageInfoModel() {
            // Required for JSON serialization/deserialization.
        }
        
    }
    
    /**
     * Package usage model - how many of each package type are needed
     */
    @Getter
    @Setter
    public static class PackageUsageModel {
        private Long packageId;
        private String packageName;
        private String packageType;
        private Integer quantityUsed;
        private BigDecimal pricePerUnit;
        private BigDecimal totalCost;
        
        public PackageUsageModel() {
            // Required for JSON serialization/deserialization.
        }
        
        public PackageUsageModel(Long packageId, String packageName, String packageType,
                                 Integer quantityUsed, BigDecimal pricePerUnit, BigDecimal totalCost) {
            this.packageId = packageId;
            this.packageName = packageName;
            this.packageType = packageType;
            this.quantityUsed = quantityUsed;
            this.pricePerUnit = pricePerUnit;
            this.totalCost = totalCost;
        }
    }
    
    /**
     * Courier/shipping option model - comprehensive data from Shiprocket API response
     */
    @Getter
    @Setter
    public static class CourierOptionModel {
        // Basic identification
        private Integer courierCompanyId;
        private Integer id;
        private String courierName;
        private String courierType; // e.g., "Surface", "Air"
        private String description;
        
        // Pricing
        private BigDecimal rate; // Total shipping cost
        private BigDecimal codCharges; // COD charges if applicable
        private BigDecimal freightCharge;
        private BigDecimal rtoCharges; // Return to origin charges
        private Integer coverageCharges;
        private Integer otherCharges;
        private Integer entryTax;
        private String cost;
        private Double codMultiplier;
        
        // Delivery information
        private String estimatedDeliveryDays;
        private String etd; // Estimated time of delivery (human readable)
        private Integer etdHours; // ETD in hours
        private String edd; // Expected delivery date
        
        // Performance metrics
        private Double rating; // Courier rating (0-5)
        private Double deliveryPerformance; // Percentage
        private Double pickupPerformance; // Percentage
        private Double rtoPerformance; // RTO performance percentage
        private Double trackingPerformance;
        private String rank;
        
        // Location info
        private String city;
        private String state;
        private String postcode;
        private String zone;
        private Integer region;
        private Integer localRegion;
        private Integer metro;
        
        // Weight and dimensions
        private Double chargeWeight; // Weight used for charging
        private Double minWeight;
        private String baseWeight;
        private String airMaxWeight;
        private String surfaceMaxWeight;
        private Integer volumetricMaxWeight;
        private Double weightCases;
        
        // Service features
        private Boolean isSurface;
        private Boolean isHyperlocal;
        private Integer isInternational;
        private String realtimeTracking;
        private String callBeforeDelivery;
        private String podAvailable; // Proof of delivery
        private Boolean isRtoAddressAvailable;
        private Integer qcCourier; // Quality check courier
        private Boolean secureShipmentDisabled;
        private Boolean odablock; // Out of delivery area block
        
        // Pickup information
        private String pickupAvailability;
        private String pickupPriority;
        private Integer pickupSupressHours;
        private Integer secondsLeftForPickup;
        private String cutoffTime;
        
        // Suppression/delay info
        private String suppressDate;
        private String suppressText;
        
        // Status flags
        private Integer blocked;
        private Integer cod; // COD available (1/0)
        private Integer isCustomRate;
        private Integer shipType;
        private Integer mode;
        
        // Other
        private Integer assuredAmount;
        private String deliveryBoyContact;
        private String others;
        
        public CourierOptionModel() {
            // Required for JSON serialization/deserialization.
        }
    }
}
