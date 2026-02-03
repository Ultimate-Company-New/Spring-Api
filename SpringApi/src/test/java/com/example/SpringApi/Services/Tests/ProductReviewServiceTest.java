package com.example.SpringApi.Services.Tests;

import com.example.SpringApi.Models.DatabaseModels.ProductReview;
import com.example.SpringApi.Models.RequestModels.PaginationBaseRequestModel;
import com.example.SpringApi.Models.RequestModels.ProductReviewRequestModel;
import com.example.SpringApi.Models.ResponseModels.PaginationBaseResponseModel;
import com.example.SpringApi.Models.ResponseModels.ProductReviewResponseModel;
import com.example.SpringApi.Repositories.ProductReviewRepository;
import com.example.SpringApi.Services.ProductReviewService;
import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductReviewService.
 *
 * Test Group Summary:
 * | Group Name                              | Number of Tests |
 * | :-------------------------------------- | :-------------- |
 * | InsertProductReviewTests                | 38              |
 * | GetProductReviewsInBatchesTests         | 1               |
 * | ToggleProductReviewTests                | 12              |
 * | SetProductReviewScoreTests              | 18              |
 * | **Total**                               | **69**          |
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductReviewService Unit Tests")
class ProductReviewServiceTest {

    @Mock
    private ProductReviewRepository productReviewRepository;

    @Mock
    private UserLogService userLogService;

    @Mock
    private com.example.SpringApi.FilterQueryBuilder.ProductReviewFilterQueryBuilder productReviewFilterQueryBuilder;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ProductReviewService productReviewService;

    private ProductReview testProductReview;
    private ProductReviewRequestModel testProductReviewRequest;
    private PaginationBaseRequestModel testPaginationRequest;

    private static final Long TEST_REVIEW_ID = 1L;
    private static final Long TEST_PRODUCT_ID = 100L;
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_CLIENT_ID = 1L;
    private static final String TEST_USER = "testuser";
    private static final BigDecimal TEST_RATING = new BigDecimal("4.5");
    private static final String TEST_REVIEW_TEXT = "Great product!";

