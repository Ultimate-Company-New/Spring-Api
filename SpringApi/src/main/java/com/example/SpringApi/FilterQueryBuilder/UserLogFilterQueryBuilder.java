package com.example.SpringApi.FilterQueryBuilder;

import com.example.SpringApi.Models.DatabaseModels.UserLog;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.FilterCondition;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * UserLog-specific filter query builder that handles dynamic multi-filter queries.
 * 
 * This class combines:
 * 1. Column mapping logic (which columns map to which database fields)
 * 2. Query building logic (using BaseFilterQueryBuilder)
 * 3. Query execution logic (executing the built queries via EntityManager)
 */
@Component
public class UserLogFilterQueryBuilder extends BaseFilterQueryBuilder {

    private final EntityManager entityManager;

    // Constructor for Spring dependency injection
    public UserLogFilterQueryBuilder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // ==================== Column Mapping Methods ====================

    @Override
    protected String mapColumnToField(String column) {
        switch (column) {
            case "logId": return "ul.logId";
            case "userId": return "ul.userId";
            case "clientId": return "ul.clientId";
            case "action": return "ul.action";
            case "description": return "ul.description";
            case "ipAddress": return "ul.ipAddress";
            case "userAgent": return "ul.userAgent";
            case "sessionId": return "ul.sessionId";
            case "logLevel": return "ul.logLevel";
            case "createdAt": return "ul.createdAt";
            case "createdUser": return "ul.createdUser";
            case "updatedAt": return "ul.updatedAt";
            case "modifiedUser": return "ul.modifiedUser";
            case "notes": return "ul.notes";
            case "auditUserId": return "ul.auditUserId";
            case "change": return "ul.change";
            case "newValue": return "ul.newValue";
            case "oldValue": return "ul.oldValue";
            default: return "ul." + column;
        }
    }

    @Override
    protected List<String> getDateColumns() {
        return Arrays.asList("createdAt", "updatedAt");
    }

    @Override
    protected List<String> getBooleanColumns() {
        return Arrays.asList(); // No boolean columns in UserLog
    }

    @Override
    protected List<String> getNumberColumns() {
        return Arrays.asList("logId", "userId", "clientId", "auditUserId");
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
     * Finds paginated user logs with multiple filter conditions combined with AND/OR logic.
     * Builds the WHERE clause dynamically and executes the query.
     * 
     * @param userId The user ID to filter logs by
     * @param clientId The client ID to filter logs by
     * @param logicOperator "AND" or "OR" to combine filter conditions
     * @param filters List of filter conditions to apply
     * @param pageable Pagination parameters
     * @return Page of user logs matching the filter criteria
     */
    public Page<UserLog> findPaginatedEntitiesWithMultipleFilters(
            Long userId,
            Long clientId,
            String logicOperator,
            List<FilterCondition> filters,
            Pageable pageable) {

        // Base query - filter by userId and optionally by clientId (allows NULL clientId as well)
        String baseQuery = "SELECT ul FROM UserLog ul " +
                "WHERE ul.userId = :userId " +
                "AND (ul.clientId = :clientId OR ul.clientId IS NULL) ";

        // Build dynamic filter conditions using the query builder
        QueryResult filterResult = buildFilterConditions(filters, logicOperator);
        
        if (filterResult.hasConditions()) {
            baseQuery += "AND (" + filterResult.getWhereClause() + ") ";
        }

        // Add ordering
        baseQuery += "ORDER BY ul.logId DESC";

        // Count query
        String countQuery = "SELECT COUNT(ul) FROM UserLog ul " +
                "WHERE ul.userId = :userId " +
                "AND (ul.clientId = :clientId OR ul.clientId IS NULL) ";

        if (filterResult.hasConditions()) {
            countQuery += "AND (" + filterResult.getWhereClause() + ") ";
        }

        // Execute count query
        TypedQuery<Long> countTypedQuery = entityManager.createQuery(countQuery, Long.class);
        countTypedQuery.setParameter("userId", userId);
        countTypedQuery.setParameter("clientId", clientId);

        // Set filter parameters
        for (Map.Entry<String, Object> entry : filterResult.getParameters().entrySet()) {
            countTypedQuery.setParameter(entry.getKey(), entry.getValue());
        }

        Long totalCount = countTypedQuery.getSingleResult();

        // Execute main query with pagination
        TypedQuery<UserLog> mainQuery = entityManager.createQuery(baseQuery, UserLog.class);
        mainQuery.setParameter("userId", userId);
        mainQuery.setParameter("clientId", clientId);

        // Set filter parameters
        for (Map.Entry<String, Object> entry : filterResult.getParameters().entrySet()) {
            mainQuery.setParameter(entry.getKey(), entry.getValue());
        }

        // Apply pagination
        mainQuery.setFirstResult((int) pageable.getOffset());
        mainQuery.setMaxResults(pageable.getPageSize());

        List<UserLog> userLogs = mainQuery.getResultList();

        return new PageImpl<>(userLogs, pageable, totalCount);
    }
}

