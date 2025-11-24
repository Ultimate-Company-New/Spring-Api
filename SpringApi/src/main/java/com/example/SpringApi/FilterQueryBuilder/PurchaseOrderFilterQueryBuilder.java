package com.example.SpringApi.FilterQueryBuilder;

import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;
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
 * PurchaseOrder-specific filter query builder that handles dynamic multi-filter queries.
 * 
 * This class combines:
 * 1. Column mapping logic (which columns map to which database fields)
 * 2. Query building logic (using BaseFilterQueryBuilder)
 * 3. Query execution logic (executing the built queries via EntityManager)
 */
@Component
public class PurchaseOrderFilterQueryBuilder extends BaseFilterQueryBuilder {

    private final EntityManager entityManager;

    // Constructor for Spring dependency injection
    public PurchaseOrderFilterQueryBuilder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // ==================== Column Mapping Methods ====================

    @Override
    protected String mapColumnToField(String column) {
        switch (column) {
            case "purchaseOrderId": return "po.purchaseOrderId";
            case "clientId": return "po.clientId";
            case "vendorNumber": return "po.vendorNumber";
            case "purchaseOrderStatus": return "po.purchaseOrderStatus";
            case "priority": return "po.priority";
            case "paymentId": return "po.paymentId";
            case "expectedDeliveryDate": return "po.expectedDeliveryDate";
            case "purchaseOrderReceipt": return "po.purchaseOrderReceipt";
            case "termsConditionsHtml": return "po.termsConditionsHtml";
            case "approvedByUserId": return "po.approvedByUserId";
            case "approvedDate": return "po.approvedDate";
            case "rejectedByUserId": return "po.rejectedByUserId";
            case "rejectedDate": return "po.rejectedDate";
            case "isDeleted": return "po.isDeleted";
            case "notes": return "po.notes";
            case "createdUser": return "po.createdUser";
            case "modifiedUser": return "po.modifiedUser";
            case "createdAt": return "po.createdAt";
            case "updatedAt": return "po.updatedAt";
            // Special case for address - concatenated field from joined table
            case "address": return "CONCAT(addr.streetAddress, ' ', addr.city, ' ', addr.state, ' ', addr.postalCode, ' ', addr.country)";
            default: return "po." + column;
        }
    }

    @Override
    protected List<String> getDateColumns() {
        return Arrays.asList("expectedDeliveryDate", "approvedDate", "rejectedDate", "createdAt", "updatedAt");
    }

    @Override
    protected List<String> getBooleanColumns() {
        return Arrays.asList("isDeleted");
    }

