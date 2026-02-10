package com.example.SpringApi.Services;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.FilterQueryBuilder.PromoFilterQueryBuilder;
import com.example.SpringApi.Helpers.BulkInsertHelper;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.DatabaseModels.Promo;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.PromoRequestModel;
import com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.PromoResponseModel;
import com.example.SpringApi.Repositories.PromoRepository;
import com.example.SpringApi.Services.Interface.IPromoSubTranslator;
import com.example.SpringApi.SuccessMessages;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for Promo operations.
 *
 * <p>
 * This service handles all business logic related to promotional codes
 * including CRUD
 * operations, batch processing, and promo code validation.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class PromoService extends BaseService implements IPromoSubTranslator {

  private final PromoRepository promoRepository;
  private final UserLogService userLogService;
  private final PromoFilterQueryBuilder promoFilterQueryBuilder;
  private final MessageService messageService;

  @Autowired
  public PromoService(
      PromoRepository promoRepository,
      UserLogService userLogService,
      PromoFilterQueryBuilder promoFilterQueryBuilder,
      MessageService messageService,
      HttpServletRequest request) {
    super();
    this.promoRepository = promoRepository;
    this.userLogService = userLogService;
    this.promoFilterQueryBuilder = promoFilterQueryBuilder;
    this.messageService = messageService;
  }

  /**
   * Retrieves promos in batches with pagination support.
   *
   * @param paginationBaseRequestModel The pagination parameters
   * @return Paginated response containing promo data
   */
  @Override
  public PaginationBaseResponseModel<Promo> getPromosInBatches(
      PaginationBaseRequestModel paginationBaseRequestModel) {
    // Valid columns for filtering
    Set<String> validColumns = new HashSet<>(Arrays.asList("promoId", "promoCode", "description", "discountValue",
        "isPercent", "isDeleted", "createdUser", "modifiedUser", "createdAt", "updatedAt", "notes", "startDate", "expiryDate"));

    // Validate filter conditions if provided (do this FIRST before pagination validation)
    if (paginationBaseRequestModel.getFilters() != null && !paginationBaseRequestModel.getFilters().isEmpty()) {
      for (PaginationBaseRequestModel.FilterCondition filter : paginationBaseRequestModel.getFilters()) {
        // Validate column name
        if (filter.getColumn() != null && !validColumns.contains(filter.getColumn())) {
          throw new BadRequestException("Invalid column name: " + filter.getColumn());
        }

        // Validate operator (FilterCondition.setOperator auto-normalizes symbols to
        // words)
        if (!filter.isValidOperator()) {
          throw new BadRequestException("Invalid operator: " + filter.getOperator());
        }

        // Validate column type matches operator - this throws IllegalArgumentException
        String columnType = promoFilterQueryBuilder.getColumnType(filter.getColumn());
        filter.validateOperatorForType(columnType, filter.getColumn());

        // Validate value presence
        filter.validateValuePresence();
      }
    }

    // Calculate page size and offset
    int start = paginationBaseRequestModel.getStart();
    int end = paginationBaseRequestModel.getEnd();
    int pageSize = end - start;

    // Validate page size
    if (pageSize <= 0) {
      throw new BadRequestException(ErrorMessages.CommonErrorMessages.InvalidPagination);
    }

    // Create custom Pageable with proper offset handling
    org.springframework.data.domain.Pageable pageable = new PageRequest(0, pageSize, Sort.by("promoId").descending()) {
      @Override
      public long getOffset() {
        return start;
      }
    };

    // Use filter query builder for dynamic filtering
    Page<Promo> page = promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
        getClientId(),
        paginationBaseRequestModel.getSelectedIds(),
        paginationBaseRequestModel.getLogicOperator() != null ? paginationBaseRequestModel.getLogicOperator() : "AND",
        paginationBaseRequestModel.getFilters(),
        paginationBaseRequestModel.isIncludeDeleted(),
        pageable);

    PaginationBaseResponseModel<Promo> response = new PaginationBaseResponseModel<>();
    response.setData(page.getContent());
    response.setTotalDataCount(page.getTotalElements());

    return response;
  }

  /**
   * Creates a new promo.
   *
   * @param promoRequestModel The promo request model containing promo data
   * @throws NotFoundException   if required dependencies are not found
   * @throws BadRequestException if promo code already exists
   */
  @Override
  @Transactional
  public void createPromo(PromoRequestModel promoRequestModel) {
    createPromo(promoRequestModel, getUser(), true);
  }

  /**
   * Retrieves promo details by ID.
   *
   * @param id The ID of the promo to retrieve
   * @return The promo details as response model
   */
  @Override
  public PromoResponseModel getPromoDetailsById(long id) {
    Promo promo = promoRepository
        .findByPromoIdAndClientId(id, getClientId())
        .orElseThrow(() -> new NotFoundException(ErrorMessages.PromoErrorMessages.InvalidId));
    return new PromoResponseModel(promo);
  }

  /**
   * Toggles the status of a promo (soft delete/restore).
   *
   * @param id The ID of the promo to toggle
   * @throws NotFoundException if the promo is not found
   */
  @Override
  @Transactional
  public void togglePromo(long id) {
    Promo promo = promoRepository
        .findByPromoIdAndClientId(id, getClientId())
        .orElseThrow(() -> new NotFoundException(ErrorMessages.PromoErrorMessages.InvalidId));

    // Toggle the isDeleted flag
    promo.setIsDeleted(!promo.getIsDeleted());
    promo.setModifiedUser(getUser());

    // save the updated promo
    promoRepository.save(promo);

    // Log the operation
    userLogService.logData(
        getUserId(),
        SuccessMessages.PromoSuccessMessages.ToggledPromo + id,
        ApiRoutes.PromosSubRoute.TOGGLE_PROMO);
  }

  /**
   * Retrieves promo details by promo code.
   * Promo code lookup is case-insensitive (converted to uppercase).
   *
   * @param promoCode The promo code to search for
   * @return The promo details as response model
   * @throws BadRequestException if promo code is null or empty
   * @throws NotFoundException   if promo code is not found
   */
  @Override
  public PromoResponseModel getPromoDetailsByName(String promoCode) {
    // Validate promo code is not null or empty
    if (promoCode == null || promoCode.trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.PromoErrorMessages.InvalidPromoCode);
    }

    // Convert to uppercase for case-insensitive lookup
    Promo promo = promoRepository
        .findByPromoCodeAndClientId(promoCode.toUpperCase(), getClientId())
        .orElseThrow(
            () -> new NotFoundException(ErrorMessages.PromoErrorMessages.InvalidName));
    return new PromoResponseModel(promo);
  }

  /**
   * Creates multiple promos asynchronously in the system with partial success
   * support.
   * 
   * This method processes promos in a background thread with the following
   * characteristics:
   * - Supports partial success: if some promos fail validation, others still
   * succeed
   * - Sends detailed results to user via message notification after processing
   * completes
   * - NOT_SUPPORTED: Runs without a transaction to avoid rollback-only issues
   * when individual promo creations fail
   * 
   * @param promos                  List of PromoRequestModel containing the promo
   *                                data to create
   * @param requestingUserId        The ID of the user making the request
   *                                (captured from security context)
   * @param requestingUserLoginName The loginName of the user making the request
   *                                (captured from security context)
   * @param requestingClientId      The client ID of the user making the request
   *                                (captured from security context)
   */
  @Override
  @org.springframework.scheduling.annotation.Async
  @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
  public void bulkCreatePromosAsync(List<PromoRequestModel> promos, Long requestingUserId,
      String requestingUserLoginName, Long requestingClientId) {
    try {
      // Validate input
      if (promos == null || promos.isEmpty()) {
        throw new BadRequestException(
            String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "Promo"));
      }

      BulkInsertResponseModel<Long> response = new BulkInsertResponseModel<>();
      response.setTotalRequested(promos.size());

      int successCount = 0;
      int failureCount = 0;

      // Process each promo individually
      for (PromoRequestModel promoRequest : promos) {
        try {
          // Call createPromo with explicit createdUser and shouldLog = false (bulk logs
          // collectively)
          createPromo(promoRequest, requestingUserLoginName, false);

          // If we get here, promo was created successfully
          // Fetch the created promo to get the promoId
          Optional<Promo> createdPromo = promoRepository.findByPromoCodeAndClientId(
              promoRequest.getPromoCode().toUpperCase(), requestingClientId);
          if (createdPromo.isPresent()) {
            response.addSuccess(promoRequest.getPromoCode(), createdPromo.get().getPromoId());
            successCount++;
          }

        } catch (BadRequestException bre) {
          // Validation or business logic error
          response.addFailure(
              promoRequest.getPromoCode() != null ? promoRequest.getPromoCode() : "unknown",
              bre.getMessage());
          failureCount++;
        } catch (Exception e) {
          // Unexpected error
          response.addFailure(
              promoRequest.getPromoCode() != null ? promoRequest.getPromoCode() : "unknown",
              "Error: " + e.getMessage());
          failureCount++;
        }
      }

      // Log bulk promo creation (using captured context values)
      userLogService.logDataWithContext(
          requestingUserId,
          requestingUserLoginName,
          requestingClientId,
          SuccessMessages.PromoSuccessMessages.CreatePromo + " (Bulk: " + successCount + " succeeded, " + failureCount
              + " failed)",
          ApiRoutes.PromosSubRoute.BULK_CREATE_PROMO);

      response.setSuccessCount(successCount);
      response.setFailureCount(failureCount);

      // Create a message with the bulk insert results using the helper (using
      // captured context)
      BulkInsertHelper.createDetailedBulkInsertResultMessage(
          response, "Promo", "Promos", "Promo Code", "Promo ID",
          messageService, requestingUserId, requestingUserLoginName, requestingClientId);

    } catch (Exception e) {
      // Still send a message to user about the failure (using captured userId)
      BulkInsertResponseModel<Long> errorResponse = new BulkInsertResponseModel<>();
      errorResponse.setTotalRequested(promos != null ? promos.size() : 0);
      errorResponse.setSuccessCount(0);
      errorResponse.setFailureCount(promos != null ? promos.size() : 0);
      errorResponse.addFailure("bulk_import", "Critical error: " + e.getMessage());
      BulkInsertHelper.createDetailedBulkInsertResultMessage(
          errorResponse, "Promo", "Promos", "Promo Code", "Promo ID",
          messageService, requestingUserId, requestingUserLoginName, requestingClientId);
    }
  }

  // ==================== HELPER METHODS ====================

  /**
   * Creates a new promo with explicit createdUser.
   * This variant is used for async operations where security context is not
   * available.
   * 
   * @param promoRequestModel The promo data to create
   * @param createdUser       The loginName of the user creating this promo (for
   *                          async operations)
   * @param shouldLog         Whether to log this individual promo creation (false
   *                          for bulk operations)
   * @throws BadRequestException if promo code already exists
   */
  @Transactional
  protected void createPromo(PromoRequestModel promoRequestModel, String createdUser, boolean shouldLog) {
    // Get security context
    Long currentClientId = getClientId();
    Long currentUserId = getUserId();

    // Validate request model and required fields
    if (promoRequestModel == null) {
      throw new BadRequestException(ErrorMessages.PromoErrorMessages.InvalidRequest);
    }

    // Validate promo code is not null or empty
    if (promoRequestModel.getPromoCode() == null || promoRequestModel.getPromoCode().trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.PromoErrorMessages.InvalidPromoCode);
    }

    // Client ID consistency check (Service level responsibility)
    if (promoRequestModel.getClientId() != null && !promoRequestModel.getClientId().equals(currentClientId)) {
      throw new BadRequestException(ErrorMessages.PromoErrorMessages.ClientIdMismatch);
    }

    // Check for overlapping promo codes in the same date range
    java.util.List<Promo> overlappingPromos = promoRepository.findOverlappingPromos(
        promoRequestModel.getPromoCode().toUpperCase(),
        currentClientId,
        promoRequestModel.getStartDate(),
        promoRequestModel.getExpiryDate());

    if (!overlappingPromos.isEmpty()) {
      throw new BadRequestException(ErrorMessages.PromoErrorMessages.OverlappingPromoCode);
    }

    // Create and save promo
    Promo promo = new Promo(promoRequestModel, createdUser, currentClientId);
    promoRepository.save(promo);

    // Log promo creation (skip for bulk operations as they log collectively)
    if (shouldLog) {
      userLogService.logData(
          currentUserId,
          SuccessMessages.PromoSuccessMessages.CreatePromo + promoRequestModel.getPromoCode(),
          ApiRoutes.PromosSubRoute.CREATE_PROMO);
    }
  }
}
