package springapi.services;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import springapi.ErrorMessages;
import springapi.SuccessMessages;
import springapi.authentication.JwtTokenProvider;
import springapi.exceptions.BadRequestException;
import springapi.exceptions.NotFoundException;
import springapi.filterquerybuilder.PickupLocationFilterQueryBuilder;
import springapi.helpers.BulkInsertHelper;
import springapi.helpers.ShipRocketHelper;
import springapi.models.ApiRoutes;
import springapi.models.databasemodels.Address;
import springapi.models.databasemodels.PackagePickupLocationMapping;
import springapi.models.databasemodels.PickupLocation;
import springapi.models.databasemodels.ProductPickupLocationMapping;
import springapi.models.requestmodels.AddressRequestModel;
import springapi.models.requestmodels.PackagePickupLocationMappingRequestModel;
import springapi.models.requestmodels.PaginationBaseRequestModel;
import springapi.models.requestmodels.PickupLocationRequestModel;
import springapi.models.requestmodels.ProductPickupLocationMappingRequestModel;
import springapi.models.responsemodels.BulkInsertResponseModel;
import springapi.models.responsemodels.ClientResponseModel;
import springapi.models.responsemodels.PaginationBaseResponseModel;
import springapi.models.responsemodels.PickupLocationResponseModel;
import springapi.models.shippingresponsemodel.AddPickupLocationResponseModel;
import springapi.repositories.AddressRepository;
import springapi.repositories.PackagePickupLocationMappingRepository;
import springapi.repositories.PickupLocationRepository;
import springapi.repositories.ProductPickupLocationMappingRepository;
import springapi.services.interfaces.PickupLocationSubTranslator;

