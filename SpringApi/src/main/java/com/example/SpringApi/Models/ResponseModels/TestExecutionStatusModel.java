package com.example.SpringApi.Models.ResponseModels;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Response model for test execution status and progress.
 * Used for real-time progress tracking during async test execution.
 */
@Getter
@Setter
public class TestExecutionStatusModel {

    public TestExecutionStatusModel() {
    }

    public TestExecutionStatusModel(String executionId, String serviceName, String methodName, int totalTests) {
        this.executionId = executionId;
        this.status = "PENDING";
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.startedAt = LocalDateTime.now();
        this.totalTests = totalTests;
        this.completedTests = 0;
    }

    /**
     * Unique identifier for this test execution
     */
    private String executionId;

    /**
     * Current status: PENDING, RUNNING, COMPLETED, FAILED
     */
    private String status;

    /**
     * Total number of tests to run
     */
    private int totalTests;

    /**
     * Number of tests completed so far
     */
    private int completedTests;

    /**
     * Number of tests passed
     */
    private int passedTests;

    /**
     * Number of tests failed
     */
    private int failedTests;

    /**
     * Number of tests skipped
     */
    private int skippedTests;

    /**
     * Total duration in milliseconds
     */
    private long durationMs;

    /**
     * When the execution started
     */
    private LocalDateTime startedAt;

    /**
     * When the execution completed (null if still running)
     */
    private LocalDateTime completedAt;

    /**
     * Service being tested
     */
    private String serviceName;

    /**
     * Method being tested (if specific method)
     */
    private String methodName;

    /**
     * Individual test results
     */
    private List<TestResultInfo> results = new ArrayList<>();

    /**
     * Error message if execution failed
     */
    private String errorMessage;

    /**
     * Progress percentage (0-100)
     */
    public int getProgressPercentage() {
        if (totalTests == 0)
            return 0;
        return (int) Math.round((double) completedTests / totalTests * 100);
    }

    /**
     * Individual test result information
     */
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

        public TestResultInfo() {
        }

        public TestResultInfo(String testMethodName, String status, long durationMs) {
            this.testMethodName = testMethodName;
            this.status = status;
            this.durationMs = durationMs;
        }
    }
}
