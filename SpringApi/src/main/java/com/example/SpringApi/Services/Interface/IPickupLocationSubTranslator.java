package com.example.SpringApi.Services.Interface;

import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.PickupLocationRequestModel;
import com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.PickupLocationResponseModel;
import java.util.List;

/**
 * Interface for PickupLocation-related business operations.
 *
 * <p>This interface defines the contract for pickup location management operations including CRUD
 * operations, batch retrieval, and status management. All implementations should handle proper
 * validation, error handling, and logging.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface IPickupLocationSubTranslator {

  /**
   * Retrieves pickup locations in paginated batches based on filter criteria.
   *
   * <p>This method supports filtering, sorting, and pagination of pickup locations. It can include
   * or exclude deleted records based on the request parameters.
   *
   * @param paginationBaseRequestModel The pagination and filter parameters
   * @return Paginated pickup location data
   * @throws IllegalArgumentException if pagination parameters are invalid
   */
  PaginationBaseResponseModel<PickupLocationResponseModel> getPickupLocationsInBatches(
      PaginationBaseRequestModel paginationBaseRequestModel);

  /**
   * Retrieves a specific pickup location by its unique identifier.
   *
   * <p>This method fetches a pickup location along with its associated address information. Returns
   * null if the pickup location is not found or if it's marked as deleted.
   *
   * @param pickupLocationId The unique identifier of the pickup location
   * @return PickupLocationResponseModel containing the pickup location details
   * @throws IllegalArgumentException if the provided ID is null or invalid
   */
  PickupLocationResponseModel getPickupLocationById(long pickupLocationId);

  /**
   * Creates a new pickup location with associated address information.
   *
   * <p>This method validates the pickup location data, creates the address record, and establishes
   * the relationship between pickup location and address. It also integrates with external shipping
   * services if configured.
   *
   * @param pickupLocationRequestModel The pickup location data to create
   * @throws IllegalArgumentException if the request model is invalid
   */
  void createPickupLocation(PickupLocationRequestModel pickupLocationRequestModel);

  /**
   * Updates an existing pickup location with new information.
   *
   * <p>This method validates the updated data, modifies the pickup location record, and updates
   * associated address information. It maintains audit trail information.
   *
   * @param pickupLocationRequestModel The updated pickup location data
   * @throws IllegalArgumentException if the request model is invalid
   */
  void updatePickupLocation(PickupLocationRequestModel pickupLocationRequestModel);

  /**
   * Toggles the deletion status of a pickup location by its ID.
   *
   * <p>This method performs a soft delete operation by toggling the isDeleted flag. If the pickup
   * location is currently active, it will be marked as deleted. If the pickup location is currently
   * deleted, it will be restored.
   *
   * @param pickupLocationId The unique identifier of the pickup location to toggle
   * @throws IllegalArgumentException if the provided ID is null or invalid
   * @throws NotFoundException if the pickup location is not found
   */
  void togglePickupLocation(long pickupLocationId);

  /**
   * Creates multiple pickup locations asynchronously in a single operation. Processing happens in
   * background thread; results sent via message notification.
   *
   * @param pickupLocations List of PickupLocationRequestModel containing the pickup location data
   *     to create
   * @param requestingUserId The ID of the user making the request (captured from security context)
   * @param requestingUserLoginName The loginName of the user making the request (captured from
   *     security context)
   * @param requestingClientId The client ID of the user making the request (captured from security
   *     context)
   */
  void bulkCreatePickupLocationsAsync(
      List<PickupLocationRequestModel> pickupLocations,
      Long requestingUserId,
      String requestingUserLoginName,
      Long requestingClientId);

  /**
   * Creates multiple pickup locations synchronously in a single operation (for testing). This is a
   * synchronous wrapper that processes pickup locations immediately and returns results.
   *
   * @param pickupLocations List of PickupLocationRequestModel containing the pickup location data
   *     to create
   * @return BulkInsertResponseModel containing success/failure details for each pickup location
   */
  BulkInsertResponseModel<Long> bulkCreatePickupLocations(
      List<PickupLocationRequestModel> pickupLocations);
}

