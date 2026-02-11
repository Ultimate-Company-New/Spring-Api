# UNIT TEST VERIFICATION REPORT — PurchaseOrder

```
╔════════════════════════════════════════════════════════════╗
║           UNIT TEST VERIFICATION REPORT                    ║
║                                                            ║
║  Status: ❌ RED                                               ║
║  Services Analyzed: 1                                      ║
║  Public Methods Found: 9                                  ║
║  Test Files Expected: 9                                  ║
║  Test Files Found: 8                                     ║
║  Total Violations: 102                                   ║
╚════════════════════════════════════════════════════════════╝
```

VIOLATIONS BY RULE:

| Rule | Description | Count |
| --- | --- | --- |
| 1 | One Test File per Method | 13 |
| 2 | Test Count Declaration | 8 |
| 3 | Controller Permission Test | 8 |
| 4 | Test Annotations | 7 |
| 5 | Test Naming Convention | 8 |
| 6 | Centralized Mocking | 7 |
| 8 | Error Constants | 2 |
| 9 | Test Documentation | 8 |
| 10 | Test Ordering | 8 |
| 11 | Complete Coverage | 3 |
| 12 | Arrange/Act/Assert | 8 |
| 14 | No Inline Mocks | 22 |


**MISSING/EXTRA TEST FILES (RULE 1)**

══════════════════════════════════════════════════════════════════════
MISSING FILE: GetPurchaseOrdersInBatchesTest.java
══════════════════════════════════════════════════════════════════════
Service: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/main/java/com/example/SpringApi/Services/PurchaseOrderService.java`
Method: `getPurchaseOrdersInBatches` (line 162)
Expected Test Path: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/GetPurchaseOrdersInBatchesTest.java`
Required Minimum Tests:
- getPurchaseOrdersInBatches_success
- getPurchaseOrdersInBatches_controller_permission_forbidden
- Add failure tests for each validation/exception path in the service method body.
Required Stubs:
- Add stub methods in the base test class for each repository/service interaction in the method.

══════════════════════════════════════════════════════════════════════
MISSING FILE: GetPurchaseOrderDetailsByIdTest.java
══════════════════════════════════════════════════════════════════════
Service: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/main/java/com/example/SpringApi/Services/PurchaseOrderService.java`
Method: `getPurchaseOrderDetailsById` (line 496)
Expected Test Path: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/GetPurchaseOrderDetailsByIdTest.java`
Required Minimum Tests:
- getPurchaseOrderDetailsById_success
- getPurchaseOrderDetailsById_controller_permission_forbidden
- Add failure tests for each validation/exception path in the service method body.
Required Stubs:
- Add stub methods in the base test class for each repository/service interaction in the method.

══════════════════════════════════════════════════════════════════════
MISSING FILE: TogglePurchaseOrderTest.java
══════════════════════════════════════════════════════════════════════
Service: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/main/java/com/example/SpringApi/Services/PurchaseOrderService.java`
Method: `togglePurchaseOrder` (line 599)
Expected Test Path: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/TogglePurchaseOrderTest.java`
Required Minimum Tests:
- togglePurchaseOrder_success
- togglePurchaseOrder_controller_permission_forbidden
- Add failure tests for each validation/exception path in the service method body.
Required Stubs:
- Add stub methods in the base test class for each repository/service interaction in the method.

══════════════════════════════════════════════════════════════════════
MISSING FILE: ApprovedByPurchaseOrderTest.java
══════════════════════════════════════════════════════════════════════
Service: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/main/java/com/example/SpringApi/Services/PurchaseOrderService.java`
Method: `approvedByPurchaseOrder` (line 635)
Expected Test Path: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/ApprovedByPurchaseOrderTest.java`
Required Minimum Tests:
- approvedByPurchaseOrder_success
- approvedByPurchaseOrder_controller_permission_forbidden
- Add failure tests for each validation/exception path in the service method body.
Required Stubs:
- Add stub methods in the base test class for each repository/service interaction in the method.

══════════════════════════════════════════════════════════════════════
MISSING FILE: RejectedByPurchaseOrderTest.java
══════════════════════════════════════════════════════════════════════
Service: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/main/java/com/example/SpringApi/Services/PurchaseOrderService.java`
Method: `rejectedByPurchaseOrder` (line 673)
Expected Test Path: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/RejectedByPurchaseOrderTest.java`
Required Minimum Tests:
- rejectedByPurchaseOrder_success
- rejectedByPurchaseOrder_controller_permission_forbidden
- Add failure tests for each validation/exception path in the service method body.
Required Stubs:
- Add stub methods in the base test class for each repository/service interaction in the method.

══════════════════════════════════════════════════════════════════════
MISSING FILE: GetPurchaseOrderPDFTest.java
══════════════════════════════════════════════════════════════════════
Service: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/main/java/com/example/SpringApi/Services/PurchaseOrderService.java`
Method: `getPurchaseOrderPDF` (line 715)
Expected Test Path: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/GetPurchaseOrderPDFTest.java`
Required Minimum Tests:
- getPurchaseOrderPDF_success
- getPurchaseOrderPDF_controller_permission_forbidden
- Add failure tests for each validation/exception path in the service method body.
Required Stubs:
- Add stub methods in the base test class for each repository/service interaction in the method.

══════════════════════════════════════════════════════════════════════
MISSING FILE: BulkCreatePurchaseOrdersAsyncTest.java
══════════════════════════════════════════════════════════════════════
Service: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/main/java/com/example/SpringApi/Services/PurchaseOrderService.java`
Method: `bulkCreatePurchaseOrdersAsync` (line 1019)
Expected Test Path: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/BulkCreatePurchaseOrdersAsyncTest.java`
Required Minimum Tests:
- bulkCreatePurchaseOrdersAsync_success
- bulkCreatePurchaseOrdersAsync_controller_permission_forbidden
- Add failure tests for each validation/exception path in the service method body.
Required Stubs:
- Add stub methods in the base test class for each repository/service interaction in the method.
Extra test file with no matching public method: `ApprovedByPOTest.java`. Either rename it to match a public method or remove it.
Extra test file with no matching public method: `GetPOsInBatchesTest.java`. Either rename it to match a public method or remove it.
Extra test file with no matching public method: `PurchaseOrderValidationTest.java`. Either rename it to match a public method or remove it.
Extra test file with no matching public method: `TogglePOTest.java`. Either rename it to match a public method or remove it.
Extra test file with no matching public method: `GetPODetailsByIdTest.java`. Either rename it to match a public method or remove it.
Extra test file with no matching public method: `RejectedByPOTest.java`. Either rename it to match a public method or remove it.


