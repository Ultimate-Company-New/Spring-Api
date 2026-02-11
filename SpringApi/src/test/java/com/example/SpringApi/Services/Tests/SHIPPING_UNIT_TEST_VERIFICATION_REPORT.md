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
║  Total Violations: 21                                    ║
╚════════════════════════════════════════════════════════════╝
```

VIOLATIONS BY RULE:

| Rule | Description | Count |
| --- | --- | --- |
| 1 | One Test File per Method | 3 |
| 2 | Test Count Declaration | 1 |
| 5 | Test Naming Convention | 5 |
| 7 | Exception Assertions | 1 |
| 10 | Test Ordering | 10 |
| 12 | Arrange/Act/Assert | 1 |


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


**FILE-BY-FILE BREAKDOWN**

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/CalculateShippingTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Shipping
Class: class CalculateShippingTest extends ShippingServiceTestBase {
Extends: ShippingServiceTestBase
Lines of Code: 332
Last Modified: 2026-02-10 23:56:34
Declared Test Count: 14 (first occurrence line 28)
Actual @Test Count: 14

VIOLATIONS FOUND:

VIOLATION 1: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 284 in `calculateShipping_NullRequest_ThrowsException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 2: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 286 in `calculateShipping_NullRequest_ThrowsException`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 3: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: calculateShipping_NullPickupLocations_ReturnsEmptyResponse, calculateShipping_EmptyPickupLocations_ReturnsEmptyResponse, calculateShipping_ValidRequest_Success, calculateShipping_WeightBelowMinimum_Success, calculateShipping_SelectCheapestCourier_Success, calculateShipping_MultipleLocations_TotalCostSum, calculateShipping_HelperThrows_Continues, calculateShipping_NullCouriers_NoSelection, calculateShipping_EmptyCouriers_NoSelection, calculateShipping_CodTrue_Success, calculateShipping_NullDeliveryPostcode_Success
- Required order: calculateShipping_CodTrue_Success, calculateShipping_EmptyCouriers_NoSelection, calculateShipping_EmptyPickupLocations_ReturnsEmptyResponse, calculateShipping_HelperThrows_Continues, calculateShipping_MultipleLocations_TotalCostSum, calculateShipping_NullCouriers_NoSelection, calculateShipping_NullDeliveryPostcode_Success, calculateShipping_NullPickupLocations_ReturnsEmptyResponse, calculateShipping_SelectCheapestCourier_Success, calculateShipping_ValidRequest_Success, calculateShipping_WeightBelowMinimum_Success

