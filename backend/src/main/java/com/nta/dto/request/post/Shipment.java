package com.nta.dto.request.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nta.dto.request.LocationCreationRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Shipment {
    BigDecimal cost;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-M-dd H:m")
    LocalDateTime pickupDatetime;
    String type; // now or latter
    LocationCreationRequest pickupLocation;
    LocationCreationRequest dropLocation;
}
