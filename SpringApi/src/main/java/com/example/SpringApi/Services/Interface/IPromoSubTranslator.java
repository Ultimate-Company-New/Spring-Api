package com.example.SpringApi.Services.Interface;

import com.example.SpringApi.Models.DatabaseModels.Promo;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.PromoRequestModel;
import com.example.SpringApi.Models.ResponseModels.PromoResponseModel;

/**
 * Interface for Promo operations and data access.
 * 
 * This interface defines the contract for all promo-related business operations
 * including CRUD operations, batch processing, and promo code management.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface IPromoSubTranslator {
    
    /**
     * Retrieves promos in batches with pagination support.
     * 
     * @param paginationBaseRequestModel The pagination parameters
     * @return Paginated response containing promo data
     */
    PaginationBaseResponseModel<Promo> getPromosInBatches(PaginationBaseRequestModel paginationBaseRequestModel);
    
    /**
     * Creates a new promo.
     * 
     * @param promoRequestModel The promo request model containing promo data
     * @throws NotFoundException if required dependencies are not found
     */
    void createPromo(PromoRequestModel promoRequestModel);
    
    /**
     * Retrieves promo details by ID.
     * 
     * @param id The ID of the promo to retrieve
     * @return The promo details as response model
     */
    PromoResponseModel getPromoDetailsById(long id);
    
    /**
     * Toggles the status of a promo (soft delete/restore).
     * 
     * @param id The ID of the promo to toggle
     * @throws NotFoundException if the promo is not found
     */
    void togglePromo(long id);
    
    /**
     * Retrieves promo details by promo code.
     * 
     * @param promoCode The promo code to search for
     * @return The promo details as response model
     */
    PromoResponseModel getPromoDetailsByName(String promoCode);

    /**
     * Creates multiple promos asynchronously in the system with partial success support.
     * 
     * This method processes promos in a background thread with the following characteristics:
     * - Supports partial success: if some promos fail validation, others still succeed
     * - Sends detailed results to user via message notification after processing completes
     * 
     * @param promos List of PromoRequestModel containing the promo data to create
     * @param requestingUserId The ID of the user making the request (captured from security context)
     * @param requestingUserLoginName The loginName of the user making the request (captured from security context)
     * @param requestingClientId The client ID of the user making the request (captured from security context)
     */
    void bulkCreatePromosAsync(java.util.List<PromoRequestModel> promos, Long requestingUserId, String requestingUserLoginName, Long requestingClientId);
}