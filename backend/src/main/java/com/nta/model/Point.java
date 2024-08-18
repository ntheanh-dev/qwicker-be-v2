package com.nta.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Point {
    private Double latitude;
    private Double longitude;
}
