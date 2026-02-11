# UNIT TEST VERIFICATION REPORT — User

```
╔════════════════════════════════════════════════════════════╗
║           UNIT TEST VERIFICATION REPORT                    ║
║                                                            ║
║  Status: ❌ RED                                               ║
║  Services Analyzed: 1                                      ║
║  Public Methods Found: 9                                  ║
║  Test Files Expected: 9                                  ║
║  Test Files Found: 9                                     ║
║  Total Violations: 64                                    ║
╚════════════════════════════════════════════════════════════╝
```

VIOLATIONS BY RULE:

| Rule | Description | Count |
| --- | --- | --- |
| 2 | Test Count Declaration | 9 |
| 3 | Controller Permission Test | 9 |
| 5 | Test Naming Convention | 7 |
| 6 | Centralized Mocking | 9 |
| 7 | Exception Assertions | 2 |
| 8 | Error Constants | 2 |
| 9 | Test Documentation | 3 |
| 10 | Test Ordering | 13 |
| 12 | Arrange/Act/Assert | 2 |
| 13 | Stub Naming | 1 |
| 14 | No Inline Mocks | 7 |


**BASE TEST FILE ISSUES**
Base Test: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/UserServiceTestBase.java`
- Rule 13 [HIGH]: method `configurePasswordHelperMock` at line 166 performs mocking but does not start with `stub`. Rename to `stubConfigurePasswordHelperMock` and update callers.


**FILE-BY-FILE BREAKDOWN**

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/BulkCreateUsersAsyncTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.User
Class: class BulkCreateUsersAsyncTest extends UserServiceTestBase {
Extends: UserServiceTestBase
Lines of Code: 236
Last Modified: 2026-02-10 21:16:04
Declared Test Count: MISSING/MISPLACED (first occurrence line 19)
Actual @Test Count: 10

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 19
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 10` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 60 has mock usage `com.example.SpringApi.Services.UserService mockService = mock(com.example.SpringApi.Services.UserService.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 34 (bulkCreateUsers_controller_permission_forbidden)
- Problem: no http status assertion detected
- Required: Call controller method and assert HTTP status.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 34 method `bulkCreateUsers_controller_permission_forbidden`
- Required rename: `bulkCreateUsersAsync_controller_permission_forbidden`
- Line: 54 method `bulkCreateUsers_withValidRequest_delegatesToService`
- Required rename: `bulkCreateUsersAsync_withValidRequest_delegatesToService`

VIOLATION 5: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 192 in `bulkCreateUsersAsync_emptyList_throwsBadRequestException` missing AAA comments: Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 208 in `bulkCreateUsersAsync_nullList_throwsBadRequestException` missing AAA comments: Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 6: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 197 in `bulkCreateUsersAsync_emptyList_throwsBadRequestException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 210 in `bulkCreateUsersAsync_nullList_throwsBadRequestException`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 7: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['PERMISSION', 'SUCCESS', 'FAILURE']
- Required: Success → Failure → Permission.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 12 issues above.
- Fix Rule 7 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/CreateUserTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.User
Class: class CreateUserTest extends UserServiceTestBase {
Extends: UserServiceTestBase
Lines of Code: 368
Last Modified: 2026-02-10 14:23:50
Declared Test Count: MISSING/MISPLACED (first occurrence line 21)
Actual @Test Count: 16

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 21
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 16` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 59 has mock usage `com.example.SpringApi.Services.UserService mockUserService = mock(`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 36 (createUser_controller_permission_forbidden)
- Problem: no http status assertion detected
- Required: Call controller method and assert HTTP status.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 282 method `createUser_checksDuplicateEmail`
- Required rename: `createUser_checksDuplicateEmail_Success`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: createUser_duplicateLoginName_throwsBadRequestException (line 257)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 62 inline mock in `createUser_WithValidRequest_DelegatesToService`: `doNothing().when(mockUserService).createUser(request);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 7: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 287 in `createUser_checksDuplicateEmail`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 8: Rule 8 - Error Constants
- Severity: HIGH
- Line: 268 has hardcoded message: `assertTrue(ex.getMessage().contains("Login name (email) already exists"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 268 has hardcoded message: `assertTrue(ex.getMessage().contains("Login name (email) already exists"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 307 has hardcoded message: `assertTrue(ex.getMessage().contains("Login name (email) already exists"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 307 has hardcoded message: `assertTrue(ex.getMessage().contains("Login name (email) already exists"));`
- Required: Replace with an ErrorMessages constant (add one if missing).

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['PERMISSION', 'SUCCESS', 'FAILURE']
- Required: Success → Failure → Permission.

VIOLATION 10: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: createUser_success_createsUserWithPermissions, createUser_withAddress_savesAddress, createUser_withUserGroups_savesGroupMappings, createUser_success_logsOperation, createUser_success_createsUserClientMapping, createUser_singlePermission_success, createUser_manyPermissions_success, createUser_withoutAddress_success, createUser_withoutUserGroups_success, createUser_duplicateLoginName_throwsBadRequestException
- Required order: createUser_duplicateLoginName_throwsBadRequestException, createUser_manyPermissions_success, createUser_singlePermission_success, createUser_success_createsUserClientMapping, createUser_success_createsUserWithPermissions, createUser_success_logsOperation, createUser_withAddress_savesAddress, createUser_withoutAddress_success, createUser_withoutUserGroups_success, createUser_withUserGroups_savesGroupMappings

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 14 issues above.
- Fix Rule 7 issues above.
- Fix Rule 8 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/GetAllPermissionsTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.User
Class: class GetAllPermissionsTest extends UserServiceTestBase {
Extends: UserServiceTestBase
Lines of Code: 152
Last Modified: 2026-02-10 14:10:19
Declared Test Count: MISSING/MISPLACED (first occurrence line 20)
Actual @Test Count: 7

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 20
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 7` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 47 has mock usage `com.example.SpringApi.Services.UserService mockUserService = mock(`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 30 (getAllPermissions_controller_permission_forbidden)
- Problem: no http status assertion detected
- Required: Call controller method and assert HTTP status.

VIOLATION 4: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getAllPermissions_controller_permission_forbidden (line 28), getAllPermissions_WithValidRequest_DelegatesToService (line 43), getAllPermissions_Success_ReturnsNonDeletedPermissions (line 63), getAllPermissions_Success_SortsByPermissionIdAsc (line 80), getAllPermissions_Success_FiltersDeletedPermissions (line 100), getAllPermissions_Success_EmptyList (line 116), getAllPermissions_Success_AllDeletedReturnsEmpty (line 129)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 5: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 50 inline mock in `getAllPermissions_WithValidRequest_DelegatesToService`: `when(mockUserService.getAllPermissions()).thenReturn(new ArrayList<>());`
- Required: Move to base test stub method and call stub in test.

VIOLATION 6: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: FAILURE
- Required: Add Success, Failure, Permission section headers.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 3 issues above.
- Fix Rule 9 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/UpdateUserTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.User
Class: class UpdateUserTest extends UserServiceTestBase {
Extends: UserServiceTestBase
Lines of Code: 422
Last Modified: 2026-02-10 21:16:02
Declared Test Count: MISSING/MISPLACED (first occurrence line 27)
Actual @Test Count: 16

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 27
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 16` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 68 has mock usage `com.example.SpringApi.Services.UserService mockUserService = mock(`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 42 (updateUser_controller_permission_forbidden)
- Problem: no http status assertion detected
- Required: Call controller method and assert HTTP status.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 149 method `updateUser_updatesPermissions`
- Required rename: `updateUser_updatesPermissions_Success`
- Line: 168 method `updateUser_updatesUserGroups`
- Required rename: `updateUser_updatesUserGroups_Success`
- Line: 225 method `updateUser_createsNewAddressWhenNoneExists`
- Required rename: `updateUser_createsNewAddressWhenNoneExists_Success`

VIOLATION 5: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 71 inline mock in `updateUser_WithValidRequest_DelegatesToService`: `doNothing().when(mockUserService).updateUser(request);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 6: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['PERMISSION', 'SUCCESS', 'FAILURE']
- Required: Success → Failure → Permission.

VIOLATION 7: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: updateUser_singlePermission_success, updateUser_success_updatesUserDetails, updateUser_success_logsOperation, updateUser_updatesPermissions, updateUser_updatesUserGroups, updateUser_withAddress_updatesAddress
- Required order: updateUser_singlePermission_success, updateUser_success_logsOperation, updateUser_success_updatesUserDetails, updateUser_updatesPermissions, updateUser_updatesUserGroups, updateUser_withAddress_updatesAddress

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/FetchUsersInCarrierInBatchesTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.User
Class: class FetchUsersInCarrierInBatchesTest extends UserServiceTestBase {
Extends: UserServiceTestBase
Lines of Code: 349
Last Modified: 2026-02-10 14:23:42
Declared Test Count: MISSING/MISPLACED (first occurrence line 20)
Actual @Test Count: 21

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 20
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 21` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 64 has mock usage `// Since userService is @InjectMocks, we can't easily mock it unless we spy it`
- Required: Move mocks to base test file.
- Line: 65 has mock usage `// or assume it's mocked if we declared it as @Mock (but it's @InjectMocks).`
- Required: Move mocks to base test file.
- Line: 70 has mock usage `// It's defined as @InjectMocks.`
- Required: Move mocks to base test file.
- Line: 74 has mock usage `com.example.SpringApi.Services.UserService mockService = mock(`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 35 (fetchUsersInCarrierInBatches_controller_permission_forbidden)
- Problem: no http status assertion detected
- Required: Call controller method and assert HTTP status.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 106 method `fetchUsersInBatches_filterByFirstName_success`
- Required rename: `fetchUsersInCarrierInBatches_filterByFirstName_success`
- Line: 112 method `fetchUsersInBatches_filterByLastName_success`
- Required rename: `fetchUsersInCarrierInBatches_filterByLastName_success`
- Line: 118 method `fetchUsersInBatches_filterByEmail_success`
- Required rename: `fetchUsersInCarrierInBatches_filterByEmail_success`
- Line: 124 method `fetchUsersInBatches_filterByRole_success`
- Required rename: `fetchUsersInCarrierInBatches_filterByRole_success`
- Line: 130 method `fetchUsersInBatches_filterByPhone_success`
- Required rename: `fetchUsersInCarrierInBatches_filterByPhone_success`
- Line: 136 method `fetchUsersInBatches_filterByLoginName_success`
- Required rename: `fetchUsersInCarrierInBatches_filterByLoginName_success`
- Line: 159 method `fetchUsersInBatches_filterByUserId_success`
- Required rename: `fetchUsersInCarrierInBatches_filterByUserId_success`
- Line: 165 method `fetchUsersInBatches_filterByLoginAttempts_success`
- Required rename: `fetchUsersInCarrierInBatches_filterByLoginAttempts_success`
- Line: 171 method `fetchUsersInBatches_filterByAddressId_success`
- Required rename: `fetchUsersInCarrierInBatches_filterByAddressId_success`
- Line: 194 method `fetchUsersInBatches_filterByIsDeleted_success`
- Required rename: `fetchUsersInCarrierInBatches_filterByIsDeleted_success`
- Line: 200 method `fetchUsersInBatches_filterByLocked_success`
- Required rename: `fetchUsersInCarrierInBatches_filterByLocked_success`
- Line: 206 method `fetchUsersInBatches_filterByEmailConfirmed_success`
- Required rename: `fetchUsersInCarrierInBatches_filterByEmailConfirmed_success`
- Line: 229 method `fetchUsersInBatches_logicOperatorAND_success`
- Required rename: `fetchUsersInCarrierInBatches_logicOperatorAND_success`
- Line: 235 method `fetchUsersInBatches_logicOperatorOR_success`
- Required rename: `fetchUsersInCarrierInBatches_logicOperatorOR_success`
- Line: 263 method `fetchUsersInBatches_invalidColumn_throwsBadRequestException`
- Required rename: `fetchUsersInCarrierInBatches_invalidColumn_throwsBadRequestException`
- Line: 278 method `fetchUsersInBatches_invalidOperator_throwsBadRequestException`
- Required rename: `fetchUsersInCarrierInBatches_invalidOperator_throwsBadRequestException`
- Line: 295 method `fetchUsersInBatches_invalidLogicOperator_throwsBadRequestException`
- Required rename: `fetchUsersInCarrierInBatches_invalidLogicOperator_throwsBadRequestException`
- Line: 320 method `fetchUsersInBatches_invalidPagination_throwsBadRequestException`
- Required rename: `fetchUsersInCarrierInBatches_invalidPagination_throwsBadRequestException`
- Line: 332 method `fetchUsersInBatches_validPagination_success`
- Required rename: `fetchUsersInCarrierInBatches_validPagination_success`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: fetchUsersInBatches_filterByFirstName_success (line 104), fetchUsersInBatches_filterByLastName_success (line 110), fetchUsersInBatches_filterByEmail_success (line 116), fetchUsersInBatches_filterByRole_success (line 122), fetchUsersInBatches_filterByPhone_success (line 128), fetchUsersInBatches_filterByLoginName_success (line 134), fetchUsersInBatches_filterByUserId_success (line 157), fetchUsersInBatches_filterByLoginAttempts_success (line 163), fetchUsersInBatches_filterByAddressId_success (line 169), fetchUsersInBatches_filterByIsDeleted_success (line 192), fetchUsersInBatches_filterByLocked_success (line 198), fetchUsersInBatches_filterByEmailConfirmed_success (line 204), fetchUsersInBatches_logicOperatorAND_success (line 227), fetchUsersInBatches_logicOperatorOR_success (line 233), fetchUsersInBatches_invalidColumn_throwsBadRequestException (line 261), fetchUsersInBatches_invalidOperator_throwsBadRequestException (line 276), fetchUsersInBatches_invalidLogicOperator_throwsBadRequestException (line 293), fetchUsersInBatches_invalidPagination_throwsBadRequestException (line 318), fetchUsersInBatches_validPagination_success (line 330)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 106 in `fetchUsersInBatches_filterByFirstName_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 112 in `fetchUsersInBatches_filterByLastName_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 118 in `fetchUsersInBatches_filterByEmail_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 124 in `fetchUsersInBatches_filterByRole_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 130 in `fetchUsersInBatches_filterByPhone_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 136 in `fetchUsersInBatches_filterByLoginName_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 159 in `fetchUsersInBatches_filterByUserId_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 165 in `fetchUsersInBatches_filterByLoginAttempts_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 171 in `fetchUsersInBatches_filterByAddressId_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 194 in `fetchUsersInBatches_filterByIsDeleted_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 200 in `fetchUsersInBatches_filterByLocked_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 206 in `fetchUsersInBatches_filterByEmailConfirmed_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 229 in `fetchUsersInBatches_logicOperatorAND_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 235 in `fetchUsersInBatches_logicOperatorOR_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 263 in `fetchUsersInBatches_invalidColumn_throwsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 278 in `fetchUsersInBatches_invalidOperator_throwsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 295 in `fetchUsersInBatches_invalidLogicOperator_throwsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 320 in `fetchUsersInBatches_invalidPagination_throwsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 332 in `fetchUsersInBatches_validPagination_success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 8 - Error Constants
- Severity: HIGH
- Line: 273 has hardcoded message: `assertTrue(ex.getMessage().contains("Invalid column"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 273 has hardcoded message: `assertTrue(ex.getMessage().contains("Invalid column"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 290 has hardcoded message: `assertTrue(ex.getMessage().contains("Invalid operator"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 290 has hardcoded message: `assertTrue(ex.getMessage().contains("Invalid operator"));`
- Required: Replace with an ErrorMessages constant (add one if missing).

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: FAILURE, SUCCESS
- Required: Add Success, Failure, Permission section headers.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 8 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/GetUserByIdTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.User
Class: class GetUserByIdTest extends UserServiceTestBase {
Extends: UserServiceTestBase
Lines of Code: 240
Last Modified: 2026-02-10 14:15:25
Declared Test Count: MISSING/MISPLACED (first occurrence line 19)
Actual @Test Count: 10

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 19
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 10` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 57 has mock usage `com.example.SpringApi.Services.UserService mockUserService = mock(`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 34 (getUserById_controller_permission_forbidden)
- Problem: no http status assertion detected
- Required: Call controller method and assert HTTP status.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 100 method `getUserById_repositoryCalledOnce`
- Required rename: `getUserById_repositoryCalledOnce_Success`

VIOLATION 5: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 60 inline mock in `getUserById_WithValidId_DelegatesToService`: `doReturn(new UserResponseModel()).when(mockUserService).getUserById(userId);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 6: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['PERMISSION', 'SUCCESS', 'FAILURE']
- Required: Success → Failure → Permission.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/ConfirmEmailTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.User
Class: class ConfirmEmailTest extends UserServiceTestBase {
Extends: UserServiceTestBase
Lines of Code: 332
Last Modified: 2026-02-10 14:08:41
Declared Test Count: MISSING/MISPLACED (first occurrence line 17)
Actual @Test Count: 12

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 17
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 12` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 70 has mock usage `com.example.SpringApi.Services.UserService mockUserService = mock(`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `confirmEmail_controller_permission_forbidden` or `confirmEmail_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 118 method `confirmEmail_findByIdCalledOnce`
- Required rename: `confirmEmail_findByIdCalledOnce_Success`

VIOLATION 5: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 73 inline mock in `confirmEmail_withValidToken_delegatesToService`: `doNothing().when(mockUserService).confirmEmail(userId, token);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 6: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['PERMISSION', 'SUCCESS', 'FAILURE']
- Required: Success → Failure → Permission.

VIOLATION 7: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: confirmEmail_nullEmailConfirmed_success, confirmEmail_findByIdCalledOnce, confirmEmail_success_setsEmailConfirmedToTrue
- Required order: confirmEmail_findByIdCalledOnce, confirmEmail_nullEmailConfirmed_success, confirmEmail_success_setsEmailConfirmedToTrue

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/ToggleUserTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.User
Class: class ToggleUserTest extends UserServiceTestBase {
Extends: UserServiceTestBase
Lines of Code: 262
Last Modified: 2026-02-10 14:08:46
Declared Test Count: MISSING/MISPLACED (first occurrence line 18)
Actual @Test Count: 11

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 18
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 11` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 56 has mock usage `com.example.SpringApi.Services.UserService mockUserService = mock(`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 33 (toggleUser_controller_permission_forbidden)
- Problem: no http status assertion detected
- Required: Call controller method and assert HTTP status.

VIOLATION 4: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 59 inline mock in `toggleUser_WithValidId_DelegatesToService`: `doNothing().when(mockUserService).toggleUser(userId);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['PERMISSION', 'SUCCESS', 'FAILURE']
- Required: Success → Failure → Permission.

VIOLATION 6: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: toggleUser_success_logsOperation, toggleUser_multipleToggles_statePersists, toggleUser_success_restoresDeletedUser, toggleUser_success_setsIsDeletedTrue, toggleUser_success_updatesModifiedUser
- Required order: toggleUser_multipleToggles_statePersists, toggleUser_success_logsOperation, toggleUser_success_restoresDeletedUser, toggleUser_success_setsIsDeletedTrue, toggleUser_success_updatesModifiedUser

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 3 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/GetUserByEmailTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.User
Class: class GetUserByEmailTest extends UserServiceTestBase {
Extends: UserServiceTestBase
Lines of Code: 261
Last Modified: 2026-02-10 14:15:27
Declared Test Count: MISSING/MISPLACED (first occurrence line 20)
Actual @Test Count: 11

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 20
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 11` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 58 has mock usage `com.example.SpringApi.Services.UserService mockUserService = mock(`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 35 (getUserByEmail_controller_permission_forbidden)
- Problem: no http status assertion detected
- Required: Call controller method and assert HTTP status.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 103 method `getUserByEmail_repositoryCalledOnce`
- Required rename: `getUserByEmail_repositoryCalledOnce_Success`

VIOLATION 5: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 61 inline mock in `getUserByEmail_WithValidEmail_DelegatesToService`: `doReturn(new UserResponseModel()).when(mockUserService).getUserByEmail(email);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 6: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['PERMISSION', 'SUCCESS', 'FAILURE']
- Required: Success → Failure → Permission.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.


**IMPLEMENTATION PLAN (STEP-BY-STEP)**
1. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/BulkCreateUsersAsyncTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
2. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/CreateUserTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
3. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/GetAllPermissionsTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
4. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/UpdateUserTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
5. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/FetchUsersInCarrierInBatchesTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
6. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/GetUserByIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
7. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/ConfirmEmailTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
8. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/ToggleUserTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
9. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/GetUserByEmailTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
10. Fix base test `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/User/UserServiceTestBase.java` violations noted above.

Verification Commands (run after fixes):
- mvn -Dtest=BulkCreateUsersAsyncTest test
- mvn -Dtest=CreateUserTest test
- mvn -Dtest=GetAllPermissionsTest test
- mvn -Dtest=UpdateUserTest test
- mvn -Dtest=FetchUsersInCarrierInBatchesTest test
- mvn -Dtest=GetUserByIdTest test