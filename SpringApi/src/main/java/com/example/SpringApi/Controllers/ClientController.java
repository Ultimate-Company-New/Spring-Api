package com.example.SpringApi.Controllers;

import com.example.SpringApi.Services.Interface.IClientSubTranslator;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.RequestModels.ClientRequestModel;
import com.example.SpringApi.Models.ResponseModels.ErrorResponseModel;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Logging.ContextualLogger;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing Client-related operations.
 * 
 * This controller provides RESTful endpoints for client management including
 * CRUD operations, client retrieval, and client status management.
 * All endpoints are secured with appropriate authorization checks and include
 * comprehensive error handling with contextual logging.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.CLIENT)
public class ClientController {
    private static final ContextualLogger logger = ContextualLogger.getLogger(ClientController.class);
    private final IClientSubTranslator clientService;

    @Autowired
    public ClientController(IClientSubTranslator clientService) {
        this.clientService = clientService;
    }

    /**
     * Retrieves a single client by its unique identifier.
     * 
     * This endpoint fetches a client from the database using the provided ID.
     * The response contains all client details including name, description,
     * integration settings, and metadata. Requires VIEW_CLIENT_PERMISSION.
     * 
     * @param id The unique identifier of the client to retrieve
     * @return ResponseEntity containing ClientResponseModel or ErrorResponseModel
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_CLIENT_PERMISSION +"')")
    @GetMapping("/" + ApiRoutes.ClientSubRoute.GET_CLIENT_BY_ID + "/{id}")
    public ResponseEntity<?> getClientById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(clientService.getClientById(id));
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Retrieves all clients in the system.
     * 
     * This endpoint fetches all clients from the database. The response contains
     * a list of ClientResponseModel objects, each with complete client information.
     * Returns an empty list if no clients are found. Requires VIEW_CLIENT_PERMISSION.
     * 
     * @return ResponseEntity containing List<ClientResponseModel> or ErrorResponseModel
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_CLIENT_PERMISSION +"')")
    @GetMapping("/" + ApiRoutes.ClientSubRoute.GET_CLIENTS)
    public ResponseEntity<?> getAllClients() {
        try {
            return ResponseEntity.ok(clientService.getAllClients());
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Retrieves all clients mapped to the current user.
     * 
     * This endpoint fetches all clients where the current user has a mapping
     * in the UserClientMapping table. The response contains a list of 
     * ClientResponseModel objects, each with complete client information.
     * Returns an empty list if no clients are mapped to the user.
     * Requires VIEW_CLIENT_PERMISSION.
     * 
     * @return ResponseEntity containing List<ClientResponseModel> or ErrorResponseModel
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_CLIENT_PERMISSION +"')")
    @GetMapping("/" + ApiRoutes.ClientSubRoute.GET_CLIENTS_BY_USER)
    public ResponseEntity<?> getClientsByUser() {
        try {
            return ResponseEntity.ok(clientService.getClientsByUser());
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Creates a new client in the system.
     * 
     * This endpoint validates the provided client data, creates a new Client entity,
     * and persists it to the database. The method automatically sets audit fields
     * such as createdUser, modifiedUser, and timestamps. Requires INSERT_CLIENT_PERMISSION.
     * 
     * @param clientRequest The ClientRequestModel containing the client data to create
     * @return ResponseEntity with 201 Created status or ErrorResponseModel
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_CLIENT_PERMISSION +"')")
    @PutMapping("/" + ApiRoutes.ClientSubRoute.CREATE_CLIENT)
    public ResponseEntity<?> createClient(@RequestBody ClientRequestModel clientRequest) {
        try {
            clientService.createClient(clientRequest);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Updates an existing client with new information.
     * 
     * This endpoint retrieves the existing client by ID, validates the new data,
     * and updates the client while preserving audit information like createdUser
     * and createdAt. Only the modifiedUser and updatedAt fields are updated.
     * Requires UPDATE_CLIENT_PERMISSION.
     * 
     * @param id The unique identifier of the client to update
     * @param clientRequest The ClientRequestModel containing the updated client data
     * @return ResponseEntity with 200 OK status or ErrorResponseModel
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_CLIENT_PERMISSION +"')")
    @PostMapping("/" + ApiRoutes.ClientSubRoute.UPDATE_CLIENT + "/{id}")
    public ResponseEntity<?> updateClient(@PathVariable Long id, @RequestBody ClientRequestModel clientRequest) {
        try {
            clientRequest.setClientId(id);
            clientService.updateClient(clientRequest);
            return ResponseEntity.ok().build();
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Toggles the deletion status of a client by its ID.
     * 
     * This endpoint performs a soft delete operation by toggling the isDeleted flag.
     * If the client is currently active (isDeleted = false), it will be marked as deleted.
     * If the client is currently deleted (isDeleted = true), it will be restored.
     * Requires DELETE_CLIENT_PERMISSION.
     * 
     * @param id The unique identifier of the client to toggle
     * @return ResponseEntity with 200 OK status or ErrorResponseModel
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.DELETE_CLIENT_PERMISSION +"')")
    @DeleteMapping("/" + ApiRoutes.ClientSubRoute.TOGGLE_CLIENT + "/{id}")
    public ResponseEntity<?> toggleClient(@PathVariable Long id) {
        try {
            clientService.toggleClient(id);
            return ResponseEntity.ok().build();
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}
