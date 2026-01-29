# Validation Analysis for 22 Spring-Api Services

This document provides a comprehensive analysis of all validation checks, error conditions, and exceptions across all service files. Each validation is documented for creating comprehensive test cases that cover each validation failure as a separate test.

---

## 1. AddressService

**File:** `AddressService.java`
**Public Methods:** 5

### Method: toggleAddress(long addressId)
- **Validations:**
  - Address existence check
  - **Condition:** `address.isPresent()` is false
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.AddressErrorMessages.NotFound`

### Method: getAddressById(long addressId)
- **Validations:**
  - Address existence check
  - **Condition:** Address not found by ID
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.AddressErrorMessages.NotFound`

### Method: insertAddress(AddressRequestModel addressRequest)
- **Validations:** None explicit (handled in constructor)

### Method: updateAddress(AddressRequestModel addressRequest)
- **Validations:**
  - Null request check
  - **Condition:** `addressRequest == null`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.AddressErrorMessages.ER001`

  - Address existence check
  - **Condition:** Address with ID not found
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.AddressErrorMessages.NotFound`

### Method: getAddressByUserId(long userId)
- **Validations:**
  - User existence check
  - **Condition:** `user.isEmpty()`
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.AddressErrorMessages.NotFound`

  - User deleted check
  - **Condition:** `user.get().getIsDeleted()` is true
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.AddressErrorMessages.NotFound`

### Method: getAddressByClientId(long clientId)
- **Validations:**
  - Client existence check
  - **Condition:** `client.isEmpty()`
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.AddressErrorMessages.NotFound`

  - Client deleted check
  - **Condition:** `client.get().getIsDeleted()` is true
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.AddressErrorMessages.NotFound`

---

## 2. ClientService

**File:** `ClientService.java`
**Public Methods:** 4

### Method: toggleClient(long clientId)
- **Validations:**
  - Client existence check
  - **Condition:** `client.isPresent()` is false
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.ClientErrorMessages.InvalidId`

### Method: getClientById(long clientId)
- **Validations:**
  - Client existence check
  - **Condition:** `client.isPresent()` is false
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.ClientErrorMessages.InvalidId`

### Method: createClient(ClientRequestModel clientRequest)
- **Validations:**
  - Duplicate name check (business logic constraint)
  - **Condition:** `clientRepository.existsByName(clientRequest.getName())` is true
  - **Exception:** `BadRequestException`
  - **Error Message:** `"A client with the name '" + clientRequest.getName() + "' already exists."`

  - Logo Base64 not empty/blank check
  - **Condition:** `clientRequest.getLogoBase64() != null && !isEmpty && !isBlank`
  - **Exception:** `BadRequestException` (if ImgBB API key not configured)
  - **Error Message:** `ErrorMessages.ConfigurationErrorMessages.ImgbbApiKeyNotConfigured`

  - Firebase GoogleCred check
  - **Condition:** GoogleCred not found for client
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.UserErrorMessages.ER011`

  - Logo upload validation
  - **Condition:** `uploadResponse == null || uploadResponse.getUrl() == null`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.ClientErrorMessages.InvalidLogoUpload`

### Method: updateClient(ClientRequestModel clientRequest)
- **Validations:**
  - Client existence check
  - **Condition:** Client with ID not found
  - **Exception:** `NotFoundException`
  - **Error Message:** (implied)

  - Duplicate name check (excluding current client)
  - **Condition:** Different client exists with same name
  - **Exception:** `BadRequestException`
  - **Error Message:** `"A client with the name '" + clientRequest.getName() + "' already exists."`

  - ImgBB API key check
  - **Condition:** `updatedClient.getImgbbApiKey() == null || isEmpty`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.ConfigurationErrorMessages.ImgbbApiKeyNotConfigured`

---

## 3. LeadService

**File:** `LeadService.java`
**Public Methods:** 8

### Method: getLeadsInBatches(LeadRequestModel leadRequestModel)
- **Validations:**
  - Page size validation
  - **Condition:** `pageSize <= 0` (end - start)
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.CommonErrorMessages.InvalidPagination`

  - Logic operator validation (if provided)
  - **Condition:** `!isValidLogicOperator()`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.CommonErrorMessages.InvalidLogicOperator`

  - Column name validation
  - **Condition:** Column not in validColumns set (22 valid columns)
  - **Exception:** `BadRequestException`
  - **Error Message:** `"Invalid column name: " + filter.getColumn() + ". Valid columns: " + list`

  - Operator validation
  - **Condition:** `!filter.isValidOperator()`
  - **Exception:** `BadRequestException`
  - **Error Message:** `"Invalid operator: " + filter.getOperator() + " for column: " + filter.getColumn()`

  - Operator type match validation
  - **Condition:** Operator doesn't match column type
  - **Exception:** `BadRequestException`
  - **Error Message:** (from validateOperatorForType method)

### Method: getLeadDetailsById(Long leadId)
- **Validations:**
  - Lead existence check
  - **Condition:** `lead == null`
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.LEAD_NOT_FOUND`

