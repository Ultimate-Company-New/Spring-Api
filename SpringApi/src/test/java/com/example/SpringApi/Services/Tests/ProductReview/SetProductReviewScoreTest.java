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
 * Test class for ProductReviewService.setProductReviewScore method.
 * 
 * Test count: 26 tests
 * - SUCCESS: 12 tests
 * - FAILURE / EXCEPTION: 12 tests
 * - PERMISSION: 2 tests
 */
@DisplayName("ProductReviewService - SetProductReviewScore Tests")
class SetProductReviewScoreTest extends ProductReviewServiceTestBase {

    // Total Tests: 26

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify score decreases by one.
     * Expected Result: Score is reduced.
     * Assertions: Score equals expected value.
     */
    @Test
    @DisplayName("Set Product Review Score - Decrease Score")
    void setProductReviewScore_Success_DecreaseScore() {
        // Arrange
        testProductReview.setScore(3);
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

        // Assert
        assertEquals(2, testProductReview.getScore());
        verify(productReviewRepository, times(1)).save(testProductReview);
    }

    /*
     * Purpose: Verify decreasing from zero stays at zero.
     * Expected Result: Score remains zero.
     * Assertions: Score equals zero.
     */
    @Test
    @DisplayName("Set Product Review Score - Decrease to Zero Minimum")
    void setProductReviewScore_Success_DecreaseToZero() {
        // Arrange
        testProductReview.setScore(0);
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

        // Assert
        assertEquals(0, testProductReview.getScore());
    }

    /*
     * Purpose: Verify score increases by one.
     * Expected Result: Score is increased.
     * Assertions: Score equals expected value.
     */
    @Test
    @DisplayName("Set Product Review Score - Increase Score")
    void setProductReviewScore_Success_IncreaseScore() {
        // Arrange
        testProductReview.setScore(3);
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

        // Assert
        assertEquals(4, testProductReview.getScore());
    }

    /*
     * Purpose: Verify null score treated as zero when increasing.
     * Expected Result: Score becomes one.
     * Assertions: Score equals one.
     */
    @Test
    @DisplayName("Set Product Review Score - Null Score Treated as Zero")
    void setProductReviewScore_Success_NullScore() {
        // Arrange
        testProductReview.setScore(null);
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

        // Assert
        assertEquals(1, testProductReview.getScore());
    }

    /*
     * Purpose: Verify decrease from 1 to 0.
     * Expected Result: Score is zero.
     * Assertions: Score equals zero.
     */
    @Test
    @DisplayName("Set Product Review Score - Decrease From 1 To 0 - Success")
    void setProductReviewScore_DecreaseFrom1To0_Success() {
        // Arrange
        testProductReview.setScore(1);
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

        // Assert
        assertEquals(0, testProductReview.getScore());
    }

    /*
     * Purpose: Verify decrease when score is null.
     * Expected Result: Score becomes zero.
     * Assertions: Score equals zero.
     */
    @Test
    @DisplayName("Set Product Review Score - Decrease When Null Score - Stays 0")
    void setProductReviewScore_DecreaseWhenNullScore_Stays0() {
        // Arrange
        testProductReview.setScore(null);
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

        // Assert
        assertEquals(0, testProductReview.getScore());
    }

    /*
     * Purpose: Verify increase from 0 to 1.
     * Expected Result: Score becomes one.
     * Assertions: Score equals one.
     */
    @Test
    @DisplayName("Set Product Review Score - Increase From 0 To 1 - Success")
    void setProductReviewScore_IncreaseFrom0To1_Success() {
        // Arrange
        testProductReview.setScore(0);
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

        // Assert
        assertEquals(1, testProductReview.getScore());
    }

    /*
     * Purpose: Verify increase from 10 to 11.
     * Expected Result: Score increases.
     * Assertions: Score equals 11.
     */
    @Test
    @DisplayName("Set Product Review Score - Increase From 10 To 11 - Success")
    void setProductReviewScore_IncreaseFrom10To11_Success() {
        // Arrange
        testProductReview.setScore(10);
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

        // Assert
        assertEquals(11, testProductReview.getScore());
    }

    /*
     * Purpose: Verify large score increment.
     * Expected Result: Score increases.
     * Assertions: Score equals expected.
     */
    @Test
    @DisplayName("Set Product Review Score - Increase From 100 To 101 Large Score - Success")
    void setProductReviewScore_IncreaseFromLargeScore_Success() {
        // Arrange
        testProductReview.setScore(100);
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

        // Assert
        assertEquals(101, testProductReview.getScore());
    }

    /*
     * Purpose: Verify very large score increment.
     * Expected Result: Score increases.
     * Assertions: Score equals expected.
     */
    @Test
    @DisplayName("Set Product Review Score - Increase From 999 To 1000 Very Large - Success")
    void setProductReviewScore_IncreaseFromVeryLargeScore_Success() {
        // Arrange
        testProductReview.setScore(999);
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

        // Assert
        assertEquals(1000, testProductReview.getScore());
    }

