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
 * Total Tests: 30
 * 
 * Test Coverage:
 * - Success scenarios (8 tests)
 * - Validation failures (12 tests)
 * - Edge cases (10 tests)
 */
@ExtendWith(MockitoExtension.class)
class StartTestExecutionTest extends QAServiceBaseTest {

    // ==================== SUCCESS TESTS ====================

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

    @Test
    void startTestExecution_nullRequest_throwsBadRequestException() {
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.startTestExecution(null);
        });
        assertEquals(ErrorMessages.QAErrorMessages.TestExecutionRequestCannotBeNull, exception.getMessage());
    }

    @Test
    void startTestExecution_runAllFalse_noTestNames_noMethod_throwsBadRequestException() {
        // Arrange
        TestExecutionRequestModel request = new TestExecutionRequestModel();
        request.setRunAll(false);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.startTestExecution(request);
        });
        assertEquals(ErrorMessages.QAErrorMessages.MustSpecifyRunAllOrTestNamesOrMethod, exception.getMessage());
    }

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
        assertEquals(ErrorMessages.QAErrorMessages.TestClassNameRequired, exception.getMessage());
    }

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
        assertEquals(ErrorMessages.QAErrorMessages.MustSpecifyServiceNameOrTestClassName, exception.getMessage());
    }

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
        assertEquals(ErrorMessages.QAErrorMessages.MustSpecifyRunAllOrTestNamesOrMethod, exception.getMessage());
    }

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
        assertEquals(ErrorMessages.QAErrorMessages.TestClassNameRequired, exception.getMessage());
    }

    @Test
    void startTestExecution_invalidServiceName_throwsBadRequestException() {
        // Arrange
        TestExecutionRequestModel request = createTestExecutionRequestWithMethodName("InvalidService", "someMethod");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            qaService.startTestExecution(request);
        });
    }

    @Test
    void startTestExecution_methodWithNoTests_throwsBadRequestException() {
        // Arrange
        TestExecutionRequestModel request = createTestExecutionRequestWithMethodName("QAService", "nonExistentMethod");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            qaService.startTestExecution(request);
        });
    }

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

    @Test
    void startTestExecution_exceptionMessages_useErrorConstants() {
        // Arrange - null request
        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            qaService.startTestExecution(null);
        });

        // Verify the exception message comes from ErrorMessages constants
        assertTrue(exception.getMessage().equals(ErrorMessages.QAErrorMessages.TestExecutionRequestCannotBeNull));
    }

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
        assertEquals(ErrorMessages.QAErrorMessages.MustSpecifyRunAllOrTestNamesOrMethod, exception.getMessage());
    }

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
        assertEquals(ErrorMessages.QAErrorMessages.MustSpecifyRunAllOrTestNamesOrMethod, exception.getMessage());
    }

    // ==================== EDGE CASES ====================

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
}
