package com.example.SpringApi.Models.ResponseModels;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponseModel {
  private String error;
  private String message;
  private int status;
  private String timestamp;
  private String path;

  public ErrorResponseModel() {
    this.timestamp = java.time.Instant.now().toString();
  }

  public ErrorResponseModel(String error, String message, int status) {
    this();
    this.error = error;
    this.message = message;
    this.status = status;
  }

  public ErrorResponseModel(String error, String message, int status, String path) {
    this(error, message, status);
    this.path = path;
  }
}
