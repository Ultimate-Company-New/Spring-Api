package com.example.springapi.controllers;

import com.example.springapi.ErrorMessages;
import com.example.springapi.exceptions.BadRequestException;
import com.example.springapi.exceptions.NotFoundException;
import com.example.springapi.exceptions.UnauthorizedException;
import com.example.springapi.logging.ContextualLogger;
import com.example.springapi.models.requestmodels.TestExecutionRequestModel;
import com.example.springapi.models.requestmodels.TestRunRequestModel;
import com.example.springapi.models.responsemodels.ErrorResponseModel;
import com.example.springapi.models.responsemodels.TestExecutionStatusModel;
import com.example.springapi.services.interfaces.QaSubTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Quality Assurance endpoint-to-test mapping.
 *
 * <p>This controller provides endpoints that return comprehensive information about all public
 * service methods and their associated unit tests. This helps QA teams understand test coverage and
 * quickly identify which tests validate specific endpoints.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/QA")
public class QaController {
  private static final ContextualLogger logger = ContextualLogger.getLogger(QaController.class);
  private final QaSubTranslator qaService;

  @Autowired
  public QaController(QaSubTranslator qaService) {
    this.qaService = qaService;
  }

  /**
   * Returns all QA dashboard data in a single response. Includes: - List of all services with
   * their. methods, tests, and last run information - Coverage summary statistics - List of
   * available service names
   *
   * @return ResponseEntity containing QaDashboardResponseModel with all dashboard data
   */
  @GetMapping("/getDashboardData")
  public ResponseEntity<?> getDashboardData() {
    try {
      return ResponseEntity.ok(qaService.getDashboardData());
    } catch (UnauthorizedException ue) {
      logger.error(ue);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  ue.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
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
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Returns a comprehensive list of all services with their public methods and associated unit.
   * tests.
   *
   * @return ResponseEntity containing list of QaResponseModel with full endpoint-to-test mapping
   */
  @GetMapping("/getAllEndpointsWithTests")
  public ResponseEntity<?> getAllEndpointsWithTests() {
    try {
      return ResponseEntity.ok(qaService.getAllEndpointsWithTests());
    } catch (UnauthorizedException ue) {
      logger.error(ue);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  ue.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
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
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Returns endpoint-to-test mapping for a specific service.
   *
   * @param serviceName The name of the service (e.g., "AddressService", "UserService", or just
   *     "Address")
   * @return ResponseEntity containing QaResponseModel for the specified service
   */
  @GetMapping("/getEndpointsWithTestsByService")
  public ResponseEntity<?> getEndpointsWithTestsByService(@RequestParam String serviceName) {
    try {
      return ResponseEntity.ok(qaService.getEndpointsWithTestsByService(serviceName));
    } catch (UnauthorizedException ue) {
      logger.error(ue);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  ue.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
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
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Returns a summary of test coverage across all services.
   *
   * @return ResponseEntity containing coverage summary map
   */
  @GetMapping("/getCoverageSummary")
  public ResponseEntity<?> getCoverageSummary() {
    try {
      return ResponseEntity.ok(qaService.getCoverageSummary());
    } catch (UnauthorizedException ue) {
      logger.error(ue);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  ue.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
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
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Returns a list of available service names.
   *
   * @return ResponseEntity containing list of service names
   */
  @GetMapping("/getAvailableServices")
  public ResponseEntity<?> getAvailableServices() {
    try {
      return ResponseEntity.ok(qaService.getAvailableServices());
    } catch (UnauthorizedException ue) {
      logger.error(ue);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  ue.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
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
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Returns the latest test results for a service or all services.
   *
   * @param serviceName The service name to filter by (null or empty for all services)
   * @return ResponseEntity containing list of LatestTestResultResponseModel
   */
  @GetMapping("/getLatestTestResults")
  public ResponseEntity<?> getLatestTestResults(
      @RequestParam(required = false) String serviceName) {
    try {
      return ResponseEntity.ok(qaService.getLatestTestResults(serviceName));
    } catch (UnauthorizedException ue) {
      logger.error(ue);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  ue.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
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
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Saves a test run with its individual results. Also updates the LatestTestResult table for each.
   * test.
   *
   * @param request The test run request containing service info and results
   * @return ResponseEntity containing TestRunResponseModel with the saved test run data
   */
  @PutMapping("/saveTestRun")
  public ResponseEntity<?> saveTestRun(@RequestBody TestRunRequestModel request) {
    try {
      return ResponseEntity.ok(qaService.saveTestRun(request));
    } catch (UnauthorizedException ue) {
      logger.error(ue);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  ue.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
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
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  // ==================== TEST EXECUTION ENDPOINT ====================

  /**
   * Unified test execution endpoint.
   *
   * <p>PUT: Start a new test execution (async). Returns executionId for polling. GET: Get
   * status/progress of an execution by ID.
   *
   * <p>Request body determines scope: - { "runAll": true } - Run ALL tests in the API - {
   * "serviceName": "AddressService" } - Run all tests for that service - { "serviceName":
   * "AddressService", "methodName": "toggleAddress" } - Run tests for specific method - {
   * "testNames": ["test1", "test2"], "testClassName": "AddressServiceTest" } - Run specific tests
   *
   * @param request The test execution request (determines what to run)
   * @return ResponseEntity with executionId and initial status
   */
  @PutMapping("/runTests")
  public ResponseEntity<?> runTests(@RequestBody TestExecutionRequestModel request) {
    try {
      TestExecutionStatusModel status = qaService.startTestExecution(request);
      return ResponseEntity.ok(status);
    } catch (UnauthorizedException ue) {
      logger.error(ue);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  ue.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (BadRequestException bre) {
      logger.error(bre);
      return ResponseEntity.badRequest()
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_BAD_REQUEST,
                  bre.getMessage(),
                  HttpStatus.BAD_REQUEST.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }

  /**
   * Get test execution status and progress. Poll this endpoint to track real-time test progress.
   *
   * @param executionId The execution ID from runTests response
   * @return TestExecutionStatusModel with current status, progress, and results
   */
  @GetMapping("/runTests/{executionId}")
  public ResponseEntity<?> getTestExecutionStatus(@PathVariable String executionId) {
    try {
      TestExecutionStatusModel status = qaService.getTestExecutionStatus(executionId);
      return ResponseEntity.ok(status);
    } catch (UnauthorizedException ue) {
      logger.error(ue);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_UNAUTHORIZED,
                  ue.getMessage(),
                  HttpStatus.UNAUTHORIZED.value()));
    } catch (NotFoundException nfe) {
      logger.error(nfe);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
    } catch (Exception e) {
      logger.error(e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              new ErrorResponseModel(
                  ErrorMessages.ERROR_INTERNAL_SERVER_ERROR,
                  e.getMessage(),
                  HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
  }
}
