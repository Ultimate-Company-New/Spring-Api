# Quick Reference: Writing Comprehensive Unit Tests

## Checklist for New Test Methods

### 1. **Test Naming**
- [ ] Use pattern: `methodName_Scenario_ExpectedOutcome`
- [ ] Name clearly describes what is being tested
- [ ] Use `@DisplayName` for human-readable description

### 2. **Test Structure (AAA Pattern)**
```java
@Test
@DisplayName("Create Payment - Valid request - Success")
void methodName_ValidScenario_Success() {
    // ARRANGE: Set up test data and mocks
    PaymentRequestModel request = createValidPaymentRequest(id, amount);
    when(repository.save(any())).thenReturn(testEntity);
    
    // ACT: Call the method being tested
    assertDoesNotThrow(() -> service.createPayment(request));
    
    // ASSERT: Verify results and mock interactions
    verify(repository, times(1)).save(any());
}
```

### 3. **For Each Public Method, Test:**

#### Success Cases
```java
@Test
void methodName_ValidInput_Success() {
    // Valid input → method succeeds
}
```

#### Failure Cases
```java
@Test
void methodName_InvalidScenario_ThrowsException() {
    // Invalid input → throws appropriate exception
    assertThrows(ExceptionType.class, () -> service.method(invalidInput));
}
```

#### Null Cases
```java
@Test
void methodName_NullRequest_ThrowsBadRequestException() {
    assertThrowsBadRequest(expectedMessage, () -> service.method(null));
}

@Test
void methodName_NullField_ThrowsBadRequestException() {
    request.setField(null);
    assertThrowsBadRequest(expectedMessage, () -> service.method(request));
}
```

#### Edge Cases
```java
@Test
void methodName_ZeroValue_ThrowsException() { }

@Test
void methodName_NegativeValue_ThrowsException() { }

@Test
void methodName_VeryLargeValue_Success() { }

@Test
void methodName_MinBoundary_Success() { }

@Test
void methodName_MaxBoundary_Success() { }
```

### 4. **Organize With @Nested**
```java
@Nested
@DisplayName("methodName Tests")
class MethodNameTests {
    @Test
    void methodName_Scenario1_Result() { }
    
    @Test
    void methodName_Scenario2_Result() { }
}
```

### 5. **Use BaseTest Factory Methods**
```java
// Instead of creating entities manually:
Payment payment = new Payment();
payment.setPaymentId(1L);
payment.setAmount(new BigDecimal("100.00"));
// ... 10 more lines

// Use factory method:
Payment payment = createTestPayment(1L, new BigDecimal("100.00"));
```

### 6. **Mock Setup Pattern**
```java
@Mock
private Repository repository;

@InjectMocks
private Service service;

@BeforeEach
void setUp() {
    // Use lenient() for optional mocks
    lenient().when(request.getHeader("Authorization")).thenReturn("Bearer token");
    
    // Mock BaseService methods if needed
    lenient().doReturn(DEFAULT_USER_ID).when(service).getUserId();
    lenient().doReturn(DEFAULT_LOGIN_NAME).when(service).getUser();
}
```

### 7. **Assertion Patterns**

**For Success Cases:**
```java
// Verify the result
assertNotNull(result);
assertEquals(expected, actual);
assertTrue(condition);
assertFalse(condition);

// Verify no exceptions were thrown
assertDoesNotThrow(() -> service.method());

// Verify mocks were called
verify(repository, times(1)).save(any());
verify(repository, never()).delete(any());
```

**For Exception Cases:**
```java
// Using helper methods
assertThrowsBadRequest(ErrorMessages.Payment.InvalidAmount, 
    () -> service.createPayment(invalidRequest));

assertThrowsNotFound(ErrorMessages.Payment.NotFound,
    () -> service.getPaymentById(id));

// Using standard assert
BadRequestException ex = assertThrows(BadRequestException.class, 
    () -> service.method(null));
assertEquals(expectedMessage, ex.getMessage());
```

### 8. **Coverage Checklist**

