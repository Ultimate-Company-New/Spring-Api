package com.example.springapi.exceptions;

/**
 * Represents the bad request exception component.
 */
public class BadRequestException extends RuntimeException {
  public BadRequestException(String message) {
    super(message);
  }
}
