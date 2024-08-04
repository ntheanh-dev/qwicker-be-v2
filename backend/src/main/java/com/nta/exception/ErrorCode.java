package com.nta.exception;

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
    //Vehicle
    VEHICLE_NOT_FOUND(1050,"Vehicle not found",HttpStatus.BAD_REQUEST),

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
