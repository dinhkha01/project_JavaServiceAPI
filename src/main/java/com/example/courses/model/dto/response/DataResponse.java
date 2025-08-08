package com.example.courses.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataResponse<T> {
    @Builder.Default
    private boolean success = true;

    private String message;

    private T data;

    private Map<String, String> errors;

    @Builder.Default
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> DataResponse<T> success(T data, String message) {
        return DataResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> DataResponse<T> success(T data) {
        return success(data, "Thao tác thành công");
    }

    public static <T> DataResponse<T> success(String message) {
        return DataResponse.<T>builder()
                .success(true)
                .message(message)
                .data(null)
                .build();
    }

    public static <T> DataResponse<T> error(String message, Map<String, String> errors) {
        return DataResponse.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }

    public static <T> DataResponse<T> error(String message) {
        return DataResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}