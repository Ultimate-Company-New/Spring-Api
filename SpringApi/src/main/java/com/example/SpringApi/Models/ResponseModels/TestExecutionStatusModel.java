package com.example.SpringApi.Models.ResponseModels;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Response model for test execution status and progress. Used for real-time progress tracking
 * during async test execution.
 */
@Getter
@Setter
public class TestExecutionStatusModel {

  public TestExecutionStatusModel() {}

  public TestExecutionStatusModel(
      String executionId, String serviceName, String methodName, int totalTests) {
    this.executionId = executionId;
    this.status = "PENDING";
    this.serviceName = serviceName;
    this.methodName = methodName;
    this.startedAt = LocalDateTime.now();
    this.totalTests = totalTests;
    this.completedTests = 0;
  }

  /** Creates a status with only executionId and PENDING status (for getOrCreateStatus). */
  public TestExecutionStatusModel(String executionId) {
    this.executionId = executionId;
    this.status = "PENDING";
  }

  /**
   * Creates a progress snapshot with estimated completed count for smoother UX during RUNNING.
   * Copies all fields from source and overrides progress-related fields with estimated values.
   */
  public static TestExecutionStatusModel createProgressSnapshot(
      TestExecutionStatusModel source, int totalTests, int smoothedCompleted, long elapsedMs) {
    TestExecutionStatusModel snapshot = new TestExecutionStatusModel();
    snapshot.setExecutionId(source.getExecutionId());
    snapshot.setStatus(source.getStatus());
    snapshot.setServiceName(source.getServiceName());
    snapshot.setMethodName(source.getMethodName());
    snapshot.setStartedAt(source.getStartedAt());
    snapshot.setCompletedAt(source.getCompletedAt());
    snapshot.setTotalTests(totalTests);
    snapshot.setCompletedTests(smoothedCompleted);
    snapshot.setPassedTests(source.getPassedTests());
    snapshot.setFailedTests(source.getFailedTests());
    snapshot.setSkippedTests(source.getSkippedTests());
    snapshot.setDurationMs(elapsedMs);
    snapshot.setErrorMessage(source.getErrorMessage());
    snapshot.getResults().addAll(source.getResults());
    return snapshot;
  }

  /**
   * Updates totalTests, completedTests, passedTests, failedTests, skippedTests from the results
   * list.
   */
  public void updateTotalsFromResults() {
    this.totalTests = results.size();
    this.completedTests = results.size();
    this.passedTests = (int) results.stream().filter(r -> "PASSED".equals(r.getStatus())).count();
    this.failedTests = (int) results.stream().filter(r -> "FAILED".equals(r.getStatus())).count();
    this.skippedTests = (int) results.stream().filter(r -> "SKIPPED".equals(r.getStatus())).count();
  }

  /** Unique identifier for this test execution */
  private String executionId;

  /** Current status: PENDING, RUNNING, COMPLETED, FAILED */
  private String status;

  /** Total number of tests to run */
  private int totalTests;

  /** Number of tests completed so far */
  private int completedTests;

  /** Number of tests passed */
  private int passedTests;

  /** Number of tests failed */
  private int failedTests;

  /** Number of tests skipped */
  private int skippedTests;

  /** Total duration in milliseconds */
  private long durationMs;

  /** When the execution started */
  private LocalDateTime startedAt;

  /** When the execution completed (null if still running) */
  private LocalDateTime completedAt;

  /** Service being tested */
  private String serviceName;

  /** Method being tested (if specific method) */
  private String methodName;

  /** Individual test results */
  private List<TestResultInfo> results = new ArrayList<>();

  /** Error message if execution failed */
  private String errorMessage;

  /** Progress percentage (0-100) */
  public int getProgressPercentage() {
    if (totalTests == 0) return 0;
    return (int) Math.round((double) completedTests / totalTests * 100);
  }

  /** Individual test result information */
  @Getter
  @Setter
  public static class TestResultInfo {
    private String testMethodName;
    private String displayName;
    private String status; // PASSED, FAILED, SKIPPED
    private long durationMs;
    private String errorMessage;
    private String stackTrace;
    private String methodName; // The service method this test covers

    public TestResultInfo() {}

    public TestResultInfo(String testMethodName, String status, long durationMs) {
      this.testMethodName = testMethodName;
      this.status = status;
      this.durationMs = durationMs;
    }
  }
}
