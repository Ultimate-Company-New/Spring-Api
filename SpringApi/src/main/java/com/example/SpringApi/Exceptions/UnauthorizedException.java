package com.example.springapi.exceptions;

/**
 * Represents the unauthorized exception component.
 */
public class UnauthorizedException extends RuntimeException {
  public UnauthorizedException(String message) {
    super(message);
  }
}
