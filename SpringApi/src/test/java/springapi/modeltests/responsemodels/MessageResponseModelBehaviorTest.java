package springapi.modeltests.responsemodels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import springapi.models.databasemodels.Message;
import springapi.models.databasemodels.MessageUserGroupMap;
import springapi.models.databasemodels.MessageUserMap;
import springapi.models.databasemodels.User;
import springapi.models.responsemodels.MessageResponseModel;

@DisplayName("Message Response Model Behavior Tests")
class MessageResponseModelBehaviorTest {

  // Total Tests: 4

  /**
   * Purpose: Verify constructor maps core fields and computes draft state when publish date is
   * null. Expected Result: Draft status, not published, editable, and daysOld computed from
   * createdAt. Assertions: Core and computed fields are set correctly.
   */
  @Test
  @DisplayName("messageResponseModel - DraftState Computes Expected Fields - Success")
  void messageResponseModel_s01_draftStateComputesExpectedFields_success() {
    // Arrange
    Message message = buildBaseMessage();
    message.setPublishDate(null);
    message.setIsDeleted(false);

    // Act
    MessageResponseModel responseModel = new MessageResponseModel(message);

    // Assert
    assertEquals("Draft", responseModel.getStatusText());
    assertFalse(responseModel.getIsPublished());
    assertTrue(responseModel.getCanEdit());
    assertTrue(responseModel.getDaysOld() >= 0);
    assertNotNull(responseModel.getCreatedByUser());
  }

  /**
   * Purpose: Verify future publish date yields scheduled state. Expected Result: Message is not yet
   * published and status is Scheduled. Assertions: isPublished false and statusText Scheduled.
   */
  @Test
  @DisplayName("messageResponseModel - FuturePublishDate ProducesScheduledState - Success")
  void messageResponseModel_s02_futurePublishDateProducesScheduledState_success() {
    // Arrange
    Message message = buildBaseMessage();
    message.setPublishDate(LocalDateTime.now().plusDays(2));
    message.setIsDeleted(false);

    // Act
    MessageResponseModel responseModel = new MessageResponseModel(message);

    // Assert
    assertFalse(responseModel.getIsPublished());
    assertEquals("Scheduled", responseModel.getStatusText());
    assertTrue(responseModel.getCanEdit());
  }

  /**
   * Purpose: Verify past publish date and long title produce published state and truncated title
   * preview. Expected Result: Published status with title preview ending in ellipsis. Assertions:
   * isPublished true, status Published, preview length and suffix are correct.
   */
  @Test
  @DisplayName(
      "messageResponseModel - PastPublishDate ProducesPublishedAndTruncatedPreview - Success")
  void messageResponseModel_s03_pastPublishDateProducesPublishedAndTruncatedPreview_success() {
    // Arrange
    Message message = buildBaseMessage();
    message.setTitle(
        "This is a deliberately long title that should be truncated in preview output");
    message.setPublishDate(LocalDateTime.now().minusDays(1));
    message.setIsDeleted(false);

    // Act
    MessageResponseModel responseModel = new MessageResponseModel(message);

    // Assert
    assertTrue(responseModel.getIsPublished());
    assertEquals("Published", responseModel.getStatusText());
    assertFalse(responseModel.getCanEdit());
    assertNotNull(responseModel.getTitlePreview());
    assertTrue(responseModel.getTitlePreview().endsWith("..."));
    assertEquals(50, responseModel.getTitlePreview().length());
  }

  /**
   * Purpose: Verify deleted message state and recipient ID extraction from mapping collections.
   * Expected Result: Deleted status takes precedence and user/group recipient IDs are extracted.
   * Assertions: Deleted status, canEdit false, and extracted IDs match source mappings.
   */
  @Test
  @DisplayName("messageResponseModel - DeletedState ExtractsRecipientIds - Success")
  void messageResponseModel_s04_deletedStateExtractsRecipientIds_success() {
    // Arrange
    Message message = buildBaseMessage();
    message.setPublishDate(LocalDateTime.now().plusHours(1));
    message.setIsDeleted(true);

    MessageUserMap userMapOne = new MessageUserMap();
    userMapOne.setUserId(101L);
    MessageUserMap userMapTwo = new MessageUserMap();
    userMapTwo.setUserId(102L);
    message.setMessageUserMaps(List.of(userMapOne, userMapTwo));

    MessageUserGroupMap groupMapOne = new MessageUserGroupMap();
    groupMapOne.setGroupId(201L);
    MessageUserGroupMap groupMapTwo = new MessageUserGroupMap();
    groupMapTwo.setGroupId(202L);
    message.setMessageUserGroupMaps(List.of(groupMapOne, groupMapTwo));

    // Act
    MessageResponseModel responseModel = new MessageResponseModel(message);

    // Assert
    assertEquals("Deleted", responseModel.getStatusText());
    assertFalse(responseModel.getCanEdit());
    assertEquals(List.of(101L, 102L), responseModel.getUserIds());
    assertEquals(List.of(201L, 202L), responseModel.getUserGroupIds());
    assertNull(responseModel.getAuditUser());
  }

  private Message buildBaseMessage() {
    Message message = new Message();
    message.setMessageId(1L);
    message.setTitle("Weekly Update");
    message.setDescriptionHtml("<p>Body</p>");
    message.setSendAsEmail(true);
    message.setIsDeleted(false);
    message.setCreatedByUserId(77L);
    message.setSendgridEmailBatchId("batch-1");
    message.setCreatedAt(LocalDateTime.now().minusDays(3));
    message.setUpdatedAt(LocalDateTime.now().minusDays(1));
    message.setCreatedUser("creator");
    message.setModifiedUser("modifier");
    message.setNotes("note");

    User createdBy = new User();
    createdBy.setUserId(77L);
    createdBy.setFirstName("Alex");
    createdBy.setLastName("Doe");
    createdBy.setEmail("alex@example.com");
    createdBy.setLoginName("alex.doe");
    message.setCreatedByUser(createdBy);

    return message;
  }
}
