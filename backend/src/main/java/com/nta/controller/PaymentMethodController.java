package com.nta.controller;

import com.nta.dto.response.ApiResponse;
import com.nta.entity.PaymentMethod;
import com.nta.service.PaymentMethodService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentMethodController {
    PaymentMethodService paymentMethodService;

    @GetMapping("/methods")
    ApiResponse<List<PaymentMethod>> findAllPaymentMethod() {
        var response = paymentMethodService.findAll();
        return ApiResponse.<List<PaymentMethod>>builder()
                .result(response)
                .build();
    }
}
