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
            // Special case for address - use subquery to get address from OrderSummary
            // JPQL subquery syntax: join Address through OrderSummary
            case "address": return "(SELECT CONCAT(COALESCE(a.streetAddress, ''), ' ', COALESCE(a.city, ''), ' ', COALESCE(a.state, ''), ' ', COALESCE(a.postalCode, ''), ' ', COALESCE(a.country, '')) " +
                    "FROM OrderSummary os, Address a " +
                    "WHERE os.entityType = 'PURCHASE_ORDER' AND os.entityId = po.purchaseOrderId AND a.addressId = os.entityAddressId)";
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
        return Arrays.asList("purchaseOrderId", "clientId", "approvedByUserId", "rejectedByUserId");
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

        // Build dynamic filter conditions
        QueryResult filterResult = buildFilterConditions(filters, logicOperator);

        // Base query with necessary JOINs
        String baseQuery = "SELECT DISTINCT po FROM PurchaseOrder po " +
                "LEFT JOIN FETCH po.createdByUser " +
                "LEFT JOIN FETCH po.modifiedByUser " +
                "LEFT JOIN FETCH po.assignedLead " +
                "LEFT JOIN FETCH po.approvedByUser " +
                "LEFT JOIN FETCH po.rejectedByUser ";

        // Add address join only if needed (through OrderSummary)
        // Note: JPQL doesn't support direct joins to unrelated entities, so we'll handle address filtering in WHERE clause
        baseQuery += "WHERE po.clientId = :clientId ";

        // Add selectedIds condition
        if (selectedIds != null && !selectedIds.isEmpty()) {
            baseQuery += "AND po.purchaseOrderId IN :selectedIds ";
        }

        // Add selectedProductIds condition - filter through ShipmentProducts via OrderSummary
        if (selectedProductIds != null && !selectedProductIds.isEmpty()) {
            baseQuery += "AND EXISTS (SELECT 1 FROM OrderSummary os2, Shipment s, ShipmentProduct sp " +
                        "WHERE os2.entityType = 'PURCHASE_ORDER' " +
                        "AND os2.entityId = po.purchaseOrderId " +
                        "AND s.orderSummaryId = os2.orderSummaryId " +
                        "AND sp.shipmentId = s.shipmentId " +
                        "AND sp.productId IN :selectedProductIds) ";
        }

        // Add includeDeleted condition
        if (!includeDeleted) {
            baseQuery += "AND po.isDeleted = false ";
        }
        
        if (filterResult.hasConditions()) {
            baseQuery += "AND (" + filterResult.getWhereClause() + ") ";
        }

        // Add ordering
        baseQuery += "ORDER BY po.purchaseOrderId DESC";

        // Count query (without FETCH joins)
        // Note: Address filtering will be handled via subquery in WHERE clause
        String countQuery = "SELECT COUNT(DISTINCT po) FROM PurchaseOrder po " +
                "WHERE po.clientId = :clientId ";

        if (selectedIds != null && !selectedIds.isEmpty()) {
            countQuery += "AND po.purchaseOrderId IN :selectedIds ";
        }

        if (selectedProductIds != null && !selectedProductIds.isEmpty()) {
            countQuery += "AND EXISTS (SELECT 1 FROM OrderSummary os2, Shipment s, ShipmentProduct sp " +
                         "WHERE os2.entityType = 'PURCHASE_ORDER' " +
                         "AND os2.entityId = po.purchaseOrderId " +
                         "AND s.orderSummaryId = os2.orderSummaryId " +
                         "AND sp.shipmentId = s.shipmentId " +
                         "AND sp.productId IN :selectedProductIds) ";
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

