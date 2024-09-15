package com.nta.service;

import ch.hsr.geohash.GeoHash;
import com.nta.constant.RedisKey;
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

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeoHashService {
  private final GeoOperations<String, String> geoOperations;
  private final ExternalApiService externalApiService;
  private final RedisService redisService;

  @Value("${spring.location.geohashPrecision}")
  private int geohashPrecision; // in second

  // Object o day la ShipperDetailCache
  public Map<String, Object> getShippersDetailCacheByGeoHash(
      final double latitude, final double longitude) {
    final String geoHash = getGeohash(latitude, longitude);
    log.info("Getting shippers in geohash: {}", geoHash);
    final Map<String, Object> shippersInGeoHash = redisService.getField(geoHash);
    if (!shippersInGeoHash.keySet().isEmpty()) {
      for (final Map.Entry<String, Object> entry : shippersInGeoHash.entrySet()) {
        final String key = entry.getKey();
        log.info("Found shipper: {}", key);
      }
      return shippersInGeoHash;
    }
    // try to find shipper in larger space
    final Map<String, Object> result = new HashMap<>();
    log.info("---Cannot find shipper in {}", geoHash + ", try to find in larger space");
    findNeighbors(geoHash)
        .forEach(
            neighBorHash -> {
              log.info("Getting shippers in neighBorHash geohash: {}", neighBorHash);
              result.putAll(redisService.getField(neighBorHash));
            });
    return result;
  }

  private void initGeoOperationPoint(
      final List<String> shipperIds,
      final String keyMember,
      final Double latitude,
      final Double longitude) {
    getShippersDetailCacheByGeoHash(latitude, longitude)
        .values()
        .forEach(
            s -> {
              ShipperDetailCache shipperDetailCache = (ShipperDetailCache) s;
              if (shipperIds.contains(((ShipperDetailCache) s).getId())) {
                geoOperations.add(
                    RedisKey.SHIPPER_LOCATION,
                    new Point(shipperDetailCache.getLongitude(), shipperDetailCache.getLatitude()),
                    keyMember);
              }
            });
  }

  public String findNearestShipperId(
      final List<String> shipperIds,
      final String keyMember,
      final Double latitude,
      final Double longitude,
      final int km) {
    Point myPoint = new Point(longitude, latitude);
    Circle within = new Circle(myPoint, new Distance(km, Metrics.KILOMETERS));
    // Find shipper's location in redis
    initGeoOperationPoint(shipperIds, keyMember, latitude, longitude);
    //

    // Query
    RedisGeoCommands.GeoRadiusCommandArgs query =
        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
            .includeCoordinates()
            .includeDistance()
            .sortAscending()
            .limit(4);
    // Get shippers within radius
    GeoResults<RedisGeoCommands.GeoLocation<String>> response =
        geoOperations.radius(RedisKey.SHIPPER_LOCATION, within, query);
    if (response == null || response.getContent().isEmpty()) {
      log.warn("Cannot find any shipper within {} km", km);
      throw new AppException(ErrorCode.CANNOT_FIND_SHIPPER_IN_REDIS);
    }
    List<Point> nearLocations =
        response.getContent().stream().map(m -> m.getContent().getPoint()).toList();

    // Call external API to calculate duration
    int nearestShipperIndexByDuration =
        externalApiService.getNearestShipperIndex(new Point(longitude, latitude), nearLocations);
    return response.getContent().get(nearestShipperIndexByDuration).getContent().getName();
  }

  public String getGeohash(final double latitude,final double longitude) {
    final GeoHash geoHash = GeoHash.withCharacterPrecision(latitude, longitude, geohashPrecision);
    return geoHash.toBase32();
  }

  public List<String> findNeighbors(final String geohashStr) {
    // Tạo một đối tượng GeoHash từ chuỗi geohash
    final GeoHash geoHash = GeoHash.fromGeohashString(geohashStr);
    final List<String> result = new ArrayList<>();
    // Lấy các geohash lân cận
    Arrays.stream(geoHash.getAdjacent()).toList().forEach(g -> result.add(g.toBase32()));
    return result;
  }
}
