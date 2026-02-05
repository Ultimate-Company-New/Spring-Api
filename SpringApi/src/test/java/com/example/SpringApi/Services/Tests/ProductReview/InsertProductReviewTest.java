package com.example.SpringApi.Services.Tests.ProductReview;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Models.DatabaseModels.ProductReview;
import com.example.SpringApi.Models.RequestModels.ProductReviewRequestModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductReviewService.insertProductReview method.
 * 
 * Test count: 38 tests
 * - SUCCESS: 20 tests (3 tests + 17 dynamic tests)
 * - FAILURE / EXCEPTION: 18 tests (13 tests + 5 dynamic tests)
 */
@DisplayName("ProductReviewService - InsertProductReview Tests")
public class InsertProductReviewTest extends ProductReviewServiceTestBase {

    // ========================================
    // SUCCESS Tests
    // ========================================

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

    @Test
    @DisplayName("Insert Product Review - Rating Exactly 1 - Success")
    void insertProductReview_RatingOne_Success() {
        testProductReviewRequest.setRatings(BigDecimal.ONE);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
        assertDoesNotThrow(() -> productReviewService.insertProductReview(testProductReviewRequest));
    }

    @Test
    @DisplayName("Insert Product Review - Rating Exactly 5 - Success")
    void insertProductReview_RatingFive_Success() {
        testProductReviewRequest.setRatings(new BigDecimal("5.0"));
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
        assertDoesNotThrow(() -> productReviewService.insertProductReview(testProductReviewRequest));
    }

    @Test
    @DisplayName("Insert Product Review - Rating Zero - Success")
    void insertProductReview_RatingZero_Success() {
        testProductReviewRequest.setRatings(BigDecimal.ZERO);
        assertDoesNotThrow(() -> productReviewService.insertProductReview(testProductReviewRequest));
    }

    // ========================================
    // FAILURE / EXCEPTION Tests
    // ========================================

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

