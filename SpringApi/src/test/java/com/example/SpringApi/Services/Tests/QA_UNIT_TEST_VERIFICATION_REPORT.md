# UNIT TEST VERIFICATION REPORT — QA

```
╔════════════════════════════════════════════════════════════╗
║           UNIT TEST VERIFICATION REPORT                    ║
║                                                            ║
║  Status: ❌ RED                                               ║
║  Services Analyzed: 1                                      ║
║  Public Methods Found: 9                                  ║
║  Test Files Expected: 9                                  ║
║  Test Files Found: 11                                    ║
║  Total Violations: 71                                    ║
╚════════════════════════════════════════════════════════════╝
```

VIOLATIONS BY RULE:

| Rule | Description | Count |
| --- | --- | --- |
| 1 | One Test File per Method | 2 |
| 2 | Test Count Declaration | 11 |
| 3 | Controller Permission Test | 11 |
| 4 | Test Annotations | 1 |
| 6 | Centralized Mocking | 1 |
| 7 | Exception Assertions | 6 |
| 8 | Error Constants | 1 |
| 9 | Test Documentation | 10 |
| 10 | Test Ordering | 11 |
| 11 | Complete Coverage | 9 |
| 12 | Arrange/Act/Assert | 6 |
| 14 | No Inline Mocks | 2 |


**MISSING/EXTRA TEST FILES (RULE 1)**
Extra test file with no matching public method: `QAServiceBaseTest.java`. Either rename it to match a public method or remove it.
Extra test file with no matching public method: `BuildServiceInfoTest.java`. Either rename it to match a public method or remove it.


**FILE-BY-FILE BREAKDOWN**

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/QA/GetAllEndpointsWithTestsTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.QA
Class: @ExtendWith(MockitoExtension.class)
Extends: None
Lines of Code: 159
Last Modified: 2026-02-10 18:07:29
Declared Test Count: MISSING/MISPLACED (first occurrence line 14)
Actual @Test Count: 12

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 14
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 12` immediately after the class opening brace.

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getAllEndpointsWithTests_controller_permission_forbidden` or `getAllEndpointsWithTests_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getAllEndpointsWithTests_success_returnsAllServices (line 26), getAllEndpointsWithTests_allServices_haveRequiredFields (line 36), getAllEndpointsWithTests_servicesOrdered_byName (line 49), getAllEndpointsWithTests_eachService_hasControllerMapping (line 60), getAllEndpointsWithTests_serviceInfo_containsServiceName (line 73), getAllEndpointsWithTests_serviceInfo_containsControllerName (line 84), getAllEndpointsWithTests_serviceInfo_containsBasePath (line 95), getAllEndpointsWithTests_serviceInfo_containsMethodsList (line 106), getAllEndpointsWithTests_emptyMethods_returnsEmptyList (line 119), getAllEndpointsWithTests_methodsWithoutTests_showZeroCoverage (line 129), getAllEndpointsWithTests_allMethodsWithTests_showFullCoverage (line 139), getAllEndpointsWithTests_mixedCoverage_calculatesCorrectly (line 149)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 4: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 27 in `getAllEndpointsWithTests_success_returnsAllServices` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 37 in `getAllEndpointsWithTests_allServices_haveRequiredFields` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 50 in `getAllEndpointsWithTests_servicesOrdered_byName` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 61 in `getAllEndpointsWithTests_eachService_hasControllerMapping` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 74 in `getAllEndpointsWithTests_serviceInfo_containsServiceName` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 85 in `getAllEndpointsWithTests_serviceInfo_containsControllerName` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 96 in `getAllEndpointsWithTests_serviceInfo_containsBasePath` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 107 in `getAllEndpointsWithTests_serviceInfo_containsMethodsList` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 120 in `getAllEndpointsWithTests_emptyMethods_returnsEmptyList` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 130 in `getAllEndpointsWithTests_methodsWithoutTests_showZeroCoverage` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 140 in `getAllEndpointsWithTests_allMethodsWithTests_showFullCoverage` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 150 in `getAllEndpointsWithTests_mixedCoverage_calculatesCorrectly` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: FAILURE, PERMISSION
- Required: Add Success, Failure, Permission section headers.

VIOLATION 6: Rule 11 - Complete Coverage
- Severity: HIGH
- Coverage by test names is incomplete.
- Missing: at least one failure/exception test (e.g., *_throws*, *_exception*, *_invalid*).

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 10 issues above.
- Fix Rule 11 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/QA/GetDashboardDataTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.QA
Class: @ExtendWith(MockitoExtension.class)
Extends: None
Lines of Code: 263
Last Modified: 2026-02-10 20:20:42
Declared Test Count: MISSING/MISPLACED (first occurrence line 17)
Actual @Test Count: 15

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 17
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 15` immediately after the class opening brace.

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getDashboardData_controller_permission_forbidden` or `getDashboardData_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getDashboardData_success_returnsCompleteDashboardData (line 29), getDashboardData_multipleServices_calculatesCorrectTotals (line 47), getDashboardData_emptyServices_returnsZeroCoverage (line 64), getDashboardData_allMethodsCovered_returns100PercentCoverage (line 77), getDashboardData_partialCoverage_calculatesCorrectPercentage (line 92), getDashboardData_coveragePercentage_isBetween0And100 (line 110), getDashboardData_totalMethods_isNonNegative (line 126), getDashboardData_totalTests_isNonNegative (line 139), getDashboardData_serviceBreakdown_matchesTotalCounts (line 152), getDashboardData_availableServices_containsAllMappedServices (line 169), getDashboardData_noTestsForAnyMethod_returnsZeroTests (line 186), getDashboardData_singleService_returnsCorrectData (line 199), getDashboardData_largeNumberOfServices_handlesCorrectly (line 214), getDashboardData_servicesWithNoMethods_excludedFromCounts (line 231), getDashboardData_roundingPrecision_correctToTwoDecimals (line 246)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 4: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: FAILURE, PERMISSION
- Required: Add Success, Failure, Permission section headers.

