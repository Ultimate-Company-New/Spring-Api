package com.example.SpringApi.Services.Tests.ProductReview;

import com.example.SpringApi.Controllers.ProductReviewController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.ProductReview;
import com.example.SpringApi.Models.RequestModels.ProductReviewRequestModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductReviewService.insertProductReview method.
 */
@DisplayName("ProductReviewService - InsertProductReview Tests")
class InsertProductReviewTest extends ProductReviewServiceTestBase {

    // Total Tests: 86

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify parentId set is accepted.
     * Expected Result: Review saved.
     * Assertions: save called.
     */
    @Test
    @DisplayName("insertProductReview - ParentId Set - Success")
    void insertProductReview_ParentIdSet_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setParentId(10L);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.insertProductReview(request);

        // Assert
        verify(productReviewRepository, times(1)).save(any(ProductReview.class));
    }


    /*
     * Purpose: Verify rating five is accepted.
     * Expected Result: Review saved.
     * Assertions: save called.
     */
    @Test
    @DisplayName("insertProductReview - Rating Five - Success")
    void insertProductReview_RatingFive_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(new BigDecimal("5.0"));
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.insertProductReview(request);

        // Assert
        verify(productReviewRepository, times(1)).save(any(ProductReview.class));
    }


    /*
     * Purpose: Verify rating one is accepted.
     * Expected Result: Review saved.
     * Assertions: save called.
     */
    @Test
    @DisplayName("insertProductReview - Rating One - Success")
    void insertProductReview_RatingOne_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(BigDecimal.ONE);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.insertProductReview(request);

        // Assert
        verify(productReviewRepository, times(1)).save(any(ProductReview.class));
    }


    /*
     * Purpose: Verify rating zero is accepted.
     * Expected Result: Review saved.
     * Assertions: save called.
     */
    @Test
    @DisplayName("insertProductReview - Rating Zero - Success")
    void insertProductReview_RatingZero_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(BigDecimal.ZERO);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.insertProductReview(request);

        // Assert
        verify(productReviewRepository, times(1)).save(any(ProductReview.class));
    }


    /*
     * Purpose: Verify ratings at max boundary accepted.
     * Expected Result: Review saved.
     * Assertions: save called.
     */
    @Test
    @DisplayName("insertProductReview - Ratings At Max Boundary - Success")
    void insertProductReview_RatingsAtMaxBoundary_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(new BigDecimal("5.0"));
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.insertProductReview(request);

        // Assert
        verify(productReviewRepository, times(1)).save(any(ProductReview.class));
    }


    /*
     * Purpose: Verify ratings at min boundary accepted.
     * Expected Result: Review saved.
     * Assertions: save called.
     */
    @Test
    @DisplayName("insertProductReview - Ratings At Min Boundary - Success")
    void insertProductReview_RatingsAtMinBoundary_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(BigDecimal.ZERO);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.insertProductReview(request);

        // Assert
        verify(productReviewRepository, times(1)).save(any(ProductReview.class));
    }


    /*
     * Purpose: Verify long review text is accepted.
     * Expected Result: Review saved.
     * Assertions: save called.
     */
    @Test
    @DisplayName("insertProductReview - Review Text Long - Success")
    void insertProductReview_ReviewTextLong_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setReview("A".repeat(500));
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.insertProductReview(request);

        // Assert
        verify(productReviewRepository, times(1)).save(any(ProductReview.class));
    }


    /*
     * Purpose: Verify review text with unicode is accepted.
     * Expected Result: Review saved.
     * Assertions: save called.
     */
    @Test
    @DisplayName("insertProductReview - Review Text Unicode - Success")
    void insertProductReview_ReviewTextUnicode_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setReview("测试 Unicode ✅");
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.insertProductReview(request);

        // Assert
        verify(productReviewRepository, times(1)).save(any(ProductReview.class));
    }


    /*
     * Purpose: Verify review text with newlines accepted.
     * Expected Result: Review saved.
     * Assertions: save called.
     */
    @Test
    @DisplayName("insertProductReview - Review Text With Newlines - Success")
    void insertProductReview_ReviewTextWithNewlines_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setReview("Line1\nLine2");
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.insertProductReview(request);

        // Assert
        verify(productReviewRepository, times(1)).save(any(ProductReview.class));
    }


    /*
     * Purpose: Verify basic success path.
     * Expected Result: Review saved.
     * Assertions: save called.
     */
    @Test
    @DisplayName("insertProductReview - Success - Success")
    void insertProductReview_Success_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        stubProductReviewRepositorySave(testProductReview);
        stubUserLogServiceLogDataReturnsTrue();

        // Act
        productReviewService.insertProductReview(request);

        // Assert
        verify(productReviewRepository, times(1)).save(any(ProductReview.class));
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify empty review text throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message matches.
     */
    @Test
    @DisplayName("insertProductReview - Empty Review Text - Throws BadRequestException")
    void insertProductReview_EmptyReviewText_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setReview("");

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER002, ex.getMessage());
    }


    /*
     * Purpose: Verify invalid ratings throw BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message matches.
     */
    @Test
    @DisplayName("insertProductReview - Invalid Ratings - Throws BadRequestException")
    void insertProductReview_InvalidRatings_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(new BigDecimal("-1"));

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER001, ex.getMessage());
    }


    /*
     * Purpose: Verify negative productId throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message matches.
     */
    @Test
    @DisplayName("insertProductReview - Negative ProductId - Throws BadRequestException")
    void insertProductReview_NegativeProductId_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setProductId(-1L);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER004, ex.getMessage());
    }


    /*
     * Purpose: Verify negative userId throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message matches.
     */
    @Test
    @DisplayName("insertProductReview - Negative UserId - Throws BadRequestException")
    void insertProductReview_NegativeUserId_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setUserId(-1L);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER003, ex.getMessage());
    }


    /*
     * Purpose: Verify null productId throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message matches.
     */
    @Test
    @DisplayName("insertProductReview - Null ProductId - Throws BadRequestException")
    void insertProductReview_NullProductId_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setProductId(null);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER004, ex.getMessage());
    }


    /*
     * Purpose: Verify null request throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message matches.
     */
    @Test
    @DisplayName("insertProductReview - Null Request - Throws BadRequestException")
    void insertProductReview_NullRequest_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = null;

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.InvalidId, ex.getMessage());
    }


    /*
     * Purpose: Verify null review text throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message matches.
     */
    @Test
    @DisplayName("insertProductReview - Null Review Text - Throws BadRequestException")
    void insertProductReview_NullReviewText_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setReview(null);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER002, ex.getMessage());
    }


    /*
     * Purpose: Verify null userId throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message matches.
     */
    @Test
    @DisplayName("insertProductReview - Null UserId - Throws BadRequestException")
    void insertProductReview_NullUserId_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setUserId(null);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER003, ex.getMessage());
    }


    /*
     * Purpose: Verify ratings too high throw BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message matches.
     */
    @Test
    @DisplayName("insertProductReview - Ratings Too High - Throws BadRequestException")
    void insertProductReview_RatingsTooHigh_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(new BigDecimal("6.0"));

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER001, ex.getMessage());
    }


    /*
     * Purpose: Verify whitespace review text throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message matches.
     */
    @Test
    @DisplayName("insertProductReview - Whitespace Review Text - Throws BadRequestException")
    void insertProductReview_WhitespaceReviewText_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setReview("   ");

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER002, ex.getMessage());
    }


    /*
     * Purpose: Verify zero productId throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message matches.
     */
    @Test
    @DisplayName("insertProductReview - Zero ProductId - Throws BadRequestException")
    void insertProductReview_ZeroProductId_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setProductId(0L);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER004, ex.getMessage());
    }


    /*
     * Purpose: Verify zero userId throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message matches.
     */
    @Test
    @DisplayName("insertProductReview - Zero UserId - Throws BadRequestException")
    void insertProductReview_ZeroUserId_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setUserId(0L);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER003, ex.getMessage());
    }

    /*
     **********************************************************************************************
     * PERMISSION TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify unauthorized access is handled at controller level.
     * Expected Result: Unauthorized status is returned.
     * Assertions: Response status is 401.
     */
    @Test
    @DisplayName("insertProductReview - Controller Permission - Unauthorized")
    void insertProductReview_controller_permission_unauthorized() {
        // Arrange
        ProductReviewController controller = new ProductReviewController(productReviewServiceMock);
        stubProductReviewServiceInsertProductReviewThrowsUnauthorized();

        // Act
        ResponseEntity<?> response = controller.insertProductReview(buildValidProductReviewRequest());

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }


    /*
     * Purpose: Verify @PreAuthorize annotation is present.
     * Expected Result: Annotation exists.
     * Assertions: Annotation is not null.
     */
    @Test
    @DisplayName("insertProductReview - Verify @PreAuthorize Annotation")
    void insertProductReview_verifyPreAuthorizeAnnotation_success() throws NoSuchMethodException {
        // Arrange
        Method method = ProductReviewController.class.getMethod("insertProductReview",
                ProductReviewRequestModel.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "insertProductReview should have @PreAuthorize annotation");
    }
}

