package springapi.controllers;

import com.itextpdf.text.DocumentException;
import freemarker.template.TemplateException;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springapi.exceptions.BadRequestException;
import springapi.exceptions.NotFoundException;
import springapi.exceptions.UnauthorizedException;
import springapi.logging.ContextualLogger;
import springapi.models.Authorizations;
import springapi.models.requestmodels.CashPaymentRequestModel;
import springapi.models.requestmodels.ProcessPaymentAndShipmentRequestModel;
import springapi.models.requestmodels.RazorpayOrderRequestModel;
import springapi.models.requestmodels.RazorpayVerifyRequestModel;
import springapi.models.responsemodels.PaymentVerificationResponseModel;
import springapi.models.responsemodels.RazorpayOrderResponseModel;
import springapi.services.ShippingService;
import springapi.services.interfaces.PaymentSubTranslator;

/**
 * REST Controller for Payment operations.
 *
 * <p>Handles Razorpay payment integration including order creation and payment verification.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2026-01-07
 */
@RestController
@RequestMapping("/api/Payment")
public class PaymentController extends BaseController {

  private static final ContextualLogger logger =
      ContextualLogger.getLogger(PaymentController.class);
  private final PaymentSubTranslator paymentService;
  private final ShippingService shippingService;

  @Autowired
  public PaymentController(PaymentSubTranslator paymentService, ShippingService shippingService) {
    this.paymentService = paymentService;
    this.shippingService = shippingService;
  }

