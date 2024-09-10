package com.nta.dto.response;

import com.nta.enums.StatisticIncomeType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatisticIncomeResponse {
    LocalDateTime dateTime;
    long totalPayments;
    BigDecimal totalRevenue;
    BigDecimal cashRevenue;
    BigDecimal vnPayRevenue;
    StatisticIncomeType type;
}
