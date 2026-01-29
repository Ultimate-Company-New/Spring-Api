# Comprehensive Test Coverage Guide for All Services

This document provides a complete guide for comprehensive test coverage of all service classes in the SpringAPI project.

## Overview

All test classes follow these patterns:
- **Naming Convention**: `methodName_Scenario_ExpectedOutcome`
- **Pattern**: AAA (Arrange-Act-Assert)
- **Coverage**: Success, Failure, Validation, Null, Edge Cases

---

## Test Coverage Checklist by Service

### 1. AddressService ✓ COMPREHENSIVE
**Status**: Fully comprehensive (1746 lines)

**Public Methods Covered**:
- ✓ `toggleAddress()` - 8 test cases
- ✓ `getAddressById()` - 8 test cases
- ✓ `insertAddress()` - 50+ test cases (including postal code, address type validation)
- ✓ `updateAddress()` - 20+ test cases
- ✓ `getAddressByUserId()` - 10+ test cases
- ✓ `getAddressByClientId()` - 15+ test cases

**Coverage Areas**:
- Success cases with valid data
- Null request handling
- Invalid field validations (empty strings, whitespace, null values)
- Edge cases (negative IDs, zero IDs, boundary values)
- Deleted entity handling
- Large datasets
- All address types (HOME, WORK, BILLING, SHIPPING, OFFICE, WAREHOUSE)
- Postal code format validation (5-6 digits)
- Multiple toggles and state transitions

---

### 2. ClientService ✓ COMPREHENSIVE
**Status**: Fully comprehensive (1239 lines)

**Public Methods Covered**:
- ✓ `toggleClient()` - Multiple scenarios
- ✓ `getClientById()` - Success and failure cases
- ✓ `createClient()` - Logo upload with Firebase/ImgBB, duplicate name handling
- ✓ `updateClient()` - Logo update/delete scenarios

**Coverage Areas**:
- Firebase image upload integration tests
- ImgBB image upload/delete integration tests
- Duplicate client name validation
- Logo URL and delete hash management
- Google credential validation
- Configuration error handling (missing API keys)

---

### 3. UserService ⚠️ NEEDS ENHANCEMENT
**Status**: Partial (needs comprehensive edge case coverage)

**Public Methods**:
- `toggleUser()` - Basic tests exist
- `getUserById()` - Basic tests exist
- `getUserByEmail()` - Basic tests exist
- `createUser()` - Basic tests exist
- `updateUser()` - Basic tests exist
- `fetchUsersInCarrierInBatches()` - Pagination tests exist
- `confirmEmail()` - Email confirmation tests exist
- `getAllPermissions()` - Permission retrieval tests exist
- `bulkCreateUsersAsync()` - Bulk operation tests exist

**Missing Coverage**:
- Email validation edge cases (special characters, case sensitivity)
- Password validation rules
- User role/permission edge cases
- Deleted user state transitions
- Batch processing with mixed success/failure
- Permission mapping validation
- User group membership validation

**Recommended Test Cases**:
- Email with special characters, Unicode
- Null vs empty password
- Case-insensitive email lookups
- Concurrent bulk create operations
- Permission conflict scenarios
- User deletion cascading

---

### 4. ProductService ⚠️ NEEDS ENHANCEMENT
**Status**: Partial (1256 lines main service, tests need expansion)

**Public Methods**:
- `toggleProduct()` - Basic tests exist
- `getProductById()` - Basic tests exist
- `createProduct()` - Image handling tests exist
- `updateProduct()` - Partial coverage
- `getProductsByPickupLocation()` - Basic tests exist
- `getPublicProducts()` - Visibility tests exist
- `searchProducts()` - Search functionality tests exist
- `bulkCreateProductsAsync()` - Bulk operation tests exist

**Missing Coverage**:
- Category hierarchy validation
- Stock quantity boundary conditions (0, negative, very large numbers)
- Price validation (negative, zero, decimal precision)
- Product visibility transitions
- Image URL null/empty handling
- Bulk create partial success scenarios
- Search filter edge cases (special characters, wildcards)
- Pickup location mapping validation
- Product status transitions
- Concurrent modifications

**Recommended Test Cases**:
- Stock quantity: 0, -1, Long.MAX_VALUE
- Price: 0.00, negative, high decimal precision (0.001)
- Empty description handling
- Large title/description text
- Special characters in product names
- Image upload failures
- Bulk create with 50% failure rate
- Search with null/empty query
- Filter with invalid column names
- Non-existent pickup location assignment

---

### 5. PaymentService ⚠️ NEEDS ENHANCEMENT
**Status**: Partial (623 lines test file exists)

