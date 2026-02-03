package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.*;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.FilterCondition;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base test class providing common helper methods and constants for unit tests.
 * 
 * This class should be extended by all service test classes to ensure
 * consistent
 * test data creation and reduce code duplication across the test suite.
 * 
 * Features:
 * - Common test constants (IDs, names, etc.)
 * - Factory methods for creating test entities (User, Client, Address, etc.)
 * - Reusable test data builders
 * 
 * Usage:
 * ```java
 * class MyServiceTest extends BaseTest {
 * @BeforeEach
 * void setUp() {
 *   testUser = createTestUser();
 *   testClient = createTestClient();
 * }
 * }
 * ```
 *
 * Test method naming convention: {@code methodName_Scenario_ExpectedOutcome}
 * (e.g. {@code createLead_Success}, {@code getLeadById_NotFound_ThrowsNotFoundException}).
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
public abstract class BaseTest {

    // ==================== COMMON TEST CONSTANTS ====================

    protected static final Long DEFAULT_ADDRESS_ID = 1L;
    protected static final Long DEFAULT_USER_ID = 1L;
    protected static final Long DEFAULT_CLIENT_ID = 100L;
    protected static final String DEFAULT_ADDRESS_TYPE = "HOME";
    protected static final String DEFAULT_STREET_ADDRESS = "123 Main St";
    protected static final String DEFAULT_CITY = "New York";
    protected static final String DEFAULT_STATE = "NY";
    protected static final String DEFAULT_POSTAL_CODE = "10001";
    protected static final String DEFAULT_COUNTRY = "USA";
    protected static final String DEFAULT_CREATED_USER = "admin";
    protected static final String DEFAULT_LOGIN_NAME = "testuser";
    protected static final String DEFAULT_EMAIL = "test@example.com";
    protected static final String DEFAULT_FIRST_NAME = "Test";
    protected static final String DEFAULT_LAST_NAME = "User";
    protected static final String DEFAULT_CLIENT_NAME = "Test Client";
    protected static final String DEFAULT_CLIENT_DESCRIPTION = "Test Client Description";
    protected static final String DEFAULT_SUPPORT_EMAIL = "support@testclient.com";
    protected static final String DEFAULT_WEBSITE = "https://testclient.com";

    // Lead Constants
    protected static final Long DEFAULT_LEAD_ID = 1L;
    protected static final String DEFAULT_PHONE = "1234567890";
    protected static final String DEFAULT_LEAD_STATUS = "Not Contacted";
    protected static final String DEFAULT_COMPANY = "Test Company";
    protected static final int DEFAULT_COMPANY_SIZE = 50;
    protected static final Long DEFAULT_CREATED_BY_ID = 1L;
    protected static final Long DEFAULT_ASSIGNED_AGENT_ID = 2L;

    // Payment Constants
    protected static final Long DEFAULT_PAYMENT_ID = 1L;
    protected static final BigDecimal DEFAULT_AMOUNT = new BigDecimal("1000.00");
    protected static final String DEFAULT_PAYMENT_METHOD = "RAZORPAY";
    protected static final String DEFAULT_PAYMENT_STATUS = "PENDING";

    // PurchaseOrder Constants
    protected static final Long DEFAULT_PURCHASE_ORDER_ID = 1L;
    protected static final String DEFAULT_PO_STATUS = "DRAFT";
    protected static final BigDecimal DEFAULT_SUBTOTAL = new BigDecimal("5000.00");
    protected static final BigDecimal DEFAULT_TAX = new BigDecimal("500.00");
    protected static final BigDecimal DEFAULT_SHIPPING_CHARGE = new BigDecimal("100.00");
    protected static final BigDecimal DEFAULT_TOTAL = new BigDecimal("5600.00");

    // Shipment Constants
    protected static final Long DEFAULT_SHIPMENT_ID = 1L;
    protected static final String DEFAULT_SHIPMENT_STATUS = "PENDING";
    protected static final BigDecimal DEFAULT_WEIGHT = new BigDecimal("10.00");
    protected static final String DEFAULT_TRACKING_NUMBER = "TRK123456";