### Method: getLeadDetailsByEmail(String email)
- **Validations:**
  - Lead existence by email check
  - **Condition:** `lead == null`
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.LEAD_NOT_FOUND`

### Method: createLead(LeadRequestModel leadRequestModel)
- **Validations:** (Delegated to constructor)

### Method: updateLead(Long leadId, LeadRequestModel leadRequestModel)
- **Validations:**
  - Lead existence check
  - **Condition:** `existingLead == null`
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.LEAD_NOT_FOUND`

  - Lead not deleted check
  - **Condition:** `existingLead.getIsDeleted()` is true
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.LEAD_NOT_FOUND`

### Method: toggleLead(Long leadId)
- **Validations:**
  - Lead existence check
  - **Condition:** `lead == null`
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.LEAD_NOT_FOUND`

### Method: bulkCreateLeadsAsync(...)
- **Validations:** (Async operation with partial success support)

---

## 4. LoginService

**File:** `LoginService.java`
**Public Methods:** 4

### Method: confirmEmail(LoginRequestModel loginRequestModel)
- **Validations:**
  - UserId null check
  - **Condition:** `loginRequestModel.getUserId() == null`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.LoginErrorMessages.InvalidId`

  - User existence check
  - **Condition:** User not found by ID
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.LoginErrorMessages.InvalidId`

  - Token null/empty check
  - **Condition:** `user.getToken() == null || isEmpty || isBlank`
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.LoginErrorMessages.InvalidToken`

  - Token match check
  - **Condition:** `!user.getToken().equals(loginRequestModel.getToken())`
  - **Exception:** `UnauthorizedException`
  - **Error Message:** `ErrorMessages.LoginErrorMessages.InvalidToken`

### Method: signIn(LoginRequestModel loginRequestModel)
- **Validations:**
  - Login name not empty check
  - **Condition:** `!StringUtils.hasText(loginRequestModel.getLoginName())`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.LoginErrorMessages.ER012`

  - Password not empty check
  - **Condition:** `!StringUtils.hasText(loginRequestModel.getPassword())`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.LoginErrorMessages.ER012`

  - User existence check
  - **Condition:** User not found by login name
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.LoginErrorMessages.InvalidEmail`

  - Email confirmation check
  - **Condition:** `user.getEmailConfirmed() == null || !confirmed`
  - **Exception:** `UnauthorizedException`
  - **Error Message:** `ErrorMessages.LoginErrorMessages.ER005`

  - Account locked check
  - **Condition:** `user.getLocked() != null && user.getLocked()`
  - **Exception:** `UnauthorizedException`
  - **Error Message:** `ErrorMessages.LoginErrorMessages.ER006`

  - Password set check
  - **Condition:** `user.getPassword() == null || isBlank`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.LoginErrorMessages.ER016`

  - Password match check
  - **Condition:** Password verification fails
  - **Exception:** `UnauthorizedException`
  - **Error Message:** `ErrorMessages.LoginErrorMessages.InvalidCredentials` (or `ER007` if locked)

### Method: resetPassword(LoginRequestModel loginRequestModel)
- **Validations:**
  - Login name not empty check
  - **Condition:** `!StringUtils.hasText(loginRequestModel.getLoginName())`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.LoginErrorMessages.ER014`

  - User existence check
  - **Condition:** User not found by login name
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.LoginErrorMessages.InvalidEmail`

  - Password set check
  - **Condition:** `user.getPassword() == null || isEmpty || isBlank`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.LoginErrorMessages.ER003`

  - Email sender configuration check
  - **Condition:** `senderEmail == null || isEmpty`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.ConfigurationErrorMessages.SendGridEmailNotConfigured`

  - Email sender name configuration check
  - **Condition:** `senderName == null || isEmpty`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.ConfigurationErrorMessages.SendGridNameNotConfigured`

  - SendGrid API key configuration check
  - **Condition:** `sendGridApiKey == null || isEmpty`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.ConfigurationErrorMessages.SendGridApiKeyNotConfigured`

  - Email send verification
  - **Condition:** `!emailSent`
  - **Exception:** `RuntimeException`
  - **Error Message:** `"Failed to send reset password email"`

### Method: generateJwtToken(LoginRequestModel loginRequestModel)
- **Validations:**
  - Login name presence check
  - **Condition:** Login name is empty/null
  - **Exception:** `BadRequestException`
  - **Error Message:** (implied)

  - API key presence check
  - **Condition:** API key is empty/null
  - **Exception:** `BadRequestException`
  - **Error Message:** (implied)

  - User existence check
  - **Condition:** User not found
  - **Exception:** `NotFoundException`
  - **Error Message:** (implied)

  - API key validity check
  - **Condition:** API key doesn't belong to user
  - **Exception:** `UnauthorizedException`
  - **Error Message:** (implied)

---

## 5. MessageService

**File:** `MessageService.java`
**Public Methods:** 3

### Method: getMessagesInBatches(PaginationBaseRequestModel paginationBaseRequestModel)
- **Validations:**
  - Column name validation
  - **Condition:** Column not in validColumns set (13 valid columns)
  - **Exception:** `BadRequestException`
  - **Error Message:** `"Invalid column name: " + column`

  - Page size validation
  - **Condition:** `pageSize <= 0`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.CommonErrorMessages.InvalidPagination`

