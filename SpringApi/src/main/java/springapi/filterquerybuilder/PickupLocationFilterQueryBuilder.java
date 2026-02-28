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
import springapi.models.databasemodels.PickupLocation;
import springapi.models.requestmodels.PaginationBaseRequestModel.FilterCondition;

/**
 * PickupLocation-specific filter query builder that handles dynamic multi-filter queries.
 *
 * <p>This class combines: 1. Column mapping logic (which columns map to which database fields) 2.
 * Query building logic (using BaseFilterQueryBuilder) 3. Query execution logic (executing the built
 * queries via EntityManager)
 */
@Component
public class PickupLocationFilterQueryBuilder extends BaseFilterQueryBuilder {
  private static final String CLIENT_ID = "clientId";
  private static final String CLIENT_ID_PARAM = ":" + CLIENT_ID;

  private final EntityManager entityManager;

  // Constructor for Spring dependency injection
  public PickupLocationFilterQueryBuilder(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  // ==================== Column Mapping Methods ====================

  @Override
  protected String mapColumnToField(String column) {
    switch (column) {
      case "pickupLocationId":
        return "pl.pickupLocationId";
      case CLIENT_ID:
        return "pl.clientId";
      case "addressNickName":
        return "pl.addressNickName";
      case "locationName":
        return "pl.addressNickName"; // Alias for addressNickName
      case "pickupLocationAddressId":
        return "pl.pickupLocationAddressId";
      case "shipRocketPickupLocationId":
        return "pl.shipRocketPickupLocationId";
      case "isDeleted":
        return "pl.isDeleted";
      case "notes":
        return "pl.notes";
      case "createdBy":
        return "pl.createdBy";
      case "modifiedBy":
        return "pl.modifiedBy";
      case "createdAt":
        return "pl.createdAt";
      case "updatedAt":
        return "pl.updatedAt";
        // Special case for address - concatenated field from joined table
      case "address":
        return "CONCAT(a.streetAddress, ' ', a.streetAddress2, ' ', a.city, ' ', "
            + "a.state, ' ', a.postalCode)";
      default:
        return "pl." + column;
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
        "pickupLocationId", CLIENT_ID, "pickupLocationAddressId", "shipRocketPickupLocationId");
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
   * Finds paginated pickup locations with multiple filter conditions combined with AND/OR logic.
   * Builds the WHERE clause dynamically and executes the query.
   *
   * @param clientId The client ID to filter pickup locations by
   * @param selectedIds List of specific pickup location IDs to include (null for all)
   * @param logicOperator "AND" or "OR" to combine filter conditions
   * @param filters List of filter conditions to apply
   * @param includeDeleted Whether to include deleted pickup locations
   * @param pageable Pagination parameters
   * @return Page of pickup locations matching the filter criteria
   */
  public Page<PickupLocation> findPaginatedEntitiesWithMultipleFilters(
      Long clientId,
      List<Long> selectedIds,
      String logicOperator,
      List<FilterCondition> filters,
      boolean includeDeleted,
      Pageable pageable) {

    // Base query with INNER JOIN FETCH for address
    // Uses INNER JOIN to exclude pickup locations with missing address (bad data)
    String baseQuery =
        "SELECT pl FROM PickupLocation pl "
            + "JOIN FETCH pl.address a "
            + "WHERE pl.clientId = "
            + CLIENT_ID_PARAM
            + " ";

    // Add selectedIds condition
    if (selectedIds != null && !selectedIds.isEmpty()) {
      baseQuery += "AND pl.pickupLocationId IN :selectedIds ";
    }

    // Add includeDeleted condition
    if (!includeDeleted) {
      baseQuery += "AND pl.isDeleted = false ";
    }

    // Build dynamic filter conditions using the query builder
    QueryResult filterResult = buildFilterConditions(filters, logicOperator);

    if (filterResult.hasConditions()) {
      baseQuery += "AND (" + filterResult.getWhereClause() + ") ";
    }

    // Add ordering
    baseQuery += "ORDER BY pl.pickupLocationId DESC";

    // Count query (without FETCH join)
    // Uses INNER JOIN to match main query - excludes pickup locations with missing address (bad
    // data)
    String countQuery =
        "SELECT COUNT(pl) FROM PickupLocation pl "
            + "JOIN pl.address a "
            + "WHERE pl.clientId = "
            + CLIENT_ID_PARAM
            + " ";

    if (selectedIds != null && !selectedIds.isEmpty()) {
      countQuery += "AND pl.pickupLocationId IN :selectedIds ";
    }

    if (!includeDeleted) {
      countQuery += "AND pl.isDeleted = false ";
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

    final Long totalCount = countTypedQuery.getSingleResult();

    // Execute main query with pagination
    TypedQuery<PickupLocation> mainQuery =
        entityManager.createQuery(baseQuery, PickupLocation.class);
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

    List<PickupLocation> pickupLocations = mainQuery.getResultList();

    return new PageImpl<>(pickupLocations, pageable, totalCount);
  }
}
