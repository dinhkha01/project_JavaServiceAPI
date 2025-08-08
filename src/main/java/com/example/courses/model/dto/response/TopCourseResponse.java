package com.example.courses.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopCourseResponse {

    private Integer courseId;
    private String title;
    private String description;
    private Integer teacherId;
    private String teacherName;
    private BigDecimal price;
    private Integer durationHours;
    private String status;
    private Integer totalLessons;
    private Integer publishedLessons;
    private Double averageRating;
    private Integer totalEnrollments;
    private Integer totalReviews;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}