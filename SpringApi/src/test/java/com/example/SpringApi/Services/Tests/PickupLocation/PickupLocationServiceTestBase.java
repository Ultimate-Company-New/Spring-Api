package com.example.SpringApi.Services.Tests.PickupLocation;

import com.example.SpringApi.Authentication.Authorization;
import com.example.SpringApi.FilterQueryBuilder.PickupLocationFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.Address;
import com.example.SpringApi.Models.DatabaseModels.PickupLocation;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.PickupLocationRequestModel;
import com.example.SpringApi.Models.ResponseModels.ClientResponseModel;
import com.example.SpringApi.Models.ShippingResponseModel.AddPickupLocationResponseModel;
import com.example.SpringApi.Repositories.AddressRepository;
import com.example.SpringApi.Repositories.PackagePickupLocationMappingRepository;
import com.example.SpringApi.Repositories.PickupLocationRepository;
import com.example.SpringApi.Repositories.ProductPickupLocationMappingRepository;
import com.example.SpringApi.Services.ClientService;
import com.example.SpringApi.Services.PickupLocationService;
import com.example.SpringApi.Services.Tests.BaseTest;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Helpers.ShippingHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;

/**
 * Base test class for PickupLocationService tests.
 * Contains common mocks, dependencies, and setup logic shared across all
 * PickupLocationService test classes.
 */
@ExtendWith(MockitoExtension.class)
public abstract class PickupLocationServiceTestBase extends BaseTest {

    @Mock
    protected PickupLocationRepository pickupLocationRepository;

    @Mock
    protected AddressRepository addressRepository;

    @Mock
    protected ProductPickupLocationMappingRepository productMappingRepository;

    @Mock
    protected PackagePickupLocationMappingRepository packageMappingRepository;

    @Mock
    protected UserLogService userLogService;

    @Mock
    protected ClientService clientService;

    @Mock
    protected ShippingHelper shippingHelper;

    @Mock
    protected PickupLocationFilterQueryBuilder pickupLocationFilterQueryBuilder;

    @Mock
    protected HttpServletRequest request;

    @Mock
    protected Authorization authorization;

    @InjectMocks
    protected PickupLocationService pickupLocationService;

    protected PickupLocation testPickupLocation;
    protected Address testAddress;
    protected PickupLocationRequestModel testPickupLocationRequest;
    protected PaginationBaseRequestModel testPaginationRequest;
    protected AddPickupLocationResponseModel testShipRocketResponse;

    protected static final Long TEST_PICKUP_LOCATION_ID = DEFAULT_PICKUP_LOCATION_ID;
    protected static final Long TEST_ADDRESS_ID = DEFAULT_ADDRESS_ID;
    protected static final Long TEST_CLIENT_ID = 1L;
    protected static final Long TEST_SHIPROCKET_ID = 300L;
    protected static final String TEST_ADDRESS_NICKNAME = "Home Warehouse";
    protected static final String TEST_STREET_ADDRESS = "123 Main St";
    protected static final String TEST_CITY = "New York";
    protected static final String TEST_STATE = "NY";
    protected static final String TEST_POSTAL_CODE = "10001";
    protected static final String TEST_COUNTRY = "USA";
    protected static final String CREATED_USER = DEFAULT_CREATED_USER;

    @BeforeEach
    void setUp() {
        AddressRequestModel addressRequest = new AddressRequestModel();
        addressRequest.setStreetAddress(TEST_STREET_ADDRESS);
        addressRequest.setCity(TEST_CITY);
        addressRequest.setState(TEST_STATE);
        addressRequest.setPostalCode(TEST_POSTAL_CODE);
        addressRequest.setCountry(TEST_COUNTRY);
        addressRequest.setAddressType("WAREHOUSE");
        addressRequest.setNameOnAddress("John Doe");
        addressRequest.setEmailOnAddress("john@example.com");
        addressRequest.setPhoneOnAddress("1234567890");

        testPickupLocationRequest = new PickupLocationRequestModel();
        testPickupLocationRequest.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        testPickupLocationRequest.setAddressNickName(TEST_ADDRESS_NICKNAME);
        testPickupLocationRequest.setPickupLocationAddressId(TEST_ADDRESS_ID);
        testPickupLocationRequest.setShipRocketPickupLocationId(TEST_SHIPROCKET_ID);
        testPickupLocationRequest.setAddress(addressRequest);
        testPickupLocationRequest.setIsDeleted(false);

        testAddress = new Address(addressRequest, CREATED_USER);
        testAddress.setAddressId(TEST_ADDRESS_ID);

        testPickupLocation = new PickupLocation(testPickupLocationRequest, CREATED_USER, TEST_CLIENT_ID);
        testPickupLocation.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        testPickupLocation.setShipRocketPickupLocationId(TEST_SHIPROCKET_ID);

        testShipRocketResponse = new AddPickupLocationResponseModel();
        testShipRocketResponse.setPickup_id(TEST_SHIPROCKET_ID);

        ClientResponseModel mockClient = new ClientResponseModel();
        mockClient.setShipRocketEmail("test@example.com");
        mockClient.setShipRocketPassword("testpassword");
        lenient().when(clientService.getClientById(anyLong())).thenReturn(mockClient);

        testPaginationRequest = new PaginationBaseRequestModel();
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(10);
        testPaginationRequest.setIncludeDeleted(false);

        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("Authorization", "Bearer test-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
    }
}
