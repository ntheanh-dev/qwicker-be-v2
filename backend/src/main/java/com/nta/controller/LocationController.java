package com.nta.controller;

import com.nta.dto.response.DurationBingMapApiResponse;
import com.nta.dto.response.ApiResponse;
import com.nta.service.ExternalApiService;
import com.nta.service.GeoHashService;
import com.nta.service.websocker.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {
    private final ExternalApiService externalApiService;
    private final GeoHashService geoLocationService;
    private final LocationService locationService;

    @GetMapping("/duration")
    ApiResponse<DurationBingMapApiResponse> getDrivingRoute(
            @RequestParam String p1,
            @RequestParam String p2
    ) {
        String[] p1Values = p1.split(",");
        double lat1 = Double.parseDouble(p1Values[0]);
        double long1 = Double.parseDouble(p1Values[1]);

        // Tách giá trị vĩ độ và kinh độ của p2
        String[] p2Values = p2.split(",");
        double lat2 = Double.parseDouble(p2Values[0]);
        double long2 = Double.parseDouble(p2Values[1]);
        var response = externalApiService.getDurationResponseAsync(lat1, long1, lat2, long2);
        return ApiResponse.<DurationBingMapApiResponse>builder()
                .result(response)
                .build();
    }

    @GetMapping("/nearest")
    ApiResponse<String> findNearestShipper(
            @RequestParam double latitude,
            @RequestParam double longitude
    ) {
        var shipperId = geoLocationService.findNearestShipperId(latitude, longitude, 20);
        return ApiResponse.<String>builder().result(shipperId).build();
    }

//    @GetMapping("/distance")
//    ApiResponse<DistanceResponse> getDistanceResponse(
//            @RequestParam(value = "p1",required = true) String p1,
//            @RequestParam(value = "p2",required = true) String p2
//    ) {
//        try {
//            // Tách giá trị vĩ độ và kinh độ của p1
//            String[] p1Values = p1.split(",");
//            double location1Lat = Double.parseDouble(p1Values[0]);
//            double location1Lon = Double.parseDouble(p1Values[1]);
//
//            // Tách giá trị vĩ độ và kinh độ của p2
//            String[] p2Values = p2.split(",");
//            double location2Lat = Double.parseDouble(p2Values[0]);
//            double location2Lon = Double.parseDouble(p2Values[1]);
//
//        } catch (Exception e) {
//            throw new AppException(ErrorCode.INVALID_LOCATION_DATA);
//        }
//    }
}
