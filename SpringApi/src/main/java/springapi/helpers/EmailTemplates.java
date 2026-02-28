package com.example.springapi.helpers;

import com.example.springapi.models.databasemodels.Client;
import com.example.springapi.models.requestmodels.SendEmailRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.env.Environment;

/**
 * Represents the email templates component.
 */
public class EmailTemplates {
  private static final String IMPORT_OF_PREFIX = "Import of ";
  private final EmailHelperContract emailHelper;
  private final Environment environment;
  private final Client client;

  /**
   * Initializes EmailTemplates.
   */
  public EmailTemplates(
      String senderName,
      String fromAddress,
      String sendgridApiKey,
      Environment environment,
      Client client) {
    this.environment = environment;
    this.client = client;
    this.emailHelper =
        EmailHelperFactory.create(fromAddress, senderName, sendgridApiKey, environment);
  }

  /**
   * Executes send import bulk data results.
   */
  public boolean sendImportBulkDataResults(
      String importType,
      Map<String, String> errors,
      int totalDataCountToBeImported,
      String userEmail) {

    String status;
    if (errors == null || errors.isEmpty()) {
      status = IMPORT_OF_PREFIX + importType + " was successful, with no errors reported.";
    } else if (errors.size() < totalDataCountToBeImported) {
      status = IMPORT_OF_PREFIX + importType + " was partially successful.";
    } else {
      status = IMPORT_OF_PREFIX + importType + " failed.";
    }

    StringBuilder tableRows = new StringBuilder();
    int srNo = 1;
    if (errors != null && !errors.isEmpty()) {
      for (Map.Entry<String, String> entry : errors.entrySet()) {
        tableRows.append(
            String.format(
                "<tr><td style='border: 1px solid #ddd; padding: 8px;'>%d</td><td "
                    + "style='border: 1px solid #ddd; padding: 8px;'>%s</td><td "
                    + "style='border: 1px solid #ddd; padding: 8px;'>%s</td></tr>",
                srNo++, entry.getKey(), entry.getValue()));
      }
    }

    // Use logo URL from client (no need to fetch from Firebase)
    String companyLogoUrl = client.getLogoUrl();
    String logoHtml =
        (companyLogoUrl != null && !companyLogoUrl.isEmpty())
            ? String.format(
                "<img src=\"%s\" alt=\"Company Logo\" style=\"width: 300px; height: "
                    + "200px; margin-bottom: 20px;\">",
                companyLogoUrl)
            : "";

    String emailTemplate =
        String.format(
            """
                <div style="font-family: Arial, sans-serif; color: #333;">
                    <header style="padding: 10px; text-align: center; background-color: #f3f4f6;">
                        %s
                        <h2>Import Report for %s</h2>
                    </header>
                    <main style="padding: 20px;">
                        <p>%s</p>
                        %s
                        <table style="width: 100%%; border-collapse: collapse; margin-top: 20px;">
                            <thead>
                                <tr>
                                    <th style="border: 1px solid #ddd; padding: 8px;
                                    background-color: #f3f4f6;">Sr No</th>
                                    <th style="border: 1px solid #ddd; padding: 8px;
                                    background-color: #f3f4f6;">Import Field</th>
                                    <th style="border: 1px solid #ddd; padding: 8px;
                                    background-color: #f3f4f6;">Error</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                            </tbody>
                        </table>
                    </main>
                    <footer style="padding: 10px; text-align: center; background-color: #f3f4f6; " +
                        "font-size: 12px; color: #666;">
                        <p>Thank you for choosing us!</p>
                    </footer>
                </div>
                """,
            logoHtml,
            importType,
            status,
            errors != null && !errors.isEmpty() ? "<p>Details of errors are listed below:</p>" : "",
            tableRows);

    String plainText =
        String.format(
            """
                        Import Report for %s

                        %s

                        %s

                        Error Details:
                        %s

                        Thank you for choosing us!""",
            importType,
            status,
            errors != null && !errors.isEmpty() ? "Details of errors are listed below:" : "",
            errors != null && !errors.isEmpty()
                ? errors.entrySet().stream()
                    .map(
                        entry ->
                            String.format(
                                "Sr No: %d%nImport Field: %s%nError: %s%n",
                                errors.entrySet().stream().toList().indexOf(entry) + 1,
                                entry.getKey(),
                                entry.getValue()))
                    .collect(Collectors.joining(System.lineSeparator()))
                : "No errors reported.");

    // Create SendEmailRequest object with the details
    SendEmailRequest sendEmailRequest = new SendEmailRequest();
    sendEmailRequest.setToAddress(List.of(userEmail));
    sendEmailRequest.setSubject("Import Report for " + importType);
    sendEmailRequest.setHtmlContent(emailTemplate);
    sendEmailRequest.setPlainTextContent(plainText);

    // No need for logo attachment since we're using logo URL directly in HTML
    // Excel attachment generation can be added here if needed in the future

    // Send the account confirmation email using email helper
    return emailHelper.sendEmail(sendEmailRequest);
  }

