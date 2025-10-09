package com.example.SpringApi.Controllers;

import com.example.SpringApi.Services.UserLogService;
import com.example.SpringApi.Models.Authorizations;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.RequestModels.UserLogsRequestModel;
import com.example.SpringApi.Logging.ContextualLogger;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.UnauthorizedException;
import com.example.SpringApi.Models.ResponseModels.ErrorResponseModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for User Log-related operations.
 *
 * This controller provides endpoints for retrieving user logs in paginated batches.
 * It follows the established pattern with proper error handling, security annotations,
 * and comprehensive documentation.
 *
 * All endpoints are secured with appropriate permission checks and include
 * comprehensive error handling with structured error responses.
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

    /**
     * Retrieves user logs in paginated batches by user ID.
     * Fetches user logs associated with a specific user in a paginated manner.
     * Accepts a request model containing pagination parameters and user ID.
     * Returns a paginated response with user log data.
     *
     * @param userRequestModel The request model containing user ID and pagination options.
     * @return ResponseEntity containing paginated UserLog data or ErrorResponseModel
     */
    @PreAuthorize("@customAuthorization.hasAuthority('" + Authorizations.VIEW_USER_PERMISSION + "')")
    @PostMapping("/" + ApiRoutes.UserLogSubRoute.GET_USER_LOGS_IN_BATCHES_BY_USERID)
    public ResponseEntity<?> fetchUserLogsInBatches(@RequestBody UserLogsRequestModel getUserLogsRequestModel) {
        try {
            return ResponseEntity.ok(userLogService.fetchUserLogsInBatches(getUserLogsRequestModel));
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest().body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException ue) {
            logger.error(ue);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseModel(ErrorMessages.ERROR_UNAUTHORIZED, ue.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, ErrorMessages.ServerError, HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}