/**
 * Test class for ProductReviewService.insertProductReview method.
 * 
 * Test count: 62 tests
 * - SUCCESS: 4 tests
 * - FAILURE / EXCEPTION: 58 tests
 */
@DisplayName("ProductReviewService - InsertProductReview Tests - Duplicate Block")
class InsertProductReviewTestDuplicate extends ProductReviewServiceTestBase {

    // ========================================
    // SUCCESS Tests
    // ========================================

    /*
     * Purpose: Verify insertProductReview_RatingFive_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Rating Exactly 5 - Success")
    void insertProductReview_RatingFive_Success() {
        // Arrange
        testProductReviewRequest.setRatings(new BigDecimal("5.0"));
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(testProductReviewRequest));
    }


    /*
     * Purpose: Verify insertProductReview_RatingOne_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Rating Exactly 1 - Success")
    void insertProductReview_RatingOne_Success() {
        // Arrange
        testProductReviewRequest.setRatings(BigDecimal.ONE);
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(testProductReviewRequest));
    }


    /*
     * Purpose: Verify insertProductReview_RatingZero_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Rating Zero - Success")
    void insertProductReview_RatingZero_Success() {
        // Arrange
        testProductReviewRequest.setRatings(BigDecimal.ZERO);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(testProductReviewRequest));
    }


    /*
     * Purpose: Verify insertProductReview_Success_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Success - Should create and save review")
    void insertProductReview_Success_Success() {
        // Arrange
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.insertProductReview(testProductReviewRequest);

        // Assert
        verify(productReviewRepository, times(1)).save(any(ProductReview.class));
        verify(userLogService, times(1)).logData(
                TEST_USER_ID.longValue(),
                "Successfully inserted product review. null",
                "insertProductReview");
    }

    // ========================================
    // FAILURE / EXCEPTION Tests
    // ========================================

    /*
     * Purpose: Verify insertProductReview_AllFieldsAtBoundaries_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - All Fields At Boundaries - Success")
    void insertProductReview_AllFieldsAtBoundaries_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(new BigDecimal("5.0"));
        request.setReview("A");
        request.setUserId(1L);
        request.setProductId(1L);
        request.setParentId(1L);
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }

    /*
     * Purpose: Verify insertProductReview_EmptyReviewText_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Empty Review Text - Throws BadRequestException")
    void insertProductReview_EmptyReviewText_ThrowsBadRequestException() {
        // Arrange
        testProductReviewRequest.setReview("");

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productReviewService.insertProductReview(testProductReviewRequest));

        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER002, exception.getMessage());
        verify(productReviewRepository, never()).save(any(ProductReview.class));
    }


    /*
     * Purpose: Verify insertProductReview_InvalidRatings_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Invalid Ratings (Negative) - Throws BadRequestException")
    void insertProductReview_InvalidRatings_ThrowsBadRequestException() {
        // Arrange
        testProductReviewRequest.setRatings(new BigDecimal("-1.0"));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productReviewService.insertProductReview(testProductReviewRequest));

        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER001, exception.getMessage());
        verify(productReviewRepository, never()).save(any(ProductReview.class));
    }


    /*
     * Purpose: Verify insertProductReview_NegativeProductId_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Negative Product ID - Throws BadRequestException")
    void insertProductReview_NegativeProductId_ThrowsBadRequestException() {
        // Arrange
        testProductReviewRequest.setProductId(-1L);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(testProductReviewRequest));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER004, ex.getMessage());
    }


    /*
     * Purpose: Verify insertProductReview_NegativeUserId_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Negative User ID - Throws BadRequestException")
    void insertProductReview_NegativeUserId_ThrowsBadRequestException() {
        // Arrange
        testProductReviewRequest.setUserId(-1L);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(testProductReviewRequest));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER003, ex.getMessage());
    }


    /*
     * Purpose: Verify insertProductReview_NullProductId_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Null Product ID - Throws BadRequestException")
    void insertProductReview_NullProductId_ThrowsBadRequestException() {
        // Arrange
        testProductReviewRequest.setProductId(null);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productReviewService.insertProductReview(testProductReviewRequest));

        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER004, exception.getMessage());
        verify(productReviewRepository, never()).save(any(ProductReview.class));
    }


    /*
     * Purpose: Verify insertProductReview_NullRequestDuplicate_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Null Request Duplicate - Throws BadRequestException")
    void insertProductReview_NullRequestDuplicate_ThrowsBadRequestException() {
        // Arrange
        // (no setup needed)

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(null));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.InvalidId, ex.getMessage());
    }


    /*
     * Purpose: Verify insertProductReview_NullRequest_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Null Request - Throws BadRequestException")
    void insertProductReview_NullRequest_ThrowsBadRequestException() {
        // Arrange

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productReviewService.insertProductReview(null));

        assertEquals(ErrorMessages.ProductReviewErrorMessages.InvalidId, exception.getMessage());
        verify(productReviewRepository, never()).save(any(ProductReview.class));
        verify(userLogService, never()).logData(anyLong(), any(), any());
    }


    /*
     * Purpose: Verify insertProductReview_NullReviewText_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Null Review Text - Throws BadRequestException")
    void insertProductReview_NullReviewText_ThrowsBadRequestException() {
        // Arrange
        testProductReviewRequest.setReview(null);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productReviewService.insertProductReview(testProductReviewRequest));

        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER002, exception.getMessage());
        verify(productReviewRepository, never()).save(any(ProductReview.class));
    }


    /*
     * Purpose: Verify insertProductReview_NullUserId_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Null User ID - Throws BadRequestException")
    void insertProductReview_NullUserId_ThrowsBadRequestException() {
        // Arrange
        testProductReviewRequest.setUserId(null);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productReviewService.insertProductReview(testProductReviewRequest));

        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER003, exception.getMessage());
        verify(productReviewRepository, never()).save(any(ProductReview.class));
    }


    /*
     * Purpose: Verify insertProductReview_ParentIdNull_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Parent ID Null Optional Field - Success")
    void insertProductReview_ParentIdNull_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setParentId(null);
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_ParentIdSet_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Parent ID Set - Success")
    void insertProductReview_ParentIdSet_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setParentId(555L);
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
        verify(productReviewRepository, atLeastOnce()).save(any(ProductReview.class));
    }


    /*
     * Purpose: Verify insertProductReview_ParentIdValid_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Parent ID 100 Valid - Success")
    void insertProductReview_ParentIdValid_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setParentId(100L);
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_ProductIdExactlyOne_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Product ID Exactly 1 - Success")
    void insertProductReview_ProductIdExactlyOne_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setProductId(1L);
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_ProductIdLargeValue_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Product ID 1000000 Large - Success")
    void insertProductReview_ProductIdLargeValue_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setProductId(1000000L);
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_ProductIdLarge_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Product ID Large Value - Success")
    void insertProductReview_ProductIdLarge_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setProductId(Long.MAX_VALUE - 2);
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_ProductIdNegative99_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Product ID Negative 99 - Throws BadRequestException")
    void insertProductReview_ProductIdNegative99_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setProductId(-99L);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER004, ex.getMessage());
    }


    /*
     * Purpose: Verify insertProductReview_ProductIdNegativeOne_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Product ID Negative 1 - Throws BadRequestException")
    void insertProductReview_ProductIdNegativeOne_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setProductId(-1L);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER004, ex.getMessage());
    }


    /*
     * Purpose: Verify insertProductReview_ProductIdNull_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Product ID Null - Throws BadRequestException")
    void insertProductReview_ProductIdNull_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setProductId(null);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER004, ex.getMessage());
    }


    /*
     * Purpose: Verify insertProductReview_ProductIdOne_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Product ID 1 - Success")
    void insertProductReview_ProductIdOne_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setProductId(1L);
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_ProductIdZero_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Product ID Zero - Throws BadRequestException")
    void insertProductReview_ProductIdZero_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setProductId(0L);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER004, ex.getMessage());
    }


    /*
     * Purpose: Verify insertProductReview_RatingsAboveMax_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Ratings Above Maximum 5.01 - Throws BadRequestException")
    void insertProductReview_RatingsAboveMax_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(new BigDecimal("5.01"));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER001, ex.getMessage());
    }


    /*
     * Purpose: Verify insertProductReview_RatingsAtMaxBoundary_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Ratings At Max Boundary 5.0 - Success")
    void insertProductReview_RatingsAtMaxBoundary_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(new BigDecimal("5.0"));
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_RatingsAtMinBoundary_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Ratings At Min Boundary 0.0 - Success")
    void insertProductReview_RatingsAtMinBoundary_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(new BigDecimal("0.0"));
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_RatingsDoubleMax_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Ratings 10.0 Double Max - Throws BadRequestException")
    void insertProductReview_RatingsDoubleMax_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(new BigDecimal("10.0"));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER001, ex.getMessage());
    }


    /*
     * Purpose: Verify insertProductReview_RatingsExactlyFiveDuplicate_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Ratings Exactly 5.0 Duplicate - Success")
    void insertProductReview_RatingsExactlyFiveDuplicate_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(new BigDecimal("5.0"));
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_RatingsJustAboveMax_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Ratings 5.001 Just Above Max - Throws BadRequestException")
    void insertProductReview_RatingsJustAboveMax_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(new BigDecimal("5.001"));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER001, ex.getMessage());
    }


    /*
     * Purpose: Verify insertProductReview_RatingsJustBelowMax_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Ratings 4.99 Just Below Max - Success")
    void insertProductReview_RatingsJustBelowMax_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(new BigDecimal("4.99"));
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_RatingsMidRange_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Ratings 2.5 Mid Range - Success")
    void insertProductReview_RatingsMidRange_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(new BigDecimal("2.5"));
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_RatingsMinimumPositive_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Ratings 0.01 Minimum Positive - Success")
    void insertProductReview_RatingsMinimumPositive_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(new BigDecimal("0.01"));
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_RatingsNegativeOne_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Ratings Negative 1.0 - Throws BadRequestException")
    void insertProductReview_RatingsNegativeOne_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(new BigDecimal("-1.0"));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER001, ex.getMessage());
    }


    /*
     * Purpose: Verify insertProductReview_RatingsNegativePointOne_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Ratings Negative 0.1 - Throws BadRequestException")
    void insertProductReview_RatingsNegativePointOne_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(new BigDecimal("-0.1"));

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER001, ex.getMessage());
    }


    /*
     * Purpose: Verify insertProductReview_RatingsNullDuplicate_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Ratings Null Duplicate - Throws BadRequestException")
    void insertProductReview_RatingsNullDuplicate_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(null);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER001, ex.getMessage());
    }


    /*
     * Purpose: Verify insertProductReview_RatingsRepeatingDecimal_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Ratings 3.33 Repeating Decimal - Success")
    void insertProductReview_RatingsRepeatingDecimal_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(new BigDecimal("3.33"));
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_RatingsTooHigh_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Ratings Too High (> 5.0) - Throws BadRequestException")
    void insertProductReview_RatingsTooHigh_ThrowsBadRequestException() {
        // Arrange
        testProductReviewRequest.setRatings(new BigDecimal("6.0"));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productReviewService.insertProductReview(testProductReviewRequest));

        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER001, exception.getMessage());
        verify(productReviewRepository, never()).save(any(ProductReview.class));
    }


    /*
     * Purpose: Verify insertProductReview_RatingsValidDecimal_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Ratings Valid Decimal 4.25 - Success")
    void insertProductReview_RatingsValidDecimal_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(new BigDecimal("4.25"));
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_ReviewText1000Chars_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Review Text 1000 Characters - Success")
    void insertProductReview_ReviewText1000Chars_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setReview("x".repeat(1000));
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_ReviewText5000Chars_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Review Text 5000 Characters - Success")
    void insertProductReview_ReviewText5000Chars_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setReview("x".repeat(5000));
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_ReviewTextEmptyString_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Review Text Empty String - Throws BadRequestException")
    void insertProductReview_ReviewTextEmptyString_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setReview("");

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER002, ex.getMessage());
    }


    /*
     * Purpose: Verify insertProductReview_ReviewTextLong_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Review Text Long 500 Chars - Success")
    void insertProductReview_ReviewTextLong_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setReview("x".repeat(500));
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_ReviewTextOnlySpaces_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Review Text Only Spaces - Throws BadRequestException")
    void insertProductReview_ReviewTextOnlySpaces_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setReview("     ");

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER002, ex.getMessage());
    }


    /*
     * Purpose: Verify insertProductReview_ReviewTextSingleChar_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Review Text Single Character - Success")
    void insertProductReview_ReviewTextSingleChar_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setReview("A");
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_ReviewTextSpecialChars_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Review Text With Special Characters - Success")
    void insertProductReview_ReviewTextSpecialChars_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setReview("Great product! @#$%^&*()_+-={}[]|:;<>?,./~`");
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_ReviewTextTrailingSpaces_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Review Text With Trailing Spaces - Success")
    void insertProductReview_ReviewTextTrailingSpaces_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setReview("Good product   ");
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_ReviewTextUnicode_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Review Text With Unicode Characters - Success")
    void insertProductReview_ReviewTextUnicode_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setReview("Excellent product! 优秀的产品 🌟🌟🌟🌟🌟");
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_ReviewTextWhitespaceOnlyTabs_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Review Text Whitespace Only Tabs - Throws BadRequestException")
    void insertProductReview_ReviewTextWhitespaceOnlyTabs_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setReview("\n\t ");

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER002, ex.getMessage());
    }


    /*
     * Purpose: Verify insertProductReview_ReviewTextWithNewlines_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Review Text With Newlines - Success")
    void insertProductReview_ReviewTextWithNewlines_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setReview("Great product!\nVery satisfied.\nHighly recommend.");
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_UserIdExactlyOne_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - User ID Exactly 1 - Success")
    void insertProductReview_UserIdExactlyOne_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setUserId(1L);
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_UserIdLargeValue_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - User ID 1000000 Large - Success")
    void insertProductReview_UserIdLargeValue_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setUserId(1000000L);
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_UserIdLarge_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - User ID Large Value - Success")
    void insertProductReview_UserIdLarge_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setUserId(Long.MAX_VALUE - 1);
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_UserIdNegative99_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - User ID Negative 99 - Throws BadRequestException")
    void insertProductReview_UserIdNegative99_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setUserId(-99L);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER003, ex.getMessage());
    }


    /*
     * Purpose: Verify insertProductReview_UserIdNegativeOne_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - User ID Negative 1 - Throws BadRequestException")
    void insertProductReview_UserIdNegativeOne_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setUserId(-1L);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER003, ex.getMessage());
    }


    /*
     * Purpose: Verify insertProductReview_UserIdNull_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - User ID Null - Throws BadRequestException")
    void insertProductReview_UserIdNull_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setUserId(null);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER003, ex.getMessage());
    }


    /*
     * Purpose: Verify insertProductReview_UserIdOne_Success behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - User ID 1 - Success")
    void insertProductReview_UserIdOne_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setUserId(1L);
        stubProductReviewRepositorySave(testProductReview);

        // Act & Assert
        assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
    }


    /*
     * Purpose: Verify insertProductReview_UserIdZero_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - User ID Zero - Throws BadRequestException")
    void insertProductReview_UserIdZero_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setUserId(0L);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER003, ex.getMessage());
    }


    /*
     * Purpose: Verify insertProductReview_WhitespaceReviewText_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Whitespace Review Text - Throws BadRequestException")
    void insertProductReview_WhitespaceReviewText_ThrowsBadRequestException() {
        // Arrange
        testProductReviewRequest.setReview("   ");

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(testProductReviewRequest));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER002, ex.getMessage());
    }


    /*
     * Purpose: Verify insertProductReview_ZeroProductId_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Zero Product ID - Throws BadRequestException")
    void insertProductReview_ZeroProductId_ThrowsBadRequestException() {
        // Arrange
        testProductReviewRequest.setProductId(0L);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productReviewService.insertProductReview(testProductReviewRequest));

        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER004, exception.getMessage());
        verify(productReviewRepository, never()).save(any(ProductReview.class));
    }


    /*
     * Purpose: Verify insertProductReview_ZeroUserId_ThrowsBadRequestException behavior.
     * Expected Result: Method completes as expected.
     * Assertions: Verify expected outcome.
     */
    @Test
    @DisplayName("Insert Product Review - Zero User ID - Throws BadRequestException")
    void insertProductReview_ZeroUserId_ThrowsBadRequestException() {
        // Arrange
        testProductReviewRequest.setUserId(0L);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> productReviewService.insertProductReview(testProductReviewRequest));

        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER003, exception.getMessage());
        verify(productReviewRepository, never()).save(any(ProductReview.class));
    }

}
