package com.nta.service;

import com.nta.dto.response.DurationBingMapApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalApiService {
    private final WebClient.Builder webClientBuilder;

    @Value("${spring.bingmapkey}")
    private String BING_MAP_KEY;

    public DurationBingMapApiResponse getDurationResponseAsync(Double latitude1, Double longitude1, Double latitude2 , Double longitude2
    ) {
        String url = String.format(
                "https://dev.virtualearth.net/REST/v1/Routes/Driving?o=json&wp.0=%.10f,%.10f&wp.1=%.10f,%.10f&key=%s",
                latitude1,longitude1,latitude2,longitude2, BING_MAP_KEY);
        return webClientBuilder.baseUrl(url).build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(DurationBingMapApiResponse.class)
                .block();
    }
}
