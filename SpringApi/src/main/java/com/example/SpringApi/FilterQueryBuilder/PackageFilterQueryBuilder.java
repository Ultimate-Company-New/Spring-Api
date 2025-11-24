package com.example.SpringApi.FilterQueryBuilder;

import com.example.SpringApi.Models.DatabaseModels.Package;
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
 * Package-specific filter query builder that handles dynamic multi-filter queries.
 * 
 * This class combines:
 * 1. Column mapping logic (which columns map to which database fields)
 * 2. Query building logic (using BaseFilterQueryBuilder)
 * 3. Query execution logic (executing the built queries via EntityManager)
 */
@Component
public class PackageFilterQueryBuilder extends BaseFilterQueryBuilder {

    private final EntityManager entityManager;

    // Constructor for Spring dependency injection
    public PackageFilterQueryBuilder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // ==================== Column Mapping Methods ====================

    @Override
    protected String mapColumnToField(String column) {
        switch (column) {
            case "packageId": return "pkg.packageId";
            case "clientId": return "pkg.clientId";
            case "packageName": return "pkg.packageName";
            case "length": return "pkg.length";
            case "breadth": return "pkg.breadth";
            case "height": return "pkg.height";
            case "maxWeight": return "pkg.maxWeight";
            case "standardCapacity": return "pkg.standardCapacity";
            case "pricePerUnit": return "pkg.pricePerUnit";
            case "packageType": return "pkg.packageType";
            case "isDeleted": return "pkg.isDeleted";
            case "notes": return "pkg.notes";
            case "createdUser": return "pkg.createdUser";
            case "modifiedUser": return "pkg.modifiedUser";
            case "createdAt": return "pkg.createdAt";
            case "updatedAt": return "pkg.updatedAt";
            // Special case for dimensions - concatenated field
            case "dimensions": return "CONCAT(pkg.length, ' x ', pkg.breadth, ' x ', pkg.height)";
            default: return "pkg." + column;
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
        return Arrays.asList("packageId", "clientId", "length", "breadth", "height", 
                "maxWeight", "standardCapacity", "pricePerUnit");
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
     * Finds paginated packages with multiple filter conditions combined with AND/OR logic.
     * Builds the WHERE clause dynamically and executes the query.
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

        // Base query
        String baseQuery = "SELECT pkg FROM Package pkg " +
                "WHERE pkg.clientId = :clientId ";

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

        // Count query
        String countQuery = "SELECT COUNT(pkg) FROM Package pkg " +
                "WHERE pkg.clientId = :clientId ";

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
        TypedQuery<Long> countTypedQuery = entityManager.createQuery(countQuery, Long.class);
        countTypedQuery.setParameter("clientId", clientId);
        
        if (selectedIds != null && !selectedIds.isEmpty()) {
            countTypedQuery.setParameter("selectedIds", selectedIds);
        }

        // Set filter parameters
        for (Map.Entry<String, Object> entry : filterResult.getParameters().entrySet()) {
            countTypedQuery.setParameter(entry.getKey(), entry.getValue());
        }

        Long totalCount = countTypedQuery.getSingleResult();

        // Execute main query with pagination
        TypedQuery<Package> mainQuery = entityManager.createQuery(baseQuery, Package.class);
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

        List<Package> packages = mainQuery.getResultList();

        return new PageImpl<>(packages, pageable, totalCount);
    }
}

