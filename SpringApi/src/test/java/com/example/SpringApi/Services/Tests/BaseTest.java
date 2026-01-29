package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.*;
import com.example.SpringApi.Models.RequestModels.*;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.FilterCondition;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDate;
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

    // ==================== PAYMENT FACTORY METHODS ====================

    protected PaymentRequestModel createValidPaymentRequest() {
        PaymentRequestModel request = new PaymentRequestModel();
        request.setPaymentId(DEFAULT_PAYMENT_ID);
        request.setAmount(DEFAULT_AMOUNT);
        request.setPaymentMethod(DEFAULT_PAYMENT_METHOD);
        request.setPaymentStatus(DEFAULT_PAYMENT_STATUS);
        request.setPurchaseOrderId(DEFAULT_PURCHASE_ORDER_ID);
        return request;
    }

    protected Payment createTestPayment() {
        Payment payment = new Payment();
        payment.setPaymentId(DEFAULT_PAYMENT_ID);
        payment.setAmount(DEFAULT_AMOUNT);
        payment.setPaymentMethod(DEFAULT_PAYMENT_METHOD);
        payment.setPaymentStatus(DEFAULT_PAYMENT_STATUS);
        payment.setIsDeleted(false);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        return payment;
    }

    // ==================== PURCHASE ORDER FACTORY METHODS ====================

    protected PurchaseOrderRequestModel createValidPurchaseOrderRequest() {
        PurchaseOrderRequestModel request = new PurchaseOrderRequestModel();
        request.setPurchaseOrderId(DEFAULT_PURCHASE_ORDER_ID);
        request.setClientId(DEFAULT_CLIENT_ID);
        request.setLeadId(DEFAULT_LEAD_ID);
        request.setBillingAddressId(DEFAULT_ADDRESS_ID);
        request.setShippingAddressId(DEFAULT_ADDRESS_ID);
        request.setStatus(DEFAULT_PO_STATUS);
        
        PurchaseOrderRequestModel.OrderSummaryData orderSummary = new PurchaseOrderRequestModel.OrderSummaryData();
        orderSummary.setSubtotal(DEFAULT_SUBTOTAL);
        orderSummary.setTax(DEFAULT_TAX);
        orderSummary.setShippingCharge(DEFAULT_SHIPPING_CHARGE);
        orderSummary.setTotal(DEFAULT_TOTAL);
        request.setOrderSummary(orderSummary);
        
        request.setProducts(new ArrayList<>());
        request.setIsDeleted(false);
        return request;
    }

    protected PurchaseOrder createTestPurchaseOrder() {
        PurchaseOrder po = new PurchaseOrder();
        po.setPurchaseOrderId(DEFAULT_PURCHASE_ORDER_ID);
        po.setClientId(DEFAULT_CLIENT_ID);
        po.setLeadId(DEFAULT_LEAD_ID);
        po.setStatus(DEFAULT_PO_STATUS);
        po.setSubtotal(DEFAULT_SUBTOTAL);
        po.setTax(DEFAULT_TAX);
        po.setShippingCharge(DEFAULT_SHIPPING_CHARGE);
        po.setTotal(DEFAULT_TOTAL);
        po.setIsDeleted(false);
        po.setCreatedAt(LocalDateTime.now());
        po.setUpdatedAt(LocalDateTime.now());
        return po;
    }

    // ==================== SHIPMENT FACTORY METHODS ====================

    protected ShipmentRequestModel createValidShipmentRequest() {
        ShipmentRequestModel request = new ShipmentRequestModel();
        request.setShipmentId(DEFAULT_SHIPMENT_ID);
        request.setPurchaseOrderId(DEFAULT_PURCHASE_ORDER_ID);
        request.setStatus(DEFAULT_SHIPMENT_STATUS);
        request.setWeight(DEFAULT_WEIGHT);
        request.setTrackingNumber(DEFAULT_TRACKING_NUMBER);
        return request;
    }

    protected Shipment createTestShipment() {
        Shipment shipment = new Shipment();
        shipment.setShipmentId(DEFAULT_SHIPMENT_ID);
        shipment.setPurchaseOrderId(DEFAULT_PURCHASE_ORDER_ID);
        shipment.setStatus(DEFAULT_SHIPMENT_STATUS);
        shipment.setWeight(DEFAULT_WEIGHT);
        shipment.setTrackingNumber(DEFAULT_TRACKING_NUMBER);
        shipment.setIsDeleted(false);
        shipment.setCreatedAt(LocalDateTime.now());
        shipment.setUpdatedAt(LocalDateTime.now());
        return shipment;
    }

    // ==================== TODO FACTORY METHODS ====================

    protected TodoRequestModel createValidTodoRequest() {
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(DEFAULT_TODO_ID);
        request.setTask(DEFAULT_TODO_TITLE);
        request.setIsDone(DEFAULT_TODO_COMPLETED);
        return request;
    }

    protected Todo createTestTodo() {
        Todo todo = new Todo();
        todo.setTodoId(DEFAULT_TODO_ID);
        todo.setTask(DEFAULT_TODO_TITLE);
        todo.setIsDone(DEFAULT_TODO_COMPLETED);
        todo.setUserId(DEFAULT_USER_ID);
        todo.setCreatedAt(LocalDateTime.now());
        todo.setUpdatedAt(LocalDateTime.now());
        return todo;
    }

    // ==================== USER GROUP FACTORY METHODS ====================

    protected UserGroupRequestModel createValidUserGroupRequest() {
        UserGroupRequestModel request = new UserGroupRequestModel();
        request.setUserGroupId(DEFAULT_USER_GROUP_ID);
        request.setUserGroupName(DEFAULT_USER_GROUP_NAME);
        request.setDescription(DEFAULT_USER_GROUP_DESCRIPTION);
        request.setClientId(DEFAULT_CLIENT_ID);
        return request;
    }

    protected UserGroup createTestUserGroup() {
        UserGroup userGroup = new UserGroup();
        userGroup.setUserGroupId(DEFAULT_USER_GROUP_ID);
        userGroup.setUserGroupName(DEFAULT_USER_GROUP_NAME);
        userGroup.setDescription(DEFAULT_USER_GROUP_DESCRIPTION);
        userGroup.setClientId(DEFAULT_CLIENT_ID);
        userGroup.setIsDeleted(false);
        userGroup.setCreatedAt(LocalDateTime.now());
        userGroup.setUpdatedAt(LocalDateTime.now());
        return userGroup;
    }

    // ==================== USER LOG FACTORY METHODS ====================

    protected UserLogRequestModel createValidUserLogRequest() {
        UserLogRequestModel request = new UserLogRequestModel();
        request.setUserLogId(DEFAULT_USER_LOG_ID);
        request.setUserId(DEFAULT_USER_ID);
        request.setAction(DEFAULT_ACTION);
        request.setDetails(DEFAULT_LOG_DETAILS);
        request.setClientId(DEFAULT_CLIENT_ID);
        return request;
    }

    protected UserLog createTestUserLog() {
        UserLog userLog = new UserLog();
        userLog.setUserLogId(DEFAULT_USER_LOG_ID);
        userLog.setUserId(DEFAULT_USER_ID);
        userLog.setAction(DEFAULT_ACTION);
        userLog.setDetails(DEFAULT_LOG_DETAILS);
        userLog.setClientId(DEFAULT_CLIENT_ID);
        userLog.setIsDeleted(false);
        userLog.setCreatedAt(LocalDateTime.now());
        userLog.setUpdatedAt(LocalDateTime.now());
        return userLog;
    }

    // ==================== PACKAGE FACTORY METHODS ====================

    protected PackageRequestModel createValidPackageRequest() {
        PackageRequestModel request = new PackageRequestModel();
        request.setPackageId(DEFAULT_PACKAGE_ID);
        request.setPackageName(DEFAULT_PACKAGE_NAME);
        request.setPricePerUnit(DEFAULT_PACKAGE_PRICE);
        request.setLength(10);
        request.setBreadth(10);
        request.setHeight(10);
        request.setMaxWeight(BigDecimal.valueOf(5.0));
        request.setStandardCapacity(1);
        request.setPackageType("Box");
        request.setIsDeleted(false);
        return request;
    }

    protected com.example.SpringApi.Models.DatabaseModels.Package createTestPackage() {
        com.example.SpringApi.Models.DatabaseModels.Package pkg = new com.example.SpringApi.Models.DatabaseModels.Package();
        pkg.setPackageId(DEFAULT_PACKAGE_ID);
        pkg.setPackageName(DEFAULT_PACKAGE_NAME);
        pkg.setPricePerUnit(DEFAULT_PACKAGE_PRICE);
        pkg.setLength(10);
        pkg.setBreadth(10);
        pkg.setHeight(10);
        pkg.setMaxWeight(BigDecimal.valueOf(5.0));
        pkg.setStandardCapacity(1);
        pkg.setPackageType("Box");
        pkg.setClientId(DEFAULT_CLIENT_ID);
        pkg.setIsDeleted(false);
        pkg.setCreatedAt(LocalDateTime.now());
        pkg.setUpdatedAt(LocalDateTime.now());
        return pkg;
    }

    /**
     * Creates a test PackagePickupLocationMapping with default values.
     *
     * @return PackagePickupLocationMapping with default test values
     */
    protected PackagePickupLocationMapping createTestPackagePickupLocationMapping() {
        return createTestPackagePickupLocationMapping(1L, DEFAULT_PACKAGE_ID, DEFAULT_PICKUP_LOCATION_ID);
    }

    /**
     * Creates a test PackagePickupLocationMapping with specified IDs.
     */
    protected PackagePickupLocationMapping createTestPackagePickupLocationMapping(
            Long mappingId, Long packageId, Long pickupLocationId) {
        PackagePickupLocationMapping m = new PackagePickupLocationMapping();
        m.setPackagePickupLocationMappingId(mappingId);
        m.setPackageId(packageId);
        m.setPickupLocationId(pickupLocationId);
        m.setAvailableQuantity(10);
        m.setReorderLevel(5);
        m.setMaxStockLevel(20);
        m.setPackageEntity(createTestPackage());
        m.setCreatedUser(DEFAULT_CREATED_USER);
        m.setModifiedUser(DEFAULT_CREATED_USER);
        return m;
    }

    // ==================== PROMO FACTORY METHODS ====================

    protected PromoRequestModel createValidPromoRequest() {
        PromoRequestModel request = new PromoRequestModel();
        request.setPromoId(DEFAULT_PROMO_ID);
        request.setPromoCode(DEFAULT_PROMO_CODE);
        request.setDiscountPercentage(DEFAULT_DISCOUNT_PERCENTAGE);
        request.setMinimumCartValue(DEFAULT_MIN_CART_VALUE);
        request.setClientId(DEFAULT_CLIENT_ID);
        request.setStartDate(LocalDate.now());
        request.setExpiryDate(LocalDate.now().plusDays(30));
        return request;
    }

    protected Promo createTestPromo() {
        Promo promo = new Promo();
        promo.setPromoId(DEFAULT_PROMO_ID);
        promo.setPromoCode(DEFAULT_PROMO_CODE);
        promo.setDiscountPercentage(DEFAULT_DISCOUNT_PERCENTAGE);
        promo.setMinimumCartValue(DEFAULT_MIN_CART_VALUE);
        promo.setClientId(DEFAULT_CLIENT_ID);
        promo.setStartDate(LocalDate.now());
        promo.setExpiryDate(LocalDate.now().plusDays(30));
        promo.setIsDeleted(false);
        promo.setCreatedAt(LocalDateTime.now());
        promo.setUpdatedAt(LocalDateTime.now());
        return promo;
    }

    // ==================== PICKUP LOCATION FACTORY METHODS ====================

    protected PickupLocationRequestModel createValidPickupLocationRequest() {
        PickupLocationRequestModel request = new PickupLocationRequestModel();
        request.setPickupLocationId(DEFAULT_PICKUP_LOCATION_ID);
        request.setLocationName(DEFAULT_LOCATION_NAME);
        request.setAddress(DEFAULT_LOCATION_ADDRESS);
        request.setClientId(DEFAULT_CLIENT_ID);
        return request;
    }

    protected PickupLocation createTestPickupLocation() {
        PickupLocation location = new PickupLocation();
        location.setPickupLocationId(DEFAULT_PICKUP_LOCATION_ID);
        location.setLocationName(DEFAULT_LOCATION_NAME);
        location.setAddress(DEFAULT_LOCATION_ADDRESS);
        location.setClientId(DEFAULT_CLIENT_ID);
        location.setIsDeleted(false);
        location.setCreatedAt(LocalDateTime.now());
        location.setUpdatedAt(LocalDateTime.now());
        return location;
    }

    // ==================== PRODUCT FACTORY METHODS ====================

    protected ProductRequestModel createValidProductRequest() {
        ProductRequestModel request = new ProductRequestModel();
        request.setProductId(DEFAULT_PRODUCT_ID);
        request.setProductName(DEFAULT_PRODUCT_NAME);
        request.setPrice(DEFAULT_PRODUCT_PRICE);
        request.setStockQuantity(DEFAULT_STOCK_QUANTITY);
        request.setClientId(DEFAULT_CLIENT_ID);
        return request;
    }

    protected Product createTestProduct() {
        Product product = new Product();
        product.setProductId(DEFAULT_PRODUCT_ID);
        product.setProductName(DEFAULT_PRODUCT_NAME);
        product.setPrice(DEFAULT_PRODUCT_PRICE);
        product.setStockQuantity(DEFAULT_STOCK_QUANTITY);
        product.setClientId(DEFAULT_CLIENT_ID);
        product.setIsDeleted(false);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }

    // ==================== PRODUCT REVIEW FACTORY METHODS ====================

    protected ProductReviewRequestModel createValidProductReviewRequest() {
        ProductReviewRequestModel request = new ProductReviewRequestModel();
        request.setReviewId(DEFAULT_REVIEW_ID);
        request.setProductId(DEFAULT_PRODUCT_ID);
        request.setRating(DEFAULT_RATING);
        request.setReviewText(DEFAULT_REVIEW_TEXT);
        request.setUserId(DEFAULT_USER_ID);
        request.setClientId(DEFAULT_CLIENT_ID);
        return request;
    }

    protected ProductReview createTestProductReview() {
        ProductReview review = new ProductReview();
        review.setReviewId(DEFAULT_REVIEW_ID);
        review.setProductId(DEFAULT_PRODUCT_ID);
        review.setRating(DEFAULT_RATING);
        review.setReviewText(DEFAULT_REVIEW_TEXT);
        review.setUserId(DEFAULT_USER_ID);
        review.setClientId(DEFAULT_CLIENT_ID);
        review.setIsDeleted(false);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        return review;
    }

    // ==================== MESSAGE FACTORY METHODS ====================

    protected MessageRequestModel createValidMessageRequest() {
        MessageRequestModel request = new MessageRequestModel();
        request.setMessageId(1L);
        request.setContent("Test message");
        request.setSenderId(DEFAULT_USER_ID);
        request.setRecipientId(2L);
        request.setClientId(DEFAULT_CLIENT_ID);
        return request;
    }

    protected Message createTestMessage() {
        Message message = new Message();
        message.setMessageId(1L);
        message.setContent("Test message");
        message.setSenderId(DEFAULT_USER_ID);
        message.setRecipientId(2L);
        message.setClientId(DEFAULT_CLIENT_ID);
        message.setIsDeleted(false);
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        return message;
    }

    // ==================== LOGIN REQUEST FACTORY METHODS ====================

    protected LoginRequestModel createValidLoginRequest() {
        LoginRequestModel request = new LoginRequestModel();
        request.setLoginName(DEFAULT_LOGIN_NAME);
        request.setPassword("testPassword123");
        return request;
    }

    // ==================== PAGINATION REQUEST FACTORY METHODS ====================

    protected PaginationBaseRequestModel createValidPaginationRequest() {
        PaginationBaseRequestModel request = new PaginationBaseRequestModel();
        request.setStart(0);
        request.setEnd(10);
        request.setFilters(new ArrayList<>());
        return request;
    }

    // ==================== BATCH FILTER TEST DATA (for Get*InBatches triple-loop tests) ====================

    /** Lead valid string columns (LeadFilterQueryBuilder). */
    protected static final String[] LEAD_STRING_COLUMNS = {
        "firstName", "email", "company", "annualRevenue", "fax", "lastName",
        "leadStatus", "phone", "title", "website", "notes", "createdUser", "modifiedUser"
    };
    /** Lead valid number columns. */
    protected static final String[] LEAD_NUMBER_COLUMNS = {
        "leadId", "companySize", "clientId", "addressId", "createdById", "assignedAgentId"
    };
    /** Lead valid boolean columns. */
    protected static final String[] LEAD_BOOLEAN_COLUMNS = { "isDeleted" };
    /** Lead valid date columns. */
    protected static final String[] LEAD_DATE_COLUMNS = { "createdAt", "updatedAt" };
    /** Invalid columns for batch filter validation. */
    protected static final String[] BATCH_INVALID_COLUMNS = { "invalidCol", "DROP TABLE" };

    /** Package valid string columns (PackageFilterQueryBuilder). */
    protected static final String[] PACKAGE_STRING_COLUMNS = {
        "packageName", "dimensions", "packageType", "notes", "createdUser", "modifiedUser"
    };
    /** Package valid number columns. */
    protected static final String[] PACKAGE_NUMBER_COLUMNS = {
        "packageId", "clientId", "length", "breadth", "height", "maxWeight",
        "standardCapacity", "pricePerUnit", "pickupLocationId"
    };
    /** Package valid boolean columns. */
    protected static final String[] PACKAGE_BOOLEAN_COLUMNS = { "isDeleted" };
    /** Package valid date columns. */
    protected static final String[] PACKAGE_DATE_COLUMNS = { "createdAt", "updatedAt" };

    /** Product valid string columns (ProductFilterQueryBuilder). */
    protected static final String[] PRODUCT_STRING_COLUMNS = {
        "title", "descriptionHtml", "brand", "color", "colorLabel", "condition",
        "countryOfManufacture", "model", "upc", "modificationHtml", "notes", "createdUser", "modifiedUser"
    };
    /** Product valid number columns. */
    protected static final String[] PRODUCT_NUMBER_COLUMNS = {
        "productId", "clientId", "price", "discount", "length", "breadth",
        "height", "weightKgs", "categoryId", "pickupLocationId"
    };
    /** Product valid boolean columns. */
    protected static final String[] PRODUCT_BOOLEAN_COLUMNS = { "isDiscountPercent", "returnsAllowed", "itemModified", "isDeleted" };
    /** Product valid date columns. */
    protected static final String[] PRODUCT_DATE_COLUMNS = { "createdAt", "updatedAt" };

    /** String operators (PaginationBaseRequestModel). */
    protected static final String[] BATCH_STRING_OPERATORS = {
        PaginationBaseRequestModel.OP_CONTAINS, PaginationBaseRequestModel.OP_EQUALS,
        PaginationBaseRequestModel.OP_STARTS_WITH, PaginationBaseRequestModel.OP_ENDS_WITH,
        PaginationBaseRequestModel.OP_IS_EMPTY, PaginationBaseRequestModel.OP_IS_NOT_EMPTY,
        PaginationBaseRequestModel.OP_IS_ONE_OF, PaginationBaseRequestModel.OP_IS_NOT_ONE_OF,
        PaginationBaseRequestModel.OP_CONTAINS_ONE_OF
    };
    /** Number operators. */
    protected static final String[] BATCH_NUMBER_OPERATORS = {
        PaginationBaseRequestModel.OP_EQUAL, PaginationBaseRequestModel.OP_NOT_EQUAL,
        PaginationBaseRequestModel.OP_GREATER_THAN, PaginationBaseRequestModel.OP_GREATER_THAN_OR_EQUAL,
        PaginationBaseRequestModel.OP_LESS_THAN, PaginationBaseRequestModel.OP_LESS_THAN_OR_EQUAL,
        PaginationBaseRequestModel.OP_IS_EMPTY, PaginationBaseRequestModel.OP_IS_NOT_EMPTY,
        PaginationBaseRequestModel.OP_NUMBER_IS_ONE_OF, PaginationBaseRequestModel.OP_NUMBER_IS_NOT_ONE_OF
    };
    /** Boolean operators. */
    protected static final String[] BATCH_BOOLEAN_OPERATORS = { PaginationBaseRequestModel.OP_IS };
    /** Date operators. */
    protected static final String[] BATCH_DATE_OPERATORS = {
        PaginationBaseRequestModel.OP_IS, PaginationBaseRequestModel.OP_IS_NOT,
        PaginationBaseRequestModel.OP_IS_AFTER, PaginationBaseRequestModel.OP_IS_ON_OR_AFTER,
        PaginationBaseRequestModel.OP_IS_BEFORE, PaginationBaseRequestModel.OP_IS_ON_OR_BEFORE,
        PaginationBaseRequestModel.OP_IS_EMPTY, PaginationBaseRequestModel.OP_IS_NOT_EMPTY
    };
    /** Invalid operators for batch filter validation. */
    protected static final String[] BATCH_INVALID_OPERATORS = { "INVALID_OP", "Unknown" };

    /** Sample valid values for filter tests. */
    protected static final String[] BATCH_VALID_VALUES = { "test", "100", "2023-01-01", "true" };
    /** Empty/null values for filter tests. */
    protected static final String[] BATCH_EMPTY_VALUES = { null, "" };

    /**
     * Creates a FilterCondition for batch filter tests.
     *
     * @param column   Column name
     * @param operator Operator name
     * @param value    Value (may be null)
     * @return FilterCondition configured with the given values
     */
    protected FilterCondition createFilterCondition(String column, String operator, Object value) {
        FilterCondition fc = new FilterCondition();
        fc.setColumn(column);
        fc.setOperator(operator);
        fc.setValue(value);
        return fc;
    }

    // ==================== HELPER METHODS ====================

    /**
     * Joins multiple arrays into a single combined array.
     * 
     * @param arrays Variable number of arrays to join
     * @return Single combined array
     */
    protected String[] joinArrays(String[]... arrays) {
        List<String> combined = new ArrayList<>();
        for (String[] array : arrays) {
            for (String item : array) {
                combined.add(item);
            }
        }
        return combined.toArray(new String[0]);
    }

    /**
     * Asserts that the executable throws BadRequestException with the exact expected message.
     *
     * @param expectedMessage Expected exception message
     * @param executable      Code under test
     */
    protected void assertThrowsBadRequest(String expectedMessage, Executable executable) {
        BadRequestException ex = assertThrows(BadRequestException.class, executable);
        assertEquals(expectedMessage, ex.getMessage());
    }

    /**
     * Asserts that the executable throws NotFoundException with the exact expected message.
     *
     * @param expectedMessage Expected exception message
     * @param executable      Code under test
     */
    protected void assertThrowsNotFound(String expectedMessage, Executable executable) {
        NotFoundException ex = assertThrows(NotFoundException.class, executable);
        assertEquals(expectedMessage, ex.getMessage());
    }

    // ==================== PAYMENT REQUEST & ENTITY FACTORY METHODS ====================

    protected PaymentRequestModel createValidPaymentRequest(Long paymentId, BigDecimal amount) {
        PaymentRequestModel request = new PaymentRequestModel();
        request.setPaymentId(paymentId);
        request.setAmount(amount);
        request.setPaymentMethod(DEFAULT_PAYMENT_METHOD);
        request.setPaymentStatus(DEFAULT_PAYMENT_STATUS);
        request.setPurchaseOrderId(DEFAULT_PURCHASE_ORDER_ID);
        request.setIsDeleted(false);
        return request;
    }

    protected Payment createTestPayment(Long paymentId, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setAmount(amount);
        payment.setPaymentMethod(DEFAULT_PAYMENT_METHOD);
        payment.setPaymentStatus(DEFAULT_PAYMENT_STATUS);
        payment.setIsDeleted(false);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        return payment;
    }

    protected Payment createDeletedTestPayment() {
        Payment payment = createTestPayment(DEFAULT_PAYMENT_ID, DEFAULT_AMOUNT);
        payment.setIsDeleted(true);
        return payment;
    }

    // ==================== PROMO REQUEST & ENTITY FACTORY METHODS ====================

    protected PromoRequestModel createValidPromoRequest() {
        return createValidPromoRequest(DEFAULT_PROMO_ID, DEFAULT_CLIENT_ID);
    }

    protected PromoRequestModel createValidPromoRequest(Long promoId, Long clientId) {
        PromoRequestModel request = new PromoRequestModel();
        request.setPromoId(promoId);
        request.setClientId(clientId);
        request.setCode(DEFAULT_PROMO_CODE);
        request.setDiscountPercentage(DEFAULT_DISCOUNT_PERCENTAGE);
        request.setMinCartValue(DEFAULT_MIN_CART_VALUE);
        request.setIsDeleted(false);
        return request;
    }

    protected Promo createTestPromo() {
        return createTestPromo(DEFAULT_PROMO_ID);
    }

    protected Promo createTestPromo(Long promoId) {
        Promo promo = new Promo();
        promo.setPromoId(promoId);
        promo.setClientId(DEFAULT_CLIENT_ID);
        promo.setCode(DEFAULT_PROMO_CODE);
        promo.setDiscountPercentage(DEFAULT_DISCOUNT_PERCENTAGE);
        promo.setMinCartValue(DEFAULT_MIN_CART_VALUE);
        promo.setIsDeleted(false);
        promo.setCreatedAt(LocalDateTime.now());
        promo.setUpdatedAt(LocalDateTime.now());
        promo.setCreatedUser(DEFAULT_CREATED_USER);
        promo.setModifiedUser(DEFAULT_CREATED_USER);
        return promo;
    }

    protected Promo createDeletedTestPromo() {
        Promo promo = createTestPromo();
        promo.setIsDeleted(true);
        return promo;
    }

    // ==================== PRODUCT REQUEST & ENTITY FACTORY METHODS ====================

    protected ProductRequestModel createValidProductRequest() {
        return createValidProductRequest(DEFAULT_PRODUCT_ID, DEFAULT_CLIENT_ID);
    }

    protected ProductRequestModel createValidProductRequest(Long productId, Long clientId) {
        ProductRequestModel request = new ProductRequestModel();
        request.setProductId(productId);
        request.setClientId(clientId);
        request.setTitle(DEFAULT_PRODUCT_NAME);
        request.setPrice(DEFAULT_PRODUCT_PRICE);
        request.setBrand("TestBrand");
        request.setColor("Red");
        request.setCondition("New");
        request.setStock(DEFAULT_STOCK_QUANTITY);
        request.setIsDeleted(false);
        return request;
    }

    protected Product createTestProduct() {
        return createTestProduct(DEFAULT_PRODUCT_ID);
    }

    protected Product createTestProduct(Long productId) {
        Product product = new Product();
        product.setProductId(productId);
        product.setClientId(DEFAULT_CLIENT_ID);
        product.setTitle(DEFAULT_PRODUCT_NAME);
        product.setPrice(DEFAULT_PRODUCT_PRICE);
        product.setBrand("TestBrand");
        product.setColor("Red");
        product.setCondition("New");
        product.setIsDeleted(false);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setCreatedUser(DEFAULT_CREATED_USER);
        product.setModifiedUser(DEFAULT_CREATED_USER);
        return product;
    }

    protected Product createDeletedTestProduct() {
        Product product = createTestProduct();
        product.setIsDeleted(true);
        return product;
    }

    // ==================== TODO REQUEST & ENTITY FACTORY METHODS ====================

    protected TodoRequestModel createValidTodoRequest() {
        return createValidTodoRequest(DEFAULT_TODO_ID);
    }

    protected TodoRequestModel createValidTodoRequest(Long todoId) {
        TodoRequestModel request = new TodoRequestModel();
        request.setTodoId(todoId);
        request.setTitle(DEFAULT_TODO_TITLE);
        request.setDescription(DEFAULT_TODO_DESCRIPTION);
        request.setCompleted(DEFAULT_TODO_COMPLETED);
        request.setIsDeleted(false);
        request.setClientId(DEFAULT_CLIENT_ID);
        request.setUserId(DEFAULT_USER_ID);
        return request;
    }

    protected Todo createTestTodo() {
        return createTestTodo(DEFAULT_TODO_ID);
    }

    protected Todo createTestTodo(Long todoId) {
        Todo todo = new Todo();
        todo.setTodoId(todoId);
        todo.setTitle(DEFAULT_TODO_TITLE);
        todo.setDescription(DEFAULT_TODO_DESCRIPTION);
        todo.setCompleted(DEFAULT_TODO_COMPLETED);
        todo.setIsDeleted(false);
        todo.setClientId(DEFAULT_CLIENT_ID);
        todo.setUserId(DEFAULT_USER_ID);
        todo.setCreatedAt(LocalDateTime.now());
        todo.setUpdatedAt(LocalDateTime.now());
        todo.setCreatedUser(DEFAULT_CREATED_USER);
        todo.setModifiedUser(DEFAULT_CREATED_USER);
        return todo;
    }

    protected Todo createDeletedTestTodo() {
        Todo todo = createTestTodo();
        todo.setIsDeleted(true);
        return todo;
    }

    // ==================== PICKUP LOCATION REQUEST & ENTITY FACTORY METHODS ====================

    protected PickupLocationRequestModel createValidPickupLocationRequest() {
        return createValidPickupLocationRequest(DEFAULT_PICKUP_LOCATION_ID, DEFAULT_CLIENT_ID);
    }

    protected PickupLocationRequestModel createValidPickupLocationRequest(Long pickupLocationId, Long clientId) {
        PickupLocationRequestModel request = new PickupLocationRequestModel();
        request.setPickupLocationId(pickupLocationId);
        request.setClientId(clientId);
        request.setLocationName(DEFAULT_LOCATION_NAME);
        request.setAddress(createValidAddressRequest());
        request.setIsDeleted(false);
        return request;
    }

    protected PickupLocation createTestPickupLocation() {
        return createTestPickupLocation(DEFAULT_PICKUP_LOCATION_ID);
    }

    protected PickupLocation createTestPickupLocation(Long pickupLocationId) {
        PickupLocation location = new PickupLocation();
        location.setPickupLocationId(pickupLocationId);
        location.setClientId(DEFAULT_CLIENT_ID);
        location.setLocationName(DEFAULT_LOCATION_NAME);
        location.setAddressId(DEFAULT_ADDRESS_ID);
        location.setIsDeleted(false);
        location.setCreatedAt(LocalDateTime.now());
        location.setUpdatedAt(LocalDateTime.now());
        location.setCreatedUser(DEFAULT_CREATED_USER);
        location.setModifiedUser(DEFAULT_CREATED_USER);
        return location;
    }

    protected PickupLocation createDeletedTestPickupLocation() {
        PickupLocation location = createTestPickupLocation();
        location.setIsDeleted(true);
        return location;
    }

    // ==================== PACKAGE REQUEST & ENTITY FACTORY METHODS ====================

    protected PackageRequestModel createValidPackageRequest() {
        return createValidPackageRequest(DEFAULT_PACKAGE_ID, DEFAULT_CLIENT_ID);
    }

    protected PackageRequestModel createValidPackageRequest(Long packageId, Long clientId) {
        PackageRequestModel request = new PackageRequestModel();
        request.setPackageId(packageId);
        request.setClientId(clientId);
        request.setPackageName(DEFAULT_PACKAGE_NAME);
        request.setPrice(DEFAULT_PACKAGE_PRICE);
        request.setLength(10.0);
        request.setBreadth(10.0);
        request.setHeight(10.0);
        request.setMaxWeight(new BigDecimal("5.00"));
        request.setIsDeleted(false);
        return request;
    }

    protected Package createTestPackage() {
        return createTestPackage(DEFAULT_PACKAGE_ID);
    }

    protected Package createTestPackage(Long packageId) {
        Package pkg = new Package();
        pkg.setPackageId(packageId);
        pkg.setClientId(DEFAULT_CLIENT_ID);
        pkg.setPackageName(DEFAULT_PACKAGE_NAME);
        pkg.setPrice(DEFAULT_PACKAGE_PRICE);
        pkg.setLength(10.0);
        pkg.setBreadth(10.0);
        pkg.setHeight(10.0);
        pkg.setMaxWeight(new BigDecimal("5.00"));
        pkg.setIsDeleted(false);
        pkg.setCreatedAt(LocalDateTime.now());
        pkg.setUpdatedAt(LocalDateTime.now());
        pkg.setCreatedUser(DEFAULT_CREATED_USER);
        pkg.setModifiedUser(DEFAULT_CREATED_USER);
        return pkg;
    }

    protected Package createDeletedTestPackage() {
        Package pkg = createTestPackage();
        pkg.setIsDeleted(true);
        return pkg;
    }

    // ==================== PRODUCT REVIEW FACTORY METHODS ====================

    protected ProductReviewRequestModel createValidProductReviewRequest() {
        return createValidProductReviewRequest(DEFAULT_REVIEW_ID);
    }

    protected ProductReviewRequestModel createValidProductReviewRequest(Long reviewId) {
        ProductReviewRequestModel request = new ProductReviewRequestModel();
        request.setReviewId(reviewId);
        request.setProductId(DEFAULT_PRODUCT_ID);
        request.setUserId(DEFAULT_USER_ID);
        request.setRating(DEFAULT_RATING);
        request.setReviewText(DEFAULT_REVIEW_TEXT);
        request.setIsDeleted(false);
        return request;
    }

    protected ProductReview createTestProductReview() {
        return createTestProductReview(DEFAULT_REVIEW_ID);
    }

    protected ProductReview createTestProductReview(Long reviewId) {
        ProductReview review = new ProductReview();
        review.setReviewId(reviewId);
        review.setProductId(DEFAULT_PRODUCT_ID);
        review.setUserId(DEFAULT_USER_ID);
        review.setRating(DEFAULT_RATING);
        review.setReviewText(DEFAULT_REVIEW_TEXT);
        review.setIsDeleted(false);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        review.setCreatedUser(DEFAULT_CREATED_USER);
        review.setModifiedUser(DEFAULT_CREATED_USER);
        return review;
    }

    protected ProductReview createDeletedTestProductReview() {
        ProductReview review = createTestProductReview();
        review.setIsDeleted(true);
        return review;
    }

    // ==================== USER GROUP FACTORY METHODS ====================

    protected UserGroupRequestModel createValidUserGroupRequest() {
        return createValidUserGroupRequest(DEFAULT_USER_GROUP_ID, DEFAULT_CLIENT_ID);
    }

    protected UserGroupRequestModel createValidUserGroupRequest(Long groupId, Long clientId) {
        UserGroupRequestModel request = new UserGroupRequestModel();
        request.setUserGroupId(groupId);
        request.setClientId(clientId);
        request.setName(DEFAULT_USER_GROUP_NAME);
        request.setDescription(DEFAULT_USER_GROUP_DESCRIPTION);
        request.setIsDeleted(false);
        return request;
    }

    protected UserGroup createTestUserGroup() {
        return createTestUserGroup(DEFAULT_USER_GROUP_ID);
    }

    protected UserGroup createTestUserGroup(Long groupId) {
        UserGroup group = new UserGroup();
        group.setUserGroupId(groupId);
        group.setClientId(DEFAULT_CLIENT_ID);
        group.setName(DEFAULT_USER_GROUP_NAME);
        group.setDescription(DEFAULT_USER_GROUP_DESCRIPTION);
        group.setIsDeleted(false);
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());
        group.setCreatedUser(DEFAULT_CREATED_USER);
        group.setModifiedUser(DEFAULT_CREATED_USER);
        return group;
    }

    protected UserGroup createDeletedTestUserGroup() {
        UserGroup group = createTestUserGroup();
        group.setIsDeleted(true);
        return group;
    }

    // ==================== SHIPMENT FACTORY METHODS ====================

    protected ShipmentRequestModel createValidShipmentRequest(Long shipmentId, Long purchaseOrderId) {
        ShipmentRequestModel request = new ShipmentRequestModel();
        request.setShipmentId(shipmentId);
        request.setPurchaseOrderId(purchaseOrderId);
        request.setStatus(DEFAULT_SHIPMENT_STATUS);
        request.setWeight(DEFAULT_WEIGHT);
        request.setTrackingNumber(DEFAULT_TRACKING_NUMBER);
        request.setIsDeleted(false);
        return request;
    }

    protected Shipment createTestShipment() {
        return createTestShipment(DEFAULT_SHIPMENT_ID);
    }

    protected Shipment createTestShipment(Long shipmentId) {
        Shipment shipment = new Shipment();
        shipment.setShipmentId(shipmentId);
        shipment.setPurchaseOrderId(DEFAULT_PURCHASE_ORDER_ID);
        shipment.setStatus(DEFAULT_SHIPMENT_STATUS);
        shipment.setWeight(DEFAULT_WEIGHT);
        shipment.setTrackingNumber(DEFAULT_TRACKING_NUMBER);
        shipment.setIsDeleted(false);
        shipment.setCreatedAt(LocalDateTime.now());
        shipment.setUpdatedAt(LocalDateTime.now());
        shipment.setCreatedUser(DEFAULT_CREATED_USER);
        shipment.setModifiedUser(DEFAULT_CREATED_USER);
        return shipment;
    }

    protected Shipment createDeletedTestShipment() {
        Shipment shipment = createTestShipment();
        shipment.setIsDeleted(true);
        return shipment;
    }
}