    @BeforeEach
    void setUp() {
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

        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("Authorization", "Bearer test-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest));

        // Ensure service has filter query builder injected (constructor injection)
        productReviewService = new ProductReviewService(
                productReviewRepository, userLogService, productReviewFilterQueryBuilder, request);
    }

    @Nested
    @DisplayName("InsertProductReviewTests")
    class InsertProductReviewTests {

        /**
         * Purpose: Verify successful product review insertion.
         * Expected Result: Review is saved and logging is performed.
         * Assertions: Repository save is called once, userLogService is called.
         */
        @Test
        @DisplayName("Insert Product Review - Success - Should create and save review")
        void insertProductReview_Success() {
            when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

            productReviewService.insertProductReview(testProductReviewRequest);

            verify(productReviewRepository, times(1)).save(any(ProductReview.class));
            verify(userLogService, times(1)).logData(
                eq(TEST_USER_ID.longValue()),
                eq("Successfully inserted product review. null"),
                eq("insertProductReview")
            );
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for null request.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches InvalidId error, repository save is never called.
         */
        @Test
        @DisplayName("Insert Product Review - Null Request - Throws BadRequestException")
        void insertProductReview_NullRequest_ThrowsBadRequestException() {
            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productReviewService.insertProductReview(null)
            );

            assertEquals(ErrorMessages.ProductReviewErrorMessages.InvalidId, exception.getMessage());
            verify(productReviewRepository, never()).save(any(ProductReview.class));
            verify(userLogService, never()).logData(anyLong(), any(), any());
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for invalid ratings (negative).
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches ER001 error.
         */
        @Test
        @DisplayName("Insert Product Review - Invalid Ratings (Negative) - Throws BadRequestException")
        void insertProductReview_InvalidRatings_ThrowsBadRequestException() {
            testProductReviewRequest.setRatings(new BigDecimal("-1.0"));

            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productReviewService.insertProductReview(testProductReviewRequest)
            );

            assertEquals(ErrorMessages.ProductReviewErrorMessages.ER001, exception.getMessage());
            verify(productReviewRepository, never()).save(any(ProductReview.class));
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for ratings above 5.0.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches ER001 error.
         */
        @Test
        @DisplayName("Insert Product Review - Ratings Too High (> 5.0) - Throws BadRequestException")
        void insertProductReview_RatingsTooHigh_ThrowsBadRequestException() {
            testProductReviewRequest.setRatings(new BigDecimal("6.0"));

            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productReviewService.insertProductReview(testProductReviewRequest)
            );

            assertEquals(ErrorMessages.ProductReviewErrorMessages.ER001, exception.getMessage());
            verify(productReviewRepository, never()).save(any(ProductReview.class));
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for null review text.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches ER002 error.
         */
        @Test
        @DisplayName("Insert Product Review - Null Review Text - Throws BadRequestException")
        void insertProductReview_NullReviewText_ThrowsBadRequestException() {
            testProductReviewRequest.setReview(null);

            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productReviewService.insertProductReview(testProductReviewRequest)
            );

            assertEquals(ErrorMessages.ProductReviewErrorMessages.ER002, exception.getMessage());
            verify(productReviewRepository, never()).save(any(ProductReview.class));
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for empty review text.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches ER002 error.
         */
        @Test
        @DisplayName("Insert Product Review - Empty Review Text - Throws BadRequestException")
        void insertProductReview_EmptyReviewText_ThrowsBadRequestException() {
            testProductReviewRequest.setReview("");

            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productReviewService.insertProductReview(testProductReviewRequest)
            );

            assertEquals(ErrorMessages.ProductReviewErrorMessages.ER002, exception.getMessage());
            verify(productReviewRepository, never()).save(any(ProductReview.class));
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for null user ID.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches ER003 error.
         */
        @Test
        @DisplayName("Insert Product Review - Null User ID - Throws BadRequestException")
        void insertProductReview_NullUserId_ThrowsBadRequestException() {
            testProductReviewRequest.setUserId(null);

            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productReviewService.insertProductReview(testProductReviewRequest)
            );

            assertEquals(ErrorMessages.ProductReviewErrorMessages.ER003, exception.getMessage());
            verify(productReviewRepository, never()).save(any(ProductReview.class));
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for zero user ID.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches ER003 error.
         */
        @Test
        @DisplayName("Insert Product Review - Zero User ID - Throws BadRequestException")
        void insertProductReview_ZeroUserId_ThrowsBadRequestException() {
            testProductReviewRequest.setUserId(0L);

            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productReviewService.insertProductReview(testProductReviewRequest)
            );

            assertEquals(ErrorMessages.ProductReviewErrorMessages.ER003, exception.getMessage());
            verify(productReviewRepository, never()).save(any(ProductReview.class));
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for null product ID.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches ER004 error.
         */
        @Test
        @DisplayName("Insert Product Review - Null Product ID - Throws BadRequestException")
        void insertProductReview_NullProductId_ThrowsBadRequestException() {
            testProductReviewRequest.setProductId(null);

            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productReviewService.insertProductReview(testProductReviewRequest)
            );

            assertEquals(ErrorMessages.ProductReviewErrorMessages.ER004, exception.getMessage());
            verify(productReviewRepository, never()).save(any(ProductReview.class));
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for zero product ID.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches ER004 error.
         */
        @Test
        @DisplayName("Insert Product Review - Zero Product ID - Throws BadRequestException")
        void insertProductReview_ZeroProductId_ThrowsBadRequestException() {
            testProductReviewRequest.setProductId(0L);

            BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productReviewService.insertProductReview(testProductReviewRequest)
            );

            assertEquals(ErrorMessages.ProductReviewErrorMessages.ER004, exception.getMessage());
            verify(productReviewRepository, never()).save(any(ProductReview.class));
        }

        /**
         * Purpose: Verify that zero rating is allowed.
         * Expected Result: Review is saved without exception.
         * Assertions: assertDoesNotThrow verifies success.
         */
        @Test
        @DisplayName("Insert Product Review - Rating Zero - Success")
        void insertProductReview_RatingZero_Success() {
            testProductReviewRequest.setRatings(BigDecimal.ZERO);

            assertDoesNotThrow(() -> productReviewService.insertProductReview(testProductReviewRequest));
        }

        /**
         * Purpose: Verify successful insertion with rating exactly 1.
         * Expected Result: Review is saved without exception.
         * Assertions: assertDoesNotThrow verifies success.
         */
        @Test
        @DisplayName("Insert Product Review - Rating Exactly 1 - Success")
        void insertProductReview_RatingOne_Success() {
            testProductReviewRequest.setRatings(BigDecimal.ONE);
            when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
            assertDoesNotThrow(() -> productReviewService.insertProductReview(testProductReviewRequest));
        }

        /**
         * Purpose: Verify successful insertion with rating exactly 5.
         * Expected Result: Review is saved without exception.
         * Assertions: assertDoesNotThrow verifies success.
         */
        @Test
        @DisplayName("Insert Product Review - Rating Exactly 5 - Success")
        void insertProductReview_RatingFive_Success() {
            testProductReviewRequest.setRatings(new BigDecimal("5.0"));
            when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
            assertDoesNotThrow(() -> productReviewService.insertProductReview(testProductReviewRequest));
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for whitespace review text.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches ER002 error.
         */
        @Test
        @DisplayName("Insert Product Review - Whitespace Review Text - Throws BadRequestException")
        void insertProductReview_WhitespaceReviewText_ThrowsBadRequestException() {
            testProductReviewRequest.setReview("   ");

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> productReviewService.insertProductReview(testProductReviewRequest));
            assertEquals(ErrorMessages.ProductReviewErrorMessages.ER002, ex.getMessage());
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for negative user ID.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches ER003 error.
         */
        @Test
        @DisplayName("Insert Product Review - Negative User ID - Throws BadRequestException")
        void insertProductReview_NegativeUserId_ThrowsBadRequestException() {
            testProductReviewRequest.setUserId(-1L);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> productReviewService.insertProductReview(testProductReviewRequest));
            assertEquals(ErrorMessages.ProductReviewErrorMessages.ER003, ex.getMessage());
        }

        /**
         * Purpose: Verify that BadRequestException is thrown for negative product ID.
         * Expected Result: BadRequestException is thrown.
         * Assertions: Exception message matches ER004 error.
         */
        @Test
        @DisplayName("Insert Product Review - Negative Product ID - Throws BadRequestException")
        void insertProductReview_NegativeProductId_ThrowsBadRequestException() {
            testProductReviewRequest.setProductId(-1L);

            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> productReviewService.insertProductReview(testProductReviewRequest));
            assertEquals(ErrorMessages.ProductReviewErrorMessages.ER004, ex.getMessage());
        }

        /**
         * Purpose: Add dynamic coverage for additional insert validations and success paths.
         * Expected Result: Invalid inputs throw BadRequestException; valid inputs succeed.
         * Assertions: Exceptions match expected messages; valid cases do not throw.
         */
        @TestFactory
        @DisplayName("Insert Product Review - Additional validation cases")
        Stream<DynamicTest> insertProductReview_AdditionalValidationCases() {
            List<DynamicTest> tests = new ArrayList<>();

            tests.add(DynamicTest.dynamicTest("Ratings null - ER001", () -> {
                ProductReviewRequestModel request = buildValidProductReviewRequest();
                request.setRatings(null);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> productReviewService.insertProductReview(request));
                assertEquals(ErrorMessages.ProductReviewErrorMessages.ER001, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Ratings negative - ER001", () -> {
                ProductReviewRequestModel request = buildValidProductReviewRequest();
                request.setRatings(new BigDecimal("-0.1"));
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> productReviewService.insertProductReview(request));
                assertEquals(ErrorMessages.ProductReviewErrorMessages.ER001, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Ratings above max - ER001", () -> {
                ProductReviewRequestModel request = buildValidProductReviewRequest();
                request.setRatings(new BigDecimal("5.01"));
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> productReviewService.insertProductReview(request));
                assertEquals(ErrorMessages.ProductReviewErrorMessages.ER001, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Review text whitespace - ER002", () -> {
                ProductReviewRequestModel request = buildValidProductReviewRequest();
                request.setReview("\n\t ");
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> productReviewService.insertProductReview(request));
                assertEquals(ErrorMessages.ProductReviewErrorMessages.ER002, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("UserId negative - ER003", () -> {
                ProductReviewRequestModel request = buildValidProductReviewRequest();
                request.setUserId(-99L);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> productReviewService.insertProductReview(request));
                assertEquals(ErrorMessages.ProductReviewErrorMessages.ER003, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("ProductId negative - ER004", () -> {
                ProductReviewRequestModel request = buildValidProductReviewRequest();
                request.setProductId(-99L);
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> productReviewService.insertProductReview(request));
                assertEquals(ErrorMessages.ProductReviewErrorMessages.ER004, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("ParentId set - Success", () -> {
                ProductReviewRequestModel request = buildValidProductReviewRequest();
                request.setParentId(555L);
                when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
                assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
                verify(productReviewRepository, atLeastOnce()).save(any(ProductReview.class));
            }));

            tests.add(DynamicTest.dynamicTest("Ratings at min boundary (0.0) - Success", () -> {
                ProductReviewRequestModel request = buildValidProductReviewRequest();
                request.setRatings(new BigDecimal("0.0"));
                when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
                assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
            }));

            tests.add(DynamicTest.dynamicTest("Ratings at max boundary (5.0) - Success", () -> {
                ProductReviewRequestModel request = buildValidProductReviewRequest();
                request.setRatings(new BigDecimal("5.0"));
                when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
                assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
            }));

            tests.add(DynamicTest.dynamicTest("Review text long - Success", () -> {
                ProductReviewRequestModel request = buildValidProductReviewRequest();
                request.setReview("x".repeat(500));
                when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
                assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
            }));

            tests.add(DynamicTest.dynamicTest("Review text single char - Success", () -> {
                ProductReviewRequestModel request = buildValidProductReviewRequest();
                request.setReview("A");
                when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
                assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
            }));

            tests.add(DynamicTest.dynamicTest("UserId large - Success", () -> {
                ProductReviewRequestModel request = buildValidProductReviewRequest();
                request.setUserId(Long.MAX_VALUE - 1);
                when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
                assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
            }));

            tests.add(DynamicTest.dynamicTest("ProductId large - Success", () -> {
                ProductReviewRequestModel request = buildValidProductReviewRequest();
                request.setProductId(Long.MAX_VALUE - 2);
                when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
                assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
            }));

            tests.add(DynamicTest.dynamicTest("Null request - InvalidId", () -> {
                BadRequestException ex = assertThrows(BadRequestException.class,
                        () -> productReviewService.insertProductReview(null));
                assertEquals(ErrorMessages.ProductReviewErrorMessages.InvalidId, ex.getMessage());
            }));

            tests.add(DynamicTest.dynamicTest("Ratings valid decimal - Success", () -> {
                ProductReviewRequestModel request = buildValidProductReviewRequest();
                request.setRatings(new BigDecimal("4.25"));
                when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
                assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
            }));

            tests.add(DynamicTest.dynamicTest("ProductId 1 - Success", () -> {
                ProductReviewRequestModel request = buildValidProductReviewRequest();
                request.setProductId(1L);
                when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
                assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
            }));

            tests.add(DynamicTest.dynamicTest("UserId 1 - Success", () -> {
                ProductReviewRequestModel request = buildValidProductReviewRequest();
                request.setUserId(1L);
                when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
                assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
            }));

            tests.add(DynamicTest.dynamicTest("Review text with trailing spaces - Success", () -> {
                ProductReviewRequestModel request = buildValidProductReviewRequest();
                request.setReview("Good product   ");
                when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
                assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
            }));

            tests.add(DynamicTest.dynamicTest("Ratings exactly 5 - Success (duplicate)", () -> {
                ProductReviewRequestModel request = buildValidProductReviewRequest();
                request.setRatings(new BigDecimal("5.0"));
                when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
                assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
            }));

            return tests.stream();
        }

    }

    @Nested
    @DisplayName("GetProductReviewsInBatchesTests")
    class GetProductReviewsInBatchesTests {

        /**
         * Purpose: Verify successful retrieval of product reviews in batches.
         * Expected Result: Paginated reviews are returned correctly.
         * Assertions: Result is not null, data size and total count match expected values.
         */
        @Test
        @DisplayName("Get Product Reviews In Batches - Invalid pagination, success, empty")
        void getProductReviewsInBatchesGivenProductId_Comprehensive() {
            // Invalid pagination (end <= start)
            PaginationBaseRequestModel invalid = new PaginationBaseRequestModel();
            invalid.setStart(10);
            invalid.setEnd(5);
            BadRequestException invalidEx = assertThrows(BadRequestException.class,
                    () -> productReviewService.getProductReviewsInBatchesGivenProductId(invalid, TEST_PRODUCT_ID));
            assertTrue(invalidEx.getMessage().contains("Invalid pagination"));

            // Success with one review
            List<ProductReview> reviewList = Arrays.asList(testProductReview);
            Page<ProductReview> reviewPage = new PageImpl<>(reviewList, PageRequest.of(0, 10), 1);

            when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), eq(TEST_PRODUCT_ID), any(), anyString(), any(), anyBoolean(), any(Pageable.class)))
                .thenReturn(reviewPage);

            PaginationBaseResponseModel<ProductReviewResponseModel> success =
                productReviewService.getProductReviewsInBatchesGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

            assertNotNull(success);
            assertNotNull(success.getData());
            assertEquals(1, success.getData().size());
            assertEquals(1L, success.getTotalDataCount());

            // Empty results
            Page<ProductReview> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);
            when(productReviewFilterQueryBuilder.findPaginatedEntitiesWithMultipleFilters(
                anyLong(), eq(TEST_PRODUCT_ID), any(), anyString(), any(), anyBoolean(), any(Pageable.class)))
                .thenReturn(emptyPage);

            PaginationBaseResponseModel<ProductReviewResponseModel> empty =
                productReviewService.getProductReviewsInBatchesGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

            assertNotNull(empty);
            assertNotNull(empty.getData());
            assertEquals(0, empty.getData().size());
            assertEquals(0L, empty.getTotalDataCount());
        }

    }

    @Nested
    @DisplayName("ToggleProductReviewTests")
    class ToggleProductReviewTests {

        /**
         * Purpose: Verify successful toggle (mark as deleted).
         * Expected Result: Review is marked as deleted.
         * Assertions: isDeleted is true after toggle, repository methods are called.
         */
        @Test
        @DisplayName("Toggle Product Review - Success - Mark as Deleted")
        void toggleProductReview_Success_MarkAsDeleted() {
            testProductReview.setIsDeleted(false);
            when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID)).thenReturn(testProductReview);
            when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
            when(productReviewRepository.markAllDescendantsAsDeleted(eq(TEST_REVIEW_ID), anyString())).thenReturn(2);

            productReviewService.toggleProductReview(TEST_REVIEW_ID);

            assertTrue(testProductReview.getIsDeleted());
            verify(productReviewRepository, times(1)).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
            verify(productReviewRepository, times(1)).save(testProductReview);
            verify(productReviewRepository, times(1)).markAllDescendantsAsDeleted(eq(TEST_REVIEW_ID), anyString());
        }

        /**
         * Purpose: Verify successful toggle (restore from deleted).
         * Expected Result: Review is restored.
         * Assertions: isDeleted is false after toggle, descendants are not affected.
         */
        @Test
        @DisplayName("Toggle Product Review - Success - Restore from Deleted")
        void toggleProductReview_Success_RestoreFromDeleted() {
            testProductReview.setIsDeleted(true);
            when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID)).thenReturn(testProductReview);
            when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

