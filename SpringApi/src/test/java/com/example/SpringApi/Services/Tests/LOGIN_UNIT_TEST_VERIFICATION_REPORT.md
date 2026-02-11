# UNIT TEST VERIFICATION REPORT — Login

```
╔════════════════════════════════════════════════════════════╗
║           UNIT TEST VERIFICATION REPORT                    ║
║                                                            ║
║  Status: ❌ RED                                               ║
║  Services Analyzed: 1                                      ║
║  Public Methods Found: 4                                  ║
║  Test Files Expected: 4                                  ║
║  Test Files Found: 4                                     ║
║  Total Violations: 3                                     ║
╚════════════════════════════════════════════════════════════╝
```

VIOLATIONS BY RULE:

| Rule | Description | Count |
| --- | --- | --- |
| 10 | Test Ordering | 3 |


**FILE-BY-FILE BREAKDOWN**

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Login/ResetPasswordTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Login
Class: public class ResetPasswordTest extends LoginServiceTestBase {
Extends: LoginServiceTestBase
Lines of Code: 347
Last Modified: 2026-02-10 20:20:42
Declared Test Count: 15 (first occurrence line 25)
Actual @Test Count: 15

VIOLATIONS FOUND:

VIOLATION 1: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: resetPassword_EmailSendFailure_ThrowsRuntimeException, resetPassword_EmptyPasswordSet_ThrowsBadRequestException, resetPassword_MissingBrevoApiKey_ThrowsBadRequestException, resetPassword_MissingLoginName_ThrowsBadRequestException, resetPassword_MissingSendGridApiKey_ThrowsBadRequestException, resetPassword_MissingSenderEmail_ThrowsBadRequestException, resetPassword_MissingSenderName_ThrowsBadRequestException, resetPassword_NoClientConfiguration_ThrowsRuntimeException, resetPassword_NoPasswordSet_ThrowsBadRequestException, resetPassword_NullLoginName_ThrowsBadRequestException, resetPassword_NullRequest_ThrowsNullPointerException, resetPassword_UserNotFound_ThrowsNotFoundException, resetPassword_WhitespaceLoginName_ThrowsBadRequestException
- Required order: resetPassword_EmailSendFailure_ThrowsRuntimeException, resetPassword_EmptyPasswordSet_ThrowsBadRequestException, resetPassword_MissingBrevoApiKey_ThrowsBadRequestException, resetPassword_MissingLoginName_ThrowsBadRequestException, resetPassword_MissingSenderEmail_ThrowsBadRequestException, resetPassword_MissingSenderName_ThrowsBadRequestException, resetPassword_MissingSendGridApiKey_ThrowsBadRequestException, resetPassword_NoClientConfiguration_ThrowsRuntimeException, resetPassword_NoPasswordSet_ThrowsBadRequestException, resetPassword_NullLoginName_ThrowsBadRequestException, resetPassword_NullRequest_ThrowsNullPointerException, resetPassword_UserNotFound_ThrowsNotFoundException, resetPassword_WhitespaceLoginName_ThrowsBadRequestException

REQUIRED FIXES SUMMARY:
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Login/ConfirmEmailTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Login
Class: public class ConfirmEmailTest extends LoginServiceTestBase {
Extends: LoginServiceTestBase
Lines of Code: 206
Last Modified: 2026-02-10 17:39:20
Declared Test Count: 8 (first occurrence line 25)
Actual @Test Count: 8

VIOLATIONS FOUND:

VIOLATION 1: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: confirmEmail_Success_Success, confirmEmail_Success_ClearsToken
- Required order: confirmEmail_Success_ClearsToken, confirmEmail_Success_Success

REQUIRED FIXES SUMMARY:
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Login/SignInTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Login
Class: public class SignInTest extends LoginServiceTestBase {
Extends: LoginServiceTestBase
Lines of Code: 527
Last Modified: 2026-02-10 17:39:20
Declared Test Count: 22 (first occurrence line 31)
Actual @Test Count: 22

VIOLATIONS FOUND:

VIOLATION 1: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: signIn_ClientNotFoundInMapping_SkipsClient, signIn_ClientsSortedByName_Success, signIn_MultipleAccessibleClients_Success, signIn_NoClientMappings_ReturnsEmptyList, signIn_Success_Success, signIn_Success_ResetsLoginAttemptsAndSavesUser, signIn_Success_SetsLastLoginAt
- Required order: signIn_ClientNotFoundInMapping_SkipsClient, signIn_ClientsSortedByName_Success, signIn_MultipleAccessibleClients_Success, signIn_NoClientMappings_ReturnsEmptyList, signIn_Success_ResetsLoginAttemptsAndSavesUser, signIn_Success_SetsLastLoginAt, signIn_Success_Success

REQUIRED FIXES SUMMARY:
- Fix Rule 10 issues above.


**IMPLEMENTATION PLAN (STEP-BY-STEP)**
1. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Login/ResetPasswordTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
2. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Login/ConfirmEmailTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
3. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Login/SignInTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.

Verification Commands (run after fixes):
- mvn -Dtest=ResetPasswordTest test
- mvn -Dtest=ConfirmEmailTest test
- mvn -Dtest=SignInTest test