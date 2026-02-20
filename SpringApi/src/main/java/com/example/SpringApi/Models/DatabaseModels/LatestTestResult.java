package com.example.SpringApi.Models.DatabaseModels;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * JPA Entity for the LatestTestResult table.
 *
 * <p>Stores the latest result for each unique test (per client). This table is upserted on each
 * test run to maintain current status. Used for quick lookups of current test status without
 * querying history.
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2026-01-12
 */
@Getter
@Setter
@Entity
@Table(
    name = "LatestTestResult",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_latest_test_result",
          columnNames = {"serviceName", "testClassName", "testMethodName", "clientId"})
    },
    indexes = {
      @Index(name = "idx_latest_test_result_service_name", columnList = "serviceName"),
      @Index(name = "idx_latest_test_result_test_method_name", columnList = "testMethodName"),
      @Index(name = "idx_latest_test_result_status", columnList = "status"),
      @Index(name = "idx_latest_test_result_client_id", columnList = "clientId"),
      @Index(name = "idx_latest_test_result_last_run_at", columnList = "lastRunAt"),
      @Index(name = "idx_latest_test_result_last_run_by_user", columnList = "lastRunByUserId")
    })
public class LatestTestResult {

  // ========================================================================
  // ENUMS
  // ========================================================================

  /** Test result status enum (includes NOT_RUN for tests never executed) */
  public enum LatestResultStatus {
    PASSED("PASSED"),
    FAILED("FAILED"),
    SKIPPED("SKIPPED"),
    ERROR("ERROR"),
    NOT_RUN("NOT_RUN");

    private final String value;

    LatestResultStatus(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    public static boolean isValid(String value) {
      if (value == null) return false;
      for (LatestResultStatus status : values()) {
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
  @Column(name = "latestTestResultId")
  private Long latestTestResultId;

  @Column(name = "serviceName", nullable = false, length = 100)
  private String serviceName;

  @Column(name = "testClassName", nullable = false, length = 150)
  private String testClassName;

  @Column(name = "testMethodName", nullable = false, length = 255)
  private String testMethodName;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "durationMs", nullable = false)
  private Integer durationMs = 0;

  @Column(name = "errorMessage", columnDefinition = "TEXT")
  private String errorMessage;

  @Column(name = "stackTrace", columnDefinition = "TEXT")
  private String stackTrace;

  @Column(name = "lastRunId")
  private Long lastRunId;

  @Column(name = "lastRunByUserId", nullable = false)
  private Long lastRunByUserId;

  @Column(name = "lastRunByUserName", nullable = false, length = 255)
  private String lastRunByUserName;

  @Column(name = "lastRunAt", nullable = false)
  private LocalDateTime lastRunAt;

  @Column(name = "clientId", nullable = false)
  private Long clientId;

  @CreationTimestamp
  @Column(name = "createdDate", nullable = false, updatable = false)
  private LocalDateTime createdDate;

  @UpdateTimestamp
  @Column(name = "modifiedDate", nullable = false)
  private LocalDateTime modifiedDate;

  // ========================================================================
  // CONSTRUCTORS
  // ========================================================================

  public LatestTestResult() {
    // Required by JPA.
  }

  // ========================================================================
  // HELPER METHODS
  // ========================================================================

  /** Updates this record with new test result data. */
  public void updateFromResult(TestRunResult result, Long testRunId, Long userId, String userName) {
    this.status = result.getStatus();
    this.durationMs = result.getDurationMs();
    this.errorMessage = result.getErrorMessage();
    this.stackTrace = result.getStackTrace();
    this.lastRunId = testRunId;
    this.lastRunByUserId = userId;
    this.lastRunByUserName = userName;
    this.lastRunAt = LocalDateTime.now();
  }
}
