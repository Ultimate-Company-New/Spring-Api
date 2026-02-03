package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.ResponseModels.TestExecutionStatusModel;
import com.example.SpringApi.Services.TestExecutorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TestExecutorService.
 *
 * Test Group Summary:
 * | Group Name                              | Number of Tests |
 * | :-------------------------------------- | :-------------- |
 * | GetOrCreateStatusTests                  | 3               |
 * | StoreStatusTests                        | 2               |
 * | SetExpectedTestCountTests               | 3               |
 * | GetStatusTests                          | 6               |
 * | StatusProgressionTests                  | 3               |
 * | DurationTrackingTests                   | 2               |
 * | **Total**                               | **19**          |
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TestExecutorService Unit Tests")
class TestExecutorServiceTest extends BaseTest {

    @InjectMocks
    private TestExecutorService testExecutorService;

    private String testExecutionId;

    @BeforeEach
    void setUp() throws Exception {
        testExecutionId = "test-execution-123";
        Field field = TestExecutorService.class.getDeclaredField("activeExecutions");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, TestExecutionStatusModel> map = (Map<String, TestExecutionStatusModel>) field.get(null);
        map.clear();
    }

    @Nested
    @DisplayName("getOrCreateStatus Tests")
    class GetOrCreateStatusTests {

        /**
         * Purpose: Verify new execution ID creates PENDING status.
         * Expected Result: Status with executionId and status "PENDING".
         * Assertions: Result non-null, executionId matches, status is PENDING.
         */
        @Test
        @DisplayName("Get Or Create Status - New Execution - Creates New Status")
        void getOrCreateStatus_NewExecution_CreatesNewStatus() {
            // Act
            TestExecutionStatusModel result = testExecutorService.getOrCreateStatus(testExecutionId);

            // Assert
            assertNotNull(result);
            assertEquals(testExecutionId, result.getExecutionId());
            assertEquals("PENDING", result.getStatus());
        }

        /**
         * Purpose: Verify existing execution returns same status (no duplicate creation).
         * Expected Result: Second call returns same object with updated status.
         * Assertions: Status matches first call's update, executionId matches.
         */
        @Test
        @DisplayName("Get Or Create Status - Existing Execution - Returns Existing Status")
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

        /**
         * Purpose: Verify multiple executions maintain separate state.
         * Expected Result: Each execution has independent status.
         * Assertions: Different executionIds, getStatus returns correct status per ID.
         */
        @Test
        @DisplayName("Get Or Create Status - Multiple Executions - Maintains Separate States")
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
    }

    @Nested
    @DisplayName("storeStatus Tests")
    class StoreStatusTests {

        /**
         * Purpose: Verify storeStatus persists status and getStatus retrieves it.
         * Expected Result: Stored status retrievable with correct values.
         * Assertions: Retrieved status matches stored values.
         */
        @Test
        @DisplayName("Store Status - Valid Status - Stores Successfully")
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

        /**
         * Purpose: Verify overwriting existing status updates correctly.
         * Expected Result: New status replaces old, getStatus returns latest.
         * Assertions: Retrieved status is RUNNING (second store).
         */
        @Test
        @DisplayName("Store Status - Overwrite Existing - Updates Status")
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
    }

    @Nested
    @DisplayName("setExpectedTestCount Tests")
    class SetExpectedTestCountTests {

        /**
         * Purpose: Verify setExpectedTestCount updates totalTests for existing status.
         * Expected Result: getStatus returns updated totalTests.
         * Assertions: totalTests equals 50.
         */
        @Test
        @DisplayName("Set Expected Test Count - Existing Status - Updates Count")
        void setExpectedTestCount_ExistingStatus_UpdatesCount() {
            // Arrange
            testExecutorService.getOrCreateStatus(testExecutionId);

            // Act
            testExecutorService.setExpectedTestCount(testExecutionId, 50);

            // Assert
            TestExecutionStatusModel retrieved = testExecutorService.getStatus(testExecutionId);
            assertEquals(50, retrieved.getTotalTests());
        }

