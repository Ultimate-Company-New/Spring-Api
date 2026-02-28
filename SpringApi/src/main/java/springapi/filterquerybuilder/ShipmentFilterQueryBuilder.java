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
import springapi.models.databasemodels.Shipment;
import springapi.models.requestmodels.PaginationBaseRequestModel.FilterCondition;

/**
 * Shipment-specific filter query builder that handles dynamic multi-filter queries.
 *
 * <p>This class combines: 1. Column mapping logic (which columns map to which database fields) 2.
 * Query building logic (using BaseFilterQueryBuilder) 3. Query execution logic (executing the built
 * queries via EntityManager)
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Component
public class ShipmentFilterQueryBuilder extends BaseFilterQueryBuilder {
  private static final String CLIENT_ID = "clientId";
  private static final String CLIENT_ID_PARAM = ":" + CLIENT_ID;

  private final EntityManager entityManager;

  // Constructor for Spring dependency injection
  public ShipmentFilterQueryBuilder(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  // ==================== Column Mapping Methods ====================

  @Override
  protected String mapColumnToField(String column) {
    switch (column) {
      case "shipmentId":
        return "s.shipmentId";
      case "orderSummaryId":
        return "s.orderSummaryId";
      case "pickupLocationId":
        return "s.pickupLocationId";
      case "totalWeightKgs":
        return "s.totalWeightKgs";
      case "totalQuantity":
        return "s.totalQuantity";
      case "expectedDeliveryDate":
        return "s.expectedDeliveryDate";
      case "packagingCost":
        return "s.packagingCost";
      case "shippingCost":
        return "s.shippingCost";
      case "totalCost":
        return "s.totalCost";
      case "selectedCourierCompanyId":
        return "s.selectedCourierCompanyId";
      case "selectedCourierName":
        return "s.selectedCourierName";
      case "selectedCourierRate":
        return "s.selectedCourierRate";
      case "selectedCourierMinWeight":
        return "s.selectedCourierMinWeight";
      case "shipRocketOrderId":
        return "s.shipRocketOrderId";
      case "shipRocketShipmentId":
        return "s.shipRocketShipmentId";
      case "shipRocketAwbCode":
        return "s.shipRocketAwbCode";
      case "shipRocketTrackingId":
        return "s.shipRocketTrackingId";
      case "shipRocketStatus":
        return "s.shipRocketStatus";
      case CLIENT_ID:
        return "s.clientId";
      case "createdUser":
        return "s.createdUser";
      case "modifiedUser":
        return "s.modifiedUser";
      case "createdAt":
        return "s.createdAt";
      case "updatedAt":
        return "s.updatedAt";
      default:
        throw new IllegalArgumentException("Invalid filter column: " + column);
    }
  }

  @Override
  protected List<String> getDateColumns() {
    return Arrays.asList("expectedDeliveryDate", "createdAt", "updatedAt");
  }

  @Override
  protected List<String> getBooleanColumns() {
    return Arrays.asList(); // No boolean columns in Shipment
  }

  @Override
  protected List<String> getNumberColumns() {
    return Arrays.asList(
        "shipmentId",
        "orderSummaryId",
        "pickupLocationId",
        "totalQuantity",
        "selectedCourierCompanyId",
        "shipRocketShipmentId");
  }

  /** Returns list of decimal number columns (BigDecimal). */
  protected List<String> getDecimalColumns() {
    return Arrays.asList(
        "totalWeightKgs",
        "packagingCost",
        "shippingCost",
        "totalCost",
        "selectedCourierRate",
        "selectedCourierMinWeight");
  }

  /**
   * Gets the column type for validation purposes.
   *
   * @param column The column name
   * @return "string", "number", "decimal", "date", or "boolean"
   */
  public String getColumnType(String column) {
    if (getDateColumns().contains(column)) {
      return "date";
    } else if (getBooleanColumns().contains(column)) {
      return "boolean";
    } else if (getNumberColumns().contains(column)) {
      return "number";
    } else if (getDecimalColumns().contains(column)) {
      return "number"; // Return as number for validation purposes
    } else {
      return "string";
    }
  }

  // ==================== Query Execution Method ====================

  /**
   * Finds paginated shipments with multiple filter conditions combined with AND/OR logic. Builds.
   * the WHERE clause dynamically and executes the query.
   *
   * @param clientId The client ID to filter shipments by
   * @param selectedIds List of specific shipment IDs to include (null for all)
   * @param logicOperator "AND" or "OR" to combine filter conditions
   * @param filters List of filter conditions to apply
   * @param pageable Pagination parameters
   * @return Page of shipments matching the filter criteria
   */
  @SuppressWarnings("java:S2077")
  public Page<Shipment> findPaginatedEntitiesWithMultipleFilters(
      Long clientId,
      List<Long> selectedIds,
      String logicOperator,
      List<FilterCondition> filters,
      Pageable pageable) {

    // Base query - Only return shipments with ShipRocket order ID assigned
    String baseQuery =
        "SELECT s FROM Shipment s "
            + "WHERE s.clientId = "
            + CLIENT_ID_PARAM
            + " "
            + "AND s.shipRocketOrderId IS NOT NULL "
            + "AND s.shipRocketOrderId != '' ";

    // Add selectedIds condition
    if (selectedIds != null && !selectedIds.isEmpty()) {
      baseQuery += "AND s.shipmentId IN :selectedIds ";
    }

    // Build dynamic filter conditions using the query builder
    QueryResult filterResult = buildFilterConditions(filters, logicOperator);

    if (filterResult.hasConditions()) {
      baseQuery += "AND (" + filterResult.getWhereClause() + ") ";
    }

    // Add ordering - default sort by createdAt descending
    baseQuery += "ORDER BY s.createdAt DESC";

    // Count query - Only count shipments with ShipRocket order ID assigned
    String countQuery =
        "SELECT COUNT(s) FROM Shipment s "
            + "WHERE s.clientId = "
            + CLIENT_ID_PARAM
            + " "
            + "AND s.shipRocketOrderId IS NOT NULL "
            + "AND s.shipRocketOrderId != '' ";

    if (selectedIds != null && !selectedIds.isEmpty()) {
      countQuery += "AND s.shipmentId IN :selectedIds ";
    }

    if (filterResult.hasConditions()) {
      countQuery += "AND (" + filterResult.getWhereClause() + ") ";
    }

    // Query fragments are mapped from whitelisted columns/operators; all values are parameterized.
    TypedQuery<Long> countTypedQuery = entityManager.createQuery(countQuery, Long.class); // NOSONAR
    countTypedQuery.setParameter(CLIENT_ID, clientId);

    if (selectedIds != null && !selectedIds.isEmpty()) {
      countTypedQuery.setParameter("selectedIds", selectedIds);
    }

    // Set filter parameters
    for (Map.Entry<String, Object> entry : filterResult.getParameters().entrySet()) {
      countTypedQuery.setParameter(entry.getKey(), entry.getValue());
    }

    final Long totalCount = countTypedQuery.getSingleResult();

    TypedQuery<Shipment> mainQuery =
        entityManager.createQuery(baseQuery, Shipment.class); // NOSONAR
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

    List<Shipment> shipments = mainQuery.getResultList();

    return new PageImpl<>(shipments, pageable, totalCount);
  }
}
