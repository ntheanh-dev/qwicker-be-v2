package com.nta.service.websocker;

import com.nta.enums.ErrorCode;
import com.nta.exception.AppException;
import com.nta.model.LocationMessage;
import com.nta.model.ShipperDetailCache;
import com.nta.service.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationService {
    OnlineOfflineService onlineOfflineService;
    AuthenticationService authenticationService;
    GeoHashService geoHashService;
    RedisService redisService;
    ShipperService shipperService;
    private final UserService userService;

    public void updateLocation(Double oldLatitude, Double oldLongitude,Double newLatitude,Double newLongitude) {
        var userDetail = authenticationService.getAuthenticatedUserDetail();
        String oldGeoHash = geoHashService.getGeohash(oldLatitude,oldLongitude);
        String newGeoHash = geoHashService.getGeohash(newLatitude,newLongitude);
        //----------Delete previous location-------------
        redisService.delete(oldGeoHash,userDetail.getId());
        //----------Add new location---------------------
        var vehicle = shipperService.getVehicleByUserId(userDetail.getId()).orElseThrow(
                () -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));
        redisService.hashSet(newGeoHash, userDetail.getId(),
                ShipperDetailCache
                        .builder()
                        .id(userDetail.getId())
                        .vehicleType(vehicle.getId())
                        .ts(LocalDateTime.now())
                        .latitude(newLatitude)
                        .longitude(newLongitude)
                        .build()
        );
    }

    public void addLocation(
            LocationMessage locationMessage, SimpMessageHeaderAccessor headerAccessor
    ) {
        var userDetail = authenticationService.getAuthenticatedUserDetail();
        String newGeoHash = geoHashService.getGeohash(locationMessage.getLatitude(), locationMessage.getLongitude());
        String oldGeoHash = geoHashService.getGeohash(locationMessage.getPrevLatitude(), locationMessage.getPrevLongitude());

        //--------Delete Old Location----------------
        redisService.delete(oldGeoHash, userDetail.getId());
        var vehicle = shipperService.getVehicleByUserId(userDetail.getId()).orElseThrow(
                () -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));
        //--------Add New Location-------------------
        redisService.hashSet(newGeoHash, userDetail.getId(),
                ShipperDetailCache
                        .builder()
                        .id(userDetail.getId())
                        .vehicleType(vehicle.getId())
                        .ts(LocalDateTime.now())
                        .latitude(locationMessage.getLatitude())
                        .longitude(locationMessage.getLongitude())
        );
    }
}
