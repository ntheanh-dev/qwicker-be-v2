package com.nta.dto.response.ws;

import com.nta.dto.response.PostResponse;
import com.nta.dto.response.ShipperResponse;
import com.nta.enums.MessageType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FoundShipperResponse {
    private MessageType messageType;
    private ShipperResponse shipper;
    private PostResponse post;
}