        /**
         * Purpose: Verify setExpectedTestCount for non-existent ID does not throw.
         * Expected Result: No exception, no side effect.
         * Assertions: assertDoesNotThrow.
         */
        @Test
        @DisplayName("Set Expected Test Count - Non-Existing Status - Does Nothing")
        void setExpectedTestCount_NonExistingStatus_DoesNothing() {
            // Act & Assert
            assertDoesNotThrow(() -> {
                testExecutorService.setExpectedTestCount("non-existent-id", 100);
            });
        }

        /**
         * Purpose: Verify zero count is set correctly.
         * Expected Result: totalTests equals 0.
         * Assertions: totalTests is 0.
         */
        @Test
        @DisplayName("Set Expected Test Count - Zero Count - Sets Zero")
        void setExpectedTestCount_ZeroCount_SetsZero() {
            // Arrange
            testExecutorService.getOrCreateStatus(testExecutionId);

            // Act
            testExecutorService.setExpectedTestCount(testExecutionId, 0);

            // Assert
            TestExecutionStatusModel retrieved = testExecutorService.getStatus(testExecutionId);
            assertEquals(0, retrieved.getTotalTests());
        }
    }

    @Nested
    @DisplayName("getStatus Tests")
    class GetStatusTests {

        /**
         * Purpose: Verify non-existent execution ID returns null.
         * Expected Result: null returned.
         * Assertions: Result is null.
         */
        @Test
        @DisplayName("Get Status - Non-Existing ID - Returns Null")
        void getStatus_NonExistingId_ReturnsNull() {
            // Act
            TestExecutionStatusModel result = testExecutorService.getStatus("non-existent-id");

            // Assert
            assertNull(result);
        }

        /**
         * Purpose: Verify PENDING status returned without progress estimation.
         * Expected Result: Status returned as-is.
         * Assertions: Status is PENDING.
         */
        @Test
        @DisplayName("Get Status - Pending Status - Returns Without Estimation")
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

        /**
         * Purpose: Verify COMPLETED status returns actual values (no estimation).
         * Expected Result: All counts match stored values.
         * Assertions: totalTests, completedTests, passedTests, failedTests match.
         */
        @Test
        @DisplayName("Get Status - Completed Status - Returns Actual Values")
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

        /**
         * Purpose: Verify RUNNING status with progress applies time-based estimation.
         * Expected Result: completedTests >= actual, < total (smoothing).
         * Assertions: completedTests in valid range.
         */
        @Test
        @DisplayName("Get Status - Running Status With Progress - Applies Time Estimation")
        void getStatus_RunningStatusWithProgress_AppliesTimeEstimation() {
            // Arrange
            TestExecutionStatusModel status = new TestExecutionStatusModel();
            status.setExecutionId(testExecutionId);
            status.setStatus("RUNNING");
            status.setStartedAt(LocalDateTime.now().minusSeconds(2));
            status.setTotalTests(100);
            status.setCompletedTests(10);
            testExecutorService.storeStatus(testExecutionId, status);

            // Act
            TestExecutionStatusModel result = testExecutorService.getStatus(testExecutionId);

            // Assert
            assertNotNull(result);
            assertEquals("RUNNING", result.getStatus());
            assertEquals(100, result.getTotalTests());
            assertTrue(result.getCompletedTests() >= 10);
            assertTrue(result.getCompletedTests() < 100);
        }

        /**
         * Purpose: Verify RUNNING status with totalTests=0 does not apply estimation.
         * Expected Result: totalTests remains 0.
         * Assertions: totalTests is 0.
         */
        @Test
        @DisplayName("Get Status - Running Status Without Total - Does Not Apply Estimation")
        void getStatus_RunningStatusWithoutTotal_DoesNotApplyEstimation() {
            // Arrange
            TestExecutionStatusModel status = new TestExecutionStatusModel();
            status.setExecutionId(testExecutionId);
            status.setStatus("RUNNING");
            status.setStartedAt(LocalDateTime.now());
            status.setTotalTests(0);
            testExecutorService.storeStatus(testExecutionId, status);

            // Act
            TestExecutionStatusModel result = testExecutorService.getStatus(testExecutionId);

            // Assert
            assertNotNull(result);
            assertEquals("RUNNING", result.getStatus());
            assertEquals(0, result.getTotalTests());
        }

