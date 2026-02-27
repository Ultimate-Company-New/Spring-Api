package com.example.SpringApi.ServiceTests.Payment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.SpringApi.Controllers.PaymentController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

/** Tests for PaymentService.getRazorpayKeyId(). */
@DisplayName("GetRazorpayKeyId Tests")
class GetRazorpayKeyIdTest extends PaymentServiceTestBase {

  // Total Tests: 8
  // ========================================
  // SUCCESS TESTS
  // ========================================

  /**
   * Purpose: Verify controller delegates getRazorpayKeyId to service. Expected Result: HTTP 200 OK.
   * Assertions: Service interaction and response status.
   */
  @Test
  @DisplayName("getRazorpayKeyId - Controller Delegates To Service - Success")
  void getRazorpayKeyId_controllerDelegatesToService_success() {
    // Arrange
    stubPaymentServiceGetRazorpayKeyId("rzp_test_public_key");

    // Act
    ResponseEntity<?> response = paymentControllerWithMock.getRazorpayKeyId();

    // Assert
    verify(paymentServiceMock, times(1)).getRazorpayKeyId();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("rzp_test_public_key", response.getBody());
  }

  /**
   * Purpose: Verify client repository is queried during key-id lookup flow. Expected Result:
   * clientRepository.findById is called once. Assertions: verify interaction count.
   */
  @Test
  @DisplayName("getRazorpayKeyId - Verify Client Repository Interaction - Success")
  void getRazorpayKeyId_verifyClientRepositoryInteraction_success() {
    // Arrange
    stubClientRepositoryFindByIdNull(Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> paymentService.getRazorpayKeyId());

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
    verify(clientRepository, times(1)).findById(any());
  }

  /**
   * Purpose: Verify service returns Razorpay key id when client credentials are configured.
   * Expected Result: Public Razorpay key is returned. Assertions: Returned key matches configured
   * client key.
   */
  @Test
  @DisplayName("getRazorpayKeyId - Service Returns Client Key - Success")
  void getRazorpayKeyId_serviceReturnsClientKey_success() {
    // Arrange
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));

    // Act
    String result = paymentService.getRazorpayKeyId();

    // Assert
    assertEquals(testClient.getRazorpayApiKey(), result);
  }

  // ========================================
  // FAILURE TESTS
  // ========================================

  /**
   * Purpose: Verify key-id lookup fails when client context is unavailable. Expected Result:
   * NotFoundException with Client InvalidId. Assertions: Exception type and exact message.
   */
  @Test
  @DisplayName("getRazorpayKeyId - Client Not Found - Failure")
  void getRazorpayKeyId_clientNotFound_throwsNotFoundException() {
    // Arrange
    stubClientRepositoryFindByIdNull(Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> paymentService.getRazorpayKeyId());

    // Assert
    assertEquals(ErrorMessages.ClientErrorMessages.INVALID_ID, ex.getMessage());
  }

  /**
   * Purpose: Verify service validates client Razorpay API key presence. Expected Result:
   * BadRequestException with API key not configured message. Assertions: Exception type and exact
   * message.
   */
  @Test
  @DisplayName("getRazorpayKeyId - Razorpay Api Key Missing - Failure")
  void getRazorpayKeyId_razorpayApiKeyMissing_failure() {
    // Arrange
    testClient.setRazorpayApiKey("  ");
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));

    // Act
    BadRequestException ex =
        assertThrows(BadRequestException.class, () -> paymentService.getRazorpayKeyId());

    // Assert
    assertEquals(
        ErrorMessages.PaymentErrorMessages.RAZORPAY_API_KEY_NOT_CONFIGURED, ex.getMessage());
  }

  /**
   * Purpose: Verify service validates client Razorpay API secret presence for credential integrity.
   * Expected Result: BadRequestException with API secret not configured message. Assertions:
   * Exception type and exact message.
   */
  @Test
  @DisplayName("getRazorpayKeyId - Razorpay Api Secret Missing - Failure")
  void getRazorpayKeyId_razorpayApiSecretMissing_failure() {
    // Arrange
    testClient.setRazorpayApiSecret("  ");
    stubClientRepositoryFindByIdDefaultClient(Optional.of(testClient));

    // Act
    BadRequestException ex =
        assertThrows(BadRequestException.class, () -> paymentService.getRazorpayKeyId());

    // Assert
    assertEquals(
        ErrorMessages.PaymentErrorMessages.RAZORPAY_API_SECRET_NOT_CONFIGURED, ex.getMessage());
  }

  // ========================================
  // PERMISSION TESTS
  // ========================================

  /**
   * Purpose: Verify endpoint is intentionally open (no @PreAuthorize annotation). Expected
   * Result: @PreAuthorize is absent. Assertions: annotation is null.
   */
  @Test
  @DisplayName("getRazorpayKeyId - Controller Permission Open Endpoint")
  void getRazorpayKeyId_controller_permission_openEndpoint() throws NoSuchMethodException {
    // Arrange
    Method method = PaymentController.class.getMethod("getRazorpayKeyId");
    stubPaymentServiceGetRazorpayKeyId("rzp_test_open");

    // Act
    PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);
    ResponseEntity<?> response = paymentControllerWithMock.getRazorpayKeyId();

    // Assert
    assertNull(annotation);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  /**
   * Purpose: Verify controller handles downstream service exception for key-id endpoint. Expected
   * Result: HTTP 500 INTERNAL_SERVER_ERROR. Assertions: Response status is INTERNAL_SERVER_ERROR.
   */
  @Test
  @DisplayName("getRazorpayKeyId - Service Exception - Failure")
  void getRazorpayKeyId_serviceException_failure() {
    // Arrange
    stubPaymentServiceGetRazorpayKeyIdThrows(
        new RuntimeException(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR));

    // Act
    ResponseEntity<?> response = paymentControllerWithMock.getRazorpayKeyId();

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }
}