    @Test
    @DisplayName("Insert Product Review - Negative Product ID - Throws BadRequestException")
    void insertProductReview_NegativeProductId_ThrowsBadRequestException() {
        testProductReviewRequest.setProductId(-1L);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(testProductReviewRequest));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER004, ex.getMessage());
    }

    @Test
    @DisplayName("Insert Product Review - Negative User ID - Throws BadRequestException")
    void insertProductReview_NegativeUserId_ThrowsBadRequestException() {
        testProductReviewRequest.setUserId(-1L);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(testProductReviewRequest));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER003, ex.getMessage());
    }

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

    @Test
    @DisplayName("Insert Product Review - Whitespace Review Text - Throws BadRequestException")
    void insertProductReview_WhitespaceReviewText_ThrowsBadRequestException() {
        testProductReviewRequest.setReview("   ");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> productReviewService.insertProductReview(testProductReviewRequest));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.ER002, ex.getMessage());
    }

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
     * Purpose: Add dynamic coverage for additional insert validations and success paths.
     * Expected Result: Invalid inputs throw BadRequestException; valid inputs succeed.
     * Assertions: Exceptions match expected messages; valid cases do not throw.
     */
    @TestFactory
    @DisplayName("Insert Product Review - Additional validation cases")
    Stream<DynamicTest> insertProductReview_AdditionalValidationCases() {
        List<DynamicTest> tests = new ArrayList<>();

        tests.add(DynamicTest.dynamicTest("Null request - InvalidId", () -> {
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> productReviewService.insertProductReview(null));
            assertEquals(ErrorMessages.ProductReviewErrorMessages.InvalidId, ex.getMessage());
        }));

        tests.add(DynamicTest.dynamicTest("ParentId set - Success", () -> {
            ProductReviewRequestModel request = buildValidProductReviewRequest();
            request.setParentId(555L);
            when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
            assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
            verify(productReviewRepository, atLeastOnce()).save(any(ProductReview.class));
        }));

        tests.add(DynamicTest.dynamicTest("ProductId 1 - Success", () -> {
            ProductReviewRequestModel request = buildValidProductReviewRequest();
            request.setProductId(1L);
            when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
            assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
        }));

        tests.add(DynamicTest.dynamicTest("ProductId large - Success", () -> {
            ProductReviewRequestModel request = buildValidProductReviewRequest();
            request.setProductId(Long.MAX_VALUE - 2);
            when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
            assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
        }));

        tests.add(DynamicTest.dynamicTest("ProductId negative - ER004", () -> {
            ProductReviewRequestModel request = buildValidProductReviewRequest();
            request.setProductId(-99L);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> productReviewService.insertProductReview(request));
            assertEquals(ErrorMessages.ProductReviewErrorMessages.ER004, ex.getMessage());
        }));

        tests.add(DynamicTest.dynamicTest("Ratings above max - ER001", () -> {
            ProductReviewRequestModel request = buildValidProductReviewRequest();
            request.setRatings(new BigDecimal("5.01"));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> productReviewService.insertProductReview(request));
            assertEquals(ErrorMessages.ProductReviewErrorMessages.ER001, ex.getMessage());
        }));

        tests.add(DynamicTest.dynamicTest("Ratings at max boundary (5.0) - Success", () -> {
            ProductReviewRequestModel request = buildValidProductReviewRequest();
            request.setRatings(new BigDecimal("5.0"));
            when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
            assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
        }));

        tests.add(DynamicTest.dynamicTest("Ratings at min boundary (0.0) - Success", () -> {
            ProductReviewRequestModel request = buildValidProductReviewRequest();
            request.setRatings(new BigDecimal("0.0"));
            when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
            assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
        }));

        tests.add(DynamicTest.dynamicTest("Ratings exactly 5 - Success (duplicate)", () -> {
            ProductReviewRequestModel request = buildValidProductReviewRequest();
            request.setRatings(new BigDecimal("5.0"));
            when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
            assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
        }));

        tests.add(DynamicTest.dynamicTest("Ratings negative - ER001", () -> {
            ProductReviewRequestModel request = buildValidProductReviewRequest();
            request.setRatings(new BigDecimal("-0.1"));
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> productReviewService.insertProductReview(request));
            assertEquals(ErrorMessages.ProductReviewErrorMessages.ER001, ex.getMessage());
        }));

        tests.add(DynamicTest.dynamicTest("Ratings null - ER001", () -> {
            ProductReviewRequestModel request = buildValidProductReviewRequest();
            request.setRatings(null);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> productReviewService.insertProductReview(request));
            assertEquals(ErrorMessages.ProductReviewErrorMessages.ER001, ex.getMessage());
        }));

        tests.add(DynamicTest.dynamicTest("Ratings valid decimal - Success", () -> {
            ProductReviewRequestModel request = buildValidProductReviewRequest();
            request.setRatings(new BigDecimal("4.25"));
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

        tests.add(DynamicTest.dynamicTest("Review text whitespace - ER002", () -> {
            ProductReviewRequestModel request = buildValidProductReviewRequest();
            request.setReview("\n\t ");
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> productReviewService.insertProductReview(request));
            assertEquals(ErrorMessages.ProductReviewErrorMessages.ER002, ex.getMessage());
        }));

        tests.add(DynamicTest.dynamicTest("Review text with trailing spaces - Success", () -> {
            ProductReviewRequestModel request = buildValidProductReviewRequest();
            request.setReview("Good product   ");
            when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
            assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
        }));

        tests.add(DynamicTest.dynamicTest("UserId 1 - Success", () -> {
            ProductReviewRequestModel request = buildValidProductReviewRequest();
            request.setUserId(1L);
            when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
            assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
        }));

        tests.add(DynamicTest.dynamicTest("UserId large - Success", () -> {
            ProductReviewRequestModel request = buildValidProductReviewRequest();
            request.setUserId(Long.MAX_VALUE - 1);
            when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
            assertDoesNotThrow(() -> productReviewService.insertProductReview(request));
        }));

        tests.add(DynamicTest.dynamicTest("UserId negative - ER003", () -> {
            ProductReviewRequestModel request = buildValidProductReviewRequest();
            request.setUserId(-99L);
            BadRequestException ex = assertThrows(BadRequestException.class,
                    () -> productReviewService.insertProductReview(request));
            assertEquals(ErrorMessages.ProductReviewErrorMessages.ER003, ex.getMessage());
        }));

        return tests.stream();
    }
}
