package com.nta.runner;

import com.nta.dto.request.ShipperLocationRequest;
import com.nta.service.GeoLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationRunner implements CommandLineRunner {
    private final GeoLocationService geoLocationService;

    @Override
    public void run(String... args) throws Exception {
//        geoLocationService.addShipperLocation(ShipperLocationRequest.builder()
//                .latitude(10.909339491094045)
//                .longitude(106.64649014816598)
//                .id("1")
//                .build());
//
//        geoLocationService.addShipperLocation(ShipperLocationRequest.builder()
//                .latitude(10.910909703943302)
//                .longitude(106.64904802328553)
//                .id("2")
//                .build());
//
//        geoLocationService.addShipperLocation(ShipperLocationRequest.builder()
//                .latitude(10.901989321074767)
//                .longitude(106.64627281469613)
//                .id("3")
//                .build());
//
//        geoLocationService.addShipperLocation(ShipperLocationRequest.builder()
//                .latitude(10.90412401715822)
//                .longitude(106.65396738536937)
//                .id("4")
//                .build());
//        log.info("---------Initial Points-----------");
    }
}
