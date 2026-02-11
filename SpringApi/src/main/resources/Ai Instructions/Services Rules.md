# Comprehensive Service Class Standards - 300 Rules

## CRITICAL: Read This First
This document contains ALL rules for creating and modifying service classes in this Spring Boot application. Every rule must be followed. When modifying a service, read through ALL sections relevant to your changes. Do not skip rules or assume patterns - verify everything against this document.

---

## Class-Level Rules (1-6)

**Rule 1**: All service classes must be annotated with `@Service`
```java
@Service
public class UserService extends BaseService implements IUserSubTranslator {
```

**Rule 2**: Service classes should extend `BaseService`
```java
public class UserService extends BaseService {
```

**Rule 3**: Service classes must implement their corresponding interface
```java
public class UserService extends BaseService implements IUserSubTranslator {
```

**Rule 4**: Include comprehensive JavaDoc at class level with `@author`, `@version`, and `@since` tags
```java
/**
 * Service class for User-related business operations.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@Service
public class UserService extends BaseService implements IUserSubTranslator {
```

**Rule 5**: Add descriptive class-level JavaDoc explaining the service's purpose and responsibilities
```java
/**
 * Service class for managing User-related business operations.
 * 
 * This service implements the IUserSubTranslator interface and provides
 * comprehensive user management functionality including CRUD operations,
 * user retrieval, and user status management.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
```

**Rule 6**: Use `@Transactional` at class level only if majority of methods modify data
```java
@Service
@Transactional  // Only if most methods need transactions
public class UserService extends BaseService {
```

---

## Field Declaration Rules (7-12)

**Rule 7**: All repository fields must be declared as `private final`
```java
private final UserRepository userRepository;
private final AddressRepository addressRepository;
```

**Rule 8**: All service dependencies must be declared as `private final`
```java
private final UserLogService userLogService;
private final ClientService clientService;
```

**Rule 9**: All filter query builders must be declared as `private final`
```java
private final UserFilterQueryBuilder userFilterQueryBuilder;
```

**Rule 10**: Configuration values must use `@Value` annotation
```java
@Value("${imageLocation:firebase}")
private String imageLocation;
```

**Rule 11**: Fields must be ordered: repositories → services → query builders → helpers → configuration values
```java
// 1. Repositories
private final UserRepository userRepository;
private final AddressRepository addressRepository;

// 2. Services
private final UserLogService userLogService;
private final ClientService clientService;

// 3. Query Builders
private final UserFilterQueryBuilder userFilterQueryBuilder;

// 4. Helpers
private final MessageService messageService;

// 5. Configuration
@Value("${imageLocation:firebase}")
private String imageLocation;
```

**Rule 12**: No static logger fields (use ContextualLogger.getLogger() when needed)
```java
// WRONG
private static final Logger logger = LoggerFactory.getLogger(UserService.class);

// CORRECT
private final ContextualLogger logger;

@Autowired
public UserService(...) {
    this.logger = ContextualLogger.getLogger(UserService.class);
}
```

---

## Constructor Rules (13-17)

**Rule 13**: Use exactly one constructor
```java
@Autowired
public UserService(UserRepository userRepository, UserLogService userLogService) {
    // Only one constructor
}
```

**Rule 14**: Constructor must be annotated with `@Autowired`
```java
@Autowired
public UserService(UserRepository userRepository) {
```

**Rule 15**: Constructor must call `super()` as first statement
```java
@Autowired
public UserService(UserRepository userRepository) {
    super();  // Must be first
    this.userRepository = userRepository;
}
```

**Rule 16**: All final fields must be initialized in constructor
```java
@Autowired
public UserService(UserRepository userRepository, UserLogService userLogService) {
    super();
    this.userRepository = userRepository;  // Initialize all finals
    this.userLogService = userLogService;
}
```

**Rule 17**: Constructor parameters should match field declaration order
```java
// Fields order
private final UserRepository userRepository;
private final AddressRepository addressRepository;
private final UserLogService userLogService;

// Constructor parameter order should match
@Autowired
public UserService(UserRepository userRepository,
                   AddressRepository addressRepository,
                   UserLogService userLogService) {
```

---

## Method Organization Rules (18-22)

**Rule 18**: Public interface methods must appear before private helper methods
```java
@Override
public void createUser(UserRequestModel request) { }

@Override
public UserResponseModel getUserById(long id) { }

// ==================== HELPER METHODS ====================

private void createUserAddress(UserRequestModel request) { }
```

**Rule 19**: Use a clear separator comment between public and private methods
```java
@Override
public void updateUser(UserRequestModel request) { }

// ==================== HELPER METHODS ====================

private void updateUserPermissions(UserRequestModel request) { }
```

**Rule 20**: Order public methods: CRUD (Create, Read, Update, Delete/Toggle) → Batch → Special queries
```java
// CREATE
@Override
public void createUser(UserRequestModel request) { }

// READ
@Override
public UserResponseModel getUserById(long id) { }

// UPDATE
@Override
public void updateUser(UserRequestModel request) { }

// DELETE/TOGGLE
@Override
public void toggleUser(long id) { }

// BATCH
@Override
public void bulkCreateUsersAsync(...) { }

// SPECIAL QUERIES
@Override
public List<UserResponseModel> getUsersByRole(String role) { }
```

**Rule 21**: Group related helper methods together under the separator
```java
// ==================== HELPER METHODS ====================

// Address helpers grouped together
private void createUserAddress(...) { }
private void updateUserAddress(...) { }

// Permission helpers grouped together
private void createUserPermissions(...) { }
private void updateUserPermissions(...) { }
```

**Rule 22**: Helper methods should be `private` unless needed by subclasses (then `protected`)
```java
private void internalHelper() { }  // Only used internally

protected void helperForTests() { }  // Might be used by tests or subclasses
```

---

## Annotation Rules (23-28)

**Rule 23**: All interface implementation methods must use `@Override`
```java
@Override
public void createUser(UserRequestModel request) { }
```

**Rule 24**: Methods that modify data must be annotated with `@Transactional`
```java
@Override
@Transactional
public void createUser(UserRequestModel request) { }
```

**Rule 25**: Methods that only read data must be annotated with `@Transactional(readOnly = true)`
```java
@Override
@Transactional(readOnly = true)
public UserResponseModel getUserById(long id) { }
```

**Rule 26**: Async methods must be annotated with `@Async`
```java
@Override
@Async
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public void bulkCreateUsersAsync(...) { }
```

**Rule 27**: Async bulk operations must use `@Transactional(propagation = Propagation.NOT_SUPPORTED)`
```java
@Override
@Async
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public void bulkCreateUsersAsync(...) { }
```

**Rule 28**: Synchronous bulk operations (for testing) must use `@Transactional`
```java
@Override
@Transactional
public BulkInsertResponseModel<Long> bulkCreateUsers(...) { }
```

---

## JavaDoc Rules (29-34)

**Rule 29**: All public methods must have JavaDoc comments
```java
/**
 * Creates a new user in the system.
 */
@Override
public void createUser(UserRequestModel request) { }
```

**Rule 30**: JavaDoc must include `@param` tags for all parameters
```java
/**
 * Creates a new user in the system.
 * 
 * @param userRequestModel The user data to create
 */
@Override
public void createUser(UserRequestModel userRequestModel) { }
```

**Rule 31**: JavaDoc must include `@return` tag when method returns a value
```java
/**
 * Retrieves a user by ID.
 * 
 * @param id The user ID
 * @return UserResponseModel containing user data
 */
@Override
public UserResponseModel getUserById(long id) { }
```

**Rule 32**: JavaDoc must include `@throws` tags for all declared exceptions
```java
/**
 * Retrieves a user by ID.
 * 
 * @param id The user ID
 * @return UserResponseModel containing user data
 * @throws NotFoundException if user is not found
 */
@Override
public UserResponseModel getUserById(long id) { }
```

**Rule 33**: JavaDoc should describe the method's purpose, behavior, and any important side effects
```java
/**
 * Creates a new user in the system.
 * 
 * This method validates the provided user data, creates a new User entity,
 * and persists it to the database along with associated address and permissions.
 * The method automatically sets audit fields and sends confirmation email.
 * 
 * @param userRequestModel The user data to create
 * @throws BadRequestException if user data is invalid
 */
```

**Rule 34**: Include examples or usage notes in JavaDoc when behavior is complex
```java
/**
 * Retrieves users in paginated batches with filtering.
 * 
 * Valid columns for filtering: "userId", "firstName", "lastName", "email"
 * Example: Filter by firstName contains "John" AND lastName equals "Doe"
 * 
 * @param request The pagination and filter parameters
 * @return Paginated user data
 */
```

---

## Validation Rules (35-45)

**Rule 35**: Always validate input parameters at the beginning of methods
```java
@Override
public void createUser(UserRequestModel request) {
    if (request == null) {
        throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidRequest);
    }
    // ... rest of method
}
```

**Rule 36**: Validate null/empty collections before processing
```java
if (users == null || users.isEmpty()) {
    throw new BadRequestException(
        String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "User"));
}
```

**Rule 37**: Validate pagination parameters (start, end, pageSize) before database calls
```java
int start = request.getStart();
int end = request.getEnd();
int pageSize = end - start;

if (pageSize <= 0) {
    throw new BadRequestException(ErrorMessages.CommonErrorMessages.InvalidPagination);
}
```

**Rule 38**: Validate logic operators when provided (must be "AND" or "OR")
```java
if (request.getLogicOperator() != null && !request.isValidLogicOperator()) {
    throw new BadRequestException(ErrorMessages.CommonErrorMessages.InvalidLogicOperator);
}
```

**Rule 39**: Validate column names against a Set of valid columns
```java
Set<String> validColumns = new HashSet<>(Arrays.asList(
    "userId", "firstName", "lastName", "email"));

if (!validColumns.contains(filter.getColumn())) {
    throw new BadRequestException("Invalid column: " + filter.getColumn());
}
```

