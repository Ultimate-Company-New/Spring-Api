# UNIT TEST VERIFICATION REPORT — UserGroup

```
╔════════════════════════════════════════════════════════════╗
║           UNIT TEST VERIFICATION REPORT                    ║
║                                                            ║
║  Status: ❌ RED                                               ║
║  Services Analyzed: 1                                      ║
║  Public Methods Found: 7                                  ║
║  Test Files Expected: 7                                  ║
║  Test Files Found: 7                                     ║
║  Total Violations: 48                                    ║
╚════════════════════════════════════════════════════════════╝
```

VIOLATIONS BY RULE:

| Rule | Description | Count |
| --- | --- | --- |
| 2 | Test Count Declaration | 7 |
| 3 | Controller Permission Test | 7 |
| 4 | Test Annotations | 1 |
| 5 | Test Naming Convention | 7 |
| 7 | Exception Assertions | 1 |
| 8 | Error Constants | 1 |
| 9 | Test Documentation | 7 |
| 10 | Test Ordering | 9 |
| 11 | Complete Coverage | 3 |
| 12 | Arrange/Act/Assert | 3 |
| 14 | No Inline Mocks | 2 |


**FILE-BY-FILE BREAKDOWN**

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/ToggleUserGroupTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.UserGroup
Class: public class ToggleUserGroupTest extends UserGroupServiceTestBase {
Extends: UserGroupServiceTestBase
Lines of Code: 293
Last Modified: 2026-02-10 14:33:04
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 13

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 13` immediately after the class opening brace.

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `toggleUserGroup_controller_permission_forbidden` or `toggleUserGroup_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 40 method `toggleUserGroup_VerifyPreAuthorizeAnnotation`
- Required rename: `toggleUserGroup_VerifyPreAuthorizeAnnotation_Success`
- Line: 104 method `toggleUserGroup_RestoresDeletedGroup`
- Required rename: `toggleUserGroup_RestoresDeletedGroup_Success`
- Line: 125 method `toggleUserGroup_Success`
- Required rename: `toggleUserGroup_Success_Success`
- Line: 166 method `toggleUserGroup_VerifyLoggingCalled`
- Required rename: `toggleUserGroup_VerifyLoggingCalled_Success`
- Line: 186 method `toggleUserGroup_VerifyRepositoryCalled`
- Required rename: `toggleUserGroup_VerifyRepositoryCalled_Success`

VIOLATION 4: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: toggleUserGroup_VerifyPreAuthorizeAnnotation (line 38), toggleUserGroup_WithValidId_DelegatesToService (line 53)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['PERMISSION', 'SUCCESS', 'FAILURE']
- Required: Success → Failure → Permission.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/UpdateUserGroupTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.UserGroup
Class: public class UpdateUserGroupTest extends UserGroupServiceTestBase {
Extends: UserGroupServiceTestBase
Lines of Code: 304
Last Modified: 2026-02-10 14:34:14
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 15

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 15` immediately after the class opening brace.

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `updateUserGroup_controller_permission_forbidden` or `updateUserGroup_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 45 method `updateUserGroup_VerifyPreAuthorizeAnnotation`
- Required rename: `updateUserGroup_VerifyPreAuthorizeAnnotation_Success`
- Line: 130 method `updateUserGroup_Success`
- Required rename: `updateUserGroup_Success_Success`
- Line: 153 method `updateUserGroup_VerifyNewMappingsCreated`
- Required rename: `updateUserGroup_VerifyNewMappingsCreated_Success`
- Line: 174 method `updateUserGroup_VerifyOldMappingsDeleted`
- Required rename: `updateUserGroup_VerifyOldMappingsDeleted_Success`

VIOLATION 4: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: updateUserGroup_VerifyPreAuthorizeAnnotation (line 43), updateUserGroup_WithValidRequest_DelegatesToService (line 58), updateUserGroup_SameNameSameGroup_Allowed (line 83), updateUserGroup_SpecialCharsInName_Success (line 105), updateUserGroup_Success (line 128), updateUserGroup_VerifyNewMappingsCreated (line 151), updateUserGroup_VerifyOldMappingsDeleted (line 172), updateUserGroup_DuplicateName_ThrowsBadRequestException (line 197), updateUserGroup_MaxLongId_ThrowsNotFoundException (line 215), updateUserGroup_NegativeId_ThrowsNotFoundException (line 228), updateUserGroup_NoUsers_ThrowsBadRequestException (line 241), updateUserGroup_NotFound_ThrowsNotFoundException (line 253), updateUserGroup_NullName_ThrowsBadRequestException (line 265), updateUserGroup_NullUsers_ThrowsBadRequestException (line 280), updateUserGroup_ZeroId_ThrowsNotFoundException (line 292)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['PERMISSION', 'SUCCESS', 'FAILURE']
- Required: Success → Failure → Permission.

VIOLATION 6: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: updateUserGroup_DuplicateName_ThrowsBadRequestException, updateUserGroup_MaxLongId_ThrowsNotFoundException, updateUserGroup_NegativeId_ThrowsNotFoundException, updateUserGroup_NoUsers_ThrowsBadRequestException, updateUserGroup_NotFound_ThrowsNotFoundException, updateUserGroup_NullName_ThrowsBadRequestException, updateUserGroup_NullUsers_ThrowsBadRequestException, updateUserGroup_ZeroId_ThrowsNotFoundException
- Required order: updateUserGroup_DuplicateName_ThrowsBadRequestException, updateUserGroup_MaxLongId_ThrowsNotFoundException, updateUserGroup_NegativeId_ThrowsNotFoundException, updateUserGroup_NotFound_ThrowsNotFoundException, updateUserGroup_NoUsers_ThrowsBadRequestException, updateUserGroup_NullName_ThrowsBadRequestException, updateUserGroup_NullUsers_ThrowsBadRequestException, updateUserGroup_ZeroId_ThrowsNotFoundException

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/FetchUserGroupsInClientInBatchesTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.UserGroup
Class: class FetchUserGroupsInClientInBatchesTest extends UserGroupServiceTestBase {
Extends: UserGroupServiceTestBase
Lines of Code: 277
Last Modified: 2026-02-10 14:48:12
Declared Test Count: MISSING/MISPLACED (first occurrence line 39)
Actual @Test Count: 3

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 39
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 3` immediately after the class opening brace.

VIOLATION 2: Rule 4 - Test Annotations
- Severity: HIGH
- Line: 94 has disallowed annotation @ParameterizedTest.
- Required: Remove or replace with allowed annotations only.
- Line: 95 has disallowed annotation @MethodSource.
- Required: Remove or replace with allowed annotations only.
- Line: 122 has disallowed annotation @ParameterizedTest.
- Required: Remove or replace with allowed annotations only.
- Line: 123 has disallowed annotation @MethodSource.
- Required: Remove or replace with allowed annotations only.
- Line: 150 has disallowed annotation @ParameterizedTest.
- Required: Remove or replace with allowed annotations only.
- Line: 151 has disallowed annotation @MethodSource.
- Required: Remove or replace with allowed annotations only.
- Line: 173 has disallowed annotation @ParameterizedTest.
- Required: Remove or replace with allowed annotations only.
- Line: 174 has disallowed annotation @ValueSource.
- Required: Remove or replace with allowed annotations only.
- Line: 199 has disallowed annotation @ParameterizedTest.
- Required: Remove or replace with allowed annotations only.
- Line: 200 has disallowed annotation @ValueSource.
- Required: Remove or replace with allowed annotations only.
- Line: 217 has disallowed annotation @ParameterizedTest.
- Required: Remove or replace with allowed annotations only.
- Line: 218 has disallowed annotation @ValueSource.
- Required: Remove or replace with allowed annotations only.
- Line: 231 has disallowed annotation @ParameterizedTest.
- Required: Remove or replace with allowed annotations only.
- Line: 232 has disallowed annotation @CsvSource.
- Required: Remove or replace with allowed annotations only.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `fetchUserGroupsInClientInBatches_controller_permission_forbidden` or `fetchUserGroupsInClientInBatches_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 50 method `getUserGroupsInBatches_VerifyPreAuthorizeAnnotation`
- Required rename: `getUserGroupsInBatches_VerifyPreAuthorizeAnnotation_Success`
- Line: 50 method `getUserGroupsInBatches_VerifyPreAuthorizeAnnotation`
- Required rename: `getUserGroupsInBatches_VerifyPreAuthorizeAnnotation_Success`
- Line: 65 method `getUserGroupsInBatches_WithValidRequest_DelegatesToService`
- Required rename: `fetchUserGroupsInClientInBatches_WithValidRequest_DelegatesToService`
- Line: 256 method `fetchUserGroupsInBatches_NoFilters_Success`
- Required rename: `fetchUserGroupsInClientInBatches_NoFilters_Success`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getUserGroupsInBatches_VerifyPreAuthorizeAnnotation (line 48), getUserGroupsInBatches_WithValidRequest_DelegatesToService (line 63), fetchUserGroupsInBatches_NoFilters_Success (line 254)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 73 inline mock in `getUserGroupsInBatches_WithValidRequest_DelegatesToService`: `doReturn(mockResponse).when(userGroupService).fetchUserGroupsInClientInBatches(request);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 7: Rule 8 - Error Constants
- Severity: HIGH
- Line: 214 has hardcoded message: `assertTrue(ex.getMessage().contains("Invalid column name"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 214 has hardcoded message: `assertTrue(ex.getMessage().contains("Invalid column name"));`
- Required: Replace with an ErrorMessages constant (add one if missing).

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: FAILURE, SUCCESS
- Required: Add Success, Failure, Permission section headers.

VIOLATION 9: Rule 11 - Complete Coverage
- Severity: HIGH
- Coverage by test names is incomplete.
- Missing: at least one failure/exception test (e.g., *_throws*, *_exception*, *_invalid*).

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 4 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 14 issues above.
- Fix Rule 8 issues above.
- Fix Rule 10 issues above.
- Fix Rule 11 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/BulkCreateUserGroupsTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.UserGroup
Class: class BulkCreateUserGroupsTest extends UserGroupServiceTestBase {
Extends: UserGroupServiceTestBase
Lines of Code: 285
Last Modified: 2026-02-10 14:43:11
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 10

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 10` immediately after the class opening brace.

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `bulkCreateUserGroups_controller_permission_forbidden` or `bulkCreateUserGroups_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 45 method `bulkCreateUserGroups_VerifyPreAuthorizeAnnotation`
- Required rename: `bulkCreateUserGroups_VerifyPreAuthorizeAnnotation_Success`
- Line: 119 method `bulkCreateUserGroups_PartialSuccess`
- Required rename: `bulkCreateUserGroups_PartialSuccess_Success`
- Line: 176 method `bulkCreateUserGroups_VerifyLoggingCalled`
- Required rename: `bulkCreateUserGroups_VerifyLoggingCalled_Success`
- Line: 203 method `bulkCreateUserGroups_AllFailures`
- Required rename: `bulkCreateUserGroups_AllFailures_Failure`
- Line: 229 method `bulkCreateUserGroups_DuplicateName`
- Required rename: `bulkCreateUserGroups_DuplicateName_Success`
- Line: 265 method `bulkCreateUserGroups_EmptyList`
- Required rename: `bulkCreateUserGroups_EmptyList_Success`
- Line: 278 method `bulkCreateUserGroups_NullList`
- Required rename: `bulkCreateUserGroups_NullList_Success`

VIOLATION 4: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: bulkCreateUserGroups_VerifyPreAuthorizeAnnotation (line 43), bulkCreateUserGroups_AllValid_Success (line 62), bulkCreateUserGroups_ManyGroups_Success (line 90), bulkCreateUserGroups_PartialSuccess (line 117), bulkCreateUserGroups_SingleGroup_Success (line 149), bulkCreateUserGroups_VerifyLoggingCalled (line 174), bulkCreateUserGroups_AllFailures (line 201), bulkCreateUserGroups_DuplicateName (line 227), bulkCreateUserGroups_EmptyList (line 263), bulkCreateUserGroups_NullList (line 276)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 5: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 278 in `bulkCreateUserGroups_NullList` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 6: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['PERMISSION', 'SUCCESS', 'FAILURE']
- Required: Success → Failure → Permission.

VIOLATION 7: Rule 11 - Complete Coverage
- Severity: HIGH
- Coverage by test names is incomplete.
- Missing: at least one failure/exception test (e.g., *_throws*, *_exception*, *_invalid*).

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 10 issues above.
- Fix Rule 11 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/GetUserGroupDetailsByIdTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.UserGroup
Class: public class GetUserGroupDetailsByIdTest extends UserGroupServiceTestBase {
Extends: UserGroupServiceTestBase
Lines of Code: 241
Last Modified: 2026-02-10 14:35:01
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 11

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 11` immediately after the class opening brace.

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getUserGroupDetailsById_controller_permission_forbidden` or `getUserGroupDetailsById_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 37 method `getUserGroupDetailsById_VerifyPreAuthorizeAnnotation`
- Required rename: `getUserGroupDetailsById_VerifyPreAuthorizeAnnotation_Success`
- Line: 76 method `getUserGroupDetailsById_RepositoryCalledOnce`
- Required rename: `getUserGroupDetailsById_RepositoryCalledOnce_Success`
- Line: 94 method `getUserGroupDetailsById_ReturnsCorrectFields`
- Required rename: `getUserGroupDetailsById_ReturnsCorrectFields_Success`
- Line: 115 method `getUserGroupDetailsById_ReturnsNonNullResponse`
- Required rename: `getUserGroupDetailsById_ReturnsNonNullResponse_Success`
- Line: 135 method `getUserGroupDetailsById_Success`
- Required rename: `getUserGroupDetailsById_Success_Success`

VIOLATION 4: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getUserGroupDetailsById_VerifyPreAuthorizeAnnotation (line 35), getUserGroupDetailsById_WithValidId_DelegatesToService (line 50)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['PERMISSION', 'SUCCESS', 'FAILURE']
- Required: Success → Failure → Permission.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/CreateUserGroupTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.UserGroup
Class: public class CreateUserGroupTest extends UserGroupServiceTestBase {
Extends: UserGroupServiceTestBase
Lines of Code: 320
Last Modified: 2026-02-10 14:32:08
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 19

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 19` immediately after the class opening brace.

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `createUserGroup_controller_permission_forbidden` or `createUserGroup_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 43 method `createUserGroup_VerifyPreAuthorizeAnnotation`
- Required rename: `createUserGroup_VerifyPreAuthorizeAnnotation_Success`
- Line: 148 method `createUserGroup_Success`
- Required rename: `createUserGroup_Success_Success`
- Line: 183 method `createUserGroup_VerifyMappingsSaved`
- Required rename: `createUserGroup_VerifyMappingsSaved_Success`
- Line: 199 method `createUserGroup_VerifyRepositorySaveCalled`
- Required rename: `createUserGroup_VerifyRepositorySaveCalled_Success`

VIOLATION 4: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: createUserGroup_VerifyPreAuthorizeAnnotation (line 41), createUserGroup_WithValidRequest_DelegatesToService (line 56), createUserGroup_LongDescription_Success (line 77), createUserGroup_ManyUsers_Success (line 94), createUserGroup_SingleUser_Success (line 111), createUserGroup_SpecialCharsInName_Success (line 128), createUserGroup_Success (line 146), createUserGroup_UnicodeCharsInName_Success (line 163), createUserGroup_VerifyMappingsSaved (line 181), createUserGroup_VerifyRepositorySaveCalled (line 197), createUserGroup_EmptyDescription_ThrowsBadRequestException (line 217), createUserGroup_EmptyGroupName_ThrowsBadRequestException (line 229), createUserGroup_GroupNameExists_ThrowsBadRequestException (line 241), createUserGroup_NoUsers_ThrowsBadRequestException (line 253), createUserGroup_NullDescription_ThrowsBadRequestException (line 265), createUserGroup_NullGroupName_ThrowsBadRequestException (line 277), createUserGroup_NullRequest_ThrowsNullPointerException (line 289), createUserGroup_NullUserList_ThrowsBadRequestException (line 297), createUserGroup_WhitespaceGroupName_ThrowsBadRequestException (line 309)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 5: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 291 in `createUserGroup_NullRequest_ThrowsNullPointerException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 6: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 293 in `createUserGroup_NullRequest_ThrowsNullPointerException`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 7: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['PERMISSION', 'SUCCESS', 'FAILURE']
- Required: Success → Failure → Permission.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 7 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/BulkCreateUserGroupsAsyncTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.UserGroup
Class: class BulkCreateUserGroupsAsyncTest extends UserGroupServiceTestBase {
Extends: UserGroupServiceTestBase
Lines of Code: 172
Last Modified: 2026-02-10 14:43:10
Declared Test Count: MISSING/MISPLACED (first occurrence line 20)
Actual @Test Count: 8

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 20
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 8` immediately after the class opening brace.

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `bulkCreateUserGroupsAsync_controller_permission_forbidden` or `bulkCreateUserGroupsAsync_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 30 method `bulkCreateUserGroups_VerifyPreAuthorizeAnnotation`
- Required rename: `bulkCreateUserGroups_VerifyPreAuthorizeAnnotation_Success`
- Line: 30 method `bulkCreateUserGroups_VerifyPreAuthorizeAnnotation`
- Required rename: `bulkCreateUserGroups_VerifyPreAuthorizeAnnotation_Success`
- Line: 45 method `bulkCreateUserGroups_WithValidRequest_DelegatesToService`
- Required rename: `bulkCreateUserGroupsAsync_WithValidRequest_DelegatesToService`

VIOLATION 4: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: bulkCreateUserGroups_VerifyPreAuthorizeAnnotation (line 28), bulkCreateUserGroups_WithValidRequest_DelegatesToService (line 43), bulkCreateUserGroupsAsync_success_allValid (line 63), bulkCreateUserGroupsAsync_success_partialFailures (line 82), bulkCreateUserGroupsAsync_failure_emptyList_sendsErrorMessage (line 114), bulkCreateUserGroupsAsync_failure_nullList_sendsErrorMessage (line 128), bulkCreateUserGroupsAsync_failure_duplicateName_recordsFailure (line 139), bulkCreateUserGroupsAsync_failure_unexpectedException_recordsFailure (line 156)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 5: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 130 in `bulkCreateUserGroupsAsync_failure_nullList_sendsErrorMessage` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 6: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 50 inline mock in `bulkCreateUserGroups_WithValidRequest_DelegatesToService`: `doNothing().when(userGroupService).bulkCreateUserGroupsAsync(anyList(), anyLong(), anyString(), anyLong());`
- Required: Move to base test stub method and call stub in test.
- Line: 94 inline mock in `bulkCreateUserGroupsAsync_success_partialFailures`: `lenient().when(userGroupRepository.findByGroupName(validRequest.getGroupName())).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 95 inline mock in `bulkCreateUserGroupsAsync_success_partialFailures`: `lenient().when(userGroupRepository.findByGroupName(duplicateRequest.getGroupName())).thenReturn(testUserGroup);`
- Required: Move to base test stub method and call stub in test.
- Line: 162 inline mock in `bulkCreateUserGroupsAsync_failure_unexpectedException_recordsFailure`: `lenient().when(userGroupRepository.save(any())).thenThrow(new RuntimeException("Unexpected error"));`
- Required: Move to base test stub method and call stub in test.

VIOLATION 7: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['PERMISSION', 'SUCCESS', 'FAILURE']
- Required: Success → Failure → Permission.

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: bulkCreateUserGroupsAsync_failure_emptyList_sendsErrorMessage, bulkCreateUserGroupsAsync_failure_nullList_sendsErrorMessage, bulkCreateUserGroupsAsync_failure_duplicateName_recordsFailure, bulkCreateUserGroupsAsync_failure_unexpectedException_recordsFailure
- Required order: bulkCreateUserGroupsAsync_failure_duplicateName_recordsFailure, bulkCreateUserGroupsAsync_failure_emptyList_sendsErrorMessage, bulkCreateUserGroupsAsync_failure_nullList_sendsErrorMessage, bulkCreateUserGroupsAsync_failure_unexpectedException_recordsFailure

VIOLATION 9: Rule 11 - Complete Coverage
- Severity: HIGH
- Coverage by test names is incomplete.
- Missing: at least one failure/exception test (e.g., *_throws*, *_exception*, *_invalid*).

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.
- Fix Rule 11 issues above.


**IMPLEMENTATION PLAN (STEP-BY-STEP)**
1. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/ToggleUserGroupTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
2. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/UpdateUserGroupTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
3. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/FetchUserGroupsInClientInBatchesTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
4. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/BulkCreateUserGroupsTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
5. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/GetUserGroupDetailsByIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
6. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/CreateUserGroupTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
7. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/BulkCreateUserGroupsAsyncTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.

Verification Commands (run after fixes):
- mvn -Dtest=ToggleUserGroupTest test
- mvn -Dtest=UpdateUserGroupTest test
- mvn -Dtest=FetchUserGroupsInClientInBatchesTest test
- mvn -Dtest=BulkCreateUserGroupsTest test
- mvn -Dtest=GetUserGroupDetailsByIdTest test
- mvn -Dtest=CreateUserGroupTest test