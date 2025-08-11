
package com.example.courses.model.dto.response;

import com.example.courses.model.entity.TypeNotification;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Integer notificationId;
    private Integer userId;
    private String message;
    private TypeNotification type;
    private String targetUrl;
    private Boolean isRead;
    private LocalDateTime createdAt;
}