package com.example.SpringApi.Models.ResponseModels;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Combined response model for QA Dashboard.
 * Contains all data needed for the QA Dashboard in a single response.
 * 
 * @author SpringApi Team
 * @version 1.0
 * @since 2026-01-12
 */
@Getter
@Setter
public class QADashboardResponseModel {
    
    /**
     * List of all services with their methods, tests, and last run information
     */
    private List<QAResponseModel> services;
    
    /**
     * Coverage summary statistics
     */
    private CoverageSummaryData coverageSummary;
    
    /**
     * List of available service names
     */
    private List<String> availableServices;

    public QADashboardResponseModel() {
    }

    public QADashboardResponseModel(List<QAResponseModel> services, 
                                     CoverageSummaryData coverageSummary, 
                                     List<String> availableServices) {
        this.services = services;
        this.coverageSummary = coverageSummary;
        this.availableServices = availableServices;
    }

    /**
     * Coverage summary data
     */
    @Getter
    @Setter
    public static class CoverageSummaryData {
        private int totalServices;
        private int totalMethods;
        private int totalMethodsWithCoverage;
        private int totalTests;
        private double overallCoveragePercentage;
        private List<ServiceBreakdownData> serviceBreakdown;

        public CoverageSummaryData() {
        }
    }

    /**
     * Per-service breakdown in coverage summary
     */
    @Getter
    @Setter
    public static class ServiceBreakdownData {
        private String serviceName;
        private int totalMethods;
        private int methodsWithCoverage;
        private int totalTests;
        private double coveragePercentage;

        public ServiceBreakdownData() {
        }

        public ServiceBreakdownData(String serviceName, int totalMethods, 
                                     int methodsWithCoverage, int totalTests, 
                                     double coveragePercentage) {
            this.serviceName = serviceName;
            this.totalMethods = totalMethods;
            this.methodsWithCoverage = methodsWithCoverage;
            this.totalTests = totalTests;
            this.coveragePercentage = coveragePercentage;
        }
    }
}