**Rule 40**: Validate filter operators using `filter.isValidOperator()`
```java
if (!filter.isValidOperator()) {
    throw new BadRequestException("Invalid operator: " + filter.getOperator());
}
```

**Rule 41**: Validate operator matches column type using `filter.validateOperatorForType()`
```java
String columnType = userFilterQueryBuilder.getColumnType(filter.getColumn());
filter.validateOperatorForType(columnType, filter.getColumn());
```

**Rule 42**: Validate value presence using `filter.validateValuePresence()`
```java
filter.validateValuePresence();
```

**Rule 43**: Validation should occur in this order: null checks → logic operator → column names → operators → type matching → value presence
```java
// 1. Null checks
if (request == null) { throw ... }

// 2. Logic operator
if (request.getLogicOperator() != null && !request.isValidLogicOperator()) { throw ... }

// 3. Column names
if (!validColumns.contains(filter.getColumn())) { throw ... }

// 4. Operators
if (!filter.isValidOperator()) { throw ... }

// 5. Type matching
filter.validateOperatorForType(columnType, filter.getColumn());

// 6. Value presence
filter.validateValuePresence();
```

**Rule 44**: Throw `BadRequestException` for validation failures with descriptive error messages
```java
throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidEmail);
```

**Rule 45**: Throw `NotFoundException` when entities are not found
```java
throw new NotFoundException(ErrorMessages.UserErrorMessages.InvalidId);
```

---

## Security Context Rules (46-52)

**Rule 46**: Use `getUser()` to retrieve current user's loginName
```java
String currentUser = getUser();
entity.setCreatedUser(currentUser);
```

**Rule 47**: Use `getUserId()` to retrieve current user's ID
```java
Long currentUserId = getUserId();
userLogService.logData(currentUserId, message, endpoint);
```

**Rule 48**: Use `getClientId()` to retrieve current client ID
```java
Long currentClientId = getClientId();
entity.setClientId(currentClientId);
```

**Rule 49**: Never call security context methods in `@Async` methods
```java
// WRONG
@Async
public void bulkCreateUsersAsync(List<UserRequestModel> users) {
    String user = getUser();  // NEVER DO THIS IN ASYNC
}

// CORRECT
@Async
public void bulkCreateUsersAsync(List<UserRequestModel> users, 
                                  String requestingUserLoginName) {
    String user = requestingUserLoginName;  // Use parameter
}
```

**Rule 50**: Capture security context values before calling async methods
```java
// In controller or synchronous method
Long requestingUserId = getUserId();
String requestingUserLoginName = getUser();
Long requestingClientId = getClientId();

bulkCreateUsersAsync(users, requestingUserId, requestingUserLoginName, requestingClientId);
```

**Rule 51**: Pass captured context values as parameters to async methods
```java
@Override
@Async
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public void bulkCreateUsersAsync(List<UserRequestModel> users,
                                  Long requestingUserId,
                                  String requestingUserLoginName,
                                  Long requestingClientId) {
```

**Rule 52**: Set `createdUser` and `modifiedUser` using `getUser()` or passed `createdUser` parameter
```java
// In sync methods
entity.setCreatedUser(getUser());

// In async methods or helpers
entity.setCreatedUser(createdUser);  // Use parameter
```

---

## Bulk Operation Rules (53-70)

**Rule 53**: Async bulk operations must accept: List of items, requestingUserId, requestingUserLoginName, requestingClientId
```java
@Override
@Async
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public void bulkCreateUsersAsync(List<UserRequestModel> users,
                                  Long requestingUserId,
                                  String requestingUserLoginName,
                                  Long requestingClientId) {
```

**Rule 54**: Async bulk methods must return `void`
```java
@Override
@Async
public void bulkCreateUsersAsync(...) {
    // Returns void
}
```

**Rule 55**: Synchronous bulk methods must return `BulkInsertResponseModel<Long>`
```java
@Override
@Transactional
public BulkInsertResponseModel<Long> bulkCreateUsers(List<UserRequestModel> users) {
    BulkInsertResponseModel<Long> response = new BulkInsertResponseModel<>();
    // ...
    return response;
}
```

**Rule 56**: Always validate that input list is not null or empty as first step
```java
if (users == null || users.isEmpty()) {
    throw new BadRequestException(
        String.format(ErrorMessages.CommonErrorMessages.ListCannotBeNullOrEmpty, "User"));
}
```

**Rule 57**: Create `BulkInsertResponseModel` and set `totalRequested` at start
```java
BulkInsertResponseModel<Long> response = new BulkInsertResponseModel<>();
response.setTotalRequested(users.size());
```

**Rule 58**: Process each item individually in a loop
```java
for (UserRequestModel userRequest : users) {
    try {
        createUser(userRequest, requestingUserLoginName, false);
        // ...
    } catch (Exception e) {
        // ...
    }
}
```

**Rule 59**: Catch `BadRequestException` separately from general `Exception` in item processing
```java
for (UserRequestModel userRequest : users) {
    try {
        createUser(userRequest, requestingUserLoginName, false);
    } catch (BadRequestException bre) {
        response.addFailure(userRequest.getEmail(), bre.getMessage());
        failureCount++;
    } catch (Exception e) {
        response.addFailure(userRequest.getEmail(), "Error: " + e.getMessage());
        failureCount++;
    }
}
```

**Rule 60**: Use `response.addSuccess()` for successful items
```java
response.addSuccess(userRequest.getEmail(), createdUser.getUserId());
successCount++;
```

**Rule 61**: Use `response.addFailure()` for failed items with error message
```java
response.addFailure(userRequest.getEmail(), bre.getMessage());
failureCount++;
```

**Rule 62**: Track `successCount` and `failureCount` separately
```java
int successCount = 0;
int failureCount = 0;

for (UserRequestModel userRequest : users) {
    try {
        // ...
        successCount++;
    } catch (Exception e) {
        failureCount++;
    }
}
```

**Rule 63**: Set `successCount` and `failureCount` on response before returning/sending message
```java
response.setSuccessCount(successCount);
response.setFailureCount(failureCount);
```

**Rule 64**: Async bulk operations must wrap entire logic in try-catch
```java
@Async
public void bulkCreateUsersAsync(...) {
    try {
        // All bulk logic here
    } catch (Exception e) {
        // Send error message to user
    }
}
```

**Rule 65**: On critical failure, send error message to user with failure details
```java
catch (Exception e) {
    BulkInsertResponseModel<Long> errorResponse = new BulkInsertResponseModel<>();
    errorResponse.setTotalRequested(users != null ? users.size() : 0);
    errorResponse.setSuccessCount(0);
    errorResponse.setFailureCount(users != null ? users.size() : 0);
    errorResponse.addFailure("bulk_import", "Critical error: " + e.getMessage());
    BulkInsertHelper.createDetailedBulkInsertResultMessage(...);
}
```

**Rule 66**: Never log individual item operations in bulk imports (use shouldLog = false)
```java
for (UserRequestModel userRequest : users) {
    createUser(userRequest, requestingUserLoginName, false);  // shouldLog = false
}
```

**Rule 67**: Log bulk operation summary after completion with success/failure counts
```java
userLogService.logDataWithContext(
    requestingUserId,
    requestingUserLoginName,
    requestingClientId,
    SuccessMessages.UserSuccessMessages.CreateUser + " (Bulk: " + successCount + 
        " succeeded, " + failureCount + " failed)",
    ApiRoutes.UsersSubRoute.BULK_CREATE_USER);
```

**Rule 68**: Use `userLogService.logDataWithContext()` in async methods with captured context
```java
userLogService.logDataWithContext(requestingUserId, requestingUserLoginName, 
    requestingClientId, message, endpoint);
```

**Rule 69**: Send detailed results to user via `BulkInsertHelper.createDetailedBulkInsertResultMessage()`
```java
BulkInsertHelper.createDetailedBulkInsertResultMessage(
    response, "User", "Users", "Email", "User ID",
    messageService, requestingUserId, requestingUserLoginName, requestingClientId);
```

**Rule 70**: Synchronous bulk operations should return response, not send messages
```java
@Transactional
public BulkInsertResponseModel<Long> bulkCreateUsers(List<UserRequestModel> users) {
    // ... process items ...
    return response;  // Return, don't send message
}
```

---

## Helper Method Rules (71-77)

**Rule 71**: Create overloaded helper methods for create/update operations (one for sync, one for async)
```java
// Sync version
@Transactional
protected void createUser(UserRequestModel request, boolean shouldLog) {
    createUser(request, getUser(), shouldLog);
}

// Async version
@Transactional
private void createUser(UserRequestModel request, String createdUser, boolean shouldLog) {
    // Implementation
}
```

**Rule 72**: Public API helper should call async-compatible helper with `getUser()` and `shouldLog = true`
```java
@Transactional
protected void createUser(UserRequestModel request, boolean shouldLog) {
    createUser(request, getUser(), true);  // Calls async-compatible version
}
```

**Rule 73**: Async-compatible helper should accept `createdUser` and `shouldLog` parameters
```java
@Transactional
private void createUser(UserRequestModel request, String createdUser, boolean shouldLog) {
```

**Rule 74**: Helper methods should be annotated with `@Transactional` when performing database operations
```java
@Transactional
private void createUserAddress(UserRequestModel request, User user, String createdUser) {
    Address address = new Address(request.getAddress(), createdUser);
    addressRepository.save(address);
}
```

**Rule 75**: Use `protected` visibility for helpers that might be used by tests or subclasses
```java
protected void createUser(UserRequestModel request, boolean sendEmail) {
```

**Rule 76**: Use `private` visibility for internal-only helpers
```java
private void createUserAddress(UserRequestModel request, User user) {
```

**Rule 77**: Helper method names should be descriptive
```java
// Good names
private void createUserAddress(...)
private void updateUserPermissions(...)
private void uploadUserProfilePicture(...)

// Bad names
private void doStuff(...)
private void helper1(...)
```

---

## Logging Rules (78-85)