**BASE TEST FILE ISSUES**
Base Test: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/PurchaseOrderServiceTestBase.java`
- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line 114: `lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);`. Move this into a `stub...` method.
- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line 115: `lenient().when(environment.getProperty("imageLocation")).thenReturn("imgbb");`. Move this into a `stub...` method.
- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line 116: `lenient().when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });`. Move this into a `stub...` method.
- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line 117: `lenient().when(clientRepository.findById(anyLong())).thenReturn(Optional.of(testClient));`. Move this into a `stub...` method.
- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line 118: `lenient().when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(),`. Move this into a `stub...` method.
- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line 120: `lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);`. Move this into a `stub...` method.
- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line 121: `lenient().when(orderSummaryRepository.findByEntityTypeAndEntityId(anyString(), anyLong()))`. Move this into a `stub...` method.
- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line 123: `lenient().when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);`. Move this into a `stub...` method.
- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line 124: `lenient().when(shipmentRepository.findByOrderSummaryId(anyLong())).thenReturn(Collections.emptyList());`. Move this into a `stub...` method.
- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line 125: `lenient().when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {`. Move this into a `stub...` method.
- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line 132: `lenient().when(shipmentPackageRepository.findByShipmentId(anyLong())).thenReturn(Collections.emptyList());`. Move this into a `stub...` method.
- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line 133: `lenient().when(shipmentPackageRepository.save(any(ShipmentPackage.class))).thenAnswer(invocation -> {`. Move this into a `stub...` method.
- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line 140: `lenient().when(shipmentProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));`. Move this into a `stub...` method.
- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line 141: `lenient().when(shipmentPackageProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));`. Move this into a `stub...` method.


**FILE-BY-FILE BREAKDOWN**

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/ApprovedByPOTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PurchaseOrder
Class: * Test class for PurchaseOrderService.approvedByPurchaseOrder method.
Extends: None
Lines of Code: 129
Last Modified: 2026-02-10 21:13:18
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
- Line: 88 has disallowed annotation @TestFactory.
- Required: Remove or replace with allowed annotations only.

VIOLATION 3: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 120 has mock usage `com.example.SpringApi.Services.PurchaseOrderService mockService = mock(com.example.SpringApi.Services.PurchaseOrderService.class);`
- Required: Move mocks to base test file.

VIOLATION 4: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `approvedByPO_controller_permission_forbidden` or `approvedByPO_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 5: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 37 method `approvedByPurchaseOrder_Success`
- Required rename: `approvedByPurchaseOrder_Success_Success`
- Line: 37 method `approvedByPurchaseOrder_Success`
- Required rename: `approvedByPurchaseOrder_Success_Success`
- Line: 58 method `approvedByPurchaseOrder_AlreadyApproved`
- Required rename: `approvedByPurchaseOrder_AlreadyApproved_Success`
- Line: 58 method `approvedByPurchaseOrder_AlreadyApproved`
- Required rename: `approvedByPurchaseOrder_AlreadyApproved_Success`
- Line: 72 method `approvedByPurchaseOrder_NotFound`
- Required rename: `approvedByPurchaseOrder_NotFound_Failure`
- Line: 72 method `approvedByPurchaseOrder_NotFound`
- Required rename: `approvedByPurchaseOrder_NotFound_Failure`
- Line: 109 method `approvedByPurchaseOrder_VerifyPreAuthorizeAnnotation`
- Required rename: `approvedByPurchaseOrder_VerifyPreAuthorizeAnnotation_Success`
- Line: 109 method `approvedByPurchaseOrder_VerifyPreAuthorizeAnnotation`
- Required rename: `approvedByPurchaseOrder_VerifyPreAuthorizeAnnotation_Success`
- Line: 119 method `approvedByPurchaseOrder_WithValidId_DelegatesToService`
- Required rename: `approvedByPO_WithValidId_DelegatesToService`

VIOLATION 6: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: approvedByPurchaseOrder_Success (line 35), approvedByPurchaseOrder_AlreadyApproved (line 56), approvedByPurchaseOrder_NotFound (line 70), approvedByPurchaseOrder_WithValidId_DelegatesToService (line 117)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 7: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 109 in `approvedByPurchaseOrder_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 119 in `approvedByPurchaseOrder_WithValidId_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 8: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 40 inline mock in `approvedByPurchaseOrder_Success`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 42 inline mock in `approvedByPurchaseOrder_Success`: `when(purchaseOrderRepository.save(any())).thenReturn(testPurchaseOrder);`
- Required: Move to base test stub method and call stub in test.
- Line: 61 inline mock in `approvedByPurchaseOrder_AlreadyApproved`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 74 inline mock in `approvedByPurchaseOrder_NotFound`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 122 inline mock in `approvedByPurchaseOrder_WithValidId_DelegatesToService`: `doNothing().when(mockService).approvedByPurchaseOrder(TEST_PO_ID);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['FAILURE', 'SUCCESS', 'PERMISSION']
- Required: Success → Failure → Permission.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 4 issues above.
- Fix Rule 6 issues above.
- Fix Rule 3 issues above.
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
Lines of Code: 251
Last Modified: 2026-02-10 20:58:58
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
- Line: 135 has disallowed annotation @TestFactory.
- Required: Remove or replace with allowed annotations only.
- Line: 200 has disallowed annotation @SuppressWarnings.
- Required: Remove or replace with allowed annotations only.

VIOLATION 3: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 242 has mock usage `PurchaseOrderService mockService = mock(PurchaseOrderService.class);`
- Required: Move mocks to base test file.

VIOLATION 4: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `createPurchaseOrder_controller_permission_forbidden` or `createPurchaseOrder_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 5: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 185 method `createPurchaseOrder_PersistsCanonicalCustomPrice`
- Required rename: `createPurchaseOrder_PersistsCanonicalCustomPrice_Success`
- Line: 231 method `createPurchaseOrder_VerifyPreAuthorizeAnnotation`
- Required rename: `createPurchaseOrder_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 6: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: createPurchaseOrder_Success_WithoutAttachments (line 41), createPurchaseOrder_Success_WithAttachments (line 78), createPurchaseOrder_PersistsCanonicalCustomPrice (line 183), createPurchaseOrder_WithValidRequest_DelegatesToService (line 239)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 7: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 231 in `createPurchaseOrder_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 241 in `createPurchaseOrder_WithValidRequest_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 8: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 47 inline mock in `createPurchaseOrder_Success_WithoutAttachments`: `when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),`
- Required: Move to base test stub method and call stub in test.
- Line: 50 inline mock in `createPurchaseOrder_Success_WithoutAttachments`: `when(addressRepository.save(any(Address.class))).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 51 inline mock in `createPurchaseOrder_Success_WithoutAttachments`: `when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);`
- Required: Move to base test stub method and call stub in test.
- Line: 52 inline mock in `createPurchaseOrder_Success_WithoutAttachments`: `when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);`
- Required: Move to base test stub method and call stub in test.
- Line: 54 inline mock in `createPurchaseOrder_Success_WithoutAttachments`: `when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {`
- Required: Move to base test stub method and call stub in test.
- Line: 59 inline mock in `createPurchaseOrder_Success_WithoutAttachments`: `when(shipmentProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));`
- Required: Move to base test stub method and call stub in test.
- Line: 61 inline mock in `createPurchaseOrder_Success_WithoutAttachments`: `when(shipmentPackageRepository.save(any(ShipmentPackage.class))).thenAnswer(invocation -> {`
- Required: Move to base test stub method and call stub in test.
- Line: 66 inline mock in `createPurchaseOrder_Success_WithoutAttachments`: `when(shipmentPackageProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));`
- Required: Move to base test stub method and call stub in test.
- Line: 86 inline mock in `createPurchaseOrder_Success_WithAttachments`: `lenient().when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(),`
- Required: Move to base test stub method and call stub in test.
- Line: 89 inline mock in `createPurchaseOrder_Success_WithAttachments`: `lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 90 inline mock in `createPurchaseOrder_Success_WithAttachments`: `lenient().when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));`
- Required: Move to base test stub method and call stub in test.
- Line: 91 inline mock in `createPurchaseOrder_Success_WithAttachments`: `lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);`
- Required: Move to base test stub method and call stub in test.
- Line: 92 inline mock in `createPurchaseOrder_Success_WithAttachments`: `lenient().when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);`
- Required: Move to base test stub method and call stub in test.
- Line: 94 inline mock in `createPurchaseOrder_Success_WithAttachments`: `lenient().when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {`
- Required: Move to base test stub method and call stub in test.
- Line: 99 inline mock in `createPurchaseOrder_Success_WithAttachments`: `lenient().when(shipmentProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));`
- Required: Move to base test stub method and call stub in test.
- Line: 101 inline mock in `createPurchaseOrder_Success_WithAttachments`: `lenient().when(shipmentPackageRepository.save(any(ShipmentPackage.class))).thenAnswer(invocation -> {`
- Required: Move to base test stub method and call stub in test.
- Line: 106 inline mock in `createPurchaseOrder_Success_WithAttachments`: `lenient().when(shipmentPackageProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));`
- Required: Move to base test stub method and call stub in test.
- Line: 107 inline mock in `createPurchaseOrder_Success_WithAttachments`: `lenient().when(resourcesRepository.save(any(Resources.class))).thenReturn(new Resources());`
- Required: Move to base test stub method and call stub in test.
- Line: 116 inline mock in `createPurchaseOrder_Success_WithAttachments`: `when(mock.uploadPurchaseOrderAttachments(anyList(), anyString(), anyString(), anyLong()))`
- Required: Move to base test stub method and call stub in test.
- Line: 187 inline mock in `createPurchaseOrder_PersistsCanonicalCustomPrice`: `when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),`
- Required: Move to base test stub method and call stub in test.
- Line: 190 inline mock in `createPurchaseOrder_PersistsCanonicalCustomPrice`: `when(addressRepository.save(any(Address.class))).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 191 inline mock in `createPurchaseOrder_PersistsCanonicalCustomPrice`: `when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);`
- Required: Move to base test stub method and call stub in test.
- Line: 192 inline mock in `createPurchaseOrder_PersistsCanonicalCustomPrice`: `when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);`
- Required: Move to base test stub method and call stub in test.
- Line: 194 inline mock in `createPurchaseOrder_PersistsCanonicalCustomPrice`: `when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {`
- Required: Move to base test stub method and call stub in test.
- Line: 204 inline mock in `createPurchaseOrder_PersistsCanonicalCustomPrice`: `when(shipmentProductRepository.saveAll(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));`
- Required: Move to base test stub method and call stub in test.
- Line: 207 inline mock in `createPurchaseOrder_PersistsCanonicalCustomPrice`: `when(shipmentPackageRepository.save(any(ShipmentPackage.class))).thenAnswer(invocation -> {`
- Required: Move to base test stub method and call stub in test.
- Line: 212 inline mock in `createPurchaseOrder_PersistsCanonicalCustomPrice`: `when(shipmentPackageProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));`
- Required: Move to base test stub method and call stub in test.
- Line: 244 inline mock in `createPurchaseOrder_WithValidRequest_DelegatesToService`: `doNothing().when(mockService).createPurchaseOrder(testPurchaseOrderRequest);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: FAILURE
- Required: Add Success, Failure, Permission section headers.

VIOLATION 10: Rule 11 - Complete Coverage
- Severity: HIGH
- Coverage by test names is incomplete.
- Missing: at least one failure/exception test (e.g., *_throws*, *_exception*, *_invalid*).

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 4 issues above.
- Fix Rule 6 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.
- Fix Rule 11 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/GetPOsInBatchesTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PurchaseOrder
Class: * Test class for PurchaseOrderService.getPurchaseOrdersInBatches method.
Extends: None
Lines of Code: 135
Last Modified: 2026-02-10 21:13:18
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 3

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 3` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 124 has mock usage `com.example.SpringApi.Services.PurchaseOrderService mockService = mock(com.example.SpringApi.Services.PurchaseOrderService.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getPOsInBatches_controller_permission_forbidden` or `getPOsInBatches_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 40 method `getPurchaseOrdersInBatches_Comprehensive`
- Required rename: `getPurchaseOrdersInBatches_Comprehensive_Success`
- Line: 40 method `getPurchaseOrdersInBatches_Comprehensive`
- Required rename: `getPurchaseOrdersInBatches_Comprehensive_Success`
- Line: 113 method `getPurchaseOrdersInBatches_VerifyPreAuthorizeAnnotation`
- Required rename: `getPurchaseOrdersInBatches_VerifyPreAuthorizeAnnotation_Success`
- Line: 113 method `getPurchaseOrdersInBatches_VerifyPreAuthorizeAnnotation`
- Required rename: `getPurchaseOrdersInBatches_VerifyPreAuthorizeAnnotation_Success`
- Line: 123 method `getPurchaseOrdersInBatches_WithValidRequest_DelegatesToService`
- Required rename: `getPOsInBatches_WithValidRequest_DelegatesToService`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getPurchaseOrdersInBatches_Comprehensive (line 38), getPurchaseOrdersInBatches_WithValidRequest_DelegatesToService (line 121)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 40 in `getPurchaseOrdersInBatches_Comprehensive` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 113 in `getPurchaseOrdersInBatches_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 123 in `getPurchaseOrdersInBatches_WithValidRequest_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 73 inline mock in `getPurchaseOrdersInBatches_Comprehensive`: `lenient().when(purchaseOrderFilterQueryBuilder.getColumnType("vendorNumber")).thenReturn("string");`
- Required: Move to base test stub method and call stub in test.
- Line: 93 inline mock in `getPurchaseOrdersInBatches_Comprehensive`: `lenient().when(purchaseOrderFilterQueryBuilder.getColumnType("vendorNumber")).thenReturn("string");`
- Required: Move to base test stub method and call stub in test.
- Line: 94 inline mock in `getPurchaseOrdersInBatches_Comprehensive`: `when(purchaseOrderFilterQueryBuilder.findPaginatedWithDetails(`
- Required: Move to base test stub method and call stub in test.
- Line: 128 inline mock in `getPurchaseOrdersInBatches_WithValidRequest_DelegatesToService`: `when(mockService.getPurchaseOrdersInBatches(request)).thenReturn(mockResponse);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 8 - Error Constants
- Severity: HIGH
- Line: 47 has hardcoded message: `assertEquals("Invalid pagination: end must be greater than start", paginationEx.getMessage());`
- Required: Replace with `ErrorMessages.PurchaseOrderErrorMessages.InvalidPagination`.
- Line: 61 has hardcoded message: `assertEquals("Invalid column name: invalidColumn", invalidColumnEx.getMessage());`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 76 has hardcoded message: `assertEquals("Invalid operator: invalidOperator", invalidOpEx.getMessage());`
- Required: Replace with an ErrorMessages constant (add one if missing).

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: FAILURE, SUCCESS
- Required: Add Success, Failure, Permission section headers.

VIOLATION 10: Rule 11 - Complete Coverage
- Severity: HIGH
- Coverage by test names is incomplete.
- Missing: at least one *_success test.
- Missing: at least one failure/exception test (e.g., *_throws*, *_exception*, *_invalid*).

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 8 issues above.
- Fix Rule 10 issues above.
- Fix Rule 11 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/UpdatePurchaseOrderTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PurchaseOrder
Class: * Test class for PurchaseOrderService.updatePurchaseOrder method.
Extends: None
Lines of Code: 193
Last Modified: 2026-02-10 20:58:58
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
- Line: 152 has disallowed annotation @TestFactory.
- Required: Remove or replace with allowed annotations only.

VIOLATION 3: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 184 has mock usage `PurchaseOrderService mockService = mock(PurchaseOrderService.class);`
- Required: Move mocks to base test file.

VIOLATION 4: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `updatePurchaseOrder_controller_permission_forbidden` or `updatePurchaseOrder_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 5: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 43 method `updatePurchaseOrder_Success`
- Required rename: `updatePurchaseOrder_Success_Success`
- Line: 116 method `updatePurchaseOrder_NotFound`
- Required rename: `updatePurchaseOrder_NotFound_Failure`
- Line: 127 method `updatePurchaseOrder_ClientMissingForCleanup`
- Required rename: `updatePurchaseOrder_ClientMissingForCleanup_Success`
- Line: 173 method `updatePurchaseOrder_VerifyPreAuthorizeAnnotation`
- Required rename: `updatePurchaseOrder_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 6: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: updatePurchaseOrder_Success (line 41), updatePurchaseOrder_NotFound (line 114), updatePurchaseOrder_ClientMissingForCleanup (line 125), updatePurchaseOrder_WithValidRequest_DelegatesToService (line 181)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 7: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 173 in `updatePurchaseOrder_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 183 in `updatePurchaseOrder_WithValidRequest_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 8: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 49 inline mock in `updatePurchaseOrder_Success`: `lenient().when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 51 inline mock in `updatePurchaseOrder_Success`: `lenient().when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(),`
- Required: Move to base test stub method and call stub in test.
- Line: 54 inline mock in `updatePurchaseOrder_Success`: `lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 55 inline mock in `updatePurchaseOrder_Success`: `lenient().when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.of(testClient));`
- Required: Move to base test stub method and call stub in test.
- Line: 56 inline mock in `updatePurchaseOrder_Success`: `lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);`
- Required: Move to base test stub method and call stub in test.
- Line: 57 inline mock in `updatePurchaseOrder_Success`: `lenient().when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);`
- Required: Move to base test stub method and call stub in test.
- Line: 65 inline mock in `updatePurchaseOrder_Success`: `lenient().when(resourcesRepository.findByEntityIdAndEntityType(anyLong(), anyString()))`
- Required: Move to base test stub method and call stub in test.
- Line: 69 inline mock in `updatePurchaseOrder_Success`: `lenient().when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {`
- Required: Move to base test stub method and call stub in test.
- Line: 76 inline mock in `updatePurchaseOrder_Success`: `lenient().when(shipmentProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));`
- Required: Move to base test stub method and call stub in test.
- Line: 78 inline mock in `updatePurchaseOrder_Success`: `lenient().when(shipmentPackageRepository.save(any(ShipmentPackage.class))).thenAnswer(invocation -> {`
- Required: Move to base test stub method and call stub in test.
- Line: 85 inline mock in `updatePurchaseOrder_Success`: `lenient().when(shipmentPackageProductRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));`
- Required: Move to base test stub method and call stub in test.
- Line: 86 inline mock in `updatePurchaseOrder_Success`: `lenient().when(resourcesRepository.save(any(Resources.class))).thenReturn(new Resources());`
- Required: Move to base test stub method and call stub in test.
- Line: 95 inline mock in `updatePurchaseOrder_Success`: `when(mock.uploadPurchaseOrderAttachments(anyList(), anyString(), anyString(), anyLong()))`
- Required: Move to base test stub method and call stub in test.
- Line: 97 inline mock in `updatePurchaseOrder_Success`: `when(mock.deleteMultipleImages(anyList())).thenReturn(1);`
- Required: Move to base test stub method and call stub in test.
- Line: 118 inline mock in `updatePurchaseOrder_NotFound`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 136 inline mock in `updatePurchaseOrder_ClientMissingForCleanup`: `lenient().when(resourcesRepository.findByEntityIdAndEntityType(anyLong(), anyString()))`
- Required: Move to base test stub method and call stub in test.
- Line: 139 inline mock in `updatePurchaseOrder_ClientMissingForCleanup`: `lenient().when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 141 inline mock in `updatePurchaseOrder_ClientMissingForCleanup`: `lenient().when(clientRepository.findById(TEST_CLIENT_ID)).thenReturn(Optional.empty());`
- Required: Move to base test stub method and call stub in test.
- Line: 186 inline mock in `updatePurchaseOrder_WithValidRequest_DelegatesToService`: `doNothing().when(mockService).updatePurchaseOrder(testPurchaseOrderRequest);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['FAILURE', 'SUCCESS', 'PERMISSION']
- Required: Success → Failure → Permission.

VIOLATION 10: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: updatePurchaseOrder_NotFound, updatePurchaseOrder_ClientMissingForCleanup
- Required order: updatePurchaseOrder_ClientMissingForCleanup, updatePurchaseOrder_NotFound

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 4 issues above.
- Fix Rule 6 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/PurchaseOrderValidationTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PurchaseOrder
Class: * Test class for PurchaseOrderService validation tests.
Extends: None
Lines of Code: 314
Last Modified: 2026-02-10 20:58:58
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 17

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 17` immediately after the class opening brace.

VIOLATION 2: Rule 4 - Test Annotations
- Severity: HIGH
- Line: 30 has disallowed annotation @Nested.
- Required: Remove or replace with allowed annotations only.
- Line: 218 has disallowed annotation @TestFactory.
- Required: Remove or replace with allowed annotations only.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `purchaseOrderValidation_controller_permission_forbidden` or `purchaseOrderValidation_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 36 method `createPurchaseOrder_NullRequest_ThrowsBadRequestException`
- Required rename: `purchaseOrderValidation_NullRequest_ThrowsBadRequestException`
- Line: 43 method `createPurchaseOrder_NullOrderSummary_ThrowsBadRequestException`
- Required rename: `purchaseOrderValidation_NullOrderSummary_ThrowsBadRequestException`
- Line: 51 method `createPurchaseOrder_MaxAttachmentsExceeded_ThrowsBadRequestException`
- Required rename: `purchaseOrderValidation_MaxAttachmentsExceeded_ThrowsBadRequestException`
- Line: 64 method `createPurchaseOrder_NullProducts_ThrowsBadRequestException`
- Required rename: `purchaseOrderValidation_NullProducts_ThrowsBadRequestException`
- Line: 72 method `createPurchaseOrder_EmptyProducts_ThrowsBadRequestException`
- Required rename: `purchaseOrderValidation_EmptyProducts_ThrowsBadRequestException`
- Line: 80 method `createPurchaseOrder_InvalidProductId_ThrowsBadRequestException`
- Required rename: `purchaseOrderValidation_InvalidProductId_ThrowsBadRequestException`
- Line: 88 method `createPurchaseOrder_InvalidQuantity_ThrowsBadRequestException`
- Required rename: `purchaseOrderValidation_InvalidQuantity_ThrowsBadRequestException`
- Line: 97 method `createPurchaseOrder_NullPricePerUnit_ThrowsBadRequestException`
- Required rename: `purchaseOrderValidation_NullPricePerUnit_ThrowsBadRequestException`
- Line: 106 method `createPurchaseOrder_NegativePricePerUnit_ThrowsBadRequestException`
- Required rename: `purchaseOrderValidation_NegativePricePerUnit_ThrowsBadRequestException`
- Line: 115 method `createPurchaseOrder_NullOrderStatus_ThrowsBadRequestException`
- Required rename: `purchaseOrderValidation_NullOrderStatus_ThrowsBadRequestException`
- Line: 123 method `createPurchaseOrder_EmptyOrderStatus_ThrowsBadRequestException`
- Required rename: `purchaseOrderValidation_EmptyOrderStatus_ThrowsBadRequestException`
- Line: 131 method `createPurchaseOrder_InvalidOrderStatusValue_ThrowsBadRequestException`
- Required rename: `purchaseOrderValidation_InvalidOrderStatusValue_ThrowsBadRequestException`
- Line: 139 method `createPurchaseOrder_NullAssignedLeadId_ThrowsBadRequestException`
- Required rename: `purchaseOrderValidation_NullAssignedLeadId_ThrowsBadRequestException`
- Line: 147 method `createPurchaseOrder_NoShipments_ThrowsBadRequestException`
- Required rename: `purchaseOrderValidation_NoShipments_ThrowsBadRequestException`
- Line: 162 method `createPurchaseOrder_ShipmentMissingCourier_ThrowsBadRequestException`
- Required rename: `purchaseOrderValidation_ShipmentMissingCourier_ThrowsBadRequestException`
- Line: 177 method `createPurchaseOrder_ShipmentMissingPackages_ThrowsBadRequestException`
- Required rename: `purchaseOrderValidation_ShipmentMissingPackages_ThrowsBadRequestException`
- Line: 195 method `createPurchaseOrder_PackageMissingProducts_ThrowsBadRequestException`
- Required rename: `purchaseOrderValidation_PackageMissingProducts_ThrowsBadRequestException`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: createPurchaseOrder_NullRequest_ThrowsBadRequestException (line 34), createPurchaseOrder_NullOrderSummary_ThrowsBadRequestException (line 41), createPurchaseOrder_MaxAttachmentsExceeded_ThrowsBadRequestException (line 49), createPurchaseOrder_NullProducts_ThrowsBadRequestException (line 62), createPurchaseOrder_EmptyProducts_ThrowsBadRequestException (line 70), createPurchaseOrder_InvalidProductId_ThrowsBadRequestException (line 78), createPurchaseOrder_InvalidQuantity_ThrowsBadRequestException (line 86), createPurchaseOrder_NullPricePerUnit_ThrowsBadRequestException (line 95), createPurchaseOrder_NegativePricePerUnit_ThrowsBadRequestException (line 104), createPurchaseOrder_NullOrderStatus_ThrowsBadRequestException (line 113), createPurchaseOrder_EmptyOrderStatus_ThrowsBadRequestException (line 121), createPurchaseOrder_InvalidOrderStatusValue_ThrowsBadRequestException (line 129), createPurchaseOrder_NullAssignedLeadId_ThrowsBadRequestException (line 137), createPurchaseOrder_NoShipments_ThrowsBadRequestException (line 145), createPurchaseOrder_ShipmentMissingCourier_ThrowsBadRequestException (line 160), createPurchaseOrder_ShipmentMissingPackages_ThrowsBadRequestException (line 175), createPurchaseOrder_PackageMissingProducts_ThrowsBadRequestException (line 193)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 36 in `createPurchaseOrder_NullRequest_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 43 in `createPurchaseOrder_NullOrderSummary_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 51 in `createPurchaseOrder_MaxAttachmentsExceeded_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 64 in `createPurchaseOrder_NullProducts_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 72 in `createPurchaseOrder_EmptyProducts_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 80 in `createPurchaseOrder_InvalidProductId_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 88 in `createPurchaseOrder_InvalidQuantity_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 97 in `createPurchaseOrder_NullPricePerUnit_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 106 in `createPurchaseOrder_NegativePricePerUnit_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 115 in `createPurchaseOrder_NullOrderStatus_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 123 in `createPurchaseOrder_EmptyOrderStatus_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 131 in `createPurchaseOrder_InvalidOrderStatusValue_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 139 in `createPurchaseOrder_NullAssignedLeadId_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 147 in `createPurchaseOrder_NoShipments_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 162 in `createPurchaseOrder_ShipmentMissingCourier_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 177 in `createPurchaseOrder_ShipmentMissingPackages_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 195 in `createPurchaseOrder_PackageMissingProducts_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 148 inline mock in `createPurchaseOrder_NoShipments_ThrowsBadRequestException`: `lenient().when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(),`
- Required: Move to base test stub method and call stub in test.
- Line: 151 inline mock in `createPurchaseOrder_NoShipments_ThrowsBadRequestException`: `lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 152 inline mock in `createPurchaseOrder_NoShipments_ThrowsBadRequestException`: `lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);`
- Required: Move to base test stub method and call stub in test.
- Line: 153 inline mock in `createPurchaseOrder_NoShipments_ThrowsBadRequestException`: `lenient().when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);`
- Required: Move to base test stub method and call stub in test.
- Line: 163 inline mock in `createPurchaseOrder_ShipmentMissingCourier_ThrowsBadRequestException`: `lenient().when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(),`
- Required: Move to base test stub method and call stub in test.
- Line: 166 inline mock in `createPurchaseOrder_ShipmentMissingCourier_ThrowsBadRequestException`: `lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 167 inline mock in `createPurchaseOrder_ShipmentMissingCourier_ThrowsBadRequestException`: `lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);`
- Required: Move to base test stub method and call stub in test.
- Line: 168 inline mock in `createPurchaseOrder_ShipmentMissingCourier_ThrowsBadRequestException`: `lenient().when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);`
- Required: Move to base test stub method and call stub in test.
- Line: 178 inline mock in `createPurchaseOrder_ShipmentMissingPackages_ThrowsBadRequestException`: `lenient().when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(),`
- Required: Move to base test stub method and call stub in test.
- Line: 181 inline mock in `createPurchaseOrder_ShipmentMissingPackages_ThrowsBadRequestException`: `lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 182 inline mock in `createPurchaseOrder_ShipmentMissingPackages_ThrowsBadRequestException`: `lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);`
- Required: Move to base test stub method and call stub in test.
- Line: 183 inline mock in `createPurchaseOrder_ShipmentMissingPackages_ThrowsBadRequestException`: `lenient().when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);`
- Required: Move to base test stub method and call stub in test.
- Line: 186 inline mock in `createPurchaseOrder_ShipmentMissingPackages_ThrowsBadRequestException`: `lenient().when(shipmentRepository.save(any(Shipment.class))).thenReturn(savedShipment);`
- Required: Move to base test stub method and call stub in test.
- Line: 196 inline mock in `createPurchaseOrder_PackageMissingProducts_ThrowsBadRequestException`: `lenient().when(addressRepository.findExactDuplicate(any(), any(), any(), any(), any(), any(), any(), any(),`
- Required: Move to base test stub method and call stub in test.
- Line: 199 inline mock in `createPurchaseOrder_PackageMissingProducts_ThrowsBadRequestException`: `lenient().when(addressRepository.save(any(Address.class))).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 200 inline mock in `createPurchaseOrder_PackageMissingProducts_ThrowsBadRequestException`: `lenient().when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(testPurchaseOrder);`
- Required: Move to base test stub method and call stub in test.
- Line: 201 inline mock in `createPurchaseOrder_PackageMissingProducts_ThrowsBadRequestException`: `lenient().when(orderSummaryRepository.save(any(OrderSummary.class))).thenReturn(testOrderSummary);`
- Required: Move to base test stub method and call stub in test.
- Line: 204 inline mock in `createPurchaseOrder_PackageMissingProducts_ThrowsBadRequestException`: `lenient().when(shipmentRepository.save(any(Shipment.class))).thenReturn(savedShipment);`
- Required: Move to base test stub method and call stub in test.
- Line: 205 inline mock in `createPurchaseOrder_PackageMissingProducts_ThrowsBadRequestException`: `lenient().when(shipmentPackageRepository.save(any(ShipmentPackage.class)))`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 8 - Error Constants
- Severity: HIGH
- Line: 92 has hardcoded message: `assertTrue(exception.getMessage().contains("Quantity must be greater than 0"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 92 has hardcoded message: `assertTrue(exception.getMessage().contains("Quantity must be greater than 0"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 101 has hardcoded message: `assertTrue(exception.getMessage().contains("pricePerUnit is required"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 101 has hardcoded message: `assertTrue(exception.getMessage().contains("pricePerUnit is required"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 110 has hardcoded message: `assertTrue(exception.getMessage().contains("pricePerUnit must be greater than or equal to 0"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 110 has hardcoded message: `assertTrue(exception.getMessage().contains("pricePerUnit must be greater than or equal to 0"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 262 has hardcoded message: `assertTrue(ex.getMessage().contains("Quantity must be greater than 0"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 262 has hardcoded message: `assertTrue(ex.getMessage().contains("Quantity must be greater than 0"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 269 has hardcoded message: `assertTrue(ex.getMessage().contains("pricePerUnit must be greater than or equal to 0"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 269 has hardcoded message: `assertTrue(ex.getMessage().contains("pricePerUnit must be greater than or equal to 0"));`
- Required: Replace with an ErrorMessages constant (add one if missing).

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: FAILURE, PERMISSION, SUCCESS
- Required: Add Success, Failure, Permission section headers.

VIOLATION 10: Rule 11 - Complete Coverage
- Severity: HIGH
- Coverage by test names is incomplete.
- Missing: at least one *_success test.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 4 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 8 issues above.
- Fix Rule 10 issues above.
- Fix Rule 11 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/TogglePOTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PurchaseOrder
Class: * Test class for PurchaseOrderService.togglePurchaseOrder method.
Extends: None
Lines of Code: 134
Last Modified: 2026-02-10 21:13:18
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
- Line: 93 has disallowed annotation @TestFactory.
- Required: Remove or replace with allowed annotations only.

VIOLATION 3: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 125 has mock usage `com.example.SpringApi.Services.PurchaseOrderService mockService = mock(com.example.SpringApi.Services.PurchaseOrderService.class);`
- Required: Move mocks to base test file.

VIOLATION 4: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `togglePO_controller_permission_forbidden` or `togglePO_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 5: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 37 method `togglePurchaseOrder_Success_MarkAsDeleted`
- Required rename: `togglePO_Success_MarkAsDeleted`
- Line: 55 method `togglePurchaseOrder_Success_Restore`
- Required rename: `togglePO_Success_Restore`
- Line: 77 method `togglePurchaseOrder_NotFound`
- Required rename: `togglePurchaseOrder_NotFound_Failure`
- Line: 77 method `togglePurchaseOrder_NotFound`
- Required rename: `togglePurchaseOrder_NotFound_Failure`
- Line: 114 method `togglePurchaseOrder_VerifyPreAuthorizeAnnotation`
- Required rename: `togglePurchaseOrder_VerifyPreAuthorizeAnnotation_Success`
- Line: 114 method `togglePurchaseOrder_VerifyPreAuthorizeAnnotation`
- Required rename: `togglePurchaseOrder_VerifyPreAuthorizeAnnotation_Success`
- Line: 124 method `togglePurchaseOrder_WithValidId_DelegatesToService`
- Required rename: `togglePO_WithValidId_DelegatesToService`

VIOLATION 6: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: togglePurchaseOrder_Success_MarkAsDeleted (line 35), togglePurchaseOrder_Success_Restore (line 53), togglePurchaseOrder_NotFound (line 75), togglePurchaseOrder_WithValidId_DelegatesToService (line 122)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 7: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 114 in `togglePurchaseOrder_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 124 in `togglePurchaseOrder_WithValidId_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 8: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 40 inline mock in `togglePurchaseOrder_Success_MarkAsDeleted`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 42 inline mock in `togglePurchaseOrder_Success_MarkAsDeleted`: `when(purchaseOrderRepository.save(any())).thenReturn(testPurchaseOrder);`
- Required: Move to base test stub method and call stub in test.
- Line: 58 inline mock in `togglePurchaseOrder_Success_Restore`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 60 inline mock in `togglePurchaseOrder_Success_Restore`: `when(purchaseOrderRepository.save(any())).thenReturn(testPurchaseOrder);`
- Required: Move to base test stub method and call stub in test.
- Line: 79 inline mock in `togglePurchaseOrder_NotFound`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 127 inline mock in `togglePurchaseOrder_WithValidId_DelegatesToService`: `doNothing().when(mockService).togglePurchaseOrder(TEST_PO_ID);`
- Required: Move to base test stub method and call stub in test.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 4 issues above.
- Fix Rule 6 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/GetPODetailsByIdTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PurchaseOrder
Class: * Test class for PurchaseOrderService.getPurchaseOrderDetailsById method.
Extends: None
Lines of Code: 118
Last Modified: 2026-02-10 21:13:18
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 4

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 4` immediately after the class opening brace.

VIOLATION 2: Rule 4 - Test Annotations
- Severity: HIGH
- Line: 76 has disallowed annotation @TestFactory.
- Required: Remove or replace with allowed annotations only.

VIOLATION 3: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 108 has mock usage `com.example.SpringApi.Services.PurchaseOrderService mockService = mock(com.example.SpringApi.Services.PurchaseOrderService.class);`
- Required: Move mocks to base test file.
- Line: 111 has mock usage `.thenReturn(mock(PurchaseOrderResponseModel.class));`
- Required: Move mocks to base test file.

VIOLATION 4: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getPODetailsById_controller_permission_forbidden` or `getPODetailsById_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 5: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 40 method `getPurchaseOrderDetailsById_Success`
- Required rename: `getPurchaseOrderDetailsById_Success_Success`
- Line: 40 method `getPurchaseOrderDetailsById_Success`
- Required rename: `getPurchaseOrderDetailsById_Success_Success`
- Line: 61 method `getPurchaseOrderDetailsById_NotFound`
- Required rename: `getPurchaseOrderDetailsById_NotFound_Failure`
- Line: 61 method `getPurchaseOrderDetailsById_NotFound`
- Required rename: `getPurchaseOrderDetailsById_NotFound_Failure`
- Line: 97 method `getPurchaseOrderDetailsById_VerifyPreAuthorizeAnnotation`
- Required rename: `getPurchaseOrderDetailsById_VerifyPreAuthorizeAnnotation_Success`
- Line: 97 method `getPurchaseOrderDetailsById_VerifyPreAuthorizeAnnotation`
- Required rename: `getPurchaseOrderDetailsById_VerifyPreAuthorizeAnnotation_Success`
- Line: 107 method `getPurchaseOrderDetailsById_WithValidId_DelegatesToService`
- Required rename: `getPODetailsById_WithValidId_DelegatesToService`

VIOLATION 6: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getPurchaseOrderDetailsById_Success (line 38), getPurchaseOrderDetailsById_NotFound (line 59), getPurchaseOrderDetailsById_WithValidId_DelegatesToService (line 105)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 7: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 97 in `getPurchaseOrderDetailsById_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 107 in `getPurchaseOrderDetailsById_WithValidId_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 8: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 42 inline mock in `getPurchaseOrderDetailsById_Success`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientIdWithAllRelations(`
- Required: Move to base test stub method and call stub in test.
- Line: 63 inline mock in `getPurchaseOrderDetailsById_NotFound`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientIdWithAllRelations(`
- Required: Move to base test stub method and call stub in test.
- Line: 110 inline mock in `getPurchaseOrderDetailsById_WithValidId_DelegatesToService`: `when(mockService.getPurchaseOrderDetailsById(TEST_PO_ID))`
- Required: Move to base test stub method and call stub in test.

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['FAILURE', 'SUCCESS', 'PERMISSION']
- Required: Success → Failure → Permission.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 4 issues above.
- Fix Rule 6 issues above.
- Fix Rule 3 issues above.
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
Lines of Code: 129
Last Modified: 2026-02-10 21:13:18
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
- Line: 88 has disallowed annotation @TestFactory.
- Required: Remove or replace with allowed annotations only.

VIOLATION 3: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 120 has mock usage `com.example.SpringApi.Services.PurchaseOrderService mockService = mock(com.example.SpringApi.Services.PurchaseOrderService.class);`
- Required: Move mocks to base test file.

VIOLATION 4: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `rejectedByPO_controller_permission_forbidden` or `rejectedByPO_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 5: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 37 method `rejectedByPurchaseOrder_Success`
- Required rename: `rejectedByPurchaseOrder_Success_Success`
- Line: 37 method `rejectedByPurchaseOrder_Success`
- Required rename: `rejectedByPurchaseOrder_Success_Success`
- Line: 58 method `rejectedByPurchaseOrder_AlreadyRejected`
- Required rename: `rejectedByPurchaseOrder_AlreadyRejected_Success`
- Line: 58 method `rejectedByPurchaseOrder_AlreadyRejected`
- Required rename: `rejectedByPurchaseOrder_AlreadyRejected_Success`
- Line: 72 method `rejectedByPurchaseOrder_NotFound`
- Required rename: `rejectedByPurchaseOrder_NotFound_Failure`
- Line: 72 method `rejectedByPurchaseOrder_NotFound`
- Required rename: `rejectedByPurchaseOrder_NotFound_Failure`
- Line: 109 method `rejectedByPurchaseOrder_VerifyPreAuthorizeAnnotation`
- Required rename: `rejectedByPurchaseOrder_VerifyPreAuthorizeAnnotation_Success`
- Line: 109 method `rejectedByPurchaseOrder_VerifyPreAuthorizeAnnotation`
- Required rename: `rejectedByPurchaseOrder_VerifyPreAuthorizeAnnotation_Success`
- Line: 119 method `rejectedByPurchaseOrder_WithValidId_DelegatesToService`
- Required rename: `rejectedByPO_WithValidId_DelegatesToService`

VIOLATION 6: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: rejectedByPurchaseOrder_Success (line 35), rejectedByPurchaseOrder_AlreadyRejected (line 56), rejectedByPurchaseOrder_NotFound (line 70), rejectedByPurchaseOrder_WithValidId_DelegatesToService (line 117)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 7: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 109 in `rejectedByPurchaseOrder_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 119 in `rejectedByPurchaseOrder_WithValidId_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 8: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 40 inline mock in `rejectedByPurchaseOrder_Success`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 42 inline mock in `rejectedByPurchaseOrder_Success`: `when(purchaseOrderRepository.save(any())).thenReturn(testPurchaseOrder);`
- Required: Move to base test stub method and call stub in test.
- Line: 61 inline mock in `rejectedByPurchaseOrder_AlreadyRejected`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 74 inline mock in `rejectedByPurchaseOrder_NotFound`: `when(purchaseOrderRepository.findByPurchaseOrderIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 122 inline mock in `rejectedByPurchaseOrder_WithValidId_DelegatesToService`: `doNothing().when(mockService).rejectedByPurchaseOrder(TEST_PO_ID);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Current order: ['FAILURE', 'SUCCESS', 'PERMISSION']
- Required: Success → Failure → Permission.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 4 issues above.
- Fix Rule 6 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.


**IMPLEMENTATION PLAN (STEP-BY-STEP)**
1. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/ApprovedByPOTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
2. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/CreatePurchaseOrderTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
3. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/GetPOsInBatchesTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
4. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/UpdatePurchaseOrderTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
5. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/PurchaseOrderValidationTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
6. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/TogglePOTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
7. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/GetPODetailsByIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
8. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/RejectedByPOTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
9. Create `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/GetPurchaseOrdersInBatchesTest.java` for service method `getPurchaseOrdersInBatches` (line 162 in `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/main/java/com/example/SpringApi/Services/PurchaseOrderService.java`). Include class header, `// Total Tests`, Success/Failure/Permission sections, and a controller permission test.
10. Create `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/GetPurchaseOrderDetailsByIdTest.java` for service method `getPurchaseOrderDetailsById` (line 496 in `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/main/java/com/example/SpringApi/Services/PurchaseOrderService.java`). Include class header, `// Total Tests`, Success/Failure/Permission sections, and a controller permission test.
11. Create `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/TogglePurchaseOrderTest.java` for service method `togglePurchaseOrder` (line 599 in `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/main/java/com/example/SpringApi/Services/PurchaseOrderService.java`). Include class header, `// Total Tests`, Success/Failure/Permission sections, and a controller permission test.
12. Create `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/ApprovedByPurchaseOrderTest.java` for service method `approvedByPurchaseOrder` (line 635 in `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/main/java/com/example/SpringApi/Services/PurchaseOrderService.java`). Include class header, `// Total Tests`, Success/Failure/Permission sections, and a controller permission test.
13. Create `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/RejectedByPurchaseOrderTest.java` for service method `rejectedByPurchaseOrder` (line 673 in `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/main/java/com/example/SpringApi/Services/PurchaseOrderService.java`). Include class header, `// Total Tests`, Success/Failure/Permission sections, and a controller permission test.
14. Create `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/GetPurchaseOrderPDFTest.java` for service method `getPurchaseOrderPDF` (line 715 in `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/main/java/com/example/SpringApi/Services/PurchaseOrderService.java`). Include class header, `// Total Tests`, Success/Failure/Permission sections, and a controller permission test.
15. Create `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/BulkCreatePurchaseOrdersAsyncTest.java` for service method `bulkCreatePurchaseOrdersAsync` (line 1019 in `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/main/java/com/example/SpringApi/Services/PurchaseOrderService.java`). Include class header, `// Total Tests`, Success/Failure/Permission sections, and a controller permission test.
16. Resolve extra test file `ApprovedByPOTest.java` by renaming it to match a public method or removing it.
17. Resolve extra test file `GetPOsInBatchesTest.java` by renaming it to match a public method or removing it.
18. Resolve extra test file `PurchaseOrderValidationTest.java` by renaming it to match a public method or removing it.
19. Resolve extra test file `TogglePOTest.java` by renaming it to match a public method or removing it.
20. Resolve extra test file `GetPODetailsByIdTest.java` by renaming it to match a public method or removing it.
21. Resolve extra test file `RejectedByPOTest.java` by renaming it to match a public method or removing it.
22. Fix base test `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PurchaseOrder/PurchaseOrderServiceTestBase.java` violations noted above.

Verification Commands (run after fixes):
- mvn -Dtest=ApprovedByPOTest test
- mvn -Dtest=CreatePurchaseOrderTest test
- mvn -Dtest=GetPOsInBatchesTest test
- mvn -Dtest=UpdatePurchaseOrderTest test
- mvn -Dtest=PurchaseOrderValidationTest test
- mvn -Dtest=TogglePOTest test