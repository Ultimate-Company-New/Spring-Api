package com.example.SpringApi.Services.Tests.ProductReview;

import com.example.SpringApi.Models.DatabaseModels.ProductReview;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.ProductReviewRequestModel;
import com.example.SpringApi.Repositories.ProductReviewRepository;
import com.example.SpringApi.Services.ProductReviewService;
import com.example.SpringApi.Services.UserLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

/**
 * Base test class for ProductReviewService tests.
 * Provides common setup, test data, and helper methods for all ProductReview
 * test files.
 */
@ExtendWith(MockitoExtension.class)
public abstract class ProductReviewServiceTestBase {

    @Mock
    protected ProductReviewRepository productReviewRepository;

    @Mock
    protected UserLogService userLogService;

    @Mock
    protected com.example.SpringApi.FilterQueryBuilder.ProductReviewFilterQueryBuilder productReviewFilterQueryBuilder;

    @Mock
    protected HttpServletRequest request;

    protected ProductReviewService productReviewService;

    protected ProductReview testProductReview;
    protected ProductReviewRequestModel testProductReviewRequest;
    protected PaginationBaseRequestModel testPaginationRequest;

    protected static final Long TEST_REVIEW_ID = 1L;
    protected static final Long TEST_PRODUCT_ID = 100L;
    protected static final Long TEST_USER_ID = 1L;
    protected static final Long TEST_CLIENT_ID = 1L;
    protected static final String TEST_USER = "testuser";
    protected static final BigDecimal TEST_RATING = new BigDecimal("4.5");
    protected static final String TEST_REVIEW_TEXT = "Great product!";

    @BeforeEach
    void setUp() {
        initializeTestData();

        stubAuthorizationHeader();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("Authorization", "Bearer test-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

        // Ensure service has filter query builder injected (constructor injection)
        productReviewService = new ProductReviewService(
                productReviewRepository, userLogService, productReviewFilterQueryBuilder, request);
    }

    /**
     * Initialize common test data.
     */
    protected void initializeTestData() {
        testProductReviewRequest = new ProductReviewRequestModel();
        testProductReviewRequest.setReviewId(TEST_REVIEW_ID);
        testProductReviewRequest.setRatings(TEST_RATING);
        testProductReviewRequest.setReview(TEST_REVIEW_TEXT);
        testProductReviewRequest.setUserId(TEST_USER_ID);
        testProductReviewRequest.setProductId(TEST_PRODUCT_ID);
        testProductReviewRequest.setParentId(null);

        testProductReview = new ProductReview(testProductReviewRequest, TEST_USER);
        testProductReview.setReviewId(TEST_REVIEW_ID);
        testProductReview.setScore(5);

        testPaginationRequest = new PaginationBaseRequestModel();
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(10);
    }

    /**
     * Build a valid ProductReviewRequestModel for test purposes.
     */
    protected ProductReviewRequestModel buildValidProductReviewRequest() {
        ProductReviewRequestModel requestModel = new ProductReviewRequestModel();
        requestModel.setReviewId(TEST_REVIEW_ID);
        requestModel.setRatings(TEST_RATING);
        requestModel.setReview(TEST_REVIEW_TEXT);
        requestModel.setUserId(TEST_USER_ID);
        requestModel.setProductId(TEST_PRODUCT_ID);
        requestModel.setParentId(null);
        return requestModel;
    }

    protected void stubAuthorizationHeader() {
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
    }

    // ==========================================
    // STUBS
    // ==========================================

    protected void stubProductReviewRepositoryFindByReviewIdAndClientId(Long reviewId, Long clientId,
            ProductReview result) {
        lenient().when(productReviewRepository.findByReviewIdAndClientId(reviewId, clientId)).thenReturn(result);
    }

    protected void stubProductReviewRepositoryFindByReviewIdAndClientIdAny(ProductReview result) {
        lenient().when(productReviewRepository.findByReviewIdAndClientId(anyLong(), anyLong())).thenReturn(result);
    }

    protected void stubProductReviewRepositorySave(ProductReview result) {
        lenient().when(productReviewRepository.save(any(ProductReview.class))).thenReturn(result);
    }

    protected void stubProductReviewRepositoryMarkAllDescendantsAsDeleted(int updatedCount) {
        lenient().when(productReviewRepository.markAllDescendantsAsDeleted(eq(TEST_REVIEW_ID), anyString()))
                .thenReturn(updatedCount);
    }

    protected void stubProductReviewRepositoryMarkAllDescendantsAsDeleted(Long reviewId, int updatedCount) {
        lenient().when(productReviewRepository.markAllDescendantsAsDeleted(eq(reviewId), anyString()))
                .thenReturn(updatedCount);
    }

    protected void stubUserLogServiceLogDataReturnsTrue() {
        lenient().when(userLogService.logData(anyLong(), anyString(), anyString())).thenReturn(true);
    }

    protected void stubProductReviewFilterQueryBuilderReturn(
            org.springframework.data.domain.Page<ProductReview> result) {
        lenient().when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), anyLong(), any(), anyString(), any(), anyBoolean(),
                any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(result);
    }

}
