package com.nta.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShipperCreationRequest {
    String username;
    @Size(min = 8 , message = "PASSWORD_INVALID")
    String password;
    String firstName;
    String lastName;
    String email;
    @NotNull(message = "AVATAR_REQUIRED")
    MultipartFile file;

    //shipper
    String vehicleNumber;

    MultipartFile identityFFile;
    MultipartFile identityBFile;

    //vehicle
    String vehicleId;
}
