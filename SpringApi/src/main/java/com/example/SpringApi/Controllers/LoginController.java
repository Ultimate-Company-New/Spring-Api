package com.example.SpringApi.Controllers;

import com.example.SpringApi.Logging.ContextualLogger;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Models.ResponseModels.ErrorResponseModel;
import com.example.SpringApi.Services.Interface.ILoginSubTranslator;
import com.example.SpringApi.Models.ApiRoutes;
import com.example.SpringApi.Models.RequestModels.LoginRequestModel;
import com.example.SpringApi.Exceptions.BadRequestException;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.UnauthorizedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling login-related HTTP requests.
 * Provides endpoints for user sign-in, sign-up, password reset, email confirmation, and token generation.
 * Delegates business logic to the ILoginSubTranslator service.
 */
@RestController
@RequestMapping("/api/" + ApiRoutes.ApiControllerNames.LOGIN)
public class LoginController {
    private static final ContextualLogger logger = ContextualLogger.getLogger(LoginController.class);
    private final ILoginSubTranslator loginService;

    @Autowired
    public LoginController(ILoginSubTranslator loginService) {
        this.loginService = loginService;
    }

    /**
     * Endpoint to confirm a user's email address.
     * Accepts a POST request with login details including user ID and confirmation token.
     * Returns 200 OK on success, or appropriate error status on failure.
     *
     * @param loginRequestModel The request body containing user ID and token.
     * @return ResponseEntity with no content on success, or error response on failure.
     */
    @PostMapping("/" + ApiRoutes.LoginSubRoute.CONFIRM_EMAIL)
    public ResponseEntity<?> confirmEmail(@RequestBody LoginRequestModel loginRequestModel) {
        try {
            loginService.confirmEmail(loginRequestModel);
            return ResponseEntity.ok().build();
        } catch (BadRequestException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (UnauthorizedException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (NotFoundException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Endpoint for user sign-in.
     * Accepts a POST request with login credentials (login name and password).
     * Returns a list of clients the user has access to (with logo, name, clientId, and apiKey for each),
     * or appropriate error status on failure.
     *
     * @param loginRequestModel The request body containing login name and password.
     * @return ResponseEntity with list of ClientResponseModel on success, or error response on failure.
     */
    @PostMapping("/" + ApiRoutes.LoginSubRoute.SIGN_IN)
    public ResponseEntity<?> signIn(@RequestBody LoginRequestModel loginRequestModel) {
        try {
            return ResponseEntity.ok(loginService.signIn(loginRequestModel));
        } catch (BadRequestException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (UnauthorizedException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Endpoint to reset a user's password.
     * Accepts a POST request with login name for password reset.
     * Returns true on successful reset, or appropriate error status on failure.
     *
     * @param loginRequestModel The request body containing login name.
     * @return ResponseEntity with boolean result on success, or error response on failure.
     */
    @PostMapping("/" + ApiRoutes.LoginSubRoute.RESET_PASSWORD)
    public ResponseEntity<?> resetPassword(@RequestBody LoginRequestModel loginRequestModel) {
        try {
            return ResponseEntity.ok(loginService.resetPassword(loginRequestModel));
        } catch (BadRequestException | IllegalArgumentException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (IllegalStateException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Endpoint to generate a JWT token for an authenticated user.
     * Accepts a POST request with login name and API key.
     * Returns a JWT token with permissions, or appropriate error status on failure.
     *
     * @param loginRequestModel The request body containing login name and API key.
     * @return ResponseEntity with JWT token on success, or error response on failure.
     */
    @PostMapping("/" + ApiRoutes.LoginSubRoute.GET_TOKEN)
    public ResponseEntity<?> getToken(@RequestBody LoginRequestModel loginRequestModel) {
        try {
            return ResponseEntity.ok(loginService.getToken(loginRequestModel));
        } catch (BadRequestException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (UnauthorizedException e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}