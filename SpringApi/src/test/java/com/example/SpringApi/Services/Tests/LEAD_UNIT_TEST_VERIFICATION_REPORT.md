# UNIT TEST VERIFICATION REPORT — Lead

```
╔════════════════════════════════════════════════════════════╗
║           UNIT TEST VERIFICATION REPORT                    ║
║                                                            ║
║  Status: ❌ RED                                               ║
║  Services Analyzed: 1                                      ║
║  Public Methods Found: 8                                  ║
║  Test Files Expected: 8                                  ║
║  Test Files Found: 8                                     ║
║  Total Violations: 29                                    ║
╚════════════════════════════════════════════════════════════╝
```

VIOLATIONS BY RULE:

| Rule | Description | Count |
| --- | --- | --- |
| 2 | Test Count Declaration | 6 |
| 3 | Controller Permission Test | 6 |
| 5 | Test Naming Convention | 5 |
| 10 | Test Ordering | 4 |
| 11 | Complete Coverage | 2 |
| 12 | Arrange/Act/Assert | 6 |


**FILE-BY-FILE BREAKDOWN**

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Lead/GetLeadDetailsByIdTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Lead
Class: class GetLeadDetailsByIdTest extends LeadServiceTestBase {
Extends: LeadServiceTestBase
Lines of Code: 230
Last Modified: 2026-02-10 17:39:20
Declared Test Count: 10 (first occurrence line 23)
Actual @Test Count: 9

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 23
- Current: 10
- Required: 9

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Lead/BulkCreateLeadsTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Lead
Class: * Test class for LeadService.bulkCreateLeads() method.
Extends: None
Lines of Code: 442
Last Modified: 2026-02-10 17:39:20
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
- Required: Add `bulkCreateLeads_controller_permission_forbidden` or `bulkCreateLeads_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 151 method `bulkCreateLeads_PartialSuccess`
- Required rename: `bulkCreateLeads_PartialSuccess_Success`
- Line: 208 method `bulkCreateLeads_VerifyLoggingCalled`
- Required rename: `bulkCreateLeads_VerifyLoggingCalled_Success`
- Line: 229 method `bulkCreateLeads_VerifyMessageNotification`
- Required rename: `bulkCreateLeads_VerifyMessageNotification_Success`
- Line: 397 method `bulkCreateLeads_VerifyPreAuthorizeAnnotation`
- Required rename: `bulkCreateLeads_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 4: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 279 in `bulkCreateLeads_EmptyList_ThrowsBadRequestException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 336 in `bulkCreateLeads_NullList_ThrowsBadRequestException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 397 in `bulkCreateLeads_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 12 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Lead/GetLeadsInBatchesTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Lead
Class: * Test class for LeadService.getLeadsInBatches() method.
Extends: None
Lines of Code: 144
Last Modified: 2026-02-10 20:20:42
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 3

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 3` immediately after the class opening brace.

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getLeadsInBatches_controller_permission_forbidden` or `getLeadsInBatches_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 47 method `getLeadsInBatches_SingleComprehensiveTest`
- Required rename: `getLeadsInBatches_SingleComprehensiveTest_Success`
- Line: 97 method `getLeadsInBatches_VerifyPreAuthorizeAnnotation`
- Required rename: `getLeadsInBatches_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 4: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 47 in `getLeadsInBatches_SingleComprehensiveTest` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 97 in `getLeadsInBatches_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: FAILURE
- Required: Add Success, Failure, Permission section headers.

VIOLATION 6: Rule 11 - Complete Coverage
- Severity: HIGH
- Coverage by test names is incomplete.
- Missing: at least one *_success test.
- Missing: at least one failure/exception test (e.g., *_throws*, *_exception*, *_invalid*).

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 12 issues above.
- Fix Rule 10 issues above.
- Fix Rule 11 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Lead/CreateLeadTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Lead
Class: * Test class for LeadService.createLead() method.
Extends: None
Lines of Code: 517
Last Modified: 2026-02-10 20:20:42
Declared Test Count: 32 (first occurrence line 24)
Actual @Test Count: 32

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `createLead_controller_permission_forbidden` or `createLead_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 2: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 479 method `createLead_VerifyPreAuthorizeAnnotation`
- Required rename: `createLead_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 3: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 166 in `createLead_EmptyEmail_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 179 in `createLead_EmptyFirstName_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 192 in `createLead_EmptyLastName_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 205 in `createLead_EmptyPhone_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 218 in `createLead_EmptyStatus_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 231 in `createLead_InvalidEmailFormat_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 244 in `createLead_InvalidPhoneFormat_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 257 in `createLead_InvalidStatusUnknown_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 270 in `createLead_MissingAddress_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 284 in `createLead_NegativeCompanySize_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 297 in `createLead_NullEmail_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 310 in `createLead_NullFirstName_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 323 in `createLead_NullLastName_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 336 in `createLead_NullPhone_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 349 in `createLead_NullRequest_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 361 in `createLead_NullStatus_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 374 in `createLead_WhitespaceEmail_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 387 in `createLead_WhitespaceFirstName_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 400 in `createLead_WhitespaceLastName_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 413 in `createLead_WhitespacePhone_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 426 in `createLead_WhitespaceStatus_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 439 in `createLead_ZeroCompanySize_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 4: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: createLead_EmptyEmail_ThrowsBadRequestException, createLead_EmptyFirstName_ThrowsBadRequestException, createLead_EmptyLastName_ThrowsBadRequestException, createLead_EmptyPhone_ThrowsBadRequestException, createLead_EmptyStatus_ThrowsBadRequestException, createLead_InvalidEmailFormat_ThrowsBadRequestException, createLead_InvalidPhoneFormat_ThrowsBadRequestException, createLead_InvalidStatusUnknown_ThrowsBadRequestException, createLead_MissingAddress_ThrowsBadRequestException, createLead_NegativeCompanySize_ThrowsBadRequestException, createLead_NullEmail_ThrowsBadRequestException, createLead_NullFirstName_ThrowsBadRequestException, createLead_NullLastName_ThrowsBadRequestException, createLead_NullPhone_ThrowsBadRequestException, createLead_NullRequest_ThrowsBadRequestException, createLead_NullStatus_ThrowsBadRequestException, createLead_WhitespaceEmail_ThrowsBadRequestException, createLead_WhitespaceFirstName_ThrowsBadRequestException, createLead_WhitespaceLastName_ThrowsBadRequestException, createLead_WhitespacePhone_ThrowsBadRequestException, createLead_WhitespaceStatus_ThrowsBadRequestException, createLead_ZeroCompanySize_ThrowsBadRequestException, createLead_unit_validation_missingEmail
- Required order: createLead_EmptyEmail_ThrowsBadRequestException, createLead_EmptyFirstName_ThrowsBadRequestException, createLead_EmptyLastName_ThrowsBadRequestException, createLead_EmptyPhone_ThrowsBadRequestException, createLead_EmptyStatus_ThrowsBadRequestException, createLead_InvalidEmailFormat_ThrowsBadRequestException, createLead_InvalidPhoneFormat_ThrowsBadRequestException, createLead_InvalidStatusUnknown_ThrowsBadRequestException, createLead_MissingAddress_ThrowsBadRequestException, createLead_NegativeCompanySize_ThrowsBadRequestException, createLead_NullEmail_ThrowsBadRequestException, createLead_NullFirstName_ThrowsBadRequestException, createLead_NullLastName_ThrowsBadRequestException, createLead_NullPhone_ThrowsBadRequestException, createLead_NullRequest_ThrowsBadRequestException, createLead_NullStatus_ThrowsBadRequestException, createLead_unit_validation_missingEmail, createLead_WhitespaceEmail_ThrowsBadRequestException, createLead_WhitespaceFirstName_ThrowsBadRequestException, createLead_WhitespaceLastName_ThrowsBadRequestException, createLead_WhitespacePhone_ThrowsBadRequestException, createLead_WhitespaceStatus_ThrowsBadRequestException, createLead_ZeroCompanySize_ThrowsBadRequestException

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 12 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Lead/UpdateLeadTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Lead
Class: * Test class for LeadService.updateLead() method.
Extends: None
Lines of Code: 569
Last Modified: 2026-02-10 17:39:20
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 32

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 32` immediately after the class opening brace.

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `updateLead_controller_permission_forbidden` or `updateLead_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 79 method `updateLead_Success`
- Required rename: `updateLead_Success_Success`
- Line: 524 method `updateLead_VerifyPreAuthorizeAnnotation`
- Required rename: `updateLead_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 4: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 121 in `updateLead_Deleted_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 136 in `updateLead_EmptyEmail_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 151 in `updateLead_EmptyFirstName_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 166 in `updateLead_EmptyLastName_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 181 in `updateLead_EmptyPhone_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 196 in `updateLead_EmptyStatus_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 211 in `updateLead_InvalidEmailFormat_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 226 in `updateLead_InvalidPhoneFormat_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 241 in `updateLead_InvalidStatus_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 256 in `updateLead_NegativeId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 270 in `updateLead_NotFound_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 284 in `updateLead_NullEmail_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 299 in `updateLead_NullFirstName_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 314 in `updateLead_NullLastName_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 329 in `updateLead_NullPhone_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 344 in `updateLead_NullRequest_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 357 in `updateLead_NullStatus_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 372 in `updateLead_ValidCompanySizeZero_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 387 in `updateLead_WhitespaceEmail_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 402 in `updateLead_WhitespaceFirstName_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 417 in `updateLead_WhitespaceLastName_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 432 in `updateLead_WhitespacePhone_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 447 in `updateLead_WhitespaceStatus_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 462 in `updateLead_ZeroId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 524 in `updateLead_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: updateLead_Deleted_ThrowsNotFoundException, updateLead_EmptyEmail_ThrowsBadRequestException, updateLead_EmptyFirstName_ThrowsBadRequestException, updateLead_EmptyLastName_ThrowsBadRequestException, updateLead_EmptyPhone_ThrowsBadRequestException, updateLead_EmptyStatus_ThrowsBadRequestException, updateLead_InvalidEmailFormat_ThrowsBadRequestException, updateLead_InvalidPhoneFormat_ThrowsBadRequestException, updateLead_InvalidStatus_ThrowsBadRequestException, updateLead_NegativeId_ThrowsNotFoundException, updateLead_NotFound_ThrowsNotFoundException, updateLead_NullEmail_ThrowsBadRequestException, updateLead_NullFirstName_ThrowsBadRequestException, updateLead_NullLastName_ThrowsBadRequestException, updateLead_NullPhone_ThrowsBadRequestException, updateLead_NullRequest_ThrowsBadRequestException, updateLead_NullStatus_ThrowsBadRequestException, updateLead_ValidCompanySizeZero_ThrowsBadRequestException, updateLead_WhitespaceEmail_ThrowsBadRequestException, updateLead_WhitespaceFirstName_ThrowsBadRequestException, updateLead_WhitespaceLastName_ThrowsBadRequestException, updateLead_WhitespacePhone_ThrowsBadRequestException, updateLead_WhitespaceStatus_ThrowsBadRequestException, updateLead_ZeroId_ThrowsNotFoundException, updateLead_unit_partialUpdate_success, updateLead_unit_notFound_failure
- Required order: updateLead_Deleted_ThrowsNotFoundException, updateLead_EmptyEmail_ThrowsBadRequestException, updateLead_EmptyFirstName_ThrowsBadRequestException, updateLead_EmptyLastName_ThrowsBadRequestException, updateLead_EmptyPhone_ThrowsBadRequestException, updateLead_EmptyStatus_ThrowsBadRequestException, updateLead_InvalidEmailFormat_ThrowsBadRequestException, updateLead_InvalidPhoneFormat_ThrowsBadRequestException, updateLead_InvalidStatus_ThrowsBadRequestException, updateLead_NegativeId_ThrowsNotFoundException, updateLead_NotFound_ThrowsNotFoundException, updateLead_NullEmail_ThrowsBadRequestException, updateLead_NullFirstName_ThrowsBadRequestException, updateLead_NullLastName_ThrowsBadRequestException, updateLead_NullPhone_ThrowsBadRequestException, updateLead_NullRequest_ThrowsBadRequestException, updateLead_NullStatus_ThrowsBadRequestException, updateLead_unit_notFound_failure, updateLead_unit_partialUpdate_success, updateLead_ValidCompanySizeZero_ThrowsBadRequestException, updateLead_WhitespaceEmail_ThrowsBadRequestException, updateLead_WhitespaceFirstName_ThrowsBadRequestException, updateLead_WhitespaceLastName_ThrowsBadRequestException, updateLead_WhitespacePhone_ThrowsBadRequestException, updateLead_WhitespaceStatus_ThrowsBadRequestException, updateLead_ZeroId_ThrowsNotFoundException

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 12 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Lead/ToggleLeadTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Lead
Class: * Test class for LeadService.toggleLead() method.
Extends: None
Lines of Code: 254
Last Modified: 2026-02-10 20:20:42
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
- Line: 178 (toggleLead_controller_permission_forbidden)
- Problem: no http status assertion detected
- Required: Call controller method and assert HTTP status.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 62 method `toggleLead_Success`
- Required rename: `toggleLead_Success_Success`
- Line: 209 method `toggleLead_VerifyPreAuthorizeAnnotation`
- Required rename: `toggleLead_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 4: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 37 in `toggleLead_MultipleToggles_Success` missing AAA comments: Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 90 in `toggleLead_MaxLongId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 104 in `toggleLead_MinLongId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 117 in `toggleLead_NegativeId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 130 in `toggleLead_NotFound_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 143 in `toggleLead_ZeroId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 178 in `toggleLead_controller_permission_forbidden` missing AAA comments: Arrange, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 209 in `toggleLead_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: toggleLead_MaxLongId_ThrowsNotFoundException, toggleLead_MinLongId_ThrowsNotFoundException, toggleLead_NegativeId_ThrowsNotFoundException, toggleLead_NotFound_ThrowsNotFoundException, toggleLead_ZeroId_ThrowsNotFoundException, toggleLead_unit_alreadyActive_noOp, toggleLead_controller_permission_forbidden
- Required order: toggleLead_controller_permission_forbidden, toggleLead_MaxLongId_ThrowsNotFoundException, toggleLead_MinLongId_ThrowsNotFoundException, toggleLead_NegativeId_ThrowsNotFoundException, toggleLead_NotFound_ThrowsNotFoundException, toggleLead_unit_alreadyActive_noOp, toggleLead_ZeroId_ThrowsNotFoundException

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 12 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Lead/BulkCreateLeadsAsyncTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Lead
Class: class BulkCreateLeadsAsyncTest extends LeadServiceTestBase {
Extends: LeadServiceTestBase
Lines of Code: 107
Last Modified: 2026-02-10 20:20:42
Declared Test Count: MISSING/MISPLACED (first occurrence line 1)
Actual @Test Count: 3

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 1
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 3` immediately after the class opening brace.

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 93 (bulkCreateLeadsAsync_controller_permission_configured)
- Problem: no http status assertion detected
- Required: Call controller method and assert HTTP status.

VIOLATION 3: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 66 in `bulkCreateLeadsAsync_unit_nullList_failure` missing AAA comments: Arrange, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 4: Rule 11 - Complete Coverage
- Severity: HIGH
- Coverage by test names is incomplete.
- Missing: at least one failure/exception test (e.g., *_throws*, *_exception*, *_invalid*).

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 12 issues above.
- Fix Rule 11 issues above.


**IMPLEMENTATION PLAN (STEP-BY-STEP)**
1. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Lead/GetLeadDetailsByIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
2. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Lead/BulkCreateLeadsTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
3. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Lead/GetLeadsInBatchesTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
4. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Lead/CreateLeadTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
5. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Lead/UpdateLeadTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
6. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Lead/ToggleLeadTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
7. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Lead/BulkCreateLeadsAsyncTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.

Verification Commands (run after fixes):
- mvn -Dtest=GetLeadDetailsByIdTest test
- mvn -Dtest=BulkCreateLeadsTest test
- mvn -Dtest=GetLeadsInBatchesTest test
- mvn -Dtest=CreateLeadTest test
- mvn -Dtest=UpdateLeadTest test
- mvn -Dtest=ToggleLeadTest test