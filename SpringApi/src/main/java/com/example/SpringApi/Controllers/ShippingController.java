package com.example.SpringApi.Controllers;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.UnauthorizedException;
import com.example.SpringApi.Logging.ContextualLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.RequestModels.OrderOptimizationRequestModel;
import com.example.SpringApi.Models.RequestModels.ShippingCalculationRequestModel;
import com.example.SpringApi.Models.RequestModels.CreateReturnRequestModel;
import com.example.SpringApi.Models.ResponseModels.ErrorResponseModel;
import com.example.SpringApi.Models.ResponseModels.OrderOptimizationResponseModel;
import com.example.SpringApi.Models.ResponseModels.ShippingCalculationResponseModel;
import com.example.SpringApi.Models.ResponseModels.ReturnShipmentResponseModel;
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

    private static final ContextualLogger contextualLogger = ContextualLogger.getLogger(ShippingController.class);
    private static final Logger logger = LoggerFactory.getLogger(ShippingController.class);
    
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
            contextualLogger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            contextualLogger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException ue) {
            contextualLogger.error(ue);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, ue.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            contextualLogger.error(e);
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
        long startTime = System.currentTimeMillis();
        logger.info("Received optimizeOrder request for " + 
                   (request.getProductQuantities() != null ? request.getProductQuantities().size() : 0) + 
                   " products, delivery postcode: " + request.getDeliveryPostcode());
        try {
            OrderOptimizationResponseModel response = shippingService.optimizeOrder(request);
            long duration = System.currentTimeMillis() - startTime;
            logger.info("optimizeOrder completed in " + duration + "ms, success: " + response.getSuccess());
            return ResponseEntity.ok(response);
        } catch (BadRequestException bre) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("optimizeOrder failed after " + duration + "ms: BadRequestException");
            contextualLogger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            contextualLogger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException ue) {
            contextualLogger.error(ue);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, ue.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            contextualLogger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
    
    /**
     * Cancel a shipment.
     * Cancels the shipment in ShipRocket and updates the local shipment status to CANCELLED.
     * Requires MODIFY_SHIPMENTS_PERMISSION to access.
     * 
     * @param shipmentId The local shipment ID to cancel
     * @return ResponseEntity with success message or error
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.MODIFY_SHIPMENTS_PERMISSION +"')")
    @PostMapping(ApiRoutes.ShippingSubRoute.CANCEL_SHIPMENT + "/{shipmentId}")
    public ResponseEntity<?> cancelShipment(@PathVariable Long shipmentId) {
        try {
            shippingService.cancelShipment(shipmentId);
            return ResponseEntity.ok().build();
        } catch (BadRequestException bre) {
            contextualLogger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            contextualLogger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException ue) {
            contextualLogger.error(ue);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, ue.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            contextualLogger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
    
    /**
     * Create a return order for a shipment.
     * Creates a return shipment in ShipRocket and stores the return details locally.
     * Requires MODIFY_SHIPMENTS_PERMISSION to access.
     * 
     * @param request The return request containing shipment ID and products to return
     * @return ResponseEntity containing the created return shipment details or error
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.MODIFY_SHIPMENTS_PERMISSION +"')")
    @PostMapping(ApiRoutes.ShippingSubRoute.CREATE_RETURN)
    public ResponseEntity<?> createReturn(@RequestBody CreateReturnRequestModel request) {
        try {
            ReturnShipmentResponseModel response = shippingService.createReturn(request);
            return ResponseEntity.ok(response);
        } catch (BadRequestException bre) {
            contextualLogger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            contextualLogger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException ue) {
            contextualLogger.error(ue);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, ue.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            contextualLogger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
    
    /**
     * Cancel a return shipment.
     * Cancels the return order in ShipRocket and updates the local status.
     * Requires MODIFY_SHIPMENTS permission.
     * 
     * @param returnShipmentId The return shipment ID to cancel
     * @return Empty response on success
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.MODIFY_SHIPMENTS_PERMISSION +"')")
    @PostMapping(ApiRoutes.ShippingSubRoute.CANCEL_RETURN + "/{returnShipmentId}")
    public ResponseEntity<?> cancelReturn(@PathVariable Long returnShipmentId) {
        try {
            shippingService.cancelReturnShipment(returnShipmentId);
            return ResponseEntity.ok().build();
        } catch (BadRequestException bre) {
            contextualLogger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            contextualLogger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException ue) {
            contextualLogger.error(ue);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, ue.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            contextualLogger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
    
    /**
     * Get the ShipRocket wallet balance.
     * Returns the current wallet balance for the client's ShipRocket account.
     * Requires VIEW_SHIPMENTS permission to access.
     * 
     * @return ResponseEntity containing the wallet balance as a Double
     */
    @PreAuthorize("@customAuthorization.hasAuthority('"+ Authorizations.VIEW_SHIPMENTS_PERMISSION +"')")
    @GetMapping(ApiRoutes.ShippingSubRoute.GET_WALLET_BALANCE)
    public ResponseEntity<?> getWalletBalance() {
        try {
            Double balance = shippingService.getWalletBalance();
            return ResponseEntity.ok(balance);
        } catch (BadRequestException bre) {
            contextualLogger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (UnauthorizedException ue) {
            contextualLogger.error(ue);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, ue.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            contextualLogger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}
