# UNIT TEST VERIFICATION REPORT — UserGroup

```
╔════════════════════════════════════════════════════════════╗
║           UNIT TEST VERIFICATION REPORT                    ║
║                                                            ║
║  Status: ❌ RED                                               ║
║  Services Analyzed: 1                                      ║
║  Public Methods Found: 7                                 ║
║  Test Files Expected: 7                                 ║
║  Test Files Found: 7                                  ║
║  Total Violations: 41                                 ║
╚════════════════════════════════════════════════════════════╝
```

VIOLATIONS BY RULE:

| Rule | Description | Count |
| --- | --- | --- |
| 2 | Test Count Declaration | 2 |
| 3 | Controller Permission Test | 7 |
| 7 | Exception Assertions | 1 |
| 9 | Test Documentation | 9 |
| 10 | Test Ordering | 8 |
| 12 | Arrange/Act/Assert | 1 |
| 14 | No Inline Mocks | 13 |


**FILE-BY-FILE BREAKDOWN**

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/BulkCreateUserGroupsAsyncTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.UserGroup
Class: class BulkCreateUserGroupsAsyncTest extends UserGroupServiceTestBase {
Extends: UserGroupServiceTestBase
Lines of Code: 232
Last Modified: 2026-02-11 11:30:59
Declared Test Count: 10 (first occurrence line 24)
Actual @Test Count: 9

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: HIGH
- Line: 24 Declared test count 10 does not match actual @Test count 9.
- Required: Ensure `// Total Tests: X` is the first line in the class body and matches the @Test count.
VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 37 Controller permission test missing HTTP status assertion.
- Required: Call controller method and assert HTTP status.
VIOLATION 3: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: bulkCreateUserGroupsAsync_VerifyPreAuthorizeAnnotation_Success (line 37), bulkCreateUserGroupsAsync_WithValidRequest_DelegatesToService (line 57), bulkCreateUserGroupsAsync_success_allValid (line 78), bulkCreateUserGroupsAsync_success_partialFailures (line 102), bulkCreateUserGroupsAsync_failure_emptyList_sendsErrorMessage (line 131), bulkCreateUserGroupsAsync_failure_nullList_sendsErrorMessage (line 145), bulkCreateUserGroupsAsync_failure_duplicateName_recordsFailure (line 161), bulkCreateUserGroupsAsync_failure_unexpectedException_recordsFailure (line 183), bulkCreateUserGroupsAsync_controller_permission_forbidden (line 209)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.
VIOLATION 4: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: bulkCreateUserGroupsAsync_VerifyPreAuthorizeAnnotation_Success, bulkCreateUserGroupsAsync_WithValidRequest_DelegatesToService, bulkCreateUserGroupsAsync_success_allValid, bulkCreateUserGroupsAsync_success_partialFailures
- Required order: bulkCreateUserGroupsAsync_success_allValid, bulkCreateUserGroupsAsync_success_partialFailures, bulkCreateUserGroupsAsync_VerifyPreAuthorizeAnnotation_Success, bulkCreateUserGroupsAsync_WithValidRequest_DelegatesToService
- Section FAILURE not alphabetical.
- Current order: bulkCreateUserGroupsAsync_failure_emptyList_sendsErrorMessage, bulkCreateUserGroupsAsync_failure_nullList_sendsErrorMessage, bulkCreateUserGroupsAsync_failure_duplicateName_recordsFailure, bulkCreateUserGroupsAsync_failure_unexpectedException_recordsFailure
- Required order: bulkCreateUserGroupsAsync_failure_duplicateName_recordsFailure, bulkCreateUserGroupsAsync_failure_emptyList_sendsErrorMessage, bulkCreateUserGroupsAsync_failure_nullList_sendsErrorMessage, bulkCreateUserGroupsAsync_failure_unexpectedException_recordsFailure
- Required: Add Success, Failure, Permission section headers and order tests alphabetically within each.
VIOLATION 5: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 145 in `bulkCreateUserGroupsAsync_failure_nullList_sendsErrorMessage` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
VIOLATION 6: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 215 inline mock in `bulkCreateUserGroupsAsync_controller_permission_forbidden`: `when(mockUserGroupService.getUserId()).thenReturn(TEST_USER_ID);`
- Required: Move to base test stub method and call stub in test.
- Line: 216 inline mock in `bulkCreateUserGroupsAsync_controller_permission_forbidden`: `when(mockUserGroupService.getUser()).thenReturn(CREATED_USER);`
- Required: Move to base test stub method and call stub in test.
- Line: 217 inline mock in `bulkCreateUserGroupsAsync_controller_permission_forbidden`: `when(mockUserGroupService.getClientId()).thenReturn(TEST_CLIENT_ID);`
- Required: Move to base test stub method and call stub in test.
- Line: 218 inline mock in `bulkCreateUserGroupsAsync_controller_permission_forbidden`: `doNothing().when(mockUserGroupService).bulkCreateUserGroupsAsync(anyList(), anyLong(), anyString(), anyLong());`
- Required: Move to base test stub method and call stub in test.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 9 issues above.
- Fix Rule 10 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.


======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/BulkCreateUserGroupsTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.UserGroup
Class: class BulkCreateUserGroupsTest extends UserGroupServiceTestBase {
Extends: UserGroupServiceTestBase
Lines of Code: 393
Last Modified: 2026-02-11 11:30:59
Declared Test Count: 13 (first occurrence line 42)
Actual @Test Count: 12

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: HIGH
- Line: 42 Declared test count 13 does not match actual @Test count 12.
- Required: Ensure `// Total Tests: X` is the first line in the class body and matches the @Test count.
VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 55 Controller permission test missing HTTP status assertion.
- Required: Call controller method and assert HTTP status.
VIOLATION 3: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: bulkCreateUserGroups_VerifyPreAuthorizeAnnotation_Success, bulkCreateUserGroups_WithValidRequest_DelegatesToService, bulkCreateUserGroups_AllValid_Success, bulkCreateUserGroups_ManyGroups_Success, bulkCreateUserGroups_PartialSuccess_Success, bulkCreateUserGroups_SingleGroup_Success, bulkCreateUserGroups_VerifyLoggingCalled_Success
- Required order: bulkCreateUserGroups_AllValid_Success, bulkCreateUserGroups_ManyGroups_Success, bulkCreateUserGroups_PartialSuccess_Success, bulkCreateUserGroups_SingleGroup_Success, bulkCreateUserGroups_VerifyLoggingCalled_Success, bulkCreateUserGroups_VerifyPreAuthorizeAnnotation_Success, bulkCreateUserGroups_WithValidRequest_DelegatesToService
- Required: Add Success, Failure, Permission section headers and order tests alphabetically within each.
VIOLATION 4: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 376 inline mock in `bulkCreateUserGroups_controller_permission_forbidden`: `when(mockUserGroupService.getUserId()).thenReturn(TEST_USER_ID);`
- Required: Move to base test stub method and call stub in test.
- Line: 377 inline mock in `bulkCreateUserGroups_controller_permission_forbidden`: `when(mockUserGroupService.getUser()).thenReturn(CREATED_USER);`
- Required: Move to base test stub method and call stub in test.
- Line: 378 inline mock in `bulkCreateUserGroups_controller_permission_forbidden`: `when(mockUserGroupService.getClientId()).thenReturn(TEST_CLIENT_ID);`
- Required: Move to base test stub method and call stub in test.
- Line: 379 inline mock in `bulkCreateUserGroups_controller_permission_forbidden`: `doNothing().when(mockUserGroupService).bulkCreateUserGroupsAsync(anyList(), anyLong(), anyString(), anyLong());`
- Required: Move to base test stub method and call stub in test.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 10 issues above.
- Fix Rule 14 issues above.


======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/CreateUserGroupTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.UserGroup
Class: public class CreateUserGroupTest extends UserGroupServiceTestBase {
Extends: UserGroupServiceTestBase
Lines of Code: 444
Last Modified: 2026-02-11 11:25:38
Declared Test Count: 20 (first occurrence line 38)
Actual @Test Count: 20

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 51 Controller permission test missing HTTP status assertion.
- Required: Call controller method and assert HTTP status.
VIOLATION 2: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 375 in `createUserGroup_NullRequest_ThrowsNullPointerException`
- Required: Exception message not asserted.
VIOLATION 3: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: createUserGroup_VerifyPreAuthorizeAnnotation_Success, createUserGroup_WithValidRequest_DelegatesToService, createUserGroup_LongDescription_Success, createUserGroup_ManyUsers_Success, createUserGroup_SingleUser_Success, createUserGroup_SpecialCharsInName_Success, createUserGroup_Success_Success, createUserGroup_UnicodeCharsInName_Success, createUserGroup_VerifyMappingsSaved_Success, createUserGroup_VerifyRepositorySaveCalled_Success
- Required order: createUserGroup_LongDescription_Success, createUserGroup_ManyUsers_Success, createUserGroup_SingleUser_Success, createUserGroup_SpecialCharsInName_Success, createUserGroup_Success_Success, createUserGroup_UnicodeCharsInName_Success, createUserGroup_VerifyMappingsSaved_Success, createUserGroup_VerifyPreAuthorizeAnnotation_Success, createUserGroup_VerifyRepositorySaveCalled_Success, createUserGroup_WithValidRequest_DelegatesToService
- Required: Add Success, Failure, Permission section headers and order tests alphabetically within each.
VIOLATION 4: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 433 inline mock in `createUserGroup_controller_permission_forbidden`: `doNothing().when(mockUserGroupService).createUserGroup(testUserGroupRequest);`
- Required: Move to base test stub method and call stub in test.

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 7 issues above.
- Fix Rule 10 issues above.
- Fix Rule 14 issues above.


======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/FetchUserGroupsInClientInBatchesTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.UserGroup
Class: class FetchUserGroupsInClientInBatchesTest extends UserGroupServiceTestBase {
Extends: UserGroupServiceTestBase
Lines of Code: 285
Last Modified: 2026-02-11 11:30:59
Declared Test Count: 10 (first occurrence line 35)
Actual @Test Count: 10

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 48 Controller permission test missing HTTP status assertion.
- Required: Call controller method and assert HTTP status.
VIOLATION 2: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: fetchUserGroupsInClientInBatches_VerifyPreAuthorizeAnnotation_Success, fetchUserGroupsInClientInBatches_WithValidRequest_DelegatesToService, fetchUserGroupsInClientInBatches_BooleanColumnFilter_Success, fetchUserGroupsInClientInBatches_NoFilters_Success, fetchUserGroupsInClientInBatches_NumberColumnFilter_Success, fetchUserGroupsInClientInBatches_StringColumnFilter_Success
- Required order: fetchUserGroupsInClientInBatches_BooleanColumnFilter_Success, fetchUserGroupsInClientInBatches_NoFilters_Success, fetchUserGroupsInClientInBatches_NumberColumnFilter_Success, fetchUserGroupsInClientInBatches_StringColumnFilter_Success, fetchUserGroupsInClientInBatches_VerifyPreAuthorizeAnnotation_Success, fetchUserGroupsInClientInBatches_WithValidRequest_DelegatesToService
- Required: Add Success, Failure, Permission section headers and order tests alphabetically within each.
VIOLATION 3: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 273 inline mock in `fetchUserGroupsInClientInBatches_controller_permission_forbidden`: `when(mockUserGroupService.fetchUserGroupsInClientInBatches(request)).thenReturn(mockResponse);`
- Required: Move to base test stub method and call stub in test.

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 10 issues above.
- Fix Rule 14 issues above.


======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/GetUserGroupDetailsByIdTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.UserGroup
Class: public class GetUserGroupDetailsByIdTest extends UserGroupServiceTestBase {
Extends: UserGroupServiceTestBase
Lines of Code: 278
Last Modified: 2026-02-11 11:25:38
Declared Test Count: 12 (first occurrence line 32)
Actual @Test Count: 12

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 45 Controller permission test missing HTTP status assertion.
- Required: Call controller method and assert HTTP status.
VIOLATION 2: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: getUserGroupDetailsById_VerifyPreAuthorizeAnnotation_Success, getUserGroupDetailsById_WithValidId_DelegatesToService, getUserGroupDetailsById_RepositoryCalledOnce_Success, getUserGroupDetailsById_ReturnsCorrectFields_Success, getUserGroupDetailsById_ReturnsNonNullResponse_Success, getUserGroupDetailsById_Success_Success
- Required order: getUserGroupDetailsById_RepositoryCalledOnce_Success, getUserGroupDetailsById_ReturnsCorrectFields_Success, getUserGroupDetailsById_ReturnsNonNullResponse_Success, getUserGroupDetailsById_Success_Success, getUserGroupDetailsById_VerifyPreAuthorizeAnnotation_Success, getUserGroupDetailsById_WithValidId_DelegatesToService
- Required: Add Success, Failure, Permission section headers and order tests alphabetically within each.
VIOLATION 3: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 267 inline mock in `getUserGroupDetailsById_controller_permission_forbidden`: `when(mockUserGroupService.getUserGroupDetailsById(TEST_GROUP_ID)).thenReturn(mockResponse);`
- Required: Move to base test stub method and call stub in test.

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 10 issues above.
- Fix Rule 14 issues above.


======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/ToggleUserGroupTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.UserGroup
Class: public class ToggleUserGroupTest extends UserGroupServiceTestBase {
Extends: UserGroupServiceTestBase
Lines of Code: 332
Last Modified: 2026-02-11 11:25:38
Declared Test Count: 14 (first occurrence line 35)
Actual @Test Count: 14

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 48 Controller permission test missing HTTP status assertion.
- Required: Call controller method and assert HTTP status.
VIOLATION 2: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: toggleUserGroup_VerifyPreAuthorizeAnnotation_Success, toggleUserGroup_WithValidId_DelegatesToService, toggleUserGroup_MultipleToggles_StatePersists, toggleUserGroup_RestoresDeletedGroup_Success, toggleUserGroup_Success_Success, toggleUserGroup_Success_LogsOperation, toggleUserGroup_VerifyLoggingCalled_Success, toggleUserGroup_VerifyRepositoryCalled_Success
- Required order: toggleUserGroup_MultipleToggles_StatePersists, toggleUserGroup_RestoresDeletedGroup_Success, toggleUserGroup_Success_LogsOperation, toggleUserGroup_Success_Success, toggleUserGroup_VerifyLoggingCalled_Success, toggleUserGroup_VerifyPreAuthorizeAnnotation_Success, toggleUserGroup_VerifyRepositoryCalled_Success, toggleUserGroup_WithValidId_DelegatesToService
- Required: Add Success, Failure, Permission section headers and order tests alphabetically within each.
VIOLATION 3: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 321 inline mock in `toggleUserGroup_controller_permission_forbidden`: `doNothing().when(mockUserGroupService).toggleUserGroup(TEST_GROUP_ID);`
- Required: Move to base test stub method and call stub in test.

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 10 issues above.
- Fix Rule 14 issues above.


======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/UpdateUserGroupTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.UserGroup
Class: public class UpdateUserGroupTest extends UserGroupServiceTestBase {
Extends: UserGroupServiceTestBase
Lines of Code: 406
Last Modified: 2026-02-11 11:25:38
Declared Test Count: 16 (first occurrence line 40)
Actual @Test Count: 16

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 53 Controller permission test missing HTTP status assertion.
- Required: Call controller method and assert HTTP status.
VIOLATION 2: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: updateUserGroup_VerifyPreAuthorizeAnnotation_Success, updateUserGroup_WithValidRequest_DelegatesToService, updateUserGroup_SameNameSameGroup_Allowed, updateUserGroup_SpecialCharsInName_Success, updateUserGroup_Success_Success, updateUserGroup_VerifyNewMappingsCreated_Success, updateUserGroup_VerifyOldMappingsDeleted_Success
- Required order: updateUserGroup_SameNameSameGroup_Allowed, updateUserGroup_SpecialCharsInName_Success, updateUserGroup_Success_Success, updateUserGroup_VerifyNewMappingsCreated_Success, updateUserGroup_VerifyOldMappingsDeleted_Success, updateUserGroup_VerifyPreAuthorizeAnnotation_Success, updateUserGroup_WithValidRequest_DelegatesToService
- Required: Add Success, Failure, Permission section headers and order tests alphabetically within each.
VIOLATION 3: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 395 inline mock in `updateUserGroup_controller_permission_forbidden`: `doNothing().when(mockUserGroupService).updateUserGroup(testUserGroupRequest);`
- Required: Move to base test stub method and call stub in test.

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 10 issues above.
- Fix Rule 14 issues above.


**IMPLEMENTATION PLAN (STEP-BY-STEP)**
1. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/BulkCreateUserGroupsAsyncTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
2. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/BulkCreateUserGroupsTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
3. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/CreateUserGroupTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
4. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/FetchUserGroupsInClientInBatchesTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
5. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/GetUserGroupDetailsByIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
6. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/ToggleUserGroupTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
7. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/UserGroup/UpdateUserGroupTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.

Verification Commands (run after fixes):
- mvn -Dtest=BulkCreateUserGroupsAsyncTest test
- mvn -Dtest=BulkCreateUserGroupsTest test
- mvn -Dtest=CreateUserGroupTest test
- mvn -Dtest=FetchUserGroupsInClientInBatchesTest test
- mvn -Dtest=GetUserGroupDetailsByIdTest test
- mvn -Dtest=ToggleUserGroupTest test