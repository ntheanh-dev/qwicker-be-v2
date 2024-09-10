package com.nta.dto.request;

import com.nta.enums.StatisticIncomeType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class StatisticIncomeRequest {
    LocalDateTime startDate;
    LocalDateTime endDate;
    String type;
}