For each public method, ensure tests for:
- [ ] **Success**: Valid input → success
- [ ] **Null Request**: Null request → exception
- [ ] **Null Fields**: Individual null fields → exceptions
- [ ] **Empty Strings**: Empty fields → exceptions if required
- [ ] **Whitespace Only**: "   " in string fields → exceptions if required
- [ ] **Zero Values**: 0 amount, 0 quantity → appropriate handling
- [ ] **Negative Values**: -1, -100 → exceptions
- [ ] **Max Boundary**: Long.MAX_VALUE → appropriate handling
- [ ] **Min Boundary**: Long.MIN_VALUE → appropriate handling
- [ ] **Very Large**: 999999999.99 → success or appropriate handling
- [ ] **Decimal Precision**: 0.001, 100.005 → success
- [ ] **Deleted Entity**: Entity with isDeleted=true → appropriate handling
- [ ] **Not Found**: Entity doesn't exist → NotFoundException
- [ ] **Status Transitions**: State changes → success
- [ ] **Multiple Items**: Lists, arrays → success with all items
- [ ] **Empty Lists**: Empty collections → empty or exception
- [ ] **Large Dataset**: 100+ items → success

---

## Example: Complete Test Class Template

```java
package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.*;
import com.example.SpringApi.Services.*;
import com.example.SpringApi.Repositories.*;
import com.example.SpringApi.Exceptions.*;
import com.example.SpringApi.ErrorMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceName Unit Tests")
class ServiceNameTest extends BaseTest {

    @Mock
    private Repository repository;

    @Mock
    private UserLogService userLogService;

    @InjectMocks
    private ServiceName service;

    private Entity testEntity;
    private EntityRequestModel testRequest;

    @BeforeEach
    void setUp() {
        testEntity = createTestEntity();
        testRequest = createValidEntityRequest();
        
        lenient().doReturn(DEFAULT_USER_ID).when(service).getUserId();
        lenient().doReturn(DEFAULT_LOGIN_NAME).when(service).getUser();
        lenient().doReturn(DEFAULT_CLIENT_ID).when(service).getClientId();
    }

    @Nested
    @DisplayName("methodName Tests")
    class MethodNameTests {

        @Test
        @DisplayName("Method - Valid request - Success")
        void method_ValidRequest_Success() {
            // Arrange
            when(repository.save(any(Entity.class))).thenReturn(testEntity);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            // Act
            assertDoesNotThrow(() -> service.method(testRequest));

            // Assert
            verify(repository, times(1)).save(any(Entity.class));
            verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("Method - Null request - Throws BadRequestException")
        void method_NullRequest_ThrowsBadRequestException() {
            assertThrowsBadRequest(ErrorMessages.Entity.InvalidRequest,
                () -> service.method(null));
        }

        @Test
        @DisplayName("Method - Invalid field - Throws BadRequestException")
        void method_InvalidField_ThrowsBadRequestException() {
            testRequest.setField(invalidValue);
            assertThrowsBadRequest(ErrorMessages.Entity.InvalidField,
                () -> service.method(testRequest));
        }

        @Test
        @DisplayName("Method - Entity not found - Throws NotFoundException")
        void method_EntityNotFound_ThrowsNotFoundException() {
            when(repository.findById(DEFAULT_ID)).thenReturn(Optional.empty());
            assertThrowsNotFound(ErrorMessages.Entity.NotFound,
                () -> service.method(DEFAULT_ID));
        }

        @Test
        @DisplayName("Method - Negative ID - Throws NotFoundException")
        void method_NegativeId_ThrowsNotFoundException() {
            when(repository.findById(-1L)).thenReturn(Optional.empty());
            assertThrowsNotFound(ErrorMessages.Entity.NotFound,
                () -> service.method(-1L));
        }

        @Test
        @DisplayName("Method - Zero ID - Throws NotFoundException")
        void method_ZeroId_ThrowsNotFoundException() {
            when(repository.findById(0L)).thenReturn(Optional.empty());
            assertThrowsNotFound(ErrorMessages.Entity.NotFound,
                () -> service.method(0L));
        }

        @Test
        @DisplayName("Method - Deleted entity - Appropriate handling")
        void method_DeletedEntity_AppropriateBehavior() {
            Entity deletedEntity = createDeletedTestEntity();
            when(repository.findById(DEFAULT_ID)).thenReturn(Optional.of(deletedEntity));

            // Behavior depends on method - should either throw or return deleted entity
            // Add assertions accordingly
        }

        @Test
        @DisplayName("Method - Multiple items - Success")
        void method_MultipleItems_Success() {
            List<Entity> items = Arrays.asList(
                createTestEntity(1L),
                createTestEntity(2L),
                createTestEntity(3L)
            );
            when(repository.findAll()).thenReturn(items);

            List<Entity> result = service.getAll();

            assertNotNull(result);
            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("Method - Empty collection - Success returns empty")
        void method_EmptyCollection_SuccessReturnsEmpty() {
            when(repository.findAll()).thenReturn(new ArrayList<>());

            List<Entity> result = service.getAll();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }
}
```

