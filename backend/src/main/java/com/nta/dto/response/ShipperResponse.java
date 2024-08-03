package com.nta.dto.response;

import com.nta.entity.User;
import com.nta.entity.Vehicle;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShipperResponse {
    String identity_f;
    String identity_b;
    String vehicle_number;
    User user;
    Vehicle vehicle;
}
