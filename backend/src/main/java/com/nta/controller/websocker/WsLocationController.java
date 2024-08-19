package com.nta.controller.websocker;

import com.nta.model.LocationMessage;
import com.nta.service.websocker.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WsLocationController {
    private final LocationService locationService;

    @MessageMapping("/locations")
    public LocationMessage sendLocationToSpecificGeohash(
            @Payload LocationMessage locationMessage,
            SimpMessageHeaderAccessor headerAccessor
    ) {
//        locationService.addLocation(locationMessage,headerAccessor);
        return locationMessage;
    }
}
