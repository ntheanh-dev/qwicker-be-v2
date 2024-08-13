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
    String contact;
    String addressLine;
    String formattedAddress;
    String postalCode;
    BigDecimal latitude;
    BigDecimal longitude;
}