**Rule 78**: Always log successful operations using `userLogService.logData()`
```java
User savedUser = userRepository.save(user);
userLogService.logData(getUserId(),
    SuccessMessages.UserSuccessMessages.CreateUser + " " + savedUser.getUserId(),
    ApiRoutes.UserSubRoute.CREATE_USER);
```

**Rule 79**: Log message format: `SuccessMessages.EntitySuccessMessages.Operation + " " + entity.getId()`
```java
userLogService.logData(getUserId(),
    SuccessMessages.UserSuccessMessages.CreateUser + " " + savedUser.getUserId(),
    ApiRoutes.UserSubRoute.CREATE_USER);
```

**Rule 80**: Include the API route endpoint as third parameter to logging
```java
userLogService.logData(getUserId(), message, ApiRoutes.UserSubRoute.CREATE_USER);
```

**Rule 81**: For bulk operations, log summary with format: `"Operation (Bulk: X succeeded, Y failed)"`
```java
userLogService.logDataWithContext(requestingUserId, requestingUserLoginName, requestingClientId,
    SuccessMessages.UserSuccessMessages.CreateUser + " (Bulk: " + successCount + 
        " succeeded, " + failureCount + " failed)",
    ApiRoutes.UsersSubRoute.BULK_CREATE_USER);
```

**Rule 82**: Use `userLogService.logDataWithContext()` in async methods
```java
@Async
public void bulkCreateUsersAsync(...) {
    userLogService.logDataWithContext(requestingUserId, requestingUserLoginName, 
        requestingClientId, message, endpoint);
}
```

**Rule 83**: Conditional logging should use `if (shouldLog)`
```java
if (shouldLog) {
    userLogService.logData(getUserId(), message, endpoint);
}
```

**Rule 84**: Don't log individual items in bulk operations
```java
// WRONG
for (UserRequestModel user : users) {
    createUser(user, getUser(), true);  // Logs each user
}

// CORRECT
for (UserRequestModel user : users) {
    createUser(user, getUser(), false);  // Doesn't log each user
}
// Log once after loop
userLogService.logData(..., "Bulk: " + successCount + " succeeded");
```

**Rule 85**: Don't log validation failures (exceptions handle those)
```java
// WRONG
if (request == null) {
    userLogService.logData(getUserId(), "Validation failed", endpoint);
    throw new BadRequestException(...);
}

// CORRECT
if (request == null) {
    throw new BadRequestException(...);  // Just throw, don't log
}
```

---

## Pagination Rules (86-93)

**Rule 86**: Calculate pageSize as `end - start`
```java
int start = request.getStart();
int end = request.getEnd();
int pageSize = end - start;
```

**Rule 87**: Validate that pageSize is greater than 0
```java
if (pageSize <= 0) {
    throw new BadRequestException(ErrorMessages.CommonErrorMessages.InvalidPagination);
}
```

**Rule 88**: Create custom `PageRequest` with offset override
```java
Pageable pageable = new PageRequest(0, pageSize, Sort.by("userId").descending()) {
    @Override
    public long getOffset() {
        return start;
    }
};
```

**Rule 89**: Use `Sort.by("entityId").descending()` as default sort
```java
Sort.by("userId").descending()
```

**Rule 90**: Override `getOffset()` method to return `start` value
```java
@Override
public long getOffset() {
    return start;
}
```

**Rule 91**: Pass `includeDeleted` flag to query builder
```java
Page<User> page = userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
    getClientId(),
    request.getSelectedUserIds(),
    logicOperator,
    request.getFilters(),
    request.isIncludeDeleted(),  // Pass includeDeleted flag
    pageable);
```

**Rule 92**: Extract `totalElements` from Page object for response count
```java
response.setTotalDataCount(page.getTotalElements());
```

**Rule 93**: Convert Page content to response models before returning
```java
response.setData(page.getContent().stream()
    .map(UserResponseModel::new)
    .collect(Collectors.toList()));
```

---

## Filter Query Builder Rules (94-100)

**Rule 94**: Declare valid columns as `Set<String>` using `Arrays.asList()`
```java
Set<String> validColumns = new HashSet<>(Arrays.asList(
    "userId", "firstName", "lastName", "email"));
```

**Rule 95**: Include all filterable columns in the valid columns set
```java
Set<String> validColumns = new HashSet<>(Arrays.asList(
    "userId", "firstName", "lastName", "email", "role", "phone",
    "isDeleted", "createdUser", "modifiedUser", "createdAt", "updatedAt"));
```

**Rule 96**: Validate filters only if they are present and non-empty
```java
if (request.getFilters() != null && !request.getFilters().isEmpty()) {
    // Validate filters
}
```

**Rule 97**: For each filter, validate in order: column name → operator → type matching → value presence
```java
for (FilterCondition filter : request.getFilters()) {
    // 1. Column name
    if (!validColumns.contains(filter.getColumn())) { throw ... }
    
    // 2. Operator
    if (!filter.isValidOperator()) { throw ... }
    
    // 3. Type matching
    filter.validateOperatorForType(columnType, filter.getColumn());
    
    // 4. Value presence
    filter.validateValuePresence();
}
```

**Rule 98**: Use filter query builder for all paginated queries with filtering
```java
Page<User> page = userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
    getClientId(), selectedIds, logicOperator, filters, includeDeleted, pageable);
```

**Rule 99**: Pass clientId, selectedIds, logicOperator, filters, includeDeleted, and pageable to query builder
```java
Page<User> page = userFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
    getClientId(),                     // clientId
    request.getSelectedUserIds(),       // selectedIds
    logicOperator,                      // "AND" or "OR"
    request.getFilters(),               // filter conditions
    request.isIncludeDeleted(),         // includeDeleted
    pageable);                          // pageable
```

**Rule 100**: Default logicOperator to "AND" if not provided
```java
String logicOperator = request.getLogicOperator() != null ? 
    request.getLogicOperator() : "AND";
```

---

## Response Model Rules (101-105)

**Rule 101**: Never return entity objects directly from service methods
```java
// WRONG
@Override
public User getUserById(long id) {
    return userRepository.findById(id).orElseThrow();
}

// CORRECT
@Override
public UserResponseModel getUserById(long id) {
    User user = userRepository.findById(id).orElseThrow();
    return new UserResponseModel(user);
}
```

**Rule 102**: Always convert entities to response models before returning
```java
User user = userRepository.findById(id).orElseThrow();
return new UserResponseModel(user);  // Convert to response model
```

**Rule 103**: Use entity constructor in response model for conversion
```java
return new UserResponseModel(user);
```

**Rule 104**: Convert collections using `.stream().map(ResponseModel::new).toList()`
```java
return users.stream()
    .map(UserResponseModel::new)
    .collect(Collectors.toList());
```

**Rule 105**: Set pagination metadata (data, totalDataCount) on response before returning
```java
PaginationBaseResponseModel<UserResponseModel> response = new PaginationBaseResponseModel<>();
response.setData(userResponseModels);
response.setTotalDataCount(page.getTotalElements());
return response;
```

---

## Toggle Operation Rules (106-112)

**Rule 106**: Toggle methods must accept entity ID as parameter
```java
@Override
@Transactional
public void toggleUser(long id) {
```

**Rule 107**: Find entity by ID, throw `NotFoundException` if not found
```java
User user = userRepository.findById(id)
    .orElseThrow(() -> new NotFoundException(ErrorMessages.UserErrorMessages.InvalidId));
```

**Rule 108**: Toggle the `isDeleted` flag: `entity.setIsDeleted(!entity.getIsDeleted())`
```java
user.setIsDeleted(!user.getIsDeleted());
```

**Rule 109**: Set `modifiedUser` using `getUser()`
```java
user.setModifiedUser(getUser());
```

**Rule 110**: Save the entity after toggling
```java
userRepository.save(user);
```

**Rule 111**: Log the toggle operation with entity ID
```java
userLogService.logData(getUserId(),
    SuccessMessages.UserSuccessMessages.ToggleUser + " " + user.getUserId(),
    ApiRoutes.UserSubRoute.TOGGLE_USER);
```

**Rule 112**: Annotate toggle methods with `@Transactional`
```java
@Override
@Transactional
public void toggleUser(long id) {
```

---

## Create Operation Rules (113-119)

**Rule 113**: Create operations must validate all required fields before database operations
```java
if (request.getFirstName() == null || request.getFirstName().isEmpty()) {
    throw new BadRequestException(ErrorMessages.UserErrorMessages.ER001);
}
```

**Rule 114**: Set clientId from security context, not from request (security measure)
```java
// WRONG
entity.setClientId(request.getClientId());

// CORRECT
entity.setClientId(getClientId());
```

**Rule 115**: Set createdById/createdUser from security context or passed parameter
```java
// In sync methods
entity.setCreatedUser(getUser());

// In async methods
entity.setCreatedUser(createdUser);  // From parameter
```

**Rule 116**: Create and save main entity first to get generated ID
```java
User user = new User(request, getUser());
User savedUser = userRepository.save(user);  // Save first to get ID

// Now use savedUser.getUserId() for related entities
```

**Rule 117**: Create related entities (addresses, mappings) after main entity has ID
```java
User savedUser = userRepository.save(user);  // Save first

// Now create related entities
if (request.getAddress() != null) {
    Address address = new Address(request.getAddress(), getUser());
    address.setUserId(savedUser.getUserId());  // Use generated ID
    addressRepository.save(address);
}
```

**Rule 118**: Use constructor-based entity creation, not setters
```java
// CORRECT
User user = new User(request, getUser());

// WRONG
User user = new User();
user.setFirstName(request.getFirstName());
user.setLastName(request.getLastName());
// ... many setters
```

**Rule 119**: Log successful creation with generated entity ID
```java
User savedUser = userRepository.save(user);
userLogService.logData(getUserId(),
    SuccessMessages.UserSuccessMessages.CreateUser + " " + savedUser.getUserId(),
    ApiRoutes.UserSubRoute.CREATE_USER);
```

