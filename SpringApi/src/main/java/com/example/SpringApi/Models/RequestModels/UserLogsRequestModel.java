package com.example.SpringApi.Models.RequestModels;

import lombok.Getter;
import lombok.Setter;

/**
 * Request model for fetching user logs in batches.
 * Extends PaginationBaseRequestModel to include pagination parameters.
 */
@Getter
@Setter
public class UserLogsRequestModel extends PaginationBaseRequestModel {
    private long userId;
    private long carrierId;
}