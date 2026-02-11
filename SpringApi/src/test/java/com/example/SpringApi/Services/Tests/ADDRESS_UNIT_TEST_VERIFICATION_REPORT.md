# UNIT TEST VERIFICATION REPORT — Address

```
╔════════════════════════════════════════════════════════════╗
║           UNIT TEST VERIFICATION REPORT                    ║
║                                                            ║
║  Status: ❌ RED                                               ║
║  Services Analyzed: 1                                      ║
║  Public Methods Found: 6                                  ║
║  Test Files Expected: 6                                  ║
║  Test Files Found: 6                                     ║
║  Total Violations: 11                                    ║
╚════════════════════════════════════════════════════════════╝
```

VIOLATIONS BY RULE:

| Rule | Description | Count |
| --- | --- | --- |
| 2 | Test Count Declaration | 1 |
| 3 | Controller Permission Test | 6 |
| 5 | Test Naming Convention | 1 |
| 9 | Test Documentation | 1 |
| 10 | Test Ordering | 2 |


**FILE-BY-FILE BREAKDOWN**

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Address/InsertAddressTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Address
Class: class InsertAddressTest extends AddressServiceTestBase {
Extends: AddressServiceTestBase
Lines of Code: 1320
Last Modified: 2026-02-10 13:16:38
Declared Test Count: 67 (first occurrence line 25)
Actual @Test Count: 68

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 25
- Current: 67
- Required: 68

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 1250 (insertAddress_controller_permission_forbidden)
- Problem: no http status assertion detected
- Required: Call controller method and assert HTTP status.

VIOLATION 3: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: insertAddress_addressType_HOME_success (line 485), insertAddress_addressType_Home_mixedcase_success (line 498), insertAddress_addressType_OFFICE_success (line 511), insertAddress_addressType_SHIPPING_success (line 524), insertAddress_addressType_WAREHOUSE_success (line 537), insertAddress_addressType_WORK_success (line 550), insertAddress_addressType_Work_mixedcase_success (line 563), insertAddress_addressType_home_lowercase_success (line 576), insertAddress_addressType_work_lowercase_success (line 589), insertAddress_postalCode_123456_success (line 620)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 4: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: insertAddress_AddressTypeNormalized_Success, insertAddress_AllOptionalFieldsNull_Success, insertAddress_AllOptionalFieldsProvided_Success, insertAddress_BoundaryPostalCode5Digits_Success, insertAddress_BoundaryPostalCode6Digits_Success, insertAddress_CaseInsensitiveHome_Success, insertAddress_CaseInsensitiveWork_Success, insertAddress_IsDeletedTrue_Success, insertAddress_IsPrimaryFalse_Success, insertAddress_IsPrimaryTrue_Success, insertAddress_LogsSuccessMessageAndRoute_Success, insertAddress_MaxLongClientId_Success, insertAddress_MaxLongUserId_Success, insertAddress_MixedCaseAddressType_Success, insertAddress_MultipleValidationsPass_Success, insertAddress_NullBooleanFields_DefaultsApplied, insertAddress_NullClientId_Success, insertAddress_NullUserId_Success, insertAddress_PostalCodeWithLeadingZeros_Success, insertAddress_SpecialCharactersInStreetAddress_Success, insertAddress_StreetAddressWithSpaces_SuccessTrimsSpaces, insertAddress_UnicodeCharactersInCity_Success, insertAddress_addressType_BILLING_success, insertAddress_addressType_HOME_success, insertAddress_addressType_Home_mixedcase_success, insertAddress_addressType_OFFICE_success, insertAddress_addressType_SHIPPING_success, insertAddress_addressType_WAREHOUSE_success, insertAddress_addressType_WORK_success, insertAddress_addressType_Work_mixedcase_success, insertAddress_addressType_home_lowercase_success, insertAddress_addressType_work_lowercase_success, insertAddress_postalCode_12345_success, insertAddress_postalCode_123456_success, insertAddress_ValidRequest_Success, insertAddress_VeryLongStreetAddress_Success
- Required order: insertAddress_addressType_BILLING_success, insertAddress_addressType_home_lowercase_success, insertAddress_addressType_Home_mixedcase_success, insertAddress_addressType_HOME_success, insertAddress_addressType_OFFICE_success, insertAddress_addressType_SHIPPING_success, insertAddress_addressType_WAREHOUSE_success, insertAddress_addressType_work_lowercase_success, insertAddress_addressType_Work_mixedcase_success, insertAddress_addressType_WORK_success, insertAddress_AddressTypeNormalized_Success, insertAddress_AllOptionalFieldsNull_Success, insertAddress_AllOptionalFieldsProvided_Success, insertAddress_BoundaryPostalCode5Digits_Success, insertAddress_BoundaryPostalCode6Digits_Success, insertAddress_CaseInsensitiveHome_Success, insertAddress_CaseInsensitiveWork_Success, insertAddress_IsDeletedTrue_Success, insertAddress_IsPrimaryFalse_Success, insertAddress_IsPrimaryTrue_Success, insertAddress_LogsSuccessMessageAndRoute_Success, insertAddress_MaxLongClientId_Success, insertAddress_MaxLongUserId_Success, insertAddress_MixedCaseAddressType_Success, insertAddress_MultipleValidationsPass_Success, insertAddress_NullBooleanFields_DefaultsApplied, insertAddress_NullClientId_Success, insertAddress_NullUserId_Success, insertAddress_postalCode_123456_success, insertAddress_postalCode_12345_success, insertAddress_PostalCodeWithLeadingZeros_Success, insertAddress_SpecialCharactersInStreetAddress_Success, insertAddress_StreetAddressWithSpaces_SuccessTrimsSpaces, insertAddress_UnicodeCharactersInCity_Success, insertAddress_ValidRequest_Success, insertAddress_VeryLongStreetAddress_Success

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: insertAddress_EmptyCountry_ThrowsBadRequestException, insertAddress_EmptyPostalCode_ThrowsBadRequestException, insertAddress_EmptyState_ThrowsBadRequestException, insertAddress_EmptyStreetAddress_ThrowsBadRequestException, insertAddress_InvalidAddressTypeHome123_ThrowsBadRequestException, insertAddress_InvalidAddressTypeInvalid_ThrowsBadRequestException, insertAddress_InvalidAddressTypeResidential_ThrowsBadRequestException, insertAddress_InvalidAddressTypeCommercial_ThrowsBadRequestException, insertAddress_InvalidAddressTypeEmpty_ThrowsBadRequestException, insertAddress_InvalidCreatedUser_ThrowsBadRequestException, insertAddress_InvalidPostalCodeLetters_ThrowsBadRequestException, insertAddress_InvalidPostalCodeHyphens_ThrowsBadRequestException, insertAddress_InvalidPostalCodePeriods_ThrowsBadRequestException, insertAddress_InvalidPostalCodeSpaces_ThrowsBadRequestException, insertAddress_NegativeClientId_ThrowsBadRequestException, insertAddress_NegativeUserId_ThrowsBadRequestException, insertAddress_NullAddressType_ThrowsBadRequestException, insertAddress_NullCountry_ThrowsBadRequestException, insertAddress_NullPostalCode_ThrowsBadRequestException, insertAddress_NullRequest_ThrowsBadRequestException, insertAddress_NullState_ThrowsBadRequestException, insertAddress_PostalCode4Digits_ThrowsBadRequestException, insertAddress_PostalCode7Digits_ThrowsBadRequestException, insertAddress_WhitespaceCountry_ThrowsBadRequestException, insertAddress_WhitespacePostalCode_ThrowsBadRequestException, insertAddress_WhitespaceState_ThrowsBadRequestException, insertAddress_WhitespaceStreetAddress_ThrowsBadRequestException, insertAddress_ZeroClientId_ThrowsBadRequestException, insertAddress_ZeroUserId_ThrowsBadRequestException
- Required order: insertAddress_EmptyCountry_ThrowsBadRequestException, insertAddress_EmptyPostalCode_ThrowsBadRequestException, insertAddress_EmptyState_ThrowsBadRequestException, insertAddress_EmptyStreetAddress_ThrowsBadRequestException, insertAddress_InvalidAddressTypeCommercial_ThrowsBadRequestException, insertAddress_InvalidAddressTypeEmpty_ThrowsBadRequestException, insertAddress_InvalidAddressTypeHome123_ThrowsBadRequestException, insertAddress_InvalidAddressTypeInvalid_ThrowsBadRequestException, insertAddress_InvalidAddressTypeResidential_ThrowsBadRequestException, insertAddress_InvalidCreatedUser_ThrowsBadRequestException, insertAddress_InvalidPostalCodeHyphens_ThrowsBadRequestException, insertAddress_InvalidPostalCodeLetters_ThrowsBadRequestException, insertAddress_InvalidPostalCodePeriods_ThrowsBadRequestException, insertAddress_InvalidPostalCodeSpaces_ThrowsBadRequestException, insertAddress_NegativeClientId_ThrowsBadRequestException, insertAddress_NegativeUserId_ThrowsBadRequestException, insertAddress_NullAddressType_ThrowsBadRequestException, insertAddress_NullCountry_ThrowsBadRequestException, insertAddress_NullPostalCode_ThrowsBadRequestException, insertAddress_NullRequest_ThrowsBadRequestException, insertAddress_NullState_ThrowsBadRequestException, insertAddress_PostalCode4Digits_ThrowsBadRequestException, insertAddress_PostalCode7Digits_ThrowsBadRequestException, insertAddress_WhitespaceCountry_ThrowsBadRequestException, insertAddress_WhitespacePostalCode_ThrowsBadRequestException, insertAddress_WhitespaceState_ThrowsBadRequestException, insertAddress_WhitespaceStreetAddress_ThrowsBadRequestException, insertAddress_ZeroClientId_ThrowsBadRequestException, insertAddress_ZeroUserId_ThrowsBadRequestException

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 9 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Address/ToggleAddressTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Address
Class: class ToggleAddressTest extends AddressServiceTestBase {
Extends: AddressServiceTestBase
Lines of Code: 335
Last Modified: 2026-02-10 13:16:24
Declared Test Count: 13 (first occurrence line 21)
Actual @Test Count: 13

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 267 (toggleAddress_controller_permission_forbidden)
- Problem: no http status assertion detected
- Required: Call controller method and assert HTTP status.

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Address/GetAddressByIdTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Address
Class: class GetAddressByIdTest extends AddressServiceTestBase {
Extends: AddressServiceTestBase
Lines of Code: 283
Last Modified: 2026-02-10 13:17:01
Declared Test Count: 11 (first occurrence line 21)
Actual @Test Count: 11

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 210 (getAddressById_controller_permission_forbidden)
- Problem: no http status assertion detected
- Required: Call controller method and assert HTTP status.

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Address/GetAddressByUserIdTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Address
Class: class GetAddressByUserIdTest extends AddressServiceTestBase {
Extends: AddressServiceTestBase
Lines of Code: 298
Last Modified: 2026-02-10 13:17:38
Declared Test Count: 12 (first occurrence line 25)
Actual @Test Count: 12

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 229 (getAddressByUserId_controller_permission_forbidden)
- Problem: no http status assertion detected
- Required: Call controller method and assert HTTP status.

VIOLATION 2: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 255 method `getAddressesByUserId_VerifyPreAuthorizeAnnotation_Success`
- Required rename: `getAddressByUserId_VerifyPreAuthorizeAnnotation_Success`
- Line: 287 method `getAddressesByUserId_WithValidRequest_DelegatesToService`
- Required rename: `getAddressByUserId_WithValidRequest_DelegatesToService`

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Address/GetAddressByClientIdTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Address
Class: class GetAddressByClientIdTest extends AddressServiceTestBase {
Extends: AddressServiceTestBase
Lines of Code: 423
Last Modified: 2026-02-10 13:18:09
Declared Test Count: 16 (first occurrence line 28)
Actual @Test Count: 16

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 354 (getAddressByClientId_controller_permission_forbidden)
- Problem: no http status assertion detected
- Required: Call controller method and assert HTTP status.

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Address/UpdateAddressTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Address
Class: class UpdateAddressTest extends AddressServiceTestBase {
Extends: AddressServiceTestBase
Lines of Code: 695
Last Modified: 2026-02-10 13:18:58
Declared Test Count: 31 (first occurrence line 25)
Actual @Test Count: 31

VIOLATIONS FOUND:

VIOLATION 1: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Line: 624 (updateAddress_controller_permission_forbidden)
- Problem: no http status assertion detected
- Required: Call controller method and assert HTTP status.

REQUIRED FIXES SUMMARY:
- Fix Rule 3 issues above.


**IMPLEMENTATION PLAN (STEP-BY-STEP)**
1. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Address/InsertAddressTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
2. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Address/ToggleAddressTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
3. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Address/GetAddressByIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
4. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Address/GetAddressByUserIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
5. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Address/GetAddressByClientIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
6. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Address/UpdateAddressTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.

Verification Commands (run after fixes):
- mvn -Dtest=InsertAddressTest test
- mvn -Dtest=ToggleAddressTest test
- mvn -Dtest=GetAddressByIdTest test
- mvn -Dtest=GetAddressByUserIdTest test
- mvn -Dtest=GetAddressByClientIdTest test
- mvn -Dtest=UpdateAddressTest test