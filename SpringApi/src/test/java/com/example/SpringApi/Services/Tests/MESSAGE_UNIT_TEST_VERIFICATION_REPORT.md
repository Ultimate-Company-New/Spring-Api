# UNIT TEST VERIFICATION REPORT — Message

```
╔════════════════════════════════════════════════════════════╗
║           UNIT TEST VERIFICATION REPORT                    ║
║                                                            ║
║  Status: ❌ RED                                               ║
║  Services Analyzed: 1                                      ║
║  Public Methods Found: 9                                  ║
║  Test Files Expected: 9                                  ║
║  Test Files Found: 10                                    ║
║  Total Violations: 29                                    ║
╚════════════════════════════════════════════════════════════╝
```

VIOLATIONS BY RULE:

| Rule | Description | Count |
| --- | --- | --- |
| 1 | One Test File per Method | 1 |
| 2 | Test Count Declaration | 3 |
| 3 | Controller Permission Test | 6 |
| 5 | Test Naming Convention | 5 |
| 7 | Exception Assertions | 1 |
| 8 | Error Constants | 5 |
| 10 | Test Ordering | 5 |
| 14 | No Inline Mocks | 3 |


**MISSING/EXTRA TEST FILES (RULE 1)**
Extra test file with no matching public method: `SetMessageReadTest.java`. Either rename it to match a public method or remove it.


