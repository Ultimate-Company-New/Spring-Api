package com.example.SpringApi.ServiceTests.PickupLocation;

import com.example.SpringApi.Authentication.Authorization;
import com.example.SpringApi.FilterQueryBuilder.PickupLocationFilterQueryBuilder;
import com.example.SpringApi.Helpers.ShipRocketHelper;
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
import com.example.SpringApi.Services.UserLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Base test class for PickupLocationService tests.
 * Contains common mocks, dependencies, and setup logic shared across all
 * PickupLocationService test classes.
 */
@ExtendWith(MockitoExtension.class)
abstract class PickupLocationServiceTestBase {

    // ==================== COMMON TEST CONSTANTS ====================

    protected static final Long DEFAULT_PICKUP_LOCATION_ID = 1L;
    protected static final Long DEFAULT_ADDRESS_ID = 1L;
    protected static final String DEFAULT_CREATED_USER = "admin";

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
    protected ShipRocketHelper shipRocketHelper;

    @Mock
    protected PickupLocationFilterQueryBuilder pickupLocationFilterQueryBuilder;

    @Mock
    protected HttpServletRequest request;

    @Mock
    protected Authorization authorization;

    @InjectMocks
    protected PickupLocationService pickupLocationService;

    @Mock
    protected PickupLocationService pickupLocationServiceMock;

    protected PickupLocation testPickupLocation;
    protected Address testAddress;
    protected PickupLocationRequestModel testPickupLocationRequest;
    protected PaginationBaseRequestModel testPaginationRequest;
    protected AddPickupLocationResponseModel testShipRocketResponse;

    protected static final Long TEST_PICKUP_LOCATION_ID = DEFAULT_PICKUP_LOCATION_ID;
    protected static final Long TEST_ADDRESS_ID = DEFAULT_ADDRESS_ID;
    protected static final Long TEST_CLIENT_ID = 1L;
    protected static final Long TEST_USER_ID = 1L;
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
        stubAddressRepositoryFindById(TEST_ADDRESS_ID, testAddress);

        testPickupLocation = new PickupLocation(testPickupLocationRequest, CREATED_USER, TEST_CLIENT_ID);
        testPickupLocation.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        testPickupLocation.setShipRocketPickupLocationId(TEST_SHIPROCKET_ID);

        testShipRocketResponse = new AddPickupLocationResponseModel();
        testShipRocketResponse.setPickupId(TEST_SHIPROCKET_ID);

        testPaginationRequest = new PaginationBaseRequestModel();
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(10);
        testPaginationRequest.setIncludeDeleted(false);

        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("Authorization", "Bearer test-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

        // CRITICAL: Inject the mock shipRocketHelper into the service
        // The service's constructor sets shipRocketHelper = null, which overrides
        // @InjectMocks
        // We must manually inject it after construction to prevent real API calls
        org.springframework.test.util.ReflectionTestUtils.setField(
                pickupLocationService, "shipRocketHelper", shipRocketHelper);

        // Default ShipRocket helper stub to avoid null responses in tests
        stubShipRocketHelperAddPickupLocation(testShipRocketResponse);
    }

    // ==========================================
    // COMMON STUB SETUP HELPERS
    // ==========================================

    /**
     * Sets up all common stubs needed for successful pickup location creation.
     * Call this in success tests to avoid repetitive stub setup.
     */
    protected void stubSuccessfulPickupLocationCreation() {
        ClientResponseModel mockClient = new ClientResponseModel();
        mockClient.setShipRocketEmail("test@example.com");
        mockClient.setShipRocketPassword("testpassword");

        stubClientServiceGetClientById(TEST_CLIENT_ID, mockClient);
        stubAddressRepositorySave(testAddress);
        stubPickupLocationRepositorySave(testPickupLocation);
        stubShipRocketHelperAddPickupLocation(testShipRocketResponse);
    }

    // ==========================================
    // STUB METHODS
    // ==========================================

