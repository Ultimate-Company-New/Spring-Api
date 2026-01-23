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
import org.junit.jupiter.api.Nested;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PackageService.
 *
 * <p>
 * This test class provides comprehensive coverage of PackageService methods
 * including:
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
 * @version 2.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PackageService Unit Tests")
class PackageServiceTest extends BaseTest {

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
    private static final String TEST_PACKAGE_NAME = "Test Package";
    private static final String TEST_PACKAGE_TYPE = "Box";

    /**
     * Sets up test data before each test execution.
     * Initializes common test objects and configures mock behaviors.
     */
    @BeforeEach
    void setUp() {
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
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        // We assume BaseService.getUserId() works via request mocking or we might need
        // lenient mocks if not strictly used in every test.
        // lenient().when(packageRepository.save(any(Package.class))).thenReturn(testPackage);
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
     * Creates a test Package entity from the PackageRequestModel, simulating
     * service behavior.
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
        pkg.setClientId(DEFAULT_CLIENT_ID);
        pkg.setIsDeleted(request.getIsDeleted() != null ? request.getIsDeleted() : Boolean.FALSE);
        pkg.setCreatedUser(DEFAULT_CREATED_USER);
        pkg.setModifiedUser(DEFAULT_CREATED_USER);
        return pkg;
    }

    // ==================== getPackagesInBatches Tests ====================

    @Nested
    @DisplayName("getPackagesInBatches Tests")
    class GetPackagesInBatchesTests {

        @Test
        @DisplayName("getPackagesInBatches - Success: Valid pagination request")
        void testGetPackagesInBatches_Success() {
            // Arrange
            List<Package> packageList = Arrays.asList(testPackage);
            Page<Package> packagePage = new PageImpl<>(packageList, PageRequest.of(0, 10, Sort.by("packageId")), 1);

            lenient().when(packageFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), isNull(), anyString(), isNull(), anyBoolean(), any(Pageable.class)))
                    .thenReturn(packagePage);

