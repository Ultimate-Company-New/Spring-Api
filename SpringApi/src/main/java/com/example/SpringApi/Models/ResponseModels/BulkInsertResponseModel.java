package com.example.SpringApi.Models.ResponseModels;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Generic response model for bulk insert operations.
 *
 * <p>This model contains the results of a bulk insert operation, including counts of successful and
 * failed insertions, and detailed error information for any failures.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-11-13
 */
@Getter
@Setter
public class BulkInsertResponseModel<T> {

  private int totalRequested;
  private int successCount;
  private int failureCount;
  private List<InsertResult<T>> results;

  /** Default constructor. */
  public BulkInsertResponseModel() {
    this.results = new ArrayList<>();
  }

  /** Constructor with counts. */
  public BulkInsertResponseModel(int totalRequested, int successCount, int failureCount) {
    this.totalRequested = totalRequested;
    this.successCount = successCount;
    this.failureCount = failureCount;
    this.results = new ArrayList<>();
  }

  /** Adds a successful insert result. */
  public void addSuccess(String identifier, T entityId) {
    InsertResult<T> result = new InsertResult<>();
    result.setIdentifier(identifier);
    result.setEntityId(entityId);
    result.setSuccess(true);
    this.results.add(result);
  }

  /** Adds a failed insert result. */
  public void addFailure(String identifier, String errorMessage) {
    InsertResult<T> result = new InsertResult<>();
    result.setIdentifier(identifier);
    result.setSuccess(false);
    result.setErrorMessage(errorMessage);
    this.results.add(result);
  }

  /** Inner class representing the result of a single insert. */
  @Getter
  @Setter
  public static class InsertResult<T> {
    private String identifier;
    private T entityId;
    private boolean success;
    private String errorMessage;
  }
}

