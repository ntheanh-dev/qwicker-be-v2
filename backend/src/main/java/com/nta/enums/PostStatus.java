package com.nta.enums;

public enum PostStatus {
    PENDING,
    WAITING_PAY,
    FOUND_SHIPPER,
    CONFIRM_WITH_CUSTOMER, // shipper confirmed with customer go to pickup location
    SHIPPED,// shipper taken order and go to drop location
    DELIVERED,// shipped
    CANCELLED
}