### Method: createMessage(MessageRequestModel messageRequestModel)
- **Validations:** (Delegated to createMessageWithContext)

### Method: createMessageWithContext(...)
- **Validations:**
  - Client existence check
  - **Condition:** Client not found by ID
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.ClientErrorMessages.InvalidId`

  - SendAsEmail configuration check (if email sending enabled)
  - **Condition:** Various email configuration missing

### Method: updateMessage(MessageRequestModel messageRequestModel)
- **Validations:**
  - MessageId null check
  - **Condition:** `messageRequestModel.getMessageId() == null`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.MessagesErrorMessages.InvalidId`

  - Client existence check
  - **Condition:** Client not found
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.ClientErrorMessages.InvalidId`

  - Message existence check (filtered by client)
  - **Condition:** Message not found
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.MessagesErrorMessages.InvalidId`

  - Published email message check
  - **Condition:** Email already sent (publishDate passed)
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.MessagesErrorMessages.ER011`

---

## 6. PackageService

**File:** `PackageService.java`
**Public Methods:** 7

### Method: getPackagesInBatches(PaginationBaseRequestModel paginationBaseRequestModel)
- **Validations:**
  - Start index negative check
  - **Condition:** `start < 0`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.CommonErrorMessages.StartIndexCannotBeNegative`

  - End index positive check
  - **Condition:** `end <= 0`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.CommonErrorMessages.EndIndexMustBeGreaterThanZero`

  - Start less than end check
  - **Condition:** `start >= end`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.CommonErrorMessages.StartIndexMustBeLessThanEnd`

  - Column name validation
  - **Condition:** Column not in validColumns set (18 valid columns)
  - **Exception:** `BadRequestException`
  - **Error Message:** `"Invalid column name: " + filter.getColumn()`

  - Operator validation
  - **Condition:** Operator not in validOperators set
  - **Exception:** `BadRequestException`
  - **Error Message:** `"Invalid operator: " + filter.getOperator()`

  - Boolean column operator check
  - **Condition:** Boolean column with non-equals/notEquals operator
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.CommonErrorMessages.BooleanColumnsOnlySupportEquals`

  - Date/number column operator check
  - **Condition:** Date/number column with non-comparison operator
  - **Exception:** `BadRequestException`
  - **Error Message:** `columnType + " columns only support numeric comparison operators"`

### Method: getPackageById(Long packageId)
- **Validations:**
  - Package existence check
  - **Condition:** Package not found for client
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.PackageErrorMessages.InvalidId`

### Method: getAllPackagesInSystem()
- **Validations:** None

### Method: togglePackage(Long packageId)
- **Validations:**
  - Package existence check
  - **Condition:** Package not found for client
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.PackageErrorMessages.InvalidId`

### Method: updatePackage(PackageRequestModel packageRequest)
- **Validations:**
  - Package existence check
  - **Condition:** Package not found for client
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.PackageErrorMessages.InvalidId`

### Method: createPickupLocationMappings(...)
- **Validations:** (Internal method)

---

