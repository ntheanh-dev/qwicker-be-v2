package com.nta.exception;

import com.nta.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Bắt exception trung trung
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Object>> runtimeExceptionHandler(Exception e) {
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setMessage(e.getMessage());
        apiResponse.setCode(999);

        log.error(e.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    // Exception tự tạo
    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse<Object>> appExceptionHandler(AppException e) {
      // Lấy errorcode đường truyền vào khi thor new AppException
      ErrorCode errorCode = e.getErrorCode();

      ApiResponse<Object> apiResponse = new ApiResponse<>();
      apiResponse.setMessage(errorCode.getMessage());
      apiResponse.setCode(errorCode.getCode());

      log.error(errorCode.getMessage());

      return ResponseEntity
              .status(errorCode.getStatusCode())
              .body(apiResponse);
    }

    // Exception khi validate cac filed
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Object>> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        // getFieldErro() còn có thể xác định lỗi ở filed nào, dữ liệu đầu vào là gi....
        String enumKey = e.getFieldError().getDefaultMessage();

        ErrorCode errorCode = ErrorCode.ERROR_KEY_INVALID;
        try {
            errorCode = ErrorCode.valueOf(enumKey);// Lấy ra enum Errorcode bằng tên mà đã được truyền khi validate
        } catch (IllegalArgumentException iae) {
            // Trong trường hợp validate mà truyền sai enum name
        }
        ApiResponse<Object> apiResponse = new ApiResponse<>();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        log.error(errorCode.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }
}