---

## Common Patterns

### Testing Collections
```java
@Test
void method_SingleItem_Success() {
    List<Entity> items = Collections.singletonList(testEntity);
    when(repository.findAll()).thenReturn(items);
    
    List<Entity> result = service.getAll();
    
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(testEntity.getId(), result.get(0).getId());
}

@Test
void method_MultipleItems_Success() {
    List<Entity> items = Arrays.asList(testEntity1, testEntity2, testEntity3);
    when(repository.findAll()).thenReturn(items);
    
    List<Entity> result = service.getAll();
    
    assertNotNull(result);
    assertEquals(3, result.size());
}

@Test
void method_EmptyCollection_ReturnsEmpty() {
    when(repository.findAll()).thenReturn(new ArrayList<>());
    
    List<Entity> result = service.getAll();
    
    assertNotNull(result);
    assertTrue(result.isEmpty());
}
```

### Testing Boolean Flags
```java
@Test
void method_FlagTrue_Success() {
    entity.setFlag(true);
    // Test with flag true
}

@Test
void method_FlagFalse_Success() {
    entity.setFlag(false);
    // Test with flag false
}
```

### Testing Status Transitions
```java
@Test
void method_StatusTransition_Success() {
    entity.setStatus("OLD_STATUS");
    // Perform transition
    entity.setStatus("NEW_STATUS");
    
    assertEquals("NEW_STATUS", entity.getStatus());
}
```

### Testing Large Values
```java
@Test
void method_VeryLargeValue_Success() {
    request.setAmount(new BigDecimal("999999999.99"));
    // Test succeeds with large value
}

@Test
void method_MaxLongValue_Appropriate() {
    when(repository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());
    // Test appropriate behavior
}
```

---

## Running Tests

```bash
# All tests
mvn test

# Specific class
mvn test -Dtest=PaymentServiceTest

# Specific method
mvn test -Dtest=PaymentServiceTest#getPaymentById_ValidPaymentExists_Success

# With coverage
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

---

## Tips for Writing Better Tests

1. **Keep tests focused**: One test, one scenario
2. **Use meaningful names**: Test name should describe the scenario
3. **Avoid test dependencies**: Tests should be independent
4. **Use factory methods**: Reduce boilerplate with BaseTest methods
5. **Mock external dependencies**: Only test the service, not dependencies
6. **Assert specifically**: Use exact equality, not loose matching
7. **Verify interactions**: Confirm mocks were called correctly
8. **Test edge cases**: Don't just test the happy path
9. **Keep tests fast**: Avoid heavy operations, use mocks
10. **Document complex tests**: Add comments explaining the scenario

---

## When to Use Each Factory Method

| Factory Method | Use Case |
|---|---|
| `createTestUser()` | Basic user creation |
| `createTestUser(id, login, email)` | User with specific values |
| `createDeletedTestUser()` | Testing deleted user scenarios |
| `createTestPayment(id, amount)` | Payment with specific amount |
| `createValidPaymentRequest()` | Testing payment requests |
| `createTestPromo()` | Promo entity testing |
| `createValidPromoRequest()` | Promo request testing |
| `createTestProduct()` | Product entity testing |
| `createDeletedTestProduct()` | Deleted product scenarios |

All factory methods are available in `BaseTest` and ready to use!

