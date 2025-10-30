package com.example.SpringApi.Controllers;

import com.example.SpringApi.Models.ResponseModels.ErrorResponseModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.PromoRequestModel;
import com.example.SpringApi.Services.Interface.IPromoSubTranslator;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.UnauthorizedException;
import com.example.SpringApi.Logging.ContextualLogger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Promo operations.
 * 
 * This controller handles all HTTP requests related to promotional code management
 * including creating, reading, updating, deleting, and managing promo codes.
 * All endpoints require appropriate permissions for access.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.PROMO)
public class PromoController {

    private static final ContextualLogger logger = ContextualLogger.getLogger(PromoController.class);
    private final IPromoSubTranslator promoService;

    @Autowired
    public PromoController(IPromoSubTranslator promoService) {
        this.promoService = promoService;
    }

    /**
     * Retrieves promos in batches with pagination support.
     * 
     * This endpoint returns a paginated list of promotional codes based on the provided
     * pagination parameters. It supports filtering and sorting options.
     * Requires VIEW_PROMOS_PERMISSION to access.
     * 
     * @param paginationBaseRequestModel The pagination parameters
     * @return ResponseEntity containing paginated promo data or error
     */
    @PostMapping("/" + ApiRoutes.PromosSubRoute.GET_PROMOS_IN_BATCHES)
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PROMOS_PERMISSION +"')")
    public ResponseEntity<?> getPromosInBatches(@RequestBody PaginationBaseRequestModel paginationBaseRequestModel) {
        try {
            return ResponseEntity.ok(promoService.getPromosInBatches(paginationBaseRequestModel));
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (UnauthorizedException uae) {
            logger.error(uae);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, uae.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Creates a new promotional code.
     * 
     * This endpoint creates a new promo with the provided details including
     * discount value, promo code, and associated client information.
     * Requires INSERT_PROMOS_PERMISSION to access.
     * 
-     * @return ResponseEntity containing the ID of the newly created promo or error
     */
    @PutMapping("/" + ApiRoutes.PromosSubRoute.CREATE_PROMO)
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.INSERT_PROMOS_PERMISSION +"')")
    public ResponseEntity<?> addPromo(@RequestBody PromoRequestModel promoRequestModel) {
        try {
            promoService.createPromo(promoRequestModel);
            return ResponseEntity.ok().build();
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (UnauthorizedException uae) {
            logger.error(uae);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, uae.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Retrieves detailed information about a specific promo by ID.
     * 
     * This endpoint returns comprehensive details about a promotional code
     * including discount information, validity status, and associated client data.
     * Requires VIEW_PROMOS_PERMISSION to access.
     * 
     * @param id The ID of the promo to retrieve
     * @return ResponseEntity containing promo details or error
     */
    @GetMapping("/" + ApiRoutes.PromosSubRoute.GET_PROMO_DETAILS_BY_ID + "/{id}")
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PROMOS_PERMISSION +"')")
    public ResponseEntity<?> getPromoDetailsById(@PathVariable long id) {
        try {
            return ResponseEntity.ok(promoService.getPromoDetailsById(id));
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException uae) {
            logger.error(uae);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, uae.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Toggles the status of a promotional code (soft delete/restore).
     * 
     * This endpoint toggles the isDeleted flag of a promo, effectively
     * enabling or disabling the promotional code without permanent deletion.
     * Requires DELETE_PROMOS_PERMISSION to access.
     * 
     * @param id The ID of the promo to toggle
     * @return ResponseEntity containing success status or error
     */
    @DeleteMapping("/" + ApiRoutes.PromosSubRoute.TOGGLE_PROMO + "/{id}")
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.DELETE_PROMOS_PERMISSION +"')")
    public ResponseEntity<?> togglePromo(@PathVariable long id) {
        try {
            promoService.togglePromo(id);
            return ResponseEntity.ok().build();
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException uae) {
            logger.error(uae);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, uae.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Retrieves detailed information about a promo by its promotional code.
     * 
     * This endpoint allows lookup of promotional codes by their unique code string,
     * commonly used during checkout processes to validate and apply discounts.
     * Requires VIEW_PROMOS_PERMISSION to access.
     * 
     * @param promoCode The promotional code to search for
     * @return ResponseEntity containing promo details or error
     */
    @GetMapping("/" + ApiRoutes.PromosSubRoute.GET_PROMO_DETAILS_BY_NAME + "/{promoCode}")
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PROMOS_PERMISSION +"')")
    public ResponseEntity<?> getPromoDetailsByName(@PathVariable String promoCode) {
        try {
            return ResponseEntity.ok(promoService.getPromoDetailsByName(promoCode));
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException uae) {
            logger.error(uae);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, uae.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}