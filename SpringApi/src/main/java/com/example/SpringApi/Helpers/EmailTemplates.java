package com.example.SpringApi.Helpers;

import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.RequestModels.SendEmailRequest;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class EmailTemplates {
    private final EmailHelper emailHelper;
    private final Environment environment;
    private final Client client;

    public EmailTemplates(String senderName,
                          String fromAddress,
                          String sendgridApiKey,
                          Environment environment,
                          Client client) {
        this.environment = environment;
        this.client = client;
        this.emailHelper = new EmailHelper(fromAddress, senderName, sendgridApiKey);
    }

    public boolean sendImportBulkDataResults(
            String importType,
            Map<String, String> errors,
            int totalDataCountToBeImported,
            String userEmail) throws IOException {

        String status;
        if (errors == null || errors.isEmpty()) {
            status = "Import of " + importType + " was successful, with no errors reported.";
        } else if (errors.size() < totalDataCountToBeImported) {
            status = "Import of " + importType + " was partially successful.";
        } else {
            status = "Import of " + importType + " failed.";
        }

        StringBuilder tableRows = new StringBuilder();
        int srNo = 1;
        if (errors != null && !errors.isEmpty()) {
            for (Map.Entry<String, String> entry : errors.entrySet()) {
                tableRows.append(String.format(
                        "<tr><td style='border: 1px solid #ddd; padding: 8px;'>%d</td><td style='border: 1px solid #ddd; padding: 8px;'>%s</td><td style='border: 1px solid #ddd; padding: 8px;'>%s</td></tr>",
                        srNo++, entry.getKey(), entry.getValue()
                ));
            }
        }

        // Use logo URL from client (no need to fetch from Firebase)
        String companyLogoUrl = client.getLogoUrl();
        String logoHtml = (companyLogoUrl != null && !companyLogoUrl.isEmpty()) 
            ? String.format("<img src=\"%s\" alt=\"Company Logo\" style=\"width: 300px; height: 200px; margin-bottom: 20px;\">", 
                companyLogoUrl)
            : "";

        String emailTemplate = String.format(
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
                                    <th style="border: 1px solid #ddd; padding: 8px; background-color: #f3f4f6;">Sr No</th>
                                    <th style="border: 1px solid #ddd; padding: 8px; background-color: #f3f4f6;">Import Field</th>
                                    <th style="border: 1px solid #ddd; padding: 8px; background-color: #f3f4f6;">Error</th>
                                </tr>
                            </thead>
                            <tbody>
                                %s
                            </tbody>
                        </table>
                    </main>
                    <footer style="padding: 10px; text-align: center; background-color: #f3f4f6; font-size: 12px; color: #666;">
                        <p>Thank you for choosing us!</p>
                    </footer>
                </div>
                """,
                logoHtml,
                importType,
                status,
                errors != null && !errors.isEmpty() ? "<p>Details of errors are listed below:</p>" : "",
                tableRows
        );

        String plainText = String.format(
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
                errors != null && !errors.isEmpty() ? errors.entrySet().stream()
                        .map(entry -> String.format("Sr No: %d\nImport Field: %s\nError: %s\n",
                                errors.entrySet().stream().toList().indexOf(entry) + 1, entry.getKey(), entry.getValue()))
                        .collect(Collectors.joining("\n"))
                        : "No errors reported."
        );

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

    public boolean sendNewUserAccountConfirmation(
            long userId,
            String userToken,
            String userEmail,
            String temporaryPassword
    ) {
        String profile = environment.getActiveProfiles().length > 0 ? environment.getActiveProfiles()[0] : "default";
        String apiUrl = environment.getProperty("app.url." + profile);

        // Generate the confirmation account link (POST endpoint with query parameter for token)
        // Using query parameter to avoid URL encoding issues with bcrypt tokens containing '/'
        String confirmAccountLink = String.format(
                "%s/api/User/confirmEmail/%s?token=%s",
                apiUrl,
                userId,
                java.net.URLEncoder.encode(userToken, java.nio.charset.StandardCharsets.UTF_8)
        );

        // Use logo URL from client (no need to fetch from Firebase)
        String companyLogoUrl = client.getLogoUrl();

        // Create the email template with logo URL (if available)
        String logoHtml = (companyLogoUrl != null && !companyLogoUrl.isEmpty()) 
            ? String.format("<img src=\"%s\" alt=\"%s Logo\" style=\"width: 300px; height: 200px; margin-bottom: 20px;\">", 
                companyLogoUrl, client.getName())
            : "";
        
        String emailTemplate = String.format(
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
                            <a href="%s" style="background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                                Confirm Account
                            </a>
                        </p>
                        <p>If you are unable to click the button, use the following link:</p>
                        <p><a href="%s">%s</a></p>
                        <p>Your temporary password is: <strong>%s</strong></p>
                    </main>
                    <footer style="padding: 10px; text-align: center; background-color: #f3f4f6; font-size: 12px; color: #666;">
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
                client.getSupportEmail()
        );

        // Plain text fallback content
        String plainText = String.format(
                """
                        Please click on the link below to confirm your email account:
                        %s
                        
                        Your temporary password is: %s
                        
                        Thank you for choosing %s!""",
                confirmAccountLink, temporaryPassword, client.getName()
        );

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
     * Sends a password reset email to the specified email address.
     *
     * @param email    The email address of the recipient.
     * @param password The new password for the user.
     * @return A response indicating the success status and message.
     */
    public boolean sendResetPasswordEmail(String email, String password) {
        // Validate that password is provided
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        // Create a SendEmailRequest object with the necessary details
        SendEmailRequest sendEmailRequest = new SendEmailRequest();
        sendEmailRequest.setSubject("Password Reset");
        List<String> toAddresses = new ArrayList<>();
        toAddresses.add(email);
        sendEmailRequest.setToAddress(toAddresses);
        sendEmailRequest.setPlainTextContent("Your New password is: " + password);
        String htmlContent = """
        <html>
          <body style='font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;'>
            <div style='max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);'>
              <header style='text-align: center; padding: 20px 0;'>
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 87.98 92.12" style='width: 150px; margin-bottom: 10px;'>
                  <path d="M6.46 69.53c3.17 2.5 6.37 4.94 10.19 6.36 13.43 5 26 3.14 37.2-5.58A22.62 22.62 0 0 0 62.52 52c.09-15.33 0-30.65 0-46 0-3.81.25-4.16 4-3.33C79 5.47 87.64 14.76 87.88 27.38c.17 8.26.08 16.53 0 24.79a39.38 39.38 0 0 1-31.35 38.47c-10.38 2.29-20.47 2.18-30.31-2.12a44.12 44.12 0 0 1-20-17.6A3.08 3.08 0 0 1 6 70z" fill="#0bf"/>
                  <path d="M0 23.1V3.46C0 .3.65-.42 3.62.2c12.16 2.55 21.27 11 21.7 24.33.25 7.74.11 15.5 0 23.26-.08 6.65 2.25 12 7.7 16 2 1.44 1.88 2.15-.37 3.16-7.94 3.54-20.48.89-26.83-7.16C1.79 54.67.3 48.77.1 42.48-.1 36 .06 29.56.06 23.1z" fill="#5592ff"/>
                </svg>
                <h1 style='color: #333;'>Ultimate Company</h1>
              </header>
              <div style='padding: 20px;'>
                <h3 style='color: #333;'>Your new password is:</h3>
                <p style='font-size: 18px; font-weight: bold; color: #555;'>""" + password + """
                </p>
                <p style='color: #777; margin-top: 20px;'>For your security, please change this password after logging in.</p>
                <p style='color: #777;'>If you did not request a new password, please contact our support team immediately.</p>
              </div>
              <footer style='text-align: center; padding: 10px 0; background-color: #333; color: white; border-top: 1px solid #ddd;'>
                <p>&copy;""" + java.time.Year.now() + """ 
                Ultimate Company. All rights reserved.</p>
                <p>Mumbai Maharashtra</p>
              </footer>
            </div>
          </body>
        </html>""";

        // Verify that the password is actually included in the HTML content
        if (!htmlContent.contains(password)) {
            throw new IllegalStateException("Password is not properly included in the email HTML content");
        }

        sendEmailRequest.setHtmlContent(htmlContent);

        // Send the reset password email using the email helper
        return emailHelper.sendEmail(sendEmailRequest);
    }
}