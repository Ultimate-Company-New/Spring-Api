package com.example.SpringApi.Services.Interface;

import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.ShipmentResponseModel;

/**
 * Interface for Shipment service operations.
 * 
 * This interface defines the contract for shipment-related business logic.
 * Implementations should handle data retrieval and filtering for shipments.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface IShipmentSubTranslator {
    
    /**
     * Retrieves shipments in batches with pagination support.
     * 
     * This method returns a paginated list of shipments based on the provided
     * pagination parameters. It supports filtering and sorting options.
     * 
     * @param paginationBaseRequestModel The pagination parameters including page size, number, filters, and sorting
     * @return Paginated response containing shipment data
     */
    PaginationBaseResponseModel<ShipmentResponseModel> getShipmentsInBatches(PaginationBaseRequestModel paginationBaseRequestModel);
    
    /**
     * Retrieves detailed information about a specific shipment by ID.
     * 
     * @param shipmentId The ID of the shipment to retrieve
     * @return The shipment response model with all details
     */
    ShipmentResponseModel getShipmentById(Long shipmentId);
}