  /**
   * Executes send new user account confirmation.
   */
  public boolean sendNewUserAccountConfirmation(
      long userId, String userToken, String userEmail, String temporaryPassword) {
    String profile =
        environment.getActiveProfiles().length > 0 ? environment.getActiveProfiles()[0] : "default";
    String frontendUrl = environment.getProperty("frontend.url." + profile);

    // Generate the confirmation account link pointing to FRONTEND
    // Frontend will handle the confirmation and call the API
    String confirmAccountLink =
        String.format(
            "%s/confirm-email?userId=%s&token=%s",
            frontendUrl,
            userId,
            java.net.URLEncoder.encode(userToken, java.nio.charset.StandardCharsets.UTF_8));

    // Use logo URL from client (no need to fetch from Firebase)
    String companyLogoUrl = client.getLogoUrl();

    // Create the email template with logo URL (if available)
    String logoHtml =
        (companyLogoUrl != null && !companyLogoUrl.isEmpty())
            ? String.format(
                "<img src=\"%s\" alt=\"%s Logo\" style=\"width: 300px; height: 200px;"
                    + " margin-bottom: 20px;\">",
                companyLogoUrl, client.getName())
            : "";

    String emailTemplate =
        String.format(
            """
                <div style="font-family: Arial, sans-serif; color: #333;">
                    <header style="padding: 10px; text-align: center; background-color: #f3f4f6;">
                        %s
                        <h2>Welcome to %s</h2>
                    </header>
                    <main style="padding: 20px;">
                        <p>Dear User,</p>
                        <p>Please click on the link below to confirm your email account:</p>
                        <p>
                            <a href="%s" style="background-color: #4CAF50; color: white;
                            padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                                Confirm Account
                            </a>
                        </p>
                        <p>If you are unable to click the button, use the following link:</p>
                        <p><a href="%s">%s</a></p>
                        <p>Your temporary password is: <strong>%s</strong></p>
                    </main>
                    <footer style="padding: 10px; text-align: center; background-color: #f3f4f6; " +
                        "font-size: 12px; color: #666;">
                        <p>Thank you for choosing %s!</p>
                        <p>For support, contact us at <a href="mailto:%s">%s</a></p>
                    </footer>
                </div>
                """,
            logoHtml,
            client.getName(),
            confirmAccountLink,
            confirmAccountLink,
            confirmAccountLink,
            temporaryPassword,
            client.getName(),
            client.getSupportEmail(),
            client.getSupportEmail());

    // Plain text fallback content
    String plainText =
        String.format(
            """
                        Please click on the link below to confirm your email account:
                        %s

                        Your temporary password is: %s

                        Thank you for choosing %s!""",
            confirmAccountLink, temporaryPassword, client.getName());

    // Create SendEmailRequest object with the details
    SendEmailRequest sendEmailRequest = new SendEmailRequest();
    sendEmailRequest.setToAddress(List.of(userEmail));
    sendEmailRequest.setSubject("Account Confirmation with temporary password");
    sendEmailRequest.setHtmlContent(emailTemplate);
    sendEmailRequest.setPlainTextContent(plainText);

    // No need for attachments since we're using logo URL directly in HTML

    // Send the account confirmation email using email helper
    return emailHelper.sendEmail(sendEmailRequest);
  }

