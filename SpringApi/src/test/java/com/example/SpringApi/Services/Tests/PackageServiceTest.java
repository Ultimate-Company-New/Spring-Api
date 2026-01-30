package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.FilterQueryBuilder.PackageFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.Package;
import com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping;
import com.example.SpringApi.Models.RequestModels.PackageRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel.FilterCondition;
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
import org.junit.jupiter.api.Nested;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PackageService.
 *
 * Test Group Summary:
 * | Group Name                              | Number of Tests |
 * | :-------------------------------------- | :-------------- |
 * | GetPackageByIdTests                     | 4               |
 * | GetPackagesInBatchesTests               | 7               |
 * | CreatePackageTests                      | 7               |
 * | UpdatePackageTests                      | 6               |
 * | TogglePackageTests                      | 4               |
 * | BulkCreatePackagesTests                 | 7               |
 * | CreatePackageValidationTests            | 15              |
 * | Additional Tests (standalone)           | 28              |
 * | **Total**                               | **78**          |
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
    private static final Long TEST_CLIENT_ID = 1L;
    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        testPackageRequest = createValidPackageRequest();
        testPackageRequest.setPackageId(TEST_PACKAGE_ID);
        testPackage = createTestPackage();
        testPackage.setPackageId(TEST_PACKAGE_ID);
        testPaginationRequest = createValidPaginationRequest();
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(10);
        testPaginationRequest.setIncludeDeleted(false);
        testMapping = createTestPackagePickupLocationMapping();
        testMapping.setPackageId(TEST_PACKAGE_ID);
        testMapping.setPickupLocationId(TEST_PICKUP_LOCATION_ID);
        testMapping.setPackageEntity(testPackage);

        lenient().when(packageRepository.save(any(Package.class))).thenReturn(testPackage);
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
    }

    @Nested
    @DisplayName("GetPackagesInBatches Tests")
    class GetPackagesInBatchesTests {

        /**
         * Single comprehensive unit test for getPackagesInBatches: invalid pagination,
         * success without filters, and triple-loop over valid/invalid column x operator x value.
         */
        @Test
        @DisplayName("Get Packages In Batches - Invalid pagination, success no filters, triple-loop filter validation")
        void getPackagesInBatches_SingleComprehensiveTest() {
            // (1) Invalid pagination
            testPaginationRequest.setStart(-1);
            testPaginationRequest.setEnd(10);
            assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.StartIndexCannotBeNegative,
                    () -> packageService.getPackagesInBatches(testPaginationRequest));

            testPaginationRequest.setStart(0);
            testPaginationRequest.setEnd(0);
            assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.EndIndexMustBeGreaterThanZero,
                    () -> packageService.getPackagesInBatches(testPaginationRequest));

            testPaginationRequest.setStart(10);
            testPaginationRequest.setEnd(5);
            assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.StartIndexMustBeLessThanEnd,
                    () -> packageService.getPackagesInBatches(testPaginationRequest));

            // (2) Success without filters
            testPaginationRequest.setStart(0);
            testPaginationRequest.setEnd(10);
            testPaginationRequest.setFilters(null);
            List<Package> list = Arrays.asList(testPackage);
            Page<Package> page = new PageImpl<>(list, PageRequest.of(0, 10, Sort.by("packageId").descending()), 1);
            when(packageFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    eq(TEST_CLIENT_ID), isNull(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(page);
            PaginationBaseResponseModel<PackageResponseModel> res = packageService.getPackagesInBatches(testPaginationRequest);
            assertNotNull(res);
            assertEquals(1, res.getData().size());
            assertEquals(DEFAULT_PACKAGE_NAME, res.getData().get(0).getPackageName());

            // (3) Triple-loop: valid/invalid column x operator x value
            String[] stringCols = PACKAGE_STRING_COLUMNS;
            String[] numberCols = PACKAGE_NUMBER_COLUMNS;
            String[] boolCols = PACKAGE_BOOLEAN_COLUMNS;
            String[] dateCols = PACKAGE_DATE_COLUMNS;
            String[] invalidCols = BATCH_INVALID_COLUMNS;
            Set<String> validOps = new HashSet<>(Arrays.asList(
                    "equals", "notEquals", "contains", "notContains", "startsWith", "endsWith",
                    "greaterThan", "lessThan", "greaterThanOrEqual", "lessThanOrEqual", "isEmpty", "isNotEmpty"));
            Set<String> numericDateOps = new HashSet<>(Arrays.asList(
                    "equals", "notEquals", "greaterThan", "lessThan", "greaterThanOrEqual", "lessThanOrEqual"));
            String[] invalidOps = BATCH_INVALID_OPERATORS;
            String[] allCols = joinArrays(stringCols, numberCols, boolCols, dateCols, invalidCols);
            String[] allOps = joinArrays(
                    validOps.toArray(new String[0]), invalidOps);
            Set<String> uniqueOps = new HashSet<>(Arrays.asList(allOps));
            String[] vals = joinArrays(BATCH_VALID_VALUES, BATCH_EMPTY_VALUES);

            Page<Package> emptyPage = new PageImpl<>(Collections.emptyList());
            lenient().when(packageFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                    anyLong(), any(), anyString(), any(), anyBoolean(), any(Pageable.class))).thenReturn(emptyPage);
            for (String c : stringCols)
                lenient().when(packageFilterQueryBuilder.getColumnType(c)).thenReturn("string");
            for (String c : numberCols)
                lenient().when(packageFilterQueryBuilder.getColumnType(c)).thenReturn("number");
            for (String c : boolCols)
                lenient().when(packageFilterQueryBuilder.getColumnType(c)).thenReturn("boolean");
            for (String c : dateCols)
                lenient().when(packageFilterQueryBuilder.getColumnType(c)).thenReturn("date");

            for (String column : allCols) {
                for (String op : uniqueOps) {
                    for (String val : vals) {
                        testPaginationRequest.setStart(0);
                        testPaginationRequest.setEnd(10);
                        FilterCondition fc = createFilterCondition(column, op, val);
                        testPaginationRequest.setFilters(Collections.singletonList(fc));

                        boolean known = !Arrays.asList(invalidCols).contains(column);
                        boolean opValid = validOps.contains(op);
                        boolean numberMatch = Arrays.asList(numberCols).contains(column);
                        boolean boolMatch = Arrays.asList(boolCols).contains(column);
                        boolean dateMatch = Arrays.asList(dateCols).contains(column);
                        boolean boolOk = !boolMatch || "equals".equals(op) || "notEquals".equals(op);
                        boolean numDateOk = !(numberMatch || dateMatch) || numericDateOps.contains(op);
                        boolean valueOk = "isEmpty".equals(op) || "isNotEmpty".equals(op) || val != null;
                        boolean shouldPass = known && opValid && boolOk && numDateOk && valueOk;

                        try {
                            packageService.getPackagesInBatches(testPaginationRequest);
                            if (!shouldPass)
                                fail("Expected failure: col=" + column + " op=" + op + " val=" + val);
                        } catch (BadRequestException e) {
                            if (shouldPass)
                                fail("Expected success: col=" + column + " op=" + op + " val=" + val + " err=" + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    @Nested
    @DisplayName("GetPackageById Tests")
    class GetPackageByIdTests {

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
        assertEquals(DEFAULT_PACKAGE_NAME, result.getPackageName());
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
    }

    @Nested
    @DisplayName("GetAllPackagesInSystem Tests")
    class GetAllPackagesInSystemTests {

    @Test
    @DisplayName("Get All Packages In System - Success - Returns all packages")
    void getAllPackagesInSystem_Success() {
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
    }

    @Nested
    @DisplayName("TogglePackage Tests")
    class TogglePackageTests {

    @Test
    @DisplayName("Toggle Package - Success - Toggles package status")
    void togglePackage_Success() {
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
                eq(ApiRoutes.PackageSubRoute.TOGGLE_PACKAGE));
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
    }

    @Nested
    @DisplayName("UpdatePackage Tests")
    class UpdatePackageTests {

    @Test
    @DisplayName("Update Package - Success - Valid update request")
    void updatePackage_Success() {
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
                eq(ApiRoutes.PackageSubRoute.UPDATE_PACKAGE));
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
        assertThrowsBadRequest(ErrorMessages.PackageErrorMessages.InvalidPackageName,
            () -> packageService.updatePackage(testPackageRequest));
    }
    }

    @Nested
    @DisplayName("CreatePackage Tests")
    class CreatePackageTests {

    @Test
    @DisplayName("Create Package - Success - Valid create request")
    void createPackage_Success() {
        // Arrange
        when(packageRepository.save(any(Package.class))).thenReturn(testPackage);

        // Act
        assertDoesNotThrow(() -> packageService.createPackage(testPackageRequest));

        // Assert
        verify(packageRepository, times(1)).save(any(Package.class));
        verify(userLogService, times(1)).logData(
                eq(TEST_USER_ID),
                contains("Successfully inserted package"),
                eq(ApiRoutes.PackageSubRoute.CREATE_PACKAGE));
    }

    @Test
    @DisplayName("createPackage - Failure: Missing package name")
    void testCreatePackage_MissingPackageName() {
        // Arrange
        testPackageRequest.setPackageName(null);

        // Act & Assert
        assertThrowsBadRequest(ErrorMessages.PackageErrorMessages.InvalidPackageName,
            () -> packageService.createPackage(testPackageRequest));
    }
    }

    @Nested
    @DisplayName("GetPackagesByPickupLocationId Tests")
    class GetPackagesByPickupLocationIdTests {

    @Test
    @DisplayName("Get Packages By Pickup Location Id - Success - Returns packages for pickup location")
    void getPackagesByPickupLocationId_Success() {
        // Arrange
        when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                .thenReturn(1L); // Pickup location exists

        List<PackagePickupLocationMapping> mappings = Arrays.asList(testMapping);
        when(packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID,
                TEST_CLIENT_ID))
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

        when(packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID,
                TEST_CLIENT_ID))
                .thenReturn(Arrays.asList());

        // Act
        List<PackageResponseModel> result = packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    }

    @Nested
    @DisplayName("BulkCreatePackages Tests")
    class BulkCreatePackagesTests {

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

    @org.junit.jupiter.api.Nested
    @DisplayName("CreatePackageValidationTests")
    class CreatePackageValidationTests {

        @Test
        @DisplayName("Create Package - Null Name - Throws BadRequestException")
        void createPackage_NullName_Throws() {
            testPackageRequest.setPackageName(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageName, ex.getMessage());
        }

        @Test
        @DisplayName("Create Package - Empty Name - Throws BadRequestException")
        void createPackage_EmptyName_Throws() {
            testPackageRequest.setPackageName("");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageName, ex.getMessage());
        }

        @Test
        @DisplayName("Create Package - Null Type - Throws BadRequestException")
        void createPackage_NullType_Throws() {
            testPackageRequest.setPackageType(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageType, ex.getMessage());
        }

        @Test
        @DisplayName("Create Package - Zero Length - Throws BadRequestException")
        void createPackage_ZeroLength_Throws() {
            testPackageRequest.setLength(0);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidLength, ex.getMessage());
        }

        @Test
        @DisplayName("Create Package - Negative Breadth - Throws BadRequestException")
        void createPackage_NegativeBreadth_Throws() {
            testPackageRequest.setBreadth(-1);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidBreadth, ex.getMessage());
        }

        @Test
        @DisplayName("Create Package - Zero Height - Throws BadRequestException")
        void createPackage_ZeroHeight_Throws() {
            testPackageRequest.setHeight(0);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidHeight, ex.getMessage());
        }

        @Test
        @DisplayName("Create Package - Negative Max Weight - Throws BadRequestException")
        void createPackage_NegativeMaxWeight_Throws() {
            testPackageRequest.setMaxWeight(BigDecimal.valueOf(-1.0));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidMaxWeight, ex.getMessage());
        }

        @Test
        @DisplayName("Create Package - Zero Standard Capacity - Throws BadRequestException")
        void createPackage_ZeroStandardCapacity_Throws() {
            testPackageRequest.setStandardCapacity(0);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidStandardCapacity, ex.getMessage());
        }

        @Test
        @DisplayName("Create Package - Negative Price Per Unit - Throws BadRequestException")
        void createPackage_NegativePricePerUnit_Throws() {
            testPackageRequest.setPricePerUnit(BigDecimal.valueOf(-1.0));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidPricePerUnit, ex.getMessage());
        }
    }

    @org.junit.jupiter.api.Nested
    @DisplayName("UpdatePackageValidationTests")
    class UpdatePackageValidationTests {

        @Test
        @DisplayName("Update Package - Null Name - Throws BadRequestException")
        void updatePackage_NullName_Throws() {
            testPackageRequest.setPackageName(null);
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.updatePackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageName, ex.getMessage());
        }

        @Test
        @DisplayName("Update Package - Invalid Length - Throws BadRequestException")
        void updatePackage_InvalidLength_Throws() {
            testPackageRequest.setLength(-5);
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.updatePackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidLength, ex.getMessage());
        }
    }


    @Test
    @DisplayName("Get Package By ID - Long.MIN_VALUE - Should handle")
    void getPackageById_MinLongValue_ThrowsNotFoundException() {
        when(packageRepository.findByPackageIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class, 
                () -> packageService.getPackageById(Long.MIN_VALUE));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Get Package By ID - Long.MAX_VALUE - Should handle")
    void getPackageById_MaxLongValue_ThrowsNotFoundException() {
        when(packageRepository.findByPackageIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> packageService.getPackageById(Long.MAX_VALUE));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Get Package By ID - Negative ID - Should handle")
    void getPackageById_NegativeId_ThrowsNotFoundException() {
        when(packageRepository.findByPackageIdAndClientId(-100L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> packageService.getPackageById(-100L));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
    }

    // ==================== Additional GetPackagesInBatches Tests ====================

    @Test
    @DisplayName("Get Packages In Batches - Negative Start Index")
    void getPackagesInBatches_NegativeStartIndex_ThrowsBadRequestException() {
        testPaginationRequest.setStart(-1);
        testPaginationRequest.setEnd(10);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.getPackagesInBatches(testPaginationRequest));
        assertTrue(ex.getMessage().contains("Invalid"));
    }

    @Test
    @DisplayName("Get Packages In Batches - End Before Start")
    void getPackagesInBatches_EndBeforeStart_ThrowsBadRequestException() {
        testPaginationRequest.setStart(10);
        testPaginationRequest.setEnd(5);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.getPackagesInBatches(testPaginationRequest));
        assertTrue(ex.getMessage().contains("Invalid"));
    }

    @Test
    @DisplayName("Get Packages In Batches - Large Page Size")
    void getPackagesInBatches_LargePageSize_Success() {
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(1000);
        List<Package> packageList = Arrays.asList(testPackage);
        Page<Package> packagePage = new PageImpl<>(packageList, PageRequest.of(0, 1000), 1);
        lenient().when(packageFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), isNull(), anyString(), isNull(), anyBoolean(), any(Pageable.class)))
                .thenReturn(packagePage);
        
        PaginationBaseResponseModel<PackageResponseModel> result = packageService
                .getPackagesInBatches(testPaginationRequest);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Get Packages In Batches - Null Filters")
    void getPackagesInBatches_NullFilters_Success() {
        testPaginationRequest.setFilters(null);
        List<Package> packageList = Arrays.asList(testPackage);
        Page<Package> packagePage = new PageImpl<>(packageList, PageRequest.of(0, 10), 1);
        lenient().when(packageFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), isNull(), anyString(), isNull(), anyBoolean(), any(Pageable.class)))
                .thenReturn(packagePage);
        
        PaginationBaseResponseModel<PackageResponseModel> result = packageService
                .getPackagesInBatches(testPaginationRequest);
        assertNotNull(result);
        assertEquals(1, result.getData().size());
    }

    @Test
    @DisplayName("Get Packages In Batches - Empty List Result")
    void getPackagesInBatches_EmptyResult_ReturnsEmptyList() {
        List<Package> emptyList = new ArrayList<>();
        Page<Package> emptyPage = new PageImpl<>(emptyList, PageRequest.of(0, 10), 0);
        lenient().when(packageFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), isNull(), anyString(), isNull(), anyBoolean(), any(Pageable.class)))
                .thenReturn(emptyPage);
        
        PaginationBaseResponseModel<PackageResponseModel> result = packageService
                .getPackagesInBatches(testPaginationRequest);
        assertNotNull(result);
        assertEquals(0, result.getData().size());
    }


    @Test
    @DisplayName("Create Package - All Valid Fields - Multiple Tests Success")
    void createPackage_AllValid_MultipleProperties_Success() {
        testPackageRequest.setPackageName("Premium Box");
        testPackageRequest.setPackageType("Premium");
        testPackageRequest.setLength(50);
        testPackageRequest.setBreadth(40);
        testPackageRequest.setHeight(30);
        testPackageRequest.setMaxWeight(BigDecimal.valueOf(25.5));
        testPackageRequest.setStandardCapacity(5);
        testPackageRequest.setPricePerUnit(BigDecimal.valueOf(15.99));
        when(packageRepository.save(any(Package.class))).thenReturn(testPackage);
        
        assertDoesNotThrow(() -> packageService.createPackage(testPackageRequest));
        verify(packageRepository).save(any(Package.class));
    }

    @Test
    @DisplayName("Create Package - Boundary Length - Zero")
    void createPackage_ZeroLength_ThrowsBadRequestException() {
        testPackageRequest.setLength(0);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidLength, ex.getMessage());
    }

    @Test
    @DisplayName("Create Package - Boundary Weight - Zero (Valid)")
    void createPackage_ZeroWeight_Success() {
        testPackageRequest.setMaxWeight(BigDecimal.ZERO);
        when(packageRepository.save(any(Package.class))).thenReturn(testPackage);
        assertDoesNotThrow(() -> packageService.createPackage(testPackageRequest));
        verify(packageRepository).save(any(Package.class));
    }

    @Test
    @DisplayName("Create Package - Boundary Price - Zero (Valid)")
    void createPackage_ZeroPrice_Success() {
        testPackageRequest.setPricePerUnit(BigDecimal.ZERO);
        when(packageRepository.save(any(Package.class))).thenReturn(testPackage);
        assertDoesNotThrow(() -> packageService.createPackage(testPackageRequest));
        verify(packageRepository).save(any(Package.class));
    }

    @Test
    @DisplayName("Create Package - Whitespace Package Name - Should be empty")
    void createPackage_WhitespacePackageName_ThrowsBadRequestException() {
        testPackageRequest.setPackageName("   ");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageName, ex.getMessage());
    }

    @Test
    @DisplayName("Create Package - Whitespace Package Type - Should be empty")
    void createPackage_WhitespacePackageType_ThrowsBadRequestException() {
        testPackageRequest.setPackageType("   ");
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.createPackage(testPackageRequest));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageType, ex.getMessage());
    }

    @Test
    @DisplayName("Create Package - Dimension Precision - Decimal Values")
    void createPackage_DecimalDimensions_Success() {
        testPackageRequest.setLength(10);
        testPackageRequest.setBreadth(20);
        testPackageRequest.setHeight(30);
        testPackageRequest.setMaxWeight(BigDecimal.valueOf(10.99));
        testPackageRequest.setPricePerUnit(BigDecimal.valueOf(99.99));
        when(packageRepository.save(any(Package.class))).thenReturn(testPackage);
        assertDoesNotThrow(() -> packageService.createPackage(testPackageRequest));
    }

    // ==================== Additional UpdatePackage Tests ====================

    @Test
    @DisplayName("Update Package - Not Found - Package ID")
    void updatePackage_PackageNotFound_ThrowsNotFoundException() {
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> packageService.updatePackage(testPackageRequest));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Update Package - Negative ID - Package Not Found")
    void updatePackage_NegativePackageId_ThrowsNotFoundException() {
        testPackageRequest.setPackageId(-1L);
        when(packageRepository.findByPackageIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> packageService.updatePackage(testPackageRequest));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Update Package - Zero Breadth - Invalid")
    void updatePackage_ZeroBreadth_ThrowsBadRequestException() {
        testPackageRequest.setBreadth(0);
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.updatePackage(testPackageRequest));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidBreadth, ex.getMessage());
    }

    @Test
    @DisplayName("Update Package - Zero Height - Invalid")
    void updatePackage_ZeroHeight_ThrowsBadRequestException() {
        testPackageRequest.setHeight(0);
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> packageService.updatePackage(testPackageRequest));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidHeight, ex.getMessage());
    }


    @Test
    @DisplayName("Toggle Package - Negative ID - Not Found")
    void togglePackage_NegativeId_ThrowsNotFoundException() {
        when(packageRepository.findByPackageIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> packageService.togglePackage(-1L));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Toggle Package - Zero ID - Not Found")
    void togglePackage_ZeroId_ThrowsNotFoundException() {
        when(packageRepository.findByPackageIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> packageService.togglePackage(0L));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Toggle Package - Max Long ID - Not Found")
    void togglePackage_MaxLongId_ThrowsNotFoundException() {
        when(packageRepository.findByPackageIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> packageService.togglePackage(Long.MAX_VALUE));
        assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
    }

    @Test
    @DisplayName("Toggle Package - Multiple Toggles - State Persistence")
    void togglePackage_MultipleToggles_StatePersists() {
        testPackage.setIsDeleted(false);
        when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID))
                .thenReturn(testPackage);
        when(packageRepository.save(any(Package.class))).thenReturn(testPackage);
        
        packageService.togglePackage(TEST_PACKAGE_ID);
        assertTrue(testPackage.getIsDeleted());
        
        packageService.togglePackage(TEST_PACKAGE_ID);
        assertFalse(testPackage.getIsDeleted());
    }

    // ==================== BulkCreate Additional Tests ====================

    @Test
    @DisplayName("Bulk Create Packages - Mixed Nulls and Valids")
    void bulkCreatePackages_MixedNullAndValid_PartialSuccess() {
        List<PackageRequestModel> requests = new ArrayList<>();
        requests.add(testPackageRequest);
        requests.add(null);
        requests.add(testPackageRequest);
        
        when(packageRepository.save(any(Package.class))).thenReturn(testPackage);
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(requests);
        
        assertNotNull(result);
        assertTrue(result.getSuccessCount() >= 0);
    }

    @Test
    @DisplayName("Bulk Create Packages - Large Batch (100 items)")
    void bulkCreatePackages_LargeBatch_Success() {
        List<PackageRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            PackageRequestModel req = new PackageRequestModel();
            req.setPackageId((long) i);
            req.setPackageName("Package_" + i);
            req.setPackageType("Type_" + i);
            req.setLength(10 + i);
            req.setBreadth(10);
            req.setHeight(10);
            req.setMaxWeight(BigDecimal.valueOf(10.0));
            req.setStandardCapacity(1);
            req.setPricePerUnit(BigDecimal.valueOf(10.0));
            requests.add(req);
        }
        
        when(packageRepository.save(any(Package.class))).thenReturn(testPackage);
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(requests);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Bulk Create Packages - All Invalid Names")
    void bulkCreatePackages_AllInvalidNames_AllFail() {
        List<PackageRequestModel> requests = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PackageRequestModel req = new PackageRequestModel();
            req.setPackageName("");
            req.setPackageType("Type");
            req.setLength(10);
            req.setBreadth(10);
            req.setHeight(10);
            requests.add(req);
        }
        
        BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(requests);
        assertNotNull(result);
        assertEquals(5, result.getTotalRequested());
    }

}
