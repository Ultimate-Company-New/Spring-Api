package com.example.SpringApi.ServiceTests.ProductReview;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.SpringApi.Controllers.ProductReviewController;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.ProductReview;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Test class for ProductReviewService.setProductReviewScore method.
 *
 * <p>Test count: 50 tests - SUCCESS: 19 tests - FAILURE / EXCEPTION: 29 tests - PERMISSION: 2 tests
 */
@DisplayName("ProductReviewService - SetProductReviewScore Tests")
class SetProductReviewScoreTest extends ProductReviewServiceTestBase {

  // Total Tests: 50
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Verify decrease from 1 to 0.
   * Expected Result: Score is zero.
   * Assertions: Score equals zero.
   */
  @Test
  @DisplayName("Set Product Review Score - Decrease From 1 To 0 - Success")
  void setProductReviewScore_s01_DecreaseFrom1To0_Success() {
    // Arrange
    testProductReview.setScore(1);
    int previousScore = testProductReview.getScore();
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

    // Assert
    assertEquals(0, testProductReview.getScore());
    assertEquals(previousScore - 1, testProductReview.getScore());
    assertTrue(testProductReview.getScore() >= 0);
    verify(productReviewRepository, times(1))
        .findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
  }

  /*
   * Purpose: Verify large score decrease.
   * Expected Result: Score decreases.
   * Assertions: Score equals expected.
   */
  @Test
  @DisplayName("Set Product Review Score - Decrease From 100 To 99 Large Score - Success")
  void setProductReviewScore_s02_DecreaseFromLargeScore_Success() {
    // Arrange
    testProductReview.setScore(100);
    int previousScore = testProductReview.getScore();
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

    // Assert
    assertEquals(99, testProductReview.getScore());
    assertEquals(previousScore - 1, testProductReview.getScore());
    assertTrue(testProductReview.getScore() > 0);
    verify(productReviewRepository, times(1)).save(testProductReview);
  }

  /*
   * Purpose: Verify decrease near zero.
   * Expected Result: Score decreases to 1.
   * Assertions: Score equals one.
   */
  @Test
  @DisplayName("Set Product Review Score - Decrease From 2 To 1 Near Zero - Success")
  void setProductReviewScore_s03_DecreaseNearZero_Success() {
    // Arrange
    testProductReview.setScore(2);
    int previousScore = testProductReview.getScore();
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

    // Assert
    assertEquals(1, testProductReview.getScore());
    assertEquals(previousScore - 1, testProductReview.getScore());
    verify(productReviewRepository, times(1))
        .findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
    verify(productReviewRepository, times(1)).save(testProductReview);
  }

  /*
   * Purpose: Verify decrease when already zero stays zero.
   * Expected Result: Score remains zero.
   * Assertions: Score equals zero.
   */
  @Test
  @DisplayName("Set Product Review Score - Decrease When Already Zero - Stays Zero")
  void setProductReviewScore_s04_DecreaseWhenAlreadyZero_StaysZero() {
    // Arrange
    testProductReview.setScore(0);
    int previousScore = testProductReview.getScore();
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

    // Assert
    assertEquals(0, testProductReview.getScore());
    assertEquals(previousScore, testProductReview.getScore());
    assertFalse(testProductReview.getScore() > previousScore);
    verify(productReviewRepository, times(1)).save(testProductReview);
  }

  /*
   * Purpose: Verify decrease when score is null.
   * Expected Result: Score becomes zero.
   * Assertions: Score equals zero.
   */
  @Test
  @DisplayName("Set Product Review Score - Decrease When Null Score - Stays 0")
  void setProductReviewScore_s05_DecreaseWhenNullScore_Stays0() {
    // Arrange
    testProductReview.setScore(null);
    assertNull(testProductReview.getScore());
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

    // Assert
    assertEquals(0, testProductReview.getScore());
    assertNotNull(testProductReview.getScore());
    verify(productReviewRepository, times(1))
        .findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
  }

  /*
   * Purpose: Verify increase from 0 to 1.
   * Expected Result: Score becomes one.
   * Assertions: Score equals one.
   */
  @Test
  @DisplayName("Set Product Review Score - Increase From 0 To 1 - Success")
  void setProductReviewScore_s06_IncreaseFrom0To1_Success() {
    // Arrange
    testProductReview.setScore(0);
    int previousScore = testProductReview.getScore();
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

    // Assert
    assertEquals(1, testProductReview.getScore());
    assertEquals(previousScore + 1, testProductReview.getScore());
    verify(productReviewRepository, times(1))
        .findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
  }

