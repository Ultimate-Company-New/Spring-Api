package com.example.SpringApi.Models.DTOs;

import com.example.SpringApi.Models.DatabaseModels.Message;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for Message with targeted user IDs and user group IDs.
 * Used to efficiently fetch messages along with their targeting information.
 */
@Getter
@Setter
public class MessageWithTargetsDTO {
    private Message message;
    private List<Long> userIds;
    private List<Long> userGroupIds;

    public MessageWithTargetsDTO() {
        this.userIds = new ArrayList<>();
        this.userGroupIds = new ArrayList<>();
    }

    public MessageWithTargetsDTO(Message message) {
        this.message = message;
        this.userIds = new ArrayList<>();
        this.userGroupIds = new ArrayList<>();
    }

    public void addUserId(Long userId) {
        if (userId != null && !this.userIds.contains(userId)) {
            this.userIds.add(userId);
        }
    }

    public void addUserGroupId(Long userGroupId) {
        if (userGroupId != null && !this.userGroupIds.contains(userGroupId)) {
            this.userGroupIds.add(userGroupId);
        }
    }
}

