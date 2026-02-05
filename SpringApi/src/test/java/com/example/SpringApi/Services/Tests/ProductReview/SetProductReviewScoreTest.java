package com.example.SpringApi.Services.Tests.ProductReview;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.ProductReview;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductReviewService.setProductReviewScore method.
 * 
 * Test count: 18 tests
 * - SUCCESS: 4 tests
 * - FAILURE / EXCEPTION: 14 tests (4 tests + 10 dynamic tests)
 */
@DisplayName("ProductReviewService - SetProductReviewScore Tests")
public class SetProductReviewScoreTest extends ProductReviewServiceTestBase {

    // ========================================
    // SUCCESS Tests
    // ========================================

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

    @Test
    @DisplayName("Set Product Review Score - Null Score Treated as Zero")
    void setProductReviewScore_Success_NullScore() {
        testProductReview.setScore(null);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID)).thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

        assertEquals(1, testProductReview.getScore());
    }

    // ========================================
    // FAILURE / EXCEPTION Tests
    // ========================================

    @Test
    @DisplayName("Set Product Review Score - Max Long ID - Not Found")
    void setProductReviewScore_MaxLongId_ThrowsNotFoundException() {
        when(productReviewRepository.findByReviewIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.setProductReviewScore(Long.MAX_VALUE, true));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Set Product Review Score - Negative ID - Not Found")
    void setProductReviewScore_NegativeId_ThrowsNotFoundException() {
        when(productReviewRepository.findByReviewIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.setProductReviewScore(-1L, true));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

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

    @Test
    @DisplayName("Set Product Review Score - Zero ID - Not Found")
    void setProductReviewScore_ZeroId_ThrowsNotFoundException() {
        when(productReviewRepository.findByReviewIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.setProductReviewScore(0L, true));
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

        tests.add(DynamicTest.dynamicTest("Decrease when score 1 - becomes 0", () -> {
            testProductReview.setScore(1);
            when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                    .thenReturn(testProductReview);
            when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
            productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);
            assertEquals(0, testProductReview.getScore());
        }));

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