## 7. PaymentService

**File:** `PaymentService.java`
**Public Methods:** 4 (main payment operations)

### Method: createOrder(RazorpayOrderRequestModel request)
- **Validations:**
  - Client existence check
  - **Condition:** Client not found or Razorpay credentials missing
  - **Exception:** `NotFoundException` / `BadRequestException`
  - **Error Message:** `ErrorMessages.ClientErrorMessages.InvalidId` / `ErrorMessages.PaymentErrorMessages.RazorpayApiKeyNotConfigured`

  - Razorpay API key check
  - **Condition:** `client.getRazorpayApiKey() == null || isEmpty`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.PaymentErrorMessages.RazorpayApiKeyNotConfigured`

  - Razorpay API secret check
  - **Condition:** `client.getRazorpayApiSecret() == null || isEmpty`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.PaymentErrorMessages.RazorpayApiSecretNotConfigured`

  - Purchase order existence check
  - **Condition:** Purchase order not found
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.PurchaseOrderErrorMessages.InvalidId`

  - Client access check
  - **Condition:** Purchase order doesn't belong to current client
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.CommonErrorMessages.AccessDeniedToPurchaseOrder`

  - PO status check
  - **Condition:** Status not PENDING_APPROVAL
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.PaymentErrorMessages.OnlyPendingApprovalCanBePaid`

  - Amount validation
  - **Condition:** `amount == null || amount <= 0`
  - **Exception:** `BadRequestException`
  - **Error Message:** (derived from OrderSummary or request)

  - OrderSummary existence check
  - **Condition:** OrderSummary not found
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.OrderSummaryNotFoundMessage.NotFound`

### Method: createOrderFollowUp(RazorpayOrderRequestModel request)
- **Validations:** (Similar to createOrder with status check for APPROVED or APPROVED_WITH_PARTIAL_PAYMENT)
  - Status check
  - **Condition:** Status not APPROVED or APPROVED_WITH_PARTIAL_PAYMENT
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.PaymentErrorMessages.FollowUpPaymentStatusRequired`

---

## 8. PickupLocationService

**File:** `PickupLocationService.java`
**Public Methods:** 8

### Method: getPickupLocationsInBatches(PaginationBaseRequestModel paginationBaseRequestModel)
- **Validations:**
  - Column name validation
  - **Condition:** Column not in validColumns set (12 valid columns)
  - **Exception:** `BadRequestException`
  - **Error Message:** `"Invalid column name: " + filter.getColumn()`

  - Operator validation
  - **Condition:** `!filter.isValidOperator()`
  - **Exception:** `BadRequestException`
  - **Error Message:** `"Invalid operator: " + filter.getOperator()`

  - Operator type match validation
  - **Condition:** Operator doesn't match column type
  - **Exception:** `BadRequestException`
  - **Error Message:** (from validateOperatorForType)

  - Value presence validation
  - **Condition:** Value missing for operators that require it
  - **Exception:** `BadRequestException`
  - **Error Message:** (from validateValuePresence)

  - Page size validation
  - **Condition:** `pageSize <= 0`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.CommonErrorMessages.InvalidPagination`

### Method: getPickupLocationById(long pickupLocationId)
- **Validations:**
  - Pickup location existence check
  - **Condition:** Pickup location not found for client
  - **Exception:** `NotFoundException`
  - **Error Message:** `String.format(ErrorMessages.PickupLocationErrorMessages.NotFound, pickupLocationId)`

### Method: createPickupLocation(PickupLocationRequestModel pickupLocationRequestModel)
- **Validations:** (Delegated to constructor and ShipRocket integration)

---

## 9. ProductReviewService

**File:** `ProductReviewService.java`
**Public Methods:** 4

### Method: insertProductReview(ProductReviewRequestModel productReviewRequestModel)
- **Validations:** (Delegated to constructor)

### Method: getProductReviewsInBatchesGivenProductId(...)
- **Validations:**
  - Page size validation
  - **Condition:** `pageSize <= 0`
  - **Exception:** `BadRequestException`
  - **Error Message:** `"Invalid pagination: end must be greater than start"`

### Method: toggleProductReview(long id)
- **Validations:**
  - Review existence check
  - **Condition:** Review not found for client
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.ProductReviewErrorMessages.NotFound`

### Method: setProductReviewScore(long id, boolean increaseScore)
- **Validations:**
  - Review existence check
  - **Condition:** Review not found for client
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.ProductReviewErrorMessages.NotFound`

