package com.example.SpringApi.Models.ResponseModels;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PaginationBaseResponseModel<T> {
    private List<T> data;
    private long totalDataCount;
}
