package com.example.courses.service;

import com.example.courses.exception.NotFoundException;
import com.example.courses.model.dto.request.NotificationRequest;
import com.example.courses.model.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getUserNotifications();
    void markNotificationAsRead(Integer notificationId);
    NotificationResponse createNotification(NotificationRequest request) throws NotFoundException;
    void deleteNotification(Integer notificationId) throws NotFoundException;
}