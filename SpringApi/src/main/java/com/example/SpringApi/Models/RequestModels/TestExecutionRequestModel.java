package com.example.SpringApi.Models.RequestModels;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Request model for starting a test execution. Supports multiple execution modes: - Run all tests
 * (runAll = true) - Run all tests for a service (serviceName only) - Run tests for a specific
 * method (serviceName + methodName) - Run specific tests (testNames list)
 */
@Getter
@Setter
public class TestExecutionRequestModel {

  /** If true, runs all tests in the project */
  private Boolean runAll;

  /** Service name to run tests for (e.g., "AddressService") */
  private String serviceName;

  /** Method name to filter tests by (e.g., "toggleAddress") */
  private String methodName;

  /** Specific test method names to run */
  private List<String> testNames;

  /** Test class name (e.g., "AddressServiceTest") */
  private String testClassName;
}

