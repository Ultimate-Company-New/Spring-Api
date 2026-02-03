package com.example.SpringApi.Services.Interface;

import com.example.SpringApi.Models.ResponseModels.TestExecutionStatusModel;

/**
 * Interface for asynchronous test execution.
 * Manages test run lifecycle: status creation, storage, progress tracking, and Maven-based execution.
 * Used by QAService to run tests in the background and poll for status.
 */
public interface ITestExecutorService {

    /**
     * Gets existing status for an execution, or creates a new PENDING status if none exists.
     *
     * @param executionId Unique execution identifier
     * @return Status model (existing or newly created)
     */
    TestExecutionStatusModel getOrCreateStatus(String executionId);

    /**
     * Stores or overwrites the status for an execution.
     *
     * @param executionId Unique execution identifier
     * @param status Status to store
     */
    void storeStatus(String executionId, TestExecutionStatusModel status);

    /**
     * Sets the expected total test count for an execution.
     * Enables accurate progress percentage calculation during RUNNING.
     *
     * @param executionId Unique execution identifier
     * @param expectedCount Expected number of tests
     */
    void setExpectedTestCount(String executionId, int expectedCount);

    /**
     * Gets the current status for an execution.
     * For RUNNING status, returns a snapshot with time-based progress estimation for smoother UX.
     *
     * @param executionId Unique execution identifier
     * @return Status model, or null if execution not found
     */
    TestExecutionStatusModel getStatus(String executionId);

    /**
     * Starts asynchronous test execution via Maven.
     * Returns immediately; tests run in background. Poll getStatus for progress.
     *
     * @param executionId Unique execution identifier
     * @param testClassName Test class to run (e.g. "LeadServiceTest"), or null for all
     * @param testMethodFilter Specific test method, or null for entire class
     * @param serviceName Service name for display
     */
    void executeTestsAsync(String executionId, String testClassName, String testMethodFilter, String serviceName);
}
