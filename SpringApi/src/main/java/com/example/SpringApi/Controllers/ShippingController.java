package com.example.SpringApi.Controllers;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.UnauthorizedException;
import com.example.SpringApi.Logging.ContextualLogger;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.RequestModels.OrderOptimizationRequestModel;
import com.example.SpringApi.Models.RequestModels.ShippingCalculationRequestModel;
import com.example.SpringApi.Models.ResponseModels.ErrorResponseModel;
import com.example.SpringApi.Models.ResponseModels.OrderOptimizationResponseModel;
import com.example.SpringApi.Models.ResponseModels.ShippingCalculationResponseModel;
import com.example.SpringApi.Services.Interface.IShippingSubTranslator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Shipping operations.
 * 
 * This controller handles HTTP requests for shipping calculations including
 * order-level shipping calculations that can combine multiple products.
 * All endpoints require proper authorization and include comprehensive error handling.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.SHIPPING)
public class ShippingController {

    private static final ContextualLogger logger = ContextualLogger.getLogger(ShippingController.class);
    
    private final IShippingSubTranslator shippingService;

    @Autowired
    public ShippingController(IShippingSubTranslator shippingService) {
        this.shippingService = shippingService;
    }

    /**
     * Calculate shipping options for an order.
     * Groups products by pickup location and returns available couriers for each location.
     * Requires VIEW_PURCHASE_ORDERS_PERMISSION to access.
     * 
     * @param request Contains delivery postcode, COD flag, and list of pickup locations with weights
     * @return ResponseEntity containing shipping options for each pickup location or error
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION +"')")
    @PostMapping(ApiRoutes.ShippingSubRoute.CALCULATE_SHIPPING)
    public ResponseEntity<?> calculateShipping(@RequestBody ShippingCalculationRequestModel request) {
        try {
            ShippingCalculationResponseModel response = shippingService.calculateShipping(request);
            return ResponseEntity.ok(response);
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException ue) {
            logger.error(ue);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, ue.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
    
    /**
     * Optimize order fulfillment across multiple pickup locations.
     * 
     * This endpoint finds the optimal allocation of products to pickup locations
     * that minimizes total cost (shipping + packaging). It considers:
     * - Product availability at each location
     * - Packaging capacity and costs at each location  
     * - Shipping rates (tiered/slab-based) from each location to delivery address
     * - Consolidation benefits (multiple products in same shipment = lower cost)
     * 
     * The algorithm evaluates multiple allocation strategies:
     * 1. Single-location options (all products from one location if possible)
     * 2. Multi-location splits optimized for cost
     * 
     * Requires VIEW_PURCHASE_ORDERS_PERMISSION to access.
     * 
     * @param request Contains map of product IDs to quantities and delivery postcode
     * @return ResponseEntity containing allocation options ranked by cost or error
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION +"')")
    @PostMapping(ApiRoutes.ShippingSubRoute.OPTIMIZE_ORDER)
    public ResponseEntity<?> optimizeOrder(@RequestBody OrderOptimizationRequestModel request) {
        try {
            OrderOptimizationResponseModel response = shippingService.optimizeOrder(request);
            return ResponseEntity.ok(response);
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException ue) {
            logger.error(ue);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, ue.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}
