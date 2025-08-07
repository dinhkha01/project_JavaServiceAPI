package com.example.courses.exception;

import com.example.courses.model.dto.response.DataResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class EnrollmentExceptionHandler {

    /**
     * Xử lý lỗi validation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<DataResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        BindingResult bindingResult = ex.getBindingResult();
        for (FieldError error : bindingResult.getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        DataResponse<Void> response = DataResponse.error("Dữ liệu không hợp lệ", errors);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Xử lý lỗi không có quyền truy cập
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<DataResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        DataResponse<Void> response = DataResponse.error("Bạn không có quyền thực hiện hành động này");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Xử lý lỗi runtime exception
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<DataResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);

        DataResponse<Void> response = DataResponse.error(ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Xử lý lỗi chung
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<DataResponse<Void>> handleGeneralException(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);

        DataResponse<Void> response = DataResponse.error("Đã xảy ra lỗi hệ thống");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}