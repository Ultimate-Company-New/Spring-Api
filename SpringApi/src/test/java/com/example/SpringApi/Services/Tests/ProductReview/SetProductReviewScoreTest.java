package com.example.SpringApi.Services.Tests.ProductReview;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.ProductReview;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductReviewService.setProductReviewScore method.
 * 
 * Test count: 20 tests
 * - SUCCESS: 12 tests
 * - FAILURE / EXCEPTION: 8 tests
 */
@DisplayName("ProductReviewService - SetProductReviewScore Tests")
class SetProductReviewScoreTest extends ProductReviewServiceTestBase {

    // Total Tests: 20

    // ========================================
    // SUCCESS Tests
    // ========================================

    @Test
    @DisplayName("Set Product Review Score - Decrease Score")
    void setProductReviewScore_Success_DecreaseScore() {
        // Arrange
        testProductReview.setScore(3);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                .thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

        // Assert
        assertEquals(2, testProductReview.getScore());
        verify(productReviewRepository, times(1)).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
        verify(productReviewRepository, times(1)).save(testProductReview);
    }

    @Test
    @DisplayName("Set Product Review Score - Decrease to Zero Minimum")
    void setProductReviewScore_Success_DecreaseToZero() {
        // Arrange
        testProductReview.setScore(0);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                .thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

        // Assert
        assertEquals(0, testProductReview.getScore());
        verify(productReviewRepository, times(1)).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
        verify(productReviewRepository, times(1)).save(testProductReview);
    }

    @Test
    @DisplayName("Set Product Review Score - Increase Score")
    void setProductReviewScore_Success_IncreaseScore() {
        // Arrange
        testProductReview.setScore(3);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                .thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

        // Assert
        assertEquals(4, testProductReview.getScore());
        verify(productReviewRepository, times(1)).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
        verify(productReviewRepository, times(1)).save(testProductReview);
    }

    @Test
    @DisplayName("Set Product Review Score - Null Score Treated as Zero")
    void setProductReviewScore_Success_NullScore() {
        // Arrange
        testProductReview.setScore(null);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                .thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

        // Assert
        assertEquals(1, testProductReview.getScore());
    }

    // ========================================
    // FAILURE / EXCEPTION Tests
    // ========================================

