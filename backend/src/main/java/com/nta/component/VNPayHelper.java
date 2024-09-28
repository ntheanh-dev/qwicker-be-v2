package com.nta.component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Component
public class VNPayHelper {
  @Value("${server.address:localhost}")  // Mặc định là localhost nếu không được cấu hình
  private String address;
  @Value("${server.servlet.context-path}")
  private String contextPath;
  @Value("${server.port:8080}")  // Mặc định là 8080 nếu không được cấu hình
  private int port;
  public String hmacSHA512(final String key, final String data) {
    try {
      if (key == null || data == null) {
        throw new NullPointerException();
      }
      final Mac hmac512 = Mac.getInstance("HmacSHA512");
      byte[] hmacKeyBytes = key.getBytes();
      final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
      hmac512.init(secretKey);
      byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
      byte[] result = hmac512.doFinal(dataBytes);
      StringBuilder sb = new StringBuilder(2 * result.length);
      for (byte b : result) {
        sb.append(String.format("%02x", b & 0xff));
      }
      return sb.toString();

    } catch (Exception ex) {
      return "";
    }
  }

  public String getIpAddress(final HttpServletRequest request) {
    String ipAddress;
    try {
      ipAddress = request.getHeader("X-FORWARDED-FOR");
      return ipAddress == null ? request.getRemoteAddr() : ipAddress;
    } catch (Exception e) {
      return null;
    }
  }

  public String getRandomNumber(final int len) {
    final Random rnd = new Random();
    final String chars = "0123456789";
    final StringBuilder sb = new StringBuilder(len);
    for (int i = 0; i < len; i++) {
      sb.append(chars.charAt(rnd.nextInt(chars.length())));
    }
    return sb.toString();
  }

  public String getPaymentURL(final Map<String, String> paramsMap, final boolean encodeKey) {
    return paramsMap.entrySet().stream()
        .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
        .sorted(Map.Entry.comparingByKey())
        .map(
            entry ->
                (encodeKey
                        ? URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII)
                        : entry.getKey())
                    + "="
                    + URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII))
        .collect(Collectors.joining("&"));
  }

  public String getPaymentReturnURL() {
    return "http:" + address +":" + port + contextPath +"/payment/vn-pay-callback";
  }
}
