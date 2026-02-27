package com.example.springapi.ServiceTests.ProductReview;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.springapi.ErrorMessages;
import com.example.springapi.controllers.ProductReviewController;
import com.example.springapi.exceptions.NotFoundException;
import com.example.springapi.models.databasemodels.ProductReview;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Test class for ProductReviewService.toggleProductReview method. */
@DisplayName("ProductReviewService - ToggleProductReview Tests")
class ToggleProductReviewTest extends ProductReviewServiceTestBase {

  // Total Tests: 26
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Verify multiple toggles preserve state.
   * Expected Result: State returns to original after even toggles.
   * Assertions: isDeleted equals original state.
   */
  @Test
  @DisplayName("Toggle Product Review - Multiple Toggles - State Persists")
  void toggleProductReview_s01_MultipleToggles_StatePersists() {
    // Arrange
    testProductReview.setIsDeleted(false);
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.toggleProductReview(TEST_REVIEW_ID);
    productReviewService.toggleProductReview(TEST_REVIEW_ID);

    // Assert
    assertFalse(testProductReview.getIsDeleted());
  }

  /*
   * Purpose: Verify marking review as deleted.
   * Expected Result: isDeleted toggles to true.
   * Assertions: isDeleted is true.
   */
  @Test
  @DisplayName("Toggle Product Review - Mark As Deleted - Success")
  void toggleProductReview_s02_Success_MarkAsDeleted() {
    // Arrange
    testProductReview.setIsDeleted(false);
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
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
  void toggleProductReview_s03_Success_RestoreFromDeleted() {
    // Arrange
    testProductReview.setIsDeleted(true);
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
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
   * Purpose: Verify invalid ID 12345 throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: error message matches.
   */
  @Test
  @DisplayName("Toggle Product Review - ID 12345 - Throws NotFoundException")
  void toggleProductReview_f01_Id12345_ThrowsNotFoundException() {
    // Arrange
    long reviewId = 12345L;
    stubProductReviewRepositoryFindByReviewIdAndClientId(reviewId, TEST_CLIENT_ID, null);

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> productReviewService.toggleProductReview(reviewId));

    // Assert
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
    assertTrue(reviewId > 10000L);
  }

  /*
   * Purpose: Verify invalid ID 2 throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: error message matches.
   */
  @Test
  @DisplayName("Toggle Product Review - ID 2 - Throws NotFoundException")
  void toggleProductReview_f02_Id2_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(2L, TEST_CLIENT_ID, null);

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> productReviewService.toggleProductReview(2L));

    // Assert
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
  }

  /*
   * Purpose: Verify invalid ID 999 throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: error message matches.
   */
  @Test
  @DisplayName("Toggle Product Review - ID 999 - Throws NotFoundException")
  void toggleProductReview_f03_Id999_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(999L, TEST_CLIENT_ID, null);

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> productReviewService.toggleProductReview(999L));

    // Assert
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
  }

  /*
   * Purpose: Verify max long ID throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: error message matches.
   */
  @Test
  @DisplayName("Toggle Product Review - ID Max Long - Throws NotFoundException")
  void toggleProductReview_f04_IdMaxLong_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID, null);

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> productReviewService.toggleProductReview(Long.MAX_VALUE));

    // Assert
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
  }

  /*
   * Purpose: Verify min long ID throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: error message matches.
   */
  @Test
  @DisplayName("Toggle Product Review - ID Min Long - Throws NotFoundException")
  void toggleProductReview_f05_IdMinLong_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID, null);

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> productReviewService.toggleProductReview(Long.MIN_VALUE));

    // Assert
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
  }

  /*
   * Purpose: Verify invalid ID -100 throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: error message matches.
   */
  @Test
  @DisplayName("Toggle Product Review - ID Negative 100 - Throws NotFoundException")
  void toggleProductReview_f06_IdNegative100_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(-100L, TEST_CLIENT_ID, null);

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> productReviewService.toggleProductReview(-100L));

    // Assert
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
  }

  /*
   * Purpose: Verify negative ID throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: error message matches.
   */
  @Test
  @DisplayName("Toggle Product Review - Negative ID - Throws NotFoundException")
  void toggleProductReview_f07_NegativeId_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(-1L, TEST_CLIENT_ID, null);

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> productReviewService.toggleProductReview(-1L));

    // Assert
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
  }

  /*
   * Purpose: Verify review not found throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: error message matches.
   */
  @Test
  @DisplayName("Toggle Product Review - Review Not Found - Throws NotFoundException")
  void toggleProductReview_f08_ReviewNotFound_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, null);

    // Act
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> productReviewService.toggleProductReview(TEST_REVIEW_ID));

    // Assert
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
  }

  /*
   * Purpose: Verify zero ID throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: error message matches.
   */
  @Test
  @DisplayName("Toggle Product Review - Zero ID - Throws NotFoundException")
  void toggleProductReview_f09_ZeroId_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(0L, TEST_CLIENT_ID, null);

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> productReviewService.toggleProductReview(0L));

    // Assert
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
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
    ProductReviewController controller = new ProductReviewController(productReviewServiceMock);
    stubProductReviewServiceToggleProductReviewThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = controller.toggleProductReview(TEST_REVIEW_ID);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }
}

/**
 * Test class for ProductReviewService.toggleProductReview method.
 *
 * <p>Test count: 9 tests - SUCCESS: 3 tests - FAILURE / EXCEPTION: 6 tests
 */
