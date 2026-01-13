package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.Lead;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.RequestModels.LeadRequestModel;

import java.time.LocalDateTime;

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
 * 
 * @BeforeEach
 *             void setUp() {
 *             testUser = createTestUser();
 *             testClient = createTestClient();
 *             }
 *             }
 *             ```
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
}
