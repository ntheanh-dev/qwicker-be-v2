package com.nta.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.stereotype.Service;
import org.springframework.data.geo.*;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class GeoLocationService {
    private final GeoOperations<String, String> geoOperations;
    private final String key = "gek4ui";
    public void add() {
        Point point = new Point(3423.23423,23423.4545);

    }

    public void findNearestShipper(Double longitude,Double latitude, int km) {
        Circle circle = new Circle(new Point(longitude, latitude), new Distance(km, Metrics.KILOMETERS));

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeCoordinates()
                .includeDistance().sortAscending().limit(10);

        var vehicleLocationResponses = new ArrayList<>();
        GeoResults<RedisGeoCommands.GeoLocation<String>> response = geoOperations.radius(key, circle, args);
        response.getContent().stream().forEach(data -> {

//            vehicleLocationResponses.add(VehicleLocationResponse.builder()
//                    .vehicleName(data.getContent().getName())
//                    .averageDistance(data.getDistance().toString())
//                    .point(data.getContent().getPoint())
//                    .hash(geoOperations.hash(key,data.getContent().getName()).stream().findFirst().get())
//                    .build());

        });
    }
}
