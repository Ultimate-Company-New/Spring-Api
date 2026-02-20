package com.example.SpringApi.Services;

import com.example.SpringApi.Authentication.JwtTokenProvider;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.FilterQueryBuilder.LeadFilterQueryBuilder;
import com.example.SpringApi.Helpers.BulkInsertHelper;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.DatabaseModels.Lead;
import com.example.SpringApi.Models.RequestModels.LeadRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.FilterCondition;
import com.example.SpringApi.Models.ResponseModels.LeadResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Repositories.AddressRepository;
import com.example.SpringApi.Repositories.LeadRepository;
import com.example.SpringApi.Services.Interface.ILeadSubTranslator;
import com.example.SpringApi.SuccessMessages;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for Lead operations and business logic. Handles all lead-related business
 * operations including CRUD operations, batch processing, validation, and specialized queries.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class LeadService extends BaseService implements ILeadSubTranslator {
  private final LeadRepository leadRepository;
  private final AddressRepository addressRepository;
  private final UserLogService userLogService;
  private final LeadFilterQueryBuilder leadFilterQueryBuilder;
  private final MessageService messageService;
  private ApplicationContext applicationContext;
  private static final String UNKNOWN_VALUE = "unknown";

  @Autowired
  public LeadService(
      LeadRepository leadRepository,
      AddressRepository addressRepository,
      UserLogService userLogService,
      LeadFilterQueryBuilder leadFilterQueryBuilder,
      MessageService messageService,
      JwtTokenProvider jwtTokenProvider,
      HttpServletRequest request,
      ApplicationContext applicationContext) {
    super(jwtTokenProvider, request);
    this.leadRepository = leadRepository;
    this.addressRepository = addressRepository;
    this.userLogService = userLogService;
    this.leadFilterQueryBuilder = leadFilterQueryBuilder;
    this.messageService = messageService;
    this.applicationContext = applicationContext;
  }

  /**
   * Retrieves leads in paginated batches with optional filtering and sorting. Supports pagination,
   * sorting by multiple fields, and multi-filter capabilities with AND/OR logic.
   *
   * <p>Valid columns for filtering: "leadId", "firstName", "lastName", "email", "address",
   * "website", "phone", "companySize", "title", "leadStatus", "company", "annualRevenue", "fax",
   * "isDeleted", "createdUser", "modifiedUser", "createdAt", "updatedAt", "notes"
   *
   * @param leadRequestModel The request model containing pagination and filter parameters
   * @return PaginationBaseResponseModel containing paginated lead data
   * @throws BadRequestException if an invalid column name or filter condition is provided
   */
  @Override
  @Transactional(readOnly = true)
  public PaginationBaseResponseModel<LeadResponseModel> getLeadsInBatches(
      LeadRequestModel leadRequestModel) {
    // Calculate page size and offset
    int start = leadRequestModel.getStart();
    int end = leadRequestModel.getEnd();
    int pageSize = end - start;

    // Validate page size
    if (pageSize <= 0) {
      throw new BadRequestException(ErrorMessages.CommonErrorMessages.INVALID_PAGINATION);
    }

    // Validate logic operator if provided
    if (leadRequestModel.getLogicOperator() != null && !leadRequestModel.isValidLogicOperator()) {
      throw new BadRequestException(ErrorMessages.CommonErrorMessages.INVALID_LOGIC_OPERATOR);
    }

    // Validate filters if provided
    if (leadRequestModel.hasMultipleFilters()) {
      Set<String> validColumns =
          new HashSet<>(
              Arrays.asList(
                  "leadId",
                  "firstName",
                  "lastName",
                  "email",
                  "address",
                  "website",
                  "phone",
                  "companySize",
                  "title",
                  "leadStatus",
                  "company",
                  "annualRevenue",
                  "fax",
                  "isDeleted",
                  "clientId",
                  "addressId",
                  "createdById",
                  "assignedAgentId",
                  "createdUser",
                  "modifiedUser",
                  "createdAt",
                  "updatedAt",
                  "notes"));

      for (FilterCondition filter : leadRequestModel.getFilters()) {
        // Validate column name
        if (!validColumns.contains(filter.getColumn())) {
          throw new BadRequestException(
              String.format(
                  ErrorMessages.LeadsErrorMessages.INVALID_COLUMN_NAME_WITH_VALID_COLUMNS_FORMAT,
                  filter.getColumn(),
                  String.join(", ", validColumns)));
        }

        // Validate operator
        if (!filter.isValidOperator()) {
          throw new BadRequestException(
              String.format(
                  ErrorMessages.LeadsErrorMessages.INVALID_OPERATOR_FOR_COLUMN_FORMAT,
                  filter.getOperator(),
                  filter.getColumn()));
        }

        // Validate operator matches column type
        String columnType = leadFilterQueryBuilder.getColumnType(filter.getColumn());
        filter.validateOperatorForType(columnType, filter.getColumn());

        // Validate value presence
        filter.validateValuePresence();
      }
    }

    // Create custom Pageable with proper offset handling
    Pageable pageable =
        new PageRequest(0, pageSize, Sort.by("leadId").descending()) {
          @Override
          public long getOffset() {
            return start;
          }
        };

    // Use the filter query builder for multi-filter support
    String logicOperator =
        leadRequestModel.getLogicOperator() != null ? leadRequestModel.getLogicOperator() : "AND";

    // Execute paginated query with clientId filter
    Page<Lead> page =
        leadFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            getClientId(),
            logicOperator,
            leadRequestModel.getFilters(),
            leadRequestModel.isIncludeDeleted(),
            pageable);

    // Convert Lead entities to LeadResponseModel (relationships already loaded)
    PaginationBaseResponseModel<LeadResponseModel> response = new PaginationBaseResponseModel<>();
    response.setData(page.getContent().stream().map(LeadResponseModel::new).toList());
    response.setTotalDataCount(page.getTotalElements());

    return response;
  }

  /**
   * Retrieves detailed information for a specific lead by ID.
   *
   * @param leadId The unique identifier of the lead
   * @return LeadResponseModel with complete details including address and user information
   */
  @Override
  @Transactional(readOnly = true)
  public LeadResponseModel getLeadDetailsById(Long leadId) {
    Lead lead = leadRepository.findLeadWithDetailsById(leadId, getClientId());
    if (lead == null) {
      throw new NotFoundException(ErrorMessages.LEAD_NOT_FOUND);
    }

    return new LeadResponseModel(lead);
  }

  /**
   * Retrieves detailed information for a specific lead by email address.
   *
   * @param email The email address of the lead
   * @return LeadResponseModel with complete details including address and user information
   */
  @Override
  @Transactional(readOnly = true)
  public LeadResponseModel getLeadDetailsByEmail(String email) {
    Lead lead = leadRepository.findLeadWithDetailsByEmail(email, getClientId());
    if (lead == null) {
      throw new NotFoundException(ErrorMessages.LEAD_NOT_FOUND);
    }

    return new LeadResponseModel(lead);
  }

  /**
   * Creates a new lead in the system.
   *
   * @param leadRequestModel The lead data to create
   */
  @Override
  @Transactional
  public void createLead(LeadRequestModel leadRequestModel) {
    if (leadRequestModel == null) {
      throw new BadRequestException(ErrorMessages.LeadsErrorMessages.ER009);
    }
    // Call the transactional overloaded method via the Spring proxy to ensure AOP interceptors are
    // applied
    LeadService proxy = applicationContext.getBean(LeadService.class);
    proxy.createLead(leadRequestModel, getUser(), true, getClientId(), getUserId());
  }

  /**
   * Updates an existing lead with new information.
   *
   * @param leadId The unique identifier of the lead to update
   * @param leadRequestModel The updated lead data
   */
  @Override
  @Transactional
  public void updateLead(Long leadId, LeadRequestModel leadRequestModel) {
    if (leadRequestModel == null) {
      throw new BadRequestException(ErrorMessages.LeadsErrorMessages.ER009);
    }
    // Get security context
    Long currentClientId = getClientId();
    Long currentUserId = getUserId();
    String authenticatedUser = getUser();

    Lead existingLead =
        leadRepository.findLeadWithDetailsByIdIncludingDeleted(leadId, currentClientId);
    if (existingLead == null) {
      throw new NotFoundException(ErrorMessages.LEAD_NOT_FOUND);
    }

    // Don't allow updating deleted leads
    if (existingLead.getIsDeleted()) {
      throw new NotFoundException(ErrorMessages.LEAD_NOT_FOUND);
    }

    // Set clientId and createdById from security context/existing lead (not from
    // frontend)
    leadRequestModel.setClientId(currentClientId);
    leadRequestModel.setCreatedById(existingLead.getCreatedById()); // Preserve original creator

    // Update address
    updateLeadAddress(leadRequestModel, existingLead, authenticatedUser);

    // Use the Lead constructor that handles validation and field mapping for
    // updates
    Lead updatedLead = new Lead(leadRequestModel, authenticatedUser, existingLead);
    leadRepository.save(updatedLead);

    // Logging
    userLogService.logData(
        currentUserId,
        SuccessMessages.LeadSuccessMessages.UPDATE_LEAD + updatedLead.getLeadId(),
        ApiRoutes.LeadsSubRoute.UPDATE_LEAD);
  }

  /**
   * Toggles the active status of a lead.
   *
   * @param leadId The unique identifier of the lead to toggle
   */
  @Override
  @Transactional
  public void toggleLead(Long leadId) {
    Lead lead = leadRepository.findLeadWithDetailsByIdIncludingDeleted(leadId, getClientId());
    if (lead == null) {
      throw new NotFoundException(ErrorMessages.LEAD_NOT_FOUND);
    }

    // Toggle the deleted status (active/inactive)
    lead.setIsDeleted(!lead.getIsDeleted());
    lead.setModifiedUser(getUser());

    leadRepository.save(lead);

    // Logging
    userLogService.logData(
        getUserId(),
        SuccessMessages.LeadSuccessMessages.TOGGLE_LEAD + lead.getLeadId(),
        ApiRoutes.LeadsSubRoute.TOGGLE_LEAD);
  }

  /**
   * Creates multiple leads asynchronously in the system with partial success support.
   *
   * <p>This method processes leads in a background thread with the following characteristics: -
   * Supports partial success: if some leads fail validation, others still succeed - Sends detailed
   * results to user via message notification after processing completes - NOT_SUPPORTED: Runs
   * without a transaction to avoid rollback-only issues when individual lead creations fail
   *
   * @param leads List of LeadRequestModel containing the lead data to create
   * @param requestingUserId The ID of the user making the request (captured from security context)
   * @param requestingUserLoginName The loginName of the user making the request (captured from
   *     security context)
   * @param requestingClientId The client ID of the user making the request (captured from security
   *     context)
   */
  @Override
  @Async
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void bulkCreateLeadsAsync(
      List<LeadRequestModel> leads,
      Long requestingUserId,
      String requestingUserLoginName,
      Long requestingClientId) {
    try {
      // Validate input
      if (leads == null || leads.isEmpty()) {
        throw new BadRequestException(
            String.format(ErrorMessages.CommonErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY, "Lead"));
      }

      com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> response =
          new com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<>();
      response.setTotalRequested(leads.size());

      int successCount = 0;
      int failureCount = 0;

      // Process each lead individually
      for (LeadRequestModel leadRequest : leads) {
        try {
          // Call createLead with explicit createdUser and shouldLog = false (bulk logs
          // collectively)
          createLead(
              leadRequest, requestingUserLoginName, false, requestingClientId, requestingUserId);

          // If we get here, lead was created successfully
          // Fetch the created lead to get the leadId
          Lead createdLead =
              leadRepository.findLeadWithDetailsByEmail(leadRequest.getEmail(), requestingClientId);
          if (createdLead != null) {
            response.addSuccess(leadRequest.getEmail(), createdLead.getLeadId());
            successCount++;
          }

        } catch (BadRequestException bre) {
          // Validation or business logic error
          response.addFailure(
              leadRequest.getEmail() != null ? leadRequest.getEmail() : UNKNOWN_VALUE,
              bre.getMessage());
          failureCount++;
        } catch (Exception e) {
          // Unexpected error
          response.addFailure(
              leadRequest.getEmail() != null ? leadRequest.getEmail() : UNKNOWN_VALUE,
              String.format(
                  ErrorMessages.LeadsErrorMessages.BULK_ITEM_ERROR_FORMAT, e.getMessage()));
          failureCount++;
        }
      }

      // Log bulk lead creation (using captured context values)
      userLogService.logDataWithContext(
          requestingUserId,
          requestingUserLoginName,
          requestingClientId,
          SuccessMessages.LeadSuccessMessages.INSERT_LEAD
              + " (Bulk: "
              + successCount
              + " succeeded, "
              + failureCount
              + " failed)",
          ApiRoutes.LeadsSubRoute.BULK_CREATE_LEAD);

      response.setSuccessCount(successCount);
      response.setFailureCount(failureCount);

      // Create a message with the bulk insert results using the helper (using
      // captured context)
      BulkInsertHelper.createDetailedBulkInsertResultMessage(
          response,
          new BulkInsertHelper.BulkMessageTemplate("Lead", "Leads", "Email", "Lead ID"),
          new BulkInsertHelper.NotificationContext(
              messageService, requestingUserId, requestingUserLoginName, requestingClientId));

    } catch (Exception e) {
      // Still send a message to user about the failure (using captured userId)
      com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> errorResponse =
          new com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<>();
      errorResponse.setTotalRequested(leads != null ? leads.size() : 0);
      errorResponse.setSuccessCount(0);
      errorResponse.setFailureCount(leads != null ? leads.size() : 0);
      errorResponse.addFailure(
          "bulk_import",
          String.format(
              ErrorMessages.LeadsErrorMessages.BULK_CRITICAL_ERROR_FORMAT, e.getMessage()));
      BulkInsertHelper.createDetailedBulkInsertResultMessage(
          errorResponse,
          new BulkInsertHelper.BulkMessageTemplate("Lead", "Leads", "Email", "Lead ID"),
          new BulkInsertHelper.NotificationContext(
              messageService, requestingUserId, requestingUserLoginName, requestingClientId));
    }
  }

  /**
   * Creates multiple leads synchronously in a single operation (for testing). This is a synchronous
   * wrapper that processes leads immediately and returns results.
   *
   * @param leads List of LeadRequestModel containing the lead data to create
   * @return BulkInsertResponseModel containing success/failure details for each lead
   */
  @Override
  @Transactional
  public com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> bulkCreateLeads(
      List<LeadRequestModel> leads) {
    // Validate input
    if (leads == null || leads.isEmpty()) {
      throw new BadRequestException(
          String.format(ErrorMessages.CommonErrorMessages.LIST_CANNOT_BE_NULL_OR_EMPTY, "Lead"));
    }

    com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> response =
        new com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<>();
    response.setTotalRequested(leads.size());

    int successCount = 0;
    int failureCount = 0;

    // Process each lead individually
    for (LeadRequestModel leadRequest : leads) {
      try {
        // Call createLead with current user and shouldLog = false
        createLead(leadRequest, getUser(), false, getClientId(), getUserId());

        // If we get here, lead was created successfully
        // Fetch the created lead to get the leadId
        Lead createdLead =
            leadRepository.findLeadWithDetailsByEmail(leadRequest.getEmail(), getClientId());
        if (createdLead != null) {
          response.addSuccess(leadRequest.getEmail(), createdLead.getLeadId());
          successCount++;
        }

      } catch (BadRequestException bre) {
        // Validation or business logic error
        response.addFailure(
            leadRequest.getEmail() != null ? leadRequest.getEmail() : UNKNOWN_VALUE,
            bre.getMessage());
        failureCount++;
      } catch (Exception e) {
        // Unexpected error
        response.addFailure(
            leadRequest.getEmail() != null ? leadRequest.getEmail() : UNKNOWN_VALUE,
            String.format(ErrorMessages.LeadsErrorMessages.BULK_ITEM_ERROR_FORMAT, e.getMessage()));
        failureCount++;
      }
    }

    // Log bulk lead creation
    userLogService.logData(
        getUserId(),
        SuccessMessages.LeadSuccessMessages.INSERT_LEAD
            + " (Bulk: "
            + successCount
            + " succeeded, "
            + failureCount
            + " failed)",
        ApiRoutes.LeadsSubRoute.BULK_CREATE_LEAD);

    response.setSuccessCount(successCount);
    response.setFailureCount(failureCount);

    return response;
  }

  // ==================== HELPER METHODS ====================

  /**
   * Creates a new lead in the system with explicit createdUser. This variant is used for async
   * operations where security context is not available.
   *
   * @param leadRequestModel The lead data to create
   * @param createdUser The loginName of the user creating this lead (for async operations)
   * @param shouldLog Whether to log this individual lead creation (false for bulk operations)
   * @param clientId The client ID captured from authenticated context
   * @param userId The user ID captured from authenticated context
   * @throws BadRequestException if the lead data is invalid or incomplete
   */
  @Transactional
  private void createLead(
      LeadRequestModel leadRequestModel,
      String createdUser,
      boolean shouldLog,
      Long clientId,
      Long userId) {
    // Set clientId and createdById from captured/authenticated context (not from
    // frontend)
    leadRequestModel.setClientId(clientId);
    leadRequestModel.setCreatedById(userId);

    // Create address first if provided
    if (leadRequestModel.getAddress() != null) {
      Address savedAddress = createLeadAddress(leadRequestModel, createdUser);
      leadRequestModel.setAddressId(savedAddress.getAddressId());
    }

    // Use the Lead constructor that handles validation and field mapping
    Lead lead = new Lead(leadRequestModel, createdUser);
    Lead savedLead = leadRepository.save(lead);

    // Log lead creation (skip for bulk operations as they log collectively)
    if (shouldLog) {
      userLogService.logData(
          userId,
          SuccessMessages.LeadSuccessMessages.INSERT_LEAD + savedLead.getLeadId(),
          ApiRoutes.LeadsSubRoute.CREATE_LEAD);
    }
  }

  /**
   * Creates address for a lead.
   *
   * @param leadRequestModel The lead request containing address data
   * @param createdUser The loginName of the user creating this address
   * @return The saved Address entity
   */
  private Address createLeadAddress(LeadRequestModel leadRequestModel, String createdUser) {
    return addressRepository.save(new Address(leadRequestModel.getAddress(), createdUser));
  }

  /**
   * Updates address for an existing lead.
   *
   * @param leadRequestModel The lead request containing address data
   * @param existingLead The existing lead entity
   * @param modifiedUser The loginName of the user modifying this address
   */
  private void updateLeadAddress(
      LeadRequestModel leadRequestModel, Lead existingLead, String modifiedUser) {
    if (leadRequestModel.getAddress() != null) {
      Address savedAddress =
          addressRepository.save(
              new Address(leadRequestModel.getAddress(), modifiedUser, existingLead.getAddress()));
      existingLead.setAddressId(savedAddress.getAddressId());
    }
  }
}
