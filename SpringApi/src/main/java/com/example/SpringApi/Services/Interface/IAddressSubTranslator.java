package com.example.SpringApi.Services.Interface;

import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.ResponseModels.AddressResponseModel;
import java.util.List;

/**
 * Interface for Address-related business operations.
 *
 * <p>This interface defines the contract for address management operations including CRUD
 * operations, user/client address retrieval, and address status management. All implementations
 * should handle proper validation, error handling, and logging.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface IAddressSubTranslator {

  /**
   * Toggles the deletion status of an address by its ID.
   *
   * <p>This method performs a soft delete operation by toggling the isDeleted flag. If the address
   * is currently active (isDeleted = false), it will be marked as deleted. If the address is
   * currently deleted (isDeleted = true), it will be restored.
   *
   * @param id The unique identifier of the address to toggle
   * @throws NotFoundException if the address was not found
   * @throws IllegalArgumentException if the provided ID is null or invalid
   */
  void toggleAddress(long id);

  /**
   * Retrieves a single address by its unique identifier.
   *
   * <p>This method fetches an address from the database using the provided ID. The returned
   * AddressResponseModel contains all address details including street address, city, state, postal
   * code, country, and metadata.
   *
   * @param id The unique identifier of the address to retrieve
   * @return AddressResponseModel containing the address information
   * @throws NotFoundException if no address exists with the given ID
   * @throws IllegalArgumentException if the provided ID is null or invalid
   */
  AddressResponseModel getAddressById(long id);

  /**
   * Creates a new address in the system.
   *
   * <p>This method validates the provided address data, creates a new Address entity, and persists
   * it to the database. The method automatically sets audit fields such as createdUser,
   * modifiedUser, and timestamps.
   *
   * @param address The AddressRequestModel containing the address data to insert
   * @throws BadRequestException if the address data is invalid or incomplete
   * @throws IllegalArgumentException if the address parameter is null
   */
  void insertAddress(AddressRequestModel address);

  /**
   * Updates an existing address with new information.
   *
   * <p>This method retrieves the existing address by ID, validates the new data, and updates the
   * address while preserving audit information like createdUser and createdAt. Only the
   * modifiedUser and updatedAt fields are updated.
   *
   * @param address The AddressRequestModel containing the updated address data
   * @throws NotFoundException if no address exists with the given ID
   * @throws BadRequestException if the address data is invalid or incomplete
   * @throws IllegalArgumentException if the address parameter is null
   */
  void updateAddress(AddressRequestModel address);

  /**
   * Retrieves all addresses associated with a specific user.
   *
   * <p>This method fetches all addresses where the userId matches the provided parameter. The
   * method returns a list of AddressResponseModel objects, each containing complete address
   * information. Returns an empty list if no addresses are found.
   *
   * @param id The unique identifier of the user
   * @return List of AddressResponseModel objects for the user
   * @throws IllegalArgumentException if the provided ID is null or invalid
   */
  List<AddressResponseModel> getAddressByUserId(long id);

  /**
   * Retrieves all addresses associated with a specific client.
   *
   * <p>This method fetches all addresses where the clientId matches the provided parameter. The
   * method returns a list of AddressResponseModel objects, each containing complete address
   * information. Returns an empty list if no addresses are found.
   *
   * @param id The unique identifier of the client
   * @return List of AddressResponseModel objects for the client
   * @throws IllegalArgumentException if the provided ID is null or invalid
   */
  List<AddressResponseModel> getAddressByClientId(long id);
}