@DisplayName("ProductReviewService - ToggleProductReview Tests - Duplicate Block")
class ToggleProductReviewDuplicateTests extends ProductReviewServiceTestBase {

  // ========================================
  // SUCCESS Tests
  // ========================================

  /*
   * Purpose: Verify toggleProductReview_MultipleToggles_StatePersists behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Toggle Product Review - Multiple Toggles - State Persistence")
  void toggleProductReview_s04_MultipleToggles_StatePersists() {
    // Arrange
    testProductReview.setIsDeleted(false);
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act 1
    productReviewService.toggleProductReview(TEST_REVIEW_ID);

    // Assert 1
    assertTrue(testProductReview.getIsDeleted());

    // Act 2
    productReviewService.toggleProductReview(TEST_REVIEW_ID);

    // Assert 2
    assertFalse(testProductReview.getIsDeleted());
  }

  /*
   * Purpose: Verify toggleProductReview_Success_MarkAsDeleted behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Toggle Product Review - Success - Mark as Deleted")
  void toggleProductReview_s05_Success_MarkAsDeleted() {
    // Arrange
    testProductReview.setIsDeleted(false);
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);
    stubProductReviewRepositoryMarkAllDescendantsAsDeleted(2);

    // Act
    productReviewService.toggleProductReview(TEST_REVIEW_ID);

    // Assert
    assertTrue(testProductReview.getIsDeleted());
    verify(productReviewRepository, times(1))
        .findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
    verify(productReviewRepository, times(1)).save(testProductReview);
    verify(productReviewRepository, times(1))
        .markAllDescendantsAsDeleted(eq(TEST_REVIEW_ID), anyString());
  }

  /*
   * Purpose: Verify toggleProductReview_Success_RestoreFromDeleted behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Toggle Product Review - Success - Restore from Deleted")
  void toggleProductReview_s06_Success_RestoreFromDeleted() {
    // Arrange
    testProductReview.setIsDeleted(true);
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.toggleProductReview(TEST_REVIEW_ID);

    // Assert
    assertFalse(testProductReview.getIsDeleted());
    verify(productReviewRepository, times(1))
        .findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
    verify(productReviewRepository, times(1)).save(testProductReview);
    verify(productReviewRepository, never()).markAllDescendantsAsDeleted(any(), any());
  }

  // ========================================
  // FAILURE / EXCEPTION Tests
  // ========================================

  /*
   * Purpose: Verify toggleProductReview_Id12345_ThrowsNotFoundException behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Toggle Product Review - ID 12345 - Throws NotFoundException")
  void toggleProductReview_f10_Id12345_ThrowsNotFoundException() {
    // Arrange
    long reviewId = 12345L;
    stubProductReviewRepositoryFindByReviewIdAndClientId(reviewId, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> productReviewService.toggleProductReview(reviewId));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
    verify(productReviewRepository, times(1)).findByReviewIdAndClientId(reviewId, TEST_CLIENT_ID);
  }

  /*
   * Purpose: Verify toggleProductReview_Id2_ThrowsNotFoundException behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Toggle Product Review - ID 2 - Throws NotFoundException")
  void toggleProductReview_f11_Id2_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(2L, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> productReviewService.toggleProductReview(2L));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
  }

  /*
   * Purpose: Verify toggleProductReview_Id999_ThrowsNotFoundException behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Toggle Product Review - ID 999 - Throws NotFoundException")
  void toggleProductReview_f12_Id999_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(999L, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> productReviewService.toggleProductReview(999L));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
  }

  /*
   * Purpose: Verify toggleProductReview_IdMaxLong_ThrowsNotFoundException behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Toggle Product Review - ID Max Long - Throws NotFoundException")
  void toggleProductReview_f13_IdMaxLong_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> productReviewService.toggleProductReview(Long.MAX_VALUE));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
  }

  /*
   * Purpose: Verify toggleProductReview_IdMinLong_ThrowsNotFoundException behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Toggle Product Review - ID Min Long - Throws NotFoundException")
  void toggleProductReview_f14_IdMinLong_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> productReviewService.toggleProductReview(Long.MIN_VALUE));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
  }

  /*
   * Purpose: Verify toggleProductReview_IdNegative100_ThrowsNotFoundException behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Toggle Product Review - ID Negative 100 - Throws NotFoundException")
  void toggleProductReview_f15_IdNegative100_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(-100L, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> productReviewService.toggleProductReview(-100L));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
  }

  /*
   * Purpose: Verify toggleProductReview_NegativeId_ThrowsNotFoundException behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Toggle Product Review - Negative ID - Not Found")
  void toggleProductReview_f16_NegativeId_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(-1L, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> productReviewService.toggleProductReview(-1L));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
  }

  /*
   * Purpose: Verify toggleProductReview_ReviewNotFound_ThrowsNotFoundException behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Toggle Product Review - Review Not Found")
  void toggleProductReview_f17_ReviewNotFound_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException exception =
        assertThrows(
            NotFoundException.class,
            () -> productReviewService.toggleProductReview(TEST_REVIEW_ID));

    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, exception.getMessage());
    verify(productReviewRepository, times(1))
        .findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
    verify(productReviewRepository, never()).save(any(ProductReview.class));
  }

  /*
   * Purpose: Verify toggleProductReview_ZeroId_ThrowsNotFoundException behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Toggle Product Review - Zero ID - Not Found")
  void toggleProductReview_f18_ZeroId_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(0L, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> productReviewService.toggleProductReview(0L));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
  }
}
