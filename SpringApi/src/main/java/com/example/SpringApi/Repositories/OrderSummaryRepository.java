package com.example.SpringApi.Repositories;

import com.example.SpringApi.Models.DatabaseModels.OrderSummary;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderSummaryRepository extends JpaRepository<OrderSummary, Long> {
  Optional<OrderSummary> findByEntityTypeAndEntityId(String entityType, Long entityId);

  Optional<OrderSummary> findByOrderSummaryIdAndClientId(Long orderSummaryId, Long clientId);

  /** Batch fetch order summaries for multiple entity IDs with address and promo eagerly loaded. */
  @Query(
      "SELECT DISTINCT os FROM OrderSummary os LEFT JOIN FETCH os.entityAddress LEFT JOIN FETCH os.promo "
          + "WHERE os.entityType = :entityType AND os.entityId IN :entityIds")
  List<OrderSummary> findByEntityTypeAndEntityIdInWithAddressAndPromo(
      @Param("entityType") String entityType, @Param("entityIds") List<Long> entityIds);

  /**
   * Find order summary by purchase order ID. Convenience method that uses
   * entityType='PURCHASE_ORDER' and entityId=purchaseOrderId.
   */
  default Optional<OrderSummary> findByPurchaseOrderId(Long purchaseOrderId) {
    return findByEntityTypeAndEntityId("PURCHASE_ORDER", purchaseOrderId);
  }
}

