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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductReviewService.
 *
 * This test class provides comprehensive coverage of ProductReviewService methods including:
 * - Product review insertion with validation
 * - Paginated review retrieval by product ID
 * - Review toggle (soft delete/restore) operations
 * - Review score management (helpful/not helpful)
 * - Hierarchical review deletion
 * - Error handling and validation
 *
 * Each test method follows the AAA (Arrange-Act-Assert) pattern and includes
 * both success and failure scenarios to ensure robust error handling.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductReviewService Unit Tests")
class ProductReviewServiceTest {

    @Mock
    private ProductReviewRepository productReviewRepository;

    @Mock
    private UserLogService userLogService;

    @Mock
    private HttpServletRequest request;

    @Spy
    @InjectMocks
    private ProductReviewService productReviewService;

    private ProductReview testProductReview;
    private ProductReviewRequestModel testProductReviewRequest;
    private PaginationBaseRequestModel testPaginationRequest;

    private static final Long TEST_REVIEW_ID = 1L;
    private static final Long TEST_PRODUCT_ID = 100L;
    private static final Long TEST_USER_ID = 200L;
    private static final Long TEST_CLIENT_ID = 1L;
    private static final String TEST_USER = "testuser";
    private static final BigDecimal TEST_RATING = new BigDecimal("4.5");
    private static final String TEST_REVIEW_TEXT = "Great product!";

    /**
     * Sets up test data before each test execution.
     * Initializes common test objects and configures mock behaviors.
     */
    @BeforeEach
    void setUp() {
        // Initialize test product review request model
        testProductReviewRequest = new ProductReviewRequestModel();
        testProductReviewRequest.setReviewId(TEST_REVIEW_ID);
        testProductReviewRequest.setRatings(TEST_RATING);
        testProductReviewRequest.setReview(TEST_REVIEW_TEXT);
        testProductReviewRequest.setUserId(TEST_USER_ID);
        testProductReviewRequest.setProductId(TEST_PRODUCT_ID);
        testProductReviewRequest.setParentId(null);

        // Create test product review using constructor
        testProductReview = new ProductReview(testProductReviewRequest, TEST_USER);
        testProductReview.setReviewId(TEST_REVIEW_ID);
        testProductReview.setScore(5);

        // Initialize test pagination request
        testPaginationRequest = new PaginationBaseRequestModel();
        testPaginationRequest.setStart(0);
        testPaginationRequest.setEnd(10);

        // Mock Authorization header for JWT authentication
        lenient().when(request.getHeader("Authorization")).thenReturn("Bearer test-token");

        // Mock getUserId(), getUser(), and getClientId() methods
        lenient().when(productReviewService.getUserId()).thenReturn(TEST_USER_ID);
        lenient().when(productReviewService.getUser()).thenReturn(TEST_USER);
        lenient().when(productReviewService.getClientId()).thenReturn(TEST_CLIENT_ID);
    }

    // ==================== Insert Product Review Tests ====================

    /**
     * Test successful product review insertion.
     * Verifies that a valid review is created and saved correctly.
     */
    @Test
    @DisplayName("Insert Product Review - Success - Should create and save review")
    void insertProductReview_Success() {
        // Arrange
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act
        productReviewService.insertProductReview(testProductReviewRequest);

        // Assert
        verify(productReviewRepository, times(1)).save(any(ProductReview.class));
        verify(userLogService, times(1)).logData(
            eq(TEST_USER_ID.longValue()),
            eq("Successfully inserted product review. null"),
            eq("insertProductReview")
        );
    }

    /**
     * Test insert product review with null request.
     * Verifies that BadRequestException is thrown for null request.
     */
    @Test
    @DisplayName("Insert Product Review - Failure - Null request throws BadRequestException")
    void insertProductReview_NullRequest_ThrowsBadRequestException() {
        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productReviewService.insertProductReview(null)
        );