VIOLATION 5: Rule 11 - Complete Coverage
- Severity: HIGH
- Coverage by test names is incomplete.
- Missing: at least one failure/exception test (e.g., *_throws*, *_exception*, *_invalid*).

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 9 issues above.
- Fix Rule 10 issues above.
- Fix Rule 11 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/QA/GetEndpointsWithTestsByServiceTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.QA
Class: @ExtendWith(MockitoExtension.class)
Extends: None
Lines of Code: 250
Last Modified: 2026-02-10 20:20:42
Declared Test Count: MISSING/MISPLACED (first occurrence line 13)
Actual @Test Count: 20

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 13
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 20` immediately after the class opening brace.

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getEndpointsWithTestsByService_controller_permission_forbidden` or `getEndpointsWithTestsByService_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getEndpointsWithTestsByService_validService_returnsServiceInfo (line 25), getEndpointsWithTestsByService_serviceWithoutSuffix_normalizesName (line 37), getEndpointsWithTestsByService_serviceWithSuffix_returnsCorrectly (line 49), getEndpointsWithTestsByService_caseInsensitive_handlesCorrectly (line 61), getEndpointsWithTestsByService_knownService_returnsAllMethods (line 77), getEndpointsWithTestsByService_nullServiceName_throwsNullPointerException (line 91), getEndpointsWithTestsByService_emptyServiceName_throwsNotFoundException (line 99), getEndpointsWithTestsByService_whitespaceServiceName_throwsNotFoundException (line 107), getEndpointsWithTestsByService_nonExistentService_throwsNotFoundException (line 115), getEndpointsWithTestsByService_invalidServiceName_throwsNotFoundException (line 127), getEndpointsWithTestsByService_serviceNotInMapping_throwsNotFoundException (line 135), getEndpointsWithTestsByService_specialCharacters_throwsNotFoundException (line 143), getEndpointsWithTestsByService_numericServiceName_throwsNotFoundException (line 151), getEndpointsWithTestsByService_serviceWithNoMethods_returnsEmptyMethodsList (line 161), getEndpointsWithTestsByService_serviceWithNoTests_returnsZeroCoverage (line 174), getEndpointsWithTestsByService_multipleCallsSameService_returnsSameData (line 186), getEndpointsWithTestsByService_differentServices_returnsDifferentData (line 201), getEndpointsWithTestsByService_serviceNameWithSpaces_trimsAndNormalizes (line 214), getEndpointsWithTestsByService_veryLongServiceName_handlesCorrectly (line 226), getEndpointsWithTestsByService_errorMessage_containsAvailableServices (line 237)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 4: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 92 in `getEndpointsWithTestsByService_nullServiceName_throwsNullPointerException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 100 in `getEndpointsWithTestsByService_emptyServiceName_throwsNotFoundException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 108 in `getEndpointsWithTestsByService_whitespaceServiceName_throwsNotFoundException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 116 in `getEndpointsWithTestsByService_nonExistentService_throwsNotFoundException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 128 in `getEndpointsWithTestsByService_invalidServiceName_throwsNotFoundException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 136 in `getEndpointsWithTestsByService_serviceNotInMapping_throwsNotFoundException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 144 in `getEndpointsWithTestsByService_specialCharacters_throwsNotFoundException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 152 in `getEndpointsWithTestsByService_numericServiceName_throwsNotFoundException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 238 in `getEndpointsWithTestsByService_errorMessage_containsAvailableServices` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 5: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 94 in `getEndpointsWithTestsByService_nullServiceName_throwsNullPointerException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 102 in `getEndpointsWithTestsByService_emptyServiceName_throwsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 110 in `getEndpointsWithTestsByService_whitespaceServiceName_throwsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 130 in `getEndpointsWithTestsByService_invalidServiceName_throwsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 138 in `getEndpointsWithTestsByService_serviceNotInMapping_throwsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 146 in `getEndpointsWithTestsByService_specialCharacters_throwsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 154 in `getEndpointsWithTestsByService_numericServiceName_throwsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 232 in `getEndpointsWithTestsByService_veryLongServiceName_handlesCorrectly`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 6: Rule 8 - Error Constants
- Severity: HIGH
- Line: 124 has hardcoded message: `assertTrue(exception.getMessage().contains("NonExistentService"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 124 has hardcoded message: `assertTrue(exception.getMessage().contains("NonExistentService"));`
- Required: Replace with an ErrorMessages constant (add one if missing).

VIOLATION 7: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: PERMISSION
- Required: Add Success, Failure, Permission section headers.

VIOLATION 8: Rule 11 - Complete Coverage
- Severity: HIGH
- Coverage by test names is incomplete.
- Missing: at least one *_success test.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 7 issues above.
- Fix Rule 8 issues above.
- Fix Rule 10 issues above.
- Fix Rule 11 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/QA/QAServiceBaseTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.QA
Class: * Base test class for QA Service tests providing common helper methods and stub
Extends: None
Lines of Code: 289
Last Modified: 2026-02-10 21:13:17
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 0

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 0` immediately after the class opening brace.