        /**
         * Purpose: Verify FAILED status returns error message.
         * Expected Result: errorMessage present in result.
         * Assertions: Status FAILED, errorMessage matches.
         */
        @Test
        @DisplayName("Get Status - Failed Status - Returns Error Information")
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
    }

    @Nested
    @DisplayName("Status Progression Tests")
    class StatusProgressionTests {

        /**
         * Purpose: Verify PENDING to RUNNING transition stores correctly.
         * Expected Result: Status RUNNING, startedAt set.
         * Assertions: Status and startedAt match.
         */
        @Test
        @DisplayName("Status Progression - PENDING to RUNNING - Updates Correctly")
        void statusProgression_PendingToRunning_UpdatesCorrectly() {
            // Arrange
            TestExecutionStatusModel status = testExecutorService.getOrCreateStatus(testExecutionId);
            assertEquals("PENDING", status.getStatus());
            status.setStatus("RUNNING");
            status.setStartedAt(LocalDateTime.now());
            testExecutorService.storeStatus(testExecutionId, status);

            // Act
            TestExecutionStatusModel retrieved = testExecutorService.getStatus(testExecutionId);

            // Assert
            assertEquals("RUNNING", retrieved.getStatus());
            assertNotNull(retrieved.getStartedAt());
        }

        /**
         * Purpose: Verify RUNNING to COMPLETED transition stores final counts.
         * Expected Result: Status COMPLETED with passed/failed counts.
         * Assertions: completedAt, totalTests, passedTests, failedTests match.
         */
        @Test
        @DisplayName("Status Progression - RUNNING to COMPLETED - Updates Correctly")
        void statusProgression_RunningToCompleted_UpdatesCorrectly() {
            // Arrange
            TestExecutionStatusModel status = testExecutorService.getOrCreateStatus(testExecutionId);
            status.setStatus("RUNNING");
            status.setStartedAt(LocalDateTime.now().minusSeconds(5));
            testExecutorService.storeStatus(testExecutionId, status);

            status.setStatus("COMPLETED");
            status.setCompletedAt(LocalDateTime.now());
            status.setTotalTests(50);
            status.setCompletedTests(50);
            status.setPassedTests(48);
            status.setFailedTests(2);
            testExecutorService.storeStatus(testExecutionId, status);

            // Act
            TestExecutionStatusModel retrieved = testExecutorService.getStatus(testExecutionId);

            // Assert
            assertEquals("COMPLETED", retrieved.getStatus());
            assertNotNull(retrieved.getCompletedAt());
            assertEquals(50, retrieved.getTotalTests());
            assertEquals(48, retrieved.getPassedTests());
            assertEquals(2, retrieved.getFailedTests());
        }

        /**
         * Purpose: Verify progressive test result accumulation.
         * Expected Result: Final counts reflect last update.
         * Assertions: completedTests, passedTests, failedTests match final store.
         */
        @Test
        @DisplayName("Status Progression - Test Results - Accumulates Correctly")
        void statusProgression_TestResults_AccumulatesCorrectly() {
            // Arrange
            TestExecutionStatusModel status = testExecutorService.getOrCreateStatus(testExecutionId);
            status.setTotalTests(10);
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

            // Act
            TestExecutionStatusModel retrieved = testExecutorService.getStatus(testExecutionId);

            // Assert
            assertEquals(10, retrieved.getCompletedTests());
            assertEquals(8, retrieved.getPassedTests());
            assertEquals(2, retrieved.getFailedTests());
        }
    }

    @Nested
    @DisplayName("Duration Tracking Tests")
    class DurationTrackingTests {

        /**
         * Purpose: Verify RUNNING status returns elapsed duration from startedAt.
         * Expected Result: durationMs >= 2000 (2 seconds).
         * Assertions: durationMs at least 2000.
         */
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
            assertTrue(result.getDurationMs() >= 2000);
        }

        /**
         * Purpose: Verify COMPLETED status returns stored duration.
         * Expected Result: durationMs matches stored value.
         * Assertions: durationMs equals 5000.
         */
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
}
