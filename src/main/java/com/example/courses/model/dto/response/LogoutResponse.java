package com.example.courses.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO cho logout API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogoutResponse {
    private String username;
    private LocalDateTime logoutTime;
    private String message;
    private boolean loggedOutFromAllDevices;
    private String deviceId;
}