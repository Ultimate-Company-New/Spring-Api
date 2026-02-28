package springapi.services.interfaces;

import com.itextpdf.text.DocumentException;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.util.List;
import springapi.models.databasemodels.Payment;
import springapi.models.requestmodels.CashPaymentRequestModel;
import springapi.models.requestmodels.RazorpayOrderRequestModel;
import springapi.models.requestmodels.RazorpayVerifyRequestModel;
import springapi.models.responsemodels.PaymentVerificationResponseModel;
import springapi.models.responsemodels.RazorpayOrderResponseModel;

/**
 * Interface for Payment-related business operations.
 *
 * <p>This interface defines the contract for Razorpay payment operations including order creation,
 * payment verification, cash payment recording, refunds, and receipt generation. All
 * implementations should handle proper validation, error handling, and logging.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public interface PaymentSubTranslator {

  /**
   * Creates a Razorpay order for a purchase order payment.
   *
   * <p>This method creates a Razorpay order and returns the necessary details for the frontend to
   * open the Razorpay checkout modal. Only PENDING_APPROVAL orders can be paid.
   *
   * @param request The order request containing purchaseOrderId and optional amount
   * @return RazorpayOrderResponseModel containing order details for frontend
   * @throws springapi.exceptions.BadRequestException if validation fails or Razorpay API fails
   * @throws springapi.exceptions.NotFoundException if purchase order or client not found
   */
  RazorpayOrderResponseModel createOrder(RazorpayOrderRequestModel request);

  /**
   * Creates a Razorpay order for a follow-up payment (order already approved/partially paid).
   *
   * <p>This method allows orders with APPROVED or APPROVED_WITH_PARTIAL_PAYMENT status to create
   * additional payment orders for remaining balance.
   *
   * @param request The order request containing purchaseOrderId and optional amount
   * @return RazorpayOrderResponseModel containing order details for frontend
   * @throws springapi.exceptions.BadRequestException if validation fails or Razorpay API fails
   * @throws springapi.exceptions.NotFoundException if purchase order or client not found
   */
  RazorpayOrderResponseModel createOrderFollowUp(RazorpayOrderRequestModel request);

  /**
   * Verifies a Razorpay payment and updates the purchase order status.
   *
   * <p>This method verifies the payment signature, marks the payment as captured, and updates the
   * purchase order status to APPROVED or APPROVED_WITH_PARTIAL_PAYMENT.
   *
   * @param request The verification request with payment details
   * @return PaymentVerificationResponseModel indicating success/failure
   * @throws springapi.exceptions.BadRequestException if signature verification fails
   * @throws springapi.exceptions.NotFoundException if purchase order or payment not found
   */
  PaymentVerificationResponseModel verifyPayment(RazorpayVerifyRequestModel request);

  /**
   * Records a cash/manual payment for a purchase order.
   *
   * <p>Creates a Payment record with CASH or UPI payment method and updates the purchase order
   * status to APPROVED. Only PENDING_APPROVAL orders can be paid.
   *
   * @param request The cash payment request containing payment details
   * @return PaymentVerificationResponseModel indicating success/failure
   * @throws springapi.exceptions.BadRequestException if validation fails
   * @throws springapi.exceptions.NotFoundException if purchase order not found
   */
  PaymentVerificationResponseModel recordCashPayment(CashPaymentRequestModel request);

  /**
   * Verifies a Razorpay payment for a follow-up payment (order already approved/partially paid).
   *
   * <p>This method allows payments for APPROVED or APPROVED_WITH_PARTIAL_PAYMENT orders. Does NOT
   * trigger shipment processing (shipments already created).
   *
   * @param request The verification request with payment details
   * @return PaymentVerificationResponseModel indicating success/failure
   * @throws springapi.exceptions.BadRequestException if signature verification fails
   * @throws springapi.exceptions.NotFoundException if purchase order or payment not found
   */
  PaymentVerificationResponseModel verifyPaymentFollowUp(RazorpayVerifyRequestModel request);

  /**
   * Records a cash/manual payment for a follow-up payment (order already approved/partially paid).
   *
   * <p>This method allows payments for APPROVED or APPROVED_WITH_PARTIAL_PAYMENT orders. Does NOT
   * trigger shipment processing (shipments already created).
   *
   * @param request The cash payment request containing payment details
   * @return PaymentVerificationResponseModel indicating success/failure
   * @throws springapi.exceptions.BadRequestException if validation fails
   * @throws springapi.exceptions.NotFoundException if purchase order not found
   */
  PaymentVerificationResponseModel recordCashPaymentFollowUp(CashPaymentRequestModel request);

  /**
   * Gets the Razorpay Key ID for the current client.
   *
   * <p>This is the public key and is safe to expose. Used when the frontend needs to initialize
   * Razorpay without creating an order first.
   *
   * @return The Razorpay Key ID (public API key)
   * @throws springapi.exceptions.NotFoundException if client not found or Razorpay not configured
   */
  String getRazorpayKeyId();

  /**
   * Generates a PDF receipt for a payment.
   *
   * <p>This method fetches the payment, validates client access, and generates a formatted PDF
   * receipt using the payment, purchase order, and client information.
   *
   * @param paymentId The ID of the payment to generate receipt for
   * @return The PDF as a byte array
   * @throws springapi.exceptions.BadRequestException if validation fails
   * @throws springapi.exceptions.NotFoundException if payment or purchase order not found
   * @throws TemplateException if PDF template processing fails
   * @throws IOException if PDF generation fails
   * @throws DocumentException if PDF document creation fails
   */
  byte[] generatePaymentReceiptPdf(Long paymentId)
      throws TemplateException, IOException, DocumentException;

  /**
   * Gets all payments for a purchase order.
   *
   * @param purchaseOrderId The purchase order ID
   * @return List of payments for the purchase order
   * @throws springapi.exceptions.BadRequestException if client access denied
   * @throws springapi.exceptions.NotFoundException if purchase order not found
   */
  List<Payment> getPaymentsForPurchaseOrder(Long purchaseOrderId);

  /**
   * Gets payment by ID.
   *
   * @param paymentId The payment ID
   * @return The payment entity
   * @throws springapi.exceptions.BadRequestException if client access denied
   * @throws springapi.exceptions.NotFoundException if payment not found
   */
  Payment getPaymentById(Long paymentId);

  /**
   * Checks if a purchase order has been paid.
   *
   * @param purchaseOrderId The purchase order ID
   * @return true if the purchase order has at least one successful payment
   */
  boolean isPurchaseOrderPaid(Long purchaseOrderId);

  /**
   * Initiates a refund for a payment.
   *
   * @param paymentId The payment ID to refund
   * @param amountInPaise Amount to refund in paise (null for full refund)
   * @param reason Reason for refund
   * @return Updated Payment entity
   * @throws springapi.exceptions.BadRequestException if validation fails or refund cannot be
   *     processed
   * @throws springapi.exceptions.NotFoundException if payment not found
   */
  Payment initiateRefund(Long paymentId, Long amountInPaise, String reason);
}
