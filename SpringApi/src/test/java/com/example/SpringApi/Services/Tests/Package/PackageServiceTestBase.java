package com.example.SpringApi.Services.Tests.Package;

import com.example.SpringApi.Authentication.Authorization;
import com.example.SpringApi.FilterQueryBuilder.PackageFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.Package;
import com.example.SpringApi.Models.DatabaseModels.PackagePickupLocationMapping;
import com.example.SpringApi.Models.RequestModels.PackageRequestModel;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Repositories.PackagePickupLocationMappingRepository;
import com.example.SpringApi.Repositories.PackageRepository;
import com.example.SpringApi.Repositories.PickupLocationRepository;
import com.example.SpringApi.Services.PackageService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Services.Interface.IPackageSubTranslator;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Base test class for PackageService tests.
 * Contains common mocks, dependencies, and setup logic shared across all PackageService test classes.
 */
@ExtendWith(MockitoExtension.class)
public abstract class PackageServiceTestBase {

    // ==================== COMMON TEST CONSTANTS ====================

    protected static final Long DEFAULT_CLIENT_ID = 100L;
    protected static final Long DEFAULT_PACKAGE_ID = 1L;
    protected static final Long DEFAULT_PICKUP_LOCATION_ID = 1L;
    protected static final String DEFAULT_PACKAGE_NAME = "Test Package";
    protected static final String DEFAULT_CREATED_USER = "admin";

    @Mock
    protected PackageRepository packageRepository;

    @Mock
    protected UserLogService userLogService;

    @Mock
    protected PackagePickupLocationMappingRepository packagePickupLocationMappingRepository;

    @Mock
    protected PickupLocationRepository pickupLocationRepository;

    @Mock
    protected PackageFilterQueryBuilder packageFilterQueryBuilder;

    @Mock
    protected HttpServletRequest request;

    @Mock
    protected Authorization authorization;

    @Mock
    IPackageSubTranslator packageServiceMock;

    @InjectMocks
    protected PackageService packageService;

    protected Package testPackage;
    protected PackageRequestModel testPackageRequest;
    protected PaginationBaseRequestModel testPaginationRequest;
    protected PackagePickupLocationMapping testMapping;