    /**
     * Stub for pickupLocationRepository.findPickupLocationByIdAndClientId
     */
    protected void stubPickupLocationRepositoryFindByIdAndClientId(Long pickupLocationId, Long clientId,
            PickupLocation pickupLocation) {
        lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(pickupLocationId, clientId))
                .thenReturn(pickupLocation);
    }

    /**
     * Stub for pickupLocationRepository.findPickupLocationByIdAndClientId returning
     * null
     */
    protected void stubPickupLocationRepositoryFindByIdAndClientIdNotFound(Long pickupLocationId, Long clientId) {
        lenient().when(pickupLocationRepository.findPickupLocationByIdAndClientId(pickupLocationId, clientId))
                .thenReturn(null);
    }

    /**
     * Stub for pickupLocationRepository.save
     */
    protected void stubPickupLocationRepositorySave(PickupLocation pickupLocation) {
        lenient().when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(pickupLocation);
    }

    protected void stubPickupLocationRepositorySaveThrows(RuntimeException exception) {
        lenient().when(pickupLocationRepository.save(any(PickupLocation.class))).thenThrow(exception);
    }

    protected void stubPickupLocationRepositorySaveReturnsNull() {
        lenient().when(pickupLocationRepository.save(any(PickupLocation.class))).thenReturn(null);
    }

    /**
     * Stub for addressRepository.save
     */
    protected void stubAddressRepositorySave(Address address) {
        lenient().when(addressRepository.save(any(Address.class))).thenReturn(address);
    }

    protected void stubAddressRepositorySaveThrows(RuntimeException exception) {
        when(addressRepository.save(any(Address.class))).thenThrow(exception);
    }

    protected void stubAddressRepositorySaveReturnsNull() {
        when(addressRepository.save(any(Address.class))).thenReturn(null);
    }

    /**
     * Stub for addressRepository.findById
     */
    protected void stubAddressRepositoryFindById(Long addressId, Address address) {
        lenient().when(addressRepository.findById(addressId)).thenReturn(java.util.Optional.of(address));
    }

    /**
     * Stub for addressRepository.findById returning empty
     */
    protected void stubAddressRepositoryFindByIdNotFound(Long addressId) {
        lenient().when(addressRepository.findById(addressId)).thenReturn(java.util.Optional.empty());
    }

    /**
     * Stub for shipRocketHelper.addPickupLocation
     */
    protected void stubShipRocketHelperAddPickupLocation(AddPickupLocationResponseModel response) {
        lenient().when(shipRocketHelper.addPickupLocation(any(PickupLocation.class))).thenReturn(response);
    }

    /**
     * Stub for clientService.getClientById
     */
    protected void stubClientServiceGetClientById(Long clientId, ClientResponseModel client) {
        lenient().when(clientService.getClientById(clientId)).thenReturn(client);
    }

    /**
     * Stub for
     * pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters
     */
    protected void stubPickupLocationFilterQueryBuilderFindPaginatedEntities(
            org.springframework.data.domain.Page<PickupLocation> page) {
        when(pickupLocationFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), any(), anyString(), any(), anyBoolean(), any()))
                .thenReturn(page);
    }

    protected void stubPickupLocationFilterQueryBuilderGetColumnType(String column, String columnType) {
        lenient().when(pickupLocationFilterQueryBuilder.getColumnType(eq(column))).thenReturn(columnType);
    }

    protected void stubPickupLocationServiceUserContext(Long userId, String userName, Long clientId) {
        lenient().when(pickupLocationServiceMock.getUserId()).thenReturn(userId);
        lenient().when(pickupLocationServiceMock.getUser()).thenReturn(userName);
        lenient().when(pickupLocationServiceMock.getClientId()).thenReturn(clientId);
    }

    protected void stubPickupLocationServiceBulkCreatePickupLocationsAsyncDoNothing() {
        lenient().doNothing().when(pickupLocationServiceMock)
                .bulkCreatePickupLocationsAsync(anyList(), anyLong(), anyString(), anyLong());
    }

    protected void stubPickupLocationServiceThrowsUnauthorized() {
        lenient().doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                .when(pickupLocationServiceMock).bulkCreatePickupLocationsAsync(anyList(), anyLong(), anyString(), anyLong());
    }

    protected void stubPickupLocationServiceGetPickupLocationByIdReturns(
            com.example.SpringApi.Models.ResponseModels.PickupLocationResponseModel result) {
        lenient().when(pickupLocationServiceMock.getPickupLocationById(anyLong())).thenReturn(result);
    }

    protected void stubPickupLocationServiceThrowsUnauthorizedOnGetById() {
        lenient().doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                .when(pickupLocationServiceMock).getPickupLocationById(anyLong());
    }

    protected void stubPickupLocationServiceThrowsUnauthorizedOnCreate() throws Exception {
        lenient().doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                .when(pickupLocationServiceMock).createPickupLocation(any());
    }

    protected void stubPickupLocationServiceCreatePickupLocationDoNothing() throws Exception {
        lenient().doNothing().when(pickupLocationServiceMock).createPickupLocation(any());
    }

    protected void stubPickupLocationServiceThrowsUnauthorizedOnUpdate() throws Exception {
        lenient().doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                .when(pickupLocationServiceMock).updatePickupLocation(any());
    }

    protected void stubPickupLocationServiceUpdatePickupLocationDoNothing() throws Exception {
        lenient().doNothing().when(pickupLocationServiceMock).updatePickupLocation(any());
    }

    protected void stubPickupLocationServiceThrowsUnauthorizedOnToggle() {
        lenient().doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                .when(pickupLocationServiceMock).togglePickupLocation(anyLong());
    }

    protected void stubPickupLocationServiceTogglePickupLocationDoNothing() {
        lenient().doNothing().when(pickupLocationServiceMock).togglePickupLocation(anyLong());
    }

    protected void stubPickupLocationServiceThrowsUnauthorizedOnGetBatches() {
        lenient().doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                .when(pickupLocationServiceMock).getPickupLocationsInBatches(any());
    }

    protected void stubPickupLocationServiceGetPickupLocationsInBatchesReturns(
            com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel<
                    com.example.SpringApi.Models.ResponseModels.PickupLocationResponseModel> result) {
        lenient().when(pickupLocationServiceMock.getPickupLocationsInBatches(any()))
                .thenReturn(result);
    }

    /**
     * Stub for userLogService.logData
     */
    protected void stubUserLogServiceLogData() {
        doNothing().when(userLogService).logData(anyLong(), anyString(), anyString());
    }

    /**
     * Stub for userLogService.logDataWithContext
     */
    protected void stubUserLogServiceLogDataWithContext() {
        doNothing().when(userLogService).logDataWithContext(anyLong(), anyString(), anyLong(), anyString(),
                anyString());
    }

    /**
     * Stub for productMappingRepository.countByPickupLocationIds
     */
    protected void stubProductMappingRepositoryCountByPickupLocationIds(java.util.List<Object[]> counts) {
        when(productMappingRepository.countByPickupLocationIds(any())).thenReturn(counts);
    }

    /**
     * Stub for packageMappingRepository.countByPickupLocationIds
     */
    protected void stubPackageMappingRepositoryCountByPickupLocationIds(java.util.List<Object[]> counts) {
        when(packageMappingRepository.countByPickupLocationIds(any())).thenReturn(counts);
    }

    /**
     * Stub for productMappingRepository.deleteByPickupLocationId
     */
    protected void stubProductMappingRepositoryDeleteByPickupLocationId() {
        doNothing().when(productMappingRepository).deleteByPickupLocationId(anyLong());
    }

    /**
     * Stub for packageMappingRepository.deleteByPickupLocationId
     */
    protected void stubPackageMappingRepositoryDeleteByPickupLocationId() {
        doNothing().when(packageMappingRepository).deleteByPickupLocationId(anyLong());
    }

    /**
     * Stub for productMappingRepository.saveAll
     */
    protected void stubProductMappingRepositorySaveAll() {
        when(productMappingRepository.saveAll(any())).thenReturn(java.util.Collections.emptyList());
    }

    /**
     * Stub for packageMappingRepository.saveAll
     */
    protected void stubPackageMappingRepositorySaveAll() {
        when(packageMappingRepository.saveAll(any())).thenReturn(java.util.Collections.emptyList());
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    /**
     * Helper method to create a valid PickupLocationRequestModel for bulk tests.
     */
    protected PickupLocationRequestModel createValidPickupLocationRequest(Long id) {
        AddressRequestModel addressRequest = new AddressRequestModel();
        addressRequest.setStreetAddress("Street " + id);
        addressRequest.setCity("City " + id);
        addressRequest.setState("NY");
        addressRequest.setPostalCode("10001");
        addressRequest.setCountry("USA");
        addressRequest.setAddressType("WAREHOUSE");
        addressRequest.setNameOnAddress("Name " + id);
        addressRequest.setEmailOnAddress("email" + id + "@example.com");
        addressRequest.setPhoneOnAddress("1234567890");

        PickupLocationRequestModel locationRequest = new PickupLocationRequestModel();
        locationRequest.setPickupLocationId(id);
        locationRequest.setAddressNickName("Nick " + id);
        locationRequest.setAddress(addressRequest);
        locationRequest.setIsDeleted(false);
        return locationRequest;
    }
}
