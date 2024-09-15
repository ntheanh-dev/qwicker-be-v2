package com.nta.dto.response.ws;

import com.nta.enums.MessageType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShipperLocationResponse {
    private MessageType messageType;
    private double latitude;
    private double longitude;
}
