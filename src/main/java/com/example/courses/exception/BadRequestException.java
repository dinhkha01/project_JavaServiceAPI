package com.example.courses.exception;

import java.util.Map;

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

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }
}
