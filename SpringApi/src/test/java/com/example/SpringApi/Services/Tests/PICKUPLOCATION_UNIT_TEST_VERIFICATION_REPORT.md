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
║  Total Violations: 66                                    ║
╚════════════════════════════════════════════════════════════╝
```

VIOLATIONS BY RULE:

| Rule | Description | Count |
| --- | --- | --- |
| 2 | Test Count Declaration | 7 |
| 3 | Controller Permission Test | 7 |
| 5 | Test Naming Convention | 7 |
| 6 | Centralized Mocking | 6 |
| 7 | Exception Assertions | 3 |
| 8 | Error Constants | 3 |
| 9 | Test Documentation | 7 |
| 10 | Test Ordering | 9 |
| 11 | Complete Coverage | 1 |
| 12 | Arrange/Act/Assert | 7 |
| 14 | No Inline Mocks | 9 |


**BASE TEST FILE ISSUES**
Base Test: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/PickupLocationServiceTestBase.java`
- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line 121: `lenient().when(addressRepository.findById(TEST_ADDRESS_ID))`. Move this into a `stub...` method.
- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line 148: `lenient().when(shipRocketHelper.addPickupLocation(any(PickupLocation.class)))`. Move this into a `stub...` method.


**FILE-BY-FILE BREAKDOWN**

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/BulkCreatePickupLocationsTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PickupLocation
Class: class BulkCreatePickupLocationsTest extends PickupLocationServiceTestBase {
Extends: PickupLocationServiceTestBase
Lines of Code: 455
Last Modified: 2026-02-10 21:05:42
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 21

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 21` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 441 has mock usage `PickupLocationService mockService = mock(PickupLocationService.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `bulkCreatePickupLocations_controller_permission_forbidden` or `bulkCreatePickupLocations_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 430 method `bulkCreatePickupLocations_VerifyPreAuthorizeAnnotation`
- Required rename: `bulkCreatePickupLocations_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: bulkCreatePickupLocations_WithValidRequests_DelegatesToService (line 438)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 45 in `bulkCreatePickupLocations_AllValid_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 70 in `bulkCreatePickupLocations_LargeBatch_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 92 in `bulkCreatePickupLocations_MixedInvalidAndValid_PartialSuccess` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 120 in `bulkCreatePickupLocations_SingleValidItem_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 145 in `bulkCreatePickupLocations_AllInvalidAddresses_AllFail` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 166 in `bulkCreatePickupLocations_DatabaseError_RecordsFailure` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 183 in `bulkCreatePickupLocations_EmptyList_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 197 in `bulkCreatePickupLocations_MissingAddressObject_Fails` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 211 in `bulkCreatePickupLocations_MissingCity_Fails` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 225 in `bulkCreatePickupLocations_MissingPhone_Fails` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 239 in `bulkCreatePickupLocations_NullNickname_Fails` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 253 in `bulkCreatePickupLocations_NullList_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 266 in `bulkCreatePickupLocations_NegativeShipRocketId_Fails` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 280 in `bulkCreatePickupLocations_DuplicateIds_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 303 in `bulkCreatePickupLocations_LongNicknames_PartialSuccess` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 333 in `bulkCreatePickupLocations_MixedShipRocketIds_PartialSuccess` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 359 in `bulkCreatePickupLocations_InternationalAddresses_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 386 in `bulkCreatePickupLocations_Batch100Items_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 410 in `bulkCreatePickupLocations_RepositoryReturnsNull_GracefulHandling` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 430 in `bulkCreatePickupLocations_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 440 in `bulkCreatePickupLocations_WithValidRequests_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 51 inline mock in `bulkCreatePickupLocations_AllValid_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 52 inline mock in `bulkCreatePickupLocations_AllValid_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 53 inline mock in `bulkCreatePickupLocations_AllValid_Success`: `lenient().when(shipRocketHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);`
- Required: Move to base test stub method and call stub in test.
- Line: 76 inline mock in `bulkCreatePickupLocations_LargeBatch_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 77 inline mock in `bulkCreatePickupLocations_LargeBatch_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 78 inline mock in `bulkCreatePickupLocations_LargeBatch_Success`: `lenient().when(shipRocketHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);`
- Required: Move to base test stub method and call stub in test.
- Line: 102 inline mock in `bulkCreatePickupLocations_MixedInvalidAndValid_PartialSuccess`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 103 inline mock in `bulkCreatePickupLocations_MixedInvalidAndValid_PartialSuccess`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 104 inline mock in `bulkCreatePickupLocations_MixedInvalidAndValid_PartialSuccess`: `lenient().when(shipRocketHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);`
- Required: Move to base test stub method and call stub in test.
- Line: 123 inline mock in `bulkCreatePickupLocations_SingleValidItem_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 124 inline mock in `bulkCreatePickupLocations_SingleValidItem_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 125 inline mock in `bulkCreatePickupLocations_SingleValidItem_Success`: `lenient().when(shipRocketHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);`
- Required: Move to base test stub method and call stub in test.
- Line: 168 inline mock in `bulkCreatePickupLocations_DatabaseError_RecordsFailure`: `when(addressRepository.save(any())).thenThrow(new RuntimeException("DB Error"));`
- Required: Move to base test stub method and call stub in test.
- Line: 286 inline mock in `bulkCreatePickupLocations_DuplicateIds_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 287 inline mock in `bulkCreatePickupLocations_DuplicateIds_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 316 inline mock in `bulkCreatePickupLocations_LongNicknames_PartialSuccess`: `lenient().when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 317 inline mock in `bulkCreatePickupLocations_LongNicknames_PartialSuccess`: `lenient().when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 342 inline mock in `bulkCreatePickupLocations_MixedShipRocketIds_PartialSuccess`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 343 inline mock in `bulkCreatePickupLocations_MixedShipRocketIds_PartialSuccess`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 369 inline mock in `bulkCreatePickupLocations_InternationalAddresses_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 370 inline mock in `bulkCreatePickupLocations_InternationalAddresses_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 393 inline mock in `bulkCreatePickupLocations_Batch100Items_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 394 inline mock in `bulkCreatePickupLocations_Batch100Items_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 413 inline mock in `bulkCreatePickupLocations_RepositoryReturnsNull_GracefulHandling`: `when(addressRepository.save(any())).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 445 inline mock in `bulkCreatePickupLocations_WithValidRequests_DelegatesToService`: `when(mockService.getUserId()).thenReturn(1L);`
- Required: Move to base test stub method and call stub in test.
- Line: 446 inline mock in `bulkCreatePickupLocations_WithValidRequests_DelegatesToService`: `when(mockService.getUser()).thenReturn("testuser");`
- Required: Move to base test stub method and call stub in test.
- Line: 447 inline mock in `bulkCreatePickupLocations_WithValidRequests_DelegatesToService`: `when(mockService.getClientId()).thenReturn(TEST_CLIENT_ID);`
- Required: Move to base test stub method and call stub in test.
- Line: 448 inline mock in `bulkCreatePickupLocations_WithValidRequests_DelegatesToService`: `doNothing().when(mockService).bulkCreatePickupLocationsAsync(eq(requests), anyLong(), anyString(), anyLong());`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 8 - Error Constants
- Severity: HIGH
- Line: 187 has hardcoded message: `assertTrue(ex.getMessage().contains("list cannot be null or empty"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 187 has hardcoded message: `assertTrue(ex.getMessage().contains("list cannot be null or empty"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 256 has hardcoded message: `assertTrue(ex.getMessage().contains("list cannot be null or empty"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 256 has hardcoded message: `assertTrue(ex.getMessage().contains("list cannot be null or empty"));`
- Required: Replace with an ErrorMessages constant (add one if missing).

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: bulkCreatePickupLocations_AllInvalidAddresses_AllFail, bulkCreatePickupLocations_DatabaseError_RecordsFailure, bulkCreatePickupLocations_EmptyList_ThrowsBadRequestException, bulkCreatePickupLocations_MissingAddressObject_Fails, bulkCreatePickupLocations_MissingCity_Fails, bulkCreatePickupLocations_MissingPhone_Fails, bulkCreatePickupLocations_NullNickname_Fails, bulkCreatePickupLocations_NullList_ThrowsBadRequestException, bulkCreatePickupLocations_NegativeShipRocketId_Fails, bulkCreatePickupLocations_DuplicateIds_Success, bulkCreatePickupLocations_LongNicknames_PartialSuccess, bulkCreatePickupLocations_MixedShipRocketIds_PartialSuccess, bulkCreatePickupLocations_InternationalAddresses_Success, bulkCreatePickupLocations_Batch100Items_Success, bulkCreatePickupLocations_RepositoryReturnsNull_GracefulHandling
- Required order: bulkCreatePickupLocations_AllInvalidAddresses_AllFail, bulkCreatePickupLocations_Batch100Items_Success, bulkCreatePickupLocations_DatabaseError_RecordsFailure, bulkCreatePickupLocations_DuplicateIds_Success, bulkCreatePickupLocations_EmptyList_ThrowsBadRequestException, bulkCreatePickupLocations_InternationalAddresses_Success, bulkCreatePickupLocations_LongNicknames_PartialSuccess, bulkCreatePickupLocations_MissingAddressObject_Fails, bulkCreatePickupLocations_MissingCity_Fails, bulkCreatePickupLocations_MissingPhone_Fails, bulkCreatePickupLocations_MixedShipRocketIds_PartialSuccess, bulkCreatePickupLocations_NegativeShipRocketId_Fails, bulkCreatePickupLocations_NullList_ThrowsBadRequestException, bulkCreatePickupLocations_NullNickname_Fails, bulkCreatePickupLocations_RepositoryReturnsNull_GracefulHandling

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

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/UpdatePickupLocationTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PickupLocation
Class: class UpdatePickupLocationTest extends PickupLocationServiceTestBase {
Extends: PickupLocationServiceTestBase
Lines of Code: 694
Last Modified: 2026-02-10 17:40:23
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 33

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 33` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 684 has mock usage `PickupLocationService mockService = mock(PickupLocationService.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `updatePickupLocation_controller_permission_forbidden` or `updatePickupLocation_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 45 method `updatePickupLocation_Success`
- Required rename: `updatePickupLocation_Success_Success`
- Line: 672 method `updatePickupLocation_VerifyPreAuthorizeAnnotation`
- Required rename: `updatePickupLocation_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: updatePickupLocation_WithValidRequest_DelegatesToService (line 681)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 45 in `updatePickupLocation_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 72 in `updatePickupLocation_UpdatesShipRocketId_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 105 in `updatePickupLocation_MaxLongId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 122 in `updatePickupLocation_MinLongId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 139 in `updatePickupLocation_NegativeId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 155 in `updatePickupLocation_NullAddress_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 174 in `updatePickupLocation_NullCity_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 193 in `updatePickupLocation_NullCountry_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 212 in `updatePickupLocation_NullNickname_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 231 in `updatePickupLocation_NullPhone_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 252 in `updatePickupLocation_NullPostalCode_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 271 in `updatePickupLocation_NullRequest_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 284 in `updatePickupLocation_NullState_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 303 in `updatePickupLocation_NullStreetAddress_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 322 in `updatePickupLocation_WhitespaceCity_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 341 in `updatePickupLocation_WhitespaceCountry_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 360 in `updatePickupLocation_WhitespaceNickname_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 379 in `updatePickupLocation_WhitespacePhone_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 400 in `updatePickupLocation_WhitespacePostalCode_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 419 in `updatePickupLocation_WhitespaceState_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 438 in `updatePickupLocation_WhitespaceStreetAddress_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 457 in `updatePickupLocation_ZeroId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 474 in `updatePickupLocation_PreservesCreatedTimestamp_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 498 in `updatePickupLocation_ModifiesOnlyChangedFields_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 523 in `updatePickupLocation_VeryLongNickname_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 544 in `updatePickupLocation_SpecialCharactersInAddress_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 565 in `updatePickupLocation_InternationalAddress_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 587 in `updatePickupLocation_VeryLargeId_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 609 in `updatePickupLocation_VerifyClientIsolation_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 632 in `updatePickupLocation_RepositoryReturnsNull_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 651 in `updatePickupLocation_NumericPostalCode_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 672 in `updatePickupLocation_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 683 in `updatePickupLocation_WithValidRequest_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 51 inline mock in `updatePickupLocation_Success`: `.when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 55 inline mock in `updatePickupLocation_Success`: `when(addressRepository.findById(any())).thenReturn(Optional.of(testAddress));`
- Required: Move to base test stub method and call stub in test.
- Line: 56 inline mock in `updatePickupLocation_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 57 inline mock in `updatePickupLocation_Success`: `when(pickupLocationRepository.save(any())).thenReturn(existingPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 77 inline mock in `updatePickupLocation_UpdatesShipRocketId_Success`: `.when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 81 inline mock in `updatePickupLocation_UpdatesShipRocketId_Success`: `when(addressRepository.findById(any())).thenReturn(Optional.of(testAddress));`
- Required: Move to base test stub method and call stub in test.
- Line: 82 inline mock in `updatePickupLocation_UpdatesShipRocketId_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 83 inline mock in `updatePickupLocation_UpdatesShipRocketId_Success`: `when(pickupLocationRepository.save(any())).thenReturn(existing);`
- Required: Move to base test stub method and call stub in test.
- Line: 107 inline mock in `updatePickupLocation_MaxLongId_ThrowsNotFoundException`: `lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(Long.MAX_VALUE,`
- Required: Move to base test stub method and call stub in test.
- Line: 124 inline mock in `updatePickupLocation_MinLongId_ThrowsNotFoundException`: `lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(Long.MIN_VALUE,`
- Required: Move to base test stub method and call stub in test.
- Line: 141 inline mock in `updatePickupLocation_NegativeId_ThrowsNotFoundException`: `lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(-100L, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 157 inline mock in `updatePickupLocation_NullAddress_ThrowsBadRequestException`: `.when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 176 inline mock in `updatePickupLocation_NullCity_ThrowsBadRequestException`: `.when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 195 inline mock in `updatePickupLocation_NullCountry_ThrowsBadRequestException`: `.when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 214 inline mock in `updatePickupLocation_NullNickname_ThrowsBadRequestException`: `.when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 233 inline mock in `updatePickupLocation_NullPhone_Success`: `.when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 238 inline mock in `updatePickupLocation_NullPhone_Success`: `when(addressRepository.findById(any())).thenReturn(Optional.of(testAddress));`
- Required: Move to base test stub method and call stub in test.
- Line: 239 inline mock in `updatePickupLocation_NullPhone_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 240 inline mock in `updatePickupLocation_NullPhone_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 254 inline mock in `updatePickupLocation_NullPostalCode_ThrowsBadRequestException`: `.when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 286 inline mock in `updatePickupLocation_NullState_ThrowsBadRequestException`: `.when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 305 inline mock in `updatePickupLocation_NullStreetAddress_ThrowsBadRequestException`: `.when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 324 inline mock in `updatePickupLocation_WhitespaceCity_ThrowsBadRequestException`: `.when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 343 inline mock in `updatePickupLocation_WhitespaceCountry_ThrowsBadRequestException`: `.when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 362 inline mock in `updatePickupLocation_WhitespaceNickname_ThrowsBadRequestException`: `.when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 381 inline mock in `updatePickupLocation_WhitespacePhone_Success`: `.when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 386 inline mock in `updatePickupLocation_WhitespacePhone_Success`: `when(addressRepository.findById(any())).thenReturn(Optional.of(testAddress));`
- Required: Move to base test stub method and call stub in test.
- Line: 387 inline mock in `updatePickupLocation_WhitespacePhone_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 388 inline mock in `updatePickupLocation_WhitespacePhone_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 402 inline mock in `updatePickupLocation_WhitespacePostalCode_ThrowsBadRequestException`: `.when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 421 inline mock in `updatePickupLocation_WhitespaceState_ThrowsBadRequestException`: `.when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 440 inline mock in `updatePickupLocation_WhitespaceStreetAddress_ThrowsBadRequestException`: `.when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 459 inline mock in `updatePickupLocation_ZeroId_ThrowsNotFoundException`: `lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(0L, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 477 inline mock in `updatePickupLocation_PreservesCreatedTimestamp_Success`: `lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 480 inline mock in `updatePickupLocation_PreservesCreatedTimestamp_Success`: `lenient().when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 481 inline mock in `updatePickupLocation_PreservesCreatedTimestamp_Success`: `lenient().when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 502 inline mock in `updatePickupLocation_ModifiesOnlyChangedFields_Success`: `lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 505 inline mock in `updatePickupLocation_ModifiesOnlyChangedFields_Success`: `lenient().when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 506 inline mock in `updatePickupLocation_ModifiesOnlyChangedFields_Success`: `lenient().when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 527 inline mock in `updatePickupLocation_VeryLongNickname_Success`: `lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 530 inline mock in `updatePickupLocation_VeryLongNickname_Success`: `lenient().when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 531 inline mock in `updatePickupLocation_VeryLongNickname_Success`: `lenient().when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 547 inline mock in `updatePickupLocation_SpecialCharactersInAddress_Success`: `lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 550 inline mock in `updatePickupLocation_SpecialCharactersInAddress_Success`: `lenient().when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 551 inline mock in `updatePickupLocation_SpecialCharactersInAddress_Success`: `lenient().when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 552 inline mock in `updatePickupLocation_SpecialCharactersInAddress_Success`: `lenient().when(shipRocketHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);`
- Required: Move to base test stub method and call stub in test.
- Line: 569 inline mock in `updatePickupLocation_InternationalAddress_Success`: `lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 572 inline mock in `updatePickupLocation_InternationalAddress_Success`: `lenient().when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 573 inline mock in `updatePickupLocation_InternationalAddress_Success`: `lenient().when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 574 inline mock in `updatePickupLocation_InternationalAddress_Success`: `lenient().when(shipRocketHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);`
- Required: Move to base test stub method and call stub in test.
- Line: 592 inline mock in `updatePickupLocation_VeryLargeId_Success`: `lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 595 inline mock in `updatePickupLocation_VeryLargeId_Success`: `lenient().when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 596 inline mock in `updatePickupLocation_VeryLargeId_Success`: `lenient().when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 611 inline mock in `updatePickupLocation_VerifyClientIsolation_Success`: `lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 614 inline mock in `updatePickupLocation_VerifyClientIsolation_Success`: `lenient().when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 615 inline mock in `updatePickupLocation_VerifyClientIsolation_Success`: `lenient().when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 634 inline mock in `updatePickupLocation_RepositoryReturnsNull_Success`: `lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 637 inline mock in `updatePickupLocation_RepositoryReturnsNull_Success`: `lenient().when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 638 inline mock in `updatePickupLocation_RepositoryReturnsNull_Success`: `lenient().when(pickupLocationRepository.save(any())).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 654 inline mock in `updatePickupLocation_NumericPostalCode_Success`: `lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(`
- Required: Move to base test stub method and call stub in test.
- Line: 657 inline mock in `updatePickupLocation_NumericPostalCode_Success`: `lenient().when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 658 inline mock in `updatePickupLocation_NumericPostalCode_Success`: `lenient().when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 686 inline mock in `updatePickupLocation_WithValidRequest_DelegatesToService`: `doNothing().when(mockService).updatePickupLocation(testPickupLocationRequest);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 8 - Error Constants
- Severity: HIGH
- Line: 145 has hardcoded message: `assertTrue(ex.getMessage().contains("-100"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 145 has hardcoded message: `assertTrue(ex.getMessage().contains("-100"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 274 has hardcoded message: `assertEquals("Pickup location request cannot be null.", ex.getMessage());`
- Required: Replace with `ErrorMessages.PickupLocationErrorMessages.InvalidRequest`.
- Line: 463 has hardcoded message: `assertTrue(ex.getMessage().contains("0"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 463 has hardcoded message: `assertTrue(ex.getMessage().contains("0"));`
- Required: Replace with an ErrorMessages constant (add one if missing).

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: updatePickupLocation_MaxLongId_ThrowsNotFoundException, updatePickupLocation_MinLongId_ThrowsNotFoundException, updatePickupLocation_NegativeId_ThrowsNotFoundException, updatePickupLocation_NullAddress_ThrowsBadRequestException, updatePickupLocation_NullCity_ThrowsBadRequestException, updatePickupLocation_NullCountry_ThrowsBadRequestException, updatePickupLocation_NullNickname_ThrowsBadRequestException, updatePickupLocation_NullPhone_Success, updatePickupLocation_NullPostalCode_ThrowsBadRequestException, updatePickupLocation_NullRequest_ThrowsBadRequestException, updatePickupLocation_NullState_ThrowsBadRequestException, updatePickupLocation_NullStreetAddress_ThrowsBadRequestException, updatePickupLocation_WhitespaceCity_ThrowsBadRequestException, updatePickupLocation_WhitespaceCountry_ThrowsBadRequestException, updatePickupLocation_WhitespaceNickname_ThrowsBadRequestException, updatePickupLocation_WhitespacePhone_Success, updatePickupLocation_WhitespacePostalCode_ThrowsBadRequestException, updatePickupLocation_WhitespaceState_ThrowsBadRequestException, updatePickupLocation_WhitespaceStreetAddress_ThrowsBadRequestException, updatePickupLocation_ZeroId_ThrowsNotFoundException, updatePickupLocation_PreservesCreatedTimestamp_Success, updatePickupLocation_ModifiesOnlyChangedFields_Success, updatePickupLocation_VeryLongNickname_Success, updatePickupLocation_SpecialCharactersInAddress_Success, updatePickupLocation_InternationalAddress_Success, updatePickupLocation_VeryLargeId_Success, updatePickupLocation_VerifyClientIsolation_Success, updatePickupLocation_RepositoryReturnsNull_Success, updatePickupLocation_NumericPostalCode_Success
- Required order: updatePickupLocation_InternationalAddress_Success, updatePickupLocation_MaxLongId_ThrowsNotFoundException, updatePickupLocation_MinLongId_ThrowsNotFoundException, updatePickupLocation_ModifiesOnlyChangedFields_Success, updatePickupLocation_NegativeId_ThrowsNotFoundException, updatePickupLocation_NullAddress_ThrowsBadRequestException, updatePickupLocation_NullCity_ThrowsBadRequestException, updatePickupLocation_NullCountry_ThrowsBadRequestException, updatePickupLocation_NullNickname_ThrowsBadRequestException, updatePickupLocation_NullPhone_Success, updatePickupLocation_NullPostalCode_ThrowsBadRequestException, updatePickupLocation_NullRequest_ThrowsBadRequestException, updatePickupLocation_NullState_ThrowsBadRequestException, updatePickupLocation_NullStreetAddress_ThrowsBadRequestException, updatePickupLocation_NumericPostalCode_Success, updatePickupLocation_PreservesCreatedTimestamp_Success, updatePickupLocation_RepositoryReturnsNull_Success, updatePickupLocation_SpecialCharactersInAddress_Success, updatePickupLocation_VerifyClientIsolation_Success, updatePickupLocation_VeryLargeId_Success, updatePickupLocation_VeryLongNickname_Success, updatePickupLocation_WhitespaceCity_ThrowsBadRequestException, updatePickupLocation_WhitespaceCountry_ThrowsBadRequestException, updatePickupLocation_WhitespaceNickname_ThrowsBadRequestException, updatePickupLocation_WhitespacePhone_Success, updatePickupLocation_WhitespacePostalCode_ThrowsBadRequestException, updatePickupLocation_WhitespaceState_ThrowsBadRequestException, updatePickupLocation_WhitespaceStreetAddress_ThrowsBadRequestException, updatePickupLocation_ZeroId_ThrowsNotFoundException

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

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/GetPickupLocationsInBatchesTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.PickupLocation
Class: class GetPickupLocationsInBatchesTest extends PickupLocationServiceTestBase {
Extends: PickupLocationServiceTestBase
Lines of Code: 339
Last Modified: 2026-02-10 20:20:42
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 13

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 13` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 328 has mock usage `PickupLocationService mockService = mock(PickupLocationService.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getPickupLocationsInBatches_controller_permission_forbidden` or `getPickupLocationsInBatches_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 316 method `getPickupLocationsInBatches_VerifyPreAuthorizeAnnotation`
- Required rename: `getPickupLocationsInBatches_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getPickupLocationsInBatches_WithValidRequest_DelegatesToService (line 325)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
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
- Line: 316 in `getPickupLocationsInBatches_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 327 in `getPickupLocationsInBatches_WithValidRequest_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
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
- Line: 331 inline mock in `getPickupLocationsInBatches_WithValidRequest_DelegatesToService`: `when(mockService.getPickupLocationsInBatches(testPaginationRequest))`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: getPickupLocationsInBatches_EndIndexZero_ThrowsBadRequestException, getPickupLocationsInBatches_NegativeStart_ThrowsBadRequestException, getPickupLocationsInBatches_StartGreaterEqualEnd_ThrowsBadRequestException, getPickupLocationsInBatches_LargeBatchSize_Success, getPickupLocationsInBatches_MinimalRange_Success, getPickupLocationsInBatches_IncludeDeleted_Success, getPickupLocationsInBatches_NegativeEndIndex_ThrowsBadRequestException, getPickupLocationsInBatches_StartEqualsEnd_ThrowsBadRequestException, getPickupLocationsInBatches_EmptyResults_Success, getPickupLocationsInBatches_MultipleResults_Success
- Required order: getPickupLocationsInBatches_EmptyResults_Success, getPickupLocationsInBatches_EndIndexZero_ThrowsBadRequestException, getPickupLocationsInBatches_IncludeDeleted_Success, getPickupLocationsInBatches_LargeBatchSize_Success, getPickupLocationsInBatches_MinimalRange_Success, getPickupLocationsInBatches_MultipleResults_Success, getPickupLocationsInBatches_NegativeEndIndex_ThrowsBadRequestException, getPickupLocationsInBatches_NegativeStart_ThrowsBadRequestException, getPickupLocationsInBatches_StartEqualsEnd_ThrowsBadRequestException, getPickupLocationsInBatches_StartGreaterEqualEnd_ThrowsBadRequestException

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 3 issues above.
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
Lines of Code: 491
Last Modified: 2026-02-10 17:40:23
Declared Test Count: MISSING/MISPLACED (first occurrence line 28)
Actual @Test Count: 29

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 28
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 29` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 482 has mock usage `PickupLocationService mockService = mock(PickupLocationService.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `createPickupLocation_controller_permission_forbidden` or `createPickupLocation_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 62 method `createPickupLocation_Success`
- Required rename: `createPickupLocation_Success_Success`
- Line: 470 method `createPickupLocation_VerifyPreAuthorizeAnnotation`
- Required rename: `createPickupLocation_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: createPickupLocation_WithValidRequest_DelegatesToService (line 479)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
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
- Line: 331 in `createPickupLocation_SpecialCharactersInAddress_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 347 in `createPickupLocation_InternationalCharacters_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 364 in `createPickupLocation_NumericPostalCode_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 380 in `createPickupLocation_AlphanumericPostalCode_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 396 in `createPickupLocation_LongPhoneNumber_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 412 in `createPickupLocation_RepositoryErrorOnAddressSave_ThrowsException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 427 in `createPickupLocation_RepositoryErrorOnLocationSave_ThrowsException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 450 in `createPickupLocation_IsDeletedFlagFalse_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 470 in `createPickupLocation_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 481 in `createPickupLocation_WithValidRequest_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 414 inline mock in `createPickupLocation_RepositoryErrorOnAddressSave_ThrowsException`: `when(addressRepository.save(any())).thenThrow(new RuntimeException("Database error"));`
- Required: Move to base test stub method and call stub in test.
- Line: 437 inline mock in `createPickupLocation_RepositoryErrorOnLocationSave_ThrowsException`: `when(pickupLocationRepository.save(any())).thenThrow(new RuntimeException("Database error"));`
- Required: Move to base test stub method and call stub in test.
- Line: 484 inline mock in `createPickupLocation_WithValidRequest_DelegatesToService`: `doNothing().when(mockService).createPickupLocation(testPickupLocationRequest);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 417 in `createPickupLocation_RepositoryErrorOnAddressSave_ThrowsException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 440 in `createPickupLocation_RepositoryErrorOnLocationSave_ThrowsException`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: createPickupLocation_NullAddress_ThrowsBadRequestException, createPickupLocation_NullCity_ThrowsBadRequestException, createPickupLocation_NullCountry_ThrowsBadRequestException, createPickupLocation_NullNickname_ThrowsBadRequestException, createPickupLocation_NullPhone_Success, createPickupLocation_NullPostalCode_ThrowsBadRequestException, createPickupLocation_NullRequest_ThrowsBadRequestException, createPickupLocation_NullState_ThrowsBadRequestException, createPickupLocation_NullStreetAddress_ThrowsBadRequestException, createPickupLocation_WhitespaceCity_ThrowsBadRequestException, createPickupLocation_WhitespaceCountry_ThrowsBadRequestException, createPickupLocation_WhitespaceNickname_ThrowsBadRequestException, createPickupLocation_WhitespacePhone_Success, createPickupLocation_WhitespacePostalCode_ThrowsBadRequestException, createPickupLocation_WhitespaceState_ThrowsBadRequestException, createPickupLocation_WhitespaceStreetAddress_ThrowsBadRequestException, createPickupLocation_VeryLongNickname_Success, createPickupLocation_SpecialCharactersInAddress_Success, createPickupLocation_InternationalCharacters_Success, createPickupLocation_NumericPostalCode_Success, createPickupLocation_AlphanumericPostalCode_Success, createPickupLocation_LongPhoneNumber_Success, createPickupLocation_RepositoryErrorOnAddressSave_ThrowsException, createPickupLocation_RepositoryErrorOnLocationSave_ThrowsException, createPickupLocation_IsDeletedFlagFalse_Success
- Required order: createPickupLocation_AlphanumericPostalCode_Success, createPickupLocation_InternationalCharacters_Success, createPickupLocation_IsDeletedFlagFalse_Success, createPickupLocation_LongPhoneNumber_Success, createPickupLocation_NullAddress_ThrowsBadRequestException, createPickupLocation_NullCity_ThrowsBadRequestException, createPickupLocation_NullCountry_ThrowsBadRequestException, createPickupLocation_NullNickname_ThrowsBadRequestException, createPickupLocation_NullPhone_Success, createPickupLocation_NullPostalCode_ThrowsBadRequestException, createPickupLocation_NullRequest_ThrowsBadRequestException, createPickupLocation_NullState_ThrowsBadRequestException, createPickupLocation_NullStreetAddress_ThrowsBadRequestException, createPickupLocation_NumericPostalCode_Success, createPickupLocation_RepositoryErrorOnAddressSave_ThrowsException, createPickupLocation_RepositoryErrorOnLocationSave_ThrowsException, createPickupLocation_SpecialCharactersInAddress_Success, createPickupLocation_VeryLongNickname_Success, createPickupLocation_WhitespaceCity_ThrowsBadRequestException, createPickupLocation_WhitespaceCountry_ThrowsBadRequestException, createPickupLocation_WhitespaceNickname_ThrowsBadRequestException, createPickupLocation_WhitespacePhone_Success, createPickupLocation_WhitespacePostalCode_ThrowsBadRequestException, createPickupLocation_WhitespaceState_ThrowsBadRequestException, createPickupLocation_WhitespaceStreetAddress_ThrowsBadRequestException

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 3 issues above.
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
Lines of Code: 284
Last Modified: 2026-02-10 17:40:23
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
- Line: 275 has mock usage `PickupLocationService mockService = mock(PickupLocationService.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `togglePickupLocation_controller_permission_forbidden` or `togglePickupLocation_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 80 method `togglePickupLocation_Success`
- Required rename: `togglePickupLocation_Success_Success`
- Line: 264 method `togglePickupLocation_VerifyPreAuthorizeAnnotation`
- Required rename: `togglePickupLocation_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: togglePickupLocation_WithValidId_DelegatesToService (line 272)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
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
- Line: 264 in `togglePickupLocation_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 274 in `togglePickupLocation_WithValidId_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
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
- Line: 277 inline mock in `togglePickupLocation_WithValidId_DelegatesToService`: `doNothing().when(mockService).togglePickupLocation(TEST_PICKUP_LOCATION_ID);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 153 in `togglePickupLocation_NotFound_ThrowsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: togglePickupLocation_MaxLongId_ThrowsNotFoundException, togglePickupLocation_MinLongId_ThrowsNotFoundException, togglePickupLocation_NegativeId_ThrowsNotFoundException, togglePickupLocation_NotFound_ThrowsNotFoundException, togglePickupLocation_ZeroId_ThrowsNotFoundException, togglePickupLocation_PreservesOtherProperties_Success, togglePickupLocation_VeryLargeId_ThrowsNotFoundException, togglePickupLocation_ClientIsolation_Success, togglePickupLocation_ThreeConsecutiveToggles_Success
- Required order: togglePickupLocation_ClientIsolation_Success, togglePickupLocation_MaxLongId_ThrowsNotFoundException, togglePickupLocation_MinLongId_ThrowsNotFoundException, togglePickupLocation_NegativeId_ThrowsNotFoundException, togglePickupLocation_NotFound_ThrowsNotFoundException, togglePickupLocation_PreservesOtherProperties_Success, togglePickupLocation_ThreeConsecutiveToggles_Success, togglePickupLocation_VeryLargeId_ThrowsNotFoundException, togglePickupLocation_ZeroId_ThrowsNotFoundException

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 3 issues above.
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
Last Modified: 2026-02-10 17:40:23
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
Lines of Code: 314
Last Modified: 2026-02-10 21:05:36
Declared Test Count: MISSING/MISPLACED (first occurrence line N/A)
Actual @Test Count: 12

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: N/A
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 12` immediately after the class opening brace.

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `bulkCreatePickupLocationsAsync_controller_permission_forbidden` or `bulkCreatePickupLocationsAsync_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 289 method `bulkCreatePickupLocationsAsync_VerifyPreAuthorizeAnnotation`
- Required rename: `bulkCreatePickupLocationsAsync_VerifyPreAuthorizeAnnotation_Success`
- Line: 304 method `bulkCreatePickupLocationsAsync_MethodIsNotNullReturnType`
- Required rename: `bulkCreatePickupLocationsAsync_MethodIsNotNullReturnType_Success`

VIOLATION 4: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: bulkCreatePickupLocationsAsync_MethodIsNotNullReturnType (line 302)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 5: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 39 in `bulkCreatePickupLocationsAsync_AllValid_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 65 in `bulkCreatePickupLocationsAsync_MixedValidAndInvalid_PartialSuccess` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 96 in `bulkCreatePickupLocationsAsync_LargeBatch100Items_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 122 in `bulkCreatePickupLocationsAsync_InternationalCharacters_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 156 in `bulkCreatePickupLocationsAsync_NullList_HandlesErrorGracefully` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 169 in `bulkCreatePickupLocationsAsync_EmptyList_HandlesErrorGracefully` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 185 in `bulkCreatePickupLocationsAsync_RequestingUserIdCaptured_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 211 in `bulkCreatePickupLocationsAsync_RequestingUserNameCaptured_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 237 in `bulkCreatePickupLocationsAsync_ClientIdIsolation_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 264 in `bulkCreatePickupLocationsAsync_RepositoryError_FailureLogged` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 289 in `bulkCreatePickupLocationsAsync_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 304 in `bulkCreatePickupLocationsAsync_MethodIsNotNullReturnType` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 6: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 46 inline mock in `bulkCreatePickupLocationsAsync_AllValid_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 47 inline mock in `bulkCreatePickupLocationsAsync_AllValid_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 48 inline mock in `bulkCreatePickupLocationsAsync_AllValid_Success`: `lenient().when(shipRocketHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);`
- Required: Move to base test stub method and call stub in test.
- Line: 76 inline mock in `bulkCreatePickupLocationsAsync_MixedValidAndInvalid_PartialSuccess`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 77 inline mock in `bulkCreatePickupLocationsAsync_MixedValidAndInvalid_PartialSuccess`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 78 inline mock in `bulkCreatePickupLocationsAsync_MixedValidAndInvalid_PartialSuccess`: `lenient().when(shipRocketHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);`
- Required: Move to base test stub method and call stub in test.
- Line: 103 inline mock in `bulkCreatePickupLocationsAsync_LargeBatch100Items_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 104 inline mock in `bulkCreatePickupLocationsAsync_LargeBatch100Items_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 105 inline mock in `bulkCreatePickupLocationsAsync_LargeBatch100Items_Success`: `lenient().when(shipRocketHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);`
- Required: Move to base test stub method and call stub in test.
- Line: 134 inline mock in `bulkCreatePickupLocationsAsync_InternationalCharacters_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 135 inline mock in `bulkCreatePickupLocationsAsync_InternationalCharacters_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 136 inline mock in `bulkCreatePickupLocationsAsync_InternationalCharacters_Success`: `lenient().when(shipRocketHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);`
- Required: Move to base test stub method and call stub in test.
- Line: 191 inline mock in `bulkCreatePickupLocationsAsync_RequestingUserIdCaptured_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 192 inline mock in `bulkCreatePickupLocationsAsync_RequestingUserIdCaptured_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 193 inline mock in `bulkCreatePickupLocationsAsync_RequestingUserIdCaptured_Success`: `lenient().when(shipRocketHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);`
- Required: Move to base test stub method and call stub in test.
- Line: 217 inline mock in `bulkCreatePickupLocationsAsync_RequestingUserNameCaptured_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 218 inline mock in `bulkCreatePickupLocationsAsync_RequestingUserNameCaptured_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 219 inline mock in `bulkCreatePickupLocationsAsync_RequestingUserNameCaptured_Success`: `lenient().when(shipRocketHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);`
- Required: Move to base test stub method and call stub in test.
- Line: 243 inline mock in `bulkCreatePickupLocationsAsync_ClientIdIsolation_Success`: `when(addressRepository.save(any())).thenReturn(testAddress);`
- Required: Move to base test stub method and call stub in test.
- Line: 244 inline mock in `bulkCreatePickupLocationsAsync_ClientIdIsolation_Success`: `when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.
- Line: 245 inline mock in `bulkCreatePickupLocationsAsync_ClientIdIsolation_Success`: `lenient().when(shipRocketHelper.addPickupLocation(any())).thenReturn(testShipRocketResponse);`
- Required: Move to base test stub method and call stub in test.
- Line: 269 inline mock in `bulkCreatePickupLocationsAsync_RepositoryError_FailureLogged`: `when(addressRepository.save(any())).thenThrow(new RuntimeException("DB Connection Error"));`
- Required: Move to base test stub method and call stub in test.
- Line: 270 inline mock in `bulkCreatePickupLocationsAsync_RepositoryError_FailureLogged`: `lenient().when(pickupLocationRepository.save(any())).thenReturn(testPickupLocation);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 7: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: bulkCreatePickupLocationsAsync_AllValid_Success, bulkCreatePickupLocationsAsync_MixedValidAndInvalid_PartialSuccess, bulkCreatePickupLocationsAsync_LargeBatch100Items_Success, bulkCreatePickupLocationsAsync_InternationalCharacters_Success
- Required order: bulkCreatePickupLocationsAsync_AllValid_Success, bulkCreatePickupLocationsAsync_InternationalCharacters_Success, bulkCreatePickupLocationsAsync_LargeBatch100Items_Success, bulkCreatePickupLocationsAsync_MixedValidAndInvalid_PartialSuccess

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: bulkCreatePickupLocationsAsync_NullList_HandlesErrorGracefully, bulkCreatePickupLocationsAsync_EmptyList_HandlesErrorGracefully, bulkCreatePickupLocationsAsync_RequestingUserIdCaptured_Success, bulkCreatePickupLocationsAsync_RequestingUserNameCaptured_Success, bulkCreatePickupLocationsAsync_ClientIdIsolation_Success, bulkCreatePickupLocationsAsync_RepositoryError_FailureLogged
- Required order: bulkCreatePickupLocationsAsync_ClientIdIsolation_Success, bulkCreatePickupLocationsAsync_EmptyList_HandlesErrorGracefully, bulkCreatePickupLocationsAsync_NullList_HandlesErrorGracefully, bulkCreatePickupLocationsAsync_RepositoryError_FailureLogged, bulkCreatePickupLocationsAsync_RequestingUserIdCaptured_Success, bulkCreatePickupLocationsAsync_RequestingUserNameCaptured_Success

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section PERMISSION not alphabetical.
- Current order: bulkCreatePickupLocationsAsync_VerifyPreAuthorizeAnnotation, bulkCreatePickupLocationsAsync_MethodIsNotNullReturnType
- Required order: bulkCreatePickupLocationsAsync_MethodIsNotNullReturnType, bulkCreatePickupLocationsAsync_VerifyPreAuthorizeAnnotation

VIOLATION 10: Rule 11 - Complete Coverage
- Severity: HIGH
- Coverage by test names is incomplete.
- Missing: at least one failure/exception test (e.g., *_throws*, *_exception*, *_invalid*).

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.
- Fix Rule 11 issues above.


**IMPLEMENTATION PLAN (STEP-BY-STEP)**
1. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/BulkCreatePickupLocationsTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
2. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/UpdatePickupLocationTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
3. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/GetPickupLocationsInBatchesTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
4. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/CreatePickupLocationTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
5. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/TogglePickupLocationTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
6. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/GetPickupLocationByIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
7. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/BulkCreatePickupLocationsAsyncTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
8. Fix base test `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/PickupLocation/PickupLocationServiceTestBase.java` violations noted above.

Verification Commands (run after fixes):
- mvn -Dtest=BulkCreatePickupLocationsTest test
- mvn -Dtest=UpdatePickupLocationTest test
- mvn -Dtest=GetPickupLocationsInBatchesTest test
- mvn -Dtest=CreatePickupLocationTest test
- mvn -Dtest=TogglePickupLocationTest test
- mvn -Dtest=GetPickupLocationByIdTest test