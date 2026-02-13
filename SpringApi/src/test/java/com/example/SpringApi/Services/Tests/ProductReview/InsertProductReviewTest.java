package com.example.SpringApi.Services.Tests.ProductReview;

import com.example.SpringApi.Controllers.ProductReviewController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.ProductReview;
import com.example.SpringApi.Models.RequestModels.ProductReviewRequestModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    void insertProductReview_s01_ParentIdSet_Success() {
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
    void insertProductReview_s02_RatingFive_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(new BigDecimal("5.0"));
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.insertProductReview(request);

        // Assert
        verify(productReviewRepository, times(1)).save(any(ProductReview.class));
        ArgumentCaptor<ProductReview> reviewCaptor = ArgumentCaptor.forClass(ProductReview.class);
        verify(productReviewRepository).save(reviewCaptor.capture());
        assertEquals(0, reviewCaptor.getValue().getRatings().compareTo(new BigDecimal("5.0")));
    }


    /*
     * Purpose: Verify rating one is accepted.
     * Expected Result: Review saved.
     * Assertions: save called.
     */
    @Test
    @DisplayName("insertProductReview - Rating One - Success")
    void insertProductReview_s03_RatingOne_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(BigDecimal.ONE);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.insertProductReview(request);

        // Assert
        verify(productReviewRepository, times(1)).save(any(ProductReview.class));
        ArgumentCaptor<ProductReview> reviewCaptor = ArgumentCaptor.forClass(ProductReview.class);
        verify(productReviewRepository).save(reviewCaptor.capture());
        assertEquals(0, reviewCaptor.getValue().getRatings().compareTo(BigDecimal.ONE));
    }


    /*
     * Purpose: Verify rating zero is accepted.
     * Expected Result: Review saved.
     * Assertions: save called.
     */
    @Test
    @DisplayName("insertProductReview - Rating Zero - Success")
    void insertProductReview_s04_RatingZero_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(BigDecimal.ZERO);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.insertProductReview(request);

        // Assert
        verify(productReviewRepository, times(1)).save(any(ProductReview.class));
        ArgumentCaptor<ProductReview> reviewCaptor = ArgumentCaptor.forClass(ProductReview.class);
        verify(productReviewRepository).save(reviewCaptor.capture());
        assertEquals(0, reviewCaptor.getValue().getRatings().compareTo(BigDecimal.ZERO));
    }


    /*
     * Purpose: Verify ratings at max boundary accepted.
     * Expected Result: Review saved.
     * Assertions: save called.
     */
    @Test
    @DisplayName("insertProductReview - Ratings At Max Boundary - Success")
    void insertProductReview_s05_RatingsAtMaxBoundary_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(new BigDecimal("5.0"));
        assertEquals(0, request.getRatings().compareTo(new BigDecimal("5.0")));
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.insertProductReview(request);

        // Assert
        verify(productReviewRepository, times(1)).save(any(ProductReview.class));
        ArgumentCaptor<ProductReview> reviewCaptor = ArgumentCaptor.forClass(ProductReview.class);
        verify(productReviewRepository).save(reviewCaptor.capture());
        assertEquals(0, reviewCaptor.getValue().getRatings().compareTo(new BigDecimal("5.0")));
        assertTrue(reviewCaptor.getValue().getRatings().scale() >= 0);
    }


    /*
     * Purpose: Verify ratings at min boundary accepted.
     * Expected Result: Review saved.
     * Assertions: save called.
     */
    @Test
    @DisplayName("insertProductReview - Ratings At Min Boundary - Success")
    void insertProductReview_s06_RatingsAtMinBoundary_Success() {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        request.setRatings(BigDecimal.ZERO);
        assertEquals(0, request.getRatings().compareTo(BigDecimal.ZERO));
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.insertProductReview(request);

        // Assert
        verify(productReviewRepository, times(1)).save(any(ProductReview.class));
        ArgumentCaptor<ProductReview> reviewCaptor = ArgumentCaptor.forClass(ProductReview.class);
        verify(productReviewRepository).save(reviewCaptor.capture());
        assertEquals(0, reviewCaptor.getValue().getRatings().compareTo(BigDecimal.ZERO));
        assertFalse(reviewCaptor.getValue().getRatings().signum() > 0);
    }


    /*
     * Purpose: Verify long review text is accepted.
     * Expected Result: Review saved.
     * Assertions: save called.
     */
    @Test
    @DisplayName("insertProductReview - Review Text Long - Success")
    void insertProductReview_s07_ReviewTextLong_Success() {
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
    void insertProductReview_s08_ReviewTextUnicode_Success() {
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
    void insertProductReview_s09_ReviewTextWithNewlines_Success() {
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
    void insertProductReview_s10_Success_Success() {
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
    @ParameterizedTest(name = "insertProductReview - {0} - Throws BadRequestException")
    @MethodSource("invalidReviewTextScenarios")
    void insertProductReview_f01_InvalidReviewText_ThrowsBadRequestException(
            String scenario,
            Consumer<ProductReviewRequestModel> requestMutator) {
        // Arrange
        ProductReviewRequestModel request = buildValidProductReviewRequest();
        requestMutator.accept(request);

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER002, ex.getMessage(), "Scenario: " + scenario);
    }

    private static Stream<Arguments> invalidReviewTextScenarios() {
        return Stream.of(
                Arguments.of("Empty Review Text", (Consumer<ProductReviewRequestModel>) req -> req.setReview("")),
                Arguments.of("Null Review Text", (Consumer<ProductReviewRequestModel>) req -> req.setReview(null)),
                Arguments.of("Whitespace Review Text", (Consumer<ProductReviewRequestModel>) req -> req.setReview("   ")));
    }


    /*
     * Purpose: Verify invalid ratings throw BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message matches.
     */
    @Test
    @DisplayName("insertProductReview - Invalid Ratings - Throws BadRequestException")
    void insertProductReview_f02_InvalidRatings_ThrowsBadRequestException() {
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
    void insertProductReview_f03_NegativeProductId_ThrowsBadRequestException() {
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
    void insertProductReview_f04_NegativeUserId_ThrowsBadRequestException() {
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
    void insertProductReview_f05_NullProductId_ThrowsBadRequestException() {
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
    void insertProductReview_f06_NullRequest_ThrowsBadRequestException() {
        // Arrange
        ProductReviewRequestModel request = null;

        // Act
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(request));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.INVALID_ID, ex.getMessage());
    }


    /*
     * Purpose: Verify null userId throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message matches.
     */
    @Test
    @DisplayName("insertProductReview - Null UserId - Throws BadRequestException")
    void insertProductReview_f08_NullUserId_ThrowsBadRequestException() {
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
    void insertProductReview_f09_RatingsTooHigh_ThrowsBadRequestException() {
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
     * Purpose: Verify zero productId throws BadRequestException.
     * Expected Result: BadRequestException.
     * Assertions: message matches.
     */
    @Test
    @DisplayName("insertProductReview - Zero ProductId - Throws BadRequestException")
    void insertProductReview_f11_ZeroProductId_ThrowsBadRequestException() {
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
    void insertProductReview_f12_ZeroUserId_ThrowsBadRequestException() {
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