**Public Methods**:
- `createOrder()` - Razorpay order creation
- `createOrderFollowUp()` - Follow-up order handling
- `verifyPayment()` - Payment verification
- `recordCashPayment()` - Cash payment recording
- `verifyPaymentFollowUp()` - Payment verification follow-up
- `recordCashPaymentFollowUp()` - Cash payment follow-up
- `getPaymentsForPurchaseOrder()` - Payment retrieval
- `getPaymentById()` - Payment lookup
- `isPurchaseOrderPaid()` - Payment status check
- `initiateRefund()` - Refund initiation
- `getRazorpayKeyId()` - API key retrieval
- `generatePaymentReceiptPDF()` - PDF generation

**Missing Coverage**:
- Razorpay API error responses (network failures, invalid responses)
- Duplicate payment prevention
- Amount validation (precision, boundaries)
- Refund amount validation (partial vs full)
- Payment status state machine transitions
- Concurrent payment processing
- Invalid purchase order ID handling
- Null/empty signature verification
- PDF generation edge cases (missing orders, corrupted data)
- Razorpay key configuration validation
- Currency handling
- Partial refund scenarios
- Refund reason validation

**Recommended Test Cases**:
- Amount: 0, negative, very large, decimal precision issues
- Duplicate order ID attempts
- Invalid Razorpay signature
- Network timeout simulations
- Concurrent payment verifications for same PO
- Refund > payment amount
- Refund reasons: null, empty, special characters
- Payment status transitions: PENDING → VERIFIED → REFUNDED
- Missing GoogleCred for PDF generation
- Corrupted PDF template handling

---

### 6. PurchaseOrderService ⚠️ NEEDS ENHANCEMENT
**Status**: Partial (1566 lines service, comprehensive tests needed)

**Public Methods**:
- `createPurchaseOrder()` - PO creation with product mapping
- `updatePurchaseOrder()` - PO update
- `getPurchaseOrderById()` - PO retrieval
- `deletePurchaseOrder()` - PO deletion (soft delete)
- `togglePurchaseOrder()` - Soft delete toggle
- `getOrdersByLeadId()` - Lead-based filtering
- `calculateOrderTotals()` - Calculation validation
- `bulkCreatePurchaseOrdersAsync()` - Async bulk creation

**Missing Coverage**:
- Order total calculation precision (rounding errors)
- Tax calculation validation
- Shipping charge edge cases
- Product mapping validation (non-existent products)
- Lead/client relationship validation
- Address validation (billing vs shipping)
- Status transition validation (DRAFT → APPROVED → SHIPPED)
- Concurrent PO modifications
- Partial shipment tracking
- Order summary consistency
- Promo code application validation
- Duplicate order prevention

**Recommended Test Cases**:
- Subtotal calculation with many items
- Tax calculation: 0%, 5%, 18%, 28%
- Shipping: free, fixed amount, calculated
- Missing products in mapping
- Deleted client reference
- Invalid lead ID
- Status transitions: DRAFT → SUBMITTED → APPROVED → SHIPPED → DELIVERED
- Concurrent modifications to same PO
- Promo code value validation
- Discount stacking rules
- Order summary mismatch detection

---

### 7. ShipmentService ⚠️ NEEDS ENHANCEMENT
**Status**: Partial (231 lines service)

**Public Methods**:
- `createShipment()` - Shipment creation
- `updateShipment()` - Shipment update
- `getShipmentById()` - Shipment retrieval
- `toggleShipment()` - Soft delete toggle
- `getShipmentsByOrderId()` - Order-based filtering
- `trackShipment()` - Shipment tracking
- `updateTrackingInfo()` - Tracking information update

**Missing Coverage**:
- Carrier integration validation
- Tracking number format validation
- Weight calculation validation
- Dimension validation (height, width, depth)
- Package mapping validation
- Delivery date calculation
- Status transitions (PENDING → IN_TRANSIT → DELIVERED)
- Concurrent shipment updates
- Invalid order reference
- Carrier API integration errors
- Duplicate tracking numbers

**Recommended Test Cases**:
- Tracking number: null, empty, duplicate
- Weight: 0, negative, very large
- Dimensions: 0, negative, exceeds limits
- Status: PENDING, IN_TRANSIT, DELIVERED, RETURNED
- Delivery date: past date, future date, null
- Missing PO reference
- Carrier: valid, invalid, unsupported
- Multiple shipments for single order
- Concurrent tracking updates
- Carrier API timeout simulation

---

### 8. TodoService ✓ MOSTLY COMPREHENSIVE
**Status**: Good coverage (903 lines, but need edge cases)

**Public Methods Covered**:
- ✓ `addTodo()` - Creation with validation
- ✓ `updateTodo()` - Update with state management
- ✓ `completeTodo()` - Completion flag handling
- ✓ `deleteTodo()` - Soft delete
- ✓ `getTodoById()` - Retrieval
- ✓ `getTodosByUserId()` - User filtering
- ✓ `getTodosByClientId()` - Client filtering