VIOLATION 2: Rule 4 - Test Annotations
- Severity: HIGH
- Line: 55 has disallowed annotation @Override.
- Required: Remove or replace with allowed annotations only.

VIOLATION 3: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 42 has mock usage `@Mock`
- Required: Move mocks to base test file.
- Line: 45 has mock usage `@Mock`
- Required: Move mocks to base test file.

VIOLATION 4: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `qAServiceBase_controller_permission_forbidden` or `qAServiceBase_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: FAILURE, PERMISSION, SUCCESS
- Required: Add Success, Failure, Permission section headers.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 4 issues above.
- Fix Rule 6 issues above.
- Fix Rule 3 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/QA/SaveTestRunTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.QA
Class: @ExtendWith(MockitoExtension.class)
Extends: None
Lines of Code: 391
Last Modified: 2026-02-10 20:20:42
Declared Test Count: MISSING/MISPLACED (first occurrence line 19)
Actual @Test Count: 25

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 19
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 25` immediately after the class opening brace.

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `saveTestRun_controller_permission_forbidden` or `saveTestRun_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: saveTestRun_validRequest_savesSuccessfully (line 31), saveTestRun_multipleResults_savesAllResults (line 45), saveTestRun_withEnvironment_savesEnvironment (line 60), saveTestRun_withRunType_savesRunType (line 76), saveTestRun_updatesLatestTestResults_correctly (line 92), saveTestRun_marksTestRunComplete_afterSave (line 107), saveTestRun_nullRequest_throwsBadRequestException (line 123), saveTestRun_nullServiceName_throwsBadRequestException (line 132), saveTestRun_emptyServiceName_throwsBadRequestException (line 145), saveTestRun_whitespaceServiceName_throwsBadRequestException (line 158), saveTestRun_nullResults_throwsBadRequestException (line 171), saveTestRun_emptyResults_throwsBadRequestException (line 184), saveTestRun_resultWithNullTestMethodName_handlesGracefully (line 197), saveTestRun_resultWithNullMethodName_handlesGracefully (line 212), saveTestRun_resultWithNullStatus_handlesGracefully (line 227), saveTestRun_resultWithNullDuration_defaultsToZero (line 242), saveTestRun_resultWithNegativeDuration_handlesGracefully (line 257), saveTestRun_exceptionMessages_useErrorConstants (line 272), saveTestRun_singleResult_savesSingleResult (line 286), saveTestRun_largeNumberOfResults_savesAll (line 301), saveTestRun_resultWithErrorMessage_savesErrorMessage (line 316), saveTestRun_resultWithStackTrace_savesStackTrace (line 332), saveTestRun_resultWithDisplayName_savesDisplayName (line 348), saveTestRun_unknownServiceName_stillSaves (line 364), saveTestRun_repositorySaveFailure_propagatesException (line 378)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 4: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 124 in `saveTestRun_nullRequest_throwsBadRequestException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 5: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 383 inline mock in `saveTestRun_repositorySaveFailure_propagatesException`: `org.mockito.Mockito.doThrow(new RuntimeException("Database error"))`
- Required: Move to base test stub method and call stub in test.
- Line: 384 inline mock in `saveTestRun_repositorySaveFailure_propagatesException`: `.when(testRunRepository).save(org.mockito.ArgumentMatchers.any(TestRun.class));`
- Required: Move to base test stub method and call stub in test.