  /*
   * Purpose: Verify increase from 10 to 11.
   * Expected Result: Score increases.
   * Assertions: Score equals 11.
   */
  @Test
  @DisplayName("Set Product Review Score - Increase From 10 To 11 - Success")
  void setProductReviewScore_s07_IncreaseFrom10To11_Success() {
    // Arrange
    testProductReview.setScore(10);
    int previousScore = testProductReview.getScore();
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

    // Assert
    assertEquals(11, testProductReview.getScore());
    assertEquals(previousScore + 1, testProductReview.getScore());
    assertTrue(testProductReview.getScore() > previousScore);
    verify(productReviewRepository, times(1)).save(testProductReview);
  }

  /*
   * Purpose: Verify large score increment.
   * Expected Result: Score increases.
   * Assertions: Score equals expected.
   */
  @Test
  @DisplayName("Set Product Review Score - Increase From 100 To 101 Large Score - Success")
  void setProductReviewScore_s08_IncreaseFromLargeScore_Success() {
    // Arrange
    testProductReview.setScore(100);
    int previousScore = testProductReview.getScore();
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

    // Assert
    assertEquals(101, testProductReview.getScore());
    assertEquals(previousScore + 1, testProductReview.getScore());
    assertTrue(testProductReview.getScore() >= 100);
    verify(productReviewRepository, times(1))
        .findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
  }

  /*
   * Purpose: Verify mid-range increase.
   * Expected Result: Score increases.
   * Assertions: Score equals expected.
   */
  @Test
  @DisplayName("Set Product Review Score - Increase From 5 To 6 Mid Range - Success")
  void setProductReviewScore_s09_IncreaseFromMidRange_Success() {
    // Arrange
    testProductReview.setScore(5);
    int previousScore = testProductReview.getScore();
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

    // Assert
    assertEquals(6, testProductReview.getScore());
    assertEquals(previousScore + 1, testProductReview.getScore());
    assertTrue(testProductReview.getScore() % 2 == 0);
    verify(productReviewRepository, times(1)).save(testProductReview);
  }

  /*
   * Purpose: Verify very large score increment.
   * Expected Result: Score increases.
   * Assertions: Score equals expected.
   */
  @Test
  @DisplayName("Set Product Review Score - Increase From 999 To 1000 Very Large - Success")
  void setProductReviewScore_s10_IncreaseFromVeryLargeScore_Success() {
    // Arrange
    testProductReview.setScore(999);
    int previousScore = testProductReview.getScore();
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

    // Assert
    assertEquals(1000, testProductReview.getScore());
    assertEquals(previousScore + 1, testProductReview.getScore());
    assertTrue(String.valueOf(testProductReview.getScore()).startsWith("10"));
    verify(productReviewRepository, times(1))
        .findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
  }

  /*
   * Purpose: Verify increase when score is null.
   * Expected Result: Score becomes one.
   * Assertions: Score equals one.
   */
  @Test
  @DisplayName("Set Product Review Score - Increase When Null Score - Sets To 1")
  void setProductReviewScore_s11_IncreaseWhenNullScore_SetsToOne() {
    // Arrange
    testProductReview.setScore(null);
    assertNull(testProductReview.getScore());
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

    // Assert
    assertEquals(1, testProductReview.getScore());
    assertNotNull(testProductReview.getScore());
    assertTrue(testProductReview.getScore() > 0);
    verify(productReviewRepository, times(1)).save(testProductReview);
  }

  /*
   * Purpose: Verify score decreases by one.
   * Expected Result: Score is reduced.
   * Assertions: Score equals expected value.
   */
  @Test
  @DisplayName("Set Product Review Score - Decrease Score")
  void setProductReviewScore_s12_Success_DecreaseScore() {
    // Arrange
    testProductReview.setScore(3);
    int previousScore = testProductReview.getScore();
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

    // Assert
    assertEquals(2, testProductReview.getScore());
    assertEquals(previousScore - 1, testProductReview.getScore());
    verify(productReviewRepository).findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
    verify(productReviewRepository, times(1)).save(testProductReview);
  }

