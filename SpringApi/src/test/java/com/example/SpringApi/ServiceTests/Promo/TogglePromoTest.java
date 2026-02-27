package com.example.SpringApi.ServiceTests.Promo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Models.DatabaseModels.Promo;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Test class for PromoService.togglePromo method. */
@DisplayName("PromoService - TogglePromo Tests")
class TogglePromoTest extends PromoServiceTestBase {

  // Total Tests: 16
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Verify double toggle returns to original state.
   * Expected Result: isDeleted equals original.
   * Assertions: isDeleted false after two toggles.
   */
  @Test
  @DisplayName("togglePromo - Double Toggle - Success")
  void togglePromo_DoubleToggle_Success() {
    // Arrange
    testPromo.setIsDeleted(false);
    stubPromoRepositoryFindByPromoIdAndClientId(
        TEST_PROMO_ID, TEST_CLIENT_ID, Optional.of(testPromo));
    stubPromoRepositorySave(testPromo);

    // Act
    promoService.togglePromo(TEST_PROMO_ID);
    promoService.togglePromo(TEST_PROMO_ID);

    // Assert
    assertFalse(testPromo.getIsDeleted());
  }

  /*
   * Purpose: Verify isolation with different promo IDs.
   * Expected Result: Correct promo toggled.
   * Assertions: save called.
   */
  @Test
  @DisplayName("togglePromo - Isolation - Success")
  void togglePromo_Isolation_Success() {
    // Arrange
    Promo otherPromo = new Promo();
    otherPromo.setPromoId(999L);
    otherPromo.setIsDeleted(false);
    stubPromoRepositoryFindByPromoIdAndClientId(
        TEST_PROMO_ID, TEST_CLIENT_ID, Optional.of(testPromo));
    stubPromoRepositoryFindByPromoIdAndClientId(999L, TEST_CLIENT_ID, Optional.of(otherPromo));
    stubPromoRepositorySave(testPromo);

    // Act
    promoService.togglePromo(TEST_PROMO_ID);

    // Assert
    verify(promoRepository, times(1)).save(any(Promo.class));
  }

  /*
   * Purpose: Verify restore from deleted.
   * Expected Result: isDeleted becomes false.
   * Assertions: isDeleted false.
   */
  @Test
  @DisplayName("togglePromo - Restore From Deleted - Success")
  void togglePromo_RestoreFromDeleted_Success() {
    // Arrange
    testPromo.setIsDeleted(true);
    stubPromoRepositoryFindByPromoIdAndClientId(
        TEST_PROMO_ID, TEST_CLIENT_ID, Optional.of(testPromo));
    stubPromoRepositorySave(testPromo);

    // Act
    promoService.togglePromo(TEST_PROMO_ID);

    // Assert
    assertFalse(testPromo.getIsDeleted());
  }

