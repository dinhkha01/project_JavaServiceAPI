package com.example.courses.service.impl;

import com.example.courses.exception.NotFoundException;
import com.example.courses.model.dto.request.NotificationRequest;
import com.example.courses.model.dto.response.NotificationResponse;
import com.example.courses.model.entity.Notification;
import com.example.courses.model.entity.User;
import com.example.courses.repository.NotificationRepository;
import com.example.courses.repository.UserRepository;
import com.example.courses.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public List<NotificationResponse> getUserNotifications() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Notification> notifications = notificationRepository.findByUserUsernameOrderByCreatedAtDesc(username);
        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void markNotificationAsRead(Integer notificationId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Notification notification = notificationRepository.findByNotificationIdAndUserUsername(notificationId, username);


        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    @Override
    public NotificationResponse createNotification(NotificationRequest request) throws NotFoundException {
        // Authorization check moved here from controller
        if (!SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Only ADMIN can create notifications");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Notification notification = new Notification();
        notification.setUserId(user.getUserId());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setTargetUrl(request.getTargetUrl());
        notification.setIsRead(false);

        Notification savedNotification = notificationRepository.save(notification);
        return convertToResponse(savedNotification);
    }

    @Override
    public void deleteNotification(Integer notificationId) throws NotFoundException {
        // Authorization check moved here from controller
        if (!SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Only ADMIN can delete notifications");
        }

        if (!notificationRepository.existsById(notificationId)) {
            throw new NotFoundException("Notification not found");
        }
        notificationRepository.deleteById(notificationId);
    }

    private NotificationResponse convertToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setNotificationId(notification.getNotificationId());
        response.setUserId(notification.getUserId());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType());
        response.setTargetUrl(notification.getTargetUrl());
        response.setIsRead(notification.getIsRead());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
}