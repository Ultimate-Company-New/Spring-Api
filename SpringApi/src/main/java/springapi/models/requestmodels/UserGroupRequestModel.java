package springapi.models.requestmodels;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** Represents the user group request model component. */
@Getter
@Setter
public class UserGroupRequestModel extends PaginationBaseRequestModel {

  private Long groupId;

  private String groupName;
  private String description;
  private String notes;
  private Boolean isDeleted;

  private List<Long> userIds;
  private List<Long> selectedGroupIds;
}
