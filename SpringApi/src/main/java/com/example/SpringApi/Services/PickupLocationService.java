package com.example.SpringApi.Services;

import com.example.SpringApi.FilterQueryBuilder.PickupLocationFilterQueryBuilder;
import com.example.SpringApi.Helpers.BulkInsertHelper;
import com.example.SpringApi.Models.ShippingResponseModel.AddPickupLocationResponseModel;
import com.example.SpringApi.Repositories.AddressRepository;
import com.example.SpringApi.Repositories.PickupLocationRepository;
import com.example.SpringApi.Repositories.ProductPickupLocationMappingRepository;
import com.example.SpringApi.Repositories.PackagePickupLocationMappingRepository;
import com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel;
import com.example.SpringApi.Models.ResponseModels.PickupLocationResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.RequestModels.PickupLocationRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.RequestModels.ProductPickupLocationMappingRequestModel;
import com.example.SpringApi.Models.RequestModels.PackagePickupLocationMappingRequestModel;
import com.example.SpringApi.Services.Interface.IPickupLocationSubTranslator;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import com.example.SpringApi.Models.DatabaseModels.PickupLocation;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.DatabaseModels.ProductPickupLocationMapping;
import com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Helpers.ShippingHelper;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.SuccessMessages;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Service implementation for PickupLocation-related business operations.
 * 
 * This service implements the contract defined in IPickupLocationSubTranslator
 * for pickup location management operations including CRUD operations,
 * batch retrieval, and status management.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class PickupLocationService extends BaseService implements IPickupLocationSubTranslator {

    private final PickupLocationRepository pickupLocationRepository;
    private final AddressRepository addressRepository;
    private final ProductPickupLocationMappingRepository productMappingRepository;
    private final PackagePickupLocationMappingRepository packageMappingRepository;
    private final UserLogService userLogService;
    private final ClientService clientService;
    private final ShippingHelper shippingHelper;
    private final PickupLocationFilterQueryBuilder pickupLocationFilterQueryBuilder;
    private final MessageService messageService;

    @Autowired
    public PickupLocationService(PickupLocationRepository pickupLocationRepository,
                                AddressRepository addressRepository,
                                ProductPickupLocationMappingRepository productMappingRepository,
                                PackagePickupLocationMappingRepository packageMappingRepository,
                                UserLogService userLogService,
                                ClientService clientService,
                                PickupLocationFilterQueryBuilder pickupLocationFilterQueryBuilder,
                                MessageService messageService,
                                HttpServletRequest request) {
        super();
        this.pickupLocationRepository = pickupLocationRepository;
        this.addressRepository = addressRepository;
        this.productMappingRepository = productMappingRepository;
        this.packageMappingRepository = packageMappingRepository;
        this.userLogService = userLogService;
        this.clientService = clientService;
        this.shippingHelper = null; // Will be initialized on demand
        this.pickupLocationFilterQueryBuilder = pickupLocationFilterQueryBuilder;
        this.messageService = messageService;
    }

    // Constructor for testing with mock ShippingHelper
    public PickupLocationService(PickupLocationRepository pickupLocationRepository,
                                AddressRepository addressRepository,
                                ProductPickupLocationMappingRepository productMappingRepository,
                                PackagePickupLocationMappingRepository packageMappingRepository,
                                UserLogService userLogService,
                                ClientService clientService,
                                ShippingHelper shippingHelper,
                                PickupLocationFilterQueryBuilder pickupLocationFilterQueryBuilder,
                                MessageService messageService,
                                HttpServletRequest request) {
        super();
        this.pickupLocationRepository = pickupLocationRepository;
        this.addressRepository = addressRepository;
        this.productMappingRepository = productMappingRepository;
        this.packageMappingRepository = packageMappingRepository;
        this.userLogService = userLogService;
        this.clientService = clientService;
        this.shippingHelper = shippingHelper;
        this.pickupLocationFilterQueryBuilder = pickupLocationFilterQueryBuilder;
        this.messageService = messageService;
    }

    // ============================================================================
    // Public Methods
    // ============================================================================

    /**
     * Retrieves pickup locations in paginated batches based on filter criteria.
     * 
     * This method supports filtering, sorting, and pagination of pickup locations.
     * It can include or exclude deleted records based on the request parameters.
     * 
     * @param paginationBaseRequestModel The pagination and filter parameters
     * @return Paginated pickup location data
     * @throws BadRequestException if pagination parameters are invalid
     */
    @Override
    public PaginationBaseResponseModel<PickupLocationResponseModel> getPickupLocationsInBatches(PaginationBaseRequestModel paginationBaseRequestModel) {
        // Valid columns for filtering
        Set<String> validColumns = new HashSet<>(Arrays.asList(
            "pickupLocationId", "locationName", "addressNickName", "address", "isDeleted", 
            "pickupLocationAddressId", "shipRocketPickupLocationId", "createdBy", "modifiedBy", 
            "createdAt", "updatedAt", "notes"
        ));

        // Validate filter conditions if provided
        if (paginationBaseRequestModel.getFilters() != null && !paginationBaseRequestModel.getFilters().isEmpty()) {
            for (PaginationBaseRequestModel.FilterCondition filter : paginationBaseRequestModel.getFilters()) {
                // Validate column name
                if (filter.getColumn() != null && !validColumns.contains(filter.getColumn())) {
                    throw new BadRequestException("Invalid column name: " + filter.getColumn());
                }

                // Validate operator (FilterCondition.setOperator auto-normalizes symbols to words)
                if (!filter.isValidOperator()) {
                    throw new BadRequestException("Invalid operator: " + filter.getOperator());
                }

                // Validate column type matches operator
                String columnType = pickupLocationFilterQueryBuilder.getColumnType(filter.getColumn());
                filter.validateOperatorForType(columnType, filter.getColumn());

                // Validate value presence
                filter.validateValuePresence();
            }
        }

        // Calculate page size and offset
        int start = paginationBaseRequestModel.getStart();
        int end = paginationBaseRequestModel.getEnd();
        int pageSize = end - start;

        // Validate page size
        if (pageSize <= 0) {
            throw new BadRequestException(ErrorMessages.CommonErrorMessages.InvalidPagination);
        }

        // Create custom Pageable with proper offset handling
        Pageable pageable = new PageRequest(0, pageSize, Sort.by("pickupLocationId").descending()) {
            @Override
            public long getOffset() {
                return start;
            }
        };

        // Use filter query builder for dynamic filtering
        Page<PickupLocation> result = pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            getClientId(),
            paginationBaseRequestModel.getSelectedIds(),
            paginationBaseRequestModel.getLogicOperator() != null ? paginationBaseRequestModel.getLogicOperator() : "AND",
            paginationBaseRequestModel.getFilters(),
            paginationBaseRequestModel.isIncludeDeleted(),
            pageable
        );

        // Get all pickup location IDs from the result for batch count queries
        List<Long> pickupLocationIds = result.getContent().stream()
            .map(PickupLocation::getPickupLocationId)
            .collect(Collectors.toList());

        // Batch fetch product and package counts (2 queries instead of 2*N queries)
        Map<Long, Integer> productCountMap = new HashMap<>();
        Map<Long, Integer> packageCountMap = new HashMap<>();
        
        if (!pickupLocationIds.isEmpty()) {
            // Get product counts in a single query
            List<Object[]> productCounts = productMappingRepository.countByPickupLocationIds(pickupLocationIds);
            for (Object[] row : productCounts) {
                productCountMap.put((Long) row[0], ((Number) row[1]).intValue());
            }
            
            // Get package counts in a single query
            List<Object[]> packageCounts = packageMappingRepository.countByPickupLocationIds(pickupLocationIds);
            for (Object[] row : packageCounts) {
                packageCountMap.put((Long) row[0], ((Number) row[1]).intValue());
            }
        }

        // Convert PickupLocation results to PickupLocationResponseModel with counts
        PaginationBaseResponseModel<PickupLocationResponseModel> response = new PaginationBaseResponseModel<>();
        response.setData(result.getContent().stream()
            .map(pickupLocation -> {
                PickupLocationResponseModel responseModel = new PickupLocationResponseModel(pickupLocation);
                // Set product and package counts from the maps (defaults to 0 if not found)
                responseModel.setProductCount(productCountMap.getOrDefault(pickupLocation.getPickupLocationId(), 0));
                responseModel.setPackageCount(packageCountMap.getOrDefault(pickupLocation.getPickupLocationId(), 0));
                return responseModel;
            })
            .collect(Collectors.toList()));
        response.setTotalDataCount(result.getTotalElements());

        return response;
    }

    /**
     * Retrieves a specific pickup location by its unique identifier.
     * 
     * This method fetches a pickup location along with its associated address information.
     * Throws NotFoundException if the pickup location is not found.
     * 
     * @param pickupLocationId The unique identifier of the pickup location
     * @return PickupLocationResponseModel containing the pickup location details
     * @throws NotFoundException if the pickup location is not found
     */
    @Override
    public PickupLocationResponseModel getPickupLocationById(long pickupLocationId) {
        PickupLocation pickupLocation = pickupLocationRepository.findPickupLocationByIdAndClientId(pickupLocationId, getClientId());
        if (pickupLocation == null) {
            throw new NotFoundException(String.format(ErrorMessages.PickupLocationErrorMessages.NotFound, pickupLocationId));
        }
        
        return new PickupLocationResponseModel(pickupLocation);
    }

    /**
     * Creates a new pickup location with associated address information.
     * 
     * This method validates the pickup location data, creates the address record,
     * and establishes the relationship between pickup location and address.
     * It also integrates with ShipRocket API for external shipping service coordination.
     * All operations are performed within a transaction for data consistency.
     * 
     * @param pickupLocationRequestModel The pickup location data to create
     * @throws BadRequestException if the request model is invalid
     * @throws Exception if ShipRocket API integration fails
     */
    @Override
    @Transactional
    public void createPickupLocation(PickupLocationRequestModel pickupLocationRequestModel) throws Exception {
        // Create the address first
        Address address = new Address(pickupLocationRequestModel.getAddress(), getUser());
        address = addressRepository.save(address);
        
        // Set the address ID in the request
        pickupLocationRequestModel.setPickupLocationAddressId(address.getAddressId());
        
        // Create the pickup location
        PickupLocation pickupLocation = new PickupLocation(pickupLocationRequestModel, getUser(), getClientId());
        
        // Set the address on the pickup location for ShipRocket API call
        // (The @ManyToOne relationship isn't loaded automatically)
        pickupLocation.setAddress(address);
        
        pickupLocation = pickupLocationRepository.save(pickupLocation);
        
        // Call ShipRocket to create pickup location
        ShippingHelper shippingHelper = getShippingHelper();
        AddPickupLocationResponseModel addPickupLocationResponse = shippingHelper.addPickupLocation(pickupLocation);
        
        // Extract and set the ShipRocket ID
        Long shipRocketPickupLocationId = extractShipRocketPickupLocationId(
                shippingHelper, addPickupLocationResponse, pickupLocation);
        pickupLocation.setShipRocketPickupLocationId(shipRocketPickupLocationId);
        pickupLocation = pickupLocationRepository.save(pickupLocation);
        
        // Save product mappings if provided
        saveProductMappings(pickupLocation.getPickupLocationId(), pickupLocationRequestModel.getProductMappings());
        
        // Save package mappings if provided
        savePackageMappings(pickupLocation.getPickupLocationId(), pickupLocationRequestModel.getPackageMappings());
        
        // Log the creation
        userLogService.logData(getUserId(), SuccessMessages.PickupLocationSuccessMessages.InsertPickupLocation + " " + pickupLocation.getPickupLocationId(), ApiRoutes.PickupLocationsSubRoute.CREATE_PICKUP_LOCATION);
    }

    /**
     * Updates an existing pickup location with new information.
     * 
     * This method validates the updated data, modifies the pickup location record,
     * and updates associated address information. It maintains audit trail information
     * and integrates with ShipRocket API for external shipping service coordination.
     * All operations are performed within a transaction for data consistency.
     * 
     * @param pickupLocationRequestModel The updated pickup location data
     * @throws NotFoundException if the pickup location or address is not found
     * @throws BadRequestException if the request model is invalid
     * @throws Exception if ShipRocket API integration fails
     */
    @Override
    @Transactional
    public void updatePickupLocation(PickupLocationRequestModel pickupLocationRequestModel) throws Exception {
        // Get the existing pickup location
        PickupLocation existingPickupLocation = pickupLocationRepository.findPickupLocationByIdAndClientId(
            pickupLocationRequestModel.getPickupLocationId(), getClientId());
        if (existingPickupLocation == null) {
            throw new NotFoundException(String.format(ErrorMessages.PickupLocationErrorMessages.NotFound, pickupLocationRequestModel.getPickupLocationId()));
        }
        
        // Get the existing address for comparison
        Address existingAddress = addressRepository.findById(existingPickupLocation.getPickupLocationAddressId())
            .orElseThrow(() -> new NotFoundException(ErrorMessages.AddressErrorMessages.NotFound));
        
        // Check if physical address fields have changed (requires new Shiprocket location)
        boolean addressFieldsChanged = hasAddressFieldsChanged(existingAddress, pickupLocationRequestModel.getAddress());
        
        // Update the address if provided
        Address updatedAddress = null;
        if (pickupLocationRequestModel.getAddress() != null) {
            updatedAddress = new Address(pickupLocationRequestModel.getAddress(), getUser(), existingAddress);
            updatedAddress = addressRepository.save(updatedAddress);
        } else {
            updatedAddress = existingAddress;
        }
        
        // Set the address ID on the request model for validation
        pickupLocationRequestModel.setPickupLocationAddressId(updatedAddress.getAddressId());
        
        // Update the pickup location
        PickupLocation updatedPickupLocation = new PickupLocation(pickupLocationRequestModel, getUser(), existingPickupLocation);
        
        // Set the address on the pickup location for ShipRocket API call
        // (The @ManyToOne relationship isn't loaded automatically)
        updatedPickupLocation.setAddress(updatedAddress);
        
        // Only call Shiprocket if physical address fields have changed
        if (addressFieldsChanged) {
            // Create new pickup location in ShipRocket and get new ID
            ShippingHelper shippingHelper = getShippingHelper();
            AddPickupLocationResponseModel addPickupLocationResponse = shippingHelper.addPickupLocation(updatedPickupLocation);
            Long shipRocketPickupLocationId = extractShipRocketPickupLocationId(
                    shippingHelper, addPickupLocationResponse, updatedPickupLocation);
            updatedPickupLocation.setShipRocketPickupLocationId(shipRocketPickupLocationId);
        } else {
            // Keep the existing Shiprocket ID
            updatedPickupLocation.setShipRocketPickupLocationId(existingPickupLocation.getShipRocketPickupLocationId());
        }
        
        // Save the updated pickup location to database
        updatedPickupLocation = pickupLocationRepository.save(updatedPickupLocation);
        
        // Update product mappings if provided (delete existing and recreate)
        if (pickupLocationRequestModel.getProductMappings() != null) {
            productMappingRepository.deleteByPickupLocationId(updatedPickupLocation.getPickupLocationId());
            saveProductMappings(updatedPickupLocation.getPickupLocationId(), pickupLocationRequestModel.getProductMappings());
        }
        
        // Update package mappings if provided (delete existing and recreate)
        if (pickupLocationRequestModel.getPackageMappings() != null) {
            packageMappingRepository.deleteByPickupLocationId(updatedPickupLocation.getPickupLocationId());
            savePackageMappings(updatedPickupLocation.getPickupLocationId(), pickupLocationRequestModel.getPackageMappings());
        }
        
        // Log the update
        userLogService.logData(getUserId(), SuccessMessages.PickupLocationSuccessMessages.UpdatePickupLocation + " " + updatedPickupLocation.getPickupLocationId(), ApiRoutes.PickupLocationsSubRoute.UPDATE_PICKUP_LOCATION);
    }

    /**
     * Toggles the deletion status of a pickup location by its ID.
     * 
     * This method performs a soft delete operation by toggling the isDeleted flag.
     * If the pickup location is currently active, it will be marked as deleted.
     * If the pickup location is currently deleted, it will be restored.
     * Updates the modified timestamp and user information.
     * 
     * @param pickupLocationId The unique identifier of the pickup location to toggle
     * @throws NotFoundException if the pickup location is not found
     */
    @Override
    public void togglePickupLocation(long pickupLocationId) {
        PickupLocation pickupLocation = pickupLocationRepository.findPickupLocationByIdAndClientId(pickupLocationId, getClientId());
        if (pickupLocation == null) {
            throw new NotFoundException(String.format(ErrorMessages.PickupLocationErrorMessages.NotFound, pickupLocationId));
        }
        
        pickupLocation.setIsDeleted(!pickupLocation.getIsDeleted());
        pickupLocation.setModifiedBy(getUser());
        pickupLocationRepository.save(pickupLocation);
        
        // Log the toggle action
        userLogService.logData(getUserId(), SuccessMessages.PickupLocationSuccessMessages.TogglePickupLocation + " " + pickupLocation.getPickupLocationId(), ApiRoutes.PickupLocationsSubRoute.TOGGLE_PICKUP_LOCATION);
    }

    /**
     * Creates multiple pickup locations asynchronously in a single operation.
     * Processing happens in background thread; results sent via message notification.
     * 
     * Uses @Async for non-blocking processing and:
     * - NOT_SUPPORTED: Runs without a transaction to avoid rollback-only issues when individual creations fail
     * 
     * @param pickupLocations List of PickupLocationRequestModel containing the pickup location data to create
     * @param requestingUserId The ID of the user making the request (captured from security context)
     * @param requestingUserLoginName The loginName of the user making the request (captured from security context)
     * @param requestingClientId The client ID of the user making the request (captured from security context)
     */
    @Override
    @Async
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void bulkCreatePickupLocationsAsync(List<PickupLocationRequestModel> pickupLocations, Long requestingUserId, String requestingUserLoginName, Long requestingClientId) {
        try {
            // Validate input
            if (pickupLocations == null || pickupLocations.isEmpty()) {
                throw new BadRequestException(String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "Pickup location"));
            }

            BulkInsertResponseModel<Long> response = new BulkInsertResponseModel<>();
            response.setTotalRequested(pickupLocations.size());
            
            int successCount = 0;
            int failureCount = 0;
            
            // Process each pickup location individually
            for (PickupLocationRequestModel pickupLocationRequest : pickupLocations) {
                try {
                    // Call createPickupLocationInternal with explicit createdUser
                    Long createdId = createPickupLocationInternal(pickupLocationRequest, requestingUserLoginName, requestingClientId);
                    
                    if (createdId != null) {
                        response.addSuccess(pickupLocationRequest.getAddressNickName(), createdId);
                        successCount++;
                    }
                    
                } catch (BadRequestException bre) {
                    // Validation or business logic error
                    response.addFailure(
                        pickupLocationRequest.getAddressNickName() != null ? pickupLocationRequest.getAddressNickName() : "unknown", 
                        bre.getMessage()
                    );
                    failureCount++;
                } catch (Exception e) {
                    // Unexpected error
                    response.addFailure(
                        pickupLocationRequest.getAddressNickName() != null ? pickupLocationRequest.getAddressNickName() : "unknown", 
                        "Error: " + e.getMessage()
                    );
                    failureCount++;
                }
            }
            
            // Log bulk pickup location creation (using captured context values)
            userLogService.logDataWithContext(
                requestingUserId,
                requestingUserLoginName,
                requestingClientId,
                SuccessMessages.PickupLocationSuccessMessages.InsertPickupLocation + " (Bulk: " + successCount + " succeeded, " + failureCount + " failed)",
                ApiRoutes.PickupLocationsSubRoute.BULK_CREATE_PICKUP_LOCATION
            );
            
            response.setSuccessCount(successCount);
            response.setFailureCount(failureCount);
            
            // Create a message with the bulk insert results using the helper (using captured context)
            BulkInsertHelper.createDetailedBulkInsertResultMessage(
                response, "Pickup Location", "Pickup Locations", "Location Name", "Pickup Location ID", 
                messageService, requestingUserId, requestingUserLoginName, requestingClientId
            );
            
        } catch (Exception e) {
            // Still send a message to user about the failure (using captured userId)
            BulkInsertResponseModel<Long> errorResponse = new BulkInsertResponseModel<>();
            errorResponse.setTotalRequested(pickupLocations != null ? pickupLocations.size() : 0);
            errorResponse.setSuccessCount(0);
            errorResponse.setFailureCount(pickupLocations != null ? pickupLocations.size() : 0);
            errorResponse.addFailure("bulk_import", "Critical error: " + e.getMessage());
            BulkInsertHelper.createDetailedBulkInsertResultMessage(
                errorResponse, "Pickup Location", "Pickup Locations", "Location Name", "Pickup Location ID", 
                messageService, requestingUserId, requestingUserLoginName, requestingClientId
            );
        }
    }

    /**
     * Creates multiple pickup locations synchronously in a single operation (for testing).
     * This is a synchronous wrapper that processes pickup locations immediately and returns results.
     * 
     * @param pickupLocations List of PickupLocationRequestModel containing the pickup location data to create
     * @return BulkInsertResponseModel containing success/failure details for each pickup location
     */
    @Override
    @Transactional
    public BulkInsertResponseModel<Long> bulkCreatePickupLocations(List<PickupLocationRequestModel> pickupLocations) {
        // Validate input
        if (pickupLocations == null || pickupLocations.isEmpty()) {
            throw new BadRequestException(String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "Pickup location"));
        }

        BulkInsertResponseModel<Long> response = new BulkInsertResponseModel<>();
        response.setTotalRequested(pickupLocations.size());
        
        int successCount = 0;
        int failureCount = 0;
        
        // Process each pickup location individually
        for (PickupLocationRequestModel pickupLocationRequest : pickupLocations) {
            try {
                // Call createPickupLocationInternal with current user
                Long createdId = createPickupLocationInternal(pickupLocationRequest, getUser(), getClientId());
                
                if (createdId != null) {
                    response.addSuccess(pickupLocationRequest.getAddressNickName(), createdId);
                    successCount++;
                }
                
            } catch (BadRequestException bre) {
                // Validation or business logic error
                response.addFailure(
                    pickupLocationRequest.getAddressNickName() != null ? pickupLocationRequest.getAddressNickName() : "unknown", 
                    bre.getMessage()
                );
                failureCount++;
            } catch (Exception e) {
                // Unexpected error
                response.addFailure(
                    pickupLocationRequest.getAddressNickName() != null ? pickupLocationRequest.getAddressNickName() : "unknown", 
                    "Error: " + e.getMessage()
                );
                failureCount++;
            }
        }
        
        // Log bulk pickup location creation
        userLogService.logData(
            getUserId(),
            SuccessMessages.PickupLocationSuccessMessages.InsertPickupLocation + " (Bulk: " + successCount + " succeeded, " + failureCount + " failed)",
            ApiRoutes.PickupLocationsSubRoute.BULK_CREATE_PICKUP_LOCATION
        );
        
        response.setSuccessCount(successCount);
        response.setFailureCount(failureCount);
        
        return response;
    }

    // ============================================================================
    // Private Helper Methods
    // ============================================================================

    /**
     * Creates a ShippingHelper instance initialized with the current client's ShipRocket credentials.
     * For testing, returns the injected mock if available.
     * 
     * @return ShippingHelper instance with client credentials
     */
    private ShippingHelper getShippingHelper() {
        if (shippingHelper != null) {
            return shippingHelper; // For testing with mock
        }
        ClientResponseModel client = clientService.getClientById(getClientId());
        return new ShippingHelper(client.getShipRocketEmail(), client.getShipRocketPassword());
    }

    /**
     * Creates a ShippingHelper instance initialized with a specific client's ShipRocket credentials.
     * Used for bulk operations where clientId is passed explicitly.
     * 
     * @param clientId The client ID to get credentials for
     * @return ShippingHelper instance with client credentials
     */
    private ShippingHelper getShippingHelper(Long clientId) {
        if (shippingHelper != null) {
            return shippingHelper; // For testing with mock
        }
        ClientResponseModel client = clientService.getClientById(clientId);
        return new ShippingHelper(client.getShipRocketEmail(), client.getShipRocketPassword());
    }

    /**
     * Checks if physical address fields have changed between existing and new address.
     * Only compares fields that require a new Shiprocket pickup location:
     * streetAddress, streetAddress2, streetAddress3, city, state, country, postalCode
     * 
     * @param existingAddress The current address in the database
     * @param newAddress The new address from the request
     * @return true if any physical address field has changed, false otherwise
     */
    private boolean hasAddressFieldsChanged(Address existingAddress, AddressRequestModel newAddress) {
        if (newAddress == null) {
            return false;
        }
        
        // Helper to safely compare strings (null-safe)
        java.util.function.BiPredicate<String, String> isDifferent = (existing, updated) -> {
            if (existing == null && updated == null) return false;
            if (existing == null || updated == null) return true;
            return !existing.equals(updated);
        };
        
        // Compare physical address fields only
        return isDifferent.test(existingAddress.getStreetAddress(), newAddress.getStreetAddress()) ||
               isDifferent.test(existingAddress.getStreetAddress2(), newAddress.getStreetAddress2()) ||
               isDifferent.test(existingAddress.getStreetAddress3(), newAddress.getStreetAddress3()) ||
               isDifferent.test(existingAddress.getCity(), newAddress.getCity()) ||
               isDifferent.test(existingAddress.getState(), newAddress.getState()) ||
               isDifferent.test(existingAddress.getCountry(), newAddress.getCountry()) ||
               isDifferent.test(existingAddress.getPostalCode(), newAddress.getPostalCode());
    }

    /**
     * Internal method to create a pickup location with explicit user and client context.
     * Used for bulk operations where security context may not be available.
     * 
     * @param pickupLocationRequestModel The pickup location data to create
     * @param createdUser The username to set as creator
     * @param clientId The client ID for this operation
     * @return The created pickup location ID
     * @throws Exception if creation fails
     */
    private Long createPickupLocationInternal(PickupLocationRequestModel pickupLocationRequestModel, String createdUser, Long clientId) throws Exception {
        // Create the address first
        Address address = new Address(pickupLocationRequestModel.getAddress(), createdUser);
        address = addressRepository.save(address);
        
        // Set the address ID in the request
        pickupLocationRequestModel.setPickupLocationAddressId(address.getAddressId());
        
        // Create the pickup location
        PickupLocation pickupLocation = new PickupLocation(pickupLocationRequestModel, createdUser, clientId);
        
        // Set the address on the pickup location for ShipRocket API call
        pickupLocation.setAddress(address);
        
        pickupLocation = pickupLocationRepository.save(pickupLocation);
        
        // Call ShipRocket to create pickup location
        ShippingHelper shippingHelperInstance = getShippingHelper(clientId);
        AddPickupLocationResponseModel addPickupLocationResponse = shippingHelperInstance.addPickupLocation(pickupLocation);
        
        // Extract and set the ShipRocket ID
        Long shipRocketPickupLocationId = extractShipRocketPickupLocationId(
                shippingHelperInstance, addPickupLocationResponse, pickupLocation);
        pickupLocation.setShipRocketPickupLocationId(shipRocketPickupLocationId);
        pickupLocationRepository.save(pickupLocation);
        
        // Save product and package mappings (for bulk import)
        saveProductMappingsInternal(pickupLocation.getPickupLocationId(), pickupLocationRequestModel.getProductMappings(), createdUser);
        savePackageMappingsInternal(pickupLocation.getPickupLocationId(), pickupLocationRequestModel.getPackageMappings(), createdUser);
        
        return pickupLocation.getPickupLocationId();
    }
    
    /**
     * Extracts the ShipRocket pickup location ID from the API response.
     * 
     * @param shippingHelper The ShippingHelper instance (unused, kept for compatibility)
     * @param addPickupLocationResponse The response from adding pickup location
     * @param pickupLocation The pickup location entity (unused, kept for compatibility)
     * @return The ShipRocket pickup location ID from pickup_id field
     * @throws BadRequestException if pickup_id is invalid or missing
     */
    private Long extractShipRocketPickupLocationId(
            ShippingHelper shippingHelper,
            AddPickupLocationResponseModel addPickupLocationResponse,
            PickupLocation pickupLocation) throws Exception {
        
        // Extract ID from pickup_id field only
        long pickupId = addPickupLocationResponse.getPickup_id();
        
        // Validate that we have a valid ShipRocket pickup location ID
        if (pickupId <= 0) {
            throw new BadRequestException("Failed to retrieve ShipRocket pickup location ID after creation. " +
                    "Response pickup_id: " + pickupId + " is invalid. " +
                    "Please verify the pickup location was created successfully in ShipRocket.");
        }
        
        return pickupId;
    }
    
    /**
     * Saves product pickup location mappings.
     * 
     * @param pickupLocationId The pickup location ID
     * @param productMappings List of product mappings to save
     */
    private void saveProductMappings(Long pickupLocationId, List<ProductPickupLocationMappingRequestModel> productMappings) {
        if (productMappings == null || productMappings.isEmpty()) {
            return;
        }
        
        String createdUser = getUser();
        for (ProductPickupLocationMappingRequestModel mapping : productMappings) {
            if (mapping.getProductId() == null || mapping.getQuantity() == null || mapping.getQuantity() < 1) {
                continue; // Skip invalid mappings
            }
            
            ProductPickupLocationMapping entity = new ProductPickupLocationMapping();
            entity.setProductId(mapping.getProductId());
            entity.setPickupLocationId(pickupLocationId);
            entity.setAvailableStock(mapping.getQuantity());
            entity.setItemAvailableFrom(LocalDateTime.now());
            entity.setIsActive(true);
            entity.setLastStockUpdate(LocalDateTime.now());
            entity.setMinStockLevel(1);
            entity.setMaxStockLevel(mapping.getQuantity() * 2);
            entity.setReorderLevel(mapping.getQuantity() / 2);
            entity.setCreatedUser(createdUser);
            entity.setModifiedUser(createdUser);
            
            productMappingRepository.save(entity);
        }
    }
    
    /**
     * Saves package pickup location mappings.
     * 
     * @param pickupLocationId The pickup location ID
     * @param packageMappings List of package mappings to save
     */
    private void savePackageMappings(Long pickupLocationId, List<PackagePickupLocationMappingRequestModel> packageMappings) {
        if (packageMappings == null || packageMappings.isEmpty()) {
            return;
        }
        
        String createdUser = getUser();
        for (PackagePickupLocationMappingRequestModel mapping : packageMappings) {
            if (mapping.getPackageId() == null || mapping.getQuantity() == null || mapping.getQuantity() < 1) {
                continue; // Skip invalid mappings
            }
            
            PackagePickupLocationMapping entity = new PackagePickupLocationMapping();
            entity.setPackageId(mapping.getPackageId());
            entity.setPickupLocationId(pickupLocationId);
            entity.setAvailableQuantity(mapping.getQuantity());
            entity.setReorderLevel(mapping.getReorderLevel() != null ? mapping.getReorderLevel() : 1);
            entity.setMaxStockLevel(mapping.getMaxStockLevel() != null ? mapping.getMaxStockLevel() : mapping.getQuantity() * 2);
            entity.setLastRestockDate(LocalDateTime.now());
            entity.setCreatedUser(createdUser);
            entity.setModifiedUser(createdUser);
            
            packageMappingRepository.save(entity);
        }
    }
    
    /**
     * Internal method to save product mappings with explicit createdUser (for bulk import).
     */
    private void saveProductMappingsInternal(Long pickupLocationId, List<ProductPickupLocationMappingRequestModel> productMappings, String createdUser) {
        if (productMappings == null || productMappings.isEmpty()) {
            return;
        }
        
        for (ProductPickupLocationMappingRequestModel mapping : productMappings) {
            if (mapping.getProductId() == null || mapping.getQuantity() == null || mapping.getQuantity() < 1) {
                continue; // Skip invalid mappings
            }
            
            ProductPickupLocationMapping entity = new ProductPickupLocationMapping();
            entity.setProductId(mapping.getProductId());
            entity.setPickupLocationId(pickupLocationId);
            entity.setAvailableStock(mapping.getQuantity());
            entity.setItemAvailableFrom(LocalDateTime.now());
            entity.setIsActive(true);
            entity.setLastStockUpdate(LocalDateTime.now());
            entity.setMinStockLevel(1);
            entity.setMaxStockLevel(mapping.getQuantity() * 2);
            entity.setReorderLevel(mapping.getQuantity() / 2);
            entity.setCreatedUser(createdUser);
            entity.setModifiedUser(createdUser);
            
            productMappingRepository.save(entity);
        }
    }
    
    /**
     * Internal method to save package mappings with explicit createdUser (for bulk import).
     */
    private void savePackageMappingsInternal(Long pickupLocationId, List<PackagePickupLocationMappingRequestModel> packageMappings, String createdUser) {
        if (packageMappings == null || packageMappings.isEmpty()) {
            return;
        }
        
        for (PackagePickupLocationMappingRequestModel mapping : packageMappings) {
            if (mapping.getPackageId() == null || mapping.getQuantity() == null || mapping.getQuantity() < 1) {
                continue; // Skip invalid mappings
            }
            
            PackagePickupLocationMapping entity = new PackagePickupLocationMapping();
            entity.setPackageId(mapping.getPackageId());
            entity.setPickupLocationId(pickupLocationId);
            entity.setAvailableQuantity(mapping.getQuantity());
            entity.setReorderLevel(mapping.getReorderLevel() != null ? mapping.getReorderLevel() : 1);
            entity.setMaxStockLevel(mapping.getMaxStockLevel() != null ? mapping.getMaxStockLevel() : mapping.getQuantity() * 2);
            entity.setLastRestockDate(LocalDateTime.now());
            entity.setCreatedUser(createdUser);
            entity.setModifiedUser(createdUser);
            
            packageMappingRepository.save(entity);
        }
    }
}