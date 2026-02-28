package com.example.springapi.models.databasemodels;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA Entity for the TestRunResult table.
 *
 * <p>Stores individual test execution results including: - Service and method being tested - Test
 * class and method names - Execution status and duration - Error details if failed
 *
 * @author SpringApi Team
 * @version 1.0
 * @since 2026-01-12
 */
@Getter
@Setter
@Entity
@Table(
    name = "TestRunResult",
    indexes = {
      @Index(name = "idx_test_run_result_test_run_id", columnList = "testRunId"),
      @Index(name = "idx_test_run_result_service_name", columnList = "serviceName"),
      @Index(name = "idx_test_run_result_method_name", columnList = "methodName"),
      @Index(name = "idx_test_run_result_test_method_name", columnList = "testMethodName"),
      @Index(name = "idx_test_run_result_status", columnList = "status"),
      @Index(name = "idx_test_run_result_client_id", columnList = "clientId"),
      @Index(name = "idx_test_run_result_executed_at", columnList = "executedAt")
    })
public class TestRunResult {

  // ========================================================================
  // ENUMS
  // ========================================================================

  /** Test result status enum. */
  public enum ResultStatus {
    PASSED("PASSED"),
    FAILED("FAILED"),
    SKIPPED("SKIPPED"),
    ERROR("ERROR");

    private final String value;

    ResultStatus(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    /**
     * Checks whether valid.
     */
    public static boolean isValid(String value) {
      if (value == null) {
        return false;
      }
      for (ResultStatus status : values()) {
        if (status.value.equals(value)) {
          return true;
        }
      }
      return false;
    }
  }

  // ========================================================================
  // FIELDS
  // ========================================================================

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "testRunResultId")
  private Long testRunResultId;

  @Column(name = "testRunId", nullable = false, insertable = false, updatable = false)
  private Long testRunId;

  @Column(name = "serviceName", nullable = false, length = 100)
  private String serviceName;

  @Column(name = "methodName", nullable = false, length = 100)
  private String methodName;

  @Column(name = "testClassName", nullable = false, length = 150)
  private String testClassName;

  @Column(name = "testMethodName", nullable = false, length = 255)
  private String testMethodName;

  @Column(name = "displayName", length = 500)
  private String displayName;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "durationMs", nullable = false)
  private Integer durationMs = 0;

  @Column(name = "errorMessage", columnDefinition = "TEXT")
  private String errorMessage;

  @Column(name = "stackTrace", columnDefinition = "TEXT")
  private String stackTrace;

  @Column(name = "executedAt", nullable = false)
  private LocalDateTime executedAt;

  @Column(name = "clientId", nullable = false)
  private Long clientId;

  // ========================================================================
  // RELATIONSHIPS
  // ========================================================================

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "testRunId", nullable = false)
  private TestRun testRun;

  // ========================================================================
  // CONSTRUCTORS
  // ========================================================================

  public TestRunResult() {
    // Required by JPA.
  }
}
