
package com.example.courses.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationRequest {
    @NotNull(message = "Userid không được để trống")
    private Integer userId;

    @NotBlank(message = "Message không được để trống")
    private String message;

    private String type;
    private String targetUrl;
}