# UNIT TEST VERIFICATION REPORT — Promo

```
╔════════════════════════════════════════════════════════════╗
║           UNIT TEST VERIFICATION REPORT                    ║
║                                                            ║
║  Status: ❌ RED                                               ║
║  Services Analyzed: 1                                      ║
║  Public Methods Found: 6                                  ║
║  Test Files Expected: 6                                  ║
║  Test Files Found: 6                                     ║
║  Total Violations: 49                                    ║
╚════════════════════════════════════════════════════════════╝
```

VIOLATIONS BY RULE:

| Rule | Description | Count |
| --- | --- | --- |
| 2 | Test Count Declaration | 9 |
| 5 | Test Naming Convention | 6 |
| 7 | Exception Assertions | 5 |
| 10 | Test Ordering | 17 |
| 12 | Arrange/Act/Assert | 6 |
| 14 | No Inline Mocks | 6 |


**FILE-BY-FILE BREAKDOWN**

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Promo/CreatePromoTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Promo
Class: * Test class for PromoService.createPromo method.
Extends: None
Lines of Code: 960
Last Modified: 2026-02-10 20:20:42
Declared Test Count: 14 (first occurrence line 29)
Actual @Test Count: 52

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 29
- Current: 14
- Required: 52

VIOLATION 2: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Lines: 29, 282
- Required: Keep only the first declaration at the class start; remove duplicates.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 931 method `createPromo_ControllerHandlesBadRequest`
- Required rename: `createPromo_ControllerHandlesBadRequest_Success`
- Line: 949 method `createPromo_ControllerHandlesGenericException`
- Required rename: `createPromo_ControllerHandlesGenericException_Failure`

VIOLATION 4: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 537 in `createPromo_NullRequest_ThrowsBadRequestException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 5: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 297 inline mock in `createPromo_AllValid_Success`: `when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 299 inline mock in `createPromo_AllValid_Success`: `when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);`
- Required: Move to base test stub method and call stub in test.
- Line: 321 inline mock in `createPromo_FixedValueDiscount_Success`: `when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 340 inline mock in `createPromo_FutureStartDate_Success`: `when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 358 inline mock in `createPromo_LongDescription_Success`: `when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 376 inline mock in `createPromo_NoExpiryDate_Success`: `when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 395 inline mock in `createPromo_PromoCodeNormalization_Success`: `when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 413 inline mock in `createPromo_SmallCaseCode_Success`: `when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 432 inline mock in `createPromo_ValidCodeString_Success`: `when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 450 inline mock in `createPromo_ValidNumericCode_Success`: `when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 474 inline mock in `createPromo_DuplicateOverlappingPromo_ThrowsBadRequestException`: `when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 845 inline mock in `createPromo_StartDateFarPast_Success`: `when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 900 inline mock in `createPromo_ControllerDelegation_Success`: `doNothing().when(promoService).createPromo(any(PromoRequestModel.class));`
- Required: Move to base test stub method and call stub in test.
- Line: 933 inline mock in `createPromo_ControllerHandlesBadRequest`: `doThrow(new com.example.SpringApi.Exceptions.BadRequestException("Bad Request"))`
- Required: Move to base test stub method and call stub in test.
- Line: 934 inline mock in `createPromo_ControllerHandlesBadRequest`: `.when(promoService).createPromo(any());`
- Required: Move to base test stub method and call stub in test.
- Line: 951 inline mock in `createPromo_ControllerHandlesGenericException`: `doThrow(new RuntimeException("Server Error"))`
- Required: Move to base test stub method and call stub in test.
- Line: 952 inline mock in `createPromo_ControllerHandlesGenericException`: `.when(promoService).createPromo(any());`
- Required: Move to base test stub method and call stub in test.

