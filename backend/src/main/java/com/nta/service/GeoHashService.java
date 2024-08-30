package com.nta.service;

import ch.hsr.geohash.GeoHash;
import com.nta.dto.request.ShipperLocationRequest;
import com.nta.enums.ErrorCode;
import com.nta.exception.AppException;
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
        Map<String,Object> shipper = redisService.getField(geoHash);
        //try to find shipper in larger space
        if(shipper == null || shipper.isEmpty()) {
            log.info("---Cannot find shipper in {}", geoHash +", try to find in larger space");
            findNeighbors(geoHash).forEach(gH -> {
                for(String key : redisService.getField(gH).keySet()) {
                    assert shipper != null;
                    ShipperDetailCache s = (ShipperDetailCache) shipper.get(key);
                    geoOperations.add(geoHash,new Point(s.getLongitude(),s.getLatitude()),s.getId());
                }
            });
        } else {
            for(String key : shipper.keySet()) {
                ShipperDetailCache s = (ShipperDetailCache) shipper.get(key);
                geoOperations.add(geoHash,new Point(s.getLongitude(),s.getLatitude()),s.getId());
            }
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
        //Get shippers within radius
        GeoResults<RedisGeoCommands.GeoLocation<String>> response = geoOperations.radius(key, within, query);
        if(response == null || response.getContent().isEmpty()) {
            log.warn("Cannot find any shipper within {} km", km);
            throw new AppException(ErrorCode.CANNOT_FIND_SHIPPER_IN_REDIS);
        }
        List<Point> nearLocations = response.getContent().stream().map(m -> m.getContent().getPoint()).toList();

        // Call external API to calculate duration
        int nearestShipperIndexByDuration = externalApiService.getNearestShipperIndex(new Point(longitude,latitude),nearLocations);
        return response.getContent().get(nearestShipperIndexByDuration).getContent().getName();
    }

    public String getGeohash(double latitude, double longitude) {
        GeoHash geoHash = GeoHash.withCharacterPrecision(latitude, longitude, geohashPrecision);
        return geoHash.toBase32();
    }

    public List<String> findNeighbors(String geohash) {
        // Tạo một đối tượng GeoHash từ chuỗi geohash
        GeoHash geoHash = GeoHash.fromGeohashString(geohash);

        // Lấy các geohash lân cận
        GeoHash north = geoHash.getNorthernNeighbour();
        GeoHash south = geoHash.getSouthernNeighbour();
        GeoHash east = geoHash.getEasternNeighbour();
        GeoHash west = geoHash.getWesternNeighbour();

        return List.of(north.toBase32(), south.toBase32(), east.toBase32(), west.toBase32());
    }
}
