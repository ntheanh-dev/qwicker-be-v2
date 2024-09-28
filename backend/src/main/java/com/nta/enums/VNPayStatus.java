package com.nta.enums;

import com.nta.exception.AppException;
import lombok.Getter;

@Getter
public enum VNPayStatus {
  VNPAY_00("00", "VNPAY SUCCESS"),
  VNPAY_05("05", "VNPAY BAD_PASSWORD_EXCEEDING"),
  VNPAY_06("06", "VNPAY WRONG_PASSWORD"),
  VNPAY_07("07", "VNPAY SUSPICIOUS_TRANSACTION"),
  VNPAY_12("12", "VNPAY CARD_IS_LOCKED"),
  VNPAY_09("09", "VNPAY NO_INTERNET_BANKING"),
  VNPAY_10("10", "VNPAY VERIFICATION_EXCEEDING"),
  VNPAY_11("11", "VNPAY TIMEOUT_PAYMENT"),
  VNPAY_24("24", "VNPAY CUSTOMER_CANCEL"),
  VNPAY_51("51", "VNPAY NOT_ENOUGH_BALANCE"),
  VNPAY_65("65", "VNPAY TRANSACTION_PER_DAY_EXCEEDING"),
  VNPAY_75("75", "VNPAY BANK_MAINTENANCE"),
  VNPAY_99("99", "VNPAY OTHER_ERRORS");

  private final String code;
  private final String description;

  VNPayStatus(String code, String description) {
    this.code = code;
    this.description = description;
  }

  public static VNPayStatus fromCode(String code) {
    for (VNPayStatus result : VNPayStatus.values()) {
      if (result.getCode().equals(code)) {
        return result;
      }
    }
    throw new AppException(ErrorCode.INVALID_VNPAY_STATUS_CODE);
  }
}
