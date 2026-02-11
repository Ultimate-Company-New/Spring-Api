# UNIT TEST VERIFICATION REPORT — Product

```
╔════════════════════════════════════════════════════════════╗
║           UNIT TEST VERIFICATION REPORT                    ║
║                                                            ║
║  Status: ❌ RED                                               ║
║  Services Analyzed: 1                                      ║
║  Public Methods Found: 11                                 ║
║  Test Files Expected: 11                                 ║
║  Test Files Found: 10                                    ║
║  Total Violations: 93                                    ║
╚════════════════════════════════════════════════════════════╝
```

VIOLATIONS BY RULE:

| Rule | Description | Count |
| --- | --- | --- |
| 1 | One Test File per Method | 3 |
| 2 | Test Count Declaration | 10 |
| 3 | Controller Permission Test | 10 |
| 5 | Test Naming Convention | 9 |
| 6 | Centralized Mocking | 9 |
| 7 | Exception Assertions | 3 |
| 8 | Error Constants | 3 |
| 9 | Test Documentation | 9 |
| 10 | Test Ordering | 12 |
| 11 | Complete Coverage | 1 |
| 12 | Arrange/Act/Assert | 10 |
| 14 | No Inline Mocks | 14 |


**MISSING/EXTRA TEST FILES (RULE 1)**

══════════════════════════════════════════════════════════════════════
MISSING FILE: GetProductInBatchesTest.java
══════════════════════════════════════════════════════════════════════
Service: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/main/java/com/example/SpringApi/Services/ProductService.java`
Method: `getProductInBatches` (line 293)
Expected Test Path: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/GetProductInBatchesTest.java`
Required Minimum Tests:
- getProductInBatches_success
- getProductInBatches_controller_permission_forbidden
- Add failure tests for each validation/exception path in the service method body.
Required Stubs:
- Add stub methods in the base test class for each repository/service interaction in the method.

══════════════════════════════════════════════════════════════════════
MISSING FILE: GetProductStockAtLocationsByProductIdTest.java
══════════════════════════════════════════════════════════════════════
Service: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/main/java/com/example/SpringApi/Services/ProductService.java`
Method: `getProductStockAtLocationsByProductId` (line 1165)
Expected Test Path: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/GetProductStockAtLocationsByProductIdTest.java`
Required Minimum Tests:
- getProductStockAtLocationsByProductId_success
- getProductStockAtLocationsByProductId_controller_permission_forbidden
- Add failure tests for each validation/exception path in the service method body.
Required Stubs:
- Add stub methods in the base test class for each repository/service interaction in the method.
Extra test file with no matching public method: `GetProductsInBatchesTest.java`. Either rename it to match a public method or remove it.