  /**
   * Sends a message email with professional template including logo and footer. The message body.
   * HTML is rendered within the template.
   *
   * @param recipientEmails List of recipient email addresses
   * @param messageTitle The title/subject of the message
   * @param messageBodyHtml The HTML content of the message body
   * @param sendAt Optional scheduled send time (null for immediate send)
   * @param batchId Optional batch ID for scheduled emails
   * @return true if email was sent successfully, false otherwise
   */
  public boolean sendMessageEmail(
      List<String> recipientEmails,
      String messageTitle,
      String messageBodyHtml,
      java.time.LocalDateTime sendAt,
      String batchId) {

    // Use logo URL from client
    String companyLogoUrl = client.getLogoUrl();

    // Create logo HTML if available
    String logoHtml =
        (companyLogoUrl != null && !companyLogoUrl.isEmpty())
            ? String.format(
                "<img src=\"%s\" alt=\"%s Logo\" style=\"max-width: 300px; height: "
                    + "auto; margin-bottom: 20px;\">",
                companyLogoUrl, client.getName())
            : "";

    // Create the professional email template with logo and footer
    String emailTemplate =
        String.format(
            """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; " +
                    "background-color: #f5f5f5;">
                    <table width="100%%" cellpadding="0" cellspacing="0"
                    style="background-color: #f5f5f5;">
                        <tr>
                            <td align="center" style="padding: 20px 0;">
                                <table width="600" cellpadding="0" cellspacing="0"
                                style="background-color: #ffffff; border-radius: 8px;
                                box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                    <!-- Header -->
                                    <tr>
                                        <td style="padding: 30px 40px; text-align: center;
                                        background-color: #f3f4f6; border-radius: 8px 8px 0
                                        0;">
                                            %s
                                            <h1 style="margin: 10px 0 0 0; color: #333;
                                            font-size: 24px; font-weight: bold;">%s</h1>
                                        </td>
                                    </tr>
                                    <!-- Message Title -->
                                    <tr>
                                        <td style="padding: 20px 40px 10px 40px;">
                                            <h2 style="margin: 0; color: #1f2937; font-size:
                                            20px; font-weight: 600;">%s</h2>
                                        </td>
                                    </tr>
                                    <!-- Message Body -->
                                    <tr>
                                        <td style="padding: 10px 40px 30px 40px; color:
                                        #4b5563; font-size: 14px; line-height: 1.6;">
                                            %s
                                        </td>
                                    </tr>
                                    <!-- Footer -->
                                    <tr>
                                        <td style="padding: 20px 40px; text-align: center;
                                        background-color: #f3f4f6; border-radius: 0 0 8px 8px;
                                        border-top: 1px solid #e5e7eb;">
                                            <p style="margin: 0 0 10px 0; font-size: 14px;
                                            color: #6b7280;">Thank you for choosing %s!</p>
                                            <p style="margin: 0; font-size: 12px; color: #9ca3af;">
                                                For support, contact us at <a href="mailto:%s"
                                                style="color: #3b82f6; text-decoration:
                                                none;">%s</a>
                                            </p>
                                            <p style="margin: 10px 0 0 0; font-size: 11px;
                                            color: #9ca3af;">
                                                &copy; %s %s. All rights reserved.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """,
            messageTitle,
            logoHtml,
            client.getName(),
            messageTitle,
            messageBodyHtml,
            client.getName(),
            client.getSupportEmail(),
            client.getSupportEmail(),
            java.time.Year.now().getValue(),
            client.getName());

    // Plain text fallback content (strip HTML tags)
    String plainText =
        String.format(
            """
                %s

                %s

                %s

                Thank you for choosing %s!
                For support, contact us at %s
                """,
            client.getName(),
            messageTitle,
            messageBodyHtml.replaceAll("<[^>]*>", ""),
            client.getName(),
            client.getSupportEmail());

    // Create SendEmailRequest object
    SendEmailRequest sendEmailRequest = new SendEmailRequest();
    sendEmailRequest.setToAddress(recipientEmails);
    sendEmailRequest.setSubject(messageTitle);
    sendEmailRequest.setHtmlContent(emailTemplate);
    sendEmailRequest.setPlainTextContent(plainText);

    // Set scheduling parameters if provided
    if (sendAt != null) {
      sendEmailRequest.setSendAt(sendAt);
    }
    if (batchId != null) {
      sendEmailRequest.setBatchId(batchId);
    }

    // Send the email
    return emailHelper.sendEmail(sendEmailRequest);
  }

