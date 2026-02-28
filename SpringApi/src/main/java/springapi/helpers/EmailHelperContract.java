package springapi.helpers;

import springapi.models.requestmodels.SendEmailRequest;

/** Common interface for email sending implementations (SendGrid, Brevo, etc.). */
public interface EmailHelperContract {
  boolean sendEmail(SendEmailRequest request);

  String generateBatchId();

  void cancelEmail(String batchId);
}
