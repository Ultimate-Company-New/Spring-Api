package com.example.SpringApi.ServiceTests.Address;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.ResponseModels.AddressResponseModel;
import com.example.SpringApi.Repositories.AddressRepository;
import com.example.SpringApi.Repositories.ClientRepository;
import com.example.SpringApi.Repositories.UserRepository;
import com.example.SpringApi.Services.AddressService;
import com.example.SpringApi.Services.UserLogService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Base test class for AddressService unit tests. Centralizes common test data and stubbing logic.
 */
@ExtendWith(MockitoExtension.class)
abstract class AddressServiceTestBase {

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

  @Mock protected AddressRepository addressRepository;

  @Mock protected ClientRepository clientRepository;

  @Mock protected UserRepository userRepository;

  @Mock protected UserLogService userLogService;

  @Mock protected HttpServletRequest request;

  @Spy @InjectMocks protected AddressService addressService;

  protected Address testAddress;
  protected AddressRequestModel testAddressRequest;
  protected Client testClient;
  protected User testUser;

  // Controller under test for permission tests (Rule 3)
  protected com.example.SpringApi.Controllers.AddressController addressController;

  /**
   * Common setup for all address tests. Rule 14: No inline mocks allowed here. Only data
   * initialization.
   */
  @BeforeEach
  void setUp() {
    testAddressRequest = createValidAddressRequest();
    testClient = createTestClient();
    testUser = createTestUser();
    testAddress = createTestAddress(testAddressRequest, DEFAULT_CREATED_USER);

    // Initialize controller with spied service for local permission tests
    addressController = new com.example.SpringApi.Controllers.AddressController(addressService);
  }

  /** Centralized stub for service-level token/user details. */
  protected void stubBaseServiceBehaviors() {
    lenient().doReturn(DEFAULT_USER_ID).when(addressService).getUserId();
    lenient().doReturn(DEFAULT_LOGIN_NAME).when(addressService).getUser();
    lenient().doReturn(DEFAULT_CLIENT_ID).when(addressService).getClientId();
  }

  /** Centralized stub for request authorization header. */
  protected void stubRequestAuthorization() {
    lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
  }

  /** Centralized stub for common repository findById success and failure cases. */
  protected void stubDefaultRepositoryResponses() {
    // Success cases
    lenient()
        .when(addressRepository.findById(DEFAULT_ADDRESS_ID))
        .thenReturn(Optional.of(testAddress));
    lenient()
        .when(clientRepository.findById(DEFAULT_CLIENT_ID))
        .thenReturn(Optional.of(testClient));
    lenient().when(userRepository.findById(DEFAULT_USER_ID)).thenReturn(Optional.of(testUser));

    // Failure / Special ID cases
    lenient().when(addressRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());
    lenient().when(addressRepository.findById(Long.MIN_VALUE)).thenReturn(Optional.empty());
    lenient().when(addressRepository.findById(-1L)).thenReturn(Optional.empty());
    lenient().when(addressRepository.findById(0L)).thenReturn(Optional.empty());

    lenient().when(clientRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());
    lenient().when(clientRepository.findById(Long.MIN_VALUE)).thenReturn(Optional.empty());
    lenient().when(clientRepository.findById(-1L)).thenReturn(Optional.empty());
    lenient().when(clientRepository.findById(0L)).thenReturn(Optional.empty());

    lenient().when(userRepository.findById(Long.MAX_VALUE)).thenReturn(Optional.empty());
    lenient().when(userRepository.findById(Long.MIN_VALUE)).thenReturn(Optional.empty());
    lenient().when(userRepository.findById(-1L)).thenReturn(Optional.empty());
    lenient().when(userRepository.findById(0L)).thenReturn(Optional.empty());
  }

  /** Centralized stub for repository save operation. */
  protected void stubAddressRepositorySave(Address returnAddress) {
    when(addressRepository.save(any(Address.class))).thenReturn(returnAddress);
  }

  /** Centralized stub for address retrieval by ID. */
  protected void stubFindAddressById(long id, Optional<Address> result) {
    when(addressRepository.findById(id)).thenReturn(result);
  }

  /** Centralized stub for client retrieval by ID. */
  protected void stubFindClientById(long id, Optional<Client> result) {
    when(clientRepository.findById(id)).thenReturn(result);
  }

  /** Centralized stub for user retrieval by ID. */
  protected void stubFindUserById(long id, Optional<User> result) {
    when(userRepository.findById(id)).thenReturn(result);
  }

  /** Centralized stub for finding addresses by Client ID. */
  protected void stubFindAddressesByClientId(long clientId, List<Address> result) {
    when(addressRepository.findByClientIdAndIsDeletedOrderByAddressIdDesc(clientId, false))
        .thenReturn(result);
  }

