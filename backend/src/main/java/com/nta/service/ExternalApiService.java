package com.nta.service;

import com.nta.dto.response.DurationBingMapApiResponse;
import com.nta.enums.ErrorCode;
import com.nta.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalApiService {
    private final WebClient.Builder webClientBuilder;

    @Value("${spring.bingmapkey}")
    private String BING_MAP_KEY;



    public int getNearestShipperIndex(Point myPoint,List<Point> points) {
        try {
            // Chờ cho CompletableFuture hoàn thành và lấy dữ liệu
            List<DurationBingMapApiResponse> responses = fetchBingMapResponsesAsync(myPoint,points).get();
            responses.forEach(r -> log.info("duration: " + r.getResourceSets().getFirst().getResources().getFirst().getTravelDuration()));
            OptionalInt minIndex = IntStream.range(0,responses.size())
                    .reduce(
                            (i,j) -> responses.get(i).getResourceSets().getFirst().getResources().getFirst().getTravelDuration()
                                    < responses.get(j).getResourceSets().getFirst().getResources().getFirst().getTravelDuration()
                                    ? i
                                    : j
                    );
            if (minIndex.isPresent()) {
                log.info(String.valueOf(minIndex.getAsInt()));
                return minIndex.getAsInt();
            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(); // Xử lý ngoại lệ nếu cần
            throw new AppException(ErrorCode.CAN_NOT_CALL_API);
        }
        return 0;
    }

    private CompletableFuture<List<DurationBingMapApiResponse>> fetchBingMapResponsesAsync(Point myPoint, List<Point> points) {
        List<CompletableFuture<DurationBingMapApiResponse>> futures = points
                .stream()
                .map(p -> callBingMapAPI(myPoint,p).toFuture())
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                );
    }

    private Mono<DurationBingMapApiResponse> callBingMapAPI(Point p1, Point p2) {
        String url = String.format(
                "https://dev.virtualearth.net/REST/v1/Routes/Driving?o=json&wp.0=%.10f,%.10f&wp.1=%.10f,%.10f&key=%s",
                p1.getY(),p1.getX(),p2.getY(),p2.getX(), BING_MAP_KEY);
        return webClientBuilder.baseUrl(url).build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(DurationBingMapApiResponse.class);
    }

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
