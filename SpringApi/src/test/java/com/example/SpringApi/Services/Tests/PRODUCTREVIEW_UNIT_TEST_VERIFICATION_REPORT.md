# UNIT TEST VERIFICATION REPORT — ProductReview

```
╔════════════════════════════════════════════════════════════╗
║           UNIT TEST VERIFICATION REPORT                    ║
║                                                            ║
║  Status: ❌ RED                                               ║
║  Services Analyzed: 1                                      ║
║  Public Methods Found: 4                                  ║
║  Test Files Expected: 4                                  ║
║  Test Files Found: 5                                     ║
║  Total Violations: 37                                    ║
╚════════════════════════════════════════════════════════════╝
```

VIOLATIONS BY RULE:

| Rule | Description | Count |
| --- | --- | --- |
| 1 | One Test File per Method | 1 |
| 2 | Test Count Declaration | 8 |
| 3 | Controller Permission Test | 1 |
| 5 | Test Naming Convention | 3 |
| 6 | Centralized Mocking | 4 |
| 8 | Error Constants | 1 |
| 9 | Test Documentation | 4 |
| 10 | Test Ordering | 9 |
| 12 | Arrange/Act/Assert | 1 |
| 14 | No Inline Mocks | 5 |


**MISSING/EXTRA TEST FILES (RULE 1)**
Extra test file with no matching public method: `GetProductReviewsInBatchesTest.java`. Either rename it to match a public method or remove it.


**FILE-BY-FILE BREAKDOWN**

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/ProductReview/GetProductReviewsInBatchesGivenProductIdTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.ProductReview
Class: * Test class for ProductReviewService.getProductReviewsInBatchesGivenProductId method.
Extends: None
Lines of Code: 343
Last Modified: 2026-02-10 20:20:42
Declared Test Count: 22 (first occurrence line 32)
Actual @Test Count: 14

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 32
- Current: 22
- Required: 14

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 313 has mock usage `IProductReviewSubTranslator serviceMock = mock(IProductReviewSubTranslator.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 47 method `getProductReviewsInBatches_Success_Success`
- Required rename: `getProductReviewsInBatchesGivenProductId_Success_Success`
- Line: 70 method `getProductReviewsInBatches_EmptyResults_Success`
- Required rename: `getProductReviewsInBatchesGivenProductId_EmptyResults_Success`
- Line: 92 method `getProductReviewsInBatches_SingleItem_Success`
- Required rename: `getProductReviewsInBatchesGivenProductId_SingleItem_Success`
- Line: 114 method `getProductReviewsInBatches_LargePage_Success`
- Required rename: `getProductReviewsInBatchesGivenProductId_LargePage_Success`
- Line: 136 method `getProductReviewsInBatches_MiddlePage_Success`
- Required rename: `getProductReviewsInBatchesGivenProductId_MiddlePage_Success`
- Line: 158 method `getProductReviewsInBatches_ProductIdOne_Success`
- Required rename: `getProductReviewsInBatchesGivenProductId_ProductIdOne_Success`
- Line: 178 method `getProductReviewsInBatches_ProductIdLarge_Success`
- Required rename: `getProductReviewsInBatchesGivenProductId_ProductIdLarge_Success`
- Line: 198 method `getProductReviewsInBatches_MultipleResults_Success`
- Required rename: `getProductReviewsInBatchesGivenProductId_MultipleResults_Success`
- Line: 229 method `getProductReviewsInBatches_InvalidPagination_ThrowsBadRequestException`
- Required rename: `getProductReviewsInBatchesGivenProductId_InvalidPagination_ThrowsBadRequestException`
- Line: 248 method `getProductReviewsInBatches_ZeroPageSize_ThrowsBadRequestException`
- Required rename: `getProductReviewsInBatchesGivenProductId_ZeroPageSize_ThrowsBadRequestException`
- Line: 267 method `getProductReviewsInBatches_EqualStartEnd_ThrowsBadRequestException`
- Required rename: `getProductReviewsInBatchesGivenProductId_EqualStartEnd_ThrowsBadRequestException`
- Line: 286 method `getProductReviewsInBatches_ReversedPagination_ThrowsBadRequestException`
- Required rename: `getProductReviewsInBatchesGivenProductId_ReversedPagination_ThrowsBadRequestException`

VIOLATION 4: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 315 inline mock in `getProductReviewsInBatchesGivenProductId_controller_permission_unauthorized`: `doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 316 inline mock in `getProductReviewsInBatchesGivenProductId_controller_permission_unauthorized`: `.when(serviceMock).getProductReviewsInBatchesGivenProductId(any(), anyLong());`
- Required: Move to base test stub method and call stub in test.

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: getProductReviewsInBatches_Success_Success, getProductReviewsInBatches_EmptyResults_Success, getProductReviewsInBatches_SingleItem_Success, getProductReviewsInBatches_LargePage_Success, getProductReviewsInBatches_MiddlePage_Success, getProductReviewsInBatches_ProductIdOne_Success, getProductReviewsInBatches_ProductIdLarge_Success, getProductReviewsInBatches_MultipleResults_Success
- Required order: getProductReviewsInBatches_EmptyResults_Success, getProductReviewsInBatches_LargePage_Success, getProductReviewsInBatches_MiddlePage_Success, getProductReviewsInBatches_MultipleResults_Success, getProductReviewsInBatches_ProductIdLarge_Success, getProductReviewsInBatches_ProductIdOne_Success, getProductReviewsInBatches_SingleItem_Success, getProductReviewsInBatches_Success_Success

