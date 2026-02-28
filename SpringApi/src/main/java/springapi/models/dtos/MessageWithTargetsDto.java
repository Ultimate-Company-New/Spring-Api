package springapi.models.dtos;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import springapi.models.databasemodels.Message;

/**
 * DTO for Message with targeted user IDs and user group IDs. Used to efficiently fetch messages.
 * along with their targeting information.
 */
@Getter
@Setter
public class MessageWithTargetsDto {
  private Message message;
  private List<Long> userIds;
  private List<Long> userGroupIds;

  public MessageWithTargetsDto() {
    this.userIds = new ArrayList<>();
    this.userGroupIds = new ArrayList<>();
  }

  /** Executes message with targets dto. */
  public MessageWithTargetsDto(Message message) {
    this.message = message;
    this.userIds = new ArrayList<>();
    this.userGroupIds = new ArrayList<>();
  }

  /** Executes add user id. */
  public void addUserId(Long userId) {
    if (userId != null && !this.userIds.contains(userId)) {
      this.userIds.add(userId);
    }
  }

  /** Executes add user group id. */
  public void addUserGroupId(Long userGroupId) {
    if (userGroupId != null && !this.userGroupIds.contains(userGroupId)) {
      this.userGroupIds.add(userGroupId);
    }
  }
}
