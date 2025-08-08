package com.example.courses.exception;

import com.example.courses.model.dto.response.DataError;
import com.example.courses.model.dto.response.DataResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Xử lý lỗi validation cho request body - Trả về DataError
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<DataError> handleValidateException(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());

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
     * Xử lý lỗi vi phạm ràng buộc (constraint violations) - Trả về DataError
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<DataError> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());

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
     * Xử lý lỗi không có quyền truy cập - Trả về DataResponse
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<DataResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        DataResponse<Void> response = DataResponse.error("Bạn không có quyền thực hiện hành động này");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Xử lý lỗi xác thực (authentication) - Trả về DataError
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<DataError> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());

        DataError dataError = DataError.builder()
                .success(false)
                .code(401)
                .message("Thông tin đăng nhập không chính xác")
                .details(Map.of("error", "Username hoặc password không đúng"))
                .build();
        return new ResponseEntity<>(dataError, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Xử lý lỗi xác thực chung - Trả về DataError
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<DataError> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());

        DataError dataError = DataError.builder()
                .success(false)
                .code(401)
                .message("Xác thực thất bại")
                .details(Map.of("error", ex.getMessage() != null ? ex.getMessage() : "Không có quyền truy cập"))
                .build();
        return new ResponseEntity<>(dataError, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Xử lý custom BadRequestException - Trả về DataError
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<DataError> handleBadRequestException(BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());

        DataError dataError = DataError.builder()
                .success(false)
                .code(400)
                .message(ex.getMessage())
                .details(ex.getDetails() != null ? ex.getDetails() : Map.of("error", "Yêu cầu không hợp lệ"))
                .build();
        return new ResponseEntity<>(dataError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý custom NotFoundException - Trả về DataError
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<DataError> handleNotFoundException(NotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());

        DataError dataError = DataError.builder()
                .success(false)
                .code(404)
                .message(ex.getMessage())
                .details(Map.of("error", "Tài nguyên không tìm thấy"))
                .build();
        return new ResponseEntity<>(dataError, HttpStatus.NOT_FOUND);
    }

    /**
     * Xử lý lỗi không tìm thấy phần tử - Trả về DataError
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<DataError> handleNoSuchElementException(NoSuchElementException ex) {
        log.warn("No such element: {}", ex.getMessage());

        DataError dataError = DataError.builder()
                .success(false)
                .code(404)
                .message("Không tìm thấy phần tử được yêu cầu")
                .details(Map.of("error", ex.getMessage() != null ? ex.getMessage() : "Phần tử không tồn tại"))
                .build();
        return new ResponseEntity<>(dataError, HttpStatus.NOT_FOUND);
    }

    /**
     * Xử lý lỗi phân tích ngày giờ - Trả về DataError
     */
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<DataError> handleDateTimeParseException(DateTimeParseException ex) {
        log.warn("Date time parse error: {}", ex.getMessage());

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
     * Xử lý lỗi không tìm thấy tài nguyên (Spring 6+) - Trả về DataError
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<DataError> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("No resource found: {}", ex.getMessage());

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
     * Xử lý RuntimeException - Trả về DataResponse
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<DataResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);

        DataResponse<Void> response = DataResponse.error(ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Xử lý các lỗi chung không được xử lý cụ thể - Trả về DataError
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<DataError> handleGenericException(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);

        DataError dataError = DataError.builder()
                .success(false)
                .code(500)
                .message("Đã xảy ra lỗi hệ thống")
                .details(Map.of("error", ex.getMessage() != null ? ex.getMessage() : "Lỗi không xác định"))
                .build();
        return new ResponseEntity<>(dataError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}