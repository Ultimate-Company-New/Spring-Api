package com.example.SpringApi.Services;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
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

  @Autowired
  public PromoService(
      PromoRepository promoRepository, UserLogService userLogService, HttpServletRequest request) {
    super(request);
    this.promoRepository = promoRepository;
    this.userLogService = userLogService;
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
        new HashSet<>(Arrays.asList("promoId", "promoCode", "description", "discountValue"));

    // Validate column name
    if (paginationBaseRequestModel.getColumnName() != null
        && !validColumns.contains(paginationBaseRequestModel.getColumnName())) {
      throw new BadRequestException(
          "Invalid column name: " + paginationBaseRequestModel.getColumnName());
    }

    // Calculate page number and size
    int pageSize = paginationBaseRequestModel.getEnd() - paginationBaseRequestModel.getStart();
    int pageNumber = paginationBaseRequestModel.getStart() / pageSize;

    PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, Sort.by("promoId").descending());

    Page<Promo> promos =
        promoRepository.findPaginatedPromos(
            paginationBaseRequestModel.getColumnName(),
            paginationBaseRequestModel.getCondition(),
            paginationBaseRequestModel.getFilterExpr(),
            paginationBaseRequestModel.isIncludeDeleted(),
            pageRequest);

    PaginationBaseResponseModel<Promo> response = new PaginationBaseResponseModel<>();
    response.setData(promos.getContent());
    response.setTotalDataCount(promos.getTotalElements());

    return response;
  }

  /**
   * Creates a new promo.
   *
   * @param promoRequestModel The promo request model containing promo data
   * @throws NotFoundException if required dependencies are not found
   */
  @Override
  public void createPromo(PromoRequestModel promoRequestModel) {
    promoRepository.save(new Promo(promoRequestModel, getUser()));
    userLogService.logData(
        getUser(),
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
            .findById(id)
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
            .findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessages.PromoErrorMessages.InvalidId));

    // Toggle the isDeleted flag
    promo.setIsDeleted(!promo.getIsDeleted());
    promo.setModifiedUser(getUser());

    // save the updated promo
    promoRepository.save(promo);

    // Log the operation
    userLogService.logData(
        getUser(),
        SuccessMessages.PromoSuccessMessages.ToggledPromo + id,
        ApiRoutes.PromosSubRoute.TOGGLE_PROMO);
  }

  /**
   * Retrieves promo details by promo code.
   *
   * @param promoCode The promo code to search for
   * @return The promo details as response model
   */
  @Override
  public PromoResponseModel getPromoDetailsByName(String promoCode) {
    Promo promo =
        promoRepository
            .findByPromoCode(promoCode)
            .orElseThrow(
                () -> new NotFoundException(ErrorMessages.PromoErrorMessages.InvalidPromoCode));
    return new PromoResponseModel(promo);
  }
}
