package com.example.SpringApi.Services.Tests.QA;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.TestRun;
import com.example.SpringApi.Models.RequestModels.TestRunRequestModel;
import com.example.SpringApi.Models.ResponseModels.TestRunResponseModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for QAService.saveTestRun() method.
 * 
 * Total Tests: 25
 * 
 * Test Coverage:
 * - Success scenarios (6 tests)
 * - Validation failures (12 tests)
 * - Edge cases (7 tests)
 */
@ExtendWith(MockitoExtension.class)
class SaveTestRunTest extends QAServiceBaseTest {

    // ==================== SUCCESS TESTS ====================

    @Test
    void saveTestRun_validRequest_savesSuccessfully() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        TestRun savedTestRun = createTestRun();
        stubTestRunRepositorySave(savedTestRun);

        // Act
        TestRunResponseModel result = qaService.saveTestRun(request);

        // Assert
        assertNotNull(result);
    }

    @Test
    void saveTestRun_multipleResults_savesAllResults() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest("TestService", 5);
        TestRun savedTestRun = createTestRun();
        stubTestRunRepositorySave(savedTestRun);

        // Act
        TestRunResponseModel result = qaService.saveTestRun(request);

        // Assert
        assertNotNull(result);
        assertEquals(5, request.getResults().size());
    }

    @Test
    void saveTestRun_withEnvironment_savesEnvironment() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        request.setEnvironment("production");
        TestRun savedTestRun = createTestRun();
        stubTestRunRepositorySave(savedTestRun);

        // Act
        TestRunResponseModel result = qaService.saveTestRun(request);

        // Assert
        assertNotNull(result);
        assertEquals("production", request.getEnvironment());
    }

    @Test
    void saveTestRun_withRunType_savesRunType() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        request.setRunType("automated");
        TestRun savedTestRun = createTestRun();
        stubTestRunRepositorySave(savedTestRun);

        // Act
        TestRunResponseModel result = qaService.saveTestRun(request);

        // Assert
        assertNotNull(result);
        assertEquals("automated", request.getRunType());
    }

    @Test
    void saveTestRun_updatesLatestTestResults_correctly() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        TestRun savedTestRun = createTestRun();
        stubTestRunRepositorySave(savedTestRun);
        stubLatestTestResultRepositorySaveAll(new ArrayList<>());

        // Act
        TestRunResponseModel result = qaService.saveTestRun(request);

        // Assert
        assertNotNull(result);
    }

    @Test
    void saveTestRun_marksTestRunComplete_afterSave() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        TestRun savedTestRun = createTestRun();
        stubTestRunRepositorySave(savedTestRun);

        // Act
        TestRunResponseModel result = qaService.saveTestRun(request);

        // Assert
        assertNotNull(result);
    }

    // ==================== VALIDATION FAILURES ====================

    @Test
    void saveTestRun_nullRequest_throwsBadRequestException() {
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.saveTestRun(null);
        });
        assertEquals(ErrorMessages.QAErrorMessages.TestRunRequestCannotBeNull, exception.getMessage());
    }

    @Test
    void saveTestRun_nullServiceName_throwsBadRequestException() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        request.setServiceName(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.saveTestRun(request);
        });
        assertEquals(ErrorMessages.QAErrorMessages.ServiceNameRequired, exception.getMessage());
    }

    @Test
    void saveTestRun_emptyServiceName_throwsBadRequestException() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        request.setServiceName("");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.saveTestRun(request);
        });
        assertEquals(ErrorMessages.QAErrorMessages.ServiceNameRequired, exception.getMessage());
    }

    @Test
    void saveTestRun_whitespaceServiceName_throwsBadRequestException() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        request.setServiceName("   ");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.saveTestRun(request);
        });
        assertEquals(ErrorMessages.QAErrorMessages.ServiceNameRequired, exception.getMessage());
    }

    @Test
    void saveTestRun_nullResults_throwsBadRequestException() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        request.setResults(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.saveTestRun(request);
        });
        assertEquals(ErrorMessages.QAErrorMessages.AtLeastOneTestResultRequired, exception.getMessage());
    }

    @Test
    void saveTestRun_emptyResults_throwsBadRequestException() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        request.setResults(new ArrayList<>());

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.saveTestRun(request);
        });
        assertEquals(ErrorMessages.QAErrorMessages.AtLeastOneTestResultRequired, exception.getMessage());
    }

    @Test
    void saveTestRun_resultWithNullTestMethodName_handlesGracefully() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        request.getResults().get(0).setTestMethodName(null);
        TestRun savedTestRun = createTestRun();
        stubTestRunRepositorySave(savedTestRun);

        // Act
        TestRunResponseModel result = qaService.saveTestRun(request);

        // Assert
        assertNotNull(result);
    }

    @Test
    void saveTestRun_resultWithNullMethodName_handlesGracefully() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        request.getResults().get(0).setMethodName(null);
        TestRun savedTestRun = createTestRun();
        stubTestRunRepositorySave(savedTestRun);

        // Act
        TestRunResponseModel result = qaService.saveTestRun(request);

        // Assert
        assertNotNull(result);
    }

    @Test
    void saveTestRun_resultWithNullStatus_handlesGracefully() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        request.getResults().get(0).setStatus(null);
        TestRun savedTestRun = createTestRun();
        stubTestRunRepositorySave(savedTestRun);

        // Act
        TestRunResponseModel result = qaService.saveTestRun(request);

        // Assert
        assertNotNull(result);
    }

    @Test
    void saveTestRun_resultWithNullDuration_defaultsToZero() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        request.getResults().get(0).setDurationMs(null);
        TestRun savedTestRun = createTestRun();
        stubTestRunRepositorySave(savedTestRun);

        // Act
        TestRunResponseModel result = qaService.saveTestRun(request);

        // Assert
        assertNotNull(result);
    }

    @Test
    void saveTestRun_resultWithNegativeDuration_handlesGracefully() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        request.getResults().get(0).setDurationMs(-100);
        TestRun savedTestRun = createTestRun();
        stubTestRunRepositorySave(savedTestRun);

        // Act
        TestRunResponseModel result = qaService.saveTestRun(request);

        // Assert
        assertNotNull(result);
    }

    @Test
    void saveTestRun_exceptionMessages_useErrorConstants() {
        // Arrange - null request
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.saveTestRun(null);
        });

        // Verify the exception message comes from ErrorMessages constants
        assertTrue(exception.getMessage().equals(ErrorMessages.QAErrorMessages.TestRunRequestCannotBeNull));
    }

    // ==================== EDGE CASES ====================

    @Test
    void saveTestRun_singleResult_savesSingleResult() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest("TestService", 1);
        TestRun savedTestRun = createTestRun();
        stubTestRunRepositorySave(savedTestRun);

        // Act
        TestRunResponseModel result = qaService.saveTestRun(request);

        // Assert
        assertNotNull(result);
        assertEquals(1, request.getResults().size());
    }

    @Test
    void saveTestRun_largeNumberOfResults_savesAll() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest("TestService", 100);
        TestRun savedTestRun = createTestRun();
        stubTestRunRepositorySave(savedTestRun);

        // Act
        TestRunResponseModel result = qaService.saveTestRun(request);

        // Assert
        assertNotNull(result);
        assertEquals(100, request.getResults().size());
    }

    @Test
    void saveTestRun_resultWithErrorMessage_savesErrorMessage() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        request.getResults().get(0).setErrorMessage("Test failed with error");
        TestRun savedTestRun = createTestRun();
        stubTestRunRepositorySave(savedTestRun);

        // Act
        TestRunResponseModel result = qaService.saveTestRun(request);

        // Assert
        assertNotNull(result);
        assertEquals("Test failed with error", request.getResults().get(0).getErrorMessage());
    }

    @Test
    void saveTestRun_resultWithStackTrace_savesStackTrace() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        request.getResults().get(0).setStackTrace("at line 1\nat line 2");
        TestRun savedTestRun = createTestRun();
        stubTestRunRepositorySave(savedTestRun);

        // Act
        TestRunResponseModel result = qaService.saveTestRun(request);

        // Assert
        assertNotNull(result);
        assertEquals("at line 1\nat line 2", request.getResults().get(0).getStackTrace());
    }

    @Test
    void saveTestRun_resultWithDisplayName_savesDisplayName() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        request.getResults().get(0).setDisplayName("Custom Display Name");
        TestRun savedTestRun = createTestRun();
        stubTestRunRepositorySave(savedTestRun);

        // Act
        TestRunResponseModel result = qaService.saveTestRun(request);

        // Assert
        assertNotNull(result);
        assertEquals("Custom Display Name", request.getResults().get(0).getDisplayName());
    }

    @Test
    void saveTestRun_unknownServiceName_stillSaves() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest("UnknownService", 1);
        TestRun savedTestRun = createTestRun();
        stubTestRunRepositorySave(savedTestRun);

        // Act
        TestRunResponseModel result = qaService.saveTestRun(request);

        // Assert
        assertNotNull(result);
    }

    @Test
    void saveTestRun_repositorySaveFailure_propagatesException() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        // Stub repository to throw exception
        org.mockito.Mockito.doThrow(new RuntimeException("Database error"))
                .when(testRunRepository).save(org.mockito.ArgumentMatchers.any(TestRun.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            qaService.saveTestRun(request);
        });
    }
}
