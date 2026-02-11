# UNIT TEST VERIFICATION REPORT — Shipping

```
╔════════════════════════════════════════════════════════════╗
║           UNIT TEST VERIFICATION REPORT                    ║
║                                                            ║
║  Status: ❌ RED                                               ║
║  Services Analyzed: 1                                      ║
║  Public Methods Found: 10                                 ║
║  Test Files Expected: 10                                 ║
║  Test Files Found: 10                                    ║
║  Total Violations: 44                                    ║
╚════════════════════════════════════════════════════════════╝
```

VIOLATIONS BY RULE:

| Rule | Description | Count |
| --- | --- | --- |
| 1 | One Test File per Method | 3 |
| 2 | Test Count Declaration | 4 |
| 3 | Controller Permission Test | 10 |
| 5 | Test Naming Convention | 9 |
| 10 | Test Ordering | 16 |
| 11 | Complete Coverage | 1 |
| 14 | No Inline Mocks | 1 |


**MISSING/EXTRA TEST FILES (RULE 1)**

══════════════════════════════════════════════════════════════════════
MISSING FILE: ProcessShipmentsAfterPaymentApprovalTest.java
══════════════════════════════════════════════════════════════════════
Service: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/main/java/com/example/SpringApi/Services/ShippingService.java`
Method: `processShipmentsAfterPaymentApproval` (line 326)
Expected Test Path: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/ProcessShipmentsAfterPaymentApprovalTest.java`
Required Minimum Tests:
- processShipmentsAfterPaymentApproval_success
- processShipmentsAfterPaymentApproval_controller_permission_forbidden
- Add failure tests for each validation/exception path in the service method body.
Required Stubs:
- Add stub methods in the base test class for each repository/service interaction in the method.
Extra test file with no matching public method: `ProcessShipmentsAfterPaymentApprovalOnlineTest.java`. Either rename it to match a public method or remove it.
Extra test file with no matching public method: `ProcessShipmentsAfterPaymentApprovalCashTest.java`. Either rename it to match a public method or remove it.


**BASE TEST FILE ISSUES**
Base Test: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/ShippingServiceTestBase.java`
- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line 298: `lenient().when(packageRepository.findById(anyLong())).thenReturn(Optional.of(testPackage));`. Move this into a `stub...` method.


**FILE-BY-FILE BREAKDOWN**

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/CalculateShippingTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Shipping
Class: class CalculateShippingTest extends ShippingServiceTestBase {
Extends: ShippingServiceTestBase
Lines of Code: 292
Last Modified: 2026-02-10 18:02:59
Declared Test Count: 12 (first occurrence line 25)
Actual @Test Count: 12

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `calculateShipping_controller_permission_forbidden` or `calculateShipping_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 2: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 281 method `calculateShipping_VerifyPreAuthorizeAnnotation`
- Required rename: `calculateShipping_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 3: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: FAILURE
- Required: Add Success, Failure, Permission section headers.

VIOLATION 4: Rule 11 - Complete Coverage
- Severity: HIGH
- Coverage by test names is incomplete.
- Missing: at least one failure/exception test (e.g., *_throws*, *_exception*, *_invalid*).

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 10 issues above.
- Fix Rule 11 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/CancelShipmentTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Shipping
Class: class CancelShipmentTest extends ShippingServiceTestBase {
Extends: ShippingServiceTestBase
Lines of Code: 235
Last Modified: 2026-02-10 18:05:46
Declared Test Count: 9 (first occurrence line 24)
Actual @Test Count: 9

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `cancelShipment_controller_permission_forbidden` or `cancelShipment_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 2: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 224 method `cancelShipment_VerifyPreAuthorizeAnnotation`
- Required rename: `cancelShipment_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 3: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: cancelShipment_ValidRequest_Success, cancelShipment_SavesShipment_Success
- Required order: cancelShipment_SavesShipment_Success, cancelShipment_ValidRequest_Success

VIOLATION 4: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: cancelShipment_NotFound_ThrowsNotFoundException, cancelShipment_AlreadyCancelled_ThrowsBadRequestException, cancelShipment_MissingOrderId_ThrowsBadRequestException, cancelShipment_CredentialsMissing_ThrowsBadRequestException, cancelShipment_NonNumericOrderId_ThrowsBadRequestException, cancelShipment_CancelApiError_ThrowsBadRequestException
- Required order: cancelShipment_AlreadyCancelled_ThrowsBadRequestException, cancelShipment_CancelApiError_ThrowsBadRequestException, cancelShipment_CredentialsMissing_ThrowsBadRequestException, cancelShipment_MissingOrderId_ThrowsBadRequestException, cancelShipment_NonNumericOrderId_ThrowsBadRequestException, cancelShipment_NotFound_ThrowsNotFoundException

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/GetShipmentByIdTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Shipping
Class: class GetShipmentByIdTest extends ShippingServiceTestBase {
Extends: ShippingServiceTestBase
Lines of Code: 330
Last Modified: 2026-02-10 17:52:06
Declared Test Count: 14 (first occurrence line 22)
Actual @Test Count: 14

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getShipmentById_controller_permission_forbidden` or `getShipmentById_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 2: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 319 method `getShipmentById_VerifyPreAuthorizeAnnotation`
- Required rename: `getShipmentById_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 3: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: getShipmentById_ValidId_Success, getShipmentById_DifferentId_Success, getShipmentById_ShipRocketOrderIdPresent_Success
- Required order: getShipmentById_DifferentId_Success, getShipmentById_ShipRocketOrderIdPresent_Success, getShipmentById_ValidId_Success

