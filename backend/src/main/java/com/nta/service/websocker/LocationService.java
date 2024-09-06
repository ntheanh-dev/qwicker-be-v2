package com.nta.service.websocker;

import com.nta.dto.request.UpdateShipperLocationRequest;
import com.nta.enums.ErrorCode;
import com.nta.exception.AppException;
import com.nta.model.ShipperDetailCache;
import com.nta.service.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class LocationService {
  OnlineOfflineService onlineOfflineService;
  AuthenticationService authenticationService;
  GeoHashService geoHashService;
  RedisService redisService;
  ShipperService shipperService;

  public void updateLocation(
      final UpdateShipperLocationRequest locationMessage, final Principal p) {
    final var userDetail = authenticationService.getUserDetail(p);
    // Id in this object added when generate token (userId) not is shipperId
    if (onlineOfflineService.isUserOnline(userDetail.getId())) {
      final String newGeoHash =
          geoHashService.getGeohash(locationMessage.getLatitude(), locationMessage.getLongitude());
      // --------Delete Old Location if it present ----------------
      if (locationMessage.getPrevLatitude() != 0.0 && locationMessage.getLongitude() != 0.0) {
        final String oldGeoHash =
            geoHashService.getGeohash(
                locationMessage.getPrevLatitude(), locationMessage.getPrevLongitude());
        redisService.delete(oldGeoHash, locationMessage.getShipperId());
      }
      final var vehicle =
          shipperService
              .getVehicleByUserId(userDetail.getId())
              .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));
      // --------Update Location-------------------
      if (redisService.isRedisLive()) {
        redisService.hashSet(
            newGeoHash,
            locationMessage.getShipperId(),
            ShipperDetailCache.builder()
                .id(locationMessage.getShipperId())
                .vehicleType(vehicle.getId())
                .ts(LocalDateTime.now())
                .latitude(locationMessage.getLatitude())
                .longitude(locationMessage.getLongitude())
                .build());
      } else {
        log.warn("Redis server is offline, cannot update shipper location");
      }
    }
  }
}
