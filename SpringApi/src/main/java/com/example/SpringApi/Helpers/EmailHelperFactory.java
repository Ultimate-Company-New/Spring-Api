package com.example.SpringApi.Helpers;

import org.springframework.core.env.Environment;

/**
 * Factory for creating the appropriate email helper based on configuration. Uses email.service
 * property (brevo or sendgrid); defaults to sendgrid when not set.
 */
public final class EmailHelperFactory {

  private static final String EMAIL_SERVICE_PROPERTY = "email.service";
  private static final String BREVO = "brevo";

  private EmailHelperFactory() {}

  public static IEmailHelper create(
      String fromAddress, String senderName, String apiKey, Environment environment) {
    String emailService =
        environment != null
            ? environment.getProperty(EMAIL_SERVICE_PROPERTY, "sendgrid")
            : "sendgrid";
    if (BREVO.equalsIgnoreCase(emailService.trim())) {
      return new BrevoEmailHelper(fromAddress, senderName, apiKey);
    }
    return new EmailHelper(fromAddress, senderName, apiKey);
  }
}
