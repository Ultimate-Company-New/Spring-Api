package com.example.SpringApi.Controllers;

import com.example.SpringApi.Services.Interface.IQASubTranslator;
import com.example.SpringApi.Models.RequestModels.TestRunRequestModel;
import com.example.SpringApi.Models.RequestModels.TestExecutionRequestModel;
import com.example.SpringApi.Models.ResponseModels.ErrorResponseModel;
import com.example.SpringApi.Models.ResponseModels.TestExecutionStatusModel;
import com.example.SpringApi.ErrorMessages;
import com.example.SpringApi.Logging.ContextualLogger;
import com.example.SpringApi.Exceptions.NotFoundException;
import com.example.SpringApi.Exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Quality Assurance endpoint-to-test mapping.
 * 
 * This controller provides endpoints that return comprehensive information about
 * all public service methods and their associated unit tests. This helps QA teams
 * understand test coverage and quickly identify which tests validate specific endpoints.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2024-01-15
 */
@RestController
@RequestMapping("/api/QA")
public class QAController {
    private static final ContextualLogger logger = ContextualLogger.getLogger(QAController.class);
    private final IQASubTranslator qaService;

    @Autowired
    public QAController(IQASubTranslator qaService) {
        this.qaService = qaService;
    }

    /**
     * Returns all QA dashboard data in a single response.
     * Includes:
     * - List of all services with their methods, tests, and last run information
     * - Coverage summary statistics
     * - List of available service names
     * 
     * @return ResponseEntity containing QADashboardResponseModel with all dashboard data
     */
    @GetMapping("/getDashboardData")
    public ResponseEntity<?> getDashboardData() {
        try {
            return ResponseEntity.ok(qaService.getDashboardData());
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest()
                .body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Saves a test run with its individual results.
     * Also updates the LatestTestResult table for each test.
     * 
     * @param request The test run request containing service info and results
     * @return ResponseEntity containing TestRunResponseModel with the saved test run data
     */
    @PutMapping("/saveTestRun")
    public ResponseEntity<?> saveTestRun(@RequestBody TestRunRequestModel request) {
        try {
            return ResponseEntity.ok(qaService.saveTestRun(request));
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest()
                .body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    // ==================== TEST EXECUTION ENDPOINT ====================

    /**
     * Unified test execution endpoint.
     * 
     * PUT: Start a new test execution (async). Returns executionId for polling.
     * GET: Get status/progress of an execution by ID.
     * 
     * Request body determines scope:
     * - { "runAll": true } - Run ALL tests in the API
     * - { "serviceName": "AddressService" } - Run all tests for that service
     * - { "serviceName": "AddressService", "methodName": "toggleAddress" } - Run tests for specific method
     * - { "testNames": ["test1", "test2"], "testClassName": "AddressServiceTest" } - Run specific tests
     * 
     * @param request The test execution request (determines what to run)
     * @return ResponseEntity with executionId and initial status
     */
    @PutMapping("/runTests")
    public ResponseEntity<?> runTests(@RequestBody TestExecutionRequestModel request) {
        try {
            TestExecutionStatusModel status = qaService.startTestExecution(request);
            return ResponseEntity.ok(status);
        } catch (BadRequestException bre) {
            logger.error(bre);
            return ResponseEntity.badRequest()
                .body(new ErrorResponseModel(ErrorMessages.ERROR_BAD_REQUEST, bre.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Get test execution status and progress.
     * Poll this endpoint to track real-time test progress.
     * 
     * @param executionId The execution ID from runTests response
     * @return TestExecutionStatusModel with current status, progress, and results
     */
    @GetMapping("/runTests/{executionId}")
    public ResponseEntity<?> getTestExecutionStatus(@PathVariable String executionId) {
        try {
            TestExecutionStatusModel status = qaService.getTestExecutionStatus(executionId);
            return ResponseEntity.ok(status);
        } catch (NotFoundException nfe) {
            logger.error(nfe);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_NOT_FOUND, nfe.getMessage(), HttpStatus.NOT_FOUND.value()));
        } catch (Exception e) {
            logger.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseModel(ErrorMessages.ERROR_INTERNAL_SERVER_ERROR, e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}
