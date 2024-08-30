package com.nta.model;

import com.nta.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationMessage {
    private String userId;
    private double latitude;
    private double longitude;

    private double prevLatitude;
    private double prevLongitude;
    private double timestamp;
    private MessageType messageType;

}
