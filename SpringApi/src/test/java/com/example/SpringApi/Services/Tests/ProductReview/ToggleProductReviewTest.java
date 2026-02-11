package com.example.SpringApi.Services.Tests.ProductReview;

import com.example.SpringApi.Controllers.ProductReviewController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.ProductReview;
import com.example.SpringApi.Services.Interface.IProductReviewSubTranslator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for ProductReviewService.toggleProductReview method.
 */
@DisplayName("ProductReviewService - ToggleProductReview Tests")
class ToggleProductReviewTest extends ProductReviewServiceTestBase {

    // Total Tests: 26

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify marking review as deleted.
     * Expected Result: isDeleted toggles to true.
     * Assertions: isDeleted is true.
     */
    @Test
    @DisplayName("Toggle Product Review - Mark As Deleted - Success")
    void toggleProductReview_Success_MarkAsDeleted() {
        // Arrange
        testProductReview.setIsDeleted(false);
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);
        stubProductReviewRepositoryMarkAllDescendantsAsDeleted(TEST_REVIEW_ID, 2);

        // Act
        productReviewService.toggleProductReview(TEST_REVIEW_ID);

        // Assert
        assertTrue(testProductReview.getIsDeleted());
    }

    /*
     * Purpose: Verify restore from deleted.
     * Expected Result: isDeleted toggles to false.
     * Assertions: isDeleted is false.
     */
    @Test
    @DisplayName("Toggle Product Review - Restore From Deleted - Success")
    void toggleProductReview_Success_RestoreFromDeleted() {
        // Arrange
        testProductReview.setIsDeleted(true);
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.toggleProductReview(TEST_REVIEW_ID);

        // Assert
        assertFalse(testProductReview.getIsDeleted());
    }

    /*
     * Purpose: Verify multiple toggles preserve state.
     * Expected Result: State returns to original after even toggles.
     * Assertions: isDeleted equals original state.
     */
    @Test
    @DisplayName("Toggle Product Review - Multiple Toggles - State Persists")
    void toggleProductReview_MultipleToggles_StatePersists() {
        // Arrange
        testProductReview.setIsDeleted(false);
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.toggleProductReview(TEST_REVIEW_ID);
        productReviewService.toggleProductReview(TEST_REVIEW_ID);

        // Assert
        assertFalse(testProductReview.getIsDeleted());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify negative ID throws NotFoundException.
     * Expected Result: NotFoundException.
     * Assertions: error message matches.
     */
    @Test
    @DisplayName("Toggle Product Review - Negative ID - Throws NotFoundException")
    void toggleProductReview_NegativeId_ThrowsNotFoundException() {
        // Arrange
        stubProductReviewRepositoryFindByReviewIdAndClientId(-1L, TEST_CLIENT_ID, null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.toggleProductReview(-1L));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    /*
     * Purpose: Verify review not found throws NotFoundException.
     * Expected Result: NotFoundException.
     * Assertions: error message matches.
     */
    @Test
    @DisplayName("Toggle Product Review - Review Not Found - Throws NotFoundException")
    void toggleProductReview_ReviewNotFound_ThrowsNotFoundException() {
        // Arrange
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.toggleProductReview(TEST_REVIEW_ID));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    /*
     * Purpose: Verify zero ID throws NotFoundException.
     * Expected Result: NotFoundException.
     * Assertions: error message matches.
     */
    @Test
    @DisplayName("Toggle Product Review - Zero ID - Throws NotFoundException")
    void toggleProductReview_ZeroId_ThrowsNotFoundException() {
        // Arrange
        stubProductReviewRepositoryFindByReviewIdAndClientId(0L, TEST_CLIENT_ID, null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.toggleProductReview(0L));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    /*
     * Purpose: Verify invalid ID -100 throws NotFoundException.
     * Expected Result: NotFoundException.
     * Assertions: error message matches.
     */
    @Test
    @DisplayName("Toggle Product Review - ID Negative 100 - Throws NotFoundException")
    void toggleProductReview_IdNegative100_ThrowsNotFoundException() {
        // Arrange
        stubProductReviewRepositoryFindByReviewIdAndClientId(-100L, TEST_CLIENT_ID, null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.toggleProductReview(-100L));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    /*
     * Purpose: Verify invalid ID 2 throws NotFoundException.
     * Expected Result: NotFoundException.
     * Assertions: error message matches.
     */
    @Test
    @DisplayName("Toggle Product Review - ID 2 - Throws NotFoundException")
    void toggleProductReview_Id2_ThrowsNotFoundException() {
        // Arrange
        stubProductReviewRepositoryFindByReviewIdAndClientId(2L, TEST_CLIENT_ID, null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.toggleProductReview(2L));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    /*
     * Purpose: Verify invalid ID 999 throws NotFoundException.
     * Expected Result: NotFoundException.
     * Assertions: error message matches.
     */
    @Test
    @DisplayName("Toggle Product Review - ID 999 - Throws NotFoundException")
    void toggleProductReview_Id999_ThrowsNotFoundException() {
        // Arrange
        stubProductReviewRepositoryFindByReviewIdAndClientId(999L, TEST_CLIENT_ID, null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.toggleProductReview(999L));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    /*
     * Purpose: Verify min long ID throws NotFoundException.
     * Expected Result: NotFoundException.
     * Assertions: error message matches.
     */
    @Test
    @DisplayName("Toggle Product Review - ID Min Long - Throws NotFoundException")
    void toggleProductReview_IdMinLong_ThrowsNotFoundException() {
        // Arrange
        stubProductReviewRepositoryFindByReviewIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID, null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.toggleProductReview(Long.MIN_VALUE));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    /*
     * Purpose: Verify max long ID throws NotFoundException.
     * Expected Result: NotFoundException.
     * Assertions: error message matches.
     */
    @Test
    @DisplayName("Toggle Product Review - ID Max Long - Throws NotFoundException")
    void toggleProductReview_IdMaxLong_ThrowsNotFoundException() {
        // Arrange
        stubProductReviewRepositoryFindByReviewIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID, null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.toggleProductReview(Long.MAX_VALUE));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    /*
     * Purpose: Verify invalid ID 12345 throws NotFoundException.
     * Expected Result: NotFoundException.
     * Assertions: error message matches.
     */
    @Test
    @DisplayName("Toggle Product Review - ID 12345 - Throws NotFoundException")
    void toggleProductReview_Id12345_ThrowsNotFoundException() {
        // Arrange
        stubProductReviewRepositoryFindByReviewIdAndClientId(12345L, TEST_CLIENT_ID, null);

        // Act
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.toggleProductReview(12345L));

        // Assert
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
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
    @DisplayName("toggleProductReview - Controller Permission - Unauthorized")
    void toggleProductReview_controller_permission_unauthorized() {
        // Arrange
        IProductReviewSubTranslator serviceMock = mock(IProductReviewSubTranslator.class);
        ProductReviewController controller = new ProductReviewController(serviceMock);
        doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                .when(serviceMock).toggleProductReview(anyLong());

        // Act
        ResponseEntity<?> response = controller.toggleProductReview(TEST_REVIEW_ID);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /*
     * Purpose: Verify @PreAuthorize annotation is present.
     * Expected Result: Annotation exists.
     * Assertions: Annotation is not null.
     */
    @Test
    @DisplayName("toggleProductReview - Verify @PreAuthorize Annotation")
    void toggleProductReview_verifyPreAuthorizeAnnotation_success() throws NoSuchMethodException {
        // Arrange
        Method method = ProductReviewController.class.getMethod("toggleProductReview", long.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "toggleProductReview should have @PreAuthorize annotation");
    }
}

/**
 * Test class for ProductReviewService.toggleProductReview method.
 * 
 * Test count: 9 tests
 * - SUCCESS: 3 tests
 * - FAILURE / EXCEPTION: 6 tests
 */
@DisplayName("ProductReviewService - ToggleProductReview Tests - Duplicate Block")
class ToggleProductReviewTestDuplicate extends ProductReviewServiceTestBase {

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