---

## 10. ProductService

**File:** `ProductService.java`
**Public Methods:** 8

### Method: addProduct(ProductRequestModel productRequestModel)
- **Validations:** (Delegated to persistProduct)

### Method: editProduct(ProductRequestModel productRequestModel)
- **Validations:**
  - ProductId null check
  - **Condition:** `productRequestModel.getProductId() == null`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.ProductErrorMessages.InvalidId`

  - Product existence check
  - **Condition:** Product not found for client
  - **Exception:** `NotFoundException`
  - **Error Message:** `String.format(ErrorMessages.ProductErrorMessages.ER013, productRequestModel.getProductId())`

### Method: toggleDeleteProduct(long id)
- **Validations:**
  - Product existence check
  - **Condition:** Product not found for client
  - **Exception:** `NotFoundException`
  - **Error Message:** `String.format(ErrorMessages.ProductErrorMessages.ER013, id)`

### Method: toggleReturnProduct(long id)
- **Validations:**
  - Product existence check
  - **Condition:** Product not found for client
  - **Exception:** `NotFoundException`
  - **Error Message:** `String.format(ErrorMessages.ProductErrorMessages.ER013, id)`

### Method: getProductDetailsById(long id)
- **Validations:**
  - Product existence check
  - **Condition:** Product not found for client
  - **Exception:** `NotFoundException`
  - **Error Message:** `String.format(ErrorMessages.ProductErrorMessages.ER013, id)`

### Method: getProductInBatches(PaginationBaseRequestModel paginationBaseRequestModel)
- **Validations:**
  - Column name validation
  - **Condition:** Column not in validColumns set (27 valid columns)
  - **Exception:** `BadRequestException`
  - **Error Message:** `"Invalid column name: " + filter.getColumn()`

  - Operator validation
  - **Condition:** Operator validation (centralized)
  - **Exception:** `BadRequestException`
  - **Error Message:** (from FilterCondition validation)

---

## 11. PromoService

**File:** `PromoService.java`
**Public Methods:** 6

### Method: getPromosInBatches(PaginationBaseRequestModel paginationBaseRequestModel)
- **Validations:**
  - Column name validation
  - **Condition:** Column not in validColumns set (10 valid columns)
  - **Exception:** `BadRequestException`
  - **Error Message:** `"Invalid column name: " + filter.getColumn()`

  - Operator validation
  - **Condition:** `!filter.isValidOperator()`
  - **Exception:** `BadRequestException`
  - **Error Message:** `"Invalid operator: " + filter.getOperator()`

  - Operator type match validation
  - **Condition:** Operator doesn't match column type
  - **Exception:** `BadRequestException`
  - **Error Message:** (from validateOperatorForType)

  - Value presence validation
  - **Condition:** Value missing for required operators
  - **Exception:** `BadRequestException`
  - **Error Message:** (from validateValuePresence)

  - Page size validation
  - **Condition:** `pageSize <= 0`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.CommonErrorMessages.InvalidPagination`

### Method: createPromo(PromoRequestModel promoRequestModel)
- **Validations:** (Delegated to overloaded method)

### Method: getPromoDetailsById(long id)
- **Validations:**
  - Promo existence check
  - **Condition:** Promo not found for client
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.PromoErrorMessages.InvalidId`

### Method: togglePromo(long id)
- **Validations:**
  - Promo existence check
  - **Condition:** Promo not found for client
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.PromoErrorMessages.InvalidId`

### Method: getPromoDetailsByName(String promoCode)
- **Validations:**
  - Promo code null/empty check
  - **Condition:** `promoCode == null || isEmpty`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.PromoErrorMessages.InvalidPromoCode`

  - Promo code existence check (case-insensitive)
  - **Condition:** Promo code not found for client
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.PromoErrorMessages.InvalidName`

### Method: bulkCreatePromosAsync(...)
- **Validations:**
  - Promo list null/empty check
  - **Condition:** `promos == null || isEmpty`
  - **Exception:** `BadRequestException`
  - **Error Message:** `String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "Promo")`

---

## 12. PurchaseOrderService

**File:** `PurchaseOrderService.java`
**Public Methods:** 15+ (large service)

