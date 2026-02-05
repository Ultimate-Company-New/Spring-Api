package com.example.SpringApi.Services.Tests.ProductReview;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.ProductReview;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductReviewService.toggleProductReview method.
 * 
 * Test count: 12 tests
 * - SUCCESS: 3 tests
 * - FAILURE / EXCEPTION: 9 tests (3 tests + 6 dynamic tests)
 */
@DisplayName("ProductReviewService - ToggleProductReview Tests")
public class ToggleProductReviewTest extends ProductReviewServiceTestBase {

    // ========================================
    // SUCCESS Tests
    // ========================================

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

    // ========================================
    // FAILURE / EXCEPTION Tests
    // ========================================

    @Test
    @DisplayName("Toggle Product Review - Negative ID - Not Found")
    void toggleProductReview_NegativeId_ThrowsNotFoundException() {
        when(productReviewRepository.findByReviewIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.toggleProductReview(-1L));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

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

    @Test
    @DisplayName("Toggle Product Review - Zero ID - Not Found")
    void toggleProductReview_ZeroId_ThrowsNotFoundException() {
        when(productReviewRepository.findByReviewIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.toggleProductReview(0L));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
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