VIOLATION 6: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 387 in `saveTestRun_repositorySaveFailure_propagatesException`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 7: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: PERMISSION
- Required: Add Success, Failure, Permission section headers.

VIOLATION 8: Rule 11 - Complete Coverage
- Severity: HIGH
- Coverage by test names is incomplete.
- Missing: at least one *_success test.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 7 issues above.
- Fix Rule 10 issues above.
- Fix Rule 11 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/QA/GetAvailableServicesTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.QA
Class: @ExtendWith(MockitoExtension.class)
Extends: None
Lines of Code: 152
Last Modified: 2026-02-10 18:05:33
Declared Test Count: MISSING/MISPLACED (first occurrence line 16)
Actual @Test Count: 10

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 16
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 10` immediately after the class opening brace.

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getAvailableServices_controller_permission_forbidden` or `getAvailableServices_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getAvailableServices_success_returnsServiceNames (line 28), getAvailableServices_success_allNamesEndWithService (line 38), getAvailableServices_success_containsKnownServices (line 50), getAvailableServices_list_isNotNull (line 62), getAvailableServices_list_isNotEmpty (line 71), getAvailableServices_list_containsNoDuplicates (line 80), getAvailableServices_list_allNamesNonEmpty (line 91), getAvailableServices_multipleInvocations_returnsSameList (line 105), getAvailableServices_list_isImmutable (line 120), getAvailableServices_count_matchesServiceMappings (line 138)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 4: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 29 in `getAvailableServices_success_returnsServiceNames` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 39 in `getAvailableServices_success_allNamesEndWithService` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 51 in `getAvailableServices_success_containsKnownServices` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 63 in `getAvailableServices_list_isNotNull` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 72 in `getAvailableServices_list_isNotEmpty` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 81 in `getAvailableServices_list_containsNoDuplicates` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 92 in `getAvailableServices_list_allNamesNonEmpty` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 106 in `getAvailableServices_multipleInvocations_returnsSameList` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 121 in `getAvailableServices_list_isImmutable` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 139 in `getAvailableServices_count_matchesServiceMappings` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: FAILURE, PERMISSION
- Required: Add Success, Failure, Permission section headers.

VIOLATION 6: Rule 11 - Complete Coverage
- Severity: HIGH
- Coverage by test names is incomplete.
- Missing: at least one failure/exception test (e.g., *_throws*, *_exception*, *_invalid*).

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 10 issues above.
- Fix Rule 11 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/QA/GetTestExecutionStatusTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.QA
Class: @ExtendWith(MockitoExtension.class)
Extends: None
Lines of Code: 229
Last Modified: 2026-02-10 20:20:42
Declared Test Count: MISSING/MISPLACED (first occurrence line 16)
Actual @Test Count: 15

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 16
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 15` immediately after the class opening brace.

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getTestExecutionStatus_controller_permission_forbidden` or `getTestExecutionStatus_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getTestExecutionStatus_validId_returnsStatus (line 28), getTestExecutionStatus_runningStatus_returnsProgressSnapshot (line 45), getTestExecutionStatus_completedExecution_returnsCompletedStatus (line 57), getTestExecutionStatus_failedStatus_returnsFailedStatus (line 69), getTestExecutionStatus_statusWithResults_includesResults (line 80), getTestExecutionStatus_nullId_throwsNotFoundException (line 93), getTestExecutionStatus_emptyId_throwsNotFoundException (line 104), getTestExecutionStatus_whitespaceId_throwsNotFoundException (line 115), getTestExecutionStatus_nonExistentId_throwsNotFoundException (line 126), getTestExecutionStatus_exceptionMessage_usesErrorConstant (line 141), getTestExecutionStatus_multipleInvocations_returnsSameStatus (line 157), getTestExecutionStatus_statusNotFound_throwsNotFoundException (line 174), getTestExecutionStatus_invalidUuidFormat_throwsNotFoundException (line 188), getTestExecutionStatus_veryLongId_handlesCorrectly (line 202), getTestExecutionStatus_specialCharactersInId_handlesCorrectly (line 216)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 4: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 94 in `getTestExecutionStatus_nullId_throwsNotFoundException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 105 in `getTestExecutionStatus_emptyId_throwsNotFoundException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 116 in `getTestExecutionStatus_whitespaceId_throwsNotFoundException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 5: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 40 in `getTestExecutionStatus_validId_returnsStatus`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 52 in `getTestExecutionStatus_runningStatus_returnsProgressSnapshot`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 64 in `getTestExecutionStatus_completedExecution_returnsCompletedStatus`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 75 in `getTestExecutionStatus_failedStatus_returnsFailedStatus`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 86 in `getTestExecutionStatus_statusWithResults_includesResults`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 6: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: PERMISSION
- Required: Add Success, Failure, Permission section headers.

VIOLATION 7: Rule 11 - Complete Coverage
- Severity: HIGH
- Coverage by test names is incomplete.
- Missing: at least one *_success test.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 7 issues above.
- Fix Rule 10 issues above.
- Fix Rule 11 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/QA/GetCoverageSummaryTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.QA
Class: @ExtendWith(MockitoExtension.class)
Extends: None
Lines of Code: 288
Last Modified: 2026-02-10 21:13:18
Declared Test Count: MISSING/MISPLACED (first occurrence line 17)
Actual @Test Count: 15

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 17
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 15` immediately after the class opening brace.

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getCoverageSummary_controller_permission_forbidden` or `getCoverageSummary_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getCoverageSummary_success_returnsAllRequiredKeys (line 29), getCoverageSummary_success_calculatesCorrectTotals (line 49), getCoverageSummary_success_includesServiceBreakdown (line 66), getCoverageSummary_success_percentageWithinRange (line 81), getCoverageSummary_success_serviceBreakdownMatchesTotals (line 101), getCoverageSummary_totalServices_isNonNegative (line 120), getCoverageSummary_totalMethods_isNonNegative (line 134), getCoverageSummary_totalTests_isNonNegative (line 148), getCoverageSummary_coveragePercentage_roundedCorrectly (line 162), getCoverageSummary_serviceBreakdown_allServicesIncluded (line 179), getCoverageSummary_noServices_returnsZeros (line 200), getCoverageSummary_noMethodsCovered_returnsZeroPercentage (line 215), getCoverageSummary_allMethodsCovered_returns100Percentage (line 230), getCoverageSummary_singleService_calculatesCorrectly (line 251), getCoverageSummary_fractionalPercentage_roundsCorrectly (line 268)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 4: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: FAILURE, PERMISSION
- Required: Add Success, Failure, Permission section headers.