/**
 * Service implementation for PickupLocation-related business operations.
 *
 * <p>This service implements the contract defined in PickupLocationSubTranslator for pickup
 * location management operations including CRUD operations, batch retrieval, and status management.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class PickupLocationService extends BaseService implements PickupLocationSubTranslator {

  private final PickupLocationRepository pickupLocationRepository;
  private final AddressRepository addressRepository;
  private final ProductPickupLocationMappingRepository productMappingRepository;
  private final PackagePickupLocationMappingRepository packageMappingRepository;
  private final UserLogService userLogService;
  private final ClientService clientService;
  private final PickupLocationFilterQueryBuilder pickupLocationFilterQueryBuilder;
  private final ShipRocketHelper shipRocketHelper;
  private final MessageService messageService;
  private static final String UNKNOWN_NAME = "unknown";

  /** Initializes PickupLocationService. */
  @Autowired
  public PickupLocationService(
      PickupLocationRepository pickupLocationRepository,
      AddressRepository addressRepository,
      ProductPickupLocationMappingRepository productMappingRepository,
      PackagePickupLocationMappingRepository packageMappingRepository,
      UserLogService userLogService,
      ClientService clientService,
      PickupLocationFilterQueryBuilder pickupLocationFilterQueryBuilder,
      MessageService messageService,
      JwtTokenProvider jwtTokenProvider,
      HttpServletRequest request) {
    super(jwtTokenProvider, request);
    this.pickupLocationRepository = pickupLocationRepository;
    this.addressRepository = addressRepository;
    this.productMappingRepository = productMappingRepository;
    this.packageMappingRepository = packageMappingRepository;
    this.userLogService = userLogService;
    this.clientService = clientService;
    this.shipRocketHelper = null;
    this.pickupLocationFilterQueryBuilder = pickupLocationFilterQueryBuilder;
    this.messageService = messageService;
  }

  // ============================================================================
  // Public Methods
  // ============================================================================

  /**
   * Retrieves pickup locations in paginated batches based on filter criteria.
   *
   * <p>This method supports filtering, sorting, and pagination of pickup locations. It can include
   * or exclude deleted records based on the request parameters.
   *
   * @param paginationBaseRequestModel The pagination and filter parameters
   * @return Paginated pickup location data
   * @throws BadRequestException if pagination parameters are invalid
   */
  @Override
  @Transactional(readOnly = true)
  public PaginationBaseResponseModel<PickupLocationResponseModel> getPickupLocationsInBatches(
      PaginationBaseRequestModel paginationBaseRequestModel) {
    if (paginationBaseRequestModel == null) {
      throw new BadRequestException(ErrorMessages.PickupLocationErrorMessages.INVALID_REQUEST);
    }

    // Validate pagination indices
    if (paginationBaseRequestModel.getStart() < 0) {
      throw new BadRequestException(
          ErrorMessages.CommonErrorMessages.START_INDEX_CANNOT_BE_NEGATIVE);
    }
    if (paginationBaseRequestModel.getEnd() <= 0) {
      throw new BadRequestException(
          ErrorMessages.CommonErrorMessages.END_INDEX_MUST_BE_GREATER_THAN_ZERO);
    }
    if (paginationBaseRequestModel.getStart() >= paginationBaseRequestModel.getEnd()) {
      throw new BadRequestException(
          ErrorMessages.CommonErrorMessages.START_INDEX_MUST_BE_LESS_THAN_END);
    }

    // Valid columns for filtering
    Set<String> validColumns =
        new HashSet<>(
            Arrays.asList(
                "pickupLocationId",
                "locationName",
                "addressNickName",
                "address",
                "isDeleted",
                "pickupLocationAddressId",
                "shipRocketPickupLocationId",
                "createdBy",
                "modifiedBy",
                "createdAt",
                "updatedAt",
                "notes"));

    // Validate filter conditions if provided
    if (paginationBaseRequestModel.getFilters() != null
        && !paginationBaseRequestModel.getFilters().isEmpty()) {
      for (PaginationBaseRequestModel.FilterCondition filter :
          paginationBaseRequestModel.getFilters()) {
        // Validate column name
        if (filter.getColumn() != null && !validColumns.contains(filter.getColumn())) {
          throw new BadRequestException(
              String.format(
                  ErrorMessages.PickupLocationErrorMessages.INVALID_COLUMN_NAME_FORMAT,
                  filter.getColumn()));
        }

        // Validate operator (FilterCondition.setOperator auto-normalizes symbols to
        // words)
        if (!filter.isValidOperator()) {
          throw new BadRequestException(
              String.format(
                  ErrorMessages.PickupLocationErrorMessages.INVALID_OPERATOR_FORMAT,
                  filter.getOperator()));
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
      throw new BadRequestException(ErrorMessages.CommonErrorMessages.INVALID_PAGINATION);
    }

    if (!paginationBaseRequestModel.isValidLogicOperator()) {
      throw new BadRequestException(ErrorMessages.CommonErrorMessages.INVALID_LOGIC_OPERATOR);
    }

    // Create custom Pageable with proper offset handling
    Pageable pageable =
        new PageRequest(0, pageSize, Sort.by("pickupLocationId").descending()) {
          @Override
          public long getOffset() {
            return start;
          }
        };

    // Use filter query builder for dynamic filtering
    String logicOperator =
        PaginationBaseRequestModel.LOGIC_OR.equalsIgnoreCase(
                paginationBaseRequestModel.getLogicOperator())
            ? PaginationBaseRequestModel.LOGIC_OR
            : PaginationBaseRequestModel.LOGIC_AND;

    Page<PickupLocation> result =
        pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            getClientId(),
            paginationBaseRequestModel.getSelectedIds(),
            logicOperator,
            paginationBaseRequestModel.getFilters(),
            paginationBaseRequestModel.isIncludeDeleted(),
            pageable);

    // Get all pickup location IDs from the result for batch count queries
    List<Long> pickupLocationIds =
        result.getContent().stream()
            .map(PickupLocation::getPickupLocationId)
            .collect(Collectors.toCollection(ArrayList::new));

    // Batch fetch product and package counts (2 queries instead of 2*N queries)
    Map<Long, Integer> productCountMap = new HashMap<>();
    Map<Long, Integer> packageCountMap = new HashMap<>();

    if (!pickupLocationIds.isEmpty()) {
      // Get product counts in a single query
      List<Object[]> productCounts =
          productMappingRepository.countByPickupLocationIds(pickupLocationIds);
      for (Object[] row : productCounts) {
        productCountMap.put((Long) row[0], ((Number) row[1]).intValue());
      }

      // Get package counts in a single query
      List<Object[]> packageCounts =
          packageMappingRepository.countByPickupLocationIds(pickupLocationIds);
      for (Object[] row : packageCounts) {
        packageCountMap.put((Long) row[0], ((Number) row[1]).intValue());
      }
    }

    // Convert PickupLocation results to PickupLocationResponseModel with counts
    PaginationBaseResponseModel<PickupLocationResponseModel> response =
        new PaginationBaseResponseModel<>();
    response.setData(
        result.getContent().stream()
            .map(
                pickupLocation -> {
                  PickupLocationResponseModel responseModel =
                      new PickupLocationResponseModel(pickupLocation);
                  // Set product and package counts from the maps (defaults to 0 if not found)
                  responseModel.setProductCount(
                      productCountMap.getOrDefault(pickupLocation.getPickupLocationId(), 0));
                  responseModel.setPackageCount(
                      packageCountMap.getOrDefault(pickupLocation.getPickupLocationId(), 0));
                  return responseModel;
                })
            .collect(Collectors.toCollection(ArrayList::new)));
    response.setTotalDataCount(result.getTotalElements());

    return response;
  }

  /**
   * Retrieves a specific pickup location by its unique identifier.
   *
   * <p>This method fetches a pickup location along with its associated address information. Throws
   * NotFoundException if the pickup location is not found.
   *
   * @param pickupLocationId The unique identifier of the pickup location
   * @return PickupLocationResponseModel containing the pickup location details
   * @throws NotFoundException if the pickup location is not found
   */
  @Override
  @Transactional(readOnly = true)
  public PickupLocationResponseModel getPickupLocationById(long pickupLocationId) {
    PickupLocation pickupLocation =
        pickupLocationRepository.findPickupLocationByIdAndClientId(pickupLocationId, getClientId());
    if (pickupLocation == null) {
      throw new NotFoundException(
          String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, pickupLocationId));
    }

    return new PickupLocationResponseModel(pickupLocation);
  }

  /**
   * Creates a new pickup location with associated address information.
   *
   * <p>This method validates the pickup location data, creates the address record, and establishes
   * the relationship between pickup location and address. It also integrates with ShipRocket API
   * for external shipping service coordination. All operations are performed within a transaction
   * for data consistency.
   *
   * @param pickupLocationRequestModel The pickup location data to create
   * @throws BadRequestException if the request model is invalid
   */
  @Override
  @Transactional
  public void createPickupLocation(PickupLocationRequestModel pickupLocationRequestModel) {
    // Validate request
    validatePickupLocationRequest(pickupLocationRequestModel, true);

    // Create the address first
    Address address = new Address(pickupLocationRequestModel.getAddress(), getUser());
    address = addressRepository.save(address);

    // Set the address ID in the request
    pickupLocationRequestModel.setPickupLocationAddressId(address.getAddressId());

    // Create the pickup location
    PickupLocation pickupLocation =
        new PickupLocation(pickupLocationRequestModel, getUser(), getClientId());

    // Set the address on the pickup location for ShipRocket API call
    // (The @ManyToOne relationship isn't loaded automatically)
    pickupLocation.setAddress(address);

    pickupLocation = pickupLocationRepository.save(pickupLocation);

    // Call ShipRocket to create pickup location
    ShipRocketHelper shipRocketHelperInstance = getShipRocketHelper();
    AddPickupLocationResponseModel addPickupLocationResponse =
        shipRocketHelperInstance.addPickupLocation(pickupLocation);

    // Extract and set the ShipRocket ID
    Long shipRocketPickupLocationId = extractShipRocketPickupLocationId(addPickupLocationResponse);
    pickupLocation.setShipRocketPickupLocationId(shipRocketPickupLocationId);
    pickupLocation = pickupLocationRepository.save(pickupLocation);

    // Save product mappings if provided
    saveProductMappings(
        pickupLocation.getPickupLocationId(), pickupLocationRequestModel.getProductMappings());

    // Save package mappings if provided
    savePackageMappings(
        pickupLocation.getPickupLocationId(), pickupLocationRequestModel.getPackageMappings());

    // Log the creation
    userLogService.logData(
        getUserId(),
        SuccessMessages.PickupLocationSuccessMessages.INSERT_PICKUP_LOCATION
            + " "
            + pickupLocation.getPickupLocationId(),
        ApiRoutes.PickupLocationsSubRoute.CREATE_PICKUP_LOCATION);
  }

  /**
   * Updates an existing pickup location with new information.
   *
   * <p>This method validates the updated data, modifies the pickup location record, and updates
   * associated address information. It maintains audit trail information and integrates with
   * ShipRocket API for external shipping service coordination. All operations are performed within
   * a transaction for data consistency.
   *
   * @param pickupLocationRequestModel The updated pickup location data
   * @throws NotFoundException if the pickup location or address is not found
   * @throws BadRequestException if the request model is invalid
   */
  @Override
  @Transactional
  public void updatePickupLocation(PickupLocationRequestModel pickupLocationRequestModel) {
    // Validate request
    validatePickupLocationRequest(pickupLocationRequestModel, false);

    // Get the existing pickup location
    PickupLocation existingPickupLocation =
        pickupLocationRepository.findPickupLocationByIdAndClientId(
            pickupLocationRequestModel.getPickupLocationId(), getClientId());
    if (existingPickupLocation == null) {
      throw new NotFoundException(
          String.format(
              ErrorMessages.PickupLocationErrorMessages.NOT_FOUND,
              pickupLocationRequestModel.getPickupLocationId()));
    }

    // Get the existing address for comparison
    Address existingAddress =
        addressRepository
            .findById(existingPickupLocation.getPickupLocationAddressId())
            .orElseThrow(() -> new NotFoundException(ErrorMessages.AddressErrorMessages.NOT_FOUND));

    // Check if physical address fields have changed (requires new Shiprocket
    // location)
    final boolean addressFieldsChanged =
        hasAddressFieldsChanged(existingAddress, pickupLocationRequestModel.getAddress());

    // Update the address if provided
    Address updatedAddress = null;
    if (pickupLocationRequestModel.getAddress() != null) {
      updatedAddress =
          new Address(pickupLocationRequestModel.getAddress(), getUser(), existingAddress);
      updatedAddress = addressRepository.save(updatedAddress);
    } else {
      updatedAddress = existingAddress;
    }

    // Set the address ID on the request model for validation
    pickupLocationRequestModel.setPickupLocationAddressId(updatedAddress.getAddressId());

    // Update the pickup location
    PickupLocation updatedPickupLocation =
        new PickupLocation(pickupLocationRequestModel, getUser(), existingPickupLocation);

    // Set the address on the pickup location for ShipRocket API call
    // (The @ManyToOne relationship isn't loaded automatically)
    updatedPickupLocation.setAddress(updatedAddress);

    // Only call Shiprocket if physical address fields have changed
    if (addressFieldsChanged) {
      // Create new pickup location in ShipRocket and get new ID
      ShipRocketHelper shipRocketHelperInstance = getShipRocketHelper();
      AddPickupLocationResponseModel addPickupLocationResponse =
          shipRocketHelperInstance.addPickupLocation(updatedPickupLocation);
      Long shipRocketPickupLocationId =
          extractShipRocketPickupLocationId(addPickupLocationResponse);
      updatedPickupLocation.setShipRocketPickupLocationId(shipRocketPickupLocationId);
    } else {
      // Keep the existing Shiprocket ID
      updatedPickupLocation.setShipRocketPickupLocationId(
          existingPickupLocation.getShipRocketPickupLocationId());
    }

    // Save the updated pickup location to database
    PickupLocation savedPickupLocation = pickupLocationRepository.save(updatedPickupLocation);
    updatedPickupLocation =
        resolveUpdatedPickupLocation(savedPickupLocation, existingPickupLocation);

    // Update product mappings if provided (delete existing and recreate)
    if (pickupLocationRequestModel.getProductMappings() != null) {
      productMappingRepository.deleteByPickupLocationId(
          updatedPickupLocation.getPickupLocationId());
      saveProductMappings(
          updatedPickupLocation.getPickupLocationId(),
          pickupLocationRequestModel.getProductMappings());
    }

    // Update package mappings if provided (delete existing and recreate)
    if (pickupLocationRequestModel.getPackageMappings() != null) {
      packageMappingRepository.deleteByPickupLocationId(
          updatedPickupLocation.getPickupLocationId());
      savePackageMappings(
          updatedPickupLocation.getPickupLocationId(),
          pickupLocationRequestModel.getPackageMappings());
    }

    // Log the update
    userLogService.logData(
        getUserId(),
        SuccessMessages.PickupLocationSuccessMessages.UPDATE_PICKUP_LOCATION
            + " "
            + updatedPickupLocation.getPickupLocationId(),
        ApiRoutes.PickupLocationsSubRoute.UPDATE_PICKUP_LOCATION);
  }

  /**
   * Toggles the deletion status of a pickup location by its ID.
   *
   * <p>This method performs a soft delete operation by toggling the isDeleted flag. If the pickup
   * location is currently active, it will be marked as deleted. If the pickup location is currently
   * deleted, it will be restored. Updates the modified timestamp and user information.
   *
   * @param pickupLocationId The unique identifier of the pickup location to toggle
   * @throws NotFoundException if the pickup location is not found
   */
  @Override
  @Transactional
  public void togglePickupLocation(long pickupLocationId) {
    PickupLocation pickupLocation =
        pickupLocationRepository.findPickupLocationByIdAndClientId(pickupLocationId, getClientId());
    if (pickupLocation == null) {
      throw new NotFoundException(
          String.format(ErrorMessages.PickupLocationErrorMessages.NOT_FOUND, pickupLocationId));
    }

    pickupLocation.setIsDeleted(!pickupLocation.getIsDeleted());
    pickupLocation.setModifiedBy(getUser());
    pickupLocationRepository.save(pickupLocation);

    // Log the toggle action
    userLogService.logData(
        getUserId(),
        SuccessMessages.PickupLocationSuccessMessages.TOGGLE_PICKUP_LOCATION
            + " "
            + pickupLocation.getPickupLocationId(),
        ApiRoutes.PickupLocationsSubRoute.TOGGLE_PICKUP_LOCATION);
  }

  /**
   * Creates multiple pickup locations asynchronously in a single operation. Processing happens in.
   * background thread; results sent via message notification.
   *
   * <p>Uses @Async for non-blocking processing and: - NOT_SUPPORTED: Runs without a transaction to
   * avoid rollback-only issues when individual creations fail
   *
   * @param pickupLocations List of PickupLocationRequestModel containing the pickup location data
   *     to create
   * @param requestingUserId The ID of the user making the request (captured from security context)
   * @param requestingUserLoginName The loginName of the user making the request (captured from
   *     security context)
   * @param requestingClientId The client ID of the user making the request (captured from security
   *     context)
   */
  @Override
  @Async
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void bulkCreatePickupLocationsAsync(
      List<PickupLocationRequestModel> pickupLocations,
      Long requestingUserId,
      String requestingUserLoginName,
      Long requestingClientId) {
    try {
      // Validate input
      if (pickupLocations == null || pickupLocations.isEmpty()) {
        throw new BadRequestException(
            String.format(
                ErrorMessages.CommonErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY, "Pickup location"));
      }

      BulkInsertResponseModel<Long> response = new BulkInsertResponseModel<>();
      response.setTotalRequested(pickupLocations.size());

      int successCount = 0;
      int failureCount = 0;

      // Process each pickup location individually
      for (PickupLocationRequestModel pickupLocationRequest : pickupLocations) {
        try {
          // Call createPickupLocationInternal with explicit createdUser
          Long createdId =
              createPickupLocationInternal(
                  pickupLocationRequest, requestingUserLoginName, requestingClientId);

          if (createdId != null) {
            response.addSuccess(pickupLocationRequest.getAddressNickName(), createdId);
            successCount++;
          }

        } catch (BadRequestException badRequestException) {
          // Validation or business logic error
          response.addFailure(
              pickupLocationRequest.getAddressNickName() != null
                  ? pickupLocationRequest.getAddressNickName()
                  : UNKNOWN_NAME,
              badRequestException.getMessage());
          failureCount++;
        } catch (Exception exception) {
          // Unexpected error
          response.addFailure(
              pickupLocationRequest.getAddressNickName() != null
                  ? pickupLocationRequest.getAddressNickName()
                  : UNKNOWN_NAME,
              "Error: " + exception.getMessage());
          failureCount++;
        }
      }

      // Log bulk pickup location creation (using captured context values)
      userLogService.logDataWithContext(
          requestingUserId,
          requestingUserLoginName,
          requestingClientId,
          SuccessMessages.PickupLocationSuccessMessages.INSERT_PICKUP_LOCATION
              + " (Bulk: "
              + successCount
              + " succeeded, "
              + failureCount
              + " failed)",
          ApiRoutes.PickupLocationsSubRoute.BULK_CREATE_PICKUP_LOCATION);

      response.setSuccessCount(successCount);
      response.setFailureCount(failureCount);

      // Create a message with the bulk insert results using the helper (using
      // captured context)
      BulkInsertHelper.createDetailedBulkInsertResultMessage(
          response,
          new BulkInsertHelper.BulkMessageTemplate(
              "Pickup Location", "Pickup Locations", "Location Name", "Pickup Location ID"),
          new BulkInsertHelper.NotificationContext(
              messageService, requestingUserId, requestingUserLoginName, requestingClientId));

    } catch (Exception exception) {
      // Still send a message to user about the failure (using captured userId)
      BulkInsertResponseModel<Long> errorResponse = new BulkInsertResponseModel<>();
      errorResponse.setTotalRequested(pickupLocations != null ? pickupLocations.size() : 0);
      errorResponse.setSuccessCount(0);
      errorResponse.setFailureCount(pickupLocations != null ? pickupLocations.size() : 0);
      errorResponse.addFailure("bulk_import", "Critical error: " + exception.getMessage());
      BulkInsertHelper.createDetailedBulkInsertResultMessage(
          errorResponse,
          new BulkInsertHelper.BulkMessageTemplate(
              "Pickup Location", "Pickup Locations", "Location Name", "Pickup Location ID"),
          new BulkInsertHelper.NotificationContext(
              messageService, requestingUserId, requestingUserLoginName, requestingClientId));
    }
  }

  /**
   * Creates multiple pickup locations synchronously in a single operation (for testing). This is a.
   * synchronous wrapper that processes pickup locations immediately and returns results.
   *
   * @param pickupLocations List of PickupLocationRequestModel containing the pickup location data
   *     to create
   * @return BulkInsertResponseModel containing success/failure details for each pickup location
   */
  @Override
  @Transactional
  public BulkInsertResponseModel<Long> bulkCreatePickupLocations(
      List<PickupLocationRequestModel> pickupLocations) {
    // Validate input
    if (pickupLocations == null || pickupLocations.isEmpty()) {
      throw new BadRequestException(
          String.format(
              ErrorMessages.CommonErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY, "Pickup location"));
    }

    BulkInsertResponseModel<Long> response = new BulkInsertResponseModel<>();
    response.setTotalRequested(pickupLocations.size());

    int successCount = 0;
    int failureCount = 0;

    // Process each pickup location individually
    for (PickupLocationRequestModel pickupLocationRequest : pickupLocations) {
      try {
        // Call createPickupLocationInternal with current user
        Long createdId =
            createPickupLocationInternal(pickupLocationRequest, getUser(), getClientId());

        if (createdId != null) {
          response.addSuccess(pickupLocationRequest.getAddressNickName(), createdId);
          successCount++;
        }

      } catch (BadRequestException badRequestException) {
        // Validation or business logic error
        response.addFailure(
            pickupLocationRequest.getAddressNickName() != null
                ? pickupLocationRequest.getAddressNickName()
                : UNKNOWN_NAME,
            badRequestException.getMessage());
        failureCount++;
      } catch (Exception exception) {
        // Unexpected error
        response.addFailure(
            pickupLocationRequest.getAddressNickName() != null
                ? pickupLocationRequest.getAddressNickName()
                : UNKNOWN_NAME,
            "Error: " + exception.getMessage());
        failureCount++;
      }
    }

    // Log bulk pickup location creation
    userLogService.logData(
        getUserId(),
        SuccessMessages.PickupLocationSuccessMessages.INSERT_PICKUP_LOCATION
            + " (Bulk: "
            + successCount
            + " succeeded, "
            + failureCount
            + " failed)",
        ApiRoutes.PickupLocationsSubRoute.BULK_CREATE_PICKUP_LOCATION);

    response.setSuccessCount(successCount);
    response.setFailureCount(failureCount);

    return response;
  }

  // ============================================================================
  // Private Helper Methods
  // ============================================================================

  /**
   * Creates a ShipRocketHelper instance initialized with the current client's ShipRocket.
   * credentials. For testing, returns the injected mock if available.
   *
   * @return ShipRocketHelper instance with client credentials
   */
  private ShipRocketHelper getShipRocketHelper() {
    if (shipRocketHelper != null) {
      return shipRocketHelper; // For testing with mock
    }
    ClientResponseModel client = clientService.getClientById(getClientId());
    return new ShipRocketHelper(client.getShipRocketEmail(), client.getShipRocketPassword());
  }

  /**
   * Creates a ShipRocketHelper instance initialized with a specific client's ShipRocket.
   * credentials. Used for bulk operations where clientId is passed explicitly.
   *
   * @param clientId The client ID to get credentials for
   * @return ShipRocketHelper instance with client credentials
   */
  private ShipRocketHelper getShipRocketHelper(Long clientId) {
    if (shipRocketHelper != null) {
      return shipRocketHelper; // For testing with mock
    }
    ClientResponseModel client = clientService.getClientById(clientId);
    return new ShipRocketHelper(client.getShipRocketEmail(), client.getShipRocketPassword());
  }

  /**
   * Checks if physical address fields have changed between existing and new address. Only compares.
   * fields that require a new Shiprocket pickup location: streetAddress, streetAddress2,
   * streetAddress3, city, state, country, postalCode
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
    java.util.function.BiPredicate<String, String> isDifferent =
        (existing, updated) -> {
          if (existing == null && updated == null) {
            return false;
          }
          if (existing == null || updated == null) {
            return true;
          }
          return !existing.equals(updated);
        };

    // Compare physical address fields only
    return isDifferent.test(existingAddress.getStreetAddress(), newAddress.getStreetAddress())
        || isDifferent.test(existingAddress.getStreetAddress2(), newAddress.getStreetAddress2())
        || isDifferent.test(existingAddress.getStreetAddress3(), newAddress.getStreetAddress3())
        || isDifferent.test(existingAddress.getCity(), newAddress.getCity())
        || isDifferent.test(existingAddress.getState(), newAddress.getState())
        || isDifferent.test(existingAddress.getCountry(), newAddress.getCountry())
        || isDifferent.test(existingAddress.getPostalCode(), newAddress.getPostalCode());
  }

  /**
   * Internal method to create a pickup location with explicit user and client context. Used for.
   * bulk operations where security context may not be available.
   *
   * @param pickupLocationRequestModel The pickup location data to create
   * @param createdUser The username to set as creator
   * @param clientId The client ID for this operation
   * @return The created pickup location ID
   */
  private Long createPickupLocationInternal(
      PickupLocationRequestModel pickupLocationRequestModel, String createdUser, Long clientId) {
    // Validate request
    validatePickupLocationRequest(pickupLocationRequestModel, true);

    // Create the address first
    Address address = new Address(pickupLocationRequestModel.getAddress(), createdUser);
    address = addressRepository.save(address);

    // Set the address ID in the request
    pickupLocationRequestModel.setPickupLocationAddressId(address.getAddressId());

    // Create the pickup location
    PickupLocation pickupLocation =
        new PickupLocation(pickupLocationRequestModel, createdUser, clientId);

    // Set the address on the pickup location for ShipRocket API call
    pickupLocation.setAddress(address);

    pickupLocation = pickupLocationRepository.save(pickupLocation);

    // Call ShipRocket to create pickup location
    ShipRocketHelper shipRocketHelperInstance = getShipRocketHelper(clientId);
    AddPickupLocationResponseModel addPickupLocationResponse =
        shipRocketHelperInstance.addPickupLocation(pickupLocation);

    // Extract and set the ShipRocket ID
    Long shipRocketPickupLocationId = extractShipRocketPickupLocationId(addPickupLocationResponse);
    pickupLocation.setShipRocketPickupLocationId(shipRocketPickupLocationId);
    pickupLocationRepository.save(pickupLocation);

    // Save product and package mappings (for bulk import)
    saveProductMappingsInternal(
        pickupLocation.getPickupLocationId(),
        pickupLocationRequestModel.getProductMappings(),
        createdUser);
    savePackageMappingsInternal(
        pickupLocation.getPickupLocationId(),
        pickupLocationRequestModel.getPackageMappings(),
        createdUser);

    return pickupLocation.getPickupLocationId();
  }

  private PickupLocation resolveUpdatedPickupLocation(
      PickupLocation savedPickupLocation, PickupLocation existingPickupLocation) {
    if (savedPickupLocation != null) {
      return savedPickupLocation;
    }
    return existingPickupLocation;
  }

  /**
   * Extracts the ShipRocket pickup location ID from the API response.
   *
   * @param addPickupLocationResponse The response from adding pickup location
   * @return The ShipRocket pickup location ID from pickupId field
   * @throws BadRequestException if pickupId is invalid or missing
   */
  private Long extractShipRocketPickupLocationId(
      AddPickupLocationResponseModel addPickupLocationResponse) {

    // Extract ID from pickupId field only
    long pickupId = addPickupLocationResponse.getPickupId();

    // Validate that we have a valid ShipRocket pickup location ID
    if (pickupId <= 0) {
      throw new BadRequestException(
          String.format(
              ErrorMessages.PickupLocationErrorMessages
                  .SHIP_ROCKET_PICKUP_LOCATION_ID_INVALID_FORMAT,
              pickupId));
    }

    return pickupId;
  }

  /**
   * Saves product pickup location mappings.
   *
   * @param pickupLocationId The pickup location ID
   * @param productMappings List of product mappings to save
   */
  private void saveProductMappings(
      Long pickupLocationId, List<ProductPickupLocationMappingRequestModel> productMappings) {
    if (productMappings == null || productMappings.isEmpty()) {
      return;
    }

    String createdUser = getUser();
    for (ProductPickupLocationMappingRequestModel mapping : productMappings) {
      if (mapping.getProductId() == null
          || mapping.getQuantity() == null
          || mapping.getQuantity() < 1) {
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
  private void savePackageMappings(
      Long pickupLocationId, List<PackagePickupLocationMappingRequestModel> packageMappings) {
    if (packageMappings == null || packageMappings.isEmpty()) {
      return;
    }

    String createdUser = getUser();
    for (PackagePickupLocationMappingRequestModel mapping : packageMappings) {
      if (mapping.getPackageId() == null
          || mapping.getQuantity() == null
          || mapping.getQuantity() < 1) {
        continue; // Skip invalid mappings
      }

      PackagePickupLocationMapping entity = new PackagePickupLocationMapping();
      entity.setPackageId(mapping.getPackageId());
      entity.setPickupLocationId(pickupLocationId);
      entity.setAvailableQuantity(mapping.getQuantity());
      entity.setReorderLevel(mapping.getReorderLevel() != null ? mapping.getReorderLevel() : 1);
      entity.setMaxStockLevel(
          mapping.getMaxStockLevel() != null
              ? mapping.getMaxStockLevel()
              : mapping.getQuantity() * 2);
      entity.setLastRestockDate(LocalDateTime.now());
      entity.setCreatedUser(createdUser);
      entity.setModifiedUser(createdUser);

      packageMappingRepository.save(entity);
    }
  }

  /** Common validation for pickup location requests. */
  private void validatePickupLocationRequest(PickupLocationRequestModel request, boolean isNew) {
    if (request == null) {
      throw new BadRequestException(ErrorMessages.PickupLocationErrorMessages.INVALID_REQUEST);
    }

    if (request.getAddressNickName() == null || request.getAddressNickName().trim().isEmpty()) {
      throw new BadRequestException(
          ErrorMessages.PickupLocationErrorMessages.INVALID_ADDRESS_NICK_NAME);
    }

    if (request.getAddress() == null) {
      throw new BadRequestException(ErrorMessages.AddressErrorMessages.ER001);
    }

    AddressRequestModel address = request.getAddress();
    if (address.getStreetAddress() == null || address.getStreetAddress().trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.AddressErrorMessages.ER001);
    }
    if (address.getCity() == null || address.getCity().trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.AddressErrorMessages.ER002);
    }
    if (address.getState() == null || address.getState().trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.AddressErrorMessages.ER003);
    }
    if (address.getPostalCode() == null || address.getPostalCode().trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.AddressErrorMessages.ER004);
    }
    if (address.getCountry() == null || address.getCountry().trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.AddressErrorMessages.ER005);
    }

    if (!isNew && (request.getPickupLocationId() == null || request.getPickupLocationId() <= 0)) {
      // Usually internal logic errors don't throw BadRequestException but let's be
      // safe
    }
  }

  /** Internal method to save product mappings with explicit createdUser (for bulk import). */
  private void saveProductMappingsInternal(
      Long pickupLocationId,
      List<ProductPickupLocationMappingRequestModel> productMappings,
      String createdUser) {
    if (productMappings == null || productMappings.isEmpty()) {
      return;
    }

    for (ProductPickupLocationMappingRequestModel mapping : productMappings) {
      if (mapping.getProductId() == null
          || mapping.getQuantity() == null
          || mapping.getQuantity() < 1) {
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

  /** Internal method to save package mappings with explicit createdUser (for bulk import). */
  private void savePackageMappingsInternal(
      Long pickupLocationId,
      List<PackagePickupLocationMappingRequestModel> packageMappings,
      String createdUser) {
    if (packageMappings == null || packageMappings.isEmpty()) {
      return;
    }

    for (PackagePickupLocationMappingRequestModel mapping : packageMappings) {
      if (mapping.getPackageId() == null
          || mapping.getQuantity() == null
          || mapping.getQuantity() < 1) {
        continue; // Skip invalid mappings
      }

      PackagePickupLocationMapping entity = new PackagePickupLocationMapping();
      entity.setPackageId(mapping.getPackageId());
      entity.setPickupLocationId(pickupLocationId);
      entity.setAvailableQuantity(mapping.getQuantity());
      entity.setReorderLevel(mapping.getReorderLevel() != null ? mapping.getReorderLevel() : 1);
      entity.setMaxStockLevel(
          mapping.getMaxStockLevel() != null
              ? mapping.getMaxStockLevel()
              : mapping.getQuantity() * 2);
      entity.setLastRestockDate(LocalDateTime.now());
      entity.setCreatedUser(createdUser);
      entity.setModifiedUser(createdUser);

      packageMappingRepository.save(entity);
    }
  }
}
