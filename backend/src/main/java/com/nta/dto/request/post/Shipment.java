package com.nta.dto.request.post;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nta.dto.request.LocationCreationRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Shipment {
    int cost;

//    @DateTimeFormat(pattern = "yyyy-M-dd HH:mm", iso = ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-M-dd HH:mm")
    LocalDateTime pickupDatetime;
    String type;

    LocationCreationRequest pickupLocation;
    LocationCreationRequest dropLocation;
}
