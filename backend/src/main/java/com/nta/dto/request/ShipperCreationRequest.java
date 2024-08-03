package com.nta.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShipperCreationRequest {
    //for user entity
    String username;
    @Size(min = 8 , message = "PASSWORD_INVALID")
    String password;
    String firstName;
    String lastName;
    String email;

    // for shipper entity
    String vehicle_number;

    //for vehicle entity
    String vehicle_id;
}
