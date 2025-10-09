package com.example.SpringApi.Services;

import com.example.SpringApi.Models.ShippingResponseModel.AddPickupLocationResponseModel;
import com.example.SpringApi.Repositories.AddressRepository;
import com.example.SpringApi.Repositories.PickupLocationRepository;
import com.example.SpringApi.Models.ResponseModels.PickupLocationResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.RequestModels.PickupLocationRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Services.Interface.IPickupLocationSubTranslator;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import com.example.SpringApi.Models.DatabaseModels.PickupLocation;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Helpers.ShippingHelper;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.SuccessMessages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
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
    private final UserLogService userLogService;
    private final ClientService clientService;
    private final ShippingHelper shippingHelper;

    @Autowired
    public PickupLocationService(PickupLocationRepository pickupLocationRepository,
                                AddressRepository addressRepository,
                                UserLogService userLogService,
                                ClientService clientService,
                                HttpServletRequest request) {
        super(request);
        this.pickupLocationRepository = pickupLocationRepository;
        this.addressRepository = addressRepository;
        this.userLogService = userLogService;
        this.clientService = clientService;
        this.shippingHelper = null; // Will be initialized on demand
    }

    // Constructor for testing with mock ShippingHelper
    public PickupLocationService(PickupLocationRepository pickupLocationRepository,
                                AddressRepository addressRepository,
                                UserLogService userLogService,
                                ClientService clientService,
                                ShippingHelper shippingHelper,
                                HttpServletRequest request) {
        super(request);
        this.pickupLocationRepository = pickupLocationRepository;
        this.addressRepository = addressRepository;
        this.userLogService = userLogService;
        this.clientService = clientService;
        this.shippingHelper = shippingHelper;
    }

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
        Set<String> validColumns = new HashSet<>(Arrays.asList(
            "pickupLocationId",
            "locationName",
            "address"
        ));

        if (paginationBaseRequestModel.getColumnName() != null &&
            !validColumns.contains(paginationBaseRequestModel.getColumnName())) {
            throw new BadRequestException("Invalid column name for filtering: " + paginationBaseRequestModel.getColumnName());
        }

        // Calculate page number from start and pageSize
        int pageNumber = paginationBaseRequestModel.getStart() / paginationBaseRequestModel.getPageSize();

        // Create pageable with basic sorting
        Sort sort = Sort.by(Sort.Direction.ASC, "pickupLocationId");
        Pageable pageable = PageRequest.of(pageNumber, paginationBaseRequestModel.getPageSize(), sort);

        // Execute paginated query with filtering
        Page<Object[]> result = pickupLocationRepository.findPaginatedPickupLocations(
            paginationBaseRequestModel.getColumnName(),
            paginationBaseRequestModel.getCondition(),
            paginationBaseRequestModel.getFilterExpr(),
            paginationBaseRequestModel.isIncludeDeleted(),
            pageable
        );

        // Convert Object[] results to PickupLocationResponseModel
        PaginationBaseResponseModel<PickupLocationResponseModel> response = new PaginationBaseResponseModel<>();
        response.setData(result.getContent().stream()
            .map(objects -> {
                PickupLocation pickupLocation = (PickupLocation) objects[0];
                Address address = (Address) objects[1];
                PickupLocationResponseModel responseModel = new PickupLocationResponseModel(pickupLocation, address);
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
        PickupLocation pickupLocation = pickupLocationRepository.findPickupLocationById(pickupLocationId);
        if (pickupLocation == null) {
            throw new NotFoundException(String.format(ErrorMessages.PickupLocationErrorMessages.NotFound, pickupLocationId));
        }
        
        return new PickupLocationResponseModel(pickupLocation, pickupLocation.getAddress());
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
        PickupLocation pickupLocation = new PickupLocation(pickupLocationRequestModel, getUser());
        pickupLocation = pickupLocationRepository.save(pickupLocation);
        
        // Call ShipRocket to create pickup location
        ShippingHelper shippingHelper = getShippingHelper();
        AddPickupLocationResponseModel addPickupLocationResponse = shippingHelper.addPickupLocation(pickupLocation);
        
        // Set the ShipRocket ID
        pickupLocation.setShipRocketPickupLocationId(addPickupLocationResponse.getPickup_id());
        pickupLocationRepository.save(pickupLocation);
        
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
        PickupLocation existingPickupLocation = pickupLocationRepository.findById(pickupLocationRequestModel.getPickupLocationId())
            .orElseThrow(() -> new NotFoundException(String.format(ErrorMessages.PickupLocationErrorMessages.NotFound, pickupLocationRequestModel.getPickupLocationId())));
        
        // Update the address if provided
        if (pickupLocationRequestModel.getAddress() != null) {
            Address existingAddress = addressRepository.findById(existingPickupLocation.getPickupLocationAddressId())
                .orElseThrow(() -> new NotFoundException(ErrorMessages.AddressErrorMessages.NotFound));
            
            Address updatedAddress = new Address(pickupLocationRequestModel.getAddress(), getUser(), existingAddress);
            addressRepository.save(updatedAddress);
        }
        
        // Update the pickup location
        PickupLocation updatedPickupLocation = new PickupLocation(pickupLocationRequestModel, getUser(), existingPickupLocation);
        pickupLocationRepository.save(updatedPickupLocation);
        
        // Update in ShipRocket
        ShippingHelper shippingHelper = getShippingHelper();
        shippingHelper.addPickupLocation(updatedPickupLocation);
        
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
        PickupLocation pickupLocation = pickupLocationRepository.findById(pickupLocationId)
            .orElseThrow(() -> new NotFoundException(String.format(ErrorMessages.PickupLocationErrorMessages.NotFound, pickupLocationId)));
        
        pickupLocation.setIsDeleted(!pickupLocation.getIsDeleted());
        pickupLocation.setModifiedBy(getUser());
        pickupLocationRepository.save(pickupLocation);
        
        // Log the toggle action
        userLogService.logData(getUserId(), SuccessMessages.PickupLocationSuccessMessages.TogglePickupLocation + " " + pickupLocation.getPickupLocationId(), ApiRoutes.PickupLocationsSubRoute.TOGGLE_PICKUP_LOCATION);
    }

    /**
     * Retrieves all pickup locations for the current client.
     * 
     * This method returns all pickup locations associated with the current client context.
     * Can optionally include deleted records based on the includeDeleted parameter.
     * Each pickup location includes its associated address information.
     * 
     * @param includeDeleted Whether to include deleted pickup locations in the result
     * @return List of pickup locations with their address information
     */
    @Override
    public List<PickupLocationResponseModel> getAllPickupLocations(boolean includeDeleted) {
        List<PickupLocation> pickupLocations = pickupLocationRepository.findAllWithAddresses(includeDeleted);
        
        return pickupLocations.stream()
            .map(pickupLocation -> new PickupLocationResponseModel(pickupLocation, pickupLocation.getAddress()))
            .collect(Collectors.toList());
    }
}