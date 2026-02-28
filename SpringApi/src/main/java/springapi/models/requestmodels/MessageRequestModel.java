package com.example.springapi.models.requestmodels;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Request model for Message operations.
 *
 * <p>This model contains all the fields required for creating or updating a message. It includes
 * validation constraints and business logic requirements.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class MessageRequestModel extends PaginationBaseRequestModel {

  private Long messageId;
  private String title;
  private LocalDateTime publishDate;
  private String descriptionHtml;
  private Boolean sendAsEmail;
  private Boolean isDeleted;
  private Long createdByUserId;
  private String sendgridEmailBatchId;
  private String notes;
  private Long auditUserId;

  // Additional fields for creation/update
  private List<Long> userGroupIds;
  private List<Long> userIds;
}
