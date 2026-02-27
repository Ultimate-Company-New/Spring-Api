package com.example.springapi.models.responsemodels;

import com.example.springapi.models.databasemodels.TestRun;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** Response model for test run data. */
@Getter
@Setter
public class TestRunResponseModel {

  private Long testRunId;
  private String serviceName;
  private String runType;
  private Long userId;
  private String userName;
  private String environment;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Integer totalTests;
  private Integer passedCount;
  private Integer failedCount;
  private Integer skippedCount;
  private String status;
  private LocalDateTime createdDate;

  public TestRunResponseModel() {}

  /**
   * Executes test run response model.
   */
  public TestRunResponseModel(TestRun testRun) {
    this.testRunId = testRun.getTestRunId();
    this.serviceName = testRun.getServiceName();
    this.runType = testRun.getRunType();
    this.userId = testRun.getUserId();
    this.userName = testRun.getUserName();
    this.environment = testRun.getEnvironment();
    this.startTime = testRun.getStartTime();
    this.endTime = testRun.getEndTime();
    this.totalTests = testRun.getTotalTests();
    this.passedCount = testRun.getPassedCount();
    this.failedCount = testRun.getFailedCount();
    this.skippedCount = testRun.getSkippedCount();
    this.status = testRun.getStatus();
    this.createdDate = testRun.getCreatedDate();
  }
}
