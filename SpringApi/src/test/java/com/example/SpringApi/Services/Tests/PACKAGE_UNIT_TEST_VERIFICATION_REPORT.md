# UNIT TEST VERIFICATION REPORT — Package

```
╔════════════════════════════════════════════════════════════╗
║           UNIT TEST VERIFICATION REPORT                    ║
║                                                            ║
║  Status: ❌ RED                                               ║
║  Services Analyzed: 1                                      ║
║  Public Methods Found: 8                                  ║
║  Test Files Expected: 8                                  ║
║  Test Files Found: 8                                     ║
║  Total Violations: 64                                    ║
╚════════════════════════════════════════════════════════════╝
```

VIOLATIONS BY RULE:

| Rule | Description | Count |
| --- | --- | --- |
| 2 | Test Count Declaration | 8 |
| 3 | Controller Permission Test | 6 |
| 4 | Test Annotations | 1 |
| 5 | Test Naming Convention | 7 |
| 6 | Centralized Mocking | 1 |
| 7 | Exception Assertions | 3 |
| 8 | Error Constants | 2 |
| 9 | Test Documentation | 7 |
| 10 | Test Ordering | 15 |
| 12 | Arrange/Act/Assert | 7 |
| 14 | No Inline Mocks | 7 |


**FILE-BY-FILE BREAKDOWN**

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Package/TogglePackageTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Package
Class: class TogglePackageTest extends PackageServiceTestBase {
Extends: PackageServiceTestBase
Lines of Code: 275
Last Modified: 2026-02-10 17:47:34
Declared Test Count: 11 (first occurrence line 25)
Actual @Test Count: 14

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 25
- Current: 11
- Required: 14

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `togglePackage_controller_permission_forbidden` or `togglePackage_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 79 method `togglePackage_Success`
- Required rename: `togglePackage_Success_Success`
- Line: 256 method `togglePackage_VerifyPreAuthorizeAnnotation`
- Required rename: `togglePackage_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 4: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: togglePackage_WithValidRequest_DelegatesToService (line 264)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 5: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 40 in `togglePackage_MultipleToggles_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 62 in `togglePackage_RestoreFromDeleted_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 79 in `togglePackage_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 96 in `togglePackage_AlreadyDeleted_Toggle_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 114 in `togglePackage_MultipleConsecutiveToggles_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 144 in `togglePackage_MaxLongId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 156 in `togglePackage_NegativeId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 168 in `togglePackage_NotFound_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 181 in `togglePackage_ZeroId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 193 in `togglePackage_RapidSuccessiveToggles_StateIntegrity` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 217 in `togglePackage_VeryHighIdValue_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 236 in `togglePackage_LoggingForEachToggle_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 256 in `togglePackage_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 266 in `togglePackage_WithValidRequest_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 6: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 42 inline mock in `togglePackage_MultipleToggles_Success`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 43 inline mock in `togglePackage_MultipleToggles_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 64 inline mock in `togglePackage_RestoreFromDeleted_Success`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 65 inline mock in `togglePackage_RestoreFromDeleted_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 80 inline mock in `togglePackage_Success`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 81 inline mock in `togglePackage_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 98 inline mock in `togglePackage_AlreadyDeleted_Toggle_Success`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 99 inline mock in `togglePackage_AlreadyDeleted_Toggle_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 116 inline mock in `togglePackage_MultipleConsecutiveToggles_Success`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 117 inline mock in `togglePackage_MultipleConsecutiveToggles_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 145 inline mock in `togglePackage_MaxLongId_ThrowsNotFoundException`: `when(packageRepository.findByPackageIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 157 inline mock in `togglePackage_NegativeId_ThrowsNotFoundException`: `when(packageRepository.findByPackageIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 169 inline mock in `togglePackage_NotFound_ThrowsNotFoundException`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 182 inline mock in `togglePackage_ZeroId_ThrowsNotFoundException`: `when(packageRepository.findByPackageIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 195 inline mock in `togglePackage_RapidSuccessiveToggles_StateIntegrity`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 196 inline mock in `togglePackage_RapidSuccessiveToggles_StateIntegrity`: `when(packageRepository.save(any())).thenAnswer(invocation -> {`
- Required: Move to base test stub method and call stub in test.
- Line: 220 inline mock in `togglePackage_VeryHighIdValue_Success`: `when(packageRepository.findByPackageIdAndClientId(highId, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 221 inline mock in `togglePackage_VeryHighIdValue_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 238 inline mock in `togglePackage_LoggingForEachToggle_Success`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 239 inline mock in `togglePackage_LoggingForEachToggle_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 268 inline mock in `togglePackage_WithValidRequest_DelegatesToService`: `doNothing().when(packageServiceMock).togglePackage(TEST_PACKAGE_ID);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 7: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 146 in `togglePackage_MaxLongId_ThrowsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 158 in `togglePackage_NegativeId_ThrowsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 183 in `togglePackage_ZeroId_ThrowsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: togglePackage_MultipleToggles_Success, togglePackage_RestoreFromDeleted_Success, togglePackage_Success, togglePackage_AlreadyDeleted_Toggle_Success, togglePackage_MultipleConsecutiveToggles_Success
- Required order: togglePackage_AlreadyDeleted_Toggle_Success, togglePackage_MultipleConsecutiveToggles_Success, togglePackage_MultipleToggles_Success, togglePackage_RestoreFromDeleted_Success, togglePackage_Success

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: togglePackage_MaxLongId_ThrowsNotFoundException, togglePackage_NegativeId_ThrowsNotFoundException, togglePackage_NotFound_ThrowsNotFoundException, togglePackage_ZeroId_ThrowsNotFoundException, togglePackage_RapidSuccessiveToggles_StateIntegrity, togglePackage_VeryHighIdValue_Success, togglePackage_LoggingForEachToggle_Success
- Required order: togglePackage_LoggingForEachToggle_Success, togglePackage_MaxLongId_ThrowsNotFoundException, togglePackage_NegativeId_ThrowsNotFoundException, togglePackage_NotFound_ThrowsNotFoundException, togglePackage_RapidSuccessiveToggles_StateIntegrity, togglePackage_VeryHighIdValue_Success, togglePackage_ZeroId_ThrowsNotFoundException

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 7 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Package/GetPackagesByPickupLocationIdTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Package
Class: class GetPackagesByPickupLocationIdTest extends PackageServiceTestBase {
Extends: PackageServiceTestBase
Lines of Code: 686
Last Modified: 2026-02-10 20:20:42
Declared Test Count: 16 (first occurrence line 28)
Actual @Test Count: 31

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 28
- Current: 16
- Required: 31

VIOLATION 2: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Lines: 28, 399
- Required: Keep only the first declaration at the class start; remove duplicates.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 359 method `getPackagesByPickupLocationId_VerifyPreAuthorizeAnnotation`
- Required rename: `getPackagesByPickupLocationId_VerifyPreAuthorizeAnnotation_Success`
- Line: 453 method `getPackagesByPickupLocationId_Success`
- Required rename: `getPackagesByPickupLocationId_Success_Success`
- Line: 666 method `getPackagesByPickupLocationId_VerifyPreAuthorizeAnnotation`
- Required rename: `getPackagesByPickupLocationId_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 4: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getPackagesByPickupLocationId_WithValidRequest_DelegatesToService (line 674)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 5: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 415 in `getPackagesByPickupLocationId_EmptyResult_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 434 in `getPackagesByPickupLocationId_MultiplePackages_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 453 in `getPackagesByPickupLocationId_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 473 in `getPackagesByPickupLocationId_ManyPackagesResult_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 499 in `getPackagesByPickupLocationId_LargeId_NotFound` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 516 in `getPackagesByPickupLocationId_OnePackageResult_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 541 in `getPackagesByPickupLocationId_MaxLongId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 554 in `getPackagesByPickupLocationId_NegativeId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 566 in `getPackagesByPickupLocationId_NotFound_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 580 in `getPackagesByPickupLocationId_ZeroId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 593 in `getPackagesByPickupLocationId_ExtremePackageCount_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 617 in `getPackagesByPickupLocationId_VeryHighLocationId_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 638 in `getPackagesByPickupLocationId_AllIdenticalPackages_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 666 in `getPackagesByPickupLocationId_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 676 in `getPackagesByPickupLocationId_WithValidRequest_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 6: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 416 inline mock in `getPackagesByPickupLocationId_EmptyResult_Success`: `when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 418 inline mock in `getPackagesByPickupLocationId_EmptyResult_Success`: `when(packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID,`
- Required: Move to base test stub method and call stub in test.
- Line: 435 inline mock in `getPackagesByPickupLocationId_MultiplePackages_Success`: `when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 437 inline mock in `getPackagesByPickupLocationId_MultiplePackages_Success`: `when(packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID,`
- Required: Move to base test stub method and call stub in test.
- Line: 454 inline mock in `getPackagesByPickupLocationId_Success`: `when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 456 inline mock in `getPackagesByPickupLocationId_Success`: `when(packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID,`
- Required: Move to base test stub method and call stub in test.
- Line: 474 inline mock in `getPackagesByPickupLocationId_ManyPackagesResult_Success`: `when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 483 inline mock in `getPackagesByPickupLocationId_ManyPackagesResult_Success`: `when(packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID,`
- Required: Move to base test stub method and call stub in test.
- Line: 500 inline mock in `getPackagesByPickupLocationId_LargeId_NotFound`: `when(pickupLocationRepository.countByPickupLocationIdAndClientId(Long.MAX_VALUE - 1, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 517 inline mock in `getPackagesByPickupLocationId_OnePackageResult_Success`: `when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 519 inline mock in `getPackagesByPickupLocationId_OnePackageResult_Success`: `when(packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID,`
- Required: Move to base test stub method and call stub in test.
- Line: 542 inline mock in `getPackagesByPickupLocationId_MaxLongId_ThrowsNotFoundException`: `when(pickupLocationRepository.countByPickupLocationIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 555 inline mock in `getPackagesByPickupLocationId_NegativeId_ThrowsNotFoundException`: `when(pickupLocationRepository.countByPickupLocationIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(0L);`
- Required: Move to base test stub method and call stub in test.
- Line: 567 inline mock in `getPackagesByPickupLocationId_NotFound_ThrowsNotFoundException`: `when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 581 inline mock in `getPackagesByPickupLocationId_ZeroId_ThrowsNotFoundException`: `when(pickupLocationRepository.countByPickupLocationIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(0L);`
- Required: Move to base test stub method and call stub in test.
- Line: 594 inline mock in `getPackagesByPickupLocationId_ExtremePackageCount_Success`: `when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 601 inline mock in `getPackagesByPickupLocationId_ExtremePackageCount_Success`: `when(packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID,`
- Required: Move to base test stub method and call stub in test.
- Line: 619 inline mock in `getPackagesByPickupLocationId_VeryHighLocationId_Success`: `when(pickupLocationRepository.countByPickupLocationIdAndClientId(highLocationId, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 621 inline mock in `getPackagesByPickupLocationId_VeryHighLocationId_Success`: `when(packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(highLocationId, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 639 inline mock in `getPackagesByPickupLocationId_AllIdenticalPackages_Success`: `when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))`
- Required: Move to base test stub method and call stub in test.
- Line: 646 inline mock in `getPackagesByPickupLocationId_AllIdenticalPackages_Success`: `when(packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID,`
- Required: Move to base test stub method and call stub in test.
- Line: 678 inline mock in `getPackagesByPickupLocationId_WithValidRequest_DelegatesToService`: `when(packageServiceMock.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID))`
- Required: Move to base test stub method and call stub in test.

VIOLATION 7: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 503 in `getPackagesByPickupLocationId_LargeId_NotFound`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 544 in `getPackagesByPickupLocationId_MaxLongId_ThrowsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 556 in `getPackagesByPickupLocationId_NegativeId_ThrowsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 569 in `getPackagesByPickupLocationId_NotFound_ThrowsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 582 in `getPackagesByPickupLocationId_ZeroId_ThrowsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: getPackagesByPickupLocationId_AllIdenticalPackages_Success, getPackagesByPickupLocationId_EmptyResult_Success, getPackagesByPickupLocationId_ExtremePackageCount_Success, getPackagesByPickupLocationId_LargeId_NotFound, getPackagesByPickupLocationId_ManyPackagesResult_Success, getPackagesByPickupLocationId_MultiplePackages_Success, getPackagesByPickupLocationId_OnePackageResult_Success, getPackagesByPickupLocationId_Success_Success, getPackagesByPickupLocationId_VeryHighLocationId_Success, getPackagesByPickupLocationId_EmptyResult_Success, getPackagesByPickupLocationId_MultiplePackages_Success, getPackagesByPickupLocationId_Success, getPackagesByPickupLocationId_ManyPackagesResult_Success, getPackagesByPickupLocationId_LargeId_NotFound, getPackagesByPickupLocationId_OnePackageResult_Success
- Required order: getPackagesByPickupLocationId_AllIdenticalPackages_Success, getPackagesByPickupLocationId_EmptyResult_Success, getPackagesByPickupLocationId_EmptyResult_Success, getPackagesByPickupLocationId_ExtremePackageCount_Success, getPackagesByPickupLocationId_LargeId_NotFound, getPackagesByPickupLocationId_LargeId_NotFound, getPackagesByPickupLocationId_ManyPackagesResult_Success, getPackagesByPickupLocationId_ManyPackagesResult_Success, getPackagesByPickupLocationId_MultiplePackages_Success, getPackagesByPickupLocationId_MultiplePackages_Success, getPackagesByPickupLocationId_OnePackageResult_Success, getPackagesByPickupLocationId_OnePackageResult_Success, getPackagesByPickupLocationId_Success, getPackagesByPickupLocationId_Success_Success, getPackagesByPickupLocationId_VeryHighLocationId_Success

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: getPackagesByPickupLocationId_MaxLongId_ThrowsNotFoundException, getPackagesByPickupLocationId_NegativeId_ThrowsNotFoundException, getPackagesByPickupLocationId_NotFound_ThrowsNotFoundException, getPackagesByPickupLocationId_ZeroId_ThrowsNotFoundException, getPackagesByPickupLocationId_MaxLongId_ThrowsNotFoundException, getPackagesByPickupLocationId_NegativeId_ThrowsNotFoundException, getPackagesByPickupLocationId_NotFound_ThrowsNotFoundException, getPackagesByPickupLocationId_ZeroId_ThrowsNotFoundException, getPackagesByPickupLocationId_ExtremePackageCount_Success, getPackagesByPickupLocationId_VeryHighLocationId_Success, getPackagesByPickupLocationId_AllIdenticalPackages_Success
- Required order: getPackagesByPickupLocationId_AllIdenticalPackages_Success, getPackagesByPickupLocationId_ExtremePackageCount_Success, getPackagesByPickupLocationId_MaxLongId_ThrowsNotFoundException, getPackagesByPickupLocationId_MaxLongId_ThrowsNotFoundException, getPackagesByPickupLocationId_NegativeId_ThrowsNotFoundException, getPackagesByPickupLocationId_NegativeId_ThrowsNotFoundException, getPackagesByPickupLocationId_NotFound_ThrowsNotFoundException, getPackagesByPickupLocationId_NotFound_ThrowsNotFoundException, getPackagesByPickupLocationId_VeryHighLocationId_Success, getPackagesByPickupLocationId_ZeroId_ThrowsNotFoundException, getPackagesByPickupLocationId_ZeroId_ThrowsNotFoundException

VIOLATION 10: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section PERMISSION not alphabetical.
- Current order: getPackagesByPickupLocationId_controller_permission_unauthorized, getPackagesByPickupLocationId_VerifyPreAuthorizeAnnotation, getPackagesByPickupLocationId_WithValidRequest_DelegatesToService, getPackagesByPickupLocationId_VerifyPreAuthorizeAnnotation, getPackagesByPickupLocationId_WithValidRequest_DelegatesToService
- Required order: getPackagesByPickupLocationId_controller_permission_unauthorized, getPackagesByPickupLocationId_VerifyPreAuthorizeAnnotation, getPackagesByPickupLocationId_VerifyPreAuthorizeAnnotation, getPackagesByPickupLocationId_WithValidRequest_DelegatesToService, getPackagesByPickupLocationId_WithValidRequest_DelegatesToService

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 2 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 7 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Package/BulkCreatePackagesTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Package
Class: class BulkCreatePackagesTest extends PackageServiceTestBase {
Extends: PackageServiceTestBase
Lines of Code: 817
Last Modified: 2026-02-10 21:29:52
Declared Test Count: 19 (first occurrence line 30)
Actual @Test Count: 37

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 30
- Current: 19
- Required: 37

VIOLATION 2: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Lines: 30, 483
- Required: Keep only the first declaration at the class start; remove duplicates.

VIOLATION 3: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 418 has mock usage `com.example.SpringApi.Services.PackageService concreteMock = mock(com.example.SpringApi.Services.PackageService.class);`
- Required: Move mocks to base test file.
- Line: 460 has mock usage `com.example.SpringApi.Services.PackageService concreteMock = mock(com.example.SpringApi.Services.PackageService.class);`
- Required: Move mocks to base test file.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 799 method `bulkCreatePackages_VerifyPreAuthorizeAnnotation`
- Required rename: `bulkCreatePackages_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: bulkCreatePackages_WithValidRequest_DelegatesToService (line 807)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 498 in `bulkCreatePackages_AllValid_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 524 in `bulkCreatePackages_LargeBatch_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 542 in `bulkCreatePackages_MixedInvalidAndValid_PartialSuccess` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 571 in `bulkCreatePackages_SingleValidItem_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 595 in `bulkCreatePackages_AllInvalidNames_AllFail` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 615 in `bulkCreatePackages_DatabaseError_RecordsFailure` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 632 in `bulkCreatePackages_EmptyList_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 646 in `bulkCreatePackages_InvalidBreadthZero_Fails` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 660 in `bulkCreatePackages_InvalidHeightZero_Fails` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 674 in `bulkCreatePackages_InvalidLengthZero_Fails` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 688 in `bulkCreatePackages_InvalidNegativeWeight_Fails` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 702 in `bulkCreatePackages_NullPackageName_Fails` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 716 in `bulkCreatePackages_NullList_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 729 in `bulkCreatePackages_ExtremeBatchSize_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 750 in `bulkCreatePackages_AllDuplicateNames_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 772 in `bulkCreatePackages_AlternatingValidInvalid_PartialSuccess` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 799 in `bulkCreatePackages_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 809 in `bulkCreatePackages_WithValidRequest_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 419 inline mock in `bulkCreatePackages_controller_permission_unauthorized`: `when(concreteMock.getUserId()).thenThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(`
- Required: Move to base test stub method and call stub in test.
- Line: 461 inline mock in `bulkCreatePackages_WithValidRequest_DelegatesToService`: `when(concreteMock.getUserId()).thenReturn(TEST_USER_ID);`
- Required: Move to base test stub method and call stub in test.
- Line: 462 inline mock in `bulkCreatePackages_WithValidRequest_DelegatesToService`: `when(concreteMock.getUser()).thenReturn("testuser");`
- Required: Move to base test stub method and call stub in test.
- Line: 463 inline mock in `bulkCreatePackages_WithValidRequest_DelegatesToService`: `when(concreteMock.getClientId()).thenReturn(TEST_CLIENT_ID);`
- Required: Move to base test stub method and call stub in test.
- Line: 465 inline mock in `bulkCreatePackages_WithValidRequest_DelegatesToService`: `doNothing().when(packageServiceMock)`
- Required: Move to base test stub method and call stub in test.
- Line: 506 inline mock in `bulkCreatePackages_AllValid_Success`: `when(packageRepository.save(any(com.example.SpringApi.Models.DatabaseModels.Package.class))).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 507 inline mock in `bulkCreatePackages_AllValid_Success`: `when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);`
- Required: Move to base test stub method and call stub in test.
- Line: 530 inline mock in `bulkCreatePackages_LargeBatch_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 552 inline mock in `bulkCreatePackages_MixedInvalidAndValid_PartialSuccess`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 553 inline mock in `bulkCreatePackages_MixedInvalidAndValid_PartialSuccess`: `when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);`
- Required: Move to base test stub method and call stub in test.
- Line: 573 inline mock in `bulkCreatePackages_SingleValidItem_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 574 inline mock in `bulkCreatePackages_SingleValidItem_Success`: `when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);`
- Required: Move to base test stub method and call stub in test.
- Line: 617 inline mock in `bulkCreatePackages_DatabaseError_RecordsFailure`: `when(packageRepository.save(any())).thenThrow(new RuntimeException("DB Error"));`
- Required: Move to base test stub method and call stub in test.
- Line: 734 inline mock in `bulkCreatePackages_ExtremeBatchSize_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 735 inline mock in `bulkCreatePackages_ExtremeBatchSize_Success`: `when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);`
- Required: Move to base test stub method and call stub in test.
- Line: 757 inline mock in `bulkCreatePackages_AllDuplicateNames_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 758 inline mock in `bulkCreatePackages_AllDuplicateNames_Success`: `when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);`
- Required: Move to base test stub method and call stub in test.
- Line: 783 inline mock in `bulkCreatePackages_AlternatingValidInvalid_PartialSuccess`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 8 - Error Constants
- Severity: HIGH
- Line: 636 has hardcoded message: `assertTrue(exception.getMessage().contains("Package list cannot be null or empty"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 636 has hardcoded message: `assertTrue(exception.getMessage().contains("Package list cannot be null or empty"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 719 has hardcoded message: `assertTrue(exception.getMessage().contains("Package list cannot be null or empty"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 719 has hardcoded message: `assertTrue(exception.getMessage().contains("Package list cannot be null or empty"));`
- Required: Replace with an ErrorMessages constant (add one if missing).

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: bulkCreatePackages_AllDuplicateNames_Success, bulkCreatePackages_AllValid_Success, bulkCreatePackages_AlternatingValidInvalid_PartialSuccess, bulkCreatePackages_ExtremeBatchSize_Success, bulkCreatePackages_LargeBatch_Success, bulkCreatePackages_MixedInvalidAndValid_PartialSuccess, bulkCreatePackages_SingleValidItem_Success, bulkCreatePackages_AllValid_Success, bulkCreatePackages_LargeBatch_Success, bulkCreatePackages_MixedInvalidAndValid_PartialSuccess, bulkCreatePackages_SingleValidItem_Success
- Required order: bulkCreatePackages_AllDuplicateNames_Success, bulkCreatePackages_AllValid_Success, bulkCreatePackages_AllValid_Success, bulkCreatePackages_AlternatingValidInvalid_PartialSuccess, bulkCreatePackages_ExtremeBatchSize_Success, bulkCreatePackages_LargeBatch_Success, bulkCreatePackages_LargeBatch_Success, bulkCreatePackages_MixedInvalidAndValid_PartialSuccess, bulkCreatePackages_MixedInvalidAndValid_PartialSuccess, bulkCreatePackages_SingleValidItem_Success, bulkCreatePackages_SingleValidItem_Success

VIOLATION 10: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: bulkCreatePackages_AllInvalidNames_AllFail, bulkCreatePackages_DatabaseError_RecordsFailure, bulkCreatePackages_EmptyList_ThrowsBadRequestException, bulkCreatePackages_InvalidBreadthZero_Fails, bulkCreatePackages_InvalidHeightZero_Fails, bulkCreatePackages_InvalidLengthZero_Fails, bulkCreatePackages_InvalidNegativeWeight_Fails, bulkCreatePackages_NullList_ThrowsBadRequestException, bulkCreatePackages_NullPackageName_Fails, bulkCreatePackages_AllInvalidNames_AllFail, bulkCreatePackages_DatabaseError_RecordsFailure, bulkCreatePackages_EmptyList_ThrowsBadRequestException, bulkCreatePackages_InvalidBreadthZero_Fails, bulkCreatePackages_InvalidHeightZero_Fails, bulkCreatePackages_InvalidLengthZero_Fails, bulkCreatePackages_InvalidNegativeWeight_Fails, bulkCreatePackages_NullPackageName_Fails, bulkCreatePackages_NullList_ThrowsBadRequestException, bulkCreatePackages_ExtremeBatchSize_Success, bulkCreatePackages_AllDuplicateNames_Success, bulkCreatePackages_AlternatingValidInvalid_PartialSuccess
- Required order: bulkCreatePackages_AllDuplicateNames_Success, bulkCreatePackages_AllInvalidNames_AllFail, bulkCreatePackages_AllInvalidNames_AllFail, bulkCreatePackages_AlternatingValidInvalid_PartialSuccess, bulkCreatePackages_DatabaseError_RecordsFailure, bulkCreatePackages_DatabaseError_RecordsFailure, bulkCreatePackages_EmptyList_ThrowsBadRequestException, bulkCreatePackages_EmptyList_ThrowsBadRequestException, bulkCreatePackages_ExtremeBatchSize_Success, bulkCreatePackages_InvalidBreadthZero_Fails, bulkCreatePackages_InvalidBreadthZero_Fails, bulkCreatePackages_InvalidHeightZero_Fails, bulkCreatePackages_InvalidHeightZero_Fails, bulkCreatePackages_InvalidLengthZero_Fails, bulkCreatePackages_InvalidLengthZero_Fails, bulkCreatePackages_InvalidNegativeWeight_Fails, bulkCreatePackages_InvalidNegativeWeight_Fails, bulkCreatePackages_NullList_ThrowsBadRequestException, bulkCreatePackages_NullList_ThrowsBadRequestException, bulkCreatePackages_NullPackageName_Fails, bulkCreatePackages_NullPackageName_Fails

VIOLATION 11: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section PERMISSION not alphabetical.
- Current order: bulkCreatePackages_controller_permission_unauthorized, bulkCreatePackages_VerifyPreAuthorizeAnnotation_Success, bulkCreatePackages_WithValidRequest_DelegatesToService, bulkCreatePackages_VerifyPreAuthorizeAnnotation, bulkCreatePackages_WithValidRequest_DelegatesToService
- Required order: bulkCreatePackages_controller_permission_unauthorized, bulkCreatePackages_VerifyPreAuthorizeAnnotation, bulkCreatePackages_VerifyPreAuthorizeAnnotation_Success, bulkCreatePackages_WithValidRequest_DelegatesToService, bulkCreatePackages_WithValidRequest_DelegatesToService

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 8 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Package/UpdatePackageTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Package
Class: class UpdatePackageTest extends PackageServiceTestBase {
Extends: PackageServiceTestBase
Lines of Code: 494
Last Modified: 2026-02-09 23:10:05
Declared Test Count: 21 (first occurrence line 35)
Actual @Test Count: 24

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 35
- Current: 21
- Required: 24

VIOLATION 2: Rule 4 - Test Annotations
- Severity: HIGH
- Line: 68 has disallowed annotation @SuppressWarnings.
- Required: Remove or replace with allowed annotations only.
- Line: 99 has disallowed annotation @SuppressWarnings.
- Required: Remove or replace with allowed annotations only.
- Line: 157 has disallowed annotation @SuppressWarnings.
- Required: Remove or replace with allowed annotations only.
- Line: 187 has disallowed annotation @SuppressWarnings.
- Required: Remove or replace with allowed annotations only.
- Line: 217 has disallowed annotation @SuppressWarnings.
- Required: Remove or replace with allowed annotations only.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `updatePackage_controller_permission_forbidden` or `updatePackage_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 114 method `updatePackage_Success`
- Required rename: `updatePackage_Success_Success`
- Line: 475 method `updatePackage_VerifyPreAuthorizeAnnotation`
- Required rename: `updatePackage_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: updatePackage_WithValidRequest_DelegatesToService (line 483)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 50 in `updatePackage_DecreaseQuantity_KeepsRestockDate` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 81 in `updatePackage_IncreaseQuantity_UpdatesRestockDate` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 114 in `updatePackage_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 131 in `updatePackage_MultipleLocationUpdates_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 170 in `updatePackage_ZeroQuantityUpdate_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 200 in `updatePackage_LargeQuantityIncrease_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 237 in `updatePackage_NegativeBreadth_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 250 in `updatePackage_NotFound_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 262 in `updatePackage_NullBreadth_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 276 in `updatePackage_NullHeight_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 290 in `updatePackage_NullLength_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 304 in `updatePackage_NullName_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 318 in `updatePackage_NullPricePerUnit_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 332 in `updatePackage_NullStandardCapacity_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 346 in `updatePackage_NullType_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 360 in `updatePackage_ZeroBreadth_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 374 in `updatePackage_ZeroHeight_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 388 in `updatePackage_ZeroLength_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 402 in `updatePackage_ZeroStandardCapacity_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 416 in `updatePackage_MaxDimensionValues_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 435 in `updatePackage_MinValidDimensions_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 454 in `updatePackage_VeryLargeStandardCapacity_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 475 in `updatePackage_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 485 in `updatePackage_WithValidRequest_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 57 inline mock in `updatePackage_DecreaseQuantity_KeepsRestockDate`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 58 inline mock in `updatePackage_DecreaseQuantity_KeepsRestockDate`: `when(packagePickupLocationMappingRepository.findByPackageId(TEST_PACKAGE_ID)).thenReturn(List.of(existing));`
- Required: Move to base test stub method and call stub in test.
- Line: 88 inline mock in `updatePackage_IncreaseQuantity_UpdatesRestockDate`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 89 inline mock in `updatePackage_IncreaseQuantity_UpdatesRestockDate`: `when(packagePickupLocationMappingRepository.findByPackageId(TEST_PACKAGE_ID)).thenReturn(List.of(existing));`
- Required: Move to base test stub method and call stub in test.
- Line: 115 inline mock in `updatePackage_Success`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 116 inline mock in `updatePackage_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 142 inline mock in `updatePackage_MultipleLocationUpdates_Success`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 143 inline mock in `updatePackage_MultipleLocationUpdates_Success`: `when(packagePickupLocationMappingRepository.findByPackageId(TEST_PACKAGE_ID)).thenReturn(List.of(loc1, loc2));`
- Required: Move to base test stub method and call stub in test.
- Line: 176 inline mock in `updatePackage_ZeroQuantityUpdate_Success`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 177 inline mock in `updatePackage_ZeroQuantityUpdate_Success`: `when(packagePickupLocationMappingRepository.findByPackageId(TEST_PACKAGE_ID)).thenReturn(List.of(existing));`
- Required: Move to base test stub method and call stub in test.
- Line: 206 inline mock in `updatePackage_LargeQuantityIncrease_Success`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 207 inline mock in `updatePackage_LargeQuantityIncrease_Success`: `when(packagePickupLocationMappingRepository.findByPackageId(TEST_PACKAGE_ID)).thenReturn(List.of(existing));`
- Required: Move to base test stub method and call stub in test.
- Line: 239 inline mock in `updatePackage_NegativeBreadth_Throws`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 251 inline mock in `updatePackage_NotFound_ThrowsNotFoundException`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 264 inline mock in `updatePackage_NullBreadth_Throws`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 278 inline mock in `updatePackage_NullHeight_Throws`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 292 inline mock in `updatePackage_NullLength_Throws`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 306 inline mock in `updatePackage_NullName_Throws`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 320 inline mock in `updatePackage_NullPricePerUnit_Throws`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 334 inline mock in `updatePackage_NullStandardCapacity_Throws`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 348 inline mock in `updatePackage_NullType_Throws`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 362 inline mock in `updatePackage_ZeroBreadth_Throws`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 376 inline mock in `updatePackage_ZeroHeight_Throws`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 390 inline mock in `updatePackage_ZeroLength_Throws`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 404 inline mock in `updatePackage_ZeroStandardCapacity_Throws`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 420 inline mock in `updatePackage_MaxDimensionValues_Success`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 421 inline mock in `updatePackage_MaxDimensionValues_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 439 inline mock in `updatePackage_MinValidDimensions_Success`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 440 inline mock in `updatePackage_MinValidDimensions_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 459 inline mock in `updatePackage_VeryLargeStandardCapacity_Success`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 460 inline mock in `updatePackage_VeryLargeStandardCapacity_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 487 inline mock in `updatePackage_WithValidRequest_DelegatesToService`: `doNothing().when(packageServiceMock).updatePackage(testPackageRequest);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 240 in `updatePackage_NegativeBreadth_Throws`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 252 in `updatePackage_NotFound_ThrowsNotFoundException`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: updatePackage_DecreaseQuantity_KeepsRestockDate, updatePackage_IncreaseQuantity_UpdatesRestockDate, updatePackage_Success, updatePackage_MultipleLocationUpdates_Success, updatePackage_ZeroQuantityUpdate_Success, updatePackage_LargeQuantityIncrease_Success
- Required order: updatePackage_DecreaseQuantity_KeepsRestockDate, updatePackage_IncreaseQuantity_UpdatesRestockDate, updatePackage_LargeQuantityIncrease_Success, updatePackage_MultipleLocationUpdates_Success, updatePackage_Success, updatePackage_ZeroQuantityUpdate_Success

VIOLATION 10: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: updatePackage_NegativeBreadth_Throws, updatePackage_NotFound_ThrowsNotFoundException, updatePackage_NullBreadth_Throws, updatePackage_NullHeight_Throws, updatePackage_NullLength_Throws, updatePackage_NullName_Throws, updatePackage_NullPricePerUnit_Throws, updatePackage_NullStandardCapacity_Throws, updatePackage_NullType_Throws, updatePackage_ZeroBreadth_Throws, updatePackage_ZeroHeight_Throws, updatePackage_ZeroLength_Throws, updatePackage_ZeroStandardCapacity_Throws, updatePackage_MaxDimensionValues_Success, updatePackage_MinValidDimensions_Success, updatePackage_VeryLargeStandardCapacity_Success
- Required order: updatePackage_MaxDimensionValues_Success, updatePackage_MinValidDimensions_Success, updatePackage_NegativeBreadth_Throws, updatePackage_NotFound_ThrowsNotFoundException, updatePackage_NullBreadth_Throws, updatePackage_NullHeight_Throws, updatePackage_NullLength_Throws, updatePackage_NullName_Throws, updatePackage_NullPricePerUnit_Throws, updatePackage_NullStandardCapacity_Throws, updatePackage_NullType_Throws, updatePackage_VeryLargeStandardCapacity_Success, updatePackage_ZeroBreadth_Throws, updatePackage_ZeroHeight_Throws, updatePackage_ZeroLength_Throws, updatePackage_ZeroStandardCapacity_Throws

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 4 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 7 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Package/GetPackageByIdTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Package
Class: class GetPackageByIdTest extends PackageServiceTestBase {
Extends: PackageServiceTestBase
Lines of Code: 277
Last Modified: 2026-02-09 23:10:05
Declared Test Count: 12 (first occurrence line 25)
Actual @Test Count: 15

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 25
- Current: 12
- Required: 15

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getPackageById_controller_permission_forbidden` or `getPackageById_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 79 method `getPackageById_Success`
- Required rename: `getPackageById_Success_Success`
- Line: 258 method `getPackageById_VerifyPreAuthorizeAnnotation`
- Required rename: `getPackageById_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 4: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getPackageById_WithValidRequest_DelegatesToService (line 266)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 5: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 40 in `getPackageById_AllFieldsPopulated_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 60 in `getPackageById_DeletedPackage_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 79 in `getPackageById_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 96 in `getPackageById_MaxLongId_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 116 in `getPackageById_WithManyMappings_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 140 in `getPackageById_MaxLongValue_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 153 in `getPackageById_MinLongValue_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 166 in `getPackageById_NegativeId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 179 in `getPackageById_NotFound_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 192 in `getPackageById_ZeroId_ThrowsNotFoundException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 205 in `getPackageById_MaxLongValue_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 222 in `getPackageById_HighValueId_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 240 in `getPackageById_SpecialCharactersInName_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 258 in `getPackageById_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 268 in `getPackageById_WithValidRequest_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 6: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 44 inline mock in `getPackageById_AllFieldsPopulated_Success`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 62 inline mock in `getPackageById_DeletedPackage_Success`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 80 inline mock in `getPackageById_Success`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 100 inline mock in `getPackageById_MaxLongId_Success`: `when(packageRepository.findByPackageIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(maxIdPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 118 inline mock in `getPackageById_WithManyMappings_Success`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 141 inline mock in `getPackageById_MaxLongValue_ThrowsNotFoundException`: `when(packageRepository.findByPackageIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 154 inline mock in `getPackageById_MinLongValue_ThrowsNotFoundException`: `when(packageRepository.findByPackageIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 167 inline mock in `getPackageById_NegativeId_ThrowsNotFoundException`: `when(packageRepository.findByPackageIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 180 inline mock in `getPackageById_NotFound_ThrowsNotFoundException`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 193 inline mock in `getPackageById_ZeroId_ThrowsNotFoundException`: `when(packageRepository.findByPackageIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);`
- Required: Move to base test stub method and call stub in test.
- Line: 207 inline mock in `getPackageById_MaxLongValue_Success`: `when(packageRepository.findByPackageIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 225 inline mock in `getPackageById_HighValueId_Success`: `when(packageRepository.findByPackageIdAndClientId(highId, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 242 inline mock in `getPackageById_SpecialCharactersInName_Success`: `when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 270 inline mock in `getPackageById_WithValidRequest_DelegatesToService`: `when(packageServiceMock.getPackageById(TEST_PACKAGE_ID)).thenReturn(new PackageResponseModel(testPackage));`
- Required: Move to base test stub method and call stub in test.

VIOLATION 7: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: getPackageById_AllFieldsPopulated_Success, getPackageById_DeletedPackage_Success, getPackageById_Success, getPackageById_MaxLongId_Success, getPackageById_WithManyMappings_Success
- Required order: getPackageById_AllFieldsPopulated_Success, getPackageById_DeletedPackage_Success, getPackageById_MaxLongId_Success, getPackageById_Success, getPackageById_WithManyMappings_Success

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: getPackageById_MaxLongValue_ThrowsNotFoundException, getPackageById_MinLongValue_ThrowsNotFoundException, getPackageById_NegativeId_ThrowsNotFoundException, getPackageById_NotFound_ThrowsNotFoundException, getPackageById_ZeroId_ThrowsNotFoundException, getPackageById_MaxLongValue_Success, getPackageById_HighValueId_Success, getPackageById_SpecialCharactersInName_Success
- Required order: getPackageById_HighValueId_Success, getPackageById_MaxLongValue_Success, getPackageById_MaxLongValue_ThrowsNotFoundException, getPackageById_MinLongValue_ThrowsNotFoundException, getPackageById_NegativeId_ThrowsNotFoundException, getPackageById_NotFound_ThrowsNotFoundException, getPackageById_SpecialCharactersInName_Success, getPackageById_ZeroId_ThrowsNotFoundException

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Package/BulkCreatePackagesAsyncTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Package
Class: class BulkCreatePackagesAsyncTest extends PackageServiceTestBase {
Extends: PackageServiceTestBase
Lines of Code: 88
Last Modified: 2026-02-10 20:58:58
Declared Test Count: 3 (first occurrence line 22)
Actual @Test Count: 3

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `bulkCreatePackagesAsync_controller_permission_forbidden` or `bulkCreatePackagesAsync_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 2: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: FAILURE, PERMISSION, SUCCESS
- Required: Add Success, Failure, Permission section headers.

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Package/GetPackagesInBatchesTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Package
Class: class GetPackagesInBatchesTest extends PackageServiceTestBase {
Extends: PackageServiceTestBase
Lines of Code: 199
Last Modified: 2026-02-09 23:10:05
Declared Test Count: 9 (first occurrence line 34)
Actual @Test Count: 9

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getPackagesInBatches_controller_permission_forbidden` or `getPackagesInBatches_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 2: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 51 method `getPackagesInBatches_Success`
- Required rename: `getPackagesInBatches_Success_Success`
- Line: 180 method `getPackagesInBatches_VerifyPreAuthorizeAnnotation`
- Required rename: `getPackagesInBatches_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 3: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getPackagesInBatches_WithValidRequest_DelegatesToService (line 188)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 4: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 51 in `getPackagesInBatches_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 71 in `getPackagesInBatches_MaxPaginationRange_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 91 in `getPackagesInBatches_LargePage_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 115 in `getPackagesInBatches_BoundaryIndexes_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 141 in `getPackagesInBatches_EndIndexZero_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 154 in `getPackagesInBatches_NegativeStart_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 166 in `getPackagesInBatches_StartGreaterEqualEnd_ThrowsBadRequestException` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 180 in `getPackagesInBatches_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 190 in `getPackagesInBatches_WithValidRequest_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 5: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 56 inline mock in `getPackagesInBatches_Success`: `when(packageFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(page);`
- Required: Move to base test stub method and call stub in test.
- Line: 76 inline mock in `getPackagesInBatches_MaxPaginationRange_Success`: `when(packageFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(page);`
- Required: Move to base test stub method and call stub in test.
- Line: 100 inline mock in `getPackagesInBatches_LargePage_Success`: `when(packageFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(page);`
- Required: Move to base test stub method and call stub in test.
- Line: 120 inline mock in `getPackagesInBatches_BoundaryIndexes_Success`: `when(packageFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(page);`
- Required: Move to base test stub method and call stub in test.
- Line: 192 inline mock in `getPackagesInBatches_WithValidRequest_DelegatesToService`: `when(packageServiceMock.getPackagesInBatches(testPaginationRequest)).thenReturn(new PaginationBaseResponseModel<PackageResponseModel>());`
- Required: Move to base test stub method and call stub in test.

VIOLATION 6: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: getPackagesInBatches_Success, getPackagesInBatches_MaxPaginationRange_Success, getPackagesInBatches_LargePage_Success, getPackagesInBatches_BoundaryIndexes_Success
- Required order: getPackagesInBatches_BoundaryIndexes_Success, getPackagesInBatches_LargePage_Success, getPackagesInBatches_MaxPaginationRange_Success, getPackagesInBatches_Success

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Package/CreatePackageTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Package
Class: class CreatePackageTest extends PackageServiceTestBase {
Extends: PackageServiceTestBase
Lines of Code: 515
Last Modified: 2026-02-09 23:31:28
Declared Test Count: 30 (first occurrence line 28)
Actual @Test Count: 33

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 28
- Current: 30
- Required: 33

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `createPackage_controller_permission_forbidden` or `createPackage_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 59 method `createPackage_Success`
- Required rename: `createPackage_Success_Success`
- Line: 496 method `createPackage_VerifyPreAuthorizeAnnotation`
- Required rename: `createPackage_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 4: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: createPackage_WithValidRequest_DelegatesToService (line 504)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 5: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 43 in `createPackage_BoundaryValues_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 59 in `createPackage_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 75 in `createPackage_ZeroMaxWeight_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 88 in `createPackage_ZeroPrice_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 107 in `createPackage_NegativeBreadth_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 121 in `createPackage_NegativeHeight_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 135 in `createPackage_NegativeLength_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 149 in `createPackage_NegativeMaxWeight_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 163 in `createPackage_NegativePricePerUnit_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 177 in `createPackage_NegativeStandardCapacity_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 191 in `createPackage_NullBreadth_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 205 in `createPackage_NullHeight_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 219 in `createPackage_NullLength_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 233 in `createPackage_NullMaxWeight_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 247 in `createPackage_NullName_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 261 in `createPackage_NullPricePerUnit_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 275 in `createPackage_NullRequest_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 287 in `createPackage_NullStandardCapacity_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 301 in `createPackage_NullType_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 315 in `createPackage_WhitespaceType_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 329 in `createPackage_ZeroBreadth_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 343 in `createPackage_ZeroHeight_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 357 in `createPackage_ZeroLength_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 371 in `createPackage_ZeroStandardCapacity_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 386 in `createPackage_MaxIntegerDimensions_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 403 in `createPackage_VeryLargePricePerUnit_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 419 in `createPackage_LargeStandardCapacity_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 433 in `createPackage_PackageName_TooLong_Throws` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 450 in `createPackage_UnicodeCharactersInName_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 466 in `createPackage_MaxPrecisionBigDecimals_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 481 in `createPackage_NegativeMaxWeight_Success` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 496 in `createPackage_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 506 in `createPackage_WithValidRequest_DelegatesToService` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 6: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 48 inline mock in `createPackage_BoundaryValues_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 60 inline mock in `createPackage_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 77 inline mock in `createPackage_ZeroMaxWeight_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 90 inline mock in `createPackage_ZeroPrice_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 391 inline mock in `createPackage_MaxIntegerDimensions_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 406 inline mock in `createPackage_VeryLargePricePerUnit_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 421 inline mock in `createPackage_LargeStandardCapacity_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 452 inline mock in `createPackage_UnicodeCharactersInName_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 469 inline mock in `createPackage_MaxPrecisionBigDecimals_Success`: `when(packageRepository.save(any())).thenReturn(testPackage);`
- Required: Move to base test stub method and call stub in test.
- Line: 508 inline mock in `createPackage_WithValidRequest_DelegatesToService`: `doNothing().when(packageServiceMock).createPackage(testPackageRequest);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 7: Rule 8 - Error Constants
- Severity: HIGH
- Line: 438 has hardcoded message: `assertTrue(ex.getMessage().contains("InvalidPackageName") || ex.getMessage().contains("Package name"),`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 438 has hardcoded message: `assertTrue(ex.getMessage().contains("InvalidPackageName") || ex.getMessage().contains("Package name"),`
- Required: Replace with an ErrorMessages constant (add one if missing).

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: createPackage_NegativeBreadth_Throws, createPackage_NegativeHeight_Throws, createPackage_NegativeLength_Throws, createPackage_NegativeMaxWeight_Throws, createPackage_NegativePricePerUnit_Throws, createPackage_NegativeStandardCapacity_Throws, createPackage_NullBreadth_Throws, createPackage_NullHeight_Throws, createPackage_NullLength_Throws, createPackage_NullMaxWeight_Throws, createPackage_NullName_Throws, createPackage_NullPricePerUnit_Throws, createPackage_NullRequest_Throws, createPackage_NullStandardCapacity_Throws, createPackage_NullType_Throws, createPackage_WhitespaceType_Throws, createPackage_ZeroBreadth_Throws, createPackage_ZeroHeight_Throws, createPackage_ZeroLength_Throws, createPackage_ZeroStandardCapacity_Throws, createPackage_MaxIntegerDimensions_Success, createPackage_VeryLargePricePerUnit_Success, createPackage_LargeStandardCapacity_Success, createPackage_PackageName_TooLong_Throws, createPackage_UnicodeCharactersInName_Success, createPackage_MaxPrecisionBigDecimals_Success, createPackage_NegativeMaxWeight_Success
- Required order: createPackage_LargeStandardCapacity_Success, createPackage_MaxIntegerDimensions_Success, createPackage_MaxPrecisionBigDecimals_Success, createPackage_NegativeBreadth_Throws, createPackage_NegativeHeight_Throws, createPackage_NegativeLength_Throws, createPackage_NegativeMaxWeight_Success, createPackage_NegativeMaxWeight_Throws, createPackage_NegativePricePerUnit_Throws, createPackage_NegativeStandardCapacity_Throws, createPackage_NullBreadth_Throws, createPackage_NullHeight_Throws, createPackage_NullLength_Throws, createPackage_NullMaxWeight_Throws, createPackage_NullName_Throws, createPackage_NullPricePerUnit_Throws, createPackage_NullRequest_Throws, createPackage_NullStandardCapacity_Throws, createPackage_NullType_Throws, createPackage_PackageName_TooLong_Throws, createPackage_UnicodeCharactersInName_Success, createPackage_VeryLargePricePerUnit_Success, createPackage_WhitespaceType_Throws, createPackage_ZeroBreadth_Throws, createPackage_ZeroHeight_Throws, createPackage_ZeroLength_Throws, createPackage_ZeroStandardCapacity_Throws

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 8 issues above.
- Fix Rule 10 issues above.


**IMPLEMENTATION PLAN (STEP-BY-STEP)**
1. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Package/TogglePackageTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
2. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Package/GetPackagesByPickupLocationIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
3. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Package/BulkCreatePackagesTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
4. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Package/UpdatePackageTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
5. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Package/GetPackageByIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
6. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Package/BulkCreatePackagesAsyncTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
7. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Package/GetPackagesInBatchesTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
8. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Package/CreatePackageTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.

Verification Commands (run after fixes):
- mvn -Dtest=TogglePackageTest test
- mvn -Dtest=GetPackagesByPickupLocationIdTest test
- mvn -Dtest=BulkCreatePackagesTest test
- mvn -Dtest=UpdatePackageTest test
- mvn -Dtest=GetPackageByIdTest test
- mvn -Dtest=BulkCreatePackagesAsyncTest test