    @Test
    @DisplayName("Set Product Review Score - Max Long ID - Not Found")
    void setProductReviewScore_MaxLongId_ThrowsNotFoundException() {
        // Arrange
        when(productReviewRepository.findByReviewIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.setProductReviewScore(Long.MAX_VALUE, true));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Set Product Review Score - Negative ID - Not Found")
    void setProductReviewScore_NegativeId_ThrowsNotFoundException() {
        // Arrange
        when(productReviewRepository.findByReviewIdAndClientId(-1L, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.setProductReviewScore(-1L, true));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Set Product Review Score - Review Not Found")
    void setProductReviewScore_ReviewNotFound_ThrowsNotFoundException() {
        // Arrange
        lenient().when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                .thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> productReviewService.setProductReviewScore(TEST_REVIEW_ID, true));

        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, exception.getMessage());
        verify(productReviewRepository, times(1)).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
        verify(productReviewRepository, never()).save(any(ProductReview.class));
    }

    @Test
    @DisplayName("Set Product Review Score - Zero ID - Not Found")
    void setProductReviewScore_ZeroId_ThrowsNotFoundException() {
        // Arrange
        when(productReviewRepository.findByReviewIdAndClientId(0L, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.setProductReviewScore(0L, true));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Set Product Review Score - Decrease From 1 To 0 - Success")
    void setProductReviewScore_DecreaseFrom1To0_Success() {
        // Arrange
        testProductReview.setScore(1);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                .thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

        // Assert
        assertEquals(0, testProductReview.getScore());
    }

    @Test
    @DisplayName("Set Product Review Score - Decrease When Null Score - Stays 0")
    void setProductReviewScore_DecreaseWhenNullScore_Stays0() {
        // Arrange
        testProductReview.setScore(null);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                .thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

        // Assert
        assertEquals(0, testProductReview.getScore());
    }

    @Test
    @DisplayName("Set Product Review Score - Increase From 0 To 1 - Success")
    void setProductReviewScore_IncreaseFrom0To1_Success() {
        // Arrange
        testProductReview.setScore(0);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                .thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

        // Assert
        assertEquals(1, testProductReview.getScore());
    }

    @Test
    @DisplayName("Set Product Review Score - Increase From 10 To 11 - Success")
    void setProductReviewScore_IncreaseFrom10To11_Success() {
        // Arrange
        testProductReview.setScore(10);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                .thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

        // Assert
        assertEquals(11, testProductReview.getScore());
    }

    @Test
    @DisplayName("Set Product Review Score - ID Negative 5 - Throws NotFoundException")
    void setProductReviewScore_IdNegative5_ThrowsNotFoundException() {
        // Arrange
        when(productReviewRepository.findByReviewIdAndClientId(-5L, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.setProductReviewScore(-5L, true));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Set Product Review Score - ID 2 - Throws NotFoundException")
    void setProductReviewScore_Id2_ThrowsNotFoundException() {
        // Arrange
        when(productReviewRepository.findByReviewIdAndClientId(2L, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.setProductReviewScore(2L, true));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Set Product Review Score - ID 99 - Throws NotFoundException")
    void setProductReviewScore_Id99_ThrowsNotFoundException() {
        // Arrange
        when(productReviewRepository.findByReviewIdAndClientId(99L, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.setProductReviewScore(99L, true));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Set Product Review Score - ID Min Long - Throws NotFoundException")
    void setProductReviewScore_IdMinLong_ThrowsNotFoundException() {
        // Arrange
        when(productReviewRepository.findByReviewIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.setProductReviewScore(Long.MIN_VALUE, true));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Set Product Review Score - ID Max Long - Throws NotFoundException")
    void setProductReviewScore_IdMaxLong_ThrowsNotFoundException() {
        // Arrange
        when(productReviewRepository.findByReviewIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID)).thenReturn(null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.setProductReviewScore(Long.MAX_VALUE, true));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    // ========================================
    // ADDITIONAL EDGE CASE Tests
    // ========================================

    @Test
    @DisplayName("Set Product Review Score - Increase From 100 To 101 Large Score - Success")
    void setProductReviewScore_IncreaseFromLargeScore_Success() {
        // Arrange
        testProductReview.setScore(100);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                .thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

        // Assert
        assertEquals(101, testProductReview.getScore());
    }

    @Test
    @DisplayName("Set Product Review Score - Increase From 999 To 1000 Very Large - Success")
    void setProductReviewScore_IncreaseFromVeryLargeScore_Success() {
        // Arrange
        testProductReview.setScore(999);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                .thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

        // Assert
        assertEquals(1000, testProductReview.getScore());
    }

    @Test
    @DisplayName("Set Product Review Score - Decrease From 100 To 99 Large Score - Success")
    void setProductReviewScore_DecreaseFromLargeScore_Success() {
        // Arrange
        testProductReview.setScore(100);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                .thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

        // Assert
        assertEquals(99, testProductReview.getScore());
    }

    @Test
    @DisplayName("Set Product Review Score - Decrease From 2 To 1 Near Zero - Success")
    void setProductReviewScore_DecreaseNearZero_Success() {
        // Arrange
        testProductReview.setScore(2);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                .thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

        // Assert
        assertEquals(1, testProductReview.getScore());
    }

    @Test
    @DisplayName("Set Product Review Score - Decrease When Already Zero - Stays Zero")
    void setProductReviewScore_DecreaseWhenAlreadyZero_StaysZero() {
        // Arrange
        testProductReview.setScore(0);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                .thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

        // Assert
        assertEquals(0, testProductReview.getScore());
    }

    @Test
    @DisplayName("Set Product Review Score - Increase When Null Score - Sets To 1")
    void setProductReviewScore_IncreaseWhenNullScore_SetsToOne() {
        // Arrange
        testProductReview.setScore(null);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                .thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

        // Assert
        assertEquals(1, testProductReview.getScore());
    }

    @Test
    @DisplayName("Set Product Review Score - Increase From 5 To 6 Mid Range - Success")
    void setProductReviewScore_IncreaseFromMidRange_Success() {
        // Arrange
        testProductReview.setScore(5);
        when(productReviewRepository.findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID))
                .thenReturn(testProductReview);
        when(productReviewRepository.save(any(ProductReview.class))).thenReturn(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

        // Assert
        assertEquals(6, testProductReview.getScore());
    }
}