    // Todo Constants
    protected static final Long DEFAULT_TODO_ID = 1L;
    protected static final String DEFAULT_TODO_TITLE = "Test Todo";
    protected static final String DEFAULT_TODO_DESCRIPTION = "This is a test todo";
    protected static final Boolean DEFAULT_TODO_COMPLETED = false;

    protected static final Long DEFAULT_GOOGLE_CRED_ID = 100L;

    // UserGroup Constants
    protected static final Long DEFAULT_USER_GROUP_ID = 1L;
    protected static final String DEFAULT_USER_GROUP_NAME = "Test User Group";
    protected static final String DEFAULT_USER_GROUP_DESCRIPTION = "Test Description";

    // UserLog Constants
    protected static final Long DEFAULT_USER_LOG_ID = 1L;
    protected static final String DEFAULT_ACTION = "LOGIN";
    protected static final String DEFAULT_LOG_DETAILS = "User logged in successfully";

    // Package Constants
    protected static final Long DEFAULT_PACKAGE_ID = 1L;
    protected static final String DEFAULT_PACKAGE_NAME = "Test Package";
    protected static final BigDecimal DEFAULT_PACKAGE_PRICE = new BigDecimal("999.99");

    // Promo Constants
    protected static final Long DEFAULT_PROMO_ID = 1L;
    protected static final String DEFAULT_PROMO_CODE = "PROMO100";
    protected static final Integer DEFAULT_DISCOUNT_PERCENTAGE = 10;
    protected static final BigDecimal DEFAULT_MIN_CART_VALUE = new BigDecimal("500.00");

    // PickupLocation Constants
    protected static final Long DEFAULT_PICKUP_LOCATION_ID = 1L;
    protected static final String DEFAULT_LOCATION_NAME = "Main Pickup";
    protected static final String DEFAULT_LOCATION_ADDRESS = "123 Pickup St";

    // Product Constants
    protected static final Long DEFAULT_PRODUCT_ID = 1L;
    protected static final String DEFAULT_PRODUCT_NAME = "Test Product";
    protected static final BigDecimal DEFAULT_PRODUCT_PRICE = new BigDecimal("599.99");
    protected static final Integer DEFAULT_STOCK_QUANTITY = 100;

    // ProductReview Constants
    protected static final Long DEFAULT_REVIEW_ID = 1L;
    protected static final Long DEFAULT_MESSAGE_ID = 1L;
    protected static final Integer DEFAULT_RATING = 5;
    protected static final String DEFAULT_REVIEW_TEXT = "Excellent product!";

    // ==================== USER FACTORY METHODS ====================

    /**
     * Creates a test User with default values.
     * 
     * @return User entity with default test values
     */
    protected User createTestUser() {
        return createTestUser(DEFAULT_USER_ID);
    }

    /**
     * Creates a test User with a specific ID.
     * 
     * @param userId The user ID to set
     * @return User entity with specified ID and default values
     */
    protected User createTestUser(Long userId) {
        return createTestUser(userId, DEFAULT_LOGIN_NAME, DEFAULT_EMAIL);
    }

