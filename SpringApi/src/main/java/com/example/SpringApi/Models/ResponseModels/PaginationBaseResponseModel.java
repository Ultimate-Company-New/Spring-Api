package com.example.springapi.models.responsemodels;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the pagination base response model component.
 */
@Getter
@Setter
public class PaginationBaseResponseModel<T> {
  private List<T> data;
  private long totalDataCount;

  public PaginationBaseResponseModel() {}

  public PaginationBaseResponseModel(List<T> data, long totalDataCount) {
    this.data = data;
    this.totalDataCount = totalDataCount;
  }
}
