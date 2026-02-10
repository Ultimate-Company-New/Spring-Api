package com.example.SpringApi.Services.Tests.ProductReview;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.ProductReview;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductReviewService.toggleProductReview method.
 * 
 * Test count: 9 tests
 * - SUCCESS: 3 tests
 * - FAILURE / EXCEPTION: 6 tests
 */
@DisplayName("ProductReviewService - ToggleProductReview Tests")
class ToggleProductReviewTest extends ProductReviewServiceTestBase {

    // Total Tests: 9

    // ========================================
    // SUCCESS Tests
    // ========================================

    @Test
    @DisplayName("Toggle Product Review - Success - Mark as Deleted")
    void toggleProductReview_Success_MarkAsDeleted() {
        // Arrange
        testProductReview.setIsDeleted(false);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                .thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);
        when(productReviewRepository.markAllDescendantsAsDeleted(eq(TEST_REVIEW_ID), anyString())).thenReturn(2);

        // Act
        productReviewService.toggleProductReview(TEST_REVIEW_ID);

        // Assert
        assertTrue(testProductReview.getIsDeleted());
        verify(productReviewRepository, times(1)).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
        verify(productReviewRepository, times(1)).save(testProductReview);
        verify(productReviewRepository, times(1)).markAllDescendantsAsDeleted(eq(TEST_REVIEW_ID), anyString());
    }

    @Test
    @DisplayName("Toggle Product Review - Success - Restore from Deleted")
    void toggleProductReview_Success_RestoreFromDeleted() {
        // Arrange
        testProductReview.setIsDeleted(true);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                .thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act
        productReviewService.toggleProductReview(TEST_REVIEW_ID);

        // Assert
        assertFalse(testProductReview.getIsDeleted());
        verify(productReviewRepository, times(1)).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
        verify(productReviewRepository, times(1)).save(testProductReview);
        verify(productReviewRepository, never()).markAllDescendantsAsDeleted(any(), any());
    }

    @Test
    @DisplayName("Toggle Product Review - Multiple Toggles - State Persistence")
    void toggleProductReview_MultipleToggles_StatePersists() {
        // Arrange
        testProductReview.setIsDeleted(false);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                .thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act 1
        productReviewService.toggleProductReview(TEST_REVIEW_ID);

        // Assert 1
        assertTrue(testProductReview.getIsDeleted());

        // Act 2
        productReviewService.toggleProductReview(TEST_REVIEW_ID);

        // Assert 2
        assertFalse(testProductReview.getIsDeleted());
    }

    // ========================================
    // FAILURE / EXCEPTION Tests
    // ========================================

    @Test
    @DisplayName("Toggle Product Review - Negative ID - Not Found")
    void toggleProductReview_NegativeId_ThrowsNotFoundException() {
        // Arrange
        when(productReviewRepository.findByReviewIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.toggleProductReview(-1L));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Toggle Product Review - Review Not Found")
    void toggleProductReview_ReviewNotFound_ThrowsNotFoundException() {
        // Arrange
        lenient().when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                .thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> productReviewService.toggleProductReview(TEST_REVIEW_ID));

        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, exception.getMessage());
        verify(productReviewRepository, times(1)).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
        verify(productReviewRepository, never()).save(any(ProductReview.class));
    }

    @Test
    @DisplayName("Toggle Product Review - Zero ID - Not Found")
    void toggleProductReview_ZeroId_ThrowsNotFoundException() {
        // Arrange
        when(productReviewRepository.findByReviewIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.toggleProductReview(0L));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Toggle Product Review - ID Negative 100 - Throws NotFoundException")
    void toggleProductReview_IdNegative100_ThrowsNotFoundException() {
        // Arrange
        when(productReviewRepository.findByReviewIdAndClientId(-100L, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.toggleProductReview(-100L));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Toggle Product Review - ID 2 - Throws NotFoundException")
    void toggleProductReview_Id2_ThrowsNotFoundException() {
        // Arrange
        when(productReviewRepository.findByReviewIdAndClientId(2L, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.toggleProductReview(2L));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Toggle Product Review - ID 999 - Throws NotFoundException")
    void toggleProductReview_Id999_ThrowsNotFoundException() {
        // Arrange
        when(productReviewRepository.findByReviewIdAndClientId(999L, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.toggleProductReview(999L));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Toggle Product Review - ID Min Long - Throws NotFoundException")
    void toggleProductReview_IdMinLong_ThrowsNotFoundException() {
        // Arrange
        when(productReviewRepository.findByReviewIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.toggleProductReview(Long.MIN_VALUE));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Toggle Product Review - ID Max Long - Throws NotFoundException")
    void toggleProductReview_IdMaxLong_ThrowsNotFoundException() {
        // Arrange
        when(productReviewRepository.findByReviewIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.toggleProductReview(Long.MAX_VALUE));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Toggle Product Review - ID 12345 - Throws NotFoundException")
    void toggleProductReview_Id12345_ThrowsNotFoundException() {
        // Arrange
        when(productReviewRepository.findByReviewIdAndClientId(12345L, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.toggleProductReview(12345L));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }
}
