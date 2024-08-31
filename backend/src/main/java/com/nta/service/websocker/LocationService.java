package com.nta.service.websocker;

import com.nta.enums.ErrorCode;
import com.nta.exception.AppException;
import com.nta.model.LocationMessage;
import com.nta.model.ShipperDetailCache;
import com.nta.service.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserService userService;

    public void updateLocation(Double oldLatitude, Double oldLongitude, Double newLatitude, Double newLongitude) {
//        var userDetail = authenticationService.getAuthenticatedUserDetail();
//        String oldGeoHash = geoHashService.getGeohash(oldLatitude,oldLongitude);
//        String newGeoHash = geoHashService.getGeohash(newLatitude,newLongitude);
//        //----------Delete previous location-------------
//        redisService.delete(oldGeoHash,userDetail.getId());
//        //----------Add new location---------------------
//        var vehicle = shipperService.getVehicleByUserId(userDetail.getId()).orElseThrow(
//                () -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));
//        redisService.hashSet(newGeoHash, userDetail.getId(),
//                ShipperDetailCache
//                        .builder()
//                        .id(userDetail.getId())
//                        .vehicleType(vehicle.getId())
//                        .ts(LocalDateTime.now())
//                        .latitude(newLatitude)
//                        .longitude(newLongitude)
//                        .build()
//        );
    }

    public void updateLocation(
            LocationMessage locationMessage, Principal p
    ) {
        var userDetail = authenticationService.getUserDetail(p);
        if(onlineOfflineService.isUserOnline(userDetail.getId())) {
            String newGeoHash = geoHashService.getGeohash(locationMessage.getLatitude(), locationMessage.getLongitude());
            //--------Delete Old Location if it present ----------------
            if (locationMessage.getPrevLatitude() != 0.0 && locationMessage.getLongitude() != 0.0) {
                String oldGeoHash = geoHashService.getGeohash(locationMessage.getPrevLatitude(), locationMessage.getPrevLongitude());
                redisService.delete(oldGeoHash, locationMessage.getUserId());
            }
            var vehicle = shipperService.getVehicleByUserId(userDetail.getId()).orElseThrow(
                    () -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));
            log.info("update location");
            //--------Update Location-------------------
            redisService.hashSet(newGeoHash, locationMessage.getUserId(),
                    ShipperDetailCache
                            .builder()
                            .id(locationMessage.getUserId())
                            .vehicleType(vehicle.getId())
                            .ts(LocalDateTime.now())
                            .latitude(locationMessage.getLatitude())
                            .longitude(locationMessage.getLongitude()).build()
            );
        }
    }
}
