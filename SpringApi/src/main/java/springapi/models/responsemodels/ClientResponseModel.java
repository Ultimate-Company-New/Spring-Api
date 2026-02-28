package springapi.models.responsemodels;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import springapi.models.databasemodels.Client;

/**
 * Response model for Client operations.
 *
 * <p>This model contains all the fields returned when retrieving client information. It includes
 * audit fields and metadata for comprehensive client data.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class ClientResponseModel {

  private Long clientId;
  private String name;
  private String logoUrl;
  private String apiKey;
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
  private String createdUser;
  private String modifiedUser;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String notes;

  /** Default constructor. */
  public ClientResponseModel() {}

  /**
   * Constructor that populates fields from a Client entity.
   *
   * @param client The Client entity to populate from
   */
  public ClientResponseModel(Client client) {
    if (client != null) {
      this.clientId = client.getClientId();
      this.name = client.getName();
      this.logoUrl = client.getLogoUrl();
      this.description = client.getDescription();
      this.sendGridApiKey = client.getSendGridApiKey();
      this.sendGridEmailAddress = client.getSendGridEmailAddress();
      this.isDeleted = client.getIsDeleted();
      this.supportEmail = client.getSupportEmail();
      this.website = client.getWebsite();
      this.sendgridSenderName = client.getSendgridSenderName();
      this.razorpayApiKey = client.getRazorpayApiKey();
      this.razorpayApiSecret = client.getRazorpayApiSecret();
      this.imgbbApiKey = client.getImgbbApiKey();
      this.shipRocketEmail = client.getShipRocketEmail();
      this.shipRocketPassword = client.getShipRocketPassword();
      this.jiraUserName = client.getJiraUserName();
      this.jiraPassword = client.getJiraPassword();
      this.jiraProjectUrl = client.getJiraProjectUrl();
      this.jiraProjectKey = client.getJiraProjectKey();
      this.issueTypes = client.getIssueTypes();
      this.googleCredId = client.getGoogleCredId();
      this.createdUser = client.getCreatedUser();
      this.modifiedUser = client.getModifiedUser();
      this.createdAt = client.getCreatedAt();
      this.updatedAt = client.getUpdatedAt();
      this.notes = client.getNotes();
    }
  }
}