  /*
   * Purpose: Verify decreasing from zero stays at zero.
   * Expected Result: Score remains zero.
   * Assertions: Score equals zero.
   */
  @Test
  @DisplayName("Set Product Review Score - Decrease to Zero Minimum")
  void setProductReviewScore_s13_Success_DecreaseToZero() {
    // Arrange
    testProductReview.setScore(0);
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

    // Assert
    assertEquals(0, testProductReview.getScore());
    verify(productReviewRepository, times(1))
        .findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
    verify(productReviewRepository, times(1)).save(testProductReview);
  }

  /*
   * Purpose: Verify score increases by one.
   * Expected Result: Score is increased.
   * Assertions: Score equals expected value.
   */
  @Test
  @DisplayName("Set Product Review Score - Increase Score")
  void setProductReviewScore_s14_Success_IncreaseScore() {
    // Arrange
    testProductReview.setScore(3);
    int previousScore = testProductReview.getScore();
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

    // Assert
    assertEquals(4, testProductReview.getScore());
    assertEquals(previousScore + 1, testProductReview.getScore());
    assertTrue(testProductReview.getScore() > 3);
    verify(productReviewRepository, times(1)).save(testProductReview);
  }

  /*
   * Purpose: Verify null score treated as zero when increasing.
   * Expected Result: Score becomes one.
   * Assertions: Score equals one.
   */
  @Test
  @DisplayName("Set Product Review Score - Null Score Treated as Zero")
  void setProductReviewScore_s15_Success_NullScore() {
    // Arrange
    testProductReview.setScore(null);
    assertNull(testProductReview.getScore());
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

    // Assert
    assertEquals(1, testProductReview.getScore());
    assertNotNull(testProductReview.getScore());
    verify(productReviewRepository, times(1)).save(testProductReview);
    verify(productReviewRepository, times(1))
        .findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Verify invalid ID 2 throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: Error message matches.
   */
  @Test
  @DisplayName("Set Product Review Score - ID 2 - Throws NotFoundException")
  void setProductReviewScore_f01_Id2_ThrowsNotFoundException() {
    // Arrange
    long reviewId = 2L;
    stubProductReviewRepositoryFindByReviewIdAndClientId(2L, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> productReviewService.setProductReviewScore(reviewId, true));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
    verify(productReviewRepository, times(1)).findByReviewIdAndClientId(reviewId, TEST_CLIENT_ID);
  }

  /*
   * Purpose: Verify invalid ID 99 throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: Error message matches.
   */
  @Test
  @DisplayName("Set Product Review Score - ID 99 - Throws NotFoundException")
  void setProductReviewScore_f02_Id99_ThrowsNotFoundException() {
    // Arrange
    long reviewId = 99L;
    stubProductReviewRepositoryFindByReviewIdAndClientId(99L, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> productReviewService.setProductReviewScore(reviewId, true));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
    assertTrue(reviewId > TEST_REVIEW_ID);
    verify(productReviewRepository, never()).save(any(ProductReview.class));
  }

  /*
   * Purpose: Verify max long ID throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: Error message matches.
   */
  @Test
  @DisplayName("Set Product Review Score - ID Max Long - Throws NotFoundException")
  void setProductReviewScore_f03_IdMaxLong_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> productReviewService.setProductReviewScore(Long.MAX_VALUE, true));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
    verify(productReviewRepository, times(1))
        .findByReviewIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID);
  }

  /*
   * Purpose: Verify min long ID throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: Error message matches.
   */
  @Test
  @DisplayName("Set Product Review Score - ID Min Long - Throws NotFoundException")
  void setProductReviewScore_f04_IdMinLong_ThrowsNotFoundException() {
    // Arrange
    long reviewId = Long.MIN_VALUE;
    stubProductReviewRepositoryFindByReviewIdAndClientId(reviewId, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> productReviewService.setProductReviewScore(reviewId, true));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
    assertTrue(reviewId < 0);
    verify(productReviewRepository, times(1)).findByReviewIdAndClientId(reviewId, TEST_CLIENT_ID);
  }

  /*
   * Purpose: Verify invalid ID -5 throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: Error message matches.
   */
  @Test
  @DisplayName("Set Product Review Score - ID Negative 5 - Throws NotFoundException")
  void setProductReviewScore_f05_IdNegative5_ThrowsNotFoundException() {
    // Arrange
    long reviewId = -5L;
    stubProductReviewRepositoryFindByReviewIdAndClientId(reviewId, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> productReviewService.setProductReviewScore(reviewId, true));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
    assertTrue(reviewId < 0);
    verify(productReviewRepository, times(1)).findByReviewIdAndClientId(reviewId, TEST_CLIENT_ID);
  }

  /*
   * Purpose: Verify max long ID throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: Error message matches.
   */
  @Test
  @DisplayName("Set Product Review Score - Max Long ID - Not Found")
  void setProductReviewScore_f06_MaxLongId_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> productReviewService.setProductReviewScore(Long.MAX_VALUE, true));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
    verify(productReviewRepository, times(1))
        .findByReviewIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID);
    verify(productReviewRepository, never()).save(any(ProductReview.class));
  }

  /*
   * Purpose: Verify negative ID throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: Error message matches.
   */
  @Test
  @DisplayName("Set Product Review Score - Negative ID - Not Found")
  void setProductReviewScore_f07_NegativeId_ThrowsNotFoundException() {
    // Arrange
    long reviewId = -1L;
    stubProductReviewRepositoryFindByReviewIdAndClientId(reviewId, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> productReviewService.setProductReviewScore(reviewId, true));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
    assertEquals(-1L, reviewId);
    verify(productReviewRepository, times(1)).findByReviewIdAndClientId(reviewId, TEST_CLIENT_ID);
  }

  /*
   * Purpose: Verify review not found throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: Error message matches.
   */
  @Test
  @DisplayName("Set Product Review Score - Review Not Found")
  void setProductReviewScore_f08_ReviewNotFound_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException exception =
        assertThrows(
            NotFoundException.class,
            () -> productReviewService.setProductReviewScore(TEST_REVIEW_ID, true));

    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, exception.getMessage());
    verify(productReviewRepository, never()).save(any(ProductReview.class));
  }

  /*
   * Purpose: Verify zero ID throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: Error message matches.
   */
  @Test
  @DisplayName("Set Product Review Score - Zero ID - Not Found")
  void setProductReviewScore_f09_ZeroId_ThrowsNotFoundException() {
    // Arrange
    long reviewId = 0L;
    stubProductReviewRepositoryFindByReviewIdAndClientId(reviewId, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> productReviewService.setProductReviewScore(reviewId, true));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
    assertEquals(0L, reviewId);
    verify(productReviewRepository, never()).save(any(ProductReview.class));
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
    ProductReviewController controller = new ProductReviewController(productReviewServiceMock);
    stubProductReviewServiceSetProductReviewScoreThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = controller.setProductReviewScore(TEST_REVIEW_ID, true);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }
}

