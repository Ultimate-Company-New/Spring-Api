package com.example.SpringApi.Services.Interface;

import com.example.SpringApi.Models.RequestModels.LeadRequestModel;
import com.example.SpringApi.Models.ResponseModels.LeadResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;

/**
 * Interface for Lead operations and business logic. Provides methods for managing leads including
 * CRUD operations, batch processing, and specialized queries.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface ILeadSubTranslator {

  /**
   * Retrieves leads in paginated batches with optional filtering and sorting. Supports pagination,
   * sorting by multiple fields, and filtering capabilities.
   *
   * @param leadRequestModel The request model containing pagination and filter parameters
   * @return PaginationBaseResponseModel containing paginated lead data
   */
  PaginationBaseResponseModel<LeadResponseModel> getLeadsInBatches(
      LeadRequestModel leadRequestModel);

  /**
   * Retrieves detailed information for a specific lead by ID.
   *
   * @param leadId The unique identifier of the lead
   * @return LeadResponseModel with complete details including address and user information
   */
  LeadResponseModel getLeadDetailsById(Long leadId);

  /**
   * Retrieves detailed information for a specific lead by email address.
   *
   * @param email The email address of the lead
   * @return LeadResponseModel with complete details including address and user information
   */
  LeadResponseModel getLeadDetailsByEmail(String email);

  /**
   * Creates a new lead in the system.
   *
   * @param leadRequestModel The lead data to create
   * @return The created Lead entity
   */
  void createLead(LeadRequestModel leadRequestModel);

  /**
   * Updates an existing lead with new information.
   *
   * @param leadId The unique identifier of the lead to update
   * @param leadRequestModel The updated lead data
   * @return The updated Lead entity
   */
  void updateLead(Long leadId, LeadRequestModel leadRequestModel);

  /**
   * Toggles the active status of a lead.
   *
   * @param leadId The unique identifier of the lead to toggle
   * @return The updated Lead entity
   */
  void toggleLead(Long leadId);

  /**
   * Creates multiple leads asynchronously in the system with partial success support.
   *
   * <p>This method processes leads in a background thread with the following characteristics: -
   * Supports partial success: if some leads fail validation, others still succeed - Sends detailed
   * results to user via message notification after processing completes - NOT_SUPPORTED: Runs
   * without a transaction to avoid rollback-only issues
   *
   * @param leads List of LeadRequestModel containing the lead data to create
   * @param requestingUserId The ID of the user making the request (captured from security context)
   * @param requestingUserLoginName The loginName of the user making the request (captured from
   *     security context)
   * @param requestingClientId The client ID of the user making the request (captured from security
   *     context)
   */
  void bulkCreateLeadsAsync(
      java.util.List<LeadRequestModel> leads,
      Long requestingUserId,
      String requestingUserLoginName,
      Long requestingClientId);

  /**
   * Creates multiple leads synchronously in a single operation (for testing). This is a synchronous
   * wrapper that processes leads immediately and returns results.
   *
   * @param leads List of LeadRequestModel containing the lead data to create
   * @return BulkInsertResponseModel containing success/failure details for each lead
   */
  com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> bulkCreateLeads(
      java.util.List<LeadRequestModel> leads);
}

