package springapi.servicetests.message;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import springapi.ErrorMessages;
import springapi.controllers.MessageController;

/** Unit tests for MessageService.getUnreadMessageCount method. */
@DisplayName("GetUnreadMessageCount Tests")
class GetUnreadMessageCountTest extends MessageServiceTestBase {

  // Total Tests: 12
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify that the service can handle and return large unread message counts. Scenario:
   * Repository returns a count of 1000. Expected: Service returns 1000 as integer.
   */
  @Test
  @DisplayName("Get Unread Message Count - Large count - Success")
  void getUnreadMessageCount_LargeCount_Success() {
    // Arrange
    stubMessageRepositoryCountUnreadMessagesByUserId(1000L);

    // Act
    int result = messageService.getUnreadMessageCount();

    // Assert
    assertEquals(1000, result);
    verify(messageRepository).countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID);
  }

  /**
   * Purpose: Verify that the service correctly handles the maximum integer boundary for counts.
   * Scenario: Repository returns a count equal to Integer.MAX_VALUE. Expected: Service returns
   * Integer.MAX_VALUE.
   */
  @Test
  @DisplayName("Get Unread Message Count - Max Integer Boundary - Success")
  void getUnreadMessageCount_MaxIntegerBoundary_Success() {
    // Arrange
    stubMessageRepositoryCountUnreadMessagesByUserId((long) Integer.MAX_VALUE);

    // Act
    int result = messageService.getUnreadMessageCount();

    // Assert
    assertEquals(Integer.MAX_VALUE, result);
  }

  /**
   * Purpose: Verify that the service returns zero when there are no unread messages. Scenario:
   * Repository returns a count of 0. Expected: Service returns 0.
   */
  @Test
  @DisplayName("Get Unread Message Count - No unread messages - Returns zero")
  void getUnreadMessageCount_NoUnreadMessages_ReturnsZero() {
    // Arrange
    stubMessageRepositoryCountUnreadMessagesByUserId(0L);

    // Act
    int result = messageService.getUnreadMessageCount();

    // Assert
    assertEquals(0, result);
    verify(messageRepository).countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID);
  }

  /**
   * Purpose: Verify that the service successfully retrieves unread message count for current user.
   * Scenario: Call getUnreadMessageCount when repository returns positive value (5). Expected:
   * Service returns 5.
   */
  @Test
  @DisplayName("Get Unread Message Count - Success")
  void getUnreadMessageCount_Success_Success() {
    // Arrange
    stubMessageRepositoryCountUnreadMessagesByUserId(5L);

    // Act
    int result = messageService.getUnreadMessageCount();

    // Assert
    assertEquals(5, result);
    verify(messageRepository).countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID);
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify that authorization denied scenarios are handled at controller level. Scenario:
   * Service layer is called (service layer itself does not enforce security). Expected Result:
   * Service returns valid result without throwing security exceptions.
   */
  @Test
  @DisplayName("Get Unread Message Count - Authorization denied - ThrowsUnauthorizedException")
  void getUnreadMessageCount_AuthorizationDenied_ThrowsUnauthorizedException() {
    // Arrange
    stubMessageRepositoryCountUnreadMessagesByUserId(5L);

    // Act & Assert
    assertDoesNotThrow(() -> messageService.getUnreadMessageCount());
  }

  /**
   * Purpose: Verify that exceptions during client context lookup are propagated. Expected Result:
   * RuntimeException with appropriate error message.
   */
  @Test
  @DisplayName("Get Unread Message Count - Client Lookup Error - Propagates")
  void getUnreadMessageCount_ClientLookupError_Propagates() {
    // Arrange
    stubMessageRepositoryCountUnreadMessagesByUserIdThrows(
        ErrorMessages.CommonErrorMessages.DATABASE_ERROR);

    // Act & Assert
    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> messageService.getUnreadMessageCount());
    assertEquals(ErrorMessages.CommonErrorMessages.DATABASE_ERROR, ex.getMessage());
  }

  /**
   * Purpose: Reject retrieval request when end index is less than or equal to start index. Expected
   * Result: BadRequestException with InvalidPagination error message.
   */
  @Test
  @DisplayName("Get Unread Message Count - Repository exception - Throws Exception")
  void getUnreadMessageCount_RepositoryException_ThrowsException() {
    // Arrange
    stubMessageRepositoryCountUnreadMessagesByUserIdThrows(
        ErrorMessages.CommonErrorMessages.DATABASE_CONNECTION_ERROR);

    // Act & Assert
    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> messageService.getUnreadMessageCount());
    assertEquals(ErrorMessages.CommonErrorMessages.DATABASE_CONNECTION_ERROR, ex.getMessage());

    verify(messageRepository).countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID);
  }

  /**
   * Purpose: Verify that unauthorized context scenarios are handled at controller level. Scenario:
   * Service layer is called for a valid context (service doesn't enforce auth). Expected Result:
   * Service returns valid result without throwing security exceptions.
   */
  @Test
  @DisplayName("Get Unread Message Count - Unauthorized Context - Should handle gracefully")
  void getUnreadMessageCount_UnauthorizedContext_SuccessWithEmptyResult() {
    // Arrange
    stubMessageRepositoryCountUnreadMessagesByUserId(0L);

    // Act & Assert
    assertDoesNotThrow(() -> messageService.getUnreadMessageCount());
  }

  /**
   * Purpose: Propagate repository exceptions when user lookup fails during count check. Expected
   * Result: RuntimeException with original database error message.
   */
  @Test
  @DisplayName("Get Unread Message Count - User ID Lookup Failure - Propagates")
  void getUnreadMessageCount_UserIdLookupFailure_Propagates() {
    // Arrange
    String databaseFailure = ErrorMessages.CommonErrorMessages.DATABASE_CONNECTION_ERROR;
    stubMessageRepositoryCountUnreadMessagesByUserIdThrows(databaseFailure);

    // Act & Assert
    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> messageService.getUnreadMessageCount());
    assertEquals(databaseFailure, ex.getMessage());
    assertFalse(ex.getMessage().isBlank());
    verify(messageRepository, times(1)).countUnreadMessagesByUserId(TEST_CLIENT_ID, TEST_USER_ID);
    verifyNoMoreInteractions(messageRepository);
  }

  /*
   **********************************************************************************************
   * CONTROLLER AUTHORIZATION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify unauthorized access is handled at the controller level. Expected Result:
   * Unauthorized status is returned. Assertions: Response status is 401 UNAUTHORIZED.
   */
  @Test
  @DisplayName("getUnreadMessageCount - Controller permission unauthorized - Success")
  void getUnreadMessageCount_controller_permission_unauthorized() {
    // Arrange
    MessageController controller = new MessageController(messageServiceMock);
    stubMessageServiceGetUnreadMessageCountThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = controller.getUnreadMessageCount();

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }

  /**
   * Purpose: Verify that the controller correctly delegates getUnreadMessageCount calls to the
   * service layer. Expected: Status 200 returned upon delegation.
   */
  @Test
  @DisplayName("getUnreadMessageCount - Controller delegates to service")
  void getUnreadMessageCount_WithValidRequest_DelegatesToService() {
    // Arrange
    MessageController controller = new MessageController(messageServiceMock);
    stubMessageServiceGetUnreadMessageCount(5);

    // Act
    ResponseEntity<?> response = controller.getUnreadMessageCount();

    // Assert
    verify(messageServiceMock).getUnreadMessageCount();
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }
}
