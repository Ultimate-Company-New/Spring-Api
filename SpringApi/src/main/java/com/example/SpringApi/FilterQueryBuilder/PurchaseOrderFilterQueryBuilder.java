package com.example.SpringApi.FilterQueryBuilder;

import com.example.SpringApi.Models.DatabaseModels.OrderSummary;
import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;
import com.example.SpringApi.Models.DatabaseModels.Shipment;
import com.example.SpringApi.Models.DatabaseModels.ShipmentPackage;
import com.example.SpringApi.Models.DTOs.PurchaseOrderWithDetails;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.FilterCondition;
import com.example.SpringApi.Constants.EntityType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

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
    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_ID_PARAM = ":" + CLIENT_ID;
    private static final String SELECTED_IDS = "selectedIds";
    private static final String SELECTED_IDS_CLAUSE = "AND po.purchaseOrderId IN :" + SELECTED_IDS + " ";
    private static final String SELECTED_PRODUCT_IDS = "selectedProductIds";
    private static final String NOT_DELETED_CLAUSE = "AND po.isDeleted = false ";
    private static final String FILTER_GROUP_PREFIX = "AND (";

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
            case CLIENT_ID: return "po.clientId";
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
        return Arrays.asList("purchaseOrderId", CLIENT_ID, "approvedByUserId", "rejectedByUserId");
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
        baseQuery += "WHERE po.clientId = " + CLIENT_ID_PARAM + " ";

        // Add selectedIds condition
        if (selectedIds != null && !selectedIds.isEmpty()) {
            baseQuery += SELECTED_IDS_CLAUSE;
        }

        // Add selectedProductIds condition - filter through ShipmentProducts via OrderSummary
        if (selectedProductIds != null && !selectedProductIds.isEmpty()) {
            baseQuery += "AND EXISTS (SELECT 1 FROM OrderSummary os2, Shipment s, ShipmentProduct sp " +
                        "WHERE os2.entityType = 'PURCHASE_ORDER' " +
                        "AND os2.entityId = po.purchaseOrderId " +
                        "AND s.orderSummaryId = os2.orderSummaryId " +
                        "AND sp.shipmentId = s.shipmentId " +
                        "AND sp.productId IN :" + SELECTED_PRODUCT_IDS + ") ";
        }

        // Add includeDeleted condition
        if (!includeDeleted) {
            baseQuery += NOT_DELETED_CLAUSE;
        }
        
        if (filterResult.hasConditions()) {
            baseQuery += FILTER_GROUP_PREFIX + filterResult.getWhereClause() + ") ";
        }

        // Add ordering
        baseQuery += "ORDER BY po.purchaseOrderId DESC";

        // Count query (without FETCH joins)
        // Note: Address filtering will be handled via subquery in WHERE clause
        String countQuery = "SELECT COUNT(DISTINCT po) FROM PurchaseOrder po " +
                "WHERE po.clientId = " + CLIENT_ID_PARAM + " ";

        if (selectedIds != null && !selectedIds.isEmpty()) {
            countQuery += SELECTED_IDS_CLAUSE;
        }

        if (selectedProductIds != null && !selectedProductIds.isEmpty()) {
            countQuery += "AND EXISTS (SELECT 1 FROM OrderSummary os2, Shipment s, ShipmentProduct sp " +
                         "WHERE os2.entityType = 'PURCHASE_ORDER' " +
                         "AND os2.entityId = po.purchaseOrderId " +
                         "AND s.orderSummaryId = os2.orderSummaryId " +
                         "AND sp.shipmentId = s.shipmentId " +
                         "AND sp.productId IN :" + SELECTED_PRODUCT_IDS + ") ";
        }

        if (!includeDeleted) {
            countQuery += NOT_DELETED_CLAUSE;
        }

        if (filterResult.hasConditions()) {
            countQuery += FILTER_GROUP_PREFIX + filterResult.getWhereClause() + ") ";
        }

        // Execute count query
        TypedQuery<Long> countTypedQuery = entityManager.createQuery(countQuery, Long.class);
        countTypedQuery.setParameter(CLIENT_ID, clientId);
        
        if (selectedIds != null && !selectedIds.isEmpty()) {
            countTypedQuery.setParameter(SELECTED_IDS, selectedIds);
        }

        if (selectedProductIds != null && !selectedProductIds.isEmpty()) {
            countTypedQuery.setParameter(SELECTED_PRODUCT_IDS, selectedProductIds);
        }

        // Set filter parameters
        for (Map.Entry<String, Object> entry : filterResult.getParameters().entrySet()) {
            countTypedQuery.setParameter(entry.getKey(), entry.getValue());
        }

        Long totalCount = countTypedQuery.getSingleResult();

        // Execute main query with pagination
        TypedQuery<PurchaseOrder> mainQuery = entityManager.createQuery(baseQuery, PurchaseOrder.class);
        mainQuery.setParameter(CLIENT_ID, clientId);
        
        if (selectedIds != null && !selectedIds.isEmpty()) {
            mainQuery.setParameter(SELECTED_IDS, selectedIds);
        }

        if (selectedProductIds != null && !selectedProductIds.isEmpty()) {
            mainQuery.setParameter(SELECTED_PRODUCT_IDS, selectedProductIds);
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

    /**
     * Finds paginated purchase orders with ALL related data in a single query.
     * Returns Page of PurchaseOrderWithDetails - extract and build response model from each.
     *
     * @param clientId The client ID to filter purchase orders by
     * @param selectedIds List of specific purchase order IDs to include (null for all)
     * @param selectedProductIds List of product IDs to filter by (null for all)
     * @param logicOperator "AND" or "OR" to combine filter conditions
     * @param filters List of filter conditions to apply
     * @param includeDeleted Whether to include deleted purchase orders
     * @param pageable Pagination parameters
     * @return Page of PurchaseOrderWithDetails (PO + OrderSummary + Shipments + Resources + Payments)
     */
    public Page<PurchaseOrderWithDetails> findPaginatedWithDetails(
            Long clientId,
            List<Long> selectedIds,
            List<Long> selectedProductIds,
            String logicOperator,
            List<FilterCondition> filters,
            boolean includeDeleted,
            Pageable pageable) {

        QueryResult filterResult = buildFilterConditions(filters, logicOperator);

        // Single query: fetch PO with orderSummary, shipments, shipmentProducts, pickupLocation
        // (Hibernate allows one collection fetch path - shipmentPackages fetched in 2nd query to avoid MultipleBagFetchException)
        String baseQuery = "SELECT DISTINCT po FROM PurchaseOrder po " +
                "LEFT JOIN FETCH po.createdByUser " +
                "LEFT JOIN FETCH po.modifiedByUser " +
                "LEFT JOIN FETCH po.assignedLead " +
                "LEFT JOIN FETCH po.approvedByUser " +
                "LEFT JOIN FETCH po.rejectedByUser " +
                "LEFT JOIN FETCH po.orderSummary os " +
                "LEFT JOIN FETCH os.entityAddress " +
                "LEFT JOIN FETCH os.promo " +
                "LEFT JOIN FETCH os.shipments s " +
                "LEFT JOIN FETCH s.pickupLocation pl " +
                "LEFT JOIN FETCH pl.address " +
                "LEFT JOIN FETCH s.shipmentProducts sp " +
                "LEFT JOIN FETCH sp.product " +
                "WHERE po.clientId = " + CLIENT_ID_PARAM + " ";

        if (selectedIds != null && !selectedIds.isEmpty()) {
            baseQuery += SELECTED_IDS_CLAUSE;
        }
        if (selectedProductIds != null && !selectedProductIds.isEmpty()) {
            baseQuery += "AND EXISTS (SELECT 1 FROM OrderSummary os2, Shipment s2, ShipmentProduct sp2 " +
                    "WHERE os2.entityType = 'PURCHASE_ORDER' AND os2.entityId = po.purchaseOrderId " +
                    "AND s2.orderSummaryId = os2.orderSummaryId AND sp2.shipmentId = s2.shipmentId " +
                    "AND sp2.productId IN :" + SELECTED_PRODUCT_IDS + ") ";
        }
        if (!includeDeleted) {
            baseQuery += NOT_DELETED_CLAUSE;
        }
        if (filterResult.hasConditions()) {
            baseQuery += FILTER_GROUP_PREFIX + filterResult.getWhereClause() + ") ";
        }
        baseQuery += "ORDER BY po.purchaseOrderId DESC, s.shipmentId";

        String countQuery = "SELECT COUNT(DISTINCT po) FROM PurchaseOrder po WHERE po.clientId = " + CLIENT_ID_PARAM + " ";
        if (selectedIds != null && !selectedIds.isEmpty()) {
            countQuery += SELECTED_IDS_CLAUSE;
        }
        if (selectedProductIds != null && !selectedProductIds.isEmpty()) {
            countQuery += "AND EXISTS (SELECT 1 FROM OrderSummary os2, Shipment s2, ShipmentProduct sp2 " +
                    "WHERE os2.entityType = 'PURCHASE_ORDER' AND os2.entityId = po.purchaseOrderId " +
                    "AND s2.orderSummaryId = os2.orderSummaryId AND sp2.shipmentId = s2.shipmentId " +
                    "AND sp2.productId IN :" + SELECTED_PRODUCT_IDS + ") ";
        }
        if (!includeDeleted) {
            countQuery += NOT_DELETED_CLAUSE;
        }
        if (filterResult.hasConditions()) {
            countQuery += FILTER_GROUP_PREFIX + filterResult.getWhereClause() + ") ";
        }

        TypedQuery<Long> countTypedQuery = entityManager.createQuery(countQuery, Long.class);
        countTypedQuery.setParameter(CLIENT_ID, clientId);
        if (selectedIds != null && !selectedIds.isEmpty()) {
            countTypedQuery.setParameter(SELECTED_IDS, selectedIds);
        }
        if (selectedProductIds != null && !selectedProductIds.isEmpty()) {
            countTypedQuery.setParameter(SELECTED_PRODUCT_IDS, selectedProductIds);
        }
        for (Map.Entry<String, Object> entry : filterResult.getParameters().entrySet()) {
            countTypedQuery.setParameter(entry.getKey(), entry.getValue());
        }
        Long totalCount = countTypedQuery.getSingleResult();

        TypedQuery<PurchaseOrder> mainQuery = entityManager.createQuery(baseQuery, PurchaseOrder.class);
        mainQuery.setParameter(CLIENT_ID, clientId);
        if (selectedIds != null && !selectedIds.isEmpty()) {
            mainQuery.setParameter(SELECTED_IDS, selectedIds);
        }
        if (selectedProductIds != null && !selectedProductIds.isEmpty()) {
            mainQuery.setParameter(SELECTED_PRODUCT_IDS, selectedProductIds);
        }
        for (Map.Entry<String, Object> entry : filterResult.getParameters().entrySet()) {
            mainQuery.setParameter(entry.getKey(), entry.getValue());
        }
        mainQuery.setFirstResult((int) pageable.getOffset());
        mainQuery.setMaxResults(pageable.getPageSize());

        List<PurchaseOrder> purchaseOrders = mainQuery.getResultList();
        // Deduplicate (Cartesian product from multiple collection fetches)
        List<PurchaseOrder> distinct = purchaseOrders.stream().distinct().toList();

        // Batch fetch shipmentPackages + packageProducts (avoids MultipleBagFetchException, 1 query)
        List<Long> shipmentIds = distinct.stream()
                .map(PurchaseOrder::getOrderSummary)
                .filter(Objects::nonNull)
                .flatMap(os -> os.getShipments().stream())
                .map(s -> s.getShipmentId())
                .distinct()
                .toList();
        Map<Long, List<ShipmentPackage>> packagesByShipmentId = new HashMap<>();
        if (!shipmentIds.isEmpty()) {
            List<ShipmentPackage> allPackages = entityManager.createQuery(
                    "SELECT spkg FROM ShipmentPackage spkg " +
                            "LEFT JOIN FETCH spkg.packageInfo " +
                            "LEFT JOIN FETCH spkg.shipmentPackageProducts spp " +
                            "LEFT JOIN FETCH spp.product " +
                            "WHERE spkg.shipmentId IN :shipmentIds",
                    ShipmentPackage.class)
                    .setParameter("shipmentIds", shipmentIds)
                    .getResultList();
            packagesByShipmentId = allPackages.stream().collect(Collectors.groupingBy(ShipmentPackage::getShipmentId));
        }
        // Wire shipmentPackages into each Shipment
        for (PurchaseOrder po : distinct) {
            if (po.getOrderSummary() != null) {
                for (Shipment s : po.getOrderSummary().getShipments()) {
                    s.setShipmentPackages(packagesByShipmentId.getOrDefault(s.getShipmentId(), List.of()));
                }
            }
        }

        // Batch fetch Resources and Payments for these POs (2 queries - unavoidable as they're in separate tables)
        List<Long> poIds = distinct.stream().map(PurchaseOrder::getPurchaseOrderId).toList();
        List<com.example.SpringApi.Models.DatabaseModels.Resources> allResources =
                poIds.isEmpty() ? List.of() : entityManager.createQuery(
                        "SELECT r FROM Resources r WHERE r.entityId IN :poIds AND r.entityType = :entityType ORDER BY r.entityId, r.createdAt DESC",
                        com.example.SpringApi.Models.DatabaseModels.Resources.class)
                        .setParameter("poIds", poIds)
                        .setParameter("entityType", EntityType.PURCHASE_ORDER)
                        .getResultList();
        List<com.example.SpringApi.Models.DatabaseModels.Payment> allPayments =
                poIds.isEmpty() ? List.of() : entityManager.createQuery(
                        "SELECT p FROM Payment p WHERE p.entityType = 'PURCHASE_ORDER' AND p.entityId IN :poIds ORDER BY p.entityId, p.createdAt DESC",
                        com.example.SpringApi.Models.DatabaseModels.Payment.class)
                        .setParameter("poIds", poIds)
                        .getResultList();

        Map<Long, List<com.example.SpringApi.Models.DatabaseModels.Resources>> resourcesByPoId =
                allResources.stream().collect(Collectors.groupingBy(com.example.SpringApi.Models.DatabaseModels.Resources::getEntityId));
        Map<Long, List<com.example.SpringApi.Models.DatabaseModels.Payment>> paymentsByPoId =
                allPayments.stream().collect(Collectors.groupingBy(com.example.SpringApi.Models.DatabaseModels.Payment::getEntityId));

        List<PurchaseOrderWithDetails> result = new ArrayList<>();
        for (PurchaseOrder po : distinct) {
            OrderSummary os = po.getOrderSummary();
            List<com.example.SpringApi.Models.DatabaseModels.Resources> attachments = resourcesByPoId.getOrDefault(po.getPurchaseOrderId(), List.of());
            List<com.example.SpringApi.Models.DatabaseModels.Payment> payments = paymentsByPoId.getOrDefault(po.getPurchaseOrderId(), List.of());
            result.add(new PurchaseOrderWithDetails(po, os, attachments, payments));
        }

        return new PageImpl<>(result, pageable, totalCount);
    }
}
