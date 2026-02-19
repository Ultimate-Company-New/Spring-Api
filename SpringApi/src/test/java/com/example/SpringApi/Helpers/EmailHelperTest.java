package com.example.SpringApi.Helpers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.RequestModels.SendEmailRequest;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.objects.Attachments;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

@DisplayName("EmailHelper Tests")
class EmailHelperTest {

  // Total Tests: 10

  /**
   * Purpose: Verify sendEmail builds mail content with invite, attachment, scheduling, and returns
   * true for 2xx status. Expected Result: True return with proper SendGrid endpoint/body usage.
   * Assertions: Return value, endpoint, and body markers.
   */
  @Test
  @DisplayName("sendEmail - Success With Invite And Attachments - Success")
  void sendEmail_s01_successWithInviteAndAttachments_success() throws Exception {
    // Arrange
    EmailHelper helper = new EmailHelper("from@example.com", "Sender Name", "sendgrid-key");
    SendEmailRequest request = buildRequest(true, true, true);

    ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);

    try (MockedConstruction<SendGrid> mocked =
        mockConstruction(
            SendGrid.class,
            (mock, context) -> {
              com.sendgrid.Response response = mock(com.sendgrid.Response.class);
              when(response.getStatusCode()).thenReturn(202);
              when(mock.api(any(Request.class))).thenReturn(response);
            })) {
      // Act
      boolean sent = helper.sendEmail(request);

      // Assert
      assertTrue(sent);
      verify(mocked.constructed().getFirst()).api(requestCaptor.capture());
      Request outboundRequest = requestCaptor.getValue();
      assertEquals(com.sendgrid.Method.POST, outboundRequest.getMethod());
      assertEquals("mail/send", outboundRequest.getEndpoint());
      assertTrue(outboundRequest.getBody().contains("invite.ics"));
      assertTrue(outboundRequest.getBody().contains("agenda.txt"));
      assertTrue(outboundRequest.getBody().contains("batch-123"));
    }
  }

  /**
   * Purpose: Verify sendEmail throws BadRequestException when SendGrid returns non-2xx status.
   * Expected Result: Exception with ER001 prefix and response body. Assertions: Exception message
   * equality.
   */
  @Test
  @DisplayName("sendEmail - Non2xx Throws BadRequest - Success")
  void sendEmail_s02_non2xxThrowsBadRequest_success() throws Exception {
    // Arrange
    EmailHelper helper = new EmailHelper("from@example.com", "Sender Name", "sendgrid-key");
    SendEmailRequest request = buildRequest(false, false, false);

    try (MockedConstruction<SendGrid> mocked =
        mockConstruction(
            SendGrid.class,
            (mock, context) -> {
              com.sendgrid.Response response = mock(com.sendgrid.Response.class);
              when(response.getStatusCode()).thenReturn(400);
              when(response.getBody()).thenReturn("bad-request");
              when(mock.api(any(Request.class))).thenReturn(response);
            })) {
      // Act
      BadRequestException exception =
          org.junit.jupiter.api.Assertions.assertThrows(
              BadRequestException.class, () -> helper.sendEmail(request));

      // Assert
      assertEquals(
          ErrorMessages.EmailErrorMessages.ER001 + ": bad-request", exception.getMessage());
      verify(mocked.constructed().getFirst()).api(any(Request.class));
    }
  }

  /**
   * Purpose: Verify sendEmail wraps IOException from SendGrid API as BadRequestException. Expected
   * Result: Exception with ER001 prefix and IO message. Assertions: Exception message equality.
   */
  @Test
  @DisplayName("sendEmail - IOException Throws BadRequest - Success")
  void sendEmail_s03_ioExceptionThrowsBadRequest_success() throws Exception {
    // Arrange
    EmailHelper helper = new EmailHelper("from@example.com", "Sender Name", "sendgrid-key");
    SendEmailRequest request = buildRequest(false, false, false);

    try (MockedConstruction<SendGrid> mocked =
        mockConstruction(
            SendGrid.class,
            (mock, context) ->
                when(mock.api(any(Request.class))).thenThrow(new IOException("network-failure")))) {
      // Act
      BadRequestException exception =
          org.junit.jupiter.api.Assertions.assertThrows(
              BadRequestException.class, () -> helper.sendEmail(request));

      // Assert
      assertEquals(
          ErrorMessages.EmailErrorMessages.ER001 + ": network-failure", exception.getMessage());
      verify(mocked.constructed().getFirst()).api(any(Request.class));
    }
  }

  /**
   * Purpose: Verify generateBatchId parses and returns batch_id for 2xx responses. Expected Result:
   * Parsed batch ID string. Assertions: Batch ID equality.
   */
  @Test
  @DisplayName("generateBatchId - Success Parses BatchId - Success")
  void generateBatchId_s04_successParsesBatchId_success() throws Exception {
    // Arrange
    EmailHelper helper = new EmailHelper("from@example.com", "Sender Name", "sendgrid-key");

    try (MockedConstruction<SendGrid> mocked =
        mockConstruction(
            SendGrid.class,
            (mock, context) -> {
              com.sendgrid.Response response = mock(com.sendgrid.Response.class);
              when(response.getStatusCode()).thenReturn(201);
              when(response.getBody()).thenReturn("{\"batch_id\":\"batch-abc\"}");
              when(mock.api(any(Request.class))).thenReturn(response);
            })) {
      // Act
      String batchId = helper.generateBatchId();

      // Assert
      assertEquals("batch-abc", batchId);
      verify(mocked.constructed().getFirst()).api(any(Request.class));
    }
  }

  /**
   * Purpose: Verify generateBatchId throws BadRequestException on non-2xx status. Expected Result:
   * Exception with ER002 prefix. Assertions: Exception message equality.
   */
  @Test
  @DisplayName("generateBatchId - Non2xx Throws BadRequest - Success")
  void generateBatchId_s05_non2xxThrowsBadRequest_success() throws Exception {
    // Arrange
    EmailHelper helper = new EmailHelper("from@example.com", "Sender Name", "sendgrid-key");

    try (MockedConstruction<SendGrid> mocked =
        mockConstruction(
            SendGrid.class,
            (mock, context) -> {
              com.sendgrid.Response response = mock(com.sendgrid.Response.class);
              when(response.getStatusCode()).thenReturn(500);
              when(response.getBody()).thenReturn("server-error");
              when(mock.api(any(Request.class))).thenReturn(response);
            })) {
      // Act
      BadRequestException exception =
          org.junit.jupiter.api.Assertions.assertThrows(
              BadRequestException.class, helper::generateBatchId);

      // Assert
      assertEquals(
          ErrorMessages.EmailErrorMessages.ER002
              + ": "
              + ErrorMessages.EmailErrorMessages.ER002
              + ": server-error",
          exception.getMessage());
      verify(mocked.constructed().getFirst()).api(any(Request.class));
    }
  }

  /**
   * Purpose: Verify generateBatchId wraps thrown exceptions from API interactions. Expected Result:
   * Exception with ER002 prefix. Assertions: Exception message contains ER002.
   */
  @Test
  @DisplayName("generateBatchId - Exception Throws BadRequest - Success")
  void generateBatchId_s06_exceptionThrowsBadRequest_success() throws Exception {
    // Arrange
    EmailHelper helper = new EmailHelper("from@example.com", "Sender Name", "sendgrid-key");

    try (MockedConstruction<SendGrid> mocked =
        mockConstruction(
            SendGrid.class,
            (mock, context) ->
                when(mock.api(any(Request.class))).thenThrow(new IOException("io-error")))) {
      // Act
      BadRequestException exception =
          org.junit.jupiter.api.Assertions.assertThrows(
              BadRequestException.class, helper::generateBatchId);

      // Assert
      assertEquals(ErrorMessages.EmailErrorMessages.ER002 + ": io-error", exception.getMessage());
      verify(mocked.constructed().getFirst()).api(any(Request.class));
    }
  }

  /**
   * Purpose: Verify cancelEmail returns normally for 2xx API response. Expected Result: No
   * exception. Assertions: No exception thrown.
   */
  @Test
  @DisplayName("cancelEmail - Success Returns Without Exception - Success")
  void cancelEmail_s07_successReturnsWithoutException_success() throws Exception {
    // Arrange
    EmailHelper helper = new EmailHelper("from@example.com", "Sender Name", "sendgrid-key");

    try (MockedConstruction<SendGrid> mocked =
        mockConstruction(
            SendGrid.class,
            (mock, context) -> {
              com.sendgrid.Response response = mock(com.sendgrid.Response.class);
              when(response.getStatusCode()).thenReturn(202);
              when(mock.api(any(Request.class))).thenReturn(response);
            })) {
      // Act + Assert
      assertDoesNotThrow(() -> helper.cancelEmail("batch-id"));
      verify(mocked.constructed().getFirst()).api(any(Request.class));
    }
  }

  /**
   * Purpose: Verify cancelEmail throws BadRequestException for non-2xx status. Expected Result:
   * Exception with ER003 prefix. Assertions: Exception message equality.
   */
  @Test
  @DisplayName("cancelEmail - Non2xx Throws BadRequest - Success")
  void cancelEmail_s08_non2xxThrowsBadRequest_success() throws Exception {
    // Arrange
    EmailHelper helper = new EmailHelper("from@example.com", "Sender Name", "sendgrid-key");

    try (MockedConstruction<SendGrid> mocked =
        mockConstruction(
            SendGrid.class,
            (mock, context) -> {
              com.sendgrid.Response response = mock(com.sendgrid.Response.class);
              when(response.getStatusCode()).thenReturn(400);
              when(response.getBody()).thenReturn("cancel-failed");
              when(mock.api(any(Request.class))).thenReturn(response);
            })) {
      // Act
      BadRequestException exception =
          org.junit.jupiter.api.Assertions.assertThrows(
              BadRequestException.class, () -> helper.cancelEmail("batch-id"));

      // Assert
      assertEquals(
          ErrorMessages.EmailErrorMessages.ER003
              + ": "
              + ErrorMessages.EmailErrorMessages.ER003
              + ": cancel-failed",
          exception.getMessage());
      verify(mocked.constructed().getFirst()).api(any(Request.class));
    }
  }

  /**
   * Purpose: Verify cancelEmail wraps runtime errors into BadRequestException. Expected Result:
   * Exception with ER003 prefix. Assertions: Exception message contains ER003.
   */
  @Test
  @DisplayName("cancelEmail - Exception Throws BadRequest - Success")
  void cancelEmail_s09_exceptionThrowsBadRequest_success() throws Exception {
    // Arrange
    EmailHelper helper = new EmailHelper("from@example.com", "Sender Name", "sendgrid-key");

    try (MockedConstruction<SendGrid> mocked =
        mockConstruction(
            SendGrid.class,
            (mock, context) ->
                when(mock.api(any(Request.class))).thenThrow(new RuntimeException("boom")))) {
      // Act
      BadRequestException exception =
          org.junit.jupiter.api.Assertions.assertThrows(
              BadRequestException.class, () -> helper.cancelEmail("batch-id"));

      // Assert
      assertEquals(ErrorMessages.EmailErrorMessages.ER003 + ": boom", exception.getMessage());
      verify(mocked.constructed().getFirst()).api(any(Request.class));
    }
  }

  /**
   * Purpose: Verify meetingRequestString supports cancellation method branch. Expected Result:
   * Calendar content includes METHOD:CANCEL marker. Assertions: ICS output contains cancellation
   * method.
   */
  @Test
  @DisplayName("meetingRequestString - Cancel Method Branch - Success")
  void meetingRequestString_s10_cancelMethodBranch_success() throws Exception {
    // Arrange
    EmailHelper helper = new EmailHelper("from@example.com", "Sender Name", "sendgrid-key");
    Method method =
        EmailHelper.class.getDeclaredMethod(
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
                "Line 1\nLine 2",
                LocalDateTime.of(2026, 1, 1, 10, 0),
                LocalDateTime.of(2026, 1, 1, 11, 0),
                true);

    // Assert
    assertTrue(calendar.contains("METHOD:CANCEL"));
    assertTrue(calendar.contains("X-ALT-DESC;FMTTYPE=text/html:Line 1<br>Line 2"));
  }

  private static SendEmailRequest buildRequest(
      boolean includeInvite, boolean includeAttachment, boolean includeSchedule) {
    SendEmailRequest request = new SendEmailRequest();
    request.setToAddress(List.of("to@example.com"));
    request.setSubject("Testing Subject");
    request.setPlainTextContent("Plain text body");
    request.setHtmlContent("<p>Html body</p>");

    if (includeAttachment) {
      Attachments attachments = new Attachments();
      attachments.setFilename("agenda.txt");
      attachments.setContent("YQ==");
      attachments.setType("text/plain");
      attachments.setDisposition("attachment");
      request.setAttachments(List.of(attachments));
    }

    if (includeSchedule) {
      request.setSendAt(LocalDateTime.of(2026, 1, 1, 9, 0));
      request.setBatchId("batch-123");
    }

    request.setInvite(includeInvite);
    if (includeInvite) {
      request.setMeetingDate(LocalDateTime.of(2026, 1, 2, 10, 0));
      request.setAttendees(List.of("attendee@example.com"));
    }

    return request;
  }
}
