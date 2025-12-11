package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.FilterQueryBuilder.PackageFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.Package;
import com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping;
import com.example.SpringApi.Models.RequestModels.PackageRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.ResponseModels.BulkInsertResponseModel;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    private PackageFilterQueryBuilder packageFilterQueryBuilder;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private PackageService packageService;

    private Package testPackage;
    private PackageRequestModel testPackageRequest;
    private PaginationBaseRequestModel testPaginationRequest;
    private PackagePickupLocationMapping testMapping;

    private static final Long TEST_PACKAGE_ID = 1L;
    private static final Long TEST_PICKUP_LOCATION_ID = 2L;
    private static final Long TEST_CLIENT_ID = 1L;
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_PACKAGE_NAME = "Test Package";
    private static final String TEST_PACKAGE_TYPE = "Box";
    private static final String CREATED_USER = "testuser";

    /**
     * Sets up test data before each test execution.
     * Initializes common test objects and configures mock behaviors.
     */
    @BeforeEach
    void setUp() {
        // Note: BaseService methods are now handled by the actual service implementation

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
        testPackageRequest.setIsDeleted(false);

        // Initialize test pagination request
        testPaginationRequest = new PaginationBaseRequestModel();
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
        pkg.setClientId(TEST_CLIENT_ID);
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

        lenient().when(packageFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            anyLong(), isNull(), anyString(), isNull(), anyBoolean(), any(Pageable.class)
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
    @DisplayName("getPackagesInBatches - Failure: Invalid column name")
    void testGetPackagesInBatches_InvalidColumnName() {
        // Arrange
        PaginationBaseRequestModel.FilterCondition invalidFilter = new PaginationBaseRequestModel.FilterCondition();
        invalidFilter.setColumn("invalidColumn");
        invalidFilter.setOperator("contains");
        invalidFilter.setValue("test");
        testPaginationRequest.setFilters(Arrays.asList(invalidFilter));
        testPaginationRequest.setLogicOperator("AND");

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> packageService.getPackagesInBatches(testPaginationRequest));

        assertTrue(exception.getMessage().contains("Invalid column name"));
        verify(packageFilterQueryBuilder, never()).getColumnType("invalidColumn");
    }

    @Test
    @DisplayName("getPackagesInBatches - Success: With single filter")
    void testGetPackagesInBatches_WithSingleFilter() {
        // Arrange
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn("packageName");
        filter.setOperator("contains");
        filter.setValue("Test");
        testPaginationRequest.setFilters(Arrays.asList(filter));
        testPaginationRequest.setLogicOperator("AND");

        List<Package> packageList = Arrays.asList(testPackage);
        Page<Package> packagePage = new PageImpl<>(packageList, PageRequest.of(0, 10, Sort.by("packageId")), 1);

        when(packageFilterQueryBuilder.getColumnType("packageName")).thenReturn("string");
        when(packageFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            eq(TEST_CLIENT_ID), isNull(), eq("AND"), eq(Arrays.asList(filter)), eq(false), any(Pageable.class)
        )).thenReturn(packagePage);

        // Act
        PaginationBaseResponseModel<PackageResponseModel> result = packageService.getPackagesInBatches(testPaginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(packageFilterQueryBuilder, times(1)).getColumnType("packageName");
    }

    @Test
    @DisplayName("getPackagesInBatches - Success: With multiple filters AND")
    void testGetPackagesInBatches_WithMultipleFiltersAND() {
        // Arrange
        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
        filter1.setColumn("packageName");
        filter1.setOperator("contains");
        filter1.setValue("Test");

        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
        filter2.setColumn("packageType");
        filter2.setOperator("contains");
        filter2.setValue("Box");

        testPaginationRequest.setFilters(Arrays.asList(filter1, filter2));
        testPaginationRequest.setLogicOperator("AND");

        List<Package> packageList = Arrays.asList(testPackage);
        Page<Package> packagePage = new PageImpl<>(packageList, PageRequest.of(0, 10, Sort.by("packageId")), 1);

        when(packageFilterQueryBuilder.getColumnType("packageName")).thenReturn("string");
        when(packageFilterQueryBuilder.getColumnType("packageType")).thenReturn("string");
        when(packageFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            eq(TEST_CLIENT_ID), isNull(), eq("AND"), eq(Arrays.asList(filter1, filter2)), eq(false), any(Pageable.class)
        )).thenReturn(packagePage);

        // Act
        PaginationBaseResponseModel<PackageResponseModel> result = packageService.getPackagesInBatches(testPaginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(packageFilterQueryBuilder, times(1)).getColumnType("packageName");
        verify(packageFilterQueryBuilder, times(1)).getColumnType("packageType");
    }

    @Test
    @DisplayName("getPackagesInBatches - Success: With multiple filters OR")
    void testGetPackagesInBatches_WithMultipleFiltersOR() {
        // Arrange
        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
        filter1.setColumn("packageName");
        filter1.setOperator("contains");
        filter1.setValue("Test");

        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
        filter2.setColumn("packageName");
        filter2.setOperator("contains");
        filter2.setValue("Package");

        testPaginationRequest.setFilters(Arrays.asList(filter1, filter2));
        testPaginationRequest.setLogicOperator("OR");

        List<Package> packageList = Arrays.asList(testPackage);
        Page<Package> packagePage = new PageImpl<>(packageList, PageRequest.of(0, 10, Sort.by("packageId")), 1);

        when(packageFilterQueryBuilder.getColumnType("packageName")).thenReturn("string");
        when(packageFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            eq(TEST_CLIENT_ID), isNull(), eq("OR"), eq(Arrays.asList(filter1, filter2)), eq(false), any(Pageable.class)
        )).thenReturn(packagePage);

        // Act
        PaginationBaseResponseModel<PackageResponseModel> result = packageService.getPackagesInBatches(testPaginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(packageFilterQueryBuilder, times(2)).getColumnType("packageName");
    }

    @Test
    @DisplayName("getPackagesInBatches - Success: With complex filters")
    void testGetPackagesInBatches_WithComplexFilters() {
        // Arrange
        PaginationBaseRequestModel.FilterCondition filter1 = new PaginationBaseRequestModel.FilterCondition();
        filter1.setColumn("packageName");
        filter1.setOperator("contains");
        filter1.setValue("Test");

        PaginationBaseRequestModel.FilterCondition filter2 = new PaginationBaseRequestModel.FilterCondition();
        filter2.setColumn("packageId");
        filter2.setOperator("greaterThan");
        filter2.setValue("0");

        testPaginationRequest.setFilters(Arrays.asList(filter1, filter2));
        testPaginationRequest.setLogicOperator("AND");

        List<Package> packageList = Arrays.asList(testPackage);
        Page<Package> packagePage = new PageImpl<>(packageList, PageRequest.of(0, 10, Sort.by("packageId")), 1);

        when(packageFilterQueryBuilder.getColumnType("packageName")).thenReturn("string");
        when(packageFilterQueryBuilder.getColumnType("packageId")).thenReturn("number");
        when(packageFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
            eq(TEST_CLIENT_ID), isNull(), eq("AND"), eq(Arrays.asList(filter1, filter2)), eq(false), any(Pageable.class)
        )).thenReturn(packagePage);

        // Act
        PaginationBaseResponseModel<PackageResponseModel> result = packageService.getPackagesInBatches(testPaginationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        verify(packageFilterQueryBuilder, times(1)).getColumnType("packageName");
        verify(packageFilterQueryBuilder, times(1)).getColumnType("packageId");
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
    @DisplayName("updatePackage - Failure: Missing package name")
    void testUpdatePackage_MissingPackageName() {
        // Arrange
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        testPackageRequest.setPackageName(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> packageService.updatePackage(testPackageRequest));

        assertTrue(exception.getMessage().contains("Package name"));
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
    @DisplayName("createPackage - Failure: Missing package name")
    void testCreatePackage_MissingPackageName() {
        // Arrange
        testPackageRequest.setPackageName(null);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
            () -> packageService.createPackage(testPackageRequest));

        assertTrue(exception.getMessage().contains("Package name"));
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

    // ==================== Bulk Create Packages Tests ====================

    @Test
    @DisplayName("Bulk Create Packages - Success - All valid packages")
    void bulkCreatePackages_AllValid_Success() {
        // Arrange
        List<PackageRequestModel> packages = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            PackageRequestModel pkgReq = new PackageRequestModel();
            pkgReq.setPackageName("Package" + i);
            pkgReq.setPackageType("Box");
            pkgReq.setLength(10);
            pkgReq.setBreadth(10);
            pkgReq.setHeight(10);
            pkgReq.setMaxWeight(BigDecimal.valueOf(5));
            pkgReq.setStandardCapacity(25);
            pkgReq.setPricePerUnit(BigDecimal.valueOf(50));
            packages.add(pkgReq);
        }

        when(packageRepository.save(any(Package.class))).thenAnswer(invocation -> {
            Package pkg = invocation.getArgument(0);
            pkg.setPackageId((long) (Math.random() * 1000));
            return pkg;
        });
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(packages);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalRequested());
        assertEquals(3, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        verify(packageRepository, times(3)).save(any(Package.class));
    }

    @Test
    @DisplayName("Bulk Create Packages - Partial Success")
    void bulkCreatePackages_PartialSuccess() {
        // Arrange
        List<PackageRequestModel> packages = new ArrayList<>();
        
        // Valid package
        PackageRequestModel validPkg = new PackageRequestModel();
        validPkg.setPackageName("Valid Package");
        validPkg.setPackageType("Box");
        validPkg.setLength(10);
        validPkg.setBreadth(10);
        validPkg.setHeight(10);
        validPkg.setMaxWeight(BigDecimal.valueOf(5));
        validPkg.setStandardCapacity(30);
        validPkg.setPricePerUnit(BigDecimal.valueOf(60));
        packages.add(validPkg);
        
        // Invalid package (missing package name)
        PackageRequestModel invalidPkg = new PackageRequestModel();
        invalidPkg.setPackageName(null);
        invalidPkg.setPackageType("Box");
        invalidPkg.setLength(10);
        invalidPkg.setBreadth(10);
        invalidPkg.setHeight(10);
        invalidPkg.setMaxWeight(BigDecimal.valueOf(5));
        invalidPkg.setStandardCapacity(30);
        invalidPkg.setPricePerUnit(BigDecimal.valueOf(60));
        packages.add(invalidPkg);

        when(packageRepository.save(any(Package.class))).thenAnswer(invocation -> {
            Package pkg = invocation.getArgument(0);
            pkg.setPackageId((long) (Math.random() * 1000));
            return pkg;
        });
        when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(packages);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalRequested());
        assertEquals(1, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
        verify(packageRepository, times(1)).save(any(Package.class));
    }

    @Test
    @DisplayName("Bulk Create Packages - Database Error")
    void bulkCreatePackages_DatabaseError() {
        // Arrange
        List<PackageRequestModel> packages = new ArrayList<>();
        PackageRequestModel pkgReq = new PackageRequestModel();
        pkgReq.setPackageName("Test Package");
        pkgReq.setPackageType("Box");
        pkgReq.setLength(10);
        pkgReq.setBreadth(10);
        pkgReq.setHeight(10);
        pkgReq.setMaxWeight(BigDecimal.valueOf(5));
        pkgReq.setStandardCapacity(25);
        pkgReq.setPricePerUnit(BigDecimal.valueOf(50));
        packages.add(pkgReq);

        lenient().when(packageRepository.save(any(Package.class))).thenThrow(new RuntimeException("Database error"));

        // Act
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(packages);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalRequested());
        assertEquals(0, result.getSuccessCount());
        assertEquals(1, result.getFailureCount());
    }

    @Test
    @DisplayName("Bulk Create Packages - Empty List")
    void bulkCreatePackages_EmptyList() {
        // Arrange
        List<PackageRequestModel> packages = new ArrayList<>();

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            packageService.bulkCreatePackages(packages);
        });
        assertTrue(exception.getMessage().contains("Package list cannot be null or empty"));
        verify(packageRepository, never()).save(any(Package.class));
    }
}