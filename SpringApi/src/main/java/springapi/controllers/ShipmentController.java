package com.example.springapi.controllers;

import com.example.springapi.ErrorMessages;
import com.example.springapi.exceptions.BadRequestException;
import com.example.springapi.exceptions.NotFoundException;
import com.example.springapi.exceptions.UnauthorizedException;
import com.example.springapi.logging.ContextualLogger;
import com.example.springapi.models.ApiRoutes;
import com.example.springapi.models.Authorizations;
import com.example.springapi.models.requestmodels.PaginationBaseRequestModel;
import com.example.springapi.models.responsemodels.ErrorResponseModel;
import com.example.springapi.services.interfaces.ShippingSubTranslator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
public class ShipmentController {

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
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (UnauthorizedException uae) {
      logger.error(uae);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  uae.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
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
    } catch (UnauthorizedException uae) {
      logger.error(uae);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  uae.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
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
