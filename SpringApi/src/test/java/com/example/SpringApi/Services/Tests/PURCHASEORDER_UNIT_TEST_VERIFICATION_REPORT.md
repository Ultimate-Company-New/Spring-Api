# UNIT TEST VERIFICATION REPORT — PurchaseOrder

```
╔════════════════════════════════════════════════════════════╗
║           UNIT TEST VERIFICATION REPORT                    ║
║                                                            ║
║  Status: ❌ RED                                               ║
║  Services Analyzed: 1                                      ║
║  Public Methods Found: 9                                  ║
║  Test Files Expected: 9                                  ║
║  Test Files Found: 14                                    ║
║  Total Violations: 96                                    ║
╚════════════════════════════════════════════════════════════╝
```

VIOLATIONS BY RULE:

| Rule | Description | Count |
| --- | --- | --- |
| 1 | One Test File per Method | 5 |
| 2 | Test Count Declaration | 6 |
| 4 | Test Annotations | 5 |
| 5 | Test Naming Convention | 14 |
| 6 | Centralized Mocking | 6 |
| 8 | Error Constants | 1 |
| 9 | Test Documentation | 14 |
| 10 | Test Ordering | 23 |
| 11 | Complete Coverage | 1 |
| 12 | Arrange/Act/Assert | 14 |
| 14 | No Inline Mocks | 7 |


**MISSING/EXTRA TEST FILES (RULE 1)**
Extra test file with no matching public method: `ApprovedByPOTest.java`. Either rename it to match a public method or remove it.
Extra test file with no matching public method: `GetPOsInBatchesTest.java`. Either rename it to match a public method or remove it.
Extra test file with no matching public method: `TogglePOTest.java`. Either rename it to match a public method or remove it.
Extra test file with no matching public method: `GetPODetailsByIdTest.java`. Either rename it to match a public method or remove it.
Extra test file with no matching public method: `RejectedByPOTest.java`. Either rename it to match a public method or remove it.


**FILE-BY-FILE BREAKDOWN**

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/ApprovedByPurchaseOrderTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PurchaseOrder
Class: * Test class for PurchaseOrderService.approvedByPurchaseOrder method.
Extends: None
Lines of Code: 149
Last Modified: 2026-02-10 23:56:34
Declared Test Count: 6 (first occurrence line 27)
Actual @Test Count: 6

VIOLATIONS FOUND:

VIOLATION 1: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 138 method `approvedByPO_WithValidId_DelegatesToService`
- Required rename: `approvedByPurchaseOrder_WithValidId_DelegatesToService`

VIOLATION 2: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: approvedByPurchaseOrder_VerifyPreAuthorizeAnnotation_Success (line 116)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 3: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 42 in `approvedByPurchaseOrder_Success_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 65 in `approvedByPurchaseOrder_AlreadyApproved_Failure` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 82 in `approvedByPurchaseOrder_NotFound_Failure` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 104 in `approvedByPurchaseOrder_controller_permission_unauthorized` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 118 in `approvedByPurchaseOrder_VerifyPreAuthorizeAnnotation_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 138 in `approvedByPO_WithValidId_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 4: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['FAILURE', 'PERMISSION', 'SUCCESS']
- Required: Success → Failure → Permission.

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section PERMISSION not alphabetical.
- Current order: approvedByPurchaseOrder_controller_permission_unauthorized, approvedByPurchaseOrder_VerifyPreAuthorizeAnnotation_Success, approvedByPO_WithValidId_DelegatesToService
- Required order: approvedByPO_WithValidId_DelegatesToService, approvedByPurchaseOrder_controller_permission_unauthorized, approvedByPurchaseOrder_VerifyPreAuthorizeAnnotation_Success

REQUIRED FIXES SUMMARY:
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/ApprovedByPOTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PurchaseOrder
Class: * Test class for PurchaseOrderService.approvedByPurchaseOrder method.
Extends: None
Lines of Code: 154
Last Modified: 2026-02-10 23:56:34
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 6

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 6` immediately after the class opening brace.

VIOLATION 2: Rule 4 - Test Annotations
- Severity: HIGH
- Line: 91 has disallowed annotation @TestFactory.
- Required: Remove or replace with allowed annotations only.