VIOLATION 6: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 134 in `createPromo_DuplicateOverlappingPromo_ThrowsBadRequestException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 149 in `createPromo_EmptyDescription_ThrowsBadRequestException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 164 in `createPromo_NullRequest_ThrowsBadRequestException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 179 in `createPromo_EmptyPromoCode_ThrowsBadRequestException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 194 in `createPromo_DiscountValueZero_ThrowsBadRequestException`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 7: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: createPromo_AllValid_Success, createPromo_FixedValueDiscount_Success, createPromo_FutureStartDate_Success, createPromo_LongDescription_Success, createPromo_AllValid_Success, createPromo_FixedValueDiscount_Success, createPromo_FutureStartDate_Success, createPromo_LongDescription_Success, createPromo_NoExpiryDate_Success, createPromo_PromoCodeNormalization_Success, createPromo_SmallCaseCode_Success, createPromo_ValidCodeString_Success, createPromo_ValidNumericCode_Success
- Required order: createPromo_AllValid_Success, createPromo_AllValid_Success, createPromo_FixedValueDiscount_Success, createPromo_FixedValueDiscount_Success, createPromo_FutureStartDate_Success, createPromo_FutureStartDate_Success, createPromo_LongDescription_Success, createPromo_LongDescription_Success, createPromo_NoExpiryDate_Success, createPromo_PromoCodeNormalization_Success, createPromo_SmallCaseCode_Success, createPromo_ValidCodeString_Success, createPromo_ValidNumericCode_Success

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: createPromo_DuplicateOverlappingPromo_ThrowsBadRequestException, createPromo_EmptyDescription_ThrowsBadRequestException, createPromo_NullRequest_ThrowsBadRequestException, createPromo_EmptyPromoCode_ThrowsBadRequestException, createPromo_DiscountValueZero_ThrowsBadRequestException, createPromo_DuplicateOverlappingPromo_ThrowsBadRequestException, createPromo_EmptyDescription_ThrowsBadRequestException, createPromo_NullDescription_ThrowsBadRequestException, createPromo_NullDiscountValue_ThrowsBadRequestException, createPromo_NullRequest_ThrowsBadRequestException, createPromo_EmptyPromoCode_ThrowsBadRequestException, createPromo_NullPromoCode_ThrowsBadRequestException, createPromo_WhitespacePromoCode_ThrowsBadRequestException, createPromo_WhitespaceDescription_ThrowsBadRequestException, createPromo_DiscountValueNegative_ThrowsBadRequestException, createPromo_PercentageOver100_ThrowsBadRequestException, createPromo_ExpiryBeforeStart_ThrowsBadRequestException, createPromo_PromoCodeSpecialCharacters_ThrowsBadRequestException, createPromo_PromoCodeTooLong_ThrowsBadRequestException, createPromo_DescriptionTooLong_ThrowsBadRequestException, createPromo_NullStartDate_ThrowsBadRequestException, createPromo_InvalidCode_Tab, createPromo_InvalidCode_Newline, createPromo_InvalidDescription_Tab, createPromo_InvalidDescription_Newline, createPromo_DiscountValueZero_ThrowsBadRequestException, createPromo_ClientIdMismatch_ThrowsBadRequestException, createPromo_PromoCodeTooShort_ThrowsBadRequestException, createPromo_StartDateFarPast_Success, createPromo_NullDiscountFixed_ThrowsBadRequestException, createPromo_NegativeDiscountFixed_ThrowsBadRequestException
- Required order: createPromo_ClientIdMismatch_ThrowsBadRequestException, createPromo_DescriptionTooLong_ThrowsBadRequestException, createPromo_DiscountValueNegative_ThrowsBadRequestException, createPromo_DiscountValueZero_ThrowsBadRequestException, createPromo_DiscountValueZero_ThrowsBadRequestException, createPromo_DuplicateOverlappingPromo_ThrowsBadRequestException, createPromo_DuplicateOverlappingPromo_ThrowsBadRequestException, createPromo_EmptyDescription_ThrowsBadRequestException, createPromo_EmptyDescription_ThrowsBadRequestException, createPromo_EmptyPromoCode_ThrowsBadRequestException, createPromo_EmptyPromoCode_ThrowsBadRequestException, createPromo_ExpiryBeforeStart_ThrowsBadRequestException, createPromo_InvalidCode_Newline, createPromo_InvalidCode_Tab, createPromo_InvalidDescription_Newline, createPromo_InvalidDescription_Tab, createPromo_NegativeDiscountFixed_ThrowsBadRequestException, createPromo_NullDescription_ThrowsBadRequestException, createPromo_NullDiscountFixed_ThrowsBadRequestException, createPromo_NullDiscountValue_ThrowsBadRequestException, createPromo_NullPromoCode_ThrowsBadRequestException, createPromo_NullRequest_ThrowsBadRequestException, createPromo_NullRequest_ThrowsBadRequestException, createPromo_NullStartDate_ThrowsBadRequestException, createPromo_PercentageOver100_ThrowsBadRequestException, createPromo_PromoCodeSpecialCharacters_ThrowsBadRequestException, createPromo_PromoCodeTooLong_ThrowsBadRequestException, createPromo_PromoCodeTooShort_ThrowsBadRequestException, createPromo_StartDateFarPast_Success, createPromo_WhitespaceDescription_ThrowsBadRequestException, createPromo_WhitespacePromoCode_ThrowsBadRequestException

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section PERMISSION not alphabetical.
- Current order: createPromo_ControllerDelegation_Success, createPromo_controller_permission_unauthorized, createPromo_ControllerHandlesBadRequest_Success, createPromo_ControllerHandlesGenericException_Failure, createPromo_ControllerDelegation_Success, createPromo_controller_permission_unauthorized, createPromo_ControllerHandlesBadRequest, createPromo_ControllerHandlesGenericException
- Required order: createPromo_controller_permission_unauthorized, createPromo_controller_permission_unauthorized, createPromo_ControllerDelegation_Success, createPromo_ControllerDelegation_Success, createPromo_ControllerHandlesBadRequest, createPromo_ControllerHandlesBadRequest_Success, createPromo_ControllerHandlesGenericException, createPromo_ControllerHandlesGenericException_Failure

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 2 issues above.
- Fix Rule 5 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 7 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Promo/TogglePromoTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Promo
Class: * Test class for PromoService.togglePromo method.
Extends: None
Lines of Code: 644
Last Modified: 2026-02-10 20:20:42
Declared Test Count: 16 (first occurrence line 25)
Actual @Test Count: 32

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 25
- Current: 16
- Required: 32

VIOLATION 2: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Lines: 25, 331
- Required: Keep only the first declaration at the class start; remove duplicates.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 365 method `togglePromo_Success`
- Required rename: `togglePromo_Success_Success`
- Line: 616 method `togglePromo_ControllerHandlesNotFound`
- Required rename: `togglePromo_ControllerHandlesNotFound_Failure`
- Line: 633 method `togglePromo_ControllerHandlesException`
- Required rename: `togglePromo_ControllerHandlesException_Failure`

