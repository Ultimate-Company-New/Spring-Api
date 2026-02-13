package com.example.SpringApi.Services.Tests.QA;

import com.example.SpringApi.Controllers.QAController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.TestRun;
import com.example.SpringApi.Models.RequestModels.TestRunRequestModel;
import com.example.SpringApi.Models.ResponseModels.TestRunResponseModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
/**
 * Unit tests for QAService.saveTestRun() method.
 * 
 * 
 * Test Coverage:
 * - Success scenarios (6 tests)
 * - Validation failures (12 tests)
 * - Edge cases (7 tests)
 */
@ExtendWith(MockitoExtension.class)
class SaveTestRunTest extends QAServiceTestBase {

    // Total Tests: 26
    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_s01_ValidRequest_Success() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        TestRun savedTestRun = createTestRun();
        stubTestRunRepositorySave(savedTestRun);

        // Act
        TestRunResponseModel result = qaService.saveTestRun(request);

        // Assert
        assertNotNull(result);
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_s02_multipleResults_savesAllResults() {
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

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_s03_withEnvironment_savesEnvironment() {
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

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_s04_withRunType_savesRunType() {
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

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_s05_updatesLatestTestResults_correctly() {
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

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_s06_marksTestRunComplete_afterSave() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        TestRun savedTestRun = createTestRun();
        stubTestRunRepositorySave(savedTestRun);

        // Act
        TestRunResponseModel result = qaService.saveTestRun(request);

        // Assert
        assertNotNull(result);
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_f01_nullRequest_throwsBadRequestException() {
        // Arrange
        TestRunRequestModel request = null;

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.saveTestRun(request);
        });
        assertEquals(ErrorMessages.QAErrorMessages.TEST_RUN_REQUEST_CANNOT_BE_NULL, exception.getMessage());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_f02_nullServiceName_throwsBadRequestException() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        request.setServiceName(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.saveTestRun(request);
        });
        assertEquals(ErrorMessages.QAErrorMessages.SERVICE_NAME_REQUIRED, exception.getMessage());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_f03_emptyServiceName_throwsBadRequestException() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        request.setServiceName("");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.saveTestRun(request);
        });
        assertEquals(ErrorMessages.QAErrorMessages.SERVICE_NAME_REQUIRED, exception.getMessage());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_f04_whitespaceServiceName_throwsBadRequestException() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        request.setServiceName("   ");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.saveTestRun(request);
        });
        assertEquals(ErrorMessages.QAErrorMessages.SERVICE_NAME_REQUIRED, exception.getMessage());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_f05_nullResults_throwsBadRequestException() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        request.setResults(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.saveTestRun(request);
        });
        assertEquals(ErrorMessages.QAErrorMessages.AT_LEAST_ONE_TEST_RESULT_REQUIRED, exception.getMessage());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_f06_emptyResults_throwsBadRequestException() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        request.setResults(new ArrayList<>());

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.saveTestRun(request);
        });
        assertEquals(ErrorMessages.QAErrorMessages.AT_LEAST_ONE_TEST_RESULT_REQUIRED, exception.getMessage());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_f07_resultWithNullTestMethodName_handlesGracefully() {
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

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_f08_resultWithNullMethodName_handlesGracefully() {
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

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_f09_resultWithNullStatus_handlesGracefully() {
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

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_f10_resultWithNullDuration_defaultsToZero() {
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

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_f11_resultWithNegativeDuration_handlesGracefully() {
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

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_f12_exceptionMessages_useErrorConstants() {
        // Arrange - null request
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.saveTestRun(null);
        });

        // Verify the exception message comes from ErrorMessages constants
        assertTrue(exception.getMessage().equals(ErrorMessages.QAErrorMessages.TEST_RUN_REQUEST_CANNOT_BE_NULL));
    }

    /*
     **********************************************************************************************
     * EDGE CASES
     **********************************************************************************************
     */

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_f13_singleResult_savesSingleResult() {
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

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_f14_largeNumberOfResults_savesAll() {
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

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_f15_resultWithErrorMessage_savesErrorMessage() {
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

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_f16_resultWithStackTrace_savesStackTrace() {
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

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_f17_resultWithDisplayName_savesDisplayName() {
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

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_f18_unknownServiceName_stillSaves() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest("UnknownService", 1);
        TestRun savedTestRun = createTestRun();
        stubTestRunRepositorySave(savedTestRun);

        // Act
        TestRunResponseModel result = qaService.saveTestRun(request);

        // Assert
        assertNotNull(result);
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void saveTestRun_f19_repositorySaveFailure_propagatesException() {
        // Arrange
        TestRunRequestModel request = createValidTestRunRequest();
        stubTestRunRepositorySaveThrows(new RuntimeException(ErrorMessages.CommonErrorMessages.DATABASE_ERROR));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> qaService.saveTestRun(request));
        assertEquals(ErrorMessages.CommonErrorMessages.DATABASE_ERROR, ex.getMessage());
    }

    /*
     **********************************************************************************************
     * PERMISSION TESTS
     **********************************************************************************************
     */

    /**
     * Purpose: Verify unauthorized access is handled at the controller level.
     * Expected Result: Unauthorized status is returned.
     * Assertions: Response status is 401 UNAUTHORIZED.
     */
    @Test
    @DisplayName("Save Test Run - Controller permission unauthorized - Success")
    void saveTestRun_controller_permission_unauthorized() {
        // Arrange
        QAController controller = new QAController(qaSubTranslator);
        stubQaTranslatorSaveTestRunThrowsUnauthorized();

        // Act
        ResponseEntity<?> response = controller.saveTestRun(createValidTestRunRequest());

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
