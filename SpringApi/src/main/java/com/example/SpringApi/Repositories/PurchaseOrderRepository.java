package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository interface for PurchaseOrder entity operations. */
@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

  /**
   * Finds a purchase order by ID and client ID.
   *
   * @param purchaseOrderId The purchase order ID
   * @param clientId The client ID
   * @return Optional containing the purchase order if found
   */
  @Query(
      "SELECT po FROM PurchaseOrder po WHERE po.purchaseOrderId = :purchaseOrderId AND po.clientId = :clientId")
  Optional<PurchaseOrder> findByPurchaseOrderIdAndClientId(
      @Param("purchaseOrderId") Long purchaseOrderId, @Param("clientId") Long clientId);

  /**
   * Finds a purchase order by ID and client ID with all related entities eagerly loaded.
   *
   * <p>This method fetches the complete purchase order data structure including: - Created By User
   * - Modified By User - Assigned Lead - Approved By User - Rejected By User - Client
   *
   * <p>Note: Address and financial details are now stored in OrderSummary (linked via
   * entityType/entityId). Products and shipments are stored in ShipmentProduct and related tables
   * (linked via OrderSummary).
   *
   * @param purchaseOrderId The purchase order ID
   * @param clientId The client ID
   * @return Optional containing the purchase order with all relationships if found
   */
  @Query(
      "SELECT DISTINCT po FROM PurchaseOrder po "
          + "LEFT JOIN FETCH po.createdByUser creator "
          + "LEFT JOIN FETCH po.modifiedByUser modifier "
          + "LEFT JOIN FETCH po.assignedLead lead "
          + "LEFT JOIN FETCH po.approvedByUser approver "
          + "LEFT JOIN FETCH po.rejectedByUser rejecter "
          + "LEFT JOIN FETCH po.client client "
          + "WHERE po.purchaseOrderId = :purchaseOrderId "
          + "AND po.clientId = :clientId "
          + "AND po.isDeleted = false")
  Optional<PurchaseOrder> findByPurchaseOrderIdAndClientIdWithAllRelations(
      @Param("purchaseOrderId") Long purchaseOrderId, @Param("clientId") Long clientId);
}

