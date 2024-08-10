package com.nta.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    ERROR_KEY_INVALID(1001,"Không tìm thấy errorcode tương ứng", HttpStatus.INTERNAL_SERVER_ERROR),
    USERNAME_EXISTED(1002,"This username has already existed", HttpStatus.CONFLICT),
    PASSWORD_INVALID(1003,"password phải ít nhất 8 kí tự",HttpStatus.BAD_REQUEST),
    AVATAR_REQUIRED(1004,"a avatar file is required", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1005,"This email has already existed",HttpStatus.CONFLICT),
    USER_NOT_EXISTED(1006,"User not existed",HttpStatus.NOT_FOUND),
    INVALID_TOKEN(1008,"Invalid token",HttpStatus.BAD_REQUEST),
    INCORRECT_PASSWORD(1009,"Invalid password", HttpStatus.BAD_REQUEST),
    ROLE_USER_NOT_FOUND(1010,"ROLE_USER not found",HttpStatus.INTERNAL_SERVER_ERROR),
    ROLE_SHIPPER_NOT_FOUND(1011,"ROLE_SHIPPER not found",HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED(1012,"unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    //Vehicle
    VEHICLE_NOT_FOUND(1050,"Vehicle not found",HttpStatus.BAD_REQUEST),
    //Redis
    REDIS_SERVER_NOT_FOUD(1080,"Cannot connect to Redis server",HttpStatus.INTERNAL_SERVER_ERROR),
    //Shipper
    CREATE_SHIPPER_FAILED(1100,"Cannot create shipper due to failure of upload identify photo",HttpStatus.INTERNAL_SERVER_ERROR)
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private int code;
    private String message;
    private HttpStatusCode statusCode;
}