    protected static final Long TEST_PACKAGE_ID = 1L;
    protected static final Long TEST_PICKUP_LOCATION_ID = 2L;
    protected static final Long TEST_CLIENT_ID = 1L;
    protected static final Long TEST_USER_ID = 1L;

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
    }

    /**
     * Stub method: Configures packageRepository.save() to return testPackage
     * Call this method in individual test methods when needed
     */
    protected Package stubPackageRepositorySave() {
        when(packageRepository.save(any(Package.class))).thenReturn(testPackage);
        return testPackage;
    }

    /**
     * Stub method: Configures packageRepository.saveAll() to return packages with assigned IDs
     * Call this method in individual test methods when needed
     */
    protected List<Package> stubPackageRepositorySaveAll() {
        when(packageRepository.saveAll(anyList())).thenAnswer(i -> {
            java.util.List<Package> list = i.getArgument(0);
            long id = TEST_PACKAGE_ID;
            for (Package pkg : list) {
                pkg.setPackageId(id++);
            }
            return list;
        });
        return Arrays.asList(testPackage);
    }

    /**
     * Stub method: Configures request.getHeader() to return authorization token
     * Call this method in individual test methods when needed
     */
    protected String stubRequestGetHeaderAuthorization() {
        when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        return "Bearer test-token";
    }

    /**
     * Stub method: Configures RequestContextHolder with mock HTTP request
     * Call this method in individual test methods when needed
     */
    protected void stubRequestContextSetup() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("Authorization", "Bearer test-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
    }

    // ==========================================
    // ADDITIONAL STUBS
    // ==========================================

    protected void stubPackageRepositoryFindByPackageIdAndClientId(Long packageId, Long clientId, Package result) {
        lenient().when(packageRepository.findByPackageIdAndClientId(packageId, clientId)).thenReturn(result);
    }

    protected void stubPackageRepositoryFindByPackageIdAndClientIdAny(Package result) {
        lenient().when(packageRepository.findByPackageIdAndClientId(anyLong(), anyLong())).thenReturn(result);
    }

    protected void stubPackageRepositorySave(Package result) {
        lenient().when(packageRepository.save(any(Package.class))).thenReturn(result);
    }

    protected void stubPackageRepositorySaveReturnsInput() {
        lenient().when(packageRepository.save(any(Package.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    protected void stubPackageRepositorySaveThrows(RuntimeException exception) {
        lenient().when(packageRepository.save(any(Package.class))).thenThrow(exception);
    }

    protected void stubPackagePickupLocationMappingRepositoryFindByPickupLocationIdAndClientId(Long pickupLocationId,
            Long clientId, List<PackagePickupLocationMapping> result) {
        lenient().when(packagePickupLocationMappingRepository.findByPickupLocationIdAndClientId(pickupLocationId, clientId))
                .thenReturn(result);
    }

    protected void stubPickupLocationRepositoryCountByPickupLocationIdAndClientId(Long pickupLocationId, Long clientId,
            long count) {
        lenient().when(pickupLocationRepository.countByPickupLocationIdAndClientId(pickupLocationId, clientId))
                .thenReturn(count);
    }

    protected void stubUserLogServiceLogDataReturnsTrue() {
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
    }

    protected void stubUserLogServiceLogDataWithContextReturnsTrue() {
        lenient().when(userLogService.logDataWithContext(anyLong(), anyString(), anyLong(), anyString(), anyString()))
                .thenReturn(true);
    }

    protected void stubPackageServiceTogglePackageDoNothing() {
        doNothing().when(packageServiceMock).togglePackage(anyLong());
    }

    protected void stubPackageServiceGetPackagesByPickupLocationIdReturns(
            List<com.example.SpringApi.Models.ResponseModels.PackageResponseModel> result) {
        lenient().when(packageServiceMock.getPackagesByPickupLocationId(anyLong())).thenReturn(result);
    }

    protected void stubPackageServiceThrowsUnauthorizedException() {
        lenient().doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                .when(packageServiceMock).togglePackage(anyLong());
        lenient().doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(
                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                .when(packageServiceMock).getPackagesByPickupLocationId(anyLong());
    }

    // ==================== FACTORY METHODS ====================

    protected PackageRequestModel createValidPackageRequest() {
        PackageRequestModel request = new PackageRequestModel();
        request.setPackageId(DEFAULT_PACKAGE_ID);
        request.setPackageName(DEFAULT_PACKAGE_NAME);
        request.setLength(10);
        request.setBreadth(10);
        request.setHeight(10);
        request.setMaxWeight(new BigDecimal("5.00"));
        request.setStandardCapacity(100);
        request.setPricePerUnit(new BigDecimal("10.00"));
        request.setPackageType("BOX");
        request.setIsDeleted(false);
        return request;
    }

    protected Package createTestPackage() {
        return createTestPackage(DEFAULT_PACKAGE_ID);
    }

    protected Package createTestPackage(Long packageId) {
        Package pkg = new Package();
        pkg.setPackageId(packageId);
        pkg.setClientId(DEFAULT_CLIENT_ID);
        pkg.setPackageName(DEFAULT_PACKAGE_NAME);
        pkg.setLength(10);
        pkg.setBreadth(10);
        pkg.setHeight(10);
        pkg.setMaxWeight(new BigDecimal("5.00"));
        pkg.setStandardCapacity(100);
        pkg.setPricePerUnit(new BigDecimal("10.00"));
        pkg.setPackageType("BOX");
        pkg.setIsDeleted(false);
        pkg.setCreatedUser(DEFAULT_CREATED_USER);
        pkg.setModifiedUser(DEFAULT_CREATED_USER);
        pkg.setCreatedAt(LocalDateTime.now());
        pkg.setUpdatedAt(LocalDateTime.now());
        return pkg;
    }

    protected PackagePickupLocationMapping createTestPackagePickupLocationMapping() {
        return createTestPackagePickupLocationMapping(1L, DEFAULT_PACKAGE_ID, DEFAULT_PICKUP_LOCATION_ID);
    }

    protected PackagePickupLocationMapping createTestPackagePickupLocationMapping(Long mappingId, Long packageId, Long pickupLocationId) {
        PackagePickupLocationMapping mapping = new PackagePickupLocationMapping();
        mapping.setPackagePickupLocationMappingId(mappingId);
        mapping.setPackageId(packageId);
        mapping.setPickupLocationId(pickupLocationId);
        mapping.setAvailableQuantity(100);
        mapping.setReorderLevel(10);
        mapping.setMaxStockLevel(500);
        mapping.setLastRestockDate(LocalDateTime.now());
        mapping.setCreatedUser(DEFAULT_CREATED_USER);
        mapping.setModifiedUser(DEFAULT_CREATED_USER);
        mapping.setCreatedAt(LocalDateTime.now());
        mapping.setUpdatedAt(LocalDateTime.now());
        return mapping;
    }

    protected PaginationBaseRequestModel createValidPaginationRequest() {
        PaginationBaseRequestModel request = new PaginationBaseRequestModel();
        request.setStart(0);
        request.setEnd(10);
        request.setFilters(new java.util.ArrayList<>());
        return request;
    }

    protected void assertThrowsBadRequest(String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
        BadRequestException ex = assertThrows(BadRequestException.class, executable);
        assertEquals(expectedMessage, ex.getMessage());
    }

    protected void assertThrowsNotFound(String expectedMessage, org.junit.jupiter.api.function.Executable executable) {
        NotFoundException ex = assertThrows(NotFoundException.class, executable);
        assertEquals(expectedMessage, ex.getMessage());
    }
}
