package com.nta.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class ShipperDetailCache {
    private String id;
    private String vehicleType;
    private LocalDateTime ts;
    private double latitude;
    private double longitude;
}