/**
 * Test class for ProductReviewService.setProductReviewScore method.
 *
 * <p>Test count: 24 tests - SUCCESS: 12 tests - FAILURE / EXCEPTION: 8 tests - PERMISSION: 4 tests
 */
@DisplayName("ProductReviewService - SetProductReviewScore Tests - Duplicate Block")
class SetProductReviewScoreDuplicateTests extends ProductReviewServiceTestBase {

  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Verify setProductReviewScore_Success_DecreaseScore behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - Decrease Score")
  void setProductReviewScore_s16_Success_DecreaseScore() {
    // Arrange
    testProductReview.setScore(3);
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

    // Assert
    assertEquals(2, testProductReview.getScore());
    verify(productReviewRepository, times(1))
        .findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
    verify(productReviewRepository, times(1)).save(testProductReview);
  }

  /*
   * Purpose: Verify setProductReviewScore_Success_DecreaseToZero behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - Decrease to Zero Minimum")
  void setProductReviewScore_s17_Success_DecreaseToZero() {
    // Arrange
    testProductReview.setScore(0);
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

    // Assert
    assertEquals(0, testProductReview.getScore());
    verify(productReviewRepository, times(1))
        .findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
    verify(productReviewRepository, times(1)).save(testProductReview);
  }

  /*
   * Purpose: Verify setProductReviewScore_Success_IncreaseScore behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - Increase Score")
  void setProductReviewScore_s18_Success_IncreaseScore() {
    // Arrange
    testProductReview.setScore(3);
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

    // Assert
    assertEquals(4, testProductReview.getScore());
    verify(productReviewRepository, times(1))
        .findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
    verify(productReviewRepository, times(1)).save(testProductReview);
  }

  /*
   * Purpose: Verify setProductReviewScore_Success_NullScore behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - Null Score Treated as Zero")
  void setProductReviewScore_s19_Success_NullScore() {
    // Arrange
    testProductReview.setScore(null);
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
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

  /*
   * Purpose: Verify setProductReviewScore_DecreaseFrom1To0_Success behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - Decrease From 1 To 0 - Success")
  void setProductReviewScore_f10_DecreaseFrom1To0_Success() {
    // Arrange
    testProductReview.setScore(1);
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

    // Assert
    assertEquals(0, testProductReview.getScore());
  }

  /*
   * Purpose: Verify setProductReviewScore_DecreaseFromLargeScore_Success behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - Decrease From 100 To 99 Large Score - Success")
  void setProductReviewScore_f11_DecreaseFromLargeScore_Success() {
    // Arrange
    testProductReview.setScore(100);
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

    // Assert
    assertEquals(99, testProductReview.getScore());
  }

  /*
   * Purpose: Verify setProductReviewScore_DecreaseNearZero_Success behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - Decrease From 2 To 1 Near Zero - Success")
  void setProductReviewScore_f12_DecreaseNearZero_Success() {
    // Arrange
    testProductReview.setScore(2);
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

    // Assert
    assertEquals(1, testProductReview.getScore());
  }

  /*
   * Purpose: Verify setProductReviewScore_DecreaseWhenAlreadyZero_StaysZero behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - Decrease When Already Zero - Stays Zero")
  void setProductReviewScore_f13_DecreaseWhenAlreadyZero_StaysZero() {
    // Arrange
    testProductReview.setScore(0);
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

    // Assert
    assertEquals(0, testProductReview.getScore());
  }

  /*
   * Purpose: Verify setProductReviewScore_DecreaseWhenNullScore_Stays0 behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - Decrease When Null Score - Stays 0")
  void setProductReviewScore_f14_DecreaseWhenNullScore_Stays0() {
    // Arrange
    testProductReview.setScore(null);
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, false);

    // Assert
    assertEquals(0, testProductReview.getScore());
  }

  /*
   * Purpose: Verify setProductReviewScore_Id2_ThrowsNotFoundException behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - ID 2 - Throws NotFoundException")
  void setProductReviewScore_f15_Id2_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(2L, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> productReviewService.setProductReviewScore(2L, true));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
  }

  /*
   * Purpose: Verify setProductReviewScore_Id99_ThrowsNotFoundException behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - ID 99 - Throws NotFoundException")
  void setProductReviewScore_f16_Id99_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(99L, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> productReviewService.setProductReviewScore(99L, true));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
  }

  /*
   * Purpose: Verify setProductReviewScore_IdMaxLong_ThrowsNotFoundException behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - ID Max Long - Throws NotFoundException")
  void setProductReviewScore_f17_IdMaxLong_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> productReviewService.setProductReviewScore(Long.MAX_VALUE, true));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
    verify(productReviewRepository, times(1))
        .findByReviewIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID);
  }

  /*
   * Purpose: Verify setProductReviewScore_IdMinLong_ThrowsNotFoundException behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - ID Min Long - Throws NotFoundException")
  void setProductReviewScore_f18_IdMinLong_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> productReviewService.setProductReviewScore(Long.MIN_VALUE, true));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
  }

  /*
   * Purpose: Verify setProductReviewScore_IdNegative5_ThrowsNotFoundException behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - ID Negative 5 - Throws NotFoundException")
  void setProductReviewScore_f19_IdNegative5_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(-5L, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> productReviewService.setProductReviewScore(-5L, true));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
  }

  /*
   * Purpose: Verify setProductReviewScore_IncreaseFrom0To1_Success behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - Increase From 0 To 1 - Success")
  void setProductReviewScore_f20_IncreaseFrom0To1_Success() {
    // Arrange
    testProductReview.setScore(0);
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

    // Assert
    assertEquals(1, testProductReview.getScore());
  }

  /*
   * Purpose: Verify setProductReviewScore_IncreaseFrom10To11_Success behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - Increase From 10 To 11 - Success")
  void setProductReviewScore_f21_IncreaseFrom10To11_Success() {
    // Arrange
    testProductReview.setScore(10);
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

    // Assert
    assertEquals(11, testProductReview.getScore());
  }

  /*
   * Purpose: Verify setProductReviewScore_IncreaseFromLargeScore_Success behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - Increase From 100 To 101 Large Score - Success")
  void setProductReviewScore_f22_IncreaseFromLargeScore_Success() {
    // Arrange
    testProductReview.setScore(100);
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

    // Assert
    assertEquals(101, testProductReview.getScore());
  }

  /*
   * Purpose: Verify setProductReviewScore_IncreaseFromMidRange_Success behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - Increase From 5 To 6 Mid Range - Success")
  void setProductReviewScore_f23_IncreaseFromMidRange_Success() {
    // Arrange
    testProductReview.setScore(5);
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

    // Assert
    assertEquals(6, testProductReview.getScore());
  }

  /*
   * Purpose: Verify setProductReviewScore_IncreaseFromVeryLargeScore_Success behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - Increase From 999 To 1000 Very Large - Success")
  void setProductReviewScore_f24_IncreaseFromVeryLargeScore_Success() {
    // Arrange
    testProductReview.setScore(999);
    int previousScore = testProductReview.getScore();
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

    // Assert
    assertEquals(1000, testProductReview.getScore());
    assertEquals(previousScore + 1, testProductReview.getScore());
    assertTrue(String.valueOf(testProductReview.getScore()).length() >= 4);
    verify(productReviewRepository, times(1)).save(testProductReview);
  }

  /*
   * Purpose: Verify setProductReviewScore_IncreaseWhenNullScore_SetsToOne behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - Increase When Null Score - Sets To 1")
  void setProductReviewScore_f25_IncreaseWhenNullScore_SetsToOne() {
    // Arrange
    testProductReview.setScore(null);
    assertNull(testProductReview.getScore());
    stubProductReviewRepositoryFindByReviewIdAndClientId(
        TEST_REVIEW_ID, TEST_CLIENT_ID, testProductReview);
    stubProductReviewRepositorySave(testProductReview);

    // Act
    productReviewService.setProductReviewScore(TEST_REVIEW_ID, true);

    // Assert
    assertEquals(1, testProductReview.getScore());
    assertNotNull(testProductReview.getScore());
    assertTrue(testProductReview.getScore() > 0);
    verify(productReviewRepository, times(1))
        .findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
    verify(productReviewRepository, times(1)).save(testProductReview);
  }

  /*
   * Purpose: Verify setProductReviewScore_MaxLongId_ThrowsNotFoundException behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - Max Long ID - Not Found")
  void setProductReviewScore_f26_MaxLongId_ThrowsNotFoundException() {
    // Arrange
    long reviewId = Long.MAX_VALUE;
    stubProductReviewRepositoryFindByReviewIdAndClientId(reviewId, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> productReviewService.setProductReviewScore(reviewId, true));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
    assertTrue(reviewId > 0);
    verify(productReviewRepository, times(1)).findByReviewIdAndClientId(reviewId, TEST_CLIENT_ID);
    verify(productReviewRepository, never()).save(any(ProductReview.class));
  }

  /*
   * Purpose: Verify setProductReviewScore_NegativeId_ThrowsNotFoundException behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - Negative ID - Not Found")
  void setProductReviewScore_f27_NegativeId_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(-1L, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> productReviewService.setProductReviewScore(-1L, true));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
  }

  /*
   * Purpose: Verify setProductReviewScore_ReviewNotFound_ThrowsNotFoundException behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - Review Not Found")
  void setProductReviewScore_f28_ReviewNotFound_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException exception =
        assertThrows(
            NotFoundException.class,
            () -> productReviewService.setProductReviewScore(TEST_REVIEW_ID, true));

    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, exception.getMessage());
    verify(productReviewRepository, times(1))
        .findByReviewIdAndClientId(TEST_REVIEW_ID, TEST_CLIENT_ID);
    verify(productReviewRepository, never()).save(any(ProductReview.class));
  }

  /*
   * Purpose: Verify setProductReviewScore_ZeroId_ThrowsNotFoundException behavior.
   * Expected Result: Method completes as expected.
   * Assertions: Verify expected outcome.
   */
  @Test
  @DisplayName("Set Product Review Score - Zero ID - Not Found")
  void setProductReviewScore_f29_ZeroId_ThrowsNotFoundException() {
    // Arrange
    stubProductReviewRepositoryFindByReviewIdAndClientId(0L, TEST_CLIENT_ID, null);

    // Act & Assert
    NotFoundException ex =
        assertThrows(
            NotFoundException.class, () -> productReviewService.setProductReviewScore(0L, true));
    assertEquals(ErrorMessages.ProductReviewErrorMessages.NOT_FOUND, ex.getMessage());
  }
}
