package com.nta.controller.websocker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nta.dto.request.UpdateShipperLocationRequest;
import com.nta.enums.MessageType;
import com.nta.model.Message;
import com.nta.service.GeoHashService;
import com.nta.service.PostService;
import com.nta.service.websocker.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
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
    private final ObjectMapper objectMapper;
    private final PostService postService;
    @MessageMapping("/shipper/{shipperId}")
    public void updateLocation(
            @Payload Message message,
            @DestinationVariable String shipperId,
            Principal principal
    ) throws JsonProcessingException {
        if(message.getMessageType() != null && message.getMessageType().equals(MessageType.UPDATE_SHIPPER_LOCATION)) {
            final UpdateShipperLocationRequest update = objectMapper.readValue(message.getContent(), UpdateShipperLocationRequest.class);
            locationService.updateLocation(update,principal);
        }
    }

}
