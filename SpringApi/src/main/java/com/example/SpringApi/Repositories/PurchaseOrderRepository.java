package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for PurchaseOrder entity operations.
 */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    
    /**
     * Finds a purchase order by ID and client ID.
     * 
     * @param purchaseOrderId The purchase order ID
     * @param clientId The client ID
     * @return Optional containing the purchase order if found
     */
    @Query("SELECT po FROM PurchaseOrder po WHERE po.purchaseOrderId = :purchaseOrderId AND po.clientId = :clientId")
    Optional<PurchaseOrder> findByPurchaseOrderIdAndClientId(@Param("purchaseOrderId") Long purchaseOrderId, 
                                                              @Param("clientId") Long clientId);
    
    /**
     * Finds a purchase order by ID and client ID with all related entities eagerly loaded.
     * 
     * This method fetches the complete purchase order data structure including:
     * - Address (delivery/billing address)
     * - PaymentInfo (payment details)
     * - Created By User
     * - Modified By User
     * - Assigned Lead
     * - Approved By User
     * - Purchase Order Quantity Maps with Products and Pickup Locations
     * 
     * @param purchaseOrderId The purchase order ID
     * @param clientId The client ID
     * @return Optional containing the purchase order with all relationships if found
     */
    @Query("SELECT DISTINCT po FROM PurchaseOrder po " +
           // 1. Fetch Address for PurchaseOrder
           "LEFT JOIN FETCH po.purchaseOrderAddress addr " +
           "LEFT JOIN FETCH po.paymentInfo payment " +
           "LEFT JOIN FETCH po.createdByUser creator " +
           "LEFT JOIN FETCH po.modifiedByUser modifier " +
           "LEFT JOIN FETCH po.assignedLead lead " +
           "LEFT JOIN FETCH po.approvedByUser approver " +
           "LEFT JOIN FETCH po.rejectedByUser rejecter " +
           
           // 2. Fetch Products with Quantities and their PickupLocations
           "LEFT JOIN FETCH po.purchaseOrderQuantityPriceMaps poqm " +
           "LEFT JOIN FETCH poqm.product prod " +
           "LEFT JOIN FETCH prod.pickupLocation pkl " +
           
           "WHERE po.purchaseOrderId = :purchaseOrderId " +
           "AND po.clientId = :clientId " +
           "AND po.isDeleted = false")
    Optional<PurchaseOrder> findByPurchaseOrderIdAndClientIdWithAllRelations(@Param("purchaseOrderId") Long purchaseOrderId, 
                                                                               @Param("clientId") Long clientId);
    
    /**
     * Finds paginated purchase orders with all related entities eagerly loaded.
     * 
     * Data Structure:
     * 1. PurchaseOrder → Address (delivery/billing address)
     * 2. PurchaseOrder → PurchaseOrderQuantityPriceMaps → Product (with quantities)
     *                                                    → PickupLocation (where each product is picked from)
     * 
     * @param clientId The client ID to filter by
     * @param columnName The column name for filtering
     * @param condition The filter condition (contains, equals, startsWith, endsWith, isEmpty, isNotEmpty)
     * @param filterExpr The filter expression/value
     * @param includeDeleted Whether to include deleted records
     * @param selectedProductIds List of product IDs to filter by (optional)
     * @param pageable Pagination parameters
     * @return Page of PurchaseOrder entities with all relationships loaded
     */
    @Query("SELECT DISTINCT po FROM PurchaseOrder po " +
           // 1. Fetch Address for PurchaseOrder
           "LEFT JOIN FETCH po.purchaseOrderAddress addr " +
           "LEFT JOIN FETCH po.paymentInfo payment " +
           "LEFT JOIN FETCH po.createdByUser creator " +
           "LEFT JOIN FETCH po.modifiedByUser modifier " +
           "LEFT JOIN FETCH po.assignedLead lead " +
           "LEFT JOIN FETCH po.approvedByUser approver " +
           "LEFT JOIN FETCH po.rejectedByUser rejecter " +
           
           // 2. Fetch Products with Quantities and their PickupLocations
           "LEFT JOIN FETCH po.purchaseOrderQuantityPriceMaps poqm " +
           "LEFT JOIN FETCH poqm.product prod " +
           "LEFT JOIN FETCH prod.pickupLocation pkl " +
           
           "WHERE po.clientId = :clientId " +
           "AND (:includeDeleted = true OR po.isDeleted = false) " +
           
           // Filter by selected product IDs
           "AND (:selectedProductIds IS NULL OR " +
           "EXISTS (SELECT 1 FROM PurchaseOrderQuantityPriceMap poqm2 " +
           "        WHERE poqm2.purchaseOrderId = po.purchaseOrderId " +
           "        AND poqm2.productId IN :selectedProductIds)) " +
           
           // Standard column filtering
           "AND (COALESCE(:filterExpr, '') = '' OR " +
           "(CASE :columnName " +
           "WHEN 'purchaseOrderId' THEN CONCAT(po.purchaseOrderId, '') " +
           "WHEN 'vendorNumber' THEN CONCAT(po.vendorNumber, '') " +
           "WHEN 'purchaseOrderStatus' THEN CONCAT(po.purchaseOrderStatus, '') " +
           "WHEN 'priority' THEN CONCAT(po.priority, '') " +
           "WHEN 'paymentId' THEN CONCAT(po.paymentId, '') " +
           "WHEN 'expectedDeliveryDate' THEN CONCAT(po.expectedDeliveryDate, '') " +
           "WHEN 'createdAt' THEN CONCAT(po.createdAt, '') " +
           "WHEN 'updatedAt' THEN CONCAT(po.updatedAt, '') " +
           "WHEN 'purchaseOrderReceipt' THEN CONCAT(po.purchaseOrderReceipt, '') " +
           "WHEN 'termsConditionsHtml' THEN CONCAT(po.termsConditionsHtml, '') " +
           "WHEN 'notes' THEN CONCAT(po.notes, '') " +
           "WHEN 'createdUser' THEN CONCAT(po.createdUser, '') " +
           "WHEN 'modifiedUser' THEN CONCAT(po.modifiedUser, '') " +
           "WHEN 'approvedByUserId' THEN CONCAT(po.approvedByUserId, '') " +
           "WHEN 'approvedDate' THEN CONCAT(po.approvedDate, '') " +
           "WHEN 'rejectedByUserId' THEN CONCAT(po.rejectedByUserId, '') " +
           "WHEN 'rejectedDate' THEN CONCAT(po.rejectedDate, '') " +
           
           // Address filtering
           "WHEN 'address' THEN CONCAT(addr.streetAddress, ' ', addr.city, ' ', addr.state, ' ', addr.postalCode, ' ', addr.country, '') " +
           
           "ELSE '' END) LIKE " +
           "(CASE :condition " +
           "WHEN 'contains' THEN CONCAT('%', :filterExpr, '%') " +
           "WHEN 'equals' THEN :filterExpr " +
           "WHEN 'startsWith' THEN CONCAT(:filterExpr, '%') " +
           "WHEN 'endsWith' THEN CONCAT('%', :filterExpr) " +
           "WHEN 'isEmpty' THEN '' " +
           "WHEN 'isNotEmpty' THEN '%' " +
           "ELSE '' END))")
    Page<PurchaseOrder> findPaginatedPurchaseOrders(@Param("clientId") Long clientId,
                                                     @Param("columnName") String columnName,
                                                     @Param("condition") String condition,
                                                     @Param("filterExpr") String filterExpr,
                                                     @Param("includeDeleted") boolean includeDeleted,
                                                     @Param("selectedProductIds") List<Long> selectedProductIds,
                                                     Pageable pageable);
}

