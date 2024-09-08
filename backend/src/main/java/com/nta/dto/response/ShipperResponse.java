package com.nta.dto.response;

import com.nta.entity.Rating;
import com.nta.entity.User;
import com.nta.entity.Vehicle;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShipperResponse {
    String id;
    String identityF;
    String identityB;
    String vehicleNumber;
    UserResponse user;
    Vehicle vehicle;
    Set<RatingResponse> ratings;
}
