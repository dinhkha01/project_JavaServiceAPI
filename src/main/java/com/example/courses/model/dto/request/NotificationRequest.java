
package com.example.courses.model.dto.request;

import com.example.courses.model.entity.TypeNotification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationRequest {
    @NotNull(message = "Userid không được để trống")
    private Integer userId;

    @NotBlank(message = "Message không được để trống")
    private String message;

    private TypeNotification type;
    private String targetUrl;
}