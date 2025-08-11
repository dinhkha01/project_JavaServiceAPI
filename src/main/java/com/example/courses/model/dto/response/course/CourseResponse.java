package com.example.courses.model.dto.response.course;

import com.example.courses.model.entity.CourseStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
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
public class CourseResponse {
    private Integer courseId;
    private String title;
    private String description;
    private Integer teacherId;
    private String teacherName;
    private BigDecimal price;
    private Integer durationHours;
    private CourseStatus status;
    private Integer totalLessons;
    private Integer publishedLessons;
    private Double averageRating;
    private Integer totalEnrollments;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}