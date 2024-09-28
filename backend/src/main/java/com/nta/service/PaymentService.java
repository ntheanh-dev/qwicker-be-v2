package com.nta.service;

import com.nta.component.VNPayHelper;
import com.nta.configuration.VNPayConfig;
import com.nta.dto.response.VNPayResponse;
import com.nta.entity.Payment;
import com.nta.entity.Post;
import com.nta.entity.PostHistory;
import com.nta.enums.ErrorCode;
import com.nta.enums.PostStatus;
import com.nta.exception.AppException;
import com.nta.repository.PaymentRepository;
import com.nta.repository.PostHistoryRepository;
import com.nta.repository.PostRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {
  private final VNPayConfig vnPayConfig;
  private final VNPayHelper vnPayHelper;
  private final PaymentRepository paymentRepository;
  private final PostRepository postRepository;
  private final PostHistoryRepository postHistoryRepository;

  public VNPayResponse createVnPayPayment(final HttpServletRequest request) {
    final String postId = request.getParameter("orderInfo"); // postID
    postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

    final long amount = Integer.parseInt(request.getParameter("amount")) * 100L;
    final String bankCode = request.getParameter("bankCode");

    final Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();
    vnpParamsMap.put("vnp_OrderInfo", postId);
    vnpParamsMap.put("vnp_Amount", String.valueOf(amount));

    if (bankCode != null && !bankCode.isEmpty()) {
      vnpParamsMap.put("vnp_BankCode", bankCode);
    }

    final String clientIp = vnPayHelper.getIpAddress(request);
    if (clientIp != null && !clientIp.isEmpty()) {
      vnpParamsMap.put("vnp_IpAddr", clientIp);
    }
    vnpParamsMap.put("vnp_ReturnUrl", vnPayHelper.getPaymentReturnURL());
    // build query url
    String queryUrl = vnPayHelper.getPaymentURL(vnpParamsMap, true);
    final String hashData = vnPayHelper.getPaymentURL(vnpParamsMap, false);
    final String vnpSecureHash = vnPayHelper.hmacSHA512(vnPayConfig.getSecretKey(), hashData);
    queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
    final String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;
    return VNPayResponse.builder().paymentUrl(paymentUrl).build();
  }

  public void handlePaymentByVNPaySuccess(final String postId) {
    final Post post = postRepository.findById(postId).orElse(null);
    final Payment payment = paymentRepository.findByPostId(postId).orElse(null);

    if (post != null && payment != null) {
      payment.setPaidAt(LocalDateTime.now());
      paymentRepository.save(payment);

      post.setStatus(PostStatus.PENDING);
      postRepository.save(post);

      postHistoryRepository.save(
          PostHistory.builder()
              .status(PostStatus.PAID_BY_VNPAY)
              .post(post)
              .statusChangeDate(LocalDateTime.now())
              .build());
    }
  }
}