    /*
     * Purpose: Verify large score decrease.
     * Expected Result: Score decreases.
     * Assertions: Score equals expected.
     */
    @Test
    @DisplayName("Set Product Review Score - Decrease From 100 To 99 Large Score - Success")
    void setProductReviewScore_DecreaseFromLargeScore_Success() {
        // Arrange
        testProductReview.setScore(100);
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

        // Assert
        assertEquals(99, testProductReview.getScore());
    }

    /*
     * Purpose: Verify decrease near zero.
     * Expected Result: Score decreases to 1.
     * Assertions: Score equals one.
     */
    @Test
    @DisplayName("Set Product Review Score - Decrease From 2 To 1 Near Zero - Success")
    void setProductReviewScore_DecreaseNearZero_Success() {
        // Arrange
        testProductReview.setScore(2);
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

        // Assert
        assertEquals(1, testProductReview.getScore());
    }

    /*
     * Purpose: Verify decrease when already zero stays zero.
     * Expected Result: Score remains zero.
     * Assertions: Score equals zero.
     */
    @Test
    @DisplayName("Set Product Review Score - Decrease When Already Zero - Stays Zero")
    void setProductReviewScore_DecreaseWhenAlreadyZero_StaysZero() {
        // Arrange
        testProductReview.setScore(0);
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

        // Assert
        assertEquals(0, testProductReview.getScore());
    }

    /*
     * Purpose: Verify increase when score is null.
     * Expected Result: Score becomes one.
     * Assertions: Score equals one.
     */
    @Test
    @DisplayName("Set Product Review Score - Increase When Null Score - Sets To 1")
    void setProductReviewScore_IncreaseWhenNullScore_SetsToOne() {
        // Arrange
        testProductReview.setScore(null);
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

        // Assert
        assertEquals(1, testProductReview.getScore());
    }

