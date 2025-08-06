package com.example.courses.exception;

import com.example.courses.model.dto.response.DataError;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Xử lý lỗi validation cho request body
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<DataError> handleValidateException(MethodArgumentNotValidException ex) {
        Map<String, String> details = new HashMap<>();
        ex.getFieldErrors().forEach(fieldError -> {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        });

        DataError dataError = DataError.builder()
                .success(false)
                .code(400)
                .message("Dữ liệu đầu vào không hợp lệ")
                .details(details)
                .build();
        return new ResponseEntity<>(dataError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý lỗi vi phạm ràng buộc (constraint violations)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<DataError> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> details = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            details.put(fieldName, errorMessage);
        });

        DataError dataError = DataError.builder()
                .success(false)
                .code(400)
                .message("Vi phạm ràng buộc dữ liệu")
                .details(details)
                .build();
        return new ResponseEntity<>(dataError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý lỗi xác thực (authentication)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<DataError> handleBadCredentialsException(BadCredentialsException ex) {
        DataError dataError = DataError.builder()
                .success(false)
                .code(401)
                .message("Thông tin đăng nhập không chính xác")
                .details(Map.of("error", "Username hoặc password không đúng"))
                .build();
        return new ResponseEntity<>(dataError, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Xử lý lỗi xác thực chung
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<DataError> handleAuthenticationException(AuthenticationException ex) {
        DataError dataError = DataError.builder()
                .success(false)
                .code(401)
                .message("Xác thực thất bại")
                .details(Map.of("error", ex.getMessage() != null ? ex.getMessage() : "Không có quyền truy cập"))
                .build();
        return new ResponseEntity<>(dataError, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Xử lý lỗi không tìm thấy phần tử
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<DataError> handleNoSuchElementException(NoSuchElementException ex) {
        DataError dataError = DataError.builder()
                .success(false)
                .code(404)
                .message("Không tìm thấy phần tử được yêu cầu")
                .details(Map.of("error", ex.getMessage() != null ? ex.getMessage() : "Phần tử không tồn tại"))
                .build();
        return new ResponseEntity<>(dataError, HttpStatus.NOT_FOUND);
    }

    /**
     * Xử lý lỗi phân tích ngày giờ
     */
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<DataError> handleDateTimeParseException(DateTimeParseException ex) {
        Map<String, String> details = new HashMap<>();
        details.put("invalidInput", ex.getParsedString());
        details.put("error", "Định dạng ngày giờ không hợp lệ");
        details.put("expectedFormat", "Vui lòng sử dụng định dạng: yyyy-MM-dd HH:mm:ss hoặc yyyy-MM-dd");

        DataError dataError = DataError.builder()
                .success(false)
                .code(400)
                .message("Lỗi phân tích ngày giờ")
                .details(details)
                .build();
        return new ResponseEntity<>(dataError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý lỗi không tìm thấy tài nguyên (Spring 6+)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<DataError> handleNoResourceFoundException(NoResourceFoundException ex) {
        Map<String, String> details = new HashMap<>();
        details.put("resourcePath", ex.getResourcePath());
        details.put("httpMethod", ex.getHttpMethod().name());

        DataError dataError = DataError.builder()
                .success(false)
                .code(404)
                .message("Không tìm thấy tài nguyên được yêu cầu")
                .details(details)
                .build();
        return new ResponseEntity<>(dataError, HttpStatus.NOT_FOUND);
    }

    /**
     * Xử lý custom BadRequestException
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<DataError> handleBadRequestException(BadRequestException ex) {
        DataError dataError = DataError.builder()
                .success(false)
                .code(400)
                .message(ex.getMessage())
                .details(ex.getDetails() != null ? ex.getDetails() : Map.of("error", "Yêu cầu không hợp lệ"))
                .build();
        return new ResponseEntity<>(dataError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý custom NotFoundException
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<DataError> handleNotFoundException(NotFoundException ex) {
        DataError dataError = DataError.builder()
                .success(false)
                .code(404)
                .message(ex.getMessage())
                .details(Map.of("error", "Tài nguyên không tìm thấy"))
                .build();
        return new ResponseEntity<>(dataError, HttpStatus.NOT_FOUND);
    }

    /**
     * Xử lý các lỗi chung không được xử lý cụ thể
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<DataError> handleGenericException(Exception ex) {
        DataError dataError = DataError.builder()
                .success(false)
                .code(500)
                .message("Đã xảy ra lỗi hệ thống")
                .details(Map.of("error", ex.getMessage() != null ? ex.getMessage() : "Lỗi không xác định"))
                .build();
        return new ResponseEntity<>(dataError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}