VIOLATION 4: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 148 in `togglePromo_InvalidId_999L` missing AAA comments: Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 163 in `togglePromo_InvalidId_MaxLong` missing AAA comments: Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 178 in `togglePromo_InvalidId_MinLong` missing AAA comments: Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 193 in `togglePromo_InvalidId_Negative100L` missing AAA comments: Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 208 in `togglePromo_NegativeId_ThrowsNotFoundException` missing AAA comments: Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 223 in `togglePromo_PromoNotFound_ThrowsNotFoundException` missing AAA comments: Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 238 in `togglePromo_ZeroId_ThrowsNotFoundException` missing AAA comments: Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 5: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 347 inline mock in `togglePromo_RestoreFromDeleted_Success`: `when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 349 inline mock in `togglePromo_RestoreFromDeleted_Success`: `when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);`
- Required: Move to base test stub method and call stub in test.
- Line: 367 inline mock in `togglePromo_Success`: `when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 369 inline mock in `togglePromo_Success`: `when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);`
- Required: Move to base test stub method and call stub in test.
- Line: 391 inline mock in `togglePromo_DoubleToggle_Success`: `when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 393 inline mock in `togglePromo_DoubleToggle_Success`: `when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);`
- Required: Move to base test stub method and call stub in test.
- Line: 415 inline mock in `togglePromo_Isolation_Success`: `lenient().when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 417 inline mock in `togglePromo_Isolation_Success`: `lenient().when(promoRepository.findByPromoIdAndClientId(999L, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 442 inline mock in `togglePromo_InvalidId_999L`: `when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 460 inline mock in `togglePromo_InvalidId_MaxLong`: `when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 478 inline mock in `togglePromo_InvalidId_MinLong`: `when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 495 inline mock in `togglePromo_InvalidId_Negative100L`: `when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 512 inline mock in `togglePromo_NegativeId_ThrowsNotFoundException`: `when(promoRepository.findByPromoIdAndClientId(negativeId, TEST_CLIENT_ID)).thenReturn(Optional.empty());`
- Required: Move to base test stub method and call stub in test.
- Line: 528 inline mock in `togglePromo_PromoNotFound_ThrowsNotFoundException`: `when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID)).thenReturn(Optional.empty());`
- Required: Move to base test stub method and call stub in test.
- Line: 547 inline mock in `togglePromo_ZeroId_ThrowsNotFoundException`: `when(promoRepository.findByPromoIdAndClientId(zeroId, TEST_CLIENT_ID)).thenReturn(Optional.empty());`
- Required: Move to base test stub method and call stub in test.
- Line: 564 inline mock in `togglePromo_DifferentClient_ThrowsNotFoundException`: `when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 585 inline mock in `togglePromo_WithValidId_DelegatesToService`: `doNothing().when(promoService).togglePromo(TEST_PROMO_ID);`
- Required: Move to base test stub method and call stub in test.
- Line: 618 inline mock in `togglePromo_ControllerHandlesNotFound`: `doThrow(new com.example.SpringApi.Exceptions.NotFoundException("Not Found"))`
- Required: Move to base test stub method and call stub in test.
- Line: 619 inline mock in `togglePromo_ControllerHandlesNotFound`: `.when(promoService).togglePromo(anyLong());`
- Required: Move to base test stub method and call stub in test.
- Line: 635 inline mock in `togglePromo_ControllerHandlesException`: `doThrow(new RuntimeException("Crash"))`
- Required: Move to base test stub method and call stub in test.
- Line: 636 inline mock in `togglePromo_ControllerHandlesException`: `.when(promoService).togglePromo(anyLong());`
- Required: Move to base test stub method and call stub in test.

VIOLATION 6: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 153 in `togglePromo_InvalidId_999L`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 168 in `togglePromo_InvalidId_MaxLong`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 183 in `togglePromo_InvalidId_MinLong`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 198 in `togglePromo_InvalidId_Negative100L`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 213 in `togglePromo_NegativeId_ThrowsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 228 in `togglePromo_PromoNotFound_ThrowsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 243 in `togglePromo_ZeroId_ThrowsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 568 in `togglePromo_DifferentClient_ThrowsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 7: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: togglePromo_DoubleToggle_Success, togglePromo_Isolation_Success, togglePromo_RestoreFromDeleted_Success, togglePromo_Success_Success, togglePromo_RestoreFromDeleted_Success, togglePromo_Success, togglePromo_DoubleToggle_Success, togglePromo_Isolation_Success
- Required order: togglePromo_DoubleToggle_Success, togglePromo_DoubleToggle_Success, togglePromo_Isolation_Success, togglePromo_Isolation_Success, togglePromo_RestoreFromDeleted_Success, togglePromo_RestoreFromDeleted_Success, togglePromo_Success, togglePromo_Success_Success

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: togglePromo_DifferentClient_ThrowsNotFoundException, togglePromo_InvalidId_999L, togglePromo_InvalidId_MaxLong, togglePromo_InvalidId_MinLong, togglePromo_InvalidId_Negative100L, togglePromo_NegativeId_ThrowsNotFoundException, togglePromo_PromoNotFound_ThrowsNotFoundException, togglePromo_ZeroId_ThrowsNotFoundException, togglePromo_InvalidId_999L, togglePromo_InvalidId_MaxLong, togglePromo_InvalidId_MinLong, togglePromo_InvalidId_Negative100L, togglePromo_NegativeId_ThrowsNotFoundException, togglePromo_PromoNotFound_ThrowsNotFoundException, togglePromo_ZeroId_ThrowsNotFoundException, togglePromo_DifferentClient_ThrowsNotFoundException
- Required order: togglePromo_DifferentClient_ThrowsNotFoundException, togglePromo_DifferentClient_ThrowsNotFoundException, togglePromo_InvalidId_999L, togglePromo_InvalidId_999L, togglePromo_InvalidId_MaxLong, togglePromo_InvalidId_MaxLong, togglePromo_InvalidId_MinLong, togglePromo_InvalidId_MinLong, togglePromo_InvalidId_Negative100L, togglePromo_InvalidId_Negative100L, togglePromo_NegativeId_ThrowsNotFoundException, togglePromo_NegativeId_ThrowsNotFoundException, togglePromo_PromoNotFound_ThrowsNotFoundException, togglePromo_PromoNotFound_ThrowsNotFoundException, togglePromo_ZeroId_ThrowsNotFoundException, togglePromo_ZeroId_ThrowsNotFoundException

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section PERMISSION not alphabetical.
- Current order: togglePromo_WithValidId_DelegatesToService, togglePromo_controller_permission_unauthorized, togglePromo_ControllerHandlesNotFound_Failure, togglePromo_ControllerHandlesException_Failure, togglePromo_WithValidId_DelegatesToService, togglePromo_controller_permission_unauthorized, togglePromo_ControllerHandlesNotFound, togglePromo_ControllerHandlesException
- Required order: togglePromo_controller_permission_unauthorized, togglePromo_controller_permission_unauthorized, togglePromo_ControllerHandlesException, togglePromo_ControllerHandlesException_Failure, togglePromo_ControllerHandlesNotFound, togglePromo_ControllerHandlesNotFound_Failure, togglePromo_WithValidId_DelegatesToService, togglePromo_WithValidId_DelegatesToService

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 2 issues above.
- Fix Rule 5 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 7 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Promo/GetPromoDetailsByNameTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Promo
Class: * Test class for PromoService.getPromoDetailsByName method.
Extends: None
Lines of Code: 353
Last Modified: 2026-02-10 19:34:12
Declared Test Count: 19 (first occurrence line 25)
Actual @Test Count: 19

VIOLATIONS FOUND:

VIOLATION 1: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 291 method `getPromoDetailsByName_ControllerHandlesNotFound`
- Required rename: `getPromoDetailsByName_ControllerHandlesNotFound_Failure`
- Line: 308 method `getPromoDetailsByName_ControllerHandlesBadRequest`
- Required rename: `getPromoDetailsByName_ControllerHandlesBadRequest_Success`
- Line: 325 method `getPromoDetailsByName_ContextRetrievalError`
- Required rename: `getPromoDetailsByName_ContextRetrievalError_Failure`
- Line: 342 method `getPromoDetailsByName_ControllerHandlesException`
- Required rename: `getPromoDetailsByName_ControllerHandlesException_Failure`

VIOLATION 2: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 156 in `getPromoDetailsByName_NullCode_ThrowsBadRequestException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 169 in `getPromoDetailsByName_EmptyCode_ThrowsBadRequestException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 180 in `getPromoDetailsByName_WhitespaceCode_ThrowsBadRequestException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 191 in `getPromoDetailsByName_TabCode_ThrowsBadRequestException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 3: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 41 inline mock in `getPromoDetailsByName_ValidCode_Success`: `when(promoRepository.findByPromoCodeAndClientId(TEST_PROMO_CODE, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 62 inline mock in `getPromoDetailsByName_LowercaseCode_Success`: `when(promoRepository.findByPromoCodeAndClientId(TEST_PROMO_CODE, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 81 inline mock in `getPromoDetailsByName_DeletedPromo_Success`: `when(promoRepository.findByPromoCodeAndClientId(TEST_PROMO_CODE, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 101 inline mock in `getPromoDetailsByName_NumericCode_Success`: `when(promoRepository.findByPromoCodeAndClientId(code, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 120 inline mock in `getPromoDetailsByName_MixedChars_Success`: `when(promoRepository.findByPromoCodeAndClientId(code, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 143 inline mock in `getPromoDetailsByName_DifferentClient_ThrowsNotFoundException`: `when(promoRepository.findByPromoCodeAndClientId(TEST_PROMO_CODE, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 205 inline mock in `getPromoDetailsByName_PromoCodeNotFound_INVALID`: `when(promoRepository.findByPromoCodeAndClientId(code, TEST_CLIENT_ID)).thenReturn(Optional.empty());`
- Required: Move to base test stub method and call stub in test.
- Line: 221 inline mock in `getPromoDetailsByName_PromoCodeNotFound_NOTFOUND`: `when(promoRepository.findByPromoCodeAndClientId(code, TEST_CLIENT_ID)).thenReturn(Optional.empty());`
- Required: Move to base test stub method and call stub in test.
- Line: 237 inline mock in `getPromoDetailsByName_NonExistentCode_ThrowsNotFoundException`: `when(promoRepository.findByPromoCodeAndClientId(code, TEST_CLIENT_ID)).thenReturn(Optional.empty());`
- Required: Move to base test stub method and call stub in test.
- Line: 259 inline mock in `getPromoDetailsByName_WithValidCode_DelegatesToService`: `doReturn(mockResponse).when(promoService).getPromoDetailsByName(TEST_PROMO_CODE);`
- Required: Move to base test stub method and call stub in test.
- Line: 293 inline mock in `getPromoDetailsByName_ControllerHandlesNotFound`: `doThrow(new com.example.SpringApi.Exceptions.NotFoundException("Not Found"))`
- Required: Move to base test stub method and call stub in test.
- Line: 294 inline mock in `getPromoDetailsByName_ControllerHandlesNotFound`: `.when(promoService).getPromoDetailsByName(anyString());`
- Required: Move to base test stub method and call stub in test.
- Line: 310 inline mock in `getPromoDetailsByName_ControllerHandlesBadRequest`: `doThrow(new com.example.SpringApi.Exceptions.BadRequestException("Empty name"))`
- Required: Move to base test stub method and call stub in test.
- Line: 311 inline mock in `getPromoDetailsByName_ControllerHandlesBadRequest`: `.when(promoService).getPromoDetailsByName(anyString());`
- Required: Move to base test stub method and call stub in test.
- Line: 327 inline mock in `getPromoDetailsByName_ContextRetrievalError`: `doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException("Context missing"))`
- Required: Move to base test stub method and call stub in test.
- Line: 328 inline mock in `getPromoDetailsByName_ContextRetrievalError`: `.when(promoService).getClientId();`
- Required: Move to base test stub method and call stub in test.
- Line: 344 inline mock in `getPromoDetailsByName_ControllerHandlesException`: `doThrow(new RuntimeException("Crash"))`
- Required: Move to base test stub method and call stub in test.
- Line: 345 inline mock in `getPromoDetailsByName_ControllerHandlesException`: `.when(promoService).getPromoDetailsByName(anyString());`
- Required: Move to base test stub method and call stub in test.

VIOLATION 4: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 147 in `getPromoDetailsByName_DifferentClient_ThrowsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 171 in `getPromoDetailsByName_EmptyCode_ThrowsBadRequestException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 182 in `getPromoDetailsByName_WhitespaceCode_ThrowsBadRequestException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 193 in `getPromoDetailsByName_TabCode_ThrowsBadRequestException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 240 in `getPromoDetailsByName_NonExistentCode_ThrowsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: getPromoDetailsByName_ValidCode_Success, getPromoDetailsByName_LowercaseCode_Success, getPromoDetailsByName_DeletedPromo_Success, getPromoDetailsByName_NumericCode_Success, getPromoDetailsByName_MixedChars_Success
- Required order: getPromoDetailsByName_DeletedPromo_Success, getPromoDetailsByName_LowercaseCode_Success, getPromoDetailsByName_MixedChars_Success, getPromoDetailsByName_NumericCode_Success, getPromoDetailsByName_ValidCode_Success

VIOLATION 6: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: getPromoDetailsByName_DifferentClient_ThrowsNotFoundException, getPromoDetailsByName_NullCode_ThrowsBadRequestException, getPromoDetailsByName_EmptyCode_ThrowsBadRequestException, getPromoDetailsByName_WhitespaceCode_ThrowsBadRequestException, getPromoDetailsByName_TabCode_ThrowsBadRequestException, getPromoDetailsByName_PromoCodeNotFound_INVALID, getPromoDetailsByName_PromoCodeNotFound_NOTFOUND, getPromoDetailsByName_NonExistentCode_ThrowsNotFoundException
- Required order: getPromoDetailsByName_DifferentClient_ThrowsNotFoundException, getPromoDetailsByName_EmptyCode_ThrowsBadRequestException, getPromoDetailsByName_NonExistentCode_ThrowsNotFoundException, getPromoDetailsByName_NullCode_ThrowsBadRequestException, getPromoDetailsByName_PromoCodeNotFound_INVALID, getPromoDetailsByName_PromoCodeNotFound_NOTFOUND, getPromoDetailsByName_TabCode_ThrowsBadRequestException, getPromoDetailsByName_WhitespaceCode_ThrowsBadRequestException

VIOLATION 7: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section PERMISSION not alphabetical.
- Current order: getPromoDetailsByName_WithValidCode_DelegatesToService, getPromoDetailsByName_controller_permission_unauthorized, getPromoDetailsByName_ControllerHandlesNotFound, getPromoDetailsByName_ControllerHandlesBadRequest, getPromoDetailsByName_ContextRetrievalError, getPromoDetailsByName_ControllerHandlesException
- Required order: getPromoDetailsByName_ContextRetrievalError, getPromoDetailsByName_controller_permission_unauthorized, getPromoDetailsByName_ControllerHandlesBadRequest, getPromoDetailsByName_ControllerHandlesException, getPromoDetailsByName_ControllerHandlesNotFound, getPromoDetailsByName_WithValidCode_DelegatesToService

REQUIRED FIXES SUMMARY:
- Fix Rule 5 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 7 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Promo/GetPromosInBatchesTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Promo
Class: * Test class for PromoService.getPromosInBatches method.
Extends: None
Lines of Code: 522
Last Modified: 2026-02-10 20:58:58
Declared Test Count: 10 (first occurrence line 29)
Actual @Test Count: 23

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 29
- Current: 10
- Required: 23

VIOLATION 2: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Lines: 29, 227
- Required: Keep only the first declaration at the class start; remove duplicates.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 494 method `getPromosInBatches_ControllerHandlesBadRequest`
- Required rename: `getPromosInBatches_ControllerHandlesBadRequest_Success`
- Line: 511 method `getPromosInBatches_ControllerHandlesException`
- Required rename: `getPromosInBatches_ControllerHandlesException_Failure`

VIOLATION 4: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 417 in `getPromosInBatches_NullRequest_ThrowsException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 5: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 243 inline mock in `getPromosInBatches_ValidRequest_Success`: `when(promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 267 inline mock in `getPromosInBatches_IncludeDeleted_Success`: `when(promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 289 inline mock in `getPromosInBatches_OrLogic_Success`: `when(promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 311 inline mock in `getPromosInBatches_LargePage_Success`: `when(promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 341 inline mock in `getPromosInBatches_MultipleFilters_Success`: `when(promoFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 462 inline mock in `getPromosInBatches_ControllerDelegation_Success`: `doReturn(mockResponse).when(promoService).getPromosInBatches(any(PaginationBaseRequestModel.class));`
- Required: Move to base test stub method and call stub in test.
- Line: 496 inline mock in `getPromosInBatches_ControllerHandlesBadRequest`: `doThrow(new com.example.SpringApi.Exceptions.BadRequestException("Bad Filter"))`
- Required: Move to base test stub method and call stub in test.
- Line: 497 inline mock in `getPromosInBatches_ControllerHandlesBadRequest`: `.when(promoService).getPromosInBatches(any());`
- Required: Move to base test stub method and call stub in test.
- Line: 513 inline mock in `getPromosInBatches_ControllerHandlesException`: `doThrow(new RuntimeException("Crash"))`
- Required: Move to base test stub method and call stub in test.
- Line: 514 inline mock in `getPromosInBatches_ControllerHandlesException`: `.when(promoService).getPromosInBatches(any());`
- Required: Move to base test stub method and call stub in test.

VIOLATION 6: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 444 in `getPromosInBatches_InvalidOperatorForType_ThrowsException`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 7: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: getPromosInBatches_ValidRequest_Success, getPromosInBatches_IncludeDeleted_Success, getPromosInBatches_ValidRequest_Success, getPromosInBatches_IncludeDeleted_Success, getPromosInBatches_OrLogic_Success, getPromosInBatches_LargePage_Success, getPromosInBatches_MultipleFilters_Success
- Required order: getPromosInBatches_IncludeDeleted_Success, getPromosInBatches_IncludeDeleted_Success, getPromosInBatches_LargePage_Success, getPromosInBatches_MultipleFilters_Success, getPromosInBatches_OrLogic_Success, getPromosInBatches_ValidRequest_Success, getPromosInBatches_ValidRequest_Success

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: getPromosInBatches_InvalidColumn_ThrowsBadRequestException, getPromosInBatches_NegativeStart_ThrowsBadRequestException, getPromosInBatches_NullRequest_ThrowsException, getPromosInBatches_InvalidColumn_ThrowsBadRequestException, getPromosInBatches_ZeroPageSize_ThrowsBadRequestException, getPromosInBatches_NegativeStart_ThrowsBadRequestException, getPromosInBatches_NullRequest_ThrowsException, getPromosInBatches_InvalidOperatorForType_ThrowsException
- Required order: getPromosInBatches_InvalidColumn_ThrowsBadRequestException, getPromosInBatches_InvalidColumn_ThrowsBadRequestException, getPromosInBatches_InvalidOperatorForType_ThrowsException, getPromosInBatches_NegativeStart_ThrowsBadRequestException, getPromosInBatches_NegativeStart_ThrowsBadRequestException, getPromosInBatches_NullRequest_ThrowsException, getPromosInBatches_NullRequest_ThrowsException, getPromosInBatches_ZeroPageSize_ThrowsBadRequestException

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section PERMISSION not alphabetical.
- Current order: getPromosInBatches_ControllerDelegation_Success, getPromosInBatches_controller_permission_unauthorized, getPromosInBatches_ControllerHandlesBadRequest_Success, getPromosInBatches_ControllerHandlesException_Failure, getPromosInBatches_ControllerDelegation_Success, getPromosInBatches_controller_permission_unauthorized, getPromosInBatches_ControllerHandlesBadRequest, getPromosInBatches_ControllerHandlesException
- Required order: getPromosInBatches_controller_permission_unauthorized, getPromosInBatches_controller_permission_unauthorized, getPromosInBatches_ControllerDelegation_Success, getPromosInBatches_ControllerDelegation_Success, getPromosInBatches_ControllerHandlesBadRequest, getPromosInBatches_ControllerHandlesBadRequest_Success, getPromosInBatches_ControllerHandlesException, getPromosInBatches_ControllerHandlesException_Failure

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 2 issues above.
- Fix Rule 5 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 7 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Promo/GetPromoDetailsByIdTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Promo
Class: * Test class for PromoService.getPromoDetailsById method.
Extends: None
Lines of Code: 527
Last Modified: 2026-02-10 20:20:42
Declared Test Count: 11 (first occurrence line 23)
Actual @Test Count: 26

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 23
- Current: 11
- Required: 26

VIOLATION 2: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Lines: 23, 207
- Required: Keep only the first declaration at the class start; remove duplicates.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 482 method `getPromoDetailsById_ControllerHandlesNotFound`
- Required rename: `getPromoDetailsById_ControllerHandlesNotFound_Failure`
- Line: 499 method `getPromoDetailsById_ControllerHandlesBadRequest`
- Required rename: `getPromoDetailsById_ControllerHandlesBadRequest_Success`
- Line: 516 method `getPromoDetailsById_ControllerHandlesException`
- Required rename: `getPromoDetailsById_ControllerHandlesException_Failure`

VIOLATION 4: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 81 in `getPromoDetailsById_InvalidId_Zero` missing AAA comments: Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 96 in `getPromoDetailsById_InvalidId_Negative` missing AAA comments: Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 5: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 223 inline mock in `getPromoDetailsById_ValidId_Success`: `when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 245 inline mock in `getPromoDetailsById_DeletedPromo_Success`: `when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 266 inline mock in `getPromoDetailsById_MaxLongId_Success`: `when(promoRepository.findByPromoIdAndClientId(maxId, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 284 inline mock in `getPromoDetailsById_InactivePromo_Success`: `when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 303 inline mock in `getPromoDetailsById_LargeId_Success`: `when(promoRepository.findByPromoIdAndClientId(largeId, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 328 inline mock in `getPromoDetailsById_DifferentClient_ThrowsNotFoundException`: `when(promoRepository.findByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 346 inline mock in `getPromoDetailsById_InvalidId_999L`: `when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID)).thenReturn(Optional.empty());`
- Required: Move to base test stub method and call stub in test.
- Line: 362 inline mock in `getPromoDetailsById_InvalidId_Zero`: `when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID)).thenReturn(Optional.empty());`
- Required: Move to base test stub method and call stub in test.
- Line: 379 inline mock in `getPromoDetailsById_InvalidId_Negative100L`: `when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID)).thenReturn(Optional.empty());`
- Required: Move to base test stub method and call stub in test.
- Line: 395 inline mock in `getPromoDetailsById_InvalidId_MinLong`: `when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID)).thenReturn(Optional.empty());`
- Required: Move to base test stub method and call stub in test.
- Line: 412 inline mock in `getPromoDetailsById_InvalidId_MaxLong_NotFound`: `when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID)).thenReturn(Optional.empty());`
- Required: Move to base test stub method and call stub in test.
- Line: 428 inline mock in `getPromoDetailsById_Negative1Id_ThrowsNotFoundException`: `when(promoRepository.findByPromoIdAndClientId(id, TEST_CLIENT_ID)).thenReturn(Optional.empty());`
- Required: Move to base test stub method and call stub in test.
- Line: 450 inline mock in `getPromoDetailsById_WithValidId_DelegatesToService`: `doReturn(mockResponse).when(promoService).getPromoDetailsById(TEST_PROMO_ID);`
- Required: Move to base test stub method and call stub in test.
- Line: 484 inline mock in `getPromoDetailsById_ControllerHandlesNotFound`: `doThrow(new com.example.SpringApi.Exceptions.NotFoundException("Not Found"))`
- Required: Move to base test stub method and call stub in test.
- Line: 485 inline mock in `getPromoDetailsById_ControllerHandlesNotFound`: `.when(promoService).getPromoDetailsById(anyLong());`
- Required: Move to base test stub method and call stub in test.
- Line: 501 inline mock in `getPromoDetailsById_ControllerHandlesBadRequest`: `doThrow(new com.example.SpringApi.Exceptions.BadRequestException("Bad ID"))`
- Required: Move to base test stub method and call stub in test.
- Line: 502 inline mock in `getPromoDetailsById_ControllerHandlesBadRequest`: `.when(promoService).getPromoDetailsById(anyLong());`
- Required: Move to base test stub method and call stub in test.
- Line: 518 inline mock in `getPromoDetailsById_ControllerHandlesException`: `doThrow(new RuntimeException("Crash"))`
- Required: Move to base test stub method and call stub in test.
- Line: 519 inline mock in `getPromoDetailsById_ControllerHandlesException`: `.when(promoService).getPromoDetailsById(anyLong());`
- Required: Move to base test stub method and call stub in test.

VIOLATION 6: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 86 in `getPromoDetailsById_InvalidId_Zero`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 101 in `getPromoDetailsById_InvalidId_Negative`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 431 in `getPromoDetailsById_Negative1Id_ThrowsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 7: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: getPromoDetailsById_ValidId_Success, getPromoDetailsById_ValidId_Success, getPromoDetailsById_DeletedPromo_Success, getPromoDetailsById_MaxLongId_Success, getPromoDetailsById_InactivePromo_Success, getPromoDetailsById_LargeId_Success
- Required order: getPromoDetailsById_DeletedPromo_Success, getPromoDetailsById_InactivePromo_Success, getPromoDetailsById_LargeId_Success, getPromoDetailsById_MaxLongId_Success, getPromoDetailsById_ValidId_Success, getPromoDetailsById_ValidId_Success

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: getPromoDetailsById_NotFound_ThrowsNotFoundException, getPromoDetailsById_InvalidId_Zero, getPromoDetailsById_InvalidId_Negative, getPromoDetailsById_DifferentClient_ThrowsNotFoundException, getPromoDetailsById_InvalidId_999L, getPromoDetailsById_InvalidId_Zero, getPromoDetailsById_InvalidId_Negative100L, getPromoDetailsById_InvalidId_MinLong, getPromoDetailsById_InvalidId_MaxLong_NotFound, getPromoDetailsById_Negative1Id_ThrowsNotFoundException
- Required order: getPromoDetailsById_DifferentClient_ThrowsNotFoundException, getPromoDetailsById_InvalidId_999L, getPromoDetailsById_InvalidId_MaxLong_NotFound, getPromoDetailsById_InvalidId_MinLong, getPromoDetailsById_InvalidId_Negative, getPromoDetailsById_InvalidId_Negative100L, getPromoDetailsById_InvalidId_Zero, getPromoDetailsById_InvalidId_Zero, getPromoDetailsById_Negative1Id_ThrowsNotFoundException, getPromoDetailsById_NotFound_ThrowsNotFoundException

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section PERMISSION not alphabetical.
- Current order: getPromoDetailsById_WithValidId_DelegatesToService, getPromoDetailsById_controller_permission_unauthorized, getPromoDetailsById_ControllerHandlesNotFound_Failure, getPromoDetailsById_ControllerHandlesBadRequest_Success, getPromoDetailsById_ControllerHandlesException_Failure, getPromoDetailsById_WithValidId_DelegatesToService, getPromoDetailsById_controller_permission_unauthorized, getPromoDetailsById_ControllerHandlesNotFound, getPromoDetailsById_ControllerHandlesBadRequest, getPromoDetailsById_ControllerHandlesException
- Required order: getPromoDetailsById_controller_permission_unauthorized, getPromoDetailsById_controller_permission_unauthorized, getPromoDetailsById_ControllerHandlesBadRequest, getPromoDetailsById_ControllerHandlesBadRequest_Success, getPromoDetailsById_ControllerHandlesException, getPromoDetailsById_ControllerHandlesException_Failure, getPromoDetailsById_ControllerHandlesNotFound, getPromoDetailsById_ControllerHandlesNotFound_Failure, getPromoDetailsById_WithValidId_DelegatesToService, getPromoDetailsById_WithValidId_DelegatesToService

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 2 issues above.
- Fix Rule 5 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 7 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Promo/BulkCreatePromosAsyncTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Promo
Class: * Test class for PromoService.bulkCreatePromosAsync method.
Extends: None
Lines of Code: 583
Last Modified: 2026-02-10 00:29:33
Declared Test Count: 22 (first occurrence line 30)
Actual @Test Count: 21

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 30
- Current: 22
- Required: 21

VIOLATION 2: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 427 method `bulkCreatePromosAsync_MixedValidity`
- Required rename: `bulkCreatePromosAsync_MixedValidity_Success`
- Line: 501 method `bulkCreatePromosAsync_ControllerHandlesBadRequest`
- Required rename: `bulkCreatePromosAsync_ControllerHandlesBadRequest_Success`
- Line: 519 method `bulkCreatePromosAsync_ControllerHandlesInternalError`
- Required rename: `bulkCreatePromosAsync_ControllerHandlesInternalError_Failure`
- Line: 537 method `bulkCreatePromosAsync_ContextRetrievalError`
- Required rename: `bulkCreatePromosAsync_ContextRetrievalError_Failure`
- Line: 555 method `bulkCreatePromosAsync_ControllerWithEmptyList`
- Required rename: `bulkCreatePromosAsync_ControllerWithEmptyList_Success`
- Line: 572 method `bulkCreatePromosAsync_ControllerWithNullList`
- Required rename: `bulkCreatePromosAsync_ControllerWithNullList_Success`

VIOLATION 3: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 307 in `bulkCreatePromosAsync_NullList_CapturesFailure` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 4: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 59 inline mock in `bulkCreatePromosAsync_AllValid_Success`: `lenient().when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 61 inline mock in `bulkCreatePromosAsync_AllValid_Success`: `lenient().when(promoRepository.findByPromoCodeAndClientId(anyString(), eq(TEST_CLIENT_ID)))`
- Required: Move to base test stub method and call stub in test.
- Line: 67 inline mock in `bulkCreatePromosAsync_AllValid_Success`: `when(promoRepository.save(any(Promo.class))).thenAnswer(invocation -> {`
- Required: Move to base test stub method and call stub in test.
- Line: 119 inline mock in `bulkCreatePromosAsync_LargeBatch_Success`: `lenient().when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 121 inline mock in `bulkCreatePromosAsync_LargeBatch_Success`: `when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);`
- Required: Move to base test stub method and call stub in test.
- Line: 153 inline mock in `bulkCreatePromosAsync_PerformanceTest_Success`: `lenient().when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 155 inline mock in `bulkCreatePromosAsync_PerformanceTest_Success`: `when(promoRepository.save(any(Promo.class))).thenReturn(testPromo);`
- Required: Move to base test stub method and call stub in test.
- Line: 174 inline mock in `bulkCreatePromosAsync_Single_Success`: `lenient().when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 226 inline mock in `bulkCreatePromosAsync_DatabaseError_CapturesFailure`: `lenient().when(promoRepository.findByPromoCodeAndClientId(anyString(), eq(TEST_CLIENT_ID)))`
- Required: Move to base test stub method and call stub in test.
- Line: 229 inline mock in `bulkCreatePromosAsync_DatabaseError_CapturesFailure`: `lenient().when(promoRepository.save(any(Promo.class)))`
- Required: Move to base test stub method and call stub in test.
- Line: 265 inline mock in `bulkCreatePromosAsync_Overlapping_PartialSuccess`: `when(promoRepository.findOverlappingPromos(eq("P11"), anyLong(), any(), any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 267 inline mock in `bulkCreatePromosAsync_Overlapping_PartialSuccess`: `when(promoRepository.findOverlappingPromos(eq("P22"), anyLong(), any(), any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 289 inline mock in `bulkCreatePromosAsync_DuplicatePromoCode_PartialSuccess`: `when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 343 inline mock in `bulkCreatePromosAsync_PartialFailure_InvalidDateRange`: `lenient().when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 372 inline mock in `bulkCreatePromosAsync_SomeFailValidation_PartialSuccess`: `lenient().when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 403 inline mock in `bulkCreatePromosAsync_PartialFailure_Reporting_Success`: `when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 405 inline mock in `bulkCreatePromosAsync_PartialFailure_Reporting_Success`: `when(promoRepository.save(any(Promo.class))).thenAnswer(invocation -> invocation.getArgument(0));`
- Required: Move to base test stub method and call stub in test.
- Line: 408 inline mock in `bulkCreatePromosAsync_PartialFailure_Reporting_Success`: `when(promoRepository.findByPromoCodeAndClientId(eq("SUCCESS"), eq(TEST_CLIENT_ID)))`
- Required: Move to base test stub method and call stub in test.
- Line: 440 inline mock in `bulkCreatePromosAsync_MixedValidity`: `lenient().when(promoRepository.findOverlappingPromos(anyString(), anyLong(), any(), any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 469 inline mock in `bulkCreatePromosAsync_ControllerDelegation_Success`: `doNothing().when(promoService).bulkCreatePromosAsync(anyList(), anyLong(), anyString(), anyLong());`
- Required: Move to base test stub method and call stub in test.
- Line: 504 inline mock in `bulkCreatePromosAsync_ControllerHandlesBadRequest`: `doThrow(new com.example.SpringApi.Exceptions.BadRequestException("Empty list"))`
- Required: Move to base test stub method and call stub in test.
- Line: 505 inline mock in `bulkCreatePromosAsync_ControllerHandlesBadRequest`: `.when(promoService).bulkCreatePromosAsync(any(), anyLong(), anyString(), anyLong());`
- Required: Move to base test stub method and call stub in test.
- Line: 522 inline mock in `bulkCreatePromosAsync_ControllerHandlesInternalError`: `doThrow(new RuntimeException("Critical failure"))`
- Required: Move to base test stub method and call stub in test.
- Line: 523 inline mock in `bulkCreatePromosAsync_ControllerHandlesInternalError`: `.when(promoService).bulkCreatePromosAsync(any(), anyLong(), anyString(), anyLong());`
- Required: Move to base test stub method and call stub in test.
- Line: 539 inline mock in `bulkCreatePromosAsync_ContextRetrievalError`: `doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException("Context missing"))`
- Required: Move to base test stub method and call stub in test.
- Line: 540 inline mock in `bulkCreatePromosAsync_ContextRetrievalError`: `.when(promoService).getUserId();`
- Required: Move to base test stub method and call stub in test.
- Line: 558 inline mock in `bulkCreatePromosAsync_ControllerWithEmptyList`: `doNothing().when(promoService).bulkCreatePromosAsync(any(), anyLong(), anyString(), anyLong());`
- Required: Move to base test stub method and call stub in test.
- Line: 574 inline mock in `bulkCreatePromosAsync_ControllerWithNullList`: `doThrow(new com.example.SpringApi.Exceptions.BadRequestException("Null list"))`
- Required: Move to base test stub method and call stub in test.
- Line: 575 inline mock in `bulkCreatePromosAsync_ControllerWithNullList`: `.when(promoService).bulkCreatePromosAsync(isNull(), anyLong(), anyString(), anyLong());`
- Required: Move to base test stub method and call stub in test.

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: bulkCreatePromosAsync_AllInvalid_NoSaves, bulkCreatePromosAsync_DatabaseError_CapturesFailure, bulkCreatePromosAsync_Overlapping_PartialSuccess, bulkCreatePromosAsync_DuplicatePromoCode_PartialSuccess, bulkCreatePromosAsync_NullList_CapturesFailure, bulkCreatePromosAsync_PartialFailure_InvalidDateRange, bulkCreatePromosAsync_SomeFailValidation_PartialSuccess, bulkCreatePromosAsync_PartialFailure_Reporting_Success, bulkCreatePromosAsync_MixedValidity
- Required order: bulkCreatePromosAsync_AllInvalid_NoSaves, bulkCreatePromosAsync_DatabaseError_CapturesFailure, bulkCreatePromosAsync_DuplicatePromoCode_PartialSuccess, bulkCreatePromosAsync_MixedValidity, bulkCreatePromosAsync_NullList_CapturesFailure, bulkCreatePromosAsync_Overlapping_PartialSuccess, bulkCreatePromosAsync_PartialFailure_InvalidDateRange, bulkCreatePromosAsync_PartialFailure_Reporting_Success, bulkCreatePromosAsync_SomeFailValidation_PartialSuccess

VIOLATION 6: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section PERMISSION not alphabetical.
- Current order: bulkCreatePromosAsync_ControllerDelegation_Success, bulkCreatePromosAsync_controller_permission_unauthorized, bulkCreatePromosAsync_ControllerHandlesBadRequest, bulkCreatePromosAsync_ControllerHandlesInternalError, bulkCreatePromosAsync_ContextRetrievalError, bulkCreatePromosAsync_ControllerWithEmptyList, bulkCreatePromosAsync_ControllerWithNullList
- Required order: bulkCreatePromosAsync_ContextRetrievalError, bulkCreatePromosAsync_controller_permission_unauthorized, bulkCreatePromosAsync_ControllerDelegation_Success, bulkCreatePromosAsync_ControllerHandlesBadRequest, bulkCreatePromosAsync_ControllerHandlesInternalError, bulkCreatePromosAsync_ControllerWithEmptyList, bulkCreatePromosAsync_ControllerWithNullList

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 5 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.


**IMPLEMENTATION PLAN (STEP-BY-STEP)**
1. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Promo/CreatePromoTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
2. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Promo/TogglePromoTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
3. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Promo/GetPromoDetailsByNameTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
4. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Promo/GetPromosInBatchesTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
5. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Promo/GetPromoDetailsByIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
6. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Promo/BulkCreatePromosAsyncTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.

Verification Commands (run after fixes):
- mvn -Dtest=CreatePromoTest test
- mvn -Dtest=TogglePromoTest test
- mvn -Dtest=GetPromoDetailsByNameTest test
- mvn -Dtest=GetPromosInBatchesTest test
- mvn -Dtest=GetPromoDetailsByIdTest test
- mvn -Dtest=BulkCreatePromosAsyncTest test