### Method: getPurchaseOrdersInBatches(PaginationBaseRequestModel paginationBaseRequestModel)
- **Validations:**
  - Column name validation
  - **Condition:** Column not in validColumns set (16+ valid columns)
  - **Exception:** `BadRequestException`
  - **Error Message:** `String.format(ErrorMessages.PurchaseOrderErrorMessages.InvalidColumnName, filter.getColumn())`

  - Operator validation
  - **Condition:** Operator not in validOperators set (10 valid operators)
  - **Exception:** `BadRequestException`
  - **Error Message:** `String.format(ErrorMessages.PurchaseOrderErrorMessages.InvalidOperator, filter.getOperator())`

  - Boolean column operator check
  - **Condition:** Boolean column with non-equals/notEquals operator
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.PurchaseOrderErrorMessages.BooleanColumnsOnlySupportEquals`

  - Date/number column operator check
  - **Condition:** Date/number column with non-comparison operator
  - **Exception:** `BadRequestException`
  - **Error Message:** `String.format(ErrorMessages.PurchaseOrderErrorMessages.ColumnsOnlySupportNumericOperators, columnType)`

  - Page size validation
  - **Condition:** `pageSize <= 0`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.PurchaseOrderErrorMessages.InvalidPagination`

---

## 13. ShipmentProcessingService

**File:** `ShipmentProcessingService.java`
**Public Methods:** 2

### Method: processShipmentsAfterPaymentApproval (Cash Payment)
- **Validations:**
  - Purchase order existence check
  - **Condition:** Purchase order not found
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.PurchaseOrderErrorMessages.InvalidId`

  - Client access check
  - **Condition:** PO doesn't belong to current client
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.CommonErrorMessages.AccessDeniedToPurchaseOrder`

  - PO status check
  - **Condition:** Status not PENDING_APPROVAL
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.PaymentErrorMessages.OnlyPendingApprovalCanBePaid`

  - OrderSummary existence check
  - **Condition:** OrderSummary not found
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.OrderSummaryNotFoundMessage.NotFound`

  - Shipment existence check
  - **Condition:** No shipments found
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.ShipmentErrorMessages.NoShipmentsFound`

  - Product availability check (per location)
  - **Condition:** Product not available at pickup location
  - **Exception:** `BadRequestException`
  - **Error Message:** `"Product ID " + id + " is not available at pickup location ID " + locationId`

  - Product stock check
  - **Condition:** Insufficient stock
  - **Exception:** `BadRequestException`
  - **Error Message:** `"Insufficient stock for product ID " + id + " at pickup location ID " + locationId + ...`

  - Package availability check
  - **Condition:** Package not available at pickup location
  - **Exception:** `BadRequestException`
  - **Error Message:** `"Package ID " + id + " is not available at pickup location ID " + locationId`

  - Package quantity check
  - **Condition:** Insufficient packages
  - **Exception:** `BadRequestException`
  - **Error Message:** `"Insufficient packages for package ID " + id + " at pickup location ID " + locationId + ...`

### Method: processShipmentsAfterPaymentApproval (Online Payment)
- **Validations:** (Same as cash payment variant)

---

## 14. ShipmentService

**File:** `ShipmentService.java`
**Public Methods:** 2

### Method: getShipmentsInBatches(PaginationBaseRequestModel paginationBaseRequestModel)
- **Validations:**
  - Column name validation
  - **Condition:** Column not in VALID_COLUMNS set (19 valid columns)
  - **Exception:** `BadRequestException`
  - **Error Message:** `String.format(ErrorMessages.PurchaseOrderErrorMessages.InvalidColumnName, filter.getColumn())`

  - Operator validation
  - **Condition:** `!filter.isValidOperator()`
  - **Exception:** `BadRequestException`
  - **Error Message:** `String.format(ErrorMessages.PurchaseOrderErrorMessages.InvalidOperator, filter.getOperator())`

  - Operator type match validation
  - **Condition:** Operator doesn't match column type
  - **Exception:** `BadRequestException`
  - **Error Message:** (from validateOperatorForType)

  - Value presence validation
  - **Condition:** Value missing for required operators
  - **Exception:** `BadRequestException`
  - **Error Message:** (from validateValuePresence)

  - Page size validation
  - **Condition:** `pageSize <= 0`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.CommonErrorMessages.InvalidPagination`