**BASE TEST FILE ISSUES**
Base Test: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/ProductServiceTestBase.java`
- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line 145: `lenient().when(environment.getActiveProfiles()).thenReturn(new String[] { "test" });`. Move this into a `stub...` method.
- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line 169: `lenient().when(clientRepository.findById(anyLong())).thenReturn(Optional.of(testClient));`. Move this into a `stub...` method.
- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line 170: `lenient().when(clientService.getClientById(anyLong())).thenReturn(testClientResponse);`. Move this into a `stub...` method.
- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line 171: `lenient().when(productCategoryRepository.findById(anyLong())).thenReturn(Optional.of(testCategory));`. Move this into a `stub...` method.
- Rule 14 [CRITICAL]: inline mock in @BeforeEach at line 172: `lenient().when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {`. Move this into a `stub...` method.


**FILE-BY-FILE BREAKDOWN**

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/GetCategoryPathsByIdsTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Product
Class: * Consolidated test class for ProductService.getCategoryPathsByIds.
Extends: None
Lines of Code: 162
Last Modified: 2026-02-11 00:58:16
Declared Test Count: MISSING/MISPLACED (first occurrence line 27)
Actual @Test Count: 8

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 27
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 8` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 121 has mock usage `ProductService mockService = mock(ProductService.class);`
- Required: Move mocks to base test file.
- Line: 137 has mock usage `ProductService mockService = mock(ProductService.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getCategoryPathsByIds_controller_permission_forbidden` or `getCategoryPathsByIds_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 110 method `getCategoryPathsByIds_VerifyPreAuthorizeAnnotation`
- Required rename: `getCategoryPathsByIds_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getCategoryPathsByIds_VerifyPreAuthorizeAnnotation (line 108), getCategoryPathsByIds_ControllerDelegation_Success (line 117), getCategoryPathsByIds_NoPermission_Unauthorized (line 133), getCategoryPathsByIds_InvalidId_Skips (line 150)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 82 in `getCategoryPathsByIds_EmptyInput_Success` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 96 in `getCategoryPathsByIds_NullList_Success` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 110 in `getCategoryPathsByIds_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 46 inline mock in `getCategoryPathsByIds_MultipleLevels_Success`: `when(productCategoryRepository.findById(TEST_CATEGORY_ID)).thenReturn(Optional.of(testCategory));`
- Required: Move to base test stub method and call stub in test.
- Line: 47 inline mock in `getCategoryPathsByIds_MultipleLevels_Success`: `when(productCategoryRepository.findById(1L)).thenReturn(Optional.of(parent));`
- Required: Move to base test stub method and call stub in test.
- Line: 67 inline mock in `getCategoryPathsByIds_RootLevel_Success`: `when(productCategoryRepository.findById(TEST_CATEGORY_ID)).thenReturn(Optional.of(testCategory));`
- Required: Move to base test stub method and call stub in test.
- Line: 123 inline mock in `getCategoryPathsByIds_ControllerDelegation_Success`: `when(mockService.getCategoryPathsByIds(any())).thenReturn(Collections.emptyMap());`
- Required: Move to base test stub method and call stub in test.
- Line: 139 inline mock in `getCategoryPathsByIds_NoPermission_Unauthorized`: `when(mockService.getCategoryPathsByIds(any()))`
- Required: Move to base test stub method and call stub in test.
- Line: 154 inline mock in `getCategoryPathsByIds_InvalidId_Skips`: `when(productCategoryRepository.findById(999L)).thenReturn(Optional.empty());`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: FAILURE, PERMISSION
- Required: Add Success, Failure, Permission section headers.

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
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/ToggleDeleteProductTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Product
Class: * Consolidated test class for ProductService.toggleDeleteProduct.
Extends: None
Lines of Code: 171
Last Modified: 2026-02-10 23:11:31
Declared Test Count: MISSING/MISPLACED (first occurrence line 24)
Actual @Test Count: 9

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 24
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 9` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 131 has mock usage `ProductService mockService = mock(ProductService.class);`
- Required: Move mocks to base test file.
- Line: 147 has mock usage `ProductService mockService = mock(ProductService.class);`
- Required: Move mocks to base test file.
- Line: 164 has mock usage `ProductService mockService = mock(ProductService.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `toggleDeleteProduct_controller_permission_forbidden` or `toggleDeleteProduct_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 120 method `toggleDeleteProduct_VerifyPreAuthorizeAnnotation`
- Required rename: `toggleDeleteProduct_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: toggleDeleteProduct_VerifyPreAuthorizeAnnotation (line 118), toggleDeleteProduct_ControllerDelegation_Success (line 127), toggleDeleteProduct_NoPermission_Unauthorized (line 143), toggleDeleteProduct_NoPermission_Forbidden (line 160)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 120 in `toggleDeleteProduct_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 133 inline mock in `toggleDeleteProduct_ControllerDelegation_Success`: `doNothing().when(mockService).toggleDeleteProduct(TEST_PRODUCT_ID);`
- Required: Move to base test stub method and call stub in test.
- Line: 149 inline mock in `toggleDeleteProduct_NoPermission_Unauthorized`: `doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 150 inline mock in `toggleDeleteProduct_NoPermission_Unauthorized`: `.when(mockService).toggleDeleteProduct(TEST_PRODUCT_ID);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 85 in `toggleDeleteProduct_NotFound_ThrowsNotFound`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 98 in `toggleDeleteProduct_IdZero_ThrowsNotFound`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 111 in `toggleDeleteProduct_IdNegative_ThrowsNotFound`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: PERMISSION
- Required: Add Success, Failure, Permission section headers.

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
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/FindCategoriesByParentIdTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Product
Class: * Consolidated test class for ProductService.findCategoriesByParentId.
Extends: None
Lines of Code: 146
Last Modified: 2026-02-10 23:11:31
Declared Test Count: MISSING/MISPLACED (first occurrence line 27)
Actual @Test Count: 7

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 27
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 7` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 96 has mock usage `ProductService mockService = mock(ProductService.class);`
- Required: Move mocks to base test file.
- Line: 112 has mock usage `ProductService mockService = mock(ProductService.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `findCategoriesByParentId_controller_permission_forbidden` or `findCategoriesByParentId_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 85 method `findCategoriesByParentId_VerifyPreAuthorizeAnnotation`
- Required rename: `findCategoriesByParentId_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: findCategoriesByParentId_VerifyPreAuthorizeAnnotation (line 83), findCategoriesByParentId_ControllerDelegation_Success (line 92), findCategoriesByParentId_NoPermission_Unauthorized (line 108), findCategoriesByParentId_EmptyResults_Success (line 125), findCategoriesByParentId_InvalidParentId_Success (line 136)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 85 in `findCategoriesByParentId_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 43 inline mock in `findCategoriesByParentId_Root_Success`: `when(productCategoryRepository.findAll())`
- Required: Move to base test stub method and call stub in test.
- Line: 66 inline mock in `findCategoriesByParentId_Child_Success`: `when(productCategoryRepository.findAll())`
- Required: Move to base test stub method and call stub in test.
- Line: 69 inline mock in `findCategoriesByParentId_Child_Success`: `when(productCategoryRepository.findById(10L)).thenReturn(Optional.empty()); // Stop path traversal`
- Required: Move to base test stub method and call stub in test.
- Line: 98 inline mock in `findCategoriesByParentId_ControllerDelegation_Success`: `when(mockService.findCategoriesByParentId(any())).thenReturn(Collections.emptyList());`
- Required: Move to base test stub method and call stub in test.
- Line: 114 inline mock in `findCategoriesByParentId_NoPermission_Unauthorized`: `doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 115 inline mock in `findCategoriesByParentId_NoPermission_Unauthorized`: `.when(mockService).findCategoriesByParentId(any());`
- Required: Move to base test stub method and call stub in test.
- Line: 129 inline mock in `findCategoriesByParentId_EmptyResults_Success`: `when(productCategoryRepository.findAll()).thenReturn(Collections.emptyList());`
- Required: Move to base test stub method and call stub in test.
- Line: 140 inline mock in `findCategoriesByParentId_InvalidParentId_Success`: `when(productCategoryRepository.findAll()).thenReturn(Collections.emptyList());`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: FAILURE, PERMISSION
- Required: Add Success, Failure, Permission section headers.

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
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/BulkAddProductsAsyncTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Product
Class: * Consolidated test class for ProductService.bulkAddProductsAsync.
Extends: None
Lines of Code: 169
Last Modified: 2026-02-10 15:48:55
Declared Test Count: MISSING/MISPLACED (first occurrence line 23)
Actual @Test Count: 8

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 23
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 8` immediately after the class opening brace.

VIOLATION 2: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `bulkAddProductsAsync_controller_permission_forbidden` or `bulkAddProductsAsync_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 3: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: bulkAddProductsAsync_PartialInvalid_Success (line 161)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 4: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 85 in `bulkAddProductsAsync_EmptyList_Success` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 99 in `bulkAddProductsAsync_NullList_Success` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 135 in `bulkAddProductsAsync_NullUserId_Success` missing AAA comments: Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 150 in `bulkAddProductsAsync_NullClientId_Success` missing AAA comments: Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 163 in `bulkAddProductsAsync_PartialInvalid_Success` missing AAA comments: Arrange
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 5: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: FAILURE, PERMISSION
- Required: Add Success, Failure, Permission section headers.

VIOLATION 6: Rule 11 - Complete Coverage
- Severity: HIGH
- Coverage by test names is incomplete.
- Missing: at least one failure/exception test (e.g., *_throws*, *_exception*, *_invalid*).

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 3 issues above.
- Fix Rule 9 issues above.
- Fix Rule 12 issues above.
- Fix Rule 10 issues above.
- Fix Rule 11 issues above.

======================================================================
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/EditProductTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Product
Class: * Consolidated test class for ProductService.editProduct.
Extends: None
Lines of Code: 286
Last Modified: 2026-02-10 23:11:31
Declared Test Count: MISSING/MISPLACED (first occurrence line 33)
Actual @Test Count: 12

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 33
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 12` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 258 has mock usage `ProductService mockService = mock(ProductService.class);`
- Required: Move mocks to base test file.
- Line: 274 has mock usage `ProductService mockService = mock(ProductService.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `editProduct_controller_permission_forbidden` or `editProduct_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 247 method `editProduct_VerifyPreAuthorizeAnnotation`
- Required rename: `editProduct_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: editProduct_VerifyPreAuthorizeAnnotation (line 245), editProduct_ControllerDelegation_Success (line 254), editProduct_NoPermission_Unauthorized (line 270)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 247 in `editProduct_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 260 inline mock in `editProduct_ControllerDelegation_Success`: `doNothing().when(mockService).editProduct(testProductRequest);`
- Required: Move to base test stub method and call stub in test.
- Line: 276 inline mock in `editProduct_NoPermission_Unauthorized`: `doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 277 inline mock in `editProduct_NoPermission_Unauthorized`: `.when(mockService).editProduct(any(ProductRequestModel.class));`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 8 - Error Constants
- Severity: HIGH
- Line: 238 has hardcoded message: `assertTrue(exception.getMessage().contains("image"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 238 has hardcoded message: `assertTrue(exception.getMessage().contains("image"));`
- Required: Replace with an ErrorMessages constant (add one if missing).

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: PERMISSION
- Required: Add Success, Failure, Permission section headers.

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
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/GetProductDetailsByIdTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Product
Class: * Consolidated test class for ProductService.getProductDetailsById.
Extends: None
Lines of Code: 138
Last Modified: 2026-02-10 23:11:31
Declared Test Count: MISSING/MISPLACED (first occurrence line 24)
Actual @Test Count: 7

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 24
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 7` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 110 has mock usage `ProductService mockService = mock(ProductService.class);`
- Required: Move mocks to base test file.
- Line: 126 has mock usage `ProductService mockService = mock(ProductService.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getProductDetailsById_controller_permission_forbidden` or `getProductDetailsById_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 99 method `getProductDetailsById_VerifyPreAuthorizeAnnotation`
- Required rename: `getProductDetailsById_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getProductDetailsById_VerifyPreAuthorizeAnnotation (line 97), getProductDetailsById_ControllerDelegation_Success (line 106), getProductDetailsById_NoPermission_Unauthorized (line 122)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 99 in `getProductDetailsById_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 112 inline mock in `getProductDetailsById_ControllerDelegation_Success`: `when(mockService.getProductDetailsById(TEST_PRODUCT_ID)).thenReturn(new ProductResponseModel(testProduct));`
- Required: Move to base test stub method and call stub in test.
- Line: 128 inline mock in `getProductDetailsById_NoPermission_Unauthorized`: `doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 129 inline mock in `getProductDetailsById_NoPermission_Unauthorized`: `.when(mockService).getProductDetailsById(TEST_PRODUCT_ID);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 64 in `getProductDetailsById_NotFound_ThrowsNotFound`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 77 in `getProductDetailsById_IdZero_ThrowsNotFound`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 90 in `getProductDetailsById_IdNegative_ThrowsNotFound`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: PERMISSION
- Required: Add Success, Failure, Permission section headers.

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
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/BulkAddProductsTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Product
Class: * Consolidated test class for ProductService.bulkAddProducts.
Extends: None
Lines of Code: 165
Last Modified: 2026-02-10 23:11:31
Declared Test Count: MISSING/MISPLACED (first occurrence line 30)
Actual @Test Count: 7

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 30
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 7` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 152 has mock usage `ProductService mockService = mock(ProductService.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `bulkAddProducts_controller_permission_forbidden` or `bulkAddProducts_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 141 method `bulkAddProducts_VerifyPreAuthorizeAnnotation`
- Required rename: `bulkAddProducts_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: bulkAddProducts_VerifyPreAuthorizeAnnotation (line 139), bulkAddProducts_NoPermission_Unauthorized (line 148)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 101 in `bulkAddProducts_EmptyList_ThrowsBadRequest` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 111 in `bulkAddProducts_NullList_ThrowsBadRequest` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).
- Line: 141 in `bulkAddProducts_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 125 inline mock in `bulkAddProducts_DatabaseError_Success`: `when(productRepository.save(any())).thenThrow(new RuntimeException("DB Error"));`
- Required: Move to base test stub method and call stub in test.
- Line: 155 inline mock in `bulkAddProducts_NoPermission_Unauthorized`: `doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 156 inline mock in `bulkAddProducts_NoPermission_Unauthorized`: `.when(mockService).bulkAddProducts(requests);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: PERMISSION
- Required: Add Success, Failure, Permission section headers.

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
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/ToggleReturnProductTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Product
Class: * Consolidated test class for ProductService.toggleReturnProduct.
Extends: None
Lines of Code: 164
Last Modified: 2026-02-10 23:11:31
Declared Test Count: MISSING/MISPLACED (first occurrence line 24)
Actual @Test Count: 8

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 24
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 8` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 136 has mock usage `ProductService mockService = mock(ProductService.class);`
- Required: Move mocks to base test file.
- Line: 152 has mock usage `ProductService mockService = mock(ProductService.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `toggleReturnProduct_controller_permission_forbidden` or `toggleReturnProduct_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 125 method `toggleReturnProduct_VerifyPreAuthorizeAnnotation`
- Required rename: `toggleReturnProduct_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: toggleReturnProduct_VerifyPreAuthorizeAnnotation (line 123), toggleReturnProduct_ControllerDelegation_Success (line 132), toggleReturnProduct_NoPermission_Unauthorized (line 148)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 125 in `toggleReturnProduct_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 138 inline mock in `toggleReturnProduct_ControllerDelegation_Success`: `doNothing().when(mockService).toggleReturnProduct(TEST_PRODUCT_ID);`
- Required: Move to base test stub method and call stub in test.
- Line: 154 inline mock in `toggleReturnProduct_NoPermission_Unauthorized`: `doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 155 inline mock in `toggleReturnProduct_NoPermission_Unauthorized`: `.when(mockService).toggleReturnProduct(TEST_PRODUCT_ID);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 7 - Exception Assertions
- Severity: HIGH
- Line: 103 in `toggleReturnProduct_NotFound_ThrowsNotFound`
- Required: Capture exception and assert exact message using ErrorMessages constant.
- Line: 116 in `toggleReturnProduct_IdZero_ThrowsNotFound`
- Required: Capture exception and assert exact message using ErrorMessages constant.

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: PERMISSION
- Required: Add Success, Failure, Permission section headers.

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
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/GetProductsInBatchesTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Product
Class: * Consolidated test class for ProductService.getProductInBatches.
Extends: None
Lines of Code: 312
Last Modified: 2026-02-11 01:18:18
Declared Test Count: MISSING/MISPLACED (first occurrence line 36)
Actual @Test Count: 15

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 36
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 15` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 274 has mock usage `ProductService mockService = mock(ProductService.class);`
- Required: Move mocks to base test file.
- Line: 291 has mock usage `ProductService mockService = mock(ProductService.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `getProductsInBatches_controller_permission_forbidden` or `getProductsInBatches_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 58 method `getProductInBatches_NoFilters_Success`
- Required rename: `getProductsInBatches_NoFilters_Success`
- Line: 81 method `getProductInBatches_TitleContains_Success`
- Required rename: `getProductsInBatches_TitleContains_Success`
- Line: 95 method `getProductInBatches_PriceGreaterThan_Success`
- Required rename: `getProductsInBatches_PriceGreaterThan_Success`
- Line: 109 method `getProductInBatches_MultipleFiltersAnd_Success`
- Required rename: `getProductsInBatches_MultipleFiltersAnd_Success`
- Line: 128 method `getProductInBatches_MultipleFiltersOr_Success`
- Required rename: `getProductsInBatches_MultipleFiltersOr_Success`
- Line: 147 method `getProductInBatches_IncludeDeleted_Success`
- Required rename: `getProductsInBatches_IncludeDeleted_Success`
- Line: 163 method `getProductInBatches_PickupLocationFilter_Success`
- Required rename: `getProductsInBatches_PickupLocationFilter_Success`
- Line: 181 method `getProductInBatches_InvalidRange_ThrowsBadRequest`
- Required rename: `getProductsInBatches_InvalidRange_ThrowsBadRequest`
- Line: 197 method `getProductInBatches_ZeroPageSize_ThrowsBadRequest`
- Required rename: `getProductsInBatches_ZeroPageSize_ThrowsBadRequest`
- Line: 213 method `getProductInBatches_UnknownColumn_ThrowsBadRequest`
- Required rename: `getProductsInBatches_UnknownColumn_ThrowsBadRequest`
- Line: 229 method `getProductInBatches_InvalidOperatorForType_ThrowsBadRequest`
- Required rename: `getProductsInBatches_InvalidOperatorForType_ThrowsBadRequest`
- Line: 245 method `getProductInBatches_MalformedOperator_ThrowsBadRequest`
- Required rename: `getProductsInBatches_MalformedOperator_ThrowsBadRequest`
- Line: 262 method `getProductInBatches_VerifyPreAuthorizeAnnotation`
- Required rename: `getProductInBatches_VerifyPreAuthorizeAnnotation_Success`
- Line: 262 method `getProductInBatches_VerifyPreAuthorizeAnnotation`
- Required rename: `getProductInBatches_VerifyPreAuthorizeAnnotation_Success`
- Line: 272 method `getProductInBatches_ControllerDelegation_Success`
- Required rename: `getProductsInBatches_ControllerDelegation_Success`
- Line: 289 method `getProductInBatches_NoPermission_Unauthorized`
- Required rename: `getProductsInBatches_NoPermission_Unauthorized`

VIOLATION 5: Rule 9 - Test Documentation
- Severity: MEDIUM
- Missing documentation blocks for: getProductInBatches_VerifyPreAuthorizeAnnotation (line 260), getProductInBatches_ControllerDelegation_Success (line 270), getProductInBatches_NoPermission_Unauthorized (line 287)
- Required: Add /* Purpose / Expected Result / Assertions */ above each @Test.

VIOLATION 6: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 262 in `getProductInBatches_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 7: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 63 inline mock in `getProductInBatches_NoFilters_Success`: `when(productFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(anyLong(), any(), anyString(),`
- Required: Move to base test stub method and call stub in test.
- Line: 277 inline mock in `getProductInBatches_ControllerDelegation_Success`: `when(mockService.getProductInBatches(request)).thenReturn(new PaginationBaseResponseModel<>());`
- Required: Move to base test stub method and call stub in test.
- Line: 294 inline mock in `getProductInBatches_NoPermission_Unauthorized`: `doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 295 inline mock in `getProductInBatches_NoPermission_Unauthorized`: `.when(mockService).getProductInBatches(request);`
- Required: Move to base test stub method and call stub in test.

VIOLATION 8: Rule 8 - Error Constants
- Severity: HIGH
- Line: 221 has hardcoded message: `assertTrue(exception.getMessage().contains("nonExistent"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 221 has hardcoded message: `assertTrue(exception.getMessage().contains("nonExistent"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 237 has hardcoded message: `assertTrue(exception.getMessage().contains("greaterThan"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 237 has hardcoded message: `assertTrue(exception.getMessage().contains("greaterThan"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 253 has hardcoded message: `assertTrue(exception.getMessage().contains("unknown_op"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 253 has hardcoded message: `assertTrue(exception.getMessage().contains("unknown_op"));`
- Required: Replace with an ErrorMessages constant (add one if missing).

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Missing sections: PERMISSION
- Required: Add Success, Failure, Permission section headers.

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
FILE: `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/AddProductTest.java`
======================================================================
Package: com.example.SpringApi.Services.Tests.Product
Class: * Consolidated test class for ProductService.addProduct.
Extends: None
Lines of Code: 667
Last Modified: 2026-02-10 23:11:31
Declared Test Count: MISSING/MISPLACED (first occurrence line 34)
Actual @Test Count: 34

VIOLATIONS FOUND:

VIOLATION 1: Rule 2 - Test Count Declaration
- Severity: CRITICAL
- Line: 34
- Problem: `// Total Tests: X` is missing or not the first line inside class body.
- Required: Insert `// Total Tests: 34` immediately after the class opening brace.

VIOLATION 2: Rule 6 - Centralized Mocking
- Severity: HIGH
- Line: 525 has mock usage `ProductService mockService = mock(ProductService.class);`
- Required: Move mocks to base test file.
- Line: 545 has mock usage `ProductService mockService = mock(ProductService.class);`
- Required: Move mocks to base test file.

VIOLATION 3: Rule 3 - Controller Permission Test
- Severity: CRITICAL
- Problem: No controller permission test found.
- Required: Add `addProduct_controller_permission_forbidden` or `addProduct_controller_permission_unauthorized` under the PERMISSION section. Ensure it calls controller and asserts HttpStatus 401/403.

VIOLATION 4: Rule 5 - Test Naming Convention
- Severity: MEDIUM
- Line: 511 method `addProduct_VerifyPreAuthorizeAnnotation`
- Required rename: `addProduct_VerifyPreAuthorizeAnnotation_Success`

VIOLATION 5: Rule 12 - Arrange/Act/Assert
- Severity: MEDIUM
- Line: 511 in `addProduct_VerifyPreAuthorizeAnnotation` missing AAA comments: Arrange, Act, Assert
- Required: Add `// Arrange`, `// Act`, `// Assert` (or `// Act & Assert`).

VIOLATION 6: Rule 14 - No Inline Mocks
- Severity: CRITICAL
- Line: 527 inline mock in `addProduct_ControllerDelegation_Success`: `doNothing().when(mockService).addProduct(testProductRequest);`
- Required: Move to base test stub method and call stub in test.
- Line: 547 inline mock in `addProduct_NoPermission_Unauthorized`: `doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))`
- Required: Move to base test stub method and call stub in test.
- Line: 548 inline mock in `addProduct_NoPermission_Unauthorized`: `.when(mockService).addProduct(any(ProductRequestModel.class));`
- Required: Move to base test stub method and call stub in test.

VIOLATION 7: Rule 8 - Error Constants
- Severity: HIGH
- Line: 303 has hardcoded message: `assertTrue(exception.getMessage().contains("main"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 303 has hardcoded message: `assertTrue(exception.getMessage().contains("main"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 356 has hardcoded message: `assertTrue(exception.getMessage().contains("Failed to upload"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 356 has hardcoded message: `assertTrue(exception.getMessage().contains("Failed to upload"));`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 480 has hardcoded message: `assertTrue(exception.getMessage().contains("Failed to process image from URL:")); // URL processing failure`
- Required: Replace with an ErrorMessages constant (add one if missing).
- Line: 480 has hardcoded message: `assertTrue(exception.getMessage().contains("Failed to process image from URL:")); // URL processing failure`
- Required: Replace with an ErrorMessages constant (add one if missing).

VIOLATION 8: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section SUCCESS not alphabetical.
- Current order: addProduct_ValidRequest_Success, addProduct_OptionalImagesNull_Success, addProduct_LongTitle_Success, addProduct_SpecialCharsInDescription_Success, addProduct_NegativeClientId_Success
- Required order: addProduct_LongTitle_Success, addProduct_NegativeClientId_Success, addProduct_OptionalImagesNull_Success, addProduct_SpecialCharsInDescription_Success, addProduct_ValidRequest_Success

VIOLATION 9: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section FAILURE not alphabetical.
- Current order: addProduct_TitleNull_ThrowsBadRequest, addProduct_TitleEmpty_ThrowsBadRequest, addProduct_DescriptionNull_ThrowsBadRequest, addProduct_BrandNull_ThrowsBadRequest, addProduct_CategoryIdNull_ThrowsBadRequest, addProduct_CategoryNotFound_ThrowsNotFound, addProduct_PriceNull_ThrowsBadRequest, addProduct_PriceNegative_ThrowsBadRequest, addProduct_MainImageNull_ThrowsBadRequest, addProduct_PickupQuantitiesEmpty_ThrowsBadRequest, addProduct_ClientIdZero_ThrowsBadRequest, addProduct_ImgbbFails_ThrowsBadRequest, addProduct_NegativeWeight_ThrowsBadRequest, addProduct_ZeroHeight_ThrowsBadRequest, addProduct_ZeroLength_ThrowsBadRequest, addProduct_ZeroBreadth_ThrowsBadRequest, addProduct_ColorLabelEmpty_ThrowsBadRequest, addProduct_ConditionEmpty_ThrowsBadRequest, addProduct_CountryEmpty_ThrowsBadRequest, addProduct_MalformedUrl_ThrowsBadRequest, addProduct_NegativePickupQuantity_ThrowsBadRequest
- Required order: addProduct_BrandNull_ThrowsBadRequest, addProduct_CategoryIdNull_ThrowsBadRequest, addProduct_CategoryNotFound_ThrowsNotFound, addProduct_ClientIdZero_ThrowsBadRequest, addProduct_ColorLabelEmpty_ThrowsBadRequest, addProduct_ConditionEmpty_ThrowsBadRequest, addProduct_CountryEmpty_ThrowsBadRequest, addProduct_DescriptionNull_ThrowsBadRequest, addProduct_ImgbbFails_ThrowsBadRequest, addProduct_MainImageNull_ThrowsBadRequest, addProduct_MalformedUrl_ThrowsBadRequest, addProduct_NegativePickupQuantity_ThrowsBadRequest, addProduct_NegativeWeight_ThrowsBadRequest, addProduct_PickupQuantitiesEmpty_ThrowsBadRequest, addProduct_PriceNegative_ThrowsBadRequest, addProduct_PriceNull_ThrowsBadRequest, addProduct_TitleEmpty_ThrowsBadRequest, addProduct_TitleNull_ThrowsBadRequest, addProduct_ZeroBreadth_ThrowsBadRequest, addProduct_ZeroHeight_ThrowsBadRequest, addProduct_ZeroLength_ThrowsBadRequest

VIOLATION 10: Rule 10 - Test Ordering
- Severity: MEDIUM
- Section PERMISSION not alphabetical.
- Current order: addProduct_VerifyPreAuthorizeAnnotation, addProduct_ControllerDelegation_Success, addProduct_NoPermission_Unauthorized, addProduct_MinimumValidDimensions_Success, addProduct_OnlyRequiredImages_Success, addProduct_PartialOptionalImages_Success, addProduct_AllOptionalImagesPresent_Success, addProduct_ZeroWeight_Success
- Required order: addProduct_AllOptionalImagesPresent_Success, addProduct_ControllerDelegation_Success, addProduct_MinimumValidDimensions_Success, addProduct_NoPermission_Unauthorized, addProduct_OnlyRequiredImages_Success, addProduct_PartialOptionalImages_Success, addProduct_VerifyPreAuthorizeAnnotation, addProduct_ZeroWeight_Success

REQUIRED FIXES SUMMARY:
- Fix Rule 2 issues above.
- Fix Rule 6 issues above.
- Fix Rule 3 issues above.
- Fix Rule 5 issues above.
- Fix Rule 12 issues above.
- Fix Rule 14 issues above.
- Fix Rule 8 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.
- Fix Rule 10 issues above.


**IMPLEMENTATION PLAN (STEP-BY-STEP)**
1. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/GetCategoryPathsByIdsTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
2. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/ToggleDeleteProductTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
3. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/FindCategoriesByParentIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
4. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/BulkAddProductsAsyncTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
5. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/EditProductTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
6. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/GetProductDetailsByIdTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
7. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/BulkAddProductsTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
8. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/ToggleReturnProductTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
9. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/GetProductsInBatchesTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
10. Update `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/AddProductTest.java` using the violations listed above. Apply line-level fixes, rename methods, add missing sections/comments, remove inline mocks, and update test count declaration.
11. Create `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/GetProductInBatchesTest.java` for service method `getProductInBatches` (line 293 in `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/main/java/com/example/SpringApi/Services/ProductService.java`). Include class header, `// Total Tests`, Success/Failure/Permission sections, and a controller permission test.
12. Create `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/GetProductStockAtLocationsByProductIdTest.java` for service method `getProductStockAtLocationsByProductId` (line 1165 in `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/main/java/com/example/SpringApi/Services/ProductService.java`). Include class header, `// Total Tests`, Success/Failure/Permission sections, and a controller permission test.
13. Resolve extra test file `GetProductsInBatchesTest.java` by renaming it to match a public method or removing it.
14. Fix base test `/Users/nahushraichura/Documents/Personal Development Repositories/Spring-Api/SpringApi/src/test/java/com/example/SpringApi/Services/Tests/Product/ProductServiceTestBase.java` violations noted above.

Verification Commands (run after fixes):
- mvn -Dtest=GetCategoryPathsByIdsTest test
- mvn -Dtest=ToggleDeleteProductTest test
- mvn -Dtest=FindCategoriesByParentIdTest test
- mvn -Dtest=BulkAddProductsAsyncTest test
- mvn -Dtest=EditProductTest test
- mvn -Dtest=GetProductDetailsByIdTest test