package com.nta.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    ERROR_KEY_INVALID(1001, "Không tìm thấy errorcode tương ứng", HttpStatus.INTERNAL_SERVER_ERROR),
    USERNAME_EXISTED(1002, "This username has already existed", HttpStatus.CONFLICT),
    PASSWORD_INVALID(1003, "password phải ít nhất 8 kí tự", HttpStatus.BAD_REQUEST),
    AVATAR_REQUIRED(1004, "a avatar file is required", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1005, "This email has already existed", HttpStatus.CONFLICT),
    USER_NOT_EXISTED(1006, "User not existed", HttpStatus.NOT_FOUND),
    INVALID_TOKEN(1008, "Invalid token", HttpStatus.BAD_REQUEST),
    INCORRECT_PASSWORD(1009, "Invalid password", HttpStatus.BAD_REQUEST),
    ROLE_USER_NOT_FOUND(1010, "ROLE_USER not found", HttpStatus.INTERNAL_SERVER_ERROR),
    ROLE_SHIPPER_NOT_FOUND(1011, "ROLE_SHIPPER not found", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED(1012, "unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    AUTHENTICATION_IST_NOT_INSTANCEOF_JWT(1008,
            "Authentication object is not instanceof jwtAuthenticaton", HttpStatus.INTERNAL_SERVER_ERROR),
    //Vehicle
    VEHICLE_NOT_FOUND(1050, "Vehicle not found", HttpStatus.BAD_REQUEST),
    //Redis
    REDIS_SERVER_NOT_FOUD(1080, "Cannot connect to Redis server", HttpStatus.INTERNAL_SERVER_ERROR),
    //Shipper
    CREATE_SHIPPER_FAILED(1100, "Cannot create shipper due to failure of upload identify photo",
            HttpStatus.INTERNAL_SERVER_ERROR),
    CANNOT_FIND_SHIPPER_IN_REDIS(1101, "Cannot find shipper in redis", HttpStatus.INTERNAL_SERVER_ERROR),
    //Products and category
    CATEGORY_NOT_FOUND(1151, "Product category not found", HttpStatus.BAD_REQUEST),
    //Payment and method
    PAYMENT_METHOD_NOT_FOUND(1200, "Payment method not found", HttpStatus.BAD_REQUEST),
    //Post
    POST_NOT_FOUND(1250, "Post not found", HttpStatus.BAD_REQUEST),
    SHIPPER_POST_EXISTED(1251, "ShipperPost existed", HttpStatus.CONFLICT),
    POST_WAS_TAKEN(1252, "Post was taken", HttpStatus.CONFLICT),
    INVALID_POST_STATUS(1253, "Invalid post status", HttpStatus.BAD_REQUEST),
    CANNOT_UPDATE_POST_STATUS(1254, "Cannot update post, new post status is not in order", HttpStatus.BAD_REQUEST),
    //webclient
    CAN_NOT_CALL_API(1280, "Can not call api", HttpStatus.INTERNAL_SERVER_ERROR),
    //location
    INVALID_LOCATION_DATA(1300,"INVALID_LOCATION_DATA", HttpStatus.BAD_REQUEST);
    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private int code;
    private String message;
    private HttpStatusCode statusCode;
}