**Rule 120**: Throw `BadRequestException` if required data is missing or invalid
```java
if (request == null) {
    throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidRequest);
}
```

---

## Update Operation Rules (121-129)

**Rule 121**: Update operations must accept entity ID and request model
```java
@Override
@Transactional
public void updateUser(Long userId, UserRequestModel request) {
```

**Rule 122**: Fetch existing entity first, throw `NotFoundException` if not found
```java
User existingUser = userRepository.findById(userId)
    .orElseThrow(() -> new NotFoundException(ErrorMessages.UserErrorMessages.InvalidId));
```

**Rule 123**: Check if entity is deleted, throw `NotFoundException` if attempting to update deleted entity
```java
if (existingUser.getIsDeleted()) {
    throw new NotFoundException(ErrorMessages.UserErrorMessages.InvalidId);
}
```

**Rule 124**: Preserve original `createdUser` and `createdAt` values
```java
// Constructor should preserve these
User updatedUser = new User(request, getUser(), existingUser);
// existingUser.getCreatedUser() and existingUser.getCreatedAt() are preserved
```

**Rule 125**: Set `modifiedUser` from `getUser()` or passed parameter
```java
updatedUser.setModifiedUser(getUser());
```

**Rule 126**: Use constructor with existing entity for updates
```java
User updatedUser = new User(request, getUser(), existingUser);
```

**Rule 127**: Update related entities (addresses, permissions) separately
```java
updateUserAddress(request, existingUser);
updateUserPermissions(request, existingUser);
updateUserGroups(request, existingUser);

// Then update main entity
User updatedUser = new User(request, getUser(), existingUser);
userRepository.save(updatedUser);
```

**Rule 128**: For many-to-many relationships, delete old mappings before creating new ones
```java
// Delete old permissions
userClientPermissionMappingRepository.deleteByUserIdAndClientId(userId, getClientId());

// Create new permissions
List<UserClientPermissionMapping> newPerms = new ArrayList<>();
for (Long permissionId : request.getPermissionIds()) {
    newPerms.add(new UserClientPermissionMapping(userId, getClientId(), permissionId, getUser(), getUser()));
}
userClientPermissionMappingRepository.saveAll(newPerms);
```

**Rule 129**: Log successful update with entity ID
```java
userLogService.logData(getUserId(),
    SuccessMessages.UserSuccessMessages.UpdateUser + " " + updatedUser.getUserId(),
    ApiRoutes.UserSubRoute.UPDATE_USER);
```

---

## Delete Operation Rules (130-134)

**Rule 130**: Never perform hard deletes (except for mappings/relations)
```java
// WRONG
userRepository.deleteById(userId);

// CORRECT
user.setIsDeleted(true);
userRepository.save(user);
```

**Rule 131**: Use soft delete by setting `isDeleted = true`
```java
user.setIsDeleted(true);
user.setModifiedUser(getUser());
userRepository.save(user);
```

**Rule 132**: Toggle methods should flip the boolean, not always set to true
```java
// CORRECT - flips the value
user.setIsDeleted(!user.getIsDeleted());

// WRONG - always sets to true
user.setIsDeleted(true);
```

**Rule 133**: Cascade soft deletes are handled at database level, not in service
```java
// Don't manually cascade soft deletes in service
// Database foreign key constraints and triggers handle this
```

**Rule 134**: Set `modifiedUser` when soft deleting
```java
user.setIsDeleted(true);
user.setModifiedUser(getUser());
```

---

## Related Entity Handling Rules (135-140)

**Rule 135**: Create separate helper methods for creating/updating related entities
```java
private void createUserAddress(UserRequestModel request, User user, String createdUser) {
    // ...
}

private void updateUserPermissions(UserRequestModel request, User existingUser) {
    // ...
}
```

**Rule 136**: Related entities (addresses) should be created/updated before setting foreign key on main entity
```java
// Create address first
Address savedAddress = addressRepository.save(address);

// Then set FK on main entity
user.setAddressId(savedAddress.getAddressId());
userRepository.save(user);
```

**Rule 137**: Many-to-many mappings should be created after main entity is saved
```java
User savedUser = userRepository.save(user);  // Save first

// Now create mappings with savedUser.getUserId()
createUserPermissions(request, savedUser, getUser());
```

**Rule 138**: Use `saveAll()` for batch saving of mappings
```java
List<UserClientPermissionMapping> mappings = new ArrayList<>();
for (Long permissionId : request.getPermissionIds()) {
    mappings.add(new UserClientPermissionMapping(...));
}
userClientPermissionMappingRepository.saveAll(mappings);  // Batch save
```

**Rule 139**: Delete old mappings before creating new ones on update operations
```java
// Delete all old mappings
userClientPermissionMappingRepository.deleteByUserIdAndClientId(userId, clientId);

// Create new mappings
userClientPermissionMappingRepository.saveAll(newMappings);
```

**Rule 140**: Pass `createdUser` to related entity constructors
```java
Address address = new Address(request.getAddress(), createdUser);
```

---

## Error Handling Rules (141-147)

**Rule 141**: Use `BadRequestException` for validation errors
```java
if (request == null) {
    throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidRequest);
}
```

**Rule 142**: Use `NotFoundException` for entity not found errors
```java
User user = userRepository.findById(id)
    .orElseThrow(() -> new NotFoundException(ErrorMessages.UserErrorMessages.InvalidId));
```

**Rule 143**: Include descriptive error messages from `ErrorMessages` constants
```java
throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidEmail);
```

**Rule 144**: Don't catch exceptions in single operations (let them propagate)
```java
// WRONG
@Override
public void createUser(UserRequestModel request) {
    try {
        // ... creation logic
    } catch (Exception e) {
        // Don't catch in single operations
    }
}

// CORRECT
@Override
public void createUser(UserRequestModel request) {
    // ... creation logic (let exceptions propagate)
}
```

**Rule 145**: Catch and handle exceptions in bulk operations (for partial success)
```java
for (UserRequestModel userRequest : users) {
    try {
        createUser(userRequest, createdUser, false);
        successCount++;
    } catch (BadRequestException bre) {
        response.addFailure(userRequest.getEmail(), bre.getMessage());
        failureCount++;
    } catch (Exception e) {
        response.addFailure(userRequest.getEmail(), "Error: " + e.getMessage());
        failureCount++;
    }
}
```

**Rule 146**: In async bulk operations, wrap entire method in try-catch to ensure message is sent
```java
@Async
public void bulkCreateUsersAsync(...) {
    try {
        // All bulk logic
    } catch (Exception e) {
        // Send error message to user
        BulkInsertHelper.createDetailedBulkInsertResultMessage(errorResponse, ...);
    }
}
```

**Rule 147**: Format error messages to include context (e.g., column name, value)
```java
throw new BadRequestException("Invalid column: " + filter.getColumn() + 
    ". Valid columns: " + String.join(", ", validColumns));
```

---

## Image/File Upload Rules (148-153)

**Rule 148**: Validate image/file data before attempting upload
```java
if (request.getProfilePictureBase64() == null || 
    request.getProfilePictureBase64().isEmpty()) {
    return;  // Nothing to upload
}
```

**Rule 149**: Use configuration to determine upload destination (Firebase vs ImgBB)
```java
if (ImageLocationConstants.IMGBB.equalsIgnoreCase(imageLocation)) {
    // Upload to ImgBB
} else if (ImageLocationConstants.FIREBASE.equalsIgnoreCase(imageLocation)) {
    // Upload to Firebase
}
```

**Rule 150**: Generate custom filenames with environment, client name, and entity ID
```java
String customFileName = ImgbbHelper.generateCustomFileNameForUserProfile(
    environmentName,
    clientDetails.getName(),
    savedUser.getUserId());
```

**Rule 151**: Delete old images before uploading new ones on update operations
```java
// Delete old image from ImgBB
if (existingUser.getProfilePictureDeleteHash() != null) {
    imgbbHelper.deleteImage(existingUser.getProfilePictureDeleteHash());
}

// Upload new image
ImgbbHelper.ImgbbUploadResponse uploadResponse = imgbbHelper.uploadFileToImgbb(...);
```

**Rule 152**: Store both URL and deleteHash for ImgBB uploads
```java
savedUser.setProfilePicture(uploadResponse.getUrl());
savedUser.setProfilePictureDeleteHash(uploadResponse.getDeleteHash());
userRepository.save(savedUser);
```

**Rule 153**: Handle upload failures with descriptive error messages
```java
if (uploadResponse == null || uploadResponse.getUrl() == null) {
    throw new BadRequestException(ErrorMessages.UserErrorMessages.ProfilePictureUploadFailed);
}
```

---

## Query Optimization Rules (154-160)

**Rule 154**: Use JOIN FETCH to load related entities in single query when needed
```java
// In repository
@Query("SELECT u FROM User u LEFT JOIN FETCH u.address WHERE u.userId = :userId")
User findByIdWithAddress(@Param("userId") Long userId);
```

**Rule 155**: Batch fetch related entities when dealing with collections (avoid N+1)
```java
// WRONG - N+1 queries
for (User user : users) {
    Address address = addressRepository.findById(user.getAddressId());  // N queries
}

// CORRECT - Batch fetch
List<Long> addressIds = users.stream().map(User::getAddressId).collect(Collectors.toList());
List<Address> addresses = addressRepository.findAllById(addressIds);  // 1 query
```

**Rule 156**: Use `findByIdWithAllRelations()` style methods instead of multiple queries
```java
// CORRECT
User user = userRepository.findByIdWithAllRelations(id, getClientId());

// WRONG
User user = userRepository.findById(id);
Address address = addressRepository.findById(user.getAddressId());
List<Permission> permissions = permissionRepository.findByUserId(user.getUserId());
```

