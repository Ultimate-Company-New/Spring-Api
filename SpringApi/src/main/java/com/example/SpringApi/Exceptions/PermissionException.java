package com.example.SpringApi.Exceptions;

public class PermissionException extends RuntimeException {

  public PermissionException(String message) {
    super(message);
  }
}

