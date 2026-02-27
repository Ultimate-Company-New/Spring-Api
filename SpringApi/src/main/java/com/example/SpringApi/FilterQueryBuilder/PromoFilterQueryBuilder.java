package com.example.SpringApi.FilterQueryBuilder;

import com.example.SpringApi.Models.DatabaseModels.Promo;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.FilterCondition;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Promo-specific filter query builder that handles dynamic multi-filter queries.
 *
 * <p>This class combines: 1. Column mapping logic (which columns map to which database fields) 2.
 * Query building logic (using BaseFilterQueryBuilder) 3. Query execution logic (executing the built
 * queries via EntityManager)
 */
@Component
public class PromoFilterQueryBuilder extends BaseFilterQueryBuilder {
  private static final String CLIENT_ID = "clientId";
  private static final String CLIENT_ID_PARAM = ":" + CLIENT_ID;

  private final EntityManager entityManager;

  // Constructor for Spring dependency injection
  public PromoFilterQueryBuilder(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  // ==================== Column Mapping Methods ====================

  @Override
  protected String mapColumnToField(String column) {
    switch (column) {
      case "promoId":
        return "p.promoId";
      case CLIENT_ID:
        return "p.clientId";
      case "promoCode":
        return "p.promoCode";
      case "description":
        return "p.description";
      case "discountValue":
        return "p.discountValue";
      case "isPercent":
        return "p.isPercent";
      case "isDeleted":
        return "p.isDeleted";
      case "notes":
        return "p.notes";
      case "createdUser":
        return "p.createdUser";
      case "modifiedUser":
        return "p.modifiedUser";
      case "createdAt":
        return "p.createdAt";
      case "updatedAt":
        return "p.updatedAt";
      default:
        return "p." + column;
    }
  }

  @Override
  protected List<String> getDateColumns() {
    return Arrays.asList("createdAt", "updatedAt");
  }

  @Override
  protected List<String> getBooleanColumns() {
    return Arrays.asList("isPercent", "isDeleted");
  }

  @Override
  protected List<String> getNumberColumns() {
    return Arrays.asList("promoId", CLIENT_ID, "discountValue");
  }

  /**
   * Gets the column type for validation purposes
   *
   * @param column The column name
   * @return "string", "number", "date", or "boolean"
   */
  public String getColumnType(String column) {
    if (getDateColumns().contains(column)) {
      return "date";
    } else if (getBooleanColumns().contains(column)) {
      return "boolean";
    } else if (getNumberColumns().contains(column)) {
      return "number";
    } else {
      return "string";
    }
  }

  // ==================== Query Execution Method ====================

  /**
   * Finds paginated promos with multiple filter conditions combined with AND/OR logic. Builds the
   * WHERE clause dynamically and executes the query.
   *
   * @param clientId The client ID to filter promos by
   * @param selectedIds List of specific promo IDs to include (null for all)
   * @param logicOperator "AND" or "OR" to combine filter conditions
   * @param filters List of filter conditions to apply
   * @param includeDeleted Whether to include deleted promos
   * @param pageable Pagination parameters
   * @return Page of promos matching the filter criteria
   */
  public Page<Promo> findPaginatedEntitiesWithMultipleFilters(
      Long clientId,
      List<Long> selectedIds,
      String logicOperator,
      List<FilterCondition> filters,
      boolean includeDeleted,
      Pageable pageable) {

    // Base query
    String baseQuery = "SELECT p FROM Promo p " + "WHERE p.clientId = " + CLIENT_ID_PARAM + " ";

    // Add selectedIds condition
    if (selectedIds != null && !selectedIds.isEmpty()) {
      baseQuery += "AND p.promoId IN :selectedIds ";
    }

    // Add includeDeleted condition
    if (!includeDeleted) {
      baseQuery += "AND p.isDeleted = false ";
    }

    // Build dynamic filter conditions using the query builder
    QueryResult filterResult = buildFilterConditions(filters, logicOperator);

    if (filterResult.hasConditions()) {
      baseQuery += "AND (" + filterResult.getWhereClause() + ") ";
    }

    // Add ordering
    baseQuery += "ORDER BY p.promoId DESC";

    // Count query
    String countQuery =
        "SELECT COUNT(p) FROM Promo p " + "WHERE p.clientId = " + CLIENT_ID_PARAM + " ";

    if (selectedIds != null && !selectedIds.isEmpty()) {
      countQuery += "AND p.promoId IN :selectedIds ";
    }

    if (!includeDeleted) {
      countQuery += "AND p.isDeleted = false ";
    }

    if (filterResult.hasConditions()) {
      countQuery += "AND (" + filterResult.getWhereClause() + ") ";
    }

    // Execute count query
    TypedQuery<Long> countTypedQuery = entityManager.createQuery(countQuery, Long.class);
    countTypedQuery.setParameter(CLIENT_ID, clientId);

    if (selectedIds != null && !selectedIds.isEmpty()) {
      countTypedQuery.setParameter("selectedIds", selectedIds);
    }

    // Set filter parameters
    for (Map.Entry<String, Object> entry : filterResult.getParameters().entrySet()) {
      countTypedQuery.setParameter(entry.getKey(), entry.getValue());
    }

    Long totalCount = countTypedQuery.getSingleResult();

    // Execute main query with pagination
    TypedQuery<Promo> mainQuery = entityManager.createQuery(baseQuery, Promo.class);
    mainQuery.setParameter(CLIENT_ID, clientId);

    if (selectedIds != null && !selectedIds.isEmpty()) {
      mainQuery.setParameter("selectedIds", selectedIds);
    }

    // Set filter parameters
    for (Map.Entry<String, Object> entry : filterResult.getParameters().entrySet()) {
      mainQuery.setParameter(entry.getKey(), entry.getValue());
    }

    // Apply pagination
    mainQuery.setFirstResult((int) pageable.getOffset());
    mainQuery.setMaxResults(pageable.getPageSize());

    List<Promo> promos = mainQuery.getResultList();

    return new PageImpl<>(promos, pageable, totalCount);
  }
}