**Needs Enhancement**:
- Task text validation (very long strings, special characters)
- Completed status transitions (true ↔ false)
- Date-based filtering edge cases
- Large dataset handling (100+ todos)
- Concurrent modifications
- Null optional fields

**Recommended Additional Tests**:
- Task: null, empty, very long (10000+ chars)
- Task with Unicode/special characters
- Multiple status transitions
- Large dataset: 1000+ todos
- Concurrent updates to same todo
- Deleted todo restoration

---

### 9. PromoService ✓ COMPREHENSIVE
**Status**: Fully comprehensive (1330 lines)

**Public Methods Covered**:
- ✓ `getPromosInBatches()` - Pagination with filtering
- ✓ `createPromo()` - Single promo creation
- ✓ `getPromoDetailsById()` - ID-based retrieval
- ✓ `togglePromo()` - Soft delete toggle
- ✓ `getPromoDetailsByName()` - Code-based retrieval
- ✓ `bulkCreatePromosAsync()` - Async bulk operations
- ✓ `bulkCreatePromos()` - Sync bulk operations

**Coverage Areas**:
- Overlapping promo code detection
- Case-insensitive code lookup
- Pagination with complex filters
- Bulk operation partial success/failure
- Async operation notifications
- Date range validation
- Discount value validation
- Promo code format validation

---

### 10. PickupLocationService ✓ GOOD
**Status**: Partial (should review and enhance)

**Coverage Needed**:
- Location address validation
- Capacity constraints
- Service area mapping
- Hours of operation validation
- Concurrent location updates

---

### 11. PackageService ✓ GOOD
**Status**: Partial (should review and enhance)

**Coverage Needed**:
- Package dimension validation
- Weight constraints
- Pricing calculation
- Availability validation
- Package type classification

---

### 12. LeadService ⚠️ NEEDS ENHANCEMENT
**Status**: Partial

**Coverage Needed**:
- Lead status transitions
- Assignment validation
- Contact information validation
- Conversion tracking
- Lead aging scenarios
- Duplicate detection

---

### 13. MessageService ⚠️ NEEDS ENHANCEMENT
**Status**: Partial

**Coverage Needed**:
- Message type validation
- Recipient validation
- Template rendering
- Bulk messaging
- Message delivery tracking
- Read/unread state management

---

### 14. UserGroupService ✓ GOOD
**Status**: Partial (should review permissions)

**Coverage Needed**:
- Group membership validation
- Permission inheritance
- Recursive group assignment
- Empty group handling
- Group deletion cascading

---

### 15. UserLogService ✓ GOOD
**Status**: Partial (audit trail specific)

**Coverage Needed**:
- Concurrent log writes
- Log retention policies
- Query performance with large datasets
- Log corruption detection

---

### 16. LoginService ⚠️ NEEDS ENHANCEMENT
**Status**: Partial (security critical)

**Coverage Needed**:
- JWT token generation and validation
- Session management
- OAuth integration
- Token expiry handling
- Multi-factor authentication
- Account lockout on failed attempts
- Password reset flows
- Session timeout scenarios

---

### 17. ProductReviewService ⚠️ NEEDS ENHANCEMENT
**Status**: Partial

**Coverage Needed**:
- Rating validation (1-5 or 1-10)
- Review text validation
- Duplicate review prevention
- Review moderation
- Helpful/unhelpful voting
- Review deletion/moderation

---

### 18. ShippingService ⚠️ NEEDS ENHANCEMENT
**Status**: Partial

**Coverage Needed**:
- Carrier integration
- Rate calculation
- Delivery time estimation
- Service level selection
- Address validation
- Weight/dimension validation
- Insurance calculation

---

### 19. ShipmentProcessingService ⚠️ NEEDS ENHANCEMENT
**Status**: Partial

**Coverage Needed**:
- Processing state machine
- Error recovery
- Retry logic
- Notification triggering
- Webhook handling
- Transaction management

---

## Test Enhancement Priorities

### Phase 1 (Critical) - Complete First
1. **PaymentService** - Financial data, critical business logic
2. **PurchaseOrderService** - Order management, revenue impact
3. **UserService** - Authentication/authorization
4. **LoginService** - Security critical
5. **ShipmentService** - Order fulfillment

### Phase 2 (High) - Complete After Phase 1
1. **ProductService** - Large service, many methods
2. **LeadService** - Sales pipeline
3. **MessageService** - Communication system
4. **ShippingService** - Logistics
5. **ShipmentProcessingService** - Background processing

### Phase 3 (Medium) - Complete When Time Permits
1. **ProductReviewService** - User engagement
2. **UserGroupService** - Permission management
3. **PackageService** - Product packaging
4. **PickupLocationService** - Logistics network
5. **UserLogService** - Audit trails

---

