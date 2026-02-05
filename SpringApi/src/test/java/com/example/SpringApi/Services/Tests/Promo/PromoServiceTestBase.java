package com.example.SpringApi.Services.Tests.Promo;

import com.example.SpringApi.FilterQueryBuilder.PromoFilterQueryBuilder;
import com.example.SpringApi.Models.DatabaseModels.Promo;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.PromoRequestModel;
import com.example.SpringApi.Repositories.PromoRepository;
import com.example.SpringApi.Services.MessageService;
import com.example.SpringApi.Services.PromoService;
import com.example.SpringApi.Services.Tests.BaseTest;
import com.example.SpringApi.Services.UserLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;

/**
 * Base test class for PromoService tests.
 * Contains common mocks, dependencies, and setup logic shared across all PromoService test classes.
 */
@ExtendWith(MockitoExtension.class)
public abstract class PromoServiceTestBase extends BaseTest {

    @Mock
    protected PromoRepository promoRepository;

    @Mock
    protected UserLogService userLogService;

    @Mock
    protected PromoFilterQueryBuilder promoFilterQueryBuilder;

    @Mock
    protected HttpServletRequest request;

    @Mock
    protected MessageService messageService;

    @Spy
    @InjectMocks
    protected PromoService promoService;

    protected Promo testPromo;
    protected PromoRequestModel testPromoRequest;
    protected PaginationBaseRequestModel testPaginationRequest;
    
    protected static final Long TEST_PROMO_ID = 1L;
    protected static final Long TEST_CLIENT_ID = 1L;
    protected static final Long TEST_USER_ID = 1L;
    protected static final String TEST_PROMO_CODE = "TEST10";
    protected static final String TEST_DESCRIPTION = "Test promo description";
    protected static final String TEST_VALID_COLUMN = "promoCode";
    protected static final String CREATED_USER = "admin";
    protected static final String TEST_LOGIN_NAME = "admin";

    @BeforeEach
    void setUp() {
        // Initialize test promo request first
        testPromoRequest = new PromoRequestModel();
        testPromoRequest.setPromoCode(TEST_PROMO_CODE);
        testPromoRequest.setDescription(TEST_DESCRIPTION);
        testPromoRequest.setIsDeleted(false);
        testPromoRequest.setIsPercent(true);
        testPromoRequest.setDiscountValue(BigDecimal.valueOf(10.0));
        testPromoRequest.setClientId(TEST_CLIENT_ID);
        testPromoRequest.setStartDate(java.time.LocalDate.now());
        testPromoRequest.setExpiryDate(java.time.LocalDate.now().plusDays(30));

        // Initialize test promo using constructor
        testPromo = new Promo(testPromoRequest, CREATED_USER, TEST_CLIENT_ID);
        testPromo.setPromoId(TEST_PROMO_ID);
        testPromo.setCreatedAt(LocalDateTime.now());
        testPromo.setUpdatedAt(LocalDateTime.now());

        // Initialize test pagination request
        testPaginationRequest = new PaginationBaseRequestModel();
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(10);
        // Set up filters using new FilterCondition structure
        PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
        filter.setColumn(TEST_VALID_COLUMN);
        filter.setOperator("equals");
        filter.setValue(TEST_PROMO_CODE);
        testPaginationRequest.setFilters(List.of(filter));
        testPaginationRequest.setLogicOperator("AND");
        testPaginationRequest.setIncludeDeleted(false);

        // Mock BaseService methods for security context (required for createPromo, etc.)
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        lenient().doReturn(TEST_USER_ID).when(promoService).getUserId();
        lenient().doReturn(TEST_LOGIN_NAME).when(promoService).getUser();
        lenient().doReturn(TEST_CLIENT_ID).when(promoService).getClientId();

        // Mock MessageService for bulk async (BulkInsertHelper calls createMessageWithContext)
        lenient().doNothing().when(messageService).createMessageWithContext(any(), anyLong(), anyString(), anyLong());
        lenient().when(userLogService.logDataWithContext(anyLong(), anyString(), anyLong(), anyString(), anyString()))
                .thenReturn(true);
    }
}
