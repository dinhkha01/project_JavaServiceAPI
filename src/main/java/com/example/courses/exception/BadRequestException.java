package com.example.courses.exception;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Setter
@Getter
public class BadRequestException extends Exception {
    private Map<String, String> details;

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Map<String, String> details) {
        super(message);
        this.details = details;
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadRequestException(String message, Map<String, String> details, Throwable cause) {
        super(message, cause);
        this.details = details;
    }
}