**Rule 157**: Pre-fetch counts in batch queries instead of individual count queries per entity
```java
// Get all pickup location IDs
List<Long> pickupLocationIds = result.getContent().stream()
    .map(PickupLocation::getPickupLocationId)
    .collect(Collectors.toList());

// Batch fetch product counts (1 query instead of N)
List<Object[]> productCounts = productMappingRepository.countByPickupLocationIds(pickupLocationIds);
Map<Long, Integer> productCountMap = new HashMap<>();
for (Object[] row : productCounts) {
    productCountMap.put((Long) row[0], ((Number) row[1]).intValue());
}
```

**Rule 158**: Use Maps to store batch-fetched data for O(1) lookup
```java
Map<Long, Integer> productCountMap = new HashMap<>();
for (Object[] row : productCounts) {
    productCountMap.put((Long) row[0], ((Number) row[1]).intValue());
}

// Later, O(1) lookup
int count = productCountMap.getOrDefault(pickupLocationId, 0);
```

**Rule 159**: Filter by clientId in all queries for multi-tenant isolation
```java
User user = userRepository.findByIdAndClientId(id, getClientId());
```

**Rule 160**: Use pagination at database level, not in application
```java
// WRONG - fetches all, paginates in memory
List<User> allUsers = userRepository.findAll();
List<User> page = allUsers.subList(start, end);

// CORRECT - database-level pagination
Pageable pageable = PageRequest.of(0, pageSize);
Page<User> page = userRepository.findAll(pageable);
```

---

## Transaction Management Rules (161-166)

**Rule 161**: Keep transactions as short as possible
```java
@Transactional
public void createUser(UserRequestModel request) {
    // Keep this method focused on database operations only
    // Move complex calculations or external API calls outside transaction
}
```

**Rule 162**: Use `@Transactional(readOnly = true)` for read-only operations
```java
@Override
@Transactional(readOnly = true)
public UserResponseModel getUserById(long id) {
    return new UserResponseModel(userRepository.findById(id).orElseThrow());
}
```

**Rule 163**: Don't nest transactions unnecessarily
```java
// WRONG
@Transactional
public void createUser(UserRequestModel request) {
    createAddress(request);  // Also @Transactional - nested!
}

// CORRECT
@Transactional
public void createUser(UserRequestModel request) {
    createAddress(request);  // No @Transactional - participates in outer transaction
}
```

**Rule 164**: Async bulk operations must use `NOT_SUPPORTED` propagation to avoid transaction issues
```java
@Async
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public void bulkCreateUsersAsync(...) {
```

**Rule 165**: Helper methods called from transactional methods don't need their own `@Transactional`
```java
@Transactional
public void createUser(UserRequestModel request) {
    createUserAddress(request);  // No @Transactional needed
}

private void createUserAddress(UserRequestModel request) {
    // Participates in createUser's transaction
}
```

**Rule 166**: Only mark the outermost public method as transactional
```java
@Transactional  // Only here
public void createUser(UserRequestModel request) {
    createUserAddress(request);      // No annotation
    createUserPermissions(request);  // No annotation
    createUserGroups(request);       // No annotation
}
```

---

## Naming Convention Rules (167-176)

**Rule 167**: Service class names must end with "Service"
```java
public class UserService { }
public class AddressService { }
```

**Rule 168**: Repository fields should match entity name + "Repository"
```java
private final UserRepository userRepository;
private final AddressRepository addressRepository;
```

**Rule 169**: Request model parameters should end with "RequestModel" or "Request"
```java
public void createUser(UserRequestModel userRequestModel) { }
public void updateUser(UserRequestModel request) { }
```

**Rule 170**: Response model return types should end with "ResponseModel"
```java
public UserResponseModel getUserById(long id) { }
```

**Rule 171**: Boolean method names should start with "is", "has", or "validate"
```java
private boolean isValidEmail(String email) { }
private boolean hasPermission(Long userId, Long permissionId) { }
private void validateUserRequest(UserRequestModel request) { }
```

**Rule 172**: Create methods should start with "create" or "insert"
```java
public void createUser(UserRequestModel request) { }
public void insertUser(UserRequestModel request) { }
```

**Rule 173**: Update methods should start with "update" or "edit"
```java
public void updateUser(UserRequestModel request) { }
public void editUser(UserRequestModel request) { }
```

**Rule 174**: Read methods should start with "get", "find", or "fetch"
```java
public UserResponseModel getUserById(long id) { }
public List<UserResponseModel> findUsersByRole(String role) { }
public PaginationBaseResponseModel<UserResponseModel> fetchUsersInBatches(...) { }
```

**Rule 175**: Delete methods should start with "delete" or "toggle"
```java
public void deleteUser(long id) { }
public void toggleUser(long id) { }
```

**Rule 176**: Batch methods should include "bulk" or "batch" in name
```java
public void bulkCreateUsersAsync(...) { }
public BulkInsertResponseModel<Long> bulkCreateUsers(...) { }
public PaginationBaseResponseModel<UserResponseModel> fetchUsersInBatches(...) { }
```

---

## Comment Rules (177-181)

**Rule 177**: Use section separator comments for major sections
```java
// ==================== HELPER METHODS ====================

// ==================== ADDRESS HELPERS ====================

// ==================== PERMISSION HELPERS ====================
```

**Rule 178**: Standard sections: PUBLIC METHODS, HELPER METHODS
```java
// Public methods here

// ==================== HELPER METHODS ====================

// Private methods here
```

**Rule 179**: Don't comment obvious code
```java
// WRONG
// Set the user's first name
user.setFirstName(request.getFirstName());

// CORRECT (no comment needed - code is self-explanatory)
user.setFirstName(request.getFirstName());
```

**Rule 180**: Comment complex business logic or non-obvious behavior
```java
// Calculate packaging estimate only if product dimensions and quantity are available
// Uses greedy bin-packing algorithm to minimize package count
if (product != null && requestedQuantity != null && requestedQuantity > 0) {
    calculatePackagingEstimate(...);
}
```

**Rule 181**: Remove commented-out code before commit
```java
// WRONG - Don't leave commented code
// User oldUser = userRepository.findById(id);
// oldUser.setActive(false);

// CORRECT - Remove commented code entirely
```

---

## Import Organization Rules (182-184)

**Rule 182**: Group imports: Java standard library → Spring → Third-party → Application
```java
// Java standard library
import java.util.List;
import java.util.ArrayList;

// Spring
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Third-party
import org.slf4j.Logger;

// Application
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Services.BaseService;
```

**Rule 183**: Avoid wildcard imports except for static imports of constants
```java
// WRONG
import java.util.*;

// CORRECT
import java.util.List;
import java.util.ArrayList;

// OK for constants
import static com.example.SpringApi.Constants.UserConstants.*;
```

**Rule 184**: Remove unused imports
```java
// Remove any imports that are not used in the file
```

---

## Exception Message Rules (185-189)

**Rule 185**: Use constants from `ErrorMessages` class, don't hardcode messages
```java
// WRONG
throw new BadRequestException("User not found");

// CORRECT
throw new NotFoundException(ErrorMessages.UserErrorMessages.InvalidId);
```

**Rule 186**: Use constants from `SuccessMessages` class for logging
```java
// WRONG
userLogService.logData(getUserId(), "User created successfully", endpoint);

// CORRECT
userLogService.logData(getUserId(), 
    SuccessMessages.UserSuccessMessages.CreateUser + " " + savedUser.getUserId(),
    endpoint);
```

**Rule 187**: Use `String.format()` or concatenation to include dynamic values
```java
throw new NotFoundException(
    String.format(ErrorMessages.ProductErrorMessages.NotFoundWithId, productId));

// OR
throw new NotFoundException(
    ErrorMessages.UserErrorMessages.InvalidId + " - User ID: " + userId);
```

**Rule 188**: Provide specific error messages that help debugging
```java
// WRONG
throw new BadRequestException("Invalid input");

// CORRECT
throw new BadRequestException(
    "Invalid column name: " + filter.getColumn() + 
    ". Valid columns: " + String.join(", ", validColumns));
```

**Rule 189**: Create error message constants organized by entity
```java
// In ErrorMessages.java
public static class UserErrorMessages {
    public static final String InvalidId = "User not found with the provided ID";
    public static final String InvalidEmail = "User not found with the provided email";
    public static final String InvalidRequest = "User request cannot be null";
}
```

---

## Client Isolation Rules (190-193)

**Rule 190**: Always filter queries by `getClientId()` for multi-tenant isolation
```java
User user = userRepository.findByIdAndClientId(id, getClientId());
```

**Rule 191**: Validate that entities belong to current client before operations
```java
User user = userRepository.findByIdAndClientId(id, getClientId());
if (user == null) {
    throw new NotFoundException(ErrorMessages.UserErrorMessages.InvalidId);
}
```

**Rule 192**: Pass clientId to repositories when needed for filtering
```java
List<User> users = userRepository.findAllByClientId(getClientId());
```

**Rule 193**: Set clientId on new entities from security context, not request
```java
// WRONG - Security risk
user.setClientId(request.getClientId());

// CORRECT - From security context
user.setClientId(getClientId());
```

---

## Permission/Authorization Rules (194-195)

**Rule 194**: Permission validation is handled at controller/filter level, not in services
```java
// Services don't check permissions
// Controllers and security filters handle authorization
```

**Rule 195**: Services assume caller has already been authorized
```java
@Override
public void deleteUser(long id) {
    // No permission checking here - controller already validated
    User user = userRepository.findById(id).orElseThrow();
    user.setIsDeleted(true);
    userRepository.save(user);
}
```

---

## Testing Support Rules (196-200)

**Rule 196**: Provide synchronous versions of async operations for testing
```java
// Async version for production
@Async
public void bulkCreateUsersAsync(...) { }

// Sync version for testing
@Transactional
public BulkInsertResponseModel<Long> bulkCreateUsers(...) { }
```

**Rule 197**: Use `protected` visibility for helpers that tests might need
```java
protected void createUser(UserRequestModel request, boolean sendEmail) {
    // Tests can call this
}
```

