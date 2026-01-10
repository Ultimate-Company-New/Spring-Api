package com.example.SpringApi.Services;

import com.example.SpringApi.Helpers.PackagingHelper;
import com.example.SpringApi.Helpers.ShippingHelper;
import com.example.SpringApi.Logging.ContextualLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping;
import com.example.SpringApi.Models.DatabaseModels.PickupLocation;
import com.example.SpringApi.Models.DatabaseModels.Product;
import com.example.SpringApi.Models.DatabaseModels.ProductPickupLocationMapping;
import com.example.SpringApi.Models.RequestModels.OrderOptimizationRequestModel;
import com.example.SpringApi.Models.RequestModels.ShippingCalculationRequestModel;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import com.example.SpringApi.Models.ResponseModels.OrderOptimizationResponseModel;
import com.example.SpringApi.Models.ResponseModels.PackageResponseModel;
import com.example.SpringApi.Models.ResponseModels.PickupLocationResponseModel;
import com.example.SpringApi.Models.ResponseModels.ProductResponseModel;
import com.example.SpringApi.Models.ResponseModels.ShippingCalculationResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.ShippingOptionsResponseModel;
import com.example.SpringApi.Repositories.PackagePickupLocationMappingRepository;
import com.example.SpringApi.Repositories.ProductPickupLocationMappingRepository;
import com.example.SpringApi.Repositories.ProductRepository;
import com.example.SpringApi.Repositories.ShipmentRepository;
import com.example.SpringApi.Repositories.ReturnShipmentRepository;
import com.example.SpringApi.Repositories.ReturnShipmentProductRepository;
import com.example.SpringApi.Models.DatabaseModels.Shipment;
import com.example.SpringApi.Models.DatabaseModels.ReturnShipment;
import com.example.SpringApi.Models.DatabaseModels.ReturnShipmentProduct;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.RequestModels.CreateReturnRequestModel;
import com.example.SpringApi.Models.RequestModels.ShipRocketReturnOrderRequestModel;
import com.example.SpringApi.Models.ResponseModels.ReturnShipmentResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.ShipRocketReturnOrderResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.ShipRocketAwbResponseModel;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Services.Interface.IShippingSubTranslator;
import com.nimbusds.jose.shaded.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Service for shipping-related operations.
 * 
 * Handles order-level shipping calculations that can combine multiple products
 * from the same pickup location. Gets Shiprocket credentials from the client.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class ShippingService extends BaseService implements IShippingSubTranslator {
    private static final ContextualLogger contextualLogger = ContextualLogger.getLogger(ShippingService.class);
    private static final Logger logger = LoggerFactory.getLogger(ShippingService.class);
    
    /**
     * Maximum weight per shipment in kg.
     * Couriers typically have limits around 50-100 kg.
     * We use 100 kg to maximize consolidation and minimize shipment count.
     * Multiple products (including partial quantities) can be combined in a single shipment
     * to reach this limit efficiently.
     */
    private static final BigDecimal MAX_WEIGHT_PER_SHIPMENT = BigDecimal.valueOf(150);
    
    /**
     * Timeout for shipping API calls in seconds.
     * Prevents infinite blocking if external API hangs.
     */
    private static final int SHIPPING_API_TIMEOUT_SECONDS = 30;
    
    private final ClientService clientService;
    private final ProductRepository productRepository;
    private final ProductPickupLocationMappingRepository productPickupLocationMappingRepository;
    private final PackagePickupLocationMappingRepository packagePickupLocationMappingRepository;
    private final PackagingHelper packagingHelper;
    private final ShipmentRepository shipmentRepository;
    private final ReturnShipmentRepository returnShipmentRepository;
    private final ReturnShipmentProductRepository returnShipmentProductRepository;

    @Autowired
    public ShippingService(
            ClientService clientService,
            ProductRepository productRepository,
            ProductPickupLocationMappingRepository productPickupLocationMappingRepository,
            PackagePickupLocationMappingRepository packagePickupLocationMappingRepository,
            PackagingHelper packagingHelper,
            ShipmentRepository shipmentRepository,
            ReturnShipmentRepository returnShipmentRepository,
            ReturnShipmentProductRepository returnShipmentProductRepository) {
        this.clientService = clientService;
        this.productRepository = productRepository;
        this.productPickupLocationMappingRepository = productPickupLocationMappingRepository;
        this.packagePickupLocationMappingRepository = packagePickupLocationMappingRepository;
        this.packagingHelper = packagingHelper;
        this.shipmentRepository = shipmentRepository;
        this.returnShipmentRepository = returnShipmentRepository;
        this.returnShipmentProductRepository = returnShipmentProductRepository;
    }

    /**
     * Calculate shipping options for an order.
     * Groups products by pickup location and returns available couriers for each location.
     * 
     * @param request Contains delivery postcode, COD flag, and list of pickup locations with weights
     * @return Shipping options for each pickup location with available couriers
     */
    @Override
    public ShippingCalculationResponseModel calculateShipping(ShippingCalculationRequestModel request) {
        ShippingCalculationResponseModel response = new ShippingCalculationResponseModel();
        response.setLocationOptions(new ArrayList<>());
        
        if (request.getPickupLocations() == null || request.getPickupLocations().isEmpty()) {
            return response;
        }
        
        // Get Shiprocket credentials from client
        ShippingHelper shippingHelper = getShippingHelper();
        String deliveryPostcode = request.getDeliveryPostcode();
        boolean isCod = Boolean.TRUE.equals(request.getIsCod());
        
        BigDecimal totalShippingCost = BigDecimal.ZERO;
        
        for (ShippingCalculationRequestModel.PickupLocationShipment location : request.getPickupLocations()) {
            ShippingCalculationResponseModel.LocationShippingOptions locationOptions = 
                new ShippingCalculationResponseModel.LocationShippingOptions();
            
            locationOptions.setPickupLocationId(location.getPickupLocationId());
            locationOptions.setLocationName(location.getLocationName());
            locationOptions.setPickupPostcode(location.getPickupPostcode());
            locationOptions.setTotalWeightKgs(location.getTotalWeightKgs());
            locationOptions.setTotalQuantity(location.getTotalQuantity());
            locationOptions.setProductIds(location.getProductIds());
            locationOptions.setAvailableCouriers(new ArrayList<>());
            
            // Get weight for shipping calculation (minimum 0.5 kg)
            BigDecimal weight = location.getTotalWeightKgs();
            if (weight == null || weight.compareTo(BigDecimal.valueOf(0.5)) < 0) {
                weight = BigDecimal.valueOf(0.5);
            }
            
            try {
                // Fetch shipping options from Shiprocket
                ShippingOptionsResponseModel shippingOptions = shippingHelper.getAvailableShippingOptions(
                    location.getPickupPostcode(),
                    deliveryPostcode,
                    isCod,
                    weight.toString()
                );
                
                if (shippingOptions != null && shippingOptions.getData() != null 
                    && shippingOptions.getData().available_courier_companies != null) {
                    
                    // Sort by rate (lowest first)
                    shippingOptions.getData().available_courier_companies.sort(
                        (a, b) -> Double.compare(a.rate, b.rate)
                    );
                    
                    // Map couriers to response
                    for (var courier : shippingOptions.getData().available_courier_companies) {
                        ShippingCalculationResponseModel.CourierOption option = mapCourier(courier);
                        locationOptions.getAvailableCouriers().add(option);
                    }
                    
                    // Set the cheapest as selected by default
                    if (!locationOptions.getAvailableCouriers().isEmpty()) {
                        locationOptions.setSelectedCourier(locationOptions.getAvailableCouriers().get(0));
                        totalShippingCost = totalShippingCost.add(
                            locationOptions.getAvailableCouriers().get(0).getRate()
                        );
                    }
                }
            } catch (Exception e) {
                // Continue with other locations on error
            }
            
            response.getLocationOptions().add(locationOptions);
        }
        
        response.setTotalShippingCost(totalShippingCost);
        return response;
    }
    
    /**
     * Creates a ShippingHelper instance initialized with the current client's ShipRocket credentials.
     * 
     * @return ShippingHelper instance with client credentials
     */
    private ShippingHelper getShippingHelper() {
        ClientResponseModel client = clientService.getClientById(getClientId());
        return new ShippingHelper(client.getShipRocketEmail(), client.getShipRocketPassword());
    }
    
    /**
     * Maps a Shiprocket courier company to the response model.
     * 
     * @param courier The Shiprocket courier company data
     * @return Mapped CourierOption for the response
     */
    private ShippingCalculationResponseModel.CourierOption mapCourier(
            ShippingOptionsResponseModel.AvailableCourierCompany courier) {
        
        ShippingCalculationResponseModel.CourierOption option = 
            new ShippingCalculationResponseModel.CourierOption();
        
        // Basic identification
        option.setCourierCompanyId(courier.courier_company_id);
        option.setId(courier.id);
        option.setCourierName(courier.courier_name);
        option.setCourierType(courier.courier_type);
        option.setDescription(courier.description);
        
        // Pricing
        option.setRate(BigDecimal.valueOf(courier.rate));
        option.setCodCharges(BigDecimal.valueOf(courier.cod_charges));
        option.setFreightCharge(BigDecimal.valueOf(courier.freight_charge));
        option.setRtoCharges(BigDecimal.valueOf(courier.rto_charges));
        option.setCoverageCharges(courier.coverage_charges);
        option.setOtherCharges(courier.other_charges);
        option.setCost(courier.cost);
        
        // Delivery information
        option.setEstimatedDeliveryDays(courier.estimated_delivery_days);
        option.setEtd(courier.etd);
        option.setEtdHours(courier.etd_hours);
        option.setEdd(courier.edd);
        
        // Performance metrics
        option.setRating(courier.rating);
        option.setDeliveryPerformance(courier.delivery_performance);
        option.setPickupPerformance(courier.pickup_performance);
        option.setRtoPerformance(courier.rto_performance);
        option.setTrackingPerformance(courier.tracking_performance);
        option.setRank(courier.rank);
        
        // Location info
        option.setCity(courier.city);
        option.setState(courier.state);
        option.setPostcode(courier.postcode);
        option.setZone(courier.zone);
        
        // Weight
        option.setChargeWeight(courier.charge_weight);
        option.setMinWeight(courier.min_weight);
        option.setBaseWeight(courier.base_weight);
        option.setAirMaxWeight(courier.air_max_weight);
        option.setSurfaceMaxWeight(courier.surface_max_weight);
        
        // Service features
        option.setIsSurface(courier.is_surface);
        option.setIsHyperlocal(courier.is_hyperlocal);
        option.setRealtimeTracking(courier.realtime_tracking);
        option.setCallBeforeDelivery(courier.call_before_delivery);
        option.setPodAvailable(courier.pod_available);
        option.setIsRtoAddressAvailable(courier.is_rto_address_available);
        
        // Pickup information
        option.setPickupAvailability(courier.pickup_availability);
        option.setCutoffTime(courier.cutoff_time);
        
        // Status flags
        option.setBlocked(courier.blocked);
        option.setCod(courier.cod);
        
        return option;
    }
    
    // ============================================================================
    // Order Optimization Algorithm
    // ============================================================================
    
    /**
     * Internal class to hold product info with stock at each location
     */
    private static class ProductLocationInfo {
        Product productEntity; // Full entity for creating ProductResponseModel
        Long productId;
        String productTitle;
        BigDecimal weightKgs;
        BigDecimal length;
        BigDecimal breadth;
        BigDecimal height;
        int requestedQuantity;
        Map<Long, LocationStock> stockByLocation = new HashMap<>();
    }
    
    /**
     * Stock info for a product at a specific location
     */
    private static class LocationStock {
        int availableStock;
        int maxItemsPackable;
        List<PackagingHelper.PackageDimension> packageDimensions;
        String packagingErrorMessage; // Store error message from packaging calculation
        // Map of packageId to Package entity for creating PackageResponseModel
        Map<Long, com.example.SpringApi.Models.DatabaseModels.Package> packageEntities = new HashMap<>();
    }
    
    /**
     * Location info with packages and postal code
     */
    private static class LocationInfo {
        PickupLocation pickupLocationEntity; // Full entity for creating PickupLocationResponseModel
        Long pickupLocationId;
        String locationName;
        String postalCode;
        List<PackagingHelper.PackageDimension> packageDimensions = new ArrayList<>();
        // Map of packageId to Package entity
        Map<Long, com.example.SpringApi.Models.DatabaseModels.Package> packageEntities = new HashMap<>();
    }
    
    /**
     * Helper class to track remaining product quantities during shipment splitting
     */
    private static class ProductAllocationTracker {
        Long productId;
        ProductLocationInfo productInfo;
        ProductResponseModel productResponseModel;
        int remainingQty;
        BigDecimal weightPerUnit;
    }
    
    /**
     * Check if there are any remaining products to allocate
     */
    private boolean hasRemainingProducts(List<ProductAllocationTracker> trackers) {
        for (ProductAllocationTracker tracker : trackers) {
            if (tracker.remainingQty > 0) return true;
        }
        return false;
    }
    
    /**
     * Candidate allocation: which products and quantities from which location
     */
    private static class AllocationCandidate {
        // Map of locationId -> (Map of productId -> quantity)
        Map<Long, Map<Long, Integer>> locationProductQuantities = new LinkedHashMap<>();
        BigDecimal totalPackagingCost = BigDecimal.ZERO;
        BigDecimal totalShippingCost = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        List<OrderOptimizationResponseModel.Shipment> shipments = new ArrayList<>();
        boolean canFulfillOrder = true;
        int shortfall = 0;
        boolean allCouriersAvailable = true;
        String unavailabilityReason = null;
    }
    
    @Override
    public OrderOptimizationResponseModel optimizeOrder(OrderOptimizationRequestModel request) {
        long overallStartTime = System.currentTimeMillis();
        logger.info("Starting order optimization for " + (request.getProductQuantities() != null ? request.getProductQuantities().size() : 0) + 
                   " products, delivery postcode: " + request.getDeliveryPostcode());
        
        OrderOptimizationResponseModel response = new OrderOptimizationResponseModel();
        
        // Validate request
        if (request.getProductQuantities() == null || request.getProductQuantities().isEmpty()) {
            logger.warn("Order optimization failed: No products specified");
            response.setSuccess(false);
            response.setErrorMessage("No products specified");
            return response;
        }
        
        if (request.getDeliveryPostcode() == null || request.getDeliveryPostcode().isEmpty()) {
            logger.warn("Order optimization failed: Delivery postcode is required");
            response.setSuccess(false);
            response.setErrorMessage("Delivery postcode is required");
            return response;
        }
        
        try {
            // Step 1: Fetch all product data
            logger.info("Step 1: Fetching product data for " + request.getProductQuantities().size() + " products");
            long step1Start = System.currentTimeMillis();
            Map<Long, ProductLocationInfo> productInfoMap = fetchProductData(request.getProductQuantities());
            logger.info("Step 1 completed in " + (System.currentTimeMillis() - step1Start) + "ms, found " + productInfoMap.size() + " products");
            
            if (productInfoMap.isEmpty()) {
                logger.warn("Order optimization failed: No valid products found");
                response.setSuccess(false);
                response.setErrorMessage("No valid products found");
                return response;
            }
            
            // Step 2: Fetch stock and packaging info for all products at all locations
            logger.info("Step 2: Fetching location data (stock and packaging)");
            long step2Start = System.currentTimeMillis();
            Map<Long, LocationInfo> locationInfoMap = fetchLocationData(productInfoMap);
            logger.info("Step 2 completed in " + (System.currentTimeMillis() - step2Start) + "ms, found " + locationInfoMap.size() + " locations");
            
            List<AllocationCandidate> candidates;
            
            // Check if custom allocations are provided
            if (request.getCustomAllocations() != null && !request.getCustomAllocations().isEmpty()) {
                // Custom mode: Validate and use user-provided allocations strictly
                logger.info("Custom allocation mode: Validating user-specified allocations");
                CustomAllocationResult customResult = createCustomAllocationCandidate(
                    request.getCustomAllocations(), 
                    productInfoMap, 
                    locationInfoMap
                );
                
                // If validation failed, return error immediately
                if (!customResult.isValid) {
                    logger.warn("Custom allocation validation failed: " + customResult.errorMessage);
                    response.setSuccess(false);
                    response.setErrorMessage(customResult.errorMessage);
                    return response;
                }
                
                candidates = new ArrayList<>();
                candidates.add(customResult.candidate);
            } else {
                // Auto mode: Generate optimal candidates
                // Step 3: Build feasibility matrix and check if order can be fulfilled
                logger.info("Step 3: Checking feasibility");
                long step3Start = System.currentTimeMillis();
                String feasibilityError = checkFeasibility(productInfoMap, request.getProductQuantities(), locationInfoMap);
                logger.info("Step 3 completed in " + (System.currentTimeMillis() - step3Start) + "ms");
                if (feasibilityError != null) {
                    logger.warn("Order optimization failed: " + feasibilityError);
                    response.setSuccess(false);
                    response.setErrorMessage(feasibilityError);
                    return response;
                }
                
                // Step 4: Generate candidate allocation strategies
                logger.info("Step 4: Generating candidate allocation strategies");
                long step4Start = System.currentTimeMillis();
                candidates = generateCandidates(
                    productInfoMap, 
                    locationInfoMap, 
                    request.getProductQuantities()
                );
                logger.info("Step 4 completed in " + (System.currentTimeMillis() - step4Start) + "ms, generated " + candidates.size() + " candidates");
                
                if (candidates.isEmpty()) {
                    logger.warn("Order optimization failed: No valid allocation strategies found");
                    response.setSuccess(false);
                    response.setErrorMessage("No valid allocation strategies found");
                    return response;
                }
            }
            
            // Step 5: Evaluate each candidate (calculate packaging and shipping costs)
            logger.info("Step 5: Evaluating " + candidates.size() + " candidates (packaging and shipping costs)");
            long step5Start = System.currentTimeMillis();
            ShippingHelper shippingHelper = getShippingHelper();
            String deliveryPostcode = request.getDeliveryPostcode();
            boolean isCod = Boolean.TRUE.equals(request.getIsCod());
            boolean isCustomAllocation = request.getCustomAllocations() != null && !request.getCustomAllocations().isEmpty();
            
            evaluateCandidates(candidates, productInfoMap, locationInfoMap, 
                              shippingHelper, deliveryPostcode, isCod, isCustomAllocation);
            logger.info("Step 5 completed in " + (System.currentTimeMillis() - step5Start) + "ms");
            
            // Step 6: Filter out options with no couriers available, sort by cost
            // First, separate options with all couriers available from those without
            List<AllocationCandidate> validCandidates = candidates.stream()
                .filter(c -> c.allCouriersAvailable)
                .sorted(Comparator.comparing(c -> c.totalCost))
                .collect(Collectors.toList());
            
            List<AllocationCandidate> invalidCandidates = candidates.stream()
                .filter(c -> !c.allCouriersAvailable)
                .sorted(Comparator.comparing(c -> c.totalCost))
                .collect(Collectors.toList());
            
            // Set the cheapest valid option directly on the response
            if (!validCandidates.isEmpty()) {
                AllocationCandidate cheapestCandidate = validCandidates.get(0);
                
                response.setDescription(generateDescription(cheapestCandidate, locationInfoMap));
                response.setTotalCost(cheapestCandidate.totalCost);
                response.setTotalPackagingCost(cheapestCandidate.totalPackagingCost);
                response.setTotalShippingCost(cheapestCandidate.totalShippingCost);
                response.setShipmentCount(cheapestCandidate.shipments.size());
                response.setShipments(cheapestCandidate.shipments);
                response.setCanFulfillOrder(cheapestCandidate.canFulfillOrder);
                response.setShortfall(cheapestCandidate.shortfall);
                response.setAllCouriersAvailable(true);
            } else if (!invalidCandidates.isEmpty()) {
                // No valid options - show the best invalid option with explanation
                AllocationCandidate bestInvalidCandidate = invalidCandidates.get(0);
                
                response.setDescription(generateDescription(bestInvalidCandidate, locationInfoMap));
                response.setTotalCost(bestInvalidCandidate.totalCost);
                response.setTotalPackagingCost(bestInvalidCandidate.totalPackagingCost);
                response.setTotalShippingCost(bestInvalidCandidate.totalShippingCost);
                response.setShipmentCount(bestInvalidCandidate.shipments.size());
                response.setShipments(bestInvalidCandidate.shipments);
                response.setCanFulfillOrder(bestInvalidCandidate.canFulfillOrder);
                response.setShortfall(bestInvalidCandidate.shortfall);
                response.setAllCouriersAvailable(false);
                response.setUnavailabilityReason(bestInvalidCandidate.unavailabilityReason);
                response.setErrorMessage("No shipping options available for any fulfillment strategy. " +
                    "This may be due to weight limits or route restrictions.");
            }
            
            // Set metadata
            response.setTotalProductCount(request.getProductQuantities().size());
            response.setTotalQuantity(request.getProductQuantities().values().stream()
                .mapToInt(Integer::intValue).sum());
            response.setSuccess(true);
            
            long totalDuration = System.currentTimeMillis() - overallStartTime;
            logger.info("Order optimization completed successfully in " + totalDuration + "ms");
            
        } catch (Exception e) {
            long totalDuration = System.currentTimeMillis() - overallStartTime;
            logger.error("Order optimization failed after " + totalDuration + "ms: " + e.getMessage(), e);
            contextualLogger.error(e);
            response.setSuccess(false);
            response.setErrorMessage("Optimization failed: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Fetch product data for all requested products
     */
    private Map<Long, ProductLocationInfo> fetchProductData(Map<Long, Integer> productQuantities) {
        Map<Long, ProductLocationInfo> result = new HashMap<>();
        
        List<Product> products = productRepository.findAllById(productQuantities.keySet());
        
        for (Product product : products) {
            ProductLocationInfo info = new ProductLocationInfo();
            info.productEntity = product; // Store full entity for response model
            info.productId = product.getProductId();
            info.productTitle = product.getTitle();
            info.weightKgs = product.getWeightKgs() != null ? product.getWeightKgs() : BigDecimal.valueOf(0.5);
            info.length = product.getLength();
            info.breadth = product.getBreadth();
            info.height = product.getHeight();
            info.requestedQuantity = productQuantities.getOrDefault(product.getProductId(), 0);
            
            result.put(product.getProductId(), info);
        }
        
        return result;
    }
    
    /**
     * Fetch location data including stock and packaging for all products
     */
    private Map<Long, LocationInfo> fetchLocationData(Map<Long, ProductLocationInfo> productInfoMap) {
        Map<Long, LocationInfo> locationInfoMap = new HashMap<>();
        
        // Fetch all product-location mappings
        for (Long productId : productInfoMap.keySet()) {
            List<ProductPickupLocationMapping> mappings = 
                productPickupLocationMappingRepository.findByProductIdWithPickupLocationAndAddress(productId);
            
            ProductLocationInfo productInfo = productInfoMap.get(productId);
            
            for (ProductPickupLocationMapping mapping : mappings) {
                Long locationId = mapping.getPickupLocationId();
                
                // Add to location info map if not exists
                if (!locationInfoMap.containsKey(locationId)) {
                    LocationInfo locInfo = new LocationInfo();
                    locInfo.pickupLocationEntity = mapping.getPickupLocation(); // Store full entity
                    locInfo.pickupLocationId = locationId;
                    locInfo.locationName = mapping.getPickupLocation() != null ? 
                        mapping.getPickupLocation().getAddressNickName() : "Location " + locationId;
                    locInfo.postalCode = mapping.getPickupLocation() != null && 
                        mapping.getPickupLocation().getAddress() != null ?
                        mapping.getPickupLocation().getAddress().getPostalCode() : null;
                    locationInfoMap.put(locationId, locInfo);
                }
                
                // Add stock info for this product at this location
                LocationStock stock = new LocationStock();
                stock.availableStock = mapping.getAvailableStock() != null ? mapping.getAvailableStock() : 0;
                productInfo.stockByLocation.put(locationId, stock);
            }
        }
        
        // Fetch package info for all locations
        if (!locationInfoMap.isEmpty()) {
            List<Long> locationIds = new ArrayList<>(locationInfoMap.keySet());
            
            List<PackagePickupLocationMapping> packageMappings = 
                packagePickupLocationMappingRepository.findByPickupLocationIdsWithPackages(locationIds);
            
            Map<Long, Integer> packagesPerLocation = new HashMap<>();
            for (PackagePickupLocationMapping pm : packageMappings) {
                LocationInfo locInfo = locationInfoMap.get(pm.getPickupLocationId());
                if (locInfo != null && pm.getPackageEntity() != null) {
                    var pkg = pm.getPackageEntity();
                    // Store the package entity for creating response models
                    locInfo.packageEntities.put(pkg.getPackageId(), pkg);
                    locInfo.packageDimensions.add(new PackagingHelper.PackageDimension(
                        pkg.getPackageId(), pkg.getPackageName(), pkg.getPackageType(),
                        pkg.getLength(), pkg.getBreadth(), pkg.getHeight(),
                        pkg.getMaxWeight(), pkg.getPricePerUnit(), pm.getAvailableQuantity()
                    ));
                    packagesPerLocation.merge(pm.getPickupLocationId(), 1, Integer::sum);
                }
            }
        }
        
        // Calculate max items packable for each product at each location
        for (ProductLocationInfo productInfo : productInfoMap.values()) {
            for (Map.Entry<Long, LocationStock> entry : productInfo.stockByLocation.entrySet()) {
                Long locationId = entry.getKey();
                LocationStock stock = entry.getValue();
                LocationInfo locInfo = locationInfoMap.get(locationId);
                
                if (locInfo != null && !locInfo.packageDimensions.isEmpty()) {
                    stock.packageDimensions = locInfo.packageDimensions;
                    
                    // Check if product can fit in any package (ignoring available quantity for feasibility check)
                    // This determines if the product dimensions/weight are compatible with package types
                    boolean canFitInAnyPackageType = false;
                    double productVolume = productInfo.length != null && productInfo.breadth != null && productInfo.height != null
                        ? productInfo.length.doubleValue() * productInfo.breadth.doubleValue() * productInfo.height.doubleValue()
                        : 0;
                    double productWeight = productInfo.weightKgs != null ? productInfo.weightKgs.doubleValue() : 0;
                    
                    for (PackagingHelper.PackageDimension pkg : locInfo.packageDimensions) {
                        double pkgVolume = pkg.getVolume();
                        double pkgMaxWeight = pkg.getMaxWeight();
                        boolean fits = pkgVolume >= productVolume && pkgMaxWeight >= productWeight;
                        if (fits) {
                            canFitInAnyPackageType = true;
                            break;
                        }
                    }
                    
                    if (canFitInAnyPackageType || (productInfo.length == null && productInfo.breadth == null && productInfo.height == null)) {
                        // Product can fit in at least one package type - calculate actual packable quantity
                        PackagingHelper.ProductDimension productDim = new PackagingHelper.ProductDimension(
                            productInfo.length, productInfo.breadth, productInfo.height,
                            productInfo.weightKgs, stock.availableStock
                        );
                        PackagingHelper.PackagingEstimateResult estimate = 
                            packagingHelper.calculatePackaging(productDim, locInfo.packageDimensions);
                        stock.maxItemsPackable = estimate.getMaxItemsPackable();
                        
                        // If product fits but maxItemsPackable is 0, it's likely a quantity issue, not dimension issue
                        // Set maxItemsPackable to availableStock to indicate it CAN be packed if packages are available
                        if (stock.maxItemsPackable == 0 && canFitInAnyPackageType && stock.availableStock > 0) {
                            // Check if any package has quantity > 0
                            boolean hasPackageQuantity = locInfo.packageDimensions.stream()
                                .anyMatch(p -> p.getAvailableQuantity() > 0);
                            
                            if (!hasPackageQuantity) {
                                // Product fits but no packages available - allow feasibility check to pass
                                // but mark that packaging is limited by package availability
                                stock.maxItemsPackable = stock.availableStock; // Assume can pack if packages become available
                                stock.packagingErrorMessage = "Product fits in package types but no packages available (all have 0 quantity)";
                            } else {
                                // Packages available but still can't pack - store error message
                                if (estimate.getErrorMessage() != null) {
                                    stock.packagingErrorMessage = estimate.getErrorMessage();
                                }
                            }
                        } else if (estimate.getMaxItemsPackable() == 0 && estimate.getErrorMessage() != null) {
                            stock.packagingErrorMessage = estimate.getErrorMessage();
                        }
                    } else {
                        // Product cannot fit in any package type (dimensions/weight exceed all package limits)
                        stock.maxItemsPackable = 0;
                        stock.packagingErrorMessage = "Product dimensions/weight exceed all available package limits";
                    }
                } else {
                    stock.maxItemsPackable = 0;
                }
            }
        }
        
        return locationInfoMap;
    }
    
    
    /**
     * Check if the order can be fulfilled
     */
    private String checkFeasibility(Map<Long, ProductLocationInfo> productInfoMap, 
                                    Map<Long, Integer> productQuantities,
                                    Map<Long, LocationInfo> locationInfoMap) {
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Long productId = entry.getKey();
            int requestedQty = entry.getValue();
            
            ProductLocationInfo info = productInfoMap.get(productId);
            if (info == null) {
                return "Product ID " + productId + " not found";
            }
            
            // Calculate total available (considering packaging constraints)
            int totalStock = info.stockByLocation.values().stream()
                .mapToInt(s -> s.availableStock)
                .sum();
            
            int totalPackable = info.stockByLocation.values().stream()
                .mapToInt(s -> Math.min(s.availableStock, s.maxItemsPackable))
                .sum();
            
            // Check if any location has packages configured (check both stock.packageDimensions and locationInfoMap)
            boolean hasPackagesConfigured = info.stockByLocation.values().stream()
                .anyMatch(s -> s.packageDimensions != null && !s.packageDimensions.isEmpty());
            
            // Also check locationInfoMap directly in case packages weren't copied to stock
            if (!hasPackagesConfigured && locationInfoMap != null) {
                hasPackagesConfigured = info.stockByLocation.keySet().stream()
                    .anyMatch(locationId -> {
                        LocationInfo locInfo = locationInfoMap.get(locationId);
                        return locInfo != null && locInfo.packageDimensions != null && !locInfo.packageDimensions.isEmpty();
                    });
            }
            
            // Check if any package has available quantity
            boolean hasAvailablePackages = info.stockByLocation.values().stream()
                .anyMatch(s -> s.packageDimensions != null && 
                    s.packageDimensions.stream().anyMatch(p -> p.getAvailableQuantity() > 0));
            
            // Also check locationInfoMap for package availability
            if (!hasAvailablePackages && locationInfoMap != null) {
                hasAvailablePackages = info.stockByLocation.keySet().stream()
                    .anyMatch(locationId -> {
                        LocationInfo locInfo = locationInfoMap.get(locationId);
                        if (locInfo != null && locInfo.packageDimensions != null) {
                            return locInfo.packageDimensions.stream().anyMatch(p -> p.getAvailableQuantity() > 0);
                        }
                        return false;
                    });
            }
            
            // Check if product can fit in any package type (ignoring quantity)
            boolean canFitInAnyPackageType = false;
            if (hasPackagesConfigured) {
                double productVolume = info.length != null && info.breadth != null && info.height != null
                    ? info.length.doubleValue() * info.breadth.doubleValue() * info.height.doubleValue()
                    : 0;
                double productWeight = info.weightKgs != null ? info.weightKgs.doubleValue() : 0;
                
                // Check stock.packageDimensions first
                canFitInAnyPackageType = info.stockByLocation.values().stream()
                    .filter(s -> s.packageDimensions != null)
                    .flatMap(s -> s.packageDimensions.stream())
                    .anyMatch(pkg -> pkg.getVolume() >= productVolume && pkg.getMaxWeight() >= productWeight);
                
                // If not found, check locationInfoMap
                if (!canFitInAnyPackageType && locationInfoMap != null) {
                    canFitInAnyPackageType = info.stockByLocation.keySet().stream()
                        .anyMatch(locationId -> {
                            LocationInfo locInfo = locationInfoMap.get(locationId);
                            if (locInfo == null || locInfo.packageDimensions == null) return false;
                            return locInfo.packageDimensions.stream()
                                .anyMatch(pkg -> pkg.getVolume() >= productVolume && pkg.getMaxWeight() >= productWeight);
                        });
                }
                
                // If no dimensions, assume it fits
                if (info.length == null && info.breadth == null && info.height == null) {
                    canFitInAnyPackageType = true;
                }
            }
            
            // Get packaging error message if available
            String packagingError = info.stockByLocation.values().stream()
                .filter(s -> s.packagingErrorMessage != null)
                .map(s -> s.packagingErrorMessage)
                .findFirst()
                .orElse(null);
            
            if (totalPackable < requestedQty) {
                // Provide more detailed error message
                if (totalStock == 0) {
                    return "Insufficient stock for product '" + info.productTitle + 
                           "'. Requested: " + requestedQty + ", Available stock: 0";
                } else if (!hasPackagesConfigured) {
                    return "Product '" + info.productTitle + 
                           "' cannot be packaged. Stock available: " + totalStock + 
                           ", but no packages are configured at pickup locations. Requested: " + requestedQty;
                } else if (!hasAvailablePackages) {
                    return "Product '" + info.productTitle + 
                           "' cannot be packaged. Stock available: " + totalStock + 
                           ", but no packages are available at pickup locations (all packages have 0 quantity). Requested: " + requestedQty;
                } else if (!canFitInAnyPackageType) {
                    return "Product '" + info.productTitle + 
                           "' cannot be packaged. Stock available: " + totalStock + 
                           ", but product dimensions/weight exceed all available package limits. Requested: " + requestedQty;
                } else if (totalStock >= requestedQty && totalPackable == 0) {
                    // Product fits but can't be packed - likely quantity issue
                    String errorDetail = packagingError != null ? packagingError : 
                        "not enough packages available to pack the requested quantity";
                    return "Product '" + info.productTitle + 
                           "' cannot be packaged with available packages. Stock available: " + totalStock + 
                           ", but " + errorDetail + ". Requested: " + requestedQty;
                } else {
                    return "Insufficient stock/packaging for product '" + info.productTitle + 
                           "'. Requested: " + requestedQty + ", Available stock: " + totalStock + 
                           ", Packable (considering packaging constraints): " + totalPackable;
                }
            }
        }
        
        return null; // Feasible
    }
    
    /**
     * Generate candidate allocation strategies
     */
    private List<AllocationCandidate> generateCandidates(
            Map<Long, ProductLocationInfo> productInfoMap,
            Map<Long, LocationInfo> locationInfoMap,
            Map<Long, Integer> productQuantities) {
        
        List<AllocationCandidate> candidates = new ArrayList<>();
        
        // Strategy 1: Single-location candidates (if any location can fulfill all products)
        for (Long locationId : locationInfoMap.keySet()) {
            if (canLocationFulfillAll(locationId, productInfoMap, productQuantities, locationInfoMap)) {
                AllocationCandidate candidate = createSingleLocationCandidate(
                    locationId, productInfoMap, productQuantities);
                candidates.add(candidate);
            }
        }
        
        // Strategy 2: Greedy by consolidation (prioritize locations with most products)
        AllocationCandidate greedyConsolidation = createGreedyConsolidationCandidate(
            productInfoMap, locationInfoMap, productQuantities);
        if (greedyConsolidation.canFulfillOrder) {
            candidates.add(greedyConsolidation);
        }
        
        // Strategy 3: Greedy by stock availability (prioritize locations with most stock)
        AllocationCandidate greedyStock = createGreedyStockCandidate(
            productInfoMap, locationInfoMap, productQuantities);
        if (greedyStock.canFulfillOrder) {
            candidates.add(greedyStock);
        }
        
        // Remove duplicates based on allocation (same locationProductQuantities)
        candidates = removeDuplicateCandidates(candidates);
        
        return candidates;
    }
    
    /**
     * Check if a single location can fulfill all products.
     * Returns false if:
     * - Location doesn't have required stock
     * - Location can't package the products (no suitable packages)
     * - Location has no packages at all
     */
    private boolean canLocationFulfillAll(Long locationId, 
                                          Map<Long, ProductLocationInfo> productInfoMap,
                                          Map<Long, Integer> productQuantities,
                                          Map<Long, LocationInfo> locationInfoMap) {
        // Check if location has any packages at all
        LocationInfo locInfo = locationInfoMap.get(locationId);
        if (locInfo == null || locInfo.packageDimensions.isEmpty()) {
            return false; // No packages available at this location
        }
        
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Long productId = entry.getKey();
            int requestedQty = entry.getValue();
            
            ProductLocationInfo info = productInfoMap.get(productId);
            if (info == null) return false;
            
            LocationStock stock = info.stockByLocation.get(locationId);
            if (stock == null) return false;
            
            // maxItemsPackable will be 0 if product can't fit in any package
            int available = Math.min(stock.availableStock, stock.maxItemsPackable);
            if (available < requestedQty) return false;
        }
        return true;
    }
    
    /**
     * Create a single-location allocation candidate
     */
    private AllocationCandidate createSingleLocationCandidate(Long locationId,
                                                               Map<Long, ProductLocationInfo> productInfoMap,
                                                               Map<Long, Integer> productQuantities) {
        AllocationCandidate candidate = new AllocationCandidate();
        Map<Long, Integer> productQtys = new HashMap<>();
        
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            productQtys.put(entry.getKey(), entry.getValue());
        }
        
        candidate.locationProductQuantities.put(locationId, productQtys);
        candidate.canFulfillOrder = true;
        
        return candidate;
    }
    
    /**
     * Result of custom allocation validation
     */
    private static class CustomAllocationResult {
        AllocationCandidate candidate;
        String errorMessage;
        boolean isValid;
        
        static CustomAllocationResult success(AllocationCandidate candidate) {
            CustomAllocationResult result = new CustomAllocationResult();
            result.candidate = candidate;
            result.isValid = true;
            return result;
        }
        
        static CustomAllocationResult error(String message) {
            CustomAllocationResult result = new CustomAllocationResult();
            result.errorMessage = message;
            result.isValid = false;
            return result;
        }
    }
    
    /**
     * Create and validate allocation candidate from user-provided custom allocations.
     * Strictly validates each product-location-quantity mapping.
     * 
     * @param customAllocations Map of productId -> (pickupLocationId -> quantity)
     * @param productInfoMap Product information map
     * @param locationInfoMap Location information map
     * @return CustomAllocationResult with candidate or error message
     */
    private CustomAllocationResult createCustomAllocationCandidate(
            Map<Long, Map<Long, Integer>> customAllocations,
            Map<Long, ProductLocationInfo> productInfoMap,
            Map<Long, LocationInfo> locationInfoMap) {
        
        AllocationCandidate candidate = new AllocationCandidate();
        List<String> errors = new ArrayList<>();
        
        // Validate and transform from productId -> (locationId -> qty) 
        // to locationId -> (productId -> qty)
        for (Map.Entry<Long, Map<Long, Integer>> productEntry : customAllocations.entrySet()) {
            Long productId = productEntry.getKey();
            Map<Long, Integer> locationQtys = productEntry.getValue();
            
            ProductLocationInfo productInfo = productInfoMap.get(productId);
            if (productInfo == null) {
                errors.add("Product ID " + productId + " not found");
                continue;
            }
            
            for (Map.Entry<Long, Integer> locEntry : locationQtys.entrySet()) {
                Long locationId = locEntry.getKey();
                Integer qty = locEntry.getValue();
                
                if (qty == null || qty <= 0) continue;
                
                // Validate location exists
                LocationInfo locInfo = locationInfoMap.get(locationId);
                if (locInfo == null) {
                    errors.add("Product '" + productInfo.productTitle + "': Location ID " + locationId + " not found");
                    continue;
                }
                
                // Validate product has stock at this specific location
                LocationStock stock = productInfo.stockByLocation.get(locationId);
                if (stock == null) {
                    errors.add("Product '" + productInfo.productTitle + "': Not available at location '" + 
                              locInfo.locationName + "' (no stock mapping exists)");
                    continue;
                }
                
                // Validate sufficient stock
                if (stock.availableStock < qty) {
                    errors.add("Product '" + productInfo.productTitle + "': Insufficient stock at '" + 
                              locInfo.locationName + "'. Requested: " + qty + ", Available: " + stock.availableStock);
                    continue;
                }
                
                // Validate packaging available at this location
                if (locInfo.packageDimensions.isEmpty()) {
                    errors.add("Product '" + productInfo.productTitle + "': No packages available at '" + 
                              locInfo.locationName + "'");
                    continue;
                }
                
                // Validate product can be packaged at this location
                int packable = Math.min(stock.availableStock, stock.maxItemsPackable);
                if (packable < qty) {
                    errors.add("Product '" + productInfo.productTitle + "': Cannot package " + qty + 
                              " units at '" + locInfo.locationName + "'. Max packable: " + packable +
                              (stock.packagingErrorMessage != null ? " (" + stock.packagingErrorMessage + ")" : ""));
                    continue;
                }
                
                // Validation passed - add to candidate
                candidate.locationProductQuantities
                    .computeIfAbsent(locationId, k -> new HashMap<>())
                    .put(productId, qty);
            }
        }
        
        // If there are validation errors, return them
        if (!errors.isEmpty()) {
            return CustomAllocationResult.error("Custom allocation validation failed:\n• " + 
                String.join("\n• ", errors));
        }
        
        // Check that at least one allocation was made
        if (candidate.locationProductQuantities.isEmpty()) {
            return CustomAllocationResult.error("No valid allocations specified");
        }
        
        candidate.canFulfillOrder = true;
        candidate.shortfall = 0;
        
        logger.info("Custom allocation validated: " + candidate.locationProductQuantities.size() + 
                   " locations, " + candidate.locationProductQuantities.values().stream()
                       .mapToInt(Map::size).sum() + " product allocations");
        
        return CustomAllocationResult.success(candidate);
    }
    
    /**
     * Create greedy consolidation candidate (prioritize locations with most products)
     */
    private AllocationCandidate createGreedyConsolidationCandidate(
            Map<Long, ProductLocationInfo> productInfoMap,
            Map<Long, LocationInfo> locationInfoMap,
            Map<Long, Integer> productQuantities) {
        
        AllocationCandidate candidate = new AllocationCandidate();
        Map<Long, Integer> remainingQuantities = new HashMap<>(productQuantities);
        
        // Score locations by how many products they have
        // Filter out locations with no packages
        List<Long> sortedLocations = locationInfoMap.keySet().stream()
            .filter(locId -> {
                LocationInfo locInfo = locationInfoMap.get(locId);
                return locInfo != null && !locInfo.packageDimensions.isEmpty();
            })
            .sorted((a, b) -> {
                long countA = productInfoMap.values().stream()
                    .filter(p -> {
                        LocationStock s = p.stockByLocation.get(a);
                        return s != null && Math.min(s.availableStock, s.maxItemsPackable) > 0;
                    })
                    .count();
                long countB = productInfoMap.values().stream()
                    .filter(p -> {
                        LocationStock s = p.stockByLocation.get(b);
                        return s != null && Math.min(s.availableStock, s.maxItemsPackable) > 0;
                    })
                    .count();
                return Long.compare(countB, countA); // Descending
            })
            .collect(Collectors.toList());
        
        // Allocate from each location
        for (Long locationId : sortedLocations) {
            Map<Long, Integer> locationAlloc = new HashMap<>();
            
            for (Map.Entry<Long, Integer> entry : remainingQuantities.entrySet()) {
                Long productId = entry.getKey();
                int remaining = entry.getValue();
                if (remaining <= 0) continue;
                
                ProductLocationInfo info = productInfoMap.get(productId);
                if (info == null) continue;
                
                LocationStock stock = info.stockByLocation.get(locationId);
                if (stock == null) continue;
                
                int available = Math.min(stock.availableStock, stock.maxItemsPackable);
                int toAllocate = Math.min(remaining, available);
                
                if (toAllocate > 0) {
                    locationAlloc.put(productId, toAllocate);
                    remainingQuantities.put(productId, remaining - toAllocate);
                }
            }
            
            if (!locationAlloc.isEmpty()) {
                candidate.locationProductQuantities.put(locationId, locationAlloc);
            }
        }
        
        // Check if fully fulfilled
        int totalRemaining = remainingQuantities.values().stream().mapToInt(Integer::intValue).sum();
        candidate.canFulfillOrder = totalRemaining == 0;
        candidate.shortfall = totalRemaining;
        
        return candidate;
    }
    
    /**
     * Create greedy stock candidate (prioritize locations with most stock for each product)
     */
    private AllocationCandidate createGreedyStockCandidate(
            Map<Long, ProductLocationInfo> productInfoMap,
            Map<Long, LocationInfo> locationInfoMap,
            Map<Long, Integer> productQuantities) {
        
        AllocationCandidate candidate = new AllocationCandidate();
        Map<Long, Integer> remainingQuantities = new HashMap<>(productQuantities);
        
        // For each product, allocate from locations with most stock
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Long productId = entry.getKey();
            int remaining = entry.getValue();
            
            ProductLocationInfo info = productInfoMap.get(productId);
            if (info == null) continue;
            
            // Sort locations by stock for this product
            // Filter out locations with no packages
            List<Long> sortedLocs = info.stockByLocation.entrySet().stream()
                .filter(e -> {
                    LocationInfo locInfo = locationInfoMap.get(e.getKey());
                    return locInfo != null && !locInfo.packageDimensions.isEmpty();
                })
                .sorted((a, b) -> {
                    int availA = Math.min(a.getValue().availableStock, a.getValue().maxItemsPackable);
                    int availB = Math.min(b.getValue().availableStock, b.getValue().maxItemsPackable);
                    return Integer.compare(availB, availA);
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
            
            for (Long locationId : sortedLocs) {
                if (remaining <= 0) break;
                
                LocationStock stock = info.stockByLocation.get(locationId);
                int available = Math.min(stock.availableStock, stock.maxItemsPackable);
                int toAllocate = Math.min(remaining, available);
                
                if (toAllocate > 0) {
                    candidate.locationProductQuantities
                        .computeIfAbsent(locationId, k -> new HashMap<>())
                        .put(productId, toAllocate);
                    remaining -= toAllocate;
                }
            }
            
            remainingQuantities.put(productId, remaining);
        }
        
        int totalRemaining = remainingQuantities.values().stream().mapToInt(Integer::intValue).sum();
        candidate.canFulfillOrder = totalRemaining == 0;
        candidate.shortfall = totalRemaining;
        
        return candidate;
    }
    
    /**
     * Remove duplicate candidates
     */
    private List<AllocationCandidate> removeDuplicateCandidates(List<AllocationCandidate> candidates) {
        Set<String> seen = new HashSet<>();
        List<AllocationCandidate> unique = new ArrayList<>();
        
        for (AllocationCandidate c : candidates) {
            String key = c.locationProductQuantities.toString();
            if (!seen.contains(key)) {
                seen.add(key);
                unique.add(c);
            }
        }
        
        return unique;
    }
    
    /**
     * Reallocate products from unserviceable locations to serviceable ones.
     * This is called after route availability is determined to move products
     * from locations that can't ship to the delivery postcode to locations that can.
     * 
     * @param candidate The allocation candidate to modify
     * @param productInfoMap Product information including stock at each location
     * @param locationInfoMap Location information including packages
     * @param serviceableLocationIds Set of location IDs that can ship to delivery postcode
     * @param unserviceableLocationIds Set of location IDs that cannot ship
     */
    private void reallocateFromUnserviceableLocations(
            AllocationCandidate candidate,
            Map<Long, ProductLocationInfo> productInfoMap,
            Map<Long, LocationInfo> locationInfoMap,
            Set<Long> serviceableLocationIds,
            Set<Long> unserviceableLocationIds) {
        
        // Track products that need reallocation from unserviceable locations
        Map<Long, Integer> productsToReallocate = new HashMap<>();
        List<Long> locationsToRemove = new ArrayList<>();
        
        // Find all products allocated to unserviceable locations
        for (Map.Entry<Long, Map<Long, Integer>> locEntry : 
             candidate.locationProductQuantities.entrySet()) {
            
            Long locationId = locEntry.getKey();
            if (unserviceableLocationIds.contains(locationId)) {
                // This location can't ship - need to reallocate its products
                Map<Long, Integer> productQtys = locEntry.getValue();
                for (Map.Entry<Long, Integer> prodEntry : productQtys.entrySet()) {
                    productsToReallocate.merge(prodEntry.getKey(), prodEntry.getValue(), Integer::sum);
                }
                locationsToRemove.add(locationId);
                
                LocationInfo locInfo = locationInfoMap.get(locationId);
                String locationName = locInfo != null ? locInfo.locationName : "Location " + locationId;
                logger.info("Need to reallocate " + productQtys.size() + " products from unserviceable location: " + locationName);
            }
        }
        
        if (productsToReallocate.isEmpty()) {
            return; // Nothing to reallocate
        }
        
        // Remove unserviceable locations from candidate
        for (Long locationId : locationsToRemove) {
            candidate.locationProductQuantities.remove(locationId);
        }
        
        // Track what we've already allocated from each serviceable location
        // to avoid over-allocating from existing allocations
        Map<Long, Map<Long, Integer>> existingAllocations = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Integer>> locEntry : 
             candidate.locationProductQuantities.entrySet()) {
            existingAllocations.put(locEntry.getKey(), new HashMap<>(locEntry.getValue()));
        }
        
        // Try to reallocate each product to serviceable locations
        for (Map.Entry<Long, Integer> prodEntry : productsToReallocate.entrySet()) {
            Long productId = prodEntry.getKey();
            int qtyToReallocate = prodEntry.getValue();
            
            ProductLocationInfo productInfo = productInfoMap.get(productId);
            if (productInfo == null) continue;
            
            // Sort serviceable locations by available stock for this product (descending)
            List<Long> sortedServiceableLocations = serviceableLocationIds.stream()
                .filter(locId -> {
                    // Must have stock and packages
                    LocationStock stock = productInfo.stockByLocation.get(locId);
                    LocationInfo locInfo = locationInfoMap.get(locId);
                    return stock != null && 
                           Math.min(stock.availableStock, stock.maxItemsPackable) > 0 &&
                           locInfo != null && 
                           !locInfo.packageDimensions.isEmpty();
                })
                .sorted((a, b) -> {
                    LocationStock stockA = productInfo.stockByLocation.get(a);
                    LocationStock stockB = productInfo.stockByLocation.get(b);
                    int availA = Math.min(stockA.availableStock, stockA.maxItemsPackable);
                    int availB = Math.min(stockB.availableStock, stockB.maxItemsPackable);
                    return Integer.compare(availB, availA); // Descending
                })
                .collect(Collectors.toList());
            
            logger.debug("Product " + productId + " (" + productInfo.productTitle + 
                        "): need to reallocate " + qtyToReallocate + 
                        " units, " + sortedServiceableLocations.size() + " serviceable locations available");
            
            // Allocate from serviceable locations
            for (Long locationId : sortedServiceableLocations) {
                if (qtyToReallocate <= 0) break;
                
                LocationStock stock = productInfo.stockByLocation.get(locationId);
                int totalAvailable = Math.min(stock.availableStock, stock.maxItemsPackable);
                
                // Subtract already allocated quantity from this location
                int alreadyAllocated = existingAllocations
                    .getOrDefault(locationId, Collections.emptyMap())
                    .getOrDefault(productId, 0);
                int remainingAvailable = totalAvailable - alreadyAllocated;
                
                if (remainingAvailable <= 0) continue;
                
                int toAllocate = Math.min(qtyToReallocate, remainingAvailable);
                
                // Add to candidate's allocations
                candidate.locationProductQuantities
                    .computeIfAbsent(locationId, k -> new HashMap<>())
                    .merge(productId, toAllocate, Integer::sum);
                
                // Track what we've now allocated
                existingAllocations
                    .computeIfAbsent(locationId, k -> new HashMap<>())
                    .merge(productId, toAllocate, Integer::sum);
                
                qtyToReallocate -= toAllocate;
                
                LocationInfo locInfo = locationInfoMap.get(locationId);
                String locationName = locInfo != null ? locInfo.locationName : "Location " + locationId;
                logger.info("Reallocated " + toAllocate + " units of product " + productId + 
                           " (" + productInfo.productTitle + ") to " + locationName);
            }
            
            // Check if we couldn't fully reallocate
            if (qtyToReallocate > 0) {
                logger.warn("Could not fully reallocate product " + productId + 
                           " (" + productInfo.productTitle + "): " + qtyToReallocate + 
                           " units have no serviceable location with available stock");
                candidate.canFulfillOrder = false;
                candidate.shortfall += qtyToReallocate;
            }
        }
    }
    
    /**
     * Evaluate candidates by calculating packaging and shipping costs.
     * Splits heavy shipments into smaller ones to meet courier weight limits.
     * Dynamically determines max weight per route by testing from 500kg down to 100kg.
     * 
     * @param candidates List of allocation candidates to evaluate
     * @param productInfoMap Product information map
     * @param locationInfoMap Location information map
     * @param shippingHelper Helper for shipping API calls
     * @param deliveryPostcode Delivery destination postcode
     * @param isCod Whether order is Cash on Delivery
     * @param isCustomAllocation If true, uses exact locations specified without reallocation
     */
    private void evaluateCandidates(
            List<AllocationCandidate> candidates,
            Map<Long, ProductLocationInfo> productInfoMap,
            Map<Long, LocationInfo> locationInfoMap,
            ShippingHelper shippingHelper,
            String deliveryPostcode,
            boolean isCod,
            boolean isCustomAllocation) {
        
        // Pre-fetch authentication token once before parallel operations
        // This ensures we have a valid token cached before multiple threads try to use it
        try {
            logger.debug("Pre-fetching authentication token before parallel operations");
            shippingHelper.getToken(); // This will cache the token for reuse
        } catch (Exception e) {
            logger.warn("Failed to pre-fetch token, will retry per-request: " + e.getMessage());
            // Continue anyway - each request will try to get token
        }
        
        // Pre-fetch: Find max weight for each unique pickup-delivery route
        // This tests 500kg, 400kg, 300kg, 200kg, 100kg until couriers are found
        Map<String, BigDecimal> routeMaxWeights = new ConcurrentHashMap<>();
        Set<String> uniquePickupPostcodes = new HashSet<>();
        
        // Collect pickup postcodes ONLY from locations actually used in candidates
        // This is especially important for custom allocation where user selects specific locations
        Set<Long> usedLocationIds = new HashSet<>();
        for (AllocationCandidate candidate : candidates) {
            usedLocationIds.addAll(candidate.locationProductQuantities.keySet());
        }
        
        for (Long locationId : usedLocationIds) {
            LocationInfo locInfo = locationInfoMap.get(locationId);
            if (locInfo != null && locInfo.postalCode != null) {
                uniquePickupPostcodes.add(locInfo.postalCode);
            }
        }
        
        // Fetch max weight for each route in parallel
        logger.info("Starting max weight lookup for " + uniquePickupPostcodes.size() + " unique pickup postcodes");
        List<CompletableFuture<Void>> maxWeightFutures = new ArrayList<>();
        for (String pickupPostcode : uniquePickupPostcodes) {
            final String postcode = pickupPostcode;
            maxWeightFutures.add(CompletableFuture.runAsync(() -> {
                try {
                    logger.debug("Fetching max weight for route: " + postcode + " -> " + deliveryPostcode);
                    long startTime = System.currentTimeMillis();
                    double maxWeight = findMaxWeightForRoute(shippingHelper, postcode, deliveryPostcode, isCod);
                    long duration = System.currentTimeMillis() - startTime;
                    logger.debug("Max weight lookup completed for " + postcode + " in " + duration + "ms: " + maxWeight + " kg");
                    if (maxWeight > 0) {
                        routeMaxWeights.put(postcode, BigDecimal.valueOf(maxWeight));
                    } else {
                        // No couriers available - use 0 to indicate this route is not serviceable
                        routeMaxWeights.put(postcode, BigDecimal.ZERO);
                        logger.warn("No couriers available for route: " + postcode + " -> " + deliveryPostcode);
                    }
                } catch (Exception e) {
                    // Log as WARN since this is an expected failure (timeout/network issues)
                    // The process will continue with default max weight
                    String errorMsg = e.getMessage();
                    if (errorMsg != null && (errorMsg.contains("timeout") || errorMsg.contains("timed out") || errorMsg.contains("connect"))) {
                        logger.warn("Max weight lookup timed out for route " + postcode + " -> " + deliveryPostcode + " (using default max weight): " + errorMsg);
                    } else {
                        logger.warn("Error fetching max weight for route " + postcode + " -> " + deliveryPostcode + " (using default max weight): " + errorMsg);
                    }
                    // Don't log to contextualLogger for expected failures - these are handled gracefully
                    routeMaxWeights.put(postcode, MAX_WEIGHT_PER_SHIPMENT);
                }
            }));
        }
        
        // Wait for all max weight calls to complete with timeout
        try {
            logger.info("Waiting for max weight lookups to complete (timeout: " + SHIPPING_API_TIMEOUT_SECONDS + "s)");
            long startTime = System.currentTimeMillis();
            CompletableFuture<Void> allMaxWeights = CompletableFuture.allOf(maxWeightFutures.toArray(new CompletableFuture[0]));
            allMaxWeights.get(SHIPPING_API_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            long duration = System.currentTimeMillis() - startTime;
            logger.info("All max weight lookups completed in " + duration + "ms");
        } catch (TimeoutException e) {
            // Log as WARN since this is an expected failure - we continue with available results
            logger.warn("Timeout waiting for max weight lookups after " + SHIPPING_API_TIMEOUT_SECONDS + " seconds (continuing with available results)");
            // Continue with available results - use default max weight for missing routes
            for (String postcode : uniquePickupPostcodes) {
                if (!routeMaxWeights.containsKey(postcode)) {
                    routeMaxWeights.put(postcode, MAX_WEIGHT_PER_SHIPMENT);
                    logger.debug("Using default max weight for route " + postcode + " due to timeout");
                }
            }
        } catch (Exception e) {
            // Log as WARN since this is an expected failure - we continue with available results
            logger.warn("Error waiting for max weight lookups (continuing with available results): " + e.getMessage());
            // Continue with available results
            for (String postcode : uniquePickupPostcodes) {
                if (!routeMaxWeights.containsKey(postcode)) {
                    routeMaxWeights.put(postcode, MAX_WEIGHT_PER_SHIPMENT);
                }
            }
        }
        
        // Build set of serviceable location IDs (only for locations actually used in candidates)
        Set<Long> serviceableLocationIds = new HashSet<>();
        Set<Long> unserviceableLocationIds = new HashSet<>();
        for (Long locationId : usedLocationIds) {
            LocationInfo locInfo = locationInfoMap.get(locationId);
            if (locInfo == null) continue;
            
            String pickupPostcode = locInfo.postalCode;
            BigDecimal routeMaxWeight = pickupPostcode != null ? 
                routeMaxWeights.getOrDefault(pickupPostcode, MAX_WEIGHT_PER_SHIPMENT) : 
                MAX_WEIGHT_PER_SHIPMENT;
            
            if (routeMaxWeight.compareTo(BigDecimal.ZERO) > 0) {
                serviceableLocationIds.add(locationId);
            } else {
                unserviceableLocationIds.add(locationId);
                logger.info("Location " + locInfo.locationName + " (" + pickupPostcode + 
                           ") is unserviceable for delivery to " + deliveryPostcode);
            }
        }
        
        logger.info("Serviceable locations: " + serviceableLocationIds.size() + 
                   ", Unserviceable locations: " + unserviceableLocationIds.size());
        
        // For custom allocation, do NOT reallocate - use exactly what the user specified
        // For auto allocation, try to reallocate products from unserviceable locations to serviceable ones
        if (!isCustomAllocation) {
            for (AllocationCandidate candidate : candidates) {
                reallocateFromUnserviceableLocations(candidate, productInfoMap, locationInfoMap, 
                                                      serviceableLocationIds, unserviceableLocationIds);
            }
        } else {
            logger.info("Custom allocation mode - using exact locations specified by user (no reallocation)");
        }
        
        // First pass: Build all shipments with packaging, then determine weight splits
        for (AllocationCandidate candidate : candidates) {
            List<OrderOptimizationResponseModel.Shipment> allShipments = new ArrayList<>();
            boolean hasUnserviceableRoute = false;
            StringBuilder routeErrors = new StringBuilder();
            
            for (Map.Entry<Long, Map<Long, Integer>> locEntry : 
                 candidate.locationProductQuantities.entrySet()) {
                
                Long locationId = locEntry.getKey();
                Map<Long, Integer> productQtys = locEntry.getValue();
                LocationInfo locInfo = locationInfoMap.get(locationId);
                
                if (locInfo == null) continue;
                
                // Get the max weight for this specific route
                String pickupPostcode = locInfo.postalCode;
                BigDecimal routeMaxWeight = pickupPostcode != null ? 
                    routeMaxWeights.getOrDefault(pickupPostcode, MAX_WEIGHT_PER_SHIPMENT) : 
                    MAX_WEIGHT_PER_SHIPMENT;
                
                // Check if this route is not serviceable (no couriers even at 100kg)
                // After reallocation, this should only happen if product couldn't be moved
                if (routeMaxWeight.compareTo(BigDecimal.ZERO) == 0) {
                    hasUnserviceableRoute = true;
                    String locationName = locInfo.locationName != null ? 
                        locInfo.locationName : "Unknown";
                    if (routeErrors.length() > 0) routeErrors.append("; ");
                    routeErrors.append("No courier options available between pickup location ")
                        .append(locationName).append(" [").append(pickupPostcode).append("]")
                        .append(" and delivery postcode [").append(deliveryPostcode).append("]")
                        .append(" (no alternative locations available)");
                    continue; // Skip this location
                }
                
                // Build product allocations and calculate total weight
                List<OrderOptimizationResponseModel.ProductAllocation> productAllocations = new ArrayList<>();
                BigDecimal locationWeight = BigDecimal.ZERO;
                int locationQty = 0;
                
                for (Map.Entry<Long, Integer> prodEntry : productQtys.entrySet()) {
                    Long productId = prodEntry.getKey();
                    int qty = prodEntry.getValue();
                    
                    ProductLocationInfo productInfo = productInfoMap.get(productId);
                    if (productInfo == null) continue;
                    
                    OrderOptimizationResponseModel.ProductAllocation prodAlloc = 
                        new OrderOptimizationResponseModel.ProductAllocation();
                    
                    if (productInfo.productEntity != null) {
                        prodAlloc.setProduct(new ProductResponseModel(productInfo.productEntity));
                    }
                    prodAlloc.setAllocatedQuantity(qty);
                    prodAlloc.setTotalWeight(productInfo.weightKgs.multiply(BigDecimal.valueOf(qty)));
                    
                    productAllocations.add(prodAlloc);
                    locationWeight = locationWeight.add(prodAlloc.getTotalWeight());
                    locationQty += qty;
                }
                
                // Calculate packaging for all products at this location
                List<OrderOptimizationResponseModel.PackageUsage> packageUsages = 
                    calculatePackagingWithDetails(productQtys, productInfoMap, locInfo);
                
                // Split into multiple shipments if weight exceeds route-specific limit
                List<OrderOptimizationResponseModel.Shipment> locationShipments = 
                    splitIntoShipments(locInfo, productAllocations, packageUsages, 
                                       locationWeight, locationQty, productInfoMap, routeMaxWeight);
                
                allShipments.addAll(locationShipments);
            }
            
            candidate.shipments = allShipments;
            
            // Mark candidate as having issues if any route is unserviceable
            if (hasUnserviceableRoute) {
                candidate.allCouriersAvailable = false;
                candidate.unavailabilityReason = routeErrors.toString();
            }
        }
        
        // Second pass: Fetch shipping rates for all shipments in parallel
        logger.info("Starting shipping rate lookup for " + candidates.size() + " candidates");
        Map<String, CompletableFuture<ShippingOptionsResponseModel>> shippingFutures = 
            new ConcurrentHashMap<>();
        
        int totalShipments = 0;
        for (AllocationCandidate candidate : candidates) {
            for (OrderOptimizationResponseModel.Shipment shipment : candidate.shipments) {
                if (shipment.getPickupLocation() == null || 
                    shipment.getPickupLocation().getAddress() == null) continue;
                
                String pickupPostcode = shipment.getPickupLocation().getAddress().getPostalCode();
                if (pickupPostcode == null) continue;
                
                BigDecimal weight = shipment.getTotalWeightKgs().max(BigDecimal.valueOf(0.5));
                String cacheKey = pickupPostcode + "-" + deliveryPostcode + "-" + 
                    weight.setScale(2, RoundingMode.HALF_UP).toString();
                
                if (!shippingFutures.containsKey(cacheKey)) {
                    final String finalPickupPostcode = pickupPostcode;
                    final String finalWeight = weight.toString();
                    totalShipments++;
                    
                    shippingFutures.put(cacheKey, CompletableFuture.supplyAsync(() -> {
                        try {
                            logger.debug("Fetching shipping options: " + finalPickupPostcode + " -> " + deliveryPostcode + " (" + finalWeight + " kg)");
                            long startTime = System.currentTimeMillis();
                            ShippingOptionsResponseModel result = shippingHelper.getAvailableShippingOptions(
                                finalPickupPostcode, deliveryPostcode, isCod, finalWeight);
                            long duration = System.currentTimeMillis() - startTime;
                            logger.debug("Shipping options fetched in " + duration + "ms for " + finalPickupPostcode + " -> " + deliveryPostcode);
                            return result;
                        } catch (Exception e) {
                            // Log as WARN since this is an expected failure (timeout/network issues)
                            // The process will continue with available results
                            String errorMsg = e.getMessage();
                            if (errorMsg != null && (errorMsg.contains("timeout") || errorMsg.contains("timed out") || errorMsg.contains("connect"))) {
                                logger.warn("Shipping options lookup timed out for " + finalPickupPostcode + " -> " + deliveryPostcode + " (will continue with available results): " + errorMsg);
                            } else {
                                logger.warn("Error fetching shipping options for " + finalPickupPostcode + " -> " + deliveryPostcode + " (will continue with available results): " + errorMsg);
                            }
                            // Don't log to contextualLogger for expected failures - these are handled gracefully
                            return null;
                        }
                    }));
                }
            }
        }
        
        logger.info("Waiting for " + shippingFutures.size() + " unique shipping rate lookups to complete (timeout: " + SHIPPING_API_TIMEOUT_SECONDS + "s)");
        
        // Wait for all shipping calls to complete with timeout
        try {
            long startTime = System.currentTimeMillis();
            CompletableFuture<Void> allShipping = CompletableFuture.allOf(
                shippingFutures.values().toArray(new CompletableFuture[0]));
            allShipping.get(SHIPPING_API_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            long duration = System.currentTimeMillis() - startTime;
            logger.info("All shipping rate lookups completed in " + duration + "ms");
        } catch (TimeoutException e) {
            // Log as WARN since this is an expected failure - we continue with available results
            logger.warn("Timeout waiting for shipping rate lookups after " + SHIPPING_API_TIMEOUT_SECONDS + " seconds (continuing with available results)");
            // Continue with available results - mark missing ones as null
        } catch (Exception e) {
            // Log as WARN since this is an expected failure - we continue with available results
            logger.warn("Error waiting for shipping rate lookups (continuing with available results): " + e.getMessage());
        }
        
        // Build shipping results map
        Map<String, ShippingOptionsResponseModel> shippingResults = new HashMap<>();
        int successfulLookups = 0;
        int failedLookups = 0;
        for (Map.Entry<String, CompletableFuture<ShippingOptionsResponseModel>> entry : 
             shippingFutures.entrySet()) {
            try {
                ShippingOptionsResponseModel result = entry.getValue().get(1, TimeUnit.SECONDS); // Quick timeout for individual get
                if (result != null) {
                    shippingResults.put(entry.getKey(), result);
                    successfulLookups++;
                } else {
                    failedLookups++;
                }
            } catch (TimeoutException e) {
                logger.warn("Individual shipping lookup timeout for key: " + entry.getKey());
                failedLookups++;
            } catch (Exception e) {
                logger.warn("Error getting shipping result for key " + entry.getKey() + ": " + e.getMessage());
                failedLookups++;
            }
        }
        logger.info("Shipping rate lookup summary: " + successfulLookups + " successful, " + failedLookups + " failed/timeout");
        
        // Third pass: Apply shipping rates and calculate totals
        // Filter out shipments with no packages (they cannot be fulfilled)
        for (AllocationCandidate candidate : candidates) {
            BigDecimal totalPackagingCost = BigDecimal.ZERO;
            BigDecimal totalShippingCost = BigDecimal.ZERO;
            boolean allCouriersAvailable = true;
            StringBuilder unavailabilityReasons = new StringBuilder();
            
            // Filter out shipments with no packages - these are invalid
            List<OrderOptimizationResponseModel.Shipment> validShipments = new ArrayList<>();
            
            for (OrderOptimizationResponseModel.Shipment shipment : candidate.shipments) {
                // Check for 0 packages - this means packaging failed at this location
                // Skip this shipment entirely - don't include it in the response
                if (shipment.getPackagesUsed() == null || shipment.getPackagesUsed().isEmpty()) {
                    String locationName = shipment.getPickupLocation() != null ? 
                        shipment.getPickupLocation().getAddressNickName() : "Unknown";
                    if (unavailabilityReasons.length() > 0) {
                        unavailabilityReasons.append("; ");
                    }
                    unavailabilityReasons.append("No packages available at ").append(locationName)
                        .append(" to fit products (skipped)");
                    // Don't add this shipment to validShipments
                    continue;
                }
                
                // Add packaging cost
                totalPackagingCost = totalPackagingCost.add(shipment.getPackagingCost());
                
                // Get shipping options
                String pickupPostcode = shipment.getPickupLocation() != null && 
                    shipment.getPickupLocation().getAddress() != null ?
                    shipment.getPickupLocation().getAddress().getPostalCode() : null;
                
                if (pickupPostcode != null) {
                    BigDecimal weight = shipment.getTotalWeightKgs().max(BigDecimal.valueOf(0.5));
                    String cacheKey = pickupPostcode + "-" + deliveryPostcode + "-" + 
                        weight.setScale(2, RoundingMode.HALF_UP).toString();
                    
                    ShippingOptionsResponseModel shippingOpts = shippingResults.get(cacheKey);
                    if (shippingOpts != null && shippingOpts.getData() != null && 
                        shippingOpts.getData().available_courier_companies != null &&
                        !shippingOpts.getData().available_courier_companies.isEmpty()) {
                        
                        // Sort by rate (cheapest first)
                        shippingOpts.getData().available_courier_companies.sort(
                            (a, b) -> Double.compare(a.rate, b.rate));
                        
                        // Map all available couriers
                        for (var courier : shippingOpts.getData().available_courier_companies) {
                            shipment.getAvailableCouriers().add(mapCourier(courier));
                        }
                        
                        // Use cheapest courier rate for cost calculation (first after sorting)
                        shipment.setShippingCost(shipment.getAvailableCouriers().get(0).getRate());
                        shipment.setTotalCost(shipment.getPackagingCost().add(shipment.getShippingCost()));
                        totalShippingCost = totalShippingCost.add(shipment.getShippingCost());
                        
                        // This shipment is valid - add it
                        validShipments.add(shipment);
                    } else {
                        // No couriers available for this shipment - still include but mark unavailable
                        shipment.setTotalCost(shipment.getPackagingCost()); // Only packaging cost
                        allCouriersAvailable = false;
                        String locationName = shipment.getPickupLocation() != null ? 
                            shipment.getPickupLocation().getAddressNickName() : "Unknown";
                        if (unavailabilityReasons.length() > 0) {
                            unavailabilityReasons.append("; ");
                        }
                        unavailabilityReasons.append("No courier options available between pickup location ")
                            .append(locationName).append(" [").append(pickupPostcode).append("]")
                            .append(" and delivery postcode [").append(deliveryPostcode).append("]");
                        validShipments.add(shipment); // Still add - has packages but no couriers
                    }
                } else {
                    shipment.setTotalCost(shipment.getPackagingCost()); // Only packaging cost
                    allCouriersAvailable = false;
                    if (unavailabilityReasons.length() > 0) {
                        unavailabilityReasons.append("; ");
                    }
                    unavailabilityReasons.append("Missing postal code for shipment");
                    validShipments.add(shipment); // Still add - has packages but no postal code
                }
            }
            
            // Sort shipments by total cost in descending order (most expensive first)
            validShipments.sort((a, b) -> b.getTotalCost().compareTo(a.getTotalCost()));
            
            // Replace candidate shipments with sorted valid ones (those with packages)
            candidate.shipments = validShipments;
            
            // If we filtered out all shipments, this candidate is completely invalid
            if (validShipments.isEmpty()) {
                allCouriersAvailable = false;
                if (unavailabilityReasons.length() > 0) {
                    unavailabilityReasons.append("; ");
                }
                unavailabilityReasons.append("No valid shipments - all locations lack suitable packaging");
            }
            
            candidate.totalPackagingCost = totalPackagingCost;
            candidate.totalShippingCost = totalShippingCost;
            candidate.totalCost = totalPackagingCost.add(totalShippingCost);
            candidate.allCouriersAvailable = allCouriersAvailable;
            candidate.unavailabilityReason = unavailabilityReasons.length() > 0 ? 
                unavailabilityReasons.toString() : null;
        }
    }
    
    /**
     * Split products into multiple shipments if total weight exceeds limit.
     * Each shipment will have weight <= maxWeightPerShipment (route-specific).
     */
    private List<OrderOptimizationResponseModel.Shipment> splitIntoShipments(
            LocationInfo locInfo,
            List<OrderOptimizationResponseModel.ProductAllocation> productAllocations,
            List<OrderOptimizationResponseModel.PackageUsage> packageUsages,
            BigDecimal totalWeight,
            int totalQty,
            Map<Long, ProductLocationInfo> productInfoMap,
            BigDecimal maxWeightPerShipment) {
        
        List<OrderOptimizationResponseModel.Shipment> shipments = new ArrayList<>();
        
        // If total weight is within limit, create single shipment
        if (totalWeight.compareTo(maxWeightPerShipment) <= 0) {
            OrderOptimizationResponseModel.Shipment shipment = createShipment(
                locInfo, productAllocations, packageUsages, totalWeight, totalQty);
            shipments.add(shipment);
            return shipments;
        }
        
        // Greedy bin-packing: Fill each shipment to MAX capacity before starting a new one
        // This minimizes the number of shipments by packing as much as possible into each
        
        // Build a list of (productId, ProductInfo, ProductResponseModel, remainingQty)
        List<ProductAllocationTracker> trackers = new ArrayList<>();
        for (OrderOptimizationResponseModel.ProductAllocation alloc : productAllocations) {
            if (alloc.getProduct() == null) continue;
            Long productId = alloc.getProduct().getProductId();
            ProductLocationInfo productInfo = productInfoMap.get(productId);
            if (productInfo == null) continue;
            
            ProductAllocationTracker tracker = new ProductAllocationTracker();
            tracker.productId = productId;
            tracker.productInfo = productInfo;
            tracker.productResponseModel = alloc.getProduct();
            tracker.remainingQty = alloc.getAllocatedQuantity();
            tracker.weightPerUnit = productInfo.weightKgs;
            trackers.add(tracker);
        }
        
        // Sort by weight per unit descending (pack heaviest items first for better fit)
        trackers.sort((a, b) -> b.weightPerUnit.compareTo(a.weightPerUnit));
        
        // Greedily fill shipments one by one
        List<List<OrderOptimizationResponseModel.ProductAllocation>> shipmentProducts = new ArrayList<>();
        List<BigDecimal> shipmentWeights = new ArrayList<>();
        
        while (hasRemainingProducts(trackers)) {
            // Start a new shipment
            List<OrderOptimizationResponseModel.ProductAllocation> currentShipmentProducts = new ArrayList<>();
            BigDecimal currentShipmentWeight = BigDecimal.ZERO;
            
            // Try to fill this shipment with products from all trackers
            for (ProductAllocationTracker tracker : trackers) {
                if (tracker.remainingQty <= 0) continue;
                
                // Calculate how many units can fit in remaining capacity
                BigDecimal remainingCapacity = maxWeightPerShipment.subtract(currentShipmentWeight);
                int unitsCanFit;
                
                if (tracker.weightPerUnit.compareTo(BigDecimal.ZERO) > 0) {
                    unitsCanFit = remainingCapacity.divide(tracker.weightPerUnit, 0, RoundingMode.DOWN).intValue();
                } else {
                    unitsCanFit = tracker.remainingQty; // Zero weight items
                }
                
                // Handle case where single item exceeds limit (on empty shipment)
                if (unitsCanFit <= 0 && currentShipmentWeight.compareTo(BigDecimal.ZERO) == 0) {
                    unitsCanFit = 1; // At least one item even if overweight
                }
                
                if (unitsCanFit <= 0) continue; // Can't fit any more of this product
                
                int toAllocate = Math.min(tracker.remainingQty, unitsCanFit);
                
                // Add to current shipment
                OrderOptimizationResponseModel.ProductAllocation splitAlloc = 
                    new OrderOptimizationResponseModel.ProductAllocation();
                splitAlloc.setProduct(tracker.productResponseModel);
                splitAlloc.setAllocatedQuantity(toAllocate);
                splitAlloc.setTotalWeight(tracker.weightPerUnit.multiply(BigDecimal.valueOf(toAllocate)));
                
                currentShipmentProducts.add(splitAlloc);
                currentShipmentWeight = currentShipmentWeight.add(splitAlloc.getTotalWeight());
                tracker.remainingQty -= toAllocate;
            }
            
            // Add this shipment to the list
            if (!currentShipmentProducts.isEmpty()) {
                shipmentProducts.add(currentShipmentProducts);
                shipmentWeights.add(currentShipmentWeight);
            }
        }
        
        int numShipments = shipmentProducts.size();
        
        // Distribute packages across shipments proportionally
        List<List<OrderOptimizationResponseModel.PackageUsage>> shipmentPackages = 
            distributePackages(packageUsages, shipmentProducts, numShipments);
        
        // Create shipment objects
        for (int i = 0; i < numShipments; i++) {
            if (shipmentProducts.get(i).isEmpty()) continue;
            
            int shipmentQty = shipmentProducts.get(i).stream()
                .mapToInt(OrderOptimizationResponseModel.ProductAllocation::getAllocatedQuantity).sum();
            
            OrderOptimizationResponseModel.Shipment shipment = createShipment(
                locInfo, shipmentProducts.get(i), shipmentPackages.get(i), 
                shipmentWeights.get(i), shipmentQty);
            
            shipments.add(shipment);
        }
        
        return shipments;
    }
    
    /**
     * Distribute packages across shipments proportionally based on product quantities.
     */
    private List<List<OrderOptimizationResponseModel.PackageUsage>> distributePackages(
            List<OrderOptimizationResponseModel.PackageUsage> totalPackages,
            List<List<OrderOptimizationResponseModel.ProductAllocation>> shipmentProducts,
            int numShipments) {
        
        List<List<OrderOptimizationResponseModel.PackageUsage>> result = new ArrayList<>();
        for (int i = 0; i < numShipments; i++) {
            result.add(new ArrayList<>());
        }
        
        // Calculate total quantity per shipment for proportional distribution
        List<Integer> shipmentQuantities = shipmentProducts.stream()
            .map(prods -> prods.stream()
                .mapToInt(OrderOptimizationResponseModel.ProductAllocation::getAllocatedQuantity).sum())
            .collect(Collectors.toList());
        
        int totalQty = shipmentQuantities.stream().mapToInt(Integer::intValue).sum();
        if (totalQty == 0) return result;
        
        // Distribute each package type proportionally
        for (OrderOptimizationResponseModel.PackageUsage pkg : totalPackages) {
            int remainingPackages = pkg.getQuantityUsed();
            
            for (int i = 0; i < numShipments && remainingPackages > 0; i++) {
                // Calculate proportional share
                double proportion = (double) shipmentQuantities.get(i) / totalQty;
                int pkgsForShipment = (int) Math.ceil(pkg.getQuantityUsed() * proportion);
                pkgsForShipment = Math.min(pkgsForShipment, remainingPackages);
                
                if (pkgsForShipment > 0) {
                    OrderOptimizationResponseModel.PackageUsage splitPkg = 
                        new OrderOptimizationResponseModel.PackageUsage();
                    splitPkg.setPackageInfo(pkg.getPackageInfo());
                    splitPkg.setQuantityUsed(pkgsForShipment);
                    
                    BigDecimal pricePerUnit = pkg.getPackageInfo() != null && 
                        pkg.getPackageInfo().getPricePerUnit() != null ?
                        pkg.getPackageInfo().getPricePerUnit() : BigDecimal.ZERO;
                    splitPkg.setTotalCost(pricePerUnit.multiply(BigDecimal.valueOf(pkgsForShipment)));
                    
                    // Add product details for this shipment's products (only productId, not full product)
                    for (OrderOptimizationResponseModel.ProductAllocation prodAlloc : shipmentProducts.get(i)) {
                        if (prodAlloc.getProduct() != null && 
                            pkg.getProductIds().contains(prodAlloc.getProduct().getProductId())) {
                            OrderOptimizationResponseModel.PackageProductDetail detail = 
                                new OrderOptimizationResponseModel.PackageProductDetail();
                            detail.setProductId(prodAlloc.getProduct().getProductId());
                            detail.setQuantity(prodAlloc.getAllocatedQuantity());
                            splitPkg.getProductDetails().add(detail);
                            splitPkg.getProductIds().add(prodAlloc.getProduct().getProductId());
                        }
                    }
                    
                    result.get(i).add(splitPkg);
                    remainingPackages -= pkgsForShipment;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Create a shipment object with all details.
     */
    private OrderOptimizationResponseModel.Shipment createShipment(
            LocationInfo locInfo,
            List<OrderOptimizationResponseModel.ProductAllocation> products,
            List<OrderOptimizationResponseModel.PackageUsage> packages,
            BigDecimal weight,
            int qty) {
        
        OrderOptimizationResponseModel.Shipment shipment = 
            new OrderOptimizationResponseModel.Shipment();
        
        if (locInfo.pickupLocationEntity != null) {
            PickupLocationResponseModel pickupLocationResponse = new PickupLocationResponseModel(locInfo.pickupLocationEntity);
            // Remove client details from response - not needed for shipping optimization
            pickupLocationResponse.setClient(null);
            shipment.setPickupLocation(pickupLocationResponse);
        }
        
        shipment.setProducts(new ArrayList<>(products));
        shipment.setPackagesUsed(new ArrayList<>(packages));
        shipment.setTotalWeightKgs(weight);
        shipment.setTotalQuantity(qty);
        
        // Calculate packaging cost
        BigDecimal packagingCost = packages.stream()
            .map(OrderOptimizationResponseModel.PackageUsage::getTotalCost)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        shipment.setPackagingCost(packagingCost);
        
        return shipment;
    }
    
    /**
     * Calculate packaging with detailed product information.
     * Uses multi-product packaging optimization to combine different products
     * into the same package when they fit, minimizing packaging cost.
     */
    private List<OrderOptimizationResponseModel.PackageUsage> calculatePackagingWithDetails(
            Map<Long, Integer> productQtys,
            Map<Long, ProductLocationInfo> productInfoMap,
            LocationInfo locInfo) {
        
        List<OrderOptimizationResponseModel.PackageUsage> result = new ArrayList<>();
        
        // Build product dimensions map for multi-product packaging
        Map<Long, PackagingHelper.ProductDimension> productDimensions = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : productQtys.entrySet()) {
            Long productId = entry.getKey();
            int qty = entry.getValue();
            
            ProductLocationInfo info = productInfoMap.get(productId);
            if (info == null) continue;
            
            productDimensions.put(productId, new PackagingHelper.ProductDimension(
                info.length, info.breadth, info.height, info.weightKgs, qty));
        }
        
        if (productDimensions.isEmpty()) {
            return result;
        }
        
        // Use multi-product packaging optimization
        PackagingHelper.MultiProductPackagingResult estimate = 
            packagingHelper.calculatePackagingForMultipleProducts(productDimensions, locInfo.packageDimensions);
        
        // Convert to response model
        for (PackagingHelper.MultiProductPackageUsageResult usage : estimate.getPackagesUsed()) {
            OrderOptimizationResponseModel.PackageUsage pkgUsage = 
                new OrderOptimizationResponseModel.PackageUsage();
            
            com.example.SpringApi.Models.DatabaseModels.Package pkgEntity = 
                locInfo.packageEntities.get(usage.getPackageId());
            if (pkgEntity != null) {
                pkgUsage.setPackageInfo(new PackageResponseModel(pkgEntity));
            }
            
            pkgUsage.setQuantityUsed(usage.getQuantityUsed());
            pkgUsage.setTotalCost(usage.getTotalCost());
            
            // Add all products that are in this package type
            for (Map.Entry<Long, Integer> productEntry : usage.getProductQuantities().entrySet()) {
                Long productId = productEntry.getKey();
                int qtyInPackage = productEntry.getValue();
                
                pkgUsage.getProductIds().add(productId);
                
                // Add product detail
                OrderOptimizationResponseModel.PackageProductDetail detail = 
                    new OrderOptimizationResponseModel.PackageProductDetail();
                detail.setProductId(productId);
                detail.setQuantity(qtyInPackage);
                pkgUsage.getProductDetails().add(detail);
            }
            
            result.add(pkgUsage);
        }
        
        return result;
    }
    
    /**
     * Generate human-readable description for an allocation
     */
    private String generateDescription(AllocationCandidate candidate, 
                                        Map<Long, LocationInfo> locationInfoMap) {
        
        // Group shipments by location to detect weight-based splits
        Map<Long, List<OrderOptimizationResponseModel.Shipment>> shipmentsByLocation = new LinkedHashMap<>();
        for (OrderOptimizationResponseModel.Shipment s : candidate.shipments) {
            Long locId = s.getPickupLocation() != null ? 
                s.getPickupLocation().getPickupLocationId() : 0L;
            shipmentsByLocation.computeIfAbsent(locId, k -> new ArrayList<>()).add(s);
        }
        
        if (shipmentsByLocation.size() == 1) {
            // All from same location
            OrderOptimizationResponseModel.Shipment firstShipment = candidate.shipments.get(0);
            String locationName = firstShipment.getPickupLocation() != null ? 
                firstShipment.getPickupLocation().getAddressNickName() : "single location";
            
            if (candidate.shipments.size() > 1) {
                // Multiple shipments from same location (weight split)
                return "All from " + locationName + " (" + candidate.shipments.size() + " shipments)";
            } else {
                return "All from " + locationName;
            }
        } else {
            // Split across multiple locations
            return "Split: " + shipmentsByLocation.entrySet().stream()
                .map(entry -> {
                    List<OrderOptimizationResponseModel.Shipment> locShipments = entry.getValue();
                    String name = locShipments.get(0).getPickupLocation() != null ? 
                        locShipments.get(0).getPickupLocation().getAddressNickName() : "Location";
                    int totalItems = locShipments.stream()
                        .mapToInt(OrderOptimizationResponseModel.Shipment::getTotalQuantity).sum();
                    if (locShipments.size() > 1) {
                        return name + " (" + totalItems + " items, " + locShipments.size() + " shipments)";
                    } else {
                        return name + " (" + totalItems + " items)";
                    }
                })
                .collect(Collectors.joining(" + "));
        }
    }
    
    /**
     * Cancel a shipment.
     * Cancels the shipment in ShipRocket and updates the local shipment status to CANCELLED.
     * 
     * @param shipmentId The local shipment ID to cancel
     * @throws BadRequestException if the shipment cannot be cancelled
     * @throws NotFoundException if the shipment is not found
     */
    @Override
    @org.springframework.transaction.annotation.Transactional
    public void cancelShipment(Long shipmentId) {
        Long clientId = getClientId();
        
        // Find the shipment
        Shipment shipment = shipmentRepository.findByShipmentIdAndClientId(shipmentId, clientId);
        if (shipment == null) {
            throw new NotFoundException(String.format(ErrorMessages.ShipmentErrorMessages.NotFound, shipmentId));
        }
        
        // Check if already cancelled
        if ("CANCELLED".equals(shipment.getShipRocketStatus())) {
            throw new BadRequestException(ErrorMessages.ShipmentErrorMessages.AlreadyCancelled);
        }
        
        // Check if shipRocketOrderId exists
        String shipRocketOrderId = shipment.getShipRocketOrderId();
        if (shipRocketOrderId == null || shipRocketOrderId.isEmpty()) {
            throw new BadRequestException(ErrorMessages.ShipmentErrorMessages.NoShipRocketOrderId);
        }
        
        // Get client Shiprocket credentials
        ClientResponseModel clientResponse = clientService.getClientById(clientId);
        if (clientResponse.getShipRocketEmail() == null || clientResponse.getShipRocketPassword() == null) {
            throw new BadRequestException(ErrorMessages.ShippingErrorMessages.ShipRocketCredentialsNotConfigured);
        }
        
        // Create ShippingHelper and cancel in ShipRocket
        ShippingHelper shippingHelper = new ShippingHelper(
            clientResponse.getShipRocketEmail(),
            clientResponse.getShipRocketPassword()
        );
        
        try {
            // Parse the shipRocketOrderId to Long
            Long shipRocketOrderIdLong = Long.parseLong(shipRocketOrderId);
            shippingHelper.cancelOrders(java.util.List.of(shipRocketOrderIdLong));
        } catch (NumberFormatException e) {
            throw new BadRequestException(ErrorMessages.ShipmentErrorMessages.InvalidId + " Format error: " + shipRocketOrderId);
        } catch (Exception e) {
            throw new BadRequestException(ErrorMessages.ShipmentErrorMessages.InvalidId + " " + e.getMessage());
        }
        
        // Update local shipment status
        shipment.setShipRocketStatus("CANCELLED");
        shipment.setUpdatedAt(java.time.LocalDateTime.now());
        shipment.setModifiedUser(getUser());
        shipmentRepository.save(shipment);
        
        logger.info("Shipment {} cancelled successfully. ShipRocket order ID: {}", shipmentId, shipRocketOrderId);
    }
    
    /**
     * Create a return order for a shipment.
     * Creates a return shipment in ShipRocket and stores the return details locally.
     * 
     * @param request The return request containing shipment ID and products to return
     * @return ReturnShipmentResponseModel with the created return details
     * @throws BadRequestException if the return cannot be created
     * @throws NotFoundException if the shipment is not found
     */
    @Override
    @org.springframework.transaction.annotation.Transactional
    public ReturnShipmentResponseModel createReturn(CreateReturnRequestModel request) {
        Long clientId = getClientId();
        String currentUser = getUser();
        
        // Validate request
        if (request.getShipmentId() == null) {
            throw new BadRequestException(ErrorMessages.ReturnShipmentErrorMessages.ShipmentIdRequired);
        }
        if (request.getProducts() == null || request.getProducts().isEmpty()) {
            throw new BadRequestException(ErrorMessages.ReturnShipmentErrorMessages.AtLeastOneProductRequired);
        }
        
        // Find the shipment
        Shipment shipment = shipmentRepository.findByShipmentIdAndClientId(request.getShipmentId(), clientId);
        if (shipment == null) {
            throw new NotFoundException(String.format(ErrorMessages.ShipmentErrorMessages.NotFound, request.getShipmentId()));
        }
        
        // Validate shipment status is DELIVERED
        if (!"DELIVERED".equals(shipment.getShipRocketStatus())) {
            throw new BadRequestException(String.format(ErrorMessages.ReturnShipmentErrorMessages.OnlyDeliveredCanReturn, shipment.getShipRocketStatus()));
        }
        
        // Get client Shiprocket credentials
        ClientResponseModel clientResponse = clientService.getClientById(clientId);
        if (clientResponse.getShipRocketEmail() == null || clientResponse.getShipRocketPassword() == null) {
            throw new BadRequestException(ErrorMessages.ShippingErrorMessages.ShipRocketCredentialsNotConfigured);
        }
        
        // Load shipment products for validation
        org.hibernate.Hibernate.initialize(shipment.getShipmentProducts());
        Map<Long, Integer> shipmentProductQuantities = new HashMap<>();
        for (var sp : shipment.getShipmentProducts()) {
            shipmentProductQuantities.put(sp.getProductId(), sp.getAllocatedQuantity());
        }
        
        // Validate products and calculate totals
        List<Product> productsToReturn = new ArrayList<>();
        BigDecimal subTotal = BigDecimal.ZERO;
        int totalQuantity = 0;
        int totalReturnableQuantity = 0;
        
        for (CreateReturnRequestModel.ReturnProductItem item : request.getProducts()) {
            if (item.getProductId() == null) {
                throw new BadRequestException(ErrorMessages.ReturnShipmentErrorMessages.ProductIdRequired);
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BadRequestException(ErrorMessages.ReturnShipmentErrorMessages.ValidQuantityRequired);
            }
            if (item.getReason() == null || item.getReason().isEmpty()) {
                throw new BadRequestException(ErrorMessages.ReturnShipmentErrorMessages.ReturnReasonRequired);
            }
            
            // Check if product is in the shipment
            Integer shipmentQty = shipmentProductQuantities.get(item.getProductId());
            if (shipmentQty == null) {
                throw new BadRequestException(String.format(ErrorMessages.ReturnShipmentErrorMessages.ProductNotInShipment, item.getProductId()));
            }
            
            // Check quantity doesn't exceed shipment quantity
            if (item.getQuantity() > shipmentQty) {
                throw new BadRequestException(String.format(ErrorMessages.ReturnShipmentErrorMessages.ReturnQuantityExceeds, item.getQuantity(), shipmentQty, item.getProductId()));
            }
            
            // Load product
            Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new NotFoundException(String.format(ErrorMessages.ProductErrorMessages.ER013, item.getProductId())));
            
            // Validate product is within return window
            if (product.getReturnWindowDays() != null && product.getReturnWindowDays() > 0 && shipment.getDeliveredDate() != null) {
                java.time.LocalDateTime returnDeadline = shipment.getDeliveredDate().plusDays(product.getReturnWindowDays());
                if (java.time.LocalDateTime.now().isAfter(returnDeadline)) {
                    throw new BadRequestException(String.format(ErrorMessages.ReturnShipmentErrorMessages.ProductPastReturnWindow, product.getTitle(), product.getReturnWindowDays()));
                }
            } else if (product.getReturnWindowDays() == null || product.getReturnWindowDays() == 0) {
                throw new BadRequestException(String.format(ErrorMessages.ReturnShipmentErrorMessages.ProductNotReturnable, product.getTitle()));
            }
            
            productsToReturn.add(product);
            
            BigDecimal sellingPrice = product.getPrice().subtract(product.getDiscount());
            subTotal = subTotal.add(sellingPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            totalQuantity += item.getQuantity();
            totalReturnableQuantity += shipmentQty;
        }
        
        // Determine return type
        boolean isFullReturn = (totalQuantity >= totalReturnableQuantity);
        ReturnShipment.ReturnType returnType = isFullReturn ? 
            ReturnShipment.ReturnType.FULL_RETURN : ReturnShipment.ReturnType.PARTIAL_RETURN;
        
        // Initialize delivery address for pickup (customer's address)
        org.hibernate.Hibernate.initialize(shipment.getOrderSummary());
        org.hibernate.Hibernate.initialize(shipment.getOrderSummary().getEntityAddress());
        Address customerAddress = shipment.getOrderSummary().getEntityAddress();
        
        // Initialize pickup location for shipping (warehouse address - where to return)
        org.hibernate.Hibernate.initialize(shipment.getPickupLocation());
        org.hibernate.Hibernate.initialize(shipment.getPickupLocation().getAddress());
        PickupLocation warehouseLocation = shipment.getPickupLocation();
        Address warehouseAddress = warehouseLocation.getAddress();
        
        // Build ShipRocket return order request
        ShipRocketReturnOrderRequestModel shipRocketRequest = new ShipRocketReturnOrderRequestModel();
        
        // Generate unique order ID
        shipRocketRequest.setOrderId("RET-" + shipment.getShipmentId() + "-" + System.currentTimeMillis());
        shipRocketRequest.setOrderDate(java.time.LocalDate.now().toString());
        // Note: Channel ID is not required for return orders, ShipRocket uses default channel
        
        // Pickup address (customer location - where to pick up)
        shipRocketRequest.setPickupCustomerName(customerAddress.getNameOnAddress() != null ? customerAddress.getNameOnAddress() : "Customer");
        shipRocketRequest.setPickupLastName("");
        shipRocketRequest.setCompanyName(clientResponse.getName() != null ? clientResponse.getName() : "");
        shipRocketRequest.setPickupAddress(customerAddress.getStreetAddress());
        shipRocketRequest.setPickupAddress2(customerAddress.getStreetAddress2() != null ? customerAddress.getStreetAddress2() : "");
        shipRocketRequest.setPickupCity(customerAddress.getCity());
        shipRocketRequest.setPickupState(customerAddress.getState());
        shipRocketRequest.setPickupCountry(customerAddress.getCountry() != null ? customerAddress.getCountry() : "India");
        shipRocketRequest.setPickupPincode(customerAddress.getPostalCode());
        shipRocketRequest.setPickupEmail(customerAddress.getEmailOnAddress() != null ? customerAddress.getEmailOnAddress() : "");
        shipRocketRequest.setPickupPhone(customerAddress.getPhoneOnAddress() != null ? customerAddress.getPhoneOnAddress() : "");
        shipRocketRequest.setPickupIsdCode("91");
        
        // Shipping address (warehouse location - where to deliver return)
        shipRocketRequest.setShippingCustomerName(warehouseAddress.getNameOnAddress() != null ? warehouseAddress.getNameOnAddress() : warehouseLocation.getAddressNickName());
        shipRocketRequest.setShippingLastName("");
        shipRocketRequest.setShippingAddress(warehouseAddress.getStreetAddress());
        shipRocketRequest.setShippingAddress2(warehouseAddress.getStreetAddress2() != null ? warehouseAddress.getStreetAddress2() : "");
        shipRocketRequest.setShippingCity(warehouseAddress.getCity());
        shipRocketRequest.setShippingState(warehouseAddress.getState());
        shipRocketRequest.setShippingCountry(warehouseAddress.getCountry() != null ? warehouseAddress.getCountry() : "India");
        shipRocketRequest.setShippingPincode(warehouseAddress.getPostalCode());
        shipRocketRequest.setShippingEmail(warehouseAddress.getEmailOnAddress() != null ? warehouseAddress.getEmailOnAddress() : "");
        shipRocketRequest.setShippingPhone(warehouseAddress.getPhoneOnAddress() != null ? warehouseAddress.getPhoneOnAddress() : "");
        shipRocketRequest.setShippingIsdCode("91");
        
        // Order items
        List<ShipRocketReturnOrderRequestModel.ReturnOrderItem> orderItems = new ArrayList<>();
        int productIndex = 0;
        for (CreateReturnRequestModel.ReturnProductItem item : request.getProducts()) {
            Product product = productsToReturn.get(productIndex++);
            
            ShipRocketReturnOrderRequestModel.ReturnOrderItem orderItem = new ShipRocketReturnOrderRequestModel.ReturnOrderItem();
            orderItem.setName(product.getTitle());
            orderItem.setQcEnable(true);
            orderItem.setQcProductName(product.getTitle());
            orderItem.setSku(product.getUpc() != null ? product.getUpc() : "SKU-" + product.getProductId());
            orderItem.setUnits(item.getQuantity());
            orderItem.setSellingPrice(product.getPrice().subtract(product.getDiscount()));
            orderItem.setDiscount(BigDecimal.ZERO);
            orderItem.setQcBrand(product.getBrand());
            orderItem.setQcProductImage(product.getMainImageUrl());
            
            orderItems.add(orderItem);
        }
        shipRocketRequest.setOrderItems(orderItems);
        
        // Payment and totals
        shipRocketRequest.setPaymentMethod("PREPAID");
        shipRocketRequest.setTotalDiscount("0");
        shipRocketRequest.setSubTotal(subTotal);
        
        // Dimensions
        shipRocketRequest.setLength(request.getLength() != null ? request.getLength() : BigDecimal.valueOf(11));
        shipRocketRequest.setBreadth(request.getBreadth() != null ? request.getBreadth() : BigDecimal.valueOf(11));
        shipRocketRequest.setHeight(request.getHeight() != null ? request.getHeight() : BigDecimal.valueOf(11));
        shipRocketRequest.setWeight(request.getWeight() != null ? request.getWeight() : BigDecimal.valueOf(0.5));
        
        // Create ShippingHelper
        ShippingHelper shippingHelper = new ShippingHelper(
            clientResponse.getShipRocketEmail(),
            clientResponse.getShipRocketPassword()
        );
        
        // Call ShipRocket API to create return order
        String returnOrderJson;
        ShipRocketReturnOrderResponseModel returnOrderResponse;
        try {
            returnOrderJson = shippingHelper.createReturnOrderAsJson(shipRocketRequest);
            returnOrderResponse = new Gson().fromJson(returnOrderJson, ShipRocketReturnOrderResponseModel.class);
        } catch (Exception e) {
            throw new BadRequestException(String.format(ErrorMessages.ReturnShipmentErrorMessages.FailedToCreateReturn, e.getMessage()));
        }
        
        // Create ReturnShipment entity
        ReturnShipment returnShipment = new ReturnShipment();
        returnShipment.setShipmentId(shipment.getShipmentId());
        returnShipment.setReturnType(returnType);
        returnShipment.setShipRocketReturnOrderId(returnOrderResponse.getOrderIdAsString());
        returnShipment.setShipRocketReturnShipmentId(returnOrderResponse.getShipmentId());
        returnShipment.setShipRocketReturnStatus(returnOrderResponse.getStatus());
        returnShipment.setShipRocketReturnStatusCode(returnOrderResponse.getStatusCode());
        returnShipment.setShipRocketReturnOrderMetadata(returnOrderJson);
        returnShipment.setReturnWeightKgs(request.getWeight() != null ? request.getWeight() : BigDecimal.valueOf(0.5));
        returnShipment.setReturnLength(request.getLength());
        returnShipment.setReturnBreadth(request.getBreadth());
        returnShipment.setReturnHeight(request.getHeight());
        returnShipment.setClientId(clientId);
        returnShipment.setCreatedUser(currentUser);
        returnShipment.setModifiedUser(currentUser);
        
        // Save return shipment to get ID
        returnShipment = returnShipmentRepository.save(returnShipment);
        
        // Create ReturnShipmentProduct entities
        productIndex = 0;
        for (CreateReturnRequestModel.ReturnProductItem item : request.getProducts()) {
            Product product = productsToReturn.get(productIndex++);
            
            ReturnShipmentProduct returnProduct = new ReturnShipmentProduct();
            returnProduct.setReturnShipmentId(returnShipment.getReturnShipmentId());
            returnProduct.setProductId(item.getProductId());
            returnProduct.setReturnQuantity(item.getQuantity());
            returnProduct.setReturnReason(item.getReason());
            returnProduct.setReturnComments(item.getComments());
            returnProduct.setProductName(product.getTitle());
            returnProduct.setProductSku(product.getUpc() != null ? product.getUpc() : "SKU-" + product.getProductId());
            returnProduct.setProductSellingPrice(product.getPrice().subtract(product.getDiscount()));
            returnProduct.setClientId(clientId);
            returnProduct.setCreatedUser(currentUser);
            returnProduct.setModifiedUser(currentUser);
            
            returnShipmentProductRepository.save(returnProduct);
        }
        
        // Assign AWB for return shipment
        try {
            String awbJson = shippingHelper.assignReturnAwbAsJson(returnOrderResponse.getShipmentId());
            ShipRocketAwbResponseModel awbResponse = new Gson().fromJson(awbJson, ShipRocketAwbResponseModel.class);
            
            returnShipment.setShipRocketReturnAwbCode(awbResponse.getAwbCode());
            returnShipment.setShipRocketReturnAwbMetadata(awbJson);
            returnShipmentRepository.save(returnShipment);
        } catch (Exception e) {
            // Log but don't fail - AWB can be assigned later
            logger.warn("Failed to assign AWB for return shipment: " + e.getMessage());
        }
        
        // Update original shipment status
        String newStatus = isFullReturn ? "FULL_RETURN_INITIATED" : "PARTIAL_RETURN_INITIATED";
        shipment.setShipRocketStatus(newStatus);
        shipment.setModifiedUser(currentUser);
        shipmentRepository.save(shipment);
        
        logger.info("Return shipment created successfully. Return ID: {}, ShipRocket Order: {}, Type: {}", 
            returnShipment.getReturnShipmentId(), 
            returnShipment.getShipRocketReturnOrderId(),
            returnType.getValue());
        
        // Reload with products for response
        returnShipment = returnShipmentRepository.findByReturnShipmentIdAndClientId(
            returnShipment.getReturnShipmentId(), clientId);
        org.hibernate.Hibernate.initialize(returnShipment.getReturnProducts());
        
        return new ReturnShipmentResponseModel(returnShipment);
    }
    
    /**
     * Cancel a return shipment.
     * Cancels the return order in ShipRocket and updates the local return shipment status to RETURN_CANCELLED.
     * 
     * @param returnShipmentId The local return shipment ID to cancel
     * @throws BadRequestException if the return shipment cannot be cancelled
     * @throws NotFoundException if the return shipment is not found
     */
    @Override
    @org.springframework.transaction.annotation.Transactional
    public void cancelReturnShipment(Long returnShipmentId) {
        Long clientId = getClientId();
        
        // Find the return shipment
        ReturnShipment returnShipment = returnShipmentRepository.findByReturnShipmentIdAndClientId(returnShipmentId, clientId);
        if (returnShipment == null) {
            throw new NotFoundException(String.format(ErrorMessages.ReturnShipmentErrorMessages.NotFound, returnShipmentId));
        }
        
        // Check if already cancelled
        if (ReturnShipment.ReturnStatus.RETURN_CANCELLED.getValue().equals(returnShipment.getShipRocketReturnStatus())) {
            throw new BadRequestException(ErrorMessages.ReturnShipmentErrorMessages.AlreadyCancelled);
        }
        
        // Check if shipRocketReturnOrderId exists
        String shipRocketReturnOrderId = returnShipment.getShipRocketReturnOrderId();
        if (shipRocketReturnOrderId == null || shipRocketReturnOrderId.isEmpty()) {
            throw new BadRequestException(ErrorMessages.ReturnShipmentErrorMessages.NoShipRocketOrderId);
        }
        
        // Get client Shiprocket credentials
        ClientResponseModel clientResponse = clientService.getClientById(clientId);
        if (clientResponse.getShipRocketEmail() == null || clientResponse.getShipRocketPassword() == null) {
            throw new BadRequestException(ErrorMessages.ShippingErrorMessages.ShipRocketCredentialsNotConfigured);
        }
        
        // Create ShippingHelper and cancel in ShipRocket
        ShippingHelper shippingHelper = new ShippingHelper(
            clientResponse.getShipRocketEmail(),
            clientResponse.getShipRocketPassword()
        );
        
        try {
            // Parse the shipRocketReturnOrderId to Long
            Long shipRocketReturnOrderIdLong = Long.parseLong(shipRocketReturnOrderId);
            shippingHelper.cancelOrders(java.util.List.of(shipRocketReturnOrderIdLong));
        } catch (NumberFormatException e) {
            throw new BadRequestException(ErrorMessages.ReturnShipmentErrorMessages.InvalidId + " Format error: " + shipRocketReturnOrderId);
        } catch (Exception e) {
            throw new BadRequestException(String.format(ErrorMessages.ReturnShipmentErrorMessages.FailedToCancelReturn, e.getMessage()));
        }
        
        // Update local return shipment status
        returnShipment.setShipRocketReturnStatus(ReturnShipment.ReturnStatus.RETURN_CANCELLED.getValue());
        returnShipment.setUpdatedAt(java.time.LocalDateTime.now());
        returnShipment.setModifiedUser(getUser());
        returnShipmentRepository.save(returnShipment);
        
        logger.info("Return shipment {} cancelled successfully. ShipRocket return order ID: {}", 
            returnShipmentId, shipRocketReturnOrderId);
    }
    
    /**
     * Get the ShipRocket wallet balance for the client.
     * 
     * @return The wallet balance as a Double
     * @throws BadRequestException if the wallet balance cannot be retrieved
     */
    @Override
    public Double getWalletBalance() {
        Long clientId = getClientId();
        
        // Get client Shiprocket credentials
        ClientResponseModel clientResponse = clientService.getClientById(clientId);
        if (clientResponse.getShipRocketEmail() == null || clientResponse.getShipRocketPassword() == null) {
            throw new BadRequestException(ErrorMessages.ShippingErrorMessages.ShipRocketCredentialsNotConfigured);
        }
        
        // Create ShippingHelper and get wallet balance
        ShippingHelper shippingHelper = new ShippingHelper(
            clientResponse.getShipRocketEmail(),
            clientResponse.getShipRocketPassword()
        );
        
        return shippingHelper.getWalletBalance();
    }
    
    /**
     * Find the maximum weight that couriers can handle for a given route.
     * Starts at 500kg and reduces by 100kg until couriers are found or reaches 100kg.
     * 
     * @param shippingHelper The ShippingHelper instance to use for API calls
     * @param pickupPostcode Pickup location postal code
     * @param deliveryPostcode Delivery location postal code
     * @param isCod Whether the order is Cash on Delivery
     * @return Maximum weight in kg that can be shipped on this route, or 0 if no couriers available
     */
    private double findMaxWeightForRoute(
            ShippingHelper shippingHelper,
            String pickupPostcode,
            String deliveryPostcode,
            boolean isCod
    ) {
        // Start at 500kg and reduce by 100kg until we find couriers
        double[] weightsToTry = {500, 400, 300, 200, 100};
        
        for (double weight : weightsToTry) {
            try {
                ShippingOptionsResponseModel response = shippingHelper.getAvailableShippingOptions(
                    pickupPostcode, deliveryPostcode, isCod, String.valueOf(weight));
                
                if (response != null && response.getData() != null && 
                    response.getData().available_courier_companies != null &&
                    !response.getData().available_courier_companies.isEmpty()) {
                    return weight;
                }
            } catch (Exception e) {
                // Error checking weight - continue to next weight
            }
        }
        
        // No couriers found even at 100kg
        return 0;
    }
}