    /**
     * Creates a fully customized test User.
     * 
     * @param userId    The user ID
     * @param loginName The login name
     * @param email     The email address
     * @return User entity with specified values
     */
    protected User createTestUser(Long userId, String loginName, String email) {
        User user = new User();
        user.setUserId(userId);
        user.setLoginName(loginName);
        user.setFirstName(DEFAULT_FIRST_NAME);
        user.setLastName(DEFAULT_LAST_NAME);
        user.setEmail(email);
        user.setIsDeleted(false);
        user.setCreatedUser(DEFAULT_CREATED_USER);
        user.setModifiedUser(DEFAULT_CREATED_USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    /**
     * Creates a deleted test User.
     * 
     * @return User entity marked as deleted
     */
    protected User createDeletedTestUser() {
        User user = createTestUser();
        user.setIsDeleted(true);
        return user;
    }

    // ==================== CLIENT FACTORY METHODS ====================

    /**
     * Creates a test Client with default values.
     * 
     * @return Client entity with default test values
     */
    protected Client createTestClient() {
        return createTestClient(DEFAULT_CLIENT_ID);
    }

    /**
     * Creates a test Client with a specific ID.
     * 
     * @param clientId The client ID to set
     * @return Client entity with specified ID and default values
     */
    protected Client createTestClient(Long clientId) {
        return createTestClient(clientId, DEFAULT_CLIENT_NAME);
    }

    /**
     * Creates a fully customized test Client.
     * 
     * @param clientId The client ID
     * @param name     The client name
     * @return Client entity with specified values
     */
    protected Client createTestClient(Long clientId, String name) {
        Client client = new Client();
        client.setClientId(clientId);
        client.setName(name);
        client.setDescription(DEFAULT_CLIENT_DESCRIPTION);
        client.setSupportEmail(DEFAULT_SUPPORT_EMAIL);
        client.setWebsite(DEFAULT_WEBSITE);
        client.setIsDeleted(false);
        client.setCreatedUser(DEFAULT_CREATED_USER);
        client.setModifiedUser(DEFAULT_CREATED_USER);
        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());
        return client;
    }

    /**
     * Creates a deleted test Client.
     * 
     * @return Client entity marked as deleted
     */
    protected Client createDeletedTestClient() {
        Client client = createTestClient();
        client.setIsDeleted(true);
        return client;
    }

    /**
     * Creates a valid ClientRequestModel with default values.
     *
     * @return ClientRequestModel with all required fields populated
     */
    protected ClientRequestModel createValidClientRequest() {
        return createValidClientRequest(DEFAULT_CLIENT_ID, DEFAULT_CLIENT_NAME);
    }

    /**
     * Creates a valid ClientRequestModel with specific ID and name.
     *
     * @param clientId The client ID
     * @param name     The client name
     * @return ClientRequestModel with specified values
     */
    protected ClientRequestModel createValidClientRequest(Long clientId, String name) {
        ClientRequestModel request = new ClientRequestModel();
        request.setClientId(clientId);
        request.setName(name);
        request.setDescription(DEFAULT_CLIENT_DESCRIPTION);
        request.setSupportEmail(DEFAULT_SUPPORT_EMAIL);
        request.setWebsite(DEFAULT_WEBSITE);
        request.setSendgridSenderName("Sender");
        request.setGoogleCredId(DEFAULT_GOOGLE_CRED_ID);
        request.setIsDeleted(false);
        return request;
    }

    /**
     * Creates a test GoogleCred with default values.
     *
     * @return GoogleCred with default test values
     */
    protected GoogleCred createTestGoogleCred() {
        return createTestGoogleCred(DEFAULT_GOOGLE_CRED_ID);
    }

    /**
     * Creates a test GoogleCred with a specific ID.
     *
     * @param googleCredId The Google credential ID
     * @return GoogleCred with specified ID
     */
    protected GoogleCred createTestGoogleCred(Long googleCredId) {
        GoogleCred cred = new GoogleCred();
        cred.setGoogleCredId(googleCredId);
        cred.setProjectId("test-project");
        return cred;
    }

    /**
     * Creates a test UserClientMapping with default values.
     */
    protected UserClientMapping createTestUserClientMapping() {
        return createTestUserClientMapping(1L, DEFAULT_USER_ID, DEFAULT_CLIENT_ID, "test-api-key");
    }

    /**
     * Creates a test UserClientMapping with specified values.
     */
    protected UserClientMapping createTestUserClientMapping(Long mappingId, Long userId, Long clientId, String apiKey) {
        UserClientMapping m = new UserClientMapping();
        m.setMappingId(mappingId);
        m.setUserId(userId);
        m.setClientId(clientId);
        m.setApiKey(apiKey);
        m.setCreatedUser(DEFAULT_CREATED_USER);
        m.setModifiedUser(DEFAULT_CREATED_USER);
        return m;
    }

    // ==================== ADDRESS REQUEST MODEL FACTORY METHODS
    // ====================

    /**
     * Creates a valid AddressRequestModel with default values.
     * 
     * @return AddressRequestModel with all required fields populated
     */
    protected AddressRequestModel createValidAddressRequest() {
        return createValidAddressRequest(DEFAULT_ADDRESS_ID, DEFAULT_USER_ID, DEFAULT_CLIENT_ID);
    }

    /**
     * Creates a valid AddressRequestModel with specific IDs.
     * 
     * @param addressId The address ID
     * @param userId    The user ID
     * @param clientId  The client ID
     * @return AddressRequestModel with specified IDs and default values
     */
    protected AddressRequestModel createValidAddressRequest(Long addressId, Long userId, Long clientId) {
        AddressRequestModel request = new AddressRequestModel();
        request.setId(addressId);
        request.setUserId(userId);
        request.setClientId(clientId);
        request.setAddressType(DEFAULT_ADDRESS_TYPE);
        request.setStreetAddress(DEFAULT_STREET_ADDRESS);
        request.setCity(DEFAULT_CITY);
        request.setState(DEFAULT_STATE);
        request.setPostalCode(DEFAULT_POSTAL_CODE);
        request.setCountry(DEFAULT_COUNTRY);
        request.setIsPrimary(true);
        request.setIsDeleted(false);
        return request;
    }

    /**
     * Creates a valid AddressRequestModel with a specific address type.
     * 
     * @param addressType The address type (HOME, WORK, BILLING, SHIPPING, OFFICE,
     *                    WAREHOUSE)
     * @return AddressRequestModel with specified address type
     */
    protected AddressRequestModel createAddressRequestWithType(String addressType) {
        AddressRequestModel request = createValidAddressRequest();
        request.setAddressType(addressType);
        return request;
    }

    /**
     * Creates an AddressRequestModel with all optional fields populated.
     * 
     * @return AddressRequestModel with all fields (required + optional) populated
     */
    protected AddressRequestModel createFullAddressRequest() {
        AddressRequestModel request = createValidAddressRequest();
        request.setStreetAddress2("Apt 101");
        request.setStreetAddress3("Building A");
        request.setNameOnAddress("John Doe");
        request.setEmailOnAddress("john@example.com");
        request.setPhoneOnAddress("1234567890");
        return request;
    }

    // ==================== ADDRESS ENTITY FACTORY METHODS ====================

    /**
     * Creates a test Address entity from an AddressRequestModel.
     * 
     * @param request     The address request model
     * @param createdUser The user who created the address
     * @return Address entity with timestamps set
     */
    protected Address createTestAddress(AddressRequestModel request, String createdUser) {
        Address address = new Address(request, createdUser);
        address.setAddressId(request.getId());
        address.setIsDeleted(false);
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());
        return address;
    }