    /*
     * Purpose: Verify mid-range increase.
     * Expected Result: Score increases.
     * Assertions: Score equals expected.
     */
    @Test
    @DisplayName("Set Product Review Score - Increase From 5 To 6 Mid Range - Success")
    void setProductReviewScore_IncreaseFromMidRange_Success() {
        // Arrange
        testProductReview.setScore(5);
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

        // Assert
        assertEquals(6, testProductReview.getScore());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    /*
     * Purpose: Verify max long ID throws NotFoundException.
     * Expected Result: NotFoundException.
     * Assertions: Error message matches.
     */
    @Test
    @DisplayName("Set Product Review Score - Max Long ID - Not Found")
    void setProductReviewScore_MaxLongId_ThrowsNotFoundException() {
        // Arrange
        stubProductReviewRepositoryFindByReviewIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID, null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.setProductReviewScore(Long.MAX_VALUE, true));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    /*
     * Purpose: Verify negative ID throws NotFoundException.
     * Expected Result: NotFoundException.
     * Assertions: Error message matches.
     */
    @Test
    @DisplayName("Set Product Review Score - Negative ID - Not Found")
    void setProductReviewScore_NegativeId_ThrowsNotFoundException() {
        // Arrange
        stubProductReviewRepositoryFindByReviewIdAndClientId(-1L, TEST_CLIENT_ID, null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.setProductReviewScore(-1L, true));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    /*
     * Purpose: Verify review not found throws NotFoundException.
     * Expected Result: NotFoundException.
     * Assertions: Error message matches.
     */
    @Test
    @DisplayName("Set Product Review Score - Review Not Found")
    void setProductReviewScore_ReviewNotFound_ThrowsNotFoundException() {
        // Arrange
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, null);

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> productReviewService.setProductReviewScore(TEST_REVIEW_ID, true));

        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, exception.getMessage());
        verify(productReviewRepository, never()).save(any(ProductReview.class));
    }

    /*
     * Purpose: Verify zero ID throws NotFoundException.
     * Expected Result: NotFoundException.
     * Assertions: Error message matches.
     */
    @Test
    @DisplayName("Set Product Review Score - Zero ID - Not Found")
    void setProductReviewScore_ZeroId_ThrowsNotFoundException() {
        // Arrange
        stubProductReviewRepositoryFindByReviewIdAndClientId(0L, TEST_CLIENT_ID, null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.setProductReviewScore(0L, true));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    /*
     * Purpose: Verify invalid ID -5 throws NotFoundException.
     * Expected Result: NotFoundException.
     * Assertions: Error message matches.
     */
    @Test
    @DisplayName("Set Product Review Score - ID Negative 5 - Throws NotFoundException")
    void setProductReviewScore_IdNegative5_ThrowsNotFoundException() {
        // Arrange
        stubProductReviewRepositoryFindByReviewIdAndClientId(-5L, TEST_CLIENT_ID, null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.setProductReviewScore(-5L, true));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    /*
     * Purpose: Verify invalid ID 2 throws NotFoundException.
     * Expected Result: NotFoundException.
     * Assertions: Error message matches.
     */
    @Test
    @DisplayName("Set Product Review Score - ID 2 - Throws NotFoundException")
    void setProductReviewScore_Id2_ThrowsNotFoundException() {
        // Arrange
        stubProductReviewRepositoryFindByReviewIdAndClientId(2L, TEST_CLIENT_ID, null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.setProductReviewScore(2L, true));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    /*
     * Purpose: Verify invalid ID 99 throws NotFoundException.
     * Expected Result: NotFoundException.
     * Assertions: Error message matches.
     */
    @Test
    @DisplayName("Set Product Review Score - ID 99 - Throws NotFoundException")
    void setProductReviewScore_Id99_ThrowsNotFoundException() {
        // Arrange
        stubProductReviewRepositoryFindByReviewIdAndClientId(99L, TEST_CLIENT_ID, null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.setProductReviewScore(99L, true));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    /*
     * Purpose: Verify min long ID throws NotFoundException.
     * Expected Result: NotFoundException.
     * Assertions: Error message matches.
     */
    @Test
    @DisplayName("Set Product Review Score - ID Min Long - Throws NotFoundException")
    void setProductReviewScore_IdMinLong_ThrowsNotFoundException() {
        // Arrange
        stubProductReviewRepositoryFindByReviewIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID, null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.setProductReviewScore(Long.MIN_VALUE, true));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    /*
     * Purpose: Verify max long ID throws NotFoundException.
     * Expected Result: NotFoundException.
     * Assertions: Error message matches.
     */
    @Test
    @DisplayName("Set Product Review Score - ID Max Long - Throws NotFoundException")
    void setProductReviewScore_IdMaxLong_ThrowsNotFoundException() {
        // Arrange
        stubProductReviewRepositoryFindByReviewIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID, null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.setProductReviewScore(Long.MAX_VALUE, true));
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
    @DisplayName("setProductReviewScore - Controller Permission - Unauthorized")
    void setProductReviewScore_controller_permission_unauthorized() {
        // Arrange
        IProductReviewSubTranslator serviceMock = mock(IProductReviewSubTranslator.class);
        ProductReviewController controller = new ProductReviewController(serviceMock);
        doThrow(new com.example.SpringApi.Exceptions.UnauthorizedException(ErrorMessages.ERROR_UNAUTHORIZED))
                .when(serviceMock).setProductReviewScore(anyLong(), anyBoolean());

        // Act
        ResponseEntity<?> response = controller.setProductReviewScore(TEST_REVIEW_ID, true);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    /*
     * Purpose: Verify @PreAuthorize annotation is present.
     * Expected Result: Annotation exists and is configured.
     * Assertions: Annotation is not null.
     */
    @Test
    @DisplayName("setProductReviewScore - Verify @PreAuthorize Annotation")
    void setProductReviewScore_verifyPreAuthorizeAnnotation_success() throws NoSuchMethodException {
        // Arrange
        Method method = ProductReviewController.class.getMethod("setProductReviewScore", long.class, boolean.class);

        // Act
        PreAuthorize annotation = method.getAnnotation(PreAuthorize.class);

        // Assert
        assertNotNull(annotation, "setProductReviewScore should have @PreAuthorize annotation");
    }
}

/**
 * Test class for ProductReviewService.setProductReviewScore method.
 * 
 * Test count: 24 tests
 * - SUCCESS: 12 tests
 * - FAILURE / EXCEPTION: 8 tests
 * - PERMISSION: 4 tests
 */
@DisplayName("ProductReviewService - SetProductReviewScore Tests - Duplicate Block")
class SetProductReviewScoreTestDuplicate extends ProductReviewServiceTestBase {

    // Total Tests: 24

    /*
     **********************************************************************************************
     * SUCCESS TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("Set Product Review Score - Decrease Score")
    void setProductReviewScore_Success_DecreaseScore() {
        // Arrange
        testProductReview.setScore(3);
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

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
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

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
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

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
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

        // Act
        productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

        // Assert
        assertEquals(1, testProductReview.getScore());
    }

    /*
     **********************************************************************************************
     * FAILURE / EXCEPTION TESTS
     **********************************************************************************************
     */

    @Test
    @DisplayName("Set Product Review Score - Max Long ID - Not Found")
    void setProductReviewScore_MaxLongId_ThrowsNotFoundException() {
        // Arrange
        stubProductReviewRepositoryFindByReviewIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID, null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.setProductReviewScore(Long.MAX_VALUE, true));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Set Product Review Score - Negative ID - Not Found")
    void setProductReviewScore_NegativeId_ThrowsNotFoundException() {
        // Arrange
        stubProductReviewRepositoryFindByReviewIdAndClientId(-1L, TEST_CLIENT_ID, null);

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> productReviewService.setProductReviewScore(-1L, true));
        assertEquals(ErrorMessages.ProductReviewErrorMessages.NotFound, ex.getMessage());
    }

    @Test
    @DisplayName("Set Product Review Score - Review Not Found")
    void setProductReviewScore_ReviewNotFound_ThrowsNotFoundException() {
        // Arrange
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, null);

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
        stubProductReviewRepositoryFindByReviewIdAndClientId(0L, TEST_CLIENT_ID, null);

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
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

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
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

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
        stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
        stubProductReviewRepositorySave(testProductReview);

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
