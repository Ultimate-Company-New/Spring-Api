package com.example.SpringApi.Helpers;

import com.example.SpringApi.Models.RequestModels.SendEmailRequest;

/** Common interface for email sending implementations (SendGrid, Brevo, etc.). */
public interface IEmailHelper {
  boolean sendEmail(SendEmailRequest request);

  String generateBatchId();

  void cancelEmail(String batchId);
}

