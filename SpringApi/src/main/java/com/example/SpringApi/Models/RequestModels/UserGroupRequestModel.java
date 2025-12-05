package com.example.SpringApi.Models.RequestModels;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

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
