package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * JPA Entity for the TestRun table.
 *
 * <p>Tracks batch/service test run execution metadata including: - Who ran the tests - When they
 * were run - Total counts (passed, failed, skipped) - Run type (single method, service, all
 * services)
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2026-01-12
 */
@Getter
@Setter
@Entity
@Table(
    name = "TestRun",
    indexes = {
      @Index(name = "idx_test_run_service_name", columnList = "serviceName"),
      @Index(name = "idx_test_run_user_id", columnList = "userId"),
      @Index(name = "idx_test_run_client_id", columnList = "clientId"),
      @Index(name = "idx_test_run_status", columnList = "status"),
      @Index(name = "idx_test_run_created_date", columnList = "createdDate")
    })
public class TestRun {

  // ========================================================================
  // ENUMS
  // ========================================================================

  /** Run type enum */
  public enum RunType {
    SINGLE_METHOD("SINGLE_METHOD"),
    SERVICE("SERVICE"),
    ALL_SERVICES("ALL_SERVICES");

    private final String value;

    RunType(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    public static boolean isValid(String value) {
      if (value == null) return false;
      for (RunType type : values()) {
        if (type.value.equals(value)) return true;
      }
      return false;
    }
  }

  /** Test run status enum */
  public enum TestRunStatus {
    RUNNING("RUNNING"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED"),
    CANCELLED("CANCELLED");

    private final String value;

    TestRunStatus(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    public static boolean isValid(String value) {
      if (value == null) return false;
      for (TestRunStatus status : values()) {
        if (status.value.equals(value)) return true;
      }
      return false;
    }
  }

  // ========================================================================
  // FIELDS
  // ========================================================================

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "testRunId")
  private Long testRunId;

  @Column(name = "serviceName", nullable = false, length = 100)
  private String serviceName;

  @Column(name = "runType", nullable = false, length = 50)
  private String runType;

  @Column(name = "userId", nullable = false)
  private Long userId;

  @Column(name = "userName", nullable = false, length = 255)
  private String userName;

  @Column(name = "environment", nullable = false, length = 50)
  private String environment = "localhost";

  @Column(name = "startTime", nullable = false)
  private LocalDateTime startTime;

  @Column(name = "endTime")
  private LocalDateTime endTime;

  @Column(name = "totalTests", nullable = false)
  private Integer totalTests = 0;

  @Column(name = "passedCount", nullable = false)
  private Integer passedCount = 0;

  @Column(name = "failedCount", nullable = false)
  private Integer failedCount = 0;

  @Column(name = "skippedCount", nullable = false)
  private Integer skippedCount = 0;

  @Column(name = "status", nullable = false, length = 20)
  private String status = TestRunStatus.RUNNING.getValue();

  @Column(name = "clientId", nullable = false)
  private Long clientId;

  @CreationTimestamp
  @Column(name = "createdDate", nullable = false, updatable = false)
  private LocalDateTime createdDate;

  // ========================================================================
  // RELATIONSHIPS
  // ========================================================================

  @OneToMany(
      mappedBy = "testRun",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<TestRunResult> results = new ArrayList<>();

  // ========================================================================
  // CONSTRUCTORS
  // ========================================================================

  public TestRun() {}

  public TestRun(String serviceName, String runType, Long userId, String userName, Long clientId) {
    this.serviceName = serviceName;
    this.runType = runType;
    this.userId = userId;
    this.userName = userName;
    this.clientId = clientId;
    this.startTime = LocalDateTime.now();
    this.status = TestRunStatus.RUNNING.getValue();
  }

  // ========================================================================
  // HELPER METHODS
  // ========================================================================

  /** Marks the test run as completed and sets the end time. */
  public void complete() {
    this.endTime = LocalDateTime.now();
    this.status =
        this.failedCount > 0 ? TestRunStatus.FAILED.getValue() : TestRunStatus.COMPLETED.getValue();
  }

  /** Adds a result to this test run and updates counts. */
  public void addResult(TestRunResult result) {
    result.setTestRun(this);
    this.results.add(result);
    this.totalTests++;

    String resultStatus = result.getStatus();
    if (resultStatus == null) {
      return;
    }

    switch (resultStatus) {
      case "PASSED":
        this.passedCount++;
        break;
      case "FAILED", "ERROR":
        this.failedCount++;
        break;
      case "SKIPPED":
        this.skippedCount++;
        break;
      default:
        break;
    }
  }
}