    @Override
    protected List<String> getNumberColumns() {
        return Arrays.asList("purchaseOrderId", "clientId", "paymentId", "approvedByUserId", "rejectedByUserId");
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
     * Finds paginated purchase orders with multiple filter conditions combined with AND/OR logic.
     * Builds the WHERE clause dynamically and executes the query.
     * 
     * @param clientId The client ID to filter purchase orders by
     * @param selectedIds List of specific purchase order IDs to include (null for all)
     * @param selectedProductIds List of product IDs to filter by (null for all)
     * @param logicOperator "AND" or "OR" to combine filter conditions
     * @param filters List of filter conditions to apply
     * @param includeDeleted Whether to include deleted purchase orders
     * @param pageable Pagination parameters
     * @return Page of purchase orders matching the filter criteria
     */
    public Page<PurchaseOrder> findPaginatedEntitiesWithMultipleFilters(
            Long clientId,
            List<Long> selectedIds,
            List<Long> selectedProductIds,
            String logicOperator,
            List<FilterCondition> filters,
            boolean includeDeleted,
            Pageable pageable) {

        // Base query with all necessary JOINs
        String baseQuery = "SELECT DISTINCT po FROM PurchaseOrder po " +
                "LEFT JOIN FETCH po.purchaseOrderAddress addr " +
                "LEFT JOIN FETCH po.paymentInfo payment " +
                "LEFT JOIN FETCH po.createdByUser creator " +
                "LEFT JOIN FETCH po.modifiedByUser modifier " +
                "LEFT JOIN FETCH po.assignedLead lead " +
                "LEFT JOIN FETCH po.approvedByUser approver " +
                "LEFT JOIN FETCH po.rejectedByUser rejecter " +
                "LEFT JOIN FETCH po.purchaseOrderQuantityPriceMaps poqm " +
                "LEFT JOIN FETCH poqm.product prod " +
                "LEFT JOIN FETCH prod.productPickupLocationMappings pplm " +
                "LEFT JOIN FETCH pplm.pickupLocation pkl " +
                "WHERE po.clientId = :clientId ";

        // Add selectedIds condition
        if (selectedIds != null && !selectedIds.isEmpty()) {
            baseQuery += "AND po.purchaseOrderId IN :selectedIds ";
        }

        // Add selectedProductIds condition
        if (selectedProductIds != null && !selectedProductIds.isEmpty()) {
            baseQuery += "AND EXISTS (SELECT 1 FROM PurchaseOrderQuantityPriceMap poqm2 " +
                        "WHERE poqm2.purchaseOrderId = po.purchaseOrderId " +
                        "AND poqm2.productId IN :selectedProductIds) ";
        }

        // Add includeDeleted condition
        if (!includeDeleted) {
            baseQuery += "AND po.isDeleted = false ";
        }

        // Build dynamic filter conditions using the query builder
        QueryResult filterResult = buildFilterConditions(filters, logicOperator);
        
        if (filterResult.hasConditions()) {
            baseQuery += "AND (" + filterResult.getWhereClause() + ") ";
        }

        // Add ordering
        baseQuery += "ORDER BY po.purchaseOrderId DESC";

        // Count query (without FETCH joins)
        String countQuery = "SELECT COUNT(DISTINCT po) FROM PurchaseOrder po " +
                "LEFT JOIN po.purchaseOrderAddress addr " +
                "WHERE po.clientId = :clientId ";

        if (selectedIds != null && !selectedIds.isEmpty()) {
            countQuery += "AND po.purchaseOrderId IN :selectedIds ";
        }

        if (selectedProductIds != null && !selectedProductIds.isEmpty()) {
            countQuery += "AND EXISTS (SELECT 1 FROM PurchaseOrderQuantityPriceMap poqm2 " +
                         "WHERE poqm2.purchaseOrderId = po.purchaseOrderId " +
                         "AND poqm2.productId IN :selectedProductIds) ";
        }

        if (!includeDeleted) {
            countQuery += "AND po.isDeleted = false ";
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

        if (selectedProductIds != null && !selectedProductIds.isEmpty()) {
            countTypedQuery.setParameter("selectedProductIds", selectedProductIds);
        }

        // Set filter parameters
        for (Map.Entry<String, Object> entry : filterResult.getParameters().entrySet()) {
            countTypedQuery.setParameter(entry.getKey(), entry.getValue());
        }

        Long totalCount = countTypedQuery.getSingleResult();

        // Execute main query with pagination
        TypedQuery<PurchaseOrder> mainQuery = entityManager.createQuery(baseQuery, PurchaseOrder.class);
        mainQuery.setParameter("clientId", clientId);
        
        if (selectedIds != null && !selectedIds.isEmpty()) {
            mainQuery.setParameter("selectedIds", selectedIds);
        }

        if (selectedProductIds != null && !selectedProductIds.isEmpty()) {
            mainQuery.setParameter("selectedProductIds", selectedProductIds);
        }

        // Set filter parameters
        for (Map.Entry<String, Object> entry : filterResult.getParameters().entrySet()) {
            mainQuery.setParameter(entry.getKey(), entry.getValue());
        }

        // Apply pagination
        mainQuery.setFirstResult((int) pageable.getOffset());
        mainQuery.setMaxResults(pageable.getPageSize());

        List<PurchaseOrder> purchaseOrders = mainQuery.getResultList();

        return new PageImpl<>(purchaseOrders, pageable, totalCount);
    }
}