**Rule 198**: Avoid private final fields that can't be mocked (allow constructor injection)
```java
// CORRECT - Can be mocked via constructor
private final UserRepository userRepository;

@Autowired
public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
}
```

**Rule 199**: Keep business logic in testable helper methods
```java
// Extract to helper for testing
private boolean isEligibleForDiscount(User user) {
    return user.getAge() > 65 || user.isStudent();
}
```

**Rule 200**: Avoid complex logic in constructors
```java
// WRONG
@Autowired
public UserService(UserRepository userRepository) {
    super();
    this.userRepository = userRepository;
    this.cache = initializeCache();  // Complex logic in constructor
}

// CORRECT
@Autowired
public UserService(UserRepository userRepository) {
    super();
    this.userRepository = userRepository;
}
```

---

## Code Quality Rules (201-210)

**Rule 201**: Methods should do one thing well (Single Responsibility)
```java
// WRONG
public void createUserAndSendEmail(UserRequestModel request) {
    // Creates user AND sends email - two responsibilities
}

// CORRECT
public void createUser(UserRequestModel request) {
    // Only creates user
    sendUserConfirmationEmail(savedUser);  // Delegate email sending
}
```

**Rule 202**: Avoid code duplication - extract to helper methods
```java
// WRONG - duplicated validation
public void createUser(UserRequestModel request) {
    if (request == null) { throw ... }
    if (request.getEmail() == null) { throw ... }
}

public void updateUser(UserRequestModel request) {
    if (request == null) { throw ... }
    if (request.getEmail() == null) { throw ... }
}

// CORRECT - extracted to helper
private void validateUserRequest(UserRequestModel request) {
    if (request == null) { throw ... }
    if (request.getEmail() == null) { throw ... }
}
```

**Rule 203**: Keep methods under 50 lines when possible
```java
// If a method exceeds 50 lines, consider extracting helper methods
```

**Rule 204**: Avoid deep nesting (max 3 levels)
```java
// WRONG - 4 levels
if (user != null) {
    if (user.isActive()) {
        if (user.hasPermission()) {
            if (user.isVerified()) {
                // Code here
            }
        }
    }
}

// CORRECT - early returns
if (user == null) return;
if (!user.isActive()) return;
if (!user.hasPermission()) return;
if (!user.isVerified()) return;
// Code here
```

**Rule 205**: Use early returns to reduce nesting
```java
// WRONG
public void processUser(User user) {
    if (user != null) {
        if (user.isActive()) {
            // Process user
        }
    }
}

// CORRECT
public void processUser(User user) {
    if (user == null) return;
    if (!user.isActive()) return;
    
    // Process user
}
```

**Rule 206**: Use meaningful variable names
```java
// WRONG
int x = users.size();
String s = user.getFirstName();

// CORRECT
int userCount = users.size();
String firstName = user.getFirstName();
```

**Rule 207**: Prefer clarity over cleverness
```java
// WRONG - too clever
return users.stream().filter(u->u.getAge()>18).map(User::getName).collect(Collectors.toList());

// CORRECT - clear
return users.stream()
    .filter(user -> user.getAge() > 18)
    .map(User::getName)
    .collect(Collectors.toList());
```

**Rule 208**: Use Optional only when null is a valid business case
```java
// CORRECT - null has business meaning
public Optional<User> findActiveUserByEmail(String email) {
    return userRepository.findByEmail(email)
        .filter(User::isActive);
}

// WRONG - don't use Optional for required values
public Optional<User> getUserById(Long id) {
    return userRepository.findById(id);  // Should throw if not found
}
```

**Rule 209**: Validate all external inputs
```java
public void createUser(UserRequestModel request) {
    // Validate all inputs before processing
    if (request == null) { throw ... }
    if (request.getEmail() == null) { throw ... }
    if (request.getFirstName() == null) { throw ... }
}
```

**Rule 210**: Use constants for magic numbers/strings
```java
// WRONG
if (user.getAge() < 18) { }
if (status.equals("ACTIVE")) { }

// CORRECT
if (user.getAge() < UserConstants.MINIMUM_AGE) { }
if (UserStatus.ACTIVE.equals(status)) { }
```

---

## Error and Success Message Rules (211-220)

**Rule 211**: All exceptions must be thrown with error messages
```java
// WRONG
throw new BadRequestException();

// CORRECT
throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidRequest);
```

**Rule 212**: Never hardcode error messages in exception constructors
```java
// WRONG
throw new BadRequestException("User not found");

// CORRECT
throw new NotFoundException(ErrorMessages.UserErrorMessages.InvalidId);
```

**Rule 213**: All error messages must be defined in `ErrorMessages.java` class
```java
// In ErrorMessages.java
public static class UserErrorMessages {
    public static final String InvalidId = "User not found with the provided ID";
    public static final String InvalidEmail = "Invalid email address format";
}
```

**Rule 214**: Error messages should be organized by entity/feature
```java
// In ErrorMessages.java
public static class UserErrorMessages { }
public static class AddressErrorMessages { }
public static class ProductErrorMessages { }
public static class CommonErrorMessages { }
```

**Rule 215**: Never hardcode success messages in log statements
```java
// WRONG
userLogService.logData(getUserId(), "User created successfully", endpoint);

// CORRECT
userLogService.logData(getUserId(), 
    SuccessMessages.UserSuccessMessages.CreateUser + " " + savedUser.getUserId(),
    endpoint);
```

**Rule 216**: All success messages must be defined in `SuccessMessages.java` class
```java
// In SuccessMessages.java
public static class UserSuccessMessages {
    public static final String CreateUser = "User created successfully with ID:";
    public static final String UpdateUser = "User updated successfully with ID:";
}
```

**Rule 217**: Success messages should be organized by entity/feature
```java
// In SuccessMessages.java
public static class UserSuccessMessages { }
public static class AddressSuccessMessages { }
public static class ProductSuccessMessages { }
```

**Rule 218**: Use descriptive constant names that reflect the message purpose
```java
// GOOD
public static final String CreateUser = "User created successfully with ID:";
public static final String InvalidEmailFormat = "Email format is invalid";

// BAD
public static final String MSG1 = "User created successfully with ID:";
public static final String ERR = "Email format is invalid";
```

**Rule 219**: Error messages should be user-friendly but informative
```java
// GOOD
public static final String InvalidEmail = "User not found with the provided email address";

// BAD (too technical)
public static final String InvalidEmail = "SELECT query returned 0 rows for email parameter";

// BAD (too vague)
public static final String InvalidEmail = "Error";
```

**Rule 220**: Include entity identifiers in error messages using concatenation or String.format()
```java
// Concatenation
throw new NotFoundException(
    ErrorMessages.UserErrorMessages.InvalidId + " - User ID: " + userId);

// String.format
throw new NotFoundException(
    String.format(ErrorMessages.ProductErrorMessages.NotFoundWithId, productId));
```

---

## Mandatory Logging Rules (221-230)

**Rule 221**: Every insert operation must be logged to the database via `userLogService.logData()`
```java
@Override
@Transactional
public void createUser(UserRequestModel request) {
    User savedUser = userRepository.save(user);
    
    // MANDATORY LOGGING
    userLogService.logData(getUserId(),
        SuccessMessages.UserSuccessMessages.CreateUser + " " + savedUser.getUserId(),
        ApiRoutes.UserSubRoute.CREATE_USER);
}
```

**Rule 222**: Every update operation must be logged to the database via `userLogService.logData()`
```java
@Override
@Transactional
public void updateUser(UserRequestModel request) {
    User updatedUser = userRepository.save(user);
    
    // MANDATORY LOGGING
    userLogService.logData(getUserId(),
        SuccessMessages.UserSuccessMessages.UpdateUser + " " + updatedUser.getUserId(),
        ApiRoutes.UserSubRoute.UPDATE_USER);
}
```

**Rule 223**: Every toggle operation must be logged to the database via `userLogService.logData()`
```java
@Override
@Transactional
public void toggleUser(long id) {
    user.setIsDeleted(!user.getIsDeleted());
    userRepository.save(user);
    
    // MANDATORY LOGGING
    userLogService.logData(getUserId(),
        SuccessMessages.UserSuccessMessages.ToggleUser + " " + user.getUserId(),
        ApiRoutes.UserSubRoute.TOGGLE_USER);
}
```

**Rule 224**: Bulk operations require only one log entry (not per item)
```java
// WRONG - logs for each item
for (UserRequestModel userRequest : users) {
    createUser(userRequest, getUser(), true);  // Logs each user
}

// CORRECT - one log for entire bulk operation
for (UserRequestModel userRequest : users) {
    createUser(userRequest, requestingUserLoginName, false);  // No individual logging
}
userLogService.logDataWithContext(requestingUserId, requestingUserLoginName, requestingClientId,
    SuccessMessages.UserSuccessMessages.CreateUser + " (Bulk: " + successCount + " succeeded, " + failureCount + " failed)",
    ApiRoutes.UsersSubRoute.BULK_CREATE_USER);
```

**Rule 225**: Bulk log messages must include success and failure counts
```java
userLogService.logDataWithContext(requestingUserId, requestingUserLoginName, requestingClientId,
    SuccessMessages.UserSuccessMessages.CreateUser + 
        " (Bulk: " + successCount + " succeeded, " + failureCount + " failed)",
    ApiRoutes.UsersSubRoute.BULK_CREATE_USER);
```

**Rule 226**: Use `userLogService.logDataWithContext()` for async operations
```java
@Async
public void bulkCreateUsersAsync(...) {
    // Don't use getUser() or getUserId() in async methods
    userLogService.logDataWithContext(
        requestingUserId,           // Passed as parameter
        requestingUserLoginName,    // Passed as parameter
        requestingClientId,         // Passed as parameter
        message,
        endpoint);
}
```

**Rule 227**: Read-only operations (get/fetch) do not require logging
```java
@Override
@Transactional(readOnly = true)
public UserResponseModel getUserById(long id) {
    // No logging needed for read operations
    return new UserResponseModel(userRepository.findById(id).orElseThrow());
}
```

