package com.nta.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalApiService {
    private final WebClient.Builder webClientBuilder;

    @Value("${spring.bingmapkey}")
    private final String BING_MAP_KEY;

    /*
     * chạy bất đồng bộ (chương trình tiếp tục thực hiện các dòng code tiếp theo mà không phải chờ cho đến khi nhận response
     * khi gọi api)
     * @id: tham số truyền vào rest api
     * @onRestOperationException: consumer xử lý khi có exception RestOperationException (Exception theo logic nghiệp vụ tự
     * handler)
     * @onException: consumer xử lý khi có exception chung
     */
    public Mono<String> getDurationResponseAsync(Double longitude1, Double latitude1, Double longitude2, Double latitude2
                                                 ) {
        Map<String, Object> params = new HashMap<>();
        params.put("o", "json");
        params.put("wp.0", String.format("%.8f,%.5f", latitude1, longitude1));
        params.put("wp.1", String.format("%.8f,%.5f", latitude2, longitude2));
        params.put("key", BING_MAP_KEY);
        Mono<String> response = webClientBuilder.baseUrl("https://dev.virtualearth.net/REST/v1/Routes/Driving").build()
                .get()
                .uri(uriBuilder -> {
                    URI uri = uriBuilder.build(params);
                    log.info("With Endpoint {}", uri);
                    return uri;
                })
                .retrieve()
//                .onStatus(
//                        httpStatus -> HttpStatus.BAD_REQUEST.equals(httpStatus), clientResponse ->{
//                            return clientResponse.bodyToMono(ExceptionDto.class)
//                                    .flatMap(exceptionDto -> {
//                                        log.error("id: {} error {}", id, gson.toJson(exceptionDto));
//                                        // Xử lý theo logic nghiệp vụ
//                                        return Mono.error(new RestOperationException(Integer.valueOf(exceptionDto.getCode()), gson.toJson(exceptionDto)));
//                                    });
//                        }
//                )
                .bodyToMono(String.class);
//                .doOnError(RestOperationException.class, onRestOperationException)
//                .doOnError(Exception.class, onException);
        return response;
    }
}
