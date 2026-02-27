package com.example.SpringApi.ServiceTests.Payment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.SpringApi.Controllers.PaymentController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Helpers.HTMLHelper;
import com.example.SpringApi.Helpers.PDFHelper;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.Payment;
import com.example.SpringApi.Models.DatabaseModels.PurchaseOrder;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

/** Tests for PaymentService.generatePaymentReceiptPDF(). */
@DisplayName("GeneratePaymentReceiptPDF Tests")
class GeneratePaymentReceiptPDFTest extends PaymentServiceTestBase {

  // Total Tests: 12
  // ========================================
  // SUCCESS TESTS
  // ========================================

  /**
   * Purpose: Verify payment repository interaction for receipt generation. Expected Result:
   * paymentRepository.findById is called once. Assertions: verify interaction count.
   */
  @Test
  @DisplayName("generatePaymentReceiptPDF - Verify Payment Repository Interaction - Success")
  void generatePaymentReceiptPDF_s01_verifyPaymentRepositoryInteraction_success() {
    // Arrange
    stubPaymentRepositoryFindById(TEST_PAYMENT_ID, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> paymentService.generatePaymentReceiptPDF(TEST_PAYMENT_ID));

    // Assert
    assertEquals(ErrorMessages.PaymentErrorMessages.NOT_FOUND, ex.getMessage());
    verify(paymentRepository, times(1)).findById(TEST_PAYMENT_ID);
  }

  /**
   * Purpose: Verify controller delegates receipt generation endpoint to service. Expected Result:
   * HTTP 200 OK with PDF byte[] response. Assertions: Service interaction and response status/body.
   */
  @Test
  @DisplayName("generatePaymentReceiptPDF - Controller Delegates To Service - Success")
  void generatePaymentReceiptPDF_s02_controllerDelegatesToService_success() throws Exception {
    // Arrange
    byte[] bytes = new byte[] {1, 2, 3};
    stubPaymentServiceGeneratePaymentReceiptPDF(bytes);

    // Act
    ResponseEntity<?> response = paymentControllerWithMock.downloadPaymentReceipt(TEST_PAYMENT_ID);

    // Assert
    verify(paymentServiceMock, times(1)).generatePaymentReceiptPDF(TEST_PAYMENT_ID);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertArrayEquals(bytes, (byte[]) response.getBody());
  }

  // ========================================
  // FAILURE TESTS
  // ========================================

  /**
   * Purpose: Verify missing payment throws NotFoundException in receipt generation. Expected
   * Result: NotFoundException with payment not found message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("generatePaymentReceiptPDF - Payment Not Found - Failure")
  void generatePaymentReceiptPDF_f01_paymentNotFound_failure() {
    // Arrange
    stubPaymentRepositoryFindById(TEST_PAYMENT_ID, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> paymentService.generatePaymentReceiptPDF(TEST_PAYMENT_ID));

    // Assert
    assertEquals(ErrorMessages.PaymentErrorMessages.NOT_FOUND, ex.getMessage());
  }

  /**
   * Purpose: Verify negative payment id throws NotFoundException. Expected Result:
   * NotFoundException with payment not found message. Assertions: Exception type and exact message.
   */
  @Test
  @DisplayName("generatePaymentReceiptPDF - Negative Payment Id - Failure")
  void generatePaymentReceiptPDF_f02_negativePaymentId_failure() {
    // Arrange
    stubPaymentRepositoryFindById(-1L, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> paymentService.generatePaymentReceiptPDF(-1L));

    // Assert
    assertEquals(ErrorMessages.PaymentErrorMessages.NOT_FOUND, ex.getMessage());
  }

  /**
   * Purpose: Verify zero payment id throws NotFoundException. Expected Result: NotFoundException
   * with payment not found message. Assertions: Exception type and exact message.
   */
  @Test
  @DisplayName("generatePaymentReceiptPDF - Zero Payment Id - Failure")
  void generatePaymentReceiptPDF_f03_zeroPaymentId_failure() {
    // Arrange
    stubPaymentRepositoryFindById(0L, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> paymentService.generatePaymentReceiptPDF(0L));

    // Assert
    assertEquals(ErrorMessages.PaymentErrorMessages.NOT_FOUND, ex.getMessage());
  }

