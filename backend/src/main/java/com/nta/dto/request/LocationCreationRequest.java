package com.nta.dto.request;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationCreationRequest {
    String name; //for user or company
    String contact;
    String formattedAddress;
    String postalCode;
    String address;
    BigDecimal latitude;
    BigDecimal longitude;
}
