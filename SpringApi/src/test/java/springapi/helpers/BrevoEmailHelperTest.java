package springapi.helpers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sendgrid.helpers.mail.objects.Attachments;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import springapi.ErrorMessages;
import springapi.exceptions.BadRequestException;
import springapi.models.requestmodels.SendEmailRequest;

@DisplayName("BrevoEmailHelper Tests")
class BrevoEmailHelperTest {

  // Total Tests: 8

  /**
   * Purpose: Verify sendEmail returns true on successful Brevo response and uses smtp endpoint.
   * Expected Result: True return value. Assertions: Return value and endpoint path.
   */
  @Test
  @DisplayName("sendEmail - Success Returns True - Success")
  void sendEmail_s01_successReturnsTrue_success() throws Exception {
    // Arrange
    BrevoEmailHelper helper = new BrevoEmailHelper("from@example.com", "Sender", "brevo-key");
    SendEmailRequest request = buildRequest(true, true, true);

    HttpClient client = mock(HttpClient.class);
    HttpResponse<String> response = new FixedStringHttpResponse(202, "{\"messageId\":\"id-1\"}");

    ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);

    try (MockedStatic<HttpClient> httpClientMock = mockStatic(HttpClient.class)) {
      httpClientMock.when(HttpClient::newHttpClient).thenReturn(client);
      when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
          .thenReturn(response);

      // Act
      boolean sent = helper.sendEmail(request);

      // Assert
      assertTrue(sent);
      verify(client).send(requestCaptor.capture(), any(HttpResponse.BodyHandler.class));
      HttpRequest outboundRequest = requestCaptor.getValue();
      assertTrue(outboundRequest.uri().toString().endsWith("/smtp/email"));
      assertEquals("POST", outboundRequest.method());
    }
  }

  /**
   * Purpose: Verify sendEmail throws BadRequestException for non-2xx Brevo responses. Expected
   * Result: Exception with ER001 and response body. Assertions: Exception message equality.
   */
  @Test
  @DisplayName("sendEmail - Non2xx Throws BadRequest - Success")
  void sendEmail_s02_non2xxThrowsBadRequest_success() throws Exception {
    // Arrange
    BrevoEmailHelper helper = new BrevoEmailHelper("from@example.com", "Sender", "brevo-key");
    SendEmailRequest request = buildRequest(false, false, false);

    HttpClient client = mock(HttpClient.class);
    HttpResponse<String> response = new FixedStringHttpResponse(400, "brevo-failed");

    try (MockedStatic<HttpClient> httpClientMock = mockStatic(HttpClient.class)) {
      httpClientMock.when(HttpClient::newHttpClient).thenReturn(client);
      when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
          .thenReturn(response);

      // Act
      BadRequestException exception =
          org.junit.jupiter.api.Assertions.assertThrows(
              BadRequestException.class, () -> helper.sendEmail(request));

      // Assert
      assertEquals(
          ErrorMessages.EmailErrorMessages.ER001 + ": brevo-failed", exception.getMessage());
    }
  }

  /**
   * Purpose: Verify sendEmail wraps client exceptions into BadRequestException. Expected Result:
   * Exception with ER001 prefix. Assertions: Exception message equality.
   */
  @Test
  @DisplayName("sendEmail - Exception Throws BadRequest - Success")
  void sendEmail_s03_exceptionThrowsBadRequest_success() throws Exception {
    // Arrange
    BrevoEmailHelper helper = new BrevoEmailHelper("from@example.com", "Sender", "brevo-key");
    SendEmailRequest request = buildRequest(false, false, false);

    HttpClient client = mock(HttpClient.class);

    try (MockedStatic<HttpClient> httpClientMock = mockStatic(HttpClient.class)) {
      httpClientMock.when(HttpClient::newHttpClient).thenReturn(client);
      when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
          .thenThrow(new IOException("io-failure"));

      // Act
      BadRequestException exception =
          org.junit.jupiter.api.Assertions.assertThrows(
              BadRequestException.class, () -> helper.sendEmail(request));

      // Assert
      assertEquals(ErrorMessages.EmailErrorMessages.ER001 + ": io-failure", exception.getMessage());
    }
  }

  /**
   * Purpose: Verify generateBatchId returns a valid UUID string. Expected Result: UUID value
   * produced. Assertions: UUID parsing and non-empty check.
   */
  @Test
  @DisplayName("generateBatchId - Returns UUID - Success")
  void generateBatchId_s04_returnsUuid_success() {
    // Arrange
    BrevoEmailHelper helper = new BrevoEmailHelper("from@example.com", "Sender", "brevo-key");

    // Act
    String batchId = helper.generateBatchId();

    // Assert
    assertTrue(batchId != null && !batchId.isBlank());
    assertEquals(batchId, UUID.fromString(batchId).toString());
  }

  /**
   * Purpose: Verify cancelEmail returns normally for 2xx Brevo response. Expected Result: No
   * exception. Assertions: No exception thrown.
   */
  @Test
  @DisplayName("cancelEmail - Success Returns Without Exception - Success")
  void cancelEmail_s05_successReturnsWithoutException_success() throws Exception {
    // Arrange
    BrevoEmailHelper helper = new BrevoEmailHelper("from@example.com", "Sender", "brevo-key");

    HttpClient client = mock(HttpClient.class);
    HttpResponse<String> response = new FixedStringHttpResponse(204, "");

    try (MockedStatic<HttpClient> httpClientMock = mockStatic(HttpClient.class)) {
      httpClientMock.when(HttpClient::newHttpClient).thenReturn(client);
      when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
          .thenReturn(response);

      // Act + Assert
      assertDoesNotThrow(() -> helper.cancelEmail("batch-1"));
    }
  }

  /**
   * Purpose: Verify cancelEmail throws BadRequestException for non-2xx responses. Expected Result:
   * Exception with ER003 and response body. Assertions: Exception message equality.
   */
  @Test
  @DisplayName("cancelEmail - Non2xx Throws BadRequest - Success")
  void cancelEmail_s06_non2xxThrowsBadRequest_success() throws Exception {
    // Arrange
    BrevoEmailHelper helper = new BrevoEmailHelper("from@example.com", "Sender", "brevo-key");

    HttpClient client = mock(HttpClient.class);
    HttpResponse<String> response = new FixedStringHttpResponse(409, "cancel-error");

    try (MockedStatic<HttpClient> httpClientMock = mockStatic(HttpClient.class)) {
      httpClientMock.when(HttpClient::newHttpClient).thenReturn(client);
      when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
          .thenReturn(response);

      // Act
      BadRequestException exception =
          org.junit.jupiter.api.Assertions.assertThrows(
              BadRequestException.class, () -> helper.cancelEmail("batch-1"));

      // Assert
      assertEquals(
          ErrorMessages.EmailErrorMessages.ER003 + ": cancel-error", exception.getMessage());
    }
  }

  /**
   * Purpose: Verify cancelEmail wraps client exceptions into BadRequestException. Expected Result:
   * Exception with ER003 prefix. Assertions: Exception message equality.
   */
  @Test
  @DisplayName("cancelEmail - Exception Throws BadRequest - Success")
  void cancelEmail_s07_exceptionThrowsBadRequest_success() throws Exception {
    // Arrange
    BrevoEmailHelper helper = new BrevoEmailHelper("from@example.com", "Sender", "brevo-key");

    HttpClient client = mock(HttpClient.class);

    try (MockedStatic<HttpClient> httpClientMock = mockStatic(HttpClient.class)) {
      httpClientMock.when(HttpClient::newHttpClient).thenReturn(client);
      when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
          .thenThrow(new IOException("cancel-io"));

      // Act
      BadRequestException exception =
          org.junit.jupiter.api.Assertions.assertThrows(
              BadRequestException.class, () -> helper.cancelEmail("batch-1"));

      // Assert
      assertEquals(ErrorMessages.EmailErrorMessages.ER003 + ": cancel-io", exception.getMessage());
    }
  }

  /**
   * Purpose: Verify meetingRequestString supports cancellation branch formatting. Expected Result:
   * Calendar content includes METHOD:CANCEL marker. Assertions: ICS text markers.
   */
  @Test
  @DisplayName("meetingRequestString - Cancel Method Branch - Success")
  void meetingRequestString_s08_cancelMethodBranch_success() throws Exception {
    // Arrange
    BrevoEmailHelper helper = new BrevoEmailHelper("from@example.com", "Sender", "brevo-key");
    Method method =
        BrevoEmailHelper.class.getDeclaredMethod(
            "meetingRequestString",
            String.class,
            java.util.Collection.class,
            String.class,
            String.class,
            LocalDateTime.class,
            LocalDateTime.class,
            boolean.class);
    method.setAccessible(true);

    // Act
    String calendar =
        (String)
            method.invoke(
                helper,
                "from@example.com",
                List.of("to@example.com"),
                "Subject",
                "Line A\nLine B",
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 1, 11, 0),
                true);

    // Assert
    assertTrue(calendar.contains("METHOD:CANCEL"));
    assertTrue(calendar.contains("X-ALT-DESC;FMTTYPE=text/html:Line A<br>Line B"));
  }

  private static SendEmailRequest buildRequest(
      boolean includeInvite, boolean includeAttachment, boolean includeSchedule) {
    SendEmailRequest request = new SendEmailRequest();
    request.setToAddress(List.of("to@example.com"));
    request.setSubject("Brevo Subject");
    request.setPlainTextContent("Brevo plain");
    request.setHtmlContent("<p>Brevo html</p>");

    if (includeAttachment) {
      Attachments attachments = new Attachments();
      attachments.setFilename("attachment.txt");
      attachments.setContent("YQ==");
      request.setAttachments(List.of(attachments));
    }

    if (includeSchedule) {
      request.setSendAt(LocalDateTime.of(2026, 1, 3, 9, 0));
      request.setBatchId("batch-xyz");
    }

    request.setInvite(includeInvite);
    if (includeInvite) {
      request.setMeetingDate(LocalDateTime.of(2026, 1, 3, 10, 0));
      request.setAttendees(List.of("attendee@example.com"));
    }

    return request;
  }

  private static final class FixedStringHttpResponse implements HttpResponse<String> {
    private final int statusCode;
    private final String body;

    FixedStringHttpResponse(int statusCode, String body) {
      this.statusCode = statusCode;
      this.body = body;
    }

    @Override
    public int statusCode() {
      return statusCode;
    }

    @Override
    public String body() {
      return body;
    }

    @Override
    public HttpRequest request() {
      return null;
    }

    @Override
    public java.util.Optional<HttpResponse<String>> previousResponse() {
      return java.util.Optional.empty();
    }

    @Override
    public java.net.http.HttpHeaders headers() {
      return java.net.http.HttpHeaders.of(java.util.Map.of(), (a, b) -> true);
    }

    @Override
    public java.net.URI uri() {
      return null;
    }

    @Override
    public java.net.http.HttpClient.Version version() {
      return java.net.http.HttpClient.Version.HTTP_1_1;
    }

    @Override
    public java.util.Optional<javax.net.ssl.SSLSession> sslSession() {
      return java.util.Optional.empty();
    }
  }
}