VIOLATION 4: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: getShipmentById_NullId_ThrowsBadRequestException, getShipmentById_ZeroId_ThrowsBadRequestException, getShipmentById_NegativeId_ThrowsBadRequestException, getShipmentById_NotFound_ThrowsNotFoundException, getShipmentById_ClientMismatch_ThrowsNotFoundException, getShipmentById_ShipRocketOrderIdNull_ThrowsNotFoundException, getShipmentById_ShipRocketOrderIdEmpty_ThrowsNotFoundException, getShipmentById_ShipRocketOrderIdWhitespace_ThrowsNotFoundException, getShipmentById_MultipleInvalidIds_ThrowsBadRequestException, getShipmentById_LargeIdNotFound_ThrowsNotFoundException
- Required order: getShipmentById_ClientMismatch_ThrowsNotFoundException, getShipmentById_LargeIdNotFound_ThrowsNotFoundException, getShipmentById_MultipleInvalidIds_ThrowsBadRequestException, getShipmentById_NegativeId_ThrowsBadRequestException, getShipmentById_NotFound_ThrowsNotFoundException, getShipmentById_NullId_ThrowsBadRequestException, getShipmentById_ShipRocketOrderIdEmpty_ThrowsNotFoundException, getShipmentById_ShipRocketOrderIdNull_ThrowsNotFoundException, getShipmentById_ShipRocketOrderIdWhitespace_ThrowsNotFoundException, getShipmentById_ZeroId_ThrowsBadRequestException

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/GetWalletBalanceTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Shipping
Class: class GetWalletBalanceTest extends ShippingServiceTestBase {
Extends: ShippingServiceTestBase
Lines of Code: 98
Last Modified: 2026-02-10 18:03:52
Declared Test Count: 3 (first occurrence line 20)
Actual @Test Count: 3

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getWalletBalance_controller_permission_forbidden` or `getWalletBalance_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 2: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 35 method `getWalletBalance_Success`
- Required rename: `getWalletBalance_Success_Success`
- Line: 87 method `getWalletBalance_VerifyPreAuthorizeAnnotation`
- Required rename: `getWalletBalance_VerifyPreAuthorizeAnnotation_Success`

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/ProcessShipmentsAfterPaymentApprovalOnlineTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Shipping
Class: class ProcessShipmentsAfterPaymentApprovalOnlineTest extends ShippingServiceTestBase {
Extends: ShippingServiceTestBase
Lines of Code: 613
Last Modified: 2026-02-10 18:06:35
Declared Test Count: 23 (first occurrence line 25)
Actual @Test Count: 19

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 25
- Current: 23
- Required: 19

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `processShipmentsAfterPaymentApprovalOnline_controller_permission_forbidden` or `processShipmentsAfterPaymentApprovalOnline_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: PERMISSION
- Required: Add Success, Failure, Permission section headers.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/CancelReturnShipmentTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Shipping
Class: class CancelReturnShipmentTest extends ShippingServiceTestBase {
Extends: ShippingServiceTestBase
Lines of Code: 191
Last Modified: 2026-02-10 18:05:01
Declared Test Count: 7 (first occurrence line 24)
Actual @Test Count: 7

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `cancelReturnShipment_controller_permission_forbidden` or `cancelReturnShipment_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 2: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 180 method `cancelReturnShipment_VerifyPreAuthorizeAnnotation`
- Required rename: `cancelReturnShipment_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 3: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: cancelReturnShipment_ValidRequest_Success, cancelReturnShipment_SavesReturnShipment_Success
- Required order: cancelReturnShipment_SavesReturnShipment_Success, cancelReturnShipment_ValidRequest_Success

VIOLATION 4: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: cancelReturnShipment_NotFound_ThrowsNotFoundException, cancelReturnShipment_AlreadyCancelled_ThrowsBadRequestException, cancelReturnShipment_MissingOrderId_ThrowsBadRequestException, cancelReturnShipment_CancelApiError_ThrowsBadRequestException
- Required order: cancelReturnShipment_AlreadyCancelled_ThrowsBadRequestException, cancelReturnShipment_CancelApiError_ThrowsBadRequestException, cancelReturnShipment_MissingOrderId_ThrowsBadRequestException, cancelReturnShipment_NotFound_ThrowsNotFoundException

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/GetShipmentsInBatchesTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Shipping
Class: class GetShipmentsInBatchesTest extends ShippingServiceTestBase {
Extends: ShippingServiceTestBase
Lines of Code: 345
Last Modified: 2026-02-10 18:05:24
Declared Test Count: 14 (first occurrence line 24)
Actual @Test Count: 14

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getShipmentsInBatches_controller_permission_forbidden` or `getShipmentsInBatches_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 2: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 334 method `getShipmentsInBatches_VerifyPreAuthorizeAnnotation`
- Required rename: `getShipmentsInBatches_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 3: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: getShipmentsInBatches_ValidRequest_Success, getShipmentsInBatches_EmptyResult_Success, getShipmentsInBatches_ValidFilters_Success, getShipmentsInBatches_SelectedIds_Success, getShipmentsInBatches_LogicOperator_Success, getShipmentsInBatches_MultipleShipments_Success
- Required order: getShipmentsInBatches_EmptyResult_Success, getShipmentsInBatches_LogicOperator_Success, getShipmentsInBatches_MultipleShipments_Success, getShipmentsInBatches_SelectedIds_Success, getShipmentsInBatches_ValidFilters_Success, getShipmentsInBatches_ValidRequest_Success

VIOLATION 4: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: getShipmentsInBatches_InvalidColumn_ThrowsBadRequestException, getShipmentsInBatches_InvalidOperator_ThrowsBadRequestException, getShipmentsInBatches_StartEqualsEnd_ThrowsBadRequestException, getShipmentsInBatches_StartGreaterThanEnd_ThrowsBadRequestException, getShipmentsInBatches_ZeroPageSize_ThrowsBadRequestException, getShipmentsInBatches_NegativePageSize_ThrowsBadRequestException, getShipmentsInBatches_EndLessThanStart_ThrowsBadRequestException
- Required order: getShipmentsInBatches_EndLessThanStart_ThrowsBadRequestException, getShipmentsInBatches_InvalidColumn_ThrowsBadRequestException, getShipmentsInBatches_InvalidOperator_ThrowsBadRequestException, getShipmentsInBatches_NegativePageSize_ThrowsBadRequestException, getShipmentsInBatches_StartEqualsEnd_ThrowsBadRequestException, getShipmentsInBatches_StartGreaterThanEnd_ThrowsBadRequestException, getShipmentsInBatches_ZeroPageSize_ThrowsBadRequestException

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/ProcessShipmentsAfterPaymentApprovalCashTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Shipping
Class: class ProcessShipmentsAfterPaymentApprovalCashTest extends ShippingServiceTestBase {
Extends: ShippingServiceTestBase
Lines of Code: 776
Last Modified: 2026-02-10 18:06:24
Declared Test Count: 25 (first occurrence line 32)
Actual @Test Count: 24

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 32
- Current: 25
- Required: 24

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `processShipmentsAfterPaymentApprovalCash_controller_permission_forbidden` or `processShipmentsAfterPaymentApprovalCash_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 765 method `processShipmentsAfterPaymentApprovalCash_VerifyPreAuthorizeAnnotation`
- Required rename: `processShipmentsAfterPaymentApprovalCash_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 4: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: processShipmentsAfterPaymentApprovalCash_ValidRequest_Success, processShipmentsAfterPaymentApprovalCash_SavesShipment_Success, processShipmentsAfterPaymentApprovalCash_LogsUserAction_Success
- Required order: processShipmentsAfterPaymentApprovalCash_LogsUserAction_Success, processShipmentsAfterPaymentApprovalCash_SavesShipment_Success, processShipmentsAfterPaymentApprovalCash_ValidRequest_Success

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: processShipmentsAfterPaymentApprovalCash_PoNotFound_ThrowsNotFoundException, processShipmentsAfterPaymentApprovalCash_ClientMismatch_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_StatusNotPending_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_OrderSummaryNotFound_ThrowsNotFoundException, processShipmentsAfterPaymentApprovalCash_ShipmentsNull_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipmentsEmpty_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ProductNotAvailable_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ProductStockInsufficient_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_PackageNotAvailable_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_PackageStockInsufficient_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_PaymentFailure_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_CredentialsMissing_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_DeliveryAddressMissing_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_PickupLocationNotFound_ThrowsNotFoundException, processShipmentsAfterPaymentApprovalCash_ShipRocketResponseNull_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipRocketResponseMessage_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipRocketMissingOrderId_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipRocketMissingShipmentId_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipRocketMissingStatus_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipRocketInvalidStatus_ThrowsBadRequestException
- Required order: processShipmentsAfterPaymentApprovalCash_ClientMismatch_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_CredentialsMissing_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_DeliveryAddressMissing_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_OrderSummaryNotFound_ThrowsNotFoundException, processShipmentsAfterPaymentApprovalCash_PackageNotAvailable_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_PackageStockInsufficient_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_PaymentFailure_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_PickupLocationNotFound_ThrowsNotFoundException, processShipmentsAfterPaymentApprovalCash_PoNotFound_ThrowsNotFoundException, processShipmentsAfterPaymentApprovalCash_ProductNotAvailable_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ProductStockInsufficient_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipmentsEmpty_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipmentsNull_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipRocketInvalidStatus_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipRocketMissingOrderId_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipRocketMissingShipmentId_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipRocketMissingStatus_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipRocketResponseMessage_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipRocketResponseNull_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_StatusNotPending_ThrowsBadRequestException

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/CreateReturnTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Shipping
Class: class CreateReturnTest extends ShippingServiceTestBase {
Extends: ShippingServiceTestBase
Lines of Code: 793
Last Modified: 2026-02-10 21:13:17
Declared Test Count: 31 (first occurrence line 27)
Actual @Test Count: 29

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 27
- Current: 31
- Required: 29

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `createReturn_controller_permission_forbidden` or `createReturn_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 782 method `createReturn_VerifyPreAuthorizeAnnotation`
- Required rename: `createReturn_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 4: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: createReturn_FullReturn_Success, createReturn_PartialReturn_Success, createReturn_AwbAssignFailure_Success, createReturn_DeliveredDateNull_Success
- Required order: createReturn_AwbAssignFailure_Success, createReturn_DeliveredDateNull_Success, createReturn_FullReturn_Success, createReturn_PartialReturn_Success

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: createReturn_ShipmentIdNull_ThrowsBadRequestException, createReturn_ProductsNull_ThrowsBadRequestException, createReturn_ProductsEmpty_ThrowsBadRequestException, createReturn_ShipmentNotFound_ThrowsNotFoundException, createReturn_NotDelivered_ThrowsBadRequestException, createReturn_CredentialsMissing_ThrowsBadRequestException, createReturn_ProductIdNull_ThrowsBadRequestException, createReturn_QuantityNull_ThrowsBadRequestException, createReturn_ReasonNull_ThrowsBadRequestException, createReturn_ReasonEmpty_ThrowsBadRequestException, createReturn_QuantityZero_ThrowsBadRequestException, createReturn_QuantityNegative_ThrowsBadRequestException, createReturn_ShipmentProductsNull_ThrowsBadRequestException, createReturn_ReturnWindowNull_ThrowsBadRequestException, createReturn_SecondItemReasonNull_ThrowsBadRequestException, createReturn_SecondItemQuantityNull_ThrowsBadRequestException, createReturn_SecondItemProductIdNull_ThrowsBadRequestException, createReturn_MultipleItems_Success, createReturn_ProductNotInShipment_ThrowsBadRequestException, createReturn_QuantityExceeds_ThrowsBadRequestException, createReturn_ProductNotFound_ThrowsNotFoundException, createReturn_ProductNotReturnable_ThrowsBadRequestException, createReturn_PastReturnWindow_ThrowsBadRequestException, createReturn_ShipRocketFailure_ThrowsBadRequestException
- Required order: createReturn_CredentialsMissing_ThrowsBadRequestException, createReturn_MultipleItems_Success, createReturn_NotDelivered_ThrowsBadRequestException, createReturn_PastReturnWindow_ThrowsBadRequestException, createReturn_ProductIdNull_ThrowsBadRequestException, createReturn_ProductNotFound_ThrowsNotFoundException, createReturn_ProductNotInShipment_ThrowsBadRequestException, createReturn_ProductNotReturnable_ThrowsBadRequestException, createReturn_ProductsEmpty_ThrowsBadRequestException, createReturn_ProductsNull_ThrowsBadRequestException, createReturn_QuantityExceeds_ThrowsBadRequestException, createReturn_QuantityNegative_ThrowsBadRequestException, createReturn_QuantityNull_ThrowsBadRequestException, createReturn_QuantityZero_ThrowsBadRequestException, createReturn_ReasonEmpty_ThrowsBadRequestException, createReturn_ReasonNull_ThrowsBadRequestException, createReturn_ReturnWindowNull_ThrowsBadRequestException, createReturn_SecondItemProductIdNull_ThrowsBadRequestException, createReturn_SecondItemQuantityNull_ThrowsBadRequestException, createReturn_SecondItemReasonNull_ThrowsBadRequestException, createReturn_ShipmentIdNull_ThrowsBadRequestException, createReturn_ShipmentNotFound_ThrowsNotFoundException, createReturn_ShipmentProductsNull_ThrowsBadRequestException, createReturn_ShipRocketFailure_ThrowsBadRequestException

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/OptimizeOrderTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Shipping
Class: class OptimizeOrderTest extends ShippingServiceTestBase {
Extends: ShippingServiceTestBase
Lines of Code: 287
Last Modified: 2026-02-10 18:03:24
Declared Test Count: 12 (first occurrence line 26)
Actual @Test Count: 11

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 26
- Current: 12
- Required: 11

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `optimizeOrder_controller_permission_forbidden` or `optimizeOrder_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 276 method `optimizeOrder_VerifyPreAuthorizeAnnotation`
- Required rename: `optimizeOrder_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 4: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: optimizeOrder_ValidRequest_Success, optimizeOrder_CustomAllocation_Success
- Required order: optimizeOrder_CustomAllocation_Success, optimizeOrder_ValidRequest_Success

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: optimizeOrder_NullProductQuantities_Error, optimizeOrder_EmptyProductQuantities_Error, optimizeOrder_NullDeliveryPostcode_Error, optimizeOrder_EmptyDeliveryPostcode_Error, optimizeOrder_NoValidProducts_Error, optimizeOrder_MissingProduct_Error, optimizeOrder_NoPackagesConfigured_Error, optimizeOrder_Exception_Error
- Required order: optimizeOrder_EmptyDeliveryPostcode_Error, optimizeOrder_EmptyProductQuantities_Error, optimizeOrder_Exception_Error, optimizeOrder_MissingProduct_Error, optimizeOrder_NoPackagesConfigured_Error, optimizeOrder_NoValidProducts_Error, optimizeOrder_NullDeliveryPostcode_Error, optimizeOrder_NullProductQuantities_Error

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.


**IMPLEMENTATION PLAN (STEP-BY-STEP)**
1. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/CalculateShippingTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
2. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/CancelShipmentTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
3. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/GetShipmentByIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
4. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/GetWalletBalanceTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
5. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/ProcessShipmentsAfterPaymentApprovalOnlineTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
6. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/CancelReturnShipmentTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
7. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/GetShipmentsInBatchesTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
8. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/ProcessShipmentsAfterPaymentApprovalCashTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
9. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/CreateReturnTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
10. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/OptimizeOrderTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
11. Create `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/ProcessShipmentsAfterPaymentApprovalTest.java` for service method `processShipmentsAfterPaymentApproval` (line 326 in `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/main/java/com/example/SpringApi/Services/ShippingService.java`). Include class header, `// Total Tests`, Success/Failure/Permission sections, and a controller permission test.
12. Resolve extra test file `ProcessShipmentsAfterPaymentApprovalOnlineTest.java` by renaming it to match a public method or removing it.
13. Resolve extra test file `ProcessShipmentsAfterPaymentApprovalCashTest.java` by renaming it to match a public method or removing it.
14. Fix base test `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/ShippingServiceTestBase.java` violations noted above.

Verification Commands (run after fixes):
- mvn -Dtest=CalculateShippingTest test
- mvn -Dtest=CancelShipmentTest test
- mvn -Dtest=GetShipmentByIdTest test
- mvn -Dtest=GetWalletBalanceTest test
- mvn -Dtest=ProcessShipmentsAfterPaymentApprovalOnlineTest test
- mvn -Dtest=CancelReturnShipmentTest test