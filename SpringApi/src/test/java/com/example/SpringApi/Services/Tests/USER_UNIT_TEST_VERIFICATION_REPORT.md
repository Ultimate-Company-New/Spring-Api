# UNIT TEST VERIFICATION REPORT — User

```
╔════════════════════════════════════════════════════════════╗
║           UNIT TEST VERIFICATION REPORT                    ║
║                                                            ║
║  Status: ❌ RED                                               ║
║  Services Analyzed: 1                                      ║
║  Public Methods Found: 9                                 ║
║  Test Files Expected: 9                                 ║
║  Test Files Found: 9                                  ║
║  Total Violations: 76                                 ║
╚════════════════════════════════════════════════════════════╝
```

VIOLATIONS BY RULE:

| Rule | Description | Count |
| --- | --- | --- |
| 3 | Controller Permission Test | 3 |
| 5 | Test Naming Convention | 2 |
| 6 | Centralized Mocking | 2 |
| 7 | Exception Assertions | 4 |
| 9 | Test Documentation | 19 |
| 10 | Test Ordering | 14 |
| 12 | Arrange/Act/Assert | 20 |
| 14 | No Inline Mocks | 12 |


**FILE-BY-FILE BREAKDOWN**

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/BulkCreateUsersAsyncTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.User
Class: class BulkCreateUsersAsyncTest extends UserServiceTestBase {
Extends: UserServiceTestBase
Lines of Code: 234
Last Modified: 2026-02-11 11:25:38
Declared Test Count: 10 (first occurrence line 21)
Actual @Test Count: 10

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 203 Controller permission test missing HTTP status assertion.
- Required: Call controller method and assert HTTP status.
VIOLATION 2: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 169 in `bulkCreateUsersAsync_emptyList_throwsBadRequestException`
- Required: assertThrows result not captured to variable.
- Line: 169 in `bulkCreateUsersAsync_emptyList_throwsBadRequestException`
- Required: Exception message not asserted.
- Line: 186 in `bulkCreateUsersAsync_nullList_throwsBadRequestException`
- Required: assertThrows result not captured to variable.
- Line: 186 in `bulkCreateUsersAsync_nullList_throwsBadRequestException`
- Required: Exception message not asserted.
VIOLATION 3: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 186 in `bulkCreateUsersAsync_nullList_throwsBadRequestException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 7 issues above.
- Fix Rule 12 issues above.


======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/ConfirmEmailTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.User
Class: class ConfirmEmailTest extends UserServiceTestBase {
Extends: UserServiceTestBase
Lines of Code: 326
Last Modified: 2026-02-11 11:25:38
Declared Test Count: 13 (first occurrence line 23)
Actual @Test Count: 13

VIOLATIONS FOUND:

VIOLATION 1: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 36 method `confirmEmail_verifyPreAuthorizeAnnotation`
- Required: Must follow <method>_<type>_<outcome> format.
VIOLATION 2: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['SUCCESS', 'PERMISSION', 'FAILURE']. Required: Success → Failure → Permission.
- Section SUCCESS not alphabetical.
- Current order: confirmEmail_nullEmailConfirmed_success, confirmEmail_findByIdCalledOnce_Success, confirmEmail_success_setsEmailConfirmedToTrue
- Required order: confirmEmail_findByIdCalledOnce_Success, confirmEmail_nullEmailConfirmed_success, confirmEmail_success_setsEmailConfirmedToTrue
- Required: Add Success, Failure, Permission section headers and order tests alphabetically within each.
VIOLATION 3: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 42 inline mock in `confirmEmail_verifyPreAuthorizeAnnotation`: `doNothing().when(mockUserService).confirmEmail(userId, token);`
- Required: Move to base test stub method and call stub in test.
- Line: 66 inline mock in `confirmEmail_withValidToken_delegatesToService`: `doNothing().when(mockUserService).confirmEmail(userId, token);`
- Required: Move to base test stub method and call stub in test.

REQUIRED FIXES SUMMARY:
- Fix Rule 5 issues above.
- Fix Rule 10 issues above.
- Fix Rule 14 issues above.


======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/CreateUserTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.User
Class: class CreateUserTest extends UserServiceTestBase {
Extends: UserServiceTestBase
Lines of Code: 362
Last Modified: 2026-02-11 11:30:59
Declared Test Count: 16 (first occurrence line 25)
Actual @Test Count: 16

VIOLATIONS FOUND:

VIOLATION 1: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: createUser_controller_permission_forbidden, createUser_WithValidRequest_DelegatesToService, createUser_manyPermissions_success, createUser_singlePermission_success, createUser_success_createsUserClientMapping, createUser_success_createsUserWithPermissions, createUser_success_logsOperation, createUser_withAddress_savesAddress, createUser_withoutAddress_success, createUser_withoutUserGroups_success, createUser_withUserGroups_savesGroupMappings
- Required order: createUser_controller_permission_forbidden, createUser_manyPermissions_success, createUser_singlePermission_success, createUser_success_createsUserClientMapping, createUser_success_createsUserWithPermissions, createUser_success_logsOperation, createUser_withAddress_savesAddress, createUser_withoutAddress_success, createUser_withoutUserGroups_success, createUser_withUserGroups_savesGroupMappings, createUser_WithValidRequest_DelegatesToService
- Section FAILURE not alphabetical.
- Current order: createUser_checksDuplicateEmail_Success, createUser_duplicateLoginName_throwsBadRequestException, createUser_duplicateEmail_throwsBadRequestException, createUser_emptyPermissions_throwsBadRequestException, createUser_nullPermissions_throwsBadRequestException
- Required order: createUser_checksDuplicateEmail_Success, createUser_duplicateEmail_throwsBadRequestException, createUser_duplicateLoginName_throwsBadRequestException, createUser_emptyPermissions_throwsBadRequestException, createUser_nullPermissions_throwsBadRequestException
- Required: Add Success, Failure, Permission section headers and order tests alphabetically within each.
VIOLATION 2: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 43 inline mock in `createUser_controller_permission_forbidden`: `doNothing().when(mockUserService).createUser(request);`
- Required: Move to base test stub method and call stub in test.
- Line: 67 inline mock in `createUser_WithValidRequest_DelegatesToService`: `doNothing().when(mockUserService).createUser(request);`
- Required: Move to base test stub method and call stub in test.

REQUIRED FIXES SUMMARY:
- Fix Rule 10 issues above.
- Fix Rule 14 issues above.


======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/FetchUsersInCarrierInBatchesTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.User
Class: class FetchUsersInCarrierInBatchesTest extends UserServiceTestBase {
Extends: UserServiceTestBase
Lines of Code: 280
Last Modified: 2026-02-11 11:30:59
Declared Test Count: 21 (first occurrence line 24)
Actual @Test Count: 21

VIOLATIONS FOUND:

VIOLATION 1: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: fetchUsersInCarrierInBatches_filterByFirstName_success (line 100), fetchUsersInCarrierInBatches_filterByLastName_success (line 106), fetchUsersInCarrierInBatches_filterByEmail_success (line 112), fetchUsersInCarrierInBatches_filterByRole_success (line 118), fetchUsersInCarrierInBatches_filterByPhone_success (line 124), fetchUsersInCarrierInBatches_filterByLoginName_success (line 130), fetchUsersInCarrierInBatches_filterByUserId_success (line 140), fetchUsersInCarrierInBatches_filterByLoginAttempts_success (line 146), fetchUsersInCarrierInBatches_filterByAddressId_success (line 152), fetchUsersInCarrierInBatches_filterByIsDeleted_success (line 162), fetchUsersInCarrierInBatches_filterByLocked_success (line 168), fetchUsersInCarrierInBatches_filterByEmailConfirmed_success (line 174), fetchUsersInCarrierInBatches_logicOperatorAND_success (line 184), fetchUsersInCarrierInBatches_logicOperatorOR_success (line 190), fetchUsersInCarrierInBatches_invalidColumn_throwsBadRequestException (line 200), fetchUsersInCarrierInBatches_invalidOperator_throwsBadRequestException (line 215), fetchUsersInCarrierInBatches_invalidLogicOperator_throwsBadRequestException (line 232), fetchUsersInCarrierInBatches_invalidPagination_throwsBadRequestException (line 257), fetchUsersInCarrierInBatches_validPagination_success (line 269)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.
VIOLATION 2: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: SUCCESS, FAILURE.
- Section PERMISSION not alphabetical.
- Current order: fetchUsersInCarrierInBatches_controller_permission_forbidden, fetchUsersInCarrierInBatches_withValidRequest_delegatesToService, fetchUsersInCarrierInBatches_filterByFirstName_success, fetchUsersInCarrierInBatches_filterByLastName_success, fetchUsersInCarrierInBatches_filterByEmail_success, fetchUsersInCarrierInBatches_filterByRole_success, fetchUsersInCarrierInBatches_filterByPhone_success, fetchUsersInCarrierInBatches_filterByLoginName_success, fetchUsersInCarrierInBatches_filterByUserId_success, fetchUsersInCarrierInBatches_filterByLoginAttempts_success, fetchUsersInCarrierInBatches_filterByAddressId_success, fetchUsersInCarrierInBatches_filterByIsDeleted_success, fetchUsersInCarrierInBatches_filterByLocked_success, fetchUsersInCarrierInBatches_filterByEmailConfirmed_success, fetchUsersInCarrierInBatches_logicOperatorAND_success, fetchUsersInCarrierInBatches_logicOperatorOR_success, fetchUsersInCarrierInBatches_invalidColumn_throwsBadRequestException, fetchUsersInCarrierInBatches_invalidOperator_throwsBadRequestException, fetchUsersInCarrierInBatches_invalidLogicOperator_throwsBadRequestException, fetchUsersInCarrierInBatches_invalidPagination_throwsBadRequestException, fetchUsersInCarrierInBatches_validPagination_success
- Required order: fetchUsersInCarrierInBatches_controller_permission_forbidden, fetchUsersInCarrierInBatches_filterByAddressId_success, fetchUsersInCarrierInBatches_filterByEmail_success, fetchUsersInCarrierInBatches_filterByEmailConfirmed_success, fetchUsersInCarrierInBatches_filterByFirstName_success, fetchUsersInCarrierInBatches_filterByIsDeleted_success, fetchUsersInCarrierInBatches_filterByLastName_success, fetchUsersInCarrierInBatches_filterByLocked_success, fetchUsersInCarrierInBatches_filterByLoginAttempts_success, fetchUsersInCarrierInBatches_filterByLoginName_success, fetchUsersInCarrierInBatches_filterByPhone_success, fetchUsersInCarrierInBatches_filterByRole_success, fetchUsersInCarrierInBatches_filterByUserId_success, fetchUsersInCarrierInBatches_invalidColumn_throwsBadRequestException, fetchUsersInCarrierInBatches_invalidLogicOperator_throwsBadRequestException, fetchUsersInCarrierInBatches_invalidOperator_throwsBadRequestException, fetchUsersInCarrierInBatches_invalidPagination_throwsBadRequestException, fetchUsersInCarrierInBatches_logicOperatorAND_success, fetchUsersInCarrierInBatches_logicOperatorOR_success, fetchUsersInCarrierInBatches_validPagination_success, fetchUsersInCarrierInBatches_withValidRequest_delegatesToService
- Required: Add Success, Failure, Permission section headers and order tests alphabetically within each.
VIOLATION 3: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 100 in `fetchUsersInCarrierInBatches_filterByFirstName_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 106 in `fetchUsersInCarrierInBatches_filterByLastName_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 112 in `fetchUsersInCarrierInBatches_filterByEmail_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 118 in `fetchUsersInCarrierInBatches_filterByRole_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 124 in `fetchUsersInCarrierInBatches_filterByPhone_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 130 in `fetchUsersInCarrierInBatches_filterByLoginName_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 140 in `fetchUsersInCarrierInBatches_filterByUserId_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 146 in `fetchUsersInCarrierInBatches_filterByLoginAttempts_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 152 in `fetchUsersInCarrierInBatches_filterByAddressId_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 162 in `fetchUsersInCarrierInBatches_filterByIsDeleted_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 168 in `fetchUsersInCarrierInBatches_filterByLocked_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 174 in `fetchUsersInCarrierInBatches_filterByEmailConfirmed_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 184 in `fetchUsersInCarrierInBatches_logicOperatorAND_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 190 in `fetchUsersInCarrierInBatches_logicOperatorOR_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 200 in `fetchUsersInCarrierInBatches_invalidColumn_throwsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 215 in `fetchUsersInCarrierInBatches_invalidOperator_throwsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 232 in `fetchUsersInCarrierInBatches_invalidLogicOperator_throwsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 257 in `fetchUsersInCarrierInBatches_invalidPagination_throwsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 269 in `fetchUsersInCarrierInBatches_validPagination_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

REQUIRED FIXES SUMMARY:
- Fix Rule 9 issues above.
- Fix Rule 10 issues above.
- Fix Rule 12 issues above.


======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/GetAllPermissionsTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.User
Class: class GetAllPermissionsTest extends UserServiceTestBase {
Extends: UserServiceTestBase
Lines of Code: 182
Last Modified: 2026-02-11 11:30:59
Declared Test Count: 7 (first occurrence line 24)
Actual @Test Count: 7

VIOLATIONS FOUND:

VIOLATION 1: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: FAILURE.
- Section SUCCESS not alphabetical.
- Current order: getAllPermissions_Success_FiltersDeletedPermissions, getAllPermissions_Success_EmptyList, getAllPermissions_Success_ReturnsNonDeletedPermissions, getAllPermissions_Success_SortsByPermissionIdAsc, getAllPermissions_Success_AllDeletedReturnsEmpty
- Required order: getAllPermissions_Success_AllDeletedReturnsEmpty, getAllPermissions_Success_EmptyList, getAllPermissions_Success_FiltersDeletedPermissions, getAllPermissions_Success_ReturnsNonDeletedPermissions, getAllPermissions_Success_SortsByPermissionIdAsc
- Required: Add Success, Failure, Permission section headers and order tests alphabetically within each.
VIOLATION 2: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 150 inline mock in `getAllPermissions_controller_permission_forbidden`: `when(mockUserService.getAllPermissions()).thenReturn(new ArrayList<>());`
- Required: Move to base test stub method and call stub in test.
- Line: 173 inline mock in `getAllPermissions_WithValidRequest_DelegatesToService`: `when(mockUserService.getAllPermissions()).thenReturn(new ArrayList<>());`
- Required: Move to base test stub method and call stub in test.

REQUIRED FIXES SUMMARY:
- Fix Rule 10 issues above.
- Fix Rule 14 issues above.


======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/GetUserByEmailTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.User
Class: class GetUserByEmailTest extends UserServiceTestBase {
Extends: UserServiceTestBase
Lines of Code: 264
Last Modified: 2026-02-10 23:11:31
Declared Test Count: 11 (first occurrence line 22)
Actual @Test Count: 11

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 37 Controller permission test missing HTTP status assertion.
- Required: Call controller method and assert HTTP status.
VIOLATION 2: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 106 method `getUserByEmail_repositoryCalledOnce`
- Required: Must follow <method>_<type>_<outcome> format.
VIOLATION 3: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 60 has mock usage `com.example.SpringApi.Services.UserService mockUserService = mock(`
- Required: Move mocks to base test file.
VIOLATION 4: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['PERMISSION', 'SUCCESS', 'FAILURE']. Required: Success → Failure → Permission.
- Required: Add Success, Failure, Permission section headers and order tests alphabetically within each.
VIOLATION 5: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 63 inline mock in `getUserByEmail_WithValidEmail_DelegatesToService`: `doReturn(new UserResponseModel()).when(mockUserService).getUserByEmail(email);`
- Required: Move to base test stub method and call stub in test.

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 6 issues above.
- Fix Rule 10 issues above.
- Fix Rule 14 issues above.


======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/GetUserByIdTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.User
Class: class GetUserByIdTest extends UserServiceTestBase {
Extends: UserServiceTestBase
Lines of Code: 246
Last Modified: 2026-02-11 11:25:38
Declared Test Count: 10 (first occurrence line 23)
Actual @Test Count: 10

VIOLATIONS FOUND:

VIOLATION 1: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['PERMISSION', 'SUCCESS', 'FAILURE']. Required: Success → Failure → Permission.
- Required: Add Success, Failure, Permission section headers and order tests alphabetically within each.
VIOLATION 2: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 41 inline mock in `getUserById_controller_permission_forbidden`: `doReturn(new UserResponseModel()).when(mockUserService).getUserById(userId);`
- Required: Move to base test stub method and call stub in test.
- Line: 65 inline mock in `getUserById_WithValidId_DelegatesToService`: `doReturn(new UserResponseModel()).when(mockUserService).getUserById(userId);`
- Required: Move to base test stub method and call stub in test.

REQUIRED FIXES SUMMARY:
- Fix Rule 10 issues above.
- Fix Rule 14 issues above.


======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/ToggleUserTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.User
Class: class ToggleUserTest extends UserServiceTestBase {
Extends: UserServiceTestBase
Lines of Code: 265
Last Modified: 2026-02-10 23:11:31
Declared Test Count: 11 (first occurrence line 20)
Actual @Test Count: 11

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 35 Controller permission test missing HTTP status assertion.
- Required: Call controller method and assert HTTP status.
VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 58 has mock usage `com.example.SpringApi.Services.UserService mockUserService = mock(`
- Required: Move mocks to base test file.
VIOLATION 3: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['PERMISSION', 'SUCCESS', 'FAILURE']. Required: Success → Failure → Permission.
- Section SUCCESS not alphabetical.
- Current order: toggleUser_success_logsOperation, toggleUser_multipleToggles_statePersists, toggleUser_success_restoresDeletedUser, toggleUser_success_setsIsDeletedTrue, toggleUser_success_updatesModifiedUser
- Required order: toggleUser_multipleToggles_statePersists, toggleUser_success_logsOperation, toggleUser_success_restoresDeletedUser, toggleUser_success_setsIsDeletedTrue, toggleUser_success_updatesModifiedUser
- Required: Add Success, Failure, Permission section headers and order tests alphabetically within each.
VIOLATION 4: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 61 inline mock in `toggleUser_WithValidId_DelegatesToService`: `doNothing().when(mockUserService).toggleUser(userId);`
- Required: Move to base test stub method and call stub in test.

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 6 issues above.
- Fix Rule 10 issues above.
- Fix Rule 14 issues above.


======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/UpdateUserTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.User
Class: class UpdateUserTest extends UserServiceTestBase {
Extends: UserServiceTestBase
Lines of Code: 412
Last Modified: 2026-02-11 11:30:59
Declared Test Count: 16 (first occurrence line 31)
Actual @Test Count: 16

VIOLATIONS FOUND:

VIOLATION 1: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['PERMISSION', 'SUCCESS', 'FAILURE']. Required: Success → Failure → Permission.
- Section SUCCESS not alphabetical.
- Current order: updateUser_singlePermission_success, updateUser_success_updatesUserDetails, updateUser_success_logsOperation, updateUser_updatesPermissions_Success, updateUser_updatesUserGroups_Success, updateUser_withAddress_updatesAddress
- Required order: updateUser_singlePermission_success, updateUser_success_logsOperation, updateUser_success_updatesUserDetails, updateUser_updatesPermissions_Success, updateUser_updatesUserGroups_Success, updateUser_withAddress_updatesAddress
- Required: Add Success, Failure, Permission section headers and order tests alphabetically within each.
VIOLATION 2: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 51 inline mock in `updateUser_controller_permission_forbidden`: `doNothing().when(mockUserService).updateUser(request);`
- Required: Move to base test stub method and call stub in test.
- Line: 77 inline mock in `updateUser_WithValidRequest_DelegatesToService`: `doNothing().when(mockUserService).updateUser(request);`
- Required: Move to base test stub method and call stub in test.

REQUIRED FIXES SUMMARY:
- Fix Rule 10 issues above.
- Fix Rule 14 issues above.


**IMPLEMENTATION PLAN (STEP-BY-STEP)**
1. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/BulkCreateUsersAsyncTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
2. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/ConfirmEmailTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
3. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/CreateUserTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
4. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/FetchUsersInCarrierInBatchesTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
5. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/GetAllPermissionsTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
6. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/GetUserByEmailTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
7. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/GetUserByIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
8. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/ToggleUserTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
9. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/UpdateUserTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.

Verification Commands (run after fixes):
- mvn -Dtest=BulkCreateUsersAsyncTest test
- mvn -Dtest=ConfirmEmailTest test
- mvn -Dtest=CreateUserTest test
- mvn -Dtest=FetchUsersInCarrierInBatchesTest test
- mvn -Dtest=GetAllPermissionsTest test
- mvn -Dtest=GetUserByEmailTest test