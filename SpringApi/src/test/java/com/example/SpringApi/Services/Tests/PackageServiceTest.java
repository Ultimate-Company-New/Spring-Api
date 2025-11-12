package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.Package;
import com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping;
import com.example.SpringApi.Models.RequestModels.PackageRequestModel;
import com.example.SpringApi.Models.RequestModels.AddressRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.ResponseModels.PackageResponseModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Repositories.PackageRepository;
import com.example.SpringApi.Repositories.PackagePickupLocationMappingRepository;
import com.example.SpringApi.Services.PackageService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.ApiRoutes;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PackageService.
 *
 * <p>This test class provides comprehensive coverage of PackageService methods including:
 * - CRUD operations (create, read, update, toggle)
 * - Package retrieval by ID, batch operations, and pickup location
 * - Validation and error handling
 * - Pagination and filtering
 *
 * Each test method follows the AAA (Arrange-Act-Assert) pattern and includes
 * both success and failure scenarios to ensure robust error handling.
 * All external dependencies are properly mocked to ensure test isolation.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PackageService Unit Tests")
class PackageServiceTest {

    @Mock
    private PackageRepository packageRepository;

    @Mock
    private UserLogService userLogService;

    @Mock
    private PackagePickupLocationMappingRepository packagePickupLocationMappingRepository;

    @Mock
    private com.example.SpringApi.Repositories.PickupLocationRepository pickupLocationRepository;

    @Mock
    private HttpServletRequest request;

    @Spy
    @InjectMocks
    private PackageService packageService;

    private Package testPackage;
    private PackageRequestModel testPackageRequest;
    private AddressRequestModel testAddress;
    private PaginationBaseRequestModel testPaginationRequest;
    private PackagePickupLocationMapping testMapping;

    private static final Long TEST_PACKAGE_ID = 1L;
    private static final Long TEST_PICKUP_LOCATION_ID = 2L;
    private static final Long TEST_CLIENT_ID = 100L;
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_PACKAGE_NAME = "Test Package";
    private static final String TEST_PACKAGE_TYPE = "Box";
    private static final String CREATED_USER = "testuser";
    private static final String TEST_STREET_ADDRESS = "123 Test St";
    private static final String TEST_CITY = "Test City";
    private static final String TEST_STATE = "Test State";
    private static final String TEST_POSTAL_CODE = "12345";

    /**
     * Sets up test data before each test execution.
     * Initializes common test objects and configures mock behaviors.
     */
    @BeforeEach
    void setUp() {
        // Mock BaseService methods
        lenient().doReturn(CREATED_USER).when(packageService).getUser();
        lenient().doReturn(TEST_USER_ID).when(packageService).getUserId();
        lenient().doReturn(TEST_CLIENT_ID).when(packageService).getClientId();

        // Initialize test data
        initializeTestData();

        // Create test package from request model (simulating service behavior)
        testPackage = createPackageFromRequest(testPackageRequest);

        // Initialize test mapping (needs testPackage to be created first)
        testMapping = new PackagePickupLocationMapping();
        testMapping.setPackagePickupLocationMappingId(1L);
        testMapping.setPackageId(TEST_PACKAGE_ID);
        testMapping.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        testMapping.setAvailableQuantity(10);
        testMapping.setReorderLevel(5);
        testMapping.setMaxStockLevel(20);
        testMapping.setPackageEntity(testPackage);

        // Setup common mock behaviors
        lenient().when(packageRepository.save(any(Package.class))).thenReturn(testPackage);
    }

    /**
     * Initializes test data objects used across multiple tests.
     */
    private void initializeTestData() {
        // Initialize test package request first
        testPackageRequest = new PackageRequestModel();
        testPackageRequest.setPackageId(TEST_PACKAGE_ID);
        testPackageRequest.setPackageName(TEST_PACKAGE_NAME);
        testPackageRequest.setPackageType(TEST_PACKAGE_TYPE);
        testPackageRequest.setLength(10);
        testPackageRequest.setBreadth(10);
        testPackageRequest.setHeight(10);
        testPackageRequest.setMaxWeight(BigDecimal.valueOf(5.0));
        testPackageRequest.setStandardCapacity(1);
        testPackageRequest.setPricePerUnit(BigDecimal.valueOf(10.0));
        testPackageRequest.setClientId(TEST_CLIENT_ID);
        testPackageRequest.setIsDeleted(false);

        // Initialize test address
        testAddress = new AddressRequestModel();
        testAddress.setStreetAddress(TEST_STREET_ADDRESS);
        testAddress.setCity(TEST_CITY);
        testAddress.setState(TEST_STATE);
        testAddress.setPostalCode(TEST_POSTAL_CODE);
        testAddress.setCountry("USA");
        testAddress.setAddressType("HOME");
        testPackageRequest.setAddress(testAddress);

        // Initialize test pagination request
        testPaginationRequest = new PaginationBaseRequestModel();
        testPaginationRequest.setColumnName("packageId");
        testPaginationRequest.setCondition("equals");
        testPaginationRequest.setFilterExpr("1");
        testPaginationRequest.setIncludeDeleted(false);
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(10);
    }

