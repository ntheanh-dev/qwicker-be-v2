package com.nta.controller;

import com.nta.dto.response.DurationBingMapApiResponse;
import com.nta.dto.response.ApiResponse;
import com.nta.service.ExternalApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {

    private final ExternalApiService externalApiService;

    @GetMapping("/duration")
    ApiResponse<DurationBingMapApiResponse> getDrivingRoute(
            @RequestParam double lat1,
            @RequestParam double long1,
            @RequestParam double lat2,
            @RequestParam double long2
    ) {
        var response = externalApiService.getDurationResponseAsync(lat1,long1,lat2,long2);
        return ApiResponse.<DurationBingMapApiResponse>builder()
                .result(response)
                .build();
    }
}
