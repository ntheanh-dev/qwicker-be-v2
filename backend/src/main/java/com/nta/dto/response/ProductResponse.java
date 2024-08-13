package com.nta.dto.response;

import com.nta.entity.ProductCategory;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {

    int quantity;
    String image;
    String mass;
    ProductCategory productCategory;
}