**Rule 228**: Log entries must include the entity ID in the message
```java
userLogService.logData(getUserId(),
    SuccessMessages.UserSuccessMessages.CreateUser + " " + savedUser.getUserId(),  // Include ID
    ApiRoutes.UserSubRoute.CREATE_USER);
```

**Rule 229**: Log entries must include the API route as the third parameter
```java
userLogService.logData(getUserId(),
    message,
    ApiRoutes.UserSubRoute.CREATE_USER);  // API route
```

**Rule 230**: Failed operations should not be logged (exceptions handle those)
```java
// WRONG
try {
    createUser(request);
} catch (BadRequestException e) {
    userLogService.logData(getUserId(), "Failed to create user", endpoint);  // Don't log failures
    throw e;
}

// CORRECT
try {
    createUser(request);
    // Success is logged inside createUser()
} catch (BadRequestException e) {
    throw e;  // Exception is enough, no need to log
}
```

---

## SonarQube Code Quality Rules

### Cognitive Complexity (231-235)

**Rule 231**: Keep method cognitive complexity under 15
```java
// If SonarQube reports complexity > 15, refactor the method
// Break into smaller helper methods
```

**Rule 232**: Break complex methods into smaller helper methods
```java
// WRONG - one large complex method
public void processUser(User user) {
    // 100 lines of complex logic
}

// CORRECT - broken into helpers
public void processUser(User user) {
    validateUser(user);
    enrichUserData(user);
    saveUser(user);
    notifyUser(user);
}
```

**Rule 233**: Avoid deeply nested if-else statements
```java
// WRONG
if (condition1) {
    if (condition2) {
        if (condition3) {
            if (condition4) {
                // Code
            }
        }
    }
}

// CORRECT - use early returns
if (!condition1) return;
if (!condition2) return;
if (!condition3) return;
if (!condition4) return;
// Code
```

**Rule 234**: Use early returns to reduce complexity
```java
public void processUser(User user) {
    if (user == null) return;
    if (!user.isActive()) return;
    if (!user.isVerified()) return;
    
    // Process user
}
```

**Rule 235**: Extract complex conditions into well-named boolean variables
```java
// WRONG
if (user.getAge() > 18 && user.isVerified() && !user.isBlocked() && user.hasPermission("WRITE")) {
    // Code
}

// CORRECT
boolean isEligible = user.getAge() > 18 && user.isVerified();
boolean canWrite = !user.isBlocked() && user.hasPermission("WRITE");
if (isEligible && canWrite) {
    // Code
}
```

---

### Code Smells (236-242)

**Rule 236**: Remove all unused imports
```java
// Remove any imports that aren't used in the file
```

**Rule 237**: Remove all unused private methods
```java
// If a private method is never called, remove it
```

**Rule 238**: Remove all unused local variables
```java
// WRONG
public void processUser(User user) {
    String unusedVariable = "test";
    // Variable is never used
}

// CORRECT
public void processUser(User user) {
    // Only declare variables you use
}
```

**Rule 239**: Remove all commented-out code
```java
// WRONG
public void createUser(UserRequestModel request) {
    // User oldUser = findOldUser();
    // oldUser.setActive(false);
    User newUser = new User(request);
}

// CORRECT
public void createUser(UserRequestModel request) {
    User newUser = new User(request);
}
```

**Rule 240**: Avoid empty catch blocks - at minimum log the exception
```java
// WRONG
try {
    processUser(user);
} catch (Exception e) {
    // Empty catch block
}

// CORRECT
try {
    processUser(user);
} catch (Exception e) {
    logger.warn("Failed to process user: {}", user.getUserId(), e);
    // Or rethrow, or handle appropriately
}
```

**Rule 241**: Close resources properly (use try-with-resources)
```java
// WRONG
InputStream input = new FileInputStream(file);
// ... use input ...
input.close();  // Might not be called if exception occurs

// CORRECT
try (InputStream input = new FileInputStream(file)) {
    // ... use input ...
}  // Automatically closed
```

**Rule 242**: Don't use printStackTrace() - use proper logging instead
```java
// WRONG
try {
    processUser(user);
} catch (Exception e) {
    e.printStackTrace();
}

// CORRECT
try {
    processUser(user);
} catch (Exception e) {
    logger.error("Error processing user", e);
}
```

---

### Bug Patterns (243-250)

**Rule 243**: Always check for null before dereferencing objects
```java
// WRONG
String name = user.getFirstName();

// CORRECT
if (user != null) {
    String name = user.getFirstName();
}
```

**Rule 244**: Use `.equals()` for string comparison, not `==`
```java
// WRONG
if (user.getRole() == "ADMIN") { }

// CORRECT
if ("ADMIN".equals(user.getRole())) { }  // Null-safe
```

**Rule 245**: Don't return null from methods - use Optional or empty collections
```java
// WRONG
public List<User> getUsers() {
    return null;
}

// CORRECT - empty collection
public List<User> getUsers() {
    return Collections.emptyList();
}

// CORRECT - Optional for single values
public Optional<User> findUser(Long id) {
    return userRepository.findById(id);
}
```

**Rule 246**: Avoid NullPointerException by validating inputs
```java
public void processUser(User user) {
    if (user == null) {
        throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidRequest);
    }
    // Now safe to use user
}
```

**Rule 247**: Don't catch generic Exception unless necessary (catch specific exceptions)
```java
// WRONG
try {
    processUser(user);
} catch (Exception e) {  // Too generic
    // ...
}

// CORRECT
try {
    processUser(user);
} catch (BadRequestException e) {
    // Handle validation errors
} catch (NotFoundException e) {
    // Handle not found errors
}
```

**Rule 248**: Don't ignore InterruptedException - handle or propagate it
```java
// WRONG
try {
    Thread.sleep(1000);
} catch (InterruptedException e) {
    // Ignored
}

// CORRECT
try {
    Thread.sleep(1000);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();  // Restore interrupt status
    throw new RuntimeException(e);
}
```

**Rule 249**: Use `Long.valueOf()` instead of `new Long()` (deprecated constructor)
```java
// WRONG
Long id = new Long(123);

// CORRECT
Long id = Long.valueOf(123);
// OR
Long id = 123L;
```

**Rule 250**: Use `Boolean.TRUE.equals()` for null-safe boolean checks
```java
// WRONG
if (user.getIsActive() == true) { }  // NPE if null

// CORRECT
if (Boolean.TRUE.equals(user.getIsActive())) { }  // Null-safe
```

---

### Security Hotspots (251-256)

**Rule 251**: Never log sensitive data (passwords, tokens, API keys)
```java
// WRONG
logger.info("User password: {}", user.getPassword());
logger.info("API Key: {}", apiKey);

// CORRECT
logger.info("User authenticated: {}", user.getEmail());
logger.info("API Key configured: {}", apiKey != null);
```

**Rule 252**: Validate all user inputs before using in queries
```java
public void searchUsers(String searchTerm) {
    // Validate input
    if (searchTerm == null || searchTerm.length() > 100) {
        throw new BadRequestException(ErrorMessages.CommonErrorMessages.InvalidInput);
    }
    // Now safe to use
}
```

**Rule 253**: Use parameterized queries (JPA handles this, but verify)
```java
// JPA handles parameterization automatically
@Query("SELECT u FROM User u WHERE u.email = :email")
User findByEmail(@Param("email") String email);
```

**Rule 254**: Don't expose stack traces to end users
```java
// WRONG - in controller
@ExceptionHandler(Exception.class)
public ResponseEntity<String> handleException(Exception e) {
    return ResponseEntity.status(500).body(e.getStackTrace().toString());
}

// CORRECT
@ExceptionHandler(Exception.class)
public ResponseEntity<String> handleException(Exception e) {
    logger.error("Internal server error", e);
    return ResponseEntity.status(500).body("An error occurred");
}
```

**Rule 255**: Validate file uploads before processing
```java
public void uploadProfilePicture(String base64Image) {
    if (base64Image == null || base64Image.isEmpty()) {
        throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidProfilePicture);
    }
    // Validate size, format, etc.
}
```

