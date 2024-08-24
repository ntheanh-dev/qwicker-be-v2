package com.nta.controller.websocker;

import com.nta.model.LocationMessage;
import com.nta.service.websocker.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WsLocationController {
    private final LocationService locationService;

    @MessageMapping("/locations")
    public LocationMessage sendLocationToSpecificGeohash(
            @Payload LocationMessage locationMessage,
            Principal principal
    ) {
        locationService.addLocation(locationMessage,principal);
        return locationMessage;
    }
}
