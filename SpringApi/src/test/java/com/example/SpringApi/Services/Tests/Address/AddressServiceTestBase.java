package com.example.SpringApi.Services.Tests.Address;

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
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Services.Tests.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Base test class for AddressService unit tests.
 * Centralizes common test data and stubbing logic.
 */
@ExtendWith(MockitoExtension.class)
abstract class AddressServiceTestBase extends BaseTest {

    @Mock
    protected AddressRepository addressRepository;

    @Mock
    protected ClientRepository clientRepository;

    @Mock
    protected UserRepository userRepository;

    @Mock
    protected UserLogService userLogService;

    @Mock
    protected HttpServletRequest request;

    @Spy
    @InjectMocks
    protected AddressService addressService;

    protected Address testAddress;
    protected AddressRequestModel testAddressRequest;
    protected Client testClient;
    protected User testUser;

    /**
     * Common setup for all address tests.
     * Rule 14: No inline mocks allowed here. Only data initialization.
     */
    @BeforeEach
    void setUp() {
        testAddressRequest = createValidAddressRequest();
        testClient = createTestClient();
        testUser = createTestUser();
        testAddress = createTestAddress(testAddressRequest, DEFAULT_CREATED_USER);
    }

    /**
     * Centralized stub for service-level token/user details.
     */
    protected void stubBaseServiceBehaviors() {
        lenient().doReturn(DEFAULT_USER_ID).when(addressService).getUserId();
        lenient().doReturn(DEFAULT_LOGIN_NAME).when(addressService).getUser();
        lenient().doReturn(DEFAULT_CLIENT_ID).when(addressService).getClientId();
    }

    /**
     * Centralized stub for request authorization header.
     */
    protected void stubRequestAuthorization() {
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
    }

    /**
     * Centralized stub for common repository findById success and failure cases.
     */
    protected void stubDefaultRepositoryResponses() {
        // Success cases
        lenient().when(addressRepository.findById(DEFAULT_ADDRESS_ID)).thenReturn(Optional.of(testAddress));
        lenient().when(clientRepository.findById(DEFAULT_CLIENT_ID)).thenReturn(Optional.of(testClient));
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

    /**
     * Centralized stub for repository save operation.
     */
    protected void stubAddressRepositorySave(Address returnAddress) {
        when(addressRepository.save(any(Address.class))).thenReturn(returnAddress);
    }

    /**
     * Centralized stub for address retrieval by ID.
     */
    protected void stubFindAddressById(long id, Optional<Address> result) {
        when(addressRepository.findById(id)).thenReturn(result);
    }

    /**
     * Centralized stub for client retrieval by ID.
     */
    protected void stubFindClientById(long id, Optional<Client> result) {
        when(clientRepository.findById(id)).thenReturn(result);
    }

    /**
     * Centralized stub for user retrieval by ID.
     */
    protected void stubFindUserById(long id, Optional<User> result) {
        when(userRepository.findById(id)).thenReturn(result);
    }

    /**
     * Centralized stub for finding addresses by Client ID.
     */
    protected void stubFindAddressesByClientId(long clientId, List<Address> result) {
        when(addressRepository.findByClientIdAndIsDeletedOrderByAddressIdDesc(clientId, false)).thenReturn(result);
    }

    /**
     * Centralized stub for finding addresses by User ID.
     */
    protected void stubFindAddressesByUserId(long userId, List<Address> result) {
        when(addressRepository.findByUserIdAndIsDeletedOrderByAddressIdDesc(userId, false)).thenReturn(result);
    }

    /**
     * Centralized stub for successful audit logging.
     */
    protected void stubUserLogSuccess() {
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
    }

    /**
     * Helper to assert a NotFoundException is thrown and the message matches.
     */
    protected void assertThrowsNotFound(String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
        NotFoundException ex = assertThrows(NotFoundException.class, executable);
        assertEquals(expectedMessage, ex.getMessage());
    }

    /**
     * Centralized stub for controller delegation tests - getAddressByClientId.
     */
    protected void stubServiceGetAddressByClientId(long clientId, List<AddressResponseModel> result) {
        lenient().doReturn(result).when(addressService).getAddressByClientId(clientId);
    }

    /**
     * Centralized stub for controller delegation tests - getAddressById.
     */
    protected void stubServiceGetAddressById(long addressId, AddressResponseModel result) {
        lenient().doReturn(result).when(addressService).getAddressById(addressId);
    }

    /**
     * Centralized stub for controller delegation tests - getAddressByUserId.
     */
    protected void stubServiceGetAddressByUserId(long userId, List<AddressResponseModel> result) {
        lenient().doReturn(result).when(addressService).getAddressByUserId(userId);
    }

    /**
     * Centralized stub for controller delegation tests - insertAddress.
     */
    protected void stubServiceInsertAddressDoNothing() {
        lenient().doNothing().when(addressService).insertAddress(any(AddressRequestModel.class));
    }

    /**
     * Centralized stub for controller delegation tests - updateAddress.
     */
    protected void stubServiceUpdateAddressDoNothing() {
        lenient().doNothing().when(addressService).updateAddress(any(AddressRequestModel.class));
    }

    /**
     * Centralized stub for controller delegation tests - toggleAddress.
     */
    protected void stubServiceToggleAddressDoNothing(long addressId) {
        lenient().doNothing().when(addressService).toggleAddress(addressId);
    }

    /**
     * Centralized stub for service-level getUser calls.
     */
    protected void stubServiceGetUser(String username) {
        lenient().doReturn(username).when(addressService).getUser();
    }
}
