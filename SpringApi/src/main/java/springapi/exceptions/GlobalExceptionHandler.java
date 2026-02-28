package springapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import springapi.logging.ContextualLogger;
import springapi.models.responsemodels.ErrorResponseModel;

/**
 * Global exception handler for the Spring API. Catches exceptions that are not handled by.
 * individual controllers and returns appropriate HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final ContextualLogger logger =
      ContextualLogger.getLogger(GlobalExceptionHandler.class);

  /**
   * Handles PermissionException thrown when a user lacks required permissions. Returns HTTP 403.
   * Forbidden with an error message.
   *
   * @param ex The PermissionException
   * @return ResponseEntity with 403 status and error details
   */
  @ExceptionHandler(PermissionException.class)
  public ResponseEntity<ErrorResponseModel> handlePermissionException(PermissionException ex) {
    logger.error(ex);
    ErrorResponseModel errorResponse =
        new ErrorResponseModel(
            "Forbidden", "Access Denied: " + ex.getMessage(), HttpStatus.FORBIDDEN.value());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
  }
}
