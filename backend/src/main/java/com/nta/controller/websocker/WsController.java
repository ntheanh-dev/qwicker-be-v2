package com.nta.controller.websocker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nta.dto.request.UpdateShipperLocationRequest;
import com.nta.enums.MessageType;
import com.nta.model.Message;
import com.nta.service.AuthenticationService;
import com.nta.service.PostService;
import com.nta.service.ShipperService;
import com.nta.service.websocker.LocationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WsController {
  LocationService locationService;
  ObjectMapper objectMapper;
  PostService postService;
  AuthenticationService authenticationService;
  ShipperService shipperService;

  @MessageMapping("/shipper/{shipperId}")
  public void updateLocation(
      @Payload Message message, @DestinationVariable String shipperId, Principal principal)
      throws JsonProcessingException {
    if (message.getMessageType() != null
        && message.getMessageType().equals(MessageType.UPDATE_SHIPPER_LOCATION)) {
      final UpdateShipperLocationRequest update =
          objectMapper.readValue(message.getContent(), UpdateShipperLocationRequest.class);
      locationService.updateLocation(update, principal);
    }
  }

  @MessageMapping("/post/{postId}")
  public void post(
      @Payload Message message, @DestinationVariable String postId, Principal principal)
      throws JsonProcessingException {

    if (message.getMessageType() != null
        && message.getMessageType().equals(MessageType.REQUEST_JOIN_POST)) {
      final var userDetail = authenticationService.getUserDetail(principal);
      final String shipperId = shipperService.findShipperIdByUserId(userDetail.getId());
      postService.joinPost(postId, shipperId);
    }
  }
}
