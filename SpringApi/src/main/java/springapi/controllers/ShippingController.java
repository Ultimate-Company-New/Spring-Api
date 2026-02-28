package springapi.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springapi.ErrorMessages;
import springapi.exceptions.BadRequestException;
import springapi.exceptions.NotFoundException;
import springapi.exceptions.UnauthorizedException;
import springapi.logging.ContextualLogger;
import springapi.models.ApiRoutes;
import springapi.models.Authorizations;
import springapi.models.requestmodels.CreateReturnRequestModel;
import springapi.models.requestmodels.OrderOptimizationRequestModel;
import springapi.models.requestmodels.ShippingCalculationRequestModel;
import springapi.models.responsemodels.ErrorResponseModel;
import springapi.models.responsemodels.OrderOptimizationResponseModel;
import springapi.models.responsemodels.ReturnShipmentResponseModel;
import springapi.models.responsemodels.ShippingCalculationResponseModel;
import springapi.services.interfaces.ShippingSubTranslator;

/**
 * REST Controller for Shipping operations.
 *
 * <p>This controller handles HTTP requests for shipping calculations including order-level shipping
 * calculations that can combine multiple products. All endpoints require proper authorization and
 * include comprehensive error handling.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.SHIPPING)
public class ShippingController {

  private static final ContextualLogger contextualLogger =
      ContextualLogger.getLogger(ShippingController.class);
  private static final Logger logger = LoggerFactory.getLogger(ShippingController.class);

  private final ShippingSubTranslator shippingService;

  @Autowired
  public ShippingController(ShippingSubTranslator shippingService) {
    this.shippingService = shippingService;
  }

  /**
   * Calculate shipping options for an order. Groups products by pickup location and returns.
   * available couriers for each location. Requires VIEW_PURCHASE_ORDERS_PERMISSION to access.
   *
   * @param request Contains delivery postcode, COD flag, and list of pickup locations with weights
   * @return ResponseEntity containing shipping options for each pickup location or error
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION + "')")
  @PostMapping(ApiRoutes.ShippingSubRoute.CALCULATE_SHIPPING)
  public ResponseEntity<?> calculateShipping(@RequestBody ShippingCalculationRequestModel request) {
    try {
      ShippingCalculationResponseModel response = shippingService.calculateShipping(request);
      return ResponseEntity.ok(response);
    } catch (BadRequestException bre) {
      contextualLogger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      contextualLogger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException ue) {
      contextualLogger.error(ue);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  ue.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      contextualLogger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Optimize order fulfillment across multiple pickup locations.
   *
   * <p>This endpoint finds the optimal allocation of products to pickup locations that minimizes
   * total cost (shipping + packaging). It considers: - Product availability at each location -
   * Packaging capacity and costs at each location - Shipping rates (tiered/slab-based) from each
   * location to delivery address - Consolidation benefits (multiple products in same shipment =
   * lower cost)
   *
   * <p>The algorithm evaluates multiple allocation strategies: 1. Single-location options (all
   * products from one location if possible) 2. Multi-location splits optimized for cost
   *
   * <p>Requires VIEW_PURCHASE_ORDERS_PERMISSION to access.
   *
   * @param request Contains map of product IDs to quantities and delivery postcode
   * @return ResponseEntity containing allocation options ranked by cost or error
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION + "')")
  @PostMapping(ApiRoutes.ShippingSubRoute.OPTIMIZE_ORDER)
  public ResponseEntity<?> optimizeOrder(@RequestBody OrderOptimizationRequestModel request) {
    long startTime = System.currentTimeMillis();
    int productCount =
        request.getProductQuantities() != null ? request.getProductQuantities().size() : 0;
    logger.info(
        "Received optimizeOrder request for {} products, delivery postcode: {}",
        productCount,
        request.getDeliveryPostcode());
    try {
      OrderOptimizationResponseModel response = shippingService.optimizeOrder(request);
      long duration = System.currentTimeMillis() - startTime;
      logger.info("optimizeOrder completed in {}ms, success: {}", duration, response.getSuccess());
      return ResponseEntity.ok(response);
    } catch (BadRequestException bre) {
      long duration = System.currentTimeMillis() - startTime;
      logger.error("optimizeOrder failed after {}ms: BadRequestException", duration);
      contextualLogger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      contextualLogger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException ue) {
      contextualLogger.error(ue);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  ue.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      contextualLogger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Cancel a shipment. Cancels the shipment in ShipRocket and updates the local shipment status to.
   * CANCELLED. Requires MODIFY_SHIPMENTS_PERMISSION to access.
   *
   * @param shipmentId The local shipment ID to cancel
   * @return ResponseEntity with success message or error
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.MODIFY_SHIPMENTS_PERMISSION + "')")
  @PostMapping(ApiRoutes.ShippingSubRoute.CANCEL_SHIPMENT + "/{shipmentId}")
  public ResponseEntity<?> cancelShipment(@PathVariable Long shipmentId) {
    try {
      shippingService.cancelShipment(shipmentId);
      return ResponseEntity.ok().build();
    } catch (BadRequestException bre) {
      contextualLogger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      contextualLogger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException ue) {
      contextualLogger.error(ue);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  ue.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      contextualLogger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Create a return order for a shipment. Creates a return shipment in ShipRocket and stores the.
   * return details locally. Requires MODIFY_SHIPMENTS_PERMISSION to access.
   *
   * @param request The return request containing shipment ID and products to return
   * @return ResponseEntity containing the created return shipment details or error
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.MODIFY_SHIPMENTS_PERMISSION + "')")
  @PostMapping(ApiRoutes.ShippingSubRoute.CREATE_RETURN)
  public ResponseEntity<?> createReturn(@RequestBody CreateReturnRequestModel request) {
    try {
      ReturnShipmentResponseModel response = shippingService.createReturn(request);
      return ResponseEntity.ok(response);
    } catch (BadRequestException bre) {
      contextualLogger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      contextualLogger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException ue) {
      contextualLogger.error(ue);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  ue.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      contextualLogger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Cancel a return shipment. Cancels the return order in ShipRocket and updates the local status.
   * Requires MODIFY_SHIPMENTS permission.
   *
   * @param returnShipmentId The return shipment ID to cancel
   * @return Empty response on success
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.MODIFY_SHIPMENTS_PERMISSION + "')")
  @PostMapping(ApiRoutes.ShippingSubRoute.CANCEL_RETURN + "/{returnShipmentId}")
  public ResponseEntity<?> cancelReturn(@PathVariable Long returnShipmentId) {
    try {
      shippingService.cancelReturnShipment(returnShipmentId);
      return ResponseEntity.ok().build();
    } catch (BadRequestException bre) {
      contextualLogger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      contextualLogger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException ue) {
      contextualLogger.error(ue);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  ue.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      contextualLogger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Get the ShipRocket wallet balance. Returns the current wallet balance for the client's.
   * ShipRocket account. Requires VIEW_SHIPMENTS permission to access.
   *
   * @return ResponseEntity containing the wallet balance as a Double
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.VIEW_SHIPMENTS_PERMISSION + "')")
  @GetMapping(ApiRoutes.ShippingSubRoute.GET_WALLET_BALANCE)
  public ResponseEntity<?> getWalletBalance() {
    try {
      Double balance = shippingService.getWalletBalance();
      return ResponseEntity.ok(balance);
    } catch (BadRequestException bre) {
      contextualLogger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (UnauthorizedException ue) {
      contextualLogger.error(ue);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  ue.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      contextualLogger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  // ============================================================================
  // SHIPMENT ENDPOINTS (moved from ShipmentController)
  // ============================================================================

  /**
   * Retrieves shipments in batches with pagination support.
   *
   * <p>This endpoint returns a paginated list of shipments based on the provided pagination
   * parameters. It supports filtering and sorting options.
   *
   * @param paginationBaseRequestModel The pagination parameters including page size, number,
   *     filters, and sorting
   * @return ResponseEntity containing paginated shipment data
   * @throws BadRequestException if validation fails
   * @throws UnauthorizedException if user lacks VIEW_SHIPMENTS_PERMISSION
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.VIEW_SHIPMENTS_PERMISSION + "')")
  @PostMapping(ApiRoutes.ShipmentSubRoute.GET_SHIPMENTS_IN_BATCHES)
  public ResponseEntity<?> getShipmentsInBatches(
      @RequestBody
          springapi.models.requestmodels.PaginationBaseRequestModel paginationBaseRequestModel) {
    try {
      return ResponseEntity.ok(shippingService.getShipmentsInBatches(paginationBaseRequestModel));
    } catch (BadRequestException bre) {
      contextualLogger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (UnauthorizedException uae) {
      contextualLogger.error(uae);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  uae.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      contextualLogger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Retrieves detailed information about a specific shipment by ID.
   *
   * @param shipmentId The ID of the shipment to retrieve
   * @return ResponseEntity containing shipment details
   * @throws BadRequestException if validation fails
   * @throws NotFoundException if shipment is not found
   * @throws UnauthorizedException if user lacks VIEW_SHIPMENTS_PERMISSION
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.VIEW_SHIPMENTS_PERMISSION + "')")
  @GetMapping(ApiRoutes.ShipmentSubRoute.GET_SHIPMENT_BY_ID + "/{shipmentId}")
  public ResponseEntity<?> getShipmentById(@PathVariable Long shipmentId) {
    try {
      return ResponseEntity.ok(shippingService.getShipmentById(shipmentId));
    } catch (BadRequestException bre) {
      contextualLogger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      contextualLogger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException uae) {
      contextualLogger.error(uae);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  uae.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      contextualLogger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }
}