## BaseTest Factory Methods Summary

All services should use BaseTest factory methods for consistent test data:

### Currently Available
- `createTestUser()` / `createTestUser(id, login, email)`
- `createDeletedTestUser()`
- `createTestClient()` / `createTestClient(id, name)`
- `createDeletedTestClient()`
- `createValidAddressRequest()` / `createValidAddressRequest(id, userId, clientId)`
- `createTestAddress()` / `createTestAddress(id, type)`
- `createDeletedTestAddress()`
- `createTestPayment()` / `createTestPayment(id, amount)`
- `createDeletedTestPayment()`
- `createTestPromo()` / `createTestPromo(id)`
- `createDeletedTestPromo()`
- `createTestProduct()` / `createTestProduct(id)`
- `createDeletedTestProduct()`
- `createTestTodo()` / `createTestTodo(id)`
- `createDeletedTestTodo()`
- `createTestPickupLocation()` / `createTestPickupLocation(id)`
- `createDeletedTestPickupLocation()`
- `createTestPackage()` / `createTestPackage(id)`
- `createDeletedTestPackage()`
- `createTestProductReview()` / `createTestProductReview(id)`
- `createDeletedTestProductReview()`
- `createTestUserGroup()` / `createTestUserGroup(id)`
- `createDeletedTestUserGroup()`
- `createTestShipment()` / `createTestShipment(id)`
- `createDeletedTestShipment()`

### Assertion Helpers
- `assertThrowsBadRequest(expectedMessage, executable)`
- `assertThrowsNotFound(expectedMessage, executable)`

---

## Example Test Pattern

```java
@Nested
@DisplayName("createPayment Tests")
class CreatePaymentTests {

    @Test
    @DisplayName("Create Payment - Valid request - Success")
    void createPayment_ValidRequest_Success() {
        // Arrange
        PaymentRequestModel validRequest = createValidPaymentRequest(DEFAULT_PAYMENT_ID, DEFAULT_AMOUNT);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> paymentService.createPayment(validRequest));

        // Assert
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(userLogService, times(1)).logData(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("Create Payment - Null request - ThrowsBadRequestException")
    void createPayment_NullRequest_ThrowsBadRequestException() {
        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.InvalidRequest, 
            () -> paymentService.createPayment(null));
    }

    @Test
    @DisplayName("Create Payment - Negative amount - ThrowsBadRequestException")
    void createPayment_NegativeAmount_ThrowsBadRequestException() {
        // Arrange
        PaymentRequestModel request = createValidPaymentRequest(DEFAULT_PAYMENT_ID, new BigDecimal("-100.00"));

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.InvalidAmount, 
            () -> paymentService.createPayment(request));
    }

    @Test
    @DisplayName("Create Payment - Zero amount - ThrowsBadRequestException")
    void createPayment_ZeroAmount_ThrowsBadRequestException() {
        // Arrange
        PaymentRequestModel request = createValidPaymentRequest(DEFAULT_PAYMENT_ID, BigDecimal.ZERO);

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.PaymentErrorMessages.InvalidAmount, 
            () -> paymentService.createPayment(request));
    }

    @Test
    @DisplayName("Create Payment - Very large amount - Success")
    void createPayment_VeryLargeAmount_Success() {
        // Arrange
        BigDecimal largeAmount = new BigDecimal("999999999.99");
        PaymentRequestModel request = createValidPaymentRequest(DEFAULT_PAYMENT_ID, largeAmount);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> paymentService.createPayment(request));
    }

    @Test
    @DisplayName("Create Payment - Decimal precision (.001) - Success")
    void createPayment_DecimalPrecision_Success() {
        // Arrange
        BigDecimal preciseAmount = new BigDecimal("100.001");
        PaymentRequestModel request = createValidPaymentRequest(DEFAULT_PAYMENT_ID, preciseAmount);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> paymentService.createPayment(request));
    }
}
```

---

## Coverage Metrics Target

For each service:
- **Minimum 80% code coverage** via test execution
- **100% method coverage** - every public method has ≥1 test
- **Branch coverage** - all if/else paths tested
- **Edge cases** - null, empty, boundary values, very large values
- **Error paths** - all exception scenarios
- **Integration points** - repository calls, service dependencies

---

## Notes

- Tests should use `@Nested` for organizing test classes by method
- Use `@DisplayName` for clear test descriptions
- Use `@ParameterizedTest` with `@ValueSource` for testing multiple similar cases
- Mock all external dependencies (repositories, services, HTTP requests)
- Use `lenient()` for optional mock behaviors
- Verify mock interactions with `verify()` statements
- Tests should be independent and not rely on execution order
- Use `assertDoesNotThrow()` for methods that should not throw exceptions
- Use specific assertion methods (`assertEquals()`, `assertTrue()`, `assertNull()`, etc.)

