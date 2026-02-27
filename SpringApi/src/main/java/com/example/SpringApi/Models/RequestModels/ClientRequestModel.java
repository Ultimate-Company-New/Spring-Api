package com.example.SpringApi.Models.RequestModels;

import lombok.Getter;
import lombok.Setter;

/**
 * Request model for Client operations.
 *
 * <p>This model contains all the fields required for creating or updating a client. It includes
 * validation constraints and business logic requirements.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class ClientRequestModel {

  private Long clientId;
  private String name;
  private String description;
  private String sendGridApiKey;
  private String sendGridEmailAddress;
  private Boolean isDeleted;
  private String supportEmail;
  private String website;
  private String sendgridSenderName;
  private String razorpayApiKey;
  private String razorpayApiSecret;
  private String imgbbApiKey;
  private String shipRocketEmail;
  private String shipRocketPassword;
  private String jiraUserName;
  private String jiraPassword;
  private String jiraProjectUrl;
  private String jiraProjectKey;
  private String issueTypes;
  private Long googleCredId;
  private String notes;
  private String logoBase64;
}

