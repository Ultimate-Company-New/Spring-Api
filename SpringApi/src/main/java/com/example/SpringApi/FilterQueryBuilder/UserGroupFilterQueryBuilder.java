package com.example.SpringApi.FilterQueryBuilder;

import com.example.SpringApi.Models.DatabaseModels.UserGroup;
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
 * UserGroup-specific filter query builder that handles dynamic multi-filter queries.
 *
 * <p>This class combines: 1. Column mapping logic (which columns map to which database fields) 2.
 * Query building logic (using BaseFilterQueryBuilder) 3. Query execution logic (executing the built
 * queries via EntityManager)
 */
@Component
public class UserGroupFilterQueryBuilder extends BaseFilterQueryBuilder {
  private static final String CLIENT_ID = "clientId";
  private static final String CLIENT_ID_PARAM = ":" + CLIENT_ID;

  private final EntityManager entityManager;

  // Constructor for Spring dependency injection
  public UserGroupFilterQueryBuilder(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  // ==================== Column Mapping Methods ====================

  @Override
  protected String mapColumnToField(String column) {
    switch (column) {
      case "groupId":
        return "ug.groupId";
      case CLIENT_ID:
        return "ug.clientId";
      case "groupName":
        return "ug.groupName";
      case "description":
        return "ug.description";
      case "isActive":
        return "ug.isActive";
      case "isDeleted":
        return "ug.isDeleted";
      case "notes":
        return "ug.notes";
      case "createdUser":
        return "ug.createdUser";
      case "modifiedUser":
        return "ug.modifiedUser";
      case "createdAt":
        return "ug.createdAt";
      case "updatedAt":
        return "ug.updatedAt";
      case "userCount", "memberCount", "members":
        // Use subquery to count user mappings
        return "(SELECT COUNT(ugm2) FROM UserGroupUserMap ugm2 WHERE ugm2.groupId = ug.groupId)";
      default:
        return "ug." + column;
    }
  }

  @Override
  protected List<String> getDateColumns() {
    return Arrays.asList("createdAt", "updatedAt");
  }

  @Override
  protected List<String> getBooleanColumns() {
    return Arrays.asList("isActive", "isDeleted");
  }

  @Override
  protected List<String> getNumberColumns() {
    return Arrays.asList("groupId", CLIENT_ID, "userCount", "memberCount", "members");
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
   * Finds paginated user groups with multiple filter conditions combined with AND/OR logic. Builds
   * the WHERE clause dynamically and executes the query.
   *
   * @param clientId The client ID to filter user groups by
   * @param selectedIds List of specific group IDs to include (null for all)
   * @param logicOperator "AND" or "OR" to combine filter conditions
   * @param filters List of filter conditions to apply
   * @param includeDeleted Whether to include deleted user groups
   * @param pageable Pagination parameters
   * @return Page of user groups matching the filter criteria
   */
  public Page<UserGroup> findPaginatedEntitiesWithMultipleFilters(
      Long clientId,
      List<Long> selectedIds,
      String logicOperator,
      List<FilterCondition> filters,
      boolean includeDeleted,
      Pageable pageable) {

    // Base query with all necessary JOINs
    String baseQuery =
        "SELECT DISTINCT ug FROM UserGroup ug "
            + "LEFT JOIN FETCH ug.userMappings ugm "
            + "LEFT JOIN FETCH ugm.user u "
            + "WHERE ug.clientId = "
            + CLIENT_ID_PARAM
            + " ";

    // Add selectedIds condition
    if (selectedIds != null && !selectedIds.isEmpty()) {
      baseQuery += "AND ug.groupId IN :selectedIds ";
    }

    // Add includeDeleted condition
    if (!includeDeleted) {
      baseQuery += "AND ug.isDeleted = false ";
    }

    // Build dynamic filter conditions using the query builder
    QueryResult filterResult = buildFilterConditions(filters, logicOperator);

    if (filterResult.hasConditions()) {
      baseQuery += "AND (" + filterResult.getWhereClause() + ") ";
    }

    // Add ordering
    baseQuery += "ORDER BY ug.groupId DESC";

    // Count query (without FETCH joins)
    String countQuery =
        "SELECT COUNT(DISTINCT ug) FROM UserGroup ug "
            + "WHERE ug.clientId = "
            + CLIENT_ID_PARAM
            + " ";

    if (selectedIds != null && !selectedIds.isEmpty()) {
      countQuery += "AND ug.groupId IN :selectedIds ";
    }

    if (!includeDeleted) {
      countQuery += "AND ug.isDeleted = false ";
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
    TypedQuery<UserGroup> mainQuery = entityManager.createQuery(baseQuery, UserGroup.class);
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

    List<UserGroup> userGroups = mainQuery.getResultList();

    return new PageImpl<>(userGroups, pageable, totalCount);
  }
}
