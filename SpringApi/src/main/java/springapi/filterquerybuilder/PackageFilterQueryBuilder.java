package springapi.filterquerybuilder;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import springapi.models.databasemodels.Package;
import springapi.models.requestmodels.PaginationBaseRequestModel.FilterCondition;

/**
 * Package-specific filter query builder that handles dynamic multi-filter queries.
 *
 * <p>This class combines: 1. Column mapping logic (which columns map to which database fields) 2.
 * Query building logic (using BaseFilterQueryBuilder) 3. Query execution logic (executing the built
 * queries via EntityManager)
 */
@Component
public class PackageFilterQueryBuilder extends BaseFilterQueryBuilder {
  private static final String CLIENT_ID = "clientId";
  private static final String CLIENT_ID_PARAM = ":" + CLIENT_ID;
  private static final String PICKUP_LOCATION_ID = "pickupLocationId";

  private final EntityManager entityManager;

  // Constructor for Spring dependency injection
  public PackageFilterQueryBuilder(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  // ==================== Column Mapping Methods ====================

  @Override
  protected String mapColumnToField(String column) {
    switch (column) {
      case "packageId":
        return "pkg.packageId";
      case CLIENT_ID:
        return "pkg.clientId";
      case "packageName":
        return "pkg.packageName";
      case "length":
        return "pkg.length";
      case "breadth":
        return "pkg.breadth";
      case "height":
        return "pkg.height";
      case "maxWeight":
        return "pkg.maxWeight";
      case "standardCapacity":
        return "pkg.standardCapacity";
      case "pricePerUnit":
        return "pkg.pricePerUnit";
      case "packageType":
        return "pkg.packageType";
      case "isDeleted":
        return "pkg.isDeleted";
      case "notes":
        return "pkg.notes";
      case "createdUser":
        return "pkg.createdUser";
      case "modifiedUser":
        return "pkg.modifiedUser";
      case "createdAt":
        return "pkg.createdAt";
      case "updatedAt":
        return "pkg.updatedAt";
        // Special case for dimensions - concatenated field
      case "dimensions":
        return "CONCAT(pkg.length, ' x ', pkg.breadth, ' x ', pkg.height)";
        // Special case: filter through PackagePickupLocationMapping join
      case PICKUP_LOCATION_ID:
        return "pplm.pickupLocationId";
      default:
        throw new IllegalArgumentException("Invalid filter column: " + column);
    }
  }

  @Override
  protected List<String> getDateColumns() {
    return Arrays.asList("createdAt", "updatedAt");
  }

  @Override
  protected List<String> getBooleanColumns() {
    return Arrays.asList("isDeleted");
  }

  @Override
  protected List<String> getNumberColumns() {
    return Arrays.asList(
        "packageId",
        CLIENT_ID,
        "length",
        "breadth",
        "height",
        "maxWeight",
        "standardCapacity",
        "pricePerUnit",
        PICKUP_LOCATION_ID);
  }

  /**
   * Gets the column type for validation purposes.
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
   * Finds paginated packages with multiple filter conditions combined with AND/OR logic. Builds
   * the. WHERE clause dynamically and executes the query.
   *
   * <p>When filtering by pickupLocationId, only packages with that pickup location are returned.
   *
   * @param clientId The client ID to filter packages by
   * @param selectedIds List of specific package IDs to include (null for all)
   * @param logicOperator "AND" or "OR" to combine filter conditions
   * @param filters List of filter conditions to apply
   * @param includeDeleted Whether to include deleted packages
   * @param pageable Pagination parameters
   * @return Page of packages matching the filter criteria
   */
  public Page<Package> findPaginatedEntitiesWithMultipleFilters(
      Long clientId,
      List<Long> selectedIds,
      String logicOperator,
      List<FilterCondition> filters,
      boolean includeDeleted,
      Pageable pageable) {

    // Check if there's a pickupLocationId filter
    Long pickupLocationIdFilter = null;
    if (filters != null) {
      for (FilterCondition filter : filters) {
        if (PICKUP_LOCATION_ID.equals(filter.getColumn())
            && "equals".equals(filter.getOperator())) {
          try {
            pickupLocationIdFilter = Long.parseLong(filter.getValue().toString());
          } catch (NumberFormatException e) {
            // Invalid pickupLocationId, will be handled by regular filter processing
          }
          break;
        }
      }
    }

    // Base query - use INNER JOIN when filtering by pickupLocationId
    String baseQuery;
    if (pickupLocationIdFilter != null) {
      baseQuery =
          "SELECT DISTINCT pkg FROM Package pkg "
              + "INNER JOIN PackagePickupLocationMapping pplm ON pkg.packageId = pplm.packageId "
              + "WHERE pkg.clientId = "
              + CLIENT_ID_PARAM
              + " ";
    } else {
      baseQuery = "SELECT pkg FROM Package pkg " + "WHERE pkg.clientId = " + CLIENT_ID_PARAM + " ";
    }

    // Add selectedIds condition
    if (selectedIds != null && !selectedIds.isEmpty()) {
      baseQuery += "AND pkg.packageId IN :selectedIds ";
    }

    // Add includeDeleted condition
    if (!includeDeleted) {
      baseQuery += "AND pkg.isDeleted = false ";
    }

    // Build dynamic filter conditions using the query builder
    QueryResult filterResult = buildFilterConditions(filters, logicOperator);

    if (filterResult.hasConditions()) {
      baseQuery += "AND (" + filterResult.getWhereClause() + ") ";
    }

    // Add ordering
    baseQuery += "ORDER BY pkg.packageId DESC";

    // Count query - include JOIN if filtering by pickupLocationId
    String countQuery;
    if (pickupLocationIdFilter != null) {
      countQuery =
          "SELECT COUNT(DISTINCT pkg) FROM Package pkg "
              + "INNER JOIN PackagePickupLocationMapping pplm ON pkg.packageId = pplm.packageId "
              + "WHERE pkg.clientId = "
              + CLIENT_ID_PARAM
              + " ";
    } else {
      countQuery =
          "SELECT COUNT(pkg) FROM Package pkg " + "WHERE pkg.clientId = " + CLIENT_ID_PARAM + " ";
    }

    if (selectedIds != null && !selectedIds.isEmpty()) {
      countQuery += "AND pkg.packageId IN :selectedIds ";
    }

    if (!includeDeleted) {
      countQuery += "AND pkg.isDeleted = false ";
    }

    if (filterResult.hasConditions()) {
      countQuery += "AND (" + filterResult.getWhereClause() + ") ";
    }

    // Execute count query
    TypedQuery<Long> countTypedQuery = // NOSONAR
        entityManager.createQuery(countQuery, Long.class);
    countTypedQuery.setParameter(CLIENT_ID, clientId);

    if (selectedIds != null && !selectedIds.isEmpty()) {
      countTypedQuery.setParameter("selectedIds", selectedIds);
    }

    // Set filter parameters
    for (Map.Entry<String, Object> entry : filterResult.getParameters().entrySet()) {
      countTypedQuery.setParameter(entry.getKey(), entry.getValue());
    }

    final Long totalCount = countTypedQuery.getSingleResult();

    // Execute main query with pagination
    TypedQuery<Package> mainQuery = // NOSONAR
        entityManager.createQuery(baseQuery, Package.class);
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

    List<Package> packages = mainQuery.getResultList();

    return new PageImpl<>(packages, pageable, totalCount);
  }

  /**
   * Gets the extracted pickupLocationId filter value if present. This is used by the service to.
   * filter the pickup location mappings in the response.
   *
   * @param filters List of filter conditions
   * @return The pickupLocationId filter value, or null if not present
   */
  public Long extractPickupLocationIdFilter(List<FilterCondition> filters) {
    if (filters != null) {
      for (FilterCondition filter : filters) {
        if (PICKUP_LOCATION_ID.equals(filter.getColumn())
            && "equals".equals(filter.getOperator())) {
          try {
            return Long.parseLong(filter.getValue().toString());
          } catch (NumberFormatException e) {
            return null;
          }
        }
      }
    }
    return null;
  }
}
