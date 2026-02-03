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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PackageService.
 *
 * Test Group Summary:
 * | Group Name                              | Number of Tests |
 * | :-------------------------------------- | :-------------- |
 * | GetPackageByIdTests                     | 24              |
 * | GetPackagesInBatchesTests               | 1               |
 * | CreatePackageTests                      | 30              |
 * | UpdatePackageTests                      | 24              |
 * | TogglePackageTests                      | 20              |
 * | GetPackagesByPickupLocationIdTests      | 16              |
 * | BulkCreatePackagesTests                 | 24              |
 * | **Total**                               | **139**         |
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
    @DisplayName("GetPackageById Tests")
    class GetPackageByIdTests {

        /**
         * Purpose: Verify successful retrieval of package by valid ID.
         * Expected Result: PackageResponseModel is returned with correct data.
         * Assertions: Result is not null, ID and name match expected values.
         */
        @Test
        @DisplayName("getPackageById - Success: Valid package ID")
        void testGetPackageById_Success() {
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);

            PackageResponseModel result = packageService.getPackageById(TEST_PACKAGE_ID);

            assertNotNull(result);
            assertEquals(TEST_PACKAGE_ID, result.getPackageId());
            assertEquals(DEFAULT_PACKAGE_NAME, result.getPackageName());
        }

        /**
         * Purpose: Verify NotFoundException is thrown when package is not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches InvalidId error.
         */
        @Test
        @DisplayName("getPackageById - Failure: Package not found")
        void testGetPackageById_NotFound() {
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(null);

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> packageService.getPackageById(TEST_PACKAGE_ID));

            assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, exception.getMessage());
        }

        /**
         * Purpose: Verify NotFoundException is thrown for Long.MIN_VALUE ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches InvalidId error.
         */
        @Test
        @DisplayName("getPackageById - Long.MIN_VALUE ID - Should throw NotFoundException")
        void getPackageById_MinLongValue_ThrowsNotFoundException() {
            when(packageRepository.findByPackageIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> packageService.getPackageById(Long.MIN_VALUE));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify NotFoundException is thrown for Long.MAX_VALUE ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches InvalidId error.
         */
        @Test
        @DisplayName("getPackageById - Long.MAX_VALUE ID - Should throw NotFoundException")
        void getPackageById_MaxLongValue_ThrowsNotFoundException() {
            when(packageRepository.findByPackageIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> packageService.getPackageById(Long.MAX_VALUE));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify NotFoundException is thrown for negative ID (-100L).
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches InvalidId error.
         */
        @Test
        @DisplayName("getPackageById - Negative ID (-100L) - Should throw NotFoundException")
        void getPackageById_NegativeId_ThrowsNotFoundException() {
            when(packageRepository.findByPackageIdAndClientId(-100L, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> packageService.getPackageById(-100L));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify NotFoundException is thrown for zero ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches InvalidId error.
         */
        @Test
        @DisplayName("getPackageById - Zero ID - Should throw NotFoundException")
        void getPackageById_ZeroId_ThrowsNotFoundException() {
            when(packageRepository.findByPackageIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> packageService.getPackageById(0L));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify NotFoundException is thrown for negative ID (-1L).
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches InvalidId error.
         */
        @Test
        @DisplayName("getPackageById - Negative ID (-1L) - Should throw NotFoundException")
        void getPackageById_NegativeOneId_ThrowsNotFoundException() {
            when(packageRepository.findByPackageIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> packageService.getPackageById(-1L));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify repository is called with correct parameters.
         * Expected Result: Repository method is invoked with expected arguments.
         * Assertions: verify repository findByPackageIdAndClientId is called once.
         */
        @Test
        @DisplayName("getPackageById - Verify repository interaction")
        void getPackageById_VerifyRepositoryInteraction() {
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);

            packageService.getPackageById(TEST_PACKAGE_ID);

            verify(packageRepository, times(1)).findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID);
        }

        /**
         * Purpose: Verify multiple independent calls are handled correctly.
         * Expected Result: Each call is processed independently.
         * Assertions: First call returns result, second call throws exception.
         */
        @Test
        @DisplayName("getPackageById - Multiple independent calls")
        void getPackageById_MultipleIndependentCalls() {
            when(packageRepository.findByPackageIdAndClientId(1L, TEST_CLIENT_ID)).thenReturn(testPackage);
            when(packageRepository.findByPackageIdAndClientId(999L, TEST_CLIENT_ID)).thenReturn(null);

            PackageResponseModel result1 = packageService.getPackageById(1L);
            assertNotNull(result1);

            assertThrows(NotFoundException.class, () -> packageService.getPackageById(999L));
        }

        /**
         * Purpose: Verify package with all fields populated is returned correctly.
         * Expected Result: All fields are mapped correctly.
         * Assertions: All package fields match expected values.
         */
        @Test
        @DisplayName("getPackageById - Package with all fields populated")
        void getPackageById_AllFieldsPopulated() {
            testPackage.setPackageName("Premium Box");
            testPackage.setPackageType("Premium");
            testPackage.setLength(50);
            testPackage.setBreadth(40);
            testPackage.setHeight(30);
            testPackage.setMaxWeight(BigDecimal.valueOf(25.5));
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);

            PackageResponseModel result = packageService.getPackageById(TEST_PACKAGE_ID);

            assertNotNull(result);
            assertEquals("Premium Box", result.getPackageName());
            assertEquals("Premium", result.getPackageType());
        }

        /**
         * Purpose: Verify deleted package is still retrievable by ID.
         * Expected Result: Deleted package is returned.
         * Assertions: Result is not null, isDeleted flag is true.
         */
        @Test
        @DisplayName("getPackageById - Deleted package - Should return package")
        void getPackageById_DeletedPackage_ReturnsPackage() {
            testPackage.setIsDeleted(true);
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);

            PackageResponseModel result = packageService.getPackageById(TEST_PACKAGE_ID);

            assertNotNull(result);
            assertTrue(result.getIsDeleted());
        }

        /**
         * Purpose: Additional invalid ID coverage.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches InvalidId error.
         */
        @TestFactory
        @DisplayName("getPackageById - Additional invalid IDs")
        Stream<DynamicTest> getPackageById_AdditionalInvalidIds() {
            when(packageRepository.findByPackageIdAndClientId(anyLong(), eq(TEST_CLIENT_ID))).thenReturn(null);
            return Stream.of(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, -999L, Long.MIN_VALUE + 1, Long.MAX_VALUE - 1)
                    .map(id -> DynamicTest.dynamicTest("Invalid ID: " + id, () -> {
                        NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> packageService.getPackageById(id));
                        assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
                    }));
        }
    }

    @Nested
    @DisplayName("GetPackagesInBatches Tests")
    class GetPackagesInBatchesTests {

        /**
         * Purpose: Comprehensive test for getPackagesInBatches covering invalid pagination,
         *          success without filters, and triple-loop over valid/invalid column x operator x value.
         * Expected Result: Invalid pagination throws BadRequestException, valid requests return data.
         * Assertions: Various assertions based on input combinations.
         */
        @Test
        @DisplayName("Get Packages In Batches - Comprehensive validation test")
        void getPackagesInBatches_SingleComprehensiveTest() {
            // (1) Invalid pagination - negative start
            testPaginationRequest.setStart(-1);
            testPaginationRequest.setEnd(10);
            assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.StartIndexCannotBeNegative,
                    () -> packageService.getPackagesInBatches(testPaginationRequest));

            // (2) Invalid pagination - zero end
            testPaginationRequest.setStart(0);
            testPaginationRequest.setEnd(0);
            assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.EndIndexMustBeGreaterThanZero,
                    () -> packageService.getPackagesInBatches(testPaginationRequest));

            // (3) Invalid pagination - start > end
            testPaginationRequest.setStart(10);
            testPaginationRequest.setEnd(5);
            assertThrowsBadRequest(ErrorMessages.CommonErrorMessages.StartIndexMustBeLessThanEnd,
                    () -> packageService.getPackagesInBatches(testPaginationRequest));

            // (4) Success without filters
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

            // (5) Triple-loop: valid/invalid column x operator x value
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
            String[] allOps = joinArrays(validOps.toArray(new String[0]), invalidOps);
            Set<String> uniqueOps = new HashSet<>(Arrays.asList(allOps));
            // Only use valid values and empty strings (not null) for testing
            // Service handles null values gracefully by ignoring the filter
            String[] vals = joinArrays(BATCH_VALID_VALUES, new String[]{"", " "});

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
    @DisplayName("CreatePackage Tests")
    class CreatePackageTests {

        /**
         * Purpose: Verify successful package creation with valid data.
         * Expected Result: Package is created and logged.
         * Assertions: Repository save and userLogService.logData are called.
         */
        @Test
        @DisplayName("createPackage - Success - Valid request")
        void createPackage_Success() {
            when(packageRepository.save(any(Package.class))).thenReturn(testPackage);

            assertDoesNotThrow(() -> packageService.createPackage(testPackageRequest));

            verify(packageRepository, times(1)).save(any(Package.class));
            verify(userLogService, times(1)).logData(
                    eq(TEST_USER_ID),
                    contains("Successfully inserted package"),
                    eq(ApiRoutes.PackageSubRoute.CREATE_PACKAGE));
        }

        /**
         * Purpose: Verify BadRequestException is thrown for null package name.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidPackageName error.
         */
        @Test
        @DisplayName("createPackage - Null name - Throws BadRequestException")
        void createPackage_NullName_Throws() {
            testPackageRequest.setPackageName(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageName, ex.getMessage());
        }

        /**
         * Purpose: Verify BadRequestException is thrown for empty package name.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidPackageName error.
         */
        @Test
        @DisplayName("createPackage - Empty name - Throws BadRequestException")
        void createPackage_EmptyName_Throws() {
            testPackageRequest.setPackageName("");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageName, ex.getMessage());
        }

        /**
         * Purpose: Verify BadRequestException is thrown for whitespace package name.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidPackageName error.
         */
        @Test
        @DisplayName("createPackage - Whitespace name - Throws BadRequestException")
        void createPackage_WhitespaceName_Throws() {
            testPackageRequest.setPackageName("   ");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageName, ex.getMessage());
        }

        /**
         * Purpose: Verify BadRequestException is thrown for null package type.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidPackageType error.
         */
        @Test
        @DisplayName("createPackage - Null type - Throws BadRequestException")
        void createPackage_NullType_Throws() {
            testPackageRequest.setPackageType(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageType, ex.getMessage());
        }

        /**
         * Purpose: Verify BadRequestException is thrown for whitespace package type.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidPackageType error.
         */
        @Test
        @DisplayName("createPackage - Whitespace type - Throws BadRequestException")
        void createPackage_WhitespaceType_Throws() {
            testPackageRequest.setPackageType("   ");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageType, ex.getMessage());
        }

        /**
         * Purpose: Verify BadRequestException is thrown for zero length.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidLength error.
         */
        @Test
        @DisplayName("createPackage - Zero length - Throws BadRequestException")
        void createPackage_ZeroLength_Throws() {
            testPackageRequest.setLength(0);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidLength, ex.getMessage());
        }

        /**
         * Purpose: Verify BadRequestException is thrown for negative length.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidLength error.
         */
        @Test
        @DisplayName("createPackage - Negative length - Throws BadRequestException")
        void createPackage_NegativeLength_Throws() {
            testPackageRequest.setLength(-5);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidLength, ex.getMessage());
        }

        /**
         * Purpose: Verify BadRequestException is thrown for negative breadth.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidBreadth error.
         */
        @Test
        @DisplayName("createPackage - Negative breadth - Throws BadRequestException")
        void createPackage_NegativeBreadth_Throws() {
            testPackageRequest.setBreadth(-1);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidBreadth, ex.getMessage());
        }

        /**
         * Purpose: Verify BadRequestException is thrown for zero height.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidHeight error.
         */
        @Test
        @DisplayName("createPackage - Zero height - Throws BadRequestException")
        void createPackage_ZeroHeight_Throws() {
            testPackageRequest.setHeight(0);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidHeight, ex.getMessage());
        }

        /**
         * Purpose: Verify BadRequestException is thrown for negative max weight.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidMaxWeight error.
         */
        @Test
        @DisplayName("createPackage - Negative max weight - Throws BadRequestException")
        void createPackage_NegativeMaxWeight_Throws() {
            testPackageRequest.setMaxWeight(BigDecimal.valueOf(-1.0));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidMaxWeight, ex.getMessage());
        }

        /**
         * Purpose: Verify BadRequestException is thrown for zero standard capacity.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidStandardCapacity error.
         */
        @Test
        @DisplayName("createPackage - Zero standard capacity - Throws BadRequestException")
        void createPackage_ZeroStandardCapacity_Throws() {
            testPackageRequest.setStandardCapacity(0);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidStandardCapacity, ex.getMessage());
        }

        /**
         * Purpose: Verify BadRequestException is thrown for negative price per unit.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidPricePerUnit error.
         */
        @Test
        @DisplayName("createPackage - Negative price per unit - Throws BadRequestException")
        void createPackage_NegativePricePerUnit_Throws() {
            testPackageRequest.setPricePerUnit(BigDecimal.valueOf(-1.0));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.createPackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidPricePerUnit, ex.getMessage());
        }

        /**
         * Purpose: Verify zero max weight is valid.
         * Expected Result: Package is created without exception.
         * Assertions: assertDoesNotThrow verifies success.
         */
        @Test
        @DisplayName("createPackage - Zero max weight - Success")
        void createPackage_ZeroMaxWeight_Success() {
            testPackageRequest.setMaxWeight(BigDecimal.ZERO);
            when(packageRepository.save(any(Package.class))).thenReturn(testPackage);
            assertDoesNotThrow(() -> packageService.createPackage(testPackageRequest));
        }

        /**
         * Purpose: Verify zero price per unit is valid.
         * Expected Result: Package is created without exception.
         * Assertions: assertDoesNotThrow verifies success.
         */
        @Test
        @DisplayName("createPackage - Zero price - Success")
        void createPackage_ZeroPrice_Success() {
            testPackageRequest.setPricePerUnit(BigDecimal.ZERO);
            when(packageRepository.save(any(Package.class))).thenReturn(testPackage);
            assertDoesNotThrow(() -> packageService.createPackage(testPackageRequest));
        }

        /**
         * Purpose: Additional create validations and success paths.
         * Expected Result: Invalid inputs throw BadRequestException; valid inputs succeed.
         * Assertions: Exception messages and success cases are validated.
         */
        @TestFactory
        @DisplayName("createPackage - Additional validations")
        Stream<DynamicTest> createPackage_AdditionalValidations() {
            List<DynamicTest> tests = new ArrayList<>();

            tests.add(DynamicTest.dynamicTest("Null request - InvalidRequest", () -> {
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.createPackage(null));
                assertEquals(ErrorMessages.PackageErrorMessages.InvalidRequest, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Null length - InvalidLength", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setLength(null);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.createPackage(req));
                assertEquals(ErrorMessages.PackageErrorMessages.InvalidLength, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Null breadth - InvalidBreadth", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setBreadth(null);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.createPackage(req));
                assertEquals(ErrorMessages.PackageErrorMessages.InvalidBreadth, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Null height - InvalidHeight", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setHeight(null);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.createPackage(req));
                assertEquals(ErrorMessages.PackageErrorMessages.InvalidHeight, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Null max weight - InvalidMaxWeight", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setMaxWeight(null);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.createPackage(req));
                assertEquals(ErrorMessages.PackageErrorMessages.InvalidMaxWeight, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Null standard capacity - InvalidStandardCapacity", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setStandardCapacity(null);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.createPackage(req));
                assertEquals(ErrorMessages.PackageErrorMessages.InvalidStandardCapacity, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Null price per unit - InvalidPricePerUnit", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setPricePerUnit(null);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.createPackage(req));
                assertEquals(ErrorMessages.PackageErrorMessages.InvalidPricePerUnit, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Empty package type - InvalidPackageType", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setPackageType("");
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.createPackage(req));
                assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageType, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Whitespace package name - InvalidPackageName", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setPackageName("   ");
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.createPackage(req));
                assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageName, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Length one - Success", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setLength(1);
                when(packageRepository.save(any(Package.class))).thenReturn(testPackage);
                assertDoesNotThrow(() -> packageService.createPackage(req));
            }));

            tests.add(DynamicTest.dynamicTest("Breadth one - Success", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setBreadth(1);
                when(packageRepository.save(any(Package.class))).thenReturn(testPackage);
                assertDoesNotThrow(() -> packageService.createPackage(req));
            }));

            tests.add(DynamicTest.dynamicTest("Height one - Success", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setHeight(1);
                when(packageRepository.save(any(Package.class))).thenReturn(testPackage);
                assertDoesNotThrow(() -> packageService.createPackage(req));
            }));

            tests.add(DynamicTest.dynamicTest("Standard capacity one - Success", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setStandardCapacity(1);
                when(packageRepository.save(any(Package.class))).thenReturn(testPackage);
                assertDoesNotThrow(() -> packageService.createPackage(req));
            }));

            tests.add(DynamicTest.dynamicTest("Price per unit zero - Success", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setPricePerUnit(BigDecimal.ZERO);
                when(packageRepository.save(any(Package.class))).thenReturn(testPackage);
                assertDoesNotThrow(() -> packageService.createPackage(req));
            }));

            tests.add(DynamicTest.dynamicTest("Max weight zero - Success", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setMaxWeight(BigDecimal.ZERO);
                when(packageRepository.save(any(Package.class))).thenReturn(testPackage);
                assertDoesNotThrow(() -> packageService.createPackage(req));
            }));

            return tests.stream();
        }
    }

    @Nested
    @DisplayName("UpdatePackage Tests")
    class UpdatePackageTests {

        /**
         * Purpose: Verify successful package update with valid data.
         * Expected Result: Package is updated and logged.
         * Assertions: Repository save and userLogService.logData are called.
         */
        @Test
        @DisplayName("updatePackage - Success - Valid request")
        void updatePackage_Success() {
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
            when(packageRepository.save(any(Package.class))).thenReturn(testPackage);

            assertDoesNotThrow(() -> packageService.updatePackage(testPackageRequest));

            verify(packageRepository, times(1)).findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID);
            verify(packageRepository, times(1)).save(any(Package.class));
            verify(userLogService, times(1)).logData(
                    eq(TEST_USER_ID),
                    contains("Successfully updated package"),
                    eq(ApiRoutes.PackageSubRoute.UPDATE_PACKAGE));
        }

        /**
         * Purpose: Verify NotFoundException is thrown when package is not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches InvalidId error, save is never called.
         */
        @Test
        @DisplayName("updatePackage - Package not found - Throws NotFoundException")
        void updatePackage_NotFound() {
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(null);

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> packageService.updatePackage(testPackageRequest));

            assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, exception.getMessage());
            verify(packageRepository, never()).save(any());
        }

        /**
         * Purpose: Verify BadRequestException is thrown for null package name.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidPackageName error.
         */
        @Test
        @DisplayName("updatePackage - Null name - Throws BadRequestException")
        void updatePackage_NullName_Throws() {
            testPackageRequest.setPackageName(null);
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.updatePackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageName, ex.getMessage());
        }

        /**
         * Purpose: Verify BadRequestException is thrown for invalid length.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidLength error.
         */
        @Test
        @DisplayName("updatePackage - Invalid length - Throws BadRequestException")
        void updatePackage_InvalidLength_Throws() {
            testPackageRequest.setLength(-5);
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.updatePackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidLength, ex.getMessage());
        }

        /**
         * Purpose: Verify NotFoundException is thrown for negative package ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches InvalidId error.
         */
        @Test
        @DisplayName("updatePackage - Negative ID - Throws NotFoundException")
        void updatePackage_NegativePackageId_ThrowsNotFoundException() {
            testPackageRequest.setPackageId(-1L);
            when(packageRepository.findByPackageIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> packageService.updatePackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify BadRequestException is thrown for zero breadth.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidBreadth error.
         */
        @Test
        @DisplayName("updatePackage - Zero breadth - Throws BadRequestException")
        void updatePackage_ZeroBreadth_ThrowsBadRequestException() {
            testPackageRequest.setBreadth(0);
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.updatePackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidBreadth, ex.getMessage());
        }

        /**
         * Purpose: Verify BadRequestException is thrown for zero height.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidHeight error.
         */
        @Test
        @DisplayName("updatePackage - Zero height - Throws BadRequestException")
        void updatePackage_ZeroHeight_ThrowsBadRequestException() {
            testPackageRequest.setHeight(0);
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.updatePackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidHeight, ex.getMessage());
        }

        /**
         * Purpose: Verify BadRequestException is thrown for negative standard capacity.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidStandardCapacity error.
         */
        @Test
        @DisplayName("updatePackage - Negative standard capacity - Throws BadRequestException")
        void updatePackage_NegativeStandardCapacity_ThrowsBadRequestException() {
            testPackageRequest.setStandardCapacity(-5);
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> packageService.updatePackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidStandardCapacity, ex.getMessage());
        }

        /**
         * Purpose: Verify NotFoundException is thrown for zero package ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches InvalidId error.
         */
        @Test
        @DisplayName("updatePackage - Zero ID - Throws NotFoundException")
        void updatePackage_ZeroId_ThrowsNotFoundException() {
            testPackageRequest.setPackageId(0L);
            when(packageRepository.findByPackageIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> packageService.updatePackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify NotFoundException is thrown for Long.MAX_VALUE package ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches InvalidId error.
         */
        @Test
        @DisplayName("updatePackage - Long.MAX_VALUE ID - Throws NotFoundException")
        void updatePackage_MaxLongId_ThrowsNotFoundException() {
            testPackageRequest.setPackageId(Long.MAX_VALUE);
            when(packageRepository.findByPackageIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> packageService.updatePackage(testPackageRequest));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify repository findByPackageIdAndClientId is called with correct parameters.
         * Expected Result: Repository method is invoked with expected arguments.
         * Assertions: verify repository method is called once with correct params.
         */
        @Test
        @DisplayName("updatePackage - Verify repository interaction")
        void updatePackage_VerifyRepositoryInteraction() {
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
            when(packageRepository.save(any(Package.class))).thenReturn(testPackage);

            packageService.updatePackage(testPackageRequest);

            verify(packageRepository, times(1)).findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID);
            verify(packageRepository, times(1)).save(any(Package.class));
        }

        /**
         * Purpose: Additional update validation coverage.
         * Expected Result: Invalid inputs throw BadRequestException.
         * Assertions: Exceptions match expected messages.
         */
        @TestFactory
        @DisplayName("updatePackage - Additional validations")
        Stream<DynamicTest> updatePackage_AdditionalValidations() {
            List<DynamicTest> tests = new ArrayList<>();

            tests.add(DynamicTest.dynamicTest("Null length - InvalidLength", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setPackageId(TEST_PACKAGE_ID);
                req.setLength(null);
                when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.updatePackage(req));
                assertEquals(ErrorMessages.PackageErrorMessages.InvalidLength, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Null breadth - InvalidBreadth", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setPackageId(TEST_PACKAGE_ID);
                req.setBreadth(null);
                when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.updatePackage(req));
                assertEquals(ErrorMessages.PackageErrorMessages.InvalidBreadth, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Null height - InvalidHeight", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setPackageId(TEST_PACKAGE_ID);
                req.setHeight(null);
                when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.updatePackage(req));
                assertEquals(ErrorMessages.PackageErrorMessages.InvalidHeight, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Null max weight - InvalidMaxWeight", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setPackageId(TEST_PACKAGE_ID);
                req.setMaxWeight(null);
                when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.updatePackage(req));
                assertEquals(ErrorMessages.PackageErrorMessages.InvalidMaxWeight, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Null standard capacity - InvalidStandardCapacity", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setPackageId(TEST_PACKAGE_ID);
                req.setStandardCapacity(null);
                when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.updatePackage(req));
                assertEquals(ErrorMessages.PackageErrorMessages.InvalidStandardCapacity, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Null price per unit - InvalidPricePerUnit", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setPackageId(TEST_PACKAGE_ID);
                req.setPricePerUnit(null);
                when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.updatePackage(req));
                assertEquals(ErrorMessages.PackageErrorMessages.InvalidPricePerUnit, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Null package type - InvalidPackageType", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setPackageId(TEST_PACKAGE_ID);
                req.setPackageType(null);
                when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.updatePackage(req));
                assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageType, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Empty package name - InvalidPackageName", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setPackageId(TEST_PACKAGE_ID);
                req.setPackageName("");
                when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.updatePackage(req));
                assertEquals(ErrorMessages.PackageErrorMessages.InvalidPackageName, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Negative length - InvalidLength", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setPackageId(TEST_PACKAGE_ID);
                req.setLength(-1);
                when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.updatePackage(req));
                assertEquals(ErrorMessages.PackageErrorMessages.InvalidLength, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Zero breadth - InvalidBreadth", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setPackageId(TEST_PACKAGE_ID);
                req.setBreadth(0);
                when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.updatePackage(req));
                assertEquals(ErrorMessages.PackageErrorMessages.InvalidBreadth, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Zero height - InvalidHeight", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setPackageId(TEST_PACKAGE_ID);
                req.setHeight(0);
                when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.updatePackage(req));
                assertEquals(ErrorMessages.PackageErrorMessages.InvalidHeight, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Zero standard capacity - InvalidStandardCapacity", () -> {
                PackageRequestModel req = createValidPackageRequest();
                req.setPackageId(TEST_PACKAGE_ID);
                req.setStandardCapacity(0);
                when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.updatePackage(req));
                assertEquals(ErrorMessages.PackageErrorMessages.InvalidStandardCapacity, ex.getMessage());
            }));

            return tests.stream();
        }
    }

    @Nested
    @DisplayName("TogglePackage Tests")
    class TogglePackageTests {

        /**
         * Purpose: Verify successful toggle of package status.
         * Expected Result: Package isDeleted flag is toggled and logged.
         * Assertions: Repository save and userLogService.logData are called.
         */
        @Test
        @DisplayName("togglePackage - Success - Toggles package status")
        void togglePackage_Success() {
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
            when(packageRepository.save(any(Package.class))).thenReturn(testPackage);

            assertDoesNotThrow(() -> packageService.togglePackage(TEST_PACKAGE_ID));

            verify(packageRepository, times(1)).findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID);
            verify(packageRepository, times(1)).save(testPackage);
            verify(userLogService, times(1)).logData(
                    eq(TEST_USER_ID),
                    contains("Successfully toggled package status"),
                    eq(ApiRoutes.PackageSubRoute.TOGGLE_PACKAGE));
        }

        /**
         * Purpose: Verify NotFoundException is thrown when package is not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches InvalidId error, save is never called.
         */
        @Test
        @DisplayName("togglePackage - Package not found - Throws NotFoundException")
        void togglePackage_NotFound() {
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(null);

            NotFoundException exception = assertThrows(NotFoundException.class,
                    () -> packageService.togglePackage(TEST_PACKAGE_ID));

            assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, exception.getMessage());
            verify(packageRepository, never()).save(any());
        }

        /**
         * Purpose: Verify NotFoundException is thrown for negative ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches InvalidId error.
         */
        @Test
        @DisplayName("togglePackage - Negative ID - Throws NotFoundException")
        void togglePackage_NegativeId_ThrowsNotFoundException() {
            when(packageRepository.findByPackageIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> packageService.togglePackage(-1L));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify NotFoundException is thrown for zero ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches InvalidId error.
         */
        @Test
        @DisplayName("togglePackage - Zero ID - Throws NotFoundException")
        void togglePackage_ZeroId_ThrowsNotFoundException() {
            when(packageRepository.findByPackageIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> packageService.togglePackage(0L));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify NotFoundException is thrown for Long.MAX_VALUE ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches InvalidId error.
         */
        @Test
        @DisplayName("togglePackage - Max Long ID - Throws NotFoundException")
        void togglePackage_MaxLongId_ThrowsNotFoundException() {
            when(packageRepository.findByPackageIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> packageService.togglePackage(Long.MAX_VALUE));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify NotFoundException is thrown for Long.MIN_VALUE ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches InvalidId error.
         */
        @Test
        @DisplayName("togglePackage - Min Long ID - Throws NotFoundException")
        void togglePackage_MinLongId_ThrowsNotFoundException() {
            when(packageRepository.findByPackageIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> packageService.togglePackage(Long.MIN_VALUE));
            assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
        }

        /**
         * Purpose: Verify multiple toggles correctly persist state.
         * Expected Result: isDeleted toggles between true and false.
         * Assertions: First toggle sets true, second toggle sets false.
         */
        @Test
        @DisplayName("togglePackage - Multiple Toggles - State Persistence")
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

        /**
         * Purpose: Verify toggle from deleted to active works correctly.
         * Expected Result: isDeleted changes from true to false.
         * Assertions: isDeleted is false after toggle.
         */
        @Test
        @DisplayName("togglePackage - Restore from deleted")
        void togglePackage_RestoreFromDeleted() {
            testPackage.setIsDeleted(true);
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
            when(packageRepository.save(any(Package.class))).thenReturn(testPackage);

            packageService.togglePackage(TEST_PACKAGE_ID);

            assertFalse(testPackage.getIsDeleted());
        }

        /**
         * Purpose: Verify repository interactions are correct.
         * Expected Result: Repository methods are called with expected parameters.
         * Assertions: verify repository findByPackageIdAndClientId and save are called.
         */
        @Test
        @DisplayName("togglePackage - Verify repository interaction")
        void togglePackage_VerifyRepositoryInteraction() {
            when(packageRepository.findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID)).thenReturn(testPackage);
            when(packageRepository.save(any(Package.class))).thenReturn(testPackage);

            packageService.togglePackage(TEST_PACKAGE_ID);

            verify(packageRepository, times(1)).findByPackageIdAndClientId(TEST_PACKAGE_ID, TEST_CLIENT_ID);
            verify(packageRepository, times(1)).save(testPackage);
        }

        /**
         * Purpose: Additional invalid ID coverage for toggle.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches InvalidId error.
         */
        @TestFactory
        @DisplayName("togglePackage - Additional invalid IDs")
        Stream<DynamicTest> togglePackage_AdditionalInvalidIds() {
            when(packageRepository.findByPackageIdAndClientId(anyLong(), eq(TEST_CLIENT_ID))).thenReturn(null);
            return Stream.of(2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, -999L)
                    .map(id -> DynamicTest.dynamicTest("Invalid ID: " + id, () -> {
                        NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> packageService.togglePackage(id));
                        assertEquals(ErrorMessages.PackageErrorMessages.InvalidId, ex.getMessage());
                    }));
        }
    }

    @Nested
    @DisplayName("GetPackagesByPickupLocationId Tests")
    class GetPackagesByPickupLocationIdTests {

        /**
         * Purpose: Verify successful retrieval of packages for pickup location.
         * Expected Result: List of packages for the pickup location is returned.
         * Assertions: Result is not null, size and ID match expected values.
         */
        @Test
        @DisplayName("getPackagesByPickupLocationId - Success - Returns packages")
        void getPackagesByPickupLocationId_Success() {
            when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                    .thenReturn(1L);

            List<PackagePickupLocationMapping> mappings = Arrays.asList(testMapping);
            when(packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID,
                    TEST_CLIENT_ID))
                    .thenReturn(mappings);

            List<PackageResponseModel> result = packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(TEST_PACKAGE_ID, result.get(0).getPackageId());
        }

        /**
         * Purpose: Verify empty result when no packages for pickup location.
         * Expected Result: Empty list is returned.
         * Assertions: Result is not null, isEmpty is true.
         */
        @Test
        @DisplayName("getPackagesByPickupLocationId - Empty result")
        void getPackagesByPickupLocationId_EmptyResult() {
            when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                    .thenReturn(1L);

            when(packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID,
                    TEST_CLIENT_ID))
                    .thenReturn(Arrays.asList());

            List<PackageResponseModel> result = packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        /**
         * Purpose: Verify NotFoundException is thrown for non-existent pickup location.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception is thrown.
         */
        @Test
        @DisplayName("getPackagesByPickupLocationId - Pickup location not found")
        void getPackagesByPickupLocationId_PickupLocationNotFound() {
            when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                    .thenReturn(0L);

            assertThrows(NotFoundException.class,
                    () -> packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID));
        }

        /**
         * Purpose: Verify NotFoundException is thrown for negative ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception is thrown.
         */
        @Test
        @DisplayName("getPackagesByPickupLocationId - Negative ID")
        void getPackagesByPickupLocationId_NegativeId() {
            when(pickupLocationRepository.countByPickupLocationIdAndClientId(-1L, TEST_CLIENT_ID))
                    .thenReturn(0L);

            assertThrows(NotFoundException.class,
                    () -> packageService.getPackagesByPickupLocationId(-1L));
        }

        /**
         * Purpose: Verify NotFoundException is thrown for zero ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception is thrown.
         */
        @Test
        @DisplayName("getPackagesByPickupLocationId - Zero ID")
        void getPackagesByPickupLocationId_ZeroId() {
            when(pickupLocationRepository.countByPickupLocationIdAndClientId(0L, TEST_CLIENT_ID))
                    .thenReturn(0L);

            assertThrows(NotFoundException.class,
                    () -> packageService.getPackagesByPickupLocationId(0L));
        }

        /**
         * Purpose: Verify multiple packages are returned for pickup location.
         * Expected Result: Multiple packages are returned.
         * Assertions: Size matches number of packages.
         */
        @Test
        @DisplayName("getPackagesByPickupLocationId - Multiple packages")
        void getPackagesByPickupLocationId_MultiplePackages() {
            when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                    .thenReturn(1L);

            Package pkg2 = createTestPackage();
            pkg2.setPackageId(2L);
            PackagePickupLocationMapping mapping2 = createTestPackagePickupLocationMapping();
            mapping2.setPackageId(2L);
            mapping2.setPackageEntity(pkg2);

            List<PackagePickupLocationMapping> mappings = Arrays.asList(testMapping, mapping2);
            when(packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID,
                    TEST_CLIENT_ID))
                    .thenReturn(mappings);

            List<PackageResponseModel> result = packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

            assertNotNull(result);
            assertEquals(2, result.size());
        }

        /**
         * Purpose: Verify repository methods are called correctly.
         * Expected Result: Repository methods are invoked with expected arguments.
         * Assertions: verify repository methods are called.
         */
        @Test
        @DisplayName("getPackagesByPickupLocationId - Verify repository interaction")
        void getPackagesByPickupLocationId_VerifyRepositoryInteraction() {
            when(pickupLocationRepository.countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID))
                    .thenReturn(1L);
            when(packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID,
                    TEST_CLIENT_ID))
                    .thenReturn(Arrays.asList(testMapping));

            packageService.getPackagesByPickupLocationId(TEST_PICKUP_LOCATION_ID);

            verify(pickupLocationRepository, times(1)).countByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
            verify(packagePickupLocationMappingRepository, times(1)).findByPickupLocationIdAndClientId(TEST_PICKUP_LOCATION_ID, TEST_CLIENT_ID);
        }

        /**
         * Purpose: Verify NotFoundException is thrown for Long.MAX_VALUE ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception is thrown.
         */
        @Test
        @DisplayName("getPackagesByPickupLocationId - Long.MAX_VALUE ID")
        void getPackagesByPickupLocationId_MaxLongId() {
            when(pickupLocationRepository.countByPickupLocationIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID))
                    .thenReturn(0L);

            assertThrows(NotFoundException.class,
                    () -> packageService.getPackagesByPickupLocationId(Long.MAX_VALUE));
        }

            /**
             * Purpose: Additional invalid ID coverage for pickup location lookup.
             * Expected Result: NotFoundException is thrown.
             * Assertions: Exception is thrown for each invalid ID.
             */
            @TestFactory
            @DisplayName("getPackagesByPickupLocationId - Additional invalid IDs")
            Stream<DynamicTest> getPackagesByPickupLocationId_AdditionalInvalidIds() {
                return Stream.of(2L, 3L, 4L, 5L, -10L, 0L, Long.MIN_VALUE, Long.MAX_VALUE - 1)
                    .map(id -> DynamicTest.dynamicTest("Invalid ID: " + id, () -> {
                    when(pickupLocationRepository.countByPickupLocationIdAndClientId(id, TEST_CLIENT_ID))
                        .thenReturn(0L);
                    assertThrows(NotFoundException.class,
                        () -> packageService.getPackagesByPickupLocationId(id));
                    }));
            }
    }

    @Nested
    @DisplayName("BulkCreatePackages Tests")
    class BulkCreatePackagesTests {

        /**
         * Purpose: Verify successful bulk creation with all valid packages.
         * Expected Result: All packages are created successfully.
         * Assertions: Total, success, failure counts match expected values.
         */
        @Test
        @DisplayName("bulkCreatePackages - All valid - Success")
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

        /**
         * Purpose: Verify partial success with some invalid packages.
         * Expected Result: Valid packages are created, invalid ones fail.
         * Assertions: Success and failure counts match expected values.
         */
        @Test
        @DisplayName("bulkCreatePackages - Partial Success")
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

        /**
         * Purpose: Verify database error is handled gracefully.
         * Expected Result: Failure is recorded for package that caused error.
         * Assertions: Failure count is 1.
         */
        @Test
        @DisplayName("bulkCreatePackages - Database Error")
        void bulkCreatePackages_DatabaseError() {
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

            BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(packages);

            assertNotNull(result);
            assertEquals(1, result.getTotalRequested());
            assertEquals(0, result.getSuccessCount());
            assertEquals(1, result.getFailureCount());
        }

        /**
         * Purpose: Verify BadRequestException is thrown for empty list.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message contains "cannot be null or empty".
         */
        @Test
        @DisplayName("bulkCreatePackages - Empty list - Throws BadRequestException")
        void bulkCreatePackages_EmptyList() {
            List<PackageRequestModel> packages = new ArrayList<>();

            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                packageService.bulkCreatePackages(packages);
            });
            assertTrue(exception.getMessage().contains("Package list cannot be null or empty"));
            verify(packageRepository, never()).save(any(Package.class));
        }

        /**
         * Purpose: Verify BadRequestException is thrown for null list.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message contains "cannot be null or empty".
         */
        @Test
        @DisplayName("bulkCreatePackages - Null list - Throws BadRequestException")
        void bulkCreatePackages_NullList() {
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                packageService.bulkCreatePackages(null);
            });
            assertTrue(exception.getMessage().contains("Package list cannot be null or empty"));
        }

        /**
         * Purpose: Verify mixed invalid and valid items are handled.
         * Expected Result: Valid items succeed, invalid items fail gracefully.
         * Assertions: Success count is greater than failure count.
         */
        @Test
        @DisplayName("bulkCreatePackages - Mixed invalid and valids")
        void bulkCreatePackages_MixedInvalidAndValid_PartialSuccess() {
            List<PackageRequestModel> requests = new ArrayList<>();
            requests.add(testPackageRequest);
            
            // Invalid package (empty name)
            PackageRequestModel invalidPkg = new PackageRequestModel();
            invalidPkg.setPackageName("");
            invalidPkg.setPackageType("Box");
            requests.add(invalidPkg);
            
            requests.add(testPackageRequest);

            when(packageRepository.save(any(Package.class))).thenReturn(testPackage);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
            
            BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(requests);

            assertNotNull(result);
            assertEquals(3, result.getTotalRequested());
            assertEquals(2, result.getSuccessCount());
            assertEquals(1, result.getFailureCount());
        }

        /**
         * Purpose: Verify large batch (100 items) is handled correctly.
         * Expected Result: All items are processed.
         * Assertions: Total requested matches 100.
         */
        @Test
        @DisplayName("bulkCreatePackages - Large batch (100 items)")
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
            assertEquals(100, result.getTotalRequested());
        }

        /**
         * Purpose: Verify all invalid names result in all failures.
         * Expected Result: All items fail validation.
         * Assertions: Total requested matches 5, all fail.
         */
        @Test
        @DisplayName("bulkCreatePackages - All invalid names")
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
            assertEquals(5, result.getFailureCount());
        }

        /**
         * Purpose: Verify single valid item is processed correctly.
         * Expected Result: Single item succeeds.
         * Assertions: Success count is 1.
         */
        @Test
        @DisplayName("bulkCreatePackages - Single valid item")
        void bulkCreatePackages_SingleValidItem() {
            List<PackageRequestModel> packages = new ArrayList<>();
            packages.add(testPackageRequest);

            when(packageRepository.save(any(Package.class))).thenReturn(testPackage);
            when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);

            BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(packages);

            assertNotNull(result);
            assertEquals(1, result.getTotalRequested());
            assertEquals(1, result.getSuccessCount());
            assertEquals(0, result.getFailureCount());
        }

        /**
         * Purpose: Verify single invalid item fails.
         * Expected Result: Single item fails.
         * Assertions: Failure count is 1.
         */
        @Test
        @DisplayName("bulkCreatePackages - Single invalid item")
        void bulkCreatePackages_SingleInvalidItem() {
            List<PackageRequestModel> packages = new ArrayList<>();
            PackageRequestModel invalidPkg = new PackageRequestModel();
            invalidPkg.setPackageName(null);
            packages.add(invalidPkg);

            BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(packages);

            assertNotNull(result);
            assertEquals(1, result.getTotalRequested());
            assertEquals(0, result.getSuccessCount());
            assertEquals(1, result.getFailureCount());
        }

        /**
         * Purpose: Additional bulk create validation coverage.
         * Expected Result: Invalid items fail with expected counts.
         * Assertions: Failure counts match requested counts.
         */
        @TestFactory
        @DisplayName("bulkCreatePackages - Additional invalid cases")
        Stream<DynamicTest> bulkCreatePackages_AdditionalInvalidCases() {
            List<DynamicTest> tests = new ArrayList<>();

            tests.add(DynamicTest.dynamicTest("Null request list - Throws BadRequestException", () -> {
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.bulkCreatePackages(null));
                assertTrue(ex.getMessage().contains("Package list cannot be null or empty"));
            }));

            tests.add(DynamicTest.dynamicTest("Empty list - Throws BadRequestException", () -> {
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> packageService.bulkCreatePackages(new ArrayList<>()));
                assertTrue(ex.getMessage().contains("Package list cannot be null or empty"));
            }));

            tests.addAll(Stream.of(
                    "Null name",
                    "Null type",
                    "Null length",
                    "Null breadth",
                    "Null height",
                    "Null maxWeight",
                    "Null standardCapacity",
                    "Null pricePerUnit",
                    "Length zero",
                    "Breadth zero",
                    "Height zero",
                    "Negative maxWeight"
            ).map(label -> DynamicTest.dynamicTest("Invalid package - " + label, () -> {
                PackageRequestModel req = new PackageRequestModel();
                req.setPackageName("Valid");
                req.setPackageType("BOX");
                req.setLength(10);
                req.setBreadth(10);
                req.setHeight(10);
                req.setMaxWeight(BigDecimal.ONE);
                req.setStandardCapacity(10);
                req.setPricePerUnit(BigDecimal.ONE);

                switch (label) {
                    case "Null name" -> req.setPackageName(null);
                    case "Null type" -> req.setPackageType(null);
                    case "Null length" -> req.setLength(null);
                    case "Null breadth" -> req.setBreadth(null);
                    case "Null height" -> req.setHeight(null);
                    case "Null maxWeight" -> req.setMaxWeight(null);
                    case "Null standardCapacity" -> req.setStandardCapacity(null);
                    case "Null pricePerUnit" -> req.setPricePerUnit(null);
                    case "Length zero" -> req.setLength(0);
                    case "Breadth zero" -> req.setBreadth(0);
                    case "Height zero" -> req.setHeight(0);
                    case "Negative maxWeight" -> req.setMaxWeight(new BigDecimal("-1"));
                }

                BulkInsertResponseModel<Long> result = packageService.bulkCreatePackages(List.of(req));

                assertNotNull(result);
                assertEquals(1, result.getTotalRequested());
                assertEquals(0, result.getSuccessCount());
                assertEquals(1, result.getFailureCount());
            })).toList());

            return tests.stream();
        }
    }
}
