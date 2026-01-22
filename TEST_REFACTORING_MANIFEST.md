# Test Structure Refactoring Manifest

## Overview
This document specifies the exact refactoring requirements for all 19 test files to achieve uniform test structure compliance.

## Required Structure for All Test Files

### 1. **Class-Level Organization**
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceName Unit Tests")
class ServiceNameTest extends BaseTest {  // ← MUST extend BaseTest
    // Mock declarations
    // Field declarations
    // @BeforeEach setUp()
    // Nested test classes
}
```

### 2. **Nested Class Organization**
Each public method in the service should have its own `@Nested` class:
```java
@Nested
@DisplayName("MethodName Tests")
class MethodNameTests {
    @Test
    @DisplayName("Method_Scenario_Result")
    void methodName_scenario_Result() { ... }
}
```

### 3. **Test Naming Convention**
- **Format**: `<MethodName>_<Scenario>_<ExpectedResult>`
- **Examples**:
  - `getUserById_NegativeId_ThrowsNotFoundException`
  - `createUser_ValidRequest_Success`
  - `updateUser_UserNotFound_ThrowsNotFoundException`

### 4. **Exception Assertion Pattern**
```java
ExceptionType ex = assertThrows(ExceptionType.class, 
    () -> service.methodName(...));
assertEquals(ErrorMessages.ServiceName.ErrorCode, ex.getMessage());
```

### 5. **Batch Endpoint Tests**
Single comprehensive triple-loop test covering:
- All valid column names grouped by type (string, number, boolean, date)
- All valid operators for each column type
- All valid value combinations
- All invalid combinations (to verify proper rejection)

Reference implementation: `LeadServiceTest.getLeadsInBatches_TripleLoopValidation()`

### 6. **Test Data Management**
- ALL test data creation must be in BaseTest.java factory methods
- NO inline `new Model()` instantiations in test files
- Use: `createTestUser()`, `createValidPaymentRequest()`, etc.
- Pattern: `createTest<Entity>()` and `createValid<Request>Request()`

---

## File-by-File Refactoring Plan

### HIGH PRIORITY (Critical - Start Here)

#### 1. LoginServiceTest (1032 lines)
**Current Status**: No @Nested classes, no BaseTest extension
**Required Changes**:
- [ ] Add `extends BaseTest`
- [ ] Create @Nested classes for:
  - `ConfirmEmailTests`
  - `SignInTests`
  - `SignUpTests`
  - `PasswordResetTests`
  - `GetUsersInBatchesTests` (with triple-loop test)
- [ ] Move test data creation to BaseTest factory methods
- [ ] Verify all exception tests assert error messages
- [ ] Add @DisplayName to all test methods

#### 2. ShipmentProcessingServiceTest (377 lines)
**Current Status**: Limited nested classes, doesn't extend BaseTest
**Required Changes**:
- [ ] Add `extends BaseTest`
- [ ] Create @Nested classes for:
  - `ProcessShipmentsCashTests`
  - `ProcessShipmentsOnlineTests`
  - `CreateShipmentsTests`
  - `UpdateShipmentStatusTests`
- [ ] Consolidate test data in setUp() using BaseTest factories
- [ ] Ensure all exception tests assert error messages
- [ ] Add missing @DisplayName annotations

#### 3. ShipmentServiceTest (369 lines)
**Current Status**: Has @Nested but incomplete, doesn't extend BaseTest
**Required Changes**:
- [ ] Add `extends BaseTest`
- [ ] Enhance @Nested structure with:
  - `GetShipmentByIdTests`
  - `GetShipmentsInBatchesTests` (with triple-loop for filtering)
  - `CreateShipmentTests`
  - `UpdateShipmentTests`
  - `ToggleShipmentTests`
- [ ] Implement batch endpoint triple-loop test
- [ ] Move all test data to BaseTest factories
- [ ] Verify exception message assertions

#### 4. UserServiceTest (1538 lines)
**Current Status**: Large file, some @Nested, needs BaseTest integration
**Required Changes**:
- [ ] Already extends BaseTest (good!)
- [ ] Reorganize into comprehensive @Nested classes:
  - `GetUserByIdTests`
  - `CreateUserTests`
  - `UpdateUserTests`
  - `ToggleUserTests`
  - `GetUserByEmailTests`
  - `GetUsersInBatchesTests` (with triple-loop test)
  - `ManagePermissionsTests`
  - `ManageAddressesTests`
  - `UpdateProfilePictureTests`
- [ ] Implement batch endpoint triple-loop test
- [ ] Ensure ALL exception tests assert error messages
- [ ] Verify test data uses BaseTest factories

#### 5. PurchaseOrderServiceTest (1360 lines)
**Current Status**: Minimal @Nested (only ValidationTests), large setup
**Required Changes**:
- [ ] Doesn't extend BaseTest - ADD `extends BaseTest`
- [ ] Create @Nested classes for each method:
  - `GetPurchaseOrderByIdTests`
  - `CreatePurchaseOrderTests`
  - `UpdatePurchaseOrderTests`
  - `TogglePurchaseOrderTests`
  - `GetPurchaseOrdersInBatchesTests` (with triple-loop test)
  - `ApprovePurchaseOrderTests`
  - `RejectPurchaseOrderTests`
  - `GeneratePdfTests`
  - `UploadAttachmentTests`
  - `ValidationTests` (already exists - reorganize)
- [ ] Consolidate setUp() - move all test data creation to BaseTest factories
- [ ] Implement comprehensive batch endpoint triple-loop test
- [ ] Verify all exception tests assert messages with ErrorMessages constants
- [ ] Add missing @DisplayName annotations

### MEDIUM PRIORITY (Significant Refactoring)

#### 6. ClientServiceTest (1266 lines)
- [ ] Add `extends BaseTest`
- [ ] Reorganize with @Nested classes
- [ ] Implement batch triple-loop test
- [ ] Move test data to BaseTest

#### 7. MessageServiceTest (604 lines)
- [ ] Add `extends BaseTest`
- [ ] Create @Nested classes
- [ ] Implement batch triple-loop test
- [ ] Move test data to BaseTest

#### 8. PaymentServiceTest (681 lines)
- [ ] Already has good @Nested structure (keep it)
- [ ] Add `extends BaseTest` (check if already done)
- [ ] Consolidate test data in BaseTest
- [ ] Verify all exception message assertions

#### 9. PickupLocationServiceTest (1075 lines)
- [ ] Better @Nested organization
- [ ] Test data to BaseTest
- [ ] Batch triple-loop test
- [ ] Add missing @DisplayName

#### 10. ProductReviewServiceTest (962 lines)
- [ ] Add `extends BaseTest`
- [ ] Create @Nested classes
- [ ] Batch operations tests needed
- [ ] Move test data to BaseTest

#### 11. PromoServiceTest (1329 lines)
- [ ] Enhance @Nested organization
- [ ] Batch triple-loop test (getPromosInBatches)
- [ ] Test data to BaseTest
- [ ] Verify exception assertions

#### 12. ShippingServiceTest (546 lines)
- [ ] Add/enhance @Nested classes
- [ ] Test data to BaseTest
- [ ] Batch endpoint triple-loop test
- [ ] Add missing @DisplayName

#### 13. TodoServiceTest (903 lines)
- [ ] Better @Nested organization
- [ ] Test data to BaseTest
- [ ] Add batch operations tests
- [ ] Verify naming conventions

#### 14. UserGroupServiceTest (818 lines)
- [ ] Test data consolidation in BaseTest
- [ ] Batch operations tests
- [ ] Better @Nested organization

#### 15. UserLogServiceTest (661 lines)
- [ ] Add `extends BaseTest`
- [ ] Create @Nested classes
- [ ] Batch triple-loop test (fetchUserLogsInBatches)
- [ ] Move test data to BaseTest

### LOW PRIORITY (Minor Adjustments)

#### 16. AddressServiceTest (1796 lines)
- Mostly compliant
- Minor @Nested reorganization
- Verify test data source
- Batch triple-loop test if applicable

#### 17. LeadServiceTest (1026 lines)
- ✅ REFERENCE IMPLEMENTATION
- Already has proper structure
- Use as template for others
- Batch triple-loop test is reference pattern

#### 18. PackageServiceTest (lines)
- Good coverage
- Minor tweaks to @Nested
- Verify batches endpoint test

#### 19. ProductServiceTest (1688 lines)
- Large file
- Enhance @Nested organization
- Batch triple-loop test
- Test data consolidation

---

## Implementation Order

### Phase 1 (Week 1): Foundation
1. ✅ Extend BaseTest.java with all factory methods
2. Refactor LoginServiceTest (smaller, establish pattern)
3. Refactor ShipmentProcessingServiceTest (quick win)
4. Refactor ShipmentServiceTest (establish batch pattern)

### Phase 2 (Week 2): Core Services
5. Refactor UserServiceTest (large, important)
6. Refactor PurchaseOrderServiceTest (complex, large)
7. Refactor ClientServiceTest

### Phase 3 (Week 3): Remaining Files
8-15. Refactor remaining MEDIUM priority files
16-19. Refactor LOW priority files

---

## Quality Checklist

For EACH refactored file verify:
- [ ] Class extends BaseTest
- [ ] All @Nested classes present with @DisplayName
- [ ] All public methods have corresponding test classes
- [ ] All test methods follow naming convention
- [ ] All exception tests assert both type AND message
- [ ] Error message assertions use ErrorMessages.<ServiceName>.<Code>
- [ ] All test data uses BaseTest factories
- [ ] No inline `new Model()` instantiations
- [ ] Batch/pagination endpoints have triple-loop test
- [ ] All @Test methods have @DisplayName
- [ ] @BeforeEach properly initializes test objects
- [ ] Test methods follow AAA pattern (Arrange-Act-Assert)

---

## Batch Endpoint Triple-Loop Pattern

Reference: `LeadServiceTest.getLeadsInBatches_TripleLoopValidation()`

```java
@Nested
@DisplayName("GetEntityInBatches Tests")
class GetEntityInBatchesTests {
    
