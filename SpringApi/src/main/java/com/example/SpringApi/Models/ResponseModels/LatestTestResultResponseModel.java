package com.example.SpringApi.Models.ResponseModels;

import com.example.SpringApi.Models.DatabaseModels.LatestTestResult;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response model for latest test result data.
 */
@Getter
@Setter
public class LatestTestResultResponseModel {

    private Long latestTestResultId;
    private String serviceName;
    private String testClassName;
    private String testMethodName;
    private String status;
    private Integer durationMs;
    private String errorMessage;
    private String stackTrace;
    private Long lastRunId;
    private Long lastRunByUserId;
    private String lastRunByUserName;
    private LocalDateTime lastRunAt;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    public LatestTestResultResponseModel() {
    }

    public LatestTestResultResponseModel(LatestTestResult result) {
        this.latestTestResultId = result.getLatestTestResultId();
        this.serviceName = result.getServiceName();
        this.testClassName = result.getTestClassName();
        this.testMethodName = result.getTestMethodName();
        this.status = result.getStatus();
        this.durationMs = result.getDurationMs();
        this.errorMessage = result.getErrorMessage();
        this.stackTrace = result.getStackTrace();
        this.lastRunId = result.getLastRunId();
        this.lastRunByUserId = result.getLastRunByUserId();
        this.lastRunByUserName = result.getLastRunByUserName();
        this.lastRunAt = result.getLastRunAt();
        this.createdDate = result.getCreatedDate();
        this.modifiedDate = result.getModifiedDate();
    }
}