  /**
   * Sends a password reset email to the specified email address with a professional template. The.
   * email includes the company logo, a styled header, the new password, security instructions, and
   * a branded footer with support information.
   *
   * @param email The email address of the recipient.
   * @param password The new password for the user.
   * @return true if the email was sent successfully, false otherwise.
   * @throws IllegalArgumentException if password is null or empty
   */
  public boolean sendResetPasswordEmail(String email, String password) {
    // Validate that password is provided
    if (password == null || password.trim().isEmpty()) {
      throw new IllegalArgumentException("Password cannot be null or empty");
    }

    // Build the email body with professional HTML template
    String emailBody =
        String.format(
            """
                <div style="padding: 30px; background-color: #ffffff;">
                    <h2 style="color: #2c3e50; margin-bottom: 20px; font-size: 24px;
                    border-bottom: 2px solid #3498db; padding-bottom: 10px;">
                        Password Reset Successful
                    </h2>

                    <p style="color: #34495e; font-size: 16px; line-height: 1.6; margin-bottom: " +
                        "20px;">
                        Your password has been successfully reset. Please use the new password
                        below to sign in to your account.
                    </p>

                    <div style="background-color: #f8f9fa; border-left: 4px solid #3498db;
                    padding: 20px; margin: 25px 0; border-radius: 4px;">
                        <p style="color: #7f8c8d; font-size: 14px; margin: 0 0 10px 0;
                        text-transform: uppercase; letter-spacing: 1px;">
                            Your New Password
                        </p>
                        <p data-test-id="reset-password-value" style="color: #2c3e50;
                        font-size: 20px; font-weight: bold; margin: 0; font-family: 'Courier
                        New', monospace; word-break: break-all;">
                            %s
                        </p>
                    </div>

                    <div style="background-color: #fff3cd; border: 1px solid #ffc107;
                    border-radius: 4px; padding: 15px; margin: 25px 0;">
                        <p style="color: #856404; font-size: 14px; margin: 0; line-height: 1.6;">
                            <strong>⚠️ Important Security Information:</strong><br/>
                            • For your security, we strongly recommend changing this password
                            after logging in.<br/>
                            • If you did not request this password reset, please contact our
                            support team immediately.<br/>
                            • Never share your password with anyone.
                        </p>
                    </div>

                    <p style="color: #34495e; font-size: 16px; line-height: 1.6; margin-top: 25px;">
                        You can now sign in to your account using this new password. Once
                        signed in, you can change it to something more memorable in your
                        account settings.
                    </p>

                    <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid
                    #e0e0e0;">
                        <p style="color: #7f8c8d; font-size: 14px; line-height: 1.6;">
                            Need assistance? Our support team is here to help!<br/>
                            Contact us at <a href="mailto:%s" style="color: #3498db;
                            text-decoration: none;">%s</a>
                        </p>
                    </div>
                </div>
                """,
            password, client.getSupportEmail(), client.getSupportEmail());

    // Generate the full HTML template with logo, header, and footer
    final String emailTemplate =
        String.format(
            """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Password Reset</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, " +
                    "Verdana, sans-serif; background-color: #f4f4f4;">
                    <table role="presentation" style="width: 100%%; border-collapse: collapse;
                    background-color: #f4f4f4;">
                        <tr>
                            <td align="center" style="padding: 40px 0;">
                                <table role="presentation" style="width: 600px;
                                border-collapse: collapse; background-color: #ffffff;
                                box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); border-radius: 8px;
                                overflow: hidden;">
                                    <!-- Header with Logo -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #667eea
                                        0%%, #764ba2 100%%); padding: 40px; text-align:
                                        center;">
                                            <img src="%s" alt="%s Logo" style="max-width:
                                            180px; height: auto; margin-bottom: 15px;" />
                                            <h1 style="color: #ffffff; margin: 0; font-size:
                                            28px; font-weight: 600;">
                                                %s
                                            </h1>
                                        </td>
                                    </tr>

                                    <!-- Main Content -->
                                    <tr>
                                        <td>
                                            %s
                                        </td>
                                    </tr>

                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #2c3e50; padding: 30px;
                                        text-align: center;">
                                            <p style="color: #ecf0f1; font-size: 14px; margin:
                                            0 0 10px 0;">
                                                Thank you for choosing <strong>%s</strong>
                                            </p>
                                            <p style="color: #95a5a6; font-size: 12px; margin:
                                            0 0 15px 0;">
                                                For support and inquiries, contact us at:<br/>
                                                <a href="mailto:%s" style="color: #3498db;
                                                text-decoration: none;">%s</a>
                                            </p>
                                            <p style="color: #7f8c8d; font-size: 11px; margin: 0;">
                                                &copy; %d %s. All rights reserved.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """,
            client.getLogoUrl(),
            client.getName(),
            client.getName(),
            emailBody,
            client.getName(),
            client.getSupportEmail(),
            client.getSupportEmail(),
            java.time.Year.now().getValue(),
            client.getName());

    // Plain text fallback content (strip HTML tags)
    final String plainText =
        String.format(
            """
                %s - Password Reset

                Your password has been successfully reset.

                Your New Password: %s

                IMPORTANT SECURITY INFORMATION:
                - For your security, please change this password after logging in.
                - If you did not request this password reset, contact support immediately.
                - Never share your password with anyone.

                You can now sign in to your account using this new password.

                Thank you for choosing %s!
                For support, contact us at %s
                """,
            client.getName(), password, client.getName(), client.getSupportEmail());

    // Create SendEmailRequest object
    SendEmailRequest sendEmailRequest = new SendEmailRequest();
    List<String> toAddresses = new ArrayList<>();
    toAddresses.add(email);
    sendEmailRequest.setToAddress(toAddresses);
    sendEmailRequest.setSubject("Password Reset - " + client.getName());
    sendEmailRequest.setHtmlContent(emailTemplate);
    sendEmailRequest.setPlainTextContent(plainText);

    // Verify that the password is actually included in the HTML content
    if (!emailTemplate.contains(password)) {
      throw new IllegalStateException(
          "Password is not properly included in the email HTML content");
    }

    // Send the reset password email using the email helper
    return emailHelper.sendEmail(sendEmailRequest);
  }
}
