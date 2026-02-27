package com.example.SpringApi.Models.RequestModels;

import lombok.Getter;
import lombok.Setter;

/**
 * Request model for task item operations.
 *
 * <p>This model contains all the fields required for creating or updating a task item. It includes
 * validation constraints and business logic requirements.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Getter
@Setter
public class TodoRequestModel extends PaginationBaseRequestModel {

  private Long todoId;
  private String task;
  private Boolean isDone;
}

