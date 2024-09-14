package com.nta.dto.response;

import com.nta.entity.PaymentMethod;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {
  String id;
  BigDecimal price;
  LocalDateTime paidAt;
  boolean isPosterPay;
  PaymentMethod method;
}
