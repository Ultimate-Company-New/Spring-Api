package com.example.SpringApi.Exceptions;

/**
 * Generic application-level runtime exception for use across the codebase. Acts as a single
 * standard unchecked exception to replace ad-hoc RuntimeException usage.
 */
public class ApplicationException extends RuntimeException {
  public ApplicationException(String message) {
    super(message);
  }

  public ApplicationException(String message, Throwable cause) {
    super(message, cause);
  }
}

