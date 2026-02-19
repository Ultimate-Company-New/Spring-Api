package com.example.SpringApi.FilterQueryBuilder;

import com.example.SpringApi.Models.DatabaseModels.Product;
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
 * Product-specific filter query builder that handles dynamic multi-filter queries.
 *
 * <p>This class combines: 1. Column mapping logic (which columns map to which database fields) 2.
 * Query building logic (using BaseFilterQueryBuilder) 3. Query execution logic (executing the built
 * queries via EntityManager)
 */
@Component
public class ProductFilterQueryBuilder extends BaseFilterQueryBuilder {
  private static final String CLIENT_ID = "clientId";
  private static final String CLIENT_ID_PARAM = ":" + CLIENT_ID;
  private static final String PICKUP_LOCATION_ID = "pickupLocationId";

  private final EntityManager entityManager;

  // Constructor for Spring dependency injection
  public ProductFilterQueryBuilder(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  // ==================== Column Mapping Methods ====================

  @Override
  protected String mapColumnToField(String column) {
    switch (column) {
      case "productId":
        return "p.productId";
      case CLIENT_ID:
        return "p.clientId";
      case "title":
        return "p.title";
      case "descriptionHtml":
        return "p.descriptionHtml";
      case "brand":
        return "p.brand";
      case "color":
        return "p.color";
      case "colorLabel":
        return "p.colorLabel";
      case "condition":
        return "p.condition";
      case "countryOfManufacture":
        return "p.countryOfManufacture";
      case "model":
        return "p.model";
      case "upc":
        return "p.upc";
      case "modificationHtml":
        return "p.modificationHtml";
      case "price":
        return "p.price";
      case "discount":
        return "p.discount";
      case "isDiscountPercent":
        return "p.isDiscountPercent";
      case "returnsAllowed":
        return "p.returnsAllowed";
      case "length":
        return "p.length";
      case "breadth":
        return "p.breadth";
      case "height":
        return "p.height";
      case "weightKgs":
        return "p.weightKgs";
      case "categoryId":
        return "p.categoryId";
      case "itemModified":
        return "p.itemModified";
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
        // Special case: filter through ProductPickupLocationMapping join
      case PICKUP_LOCATION_ID:
        return "pplm.pickupLocationId";
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
    return Arrays.asList("isDiscountPercent", "returnsAllowed", "itemModified", "isDeleted");
  }

  @Override
  protected List<String> getNumberColumns() {
    return Arrays.asList(
        "productId",
        CLIENT_ID,
        "price",
        "discount",
        "length",
        "breadth",
        "height",
        "weightKgs",
        "categoryId",
        PICKUP_LOCATION_ID);
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
   * Finds paginated products with multiple filter conditions combined with AND/OR logic. Builds the
   * WHERE clause dynamically and executes the query.
   *
   * <p>When filtering by pickupLocationId, only products with that pickup location are returned,
   * and only the mapping for that specific pickup location is included in the response.
   *
   * @param clientId The client ID to filter products by
   * @param selectedIds List of specific product IDs to include (null for all)
   * @param logicOperator "AND" or "OR" to combine filter conditions
   * @param filters List of filter conditions to apply
   * @param includeDeleted Whether to include deleted products
   * @param pageable Pagination parameters
   * @return Page of products matching the filter criteria
   */
  public Page<Product> findPaginatedEntitiesWithMultipleFilters(
      Long clientId,
      List<Long> selectedIds,
      String logicOperator,
      List<FilterCondition> filters,
      boolean includeDeleted,
      Pageable pageable) {

    // Check if there's a pickupLocationId filter (any operator, not just equals)
    boolean hasPickupLocationIdFilter = false;
    Long pickupLocationIdFilter = null;
    if (filters != null) {
      for (FilterCondition filter : filters) {
        if (PICKUP_LOCATION_ID.equals(filter.getColumn())) {
          hasPickupLocationIdFilter = true;
          if ("equals".equals(filter.getOperator())) {
            try {
              pickupLocationIdFilter = Long.parseLong(filter.getValue().toString());
            } catch (NumberFormatException e) {
              // Invalid pickupLocationId, will be handled by regular filter processing
            }
          }
          break;
        }
      }
    }

    // Base query with all necessary JOINs
    // Only include ProductPickupLocationMapping JOIN when filtering by pickupLocationId
    // This prevents errors when the table doesn't exist
    String baseQuery;
    if (hasPickupLocationIdFilter) {
      if (pickupLocationIdFilter != null) {
        // Use INNER JOIN when filtering by pickupLocationId - only get products with that mapping
        // and only fetch that specific mapping
        baseQuery =
            "SELECT DISTINCT p FROM Product p "
                + "LEFT JOIN FETCH p.category "
                + "LEFT JOIN FETCH p.createdByUser "
                + "INNER JOIN FETCH p.productPickupLocationMappings pplm "
                + "INNER JOIN FETCH pplm.pickupLocation pl "
                + "LEFT JOIN FETCH pl.address "
                + "WHERE p.clientId = "
                + CLIENT_ID_PARAM
                + " ";
      } else {
        // Use LEFT JOIN when filtering by pickupLocationId with other operators
        baseQuery =
            "SELECT DISTINCT p FROM Product p "
                + "LEFT JOIN FETCH p.category "
                + "LEFT JOIN FETCH p.createdByUser "
                + "LEFT JOIN FETCH p.productPickupLocationMappings pplm "
                + "LEFT JOIN FETCH pplm.pickupLocation pl "
                + "LEFT JOIN FETCH pl.address "
                + "WHERE p.clientId = "
                + CLIENT_ID_PARAM
                + " ";
      }
    } else {
      // No pickupLocationId filter - still fetch pickup locations for display
      baseQuery =
          "SELECT DISTINCT p FROM Product p "
              + "LEFT JOIN FETCH p.category "
              + "LEFT JOIN FETCH p.createdByUser "
              + "LEFT JOIN FETCH p.productPickupLocationMappings pplm "
              + "LEFT JOIN FETCH pplm.pickupLocation pl "
              + "LEFT JOIN FETCH pl.address "
              + "WHERE p.clientId = "
              + CLIENT_ID_PARAM
              + " ";
    }

    // Add selectedIds condition
    if (selectedIds != null && !selectedIds.isEmpty()) {
      baseQuery += "AND p.productId IN :selectedIds ";
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
    baseQuery += "ORDER BY p.productId DESC";

    // Count query (without FETCH joins, but with proper JOINs for filtering)
    // Only include ProductPickupLocationMapping JOIN when filtering by pickupLocationId
    String countQuery;
    if (hasPickupLocationIdFilter) {
      if (pickupLocationIdFilter != null) {
        countQuery =
            "SELECT COUNT(DISTINCT p) FROM Product p "
                + "INNER JOIN p.productPickupLocationMappings pplm "
                + "WHERE p.clientId = "
                + CLIENT_ID_PARAM
                + " ";
      } else {
        countQuery =
            "SELECT COUNT(DISTINCT p) FROM Product p "
                + "LEFT JOIN p.productPickupLocationMappings pplm "
                + "WHERE p.clientId = "
                + CLIENT_ID_PARAM
                + " ";
      }
    } else {
      // No pickupLocationId filter - don't join ProductPickupLocationMapping table
      countQuery =
          "SELECT COUNT(DISTINCT p) FROM Product p "
              + "WHERE p.clientId = "
              + CLIENT_ID_PARAM
              + " ";
    }

    if (selectedIds != null && !selectedIds.isEmpty()) {
      countQuery += "AND p.productId IN :selectedIds ";
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
    TypedQuery<Product> mainQuery = entityManager.createQuery(baseQuery, Product.class);
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

    List<Product> products = mainQuery.getResultList();

    return new PageImpl<>(products, pageable, totalCount);
  }
}
