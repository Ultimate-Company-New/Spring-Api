package com.example.SpringApi.FilterQueryBuilder;

import com.example.SpringApi.Models.DatabaseModels.Lead;
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
 * Lead-specific filter query builder that handles dynamic multi-filter queries.
 * 
 * This class combines:
 * 1. Column mapping logic (which columns map to which database fields)
 * 2. Query building logic (using BaseFilterQueryBuilder)
 * 3. Query execution logic (executing the built queries via EntityManager)
 */
@Component
public class LeadFilterQueryBuilder extends BaseFilterQueryBuilder {

    private final EntityManager entityManager;

    // Constructor for Spring dependency injection
    public LeadFilterQueryBuilder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // ==================== Column Mapping Methods ====================

    @Override
    protected String mapColumnToField(String column) {
        switch (column) {
            case "leadId": return "l.leadId";
            case "firstName": return "l.firstName";
            case "lastName": return "l.lastName";
            case "email": return "l.email";
            case "phone": return "l.phone";
            case "company": return "l.company";
            case "companySize": return "l.companySize";
            case "title": return "l.title";
            case "leadStatus": return "l.leadStatus";
            case "annualRevenue": return "l.annualRevenue";
            case "fax": return "l.fax";
            case "website": return "l.website";
            case "isDeleted": return "l.isDeleted";
            case "clientId": return "l.clientId";
            case "addressId": return "l.addressId";
            case "createdById": return "l.createdById";
            case "assignedAgentId": return "l.assignedAgentId";
            case "createdUser": return "l.createdUser";
            case "modifiedUser": return "l.modifiedUser";
            case "createdAt": return "l.createdAt";
            case "updatedAt": return "l.updatedAt";
            case "notes": return "l.notes";
            case "address": 
                return "CONCAT(COALESCE(a.streetAddress, ''), ' ', COALESCE(a.streetAddress2, ''), ' ', " +
                       "COALESCE(a.streetAddress3, ''), ' ', COALESCE(a.city, ''), ' ', " +
                       "COALESCE(a.state, ''), ' ', COALESCE(a.postalCode, ''), ' ', COALESCE(a.country, ''))";
            default: return "l." + column;
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
        return Arrays.asList("leadId", "companySize", "clientId", "addressId", "createdById", "assignedAgentId");
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
     * Finds paginated leads with multiple filter conditions combined with AND/OR logic.
     * Builds the WHERE clause dynamically and executes the query.
     * 
     * @param clientId The client ID to filter leads by
     * @param logicOperator "AND" or "OR" to combine filter conditions
     * @param filters List of filter conditions to apply
     * @param includeDeleted Whether to include deleted leads
     * @param pageable Pagination parameters
     * @return Page of leads matching the filter criteria
     */
    public Page<Lead> findPaginatedEntitiesWithMultipleFilters(
            Long clientId,
            String logicOperator,
            List<FilterCondition> filters,
            boolean includeDeleted,
            Pageable pageable) {

        // Base query with all necessary JOINs
        String baseQuery = "SELECT DISTINCT l FROM Lead l " +
                "LEFT JOIN FETCH l.address a " +
                "LEFT JOIN FETCH l.createdByUser " +
                "LEFT JOIN FETCH l.assignedAgent " +
                "WHERE l.clientId = :clientId ";

        // Add includeDeleted condition
        if (!includeDeleted) {
            baseQuery += "AND l.isDeleted = false ";
        }

        // Build dynamic filter conditions using the query builder
        QueryResult filterResult = buildFilterConditions(filters, logicOperator);
        
        if (filterResult.hasConditions()) {
            baseQuery += "AND (" + filterResult.getWhereClause() + ") ";
        }

        // Add ordering
        baseQuery += "ORDER BY l.leadId DESC";

        // Count query (without FETCH joins)
        String countQuery = "SELECT COUNT(DISTINCT l) FROM Lead l " +
                "LEFT JOIN l.address a " +
                "WHERE l.clientId = :clientId ";

        if (!includeDeleted) {
            countQuery += "AND l.isDeleted = false ";
        }

        if (filterResult.hasConditions()) {
            countQuery += "AND (" + filterResult.getWhereClause() + ") ";
        }

        // Execute count query
        TypedQuery<Long> countTypedQuery = entityManager.createQuery(countQuery, Long.class);
        countTypedQuery.setParameter("clientId", clientId);

        // Set filter parameters
        for (Map.Entry<String, Object> entry : filterResult.getParameters().entrySet()) {
            countTypedQuery.setParameter(entry.getKey(), entry.getValue());
        }

        Long totalCount = countTypedQuery.getSingleResult();

        // Execute main query with pagination
        TypedQuery<Lead> mainQuery = entityManager.createQuery(baseQuery, Lead.class);
        mainQuery.setParameter("clientId", clientId);

        // Set filter parameters
        for (Map.Entry<String, Object> entry : filterResult.getParameters().entrySet()) {
            mainQuery.setParameter(entry.getKey(), entry.getValue());
        }

        // Apply pagination
        mainQuery.setFirstResult((int) pageable.getOffset());
        mainQuery.setMaxResults(pageable.getPageSize());

        List<Lead> leads = mainQuery.getResultList();

        return new PageImpl<>(leads, pageable, totalCount);
    }
}

