package com.example.SpringApi.Services.Interface;

import com.example.SpringApi.Models.RequestModels.ClientRequestModel;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import java.util.List;

/**
 * Interface for Client-related business operations.
 *
 * <p>This interface defines the contract for client management operations including CRUD
 * operations, client retrieval, and client status management. All implementations should handle
 * proper validation, error handling, and logging.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface IClientSubTranslator {

  /**
   * Toggles the deletion status of a client by its ID.
   *
   * <p>This method performs a soft delete operation by toggling the isDeleted flag. If the client
   * is currently active (isDeleted = false), it will be marked as deleted. If the client is
   * currently deleted (isDeleted = true), it will be restored.
   *
   * @param id The unique identifier of the client to toggle
   * @throws NotFoundException if the client was not found
   * @throws IllegalArgumentException if the provided ID is null or invalid
   */
  void toggleClient(long id);

  /**
   * Retrieves a single client by its unique identifier.
   *
   * <p>This method fetches a client from the database using the provided ID. The returned
   * ClientResponseModel contains all client details including name, description, integration
   * settings, and metadata.
   *
   * @param id The unique identifier of the client to retrieve
   * @return ClientResponseModel containing the client information
   * @throws NotFoundException if no client exists with the given ID
   * @throws IllegalArgumentException if the provided ID is null or invalid
   */
  ClientResponseModel getClientById(long id);

  /**
   * Creates a new client in the system.
   *
   * <p>This method validates the provided client data, creates a new Client entity, and persists it
   * to the database. The method automatically sets audit fields such as createdUser, modifiedUser,
   * and timestamps.
   *
   * @param client The ClientRequestModel containing the client data to insert
   * @throws BadRequestException if the client data is invalid or incomplete
   * @throws IllegalArgumentException if the client parameter is null
   */
  void createClient(ClientRequestModel client);

  /**
   * Updates an existing client with new information.
   *
   * <p>This method retrieves the existing client by ID, validates the new data, and updates the
   * client while preserving audit information like createdUser and createdAt. Only the modifiedUser
   * and updatedAt fields are updated.
   *
   * @param client The ClientRequestModel containing the updated client data
   * @throws IllegalArgumentException if the client parameter is null
   */
  void updateClient(ClientRequestModel client);

  /**
   * Retrieves all clients mapped to the current user.
   *
   * <p>This method fetches all clients where the current user has a mapping in the
   * UserClientMapping table. The method returns a list of ClientResponseModel objects, each
   * containing complete client information. Returns an empty list if no clients are mapped to the
   * user.
   *
   * @return List of ClientResponseModel objects for the user's mapped clients
   */
  List<ClientResponseModel> getClientsByUser();
}
