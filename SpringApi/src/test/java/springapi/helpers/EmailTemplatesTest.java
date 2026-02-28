package com.example.springapi.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.springapi.models.databasemodels.Client;
import com.example.springapi.models.requestmodels.SendEmailRequest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.core.env.Environment;

@DisplayName("EmailTemplates Tests")
class EmailTemplatesTest {

  // Total Tests: 6

  /**
   * Purpose: Verify import report email status is successful when there are no errors. Expected
   * Result: Email is sent with successful status and no error-detail text. Assertions: sendEmail
   * invoked and generated content includes success wording.
   */
  @Test
  @DisplayName("sendImportBulkDataResults - No Errors Status - Success")
  void sendImportBulkDataResults_s01_noErrorsStatus_success() {
    // Arrange
    EmailHelperContract emailHelper = mock(EmailHelperContract.class);
    when(emailHelper.sendEmail(any(SendEmailRequest.class))).thenReturn(true);

    Environment environment = mock(Environment.class);
    Client client = createClient("Ultimate Co", "support@ultimate.co", "https://logo.cdn/logo.png");
    EmailTemplates templates = createTemplates(emailHelper, environment, client);

    // Act
    boolean sent = templates.sendImportBulkDataResults("Products", null, 10, "ops@ultimate.co");

    // Assert
    ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
    verify(emailHelper).sendEmail(captor.capture());
    SendEmailRequest request = captor.getValue();
    assertTrue(sent);
    assertEquals(List.of("ops@ultimate.co"), request.getToAddress());
    assertTrue(request.getSubject().contains("Import Report for Products"));
    assertTrue(request.getHtmlContent().contains("was successful, with no errors reported"));
    assertTrue(request.getPlainTextContent().contains("No errors reported."));
  }

  /**
   * Purpose: Verify import report status branches for partial and full-failure error scenarios.
   * Expected Result: Partial and failed status messages appear based on error count. Assertions:
   * Captured request contents for both branch outcomes.
   */
  @Test
  @DisplayName("sendImportBulkDataResults - Partial And Failure Status Branches - Success")
  void sendImportBulkDataResults_s02_partialAndFailureStatusBranches_success() {
    // Arrange
    EmailHelperContract emailHelper = mock(EmailHelperContract.class);
    when(emailHelper.sendEmail(any(SendEmailRequest.class))).thenReturn(true);

    Environment environment = mock(Environment.class);
    Client client = createClient("Ultimate Co", "support@ultimate.co", "");
    EmailTemplates templates = createTemplates(emailHelper, environment, client);

    Map<String, String> partialErrors = new LinkedHashMap<>();
    partialErrors.put("row-1", "missing name");

    Map<String, String> fullErrors = new LinkedHashMap<>();
    fullErrors.put("row-1", "bad email");
    fullErrors.put("row-2", "bad phone");

    // Act
    templates.sendImportBulkDataResults("Users", partialErrors, 5, "ops@ultimate.co");
    templates.sendImportBulkDataResults("Users", fullErrors, 2, "ops@ultimate.co");

    // Assert
    ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
    verify(emailHelper, org.mockito.Mockito.times(2)).sendEmail(captor.capture());
    List<SendEmailRequest> requests = captor.getAllValues();

    assertTrue(requests.get(0).getHtmlContent().contains("was partially successful"));
    assertTrue(requests.get(0).getHtmlContent().contains("Details of errors are listed below"));

    assertTrue(requests.get(1).getHtmlContent().contains("Import of Users failed"));
    assertTrue(requests.get(1).getPlainTextContent().contains("Import Field"));
  }

  /**
   * Purpose: Verify new-user confirmation email builds frontend URL using active profile and sends
   * template. Expected Result: Email includes encoded token and temporary password. Assertions:
   * Captured email content fields.
   */
  @Test
  @DisplayName("sendNewUserAccountConfirmation - Encoded Frontend Link And Password - Success")
  void sendNewUserAccountConfirmation_s03_encodedFrontendLinkAndPassword_success() {
    // Arrange
    EmailHelperContract emailHelper = mock(EmailHelperContract.class);
    when(emailHelper.sendEmail(any(SendEmailRequest.class))).thenReturn(true);

    Environment environment = mock(Environment.class);
    when(environment.getActiveProfiles()).thenReturn(new String[] {"development"});
    when(environment.getProperty("frontend.url.development")).thenReturn("https://frontend.dev");

    Client client = createClient("Ultimate Co", "support@ultimate.co", "https://logo.cdn/logo.png");
    EmailTemplates templates = createTemplates(emailHelper, environment, client);

    // Act
    boolean sent =
        templates.sendNewUserAccountConfirmation(
            77L, "token with spaces", "user@ultimate.co", "TempPass#123");

    // Assert
    ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
    verify(emailHelper).sendEmail(captor.capture());
    SendEmailRequest request = captor.getValue();

    assertTrue(sent);
    assertTrue(
        request
            .getHtmlContent()
            .contains("https://frontend.dev/confirm-email?userId=77&token=token+with+spaces"));
    assertTrue(request.getHtmlContent().contains("TempPass#123"));
    assertTrue(request.getPlainTextContent().contains("TempPass#123"));
  }

