package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.ResponseModels.TestExecutionStatusModel;
import com.example.SpringApi.Services.TestExecutorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TestExecutorService.
 * Tests async test execution, status management, and progress tracking.
 */
@DisplayName("TestExecutorService Tests")
class TestExecutorServiceTest extends BaseTest {

    @InjectMocks
    private TestExecutorService testExecutorService;

    private String testExecutionId;

    @BeforeEach
    void setUp() {
        super.setUp();
        testExecutionId = "test-execution-123";
    }

    // ==================== GET OR CREATE STATUS TESTS ====================

    @Test
    @DisplayName("getOrCreateStatus - New Execution - Creates New Status")
    void getOrCreateStatus_NewExecution_CreatesNewStatus() {
        // Act
        TestExecutionStatusModel result = testExecutorService.getOrCreateStatus(testExecutionId);

        // Assert
        assertNotNull(result);
        assertEquals(testExecutionId, result.getExecutionId());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    @DisplayName("getOrCreateStatus - Existing Execution - Returns Existing Status")
    void getOrCreateStatus_ExistingExecution_ReturnsExistingStatus() {
        // Arrange
        TestExecutionStatusModel firstCall = testExecutorService.getOrCreateStatus(testExecutionId);
        firstCall.setStatus("RUNNING");

        // Act
        TestExecutionStatusModel secondCall = testExecutorService.getOrCreateStatus(testExecutionId);

        // Assert
        assertNotNull(secondCall);
        assertEquals("RUNNING", secondCall.getStatus());
        assertEquals(testExecutionId, secondCall.getExecutionId());
    }

    @Test
    @DisplayName("getOrCreateStatus - Multiple Executions - Maintains Separate States")
    void getOrCreateStatus_MultipleExecutions_MaintainsSeparateStates() {
        // Arrange
        String execution1 = "exec-1";
        String execution2 = "exec-2";

        // Act
        TestExecutionStatusModel status1 = testExecutorService.getOrCreateStatus(execution1);
        TestExecutionStatusModel status2 = testExecutorService.getOrCreateStatus(execution2);
        
        status1.setStatus("RUNNING");
        status2.setStatus("COMPLETED");

        // Assert
        assertNotEquals(status1.getExecutionId(), status2.getExecutionId());
        assertEquals("RUNNING", testExecutorService.getStatus(execution1).getStatus());
        assertEquals("COMPLETED", testExecutorService.getStatus(execution2).getStatus());
    }

    // ==================== STORE STATUS TESTS ====================

    @Test
    @DisplayName("storeStatus - Valid Status - Stores Successfully")
    void storeStatus_ValidStatus_StoresSuccessfully() {
        // Arrange
        TestExecutionStatusModel status = new TestExecutionStatusModel();
        status.setExecutionId(testExecutionId);
        status.setStatus("RUNNING");
        status.setServiceName("AddressService");

        // Act
        testExecutorService.storeStatus(testExecutionId, status);

        // Assert
        TestExecutionStatusModel retrieved = testExecutorService.getStatus(testExecutionId);
        assertNotNull(retrieved);
        assertEquals("RUNNING", retrieved.getStatus());
        assertEquals("AddressService", retrieved.getServiceName());
    }

    @Test
    @DisplayName("storeStatus - Overwrite Existing - Updates Status")
    void storeStatus_OverwriteExisting_UpdatesStatus() {
        // Arrange
        TestExecutionStatusModel status1 = new TestExecutionStatusModel();
        status1.setExecutionId(testExecutionId);
        status1.setStatus("PENDING");
        testExecutorService.storeStatus(testExecutionId, status1);

        TestExecutionStatusModel status2 = new TestExecutionStatusModel();
        status2.setExecutionId(testExecutionId);
        status2.setStatus("RUNNING");

        // Act
        testExecutorService.storeStatus(testExecutionId, status2);

        // Assert
        TestExecutionStatusModel retrieved = testExecutorService.getStatus(testExecutionId);
        assertEquals("RUNNING", retrieved.getStatus());
    }

    // ==================== SET EXPECTED TEST COUNT TESTS ====================

    @Test
    @DisplayName("setExpectedTestCount - Existing Status - Updates Count")
    void setExpectedTestCount_ExistingStatus_UpdatesCount() {
        // Arrange
        TestExecutionStatusModel status = testExecutorService.getOrCreateStatus(testExecutionId);

        // Act
        testExecutorService.setExpectedTestCount(testExecutionId, 50);

        // Assert
        TestExecutionStatusModel retrieved = testExecutorService.getStatus(testExecutionId);
        assertEquals(50, retrieved.getTotalTests());
    }

    @Test
    @DisplayName("setExpectedTestCount - Non-Existing Status - Does Nothing")
    void setExpectedTestCount_NonExistingStatus_DoesNothing() {
        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> {
            testExecutorService.setExpectedTestCount("non-existent-id", 100);
        });
    }

