package springapi.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import springapi.models.databasemodels.Payment;

/**
 * Repository for Payment entity. Provides methods for payment tracking and Razorpay integration.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

  /** Find payment by Razorpay Order ID. */
  Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

  /** Find payment by Razorpay Payment ID. */
  Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

  /** Find all payments for an entity. */
  List<Payment> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

  /** Find successful payment for an entity. */
  @Query(
      "SELECT p FROM Payment p WHERE p.entityType = :entityType AND p.entityId = :entityId "
          + "AND p.paymentStatus IN ('CAPTURED', 'PARTIALLY_REFUNDED') ORDER BY p.createdAt DESC")
  List<Payment> findSuccessfulPaymentsForEntity(
      @Param("entityType") String entityType, @Param("entityId") Long entityId);

  /** Find the latest payment for an entity. */
  Optional<Payment> findFirstByEntityTypeAndEntityIdOrderByCreatedAtDesc(
      String entityType, Long entityId);

  /** Find payments by client ID. */
  List<Payment> findByClientIdOrderByCreatedAtDesc(Long clientId);

  /** Find payments by status. */
  List<Payment> findByClientIdAndPaymentStatusOrderByCreatedAtDesc(
      Long clientId, String paymentStatus);

  /** Find payment by entity (convenience method for purchase orders). */
  default Optional<Payment> findByPurchaseOrderId(Long purchaseOrderId) {
    return findFirstByEntityTypeAndEntityIdOrderByCreatedAtDesc("PURCHASE_ORDER", purchaseOrderId);
  }

  /** Find all payments for a purchase order. */
  default List<Payment> findAllByPurchaseOrderId(Long purchaseOrderId) {
    return findByEntityTypeAndEntityIdOrderByCreatedAtDesc("PURCHASE_ORDER", purchaseOrderId);
  }

  /** Batch fetch all payments for multiple purchase orders. */
  @Query(
      "SELECT p FROM Payment p WHERE p.entityType = 'PURCHASE_ORDER' AND "
          + "p.entityId IN :purchaseOrderIds ORDER BY p.entityId, p.createdAt "
          + "DESC")
  List<Payment> findAllByPurchaseOrderIdIn(@Param("purchaseOrderIds") List<Long> purchaseOrderIds);

  /** Find successful payments for a purchase order. */
  default List<Payment> findSuccessfulPaymentsForPurchaseOrder(Long purchaseOrderId) {
    return findSuccessfulPaymentsForEntity("PURCHASE_ORDER", purchaseOrderId);
  }

  /** Find refundable payments for an entity. */
  @Query(
      "SELECT p FROM Payment p WHERE p.entityType = :entityType AND p.entityId = :entityId "
          + "AND p.paymentStatus IN ('CAPTURED', 'PARTIALLY_REFUNDED') "
          + "AND (p.amountPaidPaise - COALESCE(p.amountRefundedPaise, 0)) > 0 "
          + "ORDER BY p.createdAt DESC")
  List<Payment> findRefundablePaymentsForEntity(
      @Param("entityType") String entityType, @Param("entityId") Long entityId);

  /** Check if entity has successful payment. */
  @Query(
      "SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Payment p "
          + "WHERE p.entityType = :entityType AND p.entityId = :entityId "
          + "AND p.paymentStatus IN ('CAPTURED', 'PARTIALLY_REFUNDED')")
  boolean hasSuccessfulPayment(
      @Param("entityType") String entityType, @Param("entityId") Long entityId);

  /** Get total paid amount for an entity in paise. */
  @Query(
      "SELECT COALESCE(SUM(p.amountPaidPaise - COALESCE(p.amountRefundedPaise, 0)), 0) "
          + "FROM Payment p WHERE p.entityType = :entityType AND p.entityId = :entityId "
          + "AND p.paymentStatus IN ('CAPTURED', 'PARTIALLY_REFUNDED')")
  Long getTotalNetPaidPaiseForEntity(
      @Param("entityType") String entityType, @Param("entityId") Long entityId);
}
