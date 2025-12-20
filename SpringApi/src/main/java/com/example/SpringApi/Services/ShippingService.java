package com.example.SpringApi.Services;

import com.example.SpringApi.Helpers.PackagingHelper;
import com.example.SpringApi.Helpers.ShippingHelper;
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
import com.example.SpringApi.Services.Interface.IShippingSubTranslator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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

    /**
     * Maximum weight per shipment in kg.
     * Couriers typically have limits around 50-100 kg.
     * We use 100 kg to maximize consolidation and minimize shipment count.
     * Multiple products (including partial quantities) can be combined in a single shipment
     * to reach this limit efficiently.
     */
    private static final BigDecimal MAX_WEIGHT_PER_SHIPMENT = BigDecimal.valueOf(150);
    
    private final ClientService clientService;
    private final ProductRepository productRepository;
    private final ProductPickupLocationMappingRepository productPickupLocationMappingRepository;
    private final PackagePickupLocationMappingRepository packagePickupLocationMappingRepository;
    private final PackagingHelper packagingHelper;

    @Autowired
    public ShippingService(
            ClientService clientService,
            ProductRepository productRepository,
            ProductPickupLocationMappingRepository productPickupLocationMappingRepository,
            PackagePickupLocationMappingRepository packagePickupLocationMappingRepository,
            PackagingHelper packagingHelper) {
        this.clientService = clientService;
        this.productRepository = productRepository;
        this.productPickupLocationMappingRepository = productPickupLocationMappingRepository;
        this.packagePickupLocationMappingRepository = packagePickupLocationMappingRepository;
        this.packagingHelper = packagingHelper;
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
                // Log error but continue with other locations
                System.err.println("Failed to fetch shipping for location " + location.getLocationName() 
                    + " (" + location.getPickupPostcode() + "): " + e.getMessage());
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
        OrderOptimizationResponseModel response = new OrderOptimizationResponseModel();
        
        // Validate request
        if (request.getProductQuantities() == null || request.getProductQuantities().isEmpty()) {
            response.setSuccess(false);
            response.setErrorMessage("No products specified");
            return response;
        }
        
        if (request.getDeliveryPostcode() == null || request.getDeliveryPostcode().isEmpty()) {
            response.setSuccess(false);
            response.setErrorMessage("Delivery postcode is required");
            return response;
        }
        
        try {
            // Step 1: Fetch all product data
            Map<Long, ProductLocationInfo> productInfoMap = fetchProductData(request.getProductQuantities());
            
            if (productInfoMap.isEmpty()) {
                response.setSuccess(false);
                response.setErrorMessage("No valid products found");
                return response;
            }
            
            // Step 2: Fetch stock and packaging info for all products at all locations
            Map<Long, LocationInfo> locationInfoMap = fetchLocationData(productInfoMap);
            
            List<AllocationCandidate> candidates;
            
            // Check if custom allocations are provided
            if (request.getCustomAllocations() != null && !request.getCustomAllocations().isEmpty()) {
                // Custom mode: Use user-provided allocations directly
                AllocationCandidate customCandidate = createCustomAllocationCandidate(
                    request.getCustomAllocations(), 
                    productInfoMap, 
                    locationInfoMap
                );
                candidates = new ArrayList<>();
                candidates.add(customCandidate);
            } else {
                // Auto mode: Generate optimal candidates
                // Step 3: Build feasibility matrix and check if order can be fulfilled
                String feasibilityError = checkFeasibility(productInfoMap, request.getProductQuantities());
                if (feasibilityError != null) {
                    response.setSuccess(false);
                    response.setErrorMessage(feasibilityError);
                    return response;
                }
                
                // Step 4: Generate candidate allocation strategies
                candidates = generateCandidates(
                    productInfoMap, 
                    locationInfoMap, 
                    request.getProductQuantities()
                );
                
                if (candidates.isEmpty()) {
                    response.setSuccess(false);
                    response.setErrorMessage("No valid allocation strategies found");
                    return response;
                }
            }
            
            // Step 5: Evaluate each candidate (calculate packaging and shipping costs)
            ShippingHelper shippingHelper = getShippingHelper();
            String deliveryPostcode = request.getDeliveryPostcode();
            boolean isCod = Boolean.TRUE.equals(request.getIsCod());
            
            evaluateCandidates(candidates, productInfoMap, locationInfoMap, 
                              shippingHelper, deliveryPostcode, isCod);
            
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
            
        } catch (Exception e) {
            response.setSuccess(false);
            response.setErrorMessage("Optimization failed: " + e.getMessage());
            e.printStackTrace();
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
            List<PackagePickupLocationMapping> packageMappings = 
                packagePickupLocationMappingRepository.findByPickupLocationIdsWithPackages(
                    new ArrayList<>(locationInfoMap.keySet())
                );
            
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
                    
                    // Check if product fits in any package
                    boolean canFit = canProductFitInAnyPackage(productInfo, locInfo.packageDimensions);
                    if (canFit) {
                        // Calculate max items packable
                        PackagingHelper.ProductDimension productDim = new PackagingHelper.ProductDimension(
                            productInfo.length, productInfo.breadth, productInfo.height,
                            productInfo.weightKgs, stock.availableStock
                        );
                        PackagingHelper.PackagingEstimateResult estimate = 
                            packagingHelper.calculatePackaging(productDim, locInfo.packageDimensions);
                        stock.maxItemsPackable = estimate.getMaxItemsPackable();
                    } else {
                        stock.maxItemsPackable = 0;
                    }
                } else {
                    stock.maxItemsPackable = 0;
                }
            }
        }
        
        return locationInfoMap;
    }
    
    /**
     * Check if a product can fit in any available package
     */
    private boolean canProductFitInAnyPackage(ProductLocationInfo product, 
                                               List<PackagingHelper.PackageDimension> packages) {
        if (product.length == null || product.breadth == null || product.height == null) {
            return true; // Assume it fits if no dimensions
        }
        
        for (PackagingHelper.PackageDimension pkg : packages) {
            if (pkg.getAvailableQuantity() > 0 &&
                pkg.getVolume() >= product.length.doubleValue() * product.breadth.doubleValue() * product.height.doubleValue() &&
                pkg.getMaxWeight() >= product.weightKgs.doubleValue()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if the order can be fulfilled
     */
    private String checkFeasibility(Map<Long, ProductLocationInfo> productInfoMap, 
                                    Map<Long, Integer> productQuantities) {
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Long productId = entry.getKey();
            int requestedQty = entry.getValue();
            
            ProductLocationInfo info = productInfoMap.get(productId);
            if (info == null) {
                return "Product ID " + productId + " not found";
            }
            
            // Calculate total available (considering packaging constraints)
            int totalAvailable = info.stockByLocation.values().stream()
                .mapToInt(s -> Math.min(s.availableStock, s.maxItemsPackable))
                .sum();
            
            if (totalAvailable < requestedQty) {
                return "Insufficient stock/packaging for product '" + info.productTitle + 
                       "'. Requested: " + requestedQty + ", Available: " + totalAvailable;
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
     * Create allocation candidate from user-provided custom allocations.
     * Transforms the nested map structure into the candidate format.
     * 
     * @param customAllocations Map of productId -> (pickupLocationId -> quantity)
     * @param productInfoMap Product information map
     * @param locationInfoMap Location information map
     * @return AllocationCandidate with the user's allocations
     */
    private AllocationCandidate createCustomAllocationCandidate(
            Map<Long, Map<Long, Integer>> customAllocations,
            Map<Long, ProductLocationInfo> productInfoMap,
            Map<Long, LocationInfo> locationInfoMap) {
        
        AllocationCandidate candidate = new AllocationCandidate();
        
        // Transform from productId -> (locationId -> qty) 
        // to locationId -> (productId -> qty)
        for (Map.Entry<Long, Map<Long, Integer>> productEntry : customAllocations.entrySet()) {
            Long productId = productEntry.getKey();
            Map<Long, Integer> locationQtys = productEntry.getValue();
            
            for (Map.Entry<Long, Integer> locEntry : locationQtys.entrySet()) {
                Long locationId = locEntry.getKey();
                Integer qty = locEntry.getValue();
                
                if (qty != null && qty > 0) {
                    candidate.locationProductQuantities
                        .computeIfAbsent(locationId, k -> new HashMap<>())
                        .put(productId, qty);
                }
            }
        }
        
        candidate.canFulfillOrder = true;
        candidate.shortfall = 0;
        
        return candidate;
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
     * Evaluate candidates by calculating packaging and shipping costs.
     * Splits heavy shipments into smaller ones to meet courier weight limits.
     * Dynamically determines max weight per route by testing from 500kg down to 100kg.
     */
    private void evaluateCandidates(
            List<AllocationCandidate> candidates,
            Map<Long, ProductLocationInfo> productInfoMap,
            Map<Long, LocationInfo> locationInfoMap,
            ShippingHelper shippingHelper,
            String deliveryPostcode,
            boolean isCod) {
        
        // Pre-fetch: Find max weight for each unique pickup-delivery route
        // This tests 500kg, 400kg, 300kg, 200kg, 100kg until couriers are found
        Map<String, BigDecimal> routeMaxWeights = new ConcurrentHashMap<>();
        Set<String> uniquePickupPostcodes = new HashSet<>();
        
        // Collect all unique pickup postcodes
        for (LocationInfo locInfo : locationInfoMap.values()) {
            if (locInfo.postalCode != null) {
                uniquePickupPostcodes.add(locInfo.postalCode);
            }
        }
        
        // Fetch max weight for each route in parallel
        List<CompletableFuture<Void>> maxWeightFutures = new ArrayList<>();
        for (String pickupPostcode : uniquePickupPostcodes) {
            maxWeightFutures.add(CompletableFuture.runAsync(() -> {
                try {
                    double maxWeight = shippingHelper.findMaxWeightForRoute(pickupPostcode, deliveryPostcode, isCod);
                    if (maxWeight > 0) {
                        routeMaxWeights.put(pickupPostcode, BigDecimal.valueOf(maxWeight));
                    } else {
                        // No couriers available - use 0 to indicate this route is not serviceable
                        routeMaxWeights.put(pickupPostcode, BigDecimal.ZERO);
                    }
                } catch (Exception e) {
                    System.err.println("Error finding max weight for route: " + e.getMessage());
                    routeMaxWeights.put(pickupPostcode, MAX_WEIGHT_PER_SHIPMENT);
                }
            }));
        }
        
        // Wait for all max weight calls to complete
        try {
            CompletableFuture.allOf(maxWeightFutures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            System.err.println("Error fetching route max weights: " + e.getMessage());
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
                if (routeMaxWeight.compareTo(BigDecimal.ZERO) == 0) {
                    hasUnserviceableRoute = true;
                    String locationName = locInfo.locationName != null ? 
                        locInfo.locationName : "Unknown";
                    if (routeErrors.length() > 0) routeErrors.append("; ");
                    routeErrors.append("No couriers service route from ").append(locationName)
                        .append(" (").append(pickupPostcode).append(") to ").append(deliveryPostcode);
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
        Map<String, CompletableFuture<ShippingOptionsResponseModel>> shippingFutures = 
            new ConcurrentHashMap<>();
        
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
                    
                    shippingFutures.put(cacheKey, CompletableFuture.supplyAsync(() -> {
                        try {
                            return shippingHelper.getAvailableShippingOptions(
                                finalPickupPostcode, deliveryPostcode, isCod, finalWeight);
                        } catch (Exception e) {
                            System.err.println("Shipping API error: " + e.getMessage());
                            return null;
                        }
                    }));
                }
            }
        }
        
        // Wait for all shipping calls to complete
        CompletableFuture.allOf(shippingFutures.values().toArray(new CompletableFuture[0])).join();
        
        // Build shipping results map
        Map<String, ShippingOptionsResponseModel> shippingResults = new HashMap<>();
        for (Map.Entry<String, CompletableFuture<ShippingOptionsResponseModel>> entry : 
             shippingFutures.entrySet()) {
            try {
                shippingResults.put(entry.getKey(), entry.getValue().get());
            } catch (Exception e) {
                // Ignore
            }
        }
        
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
                        unavailabilityReasons.append("No couriers for route from ").append(locationName)
                            .append(" (").append(shipment.getTotalWeightKgs()).append(" kg)");
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
     */
    private List<OrderOptimizationResponseModel.PackageUsage> calculatePackagingWithDetails(
            Map<Long, Integer> productQtys,
            Map<Long, ProductLocationInfo> productInfoMap,
            LocationInfo locInfo) {
        
        List<OrderOptimizationResponseModel.PackageUsage> result = new ArrayList<>();
        
        for (Map.Entry<Long, Integer> entry : productQtys.entrySet()) {
            Long productId = entry.getKey();
            int qty = entry.getValue();
            
            ProductLocationInfo info = productInfoMap.get(productId);
            if (info == null) continue;
            
            PackagingHelper.ProductDimension productDim = new PackagingHelper.ProductDimension(
                info.length, info.breadth, info.height, info.weightKgs, qty);
            
            PackagingHelper.PackagingEstimateResult estimate = 
                packagingHelper.calculatePackaging(productDim, locInfo.packageDimensions);
            
            for (PackagingHelper.PackageUsageResult usage : estimate.getPackagesUsed()) {
                OrderOptimizationResponseModel.PackageUsage pkgUsage = 
                    new OrderOptimizationResponseModel.PackageUsage();
                
                com.example.SpringApi.Models.DatabaseModels.Package pkgEntity = 
                    locInfo.packageEntities.get(usage.getPackageId());
                if (pkgEntity != null) {
                    pkgUsage.setPackageInfo(new PackageResponseModel(pkgEntity));
                }
                
                pkgUsage.setQuantityUsed(usage.getQuantityUsed());
                pkgUsage.setTotalCost(usage.getTotalCost());
                pkgUsage.getProductIds().add(productId);
                
                // Add product detail (only productId, full details are in Shipment.products)
                OrderOptimizationResponseModel.PackageProductDetail detail = 
                    new OrderOptimizationResponseModel.PackageProductDetail();
                detail.setProductId(productId);
                detail.setQuantity(qty);
                pkgUsage.getProductDetails().add(detail);
                
                result.add(pkgUsage);
            }
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
}
