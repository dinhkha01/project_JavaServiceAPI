package com.example.courses.repository;

import com.example.courses.model.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserUsernameOrderByCreatedAtDesc(String username);

    // Use notificationId instead of id
    Notification findByNotificationIdAndUserUsername(Integer notificationId, String username);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);
}