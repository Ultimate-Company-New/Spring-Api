package springapi.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import springapi.services.interfaces.ShippingSubTranslator;

/**
 * REST Controller for Shipment operations.
 *
 * <p>This controller handles all HTTP requests related to shipment management, including retrieval
 * and filtering of shipments.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.SHIPMENT)
public class ShipmentController extends BaseController {

  private static final ContextualLogger logger =
      ContextualLogger.getLogger(ShipmentController.class);
  private final ShippingSubTranslator shippingService;

  public ShipmentController(ShippingSubTranslator shippingService) {
    this.shippingService = shippingService;
  }

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
      @RequestBody PaginationBaseRequestModel paginationBaseRequestModel) {
    try {
      return ResponseEntity.ok(shippingService.getShipmentsInBatches(paginationBaseRequestModel));
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
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
