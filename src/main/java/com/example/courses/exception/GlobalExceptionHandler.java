package com.example.courses.exception;

import com.example.courses.model.dto.response.DataResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Xử lý lỗi validation cho request body
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<DataResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getFieldErrors().forEach(fieldError -> {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        });

        DataResponse<Void> response = DataResponse.error("Dữ liệu đầu vào không hợp lệ", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý lỗi type mismatch cho path variable (VD: ID không phải số)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<DataResponse<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.warn("Method argument type mismatch: {}", ex.getMessage());

        String paramName = ex.getName();
        String invalidValue = ex.getValue() != null ? ex.getValue().toString() : "null";
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown";

        Map<String, String> details = new HashMap<>();
        details.put("parameter", paramName);
        details.put("invalidValue", invalidValue);
        details.put("requiredType", requiredType);
        details.put("error", String.format("Giá trị '%s' không hợp lệ cho tham số '%s'. Yêu cầu kiểu dữ liệu: %s",
                invalidValue, paramName, requiredType));

        DataResponse<Void> response = DataResponse.error("Tham số không hợp lệ", details);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý lỗi NumberFormatException
     */
    @ExceptionHandler(NumberFormatException.class)
    public ResponseEntity<DataResponse<Void>> handleNumberFormatException(NumberFormatException ex) {
        log.warn("Number format error: {}", ex.getMessage());

        Map<String, String> details = Map.of(
                "error", "Định dạng số không hợp lệ",
                "message", "Vui lòng nhập một số nguyên hợp lệ"
        );

        DataResponse<Void> response = DataResponse.error("Lỗi định dạng số", details);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý lỗi JSON parsing (request body không đúng format)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<DataResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("HTTP message not readable: {}", ex.getMessage());

        Map<String, String> details = Map.of(
                "error", "Dữ liệu JSON không hợp lệ",
                "message", "Vui lòng kiểm tra định dạng JSON trong request body"
        );

        DataResponse<Void> response = DataResponse.error("Lỗi định dạng dữ liệu", details);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý lỗi vi phạm ràng buộc (constraint violations)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<DataResponse<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });

        DataResponse<Void> response = DataResponse.error("Vi phạm ràng buộc dữ liệu", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
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
     * Xử lý lỗi xác thực sai thông tin đăng nhập
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<DataResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());

        Map<String, String> details = Map.of("error", "Username hoặc password không đúng");
        DataResponse<Void> response = DataResponse.error("Thông tin đăng nhập không chính xác", details);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Xử lý lỗi tài khoản bị vô hiệu hóa
     */
    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<DataResponse<Void>> handleAccountDisabledException(AccountDisabledException ex) {
        log.warn("Account disabled: {}", ex.getMessage());

        Map<String, String> details = Map.of("error", "Tài khoản đã bị vô hiệu hóa");
        DataResponse<Void> response = DataResponse.error(ex.getMessage(), details);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Xử lý lỗi xác thực chung
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<DataResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());

        Map<String, String> details = Map.of("error", ex.getMessage() != null ? ex.getMessage() : "Không có quyền truy cập");
        DataResponse<Void> response = DataResponse.error("Xác thực thất bại", details);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Xử lý custom BadRequestException
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<DataResponse<Void>> handleBadRequestException(BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());

        DataResponse<Void> response = DataResponse.error(ex.getMessage(), ex.getDetails());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý custom NotFoundException
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<DataResponse<Void>> handleNotFoundException(NotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());

        DataResponse<Void> response = DataResponse.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Xử lý lỗi không tìm thấy phần tử
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<DataResponse<Void>> handleNoSuchElementException(NoSuchElementException ex) {
        log.warn("No such element: {}", ex.getMessage());

        DataResponse<Void> response = DataResponse.error("Không tìm thấy phần tử được yêu cầu");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Xử lý lỗi phân tích ngày giờ
     */
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<DataResponse<Void>> handleDateTimeParseException(DateTimeParseException ex) {
        log.warn("Date time parse error: {}", ex.getMessage());

        Map<String, String> details = new HashMap<>();
        details.put("invalidInput", ex.getParsedString());
        details.put("error", "Định dạng ngày giờ không hợp lệ");
        details.put("expectedFormat", "Vui lòng sử dụng định dạng: yyyy-MM-dd HH:mm:ss hoặc yyyy-MM-dd");

        DataResponse<Void> response = DataResponse.error("Lỗi phân tích ngày giờ", details);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý lỗi không tìm thấy tài nguyên (Spring 6+)
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<DataResponse<Void>> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("No resource found: {}", ex.getMessage());

        Map<String, String> details = new HashMap<>();
        details.put("resourcePath", ex.getResourcePath());
        details.put("httpMethod", ex.getHttpMethod().name());

        DataResponse<Void> response = DataResponse.error("Không tìm thấy tài nguyên được yêu cầu", details);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Xử lý RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<DataResponse<Void>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);

        DataResponse<Void> response = DataResponse.error(ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Xử lý các lỗi chung không được xử lý cụ thể
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<DataResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);

        DataResponse<Void> response = DataResponse.error("Đã xảy ra lỗi hệ thống");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}