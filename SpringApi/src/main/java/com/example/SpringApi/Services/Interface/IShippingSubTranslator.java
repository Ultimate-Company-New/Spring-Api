package com.example.SpringApi.Services.Interface;

import com.example.SpringApi.Models.RequestModels.OrderOptimizationRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.ShippingCalculationRequestModel;
import com.example.SpringApi.Models.ResponseModels.OrderOptimizationResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.ShipmentResponseModel;
import com.example.SpringApi.Models.ResponseModels.ShippingCalculationResponseModel;

/**
 * Interface for shipping-related operations.
 *
 * <p>Defines the contract for shipping service implementations including order-level shipping
 * calculations that can combine multiple products from the same pickup location, shipment
 * management, and returns.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface IShippingSubTranslator {

  // ============================================================================
  // SHIPMENT OPERATIONS
  // ============================================================================

  /**
   * Retrieves shipments in batches with pagination support.
   *
   * <p>This method returns a paginated list of shipments based on the provided pagination
   * parameters. It supports filtering and sorting options.
   *
   * @param paginationBaseRequestModel The pagination parameters including page size, number,
   *     filters, and sorting
   * @return Paginated response containing shipment data
   */
  PaginationBaseResponseModel<ShipmentResponseModel> getShipmentsInBatches(
      PaginationBaseRequestModel paginationBaseRequestModel);

  /**
   * Retrieves detailed information about a specific shipment by ID.
   *
   * @param shipmentId The ID of the shipment to retrieve
   * @return The shipment response model with all details
   */
  ShipmentResponseModel getShipmentById(Long shipmentId);

  // ============================================================================
  // SHIPPING CALCULATION & OPTIMIZATION
  // ============================================================================

  /**
   * Calculate shipping options for an order. Groups products by pickup location and returns
   * available couriers for each location.
   *
   * @param request Contains delivery postcode, COD flag, and list of pickup locations with weights
   * @return Shipping options for each pickup location with available couriers
   */
  ShippingCalculationResponseModel calculateShipping(ShippingCalculationRequestModel request);

  /**
   * Optimize order fulfillment across multiple pickup locations. Finds the optimal allocation of
   * products to pickup locations that minimizes total cost (shipping + packaging), considering: -
   * Product availability at each location - Packaging capacity and costs at each location -
   * Shipping rates (tiered/slab-based) from each location to delivery address - Consolidation
   * benefits (multiple products in same shipment)
   *
   * @param request Contains map of product IDs to quantities and delivery postcode
   * @return Allocation options ranked by total cost with detailed breakdown
   */
  OrderOptimizationResponseModel optimizeOrder(OrderOptimizationRequestModel request);

  // ============================================================================
  // SHIPMENT LIFECYCLE MANAGEMENT
  // ============================================================================

  /**
   * Cancel a shipment. Cancels the shipment in ShipRocket and updates the local shipment status to
   * CANCELLED.
   *
   * @param shipmentId The local shipment ID to cancel
   * @throws BadRequestException if the shipment cannot be cancelled
   */
  void cancelShipment(Long shipmentId);

  /**
   * Create a return order for a shipment. Creates a return shipment in ShipRocket and stores the
   * return details locally.
   *
   * @param request The return request containing shipment ID and products to return
   * @return ReturnShipmentResponseModel with the created return details
   * @throws BadRequestException if the return cannot be created
   */
  com.example.SpringApi.Models.ResponseModels.ReturnShipmentResponseModel createReturn(
      com.example.SpringApi.Models.RequestModels.CreateReturnRequestModel request);

  /**
   * Cancel a return shipment. Cancels the return order in ShipRocket and updates the local return
   * shipment status to RETURN_CANCELLED.
   *
   * @param returnShipmentId The local return shipment ID to cancel
   * @throws BadRequestException if the return shipment cannot be cancelled
   */
  void cancelReturnShipment(Long returnShipmentId);

  /**
   * Get the ShipRocket wallet balance for the client.
   *
   * @return The wallet balance as a Double
   * @throws BadRequestException if the wallet balance cannot be retrieved
   */
  Double getWalletBalance();
}

