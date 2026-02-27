package com.example.SpringApi.Models.ResponseModels;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Response model for bulk user insert operations.
 *
 * <p>This model contains the results of a bulk user insert operation, including counts of
 * successful and failed insertions, and detailed error information for any failures.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-11-13
 */
@Getter
@Setter
public class BulkUserInsertResponseModel {

  private int totalRequested;
  private int successCount;
  private int failureCount;
  private List<UserInsertResult> results;

  /** Default constructor. */
  public BulkUserInsertResponseModel() {
    this.results = new ArrayList<>();
  }

  /** Constructor with counts. */
  public BulkUserInsertResponseModel(int totalRequested, int successCount, int failureCount) {
    this.totalRequested = totalRequested;
    this.successCount = successCount;
    this.failureCount = failureCount;
    this.results = new ArrayList<>();
  }

  /** Adds a successful user insert result. */
  public void addSuccess(String email, Long userId) {
    UserInsertResult result = new UserInsertResult();
    result.setEmail(email);
    result.setUserId(userId);
    result.setSuccess(true);
    this.results.add(result);
  }

  /** Adds a failed user insert result. */
  public void addFailure(String email, String errorMessage) {
    UserInsertResult result = new UserInsertResult();
    result.setEmail(email);
    result.setSuccess(false);
    result.setErrorMessage(errorMessage);
    this.results.add(result);
  }

  /** Inner class representing the result of a single user insert. */
  @Getter
  @Setter
  public static class UserInsertResult {
    private String email;
    private Long userId;
    private boolean success;
    private String errorMessage;
  }
}
