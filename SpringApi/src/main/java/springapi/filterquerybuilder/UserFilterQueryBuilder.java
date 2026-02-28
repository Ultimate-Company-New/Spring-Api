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
import springapi.models.databasemodels.User;
import springapi.models.requestmodels.PaginationBaseRequestModel.FilterCondition;

/**
 * User-specific filter query builder that handles dynamic multi-filter queries.
 *
 * <p>This class combines: 1. Column mapping logic (which columns map to which database fields) 2.
 * Query building logic (using BaseFilterQueryBuilder) 3. Query execution logic (executing the built
 * queries via EntityManager)
 *
 * <p>This can be used as a template for other entities: - LeadFilterQueryBuilder -
 * ProductFilterQueryBuilder - GroupFilterQueryBuilder etc.
 */
@Component
public class UserFilterQueryBuilder extends BaseFilterQueryBuilder {

  private final EntityManager entityManager;

  // Constructor for Spring dependency injection
  public UserFilterQueryBuilder(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  // ==================== Column Mapping Methods ====================

  @Override
  protected String mapColumnToField(String column) {
    switch (column) {
      case "userId":
        return "u.userId";
      case "firstName":
        return "u.firstName";
      case "lastName":
        return "u.lastName";
      case "loginName":
        return "u.loginName";
      case "role":
        return "u.role";
      case "dob":
        return "u.dob";
      case "phone":
        return "u.phone";
      case "datePasswordChanges":
        return "u.datePasswordChanges";
      case "loginAttempts":
        return "u.loginAttempts";
      case "isDeleted":
        return "u.isDeleted";
      case "locked":
        return "u.locked";
      case "emailConfirmed":
        return "u.emailConfirmed";
      case "token":
        return "u.token";
      case "isGuest":
        return "u.isGuest";
      case "apiKey":
        return "u.apiKey";
      case "email":
        return "u.email";
      case "addressId":
        return "u.addressId";
      case "profilePicture":
        return "u.profilePicture";
      case "lastLoginAt":
        return "u.lastLoginAt";
      case "createdAt":
        return "u.createdAt";
      case "createdUser":
        return "u.createdUser";
      case "updatedAt":
        return "u.updatedAt";
      case "modifiedUser":
        return "u.modifiedUser";
      case "notes":
        return "u.notes";
      case "address":
        return "CONCAT(COALESCE(a.streetAddress, ''), ' ', COALESCE(a.streetAddress2, ''), ' ', "
            + "COALESCE(a.streetAddress3, ''), ' ', COALESCE(a.city, ''), ' ', "
            + "COALESCE(a.state, ''), ' ', COALESCE(a.postalCode, ''), ' ', "
            + "COALESCE(a.country, ''))";
      default:
        return "u." + column;
    }
  }

  @Override
  protected List<String> getDateColumns() {
    return Arrays.asList("dob", "datePasswordChanges", "lastLoginAt", "createdAt", "updatedAt");
  }

  @Override
  protected List<String> getBooleanColumns() {
    return Arrays.asList("isDeleted", "locked", "emailConfirmed", "isGuest");
  }

  @Override
  protected List<String> getNumberColumns() {
    return Arrays.asList("userId", "loginAttempts", "addressId");
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
   * Finds paginated users with multiple filter conditions combined with AND/OR logic. Builds the.
   * WHERE clause dynamically and executes the query.
   *
   * @param clientId The client ID to filter users by
   * @param selectedIds List of specific user IDs to include (null for all)
   * @param logicOperator "AND" or "OR" to combine filter conditions
   * @param filters List of filter conditions to apply
   * @param includeDeleted Whether to include deleted users
   * @param pageable Pagination parameters
   * @return Page of users matching the filter criteria
   */
  public Page<User> findPaginatedEntitiesWithMultipleFilters(
      Long clientId,
      List<Long> selectedIds,
      String logicOperator,
      List<FilterCondition> filters,
      boolean includeDeleted,
      Pageable pageable) {

    // Base query with all necessary JOINs
    String baseQuery =
        "SELECT DISTINCT u FROM User u "
            + "JOIN UserClientMapping ucm ON u.userId = ucm.userId "
            + "LEFT JOIN FETCH u.addresses "
            + "LEFT JOIN FETCH u.userClientPermissionMappings ucpm "
            + "LEFT JOIN FETCH ucpm.permission p "
            + "LEFT JOIN FETCH u.userGroupMappings ugm "
            + "LEFT JOIN FETCH ugm.userGroup ug "
            + "LEFT JOIN Address a ON u.addressId = a.addressId "
            + "WHERE ucm.clientId = :clientId "
            + "AND (ucpm IS NULL OR ucpm.clientId = :clientId) "
            + "AND (ugm IS NULL OR ug.clientId = :clientId) ";

    // Add selectedIds condition
    if (selectedIds != null && !selectedIds.isEmpty()) {
      baseQuery += "AND u.userId IN :selectedIds ";
    }

    // Add includeDeleted condition
    if (!includeDeleted) {
      baseQuery += "AND u.isDeleted = false ";
    }

    // Build dynamic filter conditions using the query builder
    QueryResult filterResult = buildFilterConditions(filters, logicOperator);

    if (filterResult.hasConditions()) {
      baseQuery += "AND (" + filterResult.getWhereClause() + ") ";
    }

    // Count query (without FETCH joins)
    String countQuery =
        "SELECT COUNT(DISTINCT u) FROM User u "
            + "JOIN UserClientMapping ucm ON u.userId = ucm.userId "
            + "LEFT JOIN Address a ON u.addressId = a.addressId "
            + "WHERE ucm.clientId = :clientId ";

    if (selectedIds != null && !selectedIds.isEmpty()) {
      countQuery += "AND u.userId IN :selectedIds ";
    }

    if (!includeDeleted) {
      countQuery += "AND u.isDeleted = false ";
    }

    if (filterResult.hasConditions()) {
      countQuery += "AND (" + filterResult.getWhereClause() + ") ";
    }

    // Execute count query
    TypedQuery<Long> countTypedQuery = entityManager.createQuery(countQuery, Long.class);
    countTypedQuery.setParameter("clientId", clientId);

    if (selectedIds != null && !selectedIds.isEmpty()) {
      countTypedQuery.setParameter("selectedIds", selectedIds);
    }

    // Set filter parameters
    for (Map.Entry<String, Object> entry : filterResult.getParameters().entrySet()) {
      countTypedQuery.setParameter(entry.getKey(), entry.getValue());
    }

    final Long totalCount = countTypedQuery.getSingleResult();

    // Execute main query with pagination
    TypedQuery<User> mainQuery = entityManager.createQuery(baseQuery, User.class);
    mainQuery.setParameter("clientId", clientId);

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

    List<User> users = mainQuery.getResultList();

    return new PageImpl<>(users, pageable, totalCount);
  }
}
