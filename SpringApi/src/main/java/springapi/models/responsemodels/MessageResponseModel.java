package springapi.models.responsemodels;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import springapi.models.databasemodels.Message;
import springapi.models.databasemodels.User;

/**
 * Response model for Message operations.
 *
 * <p>This model contains all the fields returned when retrieving message information. It includes
 * related entities and calculated fields for the UI.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class MessageResponseModel {

  private Long messageId;
  private String title;
  private LocalDateTime publishDate;
  private String descriptionHtml;
  private Boolean sendAsEmail;
  private Boolean isDeleted;
  private Long createdByUserId;
  private String sendgridEmailBatchId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdUser;
  private String modifiedUser;
  private String notes;
  private Long auditUserId;

  // Related entities (simplified to avoid circular references)
  private SimpleUserDto createdByUser;
  private SimpleUserDto auditUser;

  /** Simple DTO for user information to avoid circular references. */
  @Getter
  @Setter
  public static class SimpleUserDto {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String loginName;

    /** Initializes SimpleUserDto. */
    public SimpleUserDto(User user) {
      if (user != null) {
        this.userId = user.getUserId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.loginName = user.getLoginName();
      }
    }
  }

  // Additional computed fields
  private String titlePreview;
  private String statusText;
  private Boolean isPublished;
  private Boolean canEdit;
  private Integer daysOld;

  // Message targeting information
  private List<Long> userGroupIds;
  private List<Long> userIds;
  private Integer totalRecipients;
  private Boolean isRead;

  /**
   * Constructor to create response model from database entity.
   *
   * @param message The Message entity
   */
  public MessageResponseModel(Message message) {
    if (message != null) {
      this.messageId = message.getMessageId();
      this.title = message.getTitle();
      this.publishDate = message.getPublishDate();
      this.descriptionHtml = message.getDescriptionHtml();
      this.sendAsEmail = message.getSendAsEmail();
      this.isDeleted = message.getIsDeleted();
      this.createdByUserId = message.getCreatedByUserId();
      this.sendgridEmailBatchId = message.getSendgridEmailBatchId();
      this.createdAt = message.getCreatedAt();
      this.updatedAt = message.getUpdatedAt();
      this.createdUser = message.getCreatedUser();
      this.modifiedUser = message.getModifiedUser();
      this.notes = message.getNotes();

      // Set related entities (using simplified DTO to avoid circular references)
      if (message.getCreatedByUser() != null) {
        this.createdByUser = new SimpleUserDto(message.getCreatedByUser());
      }

      // Extract user IDs from MessageUserMap collection
      if (message.getMessageUserMaps() != null && !message.getMessageUserMaps().isEmpty()) {
        this.userIds =
            message.getMessageUserMaps().stream()
                .map(mum -> mum.getUserId())
                .collect(Collectors.toList());
      }

      // Extract group IDs from MessageUserGroupMap collection
      if (message.getMessageUserGroupMaps() != null
          && !message.getMessageUserGroupMaps().isEmpty()) {
        this.userGroupIds =
            message.getMessageUserGroupMaps().stream()
                .map(mugm -> mugm.getGroupId())
                .collect(Collectors.toList());
      }

      // Compute additional fields
      this.titlePreview = buildTitlePreview();
      this.isPublished = this.publishDate != null && this.publishDate.isBefore(LocalDateTime.now());
      this.statusText = buildStatusText();
      this.canEdit = !this.isDeleted && !this.isPublished;
      this.daysOld = calculateDaysOld();
    }
  }

  /**
   * Builds a preview of the title (truncated if too long).
   *
   * @return Title preview string
   */
  private String buildTitlePreview() {
    if (this.title != null && this.title.length() > 50) {
      return this.title.substring(0, 47) + "...";
    }
    return this.title;
  }

  /**
   * Builds a status text based on message state.
   *
   * @return Status text
   */
  private String buildStatusText() {
    if (this.isDeleted) {
      return "Deleted";
    } else if (this.publishDate == null) {
      return "Draft";
    } else if (this.publishDate.isAfter(LocalDateTime.now())) {
      return "Scheduled";
    } else {
      return "Published";
    }
  }

  /**
   * Calculates the number of days since the message was created.
   *
   * @return Number of days old
   */
  private Integer calculateDaysOld() {
    if (this.createdAt != null) {
      LocalDateTime now = LocalDateTime.now();
      return (int) java.time.Duration.between(this.createdAt, now).toDays();
    }
    return 0;
  }
}