            productReviewService.toggleProductReview(TEST_REVIEW_ID);

            assertFalse(testProductReview.getIsDeleted());
            verify(productReviewRepository, times(1)).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
            verify(productReviewRepository, times(1)).save(testProductReview);
            verify(productReviewRepository, never()).markAllDescendantsAsDeleted(any(), any());
        }

        /**
         * Purpose: Verify that NotFoundException is thrown when review is not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches NotFound error, save is never called.
         */
        @Test
        @DisplayName("Toggle Product Review - Review Not Found")
        void toggleProductReview_ReviewNotFound_ThrowsNotFoundException() {
            lenient().when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID)).thenReturn(null);

            NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> productReviewService.toggleProductReview(TEST_REVIEW_ID)
            );

            assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, exception.getMessage());
            verify(productReviewRepository, times(1)).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
            verify(productReviewRepository, never()).save(any(ProductReview.class));
        }

        /**
         * Purpose: Verify that NotFoundException is thrown for negative ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches NotFound error.
         */
        @Test
        @DisplayName("Toggle Product Review - Negative ID - Not Found")
        void toggleProductReview_NegativeId_ThrowsNotFoundException() {
            when(productReviewRepository.findByReviewIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> productReviewService.toggleProductReview(-1L));
            assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
        }

        /**
         * Purpose: Verify that NotFoundException is thrown for zero ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches NotFound error.
         */
        @Test
        @DisplayName("Toggle Product Review - Zero ID - Not Found")
        void toggleProductReview_ZeroId_ThrowsNotFoundException() {
            when(productReviewRepository.findByReviewIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> productReviewService.toggleProductReview(0L));
            assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
        }

        /**
         * Purpose: Verify that multiple toggles correctly persist state.
         * Expected Result: isDeleted toggles between true and false.
         * Assertions: First toggle sets true, second toggle sets false.
         */
        @Test
        @DisplayName("Toggle Product Review - Multiple Toggles - State Persistence")
        void toggleProductReview_MultipleToggles_StatePersists() {
            testProductReview.setIsDeleted(false);
            when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                    .thenReturn(testProductReview);
            when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

            productReviewService.toggleProductReview(TEST_REVIEW_ID);
            assertTrue(testProductReview.getIsDeleted());

            productReviewService.toggleProductReview(TEST_REVIEW_ID);
            assertFalse(testProductReview.getIsDeleted());
        }

        /**
         * Purpose: Add dynamic coverage for additional toggle paths.
         * Expected Result: Invalid IDs throw NotFoundException.
         * Assertions: Exception message matches NotFound error.
         */
        @TestFactory
        @DisplayName("Toggle Product Review - Additional invalid IDs")
        Stream<DynamicTest> toggleProductReview_AdditionalInvalidIds() {
            when(productReviewRepository.findByReviewIdAndClientId(anyLong(), anyLong())).thenReturn(null);

            return Stream.of(-100L, 2L, 999L, Long.MIN_VALUE, Long.MAX_VALUE, 12345L)
                    .map(id -> DynamicTest.dynamicTest("Not Found for ID: " + id, () -> {
                        NotFoundException ex = assertThrows(NotFoundException.class,
                                () -> productReviewService.toggleProductReview(id));
                        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
                    }));
        }
    }

    @Nested
    @DisplayName("SetProductReviewScoreTests")
    class SetProductReviewScoreTests {

        /**
         * Purpose: Verify successful score increase.
         * Expected Result: Review score is incremented.
         * Assertions: Score increases from 3 to 4.
         */
        @Test
        @DisplayName("Set Product Review Score - Increase Score")
        void setProductReviewScore_Success_IncreaseScore() {
            testProductReview.setScore(3);
            when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID)).thenReturn(testProductReview);
            when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

            productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

            assertEquals(4, testProductReview.getScore());
            verify(productReviewRepository, times(1)).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
            verify(productReviewRepository, times(1)).save(testProductReview);
        }

        /**
         * Purpose: Verify successful score decrease.
         * Expected Result: Review score is decremented.
         * Assertions: Score decreases from 3 to 2.
         */
        @Test
        @DisplayName("Set Product Review Score - Decrease Score")
        void setProductReviewScore_Success_DecreaseScore() {
            testProductReview.setScore(3);
            when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID)).thenReturn(testProductReview);
            when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

            productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

            assertEquals(2, testProductReview.getScore());
            verify(productReviewRepository, times(1)).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
            verify(productReviewRepository, times(1)).save(testProductReview);
        }

        /**
         * Purpose: Verify score does not go below zero.
         * Expected Result: Score stays at zero.
         * Assertions: Score remains 0 when attempting to decrease from 0.
         */
        @Test
        @DisplayName("Set Product Review Score - Decrease to Zero Minimum")
        void setProductReviewScore_Success_DecreaseToZero() {
            testProductReview.setScore(0);
            when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID)).thenReturn(testProductReview);
            when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

            productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

            assertEquals(0, testProductReview.getScore());
            verify(productReviewRepository, times(1)).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
            verify(productReviewRepository, times(1)).save(testProductReview);
        }

        /**
         * Purpose: Verify null score is treated as zero.
         * Expected Result: Score becomes 1 after increase.
         * Assertions: Score is 1 after incrementing from null.
         */
        @Test
        @DisplayName("Set Product Review Score - Null Score Treated as Zero")
        void setProductReviewScore_Success_NullScore() {
            testProductReview.setScore(null);
            when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID)).thenReturn(testProductReview);
            when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

            productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

            assertEquals(1, testProductReview.getScore());
        }

        /**
         * Purpose: Verify that NotFoundException is thrown when review is not found.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches NotFound error.
         */
        @Test
        @DisplayName("Set Product Review Score - Review Not Found")
        void setProductReviewScore_ReviewNotFound_ThrowsNotFoundException() {
            lenient().when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID)).thenReturn(null);

            NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> productReviewService.setProductReviewScore(TEST_REVIEW_ID, true)
            );

            assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, exception.getMessage());
            verify(productReviewRepository, times(1)).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
            verify(productReviewRepository, never()).save(any(ProductReview.class));
        }

        /**
         * Purpose: Verify that NotFoundException is thrown for negative ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches NotFound error.
         */
        @Test
        @DisplayName("Set Product Review Score - Negative ID - Not Found")
        void setProductReviewScore_NegativeId_ThrowsNotFoundException() {
            when(productReviewRepository.findByReviewIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> productReviewService.setProductReviewScore(-1L, true));
            assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
        }

        /**
         * Purpose: Verify that NotFoundException is thrown for zero ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches NotFound error.
         */
        @Test
        @DisplayName("Set Product Review Score - Zero ID - Not Found")
        void setProductReviewScore_ZeroId_ThrowsNotFoundException() {
            when(productReviewRepository.findByReviewIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> productReviewService.setProductReviewScore(0L, true));
            assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
        }

        /**
         * Purpose: Verify that NotFoundException is thrown for Long.MAX_VALUE ID.
         * Expected Result: NotFoundException is thrown.
         * Assertions: Exception message matches NotFound error.
         */
        @Test
        @DisplayName("Set Product Review Score - Max Long ID - Not Found")
        void setProductReviewScore_MaxLongId_ThrowsNotFoundException() {
            when(productReviewRepository.findByReviewIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
            NotFoundException ex = assertThrows(NotFoundException.class,
                    () -> productReviewService.setProductReviewScore(Long.MAX_VALUE, true));
            assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
        }

        /**
         * Purpose: Add dynamic coverage for additional score update paths.
         * Expected Result: Score updates correctly; invalid IDs throw NotFoundException.
         * Assertions: Score values and exceptions are as expected.
         */
        @TestFactory
        @DisplayName("Set Product Review Score - Additional paths")
        Stream<DynamicTest> setProductReviewScore_AdditionalPaths() {
            List<DynamicTest> tests = new ArrayList<>();

            tests.add(DynamicTest.dynamicTest("Decrease when score null - stays 0", () -> {
                testProductReview.setScore(null);
                when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                        .thenReturn(testProductReview);
                when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
                productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);
                assertEquals(0, testProductReview.getScore());
            }));

            tests.add(DynamicTest.dynamicTest("Increase when score 0 - becomes 1", () -> {
                testProductReview.setScore(0);
                when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                        .thenReturn(testProductReview);
                when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
                productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);
                assertEquals(1, testProductReview.getScore());
            }));

            tests.add(DynamicTest.dynamicTest("Decrease when score 1 - becomes 0", () -> {
                testProductReview.setScore(1);
                when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                        .thenReturn(testProductReview);
                when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
                productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);
                assertEquals(0, testProductReview.getScore());
            }));

            tests.add(DynamicTest.dynamicTest("Increase when score 10 - becomes 11", () -> {
                testProductReview.setScore(10);
                when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                        .thenReturn(testProductReview);
                when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
                productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);
                assertEquals(11, testProductReview.getScore());
            }));

            for (long id : new long[] { -5L, 2L, 99L, Long.MIN_VALUE, Long.MAX_VALUE }) {
                tests.add(DynamicTest.dynamicTest("Not Found for ID: " + id, () -> {
                    when(productReviewRepository.findByReviewIdAndClientId(id, TEST_CLIENT_ID)).thenReturn(null);
                    NotFoundException ex = assertThrows(NotFoundException.class,
                            () -> productReviewService.setProductReviewScore(id, true));
                    assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
                }));
            }

            return tests.stream();
        }
    }

    private ProductReviewRequestModel buildValidProductReviewRequest() {
        ProductReviewRequestModel request = new ProductReviewRequestModel();
        request.setReviewId(TEST_REVIEW_ID);
        request.setRatings(TEST_RATING);
        request.setReview(TEST_REVIEW_TEXT);
        request.setUserId(TEST_USER_ID);
        request.setProductId(TEST_PRODUCT_ID);
        request.setParentId(null);
        return request;
    }

}