    /**
     * Creates a test Package entity from the PackageRequestModel, simulating service behavior.
     */
    private Package createPackageFromRequest(PackageRequestModel request) {
        Package pkg = new Package();
        pkg.setPackageId(request.getPackageId());
        pkg.setPackageName(request.getPackageName());
        pkg.setLength(request.getLength());
        pkg.setBreadth(request.getBreadth());
        pkg.setHeight(request.getHeight());
        pkg.setMaxWeight(request.getMaxWeight());
        pkg.setStandardCapacity(request.getStandardCapacity());
        pkg.setPricePerUnit(request.getPricePerUnit());
        pkg.setPackageType(request.getPackageType());
        pkg.setClientId(request.getClientId());
        pkg.setIsDeleted(request.getIsDeleted() != null ? request.getIsDeleted() : Boolean.FALSE);
        pkg.setCreatedUser(CREATED_USER);
        pkg.setModifiedUser(CREATED_USER);
        return pkg;
    }

    // ==================== getPackagesInBatches Tests ====================

    @Test
    @DisplayName("getPackagesInBatches - Success: Valid pagination request")
    void testGetPackagesInBatches_Success() {
        // Arrange
        List<Package> packageList = Arrays.asList(testPackage);
        Page<Package> packagePage = new PageImpl<>(packageList, PageRequest.of(0, 10, Sort.by("packageId")), 1);

        when(packageRepository.findPaginatedPackages(
            eq(TEST_CLIENT_ID), eq("packageId"), eq("equals"), eq("1"), eq(false), any(Pageable.class)
        )).thenReturn(packagePage);

        // Act
        PaginationBaseResponseModel<PackageResponseModel> result = packageService.getPackagesInBatches(testPaginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1, result.getTotalDataCount());
        assertEquals(TEST_PACKAGE_NAME, result.getData().get(0).getPackageName());
    }