VIOLATION 3: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 120 has mock usage `mock(com.example.SpringApi.Services.PurchaseOrderService.class);`
- Required: Move mocks to base test file.
- Line: 145 has mock usage `com.example.SpringApi.Services.PurchaseOrderService mockService = mock(com.example.SpringApi.Services.PurchaseOrderService.class);`
- Required: Move mocks to base test file.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 40 method `approvedByPurchaseOrder_Success`
- Required rename: `approvedByPurchaseOrder_Success_Success`
- Line: 40 method `approvedByPurchaseOrder_Success`
- Required rename: `approvedByPurchaseOrder_Success_Success`
- Line: 61 method `approvedByPurchaseOrder_AlreadyApproved`
- Required rename: `approvedByPurchaseOrder_AlreadyApproved_Success`
- Line: 61 method `approvedByPurchaseOrder_AlreadyApproved`
- Required rename: `approvedByPurchaseOrder_AlreadyApproved_Success`
- Line: 75 method `approvedByPurchaseOrder_NotFound`
- Required rename: `approvedByPurchaseOrder_NotFound_Failure`
- Line: 75 method `approvedByPurchaseOrder_NotFound`
- Required rename: `approvedByPurchaseOrder_NotFound_Failure`
- Line: 117 method `approvedByPurchaseOrder_controller_permission_unauthorized`
- Required rename: `approvedByPO_controller_permission_unauthorized`
- Line: 134 method `approvedByPurchaseOrder_VerifyPreAuthorizeAnnotation`
- Required rename: `approvedByPurchaseOrder_VerifyPreAuthorizeAnnotation_Success`
- Line: 134 method `approvedByPurchaseOrder_VerifyPreAuthorizeAnnotation`
- Required rename: `approvedByPurchaseOrder_VerifyPreAuthorizeAnnotation_Success`
- Line: 144 method `approvedByPurchaseOrder_WithValidId_DelegatesToService`
- Required rename: `approvedByPO_WithValidId_DelegatesToService`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: approvedByPurchaseOrder_Success (line 38), approvedByPurchaseOrder_AlreadyApproved (line 59), approvedByPurchaseOrder_NotFound (line 73), approvedByPurchaseOrder_VerifyPreAuthorizeAnnotation (line 132), approvedByPurchaseOrder_WithValidId_DelegatesToService (line 142)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 134 in `approvedByPurchaseOrder_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 144 in `approvedByPurchaseOrder_WithValidId_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 43 inline mock in `approvedByPurchaseOrder_Success`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 45 inline mock in `approvedByPurchaseOrder_Success`: `when(purchaseOrderRepository.save(any())).thenReturn(testPurchaseOrder);`
- Required: Move to base test stub method and call stub in test.
- Line: 64 inline mock in `approvedByPurchaseOrder_AlreadyApproved`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 77 inline mock in `approvedByPurchaseOrder_NotFound`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 122 inline mock in `approvedByPurchaseOrder_controller_permission_unauthorized`: `doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 123 inline mock in `approvedByPurchaseOrder_controller_permission_unauthorized`: `.when(mockService).approvedByPurchaseOrder(TEST_PO_ID);`
- Required: Move to base test stub method and call stub in test.
- Line: 147 inline mock in `approvedByPurchaseOrder_WithValidId_DelegatesToService`: `doNothing().when(mockService).approvedByPurchaseOrder(TEST_PO_ID);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['FAILURE', 'SUCCESS', 'PERMISSION']
- Required: Success → Failure → Permission.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 4 issues above.
- Fix Rule 6 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/CreatePurchaseOrderTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PurchaseOrder
Class: * Test class for PurchaseOrderService.createPurchaseOrder method.
Extends: None
Lines of Code: 222
Last Modified: 2026-02-11 00:44:55
Declared Test Count: 8 (first occurrence line 39)
Actual @Test Count: 8

VIOLATIONS FOUND:

VIOLATION 1: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 179 method `createPurchaseOrder_VerifyPreAuthorizeAnnotation`
- Required rename: `createPurchaseOrder_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 2: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: createPurchaseOrder_VerifyPreAuthorizeAnnotation (line 177)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 3: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 54 in `createPurchaseOrder_Success_WithoutAttachments_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 70 in `createPurchaseOrder_Success_WithAttachments_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 100 in `createPurchaseOrder_PersistsCanonicalCustomPrice_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 130 in `createPurchaseOrder_NullRequest_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 143 in `createPurchaseOrder_NullOrderSummary_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 165 in `createPurchaseOrder_controller_permission_unauthorized` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 179 in `createPurchaseOrder_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 200 in `createPurchaseOrder_WithValidRequest_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 4: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: createPurchaseOrder_Success_WithoutAttachments_Success, createPurchaseOrder_Success_WithAttachments_Success, createPurchaseOrder_PersistsCanonicalCustomPrice_Success
- Required order: createPurchaseOrder_PersistsCanonicalCustomPrice_Success, createPurchaseOrder_Success_WithAttachments_Success, createPurchaseOrder_Success_WithoutAttachments_Success

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: createPurchaseOrder_NullRequest_ThrowsBadRequestException, createPurchaseOrder_NullOrderSummary_ThrowsBadRequestException
- Required order: createPurchaseOrder_NullOrderSummary_ThrowsBadRequestException, createPurchaseOrder_NullRequest_ThrowsBadRequestException

REQUIRED FIXES SUMMARY:
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/GetPurchaseOrdersInBatchesTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PurchaseOrder
Class: * Test class for PurchaseOrderService.getPurchaseOrdersInBatches method.
Extends: None
Lines of Code: 221
Last Modified: 2026-02-10 23:56:34
Declared Test Count: 7 (first occurrence line 36)
Actual @Test Count: 7

VIOLATIONS FOUND:

VIOLATION 1: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 209 method `getPOsInBatches_WithValidRequest_DelegatesToService`
- Required rename: `getPurchaseOrdersInBatches_WithValidRequest_DelegatesToService`

VIOLATION 2: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getPurchaseOrdersInBatches_VerifyPreAuthorizeAnnotation_Success (line 186)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 3: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 51 in `getPurchaseOrdersInBatches_ValidFilters_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 93 in `getPurchaseOrdersInBatches_InvalidPagination_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 114 in `getPurchaseOrdersInBatches_InvalidColumn_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 141 in `getPurchaseOrdersInBatches_InvalidOperator_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 174 in `getPurchaseOrdersInBatches_controller_permission_unauthorized` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 188 in `getPurchaseOrdersInBatches_VerifyPreAuthorizeAnnotation_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 209 in `getPOsInBatches_WithValidRequest_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 4: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['FAILURE', 'PERMISSION', 'SUCCESS']
- Required: Success → Failure → Permission.

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: getPurchaseOrdersInBatches_InvalidPagination_ThrowsBadRequestException, getPurchaseOrdersInBatches_InvalidColumn_ThrowsBadRequestException, getPurchaseOrdersInBatches_InvalidOperator_ThrowsBadRequestException
- Required order: getPurchaseOrdersInBatches_InvalidColumn_ThrowsBadRequestException, getPurchaseOrdersInBatches_InvalidOperator_ThrowsBadRequestException, getPurchaseOrdersInBatches_InvalidPagination_ThrowsBadRequestException