  /**
   * Creates a Razorpay order for a purchase order payment.
   *
   * <p>This endpoint creates a Razorpay order and returns all the necessary details for the
   * frontend to open the Razorpay checkout modal.
   *
   * @param request The order request containing purchaseOrderId and optional amount
   * @return ResponseEntity containing RazorpayOrderResponseModel with order details
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION
          + "')")
  @PostMapping("/createOrder")
  public ResponseEntity<?> createOrder(@RequestBody RazorpayOrderRequestModel request) {
    try {
      RazorpayOrderResponseModel response = paymentService.createOrder(request);
      return ResponseEntity.ok(response);
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (NotFoundException nfe) {
      return notFound(logger, nfe);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Creates a Razorpay order for a follow-up payment (order already approved/partially paid). This.
   * endpoint allows orders with APPROVED or APPROVED_WITH_PARTIAL_PAYMENT status.
   *
   * @param request The order request containing purchaseOrderId and optional amount
   * @return ResponseEntity containing RazorpayOrderResponseModel with order details
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION
          + "')")
  @PostMapping("/createOrderFollowUp")
  public ResponseEntity<?> createOrderFollowUp(@RequestBody RazorpayOrderRequestModel request) {
    try {
      RazorpayOrderResponseModel response = paymentService.createOrderFollowUp(request);
      return ResponseEntity.ok(response);
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (NotFoundException nfe) {
      return notFound(logger, nfe);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Verifies a Razorpay payment and updates the purchase order status.
   *
   * <p>After successful payment on the frontend, this endpoint verifies the payment signature and
   * updates the purchase order to APPROVED status.
   *
   * @param request The verification request containing payment details
   * @return ResponseEntity containing PaymentVerificationResponseModel
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION
          + "')")
  @PostMapping("/verifyPayment")
  public ResponseEntity<?> verifyPayment(@RequestBody RazorpayVerifyRequestModel request) {
    try {
      PaymentVerificationResponseModel response = paymentService.verifyPayment(request);
      if (response.isSuccess()) {
        return ResponseEntity.ok(response);
      } else {
        return ResponseEntity.badRequest().body(response);
      }
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (NotFoundException nfe) {
      return notFound(logger, nfe);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Gets the Razorpay Key ID for frontend use.
   *
   * <p>This is the public key and is safe to expose. Used when the frontend needs to initialize
   * Razorpay without creating an order first.
   *
   * @return ResponseEntity containing the Razorpay Key ID
   */
  @GetMapping("/getRazorpayKeyId")
  public ResponseEntity<?> getRazorpayKeyId() {
    try {
      return ResponseEntity.ok(paymentService.getRazorpayKeyId());
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Records a cash/manual payment for a purchase order.
   *
   * <p>This endpoint allows recording cash payments or manual payments (including UPI) that were
   * received outside of the Razorpay gateway. The payment is immediately marked as captured and the
   * purchase order is updated to APPROVED status.
   *
   * @param request The cash payment request containing payment details
   * @return ResponseEntity containing PaymentVerificationResponseModel
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION
          + "')")
  @PostMapping("/recordCashPayment")
  public ResponseEntity<?> recordCashPayment(@RequestBody CashPaymentRequestModel request) {
    try {
      PaymentVerificationResponseModel response = paymentService.recordCashPayment(request);
      if (response.isSuccess()) {
        return ResponseEntity.ok(response);
      } else {
        return ResponseEntity.badRequest().body(response);
      }
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (NotFoundException nfe) {
      return notFound(logger, nfe);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Processes payment and shipments for a purchase order.
   *
   * <p>This endpoint handles the complete flow: 1. Validates product and package availability 2.
   * Processes payment (online or cash) 3. Updates inventory (reduces quantities) 4. Creates
   * ShipRocket orders 5. Updates shipments with ShipRocket response 6. Updates PO status (APPROVED
   * or APPROVED_WITH_PARTIAL_PAYMENT)
   *
   * @param request The payment and shipment processing request
   * @return ResponseEntity containing PaymentVerificationResponseModel
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION
          + "')")
  @PostMapping("/processPaymentAndShipments")
  public ResponseEntity<?> processPaymentAndShipments(
      @RequestBody ProcessPaymentAndShipmentRequestModel request) {
    try {
      PaymentVerificationResponseModel response;

      if (request.isCashPayment()) {
        if (request.getCashPaymentRequest() == null) {
          throw new BadRequestException(
              "Cash payment request is required when isCashPayment is true");
        }
        response =
            shippingService.processShipmentsAfterPaymentApproval(
                request.getCashPaymentRequest().getPurchaseOrderId(),
                request.getCashPaymentRequest());
      } else {
        if (request.getOnlinePaymentRequest() == null) {
          throw new BadRequestException(
              "Online payment request is required when isCashPayment is false");
        }
        response =
            shippingService.processShipmentsAfterPaymentApproval(
                request.getOnlinePaymentRequest().getPurchaseOrderId(),
                request.getOnlinePaymentRequest());
      }

      if (response.isSuccess()) {
        return ResponseEntity.ok(response);
      } else {
        return ResponseEntity.badRequest().body(response);
      }
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (NotFoundException nfe) {
      return notFound(logger, nfe);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Verifies a Razorpay payment for a follow-up payment (order already approved/partially paid).
   * This endpoint allows payments for APPROVED or APPROVED_WITH_PARTIAL_PAYMENT orders. Does NOT
   * trigger shipment processing (shipments already created).
   *
   * @param request The verification request containing payment details
   * @return ResponseEntity containing PaymentVerificationResponseModel
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION
          + "')")
  @PostMapping("/verifyPaymentFollowUp")
  public ResponseEntity<?> verifyPaymentFollowUp(@RequestBody RazorpayVerifyRequestModel request) {
    try {
      PaymentVerificationResponseModel response = paymentService.verifyPaymentFollowUp(request);
      if (response.isSuccess()) {
        return ResponseEntity.ok(response);
      } else {
        return ResponseEntity.badRequest().body(response);
      }
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (NotFoundException nfe) {
      return notFound(logger, nfe);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Records a cash/manual payment for a follow-up payment (order already approved/partially paid).
   * This endpoint allows payments for APPROVED or APPROVED_WITH_PARTIAL_PAYMENT orders. Does NOT
   * trigger shipment processing (shipments already created).
   *
   * @param request The cash payment request containing payment details
   * @return ResponseEntity containing PaymentVerificationResponseModel
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('"
          + Authorizations.UPDATE_PURCHASE_ORDERS_PERMISSION
          + "')")
  @PostMapping("/recordCashPaymentFollowUp")
  public ResponseEntity<?> recordCashPaymentFollowUp(@RequestBody CashPaymentRequestModel request) {
    try {
      PaymentVerificationResponseModel response = paymentService.recordCashPaymentFollowUp(request);
      if (response.isSuccess()) {
        return ResponseEntity.ok(response);
      } else {
        return ResponseEntity.badRequest().body(response);
      }
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (NotFoundException nfe) {
      return notFound(logger, nfe);
    } catch (UnauthorizedException uae) {
      return unauthorized(logger, uae);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }

  /**
   * Generates and downloads a PDF receipt for a payment.
   *
   * @param paymentId The ID of the payment to generate receipt for
   * @return ResponseEntity containing the PDF file
   */
  @PreAuthorize(
      "@customAuthorization.hasAuthority('" + Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION + "')")
  @GetMapping("/downloadPaymentReceipt/{paymentId}")
  public ResponseEntity<?> downloadPaymentReceipt(@PathVariable Long paymentId) {
    try {
      byte[] pdfBytes = paymentService.generatePaymentReceiptPdf(paymentId);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_PDF);
      headers.setContentDispositionFormData("attachment", "payment_receipt_" + paymentId + ".pdf");
      headers.setContentLength(pdfBytes.length);

      return ResponseEntity.ok().headers(headers).body(pdfBytes);
    } catch (BadRequestException bre) {
      return badRequest(logger, bre);
    } catch (NotFoundException nfe) {
      return notFound(logger, nfe);
    } catch (TemplateException | IOException | DocumentException e) {
      return internalServerError(logger, e);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }
}
