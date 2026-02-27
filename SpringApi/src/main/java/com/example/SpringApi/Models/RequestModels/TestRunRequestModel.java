package com.example.springapi.models.requestmodels;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/** Request model for saving a test run with its results. */
@Getter
@Setter
public class TestRunRequestModel {

  /** Service name being tested (e.g., "AddressService"). */
  private String serviceName;

  /** Run type: SINGLE_METHOD, SERVICE, ALL_SERVICES. */
  private String runType;

  /** Environment where tests were run. */
  private String environment = "localhost";

  /** Total duration of the test run in milliseconds. */
  private Long totalDurationMs;

  /** List of individual test results. */
  private List<TestResultData> results;

  /** Inner class for individual test result data. */
  @Getter
  @Setter
  public static class TestResultData {
    /** Service method being tested. */
    private String methodName;

    /** Test class name. */
    private String testClassName;

    /** Test method name. */
    private String testMethodName;

    /** Display name from @DisplayName annotation. */
    private String displayName;

    /** Status: PASSED, FAILED, SKIPPED, ERROR. */
    private String status;

    /** Duration in milliseconds. */
    private Integer durationMs;

    /** Error message if failed. */
    private String errorMessage;

    /** Stack trace if failed. */
    private String stackTrace;
  }
}
