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
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.PromoResponseModel;
import com.example.SpringApi.Repositories.PromoRepository;
import com.example.SpringApi.Services.Interface.IPromoSubTranslator;
import com.example.SpringApi.SuccessMessages;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Service implementation for Promo operations.
 *
 * <p>This service handles all business logic related to promotional codes including CRUD
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
    Set<String> validColumns =
        new HashSet<>(Arrays.asList("promoId", "promoCode", "description", "discountValue", 
            "isPercent", "isDeleted", "createdUser", "modifiedUser", "createdAt", "updatedAt", "notes"));

    // Validate filter conditions if provided
    if (paginationBaseRequestModel.getFilters() != null && !paginationBaseRequestModel.getFilters().isEmpty()) {
      for (PaginationBaseRequestModel.FilterCondition filter : paginationBaseRequestModel.getFilters()) {
        // Validate column name
        if (filter.getColumn() != null && !validColumns.contains(filter.getColumn())) {
          throw new BadRequestException("Invalid column name: " + filter.getColumn());
        }

        // Validate operator
        Set<String> validOperators = new HashSet<>(Arrays.asList(
            "equals", "notEquals", "contains", "notContains", "startsWith", "endsWith",
            "greaterThan", "lessThan", "greaterThanOrEqual", "lessThanOrEqual",
            "isEmpty", "isNotEmpty"
        ));
        if (filter.getOperator() != null && !validOperators.contains(filter.getOperator())) {
          throw new BadRequestException("Invalid operator: " + filter.getOperator());
        }

        // Validate column type matches operator
        String columnType = promoFilterQueryBuilder.getColumnType(filter.getColumn());
        if ("boolean".equals(columnType) && !filter.getOperator().equals("equals") && !filter.getOperator().equals("notEquals")) {
          throw new BadRequestException("Boolean columns only support 'equals' and 'notEquals' operators");
        }
        if ("date".equals(columnType) || "number".equals(columnType)) {
          Set<String> numericDateOperators = new HashSet<>(Arrays.asList(
              "equals", "notEquals", "greaterThan", "lessThan", "greaterThanOrEqual", "lessThanOrEqual"
          ));
          if (!numericDateOperators.contains(filter.getOperator())) {
            throw new BadRequestException(columnType + " columns only support numeric comparison operators");
          }
        }
      }
    }

    // Calculate page size and offset
    int start = paginationBaseRequestModel.getStart();
    int end = paginationBaseRequestModel.getEnd();
    int pageSize = end - start;

    // Validate page size
    if (pageSize <= 0) {
      throw new BadRequestException("Invalid pagination: end must be greater than start");
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
        pageable
    );

    PaginationBaseResponseModel<Promo> response = new PaginationBaseResponseModel<>();
    response.setData(page.getContent());
    response.setTotalDataCount(page.getTotalElements());

    return response;
  }

  /**
   * Creates a new promo.
   *
   * @param promoRequestModel The promo request model containing promo data
   * @throws NotFoundException if required dependencies are not found
   * @throws BadRequestException if promo code already exists
   */
  @Override
  public void createPromo(PromoRequestModel promoRequestModel) {
    // Check for duplicate promo code
      Promo promo = new Promo(promoRequestModel, getUser(), getClientId());

      Optional<Promo> existingPromo = promoRepository.findByPromoCodeAndClientId(
        promoRequestModel.getPromoCode().toUpperCase(), getClientId());

      if (existingPromo.isPresent()) {
      throw new BadRequestException(ErrorMessages.PromoErrorMessages.DuplicateName);
    }
    
    // Ensure clientId is set from current context for multi-tenant isolation
    promoRepository.save(promo);
    userLogService.logData(
        getUserId(),
        SuccessMessages.PromoSuccessMessages.CreatePromo + promoRequestModel.getPromoCode(),
        ApiRoutes.PromosSubRoute.CREATE_PROMO);
  }

  /**
   * Retrieves promo details by ID.
   *
   * @param id The ID of the promo to retrieve
   * @return The promo details as response model
   */
  @Override
  public PromoResponseModel getPromoDetailsById(long id) {
    Promo promo =
        promoRepository
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
  public void togglePromo(long id) {
    Promo promo =
        promoRepository
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
   * @throws NotFoundException if promo code is not found
   */
  @Override
  public PromoResponseModel getPromoDetailsByName(String promoCode) {
    // Validate promo code is not null or empty
    if (promoCode == null || promoCode.trim().isEmpty()) {
      throw new BadRequestException(ErrorMessages.PromoErrorMessages.InvalidPromoCode);
    }
    
    // Convert to uppercase for case-insensitive lookup
    Promo promo =
        promoRepository
            .findByPromoCodeAndClientId(promoCode.toUpperCase(), getClientId())
            .orElseThrow(
                () -> new NotFoundException(ErrorMessages.PromoErrorMessages.InvalidName));
    return new PromoResponseModel(promo);
  }

  /**
   * Creates multiple promos in a single operation.
   * 
   * @param promos List of PromoRequestModel containing the promo data to insert
   * @return BulkInsertResponseModel containing success/failure details for each promo
   */
  @Override
  public com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> bulkCreatePromos(java.util.List<PromoRequestModel> promos) {
    if (promos == null || promos.isEmpty()) {
      throw new BadRequestException("Promo list cannot be null or empty");
    }

    com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<Long> response = 
        new com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel<>();
    response.setTotalRequested(promos.size());
    
    int successCount = 0;
    int failureCount = 0;
    
    for (PromoRequestModel promoRequest : promos) {
      try {
        createPromo(promoRequest);
        
        Optional<Promo> createdPromo = promoRepository.findByPromoCodeAndClientId(
            promoRequest.getPromoCode().toUpperCase(), getClientId());
        if (createdPromo.isPresent()) {
          response.addSuccess(promoRequest.getPromoCode(), createdPromo.get().getPromoId());
          successCount++;
        }
      } catch (BadRequestException bre) {
        response.addFailure(
            promoRequest.getPromoCode() != null ? promoRequest.getPromoCode() : "unknown", 
            bre.getMessage()
        );
        failureCount++;
      } catch (Exception e) {
        response.addFailure(
            promoRequest.getPromoCode() != null ? promoRequest.getPromoCode() : "unknown", 
            "Error: " + e.getMessage()
        );
        failureCount++;
      }
    }
    
    userLogService.logData(getUserId(), 
        SuccessMessages.PromoSuccessMessages.CreatePromo + " (Bulk: " + successCount + " succeeded, " + failureCount + " failed)",
        ApiRoutes.PromosSubRoute.BULK_CREATE_PROMO);
    
    response.setSuccessCount(successCount);
    response.setFailureCount(failureCount);
    
    BulkInsertHelper.createBulkInsertResultMessage(response, "Promo", messageService, getUserId(), getUser(), getClientId());
    
    return response;
  }
}