    @Test
    @DisplayName("setExpectedTestCount - Zero Count - Sets Zero")
    void setExpectedTestCount_ZeroCount_SetsZero() {
        // Arrange
        testExecutorService.getOrCreateStatus(testExecutionId);

        // Act
        testExecutorService.setExpectedTestCount(testExecutionId, 0);

        // Assert
        TestExecutionStatusModel retrieved = testExecutorService.getStatus(testExecutionId);
        assertEquals(0, retrieved.getTotalTests());
    }

    // ==================== GET STATUS TESTS ====================

    @Test
    @DisplayName("getStatus - Non-Existing ID - Returns Null")
    void getStatus_NonExistingId_ReturnsNull() {
        // Act
        TestExecutionStatusModel result = testExecutorService.getStatus("non-existent-id");

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("getStatus - Pending Status - Returns Without Estimation")
    void getStatus_PendingStatus_ReturnsWithoutEstimation() {
        // Arrange
        TestExecutionStatusModel status = new TestExecutionStatusModel();
        status.setExecutionId(testExecutionId);
        status.setStatus("PENDING");
        testExecutorService.storeStatus(testExecutionId, status);

        // Act
        TestExecutionStatusModel result = testExecutorService.getStatus(testExecutionId);

        // Assert
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    @DisplayName("getStatus - Completed Status - Returns Actual Values")
    void getStatus_CompletedStatus_ReturnsActualValues() {
        // Arrange
        TestExecutionStatusModel status = new TestExecutionStatusModel();
        status.setExecutionId(testExecutionId);
        status.setStatus("COMPLETED");
        status.setTotalTests(10);
        status.setCompletedTests(10);
        status.setPassedTests(9);
        status.setFailedTests(1);
        testExecutorService.storeStatus(testExecutionId, status);

        // Act
        TestExecutionStatusModel result = testExecutorService.getStatus(testExecutionId);

        // Assert
        assertNotNull(result);
        assertEquals("COMPLETED", result.getStatus());
        assertEquals(10, result.getTotalTests());
        assertEquals(10, result.getCompletedTests());
        assertEquals(9, result.getPassedTests());
        assertEquals(1, result.getFailedTests());
    }

    @Test
    @DisplayName("getStatus - Running Status With Progress - Applies Time Estimation")
    void getStatus_RunningStatusWithProgress_AppliesTimeEstimation() {
        // Arrange
        TestExecutionStatusModel status = new TestExecutionStatusModel();
        status.setExecutionId(testExecutionId);
        status.setStatus("RUNNING");
        status.setStartedAt(LocalDateTime.now().minusSeconds(2)); // Started 2 seconds ago
        status.setTotalTests(100);
        status.setCompletedTests(10);

        testExecutorService.storeStatus(testExecutionId, status);

        // Act
        TestExecutionStatusModel result = testExecutorService.getStatus(testExecutionId);

        // Assert
        assertNotNull(result);
        assertEquals("RUNNING", result.getStatus());
        assertEquals(100, result.getTotalTests());
        // Estimated completed should be >= actual completed (time-based estimation)
        assertTrue(result.getCompletedTests() >= 10);
        // But should never reach 100% while running (max 99)
        assertTrue(result.getCompletedTests() < 100);
    }

    @Test
    @DisplayName("getStatus - Running Status Without Total - Does Not Apply Estimation")
    void getStatus_RunningStatusWithoutTotal_DoesNotApplyEstimation() {
        // Arrange
        TestExecutionStatusModel status = new TestExecutionStatusModel();
        status.setExecutionId(testExecutionId);
        status.setStatus("RUNNING");
        status.setStartedAt(LocalDateTime.now());
        status.setTotalTests(0); // No total set

        testExecutorService.storeStatus(testExecutionId, status);

        // Act
        TestExecutionStatusModel result = testExecutorService.getStatus(testExecutionId);

        // Assert
        assertNotNull(result);
        assertEquals("RUNNING", result.getStatus());
        assertEquals(0, result.getTotalTests());
    }

    @Test
    @DisplayName("getStatus - Failed Status - Returns Error Information")
    void getStatus_FailedStatus_ReturnsErrorInformation() {
        // Arrange
        TestExecutionStatusModel status = new TestExecutionStatusModel();
        status.setExecutionId(testExecutionId);
        status.setStatus("FAILED");
        status.setErrorMessage("Maven build failed");
        testExecutorService.storeStatus(testExecutionId, status);

        // Act
        TestExecutionStatusModel result = testExecutorService.getStatus(testExecutionId);

        // Assert
        assertNotNull(result);
        assertEquals("FAILED", result.getStatus());
        assertEquals("Maven build failed", result.getErrorMessage());
    }

    // ==================== STATUS PROGRESSION TESTS ====================

    @Test
    @DisplayName("Status Progression - PENDING to RUNNING - Updates Correctly")
    void statusProgression_PendingToRunning_UpdatesCorrectly() {
        // Arrange
        TestExecutionStatusModel status = testExecutorService.getOrCreateStatus(testExecutionId);
        assertEquals("PENDING", status.getStatus());

        // Act
        status.setStatus("RUNNING");
        status.setStartedAt(LocalDateTime.now());
        testExecutorService.storeStatus(testExecutionId, status);

        // Assert
        TestExecutionStatusModel retrieved = testExecutorService.getStatus(testExecutionId);
        assertEquals("RUNNING", retrieved.getStatus());
        assertNotNull(retrieved.getStartedAt());
    }

    @Test
    @DisplayName("Status Progression - RUNNING to COMPLETED - Updates Correctly")
    void statusProgression_RunningToCompleted_UpdatesCorrectly() {
        // Arrange
        TestExecutionStatusModel status = testExecutorService.getOrCreateStatus(testExecutionId);
        status.setStatus("RUNNING");
        status.setStartedAt(LocalDateTime.now().minusSeconds(5));
        testExecutorService.storeStatus(testExecutionId, status);

        // Act
        status.setStatus("COMPLETED");
        status.setCompletedAt(LocalDateTime.now());
        status.setTotalTests(50);
        status.setCompletedTests(50);
        status.setPassedTests(48);
        status.setFailedTests(2);
        testExecutorService.storeStatus(testExecutionId, status);

        // Assert
        TestExecutionStatusModel retrieved = testExecutorService.getStatus(testExecutionId);
        assertEquals("COMPLETED", retrieved.getStatus());
        assertNotNull(retrieved.getCompletedAt());
        assertEquals(50, retrieved.getTotalTests());
        assertEquals(48, retrieved.getPassedTests());
        assertEquals(2, retrieved.getFailedTests());
    }

    @Test
    @DisplayName("Status Progression - Test Results - Accumulates Correctly")
    void statusProgression_TestResults_AccumulatesCorrectly() {
        // Arrange
        TestExecutionStatusModel status = testExecutorService.getOrCreateStatus(testExecutionId);
        status.setTotalTests(10);

        // Act - Simulate progressive updates
        status.setCompletedTests(3);
        status.setPassedTests(3);
        testExecutorService.storeStatus(testExecutionId, status);

        status.setCompletedTests(6);
        status.setPassedTests(5);
        status.setFailedTests(1);
        testExecutorService.storeStatus(testExecutionId, status);

        status.setCompletedTests(10);
        status.setPassedTests(8);
        status.setFailedTests(2);
        testExecutorService.storeStatus(testExecutionId, status);

        // Assert
        TestExecutionStatusModel retrieved = testExecutorService.getStatus(testExecutionId);
        assertEquals(10, retrieved.getCompletedTests());
        assertEquals(8, retrieved.getPassedTests());
        assertEquals(2, retrieved.getFailedTests());
    }

    // ==================== DURATION TRACKING TESTS ====================

    @Test
    @DisplayName("Duration Tracking - Running Test - Calculates Elapsed Time")
    void durationTracking_RunningTest_CalculatesElapsedTime() {
        // Arrange
        TestExecutionStatusModel status = testExecutorService.getOrCreateStatus(testExecutionId);
        status.setStatus("RUNNING");
        status.setStartedAt(LocalDateTime.now().minusSeconds(3));
        status.setTotalTests(50);
        testExecutorService.storeStatus(testExecutionId, status);

        // Act
        TestExecutionStatusModel result = testExecutorService.getStatus(testExecutionId);

        // Assert
        assertNotNull(result.getDurationMs());
        assertTrue(result.getDurationMs() >= 2000); // At least 2 seconds
    }

    @Test
    @DisplayName("Duration Tracking - Completed Test - Has Final Duration")
    void durationTracking_CompletedTest_HasFinalDuration() {
        // Arrange
        TestExecutionStatusModel status = testExecutorService.getOrCreateStatus(testExecutionId);
        status.setStatus("COMPLETED");
        status.setDurationMs(5000L);
        testExecutorService.storeStatus(testExecutionId, status);

        // Act
        TestExecutionStatusModel result = testExecutorService.getStatus(testExecutionId);

        // Assert
        assertEquals(5000L, result.getDurationMs());
    }
}