**Rule 256**: Set appropriate access modifiers (don't make everything public)
```java
// Use most restrictive access possible
private void internalHelper() { }      // Only used in this class
protected void helperForSubclass() { } // Used by subclasses
public void publicApi() { }            // Part of public API
```

---

### Maintainability (257-263)

**Rule 257**: Methods should not have more than 7 parameters
```java
// WRONG
public void createUser(String firstName, String lastName, String email, 
                       String phone, String address, Integer age, Boolean active) { }

// CORRECT - use a request model
public void createUser(UserRequestModel request) { }
```

**Rule 258**: Classes should not have more than 10 dependencies
```java
// If a service has 10+ dependencies, consider splitting it
```

**Rule 259**: Avoid duplicate string literals (use constants)
```java
// WRONG
if ("ACTIVE".equals(status)) { }
logger.info("User status is ACTIVE");

// CORRECT
private static final String STATUS_ACTIVE = "ACTIVE";
if (STATUS_ACTIVE.equals(status)) { }
logger.info("User status is {}", STATUS_ACTIVE);
```

**Rule 260**: Boolean expressions should not be gratuitous
```java
// WRONG
if (isActive == true) { }
if (isDeleted == false) { }

// CORRECT
if (isActive) { }
if (!isDeleted) { }
```

**Rule 261**: Don't use negation in if conditions when possible (use positive logic)
```java
// LESS READABLE
if (!user.isNotActive()) { }

// MORE READABLE
if (user.isActive()) { }
```

**Rule 262**: Switch statements should have a default case
```java
switch (status) {
    case ACTIVE:
        // ...
        break;
    case INACTIVE:
        // ...
        break;
    default:
        throw new BadRequestException(ErrorMessages.CommonErrorMessages.InvalidStatus);
}
```

**Rule 263**: Avoid too many return statements (max 3-4 per method)
```java
// If a method has 5+ return statements, refactor it
```

---

### Reliability (264-269)

**Rule 264**: Always close streams, connections, and files
```java
try (InputStream input = new FileInputStream(file)) {
    // Use stream
}  // Automatically closed
```

**Rule 265**: Handle all checked exceptions appropriately
```java
try {
    processUser(user);
} catch (IOException e) {
    logger.error("IO error processing user", e);
    throw new RuntimeException("Failed to process user", e);
}
```

**Rule 266**: Don't ignore return values of methods
```java
// WRONG
list.add(user);  // Ignoring return value

// CORRECT
boolean added = list.add(user);
if (!added) {
    logger.warn("Failed to add user to list");
}
```

**Rule 267**: Synchronize access to mutable static fields
```java
private static int counter;

public synchronized void incrementCounter() {
    counter++;
}
```

**Rule 268**: Don't use ThreadLocal with ExecutorService
```java
// ThreadLocal variables may not be cleared in thread pools
// Avoid using ThreadLocal with @Async methods
```

**Rule 269**: Avoid catching Throwable or Error
```java
// WRONG
try {
    processUser(user);
} catch (Throwable t) {  // Too broad
    // ...
}

// CORRECT
try {
    processUser(user);
} catch (Exception e) {  // Catch Exception, not Throwable
    // ...
}
```

---

### Performance (270-276)

**Rule 270**: Don't create unnecessary objects in loops
```java
// WRONG
for (int i = 0; i < 1000; i++) {
    String prefix = new String("User");  // Creates 1000 objects
    logger.info(prefix + i);
}

// CORRECT
String prefix = "User";
for (int i = 0; i < 1000; i++) {
    logger.info(prefix + i);
}
```

**Rule 271**: Use StringBuilder for string concatenation in loops
```java
// WRONG
String result = "";
for (String name : names) {
    result += name + ", ";  // Creates many intermediate String objects
}

// CORRECT
StringBuilder result = new StringBuilder();
for (String name : names) {
    result.append(name).append(", ");
}
```

**Rule 272**: Avoid calling expensive methods repeatedly (cache results)
```java
// WRONG
for (User user : users) {
    if (user.getAge() > calculateRetirementAge()) {  // Calculated each iteration
        // ...
    }
}

// CORRECT
int retirementAge = calculateRetirementAge();  // Calculate once
for (User user : users) {
    if (user.getAge() > retirementAge) {
        // ...
    }
}
```

**Rule 273**: Use primitive types instead of wrapper classes when possible
```java
// LESS EFFICIENT
List<Integer> numbers = new ArrayList<>();

// MORE EFFICIENT (if you don't need null)
int[] numbers = new int[100];
```

**Rule 274**: Close resources to prevent memory leaks
```java
try (Connection conn = dataSource.getConnection()) {
    // Use connection
}  // Automatically closed, preventing leak
```

**Rule 275**: Use lazy initialization for expensive objects
```java
private ExpensiveObject expensiveObject;

private ExpensiveObject getExpensiveObject() {
    if (expensiveObject == null) {
        expensiveObject = new ExpensiveObject();
    }
    return expensiveObject;
}
```

**Rule 276**: Prefer `List.of()` or `Collections.emptyList()` for immutable empty lists
```java
// LESS EFFICIENT
return new ArrayList<>();

// MORE EFFICIENT
return Collections.emptyList();
// OR
return List.of();
```

---

### Code Style (277-284)

**Rule 277**: Use Java naming conventions
```java
// Variables and methods: camelCase
int userCount;
public void createUser() { }

// Classes: PascalCase
public class UserService { }

// Constants: UPPER_SNAKE_CASE
public static final String DEFAULT_ROLE = "USER";
```

**Rule 278**: Don't use abbreviations in names unless widely understood
```java
// WRONG
int usrCnt;
String addr;

// CORRECT
int userCount;
String address;

// OK - widely understood
int httpCode;
String url;
```

**Rule 279**: Constants should be UPPER_SNAKE_CASE
```java
public static final int MAX_RETRY_COUNT = 3;
public static final String DEFAULT_STATUS = "ACTIVE";
```

**Rule 280**: Package names should be lowercase
```java
package com.example.springapi.services;
```

**Rule 281**: Avoid single-letter variable names except in loops
```java
// WRONG
String n = user.getName();

// CORRECT
String name = user.getName();

// OK in loops
for (int i = 0; i < count; i++) { }
```

**Rule 282**: Method names should be verbs
```java
public void createUser() { }
public void updateUser() { }
public User getUser() { }
public boolean isActive() { }
```

**Rule 283**: Class names should be nouns
```java
public class UserService { }
public class AddressRepository { }
public class UserRequestModel { }
```

**Rule 284**: Interface names should describe behavior
```java
public interface IUserSubTranslator { }
public interface Serializable { }
```

---

### Testing Considerations (285-290)

**Rule 285**: Make methods package-private or protected if they need to be tested
```java
// Can be tested from test classes in same package
void processUser(User user) { }

// Can be tested from test subclasses
protected void validateUser(User user) { }
```

**Rule 286**: Avoid complex logic in constructors
```java
// WRONG
@Autowired
public UserService(UserRepository userRepository) {
    super();
    this.userRepository = userRepository;
    this.cache = initializeComplexCache();  // Hard to test
}

// CORRECT
@Autowired
public UserService(UserRepository userRepository) {
    super();
    this.userRepository = userRepository;
}
```

**Rule 287**: Prefer dependency injection over hardcoded dependencies
```java
// WRONG
public class UserService {
    private UserRepository userRepository = new UserRepositoryImpl();
}

// CORRECT
public class UserService {
    private final UserRepository userRepository;
    
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

**Rule 288**: Don't use static methods for business logic
```java
// WRONG - hard to test/mock
public static void processUser(User user) { }

// CORRECT - instance method
public void processUser(User user) { }
```

**Rule 289**: Keep side effects isolated and testable
```java
// Extract side effects into separate methods
private void sendEmail(User user) { }
private void logAction(String action) { }
```

**Rule 290**: Use constructor injection instead of field injection
```java
// LESS TESTABLE - field injection
@Autowired
private UserRepository userRepository;

// MORE TESTABLE - constructor injection
private final UserRepository userRepository;

@Autowired
public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
}
```

---

### Documentation (291-295)

**Rule 291**: JavaDoc should not contain TODO tags (use IDE task management)
```java
// WRONG
/**
 * Creates a user
 * TODO: Add validation
 */

// CORRECT
/**
 * Creates a user with full validation
 */
// Use IDE's task system for TODOs
```

**Rule 292**: Update JavaDoc when method signatures change
```java
// If method parameters change, update the @param tags
```

**Rule 293**: Remove obsolete comments
```java
// WRONG
// This method used to send emails but now it doesn't
public void createUser(UserRequestModel request) {
    // Code that doesn't send emails
}

// CORRECT
public void createUser(UserRequestModel request) {
    // No obsolete comment
}
```

**Rule 294**: Don't comment what the code does, comment why
```java
// WRONG
// Loop through users
for (User user : users) {
    // Set active to true
    user.setActive(true);
}

// CORRECT
// Reactivate all users after system maintenance
for (User user : users) {
    user.setActive(true);
}
```

**Rule 295**: Keep comments in sync with code
```java
// If code changes, update the comments
```

---

### Exception Handling Best Practices (296-300)

**Rule 296**: Don't catch Exception unless you have a specific reason
```java
// WRONG
try {
    processUser(user);
} catch (Exception e) {  // Too generic
    // ...
}

// CORRECT
try {
    processUser(user);
} catch (BadRequestException | NotFoundException e) {
    // Handle specific exceptions
}
```

**Rule 297**: Always provide context in exception messages
```java
// WRONG
throw new NotFoundException("Not found");

// CORRECT
throw new NotFoundException(
    ErrorMessages.UserErrorMessages.InvalidId + " - User ID: " + userId);
```

**Rule 298**: Preserve the exception stack trace when rethrowing
```java
// WRONG
try {
    processUser(user);
} catch (Exception e) {
    throw new RuntimeException("Error processing user");  // Lost stack trace
}

// CORRECT
try {
    processUser(user);
} catch (Exception e) {
    throw new RuntimeException("Error processing user", e);  // Preserves stack trace
}
```

**Rule 299**: Use try-with-resources for AutoCloseable objects
```java
try (InputStream input = new FileInputStream(file);
     OutputStream output = new FileOutputStream(outFile)) {
    // Use streams
}  // Automatically closed even if exception occurs
```

**Rule 300**: Create custom exceptions only when necessary (use existing BadRequestException, NotFoundException)
```java
// Use existing exceptions
throw new BadRequestException(ErrorMessages.UserErrorMessages.InvalidRequest);
throw new NotFoundException(ErrorMessages.UserErrorMessages.InvalidId);

// Don't create custom exceptions unless there's a specific need
```

---

## CRITICAL REMINDERS

1. **NEVER hardcode error or success messages** - Always use ErrorMessages and SuccessMessages constants
2. **ALWAYS log insert/update/toggle operations** - Mandatory logging for all data modifications
3. **ONE log entry for bulk operations** - Don't log each item individually
4. **NEVER use security context (getUser/getUserId/getClientId) in @Async methods** - Capture context before async call
5. **ALWAYS validate inputs before database operations** - Follow validation order
6. **ALWAYS filter by clientId** - Multi-tenant isolation is critical
7. **ALWAYS convert entities to response models** - Never return entities directly
8. **ALWAYS use @Transactional correctly** - readOnly for reads, NOT_SUPPORTED for async bulk
9. **ALWAYS follow method organization** - CRUD → Batch → Helpers
10. **ALWAYS follow naming conventions** - Be consistent with existing code

---

## When Making Changes

1. Read ALL relevant rules in this document
2. Verify every change against the rules
3. Check for SonarQube violations
4. Ensure consistency with existing services
5. Test your changes
6. Document what you changed and why

**Remember: Consistency and quality are more important than speed. Take time to follow all rules correctly.**