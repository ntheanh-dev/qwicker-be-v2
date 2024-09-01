package com.nta.dto.request;

import lombok.Data;

@Data
public class UpdateShipperLocationRequest {
    private String shipperId;
    private double latitude;
    private double longitude;

    private double prevLatitude;
    private double prevLongitude;
    private double timestamp;
}
