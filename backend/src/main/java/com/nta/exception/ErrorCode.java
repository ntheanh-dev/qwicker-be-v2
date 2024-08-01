package com.nta.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    ERROR_KEY_INVALID(1001,"Không tìm thấy errorcode tương ứng", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_EXISTED(1002,"A User with this username already exist", HttpStatus.CONFLICT),
    PASSWORD_INVALID(1003,"password phải ít nhất 8 kí tự",HttpStatus.BAD_REQUEST),
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
