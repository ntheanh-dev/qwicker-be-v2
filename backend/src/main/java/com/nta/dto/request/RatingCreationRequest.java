package com.nta.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingCreationRequest {
    String postId;
    String shipperId;
    int rating;
    String feedback;
}
