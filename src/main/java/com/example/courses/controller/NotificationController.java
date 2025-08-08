package com.example.courses.controller;

import com.example.courses.exception.NotFoundException;
import com.example.courses.model.dto.request.NotificationRequest;
import com.example.courses.model.dto.response.DataResponse;
import com.example.courses.model.dto.response.NotificationResponse;
import com.example.courses.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<DataResponse<List<NotificationResponse>>> getUserNotifications() {
        List<NotificationResponse> notifications = notificationService.getUserNotifications();
        return ResponseEntity.ok(DataResponse.success(notifications));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<DataResponse<Void>> markNotificationAsRead(@PathVariable Integer notificationId) {
        notificationService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok(DataResponse.success("Notification đã được đánh dấu là đã đọc"));
    }

    @PostMapping
    public ResponseEntity<DataResponse<NotificationResponse>> createNotification(
            @RequestBody @Valid NotificationRequest request) throws NotFoundException {
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.ok(DataResponse.success(response));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<DataResponse<Void>> deleteNotification(@PathVariable Integer notificationId) throws NotFoundException {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(DataResponse.success("Notification đã được xóa thành công"));
    }
}