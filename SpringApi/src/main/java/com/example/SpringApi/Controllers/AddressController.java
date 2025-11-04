package com.example.SpringApi.Controllers;

import com.example.SpringApi.Services.Interface.IAddressSubTranslator;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.ResponseModels.ErrorResponseModel;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Logging.ContextualLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.BadRequestException;

/**
 * REST Controller for managing Address-related operations.
 * 
 * This controller provides RESTful endpoints for address management including
 * CRUD operations, user/client address retrieval, and address status management.
 * All endpoints are secured with appropriate authorization checks and include
 * comprehensive error handling with contextual logging.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.ADDRESS)
public class AddressController {
    private static final ContextualLogger logger = ContextualLogger.getLogger(AddressController.class);
    private final IAddressSubTranslator addressService;

    @Autowired
    public AddressController(IAddressSubTranslator addressService) {
        this.addressService = addressService;
    }

    /**
     * Retrieves a single address by its unique identifier.
     * 
     * This endpoint fetches an address from the database using the provided ID.
     * The response contains all address details including street address, city,
     * state, postal code, country, and metadata. Requires VIEW_ADDRESS_PERMISSION.
     * 
     * @param id The unique identifier of the address to retrieve
     * @return ResponseEntity containing AddressResponseModel or ErrorResponseModel
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_ADDRESS_PERMISSION +"')")
    @GetMapping("/" + ApiRoutes.AddressSubRoute.GET_ADDRESS_BY_ID + "/{id}")
    public ResponseEntity<?> getAddressById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(addressService.getAddressById(id));
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
     * Retrieves all addresses associated with a specific user.
     * 
     * This endpoint fetches all addresses where the userId matches the provided parameter.
     * The response contains a list of AddressResponseModel objects, each with complete
     * address information. Returns an empty list if no addresses are found.
     * Requires VIEW_ADDRESS_PERMISSION.
     * 
     * @param userId The unique identifier of the user
     * @return ResponseEntity containing List<AddressResponseModel> or ErrorResponseModel
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_ADDRESS_PERMISSION +"')")
    @GetMapping("/" + ApiRoutes.AddressSubRoute.GET_ADDRESS_BY_USER_ID + "/{userId}")
    public ResponseEntity<?> getAddressesByUserId(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(addressService.getAddressByUserId(userId));
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Retrieves all addresses associated with a specific client.
     * 
     * This endpoint fetches all addresses where the clientId matches the provided parameter.
     * The response contains a list of AddressResponseModel objects, each with complete
     * address information. Returns an empty list if no addresses are found.
     * Requires VIEW_ADDRESS_PERMISSION.
     * 
     * @param clientId The unique identifier of the client
     * @return ResponseEntity containing List<AddressResponseModel> or ErrorResponseModel
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_ADDRESS_PERMISSION +"')")
    @GetMapping("/" + ApiRoutes.AddressSubRoute.GET_ADDRESS_BY_CLIENT_ID + "/{clientId}")
    public ResponseEntity<?> getAddressByClientId(@PathVariable Long clientId) {
        try {
            return ResponseEntity.ok(addressService.getAddressByClientId(clientId));
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Creates a new address in the system.
     * 
     * This endpoint validates the provided address data, creates a new Address entity,
     * and persists it to the database. The method automatically sets audit fields
     * such as createdUser, modifiedUser, and timestamps. Requires INSERT_ADDRESS_PERMISSION.
     * 
     * @param addressRequest The AddressRequestModel containing the address data to create
     * @return ResponseEntity with success status or ErrorResponseModel
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_ADDRESS_PERMISSION +"')")
    @PutMapping("/" + ApiRoutes.AddressSubRoute.INSERT_ADDRESS)
    public ResponseEntity<?> createAddress(@RequestBody AddressRequestModel addressRequest) {
        try {
            addressService.insertAddress(addressRequest);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Updates an existing address with new information.
     * 
     * This endpoint retrieves the existing address by ID, validates the new data,
     * and updates the address while preserving audit information like createdUser
     * and createdAt. Only the modifiedUser and updatedAt fields are updated.
     * Requires UPDATE_ADDRESS_PERMISSION.
     * 
     * @param id The unique identifier of the address to update
     * @param addressRequest The AddressRequestModel containing the updated address data
     * @return ResponseEntity with success status or ErrorResponseModel
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.UPDATE_ADDRESS_PERMISSION +"')")
    @PostMapping("/" + ApiRoutes.AddressSubRoute.UPDATE_ADDRESS + "/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable Long id, @RequestBody AddressRequestModel addressRequest) {
        try {
            addressRequest.setId(id);
            addressService.updateAddress(addressRequest);
            return ResponseEntity.ok().build();
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Toggles the deletion status of an address by its ID.
     * 
     * This endpoint performs a soft delete operation by toggling the isDeleted flag.
     * If the address is currently active (isDeleted = false), it will be marked as deleted.
     * If the address is currently deleted (isDeleted = true), it will be restored.
     * Requires DELETE_ADDRESS_PERMISSION.
     * 
     * @param id The unique identifier of the address to toggle
     * @return ResponseEntity with success status or ErrorResponseModel
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.DELETE_ADDRESS_PERMISSION +"')")
    @DeleteMapping("/" + ApiRoutes.AddressSubRoute.TOGGLE_ADDRESS + "/{id}")
    public ResponseEntity<?> toggleAddress(@PathVariable Long id) {
        try {
            addressService.toggleAddress(id);
            return ResponseEntity.ok().build();
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}