        assertEquals(ErrorMessages.ProductReviewErrorMessages.InvalidId, exception.getMessage());
        verify(productReviewRepository, never()).save(any(ProductReview.class));
        verify(userLogService, never()).logData(anyLong(), any(), any());
    }

    /**
     * Test insert product review with invalid ratings (negative).
     * Verifies that BadRequestException is thrown for invalid ratings.
     */
    @Test
    @DisplayName("Insert Product Review - Failure - Invalid ratings throws BadRequestException")
    void insertProductReview_InvalidRatings_ThrowsBadRequestException() {
        // Arrange
        testProductReviewRequest.setRatings(new BigDecimal("-1.0"));

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productReviewService.insertProductReview(testProductReviewRequest)
        );

        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER001, exception.getMessage());
        verify(productReviewRepository, never()).save(any(ProductReview.class));
        verify(userLogService, never()).logData(anyLong(), any(), any());
    }

    /**
     * Test insert product review with ratings too high (>5.0).
     * Verifies that BadRequestException is thrown for ratings above 5.0.
     */
    @Test
    @DisplayName("Insert Product Review - Failure - Ratings too high throws BadRequestException")
    void insertProductReview_RatingsTooHigh_ThrowsBadRequestException() {
        // Arrange
        testProductReviewRequest.setRatings(new BigDecimal("6.0"));

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productReviewService.insertProductReview(testProductReviewRequest)
        );

        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER001, exception.getMessage());
        verify(productReviewRepository, never()).save(any(ProductReview.class));
        verify(userLogService, never()).logData(anyLong(), any(), any());
    }

    /**
     * Test insert product review with null review text.
     * Verifies that BadRequestException is thrown for null review text.
     */
    @Test
    @DisplayName("Insert Product Review - Failure - Null review text throws BadRequestException")
    void insertProductReview_NullReviewText_ThrowsBadRequestException() {
        // Arrange
        testProductReviewRequest.setReview(null);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productReviewService.insertProductReview(testProductReviewRequest)
        );

        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER002, exception.getMessage());
        verify(productReviewRepository, never()).save(any(ProductReview.class));
        verify(userLogService, never()).logData(anyLong(), any(), any());
    }

    /**
     * Test insert product review with empty review text.
     * Verifies that BadRequestException is thrown for empty review text.
     */
    @Test
    @DisplayName("Insert Product Review - Failure - Empty review text throws BadRequestException")
    void insertProductReview_EmptyReviewText_ThrowsBadRequestException() {
        // Arrange
        testProductReviewRequest.setReview("");

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productReviewService.insertProductReview(testProductReviewRequest)
        );

        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER002, exception.getMessage());
        verify(productReviewRepository, never()).save(any(ProductReview.class));
        verify(userLogService, never()).logData(anyLong(), any(), any());
    }

    /**
     * Test insert product review with invalid user ID (null).
     * Verifies that BadRequestException is thrown for null user ID.
     */
    @Test
    @DisplayName("Insert Product Review - Failure - Null user ID throws BadRequestException")
    void insertProductReview_NullUserId_ThrowsBadRequestException() {
        // Arrange
        testProductReviewRequest.setUserId(null);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productReviewService.insertProductReview(testProductReviewRequest)
        );

        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER003, exception.getMessage());
        verify(productReviewRepository, never()).save(any(ProductReview.class));
        verify(userLogService, never()).logData(anyLong(), any(), any());
    }

    /**
     * Test insert product review with invalid user ID (zero).
     * Verifies that BadRequestException is thrown for zero user ID.
     */
    @Test
    @DisplayName("Insert Product Review - Failure - Zero user ID throws BadRequestException")
    void insertProductReview_ZeroUserId_ThrowsBadRequestException() {
        // Arrange
        testProductReviewRequest.setUserId(0L);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productReviewService.insertProductReview(testProductReviewRequest)
        );

        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER003, exception.getMessage());
        verify(productReviewRepository, never()).save(any(ProductReview.class));
        verify(userLogService, never()).logData(anyLong(), any(), any());
    }

    /**
     * Test insert product review with invalid product ID (null).
     * Verifies that BadRequestException is thrown for null product ID.
     */
    @Test
    @DisplayName("Insert Product Review - Failure - Null product ID throws BadRequestException")
    void insertProductReview_NullProductId_ThrowsBadRequestException() {
        // Arrange
        testProductReviewRequest.setProductId(null);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productReviewService.insertProductReview(testProductReviewRequest)
        );

        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER004, exception.getMessage());
        verify(productReviewRepository, never()).save(any(ProductReview.class));
        verify(userLogService, never()).logData(anyLong(), any(), any());
    }

    /**
     * Test insert product review with invalid product ID (zero).
     * Verifies that BadRequestException is thrown for zero product ID.
     */
    @Test
    @DisplayName("Insert Product Review - Failure - Zero product ID throws BadRequestException")
    void insertProductReview_ZeroProductId_ThrowsBadRequestException() {
        // Arrange
        testProductReviewRequest.setProductId(0L);

        // Act & Assert
        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> productReviewService.insertProductReview(testProductReviewRequest)
        );

        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER004, exception.getMessage());
        verify(productReviewRepository, never()).save(any(ProductReview.class));
        verify(userLogService, never()).logData(anyLong(), any(), any());
    }

    // ==================== Get Product Reviews In Batches Tests ====================

    /**
     * Test successful retrieval of product reviews in batches.
     * Verifies that paginated reviews are returned correctly.
     */
    @Test
    @DisplayName("Get Product Reviews In Batches - Success - Should return paginated reviews")
    void getProductReviewsInBatchesGivenProductId_Success() {
        // Arrange
        List<ProductReview> reviewList = Arrays.asList(testProductReview);
        Page<ProductReview> reviewPage = new PageImpl<>(reviewList, PageRequest.of(0, 10), 1);

        when(productReviewRepository.findPaginatedProductReviews(
            eq(TEST_CLIENT_ID), isNull(), isNull(), isNull(), eq(false), any(Pageable.class)))
            .thenReturn(reviewPage);

        // Act
        PaginationBaseResponseModel<ProductReviewResponseModel> result =
            productReviewService.getProductReviewsInBatchesGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getData());
        assertEquals(1, result.getData().size());
        assertEquals(1L, result.getTotalDataCount());
        assertEquals(TEST_REVIEW_ID, result.getData().get(0).getReviewId());
        assertEquals(TEST_RATING, result.getData().get(0).getRatings());

        verify(productReviewRepository, times(1)).findPaginatedProductReviews(
            eq(TEST_CLIENT_ID), isNull(), isNull(), isNull(), eq(false), any(Pageable.class));
    }

    /**
     * Test get product reviews with empty results.
     * Verifies that empty list is returned when no reviews exist.
     */
    @Test
    @DisplayName("Get Product Reviews In Batches - Success - Empty results")
    void getProductReviewsInBatchesGivenProductId_EmptyResults() {
        // Arrange
        Page<ProductReview> emptyPage = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);

        when(productReviewRepository.findPaginatedProductReviews(
            eq(TEST_CLIENT_ID), isNull(), isNull(), isNull(), eq(false), any(Pageable.class)))
            .thenReturn(emptyPage);

        // Act
        PaginationBaseResponseModel<ProductReviewResponseModel> result =
            productReviewService.getProductReviewsInBatchesGivenProductId(testPaginationRequest, TEST_PRODUCT_ID);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getData());
        assertEquals(0, result.getData().size());
        assertEquals(0L, result.getTotalDataCount());

        verify(productReviewRepository, times(1)).findPaginatedProductReviews(
            eq(TEST_CLIENT_ID), isNull(), isNull(), isNull(), eq(false), any(Pageable.class));
    }

    // ==================== Toggle Product Review Tests ====================

    /**
     * Test successful product review toggle (mark as deleted).
     * Verifies that review is marked as deleted and descendants are also deleted.
     */
    @Test
    @DisplayName("Toggle Product Review - Success - Mark as deleted")
    void toggleProductReview_Success_MarkAsDeleted() {
        // Arrange
        testProductReview.setIsDeleted(false);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID)).thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
        when(productReviewRepository.markAllDescendantsAsDeleted(eq(TEST_REVIEW_ID), anyString())).thenReturn(2);

        // Act
        productReviewService.toggleProductReview(TEST_REVIEW_ID);

        // Assert
        assertTrue(testProductReview.getIsDeleted());
        verify(productReviewRepository, times(1)).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
        verify(productReviewRepository, times(1)).save(testProductReview);
        verify(productReviewRepository, times(1)).markAllDescendantsAsDeleted(eq(TEST_REVIEW_ID), anyString());
        verify(userLogService, times(1)).logData(
            eq(TEST_USER_ID.longValue()),
            eq("Successfully toggled product review. 1"),
            eq("toggleProductReview")
        );
    }

    /**
     * Test successful product review toggle (restore from deleted).
     * Verifies that review is restored and no descendants are affected.
     */
    @Test
    @DisplayName("Toggle Product Review - Success - Restore from deleted")
    void toggleProductReview_Success_RestoreFromDeleted() {
        // Arrange
        testProductReview.setIsDeleted(true);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID)).thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act
        productReviewService.toggleProductReview(TEST_REVIEW_ID);

        // Assert
        assertFalse(testProductReview.getIsDeleted());
        verify(productReviewRepository, times(1)).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
        verify(productReviewRepository, times(1)).save(testProductReview);
        verify(productReviewRepository, never()).markAllDescendantsAsDeleted(any(), any());
        verify(userLogService, times(1)).logData(
            eq(TEST_USER_ID.longValue()),
            eq("Successfully toggled product review. 1"),
            eq("toggleProductReview")
        );
    }

    /**
     * Test toggle product review with non-existent review ID.
     * Verifies that NotFoundException is thrown when review is not found.
     */
    @Test
    @DisplayName("Toggle Product Review - Failure - Review not found")
    void toggleProductReview_ReviewNotFound_ThrowsNotFoundException() {
        // Arrange
        lenient().when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> productReviewService.toggleProductReview(TEST_REVIEW_ID)
        );

        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, exception.getMessage());
        verify(productReviewRepository, times(1)).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
        verify(productReviewRepository, never()).save(any(ProductReview.class));
        verify(productReviewRepository, never()).markAllDescendantsAsDeleted(any(), any());
        verify(userLogService, never()).logData(anyLong(), any(), any());
    }

    // ==================== Set Product Review Score Tests ====================

    /**
     * Test successful score increase.
     * Verifies that review score is incremented correctly.
     */
    @Test
    @DisplayName("Set Product Review Score - Success - Increase score")
    void setProductReviewScore_Success_IncreaseScore() {
        // Arrange
        testProductReview.setScore(3);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID)).thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

        // Assert
        assertEquals(4, testProductReview.getScore());
        verify(productReviewRepository, times(1)).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
        verify(productReviewRepository, times(1)).save(testProductReview);
        verify(userLogService, times(1)).logData(
            eq(TEST_USER_ID.longValue()),
            eq("Successfully updated the review Score. 1"),
            eq("setProductReviewScore")
        );
    }

    /**
     * Test successful score decrease.
     * Verifies that review score is decremented correctly but not below zero.
     */
    @Test
    @DisplayName("Set Product Review Score - Success - Decrease score")
    void setProductReviewScore_Success_DecreaseScore() {
        // Arrange
        testProductReview.setScore(3);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID)).thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

        // Assert
        assertEquals(2, testProductReview.getScore());
        verify(productReviewRepository, times(1)).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
        verify(productReviewRepository, times(1)).save(testProductReview);
        verify(userLogService, times(1)).logData(
            eq(TEST_USER_ID.longValue()),
            eq("Successfully updated the review Score. 1"),
            eq("setProductReviewScore")
        );
    }

    /**
     * Test score decrease that would go below zero.
     * Verifies that score is set to zero and not negative.
     */
    @Test
    @DisplayName("Set Product Review Score - Success - Decrease score to zero minimum")
    void setProductReviewScore_Success_DecreaseToZero() {
        // Arrange
        testProductReview.setScore(0);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID)).thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

        // Assert
        assertEquals(0, testProductReview.getScore());
        verify(productReviewRepository, times(1)).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
        verify(productReviewRepository, times(1)).save(testProductReview);
        verify(userLogService, times(1)).logData(
            eq(TEST_USER_ID.longValue()),
            eq("Successfully updated the review Score. 1"),
            eq("setProductReviewScore")
        );
    }

    /**
     * Test set product review score with null current score.
     * Verifies that null score is treated as zero.
     */
    @Test
    @DisplayName("Set Product Review Score - Success - Null score treated as zero")
    void setProductReviewScore_Success_NullScore() {
        // Arrange
        testProductReview.setScore(null);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID)).thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

        // Assert
        assertEquals(1, testProductReview.getScore());
        verify(productReviewRepository, times(1)).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
        verify(productReviewRepository, times(1)).save(testProductReview);
        verify(userLogService, times(1)).logData(
            eq(TEST_USER_ID.longValue()),
            eq("Successfully updated the review Score. 1"),
            eq("setProductReviewScore")
        );
    }

    /**
     * Test set product review score with non-existent review ID.
     * Verifies that NotFoundException is thrown when review is not found.
     */
    @Test
    @DisplayName("Set Product Review Score - Failure - Review not found")
    void setProductReviewScore_ReviewNotFound_ThrowsNotFoundException() {
        // Arrange
        lenient().when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> productReviewService.setProductReviewScore(TEST_REVIEW_ID, true)
        );

        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, exception.getMessage());
        verify(productReviewRepository, times(1)).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
        verify(productReviewRepository, never()).save(any(ProductReview.class));
        verify(userLogService, never()).logData(anyLong(), any(), any());
    }
}