    @Test
    @DisplayName("getPackagesInBatches - Success: Empty result")
    void testGetPackagesInBatches_EmptyResult() {
        // Arrange
        Page<Package> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10, Sort.by("packageId")), 0);

        when(packageRepository.findPaginatedPackages(
            eq(TEST_CLIENT_ID), eq("packageId"), eq("equals"), eq("1"), eq(false), any(Pageable.class)
        )).thenReturn(emptyPage);

        // Act
        PaginationBaseResponseModel<PackageResponseModel> result = packageService.getPackagesInBatches(testPaginationRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getTotalDataCount());
    }

    @Test
    @DisplayName("getPackagesInBatches - Failure: Invalid column name")
    void testGetPackagesInBatches_InvalidColumnName() {
        // Arrange
        testPaginationRequest.setColumnName("invalidColumn");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> packageService.getPackagesInBatches(testPaginationRequest));

        assertTrue(exception.getMessage().contains("Invalid column name for filtering"));
    }

    // ==================== getPackageById Tests ====================

    @Test
    @DisplayName("getPackageById - Success: Valid package ID")
    void testGetPackageById_Success() {
        // Arrange
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);

        // Act
        PackageResponseModel result = packageService.getPackageById(TEST_PACKAGE_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_PACKAGE_ID, result.getPackageId());
        assertEquals(TEST_PACKAGE_NAME, result.getPackageName());
    }

    @Test
    @DisplayName("getPackageById - Failure: Package not found")
    void testGetPackageById_NotFound() {
        // Arrange
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> packageService.getPackageById(TEST_PACKAGE_ID));

        assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, exception.getMessage());
    }

    // ==================== getAllPackagesInSystem Tests ====================

    @Test
    @DisplayName("getAllPackagesInSystem - Success: Returns all packages")
    void testGetAllPackagesInSystem_Success() {
        // Arrange
        List<Package> packageList = Arrays.asList(testPackage);
        when(packageRepository.findAll()).thenReturn(packageList);

        // Act
        List<PackageResponseModel> result = packageService.getAllPackagesInSystem();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_PACKAGE_ID, result.get(0).getPackageId());
    }

    // ==================== togglePackage Tests ====================

    @Test
    @DisplayName("togglePackage - Success: Toggle package status")
    void testTogglePackage_Success() {
        // Arrange
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        when(packageRepository.save(any(Package.class))).thenReturn(testPackage);

        // Act
        assertDoesNotThrow(() -> packageService.togglePackage(TEST_PACKAGE_ID));

        // Assert
        verify(packageRepository, times(1)).findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID);
        verify(packageRepository, times(1)).save(testPackage);
        verify(userLogService, times(1)).logData(
            eq(TEST_USER_ID),
            contains("Successfully toggled package status"),
            eq(ApiRoutes.PackageSubRoute.TOGGLE_PACKAGE)
        );
    }

    @Test
    @DisplayName("togglePackage - Failure: Package not found")
    void testTogglePackage_NotFound() {
        // Arrange
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> packageService.togglePackage(TEST_PACKAGE_ID));

        assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, exception.getMessage());
        verify(packageRepository, never()).save(any());
    }

    // ==================== updatePackage Tests ====================

    @Test
    @DisplayName("updatePackage - Success: Valid update request")
    void testUpdatePackage_Success() {
        // Arrange
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        when(packageRepository.save(any(Package.class))).thenReturn(testPackage);

        // Act
        assertDoesNotThrow(() -> packageService.updatePackage(testPackageRequest));

        // Assert
        verify(packageRepository, times(1)).findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID);
        verify(packageRepository, times(1)).save(any(Package.class));
        verify(userLogService, times(1)).logData(
            eq(TEST_USER_ID),
            contains("Successfully updated package"),
            eq(ApiRoutes.PackageSubRoute.UPDATE_PACKAGE)
        );
    }

    @Test
    @DisplayName("updatePackage - Failure: Package not found")
    void testUpdatePackage_NotFound() {
        // Arrange
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> packageService.updatePackage(testPackageRequest));

        assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, exception.getMessage());
        verify(packageRepository, never()).save(any());
    }

    @Test
    @DisplayName("updatePackage - Failure: Null address")
    void testUpdatePackage_NullAddress() {
        // Arrange
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        testPackageRequest.setAddress(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> packageService.updatePackage(testPackageRequest));

        assertEquals("Address is required for package validation", exception.getMessage());
    }

    @Test
    @DisplayName("updatePackage - Failure: Missing street address")
    void testUpdatePackage_MissingStreetAddress() {
        // Arrange
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        testAddress.setStreetAddress(null);
        testPackageRequest.setAddress(testAddress);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> packageService.updatePackage(testPackageRequest));

        assertEquals("Address line 1 is required.", exception.getMessage());
    }

    @Test
    @DisplayName("updatePackage - Failure: Missing city")
    void testUpdatePackage_MissingCity() {
        // Arrange
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        testAddress.setCity(null);
        testPackageRequest.setAddress(testAddress);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> packageService.updatePackage(testPackageRequest));

        assertEquals("City is required.", exception.getMessage());
    }

    @Test
    @DisplayName("updatePackage - Failure: Missing state")
    void testUpdatePackage_MissingState() {
        // Arrange
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        testAddress.setState(null);
        testPackageRequest.setAddress(testAddress);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> packageService.updatePackage(testPackageRequest));

        assertEquals("State is required.", exception.getMessage());
    }

    @Test
    @DisplayName("updatePackage - Failure: Missing postal code")
    void testUpdatePackage_MissingPostalCode() {
        // Arrange
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        testAddress.setPostalCode(null);
        testPackageRequest.setAddress(testAddress);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> packageService.updatePackage(testPackageRequest));

        assertEquals("Zip Code is required.", exception.getMessage());
    }

    // ==================== createPackage Tests ====================

    @Test
    @DisplayName("createPackage - Success: Valid create request")
    void testCreatePackage_Success() {
        // Arrange
        when(packageRepository.save(any(Package.class))).thenReturn(testPackage);

        // Act
        assertDoesNotThrow(() -> packageService.createPackage(testPackageRequest));

        // Assert
        verify(packageRepository, times(1)).save(any(Package.class));
        verify(userLogService, times(1)).logData(
            eq(TEST_USER_ID),
            contains("Successfully inserted package"),
            eq(ApiRoutes.PackageSubRoute.CREATE_PACKAGE)
        );
    }

    @Test
    @DisplayName("createPackage - Failure: Null address")
    void testCreatePackage_NullAddress() {
        // Arrange
        testPackageRequest.setAddress(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> packageService.createPackage(testPackageRequest));

        assertEquals("Address is required for package validation", exception.getMessage());
    }

    @Test
    @DisplayName("createPackage - Failure: Missing street address")
    void testCreatePackage_MissingStreetAddress() {
        // Arrange
        testAddress.setStreetAddress("");
        testPackageRequest.setAddress(testAddress);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> packageService.createPackage(testPackageRequest));

        assertEquals("Address line 1 is required.", exception.getMessage());
    }

    // ==================== getPackagesByPickupLocationId Tests ====================

    @Test
    @DisplayName("getPackagesByPickupLocationId - Success: Returns packages for pickup location")
    void testGetPackagesByPickupLocationId_Success() {
        // Arrange
        when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
            .thenReturn(1L); // Pickup location exists
        
        List<PackagePickupLocationMapping> mappings = Arrays.asList(testMapping);
        when(packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
            .thenReturn(mappings);

        // Act
        List<PackageResponseModel> result = packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TEST_PACKAGE_ID, result.get(0).getPackageId());
    }

    @Test
    @DisplayName("getPackagesByPickupLocationId - Success: Empty result")
    void testGetPackagesByPickupLocationId_EmptyResult() {
        // Arrange
        when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
            .thenReturn(1L); // Pickup location exists
        
        when(packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
            .thenReturn(Arrays.asList());

        // Act
        List<PackageResponseModel> result = packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}