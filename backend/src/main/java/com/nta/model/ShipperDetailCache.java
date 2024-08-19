package com.nta.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash
public class ShipperDetailCache implements Serializable {
    private String id;
    private String vehicleType;
    private LocalDateTime ts;
    private double latitude;
    private double longitude;
}
