package com.example.courses.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception được throw khi tài khoản người dùng bị vô hiệu hóa (isActive = false)
 */
public class AccountDisabledException extends AuthenticationException {

    public AccountDisabledException(String message) {
        super(message);
    }

    public AccountDisabledException(String message, Throwable cause) {
        super(message, cause);
    }
}