    @Test
    @DisplayName("Get Entity In Batches - Success")
    void getEntityInBatches_Success() {
        // Standard success test
    }
    
    @Test
    @DisplayName("Get Entity In Batches - Triple Loop Filter Validation")
    void getEntityInBatches_TripleLoopValidation() {
        // 1. Define columns by type
        String[] stringColumns = { "col1", "col2", ... };
        String[] numberColumns = { "id", "count", ... };
        String[] booleanColumns = { "isDeleted", ... };
        String[] dateColumns = { "createdAt", ... };
        String[] invalidColumns = { "invalidCol", "DROP TABLE" };
        
        // 2. Define operators by type
        String[] stringOperators = { CONTAINS, EQUALS, ... };
        String[] numberOperators = { EQUAL, LESS_THAN, ... };
        String[] booleanOperators = { IS };
        String[] dateOperators = { IS_AFTER, IS_BEFORE, ... };
        String[] invalidOperators = { "INVALID" };
        
        // 3. Define test values
        String[] validValues = { "test", "123", "2024-01-01" };
        String[] emptyValues = { null, "" };
        
        // 4. Setup mocks for all column type detections
        
        // 5. Triple loop through combinations
        for (String column : allColumns) {
            for (String operator : allOperators) {
                for (String value : allValues) {
                    // Determine if combination should succeed
                    boolean shouldSucceed = validateCombination(column, operator, value);
                    
                    // Test and verify
                    if (shouldSucceed) {
                        assertDoesNotThrow(() -> service.getEntityInBatches(request));
                    } else {
                        assertThrows(BadRequestException.class, 
                            () -> service.getEntityInBatches(request));
                    }
                }
            }
        }
    }
}
```

---

## Notes

- **BaseTest.java** has been enhanced with factory methods for ALL services
- Use `createTest<Entity>()` methods for entity creation
- Use `createValid<Entity>Request()` methods for request models
- All constants are defined in BaseTest (DEFAULT_USER_ID, DEFAULT_EMAIL, etc.)
- Extend BaseTest in EVERY test class
- Exception message assertions MUST use ErrorMessages constants

