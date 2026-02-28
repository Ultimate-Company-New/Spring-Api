package springapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springapi.exceptions.BadRequestException;
import springapi.exceptions.NotFoundException;
import springapi.exceptions.UnauthorizedException;
import springapi.logging.ContextualLogger;
import springapi.models.ApiRoutes;
import springapi.models.Authorizations;
import springapi.models.requestmodels.UserLogsRequestModel;
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
public class UserLogController extends BaseController {

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
      return badRequest(logger, bre);
    } catch (NotFoundException nfe) {
      return notFound(logger, nfe);
    } catch (UnauthorizedException ue) {
      return unauthorized(logger, ue);
    } catch (Exception e) {
      return internalServerError(logger, e);
    }
  }
}
