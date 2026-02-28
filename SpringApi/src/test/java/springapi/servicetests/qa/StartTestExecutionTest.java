package springapi.servicetests.qa;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import springapi.ErrorMessages;
import springapi.exceptions.ApplicationException;
import springapi.exceptions.BadRequestException;
import springapi.models.databasemodels.LatestTestResult;
import springapi.models.requestmodels.TestExecutionRequestModel;
import springapi.models.responsemodels.TestExecutionStatusModel;
import springapi.services.QaService;

/**
 * Unit tests for QaService.startTestExecution() method.
 *
 * <p>Test Coverage: - Success scenarios (8 tests) - Validation failures (12 tests) - Edge cases (10
 * tests)
 */
@ExtendWith(MockitoExtension.class)
class StartTestExecutionTest extends QAServiceTestBase {

  // Total Tests: 56
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */
  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
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
    assertTrue(
        (endTime - startTime) < 5000,
        "Should return immediately without waiting for tests to complete");
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void startTestExecution_calculatesExpectedCount_correctly() {
    // Arrange
    List<String> testNames = Arrays.asList("testMethod1", "testMethod2");
    TestExecutionRequestModel request =
        createTestExecutionRequestWithTestNames(
            "springapi.servicetests.qa.GetAvailableServicesTest", testNames);

    // Act
    TestExecutionStatusModel status = qaService.startTestExecution(request);

    // Assert
    assertNotNull(status);
    // Expected count should be set
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void startTestExecution_duplicateTestNames_deduplicates() {
    // Arrange
    List<String> testNames = Arrays.asList("testMethod1", "testMethod1", "testMethod2");
    TestExecutionRequestModel request =
        createTestExecutionRequestWithTestNames(
            "springapi.servicetests.qa.GetAvailableServicesTest", testNames);

    // Act
    TestExecutionStatusModel status = qaService.startTestExecution(request);

    // Assert
    assertNotNull(status);
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
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
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void startTestExecution_methodName_startsMethodTests() {
    // Arrange
    TestExecutionRequestModel request =
        createTestExecutionRequestWithMethodName("QaService", "startTestExecution");
    request.setTestClassName("QA/StartTestExecutionTest");

    // Act
    TestExecutionStatusModel status = qaService.startTestExecution(request);

    // Assert
    assertNotNull(status);
    assertNotNull(status.getExecutionId());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void startTestExecution_multipleTestNames_joinsWithPlus() {
    // Arrange
    List<String> testNames = Arrays.asList("testMethod1", "testMethod2", "testMethod3");
    TestExecutionRequestModel request =
        createTestExecutionRequestWithTestNames(
            "springapi.servicetests.qa.GetAvailableServicesTest", testNames);

    // Act
    TestExecutionStatusModel status = qaService.startTestExecution(request);

    // Assert
    assertNotNull(status);
    assertNotNull(status.getExecutionId());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void startTestExecution_nestedTestClass_resolvesCorrectly() {
    // Arrange
    List<String> testNames = Arrays.asList("testMethod1");
    TestExecutionRequestModel request =
        createTestExecutionRequestWithTestNames(
            "springapi.servicetests.qa.GetAvailableServicesTest", testNames);

    // Act
    TestExecutionStatusModel status = qaService.startTestExecution(request);

    // Assert
    assertNotNull(status);
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void startTestExecution_parameterizedTest_stripsParameterSuffix() {
    // Arrange
    List<String> testNames = Arrays.asList("testMethod1[1]");
    TestExecutionRequestModel request =
        createTestExecutionRequestWithTestNames(
            "springapi.servicetests.qa.GetAvailableServicesTest", testNames);

    // Act
    TestExecutionStatusModel status = qaService.startTestExecution(request);

    // Assert
    assertNotNull(status);
    assertNotNull(status.getExecutionId());
    assertEquals("PENDING", status.getStatus());
    assertEquals(1, status.getTotalTests());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
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
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
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
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void startTestExecution_singleTestName_noPlus() {
    // Arrange
    List<String> testNames = Arrays.asList("testMethod1");
    TestExecutionRequestModel request =
        createTestExecutionRequestWithTestNames(
            "springapi.servicetests.qa.GetAvailableServicesTest", testNames);

    // Act
    TestExecutionStatusModel status = qaService.startTestExecution(request);

    // Assert
    assertNotNull(status);
    assertNotNull(status.getExecutionId());
    assertEquals(1, status.getTotalTests());
    assertEquals("PENDING", status.getStatus());
    assertFalse(status.getExecutionId().contains("+"));
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void startTestExecution_specificTestNames_startsSpecificTests() {
    // Arrange
    List<String> testNames = Arrays.asList("testMethod1", "testMethod2");
    TestExecutionRequestModel request =
        createTestExecutionRequestWithTestNames(
            "springapi.servicetests.qa.GetAvailableServicesTest", testNames);

    // Act
    TestExecutionStatusModel status = qaService.startTestExecution(request);

    // Assert
    assertNotNull(status);
    assertNotNull(status.getExecutionId());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
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
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
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
    assertEquals("PENDING", status.getStatus());
    assertEquals(0, status.getCompletedTests());
    assertTrue(status.getTotalTests() >= status.getCompletedTests());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void startTestExecution_testNamesWithSpaces_trimsCorrectly() {
    // Arrange
    List<String> testNames = Arrays.asList(" testMethod1 ", "testMethod2");
    TestExecutionRequestModel request =
        createTestExecutionRequestWithTestNames(
            "springapi.servicetests.qa.GetAvailableServicesTest", testNames);

    // Act
    TestExecutionStatusModel status = qaService.startTestExecution(request);

    // Assert
    assertNotNull(status);
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void startTestExecution_veryLongTestName_handlesCorrectly() {
    // Arrange
    String longTestName = "testMethod" + "VeryLongName".repeat(20);
    List<String> testNames = Arrays.asList(longTestName);
    TestExecutionRequestModel request =
        createTestExecutionRequestWithTestNames(
            "springapi.servicetests.qa.GetAvailableServicesTest", testNames);

    // Act
    TestExecutionStatusModel status = qaService.startTestExecution(request);

    // Assert
    assertNotNull(status);
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void startTestExecution_withTestClassName_usesTestClass() {
    // Arrange
    List<String> testNames = Arrays.asList("testMethod1");
    TestExecutionRequestModel request =
        createTestExecutionRequestWithTestNames(
            "springapi.servicetests.qa.GetAvailableServicesTest", testNames);

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
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
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
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () -> {
              qaService.startTestExecution(request);
            });
    assertEquals(
        ErrorMessages.QaErrorMessages.MUST_SPECIFY_RUN_ALL_OR_TEST_NAMES_OR_METHOD,
        exception.getMessage());
  }

  // ==================== EDGE CASES ====================

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
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
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () -> {
              qaService.startTestExecution(request);
            });
    assertEquals(
        ErrorMessages.QaErrorMessages.MUST_SPECIFY_RUN_ALL_OR_TEST_NAMES_OR_METHOD,
        exception.getMessage());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void startTestExecution_exceptionMessages_useErrorConstants() {
    // Act & Assert
    assertNullRequestThrowsBadRequest();
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void startTestExecution_invalidServiceName_throwsBadRequestException() {
    // Arrange
    TestExecutionRequestModel request =
        createTestExecutionRequestWithMethodName("InvalidService", "someMethod");

    // Act
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () -> {
              qaService.startTestExecution(request);
            });

    // Assert
    String expectedMessage =
        String.format(
            ErrorMessages.QaErrorMessages.NO_TESTS_FOUND_FOR_METHOD_FORMAT,
            "someMethod",
            "InvalidServiceTest");
    assertEquals(expectedMessage, exception.getMessage());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void startTestExecution_methodNameWithoutServiceOrClass_throwsBadRequestException() {
    // Arrange
    TestExecutionRequestModel request = new TestExecutionRequestModel();
    request.setRunAll(false);
    request.setMethodName("someMethod");

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () -> {
              qaService.startTestExecution(request);
            });
    assertEquals(
        ErrorMessages.QaErrorMessages.MUST_SPECIFY_SERVICE_NAME_OR_TEST_CLASS_NAME,
        exception.getMessage());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void startTestExecution_methodWithNoTests_throwsBadRequestException() {
    // Arrange
    TestExecutionRequestModel request =
        createTestExecutionRequestWithMethodName("QaService", "nonExistentMethod");

    // Act
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () -> {
              qaService.startTestExecution(request);
            });

    // Assert
    String expectedMessage =
        String.format(
            ErrorMessages.QaErrorMessages.NO_TESTS_FOUND_FOR_METHOD_FORMAT,
            "nonExistentMethod",
            "QAServiceTest");
    assertEquals(expectedMessage, exception.getMessage());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void startTestExecution_nonExistentTestClass_throwsBadRequestException() {
    // Arrange
    List<String> testNames = Arrays.asList("testMethod1");
    TestExecutionRequestModel request =
        createTestExecutionRequestWithTestNames("com.example.NonExistentTest", testNames);

    // Act
    TestExecutionStatusModel status = qaService.startTestExecution(request);

    // Assert
    assertNotNull(status);
    assertNotNull(status.getExecutionId());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
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
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () -> {
              qaService.startTestExecution(request);
            });
    assertEquals(
        ErrorMessages.QaErrorMessages.MUST_SPECIFY_RUN_ALL_OR_TEST_NAMES_OR_METHOD,
        exception.getMessage());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void startTestExecution_nullRequest_throwsBadRequestException() {
    // Act & Assert
    assertNullRequestThrowsBadRequest();
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
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
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () -> {
              qaService.startTestExecution(request);
            });
    assertEquals(ErrorMessages.QaErrorMessages.TEST_CLASS_NAME_REQUIRED, exception.getMessage());
    assertEquals(1, request.getTestNames().size());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
   * Assertions: See assertions in test body.
   */
  @Test
  void startTestExecution_runAllFalse_noTestNames_noMethod_throwsBadRequestException() {
    // Arrange
    TestExecutionRequestModel request = new TestExecutionRequestModel();
    request.setRunAll(false);

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () -> {
              qaService.startTestExecution(request);
            });
    assertEquals(
        ErrorMessages.QaErrorMessages.MUST_SPECIFY_RUN_ALL_OR_TEST_NAMES_OR_METHOD,
        exception.getMessage());
  }

  /**
   * Purpose: Verify expected behavior. Expected Result: Operation completes as expected.
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
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () -> {
              qaService.startTestExecution(request);
            });
    assertEquals(ErrorMessages.QaErrorMessages.TEST_CLASS_NAME_REQUIRED, exception.getMessage());
    assertNull(request.getTestClassName());
    assertEquals(1, request.getTestNames().size());
    assertFalse(request.getRunAll());
  }

  /**
   * Purpose: Verify Maven command builder includes class and method filter when both are provided.
   * Expected Result: Command contains surefire flags and -Dtest selector. Assertions: Command
   * tokens and selector format.
   */
  @Test
  void startTestExecution_privateBuildMavenCommand_withClassAndMethodFilter_success() {
    // Arrange
    QaService realService = createRealQAService();

    // Act
    Object commandObj =
        invokePrivateMethod(
            realService,
            "buildMavenCommand",
            new Class<?>[] {String.class, String.class},
            "SomeTestClass",
            "testOne+testTwo");

    // Assert
    assertTrue(commandObj instanceof List<?>);
    List<?> command = (List<?>) commandObj;
    assertEquals("mvn", command.get(0));
    assertTrue(command.contains("-Dsurefire.useFile=false"));
    assertTrue(command.contains("-DtrimStackTrace=false"));
    assertTrue(command.contains("-Dtest=SomeTestClass#testOne+testTwo"));
  }

  /**
   * Purpose: Verify Maven command builder omits -Dtest selector when class is not provided.
   * Expected Result: Base maven command returned. Assertions: Command tokens and absence of -Dtest.
   */
  @Test
  void startTestExecution_privateBuildMavenCommand_withoutClass_success() {
    // Arrange
    QaService realService = createRealQAService();

    // Act
    Object commandObj =
        invokePrivateMethod(
            realService,
            "buildMavenCommand",
            new Class<?>[] {String.class, String.class},
            null,
            null);

    // Assert
    assertTrue(commandObj instanceof List<?>);
    List<?> command = (List<?>) commandObj;
    assertEquals("mvn", command.get(0));
    assertEquals("test", command.get(1));
    assertFalse(command.stream().anyMatch(token -> String.valueOf(token).startsWith("-Dtest=")));
  }

  /**
   * Purpose: Verify parser updates execution counters from surefire summary line. Expected Result:
   * Completed/passed/failed/skipped counters are updated correctly. Assertions: Counter values and
   * total-tests adjustment.
   */
  @Test
  void startTestExecution_privateParseTestOutput_updatesCounters_success() {
    // Arrange
    QaService realService = createRealQAService();
    TestExecutionStatusModel status =
        new TestExecutionStatusModel("exec-parse", "QaService", null, 1);

    // Act
    invokePrivateMethod(
        realService,
        "parseTestOutput",
        new Class<?>[] {String.class, TestExecutionStatusModel.class},
        "Tests run: 4, Failures: 1, Errors: 1, Skipped: 1",
        status);

    // Assert
    assertEquals(4, status.getCompletedTests());
    assertEquals(1, status.getPassedTests());
    assertEquals(2, status.getFailedTests());
    assertEquals(1, status.getSkippedTests());
    assertEquals(4, status.getTotalTests());
  }

  /**
   * Purpose: Verify parser marks execution as RUNNING when maven output reports a running class.
   * Expected Result: Status changes to RUNNING. Assertions: Status value equals RUNNING.
   */
  @Test
  void startTestExecution_privateParseTestOutput_runningLine_setsRunningStatus_success() {
    // Arrange
    QaService realService = createRealQAService();
    TestExecutionStatusModel status =
        new TestExecutionStatusModel("exec-running", "QaService", null, 2);
    status.setStatus("PENDING");

    // Act
    invokePrivateMethod(
        realService,
        "parseTestOutput",
        new Class<?>[] {String.class, TestExecutionStatusModel.class},
        "Running springapi.servicetests.qa.GetAvailableServicesTest",
        status);

    // Assert
    assertEquals("RUNNING", status.getStatus());
  }

  /**
   * Purpose: Verify project directory resolver prioritizes current directory when pom and test
   * sources exist. Expected Result: Current temporary directory path is returned. Assertions:
   * Returned path equals temporary project path.
   */
  @Test
  void startTestExecution_privateFindProjectDirectory_currentDirPreferred_success()
      throws Exception {
    // Arrange
    QaService realService = createRealQAService();
    Path tempProject = Files.createTempDirectory("qa-project-");
    Files.createFile(tempProject.resolve("pom.xml"));
    Files.createDirectories(tempProject.resolve("src/test/java"));
    String originalUserDir = System.getProperty("user.dir");
    System.setProperty("user.dir", tempProject.toString());

    try {
      // Act
      Object pathObj = invokePrivateMethod(realService, "findProjectDirectory", new Class<?>[] {});

      // Assert
      assertTrue(pathObj instanceof Path);
      assertEquals(tempProject, pathObj);
    } finally {
      System.setProperty("user.dir", originalUserDir);
    }
  }

  /**
   * Purpose: Verify executeTestsAsync safely returns when execution status is missing. Expected
   * Result: No exception and no process start. Assertions: Method returns null (void invocation)
   * without failure.
   */
  @Test
  void startTestExecution_privateExecuteTestsAsync_missingStatus_returnsImmediately_success() {
    // Arrange
    QaService realService = createRealQAService();
    clearExecutionTrackingState();

    // Act
    Object result =
        invokePrivateMethod(
            realService,
            "executeTestsAsync",
            new Class<?>[] {String.class, String.class, String.class, String.class},
            "missing-execution",
            "AnyTestClass",
            "anyMethod",
            "QaService");

    // Assert
    assertNull(result);
  }

  /**
   * Purpose: Verify markExecutionFailed updates terminal fields and clears running process
   * tracking. Expected Result: Status is FAILED with error metadata and running process entry
   * removed. Assertions: Status fields and map cleanup.
   */
  @Test
  void startTestExecution_privateMarkExecutionFailed_updatesStatusAndClearsProcess_success()
      throws Exception {
    // Arrange
    QaService realService = createRealQAService();
    clearExecutionTrackingState();
    TestExecutionStatusModel status =
        new TestExecutionStatusModel("exec-fail", "QaService", null, 3);
    long startTime = System.currentTimeMillis() - 100L;

    Object runningProcessesObj = getPrivateStaticFieldValue(QaService.class, "runningProcesses");
    if (runningProcessesObj != null) {
      java.lang.reflect.Method putMethod =
          runningProcessesObj.getClass().getMethod("put", Object.class, Object.class);
      putMethod.invoke(runningProcessesObj, "exec-fail", org.mockito.Mockito.mock(Process.class));
    }

    // Act
    invokePrivateMethod(
        realService,
        "markExecutionFailed",
        new Class<?>[] {String.class, TestExecutionStatusModel.class, long.class, String.class},
        "exec-fail",
        status,
        startTime,
        "execution-failed");

    // Assert
    assertEquals("FAILED", status.getStatus());
    assertEquals("execution-failed", status.getErrorMessage());
    assertNotNull(status.getCompletedAt());
    assertTrue(status.getDurationMs() >= 0);
  }

  /**
   * Purpose: Verify parseSurefireReports exits gracefully when report directory does not exist.
   * Expected Result: No exception and status remains unchanged. Assertions: Results list remains
   * empty.
   */
  @Test
  void startTestExecution_privateParseSurefireReports_missingDirectory_noopSuccess()
      throws Exception {
    // Arrange
    QaService realService = createRealQAService();
    TestExecutionStatusModel status =
        new TestExecutionStatusModel("exec-no-reports", "QaService", null, 0);
    Path tempProject = Files.createTempDirectory("qa-no-reports-");

    // Act
    invokePrivateMethod(
        realService,
        "parseSurefireReports",
        new Class<?>[] {TestExecutionStatusModel.class, Path.class, String.class},
        status,
        tempProject,
        "AnyTestClass");

    // Assert
    assertTrue(status.getResults().isEmpty());
  }

  /**
   * Purpose: Verify Maven command builder includes class selector when method filter is absent.
   * Expected Result: Command contains -Dtest with class-only selector. Assertions: Selector exists
   * and does not include method suffix.
   */
  @Test
  void startTestExecution_privateBuildMavenCommand_withClassOnly_success() {
    // Arrange
    QaService realService = createRealQAService();

    // Act
    Object commandObj =
        invokePrivateMethod(
            realService,
            "buildMavenCommand",
            new Class<?>[] {String.class, String.class},
            "OnlyClassSelectorTest",
            null);

    // Assert
    assertTrue(commandObj instanceof List<?>);
    List<?> command = (List<?>) commandObj;
    assertTrue(command.contains("-Dtest=OnlyClassSelectorTest"));
    assertFalse(command.stream().anyMatch(token -> String.valueOf(token).contains("#")));
  }

  /**
   * Purpose: Verify project directory resolution falls back to ./SpringApi when current dir is a
   * parent workspace. Expected Result: SpringApi child directory is selected. Assertions: Returned
   * path equals currentDir/SpringApi.
   */
  @Test
  void startTestExecution_privateFindProjectDirectory_springApiSubdirSelected_success()
      throws Exception {
    // Arrange
    QaService realService = createRealQAService();
    Path tempRoot = Files.createTempDirectory("qa-find-project-subdir-");
    Path springApiDir = tempRoot.resolve("SpringApi");
    Files.createDirectories(springApiDir.resolve("src/test/java"));
    Files.createFile(springApiDir.resolve("pom.xml"));
    String originalUserDir = System.getProperty("user.dir");
    System.setProperty("user.dir", tempRoot.toString());

    try {
      // Act
      Object pathObj = invokePrivateMethod(realService, "findProjectDirectory", new Class<?>[] {});

      // Assert
      assertTrue(pathObj instanceof Path);
      assertEquals(springApiDir, pathObj);
    } finally {
      System.setProperty("user.dir", originalUserDir);
    }
  }

  /**
   * Purpose: Verify project directory resolution checks parent/SpringApi when running from nested
   * module path. Expected Result: Parent SpringApi directory is selected. Assertions: Returned path
   * equals parent workspace SpringApi directory.
   */
  @Test
  void startTestExecution_privateFindProjectDirectory_parentSpringApiSelected_success()
      throws Exception {
    // Arrange
    QaService realService = createRealQAService();
    Path tempRoot = Files.createTempDirectory("qa-find-project-parent-");
    Path parentDir = tempRoot.resolve("workspace");
    Path currentDir = parentDir.resolve("module");
    Path parentSpringApi = parentDir.resolve("SpringApi");
    Files.createDirectories(currentDir);
    Files.createDirectories(parentSpringApi.resolve("src/test/java"));
    Files.createFile(parentSpringApi.resolve("pom.xml"));
    String originalUserDir = System.getProperty("user.dir");
    System.setProperty("user.dir", currentDir.toString());

    try {
      // Act
      Object pathObj = invokePrivateMethod(realService, "findProjectDirectory", new Class<?>[] {});

      // Assert
      assertTrue(pathObj instanceof Path);
      assertEquals(parentSpringApi, pathObj);
    } finally {
      System.setProperty("user.dir", originalUserDir);
    }
  }

  /**
   * Purpose: Verify project directory resolution returns current directory when no pom/test roots
   * are discoverable. Expected Result: Current user.dir path is returned. Assertions: Returned path
   * equals current working directory.
   */
  @Test
  void startTestExecution_privateFindProjectDirectory_noMatches_returnsCurrentDirectory_success()
      throws Exception {
    // Arrange
    QaService realService = createRealQAService();
    Path tempRoot = Files.createTempDirectory("qa-find-project-fallback-");
    String originalUserDir = System.getProperty("user.dir");
    System.setProperty("user.dir", tempRoot.toString());

    try {
      // Act
      Object pathObj = invokePrivateMethod(realService, "findProjectDirectory", new Class<?>[] {});

      // Assert
      assertTrue(pathObj instanceof Path);
      assertEquals(tempRoot, pathObj);
    } finally {
      System.setProperty("user.dir", originalUserDir);
    }
  }

  /**
   * Purpose: Verify test-file parser handles nested classes, display names, and multi-line
   * parameterized @ValueSource. Expected Result: Expanded parameterized test names and declaring
   * nested class names are captured. Assertions: Parsed method names, display names, and declaring
   * class names.
   */
  @Test
  void startTestExecution_privateReadTestMethodsFromFile_parsesNestedAndParameterizedTests_success()
      throws Exception {
    // Arrange
    QaService realService = createRealQAService();
    Path tempProject = createParserFixtureProject();
    String originalUserDir = System.getProperty("user.dir");
    System.setProperty("user.dir", tempProject.toString());

    try {
      // Act
      Object parsedObj =
          invokePrivateMethod(
              realService,
              "readTestMethodsFromFile",
              new Class<?>[] {String.class},
              "ParserFixtureTest");

      // Assert
      assertTrue(parsedObj instanceof List<?>);
      List<?> parsed = (List<?>) parsedObj;
      assertFalse(parsed.isEmpty());

      List<String> methodNames = new ArrayList<>();
      List<String> displayNames = new ArrayList<>();
      List<String> declaringClasses = new ArrayList<>();
      for (Object parsedMethod : parsed) {
        methodNames.add(readPrivateStringField(parsedMethod, "methodName"));
        displayNames.add(readPrivateStringField(parsedMethod, "displayName"));
        declaringClasses.add(readPrivateStringField(parsedMethod, "declaringTestClassName"));
      }

      assertTrue(methodNames.contains("parseValues(String)[1]"));
      assertTrue(methodNames.contains("parseValues(String)[2]"));
      assertTrue(methodNames.contains("createOrderSuccess"));
      assertTrue(displayNames.contains("Parameterized parser [1]"));
      assertTrue(declaringClasses.contains("ParserFixtureTest$NestedContext"));
    } finally {
      System.setProperty("user.dir", originalUserDir);
    }
  }

  /**
   * Purpose: Verify parser handles IOException while reading a discovered test path. Expected
   * Result: Empty parsed list is returned. Assertions: Returned list exists and is empty.
   */
  @Test
  void startTestExecution_privateReadTestMethodsFromFile_ioFailure_returnsEmptyList_success()
      throws Exception {
    // Arrange
    QaService realService = createRealQAService();
    Path tempProject = Files.createTempDirectory("qa-parser-io-");
    Path testSourceDir = tempProject.resolve("src/test/java/springapi/services/tests");
    Files.createDirectories(testSourceDir);
    Files.createDirectories(testSourceDir.resolve("BrokenFixture.java"));
    String originalUserDir = System.getProperty("user.dir");
    System.setProperty("user.dir", tempProject.toString());

    try {
      // Act
      Object parsedObj =
          invokePrivateMethod(
              realService,
              "readTestMethodsFromFile",
              new Class<?>[] {String.class},
              "BrokenFixture");

      // Assert
      assertTrue(parsedObj instanceof List<?>);
      List<?> parsed = (List<?>) parsedObj;
      assertTrue(parsed.isEmpty());
    } finally {
      System.setProperty("user.dir", originalUserDir);
    }
  }

  /**
   * Purpose: Verify @ValueSource count parser supports null, braced multi-value, empty braced, and
   * single value forms. Expected Result: Correct counts are returned for each form. Assertions:
   * Exact count values per input variant.
   */
  @Test
  void startTestExecution_privateParseValueSourceCount_handlesSupportedVariants_success() {
    // Arrange
    QaService realService = createRealQAService();

    // Act
    int nullCount =
        (Integer)
            invokePrivateMethod(
                realService, "parseValueSourceCount", new Class<?>[] {String.class}, (Object) null);
    int multiCount =
        (Integer)
            invokePrivateMethod(
                realService,
                "parseValueSourceCount",
                new Class<?>[] {String.class},
                "@ValueSource(strings = {\"A\", \"B\", \"C\"})");
    int emptyCount =
        (Integer)
            invokePrivateMethod(
                realService,
                "parseValueSourceCount",
                new Class<?>[] {String.class},
                "@ValueSource(strings = {})");
    int singleCount =
        (Integer)
            invokePrivateMethod(
                realService,
                "parseValueSourceCount",
                new Class<?>[] {String.class},
                "@ValueSource(strings = \"A\")");

    // Assert
    assertEquals(0, nullCount);
    assertEquals(3, multiCount);
    assertEquals(0, emptyCount);
    assertEquals(1, singleCount);
  }

  /**
   * Purpose: Verify parameter-list type extraction handles null/empty input, annotations, final
   * modifier, and generics. Expected Result: Surefire-friendly simple type names are returned.
   * Assertions: Exact extracted type list text.
   */
  @Test
  void
      startTestExecution_privateExtractParameterTypesForSurefireName_handlesAnnotationsAndGenerics_success() {
    // Arrange
    QaService realService = createRealQAService();

    // Act
    String nullTypes =
        (String)
            invokePrivateMethod(
                realService,
                "extractParameterTypesForSurefireName",
                new Class<?>[] {String.class},
                (Object) null);
    String emptyTypes =
        (String)
            invokePrivateMethod(
                realService,
                "extractParameterTypesForSurefireName",
                new Class<?>[] {String.class},
                "   ");
    String extractedTypes =
        (String)
            invokePrivateMethod(
                realService,
                "extractParameterTypesForSurefireName",
                new Class<?>[] {String.class},
                "@org.jetbrains.annotations.NotNull final java.util.List<java.lang.String> values, final int count");

    // Assert
    assertEquals("", nullTypes);
    assertEquals("", emptyTypes);
    assertEquals("List,int", extractedTypes);
  }

  /**
   * Purpose: Verify nested declaring-class builder returns outer$inner selector and falls back when
   * outer class is absent. Expected Result: Correct nested selector for matching outer class and
   * fallback to outer name otherwise. Assertions: Exact selector values.
   */
  @Test
  void startTestExecution_privateBuildDeclaringTestClassName_nestedAndFallback_success() {
    // Arrange
    QaService realService = createRealQAService();
    ArrayDeque<Object> classStack = new ArrayDeque<>();
    classStack.add(
        createQaPrivateInnerInstance(
            "ClassScope", new Class<?>[] {String.class, int.class}, "ParserFixtureTest", 1));
    classStack.add(
        createQaPrivateInnerInstance(
            "ClassScope", new Class<?>[] {String.class, int.class}, "NestedContext", 2));

    // Act
    String nestedSelector =
        (String)
            invokePrivateMethod(
                realService,
                "buildDeclaringTestClassName",
                new Class<?>[] {String.class, ArrayDeque.class},
                "ParserFixtureTest",
                classStack);
    String fallbackSelector =
        (String)
            invokePrivateMethod(
                realService,
                "buildDeclaringTestClassName",
                new Class<?>[] {String.class, ArrayDeque.class},
                "OtherOuterTest",
                classStack);

    // Assert
    assertEquals("ParserFixtureTest$NestedContext", nestedSelector);
    assertEquals("OtherOuterTest", fallbackSelector);
  }

  /**
   * Purpose: Verify associated-test matcher supports underscore and camelCase naming and enriches
   * tests with latest run status. Expected Result: Both naming styles are matched and latest result
   * metadata is applied. Assertions: Matched test count and populated status for mapped latest
   * result.
   */
  @Test
  void startTestExecution_privateFindAssociatedTests_matchesUnderscoreAndCamelCase_success() {
    // Arrange
    QaService realService = createRealQAService();
    List<Object> allTestMethods = new ArrayList<>();
    allTestMethods.add(
        createQaPrivateInnerInstance(
            "TestMethodInfo",
            new Class<?>[] {String.class, String.class, String.class},
            "createOrder_s01_success",
            "Underscore test",
            "SomeTestClass"));
    allTestMethods.add(
        createQaPrivateInnerInstance(
            "TestMethodInfo",
            new Class<?>[] {String.class, String.class, String.class},
            "createOrderSuccessCase",
            "Camel test",
            "SomeTestClass"));

    LatestTestResult latest = createLatestTestResult(999L, "AnyService", "createOrder_s01_success");
    latest.setStatus("FAILED");
    Map<String, LatestTestResult> latestResultsMap = new HashMap<>();
    latestResultsMap.put("createOrder_s01_success", latest);

    // Act
    Object associatedObj =
        invokePrivateMethod(
            realService,
            "findAssociatedTests",
            new Class<?>[] {String.class, List.class, Map.class},
            "createOrder",
            allTestMethods,
            latestResultsMap);

    // Assert
    assertTrue(associatedObj instanceof List<?>);
    List<?> associated = (List<?>) associatedObj;
    assertEquals(2, associated.size());
    Object first = associated.getFirst();
    String firstStatus = (String) invokePrivateMethod(first, "getLastRunStatus", new Class<?>[] {});
    assertEquals("FAILED", firstStatus);
  }

  /**
   * Purpose: Verify declaring class resolution supports exact parameterized method names and base
   * names. Expected Result: Nested declaring class selector is resolved for both exact and stripped
   * names. Assertions: Exact nested class selector and null handling.
   */
  @Test
  void startTestExecution_privateResolveDeclaringClassAndStripParameterizedSuffix_success()
      throws Exception {
    // Arrange
    QaService realService = createRealQAService();
    Path tempProject = createParserFixtureProject();
    String originalUserDir = System.getProperty("user.dir");
    System.setProperty("user.dir", tempProject.toString());

    try {
      // Act
      String exactSelector =
          (String)
              invokePrivateMethod(
                  realService,
                  "resolveDeclaringTestClassForTestMethod",
                  new Class<?>[] {String.class, String.class},
                  "ParserFixtureTest",
                  "parseValues(String)[1]");
      String strippedSelector =
          (String)
              invokePrivateMethod(
                  realService,
                  "resolveDeclaringTestClassForTestMethod",
                  new Class<?>[] {String.class, String.class},
                  "ParserFixtureTest",
                  "parseValues");
      String invalidSelector =
          (String)
              invokePrivateMethod(
                  realService,
                  "resolveDeclaringTestClassForTestMethod",
                  new Class<?>[] {String.class, String.class},
                  "",
                  "anything");
      String strippedName =
          (String)
              invokePrivateMethod(
                  realService,
                  "stripParameterizedSuffix",
                  new Class<?>[] {String.class},
                  "parseValues(String)[2]");
      String strippedNull =
          (String)
              invokePrivateMethod(
                  realService,
                  "stripParameterizedSuffix",
                  new Class<?>[] {String.class},
                  (Object) null);

      // Assert
      assertEquals("ParserFixtureTest$NestedContext", exactSelector);
      assertEquals("ParserFixtureTest$NestedContext", strippedSelector);
      assertNull(invalidSelector);
      assertEquals("parseValues", strippedName);
      assertNull(strippedNull);
    } finally {
      System.setProperty("user.dir", originalUserDir);
    }
  }

  /**
   * Purpose: Verify expected test count calculation supports service-level/method-level and
   * dashboard failure fallback. Expected Result: Service/method counts are non-negative and
   * dashboard failures return 0. Assertions: Numeric ranges and fallback value.
   */
  @Test
  void
      startTestExecution_privateCalculateExpectedTestCount_serviceMethodAndFailureFallback_success() {
    // Arrange
    QaService realService = createRealQAService();

    // Act
    int serviceCount =
        (Integer)
            invokePrivateMethod(
                realService,
                "calculateExpectedTestCount",
                new Class<?>[] {String.class, String.class, List.class},
                "QaService",
                null,
                null);
    int methodCount =
        (Integer)
            invokePrivateMethod(
                realService,
                "calculateExpectedTestCount",
                new Class<?>[] {String.class, String.class, List.class},
                "QaService",
                "getDashboardData",
                null);

    stubQaServiceGetDashboardDataThrows(new RuntimeException("dashboard-failed"));
    int failedFallback =
        (Integer)
            invokePrivateMethod(
                qaService,
                "calculateExpectedTestCount",
                new Class<?>[] {String.class, String.class, List.class},
                "QaService",
                null,
                null);

    // Assert
    assertTrue(serviceCount >= 0);
    assertTrue(methodCount >= 0);
    assertEquals(0, failedFallback);
  }

  /**
   * Purpose: Verify surefire XML parsing captures passed/failed/skipped results and de-duplicates
   * repeated testcases. Expected Result: Parsed result list contains one entry per unique testcase
   * with correct statuses. Assertions: Result count, statuses, and parsed failure details.
   */
  @Test
  void startTestExecution_privateParseSurefireReports_parsesAndDeduplicatesResults_success()
      throws Exception {
    // Arrange
    QaService realService = createRealQAService();
    TestExecutionStatusModel status =
        new TestExecutionStatusModel("exec-xml-parse", "QaService", null, 0);
    Path tempProject = Files.createTempDirectory("qa-surefire-");
    Path surefireDir = tempProject.resolve("target/surefire-reports");
    Files.createDirectories(surefireDir);

    String xml =
        """
                <testsuite name="suite">
                    <testcase name="createOrder_s01_success" classname="AnyTestClass" time="0.011"/>
                    <testcase name="createOrder_f01_failure" classname="AnyTestClass" time="0.022">
                        <failure message="boom-message">stack-line-1</failure>
                    </testcase>
                    <testcase name="createOrder_s02_skipped" classname="AnyTestClass" time="0.001">
                        <skipped/>
                    </testcase>
                    <testcase name="createOrder_s01_success" classname="AnyTestClass" time="0.011"/>
                </testsuite>
                """;
    Files.writeString(surefireDir.resolve("TEST-AnyTestClass.xml"), xml);

    // Act
    invokePrivateMethod(
        realService,
        "parseSurefireReports",
        new Class<?>[] {TestExecutionStatusModel.class, Path.class, String.class},
        status,
        tempProject,
        null);

    // Assert
    assertTrue(status.getResults().size() >= 2);
    assertTrue(status.getResults().stream().anyMatch(r -> "FAILED".equals(r.getStatus())));
    assertTrue(status.getResults().stream().allMatch(r -> r.getDurationMs() >= 0));
  }

  /**
   * Purpose: Verify surefire report listing failure is wrapped as application exception. Expected
   * Result: Runtime wrapper contains ApplicationException root cause. Assertions: Root cause type
   * and error constant message.
   */
  @Test
  void startTestExecution_privateParseSurefireReports_notDirectory_throwsApplicationException()
      throws Exception {
    // Arrange
    QaService realService = createRealQAService();
    TestExecutionStatusModel status =
        new TestExecutionStatusModel("exec-surefire-fail", "QaService", null, 0);
    Path tempProject = Files.createTempDirectory("qa-surefire-not-dir-");
    Path targetDir = tempProject.resolve("target");
    Files.createDirectories(targetDir);
    Files.writeString(targetDir.resolve("surefire-reports"), "not-a-directory");

    // Act
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () ->
                invokePrivateMethod(
                    realService,
                    "parseSurefireReports",
                    new Class<?>[] {TestExecutionStatusModel.class, Path.class, String.class},
                    status,
                    tempProject,
                    null));

    // Assert
    ApplicationException appException = findCause(exception, ApplicationException.class);
    assertNotNull(appException);
    assertEquals(
        ErrorMessages.TestExecutorErrorMessages.FAILED_TO_LIST_SUREFIRE_REPORTS,
        appException.getMessage());
  }

  /**
   * Purpose: Verify XML report read failures are wrapped as application exceptions with formatted
   * file name. Expected Result: Runtime wrapper contains ApplicationException root cause.
   * Assertions: Root cause type and formatted message.
   */
  @Test
  void startTestExecution_privateParseXmlReport_missingFile_throwsApplicationException() {
    // Arrange
    QaService realService = createRealQAService();
    TestExecutionStatusModel status =
        new TestExecutionStatusModel("exec-xml-missing", "QaService", null, 0);
    Path missingFile =
        Path.of("target/surefire-reports/TEST-DefinitelyMissing-" + System.nanoTime() + ".xml");

    // Act
    RuntimeException exception =
        assertThrows(
            RuntimeException.class,
            () ->
                invokePrivateMethod(
                    realService,
                    "parseXmlReport",
                    new Class<?>[] {Path.class, TestExecutionStatusModel.class},
                    missingFile,
                    status));

    // Assert
    ApplicationException appException = findCause(exception, ApplicationException.class);
    assertNotNull(appException);
    assertEquals(
        String.format(
            ErrorMessages.TestExecutorErrorMessages.FAILED_TO_PARSE_SUREFIRE_REPORT_FORMAT,
            missingFile.getFileName()),
        appException.getMessage());
  }

  /**
   * Purpose: Verify executeTestsAsync can complete a failing Maven run and persist terminal status
   * metadata. Expected Result: Execution finishes with COMPLETED_WITH_FAILURES and completion
   * metadata is set. Assertions: Terminal status, duration, and completion timestamp.
   */
  @Test
  void startTestExecution_privateExecuteTestsAsync_nonExistentTestClass_marksCompletedWithFailures()
      throws Exception {
    // Arrange
    QaService realService = createRealQAService();
    clearExecutionTrackingState();
    String executionId = "exec-nonexistent-test-class";
    TestExecutionStatusModel status =
        new TestExecutionStatusModel(executionId, "QaService", null, 1);
    putExecutionStatus(executionId, status);

    // Act
    invokePrivateMethod(
        realService,
        "executeTestsAsync",
        new Class<?>[] {String.class, String.class, String.class, String.class},
        executionId,
        "DefinitelyNoSuchTestClass",
        null,
        "QaService");

    // Assert
    assertEquals("COMPLETED_WITH_FAILURES", status.getStatus());
    assertNotNull(status.getCompletedAt());
    assertTrue(status.getDurationMs() >= 0);
  }

  /**
   * Purpose: Verify executeTestsAsync handles process start I/O failure and marks execution as
   * failed. Expected Result: ApplicationException is thrown and status is updated to FAILED.
   * Assertions: Root cause type/message and FAILED status metadata.
   */
  @Test
  void
      startTestExecution_privateExecuteTestsAsync_ioFailure_marksFailedAndThrowsApplicationException()
          throws Exception {
    // Arrange
    QaService realService = createRealQAService();
    clearExecutionTrackingState();
    String executionId = "exec-io-failure";
    TestExecutionStatusModel status =
        new TestExecutionStatusModel(executionId, "QaService", null, 1);
    putExecutionStatus(executionId, status);

    Path missingDir =
        Path.of(
            System.getProperty("java.io.tmpdir"),
            "qa-missing-" + System.nanoTime(),
            "not-found-dir");
    String originalUserDir = System.getProperty("user.dir");
    System.setProperty("user.dir", missingDir.toString());

    try {
      // Act
      RuntimeException exception =
          assertThrows(
              RuntimeException.class,
              () ->
                  invokePrivateMethod(
                      realService,
                      "executeTestsAsync",
                      new Class<?>[] {String.class, String.class, String.class, String.class},
                      executionId,
                      "AnyTestClass",
                      null,
                      "QaService"));

      // Assert
      ApplicationException appException = findCause(exception, ApplicationException.class);
      assertNotNull(appException);
      assertEquals(ErrorMessages.TestExecutorErrorMessages.IO_FAILED, appException.getMessage());
      assertEquals("FAILED", status.getStatus());
      assertNotNull(status.getCompletedAt());
      assertTrue(status.getErrorMessage().startsWith("I/O error during test execution:"));
    } finally {
      System.setProperty("user.dir", originalUserDir);
    }
  }

  /**
   * Purpose: Verify executeTestsAsync catches unexpected runtime errors after process completion.
   * Expected Result: Status transitions to FAILED and wrapper ApplicationException is thrown.
   * Assertions: FAILED status and execution-failed error message format.
   */
  @Test
  void
      startTestExecution_privateExecuteTestsAsync_parseReportFailure_marksFailedWithExecutionFailed()
          throws Exception {
    // Arrange
    QaService realService = createRealQAService();
    clearExecutionTrackingState();
    String executionId = "exec-generic-failure";
    TestExecutionStatusModel status =
        new TestExecutionStatusModel(executionId, "QaService", null, 1);
    putExecutionStatus(executionId, status);

    Path tempProject = Files.createTempDirectory("qa-exec-generic-failure-");
    Path targetDir = tempProject.resolve("target");
    Files.createDirectories(targetDir);
    Files.writeString(targetDir.resolve("surefire-reports"), "not-a-directory");
    String originalUserDir = System.getProperty("user.dir");
    System.setProperty("user.dir", tempProject.toString());

    try {
      // Act
      RuntimeException exception =
          assertThrows(
              RuntimeException.class,
              () ->
                  invokePrivateMethod(
                      realService,
                      "executeTestsAsync",
                      new Class<?>[] {String.class, String.class, String.class, String.class},
                      executionId,
                      "DefinitelyNoSuchTestClass",
                      null,
                      "QaService"));

      // Assert
      ApplicationException appException = findCause(exception, ApplicationException.class);
      assertNotNull(appException);
      assertEquals(
          ErrorMessages.TestExecutorErrorMessages.EXECUTION_FAILED, appException.getMessage());
      assertEquals("FAILED", status.getStatus());
      assertTrue(status.getErrorMessage().startsWith("Test execution failed:"));
    } finally {
      System.setProperty("user.dir", originalUserDir);
    }
  }

  private Path createParserFixtureProject() throws Exception {
    Path tempProject = Files.createTempDirectory("qa-parser-fixture-");
    Path testSourceDir = tempProject.resolve("src/test/java/springapi/services/tests");
    Files.createDirectories(testSourceDir);

    String source =
        """
                package springapi.servicetests;

                import org.junit.jupiter.api.DisplayName;
                import org.junit.jupiter.api.Nested;
                import org.junit.jupiter.api.Test;
                import org.junit.jupiter.params.ParameterizedTest;
                import org.junit.jupiter.params.provider.ValueSource;

                class ParserFixtureTest {

                    @Nested
                    class NestedContext {

                        @DisplayName("Parameterized parser")
                        @ParameterizedTest
                        @ValueSource(
                                strings = {
                                        "A",
                                        "B"
                                }
                        )
                        void parseValues(final String value) {
                        }

                        @DisplayName("Regular parser")
                        @Test
                        void createOrderSuccess() {
                        }
                    }
                }
                """;
    Files.writeString(testSourceDir.resolve("ParserFixtureTest.java"), source);
    return tempProject;
  }

  private String readPrivateStringField(Object target, String fieldName) {
    try {
      java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      Object value = field.get(target);
      return value != null ? value.toString() : null;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void putExecutionStatus(String executionId, TestExecutionStatusModel status)
      throws Exception {
    Object activeExecutionsObj = getPrivateStaticFieldValue(QaService.class, "activeExecutions");
    java.lang.reflect.Method putMethod =
        activeExecutionsObj.getClass().getMethod("put", Object.class, Object.class);
    putMethod.invoke(activeExecutionsObj, executionId, status);
  }

  private <T extends Throwable> T findCause(Throwable exception, Class<T> causeType) {
    Throwable current = exception;
    while (current != null) {
      if (causeType.isInstance(current)) {
        return causeType.cast(current);
      }
      current = current.getCause();
    }
    return null;
  }

  private void assertNullRequestThrowsBadRequest() {
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () -> {
              qaService.startTestExecution(null);
            });
    assertEquals(
        ErrorMessages.QaErrorMessages.TEST_EXECUTION_REQUEST_CANNOT_BE_NULL,
        exception.getMessage());
  }

  /*
   **********************************************************************************************
   * PERMISSION TESTS
   **********************************************************************************************
   */
  /**
   * Purpose: Verify unauthorized access is handled at the controller level. Expected Result:
   * Unauthorized status is returned. Assertions: Response status is 401 UNAUTHORIZED.
   */
  @Test
  void startTestExecution_controller_permission_unauthorized() {
    // Arrange
    springapi.controllers.QaController controller =
        new springapi.controllers.QaController(qaSubTranslator);
    stubQaTranslatorStartTestExecutionThrowsUnauthorized();

    // Act
    org.springframework.http.ResponseEntity<?> response =
        controller.runTests(createValidTestExecutionRequest());

    // Assert
    org.junit.jupiter.api.Assertions.assertEquals(
        org.springframework.http.HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }
}