  /** Centralized stub for finding addresses by User ID. */
  protected void stubFindAddressesByUserId(long userId, List<Address> result) {
    when(addressRepository.findByUserIdAndIsDeletedOrderByAddressIdDesc(userId, false))
        .thenReturn(result);
  }

  /** Centralized stub for successful audit logging. */
  protected void stubUserLogSuccess() {
    lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
  }

  /** Centralized stub for controller delegation tests - getAddressByClientId. */
  protected void stubServiceGetAddressByClientId(long clientId, List<AddressResponseModel> result) {
    lenient().doReturn(result).when(addressService).getAddressByClientId(clientId);
  }

  /** Centralized stub for controller delegation tests - getAddressById. */
  protected void stubServiceGetAddressById(long addressId, AddressResponseModel result) {
    lenient().doReturn(result).when(addressService).getAddressById(addressId);
  }

  /** Centralized stub for controller delegation tests - getAddressByUserId. */
  protected void stubServiceGetAddressByUserId(long userId, List<AddressResponseModel> result) {
    lenient().doReturn(result).when(addressService).getAddressByUserId(userId);
  }

  /** Centralized stub for controller delegation tests - insertAddress. */
  protected void stubServiceInsertAddressDoNothing() {
    lenient().doNothing().when(addressService).insertAddress(any(AddressRequestModel.class));
  }

  /** Centralized stub for controller delegation tests - updateAddress. */
  protected void stubServiceUpdateAddressDoNothing() {
    lenient().doNothing().when(addressService).updateAddress(any(AddressRequestModel.class));
  }

  /** Centralized stub for controller delegation tests - toggleAddress. */
  protected void stubServiceToggleAddressDoNothing(long addressId) {
    lenient().doNothing().when(addressService).toggleAddress(addressId);
  }

  /** Centralized stub for service-level getUser calls. */
  protected void stubServiceGetUser(String username) {
    lenient().doReturn(username).when(addressService).getUser();
  }

  protected void assertThrowsBadRequest(
      String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
    BadRequestException ex = assertThrows(BadRequestException.class, executable);
    assertEquals(expectedMessage, ex.getMessage());
  }

  protected void assertThrowsNotFound(
      String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
    NotFoundException ex = assertThrows(NotFoundException.class, executable);
    assertEquals(expectedMessage, ex.getMessage());
  }

  // ==================== FACTORY METHODS ====================

  protected User createTestUser() {
    return createTestUser(DEFAULT_USER_ID);
  }

  protected User createTestUser(Long userId) {
    return createTestUser(userId, DEFAULT_LOGIN_NAME, DEFAULT_EMAIL);
  }

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

  protected Client createTestClient() {
    return createTestClient(DEFAULT_CLIENT_ID);
  }

  protected Client createTestClient(Long clientId) {
    return createTestClient(clientId, DEFAULT_CLIENT_NAME);
  }

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

  protected AddressRequestModel createValidAddressRequest() {
    return createValidAddressRequest(DEFAULT_ADDRESS_ID, DEFAULT_USER_ID, DEFAULT_CLIENT_ID);
  }

  protected AddressRequestModel createValidAddressRequest(
      Long addressId, Long userId, Long clientId) {
    AddressRequestModel addressRequest = new AddressRequestModel();
    addressRequest.setId(addressId);
    addressRequest.setUserId(userId);
    addressRequest.setClientId(clientId);
    addressRequest.setAddressType(DEFAULT_ADDRESS_TYPE);
    addressRequest.setStreetAddress(DEFAULT_STREET_ADDRESS);
    addressRequest.setCity(DEFAULT_CITY);
    addressRequest.setState(DEFAULT_STATE);
    addressRequest.setPostalCode(DEFAULT_POSTAL_CODE);
    addressRequest.setCountry(DEFAULT_COUNTRY);
    addressRequest.setIsPrimary(true);
    addressRequest.setIsDeleted(false);
    return addressRequest;
  }

  protected Address createTestAddress(AddressRequestModel addressRequest, String createdUser) {
    Address address = new Address(addressRequest, createdUser);
    address.setAddressId(addressRequest.getId());
    address.setIsDeleted(false);
    address.setCreatedAt(LocalDateTime.now());
    address.setUpdatedAt(LocalDateTime.now());
    return address;
  }

  protected Address createTestAddress() {
    return createTestAddress(createValidAddressRequest(), DEFAULT_CREATED_USER);
  }

  protected Address createTestAddress(Long addressId) {
    AddressRequestModel addressRequest = createValidAddressRequest();
    addressRequest.setId(addressId);
    return createTestAddress(addressRequest, DEFAULT_CREATED_USER);
  }

  protected Address createTestAddress(Long addressId, String addressType) {
    AddressRequestModel addressRequest = createValidAddressRequest();
    addressRequest.setId(addressId);
    addressRequest.setAddressType(addressType);
    return createTestAddress(addressRequest, DEFAULT_CREATED_USER);
  }
}

