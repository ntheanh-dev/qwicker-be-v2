package com.nta.service;

import com.nta.dto.request.ShipperLocationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.stereotype.Service;
import org.springframework.data.geo.*;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoLocationService {
    private final GeoOperations<String, String> geoOperations;
    private final ExternalApiService externalApiService;
    private final String key = "gek4ui";

    @Value("${spring.location.updateLocationTime}")
    private long TTL; // in second

    public void addShipperLocation(ShipperLocationRequest request) {
        Point point = new Point(request.getLongitude(), request.getLatitude());
        geoOperations.add(key, point, request.getId());
    }

    public String findNearestShipperId(Double latitude, Double longitude, int km) {
        Point myPoint = new Point(longitude, latitude);
        Circle within = new Circle(myPoint, new Distance(km, Metrics.KILOMETERS));
        //Query
        RedisGeoCommands.GeoRadiusCommandArgs query = RedisGeoCommands.
                GeoRadiusCommandArgs.
                newGeoRadiusArgs().
                includeCoordinates().
                includeDistance().
                sortAscending().
                limit(4);
        //Response
        GeoResults<RedisGeoCommands.GeoLocation<String>> response = geoOperations.radius(key, within, query);

        //Get list point after query
        assert response != null;
        List<Point> nearLocations = response.getContent().stream().map(m -> m.getContent().getPoint()).toList();
        // Call external API to calculate duration
        int nearestShipperIndexByDuration = externalApiService.getNearestShipperIndex(new Point(longitude,latitude),nearLocations);
        return response.getContent().get(nearestShipperIndexByDuration).getContent().getName();
    }
}