VIOLATION 6: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section PERMISSION not alphabetical.
- Current order: getPurchaseOrdersInBatches_controller_permission_unauthorized, getPurchaseOrdersInBatches_VerifyPreAuthorizeAnnotation_Success, getPOsInBatches_WithValidRequest_DelegatesToService
- Required order: getPOsInBatches_WithValidRequest_DelegatesToService, getPurchaseOrdersInBatches_controller_permission_unauthorized, getPurchaseOrdersInBatches_VerifyPreAuthorizeAnnotation_Success

REQUIRED FIXES SUMMARY:
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/GetPOsInBatchesTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PurchaseOrder
Class: * Test class for PurchaseOrderService.getPurchaseOrdersInBatches method.
Extends: None
Lines of Code: 162
Last Modified: 2026-02-10 23:56:34
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 4

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 4` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 126 has mock usage `mock(com.example.SpringApi.Services.PurchaseOrderService.class);`
- Required: Move mocks to base test file.
- Line: 151 has mock usage `com.example.SpringApi.Services.PurchaseOrderService mockService = mock(com.example.SpringApi.Services.PurchaseOrderService.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 43 method `getPurchaseOrdersInBatches_Comprehensive`
- Required rename: `getPurchaseOrdersInBatches_Comprehensive_Success`
- Line: 43 method `getPurchaseOrdersInBatches_Comprehensive`
- Required rename: `getPurchaseOrdersInBatches_Comprehensive_Success`
- Line: 123 method `getPurchaseOrdersInBatches_controller_permission_unauthorized`
- Required rename: `getPOsInBatches_controller_permission_unauthorized`
- Line: 140 method `getPurchaseOrdersInBatches_VerifyPreAuthorizeAnnotation`
- Required rename: `getPurchaseOrdersInBatches_VerifyPreAuthorizeAnnotation_Success`
- Line: 140 method `getPurchaseOrdersInBatches_VerifyPreAuthorizeAnnotation`
- Required rename: `getPurchaseOrdersInBatches_VerifyPreAuthorizeAnnotation_Success`
- Line: 150 method `getPurchaseOrdersInBatches_WithValidRequest_DelegatesToService`
- Required rename: `getPOsInBatches_WithValidRequest_DelegatesToService`

VIOLATION 4: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getPurchaseOrdersInBatches_Comprehensive (line 41), getPurchaseOrdersInBatches_VerifyPreAuthorizeAnnotation (line 138), getPurchaseOrdersInBatches_WithValidRequest_DelegatesToService (line 148)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 5: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 43 in `getPurchaseOrdersInBatches_Comprehensive` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 140 in `getPurchaseOrdersInBatches_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 150 in `getPurchaseOrdersInBatches_WithValidRequest_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 6: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 77 inline mock in `getPurchaseOrdersInBatches_Comprehensive`: `lenient().when(purchaseOrderFilterQueryBuilder.getColumnType("vendorNumber")).thenReturn("string");`
- Required: Move to base test stub method and call stub in test.
- Line: 98 inline mock in `getPurchaseOrdersInBatches_Comprehensive`: `lenient().when(purchaseOrderFilterQueryBuilder.getColumnType("vendorNumber")).thenReturn("string");`
- Required: Move to base test stub method and call stub in test.
- Line: 99 inline mock in `getPurchaseOrdersInBatches_Comprehensive`: `when(purchaseOrderFilterQueryBuilder.findPaginatedWithDetails(`
- Required: Move to base test stub method and call stub in test.
- Line: 128 inline mock in `getPurchaseOrdersInBatches_controller_permission_unauthorized`: `when(mockService.getPurchaseOrdersInBatches(any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 155 inline mock in `getPurchaseOrdersInBatches_WithValidRequest_DelegatesToService`: `when(mockService.getPurchaseOrdersInBatches(request)).thenReturn(mockResponse);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 7: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: FAILURE, SUCCESS
- Required: Add Success, Failure, Permission section headers.

VIOLATION 8: Rule 11 - Complete Coverage
- Severity: HIGH
- Coverage by test names is incomplete.
- Missing: at least one *_success test.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.
- Fix Rule 11 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/TogglePurchaseOrderTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PurchaseOrder
Class: * Test class for PurchaseOrderService.togglePurchaseOrder method.
Extends: None
Lines of Code: 155
Last Modified: 2026-02-10 23:56:34
Declared Test Count: 6 (first occurrence line 27)
Actual @Test Count: 6

VIOLATIONS FOUND:

VIOLATION 1: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 42 method `togglePO_Success_MarkAsDeleted`
- Required rename: `togglePurchaseOrder_Success_MarkAsDeleted`
- Line: 62 method `togglePO_Success_Restore`
- Required rename: `togglePurchaseOrder_Success_Restore`
- Line: 144 method `togglePO_WithValidId_DelegatesToService`
- Required rename: `togglePurchaseOrder_WithValidId_DelegatesToService`

VIOLATION 2: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: togglePurchaseOrder_VerifyPreAuthorizeAnnotation_Success (line 122)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 3: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 42 in `togglePO_Success_MarkAsDeleted` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 62 in `togglePO_Success_Restore` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 88 in `togglePurchaseOrder_NotFound_Failure` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 110 in `togglePurchaseOrder_controller_permission_unauthorized` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 124 in `togglePurchaseOrder_VerifyPreAuthorizeAnnotation_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 144 in `togglePO_WithValidId_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 4: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['SUCCESS', 'PERMISSION', 'FAILURE']
- Required: Success → Failure → Permission.

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section PERMISSION not alphabetical.
- Current order: togglePurchaseOrder_controller_permission_unauthorized, togglePurchaseOrder_VerifyPreAuthorizeAnnotation_Success, togglePO_WithValidId_DelegatesToService
- Required order: togglePO_WithValidId_DelegatesToService, togglePurchaseOrder_controller_permission_unauthorized, togglePurchaseOrder_VerifyPreAuthorizeAnnotation_Success

REQUIRED FIXES SUMMARY:
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/UpdatePurchaseOrderTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PurchaseOrder
Class: * Test class for PurchaseOrderService.updatePurchaseOrder method.
Extends: None
Lines of Code: 260
Last Modified: 2026-02-11 00:41:26
Declared Test Count: 7 (first occurrence line 61)
Actual @Test Count: 6

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 61
- Current: 7
- Required: 6

VIOLATION 2: Rule 4 - Test Annotations
- Severity: HIGH
- Line: 184 has disallowed annotation @TestFactory.
- Required: Remove or replace with allowed annotations only.

VIOLATION 3: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 46 has mock usage `import static org.mockito.Mockito.mock;`
- Required: Move mocks to base test file.
- Line: 47 has mock usage `import static org.mockito.Mockito.mockConstruction;`
- Required: Move mocks to base test file.
- Line: 213 has mock usage `PurchaseOrderService mockService = mock(PurchaseOrderService.class);`
- Required: Move mocks to base test file.
- Line: 249 has mock usage `PurchaseOrderService mockService = mock(PurchaseOrderService.class);`
- Required: Move mocks to base test file.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 145 method `updatePurchaseOrder_NotFound`
- Required rename: `updatePurchaseOrder_NotFound_Failure`
- Line: 161 method `updatePurchaseOrder_ClientMissingForCleanup`
- Required rename: `updatePurchaseOrder_ClientMissingForCleanup_Success`
- Line: 227 method `updatePurchaseOrder_VerifyPreAuthorizeAnnotation`
- Required rename: `updatePurchaseOrder_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: updatePurchaseOrder_VerifyPreAuthorizeAnnotation (line 225)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 76 in `updatePurchaseOrder_WithAttachments_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 145 in `updatePurchaseOrder_NotFound` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 161 in `updatePurchaseOrder_ClientMissingForCleanup` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 211 in `updatePurchaseOrder_controller_permission_unauthorized` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 227 in `updatePurchaseOrder_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 247 in `updatePurchaseOrder_WithValidRequest_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 89 inline mock in `updatePurchaseOrder_WithAttachments_Success`: `lenient().when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),`
- Required: Move to base test stub method and call stub in test.
- Line: 91 inline mock in `updatePurchaseOrder_WithAttachments_Success`: `lenient().when(addressRepository.save(any(com.example.SpringApi.Models.DatabaseModels.Address.class))).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 92 inline mock in `updatePurchaseOrder_WithAttachments_Success`: `lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);`
- Required: Move to base test stub method and call stub in test.
- Line: 93 inline mock in `updatePurchaseOrder_WithAttachments_Success`: `lenient().when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);`
- Required: Move to base test stub method and call stub in test.
- Line: 95 inline mock in `updatePurchaseOrder_WithAttachments_Success`: `lenient().when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {`
- Required: Move to base test stub method and call stub in test.
- Line: 100 inline mock in `updatePurchaseOrder_WithAttachments_Success`: `lenient().when(shipmentProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));`
- Required: Move to base test stub method and call stub in test.
- Line: 102 inline mock in `updatePurchaseOrder_WithAttachments_Success`: `lenient().when(shipmentPackageRepository.save(any(ShipmentPackage.class))).thenAnswer(invocation -> {`
- Required: Move to base test stub method and call stub in test.
- Line: 107 inline mock in `updatePurchaseOrder_WithAttachments_Success`: `lenient().when(shipmentPackageProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));`
- Required: Move to base test stub method and call stub in test.
- Line: 108 inline mock in `updatePurchaseOrder_WithAttachments_Success`: `lenient().when(resourcesRepository.save(any(Resources.class))).thenReturn(new Resources());`
- Required: Move to base test stub method and call stub in test.
- Line: 117 inline mock in `updatePurchaseOrder_WithAttachments_Success`: `lenient().when(mock.uploadPurchaseOrderAttachments(anyList(), anyString(), anyString(), anyLong()))`
- Required: Move to base test stub method and call stub in test.
- Line: 119 inline mock in `updatePurchaseOrder_WithAttachments_Success`: `lenient().when(mock.deleteMultipleImages(anyList())).thenReturn(1);`
- Required: Move to base test stub method and call stub in test.
- Line: 215 inline mock in `updatePurchaseOrder_controller_permission_unauthorized`: `doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 216 inline mock in `updatePurchaseOrder_controller_permission_unauthorized`: `.when(mockService).updatePurchaseOrder(any(PurchaseOrderRequestModel.class));`
- Required: Move to base test stub method and call stub in test.
- Line: 251 inline mock in `updatePurchaseOrder_WithValidRequest_DelegatesToService`: `doNothing().when(mockService).updatePurchaseOrder(testPurchaseOrderRequest);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['FAILURE', 'PERMISSION', 'SUCCESS']
- Required: Success → Failure → Permission.

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: updatePurchaseOrder_NotFound, updatePurchaseOrder_ClientMissingForCleanup
- Required order: updatePurchaseOrder_ClientMissingForCleanup, updatePurchaseOrder_NotFound

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 4 issues above.
- Fix Rule 6 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/BulkCreatePurchaseOrdersAsyncTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PurchaseOrder
Class: * Test class for PurchaseOrderService.bulkCreatePurchaseOrdersAsync method.
Extends: None
Lines of Code: 153
Last Modified: 2026-02-11 01:04:14
Declared Test Count: 6 (first occurrence line 29)
Actual @Test Count: 6

VIOLATIONS FOUND:

VIOLATION 1: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 44 method `bulkCreatePurchaseOrdersAsync_Success`
- Required rename: `bulkCreatePurchaseOrdersAsync_Success_Success`

VIOLATION 2: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: bulkCreatePurchaseOrdersAsync_VerifyPreAuthorizeAnnotation_Success (line 120)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 3: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 44 in `bulkCreatePurchaseOrdersAsync_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 68 in `bulkCreatePurchaseOrdersAsync_NullList_HandlesErrorGracefully` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 81 in `bulkCreatePurchaseOrdersAsync_EmptyList_HandlesErrorGracefully` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 103 in `bulkCreatePurchaseOrdersAsync_controller_permission_unauthorized` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 122 in `bulkCreatePurchaseOrdersAsync_VerifyPreAuthorizeAnnotation_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 142 in `bulkCreatePurchaseOrdersAsync_WithValidRequest_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 4: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 107 inline mock in `bulkCreatePurchaseOrdersAsync_controller_permission_unauthorized`: `when(purchaseOrderServiceMock.getUserId()).thenReturn(TEST_USER_ID);`
- Required: Move to base test stub method and call stub in test.
- Line: 108 inline mock in `bulkCreatePurchaseOrdersAsync_controller_permission_unauthorized`: `when(purchaseOrderServiceMock.getUser()).thenReturn("testuser");`
- Required: Move to base test stub method and call stub in test.
- Line: 109 inline mock in `bulkCreatePurchaseOrdersAsync_controller_permission_unauthorized`: `when(purchaseOrderServiceMock.getClientId()).thenReturn(TEST_CLIENT_ID);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['FAILURE', 'PERMISSION', 'SUCCESS']
- Required: Success → Failure → Permission.

VIOLATION 6: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: bulkCreatePurchaseOrdersAsync_NullList_HandlesErrorGracefully, bulkCreatePurchaseOrdersAsync_EmptyList_HandlesErrorGracefully
- Required order: bulkCreatePurchaseOrdersAsync_EmptyList_HandlesErrorGracefully, bulkCreatePurchaseOrdersAsync_NullList_HandlesErrorGracefully

REQUIRED FIXES SUMMARY:
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/TogglePOTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PurchaseOrder
Class: * Test class for PurchaseOrderService.togglePurchaseOrder method.
Extends: None
Lines of Code: 159
Last Modified: 2026-02-10 23:56:34
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 6

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 6` immediately after the class opening brace.

VIOLATION 2: Rule 4 - Test Annotations
- Severity: HIGH
- Line: 96 has disallowed annotation @TestFactory.
- Required: Remove or replace with allowed annotations only.

VIOLATION 3: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 125 has mock usage `mock(com.example.SpringApi.Services.PurchaseOrderService.class);`
- Required: Move mocks to base test file.
- Line: 150 has mock usage `com.example.SpringApi.Services.PurchaseOrderService mockService = mock(com.example.SpringApi.Services.PurchaseOrderService.class);`
- Required: Move mocks to base test file.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 40 method `togglePurchaseOrder_Success_MarkAsDeleted`
- Required rename: `togglePO_Success_MarkAsDeleted`
- Line: 58 method `togglePurchaseOrder_Success_Restore`
- Required rename: `togglePO_Success_Restore`
- Line: 80 method `togglePurchaseOrder_NotFound`
- Required rename: `togglePurchaseOrder_NotFound_Failure`
- Line: 80 method `togglePurchaseOrder_NotFound`
- Required rename: `togglePurchaseOrder_NotFound_Failure`
- Line: 122 method `togglePurchaseOrder_controller_permission_unauthorized`
- Required rename: `togglePO_controller_permission_unauthorized`
- Line: 139 method `togglePurchaseOrder_VerifyPreAuthorizeAnnotation`
- Required rename: `togglePurchaseOrder_VerifyPreAuthorizeAnnotation_Success`
- Line: 139 method `togglePurchaseOrder_VerifyPreAuthorizeAnnotation`
- Required rename: `togglePurchaseOrder_VerifyPreAuthorizeAnnotation_Success`
- Line: 149 method `togglePurchaseOrder_WithValidId_DelegatesToService`
- Required rename: `togglePO_WithValidId_DelegatesToService`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: togglePurchaseOrder_Success_MarkAsDeleted (line 38), togglePurchaseOrder_Success_Restore (line 56), togglePurchaseOrder_NotFound (line 78), togglePurchaseOrder_VerifyPreAuthorizeAnnotation (line 137), togglePurchaseOrder_WithValidId_DelegatesToService (line 147)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 139 in `togglePurchaseOrder_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 149 in `togglePurchaseOrder_WithValidId_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 43 inline mock in `togglePurchaseOrder_Success_MarkAsDeleted`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 45 inline mock in `togglePurchaseOrder_Success_MarkAsDeleted`: `when(purchaseOrderRepository.save(any())).thenReturn(testPurchaseOrder);`
- Required: Move to base test stub method and call stub in test.
- Line: 61 inline mock in `togglePurchaseOrder_Success_Restore`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 63 inline mock in `togglePurchaseOrder_Success_Restore`: `when(purchaseOrderRepository.save(any())).thenReturn(testPurchaseOrder);`
- Required: Move to base test stub method and call stub in test.
- Line: 82 inline mock in `togglePurchaseOrder_NotFound`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 127 inline mock in `togglePurchaseOrder_controller_permission_unauthorized`: `doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 128 inline mock in `togglePurchaseOrder_controller_permission_unauthorized`: `.when(mockService).togglePurchaseOrder(TEST_PO_ID);`
- Required: Move to base test stub method and call stub in test.
- Line: 152 inline mock in `togglePurchaseOrder_WithValidId_DelegatesToService`: `doNothing().when(mockService).togglePurchaseOrder(TEST_PO_ID);`
- Required: Move to base test stub method and call stub in test.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 4 issues above.
- Fix Rule 6 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/GetPurchaseOrderDetailsByIdTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PurchaseOrder
Class: * Test class for PurchaseOrderService.getPurchaseOrderDetailsById method.
Extends: None
Lines of Code: 137
Last Modified: 2026-02-11 00:27:09
Declared Test Count: 5 (first occurrence line 28)
Actual @Test Count: 5

VIOLATIONS FOUND:

VIOLATION 1: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 126 method `getPODetailsById_WithValidId_DelegatesToService`
- Required rename: `getPurchaseOrderDetailsById_WithValidId_DelegatesToService`

VIOLATION 2: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getPurchaseOrderDetailsById_VerifyPreAuthorizeAnnotation_Success (line 104)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 3: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 43 in `getPurchaseOrderDetailsById_Success_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 70 in `getPurchaseOrderDetailsById_NotFound_Failure` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 92 in `getPurchaseOrderDetailsById_controller_permission_unauthorized` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 106 in `getPurchaseOrderDetailsById_VerifyPreAuthorizeAnnotation_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 126 in `getPODetailsById_WithValidId_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 4: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['PERMISSION', 'SUCCESS', 'FAILURE']
- Required: Success → Failure → Permission.

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section PERMISSION not alphabetical.
- Current order: getPurchaseOrderDetailsById_controller_permission_unauthorized, getPurchaseOrderDetailsById_VerifyPreAuthorizeAnnotation_Success, getPODetailsById_WithValidId_DelegatesToService
- Required order: getPODetailsById_WithValidId_DelegatesToService, getPurchaseOrderDetailsById_controller_permission_unauthorized, getPurchaseOrderDetailsById_VerifyPreAuthorizeAnnotation_Success

REQUIRED FIXES SUMMARY:
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/RejectedByPurchaseOrderTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PurchaseOrder
Class: * Test class for PurchaseOrderService.rejectedByPurchaseOrder method.
Extends: None
Lines of Code: 149
Last Modified: 2026-02-10 23:56:34
Declared Test Count: 6 (first occurrence line 27)
Actual @Test Count: 6

VIOLATIONS FOUND:

VIOLATION 1: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 138 method `rejectedByPO_WithValidId_DelegatesToService`
- Required rename: `rejectedByPurchaseOrder_WithValidId_DelegatesToService`

VIOLATION 2: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: rejectedByPurchaseOrder_VerifyPreAuthorizeAnnotation_Success (line 116)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 3: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 42 in `rejectedByPurchaseOrder_Success_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 65 in `rejectedByPurchaseOrder_AlreadyRejected_Failure` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 82 in `rejectedByPurchaseOrder_NotFound_Failure` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 104 in `rejectedByPurchaseOrder_controller_permission_unauthorized` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 118 in `rejectedByPurchaseOrder_VerifyPreAuthorizeAnnotation_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 138 in `rejectedByPO_WithValidId_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 4: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['FAILURE', 'PERMISSION', 'SUCCESS']
- Required: Success → Failure → Permission.

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section PERMISSION not alphabetical.
- Current order: rejectedByPurchaseOrder_controller_permission_unauthorized, rejectedByPurchaseOrder_VerifyPreAuthorizeAnnotation_Success, rejectedByPO_WithValidId_DelegatesToService
- Required order: rejectedByPO_WithValidId_DelegatesToService, rejectedByPurchaseOrder_controller_permission_unauthorized, rejectedByPurchaseOrder_VerifyPreAuthorizeAnnotation_Success

REQUIRED FIXES SUMMARY:
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/GetPurchaseOrderPDFTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PurchaseOrder
Class: * Test class for PurchaseOrderService.getPurchaseOrderPDF method.
Extends: None
Lines of Code: 287
Last Modified: 2026-02-11 00:27:09
Declared Test Count: 11 (first occurrence line 34)
Actual @Test Count: 11

VIOLATIONS FOUND:

VIOLATION 1: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 49 method `getPurchaseOrderPDF_Success`
- Required rename: `getPurchaseOrderPDF_Success_Success`

VIOLATION 2: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getPurchaseOrderPDF_VerifyPreAuthorizeAnnotation_Success (line 254)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 3: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 49 in `getPurchaseOrderPDF_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 103 in `getPurchaseOrderPDF_PurchaseOrderNotFound_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 119 in `getPurchaseOrderPDF_OrderSummaryMissing_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 138 in `getPurchaseOrderPDF_AddressMissing_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 156 in `getPurchaseOrderPDF_CreatedByMissing_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 175 in `getPurchaseOrderPDF_ApprovedByMissing_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 195 in `getPurchaseOrderPDF_LeadMissing_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 215 in `getPurchaseOrderPDF_ClientMissing_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 242 in `getPurchaseOrderPDF_controller_permission_unauthorized` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 256 in `getPurchaseOrderPDF_VerifyPreAuthorizeAnnotation_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 276 in `getPurchaseOrderPDF_WithValidId_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 4: Rule 8 - Error Constants
- Severity: HIGH
- Line: 128 has hardcoded message: `assertTrue(ex.getMessage().contains("OrderSummary not found"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 128 has hardcoded message: `assertTrue(ex.getMessage().contains("OrderSummary not found"));`
- Required: Replace with an ErrorMessages constant (add one if missing).

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['FAILURE', 'PERMISSION', 'SUCCESS']
- Required: Success → Failure → Permission.

VIOLATION 6: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: getPurchaseOrderPDF_PurchaseOrderNotFound_ThrowsNotFoundException, getPurchaseOrderPDF_OrderSummaryMissing_ThrowsNotFoundException, getPurchaseOrderPDF_AddressMissing_ThrowsNotFoundException, getPurchaseOrderPDF_CreatedByMissing_ThrowsNotFoundException, getPurchaseOrderPDF_ApprovedByMissing_ThrowsNotFoundException, getPurchaseOrderPDF_LeadMissing_ThrowsNotFoundException, getPurchaseOrderPDF_ClientMissing_ThrowsNotFoundException
- Required order: getPurchaseOrderPDF_AddressMissing_ThrowsNotFoundException, getPurchaseOrderPDF_ApprovedByMissing_ThrowsNotFoundException, getPurchaseOrderPDF_ClientMissing_ThrowsNotFoundException, getPurchaseOrderPDF_CreatedByMissing_ThrowsNotFoundException, getPurchaseOrderPDF_LeadMissing_ThrowsNotFoundException, getPurchaseOrderPDF_OrderSummaryMissing_ThrowsNotFoundException, getPurchaseOrderPDF_PurchaseOrderNotFound_ThrowsNotFoundException

REQUIRED FIXES SUMMARY:
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 8 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/GetPODetailsByIdTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PurchaseOrder
Class: * Test class for PurchaseOrderService.getPurchaseOrderDetailsById method.
Extends: None
Lines of Code: 143
Last Modified: 2026-02-10 23:56:34
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 5

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 5` immediately after the class opening brace.

VIOLATION 2: Rule 4 - Test Annotations
- Severity: HIGH
- Line: 79 has disallowed annotation @TestFactory.
- Required: Remove or replace with allowed annotations only.

VIOLATION 3: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 108 has mock usage `mock(com.example.SpringApi.Services.PurchaseOrderService.class);`
- Required: Move mocks to base test file.
- Line: 133 has mock usage `com.example.SpringApi.Services.PurchaseOrderService mockService = mock(com.example.SpringApi.Services.PurchaseOrderService.class);`
- Required: Move mocks to base test file.
- Line: 136 has mock usage `.thenReturn(mock(PurchaseOrderResponseModel.class));`
- Required: Move mocks to base test file.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 43 method `getPurchaseOrderDetailsById_Success`
- Required rename: `getPurchaseOrderDetailsById_Success_Success`
- Line: 43 method `getPurchaseOrderDetailsById_Success`
- Required rename: `getPurchaseOrderDetailsById_Success_Success`
- Line: 64 method `getPurchaseOrderDetailsById_NotFound`
- Required rename: `getPurchaseOrderDetailsById_NotFound_Failure`
- Line: 64 method `getPurchaseOrderDetailsById_NotFound`
- Required rename: `getPurchaseOrderDetailsById_NotFound_Failure`
- Line: 105 method `getPurchaseOrderDetailsById_controller_permission_unauthorized`
- Required rename: `getPODetailsById_controller_permission_unauthorized`
- Line: 122 method `getPurchaseOrderDetailsById_VerifyPreAuthorizeAnnotation`
- Required rename: `getPurchaseOrderDetailsById_VerifyPreAuthorizeAnnotation_Success`
- Line: 122 method `getPurchaseOrderDetailsById_VerifyPreAuthorizeAnnotation`
- Required rename: `getPurchaseOrderDetailsById_VerifyPreAuthorizeAnnotation_Success`
- Line: 132 method `getPurchaseOrderDetailsById_WithValidId_DelegatesToService`
- Required rename: `getPODetailsById_WithValidId_DelegatesToService`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getPurchaseOrderDetailsById_Success (line 41), getPurchaseOrderDetailsById_NotFound (line 62), getPurchaseOrderDetailsById_VerifyPreAuthorizeAnnotation (line 120), getPurchaseOrderDetailsById_WithValidId_DelegatesToService (line 130)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 122 in `getPurchaseOrderDetailsById_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 132 in `getPurchaseOrderDetailsById_WithValidId_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 45 inline mock in `getPurchaseOrderDetailsById_Success`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientIdWithAllRelations(`
- Required: Move to base test stub method and call stub in test.
- Line: 66 inline mock in `getPurchaseOrderDetailsById_NotFound`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientIdWithAllRelations(`
- Required: Move to base test stub method and call stub in test.
- Line: 110 inline mock in `getPurchaseOrderDetailsById_controller_permission_unauthorized`: `when(mockService.getPurchaseOrderDetailsById(TEST_PO_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 135 inline mock in `getPurchaseOrderDetailsById_WithValidId_DelegatesToService`: `when(mockService.getPurchaseOrderDetailsById(TEST_PO_ID))`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['FAILURE', 'SUCCESS', 'PERMISSION']
- Required: Success → Failure → Permission.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 4 issues above.
- Fix Rule 6 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/RejectedByPOTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PurchaseOrder
Class: * Test class for PurchaseOrderService.rejectedByPurchaseOrder method.
Extends: None
Lines of Code: 154
Last Modified: 2026-02-10 23:56:34
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 6

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 6` immediately after the class opening brace.

VIOLATION 2: Rule 4 - Test Annotations
- Severity: HIGH
- Line: 91 has disallowed annotation @TestFactory.
- Required: Remove or replace with allowed annotations only.

VIOLATION 3: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 120 has mock usage `mock(com.example.SpringApi.Services.PurchaseOrderService.class);`
- Required: Move mocks to base test file.
- Line: 145 has mock usage `com.example.SpringApi.Services.PurchaseOrderService mockService = mock(com.example.SpringApi.Services.PurchaseOrderService.class);`
- Required: Move mocks to base test file.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 40 method `rejectedByPurchaseOrder_Success`
- Required rename: `rejectedByPurchaseOrder_Success_Success`
- Line: 40 method `rejectedByPurchaseOrder_Success`
- Required rename: `rejectedByPurchaseOrder_Success_Success`
- Line: 61 method `rejectedByPurchaseOrder_AlreadyRejected`
- Required rename: `rejectedByPurchaseOrder_AlreadyRejected_Success`
- Line: 61 method `rejectedByPurchaseOrder_AlreadyRejected`
- Required rename: `rejectedByPurchaseOrder_AlreadyRejected_Success`
- Line: 75 method `rejectedByPurchaseOrder_NotFound`
- Required rename: `rejectedByPurchaseOrder_NotFound_Failure`
- Line: 75 method `rejectedByPurchaseOrder_NotFound`
- Required rename: `rejectedByPurchaseOrder_NotFound_Failure`
- Line: 117 method `rejectedByPurchaseOrder_controller_permission_unauthorized`
- Required rename: `rejectedByPO_controller_permission_unauthorized`
- Line: 134 method `rejectedByPurchaseOrder_VerifyPreAuthorizeAnnotation`
- Required rename: `rejectedByPurchaseOrder_VerifyPreAuthorizeAnnotation_Success`
- Line: 134 method `rejectedByPurchaseOrder_VerifyPreAuthorizeAnnotation`
- Required rename: `rejectedByPurchaseOrder_VerifyPreAuthorizeAnnotation_Success`
- Line: 144 method `rejectedByPurchaseOrder_WithValidId_DelegatesToService`
- Required rename: `rejectedByPO_WithValidId_DelegatesToService`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: rejectedByPurchaseOrder_Success (line 38), rejectedByPurchaseOrder_AlreadyRejected (line 59), rejectedByPurchaseOrder_NotFound (line 73), rejectedByPurchaseOrder_VerifyPreAuthorizeAnnotation (line 132), rejectedByPurchaseOrder_WithValidId_DelegatesToService (line 142)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 134 in `rejectedByPurchaseOrder_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 144 in `rejectedByPurchaseOrder_WithValidId_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 43 inline mock in `rejectedByPurchaseOrder_Success`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 45 inline mock in `rejectedByPurchaseOrder_Success`: `when(purchaseOrderRepository.save(any())).thenReturn(testPurchaseOrder);`
- Required: Move to base test stub method and call stub in test.
- Line: 64 inline mock in `rejectedByPurchaseOrder_AlreadyRejected`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 77 inline mock in `rejectedByPurchaseOrder_NotFound`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 122 inline mock in `rejectedByPurchaseOrder_controller_permission_unauthorized`: `doThrow(new UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 123 inline mock in `rejectedByPurchaseOrder_controller_permission_unauthorized`: `.when(mockService).rejectedByPurchaseOrder(TEST_PO_ID);`
- Required: Move to base test stub method and call stub in test.
- Line: 147 inline mock in `rejectedByPurchaseOrder_WithValidId_DelegatesToService`: `doNothing().when(mockService).rejectedByPurchaseOrder(TEST_PO_ID);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['FAILURE', 'SUCCESS', 'PERMISSION']
- Required: Success → Failure → Permission.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 4 issues above.
- Fix Rule 6 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.


**IMPLEMENTATION PLAN (STEP-BY-STEP)**
1. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/ApprovedByPurchaseOrderTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
2. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/ApprovedByPOTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
3. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/CreatePurchaseOrderTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
4. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/GetPurchaseOrdersInBatchesTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
5. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/GetPOsInBatchesTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
6. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/TogglePurchaseOrderTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
7. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/UpdatePurchaseOrderTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
8. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/BulkCreatePurchaseOrdersAsyncTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
9. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/TogglePOTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
10. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/GetPurchaseOrderDetailsByIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
11. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/RejectedByPurchaseOrderTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
12. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/GetPurchaseOrderPDFTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
13. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/GetPODetailsByIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
14. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/RejectedByPOTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
15. Resolve extra test file `ApprovedByPOTest.java` by renaming it to match a public method or removing it.
16. Resolve extra test file `GetPOsInBatchesTest.java` by renaming it to match a public method or removing it.
17. Resolve extra test file `TogglePOTest.java` by renaming it to match a public method or removing it.
18. Resolve extra test file `GetPODetailsByIdTest.java` by renaming it to match a public method or removing it.
19. Resolve extra test file `RejectedByPOTest.java` by renaming it to match a public method or removing it.

Verification Commands (run after fixes):
- mvn -Dtest=ApprovedByPurchaseOrderTest test
- mvn -Dtest=ApprovedByPOTest test
- mvn -Dtest=CreatePurchaseOrderTest test
- mvn -Dtest=GetPurchaseOrdersInBatchesTest test
- mvn -Dtest=GetPOsInBatchesTest test
- mvn -Dtest=TogglePurchaseOrderTest test