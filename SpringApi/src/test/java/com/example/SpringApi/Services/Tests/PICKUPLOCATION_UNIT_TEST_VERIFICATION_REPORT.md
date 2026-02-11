# UNIT TEST VERIFICATION REPORT — PickupLocation

```
╔════════════════════════════════════════════════════════════╗
║           UNIT TEST VERIFICATION REPORT                    ║
║                                                            ║
║  Status: ❌ RED                                               ║
║  Services Analyzed: 1                                      ║
║  Public Methods Found: 7                                  ║
║  Test Files Expected: 7                                  ║
║  Test Files Found: 7                                     ║
║  Total Violations: 51                                    ║
╚════════════════════════════════════════════════════════════╝
```

VIOLATIONS BY RULE:

| Rule | Description | Count |
| --- | --- | --- |
| 2 | Test Count Declaration | 6 |
| 3 | Controller Permission Test | 1 |
| 5 | Test Naming Convention | 6 |
| 6 | Centralized Mocking | 5 |
| 7 | Exception Assertions | 3 |
| 8 | Error Constants | 2 |
| 9 | Test Documentation | 6 |
| 10 | Test Ordering | 8 |
| 12 | Arrange/Act/Assert | 7 |
| 14 | No Inline Mocks | 7 |


**FILE-BY-FILE BREAKDOWN**

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/BulkCreatePickupLocationsTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PickupLocation
Class: class BulkCreatePickupLocationsTest extends PickupLocationServiceTestBase {
Extends: PickupLocationServiceTestBase
Lines of Code: 545
Last Modified: 2026-02-11 01:04:14
Declared Test Count: 22 (first occurrence line 37)
Actual @Test Count: 22

VIOLATIONS FOUND:

VIOLATION 1: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 425 in `bulkCreatePickupLocations_NullList_ThrowsBadRequestException` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 2: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 490 inline mock in `bulkCreatePickupLocations_controller_permission_unauthorized`: `when(pickupLocationServiceMock.getUserId()).thenReturn(TEST_USER_ID);`
- Required: Move to base test stub method and call stub in test.
- Line: 491 inline mock in `bulkCreatePickupLocations_controller_permission_unauthorized`: `when(pickupLocationServiceMock.getUser()).thenReturn("testuser");`
- Required: Move to base test stub method and call stub in test.
- Line: 492 inline mock in `bulkCreatePickupLocations_controller_permission_unauthorized`: `when(pickupLocationServiceMock.getClientId()).thenReturn(TEST_CLIENT_ID);`
- Required: Move to base test stub method and call stub in test.

REQUIRED FIXES SUMMARY:
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/UpdatePickupLocationTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PickupLocation
Class: class UpdatePickupLocationTest extends PickupLocationServiceTestBase {
Extends: PickupLocationServiceTestBase
Lines of Code: 648
Last Modified: 2026-02-11 01:18:18
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 34

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 34` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 638 has mock usage `PickupLocationService mockService = mock(PickupLocationService.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 45 method `updatePickupLocation_Success`
- Required rename: `updatePickupLocation_Success_Success`
- Line: 626 method `updatePickupLocation_VerifyPreAuthorizeAnnotation`
- Required rename: `updatePickupLocation_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 4: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: updatePickupLocation_VerifyPreAuthorizeAnnotation (line 624), updatePickupLocation_WithValidRequest_DelegatesToService (line 635)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 5: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 45 in `updatePickupLocation_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 69 in `updatePickupLocation_UpdatesShipRocketId_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 99 in `updatePickupLocation_MaxLongId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 114 in `updatePickupLocation_MinLongId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 129 in `updatePickupLocation_NegativeId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 144 in `updatePickupLocation_NullAddress_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 160 in `updatePickupLocation_NullCity_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 176 in `updatePickupLocation_NullCountry_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 192 in `updatePickupLocation_NullNickname_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 208 in `updatePickupLocation_NullPhone_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 226 in `updatePickupLocation_NullPostalCode_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 242 in `updatePickupLocation_NullRequest_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 255 in `updatePickupLocation_NullState_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 271 in `updatePickupLocation_NullStreetAddress_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 287 in `updatePickupLocation_WhitespaceCity_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 303 in `updatePickupLocation_WhitespaceCountry_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 319 in `updatePickupLocation_WhitespaceNickname_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 335 in `updatePickupLocation_WhitespacePhone_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 353 in `updatePickupLocation_WhitespacePostalCode_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 369 in `updatePickupLocation_WhitespaceState_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 385 in `updatePickupLocation_WhitespaceStreetAddress_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 401 in `updatePickupLocation_ZeroId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 417 in `updatePickupLocation_PreservesCreatedTimestamp_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 440 in `updatePickupLocation_ModifiesOnlyChangedFields_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 464 in `updatePickupLocation_VeryLongNickname_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 484 in `updatePickupLocation_SpecialCharactersInAddress_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 504 in `updatePickupLocation_InternationalAddress_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 525 in `updatePickupLocation_VeryLargeId_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 546 in `updatePickupLocation_VerifyClientIsolation_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 568 in `updatePickupLocation_RepositoryReturnsNull_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 586 in `updatePickupLocation_NumericPostalCode_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 611 in `updatePickupLocation_controller_permission_unauthorized` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 626 in `updatePickupLocation_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 637 in `updatePickupLocation_WithValidRequest_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 6: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 52 inline mock in `updatePickupLocation_Success`: `when(addressRepository.findById(any())).thenReturn(Optional.of(testAddress));`
- Required: Move to base test stub method and call stub in test.
- Line: 53 inline mock in `updatePickupLocation_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 54 inline mock in `updatePickupLocation_Success`: `when(pickupLocationRepository.save(any())).thenReturn(existingPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 75 inline mock in `updatePickupLocation_UpdatesShipRocketId_Success`: `when(addressRepository.findById(any())).thenReturn(Optional.of(testAddress));`
- Required: Move to base test stub method and call stub in test.
- Line: 76 inline mock in `updatePickupLocation_UpdatesShipRocketId_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 77 inline mock in `updatePickupLocation_UpdatesShipRocketId_Success`: `when(pickupLocationRepository.save(any())).thenReturn(existing);`
- Required: Move to base test stub method and call stub in test.
- Line: 212 inline mock in `updatePickupLocation_NullPhone_Success`: `when(addressRepository.findById(any())).thenReturn(Optional.of(testAddress));`
- Required: Move to base test stub method and call stub in test.
- Line: 213 inline mock in `updatePickupLocation_NullPhone_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 214 inline mock in `updatePickupLocation_NullPhone_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 339 inline mock in `updatePickupLocation_WhitespacePhone_Success`: `when(addressRepository.findById(any())).thenReturn(Optional.of(testAddress));`
- Required: Move to base test stub method and call stub in test.
- Line: 340 inline mock in `updatePickupLocation_WhitespacePhone_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 341 inline mock in `updatePickupLocation_WhitespacePhone_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 640 inline mock in `updatePickupLocation_WithValidRequest_DelegatesToService`: `doNothing().when(mockService).updatePickupLocation(testPickupLocationRequest);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 7: Rule 8 - Error Constants
- Severity: HIGH
- Line: 134 has hardcoded message: `assertTrue(ex.getMessage().contains("-100"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 134 has hardcoded message: `assertTrue(ex.getMessage().contains("-100"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 406 has hardcoded message: `assertTrue(ex.getMessage().contains("0"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 406 has hardcoded message: `assertTrue(ex.getMessage().contains("0"));`
- Required: Replace with an ErrorMessages constant (add one if missing).

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: updatePickupLocation_MaxLongId_ThrowsNotFoundException, updatePickupLocation_MinLongId_ThrowsNotFoundException, updatePickupLocation_NegativeId_ThrowsNotFoundException, updatePickupLocation_NullAddress_ThrowsBadRequestException, updatePickupLocation_NullCity_ThrowsBadRequestException, updatePickupLocation_NullCountry_ThrowsBadRequestException, updatePickupLocation_NullNickname_ThrowsBadRequestException, updatePickupLocation_NullPhone_Success, updatePickupLocation_NullPostalCode_ThrowsBadRequestException, updatePickupLocation_NullRequest_ThrowsBadRequestException, updatePickupLocation_NullState_ThrowsBadRequestException, updatePickupLocation_NullStreetAddress_ThrowsBadRequestException, updatePickupLocation_WhitespaceCity_ThrowsBadRequestException, updatePickupLocation_WhitespaceCountry_ThrowsBadRequestException, updatePickupLocation_WhitespaceNickname_ThrowsBadRequestException, updatePickupLocation_WhitespacePhone_Success, updatePickupLocation_WhitespacePostalCode_ThrowsBadRequestException, updatePickupLocation_WhitespaceState_ThrowsBadRequestException, updatePickupLocation_WhitespaceStreetAddress_ThrowsBadRequestException, updatePickupLocation_ZeroId_ThrowsNotFoundException, updatePickupLocation_PreservesCreatedTimestamp_Success, updatePickupLocation_ModifiesOnlyChangedFields_Success, updatePickupLocation_VeryLongNickname_Success, updatePickupLocation_SpecialCharactersInAddress_Success, updatePickupLocation_InternationalAddress_Success, updatePickupLocation_VeryLargeId_Success, updatePickupLocation_VerifyClientIsolation_Success, updatePickupLocation_RepositoryReturnsNull_Success, updatePickupLocation_NumericPostalCode_Success
- Required order: updatePickupLocation_InternationalAddress_Success, updatePickupLocation_MaxLongId_ThrowsNotFoundException, updatePickupLocation_MinLongId_ThrowsNotFoundException, updatePickupLocation_ModifiesOnlyChangedFields_Success, updatePickupLocation_NegativeId_ThrowsNotFoundException, updatePickupLocation_NullAddress_ThrowsBadRequestException, updatePickupLocation_NullCity_ThrowsBadRequestException, updatePickupLocation_NullCountry_ThrowsBadRequestException, updatePickupLocation_NullNickname_ThrowsBadRequestException, updatePickupLocation_NullPhone_Success, updatePickupLocation_NullPostalCode_ThrowsBadRequestException, updatePickupLocation_NullRequest_ThrowsBadRequestException, updatePickupLocation_NullState_ThrowsBadRequestException, updatePickupLocation_NullStreetAddress_ThrowsBadRequestException, updatePickupLocation_NumericPostalCode_Success, updatePickupLocation_PreservesCreatedTimestamp_Success, updatePickupLocation_RepositoryReturnsNull_Success, updatePickupLocation_SpecialCharactersInAddress_Success, updatePickupLocation_VerifyClientIsolation_Success, updatePickupLocation_VeryLargeId_Success, updatePickupLocation_VeryLongNickname_Success, updatePickupLocation_WhitespaceCity_ThrowsBadRequestException, updatePickupLocation_WhitespaceCountry_ThrowsBadRequestException, updatePickupLocation_WhitespaceNickname_ThrowsBadRequestException, updatePickupLocation_WhitespacePhone_Success, updatePickupLocation_WhitespacePostalCode_ThrowsBadRequestException, updatePickupLocation_WhitespaceState_ThrowsBadRequestException, updatePickupLocation_WhitespaceStreetAddress_ThrowsBadRequestException, updatePickupLocation_ZeroId_ThrowsNotFoundException

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 8 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/GetPickupLocationsInBatchesTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PickupLocation
Class: class GetPickupLocationsInBatchesTest extends PickupLocationServiceTestBase {
Extends: PickupLocationServiceTestBase
Lines of Code: 358
Last Modified: 2026-02-10 23:56:34
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 14

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 14` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 347 has mock usage `PickupLocationService mockService = mock(PickupLocationService.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 335 method `getPickupLocationsInBatches_VerifyPreAuthorizeAnnotation`
- Required rename: `getPickupLocationsInBatches_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 4: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getPickupLocationsInBatches_VerifyPreAuthorizeAnnotation (line 333), getPickupLocationsInBatches_WithValidRequest_DelegatesToService (line 344)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 5: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 49 in `getPickupLocationsInBatches_Comprehensive_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 81 in `getPickupLocationsInBatches_EndIndexZero_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 99 in `getPickupLocationsInBatches_NegativeStart_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 117 in `getPickupLocationsInBatches_StartGreaterEqualEnd_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 135 in `getPickupLocationsInBatches_LargeBatchSize_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 162 in `getPickupLocationsInBatches_MinimalRange_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 190 in `getPickupLocationsInBatches_IncludeDeleted_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 220 in `getPickupLocationsInBatches_NegativeEndIndex_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 238 in `getPickupLocationsInBatches_StartEqualsEnd_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 256 in `getPickupLocationsInBatches_EmptyResults_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 284 in `getPickupLocationsInBatches_MultipleResults_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 321 in `getPickupLocationsInBatches_controller_permission_unauthorized` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 335 in `getPickupLocationsInBatches_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 346 in `getPickupLocationsInBatches_WithValidRequest_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 6: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 57 inline mock in `getPickupLocationsInBatches_Comprehensive_Success`: `when(pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 143 inline mock in `getPickupLocationsInBatches_LargeBatchSize_Success`: `when(pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 170 inline mock in `getPickupLocationsInBatches_MinimalRange_Success`: `when(pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 199 inline mock in `getPickupLocationsInBatches_IncludeDeleted_Success`: `when(pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 264 inline mock in `getPickupLocationsInBatches_EmptyResults_Success`: `when(pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 295 inline mock in `getPickupLocationsInBatches_MultipleResults_Success`: `when(pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(`
- Required: Move to base test stub method and call stub in test.
- Line: 350 inline mock in `getPickupLocationsInBatches_WithValidRequest_DelegatesToService`: `when(mockService.getPickupLocationsInBatches(testPaginationRequest))`
- Required: Move to base test stub method and call stub in test.

VIOLATION 7: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: getPickupLocationsInBatches_EndIndexZero_ThrowsBadRequestException, getPickupLocationsInBatches_NegativeStart_ThrowsBadRequestException, getPickupLocationsInBatches_StartGreaterEqualEnd_ThrowsBadRequestException, getPickupLocationsInBatches_LargeBatchSize_Success, getPickupLocationsInBatches_MinimalRange_Success, getPickupLocationsInBatches_IncludeDeleted_Success, getPickupLocationsInBatches_NegativeEndIndex_ThrowsBadRequestException, getPickupLocationsInBatches_StartEqualsEnd_ThrowsBadRequestException, getPickupLocationsInBatches_EmptyResults_Success, getPickupLocationsInBatches_MultipleResults_Success
- Required order: getPickupLocationsInBatches_EmptyResults_Success, getPickupLocationsInBatches_EndIndexZero_ThrowsBadRequestException, getPickupLocationsInBatches_IncludeDeleted_Success, getPickupLocationsInBatches_LargeBatchSize_Success, getPickupLocationsInBatches_MinimalRange_Success, getPickupLocationsInBatches_MultipleResults_Success, getPickupLocationsInBatches_NegativeEndIndex_ThrowsBadRequestException, getPickupLocationsInBatches_NegativeStart_ThrowsBadRequestException, getPickupLocationsInBatches_StartEqualsEnd_ThrowsBadRequestException, getPickupLocationsInBatches_StartGreaterEqualEnd_ThrowsBadRequestException

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/CreatePickupLocationTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PickupLocation
Class: class CreatePickupLocationTest extends PickupLocationServiceTestBase {
Extends: PickupLocationServiceTestBase
Lines of Code: 506
Last Modified: 2026-02-11 00:47:34
Declared Test Count: MISSING/MISPLACED (first occurrence line 28)
Actual @Test Count: 30

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 28
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 30` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 497 has mock usage `PickupLocationService mockService = mock(PickupLocationService.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 62 method `createPickupLocation_Success`
- Required rename: `createPickupLocation_Success_Success`
- Line: 485 method `createPickupLocation_VerifyPreAuthorizeAnnotation`
- Required rename: `createPickupLocation_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 4: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: createPickupLocation_VerifyPreAuthorizeAnnotation (line 483), createPickupLocation_WithValidRequest_DelegatesToService (line 494)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 5: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 46 in `createPickupLocation_AddressNicknameLengthOne_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 62 in `createPickupLocation_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 87 in `createPickupLocation_NullAddress_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 101 in `createPickupLocation_NullCity_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 115 in `createPickupLocation_NullCountry_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 129 in `createPickupLocation_NullNickname_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 143 in `createPickupLocation_NullPhone_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 157 in `createPickupLocation_NullPostalCode_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 171 in `createPickupLocation_NullRequest_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 184 in `createPickupLocation_NullState_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 198 in `createPickupLocation_NullStreetAddress_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 212 in `createPickupLocation_WhitespaceCity_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 226 in `createPickupLocation_WhitespaceCountry_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 240 in `createPickupLocation_WhitespaceNickname_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 254 in `createPickupLocation_WhitespacePhone_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 268 in `createPickupLocation_WhitespacePostalCode_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 282 in `createPickupLocation_WhitespaceState_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 296 in `createPickupLocation_WhitespaceStreetAddress_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 310 in `createPickupLocation_VeryLongNickname_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 327 in `createPickupLocation_SpecialCharactersInAddress_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 343 in `createPickupLocation_InternationalCharacters_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 360 in `createPickupLocation_NumericPostalCode_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 376 in `createPickupLocation_AlphanumericPostalCode_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 392 in `createPickupLocation_LongPhoneNumber_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 408 in `createPickupLocation_RepositoryErrorOnAddressSave_ThrowsException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 423 in `createPickupLocation_RepositoryErrorOnLocationSave_ThrowsException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 446 in `createPickupLocation_IsDeletedFlagFalse_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 471 in `createPickupLocation_controller_permission_unauthorized` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 485 in `createPickupLocation_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 496 in `createPickupLocation_WithValidRequest_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 6: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 410 inline mock in `createPickupLocation_RepositoryErrorOnAddressSave_ThrowsException`: `when(addressRepository.save(any())).thenThrow(new RuntimeException("Database error"));`
- Required: Move to base test stub method and call stub in test.
- Line: 433 inline mock in `createPickupLocation_RepositoryErrorOnLocationSave_ThrowsException`: `when(pickupLocationRepository.save(any())).thenThrow(new RuntimeException("Database error"));`
- Required: Move to base test stub method and call stub in test.
- Line: 499 inline mock in `createPickupLocation_WithValidRequest_DelegatesToService`: `doNothing().when(mockService).createPickupLocation(testPickupLocationRequest);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 7: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 413 in `createPickupLocation_RepositoryErrorOnAddressSave_ThrowsException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 436 in `createPickupLocation_RepositoryErrorOnLocationSave_ThrowsException`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: createPickupLocation_NullAddress_ThrowsBadRequestException, createPickupLocation_NullCity_ThrowsBadRequestException, createPickupLocation_NullCountry_ThrowsBadRequestException, createPickupLocation_NullNickname_ThrowsBadRequestException, createPickupLocation_NullPhone_Success, createPickupLocation_NullPostalCode_ThrowsBadRequestException, createPickupLocation_NullRequest_ThrowsBadRequestException, createPickupLocation_NullState_ThrowsBadRequestException, createPickupLocation_NullStreetAddress_ThrowsBadRequestException, createPickupLocation_WhitespaceCity_ThrowsBadRequestException, createPickupLocation_WhitespaceCountry_ThrowsBadRequestException, createPickupLocation_WhitespaceNickname_ThrowsBadRequestException, createPickupLocation_WhitespacePhone_Success, createPickupLocation_WhitespacePostalCode_ThrowsBadRequestException, createPickupLocation_WhitespaceState_ThrowsBadRequestException, createPickupLocation_WhitespaceStreetAddress_ThrowsBadRequestException, createPickupLocation_VeryLongNickname_Success, createPickupLocation_SpecialCharactersInAddress_Success, createPickupLocation_InternationalCharacters_Success, createPickupLocation_NumericPostalCode_Success, createPickupLocation_AlphanumericPostalCode_Success, createPickupLocation_LongPhoneNumber_Success, createPickupLocation_RepositoryErrorOnAddressSave_ThrowsException, createPickupLocation_RepositoryErrorOnLocationSave_ThrowsException, createPickupLocation_IsDeletedFlagFalse_Success
- Required order: createPickupLocation_AlphanumericPostalCode_Success, createPickupLocation_InternationalCharacters_Success, createPickupLocation_IsDeletedFlagFalse_Success, createPickupLocation_LongPhoneNumber_Success, createPickupLocation_NullAddress_ThrowsBadRequestException, createPickupLocation_NullCity_ThrowsBadRequestException, createPickupLocation_NullCountry_ThrowsBadRequestException, createPickupLocation_NullNickname_ThrowsBadRequestException, createPickupLocation_NullPhone_Success, createPickupLocation_NullPostalCode_ThrowsBadRequestException, createPickupLocation_NullRequest_ThrowsBadRequestException, createPickupLocation_NullState_ThrowsBadRequestException, createPickupLocation_NullStreetAddress_ThrowsBadRequestException, createPickupLocation_NumericPostalCode_Success, createPickupLocation_RepositoryErrorOnAddressSave_ThrowsException, createPickupLocation_RepositoryErrorOnLocationSave_ThrowsException, createPickupLocation_SpecialCharactersInAddress_Success, createPickupLocation_VeryLongNickname_Success, createPickupLocation_WhitespaceCity_ThrowsBadRequestException, createPickupLocation_WhitespaceCountry_ThrowsBadRequestException, createPickupLocation_WhitespaceNickname_ThrowsBadRequestException, createPickupLocation_WhitespacePhone_Success, createPickupLocation_WhitespacePostalCode_ThrowsBadRequestException, createPickupLocation_WhitespaceState_ThrowsBadRequestException, createPickupLocation_WhitespaceStreetAddress_ThrowsBadRequestException

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 7 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/TogglePickupLocationTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PickupLocation
Class: class TogglePickupLocationTest extends PickupLocationServiceTestBase {
Extends: PickupLocationServiceTestBase
Lines of Code: 303
Last Modified: 2026-02-10 23:56:34
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 15

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 15` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 294 has mock usage `PickupLocationService mockService = mock(PickupLocationService.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 80 method `togglePickupLocation_Success`
- Required rename: `togglePickupLocation_Success_Success`
- Line: 283 method `togglePickupLocation_VerifyPreAuthorizeAnnotation`
- Required rename: `togglePickupLocation_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 4: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: togglePickupLocation_VerifyPreAuthorizeAnnotation (line 281), togglePickupLocation_WithValidId_DelegatesToService (line 291)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 5: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 41 in `togglePickupLocation_MultipleToggles_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 62 in `togglePickupLocation_RestoreFromDeleted_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 80 in `togglePickupLocation_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 105 in `togglePickupLocation_MaxLongId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 121 in `togglePickupLocation_MinLongId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 136 in `togglePickupLocation_NegativeId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 150 in `togglePickupLocation_NotFound_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 164 in `togglePickupLocation_ZeroId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 178 in `togglePickupLocation_PreservesOtherProperties_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 200 in `togglePickupLocation_VeryLargeId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 219 in `togglePickupLocation_ClientIsolation_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 239 in `togglePickupLocation_ThreeConsecutiveToggles_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 269 in `togglePickupLocation_controller_permission_unauthorized` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 283 in `togglePickupLocation_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 293 in `togglePickupLocation_WithValidId_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 6: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 43 inline mock in `togglePickupLocation_MultipleToggles_Success`: `when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 45 inline mock in `togglePickupLocation_MultipleToggles_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 64 inline mock in `togglePickupLocation_RestoreFromDeleted_Success`: `when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 66 inline mock in `togglePickupLocation_RestoreFromDeleted_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 81 inline mock in `togglePickupLocation_Success`: `when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 83 inline mock in `togglePickupLocation_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 106 inline mock in `togglePickupLocation_MaxLongId_ThrowsNotFoundException`: `when(pickupLocationRepository.findPickupLocationByIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 122 inline mock in `togglePickupLocation_MinLongId_ThrowsNotFoundException`: `when(pickupLocationRepository.findPickupLocationByIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 137 inline mock in `togglePickupLocation_NegativeId_ThrowsNotFoundException`: `when(pickupLocationRepository.findPickupLocationByIdAndClientId(-100L, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 151 inline mock in `togglePickupLocation_NotFound_ThrowsNotFoundException`: `when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 165 inline mock in `togglePickupLocation_ZeroId_ThrowsNotFoundException`: `when(pickupLocationRepository.findPickupLocationByIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 181 inline mock in `togglePickupLocation_PreservesOtherProperties_Success`: `when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 183 inline mock in `togglePickupLocation_PreservesOtherProperties_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 203 inline mock in `togglePickupLocation_VeryLargeId_ThrowsNotFoundException`: `when(pickupLocationRepository.findPickupLocationByIdAndClientId(largeId, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 221 inline mock in `togglePickupLocation_ClientIsolation_Success`: `when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 223 inline mock in `togglePickupLocation_ClientIsolation_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 242 inline mock in `togglePickupLocation_ThreeConsecutiveToggles_Success`: `when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 244 inline mock in `togglePickupLocation_ThreeConsecutiveToggles_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 296 inline mock in `togglePickupLocation_WithValidId_DelegatesToService`: `doNothing().when(mockService).togglePickupLocation(TEST_PICKUP_LOCATION_ID);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 7: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 153 in `togglePickupLocation_NotFound_ThrowsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: togglePickupLocation_MaxLongId_ThrowsNotFoundException, togglePickupLocation_MinLongId_ThrowsNotFoundException, togglePickupLocation_NegativeId_ThrowsNotFoundException, togglePickupLocation_NotFound_ThrowsNotFoundException, togglePickupLocation_ZeroId_ThrowsNotFoundException, togglePickupLocation_PreservesOtherProperties_Success, togglePickupLocation_VeryLargeId_ThrowsNotFoundException, togglePickupLocation_ClientIsolation_Success, togglePickupLocation_ThreeConsecutiveToggles_Success
- Required order: togglePickupLocation_ClientIsolation_Success, togglePickupLocation_MaxLongId_ThrowsNotFoundException, togglePickupLocation_MinLongId_ThrowsNotFoundException, togglePickupLocation_NegativeId_ThrowsNotFoundException, togglePickupLocation_NotFound_ThrowsNotFoundException, togglePickupLocation_PreservesOtherProperties_Success, togglePickupLocation_ThreeConsecutiveToggles_Success, togglePickupLocation_VeryLargeId_ThrowsNotFoundException, togglePickupLocation_ZeroId_ThrowsNotFoundException

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 7 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/GetPickupLocationByIdTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PickupLocation
Class: class GetPickupLocationByIdTest extends PickupLocationServiceTestBase {
Extends: PickupLocationServiceTestBase
Lines of Code: 220
Last Modified: 2026-02-10 23:11:31
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 11

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 11` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 210 has mock usage `PickupLocationService mockService = mock(PickupLocationService.class);`
- Required: Move mocks to base test file.
- Line: 212 has mock usage `PickupLocationResponseModel mockResponse = mock(PickupLocationResponseModel.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getPickupLocationById_controller_permission_forbidden` or `getPickupLocationById_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 40 method `getPickupLocationById_Success`
- Required rename: `getPickupLocationById_Success_Success`
- Line: 199 method `getPickupLocationById_VerifyPreAuthorizeAnnotation`
- Required rename: `getPickupLocationById_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getPickupLocationById_WithValidId_DelegatesToService (line 207)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 40 in `getPickupLocationById_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 65 in `getPickupLocationById_MaxLongId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 81 in `getPickupLocationById_MinLongId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 96 in `getPickupLocationById_NegativeId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 111 in `getPickupLocationById_NotFound_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 125 in `getPickupLocationById_ZeroId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 139 in `getPickupLocationById_ActiveLocation_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 160 in `getPickupLocationById_VeryLargeId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 179 in `getPickupLocationById_VerifyRepositoryCall_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 199 in `getPickupLocationById_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 209 in `getPickupLocationById_WithValidId_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 41 inline mock in `getPickupLocationById_Success`: `when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 66 inline mock in `getPickupLocationById_MaxLongId_ThrowsNotFoundException`: `when(pickupLocationRepository.findPickupLocationByIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 82 inline mock in `getPickupLocationById_MinLongId_ThrowsNotFoundException`: `when(pickupLocationRepository.findPickupLocationByIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 97 inline mock in `getPickupLocationById_NegativeId_ThrowsNotFoundException`: `when(pickupLocationRepository.findPickupLocationByIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 112 inline mock in `getPickupLocationById_NotFound_ThrowsNotFoundException`: `when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 126 inline mock in `getPickupLocationById_ZeroId_ThrowsNotFoundException`: `when(pickupLocationRepository.findPickupLocationByIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 142 inline mock in `getPickupLocationById_ActiveLocation_Success`: `when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 163 inline mock in `getPickupLocationById_VeryLargeId_ThrowsNotFoundException`: `when(pickupLocationRepository.findPickupLocationByIdAndClientId(largeId, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 181 inline mock in `getPickupLocationById_VerifyRepositoryCall_Success`: `when(pickupLocationRepository.findPickupLocationByIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 213 inline mock in `getPickupLocationById_WithValidId_DelegatesToService`: `when(mockService.getPickupLocationById(TEST_PICKUP_LOCATION_ID)).thenReturn(mockResponse);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 114 in `getPickupLocationById_NotFound_ThrowsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 9: Rule 8 - Error Constants
- Severity: HIGH
- Line: 70 has hardcoded message: `assertTrue(ex.getMessage().contains("Pickup location not found"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 70 has hardcoded message: `assertTrue(ex.getMessage().contains("Pickup location not found"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 86 has hardcoded message: `assertTrue(ex.getMessage().contains("Pickup location not found"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 86 has hardcoded message: `assertTrue(ex.getMessage().contains("Pickup location not found"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 100 has hardcoded message: `assertTrue(ex.getMessage().contains("Pickup location not found"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 100 has hardcoded message: `assertTrue(ex.getMessage().contains("Pickup location not found"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 129 has hardcoded message: `assertTrue(ex.getMessage().contains("Pickup location not found"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 129 has hardcoded message: `assertTrue(ex.getMessage().contains("Pickup location not found"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 169 has hardcoded message: `assertTrue(ex.getMessage().contains("Pickup location not found"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 169 has hardcoded message: `assertTrue(ex.getMessage().contains("Pickup location not found"));`
- Required: Replace with an ErrorMessages constant (add one if missing).

VIOLATION 10: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: getPickupLocationById_MaxLongId_ThrowsNotFoundException, getPickupLocationById_MinLongId_ThrowsNotFoundException, getPickupLocationById_NegativeId_ThrowsNotFoundException, getPickupLocationById_NotFound_ThrowsNotFoundException, getPickupLocationById_ZeroId_ThrowsNotFoundException, getPickupLocationById_ActiveLocation_Success, getPickupLocationById_VeryLargeId_ThrowsNotFoundException, getPickupLocationById_VerifyRepositoryCall_Success
- Required order: getPickupLocationById_ActiveLocation_Success, getPickupLocationById_MaxLongId_ThrowsNotFoundException, getPickupLocationById_MinLongId_ThrowsNotFoundException, getPickupLocationById_NegativeId_ThrowsNotFoundException, getPickupLocationById_NotFound_ThrowsNotFoundException, getPickupLocationById_VerifyRepositoryCall_Success, getPickupLocationById_VeryLargeId_ThrowsNotFoundException, getPickupLocationById_ZeroId_ThrowsNotFoundException

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 7 issues above.
- Fix Rule 8 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/BulkCreatePickupLocationsAsyncTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PickupLocation
Class: class BulkCreatePickupLocationsAsyncTest extends PickupLocationServiceTestBase {
Extends: PickupLocationServiceTestBase
Lines of Code: 340
Last Modified: 2026-02-11 01:13:18
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 13

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 13` immediately after the class opening brace.

VIOLATION 2: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 315 method `bulkCreatePickupLocationsAsync_VerifyPreAuthorizeAnnotation`
- Required rename: `bulkCreatePickupLocationsAsync_VerifyPreAuthorizeAnnotation_Success`
- Line: 330 method `bulkCreatePickupLocationsAsync_MethodIsNotNullReturnType`
- Required rename: `bulkCreatePickupLocationsAsync_MethodIsNotNullReturnType_Success`

VIOLATION 3: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: bulkCreatePickupLocationsAsync_VerifyPreAuthorizeAnnotation (line 313), bulkCreatePickupLocationsAsync_MethodIsNotNullReturnType (line 328)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 4: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 41 in `bulkCreatePickupLocationsAsync_AllValid_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 67 in `bulkCreatePickupLocationsAsync_MixedValidAndInvalid_PartialSuccess` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 98 in `bulkCreatePickupLocationsAsync_LargeBatch100Items_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 124 in `bulkCreatePickupLocationsAsync_InternationalCharacters_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 158 in `bulkCreatePickupLocationsAsync_NullList_HandlesErrorGracefully` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 171 in `bulkCreatePickupLocationsAsync_EmptyList_HandlesErrorGracefully` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 187 in `bulkCreatePickupLocationsAsync_RequestingUserIdCaptured_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 213 in `bulkCreatePickupLocationsAsync_RequestingUserNameCaptured_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 239 in `bulkCreatePickupLocationsAsync_ClientIdIsolation_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 266 in `bulkCreatePickupLocationsAsync_RepositoryError_FailureLogged` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 296 in `bulkCreatePickupLocationsAsync_controller_permission_unauthorized` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 315 in `bulkCreatePickupLocationsAsync_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 330 in `bulkCreatePickupLocationsAsync_MethodIsNotNullReturnType` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 5: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 48 inline mock in `bulkCreatePickupLocationsAsync_AllValid_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 49 inline mock in `bulkCreatePickupLocationsAsync_AllValid_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 78 inline mock in `bulkCreatePickupLocationsAsync_MixedValidAndInvalid_PartialSuccess`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 79 inline mock in `bulkCreatePickupLocationsAsync_MixedValidAndInvalid_PartialSuccess`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 105 inline mock in `bulkCreatePickupLocationsAsync_LargeBatch100Items_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 106 inline mock in `bulkCreatePickupLocationsAsync_LargeBatch100Items_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 136 inline mock in `bulkCreatePickupLocationsAsync_InternationalCharacters_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 137 inline mock in `bulkCreatePickupLocationsAsync_InternationalCharacters_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 193 inline mock in `bulkCreatePickupLocationsAsync_RequestingUserIdCaptured_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 194 inline mock in `bulkCreatePickupLocationsAsync_RequestingUserIdCaptured_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 219 inline mock in `bulkCreatePickupLocationsAsync_RequestingUserNameCaptured_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 220 inline mock in `bulkCreatePickupLocationsAsync_RequestingUserNameCaptured_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 245 inline mock in `bulkCreatePickupLocationsAsync_ClientIdIsolation_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 246 inline mock in `bulkCreatePickupLocationsAsync_ClientIdIsolation_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 271 inline mock in `bulkCreatePickupLocationsAsync_RepositoryError_FailureLogged`: `when(addressRepository.save(any())).thenThrow(new RuntimeException("DB Connection Error"));`
- Required: Move to base test stub method and call stub in test.
- Line: 300 inline mock in `bulkCreatePickupLocationsAsync_controller_permission_unauthorized`: `when(pickupLocationServiceMock.getUserId()).thenReturn(TEST_USER_ID);`
- Required: Move to base test stub method and call stub in test.
- Line: 301 inline mock in `bulkCreatePickupLocationsAsync_controller_permission_unauthorized`: `when(pickupLocationServiceMock.getUser()).thenReturn("testuser");`
- Required: Move to base test stub method and call stub in test.
- Line: 302 inline mock in `bulkCreatePickupLocationsAsync_controller_permission_unauthorized`: `when(pickupLocationServiceMock.getClientId()).thenReturn(TEST_CLIENT_ID);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 6: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: bulkCreatePickupLocationsAsync_AllValid_Success, bulkCreatePickupLocationsAsync_MixedValidAndInvalid_PartialSuccess, bulkCreatePickupLocationsAsync_LargeBatch100Items_Success, bulkCreatePickupLocationsAsync_InternationalCharacters_Success
- Required order: bulkCreatePickupLocationsAsync_AllValid_Success, bulkCreatePickupLocationsAsync_InternationalCharacters_Success, bulkCreatePickupLocationsAsync_LargeBatch100Items_Success, bulkCreatePickupLocationsAsync_MixedValidAndInvalid_PartialSuccess

VIOLATION 7: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: bulkCreatePickupLocationsAsync_NullList_HandlesErrorGracefully, bulkCreatePickupLocationsAsync_EmptyList_HandlesErrorGracefully, bulkCreatePickupLocationsAsync_RequestingUserIdCaptured_Success, bulkCreatePickupLocationsAsync_RequestingUserNameCaptured_Success, bulkCreatePickupLocationsAsync_ClientIdIsolation_Success, bulkCreatePickupLocationsAsync_RepositoryError_FailureLogged
- Required order: bulkCreatePickupLocationsAsync_ClientIdIsolation_Success, bulkCreatePickupLocationsAsync_EmptyList_HandlesErrorGracefully, bulkCreatePickupLocationsAsync_NullList_HandlesErrorGracefully, bulkCreatePickupLocationsAsync_RepositoryError_FailureLogged, bulkCreatePickupLocationsAsync_RequestingUserIdCaptured_Success, bulkCreatePickupLocationsAsync_RequestingUserNameCaptured_Success

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section PERMISSION not alphabetical.
- Current order: bulkCreatePickupLocationsAsync_controller_permission_unauthorized, bulkCreatePickupLocationsAsync_VerifyPreAuthorizeAnnotation, bulkCreatePickupLocationsAsync_MethodIsNotNullReturnType
- Required order: bulkCreatePickupLocationsAsync_controller_permission_unauthorized, bulkCreatePickupLocationsAsync_MethodIsNotNullReturnType, bulkCreatePickupLocationsAsync_VerifyPreAuthorizeAnnotation

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.


**IMPLEMENTATION PLAN (STEP-BY-STEP)**
1. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/BulkCreatePickupLocationsTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
2. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/UpdatePickupLocationTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
3. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/GetPickupLocationsInBatchesTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
4. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/CreatePickupLocationTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
5. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/TogglePickupLocationTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
6. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/GetPickupLocationByIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
7. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/BulkCreatePickupLocationsAsyncTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.

Verification Commands (run after fixes):
- mvn -Dtest=BulkCreatePickupLocationsTest test
- mvn -Dtest=UpdatePickupLocationTest test
- mvn -Dtest=GetPickupLocationsInBatchesTest test
- mvn -Dtest=CreatePickupLocationTest test
- mvn -Dtest=TogglePickupLocationTest test
- mvn -Dtest=GetPickupLocationByIdTest test