**FILE-BY-FILE BREAKDOWN**

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Message/CreateMessageTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Message
Class: class CreateMessageTest extends MessageServiceTestBase {
Extends: MessageServiceTestBase
Lines of Code: 665
Last Modified: 2026-02-10 23:11:31
Declared Test Count: 30 (first occurrence line 34)
Actual @Test Count: 31

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 34
- Current: 30
- Required: 31

VIOLATION 2: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 636 inline mock in `createMessage_controller_permission_unauthorized`: `doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 637 inline mock in `createMessage_controller_permission_unauthorized`: `.when(messageServiceMock).createMessage(any());`
- Required: Move to base test stub method and call stub in test.

VIOLATION 3: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: createMessage_BlankNotes_Success, createMessage_GroupTargets_Success, createMessage_MixedTargets_Success, createMessage_NullNotes_Success, createMessage_SendAsEmailFalse_NoRecipientLookup, createMessage_SendAsEmailNoRecipients_NoEmailSent, createMessage_SendAsEmailWithRecipients_SendsEmail, createMessage_SendAsEmailWithoutPublishDate_NoBatchId, createMessage_SendAsEmailWithPublishDate_GeneratesBatchId, createMessage_Success_NoEmail, createMessage_TitleBoundary_Success, createMessage_TrimmedFields_Success, createMessage_UserTargets_Success, createMessage_VerifyLogging_Success
- Required order: createMessage_BlankNotes_Success, createMessage_GroupTargets_Success, createMessage_MixedTargets_Success, createMessage_NullNotes_Success, createMessage_SendAsEmailFalse_NoRecipientLookup, createMessage_SendAsEmailNoRecipients_NoEmailSent, createMessage_SendAsEmailWithoutPublishDate_NoBatchId, createMessage_SendAsEmailWithPublishDate_GeneratesBatchId, createMessage_SendAsEmailWithRecipients_SendsEmail, createMessage_Success_NoEmail, createMessage_TitleBoundary_Success, createMessage_TrimmedFields_Success, createMessage_UserTargets_Success, createMessage_VerifyLogging_Success

VIOLATION 4: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section PERMISSION not alphabetical.
- Current order: createMessage_VerifyPreAuthorizeAnnotation_Success, createMessage_controller_permission_unauthorized, createMessage_WithValidRequest_DelegatesToService
- Required order: createMessage_controller_permission_unauthorized, createMessage_VerifyPreAuthorizeAnnotation_Success, createMessage_WithValidRequest_DelegatesToService

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Message/GetMessagesInBatchesTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Message
Class: public class GetMessagesInBatchesTest extends MessageServiceTestBase {
Extends: MessageServiceTestBase
Lines of Code: 218
Last Modified: 2026-02-10 23:11:31
Declared Test Count: 6 (first occurrence line 32)
Actual @Test Count: 7

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 32
- Current: 6
- Required: 7

VIOLATION 2: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 184 inline mock in `getMessagesInBatches_controller_permission_unauthorized`: `doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 185 inline mock in `getMessagesInBatches_controller_permission_unauthorized`: `.when(messageServiceMock).getMessagesInBatches(any());`
- Required: Move to base test stub method and call stub in test.

VIOLATION 3: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section PERMISSION not alphabetical.
- Current order: getMessagesInBatches_VerifyPreAuthorizeAnnotation_Success, getMessagesInBatches_controller_permission_unauthorized, getMessagesInBatches_WithValidRequest_DelegatesToService
- Required order: getMessagesInBatches_controller_permission_unauthorized, getMessagesInBatches_VerifyPreAuthorizeAnnotation_Success, getMessagesInBatches_WithValidRequest_DelegatesToService

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Message/SetMessageReadByUserIdAndMessageIdTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Message
Class: public class SetMessageReadByUserIdAndMessageIdTest extends MessageServiceTestBase {
Extends: MessageServiceTestBase
Lines of Code: 285
Last Modified: 2026-02-10 23:11:31
Declared Test Count: 13 (first occurrence line 28)
Actual @Test Count: 13

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `setMessageReadByUserIdAndMessageId_controller_permission_forbidden` or `setMessageReadByUserIdAndMessageId_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 2: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 66 method `setMessageReadByUserIdAndMessageId_Success`
- Required rename: `setMessageReadByUserIdAndMessageId_Success_Success`
- Line: 253 method `setMessageReadByUserIdAndMessageId_VerifyPreAuthorizeAnnotation`
- Required rename: `setMessageReadByUserIdAndMessageId_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 3: Rule 8 - Error Constants
- Severity: HIGH
- Line: 237 has hardcoded message: `assertEquals("DB Error", ex.getMessage());`
- Required: Replace with an ErrorMessages constant (add one if missing).

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 8 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Message/GetUnreadMessageCountTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Message
Class: class GetUnreadMessageCountTest extends MessageServiceTestBase {
Extends: MessageServiceTestBase
Lines of Code: 269
Last Modified: 2026-02-10 23:11:31
Declared Test Count: 11 (first occurrence line 23)
Actual @Test Count: 12

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 23
- Current: 11
- Required: 12

VIOLATION 2: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 260 inline mock in `getUnreadMessageCount_controller_permission_unauthorized`: `doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 261 inline mock in `getUnreadMessageCount_controller_permission_unauthorized`: `.when(messageServiceMock).getUnreadMessageCount();`
- Required: Move to base test stub method and call stub in test.

VIOLATION 3: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section PERMISSION not alphabetical.
- Current order: getUnreadMessageCount_VerifyPreAuthorizeAnnotation_Success, getUnreadMessageCount_WithValidRequest_DelegatesToService, getUnreadMessageCount_controller_permission_unauthorized
- Required order: getUnreadMessageCount_controller_permission_unauthorized, getUnreadMessageCount_VerifyPreAuthorizeAnnotation_Success, getUnreadMessageCount_WithValidRequest_DelegatesToService

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Message/ToggleMessageTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Message
Class: public class ToggleMessageTest extends MessageServiceTestBase {
Extends: MessageServiceTestBase
Lines of Code: 254
Last Modified: 2026-02-10 17:39:20
Declared Test Count: 11 (first occurrence line 27)
Actual @Test Count: 11

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `toggleMessage_controller_permission_forbidden` or `toggleMessage_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 2: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 222 method `toggleMessage_VerifyPreAuthorizeAnnotation`
- Required rename: `toggleMessage_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 3: Rule 8 - Error Constants
- Severity: HIGH
- Line: 173 has hardcoded message: `assertEquals("DB Error", ex.getMessage());`
- Required: Replace with an ErrorMessages constant (add one if missing).

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 8 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Message/GetMessagesByUserIdTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Message
Class: public class GetMessagesByUserIdTest extends MessageServiceTestBase {
Extends: MessageServiceTestBase
Lines of Code: 406
Last Modified: 2026-02-10 17:39:20
Declared Test Count: 15 (first occurrence line 35)
Actual @Test Count: 15

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getMessagesByUserId_controller_permission_forbidden` or `getMessagesByUserId_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 2: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 151 method `getMessagesByUserId_Success`
- Required rename: `getMessagesByUserId_Success_Success`
- Line: 364 method `getMessagesByUserId_VerifyPreAuthorizeAnnotation`
- Required rename: `getMessagesByUserId_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 3: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 255 in `getMessagesByUserId_NullPaginationRequest_ThrowsNullPointerException`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 4: Rule 8 - Error Constants
- Severity: HIGH
- Line: 275 has hardcoded message: `assertEquals("Page error", ex.getMessage());`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 332 has hardcoded message: `assertEquals("User DB Error", ex.getMessage());`
- Required: Replace with an ErrorMessages constant (add one if missing).

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 7 issues above.
- Fix Rule 8 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Message/SetMessageReadTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Message
Class: public class SetMessageReadTest extends MessageServiceTestBase {
Extends: MessageServiceTestBase
Lines of Code: 285
Last Modified: 2026-02-10 17:39:20
Declared Test Count: 13 (first occurrence line 28)
Actual @Test Count: 13

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `setMessageRead_controller_permission_forbidden` or `setMessageRead_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 2: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 44 method `setMessageReadByUserIdAndMessageId_AlreadyRead_NoDuplicate`
- Required rename: `setMessageRead_AlreadyRead_NoDuplicate`
- Line: 66 method `setMessageReadByUserIdAndMessageId_Success`
- Required rename: `setMessageReadByUserIdAndMessageId_Success_Success`
- Line: 66 method `setMessageReadByUserIdAndMessageId_Success`
- Required rename: `setMessageReadByUserIdAndMessageId_Success_Success`
- Line: 85 method `setMessageReadByUserIdAndMessageId_VerifyLogContent_Success`
- Required rename: `setMessageRead_VerifyLogContent_Success`
- Line: 107 method `setMessageReadByUserIdAndMessageId_VerifyMappingData_Success`
- Required rename: `setMessageRead_VerifyMappingData_Success`
- Line: 135 method `setMessageReadByUserIdAndMessageId_MessageIdNegative_ThrowsNotFoundException`
- Required rename: `setMessageRead_MessageIdNegative_ThrowsNotFoundException`
- Line: 151 method `setMessageReadByUserIdAndMessageId_MessageIdZero_ThrowsNotFoundException`
- Required rename: `setMessageRead_MessageIdZero_ThrowsNotFoundException`
- Line: 168 method `setMessageReadByUserIdAndMessageId_MessageNotFound_ThrowsNotFoundException`
- Required rename: `setMessageRead_MessageNotFound_ThrowsNotFoundException`
- Line: 184 method `setMessageReadByUserIdAndMessageId_UserIdNegative_ThrowsNotFoundException`
- Required rename: `setMessageRead_UserIdNegative_ThrowsNotFoundException`
- Line: 199 method `setMessageReadByUserIdAndMessageId_UserIdZero_ThrowsNotFoundException`
- Required rename: `setMessageRead_UserIdZero_ThrowsNotFoundException`
- Line: 215 method `setMessageReadByUserIdAndMessageId_UserNotFound_ThrowsNotFoundException`
- Required rename: `setMessageRead_UserNotFound_ThrowsNotFoundException`
- Line: 230 method `setMessageReadByUserIdAndMessageId_UserRepositoryException_Propagates`
- Required rename: `setMessageRead_UserRepositoryException_Propagates`
- Line: 253 method `setMessageReadByUserIdAndMessageId_VerifyPreAuthorizeAnnotation`
- Required rename: `setMessageReadByUserIdAndMessageId_VerifyPreAuthorizeAnnotation_Success`
- Line: 253 method `setMessageReadByUserIdAndMessageId_VerifyPreAuthorizeAnnotation`
- Required rename: `setMessageReadByUserIdAndMessageId_VerifyPreAuthorizeAnnotation_Success`
- Line: 273 method `setMessageReadByUserIdAndMessageId_WithValidRequest_DelegatesToService`
- Required rename: `setMessageRead_WithValidRequest_DelegatesToService`

VIOLATION 3: Rule 8 - Error Constants
- Severity: HIGH
- Line: 237 has hardcoded message: `assertEquals("DB Error", ex.getMessage());`
- Required: Replace with an ErrorMessages constant (add one if missing).

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 8 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Message/GetMessageDetailsByIdTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Message
Class: public class GetMessageDetailsByIdTest extends MessageServiceTestBase {
Extends: MessageServiceTestBase
Lines of Code: 249
Last Modified: 2026-02-10 17:39:20
Declared Test Count: 11 (first occurrence line 27)
Actual @Test Count: 11

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getMessageDetailsById_controller_permission_forbidden` or `getMessageDetailsById_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 2: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 65 method `getMessageDetailsById_Success`
- Required rename: `getMessageDetailsById_Success_Success`
- Line: 207 method `getMessageDetailsById_VerifyPreAuthorizeAnnotation`
- Required rename: `getMessageDetailsById_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 3: Rule 8 - Error Constants
- Severity: HIGH
- Line: 162 has hardcoded message: `assertEquals("Lookup failed", ex.getMessage());`
- Required: Replace with an ErrorMessages constant (add one if missing).

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 8 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Message/CreateMessageWithContextTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Message
Class: class CreateMessageWithContextTest extends MessageServiceTestBase {
Extends: MessageServiceTestBase
Lines of Code: 115
Last Modified: 2026-02-10 17:39:20
Declared Test Count: 5 (first occurrence line 19)
Actual @Test Count: 5

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `createMessageWithContext_controller_permission_forbidden` or `createMessageWithContext_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 2: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: PERMISSION
- Required: Add Success, Failure, Permission section headers.

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 10 issues above.


**IMPLEMENTATION PLAN (STEP-BY-STEP)**
1. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Message/CreateMessageTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
2. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Message/GetMessagesInBatchesTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
3. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Message/SetMessageReadByUserIdAndMessageIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
4. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Message/GetUnreadMessageCountTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
5. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Message/ToggleMessageTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
6. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Message/GetMessagesByUserIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
7. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Message/SetMessageReadTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
8. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Message/GetMessageDetailsByIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
9. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Message/CreateMessageWithContextTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
10. Resolve extra test file `SetMessageReadTest.java` by renaming it to match a public method or removing it.

Verification Commands (run after fixes):
- mvn -Dtest=CreateMessageTest test
- mvn -Dtest=GetMessagesInBatchesTest test
- mvn -Dtest=SetMessageReadByUserIdAndMessageIdTest test
- mvn -Dtest=GetUnreadMessageCountTest test
- mvn -Dtest=ToggleMessageTest test
- mvn -Dtest=GetMessagesByUserIdTest test