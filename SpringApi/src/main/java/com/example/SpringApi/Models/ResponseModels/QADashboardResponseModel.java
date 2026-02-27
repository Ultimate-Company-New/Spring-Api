package com.example.SpringApi.Models.ResponseModels;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Combined response model for QA Dashboard. Contains all data needed for the QA Dashboard in a
 * single response.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2026-01-12
 */
@Getter
@Setter
public class QADashboardResponseModel {

  /** List of all services with their methods, tests, and last run information */
  private List<QAResponseModel> services;

  /** Coverage summary statistics */
  private CoverageSummaryData coverageSummary;

  /** List of available service names */
  private List<String> availableServices;

  /** Automated API tests (Playwright/API integration tests) - separate from unit tests */
  private AutomatedApiTestsData automatedApiTests;

  public QADashboardResponseModel() {}

  public QADashboardResponseModel(
      List<QAResponseModel> services,
      CoverageSummaryData coverageSummary,
      List<String> availableServices) {
    this(services, coverageSummary, availableServices, null);
  }

  public QADashboardResponseModel(
      List<QAResponseModel> services,
      CoverageSummaryData coverageSummary,
      List<String> availableServices,
      AutomatedApiTestsData automatedApiTests) {
    this.services = services;
    this.coverageSummary = coverageSummary;
    this.availableServices = availableServices;
    this.automatedApiTests = automatedApiTests;
  }

  /** Coverage summary data */
  @Getter
  @Setter
  public static class CoverageSummaryData {
    private int totalServices;
    private int totalMethods;
    private int totalMethodsWithCoverage;
    private int totalTests;
    private double overallCoveragePercentage;
    private List<ServiceBreakdownData> serviceBreakdown;

    public CoverageSummaryData() {}

    public CoverageSummaryData(
        int totalServices,
        int totalMethods,
        int totalMethodsWithCoverage,
        int totalTests,
        double overallCoveragePercentage,
        List<ServiceBreakdownData> serviceBreakdown) {
      this.totalServices = totalServices;
      this.totalMethods = totalMethods;
      this.totalMethodsWithCoverage = totalMethodsWithCoverage;
      this.totalTests = totalTests;
      this.overallCoveragePercentage = overallCoveragePercentage;
      this.serviceBreakdown = serviceBreakdown;
    }
  }

  /** Per-service breakdown in coverage summary */
  @Getter
  @Setter
  public static class ServiceBreakdownData {
    private String serviceName;
    private int totalMethods;
    private int methodsWithCoverage;
    private int totalTests;
    private double coveragePercentage;

    public ServiceBreakdownData() {}

    public ServiceBreakdownData(
        String serviceName,
        int totalMethods,
        int methodsWithCoverage,
        int totalTests,
        double coveragePercentage) {
      this.serviceName = serviceName;
      this.totalMethods = totalMethods;
      this.methodsWithCoverage = methodsWithCoverage;
      this.totalTests = totalTests;
      this.coveragePercentage = coveragePercentage;
    }
  }

  /**
   * Automated API tests section (Playwright/API integration tests). Separate from unit tests -
   * located in Spring-PlayWright-Automation project.
   */
  @Getter
  @Setter
  public static class AutomatedApiTestsData {
    /**
     * Relative path to the ApiTests folder (e.g.,
     * "../Spring-PlayWright-Automation/src/test/java/com/ultimatecompany/tests/ApiTests")
     */
    private String basePath;

    /** Total count of automated API test classes */
    private int totalTests;

    /** Tests grouped by category (Address, Client, Lead, etc.) */
    private List<AutomatedApiTestCategory> categories;

    public AutomatedApiTestsData() {}

    public AutomatedApiTestsData(
        String basePath, int totalTests, List<AutomatedApiTestCategory> categories) {
      this.basePath = basePath;
      this.totalTests = totalTests;
      this.categories = categories;
    }
  }

  /** A category of automated API tests (e.g., Address, Client, Lead) */
  @Getter
  @Setter
  public static class AutomatedApiTestCategory {
    private String categoryName;

    /** Relative path within ApiTests (e.g., "Address") */
    private String relativePath;

    private List<AutomatedApiTestInfo> tests;

    public AutomatedApiTestCategory() {}

    public AutomatedApiTestCategory(
        String categoryName, String relativePath, List<AutomatedApiTestInfo> tests) {
      this.categoryName = categoryName;
      this.relativePath = relativePath;
      this.tests = tests;
    }
  }

  /** A single automated API test class */
  @Getter
  @Setter
  public static class AutomatedApiTestInfo {
    /** Test class name (e.g., "GetAddressByClientIdTest") */
    private String testClass;

    /** Relative path from ApiTests folder (e.g., "Address/GetAddressByClientIdTest.java") */
    private String relativePath;

    public AutomatedApiTestInfo() {}

    public AutomatedApiTestInfo(String testClass, String relativePath) {
      this.testClass = testClass;
      this.relativePath = relativePath;
    }
  }
}