    /**
     * Creates a test Address entity with default values.
     * 
     * @return Address entity with default test values
     */
    protected Address createTestAddress() {
        return createTestAddress(createValidAddressRequest(), DEFAULT_CREATED_USER);
    }

    /**
     * Creates a test Address entity with a specific ID.
     * 
     * @param addressId The address ID
     * @return Address entity with specified ID
     */
    protected Address createTestAddress(Long addressId) {
        AddressRequestModel request = createValidAddressRequest();
        request.setId(addressId);
        return createTestAddress(request, DEFAULT_CREATED_USER);
    }

    /**
     * Creates a test Address entity with a specific ID and address type.
     *
     * @param addressId   The address ID
     * @param addressType The address type (HOME, WORK, BILLING, etc.)
     * @return Address entity with specified ID and type
     */
    protected Address createTestAddress(Long addressId, String addressType) {
        AddressRequestModel request = createValidAddressRequest();
        request.setId(addressId);
        request.setAddressType(addressType);
        return createTestAddress(request, DEFAULT_CREATED_USER);
    }

    /**
     * Creates a deleted test Address.
     * 
     * @return Address entity marked as deleted
     */
    protected Address createDeletedTestAddress() {
        Address address = createTestAddress();
        address.setIsDeleted(true);
        return address;
    }

