package com.nta.service;

import ch.hsr.geohash.GeoHash;
import com.nta.dto.request.ShipperLocationRequest;
import com.nta.model.ShipperDetailCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.stereotype.Service;
import org.springframework.data.geo.*;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoHashService {
    private final GeoOperations<String, String> geoOperations;
    private final ExternalApiService externalApiService;
    private final String key = "gek4ui";
    private final RedisService redisService;
    @Value("${spring.location.geohashPrecision}")
    private int geohashPrecision; // in second

    public void addShipperLocation(ShipperLocationRequest request) {
        Point point = new Point(request.getLongitude(), request.getLatitude());
        geoOperations.add(key, point, request.getId());
    }

    public void addShipperLocations(Double latitude, Double longitude) {
        String geoHash = getGeohash(latitude,longitude);
        //get all shipper by geohash
        geoOperations.remove(key);
        Map<String,Object> shipper = redisService.getField(geoHash);
        for(String key : shipper.keySet()) {
            ShipperDetailCache s = (ShipperDetailCache) shipper.get(key);
            geoOperations.add(geoHash,new Point(s.getLongitude(),s.getLatitude()),s.getId());
        }
    }

    public String findNearestShipperId(Double latitude, Double longitude, int km) {
        //Init points
        addShipperLocations(latitude,longitude);

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

    public String getGeohash(double latitude, double longitude) {
        GeoHash geoHash = GeoHash.withCharacterPrecision(latitude, longitude, geohashPrecision);
        return geoHash.toBase32();
    }
}
