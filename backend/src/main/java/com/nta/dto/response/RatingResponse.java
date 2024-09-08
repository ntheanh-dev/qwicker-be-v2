package com.nta.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingResponse {
    double rating;
    String feedback;
    LocalDateTime createdAt;
    UserResponse user;
}
