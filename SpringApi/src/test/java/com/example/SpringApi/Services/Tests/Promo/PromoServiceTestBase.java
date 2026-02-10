package com.example.SpringApi.Services.Tests.Promo;

import com.example.SpringApi.Controllers.PromoController;
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
 * Contains common mocks, dependencies, and setup logic shared across all
 * PromoService test classes.
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

        protected PromoController promoController;

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
                // Initialize test data
                initializeTestData();

                // Infrastructure stubs (lenient) - used only for base service functionality
                stubSecurityContext();
                stubInfrastructureServices();

                // Initialize controller with spied service
                promoController = new PromoController(promoService);
        }

        /**
         * Initializes common test data objects.
         */
        private void initializeTestData() {
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

                PaginationBaseRequestModel.FilterCondition filter = new PaginationBaseRequestModel.FilterCondition();
                filter.setColumn(TEST_VALID_COLUMN);
                filter.setOperator("equals");
                filter.setValue(TEST_PROMO_CODE);
                testPaginationRequest.setFilters(List.of(filter));
                testPaginationRequest.setLogicOperator("AND");
                testPaginationRequest.setIncludeDeleted(false);
        }

        /**
         * Stubs security context methods on the spy service.
         */
        protected void stubSecurityContext() {
                lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
                lenient().doReturn(TEST_USER_ID).when(promoService).getUserId();
                lenient().doReturn(TEST_LOGIN_NAME).when(promoService).getUser();
                lenient().doReturn(TEST_CLIENT_ID).when(promoService).getClientId();
        }

        /**
         * Stubs infrastructure services like MessageService and UserLogService.
         */
        protected void stubInfrastructureServices() {
                lenient().doNothing().when(messageService).createMessageWithContext(any(), anyLong(), anyString(),
                                anyLong());
                lenient().when(userLogService.logDataWithContext(anyLong(), anyString(), anyLong(), anyString(),
                                anyString()))
                                .thenReturn(true);
                lenient().when(promoFilterQueryBuilder.getColumnType(anyString())).thenReturn("string");
                lenient().when(promoFilterQueryBuilder.getColumnType("promoId")).thenReturn("number");
                lenient().when(promoFilterQueryBuilder.getColumnType("discountValue")).thenReturn("number");
                lenient().when(promoFilterQueryBuilder.getColumnType("isPercent")).thenReturn("boolean");
                lenient().when(promoFilterQueryBuilder.getColumnType("isDeleted")).thenReturn("boolean");
                lenient().when(promoFilterQueryBuilder.getColumnType("startDate")).thenReturn("date");
                lenient().when(promoFilterQueryBuilder.getColumnType("expiryDate")).thenReturn("date");
                lenient().when(promoFilterQueryBuilder.getColumnType("createdAt")).thenReturn("date");
                lenient().when(promoFilterQueryBuilder.getColumnType("updatedAt")).thenReturn("date");
        }

        /**
         * Stubs the service to throw UnauthorizedException for controller permission
         * tests.
         */
        protected void stubServiceThrowsUnauthorizedException() {
                lenient()
                                .doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(
                                                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                                .when(promoService).createPromo(any());
                lenient()
                                .doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(
                                                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                                .when(promoService).togglePromo(anyLong());
                lenient()
                                .doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(
                                                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                                .when(promoService).getPromoDetailsById(anyLong());
                lenient()
                                .doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(
                                                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                                .when(promoService).getPromoDetailsByName(anyString());
                lenient()
                                .doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(
                                                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                                .when(promoService).getPromosInBatches(any());
                lenient()
                                .doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(
                                                com.example.SpringApi.ErrorMessages.ERROR_UNAUTHORIZED))
                                .when(promoService).bulkCreatePromosAsync(any(), anyLong(), anyString(), anyLong());
        }
}