VIOLATION 5: Rule 11 - Complete Coverage
- Severity: HIGH
- Coverage by test names is incomplete.
- Missing: at least one failure/exception test (e.g., *_throws*, *_exception*, *_invalid*).

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 9 issues above.
- Fix Rule 10 issues above.
- Fix Rule 11 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/QA/GetLatestTestResultsTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.QA
Class: @ExtendWith(MockitoExtension.class)
Extends: None
Lines of Code: 263
Last Modified: 2026-02-10 21:13:18
Declared Test Count: MISSING/MISPLACED (first occurrence line 17)
Actual @Test Count: 15

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 17
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 15` immediately after the class opening brace.

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getLatestTestResults_controller_permission_forbidden` or `getLatestTestResults_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getLatestTestResults_nullServiceName_returnsAllResults (line 29), getLatestTestResults_emptyServiceName_returnsAllResults (line 45), getLatestTestResults_whitespaceServiceName_returnsAllResults (line 60), getLatestTestResults_validServiceName_returnsFilteredResults (line 75), getLatestTestResults_serviceWithoutSuffix_normalizesAndReturns (line 91), getLatestTestResults_results_orderedByTestMethodName (line 107), getLatestTestResults_results_containAllRequiredFields (line 124), getLatestTestResults_results_filterByClientId (line 142), getLatestTestResults_multipleServices_separatesCorrectly (line 158), getLatestTestResults_results_mappedToResponseModel (line 174), getLatestTestResults_noResults_returnsEmptyList (line 194), getLatestTestResults_nonExistentService_returnsEmptyList (line 207), getLatestTestResults_singleResult_returnsSingleItem (line 220), getLatestTestResults_multipleResultsSameTest_returnsLatestOnly (line 235), getLatestTestResults_repositoryFailure_propagatesException (line 251)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 4: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 254 inline mock in `getLatestTestResults_repositoryFailure_propagatesException`: `org.mockito.Mockito.doThrow(new RuntimeException("Database error"))`
- Required: Move to base test stub method and call stub in test.
- Line: 255 inline mock in `getLatestTestResults_repositoryFailure_propagatesException`: `.when(latestTestResultRepository)`
- Required: Move to base test stub method and call stub in test.

VIOLATION 5: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 259 in `getLatestTestResults_repositoryFailure_propagatesException`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 6: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: FAILURE, PERMISSION
- Required: Add Success, Failure, Permission section headers.

VIOLATION 7: Rule 11 - Complete Coverage
- Severity: HIGH
- Coverage by test names is incomplete.
- Missing: at least one *_success test.
- Missing: at least one failure/exception test (e.g., *_throws*, *_exception*, *_invalid*).

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 9 issues above.
- Fix Rule 14 issues above.
- Fix Rule 7 issues above.
- Fix Rule 10 issues above.
- Fix Rule 11 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/QA/BuildServiceInfoTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.QA
Class: @ExtendWith(MockitoExtension.class)
Extends: None
Lines of Code: 166
Last Modified: 2026-02-10 18:07:36
Declared Test Count: MISSING/MISPLACED (first occurrence line 13)
Actual @Test Count: 13

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 13
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 13` immediately after the class opening brace.

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `buildServiceInfo_controller_permission_forbidden` or `buildServiceInfo_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: buildServiceInfo_validService_buildsCorrectly (line 29), buildServiceInfo_serviceWithMethods_includesAllMethods (line 39), buildServiceInfo_serviceWithTests_associatesTests (line 49), buildServiceInfo_serviceWithLatestResults_populatesResults (line 59), buildServiceInfo_excludedMethods_notIncluded (line 71), buildServiceInfo_publicMethodsOnly_included (line 81), buildServiceInfo_methodInfo_hasCorrectEndpoint (line 91), buildServiceInfo_methodInfo_hasDescription (line 101), buildServiceInfo_serviceNotFound_returnsNull (line 113), buildServiceInfo_noTestFile_returnsEmptyTests (line 121), buildServiceInfo_noLatestResults_buildsWithoutResults (line 131), buildServiceInfo_serviceWithNoPublicMethods_returnsEmptyMethods (line 144), buildServiceInfo_unauthenticatedContext_buildsWithoutResults (line 154)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 4: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 116 in `buildServiceInfo_serviceNotFound_returnsNull`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: FAILURE, PERMISSION
- Required: Add Success, Failure, Permission section headers.

VIOLATION 6: Rule 11 - Complete Coverage
- Severity: HIGH
- Coverage by test names is incomplete.
- Missing: at least one *_success test.
- Missing: at least one failure/exception test (e.g., *_throws*, *_exception*, *_invalid*).

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 9 issues above.
- Fix Rule 7 issues above.
- Fix Rule 10 issues above.
- Fix Rule 11 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/QA/StartTestExecutionTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.QA
Class: @ExtendWith(MockitoExtension.class)
Extends: None
Lines of Code: 439
Last Modified: 2026-02-10 20:37:30
Declared Test Count: MISSING/MISPLACED (first occurrence line 20)
Actual @Test Count: 29

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 20
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 29` immediately after the class opening brace.

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `startTestExecution_controller_permission_forbidden` or `startTestExecution_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: startTestExecution_runAllTrue_startsAllTests (line 32), startTestExecution_specificTestNames_startsSpecificTests (line 46), startTestExecution_methodName_startsMethodTests (line 61), startTestExecution_withTestClassName_usesTestClass (line 76), startTestExecution_generatesExecutionId_successfully (line 91), startTestExecution_storesInitialStatus_correctly (line 104), startTestExecution_returnsStatusModel_withExecutionId (line 117), startTestExecution_nullRequest_throwsBadRequestException (line 133), startTestExecution_runAllFalse_noTestNames_noMethod_throwsBadRequestException (line 142), startTestExecution_testNamesWithoutClassName_throwsBadRequestException (line 155), startTestExecution_methodNameWithoutServiceOrClass_throwsBadRequestException (line 170), startTestExecution_emptyTestNames_throwsBadRequestException (line 184), startTestExecution_nullTestClassName_withTestNames_throwsBadRequestException (line 199), startTestExecution_invalidServiceName_throwsBadRequestException (line 214), startTestExecution_methodWithNoTests_throwsBadRequestException (line 225), startTestExecution_nonExistentTestClass_throwsBadRequestException (line 236), startTestExecution_exceptionMessages_useErrorConstants (line 251), startTestExecution_nullMethodName_withoutRunAll_throwsBadRequestException (line 263), startTestExecution_emptyMethodName_throwsBadRequestException (line 278), startTestExecution_parameterizedTest_stripsParameterSuffix (line 295), startTestExecution_nestedTestClass_resolvesCorrectly (line 310), startTestExecution_multipleTestNames_joinsWithPlus (line 324), startTestExecution_singleTestName_noPlus (line 339), startTestExecution_calculatesExpectedCount_correctly (line 353), startTestExecution_asyncExecution_returnsImmediately (line 368), startTestExecution_statusInitiallyPending_beforeExecution (line 384), startTestExecution_duplicateTestNames_deduplicates (line 397), startTestExecution_testNamesWithSpaces_trimsCorrectly (line 411), startTestExecution_veryLongTestName_handlesCorrectly (line 425)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 4: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 134 in `startTestExecution_nullRequest_throwsBadRequestException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 5: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 220 in `startTestExecution_invalidServiceName_throwsBadRequestException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 231 in `startTestExecution_methodWithNoTests_throwsBadRequestException`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 6: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: PERMISSION
- Required: Add Success, Failure, Permission section headers.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 7 issues above.
- Fix Rule 10 issues above.


**IMPLEMENTATION PLAN (STEP-BY-STEP)**
1. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/QA/GetAllEndpointsWithTestsTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
2. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/QA/GetDashboardDataTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
3. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/QA/GetEndpointsWithTestsByServiceTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
4. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/QA/QAServiceBaseTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
5. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/QA/SaveTestRunTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
6. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/QA/GetAvailableServicesTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
7. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/QA/GetTestExecutionStatusTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
8. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/QA/GetCoverageSummaryTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
9. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/QA/GetLatestTestResultsTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
10. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/QA/BuildServiceInfoTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
11. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/QA/StartTestExecutionTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
12. Resolve extra test file `QAServiceBaseTest.java` by renaming it to match a public method or removing it.
13. Resolve extra test file `BuildServiceInfoTest.java` by renaming it to match a public method or removing it.

Verification Commands (run after fixes):
- mvn -Dtest=GetAllEndpointsWithTestsTest test
- mvn -Dtest=GetDashboardDataTest test
- mvn -Dtest=GetEndpointsWithTestsByServiceTest test
- mvn -Dtest=QAServiceBaseTest test
- mvn -Dtest=SaveTestRunTest test
- mvn -Dtest=GetAvailableServicesTest test