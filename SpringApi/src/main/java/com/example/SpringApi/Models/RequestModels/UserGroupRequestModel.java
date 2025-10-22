package com.example.SpringApi.Models.RequestModels;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserGroupRequestModel extends PaginationBaseRequestModel {
    
    private Long groupId;
    private Long clientId;
    
    private String groupName;
    private String description;
    private Boolean isDeleted;

    private List<Long> userIds;
    private List<Long> selectedGroupIds;
}
