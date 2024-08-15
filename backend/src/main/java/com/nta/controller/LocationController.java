package com.nta.controller;

import com.nta.service.ExternalApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {

    private final ExternalApiService externalApiService;

    Mono<String> getDrivingRoute(
            @RequestBody double lat1,
            @RequestBody double long1,
            @RequestBody double lat2,
            @RequestBody double long2
    ) {
        return externalApiService.getDurationResponseAsync(lat1,long1,lat2,long2);
    }
}