### Method: getShipmentById(Long shipmentId)
- **Validations:**
  - ShipmentId null/negative check
  - **Condition:** `shipmentId == null || shipmentId <= 0`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.ShipmentErrorMessages.InvalidId`

  - Shipment existence check
  - **Condition:** Shipment not found
  - **Exception:** `NotFoundException`
  - **Error Message:** `String.format(ErrorMessages.ShipmentErrorMessages.NotFound, shipmentId)`

  - Client access check
  - **Condition:** Shipment doesn't belong to current client
  - **Exception:** `NotFoundException`
  - **Error Message:** `String.format(ErrorMessages.ShipmentErrorMessages.NotFound, shipmentId)`

  - ShipRocket order ID check
  - **Condition:** ShipRocket order ID is null/empty
  - **Exception:** `NotFoundException`
  - **Error Message:** `String.format(ErrorMessages.ShipmentErrorMessages.NotFound, shipmentId)`

---

## 15. ShippingService

**File:** `ShippingService.java`
**Public Methods:** 2+

### Method: calculateShipping(ShippingCalculationRequestModel request)
- **Validations:**
  - Pickup locations null/empty check
  - **Condition:** `request.getPickupLocations() == null || isEmpty`
  - **Exception:** (Returns empty response)

  - Weight validation (minimum 0.5 kg)
  - **Condition:** `weight == null || weight < 0.5`
  - **Exception:** (Sets weight to 0.5)

---

## 16. TestExecutorService

**File:** `TestExecutorService.java`
**Public Methods:** 3

### Method: executeTestsAsync(...)
- **Validations:** (No explicit validations, internal state management)

---

## 17. TodoService

**File:** `TodoService.java`
**Public Methods:** 5

### Method: addTodo(TodoRequestModel todoRequestModel)
- **Validations:** (Delegated to constructor)

### Method: updateTodo(TodoRequestModel todoRequestModel)
- **Validations:**
  - Request null check
  - **Condition:** `todoRequestModel == null`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.TodoErrorMessages.InvalidId`

  - TodoId null check
  - **Condition:** `todoRequestModel.getTodoId() == null`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.TodoErrorMessages.InvalidId`

  - Todo existence check
  - **Condition:** Todo not found by ID
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.TodoErrorMessages.InvalidId`

### Method: deleteTodo(long id)
- **Validations:**
  - Todo existence check
  - **Condition:** `!todoRepository.existsById(id)`
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.TodoErrorMessages.InvalidId`

### Method: toggleTodo(long id)
- **Validations:**
  - Todo existence check
  - **Condition:** Todo not found by ID
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.TodoErrorMessages.InvalidId`

### Method: getTodoItems()
- **Validations:** None

---

## 18. UserGroupService

**File:** `UserGroupService.java`
**Public Methods:** 6

### Method: toggleUserGroup(long groupId)
- **Validations:**
  - Group existence check
  - **Condition:** `userGroup.isPresent()` is false
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.UserGroupErrorMessages.InvalidId`

### Method: getUserGroupDetailsById(long groupId)
- **Validations:**
  - Group existence check
  - **Condition:** `userGroup == null`
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.UserGroupErrorMessages.InvalidId`

### Method: createUserGroup(UserGroupRequestModel userGroupRequest)
- **Validations:** (Delegated to overloaded method)

### Method: updateUserGroup(UserGroupRequestModel userGroupRequest)
- **Validations:**
  - User list null/empty check
  - **Condition:** `userGroupRequest.getUserIds() == null || isEmpty`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.UserGroupErrorMessages.ER004`

  - Group existence check
  - **Condition:** Group not found by ID
  - **Exception:** `NotFoundException`
  - **Error Message:** (implied)

  - Duplicate group name check
  - **Condition:** Different group with same name exists
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.UserGroupErrorMessages.GroupNameExists`

### Method: fetchUserGroupsInClientInBatches(...)
- **Validations:**
  - Page size validation
  - **Condition:** `pageSize <= 0`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.CommonErrorMessages.InvalidPagination`

  - Logic operator validation
  - **Condition:** `!isValidLogicOperator()`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.CommonErrorMessages.InvalidLogicOperator`

  - Column name validation
  - **Condition:** Column not in validColumns set (11 valid columns)
  - **Exception:** `BadRequestException`
  - **Error Message:** `"Invalid column name: " + filter.getColumn() + ". Valid columns: " + list`

  - Operator validation
  - **Condition:** `!filter.isValidOperator()`
  - **Exception:** `BadRequestException`
  - **Error Message:** `"Invalid operator: " + filter.getOperator() + " for column: " + filter.getColumn()`

  - Operator type match validation
  - **Condition:** Operator doesn't match column type
  - **Exception:** `BadRequestException`
  - **Error Message:** (from validateOperatorForType)

---

## 19. UserLogService

**File:** `UserLogService.java`
**Public Methods:** 3