    // ==================== LEAD REQUEST FACTORY METHODS ====================

    protected LeadRequestModel createValidLeadRequest() {
        return createValidLeadRequest(DEFAULT_LEAD_ID, DEFAULT_CLIENT_ID);
    }

    protected LeadRequestModel createValidLeadRequest(Long leadId, Long clientId) {
        LeadRequestModel request = new LeadRequestModel();
        request.setLeadId(leadId);
        request.setClientId(clientId);
        request.setFirstName(DEFAULT_FIRST_NAME);
        request.setLastName(DEFAULT_LAST_NAME);
        request.setEmail(DEFAULT_EMAIL);
        request.setPhone(DEFAULT_PHONE);
        request.setCompany(DEFAULT_COMPANY);
        request.setCompanySize(DEFAULT_COMPANY_SIZE);
        request.setLeadStatus(DEFAULT_LEAD_STATUS);
        request.setCreatedById(DEFAULT_CREATED_BY_ID);
        request.setAssignedAgentId(DEFAULT_ASSIGNED_AGENT_ID);
        request.setAddress(createValidAddressRequest());
        request.setIsDeleted(false);
        return request;
    }

    // ==================== LEAD ENTITY FACTORY METHODS ====================

    protected Lead createTestLead() {
        return createTestLead(createValidLeadRequest(), DEFAULT_CREATED_USER);
    }

    protected Lead createTestLead(LeadRequestModel request, String createdUser) {
        Lead lead = new Lead(request, createdUser);
        lead.setLeadId(request.getLeadId());
        lead.setAddressId(DEFAULT_ADDRESS_ID); // Assume relationship linked

        // Mock relationships
        User user = createTestUser(request.getCreatedById());
        Address address = createTestAddress(request.getAddress(), createdUser);

        lead.setCreatedByUser(user);
        lead.setAssignedAgent(user); // Reusing user for agent for simplicity
        lead.setAddress(address);

        lead.setIsDeleted(false);
        lead.setCreatedAt(LocalDateTime.now());
        lead.setUpdatedAt(LocalDateTime.now());
        return lead;
    }

    // ==================== TODO REQUEST FACTORY METHODS ====================

    protected TodoRequestModel createValidTodoRequest() {
        return createValidTodoRequest(DEFAULT_TODO_ID);
    }

    protected TodoRequestModel createValidTodoRequest(Long todoId) {
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(todoId);
        request.setTask(DEFAULT_TODO_TITLE);
        request.setIsDone(DEFAULT_TODO_COMPLETED);
        return request;
    }

    protected TodoRequestModel createValidTodoRequest(Long todoId, Long userId) {
        // userId is not used in TodoRequestModel, but we accept it for compatibility
        return createValidTodoRequest(todoId);
    }

    // ==================== TODO ENTITY FACTORY METHODS ====================

    protected Todo createTestTodo() {
        return createTestTodo(createValidTodoRequest(), DEFAULT_CREATED_USER);
    }

