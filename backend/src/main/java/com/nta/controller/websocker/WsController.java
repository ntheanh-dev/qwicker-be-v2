package com.nta.controller.websocker;

import com.nta.entity.Location;
import com.nta.entity.Post;
import com.nta.enums.ErrorCode;
import com.nta.enums.MessageType;
import com.nta.exception.AppException;
import com.nta.model.LocationMessage;
import com.nta.model.Message;
import com.nta.model.websocket.FindShipper;
import com.nta.service.GeoHashService;
import com.nta.service.websocker.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WsController {
    private final LocationService locationService;
    private final GeoHashService geoHashService;
    private final SimpMessageSendingOperations simpMessageSendingOperations;

    @MessageMapping("/locations")
    public LocationMessage sendLocationToSpecificGeohash(
            @Payload LocationMessage locationMessage,
            Principal principal
    ) {
//        locationService.addLocation(locationMessage,principal);
        return locationMessage;
    }

    @MessageMapping("/post/find-shipper")
    public void findShipper(@Payload FindShipper request, Principal principal) {
        Location location = request.getPost().getPickupLocation();
        try {
            geoHashService.findNearestShipperId(location.getLatitude(),
                    location.getLongitude(), request.getKm());
        } catch (AppException ex) {
            if (ex.getErrorCode().equals(ErrorCode.CANNOT_FIND_SHIPPER_IN_REDIS)) {
                log.info("Sent message to /topic/post/{}", request.getPost().getId());
                simpMessageSendingOperations.convertAndSend("/topic/post/" + request.getPost().getId(),
                        Message.builder().type(MessageType.NOT_FOUND_SHIPPER).build());
            }
        }

    }
}
