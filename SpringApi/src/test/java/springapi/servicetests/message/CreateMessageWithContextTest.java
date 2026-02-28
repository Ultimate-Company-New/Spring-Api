package springapi.servicetests.message;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import springapi.ErrorMessages;
import springapi.controllers.MessageController;
import springapi.exceptions.BadRequestException;

/** Unit tests for MessageService.createMessageWithContext method. */
@DisplayName("CreateMessageWithContext Tests")
class CreateMessageWithContextTest extends MessageServiceTestBase {

  // Total Tests: 6
  /*
   **********************************************************************************************
   * SUCCESS TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Verify successful message creation with explicit user and client context. Scenario:
   * CreateMessageWithContext with valid request and context parameters. Expected: Message created
   * and logged with provided context.
   */
  @Test
  @DisplayName("Create Message With Context - Valid Context - Success")
  void createMessageWithContext_ValidContext_Success() {
    // Arrange
    stubClientRepositoryFindById(Optional.of(testClient));
    stubMessageRepositorySave(testMessage);

    // Act & Assert
    assertDoesNotThrow(
        () ->
            messageService.createMessageWithContext(
                validRequest, 999L, "system_user", TEST_CLIENT_ID));
    verify(userLogService)
        .logDataWithContext(eq(999L), eq("system_user"), eq(TEST_CLIENT_ID), anyString(), any());
  }

  /*
   **********************************************************************************************
   * FAILURE / EXCEPTION TESTS
   **********************************************************************************************
   */

  /**
   * Purpose: Reject context message creation when requesting user ID is invalid. Expected Result:
   * BadRequestException with InvalidUserId message.
   */
  @Test
  @DisplayName("Create Message With Context - Invalid UserId - Throws BadRequestException")
  void createMessageWithContext_InvalidUserId_ThrowsBadRequestException() {
    // Arrange
    stubClientRepositoryFindById(Optional.of(testClient));

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () ->
                messageService.createMessageWithContext(validRequest, 0L, "admin", TEST_CLIENT_ID));
    assertEquals(ErrorMessages.MessagesErrorMessages.INVALID_USER_ID, exception.getMessage());
  }

  /**
   * Purpose: Reject context message creation when requesting user ID is negative. Expected Result:
   * BadRequestException with InvalidUserId message.
   */
  @Test
  @DisplayName("Create Message With Context - Negative UserId - Throws BadRequestException")
  void createMessageWithContext_NegativeUserId_ThrowsBadRequestException() {
    // Arrange
    stubClientRepositoryFindById(Optional.of(testClient));

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () ->
                messageService.createMessageWithContext(
                    validRequest, -1L, "admin", TEST_CLIENT_ID));
    assertEquals(ErrorMessages.MessagesErrorMessages.INVALID_USER_ID, exception.getMessage());
  }

  /**
   * Purpose: Reject context message creation when request model is null. Expected Result:
   * BadRequestException with InvalidId error code.
   */
  @Test
  @DisplayName("Create Message With Context - Null Request - Throws BadRequestException")
  void createMessageWithContext_NullRequest_ThrowsBadRequestException() {
    // Arrange
    stubClientRepositoryFindById(Optional.of(testClient));

    // Act & Assert
    BadRequestException exception =
        assertThrows(
            BadRequestException.class,
            () ->
                messageService.createMessageWithContext(
                    null, TEST_USER_ID, "admin", TEST_CLIENT_ID));
    assertEquals(ErrorMessages.MessagesErrorMessages.INVALID_ID, exception.getMessage());
  }

  /**
   * Purpose: Reject context message creation for unauthorized/non-existent client ID. Expected
   * Result: NotFoundException with client invalid ID message.
   */
  @Test
  @DisplayName("Create Message With Context - Unauthorized Client - Throws NotFoundException")
  void createMessageWithContext_UnauthorizedClient_ThrowsNotFoundException() {
    // Arrange
    stubClientRepositoryFindById(Optional.empty());

    // Act & Assert
    assertThrowsNotFound(
        ErrorMessages.ClientErrorMessages.INVALID_ID,
        () -> messageService.createMessageWithContext(validRequest, TEST_USER_ID, "admin", 9999L));
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
  @DisplayName("Create Message With Context - Controller permission unauthorized - Success")
  void createMessageWithContext_controller_permission_unauthorized() {
    // Arrange
    MessageController controller = new MessageController(messageServiceMock);
    stubMessageServiceCreateMessageThrowsUnauthorized();

    // Act
    ResponseEntity<?> response = controller.createMessage(validRequest);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
  }
}