    protected Todo createTestTodo(TodoRequestModel request, String createdUser) {
        Todo todo = new Todo();
        todo.setTodoId(request.getTodoId());
        todo.setTask(request.getTask());
        todo.setIsDone(request.getIsDone());
        todo.setCreatedUser(createdUser);
        todo.setCreatedAt(LocalDateTime.now());
        todo.setUpdatedAt(LocalDateTime.now());
        return todo;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Gets the current timestamp for test data.
     * 
     * @return Current LocalDateTime
     */
    protected LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Gets a timestamp in the past for test data.
     * 
     * @param daysAgo Number of days in the past
     * @return LocalDateTime from specified days ago
     */
    protected LocalDateTime daysAgo(int daysAgo) {
        return LocalDateTime.now().minusDays(daysAgo);
    }

    /**
     * Gets a timestamp in the future for test data.
     * 
     * @param daysFromNow Number of days in the future
     * @return LocalDateTime from specified days in the future
     */
    protected LocalDateTime daysFromNow(int daysFromNow) {
        return LocalDateTime.now().plusDays(daysFromNow);
    }

    // ==================== MESSAGE FACTORY METHODS ====================

    /**
     * Creates a valid MessageRequestModel for testing.
     */
    protected MessageRequestModel createValidMessageRequest() {
        MessageRequestModel request = new MessageRequestModel();
        request.setMessageId(DEFAULT_MESSAGE_ID);
        request.setTitle("Test Message");
        request.setDescriptionHtml("<p>Test message content</p>");
        request.setSendAsEmail(false);
        request.setIsDeleted(false);
        request.setPublishDate(LocalDateTime.now().plusDays(1));
        return request;
    }

    /**
     * Creates a test Message entity.
     */
    protected Message createTestMessage() {
        return createTestMessage(DEFAULT_MESSAGE_ID);
    }

    /**
     * Creates a test Message entity with specified ID.
     */
    protected Message createTestMessage(Long messageId) {
        Message message = new Message();
        message.setMessageId(messageId);
        message.setClientId(DEFAULT_CLIENT_ID);
        message.setTitle("Test Message");
        message.setDescriptionHtml("<p>Test message content</p>");
        message.setSendAsEmail(false);
        message.setIsDeleted(false);
        message.setPublishDate(LocalDateTime.now().plusDays(1));
        message.setCreatedUser(DEFAULT_CREATED_USER);
        message.setModifiedUser(DEFAULT_CREATED_USER);
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        return message;
    }

    // ==================== LOGIN FACTORY METHODS ====================

    /**
     * Creates a valid LoginRequestModel for testing.
     */
    protected LoginRequestModel createValidLoginRequest() {
        LoginRequestModel request = new LoginRequestModel();
        request.setUserId(DEFAULT_USER_ID);
        request.setLoginName(DEFAULT_LOGIN_NAME);
        request.setPassword("testPassword123");
        request.setClientId(DEFAULT_CLIENT_ID);
        return request;
    }

    // ==================== SHIPMENT FACTORY METHODS ====================

    /**
     * Creates a test Shipment with default values.
     */
    protected Shipment createTestShipment() {
        return createTestShipment(DEFAULT_SHIPMENT_ID);
    }

    /**
     * Creates a test Shipment with specified ID.
     */
    protected Shipment createTestShipment(Long shipmentId) {
        Shipment shipment = new Shipment();
        shipment.setShipmentId(shipmentId);
        shipment.setClientId(DEFAULT_CLIENT_ID);
        shipment.setShipRocketOrderId("SR" + shipmentId);
        shipment.setShipRocketStatus("NEW");
        shipment.setCreatedUser(DEFAULT_CREATED_USER);
        shipment.setModifiedUser(DEFAULT_CREATED_USER);
        shipment.setCreatedAt(LocalDateTime.now());
        shipment.setUpdatedAt(LocalDateTime.now());
        return shipment;
    }

    /**
     * Creates a deleted test Shipment.
     */
    protected Shipment createDeletedTestShipment() {
        Shipment shipment = createTestShipment();
        // Note: Shipment uses shipRocketStatus for deletion status
        shipment.setShipRocketStatus("CANCELLED");
        return shipment;
    }

    // ==================== ASSERTION HELPER METHODS ====================

    /**
     * Asserts that an executable throws BadRequestException with expected message.
     */
    protected void assertThrowsBadRequest(String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
        BadRequestException ex = assertThrows(BadRequestException.class, executable);
        assertEquals(expectedMessage, ex.getMessage());
    }

    /**
     * Asserts that an executable throws NotFoundException with expected message.
     */
    protected void assertThrowsNotFound(String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
        NotFoundException ex = assertThrows(NotFoundException.class, executable);
        assertEquals(expectedMessage, ex.getMessage());
    }

    // ==================== PAGINATION HELPER METHODS ====================

    /**
     * Creates a valid pagination request for testing.
     */
    protected PaginationBaseRequestModel createValidPaginationRequest() {
        PaginationBaseRequestModel request = new PaginationBaseRequestModel();
        request.setStart(0);
        request.setEnd(10);
        request.setFilters(new ArrayList<>());
        return request;
    }

    /**
     * Creates a filter condition for pagination testing.
     */
    protected FilterCondition createFilterCondition(String column, String operator, String value) {
        FilterCondition condition = new FilterCondition();
        condition.setColumn(column);
        condition.setOperator(operator);
        condition.setValue(value);
        return condition;
    }

    /**
     * Creates a filter condition for pagination testing (Object value).
     */
    protected FilterCondition createFilterCondition(String column, String operator, Object value) {
        return createFilterCondition(column, operator, value != null ? value.toString() : null);
    }

    /**
     * Joins multiple string arrays into one.
     */
    protected String[] joinArrays(String[]... arrays) {
        List<String> result = new ArrayList<>();
        for (String[] array : arrays) {
            if (array != null) {
                for (String s : array) {
                    result.add(s);
                }
            }
        }
        return result.toArray(new String[0]);
    }

    // ==================== BATCH FILTERING CONSTANTS ====================

    protected static final String[] BATCH_STRING_OPERATORS = {"equals", "contains", "startsWith", "endsWith"};
    protected static final String[] BATCH_NUMBER_OPERATORS = {"equals", "notEquals", "greaterThan", "greaterThanOrEqual", "lessThan", "lessThanOrEqual"};
    protected static final String[] BATCH_BOOLEAN_OPERATORS = {"is"};
    protected static final String[] BATCH_DATE_OPERATORS = {"is", "isNot", "isAfter", "isOnOrAfter", "isBefore", "isOnOrBefore"};
    protected static final String[] BATCH_INVALID_OPERATORS = {"invalid", "xyz", "!@#"};
    protected static final String[] BATCH_INVALID_COLUMNS = {"invalidColumn", "xyz", "!@#"};
    protected static final String[] BATCH_VALID_VALUES = {"test", "value", "123"};
    protected static final String[] BATCH_EMPTY_VALUES = {"", " ", null};

    // Lead columns
    protected static final String[] LEAD_STRING_COLUMNS = {"firstName", "lastName", "email", "phone", "company", "leadStatus"};
    protected static final String[] LEAD_NUMBER_COLUMNS = {"leadId", "companySize"};
    protected static final String[] LEAD_BOOLEAN_COLUMNS = {"isDeleted"};
    protected static final String[] LEAD_DATE_COLUMNS = {"createdAt", "updatedAt"};

    // Product columns
    protected static final String[] PRODUCT_STRING_COLUMNS = {"name", "description", "sku"};
    protected static final String[] PRODUCT_NUMBER_COLUMNS = {"productId", "price", "stock"};
    protected static final String[] PRODUCT_BOOLEAN_COLUMNS = {"isDeleted", "isActive"};
    protected static final String[] PRODUCT_DATE_COLUMNS = {"createdAt", "updatedAt"};

    // Package columns
    protected static final String[] PACKAGE_STRING_COLUMNS = {"packageName", "packageType", "notes"};
    protected static final String[] PACKAGE_NUMBER_COLUMNS = {"packageId", "length", "breadth", "height", "maxWeight", "standardCapacity", "pricePerUnit"};
    protected static final String[] PACKAGE_BOOLEAN_COLUMNS = {"isDeleted"};
    protected static final String[] PACKAGE_DATE_COLUMNS = {"createdAt", "updatedAt"};

    // ==================== PACKAGE FACTORY METHODS ====================

    /**
     * Creates a valid PackageRequestModel for testing.
     */
    protected PackageRequestModel createValidPackageRequest() {
        PackageRequestModel request = new PackageRequestModel();
        request.setPackageId(DEFAULT_PACKAGE_ID);
        request.setPackageName("Test Package");
        request.setLength(10);
        request.setBreadth(10);
        request.setHeight(10);
        request.setMaxWeight(new java.math.BigDecimal("5.00"));
        request.setStandardCapacity(100);
        request.setPricePerUnit(new java.math.BigDecimal("10.00"));
        request.setPackageType("BOX");
        request.setIsDeleted(false);
        return request;
    }

    /**
     * Creates a test Package entity.
     */
    protected com.example.SpringApi.Models.DatabaseModels.Package createTestPackage() {
        return createTestPackage(DEFAULT_PACKAGE_ID);
    }

    /**
     * Creates a test Package entity with specified ID.
     */
    protected com.example.SpringApi.Models.DatabaseModels.Package createTestPackage(Long packageId) {
        com.example.SpringApi.Models.DatabaseModels.Package pkg = new com.example.SpringApi.Models.DatabaseModels.Package();
        pkg.setPackageId(packageId);
        pkg.setClientId(DEFAULT_CLIENT_ID);
        pkg.setPackageName("Test Package");
        pkg.setLength(10);
        pkg.setBreadth(10);
        pkg.setHeight(10);
        pkg.setMaxWeight(new java.math.BigDecimal("5.00"));
        pkg.setStandardCapacity(100);
        pkg.setPricePerUnit(new java.math.BigDecimal("10.00"));
        pkg.setPackageType("BOX");
        pkg.setIsDeleted(false);
        pkg.setCreatedUser(DEFAULT_CREATED_USER);
        pkg.setModifiedUser(DEFAULT_CREATED_USER);
        pkg.setCreatedAt(LocalDateTime.now());
        pkg.setUpdatedAt(LocalDateTime.now());
        return pkg;
    }

    /**
     * Creates a test PackagePickupLocationMapping entity.
     */
    protected PackagePickupLocationMapping createTestPackagePickupLocationMapping() {
        return createTestPackagePickupLocationMapping(1L, DEFAULT_PACKAGE_ID, DEFAULT_PICKUP_LOCATION_ID);
    }

    /**
     * Creates a test PackagePickupLocationMapping entity with specified IDs.
     */
    protected PackagePickupLocationMapping createTestPackagePickupLocationMapping(Long mappingId, Long packageId, Long pickupLocationId) {
        PackagePickupLocationMapping mapping = new PackagePickupLocationMapping();
        mapping.setPackagePickupLocationMappingId(mappingId);
        mapping.setPackageId(packageId);
        mapping.setPickupLocationId(pickupLocationId);
        mapping.setAvailableQuantity(100);
        mapping.setReorderLevel(10);
        mapping.setMaxStockLevel(500);
        mapping.setLastRestockDate(LocalDateTime.now());
        mapping.setCreatedUser(DEFAULT_CREATED_USER);
        mapping.setModifiedUser(DEFAULT_CREATED_USER);
        mapping.setCreatedAt(LocalDateTime.now());
        mapping.setUpdatedAt(LocalDateTime.now());
        return mapping;
    }

    // ==================== PURCHASE ORDER FACTORY METHODS ====================

    /**
     * Creates a test PurchaseOrder entity.
     */
    protected PurchaseOrder createTestPurchaseOrder() {
        return createTestPurchaseOrder(DEFAULT_PURCHASE_ORDER_ID);
    }

    /**
     * Creates a test PurchaseOrder entity with specified ID.
     */
    protected PurchaseOrder createTestPurchaseOrder(Long purchaseOrderId) {
        PurchaseOrder po = new PurchaseOrder();
        po.setPurchaseOrderId(purchaseOrderId);
        po.setClientId(DEFAULT_CLIENT_ID);
        po.setVendorNumber("VENDOR-001");
        po.setPurchaseOrderStatus("DRAFT");
        po.setAssignedLeadId(DEFAULT_LEAD_ID);
        po.setIsDeleted(false);
        po.setCreatedUser(DEFAULT_CREATED_USER);
        po.setModifiedUser(DEFAULT_CREATED_USER);
        po.setCreatedAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());
        return po;
    }
}