VIOLATION 6: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: getProductReviewsInBatches_InvalidPagination_ThrowsBadRequestException, getProductReviewsInBatches_ZeroPageSize_ThrowsBadRequestException, getProductReviewsInBatches_EqualStartEnd_ThrowsBadRequestException, getProductReviewsInBatches_ReversedPagination_ThrowsBadRequestException
- Required order: getProductReviewsInBatches_EqualStartEnd_ThrowsBadRequestException, getProductReviewsInBatches_InvalidPagination_ThrowsBadRequestException, getProductReviewsInBatches_ReversedPagination_ThrowsBadRequestException, getProductReviewsInBatches_ZeroPageSize_ThrowsBadRequestException

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 5 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/ProductReview/SetProductReviewScoreTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.ProductReview
Class: * Test class for ProductReviewService.setProductReviewScore method.
Extends: None
Lines of Code: 927
Last Modified: 2026-02-10 20:20:42
Declared Test Count: 26 (first occurrence line 31)
Actual @Test Count: 50

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 31
- Current: 26
- Required: 50

VIOLATION 2: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Lines: 31, 559
- Required: Keep only the first declaration at the class start; remove duplicates.

VIOLATION 3: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 517 has mock usage `IProductReviewSubTranslator serviceMock = mock(IProductReviewSubTranslator.class);`
- Required: Move mocks to base test file.