  /*
   * Purpose: Verify basic success.
   * Expected Result: Promo toggled.
   * Assertions: save called once.
   */
  @Test
  @DisplayName("togglePromo - Success - Success")
  void togglePromo_Success_Success() {
    // Arrange
    stubPromoRepositoryFindByPromoIdAndClientId(
        TEST_PROMO_ID, TEST_CLIENT_ID, Optional.of(testPromo));
    stubPromoRepositorySave(testPromo);

    // Act
    promoService.togglePromo(TEST_PROMO_ID);

    // Assert
    verify(promoRepository, times(1)).save(any(Promo.class));
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Verify different client throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: error message matches.
   */
  @Test
  @DisplayName("togglePromo - Different Client - Throws NotFoundException")
  void togglePromo_DifferentClient_ThrowsNotFoundException() {
    // Arrange
    stubPromoRepositoryFindByPromoIdAndClientId(TEST_PROMO_ID, TEST_CLIENT_ID, Optional.empty());

    // Act
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> promoService.togglePromo(TEST_PROMO_ID));

    // Assert
    assertEquals(ErrorMessages.PromoErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   * Purpose: Verify invalid id 999 throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: exception thrown.
   */
  @Test
  @DisplayName("togglePromo - Invalid Id 999 - NotFound")
  void togglePromo_InvalidId_999L() {
    // Arrange
    stubPromoRepositoryFindByPromoIdAndClientId(999L, TEST_CLIENT_ID, Optional.empty());

    // Act & Assert
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> promoService.togglePromo(999L));
    assertEquals(ErrorMessages.PromoErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   * Purpose: Verify invalid max long id throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: exception thrown.
   */
  @Test
  @DisplayName("togglePromo - Invalid Id Max Long - NotFound")
  void togglePromo_InvalidId_MaxLong() {
    // Arrange
    stubPromoRepositoryFindByPromoIdAndClientId(Long.MAX_VALUE, TEST_CLIENT_ID, Optional.empty());

    // Act & Assert
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> promoService.togglePromo(Long.MAX_VALUE));
    assertEquals(ErrorMessages.PromoErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   * Purpose: Verify invalid min long id throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: exception thrown.
   */
  @Test
  @DisplayName("togglePromo - Invalid Id Min Long - NotFound")
  void togglePromo_InvalidId_MinLong() {
    // Arrange
    stubPromoRepositoryFindByPromoIdAndClientId(Long.MIN_VALUE, TEST_CLIENT_ID, Optional.empty());

    // Act & Assert
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> promoService.togglePromo(Long.MIN_VALUE));
    assertEquals(ErrorMessages.PromoErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   * Purpose: Verify invalid negative id throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: exception thrown.
   */
  @Test
  @DisplayName("togglePromo - Invalid Id Negative 100 - NotFound")
  void togglePromo_InvalidId_Negative100L() {
    // Arrange
    stubPromoRepositoryFindByPromoIdAndClientId(-100L, TEST_CLIENT_ID, Optional.empty());

    // Act & Assert
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> promoService.togglePromo(-100L));
    assertEquals(ErrorMessages.PromoErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   * Purpose: Verify negative id throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: exception thrown.
   */
  @Test
  @DisplayName("togglePromo - Negative Id - Throws NotFoundException")
  void togglePromo_NegativeId_ThrowsNotFoundException() {
    // Arrange
    stubPromoRepositoryFindByPromoIdAndClientId(-1L, TEST_CLIENT_ID, Optional.empty());

    // Act & Assert
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> promoService.togglePromo(-1L));
    assertEquals(ErrorMessages.PromoErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   * Purpose: Verify promo not found throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: exception thrown.
   */
  @Test
  @DisplayName("togglePromo - Promo Not Found - Throws NotFoundException")
  void togglePromo_PromoNotFound_ThrowsNotFoundException() {
    // Arrange
    long missingPromoId = TEST_PROMO_ID;
    stubPromoRepositoryFindByPromoIdAndClientId(missingPromoId, TEST_CLIENT_ID, Optional.empty());

    // Act & Assert
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> promoService.togglePromo(missingPromoId));
    assertEquals(ErrorMessages.PromoErrorMessages.INVALID_ID, ex.getMessage());
    assertFalse(ex.getMessage().isBlank());
    verify(promoRepository, times(1)).findByPromoIdAndClientId(missingPromoId, TEST_CLIENT_ID);
    verify(promoRepository, never()).save(any(Promo.class));
    verifyNoMoreInteractions(promoRepository);
  }

  /*
   * Purpose: Verify zero id throws NotFoundException.
   * Expected Result: NotFoundException.
   * Assertions: exception thrown.
   */
  @Test
  @DisplayName("togglePromo - Zero Id - Throws NotFoundException")
  void togglePromo_ZeroId_ThrowsNotFoundException() {
    // Arrange
    stubPromoRepositoryFindByPromoIdAndClientId(0L, TEST_CLIENT_ID, Optional.empty());

    // Act & Assert
    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> promoService.togglePromo(0L));
    assertEquals(ErrorMessages.PromoErrorMessages.INVALID_ID, ex.getMessage());
  }

  /*
   **********************************************************************************************
   * PERMISSION TESTS
   **********************************************************************************************
   */

  /*
   * Purpose: Verify unauthorized access is blocked at controller level.
   * Expected Result: Unauthorized status.
   * Assertions: status is 401.
   */
  @Test
  @DisplayName("togglePromo - Controller Permission - Unauthorized")
  void togglePromo_controller_permission_unauthorized() {
    // Arrange
    stubServiceThrowsUnauthorizedException();

    // Act
    ResponseEntity<?> response = promoController.togglePromo(TEST_PROMO_ID);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  /*
   * Purpose: Verify controller handles exception.
   * Expected Result: INTERNAL_SERVER_ERROR status.
   * Assertions: status is 500.
   */
  @Test
  @DisplayName("togglePromo - Controller Handles Exception - Failure")
  void togglePromo_ControllerHandlesException_Failure() {
    // Arrange
    stubServiceTogglePromoThrowsRuntime("Crash");

    // Act
    ResponseEntity<?> response = promoController.togglePromo(TEST_PROMO_ID);

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  /*
   * Purpose: Verify controller handles not found.
   * Expected Result: NOT_FOUND status.
   * Assertions: status is NOT_FOUND.
   */
  @Test
  @DisplayName("togglePromo - Controller Handles NotFound - Failure")
  void togglePromo_ControllerHandlesNotFound_Failure() {
    // Arrange
    stubServiceTogglePromoThrowsNotFound("Not Found");

    // Act
    ResponseEntity<?> response = promoController.togglePromo(TEST_PROMO_ID);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  /*
   * Purpose: Verify controller delegation.
   * Expected Result: Service called and 200 OK.
   * Assertions: status is OK.
   */
  @Test
  @DisplayName("togglePromo - Controller Delegation - Success")
  void togglePromo_WithValidId_DelegatesToService() {
    // Arrange
    stubServiceTogglePromoDoNothing();

    // Act
    ResponseEntity<?> response = promoController.togglePromo(TEST_PROMO_ID);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}

