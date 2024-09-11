package com.nta.controller;

import com.nta.dto.request.StatisticIncomeRequest;
import com.nta.dto.response.ApiResponse;
import com.nta.dto.response.StatisticIncomeResponse;
import com.nta.service.ShipperService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/statistic")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatisticController {

  ShipperService shipperService;

  @PostMapping()
  public ApiResponse<List<StatisticIncomeResponse>> getStatistic(@RequestBody StatisticIncomeRequest request) {
    final var response = shipperService.getStatistics(request);
    return ApiResponse.<List<StatisticIncomeResponse>>builder().result(response).build();
  }
}