            // Act
            PaginationBaseResponseModel<PackageResponseModel> result = packageService
                    .getPackagesInBatches(testPaginationRequest);

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
        @DisplayName("getPackagesInBatches - Failure: Negative Start Index")
        void getPackagesInBatches_NegativeStartIndex_ThrowsBadRequestException() {
            testPaginationRequest.setStart(-1);
            testPaginationRequest.setEnd(10);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.getPackagesInBatches(testPaginationRequest));
            assertTrue(ex.getMessage().contains("Invalid"));
        }

        @Test
        @DisplayName("getPackagesInBatches - Failure: End Before Start")
        void getPackagesInBatches_EndBeforeStart_ThrowsBadRequestException() {
            testPaginationRequest.setStart(10);
            testPaginationRequest.setEnd(5);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.getPackagesInBatches(testPaginationRequest));
            assertTrue(ex.getMessage().contains("Invalid"));
        }

        /**
         * Triple Loop Test for Filter Validation.
         * PackageService validates columns and operators.
         */
        @Test
        @DisplayName("Get Packages In Batches - Filter Logic Triple Loop Validation")
        void getPackagesInBatches_TripleLoopValidation() {
            // 1. Columns
            String[] validColumns = {
                    "packageId", "packageName", "dimensions", "length", "breadth",
                    "height", "standardCapacity", "packageType", "maxWeight",
                    "pricePerUnit", "createdUser", "modifiedUser", "createdAt",
                    "updatedAt", "notes", "isDeleted", "pickupLocationId"
            };
            String[] invalidColumns = { "invalidCol", "DROP TABLE", "unknown" };

            // 2. Operators
            String[] validOperators = {
                    "equals", "notEquals", "contains", "notContains", "startsWith", "endsWith",
                    "greaterThan", "lessThan", "greaterThanOrEqual", "lessThanOrEqual",
                    "isEmpty", "isNotEmpty"
            };
            String[] invalidOperators = { "invalidOp", "like" };

            // 3. Values
            String[] values = { "val", "" };

            // Mock response
            Page<Package> emptyPage = new PageImpl<>(Collections.emptyList());
            lenient().when(packageFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), any(), any(), any(), anyBoolean(), any(Pageable.class)))
                    .thenReturn(emptyPage);

            // Mock column types (simplified)
            lenient().when(packageFilterQueryBuilder.getColumnType(anyString())).thenReturn("string");

            for (String column : joinArrays(validColumns, invalidColumns)) {
                for (String operator : joinArrays(validOperators, invalidOperators)) {
                    for (String value : values) {
                        PaginationBaseRequestModel req = new PaginationBaseRequestModel();
                        req.setStart(0);
                        req.setEnd(10);
                        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                        filter.setColumn(column);
                        filter.setOperator(operator);
                        filter.setValue(value);
                        req.setFilters(List.of(filter));

                        boolean isValidColumn = Arrays.asList(validColumns).contains(column);
                        boolean isValidOperator = Arrays.asList(validOperators).contains(operator);

                        if (isValidColumn && isValidOperator) {
                            assertDoesNotThrow(() -> packageService.getPackagesInBatches(req),
                                    "Failed for valid column/operator: " + column + "/" + operator);
                        } else {
                            BadRequestException ex = assertThrows(BadRequestException.class,
                                    () -> packageService.getPackagesInBatches(req),
                                    "Expected BadRequest for invalid input: " + column + "/" + operator);
                            assertTrue(ex.getMessage().contains("Invalid"));
                        }
                    }
                }
            }
        }
    }

    private String[] joinArrays(String[]... arrays) {
        int length = 0;
        for (String[] array : arrays)
            length += array.length;
        String[] result = new String[length];
        int offset = 0;
        for (String[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    // ==================== getPackageById Tests ====================

    @Nested
    @DisplayName("getPackageById Tests")
    class GetPackageByIdTests {

        @Test
        @DisplayName("getPackageById - Success: Valid package ID")
        void testGetPackageById_Success() {
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(testPackage);

            PackageResponseModel result = packageService.getPackageById(TEST_PACKAGE_ID);

            assertNotNull(result);
            assertEquals(TEST_PACKAGE_ID, result.getPackageId());
            assertEquals(TEST_PACKAGE_NAME, result.getPackageName());
        }

        @Test
        @DisplayName("getPackageById - Failure: Package not found")
        void testGetPackageById_NotFound() {
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, DEFAULT_CLIENT_ID)).thenReturn(null);

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> packageService.getPackageById(TEST_PACKAGE_ID));

            assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, exception.getMessage());
        }

        @Test
        @DisplayName("Get Package By ID - Negative ID - Should handle")
        void getPackageById_NegativeId_ThrowsNotFoundException() {
            when(packageRepository.findByPackageIdAndClientId(-100L, DEFAULT_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> packageService.getPackageById(-100L));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
        }
    }

    // ==================== getAllPackagesInSystem Tests ====================

    @Nested
    @DisplayName("getAllPackagesInSystem Tests")
    class GetAllPackagesInSystemTests {

        @Test
        @DisplayName("getAllPackagesInSystem - Success: Returns all packages")
        void testGetAllPackagesInSystem_Success() {
            List<Package> packageList = Arrays.asList(testPackage);
            when(packageRepository.findAll()).thenReturn(packageList);

            List<PackageResponseModel> result = packageService.getAllPackagesInSystem();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(TEST_PACKAGE_ID, result.get(0).getPackageId());
        }
    }

    // ==================== togglePackage Tests ====================

    @Nested
    @DisplayName("togglePackage Tests")
    class TogglePackageTests {

        @Test
        @DisplayName("togglePackage - Success: Toggle package status")
        void testTogglePackage_Success() {
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(testPackage);
            when(packageRepository.save(any(Package.class))).thenReturn(testPackage);

            assertDoesNotThrow(() -> packageService.togglePackage(TEST_PACKAGE_ID));

            verify(packageRepository, times(1)).findByPackageIdAndClientId(TEST_PACKAGE_ID, DEFAULT_CLIENT_ID);
            verify(packageRepository, times(1)).save(testPackage);
            verify(userLogService, times(1)).logData(
                    eq(DEFAULT_USER_ID),
                    contains("Successfully toggled package status"),
                    eq(ApiRoutes.PackageSubRoute.TOGGLE_PACKAGE));
        }

        @Test
        @DisplayName("togglePackage - Failure: Package not found")
        void testTogglePackage_NotFound() {
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, DEFAULT_CLIENT_ID)).thenReturn(null);

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> packageService.togglePackage(TEST_PACKAGE_ID));

            assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, exception.getMessage());
            verify(packageRepository, never()).save(any());
        }
    }

    // ==================== updatePackage Tests ====================

    @Nested
    @DisplayName("updatePackage Tests")
    class UpdatePackageTests {

        @Test
        @DisplayName("updatePackage - Success: Valid update request")
        void testUpdatePackage_Success() {
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(testPackage);
            when(packageRepository.save(any(Package.class))).thenReturn(testPackage);

            assertDoesNotThrow(() -> packageService.updatePackage(testPackageRequest));

            verify(packageRepository, times(1)).findByPackageIdAndClientId(TEST_PACKAGE_ID, DEFAULT_CLIENT_ID);
            verify(packageRepository, times(1)).save(any(Package.class));
            verify(userLogService, times(1)).logData(
                    eq(DEFAULT_USER_ID),
                    contains("Successfully updated package"),
                    eq(ApiRoutes.PackageSubRoute.UPDATE_PACKAGE));
        }

        @Test
        @DisplayName("updatePackage - Failure: Package not found")
        void testUpdatePackage_NotFound() {
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, DEFAULT_CLIENT_ID)).thenReturn(null);

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> packageService.updatePackage(testPackageRequest));

            assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, exception.getMessage());
            verify(packageRepository, never()).save(any());
        }

        @Test
        @DisplayName("Update Package - Null Name - Throws BadRequestException")
        void updatePackage_NullName_Throws() {
            testPackageRequest.setPackageName(null);
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(testPackage);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.updatePackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageName, ex.getMessage());
        }
    }

    // ==================== createPackage Tests ====================

    @Nested
    @DisplayName("createPackage Tests")
    class CreatePackageTests {

        @Test
        @DisplayName("createPackage - Success: Valid create request")
        void testCreatePackage_Success() {
            when(packageRepository.save(any(Package.class))).thenReturn(testPackage);

            assertDoesNotThrow(() -> packageService.createPackage(testPackageRequest));

            verify(packageRepository, times(1)).save(any(Package.class));
            verify(userLogService, times(1)).logData(
                    eq(DEFAULT_USER_ID),
                    contains("Successfully inserted package"),
                    eq(ApiRoutes.PackageSubRoute.CREATE_PACKAGE));
        }

        @Test
        @DisplayName("createPackage - Failure: Missing package name")
        void testCreatePackage_MissingPackageName() {
            testPackageRequest.setPackageName(null);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));

            assertTrue(exception.getMessage().contains("Package name"));
        }

        @Test
        @DisplayName("Create Package - Zero Length - Throws BadRequestException")
        void createPackage_ZeroLength_Throws() {
            testPackageRequest.setLength(0);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidLength, ex.getMessage());
        }
    }

    // ==================== getPackagesByPickupLocationId Tests ====================

    @Nested
    @DisplayName("getPackagesByPickupLocationId Tests")
    class GetPackagesByPickupLocationIdTests {

        @Test
        @DisplayName("getPackagesByPickupLocationId - Success: Returns packages for pickup location")
        void testGetPackagesByPickupLocationId_Success() {
            when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(1L);

            List<PackagePickupLocationMapping> mappings = Arrays.asList(testMapping);
            when(packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID,
                    DEFAULT_CLIENT_ID))
                    .thenReturn(mappings);

            List<PackageResponseModel> result = packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(TEST_PACKAGE_ID, result.get(0).getPackageId());
        }

        @Test
        @DisplayName("getPackagesByPickupLocationId - Success: Empty result")
        void testGetPackagesByPickupLocationId_EmptyResult() {
            when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, DEFAULT_CLIENT_ID))
                    .thenReturn(1L);

            when(packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID,
                    DEFAULT_CLIENT_ID))
                    .thenReturn(Arrays.asList());

            List<PackageResponseModel> result = packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    // ==================== Bulk Create Packages Tests ====================

    @Nested
    @DisplayName("bulkCreatePackages Tests")
    class BulkCreatePackagesTests {

        @Test
        @DisplayName("Bulk Create Packages - Success - All valid packages")
        void bulkCreatePackages_AllValid_Success() {
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

            BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(packages);

            assertNotNull(result);
            assertEquals(3, result.getTotalRequested());
            assertEquals(3, result.getSuccessCount());
            assertEquals(0, result.getFailureCount());
            verify(packageRepository, times(3)).save(any(Package.class));
        }

        @Test
        @DisplayName("Bulk Create Packages - Partial Success")
        void bulkCreatePackages_PartialSuccess() {
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

            BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(packages);

            assertNotNull(result);
            assertEquals(2, result.getTotalRequested());
            assertEquals(1, result.getSuccessCount());
            assertEquals(1, result.getFailureCount());
            verify(packageRepository, times(1)).save(any(Package.class));
        }

        @Test
        @DisplayName("Bulk Create Packages - Empty List")
        void bulkCreatePackages_EmptyList() {
            List<PackageRequestModel> packages = new ArrayList<>();

            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                packageService.bulkCreatePackages(packages);
            });
            assertTrue(exception.getMessage().contains("Package list cannot be null or empty"));
            verify(packageRepository, never()).save(any(Package.class));
        }
    }
}
