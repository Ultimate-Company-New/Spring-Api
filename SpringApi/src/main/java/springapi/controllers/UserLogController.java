package springapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springapi.ErrorMessages;
import springapi.exceptions.BadRequestException;
import springapi.exceptions.NotFoundException;
import springapi.exceptions.UnauthorizedException;
import springapi.logging.ContextualLogger;
import springapi.models.ApiRoutes;
import springapi.models.Authorizations;
import springapi.models.requestmodels.UserLogsRequestModel;
import springapi.models.responsemodels.ErrorResponseModel;
import springapi.services.UserLogService;

/**
 * REST Controller for User Log-related operations.
 *
 * <p>This controller provides endpoints for retrieving user logs in paginated batches. It follows
 * the established pattern with proper error handling, security annotations, and comprehensive
 * documentation.
 *
 * <p>All endpoints are secured with appropriate permission checks and include comprehensive error
 * handling with structured error responses.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.USERLOG)
public class UserLogController {

  private final UserLogService userLogService;
  private final ContextualLogger logger;

  @Autowired
  public UserLogController(UserLogService userLogService) {
    this.userLogService = userLogService;
    this.logger = ContextualLogger.getLogger(UserLogController.class);
  }

  /** Fetches user logs in batches. */
  @PreAuthorize("@customAuthorization.hasAuthority('" + Authorizations.VIEW_USER_PERMISSION + "')")
  @PostMapping("/" + ApiRoutes.UserLogSubRoute.GET_USER_LOGS_IN_BATCHES_BY_USERID)
  public ResponseEntity<?> fetchUserLogsInBatches(
      @RequestBody UserLogsRequestModel getUserLogsRequestModel) {
    try {
      return ResponseEntity.ok(userLogService.fetchUserLogsInBatches(getUserLogsRequestModel));
    } catch (BadRequestException bre) {
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (NotFoundException nfe) {
      logger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (UnauthorizedException ue) {
      logger.error(ue);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  ue.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  ErrorMessages.SERVER_ERROR,
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }
}
