package springapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
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
import springapi.exceptions.BadRequestException;
import springapi.exceptions.NotFoundException;
import springapi.exceptions.UnauthorizedException;
import springapi.logging.ContextualLogger;
import springapi.models.ApiRoutes;
import springapi.models.Authorizations;
import springapi.models.requestmodels.PaginationBaseRequestModel;
import springapi.models.requestmodels.PromoRequestModel;
import springapi.services.PromoService;
import springapi.services.interfaces.PromoSubTranslator;

/**
 * REST Controller for Promo operations.
 *
 * <p>This controller handles all HTTP requests related to promotional code management including
 * creating, reading, updating, deleting, and managing promo codes. All endpoints require
 * appropriate permissions for access.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.PROMO)
public class PromoController extends BaseController {

  private static final ContextualLogger logger = ContextualLogger.getLogger(PromoController.class);
  private final PromoSubTranslator promoService;

  @Autowired
  public PromoController(PromoSubTranslator promoService) {
    this.promoService = promoService;
  }

  /**
   * Retrieves promos in batches with pagination support.
   *
   * <p>This endpoint returns a paginated list of promotional codes based on the provided pagination
   * parameters. It supports filtering and sorting options. Requires VIEW_PROMOS_PERMISSION to
   * access.
   *
   * @param paginationBaseRequestModel The pagination parameters
   * @return ResponseEntity containing paginated promo data or error
   */
  @PostMapping("/" + ApiRoutes.PromosSubRoute.GET_PROMOS_IN_BATCHES)
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.VIEW_PROMOS_PERMISSION + "')")
  public ResponseEntity<?> getPromosInBatches(
      @RequestBody PaginationBaseRequestModel paginationBaseRequestModel) {
    try {
      return ResponseEntity.ok(promoService.getPromosInBatches(paginationBaseRequestModel));
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Creates a new promotional code.
   *
   * <p>This endpoint creates a new promo with the provided details including discount value, promo
   * code, and associated client information. ClientId is automatically retrieved from the security
   * context. Requires INSERT_PROMOS_PERMISSION to access.
   *
   * @param promoRequestModel The promo request model containing promo data
   * @return ResponseEntity containing success status or error
   */
  @PutMapping("/" + ApiRoutes.PromosSubRoute.CREATE_PROMO)
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.INSERT_PROMOS_PERMISSION + "')")
  public ResponseEntity<?> addPromo(@RequestBody PromoRequestModel promoRequestModel) {
    try {
      promoService.createPromo(promoRequestModel);
      return ResponseEntity.ok().build();
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Creates multiple promotional codes in a single bulk operation.
   *
   * <p>This endpoint processes promos asynchronously and sends results via message notification.
   * Security context is captured BEFORE calling the async method to ensure proper user tracking.
   * Requires INSERT_PROMOS_PERMISSION to access.
   *
   * @param promos List of PromoRequestModel containing the promo data to insert
   * @return ResponseEntity with 200 OK if processing started successfully
   */
  @PutMapping("/" + ApiRoutes.PromosSubRoute.BULK_CREATE_PROMO)
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.INSERT_PROMOS_PERMISSION + "')")
  public ResponseEntity<?> bulkCreatePromos(@RequestBody java.util.List<PromoRequestModel> promos) {
    try {
      // Cast to concrete service to access BaseService methods for security context
      PromoService concreteService = (PromoService) promoService;

      // Capture security context BEFORE calling async method
      Long userId = concreteService.getUserId();
      String loginName = concreteService.getUser();
      Long clientId = concreteService.getClientId();

      // Call async method with captured context
      promoService.bulkCreatePromosAsync(promos, userId, loginName, clientId);

      // Return immediately - results will be sent via message notification
      return ResponseEntity.ok().build();
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Retrieves detailed information about a specific promo by ID.
   *
   * <p>This endpoint returns comprehensive details about a promotional code including discount
   * information, validity status, and associated client data. Requires VIEW_PROMOS_PERMISSION to
   * access.
   *
   * @param id The ID of the promo to retrieve
   * @return ResponseEntity containing promo details or error
   */
  @GetMapping("/" + ApiRoutes.PromosSubRoute.GET_PROMO_DETAILS_BY_ID + "/{id}")
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.VIEW_PROMOS_PERMISSION + "')")
  public ResponseEntity<?> getPromoDetailsById(@PathVariable long id) {
    try {
      return ResponseEntity.ok(promoService.getPromoDetailsById(id));
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (NotFoundException nfe) {
      return notFound(logger, nfe);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Toggles the status of a promotional code (soft delete/restore).
   *
   * <p>This endpoint toggles the isDeleted flag of a promo, effectively enabling or disabling the
   * promotional code without permanent deletion. Requires DELETE_PROMOS_PERMISSION to access.
   *
   * @param id The ID of the promo to toggle
   * @return ResponseEntity containing success status or error
   */
  @DeleteMapping("/" + ApiRoutes.PromosSubRoute.TOGGLE_PROMO + "/{id}")
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.DELETE_PROMOS_PERMISSION + "')")
  public ResponseEntity<?> togglePromo(@PathVariable long id) {
    try {
      promoService.togglePromo(id);
      return ResponseEntity.ok().build();
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (NotFoundException nfe) {
      return notFound(logger, nfe);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Retrieves detailed information about a promo by its promotional code.
   *
   * <p>This endpoint allows lookup of promotional codes by their unique code string, commonly used
   * during checkout processes to validate and apply discounts. Requires VIEW_PROMOS_PERMISSION to
   * access.
   *
   * @param promoCode The promotional code to search for
   * @return ResponseEntity containing promo details or error
   */
  @GetMapping("/" + ApiRoutes.PromosSubRoute.GET_PROMO_DETAILS_BY_NAME + "/{promoCode}")
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.VIEW_PROMOS_PERMISSION + "')")
  public ResponseEntity<?> getPromoDetailsByName(@PathVariable String promoCode) {
    try {
      return ResponseEntity.ok(promoService.getPromoDetailsByName(promoCode));
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (NotFoundException nfe) {
      return notFound(logger, nfe);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }
}