VIOLATION 4: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: setProductReviewScore_Success_DecreaseToZero (line 584), setProductReviewScore_Success_IncreaseScore (line 601), setProductReviewScore_Success_NullScore (line 618), setProductReviewScore_NegativeId_ThrowsNotFoundException (line 651), setProductReviewScore_ReviewNotFound_ThrowsNotFoundException (line 663), setProductReviewScore_ZeroId_ThrowsNotFoundException (line 679), setProductReviewScore_DecreaseFrom1To0_Success (line 691), setProductReviewScore_DecreaseWhenNullScore_Stays0 (line 706), setProductReviewScore_IncreaseFrom0To1_Success (line 721), setProductReviewScore_IncreaseFrom10To11_Success (line 736), setProductReviewScore_IdNegative5_ThrowsNotFoundException (line 752), setProductReviewScore_Id2_ThrowsNotFoundException (line 764), setProductReviewScore_Id99_ThrowsNotFoundException (line 776), setProductReviewScore_IdMinLong_ThrowsNotFoundException (line 788), setProductReviewScore_IdMaxLong_ThrowsNotFoundException (line 800), setProductReviewScore_IncreaseFromLargeScore_Success (line 816), setProductReviewScore_IncreaseFromVeryLargeScore_Success (line 832), setProductReviewScore_DecreaseFromLargeScore_Success (line 848), setProductReviewScore_DecreaseNearZero_Success (line 864), setProductReviewScore_DecreaseWhenAlreadyZero_StaysZero (line 880), setProductReviewScore_IncreaseWhenNullScore_SetsToOne (line 896), setProductReviewScore_IncreaseFromMidRange_Success (line 912)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 5: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 519 inline mock in `setProductReviewScore_controller_permission_unauthorized`: `doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 520 inline mock in `setProductReviewScore_controller_permission_unauthorized`: `.when(serviceMock).setProductReviewScore(anyLong(), anyBoolean());`
- Required: Move to base test stub method and call stub in test.
- Line: 741 inline mock in `setProductReviewScore_IncreaseFrom10To11_Success`: `when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 743 inline mock in `setProductReviewScore_IncreaseFrom10To11_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 756 inline mock in `setProductReviewScore_IdNegative5_ThrowsNotFoundException`: `when(productReviewRepository.findByReviewIdAndClientId(-5L, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 768 inline mock in `setProductReviewScore_Id2_ThrowsNotFoundException`: `when(productReviewRepository.findByReviewIdAndClientId(2L, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 780 inline mock in `setProductReviewScore_Id99_ThrowsNotFoundException`: `when(productReviewRepository.findByReviewIdAndClientId(99L, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 792 inline mock in `setProductReviewScore_IdMinLong_ThrowsNotFoundException`: `when(productReviewRepository.findByReviewIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 804 inline mock in `setProductReviewScore_IdMaxLong_ThrowsNotFoundException`: `when(productReviewRepository.findByReviewIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 821 inline mock in `setProductReviewScore_IncreaseFromLargeScore_Success`: `when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 823 inline mock in `setProductReviewScore_IncreaseFromLargeScore_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 837 inline mock in `setProductReviewScore_IncreaseFromVeryLargeScore_Success`: `when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 839 inline mock in `setProductReviewScore_IncreaseFromVeryLargeScore_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 853 inline mock in `setProductReviewScore_DecreaseFromLargeScore_Success`: `when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 855 inline mock in `setProductReviewScore_DecreaseFromLargeScore_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 869 inline mock in `setProductReviewScore_DecreaseNearZero_Success`: `when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 871 inline mock in `setProductReviewScore_DecreaseNearZero_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 885 inline mock in `setProductReviewScore_DecreaseWhenAlreadyZero_StaysZero`: `when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 887 inline mock in `setProductReviewScore_DecreaseWhenAlreadyZero_StaysZero`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 901 inline mock in `setProductReviewScore_IncreaseWhenNullScore_SetsToOne`: `when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 903 inline mock in `setProductReviewScore_IncreaseWhenNullScore_SetsToOne`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 917 inline mock in `setProductReviewScore_IncreaseFromMidRange_Success`: `when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 919 inline mock in `setProductReviewScore_IncreaseFromMidRange_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 6: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: setProductReviewScore_Success_DecreaseScore, setProductReviewScore_Success_DecreaseToZero, setProductReviewScore_Success_IncreaseScore, setProductReviewScore_Success_NullScore, setProductReviewScore_DecreaseFrom1To0_Success, setProductReviewScore_DecreaseWhenNullScore_Stays0, setProductReviewScore_IncreaseFrom0To1_Success, setProductReviewScore_IncreaseFrom10To11_Success, setProductReviewScore_IncreaseFromLargeScore_Success, setProductReviewScore_IncreaseFromVeryLargeScore_Success, setProductReviewScore_DecreaseFromLargeScore_Success, setProductReviewScore_DecreaseNearZero_Success, setProductReviewScore_DecreaseWhenAlreadyZero_StaysZero, setProductReviewScore_IncreaseWhenNullScore_SetsToOne, setProductReviewScore_IncreaseFromMidRange_Success, setProductReviewScore_Success_DecreaseScore, setProductReviewScore_Success_DecreaseToZero, setProductReviewScore_Success_IncreaseScore, setProductReviewScore_Success_NullScore
- Required order: setProductReviewScore_DecreaseFrom1To0_Success, setProductReviewScore_DecreaseFromLargeScore_Success, setProductReviewScore_DecreaseNearZero_Success, setProductReviewScore_DecreaseWhenAlreadyZero_StaysZero, setProductReviewScore_DecreaseWhenNullScore_Stays0, setProductReviewScore_IncreaseFrom0To1_Success, setProductReviewScore_IncreaseFrom10To11_Success, setProductReviewScore_IncreaseFromLargeScore_Success, setProductReviewScore_IncreaseFromMidRange_Success, setProductReviewScore_IncreaseFromVeryLargeScore_Success, setProductReviewScore_IncreaseWhenNullScore_SetsToOne, setProductReviewScore_Success_DecreaseScore, setProductReviewScore_Success_DecreaseScore, setProductReviewScore_Success_DecreaseToZero, setProductReviewScore_Success_DecreaseToZero, setProductReviewScore_Success_IncreaseScore, setProductReviewScore_Success_IncreaseScore, setProductReviewScore_Success_NullScore, setProductReviewScore_Success_NullScore

VIOLATION 7: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: setProductReviewScore_MaxLongId_ThrowsNotFoundException, setProductReviewScore_NegativeId_ThrowsNotFoundException, setProductReviewScore_ReviewNotFound_ThrowsNotFoundException, setProductReviewScore_ZeroId_ThrowsNotFoundException, setProductReviewScore_IdNegative5_ThrowsNotFoundException, setProductReviewScore_Id2_ThrowsNotFoundException, setProductReviewScore_Id99_ThrowsNotFoundException, setProductReviewScore_IdMinLong_ThrowsNotFoundException, setProductReviewScore_IdMaxLong_ThrowsNotFoundException, setProductReviewScore_MaxLongId_ThrowsNotFoundException, setProductReviewScore_NegativeId_ThrowsNotFoundException, setProductReviewScore_ReviewNotFound_ThrowsNotFoundException, setProductReviewScore_ZeroId_ThrowsNotFoundException, setProductReviewScore_DecreaseFrom1To0_Success, setProductReviewScore_DecreaseWhenNullScore_Stays0, setProductReviewScore_IncreaseFrom0To1_Success, setProductReviewScore_IncreaseFrom10To11_Success, setProductReviewScore_IdNegative5_ThrowsNotFoundException, setProductReviewScore_Id2_ThrowsNotFoundException, setProductReviewScore_Id99_ThrowsNotFoundException, setProductReviewScore_IdMinLong_ThrowsNotFoundException, setProductReviewScore_IdMaxLong_ThrowsNotFoundException, setProductReviewScore_IncreaseFromLargeScore_Success, setProductReviewScore_IncreaseFromVeryLargeScore_Success, setProductReviewScore_DecreaseFromLargeScore_Success, setProductReviewScore_DecreaseNearZero_Success, setProductReviewScore_DecreaseWhenAlreadyZero_StaysZero, setProductReviewScore_IncreaseWhenNullScore_SetsToOne, setProductReviewScore_IncreaseFromMidRange_Success
- Required order: setProductReviewScore_DecreaseFrom1To0_Success, setProductReviewScore_DecreaseFromLargeScore_Success, setProductReviewScore_DecreaseNearZero_Success, setProductReviewScore_DecreaseWhenAlreadyZero_StaysZero, setProductReviewScore_DecreaseWhenNullScore_Stays0, setProductReviewScore_Id2_ThrowsNotFoundException, setProductReviewScore_Id2_ThrowsNotFoundException, setProductReviewScore_Id99_ThrowsNotFoundException, setProductReviewScore_Id99_ThrowsNotFoundException, setProductReviewScore_IdMaxLong_ThrowsNotFoundException, setProductReviewScore_IdMaxLong_ThrowsNotFoundException, setProductReviewScore_IdMinLong_ThrowsNotFoundException, setProductReviewScore_IdMinLong_ThrowsNotFoundException, setProductReviewScore_IdNegative5_ThrowsNotFoundException, setProductReviewScore_IdNegative5_ThrowsNotFoundException, setProductReviewScore_IncreaseFrom0To1_Success, setProductReviewScore_IncreaseFrom10To11_Success, setProductReviewScore_IncreaseFromLargeScore_Success, setProductReviewScore_IncreaseFromMidRange_Success, setProductReviewScore_IncreaseFromVeryLargeScore_Success, setProductReviewScore_IncreaseWhenNullScore_SetsToOne, setProductReviewScore_MaxLongId_ThrowsNotFoundException, setProductReviewScore_MaxLongId_ThrowsNotFoundException, setProductReviewScore_NegativeId_ThrowsNotFoundException, setProductReviewScore_NegativeId_ThrowsNotFoundException, setProductReviewScore_ReviewNotFound_ThrowsNotFoundException, setProductReviewScore_ReviewNotFound_ThrowsNotFoundException, setProductReviewScore_ZeroId_ThrowsNotFoundException, setProductReviewScore_ZeroId_ThrowsNotFoundException

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 9 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/ProductReview/ToggleProductReviewTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.ProductReview
Class: * Test class for ProductReviewService.toggleProductReview method.
Extends: None
Lines of Code: 512
Last Modified: 2026-02-10 20:20:42
Declared Test Count: 14 (first occurrence line 26)
Actual @Test Count: 26

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 26
- Current: 14
- Required: 26

VIOLATION 2: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Lines: 26, 329
- Required: Keep only the first declaration at the class start; remove duplicates.

VIOLATION 3: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 288 has mock usage `IProductReviewSubTranslator serviceMock = mock(IProductReviewSubTranslator.class);`
- Required: Move mocks to base test file.

VIOLATION 4: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: toggleProductReview_Success_MarkAsDeleted (line 335), toggleProductReview_Success_RestoreFromDeleted (line 355), toggleProductReview_MultipleToggles_StatePersists (line 374), toggleProductReview_NegativeId_ThrowsNotFoundException (line 400), toggleProductReview_ReviewNotFound_ThrowsNotFoundException (line 412), toggleProductReview_ZeroId_ThrowsNotFoundException (line 429), toggleProductReview_IdNegative100_ThrowsNotFoundException (line 441), toggleProductReview_Id2_ThrowsNotFoundException (line 453), toggleProductReview_Id999_ThrowsNotFoundException (line 465), toggleProductReview_IdMinLong_ThrowsNotFoundException (line 477), toggleProductReview_IdMaxLong_ThrowsNotFoundException (line 489), toggleProductReview_Id12345_ThrowsNotFoundException (line 501)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 5: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 290 inline mock in `toggleProductReview_controller_permission_unauthorized`: `doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 291 inline mock in `toggleProductReview_controller_permission_unauthorized`: `.when(serviceMock).toggleProductReview(anyLong());`
- Required: Move to base test stub method and call stub in test.
- Line: 340 inline mock in `toggleProductReview_Success_MarkAsDeleted`: `when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 342 inline mock in `toggleProductReview_Success_MarkAsDeleted`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 343 inline mock in `toggleProductReview_Success_MarkAsDeleted`: `when(productReviewRepository.markAllDescendantsAsDeleted(eq(TEST_REVIEW_ID), anyString())).thenReturn(2);`
- Required: Move to base test stub method and call stub in test.
- Line: 360 inline mock in `toggleProductReview_Success_RestoreFromDeleted`: `when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 362 inline mock in `toggleProductReview_Success_RestoreFromDeleted`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 379 inline mock in `toggleProductReview_MultipleToggles_StatePersists`: `when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 381 inline mock in `toggleProductReview_MultipleToggles_StatePersists`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 404 inline mock in `toggleProductReview_NegativeId_ThrowsNotFoundException`: `when(productReviewRepository.findByReviewIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 416 inline mock in `toggleProductReview_ReviewNotFound_ThrowsNotFoundException`: `lenient().when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 433 inline mock in `toggleProductReview_ZeroId_ThrowsNotFoundException`: `when(productReviewRepository.findByReviewIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 445 inline mock in `toggleProductReview_IdNegative100_ThrowsNotFoundException`: `when(productReviewRepository.findByReviewIdAndClientId(-100L, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 457 inline mock in `toggleProductReview_Id2_ThrowsNotFoundException`: `when(productReviewRepository.findByReviewIdAndClientId(2L, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 469 inline mock in `toggleProductReview_Id999_ThrowsNotFoundException`: `when(productReviewRepository.findByReviewIdAndClientId(999L, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 481 inline mock in `toggleProductReview_IdMinLong_ThrowsNotFoundException`: `when(productReviewRepository.findByReviewIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 493 inline mock in `toggleProductReview_IdMaxLong_ThrowsNotFoundException`: `when(productReviewRepository.findByReviewIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 505 inline mock in `toggleProductReview_Id12345_ThrowsNotFoundException`: `when(productReviewRepository.findByReviewIdAndClientId(12345L, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 6: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: toggleProductReview_Success_MarkAsDeleted, toggleProductReview_Success_RestoreFromDeleted, toggleProductReview_MultipleToggles_StatePersists, toggleProductReview_Success_MarkAsDeleted, toggleProductReview_Success_RestoreFromDeleted, toggleProductReview_MultipleToggles_StatePersists
- Required order: toggleProductReview_MultipleToggles_StatePersists, toggleProductReview_MultipleToggles_StatePersists, toggleProductReview_Success_MarkAsDeleted, toggleProductReview_Success_MarkAsDeleted, toggleProductReview_Success_RestoreFromDeleted, toggleProductReview_Success_RestoreFromDeleted

VIOLATION 7: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: toggleProductReview_NegativeId_ThrowsNotFoundException, toggleProductReview_ReviewNotFound_ThrowsNotFoundException, toggleProductReview_ZeroId_ThrowsNotFoundException, toggleProductReview_IdNegative100_ThrowsNotFoundException, toggleProductReview_Id2_ThrowsNotFoundException, toggleProductReview_Id999_ThrowsNotFoundException, toggleProductReview_IdMinLong_ThrowsNotFoundException, toggleProductReview_IdMaxLong_ThrowsNotFoundException, toggleProductReview_Id12345_ThrowsNotFoundException, toggleProductReview_NegativeId_ThrowsNotFoundException, toggleProductReview_ReviewNotFound_ThrowsNotFoundException, toggleProductReview_ZeroId_ThrowsNotFoundException, toggleProductReview_IdNegative100_ThrowsNotFoundException, toggleProductReview_Id2_ThrowsNotFoundException, toggleProductReview_Id999_ThrowsNotFoundException, toggleProductReview_IdMinLong_ThrowsNotFoundException, toggleProductReview_IdMaxLong_ThrowsNotFoundException, toggleProductReview_Id12345_ThrowsNotFoundException
- Required order: toggleProductReview_Id12345_ThrowsNotFoundException, toggleProductReview_Id12345_ThrowsNotFoundException, toggleProductReview_Id2_ThrowsNotFoundException, toggleProductReview_Id2_ThrowsNotFoundException, toggleProductReview_Id999_ThrowsNotFoundException, toggleProductReview_Id999_ThrowsNotFoundException, toggleProductReview_IdMaxLong_ThrowsNotFoundException, toggleProductReview_IdMaxLong_ThrowsNotFoundException, toggleProductReview_IdMinLong_ThrowsNotFoundException, toggleProductReview_IdMinLong_ThrowsNotFoundException, toggleProductReview_IdNegative100_ThrowsNotFoundException, toggleProductReview_IdNegative100_ThrowsNotFoundException, toggleProductReview_NegativeId_ThrowsNotFoundException, toggleProductReview_NegativeId_ThrowsNotFoundException, toggleProductReview_ReviewNotFound_ThrowsNotFoundException, toggleProductReview_ReviewNotFound_ThrowsNotFoundException, toggleProductReview_ZeroId_ThrowsNotFoundException, toggleProductReview_ZeroId_ThrowsNotFoundException

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 9 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/ProductReview/InsertProductReviewTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.ProductReview
Class: * Test class for ProductReviewService.insertProductReview method.
Extends: None
Lines of Code: 1344
Last Modified: 2026-02-10 20:20:42
Declared Test Count: 24 (first occurrence line 29)
Actual @Test Count: 86

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 29
- Current: 24
- Required: 86

VIOLATION 2: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Lines: 29, 539
- Required: Keep only the first declaration at the class start; remove duplicates.

VIOLATION 3: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 497 has mock usage `IProductReviewSubTranslator serviceMock = mock(IProductReviewSubTranslator.class);`
- Required: Move mocks to base test file.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 547 method `insertProductReview_Success`
- Required rename: `insertProductReview_Success_Success`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: insertProductReview_Success (line 545), insertProductReview_RatingOne_Success (line 562), insertProductReview_RatingFive_Success (line 573), insertProductReview_RatingZero_Success (line 584), insertProductReview_EmptyReviewText_ThrowsBadRequestException (line 598), insertProductReview_InvalidRatings_ThrowsBadRequestException (line 613), insertProductReview_NegativeProductId_ThrowsBadRequestException (line 628), insertProductReview_NegativeUserId_ThrowsBadRequestException (line 640), insertProductReview_NullProductId_ThrowsBadRequestException (line 652), insertProductReview_NullRequest_ThrowsBadRequestException (line 667), insertProductReview_NullReviewText_ThrowsBadRequestException (line 680), insertProductReview_NullUserId_ThrowsBadRequestException (line 695), insertProductReview_RatingsTooHigh_ThrowsBadRequestException (line 710), insertProductReview_WhitespaceReviewText_ThrowsBadRequestException (line 725), insertProductReview_ZeroProductId_ThrowsBadRequestException (line 737), insertProductReview_ZeroUserId_ThrowsBadRequestException (line 752), insertProductReview_NullRequestDuplicate_ThrowsBadRequestException (line 767), insertProductReview_ParentIdSet_Success (line 779), insertProductReview_ProductIdOne_Success (line 792), insertProductReview_ProductIdLarge_Success (line 804), insertProductReview_ProductIdNegative99_ThrowsBadRequestException (line 816), insertProductReview_RatingsAboveMax_ThrowsBadRequestException (line 829), insertProductReview_RatingsAtMaxBoundary_Success (line 842), insertProductReview_RatingsAtMinBoundary_Success (line 854), insertProductReview_RatingsExactlyFiveDuplicate_Success (line 866), insertProductReview_RatingsNegativePointOne_ThrowsBadRequestException (line 878), insertProductReview_RatingsNullDuplicate_ThrowsBadRequestException (line 891), insertProductReview_RatingsValidDecimal_Success (line 904), insertProductReview_ReviewTextLong_Success (line 916), insertProductReview_ReviewTextSingleChar_Success (line 928), insertProductReview_ReviewTextWhitespaceOnlyTabs_ThrowsBadRequestException (line 940), insertProductReview_ReviewTextTrailingSpaces_Success (line 953), insertProductReview_UserIdOne_Success (line 965), insertProductReview_UserIdLarge_Success (line 977), insertProductReview_UserIdNegative99_ThrowsBadRequestException (line 989), insertProductReview_RatingsMinimumPositive_Success (line 1006), insertProductReview_RatingsJustBelowMax_Success (line 1018), insertProductReview_RatingsMidRange_Success (line 1030), insertProductReview_RatingsRepeatingDecimal_Success (line 1042), insertProductReview_RatingsJustAboveMax_ThrowsBadRequestException (line 1054), insertProductReview_RatingsDoubleMax_ThrowsBadRequestException (line 1067), insertProductReview_RatingsNegativeOne_ThrowsBadRequestException (line 1080), insertProductReview_ReviewText1000Chars_Success (line 1093), insertProductReview_ReviewText5000Chars_Success (line 1105), insertProductReview_ReviewTextSpecialChars_Success (line 1117), insertProductReview_ReviewTextUnicode_Success (line 1129), insertProductReview_ReviewTextWithNewlines_Success (line 1141), insertProductReview_ReviewTextOnlySpaces_ThrowsBadRequestException (line 1153), insertProductReview_ReviewTextEmptyString_ThrowsBadRequestException (line 1166), insertProductReview_ProductIdExactlyOne_Success (line 1179), insertProductReview_ProductIdLargeValue_Success (line 1191), insertProductReview_ProductIdZero_ThrowsBadRequestException (line 1203), insertProductReview_ProductIdNegativeOne_ThrowsBadRequestException (line 1216), insertProductReview_ProductIdNull_ThrowsBadRequestException (line 1229), insertProductReview_UserIdExactlyOne_Success (line 1242), insertProductReview_UserIdLargeValue_Success (line 1254), insertProductReview_UserIdZero_ThrowsBadRequestException (line 1266), insertProductReview_UserIdNegativeOne_ThrowsBadRequestException (line 1279), insertProductReview_UserIdNull_ThrowsBadRequestException (line 1292), insertProductReview_ParentIdNull_Success (line 1305), insertProductReview_ParentIdValid_Success (line 1317), insertProductReview_AllFieldsAtBoundaries_Success (line 1329)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 669 in `insertProductReview_NullRequest_ThrowsBadRequestException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 499 inline mock in `insertProductReview_controller_permission_unauthorized`: `doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 500 inline mock in `insertProductReview_controller_permission_unauthorized`: `.when(serviceMock).insertProductReview(any(ProductReviewRequestModel.class));`
- Required: Move to base test stub method and call stub in test.
- Line: 549 inline mock in `insertProductReview_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 567 inline mock in `insertProductReview_RatingOne_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 578 inline mock in `insertProductReview_RatingFive_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 785 inline mock in `insertProductReview_ParentIdSet_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 798 inline mock in `insertProductReview_ProductIdOne_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 810 inline mock in `insertProductReview_ProductIdLarge_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 848 inline mock in `insertProductReview_RatingsAtMaxBoundary_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 860 inline mock in `insertProductReview_RatingsAtMinBoundary_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 872 inline mock in `insertProductReview_RatingsExactlyFiveDuplicate_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 910 inline mock in `insertProductReview_RatingsValidDecimal_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 922 inline mock in `insertProductReview_ReviewTextLong_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 934 inline mock in `insertProductReview_ReviewTextSingleChar_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 959 inline mock in `insertProductReview_ReviewTextTrailingSpaces_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 971 inline mock in `insertProductReview_UserIdOne_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 983 inline mock in `insertProductReview_UserIdLarge_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 1012 inline mock in `insertProductReview_RatingsMinimumPositive_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 1024 inline mock in `insertProductReview_RatingsJustBelowMax_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 1036 inline mock in `insertProductReview_RatingsMidRange_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 1048 inline mock in `insertProductReview_RatingsRepeatingDecimal_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 1099 inline mock in `insertProductReview_ReviewText1000Chars_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 1111 inline mock in `insertProductReview_ReviewText5000Chars_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 1123 inline mock in `insertProductReview_ReviewTextSpecialChars_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 1135 inline mock in `insertProductReview_ReviewTextUnicode_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 1147 inline mock in `insertProductReview_ReviewTextWithNewlines_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 1185 inline mock in `insertProductReview_ProductIdExactlyOne_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 1197 inline mock in `insertProductReview_ProductIdLargeValue_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 1248 inline mock in `insertProductReview_UserIdExactlyOne_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 1260 inline mock in `insertProductReview_UserIdLargeValue_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 1311 inline mock in `insertProductReview_ParentIdNull_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 1323 inline mock in `insertProductReview_ParentIdValid_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.
- Line: 1339 inline mock in `insertProductReview_AllFieldsAtBoundaries_Success`: `when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: insertProductReview_Success_Success, insertProductReview_RatingOne_Success, insertProductReview_RatingFive_Success, insertProductReview_RatingZero_Success, insertProductReview_ParentIdSet_Success, insertProductReview_ReviewTextLong_Success, insertProductReview_ReviewTextUnicode_Success, insertProductReview_RatingsAtMaxBoundary_Success, insertProductReview_RatingsAtMinBoundary_Success, insertProductReview_ReviewTextWithNewlines_Success, insertProductReview_Success, insertProductReview_RatingOne_Success, insertProductReview_RatingFive_Success, insertProductReview_RatingZero_Success
- Required order: insertProductReview_ParentIdSet_Success, insertProductReview_RatingFive_Success, insertProductReview_RatingFive_Success, insertProductReview_RatingOne_Success, insertProductReview_RatingOne_Success, insertProductReview_RatingsAtMaxBoundary_Success, insertProductReview_RatingsAtMinBoundary_Success, insertProductReview_RatingZero_Success, insertProductReview_RatingZero_Success, insertProductReview_ReviewTextLong_Success, insertProductReview_ReviewTextUnicode_Success, insertProductReview_ReviewTextWithNewlines_Success, insertProductReview_Success, insertProductReview_Success_Success

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: insertProductReview_EmptyReviewText_ThrowsBadRequestException, insertProductReview_InvalidRatings_ThrowsBadRequestException, insertProductReview_NegativeProductId_ThrowsBadRequestException, insertProductReview_NegativeUserId_ThrowsBadRequestException, insertProductReview_NullProductId_ThrowsBadRequestException, insertProductReview_NullRequest_ThrowsBadRequestException, insertProductReview_NullReviewText_ThrowsBadRequestException, insertProductReview_NullUserId_ThrowsBadRequestException, insertProductReview_RatingsTooHigh_ThrowsBadRequestException, insertProductReview_WhitespaceReviewText_ThrowsBadRequestException, insertProductReview_ZeroProductId_ThrowsBadRequestException, insertProductReview_ZeroUserId_ThrowsBadRequestException, insertProductReview_EmptyReviewText_ThrowsBadRequestException, insertProductReview_InvalidRatings_ThrowsBadRequestException, insertProductReview_NegativeProductId_ThrowsBadRequestException, insertProductReview_NegativeUserId_ThrowsBadRequestException, insertProductReview_NullProductId_ThrowsBadRequestException, insertProductReview_NullRequest_ThrowsBadRequestException, insertProductReview_NullReviewText_ThrowsBadRequestException, insertProductReview_NullUserId_ThrowsBadRequestException, insertProductReview_RatingsTooHigh_ThrowsBadRequestException, insertProductReview_WhitespaceReviewText_ThrowsBadRequestException, insertProductReview_ZeroProductId_ThrowsBadRequestException, insertProductReview_ZeroUserId_ThrowsBadRequestException, insertProductReview_NullRequestDuplicate_ThrowsBadRequestException, insertProductReview_ParentIdSet_Success, insertProductReview_ProductIdOne_Success, insertProductReview_ProductIdLarge_Success, insertProductReview_ProductIdNegative99_ThrowsBadRequestException, insertProductReview_RatingsAboveMax_ThrowsBadRequestException, insertProductReview_RatingsAtMaxBoundary_Success, insertProductReview_RatingsAtMinBoundary_Success, insertProductReview_RatingsExactlyFiveDuplicate_Success, insertProductReview_RatingsNegativePointOne_ThrowsBadRequestException, insertProductReview_RatingsNullDuplicate_ThrowsBadRequestException, insertProductReview_RatingsValidDecimal_Success, insertProductReview_ReviewTextLong_Success, insertProductReview_ReviewTextSingleChar_Success, insertProductReview_ReviewTextWhitespaceOnlyTabs_ThrowsBadRequestException, insertProductReview_ReviewTextTrailingSpaces_Success, insertProductReview_UserIdOne_Success, insertProductReview_UserIdLarge_Success, insertProductReview_UserIdNegative99_ThrowsBadRequestException, insertProductReview_RatingsMinimumPositive_Success, insertProductReview_RatingsJustBelowMax_Success, insertProductReview_RatingsMidRange_Success, insertProductReview_RatingsRepeatingDecimal_Success, insertProductReview_RatingsJustAboveMax_ThrowsBadRequestException, insertProductReview_RatingsDoubleMax_ThrowsBadRequestException, insertProductReview_RatingsNegativeOne_ThrowsBadRequestException, insertProductReview_ReviewText1000Chars_Success, insertProductReview_ReviewText5000Chars_Success, insertProductReview_ReviewTextSpecialChars_Success, insertProductReview_ReviewTextUnicode_Success, insertProductReview_ReviewTextWithNewlines_Success, insertProductReview_ReviewTextOnlySpaces_ThrowsBadRequestException, insertProductReview_ReviewTextEmptyString_ThrowsBadRequestException, insertProductReview_ProductIdExactlyOne_Success, insertProductReview_ProductIdLargeValue_Success, insertProductReview_ProductIdZero_ThrowsBadRequestException, insertProductReview_ProductIdNegativeOne_ThrowsBadRequestException, insertProductReview_ProductIdNull_ThrowsBadRequestException, insertProductReview_UserIdExactlyOne_Success, insertProductReview_UserIdLargeValue_Success, insertProductReview_UserIdZero_ThrowsBadRequestException, insertProductReview_UserIdNegativeOne_ThrowsBadRequestException, insertProductReview_UserIdNull_ThrowsBadRequestException, insertProductReview_ParentIdNull_Success, insertProductReview_ParentIdValid_Success, insertProductReview_AllFieldsAtBoundaries_Success
- Required order: insertProductReview_AllFieldsAtBoundaries_Success, insertProductReview_EmptyReviewText_ThrowsBadRequestException, insertProductReview_EmptyReviewText_ThrowsBadRequestException, insertProductReview_InvalidRatings_ThrowsBadRequestException, insertProductReview_InvalidRatings_ThrowsBadRequestException, insertProductReview_NegativeProductId_ThrowsBadRequestException, insertProductReview_NegativeProductId_ThrowsBadRequestException, insertProductReview_NegativeUserId_ThrowsBadRequestException, insertProductReview_NegativeUserId_ThrowsBadRequestException, insertProductReview_NullProductId_ThrowsBadRequestException, insertProductReview_NullProductId_ThrowsBadRequestException, insertProductReview_NullRequest_ThrowsBadRequestException, insertProductReview_NullRequest_ThrowsBadRequestException, insertProductReview_NullRequestDuplicate_ThrowsBadRequestException, insertProductReview_NullReviewText_ThrowsBadRequestException, insertProductReview_NullReviewText_ThrowsBadRequestException, insertProductReview_NullUserId_ThrowsBadRequestException, insertProductReview_NullUserId_ThrowsBadRequestException, insertProductReview_ParentIdNull_Success, insertProductReview_ParentIdSet_Success, insertProductReview_ParentIdValid_Success, insertProductReview_ProductIdExactlyOne_Success, insertProductReview_ProductIdLarge_Success, insertProductReview_ProductIdLargeValue_Success, insertProductReview_ProductIdNegative99_ThrowsBadRequestException, insertProductReview_ProductIdNegativeOne_ThrowsBadRequestException, insertProductReview_ProductIdNull_ThrowsBadRequestException, insertProductReview_ProductIdOne_Success, insertProductReview_ProductIdZero_ThrowsBadRequestException, insertProductReview_RatingsAboveMax_ThrowsBadRequestException, insertProductReview_RatingsAtMaxBoundary_Success, insertProductReview_RatingsAtMinBoundary_Success, insertProductReview_RatingsDoubleMax_ThrowsBadRequestException, insertProductReview_RatingsExactlyFiveDuplicate_Success, insertProductReview_RatingsJustAboveMax_ThrowsBadRequestException, insertProductReview_RatingsJustBelowMax_Success, insertProductReview_RatingsMidRange_Success, insertProductReview_RatingsMinimumPositive_Success, insertProductReview_RatingsNegativeOne_ThrowsBadRequestException, insertProductReview_RatingsNegativePointOne_ThrowsBadRequestException, insertProductReview_RatingsNullDuplicate_ThrowsBadRequestException, insertProductReview_RatingsRepeatingDecimal_Success, insertProductReview_RatingsTooHigh_ThrowsBadRequestException, insertProductReview_RatingsTooHigh_ThrowsBadRequestException, insertProductReview_RatingsValidDecimal_Success, insertProductReview_ReviewText1000Chars_Success, insertProductReview_ReviewText5000Chars_Success, insertProductReview_ReviewTextEmptyString_ThrowsBadRequestException, insertProductReview_ReviewTextLong_Success, insertProductReview_ReviewTextOnlySpaces_ThrowsBadRequestException, insertProductReview_ReviewTextSingleChar_Success, insertProductReview_ReviewTextSpecialChars_Success, insertProductReview_ReviewTextTrailingSpaces_Success, insertProductReview_ReviewTextUnicode_Success, insertProductReview_ReviewTextWhitespaceOnlyTabs_ThrowsBadRequestException, insertProductReview_ReviewTextWithNewlines_Success, insertProductReview_UserIdExactlyOne_Success, insertProductReview_UserIdLarge_Success, insertProductReview_UserIdLargeValue_Success, insertProductReview_UserIdNegative99_ThrowsBadRequestException, insertProductReview_UserIdNegativeOne_ThrowsBadRequestException, insertProductReview_UserIdNull_ThrowsBadRequestException, insertProductReview_UserIdOne_Success, insertProductReview_UserIdZero_ThrowsBadRequestException, insertProductReview_WhitespaceReviewText_ThrowsBadRequestException, insertProductReview_WhitespaceReviewText_ThrowsBadRequestException, insertProductReview_ZeroProductId_ThrowsBadRequestException, insertProductReview_ZeroProductId_ThrowsBadRequestException, insertProductReview_ZeroUserId_ThrowsBadRequestException, insertProductReview_ZeroUserId_ThrowsBadRequestException

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/ProductReview/GetProductReviewsInBatchesTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.ProductReview
Class: * Test class for ProductReviewService.getProductReviewsInBatchesGivenProductId
Extends: None
Lines of Code: 489
Last Modified: 2026-02-10 01:10:30
Declared Test Count: 25 (first occurrence line 32)
Actual @Test Count: 22

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 32
- Current: 25
- Required: 22

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getProductReviewsInBatches_controller_permission_forbidden` or `getProductReviewsInBatches_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 36 method `getProductReviewsInBatches_Success`
- Required rename: `getProductReviewsInBatches_Success_Success`

VIOLATION 4: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getProductReviewsInBatches_Success (line 34), getProductReviewsInBatches_EmptyResults_Success (line 56), getProductReviewsInBatches_InvalidPagination_ThrowsBadRequestException (line 77), getProductReviewsInBatches_SingleItem_Success (line 96), getProductReviewsInBatches_LargePage_Success (line 118), getProductReviewsInBatches_MiddlePage_Success (line 140), getProductReviewsInBatches_ZeroPageSize_ThrowsBadRequestException (line 162), getProductReviewsInBatches_EqualStartEnd_ThrowsBadRequestException (line 177), getProductReviewsInBatches_ReversedPagination_ThrowsBadRequestException (line 192), getProductReviewsInBatches_ProductIdOne_Success (line 207), getProductReviewsInBatches_ProductIdLarge_Success (line 226), getProductReviewsInBatches_MultipleResults_Success (line 247), getProductReviewsInBatches_LargeTotalCount_Success (line 275), getProductReviewsInBatches_FirstPage_Success (line 295), getProductReviewsInBatches_LastPage_Success (line 317), getProductReviewsInBatches_FullDataset_Success (line 339), getProductReviewsInBatches_NullFilters_Success (line 361), getProductReviewsInBatches_IncludeDeletedTrue_Success (line 382), getProductReviewsInBatches_IncludeDeletedFalse_Success (line 403), getProductReviewsInBatches_OffsetPagination_Success (line 424), getProductReviewsInBatches_NearEnd_Success (line 446), getProductReviewsInBatches_NegativeStart_Success (line 468)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 5: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 40 inline mock in `getProductReviewsInBatches_Success`: `when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 61 inline mock in `getProductReviewsInBatches_EmptyResults_Success`: `when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 104 inline mock in `getProductReviewsInBatches_SingleItem_Success`: `when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 126 inline mock in `getProductReviewsInBatches_LargePage_Success`: `when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 148 inline mock in `getProductReviewsInBatches_MiddlePage_Success`: `when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 213 inline mock in `getProductReviewsInBatches_ProductIdOne_Success`: `when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 233 inline mock in `getProductReviewsInBatches_ProductIdLarge_Success`: `when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 260 inline mock in `getProductReviewsInBatches_MultipleResults_Success`: `when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 281 inline mock in `getProductReviewsInBatches_LargeTotalCount_Success`: `when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 303 inline mock in `getProductReviewsInBatches_FirstPage_Success`: `when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 325 inline mock in `getProductReviewsInBatches_LastPage_Success`: `when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 347 inline mock in `getProductReviewsInBatches_FullDataset_Success`: `when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 368 inline mock in `getProductReviewsInBatches_NullFilters_Success`: `when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 389 inline mock in `getProductReviewsInBatches_IncludeDeletedTrue_Success`: `when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 410 inline mock in `getProductReviewsInBatches_IncludeDeletedFalse_Success`: `when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 432 inline mock in `getProductReviewsInBatches_OffsetPagination_Success`: `when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 454 inline mock in `getProductReviewsInBatches_NearEnd_Success`: `when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 476 inline mock in `getProductReviewsInBatches_NegativeStart_Success`: `when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.

VIOLATION 6: Rule 8 - Error Constants
- Severity: HIGH
- Line: 89 has hardcoded message: `assertTrue(exception.getMessage().contains("Invalid pagination"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 89 has hardcoded message: `assertTrue(exception.getMessage().contains("Invalid pagination"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 174 has hardcoded message: `assertTrue(exception.getMessage().contains("Invalid pagination"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 174 has hardcoded message: `assertTrue(exception.getMessage().contains("Invalid pagination"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 189 has hardcoded message: `assertTrue(exception.getMessage().contains("Invalid pagination"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 189 has hardcoded message: `assertTrue(exception.getMessage().contains("Invalid pagination"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 204 has hardcoded message: `assertTrue(exception.getMessage().contains("Invalid pagination"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 204 has hardcoded message: `assertTrue(exception.getMessage().contains("Invalid pagination"));`
- Required: Replace with an ErrorMessages constant (add one if missing).

VIOLATION 7: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: PERMISSION
- Required: Add Success, Failure, Permission section headers.

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 14 issues above.
- Fix Rule 8 issues above.
- Fix Rule 10 issues above.


**IMPLEMENTATION PLAN (STEP-BY-STEP)**
1. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/ProductReview/GetProductReviewsInBatchesGivenProductIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
2. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/ProductReview/SetProductReviewScoreTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
3. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/ProductReview/ToggleProductReviewTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
4. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/ProductReview/InsertProductReviewTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
5. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/ProductReview/GetProductReviewsInBatchesTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
6. Resolve extra test file `GetProductReviewsInBatchesTest.java` by renaming it to match a public method or removing it.

Verification Commands (run after fixes):
- mvn -Dtest=GetProductReviewsInBatchesGivenProductIdTest test
- mvn -Dtest=SetProductReviewScoreTest test
- mvn -Dtest=ToggleProductReviewTest test
- mvn -Dtest=InsertProductReviewTest test
- mvn -Dtest=GetProductReviewsInBatchesTest test