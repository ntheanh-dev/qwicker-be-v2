package com.nta.dto.response;

import com.nta.entity.*;
import com.nta.enums.PostStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostResponse {
    String id;
    String description;
    LocalDateTime postTime;
    String requestType; // now or latter
    PostStatus status;
    Product product;
    Location pickupLocation;
    LocalDateTime pickupDatetime;
    Location dropLocation;
    LocalDateTime dropDateTime;
    Payment payment;
    Vehicle vehicleType;
}
