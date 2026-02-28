package springapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springapi.ErrorMessages;
import springapi.exceptions.BadRequestException;
import springapi.exceptions.NotFoundException;
import springapi.logging.ContextualLogger;
import springapi.models.ApiRoutes;
import springapi.models.Authorizations;
import springapi.models.requestmodels.ClientRequestModel;
import springapi.models.responsemodels.ErrorResponseModel;
import springapi.services.interfaces.ClientSubTranslator;

/**
 * REST Controller for managing Client-related operations.
 *
 * <p>This controller provides RESTful endpoints for client management including CRUD operations,
 * client retrieval, and client status management. All endpoints are secured with appropriate
 * authorization checks and include comprehensive error handling with contextual logging.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.CLIENT)
public class ClientController {
  private static final ContextualLogger logger = ContextualLogger.getLogger(ClientController.class);
  private final ClientSubTranslator clientService;

  @Autowired
  public ClientController(ClientSubTranslator clientService) {
    this.clientService = clientService;
  }

  /**
   * Retrieves a single client by its unique identifier.
   *
   * <p>This endpoint fetches a client from the database using the provided ID. The response
   * contains all client details including name, description, integration settings, and metadata.
   * Requires VIEW_CLIENT_PERMISSION.
   *
   * @param id The unique identifier of the client to retrieve
   * @return ResponseEntity containing ClientResponseModel or ErrorResponseModel
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.VIEW_CLIENT_PERMISSION + "')")
  @GetMapping("/" + ApiRoutes.ClientSubRoute.GET_CLIENT_BY_ID + "/{id}")
  public ResponseEntity<?> getClientById(@PathVariable Long id) {
    try {
      return ResponseEntity.ok(clientService.getClientById(id));
    } catch (BadRequestException bre) {
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      logger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /** Returns clients by user. */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.VIEW_CLIENT_PERMISSION + "')")
  @GetMapping("/" + ApiRoutes.ClientSubRoute.GET_CLIENTS_BY_USER)
  public ResponseEntity<?> getClientsByUser() {
    try {
      return ResponseEntity.ok(clientService.getClientsByUser());
    } catch (BadRequestException bre) {
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      logger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Creates a new client in the system.
   *
   * <p>This endpoint validates the provided client data, creates a new Client entity, and persists
   * it to the database. The method automatically sets audit fields such as createdUser,
   * modifiedUser, and timestamps. Requires INSERT_CLIENT_PERMISSION.
   *
   * @param clientRequest The ClientRequestModel containing the client data to create
   * @return ResponseEntity with 201 Created status or ErrorResponseModel
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.INSERT_CLIENT_PERMISSION + "')")
  @PutMapping("/" + ApiRoutes.ClientSubRoute.CREATE_CLIENT)
  public ResponseEntity<?> createClient(@RequestBody ClientRequestModel clientRequest) {
    try {
      clientService.createClient(clientRequest);
      return ResponseEntity.status(HttpStatus.CREATED).build();
    } catch (BadRequestException bre) {
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      logger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Updates an existing client with new information.
   *
   * <p>This endpoint retrieves the existing client by ID, validates the new data, and updates the
   * client while preserving audit information like createdUser and createdAt. Only the modifiedUser
   * and updatedAt fields are updated. Requires UPDATE_CLIENT_PERMISSION.
   *
   * @param id The unique identifier of the client to update
   * @param clientRequest The ClientRequestModel containing the updated client data
   * @return ResponseEntity with 200 OK status or ErrorResponseModel
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.UPDATE_CLIENT_PERMISSION + "')")
  @PostMapping("/" + ApiRoutes.ClientSubRoute.UPDATE_CLIENT + "/{id}")
  public ResponseEntity<?> updateClient(
      @PathVariable Long id, @RequestBody ClientRequestModel clientRequest) {
    try {
      clientRequest.setClientId(id);
      clientService.updateClient(clientRequest);
      return ResponseEntity.ok().build();
    } catch (BadRequestException bre) {
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      logger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Toggles the deletion status of a client by its ID.
   *
   * <p>This endpoint performs a soft delete operation by toggling the isDeleted flag. If the client
   * is currently active (isDeleted = false), it will be marked as deleted. If the client is
   * currently deleted (isDeleted = true), it will be restored. Requires DELETE_CLIENT_PERMISSION.
   *
   * @param id The unique identifier of the client to toggle
   * @return ResponseEntity with 200 OK status or ErrorResponseModel
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.DELETE_CLIENT_PERMISSION + "')")
  @DeleteMapping("/" + ApiRoutes.ClientSubRoute.TOGGLE_CLIENT + "/{id}")
  public ResponseEntity<?> toggleClient(@PathVariable Long id) {
    try {
      clientService.toggleClient(id);
      return ResponseEntity.ok().build();
    } catch (BadRequestException bre) {
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      logger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }
}