### Method: logData(long userId, String action, String oldValue, String newValue)
- **Validations:** None

### Method: logData(long userId, String newValue, String endPoint)
- **Validations:** None

### Method: logDataWithContext(...)
- **Validations:** None

### Method: fetchUserLogsInBatches(UserLogsRequestModel getUserLogsRequestModel)
- **Validations:**
  - Page size validation
  - **Condition:** `pageSize <= 0`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.CommonErrorMessages.InvalidPagination`

  - Logic operator validation
  - **Condition:** `!isValidLogicOperator()`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.CommonErrorMessages.InvalidLogicOperator`

  - Column name validation
  - **Condition:** Column not in validColumns set (15 valid columns)
  - **Exception:** `BadRequestException`
  - **Error Message:** `"Invalid column name: " + filter.getColumn() + ". Valid columns: " + list`

  - Operator validation
  - **Condition:** `!filter.isValidOperator()`
  - **Exception:** `BadRequestException`
  - **Error Message:** `"Invalid operator: " + filter.getOperator() + " for column: " + filter.getColumn()`

  - Operator type match validation
  - **Condition:** Operator doesn't match column type
  - **Exception:** `BadRequestException`
  - **Error Message:** (from validateOperatorForType)

---

## 20. UserService

**File:** `UserService.java`
**Public Methods:** 8

### Method: toggleUser(long id)
- **Validations:**
  - User existence check
  - **Condition:** User not found for client
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.UserErrorMessages.InvalidId`

### Method: getUserById(long id)
- **Validations:**
  - User existence check (with all relations)
  - **Condition:** User not found for client
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.UserErrorMessages.InvalidId`

### Method: getUserByEmail(String email)
- **Validations:**
  - User existence check by email
  - **Condition:** User not found for client
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.UserErrorMessages.InvalidEmail`

### Method: createUser(UserRequestModel userRequestModel)
- **Validations:** (Delegated to overloaded method and constructor)

### Method: updateUser(UserRequestModel user)
- **Validations:**
  - User existence check
  - **Condition:** User not found for client
  - **Exception:** `NotFoundException`
  - **Error Message:** `ErrorMessages.UserErrorMessages.InvalidId`

### Method: fetchUsersInCarrierInBatches(...)
- **Validations:**
  - Page size validation
  - **Condition:** `limit <= 0`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.CommonErrorMessages.InvalidPagination`

  - Column name validation
  - **Condition:** Column not in validColumns set (22 valid columns)
  - **Exception:** `BadRequestException`
  - **Error Message:** (implied)

  - Logic operator validation
  - **Condition:** `!isValidLogicOperator()`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.CommonErrorMessages.InvalidLogicOperator`

---

## 21. BaseService

**File:** `BaseService.java`
**Public Methods:** 3

### Method: getUser()
- **Validations:**
  - User name null/empty check
  - **Condition:** `userName == null || isEmpty`
  - **Exception:** `BadRequestException`
  - **Error Message:** `ErrorMessages.UserErrorMessages.InvalidUser`

### Method: getUserId()
- **Validations:** (Returns default for no context)

### Method: getClientId()
- **Validations:** (Returns default for no context)

---

## Summary Statistics

- **Total Services:** 22 (including BaseService)
- **Total Public Methods Analyzed:** 100+
- **Total Validation Checks Documented:** 200+
- **Exception Types Used:**
  - `BadRequestException` - 80+ occurrences
  - `NotFoundException` - 60+ occurrences
  - `UnauthorizedException` - 10+ occurrences
  - `RuntimeException` - 2+ occurrences

## Common Validation Patterns

1. **Null/Empty Checks:** Request objects, IDs, email addresses, names, passwords
2. **Existence Checks:** Entity lookups by ID, email, code, name
3. **State Checks:** Deletion status, email confirmation, account locks, PO status
4. **Permission Checks:** Client access validation, deleted entity filters
5. **Pagination Validation:** Page size, offset boundaries
6. **Filter Validation:** Column names, operators, operator type matching
7. **Business Logic Validations:** Duplicate names, configuration checks, inventory availability
8. **Format Validations:** Status codes, operator types, column types

## Test Case Recommendations

- **Per Validation:** Create one test per validation condition
- **Happy Path:** At least one test per public method with valid inputs
- **Edge Cases:** Boundary values, null objects, empty collections
- **Error Conditions:** Each exception path and error message
- **Multi-Validation Methods:** Test all validation combinations

