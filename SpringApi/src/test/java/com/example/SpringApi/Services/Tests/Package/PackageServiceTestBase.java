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
import com.example.SpringApi.Services.Tests.BaseTest;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Services.Interface.IPackageSubTranslator;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.ArgumentMatchers.anyList;

/**
 * Base test class for PackageService tests.
 * Contains common mocks, dependencies, and setup logic shared across all PackageService test classes.
 */
@ExtendWith(MockitoExtension.class)
public abstract class PackageServiceTestBase extends BaseTest {

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

        lenient().when(packageRepository.save(any(Package.class))).thenReturn(testPackage);
        lenient().when(packageRepository.saveAll(anyList())).thenAnswer(i -> {
            java.util.List<Package> list = i.getArgument(0);
            long id = TEST_PACKAGE_ID;
            for (Package pkg : list) {
                pkg.setPackageId(id++);
            }
            return list;
        });
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("Authorization", "Bearer test-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));
    }
}
