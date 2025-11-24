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
     * - Purchase Order Quantity Maps with Products and their Pickup Location Mappings
     * 
     * Note: Product.productPickupLocationMappings uses Set instead of List to avoid
     * Hibernate's MultipleBagFetchException when fetching multiple collections.
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
           
           // 2. Fetch Products with Quantities and their PickupLocation Mappings
           "LEFT JOIN FETCH po.purchaseOrderQuantityPriceMaps poqm " +
           "LEFT JOIN FETCH poqm.product prod " +
           "LEFT JOIN FETCH prod.productPickupLocationMappings pplm " +
           "LEFT JOIN FETCH pplm.pickupLocation pkl " +
           
           "WHERE po.purchaseOrderId = :purchaseOrderId " +
           "AND po.clientId = :clientId " +
           "AND po.isDeleted = false")
    Optional<PurchaseOrder> findByPurchaseOrderIdAndClientIdWithAllRelations(@Param("purchaseOrderId") Long purchaseOrderId, 
                                                                               @Param("clientId") Long clientId);
    
}

