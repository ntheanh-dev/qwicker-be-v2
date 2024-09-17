package com.nta.mapper;

import com.nta.dto.response.PaymentResponse;
import com.nta.entity.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentResponse toPaymentResponse(Payment payment);
}