  /**
   * Purpose: Verify access denied when payment belongs to a different client. Expected Result:
   * BadRequestException with payment access denied message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("generatePaymentReceiptPDF - Client Access Denied - Failure")
  void generatePaymentReceiptPDF_f04_clientAccessDenied_failure() {
    // Arrange
    testPayment.setClientId(TEST_CLIENT_ID + 1);
    stubPaymentRepositoryFindById(TEST_PAYMENT_ID, Optional.of(testPayment));

    // Act
    BadRequestException ex =
        assertThrows(
            BadRequestException.class,
            () -> paymentService.generatePaymentReceiptPDF(TEST_PAYMENT_ID));

    // Assert
    assertEquals(ErrorMessages.PaymentErrorMessages.ACCESS_DENIED, ex.getMessage());
  }

  /**
   * Purpose: Verify receipt generation validates purchase order existence. Expected Result:
   * NotFoundException with purchase order invalid id message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("generatePaymentReceiptPDF - Purchase Order Not Found - Failure")
  void generatePaymentReceiptPDF_f05_purchaseOrderNotFound_failure() {
    // Arrange
    testPayment.setClientId(TEST_CLIENT_ID);
    testPayment.setEntityId(TEST_PO_ID);
    stubPaymentRepositoryFindById(TEST_PAYMENT_ID, Optional.of(testPayment));
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> paymentService.generatePaymentReceiptPDF(TEST_PAYMENT_ID));

    // Assert
    assertEquals(ErrorMessages.PurchaseOrderErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify receipt generation validates client existence. Expected Result:
   * NotFoundException with client invalid id message. Assertions: Exception type and exact message.
   */
  @Test
  @DisplayName("generatePaymentReceiptPDF - Client Not Found - Failure")
  void generatePaymentReceiptPDF_f06_clientNotFound_failure() {
    // Arrange
    testPayment.setClientId(TEST_CLIENT_ID);
    testPayment.setEntityId(TEST_PO_ID);
    stubPaymentRepositoryFindById(TEST_PAYMENT_ID, Optional.of(testPayment));
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(testPurchaseOrder));
    stubClientRepositoryFindByIdDefaultClient(Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> paymentService.generatePaymentReceiptPDF(TEST_PAYMENT_ID));

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify receipt generation builds HTML and converts to PDF for valid payment data.
   * Expected Result: Generated byte[] returned and helper calls executed once. Assertions: Returned
   * PDF bytes and helper invocations.
   */
  @Test
  @DisplayName("generatePaymentReceiptPDF - Generates Pdf Bytes - Success")
  void generatePaymentReceiptPDF_s03_generatesPdfBytes_success() throws Exception {
    // Arrange
    byte[] expectedPdf = new byte[] {7, 8, 9};
    testPayment.setClientId(TEST_CLIENT_ID);
    testPayment.setEntityId(TEST_PO_ID);
    testPayment.setPaymentId(TEST_PAYMENT_ID);
    testPayment.setCapturedAt(null);
    testPayment.setPaymentDate(null);
    testPayment.setOrderCreatedAt(null);
    testPayment.setPaymentStatus(Payment.PaymentStatus.CAPTURED.getValue());
    testPayment.setPaymentMethod(null);
    testPayment.setPaymentGateway(null);
    testPayment.setCurrency(null);
    testPayment.setAmountPaid(null);
    testPayment.setAmountPaidPaise(null);
    Client localClient = createTestClient();
    localClient.setName("Coverage Client");
    localClient.setClientId(TEST_CLIENT_ID);
    localClient.setWebsite(null);
    localClient.setSupportEmail(null);
    localClient.setLogoUrl(null);
    PurchaseOrder localPurchaseOrder = createTestPurchaseOrder(TEST_PO_ID);

    stubPaymentRepositoryFindById(TEST_PAYMENT_ID, Optional.of(testPayment));
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(localPurchaseOrder));
    stubClientRepositoryFindByIdDefaultClient(Optional.of(localClient));

