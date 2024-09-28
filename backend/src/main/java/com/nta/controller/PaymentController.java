package com.nta.controller;

import com.cloudinary.Api;
import com.nta.dto.response.ApiResponse;
import com.nta.dto.response.VNPayResponse;
import com.nta.entity.PaymentMethod;
import com.nta.enums.VNPayStatus;
import com.nta.service.PaymentMethodService;
import com.nta.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
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
public class PaymentController {
  PaymentMethodService paymentMethodService;
  PaymentService paymentService;

  @GetMapping("/methods")
  ApiResponse<List<PaymentMethod>> findAllPaymentMethod() {
    var response = paymentMethodService.findAll();
    return ApiResponse.<List<PaymentMethod>>builder().result(response).build();
  }

  @GetMapping("/vn-pay")
  public ApiResponse<VNPayResponse> pay(
      final HttpServletRequest request) { // use HttpServletRequest for getting client ip
    return ApiResponse.<VNPayResponse>builder()
        .result(paymentService.createVnPayPayment(request))
        .build();
  }

  @GetMapping("/vn-pay-callback")
  public ApiResponse<VNPayStatus> payCallbackHandler(final HttpServletRequest request) {
    final VNPayStatus status = VNPayStatus.fromCode(request.getParameter("vnp_ResponseCode"));
    if(status == VNPayStatus.VNPAY_00) {
      final String postId = request.getParameter("vnp_OrderInfo");
      paymentService.handlePaymentByVNPaySuccess(postId);
    }
    return ApiResponse.<VNPayStatus>builder().result(status).build();
  }
}
