package com.example.SpringApi.Models.ResponseModels;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

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