  /**
   * Purpose: Verify message email flow sets scheduling fields when sendAt and batchId are provided.
   * Expected Result: Request carries sendAt and batchId metadata. Assertions: Captured request
   * scheduling fields and content.
   */
  @Test
  @DisplayName("sendMessageEmail - Scheduled Send Fields - Success")
  void sendMessageEmail_s04_scheduledSendFields_success() {
    // Arrange
    EmailHelperContract emailHelper = mock(EmailHelperContract.class);
    when(emailHelper.sendEmail(any(SendEmailRequest.class))).thenReturn(true);

    Environment environment = mock(Environment.class);
    Client client = createClient("Ultimate Co", "support@ultimate.co", "https://logo.cdn/logo.png");
    EmailTemplates templates = createTemplates(emailHelper, environment, client);

    LocalDateTime sendAt = LocalDateTime.of(2026, 2, 20, 10, 30);

    // Act
    boolean sent =
        templates.sendMessageEmail(
            List.of("a@u.co", "b@u.co"),
            "Maintenance Notice",
            "<p>Planned maintenance at 2 PM</p>",
            sendAt,
            "batch-123");

    // Assert
    ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
    verify(emailHelper).sendEmail(captor.capture());
    SendEmailRequest request = captor.getValue();

    assertTrue(sent);
    assertEquals(sendAt, request.getSendAt());
    assertEquals("batch-123", request.getBatchId());
    assertTrue(request.getHtmlContent().contains("Maintenance Notice"));
    assertTrue(request.getPlainTextContent().contains("Planned maintenance at 2 PM"));
  }

  /**
   * Purpose: Verify reset-password email rejects blank passwords. Expected Result:
   * IllegalArgumentException is thrown. Assertions: Exception type and exact message.
   */
  @Test
  @DisplayName("sendResetPasswordEmail - Blank Password Throws IllegalArgumentException - Success")
  void sendResetPasswordEmail_s05_blankPasswordThrowsIllegalArgumentException_success() {
    // Arrange
    EmailHelperContract emailHelper = mock(EmailHelperContract.class);
    Environment environment = mock(Environment.class);
    Client client = createClient("Ultimate Co", "support@ultimate.co", "https://logo.cdn/logo.png");
    EmailTemplates templates = createTemplates(emailHelper, environment, client);

    // Act
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> templates.sendResetPasswordEmail("user@ultimate.co", " "));

    // Assert
    assertEquals("Password cannot be null or empty", exception.getMessage());
  }

  /**
   * Purpose: Verify reset-password email embeds new password and support info then dispatches
   * email. Expected Result: sendEmail invoked with password present in HTML/plain text. Assertions:
   * Captured email content and subject.
   */
  @Test
  @DisplayName("sendResetPasswordEmail - Includes Password And Sends Email - Success")
  void sendResetPasswordEmail_s06_includesPasswordAndSendsEmail_success() {
    // Arrange
    EmailHelperContract emailHelper = mock(EmailHelperContract.class);
    when(emailHelper.sendEmail(any(SendEmailRequest.class))).thenReturn(true);

    Environment environment = mock(Environment.class);
    Client client = createClient("Ultimate Co", "support@ultimate.co", "https://logo.cdn/logo.png");
    EmailTemplates templates = createTemplates(emailHelper, environment, client);

    // Act
    boolean sent = templates.sendResetPasswordEmail("user@ultimate.co", "TempPass#1");

    // Assert
    ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);
    verify(emailHelper).sendEmail(captor.capture());
    SendEmailRequest request = captor.getValue();

    assertTrue(sent);
    assertTrue(request.getSubject().contains("Password Reset - Ultimate Co"));
    assertTrue(request.getHtmlContent().contains("TempPass#1"));
    assertTrue(request.getPlainTextContent().contains("TempPass#1"));
    assertTrue(request.getHtmlContent().contains("support@ultimate.co"));
  }

  private EmailTemplates createTemplates(
      EmailHelperContract emailHelper, Environment environment, Client client) {
    try (MockedStatic<EmailHelperFactory> factoryMock =
        org.mockito.Mockito.mockStatic(EmailHelperFactory.class)) {
      factoryMock
          .when(
              () ->
                  EmailHelperFactory.create(
                      org.mockito.ArgumentMatchers.anyString(),
                      org.mockito.ArgumentMatchers.anyString(),
                      org.mockito.ArgumentMatchers.anyString(),
                      org.mockito.ArgumentMatchers.any(Environment.class)))
          .thenReturn(emailHelper);
      return new EmailTemplates(
          "Ultimate Sender", "noreply@ultimate.co", "api-key", environment, client);
    }
  }

  private Client createClient(String name, String supportEmail, String logoUrl) {
    Client client = new Client();
    client.setName(name);
    client.setSupportEmail(supportEmail);
    client.setLogoUrl(logoUrl);
    return client;
  }
}
