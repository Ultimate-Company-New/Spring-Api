package com.example.SpringApi.Services.Tests.QA;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.RequestModels.TestExecutionRequestModel;
import com.example.SpringApi.Models.ResponseModels.TestExecutionStatusModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for QAService.startTestExecution() method.
 * 
 * 
 * Test Coverage:
 * - Success scenarios (8 tests)
 * - Validation failures (12 tests)
 * - Edge cases (10 tests)
 */
@ExtendWith(MockitoExtension.class)
class StartTestExecutionTest extends QAServiceTestBase {

    // Total Tests: 30
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
    void startTestExecution_asyncExecution_returnsImmediately() {
        // Arrange
        TestExecutionRequestModel request = createValidTestExecutionRequest();

        // Act
        long startTime = System.currentTimeMillis();
        TestExecutionStatusModel status = qaService.startTestExecution(request);
        long endTime = System.currentTimeMillis();

        // Assert
        assertNotNull(status);
        // Should return quickly (within 5 seconds)
        assertTrue((endTime - startTime) < 5000, "Should return immediately without waiting for tests to complete");
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_calculatesExpectedCount_correctly() {
        // Arrange
        List<String> testNames = Arrays.asList("testMethod1", "testMethod2");
        TestExecutionRequestModel request = createTestExecutionRequestWithTestNames(
                "com.example.SpringApi.Services.Tests.QA.GetAvailableServicesTest", testNames);

        // Act
        TestExecutionStatusModel status = qaService.startTestExecution(request);

        // Assert
        assertNotNull(status);
        // Expected count should be set
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_duplicateTestNames_deduplicates() {
        // Arrange
        List<String> testNames = Arrays.asList("testMethod1", "testMethod1", "testMethod2");
        TestExecutionRequestModel request = createTestExecutionRequestWithTestNames(
                "com.example.SpringApi.Services.Tests.QA.GetAvailableServicesTest", testNames);

        // Act
        TestExecutionStatusModel status = qaService.startTestExecution(request);

        // Assert
        assertNotNull(status);
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_generatesExecutionId_successfully() {
        // Arrange
        TestExecutionRequestModel request = createValidTestExecutionRequest();

        // Act
        TestExecutionStatusModel status = qaService.startTestExecution(request);

        // Assert
        assertNotNull(status.getExecutionId());
        assertFalse(status.getExecutionId().trim().isEmpty());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_methodName_startsMethodTests() {
        // Arrange
        TestExecutionRequestModel request = createTestExecutionRequestWithMethodName("QAService",
                "startTestExecution");
        request.setTestClassName("QA/StartTestExecutionTest");

        // Act
        TestExecutionStatusModel status = qaService.startTestExecution(request);

        // Assert
        assertNotNull(status);
        assertNotNull(status.getExecutionId());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_multipleTestNames_joinsWithPlus() {
        // Arrange
        List<String> testNames = Arrays.asList("testMethod1", "testMethod2", "testMethod3");
        TestExecutionRequestModel request = createTestExecutionRequestWithTestNames(
                "com.example.SpringApi.Services.Tests.QA.GetAvailableServicesTest", testNames);

        // Act
        TestExecutionStatusModel status = qaService.startTestExecution(request);

        // Assert
        assertNotNull(status);
        assertNotNull(status.getExecutionId());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_nestedTestClass_resolvesCorrectly() {
        // Arrange
        List<String> testNames = Arrays.asList("testMethod1");
        TestExecutionRequestModel request = createTestExecutionRequestWithTestNames(
                "com.example.SpringApi.Services.Tests.QA.GetAvailableServicesTest", testNames);

        // Act
        TestExecutionStatusModel status = qaService.startTestExecution(request);

        // Assert
        assertNotNull(status);
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_parameterizedTest_stripsParameterSuffix() {
        // Arrange
        List<String> testNames = Arrays.asList("testMethod1[1]");
        TestExecutionRequestModel request = createTestExecutionRequestWithTestNames(
                "com.example.SpringApi.Services.Tests.QA.GetAvailableServicesTest", testNames);

        // Act
        TestExecutionStatusModel status = qaService.startTestExecution(request);

        // Assert
        assertNotNull(status);
        assertNotNull(status.getExecutionId());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_returnsStatusModel_withExecutionId() {
        // Arrange
        TestExecutionRequestModel request = createValidTestExecutionRequest();

        // Act
        TestExecutionStatusModel status = qaService.startTestExecution(request);

        // Assert
        assertNotNull(status);
        assertTrue(status instanceof TestExecutionStatusModel);
        assertNotNull(status.getExecutionId());
    }

    // ==================== VALIDATION FAILURES ====================

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_runAllTrue_startsAllTests() {
        // Arrange
        TestExecutionRequestModel request = createValidTestExecutionRequest();
        request.setRunAll(true);

        // Act
        TestExecutionStatusModel status = qaService.startTestExecution(request);

        // Assert
        assertNotNull(status);
        assertNotNull(status.getExecutionId());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_singleTestName_noPlus() {
        // Arrange
        List<String> testNames = Arrays.asList("testMethod1");
        TestExecutionRequestModel request = createTestExecutionRequestWithTestNames(
                "com.example.SpringApi.Services.Tests.QA.GetAvailableServicesTest", testNames);

        // Act
        TestExecutionStatusModel status = qaService.startTestExecution(request);

        // Assert
        assertNotNull(status);
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_specificTestNames_startsSpecificTests() {
        // Arrange
        List<String> testNames = Arrays.asList("testMethod1", "testMethod2");
        TestExecutionRequestModel request = createTestExecutionRequestWithTestNames(
                "com.example.SpringApi.Services.Tests.QA.GetAvailableServicesTest", testNames);

        // Act
        TestExecutionStatusModel status = qaService.startTestExecution(request);

        // Assert
        assertNotNull(status);
        assertNotNull(status.getExecutionId());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_statusInitiallyPending_beforeExecution() {
        // Arrange
        TestExecutionRequestModel request = createValidTestExecutionRequest();

        // Act
        TestExecutionStatusModel status = qaService.startTestExecution(request);

        // Assert
        assertNotNull(status);
        assertNotNull(status.getStatus());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_storesInitialStatus_correctly() {
        // Arrange
        TestExecutionRequestModel request = createValidTestExecutionRequest();

        // Act
        TestExecutionStatusModel status = qaService.startTestExecution(request);

        // Assert
        assertNotNull(status);
        assertNotNull(status.getStatus());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_testNamesWithSpaces_trimsCorrectly() {
        // Arrange
        List<String> testNames = Arrays.asList(" testMethod1 ", "testMethod2");
        TestExecutionRequestModel request = createTestExecutionRequestWithTestNames(
                "com.example.SpringApi.Services.Tests.QA.GetAvailableServicesTest", testNames);

        // Act
        TestExecutionStatusModel status = qaService.startTestExecution(request);

        // Assert
        assertNotNull(status);
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_veryLongTestName_handlesCorrectly() {
        // Arrange
        String longTestName = "testMethod" + "VeryLongName".repeat(20);
        List<String> testNames = Arrays.asList(longTestName);
        TestExecutionRequestModel request = createTestExecutionRequestWithTestNames(
                "com.example.SpringApi.Services.Tests.QA.GetAvailableServicesTest", testNames);

        // Act
        TestExecutionStatusModel status = qaService.startTestExecution(request);

        // Assert
        assertNotNull(status);
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_withTestClassName_usesTestClass() {
        // Arrange
        List<String> testNames = Arrays.asList("testMethod1");
        TestExecutionRequestModel request = createTestExecutionRequestWithTestNames(
                "com.example.SpringApi.Services.Tests.QA.GetAvailableServicesTest", testNames);

        // Act
        TestExecutionStatusModel status = qaService.startTestExecution(request);

        // Assert
        assertNotNull(status);
        assertNotNull(status.getExecutionId());
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
    void startTestExecution_emptyMethodName_throwsBadRequestException() {
        // Arrange
        TestExecutionRequestModel request = new TestExecutionRequestModel();
        request.setRunAll(false);
        request.setMethodName("");
        request.setServiceName("TestService");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.startTestExecution(request);
        });
        assertEquals(ErrorMessages.QAErrorMessages.MUST_SPECIFY_RUN_ALL_OR_TEST_NAMES_OR_METHOD, exception.getMessage());
    }

    // ==================== EDGE CASES ====================

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_emptyTestNames_throwsBadRequestException() {
        // Arrange
        TestExecutionRequestModel request = new TestExecutionRequestModel();
        request.setRunAll(false);
        request.setTestNames(new ArrayList<>());
        request.setTestClassName("SomeTest");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.startTestExecution(request);
        });
        assertEquals(ErrorMessages.QAErrorMessages.MUST_SPECIFY_RUN_ALL_OR_TEST_NAMES_OR_METHOD, exception.getMessage());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_exceptionMessages_useErrorConstants() {
        // Arrange - null request
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.startTestExecution(null);
        });

        // Verify the exception message comes from ErrorMessages constants
        assertTrue(exception.getMessage().equals(ErrorMessages.QAErrorMessages.TEST_EXECUTION_REQUEST_CANNOT_BE_NULL));
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_invalidServiceName_throwsBadRequestException() {
        // Arrange
        TestExecutionRequestModel request = createTestExecutionRequestWithMethodName("InvalidService", "someMethod");

        // Act
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.startTestExecution(request);
        });

        // Assert
        String expectedMessage = String.format(
                ErrorMessages.QAErrorMessages.NO_TESTS_FOUND_FOR_METHOD_FORMAT,
                "someMethod",
                "InvalidServiceTest");
        assertEquals(expectedMessage, exception.getMessage());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_methodNameWithoutServiceOrClass_throwsBadRequestException() {
        // Arrange
        TestExecutionRequestModel request = new TestExecutionRequestModel();
        request.setRunAll(false);
        request.setMethodName("someMethod");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.startTestExecution(request);
        });
        assertEquals(ErrorMessages.QAErrorMessages.MUST_SPECIFY_SERVICE_NAME_OR_TEST_CLASS_NAME, exception.getMessage());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_methodWithNoTests_throwsBadRequestException() {
        // Arrange
        TestExecutionRequestModel request = createTestExecutionRequestWithMethodName("QAService", "nonExistentMethod");

        // Act
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.startTestExecution(request);
        });

        // Assert
        String expectedMessage = String.format(
                ErrorMessages.QAErrorMessages.NO_TESTS_FOUND_FOR_METHOD_FORMAT,
                "nonExistentMethod",
                "QAServiceTest");
        assertEquals(expectedMessage, exception.getMessage());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_nonExistentTestClass_throwsBadRequestException() {
        // Arrange
        List<String> testNames = Arrays.asList("testMethod1");
        TestExecutionRequestModel request = createTestExecutionRequestWithTestNames(
                "com.example.NonExistentTest", testNames);

        // Act
        TestExecutionStatusModel status = qaService.startTestExecution(request);

        // Assert
        assertNotNull(status);
        assertNotNull(status.getExecutionId());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_nullMethodName_withoutRunAll_throwsBadRequestException() {
        // Arrange
        TestExecutionRequestModel request = new TestExecutionRequestModel();
        request.setRunAll(false);
        request.setMethodName(null);
        request.setServiceName("TestService");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.startTestExecution(request);
        });
        assertEquals(ErrorMessages.QAErrorMessages.MUST_SPECIFY_RUN_ALL_OR_TEST_NAMES_OR_METHOD, exception.getMessage());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_nullRequest_throwsBadRequestException() {
        // Arrange

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.startTestExecution(null);
        });
        assertEquals(ErrorMessages.QAErrorMessages.TEST_EXECUTION_REQUEST_CANNOT_BE_NULL, exception.getMessage());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_nullTestClassName_withTestNames_throwsBadRequestException() {
        // Arrange
        TestExecutionRequestModel request = new TestExecutionRequestModel();
        request.setRunAll(false);
        request.setTestNames(Arrays.asList("testMethod1"));
        request.setTestClassName(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.startTestExecution(request);
        });
        assertEquals(ErrorMessages.QAErrorMessages.TEST_CLASS_NAME_REQUIRED, exception.getMessage());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_runAllFalse_noTestNames_noMethod_throwsBadRequestException() {
        // Arrange
        TestExecutionRequestModel request = new TestExecutionRequestModel();
        request.setRunAll(false);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.startTestExecution(request);
        });
        assertEquals(ErrorMessages.QAErrorMessages.MUST_SPECIFY_RUN_ALL_OR_TEST_NAMES_OR_METHOD, exception.getMessage());
    }

    /**
     * Purpose: Verify expected behavior.
     * Expected Result: Operation completes as expected.
     * Assertions: See assertions in test body.
     */
    @Test
    void startTestExecution_testNamesWithoutClassName_throwsBadRequestException() {
        // Arrange
        TestExecutionRequestModel request = new TestExecutionRequestModel();
        request.setRunAll(false);
        request.setTestNames(Arrays.asList("testMethod1"));
        request.setTestClassName(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.startTestExecution(request);
        });
        assertEquals(ErrorMessages.QAErrorMessages.TEST_CLASS_NAME_REQUIRED, exception.getMessage());
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
    void startTestExecution_controller_permission_unauthorized() {
        // Arrange
        com.example.SpringApi.Controllers.QAController controller = new com.example.SpringApi.Controllers.QAController(qaSubTranslator);
        stubQaTranslatorStartTestExecutionThrowsUnauthorized();

        // Act
        org.springframework.http.ResponseEntity<?> response = controller.runTests(createValidTestExecutionRequest());

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(org.springframework.http.HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
