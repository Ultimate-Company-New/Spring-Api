package springapi.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import springapi.models.responsemodels.ErrorResponseModel;

@DisplayName("Global Exception Handler Tests")
class GlobalExceptionHandlerTest {

  // Total Tests: 1

  /**
   * Purpose: Verify PermissionException is translated to HTTP 403 response. Expected Result:
   * Forbidden status with standardized error payload is returned. Assertions: Status code and error
   * body fields match expected values.
   */
  @Test
  @DisplayName("globalExceptionHandler - PermissionException ReturnsForbidden - Success")
  void globalExceptionHandler_s01_permissionExceptionReturnsForbidden_success() {
    // Arrange
    GlobalExceptionHandler handler = new GlobalExceptionHandler();
    PermissionException exception = new PermissionException("Missing permission");

    // Act
    ResponseEntity<ErrorResponseModel> response = handler.handlePermissionException(exception);

    // Assert
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Forbidden", response.getBody().getError());
    assertEquals("Access Denied: Missing permission", response.getBody().getMessage());
    assertEquals(HttpStatus.FORBIDDEN.value(), response.getBody().getStatus());
  }
}