    try (MockedStatic<HTMLHelper> htmlHelperMock =
            org.mockito.Mockito.mockStatic(HTMLHelper.class);
        MockedStatic<PDFHelper> pdfHelperMock = org.mockito.Mockito.mockStatic(PDFHelper.class)) {
      htmlHelperMock
          .when(() -> HTMLHelper.replaceBrTags(org.mockito.ArgumentMatchers.anyString()))
          .thenAnswer(invocation -> invocation.getArgument(0));
      pdfHelperMock
          .when(() -> PDFHelper.convertHtmlToPdf(org.mockito.ArgumentMatchers.anyString()))
          .thenReturn(expectedPdf);

      // Act
      byte[] result = paymentService.generatePaymentReceiptPDF(TEST_PAYMENT_ID);

      // Assert
      assertArrayEquals(expectedPdf, result);
      htmlHelperMock.verify(
          () -> HTMLHelper.replaceBrTags(org.mockito.ArgumentMatchers.anyString()), times(1));
      pdfHelperMock.verify(
          () -> PDFHelper.convertHtmlToPdf(org.mockito.ArgumentMatchers.anyString()), times(1));
      verify(userLogService, times(1))
          .logData(
              1L,
              "Payment receipt PDF generated for Payment ID: " + TEST_PAYMENT_ID,
              "Payment/generatePaymentReceiptPDF");
    }
  }

  /**
   * Purpose: Verify receipt HTML population handles captured timestamp and optional payment
   * metadata fields. Expected Result: PDF bytes are generated successfully with non-null payment
   * metadata. Assertions: Returned bytes and helper invocation counts.
   */
  @Test
  @DisplayName("generatePaymentReceiptPDF - Captured Timestamp And Optional Fields - Success")
  void generatePaymentReceiptPDF_s04_capturedTimestampAndOptionalFields_success() throws Exception {
    // Arrange
    byte[] expectedPdf = new byte[] {4, 5, 6};
    testPayment.setClientId(TEST_CLIENT_ID);
    testPayment.setEntityId(TEST_PO_ID);
    testPayment.setPaymentId(TEST_PAYMENT_ID);
    testPayment.setCapturedAt(LocalDateTime.now().minusHours(3));
    testPayment.setPaymentDate(null);
    testPayment.setOrderCreatedAt(LocalDateTime.now().minusDays(2));
    testPayment.setPaymentStatus(Payment.PaymentStatus.PARTIALLY_REFUNDED.getValue());
    testPayment.setPaymentMethod(Payment.PaymentMethod.UPI.getValue());
    testPayment.setPaymentGateway(Payment.PaymentGateway.RAZORPAY.getValue());
    testPayment.setCurrency("INR");
    testPayment.setAmountPaid(new BigDecimal("123.45"));
    testPayment.setAmountPaidPaise(null);
    testPayment.setRazorpayFee(new BigDecimal("2.50"));
    testPayment.setRazorpayTax(new BigDecimal("0.50"));
    testPayment.setAmountRefunded(new BigDecimal("10.00"));

    Client localClient = createTestClient();
    localClient.setClientId(TEST_CLIENT_ID);
    localClient.setWebsite("https://example.com");
    localClient.setSupportEmail("support@example.com");
    localClient.setLogoUrl("https://example.com/logo.png");
    PurchaseOrder localPurchaseOrder = createTestPurchaseOrder(TEST_PO_ID);

    stubPaymentRepositoryFindById(TEST_PAYMENT_ID, Optional.of(testPayment));
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(localPurchaseOrder));
    stubClientRepositoryFindByIdDefaultClient(Optional.of(localClient));

    try (MockedStatic<HTMLHelper> htmlHelperMock =
            org.mockito.Mockito.mockStatic(HTMLHelper.class);
        MockedStatic<PDFHelper> pdfHelperMock = org.mockito.Mockito.mockStatic(PDFHelper.class)) {
      htmlHelperMock
          .when(() -> HTMLHelper.replaceBrTags(org.mockito.ArgumentMatchers.anyString()))
          .thenAnswer(invocation -> invocation.getArgument(0));
      pdfHelperMock
          .when(() -> PDFHelper.convertHtmlToPdf(org.mockito.ArgumentMatchers.anyString()))
          .thenReturn(expectedPdf);

      // Act
      byte[] result = paymentService.generatePaymentReceiptPDF(TEST_PAYMENT_ID);

      // Assert
      assertArrayEquals(expectedPdf, result);
      htmlHelperMock.verify(
          () -> HTMLHelper.replaceBrTags(org.mockito.ArgumentMatchers.anyString()), times(1));
      pdfHelperMock.verify(
          () -> PDFHelper.convertHtmlToPdf(org.mockito.ArgumentMatchers.anyString()), times(1));
    }
  }

  /**
   * Purpose: Verify receipt HTML population falls back to paymentDate and amountPaidPaise when
   * amountPaid is absent. Expected Result: PDF bytes are generated successfully using alternate
   * date/amount branches. Assertions: Returned bytes and helper invocation counts.
   */
  @Test
  @DisplayName("generatePaymentReceiptPDF - Payment Date And Amount Paise Fallback - Success")
  void generatePaymentReceiptPDF_s05_paymentDateAndAmountPaiseFallback_success() throws Exception {
    // Arrange
    byte[] expectedPdf = new byte[] {2, 4, 6};
    testPayment.setClientId(TEST_CLIENT_ID);
    testPayment.setEntityId(TEST_PO_ID);
    testPayment.setPaymentId(TEST_PAYMENT_ID);
    testPayment.setCapturedAt(null);
    testPayment.setPaymentDate(LocalDateTime.now().minusHours(1));
    testPayment.setOrderCreatedAt(LocalDateTime.now().minusDays(1));
    testPayment.setPaymentStatus(Payment.PaymentStatus.CAPTURED.getValue());
    testPayment.setPaymentMethod(Payment.PaymentMethod.CASH.getValue());
    testPayment.setPaymentGateway(Payment.PaymentGateway.MANUAL.getValue());
    testPayment.setCurrency(null);
    testPayment.setAmountPaid(null);
    testPayment.setAmountPaidPaise(4321L);
    testPayment.setRazorpayFee(BigDecimal.ZERO);
    testPayment.setRazorpayTax(BigDecimal.ZERO);
    testPayment.setAmountRefunded(BigDecimal.ZERO);

    Client localClient = createTestClient();
    localClient.setClientId(TEST_CLIENT_ID);
    PurchaseOrder localPurchaseOrder = createTestPurchaseOrder(TEST_PO_ID);

    stubPaymentRepositoryFindById(TEST_PAYMENT_ID, Optional.of(testPayment));
    stubPurchaseOrderRepositoryFindById(TEST_PO_ID, Optional.of(localPurchaseOrder));
    stubClientRepositoryFindByIdDefaultClient(Optional.of(localClient));

    try (MockedStatic<HTMLHelper> htmlHelperMock =
            org.mockito.Mockito.mockStatic(HTMLHelper.class);
        MockedStatic<PDFHelper> pdfHelperMock = org.mockito.Mockito.mockStatic(PDFHelper.class)) {
      htmlHelperMock
          .when(() -> HTMLHelper.replaceBrTags(org.mockito.ArgumentMatchers.anyString()))
          .thenAnswer(invocation -> invocation.getArgument(0));
      pdfHelperMock
          .when(() -> PDFHelper.convertHtmlToPdf(org.mockito.ArgumentMatchers.anyString()))
          .thenReturn(expectedPdf);

      // Act
      byte[] result = paymentService.generatePaymentReceiptPDF(TEST_PAYMENT_ID);

      // Assert
      assertArrayEquals(expectedPdf, result);
      htmlHelperMock.verify(
          () -> HTMLHelper.replaceBrTags(org.mockito.ArgumentMatchers.anyString()), times(1));
      pdfHelperMock.verify(
          () -> PDFHelper.convertHtmlToPdf(org.mockito.ArgumentMatchers.anyString()), times(1));
    }
  }

  // ========================================
  // PERMISSION TESTS
  // ========================================

  /**
   * Purpose: Verify receipt download endpoint requires view purchase orders permission. Expected
   * Result: @PreAuthorize is present with VIEW_PURCHASE_ORDERS_PERMISSION. Assertions: annotation
   * not null and contains permission constant.
   */
  @Test
  @DisplayName("generatePaymentReceiptPDF - Controller Permission Forbidden")
  void generatePaymentReceiptPDF_controller_permission_forbidden() throws Exception {
    // Arrange
    Method method = PaymentController.class.getMethod("downloadPaymentReceipt", Long.class);
    stubPaymentServiceGeneratePaymentReceiptPDFThrowsUnauthorized();

    // Act
    PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
    ResponseEntity<?> response = paymentControllerWithMock.downloadPaymentReceipt(TEST_PAYMENT_ID);

    // Assert
    assertNotNull(annotation, "@PreAuthorize annotation should be present");
    assertTrue(annotation.value().contains(Authorizations.VIEW_PURCHASE_ORDERS_PERMISSION));
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }
}
