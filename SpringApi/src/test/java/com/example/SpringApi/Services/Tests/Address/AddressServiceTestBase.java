package com.example.SpringApi.Services.Tests.Address;

import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.DatabaseModels.Client;
import com.example.SpringApi.Models.DatabaseModels.User;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Repositories.AddressRepository;
import com.example.SpringApi.Repositories.ClientRepository;
import com.example.SpringApi.Repositories.UserRepository;
import com.example.SpringApi.Services.AddressService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Authentication.Authorization;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.SpringApi.Services.Tests.BaseTest;

import static org.mockito.Mockito.*;

/**
 * Base test class for AddressService unit tests.
 * Contains common mock objects, test data, and setup logic.
 */
@ExtendWith(MockitoExtension.class)
class AddressServiceTestBase extends BaseTest {

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

    @Mock
    protected Authorization authorization;

    @Spy
    @InjectMocks
    protected AddressService addressService;

    protected Address testAddress;
    protected AddressRequestModel testAddressRequest;
    protected Client testClient;
    protected User testUser;

    /**
     * Sets up test data before each test execution.
     * Initializes common test objects and configures mock behaviors.
     * Uses factory methods from BaseTest for consistent test data.
     */
    @BeforeEach
    void setUp() {
        // Initialize test objects using BaseTest factory methods
        testAddressRequest = createValidAddressRequest();
        testClient = createTestClient();
        testUser = createTestUser();
        testAddress = createTestAddress(testAddressRequest, DEFAULT_CREATED_USER);

        // Setup common mock behaviors
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");

        // Mock BaseService methods (required for service methods that call
        // getUser/getUserId)
        lenient().doReturn(DEFAULT_USER_ID).when(addressService).getUserId();
        lenient().doReturn(DEFAULT_LOGIN_NAME).when(addressService).getUser();
        lenient().doReturn(DEFAULT_CLIENT_ID).when(addressService).getClientId();
    }
}