REQUIRED FIXES SUMMARY:
- Fix Rule 12 issues above.
- Fix Rule 7 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/CancelShipmentTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Shipping
Class: class CancelShipmentTest extends ShippingServiceTestBase {
Extends: ShippingServiceTestBase
Lines of Code: 216
Last Modified: 2026-02-11 00:44:27
Declared Test Count: 10 (first occurrence line 26)
Actual @Test Count: 8

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 26
- Current: 10
- Required: 8

VIOLATION 2: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: cancelShipment_AlreadyCancelled_ThrowsBadRequestException, cancelShipment_CredentialsMissing_ThrowsBadRequestException, cancelShipment_MissingOrderId_ThrowsBadRequestException, cancelShipment_NonNumericOrderId_ThrowsBadRequestException, cancelShipment_CancelApiError_ThrowsBadRequestException
- Required order: cancelShipment_AlreadyCancelled_ThrowsBadRequestException, cancelShipment_CancelApiError_ThrowsBadRequestException, cancelShipment_CredentialsMissing_ThrowsBadRequestException, cancelShipment_MissingOrderId_ThrowsBadRequestException, cancelShipment_NonNumericOrderId_ThrowsBadRequestException

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/ProcessShipmentsAfterPaymentApprovalOnlineTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Shipping
Class: class ProcessShipmentsAfterPaymentApprovalOnlineTest extends ShippingServiceTestBase {
Extends: ShippingServiceTestBase
Lines of Code: 750
Last Modified: 2026-02-11 01:07:22
Declared Test Count: 23 (first occurrence line 29)
Actual @Test Count: 23

VIOLATIONS FOUND:

VIOLATION 1: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 738 method `processShipmentsAfterPaymentApprovalOnline_VerifyPreAuthorizeAnnotation`
- Required rename: `processShipmentsAfterPaymentApprovalOnline_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 2: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: processShipmentsAfterPaymentApprovalOnline_ValidRequest_Success, processShipmentsAfterPaymentApprovalOnline_SavesShipment_Success, processShipmentsAfterPaymentApprovalOnline_LogsUserAction_Success
- Required order: processShipmentsAfterPaymentApprovalOnline_LogsUserAction_Success, processShipmentsAfterPaymentApprovalOnline_SavesShipment_Success, processShipmentsAfterPaymentApprovalOnline_ValidRequest_Success

VIOLATION 3: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: processShipmentsAfterPaymentApprovalOnline_PoNotFound_ThrowsNotFoundException, processShipmentsAfterPaymentApprovalOnline_ClientMismatch_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_StatusNotPending_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_OrderSummaryNotFound_ThrowsNotFoundException, processShipmentsAfterPaymentApprovalOnline_ShipmentsEmpty_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_ProductNotAvailable_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_PaymentFailure_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_CredentialsMissing_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_DeliveryAddressMissing_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_PackageNotAvailable_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_PackageStockInsufficient_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_PickupLocationNotFound_ThrowsNotFoundException, processShipmentsAfterPaymentApprovalOnline_ShipRocketResponseNull_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_ShipRocketResponseMessage_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_ShipRocketMissingOrderId_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_ShipRocketMissingShipmentId_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_ShipRocketMissingStatus_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_ShipRocketInvalidStatus_ThrowsBadRequestException
- Required order: processShipmentsAfterPaymentApprovalOnline_ClientMismatch_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_CredentialsMissing_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_DeliveryAddressMissing_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_OrderSummaryNotFound_ThrowsNotFoundException, processShipmentsAfterPaymentApprovalOnline_PackageNotAvailable_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_PackageStockInsufficient_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_PaymentFailure_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_PickupLocationNotFound_ThrowsNotFoundException, processShipmentsAfterPaymentApprovalOnline_PoNotFound_ThrowsNotFoundException, processShipmentsAfterPaymentApprovalOnline_ProductNotAvailable_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_ShipmentsEmpty_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_ShipRocketInvalidStatus_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_ShipRocketMissingOrderId_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_ShipRocketMissingShipmentId_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_ShipRocketMissingStatus_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_ShipRocketResponseMessage_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_ShipRocketResponseNull_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalOnline_StatusNotPending_ThrowsBadRequestException

REQUIRED FIXES SUMMARY:
- Fix Rule 5 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/GetShipmentsInBatchesTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Shipping
Class: class GetShipmentsInBatchesTest extends ShippingServiceTestBase {
Extends: ShippingServiceTestBase
Lines of Code: 367
Last Modified: 2026-02-10 23:56:34
Declared Test Count: 15 (first occurrence line 26)
Actual @Test Count: 15

VIOLATIONS FOUND:

VIOLATION 1: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 356 method `getShipmentsInBatches_VerifyPreAuthorizeAnnotation`
- Required rename: `getShipmentsInBatches_VerifyPreAuthorizeAnnotation_Success`

REQUIRED FIXES SUMMARY:
- Fix Rule 5 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/ProcessShipmentsAfterPaymentApprovalCashTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Shipping
Class: class ProcessShipmentsAfterPaymentApprovalCashTest extends ShippingServiceTestBase {
Extends: ShippingServiceTestBase
Lines of Code: 800
Last Modified: 2026-02-10 23:56:34
Declared Test Count: 25 (first occurrence line 34)
Actual @Test Count: 25

VIOLATIONS FOUND:

VIOLATION 1: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 789 method `processShipmentsAfterPaymentApprovalCash_VerifyPreAuthorizeAnnotation`
- Required rename: `processShipmentsAfterPaymentApprovalCash_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 2: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: processShipmentsAfterPaymentApprovalCash_ValidRequest_Success, processShipmentsAfterPaymentApprovalCash_SavesShipment_Success, processShipmentsAfterPaymentApprovalCash_LogsUserAction_Success
- Required order: processShipmentsAfterPaymentApprovalCash_LogsUserAction_Success, processShipmentsAfterPaymentApprovalCash_SavesShipment_Success, processShipmentsAfterPaymentApprovalCash_ValidRequest_Success

VIOLATION 3: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: processShipmentsAfterPaymentApprovalCash_PoNotFound_ThrowsNotFoundException, processShipmentsAfterPaymentApprovalCash_ClientMismatch_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_StatusNotPending_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_OrderSummaryNotFound_ThrowsNotFoundException, processShipmentsAfterPaymentApprovalCash_ShipmentsNull_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipmentsEmpty_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ProductNotAvailable_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ProductStockInsufficient_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_PackageNotAvailable_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_PackageStockInsufficient_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_PaymentFailure_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_CredentialsMissing_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_DeliveryAddressMissing_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_PickupLocationNotFound_ThrowsNotFoundException, processShipmentsAfterPaymentApprovalCash_ShipRocketResponseNull_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipRocketResponseMessage_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipRocketMissingOrderId_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipRocketMissingShipmentId_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipRocketMissingStatus_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipRocketInvalidStatus_ThrowsBadRequestException
- Required order: processShipmentsAfterPaymentApprovalCash_ClientMismatch_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_CredentialsMissing_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_DeliveryAddressMissing_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_OrderSummaryNotFound_ThrowsNotFoundException, processShipmentsAfterPaymentApprovalCash_PackageNotAvailable_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_PackageStockInsufficient_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_PaymentFailure_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_PickupLocationNotFound_ThrowsNotFoundException, processShipmentsAfterPaymentApprovalCash_PoNotFound_ThrowsNotFoundException, processShipmentsAfterPaymentApprovalCash_ProductNotAvailable_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ProductStockInsufficient_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipmentsEmpty_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipmentsNull_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipRocketInvalidStatus_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipRocketMissingOrderId_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipRocketMissingShipmentId_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipRocketMissingStatus_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipRocketResponseMessage_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_ShipRocketResponseNull_ThrowsBadRequestException, processShipmentsAfterPaymentApprovalCash_StatusNotPending_ThrowsBadRequestException

REQUIRED FIXES SUMMARY:
- Fix Rule 5 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/CreateReturnTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Shipping
Class: class CreateReturnTest extends ShippingServiceTestBase {
Extends: ShippingServiceTestBase
Lines of Code: 814
Last Modified: 2026-02-10 23:56:34
Declared Test Count: 30 (first occurrence line 29)
Actual @Test Count: 30

VIOLATIONS FOUND:

VIOLATION 1: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 803 method `createReturn_VerifyPreAuthorizeAnnotation`
- Required rename: `createReturn_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 2: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: createReturn_FullReturn_Success, createReturn_PartialReturn_Success, createReturn_AwbAssignFailure_Success, createReturn_DeliveredDateNull_Success, createReturn_MultipleItems_Success
- Required order: createReturn_AwbAssignFailure_Success, createReturn_DeliveredDateNull_Success, createReturn_FullReturn_Success, createReturn_MultipleItems_Success, createReturn_PartialReturn_Success

VIOLATION 3: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: createReturn_ShipmentIdNull_ThrowsBadRequestException, createReturn_ProductsNull_ThrowsBadRequestException, createReturn_ProductsEmpty_ThrowsBadRequestException, createReturn_ShipmentNotFound_ThrowsNotFoundException, createReturn_NotDelivered_ThrowsBadRequestException, createReturn_CredentialsMissing_ThrowsBadRequestException, createReturn_ProductIdNull_ThrowsBadRequestException, createReturn_QuantityNull_ThrowsBadRequestException, createReturn_ReasonNull_ThrowsBadRequestException, createReturn_ReasonEmpty_ThrowsBadRequestException, createReturn_QuantityZero_ThrowsBadRequestException, createReturn_QuantityNegative_ThrowsBadRequestException, createReturn_ShipmentProductsNull_ThrowsBadRequestException, createReturn_ReturnWindowNull_ThrowsBadRequestException, createReturn_SecondItemReasonNull_ThrowsBadRequestException, createReturn_SecondItemQuantityNull_ThrowsBadRequestException, createReturn_SecondItemProductIdNull_ThrowsBadRequestException, createReturn_ProductNotInShipment_ThrowsBadRequestException, createReturn_QuantityExceeds_ThrowsBadRequestException, createReturn_ProductNotFound_ThrowsNotFoundException, createReturn_ProductNotReturnable_ThrowsBadRequestException, createReturn_PastReturnWindow_ThrowsBadRequestException, createReturn_ShipRocketFailure_ThrowsBadRequestException
- Required order: createReturn_CredentialsMissing_ThrowsBadRequestException, createReturn_NotDelivered_ThrowsBadRequestException, createReturn_PastReturnWindow_ThrowsBadRequestException, createReturn_ProductIdNull_ThrowsBadRequestException, createReturn_ProductNotFound_ThrowsNotFoundException, createReturn_ProductNotInShipment_ThrowsBadRequestException, createReturn_ProductNotReturnable_ThrowsBadRequestException, createReturn_ProductsEmpty_ThrowsBadRequestException, createReturn_ProductsNull_ThrowsBadRequestException, createReturn_QuantityExceeds_ThrowsBadRequestException, createReturn_QuantityNegative_ThrowsBadRequestException, createReturn_QuantityNull_ThrowsBadRequestException, createReturn_QuantityZero_ThrowsBadRequestException, createReturn_ReasonEmpty_ThrowsBadRequestException, createReturn_ReasonNull_ThrowsBadRequestException, createReturn_ReturnWindowNull_ThrowsBadRequestException, createReturn_SecondItemProductIdNull_ThrowsBadRequestException, createReturn_SecondItemQuantityNull_ThrowsBadRequestException, createReturn_SecondItemReasonNull_ThrowsBadRequestException, createReturn_ShipmentIdNull_ThrowsBadRequestException, createReturn_ShipmentNotFound_ThrowsNotFoundException, createReturn_ShipmentProductsNull_ThrowsBadRequestException, createReturn_ShipRocketFailure_ThrowsBadRequestException

REQUIRED FIXES SUMMARY:
- Fix Rule 5 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/OptimizeOrderTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Shipping
Class: class OptimizeOrderTest extends ShippingServiceTestBase {
Extends: ShippingServiceTestBase
Lines of Code: 308
Last Modified: 2026-02-10 23:56:34
Declared Test Count: 12 (first occurrence line 28)
Actual @Test Count: 12

VIOLATIONS FOUND:

VIOLATION 1: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 297 method `optimizeOrder_VerifyPreAuthorizeAnnotation`
- Required rename: `optimizeOrder_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 2: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: optimizeOrder_ValidRequest_Success, optimizeOrder_CustomAllocation_Success
- Required order: optimizeOrder_CustomAllocation_Success, optimizeOrder_ValidRequest_Success

VIOLATION 3: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: optimizeOrder_NullProductQuantities_Error, optimizeOrder_EmptyProductQuantities_Error, optimizeOrder_NullDeliveryPostcode_Error, optimizeOrder_EmptyDeliveryPostcode_Error, optimizeOrder_NoValidProducts_Error, optimizeOrder_MissingProduct_Error, optimizeOrder_NoPackagesConfigured_Error, optimizeOrder_Exception_Error
- Required order: optimizeOrder_EmptyDeliveryPostcode_Error, optimizeOrder_EmptyProductQuantities_Error, optimizeOrder_Exception_Error, optimizeOrder_MissingProduct_Error, optimizeOrder_NoPackagesConfigured_Error, optimizeOrder_NoValidProducts_Error, optimizeOrder_NullDeliveryPostcode_Error, optimizeOrder_NullProductQuantities_Error

REQUIRED FIXES SUMMARY:
- Fix Rule 5 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.


**IMPLEMENTATION PLAN (STEP-BY-STEP)**
1. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/CalculateShippingTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
2. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/CancelShipmentTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
3. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/ProcessShipmentsAfterPaymentApprovalOnlineTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
4. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/GetShipmentsInBatchesTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
5. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/ProcessShipmentsAfterPaymentApprovalCashTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
6. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/CreateReturnTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
7. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/OptimizeOrderTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
8. Create `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Shipping/ProcessShipmentsAfterPaymentApprovalTest.java` for service method `processShipmentsAfterPaymentApproval` (line 326 in `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/main/java/com/example/SpringApi/Services/ShippingService.java`). Include class header, `// Total Tests`, Success/Failure/Permission sections, and a controller permission test.
9. Resolve extra test file `ProcessShipmentsAfterPaymentApprovalOnlineTest.java` by renaming it to match a public method or removing it.
10. Resolve extra test file `ProcessShipmentsAfterPaymentApprovalCashTest.java` by renaming it to match a public method or removing it.

Verification Commands (run after fixes):
- mvn -Dtest=CalculateShippingTest test
- mvn -Dtest=CancelShipmentTest test
- mvn -Dtest=ProcessShipmentsAfterPaymentApprovalOnlineTest test
- mvn -Dtest=GetShipmentsInBatchesTest test
- mvn -Dtest=ProcessShipmentsAfterPaymentApprovalCashTest test
- mvn -Dtest=CreateReturnTest test