package springapi.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import springapi.ErrorMessages;
import springapi.logging.ContextualLogger;
import springapi.models.responsemodels.ErrorResponseModel;

/** Shared controller helpers for consistent error responses and logging. */
public abstract class BaseController {

  protected ResponseEntity<ErrorResponseModel> badRequest(ContextualLogger logger, Exception e) {
    return buildErrorResponse(
        logger, e, HttpStatus.BAD_REQUEST, ErrorMessages.ERROR_BAD_REQUEST, e.getMessage());
  }

  protected ResponseEntity<ErrorResponseModel> badRequestWithInternalCode(
      ContextualLogger logger, Exception e) {
    return buildErrorResponse(
        logger,
        e,
        HttpStatus.BAD_REQUEST,
        ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
        e.getMessage());
  }

  protected ResponseEntity<ErrorResponseModel> notFound(ContextualLogger logger, Exception e) {
    return buildErrorResponse(
        logger, e, HttpStatus.NOT_FOUND, ErrorMessages.ERROR_NOT_FOUND, e.getMessage());
  }

  protected ResponseEntity<ErrorResponseModel> unauthorized(ContextualLogger logger, Exception e) {
    return buildErrorResponse(
        logger, e, HttpStatus.UNAUTHORIZED, ErrorMessages.ERROR_UNAUTHORIZED, e.getMessage());
  }

  protected ResponseEntity<ErrorResponseModel> forbidden(ContextualLogger logger, Exception e) {
    return buildErrorResponse(logger, e, HttpStatus.FORBIDDEN, "Forbidden", e.getMessage());
  }

  protected ResponseEntity<ErrorResponseModel> internalServerError(
      ContextualLogger logger, Exception e) {
    return buildErrorResponse(
        logger,
        e,
        HttpStatus.INTERNAL_SERVER_ERROR,
        ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
        e.getMessage());
  }

  protected ResponseEntity<ErrorResponseModel> internalServerError(
      ContextualLogger logger, String message) {
    return buildErrorResponse(
        logger,
        null,
        HttpStatus.INTERNAL_SERVER_ERROR,
        ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
        message);
  }

  private ResponseEntity<ErrorResponseModel> buildErrorResponse(
      ContextualLogger logger, Exception e, HttpStatus status, String errorCode, String message) {
    if (logger != null && e != null) {
      logger.error(e);
    }
    return ResponseEntity.status(status)
        .body(new ErrorResponseModel(errorCode, message, status.value()));
  }
}
